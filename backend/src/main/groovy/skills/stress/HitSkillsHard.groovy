/**
 * Copyright 2020 SkillTree
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package skills.stress


import callStack.profiler.ProfThreadPool
import groovy.json.JsonSlurper
import groovy.util.logging.Slf4j
import skills.stress.errors.ErrorTracker
import skills.stress.model.StatsRes
import skills.stress.model.StatusRes
import skills.stress.services.SkillServiceFactory
import skills.stress.services.SkillsService
import skills.stress.stats.StatsHelper
import skills.stress.users.*

import java.util.concurrent.Callable
import java.util.concurrent.Future
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger

@Slf4j
class HitSkillsHard {

    int numProjects = 10
    int subjPerProject = 6
    int skillsPerSubject = 50
    int badgesPerProject = 10
    int hasDependenciesEveryNProjects = 5
    int numUsersPerApp = 100
    int numConcurrentThreads = 5
    int numMaxWebsocketClients = 25
    long sleepMsBetweenRequests = 250
    boolean removeExistingTestProjects = false
    String serviceUrl = "http://localhost:8080"
    boolean pkiMode = false
    String pkiModeUserIdFilePath
    String prependToDescription = ""
    Date start = new Date()

    private AtomicBoolean shouldRun = new AtomicBoolean(true)
    private AtomicInteger numErrors = new AtomicInteger(0)
    private boolean hasFailures = false
    CreateSkillsDef createSkillsDef
    DateFactory userAndDateFactory
    UserIdFactory userIdFactory
    SkillServiceFactory skillServiceFactory
    ErrorTracker errorTracker
    WebSocketClientManager webSocketClientManager

    void stop() {
        shouldRun.set(false)

        if (futures) {
            // recommended to use following statement to ensure the execution of all tasks
            log.info("Started All Threads")
            futures.each { it.get() }

            log.info("All Threads Completed")
            profThreadPool?.shutdown()
        }
    }

    boolean isRunning() {
        shouldRun.get()
    }

    HitSkillsHard init() {
        webSocketClientManager = new WebSocketClientManager(pkiMode: pkiMode, maxClients: numMaxWebsocketClients)

        if (pkiMode) {
            userIdFactory = FileBasedUserIdFactory.build(pkiModeUserIdFilePath)
            if (numProjects > userIdFactory.numUsers()){
                throw new IllegalArgumentException("In PKI Mode must not supply more projects than actual users. [$numProjects] > [${userIdFactory.numUsers()}]")
            }
        } else {
            userIdFactory = SimpleUserIdFactory.build(numUsersPerApp)
        }

        userIdFactory.addActiveUserRemovedListener(new RemovedListener<UserWithExpiration>() {
            @Override
            void itemRemoved(UserWithExpiration item) {
                log.debug("removing all websocket clients for user [${item.userId}]")
                webSocketClientManager.cleanUpUser(item.userId)
            }
        })

        userAndDateFactory = new DateFactory(
                numDates: 365
        )

        skillServiceFactory = new SkillServiceFactory(serviceUrl: serviceUrl, pkiMode: pkiMode, userIdFactory: userIdFactory, errorTracker: errorTracker)

        createSkillsDef = new CreateSkillsDef(
                numProjects: numProjects,
                subjPerProject: subjPerProject,
                skillsPerSubject: skillsPerSubject,
                badgesPerProject: badgesPerProject,
                hasDependenciesEveryNProjects: hasDependenciesEveryNProjects,
                remove: removeExistingTestProjects,
                skillServiceFactory: skillServiceFactory,
                prependToDescription: prependToDescription,
        )

        return this;
    }

    private ProfThreadPool profThreadPool
    private List<Future> futures
    void run() {
        assert numConcurrentThreads > 1

        printSetupParams()
        try {
            createSkillsDef.create()
        } catch (Throwable t){
            hasFailures = true;
            shouldRun.set(false)
            throw t;
        }

        int numClientDisplayThreads = Math.max(1, (numConcurrentThreads * 0.2).toInteger())
        int numReportEventThreads = numConcurrentThreads - numClientDisplayThreads
        log.info("[{}] client display threads, [{}] report events threads", numClientDisplayThreads, numReportEventThreads)

        profThreadPool = new ProfThreadPool("reportEventsPool", numConcurrentThreads)
        profThreadPool.warnIfFull = false
        futures = []

        numReportEventThreads.times {
            futures.add(profThreadPool.submit({ -> reportEvents() } as Callable))
        }

        numClientDisplayThreads.times {
            futures.add(profThreadPool.submit({ -> clientDisplaySimulation() } as Callable))
        }
    }

    StatsHelper statsHelper = new StatsHelper()
    StatsHelper clientDisplayStatsHelper = new StatsHelper()

    JsonSlurper jsonSlurper = new JsonSlurper()
    void reportEvents() {
        performWorkInThread { SkillsService service, CreateSkillsDef.RandomLookupKey randomLookupKey ->
            // time consuming lookup for large number of users
            String userId = userIdFactory.userId
            Date date = userAndDateFactory.date
            Map defParams = [projectId: randomLookupKey.projId, skillId: randomLookupKey.skillId]
            //at the moment, we don't need to do anything with the actual client that is created here
            //this will impact timing metrics, however each userId/projId pairing is only created once
            webSocketClientManager.get(userId, randomLookupKey.projId, service)

            String res
            statsHelper.startEvent()
            res = service.addSkill(defParams, userId, date)
            statsHelper.endEvent(res)
        }
    }

    void clientDisplaySimulation() {
        performWorkInThread { SkillsService service, CreateSkillsDef.RandomLookupKey randomLookupKey ->
            String userId = userIdFactory.userId
            String projectId = randomLookupKey.projId

            clientDisplayStatsHelper.startEvent()
            service.getClientDisplayProjectSummary(projectId, userId)
            clientDisplayStatsHelper.endEvent()
        }
    }

    private performWorkInThread(Closure work) {
        try {
            log.info("Thread [{}] started", Thread.currentThread().name)
            while (shouldRun.get()) {
                CreateSkillsDef.RandomLookupKey randomLookupKey = createSkillsDef.randomLookupKey()
                SkillsService service = skillServiceFactory.getServiceByProjectIndex(randomLookupKey.projIndex)
                try {
                    work.call(service, randomLookupKey)
                } catch (Throwable t){
                    log.error("Thread [${Thread.currentThread().name}] had failure", t)
                    numErrors.incrementAndGet()
                }

                if (sleepMsBetweenRequests > 0 ) {
                    try {
                        Thread.sleep(sleepMsBetweenRequests)
                    } catch (InterruptedException ie) {
                        log.error("Failed to sleep", ie)
                    }
                }
            }
        } catch (Exception e) {
            shouldRun.set(false)
            hasFailures = true
            log.error("Thread [${Thread.currentThread().name}] FAILED", e)
        } finally {
            log.info("Thread [{}] Finished", Thread.currentThread().name)
        }
    }

    StatusRes buildStatus() {
        StatsRes reportSkillsRes = statsHelper.buildStatus()
        new StatusRes(
                startTimestamp: start.time,
                numErrors: numErrors.get(),
                hasFailures: hasFailures,
                running: isRunning(),
                reportSkillsRes: reportSkillsRes,
                clientDisplayStats: clientDisplayStatsHelper.buildStatus()
        )
    }

    void printSetupParams() {
        List<String> res = [
                "\n--------------------------------------",
                "Number of projects: ${numProjects}",
                "Number of subjects per project: ${subjPerProject}",
                "Number of badges per project: ${badgesPerProject}",
                "Every N project has dependencies: ${hasDependenciesEveryNProjects}",
                "Number of skills per subject: ${skillsPerSubject}",
                "Number of users per project: ${numUsersPerApp}",
                "Number of concurrent skill reporters: ${numConcurrentThreads}",
                "--------------------------------------",
        ]
        log.info(res.join("\n"))
    }
}

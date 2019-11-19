package skills.stress

import callStack.profiler.ProfThreadPool
import groovy.util.logging.Slf4j
import skills.intTests.utils.SkillsClientException
import skills.intTests.utils.SkillsService
import skills.stress.model.ReportSkillsRes
import skills.stress.model.StatusRes

import java.util.concurrent.Callable
import java.util.concurrent.Future
import java.util.concurrent.atomic.AtomicBoolean

@Slf4j
class HitSkillsHard {

    int numProjects = 10
    int subjPerProject = 6
    int skillsPerSubject = 50
    int badgesPerProject = 10
    int hasDependenciesEveryNProjects = 5
    int numUsersPerApp = 100
    int numConcurrentThreads = 5
    boolean removeExistingTestProjects = false
    String serviceUrl = "http://localhost:8080"

    private AtomicBoolean shouldRun = new AtomicBoolean(true)

    CreateSkillsDef createSkillsDef
    UserAndDateFactory userAndDateFactory
    SkillServiceFactory skillServiceFactory

    void stop() {
        shouldRun.set(false)
    }

    boolean isRunning() {
        shouldRun.get()
    }

    HitSkillsHard init() {
        skillServiceFactory = new SkillServiceFactory(serviceUrl: serviceUrl)

        createSkillsDef = new CreateSkillsDef(
                numProjects: numProjects,
                subjPerProject: subjPerProject,
                skillsPerSubject: skillsPerSubject,
                badgesPerProject: badgesPerProject,
                hasDependenciesEveryNProjects: hasDependenciesEveryNProjects,
                remove: removeExistingTestProjects,
                skillServiceFactory: skillServiceFactory,
        )

        userAndDateFactory = new UserAndDateFactory(
                numUsers: numUsersPerApp,
                numDates: 365
        )

        return this;
    }

    void run() {
        printSetupParams()
        createSkillsDef.create()

        ProfThreadPool profThreadPool = new ProfThreadPool("reportEventsPool", numConcurrentThreads)
        profThreadPool.warnIfFull = false

        try {
            List<Future> futures = numConcurrentThreads.times {
                profThreadPool.submit({ ->
                    reportEvents()
                } as Callable);
            }
            // recommended to use following statement to ensure the execution of all tasks.
            futures.each { it.get() }
            log.info("Started All Threads")
        } finally {
            profThreadPool.shutdown()
        }
    }

    StatsHelper statsHelper = new StatsHelper()

    void reportEvents() {
        try {
            println "Thread started"
            while (shouldRun.get()) {
                CreateSkillsDef.RandomLookupKey randomLookupKey = createSkillsDef.randomLookupKey()
                while (randomLookupKey.projId == "Project90") {
                    randomLookupKey = createSkillsDef.randomLookupKey()
                }

                SkillsService service = skillServiceFactory.getService(randomLookupKey.projId)
                statsHelper.startEvent()
                try {
                    service.addSkill([projectId: randomLookupKey.projId, skillId: randomLookupKey.skillId], userAndDateFactory.userId, userAndDateFactory.date)
                } catch (SkillsClientException skillsClientException) {
                    if (skillsClientException.message.contains("Skill definition does not exist.")) {
                        // that's ok
                        log.error("Swallowed", skillsClientException)
                    } else {
                        log.error("Throwing Exception", skillsClientException)
                        throw skillsClientException
                    }

                }
                statsHelper.endEvent()
            }
        } catch (Exception e) {
            log.error("Failed Thread", e)
        } finally {
            log.info("Thread Finished")
        }
    }


    StatusRes buildStatus() {
        ReportSkillsRes reportSkillsRes = statsHelper.buildStatus()
        new StatusRes(
                running: isRunning(),
                reportSkillsRes: reportSkillsRes,
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

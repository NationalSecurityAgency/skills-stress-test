package skills.stress

import callStack.profiler.ProfThreadPool
import groovy.util.logging.Slf4j
import skills.stress.model.ReportSkillsRes
import skills.stress.model.StatusRes
import skills.stress.services.SkillServiceFactory
import skills.stress.services.SkillsService
import skills.stress.users.FileBasedUserIdFactory
import skills.stress.users.SimpleUserIdFactory
import skills.stress.users.DateFactory
import skills.stress.users.UserIdFactory

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
    boolean pkiMode = false
    String pkiModeUserIdFilePath
    String prependToDescription = ""

    private AtomicBoolean shouldRun = new AtomicBoolean(true)
    private boolean hasFailures = false
    CreateSkillsDef createSkillsDef
    DateFactory userAndDateFactory
    UserIdFactory userIdFactory
    SkillServiceFactory skillServiceFactory

    void stop() {
        shouldRun.set(false)
    }

    boolean isRunning() {
        shouldRun.get()
    }

    HitSkillsHard init() {
        if (pkiMode) {
            userIdFactory = new FileBasedUserIdFactory(pkiModeUserIdFilePath)
            if (numProjects > userIdFactory.numUsers()){
                throw new IllegalArgumentException("In PKI Mode must not supply more projects than actual users. [$numProjects] > [${userIdFactory.numUsers()}]")
            }
        } else {
            userIdFactory = new SimpleUserIdFactory(numUsers: numUsersPerApp)
        }
        userAndDateFactory = new DateFactory(
                numDates: 365
        )

        skillServiceFactory = new SkillServiceFactory(serviceUrl: serviceUrl, pkiMode: pkiMode, userIdFactory: userIdFactory)

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

    void run() {
        printSetupParams()
        try {
            createSkillsDef.create()
        } catch (Throwable t){
            hasFailures = true;
            shouldRun.set(false)
            throw t;
        }

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
            log.info("Thread [{}] started", Thread.currentThread().name)
            while (shouldRun.get()) {
                CreateSkillsDef.RandomLookupKey randomLookupKey = createSkillsDef.randomLookupKey()
                SkillsService service = skillServiceFactory.getServiceByProjectIndex(randomLookupKey.projIndex)
                statsHelper.startEvent()
//                try {
                    service.addSkill([projectId: randomLookupKey.projId, skillId: randomLookupKey.skillId], userIdFactory.userId, userAndDateFactory.date)
//                } catch (SkillsClientException skillsClientException) {
//                    if (skillsClientException.message.contains("Skill definition does not exist.")) {
//                        // addresses scenario where a project was created but then config changed to increase number of skill defs
//                        log.error("Thread [${Thread.currentThread().name}] Swallowed", skillsClientException)
//                    } else {
//                        log.error("Thread [${Thread.currentThread().name}] Throwing Exception", skillsClientException)
//                        throw skillsClientException
//                    }
//
//                }
                statsHelper.endEvent()
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
        ReportSkillsRes reportSkillsRes = statsHelper.buildStatus()
        new StatusRes(
                hasFailures: hasFailures,
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

package skills.stress

import callStack.profiler.ProfThreadPool
import groovy.util.logging.Slf4j
import org.springframework.http.ResponseEntity
import org.springframework.web.client.RestTemplate
import skills.intTests.utils.SkillsService
import skills.stress.users.FileBasedUserIdFactory
import skills.stress.users.UserIdFactory

import java.util.concurrent.Callable

@Slf4j
class SimpleMain {

    static void main(String[] args) {
        CreateSkillsDef skillsDef = new CreateSkillsDef()
        List<CreateSkillsDef.Proj> projs = skillsDef.createDef()

        UserIdFactory userIdFactory = new FileBasedUserIdFactory("/home/dimay/dev/testData/pkiUsers.txt")
        SkillServiceFactory factory = new SkillServiceFactory(serviceUrl: "https://localhost:8443", pkiMode: true, userIdFactory: userIdFactory)
        SkillsService service = factory.getServiceByProjectIndex(1)

//        CreateSkillsDef.Proj proj = projs.first()
//        service.createProject([projectId: proj.id, name: proj.name])
//        log.info("Created project [${proj}]")
//
//        service = factory.getServiceByProjectIndex(2)
//        proj = projs.get(1)
//        service.createProject([projectId: proj.id, name: proj.name])
//        log.info("Created project [${proj}]")
//
//        service = factory.getServiceByProjectIndex(3)
//        proj = projs.get(2)
//        service.createProject([projectId: proj.id, name: proj.name])
//        log.info("Created project [${proj}]")


        List<Callable> callables = projs.collect() { CreateSkillsDef.Proj proj ->
            { ->
                try {
                    log.info("Thread [{}] started", Thread.currentThread().name)
                    SkillsService skillsService = factory.getServiceByProjectIndex(proj.projIndex)
                    skillsService.createProject([projectId: proj.id, name: proj.name])
                } catch (Throwable t) {
                    log.error("Thread [${Thread.currentThread().name}] FAILED", t)
                    throw t
                } finally {
                    log.info("Thread [{}] finished", Thread.currentThread().name)
                }
                return proj
            } as Callable
        }

        int numThreads = 5
        ProfThreadPool threadPool = new ProfThreadPool("createProjects", numThreads)
        threadPool.warnIfFull = false
        try {
            threadPool.asyncExec(callables)
        } finally {
            threadPool.shutdown()
        }



//        RestTemplate restTemplate = new RestTemplate()
//        CreateSkillsDef.Proj proj = projs.first()
//        ResponseEntity<String> responseEntity = restTemplate.postForEntity("https://localhost:8443/app/projects/${proj.id}", [projectId: proj.id, name: proj.name], String)
//
//        proj = projs.get(1)
//        ResponseEntity<String> responseEntity1 = restTemplate.postForEntity("https://localhost:8443/app/projects/${proj.id}", [projectId: proj.id, name: proj.name], String)

//        CreateSkillsDef.Subj subj = proj.subjs.first()
//        String subjId = subj.id
//        service.createSubject([projectId: proj.id, subjectId: subjId, name: subj.name.toString()])

//        proj.subjs.each { CreateSkillsDef.Subj subj ->
//            String subjId = subj.id
//            service.createSubject([projectId: proj.id, subjectId: subjId, name: subj.name.toString()])
//            log.info("Created subject [${subj}]")
//        subj.skills.each { Skill skill ->
//            service.createSkill([projectId  : proj.id, subjectId: subjId, skillId: skill.id,
//                                 name       : skill.name,
//                                 type       : "Skill", pointIncrement: 10, numPerformToCompletion: 10, pointIncrementInterval: 8 * 60, numMaxOccurrencesIncrementInterval: 2,
//                                 version    : 0,
//                                 description: "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum."
//            ])
//            long value = counter.incrementAndGet()
//            if (value > 9 && value % 25 == 0) {
//                log.info("Created [{}] skills. Completed %{}", value, (((double) value / (double) (numProjects * subjPerProject * skillsPerSubject)) * 100).trunc(2))
//            }
//        }
//        }
    }
}

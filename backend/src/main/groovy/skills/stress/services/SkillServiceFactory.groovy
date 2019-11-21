package skills.stress.services

import skills.stress.CreateSkillsDef
import skills.stress.services.SkillsService
import skills.stress.users.UserIdFactory

class SkillServiceFactory {

    String serviceUrl = "http://localhost:8080"
    boolean pkiMode = false
    private Map<String, SkillsService> cache = Collections.synchronizedMap([:])
    UserIdFactory userIdFactory

    synchronized SkillsService getServiceByProjectIndex(Integer projectIndex) {
        assert serviceUrl
        assert userIdFactory
        String projectId = CreateSkillsDef.getProjectId(projectIndex)
        SkillsService service = cache.get(projectId)
        if (!service) {
            service = new SkillsService(serviceUrl: serviceUrl)
            cache.put(projectId, service)
        }
        return service
    }
}

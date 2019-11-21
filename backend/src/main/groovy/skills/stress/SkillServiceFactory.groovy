package skills.stress

import skills.intTests.utils.SkillsService
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
            service = new SkillsService("CN=dimay, OU=skills.org, O=Skills, L=SomeCity, ST=SomeState, C=CC", "password", "First", "Last", serviceUrl, pkiMode)
            cache.put(projectId, service)
        }
        return service
    }
}

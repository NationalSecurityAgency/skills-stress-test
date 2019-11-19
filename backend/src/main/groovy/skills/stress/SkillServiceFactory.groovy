package skills.stress

import skills.intTests.utils.SkillsService

class SkillServiceFactory {

    String serviceUrl = "http://localhost:8080"
    private Map<String, SkillsService> cache = Collections.synchronizedMap([:])

    synchronized SkillsService getService(String projectId) {
        assert serviceUrl
        SkillsService service = cache.get(projectId)
        if (!service) {
            service = new SkillsService("${projectId}user".toString(), "password", "First", "Last", serviceUrl, true)
            cache.put(projectId, service)
        }
        return service
    }
}

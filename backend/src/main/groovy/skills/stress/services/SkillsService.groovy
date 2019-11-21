package skills.stress.services


import groovy.util.logging.Slf4j
import org.springframework.http.ResponseEntity
import org.springframework.web.client.RestTemplate

@Slf4j
class SkillsService {

    String serviceUrl

    RestTemplate restTemplate = new RestTemplate()

    private def post(String url, Map params) {
        ResponseEntity<String> responseEntity =
                restTemplate.postForEntity(url.toString(), params, String)
        return responseEntity.body
    }

    private def get(String url){
        ResponseEntity<String> responseEntity =
                restTemplate.getForEntity(url.toString(), String)
        return responseEntity.body
    }

    def createProject(Map params) {
        post("${serviceUrl}/app/projects/${params.projectId}", params)
    }

    def getProjects() {
        get("${serviceUrl}/app/projects")
    }

    def deleteProject(String projId) {
        restTemplate.delete("${serviceUrl}/admin/projects/${projId}")
    }

    boolean projectIdExists(Map params) {
        def id = URLEncoder.encode(params.projectId, 'UTF-8')
        def res = get("${serviceUrl}/app/projectExist?projectId=${id}")
        return Boolean.valueOf(res.toString());
    }

    def createSubject(Map params) {
        post("${serviceUrl}/admin/projects/${params.projectId}/subjects/${params.subjectId}", params)
    }

    def assignDependency(Map params) {
        post("${serviceUrl}/admin/projects/${params.projectId}/skills/${params.skillId}/dependency/${params.dependentSkillId}", params)
    }

    def createBadge(Map params) {
        post("${serviceUrl}/admin/projects/${params.projectId}/badges/${params.badgeId}", params)
    }

    def assignSkillToBadge(Map params) {
        post("${serviceUrl}/admin/projects/${params.projectId}/badge/${params.badgeId}/skills/${params.skillId}", params)
    }

    def createSkill(Map params) {
        post("${serviceUrl}/admin/projects/${params.projectId}/subjects/${params.subjectId}/skills/${params.skillId}", params)
    }

    def addSkill(Map params, String userId, Date date) {
        def clientParams = [
                userId   : userId,
                timestamp: date.time
        ]
        String url = "${serviceUrl}/api/projects/${params.projectId}/skills/${params.skillId}"

        post(url, clientParams)
    }

}

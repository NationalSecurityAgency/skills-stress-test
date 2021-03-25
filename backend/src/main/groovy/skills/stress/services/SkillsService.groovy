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
package skills.stress.services

import callStack.profiler.Profile
import groovy.json.JsonSlurper
import groovy.util.logging.Slf4j
import org.apache.commons.io.IOUtils
import org.springframework.http.*
import org.springframework.http.client.ClientHttpResponse
import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.ResponseErrorHandler
import org.springframework.web.client.RestTemplate
import skills.stress.RestTemplateHelper
import skills.stress.errors.ErrorTracker

import java.nio.charset.Charset

@Slf4j
class SkillsService {

    String serviceUrl
    boolean pkiMode

    RestTemplate restTemplate

    SkillsService(String serviceUrl, boolean pkiMode, ErrorTracker errorTracker) {
        if(!pkiMode) {
            restTemplate = new RestTemplate(RestTemplateHelper.getTrustAllRequestFactory())
        } else {
            restTemplate = new RestTemplate()
        }
        this.serviceUrl = serviceUrl;

        this.pkiMode = pkiMode
//        restTemplate.setRequestFactory(new HttpComponentsClientHttpRequestFactory());
        restTemplate.setErrorHandler(new ResponseErrorHandler() {
            @Override
            boolean hasError(ClientHttpResponse clientHttpResponse) throws IOException {
                if (clientHttpResponse.getStatusCode() != HttpStatus.OK) {
                    StringBuilder msg = new StringBuilder()
                    HttpStatus status = clientHttpResponse.getStatusCode();
                    String body = IOUtils.toString(clientHttpResponse.getBody(), Charset.defaultCharset())
                    String response = clientHttpResponse.getStatusText();
                    msg.append("Status code: [" + status + "]\n");
                    msg.append("Response: [" + response + "]\n");
                    msg.append("Body: " + body);
                    log.error(msg.toString());

                    errorTracker.track(status.toString(), response, body)

                    return true
                }
                return false
            }

            @Override
            void handleError(ClientHttpResponse clientHttpResponse) throws IOException {

            }

            @Override
            void handleError(URI url, HttpMethod method, ClientHttpResponse clientHttpResponse) throws IOException {
                StringBuilder msg = new StringBuilder()
                msg.append("RestTemplate go an error for [${method}] => [${url}]\n")
                msg.append("Status code: [" + clientHttpResponse.getStatusCode() + "]\n");
                msg.append("Response: [" + clientHttpResponse.getStatusText() + "]\n");
                msg.append("Body: " + IOUtils.toString(clientHttpResponse.getBody(), Charset.defaultCharset()));
//                log.error(msg.toString());
                throw new HttpClientErrorException(clientHttpResponse.statusCode, msg.toString())
            }
        })

        if (!pkiMode) {
            auth("user1")
        }
    }
    static final String AUTH_HEADER = 'Authorization'
    String authenticationToken;

    private void auth(String username, String password = "password", String firstName = "first", lastName = "last") {
        createAccount(serviceUrl, username, password, firstName, lastName)
        HttpHeaders headers = new HttpHeaders()
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED)
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>()
        params.add('username', username)
        params.add('password', password)

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers)
        ResponseEntity<String> response = restTemplate.postForEntity(serviceUrl + '/performLogin', request, String.class)

        assert response.statusCode == HttpStatus.OK, 'authentication failed: ' + response.statusCode

        authenticationToken = response.getHeaders().getFirst(AUTH_HEADER)
//        assert authenticationToken, 'no authentication token was provided!'
//        authenticated = true
    }

    private boolean createAccount(String skillsServiceUrl, String username, String password, String firstName, String lastName) {
        ResponseEntity<String> userExistsResponse = restTemplate.getForEntity("${skillsServiceUrl}/app/users/validExistingDashboardUserId/{userId}", String, username)
        boolean userExists = Boolean.valueOf(userExistsResponse.body)
        if (!userExists) {
            Map<String, String> userInfo = [
                    firstName: firstName,
                    lastName : lastName,
                    email    : username,
                    password : password,
            ]
            restTemplate.put(skillsServiceUrl + '/createAccount', userInfo)
//            if ( response.statusCode != HttpStatus.OK) {
//                throw new RuntimeException("${response.body}, url=${skillsServiceUrl}, code=${response.statusCode}")
//            }
            return true
        }
        return false
    }

    @Profile
    private def post(String url, Map params) {
        try {
            HttpEntity entity = getHttpEntity(params)
            ResponseEntity<String> responseEntity = restTemplate.exchange(url.toString(), HttpMethod.POST, entity, String.class, params)
            return responseEntity.body
        } catch (Throwable t) {
            throw new RuntimeException("Failed for url = [$url], params = [$params]", t)
        }
    }

    JsonSlurper jsonSlurper = new JsonSlurper()

    private def get(String url) {
        ResponseEntity<String> responseEntity = restTemplate.exchange(url.toString(), HttpMethod.GET, getHttpEntity(), String.class)
        return jsonSlurper.parseText(responseEntity.body)
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
        def res = post("${serviceUrl}/app/projectExist".toString(), params)
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

    @Profile
    def addSkill(Map params, String userId, Date date) {
        def clientParams = [
                userId   : userId,
                timestamp: date.time
        ]
        String url = "${serviceUrl}/api/projects/${params.projectId}/skills/${params.skillId}"

        return post(url, clientParams)
    }

    def getClientDisplayProjectSummary(String projId, String userId) {
        get("${serviceUrl}/api/projects/${projId}/summary?userId=${userId}")
    }

    def getClientDisplaySubjectSummary(String projId, String subjId, String userId) {
        get("${serviceUrl}/api/projects/${projId}/subjects/${subjId}/summary?userId=${userId}")
    }

    def getHttpEntity(Map params){
        HttpHeaders headers = new HttpHeaders()
        headers.setContentType(MediaType.APPLICATION_JSON)
        headers.setAccept([MediaType.APPLICATION_JSON])
        if (authenticationToken) {
            headers.set(AUTH_HEADER, "Bearer ${authenticationToken}")
        }
        HttpEntity entity = new HttpEntity(params, headers)

        return entity
    }

    String getClientSecret(String projectId){
        ResponseEntity<String> responseEntity = restTemplate.getForEntity("${serviceUrl}/admin/projects/${projectId}/clientSecret".toString(), String)
        responseEntity.getBody()
    }

}

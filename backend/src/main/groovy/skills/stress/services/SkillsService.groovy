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

import groovy.json.JsonSlurper
import groovy.util.logging.Slf4j
import org.apache.commons.io.IOUtils
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.http.client.ClientHttpResponse
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory
import org.springframework.web.client.DefaultResponseErrorHandler
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.ResponseErrorHandler
import org.springframework.web.client.RestTemplate

import java.nio.charset.Charset

@Slf4j
class SkillsService {

    String serviceUrl

    RestTemplate restTemplate = new RestTemplate()

    SkillsService() {
        restTemplate.setRequestFactory(new HttpComponentsClientHttpRequestFactory());
        restTemplate.setErrorHandler(new DefaultResponseErrorHandler() {
            public boolean hasError(ClientHttpResponse response) throws IOException {
                HttpStatus statusCode = response.getStatusCode();
                return statusCode.series() == HttpStatus.Series.SERVER_ERROR;
            }
        });
//        restTemplate.setErrorHandler(new ResponseErrorHandler() {
//            @Override
//            boolean hasError(ClientHttpResponse clientHttpResponse) throws IOException {
//                if (clientHttpResponse.getStatusCode() != HttpStatus.OK) {
//                    StringBuilder msg = new StringBuilder()
//                    msg.append("Status code: [" + clientHttpResponse.getStatusCode() + "]\n");
//                    msg.append("Response: [" + clientHttpResponse.getStatusText() + "]\n");
//                    msg.append("Body: " + IOUtils.toString(clientHttpResponse.getBody(), Charset.defaultCharset()));
//                    log.error(msg.toString());
//                    return true
//                }
//                return false
//            }
//
//            @Override
//            void handleError(ClientHttpResponse clientHttpResponse) throws IOException {
//
//            }
//
//            @Override
//            void handleError(URI url, HttpMethod method, ClientHttpResponse clientHttpResponse) throws IOException {
//                StringBuilder msg = new StringBuilder()
//                msg.append("RestTemplate go an error for [${method}] => [${url}]\n")
//                msg.append("Status code: [" + clientHttpResponse.getStatusCode() + "]\n");
//                msg.append("Response: [" + clientHttpResponse.getStatusText() + "]\n");
//                msg.append("Body: " + IOUtils.toString(clientHttpResponse.getBody(), Charset.defaultCharset()));
//                log.error(msg.toString());
//                throw new HttpClientErrorException(clientHttpResponse.statusCode, msg.toString())
//            }
//        })
    }

    private def post(String url, Map params) {
        ResponseEntity<String> responseEntity =
                restTemplate.postForEntity(url.toString(), params, String)
        return responseEntity.body
    }

    JsonSlurper jsonSlurper = new JsonSlurper()

    private def get(String url) {
        ResponseEntity<String> responseEntity =
                restTemplate.getForEntity(url.toString(), String)
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

    def addSkill(Map params, String userId, Date date) {
        def clientParams = [
                userId   : userId,
                timestamp: date.time
        ]
        String url = "${serviceUrl}/api/projects/${params.projectId}/skills/${params.skillId}"

        post(url, clientParams)
    }

    def getClientDisplayProjectSummary(String projId, String userId) {
        get("${serviceUrl}/api/projects/${projId}/summary?userId=${userId}")
    }

    def getClientDisplaySubjectSummary(String projId, String subjId, String userId) {
        get("${serviceUrl}/api/projects/${projId}/subjects/${subjId}/summary?userId=${userId}")
    }

}

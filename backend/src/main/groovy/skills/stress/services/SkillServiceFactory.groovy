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

import groovy.util.logging.Slf4j
import skills.stress.CreateSkillsDef
import skills.stress.services.SkillsService
import skills.stress.users.UserIdFactory

@Slf4j
class SkillServiceFactory {

    String serviceUrl = "http://localhost:8080"
    boolean pkiMode = false
    private final Map<String, SkillsService> cache = Collections.synchronizedMap([:])
    UserIdFactory userIdFactory

    synchronized SkillsService getServiceByProjectIndex(Integer projectIndex) {
        assert serviceUrl
        assert userIdFactory
        String projectId = CreateSkillsDef.getProjectId(projectIndex)
        SkillsService service = cache.get(projectId)
        if (!service) {
            service = new SkillsService(serviceUrl, pkiMode)
            cache.put(projectId, service)
            log.info("Initialized service for [${projectId}]. Cache size=[${cache.size()}]")
        }
        return service
    }
}

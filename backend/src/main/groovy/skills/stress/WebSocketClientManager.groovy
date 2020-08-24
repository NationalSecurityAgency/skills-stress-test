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
package skills.stress

import org.springframework.cache.Cache
import org.springframework.cache.concurrent.ConcurrentMapCache
import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap
import skills.stress.services.SkillsService

import java.util.concurrent.locks.ReadWriteLock
import java.util.concurrent.locks.ReentrantReadWriteLock

class WebSocketClientManager {

    boolean pkiMode = true

    private ReadWriteLock clientLock = new ReentrantReadWriteLock()

    private MultiValueMap<String, String> userToClientIdCache = new LinkedMultiValueMap<>()
    private Map<String, WebSocketClient> userProjectWebSocketClient = new HashMap<>()

    public WebSocketClient get(String user, String project, SkillsService skillsService) {
        String key = buildKey(user, project)
        clientLock.readLock().lock()
        try {
            WebSocketClient webSocketClient = userProjectWebSocketClient.get(key)
            if(!webSocketClient) {
                clientLock.readLock().unlock()
                clientLock.writeLock().lock()
                try {
                    //re-check just in case another thread changed the state before we could acquire the write lock
                    webSocketClient = userProjectWebSocketClient.get(key)
                    if(!webSocketClient) {
                        webSocketClient = new WebSocketClient(userId: user, projId: project, serviceUrl: skillsService.getServiceUrl(), skillsService: skillsService)
                        webSocketClient = webSocketClient.init(pkiMode)
                        userToClientIdCache.add(user, key)
                        userProjectWebSocketClient.put(key, webSocketClient)
                    }
                }finally {
                    clientLock.writeLock().unlock()
                    clientLock.readLock().lock()
                }
            }
            return webSocketClient
        } finally {
            clientLock.readLock().unlock()
        }
    }

    public void cleanUpUser(String user) {
        clientLock.writeLock().lock()
        try {
            List<String> clientIds = userToClientIdCache.remove(user)
            clientIds?.each {
                WebSocketClient client = userProjectWebSocketClient.remove(it)
                client?.close()
            }
        } finally {
            clientLock.writeLock().unlock()
        }
    }

    private static String buildKey(String user, String projectId) {
        return "${user}_${projectId}"
    }


}

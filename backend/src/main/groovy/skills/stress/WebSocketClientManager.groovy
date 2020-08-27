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

import callStack.profiler.Profile
import groovy.util.logging.Slf4j
import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap
import skills.stress.services.SkillsService

import java.util.concurrent.locks.ReadWriteLock
import java.util.concurrent.locks.ReentrantReadWriteLock

@Slf4j
class WebSocketClientManager {

    boolean pkiMode = true
    int maxClients = 10

    private ReadWriteLock clientLock = new ReentrantReadWriteLock()

    private MultiValueMap<String, String> userToClientIdCache = new LinkedMultiValueMap<>()
    private Map<String, WebSocketClient> clientIdToWebSocketClient = new HashMap<>()
    private int clientCount = 0

    @Profile
    public WebSocketClient get(String user, String project, SkillsService skillsService) {
        String key = buildKey(user, project)
        clientLock.readLock().lock()
        try {
            WebSocketClient webSocketClient = clientIdToWebSocketClient.get(key)
            if(!webSocketClient) {
                clientLock.readLock().unlock()
                clientLock.writeLock().lock()
                try {
                    //re-check just in case another thread changed the state before we could acquire the write lock
                    webSocketClient = clientIdToWebSocketClient.get(key)
                    if(!webSocketClient && clientCount < maxClients) {
                        webSocketClient = new WebSocketClient(userId: user, projId: project, serviceUrl: skillsService.getServiceUrl(), skillsService: skillsService)
                        webSocketClient = webSocketClient.init(pkiMode)
                        userToClientIdCache.add(user, key)
                        clientIdToWebSocketClient.put(key, webSocketClient)
                        clientCount++
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
                WebSocketClient client = clientIdToWebSocketClient.remove(it)
                client?.close()
                clientCount--
            }
            if(log.isDebugEnabled()) {
                int count = 0
                int distinctUsers = 0
                userToClientIdCache.each {
                    count+=it.value?.size()
                    distinctUsers++
                }
                log.info("There are currently [${count}] WebSocketClient instances for [${distinctUsers}] unique users")
            }
        } finally {
            clientLock.writeLock().unlock()
        }
    }

    private static String buildKey(String user, String projectId) {
        return "${user}_${projectId}"
    }


}

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

import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.http.client.support.BasicAuthenticationInterceptor
import org.springframework.lang.Nullable
import org.springframework.messaging.converter.StringMessageConverter
import org.springframework.messaging.simp.stomp.StompHeaders
import org.springframework.messaging.simp.stomp.StompSession
import org.springframework.messaging.simp.stomp.StompSessionHandler
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter
import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap
import org.springframework.web.client.RestTemplate
import org.springframework.web.socket.WebSocketHttpHeaders
import org.springframework.web.socket.client.standard.StandardWebSocketClient
import org.springframework.web.socket.messaging.WebSocketStompClient
import org.springframework.web.socket.sockjs.client.SockJsClient
import org.springframework.web.socket.sockjs.client.Transport
import org.springframework.web.socket.sockjs.client.WebSocketTransport
import skills.stress.services.SkillsService

import java.lang.reflect.Type

// this class is WIP
class WebSocketClient {

    String projId
    String userId
    String serviceUrl
    SkillsService skillsService

    private WebSocketStompClient stompClient

    WebSocketClient init(boolean pkiAuth){
        WebSocketClient client = new StandardWebSocketClient()
        List<Transport> transports = []
        transports.add(new WebSocketTransport(client))
        SockJsClient sockJsClient = new SockJsClient(transports)
        stompClient = new WebSocketStompClient(sockJsClient)
        stompClient.setMessageConverter(new StringMessageConverter())
        StompSessionHandler sessionHandler = new StompSessionHandlerAdapter() {
            @Override
            Type getPayloadType(StompHeaders headers) {
                return String
            }

            @Override
            void handleFrame(StompHeaders headers, @Nullable Object payload) {
                String result = (String) payload
                println "Got result: $result"
            }

            @Override
            void afterConnected(StompSession session, StompHeaders connectedHeaders) {
                session.subscribe("/${userId}/queue/${projId}-skill-updates", this)
            }
        }

        WebSocketHttpHeaders headers = new WebSocketHttpHeaders()
        if (!pkiAuth) {
            String clientSecret = skillsService.getClientSecret(projId)
            String serviceTokenUrl = "${serviceUrl}/oauth/token";

            RestTemplate oAuthRestTemplate = new RestTemplate();
            oAuthRestTemplate.setInterceptors(Arrays.asList(new BasicAuthenticationInterceptor(projId, clientSecret)));
            HttpHeaders httpHeaders = new HttpHeaders();
            httpHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("grant_type", "client_credentials");
            body.add("proxy_user", userId);

            ResponseEntity<String> responseEntity = oAuthRestTemplate.postForEntity(serviceTokenUrl, new HttpEntity<>(body, httpHeaders), String.class);

            String userToken = responseEntity.getBody();
            headers.add('Authorization', "Bearer ${userToken}")
        }

        stompClient.connect("ws://${serviceUrl}/skills-websocket", headers, sessionHandler)

        return this
    }

    public void close() {
        this.stompClient?.stop()
    }

}

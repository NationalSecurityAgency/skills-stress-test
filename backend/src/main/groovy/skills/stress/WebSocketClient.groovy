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

import groovy.util.logging.Slf4j
import org.apache.commons.lang3.Validate
import org.apache.tomcat.websocket.Constants
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

import javax.net.ssl.KeyManagerFactory
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import java.lang.reflect.Type
import java.security.KeyStore
import java.security.SecureRandom

@Slf4j
class WebSocketClient {

    String projId
    String userId
    String serviceUrl
    SkillsService skillsService

    private WebSocketStompClient stompClient
    private static volatile SSLContext cachedContext

    WebSocketClient init(boolean pkiAuth) {
        StandardWebSocketClient client = new StandardWebSocketClient()
        List<Transport> transports = []
        if (pkiAuth || serviceUrl.startsWith("https")) {
            def props = [(Constants.SSL_CONTEXT_PROPERTY) :  loadSslContext()]
            client.setUserProperties(props)
        }
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
                log.debug("Got result: $result")
            }

            @Override
            void afterConnected(StompSession session, StompHeaders connectedHeaders) {
                session.subscribe("/user/${userId}/queue/${projId}-skill-updates", this)
                log.trace("subscribed to /user/${userId}/queue/${projId}-skill-updates")
            }
        }

        WebSocketHttpHeaders headers = new WebSocketHttpHeaders()
        if (!pkiAuth) {
            String clientSecret = skillsService.getClientSecret(projId)
            String serviceTokenUrl = "${serviceUrl}/oauth/token";

            RestTemplate oAuthRestTemplate = new RestTemplate(RestTemplateHelper.getTrustAllRequestFactory());
            oAuthRestTemplate.setInterceptors(Arrays.asList(new BasicAuthenticationInterceptor(projId, clientSecret)));
            HttpHeaders httpHeaders = new HttpHeaders();
            httpHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("grant_type", "client_credentials");
            body.add("proxy_user", userId);

            ResponseEntity<Map> responseEntity = oAuthRestTemplate.postForEntity(serviceTokenUrl, new HttpEntity<>(body, httpHeaders), Map.class);

            String userToken = responseEntity.getBody().get("access_token");
            headers.add('Authorization', "Bearer ${userToken}")
        }

        String wsProtocol = "ws"
        if (serviceUrl.startsWith("https")) {
            wsProtocol = "wss"
        }
        String wsUrl = serviceUrl.replaceAll(/http(s)?:\/\//, '')
        stompClient.connect("${wsProtocol}://${wsUrl}/skills-websocket", headers, sessionHandler)

        return this
    }

    private SSLContext loadSslContext() {
        if (!cachedContext) {
            SSLContext sslContext = SSLContext.getInstance("TLS");
            KeyManagerFactory kmf = null
            TrustManager tm = new AcceptEverythingTrustManager()

            String configuredKeyStore = System.getProperty("javax.net.ssl.keyStore")
            if (configuredKeyStore) {
                String keyStoreType = getStoreType(System.getProperty("javax.net.ssl.keyStoreType"), configuredKeyStore)
                KeyStore keyStore = KeyStore.getInstance(keyStoreType)
                String keyPass = System.getProperty("javax.net.ssl.keyStorePassword")
                Validate.notNull(keyPass, "javax.net.ssl.keyStorePassword must be configured")
                keyStore.load(new FileInputStream(configuredKeyStore), keyPass.toCharArray())

                kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
                kmf.init(keyStore, keyPass.toCharArray())
            }

            sslContext.init(kmf?.getKeyManagers(), [tm].toArray(new TrustManager[1]), new SecureRandom())
            cachedContext = sslContext
        }

        return cachedContext
    }

    private static String getStoreType(String storeTypePropertyValue, String storeFile) {
        if (!storeTypePropertyValue) {
            if (storeFile.endsWith(".p12")) {
                return "PKCS12"
            } else if (storeFile.endsWith(".jks")) {
                return "JKS"
            } else {
                throw new IllegalArgumentException("Unrecognized trust store type [${storeFile}]")
            }
        } else {
            return storeTypePropertyValue
        }
    }

    public void close() {
        this.stompClient?.stop()
    }

}

/*
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
import java.util.concurrent.CompletableFuture

@Slf4j
class WebSocketClient {

    String projId
    String userId
    String serviceUrl
    SkillsService skillsService

    private WebSocketStompClient stompClient
    StompSession stompSession
    private static volatile SSLContext cachedContext

    WebSocketClient init(boolean pkiAuth) {
        StandardWebSocketClient client = new StandardWebSocketClient()
        List<Transport> transports = []
        if (pkiAuth || serviceUrl.startsWith("https")) {
            client.setSslContext(loadSslContext())
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
                log.trace("Got result: $result")
            }

            @Override
            void afterConnected(StompSession session, StompHeaders connectedHeaders) {
                String destination = "/user/queue/${projId}-skill-updates";
                session.subscribe(destination, this)
                log.debug("subscribed to [${destination}]")
            }

            @Override
            void handleTransportError(StompSession session, Throwable exception) {
                log.warn("WebSocket transport error for user [{}] project [{}]: {}", userId, projId, exception.getMessage())
            }
        }

        WebSocketHttpHeaders headers = new WebSocketHttpHeaders()
        StompHeaders connectHeaders = new StompHeaders()
        if (!pkiAuth) {
            String clientSecret = skillsService.getClientSecret(projId)
            String serviceTokenUrl = "${serviceUrl}/oauth/token"

            RestTemplate oAuthRestTemplate = new RestTemplate(RestTemplateHelper.getTrustAllRequestFactory())
            oAuthRestTemplate.setInterceptors(Arrays.asList(new BasicAuthenticationInterceptor(projId, clientSecret)))
            HttpHeaders httpHeaders = new HttpHeaders()
            httpHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED)

            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>()
            body.add("grant_type", "client_credentials")
            body.add("proxy_user", userId)

            ResponseEntity<Map> responseEntity = oAuthRestTemplate.postForEntity(serviceTokenUrl, new HttpEntity<>(body, httpHeaders), Map.class)

            String userToken = responseEntity.getBody().get("access_token")
            connectHeaders.add('Authorization', "Bearer ${userToken}")
        }

        String wsProtocol = "ws"
        if (serviceUrl.startsWith("https")) {
            wsProtocol = "wss"
        }
        String wsHostAndPath = serviceUrl.replaceAll(/http(s)?:\/\//, '')
        String url = "${wsProtocol}://${wsHostAndPath}/skills-websocket"
        log.debug("connecting to [{}]", url)
        CompletableFuture<StompSession> future = stompClient.connectAsync(url, headers, connectHeaders, sessionHandler)
        wait { future.isDone() }
        if (!future.isDone()) {
            throw new IllegalStateException("Failed to create websocket connection to [${url}]. Please see the test's logs")
        }
        stompSession = future.get()
        log.info("websocket connection established to [{}]", url)
        return this
    }

    private SSLContext loadSslContext() {
        if (!cachedContext) {
            SSLContext sslContext = SSLContext.getInstance("TLS")
            KeyManagerFactory kmf = null
            TrustManager tm = new AcceptEverythingTrustManager()

            String configuredKeyStore = System.getProperty("javax.net.ssl.keyStore")
            if (configuredKeyStore) {
                String keyStoreType = getStoreType(System.getProperty("javax.net.ssl.keyStoreType"), configuredKeyStore)
                KeyStore keyStore = KeyStore.getInstance(keyStoreType)
                String keyPass = System.getProperty("javax.net.ssl.keyStorePassword")
                Validate.notNull(keyPass, "javax.net.ssl.keyStorePassword must be configured")
                keyStore.load(new FileInputStream(configuredKeyStore), keyPass.toCharArray())

                kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm())
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

    boolean isConnected() {
        return stompSession?.isConnected() ?: false
    }

    void close() {
        if (stompSession?.isConnected()) {
            stompSession.disconnect()
        }
        this.stompClient?.stop()
    }

    static boolean wait(Closure closure) {
        wait(60, closure)
    }

    static boolean wait(int secsToWait, Closure closure) {
        long start = System.currentTimeMillis()
        while(!closure.call() && (System.currentTimeMillis() - start) < (secsToWait * 1000) ) {
            Thread.sleep(250)
        }

        return closure.call()
    }
}

package skills.stress

import org.springframework.lang.Nullable
import org.springframework.messaging.converter.StringMessageConverter
import org.springframework.messaging.simp.stomp.StompHeaders
import org.springframework.messaging.simp.stomp.StompSession
import org.springframework.messaging.simp.stomp.StompSessionHandler
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter
import org.springframework.web.socket.client.standard.StandardWebSocketClient
import org.springframework.web.socket.messaging.WebSocketStompClient
import org.springframework.web.socket.sockjs.client.SockJsClient
import org.springframework.web.socket.sockjs.client.Transport
import org.springframework.web.socket.sockjs.client.WebSocketTransport

import java.lang.reflect.Type

// this class is WIP
class WebSocketClient {

    String projId
    String userId
    String serviceUrl

    WebSocketClient init(){
        WebSocketClient client = new StandardWebSocketClient()
        List<Transport> transports = []
        transports.add(new WebSocketTransport(client))
        SockJsClient sockJsClient = new SockJsClient(transports)
        WebSocketStompClient stompClient = new WebSocketStompClient(sockJsClient)
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

        stompClient.connect("ws://${serviceUrl}/skills-websocket", headers, sessionHandler)
    }
}

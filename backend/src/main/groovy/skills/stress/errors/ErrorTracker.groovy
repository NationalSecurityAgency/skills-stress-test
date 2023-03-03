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
package skills.stress.errors

import groovy.util.logging.Slf4j
import org.apache.commons.text.similarity.LevenshteinDistance
import org.springframework.stereotype.Service

import jakarta.annotation.PostConstruct
import jakarta.annotation.PreDestroy
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.SynchronousQueue
import java.util.concurrent.atomic.AtomicBoolean

@Service
@Slf4j
class ErrorTracker {

    int minDistance = 10
    int numErrorsToKeep = 20

    private final Queue<SeenError> errors = new ConcurrentLinkedQueue()
    private final LinkedBlockingQueue<SeenError> processIncomingErrors = new LinkedBlockingQueue<>();
    private final AtomicBoolean doRunErrorsProcessor = new AtomicBoolean(true)

    @PostConstruct
    void init(){
        Thread.start {
            while(doRunErrorsProcessor.get()){
                try {
                    SeenError incomingError = processIncomingErrors.take();
                    processError(incomingError)
                } catch (InterruptedException ex) {
                    log.error("Failed to process error", ex)
                }
            }
        }
    }

    @PreDestroy
    void shutdown(){
        doRunErrorsProcessor.set(false)
    }

    void track(String statusCode, String response, String body) {
        SeenError incomingError = new SeenError(httpStatus: statusCode, serverResponse: response, serverBody: body)
        processIncomingErrors.put(incomingError)
    }

    private void processError(SeenError incomingError) {
        SeenError foundMatch
        if (isUserNotFound(incomingError)) {
            foundMatch = errors.find { isUserNotFound(it) }
        } else {
            LevenshteinDistance levenshteinDistance = new LevenshteinDistance()
            foundMatch = errors.find { SeenError seenError ->
                int distance = levenshteinDistance.apply(incomingError.serverBody, seenError.serverBody)
                log.trace("Compared, distance is: [{}]", distance)
                if (distance < minDistance) {
                    return seenError
                }
            }
        }

        if (foundMatch) {
            foundMatch.addError(incomingError)
        } else {
            errors.add(incomingError)
        }

        if (errors.size() > numErrorsToKeep) {
            errors.remove()
        }
    }

    List<SeenError> getErrors() {
        return errors.toList()
    }

    void reset() {
        log.info("Reset Errors")
        errors.clear()
    }

    private boolean isUserNotFound(SeenError seenError){
        return seenError.serverBody.contains("\"errorCode\":\"UserNotFound\"")
    }
}

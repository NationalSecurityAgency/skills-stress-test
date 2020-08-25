package skills.stress.errors

import groovy.util.logging.Slf4j
import org.apache.commons.text.similarity.LevenshteinDistance
import org.apache.http.HttpStatus
import org.springframework.stereotype.Service

import java.util.concurrent.ConcurrentLinkedQueue

@Service
@Slf4j
class ErrorTracker {

    int minDistance = 10
    int numErrorsToKeep = 20

    private final Queue<SeenError> errors = new ConcurrentLinkedQueue()

    private boolean isUserNotFound(SeenError seenError){
        return seenError.serverBody.contains("\"errorCode\":\"UserNotFound\"")
    }

    synchronized void track(String statusCode, String response, String body) {
        SeenError incomingError = new SeenError(httpStatus: statusCode, serverResponse: response, serverBody: body)
        SeenError foundMatch
        if (isUserNotFound(incomingError)) {
            foundMatch = errors.find { isUserNotFound(it) }
        } else {
            LevenshteinDistance levenshteinDistance = new LevenshteinDistance()
            foundMatch = errors.find { SeenError seenError ->
                int distance = levenshteinDistance.apply(body, seenError.serverBody)
                log.info("Compared, distance is: [$distance]")
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
}

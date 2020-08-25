package skills.stress.errors

import org.apache.http.HttpStatus

import java.util.concurrent.ConcurrentLinkedQueue

class SeenError {
    String id = UUID.randomUUID().toString()
    Date lastSeen = new Date()
    String fullError
    String msg
    int numOccur = 1
    String httpStatus
    String serverBody
    String serverResponse
    Queue<SeenError> latestErrors = new ConcurrentLinkedQueue<SeenError>()

    synchronized void addError(SeenError seenError) {
        lastSeen = new Date()
        numOccur++
        latestErrors.add(seenError)
        if (latestErrors.size() > 10){
            latestErrors.remove()
        }
    }
}

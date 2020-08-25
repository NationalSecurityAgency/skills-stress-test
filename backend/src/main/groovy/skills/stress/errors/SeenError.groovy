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

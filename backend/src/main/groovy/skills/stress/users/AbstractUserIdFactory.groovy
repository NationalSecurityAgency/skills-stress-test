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
package skills.stress.users

import groovy.time.TimeCategory
import groovy.util.logging.Slf4j

@Slf4j
abstract class AbstractUserIdFactory implements UserIdFactory{
    protected final Random random = new Random()

    protected final ListChangeDecorator<UserWithExpiration> currentActiveUsers
    protected final List<String> userIds

    AbstractUserIdFactory(List<UserWithExpiration> currentActiveUsers, List<String> userIds) {
        this.currentActiveUsers = new ListChangeDecorator<UserWithExpiration>(currentActiveUsers)
        this.userIds = userIds
    }

    public void addActiveUserAddedListener(AddedListener<UserWithExpiration> addedListener) {
        this.currentActiveUsers.getAddedListeners()?.add(addedListener)
    }

    public void addActiveUserRemovedListener(RemovedListener<UserWithExpiration> removedListener){
        this.currentActiveUsers.getRemovedListeners()?.add(removedListener)
    }

    protected Date getRandomUserExpiration() {
        int expirationIMinutes = random.nextInt(25) + 5
        Date expirationDate
        use(TimeCategory) {
            expirationDate = new Date() + expirationIMinutes.minutes
        }
        expirationDate
    }

    private synchronized UserWithExpiration getNonActiveUserWithExpiration() {
        List<String> currentActiveIds = currentActiveUsers.collect {it.userId}
        List<String> availableToBeActive = userIds.findAll { !currentActiveIds.contains(it) }
        int ranNum = random.nextInt(availableToBeActive.size())
        String newId = availableToBeActive.get(ranNum)
        Date expirationDate = getRandomUserExpiration()
        return new UserWithExpiration(userId: newId, expireOn: expirationDate)
    }

    public synchronized String getUserId() {
        int ranNum = random.nextInt(currentActiveUsers.size())
        UserWithExpiration userWithExpiration = currentActiveUsers.get(ranNum)
        if (userWithExpiration.isExpired()) {
            UserWithExpiration newActiveUser = getNonActiveUserWithExpiration()
            currentActiveUsers.remove(userWithExpiration)
            currentActiveUsers.add(newActiveUser)
            userWithExpiration = newActiveUser
            log.info("New Active user [{}]", newActiveUser)
        }

        return userWithExpiration.userId
    }

    public int numUsers() {
        return userIds.size()
    }

    public String getUserByProjectIndex(Integer index) {
        // project index starts at 1
        assert index <= userIds.size()
        String userId = userIds.get(index - 1)
        return userId
    }
}

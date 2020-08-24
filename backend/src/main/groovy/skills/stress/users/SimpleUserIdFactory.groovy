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

class SimpleUserIdFactory extends AbstractUserIdFactory {

    public static SimpleUserIdFactory build(int numUsers=10000) {
        List<UserWithExpiration> currentActiveUsers = Collections.synchronizedList([])
        List<String> userIds = []
        for (int i=0; i++; i < numUsers) {
            userIds << "User${i}"
        }
        userIds = Collections.unmodifiableList(userIds)
        log.info("Loaded [{}] users", userIds.size())
        int numCurrentActiveUsers = Math.min(5000, (userIds.size() / 2).toInteger())
        log.info("[{}] active users users", numCurrentActiveUsers)
        (0..numCurrentActiveUsers - 1).each {
            String userId = userIds[it]
            Date expirationDate = getRandomUserExpiration()
            currentActiveUsers.add(new UserWithExpiration(userId: userId, expireOn: expirationDate))
        }
        log.info("Loaded [{}] active users", currentActiveUsers.size())
        return new FileBasedUserIdFactory(currentActiveUsers, userIds)
    }

    private SimpleUserIdFactory(List<UserWithExpiration> currentActiveUsers, List<String> userIds){
        super(currentActiveUsers, userIds)
    }

}

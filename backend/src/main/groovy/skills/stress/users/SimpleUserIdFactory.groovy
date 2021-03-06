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

import groovy.util.logging.Slf4j

@Slf4j
class SimpleUserIdFactory extends AbstractUserIdFactory {

    public static SimpleUserIdFactory build(int numUsers=10000) {
        List<UserWithExpiration> currentActiveUsers = Collections.synchronizedList([])
        List<String> uids = []
        log.info("generating [${numUsers}] users")
        for (int i=0; i < numUsers; i++) {
            uids.add("User${i}")
        }
        uids = Collections.unmodifiableList(uids)
        log.info("Loaded [{}] users", uids.size())
        int numCurrentActiveUsers = Math.min(5000, (uids.size() / 2).toInteger())
        log.info("[{}] active users ", numCurrentActiveUsers)
        for(int i=0; i< numCurrentActiveUsers;i++) {
            String userId = uids[i]
            Date expirationDate = getRandomUserExpiration()
            currentActiveUsers.add(new UserWithExpiration(userId: userId, expireOn: expirationDate))
        }
        log.info("Loaded [{}] active users", currentActiveUsers.size())
        return new SimpleUserIdFactory(currentActiveUsers, uids)
    }

    private SimpleUserIdFactory(List<UserWithExpiration> currentActiveUsers, List<String> userIds){
        super(currentActiveUsers, userIds)
    }

}

package skills.stress.users

import groovy.util.logging.Slf4j

@Slf4j
class FileBasedUserIdFactory implements UserIdFactory {

    final List<String> userIds

    FileBasedUserIdFactory(String path){
        File f = new File(path)
        if (!f.exists()) {
            throw new IllegalArgumentException("[{}] does not exist", path)
        }
        List<String> userIdsTmp = []
        f.eachLine {
            userIdsTmp.add(it)
        }
        userIds = Collections.unmodifiableList(userIdsTmp)
        log.info("Loaded [{}] users", userIds.size())
    }

    Random r = new Random()
    String getUserId(){
        int ranNum = r.nextInt(userIds.size())
        return userIds.get(ranNum)
    }

    String getUserByProjectIndex(Integer index){
        // project index starts at 1
        assert index <= userIds.size()
        String userId = userIds.get(index-1)
        return userId
    }


    int numUsers() {
        return userIds.size()
    }
}

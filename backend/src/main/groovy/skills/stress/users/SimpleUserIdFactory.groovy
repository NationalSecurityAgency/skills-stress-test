package skills.stress.users

class SimpleUserIdFactory implements UserIdFactory {

    int numUsers = 10000
    Random r = new Random()
    String getUserId(){
        int ranNum = r.nextInt(numUsers)
        return "User${ranNum}"
    }

    String getUserWithIndex(int index) {
        return "User${index}"
    }

    String getUserByProjectIndex(Integer index){
        return getUserWithIndex(index)
    }

    int numUsers() {
        return numUsers
    }
}

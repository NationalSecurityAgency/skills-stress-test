package skills.stress.users

interface UserIdFactory {
    String getUserId()
    String getUserByProjectIndex(Integer index)
    int numUsers()
}

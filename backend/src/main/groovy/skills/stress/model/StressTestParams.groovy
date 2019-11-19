package skills.stress.model

import groovy.transform.ToString

@ToString(includeNames = true)
class StressTestParams {

    int numProjects = 10
    int subjPerProject = 6
    int skillsPerSubject = 50
    int badgesPerProject = 10
    int hasDependenciesEveryNProjects = 5
    int numUsersPerApp = 100
    int numConcurrentThreads = 5
    boolean removeExistingTestProjects = false;
    String serviceUrl = "http://localhost:8080"
}

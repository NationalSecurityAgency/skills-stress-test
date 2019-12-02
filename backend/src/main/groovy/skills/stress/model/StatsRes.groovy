package skills.stress.model

class StatsRes {
    long totalEvents
    long totalExecTime
    long totalExecTimeLast1k

    double avgEventResponse
    double avgEventResponseLast1k

    List<ProfGroup> groupedExecTimes
    List<ProfGroup> groupedExecTimesLast1k
}

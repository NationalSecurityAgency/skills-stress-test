package skills.stress.model

class StatusRes {
    int numErrors = 0
    boolean hasFailures = false;
    boolean running = false;
    StatsRes reportSkillsRes
    StatsRes clientDisplayStats
}

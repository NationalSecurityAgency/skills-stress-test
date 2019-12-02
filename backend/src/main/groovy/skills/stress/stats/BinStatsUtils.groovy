package skills.stress.stats

class BinStatsUtils {

    static void addToBinnedExecTimes(Map<String, Long> map, long execTime) {
        assert map != null
        assert execTime >= 0
        if (execTime <= 50) {
            incrementBinnedCount(map, "a) time <= 50")
        } else if (execTime > 50 && execTime <= 100) {
            incrementBinnedCount(map, "b)  50 < time <= 100")
        } else if (execTime > 100 && execTime <= 150) {
            incrementBinnedCount(map, "c) 100 < time <= 150")
        } else if (execTime > 150 && execTime <= 200) {
            incrementBinnedCount(map, "d) 150 < time <= 200")
        } else if (execTime > 200 && execTime <= 300) {
            incrementBinnedCount(map, "e) 200 < time <= 300")
        } else if (execTime > 300 && execTime <= 1000) {
            incrementBinnedCount(map, "f) 300 < time <= 1s")
        } else {
            incrementBinnedCount(map, "g) time > 1s")
        }
    }

    private static void incrementBinnedCount(Map<String, Long> map, String name) {
        Long count = map[name]
        if (count == null) {
            map[name] = 1l
        } else {
            map[name] = count + 1l
        }
    }
}

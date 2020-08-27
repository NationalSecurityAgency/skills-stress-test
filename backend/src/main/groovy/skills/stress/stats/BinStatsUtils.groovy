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
package skills.stress.stats

class BinStatsUtils {

    static void addToBinnedExecTimes(Map<String, Long> map, long execTime) {
        assert map != null
        assert execTime >= 0
        if (execTime <= 50) {
            incrementBinnedCount(map, bins[0])
        } else if (execTime > 50 && execTime <= 100) {
            incrementBinnedCount(map, bins[1])
        } else if (execTime > 100 && execTime <= 150) {
            incrementBinnedCount(map, bins[2])
        } else if (execTime > 150 && execTime <= 200) {
            incrementBinnedCount(map, bins[3])
        } else if (execTime > 200 && execTime <= 300) {
            incrementBinnedCount(map, bins[4])
        } else if (execTime > 300 && execTime <= 1000) {
            incrementBinnedCount(map, bins[5])
        } else {
            incrementBinnedCount(map, bins[6])
        }
    }

    private static List<String> bins = [
            "a) t <= 50" ,
            "b) 50 < t <= 100",
            "c) 100 < t <= 150",
            "d) 150 < t <= 200",
            "e) 200 < t <= 300",
            "f) 300 < t <= 1s",
            "g) t > 1s"
    ]
    static Map<String, Long> constructEmptyBinnedMap() {
        Map<String, Long> withVals = [:]
        bins.each {
            withVals[it] = 0
        }
        return Collections.synchronizedMap(new LinkedHashMap<String, Long>(withVals))
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

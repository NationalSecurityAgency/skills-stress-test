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

import groovy.transform.WithReadLock
import groovy.transform.WithWriteLock
import groovy.util.logging.Slf4j
import skills.stress.model.ProfGroup
import skills.stress.model.StatsRes

import java.util.concurrent.atomic.AtomicLong

@Slf4j
class StatsHelper {

    AtomicLong totalEvents = new AtomicLong(0)
    AtomicLong totalTime = new AtomicLong(0)

    AtomicLong last1kTime = new AtomicLong(0)

    ThreadLocal<Long> startTime = new ThreadLocal<>()

    Map<String, Long> binExecTimes = Collections.synchronizedMap([:])
    Map<String, Long> binExecTimesLast1k = Collections.synchronizedMap([:])

    double avgEventResponse
    double avgEventResponseLast1k

    @WithWriteLock
    void startEvent() {
        startTime.set(System.currentTimeMillis())
    }

    @WithWriteLock
    void endEvent() {
        long execTime = System.currentTimeMillis() - startTime.get()
        BinStatsUtils.addToBinnedExecTimes(binExecTimes, execTime)
        BinStatsUtils.addToBinnedExecTimes(binExecTimesLast1k, execTime)
        int totalEvents = totalEvents.incrementAndGet()
        long totalExecTime = totalTime.addAndGet(execTime)
        long last1kExecTime = last1kTime.addAndGet(execTime)

        avgEventResponse = (totalExecTime / totalEvents).trunc(2)
        avgEventResponseLast1k = (last1kExecTime / 1000).trunc(2)

        if (totalEvents % 1000 == 0) {
//            log.info(buildMessage(totalEvents, totalExecTime, last1kExecTime))
            last1kTime.set(0)
            binExecTimesLast1k.clear()
        }
    }


//    private String buildMessage(totalEvents, totalExecTime, last1kExecTime) {
//        List<String> msgs = [
//                "\n--------------------------------",
//                "Total Events: [${totalEvents}]",
//                "Avg. Event Response: [${(totalEvents / (totalExecTime / 1000)).trunc(2)}] ms",
//                "Avg. Event Response (last 1k events): [${(1000 / (last1kExecTime / 1000)).trunc(2)}] ms",
//
//        ]
//
//        msgs.add("Overall breakdown:")
//        msgs.addAll(binExecTimes.collect { "  ${it.key}: ${it.value}" }.sort())
//        msgs.add("Last 1k breakdown:")
//        msgs.addAll(binExecTimesLast1k.collect { "  ${it.key}: ${it.value}" }.sort())
//
//        return msgs.join("\n")
//    }


    @WithReadLock
    StatsRes buildStatus() {
        return new StatsRes(
                totalEvents: totalEvents.get(),
                totalExecTime: (totalTime.get() / 1000).toInteger() ,
                totalExecTimeLast1k: last1kTime.get(),
                groupedExecTimes: binExecTimes?.collect {
                    new ProfGroup(groupName: it.key, numberOfEvents: it.value)
                }?.sort({ it.groupName }),
                groupedExecTimesLast1k: binExecTimesLast1k?.collect {
                    new ProfGroup(groupName: it.key, numberOfEvents: it.value)
                }?.sort({ it.groupName }),
                avgEventResponse: avgEventResponse,
                avgEventResponseLast1k: avgEventResponseLast1k,
        )
    }
}

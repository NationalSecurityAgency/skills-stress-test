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

import groovy.json.JsonSlurper
import groovy.transform.WithReadLock
import groovy.transform.WithWriteLock
import groovy.util.logging.Slf4j
import skills.stress.model.LabelsAndSeries
import skills.stress.model.ProfGroup
import skills.stress.model.StatsRes

import java.util.concurrent.atomic.AtomicLong

@Slf4j
class StatsHelper {

    AtomicLong totalEvents = new AtomicLong(0)
    AtomicLong totalTime = new AtomicLong(0)

    AtomicLong last1kTime = new AtomicLong(0)
    AtomicLong last1kEventsCounter = new AtomicLong(0)

    JsonSlurper jsonSlurper = new JsonSlurper()
    ThreadLocal<Long> startTime = new ThreadLocal<>()

    Map<String, Long> binExecTimes = BinStatsUtils.constructEmptyBinnedMap()
    Map<String, Long> binExecTimesLast1k = BinStatsUtils.constructEmptyBinnedMap()
    Map<Long, Long> historyAvgTimePer1k = Collections.synchronizedMap([:])

    Map<String, Long> binExplanations = Collections.synchronizedMap(ExplanationUtils.constructEmptyExplanationMap())
    Map<String, Long> binExplanationsLast1k = Collections.synchronizedMap(ExplanationUtils.constructEmptyExplanationMap())
    double avgEventResponse
    double avgEventResponseLast1k

    @WithWriteLock()
    void startEvent() {
        startTime.set(System.currentTimeMillis())
    }

    @WithWriteLock
    void endEvent(def res = null) {
        long execTime = System.currentTimeMillis() - startTime.get()
        BinStatsUtils.addToBinnedExecTimes(binExecTimes, execTime)
        BinStatsUtils.addToBinnedExecTimes(binExecTimesLast1k, execTime)
        int totalEvents = totalEvents.incrementAndGet()
        long totalExecTime = totalTime.addAndGet(execTime)

        long last1kCounter = last1kEventsCounter.incrementAndGet()
        long last1kExecTime = last1kTime.addAndGet(execTime)

        avgEventResponse = (totalExecTime / totalEvents).trunc(2)
        avgEventResponseLast1k = (last1kExecTime / last1kCounter).trunc(2)

        if (totalEvents % 1000 == 0) {
//            log.info(buildMessage(totalEvents, totalExecTime, last1kExecTime))
            historyAvgTimePer1k.put(System.currentTimeMillis(), avgEventResponseLast1k)
            last1kTime.set(0)
            last1kEventsCounter.set(0)
            binExecTimesLast1k.clear()
            binExecTimesLast1k.putAll(BinStatsUtils.constructEmptyBinnedMap())
            binExplanationsLast1k.clear()
            binExplanationsLast1k.putAll(ExplanationUtils.constructEmptyExplanationMap())
        }

        if (res) {
            def parsed = jsonSlurper.parseText(res)
            ExplanationUtils.documentResult(binExplanations, parsed)
            ExplanationUtils.documentResult(binExplanationsLast1k, parsed)
        }
    }


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
                historyOfAvgLatencyPer1k: historyAvgTimePer1k.collect({ [it.key, it.value]}),
                explanationCounts: ExplanationUtils.buildLabelAndSeries(binExplanations),
                explanationCountsLast1k: ExplanationUtils.buildLabelAndSeries(binExplanationsLast1k)
        )
    }
}


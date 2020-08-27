/*
Copyright 2020 SkillTree

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    https://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/
<template>
  <div v-if="reportSkillsRes">
    <h3 class="text-left border-bottom text-info font-weight-bold text-uppercase">
      <div class="row">
        <div class="col">
          {{ title }}
        </div>
        <div class="col-md text-center text-md-right">
          <h5 class="text-muted text-uppercase text-right">
            Started: <span class="text-dark">{{ startTimestamp | date }}</span>
          </h5>
        </div>
      </div>
    </h3>

    <div class="row mb-4">
      <div class="col">
        <single-stat-card title="# Events" :value="reportSkillsRes.totalEvents" icon="calculator" class="text-primary border-left-primary"/>
      </div>
      <div class="col">
        <single-stat-card title="Overall Latency Avg." :value="reportSkillsRes.avgEventResponse" icon="calendar2-check" class="text-success border-left-success"/>
      </div>
      <div class="col">
        <single-stat-card title="Last 1k Latency Avg." :value="reportSkillsRes.avgEventResponseLast1k" icon="alarm"  class="text-warning border-left-warning"/>
      </div>
    </div>

    <div>
      <chart-avg-latency-timechart :time-series="reportSkillsRes.historyOfAvgLatencyPer1k"/>
    </div>

    <div class="row mt-3">
      <div class="col-6">
        <group-by-exec-time-chart title="Overall Latency Breakdown"  :grouped-exec-times="reportSkillsRes.groupedExecTimes"/>
      </div>
      <div class="col-6">
        <group-by-exec-time-chart title="Last 1k Latency Breakdown" :grouped-exec-times="reportSkillsRes.groupedExecTimesLast1k"/>
      </div>
    </div>
  </div>
</template>

<script>
import NumberFilter from "@/filters/NumberFilter";
import ChartAvgLatencyTimechart from "@/components/charts/ChartAvgLatencyTimechart";
import GroupByExecTimeChart from "@/components/charts/GroupByExecTimeChart";
import SingleStatCard from "@/components/metrics/SingleStatCard";

export default {
  name: "StressTestsMetrics",
  components: {SingleStatCard, GroupByExecTimeChart, ChartAvgLatencyTimechart},
  props: ['reportSkillsRes', 'startTimestamp', 'title'],
  methods: {
    formatNum(numVal) {
      return NumberFilter(numVal);
    },
  },
}
</script>

<style scoped>
.border-left-primary {
  border-left: .25rem solid #007bff !important;
}
.border-left-success {
  border-left: .25rem solid #28a745 !important;
}
.border-left-warning {
  border-left: .25rem solid #ffc107 !important;
}
</style>

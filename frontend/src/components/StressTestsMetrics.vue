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
<script setup>
import { defineProps } from 'vue';
import NumberFilter from "@/filters/NumberFilter.js";
import ChartAvgLatencyTimechart from "@/components/charts/ChartAvgLatencyTimechart.vue";
import GroupByExecTimeChart from "@/components/charts/GroupByExecTimeChart.vue";
import SingleStatCard from "@/components/metrics/SingleStatCard.vue";
import ResultExaplanationChart from "@/components/charts/ResultExaplanationChart.vue";

defineProps(['reportSkillsRes', 'startTimestamp', 'title', 'disableResCharts']);

const formatNum = (numVal) => {
  return NumberFilter(numVal);
};
</script>

<template>
  <div v-if="reportSkillsRes">
    <h3 class="text-center text-md-left border-bottom text-info font-weight-bold text-uppercase">
      <div class="flex">
        <div class="flex flex-1">
          {{ title }}
        </div>
        <div class="flex flex-1 text-center text-md-right">
          <h5 class="text-muted text-uppercase text-md-right">
            Started: <span class="text-dark">{{ startTimestamp }}</span>
          </h5>
        </div>
      </div>
    </h3>

    <div class="row mb-4">
      <div class="col-md mt-2">
        <SingleStatCard title="# Events" :value="reportSkillsRes.totalEvents" icon="calculator" class="text-primary border-left-primary"/>
      </div>
      <div class="col-md mt-2">
        <SingleStatCard title="Overall Latency Avg." :value="reportSkillsRes.avgEventResponse" icon="calendar2-check" class="text-success border-left-success"/>
      </div>
      <div class="col mt-2">
        <SingleStatCard title="Last 1k Latency Avg." :value="reportSkillsRes.avgEventResponseLast1k" icon="alarm"  class="text-warning border-left-warning"/>
      </div>
    </div>

    <div>
      <ChartAvgLatencyTimechart :time-series="reportSkillsRes.historyOfAvgLatencyPer1k"/>
    </div>

    <div class="row mt-3">
      <div class="col-md-6 mt-2">
        <GroupByExecTimeChart title="Overall Latency Breakdown"  :grouped-exec-times="reportSkillsRes.groupedExecTimes"/>
      </div>
      <div v-if="!disableResCharts" class="col-md-6 mt-2">
        <ResultExaplanationChart title="Overall Result Explanations" :explanation-counts="reportSkillsRes.explanationCounts" class="h-100"/>
      </div>
      <div class="col-md-6 mt-2">
        <GroupByExecTimeChart title="Last 1k Latency Breakdown" :grouped-exec-times="reportSkillsRes.groupedExecTimesLast1k"/>
      </div>
      <div v-if="!disableResCharts" class="col-md-6 mt-2">
        <ResultExaplanationChart title="Last 1k Result Explanations" :explanation-counts="reportSkillsRes.explanationCountsLast1k" class="h-100"/>
      </div>
    </div>
  </div>
</template>

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

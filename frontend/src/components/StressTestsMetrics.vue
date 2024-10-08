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
import NumberFilter from "@/filters/NumberFilter.js";
import DateFormatter from '@/filters/DateFilter.js';
import ChartAvgLatencyTimechart from "@/components/charts/ChartAvgLatencyTimechart.vue";
import GroupByExecTimeChart from "@/components/charts/GroupByExecTimeChart.vue";
import SingleStatCard from "@/components/metrics/SingleStatCard.vue";
import ResultExaplanationChart from "@/components/charts/ResultExaplanationChart.vue";

defineProps(['reportSkillsRes', 'startTimestamp', 'title', 'disableResCharts']);

const formatNum = (numVal) => {
  return NumberFilter.format(numVal);
};

const formatDate = (dateVal) => {
  return DateFormatter.format(dateVal);
}
</script>

<template>
  <div v-if="reportSkillsRes" class="mt-8">
    <div class="flex border-bottom-1 text-surface uppercase">
        <div class="flex-1 font-bold text-3xl">
          {{ title }}
        </div>
        <div class="text-xl">
          Started: <span>{{ formatDate(startTimestamp) }}</span>
        </div>
    </div>

    <div class="flex gap-2 mt-4 mb-4">
      <div class="flex-1">
        <SingleStatCard title="# Events" :value="reportSkillsRes.totalEvents" icon="pi-calculator" class="text-primary border-left-primary"/>
      </div>
      <div class="flex-1">
        <SingleStatCard title="Overall Latency Avg." :value="reportSkillsRes.avgEventResponse" icon="pi-calendar" class="text-success border-left-success"/>
      </div>
      <div class="flex-1">
        <SingleStatCard title="Last 1k Latency Avg." :value="reportSkillsRes.avgEventResponseLast1k" icon="pi-bell"  class="text-warning border-left-warning"/>
      </div>
    </div>

    <div>
      <ChartAvgLatencyTimechart :time-series="reportSkillsRes.historyOfAvgLatencyPer1k"/>
    </div>

    <div class="flex gap-2 mt-3">
      <GroupByExecTimeChart title="Overall Latency Breakdown"  :grouped-exec-times="reportSkillsRes.groupedExecTimes" class="flex-1"/>
      <ResultExaplanationChart v-if="!disableResCharts" title="Overall Result Explanations" :explanation-counts="reportSkillsRes.explanationCounts" class="flex-1"/>
    </div>
    <div class="flex gap-2 mt-3">
      <GroupByExecTimeChart title="Last 1k Latency Breakdown" :grouped-exec-times="reportSkillsRes.groupedExecTimesLast1k" class="flex-1"/>
      <ResultExaplanationChart v-if="!disableResCharts" title="Last 1k Result Explanations" :explanation-counts="reportSkillsRes.explanationCountsLast1k" class="flex-1"/>
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

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
import { ref, watch } from 'vue';
import dateFormatter from "@/filters/DateFilter.js";
import Message from 'primevue/message';

const props = defineProps(['timeSeries']);

const numItems = ref(!props.timeSeries ? 0 : props.timeSeries.length);
const options = {
  chart: {
    type: 'line',
    stacked: false,
    height: 350,
    zoom: {
      type: 'x',
      enabled: true,
      autoScaleYaxis: true
    },
    toolbar: {
      autoSelected: 'zoom'
    }
  },
  dataLabels: {
    enabled: false,
  },
  stroke: {
    width: 7,
    curve: 'smooth'
  },
  markers: {
    size: 6,
    colors: ["#FFA41B"],
    strokeColors: "#fff",
    strokeWidth: 2,
    hover: {
      size: 7,
    }
  },
  fill: {
    type: 'gradient',
    gradient: {
      shade: 'dark',
      gradientToColors: ['#FDD835'],
      shadeIntensity: 1,
      type: 'horizontal',
      opacityFrom: 1,
      opacityTo: 1,
      stops: [0, 100, 100, 100]
    },
  },
  yaxis: {
    title: {
      text: 'Latency (MS)'
    },
  },
  xaxis: {
    type: 'datetime',
    labels: {
      formatter: (value, timestamp) => {
        return dateFormatter.format(timestamp);
      }
    }
  },
};

const series = ref([{
  name: 'Points',
  data: props.timeSeries,
}]);

watch(() => props.timeSeries, () => {
  if(props.timeSeries.length > numItems.value) {
    updateSeriesLine()
    numItems.value = props.timeSeries.length;
  }
})

const avgLatencyChart = ref();

const updateSeriesLine = () => {
  avgLatencyChart.value.updateSeries([{
    data: props.timeSeries
  }], false, true)
}
</script>

<template>
  <Card>
    <template #title class="text-left">
      <div class="border-bottom-1 p-3 surface-border surface-100 flex gap-4">
        <span class="uppercase text-primary flex-1 text-left">Average Latency Per 1k Records</span>
      </div>
    </template>
    <template #content>
      <div>
        <apexchart id="avgLatencyChart" ref="avgLatencyChart" height="350" type="line" :options="options" :series="series">
        </apexchart>
        <div v-if="!timeSeries || (timeSeries && timeSeries.length == 0)" class="flex absolute left-0 right-0 justify-content-center"
             style="z-index: 1000 !important; margin-top: -220px" role="alert">
            <Message>Not enough events processed</Message>
        </div>
      </div>

    </template>
  </Card>
</template>

<style scoped>
</style>

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
import { ref } from 'vue';
import dateFormatter from "@/filters/DateFilter.js";

const props = defineProps(['timeSeries']);

const loading = ref(true);
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


  // watch: {
  //   timeSeries() {
  //     if (this.timeSeries.length > this.numItems) {
  //       this.updateSeriesLine();
  //       this.numItems = this.timeSeries.length;
  //     }
  //   },
  // },
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
      <h5>Average Latency Per 1k Records</h5>
    </template>
    <template #content>
      <div v-if="!timeSeries || (timeSeries && timeSeries.length == 0)" class="msg-overlay row justify-content-md-center">
        <div class="col-7">
          <div class="alert alert-info">Not enough events processed</div>
        </div>
      </div>
      <apexchart id="avgLatencyChart" ref="avgLatencyChart" height="350" type="line"
                 :options="options" :series="series">
      </apexchart>
    </template>
  </Card>
</template>

<style scoped>
.msg-overlay {
  position: absolute;
  z-index: 1000;
  text-align: center;
  width: 100%;
  top: 30%;
  left: 2rem;
}
</style>

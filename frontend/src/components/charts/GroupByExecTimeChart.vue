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
import { ref, onMounted } from 'vue';
import numberFormatter from '@/filters/NumberFilter.js';

const props = defineProps(['title', 'groupedExecTimes'])

onMounted(() => {
  updateSeries()
})

const getXAxis = () => {
  const xAxisCategories = [];
  props.groupedExecTimes.forEach((e) => {
    xAxisCategories.push(e.groupName);
  })
  return xAxisCategories;
}

const series = ref([]);
const options = {
  chart: {
    type: 'bar',
        height: 350
  },
  legend: {
    show: false,
  },
  plotOptions: {
    bar: {
      horizontal: false,
      columnWidth: '55%',
      endingShape: 'rounded',
      distributed: true
    },
  },
  dataLabels: {
    enabled: false
  },
  stroke: {
    show: true,
        width: 2,
        colors: ['transparent']
  },
  xaxis: {
    categories: getXAxis(),
  },
  yaxis: {
    title: {
      text: 'Number of Events'
    }
  },
  fill: {
    opacity: 1
  },
  tooltip: {
    y: {
      formatter: (val) => {
        return numberFormatter.format(val)
      }
    }
  }
}

const updateSeries = () => {
  const seriesData = [];
  props.groupedExecTimes.forEach((e) => {
    seriesData.push(e.numberOfEvents);
  })
  series.value = [{
    name: 'Exec Time',
    data: seriesData
  }];
}

  // watch: {
  //   groupedExecTimes() {
  //     this.updateSeries();
  //   },
  // },

</script>

<template>
  <Card>
    <template #title class="text-left">
      <h5>{{ title }}</h5>
    </template>
    <template #content>
      <apexchart type="bar" height="400" :options="options" :series="series"></apexchart>
    </template>
  </Card>
</template>

<style scoped>

</style>

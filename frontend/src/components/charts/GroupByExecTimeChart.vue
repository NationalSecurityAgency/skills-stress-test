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
  <div class="card">
    <div class="card-header text-left">
      <h5>{{ title }}</h5>
    </div>
    <div class="card-body">
      <apexchart type="bar" height="400" :options="options" :series="series"></apexchart>
    </div>
  </div>
</template>

<script>
import numberFormatter from '@/filters/NumberFilter';

export default {
  name: "GroupByExecTimeChart",
  props: ['title', 'groupedExecTimes'],
  mounted() {
    this.updateSeries();
  },
  methods: {
    updateSeries() {
      const seriesData = [];
      this.groupedExecTimes.forEach((e) => {
        seriesData.push(e.numberOfEvents);
      })
      this.series = [{
        name: 'Exec Time',
        data: seriesData
      }];
    },
    getXAxis() {
      const xAxisCategories = [];
      this.groupedExecTimes.forEach((e) => {
        xAxisCategories.push(e.groupName);
      })
      return xAxisCategories;
    },
  },
  watch: {
    groupedExecTimes() {
      this.updateSeries();
    },
  },
  data() {
    return {
      loading: true,
      series: [],
      options: {
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
          categories: this.getXAxis(),
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
            formatter: function (val) {
              return numberFormatter(val)
            }
          }
        }
      },
    };
  },
}
</script>

<style scoped>

</style>

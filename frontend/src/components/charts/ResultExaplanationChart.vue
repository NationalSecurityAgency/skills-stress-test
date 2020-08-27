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
import numberFormatter from "@/filters/NumberFilter";
export default {
  name: "ResultExaplanationChart",
  props: ['title', 'explanationCounts'],
  watch: {
    explanationCounts() {
      this.series = [{
        data: this.explanationCounts.series,
      }];
    },
  },
  data() {
    return {
      loading: true,
      series: [{
        data: this.explanationCounts.series,
      }],
      options: {
        chart: {
          type: 'bar',
          height: 400
        },
        plotOptions: {
          bar: {
            barHeight: '80%',
            distributed: true,
            horizontal: true,
            dataLabels: {
              position: 'bottom'
            },
          }
        },
        colors: ['#33b2df',  '#13d8aa', '#f9a3a4', '#90ee7e',
          '#f48024', '#69d2e7'
        ],
        dataLabels: {
          enabled: true,
          textAnchor: 'start',
          style: {
            colors: ['#17a2b8'],
            fontSize: '14px',
            fontFamily: 'Helvetica, Arial, sans-serif',
            fontWeight: 'bold',
          },
          formatter: function (val, opt) {
            return opt.w.globals.labels[opt.dataPointIndex] + ":  " + numberFormatter(val)
          },
          offsetX: 10,
          dropShadow: {
            enabled: false
          },
          background: {
            enabled: true,
            foreColor: '#ffffff',
            padding: 10,
            borderRadius: 2,
            borderWidth: 1,
            borderColor: '#686565',
            opacity: 1,
            dropShadow: {
              enabled: false,
            }
          },
        },
        legend: {
          show: false,
        },
        stroke: {
          width: 1,
          colors: ['#fff']
        },
        xaxis: {
          categories: this.explanationCounts.labels,
        },
        yaxis: {
          labels: {
            show: false
          }
        },
        tooltip: {
          theme: 'dark',
          x: {
            show: false
          },
          y: {
            title: {
              formatter: function () {
                return ''
              }
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

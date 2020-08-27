<template>
  <div class="card">
    <div class="card-header text-left">
      <h5>Average Latency Per 1k Records</h5>
    </div>
    <div class="card-body">
      <div v-if="!timeSeries || (timeSeries && timeSeries.length == 0)" class="msg-overlay row justify-content-md-center">
        <div class="col-7">
          <div class="alert alert-info">Not enough events processed</div>
        </div>
      </div>
      <apexchart id="avgLatencyChart" ref="avgLatencyChart" height="350" type="line"
                 :options="options" :series="series">
      </apexchart>
    </div>
  </div>
</template>

<script>
import dateFormatter from "@/filters/DateFilter";

export default {
  name: "ChartAvgLatencyTimechart",
  props: ['timeSeries'],
  data() {
    return {
      loading: true,
      numItems: !this.timeSeries ? 0 : this.timeSeries.length,
      options: {
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
            formatter: function (value, timestamp) {
              return dateFormatter(timestamp);
            }
          }
        },
      },
      series: [{
        name: 'Points',
        data: this.timeSeries,
      }]
    };
  },
  watch: {
    timeSeries() {
      if (this.timeSeries.length > this.numItems) {
        this.updateSeriesLine();
        this.numItems = this.timeSeries.length;
      }
    },
  },
  methods: {
    updateSeriesLine() {
      this.$refs.avgLatencyChart.updateSeries([{
        data: this.timeSeries
      }], false, true)
    }
  },
}
</script>

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

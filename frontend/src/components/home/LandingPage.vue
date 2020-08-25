<template>
  <div class="container">
    <start-stress-test @start-test="startTest" @stop-test="stopTest" :running="running"/>
    <div v-if="failedToStart" class="alert alert-danger mt-2">Failed to start! See Logs!</div>
    <stress-test-status class="mt-2" :status="status" :running="running"/>
  </div>
</template>

<script>
import StressTestStatus from "@/components/StressTestStatus";
import StartStressTest from "@/components/StartStressTest";
import StressTestsService from "@/services/StressTestsService";

export default {
  name: 'LandingPage',
  components: {
    StartStressTest,
    StressTestStatus,
  },
  data() {
    return {
      running: false,
      failedToStart: false,
      status: {},
      timer: undefined,
    };
  },
  mounted() {
    this.loadStatus();
  },
  methods: {
    loadStatus() {
      StressTestsService.getStatus()
          .then((status) => {
            this.status = status
            if (status) {
              this.running = status.running
            }
          });
    },
    startTest(config) {
      this.failedToStart = false;
      StressTestsService.startTest(config)
          .then(() => {
            this.running = true;
          })
          .catch(() => {
            this.failedToStart = true;
          });
    },
    stopTest() {
      StressTestsService.stopTest()
          .then(() => {
            this.running = false;
          });
    }
  },
  watch: {
    running(val) {
      if (val === true) {
        this.timer = setInterval(this.loadStatus, 2000);
      } else {
        clearInterval(this.timer);
      }
    },
  },
  beforeDestroy() {
    if (this.timer) {
      clearInterval(this.timer);
    }
  }
}
</script>

<style scoped>

</style>

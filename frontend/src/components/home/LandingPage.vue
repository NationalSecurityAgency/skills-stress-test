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
import { ref, onMounted, onBeforeUnmount, watch } from 'vue';
import StressTestStatus from "@/components/StressTestStatus.vue";
import StartStressTest from "@/components/StartStressTest.vue";
import StressTestsService from "@/services/StressTestsService.js";

const running = ref(false);
const failedToStart = ref(false);
const status = ref({});
const timer = ref(undefined);

onMounted(() => {loadStatus()})

onBeforeUnmount( () => {
  if (timer.value) {
    clearInterval(timer.value);
  }
});

const loadStatus = () => {
  StressTestsService.getStatus().then((newStatus) => {
    status.value = newStatus
    if (newStatus) {
      running.value = newStatus.running
    }
  });
};

const startTest = (config) => {
  failedToStart.value = false;
  StressTestsService.startTest(config).then(() => {
    running.value = true;
  }).catch(() => {
    failedToStart.value = true;
  });
};

const stopTest = () => {
  StressTestsService.stopTest().then(() => {
    running.value = false;
  });
};

watch(running, (value) => {
  if (value === true ) {
    timer.value = setInterval(loadStatus, 2000);
  } else {
    clearInterval(timer.value);
  }
})
</script>

<template>
  <div class="container">
    <start-stress-test @start-test="startTest" @stop-test="stopTest" :running="running"/>
    <div v-if="failedToStart" class="alert alert-danger mt-2">Failed to start! See Logs!</div>
    <stress-test-status class="mt-2" :status="status" :running="running"/>
  </div>
</template>

<style scoped>

</style>

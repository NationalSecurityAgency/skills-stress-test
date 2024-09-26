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
import StressTestsService from "@/services/StressTestsService.js";
import Message from 'primevue/message';
import Badge from 'primevue/badge';
import DateFormatter from '@/filters/DateFilter.js';
import TimePassedFilter from '@/filters/TimePassedFilter.js';

const loading = ref(true);
const errors = ref([]);

onMounted(() => {
  loadErrors();
})

const loadErrors = () => {
  loading.value = true;
  StressTestsService.getErrors().then((newErrors) => {
    errors.value = newErrors.map((er) => Object.assign({collapsed: true}, er))
    loading.value = false;
  });
};
const formatJson = (stringJson) => {
  return JSON.stringify(JSON.parse(stringJson), null, 2)
};
const collapseFlip = (error) => {
  error.collapsed = !error.collapsed;
  errors.value = errors.value.map((er) => er);
}
</script>

<template>
  <div class="text-left">
    <h2 class="pb-3 mb-5 border-bottom-1">
      <i class="pi pi-exclamation-circle"></i>
      <span class="uppercase mx-1">Errors</span> <span class="font-light">(last 20)</span>
      <Button @click="loadErrors" class="float-right mb-3" variant="outline-primary">
        <i class="pi pi-refresh"></i>
      </Button>
    </h2>
    <div v-if="loading" class="text-center">
<!--      <b-spinner variant="info" type="grow" label="Spinning" style="width: 3rem; height: 3rem;"></b-spinner>-->
      <div class="uppercase mt-2">
        Loading...
      </div>
    </div>
    <div v-if="!errors || errors.length === 0" class="flex justify-content-md-center row">
      <div class="flex-1 text-center">
        <h4>
          <Message severity="success">
            <i class="pi pi-trophy"></i>
            Congrats!! There are no errors!
          </Message>
        </h4>
      </div>
    </div>
    <div v-if="!loading">
      <div v-for="error in errors" :key="error.id" class="mt-3 border-bottom-1">
        <div>
          <div>
            <Badge size="xlarge">{{ error.httpStatus}} <Badge severity="danger" class="ml-4">{{ error.numOccur }}</Badge></Badge>
          </div>
          <div class="flex gap-2 mt-2">
            <span class="flex">Last Seen:</span>
            <span class="flex">{{ DateFormatter.format(error.lastSeen) }}</span> <span class="font-light ml-1">({{ TimePassedFilter.format(error.lastSeen) }})</span>
            <a href="#" class="" @click="collapseFlip(error)">
              View Error History <i class="pi pi-arrow-down"></i>
            </a>
          </div>
        </div>
        <div class="flex-1">
          <pre class="bg-light p-3">{{ error.serverBody }}</pre>
        </div>
        <div v-if="!error.collapsed" class="flex-1">
          <div class="ml-5 border-1 rounded p-3">
            <div class="mb-3 border-bottom-1"><span>History:</span> <span class="font-light ml-1">(last 20)</span></div>
            <div v-for="historyError in error.latestErrors" :key="historyError.id" class="border-bottom mb-3">
              <div>
                <span>Date:</span>
                <span class="ml-1">{{ error.lastSeen }}</span>
              </div>
              <pre class="bg-light p-3" style="max-width: 100%;">{{ historyError.serverBody }}</pre>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>

</style>

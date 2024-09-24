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
    <h2 class="pb-3 mb-5 border-bottom">
<!--      <b-icon icon="emoji-frown" class="font-light"/>-->
      <span class="uppercase mx-1">Errors</span> <span class="font-light">(last 20)</span>
      <Button @click="loadErrors" class="float-right mb-3" variant="outline-primary">
        arrow-repeat
<!--        <b-icon icon="arrow-repeat"/>-->
      </Button>
    </h2>
    <div v-if="loading" class="text-center">
<!--      <b-spinner variant="info" type="grow" label="Spinning" style="width: 3rem; height: 3rem;"></b-spinner>-->
      <div class="uppercase text-info mt-2">
        Loading...
      </div>
    </div>
    <div v-if="!errors || errors.length === 0" class="justify-content-md-center row">
      <div class="col-12 text-center">
        <h4>
<!--          <b-alert variant="success" show>-->
<!--            <b-icon icon="trophy"/>-->
            Congrats!! There are no errors!
<!--          </b-alert>-->
        </h4>
      </div>
    </div>
    <div v-if="!loading">
      <div v-for="error in errors" :key="error.id" class="row mt-3 border-bottom">
        <div class="col-12">
          <h5>
<!--            <b-badge class="p-2 border border-dark" variant="info">{{ error.httpStatus }}-->
<!--              <b-badge variant="light">{{ error.numOccur }}</b-badge>-->
<!--            </b-badge>-->
          </h5>
          <h6><span class="font-light">Last Seen:</span>
            <span class="ml-1">{{ error.lastSeen }}</span> <span
                class="font-light ml-1">({{ error.lastSeen }})</span>
            <a href="#" class="float-right" @click="collapseFlip(error)">View Error History
<!--              <b-icon icon="arrow-down-square-fill"/>-->
            </a>
          </h6>
        </div>
        <div class="col-12">
          <pre class="bg-light p-3">{{ formatJson(error.serverBody) }}</pre>
        </div>
        <div v-if="!error.collapsed" class="col-12">
          <div class="ml-5 border rounded p-3">
            <h5 class="mb-3 border-bottom"><span>History:</span> <span class="font-light ml-1">(last 20)</span></h5>
            <div v-for="historyError in error.latestErrors" :key="historyError.id" class="border-bottom mb-3">
              <h6><span class="font-light">Date:</span>
                <span class="ml-1">{{ error.lastSeen }}</span>
              </h6>
              <pre class="bg-light p-3">{{ formatJson(historyError.serverBody) }}</pre>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>

</style>

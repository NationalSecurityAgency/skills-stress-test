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
  <div class="text-left">
    <h2 class="pb-3 mb-5 border-bottom">
      <b-icon icon="emoji-frown" class="text-muted"/>
      <span class="text-uppercase mx-1">Errors</span> <span class="text-muted">(last 20)</span>
      <b-button @click="loadErrors" class="float-right mb-3" variant="outline-primary">
        <b-icon icon="arrow-repeat"/>
      </b-button>
    </h2>
    <div v-if="loading" class="text-center">
      <b-spinner variant="info" type="grow" label="Spinning" style="width: 3rem; height: 3rem;"></b-spinner>
      <div class="text-uppercase text-info mt-2">
        Loading...
      </div>
    </div>
    <div v-if="!errors || errors.length === 0" class="justify-content-md-center row">
      <div class="col-12 text-center">
        <h4>
          <b-alert variant="success" show>
            <b-icon icon="trophy"/>
            Congrats!! There are no errors!
          </b-alert>
        </h4>
      </div>
    </div>
    <div v-if="!loading">
      <div v-for="error in errors" :key="error.id" class="row mt-3 border-bottom">
        <div class="col-12">
          <h5>
            <b-badge class="p-2 border border-dark" variant="info">{{ error.httpStatus }}
              <b-badge variant="light">{{ error.numOccur }}</b-badge>
            </b-badge>
          </h5>
          <h6><span class="text-muted">Last Seen:</span>
            <span class="ml-1">{{ error.lastSeen | date }}</span> <span
                class="text-muted ml-1">({{ error.lastSeen | timePassed }})</span>
            <a href="#" class="float-right" @click="collapseFlip(error)">View Error History
              <b-icon icon="arrow-down-square-fill"/>
            </a>
          </h6>
        </div>
        <div class="col-12">
          <pre class="bg-light p-3">{{ formatJson(error.serverBody) }}</pre>
        </div>
        <div v-if="!error.collapsed" class="col-12">
          <div class="ml-5 border rounded p-3">
            <h5 class="mb-3 border-bottom"><span text-uppercase>History:</span> <span class="text-muted ml-1">(last 20)</span></h5>
            <div v-for="historyError in error.latestErrors" :key="historyError.id" class="border-bottom mb-3">
              <h6><span class="text-muted">Date:</span>
                <span class="ml-1">{{ error.lastSeen | date }}</span>
              </h6>
              <pre class="bg-light p-3">{{ formatJson(historyError.serverBody) }}</pre>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import StressTestsService from "@/services/StressTestsService";

export default {
  name: "ErrorsPage",
  data() {
    return {
      loading: true,
      errors: [],
    };
  },
  mounted() {
    this.errors = this.loadErrors();
  },
  methods: {
    loadErrors() {
      this.loading = true;
      StressTestsService.getErrors()
          .then((errors) => {
            this.errors = errors.map((er) => Object.assign({collapsed: true}, er))
            this.loading = false;
          });
    },
    formatJson(stringJson) {
      return JSON.stringify(JSON.parse(stringJson), null, 2)
    },
    collapseFlip(error) {
      error.collapsed = !error.collapsed;
      this.errors = this.errors.map((er) => er);
    }
  }
}
</script>

<style scoped>

</style>

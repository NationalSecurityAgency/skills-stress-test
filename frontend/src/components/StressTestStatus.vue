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
  <div v-if="status">

    <div class="text-left text-uppercase text-info mt-5 border-bottom">
      <div class="row">
        <div class="col">
          <h2 class="font-weight-bold">
            STRESS TEST RUN
          </h2>
        </div>
        <div class="col-md text-center text-md-right">
          <h5 class="text-muted">
            Errors: <router-link to="/errors"><span class="text-warning">{{ status.numErrors }}</span></router-link> <span class="d-none d-md-inline mx-2">|</span> Running: <span class="text-success">{{ running || false }}</span>
          </h5>
        </div>
      </div>
    </div>

    <div class="mt-5">
      <div v-if="status.reportSkillsRes" class="mb-5">
        <stress-tests-metrics :report-skills-res="status.reportSkillsRes" :start-timestamp="status.startTimestamp" title="Report Events"/>
      </div>
      <div v-if="status.clientDisplayStats" class="mt-5">
        <stress-tests-metrics :report-skills-res="status.clientDisplayStats"
                              :disable-res-charts="true"
                              :start-timestamp="status.startTimestamp" title="Client Display"/>
      </div>
      <div v-if="!status.reportSkillsRes && !status.clientDisplayStats" class="text-muted">
        No Runs Yet...
      </div>
    </div>
  </div>
</template>

<script>

import StressTestsMetrics from "@/components/StressTestsMetrics";

export default {
  name: "StressTestStatus",
  components: {StressTestsMetrics},
  props: ['status', 'running'],
}
</script>

<style scoped>

</style>

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
  <div v-if="status" class="card">
    <div class="card-header text-center text-md-left text-uppercase text-primary">
      <div class="row">
        <div class="col">
          <h5>
            STRESS TEST RUN
          </h5>
        </div>
        <div class="col-md text-center text-md-right">
          <h5 class="text-muted">
            Errors: <span class="text-warning">{{ status.numErrors }}</span> <span class="d-none d-md-inline mx-2">|</span> Running: <span class="text-success">{{ running || false }}</span>
          </h5>
        </div>
      </div>
    </div>
    <div class="card-body">
      <h3 class="text-left border-bottom text-info">Report Events</h3>
      <stress-tests-metrics :report-skills-res="status.reportSkillsRes"/>

      <h3 class="text-left border-bottom text-info mt-5">Client Display</h3>
      <stress-tests-metrics :report-skills-res="status.clientDisplayStats"/>
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

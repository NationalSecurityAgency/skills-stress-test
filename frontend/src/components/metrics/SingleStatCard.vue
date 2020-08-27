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
  <div class="card border-left shadow h-100 py-2 text-left">
    <div class="card-body">
      <div class="row no-gutters align-items-center">
        <div class="col mr-2">
          <div class="font-weight-bold text-uppercase mb-1">{{ title }}</div>
          <div class="h5 mb-0 font-weight-bold text-dark">{{  value | number }}</div>
        </div>
        <div class="col-auto text-muted">
          <h1><b-icon :icon="icon"/></h1>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: "SingleStatCard",
  props: ['title', 'value', 'icon']
}
</script>

<style scoped>

</style>

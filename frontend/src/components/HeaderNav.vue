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

</script>

<template>
    <div class="flex gap-4 header p-3">
      <router-link to="/" class="text-white text-2xl">
        <i class="pi pi-hammer" style="font-size: 1rem"></i>
        SkillTree Stress Tests
      </router-link>
      <router-link to="/errors" class="text-white text-xl">Errors</router-link>
    </div>
</template>

<style scoped>
.header {
  background-color: #17a2b8 !important
}
</style>

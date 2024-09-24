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
defineProps(['title', 'value', 'icon']);

</script>

<template>
  <Card class="text-left">
    <template #content>
      <div class="flex align-items-center">
        <div class="flex-1 mr-2">
          <div class="font-bold uppercase mb-1">{{ title }}</div>
          <div class="h5 mb-0 font-bold text-dark">{{  value }}</div>
        </div>
        <div>
          <i :class="`pi ${icon}`" style="font-size: 2rem"></i>
        </div>
      </div>
    </template>
  </Card>
</template>

<style scoped>

</style>

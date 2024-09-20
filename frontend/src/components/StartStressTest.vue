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
  import { ref, defineProps, defineEmits } from 'vue';
  defineProps(['running']);
  const emit = defineEmits(['start-test', 'stop-test']);

  const expanded = ref(null);
  const serviceUrl = ref('http://localhost:8080')
  const numProjects = ref(2)
  const subjPerProject = ref(6)
  const skillsPerSubject = ref(50)
  const badgesPerProject = ref(10)
  const hasDependenciesEveryNProjects = ref(5)
  const numUsersPerApp = ref(100)
  const numConcurrentThreads = ref(5)
  const sleepMsBetweenRequests = ref(500)
  const removeExistingTestProjects = ref(false)

  const startTest = () => {
    emit('start-test', {
      numProjects: numProjects.value,
      subjPerProject: subjPerProject.value,
      skillsPerSubject: skillsPerSubject.value,
      badgesPerProject: badgesPerProject.value,
      hasDependenciesEveryNProjects: hasDependenciesEveryNProjects.value,
      numUsersPerApp: numUsersPerApp.value,
      numConcurrentThreads: numConcurrentThreads.value,
      removeExistingTestProjects: removeExistingTestProjects.value,
      sleepMsBetweenRequests: sleepMsBetweenRequests.value,
      serviceUrl: serviceUrl.value,
    });
  };

  const stopTest = () => {
    emit('stop-test', false);
  };
</script>

<template>
  <Card>
    <template #title>
      <h4>
        <span class="text-uppercase text-primary">Stress Test Settings</span>
        <ToggleButton v-model="expanded" offLabel="Show Settings" onLabel="Hide Settings">
        </ToggleButton>
      </h4>
    </template>
    <template #content>
      <div class="flex text-left">
        <div class="flex flex-1">
          <span class="text-uppercase text-muted">Service Url</span>
          <InputText v-model="serviceUrl"></InputText>
        </div>
      </div>
      <div v-if="expanded">
        <div class="flex text-left mt-3">
          <div class="flex flex-1">
            <span class="text-uppercase text-muted"># Projs</span>
            <InputNumber v-model="numProjects"></InputNumber>
          </div>
          <div class="flex flex-1">
            <span class="text-uppercase text-muted">Subj Per Proj</span>
            <InputNumber v-model="subjPerProject"></InputNumber>
          </div>
          <div class="flex flex-1">
            <span class="text-uppercase text-muted">skills per Subj</span>
            <InputNumber v-model="skillsPerSubject"></InputNumber>
          </div>
          <div class="flex flex-1">
            <span class="text-uppercase text-muted">badges per Proj</span>
            <InputNumber v-model="badgesPerProject"></InputNumber>
          </div>
        </div>
        <div class="flex text-left mt-3">
          <div class="flex flex-1">
            <span class="text-uppercase text-muted">Dependency Every N Projects</span>
            <InputNumber v-model="hasDependenciesEveryNProjects"></InputNumber>
          </div>
          <div class="flex flex-1">
            <span class="text-uppercase text-muted">Users Per Proj</span>
            <InputNumber v-model="numUsersPerApp"></InputNumber>
          </div>
        </div>
        <div class="flex text-left mt-3">
          <div class="flex flex-1">
            <span class="text-uppercase text-muted">Number of Concurrent Threads</span>
            <InputNumber v-model="numConcurrentThreads"></InputNumber>
          </div>
          <div class="flex flex-1">
            <span class="text-uppercase text-muted">Sleep (MS) between Requests</span>
            <InputNumber v-model="sleepMsBetweenRequests"></InputNumber>
          </div>
          <div class="flex flex-1">
            <span class="text-uppercase text-muted">Remove Existing Test Projects</span>
            <Checkbox v-model="removeExistingTestProjects" name="check-button" switch>
              <span v-if="removeExistingTestProjects">Existing projects will be deleted</span>
              <span v-else>Existing projects will be preserved</span>
            </Checkbox>
          </div>
        </div>
      </div>
      <div class="text-left mt-3 text-uppercase">
        <Button v-if="!running" @click="startTest" variant="outline-primary">Start Test</Button>
        <Button v-else @click="stopTest" variant="outline-warning">Stop Test</Button>
      </div>
    </template>
  </Card>
</template>

<style scoped>

</style>

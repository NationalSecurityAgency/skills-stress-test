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
    <div class="card mt-2">
        <div class="card-header text-left text-uppercase text-primary">
            <h5>Stress Test Settings</h5>
        </div>
        <div class="card-body">
            <div class="row text-left">
                <div class="col-lg">
                    <span class="text-uppercase text-muted">Service Url</span>
                    <b-form-input v-model="serviceUrl" type="string"></b-form-input>
                </div>
            </div>
            <div class="row text-left mt-3">
                <div class="col-lg">
                    <span class="text-uppercase text-muted"># Projs</span>
                    <b-form-input v-model="numProjects" type="number"></b-form-input>
                </div>
                <div class="col-lg">
                    <span class="text-uppercase text-muted">Subj Per Proj</span>
                    <b-form-input v-model="subjPerProject" type="number"></b-form-input>
                </div>
                <div class="col-lg">
                    <span class="text-uppercase text-muted">skills per Subj</span>
                    <b-form-input v-model="skillsPerSubject" type="number"></b-form-input>
                </div>
                <div class="col-lg">
                    <span class="text-uppercase text-muted">badges per Proj</span>
                    <b-form-input v-model="badgesPerProject" type="number"></b-form-input>
                </div>
            </div>
            <div class="row text-left mt-3">
                <div class="col-lg">
                    <span class="text-uppercase text-muted">Dependency Every N Projects</span>
                    <b-form-input v-model="hasDependenciesEveryNProjects" type="number"></b-form-input>
                </div>
                <div class="col-lg">
                    <span class="text-uppercase text-muted">Users Per Proj</span>
                    <b-form-input v-model="numUsersPerApp" type="number"></b-form-input>
                </div>
            </div>
            <div class="row text-left mt-3">
                <div class="col-lg">
                    <span class="text-uppercase text-muted">Number of Concurrent Threads</span>
                    <b-form-input v-model="numConcurrentThreads" type="number"></b-form-input>
                </div>
                <div class="col-lg">
                    <span class="text-uppercase text-muted">Remove Existing Test Projects</span>
                    <b-form-checkbox v-model="removeExistingTestProjects" name="check-button" switch>
                        <span v-if="removeExistingTestProjects">Existing projects will be deleted</span>
                        <span v-else>Existing projects will be preserved</span>
                    </b-form-checkbox>
                </div>
            </div>
            <div class="text-left mt-3 text-uppercase">
                <b-button v-if="!running" @click="startTest" variant="outline-primary">Start Test</b-button>
                <b-button v-else @click="stopTest" variant="outline-warning">Stop Test</b-button>
            </div>
        </div>
    </div>
</template>

<script>
    export default {
        name: "StartStressTest",
        props: ['running'],
        data() {
            return {
                serviceUrl: 'http://localhost:8080',
                numProjects: 2,
                subjPerProject: 6,
                skillsPerSubject: 50,
                badgesPerProject: 10,
                hasDependenciesEveryNProjects: 5,
                numUsersPerApp: 100,
                numConcurrentThreads: 5,
                removeExistingTestProjects: false,
            };
        },
        methods: {
            startTest() {
                this.$emit('start-test', {
                    numProjects: this.numProjects,
                    subjPerProject: this.subjPerProject,
                    skillsPerSubject: this.skillsPerSubject,
                    badgesPerProject: this.badgesPerProject,
                    hasDependenciesEveryNProjects: this.hasDependenciesEveryNProjects,
                    numUsersPerApp: this.numUsersPerApp,
                    numConcurrentThreads: this.numConcurrentThreads,
                    removeExistingTestProjects: this.removeExistingTestProjects,
                    serviceUrl: this.serviceUrl,
                });
            },
            stopTest() {
                this.$emit('stop-test', false);
            }
        },
    }
</script>

<style scoped>

</style>

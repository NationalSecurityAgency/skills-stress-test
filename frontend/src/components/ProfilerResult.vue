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
    <div v-if="prof" class="mt-3">
        <h2 class="text-uppercase border-bottom text-muted">
            {{ title }}
        </h2>
        <div class="row mb-4">
            <div class="col">
                <p class="text-uppercase text-muted count-label">Events</p>
                <strong class="h5">{{ prof.totalEvents | number }}</strong>
            </div>
            <div class="col">
                <p class="text-uppercase text-muted count-label">Exec Time (Seconds)</p>
                <strong class="h5">{{ prof.totalExecTime | number }}</strong>
            </div>
        </div>

        <div class="row text-left">
            <div class="col-lg border rounded p-3 mr-2">
                <h5 class="text-uppercase">Overall</h5>
                <div class="mb-2">Average Response Time: <span class="text-info">{{ prof.avgEventResponse }} ms</span>
                </div>
                <b-table striped hover :items="prof.groupedExecTimes">
                    <template v-slot:cell(numberOfEvents)="numberOfEvents">
                        {{ formatNum(numberOfEvents.value) }}
                    </template>
                </b-table>
            </div>
            <div class="col-lg border rounded p-3">
                <h5 class="text-uppercase">Last 1K</h5>
                <div class="mb-2">Average Response Time: <span class="text-info">{{ prof.avgEventResponseLast1k }} ms</span>
                </div>
                <b-table striped hover :items="prof.groupedExecTimesLast1k">
                    <template v-slot:cell(numberOfEvents)="numberOfEvents">
                        {{ formatNum(numberOfEvents.value) }}
                    </template>
                </b-table>
            </div>
        </div>
    </div>
</template>

<script>
    import NumberFilter from "../filters/NumberFilter";

    export default {
        name: "ProfilerResult",
        props: ['prof', 'title'],
        methods: {
            formatNum(numVal) {
                return NumberFilter(numVal);
            },
        },
    }
</script>

<style scoped>

</style>

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
    <div>
        <b-navbar toggleable="lg" type="dark" variant="info">
          <b-navbar-brand>
            <router-link to="/" class="text-white"><b-icon icon="hammer" class="mr-1" />SkillTree Stress Tests</router-link>
          </b-navbar-brand>
          <b-collapse id="nav-collapse" is-nav>
            <b-navbar-nav>
              <b-nav-item>
                <router-link to="/errors" class="text-white">Errors</router-link>
              </b-nav-item>
            </b-navbar-nav>
          </b-collapse>
        </b-navbar>
    </div>
</template>

<script>
    export default {
        name: "HeaderNav",
    }
</script>

<style scoped>

</style>

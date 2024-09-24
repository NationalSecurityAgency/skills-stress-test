/*
 * Copyright 2020 SkillTree
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
import { createApp } from 'vue';
import App from './App.vue'
import VueApexCharts from 'vue3-apexcharts';
// import './filters/NumberFilter';
// import './filters/DateFilter';
// import './filters/TimePassedFilter';
import router from './router';
import PrimeVue from 'primevue/config';
import Aura from '@primevue/themes/aura';
import Button from "primevue/button"
import InputNumber from 'primevue/inputnumber';
import InputText from 'primevue/inputtext';
import Checkbox from 'primevue/checkbox';
import Card from 'primevue/card';
import ToggleButton from 'primevue/togglebutton';
import ToggleSwitch from 'primevue/toggleswitch';

import 'primeflex/primeflex.css';
import 'primeicons/primeicons.css';

const app = createApp(App);
app.use(router);
app.use(VueApexCharts);
app.use(PrimeVue, {
    theme: {
        preset: Aura,
        options: {
            darkModeSelector: '.fake-selector'
        }
    }
});
app.component('Button', Button);
app.component('InputNumber', InputNumber);
app.component('InputText', InputText);
app.component('Checkbox', Checkbox);
app.component('Card', Card);
app.component('ToggleButton', ToggleButton);
app.component('ToggleSwitch', ToggleSwitch);

app.mount('#app')

// Vue.config.productionTip = false

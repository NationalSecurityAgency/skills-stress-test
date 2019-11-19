<template>
    <div id="app">
        <!--    <img alt="Vue logo" src="./assets/logo.png">-->
        <!--    <HelloWorld msg="Welcome to Your Vue.js App"/>-->
        <header-nav/>
        <div class="container">
            <start-stress-test @start-test="startTest" @stop-test="stopTest" :running="running"/>
            <stress-test-status class="mt-2" :status="status" :running="running"/>
        </div>
    </div>
</template>

<script>
    // import HelloWorld from './components/HelloWorld.vue'
    import StressTestStatus from "./components/StressTestStatus";
    import HeaderNav from "./components/HeaderNav";
    import StartStressTest from "./components/StartStressTest";
    import StressTestsService from "./services/StressTestsService";

    export default {
        name: 'app',
        components: {
            StartStressTest,
            HeaderNav,
            StressTestStatus,
        },
        data() {
            return {
                running: false,
                status: {},
                timer: undefined,
            };
        },
        mounted() {
            this.loadStatus();
        },
        methods: {
            loadStatus() {
                StressTestsService.getStatus()
                    .then((status) => {
                        this.status = status
                    });
            },
            startTest(config) {
                this.running = true;
                StressTestsService.startTest(config);
            },
            stopTest() {
                this.running = false;
                StressTestsService.stopTest();
            }
        },
        watch: {
            running(val) {
                if (val === true) {
                    this.timer = setInterval(this.loadStatus, 2000);
                } else {
                    clearInterval(this.timer);
                }
            },
        },
        beforeDestroy() {
            if (this.timer) {
                clearInterval(this.timer);
            }
        }
    }
</script>

<style>
    #app {
        font-family: 'Avenir', Helvetica, Arial, sans-serif;
        -webkit-font-smoothing: antialiased;
        -moz-osx-font-smoothing: grayscale;
        text-align: center;
        color: #2c3e50;
    }
</style>

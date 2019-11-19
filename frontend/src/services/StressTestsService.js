import axios from 'axios';

export default {
    getStatus() {
        return axios.get('/stress/status')
            .then(response => response.data);
    },
    startTest(testConfig) {
        return axios.post('/stress/start', testConfig)
            .then(response => response.data);
    },
    stopTest() {
        return axios.post('/stress/stop')
            .then(response => response.data);
    },
};

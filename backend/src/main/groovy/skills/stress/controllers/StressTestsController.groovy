package skills.stress.controllers

import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.actuate.autoconfigure.security.servlet.ManagementWebSecurityAutoConfiguration
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.RestController
import skills.stress.HitSkillsHard
import skills.stress.model.StatusRes
import skills.stress.model.StressTestParams

import javax.annotation.PostConstruct

@RestController
@RequestMapping("/stress")
@EnableAutoConfiguration(exclude = [SecurityAutoConfiguration.class, ManagementWebSecurityAutoConfiguration.class])
@Slf4j
class StressTestsController {

    @Value('#{"${skills.stress.pkiMode:false}"}')
    Boolean pkiMode

    @Value('#{"${skills.stress.pkiMode.userIdsFile}"}')
    String pkiModeUserIdsFilePath

    HitSkillsHard hitSkillsHard

    @PostConstruct
    void init(){
        log.info("Is PKI Mode: {}", pkiMode)
        if (pkiMode){
            assert pkiModeUserIdsFilePath
        }
    }

    @RequestMapping(value = "/status", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    def getStatus() {
        return hitSkillsHard ? hitSkillsHard.buildStatus() : new StatusRes()
    }

    @RequestMapping(value = "/start", method = [RequestMethod.PUT, RequestMethod.POST], produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    def startStress(@RequestBody StressTestParams stressTestParams) {
        log.info("Starting stress test: {}", stressTestParams)
        StressTestParams params = stressTestParams
        hitSkillsHard = new HitSkillsHard(
                numProjects: params.numProjects,
                subjPerProject: params.subjPerProject,
                skillsPerSubject: params.skillsPerSubject,
                badgesPerProject: params.badgesPerProject,
                hasDependenciesEveryNProjects: params.hasDependenciesEveryNProjects,
                numUsersPerApp: params.numUsersPerApp,
                numConcurrentThreads: params.numConcurrentThreads,
                removeExistingTestProjects: params.removeExistingTestProjects,
                serviceUrl: stressTestParams.serviceUrl,
                pkiMode: pkiMode,
                pkiModeUserIdFilePath: pkiModeUserIdsFilePath
        ).init()
        hitSkillsHard.run()

        return [ status: "success" ]
    }

    @RequestMapping(value = "/stop", method = [RequestMethod.PUT, RequestMethod.POST], produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    def stop() {
        log.info("Stopping Stress Tests")
        hitSkillsHard.stop()
        return [ status: "success" ]
    }
}

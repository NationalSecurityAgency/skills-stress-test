/**
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
package skills.stress.controllers

import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Autowired
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
import skills.stress.errors.ErrorTracker
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

    @Value('#{"${skills.stress.prependToDescription}"}')
    String prependToDescription

    HitSkillsHard hitSkillsHard

    @Autowired
    ErrorTracker errorTracker

    @PostConstruct
    void init() {
        log.info("Is PKI Mode: {}", pkiMode)
        if (pkiMode) {
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

        errorTracker.reset()

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
                pkiModeUserIdFilePath: pkiModeUserIdsFilePath,
                prependToDescription: prependToDescription,
                errorTracker: errorTracker
        ).init()
        hitSkillsHard.run()

        return [status: "success"]
    }

    @RequestMapping(value = "/stop", method = [RequestMethod.PUT, RequestMethod.POST], produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    def stop() {
        log.info("Stopping Stress Tests")
        hitSkillsHard.stop()
        return [status: "success"]
    }


    @RequestMapping(value = "/errors", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    def getErrors() {
        return errorTracker.getErrors()
    }
}

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
package skills.stress.stats

import skills.stress.model.LabelsAndSeries

class ExplanationUtils {

    static void documentResult( Map<String, Long> binExplanations, def parsed) {
        String explanation = formatExplanation(parsed.explanation)
        Long count = binExplanations.get(explanation)
        if (count == null){
            binExplanations[explanation] = 1l
        } else {
            binExplanations[explanation] = count + 1l
        }
    }
    private static String formatExplanation(String explanation) {
        if (explanation.startsWith("This skill was already performed ") && explanation.contains("within the configured time period")) {
            return "This skill was already performed in its Time Window"
        }
        if (explanation.startsWith("Not all dependent skills have been achieved.")){
            return "Not all dependent skills have been achieved"
        }
        return explanation
    }

    static LabelsAndSeries buildLabelAndSeries(Map<String, Long> binExplanations){
        if (!binExplanations){
            return null
        }
        LabelsAndSeries explanationCounts = new LabelsAndSeries()
        binExplanations.each {
            explanationCounts.labels.add(it.key)
            explanationCounts.series.add(it.value)
        }

        return explanationCounts
    }
    static Map<String, Long> constructEmptyExplanationMap(){
        return [
                "This skill was already performed in its Time Window" : 0l,
                "This skill reached its maximum points": 0l,
                "Not all dependent skills have been achieved": 0l,
                "Skill event was applied": 0l,
        ]
    }
}

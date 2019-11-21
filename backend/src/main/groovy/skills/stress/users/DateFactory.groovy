package skills.stress.users

import groovy.time.TimeCategory

class DateFactory {

    int numDates = 365

    Random r = new Random()
    Date getDate(){
        int ranNum = r.nextInt(numDates)
        use (TimeCategory) {
            return ranNum.days.ago
        }
    }
}

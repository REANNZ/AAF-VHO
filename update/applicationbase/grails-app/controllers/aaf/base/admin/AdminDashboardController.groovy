package aaf.base.admin

import groovy.time.TimeCategory

import aaf.base.identity.*

class AdminDashboardController {
  
  def index() {
    def subjectCount = Subject.countByEnabled(true) 
    def disabledSubjectCount = Subject.countByEnabled(false) 
    def roleCount = Role.count()
    def permCount = Permission.count()

    def lastHourSessions, lastDaySessions, lastWeekSessions, lastMonthSessions, last12MonthSessions
    use(TimeCategory) {
      def queryParams = [:]
      queryParams.endDate = new Date()
      queryParams.startDate = queryParams.endDate - 1.hour

      lastHourSessions = SessionRecord.executeQuery("select count(*) from SessionRecord where dateCreated between :startDate and :endDate", queryParams)[0]

      queryParams.startDate = queryParams.endDate - 1.day
      lastDaySessions = SessionRecord.executeQuery("select count(*) from SessionRecord where dateCreated between :startDate and :endDate", queryParams)[0]

      queryParams.startDate = queryParams.endDate - 1.week
      lastWeekSessions = SessionRecord.executeQuery("select count(*) from SessionRecord where dateCreated between :startDate and :endDate", queryParams)[0]

      queryParams.startDate = queryParams.endDate - 30.days
      lastMonthSessions = SessionRecord.executeQuery("select count(*) from SessionRecord where dateCreated between :startDate and :endDate", queryParams)[0]

      queryParams.startDate = queryParams.endDate - 12.month
      last12MonthSessions = SessionRecord.executeQuery("select count(*) from SessionRecord where dateCreated between :startDate and :endDate group by month(dateCreated)", queryParams)
    
      def monthsCovered = last12MonthSessions.size()
      if( monthsCovered < 12) {
        while ( monthsCovered++ < 12 ) {
            last12MonthSessions.add(0,0)
        }  
      }
    }

    def query = "from SessionRecord as sr order by sr.dateCreated desc"
    def last25sessions = SessionRecord.findAll(query, [max: 25])

    [subjectCount:subjectCount, disabledSubjectCount:disabledSubjectCount, roleCount:roleCount, permCount:permCount, lastHourSessions:lastHourSessions,
    lastDaySessions:lastDaySessions, lastWeekSessions:lastWeekSessions, lastMonthSessions:lastMonthSessions, last25sessions:last25sessions, last12MonthSessions:last12MonthSessions]
  }

  def environment() {
    log.info "requesting environment"
  }

}

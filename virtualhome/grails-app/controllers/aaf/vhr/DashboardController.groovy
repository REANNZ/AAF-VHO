package aaf.vhr

import groovy.time.TimeCategory
import aaf.base.identity.SessionRecord
import aaf.vhr.AdminHelper

class DashboardController {
  
  static defaultAction = "dashboard"

  def welcome = {
    if(subject) {
      redirect action:'dashboard'
    }
  }

  def dashboard = {
    def organizationInstanceList = [] as List
    def groupInstanceList = [] as List
    def collatedGroupInstanceList = [:]
    def statistics = [:]
      
    use(TimeCategory) {
      subject.roles?.each { role -> 
        if(role.name.startsWith('organization:')) {
          def components = role.name.split(':')
          if(components.size() == 3) {
            def organization = Organization.get(components[1])
            organizationInstanceList << organization

            organization.groups.each { group ->
              groupInstanceList << group
            }
          }
        }
        if(role.name.startsWith('group:')) {
          def components = role.name.split(':')
          if(components.size() == 3) {
            def group = Group.get(components[1])
            if(!groupInstanceList.contains(group)){
              groupInstanceList << group
            }
            // We also need to add any organization that owns a group we're an admin of, even if we're not in that organization.
            def groupOrg = group.organization
            if (AdminHelper.isOrganizationInsider(groupOrg.id as Integer)) {
              organizationInstanceList << groupOrg
            }
          }
        }
      }
      
      statistics.organizations = Organization.count()
      statistics.groups = Group.count()
      statistics.managedSubjects = ManagedSubject.count()

      def queryParams = [:]
      queryParams.endDate = new Date()
      queryParams.startDate = queryParams.endDate - 12.month
      def last12MonthSessions = SessionRecord.executeQuery("select count(*) from SessionRecord where dateCreated between :startDate and :endDate group by month(dateCreated)", queryParams)
    
      def monthsCovered = last12MonthSessions.size()
      if( monthsCovered < 12) {
        while ( monthsCovered++ < 12 ) {
            last12MonthSessions.add(0,0)
        }  
      }

      statistics.last12MonthSessions = last12MonthSessions
    }

    groupInstanceList.each {
      if(collatedGroupInstanceList."${it.organization.displayName}" == null) {
        collatedGroupInstanceList."${it.organization.displayName}" = [] as List
      }
      if(!collatedGroupInstanceList."${it.organization.displayName}".contains(it))
        collatedGroupInstanceList."${it.organization.displayName}" << it
    }

    // There's a risk that when we add an org due to us being a group admin that we have duplicated
    organizationInstanceList.unique()
    [organizationInstanceList:organizationInstanceList, groupInstanceList: collatedGroupInstanceList, statistics:statistics]
  }

}

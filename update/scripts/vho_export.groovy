import groovy.sql.Sql
import groovy.json.*
import java.security.MessageDigest

import java.text.SimpleDateFormat
import java.util.Date

/*
Suck data out of SWITCH VHO DB, munge and output in JSON for VHR import.
Haxored up as quickly as possible by Bradley Beddoes.

There is some terribly awful shit in here but I hope to only ever need to use it twice (test/production) ;-)
*/

// VHO specific config
database = 'VHOtools'
dbusername = 'root'
dbpassword = ''

// You shouldn't really have to haxor below here
sql = Sql.newInstance( "jdbc:mysql://localhost:3306/$database", dbusername, dbpassword, 'com.mysql.jdbc.Driver' )

dateFormat = "EEE, d MMM yyyy HH:mm:ss z"
md5hashes = []

def populateSubjectFromUser(def u) {
    SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);
    sdf.setTimeZone(TimeZone.getTimeZone("GMT"));

    def subject = [:]
    subject.login = u.username

    byte[] ba = u.password.decodeBase64()

    String hex = "";
    for(int i = 0; i < ba.length; i++)
      hex += Integer.toString( ( ba[i] & 0xff ) + 0x100, 16).substring( 1 );

    subject.md5password = hex
    md5hashes.add(hex)

    subject.cn = "${u.attributes.givenname.trim().replace(' ','_')} ${u.attributes.surname.trim().replace(' ','_')}"
    subject.email = u.attributes.mail
    if(u.attributes.auEduPersonSharedToken)
      subject.sharedToken = u.attributes.auEduPersonSharedToken
    subject.eduPersonAffiliation = u.attributes.eduPersonAffiliation

    def entitlement = u.attributes.eduPersonEntitlement
    if(entitlement && entitlement.startsWith('urn:mace:') && !entitlement == 'urn:mace:aaf.edu.au' && !entitlement.startsWith('urn:mace:aaf.edu.au:iap')) {
      subject.eduPersonEntitlement = entitlement
    }

    subject.givenName = u.attributes.givenname.trim().replace(' ','_')  // dodgy hax to get a single string for given/surname
    subject.surname = u.attributes.surname.trim().replace(' ','_')
    
    subject.accountExpires = sdf.format(u.dateExpire);

    subject.storedid = []
    storedIDs = sql.rows ( 'select * from shibpid where principalName = ? and deactivationDate IS NULL', subject.login)
    storedIDs.each { sid ->
      def storedid = [:]
      storedid.localEntity = sid.localEntity
      storedid.peerEntity = sid.peerEntity
      storedid.persistentId = sid.persistentId
      storedid.peerProvidedId = sid.peerProvidedId
      storedid.creationDate = sdf.format(sid.creationDate)

      subject.storedid.add(storedid)
    }

    subject
}

def activeUsers = sql.rows( 'select * from vho_Users where state = "active"' )

println "A total of ${activeUsers.size()} to possibly be migrated"

println "\nLets have a look at the quality of our user data..."

def userErrorCount = 0
def invalidUsers = []

activeUsers.eachWithIndex { user, i ->

  def attributes = sql.rows( 'select a.attributeName as name, ua.value as value from vho_Attributes as a left join vho_UserAttributes as ua on a.idAttribute = ua.idAttribute where ua.idUser = ?', user.idUser).collectEntries{ [it.name, it.value] }

  if(!attributes.givenname || !attributes.surname) {

    def cnPattern = /^([a-zA-Z]+) ([a-zA-Z]+)$/

    if(attributes.cn && attributes.cn ==~ cnPattern) {
        def names = attributes.cn =~ cnPattern
        attributes.givenname = names[0][1]
        attributes.surname = names[0][2]
    }
  }

  if(!attributes.givenname || !attributes.surname || !attributes.eduPersonAffiliation) {
    if(!attributes.eduPersonSharedToken) {
      println "Ejecting invalid account data for ${user.username} who has never logged in"
      userErrorCount++
      invalidUsers.add(user)
    } else {
      println "Erronous account data for ${user.username} which needs manual correction\n$user\n$attributes\n"
    }
  } else {
    user.attributes = attributes
  }
}

activeUsers.removeAll(invalidUsers)

activeCount = activeUsers.size()
println "A total of ${activeUsers.size()} to be migrated after further clean up"

def groupCount = sql.firstRow( 'select count(*) as total from vho_Groups' ).total

// Holy hacks batman!!
def activeUsernames = activeUsers.collect {it.username}
def placeholders = []
activeUsernames.each { placeholders << '?' }
def select = "select g.idParent, g.name, g.description, g.idGroup from vho_Groups as g inner join vho_Users as u on g.idGroup = u.idGroup where u.username IN (${placeholders.join(',')}) or g.idParent = 1 group by u.idGroup"

def groups = sql.rows(select, activeUsernames)

// Unholy hacks!
// def orgs = groups.findAll{ group -> group.idParent == 1}
// accept 0 as parent ID to also import the root group
def orgs = groups.findAll{ group -> group.idParent <= 1}

println "There are a total of $groupCount groups known to the old VHO of which only ${groups.size()} have active users. From these ${orgs.size()} are supposedly organizations."

def adminSelect = "select am.idAdministrator, am.uniqueID, am.givenname, am.surname, am.mail from vho_Groups as g left join vho_GroupAdministrators as agm on g.idGroup = agm.idGroup left join vho_Administrators as am on agm.idAdministrator = am.idAdministrator where g.idGroup = ?;"
    
def organizations = []
orgs.sort{it.name}.each { o -> 
  def org = [:]
  org.name = o.name
  org.description = o.description ?: o.name
  org.count = activeUsers.findAll{ u -> u.idGroup == o.idGroup}.size()

  org.administrators = []
  def admins = sql.rows( adminSelect, o.idGroup )
  if(admins?.size() > 0) {
    admins?.sort{it.surname}.each { a ->
      if(a.givenname && a.surname && a.mail && a.uniqueID?.size() > 10) {
        def admin = [:]
        admin.givenName = a.givenname
        admin.surname = a.surname
        admin.email = a.mail
        admin.principal = a.uniqueID

        org.administrators.add(admin)
      }
    }
  }

  org.subjects = []
  activeUsers.findAll{ u -> u.idGroup == o.idGroup}.each { u ->
    org.subjects.add(populateSubjectFromUser(u))
  }

  org.groups = []
  // unholy hack countermeasure: for root group, do not recurse into groups
  def children = ( o.idGroup > 1 ? groups.findAll{ group -> group.idParent == o.idGroup} : [] )
  if(children) {
    children.each { child ->
      def group = [:]
      group.name = child.name
      group.description = child.description ?: child.name
      group.count = activeUsers.findAll{ u -> u.idGroup == child.idGroup}.size()

      group.administrators = []
      admins = sql.rows( adminSelect, child.idGroup )
      if(admins?.size() > 0) {
        admins?.sort{it.surname}.each { a ->
          if(a.givenname && a.surname && a.mail && a.uniqueID?.size() > 10) {
            def cadmin = [:]
            cadmin.givenName = a.givenname
            cadmin.surname = a.surname
            cadmin.email = a.mail
            cadmin.principal = a.uniqueID

            group.administrators.add(cadmin)
          }
        }
      }

      group.subjects = []
      activeUsers.findAll{ u -> u.idGroup == child.idGroup}.each { u ->
        group.subjects.add(populateSubjectFromUser(u))
      }
    
      def subChildren = groups.findAll{ g -> g.idParent == child.idGroup}
      if(subChildren) {
        subChildren.each { schild ->
          def scgroup = [:]
          scgroup.name = schild.name
          scgroup.description = schild.description ?: schild.name
          scgroup.count = activeUsers.findAll{ u -> u.idGroup == schild.idGroup}.size()

          scgroup.subjects = []
          activeUsers.findAll{ u -> u.idGroup == schild.idGroup}.each { u ->
            scgroup.subjects.add(populateSubjectFromUser(u))
          }

          scgroup.administrators = []
          def schildAdmins = sql.rows( adminSelect, schild.idGroup )
          if(schildAdmins?.size() > 0) {
            schildAdmins.sort{it.surname}.each { a ->
              if(a.givenname && a.surname && a.mail && a.uniqueID?.size() > 10){
                def scadmin = [:]
                scadmin.givenName = a.givenname
                scadmin.surname = a.surname
                scadmin.email = a.mail
                scadmin.principal = a.uniqueID

                scgroup.administrators.add(scadmin)
              }
            }
          }

          org.groups.add(scgroup)  // Flatten the structure down`
        }
      }
      org.groups.add(group)
    }
  }

  organizations.add(org)
}


def json = new JsonBuilder(organizations)

File file = new File("/tmp/${database}.json")
file.delete()
file.write(json.toPrettyString())
println "JSON output of database $database stored at /tmp/${database}.json"

file = new File("/tmp/${database}_fr_mapping.txt") 
if(!file.exists()) {
  organizations.each {
    file << "${it.name}:\n"
  }
  println "A file ready to map FR organization ID stored at /tmp/${database}_fr_mapping.txt"
}

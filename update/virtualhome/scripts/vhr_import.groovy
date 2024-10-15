import aaf.base.identity.*
import aaf.vhr.*
import aaf.vhr.switchch.vho.*
import grails.converters.*
import org.codehaus.groovy.grails.web.json.*
import java.text.SimpleDateFormat
import java.util.Date
import groovy.sql.Sql

/*
Grab JSON data from SWITCH VHO output and populate into FR.
Haxored up as quickly as possible by Bradley Beddoes.

There is some terribly awful shit in here but I hope to only ever need to use it twice (test/production) ;-)
*/

// Environment config
commit = true
verbose = false
updateOrgs = false
mappingFile = "/tmp/old_vho_prd_fr_mapping.txt"
dataFile = "/tmp/old_vho_prd.json"

// Shouldn't need to modify anything below here

dataSource = ctx.dataSource
sql = new Sql(dataSource)

if(updateOrgs) {
  organizationService = ctx.getBean('organizationService')
  organizationService.populate()
}

sharedTokenService = ctx.getBean('sharedTokenService')
managedSubjectService = ctx.getBean('managedSubjectService')

dateFormat = "EEE, d MMM yyyy HH:mm:ss z";
count = 0
ecount = 0
frMap = [:]
orgs = []

m = new File(mappingFile)
m.eachLine {
  def mapping = it.split(':') //e.g aaf:2 - [vhogroupname]:[FR Org ID]
  
  // No mapping - no import
  if(mapping.size() == 2 && mapping[1].isLong())
    frMap.put(mapping[0], mapping[1].toLong())
}

f = new File(dataFile)
orgs = JSON.parse(f.text)

def createManagedSubject(def s, def group) {
    SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);
    sdf.setTimeZone(TimeZone.getTimeZone("GMT"));

    def ms = new ManagedSubject()
    ms.active = true
    ms.login = s.login
    ms.cn = s.cn
    ms.displayName = s.cn
    ms.email = s.email
    
    if(s.sharedToken) {
        ms.sharedToken = s.sharedToken
    }
    else {
        sharedTokenService.generate(ms)
    }
    
    ms.eduPersonAffiliation = s.eduPersonAffiliation
    ms.eduPersonEntitlement = s.eduPersonEntitlement
    ms.eduPersonAssurance = 'urn:mace:aaf.edu.au:iap:id:1'
    ms.givenName = s.givenName
    ms.surname = s.surname
    
    ms.accountExpires = sdf.parse(s.accountExpires)
    
    ms.group = group
    ms.organization = group.organization
    
    if(!ms.validate()) {
        ms.errors.each {println it}
        println "DID NOT IMPORT $ms to group [${group.id}]${group.name} and organization [${group.organization.id}]${group.organization.displayName} but did continue on. Migrate manually if required."
        ecount++
        return
    }
    
    if(commit) {
      def managedSubject = managedSubjectService.register(ms, false)
      if(managedSubject.hasErrors()) {
          managedSubject.errors.each { println it }
          throw new RuntimeException("Error creating $managedSubject")
      }
    
      // Allow migration to occur
      def ds = new DeprecatedSubject(login:s.login, password:s.md5password)
      if(!ds.save()) {
          ds.errors.each {println it}
          throw new RuntimeException("Error creating DeprecatedSubject for $managedSubject")
      }

      s.storedid.each { sid ->
        sql.execute('INSERT INTO shibpid (creationDate, localEntity, localId, peerEntity, peerProvidedId, persistentId, principalName) VALUES (?,?,?,?,?,?,?)',
                    [sdf.parse(sid.creationDate), sid.localEntity, managedSubject.eptidKey, sid.peerEntity, sid.peerProvidedId ?:null, sid.persistentId, managedSubject.login])
      }
    }
    
    count++
    
    if(verbose)
      println "Created $ms and associated deprecated subject for migration in group [${group.id}]${group.name} and organization [${group.organization.id}]${group.organization.displayName}"
}

def assignAdministrators(def a, def group, boolean assignToOrg = false) {
    def subject = Subject.findWhere(principal:a.principal)
    if(!subject) {
      subject = new Subject(principal:a.principal, cn:"${a.givenName} ${a.surname}", email:a.email, enabled:true)
      
      if(commit){
        subject.save()
        if(subject.hasErrors()) {
          s.errors.each { println it}
          throw new RuntimeException("Error administrator $subject for $group")
        }
      }
    }
    
    def roleName
    if(assignToOrg)
        roleName = "organization:${group.organization.id}:administrators"
    else
        roleName = "group:${group.id}:administrators"
        
    if(commit) {
        def role = Role.findWhere(name:roleName)
        if (!role) {
          throw new RuntimeException("Error no administrative role found for $roleName")
        }
        
        role.addToSubjects(subject)
        subject.addToRoles(role)
        
        if(!role.save()) {
          throw new RuntimeException("Failed adding $subject to $role")  
        }
        if(!subject.save()) {
          throw new RuntimeException("Failed adding $role to $subject")  
        }
    }
}

// Lets do some real work
def unmigrated = []
Organization.withTransaction {
    
    orgs.each { org ->
        id = frMap.get(org.name) 
        if(id) {
            def organization = Organization.findWhere(frID: id)
            def group = organization.groups[0]    // Default Groups populated via by FR import
            
            if(verbose)
              println "Processing '${org.name}' as VHO organisation [${organization.id}]${organization.displayName}. Inserting top level subjects into group [${group.id}]${group.name}"
            
            org.subjects.each { s ->
              createManagedSubject(s, group) 
            }
            
            org.administrators.each { a ->
              assignAdministrators(a, group, true)
            }
            
            org.groups.each { g ->
                def groupInstance = Group.findWhere(organization: organization, name:g.name)

                if(!groupInstance) {
                  groupInstance = new Group()

                  groupInstance.name = g.name
                  groupInstance.description = g.description
                  groupInstance.organization = organization
                  organization.addToGroups(groupInstance)
                  
                  if(commit) {
                    if (!groupInstance.save(flush:true)) {
                        groupInstance.errors.each { println it }
                        throw new RuntimeException("Failed adding $groupInstance to $organization") 
                    }
                  }
                  
                  def groupRole = new Role(name:"group:${groupInstance.id}:administrators", description: "Administrators for the Group ${groupInstance.name} of Organization ${groupInstance.organization.displayName}")
                  def groupPermission = new Permission(type: Permission.wildcardPerm, target: "app:manage:organization:${groupInstance.organization.id}:group:${groupInstance.id}:*", role:groupRole)
                  groupRole.addToPermissions(groupPermission)

                  if(commit) {
                    if(!groupRole.save(flush:true)) {
                       throw new RuntimeException("Failed adding $groupRole for admin rights to $groupInsteance")
                    }
                  }
                  
                  if(verbose)
                    println "Created group [${groupInstance.id}]${groupInstance.name} in organisation [${organization.id}]${organization.displayName} with administrative role [${groupRole.id}]${groupRole.name}"
                }

                g.subjects.each { s ->
                   createManagedSubject(s, groupInstance) 
                }
                
                g.administrators.each { a ->
                  assignAdministrators(a, groupInstance)
                }
            }
        } else {
            unmigrated.add(org)
        }
    }
    
    int ucount = 0
    unmigrated.each { org ->
        ucount = ucount + org.count
        org.groups.each { g ->
            ucount = ucount + g.count
        }
    }
    
    println "A total of $count VHO accounts where imported succesfully."
    println "A total of $ecount VHO accounts where not imported succesfully but should have been."
    println "A total of $ucount VHO accounts where not imported due to no VHO top level group to FR org mapping being available."
    println "Overall this script has processed ${count+ecount+ucount} subject records which should equal the number exported from VHO."

    if(!commit)
      println "\n---RUN IN NON COMMIT MODE, NOTHING STORED IN DATABASE---\n"
    
}

println "--import end--\n"
true

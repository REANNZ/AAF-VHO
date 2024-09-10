package aaf.base.identity

import grails.util.Holders
import org.springframework.context.i18n.LocaleContextHolder
import aaf.base.admin.EmailTemplate

class RoleService {
  private final String TOKEN_EMAIL_SUBJECT ='branding.email.role.invitation'

  def messageSource
  
  /**
   * Creates a new role.
   *
   * @param name Name to assign to group
   * @param description Description to assign to group
   * @param protect Boolean indicating if this is a protected group (True disables modification in UI)
   *
   * @throws RuntimeException When internal state requires transaction rollback
   */
  public Role createRole(String name, String description, boolean protect) {
    def role = new Role()
    role.name = name
    role.description = description
    role.protect = protect

    if(!role.validate()) {
      log.debug("Supplied values for new role are invalid")
      role.errors.each {
        log.debug it
      }
      return role
    }

    def savedRole = role.save()
    if (savedRole) {
      log.info("Created role [$role.id]$role.name")
      return savedRole
    }

    log.error("Error creating new group")
    role.errors.each {
      log.error it
    }

    throw new RuntimeException("Error creating new role, object persistance failed")
  }

  /**
   * Deletes an exisiting Role.
   *
   * @param role The role instance to be deleted
   *
   * @throws RuntimeException When internal state requires transaction rollback
   */
  public void deleteRole(Role role) {

    // Remove all subjects from this role
    def subjects = []
    subjects.addAll(role.subjects)
    subjects.each {
      it.removeFromRoles(role)

      if (!it.save()) {
        log.error("Error updating $it to remove $role")
        it.errors.each {err ->
          log.error err
        }

        throw new RuntimeException("Error updating $it to remove $role")
      }
    }

    role.delete()
    log.info("Deleted role [$role.id]$role.name")
  }

  /**
   * Updates an existing role.
   *
   * @param role The role to be updated.
   *
   * @throws RuntimeException When internal state requires transaction rollback
   */
  public Role updateRole(Role role) {

    def updatedRole = role.save()
    if (updatedRole) {
      log.info("Updated role [$role.id]$role.name")
      return updatedRole
    }

    log.error("Error updating role [$role.id]$role.name")
    role.errors.each {err ->
      log.error err
    }
    throw new RuntimeException("Error updating $role")
  }

  /**
   * Assigns a role to a subject.
   *
   * @param subject The subject whole the referenced role should be assigned to
   * @param role The role to be assigned
   *
   * @throws RuntimeException When internal state requires transaction rollback
   */
  public void addMember(Subject subject, Role role) {
    role.addToSubjects(subject)
    subject.addToRoles(role)

    if (!role.save()) {
      log.error("Error updating $role to add $subject")

      role.errors.each {
        log.error(it)
      }

      throw new RuntimeException("Error updating $role to add $subject")
    }
 
    if (!subject.save()) {
      log.error("Error updating $subject when adding to $role")

      subject.errors.each {
        log.error(it)
      }

      throw new RuntimeException("Error updating $subject when adding to $role")
    }

    log.info("Successfully added $role to $subject")
  }

  /**
   * Removes a role from a subject.
   *
   * @param subject The subject whole the referenced role should be removed from
   * @param role The role to be assigned
   * 
   * @throws RuntimeException When internal state requires transaction rollback
   */
  public void deleteMember(Subject subject, Role role) {
    role.removeFromSubjects(subject)
    subject.removeFromRoles(role)

    if (!role.save()) {
      log.error("Error updating $role to delete $subject")

      role.errors.each {
        log.error(it)
      }

      throw new RuntimeException("Error updating $role to delete $subject")
    }

    if (!subject.save()) {
      log.error("Error updating $subject to delete from $role")
      subject.errors.each {
        log.error(it)
      }

      throw new RuntimeException("Error updating $subject to delete from $role")
    }

    log.info("Successfully removed $subject from $role")
  }

  public void sendInvitation(String targetName, String emailAddress, String redirectTo, Role role) {
    def emailManagerService = Holders.applicationContext.getBean("emailManagerService")
    def emailSubject = messageSource.getMessage(TOKEN_EMAIL_SUBJECT, [] as Object[], TOKEN_EMAIL_SUBJECT, LocaleContextHolder.locale)
    def emailTemplate = EmailTemplate.findWhere(name:"role_invitation")

    if(!emailTemplate) {
      throw new RuntimeException("Email template for inviting new administrators 'role_invitation' does not exist")  // Rollback transaction
    }

    def invitation = new RoleInvitation(role:role, redirectTo: redirectTo)
    if(!invitation.save()) {
      log.error "Failed to create invitation code for $role aborting"
      invitation.errors.each {
        log.warn it
      }
      throw new RuntimeException("Failed to create invitation code for $role aborting")  // Rollback transaction
    }
    emailManagerService.send(emailAddress, emailSubject, emailTemplate, [targetName:targetName, invitee:subject, invitation:invitation]) 
  }

  public boolean finalizeInvitation(RoleInvitation invitation) {
    if(invitation.utilized) {
      log.error "The presented invitation $invitation by $subject has already been used"
      return false
    }

    addMember(subject, invitation.role)
    invitation.utilized = true

    if(!invitation.save()) {
      log.error "Failed to set invitation $invitation to utilized aborting"
      throw new RuntimeException("Failed to set invitation $invitation to utilized")
    }

    log.info ("Added $subject to ${invitation.role} via successful invitation code presentation")
    true
  }
}

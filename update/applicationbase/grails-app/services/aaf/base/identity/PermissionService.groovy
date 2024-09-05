package aaf.base.identity

class PermissionService {

  boolean transactional = true

  /**
   * Assigns a permission object to an owner and performs checks to ensure permission is correctly applied
   *
   * @param permission The populated permission object to persist
   * @param owner The Subject or Role that will own the permission
   *
   * @return A permission object. The saved object is all was successful or the permission object with error details if persistence fails.
   * 
   * @throws RuntimeException if an unrecoverable/unexpected error occurs (Rolls back transaction)
   */
  public Permission createPermission(Permission permission, def owner) {

    permission.owner = owner
    def savedPermission = permission.save()
    if (!savedPermission) {
      log.error("Unable to persist new permission")
      permission.errors.each {
        log.error(it)
      }

      throw new RuntimeException("Unable to persist new permission")
    }

    owner.addToPermissions(permission)
    def savedOwner = owner.save()

    if (!savedOwner) {
      log.error("Unable to add permission $savedPermission.id to owner $owner.id")
      owner.errors.each {
        log.error(it)
      }

      throw new RuntimeException("Unable to add permission $savedPermission.id to owner $owner.id")
    }

    log.info("Successfully added permission $savedPermission.id to owner $owner.id")
    return savedPermission
  }

  /**
   * Removes permission from owner and deletes reference in data repository.
   *
   * @permission The populated permission object to delete.
   *
   * @throws RuntimeException if an unrecoverable/unexpected error occurs (Rolls back transaction)
   */
  public void deletePermission(permission) {
    def owner = permission.owner

    owner.removeFromPermissions(permission)
    def savedOwner = owner.save()

    if (!savedOwner) {
      log.error("Unable to remove permission $permission from $owner")
      owner.errors.each {
        log.error(it)
      }

      throw new RuntimeException("Unable to remove permission $permission from $owner")
    }

    permission.delete();
    log.info("Successfully removed permission $permission.id from owner $owner.id")
  }

}

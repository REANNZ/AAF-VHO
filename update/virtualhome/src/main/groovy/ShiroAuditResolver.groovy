import org.springframework.stereotype.Component
import grails.plugins.orm.auditable.resolvers.DefaultAuditRequestResolver

// Used in place of the old actorClosure from Config.groovy
@Component('auditRequestResolver')
class ShiroAuditResolver extends DefaultAuditRequestResolver {
    @Override
    String getCurrentActor() {
        org.apache.shiro.SecurityUtils.getSubject()?.getPrincipal()
    }
}

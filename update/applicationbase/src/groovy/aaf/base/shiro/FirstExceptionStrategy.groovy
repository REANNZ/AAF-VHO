package aaf.base.shiro

import org.apache.shiro.authc.*
import org.apache.shiro.authc.pam.FirstSuccessfulStrategy
import org.apache.shiro.realm.Realm

/**
 * {@link org.apache.shiro.authc.pam.AuthenticationStrategy} implementation that throws the first exception it gets
 * and ignores all subsequent realms. If there is no exceptions it works as the {@link FirstSuccessfulStrategy}
 *
 * Must implement ONLY ONE Realm per Token type.
 *
 */

public class FirstExceptionStrategy extends FirstSuccessfulStrategy {

    @Override
    public AuthenticationInfo afterAttempt(Realm realm, AuthenticationToken token, AuthenticationInfo singleRealmInfo, AuthenticationInfo aggregateInfo, Throwable t) throws AuthenticationException {
        if ((t != null) && (t instanceof AuthenticationException)) throw (AuthenticationException) t;
        return super.afterAttempt(realm, token, singleRealmInfo, aggregateInfo, t);
    }

}

package aaf.vhr

import java.util.Date

class AuthnTuple {
    String username
    Date authnInstant
    boolean mfa

    AuthnTuple(String _username, Date _authnInstant, boolean _mfa ) {
        username = _username
        authnInstant = _authnInstant
        mfa = _mfa
    }

}

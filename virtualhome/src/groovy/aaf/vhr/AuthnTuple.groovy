package aaf.vhr

import java.util.Date

class AuthnTuple {
    String username
    Date authnInstant

    AuthnTuple(String _username, Date _authnInstant) {
        username = _username
        authnInstant = _authnInstant
    }

}

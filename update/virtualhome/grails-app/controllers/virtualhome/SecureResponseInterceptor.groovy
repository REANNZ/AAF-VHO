package virtualhome

import java.util.Date
import java.util.TimeZone
import java.text.SimpleDateFormat

class SecureResponseInterceptor {

    String DATE_FORMAT = "EEE, d MMM yyyy HH:mm:ss z"

    SecureResponseInterceptor() {
        matchAll()
    }

    boolean before() { true }

    boolean after() {
        Date responseDate = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);
        sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
        String responseDateHeader = sdf.format(responseDate);

        // We do everything over SSL so prevent caching
        header("Expires", "Tue, 03 Jul 2001 06:00:00 GMT");
        header("Last-Modified", responseDateHeader);
        response.setHeader("Cache-Control", "no-store, no-cache, must-revalidate, max-age=0");
        response.setHeader("Cache-Control", "post-check=0, pre-check=0");
        response.setHeader("Pragma", "no-cache");

        //OWASP - prevent click jacking
        response.addHeader("X-FRAME-OPTIONS", "DENY")
    }

    void afterView() {
        // no-op
    }
}

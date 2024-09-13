package aaf.base.util.http;

import org.apache.commons.io.IOUtils;

import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.ServletInputStream;
import java.io.*;
import java.util.concurrent.atomic.AtomicBoolean;

// This is adapted from a helpful answer at 
// http://stackoverflow.com/questions/1046721/accessing-the-raw-body-of-a-put-or-post-request
// Solves issues with API needing raw access to request body

public class MultiReadHttpServletRequest extends HttpServletRequestWrapper {

  private byte[] body;

  public MultiReadHttpServletRequest(HttpServletRequest httpServletRequest) throws IOException {
    super(httpServletRequest);

    InputStream is = super.getInputStream();
    body = IOUtils.toByteArray(is);
  }

  @Override
  public ServletInputStream getInputStream() throws IOException {
    return new ServletInputStreamImpl(new ByteArrayInputStream(body));
  }

  @Override
  public BufferedReader getReader() throws IOException {
    String enc = getCharacterEncoding();
    if(enc == null) enc = "UTF-8";
    
    return new BufferedReader(new InputStreamReader(getInputStream(), enc));
  }

  private class ServletInputStreamImpl extends ServletInputStream {

    private InputStream is;

    public ServletInputStreamImpl(InputStream is) {
      this.is = is;
    }

    public int read() throws IOException {
      return is.read();
    }

    public boolean markSupported() {
      return false;
    }

    public synchronized void mark(int i) {
      throw new RuntimeException(new IOException("mark/reset not supported"));
    }

    public synchronized void reset() throws IOException {
      throw new IOException("mark/reset not supported");
    }
  }

}

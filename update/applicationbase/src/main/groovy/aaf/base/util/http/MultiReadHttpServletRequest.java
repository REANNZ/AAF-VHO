package aaf.base.util.http;

import org.apache.commons.io.IOUtils;

import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.ServletInputStream;
import javax.servlet.ReadListener;
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
    private AtomicBoolean readFinished = new AtomicBoolean(false);

    public ServletInputStreamImpl(InputStream is) {
      this.is = is;
    }

    public int read() throws IOException {
      int result = is.read();
      readFinished.set(-1 == result);
      return result;
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

    //The following methods are required as part of the ServletInputStream but were not implemented in the original project.

    // Returns true when all the data from the stream has been read else it returns false.
    public boolean isFinished() {
      return readFinished.get();
    }

    // Returns true if data can be read without blocking else returns false.
    public boolean isReady() {
      try {
        return is.available() > 0;
      }
      catch (IOException ioe) {
        return false;
      }
    }

    // Instructs the ServletInputStream to invoke the provided ReadListener when it is possible to read
    public void setReadListener (ReadListener readListener) {
      throw new UnsupportedOperationException("Non-blocking IO is not supported.");
    }
  }

}

package com.example.bankingapi.config;

import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;

import java.io.*;
import java.nio.charset.StandardCharsets;

//  This class wraps the original request to allow reading the body multiple times and allows controllers read,
// or saves/caches req data and gives controllers, request bodies are read once when a req is made
public class MultipleReader extends HttpServletRequestWrapper {

    private byte[] cachedBody;

    public MultipleReader(HttpServletRequest request) throws IOException {
        super(request);
        InputStream requestInputStream = request.getInputStream();

        // Read ALL the bytes from the stream and store them in cachedBody
        // This is done ONCE when the wrapper is created
        this.cachedBody = requestInputStream.readAllBytes();
    }

    // Override the getInputStream method
    // When Spring or your filter calls request.getInputStream(), this method runs
    @Override
    public ServletInputStream getInputStream() {
        // Return a NEW stream that reads from cached bytes
        // This allows multiple reads of the same data
        return new CachedBodyServletInputStream(this.cachedBody);
    }

    @Override
    public BufferedReader getReader() {
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(this.cachedBody);
        // Wrap it in a BufferedReader so it can be read as text
        return new BufferedReader(new InputStreamReader(byteArrayInputStream, StandardCharsets.UTF_8));
    }

    public String getBody() {
        return new String(this.cachedBody, StandardCharsets.UTF_8);
    }

    private static class CachedBodyServletInputStream extends ServletInputStream {
        private final ByteArrayInputStream buffer;

        public CachedBodyServletInputStream(byte[] contents) {
            // Create a stream that reads from the byte array
            this.buffer = new ByteArrayInputStream(contents);
        }

        // read the stream
        @Override
        public int read() {
            return buffer.read();
        }

        @Override
        public boolean isFinished() {
            return buffer.available() == 0;
        }

        // Check if the stream is ready to be read
        @Override
        public boolean isReady() {
            return true;
        }

        // for async reading - not supported
        @Override
        public void setReadListener(ReadListener listener) {
            throw new UnsupportedOperationException();
        }
    }
}

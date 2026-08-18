// buggy code
    public int read(byte b[], int offset, int len) throws IOException {
        if (b == null) {
            throw new NullPointerException();
        } else if (offset < 0 || len < 0) {
            throw new IndexOutOfBoundsException();
        } else if (offset > b.length || offset + len > b.length) {
            throw new IndexOutOfBoundsException();
        } else if (len == 0) {
            return 0;
        } else {
            /*
             Rationale for while-loop on (readLen == 0):
             -----
             Base64.readResults() usually returns > 0 or EOF (-1).  In the
             rare case where it returns 0, we just keep trying.

             This is essentially an undocumented contract for InputStream
             implementors that want their code to work properly with
             java.io.InputStreamReader, since the latter hates it when
             InputStream.read(byte[]) returns a zero.  Unfortunately our
             readResults() call must return 0 if a large amount of the data
             being decoded was non-base64, so this while-loop enables proper
             interop with InputStreamReader for that scenario.
             -----
             This is a fix for CODEC-101
            */
                if (!base64.hasData()) {
                    byte[] buf = new byte[doEncode ? 4096 : 8192];
                    int c = in.read(buf);
                    // A little optimization to avoid System.arraycopy()
                    // when possible.
                    if (c > 0 && b.length == len) {
                        base64.setInitialBuffer(b, offset, len);
                    }
                    if (doEncode) {
                        base64.encode(buf, 0, c);
                    } else {
                        base64.decode(buf, 0, c);
                    }
                }
            return base64.readResults(b, offset, len);
        }
    }

// relevant test
// org.apache.commons.codec.binary.Base64InputStreamTest::testCodec101
    public void testCodec101() throws Exception {
        byte[] codec101 = StringUtils.getBytesUtf8(Base64TestData.CODEC_101_MULTIPLE_OF_3);
        ByteArrayInputStream bais = new ByteArrayInputStream(codec101);
        Base64InputStream in = new Base64InputStream(bais);
        byte[] result = new byte[8192];
        int c = in.read(result);
        assertTrue("Codec101: First read successful [c=" + c + "]", c > 0);

        c = in.read(result);
        assertTrue("Codec101: Second read should report end-of-stream [c=" + c + "]", c < 0);
    }

// org.apache.commons.codec.binary.Base64InputStreamTest::testCodec98NPE
    public void testCodec98NPE() throws Exception {
        byte[] codec98 = StringUtils.getBytesUtf8(Base64TestData.CODEC_98_NPE);
        ByteArrayInputStream data = new ByteArrayInputStream(codec98);
        Base64InputStream stream = new Base64InputStream(data);

        
        byte[] decodedBytes = Base64TestData.streamToBytes(stream, new byte[1024]);

        String decoded = StringUtils.newStringUtf8(decodedBytes);
        assertEquals(
            "codec-98 NPE Base64InputStream", Base64TestData.CODEC_98_NPE_DECODED, decoded
        );
    }

// org.apache.commons.codec.binary.Base64InputStreamTest::testBase64EmptyInputStreamMimeChuckSize
    public void testBase64EmptyInputStreamMimeChuckSize() throws Exception {
        testBase64EmptyInputStream(Base64.MIME_CHUNK_SIZE);
    }

// org.apache.commons.codec.binary.Base64InputStreamTest::testBase64EmptyInputStreamPemChuckSize
    public void testBase64EmptyInputStreamPemChuckSize() throws Exception {
        testBase64EmptyInputStream(Base64.PEM_CHUNK_SIZE);
    }

// org.apache.commons.codec.binary.Base64InputStreamTest::testBase64InputStreamByChunk
    public void testBase64InputStreamByChunk() throws Exception {
        
        byte[] encoded = StringUtils.getBytesUtf8("SGVsbG8gV29ybGQ=\r\n");
        byte[] decoded = StringUtils.getBytesUtf8(STRING_FIXTURE);
        testByChunk(encoded, decoded, Base64.MIME_CHUNK_SIZE, CRLF);

        
        encoded = StringUtils.getBytesUtf8("AA==\r\n");
        decoded = new byte[]{(byte) 0};
        testByChunk(encoded, decoded, Base64.MIME_CHUNK_SIZE, CRLF);

        
        encoded = StringUtils.getBytesUtf8(Base64TestData.ENCODED_64_CHARS_PER_LINE);
        decoded = Base64TestData.DECODED;
        testByChunk(encoded, decoded, Base64.PEM_CHUNK_SIZE, LF);

        
        String singleLine = Base64TestData.ENCODED_64_CHARS_PER_LINE.replaceAll("\n", "");
        encoded = StringUtils.getBytesUtf8(singleLine);
        decoded = Base64TestData.DECODED;
        testByChunk(encoded, decoded, 0, LF);

        
        for (int i = 0; i <= 150; i++) {
            byte[][] randomData = Base64TestData.randomData(i, false);
            encoded = randomData[1];
            decoded = randomData[0];
            testByChunk(encoded, decoded, 0, LF);
        }
    }

// org.apache.commons.codec.binary.Base64InputStreamTest::testBase64InputStreamByteByByte
    public void testBase64InputStreamByteByByte() throws Exception {
        
        byte[] encoded = StringUtils.getBytesUtf8("SGVsbG8gV29ybGQ=\r\n");
        byte[] decoded = StringUtils.getBytesUtf8(STRING_FIXTURE);
        testByteByByte(encoded, decoded, Base64.MIME_CHUNK_SIZE, CRLF);

        
        encoded = StringUtils.getBytesUtf8("AA==\r\n");
        decoded = new byte[]{(byte) 0};
        testByteByByte(encoded, decoded, Base64.MIME_CHUNK_SIZE, CRLF);

        
        encoded = StringUtils.getBytesUtf8(Base64TestData.ENCODED_64_CHARS_PER_LINE);
        decoded = Base64TestData.DECODED;
        testByteByByte(encoded, decoded, Base64.PEM_CHUNK_SIZE, LF);

        
        String singleLine = Base64TestData.ENCODED_64_CHARS_PER_LINE.replaceAll("\n", "");
        encoded = StringUtils.getBytesUtf8(singleLine);
        decoded = Base64TestData.DECODED;
        testByteByByte(encoded, decoded, 0, LF);

        
        for (int i = 0; i <= 150; i++) {
            byte[][] randomData = Base64TestData.randomData(i, false);
            encoded = randomData[1];
            decoded = randomData[0];
            testByteByByte(encoded, decoded, 0, LF);
        }
    }

// org.apache.commons.codec.binary.Base64InputStreamTest::testMarkSupported
    public void testMarkSupported() throws Exception {
        byte[] decoded = StringUtils.getBytesUtf8(STRING_FIXTURE);
        ByteArrayInputStream bin = new ByteArrayInputStream(decoded);
        Base64InputStream in = new Base64InputStream(bin, true, 4, new byte[]{0, 0, 0});
        
        assertFalse("Base64InputStream.markSupported() is false", in.markSupported());
    }

// org.apache.commons.codec.binary.Base64InputStreamTest::testRead0
    public void testRead0() throws Exception {
        byte[] decoded = StringUtils.getBytesUtf8(STRING_FIXTURE);
        byte[] buf = new byte[1024];
        int bytesRead = 0;
        ByteArrayInputStream bin = new ByteArrayInputStream(decoded);
        Base64InputStream in = new Base64InputStream(bin, true, 4, new byte[]{0, 0, 0});
        bytesRead = in.read(buf, 0, 0);
        assertEquals("Base64InputStream.read(buf, 0, 0) returns 0", 0, bytesRead);
    }

// org.apache.commons.codec.binary.Base64InputStreamTest::testReadNull
    public void testReadNull() throws Exception {
        byte[] decoded = StringUtils.getBytesUtf8(STRING_FIXTURE);
        ByteArrayInputStream bin = new ByteArrayInputStream(decoded);
        Base64InputStream in = new Base64InputStream(bin, true, 4, new byte[]{0, 0, 0});
        try {
            in.read(null, 0, 0);
            fail("Base64InputStream.read(null, 0, 0) to throw a NullPointerException");
        } catch (NullPointerException e) {
            
        }
    }

// org.apache.commons.codec.binary.Base64InputStreamTest::testReadOutOfBounds
    public void testReadOutOfBounds() throws Exception {
        byte[] decoded = StringUtils.getBytesUtf8(STRING_FIXTURE);
        byte[] buf = new byte[1024];
        ByteArrayInputStream bin = new ByteArrayInputStream(decoded);
        Base64InputStream in = new Base64InputStream(bin, true, 4, new byte[]{0, 0, 0});

        try {
            in.read(buf, -1, 0);
            fail("Expected Base64InputStream.read(buf, -1, 0) to throw IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
            
        }

        try {
            in.read(buf, 0, -1);
            fail("Expected Base64InputStream.read(buf, 0, -1) to throw IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
            
        }

        try {
            in.read(buf, buf.length + 1, 0);
            fail("Base64InputStream.read(buf, buf.length + 1, 0) throws IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
            
        }

        try {
            in.read(buf, buf.length - 1, 2);
            fail("Base64InputStream.read(buf, buf.length - 1, 2) throws IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
            
        }
    }

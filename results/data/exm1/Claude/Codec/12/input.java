// buggy code
    public boolean markSupported() {
        return false; // not an easy job to support marks
    }

// relevant test
// org.apache.commons.codec.binary.Base32InputStreamTest::testCodec130
    public void testCodec130() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        Base32OutputStream base32os = new Base32OutputStream(bos);

        base32os.write(StringUtils.getBytesUtf8(STRING_FIXTURE));
        base32os.close();

        ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
        Base32InputStream ins = new Base32InputStream(bis);

        
        ins.skip(1);
        byte[] decodedBytes = Base32TestData.streamToBytes(ins, new byte[64]);
        String str = StringUtils.newStringUtf8(decodedBytes);

        assertEquals(STRING_FIXTURE.substring(1), str);
    }

// org.apache.commons.codec.binary.Base32InputStreamTest::testCodec105
    public void testCodec105() throws IOException {
        Base32InputStream in = new Base32InputStream(new Codec105ErrorInputStream(), true, 0, null);
        try {
            for (int i = 0; i < 5; i++) {
                in.read();
            }
        } finally {
            in.close();
        }
    }

// org.apache.commons.codec.binary.Base32InputStreamTest::testAvailable
    public void testAvailable() throws Throwable {
        InputStream ins = new ByteArrayInputStream(StringUtils.getBytesIso8859_1(ENCODED_FOO));
        Base32InputStream b32stream = new Base32InputStream(ins);
        assertEquals(1, b32stream.available());
        assertEquals(3, b32stream.skip(10));
        
        assertEquals(0, b32stream.available());
        assertEquals(-1, b32stream.read());
        assertEquals(-1, b32stream.read());
        assertEquals(0, b32stream.available());
    }

// org.apache.commons.codec.binary.Base32InputStreamTest::testBase32EmptyInputStreamMimeChuckSize
    public void testBase32EmptyInputStreamMimeChuckSize() throws Exception {
        testBase32EmptyInputStream(BaseNCodec.MIME_CHUNK_SIZE);
    }

// org.apache.commons.codec.binary.Base32InputStreamTest::testBase32EmptyInputStreamPemChuckSize
    public void testBase32EmptyInputStreamPemChuckSize() throws Exception {
        testBase32EmptyInputStream(BaseNCodec.PEM_CHUNK_SIZE);
    }

// org.apache.commons.codec.binary.Base32InputStreamTest::testBase32InputStreamByChunk
    public void testBase32InputStreamByChunk() throws Exception {
        
        byte[] encoded = StringUtils.getBytesUtf8(Base32TestData.BASE32_FIXTURE);
        byte[] decoded = StringUtils.getBytesUtf8(Base32TestData.STRING_FIXTURE);
        testByChunk(encoded, decoded, BaseNCodec.MIME_CHUNK_SIZE, CRLF);

        
        encoded = StringUtils.getBytesUtf8("AA======\r\n");
        decoded = new byte[] { (byte) 0 };
        testByChunk(encoded, decoded, BaseNCodec.MIME_CHUNK_SIZE, CRLF);

        
        
        
        
        
        
        
        
        
        

        
        BaseNCodec codec = new Base32();
        for (int i = 0; i <= 150; i++) {
            byte[][] randomData = Base32TestData.randomData(codec, i);
            encoded = randomData[1];
            decoded = randomData[0];
            testByChunk(encoded, decoded, 0, LF);
        }
    }

// org.apache.commons.codec.binary.Base32InputStreamTest::testBase32InputStreamByteByByte
    public void testBase32InputStreamByteByByte() throws Exception {
        
        byte[] encoded = StringUtils.getBytesUtf8(Base32TestData.BASE32_FIXTURE);
        byte[] decoded = StringUtils.getBytesUtf8(Base32TestData.STRING_FIXTURE);
        testByteByByte(encoded, decoded, BaseNCodec.MIME_CHUNK_SIZE, CRLF);

        
        encoded = StringUtils.getBytesUtf8("AA======\r\n");
        decoded = new byte[] { (byte) 0 };
        testByteByByte(encoded, decoded, BaseNCodec.MIME_CHUNK_SIZE, CRLF);

        
        
        
        
        

        
        BaseNCodec codec = new Base32();
        for (int i = 0; i <= 150; i++) {
            byte[][] randomData = Base32TestData.randomData(codec, i);
            encoded = randomData[1];
            decoded = randomData[0];
            testByteByByte(encoded, decoded, 0, LF);
        }
    }

// org.apache.commons.codec.binary.Base32InputStreamTest::testMarkSupported
    public void testMarkSupported() throws Exception {
        byte[] decoded = StringUtils.getBytesUtf8(Base32TestData.STRING_FIXTURE);
        ByteArrayInputStream bin = new ByteArrayInputStream(decoded);
        Base32InputStream in = new Base32InputStream(bin, true, 4, new byte[] { 0, 0, 0 });
        
        assertFalse("Base32InputStream.markSupported() is false", in.markSupported());
    }

// org.apache.commons.codec.binary.Base32InputStreamTest::testRead0
    public void testRead0() throws Exception {
        byte[] decoded = StringUtils.getBytesUtf8(Base32TestData.STRING_FIXTURE);
        byte[] buf = new byte[1024];
        int bytesRead = 0;
        ByteArrayInputStream bin = new ByteArrayInputStream(decoded);
        Base32InputStream in = new Base32InputStream(bin, true, 4, new byte[] { 0, 0, 0 });
        bytesRead = in.read(buf, 0, 0);
        assertEquals("Base32InputStream.read(buf, 0, 0) returns 0", 0, bytesRead);
    }

// org.apache.commons.codec.binary.Base32InputStreamTest::testReadNull
    public void testReadNull() throws Exception {
        byte[] decoded = StringUtils.getBytesUtf8(Base32TestData.STRING_FIXTURE);
        ByteArrayInputStream bin = new ByteArrayInputStream(decoded);
        Base32InputStream in = new Base32InputStream(bin, true, 4, new byte[] { 0, 0, 0 });
        try {
            in.read(null, 0, 0);
            fail("Base32InputStream.read(null, 0, 0) to throw a NullPointerException");
        } catch (NullPointerException e) {
            
        }
    }

// org.apache.commons.codec.binary.Base32InputStreamTest::testReadOutOfBounds
    public void testReadOutOfBounds() throws Exception {
        byte[] decoded = StringUtils.getBytesUtf8(Base32TestData.STRING_FIXTURE);
        byte[] buf = new byte[1024];
        ByteArrayInputStream bin = new ByteArrayInputStream(decoded);
        Base32InputStream in = new Base32InputStream(bin, true, 4, new byte[] { 0, 0, 0 });

        try {
            in.read(buf, -1, 0);
            fail("Expected Base32InputStream.read(buf, -1, 0) to throw IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
            
        }

        try {
            in.read(buf, 0, -1);
            fail("Expected Base32InputStream.read(buf, 0, -1) to throw IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
            
        }

        try {
            in.read(buf, buf.length + 1, 0);
            fail("Base32InputStream.read(buf, buf.length + 1, 0) throws IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
            
        }

        try {
            in.read(buf, buf.length - 1, 2);
            fail("Base32InputStream.read(buf, buf.length - 1, 2) throws IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
            
        }
    }

// org.apache.commons.codec.binary.Base32InputStreamTest::testSkipNone
    public void testSkipNone() throws Throwable {
        InputStream ins = new ByteArrayInputStream(StringUtils.getBytesIso8859_1(ENCODED_FOO));
        Base32InputStream b32stream = new Base32InputStream(ins);
        byte[] actualBytes = new byte[6];
        assertEquals(0, b32stream.skip(0));
        b32stream.read(actualBytes, 0, actualBytes.length);
        assertArrayEquals(actualBytes, new byte[] { 102, 111, 111, 0, 0, 0 });
        
        assertEquals(-1, b32stream.read());
    }

// org.apache.commons.codec.binary.Base32InputStreamTest::testSkipBig
    public void testSkipBig() throws Throwable {
        InputStream ins = new ByteArrayInputStream(StringUtils.getBytesIso8859_1(ENCODED_FOO));
        Base32InputStream b32stream = new Base32InputStream(ins);
        assertEquals(3, b32stream.skip(1024));
        
        assertEquals(-1, b32stream.read());
        assertEquals(-1, b32stream.read());
    }

// org.apache.commons.codec.binary.Base32InputStreamTest::testSkipPastEnd
    public void testSkipPastEnd() throws Throwable {
        InputStream ins = new ByteArrayInputStream(StringUtils.getBytesIso8859_1(ENCODED_FOO));
        Base32InputStream b32stream = new Base32InputStream(ins);
        
        assertEquals(3, b32stream.skip(10));
        
        assertEquals(-1, b32stream.read());
        assertEquals(-1, b32stream.read());
    }

// org.apache.commons.codec.binary.Base32InputStreamTest::testSkipToEnd
    public void testSkipToEnd() throws Throwable {
        InputStream ins = new ByteArrayInputStream(StringUtils.getBytesIso8859_1(ENCODED_FOO));
        Base32InputStream b32stream = new Base32InputStream(ins);
        
        assertEquals(3, b32stream.skip(3));
        
        assertEquals(-1, b32stream.read());
        assertEquals(-1, b32stream.read());
    }

// org.apache.commons.codec.binary.Base32InputStreamTest::testSkipWrongArgument
    public void testSkipWrongArgument() throws Throwable {
        InputStream ins = new ByteArrayInputStream(StringUtils.getBytesIso8859_1(ENCODED_FOO));
        Base32InputStream b32stream = new Base32InputStream(ins);
        b32stream.skip(-10);
    }

// org.apache.commons.codec.binary.Base64InputStreamTest::testCodec130
    public void testCodec130() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        Base64OutputStream base64os = new Base64OutputStream(bos);

        base64os.write(StringUtils.getBytesUtf8(STRING_FIXTURE));
        base64os.close();

        ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
        Base64InputStream ins = new Base64InputStream(bis);

        
        ins.skip(1);
        byte[] decodedBytes = Base64TestData.streamToBytes(ins, new byte[64]);
        String str = StringUtils.newStringUtf8(decodedBytes);

        assertEquals(STRING_FIXTURE.substring(1), str);
    }

// org.apache.commons.codec.binary.Base64InputStreamTest::testCodec105
    public void testCodec105() throws IOException {
        Base64InputStream in = new Base64InputStream(new Codec105ErrorInputStream(), true, 0, null);
        try {
            for (int i = 0; i < 5; i++) {
                in.read();
            }
        } finally {
            in.close();
        }
    }

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

// org.apache.commons.codec.binary.Base64InputStreamTest::testInputStreamReader
    public void testInputStreamReader() throws Exception {
        byte[] codec101 = StringUtils.getBytesUtf8(Base64TestData.CODEC_101_MULTIPLE_OF_3);
        ByteArrayInputStream bais = new ByteArrayInputStream(codec101);
        Base64InputStream in = new Base64InputStream(bais);
        InputStreamReader isr = new InputStreamReader(in);
        BufferedReader br = new BufferedReader(isr);
        String line = br.readLine();
        assertNotNull("Codec101:  InputStreamReader works!", line);
    }

// org.apache.commons.codec.binary.Base64InputStreamTest::testCodec98NPE
    public void testCodec98NPE() throws Exception {
        byte[] codec98 = StringUtils.getBytesUtf8(Base64TestData.CODEC_98_NPE);
        ByteArrayInputStream data = new ByteArrayInputStream(codec98);
        Base64InputStream stream = new Base64InputStream(data);

        
        byte[] decodedBytes = Base64TestData.streamToBytes(stream, new byte[1024]);

        String decoded = StringUtils.newStringUtf8(decodedBytes);
        assertEquals("codec-98 NPE Base64InputStream", Base64TestData.CODEC_98_NPE_DECODED, decoded);
    }

// org.apache.commons.codec.binary.Base64InputStreamTest::testAvailable
    public void testAvailable() throws Throwable {
        InputStream ins = new ByteArrayInputStream(StringUtils.getBytesIso8859_1(ENCODED_B64));
        Base64InputStream b64stream = new Base64InputStream(ins);
        assertEquals(1, b64stream.available());
        assertEquals(6, b64stream.skip(10));
        
        assertEquals(0, b64stream.available());
        assertEquals(-1, b64stream.read());
        assertEquals(-1, b64stream.read());
        assertEquals(0, b64stream.available());
    }

// org.apache.commons.codec.binary.Base64InputStreamTest::testBase64EmptyInputStreamMimeChuckSize
    public void testBase64EmptyInputStreamMimeChuckSize() throws Exception {
        testBase64EmptyInputStream(BaseNCodec.MIME_CHUNK_SIZE);
    }

// org.apache.commons.codec.binary.Base64InputStreamTest::testBase64EmptyInputStreamPemChuckSize
    public void testBase64EmptyInputStreamPemChuckSize() throws Exception {
        testBase64EmptyInputStream(BaseNCodec.PEM_CHUNK_SIZE);
    }

// org.apache.commons.codec.binary.Base64InputStreamTest::testBase64InputStreamByChunk
    public void testBase64InputStreamByChunk() throws Exception {
        
        byte[] encoded = StringUtils.getBytesUtf8("SGVsbG8gV29ybGQ=\r\n");
        byte[] decoded = StringUtils.getBytesUtf8(STRING_FIXTURE);
        testByChunk(encoded, decoded, BaseNCodec.MIME_CHUNK_SIZE, CRLF);

        
        encoded = StringUtils.getBytesUtf8("AA==\r\n");
        decoded = new byte[] { (byte) 0 };
        testByChunk(encoded, decoded, BaseNCodec.MIME_CHUNK_SIZE, CRLF);

        
        encoded = StringUtils.getBytesUtf8(Base64TestData.ENCODED_64_CHARS_PER_LINE);
        decoded = Base64TestData.DECODED;
        testByChunk(encoded, decoded, BaseNCodec.PEM_CHUNK_SIZE, LF);

        
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
        testByteByByte(encoded, decoded, BaseNCodec.MIME_CHUNK_SIZE, CRLF);

        
        encoded = StringUtils.getBytesUtf8("AA==\r\n");
        decoded = new byte[] { (byte) 0 };
        testByteByByte(encoded, decoded, BaseNCodec.MIME_CHUNK_SIZE, CRLF);

        
        encoded = StringUtils.getBytesUtf8(Base64TestData.ENCODED_64_CHARS_PER_LINE);
        decoded = Base64TestData.DECODED;
        testByteByByte(encoded, decoded, BaseNCodec.PEM_CHUNK_SIZE, LF);

        
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
        Base64InputStream in = new Base64InputStream(bin, true, 4, new byte[] { 0, 0, 0 });
        
        assertFalse("Base64InputStream.markSupported() is false", in.markSupported());
    }

// org.apache.commons.codec.binary.Base64InputStreamTest::testRead0
    public void testRead0() throws Exception {
        byte[] decoded = StringUtils.getBytesUtf8(STRING_FIXTURE);
        byte[] buf = new byte[1024];
        int bytesRead = 0;
        ByteArrayInputStream bin = new ByteArrayInputStream(decoded);
        Base64InputStream in = new Base64InputStream(bin, true, 4, new byte[] { 0, 0, 0 });
        bytesRead = in.read(buf, 0, 0);
        assertEquals("Base64InputStream.read(buf, 0, 0) returns 0", 0, bytesRead);
    }

// org.apache.commons.codec.binary.Base64InputStreamTest::testReadNull
    public void testReadNull() throws Exception {
        byte[] decoded = StringUtils.getBytesUtf8(STRING_FIXTURE);
        ByteArrayInputStream bin = new ByteArrayInputStream(decoded);
        Base64InputStream in = new Base64InputStream(bin, true, 4, new byte[] { 0, 0, 0 });
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
        Base64InputStream in = new Base64InputStream(bin, true, 4, new byte[] { 0, 0, 0 });

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

// org.apache.commons.codec.binary.Base64InputStreamTest::testSkipBig
    public void testSkipBig() throws Throwable {
        InputStream ins = new ByteArrayInputStream(StringUtils.getBytesIso8859_1(ENCODED_B64));
        Base64InputStream b64stream = new Base64InputStream(ins);
        assertEquals(6, b64stream.skip(1024));
        
        assertEquals(-1, b64stream.read());
        assertEquals(-1, b64stream.read());
    }

// org.apache.commons.codec.binary.Base64InputStreamTest::testSkipNone
    public void testSkipNone() throws Throwable {
        InputStream ins = new ByteArrayInputStream(StringUtils.getBytesIso8859_1(ENCODED_B64));
        Base64InputStream b64stream = new Base64InputStream(ins);
        byte[] actualBytes = new byte[6];
        assertEquals(0, b64stream.skip(0));
        b64stream.read(actualBytes, 0, actualBytes.length);
        assertArrayEquals(actualBytes, new byte[] { 0, 0, 0, (byte) 255, (byte) 255, (byte) 255 });
        
        assertEquals(-1, b64stream.read());
    }

// org.apache.commons.codec.binary.Base64InputStreamTest::testSkipPastEnd
    public void testSkipPastEnd() throws Throwable {
        InputStream ins = new ByteArrayInputStream(StringUtils.getBytesIso8859_1(ENCODED_B64));
        Base64InputStream b64stream = new Base64InputStream(ins);
        
        assertEquals(6, b64stream.skip(10));
        
        assertEquals(-1, b64stream.read());
        assertEquals(-1, b64stream.read());
    }

// org.apache.commons.codec.binary.Base64InputStreamTest::testSkipToEnd
    public void testSkipToEnd() throws Throwable {
        InputStream ins = new ByteArrayInputStream(StringUtils.getBytesIso8859_1(ENCODED_B64));
        Base64InputStream b64stream = new Base64InputStream(ins);
        
        assertEquals(6, b64stream.skip(6));
        
        assertEquals(-1, b64stream.read());
        assertEquals(-1, b64stream.read());
    }

// org.apache.commons.codec.binary.Base64InputStreamTest::testSkipWrongArgument
    public void testSkipWrongArgument() throws Throwable {
        InputStream ins = new ByteArrayInputStream(StringUtils.getBytesIso8859_1(ENCODED_B64));
        Base64InputStream b64stream = new Base64InputStream(ins);
        b64stream.skip(-10);
    }

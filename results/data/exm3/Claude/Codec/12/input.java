// buggy function
    public boolean markSupported() {
        return false; // not an easy job to support marks
    }

// trigger testcase
// org/apache/commons/codec/binary/Base32InputStreamTest.java::testAvailable
@Test
    public void testAvailable() throws Throwable {
        InputStream ins = new ByteArrayInputStream(StringUtils.getBytesIso8859_1(ENCODED_FOO));
        Base32InputStream b32stream = new Base32InputStream(ins);
        assertEquals(1, b32stream.available());
        assertEquals(3, b32stream.skip(10));
        // End of stream reached
        assertEquals(0, b32stream.available());
        assertEquals(-1, b32stream.read());
        assertEquals(-1, b32stream.read());
        assertEquals(0, b32stream.available());
    }

// org/apache/commons/codec/binary/Base32InputStreamTest.java::testCodec130
@Test
    public void testCodec130() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        Base32OutputStream base32os = new Base32OutputStream(bos);

        base32os.write(StringUtils.getBytesUtf8(STRING_FIXTURE));
        base32os.close();

        ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
        Base32InputStream ins = new Base32InputStream(bis);

        // we skip the first character read from the reader
        ins.skip(1);
        byte[] decodedBytes = Base32TestData.streamToBytes(ins, new byte[64]);
        String str = StringUtils.newStringUtf8(decodedBytes);

        assertEquals(STRING_FIXTURE.substring(1), str);
    }

// org/apache/commons/codec/binary/Base32InputStreamTest.java::testSkipBig
@Test
    public void testSkipBig() throws Throwable {
        InputStream ins = new ByteArrayInputStream(StringUtils.getBytesIso8859_1(ENCODED_FOO));
        Base32InputStream b32stream = new Base32InputStream(ins);
        assertEquals(3, b32stream.skip(1024));
        // End of stream reached
        assertEquals(-1, b32stream.read());
        assertEquals(-1, b32stream.read());
    }

// org/apache/commons/codec/binary/Base32InputStreamTest.java::testSkipPastEnd
@Test
    public void testSkipPastEnd() throws Throwable {
        InputStream ins = new ByteArrayInputStream(StringUtils.getBytesIso8859_1(ENCODED_FOO));
        Base32InputStream b32stream = new Base32InputStream(ins);
        // due to CODEC-130, skip now skips correctly decoded characters rather than encoded
        assertEquals(3, b32stream.skip(10));
        // End of stream reached
        assertEquals(-1, b32stream.read());
        assertEquals(-1, b32stream.read());
    }

// org/apache/commons/codec/binary/Base32InputStreamTest.java::testSkipToEnd
@Test
    public void testSkipToEnd() throws Throwable {
        InputStream ins = new ByteArrayInputStream(StringUtils.getBytesIso8859_1(ENCODED_FOO));
        Base32InputStream b32stream = new Base32InputStream(ins);
        // due to CODEC-130, skip now skips correctly decoded characters rather than encoded
        assertEquals(3, b32stream.skip(3));
        // End of stream reached
        assertEquals(-1, b32stream.read());
        assertEquals(-1, b32stream.read());
    }

// org/apache/commons/codec/binary/Base32InputStreamTest.java::testSkipWrongArgument
public void testSkipWrongArgument() throws Throwable {
        InputStream ins = new ByteArrayInputStream(StringUtils.getBytesIso8859_1(ENCODED_FOO));
        Base32InputStream b32stream = new Base32InputStream(ins);
        b32stream.skip(-10);
    }

// org/apache/commons/codec/binary/Base64InputStreamTest.java::testAvailable
@Test
    public void testAvailable() throws Throwable {
        InputStream ins = new ByteArrayInputStream(StringUtils.getBytesIso8859_1(ENCODED_B64));
        Base64InputStream b64stream = new Base64InputStream(ins);
        assertEquals(1, b64stream.available());
        assertEquals(6, b64stream.skip(10));
        // End of stream reached
        assertEquals(0, b64stream.available());
        assertEquals(-1, b64stream.read());
        assertEquals(-1, b64stream.read());
        assertEquals(0, b64stream.available());
    }

// org/apache/commons/codec/binary/Base64InputStreamTest.java::testCodec130
@Test
    public void testCodec130() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        Base64OutputStream base64os = new Base64OutputStream(bos);

        base64os.write(StringUtils.getBytesUtf8(STRING_FIXTURE));
        base64os.close();

        ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
        Base64InputStream ins = new Base64InputStream(bis);

        // we skip the first character read from the reader
        ins.skip(1);
        byte[] decodedBytes = Base64TestData.streamToBytes(ins, new byte[64]);
        String str = StringUtils.newStringUtf8(decodedBytes);

        assertEquals(STRING_FIXTURE.substring(1), str);
    }

// org/apache/commons/codec/binary/Base64InputStreamTest.java::testSkipBig
@Test
    public void testSkipBig() throws Throwable {
        InputStream ins = new ByteArrayInputStream(StringUtils.getBytesIso8859_1(ENCODED_B64));
        Base64InputStream b64stream = new Base64InputStream(ins);
        assertEquals(6, b64stream.skip(1024));
        // End of stream reached
        assertEquals(-1, b64stream.read());
        assertEquals(-1, b64stream.read());
    }

// org/apache/commons/codec/binary/Base64InputStreamTest.java::testSkipPastEnd
@Test
    public void testSkipPastEnd() throws Throwable {
        InputStream ins = new ByteArrayInputStream(StringUtils.getBytesIso8859_1(ENCODED_B64));
        Base64InputStream b64stream = new Base64InputStream(ins);
        // due to CODEC-130, skip now skips correctly decoded characters rather than encoded
        assertEquals(6, b64stream.skip(10));
        // End of stream reached
        assertEquals(-1, b64stream.read());
        assertEquals(-1, b64stream.read());
    }

// org/apache/commons/codec/binary/Base64InputStreamTest.java::testSkipToEnd
@Test
    public void testSkipToEnd() throws Throwable {
        InputStream ins = new ByteArrayInputStream(StringUtils.getBytesIso8859_1(ENCODED_B64));
        Base64InputStream b64stream = new Base64InputStream(ins);
        // due to CODEC-130, skip now skips correctly decoded characters rather than encoded
        assertEquals(6, b64stream.skip(6));
        // End of stream reached
        assertEquals(-1, b64stream.read());
        assertEquals(-1, b64stream.read());
    }

// org/apache/commons/codec/binary/Base64InputStreamTest.java::testSkipWrongArgument
public void testSkipWrongArgument() throws Throwable {
        InputStream ins = new ByteArrayInputStream(StringUtils.getBytesIso8859_1(ENCODED_B64));
        Base64InputStream b64stream = new Base64InputStream(ins);
        b64stream.skip(-10);
    }

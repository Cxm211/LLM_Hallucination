// buggy code
    public static boolean equals(final CharSequence cs1, final CharSequence cs2) {
        if (cs1 == cs2) {
            return true;
        }
        if (cs1 == null || cs2 == null) {
            return false;
        }
        if (cs1 instanceof String && cs2 instanceof String) {
            return cs1.equals(cs2);
        }
        return CharSequenceUtils.regionMatches(cs1, false, 0, cs2, 0, Math.max(cs1.length(), cs2.length()));
    }

// relevant test
// org.apache.commons.codec.binary.Base32InputStreamTest::testCodec130
    public void testCodec130() throws IOException {
        final ByteArrayOutputStream bos = new ByteArrayOutputStream();
        final Base32OutputStream base32os = new Base32OutputStream(bos);

        base32os.write(StringUtils.getBytesUtf8(STRING_FIXTURE));
        base32os.close();

        final ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
        final Base32InputStream ins = new Base32InputStream(bis);

        
        ins.skip(1);
        final byte[] decodedBytes = Base32TestData.streamToBytes(ins, new byte[64]);
        final String str = StringUtils.newStringUtf8(decodedBytes);

        assertEquals(STRING_FIXTURE.substring(1), str);
    }

// org.apache.commons.codec.binary.Base32InputStreamTest::testCodec105
    public void testCodec105() throws IOException {
        final Base32InputStream in = new Base32InputStream(new Codec105ErrorInputStream(), true, 0, null);
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
        final InputStream ins = new ByteArrayInputStream(StringUtils.getBytesIso8859_1(ENCODED_FOO));
        final Base32InputStream b32stream = new Base32InputStream(ins);
        assertEquals(1, b32stream.available());
        assertEquals(3, b32stream.skip(10));
        
        assertEquals(0, b32stream.available());
        assertEquals(-1, b32stream.read());
        assertEquals(-1, b32stream.read());
        assertEquals(0, b32stream.available());
        b32stream.close();
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

        
        
        
        
        
        
        
        
        
        

        
        final BaseNCodec codec = new Base32();
        for (int i = 0; i <= 150; i++) {
            final byte[][] randomData = Base32TestData.randomData(codec, i);
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

        
        
        
        
        

        
        final BaseNCodec codec = new Base32();
        for (int i = 0; i <= 150; i++) {
            final byte[][] randomData = Base32TestData.randomData(codec, i);
            encoded = randomData[1];
            decoded = randomData[0];
            testByteByByte(encoded, decoded, 0, LF);
        }
    }

// org.apache.commons.codec.binary.Base32InputStreamTest::testMarkSupported
    public void testMarkSupported() throws Exception {
        final byte[] decoded = StringUtils.getBytesUtf8(Base32TestData.STRING_FIXTURE);
        final ByteArrayInputStream bin = new ByteArrayInputStream(decoded);
        final Base32InputStream in = new Base32InputStream(bin, true, 4, new byte[] { 0, 0, 0 });
        
        assertFalse("Base32InputStream.markSupported() is false", in.markSupported());
        in.close();
    }

// org.apache.commons.codec.binary.Base32InputStreamTest::testRead0
    public void testRead0() throws Exception {
        final byte[] decoded = StringUtils.getBytesUtf8(Base32TestData.STRING_FIXTURE);
        final byte[] buf = new byte[1024];
        int bytesRead = 0;
        final ByteArrayInputStream bin = new ByteArrayInputStream(decoded);
        final Base32InputStream in = new Base32InputStream(bin, true, 4, new byte[] { 0, 0, 0 });
        bytesRead = in.read(buf, 0, 0);
        assertEquals("Base32InputStream.read(buf, 0, 0) returns 0", 0, bytesRead);
        in.close();
    }

// org.apache.commons.codec.binary.Base32InputStreamTest::testReadNull
    public void testReadNull() throws Exception {
        final byte[] decoded = StringUtils.getBytesUtf8(Base32TestData.STRING_FIXTURE);
        final ByteArrayInputStream bin = new ByteArrayInputStream(decoded);
        final Base32InputStream in = new Base32InputStream(bin, true, 4, new byte[] { 0, 0, 0 });
        try {
            in.read(null, 0, 0);
            fail("Base32InputStream.read(null, 0, 0) to throw a NullPointerException");
        } catch (final NullPointerException e) {
            
        }
        in.close();
    }

// org.apache.commons.codec.binary.Base32InputStreamTest::testReadOutOfBounds
    public void testReadOutOfBounds() throws Exception {
        final byte[] decoded = StringUtils.getBytesUtf8(Base32TestData.STRING_FIXTURE);
        final byte[] buf = new byte[1024];
        final ByteArrayInputStream bin = new ByteArrayInputStream(decoded);
        final Base32InputStream in = new Base32InputStream(bin, true, 4, new byte[] { 0, 0, 0 });

        try {
            in.read(buf, -1, 0);
            fail("Expected Base32InputStream.read(buf, -1, 0) to throw IndexOutOfBoundsException");
        } catch (final IndexOutOfBoundsException e) {
            
        }

        try {
            in.read(buf, 0, -1);
            fail("Expected Base32InputStream.read(buf, 0, -1) to throw IndexOutOfBoundsException");
        } catch (final IndexOutOfBoundsException e) {
            
        }

        try {
            in.read(buf, buf.length + 1, 0);
            fail("Base32InputStream.read(buf, buf.length + 1, 0) throws IndexOutOfBoundsException");
        } catch (final IndexOutOfBoundsException e) {
            
        }

        try {
            in.read(buf, buf.length - 1, 2);
            fail("Base32InputStream.read(buf, buf.length - 1, 2) throws IndexOutOfBoundsException");
        } catch (final IndexOutOfBoundsException e) {
            
        }
        in.close();
    }

// org.apache.commons.codec.binary.Base32InputStreamTest::testSkipNone
    public void testSkipNone() throws Throwable {
        final InputStream ins = new ByteArrayInputStream(StringUtils.getBytesIso8859_1(ENCODED_FOO));
        final Base32InputStream b32stream = new Base32InputStream(ins);
        final byte[] actualBytes = new byte[6];
        assertEquals(0, b32stream.skip(0));
        b32stream.read(actualBytes, 0, actualBytes.length);
        assertArrayEquals(actualBytes, new byte[] { 102, 111, 111, 0, 0, 0 });
        
        assertEquals(-1, b32stream.read());
        b32stream.close();
    }

// org.apache.commons.codec.binary.Base32InputStreamTest::testSkipBig
    public void testSkipBig() throws Throwable {
        final InputStream ins = new ByteArrayInputStream(StringUtils.getBytesIso8859_1(ENCODED_FOO));
        final Base32InputStream b32stream = new Base32InputStream(ins);
        assertEquals(3, b32stream.skip(1024));
        
        assertEquals(-1, b32stream.read());
        assertEquals(-1, b32stream.read());
        b32stream.close();
    }

// org.apache.commons.codec.binary.Base32InputStreamTest::testSkipPastEnd
    public void testSkipPastEnd() throws Throwable {
        final InputStream ins = new ByteArrayInputStream(StringUtils.getBytesIso8859_1(ENCODED_FOO));
        final Base32InputStream b32stream = new Base32InputStream(ins);
        
        assertEquals(3, b32stream.skip(10));
        
        assertEquals(-1, b32stream.read());
        assertEquals(-1, b32stream.read());
        b32stream.close();
}

// org.apache.commons.codec.binary.Base32InputStreamTest::testSkipToEnd
    public void testSkipToEnd() throws Throwable {
        final InputStream ins = new ByteArrayInputStream(StringUtils.getBytesIso8859_1(ENCODED_FOO));
        final Base32InputStream b32stream = new Base32InputStream(ins);
        
        assertEquals(3, b32stream.skip(3));
        
        assertEquals(-1, b32stream.read());
        assertEquals(-1, b32stream.read());
        b32stream.close();
    }

// org.apache.commons.codec.binary.Base32InputStreamTest::testSkipWrongArgument
    public void testSkipWrongArgument() throws Throwable {
        final InputStream ins = new ByteArrayInputStream(StringUtils.getBytesIso8859_1(ENCODED_FOO));
        final Base32InputStream b32stream = new Base32InputStream(ins);
        b32stream.skip(-10);
        b32stream.close();
    }

// org.apache.commons.codec.binary.Base32OutputStreamTest::testBase32EmptyOutputStreamMimeChunkSize
    public void testBase32EmptyOutputStreamMimeChunkSize() throws Exception {
        testBase32EmptyOutputStream(BaseNCodec.MIME_CHUNK_SIZE);
    }

// org.apache.commons.codec.binary.Base32OutputStreamTest::testBase32EmptyOutputStreamPemChunkSize
    public void testBase32EmptyOutputStreamPemChunkSize() throws Exception {
        testBase32EmptyOutputStream(BaseNCodec.PEM_CHUNK_SIZE);
    }

// org.apache.commons.codec.binary.Base32OutputStreamTest::testBase32OutputStreamByChunk
    public void testBase32OutputStreamByChunk() throws Exception {
        
        byte[] encoded = StringUtils.getBytesUtf8(Base32TestData.BASE32_FIXTURE);
        byte[] decoded = StringUtils.getBytesUtf8(Base32TestData.STRING_FIXTURE);
        testByChunk(encoded, decoded, BaseNCodec.MIME_CHUNK_SIZE, CRLF);

        
        final BaseNCodec codec = new Base32();
        for (int i = 0; i <= 150; i++) {
            final byte[][] randomData = Base32TestData.randomData(codec, i);
            encoded = randomData[1];
            decoded = randomData[0];
            testByChunk(encoded, decoded, 0, LF);
        }
    }

// org.apache.commons.codec.binary.Base32OutputStreamTest::testBase32OutputStreamByteByByte
    public void testBase32OutputStreamByteByByte() throws Exception {
        
        byte[] encoded = StringUtils.getBytesUtf8(Base32TestData.BASE32_FIXTURE);
        byte[] decoded = StringUtils.getBytesUtf8(Base32TestData.STRING_FIXTURE);
        testByteByByte(encoded, decoded, 76, CRLF);

        
        final BaseNCodec codec = new Base32();
        for (int i = 0; i <= 150; i++) {
            final byte[][] randomData = Base32TestData.randomData(codec, i);
            encoded = randomData[1];
            decoded = randomData[0];
            testByteByByte(encoded, decoded, 0, LF);
        }
    }

// org.apache.commons.codec.binary.Base32OutputStreamTest::testWriteOutOfBounds
    public void testWriteOutOfBounds() throws Exception {
        final byte[] buf = new byte[1024];
        final ByteArrayOutputStream bout = new ByteArrayOutputStream();
        final Base32OutputStream out = new Base32OutputStream(bout);

        try {
            out.write(buf, -1, 1);
            fail("Expected Base32OutputStream.write(buf, -1, 1) to throw a IndexOutOfBoundsException");
        } catch (final IndexOutOfBoundsException ioobe) {
            
        }

        try {
            out.write(buf, 1, -1);
            fail("Expected Base32OutputStream.write(buf, 1, -1) to throw a IndexOutOfBoundsException");
        } catch (final IndexOutOfBoundsException ioobe) {
            
        }

        try {
            out.write(buf, buf.length + 1, 0);
            fail("Expected Base32OutputStream.write(buf, buf.length + 1, 0) to throw a IndexOutOfBoundsException");
        } catch (final IndexOutOfBoundsException ioobe) {
            
        }

        try {
            out.write(buf, buf.length - 1, 2);
            fail("Expected Base32OutputStream.write(buf, buf.length - 1, 2) to throw a IndexOutOfBoundsException");
        } catch (final IndexOutOfBoundsException ioobe) {
            
        }
        out.close();
    }

// org.apache.commons.codec.binary.Base32OutputStreamTest::testWriteToNullCoverage
    public void testWriteToNullCoverage() throws Exception {
        final ByteArrayOutputStream bout = new ByteArrayOutputStream();
        final Base32OutputStream out = new Base32OutputStream(bout);
        try {
            out.write(null, 0, 0);
            fail("Expcted Base32OutputStream.write(null) to throw a NullPointerException");
        } catch (final NullPointerException e) {
            
        }
        out.close();
    }

// org.apache.commons.codec.binary.Base32Test::testBase64AtBufferStart
    public void testBase64AtBufferStart() {
        testBase64InBuffer(0, 100);
    }

// org.apache.commons.codec.binary.Base32Test::testBase64AtBufferEnd
    public void testBase64AtBufferEnd() {
        testBase64InBuffer(100, 0);
    }

// org.apache.commons.codec.binary.Base32Test::testBase64AtBufferMiddle
    public void testBase64AtBufferMiddle() {
        testBase64InBuffer(100, 100);
    }

// org.apache.commons.codec.binary.Base32Test::testBase32Chunked
    public void testBase32Chunked () throws Exception {
        final Base32 codec = new Base32(20);
        for (final String[] element : BASE32_TEST_CASES_CHUNKED) {
                assertEquals(element[1], codec.encodeAsString(element[0].getBytes(CHARSET_UTF8)));
        }
    }

// org.apache.commons.codec.binary.Base32Test::testBase32HexSamples
    public void testBase32HexSamples() throws Exception {
        final Base32 codec = new Base32(true);
        for (final String[] element : BASE32HEX_TEST_CASES) {
                assertEquals(element[1], codec.encodeAsString(element[0].getBytes(CHARSET_UTF8)));
        }
    }

// org.apache.commons.codec.binary.Base32Test::testBase32Samples
    public void testBase32Samples() throws Exception {
        final Base32 codec = new Base32();
        for (final String[] element : BASE32_TEST_CASES) {
                assertEquals(element[1], codec.encodeAsString(element[0].getBytes(CHARSET_UTF8)));
        }
    }

// org.apache.commons.codec.binary.Base32Test::testBase32SamplesNonDefaultPadding
    public void testBase32SamplesNonDefaultPadding() throws Exception {
        final Base32 codec = new Base32((byte)0x25); 

        for (final String[] element : BASE32_PAD_TEST_CASES) {
                assertEquals(element[1], codec.encodeAsString(element[0].getBytes(CHARSET_UTF8)));
        }
    }

// org.apache.commons.codec.binary.Base32Test::testCodec200
    public void testCodec200() {
        final Base32 codec = new Base32(true, (byte)'W'); 
        assertNotNull(codec);
    }

// org.apache.commons.codec.binary.Base32Test::testRandomBytes
    public void testRandomBytes() {
        for (int i = 0; i < 20; i++) {
            final Base32 codec = new Base32();
            final byte[][] b = Base32TestData.randomData(codec, i);
            assertEquals(""+i+" "+codec.lineLength,b[1].length,codec.getEncodedLength(b[0]));
            
        }
    }

// org.apache.commons.codec.binary.Base32Test::testRandomBytesChunked
    public void testRandomBytesChunked() {
        for (int i = 0; i < 20; i++) {
            final Base32 codec = new Base32(10);
            final byte[][] b = Base32TestData.randomData(codec, i);
            assertEquals(""+i+" "+codec.lineLength,b[1].length,codec.getEncodedLength(b[0]));
            
        }
    }

// org.apache.commons.codec.binary.Base32Test::testRandomBytesHex
    public void testRandomBytesHex() {
        for (int i = 0; i < 20; i++) {
            final Base32 codec = new Base32(true);
            final byte[][] b = Base32TestData.randomData(codec, i);
            assertEquals(""+i+" "+codec.lineLength,b[1].length,codec.getEncodedLength(b[0]));
            
        }
    }

// org.apache.commons.codec.binary.Base32Test::testSingleCharEncoding
    public void testSingleCharEncoding() {
        for (int i = 0; i < 20; i++) {
            Base32 codec = new Base32();
            final BaseNCodec.Context context = new BaseNCodec.Context();
            final byte unencoded[] = new byte[i];
            final byte allInOne[] = codec.encode(unencoded);
            codec = new Base32();
            for (int j=0; j< unencoded.length; j++) {
                codec.encode(unencoded, j, 1, context);
            }
            codec.encode(unencoded, 0, -1, context);
            final byte singly[] = new byte[allInOne.length];
            codec.readResults(singly, 0, 100, context);
            if (!Arrays.equals(allInOne, singly)){
                fail();
            }
        }
    }

// org.apache.commons.codec.binary.Base64InputStreamTest::testCodec130
    public void testCodec130() throws IOException {
        final ByteArrayOutputStream bos = new ByteArrayOutputStream();
        final Base64OutputStream base64os = new Base64OutputStream(bos);

        base64os.write(StringUtils.getBytesUtf8(STRING_FIXTURE));
        base64os.close();

        final ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
        final Base64InputStream ins = new Base64InputStream(bis);

        
        ins.skip(1);
        final byte[] decodedBytes = Base64TestData.streamToBytes(ins, new byte[64]);
        final String str = StringUtils.newStringUtf8(decodedBytes);

        assertEquals(STRING_FIXTURE.substring(1), str);
    }

// org.apache.commons.codec.binary.Base64InputStreamTest::testCodec105
    public void testCodec105() throws IOException {
        final Base64InputStream in = new Base64InputStream(new Codec105ErrorInputStream(), true, 0, null);
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
        final byte[] codec101 = StringUtils.getBytesUtf8(Base64TestData.CODEC_101_MULTIPLE_OF_3);
        final ByteArrayInputStream bais = new ByteArrayInputStream(codec101);
        final Base64InputStream in = new Base64InputStream(bais);
        final byte[] result = new byte[8192];
        int c = in.read(result);
        assertTrue("Codec101: First read successful [c=" + c + "]", c > 0);

        c = in.read(result);
        assertTrue("Codec101: Second read should report end-of-stream [c=" + c + "]", c < 0);
        in.close();
    }

// org.apache.commons.codec.binary.Base64InputStreamTest::testInputStreamReader
    public void testInputStreamReader() throws Exception {
        final byte[] codec101 = StringUtils.getBytesUtf8(Base64TestData.CODEC_101_MULTIPLE_OF_3);
        final ByteArrayInputStream bais = new ByteArrayInputStream(codec101);
        final Base64InputStream in = new Base64InputStream(bais);
        final InputStreamReader isr = new InputStreamReader(in);
        final BufferedReader br = new BufferedReader(isr);
        final String line = br.readLine();
        assertNotNull("Codec101:  InputStreamReader works!", line);
        br.close();
    }

// org.apache.commons.codec.binary.Base64InputStreamTest::testCodec98NPE
    public void testCodec98NPE() throws Exception {
        final byte[] codec98 = StringUtils.getBytesUtf8(Base64TestData.CODEC_98_NPE);
        final ByteArrayInputStream data = new ByteArrayInputStream(codec98);
        final Base64InputStream stream = new Base64InputStream(data);

        
        final byte[] decodedBytes = Base64TestData.streamToBytes(stream, new byte[1024]);

        final String decoded = StringUtils.newStringUtf8(decodedBytes);
        assertEquals("codec-98 NPE Base64InputStream", Base64TestData.CODEC_98_NPE_DECODED, decoded);
    }

// org.apache.commons.codec.binary.Base64InputStreamTest::testAvailable
    public void testAvailable() throws Throwable {
        final InputStream ins = new ByteArrayInputStream(StringUtils.getBytesIso8859_1(ENCODED_B64));
        final Base64InputStream b64stream = new Base64InputStream(ins);
        assertEquals(1, b64stream.available());
        assertEquals(6, b64stream.skip(10));
        
        assertEquals(0, b64stream.available());
        assertEquals(-1, b64stream.read());
        assertEquals(-1, b64stream.read());
        assertEquals(0, b64stream.available());
        b64stream.close();
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

        
        final String singleLine = Base64TestData.ENCODED_64_CHARS_PER_LINE.replaceAll("\n", "");
        encoded = StringUtils.getBytesUtf8(singleLine);
        decoded = Base64TestData.DECODED;
        testByChunk(encoded, decoded, 0, LF);

        
        for (int i = 0; i <= 150; i++) {
            final byte[][] randomData = Base64TestData.randomData(i, false);
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

        
        final String singleLine = Base64TestData.ENCODED_64_CHARS_PER_LINE.replaceAll("\n", "");
        encoded = StringUtils.getBytesUtf8(singleLine);
        decoded = Base64TestData.DECODED;
        testByteByByte(encoded, decoded, 0, LF);

        
        for (int i = 0; i <= 150; i++) {
            final byte[][] randomData = Base64TestData.randomData(i, false);
            encoded = randomData[1];
            decoded = randomData[0];
            testByteByByte(encoded, decoded, 0, LF);
        }
    }

// org.apache.commons.codec.binary.Base64InputStreamTest::testMarkSupported
    public void testMarkSupported() throws Exception {
        final byte[] decoded = StringUtils.getBytesUtf8(STRING_FIXTURE);
        final ByteArrayInputStream bin = new ByteArrayInputStream(decoded);
        final Base64InputStream in = new Base64InputStream(bin, true, 4, new byte[] { 0, 0, 0 });
        
        assertFalse("Base64InputStream.markSupported() is false", in.markSupported());
        in.close();
    }

// org.apache.commons.codec.binary.Base64InputStreamTest::testRead0
    public void testRead0() throws Exception {
        final byte[] decoded = StringUtils.getBytesUtf8(STRING_FIXTURE);
        final byte[] buf = new byte[1024];
        int bytesRead = 0;
        final ByteArrayInputStream bin = new ByteArrayInputStream(decoded);
        final Base64InputStream in = new Base64InputStream(bin, true, 4, new byte[] { 0, 0, 0 });
        bytesRead = in.read(buf, 0, 0);
        assertEquals("Base64InputStream.read(buf, 0, 0) returns 0", 0, bytesRead);
        in.close();
    }

// org.apache.commons.codec.binary.Base64InputStreamTest::testReadNull
    public void testReadNull() throws Exception {
        final byte[] decoded = StringUtils.getBytesUtf8(STRING_FIXTURE);
        final ByteArrayInputStream bin = new ByteArrayInputStream(decoded);
        final Base64InputStream in = new Base64InputStream(bin, true, 4, new byte[] { 0, 0, 0 });
        try {
            in.read(null, 0, 0);
            fail("Base64InputStream.read(null, 0, 0) to throw a NullPointerException");
        } catch (final NullPointerException e) {
            
        }
        in.close();
    }

// org.apache.commons.codec.binary.Base64InputStreamTest::testReadOutOfBounds
    public void testReadOutOfBounds() throws Exception {
        final byte[] decoded = StringUtils.getBytesUtf8(STRING_FIXTURE);
        final byte[] buf = new byte[1024];
        final ByteArrayInputStream bin = new ByteArrayInputStream(decoded);
        final Base64InputStream in = new Base64InputStream(bin, true, 4, new byte[] { 0, 0, 0 });

        try {
            in.read(buf, -1, 0);
            fail("Expected Base64InputStream.read(buf, -1, 0) to throw IndexOutOfBoundsException");
        } catch (final IndexOutOfBoundsException e) {
            
        }

        try {
            in.read(buf, 0, -1);
            fail("Expected Base64InputStream.read(buf, 0, -1) to throw IndexOutOfBoundsException");
        } catch (final IndexOutOfBoundsException e) {
            
        }

        try {
            in.read(buf, buf.length + 1, 0);
            fail("Base64InputStream.read(buf, buf.length + 1, 0) throws IndexOutOfBoundsException");
        } catch (final IndexOutOfBoundsException e) {
            
        }

        try {
            in.read(buf, buf.length - 1, 2);
            fail("Base64InputStream.read(buf, buf.length - 1, 2) throws IndexOutOfBoundsException");
        } catch (final IndexOutOfBoundsException e) {
            
        }
        in.close();
    }

// org.apache.commons.codec.binary.Base64InputStreamTest::testSkipBig
    public void testSkipBig() throws Throwable {
        final InputStream ins = new ByteArrayInputStream(StringUtils.getBytesIso8859_1(ENCODED_B64));
        final Base64InputStream b64stream = new Base64InputStream(ins);
        assertEquals(6, b64stream.skip(Integer.MAX_VALUE));
        
        assertEquals(-1, b64stream.read());
        assertEquals(-1, b64stream.read());
        b64stream.close();
    }

// org.apache.commons.codec.binary.Base64InputStreamTest::testSkipNone
    public void testSkipNone() throws Throwable {
        final InputStream ins = new ByteArrayInputStream(StringUtils.getBytesIso8859_1(ENCODED_B64));
        final Base64InputStream b64stream = new Base64InputStream(ins);
        final byte[] actualBytes = new byte[6];
        assertEquals(0, b64stream.skip(0));
        b64stream.read(actualBytes, 0, actualBytes.length);
        assertArrayEquals(actualBytes, new byte[] { 0, 0, 0, (byte) 255, (byte) 255, (byte) 255 });
        
        assertEquals(-1, b64stream.read());
        b64stream.close();
    }

// org.apache.commons.codec.binary.Base64InputStreamTest::testSkipPastEnd
    public void testSkipPastEnd() throws Throwable {
        final InputStream ins = new ByteArrayInputStream(StringUtils.getBytesIso8859_1(ENCODED_B64));
        final Base64InputStream b64stream = new Base64InputStream(ins);
        
        assertEquals(6, b64stream.skip(10));
        
        assertEquals(-1, b64stream.read());
        assertEquals(-1, b64stream.read());
        b64stream.close();
    }

// org.apache.commons.codec.binary.Base64InputStreamTest::testSkipToEnd
    public void testSkipToEnd() throws Throwable {
        final InputStream ins = new ByteArrayInputStream(StringUtils.getBytesIso8859_1(ENCODED_B64));
        final Base64InputStream b64stream = new Base64InputStream(ins);
        
        assertEquals(6, b64stream.skip(6));
        
        assertEquals(-1, b64stream.read());
        assertEquals(-1, b64stream.read());
        b64stream.close();
    }

// org.apache.commons.codec.binary.Base64InputStreamTest::testSkipWrongArgument
    public void testSkipWrongArgument() throws Throwable {
        final InputStream ins = new ByteArrayInputStream(StringUtils.getBytesIso8859_1(ENCODED_B64));
        final Base64InputStream b64stream = new Base64InputStream(ins);
        b64stream.skip(-10);
        b64stream.close();
    }

// org.apache.commons.codec.binary.Base64OutputStreamTest::testCodec98NPE
    public void testCodec98NPE() throws Exception {
        final byte[] codec98 = StringUtils.getBytesUtf8(Base64TestData.CODEC_98_NPE);
        final byte[] codec98_1024 = new byte[1024];
        System.arraycopy(codec98, 0, codec98_1024, 0, codec98.length);
        final ByteArrayOutputStream data = new ByteArrayOutputStream(1024);
        final Base64OutputStream stream = new Base64OutputStream(data, false);
        stream.write(codec98_1024, 0, 1024);
        stream.close();

        final byte[] decodedBytes = data.toByteArray();
        final String decoded = StringUtils.newStringUtf8(decodedBytes);
        assertEquals(
            "codec-98 NPE Base64OutputStream", Base64TestData.CODEC_98_NPE_DECODED, decoded
        );
    }

// org.apache.commons.codec.binary.Base64OutputStreamTest::testBase64EmptyOutputStreamMimeChunkSize
    public void testBase64EmptyOutputStreamMimeChunkSize() throws Exception {
        testBase64EmptyOutputStream(BaseNCodec.MIME_CHUNK_SIZE);
    }

// org.apache.commons.codec.binary.Base64OutputStreamTest::testBase64EmptyOutputStreamPemChunkSize
    public void testBase64EmptyOutputStreamPemChunkSize() throws Exception {
        testBase64EmptyOutputStream(BaseNCodec.PEM_CHUNK_SIZE);
    }

// org.apache.commons.codec.binary.Base64OutputStreamTest::testBase64OutputStreamByChunk
    public void testBase64OutputStreamByChunk() throws Exception {
        
        byte[] encoded = StringUtils.getBytesUtf8("SGVsbG8gV29ybGQ=\r\n");
        byte[] decoded = StringUtils.getBytesUtf8(STRING_FIXTURE);
        testByChunk(encoded, decoded, BaseNCodec.MIME_CHUNK_SIZE, CRLF);

        
        encoded = StringUtils.getBytesUtf8("AA==\r\n");
        decoded = new byte[]{(byte) 0};
        testByChunk(encoded, decoded, BaseNCodec.MIME_CHUNK_SIZE, CRLF);

        
        encoded = StringUtils.getBytesUtf8(Base64TestData.ENCODED_64_CHARS_PER_LINE);
        decoded = Base64TestData.DECODED;
        testByChunk(encoded, decoded, BaseNCodec.PEM_CHUNK_SIZE, LF);

        
        final String singleLine = Base64TestData.ENCODED_64_CHARS_PER_LINE.replaceAll("\n", "");
        encoded = StringUtils.getBytesUtf8(singleLine);
        decoded = Base64TestData.DECODED;
        testByChunk(encoded, decoded, 0, LF);

        
        for (int i = 0; i <= 150; i++) {
            final byte[][] randomData = Base64TestData.randomData(i, false);
            encoded = randomData[1];
            decoded = randomData[0];
            testByChunk(encoded, decoded, 0, LF);
        }
    }

// org.apache.commons.codec.binary.Base64OutputStreamTest::testBase64OutputStreamByteByByte
    public void testBase64OutputStreamByteByByte() throws Exception {
        
        byte[] encoded = StringUtils.getBytesUtf8("SGVsbG8gV29ybGQ=\r\n");
        byte[] decoded = StringUtils.getBytesUtf8(STRING_FIXTURE);
        testByteByByte(encoded, decoded, 76, CRLF);

        
        encoded = StringUtils.getBytesUtf8("AA==\r\n");
        decoded = new byte[]{(byte) 0};
        testByteByByte(encoded, decoded, 76, CRLF);

        
        encoded = StringUtils.getBytesUtf8(Base64TestData.ENCODED_64_CHARS_PER_LINE);
        decoded = Base64TestData.DECODED;
        testByteByByte(encoded, decoded, 64, LF);

        
        final String singleLine = Base64TestData.ENCODED_64_CHARS_PER_LINE.replaceAll("\n", "");
        encoded = StringUtils.getBytesUtf8(singleLine);
        decoded = Base64TestData.DECODED;
        testByteByByte(encoded, decoded, 0, LF);

        
        for (int i = 0; i <= 150; i++) {
            final byte[][] randomData = Base64TestData.randomData(i, false);
            encoded = randomData[1];
            decoded = randomData[0];
            testByteByByte(encoded, decoded, 0, LF);
        }
    }

// org.apache.commons.codec.binary.Base64OutputStreamTest::testWriteOutOfBounds
    public void testWriteOutOfBounds() throws Exception {
        final byte[] buf = new byte[1024];
        final ByteArrayOutputStream bout = new ByteArrayOutputStream();
        final Base64OutputStream out = new Base64OutputStream(bout);

        try {
            out.write(buf, -1, 1);
            fail("Expected Base64OutputStream.write(buf, -1, 1) to throw a IndexOutOfBoundsException");
        } catch (final IndexOutOfBoundsException ioobe) {
            
        }

        try {
            out.write(buf, 1, -1);
            fail("Expected Base64OutputStream.write(buf, 1, -1) to throw a IndexOutOfBoundsException");
        } catch (final IndexOutOfBoundsException ioobe) {
            
        }

        try {
            out.write(buf, buf.length + 1, 0);
            fail("Expected Base64OutputStream.write(buf, buf.length + 1, 0) to throw a IndexOutOfBoundsException");
        } catch (final IndexOutOfBoundsException ioobe) {
            
        }

        try {
            out.write(buf, buf.length - 1, 2);
            fail("Expected Base64OutputStream.write(buf, buf.length - 1, 2) to throw a IndexOutOfBoundsException");
        } catch (final IndexOutOfBoundsException ioobe) {
            
        }
        out.close();
    }

// org.apache.commons.codec.binary.Base64OutputStreamTest::testWriteToNullCoverage
    public void testWriteToNullCoverage() throws Exception {
        final ByteArrayOutputStream bout = new ByteArrayOutputStream();
        final Base64OutputStream out = new Base64OutputStream(bout);
        try {
            out.write(null, 0, 0);
            fail("Expcted Base64OutputStream.write(null) to throw a NullPointerException");
        } catch (final NullPointerException e) {
            
        } finally {
            out.close();
        }
    }

// org.apache.commons.codec.binary.Base64Test::testIsStringBase64
    public void testIsStringBase64() {
        final String nullString = null;
        final String emptyString = "";
        final String validString = "abc===defg\n\r123456\r789\r\rABC\n\nDEF==GHI\r\nJKL==============";
        final String invalidString = validString + (char) 0; 
                                                                

        try {
            Base64.isBase64(nullString);
            fail("Base64.isStringBase64() should not be null-safe.");
        } catch (final NullPointerException npe) {
            assertNotNull("Base64.isStringBase64() should not be null-safe.", npe);
        }

        assertTrue("Base64.isStringBase64(empty-string) is true", Base64.isBase64(emptyString));
        assertTrue("Base64.isStringBase64(valid-string) is true", Base64.isBase64(validString));
        assertFalse("Base64.isStringBase64(invalid-string) is false", Base64.isBase64(invalidString));
    }

// org.apache.commons.codec.binary.Base64Test::testBase64
    public void testBase64() {
        final String content = "Hello World";
        String encodedContent;
        byte[] encodedBytes = Base64.encodeBase64(StringUtils.getBytesUtf8(content));
        encodedContent = StringUtils.newStringUtf8(encodedBytes);
        assertEquals("encoding hello world", "SGVsbG8gV29ybGQ=", encodedContent);

        Base64 b64 = new Base64(BaseNCodec.MIME_CHUNK_SIZE, null); 
                                                                    
                                                                    
                                                                    
                                                                    
        encodedBytes = b64.encode(StringUtils.getBytesUtf8(content));
        encodedContent = StringUtils.newStringUtf8(encodedBytes);
        assertEquals("encoding hello world", "SGVsbG8gV29ybGQ=", encodedContent);

        b64 = new Base64(0, null); 
                                    
        encodedBytes = b64.encode(StringUtils.getBytesUtf8(content));
        encodedContent = StringUtils.newStringUtf8(encodedBytes);
        assertEquals("encoding hello world", "SGVsbG8gV29ybGQ=", encodedContent);

        
        final byte[] decode = b64.decode("SGVsbG{\u00e9\u00e9\u00e9\u00e9\u00e9\u00e9}8gV29ybGQ=");
        final String decodeString = StringUtils.newStringUtf8(decode);
        assertEquals("decode hello world", "Hello World", decodeString);
    }

// org.apache.commons.codec.binary.Base64Test::testBase64AtBufferStart
    public void testBase64AtBufferStart() {
        testBase64InBuffer(0, 100);
    }

// org.apache.commons.codec.binary.Base64Test::testBase64AtBufferEnd
    public void testBase64AtBufferEnd() {
        testBase64InBuffer(100, 0);
    }

// org.apache.commons.codec.binary.Base64Test::testBase64AtBufferMiddle
    public void testBase64AtBufferMiddle() {
        testBase64InBuffer(100, 100);
    }

// org.apache.commons.codec.binary.Base64Test::testDecodeWithInnerPad
    public void testDecodeWithInnerPad() {
        final String content = "SGVsbG8gV29ybGQ=SGVsbG8gV29ybGQ=";
        final byte[] result = Base64.decodeBase64(content);
        final byte[] shouldBe = StringUtils.getBytesUtf8("Hello World");
        assertTrue("decode should halt at pad (=)", Arrays.equals(result, shouldBe));
    }

// org.apache.commons.codec.binary.Base64Test::testChunkedEncodeMultipleOf76
    public void testChunkedEncodeMultipleOf76() {
        final byte[] expectedEncode = Base64.encodeBase64(Base64TestData.DECODED, true);
        
        
        
        final String actualResult = Base64TestData.ENCODED_76_CHARS_PER_LINE.replaceAll("\n", "\r\n");
        final byte[] actualEncode = StringUtils.getBytesUtf8(actualResult);
        assertTrue("chunkedEncodeMultipleOf76", Arrays.equals(expectedEncode, actualEncode));
    }

// org.apache.commons.codec.binary.Base64Test::testCodec68
    public void testCodec68() {
        final byte[] x = new byte[] { 'n', 'A', '=', '=', (byte) 0x9c };
        Base64.decodeBase64(x);
    }

// org.apache.commons.codec.binary.Base64Test::testCodeInteger1
    public void testCodeInteger1() {
        final String encodedInt1 = "li7dzDacuo67Jg7mtqEm2TRuOMU=";
        final BigInteger bigInt1 = new BigInteger("85739377120809420210425962799" + "0318636601332086981");

        assertEquals(encodedInt1, new String(Base64.encodeInteger(bigInt1)));
        assertEquals(bigInt1, Base64.decodeInteger(encodedInt1.getBytes(CHARSET_UTF8)));
    }

// org.apache.commons.codec.binary.Base64Test::testCodeInteger2
    public void testCodeInteger2() {
        final String encodedInt2 = "9B5ypLY9pMOmtxCeTDHgwdNFeGs=";
        final BigInteger bigInt2 = new BigInteger("13936727572861167254666467268" + "91466679477132949611");

        assertEquals(encodedInt2, new String(Base64.encodeInteger(bigInt2)));
        assertEquals(bigInt2, Base64.decodeInteger(encodedInt2.getBytes(CHARSET_UTF8)));
    }

// org.apache.commons.codec.binary.Base64Test::testCodeInteger3
    public void testCodeInteger3() {
        final String encodedInt3 = "FKIhdgaG5LGKiEtF1vHy4f3y700zaD6QwDS3IrNVGzNp2"
                + "rY+1LFWTK6D44AyiC1n8uWz1itkYMZF0/aKDK0Yjg==";
        final BigInteger bigInt3 = new BigInteger(
                "10806548154093873461951748545" + "1196989136416448805819079363524309897749044958112417136240557"
                        + "4495062430572478766856090958495998158114332651671116876320938126");

        assertEquals(encodedInt3, new String(Base64.encodeInteger(bigInt3)));
        assertEquals(bigInt3, Base64.decodeInteger(encodedInt3.getBytes(CHARSET_UTF8)));
    }

// org.apache.commons.codec.binary.Base64Test::testCodeInteger4
    public void testCodeInteger4() {
        final String encodedInt4 = "ctA8YGxrtngg/zKVvqEOefnwmViFztcnPBYPlJsvh6yKI"
                + "4iDm68fnp4Mi3RrJ6bZAygFrUIQLxLjV+OJtgJAEto0xAs+Mehuq1DkSFEpP3o"
                + "DzCTOsrOiS1DwQe4oIb7zVk/9l7aPtJMHW0LVlMdwZNFNNJoqMcT2ZfCPrfvYv" + "Q0=";
        final BigInteger bigInt4 = new BigInteger(
                "80624726256040348115552042320" + "6968135001872753709424419772586693950232350200555646471175944"
                        + "519297087885987040810778908507262272892702303774422853675597"
                        + "748008534040890923814202286633163248086055216976551456088015"
                        + "338880713818192088877057717530169381044092839402438015097654"
                        + "53542091716518238707344493641683483917");

        assertEquals(encodedInt4, new String(Base64.encodeInteger(bigInt4)));
        assertEquals(bigInt4, Base64.decodeInteger(encodedInt4.getBytes(CHARSET_UTF8)));
    }

// org.apache.commons.codec.binary.Base64Test::testCodeIntegerEdgeCases
    public void testCodeIntegerEdgeCases() {
        
    }

// org.apache.commons.codec.binary.Base64Test::testCodeIntegerNull
    public void testCodeIntegerNull() {
        try {
            Base64.encodeInteger(null);
            fail("Exception not thrown when passing in null to encodeInteger(BigInteger)");
        } catch (final NullPointerException npe) {
            
        } catch (final Exception e) {
            fail("Incorrect Exception caught when passing in null to encodeInteger(BigInteger)");
        }
    }

// org.apache.commons.codec.binary.Base64Test::testConstructors
    public void testConstructors() {
        Base64 base64;
        base64 = new Base64();
        base64 = new Base64(-1);
        base64 = new Base64(-1, new byte[] {});
        base64 = new Base64(64, new byte[] {});
        try {
            base64 = new Base64(-1, new byte[] { 'A' }); 
                                                            
                                                            
            fail("Should have rejected attempt to use 'A' as a line separator");
        } catch (final IllegalArgumentException ignored) {
            
        }
        try {
            base64 = new Base64(64, new byte[] { 'A' });
            fail("Should have rejected attempt to use 'A' as a line separator");
        } catch (final IllegalArgumentException ignored) {
            
        }
        try {
            base64 = new Base64(64, new byte[] { '=' });
            fail("Should have rejected attempt to use '=' as a line separator");
        } catch (final IllegalArgumentException ignored) {
            
        }
        base64 = new Base64(64, new byte[] { '$' }); 
        try {
            base64 = new Base64(64, new byte[] { 'A', '$' });
            fail("Should have rejected attempt to use 'A$' as a line separator");
        } catch (final IllegalArgumentException ignored) {
            
        }
        base64 = new Base64(64, new byte[] { ' ', '$', '\n', '\r', '\t' }); 
        assertNotNull(base64);
    }

// org.apache.commons.codec.binary.Base64Test::testConstructor_Int_ByteArray_Boolean
    public void testConstructor_Int_ByteArray_Boolean() {
        final Base64 base64 = new Base64(65, new byte[] { '\t' }, false);
        final byte[] encoded = base64.encode(Base64TestData.DECODED);
        String expectedResult = Base64TestData.ENCODED_64_CHARS_PER_LINE;
        expectedResult = expectedResult.replace('\n', '\t');
        final String result = StringUtils.newStringUtf8(encoded);
        assertEquals("new Base64(65, \\t, false)", expectedResult, result);
    }

// org.apache.commons.codec.binary.Base64Test::testConstructor_Int_ByteArray_Boolean_UrlSafe
    public void testConstructor_Int_ByteArray_Boolean_UrlSafe() {
        
        final Base64 base64 = new Base64(64, new byte[] { '\t' }, true);
        final byte[] encoded = base64.encode(Base64TestData.DECODED);
        String expectedResult = Base64TestData.ENCODED_64_CHARS_PER_LINE;
        expectedResult = expectedResult.replaceAll("=", ""); 
                                                                
        expectedResult = expectedResult.replace('\n', '\t');
        expectedResult = expectedResult.replace('+', '-');
        expectedResult = expectedResult.replace('/', '_');
        final String result = StringUtils.newStringUtf8(encoded);
        assertEquals("new Base64(64, \\t, true)", result, expectedResult);
    }

// org.apache.commons.codec.binary.Base64Test::testDecodePadMarkerIndex2
    public void testDecodePadMarkerIndex2() {
        assertEquals("A", new String(Base64.decodeBase64("QQ==".getBytes(CHARSET_UTF8))));
    }

// org.apache.commons.codec.binary.Base64Test::testDecodePadMarkerIndex3
    public void testDecodePadMarkerIndex3() {
        assertEquals("AA", new String(Base64.decodeBase64("QUE=".getBytes(CHARSET_UTF8))));
        assertEquals("AAA", new String(Base64.decodeBase64("QUFB".getBytes(CHARSET_UTF8))));
    }

// org.apache.commons.codec.binary.Base64Test::testDecodePadOnly
    public void testDecodePadOnly() {
        assertEquals(0, Base64.decodeBase64("====".getBytes(CHARSET_UTF8)).length);
        assertEquals("", new String(Base64.decodeBase64("====".getBytes(CHARSET_UTF8))));
        
        assertEquals(0, Base64.decodeBase64("===".getBytes(CHARSET_UTF8)).length);
        assertEquals(0, Base64.decodeBase64("==".getBytes(CHARSET_UTF8)).length);
        assertEquals(0, Base64.decodeBase64("=".getBytes(CHARSET_UTF8)).length);
        assertEquals(0, Base64.decodeBase64("".getBytes(CHARSET_UTF8)).length);
    }

// org.apache.commons.codec.binary.Base64Test::testDecodePadOnlyChunked
    public void testDecodePadOnlyChunked() {
        assertEquals(0, Base64.decodeBase64("====\n".getBytes(CHARSET_UTF8)).length);
        assertEquals("", new String(Base64.decodeBase64("====\n".getBytes(CHARSET_UTF8))));
        
        assertEquals(0, Base64.decodeBase64("===\n".getBytes(CHARSET_UTF8)).length);
        assertEquals(0, Base64.decodeBase64("==\n".getBytes(CHARSET_UTF8)).length);
        assertEquals(0, Base64.decodeBase64("=\n".getBytes(CHARSET_UTF8)).length);
        assertEquals(0, Base64.decodeBase64("\n".getBytes(CHARSET_UTF8)).length);
    }

// org.apache.commons.codec.binary.Base64Test::testDecodeWithWhitespace
    public void testDecodeWithWhitespace() throws Exception {

        final String orig = "I am a late night coder.";

        final byte[] encodedArray = Base64.encodeBase64(orig.getBytes(CHARSET_UTF8));
        final StringBuilder intermediate = new StringBuilder(new String(encodedArray));

        intermediate.insert(2, ' ');
        intermediate.insert(5, '\t');
        intermediate.insert(10, '\r');
        intermediate.insert(15, '\n');

        final byte[] encodedWithWS = intermediate.toString().getBytes(CHARSET_UTF8);
        final byte[] decodedWithWS = Base64.decodeBase64(encodedWithWS);

        final String dest = new String(decodedWithWS);

        assertEquals("Dest string doesn't equal the original", orig, dest);
    }

// org.apache.commons.codec.binary.Base64Test::testEmptyBase64
    public void testEmptyBase64() {
        byte[] empty = new byte[0];
        byte[] result = Base64.encodeBase64(empty);
        assertEquals("empty base64 encode", 0, result.length);
        assertEquals("empty base64 encode", null, Base64.encodeBase64(null));

        empty = new byte[0];
        result = Base64.decodeBase64(empty);
        assertEquals("empty base64 decode", 0, result.length);
        assertEquals("empty base64 encode", null, Base64.decodeBase64((byte[]) null));
    }

// org.apache.commons.codec.binary.Base64Test::testEncodeDecodeRandom
    public void testEncodeDecodeRandom() {
        for (int i = 1; i < 5; i++) {
            final byte[] data = new byte[this.getRandom().nextInt(10000) + 1];
            this.getRandom().nextBytes(data);
            final byte[] enc = Base64.encodeBase64(data);
            assertTrue(Base64.isBase64(enc));
            final byte[] data2 = Base64.decodeBase64(enc);
            assertTrue(Arrays.equals(data, data2));
        }
    }

// org.apache.commons.codec.binary.Base64Test::testEncodeDecodeSmall
    public void testEncodeDecodeSmall() {
        for (int i = 0; i < 12; i++) {
            final byte[] data = new byte[i];
            this.getRandom().nextBytes(data);
            final byte[] enc = Base64.encodeBase64(data);
            assertTrue("\"" + new String(enc) + "\" is Base64 data.", Base64.isBase64(enc));
            final byte[] data2 = Base64.decodeBase64(enc);
            assertTrue(toString(data) + " equals " + toString(data2), Arrays.equals(data, data2));
        }
    }

// org.apache.commons.codec.binary.Base64Test::testEncodeOverMaxSize
    public void testEncodeOverMaxSize() throws Exception {
        testEncodeOverMaxSize(-1);
        testEncodeOverMaxSize(0);
        testEncodeOverMaxSize(1);
        testEncodeOverMaxSize(2);
    }

// org.apache.commons.codec.binary.Base64Test::testCodec112
    public void testCodec112() { 
        final byte[] in = new byte[] { 0 };
        final byte[] out = Base64.encodeBase64(in);
        Base64.encodeBase64(in, false, false, out.length);
    }

// org.apache.commons.codec.binary.Base64Test::testIgnoringNonBase64InDecode
    public void testIgnoringNonBase64InDecode() throws Exception {
        assertEquals("The quick brown fox jumped over the lazy dogs.",
                new String(Base64.decodeBase64(
                        "VGhlIH@$#$@%F1aWN@#@#@@rIGJyb3duIGZve\n\r\t%#%#%#%CBqd##$#$W1wZWQgb3ZlciB0aGUgbGF6eSBkb2dzLg=="
                                .getBytes(CHARSET_UTF8))));
    }

// org.apache.commons.codec.binary.Base64Test::testIsArrayByteBase64
    public void testIsArrayByteBase64() {
        assertFalse(Base64.isBase64(new byte[] { Byte.MIN_VALUE }));
        assertFalse(Base64.isBase64(new byte[] { -125 }));
        assertFalse(Base64.isBase64(new byte[] { -10 }));
        assertFalse(Base64.isBase64(new byte[] { 0 }));
        assertFalse(Base64.isBase64(new byte[] { 64, Byte.MAX_VALUE }));
        assertFalse(Base64.isBase64(new byte[] { Byte.MAX_VALUE }));
        assertTrue(Base64.isBase64(new byte[] { 'A' }));
        assertFalse(Base64.isBase64(new byte[] { 'A', Byte.MIN_VALUE }));
        assertTrue(Base64.isBase64(new byte[] { 'A', 'Z', 'a' }));
        assertTrue(Base64.isBase64(new byte[] { '/', '=', '+' }));
        assertFalse(Base64.isBase64(new byte[] { '$' }));
    }

// org.apache.commons.codec.binary.Base64Test::testIsUrlSafe
    public void testIsUrlSafe() {
        final Base64 base64Standard = new Base64(false);
        final Base64 base64URLSafe = new Base64(true);

        assertFalse("Base64.isUrlSafe=false", base64Standard.isUrlSafe());
        assertTrue("Base64.isUrlSafe=true", base64URLSafe.isUrlSafe());

        final byte[] whiteSpace = { ' ', '\n', '\r', '\t' };
        assertTrue("Base64.isBase64(whiteSpace)=true", Base64.isBase64(whiteSpace));
    }

// org.apache.commons.codec.binary.Base64Test::testKnownDecodings
    public void testKnownDecodings() {
        assertEquals("The quick brown fox jumped over the lazy dogs.", new String(Base64.decodeBase64(
                "VGhlIHF1aWNrIGJyb3duIGZveCBqdW1wZWQgb3ZlciB0aGUgbGF6eSBkb2dzLg==".getBytes(CHARSET_UTF8))));
        assertEquals("It was the best of times, it was the worst of times.", new String(Base64.decodeBase64(
                "SXQgd2FzIHRoZSBiZXN0IG9mIHRpbWVzLCBpdCB3YXMgdGhlIHdvcnN0IG9mIHRpbWVzLg==".getBytes(CHARSET_UTF8))));
        assertEquals("http://jakarta.apache.org/commmons", new String(
                Base64.decodeBase64("aHR0cDovL2pha2FydGEuYXBhY2hlLm9yZy9jb21tbW9ucw==".getBytes(CHARSET_UTF8))));
        assertEquals("AaBbCcDdEeFfGgHhIiJjKkLlMmNnOoPpQqRrSsTtUuVvWwXxYyZz", new String(Base64.decodeBase64(
                "QWFCYkNjRGRFZUZmR2dIaElpSmpLa0xsTW1Obk9vUHBRcVJyU3NUdFV1VnZXd1h4WXlaeg==".getBytes(CHARSET_UTF8))));
        assertEquals("{ 0, 1, 2, 3, 4, 5, 6, 7, 8, 9 }",
                new String(Base64.decodeBase64("eyAwLCAxLCAyLCAzLCA0LCA1LCA2LCA3LCA4LCA5IH0=".getBytes(CHARSET_UTF8))));
        assertEquals("xyzzy!", new String(Base64.decodeBase64("eHl6enkh".getBytes(CHARSET_UTF8))));
    }

// org.apache.commons.codec.binary.Base64Test::testKnownEncodings
    public void testKnownEncodings() {
        assertEquals("VGhlIHF1aWNrIGJyb3duIGZveCBqdW1wZWQgb3ZlciB0aGUgbGF6eSBkb2dzLg==", new String(
                Base64.encodeBase64("The quick brown fox jumped over the lazy dogs.".getBytes(CHARSET_UTF8))));
        assertEquals(
                "YmxhaCBibGFoIGJsYWggYmxhaCBibGFoIGJsYWggYmxhaCBibGFoIGJsYWggYmxhaCBibGFoIGJs\r\nYWggYmxhaCBibGFoIGJsYWggYmxhaCBibGFoIGJsYWggYmxhaCBibGFoIGJsYWggYmxhaCBibGFo\r\nIGJsYWggYmxhaCBibGFoIGJsYWggYmxhaCBibGFoIGJsYWggYmxhaCBibGFoIGJsYWggYmxhaCBi\r\nbGFoIGJsYWg=\r\n",
                new String(Base64.encodeBase64Chunked(
                        "blah blah blah blah blah blah blah blah blah blah blah blah blah blah blah blah blah blah blah blah blah blah blah blah blah blah blah blah blah blah blah blah blah blah blah blah"
                                .getBytes(CHARSET_UTF8))));
        assertEquals("SXQgd2FzIHRoZSBiZXN0IG9mIHRpbWVzLCBpdCB3YXMgdGhlIHdvcnN0IG9mIHRpbWVzLg==", new String(
                Base64.encodeBase64("It was the best of times, it was the worst of times.".getBytes(CHARSET_UTF8))));
        assertEquals("aHR0cDovL2pha2FydGEuYXBhY2hlLm9yZy9jb21tbW9ucw==",
                new String(Base64.encodeBase64("http://jakarta.apache.org/commmons".getBytes(CHARSET_UTF8))));
        assertEquals("QWFCYkNjRGRFZUZmR2dIaElpSmpLa0xsTW1Obk9vUHBRcVJyU3NUdFV1VnZXd1h4WXlaeg==", new String(
                Base64.encodeBase64("AaBbCcDdEeFfGgHhIiJjKkLlMmNnOoPpQqRrSsTtUuVvWwXxYyZz".getBytes(CHARSET_UTF8))));
        assertEquals("eyAwLCAxLCAyLCAzLCA0LCA1LCA2LCA3LCA4LCA5IH0=",
                new String(Base64.encodeBase64("{ 0, 1, 2, 3, 4, 5, 6, 7, 8, 9 }".getBytes(CHARSET_UTF8))));
        assertEquals("eHl6enkh", new String(Base64.encodeBase64("xyzzy!".getBytes(CHARSET_UTF8))));
    }

// org.apache.commons.codec.binary.Base64Test::testNonBase64Test
    public void testNonBase64Test() throws Exception {

        final byte[] bArray = { '%' };

        assertFalse("Invalid Base64 array was incorrectly validated as " + "an array of Base64 encoded data",
                Base64.isBase64(bArray));

        try {
            final Base64 b64 = new Base64();
            final byte[] result = b64.decode(bArray);

            assertEquals("The result should be empty as the test encoded content did "
                    + "not contain any valid base 64 characters", 0, result.length);
        } catch (final Exception e) {
            fail("Exception was thrown when trying to decode "
                    + "invalid base64 encoded data - RFC 2045 requires that all "
                    + "non base64 character be discarded, an exception should not" + " have been thrown");
        }
    }

// org.apache.commons.codec.binary.Base64Test::testObjectDecodeWithInvalidParameter
    public void testObjectDecodeWithInvalidParameter() throws Exception {
        final Base64 b64 = new Base64();

        try {
            b64.decode(Integer.valueOf(5));
            fail("decode(Object) didn't throw an exception when passed an Integer object");
        } catch (final DecoderException e) {
            
        }

    }

// org.apache.commons.codec.binary.Base64Test::testObjectDecodeWithValidParameter
    public void testObjectDecodeWithValidParameter() throws Exception {

        final String original = "Hello World!";
        final Object o = Base64.encodeBase64(original.getBytes(CHARSET_UTF8));

        final Base64 b64 = new Base64();
        final Object oDecoded = b64.decode(o);
        final byte[] baDecoded = (byte[]) oDecoded;
        final String dest = new String(baDecoded);

        assertEquals("dest string does not equal original", original, dest);
    }

// org.apache.commons.codec.binary.Base64Test::testObjectEncodeWithInvalidParameter
    public void testObjectEncodeWithInvalidParameter() throws Exception {
        final Base64 b64 = new Base64();
        try {
            b64.encode("Yadayadayada");
            fail("encode(Object) didn't throw an exception when passed a String object");
        } catch (final EncoderException e) {
            
        }
    }

// org.apache.commons.codec.binary.Base64Test::testObjectEncodeWithValidParameter
    public void testObjectEncodeWithValidParameter() throws Exception {

        final String original = "Hello World!";
        final Object origObj = original.getBytes(CHARSET_UTF8);

        final Base64 b64 = new Base64();
        final Object oEncoded = b64.encode(origObj);
        final byte[] bArray = Base64.decodeBase64((byte[]) oEncoded);
        final String dest = new String(bArray);

        assertEquals("dest string does not equal original", original, dest);
    }

// org.apache.commons.codec.binary.Base64Test::testObjectEncode
    public void testObjectEncode() throws Exception {
        final Base64 b64 = new Base64();
        assertEquals("SGVsbG8gV29ybGQ=", new String(b64.encode("Hello World".getBytes(CHARSET_UTF8))));
    }

// org.apache.commons.codec.binary.Base64Test::testPairs
    public void testPairs() {
        assertEquals("AAA=", new String(Base64.encodeBase64(new byte[] { 0, 0 })));
        for (int i = -128; i <= 127; i++) {
            final byte test[] = { (byte) i, (byte) i };
            assertTrue(Arrays.equals(test, Base64.decodeBase64(Base64.encodeBase64(test))));
        }
    }

// org.apache.commons.codec.binary.Base64Test::testRfc2045Section2Dot1CrLfDefinition
    public void testRfc2045Section2Dot1CrLfDefinition() {
        assertTrue(Arrays.equals(new byte[] { 13, 10 }, Base64.CHUNK_SEPARATOR));
    }

// org.apache.commons.codec.binary.Base64Test::testRfc2045Section6Dot8ChunkSizeDefinition
    public void testRfc2045Section6Dot8ChunkSizeDefinition() {
        assertEquals(76, BaseNCodec.MIME_CHUNK_SIZE);
    }

// org.apache.commons.codec.binary.Base64Test::testRfc1421Section6Dot8ChunkSizeDefinition
    public void testRfc1421Section6Dot8ChunkSizeDefinition() {
        assertEquals(64, BaseNCodec.PEM_CHUNK_SIZE);
    }

// org.apache.commons.codec.binary.Base64Test::testRfc4648Section10Decode
    public void testRfc4648Section10Decode() {
        assertEquals("", StringUtils.newStringUsAscii(Base64.decodeBase64("")));
        assertEquals("f", StringUtils.newStringUsAscii(Base64.decodeBase64("Zg==")));
        assertEquals("fo", StringUtils.newStringUsAscii(Base64.decodeBase64("Zm8=")));
        assertEquals("foo", StringUtils.newStringUsAscii(Base64.decodeBase64("Zm9v")));
        assertEquals("foob", StringUtils.newStringUsAscii(Base64.decodeBase64("Zm9vYg==")));
        assertEquals("fooba", StringUtils.newStringUsAscii(Base64.decodeBase64("Zm9vYmE=")));
        assertEquals("foobar", StringUtils.newStringUsAscii(Base64.decodeBase64("Zm9vYmFy")));
    }

// org.apache.commons.codec.binary.Base64Test::testRfc4648Section10DecodeWithCrLf
    public void testRfc4648Section10DecodeWithCrLf() {
        final String CRLF = StringUtils.newStringUsAscii(Base64.CHUNK_SEPARATOR);
        assertEquals("", StringUtils.newStringUsAscii(Base64.decodeBase64("" + CRLF)));
        assertEquals("f", StringUtils.newStringUsAscii(Base64.decodeBase64("Zg==" + CRLF)));
        assertEquals("fo", StringUtils.newStringUsAscii(Base64.decodeBase64("Zm8=" + CRLF)));
        assertEquals("foo", StringUtils.newStringUsAscii(Base64.decodeBase64("Zm9v" + CRLF)));
        assertEquals("foob", StringUtils.newStringUsAscii(Base64.decodeBase64("Zm9vYg==" + CRLF)));
        assertEquals("fooba", StringUtils.newStringUsAscii(Base64.decodeBase64("Zm9vYmE=" + CRLF)));
        assertEquals("foobar", StringUtils.newStringUsAscii(Base64.decodeBase64("Zm9vYmFy" + CRLF)));
    }

// org.apache.commons.codec.binary.Base64Test::testRfc4648Section10Encode
    public void testRfc4648Section10Encode() {
        assertEquals("", Base64.encodeBase64String(StringUtils.getBytesUtf8("")));
        assertEquals("Zg==", Base64.encodeBase64String(StringUtils.getBytesUtf8("f")));
        assertEquals("Zm8=", Base64.encodeBase64String(StringUtils.getBytesUtf8("fo")));
        assertEquals("Zm9v", Base64.encodeBase64String(StringUtils.getBytesUtf8("foo")));
        assertEquals("Zm9vYg==", Base64.encodeBase64String(StringUtils.getBytesUtf8("foob")));
        assertEquals("Zm9vYmE=", Base64.encodeBase64String(StringUtils.getBytesUtf8("fooba")));
        assertEquals("Zm9vYmFy", Base64.encodeBase64String(StringUtils.getBytesUtf8("foobar")));
    }

// org.apache.commons.codec.binary.Base64Test::testRfc4648Section10DecodeEncode
    public void testRfc4648Section10DecodeEncode() {
        testDecodeEncode("");
        testDecodeEncode("Zg==");
        testDecodeEncode("Zm8=");
        testDecodeEncode("Zm9v");
        testDecodeEncode("Zm9vYg==");
        testDecodeEncode("Zm9vYmE=");
        testDecodeEncode("Zm9vYmFy");
    }

// org.apache.commons.codec.binary.Base64Test::testRfc4648Section10EncodeDecode
    public void testRfc4648Section10EncodeDecode() {
        testEncodeDecode("");
        testEncodeDecode("f");
        testEncodeDecode("fo");
        testEncodeDecode("foo");
        testEncodeDecode("foob");
        testEncodeDecode("fooba");
        testEncodeDecode("foobar");
    }

// org.apache.commons.codec.binary.Base64Test::testSingletons
    public void testSingletons() {
        assertEquals("AA==", new String(Base64.encodeBase64(new byte[] { (byte) 0 })));
        assertEquals("AQ==", new String(Base64.encodeBase64(new byte[] { (byte) 1 })));
        assertEquals("Ag==", new String(Base64.encodeBase64(new byte[] { (byte) 2 })));
        assertEquals("Aw==", new String(Base64.encodeBase64(new byte[] { (byte) 3 })));
        assertEquals("BA==", new String(Base64.encodeBase64(new byte[] { (byte) 4 })));
        assertEquals("BQ==", new String(Base64.encodeBase64(new byte[] { (byte) 5 })));
        assertEquals("Bg==", new String(Base64.encodeBase64(new byte[] { (byte) 6 })));
        assertEquals("Bw==", new String(Base64.encodeBase64(new byte[] { (byte) 7 })));
        assertEquals("CA==", new String(Base64.encodeBase64(new byte[] { (byte) 8 })));
        assertEquals("CQ==", new String(Base64.encodeBase64(new byte[] { (byte) 9 })));
        assertEquals("Cg==", new String(Base64.encodeBase64(new byte[] { (byte) 10 })));
        assertEquals("Cw==", new String(Base64.encodeBase64(new byte[] { (byte) 11 })));
        assertEquals("DA==", new String(Base64.encodeBase64(new byte[] { (byte) 12 })));
        assertEquals("DQ==", new String(Base64.encodeBase64(new byte[] { (byte) 13 })));
        assertEquals("Dg==", new String(Base64.encodeBase64(new byte[] { (byte) 14 })));
        assertEquals("Dw==", new String(Base64.encodeBase64(new byte[] { (byte) 15 })));
        assertEquals("EA==", new String(Base64.encodeBase64(new byte[] { (byte) 16 })));
        assertEquals("EQ==", new String(Base64.encodeBase64(new byte[] { (byte) 17 })));
        assertEquals("Eg==", new String(Base64.encodeBase64(new byte[] { (byte) 18 })));
        assertEquals("Ew==", new String(Base64.encodeBase64(new byte[] { (byte) 19 })));
        assertEquals("FA==", new String(Base64.encodeBase64(new byte[] { (byte) 20 })));
        assertEquals("FQ==", new String(Base64.encodeBase64(new byte[] { (byte) 21 })));
        assertEquals("Fg==", new String(Base64.encodeBase64(new byte[] { (byte) 22 })));
        assertEquals("Fw==", new String(Base64.encodeBase64(new byte[] { (byte) 23 })));
        assertEquals("GA==", new String(Base64.encodeBase64(new byte[] { (byte) 24 })));
        assertEquals("GQ==", new String(Base64.encodeBase64(new byte[] { (byte) 25 })));
        assertEquals("Gg==", new String(Base64.encodeBase64(new byte[] { (byte) 26 })));
        assertEquals("Gw==", new String(Base64.encodeBase64(new byte[] { (byte) 27 })));
        assertEquals("HA==", new String(Base64.encodeBase64(new byte[] { (byte) 28 })));
        assertEquals("HQ==", new String(Base64.encodeBase64(new byte[] { (byte) 29 })));
        assertEquals("Hg==", new String(Base64.encodeBase64(new byte[] { (byte) 30 })));
        assertEquals("Hw==", new String(Base64.encodeBase64(new byte[] { (byte) 31 })));
        assertEquals("IA==", new String(Base64.encodeBase64(new byte[] { (byte) 32 })));
        assertEquals("IQ==", new String(Base64.encodeBase64(new byte[] { (byte) 33 })));
        assertEquals("Ig==", new String(Base64.encodeBase64(new byte[] { (byte) 34 })));
        assertEquals("Iw==", new String(Base64.encodeBase64(new byte[] { (byte) 35 })));
        assertEquals("JA==", new String(Base64.encodeBase64(new byte[] { (byte) 36 })));
        assertEquals("JQ==", new String(Base64.encodeBase64(new byte[] { (byte) 37 })));
        assertEquals("Jg==", new String(Base64.encodeBase64(new byte[] { (byte) 38 })));
        assertEquals("Jw==", new String(Base64.encodeBase64(new byte[] { (byte) 39 })));
        assertEquals("KA==", new String(Base64.encodeBase64(new byte[] { (byte) 40 })));
        assertEquals("KQ==", new String(Base64.encodeBase64(new byte[] { (byte) 41 })));
        assertEquals("Kg==", new String(Base64.encodeBase64(new byte[] { (byte) 42 })));
        assertEquals("Kw==", new String(Base64.encodeBase64(new byte[] { (byte) 43 })));
        assertEquals("LA==", new String(Base64.encodeBase64(new byte[] { (byte) 44 })));
        assertEquals("LQ==", new String(Base64.encodeBase64(new byte[] { (byte) 45 })));
        assertEquals("Lg==", new String(Base64.encodeBase64(new byte[] { (byte) 46 })));
        assertEquals("Lw==", new String(Base64.encodeBase64(new byte[] { (byte) 47 })));
        assertEquals("MA==", new String(Base64.encodeBase64(new byte[] { (byte) 48 })));
        assertEquals("MQ==", new String(Base64.encodeBase64(new byte[] { (byte) 49 })));
        assertEquals("Mg==", new String(Base64.encodeBase64(new byte[] { (byte) 50 })));
        assertEquals("Mw==", new String(Base64.encodeBase64(new byte[] { (byte) 51 })));
        assertEquals("NA==", new String(Base64.encodeBase64(new byte[] { (byte) 52 })));
        assertEquals("NQ==", new String(Base64.encodeBase64(new byte[] { (byte) 53 })));
        assertEquals("Ng==", new String(Base64.encodeBase64(new byte[] { (byte) 54 })));
        assertEquals("Nw==", new String(Base64.encodeBase64(new byte[] { (byte) 55 })));
        assertEquals("OA==", new String(Base64.encodeBase64(new byte[] { (byte) 56 })));
        assertEquals("OQ==", new String(Base64.encodeBase64(new byte[] { (byte) 57 })));
        assertEquals("Og==", new String(Base64.encodeBase64(new byte[] { (byte) 58 })));
        assertEquals("Ow==", new String(Base64.encodeBase64(new byte[] { (byte) 59 })));
        assertEquals("PA==", new String(Base64.encodeBase64(new byte[] { (byte) 60 })));
        assertEquals("PQ==", new String(Base64.encodeBase64(new byte[] { (byte) 61 })));
        assertEquals("Pg==", new String(Base64.encodeBase64(new byte[] { (byte) 62 })));
        assertEquals("Pw==", new String(Base64.encodeBase64(new byte[] { (byte) 63 })));
        assertEquals("QA==", new String(Base64.encodeBase64(new byte[] { (byte) 64 })));
        assertEquals("QQ==", new String(Base64.encodeBase64(new byte[] { (byte) 65 })));
        assertEquals("Qg==", new String(Base64.encodeBase64(new byte[] { (byte) 66 })));
        assertEquals("Qw==", new String(Base64.encodeBase64(new byte[] { (byte) 67 })));
        assertEquals("RA==", new String(Base64.encodeBase64(new byte[] { (byte) 68 })));
        assertEquals("RQ==", new String(Base64.encodeBase64(new byte[] { (byte) 69 })));
        assertEquals("Rg==", new String(Base64.encodeBase64(new byte[] { (byte) 70 })));
        assertEquals("Rw==", new String(Base64.encodeBase64(new byte[] { (byte) 71 })));
        assertEquals("SA==", new String(Base64.encodeBase64(new byte[] { (byte) 72 })));
        assertEquals("SQ==", new String(Base64.encodeBase64(new byte[] { (byte) 73 })));
        assertEquals("Sg==", new String(Base64.encodeBase64(new byte[] { (byte) 74 })));
        assertEquals("Sw==", new String(Base64.encodeBase64(new byte[] { (byte) 75 })));
        assertEquals("TA==", new String(Base64.encodeBase64(new byte[] { (byte) 76 })));
        assertEquals("TQ==", new String(Base64.encodeBase64(new byte[] { (byte) 77 })));
        assertEquals("Tg==", new String(Base64.encodeBase64(new byte[] { (byte) 78 })));
        assertEquals("Tw==", new String(Base64.encodeBase64(new byte[] { (byte) 79 })));
        assertEquals("UA==", new String(Base64.encodeBase64(new byte[] { (byte) 80 })));
        assertEquals("UQ==", new String(Base64.encodeBase64(new byte[] { (byte) 81 })));
        assertEquals("Ug==", new String(Base64.encodeBase64(new byte[] { (byte) 82 })));
        assertEquals("Uw==", new String(Base64.encodeBase64(new byte[] { (byte) 83 })));
        assertEquals("VA==", new String(Base64.encodeBase64(new byte[] { (byte) 84 })));
        assertEquals("VQ==", new String(Base64.encodeBase64(new byte[] { (byte) 85 })));
        assertEquals("Vg==", new String(Base64.encodeBase64(new byte[] { (byte) 86 })));
        assertEquals("Vw==", new String(Base64.encodeBase64(new byte[] { (byte) 87 })));
        assertEquals("WA==", new String(Base64.encodeBase64(new byte[] { (byte) 88 })));
        assertEquals("WQ==", new String(Base64.encodeBase64(new byte[] { (byte) 89 })));
        assertEquals("Wg==", new String(Base64.encodeBase64(new byte[] { (byte) 90 })));
        assertEquals("Ww==", new String(Base64.encodeBase64(new byte[] { (byte) 91 })));
        assertEquals("XA==", new String(Base64.encodeBase64(new byte[] { (byte) 92 })));
        assertEquals("XQ==", new String(Base64.encodeBase64(new byte[] { (byte) 93 })));
        assertEquals("Xg==", new String(Base64.encodeBase64(new byte[] { (byte) 94 })));
        assertEquals("Xw==", new String(Base64.encodeBase64(new byte[] { (byte) 95 })));
        assertEquals("YA==", new String(Base64.encodeBase64(new byte[] { (byte) 96 })));
        assertEquals("YQ==", new String(Base64.encodeBase64(new byte[] { (byte) 97 })));
        assertEquals("Yg==", new String(Base64.encodeBase64(new byte[] { (byte) 98 })));
        assertEquals("Yw==", new String(Base64.encodeBase64(new byte[] { (byte) 99 })));
        assertEquals("ZA==", new String(Base64.encodeBase64(new byte[] { (byte) 100 })));
        assertEquals("ZQ==", new String(Base64.encodeBase64(new byte[] { (byte) 101 })));
        assertEquals("Zg==", new String(Base64.encodeBase64(new byte[] { (byte) 102 })));
        assertEquals("Zw==", new String(Base64.encodeBase64(new byte[] { (byte) 103 })));
        assertEquals("aA==", new String(Base64.encodeBase64(new byte[] { (byte) 104 })));
        for (int i = -128; i <= 127; i++) {
            final byte test[] = { (byte) i };
            assertTrue(Arrays.equals(test, Base64.decodeBase64(Base64.encodeBase64(test))));
        }
    }

// org.apache.commons.codec.binary.Base64Test::testSingletonsChunked
    public void testSingletonsChunked() {
        assertEquals("AA==\r\n", new String(Base64.encodeBase64Chunked(new byte[] { (byte) 0 })));
        assertEquals("AQ==\r\n", new String(Base64.encodeBase64Chunked(new byte[] { (byte) 1 })));
        assertEquals("Ag==\r\n", new String(Base64.encodeBase64Chunked(new byte[] { (byte) 2 })));
        assertEquals("Aw==\r\n", new String(Base64.encodeBase64Chunked(new byte[] { (byte) 3 })));
        assertEquals("BA==\r\n", new String(Base64.encodeBase64Chunked(new byte[] { (byte) 4 })));
        assertEquals("BQ==\r\n", new String(Base64.encodeBase64Chunked(new byte[] { (byte) 5 })));
        assertEquals("Bg==\r\n", new String(Base64.encodeBase64Chunked(new byte[] { (byte) 6 })));
        assertEquals("Bw==\r\n", new String(Base64.encodeBase64Chunked(new byte[] { (byte) 7 })));
        assertEquals("CA==\r\n", new String(Base64.encodeBase64Chunked(new byte[] { (byte) 8 })));
        assertEquals("CQ==\r\n", new String(Base64.encodeBase64Chunked(new byte[] { (byte) 9 })));
        assertEquals("Cg==\r\n", new String(Base64.encodeBase64Chunked(new byte[] { (byte) 10 })));
        assertEquals("Cw==\r\n", new String(Base64.encodeBase64Chunked(new byte[] { (byte) 11 })));
        assertEquals("DA==\r\n", new String(Base64.encodeBase64Chunked(new byte[] { (byte) 12 })));
        assertEquals("DQ==\r\n", new String(Base64.encodeBase64Chunked(new byte[] { (byte) 13 })));
        assertEquals("Dg==\r\n", new String(Base64.encodeBase64Chunked(new byte[] { (byte) 14 })));
        assertEquals("Dw==\r\n", new String(Base64.encodeBase64Chunked(new byte[] { (byte) 15 })));
        assertEquals("EA==\r\n", new String(Base64.encodeBase64Chunked(new byte[] { (byte) 16 })));
        assertEquals("EQ==\r\n", new String(Base64.encodeBase64Chunked(new byte[] { (byte) 17 })));
        assertEquals("Eg==\r\n", new String(Base64.encodeBase64Chunked(new byte[] { (byte) 18 })));
        assertEquals("Ew==\r\n", new String(Base64.encodeBase64Chunked(new byte[] { (byte) 19 })));
        assertEquals("FA==\r\n", new String(Base64.encodeBase64Chunked(new byte[] { (byte) 20 })));
        assertEquals("FQ==\r\n", new String(Base64.encodeBase64Chunked(new byte[] { (byte) 21 })));
        assertEquals("Fg==\r\n", new String(Base64.encodeBase64Chunked(new byte[] { (byte) 22 })));
        assertEquals("Fw==\r\n", new String(Base64.encodeBase64Chunked(new byte[] { (byte) 23 })));
        assertEquals("GA==\r\n", new String(Base64.encodeBase64Chunked(new byte[] { (byte) 24 })));
        assertEquals("GQ==\r\n", new String(Base64.encodeBase64Chunked(new byte[] { (byte) 25 })));
        assertEquals("Gg==\r\n", new String(Base64.encodeBase64Chunked(new byte[] { (byte) 26 })));
        assertEquals("Gw==\r\n", new String(Base64.encodeBase64Chunked(new byte[] { (byte) 27 })));
        assertEquals("HA==\r\n", new String(Base64.encodeBase64Chunked(new byte[] { (byte) 28 })));
        assertEquals("HQ==\r\n", new String(Base64.encodeBase64Chunked(new byte[] { (byte) 29 })));
        assertEquals("Hg==\r\n", new String(Base64.encodeBase64Chunked(new byte[] { (byte) 30 })));
        assertEquals("Hw==\r\n", new String(Base64.encodeBase64Chunked(new byte[] { (byte) 31 })));
        assertEquals("IA==\r\n", new String(Base64.encodeBase64Chunked(new byte[] { (byte) 32 })));
        assertEquals("IQ==\r\n", new String(Base64.encodeBase64Chunked(new byte[] { (byte) 33 })));
        assertEquals("Ig==\r\n", new String(Base64.encodeBase64Chunked(new byte[] { (byte) 34 })));
        assertEquals("Iw==\r\n", new String(Base64.encodeBase64Chunked(new byte[] { (byte) 35 })));
        assertEquals("JA==\r\n", new String(Base64.encodeBase64Chunked(new byte[] { (byte) 36 })));
        assertEquals("JQ==\r\n", new String(Base64.encodeBase64Chunked(new byte[] { (byte) 37 })));
        assertEquals("Jg==\r\n", new String(Base64.encodeBase64Chunked(new byte[] { (byte) 38 })));
        assertEquals("Jw==\r\n", new String(Base64.encodeBase64Chunked(new byte[] { (byte) 39 })));
        assertEquals("KA==\r\n", new String(Base64.encodeBase64Chunked(new byte[] { (byte) 40 })));
        assertEquals("KQ==\r\n", new String(Base64.encodeBase64Chunked(new byte[] { (byte) 41 })));
        assertEquals("Kg==\r\n", new String(Base64.encodeBase64Chunked(new byte[] { (byte) 42 })));
        assertEquals("Kw==\r\n", new String(Base64.encodeBase64Chunked(new byte[] { (byte) 43 })));
        assertEquals("LA==\r\n", new String(Base64.encodeBase64Chunked(new byte[] { (byte) 44 })));
        assertEquals("LQ==\r\n", new String(Base64.encodeBase64Chunked(new byte[] { (byte) 45 })));
        assertEquals("Lg==\r\n", new String(Base64.encodeBase64Chunked(new byte[] { (byte) 46 })));
        assertEquals("Lw==\r\n", new String(Base64.encodeBase64Chunked(new byte[] { (byte) 47 })));
        assertEquals("MA==\r\n", new String(Base64.encodeBase64Chunked(new byte[] { (byte) 48 })));
        assertEquals("MQ==\r\n", new String(Base64.encodeBase64Chunked(new byte[] { (byte) 49 })));
        assertEquals("Mg==\r\n", new String(Base64.encodeBase64Chunked(new byte[] { (byte) 50 })));
        assertEquals("Mw==\r\n", new String(Base64.encodeBase64Chunked(new byte[] { (byte) 51 })));
        assertEquals("NA==\r\n", new String(Base64.encodeBase64Chunked(new byte[] { (byte) 52 })));
        assertEquals("NQ==\r\n", new String(Base64.encodeBase64Chunked(new byte[] { (byte) 53 })));
        assertEquals("Ng==\r\n", new String(Base64.encodeBase64Chunked(new byte[] { (byte) 54 })));
        assertEquals("Nw==\r\n", new String(Base64.encodeBase64Chunked(new byte[] { (byte) 55 })));
        assertEquals("OA==\r\n", new String(Base64.encodeBase64Chunked(new byte[] { (byte) 56 })));
        assertEquals("OQ==\r\n", new String(Base64.encodeBase64Chunked(new byte[] { (byte) 57 })));
        assertEquals("Og==\r\n", new String(Base64.encodeBase64Chunked(new byte[] { (byte) 58 })));
        assertEquals("Ow==\r\n", new String(Base64.encodeBase64Chunked(new byte[] { (byte) 59 })));
        assertEquals("PA==\r\n", new String(Base64.encodeBase64Chunked(new byte[] { (byte) 60 })));
        assertEquals("PQ==\r\n", new String(Base64.encodeBase64Chunked(new byte[] { (byte) 61 })));
        assertEquals("Pg==\r\n", new String(Base64.encodeBase64Chunked(new byte[] { (byte) 62 })));
        assertEquals("Pw==\r\n", new String(Base64.encodeBase64Chunked(new byte[] { (byte) 63 })));
        assertEquals("QA==\r\n", new String(Base64.encodeBase64Chunked(new byte[] { (byte) 64 })));
        assertEquals("QQ==\r\n", new String(Base64.encodeBase64Chunked(new byte[] { (byte) 65 })));
        assertEquals("Qg==\r\n", new String(Base64.encodeBase64Chunked(new byte[] { (byte) 66 })));
        assertEquals("Qw==\r\n", new String(Base64.encodeBase64Chunked(new byte[] { (byte) 67 })));
        assertEquals("RA==\r\n", new String(Base64.encodeBase64Chunked(new byte[] { (byte) 68 })));
        assertEquals("RQ==\r\n", new String(Base64.encodeBase64Chunked(new byte[] { (byte) 69 })));
        assertEquals("Rg==\r\n", new String(Base64.encodeBase64Chunked(new byte[] { (byte) 70 })));
        assertEquals("Rw==\r\n", new String(Base64.encodeBase64Chunked(new byte[] { (byte) 71 })));
        assertEquals("SA==\r\n", new String(Base64.encodeBase64Chunked(new byte[] { (byte) 72 })));
        assertEquals("SQ==\r\n", new String(Base64.encodeBase64Chunked(new byte[] { (byte) 73 })));
        assertEquals("Sg==\r\n", new String(Base64.encodeBase64Chunked(new byte[] { (byte) 74 })));
        assertEquals("Sw==\r\n", new String(Base64.encodeBase64Chunked(new byte[] { (byte) 75 })));
        assertEquals("TA==\r\n", new String(Base64.encodeBase64Chunked(new byte[] { (byte) 76 })));
        assertEquals("TQ==\r\n", new String(Base64.encodeBase64Chunked(new byte[] { (byte) 77 })));
        assertEquals("Tg==\r\n", new String(Base64.encodeBase64Chunked(new byte[] { (byte) 78 })));
        assertEquals("Tw==\r\n", new String(Base64.encodeBase64Chunked(new byte[] { (byte) 79 })));
        assertEquals("UA==\r\n", new String(Base64.encodeBase64Chunked(new byte[] { (byte) 80 })));
        assertEquals("UQ==\r\n", new String(Base64.encodeBase64Chunked(new byte[] { (byte) 81 })));
        assertEquals("Ug==\r\n", new String(Base64.encodeBase64Chunked(new byte[] { (byte) 82 })));
        assertEquals("Uw==\r\n", new String(Base64.encodeBase64Chunked(new byte[] { (byte) 83 })));
        assertEquals("VA==\r\n", new String(Base64.encodeBase64Chunked(new byte[] { (byte) 84 })));
        assertEquals("VQ==\r\n", new String(Base64.encodeBase64Chunked(new byte[] { (byte) 85 })));
        assertEquals("Vg==\r\n", new String(Base64.encodeBase64Chunked(new byte[] { (byte) 86 })));
        assertEquals("Vw==\r\n", new String(Base64.encodeBase64Chunked(new byte[] { (byte) 87 })));
        assertEquals("WA==\r\n", new String(Base64.encodeBase64Chunked(new byte[] { (byte) 88 })));
        assertEquals("WQ==\r\n", new String(Base64.encodeBase64Chunked(new byte[] { (byte) 89 })));
        assertEquals("Wg==\r\n", new String(Base64.encodeBase64Chunked(new byte[] { (byte) 90 })));
        assertEquals("Ww==\r\n", new String(Base64.encodeBase64Chunked(new byte[] { (byte) 91 })));
        assertEquals("XA==\r\n", new String(Base64.encodeBase64Chunked(new byte[] { (byte) 92 })));
        assertEquals("XQ==\r\n", new String(Base64.encodeBase64Chunked(new byte[] { (byte) 93 })));
        assertEquals("Xg==\r\n", new String(Base64.encodeBase64Chunked(new byte[] { (byte) 94 })));
        assertEquals("Xw==\r\n", new String(Base64.encodeBase64Chunked(new byte[] { (byte) 95 })));
        assertEquals("YA==\r\n", new String(Base64.encodeBase64Chunked(new byte[] { (byte) 96 })));
        assertEquals("YQ==\r\n", new String(Base64.encodeBase64Chunked(new byte[] { (byte) 97 })));
        assertEquals("Yg==\r\n", new String(Base64.encodeBase64Chunked(new byte[] { (byte) 98 })));
        assertEquals("Yw==\r\n", new String(Base64.encodeBase64Chunked(new byte[] { (byte) 99 })));
        assertEquals("ZA==\r\n", new String(Base64.encodeBase64Chunked(new byte[] { (byte) 100 })));
        assertEquals("ZQ==\r\n", new String(Base64.encodeBase64Chunked(new byte[] { (byte) 101 })));
        assertEquals("Zg==\r\n", new String(Base64.encodeBase64Chunked(new byte[] { (byte) 102 })));
        assertEquals("Zw==\r\n", new String(Base64.encodeBase64Chunked(new byte[] { (byte) 103 })));
        assertEquals("aA==\r\n", new String(Base64.encodeBase64Chunked(new byte[] { (byte) 104 })));
    }

// org.apache.commons.codec.binary.Base64Test::testTriplets
    public void testTriplets() {
        assertEquals("AAAA", new String(Base64.encodeBase64(new byte[] { (byte) 0, (byte) 0, (byte) 0 })));
        assertEquals("AAAB", new String(Base64.encodeBase64(new byte[] { (byte) 0, (byte) 0, (byte) 1 })));
        assertEquals("AAAC", new String(Base64.encodeBase64(new byte[] { (byte) 0, (byte) 0, (byte) 2 })));
        assertEquals("AAAD", new String(Base64.encodeBase64(new byte[] { (byte) 0, (byte) 0, (byte) 3 })));
        assertEquals("AAAE", new String(Base64.encodeBase64(new byte[] { (byte) 0, (byte) 0, (byte) 4 })));
        assertEquals("AAAF", new String(Base64.encodeBase64(new byte[] { (byte) 0, (byte) 0, (byte) 5 })));
        assertEquals("AAAG", new String(Base64.encodeBase64(new byte[] { (byte) 0, (byte) 0, (byte) 6 })));
        assertEquals("AAAH", new String(Base64.encodeBase64(new byte[] { (byte) 0, (byte) 0, (byte) 7 })));
        assertEquals("AAAI", new String(Base64.encodeBase64(new byte[] { (byte) 0, (byte) 0, (byte) 8 })));
        assertEquals("AAAJ", new String(Base64.encodeBase64(new byte[] { (byte) 0, (byte) 0, (byte) 9 })));
        assertEquals("AAAK", new String(Base64.encodeBase64(new byte[] { (byte) 0, (byte) 0, (byte) 10 })));
        assertEquals("AAAL", new String(Base64.encodeBase64(new byte[] { (byte) 0, (byte) 0, (byte) 11 })));
        assertEquals("AAAM", new String(Base64.encodeBase64(new byte[] { (byte) 0, (byte) 0, (byte) 12 })));
        assertEquals("AAAN", new String(Base64.encodeBase64(new byte[] { (byte) 0, (byte) 0, (byte) 13 })));
        assertEquals("AAAO", new String(Base64.encodeBase64(new byte[] { (byte) 0, (byte) 0, (byte) 14 })));
        assertEquals("AAAP", new String(Base64.encodeBase64(new byte[] { (byte) 0, (byte) 0, (byte) 15 })));
        assertEquals("AAAQ", new String(Base64.encodeBase64(new byte[] { (byte) 0, (byte) 0, (byte) 16 })));
        assertEquals("AAAR", new String(Base64.encodeBase64(new byte[] { (byte) 0, (byte) 0, (byte) 17 })));
        assertEquals("AAAS", new String(Base64.encodeBase64(new byte[] { (byte) 0, (byte) 0, (byte) 18 })));
        assertEquals("AAAT", new String(Base64.encodeBase64(new byte[] { (byte) 0, (byte) 0, (byte) 19 })));
        assertEquals("AAAU", new String(Base64.encodeBase64(new byte[] { (byte) 0, (byte) 0, (byte) 20 })));
        assertEquals("AAAV", new String(Base64.encodeBase64(new byte[] { (byte) 0, (byte) 0, (byte) 21 })));
        assertEquals("AAAW", new String(Base64.encodeBase64(new byte[] { (byte) 0, (byte) 0, (byte) 22 })));
        assertEquals("AAAX", new String(Base64.encodeBase64(new byte[] { (byte) 0, (byte) 0, (byte) 23 })));
        assertEquals("AAAY", new String(Base64.encodeBase64(new byte[] { (byte) 0, (byte) 0, (byte) 24 })));
        assertEquals("AAAZ", new String(Base64.encodeBase64(new byte[] { (byte) 0, (byte) 0, (byte) 25 })));
        assertEquals("AAAa", new String(Base64.encodeBase64(new byte[] { (byte) 0, (byte) 0, (byte) 26 })));
        assertEquals("AAAb", new String(Base64.encodeBase64(new byte[] { (byte) 0, (byte) 0, (byte) 27 })));
        assertEquals("AAAc", new String(Base64.encodeBase64(new byte[] { (byte) 0, (byte) 0, (byte) 28 })));
        assertEquals("AAAd", new String(Base64.encodeBase64(new byte[] { (byte) 0, (byte) 0, (byte) 29 })));
        assertEquals("AAAe", new String(Base64.encodeBase64(new byte[] { (byte) 0, (byte) 0, (byte) 30 })));
        assertEquals("AAAf", new String(Base64.encodeBase64(new byte[] { (byte) 0, (byte) 0, (byte) 31 })));
        assertEquals("AAAg", new String(Base64.encodeBase64(new byte[] { (byte) 0, (byte) 0, (byte) 32 })));
        assertEquals("AAAh", new String(Base64.encodeBase64(new byte[] { (byte) 0, (byte) 0, (byte) 33 })));
        assertEquals("AAAi", new String(Base64.encodeBase64(new byte[] { (byte) 0, (byte) 0, (byte) 34 })));
        assertEquals("AAAj", new String(Base64.encodeBase64(new byte[] { (byte) 0, (byte) 0, (byte) 35 })));
        assertEquals("AAAk", new String(Base64.encodeBase64(new byte[] { (byte) 0, (byte) 0, (byte) 36 })));
        assertEquals("AAAl", new String(Base64.encodeBase64(new byte[] { (byte) 0, (byte) 0, (byte) 37 })));
        assertEquals("AAAm", new String(Base64.encodeBase64(new byte[] { (byte) 0, (byte) 0, (byte) 38 })));
        assertEquals("AAAn", new String(Base64.encodeBase64(new byte[] { (byte) 0, (byte) 0, (byte) 39 })));
        assertEquals("AAAo", new String(Base64.encodeBase64(new byte[] { (byte) 0, (byte) 0, (byte) 40 })));
        assertEquals("AAAp", new String(Base64.encodeBase64(new byte[] { (byte) 0, (byte) 0, (byte) 41 })));
        assertEquals("AAAq", new String(Base64.encodeBase64(new byte[] { (byte) 0, (byte) 0, (byte) 42 })));
        assertEquals("AAAr", new String(Base64.encodeBase64(new byte[] { (byte) 0, (byte) 0, (byte) 43 })));
        assertEquals("AAAs", new String(Base64.encodeBase64(new byte[] { (byte) 0, (byte) 0, (byte) 44 })));
        assertEquals("AAAt", new String(Base64.encodeBase64(new byte[] { (byte) 0, (byte) 0, (byte) 45 })));
        assertEquals("AAAu", new String(Base64.encodeBase64(new byte[] { (byte) 0, (byte) 0, (byte) 46 })));
        assertEquals("AAAv", new String(Base64.encodeBase64(new byte[] { (byte) 0, (byte) 0, (byte) 47 })));
        assertEquals("AAAw", new String(Base64.encodeBase64(new byte[] { (byte) 0, (byte) 0, (byte) 48 })));
        assertEquals("AAAx", new String(Base64.encodeBase64(new byte[] { (byte) 0, (byte) 0, (byte) 49 })));
        assertEquals("AAAy", new String(Base64.encodeBase64(new byte[] { (byte) 0, (byte) 0, (byte) 50 })));
        assertEquals("AAAz", new String(Base64.encodeBase64(new byte[] { (byte) 0, (byte) 0, (byte) 51 })));
        assertEquals("AAA0", new String(Base64.encodeBase64(new byte[] { (byte) 0, (byte) 0, (byte) 52 })));
        assertEquals("AAA1", new String(Base64.encodeBase64(new byte[] { (byte) 0, (byte) 0, (byte) 53 })));
        assertEquals("AAA2", new String(Base64.encodeBase64(new byte[] { (byte) 0, (byte) 0, (byte) 54 })));
        assertEquals("AAA3", new String(Base64.encodeBase64(new byte[] { (byte) 0, (byte) 0, (byte) 55 })));
        assertEquals("AAA4", new String(Base64.encodeBase64(new byte[] { (byte) 0, (byte) 0, (byte) 56 })));
        assertEquals("AAA5", new String(Base64.encodeBase64(new byte[] { (byte) 0, (byte) 0, (byte) 57 })));
        assertEquals("AAA6", new String(Base64.encodeBase64(new byte[] { (byte) 0, (byte) 0, (byte) 58 })));
        assertEquals("AAA7", new String(Base64.encodeBase64(new byte[] { (byte) 0, (byte) 0, (byte) 59 })));
        assertEquals("AAA8", new String(Base64.encodeBase64(new byte[] { (byte) 0, (byte) 0, (byte) 60 })));
        assertEquals("AAA9", new String(Base64.encodeBase64(new byte[] { (byte) 0, (byte) 0, (byte) 61 })));
        assertEquals("AAA+", new String(Base64.encodeBase64(new byte[] { (byte) 0, (byte) 0, (byte) 62 })));
        assertEquals("AAA/", new String(Base64.encodeBase64(new byte[] { (byte) 0, (byte) 0, (byte) 63 })));
    }

// org.apache.commons.codec.binary.Base64Test::testTripletsChunked
    public void testTripletsChunked() {
        assertEquals("AAAA\r\n", new String(Base64.encodeBase64Chunked(new byte[] { (byte) 0, (byte) 0, (byte) 0 })));
        assertEquals("AAAB\r\n", new String(Base64.encodeBase64Chunked(new byte[] { (byte) 0, (byte) 0, (byte) 1 })));
        assertEquals("AAAC\r\n", new String(Base64.encodeBase64Chunked(new byte[] { (byte) 0, (byte) 0, (byte) 2 })));
        assertEquals("AAAD\r\n", new String(Base64.encodeBase64Chunked(new byte[] { (byte) 0, (byte) 0, (byte) 3 })));
        assertEquals("AAAE\r\n", new String(Base64.encodeBase64Chunked(new byte[] { (byte) 0, (byte) 0, (byte) 4 })));
        assertEquals("AAAF\r\n", new String(Base64.encodeBase64Chunked(new byte[] { (byte) 0, (byte) 0, (byte) 5 })));
        assertEquals("AAAG\r\n", new String(Base64.encodeBase64Chunked(new byte[] { (byte) 0, (byte) 0, (byte) 6 })));
        assertEquals("AAAH\r\n", new String(Base64.encodeBase64Chunked(new byte[] { (byte) 0, (byte) 0, (byte) 7 })));
        assertEquals("AAAI\r\n", new String(Base64.encodeBase64Chunked(new byte[] { (byte) 0, (byte) 0, (byte) 8 })));
        assertEquals("AAAJ\r\n", new String(Base64.encodeBase64Chunked(new byte[] { (byte) 0, (byte) 0, (byte) 9 })));
        assertEquals("AAAK\r\n", new String(Base64.encodeBase64Chunked(new byte[] { (byte) 0, (byte) 0, (byte) 10 })));
        assertEquals("AAAL\r\n", new String(Base64.encodeBase64Chunked(new byte[] { (byte) 0, (byte) 0, (byte) 11 })));
        assertEquals("AAAM\r\n", new String(Base64.encodeBase64Chunked(new byte[] { (byte) 0, (byte) 0, (byte) 12 })));
        assertEquals("AAAN\r\n", new String(Base64.encodeBase64Chunked(new byte[] { (byte) 0, (byte) 0, (byte) 13 })));
        assertEquals("AAAO\r\n", new String(Base64.encodeBase64Chunked(new byte[] { (byte) 0, (byte) 0, (byte) 14 })));
        assertEquals("AAAP\r\n", new String(Base64.encodeBase64Chunked(new byte[] { (byte) 0, (byte) 0, (byte) 15 })));
        assertEquals("AAAQ\r\n", new String(Base64.encodeBase64Chunked(new byte[] { (byte) 0, (byte) 0, (byte) 16 })));
        assertEquals("AAAR\r\n", new String(Base64.encodeBase64Chunked(new byte[] { (byte) 0, (byte) 0, (byte) 17 })));
        assertEquals("AAAS\r\n", new String(Base64.encodeBase64Chunked(new byte[] { (byte) 0, (byte) 0, (byte) 18 })));
        assertEquals("AAAT\r\n", new String(Base64.encodeBase64Chunked(new byte[] { (byte) 0, (byte) 0, (byte) 19 })));
        assertEquals("AAAU\r\n", new String(Base64.encodeBase64Chunked(new byte[] { (byte) 0, (byte) 0, (byte) 20 })));
        assertEquals("AAAV\r\n", new String(Base64.encodeBase64Chunked(new byte[] { (byte) 0, (byte) 0, (byte) 21 })));
        assertEquals("AAAW\r\n", new String(Base64.encodeBase64Chunked(new byte[] { (byte) 0, (byte) 0, (byte) 22 })));
        assertEquals("AAAX\r\n", new String(Base64.encodeBase64Chunked(new byte[] { (byte) 0, (byte) 0, (byte) 23 })));
        assertEquals("AAAY\r\n", new String(Base64.encodeBase64Chunked(new byte[] { (byte) 0, (byte) 0, (byte) 24 })));
        assertEquals("AAAZ\r\n", new String(Base64.encodeBase64Chunked(new byte[] { (byte) 0, (byte) 0, (byte) 25 })));
        assertEquals("AAAa\r\n", new String(Base64.encodeBase64Chunked(new byte[] { (byte) 0, (byte) 0, (byte) 26 })));
        assertEquals("AAAb\r\n", new String(Base64.encodeBase64Chunked(new byte[] { (byte) 0, (byte) 0, (byte) 27 })));
        assertEquals("AAAc\r\n", new String(Base64.encodeBase64Chunked(new byte[] { (byte) 0, (byte) 0, (byte) 28 })));
        assertEquals("AAAd\r\n", new String(Base64.encodeBase64Chunked(new byte[] { (byte) 0, (byte) 0, (byte) 29 })));
        assertEquals("AAAe\r\n", new String(Base64.encodeBase64Chunked(new byte[] { (byte) 0, (byte) 0, (byte) 30 })));
        assertEquals("AAAf\r\n", new String(Base64.encodeBase64Chunked(new byte[] { (byte) 0, (byte) 0, (byte) 31 })));
        assertEquals("AAAg\r\n", new String(Base64.encodeBase64Chunked(new byte[] { (byte) 0, (byte) 0, (byte) 32 })));
        assertEquals("AAAh\r\n", new String(Base64.encodeBase64Chunked(new byte[] { (byte) 0, (byte) 0, (byte) 33 })));
        assertEquals("AAAi\r\n", new String(Base64.encodeBase64Chunked(new byte[] { (byte) 0, (byte) 0, (byte) 34 })));
        assertEquals("AAAj\r\n", new String(Base64.encodeBase64Chunked(new byte[] { (byte) 0, (byte) 0, (byte) 35 })));
        assertEquals("AAAk\r\n", new String(Base64.encodeBase64Chunked(new byte[] { (byte) 0, (byte) 0, (byte) 36 })));
        assertEquals("AAAl\r\n", new String(Base64.encodeBase64Chunked(new byte[] { (byte) 0, (byte) 0, (byte) 37 })));
        assertEquals("AAAm\r\n", new String(Base64.encodeBase64Chunked(new byte[] { (byte) 0, (byte) 0, (byte) 38 })));
        assertEquals("AAAn\r\n", new String(Base64.encodeBase64Chunked(new byte[] { (byte) 0, (byte) 0, (byte) 39 })));
        assertEquals("AAAo\r\n", new String(Base64.encodeBase64Chunked(new byte[] { (byte) 0, (byte) 0, (byte) 40 })));
        assertEquals("AAAp\r\n", new String(Base64.encodeBase64Chunked(new byte[] { (byte) 0, (byte) 0, (byte) 41 })));
        assertEquals("AAAq\r\n", new String(Base64.encodeBase64Chunked(new byte[] { (byte) 0, (byte) 0, (byte) 42 })));
        assertEquals("AAAr\r\n", new String(Base64.encodeBase64Chunked(new byte[] { (byte) 0, (byte) 0, (byte) 43 })));
        assertEquals("AAAs\r\n", new String(Base64.encodeBase64Chunked(new byte[] { (byte) 0, (byte) 0, (byte) 44 })));
        assertEquals("AAAt\r\n", new String(Base64.encodeBase64Chunked(new byte[] { (byte) 0, (byte) 0, (byte) 45 })));
        assertEquals("AAAu\r\n", new String(Base64.encodeBase64Chunked(new byte[] { (byte) 0, (byte) 0, (byte) 46 })));
        assertEquals("AAAv\r\n", new String(Base64.encodeBase64Chunked(new byte[] { (byte) 0, (byte) 0, (byte) 47 })));
        assertEquals("AAAw\r\n", new String(Base64.encodeBase64Chunked(new byte[] { (byte) 0, (byte) 0, (byte) 48 })));
        assertEquals("AAAx\r\n", new String(Base64.encodeBase64Chunked(new byte[] { (byte) 0, (byte) 0, (byte) 49 })));
        assertEquals("AAAy\r\n", new String(Base64.encodeBase64Chunked(new byte[] { (byte) 0, (byte) 0, (byte) 50 })));
        assertEquals("AAAz\r\n", new String(Base64.encodeBase64Chunked(new byte[] { (byte) 0, (byte) 0, (byte) 51 })));
        assertEquals("AAA0\r\n", new String(Base64.encodeBase64Chunked(new byte[] { (byte) 0, (byte) 0, (byte) 52 })));
        assertEquals("AAA1\r\n", new String(Base64.encodeBase64Chunked(new byte[] { (byte) 0, (byte) 0, (byte) 53 })));
        assertEquals("AAA2\r\n", new String(Base64.encodeBase64Chunked(new byte[] { (byte) 0, (byte) 0, (byte) 54 })));
        assertEquals("AAA3\r\n", new String(Base64.encodeBase64Chunked(new byte[] { (byte) 0, (byte) 0, (byte) 55 })));
        assertEquals("AAA4\r\n", new String(Base64.encodeBase64Chunked(new byte[] { (byte) 0, (byte) 0, (byte) 56 })));
        assertEquals("AAA5\r\n", new String(Base64.encodeBase64Chunked(new byte[] { (byte) 0, (byte) 0, (byte) 57 })));
        assertEquals("AAA6\r\n", new String(Base64.encodeBase64Chunked(new byte[] { (byte) 0, (byte) 0, (byte) 58 })));
        assertEquals("AAA7\r\n", new String(Base64.encodeBase64Chunked(new byte[] { (byte) 0, (byte) 0, (byte) 59 })));
        assertEquals("AAA8\r\n", new String(Base64.encodeBase64Chunked(new byte[] { (byte) 0, (byte) 0, (byte) 60 })));
        assertEquals("AAA9\r\n", new String(Base64.encodeBase64Chunked(new byte[] { (byte) 0, (byte) 0, (byte) 61 })));
        assertEquals("AAA+\r\n", new String(Base64.encodeBase64Chunked(new byte[] { (byte) 0, (byte) 0, (byte) 62 })));
        assertEquals("AAA/\r\n", new String(Base64.encodeBase64Chunked(new byte[] { (byte) 0, (byte) 0, (byte) 63 })));
    }

// org.apache.commons.codec.binary.Base64Test::testUrlSafe
    public void testUrlSafe() {
        
        for (int i = 0; i <= 150; i++) {
            final byte[][] randomData = Base64TestData.randomData(i, true);
            final byte[] encoded = randomData[1];
            final byte[] decoded = randomData[0];
            final byte[] result = Base64.decodeBase64(encoded);
            assertTrue("url-safe i=" + i, Arrays.equals(decoded, result));
            assertFalse("url-safe i=" + i + " no '='", Base64TestData.bytesContain(encoded, (byte) '='));
            assertFalse("url-safe i=" + i + " no '\\'", Base64TestData.bytesContain(encoded, (byte) '\\'));
            assertFalse("url-safe i=" + i + " no '+'", Base64TestData.bytesContain(encoded, (byte) '+'));
        }

    }

// org.apache.commons.codec.binary.Base64Test::testUUID
    public void testUUID() throws DecoderException {
        
        
        final byte[][] ids = new byte[4][];

        
        ids[0] = Hex.decodeHex("94ed8d0319e4493399560fb67404d370");

        
        ids[1] = Hex.decodeHex("2bf7cc2701fe4397b49ebeed5acc7090");

        
        ids[2] = Hex.decodeHex("64be154b6ffa40258d1a01288e7c31ca");

        
        
        ids[3] = Hex.decodeHex("ff7f8fc01cdb471a8c8b5a9306183fe8");

        final byte[][] standard = new byte[4][];
        standard[0] = StringUtils.getBytesUtf8("lO2NAxnkSTOZVg+2dATTcA==");
        standard[1] = StringUtils.getBytesUtf8("K/fMJwH+Q5e0nr7tWsxwkA==");
        standard[2] = StringUtils.getBytesUtf8("ZL4VS2/6QCWNGgEojnwxyg==");
        standard[3] = StringUtils.getBytesUtf8("/3+PwBzbRxqMi1qTBhg/6A==");

        final byte[][] urlSafe1 = new byte[4][];
        
        urlSafe1[0] = StringUtils.getBytesUtf8("lO2NAxnkSTOZVg-2dATTcA==");
        urlSafe1[1] = StringUtils.getBytesUtf8("K_fMJwH-Q5e0nr7tWsxwkA==");
        urlSafe1[2] = StringUtils.getBytesUtf8("ZL4VS2_6QCWNGgEojnwxyg==");
        urlSafe1[3] = StringUtils.getBytesUtf8("_3-PwBzbRxqMi1qTBhg_6A==");

        final byte[][] urlSafe2 = new byte[4][];
        
        urlSafe2[0] = StringUtils.getBytesUtf8("lO2NAxnkSTOZVg-2dATTcA=");
        urlSafe2[1] = StringUtils.getBytesUtf8("K_fMJwH-Q5e0nr7tWsxwkA=");
        urlSafe2[2] = StringUtils.getBytesUtf8("ZL4VS2_6QCWNGgEojnwxyg=");
        urlSafe2[3] = StringUtils.getBytesUtf8("_3-PwBzbRxqMi1qTBhg_6A=");

        final byte[][] urlSafe3 = new byte[4][];
        
        urlSafe3[0] = StringUtils.getBytesUtf8("lO2NAxnkSTOZVg-2dATTcA");
        urlSafe3[1] = StringUtils.getBytesUtf8("K_fMJwH-Q5e0nr7tWsxwkA");
        urlSafe3[2] = StringUtils.getBytesUtf8("ZL4VS2_6QCWNGgEojnwxyg");
        urlSafe3[3] = StringUtils.getBytesUtf8("_3-PwBzbRxqMi1qTBhg_6A");

        for (int i = 0; i < 4; i++) {
            final byte[] encodedStandard = Base64.encodeBase64(ids[i]);
            final byte[] encodedUrlSafe = Base64.encodeBase64URLSafe(ids[i]);
            final byte[] decodedStandard = Base64.decodeBase64(standard[i]);
            final byte[] decodedUrlSafe1 = Base64.decodeBase64(urlSafe1[i]);
            final byte[] decodedUrlSafe2 = Base64.decodeBase64(urlSafe2[i]);
            final byte[] decodedUrlSafe3 = Base64.decodeBase64(urlSafe3[i]);

            
            

            assertTrue("standard encode uuid", Arrays.equals(encodedStandard, standard[i]));
            assertTrue("url-safe encode uuid", Arrays.equals(encodedUrlSafe, urlSafe3[i]));
            assertTrue("standard decode uuid", Arrays.equals(decodedStandard, ids[i]));
            assertTrue("url-safe1 decode uuid", Arrays.equals(decodedUrlSafe1, ids[i]));
            assertTrue("url-safe2 decode uuid", Arrays.equals(decodedUrlSafe2, ids[i]));
            assertTrue("url-safe3 decode uuid", Arrays.equals(decodedUrlSafe3, ids[i]));
        }
    }

// org.apache.commons.codec.binary.Base64Test::testByteToStringVariations
    public void testByteToStringVariations() throws DecoderException {
        final Base64 base64 = new Base64(0);
        final byte[] b1 = StringUtils.getBytesUtf8("Hello World");
        final byte[] b2 = new byte[0];
        final byte[] b3 = null;
        final byte[] b4 = Hex.decodeHex("2bf7cc2701fe4397b49ebeed5acc7090"); 
                                                                                            
                                                                                            

        assertEquals("byteToString Hello World", "SGVsbG8gV29ybGQ=", base64.encodeToString(b1));
        assertEquals("byteToString static Hello World", "SGVsbG8gV29ybGQ=", Base64.encodeBase64String(b1));
        assertEquals("byteToString \"\"", "", base64.encodeToString(b2));
        assertEquals("byteToString static \"\"", "", Base64.encodeBase64String(b2));
        assertEquals("byteToString null", null, base64.encodeToString(b3));
        assertEquals("byteToString static null", null, Base64.encodeBase64String(b3));
        assertEquals("byteToString UUID", "K/fMJwH+Q5e0nr7tWsxwkA==", base64.encodeToString(b4));
        assertEquals("byteToString static UUID", "K/fMJwH+Q5e0nr7tWsxwkA==", Base64.encodeBase64String(b4));
        assertEquals("byteToString static-url-safe UUID", "K_fMJwH-Q5e0nr7tWsxwkA",
                Base64.encodeBase64URLSafeString(b4));
    }

// org.apache.commons.codec.binary.Base64Test::testStringToByteVariations
    public void testStringToByteVariations() throws DecoderException {
        final Base64 base64 = new Base64();
        final String s1 = "SGVsbG8gV29ybGQ=\r\n";
        final String s2 = "";
        final String s3 = null;
        final String s4a = "K/fMJwH+Q5e0nr7tWsxwkA==\r\n";
        final String s4b = "K_fMJwH-Q5e0nr7tWsxwkA";
        final byte[] b4 = Hex.decodeHex("2bf7cc2701fe4397b49ebeed5acc7090"); 
                                                                                            
                                                                                            

        assertEquals("StringToByte Hello World", "Hello World", StringUtils.newStringUtf8(base64.decode(s1)));
        assertEquals("StringToByte Hello World", "Hello World",
                StringUtils.newStringUtf8((byte[]) base64.decode((Object) s1)));
        assertEquals("StringToByte static Hello World", "Hello World",
                StringUtils.newStringUtf8(Base64.decodeBase64(s1)));
        assertEquals("StringToByte \"\"", "", StringUtils.newStringUtf8(base64.decode(s2)));
        assertEquals("StringToByte static \"\"", "", StringUtils.newStringUtf8(Base64.decodeBase64(s2)));
        assertEquals("StringToByte null", null, StringUtils.newStringUtf8(base64.decode(s3)));
        assertEquals("StringToByte static null", null, StringUtils.newStringUtf8(Base64.decodeBase64(s3)));
        assertTrue("StringToByte UUID", Arrays.equals(b4, base64.decode(s4b)));
        assertTrue("StringToByte static UUID", Arrays.equals(b4, Base64.decodeBase64(s4a)));
        assertTrue("StringToByte static-url-safe UUID", Arrays.equals(b4, Base64.decodeBase64(s4b)));
    }

// org.apache.commons.codec.binary.Base64Test::testHugeLineSeparator
    public void testHugeLineSeparator() {
        final int BaseNCodec_DEFAULT_BUFFER_SIZE = 8192;
        final int Base64_BYTES_PER_ENCODED_BLOCK = 4;
        final byte[] baLineSeparator = new byte[BaseNCodec_DEFAULT_BUFFER_SIZE * 4 - 3];
        final Base64 b64 = new Base64(Base64_BYTES_PER_ENCODED_BLOCK, baLineSeparator);
        final String strOriginal = "Hello World";
        final String strDecoded = new String(b64.decode(b64.encode(StringUtils.getBytesUtf8(strOriginal))));
        assertEquals("testDEFAULT_BUFFER_SIZE", strOriginal, strDecoded);
    }

// org.apache.commons.codec.binary.BaseNCodecTest::testBaseNCodec
    public void testBaseNCodec() {
        assertNotNull(codec);
    }

// org.apache.commons.codec.binary.BaseNCodecTest::testIsWhiteSpace
    public void testIsWhiteSpace() {
        assertTrue(BaseNCodec.isWhiteSpace((byte) ' '));
        assertTrue(BaseNCodec.isWhiteSpace((byte) '\n'));
        assertTrue(BaseNCodec.isWhiteSpace((byte) '\r'));
        assertTrue(BaseNCodec.isWhiteSpace((byte) '\t'));
    }

// org.apache.commons.codec.binary.BaseNCodecTest::testIsInAlphabetByte
    public void testIsInAlphabetByte() {
        assertFalse(codec.isInAlphabet((byte) 0));
        assertFalse(codec.isInAlphabet((byte) 'a'));
        assertTrue(codec.isInAlphabet((byte) 'O'));
        assertTrue(codec.isInAlphabet((byte) 'K'));
    }

// org.apache.commons.codec.binary.BaseNCodecTest::testIsInAlphabetByteArrayBoolean
    public void testIsInAlphabetByteArrayBoolean() {
        assertTrue(codec.isInAlphabet(new byte[]{}, false));
        assertTrue(codec.isInAlphabet(new byte[]{'O'}, false));
        assertFalse(codec.isInAlphabet(new byte[]{'O',' '}, false));
        assertFalse(codec.isInAlphabet(new byte[]{' '}, false));
        assertTrue(codec.isInAlphabet(new byte[]{}, true));
        assertTrue(codec.isInAlphabet(new byte[]{'O'}, true));
        assertTrue(codec.isInAlphabet(new byte[]{'O',' '}, true));
        assertTrue(codec.isInAlphabet(new byte[]{' '}, true));
    }

// org.apache.commons.codec.binary.BaseNCodecTest::testIsInAlphabetString
    public void testIsInAlphabetString() {
        assertTrue(codec.isInAlphabet("OK"));
        assertTrue(codec.isInAlphabet("O=K= \t\n\r"));
    }

// org.apache.commons.codec.binary.BaseNCodecTest::testContainsAlphabetOrPad
    public void testContainsAlphabetOrPad() {
        assertFalse(codec.containsAlphabetOrPad(null));
        assertFalse(codec.containsAlphabetOrPad(new byte[]{}));
        assertTrue(codec.containsAlphabetOrPad("OK".getBytes()));
        assertTrue(codec.containsAlphabetOrPad("OK ".getBytes()));
        assertFalse(codec.containsAlphabetOrPad("ok ".getBytes()));
        assertTrue(codec.containsAlphabetOrPad(new byte[]{codec.pad}));
    }

// org.apache.commons.codec.binary.BaseNCodecTest::testProvidePaddingByte
    public void testProvidePaddingByte() {
        
        codec = new BaseNCodec(0, 0, 0, 0, (byte)0x25) {
            @Override
            protected boolean isInAlphabet(final byte b) {
                return b=='O' || b == 'K'; 
            }

            @Override
            void encode(final byte[] pArray, final int i, final int length, final Context context) {
            }

            @Override
            void decode(final byte[] pArray, final int i, final int length, final Context context) {
            }
        };

        
        final byte actualPaddingByte = codec.pad;

        
        assertEquals(0x25, actualPaddingByte);
    }

// org.apache.commons.codec.binary.HexTest::testCustomCharset
    public void testCustomCharset() throws UnsupportedEncodingException, DecoderException {
        for (final String name : Charset.availableCharsets().keySet()) {
            testCustomCharset(name, "testCustomCharset");
        }
    }

// org.apache.commons.codec.binary.HexTest::testCustomCharsetBadName
    public void testCustomCharsetBadName() {
        new Hex(BAD_ENCODING_NAME);
    }

// org.apache.commons.codec.binary.HexTest::testCustomCharsetToString
    public void testCustomCharsetToString() {
        assertTrue(new Hex().toString().indexOf(Hex.DEFAULT_CHARSET_NAME) >= 0);
    }

// org.apache.commons.codec.binary.HexTest::testDecodeBadCharacterPos0
    public void testDecodeBadCharacterPos0() {
        try {
            new Hex().decode("q0");
            fail("An exception wasn't thrown when trying to decode an illegal character");
        } catch (final DecoderException e) {
            
        }
    }

// org.apache.commons.codec.binary.HexTest::testDecodeBadCharacterPos1
    public void testDecodeBadCharacterPos1() {
        try {
            new Hex().decode("0q");
            fail("An exception wasn't thrown when trying to decode an illegal character");
        } catch (final DecoderException e) {
            
        }
    }

// org.apache.commons.codec.binary.HexTest::testDecodeByteArrayEmpty
    public void testDecodeByteArrayEmpty() throws DecoderException {
        assertTrue(Arrays.equals(new byte[0], new Hex().decode(new byte[0])));
    }

// org.apache.commons.codec.binary.HexTest::testDecodeByteArrayObjectEmpty
    public void testDecodeByteArrayObjectEmpty() throws DecoderException {
        assertTrue(Arrays.equals(new byte[0], (byte[]) new Hex().decode((Object) new byte[0])));
    }

// org.apache.commons.codec.binary.HexTest::testDecodeByteArrayOddCharacters
    public void testDecodeByteArrayOddCharacters() {
        try {
            new Hex().decode(new byte[] { 65 });
            fail("An exception wasn't thrown when trying to decode an odd number of characters");
        } catch (final DecoderException e) {
            
        }
    }

// org.apache.commons.codec.binary.HexTest::testDecodeByteBufferEmpty
    public void testDecodeByteBufferEmpty() throws DecoderException {
        assertTrue(Arrays.equals(new byte[0], new Hex().decode(ByteBuffer.allocate(0))));
    }

// org.apache.commons.codec.binary.HexTest::testDecodeByteBufferObjectEmpty
    public void testDecodeByteBufferObjectEmpty() throws DecoderException {
        assertTrue(Arrays.equals(new byte[0], (byte[]) new Hex().decode((Object) ByteBuffer.allocate(0))));
    }

// org.apache.commons.codec.binary.HexTest::testDecodeByteBufferOddCharacters
    public void testDecodeByteBufferOddCharacters() {
        final ByteBuffer buffer = ByteBuffer.allocate(1);
        buffer.put((byte) 65);
        try {
            new Hex().decode(buffer);
            fail("An exception wasn't thrown when trying to decode an odd number of characters");
        } catch (final DecoderException e) {
            
        }
    }

// org.apache.commons.codec.binary.HexTest::testDecodeHexCharArrayEmpty
    public void testDecodeHexCharArrayEmpty() throws DecoderException {
        assertTrue(Arrays.equals(new byte[0], Hex.decodeHex(new char[0])));
    }

// org.apache.commons.codec.binary.HexTest::testDecodeHexStringEmpty
    public void testDecodeHexStringEmpty() throws DecoderException {
        assertTrue(Arrays.equals(new byte[0], Hex.decodeHex("")));
    }

// org.apache.commons.codec.binary.HexTest::testDecodeClassCastException
    public void testDecodeClassCastException() {
        try {
            new Hex().decode(new int[] { 65 });
            fail("An exception wasn't thrown when trying to decode.");
        } catch (final DecoderException e) {
            
        }
    }

// org.apache.commons.codec.binary.HexTest::testDecodeHexCharArrayOddCharacters1
    public void testDecodeHexCharArrayOddCharacters1() {
        checkDecodeHexCharArrayOddCharacters(new char[] { 'A' });
    }

// org.apache.commons.codec.binary.HexTest::testDecodeHexStringOddCharacters1
    public void testDecodeHexStringOddCharacters1() {
        checkDecodeHexCharArrayOddCharacters("A");
    }

// org.apache.commons.codec.binary.HexTest::testDecodeHexCharArrayOddCharacters3
    public void testDecodeHexCharArrayOddCharacters3() {
        checkDecodeHexCharArrayOddCharacters(new char[] { 'A', 'B', 'C' });
    }

// org.apache.commons.codec.binary.HexTest::testDecodeHexCharArrayOddCharacters5
    public void testDecodeHexCharArrayOddCharacters5() {
        checkDecodeHexCharArrayOddCharacters(new char[] { 'A', 'B', 'C', 'D', 'E' });
    }

// org.apache.commons.codec.binary.HexTest::testDecodeHexStringOddCharacters
    public void testDecodeHexStringOddCharacters() {
        try {
            new Hex().decode("6");
            fail("An exception wasn't thrown when trying to decode an odd number of characters");
        } catch (final DecoderException e) {
            
        }
    }

// org.apache.commons.codec.binary.HexTest::testDecodeStringEmpty
    public void testDecodeStringEmpty() throws DecoderException {
        assertTrue(Arrays.equals(new byte[0], (byte[]) new Hex().decode("")));
    }

// org.apache.commons.codec.binary.HexTest::testEncodeByteArrayEmpty
    public void testEncodeByteArrayEmpty() {
        assertTrue(Arrays.equals(new byte[0], new Hex().encode(new byte[0])));
    }

// org.apache.commons.codec.binary.HexTest::testEncodeByteArrayObjectEmpty
    public void testEncodeByteArrayObjectEmpty() throws EncoderException {
        assertTrue(Arrays.equals(new char[0], (char[]) new Hex().encode((Object) new byte[0])));
    }

// org.apache.commons.codec.binary.HexTest::testEncodeByteBufferEmpty
    public void testEncodeByteBufferEmpty() {
        assertTrue(Arrays.equals(new byte[0], new Hex().encode(ByteBuffer.allocate(0))));
    }

// org.apache.commons.codec.binary.HexTest::testEncodeByteBufferObjectEmpty
    public void testEncodeByteBufferObjectEmpty() throws EncoderException {
        assertTrue(Arrays.equals(new char[0], (char[]) new Hex().encode((Object) ByteBuffer.allocate(0))));
    }

// org.apache.commons.codec.binary.HexTest::testEncodeClassCastException
    public void testEncodeClassCastException() {
        try {
            new Hex().encode(new int[] { 65 });
            fail("An exception wasn't thrown when trying to encode.");
        } catch (final EncoderException e) {
            
        }
    }

// org.apache.commons.codec.binary.HexTest::testEncodeDecodeHexCharArrayRandom
    public void testEncodeDecodeHexCharArrayRandom() throws DecoderException, EncoderException {
        final Random random = new Random();

        final Hex hex = new Hex();
        for (int i = 5; i > 0; i--) {
            final byte[] data = new byte[random.nextInt(10000) + 1];
            random.nextBytes(data);

            
            final char[] encodedChars = Hex.encodeHex(data);
            byte[] decodedBytes = Hex.decodeHex(encodedChars);
            assertTrue(Arrays.equals(data, decodedBytes));

            
            final byte[] encodedStringBytes = hex.encode(data);
            decodedBytes = hex.decode(encodedStringBytes);
            assertTrue(Arrays.equals(data, decodedBytes));

            
            String dataString = new String(encodedChars);
            char[] encodedStringChars = (char[]) hex.encode(dataString);
            decodedBytes = (byte[]) hex.decode(encodedStringChars);
            assertTrue(Arrays.equals(StringUtils.getBytesUtf8(dataString), decodedBytes));

            
            dataString = new String(encodedChars);
            encodedStringChars = (char[]) hex.encode(dataString);
            decodedBytes = (byte[]) hex.decode(new String(encodedStringChars));
            assertTrue(Arrays.equals(StringUtils.getBytesUtf8(dataString), decodedBytes));
        }
    }

// org.apache.commons.codec.binary.HexTest::testEncodeHexByteArrayEmpty
    public void testEncodeHexByteArrayEmpty() {
        assertTrue(Arrays.equals(new char[0], Hex.encodeHex(new byte[0])));
        assertTrue(Arrays.equals(new byte[0], new Hex().encode(new byte[0])));
    }

// org.apache.commons.codec.binary.HexTest::testEncodeHexByteArrayHelloWorldLowerCaseHex
    public void testEncodeHexByteArrayHelloWorldLowerCaseHex() {
        final byte[] b = StringUtils.getBytesUtf8("Hello World");
        final String expected = "48656c6c6f20576f726c64";
        char[] actual;
        actual = Hex.encodeHex(b);
        assertEquals(expected, new String(actual));
        actual = Hex.encodeHex(b, true);
        assertEquals(expected, new String(actual));
        actual = Hex.encodeHex(b, false);
        assertFalse(expected.equals(new String(actual)));
    }

// org.apache.commons.codec.binary.HexTest::testEncodeHexByteArrayHelloWorldUpperCaseHex
    public void testEncodeHexByteArrayHelloWorldUpperCaseHex() {
        final byte[] b = StringUtils.getBytesUtf8("Hello World");
        final String expected = "48656C6C6F20576F726C64";
        char[] actual;
        actual = Hex.encodeHex(b);
        assertFalse(expected.equals(new String(actual)));
        actual = Hex.encodeHex(b, true);
        assertFalse(expected.equals(new String(actual)));
        actual = Hex.encodeHex(b, false);
        assertTrue(expected.equals(new String(actual)));
    }

// org.apache.commons.codec.binary.HexTest::testEncodeHexByteArrayZeroes
    public void testEncodeHexByteArrayZeroes() {
        final char[] c = Hex.encodeHex(new byte[36]);
        assertEquals("000000000000000000000000000000000000000000000000000000000000000000000000", new String(c));
    }

// org.apache.commons.codec.binary.HexTest::testEncodeHexByteBufferEmpty
    public void testEncodeHexByteBufferEmpty() {
        assertTrue(Arrays.equals(new char[0], Hex.encodeHex(ByteBuffer.allocate(0))));
        assertTrue(Arrays.equals(new byte[0], new Hex().encode(ByteBuffer.allocate(0))));
    }

// org.apache.commons.codec.binary.HexTest::testEncodeHexByteBufferHelloWorldLowerCaseHex
    public void testEncodeHexByteBufferHelloWorldLowerCaseHex() {
        final ByteBuffer b = StringUtils.getByteBufferUtf8("Hello World");
        final String expected = "48656c6c6f20576f726c64";
        char[] actual;
        actual = Hex.encodeHex(b);
        assertEquals(expected, new String(actual));
        actual = Hex.encodeHex(b, true);
        assertEquals(expected, new String(actual));
        actual = Hex.encodeHex(b, false);
        assertFalse(expected.equals(new String(actual)));
    }

// org.apache.commons.codec.binary.HexTest::testEncodeHexByteBufferHelloWorldUpperCaseHex
    public void testEncodeHexByteBufferHelloWorldUpperCaseHex() {
        final ByteBuffer b = StringUtils.getByteBufferUtf8("Hello World");
        final String expected = "48656C6C6F20576F726C64";
        char[] actual;
        actual = Hex.encodeHex(b);
        assertFalse(expected.equals(new String(actual)));
        actual = Hex.encodeHex(b, true);
        assertFalse(expected.equals(new String(actual)));
        actual = Hex.encodeHex(b, false);
        assertTrue(expected.equals(new String(actual)));
    }

// org.apache.commons.codec.binary.HexTest::testEncodeHex_ByteBufferOfZeroes
    public void testEncodeHex_ByteBufferOfZeroes() {
        final char[] c = Hex.encodeHex(ByteBuffer.allocate(36));
        assertEquals("000000000000000000000000000000000000000000000000000000000000000000000000", new String(c));
    }

// org.apache.commons.codec.binary.HexTest::testEncodeHexByteString_ByteBufferOfZeroes
    public void testEncodeHexByteString_ByteBufferOfZeroes() {
        final String c = Hex.encodeHexString(ByteBuffer.allocate(36));
        assertEquals("000000000000000000000000000000000000000000000000000000000000000000000000", c);
    }

// org.apache.commons.codec.binary.HexTest::testEncodeHexByteString_ByteArrayOfZeroes
    public void testEncodeHexByteString_ByteArrayOfZeroes() {
        final String c = Hex.encodeHexString(new byte[36]);
        assertEquals("000000000000000000000000000000000000000000000000000000000000000000000000", c);
    }

// org.apache.commons.codec.binary.HexTest::testEncodeHexByteString_ByteArrayBoolean_ToLowerCase
    public void testEncodeHexByteString_ByteArrayBoolean_ToLowerCase() {
        assertEquals("0a", Hex.encodeHexString(new byte[] { 10 }, true));
    }

// org.apache.commons.codec.binary.HexTest::testEncodeHexByteString_ByteArrayBoolean_ToUpperCase
    public void testEncodeHexByteString_ByteArrayBoolean_ToUpperCase() {
        assertEquals("0A", Hex.encodeHexString(new byte[] { 10 }, false));
    }

// org.apache.commons.codec.binary.HexTest::testEncodeHexByteString_ByteBufferBoolean_ToLowerCase
    public void testEncodeHexByteString_ByteBufferBoolean_ToLowerCase() {
        assertEquals("0a", Hex.encodeHexString(ByteBuffer.wrap(new byte[] { 10 }), true));
    }

// org.apache.commons.codec.binary.HexTest::testEncodeHexByteString_ByteBufferBoolean_ToUpperCase
    public void testEncodeHexByteString_ByteBufferBoolean_ToUpperCase() {
        assertEquals("0A", Hex.encodeHexString(ByteBuffer.wrap(new byte[] { 10 }), false));
    }

// org.apache.commons.codec.binary.HexTest::testEncodeStringEmpty
    public void testEncodeStringEmpty() throws EncoderException {
        assertTrue(Arrays.equals(new char[0], (char[]) new Hex().encode("")));
    }

// org.apache.commons.codec.binary.HexTest::testGetCharset
    public void testGetCharset() {
        Assert.assertEquals(Charsets.UTF_8, new Hex(Charsets.UTF_8).getCharset());
    }

// org.apache.commons.codec.binary.HexTest::testGetCharsetName
    public void testGetCharsetName() {
        Assert.assertEquals(Charsets.UTF_8.name(), new Hex(Charsets.UTF_8).getCharsetName());
    }

// org.apache.commons.codec.binary.HexTest::testRequiredCharset
    public void testRequiredCharset() throws UnsupportedEncodingException, DecoderException {
        testCustomCharset("UTF-8", "testRequiredCharset");
        testCustomCharset("UTF-16", "testRequiredCharset");
        testCustomCharset("UTF-16BE", "testRequiredCharset");
        testCustomCharset("UTF-16LE", "testRequiredCharset");
        testCustomCharset("US-ASCII", "testRequiredCharset");
        testCustomCharset("ISO8859_1", "testRequiredCharset");
    }

// org.apache.commons.codec.binary.StringUtilsTest::testConstructor
    public void testConstructor() {
        new StringUtils();
    }

// org.apache.commons.codec.binary.StringUtilsTest::testGetBytesIso8859_1
    public void testGetBytesIso8859_1() throws UnsupportedEncodingException {
        final String charsetName = "ISO-8859-1";
        testGetBytesUnchecked(charsetName);
        final byte[] expected = STRING_FIXTURE.getBytes(charsetName);
        final byte[] actual = StringUtils.getBytesIso8859_1(STRING_FIXTURE);
        Assert.assertTrue(Arrays.equals(expected, actual));
    }

// org.apache.commons.codec.binary.StringUtilsTest::testGetBytesUsAscii
    public void testGetBytesUsAscii() throws UnsupportedEncodingException {
        final String charsetName = "US-ASCII";
        testGetBytesUnchecked(charsetName);
        final byte[] expected = STRING_FIXTURE.getBytes(charsetName);
        final byte[] actual = StringUtils.getBytesUsAscii(STRING_FIXTURE);
        Assert.assertTrue(Arrays.equals(expected, actual));
    }

// org.apache.commons.codec.binary.StringUtilsTest::testGetBytesUtf16
    public void testGetBytesUtf16() throws UnsupportedEncodingException {
        final String charsetName = "UTF-16";
        testGetBytesUnchecked(charsetName);
        final byte[] expected = STRING_FIXTURE.getBytes(charsetName);
        final byte[] actual = StringUtils.getBytesUtf16(STRING_FIXTURE);
        Assert.assertTrue(Arrays.equals(expected, actual));
    }

// org.apache.commons.codec.binary.StringUtilsTest::testGetBytesUtf16Be
    public void testGetBytesUtf16Be() throws UnsupportedEncodingException {
        final String charsetName = "UTF-16BE";
        testGetBytesUnchecked(charsetName);
        final byte[] expected = STRING_FIXTURE.getBytes(charsetName);
        final byte[] actual = StringUtils.getBytesUtf16Be(STRING_FIXTURE);
        Assert.assertTrue(Arrays.equals(expected, actual));
    }

// org.apache.commons.codec.binary.StringUtilsTest::testGetBytesUtf16Le
    public void testGetBytesUtf16Le() throws UnsupportedEncodingException {
        final String charsetName = "UTF-16LE";
        testGetBytesUnchecked(charsetName);
        final byte[] expected = STRING_FIXTURE.getBytes(charsetName);
        final byte[] actual = StringUtils.getBytesUtf16Le(STRING_FIXTURE);
        Assert.assertTrue(Arrays.equals(expected, actual));
    }

// org.apache.commons.codec.binary.StringUtilsTest::testGetBytesUtf8
    public void testGetBytesUtf8() throws UnsupportedEncodingException {
        final String charsetName = "UTF-8";
        testGetBytesUnchecked(charsetName);
        final byte[] expected = STRING_FIXTURE.getBytes(charsetName);
        final byte[] actual = StringUtils.getBytesUtf8(STRING_FIXTURE);
        Assert.assertTrue(Arrays.equals(expected, actual));
    }

// org.apache.commons.codec.binary.StringUtilsTest::testGetBytesUncheckedBadName
    public void testGetBytesUncheckedBadName() {
        try {
            StringUtils.getBytesUnchecked(STRING_FIXTURE, "UNKNOWN");
            Assert.fail("Expected " + IllegalStateException.class.getName());
        } catch (final IllegalStateException e) {
            
        }
    }

// org.apache.commons.codec.binary.StringUtilsTest::testGetBytesUncheckedNullInput
    public void testGetBytesUncheckedNullInput() {
        Assert.assertNull(StringUtils.getBytesUnchecked(null, "UNKNOWN"));
    }

// org.apache.commons.codec.binary.StringUtilsTest::testNewStringBadEnc
    public void testNewStringBadEnc() {
        try {
            StringUtils.newString(BYTES_FIXTURE, "UNKNOWN");
            Assert.fail("Expected " + IllegalStateException.class.getName());
        } catch (final IllegalStateException e) {
            
        }
    }

// org.apache.commons.codec.binary.StringUtilsTest::testNewStringNullInput
    public void testNewStringNullInput() {
        Assert.assertNull(StringUtils.newString(null, "UNKNOWN"));
    }

// org.apache.commons.codec.binary.StringUtilsTest::testNewStringNullInput_CODEC229
    public void testNewStringNullInput_CODEC229() {
        Assert.assertNull(StringUtils.newStringUtf8(null));
        Assert.assertNull(StringUtils.newStringIso8859_1(null));
        Assert.assertNull(StringUtils.newStringUsAscii(null));
        Assert.assertNull(StringUtils.newStringUtf16(null));
        Assert.assertNull(StringUtils.newStringUtf16Be(null));
        Assert.assertNull(StringUtils.newStringUtf16Le(null));
    }

// org.apache.commons.codec.binary.StringUtilsTest::testNewStringIso8859_1
    public void testNewStringIso8859_1() throws UnsupportedEncodingException {
        final String charsetName = "ISO-8859-1";
        testNewString(charsetName);
        final String expected = new String(BYTES_FIXTURE, charsetName);
        final String actual = StringUtils.newStringIso8859_1(BYTES_FIXTURE);
        Assert.assertEquals(expected, actual);
    }

// org.apache.commons.codec.binary.StringUtilsTest::testNewStringUsAscii
    public void testNewStringUsAscii() throws UnsupportedEncodingException {
        final String charsetName = "US-ASCII";
        testNewString(charsetName);
        final String expected = new String(BYTES_FIXTURE, charsetName);
        final String actual = StringUtils.newStringUsAscii(BYTES_FIXTURE);
        Assert.assertEquals(expected, actual);
    }

// org.apache.commons.codec.binary.StringUtilsTest::testNewStringUtf16
    public void testNewStringUtf16() throws UnsupportedEncodingException {
        final String charsetName = "UTF-16";
        testNewString(charsetName);
        final String expected = new String(BYTES_FIXTURE, charsetName);
        final String actual = StringUtils.newStringUtf16(BYTES_FIXTURE);
        Assert.assertEquals(expected, actual);
    }

// org.apache.commons.codec.binary.StringUtilsTest::testNewStringUtf16Be
    public void testNewStringUtf16Be() throws UnsupportedEncodingException {
        final String charsetName = "UTF-16BE";
        testNewString(charsetName);
        final String expected = new String(BYTES_FIXTURE_16BE, charsetName);
        final String actual = StringUtils.newStringUtf16Be(BYTES_FIXTURE_16BE);
        Assert.assertEquals(expected, actual);
    }

// org.apache.commons.codec.binary.StringUtilsTest::testNewStringUtf16Le
    public void testNewStringUtf16Le() throws UnsupportedEncodingException {
        final String charsetName = "UTF-16LE";
        testNewString(charsetName);
        final String expected = new String(BYTES_FIXTURE_16LE, charsetName);
        final String actual = StringUtils.newStringUtf16Le(BYTES_FIXTURE_16LE);
        Assert.assertEquals(expected, actual);
    }

// org.apache.commons.codec.binary.StringUtilsTest::testNewStringUtf8
    public void testNewStringUtf8() throws UnsupportedEncodingException {
        final String charsetName = "UTF-8";
        testNewString(charsetName);
        final String expected = new String(BYTES_FIXTURE, charsetName);
        final String actual = StringUtils.newStringUtf8(BYTES_FIXTURE);
        Assert.assertEquals(expected, actual);
    }

// org.apache.commons.codec.binary.StringUtilsTest::testEqualsString
    public void testEqualsString() {
        Assert.assertTrue(StringUtils.equals(null, null));
        Assert.assertFalse(StringUtils.equals("abc", null));
        Assert.assertFalse(StringUtils.equals(null, "abc"));
        Assert.assertTrue(StringUtils.equals("abc", "abc"));
        Assert.assertFalse(StringUtils.equals("abc", "abcd"));
        Assert.assertFalse(StringUtils.equals("abcd", "abc"));
        Assert.assertFalse(StringUtils.equals("abc", "ABC"));
    }

// org.apache.commons.codec.binary.StringUtilsTest::testEqualsCS1
    public void testEqualsCS1() {
        Assert.assertFalse(StringUtils.equals(new StringBuilder("abc"), null));
        Assert.assertFalse(StringUtils.equals(null, new StringBuilder("abc")));
        Assert.assertTrue(StringUtils.equals(new StringBuilder("abc"), new StringBuilder("abc")));
        Assert.assertFalse(StringUtils.equals(new StringBuilder("abc"), new StringBuilder("abcd")));
        Assert.assertFalse(StringUtils.equals(new StringBuilder("abcd"), new StringBuilder("abc")));
        Assert.assertFalse(StringUtils.equals(new StringBuilder("abc"), new StringBuilder("ABC")));
    }

// org.apache.commons.codec.binary.StringUtilsTest::testEqualsCS2
    public void testEqualsCS2() {
        Assert.assertTrue(StringUtils.equals("abc", new StringBuilder("abc")));
        Assert.assertFalse(StringUtils.equals(new StringBuilder("abc"), "abcd"));
        Assert.assertFalse(StringUtils.equals("abcd", new StringBuilder("abc")));
        Assert.assertFalse(StringUtils.equals(new StringBuilder("abc"), "ABC"));
    }

// org.apache.commons.codec.digest.DigestUtilsTest::testInternalNoSuchAlgorithmException
    public void testInternalNoSuchAlgorithmException() {
        DigestUtils.getDigest("Bogus Bogus");
    }

// org.apache.commons.codec.digest.DigestUtilsTest::testMd2Hex
    public void testMd2Hex() throws IOException {
        
        assertEquals("8350e5a3e24c153df2275c9f80692773", DigestUtils.md2Hex(""));

        assertEquals("32ec01ec4a6dac72c0ab96fb34c0b5d1", DigestUtils.md2Hex("a"));

        assertEquals("da853b0d3f88d99b30283a69e6ded6bb", DigestUtils.md2Hex("abc"));

        assertEquals("ab4f496bfb2a530b219ff33031fe06b0", DigestUtils.md2Hex("message digest"));

        assertEquals("4e8ddff3650292ab5a4108c3aa47940b", DigestUtils.md2Hex("abcdefghijklmnopqrstuvwxyz"));

        assertEquals(
            "da33def2a42df13975352846c30338cd",
            DigestUtils.md2Hex("ABCDEFGHIJKLMNOPQRSTUVWXYZ" + "abcdefghijklmnopqrstuvwxyz" + "0123456789"));

        assertEquals(
            "d5976f79d83d3a0dc9806c3c66f3efd8",
            DigestUtils.md2Hex("1234567890123456789012345678901234567890" + "1234567890123456789012345678901234567890"));

        assertEquals(DigestUtils.md2Hex(testData),
                DigestUtils.md2Hex(new ByteArrayInputStream(testData)));
}

// org.apache.commons.codec.digest.DigestUtilsTest::testMd2HexLength
    public void testMd2HexLength() {
        String hashMe = "this is some string that is longer than 32 characters";
        String hash = DigestUtils.md2Hex(getBytesUtf8(hashMe));
        assertEquals(32, hash.length());

        hashMe = "length < 32";
        hash = DigestUtils.md2Hex(getBytesUtf8(hashMe));
        assertEquals(32, hash.length());
    }

// org.apache.commons.codec.digest.DigestUtilsTest::testMd2Length
    public void testMd2Length() {
        String hashMe = "this is some string that is longer than 16 characters";
        byte[] hash = DigestUtils.md2(getBytesUtf8(hashMe));
        assertEquals(16, hash.length);

        hashMe = "length < 16";
        hash = DigestUtils.md2(getBytesUtf8(hashMe));
        assertEquals(16, hash.length);
    }

// org.apache.commons.codec.digest.DigestUtilsTest::testMd5Hex
    public void testMd5Hex() throws IOException {
        
        assertEquals("d41d8cd98f00b204e9800998ecf8427e", DigestUtils.md5Hex(""));

        assertEquals("0cc175b9c0f1b6a831c399e269772661", DigestUtils.md5Hex("a"));

        assertEquals("900150983cd24fb0d6963f7d28e17f72", DigestUtils.md5Hex("abc"));

        assertEquals("f96b697d7cb7938d525a2f31aaf161d0", DigestUtils.md5Hex("message digest"));

        assertEquals("c3fcd3d76192e4007dfb496cca67e13b", DigestUtils.md5Hex("abcdefghijklmnopqrstuvwxyz"));

        assertEquals(
            "d174ab98d277d9f5a5611c2c9f419d9f",
            DigestUtils.md5Hex("ABCDEFGHIJKLMNOPQRSTUVWXYZ" + "abcdefghijklmnopqrstuvwxyz" + "0123456789"));

        assertEquals(
            "57edf4a22be3c955ac49da2e2107b67a",
            DigestUtils.md5Hex("1234567890123456789012345678901234567890" + "1234567890123456789012345678901234567890"));

        assertEquals(DigestUtils.md5Hex(testData),
                DigestUtils.md5Hex(new ByteArrayInputStream(testData)));
}

// org.apache.commons.codec.digest.DigestUtilsTest::testMd5HexLengthForBytes
    public void testMd5HexLengthForBytes() {
        String hashMe = "this is some string that is longer than 32 characters";
        String hash = DigestUtils.md5Hex(getBytesUtf8(hashMe));
        assertEquals(32, hash.length());

        hashMe = "length < 32";
        hash = DigestUtils.md5Hex(getBytesUtf8(hashMe));
        assertEquals(32, hash.length());
    }

// org.apache.commons.codec.digest.DigestUtilsTest::testMd5LengthForBytes
    public void testMd5LengthForBytes() {
        String hashMe = "this is some string that is longer than 16 characters";
        byte[] hash = DigestUtils.md5(getBytesUtf8(hashMe));
        assertEquals(16, hash.length);

        hashMe = "length < 16";
        hash = DigestUtils.md5(getBytesUtf8(hashMe));
        assertEquals(16, hash.length);
    }

// org.apache.commons.codec.digest.DigestUtilsTest::testSha1Hex
    public void testSha1Hex() throws IOException {
        
        assertEquals("a9993e364706816aba3e25717850c26c9cd0d89d", DigestUtils.sha1Hex("abc"));

        assertEquals("a9993e364706816aba3e25717850c26c9cd0d89d", DigestUtils.sha1Hex(getBytesUtf8("abc")));

        assertEquals(
            "84983e441c3bd26ebaae4aa1f95129e5e54670f1",
            DigestUtils.sha1Hex("abcdbcdecdefdefgefghfghighij" + "hijkijkljklmklmnlmnomnopnopq"));
        assertEquals(DigestUtils.sha1Hex(testData),
                DigestUtils.sha1Hex(new ByteArrayInputStream(testData)));
    }

// org.apache.commons.codec.digest.DigestUtilsTest::testSha1UpdateWithByteArray
    public void testSha1UpdateWithByteArray(){
        final String d1 = "C'est un homme qui rentre dans un café, et plouf";
        final String d2 = "C'est un homme, c'est qu'une tête, on lui offre un cadeau: 'oh... encore un chapeau!'";

        MessageDigest messageDigest = DigestUtils.getSha1Digest();
        messageDigest.update(d1.getBytes());
        messageDigest.update(d2.getBytes());
        final String expectedResult = Hex.encodeHexString(messageDigest.digest());

        messageDigest = DigestUtils.getSha1Digest();
        DigestUtils.updateDigest(messageDigest, d1.getBytes());
        DigestUtils.updateDigest(messageDigest, d2.getBytes());
        final String actualResult = Hex.encodeHexString(messageDigest.digest());

        assertEquals(expectedResult, actualResult);
    }

// org.apache.commons.codec.digest.DigestUtilsTest::testSha1UpdateWithByteBuffer
    public void testSha1UpdateWithByteBuffer(){
        final String d1 = "C'est un homme qui rentre dans un café, et plouf";
        final String d2 = "C'est un homme, c'est qu'une tête, on lui offre un cadeau: 'oh... encore un chapeau!'";

        MessageDigest messageDigest = DigestUtils.getSha1Digest();
        messageDigest.update(d1.getBytes());
        messageDigest.update(d2.getBytes());
        final String expectedResult = Hex.encodeHexString(messageDigest.digest());

        messageDigest = DigestUtils.getSha1Digest();
        DigestUtils.updateDigest(messageDigest, ByteBuffer.wrap(d1.getBytes()));
        DigestUtils.updateDigest(messageDigest, ByteBuffer.wrap(d2.getBytes()));
        final String actualResult = Hex.encodeHexString(messageDigest.digest());

        assertEquals(expectedResult, actualResult);
    }

// org.apache.commons.codec.digest.DigestUtilsTest::testSha1UpdateWithString
    public void testSha1UpdateWithString(){
        final String d1 = "C'est un homme qui rentre dans un café, et plouf";
        final String d2 = "C'est un homme, c'est qu'une tête, on lui offre un cadeau: 'oh... encore un chapeau!'";

        MessageDigest messageDigest = DigestUtils.getSha1Digest();
        messageDigest.update(StringUtils.getBytesUtf8(d1));
        messageDigest.update(StringUtils.getBytesUtf8(d2));
        final String expectedResult = Hex.encodeHexString(messageDigest.digest());

        messageDigest = DigestUtils.getSha1Digest();
        DigestUtils.updateDigest(messageDigest, d1);
        DigestUtils.updateDigest(messageDigest, d2);
        final String actualResult = Hex.encodeHexString(messageDigest.digest());

        assertEquals(expectedResult, actualResult);
    }

// org.apache.commons.codec.digest.DigestUtilsTest::testSha224
    public void testSha224() throws IOException {
        assumeJava8();
        assertEquals("d14a028c2a3a2bc9476102bb288234c415a2b01f828ea62ac5b3e42f",
                new DigestUtils(MessageDigestAlgorithms.SHA_224).digestAsHex(("")));
        assertEquals("730e109bd7a8a32b1cb9d9a09aa2325d2430587ddbc0c38bad911525",
                new DigestUtils(MessageDigestAlgorithms.SHA_224).digestAsHex("The quick brown fox jumps over the lazy dog"));

        
    }

// org.apache.commons.codec.digest.DigestUtilsTest::testSha256
    public void testSha256() throws IOException {
    
    assertEquals("ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad",
             DigestUtils.sha256Hex("abc"));
    assertEquals("ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad",
             DigestUtils.sha256Hex(getBytesUtf8("abc")));
    assertEquals("248d6a61d20638b8e5c026930c3e6039a33ce45964ff2167f6ecedd419db06c1",
             DigestUtils.sha256Hex("abcdbcdecdefdefgefghfghighijhijkijkljklmklmnlmnomnopnopq"));

    assertEquals(DigestUtils.sha256Hex(testData),
            DigestUtils.sha256Hex(new ByteArrayInputStream(testData)));
    }

// org.apache.commons.codec.digest.DigestUtilsTest::testSha384
    public void testSha384() throws IOException {
    
    assertEquals("cb00753f45a35e8bb5a03d699ac65007272c32ab0eded1631a8b605a43ff5bed" +
             "8086072ba1e7cc2358baeca134c825a7",
             DigestUtils.sha384Hex("abc"));
    assertEquals("cb00753f45a35e8bb5a03d699ac65007272c32ab0eded1631a8b605a43ff5bed" +
             "8086072ba1e7cc2358baeca134c825a7",
             DigestUtils.sha384Hex(getBytesUtf8("abc")));
    assertEquals("09330c33f71147e83d192fc782cd1b4753111b173b3b05d22fa08086e3b0f712" +
            "fcc7c71a557e2db966c3e9fa91746039",
             DigestUtils.sha384Hex("abcdefghbcdefghicdefghijdefghijkefghijklfghijklmghijklmn" +
                       "hijklmnoijklmnopjklmnopqklmnopqrlmnopqrsmnopqrstnopqrstu"));
    assertEquals(DigestUtils.sha384Hex(testData),
            DigestUtils.sha384Hex(new ByteArrayInputStream(testData)));
    }

// org.apache.commons.codec.digest.DigestUtilsTest::testSha512
    public void testSha512() throws IOException {
    
    assertEquals("ddaf35a193617abacc417349ae20413112e6fa4e89a97ea20a9eeee64b55d39a" +
            "2192992a274fc1a836ba3c23a3feebbd454d4423643ce80e2a9ac94fa54ca49f",
             DigestUtils.sha512Hex("abc"));
    assertEquals("ddaf35a193617abacc417349ae20413112e6fa4e89a97ea20a9eeee64b55d39a" +
             "2192992a274fc1a836ba3c23a3feebbd454d4423643ce80e2a9ac94fa54ca49f",
             DigestUtils.sha512Hex(getBytesUtf8("abc")));
    assertEquals("8e959b75dae313da8cf4f72814fc143f8f7779c6eb9f7fa17299aeadb6889018" +
             "501d289e4900f7e4331b99dec4b5433ac7d329eeb6dd26545e96e55b874be909",
             DigestUtils.sha512Hex("abcdefghbcdefghicdefghijdefghijkefghijklfghijklmghijklmn" +
                       "hijklmnoijklmnopjklmnopqklmnopqrlmnopqrsmnopqrstnopqrstu"));
    }

// org.apache.commons.codec.digest.DigestUtilsTest::testSha512HexInputStream
    public void testSha512HexInputStream() throws IOException {
        assertEquals(DigestUtils.sha512Hex(testData),
                DigestUtils.sha512Hex(new ByteArrayInputStream(testData)));
    }

// org.apache.commons.codec.digest.DigestUtilsTest::testShaHex
    public void testShaHex() throws IOException {
        
        assertEquals("a9993e364706816aba3e25717850c26c9cd0d89d", DigestUtils.shaHex("abc"));

        assertEquals("a9993e364706816aba3e25717850c26c9cd0d89d", DigestUtils.shaHex(getBytesUtf8("abc")));

        assertEquals(
            "84983e441c3bd26ebaae4aa1f95129e5e54670f1",
            DigestUtils.shaHex("abcdbcdecdefdefgefghfghighij" + "hijkijkljklmklmnlmnomnopnopq"));
        assertEquals(DigestUtils.shaHex(testData),
                DigestUtils.shaHex(new ByteArrayInputStream(testData)));
    }

// org.apache.commons.codec.digest.DigestUtilsTest::testShaUpdateWithByteArray
    public void testShaUpdateWithByteArray(){
        final String d1 = "C'est un homme qui rentre dans un café, et plouf";
        final String d2 = "C'est un homme, c'est qu'une tête, on lui offre un cadeau: 'oh... encore un chapeau!'";

        MessageDigest messageDigest = DigestUtils.getShaDigest();
        messageDigest.update(d1.getBytes());
        messageDigest.update(d2.getBytes());
        final String expectedResult = Hex.encodeHexString(messageDigest.digest());

        messageDigest = DigestUtils.getShaDigest();
        DigestUtils.updateDigest(messageDigest, d1.getBytes());
        DigestUtils.updateDigest(messageDigest, d2.getBytes());
        final String actualResult = Hex.encodeHexString(messageDigest.digest());

        assertEquals(expectedResult, actualResult);
    }

// org.apache.commons.codec.digest.DigestUtilsTest::testShaUpdateWithString
    public void testShaUpdateWithString(){
        final String d1 = "C'est un homme qui rentre dans un café, et plouf";
        final String d2 = "C'est un homme, c'est qu'une tête, on lui offre un cadeau: 'oh... encore un chapeau!'";

        MessageDigest messageDigest = DigestUtils.getShaDigest();
        messageDigest.update(StringUtils.getBytesUtf8(d1));
        messageDigest.update(StringUtils.getBytesUtf8(d2));
        final String expectedResult = Hex.encodeHexString(messageDigest.digest());

        messageDigest = DigestUtils.getShaDigest();
        DigestUtils.updateDigest(messageDigest, d1);
        DigestUtils.updateDigest(messageDigest, d2);
        final String actualResult = Hex.encodeHexString(messageDigest.digest());

        assertEquals(expectedResult, actualResult);
    }

// org.apache.commons.codec.digest.HmacAlgorithmsTest::testAlgorithm
    public void testAlgorithm() throws IOException, NoSuchAlgorithmException {
        final String algorithm = hmacAlgorithm.getName();
        Assert.assertNotNull(algorithm);
        Assert.assertFalse(algorithm.isEmpty());
        Assume.assumeTrue(HmacUtils.isAvailable(hmacAlgorithm));
        Mac.getInstance(algorithm);
    }

// org.apache.commons.codec.digest.HmacAlgorithmsTest::testGetHmacEmptyKey
    public void testGetHmacEmptyKey() {
        HmacUtils.getInitializedMac(hmacAlgorithm, EMPTY_BYTE_ARRAY);
    }

// org.apache.commons.codec.digest.HmacAlgorithmsTest::testGetHmacNullKey
    public void testGetHmacNullKey() {
        HmacUtils.getInitializedMac(hmacAlgorithm, null);
    }

// org.apache.commons.codec.digest.HmacAlgorithmsTest::testHmacFailByteArray
    public void testHmacFailByteArray() throws IOException {
        new HmacUtils(hmacAlgorithm, (byte[]) null).hmac(STANDARD_PHRASE_BYTES);
    }

// org.apache.commons.codec.digest.HmacAlgorithmsTest::testHmacFailInputStream
    public void testHmacFailInputStream() throws IOException {
        new HmacUtils(hmacAlgorithm, (byte[]) null).hmac(new ByteArrayInputStream(STANDARD_PHRASE_BYTES));
    }

// org.apache.commons.codec.digest.HmacAlgorithmsTest::testHmacFailString
    public void testHmacFailString() throws IOException {
        new HmacUtils(hmacAlgorithm, (String) null).hmac(STANDARD_PHRASE_STRING);
    }

// org.apache.commons.codec.digest.HmacAlgorithmsTest::testHmacHexFailByteArray
    public void testHmacHexFailByteArray() throws IOException {
        new HmacUtils(hmacAlgorithm, (byte[]) null).hmac(STANDARD_PHRASE_BYTES);
    }

// org.apache.commons.codec.digest.HmacAlgorithmsTest::testHmacHexFailInputStream
    public void testHmacHexFailInputStream() throws IOException {
        new HmacUtils(hmacAlgorithm, (byte[]) null).hmac(new ByteArrayInputStream(STANDARD_PHRASE_BYTES));
    }

// org.apache.commons.codec.digest.HmacAlgorithmsTest::testHmacHexFailString
    public void testHmacHexFailString() throws IOException {
        new HmacUtils(hmacAlgorithm, (String) null).hmac(STANDARD_PHRASE_STRING);
    }

// org.apache.commons.codec.digest.HmacAlgorithmsTest::testInitializedMac
    public void testInitializedMac() throws IOException {
        final Mac mac = HmacUtils.getInitializedMac(hmacAlgorithm, STANDARD_KEY_BYTES);
        final Mac mac2 = HmacUtils.getInitializedMac(hmacAlgorithm.getName(), STANDARD_KEY_BYTES);
        Assert.assertArrayEquals(standardResultBytes, HmacUtils.updateHmac(mac, STANDARD_PHRASE_STRING).doFinal());
        Assert.assertArrayEquals(standardResultBytes, HmacUtils.updateHmac(mac2, STANDARD_PHRASE_STRING).doFinal());
    }

// org.apache.commons.codec.digest.HmacAlgorithmsTest::testMacByteArary
    public void testMacByteArary() throws IOException {
        Assert.assertArrayEquals(standardResultBytes, new HmacUtils(hmacAlgorithm, STANDARD_KEY_BYTES).hmac(STANDARD_PHRASE_BYTES));
    }

// org.apache.commons.codec.digest.HmacAlgorithmsTest::testMacHexByteArray
    public void testMacHexByteArray() throws IOException {
        Assert.assertEquals(standardResultString, new HmacUtils(hmacAlgorithm, STANDARD_KEY_BYTES).hmacHex(STANDARD_PHRASE_BYTES));
    }

// org.apache.commons.codec.digest.HmacAlgorithmsTest::testMacHexInputStream
    public void testMacHexInputStream() throws IOException {
        Assert.assertEquals(standardResultString,
                new HmacUtils(hmacAlgorithm, STANDARD_KEY_BYTES).hmacHex(new ByteArrayInputStream(STANDARD_PHRASE_BYTES)));
    }

// org.apache.commons.codec.digest.HmacAlgorithmsTest::testMacHexString
    public void testMacHexString() throws IOException {
        Assert.assertEquals(standardResultString, new HmacUtils(hmacAlgorithm, STANDARD_KEY_BYTES).hmacHex(STANDARD_PHRASE_STRING));
    }

// org.apache.commons.codec.digest.HmacAlgorithmsTest::testMacInputStream
    public void testMacInputStream() throws IOException {
        Assert.assertArrayEquals(standardResultBytes,
                new HmacUtils(hmacAlgorithm, STANDARD_KEY_BYTES).hmac(new ByteArrayInputStream(STANDARD_PHRASE_BYTES)));
    }

// org.apache.commons.codec.digest.HmacAlgorithmsTest::testMacString
    public void testMacString() throws IOException {
        Assert.assertArrayEquals(standardResultBytes, new HmacUtils(hmacAlgorithm, STANDARD_KEY_BYTES).hmac(STANDARD_PHRASE_STRING));
    }

// org.apache.commons.codec.digest.HmacUtilsTest::testEmptyKey
    public void testEmptyKey() {
        HmacUtils.getHmacMd5(new byte[] {});
    }

// org.apache.commons.codec.digest.HmacUtilsTest::testGetHMac
    public void testGetHMac() throws IOException {
        Assert.assertArrayEquals(HmacAlgorithmsTest.STANDARD_MD5_RESULT_BYTES,
                HmacUtils.getHmacMd5(HmacAlgorithmsTest.STANDARD_KEY_BYTES).doFinal(HmacAlgorithmsTest.STANDARD_PHRASE_BYTES));
        Assert.assertArrayEquals(HmacAlgorithmsTest.STANDARD_SHA1_RESULT_BYTES,
                HmacUtils.getHmacSha1(HmacAlgorithmsTest.STANDARD_KEY_BYTES).doFinal(HmacAlgorithmsTest.STANDARD_PHRASE_BYTES));
        Assert.assertArrayEquals(HmacAlgorithmsTest.STANDARD_SHA256_RESULT_BYTES,
                HmacUtils.getHmacSha256(HmacAlgorithmsTest.STANDARD_KEY_BYTES).doFinal(HmacAlgorithmsTest.STANDARD_PHRASE_BYTES));
        Assert.assertArrayEquals(HmacAlgorithmsTest.STANDARD_SHA384_RESULT_BYTES,
                HmacUtils.getHmacSha384(HmacAlgorithmsTest.STANDARD_KEY_BYTES).doFinal(HmacAlgorithmsTest.STANDARD_PHRASE_BYTES));
        Assert.assertArrayEquals(HmacAlgorithmsTest.STANDARD_SHA512_RESULT_BYTES,
                HmacUtils.getHmacSha512(HmacAlgorithmsTest.STANDARD_KEY_BYTES).doFinal(HmacAlgorithmsTest.STANDARD_PHRASE_BYTES));
    }

// org.apache.commons.codec.digest.HmacUtilsTest::testHmacMd5Hex
    public void testHmacMd5Hex() throws IOException {
        assertEquals(HmacAlgorithmsTest.STANDARD_MD5_RESULT_STRING,
                HmacUtils.hmacMd5Hex(HmacAlgorithmsTest.STANDARD_KEY_STRING, "The quick brown fox jumps over the lazy dog"));
        assertEquals("750c783e6ab0b503eaa86e310a5db738", HmacUtils.hmacMd5Hex("Jefe", "what do ya want for nothing?"));
        assertEquals(
                "750c783e6ab0b503eaa86e310a5db738",
                HmacUtils.hmacMd5Hex("Jefe".getBytes(),
                        new ByteArrayInputStream("what do ya want for nothing?".getBytes())));
    }

// org.apache.commons.codec.digest.HmacUtilsTest::testHmacSha1Hex
    public void testHmacSha1Hex() throws IOException {
        assertEquals(HmacAlgorithmsTest.STANDARD_SHA1_RESULT_STRING, HmacUtils.hmacSha1Hex(HmacAlgorithmsTest.STANDARD_KEY_STRING, HmacAlgorithmsTest.STANDARD_PHRASE_STRING));
        assertEquals("f42bb0eeb018ebbd4597ae7213711ec60760843f", HmacUtils.hmacSha1Hex(HmacAlgorithmsTest.STANDARD_KEY_STRING, ""));
        assertEquals("effcdf6ae5eb2fa2d27416d5f184df9c259a7c79",
                HmacUtils.hmacSha1Hex("Jefe", "what do ya want for nothing?"));
        assertEquals(
                "effcdf6ae5eb2fa2d27416d5f184df9c259a7c79",
                HmacUtils.hmacSha1Hex("Jefe".getBytes(),
                        new ByteArrayInputStream("what do ya want for nothing?".getBytes())));
    }

// org.apache.commons.codec.digest.HmacUtilsTest::testHmacSha1UpdateWithByteArray
    public void testHmacSha1UpdateWithByteArray() throws IOException {
        final Mac mac = HmacUtils.getHmacSha1(HmacAlgorithmsTest.STANDARD_KEY_BYTES);
        HmacUtils.updateHmac(mac, HmacAlgorithmsTest.STANDARD_PHRASE_BYTES);
        assertEquals(HmacAlgorithmsTest.STANDARD_SHA1_RESULT_STRING, Hex.encodeHexString(mac.doFinal()));
        HmacUtils.updateHmac(mac, "".getBytes());
        assertEquals("f42bb0eeb018ebbd4597ae7213711ec60760843f", Hex.encodeHexString(mac.doFinal()));
    }

// org.apache.commons.codec.digest.HmacUtilsTest::testHmacSha1UpdateWithInpustream
    public void testHmacSha1UpdateWithInpustream() throws IOException {
        final Mac mac = HmacUtils.getHmacSha1(HmacAlgorithmsTest.STANDARD_KEY_BYTES);
        HmacUtils.updateHmac(mac, new ByteArrayInputStream(HmacAlgorithmsTest.STANDARD_PHRASE_BYTES));
        assertEquals(HmacAlgorithmsTest.STANDARD_SHA1_RESULT_STRING, Hex.encodeHexString(mac.doFinal()));
        HmacUtils.updateHmac(mac, new ByteArrayInputStream("".getBytes()));
        assertEquals("f42bb0eeb018ebbd4597ae7213711ec60760843f", Hex.encodeHexString(mac.doFinal()));
    }

// org.apache.commons.codec.digest.HmacUtilsTest::testHmacSha1UpdateWithString
    public void testHmacSha1UpdateWithString() throws IOException {
        final Mac mac = HmacUtils.getHmacSha1(HmacAlgorithmsTest.STANDARD_KEY_BYTES);
        HmacUtils.updateHmac(mac, HmacAlgorithmsTest.STANDARD_PHRASE_STRING);
        assertEquals(HmacAlgorithmsTest.STANDARD_SHA1_RESULT_STRING, Hex.encodeHexString(mac.doFinal()));
        HmacUtils.updateHmac(mac, "");
        assertEquals("f42bb0eeb018ebbd4597ae7213711ec60760843f", Hex.encodeHexString(mac.doFinal()));
    }

// org.apache.commons.codec.digest.HmacUtilsTest::testInitializedMac
    public void testInitializedMac() throws IOException {
        final Mac md5Mac = HmacUtils.getInitializedMac(HmacAlgorithms.HMAC_MD5, HmacAlgorithmsTest.STANDARD_KEY_BYTES);
        final Mac md5Mac2 = HmacUtils.getInitializedMac("HmacMD5", HmacAlgorithmsTest.STANDARD_KEY_BYTES);
        Assert.assertArrayEquals(HmacAlgorithmsTest.STANDARD_MD5_RESULT_BYTES, HmacUtils.updateHmac(md5Mac, HmacAlgorithmsTest.STANDARD_PHRASE_STRING)
                .doFinal());
        Assert.assertArrayEquals(HmacAlgorithmsTest.STANDARD_MD5_RESULT_BYTES, HmacUtils.updateHmac(md5Mac2, HmacAlgorithmsTest.STANDARD_PHRASE_STRING)
                .doFinal());
    }

// org.apache.commons.codec.digest.HmacUtilsTest::testInitializedMacNullAlgo
    public void testInitializedMacNullAlgo() throws IOException {
        HmacUtils.getInitializedMac((String) null, HmacAlgorithmsTest.STANDARD_KEY_BYTES);
    }

// org.apache.commons.codec.digest.HmacUtilsTest::testInitializedMacNullKey
    public void testInitializedMacNullKey() throws IOException {
        HmacUtils.getInitializedMac(HmacAlgorithms.HMAC_MD5, null);
    }

// org.apache.commons.codec.digest.HmacUtilsTest::testInternalNoSuchAlgorithmException
    public void testInternalNoSuchAlgorithmException() {
        HmacUtils.getInitializedMac("Bogus Bogus", StringUtils.getBytesUtf8("akey"));
    }

// org.apache.commons.codec.digest.HmacUtilsTest::testMd5HMac
    public void testMd5HMac() throws IOException {
        Assert.assertArrayEquals(HmacAlgorithmsTest.STANDARD_MD5_RESULT_BYTES,
                HmacUtils.hmacMd5(HmacAlgorithmsTest.STANDARD_KEY_BYTES, HmacAlgorithmsTest.STANDARD_PHRASE_BYTES));
        Assert.assertArrayEquals(HmacAlgorithmsTest.STANDARD_MD5_RESULT_BYTES,
                HmacUtils.hmacMd5(HmacAlgorithmsTest.STANDARD_KEY_BYTES, new ByteArrayInputStream(HmacAlgorithmsTest.STANDARD_PHRASE_BYTES)));
        Assert.assertArrayEquals(HmacAlgorithmsTest.STANDARD_MD5_RESULT_BYTES,
                HmacUtils.hmacMd5(HmacAlgorithmsTest.STANDARD_KEY_STRING, HmacAlgorithmsTest.STANDARD_PHRASE_STRING));
        Assert.assertEquals(HmacAlgorithmsTest.STANDARD_MD5_RESULT_STRING, HmacUtils.hmacMd5Hex(HmacAlgorithmsTest.STANDARD_KEY_BYTES, HmacAlgorithmsTest.STANDARD_PHRASE_BYTES));
        Assert.assertEquals(HmacAlgorithmsTest.STANDARD_MD5_RESULT_STRING,
                HmacUtils.hmacMd5Hex(HmacAlgorithmsTest.STANDARD_KEY_BYTES, new ByteArrayInputStream(HmacAlgorithmsTest.STANDARD_PHRASE_BYTES)));
        Assert.assertEquals(HmacAlgorithmsTest.STANDARD_MD5_RESULT_STRING,
                HmacUtils.hmacMd5Hex(HmacAlgorithmsTest.STANDARD_KEY_STRING, HmacAlgorithmsTest.STANDARD_PHRASE_STRING));
    }

// org.apache.commons.codec.digest.HmacUtilsTest::testMd5HMacFail
    public void testMd5HMacFail() throws IOException {
        HmacUtils.hmacMd5((byte[]) null, HmacAlgorithmsTest.STANDARD_PHRASE_BYTES);
    }

// org.apache.commons.codec.digest.HmacUtilsTest::testNullKey
    public void testNullKey() {
        HmacUtils.getHmacMd5(null);
    }

// org.apache.commons.codec.digest.HmacUtilsTest::testSecretKeySpecAllowsEmtyKeys
    public void testSecretKeySpecAllowsEmtyKeys() {
        new SecretKeySpec(new byte[] {}, "HmacMD5");
    }

// org.apache.commons.codec.digest.HmacUtilsTest::testSha1HMac
    public void testSha1HMac() throws IOException {
        Assert.assertArrayEquals(HmacAlgorithmsTest.STANDARD_SHA1_RESULT_BYTES,
                HmacUtils.hmacSha1(HmacAlgorithmsTest.STANDARD_KEY_BYTES, HmacAlgorithmsTest.STANDARD_PHRASE_BYTES));
        Assert.assertArrayEquals(HmacAlgorithmsTest.STANDARD_SHA1_RESULT_BYTES,
                HmacUtils.hmacSha1(HmacAlgorithmsTest.STANDARD_KEY_BYTES, new ByteArrayInputStream(HmacAlgorithmsTest.STANDARD_PHRASE_BYTES)));
        Assert.assertArrayEquals(HmacAlgorithmsTest.STANDARD_SHA1_RESULT_BYTES,
                HmacUtils.hmacSha1(HmacAlgorithmsTest.STANDARD_KEY_STRING, HmacAlgorithmsTest.STANDARD_PHRASE_STRING));
        Assert.assertEquals(HmacAlgorithmsTest.STANDARD_SHA1_RESULT_STRING,
                HmacUtils.hmacSha1Hex(HmacAlgorithmsTest.STANDARD_KEY_BYTES, HmacAlgorithmsTest.STANDARD_PHRASE_BYTES));
        Assert.assertEquals(HmacAlgorithmsTest.STANDARD_SHA1_RESULT_STRING,
                HmacUtils.hmacSha1Hex(HmacAlgorithmsTest.STANDARD_KEY_BYTES, new ByteArrayInputStream(HmacAlgorithmsTest.STANDARD_PHRASE_BYTES)));
        Assert.assertEquals(HmacAlgorithmsTest.STANDARD_SHA1_RESULT_STRING,
                HmacUtils.hmacSha1Hex(HmacAlgorithmsTest.STANDARD_KEY_STRING, HmacAlgorithmsTest.STANDARD_PHRASE_STRING));
    }

// org.apache.commons.codec.digest.HmacUtilsTest::testSha1HMacFail
    public void testSha1HMacFail() throws IOException {
        HmacUtils.hmacSha1((byte[]) null, HmacAlgorithmsTest.STANDARD_PHRASE_BYTES);
    }

// org.apache.commons.codec.digest.HmacUtilsTest::testSha256HMac
    public void testSha256HMac() throws IOException {
        Assert.assertArrayEquals(HmacAlgorithmsTest.STANDARD_SHA256_RESULT_BYTES,
                HmacUtils.hmacSha256(HmacAlgorithmsTest.STANDARD_KEY_BYTES, HmacAlgorithmsTest.STANDARD_PHRASE_BYTES));
        Assert.assertArrayEquals(HmacAlgorithmsTest.STANDARD_SHA256_RESULT_BYTES,
                HmacUtils.hmacSha256(HmacAlgorithmsTest.STANDARD_KEY_BYTES, new ByteArrayInputStream(HmacAlgorithmsTest.STANDARD_PHRASE_BYTES)));
        Assert.assertArrayEquals(HmacAlgorithmsTest.STANDARD_SHA256_RESULT_BYTES,
                HmacUtils.hmacSha256(HmacAlgorithmsTest.STANDARD_KEY_STRING, HmacAlgorithmsTest.STANDARD_PHRASE_STRING));
        Assert.assertEquals(HmacAlgorithmsTest.STANDARD_SHA256_RESULT_STRING,
                HmacUtils.hmacSha256Hex(HmacAlgorithmsTest.STANDARD_KEY_BYTES, HmacAlgorithmsTest.STANDARD_PHRASE_BYTES));
        Assert.assertEquals(HmacAlgorithmsTest.STANDARD_SHA256_RESULT_STRING,
                HmacUtils.hmacSha256Hex(HmacAlgorithmsTest.STANDARD_KEY_BYTES, new ByteArrayInputStream(HmacAlgorithmsTest.STANDARD_PHRASE_BYTES)));
        Assert.assertEquals(HmacAlgorithmsTest.STANDARD_SHA256_RESULT_STRING,
                HmacUtils.hmacSha256Hex(HmacAlgorithmsTest.STANDARD_KEY_STRING, HmacAlgorithmsTest.STANDARD_PHRASE_STRING));
    }

// org.apache.commons.codec.digest.HmacUtilsTest::testSha256HMacFail
    public void testSha256HMacFail() throws IOException {
        HmacUtils.hmacSha256((byte[]) null, HmacAlgorithmsTest.STANDARD_PHRASE_BYTES);
    }

// org.apache.commons.codec.digest.HmacUtilsTest::testSha384HMac
    public void testSha384HMac() throws IOException {
        Assert.assertArrayEquals(HmacAlgorithmsTest.STANDARD_SHA384_RESULT_BYTES,
                HmacUtils.hmacSha384(HmacAlgorithmsTest.STANDARD_KEY_BYTES, HmacAlgorithmsTest.STANDARD_PHRASE_BYTES));
        Assert.assertArrayEquals(HmacAlgorithmsTest.STANDARD_SHA384_RESULT_BYTES,
                HmacUtils.hmacSha384(HmacAlgorithmsTest.STANDARD_KEY_BYTES, new ByteArrayInputStream(HmacAlgorithmsTest.STANDARD_PHRASE_BYTES)));
        Assert.assertArrayEquals(HmacAlgorithmsTest.STANDARD_SHA384_RESULT_BYTES,
                HmacUtils.hmacSha384(HmacAlgorithmsTest.STANDARD_KEY_STRING, HmacAlgorithmsTest.STANDARD_PHRASE_STRING));
        Assert.assertEquals(HmacAlgorithmsTest.STANDARD_SHA384_RESULT_STRING,
                HmacUtils.hmacSha384Hex(HmacAlgorithmsTest.STANDARD_KEY_BYTES, HmacAlgorithmsTest.STANDARD_PHRASE_BYTES));
        Assert.assertEquals(HmacAlgorithmsTest.STANDARD_SHA384_RESULT_STRING,
                HmacUtils.hmacSha384Hex(HmacAlgorithmsTest.STANDARD_KEY_BYTES, new ByteArrayInputStream(HmacAlgorithmsTest.STANDARD_PHRASE_BYTES)));
        Assert.assertEquals(HmacAlgorithmsTest.STANDARD_SHA384_RESULT_STRING,
                HmacUtils.hmacSha384Hex(HmacAlgorithmsTest.STANDARD_KEY_STRING, HmacAlgorithmsTest.STANDARD_PHRASE_STRING));
    }

// org.apache.commons.codec.digest.HmacUtilsTest::testSha384HMacFail
    public void testSha384HMacFail() throws IOException {
        HmacUtils.hmacSha384((byte[]) null, HmacAlgorithmsTest.STANDARD_PHRASE_BYTES);
    }

// org.apache.commons.codec.digest.HmacUtilsTest::testSha512HMac
    public void testSha512HMac() throws IOException {
        Assert.assertArrayEquals(HmacAlgorithmsTest.STANDARD_SHA512_RESULT_BYTES,
                HmacUtils.hmacSha512(HmacAlgorithmsTest.STANDARD_KEY_BYTES, HmacAlgorithmsTest.STANDARD_PHRASE_BYTES));
        Assert.assertArrayEquals(HmacAlgorithmsTest.STANDARD_SHA512_RESULT_BYTES,
                HmacUtils.hmacSha512(HmacAlgorithmsTest.STANDARD_KEY_BYTES, new ByteArrayInputStream(HmacAlgorithmsTest.STANDARD_PHRASE_BYTES)));
        Assert.assertArrayEquals(HmacAlgorithmsTest.STANDARD_SHA512_RESULT_BYTES,
                HmacUtils.hmacSha512(HmacAlgorithmsTest.STANDARD_KEY_STRING, HmacAlgorithmsTest.STANDARD_PHRASE_STRING));
        Assert.assertEquals(HmacAlgorithmsTest.STANDARD_SHA512_RESULT_STRING,
                HmacUtils.hmacSha512Hex(HmacAlgorithmsTest.STANDARD_KEY_BYTES, HmacAlgorithmsTest.STANDARD_PHRASE_BYTES));
        Assert.assertEquals(HmacAlgorithmsTest.STANDARD_SHA512_RESULT_STRING,
                HmacUtils.hmacSha512Hex(HmacAlgorithmsTest.STANDARD_KEY_BYTES, new ByteArrayInputStream(HmacAlgorithmsTest.STANDARD_PHRASE_BYTES)));
        Assert.assertEquals(HmacAlgorithmsTest.STANDARD_SHA512_RESULT_STRING,
                HmacUtils.hmacSha512Hex(HmacAlgorithmsTest.STANDARD_KEY_STRING, HmacAlgorithmsTest.STANDARD_PHRASE_STRING));
    }

// org.apache.commons.codec.digest.HmacUtilsTest::testSha512HMacFail
    public void testSha512HMacFail() throws IOException {
        HmacUtils.hmacSha512((byte[]) null, HmacAlgorithmsTest.STANDARD_PHRASE_BYTES);
    }

// org.apache.commons.codec.language.DoubleMetaphoneTest::testCCedilla
    public void testCCedilla() {
        assertTrue(this.getStringEncoder().isDoubleMetaphoneEqual("\u00e7", "S")); 
    }

// org.apache.commons.codec.language.DoubleMetaphoneTest::testCodec184
    public void testCodec184() throws Throwable {
        assertTrue(new DoubleMetaphone().isDoubleMetaphoneEqual("", "", false));
        assertTrue(new DoubleMetaphone().isDoubleMetaphoneEqual("", "", true));
        assertFalse(new DoubleMetaphone().isDoubleMetaphoneEqual("aa", "", false));
        assertFalse(new DoubleMetaphone().isDoubleMetaphoneEqual("aa", "", true));
        assertFalse(new DoubleMetaphone().isDoubleMetaphoneEqual("", "aa", false));
        assertFalse(new DoubleMetaphone().isDoubleMetaphoneEqual("", "aa", true));
      }

// org.apache.commons.codec.language.DoubleMetaphoneTest::testDoubleMetaphone
    public void testDoubleMetaphone() {
        assertDoubleMetaphone("TSTN", "testing");
        assertDoubleMetaphone("0", "The");
        assertDoubleMetaphone("KK", "quick");
        assertDoubleMetaphone("PRN", "brown");
        assertDoubleMetaphone("FKS", "fox");
        assertDoubleMetaphone("JMPT", "jumped");
        assertDoubleMetaphone("AFR", "over");
        assertDoubleMetaphone("0", "the");
        assertDoubleMetaphone("LS", "lazy");
        assertDoubleMetaphone("TKS", "dogs");
        assertDoubleMetaphone("MKFR", "MacCafferey");
        assertDoubleMetaphone("STFN", "Stephan");
        assertDoubleMetaphone("KSSK", "Kuczewski");
        assertDoubleMetaphone("MKLL", "McClelland");
        assertDoubleMetaphone("SNHS", "san jose");
        assertDoubleMetaphone("SNFP", "xenophobia");

        assertDoubleMetaphoneAlt("TSTN", "testing");
        assertDoubleMetaphoneAlt("T", "The");
        assertDoubleMetaphoneAlt("KK", "quick");
        assertDoubleMetaphoneAlt("PRN", "brown");
        assertDoubleMetaphoneAlt("FKS", "fox");
        assertDoubleMetaphoneAlt("AMPT", "jumped");
        assertDoubleMetaphoneAlt("AFR", "over");
        assertDoubleMetaphoneAlt("T", "the");
        assertDoubleMetaphoneAlt("LS", "lazy");
        assertDoubleMetaphoneAlt("TKS", "dogs");
        assertDoubleMetaphoneAlt("MKFR", "MacCafferey");
        assertDoubleMetaphoneAlt("STFN", "Stephan");
        assertDoubleMetaphoneAlt("KXFS", "Kutchefski");
        assertDoubleMetaphoneAlt("MKLL", "McClelland");
        assertDoubleMetaphoneAlt("SNHS", "san jose");
        assertDoubleMetaphoneAlt("SNFP", "xenophobia");
        assertDoubleMetaphoneAlt("FKR", "Fokker");
        assertDoubleMetaphoneAlt("AK", "Joqqi");
        assertDoubleMetaphoneAlt("HF", "Hovvi");
        assertDoubleMetaphoneAlt("XRN", "Czerny");
    }

// org.apache.commons.codec.language.DoubleMetaphoneTest::testEmpty
    public void testEmpty() {
        assertEquals(null, this.getStringEncoder().doubleMetaphone(null));
        assertEquals(null, this.getStringEncoder().doubleMetaphone(""));
        assertEquals(null, this.getStringEncoder().doubleMetaphone(" "));
        assertEquals(null, this.getStringEncoder().doubleMetaphone("\t\n\r "));
    }

// org.apache.commons.codec.language.DoubleMetaphoneTest::testIsDoubleMetaphoneEqualBasic
    public void testIsDoubleMetaphoneEqualBasic() {
        final String[][] testFixture = new String[][] { {
                "", "" }, {
                "Case", "case" }, {
                "CASE", "Case" }, {
                "caSe", "cAsE" }, {
                "cookie", "quick" }, {
                "quick", "cookie" }, {
                "Brian", "Bryan" }, {
                "Auto", "Otto" }, {
                "Steven", "Stefan" }, {
                "Philipowitz", "Filipowicz" }
        };
        doubleMetaphoneEqualTest(testFixture, false);
        doubleMetaphoneEqualTest(testFixture, true);
    }

// org.apache.commons.codec.language.DoubleMetaphoneTest::testIsDoubleMetaphoneEqualExtended1
    public void testIsDoubleMetaphoneEqualExtended1() {
        
        
        
        
    }

// org.apache.commons.codec.language.DoubleMetaphoneTest::testIsDoubleMetaphoneEqualExtended2
    public void testIsDoubleMetaphoneEqualExtended2() {
        final String[][] testFixture = new String[][] { { "Jablonski", "Yablonsky" }
        };
        
        doubleMetaphoneEqualTest(testFixture, true);
    }

// org.apache.commons.codec.language.DoubleMetaphoneTest::testIsDoubleMetaphoneEqualExtended3
    public void testIsDoubleMetaphoneEqualExtended3() {
        this.validateFixture(FIXTURE);
        final StringBuilder failures = new StringBuilder();
        final StringBuilder matches = new StringBuilder();
        final String cr = System.getProperty("line.separator");
        matches.append("private static final String[][] MATCHES = {" + cr);
        int failCount = 0;
        for (int i = 0; i < FIXTURE.length; i++) {
            final String name0 = FIXTURE[i][0];
            final String name1 = FIXTURE[i][1];
            final boolean match1 = this.getStringEncoder().isDoubleMetaphoneEqual(name0, name1, false);
            final boolean match2 = this.getStringEncoder().isDoubleMetaphoneEqual(name0, name1, true);
            if (match1 == false && match2 == false) {
                final String failMsg = "[" + i + "] " + name0 + " and " + name1 + cr;
                failures.append(failMsg);
                failCount++;
            } else {
                matches.append("{\"" + name0 + "\", \"" + name1 + "\"}," + cr);
            }
        }
        matches.append("};");
        
        
        if (failCount > 0) {
            
            
            
            
        }
    }

// org.apache.commons.codec.language.DoubleMetaphoneTest::testIsDoubleMetaphoneEqualWithMATCHES
    public void testIsDoubleMetaphoneEqualWithMATCHES() {
        this.validateFixture(MATCHES);
        for (int i = 0; i < MATCHES.length; i++) {
            final String name0 = MATCHES[i][0];
            final String name1 = MATCHES[i][1];
            final boolean match1 = this.getStringEncoder().isDoubleMetaphoneEqual(name0, name1, false);
            final boolean match2 = this.getStringEncoder().isDoubleMetaphoneEqual(name0, name1, true);
            if (match1 == false && match2 == false) {
                fail("Expected match [" + i + "] " + name0 + " and " + name1);
            }
        }
    }

// org.apache.commons.codec.language.DoubleMetaphoneTest::testIsDoubleMetaphoneNotEqual
    public void testIsDoubleMetaphoneNotEqual() {
        doubleMetaphoneNotEqualTest(false);
        doubleMetaphoneNotEqualTest(true);
    }

// org.apache.commons.codec.language.DoubleMetaphoneTest::testNTilde
    public void testNTilde() {
        assertTrue(this.getStringEncoder().isDoubleMetaphoneEqual("\u00f1", "N")); 
    }

// org.apache.commons.codec.language.DoubleMetaphoneTest::testSetMaxCodeLength
    public void testSetMaxCodeLength() {
        final String value = "jumped";

        final DoubleMetaphone doubleMetaphone = new DoubleMetaphone();

        
        assertEquals("Default Max Code Length", 4, doubleMetaphone.getMaxCodeLen());
        assertEquals("Default Primary",   "JMPT", doubleMetaphone.doubleMetaphone(value, false));
        assertEquals("Default Alternate", "AMPT", doubleMetaphone.doubleMetaphone(value, true));

        
        doubleMetaphone.setMaxCodeLen(3);
        assertEquals("Set Max Code Length", 3, doubleMetaphone.getMaxCodeLen());
        assertEquals("Max=3 Primary",   "JMP", doubleMetaphone.doubleMetaphone(value, false));
        assertEquals("Max=3 Alternate", "AMP", doubleMetaphone.doubleMetaphone(value, true));
    }

// org.apache.commons.codec.net.BCodecTest::testNullInput
    public void testNullInput() throws Exception {
        final BCodec bcodec = new BCodec();
        assertNull(bcodec.doDecoding(null));
        assertNull(bcodec.doEncoding(null));
    }

// org.apache.commons.codec.net.BCodecTest::testUTF8RoundTrip
    public void testUTF8RoundTrip() throws Exception {

        final String ru_msg = constructString(RUSSIAN_STUFF_UNICODE);
        final String ch_msg = constructString(SWISS_GERMAN_STUFF_UNICODE);

        final BCodec bcodec = new BCodec(CharEncoding.UTF_8);

        assertEquals("=?UTF-8?B?0JLRgdC10Lxf0L/RgNC40LLQtdGC?=", bcodec.encode(ru_msg));
        assertEquals("=?UTF-8?B?R3LDvGV6aV96w6Rtw6Q=?=", bcodec.encode(ch_msg));

        assertEquals(ru_msg, bcodec.decode(bcodec.encode(ru_msg)));
        assertEquals(ch_msg, bcodec.decode(bcodec.encode(ch_msg)));
    }

// org.apache.commons.codec.net.BCodecTest::testBasicEncodeDecode
    public void testBasicEncodeDecode() throws Exception {
        final BCodec bcodec = new BCodec();
        final String plain = "Hello there";
        final String encoded = bcodec.encode(plain);
        assertEquals("Basic B encoding test", "=?UTF-8?B?SGVsbG8gdGhlcmU=?=", encoded);
        assertEquals("Basic B decoding test", plain, bcodec.decode(encoded));
    }

// org.apache.commons.codec.net.BCodecTest::testEncodeDecodeNull
    public void testEncodeDecodeNull() throws Exception {
        final BCodec bcodec = new BCodec();
        assertNull("Null string B encoding test", bcodec.encode((String) null));
        assertNull("Null string B decoding test", bcodec.decode((String) null));
    }

// org.apache.commons.codec.net.BCodecTest::testEncodeStringWithNull
    public void testEncodeStringWithNull() throws Exception {
        final BCodec bcodec = new BCodec();
        final String test = null;
        final String result = bcodec.encode(test, "charset");
        assertEquals("Result should be null", null, result);
    }

// org.apache.commons.codec.net.BCodecTest::testDecodeStringWithNull
    public void testDecodeStringWithNull() throws Exception {
        final BCodec bcodec = new BCodec();
        final String test = null;
        final String result = bcodec.decode(test);
        assertEquals("Result should be null", null, result);
    }

// org.apache.commons.codec.net.BCodecTest::testEncodeObjects
    public void testEncodeObjects() throws Exception {
        final BCodec bcodec = new BCodec();
        final String plain = "what not";
        final String encoded = (String) bcodec.encode((Object) plain);

        assertEquals("Basic B encoding test", "=?UTF-8?B?d2hhdCBub3Q=?=", encoded);

        final Object result = bcodec.encode((Object) null);
        assertEquals("Encoding a null Object should return null", null, result);

        try {
            final Object dObj = new Double(3.0);
            bcodec.encode(dObj);
            fail("Trying to url encode a Double object should cause an exception.");
        } catch (final EncoderException ee) {
            
        }
    }

// org.apache.commons.codec.net.BCodecTest::testInvalidEncoding
    public void testInvalidEncoding() {
        new BCodec("NONSENSE");
    }

// org.apache.commons.codec.net.BCodecTest::testDecodeObjects
    public void testDecodeObjects() throws Exception {
        final BCodec bcodec = new BCodec();
        final String decoded = "=?UTF-8?B?d2hhdCBub3Q=?=";
        final String plain = (String) bcodec.decode((Object) decoded);
        assertEquals("Basic B decoding test", "what not", plain);

        final Object result = bcodec.decode((Object) null);
        assertEquals("Decoding a null Object should return null", null, result);

        try {
            final Object dObj = new Double(3.0);
            bcodec.decode(dObj);
            fail("Trying to url encode a Double object should cause an exception.");
        } catch (final DecoderException ee) {
            
        }
    }

// org.apache.commons.codec.net.QCodecTest::testNullInput
    public void testNullInput() throws Exception {
        final QCodec qcodec = new QCodec();
        assertNull(qcodec.doDecoding(null));
        assertNull(qcodec.doEncoding(null));
    }

// org.apache.commons.codec.net.QCodecTest::testUTF8RoundTrip
    public void testUTF8RoundTrip() throws Exception {

        final String ru_msg = constructString(RUSSIAN_STUFF_UNICODE);
        final String ch_msg = constructString(SWISS_GERMAN_STUFF_UNICODE);

        final QCodec qcodec = new QCodec(CharEncoding.UTF_8);

        assertEquals(
            "=?UTF-8?Q?=D0=92=D1=81=D0=B5=D0=BC=5F=D0=BF=D1=80=D0=B8=D0=B2=D0=B5=D1=82?=",
        qcodec.encode(ru_msg)
        );
        assertEquals("=?UTF-8?Q?Gr=C3=BCezi=5Fz=C3=A4m=C3=A4?=", qcodec.encode(ch_msg));

        assertEquals(ru_msg, qcodec.decode(qcodec.encode(ru_msg)));
        assertEquals(ch_msg, qcodec.decode(qcodec.encode(ch_msg)));
    }

// org.apache.commons.codec.net.QCodecTest::testBasicEncodeDecode
    public void testBasicEncodeDecode() throws Exception {
        final QCodec qcodec = new QCodec();
        final String plain = "= Hello there =\r\n";
        final String encoded = qcodec.encode(plain);
        assertEquals("Basic Q encoding test",
            "=?UTF-8?Q?=3D Hello there =3D=0D=0A?=", encoded);
        assertEquals("Basic Q decoding test",
            plain, qcodec.decode(encoded));
    }

// org.apache.commons.codec.net.QCodecTest::testUnsafeEncodeDecode
    public void testUnsafeEncodeDecode() throws Exception {
        final QCodec qcodec = new QCodec();
        final String plain = "?_=\r\n";
        final String encoded = qcodec.encode(plain);
        assertEquals("Unsafe chars Q encoding test",
            "=?UTF-8?Q?=3F=5F=3D=0D=0A?=", encoded);
        assertEquals("Unsafe chars Q decoding test",
            plain, qcodec.decode(encoded));
    }

// org.apache.commons.codec.net.QCodecTest::testEncodeDecodeNull
    public void testEncodeDecodeNull() throws Exception {
        final QCodec qcodec = new QCodec();
        assertNull("Null string Q encoding test",
            qcodec.encode((String)null));
        assertNull("Null string Q decoding test",
            qcodec.decode((String)null));
    }

// org.apache.commons.codec.net.QCodecTest::testEncodeStringWithNull
    public void testEncodeStringWithNull() throws Exception {
        final QCodec qcodec = new QCodec();
        final String test = null;
        final String result = qcodec.encode( test, "charset" );
        assertEquals("Result should be null", null, result);
    }

// org.apache.commons.codec.net.QCodecTest::testDecodeStringWithNull
    public void testDecodeStringWithNull() throws Exception {
        final QCodec qcodec = new QCodec();
        final String test = null;
        final String result = qcodec.decode( test );
        assertEquals("Result should be null", null, result);
    }

// org.apache.commons.codec.net.QCodecTest::testEncodeObjects
    public void testEncodeObjects() throws Exception {
        final QCodec qcodec = new QCodec();
        final String plain = "1+1 = 2";
        final String encoded = (String) qcodec.encode((Object) plain);
        assertEquals("Basic Q encoding test",
            "=?UTF-8?Q?1+1 =3D 2?=", encoded);

        final Object result = qcodec.encode((Object) null);
        assertEquals( "Encoding a null Object should return null", null, result);

        try {
            final Object dObj = new Double(3.0);
            qcodec.encode( dObj );
            fail( "Trying to url encode a Double object should cause an exception.");
        } catch (final EncoderException ee) {
            
        }
    }

// org.apache.commons.codec.net.QCodecTest::testInvalidEncoding
    public void testInvalidEncoding() {
        new QCodec("NONSENSE");
    }

// org.apache.commons.codec.net.QCodecTest::testDecodeObjects
    public void testDecodeObjects() throws Exception {
        final QCodec qcodec = new QCodec();
        final String decoded = "=?UTF-8?Q?1+1 =3D 2?=";
        final String plain = (String) qcodec.decode((Object) decoded);
        assertEquals("Basic Q decoding test",
            "1+1 = 2", plain);

        final Object result = qcodec.decode((Object) null);
        assertEquals( "Decoding a null Object should return null", null, result);

        try {
            final Object dObj = new Double(3.0);
            qcodec.decode( dObj );
            fail( "Trying to url encode a Double object should cause an exception.");
        } catch (final DecoderException ee) {
            
        }
    }

// org.apache.commons.codec.net.QCodecTest::testEncodeDecodeBlanks
    public void testEncodeDecodeBlanks() throws Exception {
        final String plain = "Mind those pesky blanks";
        final String encoded1 = "=?UTF-8?Q?Mind those pesky blanks?=";
        final String encoded2 = "=?UTF-8?Q?Mind_those_pesky_blanks?=";
        final QCodec qcodec = new QCodec();
        qcodec.setEncodeBlanks(false);
        String s = qcodec.encode(plain);
        assertEquals("Blanks encoding with the Q codec test", encoded1, s);
        qcodec.setEncodeBlanks(true);
        s = qcodec.encode(plain);
        assertEquals("Blanks encoding with the Q codec test", encoded2, s);
        s = qcodec.decode(encoded1);
        assertEquals("Blanks decoding with the Q codec test", plain, s);
        s = qcodec.decode(encoded2);
        assertEquals("Blanks decoding with the Q codec test", plain, s);
    }

// org.apache.commons.codec.net.QCodecTest::testLetUsMakeCloverHappy
    public void testLetUsMakeCloverHappy() throws Exception {
        final QCodec qcodec = new QCodec();
        qcodec.setEncodeBlanks(true);
        assertTrue(qcodec.isEncodeBlanks());
        qcodec.setEncodeBlanks(false);
        assertFalse(qcodec.isEncodeBlanks());
    }

// org.apache.commons.codec.net.QuotedPrintableCodecTest::testUTF8RoundTrip
    public void testUTF8RoundTrip() throws Exception {

        final String ru_msg = constructString(RUSSIAN_STUFF_UNICODE);
        final String ch_msg = constructString(SWISS_GERMAN_STUFF_UNICODE);

        final QuotedPrintableCodec qpcodec = new QuotedPrintableCodec();

        assertEquals(
            "=D0=92=D1=81=D0=B5=D0=BC_=D0=BF=D1=80=D0=B8=D0=B2=D0=B5=D1=82",
        qpcodec.encode(ru_msg, CharEncoding.UTF_8)
        );
        assertEquals("Gr=C3=BCezi_z=C3=A4m=C3=A4", qpcodec.encode(ch_msg, CharEncoding.UTF_8));

        assertEquals(ru_msg, qpcodec.decode(qpcodec.encode(ru_msg, CharEncoding.UTF_8), CharEncoding.UTF_8));
        assertEquals(ch_msg, qpcodec.decode(qpcodec.encode(ch_msg, CharEncoding.UTF_8), CharEncoding.UTF_8));
    }

// org.apache.commons.codec.net.QuotedPrintableCodecTest::testBasicEncodeDecode
    public void testBasicEncodeDecode() throws Exception {
        final QuotedPrintableCodec qpcodec = new QuotedPrintableCodec();
        final String plain = "= Hello there =\r\n";
        final String encoded = qpcodec.encode(plain);
        assertEquals("Basic quoted-printable encoding test",
            "=3D Hello there =3D=0D=0A", encoded);
        assertEquals("Basic quoted-printable decoding test",
            plain, qpcodec.decode(encoded));
    }

// org.apache.commons.codec.net.QuotedPrintableCodecTest::testSafeCharEncodeDecode
    public void testSafeCharEncodeDecode() throws Exception {
        final QuotedPrintableCodec qpcodec = new QuotedPrintableCodec();
        final String plain = "abc123_-.*~!@#$%^&()+{}\"\\;:`,/[]";
        final String encoded = qpcodec.encode(plain);
        assertEquals("Safe chars quoted-printable encoding test",
            plain, encoded);
        assertEquals("Safe chars quoted-printable decoding test",
            plain, qpcodec.decode(encoded));
    }

// org.apache.commons.codec.net.QuotedPrintableCodecTest::testUnsafeEncodeDecode
    public void testUnsafeEncodeDecode() throws Exception {
        final QuotedPrintableCodec qpcodec = new QuotedPrintableCodec();
        final String plain = "=\r\n";
        final String encoded = qpcodec.encode(plain);
        assertEquals("Unsafe chars quoted-printable encoding test",
            "=3D=0D=0A", encoded);
        assertEquals("Unsafe chars quoted-printable decoding test",
            plain, qpcodec.decode(encoded));
    }

// org.apache.commons.codec.net.QuotedPrintableCodecTest::testEncodeDecodeNull
    public void testEncodeDecodeNull() throws Exception {
        final QuotedPrintableCodec qpcodec = new QuotedPrintableCodec();
        assertNull("Null string quoted-printable encoding test",
            qpcodec.encode((String)null));
        assertNull("Null string quoted-printable decoding test",
            qpcodec.decode((String)null));
    }

// org.apache.commons.codec.net.QuotedPrintableCodecTest::testDecodeInvalid
    public void testDecodeInvalid() throws Exception {
        final QuotedPrintableCodec qpcodec = new QuotedPrintableCodec();
        try {
            qpcodec.decode("=");
            fail("DecoderException should have been thrown");
        } catch (final DecoderException e) {
            
        }
        try {
            qpcodec.decode("=A");
            fail("DecoderException should have been thrown");
        } catch (final DecoderException e) {
            
        }
        try {
            qpcodec.decode("=WW");
            fail("DecoderException should have been thrown");
        } catch (final DecoderException e) {
            
        }
    }

// org.apache.commons.codec.net.QuotedPrintableCodecTest::testEncodeNull
    public void testEncodeNull() throws Exception {
        final QuotedPrintableCodec qpcodec = new QuotedPrintableCodec();
        final byte[] plain = null;
        final byte[] encoded = qpcodec.encode(plain);
        assertEquals("Encoding a null string should return null",
            null, encoded);
    }

// org.apache.commons.codec.net.QuotedPrintableCodecTest::testEncodeUrlWithNullBitSet
    public void testEncodeUrlWithNullBitSet() throws Exception {
        final QuotedPrintableCodec qpcodec = new QuotedPrintableCodec();
        final String plain = "1+1 = 2";
        final String encoded = new String(QuotedPrintableCodec.
            encodeQuotedPrintable(null, plain.getBytes(Charsets.UTF_8)));
        assertEquals("Basic quoted-printable encoding test",
            "1+1 =3D 2", encoded);
        assertEquals("Basic quoted-printable decoding test",
            plain, qpcodec.decode(encoded));

    }

// org.apache.commons.codec.net.QuotedPrintableCodecTest::testDecodeWithNullArray
    public void testDecodeWithNullArray() throws Exception {
        final byte[] plain = null;
        final byte[] result = QuotedPrintableCodec.decodeQuotedPrintable( plain );
        assertEquals("Result should be null", null, result);
    }

// org.apache.commons.codec.net.QuotedPrintableCodecTest::testEncodeStringWithNull
    public void testEncodeStringWithNull() throws Exception {
        final QuotedPrintableCodec qpcodec = new QuotedPrintableCodec();
        final String test = null;
        final String result = qpcodec.encode( test, "charset" );
        assertEquals("Result should be null", null, result);
    }

// org.apache.commons.codec.net.QuotedPrintableCodecTest::testDecodeStringWithNull
    public void testDecodeStringWithNull() throws Exception {
        final QuotedPrintableCodec qpcodec = new QuotedPrintableCodec();
        final String test = null;
        final String result = qpcodec.decode( test, "charset" );
        assertEquals("Result should be null", null, result);
    }

// org.apache.commons.codec.net.QuotedPrintableCodecTest::testEncodeObjects
    public void testEncodeObjects() throws Exception {
        final QuotedPrintableCodec qpcodec = new QuotedPrintableCodec();
        final String plain = "1+1 = 2";
        String encoded = (String) qpcodec.encode((Object) plain);
        assertEquals("Basic quoted-printable encoding test",
            "1+1 =3D 2", encoded);

        final byte[] plainBA = plain.getBytes(Charsets.UTF_8);
        final byte[] encodedBA = (byte[]) qpcodec.encode((Object) plainBA);
        encoded = new String(encodedBA);
        assertEquals("Basic quoted-printable encoding test",
            "1+1 =3D 2", encoded);

        final Object result = qpcodec.encode((Object) null);
        assertEquals( "Encoding a null Object should return null", null, result);

        try {
            final Object dObj = new Double(3.0);
            qpcodec.encode( dObj );
            fail( "Trying to url encode a Double object should cause an exception.");
        } catch (final EncoderException ee) {
            
        }
    }

// org.apache.commons.codec.net.QuotedPrintableCodecTest::testInvalidEncoding
    public void testInvalidEncoding() {
        new QuotedPrintableCodec("NONSENSE");
    }

// org.apache.commons.codec.net.QuotedPrintableCodecTest::testDecodeObjects
    public void testDecodeObjects() throws Exception {
        final QuotedPrintableCodec qpcodec = new QuotedPrintableCodec();
        final String plain = "1+1 =3D 2";
        String decoded = (String) qpcodec.decode((Object) plain);
        assertEquals("Basic quoted-printable decoding test",
            "1+1 = 2", decoded);

        final byte[] plainBA = plain.getBytes(Charsets.UTF_8);
        final byte[] decodedBA = (byte[]) qpcodec.decode((Object) plainBA);
        decoded = new String(decodedBA);
        assertEquals("Basic quoted-printable decoding test",
            "1+1 = 2", decoded);

        final Object result = qpcodec.decode((Object) null);
        assertEquals( "Decoding a null Object should return null", null, result);

        try {
            final Object dObj = new Double(3.0);
            qpcodec.decode( dObj );
            fail( "Trying to url encode a Double object should cause an exception.");
        } catch (final DecoderException ee) {
            
        }
    }

// org.apache.commons.codec.net.QuotedPrintableCodecTest::testDefaultEncoding
    public void testDefaultEncoding() throws Exception {
        final String plain = "Hello there!";
        final QuotedPrintableCodec qpcodec = new QuotedPrintableCodec("UnicodeBig");
        qpcodec.encode(plain); 
        final String encoded1 = qpcodec.encode(plain, "UnicodeBig");
        final String encoded2 = qpcodec.encode(plain);
        assertEquals(encoded1, encoded2);
    }

// org.apache.commons.codec.net.QuotedPrintableCodecTest::testSoftLineBreakDecode
    public void testSoftLineBreakDecode() throws Exception {
        final String qpdata = "If you believe that truth=3Dbeauty, then surely=20=\r\nmathematics is the most beautiful branch of philosophy.";
        final String expected = "If you believe that truth=beauty, then surely mathematics is the most beautiful branch of philosophy.";

        final QuotedPrintableCodec qpcodec = new QuotedPrintableCodec();
        assertEquals(expected, qpcodec.decode(qpdata));

        final String encoded = qpcodec.encode(expected);
        assertEquals(expected, qpcodec.decode(encoded));
    }

// org.apache.commons.codec.net.QuotedPrintableCodecTest::testSoftLineBreakEncode
    public void testSoftLineBreakEncode() throws Exception {
        final String qpdata = "If you believe that truth=3Dbeauty, then surely mathematics is the most b=\r\neautiful branch of philosophy.";
        final String expected = "If you believe that truth=beauty, then surely mathematics is the most beautiful branch of philosophy.";

        final QuotedPrintableCodec qpcodec = new QuotedPrintableCodec(true);
        assertEquals(qpdata, qpcodec.encode(expected));

        final String decoded = qpcodec.decode(qpdata);
        assertEquals(qpdata, qpcodec.encode(decoded));
    }

// org.apache.commons.codec.net.QuotedPrintableCodecTest::testSkipNotEncodedCRLF
    public void testSkipNotEncodedCRLF() throws Exception {
        final String qpdata = "CRLF in an\n encoded text should be=20=\r\n\rskipped in the\r decoding.";
        final String expected = "CRLF in an encoded text should be skipped in the decoding.";

        final QuotedPrintableCodec qpcodec = new QuotedPrintableCodec(true);
        assertEquals(expected, qpcodec.decode(qpdata));

        final String encoded = qpcodec.encode(expected);
        assertEquals(expected, qpcodec.decode(encoded));
    }

// org.apache.commons.codec.net.QuotedPrintableCodecTest::testTrailingSpecial
    public void testTrailingSpecial() throws Exception {
        final QuotedPrintableCodec qpcodec = new QuotedPrintableCodec(true);

        String plain ="This is a example of a quoted-printable text file. This might contain sp=cial chars.";
        String expected = "This is a example of a quoted-printable text file. This might contain sp=3D=\r\ncial chars.";
        assertEquals(expected, qpcodec.encode(plain));

        plain ="This is a example of a quoted-printable text file. This might contain ta\tbs as well.";
        expected = "This is a example of a quoted-printable text file. This might contain ta=09=\r\nbs as well.";
        assertEquals(expected, qpcodec.encode(plain));
    }

// org.apache.commons.codec.net.QuotedPrintableCodecTest::testUltimateSoftBreak
    public void testUltimateSoftBreak() throws Exception {
        final QuotedPrintableCodec qpcodec = new QuotedPrintableCodec(true);

        String plain ="This is a example of a quoted-printable text file. There is no end to it\t";
        String expected = "This is a example of a quoted-printable text file. There is no end to i=\r\nt=09";

        assertEquals(expected, qpcodec.encode(plain));

        plain ="This is a example of a quoted-printable text file. There is no end to it ";
        expected = "This is a example of a quoted-printable text file. There is no end to i=\r\nt=20";

        assertEquals(expected, qpcodec.encode(plain));

        
        plain ="This is a example of a quoted-printable text file. There is no end to   ";
        expected = "This is a example of a quoted-printable text file. There is no end to=20=\r\n =20";

        assertEquals(expected, qpcodec.encode(plain));

        
        plain ="This is a example of a quoted-printable text file. There is no end to=  ";
        expected = "This is a example of a quoted-printable text file. There is no end to=3D=\r\n =20";

        assertEquals(expected, qpcodec.encode(plain));
    }

// org.apache.commons.codec.net.QuotedPrintableCodecTest::testFinalBytes
    public void testFinalBytes() throws Exception {
        
        final String plain ="This is a example of a quoted=printable text file. There is no tt";
        final String expected = "This is a example of a quoted=3Dprintable text file. There is no tt";

        assertEquals(expected, new QuotedPrintableCodec(true).encode(plain));
    }

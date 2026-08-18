// buggy code
    public static String encodeBase64String(byte[] binaryData) {
        return StringUtils.newStringUtf8(encodeBase64(binaryData, true));
    }

// relevant test
// org.apache.commons.codec.binary.Base64Codec13Test::testEncoder
    public void testEncoder() throws EncoderException {
        Encoder enc = new Base64();
        for (int i = 0; i < STRINGS.length; i++) {
            if (STRINGS[i] != null) {
                byte[] base64 = utf8(STRINGS[i]);
                byte[] binary = BYTES[i];
                boolean b = Arrays.equals(base64, (byte[]) enc.encode(binary));
                assertTrue("Encoder test-" + i, b);
            }
        }
    }

// org.apache.commons.codec.binary.Base64Codec13Test::testDecoder
    public void testDecoder() throws DecoderException {
        Decoder dec = new Base64();
        for (int i = 0; i < STRINGS.length; i++) {
            if (STRINGS[i] != null) {
                byte[] base64 = utf8(STRINGS[i]);
                byte[] binary = BYTES[i];
                boolean b = Arrays.equals(binary, (byte[]) dec.decode(base64));
                assertTrue("Decoder test-" + i, b);
            }
        }
    }

// org.apache.commons.codec.binary.Base64Codec13Test::testBinaryEncoder
    public void testBinaryEncoder() throws EncoderException {
        BinaryEncoder enc = new Base64();
        for (int i = 0; i < STRINGS.length; i++) {
            if (STRINGS[i] != null) {
                byte[] base64 = utf8(STRINGS[i]);
                byte[] binary = BYTES[i];
                boolean b = Arrays.equals(base64, enc.encode(binary));
                assertTrue("BinaryEncoder test-" + i, b);
            }
        }
    }

// org.apache.commons.codec.binary.Base64Codec13Test::testBinaryDecoder
    public void testBinaryDecoder() throws DecoderException {
        BinaryDecoder dec = new Base64();
        for (int i = 0; i < STRINGS.length; i++) {
            if (STRINGS[i] != null) {
                byte[] base64 = utf8(STRINGS[i]);
                byte[] binary = BYTES[i];
                boolean b = Arrays.equals(binary, dec.decode(base64));
                assertTrue("BinaryDecoder test-" + i, b);
            }
        }
    }

// org.apache.commons.codec.binary.Base64Codec13Test::testStaticEncode
    public void testStaticEncode() throws EncoderException {
        for (int i = 0; i < STRINGS.length; i++) {
            if (STRINGS[i] != null) {
                byte[] base64 = utf8(STRINGS[i]);
                byte[] binary = BYTES[i];
                boolean b = Arrays.equals(base64, Base64.encodeBase64(binary));
                assertTrue("static Base64.encodeBase64() test-" + i, b);
            }
        }
    }

// org.apache.commons.codec.binary.Base64Codec13Test::testStaticDecode
    public void testStaticDecode() throws DecoderException {
        for (int i = 0; i < STRINGS.length; i++) {
            if (STRINGS[i] != null) {
                byte[] base64 = utf8(STRINGS[i]);
                byte[] binary = BYTES[i];
                boolean b = Arrays.equals(binary, Base64.decodeBase64(base64));
                assertTrue("static Base64.decodeBase64() test-" + i, b);
            }
        }
    }

// org.apache.commons.codec.binary.Base64Codec13Test::testStaticEncodeChunked
    public void testStaticEncodeChunked() throws EncoderException {
        for (int i = 0; i < STRINGS.length; i++) {
            if (STRINGS[i] != null) {
                byte[] base64Chunked = utf8(CHUNKED_STRINGS[i]);
                byte[] binary = BYTES[i];
                boolean b = Arrays.equals(base64Chunked, Base64.encodeBase64Chunked(binary));
                assertTrue("static Base64.encodeBase64Chunked() test-" + i, b);
            }
        }
    }

// org.apache.commons.codec.binary.Base64Codec13Test::testStaticDecodeChunked
    public void testStaticDecodeChunked() throws DecoderException {
        for (int i = 0; i < STRINGS.length; i++) {
            if (STRINGS[i] != null) {
                byte[] base64Chunked = utf8(CHUNKED_STRINGS[i]);
                byte[] binary = BYTES[i];
                boolean b = Arrays.equals(binary, Base64.decodeBase64(base64Chunked));
                assertTrue("static Base64.decodeBase64Chunked() test-" + i, b);
            }
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

// org.apache.commons.codec.binary.Base64OutputStreamTest::testCodec98NPE
    public void testCodec98NPE() throws Exception {
        byte[] codec98 = StringUtils.getBytesUtf8(Base64TestData.CODEC_98_NPE);
        byte[] codec98_1024 = new byte[1024];
        System.arraycopy(codec98, 0, codec98_1024, 0, codec98.length);
        ByteArrayOutputStream data = new ByteArrayOutputStream(1024);
        Base64OutputStream stream = new Base64OutputStream(data, false);
        stream.write(codec98_1024, 0, 1024);
        stream.close();

        byte[] decodedBytes = data.toByteArray();
        String decoded = StringUtils.newStringUtf8(decodedBytes);
        assertEquals(
            "codec-98 NPE Base64OutputStream", Base64TestData.CODEC_98_NPE_DECODED, decoded
        );
    }

// org.apache.commons.codec.binary.Base64OutputStreamTest::testBase64EmptyOutputStreamMimeChunkSize
    public void testBase64EmptyOutputStreamMimeChunkSize() throws Exception {
        testBase64EmptyOutputStream(Base64.MIME_CHUNK_SIZE);
    }

// org.apache.commons.codec.binary.Base64OutputStreamTest::testBase64EmptyOutputStreamPemChunkSize
    public void testBase64EmptyOutputStreamPemChunkSize() throws Exception {
        testBase64EmptyOutputStream(Base64.PEM_CHUNK_SIZE);
    }

// org.apache.commons.codec.binary.Base64OutputStreamTest::testBase64OutputStreamByChunk
    public void testBase64OutputStreamByChunk() throws Exception {
        
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

// org.apache.commons.codec.binary.Base64OutputStreamTest::testWriteOutOfBounds
    public void testWriteOutOfBounds() throws Exception {
        byte[] buf = new byte[1024];
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        Base64OutputStream out = new Base64OutputStream(bout);

        try {
            out.write(buf, -1, 1);
            fail("Expected Base64OutputStream.write(buf, -1, 1) to throw a IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException ioobe) {
            
        }

        try {
            out.write(buf, 1, -1);
            fail("Expected Base64OutputStream.write(buf, 1, -1) to throw a IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException ioobe) {
            
        }

        try {
            out.write(buf, buf.length + 1, 0);
            fail("Expected Base64OutputStream.write(buf, buf.length + 1, 0) to throw a IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException ioobe) {
            
        }

        try {
            out.write(buf, buf.length - 1, 2);
            fail("Expected Base64OutputStream.write(buf, buf.length - 1, 2) to throw a IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException ioobe) {
            
        }
    }

// org.apache.commons.codec.binary.Base64OutputStreamTest::testWriteToNullCoverage
    public void testWriteToNullCoverage() throws Exception {
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        Base64OutputStream out = new Base64OutputStream(bout);
        try {
            out.write(null, 0, 0);
            fail("Expcted Base64OutputStream.write(null) to throw a NullPointerException");
        } catch (NullPointerException e) {
            
        }
    }

// org.apache.commons.codec.binary.Base64Test::testBase64
    public void testBase64() {
        String content = "Hello World";
        String encodedContent;
        byte[] encodedBytes = Base64.encodeBase64(StringUtils.getBytesUtf8(content));
        encodedContent = StringUtils.newStringUtf8(encodedBytes);
        assertTrue("encoding hello world", encodedContent.equals("SGVsbG8gV29ybGQ="));

        Base64 b64 = new Base64(Base64.MIME_CHUNK_SIZE, null);  
        encodedBytes = b64.encode(StringUtils.getBytesUtf8(content));
        encodedContent = StringUtils.newStringUtf8(encodedBytes);
        assertTrue("encoding hello world", encodedContent.equals("SGVsbG8gV29ybGQ="));

        b64 = new Base64(0, null);  
        encodedBytes = b64.encode(StringUtils.getBytesUtf8(content));
        encodedContent = StringUtils.newStringUtf8(encodedBytes);
        assertTrue("encoding hello world", encodedContent.equals("SGVsbG8gV29ybGQ="));

        
        byte[] decode = b64.decode("SGVsbG{éééééé}8gV29ybGQ=");
        String decodeString = StringUtils.newStringUtf8(decode);
        assertTrue("decode hello world", decodeString.equals("Hello World"));        
    }

// org.apache.commons.codec.binary.Base64Test::testChunkedEncodeMultipleOf76
    public void testChunkedEncodeMultipleOf76() {
        byte[] expectedEncode = Base64.encodeBase64(Base64TestData.DECODED, true);
        
        
        String actualResult = Base64TestData.ENCODED_76_CHARS_PER_LINE.replaceAll("\n", "\r\n");
        byte[] actualEncode = StringUtils.getBytesUtf8(actualResult);
        assertTrue("chunkedEncodeMultipleOf76", Arrays.equals(expectedEncode, actualEncode));
    }

// org.apache.commons.codec.binary.Base64Test::testCodec68
    public void testCodec68() {
        byte[] x = new byte[]{'n', 'A', '=', '=', (byte) 0x9c};
        Base64.decodeBase64(x);
    }

// org.apache.commons.codec.binary.Base64Test::testCodeInteger1
    public void testCodeInteger1() throws UnsupportedEncodingException {
        String encodedInt1 = "li7dzDacuo67Jg7mtqEm2TRuOMU=";
        BigInteger bigInt1 = new BigInteger("85739377120809420210425962799" + "0318636601332086981");

        assertEquals(encodedInt1, new String(Base64.encodeInteger(bigInt1)));
        assertEquals(bigInt1, Base64.decodeInteger(encodedInt1.getBytes("UTF-8")));
    }

// org.apache.commons.codec.binary.Base64Test::testCodeInteger2
    public void testCodeInteger2() throws UnsupportedEncodingException {
        String encodedInt2 = "9B5ypLY9pMOmtxCeTDHgwdNFeGs=";
        BigInteger bigInt2 = new BigInteger("13936727572861167254666467268" + "91466679477132949611");

        assertEquals(encodedInt2, new String(Base64.encodeInteger(bigInt2)));
        assertEquals(bigInt2, Base64.decodeInteger(encodedInt2.getBytes("UTF-8")));
    }

// org.apache.commons.codec.binary.Base64Test::testCodeInteger3
    public void testCodeInteger3() throws UnsupportedEncodingException {
        String encodedInt3 = "FKIhdgaG5LGKiEtF1vHy4f3y700zaD6QwDS3IrNVGzNp2" + "rY+1LFWTK6D44AyiC1n8uWz1itkYMZF0/aKDK0Yjg==";
        BigInteger bigInt3 = new BigInteger("10806548154093873461951748545"
            + "1196989136416448805819079363524309897749044958112417136240557"
            + "4495062430572478766856090958495998158114332651671116876320938126");

        assertEquals(encodedInt3, new String(Base64.encodeInteger(bigInt3)));
        assertEquals(bigInt3, Base64.decodeInteger(encodedInt3.getBytes("UTF-8")));
    }

// org.apache.commons.codec.binary.Base64Test::testCodeInteger4
    public void testCodeInteger4() throws UnsupportedEncodingException {
        String encodedInt4 = "ctA8YGxrtngg/zKVvqEOefnwmViFztcnPBYPlJsvh6yKI"
            + "4iDm68fnp4Mi3RrJ6bZAygFrUIQLxLjV+OJtgJAEto0xAs+Mehuq1DkSFEpP3o"
            + "DzCTOsrOiS1DwQe4oIb7zVk/9l7aPtJMHW0LVlMdwZNFNNJoqMcT2ZfCPrfvYv"
            + "Q0=";
        BigInteger bigInt4 = new BigInteger("80624726256040348115552042320"
            + "6968135001872753709424419772586693950232350200555646471175944"
            + "519297087885987040810778908507262272892702303774422853675597"
            + "748008534040890923814202286633163248086055216976551456088015"
            + "338880713818192088877057717530169381044092839402438015097654"
            + "53542091716518238707344493641683483917");

        assertEquals(encodedInt4, new String(Base64.encodeInteger(bigInt4)));
        assertEquals(bigInt4, Base64.decodeInteger(encodedInt4.getBytes("UTF-8")));
    }

// org.apache.commons.codec.binary.Base64Test::testCodeIntegerEdgeCases
    public void testCodeIntegerEdgeCases() {
        
    }

// org.apache.commons.codec.binary.Base64Test::testCodeIntegerNull
    public void testCodeIntegerNull() {
        try {
            Base64.encodeInteger(null);
            fail("Exception not thrown when passing in null to encodeInteger(BigInteger)");
        } catch (NullPointerException npe) {
            
        } catch (Exception e) {
            fail("Incorrect Exception caught when passing in null to encodeInteger(BigInteger)");
        }
    }

// org.apache.commons.codec.binary.Base64Test::testConstructors
    public void testConstructors() {
        Base64 base64;
        base64 = new Base64();
        base64 = new Base64(-1);
        base64 = new Base64(-1, new byte[]{});
        base64 = new Base64(64, new byte[]{});
        try {
            base64 = new Base64(-1, new byte[]{'A'});
            fail("Should have rejected attempt to use 'A' as a line separator");
        } catch (IllegalArgumentException ignored) {
            
        }
        try {
            base64 = new Base64(64, new byte[]{'A'});
            fail("Should have rejected attempt to use 'A' as a line separator");
        } catch (IllegalArgumentException ignored) {
            
        }
        try {
            base64 = new Base64(64, new byte[]{'='});
            fail("Should have rejected attempt to use '=' as a line separator");
        } catch (IllegalArgumentException ignored) {
            
        }
        base64 = new Base64(64, new byte[]{'$'}); 
        try {
            base64 = new Base64(64, new byte[]{'A', '$'});
            fail("Should have rejected attempt to use 'A$' as a line separator");
        } catch (IllegalArgumentException ignored) {
            
        }
        base64 = new Base64(64, new byte[]{' ', '$', '\n', '\r', '\t'}); 
    }

// org.apache.commons.codec.binary.Base64Test::testConstructor_Int_ByteArray_Boolean
    public void testConstructor_Int_ByteArray_Boolean() {
        Base64 base64 = new Base64(65, new byte[]{'\t'}, false);
        byte[] encoded = base64.encode(Base64TestData.DECODED);
        String expectedResult = Base64TestData.ENCODED_64_CHARS_PER_LINE;
        expectedResult = expectedResult.replace('\n', '\t');
        String result = StringUtils.newStringUtf8(encoded);
        assertEquals("new Base64(65, \\t, false)", expectedResult, result);
    }

// org.apache.commons.codec.binary.Base64Test::testConstructor_Int_ByteArray_Boolean_UrlSafe
    public void testConstructor_Int_ByteArray_Boolean_UrlSafe() {
        
        Base64 base64 = new Base64(64, new byte[]{'\t'}, true);
        byte[] encoded = base64.encode(Base64TestData.DECODED);
        String expectedResult = Base64TestData.ENCODED_64_CHARS_PER_LINE;
        expectedResult = expectedResult.replaceAll("=", ""); 
        expectedResult = expectedResult.replace('\n', '\t');
        expectedResult = expectedResult.replace('+', '-');
        expectedResult = expectedResult.replace('/', '_');
        String result = StringUtils.newStringUtf8(encoded);
        assertEquals("new Base64(64, \\t, true)", result, expectedResult);
    }

// org.apache.commons.codec.binary.Base64Test::testDecodePadMarkerIndex2
    public void testDecodePadMarkerIndex2() throws UnsupportedEncodingException {
        assertEquals("A", new String(Base64.decodeBase64("QQ==".getBytes("UTF-8"))));
    }

// org.apache.commons.codec.binary.Base64Test::testDecodePadMarkerIndex3
    public void testDecodePadMarkerIndex3() throws UnsupportedEncodingException {
        assertEquals("AA", new String(Base64.decodeBase64("QUE=".getBytes("UTF-8"))));
        assertEquals("AAA", new String(Base64.decodeBase64("QUFB".getBytes("UTF-8"))));
    }

// org.apache.commons.codec.binary.Base64Test::testDecodePadOnly
    public void testDecodePadOnly() throws UnsupportedEncodingException {
        assertTrue(Base64.decodeBase64("====".getBytes("UTF-8")).length == 0);
        assertEquals("", new String(Base64.decodeBase64("====".getBytes("UTF-8"))));
        
        assertTrue(Base64.decodeBase64("===".getBytes("UTF-8")).length == 0);
        assertTrue(Base64.decodeBase64("==".getBytes("UTF-8")).length == 0);
        assertTrue(Base64.decodeBase64("=".getBytes("UTF-8")).length == 0);
        assertTrue(Base64.decodeBase64("".getBytes("UTF-8")).length == 0);
    }

// org.apache.commons.codec.binary.Base64Test::testDecodePadOnlyChunked
    public void testDecodePadOnlyChunked() throws UnsupportedEncodingException {
        assertTrue(Base64.decodeBase64("====\n".getBytes("UTF-8")).length == 0);
        assertEquals("", new String(Base64.decodeBase64("====\n".getBytes("UTF-8"))));
        
        assertTrue(Base64.decodeBase64("===\n".getBytes("UTF-8")).length == 0);
        assertTrue(Base64.decodeBase64("==\n".getBytes("UTF-8")).length == 0);
        assertTrue(Base64.decodeBase64("=\n".getBytes("UTF-8")).length == 0);
        assertTrue(Base64.decodeBase64("\n".getBytes("UTF-8")).length == 0);
    }

// org.apache.commons.codec.binary.Base64Test::testDecodeWithWhitespace
    public void testDecodeWithWhitespace() throws Exception {

        String orig = "I am a late night coder.";

        byte[] encodedArray = Base64.encodeBase64(orig.getBytes("UTF-8"));
        StringBuffer intermediate = new StringBuffer(new String(encodedArray));

        intermediate.insert(2, ' ');
        intermediate.insert(5, '\t');
        intermediate.insert(10, '\r');
        intermediate.insert(15, '\n');

        byte[] encodedWithWS = intermediate.toString().getBytes("UTF-8");
        byte[] decodedWithWS = Base64.decodeBase64(encodedWithWS);

        String dest = new String(decodedWithWS);

        assertTrue("Dest string doesn't equal the original", dest.equals(orig));
    }

// org.apache.commons.codec.binary.Base64Test::testDiscardWhitespace
    public void testDiscardWhitespace() throws Exception {

        String orig = "I am a late night coder.";

        byte[] encodedArray = Base64.encodeBase64(orig.getBytes("UTF-8"));
        StringBuffer intermediate = new StringBuffer(new String(encodedArray));

        intermediate.insert(2, ' ');
        intermediate.insert(5, '\t');
        intermediate.insert(10, '\r');
        intermediate.insert(15, '\n');

        byte[] encodedWithWS = intermediate.toString().getBytes("UTF-8");
        byte[] encodedNoWS = Base64.discardWhitespace(encodedWithWS);
        byte[] decodedWithWS = Base64.decodeBase64(encodedWithWS);
        byte[] decodedNoWS = Base64.decodeBase64(encodedNoWS);

        String destFromWS = new String(decodedWithWS);
        String destFromNoWS = new String(decodedNoWS);

        assertTrue("Dest string doesn't equal original", destFromWS.equals(orig));
        assertTrue("Dest string doesn't equal original", destFromNoWS.equals(orig));
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
            byte[] data = new byte[this.getRandom().nextInt(10000) + 1];
            this.getRandom().nextBytes(data);
            byte[] enc = Base64.encodeBase64(data);
            assertTrue(Base64.isArrayByteBase64(enc));
            byte[] data2 = Base64.decodeBase64(enc);
            assertTrue(Arrays.equals(data, data2));
        }
    }

// org.apache.commons.codec.binary.Base64Test::testEncodeDecodeSmall
    public void testEncodeDecodeSmall() {
        for (int i = 0; i < 12; i++) {
            byte[] data = new byte[i];
            this.getRandom().nextBytes(data);
            byte[] enc = Base64.encodeBase64(data);
            assertTrue("\"" + (new String(enc)) + "\" is Base64 data.", Base64.isArrayByteBase64(enc));
            byte[] data2 = Base64.decodeBase64(enc);
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

// org.apache.commons.codec.binary.Base64Test::testIgnoringNonBase64InDecode
    public void testIgnoringNonBase64InDecode() throws Exception {
        assertEquals("The quick brown fox jumped over the lazy dogs.", new String(Base64
                .decodeBase64("VGhlIH@$#$@%F1aWN@#@#@@rIGJyb3duIGZve\n\r\t%#%#%#%CBqd##$#$W1wZWQgb3ZlciB0aGUgbGF6eSBkb2dzLg==".getBytes("UTF-8"))));
    }

// org.apache.commons.codec.binary.Base64Test::testIsArrayByteBase64
    public void testIsArrayByteBase64() {
        assertFalse(Base64.isArrayByteBase64(new byte[]{Byte.MIN_VALUE}));
        assertFalse(Base64.isArrayByteBase64(new byte[]{-125}));
        assertFalse(Base64.isArrayByteBase64(new byte[]{-10}));
        assertFalse(Base64.isArrayByteBase64(new byte[]{0}));
        assertFalse(Base64.isArrayByteBase64(new byte[]{64, Byte.MAX_VALUE}));
        assertFalse(Base64.isArrayByteBase64(new byte[]{Byte.MAX_VALUE}));
        assertTrue(Base64.isArrayByteBase64(new byte[]{'A'}));
        assertFalse(Base64.isArrayByteBase64(new byte[]{'A', Byte.MIN_VALUE}));
        assertTrue(Base64.isArrayByteBase64(new byte[]{'A', 'Z', 'a'}));
        assertTrue(Base64.isArrayByteBase64(new byte[]{'/', '=', '+'}));
        assertFalse(Base64.isArrayByteBase64(new byte[]{'$'}));
    }

// org.apache.commons.codec.binary.Base64Test::testIsUrlSafe
    public void testIsUrlSafe() {
        Base64 base64Standard = new Base64(false);
        Base64 base64URLSafe = new Base64(true);

        assertFalse("Base64.isUrlSafe=false", base64Standard.isUrlSafe());
        assertTrue("Base64.isUrlSafe=true", base64URLSafe.isUrlSafe());

        byte[] whiteSpace = {' ', '\n', '\r', '\t'};
        assertTrue("Base64.isArrayByteBase64(whiteSpace)=true", Base64.isArrayByteBase64(whiteSpace));
    }

// org.apache.commons.codec.binary.Base64Test::testKnownDecodings
    public void testKnownDecodings() throws UnsupportedEncodingException {
        assertEquals("The quick brown fox jumped over the lazy dogs.", new String(Base64
                .decodeBase64("VGhlIHF1aWNrIGJyb3duIGZveCBqdW1wZWQgb3ZlciB0aGUgbGF6eSBkb2dzLg==".getBytes("UTF-8"))));
        assertEquals("It was the best of times, it was the worst of times.", new String(Base64
                .decodeBase64("SXQgd2FzIHRoZSBiZXN0IG9mIHRpbWVzLCBpdCB3YXMgdGhlIHdvcnN0IG9mIHRpbWVzLg==".getBytes("UTF-8"))));
        assertEquals("http://jakarta.apache.org/commmons", new String(Base64
                .decodeBase64("aHR0cDovL2pha2FydGEuYXBhY2hlLm9yZy9jb21tbW9ucw==".getBytes("UTF-8"))));
        assertEquals("AaBbCcDdEeFfGgHhIiJjKkLlMmNnOoPpQqRrSsTtUuVvWwXxYyZz", new String(Base64
                .decodeBase64("QWFCYkNjRGRFZUZmR2dIaElpSmpLa0xsTW1Obk9vUHBRcVJyU3NUdFV1VnZXd1h4WXlaeg==".getBytes("UTF-8"))));
        assertEquals("{ 0, 1, 2, 3, 4, 5, 6, 7, 8, 9 }", new String(Base64.decodeBase64("eyAwLCAxLCAyLCAzLCA0LCA1LCA2LCA3LCA4LCA5IH0="
                .getBytes("UTF-8"))));
        assertEquals("xyzzy!", new String(Base64.decodeBase64("eHl6enkh".getBytes("UTF-8"))));
    }

// org.apache.commons.codec.binary.Base64Test::testKnownEncodings
    public void testKnownEncodings() throws UnsupportedEncodingException {
        assertEquals("VGhlIHF1aWNrIGJyb3duIGZveCBqdW1wZWQgb3ZlciB0aGUgbGF6eSBkb2dzLg==", new String(Base64
                .encodeBase64("The quick brown fox jumped over the lazy dogs.".getBytes("UTF-8"))));
        assertEquals(
                "YmxhaCBibGFoIGJsYWggYmxhaCBibGFoIGJsYWggYmxhaCBibGFoIGJsYWggYmxhaCBibGFoIGJs\r\nYWggYmxhaCBibGFoIGJsYWggYmxhaCBibGFoIGJsYWggYmxhaCBibGFoIGJsYWggYmxhaCBibGFo\r\nIGJsYWggYmxhaCBibGFoIGJsYWggYmxhaCBibGFoIGJsYWggYmxhaCBibGFoIGJsYWggYmxhaCBi\r\nbGFoIGJsYWg=\r\n",
                new String(
                        Base64
                                .encodeBase64Chunked("blah blah blah blah blah blah blah blah blah blah blah blah blah blah blah blah blah blah blah blah blah blah blah blah blah blah blah blah blah blah blah blah blah blah blah blah"
                                        .getBytes("UTF-8"))));
        assertEquals("SXQgd2FzIHRoZSBiZXN0IG9mIHRpbWVzLCBpdCB3YXMgdGhlIHdvcnN0IG9mIHRpbWVzLg==", new String(Base64
                .encodeBase64("It was the best of times, it was the worst of times.".getBytes("UTF-8"))));
        assertEquals("aHR0cDovL2pha2FydGEuYXBhY2hlLm9yZy9jb21tbW9ucw==", new String(Base64
                .encodeBase64("http://jakarta.apache.org/commmons".getBytes("UTF-8"))));
        assertEquals("QWFCYkNjRGRFZUZmR2dIaElpSmpLa0xsTW1Obk9vUHBRcVJyU3NUdFV1VnZXd1h4WXlaeg==", new String(Base64
                .encodeBase64("AaBbCcDdEeFfGgHhIiJjKkLlMmNnOoPpQqRrSsTtUuVvWwXxYyZz".getBytes("UTF-8"))));
        assertEquals("eyAwLCAxLCAyLCAzLCA0LCA1LCA2LCA3LCA4LCA5IH0=", new String(Base64.encodeBase64("{ 0, 1, 2, 3, 4, 5, 6, 7, 8, 9 }"
                .getBytes("UTF-8"))));
        assertEquals("eHl6enkh", new String(Base64.encodeBase64("xyzzy!".getBytes("UTF-8"))));
    }

// org.apache.commons.codec.binary.Base64Test::testNonBase64Test
    public void testNonBase64Test() throws Exception {

        byte[] bArray = {'%'};

        assertFalse("Invalid Base64 array was incorrectly validated as " + "an array of Base64 encoded data", Base64
                .isArrayByteBase64(bArray));

        try {
            Base64 b64 = new Base64();
            byte[] result = b64.decode(bArray);

            assertTrue("The result should be empty as the test encoded content did " + "not contain any valid base 64 characters",
                    result.length == 0);
        } catch (Exception e) {
            fail("Exception was thrown when trying to decode "
                + "invalid base64 encoded data - RFC 2045 requires that all "
                + "non base64 character be discarded, an exception should not"
                + " have been thrown");
        }
    }

// org.apache.commons.codec.binary.Base64Test::testObjectDecodeWithInvalidParameter
    public void testObjectDecodeWithInvalidParameter() throws Exception {
        Base64 b64 = new Base64();

        try {
            b64.decode(new Integer(5));
            fail("decode(Object) didn't throw an exception when passed an Integer object");
        } catch (DecoderException e) {
            
        }

    }

// org.apache.commons.codec.binary.Base64Test::testObjectDecodeWithValidParameter
    public void testObjectDecodeWithValidParameter() throws Exception {

        String original = "Hello World!";
        Object o = Base64.encodeBase64(original.getBytes("UTF-8"));

        Base64 b64 = new Base64();
        Object oDecoded = b64.decode(o);
        byte[] baDecoded = (byte[]) oDecoded;
        String dest = new String(baDecoded);

        assertTrue("dest string does not equal original", dest.equals(original));
    }

// org.apache.commons.codec.binary.Base64Test::testObjectEncodeWithInvalidParameter
    public void testObjectEncodeWithInvalidParameter() throws Exception {
        Base64 b64 = new Base64();
        try {
            b64.encode("Yadayadayada");
            fail("encode(Object) didn't throw an exception when passed a String object");
        } catch (EncoderException e) {
            
        }
    }

// org.apache.commons.codec.binary.Base64Test::testObjectEncodeWithValidParameter
    public void testObjectEncodeWithValidParameter() throws Exception {

        String original = "Hello World!";
        Object origObj = original.getBytes("UTF-8");

        Base64 b64 = new Base64();
        Object oEncoded = b64.encode(origObj);
        byte[] bArray = Base64.decodeBase64((byte[]) oEncoded);
        String dest = new String(bArray);

        assertTrue("dest string does not equal original", dest.equals(original));
    }

// org.apache.commons.codec.binary.Base64Test::testObjectEncode
    public void testObjectEncode() throws Exception {
        Base64 b64 = new Base64();
        assertEquals("SGVsbG8gV29ybGQ=", new String(b64.encode("Hello World".getBytes("UTF-8"))));
    }

// org.apache.commons.codec.binary.Base64Test::testPairs
    public void testPairs() {
        assertEquals("AAA=", new String(Base64.encodeBase64(new byte[]{0, 0})));
        for (int i = -128; i <= 127; i++) {
            byte test[] = {(byte) i, (byte) i};
            assertTrue(Arrays.equals(test, Base64.decodeBase64(Base64.encodeBase64(test))));
        }
    }

// org.apache.commons.codec.binary.Base64Test::testRfc2045Section2Dot1CrLfDefinition
    public void testRfc2045Section2Dot1CrLfDefinition() {
        assertTrue(Arrays.equals(new byte[]{13, 10}, Base64.CHUNK_SEPARATOR));
    }

// org.apache.commons.codec.binary.Base64Test::testRfc2045Section6Dot8ChunkSizeDefinition
    public void testRfc2045Section6Dot8ChunkSizeDefinition() {
        assertEquals(76, Base64.MIME_CHUNK_SIZE);
    }

// org.apache.commons.codec.binary.Base64Test::testRfc1421Section6Dot8ChunkSizeDefinition
    public void testRfc1421Section6Dot8ChunkSizeDefinition() {
        assertEquals(64, Base64.PEM_CHUNK_SIZE);
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
        String CRLF = StringUtils.newStringUsAscii(Base64.CHUNK_SEPARATOR);
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
        assertEquals("AA==", new String(Base64.encodeBase64(new byte[]{(byte) 0})));
        assertEquals("AQ==", new String(Base64.encodeBase64(new byte[]{(byte) 1})));
        assertEquals("Ag==", new String(Base64.encodeBase64(new byte[]{(byte) 2})));
        assertEquals("Aw==", new String(Base64.encodeBase64(new byte[]{(byte) 3})));
        assertEquals("BA==", new String(Base64.encodeBase64(new byte[]{(byte) 4})));
        assertEquals("BQ==", new String(Base64.encodeBase64(new byte[]{(byte) 5})));
        assertEquals("Bg==", new String(Base64.encodeBase64(new byte[]{(byte) 6})));
        assertEquals("Bw==", new String(Base64.encodeBase64(new byte[]{(byte) 7})));
        assertEquals("CA==", new String(Base64.encodeBase64(new byte[]{(byte) 8})));
        assertEquals("CQ==", new String(Base64.encodeBase64(new byte[]{(byte) 9})));
        assertEquals("Cg==", new String(Base64.encodeBase64(new byte[]{(byte) 10})));
        assertEquals("Cw==", new String(Base64.encodeBase64(new byte[]{(byte) 11})));
        assertEquals("DA==", new String(Base64.encodeBase64(new byte[]{(byte) 12})));
        assertEquals("DQ==", new String(Base64.encodeBase64(new byte[]{(byte) 13})));
        assertEquals("Dg==", new String(Base64.encodeBase64(new byte[]{(byte) 14})));
        assertEquals("Dw==", new String(Base64.encodeBase64(new byte[]{(byte) 15})));
        assertEquals("EA==", new String(Base64.encodeBase64(new byte[]{(byte) 16})));
        assertEquals("EQ==", new String(Base64.encodeBase64(new byte[]{(byte) 17})));
        assertEquals("Eg==", new String(Base64.encodeBase64(new byte[]{(byte) 18})));
        assertEquals("Ew==", new String(Base64.encodeBase64(new byte[]{(byte) 19})));
        assertEquals("FA==", new String(Base64.encodeBase64(new byte[]{(byte) 20})));
        assertEquals("FQ==", new String(Base64.encodeBase64(new byte[]{(byte) 21})));
        assertEquals("Fg==", new String(Base64.encodeBase64(new byte[]{(byte) 22})));
        assertEquals("Fw==", new String(Base64.encodeBase64(new byte[]{(byte) 23})));
        assertEquals("GA==", new String(Base64.encodeBase64(new byte[]{(byte) 24})));
        assertEquals("GQ==", new String(Base64.encodeBase64(new byte[]{(byte) 25})));
        assertEquals("Gg==", new String(Base64.encodeBase64(new byte[]{(byte) 26})));
        assertEquals("Gw==", new String(Base64.encodeBase64(new byte[]{(byte) 27})));
        assertEquals("HA==", new String(Base64.encodeBase64(new byte[]{(byte) 28})));
        assertEquals("HQ==", new String(Base64.encodeBase64(new byte[]{(byte) 29})));
        assertEquals("Hg==", new String(Base64.encodeBase64(new byte[]{(byte) 30})));
        assertEquals("Hw==", new String(Base64.encodeBase64(new byte[]{(byte) 31})));
        assertEquals("IA==", new String(Base64.encodeBase64(new byte[]{(byte) 32})));
        assertEquals("IQ==", new String(Base64.encodeBase64(new byte[]{(byte) 33})));
        assertEquals("Ig==", new String(Base64.encodeBase64(new byte[]{(byte) 34})));
        assertEquals("Iw==", new String(Base64.encodeBase64(new byte[]{(byte) 35})));
        assertEquals("JA==", new String(Base64.encodeBase64(new byte[]{(byte) 36})));
        assertEquals("JQ==", new String(Base64.encodeBase64(new byte[]{(byte) 37})));
        assertEquals("Jg==", new String(Base64.encodeBase64(new byte[]{(byte) 38})));
        assertEquals("Jw==", new String(Base64.encodeBase64(new byte[]{(byte) 39})));
        assertEquals("KA==", new String(Base64.encodeBase64(new byte[]{(byte) 40})));
        assertEquals("KQ==", new String(Base64.encodeBase64(new byte[]{(byte) 41})));
        assertEquals("Kg==", new String(Base64.encodeBase64(new byte[]{(byte) 42})));
        assertEquals("Kw==", new String(Base64.encodeBase64(new byte[]{(byte) 43})));
        assertEquals("LA==", new String(Base64.encodeBase64(new byte[]{(byte) 44})));
        assertEquals("LQ==", new String(Base64.encodeBase64(new byte[]{(byte) 45})));
        assertEquals("Lg==", new String(Base64.encodeBase64(new byte[]{(byte) 46})));
        assertEquals("Lw==", new String(Base64.encodeBase64(new byte[]{(byte) 47})));
        assertEquals("MA==", new String(Base64.encodeBase64(new byte[]{(byte) 48})));
        assertEquals("MQ==", new String(Base64.encodeBase64(new byte[]{(byte) 49})));
        assertEquals("Mg==", new String(Base64.encodeBase64(new byte[]{(byte) 50})));
        assertEquals("Mw==", new String(Base64.encodeBase64(new byte[]{(byte) 51})));
        assertEquals("NA==", new String(Base64.encodeBase64(new byte[]{(byte) 52})));
        assertEquals("NQ==", new String(Base64.encodeBase64(new byte[]{(byte) 53})));
        assertEquals("Ng==", new String(Base64.encodeBase64(new byte[]{(byte) 54})));
        assertEquals("Nw==", new String(Base64.encodeBase64(new byte[]{(byte) 55})));
        assertEquals("OA==", new String(Base64.encodeBase64(new byte[]{(byte) 56})));
        assertEquals("OQ==", new String(Base64.encodeBase64(new byte[]{(byte) 57})));
        assertEquals("Og==", new String(Base64.encodeBase64(new byte[]{(byte) 58})));
        assertEquals("Ow==", new String(Base64.encodeBase64(new byte[]{(byte) 59})));
        assertEquals("PA==", new String(Base64.encodeBase64(new byte[]{(byte) 60})));
        assertEquals("PQ==", new String(Base64.encodeBase64(new byte[]{(byte) 61})));
        assertEquals("Pg==", new String(Base64.encodeBase64(new byte[]{(byte) 62})));
        assertEquals("Pw==", new String(Base64.encodeBase64(new byte[]{(byte) 63})));
        assertEquals("QA==", new String(Base64.encodeBase64(new byte[]{(byte) 64})));
        assertEquals("QQ==", new String(Base64.encodeBase64(new byte[]{(byte) 65})));
        assertEquals("Qg==", new String(Base64.encodeBase64(new byte[]{(byte) 66})));
        assertEquals("Qw==", new String(Base64.encodeBase64(new byte[]{(byte) 67})));
        assertEquals("RA==", new String(Base64.encodeBase64(new byte[]{(byte) 68})));
        assertEquals("RQ==", new String(Base64.encodeBase64(new byte[]{(byte) 69})));
        assertEquals("Rg==", new String(Base64.encodeBase64(new byte[]{(byte) 70})));
        assertEquals("Rw==", new String(Base64.encodeBase64(new byte[]{(byte) 71})));
        assertEquals("SA==", new String(Base64.encodeBase64(new byte[]{(byte) 72})));
        assertEquals("SQ==", new String(Base64.encodeBase64(new byte[]{(byte) 73})));
        assertEquals("Sg==", new String(Base64.encodeBase64(new byte[]{(byte) 74})));
        assertEquals("Sw==", new String(Base64.encodeBase64(new byte[]{(byte) 75})));
        assertEquals("TA==", new String(Base64.encodeBase64(new byte[]{(byte) 76})));
        assertEquals("TQ==", new String(Base64.encodeBase64(new byte[]{(byte) 77})));
        assertEquals("Tg==", new String(Base64.encodeBase64(new byte[]{(byte) 78})));
        assertEquals("Tw==", new String(Base64.encodeBase64(new byte[]{(byte) 79})));
        assertEquals("UA==", new String(Base64.encodeBase64(new byte[]{(byte) 80})));
        assertEquals("UQ==", new String(Base64.encodeBase64(new byte[]{(byte) 81})));
        assertEquals("Ug==", new String(Base64.encodeBase64(new byte[]{(byte) 82})));
        assertEquals("Uw==", new String(Base64.encodeBase64(new byte[]{(byte) 83})));
        assertEquals("VA==", new String(Base64.encodeBase64(new byte[]{(byte) 84})));
        assertEquals("VQ==", new String(Base64.encodeBase64(new byte[]{(byte) 85})));
        assertEquals("Vg==", new String(Base64.encodeBase64(new byte[]{(byte) 86})));
        assertEquals("Vw==", new String(Base64.encodeBase64(new byte[]{(byte) 87})));
        assertEquals("WA==", new String(Base64.encodeBase64(new byte[]{(byte) 88})));
        assertEquals("WQ==", new String(Base64.encodeBase64(new byte[]{(byte) 89})));
        assertEquals("Wg==", new String(Base64.encodeBase64(new byte[]{(byte) 90})));
        assertEquals("Ww==", new String(Base64.encodeBase64(new byte[]{(byte) 91})));
        assertEquals("XA==", new String(Base64.encodeBase64(new byte[]{(byte) 92})));
        assertEquals("XQ==", new String(Base64.encodeBase64(new byte[]{(byte) 93})));
        assertEquals("Xg==", new String(Base64.encodeBase64(new byte[]{(byte) 94})));
        assertEquals("Xw==", new String(Base64.encodeBase64(new byte[]{(byte) 95})));
        assertEquals("YA==", new String(Base64.encodeBase64(new byte[]{(byte) 96})));
        assertEquals("YQ==", new String(Base64.encodeBase64(new byte[]{(byte) 97})));
        assertEquals("Yg==", new String(Base64.encodeBase64(new byte[]{(byte) 98})));
        assertEquals("Yw==", new String(Base64.encodeBase64(new byte[]{(byte) 99})));
        assertEquals("ZA==", new String(Base64.encodeBase64(new byte[]{(byte) 100})));
        assertEquals("ZQ==", new String(Base64.encodeBase64(new byte[]{(byte) 101})));
        assertEquals("Zg==", new String(Base64.encodeBase64(new byte[]{(byte) 102})));
        assertEquals("Zw==", new String(Base64.encodeBase64(new byte[]{(byte) 103})));
        assertEquals("aA==", new String(Base64.encodeBase64(new byte[]{(byte) 104})));
        for (int i = -128; i <= 127; i++) {
            byte test[] = {(byte) i};
            assertTrue(Arrays.equals(test, Base64.decodeBase64(Base64.encodeBase64(test))));
        }
    }

// org.apache.commons.codec.binary.Base64Test::testSingletonsChunked
    public void testSingletonsChunked() {
        assertEquals("AA==\r\n", new String(Base64.encodeBase64Chunked(new byte[]{(byte) 0})));
        assertEquals("AQ==\r\n", new String(Base64.encodeBase64Chunked(new byte[]{(byte) 1})));
        assertEquals("Ag==\r\n", new String(Base64.encodeBase64Chunked(new byte[]{(byte) 2})));
        assertEquals("Aw==\r\n", new String(Base64.encodeBase64Chunked(new byte[]{(byte) 3})));
        assertEquals("BA==\r\n", new String(Base64.encodeBase64Chunked(new byte[]{(byte) 4})));
        assertEquals("BQ==\r\n", new String(Base64.encodeBase64Chunked(new byte[]{(byte) 5})));
        assertEquals("Bg==\r\n", new String(Base64.encodeBase64Chunked(new byte[]{(byte) 6})));
        assertEquals("Bw==\r\n", new String(Base64.encodeBase64Chunked(new byte[]{(byte) 7})));
        assertEquals("CA==\r\n", new String(Base64.encodeBase64Chunked(new byte[]{(byte) 8})));
        assertEquals("CQ==\r\n", new String(Base64.encodeBase64Chunked(new byte[]{(byte) 9})));
        assertEquals("Cg==\r\n", new String(Base64.encodeBase64Chunked(new byte[]{(byte) 10})));
        assertEquals("Cw==\r\n", new String(Base64.encodeBase64Chunked(new byte[]{(byte) 11})));
        assertEquals("DA==\r\n", new String(Base64.encodeBase64Chunked(new byte[]{(byte) 12})));
        assertEquals("DQ==\r\n", new String(Base64.encodeBase64Chunked(new byte[]{(byte) 13})));
        assertEquals("Dg==\r\n", new String(Base64.encodeBase64Chunked(new byte[]{(byte) 14})));
        assertEquals("Dw==\r\n", new String(Base64.encodeBase64Chunked(new byte[]{(byte) 15})));
        assertEquals("EA==\r\n", new String(Base64.encodeBase64Chunked(new byte[]{(byte) 16})));
        assertEquals("EQ==\r\n", new String(Base64.encodeBase64Chunked(new byte[]{(byte) 17})));
        assertEquals("Eg==\r\n", new String(Base64.encodeBase64Chunked(new byte[]{(byte) 18})));
        assertEquals("Ew==\r\n", new String(Base64.encodeBase64Chunked(new byte[]{(byte) 19})));
        assertEquals("FA==\r\n", new String(Base64.encodeBase64Chunked(new byte[]{(byte) 20})));
        assertEquals("FQ==\r\n", new String(Base64.encodeBase64Chunked(new byte[]{(byte) 21})));
        assertEquals("Fg==\r\n", new String(Base64.encodeBase64Chunked(new byte[]{(byte) 22})));
        assertEquals("Fw==\r\n", new String(Base64.encodeBase64Chunked(new byte[]{(byte) 23})));
        assertEquals("GA==\r\n", new String(Base64.encodeBase64Chunked(new byte[]{(byte) 24})));
        assertEquals("GQ==\r\n", new String(Base64.encodeBase64Chunked(new byte[]{(byte) 25})));
        assertEquals("Gg==\r\n", new String(Base64.encodeBase64Chunked(new byte[]{(byte) 26})));
        assertEquals("Gw==\r\n", new String(Base64.encodeBase64Chunked(new byte[]{(byte) 27})));
        assertEquals("HA==\r\n", new String(Base64.encodeBase64Chunked(new byte[]{(byte) 28})));
        assertEquals("HQ==\r\n", new String(Base64.encodeBase64Chunked(new byte[]{(byte) 29})));
        assertEquals("Hg==\r\n", new String(Base64.encodeBase64Chunked(new byte[]{(byte) 30})));
        assertEquals("Hw==\r\n", new String(Base64.encodeBase64Chunked(new byte[]{(byte) 31})));
        assertEquals("IA==\r\n", new String(Base64.encodeBase64Chunked(new byte[]{(byte) 32})));
        assertEquals("IQ==\r\n", new String(Base64.encodeBase64Chunked(new byte[]{(byte) 33})));
        assertEquals("Ig==\r\n", new String(Base64.encodeBase64Chunked(new byte[]{(byte) 34})));
        assertEquals("Iw==\r\n", new String(Base64.encodeBase64Chunked(new byte[]{(byte) 35})));
        assertEquals("JA==\r\n", new String(Base64.encodeBase64Chunked(new byte[]{(byte) 36})));
        assertEquals("JQ==\r\n", new String(Base64.encodeBase64Chunked(new byte[]{(byte) 37})));
        assertEquals("Jg==\r\n", new String(Base64.encodeBase64Chunked(new byte[]{(byte) 38})));
        assertEquals("Jw==\r\n", new String(Base64.encodeBase64Chunked(new byte[]{(byte) 39})));
        assertEquals("KA==\r\n", new String(Base64.encodeBase64Chunked(new byte[]{(byte) 40})));
        assertEquals("KQ==\r\n", new String(Base64.encodeBase64Chunked(new byte[]{(byte) 41})));
        assertEquals("Kg==\r\n", new String(Base64.encodeBase64Chunked(new byte[]{(byte) 42})));
        assertEquals("Kw==\r\n", new String(Base64.encodeBase64Chunked(new byte[]{(byte) 43})));
        assertEquals("LA==\r\n", new String(Base64.encodeBase64Chunked(new byte[]{(byte) 44})));
        assertEquals("LQ==\r\n", new String(Base64.encodeBase64Chunked(new byte[]{(byte) 45})));
        assertEquals("Lg==\r\n", new String(Base64.encodeBase64Chunked(new byte[]{(byte) 46})));
        assertEquals("Lw==\r\n", new String(Base64.encodeBase64Chunked(new byte[]{(byte) 47})));
        assertEquals("MA==\r\n", new String(Base64.encodeBase64Chunked(new byte[]{(byte) 48})));
        assertEquals("MQ==\r\n", new String(Base64.encodeBase64Chunked(new byte[]{(byte) 49})));
        assertEquals("Mg==\r\n", new String(Base64.encodeBase64Chunked(new byte[]{(byte) 50})));
        assertEquals("Mw==\r\n", new String(Base64.encodeBase64Chunked(new byte[]{(byte) 51})));
        assertEquals("NA==\r\n", new String(Base64.encodeBase64Chunked(new byte[]{(byte) 52})));
        assertEquals("NQ==\r\n", new String(Base64.encodeBase64Chunked(new byte[]{(byte) 53})));
        assertEquals("Ng==\r\n", new String(Base64.encodeBase64Chunked(new byte[]{(byte) 54})));
        assertEquals("Nw==\r\n", new String(Base64.encodeBase64Chunked(new byte[]{(byte) 55})));
        assertEquals("OA==\r\n", new String(Base64.encodeBase64Chunked(new byte[]{(byte) 56})));
        assertEquals("OQ==\r\n", new String(Base64.encodeBase64Chunked(new byte[]{(byte) 57})));
        assertEquals("Og==\r\n", new String(Base64.encodeBase64Chunked(new byte[]{(byte) 58})));
        assertEquals("Ow==\r\n", new String(Base64.encodeBase64Chunked(new byte[]{(byte) 59})));
        assertEquals("PA==\r\n", new String(Base64.encodeBase64Chunked(new byte[]{(byte) 60})));
        assertEquals("PQ==\r\n", new String(Base64.encodeBase64Chunked(new byte[]{(byte) 61})));
        assertEquals("Pg==\r\n", new String(Base64.encodeBase64Chunked(new byte[]{(byte) 62})));
        assertEquals("Pw==\r\n", new String(Base64.encodeBase64Chunked(new byte[]{(byte) 63})));
        assertEquals("QA==\r\n", new String(Base64.encodeBase64Chunked(new byte[]{(byte) 64})));
        assertEquals("QQ==\r\n", new String(Base64.encodeBase64Chunked(new byte[]{(byte) 65})));
        assertEquals("Qg==\r\n", new String(Base64.encodeBase64Chunked(new byte[]{(byte) 66})));
        assertEquals("Qw==\r\n", new String(Base64.encodeBase64Chunked(new byte[]{(byte) 67})));
        assertEquals("RA==\r\n", new String(Base64.encodeBase64Chunked(new byte[]{(byte) 68})));
        assertEquals("RQ==\r\n", new String(Base64.encodeBase64Chunked(new byte[]{(byte) 69})));
        assertEquals("Rg==\r\n", new String(Base64.encodeBase64Chunked(new byte[]{(byte) 70})));
        assertEquals("Rw==\r\n", new String(Base64.encodeBase64Chunked(new byte[]{(byte) 71})));
        assertEquals("SA==\r\n", new String(Base64.encodeBase64Chunked(new byte[]{(byte) 72})));
        assertEquals("SQ==\r\n", new String(Base64.encodeBase64Chunked(new byte[]{(byte) 73})));
        assertEquals("Sg==\r\n", new String(Base64.encodeBase64Chunked(new byte[]{(byte) 74})));
        assertEquals("Sw==\r\n", new String(Base64.encodeBase64Chunked(new byte[]{(byte) 75})));
        assertEquals("TA==\r\n", new String(Base64.encodeBase64Chunked(new byte[]{(byte) 76})));
        assertEquals("TQ==\r\n", new String(Base64.encodeBase64Chunked(new byte[]{(byte) 77})));
        assertEquals("Tg==\r\n", new String(Base64.encodeBase64Chunked(new byte[]{(byte) 78})));
        assertEquals("Tw==\r\n", new String(Base64.encodeBase64Chunked(new byte[]{(byte) 79})));
        assertEquals("UA==\r\n", new String(Base64.encodeBase64Chunked(new byte[]{(byte) 80})));
        assertEquals("UQ==\r\n", new String(Base64.encodeBase64Chunked(new byte[]{(byte) 81})));
        assertEquals("Ug==\r\n", new String(Base64.encodeBase64Chunked(new byte[]{(byte) 82})));
        assertEquals("Uw==\r\n", new String(Base64.encodeBase64Chunked(new byte[]{(byte) 83})));
        assertEquals("VA==\r\n", new String(Base64.encodeBase64Chunked(new byte[]{(byte) 84})));
        assertEquals("VQ==\r\n", new String(Base64.encodeBase64Chunked(new byte[]{(byte) 85})));
        assertEquals("Vg==\r\n", new String(Base64.encodeBase64Chunked(new byte[]{(byte) 86})));
        assertEquals("Vw==\r\n", new String(Base64.encodeBase64Chunked(new byte[]{(byte) 87})));
        assertEquals("WA==\r\n", new String(Base64.encodeBase64Chunked(new byte[]{(byte) 88})));
        assertEquals("WQ==\r\n", new String(Base64.encodeBase64Chunked(new byte[]{(byte) 89})));
        assertEquals("Wg==\r\n", new String(Base64.encodeBase64Chunked(new byte[]{(byte) 90})));
        assertEquals("Ww==\r\n", new String(Base64.encodeBase64Chunked(new byte[]{(byte) 91})));
        assertEquals("XA==\r\n", new String(Base64.encodeBase64Chunked(new byte[]{(byte) 92})));
        assertEquals("XQ==\r\n", new String(Base64.encodeBase64Chunked(new byte[]{(byte) 93})));
        assertEquals("Xg==\r\n", new String(Base64.encodeBase64Chunked(new byte[]{(byte) 94})));
        assertEquals("Xw==\r\n", new String(Base64.encodeBase64Chunked(new byte[]{(byte) 95})));
        assertEquals("YA==\r\n", new String(Base64.encodeBase64Chunked(new byte[]{(byte) 96})));
        assertEquals("YQ==\r\n", new String(Base64.encodeBase64Chunked(new byte[]{(byte) 97})));
        assertEquals("Yg==\r\n", new String(Base64.encodeBase64Chunked(new byte[]{(byte) 98})));
        assertEquals("Yw==\r\n", new String(Base64.encodeBase64Chunked(new byte[]{(byte) 99})));
        assertEquals("ZA==\r\n", new String(Base64.encodeBase64Chunked(new byte[]{(byte) 100})));
        assertEquals("ZQ==\r\n", new String(Base64.encodeBase64Chunked(new byte[]{(byte) 101})));
        assertEquals("Zg==\r\n", new String(Base64.encodeBase64Chunked(new byte[]{(byte) 102})));
        assertEquals("Zw==\r\n", new String(Base64.encodeBase64Chunked(new byte[]{(byte) 103})));
        assertEquals("aA==\r\n", new String(Base64.encodeBase64Chunked(new byte[]{(byte) 104})));
    }

// org.apache.commons.codec.binary.Base64Test::testTriplets
    public void testTriplets() {
        assertEquals("AAAA", new String(Base64.encodeBase64(new byte[]{(byte) 0, (byte) 0, (byte) 0})));
        assertEquals("AAAB", new String(Base64.encodeBase64(new byte[]{(byte) 0, (byte) 0, (byte) 1})));
        assertEquals("AAAC", new String(Base64.encodeBase64(new byte[]{(byte) 0, (byte) 0, (byte) 2})));
        assertEquals("AAAD", new String(Base64.encodeBase64(new byte[]{(byte) 0, (byte) 0, (byte) 3})));
        assertEquals("AAAE", new String(Base64.encodeBase64(new byte[]{(byte) 0, (byte) 0, (byte) 4})));
        assertEquals("AAAF", new String(Base64.encodeBase64(new byte[]{(byte) 0, (byte) 0, (byte) 5})));
        assertEquals("AAAG", new String(Base64.encodeBase64(new byte[]{(byte) 0, (byte) 0, (byte) 6})));
        assertEquals("AAAH", new String(Base64.encodeBase64(new byte[]{(byte) 0, (byte) 0, (byte) 7})));
        assertEquals("AAAI", new String(Base64.encodeBase64(new byte[]{(byte) 0, (byte) 0, (byte) 8})));
        assertEquals("AAAJ", new String(Base64.encodeBase64(new byte[]{(byte) 0, (byte) 0, (byte) 9})));
        assertEquals("AAAK", new String(Base64.encodeBase64(new byte[]{(byte) 0, (byte) 0, (byte) 10})));
        assertEquals("AAAL", new String(Base64.encodeBase64(new byte[]{(byte) 0, (byte) 0, (byte) 11})));
        assertEquals("AAAM", new String(Base64.encodeBase64(new byte[]{(byte) 0, (byte) 0, (byte) 12})));
        assertEquals("AAAN", new String(Base64.encodeBase64(new byte[]{(byte) 0, (byte) 0, (byte) 13})));
        assertEquals("AAAO", new String(Base64.encodeBase64(new byte[]{(byte) 0, (byte) 0, (byte) 14})));
        assertEquals("AAAP", new String(Base64.encodeBase64(new byte[]{(byte) 0, (byte) 0, (byte) 15})));
        assertEquals("AAAQ", new String(Base64.encodeBase64(new byte[]{(byte) 0, (byte) 0, (byte) 16})));
        assertEquals("AAAR", new String(Base64.encodeBase64(new byte[]{(byte) 0, (byte) 0, (byte) 17})));
        assertEquals("AAAS", new String(Base64.encodeBase64(new byte[]{(byte) 0, (byte) 0, (byte) 18})));
        assertEquals("AAAT", new String(Base64.encodeBase64(new byte[]{(byte) 0, (byte) 0, (byte) 19})));
        assertEquals("AAAU", new String(Base64.encodeBase64(new byte[]{(byte) 0, (byte) 0, (byte) 20})));
        assertEquals("AAAV", new String(Base64.encodeBase64(new byte[]{(byte) 0, (byte) 0, (byte) 21})));
        assertEquals("AAAW", new String(Base64.encodeBase64(new byte[]{(byte) 0, (byte) 0, (byte) 22})));
        assertEquals("AAAX", new String(Base64.encodeBase64(new byte[]{(byte) 0, (byte) 0, (byte) 23})));
        assertEquals("AAAY", new String(Base64.encodeBase64(new byte[]{(byte) 0, (byte) 0, (byte) 24})));
        assertEquals("AAAZ", new String(Base64.encodeBase64(new byte[]{(byte) 0, (byte) 0, (byte) 25})));
        assertEquals("AAAa", new String(Base64.encodeBase64(new byte[]{(byte) 0, (byte) 0, (byte) 26})));
        assertEquals("AAAb", new String(Base64.encodeBase64(new byte[]{(byte) 0, (byte) 0, (byte) 27})));
        assertEquals("AAAc", new String(Base64.encodeBase64(new byte[]{(byte) 0, (byte) 0, (byte) 28})));
        assertEquals("AAAd", new String(Base64.encodeBase64(new byte[]{(byte) 0, (byte) 0, (byte) 29})));
        assertEquals("AAAe", new String(Base64.encodeBase64(new byte[]{(byte) 0, (byte) 0, (byte) 30})));
        assertEquals("AAAf", new String(Base64.encodeBase64(new byte[]{(byte) 0, (byte) 0, (byte) 31})));
        assertEquals("AAAg", new String(Base64.encodeBase64(new byte[]{(byte) 0, (byte) 0, (byte) 32})));
        assertEquals("AAAh", new String(Base64.encodeBase64(new byte[]{(byte) 0, (byte) 0, (byte) 33})));
        assertEquals("AAAi", new String(Base64.encodeBase64(new byte[]{(byte) 0, (byte) 0, (byte) 34})));
        assertEquals("AAAj", new String(Base64.encodeBase64(new byte[]{(byte) 0, (byte) 0, (byte) 35})));
        assertEquals("AAAk", new String(Base64.encodeBase64(new byte[]{(byte) 0, (byte) 0, (byte) 36})));
        assertEquals("AAAl", new String(Base64.encodeBase64(new byte[]{(byte) 0, (byte) 0, (byte) 37})));
        assertEquals("AAAm", new String(Base64.encodeBase64(new byte[]{(byte) 0, (byte) 0, (byte) 38})));
        assertEquals("AAAn", new String(Base64.encodeBase64(new byte[]{(byte) 0, (byte) 0, (byte) 39})));
        assertEquals("AAAo", new String(Base64.encodeBase64(new byte[]{(byte) 0, (byte) 0, (byte) 40})));
        assertEquals("AAAp", new String(Base64.encodeBase64(new byte[]{(byte) 0, (byte) 0, (byte) 41})));
        assertEquals("AAAq", new String(Base64.encodeBase64(new byte[]{(byte) 0, (byte) 0, (byte) 42})));
        assertEquals("AAAr", new String(Base64.encodeBase64(new byte[]{(byte) 0, (byte) 0, (byte) 43})));
        assertEquals("AAAs", new String(Base64.encodeBase64(new byte[]{(byte) 0, (byte) 0, (byte) 44})));
        assertEquals("AAAt", new String(Base64.encodeBase64(new byte[]{(byte) 0, (byte) 0, (byte) 45})));
        assertEquals("AAAu", new String(Base64.encodeBase64(new byte[]{(byte) 0, (byte) 0, (byte) 46})));
        assertEquals("AAAv", new String(Base64.encodeBase64(new byte[]{(byte) 0, (byte) 0, (byte) 47})));
        assertEquals("AAAw", new String(Base64.encodeBase64(new byte[]{(byte) 0, (byte) 0, (byte) 48})));
        assertEquals("AAAx", new String(Base64.encodeBase64(new byte[]{(byte) 0, (byte) 0, (byte) 49})));
        assertEquals("AAAy", new String(Base64.encodeBase64(new byte[]{(byte) 0, (byte) 0, (byte) 50})));
        assertEquals("AAAz", new String(Base64.encodeBase64(new byte[]{(byte) 0, (byte) 0, (byte) 51})));
        assertEquals("AAA0", new String(Base64.encodeBase64(new byte[]{(byte) 0, (byte) 0, (byte) 52})));
        assertEquals("AAA1", new String(Base64.encodeBase64(new byte[]{(byte) 0, (byte) 0, (byte) 53})));
        assertEquals("AAA2", new String(Base64.encodeBase64(new byte[]{(byte) 0, (byte) 0, (byte) 54})));
        assertEquals("AAA3", new String(Base64.encodeBase64(new byte[]{(byte) 0, (byte) 0, (byte) 55})));
        assertEquals("AAA4", new String(Base64.encodeBase64(new byte[]{(byte) 0, (byte) 0, (byte) 56})));
        assertEquals("AAA5", new String(Base64.encodeBase64(new byte[]{(byte) 0, (byte) 0, (byte) 57})));
        assertEquals("AAA6", new String(Base64.encodeBase64(new byte[]{(byte) 0, (byte) 0, (byte) 58})));
        assertEquals("AAA7", new String(Base64.encodeBase64(new byte[]{(byte) 0, (byte) 0, (byte) 59})));
        assertEquals("AAA8", new String(Base64.encodeBase64(new byte[]{(byte) 0, (byte) 0, (byte) 60})));
        assertEquals("AAA9", new String(Base64.encodeBase64(new byte[]{(byte) 0, (byte) 0, (byte) 61})));
        assertEquals("AAA+", new String(Base64.encodeBase64(new byte[]{(byte) 0, (byte) 0, (byte) 62})));
        assertEquals("AAA/", new String(Base64.encodeBase64(new byte[]{(byte) 0, (byte) 0, (byte) 63})));
    }

// org.apache.commons.codec.binary.Base64Test::testTripletsChunked
    public void testTripletsChunked() {
        assertEquals("AAAA\r\n", new String(Base64.encodeBase64Chunked(new byte[]{(byte) 0, (byte) 0, (byte) 0})));
        assertEquals("AAAB\r\n", new String(Base64.encodeBase64Chunked(new byte[]{(byte) 0, (byte) 0, (byte) 1})));
        assertEquals("AAAC\r\n", new String(Base64.encodeBase64Chunked(new byte[]{(byte) 0, (byte) 0, (byte) 2})));
        assertEquals("AAAD\r\n", new String(Base64.encodeBase64Chunked(new byte[]{(byte) 0, (byte) 0, (byte) 3})));
        assertEquals("AAAE\r\n", new String(Base64.encodeBase64Chunked(new byte[]{(byte) 0, (byte) 0, (byte) 4})));
        assertEquals("AAAF\r\n", new String(Base64.encodeBase64Chunked(new byte[]{(byte) 0, (byte) 0, (byte) 5})));
        assertEquals("AAAG\r\n", new String(Base64.encodeBase64Chunked(new byte[]{(byte) 0, (byte) 0, (byte) 6})));
        assertEquals("AAAH\r\n", new String(Base64.encodeBase64Chunked(new byte[]{(byte) 0, (byte) 0, (byte) 7})));
        assertEquals("AAAI\r\n", new String(Base64.encodeBase64Chunked(new byte[]{(byte) 0, (byte) 0, (byte) 8})));
        assertEquals("AAAJ\r\n", new String(Base64.encodeBase64Chunked(new byte[]{(byte) 0, (byte) 0, (byte) 9})));
        assertEquals("AAAK\r\n", new String(Base64.encodeBase64Chunked(new byte[]{(byte) 0, (byte) 0, (byte) 10})));
        assertEquals("AAAL\r\n", new String(Base64.encodeBase64Chunked(new byte[]{(byte) 0, (byte) 0, (byte) 11})));
        assertEquals("AAAM\r\n", new String(Base64.encodeBase64Chunked(new byte[]{(byte) 0, (byte) 0, (byte) 12})));
        assertEquals("AAAN\r\n", new String(Base64.encodeBase64Chunked(new byte[]{(byte) 0, (byte) 0, (byte) 13})));
        assertEquals("AAAO\r\n", new String(Base64.encodeBase64Chunked(new byte[]{(byte) 0, (byte) 0, (byte) 14})));
        assertEquals("AAAP\r\n", new String(Base64.encodeBase64Chunked(new byte[]{(byte) 0, (byte) 0, (byte) 15})));
        assertEquals("AAAQ\r\n", new String(Base64.encodeBase64Chunked(new byte[]{(byte) 0, (byte) 0, (byte) 16})));
        assertEquals("AAAR\r\n", new String(Base64.encodeBase64Chunked(new byte[]{(byte) 0, (byte) 0, (byte) 17})));
        assertEquals("AAAS\r\n", new String(Base64.encodeBase64Chunked(new byte[]{(byte) 0, (byte) 0, (byte) 18})));
        assertEquals("AAAT\r\n", new String(Base64.encodeBase64Chunked(new byte[]{(byte) 0, (byte) 0, (byte) 19})));
        assertEquals("AAAU\r\n", new String(Base64.encodeBase64Chunked(new byte[]{(byte) 0, (byte) 0, (byte) 20})));
        assertEquals("AAAV\r\n", new String(Base64.encodeBase64Chunked(new byte[]{(byte) 0, (byte) 0, (byte) 21})));
        assertEquals("AAAW\r\n", new String(Base64.encodeBase64Chunked(new byte[]{(byte) 0, (byte) 0, (byte) 22})));
        assertEquals("AAAX\r\n", new String(Base64.encodeBase64Chunked(new byte[]{(byte) 0, (byte) 0, (byte) 23})));
        assertEquals("AAAY\r\n", new String(Base64.encodeBase64Chunked(new byte[]{(byte) 0, (byte) 0, (byte) 24})));
        assertEquals("AAAZ\r\n", new String(Base64.encodeBase64Chunked(new byte[]{(byte) 0, (byte) 0, (byte) 25})));
        assertEquals("AAAa\r\n", new String(Base64.encodeBase64Chunked(new byte[]{(byte) 0, (byte) 0, (byte) 26})));
        assertEquals("AAAb\r\n", new String(Base64.encodeBase64Chunked(new byte[]{(byte) 0, (byte) 0, (byte) 27})));
        assertEquals("AAAc\r\n", new String(Base64.encodeBase64Chunked(new byte[]{(byte) 0, (byte) 0, (byte) 28})));
        assertEquals("AAAd\r\n", new String(Base64.encodeBase64Chunked(new byte[]{(byte) 0, (byte) 0, (byte) 29})));
        assertEquals("AAAe\r\n", new String(Base64.encodeBase64Chunked(new byte[]{(byte) 0, (byte) 0, (byte) 30})));
        assertEquals("AAAf\r\n", new String(Base64.encodeBase64Chunked(new byte[]{(byte) 0, (byte) 0, (byte) 31})));
        assertEquals("AAAg\r\n", new String(Base64.encodeBase64Chunked(new byte[]{(byte) 0, (byte) 0, (byte) 32})));
        assertEquals("AAAh\r\n", new String(Base64.encodeBase64Chunked(new byte[]{(byte) 0, (byte) 0, (byte) 33})));
        assertEquals("AAAi\r\n", new String(Base64.encodeBase64Chunked(new byte[]{(byte) 0, (byte) 0, (byte) 34})));
        assertEquals("AAAj\r\n", new String(Base64.encodeBase64Chunked(new byte[]{(byte) 0, (byte) 0, (byte) 35})));
        assertEquals("AAAk\r\n", new String(Base64.encodeBase64Chunked(new byte[]{(byte) 0, (byte) 0, (byte) 36})));
        assertEquals("AAAl\r\n", new String(Base64.encodeBase64Chunked(new byte[]{(byte) 0, (byte) 0, (byte) 37})));
        assertEquals("AAAm\r\n", new String(Base64.encodeBase64Chunked(new byte[]{(byte) 0, (byte) 0, (byte) 38})));
        assertEquals("AAAn\r\n", new String(Base64.encodeBase64Chunked(new byte[]{(byte) 0, (byte) 0, (byte) 39})));
        assertEquals("AAAo\r\n", new String(Base64.encodeBase64Chunked(new byte[]{(byte) 0, (byte) 0, (byte) 40})));
        assertEquals("AAAp\r\n", new String(Base64.encodeBase64Chunked(new byte[]{(byte) 0, (byte) 0, (byte) 41})));
        assertEquals("AAAq\r\n", new String(Base64.encodeBase64Chunked(new byte[]{(byte) 0, (byte) 0, (byte) 42})));
        assertEquals("AAAr\r\n", new String(Base64.encodeBase64Chunked(new byte[]{(byte) 0, (byte) 0, (byte) 43})));
        assertEquals("AAAs\r\n", new String(Base64.encodeBase64Chunked(new byte[]{(byte) 0, (byte) 0, (byte) 44})));
        assertEquals("AAAt\r\n", new String(Base64.encodeBase64Chunked(new byte[]{(byte) 0, (byte) 0, (byte) 45})));
        assertEquals("AAAu\r\n", new String(Base64.encodeBase64Chunked(new byte[]{(byte) 0, (byte) 0, (byte) 46})));
        assertEquals("AAAv\r\n", new String(Base64.encodeBase64Chunked(new byte[]{(byte) 0, (byte) 0, (byte) 47})));
        assertEquals("AAAw\r\n", new String(Base64.encodeBase64Chunked(new byte[]{(byte) 0, (byte) 0, (byte) 48})));
        assertEquals("AAAx\r\n", new String(Base64.encodeBase64Chunked(new byte[]{(byte) 0, (byte) 0, (byte) 49})));
        assertEquals("AAAy\r\n", new String(Base64.encodeBase64Chunked(new byte[]{(byte) 0, (byte) 0, (byte) 50})));
        assertEquals("AAAz\r\n", new String(Base64.encodeBase64Chunked(new byte[]{(byte) 0, (byte) 0, (byte) 51})));
        assertEquals("AAA0\r\n", new String(Base64.encodeBase64Chunked(new byte[]{(byte) 0, (byte) 0, (byte) 52})));
        assertEquals("AAA1\r\n", new String(Base64.encodeBase64Chunked(new byte[]{(byte) 0, (byte) 0, (byte) 53})));
        assertEquals("AAA2\r\n", new String(Base64.encodeBase64Chunked(new byte[]{(byte) 0, (byte) 0, (byte) 54})));
        assertEquals("AAA3\r\n", new String(Base64.encodeBase64Chunked(new byte[]{(byte) 0, (byte) 0, (byte) 55})));
        assertEquals("AAA4\r\n", new String(Base64.encodeBase64Chunked(new byte[]{(byte) 0, (byte) 0, (byte) 56})));
        assertEquals("AAA5\r\n", new String(Base64.encodeBase64Chunked(new byte[]{(byte) 0, (byte) 0, (byte) 57})));
        assertEquals("AAA6\r\n", new String(Base64.encodeBase64Chunked(new byte[]{(byte) 0, (byte) 0, (byte) 58})));
        assertEquals("AAA7\r\n", new String(Base64.encodeBase64Chunked(new byte[]{(byte) 0, (byte) 0, (byte) 59})));
        assertEquals("AAA8\r\n", new String(Base64.encodeBase64Chunked(new byte[]{(byte) 0, (byte) 0, (byte) 60})));
        assertEquals("AAA9\r\n", new String(Base64.encodeBase64Chunked(new byte[]{(byte) 0, (byte) 0, (byte) 61})));
        assertEquals("AAA+\r\n", new String(Base64.encodeBase64Chunked(new byte[]{(byte) 0, (byte) 0, (byte) 62})));
        assertEquals("AAA/\r\n", new String(Base64.encodeBase64Chunked(new byte[]{(byte) 0, (byte) 0, (byte) 63})));
    }

// org.apache.commons.codec.binary.Base64Test::testUrlSafe
    public void testUrlSafe() {
        
        for (int i = 0; i <= 150; i++) {
            byte[][] randomData = Base64TestData.randomData(i, true);
            byte[] encoded = randomData[1];
            byte[] decoded = randomData[0];
            byte[] result = Base64.decodeBase64(encoded);
            assertTrue("url-safe i=" + i, Arrays.equals(decoded, result));
            assertFalse("url-safe i=" + i + " no '='", Base64TestData.bytesContain(encoded, (byte) '='));
            assertFalse("url-safe i=" + i + " no '\\'", Base64TestData.bytesContain(encoded, (byte) '\\'));
            assertFalse("url-safe i=" + i + " no '+'", Base64TestData.bytesContain(encoded, (byte) '+'));
        }

    }

// org.apache.commons.codec.binary.Base64Test::testUUID
    public void testUUID() throws DecoderException {
        
        
        byte[][] ids = new byte[4][];

        
        ids[0] = Hex.decodeHex("94ed8d0319e4493399560fb67404d370".toCharArray());

        
        ids[1] = Hex.decodeHex("2bf7cc2701fe4397b49ebeed5acc7090".toCharArray());

        
        ids[2] = Hex.decodeHex("64be154b6ffa40258d1a01288e7c31ca".toCharArray());

        
        
        ids[3] = Hex.decodeHex("ff7f8fc01cdb471a8c8b5a9306183fe8".toCharArray());

        byte[][] standard = new byte[4][];
        standard[0] = StringUtils.getBytesUtf8("lO2NAxnkSTOZVg+2dATTcA==");
        standard[1] = StringUtils.getBytesUtf8("K/fMJwH+Q5e0nr7tWsxwkA==");
        standard[2] = StringUtils.getBytesUtf8("ZL4VS2/6QCWNGgEojnwxyg==");
        standard[3] = StringUtils.getBytesUtf8("/3+PwBzbRxqMi1qTBhg/6A==");

        byte[][] urlSafe1 = new byte[4][];
        
        urlSafe1[0] = StringUtils.getBytesUtf8("lO2NAxnkSTOZVg-2dATTcA==");
        urlSafe1[1] = StringUtils.getBytesUtf8("K_fMJwH-Q5e0nr7tWsxwkA==");
        urlSafe1[2] = StringUtils.getBytesUtf8("ZL4VS2_6QCWNGgEojnwxyg==");
        urlSafe1[3] = StringUtils.getBytesUtf8("_3-PwBzbRxqMi1qTBhg_6A==");

        byte[][] urlSafe2 = new byte[4][];
        
        urlSafe2[0] = StringUtils.getBytesUtf8("lO2NAxnkSTOZVg-2dATTcA=");
        urlSafe2[1] = StringUtils.getBytesUtf8("K_fMJwH-Q5e0nr7tWsxwkA=");
        urlSafe2[2] = StringUtils.getBytesUtf8("ZL4VS2_6QCWNGgEojnwxyg=");
        urlSafe2[3] = StringUtils.getBytesUtf8("_3-PwBzbRxqMi1qTBhg_6A=");

        byte[][] urlSafe3 = new byte[4][];
        
        urlSafe3[0] = StringUtils.getBytesUtf8("lO2NAxnkSTOZVg-2dATTcA");
        urlSafe3[1] = StringUtils.getBytesUtf8("K_fMJwH-Q5e0nr7tWsxwkA");
        urlSafe3[2] = StringUtils.getBytesUtf8("ZL4VS2_6QCWNGgEojnwxyg");
        urlSafe3[3] = StringUtils.getBytesUtf8("_3-PwBzbRxqMi1qTBhg_6A");

        for (int i = 0; i < 4; i++) {
            byte[] encodedStandard = Base64.encodeBase64(ids[i]);
            byte[] encodedUrlSafe = Base64.encodeBase64URLSafe(ids[i]);
            byte[] decodedStandard = Base64.decodeBase64(standard[i]);
            byte[] decodedUrlSafe1 = Base64.decodeBase64(urlSafe1[i]);
            byte[] decodedUrlSafe2 = Base64.decodeBase64(urlSafe2[i]);
            byte[] decodedUrlSafe3 = Base64.decodeBase64(urlSafe3[i]);

            
            
            if (false) {
                System.out.println("reference: [" + Hex.encodeHexString(ids[i]) + "]");
                System.out.println("standard:  [" +
                        Hex.encodeHexString(decodedStandard) +
                    "] From: [" +
                    StringUtils.newStringUtf8(standard[i]) +
                    "]");
                System.out.println("safe1:     [" +
                        Hex.encodeHexString(decodedUrlSafe1) +
                    "] From: [" +
                    StringUtils.newStringUtf8(urlSafe1[i]) +
                    "]");
                System.out.println("safe2:     [" +
                        Hex.encodeHexString(decodedUrlSafe2) +
                    "] From: [" +
                    StringUtils.newStringUtf8(urlSafe2[i]) +
                    "]");
                System.out.println("safe3:     [" +
                        Hex.encodeHexString(decodedUrlSafe3) +
                    "] From: [" +
                    StringUtils.newStringUtf8(urlSafe3[i]) +
                    "]");
            }

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
        Base64 base64 = new Base64(0);
        byte[] b1 = StringUtils.getBytesUtf8("Hello World");
        byte[] b2 = new byte[0];
        byte[] b3 = null;
        byte[] b4 = Hex.decodeHex("2bf7cc2701fe4397b49ebeed5acc7090".toCharArray());  

        assertEquals("byteToString Hello World", "SGVsbG8gV29ybGQ=", base64.encodeToString(b1));
        assertEquals("byteToString static Hello World", "SGVsbG8gV29ybGQ=", Base64.encodeBase64String(b1));
        assertEquals("byteToString \"\"", "", base64.encodeToString(b2));
        assertEquals("byteToString static \"\"", "", Base64.encodeBase64String(b2));
        assertEquals("byteToString null", null, base64.encodeToString(b3));
        assertEquals("byteToString static null", null, Base64.encodeBase64String(b3));
        assertEquals("byteToString UUID", "K/fMJwH+Q5e0nr7tWsxwkA==", base64.encodeToString(b4));
        assertEquals("byteToString static UUID", "K/fMJwH+Q5e0nr7tWsxwkA==", Base64.encodeBase64String(b4));
        assertEquals("byteToString static-url-safe UUID", "K_fMJwH-Q5e0nr7tWsxwkA", Base64.encodeBase64URLSafeString(b4));
    }

// org.apache.commons.codec.binary.Base64Test::testStringToByteVariations
    public void testStringToByteVariations() throws DecoderException {
        Base64 base64 = new Base64();
        String s1 = "SGVsbG8gV29ybGQ=\r\n";
        String s2 = "";
        String s3 = null;
        String s4a = "K/fMJwH+Q5e0nr7tWsxwkA==\r\n";
        String s4b = "K_fMJwH-Q5e0nr7tWsxwkA";
        byte[] b4 = Hex.decodeHex("2bf7cc2701fe4397b49ebeed5acc7090".toCharArray());  

        assertEquals("StringToByte Hello World", "Hello World", StringUtils.newStringUtf8(base64.decode(s1)));
        assertEquals("StringToByte Hello World", "Hello World", StringUtils.newStringUtf8((byte[])base64.decode((Object)s1)));
        assertEquals("StringToByte static Hello World", "Hello World", StringUtils.newStringUtf8(Base64.decodeBase64(s1)));
        assertEquals("StringToByte \"\"", "", StringUtils.newStringUtf8(base64.decode(s2)));
        assertEquals("StringToByte static \"\"", "", StringUtils.newStringUtf8(Base64.decodeBase64(s2)));
        assertEquals("StringToByte null", null, StringUtils.newStringUtf8(base64.decode(s3)));
        assertEquals("StringToByte static null", null, StringUtils.newStringUtf8(Base64.decodeBase64(s3)));
        assertTrue("StringToByte UUID", Arrays.equals(b4, base64.decode(s4b)));
        assertTrue("StringToByte static UUID", Arrays.equals(b4, Base64.decodeBase64(s4a)));
        assertTrue("StringToByte static-url-safe UUID", Arrays.equals(b4, Base64.decodeBase64(s4b)));
    }

// org.apache.commons.codec.net.BCodecTest::testNullInput
    public void testNullInput() throws Exception {
        BCodec bcodec = new BCodec();
        assertNull(bcodec.doDecoding(null));
        assertNull(bcodec.doEncoding(null));
    }

// org.apache.commons.codec.net.BCodecTest::testUTF8RoundTrip
    public void testUTF8RoundTrip() throws Exception {

        String ru_msg = constructString(RUSSIAN_STUFF_UNICODE);
        String ch_msg = constructString(SWISS_GERMAN_STUFF_UNICODE);

        BCodec bcodec = new BCodec(CharEncoding.UTF_8);

        assertEquals("=?UTF-8?B?0JLRgdC10Lxf0L/RgNC40LLQtdGC?=", bcodec.encode(ru_msg));
        assertEquals("=?UTF-8?B?R3LDvGV6aV96w6Rtw6Q=?=", bcodec.encode(ch_msg));

        assertEquals(ru_msg, bcodec.decode(bcodec.encode(ru_msg)));
        assertEquals(ch_msg, bcodec.decode(bcodec.encode(ch_msg)));
    }

// org.apache.commons.codec.net.BCodecTest::testBasicEncodeDecode
    public void testBasicEncodeDecode() throws Exception {
        BCodec bcodec = new BCodec();
        String plain = "Hello there";
        String encoded = bcodec.encode(plain);
        assertEquals("Basic B encoding test", "=?UTF-8?B?SGVsbG8gdGhlcmU=?=", encoded);
        assertEquals("Basic B decoding test", plain, bcodec.decode(encoded));
    }

// org.apache.commons.codec.net.BCodecTest::testEncodeDecodeNull
    public void testEncodeDecodeNull() throws Exception {
        BCodec bcodec = new BCodec();
        assertNull("Null string B encoding test", bcodec.encode((String) null));
        assertNull("Null string B decoding test", bcodec.decode((String) null));
    }

// org.apache.commons.codec.net.BCodecTest::testEncodeStringWithNull
    public void testEncodeStringWithNull() throws Exception {
        BCodec bcodec = new BCodec();
        String test = null;
        String result = bcodec.encode(test, "charset");
        assertEquals("Result should be null", null, result);
    }

// org.apache.commons.codec.net.BCodecTest::testDecodeStringWithNull
    public void testDecodeStringWithNull() throws Exception {
        BCodec bcodec = new BCodec();
        String test = null;
        String result = bcodec.decode(test);
        assertEquals("Result should be null", null, result);
    }

// org.apache.commons.codec.net.BCodecTest::testEncodeObjects
    public void testEncodeObjects() throws Exception {
        BCodec bcodec = new BCodec();
        String plain = "what not";
        String encoded = (String) bcodec.encode((Object) plain);

        assertEquals("Basic B encoding test", "=?UTF-8?B?d2hhdCBub3Q=?=", encoded);

        Object result = bcodec.encode((Object) null);
        assertEquals("Encoding a null Object should return null", null, result);

        try {
            Object dObj = new Double(3.0);
            bcodec.encode(dObj);
            fail("Trying to url encode a Double object should cause an exception.");
        } catch (EncoderException ee) {
            
        }
    }

// org.apache.commons.codec.net.BCodecTest::testInvalidEncoding
    public void testInvalidEncoding() {
        BCodec bcodec = new BCodec("NONSENSE");
        try {
            bcodec.encode("Hello there!");
            fail("We set the encoding to a bogus NONSENSE value, this shouldn't have worked.");
        } catch (EncoderException ee) {
            
        }
        try {
            bcodec.decode("=?NONSENSE?B?Hello there!?=");
            fail("We set the encoding to a bogus NONSENSE value, this shouldn't have worked.");
        } catch (DecoderException ee) {
            
        }
    }

// org.apache.commons.codec.net.BCodecTest::testDecodeObjects
    public void testDecodeObjects() throws Exception {
        BCodec bcodec = new BCodec();
        String decoded = "=?UTF-8?B?d2hhdCBub3Q=?=";
        String plain = (String) bcodec.decode((Object) decoded);
        assertEquals("Basic B decoding test", "what not", plain);

        Object result = bcodec.decode((Object) null);
        assertEquals("Decoding a null Object should return null", null, result);

        try {
            Object dObj = new Double(3.0);
            bcodec.decode(dObj);
            fail("Trying to url encode a Double object should cause an exception.");
        } catch (DecoderException ee) {
            
        }
    }

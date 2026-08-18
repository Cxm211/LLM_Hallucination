// buggy code
    public static String newStringIso8859_1(final byte[] bytes) {
        return new String(bytes, Charsets.ISO_8859_1);
    }

// relevant test
// org.apache.commons.codec.net.URLCodecTest::testUnsafeEncodeDecode
    public void testUnsafeEncodeDecode() throws Exception {
        final URLCodec urlCodec = new URLCodec();
        final String plain = "~!@#$%^&()+{}\"\\;:`,/[]";
        final String encoded = urlCodec.encode(plain);
        assertEquals("Unsafe chars URL encoding test",
            "%7E%21%40%23%24%25%5E%26%28%29%2B%7B%7D%22%5C%3B%3A%60%2C%2F%5B%5D", encoded);
        assertEquals("Unsafe chars URL decoding test",
            plain, urlCodec.decode(encoded));
        this.validateState(urlCodec);
    }

// org.apache.commons.codec.net.URLCodecTest::testEncodeDecodeNull
    public void testEncodeDecodeNull() throws Exception {
        final URLCodec urlCodec = new URLCodec();
        assertNull("Null string URL encoding test",
            urlCodec.encode((String)null));
        assertNull("Null string URL decoding test",
            urlCodec.decode((String)null));
        this.validateState(urlCodec);
    }

// org.apache.commons.codec.net.URLCodecTest::testDecodeInvalid
    public void testDecodeInvalid() throws Exception {
        final URLCodec urlCodec = new URLCodec();
        try {
            urlCodec.decode("%");
            fail("DecoderException should have been thrown");
        } catch (final DecoderException e) {
            
        }
        try {
            urlCodec.decode("%A");
            fail("DecoderException should have been thrown");
        } catch (final DecoderException e) {
            
        }
        try {
            
            urlCodec.decode("%WW");
            fail("DecoderException should have been thrown");
        } catch (final DecoderException e) {
            
        }
        try {
            
            urlCodec.decode("%0W");
            fail("DecoderException should have been thrown");
        } catch (final DecoderException e) {
            
        }
        this.validateState(urlCodec);
    }

// org.apache.commons.codec.net.URLCodecTest::testDecodeInvalidContent
    public void testDecodeInvalidContent() throws UnsupportedEncodingException, DecoderException {
        final String ch_msg = constructString(SWISS_GERMAN_STUFF_UNICODE);
        final URLCodec urlCodec = new URLCodec();
        final byte[] input = ch_msg.getBytes("ISO-8859-1");
        final byte[] output = urlCodec.decode(input);
        assertEquals(input.length, output.length);
        for (int i = 0; i < input.length; i++) {
            assertEquals(input[i], output[i]);
        }
        this.validateState(urlCodec);
    }

// org.apache.commons.codec.net.URLCodecTest::testEncodeNull
    public void testEncodeNull() throws Exception {
        final URLCodec urlCodec = new URLCodec();
        final byte[] plain = null;
        final byte[] encoded = urlCodec.encode(plain);
        assertEquals("Encoding a null string should return null",
            null, encoded);
        this.validateState(urlCodec);
    }

// org.apache.commons.codec.net.URLCodecTest::testEncodeUrlWithNullBitSet
    public void testEncodeUrlWithNullBitSet() throws Exception {
        final URLCodec urlCodec = new URLCodec();
        final String plain = "Hello there!";
        final String encoded = new String( URLCodec.encodeUrl(null, plain.getBytes(Charsets.UTF_8)));
        assertEquals("Basic URL encoding test",
            "Hello+there%21", encoded);
        assertEquals("Basic URL decoding test",
            plain, urlCodec.decode(encoded));
        this.validateState(urlCodec);
    }

// org.apache.commons.codec.net.URLCodecTest::testDecodeWithNullArray
    public void testDecodeWithNullArray() throws Exception {
        final byte[] plain = null;
        final byte[] result = URLCodec.decodeUrl( plain );
        assertEquals("Result should be null", null, result);
    }

// org.apache.commons.codec.net.URLCodecTest::testEncodeStringWithNull
    public void testEncodeStringWithNull() throws Exception {
        final URLCodec urlCodec = new URLCodec();
        final String test = null;
        final String result = urlCodec.encode( test, "charset" );
        assertEquals("Result should be null", null, result);
    }

// org.apache.commons.codec.net.URLCodecTest::testDecodeStringWithNull
    public void testDecodeStringWithNull() throws Exception {
        final URLCodec urlCodec = new URLCodec();
        final String test = null;
        final String result = urlCodec.decode( test, "charset" );
        assertEquals("Result should be null", null, result);
    }

// org.apache.commons.codec.net.URLCodecTest::testEncodeObjects
    public void testEncodeObjects() throws Exception {
        final URLCodec urlCodec = new URLCodec();
        final String plain = "Hello there!";
        String encoded = (String) urlCodec.encode((Object) plain);
        assertEquals("Basic URL encoding test",
            "Hello+there%21", encoded);

        final byte[] plainBA = plain.getBytes(Charsets.UTF_8);
        final byte[] encodedBA = (byte[]) urlCodec.encode((Object) plainBA);
        encoded = new String(encodedBA);
        assertEquals("Basic URL encoding test",
            "Hello+there%21", encoded);

        final Object result = urlCodec.encode((Object) null);
        assertEquals( "Encoding a null Object should return null", null, result);

        try {
            final Object dObj = new Double(3.0);
            urlCodec.encode( dObj );
            fail( "Trying to url encode a Double object should cause an exception.");
        } catch (final EncoderException ee) {
            
        }
        this.validateState(urlCodec);
    }

// org.apache.commons.codec.net.URLCodecTest::testInvalidEncoding
    public void testInvalidEncoding() {
        final URLCodec urlCodec = new URLCodec("NONSENSE");
        final String plain = "Hello there!";
        try {
            urlCodec.encode(plain);
            fail("We set the encoding to a bogus NONSENSE vlaue, this shouldn't have worked.");
        } catch (final EncoderException ee) {
            
        }
        try {
            urlCodec.decode(plain);
            fail("We set the encoding to a bogus NONSENSE vlaue, this shouldn't have worked.");
        } catch (final DecoderException ee) {
            
        }
        this.validateState(urlCodec);
    }

// org.apache.commons.codec.net.URLCodecTest::testDecodeObjects
    public void testDecodeObjects() throws Exception {
        final URLCodec urlCodec = new URLCodec();
        final String plain = "Hello+there%21";
        String decoded = (String) urlCodec.decode((Object) plain);
        assertEquals("Basic URL decoding test",
            "Hello there!", decoded);

        final byte[] plainBA = plain.getBytes(Charsets.UTF_8);
        final byte[] decodedBA = (byte[]) urlCodec.decode((Object) plainBA);
        decoded = new String(decodedBA);
        assertEquals("Basic URL decoding test",
            "Hello there!", decoded);

        final Object result = urlCodec.decode((Object) null);
        assertEquals( "Decoding a null Object should return null", null, result);

        try {
            final Object dObj = new Double(3.0);
            urlCodec.decode( dObj );
            fail( "Trying to url encode a Double object should cause an exception.");
        } catch (final DecoderException ee) {
            
        }
        this.validateState(urlCodec);
    }

// org.apache.commons.codec.net.URLCodecTest::testDefaultEncoding
    public void testDefaultEncoding() throws Exception {
        final String plain = "Hello there!";
        final URLCodec urlCodec = new URLCodec("UnicodeBig");
        urlCodec.encode(plain); 
        final String encoded1 = urlCodec.encode(plain, "UnicodeBig");
        final String encoded2 = urlCodec.encode(plain);
        assertEquals(encoded1, encoded2);
        this.validateState(urlCodec);
    }

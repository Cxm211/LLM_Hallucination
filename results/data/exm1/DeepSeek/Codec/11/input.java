// buggy code
    private static final void encodeQuotedPrintable(int b, ByteArrayOutputStream buffer) {
        buffer.write(ESCAPE_CHAR);
        char hex1 = Character.toUpperCase(Character.forDigit((b >> 4) & 0xF, 16));
        char hex2 = Character.toUpperCase(Character.forDigit(b & 0xF, 16));
        buffer.write(hex1);
        buffer.write(hex2);
    }

    public static final byte[] encodeQuotedPrintable(BitSet printable, byte[] bytes) {
        if (bytes == null) {
            return null;
        }
        if (printable == null) {
            printable = PRINTABLE_CHARS;
        }
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        // encode up to buffer.length - 3, the last three octets will be treated
        // separately for simplification of note #3
                // up to this length it is safe to add any byte, encoded or not
        for (byte c : bytes) {
            int b = c;
            if (b < 0) {
                b = 256 + b;
            }
            if (printable.get(b)) {
                buffer.write(b);
            } else {
                // rule #3: whitespace at the end of a line *must* be encoded

                // rule #5: soft line break
                encodeQuotedPrintable(b, buffer);
            }
        }

        // rule #3: whitespace at the end of a line *must* be encoded
        // if we would do a soft break line after this octet, encode whitespace

        // note #3: '=' *must not* be the ultimate or penultimate character
        // simplification: if < 6 bytes left, do a soft line break as we may need
        //                 exactly 6 bytes space for the last 2 bytes
            // rule #3: trailing whitespace shall be encoded

        return buffer.toByteArray();
    }

    public static final byte[] decodeQuotedPrintable(byte[] bytes) throws DecoderException {
        if (bytes == null) {
            return null;
        }
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        for (int i = 0; i < bytes.length; i++) {
            final int b = bytes[i];
            if (b == ESCAPE_CHAR) {
                try {
                    // if the next octet is a CR we have found a soft line break
                    int u = Utils.digit16(bytes[++i]);
                    int l = Utils.digit16(bytes[++i]);
                    buffer.write((char) ((u << 4) + l));
                } catch (ArrayIndexOutOfBoundsException e) {
                    throw new DecoderException("Invalid quoted-printable encoding", e);
                }
            } else {
                // every other octet is appended except for CR & LF
                buffer.write(b);
            }
        }
        return buffer.toByteArray();
    }

// relevant test
// org.apache.commons.codec.net.QCodecTest::testNullInput
    public void testNullInput() throws Exception {
        QCodec qcodec = new QCodec();
        assertNull(qcodec.doDecoding(null));
        assertNull(qcodec.doEncoding(null));
    }

// org.apache.commons.codec.net.QCodecTest::testUTF8RoundTrip
    public void testUTF8RoundTrip() throws Exception {

        String ru_msg = constructString(RUSSIAN_STUFF_UNICODE); 
        String ch_msg = constructString(SWISS_GERMAN_STUFF_UNICODE); 
        
        QCodec qcodec = new QCodec(CharEncoding.UTF_8);
        
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
        QCodec qcodec = new QCodec();
        String plain = "= Hello there =\r\n";
        String encoded = qcodec.encode(plain);
        assertEquals("Basic Q encoding test", 
            "=?UTF-8?Q?=3D Hello there =3D=0D=0A?=", encoded);
        assertEquals("Basic Q decoding test", 
            plain, qcodec.decode(encoded));
    }

// org.apache.commons.codec.net.QCodecTest::testUnsafeEncodeDecode
    public void testUnsafeEncodeDecode() throws Exception {
        QCodec qcodec = new QCodec();
        String plain = "?_=\r\n";
        String encoded = qcodec.encode(plain);
        assertEquals("Unsafe chars Q encoding test", 
            "=?UTF-8?Q?=3F=5F=3D=0D=0A?=", encoded);
        assertEquals("Unsafe chars Q decoding test", 
            plain, qcodec.decode(encoded));
    }

// org.apache.commons.codec.net.QCodecTest::testEncodeDecodeNull
    public void testEncodeDecodeNull() throws Exception {
        QCodec qcodec = new QCodec();
        assertNull("Null string Q encoding test", 
            qcodec.encode((String)null));
        assertNull("Null string Q decoding test", 
            qcodec.decode((String)null));
    }

// org.apache.commons.codec.net.QCodecTest::testEncodeStringWithNull
    public void testEncodeStringWithNull() throws Exception {
        QCodec qcodec = new QCodec();
        String test = null;
        String result = qcodec.encode( test, "charset" );
        assertEquals("Result should be null", null, result);
    }

// org.apache.commons.codec.net.QCodecTest::testDecodeStringWithNull
    public void testDecodeStringWithNull() throws Exception {
        QCodec qcodec = new QCodec();
        String test = null;
        String result = qcodec.decode( test );
        assertEquals("Result should be null", null, result);
    }

// org.apache.commons.codec.net.QCodecTest::testEncodeObjects
    public void testEncodeObjects() throws Exception {
        QCodec qcodec = new QCodec();
        String plain = "1+1 = 2";
        String encoded = (String) qcodec.encode((Object) plain);
        assertEquals("Basic Q encoding test", 
            "=?UTF-8?Q?1+1 =3D 2?=", encoded);

        Object result = qcodec.encode((Object) null);
        assertEquals( "Encoding a null Object should return null", null, result);
        
        try {
            Object dObj = new Double(3.0);
            qcodec.encode( dObj );
            fail( "Trying to url encode a Double object should cause an exception.");
        } catch (EncoderException ee) {
            
        }
    }

// org.apache.commons.codec.net.QCodecTest::testInvalidEncoding
    public void testInvalidEncoding() {
        QCodec qcodec = new QCodec("NONSENSE");
            try {
               qcodec.encode("Hello there!");
                fail( "We set the encoding to a bogus NONSENSE vlaue, this shouldn't have worked.");
            } catch (EncoderException ee) {
                
            }
            try {
               qcodec.decode("=?NONSENSE?Q?Hello there!?=");
                fail( "We set the encoding to a bogus NONSENSE vlaue, this shouldn't have worked.");
            } catch (DecoderException ee) {
                
            }
    }

// org.apache.commons.codec.net.QCodecTest::testDecodeObjects
    public void testDecodeObjects() throws Exception {
        QCodec qcodec = new QCodec();
        String decoded = "=?UTF-8?Q?1+1 =3D 2?=";
        String plain = (String) qcodec.decode((Object) decoded);
        assertEquals("Basic Q decoding test", 
            "1+1 = 2", plain);

        Object result = qcodec.decode((Object) null);
        assertEquals( "Decoding a null Object should return null", null, result);
        
        try {
            Object dObj = new Double(3.0);
            qcodec.decode( dObj );
            fail( "Trying to url encode a Double object should cause an exception.");
        } catch (DecoderException ee) {
            
        }
    }

// org.apache.commons.codec.net.QCodecTest::testEncodeDecodeBlanks
    public void testEncodeDecodeBlanks() throws Exception {
        String plain = "Mind those pesky blanks";
        String encoded1 = "=?UTF-8?Q?Mind those pesky blanks?=";
        String encoded2 = "=?UTF-8?Q?Mind_those_pesky_blanks?=";
        QCodec qcodec = new QCodec();
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
        QCodec qcodec = new QCodec();
        qcodec.setEncodeBlanks(true);
        assertTrue(qcodec.isEncodeBlanks());
        qcodec.setEncodeBlanks(false);
        assertFalse(qcodec.isEncodeBlanks());
    }

// org.apache.commons.codec.net.QuotedPrintableCodecTest::testUTF8RoundTrip
    public void testUTF8RoundTrip() throws Exception {

        String ru_msg = constructString(RUSSIAN_STUFF_UNICODE); 
        String ch_msg = constructString(SWISS_GERMAN_STUFF_UNICODE); 
        
        QuotedPrintableCodec qpcodec = new QuotedPrintableCodec();
        
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
        QuotedPrintableCodec qpcodec = new QuotedPrintableCodec();
        String plain = "= Hello there =\r\n";
        String encoded = qpcodec.encode(plain);
        assertEquals("Basic quoted-printable encoding test", 
            "=3D Hello there =3D=0D=0A", encoded);
        assertEquals("Basic quoted-printable decoding test", 
            plain, qpcodec.decode(encoded));
    }

// org.apache.commons.codec.net.QuotedPrintableCodecTest::testSafeCharEncodeDecode
    public void testSafeCharEncodeDecode() throws Exception {
        QuotedPrintableCodec qpcodec = new QuotedPrintableCodec();
        String plain = "abc123_-.*~!@#$%^&()+{}\"\\;:`,/[]";
        String encoded = qpcodec.encode(plain);
        assertEquals("Safe chars quoted-printable encoding test", 
            plain, encoded);
        assertEquals("Safe chars quoted-printable decoding test", 
            plain, qpcodec.decode(encoded));
    }

// org.apache.commons.codec.net.QuotedPrintableCodecTest::testUnsafeEncodeDecode
    public void testUnsafeEncodeDecode() throws Exception {
        QuotedPrintableCodec qpcodec = new QuotedPrintableCodec();
        String plain = "=\r\n";
        String encoded = qpcodec.encode(plain);
        assertEquals("Unsafe chars quoted-printable encoding test", 
            "=3D=0D=0A", encoded);
        assertEquals("Unsafe chars quoted-printable decoding test", 
            plain, qpcodec.decode(encoded));
    }

// org.apache.commons.codec.net.QuotedPrintableCodecTest::testEncodeDecodeNull
    public void testEncodeDecodeNull() throws Exception {
        QuotedPrintableCodec qpcodec = new QuotedPrintableCodec();
        assertNull("Null string quoted-printable encoding test", 
            qpcodec.encode((String)null));
        assertNull("Null string quoted-printable decoding test", 
            qpcodec.decode((String)null));
    }

// org.apache.commons.codec.net.QuotedPrintableCodecTest::testDecodeInvalid
    public void testDecodeInvalid() throws Exception {
        QuotedPrintableCodec qpcodec = new QuotedPrintableCodec();
        try {
            qpcodec.decode("=");
            fail("DecoderException should have been thrown");
        } catch (DecoderException e) {
            
        }
        try {
            qpcodec.decode("=A");
            fail("DecoderException should have been thrown");
        } catch (DecoderException e) {
            
        }
        try {
            qpcodec.decode("=WW");
            fail("DecoderException should have been thrown");
        } catch (DecoderException e) {
            
        }
    }

// org.apache.commons.codec.net.QuotedPrintableCodecTest::testEncodeNull
    public void testEncodeNull() throws Exception {
        QuotedPrintableCodec qpcodec = new QuotedPrintableCodec();
        byte[] plain = null;
        byte[] encoded = qpcodec.encode(plain);
        assertEquals("Encoding a null string should return null", 
            null, encoded);
    }

// org.apache.commons.codec.net.QuotedPrintableCodecTest::testEncodeUrlWithNullBitSet
    public void testEncodeUrlWithNullBitSet() throws Exception {
        QuotedPrintableCodec qpcodec = new QuotedPrintableCodec();
        String plain = "1+1 = 2";
        String encoded = new String(QuotedPrintableCodec.
            encodeQuotedPrintable(null, plain.getBytes("UTF-8")));
        assertEquals("Basic quoted-printable encoding test", 
            "1+1 =3D 2", encoded);
        assertEquals("Basic quoted-printable decoding test", 
            plain, qpcodec.decode(encoded));
        
    }

// org.apache.commons.codec.net.QuotedPrintableCodecTest::testDecodeWithNullArray
    public void testDecodeWithNullArray() throws Exception {
        byte[] plain = null;
        byte[] result = QuotedPrintableCodec.decodeQuotedPrintable( plain );
        assertEquals("Result should be null", null, result);
    }

// org.apache.commons.codec.net.QuotedPrintableCodecTest::testEncodeStringWithNull
    public void testEncodeStringWithNull() throws Exception {
        QuotedPrintableCodec qpcodec = new QuotedPrintableCodec();
        String test = null;
        String result = qpcodec.encode( test, "charset" );
        assertEquals("Result should be null", null, result);
    }

// org.apache.commons.codec.net.QuotedPrintableCodecTest::testDecodeStringWithNull
    public void testDecodeStringWithNull() throws Exception {
        QuotedPrintableCodec qpcodec = new QuotedPrintableCodec();
        String test = null;
        String result = qpcodec.decode( test, "charset" );
        assertEquals("Result should be null", null, result);
    }

// org.apache.commons.codec.net.QuotedPrintableCodecTest::testEncodeObjects
    public void testEncodeObjects() throws Exception {
        QuotedPrintableCodec qpcodec = new QuotedPrintableCodec();
        String plain = "1+1 = 2";
        String encoded = (String) qpcodec.encode((Object) plain);
        assertEquals("Basic quoted-printable encoding test", 
            "1+1 =3D 2", encoded);

        byte[] plainBA = plain.getBytes("UTF-8");
        byte[] encodedBA = (byte[]) qpcodec.encode((Object) plainBA);
        encoded = new String(encodedBA);
        assertEquals("Basic quoted-printable encoding test", 
            "1+1 =3D 2", encoded);
            
        Object result = qpcodec.encode((Object) null);
        assertEquals( "Encoding a null Object should return null", null, result);
        
        try {
            Object dObj = new Double(3.0);
            qpcodec.encode( dObj );
            fail( "Trying to url encode a Double object should cause an exception.");
        } catch (EncoderException ee) {
            
        }
    }

// org.apache.commons.codec.net.QuotedPrintableCodecTest::testInvalidEncoding
    public void testInvalidEncoding() {
        QuotedPrintableCodec qpcodec = new QuotedPrintableCodec("NONSENSE");
           String plain = "Hello there!";
            try {
               qpcodec.encode(plain);
                fail( "We set the encoding to a bogus NONSENSE vlaue, this shouldn't have worked.");
            } catch (EncoderException ee) {
                
            }
            try {
               qpcodec.decode(plain);
                fail( "We set the encoding to a bogus NONSENSE vlaue, this shouldn't have worked.");
            } catch (DecoderException ee) {
                
            }
    }

// org.apache.commons.codec.net.QuotedPrintableCodecTest::testDecodeObjects
    public void testDecodeObjects() throws Exception {
        QuotedPrintableCodec qpcodec = new QuotedPrintableCodec();
        String plain = "1+1 =3D 2";
        String decoded = (String) qpcodec.decode((Object) plain);
        assertEquals("Basic quoted-printable decoding test", 
            "1+1 = 2", decoded);

        byte[] plainBA = plain.getBytes("UTF-8");
        byte[] decodedBA = (byte[]) qpcodec.decode((Object) plainBA);
        decoded = new String(decodedBA);
        assertEquals("Basic quoted-printable decoding test", 
            "1+1 = 2", decoded);
            
        Object result = qpcodec.decode((Object) null);
        assertEquals( "Decoding a null Object should return null", null, result);
        
        try {
            Object dObj = new Double(3.0);
            qpcodec.decode( dObj );
            fail( "Trying to url encode a Double object should cause an exception.");
        } catch (DecoderException ee) {
            
        }
    }

// org.apache.commons.codec.net.QuotedPrintableCodecTest::testDefaultEncoding
    public void testDefaultEncoding() throws Exception {
        String plain = "Hello there!";
        QuotedPrintableCodec qpcodec = new QuotedPrintableCodec("UnicodeBig");
        qpcodec.encode(plain); 
        String encoded1 = qpcodec.encode(plain, "UnicodeBig");
        String encoded2 = qpcodec.encode(plain);
        assertEquals(encoded1, encoded2);
    }

// org.apache.commons.codec.net.QuotedPrintableCodecTest::testSoftLineBreakDecode
    public void testSoftLineBreakDecode() throws Exception {
        String qpdata = "If you believe that truth=3Dbeauty, then surely=20=\r\nmathematics " +
                "is the most beautiful branch of philosophy.";
        String expected = "If you believe that truth=beauty, then surely mathematics " +
                "is the most beautiful branch of philosophy.";

        QuotedPrintableCodec qpcodec = new QuotedPrintableCodec();
        assertEquals(expected, qpcodec.decode(qpdata));

        String encoded = qpcodec.encode(expected);
        assertEquals(expected, qpcodec.decode(encoded));
    }

// org.apache.commons.codec.net.QuotedPrintableCodecTest::testSoftLineBreakEncode
    public void testSoftLineBreakEncode() throws Exception {
        String qpdata = "If you believe that truth=3Dbeauty, then surely mathematics is the most " +
                "b=\r\neautiful branch of philosophy.";
        String expected = "If you believe that truth=beauty, then surely mathematics is the most " +
                "beautiful branch of philosophy.";

        QuotedPrintableCodec qpcodec = new QuotedPrintableCodec();
        assertEquals(qpdata, qpcodec.encode(expected));

        String decoded = qpcodec.decode(qpdata);
        assertEquals(qpdata, qpcodec.encode(decoded));
    }

// org.apache.commons.codec.net.QuotedPrintableCodecTest::testSkipNotEncodedCRLF
    public void testSkipNotEncodedCRLF() throws Exception {
        String qpdata = "CRLF in an\n encoded text should be=20=\r\n\rskipped in the\r decoding.";
        String expected = "CRLF in an encoded text should be skipped in the decoding.";

        QuotedPrintableCodec qpcodec = new QuotedPrintableCodec();
        assertEquals(expected, qpcodec.decode(qpdata));

        String encoded = qpcodec.encode(expected);
        assertEquals(expected, qpcodec.decode(encoded));
    }

// org.apache.commons.codec.net.QuotedPrintableCodecTest::testTrailingSpecial
    public void testTrailingSpecial() throws Exception {
        final QuotedPrintableCodec qpcodec = new QuotedPrintableCodec();

        String plain ="This is a example of a quoted-printable text file. This might contain sp=cial chars.";
        String expected = "This is a example of a quoted-printable text file. This might contain sp=3D=\r\ncial chars.";
        assertEquals(expected, qpcodec.encode(plain));

        plain ="This is a example of a quoted-printable text file. This might contain ta\tbs as well.";
        expected = "This is a example of a quoted-printable text file. This might contain ta=09=\r\nbs as well.";
        assertEquals(expected, qpcodec.encode(plain));
    }

// org.apache.commons.codec.net.QuotedPrintableCodecTest::testUltimateSoftBreak
    public void testUltimateSoftBreak() throws Exception {
        final QuotedPrintableCodec qpcodec = new QuotedPrintableCodec();

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
        
        String plain ="This is a example of a quoted=printable text file. There is no tt";
        String expected = "This is a example of a quoted=3Dprintable text file. There is no tt";

        assertEquals(expected, new QuotedPrintableCodec().encode(plain));
    }

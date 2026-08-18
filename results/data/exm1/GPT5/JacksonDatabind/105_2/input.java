// buggy code
    public static JsonDeserializer<?> find(Class<?> rawType, String clsName)
    {
        if (_classNames.contains(clsName)) {
            JsonDeserializer<?> d = FromStringDeserializer.findDeserializer(rawType);
            if (d != null) {
                return d;
            }
            if (rawType == UUID.class) {
                return new UUIDDeserializer();
            }
            if (rawType == StackTraceElement.class) {
                return new StackTraceElementDeserializer();
            }
            if (rawType == AtomicBoolean.class) {
                // (note: AtomicInteger/Long work due to single-arg constructor. For now?
                return new AtomicBooleanDeserializer();
            }
            if (rawType == ByteBuffer.class) {
                return new ByteBufferDeserializer();
            }
        }
        return null;
    }

// relevant test
// com.fasterxml.jackson.databind.deser.jdk.JDKScalarsTest::testFloatWrapper
    public void testFloatWrapper() throws Exception
    {
        
        String[] STRS = new String[] {
            "1.0", "0.0", "-0.3", "0.7", "42.012", "-999.0", NAN_STRING
        };

        for (String str : STRS) {
            Float exp = Float.valueOf(str);
            Float result;

            if (NAN_STRING != str) {
                
                result = MAPPER.readValue(str, Float.class);
                assertEquals(exp, result);
            }

            
            result = MAPPER.readValue(" \""+str+"\"", Float.class);
            assertEquals(exp, result);
        }
    }

// com.fasterxml.jackson.databind.deser.jdk.JDKScalarsTest::testDoubleWrapper
    public void testDoubleWrapper() throws Exception
    {
        
        String[] STRS = new String[] {
            "1.0", "0.0", "-0.3", "0.7", "42.012", "-999.0", NAN_STRING
        };

        for (String str : STRS) {
            Double exp = Double.valueOf(str);
            Double result;

            
            if (NAN_STRING != str) {
                result = MAPPER.readValue(str, Double.class);
               assertEquals(exp, result);
            }
            
            result = MAPPER.readValue(" \""+str+"\"", Double.class);
            assertEquals(exp, result);
        }
    }

// com.fasterxml.jackson.databind.deser.jdk.JDKScalarsTest::testDoubleAsArray
    public void testDoubleAsArray() throws Exception
    {
        final ObjectMapper mapper = new ObjectMapper();
        mapper.disable(DeserializationFeature.UNWRAP_SINGLE_VALUE_ARRAYS);
        final double value = 0.016;
        try {
            mapper.readValue("{\"v\":[" + value + "]}", DoubleBean.class);
            fail("Did not throw exception when reading a value from a single value array with the UNWRAP_SINGLE_VALUE_ARRAYS feature disabled");
        } catch (JsonMappingException exp) {
            
        }
        
        mapper.enable(DeserializationFeature.UNWRAP_SINGLE_VALUE_ARRAYS);
        
        DoubleBean result = mapper.readValue("{\"v\":[" + value + "]}",
                DoubleBean.class);
        assertEquals(value, result._v);
        
        result = mapper.readValue("[{\"v\":[" + value + "]}]", DoubleBean.class);
        assertEquals(value, result._v);
        
        try {
            mapper.readValue("[{\"v\":[" + value + "," + value + "]}]", DoubleBean.class);
            fail("Did not throw exception while reading a value from a multi value array with UNWRAP_SINGLE_VALUE_ARRAY feature enabled");
        } catch (MismatchedInputException exp) {
            
        }
        
        result = mapper.readValue("{\"v\":[null]}", DoubleBean.class);
        assertNotNull(result);
        assertEquals(0d, result._v);

        double[] array = mapper.readValue("[ [ null ] ]", double[].class);
        assertNotNull(array);
        assertEquals(1, array.length);
        assertEquals(0d, array[0]);
    }

// com.fasterxml.jackson.databind.deser.jdk.JDKScalarsTest::testDoublePrimitiveNonNumeric
    public void testDoublePrimitiveNonNumeric() throws Exception
    {
        
        
        double value = Double.POSITIVE_INFINITY;
        DoubleBean result = MAPPER.readValue("{\"v\":\""+value+"\"}", DoubleBean.class);
        assertEquals(value, result._v);
        
        
        double[] array = MAPPER.readValue("[ \"Infinity\" ]", double[].class);
        assertNotNull(array);
        assertEquals(1, array.length);
        assertEquals(Double.POSITIVE_INFINITY, array[0]);
    }

// com.fasterxml.jackson.databind.deser.jdk.JDKScalarsTest::testFloatPrimitiveNonNumeric
    public void testFloatPrimitiveNonNumeric() throws Exception
    {
        
        float value = Float.POSITIVE_INFINITY;
        FloatBean result = MAPPER.readValue("{\"v\":\""+value+"\"}", FloatBean.class);
        assertEquals(value, result._v);
        
        
        float[] array = MAPPER.readValue("[ \"Infinity\" ]", float[].class);
        assertNotNull(array);
        assertEquals(1, array.length);
        assertEquals(Float.POSITIVE_INFINITY, array[0]);
    }

// com.fasterxml.jackson.databind.deser.jdk.JDKScalarsTest::testEmptyToNullCoercionForPrimitives
    public void testEmptyToNullCoercionForPrimitives() throws Exception {
        _testEmptyToNullCoercion(int.class, Integer.valueOf(0));
        _testEmptyToNullCoercion(long.class, Long.valueOf(0));
        _testEmptyToNullCoercion(double.class, Double.valueOf(0.0));
        _testEmptyToNullCoercion(float.class, Float.valueOf(0.0f));
    }

// com.fasterxml.jackson.databind.deser.jdk.JDKScalarsTest::testBase64Variants
    public void testBase64Variants() throws Exception
    {
        final byte[] INPUT = "abcdefghijklmnopqrstuvwxyz1234567890abcdefghijklmnopqrstuvwxyz1234567890X".getBytes("UTF-8");
        
        
        Assert.assertArrayEquals(INPUT, MAPPER.readValue(
                quote("YWJjZGVmZ2hpamtsbW5vcHFyc3R1dnd4eXoxMjM0NTY3ODkwYWJjZGVmZ2hpamtsbW5vcHFyc3R1dnd4eXoxMjM0NTY3ODkwWA=="),
                byte[].class));
        ObjectReader reader = MAPPER.readerFor(byte[].class);
        Assert.assertArrayEquals(INPUT, (byte[]) reader.with(Base64Variants.MIME_NO_LINEFEEDS).readValue(
                quote("YWJjZGVmZ2hpamtsbW5vcHFyc3R1dnd4eXoxMjM0NTY3ODkwYWJjZGVmZ2hpamtsbW5vcHFyc3R1dnd4eXoxMjM0NTY3ODkwWA=="
        )));

        
        Assert.assertArrayEquals(INPUT, (byte[]) reader.with(Base64Variants.MIME).readValue(
                quote("YWJjZGVmZ2hpamtsbW5vcHFyc3R1dnd4eXoxMjM0NTY3ODkwYWJjZGVmZ2hpamtsbW5vcHFyc3R1\\ndnd4eXoxMjM0NTY3ODkwWA=="
        )));
        Assert.assertArrayEquals(INPUT, (byte[]) reader.with(Base64Variants.MODIFIED_FOR_URL).readValue(
                quote("YWJjZGVmZ2hpamtsbW5vcHFyc3R1dnd4eXoxMjM0NTY3ODkwYWJjZGVmZ2hpamtsbW5vcHFyc3R1dnd4eXoxMjM0NTY3ODkwWA"
        )));
        
        Assert.assertArrayEquals(INPUT, (byte[]) reader.with(Base64Variants.PEM).readValue(
                quote("YWJjZGVmZ2hpamtsbW5vcHFyc3R1dnd4eXoxMjM0NTY3ODkwYWJjZGVmZ2hpamts\\nbW5vcHFyc3R1dnd4eXoxMjM0NTY3ODkwWA=="
        )));
    }

// com.fasterxml.jackson.databind.deser.jdk.JDKScalarsTest::testSequenceOfInts
    public void testSequenceOfInts() throws Exception
    {
        final int NR_OF_INTS = 100;

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < NR_OF_INTS; ++i) {
            sb.append(" ");
            sb.append(i);
        }
        JsonParser jp = MAPPER.getFactory().createParser(sb.toString());
        for (int i = 0; i < NR_OF_INTS; ++i) {
            Integer result = MAPPER.readValue(jp, Integer.class);
            assertEquals(Integer.valueOf(i), result);
        }
        jp.close();
    }

// com.fasterxml.jackson.databind.deser.jdk.JDKScalarsTest::testEmptyStringForWrappers
    public void testEmptyStringForWrappers() throws IOException
    {
        WrappersBean bean;

        bean = MAPPER.readValue("{\"booleanValue\":\"\"}", WrappersBean.class);
        assertNull(bean.booleanValue);
        bean = MAPPER.readValue("{\"byteValue\":\"\"}", WrappersBean.class);
        assertNull(bean.byteValue);

        
        bean = MAPPER.readValue("{\"charValue\":\"\"}", WrappersBean.class);
        assertNull(bean.charValue);

        bean = MAPPER.readValue("{\"shortValue\":\"\"}", WrappersBean.class);
        assertNull(bean.shortValue);
        bean = MAPPER.readValue("{\"intValue\":\"\"}", WrappersBean.class);
        assertNull(bean.intValue);
        bean = MAPPER.readValue("{\"longValue\":\"\"}", WrappersBean.class);
        assertNull(bean.longValue);
        bean = MAPPER.readValue("{\"floatValue\":\"\"}", WrappersBean.class);
        assertNull(bean.floatValue);
        bean = MAPPER.readValue("{\"doubleValue\":\"\"}", WrappersBean.class);
        assertNull(bean.doubleValue);
    }

// com.fasterxml.jackson.databind.deser.jdk.JDKScalarsTest::testEmptyStringForPrimitives
    public void testEmptyStringForPrimitives() throws IOException
    {
        PrimitivesBean bean;
        bean = MAPPER.readValue("{\"booleanValue\":\"\"}", PrimitivesBean.class);
        assertFalse(bean.booleanValue);
        bean = MAPPER.readValue("{\"byteValue\":\"\"}", PrimitivesBean.class);
        assertEquals((byte) 0, bean.byteValue);
        bean = MAPPER.readValue("{\"charValue\":\"\"}", PrimitivesBean.class);
        assertEquals((char) 0, bean.charValue);
        bean = MAPPER.readValue("{\"shortValue\":\"\"}", PrimitivesBean.class);
        assertEquals((short) 0, bean.shortValue);
        bean = MAPPER.readValue("{\"intValue\":\"\"}", PrimitivesBean.class);
        assertEquals(0, bean.intValue);
        bean = MAPPER.readValue("{\"longValue\":\"\"}", PrimitivesBean.class);
        assertEquals(0L, bean.longValue);
        bean = MAPPER.readValue("{\"floatValue\":\"\"}", PrimitivesBean.class);
        assertEquals(0.0f, bean.floatValue);
        bean = MAPPER.readValue("{\"doubleValue\":\"\"}", PrimitivesBean.class);
        assertEquals(0.0, bean.doubleValue);
    }

// com.fasterxml.jackson.databind.deser.jdk.JDKScalarsTest::testEmptyStringFailForPrimitives
    public void testEmptyStringFailForPrimitives() throws IOException
    {
        _verifyEmptyStringFailForPrimitives("booleanValue");
        _verifyEmptyStringFailForPrimitives("byteValue");
        _verifyEmptyStringFailForPrimitives("charValue");
        _verifyEmptyStringFailForPrimitives("shortValue");
        _verifyEmptyStringFailForPrimitives("intValue");
        _verifyEmptyStringFailForPrimitives("longValue");
        _verifyEmptyStringFailForPrimitives("floatValue");
        _verifyEmptyStringFailForPrimitives("doubleValue");
    }

// com.fasterxml.jackson.databind.deser.jdk.JDKScalarsTest::testNullForPrimitives
    public void testNullForPrimitives() throws IOException
    {
        
        PrimitivesBean bean = MAPPER.readValue(
                "{\"intValue\":null, \"booleanValue\":null, \"doubleValue\":null}",
                PrimitivesBean.class);
        assertNotNull(bean);
        assertEquals(0, bean.intValue);
        assertEquals(false, bean.booleanValue);
        assertEquals(0.0, bean.doubleValue);

        bean = MAPPER.readValue("{\"byteValue\":null, \"longValue\":null, \"floatValue\":null}",
                PrimitivesBean.class);
        assertNotNull(bean);
        assertEquals((byte) 0, bean.byteValue);
        assertEquals(0L, bean.longValue);
        assertEquals(0.0f, bean.floatValue);

        
        final ObjectReader reader = MAPPER
                .readerFor(PrimitivesBean.class)
                .with(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES);
        
        try {
            reader.readValue("{\"booleanValue\":null}");
            fail("Expected failure for boolean + null");
        } catch (MismatchedInputException e) {
            verifyException(e, "Cannot map `null` into type boolean");
            verifyPath(e, "booleanValue");
        }
        
        try {
            reader.readValue("{\"byteValue\":null}");
            fail("Expected failure for byte + null");
        } catch (MismatchedInputException e) {
            verifyException(e, "Cannot map `null` into type byte");
            verifyPath(e, "byteValue");
        }
        try {
            reader.readValue("{\"charValue\":null}");
            fail("Expected failure for char + null");
        } catch (MismatchedInputException e) {
            verifyException(e, "Cannot map `null` into type char");
            verifyPath(e, "charValue");
        }
        try {
            reader.readValue("{\"shortValue\":null}");
            fail("Expected failure for short + null");
        } catch (MismatchedInputException e) {
            verifyException(e, "Cannot map `null` into type short");
            verifyPath(e, "shortValue");
        }
        try {
            reader.readValue("{\"intValue\":null}");
            fail("Expected failure for int + null");
        } catch (MismatchedInputException e) {
            verifyException(e, "Cannot map `null` into type int");
            verifyPath(e, "intValue");
        }
        try {
            reader.readValue("{\"longValue\":null}");
            fail("Expected failure for long + null");
        } catch (MismatchedInputException e) {
            verifyException(e, "Cannot map `null` into type long");
            verifyPath(e, "longValue");
        }

        
        try {
            reader.readValue("{\"floatValue\":null}");
            fail("Expected failure for float + null");
        } catch (MismatchedInputException e) {
            verifyException(e, "Cannot map `null` into type float");
            verifyPath(e, "floatValue");
        }
        try {
            reader.readValue("{\"doubleValue\":null}");
            fail("Expected failure for double + null");
        } catch (MismatchedInputException e) {
            verifyException(e, "Cannot map `null` into type double");
            verifyPath(e, "doubleValue");
        }
    }

// com.fasterxml.jackson.databind.deser.jdk.JDKScalarsTest::testNullForPrimitivesViaCreator
    public void testNullForPrimitivesViaCreator() throws IOException
    {
        try {
             MAPPER
                    .readerFor(PrimitiveCreatorBean.class)
                    .with(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES)
                    .readValue(aposToQuotes("{'a': null}"));
            fail("Expected failure for `int` and `null`");
        } catch (MismatchedInputException e) {
            verifyException(e, "Cannot map `null` into type int");
            verifyPath(e, "a");
        }
    }

// com.fasterxml.jackson.databind.deser.jdk.JDKScalarsTest::testNullForPrimitiveArrays
    public void testNullForPrimitiveArrays() throws IOException
    {
        _testNullForPrimitiveArrays(boolean[].class, Boolean.FALSE);
        _testNullForPrimitiveArrays(byte[].class, Byte.valueOf((byte) 0));
        _testNullForPrimitiveArrays(char[].class, Character.valueOf((char) 0), false);
        _testNullForPrimitiveArrays(short[].class, Short.valueOf((short)0));
        _testNullForPrimitiveArrays(int[].class, Integer.valueOf(0));
        _testNullForPrimitiveArrays(long[].class, Long.valueOf(0L));
        _testNullForPrimitiveArrays(float[].class, Float.valueOf(0f));
        _testNullForPrimitiveArrays(double[].class, Double.valueOf(0d));
    }

// com.fasterxml.jackson.databind.deser.jdk.JDKScalarsTest::testVoidDeser
    public void testVoidDeser() throws Exception
    {
        VoidBean bean = MAPPER.readValue(aposToQuotes("{'value' : 123 }"),
                VoidBean.class);
        assertNull(bean.value);
    }

// com.fasterxml.jackson.databind.deser.jdk.JDKScalarsTest::testInvalidStringCoercionFail
    public void testInvalidStringCoercionFail() throws IOException
    {
        _testInvalidStringCoercionFail(boolean[].class);
        _testInvalidStringCoercionFail(byte[].class);

        

        _testInvalidStringCoercionFail(short[].class);
        _testInvalidStringCoercionFail(int[].class);
        _testInvalidStringCoercionFail(long[].class);
        _testInvalidStringCoercionFail(float[].class);
        _testInvalidStringCoercionFail(double[].class);
    }

// com.fasterxml.jackson.databind.deser.jdk.JDKStringLikeTypesTest::testByteBuffer
    public void testByteBuffer() throws Exception
    {
        byte[] INPUT = new byte[] { 1, 3, 9, -1, 6 };
        String exp = MAPPER.writeValueAsString(INPUT);
        ByteBuffer result = MAPPER.readValue(exp,  ByteBuffer.class); 
        assertNotNull(result);
        assertEquals(INPUT.length, result.remaining());
        for (int i = 0; i < INPUT.length; ++i) {
            assertEquals(INPUT[i], result.get());
        }
        assertEquals(0, result.remaining());
    }

// com.fasterxml.jackson.databind.deser.jdk.JDKStringLikeTypesTest::testCharset
    public void testCharset() throws Exception
    {
        Charset UTF8 = Charset.forName("UTF-8");
        assertSame(UTF8, MAPPER.readValue(quote("UTF-8"), Charset.class));
    }

// com.fasterxml.jackson.databind.deser.jdk.JDKStringLikeTypesTest::testClass
    public void testClass() throws IOException
    {
        ObjectMapper mapper = new ObjectMapper();
        assertSame(String.class, mapper.readValue(quote("java.lang.String"), Class.class));

        
        assertSame(Boolean.TYPE, mapper.readValue(quote("boolean"), Class.class));
        assertSame(Byte.TYPE, mapper.readValue(quote("byte"), Class.class));
        assertSame(Short.TYPE, mapper.readValue(quote("short"), Class.class));
        assertSame(Character.TYPE, mapper.readValue(quote("char"), Class.class));
        assertSame(Integer.TYPE, mapper.readValue(quote("int"), Class.class));
        assertSame(Long.TYPE, mapper.readValue(quote("long"), Class.class));
        assertSame(Float.TYPE, mapper.readValue(quote("float"), Class.class));
        assertSame(Double.TYPE, mapper.readValue(quote("double"), Class.class));
        assertSame(Void.TYPE, mapper.readValue(quote("void"), Class.class));
    }

// com.fasterxml.jackson.databind.deser.jdk.JDKStringLikeTypesTest::testClassWithParams
    public void testClassWithParams() throws IOException
    {
        String json = MAPPER.writeValueAsString(new ParamClassBean("Foobar"));

        ParamClassBean result = MAPPER.readValue(json, ParamClassBean.class);
        assertEquals("Foobar", result.name);
        assertSame(String.class, result.clazz);
    }

// com.fasterxml.jackson.databind.deser.jdk.JDKStringLikeTypesTest::testCurrency
    public void testCurrency() throws IOException
    {
        Currency usd = Currency.getInstance("USD");
        assertEquals(usd, new ObjectMapper().readValue(quote("USD"), Currency.class));
    }

// com.fasterxml.jackson.databind.deser.jdk.JDKStringLikeTypesTest::testFile
    public void testFile() throws Exception
    {
        
        File src = new File("/test").getAbsoluteFile();
        String abs = src.getAbsolutePath();

        
        String json = MAPPER.writeValueAsString(abs);
        File result = MAPPER.readValue(json, File.class);
        assertEquals(abs, result.getAbsolutePath());
    }

// com.fasterxml.jackson.databind.deser.jdk.JDKStringLikeTypesTest::testLocale
    public void testLocale() throws IOException
    {
        assertEquals(new Locale("en"), MAPPER.readValue(quote("en"), Locale.class));
        assertEquals(new Locale("es", "ES"), MAPPER.readValue(quote("es_ES"), Locale.class));
        assertEquals(new Locale("FI", "fi", "savo"),
                MAPPER.readValue(quote("fi_FI_savo"), Locale.class));
        assertEquals(new Locale("en", "US"),
                MAPPER.readValue(quote("en-US"), Locale.class));

        
        Locale loc = MAPPER.readValue(quote(""), Locale.class);
        assertSame(Locale.ROOT, loc);
    }

// com.fasterxml.jackson.databind.deser.jdk.JDKStringLikeTypesTest::testCharSequence
    public void testCharSequence() throws IOException
    {
        CharSequence cs = MAPPER.readValue("\"abc\"", CharSequence.class);
        assertEquals(String.class, cs.getClass());
        assertEquals("abc", cs.toString());
    }

// com.fasterxml.jackson.databind.deser.jdk.JDKStringLikeTypesTest::testInetAddress
    public void testInetAddress() throws IOException
    {
        InetAddress address = MAPPER.readValue(quote("127.0.0.1"), InetAddress.class);
        assertEquals("127.0.0.1", address.getHostAddress());

        
        final String HOST = "google.com";
        address = MAPPER.readValue(quote(HOST), InetAddress.class);
        assertEquals(HOST, address.getHostName());
    }

// com.fasterxml.jackson.databind.deser.jdk.JDKStringLikeTypesTest::testInetSocketAddress
    public void testInetSocketAddress() throws IOException
    {
        InetSocketAddress address = MAPPER.readValue(quote("127.0.0.1"), InetSocketAddress.class);
        assertEquals("127.0.0.1", address.getAddress().getHostAddress());

        InetSocketAddress ip6 = MAPPER.readValue(
                quote("2001:db8:85a3:8d3:1319:8a2e:370:7348"), InetSocketAddress.class);
        assertEquals("2001:db8:85a3:8d3:1319:8a2e:370:7348", ip6.getAddress().getHostAddress());

        InetSocketAddress ip6port = MAPPER.readValue(
                quote("[2001:db8:85a3:8d3:1319:8a2e:370:7348]:443"), InetSocketAddress.class);
        assertEquals("2001:db8:85a3:8d3:1319:8a2e:370:7348", ip6port.getAddress().getHostAddress());
        assertEquals(443, ip6port.getPort());

        
        final String HOST = "www.google.com";
        address = MAPPER.readValue(quote(HOST), InetSocketAddress.class);
        assertEquals(HOST, address.getHostName());

        final String HOST_AND_PORT = HOST+":80";
        address = MAPPER.readValue(quote(HOST_AND_PORT), InetSocketAddress.class);
        assertEquals(HOST, address.getHostName());
        assertEquals(80, address.getPort());
    }

// com.fasterxml.jackson.databind.deser.jdk.JDKStringLikeTypesTest::testRegexps
    public void testRegexps() throws IOException
    {
        final String PATTERN_STR = "abc:\\s?(\\d+)";
        Pattern exp = Pattern.compile(PATTERN_STR);
        
        String json = MAPPER.writeValueAsString(exp);
        Pattern result = MAPPER.readValue(json, Pattern.class);
        assertEquals(exp.pattern(), result.pattern());
    }

// com.fasterxml.jackson.databind.deser.jdk.JDKStringLikeTypesTest::testStackTraceElement
    public void testStackTraceElement() throws Exception
    {
        StackTraceElement elem = null;
        try {
            throw new IllegalStateException();
        } catch (Exception e) {
            elem = e.getStackTrace()[0];
        }
        String json = MAPPER.writeValueAsString(elem);
        StackTraceElement back = MAPPER.readValue(json, StackTraceElement.class);
        
        assertEquals("testStackTraceElement", back.getMethodName());
        assertEquals(elem.getLineNumber(), back.getLineNumber());
        assertEquals(elem.getClassName(), back.getClassName());
        assertEquals(elem.isNativeMethod(), back.isNativeMethod());
        assertTrue(back.getClassName().endsWith("JDKStringLikeTypesTest"));
        assertFalse(back.isNativeMethod());
    }

// com.fasterxml.jackson.databind.deser.jdk.JDKStringLikeTypesTest::testStackTraceElementWithCustom
    public void testStackTraceElementWithCustom() throws Exception
    {
        
        StackTraceBean bean = MAPPER.readValue(aposToQuotes("{'Location':'foobar'}"),
                StackTraceBean.class);
        assertNotNull(bean);
        assertNotNull(bean.location);
        assertEquals(StackTraceBean.NUM, bean.location.getLineNumber());

        
        ObjectMapper mapper = new ObjectMapper();
        SimpleModule module = new SimpleModule();
        module.addDeserializer(StackTraceElement.class, new MyStackTraceElementDeserializer());
        mapper.registerModule(module);
        
        StackTraceElement elem = mapper.readValue("123", StackTraceElement.class);
        assertNotNull(elem);
        assertEquals(StackTraceBean.NUM, elem.getLineNumber());
 
        
        
        IOException ioe = mapper.readValue(aposToQuotes("{'stackTrace':[ 123, 456 ]}"),
                IOException.class);
        assertNotNull(ioe);
        StackTraceElement[] traces = ioe.getStackTrace();
        assertNotNull(traces);
        assertEquals(2, traces.length);
        assertEquals(StackTraceBean.NUM, traces[0].getLineNumber());
        assertEquals(StackTraceBean.NUM, traces[1].getLineNumber());
    }

// com.fasterxml.jackson.databind.deser.jdk.JDKStringLikeTypesTest::testStringBuilder
    public void testStringBuilder() throws Exception
    {
        StringBuilder sb = MAPPER.readValue(quote("abc"), StringBuilder.class);
        assertEquals("abc", sb.toString());
    }

// com.fasterxml.jackson.databind.deser.jdk.JDKStringLikeTypesTest::testURI
    public void testURI() throws Exception
    {
        final ObjectReader reader = MAPPER.readerFor(URI.class);
        final URI value = new URI("http://foo.com");
        assertEquals(value, reader.readValue("\""+value.toString()+"\""));

        
        URI result = reader.readValue(quote(""));
        assertNotNull(result);
        assertEquals(URI.create(""), result);
        
        
        try {
            result = reader.readValue(quote("a b"));
            fail("Should not accept malformed URI, instead got: "+result);
        } catch (InvalidFormatException e) {
            verifyException(e, "not a valid textual representation");
        }
    }

// com.fasterxml.jackson.databind.deser.jdk.JDKStringLikeTypesTest::testURL
    public void testURL() throws Exception
    {
        URL exp = new URL("http://foo.com");
        assertEquals(exp, MAPPER.readValue("\""+exp.toString()+"\"", URL.class));

        
        TokenBuffer buf = new TokenBuffer(null, false);
        buf.writeObject(null);
        assertNull(MAPPER.readValue(buf.asParser(), URL.class));
        buf.close();

        
        buf = new TokenBuffer(null, false);
        buf.writeObject(exp);
        assertSame(exp, MAPPER.readValue(buf.asParser(), URL.class));
        buf.close();

        
        try {
            URL result = MAPPER.readValue(quote("a b"), URL.class);
            fail("Should not accept malformed URI, instead got: "+result);
        } catch (InvalidFormatException e) {
            verifyException(e, "not a valid textual representation");
        }
    }

// com.fasterxml.jackson.databind.deser.jdk.JDKStringLikeTypesTest::testUUID
    public void testUUID() throws Exception
    {
        final ObjectMapper mapper = objectMapper();
        
        final String NULL_UUID = "00000000-0000-0000-0000-000000000000";
        
        for (String value : new String[] {
                "76e6d183-5f68-4afa-b94a-922c1fdb83f8",
                "540a88d1-e2d8-4fb1-9396-9212280d0a7f",
                "2c9e441d-1cd0-472d-9bab-69838f877574",
                "591b2869-146e-41d7-8048-e8131f1fdec5",
                "82994ac2-7b23-49f2-8cc5-e24cf6ed77be",
                "00000007-0000-0000-0000-000000000000"
        }) {
            
            mapper.disable(DeserializationFeature.UNWRAP_SINGLE_VALUE_ARRAYS);
            
            UUID uuid = UUID.fromString(value);
            assertEquals(uuid,
                    mapper.readValue(quote(value), UUID.class));
        }
        
        
        final String TEMPL = NULL_UUID;
        final String chars = "123456789abcdefABCDEF";

        for (int i = 0; i < chars.length(); ++i) {
            String value = TEMPL.replace('0', chars.charAt(i));
            assertEquals(UUID.fromString(value).toString(),
                    mapper.readValue(quote(value), UUID.class).toString());
        }

        
        String base64 = Base64Variants.getDefaultVariant().encode(new byte[16]);
        assertEquals(UUID.fromString(NULL_UUID),
                mapper.readValue(quote(base64), UUID.class));
    }

// com.fasterxml.jackson.databind.deser.jdk.JDKStringLikeTypesTest::testUUIDInvalid
    public void testUUIDInvalid() throws Exception
    {
        
        try {
            MAPPER.readValue(quote("abcde"), UUID.class);
            fail("Should fail on invalid UUID string");
        } catch (InvalidFormatException e) {
            verifyException(e, "UUID has to be represented by standard");
        }
        try {
            MAPPER.readValue(quote("76e6d183-5f68-4afa-b94a-922c1fdb83fx"), UUID.class);
            fail("Should fail on invalid UUID string");
        } catch (InvalidFormatException e) {
            verifyException(e, "non-hex character 'x'");
        }
        
    }

// com.fasterxml.jackson.databind.deser.jdk.JDKStringLikeTypesTest::testUUIDAux
    public void testUUIDAux() throws Exception
    {
        
        final UUID value = UUID.fromString("76e6d183-5f68-4afa-b94a-922c1fdb83f8");

        
        TokenBuffer buf = new TokenBuffer(null, false);
        buf.writeObject(null);
        assertNull(MAPPER.readValue(buf.asParser(), UUID.class));
        buf.close();

        
        buf = new TokenBuffer(null, false);
        buf.writeObject(value);
        assertSame(value, MAPPER.readValue(buf.asParser(), UUID.class));

        
        
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(bytes);
        out.writeLong(value.getMostSignificantBits());
        out.writeLong(value.getLeastSignificantBits());
        byte[] data = bytes.toByteArray();
        assertEquals(16, data.length);
        
        buf.writeObject(data);

        UUID value2 = MAPPER.readValue(buf.asParser(), UUID.class);
        
        assertEquals(value, value2);
        buf.close();
    }

// com.fasterxml.jackson.databind.deser.jdk.MapDeserializationTest::testBigUntypedMap
    public void testBigUntypedMap() throws Exception
    {
        Map<String,Object> map = new LinkedHashMap<String,Object>();
        for (int i = 0; i < 1100; ++i) {
            if ((i & 1) == 0) {
                map.put(String.valueOf(i), Integer.valueOf(i));
            } else {
                Map<String,Object> map2 = new LinkedHashMap<String,Object>();
                map2.put("x", Integer.valueOf(i));
                map.put(String.valueOf(i), map2);
            }
        }
        String json = MAPPER.writeValueAsString(map);
        Object bound = MAPPER.readValue(json, Object.class);
        assertEquals(map, bound);
    }

// com.fasterxml.jackson.databind.deser.jdk.MapDeserializationTest::testUntypedMap2
    public void testUntypedMap2() throws Exception
    {
        
        String JSON = "{ \"a\" : \"x\" }";

        @SuppressWarnings("unchecked")
        HashMap<String,Object> result =  MAPPER.readValue(JSON, HashMap.class);
        assertNotNull(result);
        assertTrue(result instanceof Map<?,?>);

        assertEquals(1, result.size());

        assertEquals("x", result.get("a"));
    }

// com.fasterxml.jackson.databind.deser.jdk.MapDeserializationTest::testUntypedMap3
    public void testUntypedMap3() throws Exception
    {
        String JSON = "{\"a\":[{\"a\":\"b\"},\"value\"]}";
        Map<?,?> result = MAPPER.readValue(JSON, Map.class);
        assertTrue(result instanceof Map<?,?>);
        assertEquals(1, result.size());
        Object ob = result.get("a");
        assertNotNull(ob);
        Collection<?> list = (Collection<?>)ob;
        assertEquals(2, list.size());

        JSON = "{ \"var1\":\"val1\", \"var2\":\"val2\", "
            +"\"subvars\": ["
            +" {  \"subvar1\" : \"subvar2\", \"x\" : \"y\" }, "
            +" { \"a\":1 } ]"
            +" }"
            ;
        result = MAPPER.readValue(JSON, Map.class);
        assertTrue(result instanceof Map<?,?>);
        assertEquals(3, result.size());
    }

// com.fasterxml.jackson.databind.deser.jdk.MapDeserializationTest::testSpecialMap
    public void testSpecialMap() throws IOException
    {
       final ObjectWrapperMap map = MAPPER.readValue(UNTYPED_MAP_JSON, ObjectWrapperMap.class);
       assertNotNull(map);
       _doTestUntyped(map);
    }

// com.fasterxml.jackson.databind.deser.jdk.MapDeserializationTest::testGenericMap
    public void testGenericMap() throws IOException
    {
        final Map<String, ObjectWrapper> map = MAPPER.readValue
            (UNTYPED_MAP_JSON,
             new TypeReference<Map<String, ObjectWrapper>>() { });
       _doTestUntyped(map);
    }

// com.fasterxml.jackson.databind.deser.jdk.MapDeserializationTest::testFromEmptyString
    public void testFromEmptyString() throws Exception
    {
        ObjectMapper m = new ObjectMapper();
        m.configure(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, true);
        Map<?,?> result = m.readValue(quote(""), Map.class);
        assertNull(result);
    }

// com.fasterxml.jackson.databind.deser.jdk.MapDeserializationTest::testExactStringIntMap
    public void testExactStringIntMap() throws Exception
    {
        
        String JSON = "{ \"foo\" : 13, \"bar\" : -39, \n \"\" : 0 }";
        Map<String,Integer> result = MAPPER.readValue
            (JSON, new TypeReference<HashMap<String,Integer>>() { });

        assertNotNull(result);
        assertEquals(HashMap.class, result.getClass());
        assertEquals(3, result.size());

        assertEquals(Integer.valueOf(13), result.get("foo"));
        assertEquals(Integer.valueOf(-39), result.get("bar"));
        assertEquals(Integer.valueOf(0), result.get(""));
        assertNull(result.get("foobar"));
        assertNull(result.get(" "));
    }

// com.fasterxml.jackson.databind.deser.jdk.MapDeserializationTest::testIntBooleanMap
    public void testIntBooleanMap() throws Exception
    {
        
        String JSON = "{ \"1\" : true, \"-1\" : false }";
        Map<?,Object> result = MAPPER.readValue
            (JSON, new TypeReference<HashMap<Integer,Object>>() { });

        assertNotNull(result);
        assertEquals(HashMap.class, result.getClass());
        assertEquals(2, result.size());

        assertEquals(Boolean.TRUE, result.get(Integer.valueOf(1)));
        assertEquals(Boolean.FALSE, result.get(Integer.valueOf(-1)));
        assertNull(result.get("foobar"));
        assertNull(result.get(0));
    }

// com.fasterxml.jackson.databind.deser.jdk.MapDeserializationTest::testExactStringStringMap
    public void testExactStringStringMap() throws Exception
    {
        
        String JSON = "{ \"a\" : \"b\" }";
        Map<String,Integer> result = MAPPER.readValue
            (JSON, new TypeReference<TreeMap<String,String>>() { });

        assertNotNull(result);
        assertEquals(TreeMap.class, result.getClass());
        assertEquals(1, result.size());

        assertEquals("b", result.get("a"));
        assertNull(result.get("b"));
    }

// com.fasterxml.jackson.databind.deser.jdk.MapDeserializationTest::testGenericStringIntMap
    public void testGenericStringIntMap() throws Exception
    {
        
        String JSON = "{ \"a\" : 1, \"b\" : 2, \"c\" : -99 }";
        Map<String,Integer> result = MAPPER.readValue
            (JSON, new TypeReference<Map<String,Integer>>() { });
        assertNotNull(result);
        assertTrue(result instanceof Map<?,?>);
        assertEquals(3, result.size());

        assertEquals(Integer.valueOf(-99), result.get("c"));
        assertEquals(Integer.valueOf(2), result.get("b"));
        assertEquals(Integer.valueOf(1), result.get("a"));

        assertNull(result.get(""));
    }

// com.fasterxml.jackson.databind.deser.jdk.MapDeserializationTest::testEnumMap
    public void testEnumMap() throws Exception
    {
        String JSON = "{ \"KEY1\" : \"\", \"WHATEVER\" : null }";

        
        EnumMap<Key,String> result = MAPPER.readValue
            (JSON, new TypeReference<EnumMap<Key,String>>() { });

        assertNotNull(result);
        assertEquals(EnumMap.class, result.getClass());
        assertEquals(2, result.size());

        assertEquals("", result.get(Key.KEY1));
        
        assertTrue(result.containsKey(Key.WHATEVER));
        assertNull(result.get(Key.WHATEVER));

        
        assertFalse(result.containsKey(Key.KEY2));
        assertNull(result.get(Key.KEY2));
    }

// com.fasterxml.jackson.databind.deser.jdk.MapDeserializationTest::testMapWithEnums
    public void testMapWithEnums() throws Exception
    {
        String JSON = "{ \"KEY2\" : \"WHATEVER\" }";

        
        Map<Enum<?>,Enum<?>> result = MAPPER.readValue
            (JSON, new TypeReference<Map<Key,Key>>() { });

        assertNotNull(result);
        assertTrue(result instanceof Map<?,?>);
        assertEquals(1, result.size());

        assertEquals(Key.WHATEVER, result.get(Key.KEY2));
        assertNull(result.get(Key.WHATEVER));
        assertNull(result.get(Key.KEY1));
    }

// com.fasterxml.jackson.databind.deser.jdk.MapDeserializationTest::testEnumPolymorphicSerializationTest
    public void testEnumPolymorphicSerializationTest() throws Exception 
    {
        ObjectMapper mapper = new ObjectMapper();
        List<ITestType> testTypesList = new ArrayList<ITestType>();
        testTypesList.add(ConcreteType.ONE);
        testTypesList.add(ConcreteType.TWO);
        ListContainer listContainer = new ListContainer();
        listContainer.testTypes = testTypesList;
        String json = mapper.writeValueAsString(listContainer);
        listContainer = mapper.readValue(json, ListContainer.class);
        EnumMapContainer enumMapContainer = new EnumMapContainer();
        EnumMap<KeyEnum,ITestType> testTypesMap = new EnumMap<KeyEnum,ITestType>(KeyEnum.class);
        testTypesMap.put(KeyEnum.A, ConcreteType.ONE);
        testTypesMap.put(KeyEnum.B, ConcreteType.TWO);
        enumMapContainer.testTypes = testTypesMap;
        
        json = mapper.writeValueAsString(enumMapContainer);
        enumMapContainer = mapper.readValue(json, EnumMapContainer.class);
    }

// com.fasterxml.jackson.databind.deser.jdk.MapDeserializationTest::testDateMap
    public void testDateMap() throws Exception
    {
    	 Date date1=new Date(123456000L);
    	 DateFormat fmt = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz", Locale.US);
         
    	 String JSON = "{ \""+  fmt.format(date1)+"\" : \"\", \""+new Date(0).getTime()+"\" : null }";
    	 HashMap<Date,String> result=  MAPPER.readValue
    	            (JSON, new TypeReference<HashMap<Date,String>>() { });
    	 
    	 assertNotNull(result);
    	 assertEquals(HashMap.class, result.getClass());
    	 assertEquals(2, result.size());
    	 
    	 assertTrue(result.containsKey(date1));
    	 assertEquals("", result.get(new Date(123456000L)));

    	 assertTrue(result.containsKey(new Date(0)));
    	 assertNull(result.get(new Date(0)));
    }

// com.fasterxml.jackson.databind.deser.jdk.MapDeserializationTest::testCalendarMap
    public void testCalendarMap() throws Exception
    {
        
        TimeZone tz = MAPPER.getSerializationConfig().getTimeZone();        
        Calendar c = Calendar.getInstance(tz);

        c.setTimeInMillis(123456000L);
        DateFormat fmt = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz", Locale.US);
        String JSON = "{ \""+fmt.format(c.getTime())+"\" : \"\", \""+new Date(0).getTime()+"\" : null }";
        HashMap<Calendar,String> result = MAPPER.readValue
                (JSON, new TypeReference<HashMap<Calendar,String>>() { });

        assertNotNull(result);
        assertEquals(HashMap.class, result.getClass());
        assertEquals(2, result.size());

        assertTrue(result.containsKey(c));
        assertEquals("", result.get(c));
        c.setTimeInMillis(0);
        assertTrue(result.containsKey(c));
        assertNull(result.get(c));
    }

// com.fasterxml.jackson.databind.deser.jdk.MapDeserializationTest::testUUIDKeyMap
    public void testUUIDKeyMap() throws Exception
    {
         UUID key = UUID.nameUUIDFromBytes("foobar".getBytes("UTF-8"));
         String JSON = "{ \""+key+"\":4}";
         Map<UUID,Object> result = MAPPER.readValue(JSON, new TypeReference<Map<UUID,Object>>() { });
         assertNotNull(result);
         assertEquals(1, result.size());
         Object ob = result.keySet().iterator().next();
         assertNotNull(ob);
         assertEquals(UUID.class, ob.getClass());
         assertEquals(key, ob);
    }

// com.fasterxml.jackson.databind.deser.jdk.MapDeserializationTest::testLocaleKeyMap
    public void testLocaleKeyMap() throws Exception {
        Locale key = Locale.CHINA;
        String JSON = "{ \"" + key + "\":4}";
        Map<Locale, Object> result = MAPPER.readValue(JSON, new TypeReference<Map<Locale, Object>>() {
        });
        assertNotNull(result);
        assertEquals(1, result.size());
        Object ob = result.keySet().iterator().next();
        assertNotNull(ob);
        assertEquals(Locale.class, ob.getClass());
        assertEquals(key, ob);
    }

// com.fasterxml.jackson.databind.deser.jdk.MapDeserializationTest::testCurrencyKeyMap
    public void testCurrencyKeyMap() throws Exception {
        Currency key = Currency.getInstance("USD");
        String JSON = "{ \"" + key + "\":4}";
        Map<Currency, Object> result = MAPPER.readValue(JSON, new TypeReference<Map<Currency, Object>>() {
        });
        assertNotNull(result);
        assertEquals(1, result.size());
        Object ob = result.keySet().iterator().next();
        assertNotNull(ob);
        assertEquals(Currency.class, ob.getClass());
        assertEquals(key, ob);
    }

// com.fasterxml.jackson.databind.deser.jdk.MapDeserializationTest::testKeyWithCreator
    public void testKeyWithCreator() throws Exception
    {
        
        KeyType key = MAPPER.readValue(quote("abc"), KeyType.class);
        assertEquals("abc", key.value);

        Map<KeyType,Integer> map = MAPPER.readValue("{\"foo\":3}", new TypeReference<Map<KeyType,Integer>>() {} );
        assertEquals(1, map.size());
        key = map.keySet().iterator().next();
        assertEquals("foo", key.value);
    }

// com.fasterxml.jackson.databind.deser.jdk.MapDeserializationTest::testClassKeyMap
    public void testClassKeyMap() throws Exception {
        ClassStringMap map = MAPPER.readValue(aposToQuotes("{'java.lang.String':'foo'}"),
                ClassStringMap.class);
        assertNotNull(map);
        assertEquals(1, map.size());
        assertEquals("foo", map.get(String.class));
    }

// com.fasterxml.jackson.databind.deser.jdk.MapDeserializationTest::testcharSequenceKeyMap
    public void testcharSequenceKeyMap() throws Exception {
        String JSON = aposToQuotes("{'a':'b'}");
        Map<CharSequence,String> result = MAPPER.readValue(JSON, new TypeReference<Map<CharSequence,String>>() { });
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("b", result.get("a"));
    }

// com.fasterxml.jackson.databind.deser.jdk.MapDeserializationTest::testMapWithDeserializer
    public void testMapWithDeserializer() throws Exception
    {
        CustomMap result = MAPPER.readValue(quote("xyz"), CustomMap.class);
        assertEquals(1, result.size());
        assertEquals("xyz", result.get("x"));
    }

// com.fasterxml.jackson.databind.deser.jdk.MapDeserializationTest::testMapError
    public void testMapError() throws Exception
    {
        try {
            Object result = MAPPER.readValue("[ 1, 2 ]", 
                    new TypeReference<Map<String,String>>() { });
            fail("Expected an exception, but got result value: "+result);
        } catch (JsonMappingException jex) {
            verifyException(jex, "START_ARRAY");
        }
    }

// com.fasterxml.jackson.databind.deser.jdk.MapDeserializationTest::testNoCtorMap
    public void testNoCtorMap() throws Exception
    {
        try {
            BrokenMap result = MAPPER.readValue("{ \"a\" : 3 }", BrokenMap.class);
            
            assertNull(result);
        } catch (JsonMappingException e) {
            
            verifyException(e, "no default constructor found");
        }
    }

// com.fasterxml.jackson.databind.deser.jdk.MapDeserializerCachingTest::testCachedSerialize
    public void testCachedSerialize() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        String json = aposToQuotes("{'data':{'1st':'onedata','2nd':'twodata'}}");

        
        NonAnnotatedMapHolderClass ignored = mapper.readValue(json, NonAnnotatedMapHolderClass.class);
        assertTrue(ignored.data.containsKey("1st"));
        assertTrue(ignored.data.containsKey("2nd"));

        
        MapHolder model2 = mapper.readValue(json, MapHolder.class);
        if (!model2.data.containsKey("1st (CUSTOM)")
            || !model2.data.containsKey("2nd (CUSTOM)")) {
            fail("Not using custom key deserializer for input: "+json+"; resulted in: "+model2.data);
        }
    }

// com.fasterxml.jackson.databind.deser.jdk.MapKeyDeserializationTest::testBooleanMapKeyDeserialization
    public void testBooleanMapKeyDeserialization() throws Exception
    {
        TypeReference<?> type = new TypeReference<MapWrapper<Boolean, String>>() { };
        MapWrapper<byte[], String> result = MAPPER.readValue(aposToQuotes("{'map':{'true':'foobar'}}"), type);
                
        assertEquals(1, result.map.size());
        Assert.assertEquals(Boolean.TRUE, result.map.entrySet().iterator().next().getKey());

        result = MAPPER.readValue(aposToQuotes("{'map':{'false':'foobar'}}"), type);
        assertEquals(1, result.map.size());
        Assert.assertEquals(Boolean.FALSE, result.map.entrySet().iterator().next().getKey());
    }

// com.fasterxml.jackson.databind.deser.jdk.MapKeyDeserializationTest::testByteMapKeyDeserialization
    public void testByteMapKeyDeserialization() throws Exception
    {
        TypeReference<?> type = new TypeReference<MapWrapper<Byte, String>>() { };
        MapWrapper<byte[], String> result = MAPPER.readValue(aposToQuotes("{'map':{'13':'foobar'}}"), type);
        assertEquals(1, result.map.size());
        Assert.assertEquals(Byte.valueOf((byte) 13), result.map.entrySet().iterator().next().getKey());
    }

// com.fasterxml.jackson.databind.deser.jdk.MapKeyDeserializationTest::testShortMapKeyDeserialization
    public void testShortMapKeyDeserialization() throws Exception
    {
        TypeReference<?> type = new TypeReference<MapWrapper<Short, String>>() { };
        MapWrapper<byte[], String> result = MAPPER.readValue(aposToQuotes("{'map':{'13':'foobar'}}"), type);
        assertEquals(1, result.map.size());
        Assert.assertEquals(Short.valueOf((short) 13), result.map.entrySet().iterator().next().getKey());
    }

// com.fasterxml.jackson.databind.deser.jdk.MapKeyDeserializationTest::testIntegerMapKeyDeserialization
    public void testIntegerMapKeyDeserialization() throws Exception
    {
        TypeReference<?> type = new TypeReference<MapWrapper<Integer, String>>() { };
        MapWrapper<byte[], String> result = MAPPER.readValue(aposToQuotes("{'map':{'-3':'foobar'}}"), type);
        assertEquals(1, result.map.size());
        Assert.assertEquals(Integer.valueOf(-3), result.map.entrySet().iterator().next().getKey());
    }

// com.fasterxml.jackson.databind.deser.jdk.MapKeyDeserializationTest::testLongMapKeyDeserialization
    public void testLongMapKeyDeserialization() throws Exception
    {
        TypeReference<?> type = new TypeReference<MapWrapper<Long, String>>() { };
        MapWrapper<byte[], String> result = MAPPER.readValue(aposToQuotes("{'map':{'42':'foobar'}}"), type);
        assertEquals(1, result.map.size());
        Assert.assertEquals(Long.valueOf(42), result.map.entrySet().iterator().next().getKey());
    }

// com.fasterxml.jackson.databind.deser.jdk.MapKeyDeserializationTest::testFloatMapKeyDeserialization
    public void testFloatMapKeyDeserialization() throws Exception
    {
        TypeReference<?> type = new TypeReference<MapWrapper<Float, String>>() { };
        MapWrapper<byte[], String> result = MAPPER.readValue(aposToQuotes("{'map':{'3.5':'foobar'}}"), type);
        assertEquals(1, result.map.size());
        Assert.assertEquals(Float.valueOf(3.5f), result.map.entrySet().iterator().next().getKey());
    }

// com.fasterxml.jackson.databind.deser.jdk.MapKeyDeserializationTest::testDoubleMapKeyDeserialization
    public void testDoubleMapKeyDeserialization() throws Exception
    {
        TypeReference<?> type = new TypeReference<MapWrapper<Double, String>>() { };
        MapWrapper<byte[], String> result = MAPPER.readValue(aposToQuotes("{'map':{'0.25':'foobar'}}"), type);
        assertEquals(1, result.map.size());
        Assert.assertEquals(Double.valueOf(0.25), result.map.entrySet().iterator().next().getKey());
    }

// com.fasterxml.jackson.databind.deser.jdk.MapKeyDeserializationTest::testDeserializeKeyViaFactory
    public void testDeserializeKeyViaFactory() throws Exception
    {
        Map<FullName, Double> map =
            MAPPER.readValue("{\"first.last\": 42}",
                    new TypeReference<Map<FullName, Double>>() { });
        Map.Entry<FullName, Double> entry = map.entrySet().iterator().next();
        FullName key = entry.getKey();
        assertEquals(key._firstname, "first");
        assertEquals(key._lastname, "last");
        assertEquals(entry.getValue().doubleValue(), 42, 0);
    }

// com.fasterxml.jackson.databind.deser.jdk.MapKeyDeserializationTest::testByteArrayMapKeyDeserialization
    public void testByteArrayMapKeyDeserialization() throws Exception
    {
        byte[] binary = new byte[] { 1, 2, 4, 8, 16, 33, 79 };
        String encoded = Base64Variants.MIME.encode(binary);

        MapWrapper<byte[], String> result = MAPPER.readValue(
                aposToQuotes("{'map':{'"+encoded+"':'foobar'}}"),
                new TypeReference<MapWrapper<byte[], String>>() { });
        assertEquals(1, result.map.size());
        Map.Entry<byte[],String> entry = result.map.entrySet().iterator().next();
        assertEquals("foobar", entry.getValue());
        byte[] key = entry.getKey();
        Assert.assertArrayEquals(binary, key);
    }

// com.fasterxml.jackson.databind.deser.jdk.MapRelatedTypesDeserTest::testMapEntrySimpleTypes
    public void testMapEntrySimpleTypes() throws Exception
    {
        List<Map.Entry<String,Long>> stuff = MAPPER.readValue(aposToQuotes("[{'a':15},{'b':42}]"),
                new TypeReference<List<Map.Entry<String,Long>>>() { });
        assertNotNull(stuff);
        assertEquals(2, stuff.size());
        assertNotNull(stuff.get(1));
        assertEquals("b", stuff.get(1).getKey());
        assertEquals(Long.valueOf(42), stuff.get(1).getValue());
    }

// com.fasterxml.jackson.databind.deser.jdk.MapRelatedTypesDeserTest::testMapEntryWithStringBean
    public void testMapEntryWithStringBean() throws Exception
    {
        List<Map.Entry<Integer,StringWrapper>> stuff = MAPPER.readValue(aposToQuotes("[{'28':'Foo'},{'13':'Bar'}]"),
                new TypeReference<List<Map.Entry<Integer,StringWrapper>>>() { });
        assertNotNull(stuff);
        assertEquals(2, stuff.size());
        assertNotNull(stuff.get(1));
        assertEquals(Integer.valueOf(13), stuff.get(1).getKey());
        
        StringWrapper sw = stuff.get(1).getValue();
        assertEquals("Bar", sw.str);
    }

// com.fasterxml.jackson.databind.deser.jdk.MapRelatedTypesDeserTest::testMapEntryFail
    public void testMapEntryFail() throws Exception
    {
        try {
             MAPPER.readValue(aposToQuotes("[{'28':'Foo','13':'Bar'}]"),
                    new TypeReference<List<Map.Entry<Integer,StringWrapper>>>() { });
            fail("Should not have passed");
        } catch (Exception e) {
            verifyException(e, "more than one entry in JSON");
        }
    }

// com.fasterxml.jackson.databind.deser.jdk.MapRelatedTypesDeserTest::testReadProperties
    public void testReadProperties() throws Exception
    {
        Properties props = MAPPER.readValue(aposToQuotes("{'a':'foo', 'b':123, 'c':true}"),
                Properties.class);
        assertEquals(3, props.size());
        assertEquals("foo", props.getProperty("a"));
        assertEquals("123", props.getProperty("b"));
        assertEquals("true", props.getProperty("c"));
    }

// com.fasterxml.jackson.databind.deser.jdk.MapRelatedTypesDeserTest::testSingletonMapRoundtrip
    public void testSingletonMapRoundtrip() throws Exception
    {
        final TypeReference<?> type = new TypeReference<Map<String,IntWrapper>>() { };

        String json = MAPPER.writeValueAsString(Collections.singletonMap("value", new IntWrapper(5)));
        Map<String,IntWrapper> result = MAPPER.readValue(json, type);
        assertNotNull(result);
        assertEquals(1, result.size());
        IntWrapper w = result.get("value");
        assertNotNull(w);
        assertEquals(5, w.i);
    }

// com.fasterxml.jackson.databind.deser.jdk.UntypedDeserializationTest::testSampleDoc
    public void testSampleDoc() throws Exception
    {
        final String JSON = SAMPLE_DOC_JSON_SPEC;

        
        Object root = MAPPER.readValue(JSON, Object.class);

        assertType(root, Map.class);
        Map<?,?> rootMap = (Map<?,?>) root;
        assertEquals(1, rootMap.size());
        Map.Entry<?,?> rootEntry =  rootMap.entrySet().iterator().next();
        assertEquals("Image", rootEntry.getKey());
        Object image = rootEntry.getValue();
        assertType(image, Map.class);
        Map<?,?> imageMap = (Map<?,?>) image;
        assertEquals(5, imageMap.size());

        Object value = imageMap.get("Width");
        assertType(value, Integer.class);
        assertEquals(Integer.valueOf(SAMPLE_SPEC_VALUE_WIDTH), value);

        value = imageMap.get("Height");
        assertType(value, Integer.class);
        assertEquals(Integer.valueOf(SAMPLE_SPEC_VALUE_HEIGHT), value);

        assertEquals(SAMPLE_SPEC_VALUE_TITLE, imageMap.get("Title"));

        
        value = imageMap.get("Thumbnail");
        assertType(value, Map.class);
        Map<?,?> tnMap = (Map<?,?>) value;
        assertEquals(3, tnMap.size());

        assertEquals(Integer.valueOf(SAMPLE_SPEC_VALUE_TN_HEIGHT), tnMap.get("Height"));
        
        assertEquals(SAMPLE_SPEC_VALUE_TN_WIDTH, tnMap.get("Width"));
        assertEquals(SAMPLE_SPEC_VALUE_TN_URL, tnMap.get("Url"));

        
        value = imageMap.get("IDs");
        assertType(value, List.class);
        List<Object> ids = (List<Object>) value;
        assertEquals(4, ids.size());
        assertEquals(Integer.valueOf(SAMPLE_SPEC_VALUE_TN_ID1), ids.get(0));
        assertEquals(Integer.valueOf(SAMPLE_SPEC_VALUE_TN_ID2), ids.get(1));
        assertEquals(Integer.valueOf(SAMPLE_SPEC_VALUE_TN_ID3), ids.get(2));
        assertEquals(Integer.valueOf(SAMPLE_SPEC_VALUE_TN_ID4), ids.get(3));

        
    }

// com.fasterxml.jackson.databind.deser.jdk.UntypedDeserializationTest::testUntypedMap
    public void testUntypedMap() throws Exception
    {
        
        String JSON = "{ \"foo\" : \"bar\", \"crazy\" : true, \"null\" : null }";

        
        @SuppressWarnings("unchecked")
        Map<String,Object> result = (Map<String,Object>)MAPPER.readValue(JSON, Object.class);
        assertNotNull(result);
        assertTrue(result instanceof Map<?,?>);

        assertEquals(3, result.size());

        assertEquals("bar", result.get("foo"));
        assertEquals(Boolean.TRUE, result.get("crazy"));
        assertNull(result.get("null"));

        
        assertNull(result.get("bar"));
        assertNull(result.get(3));
    }

// com.fasterxml.jackson.databind.deser.jdk.UntypedDeserializationTest::testNestedUntypes
    public void testNestedUntypes() throws IOException
    {
        
        Object root = MAPPER.readValue(aposToQuotes("{'a':3,'b':[1,2]}"),
                Object.class);
        assertTrue(root instanceof Map<?,?>);
        Map<?,?> map = (Map<?,?>) root;
        assertEquals(2, map.size());
        assertEquals(Integer.valueOf(3), map.get("a"));
        Object ob = map.get("b");
        assertTrue(ob instanceof List<?>);
        List<?> l = (List<?>) ob;
        assertEquals(2, l.size());
        assertEquals(Integer.valueOf(2), l.get(1));
    }

// com.fasterxml.jackson.databind.deser.jdk.UntypedDeserializationTest::testObjectSerializeWithLong
    public void testObjectSerializeWithLong() throws IOException
    {
        final ObjectMapper mapper = new ObjectMapper();
        mapper.enableDefaultTyping(DefaultTyping.JAVA_LANG_OBJECT, As.PROPERTY);
        final long VALUE = 1337800584532L;

        String serialized = "{\"timestamp\":"+VALUE+"}";
        
        JsonNode deserialized = mapper.readTree(serialized);
        assertEquals(VALUE, deserialized.get("timestamp").asLong());
        
        Map<?,?> deserMap = mapper.readValue(serialized, Map.class);
        Number n = (Number) deserMap.get("timestamp");
        assertNotNull(n);
        assertSame(Long.class, n.getClass());
        assertEquals(Long.valueOf(VALUE), n);
    }

// com.fasterxml.jackson.databind.deser.jdk.UntypedDeserializationTest::testUntypedWithCustomScalarDesers
    public void testUntypedWithCustomScalarDesers() throws IOException
    {
        SimpleModule m = new SimpleModule("test-module");
        m.addDeserializer(String.class, new UCStringDeserializer());
        m.addDeserializer(Number.class, new CustomNumberDeserializer(13));
        final ObjectMapper mapper = new ObjectMapper()
            .registerModule(m);

        Object ob = mapper.readValue("{\"a\":\"b\", \"nr\":1 }", Object.class);
        assertTrue(ob instanceof Map);
        Object value = ((Map<?,?>) ob).get("a");
        assertNotNull(value);
        assertTrue(value instanceof String);
        assertEquals("B", value);

        value = ((Map<?,?>) ob).get("nr");
        assertNotNull(value);
        assertTrue(value instanceof Number);
        assertEquals(Integer.valueOf(13), value);
    }

// com.fasterxml.jackson.databind.deser.jdk.UntypedDeserializationTest::testNonVanilla
    public void testNonVanilla() throws IOException
    {
        SimpleModule m = new SimpleModule("test-module");
        m.addDeserializer(String.class, new UCStringDeserializer());
        final ObjectMapper mapper = new ObjectMapper()
            .registerModule(m);

        
        List<?> l = (List<?>) mapper.readValue("[ true, false, 7, 0.5, \"foo\"]", Object.class);
        assertEquals(5, l.size());
        assertEquals(Boolean.TRUE, l.get(0));
        assertEquals(Boolean.FALSE, l.get(1));
        assertEquals(Integer.valueOf(7), l.get(2));
        assertEquals(Double.valueOf(0.5), l.get(3));
        assertEquals("FOO", l.get(4));

        l = (List<?>) mapper.readValue("[ {}, [] ]", Object.class);
        assertEquals(2, l.size());
        assertTrue(l.get(0) instanceof Map<?,?>);
        assertTrue(l.get(1) instanceof List<?>);

        ObjectReader rDefault = mapper.readerFor(WrappedPolymorphicUntyped.class);
        ObjectReader rAlt = rDefault
                .with(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS,
                        DeserializationFeature.USE_BIG_INTEGER_FOR_INTS);
        WrappedPolymorphicUntyped w;

        w = rDefault.readValue(aposToQuotes("{'value':10}"));
        assertEquals(Integer.valueOf(10), w.value);
        w = rAlt.readValue(aposToQuotes("{'value':10}"));
        assertEquals(BigInteger.TEN, w.value);

        w = rDefault.readValue(aposToQuotes("{'value':5.0}"));
        assertEquals(Double.valueOf(5.0), w.value);
        w = rAlt.readValue(aposToQuotes("{'value':5.0}"));
        assertEquals(new BigDecimal("5.0"), w.value);

        StringBuilder sb = new StringBuilder(100).append("[0");
        for (int i = 1; i < 100; ++i) {
            sb.append(", ").append(i);
        }
        sb.append("]");
        final String INT_ARRAY_JSON = sb.toString();

        
        Object ob = mapper.readerFor(Object.class)
                .with(DeserializationFeature.USE_JAVA_ARRAY_FOR_JSON_ARRAY)
                .readValue(INT_ARRAY_JSON);
        assertTrue(ob instanceof Object[]);
        Object[] obs = (Object[]) ob;
        for (int i = 0; i < 100; ++i) {
            assertEquals(Integer.valueOf(i), obs[i]);
        }
    }

// com.fasterxml.jackson.databind.deser.jdk.UntypedDeserializationTest::testUntypedWithListDeser
    public void testUntypedWithListDeser() throws IOException
    {
        SimpleModule m = new SimpleModule("test-module");
        m.addDeserializer(List.class, new ListDeserializer());
        final ObjectMapper mapper = new ObjectMapper()
            .registerModule(m);

        
        Object ob = mapper.readValue("[1, 2, true]", Object.class);
        assertTrue(ob instanceof List<?>);
        List<?> l = (List<?>) ob;
        assertEquals(3, l.size());
        assertEquals("X1", l.get(0));
        assertEquals("X2", l.get(1));
        assertEquals("Xtrue", l.get(2));
    }

// com.fasterxml.jackson.databind.deser.jdk.UntypedDeserializationTest::testUntypedWithMapDeser
    public void testUntypedWithMapDeser() throws IOException
    {
        SimpleModule m = new SimpleModule("test-module");
        m.addDeserializer(Map.class, new YMapDeserializer());
        final ObjectMapper mapper = new ObjectMapper()
            .registerModule(m);

        
        Object ob = mapper.readValue("{\"a\":true}", Object.class);
        assertTrue(ob instanceof Map<?,?>);
        Map<?,?> map = (Map<?,?>) ob;
        assertEquals(1, map.size());
        assertEquals("Ytrue", map.get("a"));
    }

// com.fasterxml.jackson.databind.deser.jdk.UntypedDeserializationTest::testNestedUntyped989
    public void testNestedUntyped989() throws IOException
    {
        DelegatingUntyped pojo;
        ObjectReader r = MAPPER.readerFor(DelegatingUntyped.class);

        pojo = r.readValue("[]");
        assertTrue(pojo.value instanceof List);
        pojo = r.readValue("[{}]");
        assertTrue(pojo.value instanceof List);
        
        pojo = r.readValue("{}");
        assertTrue(pojo.value instanceof Map);
        pojo = r.readValue("{\"a\":[]}");
        assertTrue(pojo.value instanceof Map);
    }

// com.fasterxml.jackson.databind.deser.jdk.UntypedDeserializationTest::testUntypedWithJsonArrays
    public void testUntypedWithJsonArrays() throws Exception
    {
        
        Object ob = MAPPER.readValue("[1]", Object.class);
        assertTrue(ob instanceof List<?>);

        
        MAPPER.configure(DeserializationFeature.USE_JAVA_ARRAY_FOR_JSON_ARRAY, true);
        ob = MAPPER.readValue("[1]", Object.class);
        assertEquals(Object[].class, ob.getClass());
    }

// com.fasterxml.jackson.databind.deser.jdk.UntypedDeserializationTest::testUntypedIntAsLong
    public void testUntypedIntAsLong() throws Exception
    {
        final String JSON = aposToQuotes("{'value':3}");
        WrappedUntyped1460 w = MAPPER.readerFor(WrappedUntyped1460.class)
                .readValue(JSON);
        assertEquals(Integer.valueOf(3), w.value);

        w = MAPPER.readerFor(WrappedUntyped1460.class)
                .with(DeserializationFeature.USE_LONG_FOR_INTS)
                .readValue(JSON);
        assertEquals(Long.valueOf(3), w.value);
    }

// com.fasterxml.jackson.databind.deser.merge.ArrayMergeTest::testObjectArrayMerging
    public void testObjectArrayMerging() throws Exception
    {
        MergedX<Object[]> input = new MergedX<Object[]>(new Object[] {
                "foo"
        });
        final JavaType type = MAPPER.getTypeFactory().constructType(new TypeReference<MergedX<Object[]>>() {});
        MergedX<Object[]> result = MAPPER.readerFor(type)
                .withValueToUpdate(input)
                .readValue(aposToQuotes("{'value':['bar']}"));
        assertSame(input, result);
        assertEquals(2, result.value.length);
        assertEquals("foo", result.value[0]);
        assertEquals("bar", result.value[1]);

        
        result = MAPPER.readerFor(type)
                .with(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
                .withValueToUpdate(input)
                .readValue(aposToQuotes("{'value':'zap'}"));
        assertSame(input, result);
        assertEquals(3, result.value.length);
        assertEquals("foo", result.value[0]);
        assertEquals("bar", result.value[1]);
        assertEquals("zap", result.value[2]);
    }

// com.fasterxml.jackson.databind.deser.merge.ArrayMergeTest::testStringArrayMerging
    public void testStringArrayMerging() throws Exception
    {
        MergedX<String[]> input = new MergedX<String[]>(new String[] { "foo" });
        MergedX<String[]> result = MAPPER
                .readerFor(new TypeReference<MergedX<String[]>>() {})
                .withValueToUpdate(input)
                .readValue(aposToQuotes("{'value':['bar']}"));
        assertSame(input, result);
        assertEquals(2, result.value.length);
        assertEquals("foo", result.value[0]);
        assertEquals("bar", result.value[1]);
    }

// com.fasterxml.jackson.databind.deser.merge.ArrayMergeTest::testBooleanArrayMerging
    public void testBooleanArrayMerging() throws Exception
    {
        MergedX<boolean[]> input = new MergedX<boolean[]>(new boolean[] { true, false });
        MergedX<boolean[]> result = MAPPER
                .readerFor(new TypeReference<MergedX<boolean[]>>() {})
                .withValueToUpdate(input)
                .readValue(aposToQuotes("{'value':[true]}"));
        assertSame(input, result);
        assertEquals(3, result.value.length);
        Assert.assertArrayEquals(new boolean[] { true, false, true }, result.value);
    }

// com.fasterxml.jackson.databind.deser.merge.ArrayMergeTest::testByteArrayMerging
    public void testByteArrayMerging() throws Exception
    {
        MergedX<byte[]> input = new MergedX<byte[]>(new byte[] { 1, 2 });
        MergedX<byte[]> result = MAPPER
                .readerFor(new TypeReference<MergedX<byte[]>>() {})
                .withValueToUpdate(input)
                .readValue(aposToQuotes("{'value':[4, 6.0, null]}"));
        assertSame(input, result);
        assertEquals(5, result.value.length);
        Assert.assertArrayEquals(new byte[] { 1, 2, 4, 6, 0 }, result.value);
    }

// com.fasterxml.jackson.databind.deser.merge.ArrayMergeTest::testShortArrayMerging
    public void testShortArrayMerging() throws Exception
    {
        MergedX<short[]> input = new MergedX<short[]>(new short[] { 1, 2 });
        MergedX<short[]> result = MAPPER
                .readerFor(new TypeReference<MergedX<short[]>>() {})
                .withValueToUpdate(input)
                .readValue(aposToQuotes("{'value':[4, 6]}"));
        assertSame(input, result);
        assertEquals(4, result.value.length);
        Assert.assertArrayEquals(new short[] { 1, 2, 4, 6 }, result.value);
    }

// com.fasterxml.jackson.databind.deser.merge.ArrayMergeTest::testCharArrayMerging
    public void testCharArrayMerging() throws Exception
    {
        MergedX<char[]> input = new MergedX<char[]>(new char[] { 'a', 'b' });
        MergedX<char[]> result = MAPPER
                .readerFor(new TypeReference<MergedX<char[]>>() {})
                .withValueToUpdate(input)
                .readValue(aposToQuotes("{'value':['c']}"));
        assertSame(input, result);
        Assert.assertArrayEquals(new char[] { 'a', 'b', 'c' }, result.value);

        
        input = new MergedX<char[]>(new char[] { });
        result = MAPPER
                .readerFor(new TypeReference<MergedX<char[]>>() {})
                .withValueToUpdate(input)
                .readValue(aposToQuotes("{'value':['c']}"));
        assertSame(input, result);
        Assert.assertArrayEquals(new char[] { 'c' }, result.value);
    }

// com.fasterxml.jackson.databind.deser.merge.ArrayMergeTest::testIntArrayMerging
    public void testIntArrayMerging() throws Exception
    {
        MergedX<int[]> input = new MergedX<int[]>(new int[] { 1, 2 });
        MergedX<int[]> result = MAPPER
                .readerFor(new TypeReference<MergedX<int[]>>() {})
                .withValueToUpdate(input)
                .readValue(aposToQuotes("{'value':[4, 6]}"));
        assertSame(input, result);
        assertEquals(4, result.value.length);
        Assert.assertArrayEquals(new int[] { 1, 2, 4, 6 }, result.value);

        
        input = new MergedX<int[]>(new int[] { 3, 4, 6 });
        result = MAPPER
                .readerFor(new TypeReference<MergedX<int[]>>() {})
                .withValueToUpdate(input)
                .readValue(aposToQuotes("{'value':[ ]}"));
        assertSame(input, result);
        Assert.assertArrayEquals(new int[] { 3, 4, 6 }, result.value);
    }

// com.fasterxml.jackson.databind.deser.merge.ArrayMergeTest::testLongArrayMerging
    public void testLongArrayMerging() throws Exception
    {
        MergedX<long[]> input = new MergedX<long[]>(new long[] { 1, 2 });
        MergedX<long[]> result = MAPPER
                .readerFor(new TypeReference<MergedX<long[]>>() {})
                .withValueToUpdate(input)
                .readValue(aposToQuotes("{'value':[4, 6]}"));
        assertSame(input, result);
        assertEquals(4, result.value.length);
        Assert.assertArrayEquals(new long[] { 1, 2, 4, 6 }, result.value);
    }

// com.fasterxml.jackson.databind.deser.merge.CollectionMergeTest::testCollectionMerging
    public void testCollectionMerging() throws Exception
    {
        CollectionWrapper w = MAPPER.readValue(aposToQuotes("{'bag':['b']}"), CollectionWrapper.class);
        assertEquals(2, w.bag.size());
        assertTrue(w.bag.contains("a"));
        assertTrue(w.bag.contains("b"));
    }

// com.fasterxml.jackson.databind.deser.merge.CollectionMergeTest::testListMerging
    public void testListMerging() throws Exception
    {
        MergedList w = MAPPER.readValue(aposToQuotes("{'values':['x']}"), MergedList.class);
        assertEquals(2, w.values.size());
        assertTrue(w.values.contains("a"));
        assertTrue(w.values.contains("x"));
    }

// com.fasterxml.jackson.databind.deser.merge.CollectionMergeTest::testGenericListMerging
    public void testGenericListMerging() throws Exception
    {
        Collection<String> l = new ArrayList<>();
        l.add("foo");
        MergedX<Collection<String>> input = new MergedX<Collection<String>>(l);

        MergedX<Collection<String>> result = MAPPER
                .readerFor(new TypeReference<MergedX<Collection<String>>>() {})
                .withValueToUpdate(input)
                .readValue(aposToQuotes("{'value':['bar']}"));
        assertSame(input, result);
        assertEquals(2, result.value.size());
        Iterator<String> it = result.value.iterator();
        assertEquals("foo", it.next());
        assertEquals("bar", it.next());
    }

// com.fasterxml.jackson.databind.deser.merge.CollectionMergeTest::testEnumSetMerging
    public void testEnumSetMerging() throws Exception
    {
        MergedEnumSet result = MAPPER.readValue(aposToQuotes("{'abc':['A']}"), MergedEnumSet.class);
        assertEquals(2, result.abc.size());
        assertTrue(result.abc.contains(ABC.B)); 
        assertTrue(result.abc.contains(ABC.A)); 
    }

// com.fasterxml.jackson.databind.deser.merge.MapMerge1844Test::testMap1844
    public void testMap1844() throws Exception
    {
        final ObjectMapper mapper = newObjectMapper();
        mapper.setDefaultMergeable(true);

        final String f1 = aposToQuotes(
"{ 'key1' : {\n"
+"  '1': 1, '2': 2, '3': 3\n"
+"}, 'key2': {\n"
+"  '1': 1, '2': 2, '3': 3\n"
+"} }"
);
        final String f2 = aposToQuotes(
"{ 'key1' : {\n"
+"  '1': 2, '2': 3, '4': 5\n"
+"}, 'key2': {\n"
+"  '1': 2, '2': 3, '4': 5\n"
+"} }"
);
        TestMap1844 testMap = mapper.readerFor(TestMap1844.class).readValue(f1);
        testMap = mapper.readerForUpdating(testMap).readValue(f2);

        assertEquals(Integer.valueOf(2), testMap.getMapStringInteger().get("1"));
        assertEquals(Integer.valueOf(3), testMap.getMapStringInteger().get("2"));
        assertEquals(Integer.valueOf(3), testMap.getMapStringInteger().get("3"));
        assertEquals(Integer.valueOf(5), testMap.getMapStringInteger().get("4"));

        assertEquals(Integer.valueOf(2), testMap.getMapIntegerInteger().get(1));
        assertEquals(Integer.valueOf(3), testMap.getMapIntegerInteger().get(2));
        assertEquals(Integer.valueOf(3), testMap.getMapIntegerInteger().get(3));
        assertEquals(Integer.valueOf(5), testMap.getMapIntegerInteger().get(4));
    }

// com.fasterxml.jackson.databind.deser.merge.MapMergeTest::testShallowMapMerging
    public void testShallowMapMerging() throws Exception
    {
        final String JSON = aposToQuotes("{'values':{'c':'y','d':null}}");
        MergedMap v = MAPPER.readValue(JSON, MergedMap.class);
        assertEquals(3, v.values.size());
        assertEquals("y", v.values.get("c"));
        assertEquals("x", v.values.get("a"));
        assertNull(v.values.get("d"));

        
        v = MAPPER_SKIP_NULLS.readValue(JSON, MergedMap.class);
        assertEquals(2, v.values.size());
        assertEquals("y", v.values.get("c"));
        assertEquals("x", v.values.get("a"));
    }

// com.fasterxml.jackson.databind.deser.merge.MapMergeTest::testShallowNonStringMerging
    public void testShallowNonStringMerging() throws Exception
    {
        final String JSON = aposToQuotes("{'values':{'72':'b','666':null}}");
        MergedIntMap v = MAPPER.readValue(JSON , MergedIntMap.class);
        assertEquals(3, v.values.size());
        assertEquals("a", v.values.get(Integer.valueOf(13)));
        assertEquals("b", v.values.get(Integer.valueOf(72)));
        assertNull(v.values.get(Integer.valueOf(666)));

        v = MAPPER_SKIP_NULLS.readValue(JSON , MergedIntMap.class);
        assertEquals(2, v.values.size());
        assertEquals("a", v.values.get(Integer.valueOf(13)));
        assertEquals("b", v.values.get(Integer.valueOf(72)));
    }

// com.fasterxml.jackson.databind.deser.merge.MapMergeTest::testDeeperMapMerging
    public void testDeeperMapMerging() throws Exception
    {
        
        MergedMap base = new MergedMap("name", "foobar");
        Map<String,Object> props = new LinkedHashMap<>();
        props.put("default", "yes");
        props.put("x", "abc");
        Map<String,Object> innerProps = new LinkedHashMap<>();
        innerProps.put("z", Integer.valueOf(13));
        props.put("extra", innerProps);
        base.values.put("props", props);

        
        MergedMap v = MAPPER.readerForUpdating(base)
                .readValue(aposToQuotes("{'values':{'props':{'x':'xyz','y' : '...','extra':{ 'ab' : true}}}}"));
        assertEquals(2, v.values.size());
        assertEquals("foobar", v.values.get("name"));
        assertNotNull(v.values.get("props"));
        props = (Map<String,Object>) v.values.get("props");
        assertEquals(4, props.size());
        assertEquals("yes", props.get("default"));
        assertEquals("xyz", props.get("x"));
        assertEquals("...", props.get("y"));
        assertNotNull(props.get("extra"));
        innerProps = (Map<String,Object>) props.get("extra");
        assertEquals(2, innerProps.size());
        assertEquals(Integer.valueOf(13), innerProps.get("z"));
        assertEquals(Boolean.TRUE, innerProps.get("ab"));
    }

// com.fasterxml.jackson.databind.deser.merge.MapMergeTest::testMapMergingWithArray
    public void testMapMergingWithArray() throws Exception
    {
        
        MergedMap base = new MergedMap("name", "foobar");
        Map<String,Object> props = new LinkedHashMap<>();
        List<String> names = new ArrayList<>();
        names.add("foo");
        props.put("names", names);
        base.values.put("props", props);
        props.put("extra", "misc");

        
        MergedMap v = MAPPER.readerForUpdating(base)
                .readValue(aposToQuotes("{'values':{'props':{'names': [ 'bar' ] }}}"));
        assertEquals(2, v.values.size());
        assertEquals("foobar", v.values.get("name"));
        assertNotNull(v.values.get("props"));
        props = (Map<String,Object>) v.values.get("props");
        assertEquals(2, props.size());
        assertEquals("misc", props.get("extra"));
        assertNotNull(props.get("names"));
        names = (List<String>) props.get("names");
        assertEquals(2, names.size());
        assertEquals("foo", names.get(0));
        assertEquals("bar", names.get(1));
    }

// com.fasterxml.jackson.databind.deser.merge.MapMergeTest::testDefaultDeepMapMerge
    public void testDefaultDeepMapMerge() throws Exception
    {
        
        HashMap<String,Object> input = new HashMap<>();
        input.put("list", new ArrayList<>(Arrays.asList("a")));

        Map<?,?> resultMap = MAPPER.readerForUpdating(input)
                .readValue(aposToQuotes("{'list':['b']}"));

        List<?> resultList = (List<?>) resultMap.get("list");
        assertEquals(Arrays.asList("a", "b"), resultList);
    }

// com.fasterxml.jackson.databind.deser.merge.MapMergeTest::testDisabledMergeViaGlobal
    public void testDisabledMergeViaGlobal() throws Exception
    {
        ObjectMapper mapper = newObjectMapper();
        
        mapper.setDefaultMergeable(false);

        HashMap<String,Object> input = new HashMap<>();
        input.put("list", new ArrayList<>(Arrays.asList("a")));

        Map<?,?> resultMap = mapper.readerForUpdating(input)
                .readValue(aposToQuotes("{'list':['b']}"));

        List<?> resultList = (List<?>) resultMap.get("list");

        assertEquals(Arrays.asList("b"), resultList);
    }

// com.fasterxml.jackson.databind.deser.merge.MapMergeTest::testDisabledMergeByType
    public void testDisabledMergeByType() throws Exception
    {
        ObjectMapper mapper = newObjectMapper();
        
        mapper.configOverride(Object.class)
            .setMergeable(false);

        HashMap<String,Object> input = new HashMap<>();
        input.put("list", new ArrayList<>(Arrays.asList("a")));

        Map<?,?> resultMap = mapper.readerForUpdating(input)
                .readValue(aposToQuotes("{'list':['b']}"));
        List<?> resultList = (List<?>) resultMap.get("list");
        assertEquals(Arrays.asList("b"), resultList);

        
        

        mapper = newObjectMapper();
        mapper.setDefaultMergeable(false);
        mapper.configOverride(Object.class)
            .setMergeable(true);

        input = new HashMap<>();
        input.put("list", new ArrayList<>(Arrays.asList("x")));

        resultMap = mapper.readerForUpdating(input)
                .readValue(aposToQuotes("{'list':['y']}"));
        resultList = (List<?>) resultMap.get("list");
        assertEquals(Arrays.asList("x", "y"), resultList);
    }

// com.fasterxml.jackson.databind.deser.merge.MergeWithNullTest::testBeanMergingWithNullDefault
    public void testBeanMergingWithNullDefault() throws Exception
    {
        
        ConfigDefault config = MAPPER.readerForUpdating(new ConfigDefault(5, 7))
                .readValue(aposToQuotes("{'loc':null}"));
        assertNotNull(config);
        assertNull(config.loc);

        

        
        
        ObjectMapper mapper = newObjectMapper();
        mapper.configOverride(AB.class)
            .setSetterInfo(JsonSetter.Value.forValueNulls(Nulls.SKIP));
        config = mapper.readerForUpdating(new ConfigDefault(137, -3))
                .readValue(aposToQuotes("{'loc':null}"));
        assertNotNull(config.loc);
        assertEquals(137, config.loc.a);
        assertEquals(-3, config.loc.b);

        
        mapper = newObjectMapper();
        mapper.setDefaultSetterInfo(JsonSetter.Value.forValueNulls(Nulls.SKIP));
        config = mapper.readerForUpdating(new ConfigDefault(12, 34))
                .readValue(aposToQuotes("{'loc':null}"));
        assertNotNull(config.loc);
        assertEquals(12, config.loc.a);
        assertEquals(34, config.loc.b);
    }

// com.fasterxml.jackson.databind.deser.merge.MergeWithNullTest::testBeanMergingWithNullSkip
    public void testBeanMergingWithNullSkip() throws Exception
    {
        ConfigSkipNull config = MAPPER.readerForUpdating(new ConfigSkipNull(5, 7))
                .readValue(aposToQuotes("{'loc':null}"));
        assertNotNull(config);
        assertNotNull(config.loc);
        assertEquals(5, config.loc.a);
        assertEquals(7, config.loc.b);
    }

// com.fasterxml.jackson.databind.deser.merge.MergeWithNullTest::testBeanMergingWithNullSet
    public void testBeanMergingWithNullSet() throws Exception
    {
        ConfigAllowNullOverwrite config = MAPPER.readerForUpdating(new ConfigAllowNullOverwrite(5, 7))
                .readValue(aposToQuotes("{'loc':null}"));
        assertNotNull(config);
        assertNull(config.loc);
    }

// com.fasterxml.jackson.databind.deser.merge.MergeWithNullTest::testSetterlessMergingWithNull
    public void testSetterlessMergingWithNull() throws Exception
    {
        NoSetterConfig input = new NoSetterConfig();
        NoSetterConfig result = MAPPER.readerForUpdating(input)
                .readValue(aposToQuotes("{'value':null}"));
        assertNotNull(result.getValue());
        assertEquals(2, result.getValue().a);
        assertEquals(3, result.getValue().b);
        assertSame(input, result);
    }

// com.fasterxml.jackson.databind.deser.merge.NodeMergeTest::testObjectNodeUpdateValue
    public void testObjectNodeUpdateValue() throws Exception
    {
        ObjectNode base = MAPPER.createObjectNode();
        base.put("first", "foo");
        assertSame(base,
                MAPPER.readerForUpdating(base)
                .readValue(aposToQuotes("{'second':'bar', 'third':5, 'fourth':true}")));
        assertEquals(4, base.size());
        assertEquals("bar", base.path("second").asText());
        assertEquals("foo", base.path("first").asText());
        assertEquals(5, base.path("third").asInt());
        assertTrue(base.path("fourth").asBoolean());
    }

// com.fasterxml.jackson.databind.deser.merge.NodeMergeTest::testObjectNodeMerge
    public void testObjectNodeMerge() throws Exception
    {
        ObjectNodeWrapper w = MAPPER.readValue(aposToQuotes("{'props':{'stuff':'xyz'}}"),
                ObjectNodeWrapper.class);
        assertEquals(2, w.props.size());
        assertEquals("enabled", w.props.path("default").asText());
        assertEquals("xyz", w.props.path("stuff").asText());
    }

// com.fasterxml.jackson.databind.deser.merge.NodeMergeTest::testObjectDeepUpdate
    public void testObjectDeepUpdate() throws Exception
    {
        ObjectNode base = MAPPER.createObjectNode();
        ObjectNode props = base.putObject("props");
        props.put("base", 123);
        props.put("value", 456);
        ArrayNode a = props.putArray("array");
        a.add(true);
        base.putNull("misc");
        assertSame(base,
                MAPPER.readerForUpdating(base)
                .readValue(aposToQuotes(
                        "{'props':{'value':true, 'extra':25.5, 'array' : [ 3 ]}}")));
        assertEquals(2, base.size());
        ObjectNode resultProps = (ObjectNode) base.get("props");
        assertEquals(4, resultProps.size());
        
        assertEquals(123, resultProps.path("base").asInt());
        assertTrue(resultProps.path("value").asBoolean());
        assertEquals(25.5, resultProps.path("extra").asDouble());
        JsonNode n = resultProps.get("array");
        assertEquals(ArrayNode.class, n.getClass());
        assertEquals(2, n.size());
        assertEquals(3, n.get(1).asInt());
    }

// com.fasterxml.jackson.databind.deser.merge.NodeMergeTest::testArrayNodeUpdateValue
    public void testArrayNodeUpdateValue() throws Exception
    {
        ArrayNode base = MAPPER.createArrayNode();
        base.add("first");
        assertSame(base,
                MAPPER.readerForUpdating(base)
                .readValue(aposToQuotes("['second',false,null]")));
        assertEquals(4, base.size());
        assertEquals("first", base.path(0).asText());
        assertEquals("second", base.path(1).asText());
        assertFalse(base.path(2).asBoolean());
        assertTrue(base.path(3).isNull());
    }

// com.fasterxml.jackson.databind.deser.merge.NodeMergeTest::testArrayNodeMerge
    public void testArrayNodeMerge() throws Exception
    {
        ArrayNodeWrapper w = MAPPER.readValue(aposToQuotes("{'list':[456,true,{},  [], 'foo']}"),
                ArrayNodeWrapper.class);
        assertEquals(6, w.list.size());
        assertEquals(123, w.list.get(0).asInt());
        assertEquals(456, w.list.get(1).asInt());
        assertTrue(w.list.get(2).asBoolean());
        JsonNode n = w.list.get(3);
        assertTrue(n.isObject());
        assertEquals(0, n.size());
        n = w.list.get(4);
        assertTrue(n.isArray());
        assertEquals(0, n.size());
        assertEquals("foo", w.list.get(5).asText());
    }

// com.fasterxml.jackson.databind.deser.merge.PropertyMergeTest::testBeanMergingViaProp
    public void testBeanMergingViaProp() throws Exception
    {
        Config config = MAPPER.readValue(aposToQuotes("{'loc':{'b':3}}"), Config.class);
        assertEquals(1, config.loc.a);
        assertEquals(3, config.loc.b);

        config = MAPPER.readerForUpdating(new Config(5, 7))
                .readValue(aposToQuotes("{'loc':{'b':2}}"));
        assertEquals(5, config.loc.a);
        assertEquals(2, config.loc.b);
    }

// com.fasterxml.jackson.databind.deser.merge.PropertyMergeTest::testBeanMergingViaType
    public void testBeanMergingViaType() throws Exception
    {
        
        NonMergeConfig config = MAPPER.readValue(aposToQuotes("{'loc':{'a':3}}"), NonMergeConfig.class);
        assertEquals(3, config.loc.a);
        assertEquals(0, config.loc.b); 

        
        ObjectMapper mapper = newObjectMapper();
        mapper.configOverride(AB.class).setMergeable(true);
        config = mapper.readValue(aposToQuotes("{'loc':{'a':3}}"), NonMergeConfig.class);
        assertEquals(3, config.loc.a);
        assertEquals(2, config.loc.b); 
    }

// com.fasterxml.jackson.databind.deser.merge.PropertyMergeTest::testBeanMergingViaGlobal
    public void testBeanMergingViaGlobal() throws Exception
    {
        
        ObjectMapper mapper = newObjectMapper()
                .setDefaultMergeable(true);
        NonMergeConfig config = mapper.readValue(aposToQuotes("{'loc':{'a':3}}"), NonMergeConfig.class);
        assertEquals(3, config.loc.a);
        assertEquals(2, config.loc.b); 

        
        FiveMinuteUser user0 = new FiveMinuteUser("Bob", "Bush", true, FiveMinuteUser.Gender.MALE,
                new byte[] { 1, 2, 3, 4, 5 });
        FiveMinuteUser user = mapper.readerFor(FiveMinuteUser.class)
                .withValueToUpdate(user0)
                .readValue(aposToQuotes("{'name':{'last':'Brown'}}"));
        assertEquals("Bob", user.getName().getFirst());
        assertEquals("Brown", user.getName().getLast());
    }

// com.fasterxml.jackson.databind.deser.merge.PropertyMergeTest::testBeanMergingWithoutSetter
    public void testBeanMergingWithoutSetter() throws Exception
    {
        NoSetterConfig config = MAPPER.readValue(aposToQuotes("{'value':{'b':99}}"),
                NoSetterConfig.class);
        assertEquals(99, config._value.b);
        assertEquals(1, config._value.a);
    }

// com.fasterxml.jackson.databind.deser.merge.PropertyMergeTest::testBeanAsArrayMerging
    public void testBeanAsArrayMerging() throws Exception
    {
        ABAsArray input = new ABAsArray();
        input.a = 4;
        input.b = 6;

        assertSame(input, MAPPER.readerForUpdating(input)
                .readValue("[1, 3]"));
        assertEquals(1, input.a);
        assertEquals(3, input.b);

        
        assertSame(input, MAPPER.readerForUpdating(input)
                .readValue("[9]"));
        assertEquals(9, input.a);
        assertEquals(3, input.b);

        
        try {
            MAPPER.readerForUpdating(input)
                .readValue("[9, 8, 14]");
            fail("Should not pass");
        } catch (MismatchedInputException e) {
            verifyException(e, "expected at most 2 properties");
        }

        try {
            MAPPER.readerForUpdating(input)
                .readValue("\"blob\"");
            fail("Should not pass");
        } catch (MismatchedInputException e) {
            verifyException(e, "Cannot deserialize");
            verifyException(e, "from non-Array representation");
        }
    }

// com.fasterxml.jackson.databind.deser.merge.PropertyMergeTest::testReferenceMerging
    public void testReferenceMerging() throws Exception
    {
        MergedReference result = MAPPER.readValue(aposToQuotes("{'value':'override'}"),
                MergedReference.class);
        assertEquals("override", result.value.get());
    }

// com.fasterxml.jackson.databind.deser.merge.PropertyMergeTest::testInvalidPropertyMerge
    public void testInvalidPropertyMerge() throws Exception
    {
        ObjectMapper mapper = newObjectMapper()
                .disable(MapperFeature.IGNORE_MERGE_FOR_UNMERGEABLE);
        
        try {
            mapper.readValue("{\"value\":3}", CantMergeInts.class);
            fail("Should not pass");
        } catch (InvalidDefinitionException e) {
            verifyException(e, "cannot be merged");
        }
    }

// com.fasterxml.jackson.databind.deser.merge.UpdateValueTest::testValueUpdateWithCreator
    public void testValueUpdateWithCreator() throws Exception
    {
        Bean bean = new Bean("abc", "def");
        MAPPER.readerFor(Bean.class).withValueToUpdate(bean).readValue("{\"a\":\"ghi\",\"b\":\"jkl\"}");
        assertEquals("ghi", bean.getA());
        assertEquals("jkl", bean.getB());
    }

// com.fasterxml.jackson.databind.deser.merge.UpdateValueTest::testValueUpdateOther
    public void testValueUpdateOther() throws Exception
    {
        Bean bean = new Bean("abc", "def");
        ObjectReader r = MAPPER.readerFor(Bean.class).withValueToUpdate(bean);
        
        r = r.withValueToUpdate(null);
        
        Bean result = r.readValue(aposToQuotes("{'a':'x'}"));
        assertNotNull(result);
    }

// com.fasterxml.jackson.databind.exc.DeserExceptionTypeTest::testHandlingOfUnrecognized
    public void testHandlingOfUnrecognized() throws Exception
    {
        UnrecognizedPropertyException exc = null;
        try {
            MAPPER.readValue("{\"bar\":3}", Bean.class);
        } catch (UnrecognizedPropertyException e) {
            exc = e;
        }
        if (exc == null) {
            fail("Should have failed binding");
        }
        assertEquals("bar", exc.getPropertyName());
        assertEquals(Bean.class, exc.getReferringClass());
        
        verifyException(exc, "propX");
    }

// com.fasterxml.jackson.databind.exc.DeserExceptionTypeTest::testExceptionWithEmpty
    public void testExceptionWithEmpty() throws Exception
    {
        try {
            Object result = MAPPER.readValue("    ", Object.class);
            fail("Expected an exception, but got result value: "+result);
        } catch (Exception e) {
            verifyException(e, MismatchedInputException.class, "No content");
        }
    }

// com.fasterxml.jackson.databind.exc.DeserExceptionTypeTest::testExceptionWithIncomplete
    public void testExceptionWithIncomplete()
        throws Exception
    {
        BrokenStringReader r = new BrokenStringReader("[ 1, ", "TEST");
        JsonParser p = MAPPER.getFactory().createParser(r);
        try {
            @SuppressWarnings("unused")
            Object ob = MAPPER.readValue(p, Object.class);
            fail("Should have gotten an exception");
        } catch (IOException e) {
            
            verifyException(e, IOException.class, "TEST");
        }
    }

// com.fasterxml.jackson.databind.exc.DeserExceptionTypeTest::testExceptionWithEOF
    public void testExceptionWithEOF() throws Exception
    {
        JsonParser p = MAPPER.getFactory().createParser("  3");

        Integer I = MAPPER.readValue(p, Integer.class);
        assertEquals(3, I.intValue());

        
        try {
            I = MAPPER.readValue(p, Integer.class);
            fail("Should have gotten an exception");
        } catch (IOException e) {
            verifyException(e, MismatchedInputException.class, "No content");
        }
        
        JsonToken t = p.getCurrentToken();
        if (t != null) {
            fail("Expected current token to be null after end-of-stream, was: "+t);
        }
        p.close();
    }

// com.fasterxml.jackson.databind.exc.DeserExceptionTypeTest::testExceptionForNoCreators
    public void testExceptionForNoCreators() throws Exception
    {
        try {
            NoCreatorsBean b = MAPPER.readValue("{}", NoCreatorsBean.class);
            fail("Should not succeed, got: "+b);
        } catch (JsonMappingException e) {
            verifyException(e, InvalidDefinitionException.class, "no Creators");
        }
    }

// com.fasterxml.jackson.databind.exc.ExceptionDeserializationTest::testIOException
    public void testIOException() throws IOException
    {
        IOException ioe = new IOException("TEST");
        String json = MAPPER.writeValueAsString(ioe);
        IOException result = MAPPER.readValue(json, IOException.class);
        assertEquals(ioe.getMessage(), result.getMessage());
    }

// com.fasterxml.jackson.databind.exc.ExceptionDeserializationTest::testWithCreator
    public void testWithCreator() throws IOException
    {
        final String MSG = "the message";
        String json = MAPPER.writeValueAsString(new MyException(MSG, 3));

        MyException result = MAPPER.readValue(json, MyException.class);
        assertEquals(MSG, result.getMessage());
        assertEquals(3, result.value);
        assertEquals(1, result.stuff.size());
        assertEquals(result.getFoo(), result.stuff.get("foo"));
    }

// com.fasterxml.jackson.databind.exc.ExceptionDeserializationTest::testWithNullMessage
    public void testWithNullMessage() throws IOException
    {
        final ObjectMapper mapper = new ObjectMapper();
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        String json = mapper.writeValueAsString(new IOException((String) null));
        IOException result = mapper.readValue(json, IOException.class);
        assertNotNull(result);
        assertNull(result.getMessage());
    }

// com.fasterxml.jackson.databind.exc.ExceptionDeserializationTest::testNoArgsException
    public void testNoArgsException() throws IOException
    {
        MyNoArgException exc = MAPPER.readValue("{}", MyNoArgException.class);
        assertNotNull(exc);
    }

// com.fasterxml.jackson.databind.exc.ExceptionDeserializationTest::testJDK7SuppressionProperty
    public void testJDK7SuppressionProperty() throws IOException
    {
        Exception exc = MAPPER.readValue("{\"suppressed\":[]}", IOException.class);
        assertNotNull(exc);
    }

// com.fasterxml.jackson.databind.exc.ExceptionDeserializationTest::testSingleValueArrayDeserialization
    public void testSingleValueArrayDeserialization() {}

// com.fasterxml.jackson.databind.exc.ExceptionDeserializationTest::testSingleValueArrayDeserializationException
    public void testSingleValueArrayDeserializationException() throws Exception {
        final ObjectMapper mapper = new ObjectMapper();
        mapper.disable(DeserializationFeature.UNWRAP_SINGLE_VALUE_ARRAYS);
        
        final IOException exp;
        try {
            throw new IOException("testing");
        } catch (IOException internal) {
            exp = internal;
        }
        final String value = "[" + mapper.writeValueAsString(exp) + "]";
        
        try {
            mapper.readValue(value, IOException.class);
            fail("Exception not thrown when attempting to deserialize an IOException wrapped in a single value array with UNWRAP_SINGLE_VALUE_ARRAYS disabled");
        } catch (JsonMappingException exp2) {
            verifyException(exp2, "out of START_ARRAY");
        }
    }

// com.fasterxml.jackson.databind.exc.ExceptionDeserializationTest::testLineNumberAsString
    public void testLineNumberAsString() throws IOException
    {
        Exception exc = MAPPER.readValue(aposToQuotes(
                "{'message':'Test',\n'stackTrace': "
                +"[ { 'lineNumber':'50' } ] }"
        ), IOException.class);
        assertNotNull(exc);
    }

// com.fasterxml.jackson.databind.exc.ExceptionDeserializationTest::testNullAsMessage
    public void testNullAsMessage() throws IOException
    {
        Exception exc = MAPPER.readValue(aposToQuotes(
                "{'message':null, 'localizedMessage':null }"
        ), IOException.class);
        assertNotNull(exc);
        assertNull(exc.getMessage());
        assertNull(exc.getLocalizedMessage());
    }

// com.fasterxml.jackson.databind.exc.ExceptionPathTest::testReferenceChainForInnerClass
    public void testReferenceChainForInnerClass() throws Exception
    {
        String json = MAPPER.writeValueAsString(new Outer());
        try {
            MAPPER.readValue(json, Outer.class);
            fail("Should not pass");
        } catch (JsonMappingException e) {
            JsonMappingException.Reference reference = e.getPath().get(0);
            assertEquals(getClass().getName()+"$Outer[\"inner\"]",
                    reference.toString());
        }
    }

// com.fasterxml.jackson.databind.exc.ExceptionSerializationTest::testSimple
    public void testSimple() throws Exception
    {
        String TEST = "test exception";
        Map<String,Object> result = writeAndMap(MAPPER, new Exception(TEST));
        
        Object ob = result.get("suppressed");
        if (ob != null) {
            assertEquals(5, result.size());
        } else {
            assertEquals(4, result.size());
        }

        assertEquals(TEST, result.get("message"));
        assertNull(result.get("cause"));
        assertEquals(TEST, result.get("localizedMessage"));

        
        Object traces = result.get("stackTrace");
        if (!(traces instanceof List<?>)) {
            fail("Expected a List for exception member 'stackTrace', got: "+traces);
        }
    }

// com.fasterxml.jackson.databind.exc.ExceptionSerializationTest::testSimpleOther
    public void testSimpleOther() throws Exception
    {
        JsonParser p = MAPPER.getFactory().createParser("{ }");
        InvalidFormatException exc = InvalidFormatException.from(p, "Test", getClass(), String.class);
        String json = MAPPER.writeValueAsString(exc);
        p.close();
        assertNotNull(json);
    }

// com.fasterxml.jackson.databind.exc.ExceptionSerializationTest::testIgnorals
    public void testIgnorals() throws Exception
    {
        ExceptionWithIgnoral input = new ExceptionWithIgnoral("foobar");
        input.initCause(new IOException("surprise!"));

        
        String json = MAPPER
                .writerWithDefaultPrettyPrinter()
                .writeValueAsString(input);

        Map<String,Object> result = MAPPER.readValue(json, Map.class);
        assertEquals("foobar", result.get("message"));

        assertNull(result.get("bogus1"));
        assertNotNull(result.get("bogus2"));

        
        ObjectMapper mapper = new ObjectMapper();
        mapper.configOverride(ExceptionWithIgnoral.class)
            .setIgnorals(JsonIgnoreProperties.Value.forIgnoredProperties("bogus2"));
        String json2 = mapper
                .writeValueAsString(new ExceptionWithIgnoral("foobar"));

        Map<String,Object> result2 = mapper.readValue(json2, Map.class);
        assertNull(result2.get("bogus1"));
        assertNull(result2.get("bogus2"));

        
        ExceptionWithIgnoral output = mapper.readValue(json2, ExceptionWithIgnoral.class);
        assertNotNull(output);
        assertEquals("foobar", output.getMessage());
    }

// com.fasterxml.jackson.databind.exc.ExceptionSerializationTest::testJsonMappingExceptionSerialization
    public void testJsonMappingExceptionSerialization() throws IOException {
        Exception e = null;
        
        try {
            MAPPER.readValue( "{ \"val\": \"foo\" }", NoSerdeConstructor.class );
            fail("Should not pass");
        } catch (JsonMappingException e0) {
            verifyException(e0, "cannot deserialize from Object");
            e = e0;
        }
        
        String json = MAPPER.writeValueAsString(e);
        JsonNode root = MAPPER.readTree(json);
        String msg = root.path("message").asText();
        String MATCH = "cannot construct instance";
        if (!msg.toLowerCase().contains(MATCH)) {
            fail("Exception should contain '"+MATCH+"', does not: '"+msg+"'");
        }
    }

// com.fasterxml.jackson.databind.exc.StackTraceElementTest::testCustomStackTraceDeser
    public void testCustomStackTraceDeser() throws Exception
    {
        ObjectMapper mapper = newObjectMapper();
        mapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);

        String json = mapper
                .writerWithDefaultPrettyPrinter()
                .writeValueAsString(new ErrorObject(new Exception("exception message")));

        ErrorObject result = mapper.readValue(json, ErrorObject.class);
        assertNotNull(result);
    }

// com.fasterxml.jackson.databind.exc.TestExceptionHandlingWithDefaultDeserialization::testShouldThrowJsonMappingExceptionWithPathReference
    public void testShouldThrowJsonMappingExceptionWithPathReference() throws IOException {
        
        ObjectMapper mapper = new ObjectMapper();
        String input = "{\"bar\":{\"baz\":{qux:\"quxValue\"))}";
        final String THIS = getClass().getName();

        
        try {
            mapper.readValue(input, Foo.class);
            fail("Upsss! Exception has not been thrown.");
        } catch (JsonMappingException ex) {
            
            assertEquals(THIS+"$Foo[\"bar\"]->"+THIS+"$Bar[\"baz\"]",
                    ex.getPathReference());
        }
    }

// com.fasterxml.jackson.databind.exc.TestExceptionHandlingWithJsonCreatorDeserialization::testShouldThrowJsonMappingExceptionWithPathReference
    public void testShouldThrowJsonMappingExceptionWithPathReference() throws IOException {
        
        ObjectMapper mapper = new ObjectMapper();
        String input = "{\"bar\":{\"baz\":{qux:\"quxValue\"))}";
        final String THIS = getClass().getName();

        
        try {
            mapper.readValue(input, Foo.class);
            fail("Upsss! Exception has not been thrown.");
        } catch (JsonMappingException ex) {
            
            assertEquals(THIS+"$Foo[\"bar\"]->"+THIS+"$Bar[\"baz\"]",
                    ex.getPathReference());
        }
    }

// com.fasterxml.jackson.databind.filter.ProblemHandler1767Test::testPrimitivePropertyWithHandler
    public void testPrimitivePropertyWithHandler() throws Exception {
        final ObjectMapper mapper = new ObjectMapper();
        mapper.addHandler(new IntHandler());
        TestBean result = mapper.readValue(aposToQuotes("{'a': 'not-a-number'}"), TestBean.class);
        assertNotNull(result);
        assertEquals(1, result.a);
    }

// com.fasterxml.jackson.databind.format.CollectionFormatShapeTest::testListAsObjectRoundtrip
    public void testListAsObjectRoundtrip() throws Exception
    {
        
        CollectionAsPOJO list = new CollectionAsPOJO();
        list.add("a");
        list.add("b");
        String json = MAPPER.writeValueAsString(list);
        assertEquals("{\"size\":2,\"values\":[\"a\",\"b\"]}", json);

        
        CollectionAsPOJO result = MAPPER.readValue(json, CollectionAsPOJO.class);
        assertEquals(2, result.size());
    }

// com.fasterxml.jackson.databind.format.DateFormatTest::testTypeDefaults
    public void testTypeDefaults() throws Exception
    {
        ObjectMapper mapper = newObjectMapper();
        mapper.configOverride(Date.class)
            .setFormat(JsonFormat.Value.forPattern("yyyy.dd.MM"));
        
        String json = mapper.writeValueAsString(new DateWrapper(0L));
        assertEquals(aposToQuotes("{'value':'1970.01.01'}"), json);

        
        DateWrapper w = mapper.readValue(aposToQuotes("{'value':'1981.13.3'}"), DateWrapper.class);
        assertNotNull(w);
        
        Calendar c = Calendar.getInstance();
        c.setTime(w.value);
        assertEquals(1981, c.get(Calendar.YEAR));
        assertEquals(Calendar.MARCH, c.get(Calendar.MONTH));
    }

// com.fasterxml.jackson.databind.format.MapEntryFormatTest::testInclusion
    public void testInclusion() throws Exception
    {
        assertEquals(aposToQuotes("{'entry':{'a':'b'}}"),
                MAPPER.writeValueAsString(new EmptyEntryWrapper("a", "b")));
        assertEquals(aposToQuotes("{'entry':{'a':'b'}}"),
                MAPPER.writeValueAsString(new EntryWithDefaultWrapper("a", "b")));
        assertEquals(aposToQuotes("{'entry':{'a':'b'}}"),
                MAPPER.writeValueAsString(new EntryWithNullWrapper("a", "b")));

        assertEquals(aposToQuotes("{}"),
                MAPPER.writeValueAsString(new EmptyEntryWrapper("a", "")));
        assertEquals(aposToQuotes("{}"),
                MAPPER.writeValueAsString(new EntryWithDefaultWrapper("a", "")));
        assertEquals(aposToQuotes("{'entry':{'a':''}}"),
                MAPPER.writeValueAsString(new EntryWithNullWrapper("a", "")));
        assertEquals(aposToQuotes("{}"),
                MAPPER.writeValueAsString(new EntryWithNullWrapper("a", null)));
    }

// com.fasterxml.jackson.databind.format.MapEntryFormatTest::testInclusionWithReference
    public void testInclusionWithReference() throws Exception
    {
        assertEquals(aposToQuotes("{'entry':{'a':'b'}}"),
                MAPPER.writeValueAsString(new EntryWithNonAbsentWrapper("a", "b")));
        
        
        assertEquals(aposToQuotes("{'entry':{'a':''}}"),
                MAPPER.writeValueAsString(new EntryWithNonAbsentWrapper("a", "")));
        assertEquals(aposToQuotes("{}"),
                MAPPER.writeValueAsString(new EntryWithNonAbsentWrapper("a", null)));
    }

// com.fasterxml.jackson.databind.format.MapEntryFormatTest::testAsNaturalRoundtrip
    public void testAsNaturalRoundtrip() throws Exception
    {
        BeanWithMapEntry input = new BeanWithMapEntry("foo" ,"bar");
        String json = MAPPER.writeValueAsString(input);
        assertEquals(aposToQuotes("{'entry':{'foo':'bar'}}"), json);
        BeanWithMapEntry result = MAPPER.readValue(json, BeanWithMapEntry.class);
        assertEquals("foo", result.entry.getKey());
        assertEquals("bar", result.entry.getValue());
    }

// com.fasterxml.jackson.databind.format.MapEntryFormatTest::testAsObjectRoundtrip
    public void testAsObjectRoundtrip() throws Exception
    {
        MapEntryAsObject input = new MapEntryAsObject("foo" ,"bar");
        String json = MAPPER.writeValueAsString(input);
        assertEquals(aposToQuotes("{'key':'foo','value':'bar'}"), json);

        
        
        
        MapEntryAsObject result = MAPPER.readValue(json, MapEntryAsObject.class);
        assertEquals("foo", result.getKey());
        assertEquals("bar", result.getValue());
    }

// com.fasterxml.jackson.databind.format.MapEntryFormatTest::testDefaultShapeOverride
    public void testDefaultShapeOverride() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configOverride(Map.Entry.class)
            .setFormat(JsonFormat.Value.forShape(JsonFormat.Shape.OBJECT));
        Map.Entry<String,String> input = new BeanWithMapEntry("foo", "bar").entry;
        assertEquals(aposToQuotes("{'key':'foo','value':'bar'}"),
                mapper.writeValueAsString(input));
    }

// com.fasterxml.jackson.databind.format.MapFormatShapeTest::testSerializeAsPOJOViaClass
    public void testSerializeAsPOJOViaClass() throws Exception
    {
        String result = MAPPER.writeValueAsString(new Bean476Container(1,2,0));
        assertEquals(aposToQuotes("{'a':{'extra':13,'empty':false},'b':{'value':2}}"),
                result);
    }

// com.fasterxml.jackson.databind.format.MapFormatShapeTest::testRoundTrip
    public void testRoundTrip() throws Exception
    {
        Map1540Implementation input = new Map1540Implementation();
        input.property = 55;
        input.put(12, 45);
        input.put(6, 88);

        String json = MAPPER.writeValueAsString(input);

        assertEquals(aposToQuotes("{'property':55,'map':{'6':88,'12':45}}"), json);

        Map1540Implementation result = MAPPER.readValue(json, Map1540Implementation.class);
        assertEquals(result.property, input.property);
        assertEquals(input.getMap(), input.getMap());
   }

// com.fasterxml.jackson.databind.format.MapFormatShapeTest::testDeserializeAsPOJOViaClass
    public void testDeserializeAsPOJOViaClass() throws Exception
    {
        Map476AsPOJO result = MAPPER.readValue(aposToQuotes("{'extra':42}"),
                Map476AsPOJO.class);
        assertEquals(0, result.size());
        assertEquals(42, result.extra);
    }

// com.fasterxml.jackson.databind.interop.DeprecatedTypeHandling1102Test::testSimplePOJOType
    public void testSimplePOJOType() throws Exception
    {
        JavaType elem = SimpleType.construct(Point.class);

        Point p = MAPPER.readValue(aposToQuotes("{'x':1,'y':2}"), elem);
        assertNotNull(p);
        assertEquals(1, p.x);
        assertEquals(2, p.getY());
    }

// com.fasterxml.jackson.databind.interop.DeprecatedTypeHandling1102Test::testPOJOSubType
    public void testPOJOSubType() throws Exception
    {
        JavaType elem = SimpleType.construct(Point3D.class);

        Point3D p = MAPPER.readValue(aposToQuotes("{'x':1,'z':3,'y':2}"), elem);
        assertNotNull(p);
        assertEquals(1, p.x);
        assertEquals(2, p.getY());
        assertEquals(3, p.z);
    }

// com.fasterxml.jackson.databind.interop.DeprecatedTypeHandling1102Test::testExplicitCollectionType
    public void testExplicitCollectionType() throws Exception
    {
        JavaType elem = SimpleType.construct(Point.class);
        JavaType t = CollectionType.construct(List.class, elem);

        final String json = aposToQuotes("[ {'x':1,'y':2}, {'x':3,'y':6 }]");        

        List<Point> l = MAPPER.readValue(json, t);
        assertNotNull(l);
        assertEquals(2, l.size());
        Object ob = l.get(0);
        assertEquals(Point.class, ob.getClass());
        Point p = (Point) ob;
        assertEquals(1, p.x);
        assertEquals(2, p.getY());
    }

// com.fasterxml.jackson.databind.interop.DeprecatedTypeHandling1102Test::testExplicitMapType
    public void testExplicitMapType() throws Exception
    {
        JavaType key = SimpleType.construct(String.class);
        JavaType elem = SimpleType.construct(Point.class);
        JavaType t = MapType.construct(Map.class, key, elem);

        final String json = aposToQuotes("{'x':{'x':3,'y':5}}");        

        Map<String,Point> m = MAPPER.readValue(json, t);
        assertNotNull(m);
        assertEquals(1, m.size());
        Object ob = m.values().iterator().next();
        assertEquals(Point.class, ob.getClass());
        Point p = (Point) ob;
        assertEquals(3, p.x);
        assertEquals(5, p.getY());
    }

// com.fasterxml.jackson.databind.interop.DeprecatedTypeHandling1102Test::testDeprecatedTypeResolution
    public void testDeprecatedTypeResolution() throws Exception
    {
        TypeFactory tf = MAPPER.getTypeFactory();

        
        JavaType t = tf.constructType(Point.class, getClass());
        assertEquals(Point.class, t.getRawClass());

        
        JavaType t2 = tf.constructType(Point.class, (Class<?>) null);
        assertEquals(Point.class, t2.getRawClass());

        JavaType ctxt = tf.constructType(getClass());
        JavaType t3 = tf.constructType(Point.class, ctxt);
        assertEquals(Point.class, t3.getRawClass());
    }

// com.fasterxml.jackson.databind.interop.IllegalTypesCheckTest::testXalanTypes1599
    public void testXalanTypes1599() throws Exception
    {
        final String clsName = "com.sun.org.apache.xalan.internal.xsltc.trax.TemplatesImpl";
        final String JSON = aposToQuotes(
 "{'id': 124,\n"
+" 'obj':[ '"+clsName+"',\n"
+"  {\n"
+"    'transletBytecodes' : [ 'AAIAZQ==' ],\n"
+"    'transletName' : 'a.b',\n"
+"    'outputProperties' : { }\n"
+"  }\n"
+" ]\n"
+"}"
        );
        ObjectMapper mapper = new ObjectMapper();
        mapper.enableDefaultTyping();
        try {
            mapper.readValue(JSON, Bean1599.class);
            fail("Should not pass");
        } catch (JsonMappingException e) {
            _verifySecurityException(e, clsName);
        }
    }

// com.fasterxml.jackson.databind.interop.IllegalTypesCheckTest::testJDKTypes1737
    public void testJDKTypes1737() throws Exception
    {
        _testIllegalType(java.util.logging.FileHandler.class);
        _testIllegalType(java.rmi.server.UnicastRemoteObject.class);
    }

// com.fasterxml.jackson.databind.interop.IllegalTypesCheckTest::testJDKTypes1855
    public void testJDKTypes1855() throws Exception
    {
        

        
        _testIllegalType(BogusPointcutAdvisor.class);
        _testIllegalType(BogusApplicationContext.class);
    }

// com.fasterxml.jackson.databind.interop.IllegalTypesCheckTest::testJDKTypes1872
    public void testJDKTypes1872() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        mapper.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL, JsonTypeInfo.As.PROPERTY);

        String json = aposToQuotes(String.format("{'@class':'%s','authorities':['java.util.ArrayList',[]]}",
                Authentication1872.class.getName()));
        Authentication1872 result = mapper.readValue(json, Authentication1872.class);
        assertNotNull(result);
    }

// com.fasterxml.jackson.databind.interop.IllegalTypesCheckTest::testC3P0Types
    public void testC3P0Types() throws Exception
    {
        _testIllegalType(ComboPooledDataSource.class); 
    }

// com.fasterxml.jackson.databind.interop.TestExternalizable::testSerializeAsExternalizable
    public void testSerializeAsExternalizable() throws Exception
    {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        ObjectOutputStream obs = new ObjectOutputStream(bytes);
        final MyPojo input = new MyPojo(13, "Foobar", new int[] { 1, 2, 3 } );
        obs.writeObject(input);
        obs.close();
        byte[] ser = bytes.toByteArray();

        
        byte[] json = MapperHolder.mapper().writeValueAsBytes(input);

        int ix = indexOf(ser, json);
        if (ix < 0) {
            fail("Serialization ("+ser.length+") does NOT contain JSON (of "+json.length+")");
        }
        
        
        if (false) {
            bytes = new ByteArrayOutputStream();
            obs = new ObjectOutputStream(bytes);
            MyPojoNative p = new MyPojoNative(13, "Foobar", new int[] { 1, 2, 3 } );
            obs.writeObject(p);
            obs.close();
            System.out.println("Native size: "+bytes.size()+", vs JSON: "+ser.length);
        }
        
        
        ObjectInputStream ins = new ObjectInputStream(new ByteArrayInputStream(ser));
        MyPojo output = (MyPojo) ins.readObject();
        ins.close();
        assertNotNull(output);
        
        assertEquals(input, output);
    }

// com.fasterxml.jackson.databind.interop.TestFormatDetection::testSimpleWithJSON
    public void testSimpleWithJSON() throws Exception
    {
        ObjectReader detecting = READER.forType(POJO.class);
        detecting = detecting.withFormatDetection(detecting);
        POJO pojo = detecting.readValue(utf8Bytes("{\"x\":1}"));
        assertNotNull(pojo);
        assertEquals(1, pojo.x);
    }

// com.fasterxml.jackson.databind.interop.TestFormatDetection::testSequenceWithJSON
    public void testSequenceWithJSON() throws Exception
    {
        ObjectReader detecting = READER.forType(POJO.class);
        detecting = detecting.withFormatDetection(detecting);
        MappingIterator<POJO> it = detecting.
                readValues(utf8Bytes(aposToQuotes("{'x':1}\n{'x':2,'y':5}")));

        assertTrue(it.hasNextValue());
        POJO pojo = it.nextValue();
        assertEquals(1, pojo.x);

        assertTrue(it.hasNextValue());
        pojo = it.nextValue();
        assertEquals(2, pojo.x);
        assertEquals(5, pojo.y);
        
        assertFalse(it.hasNextValue());
        it.close();

        
        ObjectReader r2 = READER.forType(JsonNode.class);
        r2 = r2.withFormatDetection(r2);
        MappingIterator<JsonNode> nodes = r2.
                readValues(utf8Bytes(aposToQuotes("{'x':1}\n{'x':2,'y':5}")));

        assertTrue(nodes.hasNextValue());
        JsonNode n = nodes.nextValue();
        assertEquals(1, n.size());

        assertTrue(nodes.hasNextValue());
        n = nodes.nextValue();
        assertEquals(2, n.size());
        assertEquals(2, n.path("x").asInt());
        assertEquals(5, n.path("y").asInt());

        assertFalse(nodes.hasNextValue());
        nodes.close();
    }

// com.fasterxml.jackson.databind.interop.TestFormatDetection::testInvalid
    public void testInvalid() throws Exception
    {
        ObjectReader detecting = READER.forType(POJO.class);
        detecting = detecting.withFormatDetection(detecting);
        try {
            detecting.readValue(utf8Bytes("<POJO><x>1</x></POJO>"));
            fail("Should have failed");
        } catch (JsonProcessingException e) {
            verifyException(e, "Cannot detect format from input");
        }
    }

// com.fasterxml.jackson.databind.interop.TestJDKProxy::testSimple
    public void testSimple() throws Exception
    {
        IPlanet input = getProxy(IPlanet.class, new Planet("Foo"));
        String json = MAPPER.writeValueAsString(input);
        assertEquals("{\"name\":\"Foo\"}", json);
        
        
        Planet output = MAPPER.readValue(json, Planet.class);
        assertEquals("Foo", output.getName());
    }

// com.fasterxml.jackson.databind.introspect.CustomAnnotationIntrospector1756Test::testIssue1756
    public void testIssue1756() throws Exception
    {
        Issue1756Module m = new Issue1756Module();
        m.addAbstractTypeMapping(Foobar.class, FoobarImpl.class);
        final ObjectMapper mapper = new ObjectMapper()
            .registerModule(m);

        final Foobar foobar = mapper.readValue(aposToQuotes("{'bar':'bar', 'foo':'foo'}"),
                Foobar.class);
        assertNotNull(foobar);
    }

// com.fasterxml.jackson.databind.introspect.IgnoredCreatorProperty1572Test::testIgnoredCtorParam
    public void testIgnoredCtorParam() throws Exception
    {
        final ObjectMapper mapper = new ObjectMapper();
        mapper.setAnnotationIntrospector(new ImplicitNames());
        String JSON = aposToQuotes("{'innerTest': {\n"
                +"'str':'str',\n"
                +"'otherStr': 'otherStr'\n"
                +"}}\n");
        OuterTest result = mapper.readValue(JSON, OuterTest.class);
        assertNotNull(result);
        assertNotNull(result.innerTest);
        assertEquals("otherStr", result.innerTest.otherStr);
    }

// com.fasterxml.jackson.databind.introspect.IgnoredFieldPresentInCreatorProperty2001Test::testIgnoredFieldPresentInPropertyCreator
    public void testIgnoredFieldPresentInPropertyCreator() throws Exception {
        Foo deserialized = newObjectMapper().readValue("{\"query\": \"bar\"}", Foo.class);
        assertEquals("bar", deserialized.query);
    }

// com.fasterxml.jackson.databind.introspect.NoClassDefFoundWorkaroundTest::testClassIsMissing
    public void testClassIsMissing() {}

// com.fasterxml.jackson.databind.introspect.NoClassDefFoundWorkaroundTest::testDeserialize
    public void testDeserialize() throws Exception
    {
        ObjectMapper m = new ObjectMapper();
        Parent result = null;

        try {
            result = m.readValue(" { } ", Parent.class);
        } catch (Exception e) {
            fail("Should not have had issues, got: "+e);
        }
        assertNotNull(result);
    }

// com.fasterxml.jackson.databind.introspect.NoClassDefFoundWorkaroundTest::testUseMissingClass
    public void testUseMissingClass() {}

// com.fasterxml.jackson.databind.introspect.SetterConflictTest::testSetterPriority
    public void testSetterPriority() throws Exception
    {
        Issue1033Bean bean = MAPPER.readValue(aposToQuotes("{'value':42}"),
                Issue1033Bean.class);
        assertEquals(42, bean.value);
    }

// com.fasterxml.jackson.databind.introspect.TestAnnotationBundles::testKeepAnnotationBundle
    public void testKeepAnnotationBundle() throws Exception
    {
        MAPPER.setAnnotationIntrospector(new BundleAnnotationIntrospector());
        assertEquals("{\"important\":42}", MAPPER.writeValueAsString(new InformingHolder()));
    }

// com.fasterxml.jackson.databind.introspect.TestAnnotationBundles::testRecursiveBundlesField
    public void testRecursiveBundlesField() throws Exception {
        assertEquals("{\"unimportant\":42}", MAPPER.writeValueAsString(new RecursiveHolder()));
    }

// com.fasterxml.jackson.databind.introspect.TestAnnotationBundles::testRecursiveBundlesMethod
    public void testRecursiveBundlesMethod() throws Exception {
        assertEquals("{\"value\":28}", MAPPER.writeValueAsString(new RecursiveHolder2()));
    }

// com.fasterxml.jackson.databind.introspect.TestAnnotationBundles::testRecursiveBundlesConstructor
    public void testRecursiveBundlesConstructor() throws Exception {
        RecursiveHolder3 result = MAPPER.readValue("17", RecursiveHolder3.class);
        assertNotNull(result);
        assertEquals(17, result.x);
    }

// com.fasterxml.jackson.databind.introspect.TestAnnotationBundles::testBundledIgnore
    public void testBundledIgnore() throws Exception
    {
        assertEquals("{\"foobar\":13}", MAPPER.writeValueAsString(new Bean()));
    }

// com.fasterxml.jackson.databind.introspect.TestAnnotationBundles::testVisibilityBundle
    public void testVisibilityBundle() throws Exception
    {
        assertEquals("{\"b\":5}", MAPPER.writeValueAsString(new NoAutoDetect()));
    }

// com.fasterxml.jackson.databind.introspect.TestAnnotationBundles::testIssue92
    public void testIssue92() throws Exception
    {
        assertEquals("{\"_id\":\"abc\"}", MAPPER.writeValueAsString(new Bean92()));
    }

// com.fasterxml.jackson.databind.introspect.TestAnnotationMerging::testSharedNames
    public void testSharedNames() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        assertEquals("{\"x\":6}", mapper.writeValueAsString(new SharedName(6)));
    }

// com.fasterxml.jackson.databind.introspect.TestAnnotationMerging::testSharedNamesFromGetterToSetter
    public void testSharedNamesFromGetterToSetter() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        String json = mapper.writeValueAsString(new SharedName2());
        assertEquals("{\"x\":1}", json);
        SharedName2 result = mapper.readValue(json, SharedName2.class);
        assertNotNull(result);
    }

// com.fasterxml.jackson.databind.introspect.TestAnnotationMerging::testSharedTypeInfo
    public void testSharedTypeInfo() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        String json = mapper.writeValueAsString(new Wrapper(13L));
        Wrapper result = mapper.readValue(json, Wrapper.class);
        assertEquals(Long.class, result.value.getClass());
    }

// com.fasterxml.jackson.databind.introspect.TestAnnotationMerging::testSharedTypeInfoWithCtor
    public void testSharedTypeInfoWithCtor() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        String json = mapper.writeValueAsString(new TypeWrapper(13L));
        TypeWrapper result = mapper.readValue(json, TypeWrapper.class);
        assertEquals(Long.class, result.value.getClass());
    }

// com.fasterxml.jackson.databind.introspect.TestAutoDetect::testPrivateCtor
    public void testPrivateCtor() throws Exception
    {
        
        ObjectMapper m = new ObjectMapper();
        PrivateBean bean = m.readValue("\"abc\"", PrivateBean.class);
        assertEquals("abc", bean.a);

        
        m = new ObjectMapper();
        VisibilityChecker<?> vc = m.getVisibilityChecker();
        vc = vc.withCreatorVisibility(JsonAutoDetect.Visibility.PUBLIC_ONLY);
        m.setVisibility(vc);
        try {
            m.readValue("\"abc\"", PrivateBean.class);
            fail("Expected exception for missing constructor");
        } catch (JsonProcessingException e) {
            verifyException(e, "no String-argument constructor/factory");
        }
    }

// com.fasterxml.jackson.databind.introspect.TestAutoDetect::testVisibilityConfigOverridesForSer
    public void testVisibilityConfigOverridesForSer() throws Exception
    {
        
        final Feature1347SerBean input = new Feature1347SerBean();
        assertEquals(aposToQuotes("{'field':2,'value':3}"),
                MAPPER.writeValueAsString(input));

        ObjectMapper mapper = new ObjectMapper();
        mapper.configOverride(Feature1347SerBean.class)
            .setVisibility(JsonAutoDetect.Value.construct(PropertyAccessor.GETTER,
                            Visibility.NONE));
        assertEquals(aposToQuotes("{'field':2}"),
                mapper.writeValueAsString(input));
    }

// com.fasterxml.jackson.databind.introspect.TestAutoDetect::testVisibilityConfigOverridesForDeser
    public void testVisibilityConfigOverridesForDeser() throws Exception
    {
        final String JSON = aposToQuotes("{'value':3}");

        
        try {
            
            MAPPER.readValue(JSON, Feature1347DeserBean.class);
            fail("Should not pass");
        } catch (JsonMappingException e) {
            verifyException(e, "Should NOT get called");
        }

        
        ObjectMapper mapper = new ObjectMapper();
        mapper.configOverride(Feature1347DeserBean.class)
            .setVisibility(JsonAutoDetect.Value.construct(PropertyAccessor.SETTER,
                        Visibility.NONE));
        Feature1347DeserBean result = mapper.readValue(JSON, Feature1347DeserBean.class);
        assertEquals(3, result.value);
    }

// com.fasterxml.jackson.databind.introspect.TestInferredMutators::testFinalFieldIgnoral
    public void testFinalFieldIgnoral() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        
        assertTrue(mapper.isEnabled(MapperFeature.ALLOW_FINAL_FIELDS_AS_MUTATORS));
        mapper.disable(MapperFeature.ALLOW_FINAL_FIELDS_AS_MUTATORS);
        try {
             mapper.readValue("{\"x\":2}", FixedPoint.class);
            fail("Should not try to use final field");
        } catch (JsonMappingException e) {
            verifyException(e, "unrecognized field \"x\"");
        }
    }

// com.fasterxml.jackson.databind.introspect.TestInferredMutators::testDeserializationInference
    public void testDeserializationInference() throws Exception
    {
        final String JSON = "{\"x\":2}";
        ObjectMapper mapper = new ObjectMapper();
        
        assertTrue(mapper.isEnabled(MapperFeature.INFER_PROPERTY_MUTATORS));
        Point p = mapper.readValue(JSON,  Point.class);
        assertEquals(2, p.x);

        
        mapper = new ObjectMapper();
        mapper.disable(MapperFeature.INFER_PROPERTY_MUTATORS);
        try {
            p = mapper.readValue(JSON,  Point.class);
            fail("Should not succeeed");
        } catch (JsonMappingException e) {
            verifyException(e, "unrecognized field \"x\"");
        }
    }

// com.fasterxml.jackson.databind.introspect.TestJacksonAnnotationIntrospector::testSerializeDeserializeWithJaxbAnnotations
    public void testSerializeDeserializeWithJaxbAnnotations() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        JacksonExample ex = new JacksonExample();
        QName qname = new QName("urn:hi", "hello");
        ex.setQname(qname);
        ex.setAttributeProperty("attributeValue");
        ex.setElementProperty("elementValue");
        ex.setWrappedElementProperty(Arrays.asList("wrappedElementValue"));
        ex.setEnumProperty(EnumExample.VALUE1);
        StringWriter writer = new StringWriter();
        mapper.writeValue(writer, ex);
        writer.flush();
        writer.close();

        String json = writer.toString();
        JacksonExample readEx = mapper.readValue(json, JacksonExample.class);

        assertEquals(ex.qname, readEx.qname);
        assertEquals(ex.attributeProperty, readEx.attributeProperty);
        assertEquals(ex.elementProperty, readEx.elementProperty);
        assertEquals(ex.wrappedElementProperty, readEx.wrappedElementProperty);
        assertEquals(ex.enumProperty, readEx.enumProperty);
    }

// com.fasterxml.jackson.databind.introspect.TestJacksonAnnotationIntrospector::testJsonTypeResolver
    public void testJsonTypeResolver() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        JacksonAnnotationIntrospector ai = new JacksonAnnotationIntrospector();
        AnnotatedClass ac = AnnotatedClassResolver.resolveWithoutSuperTypes(mapper.getSerializationConfig(),
                TypeResolverBean.class);
        JavaType baseType = TypeFactory.defaultInstance().constructType(TypeResolverBean.class);
        TypeResolverBuilder<?> rb = ai.findTypeResolver(mapper.getDeserializationConfig(), ac, baseType);
        assertNotNull(rb);
        assertSame(DummyBuilder.class, rb.getClass());
    }

// com.fasterxml.jackson.databind.introspect.TestJacksonAnnotationIntrospector::testEnumHandling
    public void testEnumHandling() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setAnnotationIntrospector(new LcEnumIntrospector());
        assertEquals("\"value1\"", mapper.writeValueAsString(EnumExample.VALUE1));
        EnumExample result = mapper.readValue(quote("value1"), EnumExample.class);
        assertEquals(EnumExample.VALUE1, result);
    }

// com.fasterxml.jackson.databind.introspect.TestNameConflicts::testIssue193
    public void testIssue193() throws Exception
    {
        String json = objectWriter().writeValueAsString(new Bean193(1, 2));
        assertNotNull(json);
    }

// com.fasterxml.jackson.databind.introspect.TestNameConflicts::testNonConflict
    public void testNonConflict() throws Exception
    {
        String json = MAPPER.writeValueAsString(new BogusConflictBean());
        assertEquals(aposToQuotes("{'prop1':2,'prop2':1}"), json);
    }

// com.fasterxml.jackson.databind.introspect.TestNameConflicts::testHypotheticalGetters
    public void testHypotheticalGetters() throws Exception
    {
        String json = objectWriter().writeValueAsString(new MultipleTheoreticalGetters());
        assertEquals(aposToQuotes("{'a':3}"), json);
    }

// com.fasterxml.jackson.databind.introspect.TestNameConflicts::testOverrideName
    public void testOverrideName() throws Exception
    {
        final ObjectMapper mapper = objectMapper();
        String json = mapper.writeValueAsString(new CoreBean158());
        assertEquals(aposToQuotes("{'bar':'x'}"), json);

        
        CoreBean158 result = null;
        try {
            result = mapper.readValue(aposToQuotes("{'bar':'y'}"), CoreBean158.class);
        } catch (Exception e) {
            fail("Unexpected failure when reading CoreBean158: "+e);
        }
        assertNotNull(result);
        assertEquals("y", result.bar);
    }

// com.fasterxml.jackson.databind.introspect.TestNamingStrategyCustom::testSimpleGetters
    public void testSimpleGetters() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setPropertyNamingStrategy(new PrefixStrategy());
        assertEquals("{\"Get-key\":123}", mapper.writeValueAsString(new GetterBean()));
    }

// com.fasterxml.jackson.databind.introspect.TestNamingStrategyCustom::testSimpleSetters
    public void testSimpleSetters() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setPropertyNamingStrategy(new PrefixStrategy());
        SetterBean bean = mapper.readValue("{\"Set-key\":13}", SetterBean.class);
        assertEquals(13, bean.value);
    }

// com.fasterxml.jackson.databind.introspect.TestNamingStrategyCustom::testSimpleFields
    public void testSimpleFields() throws Exception
    {
        
        ObjectMapper mapper = new ObjectMapper();
        mapper.setPropertyNamingStrategy(new PrefixStrategy());
        String json = mapper.writeValueAsString(new FieldBean(999));
        assertEquals("{\"Field-key\":999}", json);

        
        FieldBean result = mapper.readValue(json, FieldBean.class);
        assertEquals(999, result.key);
    }

// com.fasterxml.jackson.databind.introspect.TestNamingStrategyCustom::testCStyleNaming
    public void testCStyleNaming() throws Exception
    {
        
        ObjectMapper mapper = new ObjectMapper();
        mapper.setPropertyNamingStrategy(new CStyleStrategy());
        String json = mapper.writeValueAsString(new PersonBean("Joe", "Sixpack", 42));
        assertEquals("{\"first_name\":\"Joe\",\"last_name\":\"Sixpack\",\"age\":42}", json);
        
        
        PersonBean result = mapper.readValue(json, PersonBean.class);
        assertEquals("Joe", result.firstName);
        assertEquals("Sixpack", result.lastName);
        assertEquals(42, result.age);
    }

// com.fasterxml.jackson.databind.introspect.TestNamingStrategyCustom::testWithGetterAsSetter
    public void testWithGetterAsSetter() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setPropertyNamingStrategy(new CStyleStrategy());
        SetterlessWithValue input = new SetterlessWithValue().add(3);
        String json = mapper.writeValueAsString(input);
        assertEquals("{\"value_list\":[{\"int_value\":3}]}", json);

        SetterlessWithValue result = mapper.readValue(json, SetterlessWithValue.class);
        assertNotNull(result.values);
        assertEquals(1, result.values.size());
        assertEquals(3, result.values.get(0).intValue);
    }

// com.fasterxml.jackson.databind.introspect.TestNamingStrategyCustom::testLowerCase
    public void testLowerCase() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setPropertyNamingStrategy(new LcStrategy());

        RenamedCollectionBean result = mapper.readValue("{\"thevalues\":[\"a\"]}",
                RenamedCollectionBean.class);
        assertNotNull(result.getTheValues());
        assertEquals(1, result.getTheValues().size());
        assertEquals("a", result.getTheValues().get(0));
    }

// com.fasterxml.jackson.databind.introspect.TestNamingStrategyCustom::testPerClassAnnotation
    public void testPerClassAnnotation() throws Exception
    {
        final ObjectMapper mapper = new ObjectMapper();
        mapper.setPropertyNamingStrategy(new LcStrategy());
        BeanWithPrefixNames input = new BeanWithPrefixNames();
        String json = mapper.writeValueAsString(input);
        assertEquals("{\"Get-a\":3}", json);

        BeanWithPrefixNames output = mapper.readValue("{\"Set-a\":7}",
                BeanWithPrefixNames.class);
        assertEquals(7, output.a);
    }

// com.fasterxml.jackson.databind.introspect.TestNamingStrategyStd::testLowerCaseStrategyStandAlone
    public void testLowerCaseStrategyStandAlone()
    {
        for (Object[] pair : SNAKE_CASE_NAME_TRANSLATIONS) {
            String translatedJavaName = PropertyNamingStrategy.SNAKE_CASE.nameForField(null, null,
                    (String) pair[0]);
            assertEquals((String) pair[1], translatedJavaName);
        }
    }

// com.fasterxml.jackson.databind.introspect.TestNamingStrategyStd::testLowerCaseTranslations
    public void testLowerCaseTranslations() throws Exception
    {
        
        String json = _lcWithUndescoreMapper.writeValueAsString(new PersonBean("Joe", "Sixpack", 42));
        assertEquals("{\"first_name\":\"Joe\",\"last_name\":\"Sixpack\",\"age\":42}", json);
        
        
        PersonBean result = _lcWithUndescoreMapper.readValue(json, PersonBean.class);
        assertEquals("Joe", result.firstName);
        assertEquals("Sixpack", result.lastName);
        assertEquals(42, result.age);
    }

// com.fasterxml.jackson.databind.introspect.TestNamingStrategyStd::testLowerCaseAcronymsTranslations
    public void testLowerCaseAcronymsTranslations() throws Exception
    {
        
        String json = _lcWithUndescoreMapper.writeValueAsString(new Acronyms("world wide web", "http://jackson.codehaus.org", "/path1/,/path2/"));
        assertEquals("{\"www\":\"world wide web\",\"some_url\":\"http://jackson.codehaus.org\",\"some_uris\":\"/path1/,/path2/\"}", json);
        
        
        Acronyms result = _lcWithUndescoreMapper.readValue(json, Acronyms.class);
        assertEquals("world wide web", result.WWW);
        assertEquals("http://jackson.codehaus.org", result.someURL);
        assertEquals("/path1/,/path2/", result.someURIs);
    }

// com.fasterxml.jackson.databind.introspect.TestNamingStrategyStd::testLowerCaseOtherNonStandardNamesTranslations
    public void testLowerCaseOtherNonStandardNamesTranslations() throws Exception
    {
        
        String json = _lcWithUndescoreMapper.writeValueAsString(new OtherNonStandardNames("Results", "_User", "___", "$User"));
        assertEquals("{\"results\":\"Results\",\"user\":\"_User\",\"__\":\"___\",\"$_user\":\"$User\"}", json);
        
        
        OtherNonStandardNames result = _lcWithUndescoreMapper.readValue(json, OtherNonStandardNames.class);
        assertEquals("Results", result.Results);
        assertEquals("_User", result._User);
        assertEquals("___", result.___);
        assertEquals("$User", result.$User);
    }

// com.fasterxml.jackson.databind.introspect.TestNamingStrategyStd::testLowerCaseUnchangedNames
    public void testLowerCaseUnchangedNames() throws Exception
    {
        
        String json = _lcWithUndescoreMapper.writeValueAsString(new UnchangedNames("from_user", "_user", "from$user", "from7user", "_x"));
        assertEquals("{\"from_user\":\"from_user\",\"user\":\"_user\",\"from$user\":\"from$user\",\"from7user\":\"from7user\",\"x\":\"_x\"}", json);
        
        
        UnchangedNames result = _lcWithUndescoreMapper.readValue(json, UnchangedNames.class);
        assertEquals("from_user", result.from_user);
        assertEquals("_user", result._user);
        assertEquals("from$user", result.from$user);
        assertEquals("from7user", result.from7user);
        assertEquals("_x", result._x);
    }

// com.fasterxml.jackson.databind.introspect.TestNamingStrategyStd::testPascalCaseStandAlone
    public void testPascalCaseStandAlone()
    {
        assertEquals("UserName", PropertyNamingStrategy.UPPER_CAMEL_CASE.nameForField(null, null, "userName"));
        assertEquals("User", PropertyNamingStrategy.UPPER_CAMEL_CASE.nameForField(null, null, "User"));
        assertEquals("User", PropertyNamingStrategy.UPPER_CAMEL_CASE.nameForField(null, null, "user"));
        assertEquals("X", PropertyNamingStrategy.UPPER_CAMEL_CASE.nameForField(null, null, "x"));

        assertEquals("BADPublicName", PropertyNamingStrategy.UPPER_CAMEL_CASE.nameForField(null, null, "bADPublicName"));
        assertEquals("BADPublicName", PropertyNamingStrategy.UPPER_CAMEL_CASE.nameForGetterMethod(null, null, "bADPublicName"));
    }

// com.fasterxml.jackson.databind.introspect.TestNamingStrategyStd::testIssue428PascalWithOverrides
    public void testIssue428PascalWithOverrides() throws Exception
    {
        String json = new ObjectMapper()
                .setPropertyNamingStrategy(PropertyNamingStrategy.UPPER_CAMEL_CASE)
                .writeValueAsString(new Bean428());
        if (!json.contains(quote("fooBar"))) {
            fail("Should use name 'fooBar', does not: "+json);
        }
    }

// com.fasterxml.jackson.databind.introspect.TestNamingStrategyStd::testSimpleLowerCase
    public void testSimpleLowerCase() throws Exception
    {
        final BoringBean input = new BoringBean();
        ObjectMapper m = objectMapper();

        assertEquals(aposToQuotes("{'firstname':'Bob','lastname':'Burger'}"),
                m.writeValueAsString(input));
    }

// com.fasterxml.jackson.databind.introspect.TestNamingStrategyStd::testKebabCaseStrategyStandAlone
    public void testKebabCaseStrategyStandAlone()
    {
        assertEquals("some-value",
                PropertyNamingStrategy.KEBAB_CASE.nameForField(null, null, "someValue"));
        assertEquals("some-value",
                PropertyNamingStrategy.KEBAB_CASE.nameForField(null, null, "SomeValue"));
        assertEquals("url",
                PropertyNamingStrategy.KEBAB_CASE.nameForField(null, null, "URL"));
        assertEquals("url-stuff",
                PropertyNamingStrategy.KEBAB_CASE.nameForField(null, null, "URLStuff"));
        assertEquals("some-url-stuff",
                PropertyNamingStrategy.KEBAB_CASE.nameForField(null, null, "SomeURLStuff"));
    }

// com.fasterxml.jackson.databind.introspect.TestNamingStrategyStd::testSimpleKebabCase
    public void testSimpleKebabCase() throws Exception
    {
        final FirstNameBean input = new FirstNameBean("Bob");
        ObjectMapper m = new ObjectMapper()
                .setPropertyNamingStrategy(PropertyNamingStrategy.KEBAB_CASE);

        assertEquals(aposToQuotes("{'first-name':'Bob'}"), m.writeValueAsString(input));

        FirstNameBean result = m.readValue(aposToQuotes("{'first-name':'Billy'}"),
                FirstNameBean.class);
        assertEquals("Billy", result.firstName);
    }

// com.fasterxml.jackson.databind.introspect.TestNamingStrategyStd::testNamingWithObjectNode
    public void testNamingWithObjectNode() throws Exception
    {
        ObjectMapper m = new ObjectMapper()
            .setPropertyNamingStrategy(PropertyNamingStrategy.LOWER_CASE);
        ClassWithObjectNodeField result =
            m.readValue(
                "{ \"id\": \"1\", \"json\": { \"foo\": \"bar\", \"baz\": \"bing\" } }",
                ClassWithObjectNodeField.class);
        assertNotNull(result);
        assertEquals("1", result.id);
        assertNotNull(result.json);
        assertEquals(2, result.json.size());
        assertEquals("bing", result.json.path("baz").asText());
    }

// com.fasterxml.jackson.databind.introspect.TestNamingStrategyStd::testExplicitRename
    public void testExplicitRename() throws Exception
    {
      ObjectMapper m = new ObjectMapper();
      m.setPropertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE);
      m.enable(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY);
      
      assertEquals(aposToQuotes("{'firstName':'Peter','lastName':'Venkman','user_age':'35'}"),
          m.writeValueAsString(new ExplicitBean()));

      m = new ObjectMapper();
      m.setPropertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE);
      m.enable(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY);
      m.enable(MapperFeature.ALLOW_EXPLICIT_PROPERTY_RENAMING);
      
      assertEquals(aposToQuotes("{'first_name':'Peter','last_name':'Venkman','user_age':'35'}"),
          m.writeValueAsString(new ExplicitBean()));

      
      ExplicitBean bean =
          m.readValue(aposToQuotes("{'first_name':'Egon','last_name':'Spengler','user_age':'32'}"),
              ExplicitBean.class);

      assertNotNull(bean);
      assertEquals("Egon", bean.userFirstName);
      assertEquals("Spengler", bean.userLastName);
      assertEquals("32", bean.userAge);
    }

// com.fasterxml.jackson.databind.introspect.TestNamingStrategyStd::testExplicitNoNaming
    public void testExplicitNoNaming() throws Exception
    {
        ObjectMapper mapper = objectMapper();
        String json = mapper.writeValueAsString(new DefaultNaming());
        assertEquals(aposToQuotes("{'someValue':3}"), json);
    }

// com.fasterxml.jackson.databind.introspect.TestPropertyConflicts::testFailWithDupProps
    public void testFailWithDupProps() throws Exception
    {
        BeanWithConflict bean = new BeanWithConflict();
        try {
            String json = objectWriter().writeValueAsString(bean);
            fail("Should have failed due to conflicting accessor definitions; got JSON = "+json);
        } catch (JsonProcessingException e) {
            verifyException(e, "Conflicting getter definitions");
        }
    }

// com.fasterxml.jackson.databind.introspect.TestPropertyConflicts::testRegularAndIsGetter
    public void testRegularAndIsGetter() throws Exception
    {
        final ObjectWriter writer = objectWriter();
        
        
        assertEquals("{\"value\":4}", writer.writeValueAsString(new Getters1A()));
        assertEquals("{\"value\":4}", writer.writeValueAsString(new Getters1B()));

        
        ObjectMapper mapper = objectMapper();
        assertEquals(1, mapper.readValue("{\"value\":1}", Getters1A.class).value);
        assertEquals(2, mapper.readValue("{\"value\":2}", Getters1B.class).value);
    }

// com.fasterxml.jackson.databind.introspect.TestPropertyConflicts::testInferredNameConflictsWithGetters
    public void testInferredNameConflictsWithGetters() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setAnnotationIntrospector(new InferingIntrospector());
        String json = mapper.writeValueAsString(new Infernal());
        assertEquals(aposToQuotes("{'name':'Bob'}"), json);
    }

// com.fasterxml.jackson.databind.introspect.TestPropertyConflicts::testInferredNameConflictsWithSetters
    public void testInferredNameConflictsWithSetters() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setAnnotationIntrospector(new InferingIntrospector());
        Infernal inf = mapper.readValue(aposToQuotes("{'stuff':'Bob'}"), Infernal.class);
        assertNotNull(inf);
    }

// com.fasterxml.jackson.databind.introspect.TestPropertyConflicts::testIssue541
    public void testIssue541() throws Exception {
        final ObjectMapper mapper = new ObjectMapper();
        mapper.disable(
                MapperFeature.AUTO_DETECT_CREATORS,
                MapperFeature.AUTO_DETECT_FIELDS,
                MapperFeature.AUTO_DETECT_GETTERS,
                MapperFeature.AUTO_DETECT_IS_GETTERS,
                MapperFeature.AUTO_DETECT_SETTERS,
                MapperFeature.USE_GETTERS_AS_SETTERS
        );
        Bean541 data = mapper.readValue("{\"str\":\"the string\"}", Bean541.class);
        if (data == null) {
            throw new IllegalStateException("data is null");
        }
        if (!"the string".equals(data.getStr())) {
            throw new IllegalStateException("bad value for data.str");
        }
    }

// com.fasterxml.jackson.databind.introspect.TestScalaLikeImplicitProperties::testValProperty
    public void testValProperty() throws Exception
    {
        ObjectMapper m = manglingMapper();

        assertEquals("{\"prop\":\"val\"}", m.writeValueAsString(new ValProperty("val")));
    }

// com.fasterxml.jackson.databind.introspect.TestScalaLikeImplicitProperties::testValWithBeanProperty
    public void testValWithBeanProperty() throws Exception
    {
        ObjectMapper m = manglingMapper();

        assertEquals("{\"prop\":\"val\"}", m.writeValueAsString(new ValWithBeanProperty("val")));
    }

// com.fasterxml.jackson.databind.introspect.TestScalaLikeImplicitProperties::testVarProperty
    public void testVarProperty() throws Exception
    {
        ObjectMapper m = manglingMapper();

        assertEquals("{\"prop\":\"var\"}", m.writeValueAsString(new VarProperty("var")));
        VarProperty result = m.readValue("{\"prop\":\"read\"}", VarProperty.class);
        assertEquals("read", result.prop());
    }

// com.fasterxml.jackson.databind.introspect.TestScalaLikeImplicitProperties::testVarWithBeanProperty
    public void testVarWithBeanProperty() throws Exception
    {
        ObjectMapper m = manglingMapper();

        assertEquals("{\"prop\":\"var\"}", m.writeValueAsString(new VarWithBeanProperty("var")));
        VarWithBeanProperty result = m.readValue("{\"prop\":\"read\"}", VarWithBeanProperty.class);
        assertEquals("read", result.prop());
    }

// com.fasterxml.jackson.databind.introspect.TestScalaLikeImplicitProperties::testGetterSetterProperty
    public void testGetterSetterProperty() throws Exception
    {
        ObjectMapper m = manglingMapper();

        assertEquals("{\"prop\":\"get/set\"}", m.writeValueAsString(new GetterSetterProperty()));
        GetterSetterProperty result = m.readValue("{\"prop\":\"read\"}", GetterSetterProperty.class);
        assertEquals("read", result.prop());
    }

// com.fasterxml.jackson.databind.introspect.TypeCoercion1592Test::testTypeCoercion1592
    public void testTypeCoercion1592() throws Exception
    {
        
        MAPPER.writeValueAsString(new Bean1592());
        Bean1592 result = MAPPER.readValue("{}", Bean1592.class);
        assertNotNull(result);
    }

// com.fasterxml.jackson.databind.jsonschema.TestReadJsonSchema::testDeserializeSimple
    public void testDeserializeSimple() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        JsonSchema schema = mapper.generateJsonSchema(Schemable.class);
        assertNotNull(schema);

        String schemaStr = mapper.writeValueAsString(schema);
        assertNotNull(schemaStr);
        JsonSchema result = mapper.readValue(schemaStr, JsonSchema.class);
        assertEquals("Trying to read from '"+schemaStr+"'", schema, result);
    }

// com.fasterxml.jackson.databind.jsontype.AbstractTypeMapping1186Test::testDeserializeMyContainer
    public void testDeserializeMyContainer() throws Exception {
        SimpleModule module = new SimpleModule().addAbstractTypeMapping(IContainer.class, MyContainer.class);
        final ObjectMapper mapper = new ObjectMapper().registerModule(module);
        String json = "{\"ts\": [ { \"msg\": \"hello\"} ] }";
        final Object o = mapper.readValue(json,
                mapper.getTypeFactory().constructParametricType(IContainer.class, MyObject.class));
        assertEquals(MyContainer.class, o.getClass());
        MyContainer<?> myc = (MyContainer<?>) o;
        assertEquals(1, myc.ts.size());
        Object value = myc.ts.get(0);
        assertEquals(MyObject.class, value.getClass());
    }

// com.fasterxml.jackson.databind.jsontype.ExistingPropertyTest::testExistingPropertySerializationFruits
    public void testExistingPropertySerializationFruits() throws Exception
    {
        Map<String,Object> result = writeAndMap(MAPPER, pinguo);
        assertEquals(3, result.size());
        assertEquals(pinguo.name, result.get("name"));
        assertEquals(pinguo.seedCount, result.get("seedCount"));
        assertEquals(pinguo.type, result.get("type"));
        
        result = writeAndMap(MAPPER, mandarin);
        assertEquals(3, result.size());
        assertEquals(mandarin.name, result.get("name"));
        assertEquals(mandarin.color, result.get("color"));
        assertEquals(mandarin.type, result.get("type"));
        
        String pinguoSerialized = MAPPER.writeValueAsString(pinguo);
        assertEquals(pinguoSerialized, pinguoJson);

        String mandarinSerialized = MAPPER.writeValueAsString(mandarin);
        assertEquals(mandarinSerialized, mandarinJson);

        String fruitWrapperSerialized = MAPPER.writeValueAsString(pinguoWrapper);
        assertEquals(fruitWrapperSerialized, pinguoWrapperJson);

        String fruitListSerialized = MAPPER.writeValueAsString(fruitList);
        assertEquals(fruitListSerialized, fruitListJson);
    }

// com.fasterxml.jackson.databind.jsontype.ExistingPropertyTest::testSimpleClassAsExistingPropertyDeserializationFruits
    public void testSimpleClassAsExistingPropertyDeserializationFruits() throws Exception
    {
        Fruit pinguoDeserialized = MAPPER.readValue(pinguoJson, Fruit.class);
        assertTrue(pinguoDeserialized instanceof Apple);
        assertSame(pinguoDeserialized.getClass(), Apple.class);
        assertEquals(pinguo.name, pinguoDeserialized.name);
        assertEquals(pinguo.seedCount, ((Apple) pinguoDeserialized).seedCount);
        assertEquals(pinguo.type, ((Apple) pinguoDeserialized).type);

        FruitWrapper pinguoWrapperDeserialized = MAPPER.readValue(pinguoWrapperJson, FruitWrapper.class);
        Fruit pinguoExtracted = pinguoWrapperDeserialized.fruit;
        assertTrue(pinguoExtracted instanceof Apple);
        assertSame(pinguoExtracted.getClass(), Apple.class);
        assertEquals(pinguo.name, pinguoExtracted.name);
        assertEquals(pinguo.seedCount, ((Apple) pinguoExtracted).seedCount);
        assertEquals(pinguo.type, ((Apple) pinguoExtracted).type);

        Fruit[] fruits = MAPPER.readValue(fruitListJson, Fruit[].class);
        assertEquals(2, fruits.length);
        assertEquals(Apple.class, fruits[0].getClass());
        assertEquals("apple", ((Apple) fruits[0]).type);
        assertEquals(Orange.class, fruits[1].getClass());
        assertEquals("orange", ((Orange) fruits[1]).type);
        
        List<Fruit> f2 = MAPPER.readValue(fruitListJson,
                new TypeReference<List<Fruit>>() { });
        assertNotNull(f2);
        assertTrue(f2.size() == 2);
        assertEquals(Apple.class, f2.get(0).getClass());
        assertEquals(Orange.class, f2.get(1).getClass());
    }

// com.fasterxml.jackson.databind.jsontype.ExistingPropertyTest::testExistingPropertySerializationAnimals
    public void testExistingPropertySerializationAnimals() throws Exception
    {
        Map<String,Object> result = writeAndMap(MAPPER, beelzebub);
        assertEquals(3, result.size());
        assertEquals(beelzebub.name, result.get("name"));
        assertEquals(beelzebub.furColor, result.get("furColor"));
        assertEquals(beelzebub.getType(), result.get("type"));

        result = writeAndMap(MAPPER, rover);
        assertEquals(3, result.size());
        assertEquals(rover.name, result.get("name"));
        assertEquals(rover.boneCount, result.get("boneCount"));
        assertEquals(rover.getType(), result.get("type"));
        
        String beelzebubSerialized = MAPPER.writeValueAsString(beelzebub);
        assertEquals(beelzebubSerialized, beelzebubJson);
        
        String roverSerialized = MAPPER.writeValueAsString(rover);
        assertEquals(roverSerialized, roverJson);
        
        String animalWrapperSerialized = MAPPER.writeValueAsString(beelzebubWrapper);
        assertEquals(animalWrapperSerialized, beelzebubWrapperJson);

        String animalListSerialized = MAPPER.writeValueAsString(animalList);
        assertEquals(animalListSerialized, animalListJson);
    }

// com.fasterxml.jackson.databind.jsontype.ExistingPropertyTest::testSimpleClassAsExistingPropertyDeserializationAnimals
    public void testSimpleClassAsExistingPropertyDeserializationAnimals() throws Exception
    {
        Animal beelzebubDeserialized = MAPPER.readValue(beelzebubJson, Animal.class);
        assertTrue(beelzebubDeserialized instanceof Cat);
        assertSame(beelzebubDeserialized.getClass(), Cat.class);
        assertEquals(beelzebub.name, beelzebubDeserialized.name);
        assertEquals(beelzebub.furColor, ((Cat) beelzebubDeserialized).furColor);
        assertEquals(beelzebub.getType(), beelzebubDeserialized.getType());

        AnimalWrapper beelzebubWrapperDeserialized = MAPPER.readValue(beelzebubWrapperJson, AnimalWrapper.class);
        Animal beelzebubExtracted = beelzebubWrapperDeserialized.animal;
        assertTrue(beelzebubExtracted instanceof Cat);
        assertSame(beelzebubExtracted.getClass(), Cat.class);
        assertEquals(beelzebub.name, beelzebubExtracted.name);
        assertEquals(beelzebub.furColor, ((Cat) beelzebubExtracted).furColor);
        assertEquals(beelzebub.getType(), beelzebubExtracted.getType());
    	
        @SuppressWarnings("unchecked")
        List<Animal> animalListDeserialized = MAPPER.readValue(animalListJson, List.class);
        assertNotNull(animalListDeserialized);
        assertTrue(animalListDeserialized.size() == 2);
        Animal cat = MAPPER.convertValue(animalListDeserialized.get(0), Animal.class);
        assertTrue(cat instanceof Cat);
        assertSame(cat.getClass(), Cat.class);
        Animal dog = MAPPER.convertValue(animalListDeserialized.get(1), Animal.class);
        assertTrue(dog instanceof Dog);
        assertSame(dog.getClass(), Dog.class);
    }

// com.fasterxml.jackson.databind.jsontype.ExistingPropertyTest::testExistingPropertySerializationCars
    public void testExistingPropertySerializationCars() throws Exception
    {
        Map<String,Object> result = writeAndMap(MAPPER, camry);
        assertEquals(3, result.size());
        assertEquals(camry.name, result.get("name"));
        assertEquals(camry.exteriorColor, result.get("exteriorColor"));
        assertEquals(camry.getType(), result.get("type"));

        result = writeAndMap(MAPPER, accord);
        assertEquals(3, result.size());
        assertEquals(accord.name, result.get("name"));
        assertEquals(accord.speakerCount, result.get("speakerCount"));
        assertEquals(accord.getType(), result.get("type"));

        String camrySerialized = MAPPER.writeValueAsString(camry);
        assertEquals(camrySerialized, camryJson);

        String accordSerialized = MAPPER.writeValueAsString(accord);
        assertEquals(accordSerialized, accordJson);
        
        String carWrapperSerialized = MAPPER.writeValueAsString(camryWrapper);
        assertEquals(carWrapperSerialized, camryWrapperJson);

        String carListSerialized = MAPPER.writeValueAsString(carList);
        assertEquals(carListSerialized, carListJson);
    }

// com.fasterxml.jackson.databind.jsontype.ExistingPropertyTest::testSimpleClassAsExistingPropertyDeserializationCars
    public void testSimpleClassAsExistingPropertyDeserializationCars() throws Exception
    {
        Car camryDeserialized = MAPPER.readValue(camryJson, Camry.class);
        assertTrue(camryDeserialized instanceof Camry);
        assertSame(camryDeserialized.getClass(), Camry.class);
        assertEquals(camry.name, camryDeserialized.name);
        assertEquals(camry.exteriorColor, ((Camry) camryDeserialized).exteriorColor);
        assertEquals(camry.getType(), ((Camry) camryDeserialized).getType());

        CarWrapper camryWrapperDeserialized = MAPPER.readValue(camryWrapperJson, CarWrapper.class);
        Car camryExtracted = camryWrapperDeserialized.car;
        assertTrue(camryExtracted instanceof Camry);
        assertSame(camryExtracted.getClass(), Camry.class);
        assertEquals(camry.name, camryExtracted.name);
        assertEquals(camry.exteriorColor, ((Camry) camryExtracted).exteriorColor);
        assertEquals(camry.getType(), ((Camry) camryExtracted).getType());

        @SuppressWarnings("unchecked")
        List<Car> carListDeserialized = MAPPER.readValue(carListJson, List.class);
        assertNotNull(carListDeserialized);
        assertTrue(carListDeserialized.size() == 2);
        Car result = MAPPER.convertValue(carListDeserialized.get(0), Car.class);
        assertTrue(result instanceof Camry);
        assertSame(result.getClass(), Camry.class);

        result = MAPPER.convertValue(carListDeserialized.get(1), Car.class);
        assertTrue(result instanceof Accord);
        assertSame(result.getClass(), Accord.class);
    }

// com.fasterxml.jackson.databind.jsontype.ExistingPropertyTest::testExistingEnumTypeId
    public void testExistingEnumTypeId() throws Exception
    {
        Bean1635 result = MAPPER.readValue(aposToQuotes("{'value':3, 'type':'A'}"),
                Bean1635.class);
        assertEquals(Bean1635A.class, result.getClass());
        Bean1635A bean = (Bean1635A) result;
        assertEquals(3, bean.value);
        assertEquals(ABC.A, bean.type);
    }

// com.fasterxml.jackson.databind.jsontype.ExistingPropertyTest::testExistingEnumTypeIdViaDefault
    public void testExistingEnumTypeIdViaDefault() throws Exception
    {
        Bean1635 result = MAPPER.readValue(aposToQuotes("{'type':'C'}"),
                Bean1635.class);
        assertEquals(Bean1635Default.class, result.getClass());
        assertEquals(ABC.C, result.type);
    }

// com.fasterxml.jackson.databind.jsontype.Generic1128Test::testIssue1128
    public void testIssue1128() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);

        final DevMContainer devMContainer1 = new DevMContainer();
        final DevM entity = new DevM();
        final Dev parent = new Dev();
        parent.id = 2L;
        entity.parent = parent;
        devMContainer1.entity = entity;
    
        String json = mapper.writeValueAsString(devMContainer1);

        final DevMContainer devMContainer = mapper.readValue(json, DevMContainer.class);
        long id = devMContainer.entity.parent.id;

        assertEquals(2, id);
    }

// com.fasterxml.jackson.databind.jsontype.GenericTypeId1735Test::testSimpleTypeCheck1735
    public void testSimpleTypeCheck1735() throws Exception
    {
        try {
            MAPPER.readValue(aposToQuotes(
"{'w':{'type':'"+NEF_CLASS+"'}}"),
                    Wrapper1735.class);
            fail("Should not pass");
        } catch (InvalidTypeIdException e) {
            verifyException(e, "could not resolve type id");
            verifyException(e, "not a subtype");
        }
    }

// com.fasterxml.jackson.databind.jsontype.GenericTypeId1735Test::testNestedTypeCheck1735
    public void testNestedTypeCheck1735() throws Exception
    {
        try {
            MAPPER.readValue(aposToQuotes(
"{'w':{'type':'java.util.HashMap<java.lang.String,java.lang.String>'}}"),
                    Wrapper1735.class);
            fail("Should not pass");
        } catch (InvalidTypeIdException e) {
            verifyException(e, "could not resolve type id");
            verifyException(e, "not a subtype");
        }
    }

// com.fasterxml.jackson.databind.jsontype.NoTypeInfoTest::testWithIdNone
    public void testWithIdNone() throws Exception
    {
        final ObjectMapper mapper = newObjectMapper();
        mapper.enableDefaultTyping();
        
        String json = mapper.writeValueAsString(new NoType());
        assertEquals("{\"a\":3}", json);

        
        NoTypeInterface bean = mapper.readValue("{\"a\":6}", NoTypeInterface.class);
        assertNotNull(bean);
        NoType impl = (NoType) bean;
        assertEquals(6, impl.a);
    }

// com.fasterxml.jackson.databind.jsontype.PolymorphicList1451SerTest::testCollectionWithTypeInfo
    public void testCollectionWithTypeInfo() throws Exception {
        ObjectMapper mapper = new ObjectMapper()
                .disable(SerializationFeature.EAGER_SERIALIZER_FETCH)

                ;

        List<A> input = new ArrayList<A>();
        A a = new A();
        a.a = "a1";
        input.add(a);

        B b = new B();
        b.b = "b";
        b.a = "a2";
        input.add(b);

        final TypeReference<?> typeRef = 
                new TypeReference<Collection<A>>(){};
        ObjectWriter writer = mapper.writerFor(typeRef);

        String result = writer.writeValueAsString(input);

        assertEquals(aposToQuotes(
"[{'@class':'."+CLASS_NAME+"$A','a':'a1'},{'@class':'."+CLASS_NAME+"$B','a':'a2','b':'b'}]"
), result);

        List<A> output = mapper.readerFor(typeRef)
                .readValue(result);
        assertEquals(2, output.size());
        assertEquals(A.class, output.get(0).getClass());
        assertEquals(B.class, output.get(1).getClass());
    }

// com.fasterxml.jackson.databind.jsontype.PolymorphicViaRefTypeTest::testPolymorphicAtomicRefProperty
    public void testPolymorphicAtomicRefProperty() throws Exception
    {
        TypeInfoAtomic data = new TypeInfoAtomic();
        data.value = new AtomicReference<BaseForAtomic>(new ImplForAtomic(42));
        String json = MAPPER.writeValueAsString(data);
        TypeInfoAtomic result = MAPPER.readValue(json, TypeInfoAtomic.class);
        assertNotNull(result);
        BaseForAtomic value = result.value.get();
        assertNotNull(value);
        assertEquals(ImplForAtomic.class, value.getClass());
        assertEquals(42, ((ImplForAtomic) value).x);
    }

// com.fasterxml.jackson.databind.jsontype.PolymorphicViaRefTypeTest::testAtomicRefViaDefaultTyping
    public void testAtomicRefViaDefaultTyping() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        mapper.enableDefaultTyping(DefaultTyping.NON_FINAL);
        AtomicStringWrapper data = new AtomicStringWrapper("foo");
        String json = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(data);
        AtomicStringWrapper result = mapper.readValue(json, AtomicStringWrapper.class);
        assertNotNull(result);
        assertNotNull(result.wrapper);
        assertEquals(AtomicReference.class, result.wrapper.getClass());
        StringWrapper w = result.wrapper.get();
        assertEquals("foo", w.str);
    }

// com.fasterxml.jackson.databind.jsontype.TestAbstractContainers::testAbstractLists
    public void testAbstractLists() throws Exception
    {
        ListWrapper w = new ListWrapper();
        w.list.add("x");

        String json = MAPPER.writeValueAsString(w);
        Object o = MAPPER.readValue(json, ListWrapper.class);
        assertEquals(ListWrapper.class, o.getClass());
        ListWrapper out = (ListWrapper) o;
        assertNotNull(out.list);
        assertEquals(1, out.list.size());
        assertEquals("x", out.list.get(0));
   }

// com.fasterxml.jackson.databind.jsontype.TestAbstractContainers::testAbstractMaps
    public void testAbstractMaps() throws Exception
    {
        MapWrapper w = new MapWrapper();
        w.map.put("key1", "name1");

        String json = MAPPER.writeValueAsString(w);
        Object o = MAPPER.readValue(json, MapWrapper.class);
        assertEquals(MapWrapper.class, o.getClass());
        MapWrapper out = (MapWrapper) o;
        assertEquals(1, out.map.size());
   }

// com.fasterxml.jackson.databind.jsontype.TestAbstractTypeNames::testEmptyCollection
    public void testEmptyCollection() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(SerializationFeature.INDENT_OUTPUT, true);
        List<User>friends = new ArrayList<User>();
        friends.add(new DefaultUser("Joe Hildebrandt", null));
        friends.add(new DefaultEmployee("Richard Nasr",null,"MDA"));

        User user = new DefaultEmployee("John Vanspronssen", friends, "MDA");
        String json = mapper.writeValueAsString(user);

        
        mapper = new ObjectMapper();
        mapper.registerSubtypes(DefaultEmployee.class);
        mapper.registerSubtypes(DefaultUser.class);
        
        User result = mapper.readValue(json, User.class);
        assertNotNull(result);
        assertEquals(DefaultEmployee.class, result.getClass());

        friends = result.getFriends();
        assertEquals(2, friends.size());
        assertEquals(DefaultUser.class, friends.get(0).getClass());
        assertEquals(DefaultEmployee.class, friends.get(1).getClass());
    }

// com.fasterxml.jackson.databind.jsontype.TestAbstractTypeNames::testInnerClassWithType
    public void testInnerClassWithType() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        mapper.enableDefaultTyping(DefaultTyping.NON_FINAL);
        String json = mapper.writeValueAsString(new BeanWithAnon());
        BeanWithAnon result = mapper.readValue(json, BeanWithAnon.class);
        assertEquals(BeanWithAnon.class, result.getClass());
    }

// com.fasterxml.jackson.databind.jsontype.TestBaseTypeAsDefault::testPositiveForParent
    public void testPositiveForParent() throws IOException {
        Object o = MAPPER_WITH_BASE.readerFor(Parent.class).readValue("{}");
        assertEquals(o.getClass(), Parent.class);
    }

// com.fasterxml.jackson.databind.jsontype.TestBaseTypeAsDefault::testPositiveForChild
    public void testPositiveForChild() throws IOException {
        Object o = MAPPER_WITH_BASE.readerFor(Child.class).readValue("{}");
        assertEquals(o.getClass(), Child.class);
    }

// com.fasterxml.jackson.databind.jsontype.TestBaseTypeAsDefault::testNegativeForParent
    public void testNegativeForParent() throws IOException {
        try {
             MAPPER_WITHOUT_BASE.readerFor(Parent.class).readValue("{}");
            fail("Should not pass");
        } catch (JsonMappingException ex) {
            assertTrue(ex.getMessage().contains("missing type id property '@class'"));
        }
    }

// com.fasterxml.jackson.databind.jsontype.TestBaseTypeAsDefault::testNegativeForChild
    public void testNegativeForChild() throws IOException {
        try {
             MAPPER_WITHOUT_BASE.readerFor(Child.class).readValue("{}");
            fail("Should not pass");
        } catch (JsonMappingException ex) {
            assertTrue(ex.getMessage().contains("missing type id property '@class'"));
        }
    }

// com.fasterxml.jackson.databind.jsontype.TestBaseTypeAsDefault::testConversionForAbstractWithDefault
    public void testConversionForAbstractWithDefault() throws IOException {
        
        Object o = MAPPER_WITH_BASE.readerFor(AbstractParentWithDefault.class).readValue("{}");
        assertEquals(o.getClass(), ChildOfChild.class);
    }

// com.fasterxml.jackson.databind.jsontype.TestBaseTypeAsDefault::testPositiveWithTypeSpecification
    public void testPositiveWithTypeSpecification() throws IOException {
        Object o = MAPPER_WITH_BASE.readerFor(Parent.class)
                .readValue("{\"@class\":\""+Child.class.getName()+"\"}");
        assertEquals(o.getClass(), Child.class);
    }

// com.fasterxml.jackson.databind.jsontype.TestBaseTypeAsDefault::testPositiveWithManualDefault
    public void testPositiveWithManualDefault() throws IOException {
        Object o = MAPPER_WITH_BASE.readerFor(ChildOfAbstract.class).readValue("{}");

        assertEquals(o.getClass(), ChildOfChild.class);
    }

// com.fasterxml.jackson.databind.jsontype.TestCustomTypeIdResolver::testCustomTypeIdResolver
    public void testCustomTypeIdResolver() throws Exception
    {
        List<JavaType> types = new ArrayList<JavaType>();
        CustomResolver.initTypes = types;
        String json = MAPPER.writeValueAsString(new CustomBean[] { new CustomBeanImpl(28) });
        assertEquals("[{\"*\":{\"x\":28}}]", json);
        assertEquals(1, types.size());
        assertEquals(CustomBean.class, types.get(0).getRawClass());

        types = new ArrayList<JavaType>();
        CustomResolver.initTypes = types;
        CustomBean[] result = MAPPER.readValue(json, CustomBean[].class);
        assertNotNull(result);
        assertEquals(1, result.length);
        assertEquals(28, ((CustomBeanImpl) result[0]).x);
        assertEquals(1, types.size());
        assertEquals(CustomBean.class, types.get(0).getRawClass());
    }

// com.fasterxml.jackson.databind.jsontype.TestCustomTypeIdResolver::testCustomWithExternal
    public void testCustomWithExternal() throws Exception
    {
        ExtBeanWrapper w = new ExtBeanWrapper();
        w.value = new ExtBeanImpl(12);

        String json = MAPPER.writeValueAsString(w);

        ExtBeanWrapper out = MAPPER.readValue(json, ExtBeanWrapper.class);
        assertNotNull(out);
        
        assertEquals(12, ((ExtBeanImpl) out.value).y);
    }

// com.fasterxml.jackson.databind.jsontype.TestCustomTypeIdResolver::testPolymorphicTypeViaCustom
    public void testPolymorphicTypeViaCustom() throws Exception {
        Base1270<Poly1> req = new Base1270<Poly1>();
        Poly1 o = new Poly1();
        o.val = "optionValue";
        req.options = o;
        req.val = "some value";
        Top1270 top = new Top1270();
        top.b = req;
        String json = MAPPER.writeValueAsString(top);
        JsonNode tree = MAPPER.readTree(json);
        assertNotNull(tree.get("b"));
        assertNotNull(tree.get("b").get("options"));
        assertNotNull(tree.get("b").get("options").get("val"));

        
        Top1270 itemRead = MAPPER.readValue(json, Top1270.class);
        assertNotNull(itemRead);
        assertNotNull(itemRead.b);
    }

// com.fasterxml.jackson.databind.jsontype.TestEnumTyping::testTagList
    public void testTagList() throws Exception
    {
        ObjectMapper m = new ObjectMapper();
        TagList list = new TagList();
        list.add(Tag.A);
        list.add(Tag.B);
        String json = m.writeValueAsString(list);

        TagList result = m.readValue(json, TagList.class);
        assertEquals(2, result.size());
        assertSame(Tag.A, result.get(0));
        assertSame(Tag.B, result.get(1));
    }

// com.fasterxml.jackson.databind.jsontype.TestEnumTyping::testEnumInterface
    public void testEnumInterface() throws Exception
    {
        ObjectMapper m = new ObjectMapper();
        String json = m.writeValueAsString(Tag.B);
        EnumInterface result = m.readValue(json, EnumInterface.class);
        assertSame(Tag.B, result);
    }

// com.fasterxml.jackson.databind.jsontype.TestEnumTyping::testEnumInterfaceList
    public void testEnumInterfaceList() throws Exception
    {
        ObjectMapper m = new ObjectMapper();
        EnumInterfaceList list = new EnumInterfaceList();
        list.add(Tag.A);
        list.add(Tag.B);
        String json = m.writeValueAsString(list);
        
        EnumInterfaceList result = m.readValue(json, EnumInterfaceList.class);
        assertEquals(2, result.size());
        assertSame(Tag.A, result.get(0));
        assertSame(Tag.B, result.get(1));
    }

// com.fasterxml.jackson.databind.jsontype.TestEnumTyping::testUntypedEnum
    public void testUntypedEnum() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        String str = mapper.writeValueAsString(new UntypedEnumBean(TestEnum.B));
        UntypedEnumBean result = mapper.readValue(str, UntypedEnumBean.class);
        assertNotNull(result);
        assertNotNull(result.value);
        Object ob = result.value;
        assertSame(TestEnum.class, ob.getClass());
        assertEquals(TestEnum.B, result.value);
    }

// com.fasterxml.jackson.databind.jsontype.TestGenericListSerialization::testSubTypesFor356
    public void testSubTypesFor356() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        
        JSONResponse<List<Parent>> input = new JSONResponse<List<Parent>>();

        List<Parent> embedded = new ArrayList<Parent>();
        embedded.add(new Child1());
        embedded.add(new Child2());
        input.setResult(embedded);
        mapper.configure(MapperFeature.USE_STATIC_TYPING, true);

        JavaType rootType = TypeFactory.defaultInstance().constructType(new TypeReference<JSONResponse<List<Parent>>>() { });
        byte[] json = mapper.writerFor(rootType).writeValueAsBytes(input);
        
        JSONResponse<List<Parent>> out = mapper.readValue(json, 0, json.length, rootType);

        List<Parent> deserializedContent = out.getResult();

        assertEquals(2, deserializedContent.size());
        assertTrue(deserializedContent.get(0) instanceof Parent);
        assertTrue(deserializedContent.get(0) instanceof Child1);
        assertFalse(deserializedContent.get(0) instanceof Child2);
        assertTrue(deserializedContent.get(1) instanceof Child2);
        assertFalse(deserializedContent.get(1) instanceof Child1);

        assertEquals("PARENT", ((Child1) deserializedContent.get(0)).parentContent);
        assertEquals("PARENT", ((Child2) deserializedContent.get(1)).parentContent);
        assertEquals("CHILD1", ((Child1) deserializedContent.get(0)).childContent1);
        assertEquals("CHILD2", ((Child2) deserializedContent.get(1)).childContent2);
    }

// com.fasterxml.jackson.databind.jsontype.TestOverlappingTypeIdNames::testOverlappingNameDeser
    public void testOverlappingNameDeser() throws Exception
    {
        Base312 value;

        

        value = MAPPER.readValue(aposToQuotes("{'type':'a','x':7}"), Base312.class);
        assertNotNull(value);
        assertEquals(Impl312.class, value.getClass());
        assertEquals(7, ((Impl312) value).x);
        
        value = MAPPER.readValue(aposToQuotes("{'type':'b','x':3}"), Base312.class);
        assertNotNull(value);
        assertEquals(Impl312.class, value.getClass());
        assertEquals(3, ((Impl312) value).x);
    }

// com.fasterxml.jackson.databind.jsontype.TestOverlappingTypeIdNames::testOverlappingNameSer
    public void testOverlappingNameSer() throws Exception
    {
        assertEquals(aposToQuotes("{'type':'a','value':1}"),
                MAPPER.writeValueAsString(new Impl312B1()));
        assertEquals(aposToQuotes("{'type':'a','value':1}"),
                MAPPER.writeValueAsString(new Impl312B2()));
    }

// com.fasterxml.jackson.databind.jsontype.TestPolymorphicDeserialization676::testDeSerFail
    public void testDeSerFail() throws IOException {
        ObjectMapper mapper = new ObjectMapper();

        MapContainer deserMapBad = createDeSerMapContainer(originMap, mapper);
        assertEquals(originMap, deserMapBad);
        assertEquals(originMap,
                mapper.readValue(mapper.writeValueAsString(originMap), MapContainer.class));
    }

// com.fasterxml.jackson.databind.jsontype.TestPolymorphicDeserialization676::testDeSerCorrect
    public void testDeSerCorrect() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS);
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("1", 1);
        
        assertEquals(new MapContainer(map),
                mapper.readValue(mapper.writeValueAsString(new MapContainer(map)),
                        MapContainer.class));

        MapContainer deserMapGood = createDeSerMapContainer(originMap, mapper);

        assertEquals(originMap, deserMapGood);
        assertEquals(new Date(TIMESTAMP), deserMapGood.map.get("DateValue"));

        assertEquals(originMap, mapper.readValue(mapper.writeValueAsString(originMap), MapContainer.class));
    }

// com.fasterxml.jackson.databind.jsontype.TestPolymorphicWithDefaultImpl::testDeserializationWithObject
    public void testDeserializationWithObject() throws Exception
    {
        Inter inter = MAPPER.readerFor(Inter.class).readValue("{\"type\": \"mine\", \"blah\": [\"a\", \"b\", \"c\"]}");
        assertTrue(inter instanceof MyInter);
        assertFalse(inter instanceof LegacyInter);
        assertEquals(Arrays.asList("a", "b", "c"), ((MyInter) inter).blah);
    }

// com.fasterxml.jackson.databind.jsontype.TestPolymorphicWithDefaultImpl::testDeserializationWithString
    public void testDeserializationWithString() throws Exception
    {
        Inter inter = MAPPER.readerFor(Inter.class).readValue("\"a,b,c,d\"");
        assertTrue(inter instanceof LegacyInter);
        assertEquals(Arrays.asList("a", "b", "c", "d"), ((MyInter) inter).blah);
    }

// com.fasterxml.jackson.databind.jsontype.TestPolymorphicWithDefaultImpl::testDeserializationWithArray
    public void testDeserializationWithArray() throws Exception
    {
        Inter inter = MAPPER.readerFor(Inter.class).readValue("[\"a\", \"b\", \"c\", \"d\"]");
        assertTrue(inter instanceof LegacyInter);
        assertEquals(Arrays.asList("a", "b", "c", "d"), ((MyInter) inter).blah);
    }

// com.fasterxml.jackson.databind.jsontype.TestPolymorphicWithDefaultImpl::testDeserializationWithArrayOfSize2
    public void testDeserializationWithArrayOfSize2() throws Exception
    {
        Inter inter = MAPPER.readerFor(Inter.class).readValue("[\"a\", \"b\"]");
        assertTrue(inter instanceof LegacyInter);
        assertEquals(Arrays.asList("a", "b"), ((MyInter) inter).blah);
    }

// com.fasterxml.jackson.databind.jsontype.TestPolymorphicWithDefaultImpl::testDefaultAsNoClass
    public void testDefaultAsNoClass() throws Exception
    {
        Object ob = MAPPER.readerFor(DefaultWithNoClass.class).readValue("{ }");
        assertNull(ob);
        ob = MAPPER.readerFor(DefaultWithNoClass.class).readValue("{ \"bogus\":3 }");
        assertNull(ob);
    }

// com.fasterxml.jackson.databind.jsontype.TestPolymorphicWithDefaultImpl::testDefaultAsVoid
    public void testDefaultAsVoid() throws Exception
    {
        Object ob = MAPPER.readerFor(DefaultWithVoidAsDefault.class).readValue("{ }");
        assertNull(ob);
        ob = MAPPER.readerFor(DefaultWithVoidAsDefault.class).readValue("{ \"bogus\":3 }");
        assertNull(ob);
    }

// com.fasterxml.jackson.databind.jsontype.TestPolymorphicWithDefaultImpl::testBadTypeAsNull
    public void testBadTypeAsNull() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        mapper.disable(DeserializationFeature.FAIL_ON_INVALID_SUBTYPE);
        Object ob = mapper.readValue("{}", MysteryPolymorphic.class);
        assertNull(ob);
        ob = mapper.readValue("{ \"whatever\":13}", MysteryPolymorphic.class);
        assertNull(ob);
    }

// com.fasterxml.jackson.databind.jsontype.TestPolymorphicWithDefaultImpl::testInvalidTypeId511
    public void testInvalidTypeId511() throws Exception {
        ObjectReader reader = MAPPER.reader().without(
                DeserializationFeature.FAIL_ON_INVALID_SUBTYPE,
                DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,
                DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES
        );
        String json = "{\"many\":[{\"sub1\":{\"a\":\"foo\"}},{\"sub2\":{\"b\":\"bar\"}}]}" ;
        Good goodResult = reader.forType(Good.class).readValue(json) ;
        assertNotNull(goodResult) ;
        Bad badResult = reader.forType(Bad.class).readValue(json);
        assertNotNull(badResult);
    }

// com.fasterxml.jackson.databind.jsontype.TestPolymorphicWithDefaultImpl::testDefaultImplWithObjectWrapper
    public void testDefaultImplWithObjectWrapper() throws Exception
    {
        BaseFor656 value = MAPPER.readValue(aposToQuotes("{'foobar':{'a':3}}"), BaseFor656.class);
        assertNotNull(value);
        assertEquals(ImplFor656.class, value.getClass());
        assertEquals(3, ((ImplFor656) value).a);
    }

// com.fasterxml.jackson.databind.jsontype.TestPolymorphicWithDefaultImpl::testUnknownTypeIDRecovery
    public void testUnknownTypeIDRecovery() throws Exception
    {
        ObjectReader reader = MAPPER.readerFor(CallRecord.class).without(
                DeserializationFeature.FAIL_ON_INVALID_SUBTYPE);
        String json = aposToQuotes("{'version':0.0,'application':'123',"
                +"'item':{'type':'xevent','location':'location1'},"
                +"'item2':{'type':'event','location':'location1'}}");
        
        CallRecord r = reader.readValue(json);
        assertNull(r.item);
        assertNotNull(r.item2);

        json = aposToQuotes("{'item':{'type':'xevent','location':'location1'}, 'version':0.0,'application':'123'}");
        CallRecord r3 = reader.readValue(json);
        assertNull(r3.item);
        assertEquals("123", r3.application);
    }

// com.fasterxml.jackson.databind.jsontype.TestPolymorphicWithDefaultImpl::testUnknownClassAsSubtype
    public void testUnknownClassAsSubtype() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_INVALID_SUBTYPE, false);
        BaseWrapper w = mapper.readValue(aposToQuotes
                ("{'value':{'clazz':'com.foobar.Nothing'}}'"),
                BaseWrapper.class);
        assertNotNull(w);
        assertNull(w.value);
    }

// com.fasterxml.jackson.databind.jsontype.TestPolymorphicWithDefaultImpl::testWithoutEmptyStringAsNullObject1533
    public void testWithoutEmptyStringAsNullObject1533() throws Exception
    {
        ObjectReader r = MAPPER.readerFor(AsPropertyWrapper.class)
                .without(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT);
        try {
            r.readValue("{ \"value\": \"\" }");
            fail("Expected " + JsonMappingException.class);
        } catch (InvalidTypeIdException e) {
            verifyException(e, "missing type id property 'type'");
        }
    }

// com.fasterxml.jackson.databind.jsontype.TestPolymorphicWithDefaultImpl::testWithEmptyStringAsNullObject1533
    public void testWithEmptyStringAsNullObject1533() throws Exception
    {
        ObjectReader r = MAPPER.readerFor(AsPropertyWrapper.class)
                .with(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT);
        AsPropertyWrapper wrapper = r.readValue("{ \"value\": \"\" }");
        assertNull(wrapper.value);
    }

// com.fasterxml.jackson.databind.jsontype.TestPolymorphicWithDefaultImpl1565::testIncompatibleDefaultImpl1565
    public void testIncompatibleDefaultImpl1565() throws Exception
    {
        String value = "{\"typeInfo\": \"derived\", \"name\": \"John\", \"description\": \"Owner\"}";
        CDerived1565 result = MAPPER.readValue(value, CDerived1565.class);
        assertNotNull(result);
    }

// com.fasterxml.jackson.databind.jsontype.TestPolymorphicWithDefaultImpl1565::testWithIncompatibleTargetType1861
    public void testWithIncompatibleTargetType1861() throws Exception
    {
        
        Impl1861A result = MAPPER.readValue(aposToQuotes("{'type':'a','base':'foo','valueA':3}"),
                Impl1861A.class);
        assertNotNull(result);
    }

// com.fasterxml.jackson.databind.jsontype.TestPropertyTypeInfo::testSimpleField
    public void testSimpleField() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        String json = mapper.writeValueAsString(new FieldWrapperBean(new StringWrapper("foo")));

        FieldWrapperBean bean = mapper.readValue(json, FieldWrapperBean.class);
        assertNotNull(bean.value);
        assertEquals(StringWrapper.class, bean.value.getClass());
        assertEquals(((StringWrapper) bean.value).str, "foo");
    }

// com.fasterxml.jackson.databind.jsontype.TestPropertyTypeInfo::testSimpleMethod
    public void testSimpleMethod() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        String json = mapper.writeValueAsString(new FieldWrapperBean(new IntWrapper(37)));

        FieldWrapperBean bean = mapper.readValue(json, FieldWrapperBean.class);
        assertNotNull(bean.value);
        assertEquals(IntWrapper.class, bean.value.getClass());
        assertEquals(((IntWrapper) bean.value).i, 37);
    }

// com.fasterxml.jackson.databind.jsontype.TestPropertyTypeInfo::testSimpleListField
    public void testSimpleListField() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        FieldWrapperBeanList list = new FieldWrapperBeanList();
        list.add(new FieldWrapperBean(new OtherBean()));
        String json = mapper.writeValueAsString(list);

        FieldWrapperBeanList result = mapper.readValue(json, FieldWrapperBeanList.class);
        assertNotNull(result);
        assertEquals(1, result.size());
        FieldWrapperBean bean = list.get(0);
        assertEquals(OtherBean.class, bean.value.getClass());
        assertEquals(((OtherBean) bean.value).x, 1);
        assertEquals(((OtherBean) bean.value).y, 1);
    }

// com.fasterxml.jackson.databind.jsontype.TestPropertyTypeInfo::testSimpleListMethod
    public void testSimpleListMethod() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        MethodWrapperBeanList list = new MethodWrapperBeanList();
        list.add(new MethodWrapperBean(new BooleanValue(true)));
        list.add(new MethodWrapperBean(new StringWrapper("x")));
        list.add(new MethodWrapperBean(new OtherBean()));
        String json = mapper.writeValueAsString(list);
        MethodWrapperBeanList result = mapper.readValue(json, MethodWrapperBeanList.class);
        assertNotNull(result);
        assertEquals(3, result.size());
        MethodWrapperBean bean = result.get(0);
        assertEquals(BooleanValue.class, bean.value.getClass());
        assertEquals(((BooleanValue) bean.value).b, Boolean.TRUE);
        bean = result.get(1);
        assertEquals(StringWrapper.class, bean.value.getClass());
        assertEquals(((StringWrapper) bean.value).str, "x");
        bean = result.get(2);
        assertEquals(OtherBean.class, bean.value.getClass());
    }

// com.fasterxml.jackson.databind.jsontype.TestPropertyTypeInfo::testSimpleArrayField
    public void testSimpleArrayField() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        FieldWrapperBeanArray array = new FieldWrapperBeanArray(new
                FieldWrapperBean[] { new FieldWrapperBean(new BooleanValue(true)) });
        String json = mapper.writeValueAsString(array);
        FieldWrapperBeanArray result = mapper.readValue(json, FieldWrapperBeanArray.class);
        assertNotNull(result);
        FieldWrapperBean[] beans = result.beans;
        assertEquals(1, beans.length);
        FieldWrapperBean bean = beans[0];
        assertEquals(BooleanValue.class, bean.value.getClass());
        assertEquals(((BooleanValue) bean.value).b, Boolean.TRUE);
    }

// com.fasterxml.jackson.databind.jsontype.TestPropertyTypeInfo::testSimpleArrayMethod
    public void testSimpleArrayMethod() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        MethodWrapperBeanArray array = new MethodWrapperBeanArray(new
                MethodWrapperBean[] { new MethodWrapperBean(new StringWrapper("A")) });
        String json = mapper.writeValueAsString(array);
        MethodWrapperBeanArray result = mapper.readValue(json, MethodWrapperBeanArray.class);
        assertNotNull(result);
        MethodWrapperBean[] beans = result.beans;
        assertEquals(1, beans.length);
        MethodWrapperBean bean = beans[0];
        assertEquals(StringWrapper.class, bean.value.getClass());
        assertEquals(((StringWrapper) bean.value).str, "A");
    }

// com.fasterxml.jackson.databind.jsontype.TestPropertyTypeInfo::testSimpleMapField
    public void testSimpleMapField() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        FieldWrapperBeanMap map = new FieldWrapperBeanMap();
        map.put("foop", new FieldWrapperBean(new IntWrapper(13)));
        String json = mapper.writeValueAsString(map);
        FieldWrapperBeanMap result = mapper.readValue(json, FieldWrapperBeanMap.class);
        assertNotNull(result);
        assertEquals(1, result.size());
        FieldWrapperBean bean = result.get("foop");
        assertNotNull(bean);
        Object ob = bean.value;
        assertEquals(IntWrapper.class, ob.getClass());
        assertEquals(((IntWrapper) ob).i, 13);
    }

// com.fasterxml.jackson.databind.jsontype.TestPropertyTypeInfo::testSimpleMapMethod
    public void testSimpleMapMethod() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        MethodWrapperBeanMap map = new MethodWrapperBeanMap();
        map.put("xyz", new MethodWrapperBean(new BooleanValue(true)));
        String json = mapper.writeValueAsString(map);
        MethodWrapperBeanMap result = mapper.readValue(json, MethodWrapperBeanMap.class);
        assertNotNull(result);
        assertEquals(1, result.size());
        MethodWrapperBean bean = result.get("xyz");
        assertNotNull(bean);
        Object ob = bean.value;
        assertEquals(BooleanValue.class, ob.getClass());
        assertEquals(((BooleanValue) ob).b, Boolean.TRUE);
    }

// com.fasterxml.jackson.databind.jsontype.TestScalars::testScalarsWithTyping
    public void testScalarsWithTyping() throws Exception
    {
        String json;
        DynamicWrapper result;
        ObjectMapper m = MAPPER;

        
        json = m.writeValueAsString(new DynamicWrapper(Integer.valueOf(3)));
        result = m.readValue(json, DynamicWrapper.class);
        assertEquals(Integer.valueOf(3), result.value);

        json = m.writeValueAsString(new DynamicWrapper("abc"));
        result = m.readValue(json, DynamicWrapper.class);
        assertEquals("abc", result.value);

        json = m.writeValueAsString(new DynamicWrapper("abc"));
        result = m.readValue(json, DynamicWrapper.class);
        assertEquals("abc", result.value);

        json = m.writeValueAsString(new DynamicWrapper(Boolean.TRUE));
        result = m.readValue(json, DynamicWrapper.class);
        assertEquals(Boolean.TRUE, result.value);
        
        
        json = m.writeValueAsString(new DynamicWrapper(Long.valueOf(7L)));
        result = m.readValue(json, DynamicWrapper.class);
        assertEquals(Long.valueOf(7), result.value);

        json = m.writeValueAsString(new DynamicWrapper(TestEnum.B));
        result = m.readValue(json, DynamicWrapper.class);
        assertEquals(TestEnum.B, result.value);
    }

// com.fasterxml.jackson.databind.jsontype.TestScalars::testScalarsViaAbstractType
    public void testScalarsViaAbstractType() throws Exception
    {
        ObjectMapper m = MAPPER;
        String json;
        AbstractWrapper result;

        
        json = m.writeValueAsString(new AbstractWrapper(Integer.valueOf(3)));
        result = m.readValue(json, AbstractWrapper.class);
        assertEquals(Integer.valueOf(3), result.value);

        json = m.writeValueAsString(new AbstractWrapper("abc"));
        result = m.readValue(json, AbstractWrapper.class);
        assertEquals("abc", result.value);

        json = m.writeValueAsString(new AbstractWrapper("abc"));
        result = m.readValue(json, AbstractWrapper.class);
        assertEquals("abc", result.value);

        json = m.writeValueAsString(new AbstractWrapper(Boolean.TRUE));
        result = m.readValue(json, AbstractWrapper.class);
        assertEquals(Boolean.TRUE, result.value);
        
        
        json = m.writeValueAsString(new AbstractWrapper(Long.valueOf(7L)));
        result = m.readValue(json, AbstractWrapper.class);
        assertEquals(Long.valueOf(7), result.value);

        json = m.writeValueAsString(new AbstractWrapper(TestEnum.B));
        result = m.readValue(json, AbstractWrapper.class);
        assertEquals(TestEnum.B, result.value);
    }

// com.fasterxml.jackson.databind.jsontype.TestScalars::testHeterogenousStringScalars
    public void testHeterogenousStringScalars() throws Exception
    {
        final UUID NULL_UUID = UUID.fromString("00000000-0000-0000-0000-000000000000");
        ScalarList input = new ScalarList()
                .add("Test")
                .add(java.lang.Object.class)
                .add(NULL_UUID)
                ;
        String json = MAPPER.writeValueAsString(input);

        ScalarList result = MAPPER.readValue(json, ScalarList.class);
        assertNotNull(result.values);
        assertEquals(3, result.values.size());
        assertEquals("Test", result.values.get(0));
        assertEquals(Object.class, result.values.get(1));
        assertEquals(NULL_UUID, result.values.get(2));
    }

// com.fasterxml.jackson.databind.jsontype.TestSubtypes::testPropertyWithSubtypes
    public void testPropertyWithSubtypes() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        
        mapper.registerSubtypes(SubB.class, SubC.class, SubD.class);
        String json = mapper.writeValueAsString(new PropertyBean(new SubC()));
        PropertyBean result = mapper.readValue(json, PropertyBean.class);
        assertSame(SubC.class, result.value.getClass());
    }

// com.fasterxml.jackson.databind.jsontype.TestSubtypes::testSubtypesViaModule
    public void testSubtypesViaModule() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        SimpleModule module = new SimpleModule();
        module.registerSubtypes(SubB.class, SubC.class, SubD.class);
        mapper.registerModule(module);
        String json = mapper.writeValueAsString(new PropertyBean(new SubC()));
        PropertyBean result = mapper.readValue(json, PropertyBean.class);
        assertSame(SubC.class, result.value.getClass());

        
        mapper = new ObjectMapper();
        module = new SimpleModule();
        List<Class<?>> l = new ArrayList<>();
        l.add(SubB.class);
        l.add(SubC.class);
        l.add(SubD.class);
        module.registerSubtypes(l);
        mapper.registerModule(module);
        json = mapper.writeValueAsString(new PropertyBean(new SubC()));
        result = mapper.readValue(json, PropertyBean.class);
        assertSame(SubC.class, result.value.getClass());
    }

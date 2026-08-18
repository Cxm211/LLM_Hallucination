// buggy code
    protected Date parseAsISO8601(String dateStr, ParsePosition pos)
    {
        /* 21-May-2009, tatu: DateFormat has very strict handling of
         * timezone  modifiers for ISO-8601. So we need to do some scrubbing.
         */

        /* First: do we have "zulu" format ('Z' == "GMT")? If yes, that's
         * quite simple because we already set date format timezone to be
         * GMT, and hence can just strip out 'Z' altogether
         */
        int len = dateStr.length();
        char c = dateStr.charAt(len-1);
        DateFormat df;

        // [JACKSON-200]: need to support "plain" date...
        if (len <= 10 && Character.isDigit(c)) {
            df = _formatPlain;
            if (df == null) {
                df = _formatPlain = _cloneFormat(DATE_FORMAT_PLAIN, DATE_FORMAT_STR_PLAIN, _timezone, _locale);
            }
        } else if (c == 'Z') {
            df = _formatISO8601_z;
            if (df == null) {
                df = _formatISO8601_z = _cloneFormat(DATE_FORMAT_ISO8601_Z, DATE_FORMAT_STR_ISO8601_Z, _timezone, _locale);
            }
            // [JACKSON-334]: may be missing milliseconds... if so, add
            if (dateStr.charAt(len-4) == ':') {
                StringBuilder sb = new StringBuilder(dateStr);
                sb.insert(len-1, ".000");
                dateStr = sb.toString();
            }
        } else {
            // Let's see if we have timezone indicator or not...
            if (hasTimeZone(dateStr)) {
                c = dateStr.charAt(len-3);
                if (c == ':') { // remove optional colon
                    // remove colon
                    StringBuilder sb = new StringBuilder(dateStr);
                    sb.delete(len-3, len-2);
                    dateStr = sb.toString();
                } else if (c == '+' || c == '-') { // missing minutes
                    // let's just append '00'
                    dateStr += "00";
                }
                // Milliseconds partial or missing; and even seconds are optional
                len = dateStr.length();
                // remove 'T', '+'/'-' and 4-digit timezone-offset
                c = dateStr.charAt(len-9);
                if (Character.isDigit(c)) {
                    StringBuilder sb = new StringBuilder(dateStr);
                    sb.insert(len-5, ".000");
                    dateStr = sb.toString();
                }
                df = _formatISO8601;
                if (_formatISO8601 == null) {
                    df = _formatISO8601 = _cloneFormat(DATE_FORMAT_ISO8601, DATE_FORMAT_STR_ISO8601, _timezone, _locale);
                }
            } else {
                // If not, plain date. Easiest to just patch 'Z' in the end?
                StringBuilder sb = new StringBuilder(dateStr);
                // And possible also millisecond part if missing
                int timeLen = len - dateStr.lastIndexOf('T') - 1;
                if (timeLen <= 8) {
                        sb.append(".000");
                }
                sb.append('Z');
                dateStr = sb.toString();
                df = _formatISO8601_z;
                if (df == null) {
                    df = _formatISO8601_z = _cloneFormat(DATE_FORMAT_ISO8601_Z, DATE_FORMAT_STR_ISO8601_Z,
                            _timezone, _locale);
                }
            }
        }
        return df.parse(dateStr, pos);
    }

// relevant test
// com.fasterxml.jackson.databind.TestFormatSchema::testFormatForParsers
    public void testFormatForParsers() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper(new FactoryWithSchema());
        MySchema s = new MySchema();
        StringReader r = new StringReader("{}");
        
        try {
            mapper.reader(s).withType(Object.class).readValue(r);
            fail("Excpected exception");
        } catch (SchemaException e) {
            assertSame(s, e._schema);
        }
    }

// com.fasterxml.jackson.databind.TestFormatSchema::testFormatForGenerators
    public void testFormatForGenerators() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper(new FactoryWithSchema());
        MySchema s = new MySchema();
        StringWriter sw = new StringWriter();
        
        try {
            mapper.writer(s).writeValue(sw, "Foobar");
            fail("Excpected exception");
        } catch (SchemaException e) {
            assertSame(s, e._schema);
        }
    }

// com.fasterxml.jackson.databind.TestGeneratorUsingMapper::testPojoWriting
    public void testPojoWriting()
        throws IOException
    {
        JsonFactory jf = new MappingJsonFactory();
        StringWriter sw = new StringWriter();
        JsonGenerator gen = jf.createGenerator(sw);
        gen.writeObject(new Pojo());
        gen.close();
        
        String act = sw.toString().trim();
        assertEquals("{\"x\":4}", act);
    }

// com.fasterxml.jackson.databind.TestGeneratorUsingMapper::testPojoWritingFailing
    public void testPojoWritingFailing()
        throws IOException
    {
        
        JsonFactory jf = new JsonFactory();
        try {
            StringWriter sw = new StringWriter();
            JsonGenerator gen = jf.createGenerator(sw);
            gen.writeObject(new Pojo());
            gen.close();
            fail("Expected an exception: got sw '"+sw.toString()+"'");
        } catch (IllegalStateException e) {
            verifyException(e, "No ObjectCodec defined");
        }
    }

// com.fasterxml.jackson.databind.TestGeneratorUsingMapper::testIssue820
    public void testIssue820() throws IOException
    {
        StringBuffer sb = new StringBuffer();
        while (sb.length() <= 5000) {
            sb.append("Yet another line of text...\n");
        }
        String sampleText = sb.toString();
        assertTrue(
                "Sanity check so I don't mess up the sample text later.",
                sampleText.contains("\n"));

        final ObjectMapper mapper = new ObjectMapper();
        final CharacterEscapes defaultCharacterEscapes = new CharacterEscapes() {
            private static final long serialVersionUID = 1L;

            @Override
            public int[] getEscapeCodesForAscii() {
                return standardAsciiEscapesForJSON();
            }

            @Override
            public SerializableString getEscapeSequence(final int ch) {
                return null;
            }
        };

        mapper.getFactory().setCharacterEscapes(defaultCharacterEscapes);
        String jacksonJson = mapper.writeValueAsString(sampleText);
        boolean hasLFs = jacksonJson.indexOf('\n') > 0;
        assertFalse("Should NOT contain linefeeds, should have been escaped", hasLFs);
    }

// com.fasterxml.jackson.databind.TestHandlerInstantiation::testDeserializer
    public void testDeserializer() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setHandlerInstantiator(new MyInstantiator("abc:"));
        MyBean result = mapper.readValue(quote("123"), MyBean.class);
        assertEquals("abc:123", result.value);
    }

// com.fasterxml.jackson.databind.TestHandlerInstantiation::testKeyDeserializer
    public void testKeyDeserializer() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setHandlerInstantiator(new MyInstantiator("abc:"));
        MyMap map = mapper.readValue("{\"a\":\"b\"}", MyMap.class);
        
        assertEquals("{\"KEY\":\"b\"}", mapper.writeValueAsString(map));
    }

// com.fasterxml.jackson.databind.TestHandlerInstantiation::testSerializer
    public void testSerializer() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setHandlerInstantiator(new MyInstantiator("xyz:"));
        assertEquals(quote("xyz:456"), mapper.writeValueAsString(new MyBean("456")));
    }

// com.fasterxml.jackson.databind.TestHandlerInstantiation::testTypeIdResolver
    public void testTypeIdResolver() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setHandlerInstantiator(new MyInstantiator("foobar"));
        String json = mapper.writeValueAsString(new TypeIdBeanWrapper(new TypeIdBean(123)));
        
        assertEquals("{\"bean\":[\"!!!\",{\"x\":123}]}", json);
        
        TypeIdBeanWrapper result = mapper.readValue(json, TypeIdBeanWrapper.class);
        TypeIdBean bean = result.bean;
        assertEquals(123, bean.x);
    }

// com.fasterxml.jackson.databind.TestJDKSerialization::testConfigs
    public void testConfigs() throws IOException
    {
        byte[] base = jdkSerialize(MAPPER.getDeserializationConfig().getBaseSettings());
        assertNotNull(jdkDeserialize(base));

        
        
        DeserializationConfig origDC = MAPPER.getDeserializationConfig();
        SerializationConfig origSC = MAPPER.getSerializationConfig();
        byte[] dcBytes = jdkSerialize(origDC);
        byte[] scBytes = jdkSerialize(origSC);

        DeserializationConfig dc = jdkDeserialize(dcBytes);
        assertNotNull(dc);
        assertEquals(dc._deserFeatures, origDC._deserFeatures);
        SerializationConfig sc = jdkDeserialize(scBytes);
        assertNotNull(sc);
        assertEquals(sc._serFeatures, origSC._serFeatures);
    }

// com.fasterxml.jackson.databind.TestJDKSerialization::testObjectWriter
    public void testObjectWriter() throws IOException
    {
        ObjectWriter origWriter = MAPPER.writer();
        final String EXP_JSON = "{\"x\":2,\"y\":3}";
        final MyPojo p = new MyPojo(2, 3);
        assertEquals(EXP_JSON, origWriter.writeValueAsString(p));
        byte[] bytes = jdkSerialize(origWriter);
        ObjectWriter writer2 = jdkDeserialize(bytes);
        assertEquals(EXP_JSON, writer2.writeValueAsString(p));
    }

// com.fasterxml.jackson.databind.TestJDKSerialization::testObjectReader
    public void testObjectReader() throws IOException
    {
        ObjectReader origReader = MAPPER.reader(MyPojo.class);
        final String JSON = "{\"x\":1,\"y\":2}";
        MyPojo p1 = origReader.readValue(JSON);
        assertEquals(2, p1.y);
        byte[] bytes = jdkSerialize(origReader);
        ObjectReader reader2 = jdkDeserialize(bytes);
        MyPojo p2 = reader2.readValue(JSON);
        assertEquals(2, p2.y);
    }

// com.fasterxml.jackson.databind.TestJDKSerialization::testObjectMapper
    public void testObjectMapper() throws IOException
    {
        final String EXP_JSON = "{\"x\":2,\"y\":3}";
        final MyPojo p = new MyPojo(2, 3);
        assertEquals(EXP_JSON, MAPPER.writeValueAsString(p));

        byte[] bytes = jdkSerialize(MAPPER);
        ObjectMapper mapper2 = jdkDeserialize(bytes);
        assertEquals(EXP_JSON, mapper2.writeValueAsString(p));
        MyPojo p2 = mapper2.readValue(EXP_JSON, MyPojo.class);
        assertEquals(p.x, p2.x);
        assertEquals(p.y, p2.y);
    }

// com.fasterxml.jackson.databind.TestObjectMapper::testProps
    public void testProps()
    {
        ObjectMapper m = new ObjectMapper();
        
        assertNotNull(m.getNodeFactory());
        JsonNodeFactory nf = JsonNodeFactory.instance;
        m.setNodeFactory(nf);
        assertSame(nf, m.getNodeFactory());
    }

// com.fasterxml.jackson.databind.TestObjectMapper::testSupport
    public void testSupport()
    {
        assertTrue(MAPPER.canSerialize(String.class));
        assertTrue(MAPPER.canDeserialize(TypeFactory.defaultInstance().constructType(String.class)));
    }

// com.fasterxml.jackson.databind.TestObjectMapper::testTreeRead
    public void testTreeRead() throws Exception
    {
        String JSON = "{ }";
        JsonNode n = MAPPER.readTree(JSON);
        assertTrue(n instanceof ObjectNode);

        n = MAPPER.readTree(new StringReader(JSON));
        assertTrue(n instanceof ObjectNode);

        n = MAPPER.readTree(new ByteArrayInputStream(JSON.getBytes("UTF-8")));
        assertTrue(n instanceof ObjectNode);
    }

// com.fasterxml.jackson.databind.TestObjectMapper::testConfigForPropertySorting
    public void testConfigForPropertySorting() throws Exception
    {
        ObjectMapper m = new ObjectMapper();
        
        
        assertFalse(m.isEnabled(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY));
        SerializationConfig sc = m.getSerializationConfig();
        assertFalse(sc.isEnabled(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY));
        assertFalse(sc.shouldSortPropertiesAlphabetically());
        DeserializationConfig dc = m.getDeserializationConfig();
        assertFalse(dc.shouldSortPropertiesAlphabetically());

        
        m.enable(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY);
        sc = m.getSerializationConfig();
        assertTrue(sc.isEnabled(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY));
        assertTrue(sc.shouldSortPropertiesAlphabetically());
        dc = m.getDeserializationConfig();
        
        assertTrue(dc.isEnabled(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY));
        assertTrue(dc.shouldSortPropertiesAlphabetically());
    }

// com.fasterxml.jackson.databind.TestObjectMapper::testJsonFactoryLinkage
    public void testJsonFactoryLinkage()
    {
        
        assertSame(MAPPER, MAPPER.getFactory().getCodec());

        
        JsonFactory f = new JsonFactory();
        ObjectMapper m = new ObjectMapper(f);
        assertSame(f, m.getFactory());
        assertSame(m, f.getCodec());
    }

// com.fasterxml.jackson.databind.TestObjectMapper::testProviderConfig
    public void testProviderConfig() throws Exception   
    {
        ObjectMapper m = new ObjectMapper();

        assertEquals(0, m._deserializationContext._cache.cachedDeserializersCount());
        
        Bean bean = m.readValue("{ \"x\" : 3 }", Bean.class);
        assertNotNull(bean);
        assertEquals(1, m._deserializationContext._cache.cachedDeserializersCount());
        m._deserializationContext._cache.flushCachedDeserializers();
        assertEquals(0, m._deserializationContext._cache.cachedDeserializersCount());
    }

// com.fasterxml.jackson.databind.TestObjectMapper::testCopy
    public void testCopy() throws Exception
    {
        ObjectMapper m = new ObjectMapper();
        assertTrue(m.isEnabled(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES));
        m.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        assertFalse(m.isEnabled(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES));

        
        
        ObjectMapper m2 = m.copy();
        assertFalse(m2.isEnabled(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES));
        m2.enable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        assertTrue(m2.isEnabled(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES));
        
        assertFalse(m.isEnabled(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES));

        
        assertFalse(m.isEnabled(DeserializationFeature.UNWRAP_ROOT_VALUE));
        assertFalse(m2.isEnabled(DeserializationFeature.UNWRAP_ROOT_VALUE));
        m.enable(DeserializationFeature.UNWRAP_ROOT_VALUE);
        assertTrue(m.isEnabled(DeserializationFeature.UNWRAP_ROOT_VALUE));
        assertFalse(m2.isEnabled(DeserializationFeature.UNWRAP_ROOT_VALUE));

        
        
        assertNotSame(m.getFactory(), m2.getFactory());

        
        assertEquals(0, m.getSerializationConfig().mixInCount());
        assertEquals(0, m2.getSerializationConfig().mixInCount());
        assertEquals(0, m.getDeserializationConfig().mixInCount());
        assertEquals(0, m2.getDeserializationConfig().mixInCount());

        m.addMixInAnnotations(String.class, Integer.class);
        assertEquals(1, m.getSerializationConfig().mixInCount());
        assertEquals(0, m2.getSerializationConfig().mixInCount());
        assertEquals(1, m.getDeserializationConfig().mixInCount());
        assertEquals(0, m2.getDeserializationConfig().mixInCount());
    }

// com.fasterxml.jackson.databind.TestObjectMapperBeanDeserializer::testFromStringCtor
    public void testFromStringCtor() throws Exception
    {
        CtorValueBean result = MAPPER.readValue("\"abc\"", CtorValueBean.class);
        assertEquals("abc", result.toString());
    }

// com.fasterxml.jackson.databind.TestObjectMapperBeanDeserializer::testFromIntCtor
    public void testFromIntCtor() throws Exception
    {
        CtorValueBean result = MAPPER.readValue("13", CtorValueBean.class);
        assertEquals("13", result.toString());

        try {
            OtherBean otherResult = MAPPER.readValue("13", OtherBean.class);
            fail("Expected an exception, but got result value: "+otherResult.o);
        } catch (JsonMappingException e) {
            verifyException(e, "from Integral number", "no single-int-arg constructor/factory method");
            assertValidLocation(e.getLocation());
        }
    }

// com.fasterxml.jackson.databind.TestObjectMapperBeanDeserializer::testFromLongCtor
    public void testFromLongCtor() throws Exception
    {
        
        long value = 12345678901244L;
        CtorValueBean result = MAPPER.readValue(""+value, CtorValueBean.class);
        assertEquals(""+value, result.toString());

        try {
            OtherBean otherResult = MAPPER.readValue(""+value, OtherBean.class);
            fail("Expected an exception, but got result value: "+otherResult.o);
        } catch (JsonMappingException e) {
            verifyException(e, "from Long integral number", "no single-long-arg constructor/factory method");
            assertValidLocation(e.getLocation());
        }
    }

// com.fasterxml.jackson.databind.TestObjectMapperBeanDeserializer::testFromDoubleCtor
    public void testFromDoubleCtor() throws Exception
    {
        CtorValueBean result = MAPPER.readValue("13.5", CtorValueBean.class);
        assertEquals("13.5", result.toString());

        try {
            OtherBean otherResult = MAPPER.readValue("13.5", OtherBean.class);
            fail("Expected an exception, but got result value: "+otherResult.o);
        } catch (JsonMappingException e) {
            verifyException(e, "from Floating-point number", "no one-double/Double-arg constructor/factory method");
            assertValidLocation(e.getLocation());
        }
    }

// com.fasterxml.jackson.databind.TestObjectMapperBeanDeserializer::testFromStringFactory
    public void testFromStringFactory() throws Exception
    {
        FactoryValueBean result = MAPPER.readValue("\"abc\"", FactoryValueBean.class);
        assertEquals("abc", result.toString());
    }

// com.fasterxml.jackson.databind.TestObjectMapperBeanDeserializer::testFromIntFactory
    public void testFromIntFactory() throws Exception
    {
        FactoryValueBean result = MAPPER.readValue("13", FactoryValueBean.class);
        assertEquals("13", result.toString());
    }

// com.fasterxml.jackson.databind.TestObjectMapperBeanDeserializer::testFromLongFactory
    public void testFromLongFactory() throws Exception
    {
        
        long value = 12345678901244L;
        FactoryValueBean result = MAPPER.readValue(""+value, FactoryValueBean.class);
        assertEquals(""+value, result.toString());
    }

// com.fasterxml.jackson.databind.TestObjectMapperBeanDeserializer::testSimpleBean
    public void testSimpleBean() throws Exception
    {
        ArrayList<Object> misc = new ArrayList<Object>();
        misc.add("xyz");
        misc.add(42);
        misc.add(null);
        misc.add(Boolean.TRUE);
        TestBean bean = new TestBean(13, -900L, "\"test\"", new URI("http://foobar.com"), misc);

        
        String json = MAPPER.writeValueAsString(bean);

        TestBean result = MAPPER.readValue(json, TestBean.class);
        assertEquals(bean, result);
    }

// com.fasterxml.jackson.databind.TestObjectMapperBeanDeserializer::testListBean
    public void testListBean() throws Exception
    {
        final int COUNT = 13;
        ArrayList<CtorValueBean> beans = new ArrayList<CtorValueBean>();
        for (int i = 0; i < COUNT; ++i) {
            beans.add(new CtorValueBean(i));
        }
        BeanWithList bean = new BeanWithList(beans);

        StringWriter sw = new StringWriter();
        MAPPER.writeValue(sw, bean);

        BeanWithList result = MAPPER.readValue(sw.toString(), BeanWithList.class);
        assertEquals(bean, result);
    }

// com.fasterxml.jackson.databind.TestObjectMapperBeanDeserializer::testUnknownFields
    public void testUnknownFields() throws Exception
    {
        try {
            TestBean bean = MAPPER.readValue("{ \"foobar\" : 3 }", TestBean.class);
            fail("Expected an exception, got bean: "+bean);
        } catch (JsonMappingException jse) {
            ;
        }
    }

// com.fasterxml.jackson.databind.TestObjectMapperBeanSerializer::testComplexObject
    public void testComplexObject()
        throws Exception
    {
        FixtureObject  aTestObj = new FixtureObject();
        ObjectMapper aMapper  = new ObjectMapper();
        StringWriter aWriter = new StringWriter();
        JsonGenerator aGen = new JsonFactory().createGenerator(aWriter);
        aMapper.writeValue(aGen, aTestObj);
        aGen.close();
        JsonParser jp = new JsonFactory().createParser(new StringReader(aWriter.toString()));

        assertEquals(JsonToken.START_OBJECT, jp.nextToken());

        while (jp.nextToken() != JsonToken.END_OBJECT) {
            assertEquals(JsonToken.FIELD_NAME, jp.getCurrentToken());
            String name = jp.getCurrentName();
            JsonToken t = jp.nextToken();

            if (name.equals("uri")) {
                assertToken(JsonToken.VALUE_STRING, t);
                assertEquals(FixtureObjectBase.VALUE_URSTR, getAndVerifyText(jp));
            } else if (name.equals("url")) {
                assertToken(JsonToken.VALUE_STRING, t);
                assertEquals(FixtureObjectBase.VALUE_URSTR, getAndVerifyText(jp));
            } else if (name.equals("testNull")) {
                assertToken(JsonToken.VALUE_NULL, t);
            } else if (name.equals("testString")) {
                assertToken(JsonToken.VALUE_STRING, t);
                assertEquals(FixtureObjectBase.VALUE_STRING, getAndVerifyText(jp));
            } else if (name.equals("testBoolean")) {
                assertToken(JsonToken.VALUE_TRUE, t);
            } else if (name.equals("testEnum")) {
                assertToken(JsonToken.VALUE_STRING, t);
                assertEquals(FixtureObjectBase.VALUE_ENUM.toString(),getAndVerifyText(jp));
            } else if (name.equals("testInteger")) {
                assertToken(JsonToken.VALUE_NUMBER_INT, t);
                assertEquals(jp.getIntValue(),FixtureObjectBase.VALUE_INT);
            } else if (name.equals("testLong")) {
                assertToken(JsonToken.VALUE_NUMBER_INT, t);
                assertEquals(jp.getLongValue(),FixtureObjectBase.VALUE_LONG);
            } else if (name.equals("testBigInteger")) {
                assertToken(JsonToken.VALUE_NUMBER_INT, t);
                assertEquals(jp.getLongValue(),FixtureObjectBase.VALUE_BIGINT.longValue());
            } else if (name.equals("testBigDecimal")) {
                assertToken(JsonToken.VALUE_NUMBER_FLOAT, t);
                assertEquals(jp.getText(), FixtureObjectBase.VALUE_BIGDEC.toString());
            } else if (name.equals("testCharacter")) {
                assertToken(JsonToken.VALUE_STRING, t);
                assertEquals(String.valueOf(FixtureObjectBase.VALUE_CHAR), getAndVerifyText(jp));
            } else if (name.equals("testShort")) {
                assertToken(JsonToken.VALUE_NUMBER_INT, t);
                assertEquals(jp.getIntValue(),FixtureObjectBase.VALUE_SHORT);
            } else if (name.equals("testByte")) {
                assertToken(JsonToken.VALUE_NUMBER_INT, t);
                assertEquals(jp.getIntValue(),FixtureObjectBase.VALUE_BYTE);
            } else if (name.equals("testFloat")) {
                assertToken(JsonToken.VALUE_NUMBER_FLOAT, t);
                assertEquals(jp.getDecimalValue().floatValue(),FixtureObjectBase.VALUE_FLOAT);
            } else if (name.equals("testDouble")) {
                assertToken(JsonToken.VALUE_NUMBER_FLOAT, t);
                assertEquals(jp.getDoubleValue(),FixtureObjectBase.VALUE_DBL);
            } else if (name.equals("testStringBuffer")) {
                assertToken(JsonToken.VALUE_STRING, t);
                assertEquals(FixtureObjectBase.VALUE_STRING, getAndVerifyText(jp));
            } else if (name.equals("testError")) {
                
                assertToken(JsonToken.START_OBJECT, t);

                
                
                while (jp.nextToken() == JsonToken.FIELD_NAME) {
                    name = jp.getCurrentName();
                    if (name.equals("cause")) {
                        assertEquals(JsonToken.VALUE_NULL, jp.nextToken());
                    } else if (name.equals("message")) {
                        assertEquals(JsonToken.VALUE_STRING, jp.nextToken());
                        assertEquals(FixtureObjectBase.VALUE_ERRTXT, getAndVerifyText(jp));
                    } else if (name.equals("localizedMessage")) {
                        assertEquals(JsonToken.VALUE_STRING, jp.nextToken());
                    } else if (name.equals("stackTrace")) {
                        assertEquals(JsonToken.START_ARRAY,jp.nextToken());
                        int i = 0;
                        while(jp.nextToken() != JsonToken.END_ARRAY) {
                            if(i >= 100000) {
                                assertTrue("Probably run away loop in test. StackTrack Array was not properly closed.",false);
                            }
                        }
                    } else if (name.equals("suppressed")) {
                        
                        assertEquals(JsonToken.START_ARRAY,jp.nextToken());
                        assertEquals(JsonToken.END_ARRAY,jp.nextToken());
                    } else {
                        fail("Unexpected field name '"+name+"'");
                    }
                }
                
                assertEquals(JsonToken.END_OBJECT, jp.getCurrentToken());
            } else {
                fail("Unexpected field, name '"+name+"'");
            }
        }

        
        assertNull(jp.nextToken());
        jp.close();
    }

// com.fasterxml.jackson.databind.TestParserUsingMapper::testReadingArrayAsTree
    public void testReadingArrayAsTree() throws IOException
    {
        JsonFactory jf = new MappingJsonFactory();
        final String JSON = "[ 1, 2, false ]";

        for (int i = 0; i < 2; ++i) {
            JsonParser jp = jf.createParser(new StringReader(JSON));
            
            if (i == 0) {
                assertToken(JsonToken.START_ARRAY, jp.nextToken());
            }
            JsonNode root = (JsonNode) jp.readValueAsTree();
            jp.close();
            assertTrue(root.isArray());
            assertEquals(3, root.size());
            assertEquals(1, root.get(0).intValue());
            assertEquals(2, root.get(1).intValue());
            assertFalse(root.get(2).booleanValue());
        }
    }

// com.fasterxml.jackson.databind.TestParserUsingMapper::testPojoReading
    public void testPojoReading() throws IOException
    {
        JsonFactory jf = new MappingJsonFactory();
        final String JSON = "{ \"x\" : 9 }";
        JsonParser jp = jf.createParser(new StringReader(JSON));

        
        assertToken(JsonToken.START_OBJECT, jp.nextToken());
        Pojo p = jp.readValueAs(Pojo.class);
        assertEquals(9, p._x);
        jp.close();

        
        jp = jf.createParser(new StringReader(JSON));
        p = jp.readValueAs(Pojo.class);
        assertEquals(9, p._x);
        jp.close();
    }

// com.fasterxml.jackson.databind.TestParserUsingMapper::testIncrementalPojoReading
    public void testIncrementalPojoReading()
        throws IOException
    {
        JsonFactory jf = new MappingJsonFactory();
        final String JSON = "[ 1, true, null, \"abc\" ]";
        JsonParser jp = jf.createParser(new StringReader(JSON));

        
        assertToken(JsonToken.START_ARRAY, jp.nextToken());

        assertToken(JsonToken.VALUE_NUMBER_INT, jp.nextToken());
        assertEquals(Integer.valueOf(1), jp.readValueAs(Integer.class));
        assertEquals(Boolean.TRUE, jp.readValueAs(Boolean.class));
        
        assertNull(jp.readValueAs(Object.class));
        
        assertEquals(JsonToken.VALUE_NULL, jp.getLastClearedToken());

        assertEquals("abc", jp.readValueAs(String.class));

        
        assertNull(jp.readValueAs(Object.class));
        assertEquals(JsonToken.END_ARRAY, jp.getLastClearedToken());

        
        assertNull(jp.nextToken());

        jp.close();
    }

// com.fasterxml.jackson.databind.TestParserUsingMapper::testPojoReadingFailing
    public void testPojoReadingFailing()
        throws IOException
    {
        
        JsonFactory jf = new JsonFactory();
        try {
            final String JSON = "{ \"x\" : 9 }";
            JsonParser jp = jf.createParser(new StringReader(JSON));
            Pojo p = jp.readValueAs(Pojo.class);
            fail("Expected an exception: got "+p);
        } catch (IllegalStateException e) {
            verifyException(e, "No ObjectCodec defined");
        }
    }

// com.fasterxml.jackson.databind.TestParserUsingMapper::testEscapingUsingMapper
    public void testEscapingUsingMapper() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(JsonGenerator.Feature.ESCAPE_NON_ASCII, true);
        assertEquals(quote("\\u0101"), mapper.writeValueAsString(String.valueOf((char) 257)));
    }

// com.fasterxml.jackson.databind.TestReadValues::testRootBeans
    public void testRootBeans() throws Exception
    {
        final String JSON = "{\"a\":3}{\"a\":27}  ";
        Iterator<Bean> it = MAPPER.reader(Bean.class).readValues(JSON);

        assertNotNull(((MappingIterator<?>) it).getCurrentLocation());
        assertTrue(it.hasNext());
        Bean b = it.next();
        assertEquals(3, b.a);
        assertTrue(it.hasNext());
        b = it.next();
        assertEquals(27, b.a);
        assertFalse(it.hasNext());
    }

// com.fasterxml.jackson.databind.TestReadValues::testRootMaps
    public void testRootMaps() throws Exception
    {
        final String JSON = "{\"a\":3}{\"a\":27}  ";
        Iterator<Map<?,?>> it = MAPPER.reader(Map.class).readValues(JSON);

        assertNotNull(((MappingIterator<?>) it).getCurrentLocation());
        assertTrue(it.hasNext());
        Map<?,?> map = it.next();
        assertEquals(1, map.size());
        assertEquals(Integer.valueOf(3), map.get("a"));
        assertTrue(it.hasNext());
        assertNotNull(((MappingIterator<?>) it).getCurrentLocation());
        map = it.next();
        assertEquals(1, map.size());
        assertEquals(Integer.valueOf(27), map.get("a"));
        assertFalse(it.hasNext());
    }

// com.fasterxml.jackson.databind.TestReadValues::testRootBeansWithParser
    public void testRootBeansWithParser() throws Exception
    {
        final String JSON = "{\"a\":3}{\"a\":27}  ";
        JsonParser jp = MAPPER.getFactory().createParser(JSON);
        
        Iterator<Bean> it = jp.readValuesAs(Bean.class);

        assertTrue(it.hasNext());
        Bean b = it.next();
        assertEquals(3, b.a);
        assertTrue(it.hasNext());
        b = it.next();
        assertEquals(27, b.a);
        assertFalse(it.hasNext());
    }

// com.fasterxml.jackson.databind.TestReadValues::testRootArraysWithParser
    public void testRootArraysWithParser() throws Exception
    {
        final String JSON = "[1][3]";
        JsonParser jp = MAPPER.getFactory().createParser(JSON);

        
        
        assertToken(JsonToken.START_ARRAY, jp.nextToken());
        
        Iterator<int[]> it = MAPPER.reader(int[].class).readValues(jp);
        assertTrue(it.hasNext());
        int[] array = it.next();
        assertEquals(1, array.length);
        assertEquals(1, array[0]);
        assertTrue(it.hasNext());
        array = it.next();
        assertEquals(1, array.length);
        assertEquals(3, array[0]);
        assertFalse(it.hasNext());
    }

// com.fasterxml.jackson.databind.TestReadValues::testHasNextWithEndArray
    public void testHasNextWithEndArray() throws Exception {
        final String JSON = "[1,3]";
        JsonParser jp = MAPPER.getFactory().createParser(JSON);

        
        
        assertToken(JsonToken.START_ARRAY, jp.nextToken());
        jp.nextToken();
        
        Iterator<Integer> it = MAPPER.reader(Integer.class).readValues(jp);
        assertTrue(it.hasNext());
        int value = it.next();
        assertEquals(1, value);
        assertTrue(it.hasNext());
        value = it.next();
        assertEquals(3, value);
        assertFalse(it.hasNext());
        assertFalse(it.hasNext());
    }

// com.fasterxml.jackson.databind.TestReadValues::testHasNextWithEndArrayManagedParser
    public void testHasNextWithEndArrayManagedParser() throws Exception {
        final String JSON = "[1,3]";

        Iterator<Integer> it = MAPPER.reader(Integer.class).readValues(JSON);
        assertTrue(it.hasNext());
        int value = it.next();
        assertEquals(1, value);
        assertTrue(it.hasNext());
        value = it.next();
        assertEquals(3, value);
        assertFalse(it.hasNext());
        assertFalse(it.hasNext());
    }

// com.fasterxml.jackson.databind.TestReadValues::testNonRootBeans
    public void testNonRootBeans() throws Exception
    {
        final String JSON = "{\"leaf\":[{\"a\":3},{\"a\":27}]}";
        JsonParser jp = MAPPER.getFactory().createParser(JSON);
        assertToken(JsonToken.START_OBJECT, jp.nextToken());
        assertToken(JsonToken.FIELD_NAME, jp.nextToken());
        assertToken(JsonToken.START_ARRAY, jp.nextToken());
        
        
        
        assertToken(JsonToken.START_OBJECT, jp.nextToken());
        
        Iterator<Bean> it = MAPPER.reader(Bean.class).readValues(jp);

        assertTrue(it.hasNext());
        Bean b = it.next();
        assertEquals(3, b.a);
        assertTrue(it.hasNext());
        b = it.next();
        assertEquals(27, b.a);
        assertFalse(it.hasNext());
        jp.close();
    }

// com.fasterxml.jackson.databind.TestReadValues::testNonRootMapsWithParser
    public void testNonRootMapsWithParser() throws Exception
    {
        final String JSON = "[{\"a\":3},{\"a\":27}]";
        JsonParser jp = MAPPER.getFactory().createParser(JSON);
        assertToken(JsonToken.START_ARRAY, jp.nextToken());

        
        
        
        jp.clearCurrentToken();
        
        Iterator<Map<?,?>> it = MAPPER.reader(Map.class).readValues(jp);

        assertTrue(it.hasNext());
        Map<?,?> map = it.next();
        assertEquals(1, map.size());
        assertEquals(Integer.valueOf(3), map.get("a"));
        assertTrue(it.hasNext());
        map = it.next();
        assertEquals(1, map.size());
        assertEquals(Integer.valueOf(27), map.get("a"));
        assertFalse(it.hasNext());
        jp.close();
    }

// com.fasterxml.jackson.databind.TestReadValues::testNonRootMapsWithObjectReader
    public void testNonRootMapsWithObjectReader() throws Exception
    {
        String JSON = "[{ \"hi\": \"ho\", \"neighbor\": \"Joe\" },\n"
            +"{\"boy\": \"howdy\", \"huh\": \"what\"}]";
        final MappingIterator<Map<String, Object>> iterator = MAPPER
                .reader()
                .withType(new TypeReference<Map<String, Object>>(){})
                .readValues(JSON);

        Map<String,Object> map;
        assertTrue(iterator.hasNext());
        map = iterator.nextValue();
        assertEquals(2, map.size());
        assertTrue(iterator.hasNext());
        map = iterator.nextValue();
        assertEquals(2, map.size());
        assertFalse(iterator.hasNext());
    }

// com.fasterxml.jackson.databind.TestReadValues::testNonRootArraysUsingParser
    public void testNonRootArraysUsingParser() throws Exception
    {
        final String JSON = "[[1],[3]]";
        JsonParser jp = MAPPER.getFactory().createParser(JSON);
        assertToken(JsonToken.START_ARRAY, jp.nextToken());
        
        
        
        
        assertToken(JsonToken.START_ARRAY, jp.nextToken());
        
        Iterator<int[]> it = MAPPER.readValues(jp, int[].class);

        assertTrue(it.hasNext());
        int[] array = it.next();
        assertEquals(1, array.length);
        assertEquals(1, array[0]);
        assertTrue(it.hasNext());
        array = it.next();
        assertEquals(1, array.length);
        assertEquals(3, array[0]);
        assertFalse(it.hasNext());
        jp.close();
    }

// com.fasterxml.jackson.databind.TestRootName::testRootViaMapper
    public void testRootViaMapper() throws Exception
    {
        ObjectMapper mapper = rootMapper();
        String json = mapper.writeValueAsString(new Bean());
        assertEquals("{\"rudy\":{\"a\":3}}", json);
        Bean bean = mapper.readValue(json, Bean.class);
        assertNotNull(bean);

        
        json = mapper.writeValueAsString(new RootBeanWithEmpty());
        assertEquals("{\"RootBeanWithEmpty\":{\"a\":2}}", json);
        RootBeanWithEmpty bean2 = mapper.readValue(json, RootBeanWithEmpty.class);
        assertNotNull(bean2);
        assertEquals(2, bean2.a);
    }

// com.fasterxml.jackson.databind.TestRootName::testRootViaWriterAndReader
    public void testRootViaWriterAndReader() throws Exception
    {
        ObjectMapper mapper = rootMapper();
        String json = mapper.writer().writeValueAsString(new Bean());
        assertEquals("{\"rudy\":{\"a\":3}}", json);
        Bean bean = mapper.reader(Bean.class).readValue(json);
        assertNotNull(bean);
    }

// com.fasterxml.jackson.databind.TestRootName::testReconfiguringOfWrapping
    public void testReconfiguringOfWrapping() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        
        final Bean input = new Bean();
        String jsonUnwrapped = mapper.writeValueAsString(input);
        assertEquals("{\"a\":3}", jsonUnwrapped);
        
        String jsonWrapped = mapper.writer(SerializationFeature.WRAP_ROOT_VALUE)
            .writeValueAsString(input);
        assertEquals("{\"rudy\":{\"a\":3}}", jsonWrapped);

        
        Bean result = mapper.readValue(jsonUnwrapped, Bean.class);
        assertNotNull(result);
        try { 
            result = mapper.reader(Bean.class).with(DeserializationFeature.UNWRAP_ROOT_VALUE)
                .readValue(jsonUnwrapped);
            fail("Should have failed");
        } catch (JsonMappingException e) {
            verifyException(e, "Root name 'a'");
        }
        
        result = mapper.reader(Bean.class).with(DeserializationFeature.UNWRAP_ROOT_VALUE)
            .readValue(jsonWrapped);
        assertNotNull(result);
    }

// com.fasterxml.jackson.databind.TestRootName::testRootUsingExplicitConfig
    public void testRootUsingExplicitConfig() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        ObjectWriter writer = mapper.writer().withRootName("wrapper");
        String json = writer.writeValueAsString(new Bean());
        assertEquals("{\"wrapper\":{\"a\":3}}", json);

        ObjectReader reader = mapper.reader(Bean.class).withRootName("wrapper");
        Bean bean = reader.readValue(json);
        assertNotNull(bean);

        
        ObjectMapper wrapping = rootMapper();
        json = wrapping.writer().withRootName("something").writeValueAsString(new Bean());
        assertEquals("{\"something\":{\"a\":3}}", json);
        json = wrapping.writer().withRootName("").writeValueAsString(new Bean());
        assertEquals("{\"a\":3}", json);

        bean = wrapping.reader(Bean.class).withRootName("").readValue(json);
        assertNotNull(bean);
    }

// com.fasterxml.jackson.databind.TestStdDateFormat::testFactories
    public void testFactories() {
        TimeZone tz = TimeZone.getTimeZone("GMT");
        Locale loc = Locale.US;
        assertNotNull(StdDateFormat.getISO8601Format(tz, loc));
        assertNotNull(StdDateFormat.getRFC1123Format(tz, loc));
    }

// com.fasterxml.jackson.databind.TestStdDateFormat::testInvalid
    public void testInvalid() {
        StdDateFormat std = new StdDateFormat();
        try {
            std.parse("foobar");
        } catch (java.text.ParseException e) {
            verifyException(e, "Can not parse");
        }
    }

// com.fasterxml.jackson.databind.TestVersions::testMapperVersions
    public void testMapperVersions()
    {
        ObjectMapper mapper = new ObjectMapper();
        assertVersion(mapper);
        assertVersion(mapper.reader());
        assertVersion(mapper.writer());
        assertVersion(new JacksonAnnotationIntrospector());
    }

// com.fasterxml.jackson.databind.access.TestSerAnyGetter::testDynaBean
    public void testDynaBean() throws Exception
    {
        DynaBean b = new DynaBean();
        b.id = 123;
        b.set("name", "Billy");
        assertEquals("{\"id\":123,\"name\":\"Billy\"}", MAPPER.writeValueAsString(b));

        DynaBean result = MAPPER.readValue("{\"id\":2,\"name\":\"Joe\"}", DynaBean.class);
        assertEquals(2, result.id);
        assertEquals("Joe", result.other.get("name"));
    }

// com.fasterxml.jackson.databind.access.TestSerAnyGetter::testPrivate
    public void testPrivate() throws Exception
    {
        String json = MAPPER.writeValueAsString(new PrivateThing());
        assertEquals("{\"a\":\"A\"}", json);
    }

// com.fasterxml.jackson.databind.big.TestBiggerData::testReading
	public void testReading() throws Exception
	{
		ObjectMapper mapper = objectMapper();
		Citm citm = mapper.readValue(getClass().getResourceAsStream("/data/citm_catalog.json"),
				Citm.class);
		assertNotNull(citm);
		assertNotNull(citm.areaNames);
		assertEquals(17, citm.areaNames.size());
		assertNotNull(citm.events);
		assertEquals(184, citm.events.size());

		assertNotNull(citm.seatCategoryNames);
		assertEquals(64, citm.seatCategoryNames.size());
		assertNotNull(citm.subTopicNames);
		assertEquals(19, citm.subTopicNames.size());
		assertNotNull(citm.subjectNames);
		assertEquals(0, citm.subjectNames.size());
		assertNotNull(citm.topicNames);
		assertEquals(4, citm.topicNames.size());
		assertNotNull(citm.topicSubTopics);
		assertEquals(4, citm.topicSubTopics.size());
		assertNotNull(citm.venueNames);
		assertEquals(1, citm.venueNames.size());
	}

// com.fasterxml.jackson.databind.big.TestBiggerData::testRoundTrip
	public void testRoundTrip() throws Exception
	{
		ObjectMapper mapper = objectMapper();
		Citm citm = mapper.readValue(getClass().getResourceAsStream("/data/citm_catalog.json"),
				Citm.class);

		ObjectWriter w = mapper.writerWithDefaultPrettyPrinter();
		
		String json1 = w.writeValueAsString(citm);
		Citm citm2 = mapper.readValue(json1, Citm.class);
		String json2 = w.writeValueAsString(citm2);

		assertEquals(json1, json2);
	}

// com.fasterxml.jackson.databind.contextual.TestContextAttributeWithDeser::testSimplePerCall
    public void testSimplePerCall() throws Exception
    {
        final String INPUT = aposToQuotes("[{'value':'a'},{'value':'b'}]");
        TestPOJO[] pojos = MAPPER.reader(TestPOJO[].class).readValue(INPUT);
        assertEquals(2, pojos.length);
        assertEquals("a/0", pojos[0].value);
        assertEquals("b/1", pojos[1].value);

        
        TestPOJO[] pojos2 = MAPPER.reader(TestPOJO[].class).readValue(INPUT);
        assertEquals(2, pojos2.length);
        assertEquals("a/0", pojos2[0].value);
        assertEquals("b/1", pojos2[1].value);
    }

// com.fasterxml.jackson.databind.contextual.TestContextAttributeWithDeser::testSimpleDefaults
    public void testSimpleDefaults() throws Exception
    {
        final String INPUT = aposToQuotes("{'value':'x'}");
        TestPOJO pojo = MAPPER.reader(TestPOJO.class)
                .withAttribute(KEY, Integer.valueOf(3))
                .readValue(INPUT);
        assertEquals("x/3", pojo.value);

        
        TestPOJO pojo2 = MAPPER.reader(TestPOJO.class)
                .withAttribute(KEY, Integer.valueOf(3))
                .readValue(INPUT);
        assertEquals("x/3", pojo2.value);
    }

// com.fasterxml.jackson.databind.contextual.TestContextAttributeWithDeser::testHierarchic
    public void testHierarchic() throws Exception
    {
        final String INPUT = aposToQuotes("[{'value':'x'},{'value':'y'}]");
        ObjectReader r = MAPPER.reader(TestPOJO[].class).withAttribute(KEY, Integer.valueOf(2));
        TestPOJO[] pojos = r.readValue(INPUT);
        assertEquals(2, pojos.length);
        assertEquals("x/2", pojos[0].value);
        assertEquals("y/3", pojos[1].value);

        
        TestPOJO[] pojos2 = r.readValue(INPUT);
        assertEquals(2, pojos2.length);
        assertEquals("x/2", pojos2[0].value);
        assertEquals("y/3", pojos2[1].value);
    }

// com.fasterxml.jackson.databind.contextual.TestContextAttributeWithSer::testSimplePerCall
    public void testSimplePerCall() throws Exception
    {
        final String EXP = aposToQuotes("[{'value':'0:a'},{'value':'1:b'}]");
        ObjectWriter w = MAPPER.writer();
        final TestPOJO[] INPUT = new TestPOJO[] {
                new TestPOJO("a"), new TestPOJO("b") };
        assertEquals(EXP, w.writeValueAsString(INPUT));

        
        assertEquals(EXP, w.writeValueAsString(INPUT));
    }

// com.fasterxml.jackson.databind.contextual.TestContextAttributeWithSer::testSimpleDefaults
    public void testSimpleDefaults() throws Exception
    {
        final String EXP = aposToQuotes("{'value':'3:xyz'}");
        final TestPOJO INPUT = new TestPOJO("xyz");
        String json = MAPPER.writer().withAttribute(KEY, Integer.valueOf(3))
                .writeValueAsString(INPUT);
        assertEquals(EXP, json);

        String json2 = MAPPER.writer().withAttribute(KEY, Integer.valueOf(3))
                .writeValueAsString(INPUT);
        assertEquals(EXP, json2);
    }

// com.fasterxml.jackson.databind.contextual.TestContextAttributeWithSer::testHierarchic
    public void testHierarchic() throws Exception
    {
        final TestPOJO[] INPUT = new TestPOJO[] { new TestPOJO("a"), new TestPOJO("b") };
        final String EXP = aposToQuotes("[{'value':'2:a'},{'value':'3:b'}]");
        ObjectWriter w = MAPPER.writer().withAttribute(KEY, Integer.valueOf(2));
        assertEquals(EXP, w.writeValueAsString(INPUT));

        
        assertEquals(EXP, w.writeValueAsString(INPUT));
    }

// com.fasterxml.jackson.databind.contextual.TestContextualDeserialization::testSimple
    public void testSimple() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        SimpleModule module = new SimpleModule("test", Version.unknownVersion());
        module.addDeserializer(StringValue.class, new MyContextualDeserializer());
        mapper.registerModule(module);
        ContextualBean bean = mapper.readValue("{\"a\":\"1\",\"b\":\"2\"}", ContextualBean.class);
        assertEquals("a=1", bean.a.value);
        assertEquals("b=2", bean.b.value);

        
        bean = mapper.readValue("{\"a\":\"3\",\"b\":\"4\"}", ContextualBean.class);
        assertEquals("a=3", bean.a.value);
        assertEquals("b=4", bean.b.value);
    }

// com.fasterxml.jackson.databind.contextual.TestContextualDeserialization::testSimpleWithAnnotations
    public void testSimpleWithAnnotations() throws Exception
    {
        ObjectMapper mapper = _mapperWithAnnotatedContextual();
        ContextualBean bean = mapper.readValue("{\"a\":\"1\",\"b\":\"2\"}", ContextualBean.class);
        assertEquals("NameA=1", bean.a.value);
        assertEquals("NameB=2", bean.b.value);

        
        bean = mapper.readValue("{\"a\":\"x\",\"b\":\"y\"}", ContextualBean.class);
        assertEquals("NameA=x", bean.a.value);
        assertEquals("NameB=y", bean.b.value);
    }

// com.fasterxml.jackson.databind.contextual.TestContextualDeserialization::testSimpleWithClassAnnotations
    public void testSimpleWithClassAnnotations() throws Exception
    {
        ObjectMapper mapper = _mapperWithAnnotatedContextual();
        ContextualClassBean bean = mapper.readValue("{\"a\":\"1\",\"b\":\"2\"}", ContextualClassBean.class);
        assertEquals("Class=1", bean.a.value);
        assertEquals("NameB=2", bean.b.value);
        
        bean = mapper.readValue("{\"a\":\"123\",\"b\":\"345\"}", ContextualClassBean.class);
        assertEquals("Class=123", bean.a.value);
        assertEquals("NameB=345", bean.b.value);
    }

// com.fasterxml.jackson.databind.contextual.TestContextualDeserialization::testAnnotatedCtor
    public void testAnnotatedCtor() throws Exception
    {
        ObjectMapper mapper = _mapperWithAnnotatedContextual();
        ContextualCtorBean bean = mapper.readValue("{\"a\":\"foo\",\"b\":\"bar\"}", ContextualCtorBean.class);
        assertEquals("CtorA=foo", bean.a);
        assertEquals("CtorB=bar", bean.b);

        bean = mapper.readValue("{\"a\":\"1\",\"b\":\"0\"}", ContextualCtorBean.class);
        assertEquals("CtorA=1", bean.a);
        assertEquals("CtorB=0", bean.b);
    }

// com.fasterxml.jackson.databind.contextual.TestContextualDeserialization::testAnnotatedArray
    public void testAnnotatedArray() throws Exception
    {
        ObjectMapper mapper = _mapperWithAnnotatedContextual();
        ContextualArrayBean bean = mapper.readValue("{\"beans\":[\"x\"]}", ContextualArrayBean.class);
        assertEquals(1, bean.beans.length);
        assertEquals("array=x", bean.beans[0].value);

        bean = mapper.readValue("{\"beans\":[\"a\",\"b\"]}", ContextualArrayBean.class);
        assertEquals(2, bean.beans.length);
        assertEquals("array=a", bean.beans[0].value);
        assertEquals("array=b", bean.beans[1].value);
    }

// com.fasterxml.jackson.databind.contextual.TestContextualDeserialization::testAnnotatedList
    public void testAnnotatedList() throws Exception
    {
        ObjectMapper mapper = _mapperWithAnnotatedContextual();
        ContextualListBean bean = mapper.readValue("{\"beans\":[\"x\"]}", ContextualListBean.class);
        assertEquals(1, bean.beans.size());
        assertEquals("list=x", bean.beans.get(0).value);

        bean = mapper.readValue("{\"beans\":[\"x\",\"y\",\"z\"]}", ContextualListBean.class);
        assertEquals(3, bean.beans.size());
        assertEquals("list=x", bean.beans.get(0).value);
        assertEquals("list=y", bean.beans.get(1).value);
        assertEquals("list=z", bean.beans.get(2).value);
    }

// com.fasterxml.jackson.databind.contextual.TestContextualDeserialization::testAnnotatedMap
    public void testAnnotatedMap() throws Exception
    {
        ObjectMapper mapper = _mapperWithAnnotatedContextual();
        ContextualMapBean bean = mapper.readValue("{\"beans\":{\"a\":\"b\"}}", ContextualMapBean.class);
        assertEquals(1, bean.beans.size());
        Map.Entry<String,StringValue> entry = bean.beans.entrySet().iterator().next();
        assertEquals("a", entry.getKey());
        assertEquals("map=b", entry.getValue().value);

        bean = mapper.readValue("{\"beans\":{\"x\":\"y\",\"1\":\"2\"}}", ContextualMapBean.class);
        assertEquals(2, bean.beans.size());
        Iterator<Map.Entry<String,StringValue>> it = bean.beans.entrySet().iterator();
        entry = it.next();
        assertEquals("x", entry.getKey());
        assertEquals("map=y", entry.getValue().value);
        entry = it.next();
        assertEquals("1", entry.getKey());
        assertEquals("map=2", entry.getValue().value);
    }

// com.fasterxml.jackson.databind.contextual.TestContextualKeyTypes::testSimpleKeySer
    public void testSimpleKeySer() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        SimpleModule module = new SimpleModule("test", Version.unknownVersion());
        module.addKeySerializer(String.class, new ContextualKeySerializer("prefix"));
        mapper.registerModule(module);
        Map<String,Object> input = new HashMap<String,Object>();
        input.put("a", Integer.valueOf(3));
        String json = mapper.writerWithType(TypeFactory.defaultInstance().constructMapType(HashMap.class, String.class, Object.class))
            .writeValueAsString(input);
        assertEquals("{\"prefix:a\":3}", json);
    }

// com.fasterxml.jackson.databind.contextual.TestContextualKeyTypes::testSimpleKeyDeser
    public void testSimpleKeyDeser() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        SimpleModule module = new SimpleModule("test", Version.unknownVersion());
        module.addKeyDeserializer(String.class, new ContextualDeser("???"));
        mapper.registerModule(module);
        MapBean result = mapper.readValue("{\"map\":{\"a\":3}}", MapBean.class);
        Map<String,Integer> map = result.map;
        assertNotNull(map);
        assertEquals(1, map.size());
        Map.Entry<String,Integer> entry = map.entrySet().iterator().next();
        assertEquals(Integer.valueOf(3), entry.getValue());
        assertEquals("map:a", entry.getKey());
    }

// com.fasterxml.jackson.databind.contextual.TestContextualSerialization::testMethodAnnotations
    public void testMethodAnnotations() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        SimpleModule module = new SimpleModule("test", Version.unknownVersion());
        module.addSerializer(String.class, new AnnotatedContextualSerializer());
        mapper.registerModule(module);
        assertEquals("{\"value\":\"see:foobar\"}", mapper.writeValueAsString(new ContextualBean("foobar")));
    }

// com.fasterxml.jackson.databind.contextual.TestContextualSerialization::testClassAnnotations
    public void testClassAnnotations() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        SimpleModule module = new SimpleModule("test", Version.unknownVersion());
        module.addSerializer(String.class, new AnnotatedContextualSerializer());
        mapper.registerModule(module);
        assertEquals("{\"value\":\"Voila->xyz\"}", mapper.writeValueAsString(new BeanWithClassConfig("xyz")));
    }

// com.fasterxml.jackson.databind.contextual.TestContextualSerialization::testWrappedBean
    public void testWrappedBean() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        SimpleModule module = new SimpleModule("test", Version.unknownVersion());
        module.addSerializer(String.class, new AnnotatedContextualSerializer());
        mapper.registerModule(module);
        assertEquals("{\"wrapped\":{\"value\":\"see:xyz\"}}", mapper.writeValueAsString(new ContextualBeanWrapper("xyz")));
    }

// com.fasterxml.jackson.databind.contextual.TestContextualSerialization::testMethodAnnotationInArray
    public void testMethodAnnotationInArray() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        SimpleModule module = new SimpleModule("test", Version.unknownVersion());
        module.addSerializer(String.class, new AnnotatedContextualSerializer());
        mapper.registerModule(module);
        ContextualArrayBean beans = new ContextualArrayBean("123");
        assertEquals("{\"beans\":[\"array->123\"]}", mapper.writeValueAsString(beans));
    }

// com.fasterxml.jackson.databind.contextual.TestContextualSerialization::testMethodAnnotationInList
    public void testMethodAnnotationInList() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        SimpleModule module = new SimpleModule("test", Version.unknownVersion());
        module.addSerializer(String.class, new AnnotatedContextualSerializer());
        mapper.registerModule(module);
        ContextualListBean beans = new ContextualListBean("abc");
        assertEquals("{\"beans\":[\"list->abc\"]}", mapper.writeValueAsString(beans));
    }

// com.fasterxml.jackson.databind.contextual.TestContextualSerialization::testMethodAnnotationInMap
    public void testMethodAnnotationInMap() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        SimpleModule module = new SimpleModule("test", Version.unknownVersion());
        module.addSerializer(String.class, new AnnotatedContextualSerializer());
        mapper.registerModule(module);
        ContextualMapBean map = new ContextualMapBean();
        map.beans.put("first", "In Map");
        assertEquals("{\"beans\":{\"first\":\"map->In Map\"}}", mapper.writeValueAsString(map));
    }

// com.fasterxml.jackson.databind.contextual.TestContextualSerialization::testContextualViaAnnotation
    public void testContextualViaAnnotation() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        AnnotatedContextualBean bean = new AnnotatedContextualBean("abc");
        assertEquals("{\"value\":\"prefix->abc\"}", mapper.writeValueAsString(bean));
    }

// com.fasterxml.jackson.databind.contextual.TestContextualWithAnnDeserializer::testAnnotatedContextual
    public void testAnnotatedContextual() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        AnnotatedContextualClassBean bean = mapper.readValue(
                "{\"value\":\"a\"}",
              AnnotatedContextualClassBean.class);
        assertNotNull(bean);
        assertEquals("xyz=a", bean.value.value);
    }

// com.fasterxml.jackson.databind.convert.TestArrayConversions::testNullXform
    public void testNullXform() throws Exception
    {
        
        assertNull(mapper.convertValue(null, Integer.class));
        assertNull(mapper.convertValue(null, String.class));
        assertNull(mapper.convertValue(null, byte[].class));
    }

// com.fasterxml.jackson.databind.convert.TestArrayConversions::testArrayIdentityTransforms
    public void testArrayIdentityTransforms() throws Exception
    {
        
        
        verifyByteArrayConversion(bytes(), byte[].class);
        verifyShortArrayConversion(shorts(), short[].class);
        verifyIntArrayConversion(ints(), int[].class);
        verifyLongArrayConversion(longs(), long[].class);
        
        verifyFloatArrayConversion(floats(), float[].class);
        verifyDoubleArrayConversion(doubles(), float[].class);
    }

// com.fasterxml.jackson.databind.convert.TestArrayConversions::testByteArrayFrom
    public void testByteArrayFrom() throws Exception
    {
        
        byte[] data = _convert("c3VyZS4=", byte[].class);
        byte[] exp = "sure.".getBytes("Ascii");
        verifyIntegralArrays(exp, data, exp.length);
    }

// com.fasterxml.jackson.databind.convert.TestArrayConversions::testShortArrayToX
    public void testShortArrayToX() throws Exception
    {
        short[] data = shorts();
        verifyShortArrayConversion(data, byte[].class);
        verifyShortArrayConversion(data, int[].class);
        verifyShortArrayConversion(data, long[].class);
    }

// com.fasterxml.jackson.databind.convert.TestArrayConversions::testIntArrayToX
    public void testIntArrayToX() throws Exception
    {
        int[] data = ints();
        verifyIntArrayConversion(data, byte[].class);
        verifyIntArrayConversion(data, short[].class);
        verifyIntArrayConversion(data, long[].class);

        List<Number> expNums = _numberList(data, data.length);
        
        List<Integer> actNums = mapper.convertValue(data, new TypeReference<List<Integer>>() {});
        assertEquals(expNums, actNums);
    }

// com.fasterxml.jackson.databind.convert.TestArrayConversions::testLongArrayToX
    public void testLongArrayToX() throws Exception
    {
        long[] data = longs();
        verifyLongArrayConversion(data, byte[].class);
        verifyLongArrayConversion(data, short[].class);
        verifyLongArrayConversion(data, int[].class);
 
        List<Number> expNums = _numberList(data, data.length);
        List<Long> actNums = mapper.convertValue(data, new TypeReference<List<Long>>() {});
        assertEquals(expNums, actNums);        
    }

// com.fasterxml.jackson.databind.convert.TestArrayConversions::testOverflows
    public void testOverflows()
    {
        
        try {
            mapper.convertValue(new int[] { 1000 }, byte[].class);
        } catch (IllegalArgumentException e) {
            verifyException(e, OVERFLOW_MSG_BYTE);
        }
        
        try {
            mapper.convertValue(new int[] { -99999 }, short[].class);
        } catch (IllegalArgumentException e) {
            verifyException(e, OVERFLOW_MSG);
        }
        
        try {
            mapper.convertValue(new long[] { Long.MAX_VALUE }, int[].class);
        } catch (IllegalArgumentException e) {
            verifyException(e, OVERFLOW_MSG);
        }
        
        BigInteger biggie = BigInteger.valueOf(Long.MAX_VALUE);
        biggie.add(BigInteger.ONE);
        List<BigInteger> l = new ArrayList<BigInteger>();
        l.add(biggie);
        try {
            mapper.convertValue(l, int[].class);
        } catch (IllegalArgumentException e) {
            verifyException(e, OVERFLOW_MSG);
        }
        
    }

// com.fasterxml.jackson.databind.convert.TestBeanConversions::testBeanConvert
    public void testBeanConvert()
    {
        
        PointStrings input = new PointStrings("37", "-9");
        Point point = MAPPER.convertValue(input, Point.class);
        assertEquals(37, point.x);
        assertEquals(-9, point.y);
        
        assertEquals(-13, point.z);
    }

// com.fasterxml.jackson.databind.convert.TestBeanConversions::testErrorReporting
    public void testErrorReporting() throws Exception
    {
        
        
        try {
            MAPPER.readValue("{\"unknownProp\":true}", BooleanBean.class);
        } catch (JsonProcessingException e) {
            verifyException(e, "unknownProp");
        }

        
        try {
            MAPPER.readValue("{\"boolProp\":\"foobar\"}", BooleanBean.class);
        } catch (JsonMappingException e) {
            verifyException(e, "from String value 'foobar'");
        }
    }

// com.fasterxml.jackson.databind.convert.TestBeanConversions::testIssue458
    public void testIssue458() throws Exception
    {
        ObjectWrapper a = new ObjectWrapper("foo");
        ObjectWrapper b = new ObjectWrapper(a);
        ObjectWrapper b2 = MAPPER.convertValue(b, ObjectWrapper.class);
        ObjectWrapper a2 = MAPPER.convertValue(b2.getData(), ObjectWrapper.class);
        assertEquals("foo", a2.getData());
    }

// com.fasterxml.jackson.databind.convert.TestBeanConversions::testWrapping
    public void testWrapping() throws Exception
    {
        ObjectMapper wrappingMapper = new ObjectMapper();
        wrappingMapper.enable(DeserializationFeature.UNWRAP_ROOT_VALUE);
        wrappingMapper.enable(SerializationFeature.WRAP_ROOT_VALUE);

        
        _convertAndVerifyPoint(wrappingMapper);

        
        
        wrappingMapper = new ObjectMapper();
        wrappingMapper.enable(DeserializationFeature.UNWRAP_ROOT_VALUE);
        wrappingMapper.disable(SerializationFeature.WRAP_ROOT_VALUE);
        _convertAndVerifyPoint(wrappingMapper);

        wrappingMapper = new ObjectMapper();
        wrappingMapper.disable(DeserializationFeature.UNWRAP_ROOT_VALUE);
        wrappingMapper.enable(SerializationFeature.WRAP_ROOT_VALUE);
        _convertAndVerifyPoint(wrappingMapper);
    }

// com.fasterxml.jackson.databind.convert.TestBeanConversions::testConvertUsingCast
    public void testConvertUsingCast() throws Exception
    {
        String str = new String("foo");
        CharSequence seq = str;
        String result = MAPPER.convertValue(seq, String.class);
        
        assertSame(str, result);
    }

// com.fasterxml.jackson.databind.convert.TestBeanConversions::testNodeConvert
    public void testNodeConvert() throws Exception
    {
        ObjectNode src = (ObjectNode) MAPPER.readTree("{}");
        TreeNode node = src;
        ObjectNode result = MAPPER.treeToValue(node, ObjectNode.class);
        
        assertSame(src, result);
    }

// com.fasterxml.jackson.databind.convert.TestBeanConversions::testIssue11
    public void testIssue11() throws Exception
    {
        
        ObjectNode root = MAPPER.createObjectNode();
        JsonNode n = root;
        ObjectNode ob2 = MAPPER.convertValue(n, ObjectNode.class);
        assertSame(root, ob2);

        JsonNode n2 = MAPPER.convertValue(n, JsonNode.class);
        assertSame(root, n2);
        
        
        String STR = "test";
        CharSequence seq = MAPPER.convertValue(STR, CharSequence.class);
        assertSame(STR, seq);

        
        Leaf l = new Leaf(13);
        Map<?,?> m = MAPPER.convertValue(l, Map.class);
        assertNotNull(m);
        assertEquals(1, m.size());
        assertEquals(Integer.valueOf(13), m.get("value"));

        
        Leaf l2 = MAPPER.convertValue(m, Leaf.class);
        assertEquals(13, l2.value);

        
        Object ob = MAPPER.convertValue(l, Object.class);
        assertNotNull(ob);
        assertEquals(LinkedHashMap.class, ob.getClass());

        
        final Object plaino = new Object();
        
        try {
            m = MAPPER.convertValue(plaino, Map.class);
            fail("Conversion should have failed");
        } catch (IllegalArgumentException e) {
            verifyException(e, "no properties discovered");
        }
        
        ObjectMapper mapper = new ObjectMapper();
        mapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
        try {
            assertEquals("{}", mapper.writeValueAsString(plaino));
        } catch (Exception e) {
            throw (Exception) e.getCause();
        }
        
        m = mapper.convertValue(plaino, Map.class);
        assertNotNull(m);
        assertEquals(0, m.size());
    }

// com.fasterxml.jackson.databind.convert.TestBeanConversions::testConversionIssue288
    public void testConversionIssue288() throws Exception
    {
        String json = MAPPER.writeValueAsString(new ConvertingBean(1, 2));
        
        assertEquals("{\"a\":2,\"b\":4}", json);
     }

// com.fasterxml.jackson.databind.convert.TestConvertingDeserializer::testClassAnnotationSimple
    public void testClassAnnotationSimple() throws Exception
    {
        ConvertingBean bean = objectReader(ConvertingBean.class).readValue("[1,2]");
        assertNotNull(bean);
        assertEquals(1, bean.x);
        assertEquals(2, bean.y);
    }

// com.fasterxml.jackson.databind.convert.TestConvertingDeserializer::testClassAnnotationForLists
    public void testClassAnnotationForLists() throws Exception
    {
        ConvertingBeanContainer container = objectReader(ConvertingBeanContainer.class)
                .readValue("{\"values\":[[1,2],[3,4]]}");
        assertNotNull(container);
        assertNotNull(container.values);
        assertEquals(2, container.values.size());
        assertEquals(4, container.values.get(1).y);
    }

// com.fasterxml.jackson.databind.convert.TestConvertingDeserializer::testPropertyAnnotationSimple
    public void testPropertyAnnotationSimple() throws Exception
    {
        PointWrapper wrapper = objectReader(PointWrapper.class).readValue("{\"value\":[3,4]}");
        assertNotNull(wrapper);
        assertNotNull(wrapper.value);
        assertEquals(3, wrapper.value.x);
        assertEquals(4, wrapper.value.y);
    }

// com.fasterxml.jackson.databind.convert.TestConvertingDeserializer::testPropertyAnnotationLowerCasing
    public void testPropertyAnnotationLowerCasing() throws Exception
    {
        LowerCaseText text = objectReader(LowerCaseText.class).readValue("{\"text\":\"Yay!\"}");
        assertNotNull(text);
        assertNotNull(text.text);
        assertEquals("yay!", text.text);
    }

// com.fasterxml.jackson.databind.convert.TestConvertingDeserializer::testPropertyAnnotationArrayLC
    public void testPropertyAnnotationArrayLC() throws Exception
    {
        LowerCaseTextArray texts = objectReader(LowerCaseTextArray.class).readValue("{\"texts\":[\"ABC\"]}");
        assertNotNull(texts);
        assertNotNull(texts.texts);
        assertEquals(1, texts.texts.length);
        assertEquals("abc", texts.texts[0]);
    }

// com.fasterxml.jackson.databind.convert.TestConvertingDeserializer::testPropertyAnnotationForArrays
    public void testPropertyAnnotationForArrays() throws Exception
    {
        PointListWrapperArray array = objectReader(PointListWrapperArray.class)
                .readValue("{\"values\":[[4,5],[5,4]]}");
        assertNotNull(array);
        assertNotNull(array.values);
        assertEquals(2, array.values.length);
        assertEquals(5, array.values[1].x);
    }

// com.fasterxml.jackson.databind.convert.TestConvertingDeserializer::testPropertyAnnotationForLists
    public void testPropertyAnnotationForLists() throws Exception
    {
        PointListWrapperList array = objectReader(PointListWrapperList.class)
                .readValue("{\"values\":[[7,8],[8,7]]}");
        assertNotNull(array);
        assertNotNull(array.values);
        assertEquals(2, array.values.size());
        assertEquals(7, array.values.get(0).x);
    }

// com.fasterxml.jackson.databind.convert.TestConvertingDeserializer::testPropertyAnnotationForMaps
    public void testPropertyAnnotationForMaps() throws Exception
    {
        PointListWrapperMap map = objectReader(PointListWrapperMap.class)
                .readValue("{\"values\":{\"a\":[1,2]}}");
        assertNotNull(map);
        assertNotNull(map.values);
        assertEquals(1, map.values.size());
        Point p = map.values.get("a");
        assertNotNull(p);
        assertEquals(1, p.x);
        assertEquals(2, p.y);
    }

// com.fasterxml.jackson.databind.convert.TestConvertingSerializer::testClassAnnotationSimple
    public void testClassAnnotationSimple() throws Exception
    {
        String json = objectWriter().writeValueAsString(new ConvertingBean(1, 2));
        assertEquals("[1,2]", json);
    }

// com.fasterxml.jackson.databind.convert.TestConvertingSerializer::testClassAnnotationForLists
    public void testClassAnnotationForLists() throws Exception
    {
        String json = objectWriter().writeValueAsString(new ConvertingBeanContainer(
                new ConvertingBean(1, 2), new ConvertingBean(3, 4)));
        assertEquals("{\"values\":[[1,2],[3,4]]}", json);
    }

// com.fasterxml.jackson.databind.convert.TestConvertingSerializer::testPropertyAnnotationSimple
    public void testPropertyAnnotationSimple() throws Exception
    {
        String json = objectWriter().writeValueAsString(new PointWrapper(3, 4));
        assertEquals("{\"value\":[3,4]}", json);
    }

// com.fasterxml.jackson.databind.convert.TestConvertingSerializer::testPropertyAnnotationForArrays
    public void testPropertyAnnotationForArrays() throws Exception {
        String json = objectWriter().writeValueAsString(new PointListWrapperArray(4, 5));
        assertEquals("{\"values\":[[4,5],[5,4]]}", json);
    }

// com.fasterxml.jackson.databind.convert.TestConvertingSerializer::testPropertyAnnotationForLists
    public void testPropertyAnnotationForLists() throws Exception {
        String json = objectWriter().writeValueAsString(new PointListWrapperList(7, 8));
        assertEquals("{\"values\":[[7,8],[8,7]]}", json);
    }

// com.fasterxml.jackson.databind.convert.TestConvertingSerializer::testPropertyAnnotationForMaps
    public void testPropertyAnnotationForMaps() throws Exception {
        String json = objectWriter().writeValueAsString(new PointListWrapperMap("a", 1, 2));
        assertEquals("{\"values\":{\"a\":[1,2]}}", json);
    }

// com.fasterxml.jackson.databind.convert.TestConvertingSerializer::testIssue359
    public void testIssue359() throws Exception {
        String json = objectWriter().writeValueAsString(new Bean359());
        assertEquals("{\"stuff\":[\"Target\"]}", json);
    }

// com.fasterxml.jackson.databind.convert.TestMapConversions::testMapToMap
    public void testMapToMap()
    {
        Map<String,Integer> input = new LinkedHashMap<String,Integer>();
        input.put("A", Integer.valueOf(3));
        input.put("B", Integer.valueOf(-4));
        Map<AB,String> output = MAPPER.convertValue(input,
                new TypeReference<Map<AB,String>>() { });
        assertEquals(2, output.size());
        assertEquals("3", output.get(AB.A));
        assertEquals("-4", output.get(AB.B));

        
        Map<String,Integer> roundtrip = MAPPER.convertValue(input,
                new TypeReference<TreeMap<String,Integer>>() { });
        assertEquals(2, roundtrip.size());
        assertEquals(Integer.valueOf(3), roundtrip.get("A"));
        assertEquals(Integer.valueOf(-4), roundtrip.get("B"));
    }

// com.fasterxml.jackson.databind.convert.TestMapConversions::testMapToBean
    public void testMapToBean()
    {
        EnumMap<AB,String> map = new EnumMap<AB,String>(AB.class);
        map.put(AB.A, "   17");
        map.put(AB.B, " -1");
        Bean bean = MAPPER.convertValue(map, Bean.class);
        assertEquals(Integer.valueOf(17), bean.A);
        assertEquals(" -1", bean.B);
    }

// com.fasterxml.jackson.databind.convert.TestMapConversions::testBeanToMap
    public void testBeanToMap()
    {
        Bean bean = new Bean();
        bean.A = 129;
        bean.B = "13";
        EnumMap<AB,String> result = MAPPER.convertValue(bean,
                new TypeReference<EnumMap<AB,String>>() { });
        assertEquals("129", result.get(AB.A));
        assertEquals("13", result.get(AB.B));
    }

// com.fasterxml.jackson.databind.convert.TestMapConversions::testIssue287
    public void testIssue287() throws Exception
    {
        
        final ObjectMapper mapper = new ObjectMapper();
        final Request request = new Request();
        final String retString = mapper.writeValueAsString(request);
        assertEquals("{\"hello\":{\"value\":1}}",retString);
    }

// com.fasterxml.jackson.databind.convert.TestPolymorphicUpdateValue::testPolymorphicTest
    public void testPolymorphicTest() throws Exception
    {
         Child c = new Child();
         c.w = 10;
         c.h = 11;
         MAPPER.readerForUpdating(c).readValue("{\"x\":3,\"y\":4,\"w\":111}");
         assertEquals(3, c.x);
         assertEquals(4, c.y);
         assertEquals(111, c.w);
    }

// com.fasterxml.jackson.databind.convert.TestStringConversions::testSimple
    public void testSimple()
    {
        assertEquals(Boolean.TRUE, MAPPER.convertValue("true", Boolean.class));
        assertEquals(Integer.valueOf(-3), MAPPER.convertValue("  -3 ", Integer.class));
        assertEquals(Long.valueOf(77), MAPPER.convertValue("77", Long.class));

        int[] ints = { 1, 2, 3 };
        List<Integer> Ints = new ArrayList<Integer>();
        Ints.add(1);
        Ints.add(2);
        Ints.add(3);
        
        assertArrayEquals(ints, MAPPER.convertValue(Ints, int[].class));
    }

// com.fasterxml.jackson.databind.convert.TestStringConversions::testStringsToInts
    public void testStringsToInts()
    {
        
        assertArrayEquals(new int[] { 1, 2, 3, 4, -1, 0 },
                          MAPPER.convertValue("1  2 3    4  -1 0".split("\\s+"), int[].class));
    }

// com.fasterxml.jackson.databind.convert.TestStringConversions::testBytesToBase64AndBack
    public void testBytesToBase64AndBack() throws Exception
    {
        byte[] input = new byte[] { 1, 2, 3, 4, 5, 6, 7 };
        String encoded = MAPPER.convertValue(input, String.class);
        assertNotNull(encoded);

        assertEquals("AQIDBAUGBw==", encoded);

        
        assertEquals(Base64Variants.MIME.encode(input), encoded);

        byte[] result = MAPPER.convertValue(encoded, byte[].class);
        assertArrayEquals(input, result);
    }

// com.fasterxml.jackson.databind.convert.TestStringConversions::testBytestoCharArray
    public void testBytestoCharArray() throws Exception
    {
        byte[] input = new byte[] { 1, 2, 3, 4, 5, 6, 7 };
        
        char[] expEncoded = MAPPER.convertValue(input, String.class).toCharArray();
        
        char[] actEncoded = MAPPER.convertValue(input, char[].class);
        assertArrayEquals(expEncoded, actEncoded);
    }

// com.fasterxml.jackson.databind.convert.TestUpdateValue::testBeanUpdate
    public void testBeanUpdate() throws Exception
    {
        Bean bean = new Bean();
        assertEquals("b", bean.b);
        assertEquals(3, bean.c.length);
        assertNull(bean.child);

        Object ob = MAPPER.readerForUpdating(bean).readValue("{ \"b\":\"x\", \"c\":[4,5], \"child\":{ \"a\":\"y\"} }");
        assertSame(ob, bean);

        assertEquals("a", bean.a);
        assertEquals("x", bean.b);
        assertArrayEquals(new int[] { 4, 5 }, bean.c);

        Bean child = bean.child;
        assertNotNull(child);
        assertEquals("y", child.a);
        assertEquals("b", child.b);
        assertArrayEquals(new int[] { 1, 2, 3 }, child.c);
        assertNull(child.child);
    }

// com.fasterxml.jackson.databind.convert.TestUpdateValue::testListUpdate
    public void testListUpdate() throws Exception
    {
        List<String> strs = new ArrayList<String>();
        strs.add("a");
        
        Object ob = MAPPER.readerForUpdating(strs).readValue("[ \"b\", \"c\", \"d\" ]");
        assertSame(strs, ob);
        assertEquals(4, strs.size());
        assertEquals("a", strs.get(0));
        assertEquals("b", strs.get(1));
        assertEquals("c", strs.get(2));
        assertEquals("d", strs.get(3));
    }

// com.fasterxml.jackson.databind.convert.TestUpdateValue::testMapUpdate
    public void testMapUpdate() throws Exception
    {
        Map<String,String> strs = new HashMap<String,String>();
        strs.put("a", "a");
        strs.put("b", "b");
        
        Object ob = MAPPER.readerForUpdating(strs).readValue("{ \"c\" : \"c\", \"a\" : \"z\" }");
        assertSame(strs, ob);
        assertEquals(3, strs.size());
        assertEquals("z", strs.get("a"));
        assertEquals("b", strs.get("b"));
        assertEquals("c", strs.get("c"));
    }

// com.fasterxml.jackson.databind.convert.TestUpdateValue::testUpdateSequence
    public void testUpdateSequence() throws Exception
    {
        XYBean toUpdate = new XYBean();
        Iterator<XYBean> it = MAPPER.readerForUpdating(toUpdate).readValues(
                "{\"x\":1,\"y\":2}\n{\"x\":16}{\"y\":37}");

        assertTrue(it.hasNext());
        XYBean value = it.next();
        assertSame(toUpdate, value);
        assertEquals(1, value.x);
        assertEquals(2, value.y);

        assertTrue(it.hasNext());
        value = it.next();
        assertSame(toUpdate, value);
        assertEquals(16, value.x);
        assertEquals(2, value.y); 

        assertTrue(it.hasNext());
        value = it.next();
        assertSame(toUpdate, value);
        assertEquals(16, value.x); 
        assertEquals(37, value.y);
        
        assertFalse(it.hasNext());
    }

// com.fasterxml.jackson.databind.convert.TestUpdateValue::testUpdatingWithViews
    public void testUpdatingWithViews() throws Exception
    {
        Updateable bean = new Updateable();
        bean.num = 100;
        bean.str = "test";
        Updateable result = MAPPER.readerForUpdating(bean)
                .withView(TextView.class)
                .readValue("{\"num\": 10, \"str\":\"foobar\"}");    
        assertSame(bean, result);

        assertEquals(100, bean.num);
        assertEquals("foobar", bean.str);
    }

// com.fasterxml.jackson.databind.creators.SingleArgCreatorTest::testNamedSingleArg
    public void testNamedSingleArg() throws Exception
    {
        SingleNamedStringBean bean = MAPPER.readValue(quote("foobar"),
                SingleNamedStringBean.class);
        assertEquals("foobar", bean._ss);
    }

// com.fasterxml.jackson.databind.creators.TestBuilderSimple::testSimple
    public void testSimple() throws Exception
    {
    	String json = "{\"x\":1,\"y\":2}";
    	Object o = mapper.readValue(json, ValueClassXY.class);
    	assertNotNull(o);
    	assertSame(ValueClassXY.class, o.getClass());
    	ValueClassXY value = (ValueClassXY) o;
    	
    	assertEquals(value._x, 2);
    	assertEquals(value._y, 3);
    }

// com.fasterxml.jackson.databind.creators.TestBuilderSimple::testMultiAccess
    public void testMultiAccess() throws Exception
    {
    	String json = "{\"c\":3,\"a\":2,\"b\":-9}";
    	ValueClassABC value = mapper.readValue(json, ValueClassABC.class);
    	assertNotNull(value);
    	
    	assertEquals(value.a, 2);
    	assertEquals(value.b, -9);
    	assertEquals(value.c, 3);
    }

// com.fasterxml.jackson.databind.creators.TestBuilderSimple::testImmutable
    public void testImmutable() throws Exception
    {
        final String json = "{\"value\":13}";
        ValueImmutable value = mapper.readValue(json, ValueImmutable.class);        
        assertEquals(13, value.value);
    }

// com.fasterxml.jackson.databind.creators.TestBuilderSimple::testCustomWith
    public void testCustomWith() throws Exception
    {
        final String json = "{\"value\":1}";
        ValueFoo value = mapper.readValue(json, ValueFoo.class);        
        assertEquals(1, value.value);
    }

// com.fasterxml.jackson.databind.creators.TestBuilderSimple::testWithCreator
    public void testWithCreator() throws Exception
    {
        final String json = "{\"a\":1,\"c\":3,\"b\":2}";
        CreatorValue value = mapper.readValue(json, CreatorValue.class);        
        assertEquals(1, value.a);
        assertEquals(2, value.b);
        assertEquals(3, value.c);
    }

// com.fasterxml.jackson.databind.creators.TestConstructFromMap::testViaConstructor
    public void testViaConstructor() throws Exception
    {
        ObjectMapper m = new ObjectMapper();
        ConstructorFromMap result = m.readValue
            ("{ \"x\":1, \"y\" : \"abc\" }", ConstructorFromMap.class);
        assertEquals(1, result._x);
        assertEquals("abc", result._y);
    }

// com.fasterxml.jackson.databind.creators.TestConstructFromMap::testViaFactory
    public void testViaFactory() throws Exception
    {
        ObjectMapper m = new ObjectMapper();
        FactoryFromPoint result = m.readValue("{ \"x\" : 3, \"y\" : 4 }", FactoryFromPoint.class);
        assertEquals(3, result._x);
        assertEquals(4, result._y);
    }

// com.fasterxml.jackson.databind.creators.TestConstructFromMap::testViaFactoryUsingString
    public void testViaFactoryUsingString() throws Exception
    {
        ObjectMapper m = new ObjectMapper();
        FactoryFromDecimalString result = m.readValue("\"12.57\"", FactoryFromDecimalString.class);
        assertNotNull(result);
        assertEquals(12, result._value);
    }

// com.fasterxml.jackson.databind.creators.TestCreatorNullValue::testUsesDeserializersNullValue
    public void testUsesDeserializersNullValue() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new TestModule());
        Container container = mapper.readValue("{}", Container.class);
        assertEquals(NULL_CONTAINED, container.contained);
    }

// com.fasterxml.jackson.databind.creators.TestCreators::testSimpleConstructor
    public void testSimpleConstructor() throws Exception
    {
        ConstructorBean bean = MAPPER.readValue("{ \"x\" : 42 }", ConstructorBean.class);
        assertEquals(42, bean.x);
    }

// com.fasterxml.jackson.databind.creators.TestCreators::testNoArgsFactory
    public void testNoArgsFactory() throws Exception
    {
        NoArgFactoryBean value = MAPPER.readValue("{\"y\":13}", NoArgFactoryBean.class);
        assertEquals(13, value.y);
        assertEquals(123, value.x);
    }

// com.fasterxml.jackson.databind.creators.TestCreators::testSimpleDoubleConstructor
    public void testSimpleDoubleConstructor() throws Exception
    {
        Double exp = new Double("0.25");
        DoubleConstructorBean bean = MAPPER.readValue(exp.toString(), DoubleConstructorBean.class);
        assertEquals(exp, bean.d);
    }

// com.fasterxml.jackson.databind.creators.TestCreators::testSimpleBooleanConstructor
    public void testSimpleBooleanConstructor() throws Exception
    {
        BooleanConstructorBean bean = MAPPER.readValue(" true ", BooleanConstructorBean.class);
        assertEquals(Boolean.TRUE, bean.b);

        BooleanConstructorBean2 bean2 = MAPPER.readValue(" true ", BooleanConstructorBean2.class);
        assertTrue(bean2.b);
    }

// com.fasterxml.jackson.databind.creators.TestCreators::testSimpleFactory
    public void testSimpleFactory() throws Exception
    {
        FactoryBean bean = MAPPER.readValue("{ \"f\" : 0.25 }", FactoryBean.class);
        assertEquals(0.25, bean.d);
    }

// com.fasterxml.jackson.databind.creators.TestCreators::testLongFactory
    public void testLongFactory() throws Exception
    {
        long VALUE = 123456789000L;
        LongFactoryBean bean = MAPPER.readValue(String.valueOf(VALUE), LongFactoryBean.class);
        assertEquals(VALUE, bean.value);
    }

// com.fasterxml.jackson.databind.creators.TestCreators::testStringFactory
    public void testStringFactory() throws Exception
    {
        String str = "abc";
        StringFactoryBean bean = MAPPER.readValue(quote(str), StringFactoryBean.class);
        assertEquals(str, bean.value);
    }

// com.fasterxml.jackson.databind.creators.TestCreators::testStringFactoryAlt
    public void testStringFactoryAlt() throws Exception
    {
        String str = "xyz";
        FromStringBean bean = MAPPER.readValue(quote(str), FromStringBean.class);
        assertEquals(str, bean.value);
    }

// com.fasterxml.jackson.databind.creators.TestCreators::testConstructorCreator
    public void testConstructorCreator() throws Exception
    {
        CreatorBean bean = MAPPER.readValue
            ("{ \"a\" : \"xyz\", \"x\" : 12 }", CreatorBean.class);
        assertEquals(13, bean.x);
        assertEquals("ctor:xyz", bean.a);
    }

// com.fasterxml.jackson.databind.creators.TestCreators::testConstructorAndProps
    public void testConstructorAndProps() throws Exception
    {
        ConstructorAndPropsBean bean = MAPPER.readValue
            ("{ \"a\" : \"1\", \"b\": 2, \"c\" : true }", ConstructorAndPropsBean.class);
        assertEquals(1, bean.a);
        assertEquals(2, bean.b);
        assertEquals(true, bean.c);
    }

// com.fasterxml.jackson.databind.creators.TestCreators::testFactoryAndProps
    public void testFactoryAndProps() throws Exception
    {
        FactoryAndPropsBean bean = MAPPER.readValue
            ("{ \"a\" : [ false, true, false ], \"b\": 2, \"c\" : -1 }", FactoryAndPropsBean.class);
        assertEquals(2, bean.arg2);
        assertEquals(-1, bean.arg3);
        boolean[] arg1 = bean.arg1;
        assertNotNull(arg1);
        assertEquals(3, arg1.length);
        assertFalse(arg1[0]);
        assertTrue(arg1[1]);
        assertFalse(arg1[2]);
    }

// com.fasterxml.jackson.databind.creators.TestCreators::testMultipleCreators
    public void testMultipleCreators() throws Exception
    {
        MultiBean bean = MAPPER.readValue("123", MultiBean.class);
        assertEquals(Integer.valueOf(123), bean.value);
        bean = MAPPER.readValue(quote("abc"), MultiBean.class);
        assertEquals("abc", bean.value);
        bean = MAPPER.readValue("0.25", MultiBean.class);
        assertEquals(Double.valueOf(0.25), bean.value);
    }

// com.fasterxml.jackson.databind.creators.TestCreators::testDeferredConstructorAndProps
    public void testDeferredConstructorAndProps() throws Exception
    {
        DeferredConstructorAndPropsBean bean = MAPPER.readValue
            ("{ \"propB\" : \"...\", \"createA\" : [ 1 ], \"propA\" : null }",
             DeferredConstructorAndPropsBean.class);

        assertEquals("...", bean.propB);
        assertNull(bean.propA);
        assertNotNull(bean.createA);
        assertEquals(1, bean.createA.length);
        assertEquals(1, bean.createA[0]);
    }

// com.fasterxml.jackson.databind.creators.TestCreators::testDeferredFactoryAndProps
    public void testDeferredFactoryAndProps() throws Exception
    {
        DeferredFactoryAndPropsBean bean = MAPPER.readValue
            ("{ \"prop\" : \"1\", \"ctor\" : \"2\" }", DeferredFactoryAndPropsBean.class);
        assertEquals("1", bean.prop);
        assertEquals("2", bean.ctor);
    }

// com.fasterxml.jackson.databind.creators.TestCreators::testFactoryCreatorWithMixin
    public void testFactoryCreatorWithMixin() throws Exception
    {
        ObjectMapper m = new ObjectMapper();
        m.addMixInAnnotations(CreatorBean.class, MixIn.class);
        CreatorBean bean = m.readValue
            ("{ \"a\" : \"xyz\", \"x\" : 12 }", CreatorBean.class);
        assertEquals(11, bean.x);
        assertEquals("factory:xyz", bean.a);
    }

// com.fasterxml.jackson.databind.creators.TestCreators::testFactoryCreatorWithRenamingMixin
    public void testFactoryCreatorWithRenamingMixin() throws Exception
    {
        ObjectMapper m = new ObjectMapper();
        m.addMixInAnnotations(FactoryBean.class, FactoryBeanMixIn.class);
        
        FactoryBean bean = m.readValue("{ \"mixed\" :  20.5 }", FactoryBean.class);
        assertEquals(20.5, bean.d);
    }

// com.fasterxml.jackson.databind.creators.TestCreators::testMapWithConstructor
    public void testMapWithConstructor() throws Exception
    {
        MapWithCtor result = MAPPER.readValue
            ("{\"text\":\"abc\", \"entry\":true, \"number\":123, \"xy\":\"yx\"}",
             MapWithCtor.class);
        
        assertEquals(Boolean.TRUE, result.get("entry"));
        assertEquals("yx", result.get("xy"));
        assertEquals(2, result.size());
        
        assertEquals("abc", result._text);
        assertEquals(123, result._number);
    }

// com.fasterxml.jackson.databind.creators.TestCreators::testMapWithFactory
    public void testMapWithFactory() throws Exception
    {
        MapWithFactory result = MAPPER.readValue
            ("{\"x\":\"...\",\"b\":true  }",
             MapWithFactory.class);
        assertEquals("...", result.get("x"));
        assertEquals(1, result.size());
        assertEquals(Boolean.TRUE, result._b);
    }

// com.fasterxml.jackson.databind.creators.TestCreators::testBrokenConstructor
    public void testBrokenConstructor() throws Exception
    {
        try {
             MAPPER.readValue("{ \"x\" : 42 }", BrokenBean.class);
        } catch (JsonMappingException je) {
            verifyException(je, "has no property name");
        }
    }

// com.fasterxml.jackson.databind.creators.TestCreators2::testExceptionFromConstructor
    public void testExceptionFromConstructor() throws Exception
    {
        try {
            MAPPER.readValue("{}", BustedCtor.class);
            fail("Expected exception");
        } catch (JsonMappingException e) {
            verifyException(e, ": foobar");
            
            Throwable t = e.getCause();
            assertNotNull(t);
            assertEquals(IllegalArgumentException.class, t.getClass());
            assertEquals("foobar", t.getMessage());
        }
    }

// com.fasterxml.jackson.databind.creators.TestCreators2::testSimpleConstructor
    public void testSimpleConstructor() throws Exception
    {
        HashTest test = MAPPER.readValue("{\"type\":\"custom\",\"bytes\":\"abc\" }", HashTest.class);
        assertEquals("custom", test.type);
        assertEquals("abc", new String(test.bytes, "UTF-8"));
    }

// com.fasterxml.jackson.databind.creators.TestCreators2::testMissingPrimitives
    public void testMissingPrimitives() throws Exception
    {
        Primitives p = MAPPER.readValue("{}", Primitives.class);
        assertFalse(p.b);
        assertEquals(0, p.x);
        assertEquals(0.0, p.d);
    }

// com.fasterxml.jackson.databind.creators.TestCreators2::testJackson431
    public void testJackson431() throws Exception
    {
        final Test431Container foo = MAPPER.readValue(
                "{\"items\":\n"
                +"[{\"bar\": 0,\n"
                +"\"id\": \"id123\",\n"
                +"\"foo\": 1\n" 
                +"}]}",
                Test431Container.class);
        assertNotNull(foo);
    }

// com.fasterxml.jackson.databind.creators.TestCreators2::testJackson438
    public void testJackson438() throws Exception
    {
        try {
            MAPPER.readValue("{ \"name\":\"foobar\" }", BeanFor438.class);
            fail("Should have failed");
        } catch (Exception e) {
            if (!(e instanceof JsonMappingException)) {
                fail("Should have received JsonMappingException, caught "+e.getClass().getName());
            }
            verifyException(e, "don't like that name");
            
            Throwable t = e.getCause();
            assertNotNull(t);
            assertEquals(IllegalArgumentException.class, t.getClass());
            verifyException(e, "don't like that name");
        }
    }

// com.fasterxml.jackson.databind.creators.TestCreators2::testIssue465
    public void testIssue465() throws Exception
    {
        final String JSON = "{\"A\":12}";

        
        Map<String,Long> map = MAPPER.readValue(JSON, Map.class);
        assertEquals(1, map.size());
        assertEquals(Integer.valueOf(12), map.get("A"));
        
        MapBean bean = MAPPER.readValue(JSON, MapBean.class);
        assertEquals(1, bean.map.size());
        assertEquals(Long.valueOf(12L), bean.map.get("A"));

        
        final String EMPTY_JSON = "{}";

        map = MAPPER.readValue(EMPTY_JSON, Map.class);
        assertEquals(0, map.size());
        
        bean = MAPPER.readValue(EMPTY_JSON, MapBean.class);
        assertEquals(0, bean.map.size());
    }

// com.fasterxml.jackson.databind.creators.TestCreators2::testCreatorWithDupNames
    public void testCreatorWithDupNames() throws Exception
    {
        try {
            MAPPER.readValue("{\"bar\":\"x\"}", BrokenCreatorBean.class);
            fail("Should have caught duplicate creator parameters");
        } catch (JsonMappingException e) {
            verifyException(e, "duplicate creator property \"bar\"");
        }
    }

// com.fasterxml.jackson.databind.creators.TestCreators2::testCreatorMultipleArgumentWithoutAnnotation
    public void testCreatorMultipleArgumentWithoutAnnotation() throws Exception {
        AutoDetectConstructorBean value = MAPPER.readValue("{\"bar\":\"bar\",\"foo\":\"foo\"}", AutoDetectConstructorBean.class);
        assertEquals("bar", value.bar);
        assertEquals("foo", value.foo);
    }

// com.fasterxml.jackson.databind.creators.TestCreators2::testIgnoredSingleArgCtor
    public void testIgnoredSingleArgCtor() throws Exception
    {
        try {
            MAPPER.readValue(quote("abc"), IgnoredCtor.class);
            fail("Should have caught missing constructor problem");
        } catch (JsonMappingException e) {
            verifyException(e, "no single-String constructor/factory method");
        }
    }

// com.fasterxml.jackson.databind.creators.TestCreators2::testAbstractFactory
    public void testAbstractFactory() throws Exception
    {
        AbstractBase bean = MAPPER.readValue("{\"a\":3}", AbstractBase.class);
        assertNotNull(bean);
        AbstractBaseImpl impl = (AbstractBaseImpl) bean;
        assertEquals(1, impl.props.size());
        assertEquals(Integer.valueOf(3), impl.props.get("a"));
    }

// com.fasterxml.jackson.databind.creators.TestCreators2::testCreatorProperties
    public void testCreatorProperties() throws Exception
    {
        Issue700Bean value = MAPPER.readValue("{ \"item\" : \"foo\" }", Issue700Bean.class);
        assertNotNull(value);
    }

// com.fasterxml.jackson.databind.creators.TestCreators3::testMultiCtor421
    public void testMultiCtor421() throws Exception
    {
        MultiCtor bean = MAPPER.readValue(aposToQuotes("{'a':'123','b':'foo'}"), MultiCtor.class);
        assertNotNull(bean);
        assertEquals("123", bean._a);
        assertEquals("foo", bean._b);
    }

// com.fasterxml.jackson.databind.creators.TestCreatorsDelegating::testBooleanDelegate
    public void testBooleanDelegate() throws Exception
    {
        ObjectMapper m = new ObjectMapper();
        
        BooleanBean bb = m.readValue("true", BooleanBean.class);
        assertEquals(Boolean.TRUE, bb.value);

        
        bb = m.readValue(quote("true"), BooleanBean.class);
        assertEquals(Boolean.TRUE, bb.value);
    }

// com.fasterxml.jackson.databind.creators.TestCreatorsDelegating::testWithCtorAndDelegate
    public void testWithCtorAndDelegate() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setInjectableValues(new InjectableValues.Std()
            .addValue(String.class, "Pooka")
            );
        CtorBean711 bean = null;
        try {
            bean = mapper.readValue("38", CtorBean711.class);
        } catch (JsonMappingException e) {
            fail("Did not expect problems, got: "+e.getMessage());
        }
        assertEquals(38, bean.age);
        assertEquals("Pooka", bean.name);
    }

// com.fasterxml.jackson.databind.creators.TestCreatorsDelegating::testWithFactoryAndDelegate
    public void testWithFactoryAndDelegate() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setInjectableValues(new InjectableValues.Std()
            .addValue(String.class, "Fygar")
            );
        FactoryBean711 bean = null;
        try {
            bean = mapper.readValue("38", FactoryBean711.class);
        } catch (JsonMappingException e) {
            fail("Did not expect problems, got: "+e.getMessage());
        }
        assertEquals(38, bean.age);
        assertEquals("Fygar", bean.name1);
        assertEquals("Fygar", bean.name2);
    }

// com.fasterxml.jackson.databind.creators.TestPolymorphicCreators::testManualPolymorphicDog
    public void testManualPolymorphicDog() throws Exception
    {
        
        Animal animal = MAPPER.readValue("{ \"type\":\"dog\", \"name\":\"Fido\", \"barkVolume\" : 95.0 }", Animal.class);
        assertEquals(Dog.class, animal.getClass());
        assertEquals("Fido", animal.name);
        assertEquals(95.0, ((Dog) animal).barkVolume);
    }

// com.fasterxml.jackson.databind.creators.TestPolymorphicCreators::testManualPolymorphicCatBasic
    public void testManualPolymorphicCatBasic() throws Exception
    {
        
        Animal animal = MAPPER.readValue("{ \"name\" : \"Macavity\", \"type\":\"cat\", \"lives\":18, \"likesCream\":false }", Animal.class);
        assertEquals(Cat.class, animal.getClass());
        assertEquals("Macavity", animal.name); 
        Cat cat = (Cat) animal;
        assertEquals(18, cat.lives);
        
        assertEquals(false, cat.likesCream);
    }

// com.fasterxml.jackson.databind.creators.TestPolymorphicCreators::testManualPolymorphicCatWithReorder
    public void testManualPolymorphicCatWithReorder() throws Exception
    {
        
        Animal animal = MAPPER.readValue("{ \"likesCream\":true, \"name\" : \"Venla\", \"type\":\"cat\" }", Animal.class);
        assertEquals(Cat.class, animal.getClass());
        assertEquals("Venla", animal.name);
        
        assertTrue(((Cat) animal).likesCream);
    }

// com.fasterxml.jackson.databind.creators.TestPolymorphicCreators::testManualPolymorphicWithNumbered
    public void testManualPolymorphicWithNumbered() throws Exception
    {
         final ObjectWriter w = MAPPER.writerWithType(AbstractRoot.class);
         final ObjectReader r = MAPPER.reader(AbstractRoot.class);

         AbstractRoot input = AbstractRoot.make(1, "oh hai!");
         String json = w.writeValueAsString(input);
         AbstractRoot result = r.readValue(json);
         assertNotNull(result);
         assertEquals("oh hai!", result.getOpt());
    }

// com.fasterxml.jackson.databind.creators.TestValueInstantiator::testCustomBeanInstantiator
    public void testCustomBeanInstantiator() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new MyModule(MyBean.class, new MyBeanInstantiator()));
        MyBean bean = mapper.readValue("{}", MyBean.class);
        assertNotNull(bean);
        assertEquals("secret!", bean._secret);
    }

// com.fasterxml.jackson.databind.creators.TestValueInstantiator::testCustomListInstantiator
    public void testCustomListInstantiator() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new MyModule(MyList.class, new MyListInstantiator()));
        MyList result = mapper.readValue("[]", MyList.class);
        assertNotNull(result);
        assertEquals(MyList.class, result.getClass());
        assertEquals(0, result.size());
    }

// com.fasterxml.jackson.databind.creators.TestValueInstantiator::testCustomMapInstantiator
    public void testCustomMapInstantiator() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new MyModule(MyMap.class, new MyMapInstantiator()));
        MyMap result = mapper.readValue("{ \"a\":\"b\" }", MyMap.class);
        assertNotNull(result);
        assertEquals(MyMap.class, result.getClass());
        assertEquals(1, result.size());
    }

// com.fasterxml.jackson.databind.creators.TestValueInstantiator::testDelegateBeanInstantiator
    public void testDelegateBeanInstantiator() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new MyModule(MyBean.class, new MyDelegateBeanInstantiator()));
        MyBean bean = mapper.readValue("123", MyBean.class);
        assertNotNull(bean);
        assertEquals("123", bean._secret);
    }

// com.fasterxml.jackson.databind.creators.TestValueInstantiator::testDelegateListInstantiator
    public void testDelegateListInstantiator() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new MyModule(MyList.class, new MyDelegateListInstantiator()));
        MyList result = mapper.readValue("123", MyList.class);
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(Integer.valueOf(123), result.get(0));
    }

// com.fasterxml.jackson.databind.creators.TestValueInstantiator::testDelegateMapInstantiator
    public void testDelegateMapInstantiator() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new MyModule(MyMap.class, new MyDelegateMapInstantiator()));
        MyMap result = mapper.readValue("123", MyMap.class);
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(Integer.valueOf(123), result.values().iterator().next());
    }

// com.fasterxml.jackson.databind.creators.TestValueInstantiator::testPropertyBasedBeanInstantiator
    public void testPropertyBasedBeanInstantiator() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new MyModule(CreatorBean.class,
                new InstantiatorBase() {
                    @Override
                    public boolean canCreateFromObjectWith() { return true; }
        
                    @Override
                    public CreatorProperty[] getFromObjectArguments(DeserializationConfig config) {
                        return  new CreatorProperty[] {
                                new CreatorProperty(new PropertyName("secret"), config.constructType(String.class), null,
                                        null, null, null, 0, null,
                                        PropertyMetadata.STD_REQUIRED)
                        };
                    }
        
                    @Override
                    public Object createFromObjectWith(DeserializationContext ctxt, Object[] args) {
                        return new CreatorBean((String) args[0]);
                    }
        }));
        CreatorBean bean = mapper.readValue("{\"secret\":123,\"value\":37}", CreatorBean.class);
        assertNotNull(bean);
        assertEquals("123", bean._secret);
    }

// com.fasterxml.jackson.databind.creators.TestValueInstantiator::testPropertyBasedMapInstantiator
    public void testPropertyBasedMapInstantiator() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new MyModule(MyMap.class, new CreatorMapInstantiator()));
        MyMap result = mapper.readValue("{\"name\":\"bob\", \"x\":\"y\"}", MyMap.class);
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("bob", result.get("bob"));
        assertEquals("y", result.get("x"));
    }

// com.fasterxml.jackson.databind.creators.TestValueInstantiator::testBeanFromString
    public void testBeanFromString() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new MyModule(MysteryBean.class,
                new InstantiatorBase() {
                    @Override
                    public boolean canCreateFromString() { return true; }
                    
                    @Override
                    public Object createFromString(DeserializationContext ctxt, String value) {
                        return new MysteryBean(value);
                    }
        }));
        MysteryBean result = mapper.readValue(quote("abc"), MysteryBean.class);
        assertNotNull(result);
        assertEquals("abc", result.value);
    }

// com.fasterxml.jackson.databind.creators.TestValueInstantiator::testBeanFromInt
    public void testBeanFromInt() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new MyModule(MysteryBean.class,
                new InstantiatorBase() {
                    @Override
                    public boolean canCreateFromInt() { return true; }
                    
                    @Override
                    public Object createFromInt(DeserializationContext ctxt, int value) {
                        return new MysteryBean(value+1);
                    }
        }));
        MysteryBean result = mapper.readValue("37", MysteryBean.class);
        assertNotNull(result);
        assertEquals(Integer.valueOf(38), result.value);
    }

// com.fasterxml.jackson.databind.creators.TestValueInstantiator::testBeanFromLong
    public void testBeanFromLong() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new MyModule(MysteryBean.class,
                new InstantiatorBase() {
                    @Override
                    public boolean canCreateFromLong() { return true; }
                    
                    @Override
                    public Object createFromLong(DeserializationContext ctxt, long value) {
                        return new MysteryBean(value+1L);
                    }
        }));
        MysteryBean result = mapper.readValue("9876543210", MysteryBean.class);
        assertNotNull(result);
        assertEquals(Long.valueOf(9876543211L), result.value);
    }

// com.fasterxml.jackson.databind.creators.TestValueInstantiator::testBeanFromDouble
    public void testBeanFromDouble() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new MyModule(MysteryBean.class,
                new InstantiatorBase() {
                    @Override
                    public boolean canCreateFromDouble() { return true; }

                    @Override
                    public Object createFromDouble(DeserializationContext ctxt, double value) {
                        return new MysteryBean(2.0 * value);
                    }
        }));
        MysteryBean result = mapper.readValue("0.25", MysteryBean.class);
        assertNotNull(result);
        assertEquals(Double.valueOf(0.5), result.value);
    }

// com.fasterxml.jackson.databind.creators.TestValueInstantiator::testBeanFromBoolean
    public void testBeanFromBoolean() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new MyModule(MysteryBean.class,
                new InstantiatorBase() {
                    @Override
                    public boolean canCreateFromBoolean() { return true; }
                    
                    @Override
                    public Object createFromBoolean(DeserializationContext ctxt, boolean value) {
                        return new MysteryBean(Boolean.valueOf(value));
                    }
        }));
        MysteryBean result = mapper.readValue("true", MysteryBean.class);
        assertNotNull(result);
        assertEquals(Boolean.TRUE, result.value);
    }

// com.fasterxml.jackson.databind.creators.TestValueInstantiator::testPolymorphicCreatorBean
    public void testPolymorphicCreatorBean() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new MyModule(PolymorphicBeanBase.class, new PolymorphicBeanInstantiator()));
        String JSON = "{\"type\":"+quote(PolymorphicBean.class.getName())+",\"name\":\"Axel\"}";
        PolymorphicBeanBase result = mapper.readValue(JSON, PolymorphicBeanBase.class);
        assertNotNull(result);
        assertSame(PolymorphicBean.class, result.getClass());
        assertEquals("Axel", ((PolymorphicBean) result).name);
    }

// com.fasterxml.jackson.databind.creators.TestValueInstantiator::testJackson633
    public void testJackson633() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        AnnotatedBean bean = mapper.readValue("{}", AnnotatedBean.class);
        assertNotNull(bean);
        assertEquals("foo", bean.a);
        assertEquals(3, bean.b);
    }

// com.fasterxml.jackson.databind.creators.TestValueUpdate::testValueUpdateWithCreator
    public void testValueUpdateWithCreator() throws Exception
    {
        Bean bean = new Bean("abc", "def");
        new ObjectMapper().reader(Bean.class).withValueToUpdate(bean).readValue("{\"a\":\"ghi\",\"b\":\"jkl\"}");
        assertEquals("ghi", bean.getA());
        assertEquals("jkl", bean.getB());
    }

// com.fasterxml.jackson.databind.deser.TestAbstract::testAbstractFailure
    public void testAbstractFailure() throws Exception
    {
        ObjectMapper m = new ObjectMapper();
        try {
            m.readValue("{ \"x\" : 3 }", Abstract.class);
            fail("Should fail on trying to deserialize abstract type");
        } catch (JsonProcessingException e) {
            verifyException(e, "can not construct");
        }
    }

// com.fasterxml.jackson.databind.deser.TestAnnotationIgnore::testSimpleIgnore
    public void testSimpleIgnore() throws Exception
    {
        SizeClassIgnore result = MAPPER.readValue
            ("{ \"x\":1, \"y\" : 2 }",
             SizeClassIgnore.class);
        
        assertEquals(1, result._x);
        assertEquals(0, result._y);
    }

// com.fasterxml.jackson.databind.deser.TestAnnotationIgnore::testFailOnIgnore
    public void testFailOnIgnore() throws Exception
    {
        ObjectReader r = MAPPER.reader(NoYOrZ.class);
        
        
        NoYOrZ result = r.readValue(aposToQuotes("{'x':3}"));
        assertEquals(3, result.x);
        assertEquals(1, result.y);

        
        r = r.with(DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES);
        try {
            result = r.readValue(aposToQuotes("{'x':3, 'y':4}"));
            fail("Should fail");
        } catch (JsonMappingException e) {
            verifyException(e, "Ignored field");
        }

        
        try {
            result = r.readValue(aposToQuotes("{'z':2 }"));
            fail("Should fail");
        } catch (JsonMappingException e) {
            verifyException(e, "Ignored field");
        }
    }

// com.fasterxml.jackson.databind.deser.TestAnnotationUsing::testClassDeserializer
    public void testClassDeserializer() throws Exception
    {
        ObjectMapper m = new ObjectMapper();
        ValueClass result = m.readValue("  123  ", ValueClass.class);
        assertEquals(123, result._a);
    }

// com.fasterxml.jackson.databind.deser.TestAnnotationUsing::testMethodDeserializer
    public void testMethodDeserializer() throws Exception
    {
        ObjectMapper m = new ObjectMapper();
        
        MethodBean result = m.readValue(" { \"ints\" : 3 } ", MethodBean.class);
        assertNotNull(result);
        int[] ints = result._ints;
        assertNotNull(ints);
        assertEquals(1, ints.length);
        assertEquals(3, ints[0]);
    }

// com.fasterxml.jackson.databind.deser.TestAnnotationUsing::testArrayContentUsing
    public void testArrayContentUsing() throws Exception
    {
        ObjectMapper m = new ObjectMapper();
        ArrayBean result = m.readValue(" { \"values\" : [ 1, 2, 3 ] } ", ArrayBean.class);
        assertNotNull(result);
        Object[] obs = result.values;
        assertNotNull(obs);
        assertEquals(3, obs.length);
        assertEquals(ValueClass.class, obs[0].getClass());
        assertEquals(1, ((ValueClass) obs[0])._a);
        assertEquals(ValueClass.class, obs[1].getClass());
        assertEquals(2, ((ValueClass) obs[1])._a);
        assertEquals(ValueClass.class, obs[2].getClass());
        assertEquals(3, ((ValueClass) obs[2])._a);
    }

// com.fasterxml.jackson.databind.deser.TestAnnotationUsing::testListContentUsing
    public void testListContentUsing() throws Exception
    {
        ObjectMapper m = new ObjectMapper();
        ListBean result = m.readValue(" { \"values\" : [ 1, 2, 3 ] } ", ListBean.class);
        assertNotNull(result);
        List<Object> obs = result.values;
        assertNotNull(obs);
        assertEquals(3, obs.size());
        assertEquals(ValueClass.class, obs.get(0).getClass());
        assertEquals(1, ((ValueClass) obs.get(0))._a);
        assertEquals(ValueClass.class, obs.get(1).getClass());
        assertEquals(2, ((ValueClass) obs.get(1))._a);
        assertEquals(ValueClass.class, obs.get(2).getClass());
        assertEquals(3, ((ValueClass) obs.get(2))._a);
    }

// com.fasterxml.jackson.databind.deser.TestAnnotationUsing::testMapContentUsing
    public void testMapContentUsing() throws Exception
    {
        ObjectMapper m = new ObjectMapper();
        MapBean result = m.readValue(" { \"values\" : { \"a\": 1, \"b\":2 } } ", MapBean.class);
        assertNotNull(result);
        Map<String,Object> map = result.values;
        assertNotNull(map);
        assertEquals(2, map.size());
        assertEquals(ValueClass.class, map.get("a").getClass());
        assertEquals(1, ((ValueClass) map.get("a"))._a);
        assertEquals(ValueClass.class, map.get("b").getClass());
        assertEquals(2, ((ValueClass) map.get("b"))._a);
    }

// com.fasterxml.jackson.databind.deser.TestAnnotationUsing::testMapKeyUsing
    public void testMapKeyUsing() throws Exception
    {
        ObjectMapper m = new ObjectMapper();
        MapKeyBean result = m.readValue(" { \"values\" : { \"a\": true } } ", MapKeyBean.class);
        assertNotNull(result);
        Map<Object,Object> map = result.values;
        assertNotNull(map);
        assertEquals(1, map.size());
        Map.Entry<Object,Object> en = map.entrySet().iterator().next();
        assertEquals(String[].class, en.getKey().getClass());
        assertEquals(Boolean.TRUE, en.getValue());
    }

// com.fasterxml.jackson.databind.deser.TestAnnotationUsing::testRootValueWithCustomKey
    public void testRootValueWithCustomKey() throws Exception
    {
        ObjectMapper m = new ObjectMapper();
        MapKeyMap result = m.readValue(" { \"a\": 13 } ", MapKeyMap.class);
        assertNotNull(result);
        assertNotNull(result);
        assertEquals(1, result.size());
        Map.Entry<Object,Object> en = result.entrySet().iterator().next();
        assertEquals(ValueClass.class, en.getValue().getClass());
        assertEquals(13, ((ValueClass) en.getValue())._a);
        assertEquals(String[].class, en.getKey().getClass());
    }

// com.fasterxml.jackson.databind.deser.TestAnyProperties::testSimpleMapImitation
    public void testSimpleMapImitation() throws Exception
    {
        MapImitator mapHolder = MAPPER.readValue
            ("{ \"a\" : 3, \"b\" : true, \"c\":[1,2,3] }", MapImitator.class);
        Map<String,Object> result = mapHolder._map;
        assertEquals(3, result.size());
        assertEquals(Integer.valueOf(3), result.get("a"));
        assertEquals(Boolean.TRUE, result.get("b"));
        Object ob = result.get("c");
        assertTrue(ob instanceof List<?>);
        List<?> l = (List<?>)ob;
        assertEquals(3, l.size());
        assertEquals(Integer.valueOf(3), l.get(2));
    }

// com.fasterxml.jackson.databind.deser.TestAnyProperties::testSimpleTyped
    public void testSimpleTyped() throws Exception
    {
        MapImitatorWithValue mapHolder = MAPPER.readValue
            ("{ \"a\" : [ 3, -1 ], \"b\" : [ ] }", MapImitatorWithValue.class);
        Map<String,int[]> result = mapHolder._map;
        assertEquals(2, result.size());
        assertEquals(new int[] { 3, -1 }, result.get("a"));
        assertEquals(new int[0], result.get("b"));
    }

// com.fasterxml.jackson.databind.deser.TestAnyProperties::testBrokenWithDoubleAnnotations
    public void testBrokenWithDoubleAnnotations() throws Exception
    {
        try {
            @SuppressWarnings("unused")
            Broken b = MAPPER.readValue("{ \"a\" : 3 }", Broken.class);
            fail("Should have gotten an exception");
        } catch (JsonMappingException e) {
            verifyException(e, "Multiple 'any-setters'");
        }
    }

// com.fasterxml.jackson.databind.deser.TestAnyProperties::testIgnored
    public void testIgnored() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        _testIgnorals(mapper);
    }

// com.fasterxml.jackson.databind.deser.TestAnyProperties::testIgnoredPart2
    public void testIgnoredPart2() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        _testIgnorals(mapper);
    }

// com.fasterxml.jackson.databind.deser.TestAnyProperties::testProblem744
    public void testProblem744() throws Exception
    {
        Bean744 bean = MAPPER.readValue("{\"name\":\"Bob\"}", Bean744.class);
        assertNotNull(bean.additionalProperties);
        assertEquals(1, bean.additionalProperties.size());
        assertEquals("Bob", bean.additionalProperties.get("name"));
    }

// com.fasterxml.jackson.databind.deser.TestAnyProperties::testIssue797
    public void testIssue797() throws Exception
    {
        String json = MAPPER.writeValueAsString(new Bean797BaseImpl());
        assertEquals("{}", json);
    }

// com.fasterxml.jackson.databind.deser.TestAnyProperties::testPolymorphic
    public void testPolymorphic() throws Exception
    {
        PolyAnyBean input = new PolyAnyBean();
        input.props.put("a", new Impl("xyz"));
        String json = MAPPER.writeValueAsString(input);
        

        PolyAnyBean result = MAPPER.readValue(json, PolyAnyBean.class);
        assertEquals(1, result.props.size());
        Base ob = result.props.get("a");
        assertNotNull(ob);
        assertTrue(ob instanceof Impl);
        assertEquals("xyz", ((Impl) ob).value);
    }

// com.fasterxml.jackson.databind.deser.TestArrayDeserialization::testUntypedArray
    public void testUntypedArray() throws Exception
    {

        
        String JSON = "[ 1, null, \"x\", true, 2.0 ]";

        Object[] result = MAPPER.readValue(JSON, Object[].class);
        assertNotNull(result);

        assertEquals(5, result.length);

        assertEquals(Integer.valueOf(1), result[0]);
        assertNull(result[1]);
        assertEquals("x", result[2]);
        assertEquals(Boolean.TRUE, result[3]);
        assertEquals(Double.valueOf(2.0), result[4]);
    }

// com.fasterxml.jackson.databind.deser.TestArrayDeserialization::testIntegerArray
    public void testIntegerArray() throws Exception
    {
        final int LEN = 90000;

        

        StringBuilder sb = new StringBuilder();
        sb.append('[');
        for (int i = 0; i < LEN; ++i) {
            if (i > 0) {
                sb.append(',');
            }
            sb.append(i);
        }
        sb.append(']');

        Integer[] result = MAPPER.readValue(sb.toString(), Integer[].class);
        assertNotNull(result);

        assertEquals(LEN, result.length);
        for (int i = 0; i < LEN; ++i) {
            assertEquals(i, result[i].intValue());
        }
    }

// com.fasterxml.jackson.databind.deser.TestArrayDeserialization::testFromEmptyString
    public void testFromEmptyString() throws Exception
    {
        ObjectMapper m = new ObjectMapper();
        m.configure(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, true);
        assertNull(m.readValue(quote(""), Object[].class));
        assertNull( m.readValue(quote(""), String[].class));
        assertNull( m.readValue(quote(""), int[].class));
    }

// com.fasterxml.jackson.databind.deser.TestArrayDeserialization::testFromEmptyString2
    public void testFromEmptyString2() throws Exception
    {
        ObjectMapper m = new ObjectMapper();
        m.configure(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, true);
        m.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
        Product p = m.readValue("{\"thelist\":\"\"}", Product.class);
        assertNotNull(p);
        assertNull(p.thelist);
    }

// com.fasterxml.jackson.databind.deser.TestArrayDeserialization::testUntypedArrayOfArrays
    public void testUntypedArrayOfArrays() throws Exception
    {
        
        final String JSON = "[[[-0.027512,51.503221],[-0.008497,51.503221],[-0.008497,51.509744],[-0.027512,51.509744]]]";

        Object result = MAPPER.readValue(JSON, Object.class);
        assertEquals(ArrayList.class, result.getClass());
        assertNotNull(result);

        

        Object[] array = MAPPER.readValue(JSON, Object[].class);
        assertNotNull(array);
        assertEquals(Object[].class, array.getClass());

        
        ObjectWrapper w = MAPPER.readValue("{\"wrapped\":"+JSON+"}", ObjectWrapper.class);
        assertNotNull(w);
        assertNotNull(w.wrapped);
        assertEquals(ArrayList.class, w.wrapped.getClass());

        ObjectArrayWrapper aw = MAPPER.readValue("{\"wrapped\":"+JSON+"}", ObjectArrayWrapper.class);
        assertNotNull(aw);
        assertNotNull(aw.wrapped);
    }

// com.fasterxml.jackson.databind.deser.TestArrayDeserialization::testStringArray
    public void testStringArray() throws Exception
    {
        final String[] STRS = new String[] {
            "a", "b", "abcd", "", "???", "\"quoted\"", "lf: \n",
        };
        StringWriter sw = new StringWriter();
        JsonGenerator jg = MAPPER.getFactory().createGenerator(sw);
        jg.writeStartArray();
        for (String str : STRS) {
            jg.writeString(str);
        }
        jg.writeEndArray();
        jg.close();

        String[] result = MAPPER.readValue(sw.toString(), String[].class);
        assertNotNull(result);

        assertEquals(STRS.length, result.length);
        for (int i = 0; i < STRS.length; ++i) {
            assertEquals(STRS[i], result[i]);
        }

        
        result = MAPPER.readValue(" [ null ]", String[].class);
        assertNotNull(result);
        assertEquals(1, result.length);
        assertNull(result[0]);
    }

// com.fasterxml.jackson.databind.deser.TestArrayDeserialization::testCharArray
    public void testCharArray() throws Exception
    {
        final String TEST_STR = "Let's just test it? Ok!";
        char[] result = MAPPER.readValue("\""+TEST_STR+"\"", char[].class);
        assertEquals(TEST_STR, new String(result));

        
        result = MAPPER.readValue("[\"a\",\"b\",\"c\"]", char[].class);
        assertEquals("abc", new String(result));
    }

// com.fasterxml.jackson.databind.deser.TestArrayDeserialization::testBooleanArray
    public void testBooleanArray() throws Exception
    {
        boolean[] result = MAPPER.readValue("[ true, false, false ]", boolean[].class);
        assertNotNull(result);
        assertEquals(3, result.length);
        assertTrue(result[0]);
        assertFalse(result[1]);
        assertFalse(result[2]);
    }

// com.fasterxml.jackson.databind.deser.TestArrayDeserialization::testByteArrayAsNumbers
    public void testByteArrayAsNumbers() throws Exception
    {
        final int LEN = 37000;
        StringBuilder sb = new StringBuilder();
        sb.append('[');
        for (int i = 0; i < LEN; ++i) {
            int value = i - 128;
            sb.append((value < 256) ? value : (value & 0x7F));
            sb.append(',');
        }
        sb.append("0]");
        byte[] result = MAPPER.readValue(sb.toString(), byte[].class);
        assertNotNull(result);
        assertEquals(LEN+1, result.length);
        for (int i = 0; i < LEN; ++i) {
            int value = i - 128;
            byte exp = (byte) ((value < 256) ? value : (value & 0x7F));
            if (exp != result[i]) {
                fail("At offset #"+i+" ("+result.length+"), expected "+exp+", got "+result[i]);
            }
            assertEquals(exp, result[i]);
        }
        assertEquals(0, result[LEN]);
    }

// com.fasterxml.jackson.databind.deser.TestArrayDeserialization::testByteArrayAsBase64
    public void testByteArrayAsBase64() throws Exception
    {
        
        JsonFactory jf = new JsonFactory();
        StringWriter sw = new StringWriter();

        int LEN = 9000;
        byte[] TEST = new byte[LEN];
        for (int i = 0; i < LEN; ++i) {
            TEST[i] = (byte) i;
        }

        JsonGenerator jg = jf.createGenerator(sw);
        jg.writeBinary(TEST);
        jg.close();
        String inputData = sw.toString();

        byte[] result = MAPPER.readValue(inputData, byte[].class);
        assertNotNull(result);
        assertArrayEquals(TEST, result);
    }

// com.fasterxml.jackson.databind.deser.TestArrayDeserialization::testByteArraysAsBase64
    public void testByteArraysAsBase64() throws Exception
    {
        JsonFactory jf = new JsonFactory();
        StringWriter sw = new StringWriter(1000);

        final int entryCount = 15;

        JsonGenerator jg = jf.createGenerator(sw);
        jg.writeStartArray();

        byte[][] entries = new byte[entryCount][];
        for (int i = 0; i < entryCount; ++i) {
            byte[] b = new byte[1000 - i * 20];
            for (int x = 0; x < b.length; ++x) {
                b[x] = (byte) (i + x);
            }
            entries[i] = b;
            jg.writeBinary(b);
        }
        jg.writeEndArray();
        jg.close();

        String inputData = sw.toString();

        byte[][] result = MAPPER.readValue(inputData, byte[][].class);
        assertNotNull(result);

        assertEquals(entryCount, result.length);
        for (int i = 0; i < entryCount; ++i) {
            byte[] b = result[i];
            assertArrayEquals("Comparing entry #"+i+"/"+entryCount,entries[i], b);
        }
    }

// com.fasterxml.jackson.databind.deser.TestArrayDeserialization::testByteArraysWith763
    public void testByteArraysWith763() throws Exception
    {
        String[] input = new String[] { "YQ==", "Yg==", "Yw==" };
        byte[][] data = MAPPER.convertValue(input, byte[][].class);
        assertEquals("a", new String(data[0], "US-ASCII"));
        assertEquals("b", new String(data[1], "US-ASCII"));
        assertEquals("c", new String(data[2], "US-ASCII"));
    }

// com.fasterxml.jackson.databind.deser.TestArrayDeserialization::testShortArray
    public void testShortArray() throws Exception
    {
        final int LEN = 31001; 
        StringBuilder sb = new StringBuilder();
        sb.append('[');
        for (int i = 0; i < LEN; ++i) {
            if (i > 0) {
                sb.append(',');
            }
            sb.append(i);
        }
        sb.append(']');

        short[] result = MAPPER.readValue(sb.toString(), short[].class);
        assertNotNull(result);

        assertEquals(LEN, result.length);
        for (int i = 0; i < LEN; ++i) {
            short exp = (short) i;
            assertEquals(exp, result[i]);
        }
    }

// com.fasterxml.jackson.databind.deser.TestArrayDeserialization::testIntArray
    public void testIntArray() throws Exception
    {
        final int LEN = 70000;

        

        StringBuilder sb = new StringBuilder();
        sb.append('[');
        for (int i = 0; i < LEN; ++i) {
            if (i > 0) {
                sb.append(',');
            }
            sb.append(-i);
        }
        sb.append(']');

        int[] result = MAPPER.readValue(sb.toString(), int[].class);
        assertNotNull(result);

        assertEquals(LEN, result.length);
        for (int i = 0; i < LEN; ++i) {
            assertEquals(-i, result[i]);
        }
    }

// com.fasterxml.jackson.databind.deser.TestArrayDeserialization::testLongArray
    public void testLongArray() throws Exception
    {
        final int LEN = 12300;
        StringBuilder sb = new StringBuilder();
        sb.append('[');
        for (int i = 0; i < LEN; ++i) {
            if (i > 0) {
                sb.append(',');
            }
            sb.append(i);
        }
        sb.append(']');

        long[] result = MAPPER.readValue(sb.toString(), long[].class);
        assertNotNull(result);

        assertEquals(LEN, result.length);
        for (int i = 0; i < LEN; ++i) {
            long exp = (long) i;
            assertEquals(exp, result[i]);
        }
    }

// com.fasterxml.jackson.databind.deser.TestArrayDeserialization::testDoubleArray
    public void testDoubleArray() throws Exception
    {
        final int LEN = 7000;
        StringBuilder sb = new StringBuilder();
        sb.append('[');
        for (int i = 0; i < LEN; ++i) {
            
            if (i > 0) {
                sb.append(',');
            }
            sb.append(i).append('.').append(i % 10);
        }
        sb.append(']');

        double[] result = MAPPER.readValue(sb.toString(), double[].class);
        assertNotNull(result);

        assertEquals(LEN, result.length);
        for (int i = 0; i < LEN; ++i) {
            String expStr = String.valueOf(i) + "." + String.valueOf(i % 10);
            String actStr = String.valueOf(result[i]);
            if (!expStr.equals(actStr)) {
                fail("Entry #"+i+"/"+LEN+"; exp '"+expStr+"', got '"+actStr+"'");
            }
        }
    }

// com.fasterxml.jackson.databind.deser.TestArrayDeserialization::testFloatArray
    public void testFloatArray() throws Exception
    {
        final int LEN = 7000;
        StringBuilder sb = new StringBuilder();
        sb.append('[');
        for (int i = 0; i < LEN; ++i) {
            if (i > 0) {
                sb.append(',');
            }
            
            sb.append(i).append('.').append(i % 10);
        }
        sb.append(']');

        float[] result = MAPPER.readValue(sb.toString(), float[].class);
        assertNotNull(result);

        assertEquals(LEN, result.length);
        for (int i = 0; i < LEN; ++i) {
            String expStr = String.valueOf(i) + "." + String.valueOf(i % 10);
            assertEquals(expStr, String.valueOf(result[i]));
        }
    }

// com.fasterxml.jackson.databind.deser.TestArrayDeserialization::testBeanArray
    public void testBeanArray()
        throws Exception
    {
        List<Bean1> src = new ArrayList<Bean1>();

        List<Bean2> b2 = new ArrayList<Bean2>();
        b2.add(new Bean2("a"));
        b2.add(new Bean2("foobar"));
        src.add(new Bean1(1, 2, b2));

        b2 = new ArrayList<Bean2>();
        b2.add(null);
        src.add(new Bean1(4, 5, b2));

        
        StringWriter sw = new StringWriter();

        MAPPER.writeValue(sw, src);

        
        List<Bean1> result = MAPPER.readValue(sw.toString(), new TypeReference<List<Bean1>>() { });
        assertNotNull(result);
        assertEquals(src, result);
    }

// com.fasterxml.jackson.databind.deser.TestArrayDeserialization::testCustomDeserializers
    public void testCustomDeserializers() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        SimpleModule testModule = new SimpleModule("test", Version.unknownVersion());
        testModule.addDeserializer(NonDeserializable[].class, new CustomNonDeserArrayDeserializer());
        mapper.registerModule(testModule);
        
        NonDeserializable[] result = mapper.readValue("[\"a\"]", NonDeserializable[].class);
        assertNotNull(result);
        assertEquals(1, result.length);
        assertEquals("a", result[0].value);
    }

// com.fasterxml.jackson.databind.deser.TestAutoDetect::testPrivateCtor
    public void testPrivateCtor() throws Exception
    {
        
        ObjectMapper m = new ObjectMapper();
        PrivateBean bean = m.readValue("\"abc\"", PrivateBean.class);
        assertEquals("abc", bean.a);

        
        m = new ObjectMapper();
        
        VisibilityChecker<?> vc = m.getVisibilityChecker();
        vc = vc.withCreatorVisibility(JsonAutoDetect.Visibility.PUBLIC_ONLY);
        m.setVisibilityChecker(vc);
        try {
            m.readValue("\"abc\"", PrivateBean.class);
            fail("Expected exception for missing constructor");
        } catch (JsonProcessingException e) {
            verifyException(e, "no single-String constructor/factory");
        }
    }

// com.fasterxml.jackson.databind.deser.TestBasicAnnotations::testSimpleSetter
    public void testSimpleSetter() throws Exception
    {
        SizeClassSetter result = MAPPER.readValue
            ("{ \"other\":3, \"size\" : 2, \"length\" : -999 }",
             SizeClassSetter.class);
                                             
        assertEquals(3, result._other);
        assertEquals(2, result._size);
        assertEquals(-999, result._length);
    }

// com.fasterxml.jackson.databind.deser.TestBasicAnnotations::testSimpleSetter2
    public void testSimpleSetter2() throws Exception
    {
        SizeClassSetter2 result = MAPPER.readValue("{ \"x\": -3 }",
             SizeClassSetter2.class);
        assertEquals(-3, result._x);
    }

// com.fasterxml.jackson.databind.deser.TestBasicAnnotations::testSimpleSetter3
    public void testSimpleSetter3() throws Exception
    {
        SizeClassSetter3 result = MAPPER.readValue
            ("{ \"x\": 128 }",
             SizeClassSetter3.class);
        assertEquals(128, result._x);
    }

// com.fasterxml.jackson.databind.deser.TestBasicAnnotations::testSetterInheritance
    public void testSetterInheritance() throws Exception
    {
        BeanSubClass result = MAPPER.readValue
            ("{ \"x\":1, \"z\" : 3, \"y\" : 2 }",
             BeanSubClass.class);
        assertEquals(1, result._x);
        assertEquals(2, result._y);
        assertEquals(3, result._z);
    }

// com.fasterxml.jackson.databind.deser.TestBasicAnnotations::testImpliedProperty
    public void testImpliedProperty() throws Exception
    {
        BeanWithDeserialize bean = MAPPER.readValue("{\"a\":3}", BeanWithDeserialize.class);
        assertNotNull(bean);
        assertEquals(3, bean.a);
    }

// com.fasterxml.jackson.databind.deser.TestBasicAnnotations::testIssue442PrivateUnwrapped
    public void testIssue442PrivateUnwrapped() throws Exception
    {
        Issue442Bean bean = MAPPER.readValue("{\"i\":5}", Issue442Bean.class);
        assertEquals(5, bean.w.i);
    }

// com.fasterxml.jackson.databind.deser.TestBeanDeserializer::testPropertyRemoval
    public void testPropertyRemoval() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new ModuleImpl(new RemovingModifier("a")));
        Bean bean = mapper.readValue("{\"b\":\"2\"}", Bean.class);
        assertEquals("2", bean.b);
        
        assertEquals("a", bean.a);
    }

// com.fasterxml.jackson.databind.deser.TestBeanDeserializer::testDeserializerReplacement
    public void testDeserializerReplacement() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new ModuleImpl(new ReplacingModifier(new BogusBeanDeserializer("foo", "bar"))));
        Bean bean = mapper.readValue("{\"a\":\"xyz\"}", Bean.class);
        
        assertEquals("foo", bean.a);
        assertEquals("bar", bean.b);
    }

// com.fasterxml.jackson.databind.deser.TestBeanDeserializer::testIssue476
    public void testIssue476() throws Exception
    {
        final String JSON = "{\"value1\" : {\"name\" : \"fruit\", \"value\" : \"apple\"}, \"value2\" : {\"name\" : \"color\", \"value\" : \"red\"}}";
        
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new Issue476Module());
        mapper.readValue(JSON, Issue476Bean.class);

        
        assertEquals(2, Issue476Deserializer.propCount);
    }

// com.fasterxml.jackson.databind.deser.TestBeanDeserializer::testPOJOFromEmptyString
    public void testPOJOFromEmptyString() throws Exception
    {
        
        ObjectMapper mapper = new ObjectMapper();
        try {
            mapper.readValue(quote(""), Bean.class);
            fail("Should not accept Empty String for POJO");
        } catch (JsonProcessingException e) {
            verifyException(e, "from String value");
            assertValidLocation(e.getLocation());
        }

        
        mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, true);
        Bean result = mapper.readValue(quote(""), Bean.class);
        assertNull(result);
    }

// com.fasterxml.jackson.databind.deser.TestBeanDeserializer::testModifyArrayDeserializer
    public void testModifyArrayDeserializer() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new SimpleModule("test")
            .setDeserializerModifier(new ArrayDeserializerModifier()));
        Object[] result = mapper.readValue("[1,2]", Object[].class);
        assertEquals(1, result.length);
        assertEquals("foo", result[0]);
    }

// com.fasterxml.jackson.databind.deser.TestBeanDeserializer::testModifyCollectionDeserializer
    public void testModifyCollectionDeserializer() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new SimpleModule("test")
            .setDeserializerModifier(new CollectionDeserializerModifier())
        );
        List<?> result = mapper.readValue("[1,2]", List.class);
        assertEquals(1, result.size());
        assertEquals("foo", result.get(0));
    }

// com.fasterxml.jackson.databind.deser.TestBeanDeserializer::testModifyMapDeserializer
    public void testModifyMapDeserializer() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new SimpleModule("test")
            .setDeserializerModifier(new MapDeserializerModifier())
        );
        Map<?,?> result = mapper.readValue("{\"a\":1,\"b\":2}", Map.class);
        assertEquals(1, result.size());
        assertEquals("foo", result.get("a"));
    }

// com.fasterxml.jackson.databind.deser.TestBeanDeserializer::testModifyEnumDeserializer
    public void testModifyEnumDeserializer() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new SimpleModule("test")
            .setDeserializerModifier(new EnumDeserializerModifier())
        );
        Object result = mapper.readValue(quote("B"), EnumABC.class);
        assertEquals("foo", result);
    }

// com.fasterxml.jackson.databind.deser.TestBeanDeserializer::testModifyKeyDeserializer
    public void testModifyKeyDeserializer() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new SimpleModule("test")
            .setDeserializerModifier(new KeyDeserializerModifier())
        );
        Map<?,?> result = mapper.readValue("{\"a\":1}", Map.class);
        assertEquals(1, result.size());
        assertEquals("foo", result.entrySet().iterator().next().getKey());
    }

// com.fasterxml.jackson.databind.deser.TestBlocking::testEagerAdvance
    public void testEagerAdvance() throws IOException
    {
        ObjectMapper mapper = new ObjectMapper();
        JsonParser jp = createParserUsingReader("[ 1  ");
        assertToken(JsonToken.START_ARRAY, jp.nextToken());
        assertToken(JsonToken.VALUE_NUMBER_INT, jp.nextToken());

        
        Integer I = mapper.readValue(jp, Integer.class);
        assertEquals(Integer.valueOf(1), I);

        
        try {
            jp.nextToken();
        } catch (IOException ioe) {
            verifyException(ioe, "Unexpected end-of-input: expected close marker for ARRAY");
        }
        jp.close();
    }

// com.fasterxml.jackson.databind.deser.TestCollectionDeserialization::testUntypedList
    public void testUntypedList() throws Exception
    {
        
        String JSON = "[ \"text!\", true, null, 23 ]";

        
        
        Object value = MAPPER.readValue(JSON, Object.class);
        assertNotNull(value);
        assertTrue(value instanceof ArrayList<?>);
        List<?> result = (List<?>) value;

        assertEquals(4, result.size());

        assertEquals("text!", result.get(0));
        assertEquals(Boolean.TRUE, result.get(1));
        assertNull(result.get(2));
        assertEquals(Integer.valueOf(23), result.get(3));
    }

// com.fasterxml.jackson.databind.deser.TestCollectionDeserialization::testExactStringCollection
    public void testExactStringCollection() throws Exception
    {
        
        String JSON = "[ \"a\", \"b\" ]";
        List<String> result = MAPPER.readValue(JSON, new TypeReference<ArrayList<String>>() { });

        assertNotNull(result);
        assertEquals(ArrayList.class, result.getClass());
        assertEquals(2, result.size());

        assertEquals("a", result.get(0));
        assertEquals("b", result.get(1));
    }

// com.fasterxml.jackson.databind.deser.TestCollectionDeserialization::testHashSet
    public void testHashSet() throws Exception
    {
        String JSON = "[ \"KEY1\", \"KEY2\" ]";

        EnumSet<Key> result = MAPPER.readValue(JSON, new TypeReference<EnumSet<Key>>() { });
        assertNotNull(result);
        assertTrue(EnumSet.class.isAssignableFrom(result.getClass()));
        assertEquals(2, result.size());

        assertTrue(result.contains(Key.KEY1));
        assertTrue(result.contains(Key.KEY2));
        assertFalse(result.contains(Key.WHATEVER));
    }

// com.fasterxml.jackson.databind.deser.TestCollectionDeserialization::testCustomDeserializer
    public void testCustomDeserializer() throws IOException
    {
        CustomList result = MAPPER.readValue(quote("abc"), CustomList.class);
        assertEquals(1, result.size());
        assertEquals("abc", result.get(0));
    }

// com.fasterxml.jackson.databind.deser.TestCollectionDeserialization::testImplicitArrays
    public void testImplicitArrays() throws Exception
    {
        
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);

        
        List<Integer> ints = mapper.readValue("4", List.class);
        assertEquals(1, ints.size());
        assertEquals(Integer.valueOf(4), ints.get(0));
        List<String> strings = mapper.readValue(quote("abc"), new TypeReference<ArrayList<String>>() { });
        assertEquals(1, strings.size());
        assertEquals("abc", strings.get(0));
        
        int[] intArray = mapper.readValue("-7", int[].class);
        assertEquals(1, intArray.length);
        assertEquals(-7, intArray[0]);
        String[] stringArray = mapper.readValue(quote("xyz"), String[].class);
        assertEquals(1, stringArray.length);
        assertEquals("xyz", stringArray[0]);

        
        List<XBean> xbeanList = mapper.readValue("{\"x\":4}", new TypeReference<List<XBean>>() { });
        assertEquals(1, xbeanList.size());
        assertEquals(XBean.class, xbeanList.get(0).getClass());

        Object ob = mapper.readValue("{\"x\":29}", XBean[].class);
        XBean[] xbeanArray = (XBean[]) ob;
        assertEquals(1, xbeanArray.length);
        assertEquals(XBean.class, xbeanArray[0].getClass());
    }

// com.fasterxml.jackson.databind.deser.TestCollectionDeserialization::testFromEmptyString
    public void testFromEmptyString() throws Exception
    {
        ObjectReader r = MAPPER.reader(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT);
        List<?> result = r.withType(List.class).readValue(quote(""));
        assertNull(result);
    }

// com.fasterxml.jackson.databind.deser.TestCollectionDeserialization::testArrayBlockingQueue
    public void testArrayBlockingQueue() throws Exception
    {
        
        ArrayBlockingQueue<?> q = MAPPER.readValue("[1, 2, 3]", ArrayBlockingQueue.class);
        assertNotNull(q);
        assertEquals(3, q.size());
        assertEquals(Integer.valueOf(1), q.take());
        assertEquals(Integer.valueOf(2), q.take());
        assertEquals(Integer.valueOf(3), q.take());
    }

// com.fasterxml.jackson.databind.deser.TestCollectionDeserialization::testIterableWithStrings
    public void testIterableWithStrings() throws Exception
    {
        String JSON = "{ \"values\":[\"a\",\"b\"]}";
        ListAsIterable w = MAPPER.readValue(JSON, ListAsIterable.class);
        assertNotNull(w);
        assertNotNull(w.values);
        Iterator<String> it = w.values.iterator();
        assertTrue(it.hasNext());
        assertEquals("a", it.next());
        assertEquals("b", it.next());
        assertFalse(it.hasNext());
    }

// com.fasterxml.jackson.databind.deser.TestCollectionDeserialization::testIterableWithBeans
    public void testIterableWithBeans() throws Exception
    {
        String JSON = "{ \"nums\":[{\"x\":1},{\"x\":2}]}";
        ListAsIterableX w = MAPPER.readValue(JSON, ListAsIterableX.class);
        assertNotNull(w);
        assertNotNull(w.nums);
        Iterator<XBean> it = w.nums.iterator();
        assertTrue(it.hasNext());
        XBean xb = it.next();
        assertNotNull(xb);
        assertEquals(1, xb.x);
        xb = it.next();
        assertEquals(2, xb.x);
        assertFalse(it.hasNext());
    }

// com.fasterxml.jackson.databind.deser.TestCollectionDeserialization::testArrayIndexForExceptions
    public void testArrayIndexForExceptions() throws Exception
    {
        final String OBJECTS_JSON = "[ \"KEY2\", false ]";
        try {
            MAPPER.readValue(OBJECTS_JSON, Key[].class);
            fail("Should not pass");
        } catch (JsonMappingException e) {
            verifyException(e, "Can not deserialize");
            List<JsonMappingException.Reference> refs = e.getPath();
            assertEquals(1, refs.size());
            assertEquals(1, refs.get(0).getIndex());
        }

        try {
            MAPPER.readValue("[ \"xyz\", { } ]", String[].class);
            fail("Should not pass");
        } catch (JsonMappingException e) {
            verifyException(e, "Can not deserialize");
            List<JsonMappingException.Reference> refs = e.getPath();
            assertEquals(1, refs.size());
            assertEquals(1, refs.get(0).getIndex());
        }

        try {
            MAPPER.readValue("{\"keys\":"+OBJECTS_JSON+"}", KeyListBean.class);
            fail("Should not pass");
        } catch (JsonMappingException e) {
            verifyException(e, "Can not deserialize");
            List<JsonMappingException.Reference> refs = e.getPath();
            assertEquals(2, refs.size());
            
            assertEquals(-1, refs.get(0).getIndex());
            assertEquals("keys", refs.get(0).getFieldName());

            
            assertEquals(1, refs.get(1).getIndex());
            assertNull(refs.get(1).getFieldName());
        }
    }

// com.fasterxml.jackson.databind.deser.TestConcurrency::testDeserializerResolution
    public void testDeserializerResolution() throws Exception
    {
        
        final String JSON = "{\"value\":42}";
        
        for (int i = 0; i < 5; ++i) {
            final ObjectMapper mapper = new ObjectMapper();
            Runnable r = new Runnable() {
                @Override
                public void run() {
                    try {
                         mapper.readValue(JSON, Bean.class);
                    } catch (Exception e) { }
                }
            };
            Thread t = new Thread(r);
            t.start();
            
            Thread.sleep(10L);
            
            Bean b = mapper.readValue(JSON, Bean.class);
            
            assertEquals(13, b.value);
            t.join();
        }   
    }

// com.fasterxml.jackson.databind.deser.TestConfig::testEnumIndexes
    public void testEnumIndexes()
    {
        int max = 0;
        
        for (DeserializationFeature f : DeserializationFeature.values()) {
            max = Math.max(max, f.ordinal());
        }
        if (max >= 31) { 
            fail("Max number of DeserializationFeature enums reached: "+max);
        }
    }

// com.fasterxml.jackson.databind.deser.TestConfig::testDefaults
    public void testDefaults()
    {
        ObjectMapper m = new ObjectMapper();
        DeserializationConfig cfg = m.getDeserializationConfig();

        
        assertTrue(cfg.isEnabled(MapperFeature.USE_ANNOTATIONS));
        assertTrue(cfg.isEnabled(MapperFeature.AUTO_DETECT_SETTERS));
        assertTrue(cfg.isEnabled(MapperFeature.AUTO_DETECT_CREATORS));
        assertTrue(cfg.isEnabled(MapperFeature.USE_GETTERS_AS_SETTERS));
        assertTrue(cfg.isEnabled(MapperFeature.CAN_OVERRIDE_ACCESS_MODIFIERS));

        assertFalse(cfg.isEnabled(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS));
        assertFalse(cfg.isEnabled(DeserializationFeature.USE_BIG_INTEGER_FOR_INTS));

        assertTrue(cfg.isEnabled(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES));
    }

// com.fasterxml.jackson.databind.deser.TestConfig::testOverrideIntrospectors
    public void testOverrideIntrospectors()
    {
        ObjectMapper m = new ObjectMapper();
        DeserializationConfig cfg = m.getDeserializationConfig();
        
        cfg = cfg.with((ClassIntrospector) null); 
        cfg = cfg.with((AnnotationIntrospector) null);
        assertNull(cfg.getAnnotationIntrospector());
    }

// com.fasterxml.jackson.databind.deser.TestConfig::testAnnotationsDisabled
    public void testAnnotationsDisabled() throws Exception
    {
        
        ObjectMapper m = new ObjectMapper();
        assertTrue(m.getDeserializationConfig().isEnabled(MapperFeature.USE_ANNOTATIONS));
        
        AnnoBean bean = m.readValue("{ \"y\" : 0 }", AnnoBean.class);
        assertEquals(0, bean.value);

        m = new ObjectMapper();
        m.configure(MapperFeature.USE_ANNOTATIONS, false);
        
        bean = m.readValue("{ \"x\" : 0 }", AnnoBean.class);
        assertEquals(0, bean.value);
    }

// com.fasterxml.jackson.databind.deser.TestConfig::testEnumsWhenDisabled
    public void testEnumsWhenDisabled() throws Exception
    {
        ObjectMapper m = new ObjectMapper();
        assertEquals(Alpha.B, m.readValue(quote("B"), Alpha.class));

        m = new ObjectMapper();
        m.configure(MapperFeature.USE_ANNOTATIONS, false);
        
        assertEquals(Alpha.B, m.readValue(quote("B"), Alpha.class));
    }

// com.fasterxml.jackson.databind.deser.TestConfig::testNoAccessOverrides
    public void testNoAccessOverrides() throws Exception
    {
        ObjectMapper m = new ObjectMapper();
        m.disable(MapperFeature.CAN_OVERRIDE_ACCESS_MODIFIERS);
        SimpleBean bean = m.readValue("{\"x\":1,\"y\":2}", SimpleBean.class);
        assertEquals(1, bean.x);
        assertEquals(2, bean.y);
    }

// com.fasterxml.jackson.databind.deser.TestCustomDeserializers::testCustomBeanDeserializer
    public void testCustomBeanDeserializer() throws Exception
    {
        String json = "{\"beans\":[{\"c\":{\"a\":10,\"b\":20},\"d\":\"hello, tatu\"}]}";
        TestBeans beans = MAPPER.readValue(json, TestBeans.class);

        assertNotNull(beans);
        List<TestBean> results = beans.beans;
        assertNotNull(results);
        assertEquals(1, results.size());
        TestBean bean = results.get(0);
        assertEquals("hello, tatu", bean.d);
        CustomBean c = bean.c;
        assertNotNull(c);
        assertEquals(10, c.a);
        assertEquals(20, c.b);

        json = "{\"beans\":[{\"c\":{\"b\":3,\"a\":-4},\"d\":\"\"},"
            +"{\"d\":\"abc\", \"c\":{\"b\":15}}]}";
        beans = MAPPER.readValue(json, TestBeans.class);

        assertNotNull(beans);
        results = beans.beans;
        assertNotNull(results);
        assertEquals(2, results.size());

        bean = results.get(0);
        assertEquals("", bean.d);
        c = bean.c;
        assertNotNull(c);
        assertEquals(-4, c.a);
        assertEquals(3, c.b);

        bean = results.get(1);
        assertEquals("abc", bean.d);
        c = bean.c;
        assertNotNull(c);
        assertEquals(0, c.a);
        assertEquals(15, c.b);
    }

// com.fasterxml.jackson.databind.deser.TestCustomDeserializers::testDelegating
    public void testDelegating() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        SimpleModule module = new SimpleModule("test", Version.unknownVersion());
        module.addDeserializer(Immutable.class,
            new StdDelegatingDeserializer<Immutable>(
                new StdConverter<JsonNode, Immutable>() {
                    @Override
                    public Immutable convert(JsonNode value)
                    {
                        int x = value.path("x").asInt();
                        int y = value.path("y").asInt();
                        return new Immutable(x, y);
                    }
                }
                ));

        mapper.registerModule(module);
        Immutable imm = mapper.readValue("{\"x\":3,\"y\":7}", Immutable.class);
        assertEquals(3, imm.x);
        assertEquals(7, imm.y);
    }

// com.fasterxml.jackson.databind.deser.TestCustomDeserializers::testIssue882
    public void testIssue882() throws Exception
    {
        Model original = new Model(Collections.singletonMap(new CustomKey(123), "test"));
        String json = MAPPER.writeValueAsString(original);
        Model deserialized = MAPPER.readValue(json, Model.class);
        assertNotNull(deserialized);
        assertNotNull(deserialized.map);
        assertEquals(1, deserialized.map.size());
    }

// com.fasterxml.jackson.databind.deser.TestCustomDeserializers::testContextReadValue
    public void testContextReadValue() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        SimpleModule module = new SimpleModule("test", Version.unknownVersion());
        module.addDeserializer(Bean375Outer.class, new Bean375OuterDeserializer());
        module.addDeserializer(Bean375Inner.class, new Bean375InnerDeserializer());
        mapper.registerModule(module);

        
        Bean375Outer outer = mapper.readValue("13", Bean375Outer.class);
        assertEquals(26, outer.inner.x);

        
        Bean375Wrapper w = mapper.readValue("{\"value\":13}", Bean375Wrapper.class);
        assertNotNull(w.value);
        assertNotNull(w.value.inner);
        assertEquals(-13, w.value.inner.x);
    }

// com.fasterxml.jackson.databind.deser.TestCustomFactory::testCustomBeanDeserializer
    public void testCustomBeanDeserializer() throws Exception
    {

        final ObjectMapper map = new ObjectMapper();
        String json = "{\"beans\":[{\"c\":{\"a\":10,\"b\":20},\"d\":\"hello, tatu\"}]}";
        TestBeans beans = map.readValue(json, TestBeans.class);

        assertNotNull(beans);
        List<TestBean> results = beans.beans;
        assertNotNull(results);
        assertEquals(1, results.size());
        TestBean bean = results.get(0);
        assertEquals("hello, tatu", bean.d);
        CustomBean c = bean.c;
        assertNotNull(c);
        assertEquals(10, c.a);
        assertEquals(20, c.b);

        json = "{\"beans\":[{\"c\":{\"b\":3,\"a\":-4},\"d\":\"\"},"
            +"{\"d\":\"abc\", \"c\":{\"b\":15}}]}";
        beans = map.readValue(json, TestBeans.class);

        assertNotNull(beans);
        results = beans.beans;
        assertNotNull(results);
        assertEquals(2, results.size());

        bean = results.get(0);
        assertEquals("", bean.d);
        c = bean.c;
        assertNotNull(c);
        assertEquals(-4, c.a);
        assertEquals(3, c.b);

        bean = results.get(1);
        assertEquals("abc", bean.d);
        c = bean.c;
        assertNotNull(c);
        assertEquals(0, c.a);
        assertEquals(15, c.b);
    }

// com.fasterxml.jackson.databind.deser.TestCyclicTypes::testLinked
    public void testLinked() throws Exception
    {
        Bean first = MAPPER.readValue
            ("{\"name\":\"first\", \"next\": { "
             +" \"name\":\"last\", \"next\" : null }}",
             Bean.class);

        assertNotNull(first);
        assertEquals("first", first._name);
        Bean last = first._next;
        assertNotNull(last);
        assertEquals("last", last._name);
        assertNull(last._next);
    }

// com.fasterxml.jackson.databind.deser.TestCyclicTypes::testLinkedGeneric
    public void testLinkedGeneric() throws Exception
    {
        StringLink link = MAPPER.readValue("{\"next\":null}", StringLink.class);
        assertNotNull(link);
        assertNull(link.next);
    }

// com.fasterxml.jackson.databind.deser.TestCyclicTypes::testCycleWith2Classes
    public void testCycleWith2Classes() throws Exception
    {
        LinkA a = MAPPER.readValue("{\"next\":{\"a\":null}}", LinkA.class);
        assertNotNull(a.next);
        LinkB b = a.next;
        assertNull(b.a);
    }

// com.fasterxml.jackson.databind.deser.TestCyclicTypes::testIgnoredCycle
    public void testIgnoredCycle() throws Exception
    {
        Selfie405 self1 = new Selfie405(1);
        self1.parent = self1;

        
        assertTrue(MAPPER.isEnabled(SerializationFeature.FAIL_ON_SELF_REFERENCES));
        try {
            MAPPER.writeValueAsString(self1);
            fail("Should fail with direct self-ref");
        } catch (JsonMappingException e) {
            verifyException(e, "Direct self-reference");
        }
        
        ObjectWriter w = MAPPER.writer()
                .without(SerializationFeature.FAIL_ON_SELF_REFERENCES);
        String json = w.writeValueAsString(self1);
        assertNotNull(json);
        assertEquals(aposToQuotes("{'id':1,'parent':{'id':1}}"), json);
    }

// com.fasterxml.jackson.databind.deser.TestDateDeserialization::testDateUtil
    public void testDateUtil() throws Exception
    {
        long now = 123456789L;
        java.util.Date value = new java.util.Date(now);

        
        assertEquals(value, MAPPER.readValue(""+now, java.util.Date.class));

        
        String dateStr = dateToString(value);
        java.util.Date result = MAPPER.readValue("\""+dateStr+"\"", java.util.Date.class);

        assertEquals("Date: expect "+value+" ("+value.getTime()+"), got "+result+" ("+result.getTime()+")",
                value.getTime(), result.getTime());
    }

// com.fasterxml.jackson.databind.deser.TestDateDeserialization::testDateUtilWithStringTimestamp
    public void testDateUtilWithStringTimestamp() throws Exception
    {
        long now = 1321992375446L;
        
        String json = quote(String.valueOf(now));
        java.util.Date value = MAPPER.readValue(json, java.util.Date.class);
        assertEquals(now, value.getTime());

        
        long before = - (24 * 3600 * 1000L);
        json = quote(String.valueOf(before));
        value = MAPPER.readValue(json, java.util.Date.class);
        assertEquals(before, value.getTime());
    }

// com.fasterxml.jackson.databind.deser.TestDateDeserialization::testDateUtilRFC1123
    public void testDateUtilRFC1123() throws Exception
    {
        DateFormat fmt = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz", Locale.US);
        
        String inputStr = "Sat, 17 Jan 2009 06:13:58 +0000";
        java.util.Date inputDate = fmt.parse(inputStr);
        assertEquals(inputDate, MAPPER.readValue("\""+inputStr+"\"", java.util.Date.class));
    }

// com.fasterxml.jackson.databind.deser.TestDateDeserialization::testDateUtilRFC1123OnNonUSLocales
    public void testDateUtilRFC1123OnNonUSLocales() throws Exception
    {
        Locale old = Locale.getDefault();
        Locale.setDefault(Locale.GERMAN);
        DateFormat fmt = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz", Locale.US);
        
        String inputStr = "Sat, 17 Jan 2009 06:13:58 +0000";
        java.util.Date inputDate = fmt.parse(inputStr);
        assertEquals(inputDate, MAPPER.readValue("\""+inputStr+"\"", java.util.Date.class));
        Locale.setDefault(old);
    }

// com.fasterxml.jackson.databind.deser.TestDateDeserialization::testDateUtilISO8601
    public void testDateUtilISO8601() throws Exception
    {
        
        String inputStr = "1972-12-28T00:00:00.000+0000";
        Date inputDate = MAPPER.readValue("\""+inputStr+"\"", java.util.Date.class);
        Calendar c = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
        c.setTime(inputDate);
        assertEquals(1972, c.get(Calendar.YEAR));
        assertEquals(Calendar.DECEMBER, c.get(Calendar.MONTH));
        assertEquals(28, c.get(Calendar.DAY_OF_MONTH));

        
        inputStr = "1972-12-28T00:00:00.000Z";
        inputDate = MAPPER.readValue(quote(inputStr), java.util.Date.class);
        c.setTime(inputDate);
        assertEquals(1972, c.get(Calendar.YEAR));
        assertEquals(Calendar.DECEMBER, c.get(Calendar.MONTH));
        assertEquals(28, c.get(Calendar.DAY_OF_MONTH));

        
        inputStr = "1972-12-28T00:00:00.000+00:00";
        inputDate = MAPPER.readValue(quote(inputStr), java.util.Date.class);
        c.setTime(inputDate);
        assertEquals(1972, c.get(Calendar.YEAR));
        assertEquals(Calendar.DECEMBER, c.get(Calendar.MONTH));
        assertEquals(28, c.get(Calendar.DAY_OF_MONTH));

        
        inputStr = "1972-12-28T00:00:00.000+00";
        inputDate = MAPPER.readValue(quote(inputStr), java.util.Date.class);
        c.setTime(inputDate);
        assertEquals(1972, c.get(Calendar.YEAR));
        assertEquals(Calendar.DECEMBER, c.get(Calendar.MONTH));
        assertEquals(28, c.get(Calendar.DAY_OF_MONTH));

        inputStr = "1984-11-30T00:00:00.000Z";
        inputDate = MAPPER.readValue(quote(inputStr), java.util.Date.class);
        c.setTime(inputDate);
        assertEquals(1984, c.get(Calendar.YEAR));
        assertEquals(Calendar.NOVEMBER, c.get(Calendar.MONTH));
        assertEquals(30, c.get(Calendar.DAY_OF_MONTH));
    }

// com.fasterxml.jackson.databind.deser.TestDateDeserialization::testISO8601PartialMilliseconds
    public void testISO8601PartialMilliseconds() throws Exception
    {
        String inputStr;
        Date inputDate;
        Calendar c = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
        
        inputStr = "2014-10-03T18:00:00.6-05:00";
        inputDate = MAPPER.readValue(quote(inputStr), java.util.Date.class);
        c.setTime(inputDate);
        assertEquals(2014, c.get(Calendar.YEAR));
        assertEquals(Calendar.OCTOBER, c.get(Calendar.MONTH));
        assertEquals(3, c.get(Calendar.DAY_OF_MONTH));
        assertEquals(600, c.get(Calendar.MILLISECOND));

        inputStr = "2014-10-03T18:00:00.61-05:00";
        inputDate = MAPPER.readValue(quote(inputStr), java.util.Date.class);
        c.setTime(inputDate);
        assertEquals(2014, c.get(Calendar.YEAR));
        assertEquals(Calendar.OCTOBER, c.get(Calendar.MONTH));
        assertEquals(3, c.get(Calendar.DAY_OF_MONTH));
        assertEquals(18 + 5, c.get(Calendar.HOUR_OF_DAY));
        assertEquals(0, c.get(Calendar.MINUTE));
        assertEquals(0, c.get(Calendar.SECOND));
        assertEquals(610, c.get(Calendar.MILLISECOND));

        inputStr = "1997-07-16T19:20:30.45+01:00";
        inputDate = MAPPER.readValue(quote(inputStr), java.util.Date.class);
        c.setTime(inputDate);
        assertEquals(1997, c.get(Calendar.YEAR));
        assertEquals(Calendar.JULY, c.get(Calendar.MONTH));
        assertEquals(16, c.get(Calendar.DAY_OF_MONTH));
        assertEquals(19 - 1, c.get(Calendar.HOUR_OF_DAY));
        assertEquals(20, c.get(Calendar.MINUTE));
        assertEquals(30, c.get(Calendar.SECOND));
        assertEquals(450, c.get(Calendar.MILLISECOND));
    }

// com.fasterxml.jackson.databind.deser.TestDateDeserialization::testISO8601MissingSeconds
    public void testISO8601MissingSeconds() throws Exception
    {
        String inputStr;
        Date inputDate;
        Calendar c = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
    
        inputStr = "1997-07-16T19:20+01:00";
        inputDate = MAPPER.readValue(quote(inputStr), java.util.Date.class);
        c.setTime(inputDate);
        assertEquals(1997, c.get(Calendar.YEAR));
        assertEquals(Calendar.JULY, c.get(Calendar.MONTH));
        assertEquals(16, c.get(Calendar.DAY_OF_MONTH));
        assertEquals(19 - 1, c.get(Calendar.HOUR_OF_DAY));
        assertEquals(0, c.get(Calendar.SECOND));
        assertEquals(0, c.get(Calendar.MILLISECOND));
}

// com.fasterxml.jackson.databind.deser.TestDateDeserialization::testDateUtilISO8601NoTimezone
    public void testDateUtilISO8601NoTimezone() throws Exception
    {
        
        String inputStr = "1984-11-13T00:00:09";
        Date inputDate = MAPPER.readValue(quote(inputStr), java.util.Date.class);
        Calendar c = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
        c.setTime(inputDate);
        assertEquals(1984, c.get(Calendar.YEAR));
        assertEquals(Calendar.NOVEMBER, c.get(Calendar.MONTH));
        assertEquals(13, c.get(Calendar.DAY_OF_MONTH));
        assertEquals(0, c.get(Calendar.HOUR_OF_DAY));
        assertEquals(0, c.get(Calendar.MINUTE));
        assertEquals(9, c.get(Calendar.SECOND));
        assertEquals(0, c.get(Calendar.MILLISECOND));
    }

// com.fasterxml.jackson.databind.deser.TestDateDeserialization::testDateUtilISO8601NoMilliseconds
    public void testDateUtilISO8601NoMilliseconds() throws Exception
    {
        final String INPUT_STR = "2013-10-31T17:27:00";
        Date inputDate;
        Calendar c;
        
        inputDate = MAPPER.readValue(quote(INPUT_STR), java.util.Date.class);
        c = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
        c.setTime(inputDate);
        assertEquals(2013, c.get(Calendar.YEAR));
        assertEquals(Calendar.OCTOBER, c.get(Calendar.MONTH));
        assertEquals(31, c.get(Calendar.DAY_OF_MONTH));
        assertEquals(17, c.get(Calendar.HOUR_OF_DAY));
        assertEquals(27, c.get(Calendar.MINUTE));
        assertEquals(0, c.get(Calendar.SECOND));
        assertEquals(0, c.get(Calendar.MILLISECOND));

        
        
        
    }

// com.fasterxml.jackson.databind.deser.TestDateDeserialization::testDateUtilISO8601JustDate
    public void testDateUtilISO8601JustDate() throws Exception
    {
        
        String inputStr = "1972-12-28";
        Date inputDate = MAPPER.readValue(quote(inputStr), java.util.Date.class);
        Calendar c = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
        c.setTime(inputDate);
        assertEquals(1972, c.get(Calendar.YEAR));
        assertEquals(Calendar.DECEMBER, c.get(Calendar.MONTH));
        assertEquals(28, c.get(Calendar.DAY_OF_MONTH));
    }

// com.fasterxml.jackson.databind.deser.TestDateDeserialization::testDateSql
    public void testDateSql() throws Exception
    {
        java.sql.Date value = new java.sql.Date(0L);
        value.setYear(99); 
        value.setDate(19);
        value.setMonth(Calendar.APRIL);
        long now = value.getTime();

        
        assertEquals(value, MAPPER.readValue(String.valueOf(now), java.sql.Date.class));

        
        
        java.sql.Date result = MAPPER.readValue(quote(value.toString()), java.sql.Date.class);
        Calendar c = gmtCalendar(result.getTime());
        assertEquals(1999, c.get(Calendar.YEAR));
        assertEquals(Calendar.APRIL, c.get(Calendar.MONTH));
        assertEquals(19, c.get(Calendar.DAY_OF_MONTH));

        
        String expStr = "1981-07-13";
        result = MAPPER.readValue(quote(expStr), java.sql.Date.class);
        c.setTimeInMillis(result.getTime());
        assertEquals(1981, c.get(Calendar.YEAR));
        assertEquals(Calendar.JULY, c.get(Calendar.MONTH));
        assertEquals(13, c.get(Calendar.DAY_OF_MONTH));

        

    }

// com.fasterxml.jackson.databind.deser.TestDateDeserialization::testCalendar
    public void testCalendar() throws Exception
    {
        
        java.util.Calendar value = Calendar.getInstance();
        long l = 12345678L;
        value.setTimeInMillis(l);

        
        Calendar result = MAPPER.readValue(""+l, Calendar.class);
        assertEquals(l, result.getTimeInMillis());

        
        String dateStr = dateToString(new Date(l));
        result = MAPPER.readValue(quote(dateStr), Calendar.class);

        
        assertEquals(l, result.getTimeInMillis());
    }

// com.fasterxml.jackson.databind.deser.TestDateDeserialization::testCustom
    public void testCustom() throws Exception
    {
        final ObjectMapper mapper = new ObjectMapper();
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd'X'HH:mm:ss");
        df.setTimeZone(TimeZone.getTimeZone("PST"));
        mapper.setDateFormat(df);

        String dateStr = "1972-12-28X15:45:00";
        java.util.Date exp = df.parse(dateStr);
        java.util.Date result = mapper.readValue("\""+dateStr+"\"", java.util.Date.class);
        assertEquals(exp, result);
    }

// com.fasterxml.jackson.databind.deser.TestDateDeserialization::testDatesWithEmptyStrings
    public void testDatesWithEmptyStrings() throws Exception
    {
        assertNull(MAPPER.readValue(quote(""), java.util.Date.class));
        assertNull(MAPPER.readValue(quote(""), java.util.Calendar.class));
        assertNull(MAPPER.readValue(quote(""), java.sql.Date.class));
    }

// com.fasterxml.jackson.databind.deser.TestDateDeserialization::test8601DateTimeNoMilliSecs
    public void test8601DateTimeNoMilliSecs() throws Exception
    {
        
        for (String inputStr : new String[] {
               "2010-06-28T23:34:22Z",
               "2010-06-28T23:34:22+0000",
               "2010-06-28T23:34:22+00",
        }) {
            Date inputDate = MAPPER.readValue(quote(inputStr), java.util.Date.class);
            Calendar c = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
            c.setTime(inputDate);
            assertEquals(2010, c.get(Calendar.YEAR));
            assertEquals(Calendar.JUNE, c.get(Calendar.MONTH));
            assertEquals(28, c.get(Calendar.DAY_OF_MONTH));
            assertEquals(23, c.get(Calendar.HOUR_OF_DAY));
            assertEquals(34, c.get(Calendar.MINUTE));
            assertEquals(22, c.get(Calendar.SECOND));
            assertEquals(0, c.get(Calendar.MILLISECOND));
        }
    }

// com.fasterxml.jackson.databind.deser.TestDateDeserialization::testTimeZone
    public void testTimeZone() throws Exception
    {
        TimeZone result = MAPPER.readValue(quote("PST"), TimeZone.class);
        assertEquals("PST", result.getID());
    }

// com.fasterxml.jackson.databind.deser.TestDateDeserialization::testCustomDateWithAnnotation
    public void testCustomDateWithAnnotation() throws Exception
    {
        final String INPUT = "{\"date\":\"/2005/05/25/\"}";
        DateAsStringBean result = MAPPER.readValue(INPUT, DateAsStringBean.class);
        assertNotNull(result);
        assertNotNull(result.date);
        Calendar c = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
        long l = result.date.getTime();
        if (l == 0L) {
            fail("Should not get null date");
        }
        c.setTimeInMillis(l);
        assertEquals(2005, c.get(Calendar.YEAR));
        assertEquals(Calendar.MAY, c.get(Calendar.MONTH));
        assertEquals(25, c.get(Calendar.DAY_OF_MONTH));

        
        
        result = MAPPER.reader(DateAsStringBean.class)
                .with(Locale.GERMANY)
                .readValue(INPUT);
        assertNotNull(result);
        assertNotNull(result.date);
        l = result.date.getTime();
        if (l == 0L) {
            fail("Should not get null date");
        }
        c.setTimeInMillis(l);
        assertEquals(2005, c.get(Calendar.YEAR));
        assertEquals(Calendar.MAY, c.get(Calendar.MONTH));
        assertEquals(25, c.get(Calendar.DAY_OF_MONTH));

        
        DateAsStringBeanGermany result2 = MAPPER.reader(DateAsStringBeanGermany.class).readValue(INPUT);
        assertNotNull(result2);
        assertNotNull(result2.date);
        l = result2.date.getTime();
        if (l == 0L) {
            fail("Should not get null date");
        }
        c.setTimeInMillis(l);
        assertEquals(2005, c.get(Calendar.YEAR));
        assertEquals(Calendar.MAY, c.get(Calendar.MONTH));
        assertEquals(25, c.get(Calendar.DAY_OF_MONTH));
    }

// com.fasterxml.jackson.databind.deser.TestDateDeserialization::testCustomCalendarWithAnnotation
    public void testCustomCalendarWithAnnotation() throws Exception
    {
        CalendarAsStringBean cbean = MAPPER.readValue("{\"cal\":\";2007/07/13;\"}",
                CalendarAsStringBean.class);
        assertNotNull(cbean);
        assertNotNull(cbean.cal);
        Calendar c = cbean.cal;
        assertEquals(2007, c.get(Calendar.YEAR));
        assertEquals(Calendar.JULY, c.get(Calendar.MONTH));
        assertEquals(13, c.get(Calendar.DAY_OF_MONTH));
    }

// com.fasterxml.jackson.databind.deser.TestDateDeserialization::testCustomCalendarWithTimeZone
    public void testCustomCalendarWithTimeZone() throws Exception
    {
        
        DateInCETBean cet = MAPPER.readValue("{\"date\":\"2001-01-01,10\"}",
                DateInCETBean.class);
        Calendar c = Calendar.getInstance(getUTCTimeZone());
        c.setTimeInMillis(cet.date.getTime());
        
        assertEquals(2001, c.get(Calendar.YEAR));
        assertEquals(Calendar.JANUARY, c.get(Calendar.MONTH));
        assertEquals(1, c.get(Calendar.DAY_OF_MONTH));
        assertEquals(9, c.get(Calendar.HOUR_OF_DAY));
    }

// com.fasterxml.jackson.databind.deser.TestDateDeserialization::testInvalidFormat
    public void testInvalidFormat() throws Exception
    {
        try {
            MAPPER.readValue(quote("foobar"), Date.class);
            fail("Should have failed with an exception");
        } catch (InvalidFormatException e) {
            verifyException(e, "Can not construct instance");
            assertEquals("foobar", e.getValue());
            assertEquals(Date.class, e.getTargetType());
        } catch (Exception e) {
            fail("Wrong type of exception ("+e.getClass().getName()+"), should get "
                    +InvalidFormatException.class.getName());
        }
    }

// com.fasterxml.jackson.databind.deser.TestEnumDeserialization::testSimple
    public void testSimple() throws Exception
    {
        
        String JSON = "\"OK\" \"RULES\"  null";
        
        JsonParser jp = MAPPER.getFactory().createParser(JSON);

        assertEquals(TestEnum.OK, MAPPER.readValue(jp, TestEnum.class));
        assertEquals(TestEnum.RULES, MAPPER.readValue(jp, TestEnum.class));

        
        assertNull(MAPPER.readValue(jp, TestEnum.class));

        
        assertFalse(jp.hasCurrentToken());

        
        assertEquals(TestEnum.JACKSON, MAPPER.readValue(" 0 ", TestEnum.class));

        
        try {
             MAPPER.readValue("\"NO-SUCH-VALUE\"", TestEnum.class);
            fail("Expected an exception for bogus enum value...");
        } catch (JsonMappingException jex) {
            verifyException(jex, "value not one of declared");
        }
        jp.close();
    }

// com.fasterxml.jackson.databind.deser.TestEnumDeserialization::testComplexEnum
    public void testComplexEnum() throws Exception
    {
        String json = MAPPER.writeValueAsString(TimeUnit.SECONDS);
        assertEquals(quote("SECONDS"), json);
        TimeUnit result = MAPPER.readValue(json, TimeUnit.class);
        assertSame(TimeUnit.SECONDS, result);
    }

// com.fasterxml.jackson.databind.deser.TestEnumDeserialization::testAnnotated
    public void testAnnotated() throws Exception
    {
        AnnotatedTestEnum e = MAPPER.readValue("\"JACKSON\"", AnnotatedTestEnum.class);
        
        assertEquals(AnnotatedTestEnum.OK, e);
    }

// com.fasterxml.jackson.databind.deser.TestEnumDeserialization::testEnumMaps
    public void testEnumMaps() throws Exception
    {
        EnumMap<TestEnum,String> value = MAPPER.readValue("{\"OK\":\"value\"}",
                new TypeReference<EnumMap<TestEnum,String>>() { });
        assertEquals("value", value.get(TestEnum.OK));
    }

// com.fasterxml.jackson.databind.deser.TestEnumDeserialization::testSubclassedEnums
    public void testSubclassedEnums() throws Exception
    {
        EnumWithSubClass value = MAPPER.readValue("\"A\"", EnumWithSubClass.class);
        assertEquals(EnumWithSubClass.A, value);
    }

// com.fasterxml.jackson.databind.deser.TestEnumDeserialization::testCreatorEnums
    public void testCreatorEnums() throws Exception
    {
        EnumWithCreator value = MAPPER.readValue("\"enumA\"", EnumWithCreator.class);
        assertEquals(EnumWithCreator.A, value);
    }

// com.fasterxml.jackson.databind.deser.TestEnumDeserialization::testToStringEnums
    public void testToStringEnums() throws Exception
    {
        
        ObjectMapper m = new ObjectMapper();
        m.configure(DeserializationFeature.READ_ENUMS_USING_TO_STRING, true);
        LowerCaseEnum value = m.readValue("\"c\"", LowerCaseEnum.class);
        assertEquals(LowerCaseEnum.C, value);
    }

// com.fasterxml.jackson.databind.deser.TestEnumDeserialization::testToStringEnumMaps
    public void testToStringEnumMaps() throws Exception
    {
        
        ObjectMapper m = new ObjectMapper();
        m.configure(DeserializationFeature.READ_ENUMS_USING_TO_STRING, true);
        EnumMap<LowerCaseEnum,String> value = m.readValue("{\"a\":\"value\"}",
                new TypeReference<EnumMap<LowerCaseEnum,String>>() { });
        assertEquals("value", value.get(LowerCaseEnum.A));
    }

// com.fasterxml.jackson.databind.deser.TestEnumDeserialization::testNumbersToEnums
    public void testNumbersToEnums() throws Exception
    {
        
        assertFalse(MAPPER.getDeserializationConfig().isEnabled(DeserializationFeature.FAIL_ON_NUMBERS_FOR_ENUMS));
        TestEnum value = MAPPER.readValue("1", TestEnum.class);
        assertSame(TestEnum.RULES, value);

        
        ObjectMapper m = new ObjectMapper();
        m.configure(DeserializationFeature.FAIL_ON_NUMBERS_FOR_ENUMS, true);
        try {
            value = m.readValue("1", TestEnum.class);
            fail("Expected an error");
        } catch (JsonMappingException e) {
            verifyException(e, "Not allowed to deserialize Enum value out of JSON number");
        }
    }

// com.fasterxml.jackson.databind.deser.TestEnumDeserialization::testEnumsWithIndex
    public void testEnumsWithIndex() throws Exception
    {
        ObjectMapper m = new ObjectMapper();
        m.enable(SerializationFeature.WRITE_ENUMS_USING_INDEX);
        String json = m.writeValueAsString(TestEnum.RULES);
        assertEquals(String.valueOf(TestEnum.RULES.ordinal()), json);
        TestEnum result = m.readValue(json, TestEnum.class);
        assertSame(TestEnum.RULES, result);
    }

// com.fasterxml.jackson.databind.deser.TestEnumDeserialization::testEnumsWithJsonValue
    public void testEnumsWithJsonValue() throws Exception
    {
        
        EnumWithJsonValue e = MAPPER.readValue(quote("foo"), EnumWithJsonValue.class);
        assertSame(EnumWithJsonValue.A, e);
        e = MAPPER.readValue(quote("bar"), EnumWithJsonValue.class);
        assertSame(EnumWithJsonValue.B, e);

        
        EnumSet<EnumWithJsonValue> set = MAPPER.readValue("[\"bar\"]",
                new TypeReference<EnumSet<EnumWithJsonValue>>() { });
        assertNotNull(set);
        assertEquals(1, set.size());
        assertTrue(set.contains(EnumWithJsonValue.B));
        assertFalse(set.contains(EnumWithJsonValue.A));

        
        EnumMap<EnumWithJsonValue,Integer> map = MAPPER.readValue("{\"foo\":13}",
                new TypeReference<EnumMap<EnumWithJsonValue, Integer>>() { });
        assertNotNull(map);
        assertEquals(1, map.size());
        assertEquals(Integer.valueOf(13), map.get(EnumWithJsonValue.A));
    }

// com.fasterxml.jackson.databind.deser.TestEnumDeserialization::testEnumWithCreatorEnumMaps
    public void testEnumWithCreatorEnumMaps() throws Exception {
          EnumMap<EnumWithCreator,String> value = MAPPER.readValue("{\"enumA\":\"value\"}",
                  new TypeReference<EnumMap<EnumWithCreator,String>>() {});
          assertEquals("value", value.get(EnumWithCreator.A));
    }

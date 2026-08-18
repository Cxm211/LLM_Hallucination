// buggy code
    public void serializeAsColumn(Object bean, JsonGenerator jgen, SerializerProvider prov)
        throws Exception
    {
        Object value = get(bean);
        if (value == null) { // nulls need specialized handling
            if (_nullSerializer != null) {
                _nullSerializer.serialize(null, jgen, prov);
            } else { // can NOT suppress entries in tabular output
                jgen.writeNull();
            }
        }
        // otherwise find serializer to use
        JsonSerializer<Object> ser = _serializer;
        if (ser == null) {
            Class<?> cls = value.getClass();
            PropertySerializerMap map = _dynamicSerializers;
            ser = map.serializerFor(cls);
            if (ser == null) {
                ser = _findAndAddDynamic(map, cls, prov);
            }
        }
        // and then see if we must suppress certain values (default, empty)
        if (_suppressableValue != null) {
            if (MARKER_FOR_EMPTY == _suppressableValue) {
                if (ser.isEmpty(value)) { // can NOT suppress entries in tabular output
                    serializeAsPlaceholder(bean, jgen, prov);
                    return;
                }
            } else if (_suppressableValue.equals(value)) { // can NOT suppress entries in tabular output
                serializeAsPlaceholder(bean, jgen, prov);
                return;
            }
        }
        // For non-nulls: simple check for direct cycles
        if (value == bean) {
            _handleSelfReference(bean, ser);
        }
        if (_typeSerializer == null) {
            ser.serialize(value, jgen, prov);
        } else {
            ser.serializeWithType(value, jgen, prov, _typeSerializer);
        }
    }

// relevant test
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
        ObjectMapper mapper = new ObjectMapper();
        byte[] base = jdkSerialize(mapper.getDeserializationConfig().getBaseSettings());
        assertNotNull(jdkDeserialize(base));

        
        
        DeserializationConfig origDC = mapper.getDeserializationConfig();
        SerializationConfig origSC = mapper.getSerializationConfig();
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
        ObjectWriter origWriter = new ObjectMapper().writer();
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
        ObjectReader origReader = new ObjectMapper().reader(MyPojo.class);
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
        ObjectMapper mapper = new ObjectMapper();
        final String EXP_JSON = "{\"x\":2,\"y\":3}";
        final MyPojo p = new MyPojo(2, 3);
        assertEquals(EXP_JSON, mapper.writeValueAsString(p));

        byte[] bytes = jdkSerialize(mapper);
        ObjectMapper mapper2 = jdkDeserialize(bytes);
        assertEquals(EXP_JSON, mapper2.writeValueAsString(p));
        MyPojo p2 = mapper2.readValue(EXP_JSON, MyPojo.class);
        assertEquals(p.x, p2.x);
        assertEquals(p.y, p2.y);
    }

// com.fasterxml.jackson.databind.TestNamingStrategy::testSimpleGetters
    public void testSimpleGetters() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setPropertyNamingStrategy(new PrefixStrategy());
        assertEquals("{\"Get-key\":123}", mapper.writeValueAsString(new GetterBean()));
    }

// com.fasterxml.jackson.databind.TestNamingStrategy::testSimpleSetters
    public void testSimpleSetters() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setPropertyNamingStrategy(new PrefixStrategy());
        SetterBean bean = mapper.readValue("{\"Set-key\":13}", SetterBean.class);
        assertEquals(13, bean.value);
    }

// com.fasterxml.jackson.databind.TestNamingStrategy::testSimpleFields
    public void testSimpleFields() throws Exception
    {
        
        ObjectMapper mapper = new ObjectMapper();
        mapper.setPropertyNamingStrategy(new PrefixStrategy());
        String json = mapper.writeValueAsString(new FieldBean(999));
        assertEquals("{\"Field-key\":999}", json);

        
        FieldBean result = mapper.readValue(json, FieldBean.class);
        assertEquals(999, result.key);
    }

// com.fasterxml.jackson.databind.TestNamingStrategy::testCStyleNaming
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

// com.fasterxml.jackson.databind.TestNamingStrategy::testWithGetterAsSetter
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

// com.fasterxml.jackson.databind.TestNamingStrategy::testJson
    public void testJson() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setPropertyNamingStrategy(new LcStrategy());

        RenamedCollectionBean foo = mapper.readValue("{\"thevalues\":[\"a\"]}", RenamedCollectionBean.class);
        assertNotNull(foo.getTheVALUEs());
        assertEquals(1, foo.getTheVALUEs().size());
        assertEquals("a", foo.getTheVALUEs().get(0));
    }

// com.fasterxml.jackson.databind.TestNamingStrategy::testPerClassAnnotation
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
    }

// com.fasterxml.jackson.databind.TestObjectMapperBeanDeserializer::testFromLongCtor
    public void testFromLongCtor() throws Exception
    {
        
        long value = 12345678901244L;
        CtorValueBean result = MAPPER.readValue(""+value, CtorValueBean.class);
        assertEquals(""+value, result.toString());
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
                assertEquals(FixtureObject.VALUE_URSTR, getAndVerifyText(jp));
            } else if (name.equals("url")) {
                assertToken(JsonToken.VALUE_STRING, t);
                assertEquals(FixtureObject.VALUE_URSTR, getAndVerifyText(jp));
            } else if (name.equals("testNull")) {
                assertToken(JsonToken.VALUE_NULL, t);
            } else if (name.equals("testString")) {
                assertToken(JsonToken.VALUE_STRING, t);
                assertEquals(FixtureObject.VALUE_STRING, getAndVerifyText(jp));
            } else if (name.equals("testBoolean")) {
                assertToken(JsonToken.VALUE_TRUE, t);
            } else if (name.equals("testEnum")) {
                assertToken(JsonToken.VALUE_STRING, t);
                assertEquals(FixtureObject.VALUE_ENUM.toString(),getAndVerifyText(jp));
            } else if (name.equals("testInteger")) {
                assertToken(JsonToken.VALUE_NUMBER_INT, t);
                assertEquals(jp.getIntValue(),FixtureObject.VALUE_INT);
            } else if (name.equals("testLong")) {
                assertToken(JsonToken.VALUE_NUMBER_INT, t);
                assertEquals(jp.getLongValue(),FixtureObject.VALUE_LONG);
            } else if (name.equals("testBigInteger")) {
                assertToken(JsonToken.VALUE_NUMBER_INT, t);
                assertEquals(jp.getLongValue(),FixtureObject.VALUE_BIGINT.longValue());
            } else if (name.equals("testBigDecimal")) {
                assertToken(JsonToken.VALUE_NUMBER_FLOAT, t);
                assertEquals(jp.getText(), FixtureObject.VALUE_BIGDEC.toString());
            } else if (name.equals("testCharacter")) {
                assertToken(JsonToken.VALUE_STRING, t);
                assertEquals(String.valueOf(FixtureObject.VALUE_CHAR), getAndVerifyText(jp));
            } else if (name.equals("testShort")) {
                assertToken(JsonToken.VALUE_NUMBER_INT, t);
                assertEquals(jp.getIntValue(),FixtureObject.VALUE_SHORT);
            } else if (name.equals("testByte")) {
                assertToken(JsonToken.VALUE_NUMBER_INT, t);
                assertEquals(jp.getIntValue(),FixtureObject.VALUE_BYTE);
            } else if (name.equals("testFloat")) {
                assertToken(JsonToken.VALUE_NUMBER_FLOAT, t);
                assertEquals(jp.getDecimalValue().floatValue(),FixtureObject.VALUE_FLOAT);
            } else if (name.equals("testDouble")) {
                assertToken(JsonToken.VALUE_NUMBER_FLOAT, t);
                assertEquals(jp.getDoubleValue(),FixtureObject.VALUE_DBL);
            } else if (name.equals("testStringBuffer")) {
                assertToken(JsonToken.VALUE_STRING, t);
                assertEquals(FixtureObject.VALUE_STRING, getAndVerifyText(jp));
            } else if (name.equals("testError")) {
                
                assertToken(JsonToken.START_OBJECT, t);

                
                
                while (jp.nextToken() == JsonToken.FIELD_NAME) {
                    name = jp.getCurrentName();
                    if (name.equals("cause")) {
                        assertEquals(JsonToken.VALUE_NULL, jp.nextToken());
                    } else if (name.equals("message")) {
                        assertEquals(JsonToken.VALUE_STRING, jp.nextToken());
                        assertEquals(FixtureObject.VALUE_ERRTXT, getAndVerifyText(jp));
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

// com.fasterxml.jackson.databind.TestStdNamingStrategies::testLowerCaseStrategyStandAlone
    public void testLowerCaseStrategyStandAlone()
    {
        for (Object[] pair : NAME_TRANSLATIONS) {
            String translatedJavaName = PropertyNamingStrategy.CAMEL_CASE_TO_LOWER_CASE_WITH_UNDERSCORES.nameForField(null, null,
                    (String) pair[0]);
            assertEquals((String) pair[1], translatedJavaName);
        }
    }

// com.fasterxml.jackson.databind.TestStdNamingStrategies::testLowerCaseTranslations
    public void testLowerCaseTranslations() throws Exception
    {
        
        String json = mapper.writeValueAsString(new PersonBean("Joe", "Sixpack", 42));
        assertEquals("{\"first_name\":\"Joe\",\"last_name\":\"Sixpack\",\"age\":42}", json);
        
        
        PersonBean result = mapper.readValue(json, PersonBean.class);
        assertEquals("Joe", result.firstName);
        assertEquals("Sixpack", result.lastName);
        assertEquals(42, result.age);
    }

// com.fasterxml.jackson.databind.TestStdNamingStrategies::testLowerCaseAcronymsTranslations
    public void testLowerCaseAcronymsTranslations() throws Exception
    {
        
        String json = mapper.writeValueAsString(new Acronyms("world wide web", "http://jackson.codehaus.org", "/path1/,/path2/"));
        assertEquals("{\"www\":\"world wide web\",\"some_url\":\"http://jackson.codehaus.org\",\"some_uris\":\"/path1/,/path2/\"}", json);
        
        
        Acronyms result = mapper.readValue(json, Acronyms.class);
        assertEquals("world wide web", result.WWW);
        assertEquals("http://jackson.codehaus.org", result.someURL);
        assertEquals("/path1/,/path2/", result.someURIs);
    }

// com.fasterxml.jackson.databind.TestStdNamingStrategies::testLowerCaseOtherNonStandardNamesTranslations
    public void testLowerCaseOtherNonStandardNamesTranslations() throws Exception
    {
        
        String json = mapper.writeValueAsString(new OtherNonStandardNames("Results", "_User", "___", "$User"));
        assertEquals("{\"results\":\"Results\",\"user\":\"_User\",\"__\":\"___\",\"$_user\":\"$User\"}", json);
        
        
        OtherNonStandardNames result = mapper.readValue(json, OtherNonStandardNames.class);
        assertEquals("Results", result.Results);
        assertEquals("_User", result._User);
        assertEquals("___", result.___);
        assertEquals("$User", result.$User);
    }

// com.fasterxml.jackson.databind.TestStdNamingStrategies::testLowerCaseUnchangedNames
    public void testLowerCaseUnchangedNames() throws Exception
    {
        
        String json = mapper.writeValueAsString(new UnchangedNames("from_user", "_user", "from$user", "from7user", "_"));
        assertEquals("{\"from_user\":\"from_user\",\"user\":\"_user\",\"from$user\":\"from$user\",\"from7user\":\"from7user\",\"_\":\"_\"}", json);
        
        
        UnchangedNames result = mapper.readValue(json, UnchangedNames.class);
        assertEquals("from_user", result.from_user);
        assertEquals("_user", result._user);
        assertEquals("from$user", result.from$user);
        assertEquals("from7user", result.from7user);
        assertEquals("_", result._);
    }

// com.fasterxml.jackson.databind.TestStdNamingStrategies::testPascalCaseStandAlone
    public void testPascalCaseStandAlone()
    {
    	String translatedJavaName = PropertyNamingStrategy.PASCAL_CASE_TO_CAMEL_CASE.nameForField
    	        (null, null, "userName");
        assertEquals("UserName", translatedJavaName);

        translatedJavaName = PropertyNamingStrategy.PASCAL_CASE_TO_CAMEL_CASE.nameForField
                (null, null, "User");
        assertEquals("User", translatedJavaName);

        translatedJavaName = PropertyNamingStrategy.PASCAL_CASE_TO_CAMEL_CASE.nameForField
                (null, null, "user");
        assertEquals("User", translatedJavaName);
        translatedJavaName = PropertyNamingStrategy.PASCAL_CASE_TO_CAMEL_CASE.nameForField
                (null, null, "x");
        assertEquals("X", translatedJavaName);
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
        } catch (JsonProcessingException e) {
            verifyException(e, "boolProp");
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

// com.fasterxml.jackson.databind.convert.TestMapConversions::testMapToMap
    public void testMapToMap()
    {
        Map<String,Integer> input = new LinkedHashMap<String,Integer>();
        input.put("A", Integer.valueOf(3));
        input.put("B", Integer.valueOf(-4));
        Map<AB,String> output = mapper.convertValue(input,
                new TypeReference<Map<AB,String>>() { });
        assertEquals(2, output.size());
        assertEquals("3", output.get(AB.A));
        assertEquals("-4", output.get(AB.B));

        
        Map<String,Integer> roundtrip = mapper.convertValue(input,
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
        Bean bean = mapper.convertValue(map, Bean.class);
        assertEquals(Integer.valueOf(17), bean.A);
        assertEquals(" -1", bean.B);
    }

// com.fasterxml.jackson.databind.convert.TestMapConversions::testBeanToMap
    public void testBeanToMap()
    {
        Bean bean = new Bean();
        bean.A = 129;
        bean.B = "13";
        EnumMap<AB,String> result = mapper.convertValue(bean,
                new TypeReference<EnumMap<AB,String>>() { });
        assertEquals("129", result.get(AB.A));
        assertEquals("13", result.get(AB.B));
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

// com.fasterxml.jackson.databind.deser.TestAnyProperties::testSimpleMapImitation
    public void testSimpleMapImitation() throws Exception
    {
        MapImitator mapHolder = MAPPER.readValue
            ("{ \"a\" : 3, \"b\" : true }", MapImitator.class);
        Map<String,Object> result = mapHolder._map;
        assertEquals(2, result.size());
        assertEquals(Integer.valueOf(3), result.get("a"));
        assertEquals(Boolean.TRUE, result.get("b"));
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

// com.fasterxml.jackson.databind.deser.TestAnyProperties::testIgnored383
    public void testIgnored383() throws Exception
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
        JsonGenerator jg = new JsonFactory().createGenerator(sw);
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

// com.fasterxml.jackson.databind.deser.TestExceptionDeserialization::testIOException
    public void testIOException() throws IOException
    {
        IOException ioe = new IOException("TEST");
        String json = MAPPER.writeValueAsString(ioe);
        IOException result = MAPPER.readValue(json, IOException.class);
        assertEquals(ioe.getMessage(), result.getMessage());
    }

// com.fasterxml.jackson.databind.deser.TestExceptionDeserialization::testWithCreator
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

// com.fasterxml.jackson.databind.deser.TestExceptionDeserialization::testWithNullMessage
    public void testWithNullMessage() throws IOException
    {
        final ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        String json = mapper.writeValueAsString(new IOException((String) null));
        IOException result = mapper.readValue(json, IOException.class);
        assertNotNull(result);
        assertNull(result.getMessage());
    }

// com.fasterxml.jackson.databind.deser.TestExceptionDeserialization::testNoArgsException
    public void testNoArgsException() throws IOException
    {
        MyNoArgException exc = MAPPER.readValue("{}", MyNoArgException.class);
        assertNotNull(exc);
    }

// com.fasterxml.jackson.databind.deser.TestExceptionDeserialization::testJDK7SuppressionProperty
    public void testJDK7SuppressionProperty() throws IOException
    {
        Exception exc = MAPPER.readValue("{\"suppressed\":[]}", IOException.class);
        assertNotNull(exc);
    }

// com.fasterxml.jackson.databind.deser.TestFieldDeserialization::testSimpleAutoDetect
    public void testSimpleAutoDetect() throws Exception
    {
        ObjectMapper m = new ObjectMapper();
        SimpleFieldBean result = m.readValue("{ \"x\" : -13 }",
                                           SimpleFieldBean.class);
        assertEquals(-13, result.x);
        assertEquals(0, result.y);
    }

// com.fasterxml.jackson.databind.deser.TestFieldDeserialization::testSimpleAnnotation
    public void testSimpleAnnotation() throws Exception
    {
        ObjectMapper m = new ObjectMapper();
        SimpleFieldBean2 bean = m.readValue("{ \"values\" : [ \"x\", \"y\" ] }",
                SimpleFieldBean2.class);
        String[] values = bean.values;
        assertNotNull(values);
        assertEquals(2, values.length);
        assertEquals("x", values[0]);
        assertEquals("y", values[1]);
    }

// com.fasterxml.jackson.databind.deser.TestFieldDeserialization::testNoAutoDetect
    public void testNoAutoDetect() throws Exception
    {
        ObjectMapper m = new ObjectMapper();
        NoAutoDetectBean bean = m.readValue("{ \"z\" : 7 }",
                                            NoAutoDetectBean.class);
        assertEquals(7, bean._z);
    }

// com.fasterxml.jackson.databind.deser.TestFieldDeserialization::testTypeAnnotation
    public void testTypeAnnotation() throws Exception
    {
        ObjectMapper m = new ObjectMapper();
        AbstractWrapper w = m.readValue("{ \"value\" : \"abc\" }",
                                        AbstractWrapper.class);
        Abstract bean = w.value;
        assertNotNull(bean);
        assertEquals(Concrete.class, bean.getClass());
        assertEquals("abc", ((Concrete)bean).value);
    }

// com.fasterxml.jackson.databind.deser.TestFieldDeserialization::testFailureDueToDups
    public void testFailureDueToDups() throws Exception
    {
        try {
            writeAndMap(new ObjectMapper(), new DupFieldBean());
        } catch (JsonMappingException e) {
            verifyException(e, "Multiple fields representing property");
        }
    }

// com.fasterxml.jackson.databind.deser.TestFieldDeserialization::testFailureDueToDups2
    public void testFailureDueToDups2() throws Exception
    {
        try {
            writeAndMap(new ObjectMapper(), new DupFieldBean2());
        } catch (JsonMappingException e) {
            verifyException(e, "Multiple fields representing property");
        }
    }

// com.fasterxml.jackson.databind.deser.TestFieldDeserialization::testOkFieldOverride
    public void testOkFieldOverride() throws Exception
    {
        ObjectMapper m = new ObjectMapper();
        OkDupFieldBean result = m.readValue("{ \"x\" : 1, \"y\" : 2 }",
                OkDupFieldBean.class);
        assertEquals(1, result.myX);
        assertEquals(2, result.y);
    }

// com.fasterxml.jackson.databind.deser.TestGenericsBounded::testLowerBound
    public void testLowerBound() throws Exception
    {
        IntBeanWrapper<?> result = new ObjectMapper().readValue("{\"wrapped\":{\"x\":3}}",
                IntBeanWrapper.class);
        assertNotNull(result);
        assertEquals(IntBean.class, result.wrapped.getClass());
        assertEquals(3, result.wrapped.x);
    }

// com.fasterxml.jackson.databind.deser.TestGenericsBounded::testBounded
    public void testBounded() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        BoundedWrapper<IntBean> result = mapper.readValue
            ("{\"values\":[ {\"x\":3} ] } ", new TypeReference<BoundedWrapper<IntBean>>() {});
        List<?> list = result.values;
        assertEquals(1, list.size());
        Object ob = list.get(0);
        assertEquals(IntBean.class, ob.getClass());
        assertEquals(3, result.values.get(0).x);
    }

// com.fasterxml.jackson.databind.deser.TestGenericsBounded::testGenericsComplex
    public void testGenericsComplex() throws Exception
    {
        ObjectMapper m = new ObjectMapper();
        DoubleRange in = new DoubleRange(-0.5, 0.5);
        String json = m.writeValueAsString(in);
        DoubleRange out = m.readValue(json, DoubleRange.class);
        assertNotNull(out);
        assertEquals(-0.5, out.start);
        assertEquals(0.5, out.end);
    }

// com.fasterxml.jackson.databind.deser.TestGenericsBounded::testIssue778
    public void testIssue778() throws Exception
    {
        final ObjectMapper mapper = new ObjectMapper();
        String json = "{\"rows\":[{\"d\":{}}]}";

        final TypeReference<?> type = new TypeReference<ResultSetWithDoc<MyDoc>>() {};
        
        
        ResultSetWithDoc<MyDoc> rs = mapper.readValue(json, type);
        Document d = rs.rows.iterator().next().d;
    
        assertEquals(MyDoc.class, d.getClass()); 
    }

// com.fasterxml.jackson.databind.deser.TestInnerClass::testSimpleNonStaticInner
    public void testSimpleNonStaticInner() throws Exception
    {
        
        ObjectMapper mapper = new ObjectMapper();
        Dog input = new Dog("Smurf", true);
        String json = mapper.writeValueAsString(input);
        Dog output = mapper.readValue(json, Dog.class);
        assertEquals("Smurf", output.name);
        assertNotNull(output.brain);
        assertTrue(output.brain.isThinking);
        
        assertEquals("Smurf", output.brain.parentName());
        output.name = "Foo";
        assertEquals("Foo", output.brain.parentName());
    }

// com.fasterxml.jackson.databind.deser.TestJacksonTypes::testJsonLocation
    public void testJsonLocation() throws Exception
    {
        ObjectMapper m = new ObjectMapper();
        
        JsonLocation loc = new JsonLocation("whatever",  -1, -1, 100, 13);
        
        String ser = serializeAsString(m, loc);
        JsonLocation result = m.readValue(ser, JsonLocation.class);
        assertNotNull(result);
        assertEquals(loc.getSourceRef(), result.getSourceRef());
        assertEquals(loc.getByteOffset(), result.getByteOffset());
        assertEquals(loc.getCharOffset(), result.getCharOffset());
        assertEquals(loc.getColumnNr(), result.getColumnNr());
        assertEquals(loc.getLineNr(), result.getLineNr());
    }

// com.fasterxml.jackson.databind.deser.TestJacksonTypes::testJsonLocationProps
    public void testJsonLocationProps()
    {
        JsonLocation loc = new JsonLocation(null,  -1, -1, 100, 13);
        assertTrue(loc.equals(loc));
        assertFalse(loc.equals(null));
        assertFalse(loc.equals("abx"));

        
        loc.hashCode();
    }

// com.fasterxml.jackson.databind.deser.TestJacksonTypes::testTokenBufferWithSample
    public void testTokenBufferWithSample() throws Exception
    {
        ObjectMapper m = new ObjectMapper();
        
        TokenBuffer result = m.readValue(SAMPLE_DOC_JSON_SPEC, TokenBuffer.class);
        verifyJsonSpecSampleDoc(result.asParser(), true);
    }

// com.fasterxml.jackson.databind.deser.TestJacksonTypes::testTokenBufferWithSequence
    public void testTokenBufferWithSequence() throws Exception
    {
        ObjectMapper m = new ObjectMapper();
        
        JsonParser jp = createParserUsingReader("[ 32, [ 1 ], \"abc\", { \"a\" : true } ]");
        assertToken(JsonToken.START_ARRAY, jp.nextToken());

        assertToken(JsonToken.VALUE_NUMBER_INT, jp.nextToken());
        TokenBuffer buf = m.readValue(jp, TokenBuffer.class);

        
        JsonParser bufParser = buf.asParser();
        assertToken(JsonToken.VALUE_NUMBER_INT, bufParser.nextToken());
        assertEquals(32, bufParser.getIntValue());
        assertNull(bufParser.nextToken());

        
        buf = m.readValue(jp, TokenBuffer.class);
        bufParser = buf.asParser();
        assertToken(JsonToken.START_ARRAY, bufParser.nextToken());
        assertToken(JsonToken.VALUE_NUMBER_INT, bufParser.nextToken());
        assertEquals(1, bufParser.getIntValue());
        assertToken(JsonToken.END_ARRAY, bufParser.nextToken());
        assertNull(bufParser.nextToken());

        
        buf = m.readValue(jp, TokenBuffer.class);
        String str = m.readValue(buf.asParser(), String.class);
        assertEquals("abc", str);

        
        buf = m.readValue(jp, TokenBuffer.class);
        Map<?,?> map = m.readValue(buf.asParser(), Map.class);
        assertEquals(1, map.size());
        assertEquals(Boolean.TRUE, map.get("a"));
        
        assertEquals(JsonToken.END_ARRAY, jp.nextToken());
        assertNull(jp.nextToken());
    }

// com.fasterxml.jackson.databind.deser.TestJacksonTypes::testJavaType
    public void testJavaType() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        TypeFactory tf = TypeFactory.defaultInstance();
        
        String json = mapper.writeValueAsString(tf.constructType(String.class));
        assertEquals(quote(java.lang.String.class.getName()), json);
        
        JavaType t = mapper.readValue(json, JavaType.class);
        assertNotNull(t);
        assertEquals(String.class, t.getRawClass());
    }

// com.fasterxml.jackson.databind.deser.TestJdkTypes::testFile
    public void testFile() throws Exception
    {
        
        File src = new File("/test").getAbsoluteFile();
        String abs = src.getAbsolutePath();

        
        String json = mapper.writeValueAsString(abs);
        File result = mapper.readValue(json, File.class);
        assertEquals(abs, result.getAbsolutePath());

        
        final ObjectMapper mapper2 = new ObjectMapper();
        mapper2.setVisibility(PropertyAccessor.CREATOR, Visibility.NONE);

        result = mapper2.readValue(json, File.class);
        assertEquals(abs, result.getAbsolutePath());
    }

// com.fasterxml.jackson.databind.deser.TestJdkTypes::testRegexps
    public void testRegexps() throws IOException
    {
        final String PATTERN_STR = "abc:\\s?(\\d+)";
        Pattern exp = Pattern.compile(PATTERN_STR);
        
        String json = mapper.writeValueAsString(exp);
        Pattern result = mapper.readValue(json, Pattern.class);
        assertEquals(exp.pattern(), result.pattern());
    }

// com.fasterxml.jackson.databind.deser.TestJdkTypes::testCurrency
    public void testCurrency() throws IOException
    {
        Currency usd = Currency.getInstance("USD");
        assertEquals(usd, new ObjectMapper().readValue(quote("USD"), Currency.class));
    }

// com.fasterxml.jackson.databind.deser.TestJdkTypes::testLocale
    public void testLocale() throws IOException
    {
        assertEquals(new Locale("en"), mapper.readValue(quote("en"), Locale.class));
        assertEquals(new Locale("es", "ES"), mapper.readValue(quote("es_ES"), Locale.class));
        assertEquals(new Locale("FI", "fi", "savo"), mapper.readValue(quote("fi_FI_savo"), Locale.class));
    }

// com.fasterxml.jackson.databind.deser.TestJdkTypes::testNullForPrimitives
    public void testNullForPrimitives() throws IOException
    {
        
        PrimitivesBean bean = mapper.readValue("{\"intValue\":null, \"booleanValue\":null, \"doubleValue\":null}",
                PrimitivesBean.class);
        assertNotNull(bean);
        assertEquals(0, bean.intValue);
        assertEquals(false, bean.booleanValue);
        assertEquals(0.0, bean.doubleValue);

        bean = mapper.readValue("{\"byteValue\":null, \"longValue\":null, \"floatValue\":null}",
                PrimitivesBean.class);
        assertNotNull(bean);
        assertEquals((byte) 0, bean.byteValue);
        assertEquals(0L, bean.longValue);
        assertEquals(0.0f, bean.floatValue);
        
        
        final ObjectMapper mapper2 = new ObjectMapper();
        mapper2.configure(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES, true);

        
        try {
            mapper2.readValue("{\"booleanValue\":null}", PrimitivesBean.class);
            fail("Expected failure for boolean + null");
        } catch (JsonMappingException e) {
            verifyException(e, "Can not map JSON null into type boolean");
        }
        
        try {
            mapper2.readValue("{\"byteValue\":null}", PrimitivesBean.class);
            fail("Expected failure for byte + null");
        } catch (JsonMappingException e) {
            verifyException(e, "Can not map JSON null into type byte");
        }
        try {
            mapper2.readValue("{\"charValue\":null}", PrimitivesBean.class);
            fail("Expected failure for char + null");
        } catch (JsonMappingException e) {
            verifyException(e, "Can not map JSON null into type char");
        }
        try {
            mapper2.readValue("{\"shortValue\":null}", PrimitivesBean.class);
            fail("Expected failure for short + null");
        } catch (JsonMappingException e) {
            verifyException(e, "Can not map JSON null into type short");
        }
        try {
            mapper2.readValue("{\"intValue\":null}", PrimitivesBean.class);
            fail("Expected failure for int + null");
        } catch (JsonMappingException e) {
            verifyException(e, "Can not map JSON null into type int");
        }
        try {
            mapper2.readValue("{\"longValue\":null}", PrimitivesBean.class);
            fail("Expected failure for long + null");
        } catch (JsonMappingException e) {
            verifyException(e, "Can not map JSON null into type long");
        }

        
        try {
            mapper2.readValue("{\"floatValue\":null}", PrimitivesBean.class);
            fail("Expected failure for float + null");
        } catch (JsonMappingException e) {
            verifyException(e, "Can not map JSON null into type float");
        }
        try {
            mapper2.readValue("{\"doubleValue\":null}", PrimitivesBean.class);
            fail("Expected failure for double + null");
        } catch (JsonMappingException e) {
            verifyException(e, "Can not map JSON null into type double");
        }
    }

// com.fasterxml.jackson.databind.deser.TestJdkTypes::testCharSequence
    public void testCharSequence() throws IOException
    {
        CharSequence cs = mapper.readValue("\"abc\"", CharSequence.class);
        assertEquals(String.class, cs.getClass());
        assertEquals("abc", cs.toString());
    }

// com.fasterxml.jackson.databind.deser.TestJdkTypes::testInetAddress
    public void testInetAddress() throws IOException
    {
        InetAddress address = mapper.readValue(quote("127.0.0.1"), InetAddress.class);
        assertEquals("127.0.0.1", address.getHostAddress());

        
        final String HOST = "www.ning.com";
        address = mapper.readValue(quote(HOST), InetAddress.class);
        assertEquals(HOST, address.getHostName());
    }

// com.fasterxml.jackson.databind.deser.TestJdkTypes::testClass
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

// com.fasterxml.jackson.databind.deser.TestJdkTypes::testClassWithParams
    public void testClassWithParams() throws IOException
    {
        String json = mapper.writeValueAsString(new ParamClassBean("Foobar"));

        ParamClassBean result = mapper.readValue(json, ParamClassBean.class);
        assertEquals("Foobar", result.name);
        assertSame(String.class, result.clazz);
    }

// com.fasterxml.jackson.databind.deser.TestJdkTypes::testEmptyStringForWrappers
    public void testEmptyStringForWrappers() throws IOException
    {
        WrappersBean bean;

        
        bean = mapper.readValue("{\"booleanValue\":\"\"}", WrappersBean.class);
        assertNull(bean.booleanValue);
        bean = mapper.readValue("{\"byteValue\":\"\"}", WrappersBean.class);
        assertNull(bean.byteValue);

        
        bean = mapper.readValue("{\"charValue\":\"\"}", WrappersBean.class);
        assertNull(bean.charValue);

        bean = mapper.readValue("{\"shortValue\":\"\"}", WrappersBean.class);
        assertNull(bean.shortValue);
        bean = mapper.readValue("{\"intValue\":\"\"}", WrappersBean.class);
        assertNull(bean.intValue);
        bean = mapper.readValue("{\"longValue\":\"\"}", WrappersBean.class);
        assertNull(bean.longValue);
        bean = mapper.readValue("{\"floatValue\":\"\"}", WrappersBean.class);
        assertNull(bean.floatValue);
        bean = mapper.readValue("{\"doubleValue\":\"\"}", WrappersBean.class);
        assertNull(bean.doubleValue);
    }

// com.fasterxml.jackson.databind.deser.TestJdkTypes::testEmptyStringForPrimitives
    public void testEmptyStringForPrimitives() throws IOException
    {
        PrimitivesBean bean;
        bean = mapper.readValue("{\"booleanValue\":\"\"}", PrimitivesBean.class);
        assertFalse(bean.booleanValue);
        bean = mapper.readValue("{\"byteValue\":\"\"}", PrimitivesBean.class);
        assertEquals((byte) 0, bean.byteValue);
        bean = mapper.readValue("{\"charValue\":\"\"}", PrimitivesBean.class);
        assertEquals((char) 0, bean.charValue);
        bean = mapper.readValue("{\"shortValue\":\"\"}", PrimitivesBean.class);
        assertEquals((short) 0, bean.shortValue);
        bean = mapper.readValue("{\"intValue\":\"\"}", PrimitivesBean.class);
        assertEquals(0, bean.intValue);
        bean = mapper.readValue("{\"longValue\":\"\"}", PrimitivesBean.class);
        assertEquals(0L, bean.longValue);
        bean = mapper.readValue("{\"floatValue\":\"\"}", PrimitivesBean.class);
        assertEquals(0.0f, bean.floatValue);
        bean = mapper.readValue("{\"doubleValue\":\"\"}", PrimitivesBean.class);
        assertEquals(0.0, bean.doubleValue);
    }

// com.fasterxml.jackson.databind.deser.TestJdkTypes::testUntypedWithJsonArrays
    public void testUntypedWithJsonArrays() throws Exception
    {
        
        Object ob = mapper.readValue("[1]", Object.class);
        assertTrue(ob instanceof List<?>);

        
        mapper.configure(DeserializationFeature.USE_JAVA_ARRAY_FOR_JSON_ARRAY, true);
        ob = mapper.readValue("[1]", Object.class);
        assertEquals(Object[].class, ob.getClass());
    }

// com.fasterxml.jackson.databind.deser.TestJdkTypes::testLongToBoolean
    public void testLongToBoolean() throws Exception
    {
        long value = 1L + Integer.MAX_VALUE;
        BooleanBean b = mapper.readValue("{\"primitive\" : "+value+", \"wrapper\":"+value+", \"ctor\":"+value+"}",
                    BooleanBean.class);
        assertEquals(Boolean.TRUE, b.wrapper);
        assertTrue(b.primitive);
        assertEquals(Boolean.TRUE, b.ctor);
    }

// com.fasterxml.jackson.databind.deser.TestJdkTypes::testCharset
    public void testCharset() throws Exception
    {
        Charset UTF8 = Charset.forName("UTF-8");
        assertSame(UTF8, mapper.readValue(quote("UTF-8"), Charset.class));
    }

// com.fasterxml.jackson.databind.deser.TestJdkTypes::testStackTraceElement
    public void testStackTraceElement() throws Exception
    {
        StackTraceElement elem = null;
        try {
            throw new IllegalStateException();
        } catch (Exception e) {
            elem = e.getStackTrace()[0];
        }
        String json = mapper.writeValueAsString(elem);
        StackTraceElement back = mapper.readValue(json, StackTraceElement.class);
        
        assertEquals("testStackTraceElement", back.getMethodName());
        assertEquals(elem.getLineNumber(), back.getLineNumber());
        assertEquals(elem.getClassName(), back.getClassName());
        assertEquals(elem.isNativeMethod(), back.isNativeMethod());
        assertTrue(back.getClassName().endsWith("TestJdkTypes"));
        assertFalse(back.isNativeMethod());
    }

// com.fasterxml.jackson.databind.deser.TestMapDeserialization::testUntypedMap
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

// com.fasterxml.jackson.databind.deser.TestMapDeserialization::testUntypedMap2
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

// com.fasterxml.jackson.databind.deser.TestMapDeserialization::testUntypedMap3
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

// com.fasterxml.jackson.databind.deser.TestMapDeserialization::testSpecialMap
    public void testSpecialMap() throws IOException
    {
       final ObjectWrapperMap map = MAPPER.readValue(UNTYPED_MAP_JSON, ObjectWrapperMap.class);
       _doTestUntyped(map);
    }

// com.fasterxml.jackson.databind.deser.TestMapDeserialization::testGenericMap
    public void testGenericMap() throws IOException
    {
        final Map<String, ObjectWrapper> map = MAPPER.readValue
            (UNTYPED_MAP_JSON,
             new TypeReference<Map<String, ObjectWrapper>>() { });
       _doTestUntyped(map);
    }

// com.fasterxml.jackson.databind.deser.TestMapDeserialization::testFromEmptyString
    public void testFromEmptyString() throws Exception
    {
        ObjectMapper m = new ObjectMapper();
        m.configure(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, true);
        Map<?,?> result = m.readValue(quote(""), Map.class);
        assertNull(result);
    }

// com.fasterxml.jackson.databind.deser.TestMapDeserialization::testExactStringIntMap
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

// com.fasterxml.jackson.databind.deser.TestMapDeserialization::testIntBooleanMap
    public void testIntBooleanMap() throws Exception
    {
        
        String JSON = "{ \"1\" : true, \"-1\" : false }";
        Map<String,Integer> result = MAPPER.readValue
            (JSON, new TypeReference<HashMap<Integer,Boolean>>() { });

        assertNotNull(result);
        assertEquals(HashMap.class, result.getClass());
        assertEquals(2, result.size());

        assertEquals(Boolean.TRUE, result.get(Integer.valueOf(1)));
        assertEquals(Boolean.FALSE, result.get(Integer.valueOf(-1)));
        assertNull(result.get("foobar"));
        assertNull(result.get(0));
    }

// com.fasterxml.jackson.databind.deser.TestMapDeserialization::testExactStringStringMap
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

// com.fasterxml.jackson.databind.deser.TestMapDeserialization::testGenericStringIntMap
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

// com.fasterxml.jackson.databind.deser.TestMapDeserialization::testEnumMap
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

// com.fasterxml.jackson.databind.deser.TestMapDeserialization::testMapWithEnums
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

// com.fasterxml.jackson.databind.deser.TestMapDeserialization::testEnumPolymorphicSerializationTest
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

// com.fasterxml.jackson.databind.deser.TestMapDeserialization::testDateMap
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

// com.fasterxml.jackson.databind.deser.TestMapDeserialization::testCalendarMap
    public void testCalendarMap() throws Exception
    {
    	 Calendar c = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
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

// com.fasterxml.jackson.databind.deser.TestMapDeserialization::testUUIDKeyMap
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

// com.fasterxml.jackson.databind.deser.TestMapDeserialization::testLocaleKeyMap
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

// com.fasterxml.jackson.databind.deser.TestMapDeserialization::testKeyWithCreator
    public void testKeyWithCreator() throws Exception
    {
        
        KeyType key = MAPPER.readValue(quote("abc"), KeyType.class);
        assertEquals("abc", key.value);

        Map<KeyType,Integer> map = MAPPER.readValue("{\"foo\":3}", new TypeReference<Map<KeyType,Integer>>() {} );
        assertEquals(1, map.size());
        key = map.keySet().iterator().next();
        assertEquals("foo", key.value);
    }

// com.fasterxml.jackson.databind.deser.TestMapDeserialization::testMapWithDeserializer
    public void testMapWithDeserializer() throws IOException
    {
        CustomMap result = MAPPER.readValue(quote("xyz"), CustomMap.class);
        assertEquals(1, result.size());
        assertEquals("xyz", result.get("x"));
    }

// com.fasterxml.jackson.databind.deser.TestMapDeserialization::testMapError
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

// com.fasterxml.jackson.databind.deser.TestMapDeserialization::testNoCtorMap
    public void testNoCtorMap() throws Exception
    {
        try {
            BrokenMap result = MAPPER.readValue("{ \"a\" : 3 }", BrokenMap.class);
            
            assertNull(result);
        } catch (JsonMappingException e) {
            
            verifyException(e, "no default constructor found");
        }
    }

// com.fasterxml.jackson.databind.deser.TestParentChildReferences::testSimpleRefs
    public void testSimpleRefs() throws Exception
    {
        SimpleTreeNode root = new SimpleTreeNode("root");
        SimpleTreeNode child = new SimpleTreeNode("kid");
        ObjectMapper mapper = new ObjectMapper();
        root.child = child;
        child.parent = root;
        
        String json = mapper.writeValueAsString(root);
        
        SimpleTreeNode resultNode = mapper.readValue(json, SimpleTreeNode.class);
        assertEquals("root", resultNode.name);
        SimpleTreeNode resultChild = resultNode.child;
        assertNotNull(resultChild);
        assertEquals("kid", resultChild.name);
        assertSame(resultChild.parent, resultNode);
    }

// com.fasterxml.jackson.databind.deser.TestParentChildReferences::testSimpleRefsWithGetter
    public void testSimpleRefsWithGetter() throws Exception
    {
        SimpleTreeNode2 root = new SimpleTreeNode2("root");
        SimpleTreeNode2 child = new SimpleTreeNode2("kid");
        ObjectMapper mapper = new ObjectMapper();
        root.child = child;
        child.parent = root;
        
        String json = mapper.writeValueAsString(root);
        
        SimpleTreeNode2 resultNode = mapper.readValue(json, SimpleTreeNode2.class);
        assertEquals("root", resultNode.name);
        SimpleTreeNode2 resultChild = resultNode.child;
        assertNotNull(resultChild);
        assertEquals("kid", resultChild.name);
        assertSame(resultChild.parent, resultNode);
    }

// com.fasterxml.jackson.databind.deser.TestParentChildReferences::testFullRefs
    public void testFullRefs() throws Exception
    {
        FullTreeNode root = new FullTreeNode("root");
        FullTreeNode child1 = new FullTreeNode("kid1");
        FullTreeNode child2 = new FullTreeNode("kid2");
        ObjectMapper mapper = new ObjectMapper();
        root.firstChild = child1;
        child1.parent = root;
        child1.next = child2;
        child2.prev = child1;
        
        String json = mapper.writeValueAsString(root);
        
        FullTreeNode resultNode = mapper.readValue(json, FullTreeNode.class);
        assertEquals("root", resultNode.name);
        FullTreeNode resultChild = resultNode.firstChild;
        assertNotNull(resultChild);
        assertEquals("kid1", resultChild.name);
        assertSame(resultChild.parent, resultNode);

        
        assertNull(resultChild.prev);
        FullTreeNode resultChild2 = resultChild.next;
        assertNotNull(resultChild2);
        assertEquals("kid2", resultChild2.name);
        assertSame(resultChild, resultChild2.prev);
        assertNull(resultChild2.next);
    }

// com.fasterxml.jackson.databind.deser.TestParentChildReferences::testArrayOfRefs
    public void testArrayOfRefs() throws Exception
    {
        NodeArray root = new NodeArray();
        ArrayNode node1 = new ArrayNode("a");
        ArrayNode node2 = new ArrayNode("b");
        root.nodes = new ArrayNode[] { node1, node2 };
        ObjectMapper mapper = new ObjectMapper();
        String json = mapper.writeValueAsString(root);
        
        NodeArray result = mapper.readValue(json, NodeArray.class);
        ArrayNode[] kids = result.nodes;
        assertNotNull(kids);
        assertEquals(2, kids.length);
        assertEquals("a", kids[0].name);
        assertEquals("b", kids[1].name);
        assertSame(result, kids[0].parent);
        assertSame(result, kids[1].parent);
    }

// com.fasterxml.jackson.databind.deser.TestParentChildReferences::testListOfRefs
    public void testListOfRefs() throws Exception
    {
        NodeList root = new NodeList();
        NodeForList node1 = new NodeForList("a");
        NodeForList node2 = new NodeForList("b");
        root.nodes = Arrays.asList(node1, node2);
        ObjectMapper mapper = new ObjectMapper();
        String json = mapper.writeValueAsString(root);
        
        NodeList result = mapper.readValue(json, NodeList.class);
        List<NodeForList> kids = result.nodes;
        assertNotNull(kids);
        assertEquals(2, kids.size());
        assertEquals("a", kids.get(0).name);
        assertEquals("b", kids.get(1).name);
        assertSame(result, kids.get(0).parent);
        assertSame(result, kids.get(1).parent);
    }

// com.fasterxml.jackson.databind.deser.TestParentChildReferences::testMapOfRefs
    public void testMapOfRefs() throws Exception
    {
        NodeMap root = new NodeMap();
        NodeForMap node1 = new NodeForMap("a");
        NodeForMap node2 = new NodeForMap("b");
        Map<String,NodeForMap> nodes = new HashMap<String, NodeForMap>();
        nodes.put("a1", node1);
        nodes.put("b2", node2);
        root.nodes = nodes;
        ObjectMapper mapper = new ObjectMapper();
        String json = mapper.writeValueAsString(root);
        
        NodeMap result = mapper.readValue(json, NodeMap.class);
        Map<String,NodeForMap> kids = result.nodes;
        assertNotNull(kids);
        assertEquals(2, kids.size());
        assertNotNull(kids.get("a1"));
        assertNotNull(kids.get("b2"));
        assertEquals("a", kids.get("a1").name);
        assertEquals("b", kids.get("b2").name);
        assertSame(result, kids.get("a1").parent);
        assertSame(result, kids.get("b2").parent);
    }

// com.fasterxml.jackson.databind.deser.TestParentChildReferences::testAbstract368
    public void testAbstract368() throws Exception
    {
        AbstractNode parent = new ConcreteNode("p");
        AbstractNode child = new ConcreteNode("c");
        parent.next = child;
        child.prev = parent;

        
        ObjectMapper mapper = new ObjectMapper();
        String json = mapper.writeValueAsString(parent);

        AbstractNode root = mapper.readValue(json, AbstractNode.class);

        assertEquals(ConcreteNode.class, root.getClass());
        assertEquals("p", root.id);
        assertNull(root.prev);
        AbstractNode leaf = root.next;
        assertNotNull(leaf);
        assertEquals("c", leaf.id);
        assertSame(root, leaf.prev);
    }

// com.fasterxml.jackson.databind.deser.TestParentChildReferences::testIssue693
    public void testIssue693() throws Exception
    {
        Parent parent = new Parent();
        parent.addChild(new Child("foo"));
        parent.addChild(new Child("bar"));
        ObjectMapper mapper = new ObjectMapper();
        byte[] bytes = mapper.writeValueAsBytes(parent);
        Parent value = mapper.readValue(bytes, Parent.class); 
        for (Child child : value.children) {
            assertEquals(value, child.getParent());
        }
    }

// com.fasterxml.jackson.databind.deser.TestParentChildReferences::testIssue708
    public void testIssue708() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        Advertisement708 ad = mapper.readValue("{\"title\":\"Hroch\",\"photos\":[{\"id\":3}]}", Advertisement708.class);      
        assertNotNull(ad);
    }

// com.fasterxml.jackson.databind.filter.TestIgnorePropsForSerialization::testExplicitIgnoralWithBean
    public void testExplicitIgnoralWithBean() throws Exception
    {
        IgnoreSome value = new IgnoreSome();
        Map<String,Object> result = writeAndMap(MAPPER, value);
        assertEquals(2, result.size());
        
        assertFalse(result.containsKey("b"));
        assertFalse(result.containsKey("c"));
        
        assertEquals(Integer.valueOf(value.a), result.get("a"));
        assertEquals(value.getD(), result.get("d"));
    }

// com.fasterxml.jackson.databind.filter.TestIgnorePropsForSerialization::testExplicitIgnoralWithMap
    public void testExplicitIgnoralWithMap() throws Exception
    {
        
        MyMap value = new MyMap();
        value.put("a", "b");
        value.put("@class", MyMap.class.getName());
        Map<String,Object> result = writeAndMap(MAPPER, value);
        assertEquals(1, result.size());
        
        assertFalse(result.containsKey("@class"));
        
        assertEquals(value.get("a"), result.get("a"));
    }

// com.fasterxml.jackson.databind.filter.TestIgnorePropsForSerialization::testIgnoreViaOnlyProps
    public void testIgnoreViaOnlyProps() throws Exception
    {
        assertEquals("{\"value\":{\"x\":1}}",
                MAPPER.writeValueAsString(new WrapperWithPropIgnore()));
    }

// com.fasterxml.jackson.databind.filter.TestIgnorePropsForSerialization::testIgnoreWithMapProperty
    public void testIgnoreWithMapProperty() throws Exception
    {
        assertEquals("{\"value\":{\"b\":2}}", MAPPER.writeValueAsString(new MapWrapper()));
    }

// com.fasterxml.jackson.databind.filter.TestIgnorePropsForSerialization::testIgnoreViaPropsAndClass
    public void testIgnoreViaPropsAndClass() throws Exception
    {
        assertEquals("{\"value\":{\"y\":2}}",
                MAPPER.writeValueAsString(new WrapperWithPropIgnore2()));
    }

// com.fasterxml.jackson.databind.filter.TestSimpleSerializationIgnore::testSimpleIgnore
    public void testSimpleIgnore() throws Exception
    {
        ObjectMapper m = new ObjectMapper();
        
        Map<String,Object> result = writeAndMap(m, new SizeClassEnabledIgnore());
        assertEquals(1, result.size());
        assertEquals(Integer.valueOf(1), result.get("x"));
        assertNull(result.get("y"));
    }

// com.fasterxml.jackson.databind.filter.TestSimpleSerializationIgnore::testDisabledIgnore
    public void testDisabledIgnore() throws Exception
    {
        ObjectMapper m = new ObjectMapper();
        
        Map<String,Object> result = writeAndMap(m, new SizeClassDisabledIgnore());
        assertEquals(2, result.size());
        assertEquals(Integer.valueOf(3), result.get("x"));
        assertEquals(Integer.valueOf(4), result.get("y"));
    }

// com.fasterxml.jackson.databind.filter.TestSimpleSerializationIgnore::testIgnoreOver
    public void testIgnoreOver() throws Exception
    {
        ObjectMapper m = new ObjectMapper();

        
        Map<String,Object> result = writeAndMap(m, new BaseClassIgnore());
        assertEquals(1, result.size());
        assertEquals(Integer.valueOf(2), result.get("y"));

        
        result = writeAndMap(m, new SubClassNonIgnore());
        assertEquals(2, result.size());
        assertEquals(Integer.valueOf(3), result.get("x"));
        assertEquals(Integer.valueOf(2), result.get("y"));
    }

// com.fasterxml.jackson.databind.filter.TestSimpleSerializationIgnore::testIgnoreType
    public void testIgnoreType() throws Exception
    {
        ObjectMapper m = new ObjectMapper();
        assertEquals("{\"value\":13}", m.writeValueAsString(new NonIgnoredType()));
    }

// com.fasterxml.jackson.databind.interop.TestCglibUsage::testSimpleProxied
    public void testSimpleProxied() throws Exception
    {
        Enhancer enh = new Enhancer();
        enh.setInterfaces(new Class[] { BeanInterface.class });
        enh.setCallback(new MethodInterceptor() {
            @Override
            public Object intercept(Object obj, Method method,
                                        Object[] args, MethodProxy proxy)
                    throws Throwable
                {
                    if ("getX".equals(method.getName ())) {
                        return Integer.valueOf(13);
                    }
                    return proxy.invokeSuper(obj, args);
                }
            });
        BeanInterface bean = (BeanInterface) enh.create();
        ObjectMapper mapper = new ObjectMapper();
        Map<String,Object> result = writeAndMap(mapper, bean);
        assertEquals(1, result.size());
        assertEquals(Integer.valueOf(13), result.get("x"));
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

// com.fasterxml.jackson.databind.interop.TestGroovyBeans::testSimpleSerialization
    public void testSimpleSerialization() throws Exception
    {
        Object ob = newGroovyObject(SIMPLE_POGO);
        Map<String,Object> result = writeAndMap(MAPPER, ob);
        assertEquals(2, result.size());
        assertEquals("whome", result.get("name"));
        
        Object num = result.get("id");
        assertNotNull(num);
        assertTrue(num instanceof Number);
        assertEquals(3, ((Number) num).intValue());
    }

// com.fasterxml.jackson.databind.interop.TestGroovyBeans::testSimpleDeserialization
    public void testSimpleDeserialization() throws Exception
    {
        Class<?> cls = defineGroovyClass(SIMPLE_POGO);
        
        Object pogo = MAPPER.readValue("{\"id\":9,\"name\":\"Bob\"}", cls);
        assertNotNull(pogo);
        
        Map<String,Object> result = writeAndMap(MAPPER, pogo);
        assertEquals(2, result.size());
        assertEquals("Bob", result.get("name"));
        
        Object num = result.get("id");
        assertNotNull(num);
        assertTrue(num instanceof Number);
        assertEquals(9, ((Number) num).intValue());
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

// com.fasterxml.jackson.databind.introspect.TestAnnotionBundles::testBundledIgnore
    public void testBundledIgnore() throws Exception
    {
        assertEquals("{\"foobar\":13}", MAPPER.writeValueAsString(new Bean()));
    }

// com.fasterxml.jackson.databind.introspect.TestAnnotionBundles::testVisibilityBundle
    public void testVisibilityBundle() throws Exception
    {
        assertEquals("{\"b\":5}", MAPPER.writeValueAsString(new NoAutoDetect()));
    }

// com.fasterxml.jackson.databind.introspect.TestAnnotionBundles::testIssue92
    public void testIssue92() throws Exception
    {
        assertEquals("{\"_id\":\"abc\"}", MAPPER.writeValueAsString(new Bean92()));
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
        JacksonAnnotationIntrospector ai = new JacksonAnnotationIntrospector();
        AnnotatedClass ac = AnnotatedClass.constructWithoutSuperTypes(TypeResolverBean.class, ai, null);
        JavaType baseType = TypeFactory.defaultInstance().constructType(TypeResolverBean.class);
        ObjectMapper mapper = new ObjectMapper();
        TypeResolverBuilder<?> rb = ai.findTypeResolver(mapper.getDeserializationConfig(), ac, baseType);
        assertNotNull(rb);
        assertSame(DummyBuilder.class, rb.getClass());
    }

// com.fasterxml.jackson.databind.introspect.TestJacksonAnnotationIntrospector::testIgnoredType
    public void testIgnoredType() throws Exception
    {
        JacksonAnnotationIntrospector ai = new JacksonAnnotationIntrospector();
        AnnotatedClass ac = AnnotatedClass.construct(IgnoredType.class, ai, null);
        assertEquals(Boolean.TRUE, ai.isIgnorableType(ac));

        
        ac = AnnotatedClass.construct(IgnoredSubType.class, ai, null);
        assertEquals(Boolean.TRUE, ai.isIgnorableType(ac));
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

// com.fasterxml.jackson.databind.introspect.TestPOJOPropertiesCollector::testSimple
    public void testSimple()
    {
        POJOPropertiesCollector coll = collector(mapper,
        		Simple.class, true);
        Map<String, POJOPropertyBuilder> props = coll.getPropertyMap();
        assertEquals(1, props.size());
        POJOPropertyBuilder prop = props.get("value");
        assertNotNull(prop);
        assertTrue(prop.hasSetter());
        assertTrue(prop.hasGetter());
        assertTrue(prop.hasField());
    }

// com.fasterxml.jackson.databind.introspect.TestPOJOPropertiesCollector::testSimpleFieldVisibility
    public void testSimpleFieldVisibility()
    {
        
        POJOPropertiesCollector coll = collector(mapper,
        		SimpleFieldDeser.class, false);
        Map<String, POJOPropertyBuilder> props = coll.getPropertyMap();
        assertEquals(1, props.size());
        POJOPropertyBuilder prop = props.get("values");
        assertNotNull(prop);
        assertFalse(prop.hasSetter());
        assertFalse(prop.hasGetter());
        assertTrue(prop.hasField());
    }

// com.fasterxml.jackson.databind.introspect.TestPOJOPropertiesCollector::testSimpleGetterVisibility
    public void testSimpleGetterVisibility()
    {
        POJOPropertiesCollector coll = collector(mapper,
        		SimpleGetterVisibility.class, true);
        Map<String, POJOPropertyBuilder> props = coll.getPropertyMap();
        assertEquals(1, props.size());
        POJOPropertyBuilder prop = props.get("a");
        assertNotNull(prop);
        assertFalse(prop.hasSetter());
        assertTrue(prop.hasGetter());
        assertFalse(prop.hasField());
    }

// com.fasterxml.jackson.databind.introspect.TestPOJOPropertiesCollector::testEmpty
    public void testEmpty()
    {
        POJOPropertiesCollector coll = collector(mapper,
        		Empty.class, true);
        Map<String, POJOPropertyBuilder> props = coll.getPropertyMap();
        assertEquals(0, props.size());
    }

// com.fasterxml.jackson.databind.introspect.TestPOJOPropertiesCollector::testPartialIgnore
    public void testPartialIgnore()
    {
        POJOPropertiesCollector coll = collector(mapper,
        		IgnoredSetter.class, true);
        Map<String, POJOPropertyBuilder> props = coll.getPropertyMap();
        assertEquals(1, props.size());
        POJOPropertyBuilder prop = props.get("value");
        assertNotNull(prop);
        assertFalse(prop.hasSetter());
        assertTrue(prop.hasGetter());
        assertTrue(prop.hasField());
    }

// com.fasterxml.jackson.databind.introspect.TestPOJOPropertiesCollector::testSimpleRenamed
    public void testSimpleRenamed()
    {
        POJOPropertiesCollector coll = collector(mapper,
        		RenamedProperties.class, true);
        Map<String, POJOPropertyBuilder> props = coll.getPropertyMap();
        assertEquals(1, props.size());
        POJOPropertyBuilder prop = props.get("x");
        assertNotNull(prop);
        assertTrue(prop.hasSetter());
        assertTrue(prop.hasGetter());
        assertTrue(prop.hasField());
    }

// com.fasterxml.jackson.databind.introspect.TestPOJOPropertiesCollector::testSimpleRenamed2
    public void testSimpleRenamed2()
    {
        POJOPropertiesCollector coll = collector(mapper,
        		RenamedProperties2.class, true);
        Map<String, POJOPropertyBuilder> props = coll.getPropertyMap();
        assertEquals(1, props.size());
        POJOPropertyBuilder prop = props.get("renamed");
        assertNotNull(prop);
        assertTrue(prop.hasSetter());
        assertTrue(prop.hasGetter());
        assertFalse(prop.hasField());
    }

// com.fasterxml.jackson.databind.introspect.TestPOJOPropertiesCollector::testMergeWithRename
    public void testMergeWithRename()
    {
        POJOPropertiesCollector coll = collector(mapper,
        		MergedProperties.class, true);
        Map<String, POJOPropertyBuilder> props = coll.getPropertyMap();
        assertEquals(1, props.size());
        POJOPropertyBuilder prop = props.get("x");
        assertNotNull(prop);
        assertTrue(prop.hasSetter());
        assertFalse(prop.hasGetter());
        assertTrue(prop.hasField());
    }

// com.fasterxml.jackson.databind.introspect.TestPOJOPropertiesCollector::testSimpleIgnoreAndRename
    public void testSimpleIgnoreAndRename()
    {
        POJOPropertiesCollector coll = collector(mapper,
        		IgnoredRenamedSetter.class, true);
        Map<String, POJOPropertyBuilder> props = coll.getPropertyMap();
        assertEquals(1, props.size());
        POJOPropertyBuilder prop = props.get("y");
        assertNotNull(prop);
        assertTrue(prop.hasSetter());
        assertFalse(prop.hasGetter());
        assertFalse(prop.hasField());
    }

// com.fasterxml.jackson.databind.introspect.TestPOJOPropertiesCollector::testGlobalVisibilityForGetters
    public void testGlobalVisibilityForGetters()
    {
        ObjectMapper m = new ObjectMapper();
        m.configure(MapperFeature.AUTO_DETECT_GETTERS, false);
        POJOPropertiesCollector coll = collector(m, SimpleGetterVisibility.class, true);
        
        Map<String, POJOPropertyBuilder> props = coll.getPropertyMap();
        assertEquals(0, props.size());
    }

// com.fasterxml.jackson.databind.introspect.TestPOJOPropertiesCollector::testCollectionOfIgnored
    public void testCollectionOfIgnored()
    {
        POJOPropertiesCollector coll = collector(mapper, ImplicitIgnores.class, false);
        
        Map<String, POJOPropertyBuilder> props = coll.getPropertyMap();
        assertEquals(1, props.size());
        
        Collection<String> ign = coll.getIgnoredPropertyNames();
        assertEquals(2, ign.size());
        assertTrue(ign.contains("a"));
        assertTrue(ign.contains("b"));
    }

// com.fasterxml.jackson.databind.introspect.TestPOJOPropertiesCollector::testSimpleOrderingForDeserialization
    public void testSimpleOrderingForDeserialization()
    {
        POJOPropertiesCollector coll = collector(mapper, SortedProperties.class, false);
        List<BeanPropertyDefinition> props = coll.getProperties();
        assertEquals(4, props.size());
        assertEquals("a", props.get(0).getName());
        assertEquals("b", props.get(1).getName());
        assertEquals("c", props.get(2).getName());
        assertEquals("d", props.get(3).getName());
    }

// com.fasterxml.jackson.databind.introspect.TestPOJOPropertiesCollector::testSimpleWithType
    public void testSimpleWithType()
    {
        
        POJOPropertiesCollector coll = collector(mapper, TypeTestBean.class, true);
        List<BeanPropertyDefinition> props = coll.getProperties();
        assertEquals(1, props.size());
        assertEquals("value", props.get(0).getName());
        AnnotatedMember m = props.get(0).getAccessor();
        assertTrue(m instanceof AnnotatedMethod);
        assertEquals(Integer.class, m.getRawType());

        
        coll = collector(mapper, TypeTestBean.class, false);
        props = coll.getProperties();
        assertEquals(1, props.size());
        assertEquals("value", props.get(0).getName());
        m = props.get(0).getMutator();
        assertEquals(AnnotatedParameter.class, m.getClass());
        assertEquals(String.class, m.getRawType());
    }

// com.fasterxml.jackson.databind.introspect.TestPOJOPropertiesCollector::testInnerClassWithAnnotationsInCreator
    public void testInnerClassWithAnnotationsInCreator() throws Exception
    {
        BasicBeanDescription beanDesc;
        
        beanDesc = mapper.getSerializationConfig().introspect(mapper.constructType(Issue701Bean.class));
        assertNotNull(beanDesc);
        
        beanDesc = mapper.getDeserializationConfig().introspect(mapper.constructType(Issue701Bean.class));
        assertNotNull(beanDesc);
    }

// com.fasterxml.jackson.databind.introspect.TestPOJOPropertiesCollector::testJackson703
    public void testJackson703() throws Exception
    {
    	
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(MapperFeature.USE_ANNOTATIONS, false);
        BasicBeanDescription beanDesc = mapper.getSerializationConfig().introspect(mapper.constructType(Jackson703.class));
        assertNotNull(beanDesc);

        Jackson703 bean = new Jackson703();
        String json = mapper.writeValueAsString(bean);
        assertNotNull(json);
    }

// com.fasterxml.jackson.databind.introspect.TestPOJOPropertiesCollector::testJackson744
    public void testJackson744() throws Exception
    {
        BasicBeanDescription beanDesc = mapper.getDeserializationConfig().introspect(mapper.constructType(Issue744Bean.class));
        assertNotNull(beanDesc);
        AnnotatedMethod setter = beanDesc.findAnySetter();
        assertNotNull(setter);
    }

// com.fasterxml.jackson.databind.jsonschema.TestGenerateJsonSchema::testGeneratingJsonSchema
    public void testGeneratingJsonSchema()
        throws Exception
    {
        JsonSchema jsonSchema = MAPPER.generateJsonSchema(SimpleBean.class);
        
        assertNotNull(jsonSchema);

        
        assertTrue(jsonSchema.equals(jsonSchema));
        assertFalse(jsonSchema.equals(null));
        assertFalse(jsonSchema.equals("foo"));

        
        assertNotNull(jsonSchema.toString());
        assertNotNull(JsonSchema.getDefaultSchemaNode());

	ObjectNode root = jsonSchema.getSchemaNode();
        assertEquals("object", root.get("type").asText());
        assertEquals(false, root.path("required").booleanValue());
        JsonNode propertiesSchema = root.get("properties");
        assertNotNull(propertiesSchema);
        JsonNode property1Schema = propertiesSchema.get("property1");
        assertNotNull(property1Schema);
        assertEquals("integer", property1Schema.get("type").asText());
        assertEquals(false, property1Schema.path("required").booleanValue());
        JsonNode property2Schema = propertiesSchema.get("property2");
        assertNotNull(property2Schema);
        assertEquals("string", property2Schema.get("type").asText());
        assertEquals(false, property2Schema.path("required").booleanValue());
        JsonNode property3Schema = propertiesSchema.get("property3");
        assertNotNull(property3Schema);
        assertEquals("array", property3Schema.get("type").asText());
        assertEquals(false, property3Schema.path("required").booleanValue());
        assertEquals("string", property3Schema.get("items").get("type").asText());
        JsonNode property4Schema = propertiesSchema.get("property4");
        assertNotNull(property4Schema);
        assertEquals("array", property4Schema.get("type").asText());
        assertEquals(false, property4Schema.path("required").booleanValue());
        assertEquals("number", property4Schema.get("items").get("type").asText());
    }

// com.fasterxml.jackson.databind.jsonschema.TestGenerateJsonSchema::testGeneratingJsonSchemaWithFilters
    public void testGeneratingJsonSchemaWithFilters() throws Exception {
    	ObjectMapper mapper = new ObjectMapper();
    	mapper.setFilters(secretFilterProvider);
    	JsonSchema schema = mapper.generateJsonSchema(FilteredBean.class);
    	JsonNode node = schema.getSchemaNode().get("properties");
    	assertTrue(node.has("obvious"));
    	assertFalse(node.has("secret"));
    }

// com.fasterxml.jackson.databind.jsonschema.TestGenerateJsonSchema::testSchemaSerialization
    public void testSchemaSerialization()
            throws Exception
    {
        JsonSchema jsonSchema = MAPPER.generateJsonSchema(SimpleBean.class);
	Map<String,Object> result = writeAndMap(MAPPER, jsonSchema);
	assertNotNull(result);
	
	assertEquals("object", result.get("type"));
	
	assertNull(result.get("required"));
	assertNotNull(result.get("properties"));
    }

// com.fasterxml.jackson.databind.jsonschema.TestGenerateJsonSchema::testInvalidCall
    public void testInvalidCall()
        throws Exception
    {
        
        try {
            MAPPER.generateJsonSchema(null);
            fail("Should have failed");
        } catch (IllegalArgumentException iae) {
            verifyException(iae, "class must be provided");
        }
    }

// com.fasterxml.jackson.databind.jsonschema.TestGenerateJsonSchema::testThatObjectsHaveNoItems
    public void testThatObjectsHaveNoItems() throws Exception
    {
        JsonSchema jsonSchema = MAPPER.generateJsonSchema(TrivialBean.class);
        String json = jsonSchema.toString().replaceAll("\"", "'");
        
        
        assertEquals("{'type':'object','properties':{'name':{'type':'string'}}}",
                json);
    }

// com.fasterxml.jackson.databind.jsonschema.TestGenerateJsonSchema::testSchemaId
    public void testSchemaId() throws Exception
    {
        JsonSchema jsonSchema = MAPPER.generateJsonSchema(BeanWithId.class);
        String json = jsonSchema.toString().replaceAll("\"", "'");
        assertEquals("{'type':'object','id':'myType','properties':{'value':{'type':'string'}}}",
                json);
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

// com.fasterxml.jackson.databind.jsontype.TestAbstractWithObjectId::testIssue877
    public void testIssue877() throws Exception
    {
        
        BaseInterfaceImpl one = new BaseInterfaceImpl();
        BaseInterfaceImpl two = new BaseInterfaceImpl();

        
        one.addInstance(two);
        two.addInstance(one);

        
        ListWrapper<BaseInterfaceImpl> myList = new ListWrapper<BaseInterfaceImpl>();
        myList.add(one);
        myList.add(two);

        
        ObjectMapper om = new ObjectMapper();
        om.enableDefaultTypingAsProperty(ObjectMapper.DefaultTyping.NON_FINAL, "@class");

        
        String json = om.writerWithDefaultPrettyPrinter().writeValueAsString(myList);
        ListWrapper<BaseInterfaceImpl> result;
        
        result = om.readValue(json, new TypeReference<ListWrapper<BaseInterfaceImpl>>() { });

        assertNotNull(result);
        
        System.out.println("deserialised list size = " + result.size());
    }

// com.fasterxml.jackson.databind.jsontype.TestCustomTypeIdResolver::testCustomTypeIdResolver
    public void testCustomTypeIdResolver() throws Exception
    {
        ObjectMapper m = new ObjectMapper();
        List<JavaType> types = new ArrayList<JavaType>();
        CustomResolver.initTypes = types;
        String json = m.writeValueAsString(new CustomBean[] { new CustomBean(28) });
        assertEquals("[{\"*\":{\"x\":28}}]", json);
        assertEquals(1, types.size());
        assertEquals(CustomBean.class, types.get(0).getRawClass());

        types = new ArrayList<JavaType>();
        CustomResolver.initTypes = types;
        CustomBean[] result = m.readValue(json, CustomBean[].class);
        assertNotNull(result);
        assertEquals(1, result.length);
        assertEquals(28, result[0].x);
        assertEquals(1, types.size());
        assertEquals(CustomBean.class, types.get(0).getRawClass());
    }

// com.fasterxml.jackson.databind.jsontype.TestDefaultForArrays::testArrayTypingSimple
    public void testArrayTypingSimple() throws Exception
    {
        ObjectMapper m = new ObjectMapper();
        m.enableDefaultTyping(DefaultTyping.NON_CONCRETE_AND_ARRAYS);
        ArrayBean bean = new ArrayBean(new String[0]);
        String json = m.writeValueAsString(bean);
        ArrayBean result = m.readValue(json, ArrayBean.class);
        assertNotNull(result.values);
        assertEquals(String[].class, result.values.getClass());
    }

// com.fasterxml.jackson.databind.jsontype.TestDefaultForArrays::testArrayTypingNested
    public void testArrayTypingNested() throws Exception
    {
        ObjectMapper m = new ObjectMapper();
        m.enableDefaultTyping(DefaultTyping.NON_CONCRETE_AND_ARRAYS);
        ArrayBean bean = new ArrayBean(new String[0][0]);
        String json = m.writeValueAsString(bean);
        ArrayBean result = m.readValue(json, ArrayBean.class);
        assertNotNull(result.values);
        assertEquals(String[][].class, result.values.getClass());
    }

// com.fasterxml.jackson.databind.jsontype.TestDefaultForArrays::testNodeInArray
    public void testNodeInArray() throws Exception
    {
        JsonNode node = new ObjectMapper().readTree("{\"a\":3}");

        ObjectMapper m = new ObjectMapper();
        m.enableDefaultTyping(DefaultTyping.JAVA_LANG_OBJECT);
        Object[] obs = new Object[] { node };
        String json = m.writeValueAsString(obs);
        Object[] result = m.readValue(json, Object[].class);
        assertEquals(1, result.length);
        Object ob = result[0];
        assertTrue(ob instanceof JsonNode);
    }

// com.fasterxml.jackson.databind.jsontype.TestDefaultForArrays::testArraysOfArrays
    public void testArraysOfArrays() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        mapper.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL, JsonTypeInfo.As.PROPERTY);

        Object value = new Object[][] { new Object[] {} };
        String json = mapper.writeValueAsString(value);

        
        _testArraysAs(mapper, json, Object[][].class);
        _testArraysAs(mapper, json, Object[].class);
        _testArraysAs(mapper, json, Object.class);
    }

// com.fasterxml.jackson.databind.jsontype.TestDefaultForEnums::testSimpleEnumBean
    public void testSimpleEnumBean() throws Exception
    {
        TimeUnitBean bean = new TimeUnitBean();
        bean.timeUnit = TimeUnit.SECONDS;
        
        
        ObjectMapper m = new ObjectMapper();
        String json = m.writeValueAsString(bean);
        TimeUnitBean result = m.readValue(json, TimeUnitBean.class);
        assertEquals(TimeUnit.SECONDS, result.timeUnit);
        
        
        m = new ObjectMapper();
        m.enableDefaultTyping();
        json = m.writeValueAsString(bean);
        result = m.readValue(json, TimeUnitBean.class);

        assertEquals(TimeUnit.SECONDS, result.timeUnit);
    }

// com.fasterxml.jackson.databind.jsontype.TestDefaultForEnums::testSimpleEnumsInObjectArray
    public void testSimpleEnumsInObjectArray() throws Exception
    {
        ObjectMapper m = new ObjectMapper();
        m.enableDefaultTyping();
        
        
        String json = m.writeValueAsString(new Object[] { TestEnum.A });
        assertEquals("[[\"com.fasterxml.jackson.databind.jsontype.TestDefaultForEnums$TestEnum\",\"A\"]]", json);

        
        Object[] value = m.readValue(json, Object[].class);
        assertEquals(1, value.length);
        assertSame(TestEnum.A, value[0]);
    }

// com.fasterxml.jackson.databind.jsontype.TestDefaultForEnums::testSimpleEnumsAsField
    public void testSimpleEnumsAsField() throws Exception
    {
        ObjectMapper m = new ObjectMapper();
        m.enableDefaultTyping();
        String json = m.writeValueAsString(new EnumHolder(TestEnum.B));
        assertEquals("{\"value\":[\"com.fasterxml.jackson.databind.jsontype.TestDefaultForEnums$TestEnum\",\"B\"]}", json);
        EnumHolder holder = m.readValue(json, EnumHolder.class);
        assertSame(TestEnum.B, holder.value);
    }

// com.fasterxml.jackson.databind.jsontype.TestDefaultForLists::testListOfLongs
    public void testListOfLongs() throws Exception
    {
        ObjectMapper m = new ObjectMapper();
        m.enableDefaultTyping();
        ListOfLongs input = new ListOfLongs(1L, 2L, 3L);
        String json = m.writeValueAsString(input);
        assertEquals("{\"longs\":[\"java.util.ArrayList\",[1,2,3]]}", json);
        ListOfLongs output = m.readValue(json, ListOfLongs.class);

        assertNotNull(output.longs);
        assertEquals(3, output.longs.size());
        assertEquals(Long.valueOf(1L), output.longs.get(0));
        assertEquals(Long.valueOf(2L), output.longs.get(1));
        assertEquals(Long.valueOf(3L), output.longs.get(2));
    }

// com.fasterxml.jackson.databind.jsontype.TestDefaultForLists::testListOfNumbers
    public void testListOfNumbers() throws Exception
    {
        ObjectMapper m = new ObjectMapper();
        m.enableDefaultTyping();
        ListOfNumbers input = new ListOfNumbers(Long.valueOf(1L), Integer.valueOf(2), Double.valueOf(3.0));
        String json = m.writeValueAsString(input);
        assertEquals("{\"nums\":[\"java.util.ArrayList\",[[\"java.lang.Long\",1],2,3.0]]}", json);
        ListOfNumbers output = m.readValue(json, ListOfNumbers.class);

        assertNotNull(output.nums);
        assertEquals(3, output.nums.size());
        assertEquals(Long.valueOf(1L), output.nums.get(0));
        assertEquals(Integer.valueOf(2), output.nums.get(1));
        assertEquals(Double.valueOf(3.0), output.nums.get(2));
    }

// com.fasterxml.jackson.databind.jsontype.TestDefaultForLists::testDateTypes
    public void testDateTypes() throws Exception
    {
        ObjectMapper m = new ObjectMapper();
        m.enableDefaultTyping();
        ObjectListBean input = new ObjectListBean();
        List<Object> inputList = new ArrayList<Object>();
        inputList.add(TimeZone.getTimeZone("EST"));
        inputList.add(Locale.CHINESE);
        input.values = inputList;
        String json = m.writeValueAsString(input);
        
        ObjectListBean output = m.readValue(json, ObjectListBean.class);
        List<Object> outputList = output.values;
        assertEquals(2, outputList.size());
        assertTrue(outputList.get(0) instanceof TimeZone);
        assertTrue(outputList.get(1) instanceof Locale);
    }

// com.fasterxml.jackson.databind.jsontype.TestDefaultForLists::testJackson628
    public void testJackson628() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        mapper.enableDefaultTyping(DefaultTyping.NON_FINAL);
        ArrayList<Foo> data = new ArrayList<Foo>();
        String json = mapper.writeValueAsString(data);
        List<?> output = mapper.readValue(json, List.class);
        assertTrue(output.isEmpty());
    }

// com.fasterxml.jackson.databind.jsontype.TestDefaultForLists::testJackson667
    public void testJackson667() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        mapper.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL,
                JsonTypeInfo.As.PROPERTY);
        String json = mapper.writeValueAsString(new SetBean("abc"));
        SetBean bean = mapper.readValue(json, SetBean.class);
        assertNotNull(bean);
        assertTrue(bean.names instanceof HashSet);
    }

// com.fasterxml.jackson.databind.jsontype.TestDefaultForMaps::testJackson428
    public void testJackson428() throws Exception
    {
        ObjectMapper serMapper = new ObjectMapper();

        TypeResolverBuilder<?> serializerTyper = new ObjectMapper.DefaultTypeResolverBuilder(ObjectMapper.DefaultTyping.NON_FINAL);
        serializerTyper = serializerTyper.init(JsonTypeInfo.Id.NAME, createTypeNameIdResolver(true));
        serializerTyper = serializerTyper.inclusion(JsonTypeInfo.As.PROPERTY);
        serMapper.setDefaultTyping(serializerTyper);

        
        MapHolder holder = new MapHolder();
        holder.map = new HashMap<MapKey,List<Object>>();
        List<Object> ints = new ArrayList<Object>();
        ints.add(Integer.valueOf(3));
        holder.map.put(new MapKey("key"), ints);
        String json = serMapper.writeValueAsString(holder);

        
        ObjectMapper deserMapper = new ObjectMapper();
        TypeResolverBuilder<?> deserializerTyper = new ObjectMapper.DefaultTypeResolverBuilder(ObjectMapper.DefaultTyping.NON_FINAL);
        deserializerTyper = deserializerTyper.init(JsonTypeInfo.Id.NAME, createTypeNameIdResolver(false));
        deserializerTyper = deserializerTyper.inclusion(JsonTypeInfo.As.PROPERTY);
        deserMapper.setDefaultTyping(deserializerTyper);

        MapHolder result = deserMapper.readValue(json, MapHolder.class);
        assertNotNull(result);
        Map<?,?> map = result.map;
        assertEquals(1, map.size());
        Map.Entry<?,?> entry = map.entrySet().iterator().next();
        Object key = entry.getKey();
        assertEquals(MapKey.class, key.getClass());
        Object value = entry.getValue();
        assertTrue(value instanceof List<?>);
        List<?> list = (List<?>) value;
        assertEquals(1, list.size());
        assertEquals(Integer.class, list.get(0).getClass());
        assertEquals(Integer.valueOf(3), list.get(0));
    }

// com.fasterxml.jackson.databind.jsontype.TestDefaultForObject::testBeanAsObject
    public void testBeanAsObject() throws Exception
    {
        ObjectMapper m = new ObjectMapper();
        m.enableDefaultTyping();
        
        String str = m.writeValueAsString(new Object[] { new StringBean("abc") });

        _verifySerializationAsMap(str);
        
        
        Object ob = m.readValue(str, Object[].class);
        assertNotNull(ob);
        Object[] result = (Object[]) ob;
        assertNotNull(result[0]);
        assertEquals(StringBean.class, result[0].getClass());
        assertEquals("abc", ((StringBean) result[0]).name);
    }

// com.fasterxml.jackson.databind.jsontype.TestDefaultForObject::testAbstractBean
    public void testAbstractBean() throws Exception
    {
        
        ObjectMapper m = new ObjectMapper();
        AbstractBean[] input = new AbstractBean[] { new StringBean("xyz") };
        String serial = m.writeValueAsString(input);
        try {
            m.readValue(serial, AbstractBean[].class);
            fail("Should have failed");
        } catch (JsonMappingException e) {
            
            verifyException(e, "can not construct");
        }
        
        
        m = new ObjectMapper();
        m.enableDefaultTyping(ObjectMapper.DefaultTyping.OBJECT_AND_NON_CONCRETE);
        serial = m.writeValueAsString(input);
        AbstractBean[] beans = m.readValue(serial, AbstractBean[].class);
        assertEquals(1, beans.length);
        assertEquals(StringBean.class, beans[0].getClass());
        assertEquals("xyz", ((StringBean) beans[0]).name);
    }

// com.fasterxml.jackson.databind.jsontype.TestDefaultForObject::testNonFinalBean
    public void testNonFinalBean() throws Exception
    {
        ObjectMapper m = new ObjectMapper();
        
        m.enableDefaultTyping(ObjectMapper.DefaultTyping.OBJECT_AND_NON_CONCRETE);
        StringBean bean = new StringBean("x");
        assertEquals("{\"name\":\"x\"}", m.writeValueAsString(bean));
        
        m = new ObjectMapper();
        m.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);
        assertEquals("[\""+StringBean.class.getName()+"\",{\"name\":\"x\"}]",
            m.writeValueAsString(bean));
    }

// com.fasterxml.jackson.databind.jsontype.TestDefaultForObject::testNullValue
    public void testNullValue() throws Exception
    {
        ObjectMapper m = new ObjectMapper();
        m.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);
        BeanHolder h = new BeanHolder();
        String json = m.writeValueAsString(h);
        assertNotNull(json);
        BeanHolder result = m.readValue(json, BeanHolder.class);
        assertNotNull(result);
        assertNull(result.bean);
    }

// com.fasterxml.jackson.databind.jsontype.TestDefaultForObject::testEnumAsObject
    public void testEnumAsObject() throws Exception
    {
        
        Object[] input = new Object[] { Choice.YES };
        Object[] input2 = new Object[] { ComplexChoice.MAYBE};
        
        assertEquals("[\"YES\"]", serializeAsString(input));
        assertEquals("[\"MAYBE\"]", serializeAsString(input2));

        
        ObjectMapper m = new ObjectMapper();
        m.enableDefaultTyping();

        String json = m.writeValueAsString(input);
        assertEquals("[[\""+Choice.class.getName()+"\",\"YES\"]]", json);

        
        Object[] output = m.readValue(json, Object[].class);
        assertEquals(1, output.length);
        assertEquals(Choice.YES, output[0]);

        
        json = m.writeValueAsString(input2);
        assertEquals("[[\""+ComplexChoice.class.getName()+"\",\"MAYBE\"]]", json);
        output = m.readValue(json, Object[].class);
        assertEquals(1, output.length);
        assertEquals(ComplexChoice.MAYBE, output[0]);
    }

// com.fasterxml.jackson.databind.jsontype.TestDefaultForObject::testEnumSet
    public void testEnumSet() throws Exception
    {
        EnumSet<Choice> set = EnumSet.of(Choice.NO);
        Object[] input = new Object[] { set };
        ObjectMapper m = new ObjectMapper();
        m.enableDefaultTyping();
        String json = m.writeValueAsString(input);
        Object[] output = m.readValue(json, Object[].class);
        assertEquals(1, output.length);
        Object ob = output[0];
        assertTrue(ob instanceof EnumSet<?>);
        EnumSet<Choice> set2 = (EnumSet<Choice>) ob;
        assertEquals(1, set2.size());
        assertTrue(set2.contains(Choice.NO));
        assertFalse(set2.contains(Choice.YES));
    }

// com.fasterxml.jackson.databind.jsontype.TestDefaultForObject::testEnumMap
    public void testEnumMap() throws Exception
    {
        EnumMap<Choice,String> map = new EnumMap<Choice,String>(Choice.class);
        map.put(Choice.NO, "maybe");
        Object[] input = new Object[] { map };
        ObjectMapper m = new ObjectMapper();
        m.enableDefaultTyping();
        String json = m.writeValueAsString(input);
        Object[] output = m.readValue(json, Object[].class);
        assertEquals(1, output.length);
        Object ob = output[0];
        assertTrue(ob instanceof EnumMap<?,?>);
        EnumMap<Choice,String> map2 = (EnumMap<Choice,String>) ob;
        assertEquals(1, map2.size());
        assertEquals("maybe", map2.get(Choice.NO));
        assertNull(map2.get(Choice.YES));
    }

// com.fasterxml.jackson.databind.jsontype.TestDefaultForObject::testJackson311
    public void testJackson311() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        mapper.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);
        String json = mapper.writeValueAsString(new PolymorphicType("hello", 2));
        PolymorphicType value = mapper.readValue(json, PolymorphicType.class);
        assertEquals("hello", value.foo);
        assertEquals(Integer.valueOf(2), value.bar);
    }

// com.fasterxml.jackson.databind.jsontype.TestDefaultForObject::testTokenBuffer
    public void testTokenBuffer() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        mapper.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);

        
        TokenBuffer buf = new TokenBuffer(mapper);
        buf.writeStartObject();
        buf.writeNumberField("num", 42);
        buf.writeEndObject();
        String json = mapper.writeValueAsString(new ObjectHolder(buf));
        ObjectHolder holder = mapper.readValue(json, ObjectHolder.class);
        assertNotNull(holder.value);
        assertSame(TokenBuffer.class, holder.value.getClass());
        JsonParser jp = ((TokenBuffer) holder.value).asParser();
        assertToken(JsonToken.START_OBJECT, jp.nextToken());
        assertToken(JsonToken.FIELD_NAME, jp.nextToken());
        assertToken(JsonToken.VALUE_NUMBER_INT, jp.nextToken());
        assertToken(JsonToken.END_OBJECT, jp.nextToken());
        assertNull(jp.nextToken());
        jp.close();

        
        buf = new TokenBuffer(mapper);
        buf.writeStartArray();
        buf.writeBoolean(true);
        buf.writeEndArray();
        json = mapper.writeValueAsString(new ObjectHolder(buf));
        holder = mapper.readValue(json, ObjectHolder.class);
        assertNotNull(holder.value);
        assertSame(TokenBuffer.class, holder.value.getClass());
        jp = ((TokenBuffer) holder.value).asParser();
        assertToken(JsonToken.START_ARRAY, jp.nextToken());
        assertToken(JsonToken.VALUE_TRUE, jp.nextToken());
        assertToken(JsonToken.END_ARRAY, jp.nextToken());
        assertNull(jp.nextToken());
        jp.close();

        
        buf = new TokenBuffer(mapper);
        buf.writeNumber(321);
        json = mapper.writeValueAsString(new ObjectHolder(buf));
        holder = mapper.readValue(json, ObjectHolder.class);
        assertNotNull(holder.value);
        assertSame(TokenBuffer.class, holder.value.getClass());
        jp = ((TokenBuffer) holder.value).asParser();
        assertToken(JsonToken.VALUE_NUMBER_INT, jp.nextToken());
        assertEquals(321, jp.getIntValue());
        assertNull(jp.nextToken());
        jp.close();
    }

// com.fasterxml.jackson.databind.jsontype.TestDefaultForObject::testIssue352
    public void testIssue352() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        mapper.enableDefaultTyping (ObjectMapper.DefaultTyping.OBJECT_AND_NON_CONCRETE, JsonTypeInfo.As.PROPERTY);
        DiscussBean d1 = new DiscussBean();
        d1.subject = "mouse";
        d1.weight=88;
        DomainBeanWrapper wrapper = new DomainBeanWrapper();
        wrapper.name = "mickey";
        wrapper.myBean = d1;
        String json = mapper.writeValueAsString(wrapper);
        DomainBeanWrapper result = mapper.readValue(json, DomainBeanWrapper.class);
        assertNotNull(result);
        assertNotNull(wrapper.myBean);
        assertSame(DiscussBean.class, wrapper.myBean.getClass());
    }

// com.fasterxml.jackson.databind.jsontype.TestDefaultForObject::testFeature432
    public void testFeature432() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        mapper.enableDefaultTypingAsProperty(ObjectMapper.DefaultTyping.OBJECT_AND_NON_CONCRETE, "*CLASS*");
        String json = mapper.writeValueAsString(new BeanHolder(new StringBean("punny")));
        assertEquals("{\"bean\":{\"*CLASS*\":\"com.fasterxml.jackson.databind.jsontype.TestDefaultForObject$StringBean\",\"name\":\"punny\"}}", json);
    }

// com.fasterxml.jackson.databind.jsontype.TestDefaultForScalars::testNumericScalars
    public void testNumericScalars() throws Exception
    {
        ObjectMapper m = new ObjectMapper();
        m.enableDefaultTyping();

        
        assertEquals("[123]", m.writeValueAsString(new Object[] { Integer.valueOf(123) }));
        assertEquals("[[\"java.lang.Long\",37]]", m.writeValueAsString(new Object[] { Long.valueOf(37) }));
        assertEquals("[0.25]", m.writeValueAsString(new Object[] { Double.valueOf(0.25) }));
        assertEquals("[[\"java.lang.Float\",0.5]]", m.writeValueAsString(new Object[] { Float.valueOf(0.5f) }));
    }

// com.fasterxml.jackson.databind.jsontype.TestDefaultForScalars::testDateScalars
    public void testDateScalars() throws Exception
    {
        ObjectMapper m = new ObjectMapper();
        m.enableDefaultTyping();

        long ts = 12345678L;
        assertEquals("[[\"java.util.Date\","+ts+"]]",
                m.writeValueAsString(new Object[] { new Date(ts) }));

        
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(ts);
        String json = m.writeValueAsString(new Object[] { c });
        assertEquals("[[\""+c.getClass().getName()+"\","+ts+"]]", json);
        
        Object[] result = m.readValue(json, Object[].class);
        assertEquals(1, result.length);
        assertTrue(result[0] instanceof Calendar);
        assertEquals(ts, ((Calendar) result[0]).getTimeInMillis());
    }

// com.fasterxml.jackson.databind.jsontype.TestDefaultForScalars::testMiscScalars
    public void testMiscScalars() throws Exception
    {
        ObjectMapper m = new ObjectMapper();
        m.enableDefaultTyping();

        
        assertEquals("[\"abc\"]", m.writeValueAsString(new Object[] { "abc" }));
        assertEquals("[true,null,false]", m.writeValueAsString(new Boolean[] { true, null, false }));
    }

// com.fasterxml.jackson.databind.jsontype.TestDefaultForScalars::testScalarArrays
    public void testScalarArrays() throws Exception
    {
        ObjectMapper m = new ObjectMapper();
        m.enableDefaultTyping(ObjectMapper.DefaultTyping.JAVA_LANG_OBJECT);
        Object[] input = new Object[] {
                "abc", new Date(1234567), null, Integer.valueOf(456)
        };
        String json = m.writeValueAsString(input);
        assertEquals("[\"abc\",[\"java.util.Date\",1234567],null,456]", json);

        
        Object[] output = m.readValue(json, Object[].class);
        assertArrayEquals(input, output);
    }

// com.fasterxml.jackson.databind.jsontype.TestDefaultForScalars::test417
    public void test417() throws Exception
    {
        ObjectMapper m = new ObjectMapper();
        m.enableDefaultTyping();
        Jackson417Bean input = new Jackson417Bean();
        String json = m.writeValueAsString(input);
        Jackson417Bean result = m.readValue(json, Jackson417Bean.class);
        assertEquals(input.foo, result.foo);
        assertEquals(input.bar, result.bar);
    }

// com.fasterxml.jackson.databind.jsontype.TestDefaultWithCreators::testWithCreators
    public void testWithCreators() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        mapper.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);
        UrlJob input = new UrlJob(123L, "http://foo", 3);
        String json = mapper.writeValueAsString(input);
        assertNotNull(json);
        Job output = mapper.readValue(json, Job.class);
        assertNotNull(output);
        assertSame(UrlJob.class, output.getClass());
        UrlJob o2 = (UrlJob) output;
        assertEquals(123L, o2.id);
        assertEquals("http://foo", o2.getUrl());
        assertEquals(3, o2.getCount());
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

// com.fasterxml.jackson.databind.jsontype.TestExternalId::testSimpleSerialization
    public void testSimpleSerialization() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerSubtypes(ValueBean.class);
        
        
        
        
        
        
        assertEquals("{\"bean\":{\"value\":11},\"extType\":\"vbean\"}",
                mapper.writeValueAsString(new ExternalBean(11)));
    }

// com.fasterxml.jackson.databind.jsontype.TestExternalId::testImproperExternalIdSerialization
    public void testImproperExternalIdSerialization() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        assertEquals("{\"extType\":\"funk\",\"i\":3}",
                mapper.writeValueAsString(new FunkyExternalBean()));
    }

// com.fasterxml.jackson.databind.jsontype.TestExternalId::testSimpleDeserialization
    public void testSimpleDeserialization() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerSubtypes(ValueBean.class);
        ExternalBean result = mapper.readValue("{\"bean\":{\"value\":11},\"extType\":\"vbean\"}", ExternalBean.class);
        assertNotNull(result);
        assertNotNull(result.bean);
        ValueBean vb = (ValueBean) result.bean;
        assertEquals(11, vb.value);
    }

// com.fasterxml.jackson.databind.jsontype.TestExternalId::testMultipleTypeIdsDeserialization
    public void testMultipleTypeIdsDeserialization() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerSubtypes(ValueBean.class);
        String json = mapper.writeValueAsString(new ExternalBean3(3));
        ExternalBean3 result = mapper.readValue(json, ExternalBean3.class);
        assertNotNull(result);
        assertNotNull(result.value1);
        assertNotNull(result.value2);
        assertNotNull(result.value3);
        assertEquals(3, ((ValueBean)result.value1).value);
        assertEquals(4, ((ValueBean)result.value2).value);
        assertEquals(5, ((ValueBean)result.value3).value);
        assertEquals(3, result.foo);
    }

// com.fasterxml.jackson.databind.jsontype.TestExternalId::testExternalTypeWithCreator
    public void testExternalTypeWithCreator() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerSubtypes(ValueBean.class);
        String json = mapper.writeValueAsString(new ExternalBeanWithCreator(7));
        ExternalBeanWithCreator result = mapper.readValue(json, ExternalBeanWithCreator.class);
        assertNotNull(result);
        assertNotNull(result.value);
        assertEquals(7, ((ValueBean)result.value).value);
        assertEquals(7, result.foo);
    }

// com.fasterxml.jackson.databind.jsontype.TestExternalId::testImproperExternalIdDeserialization
    public void testImproperExternalIdDeserialization() throws Exception
    {
        FunkyExternalBean result = MAPPER.readValue("{\"extType\":\"funk\",\"i\":3}",
                FunkyExternalBean.class);
        assertNotNull(result);
        assertEquals(3, result.i);
    }

// com.fasterxml.jackson.databind.jsontype.TestExternalId::testIssue798
    public void testIssue798() throws Exception
    {
        Base base = new Derived1("derived1 prop val", "base prop val");
        BaseContainer baseContainer = new BaseContainer("bc prop val", base);
        String generatedJson = MAPPER.writeValueAsString(baseContainer);
        BaseContainer baseContainer2 = MAPPER.readValue(generatedJson,BaseContainer.class);
        assertEquals("bc prop val", baseContainer.getBaseContainerProperty());

        Base b = baseContainer2.getBase();
        assertNotNull(b);
        if (b.getClass() != Derived1.class) {
            fail("Should have type Derived1, was "+b.getClass().getName());
        }

        Derived1 derived1 = (Derived1) b;
        assertEquals("base prop val", derived1.getBaseProperty());
        assertEquals("derived1 prop val", derived1.getDerived1Property());
    }

// com.fasterxml.jackson.databind.jsontype.TestExternalId::testIssue831
    public void testIssue831() throws Exception
    {
        final String JSON = "{ \"petType\": \"dog\",\n"
                +"\"pet\": { \"name\": \"Pluto\" }\n}";
        House831 result = MAPPER.readValue(JSON, House831.class);
        assertNotNull(result);
        assertNotNull(result.pet);
        assertSame(Dog.class, result.pet.getClass());
        assertEquals("dog", result.petType);
    }

// com.fasterxml.jackson.databind.jsontype.TestExternalId::testWithScalar118
    public void testWithScalar118() throws Exception
    {
        ExternalTypeWithNonPOJO input = new ExternalTypeWithNonPOJO(new java.util.Date(123L));
        String json = MAPPER.writeValueAsString(input);
        assertNotNull(json);

        
        ExternalTypeWithNonPOJO result = MAPPER.readValue(json, ExternalTypeWithNonPOJO.class);
        assertNotNull(result.value);
        assertTrue(result.value instanceof java.util.Date);
    }

// com.fasterxml.jackson.databind.jsontype.TestExternalId::testWithNaturalScalar118
    public void testWithNaturalScalar118() throws Exception
    {
        ExternalTypeWithNonPOJO input = new ExternalTypeWithNonPOJO(Integer.valueOf(13));
        String json = MAPPER.writeValueAsString(input);
        assertNotNull(json);
        
        ExternalTypeWithNonPOJO result = MAPPER.readValue(json, ExternalTypeWithNonPOJO.class);
        assertNotNull(result.value);
        assertTrue(result.value instanceof Integer);

        
        input = new ExternalTypeWithNonPOJO(Boolean.TRUE);
        json = MAPPER.writeValueAsString(input);
        assertNotNull(json);
        result = MAPPER.readValue(json, ExternalTypeWithNonPOJO.class);
        assertNotNull(result.value);
        assertTrue(result.value instanceof Boolean);

        input = new ExternalTypeWithNonPOJO("foobar");
        json = MAPPER.writeValueAsString(input);
        assertNotNull(json);
        result = MAPPER.readValue(json, ExternalTypeWithNonPOJO.class);
        assertNotNull(result.value);
        assertTrue(result.value instanceof String);
        assertEquals("foobar", result.value);
    }

// com.fasterxml.jackson.databind.jsontype.TestExternalId::testWithAsValue
    public void testWithAsValue() throws Exception
    {
        ExternalTypeWithNonPOJO input = new ExternalTypeWithNonPOJO(new AsValueThingy(12345L));
        String json = MAPPER.writeValueAsString(input);
        assertNotNull(json);
        assertEquals("{\"value\":12345,\"type\":\"date\"}", json);

        
        ExternalTypeWithNonPOJO result = MAPPER.readValue(json, ExternalTypeWithNonPOJO.class);
        assertNotNull(result);
        assertNotNull(result.value);
        
        
        assertEquals(Date.class, result.value.getClass());
        assertEquals(12345L, ((Date) result.value).getTime());
    }

// com.fasterxml.jackson.databind.jsontype.TestGenericListSerialization::testSubTypesFor356
    public void testSubTypesFor356() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        
        Version v = mapper.version();
        if (v.getMajorVersion() == 1 && v.getMinorVersion() == 6) {
            System.err.println("Note: skipping test for Jackson 1.6");
            return;
        }
        
        JSONResponse<List<Parent>> input = new JSONResponse<List<Parent>>();

        List<Parent> embedded = new ArrayList<Parent>();
        embedded.add(new Child1());
        embedded.add(new Child2());
        input.setResult(embedded);
        mapper.configure(MapperFeature.USE_STATIC_TYPING, true);

        JavaType rootType = TypeFactory.defaultInstance().constructType(new TypeReference<JSONResponse<List<Parent>>>() { });
        byte[] json = mapper.writerWithType(rootType).writeValueAsBytes(input);

        
        JSONResponse<List<Parent>> out = mapper.readValue(json, 0, json.length, rootType);

        List<Parent> deserializedContent = (List<Parent>) out.getResult();

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

// com.fasterxml.jackson.databind.jsontype.TestNoTypeInfo::testWithIdNone
    public void testWithIdNone() throws Exception
    {
        final ObjectMapper mapper = new ObjectMapper();
        mapper.enableDefaultTyping();
        
        String json = mapper.writeValueAsString(new NoType());
        assertEquals("{\"a\":3}", json);

        
        NoTypeInterface bean = mapper.readValue("{\"a\":6}", NoTypeInterface.class);
        assertNotNull(bean);
        NoType impl = (NoType) bean;
        assertEquals(6, impl.a);
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
        list.add(new MethodWrapperBean(new BooleanWrapper(true)));
        list.add(new MethodWrapperBean(new StringWrapper("x")));
        list.add(new MethodWrapperBean(new OtherBean()));
        String json = mapper.writeValueAsString(list);
        MethodWrapperBeanList result = mapper.readValue(json, MethodWrapperBeanList.class);
        assertNotNull(result);
        assertEquals(3, result.size());
        MethodWrapperBean bean = result.get(0);
        assertEquals(BooleanWrapper.class, bean.value.getClass());
        assertEquals(((BooleanWrapper) bean.value).b, Boolean.TRUE);
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
                FieldWrapperBean[] { new FieldWrapperBean(new BooleanWrapper(true)) });
        String json = mapper.writeValueAsString(array);
        FieldWrapperBeanArray result = mapper.readValue(json, FieldWrapperBeanArray.class);
        assertNotNull(result);
        FieldWrapperBean[] beans = result.beans;
        assertEquals(1, beans.length);
        FieldWrapperBean bean = beans[0];
        assertEquals(BooleanWrapper.class, bean.value.getClass());
        assertEquals(((BooleanWrapper) bean.value).b, Boolean.TRUE);
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
        map.put("xyz", new MethodWrapperBean(new BooleanWrapper(true)));
        String json = mapper.writeValueAsString(map);
        MethodWrapperBeanMap result = mapper.readValue(json, MethodWrapperBeanMap.class);
        assertNotNull(result);
        assertEquals(1, result.size());
        MethodWrapperBean bean = result.get("xyz");
        assertNotNull(bean);
        Object ob = bean.value;
        assertEquals(BooleanWrapper.class, ob.getClass());
        assertEquals(((BooleanWrapper) ob).b, Boolean.TRUE);
    }

// com.fasterxml.jackson.databind.jsontype.TestScalars::testScalarsWithTyping
    public void testScalarsWithTyping() throws Exception
    {
        ObjectMapper m = new ObjectMapper();
        String json;
        DynamicWrapper result;

        
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
        ObjectMapper m = new ObjectMapper();
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
    }

// com.fasterxml.jackson.databind.jsontype.TestSubtypes::testSerialization
    public void testSerialization() throws Exception
    {
        
        SubB bean = new SubB();
        ObjectMapper mapper = new ObjectMapper();
        assertEquals("{\"@type\":\"TypeB\",\"b\":1}", mapper.writeValueAsString(bean));

        
        mapper = new ObjectMapper();
        mapper.registerSubtypes(new NamedType(SubB.class, "typeB"));
        assertEquals("{\"@type\":\"typeB\",\"b\":1}", mapper.writeValueAsString(bean));

        
        assertEquals("{\"@type\":\"TestSubtypes$SubD\",\"d\":0}", mapper.writeValueAsString(new SubD()));  
    }

// com.fasterxml.jackson.databind.jsontype.TestSubtypes::testDeserializationNonNamed
    public void testDeserializationNonNamed() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerSubtypes(SubC.class);

        
        SuperType bean = mapper.readValue("{\"@type\":\"TestSubtypes$SubC\", \"c\":1}", SuperType.class);
        assertSame(SubC.class, bean.getClass());
        assertEquals(1, ((SubC) bean).c);
    }

// com.fasterxml.jackson.databind.jsontype.TestSubtypes::testDeserializatioNamed
    public void testDeserializatioNamed() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerSubtypes(SubB.class);
        mapper.registerSubtypes(new NamedType(SubD.class, "TypeD"));

        SuperType bean = mapper.readValue("{\"@type\":\"TypeB\", \"b\":13}", SuperType.class);
        assertSame(SubB.class, bean.getClass());
        assertEquals(13, ((SubB) bean).b);

        
        bean = mapper.readValue("{\"@type\":\"TypeD\", \"d\":-4}", SuperType.class);
        assertSame(SubD.class, bean.getClass());
        assertEquals(-4, ((SubD) bean).d);
    }

// com.fasterxml.jackson.databind.jsontype.TestSubtypes::testEmptyBean
    public void testEmptyBean() throws Exception
    {
        
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, true);
        String json = mapper.writeValueAsString(new EmptyBean());
        assertEquals("{\"@type\":\"TestSubtypes$EmptyBean\"}", json);

        mapper = new ObjectMapper();
        mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        json = mapper.writeValueAsString(new EmptyBean());
        assertEquals("{\"@type\":\"TestSubtypes$EmptyBean\"}", json);

        
        mapper = new ObjectMapper();
        mapper.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);
        mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        json = mapper.writeValueAsString(new EmptyNonFinal());
        assertEquals("[\"com.fasterxml.jackson.databind.jsontype.TestSubtypes$EmptyNonFinal\",{}]", json);
    }

// com.fasterxml.jackson.databind.jsontype.TestSubtypes::testDefaultImpl
    public void testDefaultImpl() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        
        SuperTypeWithDefault bean = mapper.readValue("{\"a\":13}", SuperTypeWithDefault.class);
        assertEquals(DefaultImpl.class, bean.getClass());
        assertEquals(13, ((DefaultImpl) bean).a);

        
        bean = mapper.readValue("{\"a\":14,\"#type\":\"foobar\"}", SuperTypeWithDefault.class);
        assertEquals(DefaultImpl.class, bean.getClass());
        assertEquals(14, ((DefaultImpl) bean).a);

        bean = mapper.readValue("{\"#type\":\"foobar\",\"a\":15}", SuperTypeWithDefault.class);
        assertEquals(DefaultImpl.class, bean.getClass());
        assertEquals(15, ((DefaultImpl) bean).a);

        bean = mapper.readValue("{\"#type\":\"foobar\"}", SuperTypeWithDefault.class);
        assertEquals(DefaultImpl.class, bean.getClass());
        assertEquals(0, ((DefaultImpl) bean).a);
    }

// com.fasterxml.jackson.databind.jsontype.TestSubtypes::testDefaultImplViaModule
    public void testDefaultImplViaModule() throws Exception
    {
        final String JSON = "{\"a\":123}";
        
        
        ObjectMapper mapper = new ObjectMapper();
        try {
            mapper.readValue(JSON, SuperTypeWithoutDefault.class);
            fail("Expected an exception");
        } catch (JsonMappingException e) {
            verifyException(e, "missing property");
        }

        
        mapper = new ObjectMapper();
        SimpleModule module = new SimpleModule("test", Version.unknownVersion());
        module.addAbstractTypeMapping(SuperTypeWithoutDefault.class, DefaultImpl505.class);
        mapper.registerModule(module);
        SuperTypeWithoutDefault bean = mapper.readValue(JSON, SuperTypeWithoutDefault.class);
        assertNotNull(bean);
        assertEquals(DefaultImpl505.class, bean.getClass());
        assertEquals(123, ((DefaultImpl505) bean).a);

        bean = mapper.readValue("{\"#type\":\"foobar\"}", SuperTypeWithoutDefault.class);
        assertEquals(DefaultImpl505.class, bean.getClass());
        assertEquals(0, ((DefaultImpl505) bean).a);
    
    }

// com.fasterxml.jackson.databind.jsontype.TestTypeNames::testSerialization
    public void testSerialization() throws Exception
    {
        ObjectMapper m = new ObjectMapper();

        
        
        
        
        assertEquals("[{\"doggy\":{\"name\":\"Spot\",\"ageInYears\":3}}]",
                m.writeValueAsString(new Animal[] { new Dog("Spot", 3) }));
        assertEquals("[{\"MaineCoon\":{\"name\":\"Belzebub\",\"purrs\":true}}]",
                m.writeValueAsString(new Animal[] { new MaineCoon("Belzebub", true)}));
    }

// com.fasterxml.jackson.databind.jsontype.TestTypeNames::testRoundTrip
    public void testRoundTrip() throws Exception
    {
        ObjectMapper m = new ObjectMapper();
        Animal[] input = new Animal[] {
                new Dog("Odie", 7),
                null,
                new MaineCoon("Piru", false),
                new Persian("Khomeini", true)
        };
        String json = m.writeValueAsString(input);
        List<Animal> output = m.readValue(json,
                TypeFactory.defaultInstance().constructCollectionType(ArrayList.class, Animal.class));
        assertEquals(input.length, output.size());
        for (int i = 0, len = input.length; i < len; ++i) {
            assertEquals("Entry #"+i+" differs, input = '"+json+"'",
                input[i], output.get(i));
        }
    }

// com.fasterxml.jackson.databind.jsontype.TestTypeNames::testRoundTripMap
    public void testRoundTripMap() throws Exception
    {
        ObjectMapper m = new ObjectMapper();
        AnimalMap input = new AnimalMap();
        input.put("venla", new MaineCoon("Venla", true));
        input.put("ama", new Dog("Amadeus", 13));
        String json = m.writeValueAsString(input);
        AnimalMap output = m.readValue(json, AnimalMap.class);
        assertEquals(input, output);
    }

// com.fasterxml.jackson.databind.jsontype.TestTypedArraySerialization::testListWithPolymorphic
    public void testListWithPolymorphic() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        BeanListWrapper beans = new BeanListWrapper();
        assertEquals("{\"beans\":[{\"@type\":\"bean\",\"x\":0}]}", mapper.writeValueAsString(beans));
        
        ObjectWriter w = mapper.writerWithView(Object.class);
        assertEquals("{\"beans\":[{\"@type\":\"bean\",\"x\":0}]}", w.writeValueAsString(beans));
    }

// com.fasterxml.jackson.databind.jsontype.TestTypedArraySerialization::testIntList
    public void testIntList() throws Exception
    {
        TypedList<Integer> input = new TypedList<Integer>();
        input.add(5);
        input.add(13);
        
        assertEquals("[\""+TypedList.class.getName()+"\",[5,13]]", serializeAsString(input));
    }

// com.fasterxml.jackson.databind.jsontype.TestTypedArraySerialization::testStringListAsProp
    public void testStringListAsProp() throws Exception
    {
        TypedListAsProp<String> input = new TypedListAsProp<String>();
        input.add("a");
        input.add("b");
        assertEquals("[\""+TypedListAsProp.class.getName()+"\",[\"a\",\"b\"]]",
                serializeAsString(input));
    }

// com.fasterxml.jackson.databind.jsontype.TestTypedArraySerialization::testStringListAsObjectWrapper
    public void testStringListAsObjectWrapper() throws Exception
    {
        TypedListAsWrapper<Boolean> input = new TypedListAsWrapper<Boolean>();
        input.add(true);
        input.add(null);
        input.add(false);
        
        
        
        String expName = "TestTypedArraySerialization$TypedListAsWrapper";
        assertEquals("{\""+expName+"\":[true,null,false]}",
                serializeAsString(input));
    }

// com.fasterxml.jackson.databind.jsontype.TestTypedArraySerialization::testIntArray
    public void testIntArray() throws Exception
    {
        ObjectMapper m = new ObjectMapper();
        m.addMixInAnnotations(int[].class, WrapperMixIn.class);
        int[] input = new int[] { 1, 2, 3 };
        String clsName = int[].class.getName();
        assertEquals("{\""+clsName+"\":[1,2,3]}", serializeAsString(m, input));
    }

// com.fasterxml.jackson.databind.jsontype.TestTypedArraySerialization::testGenericArray
    public void testGenericArray() throws Exception
    {
        ObjectMapper m;
        final A[] input = new A[] { new B() };
        final String EXP = "[{\"BB\":{\"value\":2}}]";

        
        m = new ObjectMapper();
        assertEquals(EXP, m.writeValueAsString(input));

        
        m = new ObjectMapper();
        m.configure(MapperFeature.USE_STATIC_TYPING, true);
        assertEquals(EXP, m.writeValueAsString(input));
    }

// com.fasterxml.jackson.databind.jsontype.TestTypedContainerSerialization::testIssue265
    public void testIssue265() throws Exception
    {
		Dog dog = new Dog("medor");
		dog.setBoneCount(3);
		Container1 c1 = new Container1();
		c1.setAnimal(dog);
		String s1 = mapper.writeValueAsString(c1);
		Assert.assertTrue("polymorphic type info is kept (1)", s1
				.indexOf("\"object-type\":\"doggy\"") >= 0);
		Container2<Animal> c2 = new Container2<Animal>();
		c2.setAnimal(dog);
		String s2 = mapper.writeValueAsString(c2);
		Assert.assertTrue("polymorphic type info is kept (2)", s2
				.indexOf("\"object-type\":\"doggy\"") >= 0);
    }

// com.fasterxml.jackson.databind.jsontype.TestTypedContainerSerialization::testIssue329
    public void testIssue329() throws Exception
    {
            ArrayList<Animal> animals = new ArrayList<Animal>();
            animals.add(new Dog("Spot"));
            JavaType rootType = TypeFactory.defaultInstance().constructParametricType(Iterator.class, Animal.class);
            String json = mapper.writerWithType(rootType).writeValueAsString(animals.iterator());
            if (json.indexOf("\"object-type\":\"doggy\"") < 0) {
                fail("No polymorphic type retained, should be; JSON = '"+json+"'");
            }
    }

// com.fasterxml.jackson.databind.jsontype.TestTypedContainerSerialization::testIssue508
    public void testIssue508() throws Exception
    {
            List<List<Issue508A>> l = new ArrayList<List<Issue508A>>();
            List<Issue508A> l2 = new ArrayList<Issue508A>();
            l2.add(new Issue508A());
            l.add(l2);
            TypeReference<?> typeRef = new TypeReference<List<List<Issue508A>>>() {};
            String json = mapper.writerWithType(typeRef).writeValueAsString(l);

            List<?> output = mapper.readValue(json, typeRef);
            assertEquals(1, output.size());
            Object ob = output.get(0);
            assertTrue(ob instanceof List<?>);
            List<?> list2 = (List<?>) ob;
            assertEquals(1, list2.size());
            ob = list2.get(0);
            assertSame(Issue508A.class, ob.getClass());
    }

// com.fasterxml.jackson.databind.jsontype.TestTypedDeserialization::testSimpleClassAsProperty
    public void testSimpleClassAsProperty() throws Exception
    {
        ObjectMapper m = new ObjectMapper();
        Animal a = m.readValue(asJSONObjectValueString("@classy", Cat.class.getName(),
                "furColor", "tabby", "name", "Garfield"), Animal.class);
        assertNotNull(a);
        assertEquals(Cat.class, a.getClass());
        Cat c = (Cat) a;
        assertEquals("Garfield", c.name);
        assertEquals("tabby", c.furColor);
    }

// com.fasterxml.jackson.databind.jsontype.TestTypedDeserialization::testTypeAsWrapper
    public void testTypeAsWrapper() throws Exception
    {
        ObjectMapper m = new ObjectMapper();
        m.addMixInAnnotations(Animal.class, TypeWithWrapper.class);
        String JSON = "{\".TestTypedDeserialization$Dog\" : "
            +asJSONObjectValueString(m, "name", "Scooby", "boneCount", "6")+" }";
        Animal a = m.readValue(JSON, Animal.class);
        assertTrue(a instanceof Animal);
        assertEquals(Dog.class, a.getClass());
        Dog d = (Dog) a;
        assertEquals("Scooby", d.name);
        assertEquals(6, d.boneCount);
    }

// com.fasterxml.jackson.databind.jsontype.TestTypedDeserialization::testTypeAsArray
    public void testTypeAsArray() throws Exception
    {
        ObjectMapper m = new ObjectMapper();
        m.addMixInAnnotations(Animal.class, TypeWithArray.class);
        
        String JSON = "[\""+Dog.class.getName()+"\", "
            +asJSONObjectValueString(m, "name", "Martti", "boneCount", "11")+" ]";
        Animal a = m.readValue(JSON, Animal.class);
        assertEquals(Dog.class, a.getClass());
        Dog d = (Dog) a;
        assertEquals("Martti", d.name);
        assertEquals(11, d.boneCount);
    }

// com.fasterxml.jackson.databind.jsontype.TestTypedDeserialization::testListAsArray
    public void testListAsArray() throws Exception
    {
        ObjectMapper m = new ObjectMapper();
        
        String JSON = "[\n"
            +asJSONObjectValueString(m, "@classy", Cat.class.getName(), "name", "Hello", "furColor", "white")
            +",\n"
            
            +asJSONObjectValueString(m,
                                     "boneCount", Integer.valueOf(1),
                                     "@classy", Dog.class.getName(),
                                     "name", "Bob"
                                     )
            +",\n"
            +asJSONObjectValueString(m, "@classy", Fish.class.getName())
            +", null\n]";
        
        JavaType expType = TypeFactory.defaultInstance().constructCollectionType(ArrayList.class, Animal.class);
        List<Animal> animals = m.readValue(JSON, expType);
        assertNotNull(animals);
        assertEquals(4, animals.size());
        Cat c = (Cat) animals.get(0);
        assertEquals("Hello", c.name);
        assertEquals("white", c.furColor);
        Dog d = (Dog) animals.get(1);
        assertEquals("Bob", d.name);
        assertEquals(1, d.boneCount);
        Fish f = (Fish) animals.get(2);
        assertNotNull(f);
        assertNull(animals.get(3));
    }

// com.fasterxml.jackson.databind.jsontype.TestTypedDeserialization::testCagedAnimal
    public void testCagedAnimal() throws Exception
    {
        ObjectMapper m = new ObjectMapper();
        String jsonCat = asJSONObjectValueString(m, "@classy", Cat.class.getName(), "name", "Nilson", "furColor", "black");
        String JSON = "{\"animal\":"+jsonCat+"}";

        AnimalContainer cont = m.readValue(JSON, AnimalContainer.class);
        assertNotNull(cont);
        Animal a = cont.animal;
        assertNotNull(a);
        Cat c = (Cat) a;
        assertEquals("Nilson", c.name);
        assertEquals("black", c.furColor);
    }

// com.fasterxml.jackson.databind.jsontype.TestTypedDeserialization::testAbstractEmptyBaseClass
    public void testAbstractEmptyBaseClass() throws Exception
    {
        DummyBase result = new ObjectMapper().readValue(
                "[\""+DummyImpl.class.getName()+"\",{\"x\":3}]", DummyBase.class);
        assertNotNull(result);
        assertEquals(DummyImpl.class, result.getClass());
        assertEquals(3, ((DummyImpl) result).x);
    }

// com.fasterxml.jackson.databind.jsontype.TestTypedDeserialization::testIssue506WithDate
    public void testIssue506WithDate() throws Exception
    {
        Issue506DateBean input = new Issue506DateBean();
        input.date = new Date(1234L);

        ObjectMapper mapper = new ObjectMapper();
        String json = mapper.writeValueAsString(input);

        Issue506DateBean output = mapper.readValue(json, Issue506DateBean.class);
        assertEquals(input.date, output.date);
    }

// com.fasterxml.jackson.databind.jsontype.TestTypedDeserialization::testIssue506WithNumber
    public void testIssue506WithNumber() throws Exception
    {
        Issue506NumberBean input = new Issue506NumberBean();
        input.number = Long.valueOf(4567L);

        ObjectMapper mapper = new ObjectMapper();
        String json = mapper.writeValueAsString(input);

        Issue506NumberBean output = mapper.readValue(json, Issue506NumberBean.class);
        assertEquals(input.number, output.number);
    }

// buggy code
    public void writeObject(Object value) throws IOException
    {
            _append(JsonToken.VALUE_EMBEDDED_OBJECT, value);
            /* 28-May-2014, tatu: Tricky choice here; if no codec, should we
             *   err out, or just embed? For now, do latter.
             */
//          throw new JsonMappingException("No ObjectCodec configured for TokenBuffer, writeObject() called");
    }

    public void writeTree(TreeNode node) throws IOException
    {

            // as with 'writeObject()', is codec optional?
            _append(JsonToken.VALUE_EMBEDDED_OBJECT, node);
    }

// relevant test
// com.fasterxml.jackson.databind.deser.TestEnumDeserialization::testAllowUnknownEnumValuesForEnumSets
    public void testAllowUnknownEnumValuesForEnumSets() throws Exception
    {
        ObjectReader reader = MAPPER.reader(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_AS_NULL);
        EnumSet<TestEnum> result = reader.withType(new TypeReference<EnumSet<TestEnum>>() { })
                .readValue("[\"NO-SUCH-VALUE\"]");
        assertEquals(0, result.size());
    }

// com.fasterxml.jackson.databind.deser.TestEnumDeserialization::testAllowUnknownEnumValuesAsMapKeysReadAsNull
    public void testAllowUnknownEnumValuesAsMapKeysReadAsNull() throws Exception
    {
        ObjectReader reader = MAPPER.reader(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_AS_NULL);
        ClassWithEnumMapKey result = reader.withType(ClassWithEnumMapKey.class)
                .readValue("{\"map\":{\"NO-SUCH-VALUE\":\"val\"}}");
        assertTrue(result.map.containsKey(null));
    }

// com.fasterxml.jackson.databind.deser.TestEnumDeserialization::testDoNotAllowUnknownEnumValuesAsMapKeysWhenReadAsNullDisabled
    public void testDoNotAllowUnknownEnumValuesAsMapKeysWhenReadAsNullDisabled() throws Exception
    {
        assertFalse(MAPPER.isEnabled(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_AS_NULL));
         try {
             MAPPER.readValue("{\"map\":{\"NO-SUCH-VALUE\":\"val\"}}", ClassWithEnumMapKey.class);
             fail("Expected an exception for bogus enum value...");
         } catch (JsonMappingException jex) {
             verifyException(jex, "Can not construct Map key");
         }
    }

// com.fasterxml.jackson.databind.deser.TestEnumDeserialization::testEnumsFromInts
    public void testEnumsFromInts() throws Exception
    {
        TestEnumFor834 res = MAPPER.readValue("1 ", TestEnumFor834.class);
        assertSame(TestEnumFor834.ENUM_A, res);
    }

// com.fasterxml.jackson.databind.deser.TestEnumDeserialization::testEnumsWithEmpty
    public void testEnumsWithEmpty() throws Exception
    {
       final ObjectMapper mapper = new ObjectMapper();
       mapper.configure(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, true);
       TestEnum result = mapper.readValue("\"\"", TestEnum.class);
       assertNull(result);
    }

// com.fasterxml.jackson.databind.deser.TestEnumDeserialization::testGenericEnumDeserialization
    public void testGenericEnumDeserialization() throws Exception
    {
       final ObjectMapper mapper = new ObjectMapper();
       SimpleModule module = new SimpleModule("foobar");
       module.addDeserializer(Enum.class, new LcEnumDeserializer());
       mapper.registerModule(module);
       
       assertEquals(TestEnum.JACKSON, mapper.readValue(quote("jackson"), TestEnum.class));
    }

// com.fasterxml.jackson.databind.deser.TestEnumDeserialization::testExceptionFromCreator
    public void testExceptionFromCreator() throws Exception
    {
        try {
             MAPPER.readValue(quote("xyz"), TestEnum324.class);
            fail("Should throw exception");
        } catch (JsonMappingException e) {
            verifyException(e, "foobar");
        }
    }

// com.fasterxml.jackson.databind.deser.TestEnumDeserialization::testUnwrappedEnum
    public void testUnwrappedEnum() throws Exception {
        final ObjectMapper mapper = new ObjectMapper();
        mapper.enable(DeserializationFeature.UNWRAP_SINGLE_VALUE_ARRAYS);
        
        assertEquals(TestEnum.JACKSON, mapper.readValue("[" + quote("JACKSON") + "]", TestEnum.class));
    }

// com.fasterxml.jackson.databind.deser.TestEnumDeserialization::testUnwrappedEnumException
    public void testUnwrappedEnumException() throws Exception {
        final ObjectMapper mapper = new ObjectMapper();
        mapper.disable(DeserializationFeature.UNWRAP_SINGLE_VALUE_ARRAYS);
        try {
            assertEquals(TestEnum.JACKSON, mapper.readValue("[" + quote("JACKSON") + "]", TestEnum.class));
            fail("Exception was not thrown on deserializing a single array element of type enum");
        } catch (JsonMappingException exp) {
            
        }
    }

// com.fasterxml.jackson.databind.deser.TestEnumDeserialization::testIndexAsString
    public void testIndexAsString() throws Exception
    {
        
        TestEnum en = MAPPER.readValue("2", TestEnum.class);
        assertSame(TestEnum.values()[2], en);

        
        en = MAPPER.readValue(quote("1"), TestEnum.class);
        assertSame(TestEnum.values()[1], en);
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

// com.fasterxml.jackson.databind.deser.TestExceptionDeserialization::testSingleValueArrayDeserialization
    public void testSingleValueArrayDeserialization() {}

// com.fasterxml.jackson.databind.deser.TestExceptionDeserialization::testSingleValueArrayDeserializationException
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
            
        }
    }

// com.fasterxml.jackson.databind.deser.TestExceptionHandling::testHandlingOfUnrecognized
    public void testHandlingOfUnrecognized() throws Exception
    {
        UnrecognizedPropertyException exc = null;
        try {
            new ObjectMapper().readValue("{\"bar\":3}", Bean.class);
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

// com.fasterxml.jackson.databind.deser.TestExceptionHandling::testExceptionWithEmpty
    public void testExceptionWithEmpty() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        try {
            Object result = mapper.readValue("    ", Object.class);
            fail("Expected an exception, but got result value: "+result);
        } catch (Exception e) {
            verifyException(e, JsonMappingException.class, "No content");
        }
    }

// com.fasterxml.jackson.databind.deser.TestExceptionHandling::testExceptionWithIncomplete
    public void testExceptionWithIncomplete()
        throws Exception
    {
        BrokenStringReader r = new BrokenStringReader("[ 1, ", "TEST");
        JsonFactory f = new JsonFactory();
        JsonParser jp = f.createParser(r);
        ObjectMapper mapper = new ObjectMapper();
        try {
            @SuppressWarnings("unused")
            Object ob = mapper.readValue(jp, Object.class);
            fail("Should have gotten an exception");
        } catch (IOException e) {
            
            verifyException(e, IOException.class, "TEST");
        }
    }

// com.fasterxml.jackson.databind.deser.TestExceptionHandling::testExceptionWithEOF
    public void testExceptionWithEOF()
        throws Exception
    {
        StringReader r = new StringReader("  3");
        JsonFactory f = new JsonFactory();
        JsonParser jp = f.createParser(r);
        ObjectMapper mapper = new ObjectMapper();

        Integer I = mapper.readValue(jp, Integer.class);
        assertEquals(3, I.intValue());

        
        try {
            I = mapper.readValue(jp, Integer.class);
            fail("Should have gotten an exception");
        } catch (IOException e) {
            verifyException(e, JsonMappingException.class, "No content");
        }
        
        JsonToken t = jp.getCurrentToken();
        if (t != null) {
            fail("Expected current token to be null after end-of-stream, was: "+t);
        }
        jp.close();
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

// com.fasterxml.jackson.databind.deser.TestGenericCollectionDeser::testListSubClass
    public void testListSubClass() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        ListSubClass result = mapper.readValue("[ \"123\" ]", ListSubClass.class);
        assertEquals(1, result.size());
        Object value = result.get(0);
        assertEquals(StringWrapper.class, value.getClass());
        StringWrapper bw = (StringWrapper) value;
        assertEquals("123", bw.str);
    }

// com.fasterxml.jackson.databind.deser.TestGenericCollectionDeser::testAnnotatedLStringist
    public void testAnnotatedLStringist() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        AnnotatedStringList result = mapper.readValue("[ \"...\" ]", AnnotatedStringList.class);
        assertEquals(1, result.size());
        Object ob = result.get(0);
        assertEquals(StringWrapper.class, ob.getClass());
        assertEquals("...", ((StringWrapper) ob).str);
    }

// com.fasterxml.jackson.databind.deser.TestGenericCollectionDeser::testAnnotatedBooleanList
    public void testAnnotatedBooleanList() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        AnnotatedBooleanList result = mapper.readValue("[ false ]", AnnotatedBooleanList.class);
        assertEquals(1, result.size());
        Object ob = result.get(0);
        assertEquals(BooleanWrapper.class, ob.getClass());
        assertFalse(((BooleanWrapper) ob).b);
    }

// com.fasterxml.jackson.databind.deser.TestGenericMapDeser::testMapSubClass
    public void testMapSubClass() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        MapSubClass result = mapper.readValue
            ("{\"a\":true }", MapSubClass.class);
        assertEquals(1, result.size());
        Object value = result.get("a");
        assertEquals(BooleanWrapper.class, value.getClass());
        BooleanWrapper bw = (BooleanWrapper) value;
        assertEquals(Boolean.TRUE, bw.b);
    }

// com.fasterxml.jackson.databind.deser.TestGenericMapDeser::testMapWrapper
    public void testMapWrapper() throws Exception
    {
        StringMap value = new ObjectMapper().readValue
            ("{\"entries\":{\"a\":9} }", StringMap.class);
        assertNotNull(value.getEntries());
        assertEquals(1, value.getEntries().size());
        assertEquals(Long.valueOf(9), value.getEntries().get("a"));
    }

// com.fasterxml.jackson.databind.deser.TestGenericMapDeser::testIntermediateTypes
    public void testIntermediateTypes() throws Exception
    {
        StringStringWrapperMap result = new ObjectMapper().readValue
            ("{\"a\":\"b\"}", StringStringWrapperMap.class);
        assertEquals(1, result.size());
        Object value = result.get("a");
        assertNotNull(value);
        assertEquals(value.getClass(), StringWrapper.class);
        assertEquals("b", ((StringWrapper) value).str);
    }

// com.fasterxml.jackson.databind.deser.TestGenericMapDeser::testAnnotatedMap
    public void testAnnotatedMap() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        AnnotatedMap result = mapper.readValue
            ("{\"a\":true }", AnnotatedMap.class);
        assertEquals(1, result.size());
        Map.Entry<Object,Object> en = result.entrySet().iterator().next();
        assertEquals(StringWrapper.class, en.getKey().getClass());
        assertEquals(BooleanWrapper.class, en.getValue().getClass());
        assertEquals("a", ((StringWrapper) en.getKey()).str);
        assertEquals(Boolean.TRUE, ((BooleanWrapper) en.getValue()).b);
    }

// com.fasterxml.jackson.databind.deser.TestGenericMapDeser::testKeyViaCtor
    public void testKeyViaCtor() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        Map<KeyTypeCtor,Integer> map = mapper.readValue("{\"a\":123}",
                TypeFactory.defaultInstance().constructMapType(HashMap.class, KeyTypeCtor.class, Integer.class));
        assertEquals(1, map.size());
        Map.Entry<?,?> entry = map.entrySet().iterator().next();
        assertEquals(Integer.valueOf(123), entry.getValue());
        Object key = entry.getKey();
        assertEquals(KeyTypeCtor.class, key.getClass());
        assertEquals("a", ((KeyTypeCtor) key).value);
    }

// com.fasterxml.jackson.databind.deser.TestGenericMapDeser::testKeyViaFactory
    public void testKeyViaFactory() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        Map<KeyTypeCtor,Integer> map = mapper.readValue("{\"a\":123}",
                TypeFactory.defaultInstance().constructMapType(HashMap.class, KeyTypeFactory.class, Integer.class));
        assertEquals(1, map.size());
        Map.Entry<?,?> entry = map.entrySet().iterator().next();
        assertEquals(Integer.valueOf(123), entry.getValue());
        Object key = entry.getKey();
        assertEquals(KeyTypeFactory.class, key.getClass());
        assertEquals("a", ((KeyTypeFactory) key).value);
    }

// com.fasterxml.jackson.databind.deser.TestGenericNumber::testIntAsNumber
    public void testIntAsNumber() throws Exception
    {
        
        Number result = new ObjectMapper().readValue(new StringReader(" 123 "), Number.class);
        assertEquals(Integer.valueOf(123), result);
    }

// com.fasterxml.jackson.databind.deser.TestGenericNumber::testLongAsNumber
    public void testLongAsNumber() throws Exception
    {
        
        long exp = 1234567890123L;
        Number result = new ObjectMapper().readValue(String.valueOf(exp), Number.class);
        assertEquals(Long.valueOf(exp), result);
    }

// com.fasterxml.jackson.databind.deser.TestGenericNumber::testBigIntAsNumber
    public void testBigIntAsNumber() throws Exception
    {
        
        BigInteger biggie = new BigInteger("1234567890123456789012345678901234567890");
        Number result = new ObjectMapper().readValue(biggie.toString(), Number.class);
        assertEquals(BigInteger.class, biggie.getClass());
        assertEquals(biggie, result);
    }

// com.fasterxml.jackson.databind.deser.TestGenericNumber::testIntTypeOverride
    public void testIntTypeOverride() throws Exception
    {
        
        ObjectMapper m = new ObjectMapper();
        m.enable(DeserializationFeature.USE_BIG_INTEGER_FOR_INTS);
        BigInteger exp = BigInteger.valueOf(123L);

        
        Number result = m.readValue(new StringReader(" 123 "), Number.class);
        assertEquals(BigInteger.class, result.getClass());
        assertEquals(exp, result);

        
         m.readValue(new StringReader("123"), Object.class);
        assertEquals(BigInteger.class, result.getClass());
        assertEquals(exp, result);
    }

// com.fasterxml.jackson.databind.deser.TestGenericNumber::testDoubleAsNumber
    public void testDoubleAsNumber() throws Exception
    {
        Number result = new ObjectMapper().readValue(new StringReader(" 1.0 "), Number.class);
        assertEquals(Double.valueOf(1.0), result);
    }

// com.fasterxml.jackson.databind.deser.TestGenericNumber::testFpTypeOverrideSimple
    public void testFpTypeOverrideSimple() throws Exception
    {
        ObjectMapper m = new ObjectMapper();
        m.enable(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS);
        BigDecimal dec = new BigDecimal("0.1");

        
        Number result = m.readValue(dec.toString(), Number.class);
        assertEquals(BigDecimal.class, result.getClass());
        assertEquals(dec, result);

        
        Object value = m.readValue(dec.toString(), Object.class);
        assertEquals(BigDecimal.class, result.getClass());
        assertEquals(dec, value);
    }

// com.fasterxml.jackson.databind.deser.TestGenericNumber::testFpTypeOverrideStructured
	public void testFpTypeOverrideStructured() throws Exception
    {
        ObjectMapper m = new ObjectMapper();
        BigDecimal dec = new BigDecimal("-19.37");

        m.enable(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS);

        
        @SuppressWarnings("unchecked")
        List<Object> list = (List<Object>)m.readValue("[ "+dec.toString()+" ]", List.class);
        assertEquals(1, list.size());
        Object val = list.get(0);
        assertEquals(BigDecimal.class, val.getClass());
        assertEquals(dec, val);

        
        Map<?,?> map = m.readValue("{ \"a\" : "+dec.toString()+" }", Map.class);
        assertEquals(1, map.size());
        val = map.get("a");
        assertEquals(BigDecimal.class, val.getClass());
        assertEquals(dec, val);
    }

// com.fasterxml.jackson.databind.deser.TestGenerics::testSimpleNumberBean
    public void testSimpleNumberBean() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        NumberBean result = mapper.readValue("{\"number\":17}", NumberBean.class);
        assertEquals(17, result._number);
    }

// com.fasterxml.jackson.databind.deser.TestGenerics::testGenericWrapper
    public void testGenericWrapper() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        Wrapper<SimpleBean> result = mapper.readValue
            ("{\"value\": { \"x\" : 13 } }",
             new TypeReference<Wrapper<SimpleBean>>() { });
        assertNotNull(result);
        assertEquals(Wrapper.class, result.getClass());
        Object contents = result.value;
        assertNotNull(contents);
        assertEquals(SimpleBean.class, contents.getClass());
        SimpleBean bean = (SimpleBean) contents;
        assertEquals(13, bean.x);
    }

// com.fasterxml.jackson.databind.deser.TestGenerics::testGenericWrapperWithSingleElementArray
    public void testGenericWrapperWithSingleElementArray() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(DeserializationFeature.UNWRAP_SINGLE_VALUE_ARRAYS);
        
        Wrapper<SimpleBean> result = mapper.readValue
            ("[{\"value\": [{ \"x\" : 13 }] }]",
             new TypeReference<Wrapper<SimpleBean>>() { });
        assertNotNull(result);
        assertEquals(Wrapper.class, result.getClass());
        Object contents = result.value;
        assertNotNull(contents);
        assertEquals(SimpleBean.class, contents.getClass());
        SimpleBean bean = (SimpleBean) contents;
        assertEquals(13, bean.x);
    }

// com.fasterxml.jackson.databind.deser.TestGenerics::testMultipleWrappers
    public void testMultipleWrappers() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();

        
        Wrapper<Boolean> result = mapper.readValue
            ("{\"value\": true}", new TypeReference<Wrapper<Boolean>>() { });
        assertEquals(new Wrapper<Boolean>(Boolean.TRUE), result);

        
        Wrapper<String> result2 = mapper.readValue
            ("{\"value\": \"abc\"}", new TypeReference<Wrapper<String>>() { });
        assertEquals(new Wrapper<String>("abc"), result2);

        
        Wrapper<Long> result3 = mapper.readValue
            ("{\"value\": 7}", new TypeReference<Wrapper<Long>>() { });
        assertEquals(new Wrapper<Long>(7L), result3);
    }

// com.fasterxml.jackson.databind.deser.TestGenerics::testMultipleWrappersSingleValueArray
    public void testMultipleWrappersSingleValueArray() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(DeserializationFeature.UNWRAP_SINGLE_VALUE_ARRAYS);

        
        Wrapper<Boolean> result = mapper.readValue
            ("[{\"value\": [true]}]", new TypeReference<Wrapper<Boolean>>() { });
        assertEquals(new Wrapper<Boolean>(Boolean.TRUE), result);

        
        Wrapper<String> result2 = mapper.readValue
            ("[{\"value\": [\"abc\"]}]", new TypeReference<Wrapper<String>>() { });
        assertEquals(new Wrapper<String>("abc"), result2);

        
        Wrapper<Long> result3 = mapper.readValue
            ("[{\"value\": [7]}]", new TypeReference<Wrapper<Long>>() { });
        assertEquals(new Wrapper<Long>(7L), result3);
    }

// com.fasterxml.jackson.databind.deser.TestGenerics::testArrayOfGenericWrappers
    public void testArrayOfGenericWrappers() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        Wrapper<SimpleBean>[] result = mapper.readValue
            ("[ {\"value\": { \"x\" : 9 } } ]",
             new TypeReference<Wrapper<SimpleBean>[]>() { });
        assertNotNull(result);
        assertEquals(Wrapper[].class, result.getClass());
        assertEquals(1, result.length);
        Wrapper<SimpleBean> elem = result[0];
        Object contents = elem.value;
        assertNotNull(contents);
        assertEquals(SimpleBean.class, contents.getClass());
        SimpleBean bean = (SimpleBean) contents;
        assertEquals(9, bean.x);
    }

// com.fasterxml.jackson.databind.deser.TestGenerics::testArrayOfGenericWrappersSingleValueArray
    public void testArrayOfGenericWrappersSingleValueArray() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(DeserializationFeature.UNWRAP_SINGLE_VALUE_ARRAYS);
        
        Wrapper<SimpleBean>[] result = mapper.readValue
            ("[ {\"value\": [ { \"x\" : [ 9 ] } ] } ]",
             new TypeReference<Wrapper<SimpleBean>[]>() { });
        assertNotNull(result);
        assertEquals(Wrapper[].class, result.getClass());
        assertEquals(1, result.length);
        Wrapper<SimpleBean> elem = result[0];
        Object contents = elem.value;
        assertNotNull(contents);
        assertEquals(SimpleBean.class, contents.getClass());
        SimpleBean bean = (SimpleBean) contents;
        assertEquals(9, bean.x);
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

// com.fasterxml.jackson.databind.deser.TestIgnoredTypes::testIgnoredType
    public void testIgnoredType() throws Exception
    {
        
        ObjectMapper mapper = new ObjectMapper();
        NonIgnoredType bean = mapper.readValue("{\"value\":13}", NonIgnoredType.class);
        assertNotNull(bean);
        assertEquals(13, bean.value);

        
        bean = mapper.readValue("{ \"ignored\":[1,2,{}], \"value\":9 }", NonIgnoredType.class);
        assertNotNull(bean);
        assertEquals(9, bean.value);
    }

// com.fasterxml.jackson.databind.deser.TestIgnoredTypes::testSingleWithMixins
    public void testSingleWithMixins() throws Exception {
        SimpleModule module = new SimpleModule();
        module.setMixInAnnotation(Person.class, PersonMixin.class);
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(module);
        PersonWrapper input = new PersonWrapper();
        String json = mapper.writeValueAsString(input);
        assertEquals("{\"value\":1}", json);
    }

// com.fasterxml.jackson.databind.deser.TestIgnoredTypes::testListWithMixins
    public void testListWithMixins() throws Exception {
        SimpleModule module = new SimpleModule();
        module.setMixInAnnotation(Person.class, PersonMixin.class);
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(module);
        List<Person> persons = new ArrayList<Person>();
        persons.add(new Person("Bob"));
        String json = mapper.writeValueAsString(persons);
        assertEquals("[{\"name\":\"Bob\"}]", json);
    }

// com.fasterxml.jackson.databind.deser.TestInjectables::testSimple
    public void testSimple() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setInjectableValues(new InjectableValues.Std()
            .addValue(String.class, "stuffValue")
            .addValue("myId", "xyz")
            .addValue(Long.TYPE, Long.valueOf(37))
            );
        InjectedBean bean = mapper.readValue("{\"value\":3}", InjectedBean.class);
        assertEquals(3, bean.value);
        assertEquals("stuffValue", bean.stuff);
        assertEquals("xyz", bean.otherStuff);
        assertEquals(37L, bean.third);
    }

// com.fasterxml.jackson.databind.deser.TestInjectables::testWithCtors
    public void testWithCtors() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setInjectableValues(new InjectableValues.Std()
            .addValue(String.class, "Bubba")
            );
        CtorBean bean = mapper.readValue("{\"age\":55}", CtorBean.class);
        assertEquals(55, bean.age);
        assertEquals("Bubba", bean.name);
    }

// com.fasterxml.jackson.databind.deser.TestInjectables::testTwoInjectablesViaCreator
    public void testTwoInjectablesViaCreator() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setInjectableValues(new InjectableValues.Std()
            .addValue(String.class, "Bob")
            .addValue("number", Integer.valueOf(13))
            );
        CtorBean2 bean = mapper.readValue("{ }", CtorBean2.class);
        assertEquals(Integer.valueOf(13), bean.age);
        assertEquals("Bob", bean.name);
    }

// com.fasterxml.jackson.databind.deser.TestInjectables::testInvalidDup
    public void testInvalidDup() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        try {
            mapper.readValue("{}", BadBean1.class);
        } catch (Exception e) {
            verifyException(e, "Duplicate injectable value");
        }
        try {
            mapper.readValue("{}", BadBean2.class);
        } catch (Exception e) {
            verifyException(e, "Duplicate injectable value");
        }
    }

// com.fasterxml.jackson.databind.deser.TestInjectables::testIssueGH471
    public void testIssueGH471() throws Exception
    {
        final Object constructorInjected = "constructorInjected";
        final Object methodInjected = "methodInjected";
        final Object fieldInjected = "fieldInjected";

        ObjectMapper mapper = new ObjectMapper()
                        .setInjectableValues(new InjectableValues.Std()
                                .addValue("constructor_injected", constructorInjected)
                                .addValue("method_injected", methodInjected)
                                .addValue("field_injected", fieldInjected));

        IssueGH471Bean bean = mapper.readValue("{\"x\":13,\"constructor_value\":\"constructor\",\"method_value\":\"method\",\"field_value\":\"field\"}",
                IssueGH471Bean.class);

        
        assertSame(constructorInjected, bean.constructorInjected);
        assertSame(methodInjected, bean.methodInjected);
        assertSame(fieldInjected, bean.fieldInjected);

        
        assertEquals("constructor", bean.constructorValue);
        assertEquals("method", bean.methodValue);
        assertEquals("field", bean.fieldValue);

        assertEquals(13, bean.x);
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
        result.close();
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

        
        String json = MAPPER.writeValueAsString(abs);
        File result = MAPPER.readValue(json, File.class);
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
        
        String json = MAPPER.writeValueAsString(exp);
        Pattern result = MAPPER.readValue(json, Pattern.class);
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
        assertEquals(new Locale("en"), MAPPER.readValue(quote("en"), Locale.class));
        assertEquals(new Locale("es", "ES"), MAPPER.readValue(quote("es_ES"), Locale.class));
        assertEquals(new Locale("FI", "fi", "savo"), MAPPER.readValue(quote("fi_FI_savo"), Locale.class));
    }

// com.fasterxml.jackson.databind.deser.TestJdkTypes::testNullForPrimitives
    public void testNullForPrimitives() throws IOException
    {
        
        PrimitivesBean bean = MAPPER.readValue("{\"intValue\":null, \"booleanValue\":null, \"doubleValue\":null}",
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
        CharSequence cs = MAPPER.readValue("\"abc\"", CharSequence.class);
        assertEquals(String.class, cs.getClass());
        assertEquals("abc", cs.toString());
    }

// com.fasterxml.jackson.databind.deser.TestJdkTypes::testInetAddress
    public void testInetAddress() throws IOException
    {
        InetAddress address = MAPPER.readValue(quote("127.0.0.1"), InetAddress.class);
        assertEquals("127.0.0.1", address.getHostAddress());

        
        final String HOST = "google.com";
        address = MAPPER.readValue(quote(HOST), InetAddress.class);
        assertEquals(HOST, address.getHostName());
    }

// com.fasterxml.jackson.databind.deser.TestJdkTypes::testInetSocketAddress
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
        String json = MAPPER.writeValueAsString(new ParamClassBean("Foobar"));

        ParamClassBean result = MAPPER.readValue(json, ParamClassBean.class);
        assertEquals("Foobar", result.name);
        assertSame(String.class, result.clazz);
    }

// com.fasterxml.jackson.databind.deser.TestJdkTypes::testEmptyStringForWrappers
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

// com.fasterxml.jackson.databind.deser.TestJdkTypes::testEmptyStringForPrimitives
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

// com.fasterxml.jackson.databind.deser.TestJdkTypes::testUntypedWithJsonArrays
    public void testUntypedWithJsonArrays() throws Exception
    {
        
        Object ob = MAPPER.readValue("[1]", Object.class);
        assertTrue(ob instanceof List<?>);

        
        MAPPER.configure(DeserializationFeature.USE_JAVA_ARRAY_FOR_JSON_ARRAY, true);
        ob = MAPPER.readValue("[1]", Object.class);
        assertEquals(Object[].class, ob.getClass());
    }

// com.fasterxml.jackson.databind.deser.TestJdkTypes::testLongToBoolean
    public void testLongToBoolean() throws Exception
    {
        long value = 1L + Integer.MAX_VALUE;
        BooleanBean b = MAPPER.readValue("{\"primitive\" : "+value+", \"wrapper\":"+value+", \"ctor\":"+value+"}",
                    BooleanBean.class);
        assertEquals(Boolean.TRUE, b.wrapper);
        assertTrue(b.primitive);
        assertEquals(Boolean.TRUE, b.ctor);
    }

// com.fasterxml.jackson.databind.deser.TestJdkTypes::testCharset
    public void testCharset() throws Exception
    {
        Charset UTF8 = Charset.forName("UTF-8");
        assertSame(UTF8, MAPPER.readValue(quote("UTF-8"), Charset.class));
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
        String json = MAPPER.writeValueAsString(elem);
        StackTraceElement back = MAPPER.readValue(json, StackTraceElement.class);
        
        assertEquals("testStackTraceElement", back.getMethodName());
        assertEquals(elem.getLineNumber(), back.getLineNumber());
        assertEquals(elem.getClassName(), back.getClassName());
        assertEquals(elem.isNativeMethod(), back.isNativeMethod());
        assertTrue(back.getClassName().endsWith("TestJdkTypes"));
        assertFalse(back.isNativeMethod());
    }

// com.fasterxml.jackson.databind.deser.TestJdkTypes::testByteBuffer
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

// com.fasterxml.jackson.databind.deser.TestJdkTypes::testStackTraceElementWithCustom
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

// com.fasterxml.jackson.databind.deser.TestJdkTypes::testSingleElementArray
    public void testSingleElementArray() throws Exception {
        final int intTest = 932832;
        final double doubleTest = 32.3234;
        final long longTest = 2374237428374293423L;
        final short shortTest = (short) intTest;
        final float floatTest = 84.3743f;
        final byte byteTest = (byte) 43;
        final char charTest = 'c';

        final ObjectMapper mapper = new ObjectMapper();
        mapper.enable(DeserializationFeature.UNWRAP_SINGLE_VALUE_ARRAYS);

        final int intValue = mapper.readValue(asArray(intTest), Integer.TYPE);
        assertEquals(intTest, intValue);
        final Integer integerWrapperValue = mapper.readValue(asArray(Integer.valueOf(intTest)), Integer.class);
        assertEquals(Integer.valueOf(intTest), integerWrapperValue);

        final double doubleValue = mapper.readValue(asArray(doubleTest), Double.class);
        assertEquals(doubleTest, doubleValue);
        final Double doubleWrapperValue = mapper.readValue(asArray(Double.valueOf(doubleTest)), Double.class);
        assertEquals(Double.valueOf(doubleTest), doubleWrapperValue);

        final long longValue = mapper.readValue(asArray(longTest), Long.TYPE);
        assertEquals(longTest, longValue);
        final Long longWrapperValue = mapper.readValue(asArray(Long.valueOf(longTest)), Long.class);
        assertEquals(Long.valueOf(longTest), longWrapperValue);

        final short shortValue = mapper.readValue(asArray(shortTest), Short.TYPE);
        assertEquals(shortTest, shortValue);
        final Short shortWrapperValue = mapper.readValue(asArray(Short.valueOf(shortTest)), Short.class);
        assertEquals(Short.valueOf(shortTest), shortWrapperValue);

        final float floatValue = mapper.readValue(asArray(floatTest), Float.TYPE);
        assertEquals(floatTest, floatValue);
        final Float floatWrapperValue = mapper.readValue(asArray(Float.valueOf(floatTest)), Float.class);
        assertEquals(Float.valueOf(floatTest), floatWrapperValue);

        final byte byteValue = mapper.readValue(asArray(byteTest), Byte.TYPE);
        assertEquals(byteTest, byteValue);
        final Byte byteWrapperValue = mapper.readValue(asArray(Byte.valueOf(byteTest)), Byte.class);
        assertEquals(Byte.valueOf(byteTest), byteWrapperValue);

        final char charValue = mapper.readValue(asArray(quote(String.valueOf(charTest))), Character.TYPE);
        assertEquals(charTest, charValue);
        final Character charWrapperValue = mapper.readValue(asArray(quote(String.valueOf(charTest))), Character.class);
        assertEquals(Character.valueOf(charTest), charWrapperValue);

        final boolean booleanTrueValue = mapper.readValue(asArray(true), Boolean.TYPE);
        assertTrue(booleanTrueValue);

        final boolean booleanFalseValue = mapper.readValue(asArray(false), Boolean.TYPE);
        assertFalse(booleanFalseValue);

        final Boolean booleanWrapperTrueValue = mapper.readValue(asArray(Boolean.valueOf(true)), Boolean.class);
        assertEquals(Boolean.TRUE, booleanWrapperTrueValue);
    }

// com.fasterxml.jackson.databind.deser.TestJdkTypes::testSingleElementArrayException
    public void testSingleElementArrayException() throws Exception {
        final ObjectMapper mapper = new ObjectMapper();
        mapper.disable(DeserializationFeature.UNWRAP_SINGLE_VALUE_ARRAYS);
        try {
            mapper.readValue("[42]", Integer.class);
            fail("Single value array didn't throw an exception when DeserializationFeature.UNWRAP_SINGLE_VALUE_ARRAYS is disabled");
        } catch (JsonMappingException exp) {
            
        }
        try {
            mapper.readValue("[42]", Integer.TYPE);
            fail("Single value array didn't throw an exception when DeserializationFeature.UNWRAP_SINGLE_VALUE_ARRAYS is disabled");
        } catch (JsonMappingException exp) {
            
        }

        try {
            mapper.readValue("[42.273]", Double.class);
            fail("Single value array didn't throw an exception when DeserializationFeature.UNWRAP_SINGLE_VALUE_ARRAYS is disabled");
        } catch (JsonMappingException exp) {
            
        }
        try {
            mapper.readValue("[42.2723]", Double.TYPE);
            fail("Single value array didn't throw an exception when DeserializationFeature.UNWRAP_SINGLE_VALUE_ARRAYS is disabled");
        } catch (JsonMappingException exp) {
            
        }

        try {
            mapper.readValue("[42342342342342]", Long.class);
            fail("Single value array didn't throw an exception when DeserializationFeature.UNWRAP_SINGLE_VALUE_ARRAYS is disabled");
        } catch (JsonMappingException exp) {
            
        }
        try {
            mapper.readValue("[42342342342342342]", Long.TYPE);
            fail("Single value array didn't throw an exception when DeserializationFeature.UNWRAP_SINGLE_VALUE_ARRAYS is disabled");
        } catch (JsonMappingException exp) {
            
        }

        try {
            mapper.readValue("[42]", Short.class);
            fail("Single value array didn't throw an exception when DeserializationFeature.UNWRAP_SINGLE_VALUE_ARRAYS is disabled");
        } catch (JsonMappingException exp) {
            
        }
        try {
            mapper.readValue("[42]", Short.TYPE);
            fail("Single value array didn't throw an exception when DeserializationFeature.UNWRAP_SINGLE_VALUE_ARRAYS is disabled");
        } catch (JsonMappingException exp) {
            
        }

        try {
            mapper.readValue("[327.2323]", Float.class);
            fail("Single value array didn't throw an exception when DeserializationFeature.UNWRAP_SINGLE_VALUE_ARRAYS is disabled");
        } catch (JsonMappingException exp) {
            
        }
        try {
            mapper.readValue("[82.81902]", Float.TYPE);
            fail("Single value array didn't throw an exception when DeserializationFeature.UNWRAP_SINGLE_VALUE_ARRAYS is disabled");
        } catch (JsonMappingException exp) {
            
        }

        try {
            mapper.readValue("[22]", Byte.class);
            fail("Single value array didn't throw an exception when DeserializationFeature.UNWRAP_SINGLE_VALUE_ARRAYS is disabled");
        } catch (JsonMappingException exp) {
            
        }
        try {
            mapper.readValue("[22]", Byte.TYPE);
            fail("Single value array didn't throw an exception when DeserializationFeature.UNWRAP_SINGLE_VALUE_ARRAYS is disabled");
        } catch (JsonMappingException exp) {
            
        }

        try {
            mapper.readValue("['d']", Character.class);
            fail("Single value array didn't throw an exception when DeserializationFeature.UNWRAP_SINGLE_VALUE_ARRAYS is disabled");
        } catch (JsonMappingException exp) {
            
        }
        try {
            mapper.readValue("['d']", Character.TYPE);
            fail("Single value array didn't throw an exception when DeserializationFeature.UNWRAP_SINGLE_VALUE_ARRAYS is disabled");
        } catch (JsonMappingException exp) {
            
        }

        try {
            mapper.readValue("[true]", Boolean.class);
            fail("Single value array didn't throw an exception when DeserializationFeature.UNWRAP_SINGLE_VALUE_ARRAYS is disabled");
        } catch (JsonMappingException exp) {
            
        }
        try {
            mapper.readValue("[true]", Boolean.TYPE);
            fail("Single value array didn't throw an exception when DeserializationFeature.UNWRAP_SINGLE_VALUE_ARRAYS is disabled");
        } catch (JsonMappingException exp) {
            
        }
    }

// com.fasterxml.jackson.databind.deser.TestJdkTypes::testMultiValueArrayException
    public void testMultiValueArrayException() throws IOException {
        final ObjectMapper mapper = new ObjectMapper();
        mapper.enable(DeserializationFeature.UNWRAP_SINGLE_VALUE_ARRAYS);
        
        try {
            mapper.readValue("[42,42]", Integer.class);
            fail("Single value array didn't throw an exception when DeserializationFeature.UNWRAP_SINGLE_VALUE_ARRAYS is disabled");
        } catch (JsonMappingException exp) {
            
        }
        try {
            mapper.readValue("[42,42]", Integer.TYPE);
            fail("Single value array didn't throw an exception when DeserializationFeature.UNWRAP_SINGLE_VALUE_ARRAYS is disabled");
        } catch (JsonMappingException exp) {
            
        }
        
        try {
            mapper.readValue("[42.273,42.273]", Double.class);
            fail("Single value array didn't throw an exception when DeserializationFeature.UNWRAP_SINGLE_VALUE_ARRAYS is disabled");
        } catch (JsonMappingException exp) {
            
        }
        try {
            mapper.readValue("[42.2723,42.273]", Double.TYPE);
            fail("Single value array didn't throw an exception when DeserializationFeature.UNWRAP_SINGLE_VALUE_ARRAYS is disabled");
        } catch (JsonMappingException exp) {
            
        }
        
        try {
            mapper.readValue("[42342342342342,42342342342342]", Long.class);
            fail("Single value array didn't throw an exception when DeserializationFeature.UNWRAP_SINGLE_VALUE_ARRAYS is disabled");
        } catch (JsonMappingException exp) {
            
        }
        try {
            mapper.readValue("[42342342342342342,42342342342342]", Long.TYPE);
            fail("Single value array didn't throw an exception when DeserializationFeature.UNWRAP_SINGLE_VALUE_ARRAYS is disabled");
        } catch (JsonMappingException exp) {
            
        }
        
        try {
            mapper.readValue("[42,42]", Short.class);
            fail("Single value array didn't throw an exception when DeserializationFeature.UNWRAP_SINGLE_VALUE_ARRAYS is disabled");
        } catch (JsonMappingException exp) {
            
        }
        try {
            mapper.readValue("[42,42]", Short.TYPE);
            fail("Single value array didn't throw an exception when DeserializationFeature.UNWRAP_SINGLE_VALUE_ARRAYS is disabled");
        } catch (JsonMappingException exp) {
            
        }
        
        try {
            mapper.readValue("[327.2323,327.2323]", Float.class);
            fail("Single value array didn't throw an exception when DeserializationFeature.UNWRAP_SINGLE_VALUE_ARRAYS is disabled");
        } catch (JsonMappingException exp) {
            
        }
        try {
            mapper.readValue("[82.81902,327.2323]", Float.TYPE);
            fail("Single value array didn't throw an exception when DeserializationFeature.UNWRAP_SINGLE_VALUE_ARRAYS is disabled");
        } catch (JsonMappingException exp) {
            
        }
        
        try {
            mapper.readValue("[22,23]", Byte.class);
            fail("Single value array didn't throw an exception when DeserializationFeature.UNWRAP_SINGLE_VALUE_ARRAYS is disabled");
        } catch (JsonMappingException exp) {
            
        }
        try {
            mapper.readValue("[22,23]", Byte.TYPE);
            fail("Single value array didn't throw an exception when DeserializationFeature.UNWRAP_SINGLE_VALUE_ARRAYS is disabled");
        } catch (JsonMappingException exp) {
            
        }
        
        try {
            mapper.readValue(asArray(quote("c") + ","  + quote("d")), Character.class);
            
            fail("Single value array didn't throw an exception when DeserializationFeature.UNWRAP_SINGLE_VALUE_ARRAYS is disabled");
        } catch (JsonMappingException exp) {
            
        }
        try {
            mapper.readValue(asArray(quote("c") + ","  + quote("d")), Character.TYPE);
            fail("Single value array didn't throw an exception when DeserializationFeature.UNWRAP_SINGLE_VALUE_ARRAYS is disabled");
        } catch (JsonMappingException exp) {
            
        }
        
        try {
            mapper.readValue("[true,false]", Boolean.class);
            fail("Single value array didn't throw an exception when DeserializationFeature.UNWRAP_SINGLE_VALUE_ARRAYS is disabled");
        } catch (JsonMappingException exp) {
            
        }
        try {
            mapper.readValue("[true,false]", Boolean.TYPE);
            fail("Single value array didn't throw an exception when DeserializationFeature.UNWRAP_SINGLE_VALUE_ARRAYS is disabled");
        } catch (JsonMappingException exp) {
            
        }
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

// com.fasterxml.jackson.databind.deser.TestMapDeserialization::testBigUntypedMap
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

// com.fasterxml.jackson.databind.deser.TestNullHandling::testCustomRootNulls
    public void testCustomRootNulls() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        SimpleModule module = new SimpleModule("test", Version.unknownVersion());
        module.addDeserializer(String.class, new FunnyNullDeserializer());
        mapper.registerModule(module);

        
        String str = mapper.readValue("null", String.class);
        assertNotNull(str);
        assertEquals("funny", str);
        
        
        ObjectReader reader = mapper.reader(String.class);
        str = reader.readValue("null");
        assertNotNull(str);
        assertEquals("funny", str);
    }

// com.fasterxml.jackson.databind.deser.TestNullHandling::testListOfNulls
    public void testListOfNulls() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        SimpleModule module = new SimpleModule("test", Version.unknownVersion());
        module.addDeserializer(String.class, new FunnyNullDeserializer());
        mapper.registerModule(module);

        List<String> list = Arrays.asList("funny");
        JavaType type = mapper.getTypeFactory().constructCollectionType(List.class, String.class);

        
        List<?> deser = mapper.readValue("[null]", type);
        assertNotNull(deser);
        assertEquals(1, deser.size());
        assertEquals(list.get(0), deser.get(0));

        
        ObjectReader reader = mapper.reader(type);
        deser = reader.readValue("[null]");
        assertNotNull(deser);
        assertEquals(1, deser.size());
        assertEquals(list.get(0), deser.get(0));
    }

// com.fasterxml.jackson.databind.deser.TestNullHandling::testMapOfNulls
    public void testMapOfNulls() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        SimpleModule module = new SimpleModule("test", Version.unknownVersion());
        module.addDeserializer(String.class, new FunnyNullDeserializer());
        mapper.registerModule(module);

        JavaType type = mapper.getTypeFactory().constructMapType(Map.class, String.class, String.class);
        
        Map<?,?> deser = mapper.readValue("{\"key\":null}", type);
        assertNotNull(deser);
        assertEquals(1, deser.size());
        assertEquals("funny", deser.get("key"));

        
        ObjectReader reader = mapper.reader(type);
        deser = reader.readValue("{\"key\":null}");
        assertNotNull(deser);
        assertEquals(1, deser.size());
        assertEquals("funny", deser.get("key"));
    }

// com.fasterxml.jackson.databind.deser.TestNumbers::testNaN
    public void testNaN() throws Exception
    {
        ObjectMapper m = new ObjectMapper();
        Float result = m.readValue(" \"NaN\"", Float.class);
        assertEquals(Float.valueOf(Float.NaN), result);

        Double d = m.readValue(" \"NaN\"", Double.class);
        assertEquals(Double.valueOf(Double.NaN), d);

        Number num = m.readValue(" \"NaN\"", Number.class);
        assertEquals(Double.valueOf(Double.NaN), num);
    }

// com.fasterxml.jackson.databind.deser.TestNumbers::testDoubleInf
    public void testDoubleInf() throws Exception
    {
        ObjectMapper m = new ObjectMapper();
        Double result = m.readValue(" \""+Double.POSITIVE_INFINITY+"\"", Double.class);
        assertEquals(Double.valueOf(Double.POSITIVE_INFINITY), result);

        result = m.readValue(" \""+Double.NEGATIVE_INFINITY+"\"", Double.class);
        assertEquals(Double.valueOf(Double.NEGATIVE_INFINITY), result);
    }

// com.fasterxml.jackson.databind.deser.TestNumbers::testEmptyAsNumber
    public void testEmptyAsNumber() throws Exception
    {
        ObjectMapper m = new ObjectMapper();
        assertNull(m.readValue(quote(""), Integer.class));
        assertNull(m.readValue(quote(""), Long.class));
        assertNull(m.readValue(quote(""), Float.class));
        assertNull(m.readValue(quote(""), Double.class));
        assertNull(m.readValue(quote(""), BigInteger.class));
        assertNull(m.readValue(quote(""), BigDecimal.class));
    }

// com.fasterxml.jackson.databind.deser.TestNumbers::testDeserializeDecimalHappyPath
    public void testDeserializeDecimalHappyPath() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        String json = "{\"defaultValue\": { \"value\": 123 } }";
        MyBeanHolder result = mapper.readValue(json, MyBeanHolder.class);
        assertEquals(BigDecimal.valueOf(123), result.defaultValue.value.decimal);
    }

// com.fasterxml.jackson.databind.deser.TestNumbers::testDeserializeDecimalProperException
    public void testDeserializeDecimalProperException() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        String json = "{\"defaultValue\": { \"value\": \"123\" } }";
        try {
            mapper.readValue(json, MyBeanHolder.class);
            fail("should have raised exception");
        } catch (JsonProcessingException e) {
            verifyException(e, "not numeric");
        }
    }

// com.fasterxml.jackson.databind.deser.TestNumbers::testDeserializeDecimalProperExceptionWhenIdSet
    public void testDeserializeDecimalProperExceptionWhenIdSet() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        String json = "{\"id\": 5, \"defaultValue\": { \"value\": \"123\" } }";
        try {
            MyBeanHolder result = mapper.readValue(json, MyBeanHolder.class);
            fail("should have raised exception instead value was set to " + result.defaultValue.value.decimal.toString());
        } catch (JsonProcessingException e) {
            verifyException(e, "not numeric");
        }
    }

// com.fasterxml.jackson.databind.deser.TestOverloaded::testSpecialization
    public void testSpecialization() throws Exception
    {
        ArrayListBean bean = MAPPER.readValue
            ("{\"list\":[\"a\",\"b\",\"c\"]}", ArrayListBean.class);
        assertNotNull(bean.list);
        assertEquals(3, bean.list.size());
        assertEquals(ArrayList.class, bean.list.getClass());
        assertEquals("a", bean.list.get(0));
        assertEquals("b", bean.list.get(1));
        assertEquals("c", bean.list.get(2));
    }

// com.fasterxml.jackson.databind.deser.TestOverloaded::testOverride
    public void testOverride() throws Exception
    {
        WasNumberBean bean = MAPPER.readValue
            ("{\"value\" : \"abc\"}", WasNumberBean.class);
        assertNotNull(bean);
        assertEquals("abc", bean.value);
    }

// com.fasterxml.jackson.databind.deser.TestOverloaded::testConflictResolution
    public void testConflictResolution() throws Exception
    {
        Overloaded739 bean = MAPPER.readValue
                ("{\"value\":\"abc\"}", Overloaded739.class);
        assertNotNull(bean);
        assertEquals("abc", bean._value);
    }

// com.fasterxml.jackson.databind.deser.TestOverloaded::testSetterConflict
    public void testSetterConflict() throws Exception
    {
    	try {    		
    	MAPPER.readValue("{ }", ConflictBean.class);
    	} catch (Exception e) {
    	    verifyException(e, "Conflicting setter definitions");
    	}
    }

// com.fasterxml.jackson.databind.deser.TestParentChildReferences::testSimpleRefs
    public void testSimpleRefs() throws Exception
    {
        SimpleTreeNode root = new SimpleTreeNode("root");
        SimpleTreeNode child = new SimpleTreeNode("kid");
        root.child = child;
        child.parent = root;
        
        String json = MAPPER.writeValueAsString(root);
        
        SimpleTreeNode resultNode = MAPPER.readValue(json, SimpleTreeNode.class);
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
        root.child = child;
        child.parent = root;
        
        String json = MAPPER.writeValueAsString(root);
        
        SimpleTreeNode2 resultNode = MAPPER.readValue(json, SimpleTreeNode2.class);
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
        root.firstChild = child1;
        child1.parent = root;
        child1.next = child2;
        child2.prev = child1;
        
        String json = MAPPER.writeValueAsString(root);
        
        FullTreeNode resultNode = MAPPER.readValue(json, FullTreeNode.class);
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
        String json = MAPPER.writeValueAsString(root);
        
        NodeArray result = MAPPER.readValue(json, NodeArray.class);
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
        String json = MAPPER.writeValueAsString(root);
        
        NodeList result = MAPPER.readValue(json, NodeList.class);
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
        String json = MAPPER.writeValueAsString(root);
        
        NodeMap result = MAPPER.readValue(json, NodeMap.class);
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

        
        String json = MAPPER.writeValueAsString(parent);

        AbstractNode root = MAPPER.readValue(json, AbstractNode.class);

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
        byte[] bytes = MAPPER.writeValueAsBytes(parent);
        Parent value = MAPPER.readValue(bytes, Parent.class); 
        for (Child child : value.children) {
            assertEquals(value, child.getParent());
        }
    }

// com.fasterxml.jackson.databind.deser.TestParentChildReferences::testIssue708
    public void testIssue708() throws Exception
    {
        Advertisement708 ad = MAPPER.readValue("{\"title\":\"Hroch\",\"photos\":[{\"id\":3}]}", Advertisement708.class);      
        assertNotNull(ad);
    }

// com.fasterxml.jackson.databind.deser.TestSetterlessProperties::testSimpleSetterlessCollectionOk
    public void testSimpleSetterlessCollectionOk()
        throws Exception
    {
        CollectionBean result = new ObjectMapper().readValue
            ("{\"values\":[ \"abc\", \"def\" ]}", CollectionBean.class);
        List<String> l = result._values;
        assertEquals(2, l.size());
        assertEquals("abc", l.get(0));
        assertEquals("def", l.get(1));
    }

// com.fasterxml.jackson.databind.deser.TestSetterlessProperties::testSimpleSetterlessCollectionFailure
    public void testSimpleSetterlessCollectionFailure()
        throws Exception
    {
        ObjectMapper m = new ObjectMapper();
        
        assertTrue(m.isEnabled(MapperFeature.USE_GETTERS_AS_SETTERS));
        m.configure(MapperFeature.USE_GETTERS_AS_SETTERS, false);
        assertFalse(m.isEnabled(MapperFeature.USE_GETTERS_AS_SETTERS));

        
        try {
            m.readValue
                ("{\"values\":[ \"abc\", \"def\" ]}", CollectionBean.class);
            fail("Expected an exception");
        } catch (JsonMappingException e) {
            
            verifyException(e, "Unrecognized field");
        }
    }

// com.fasterxml.jackson.databind.deser.TestSetterlessProperties::testSimpleSetterlessMapOk
    public void testSimpleSetterlessMapOk()
        throws Exception
    {
        MapBean result = new ObjectMapper().readValue
            ("{\"values\":{ \"a\": 15, \"b\" : -3 }}", MapBean.class);
        Map<String,Integer> m = result._values;
        assertEquals(2, m.size());
        assertEquals(Integer.valueOf(15), m.get("a"));
        assertEquals(Integer.valueOf(-3), m.get("b"));
    }

// com.fasterxml.jackson.databind.deser.TestSetterlessProperties::testSimpleSetterlessMapFailure
    public void testSimpleSetterlessMapFailure()
        throws Exception
    {
        ObjectMapper m = new ObjectMapper();
        m.configure(MapperFeature.USE_GETTERS_AS_SETTERS, false);
        
        try {
            m.readValue
                ("{\"values\":{ \"a\":3 }}", MapBean.class);
            fail("Expected an exception");
        } catch (JsonMappingException e) {
            verifyException(e, "Unrecognized field");
        }
    }

// com.fasterxml.jackson.databind.deser.TestSetterlessProperties::testSetterlessPrecedence
    public void testSetterlessPrecedence() throws Exception
    {
        ObjectMapper m = new ObjectMapper();
        m.configure(MapperFeature.USE_GETTERS_AS_SETTERS, true);
        Dual value = m.readValue("{\"list\":[1,2,3]}, valueType)", Dual.class);
        assertNotNull(value);
        assertEquals(3, value.values.size());
    }

// com.fasterxml.jackson.databind.deser.TestSimpleAtomicTypes::testAtomicBoolean
    public void testAtomicBoolean() throws Exception
    {
        AtomicBoolean b = MAPPER.readValue("true", AtomicBoolean.class);
        assertTrue(b.get());
    }

// com.fasterxml.jackson.databind.deser.TestSimpleAtomicTypes::testAtomicInt
    public void testAtomicInt() throws Exception
    {
        AtomicInteger value = MAPPER.readValue("13", AtomicInteger.class);
        assertEquals(13, value.get());
    }

// com.fasterxml.jackson.databind.deser.TestSimpleAtomicTypes::testAtomicLong
    public void testAtomicLong() throws Exception
    {
        AtomicLong value = MAPPER.readValue("12345678901", AtomicLong.class);
        assertEquals(12345678901L, value.get());
    }

// com.fasterxml.jackson.databind.deser.TestSimpleAtomicTypes::testAtomicReference
    public void testAtomicReference() throws Exception
    {
        AtomicReference<long[]> value = MAPPER.readValue("[1,2]",
                new com.fasterxml.jackson.core.type.TypeReference<AtomicReference<long[]>>() { });
        Object ob = value.get();
        assertNotNull(ob);
        assertEquals(long[].class, ob.getClass());
        long[] longs = (long[]) ob;
        assertNotNull(longs);
        assertEquals(2, longs.length);
        assertEquals(1, longs[0]);
        assertEquals(2, longs[1]);
    }

// com.fasterxml.jackson.databind.deser.TestSimpleAtomicTypes::testPolymorphicAtomicReference
    public void testPolymorphicAtomicReference() throws Exception
    {
        RefWrapper input = new RefWrapper(13);
        String json = MAPPER.writeValueAsString(input);
        
        RefWrapper result = MAPPER.readValue(json, RefWrapper.class);
        assertNotNull(result.w);
        Object ob = result.w.get();
        assertEquals(Impl.class, ob.getClass());
        assertEquals(13, ((Impl) ob).value);
    }

// com.fasterxml.jackson.databind.deser.TestSimpleTypes::testBooleanPrimitive
    public void testBooleanPrimitive() throws Exception
    {
        
        BooleanBean result = MAPPER.readValue(new StringReader("{\"v\":true}"), BooleanBean.class);
        assertTrue(result._v);
        
        result = MAPPER.readValue(new StringReader("{\"v\":null}"), BooleanBean.class);
        assertNotNull(result);
        assertFalse(result._v);

        
        boolean[] array = MAPPER.readValue(new StringReader("[ null ]"), boolean[].class);
        assertNotNull(array);
        assertEquals(1, array.length);
        assertFalse(array[0]);
        
        
        final ObjectMapper mapper = new ObjectMapper();
        mapper.enable(DeserializationFeature.UNWRAP_SINGLE_VALUE_ARRAYS);
        result = mapper.readValue(new StringReader("{\"v\":[true]}"), BooleanBean.class);
        assertTrue(result._v);
        
        try {
            mapper.readValue(new StringReader("[{\"v\":[true,true]}]"), BooleanBean.class);
            fail("Did not throw exception while reading a value from a multi value array with UNWRAP_SINGLE_VALUE_ARRAY feature enabled");
        } catch (JsonMappingException exp) {
            
        }
        
        result = mapper.readValue(new StringReader("{\"v\":[null]}"), BooleanBean.class);
        assertNotNull(result);
        assertFalse(result._v);
        
        result = mapper.readValue(new StringReader("[{\"v\":[null]}]"), BooleanBean.class);
        assertNotNull(result);
        assertFalse(result._v);
        
        array = mapper.readValue(new StringReader("[ [ null ] ]"), boolean[].class);
        assertNotNull(array);
        assertEquals(1, array.length);
        assertFalse(array[0]);
    }

// com.fasterxml.jackson.databind.deser.TestSimpleTypes::testIntPrimitive
    public void testIntPrimitive() throws Exception
    {
        
        IntBean result = MAPPER.readValue(new StringReader("{\"v\":3}"), IntBean.class);
        assertEquals(3, result._v);
        
        result = MAPPER.readValue(new StringReader("{\"v\":null}"), IntBean.class);
        assertNotNull(result);
        assertEquals(0, result._v);

        
        int[] array = MAPPER.readValue(new StringReader("[ null ]"), int[].class);
        assertNotNull(array);
        assertEquals(1, array.length);
        assertEquals(0, array[0]);
        
        
        final ObjectMapper mapper = new ObjectMapper();
        mapper.disable(DeserializationFeature.UNWRAP_SINGLE_VALUE_ARRAYS);
        try {
            mapper.readValue(new StringReader("{\"v\":[3]}"), IntBean.class);
            fail("Did not throw exception when reading a value from a single value array with the UNWRAP_SINGLE_VALUE_ARRAYS feature disabled");
        } catch (JsonMappingException exp) {
            
        }
        
        mapper.enable(DeserializationFeature.UNWRAP_SINGLE_VALUE_ARRAYS);
        
        result = mapper.readValue(new StringReader("{\"v\":[3]}"), IntBean.class);
        assertEquals(3, result._v);
        
        result = mapper.readValue(new StringReader("[{\"v\":[3]}]"), IntBean.class);
        assertEquals(3, result._v);
        
        try {
            mapper.readValue(new StringReader("[{\"v\":[3,3]}]"), IntBean.class);
            fail("Did not throw exception while reading a value from a multi value array with UNWRAP_SINGLE_VALUE_ARRAY feature enabled");
        } catch (JsonMappingException exp) {
            
        }
        
        result = mapper.readValue(new StringReader("{\"v\":[null]}"), IntBean.class);
        assertNotNull(result);
        assertEquals(0, result._v);

        array = mapper.readValue(new StringReader("[ [ null ] ]"), int[].class);
        assertNotNull(array);
        assertEquals(1, array.length);
        assertEquals(0, array[0]);
    }

// com.fasterxml.jackson.databind.deser.TestSimpleTypes::testDoublePrimitive
    public void testDoublePrimitive() throws Exception
    {
        
        
        double value = 0.016;
        DoubleBean result = MAPPER.readValue(new StringReader("{\"v\":"+value+"}"), DoubleBean.class);
        assertEquals(value, result._v);
        
        result = MAPPER.readValue(new StringReader("{\"v\":null}"), DoubleBean.class);
        assertNotNull(result);
        assertEquals(0.0, result._v);

        
        double[] array = MAPPER.readValue(new StringReader("[ null ]"), double[].class);
        assertNotNull(array);
        assertEquals(1, array.length);
        assertEquals(0.0, array[0]);
        
        
        final ObjectMapper mapper = new ObjectMapper();
        mapper.disable(DeserializationFeature.UNWRAP_SINGLE_VALUE_ARRAYS);
        try {
            mapper.readValue(new StringReader("{\"v\":[" + value + "]}"), DoubleBean.class);
            fail("Did not throw exception when reading a value from a single value array with the UNWRAP_SINGLE_VALUE_ARRAYS feature disabled");
        } catch (JsonMappingException exp) {
            
        }
        
        mapper.enable(DeserializationFeature.UNWRAP_SINGLE_VALUE_ARRAYS);
        
        result = mapper.readValue(new StringReader("{\"v\":[" + value + "]}"), DoubleBean.class);
        assertEquals(value, result._v);
        
        result = mapper.readValue(new StringReader("[{\"v\":[" + value + "]}]"), DoubleBean.class);
        assertEquals(value, result._v);
        
        try {
            mapper.readValue(new StringReader("[{\"v\":[" + value + "," + value + "]}]"), DoubleBean.class);
            fail("Did not throw exception while reading a value from a multi value array with UNWRAP_SINGLE_VALUE_ARRAY feature enabled");
        } catch (JsonMappingException exp) {
            
        }
        
        result = mapper.readValue(new StringReader("{\"v\":[null]}"), DoubleBean.class);
        assertNotNull(result);
        assertEquals(0d, result._v);

        array = mapper.readValue(new StringReader("[ [ null ] ]"), double[].class);
        assertNotNull(array);
        assertEquals(1, array.length);
        assertEquals(0d, array[0]);
    }

// com.fasterxml.jackson.databind.deser.TestSimpleTypes::testDoublePrimitiveNonNumeric
    public void testDoublePrimitiveNonNumeric() throws Exception
    {
        
        
        double value = Double.POSITIVE_INFINITY;
        DoubleBean result = MAPPER.readValue(new StringReader("{\"v\":\""+value+"\"}"), DoubleBean.class);
        assertEquals(value, result._v);
        
        
        double[] array = MAPPER.readValue(new StringReader("[ \"Infinity\" ]"), double[].class);
        assertNotNull(array);
        assertEquals(1, array.length);
        assertEquals(Double.POSITIVE_INFINITY, array[0]);
    }

// com.fasterxml.jackson.databind.deser.TestSimpleTypes::testFloatPrimitiveNonNumeric
    public void testFloatPrimitiveNonNumeric() throws Exception
    {
        
        float value = Float.POSITIVE_INFINITY;
        FloatBean result = MAPPER.readValue(new StringReader("{\"v\":\""+value+"\"}"), FloatBean.class);
        assertEquals(value, result._v);
        
        
        float[] array = MAPPER.readValue(new StringReader("[ \"Infinity\" ]"), float[].class);
        assertNotNull(array);
        assertEquals(1, array.length);
        assertEquals(Float.POSITIVE_INFINITY, array[0]);
    }

// com.fasterxml.jackson.databind.deser.TestSimpleTypes::testIntWithOverride
    public void testIntWithOverride() throws Exception
    {
        IntBean2 result = MAPPER.readValue(new StringReader("{\"v\":8}"), IntBean2.class);
        assertEquals(9, result._v);
    }

// com.fasterxml.jackson.databind.deser.TestSimpleTypes::testBooleanWrapper
    public void testBooleanWrapper() throws Exception
    {
        Boolean result = MAPPER.readValue(new StringReader("true"), Boolean.class);
        assertEquals(Boolean.TRUE, result);
        result = MAPPER.readValue(new StringReader("false"), Boolean.class);
        assertEquals(Boolean.FALSE, result);

        
        result = MAPPER.readValue(new StringReader("0"), Boolean.class);
        assertEquals(Boolean.FALSE, result);
        result = MAPPER.readValue(new StringReader("1"), Boolean.class);
        assertEquals(Boolean.TRUE, result);
    }

// com.fasterxml.jackson.databind.deser.TestSimpleTypes::testByteWrapper
    public void testByteWrapper() throws Exception
    {
        Byte result = MAPPER.readValue(new StringReader("   -42\t"), Byte.class);
        assertEquals(Byte.valueOf((byte)-42), result);

        
        result = MAPPER.readValue(new StringReader(" \"-12\""), Byte.class);
        assertEquals(Byte.valueOf((byte)-12), result);

        result = MAPPER.readValue(new StringReader(" 39.07"), Byte.class);
        assertEquals(Byte.valueOf((byte)39), result);
    }

// com.fasterxml.jackson.databind.deser.TestSimpleTypes::testShortWrapper
    public void testShortWrapper() throws Exception
    {
        Short result = MAPPER.readValue(new StringReader("37"), Short.class);
        assertEquals(Short.valueOf((short)37), result);

        
        result = MAPPER.readValue(new StringReader(" \"-1009\""), Short.class);
        assertEquals(Short.valueOf((short)-1009), result);

        result = MAPPER.readValue(new StringReader("-12.9"), Short.class);
        assertEquals(Short.valueOf((short)-12), result);
    }

// com.fasterxml.jackson.databind.deser.TestSimpleTypes::testCharacterWrapper
    public void testCharacterWrapper() throws Exception
    {
        
        Character result = MAPPER.readValue(new StringReader("\"a\""), Character.class);
        assertEquals(Character.valueOf('a'), result);

        
        result = MAPPER.readValue(new StringReader(" "+((int) 'X')), Character.class);
        assertEquals(Character.valueOf('X'), result);
        
        final CharacterWrapperBean wrapper = MAPPER.readValue(new StringReader("{\"v\":null}"), CharacterWrapperBean.class);
        assertNotNull(wrapper);
        assertNull(wrapper.getV());
        
        final ObjectMapper mapper = new ObjectMapper();
        try {
            mapper.enable(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES);
            mapper.readValue("{\"v\":null}", CharacterBean.class);
            fail("Attempting to deserialize a 'null' JSON reference into a 'char' property did not throw an exception");
        } catch (JsonMappingException exp) {
            
        }
        
        mapper.disable(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES);  
        final CharacterBean charBean = MAPPER.readValue(new StringReader("{\"v\":null}"), CharacterBean.class);
        assertNotNull(wrapper);
        assertEquals('\u0000', charBean.getV());
    }

// com.fasterxml.jackson.databind.deser.TestSimpleTypes::testIntWrapper
    public void testIntWrapper() throws Exception
    {
        Integer result = MAPPER.readValue(new StringReader("   -42\t"), Integer.class);
        assertEquals(Integer.valueOf(-42), result);

        
        result = MAPPER.readValue(new StringReader(" \"-1200\""), Integer.class);
        assertEquals(Integer.valueOf(-1200), result);

        result = MAPPER.readValue(new StringReader(" 39.07"), Integer.class);
        assertEquals(Integer.valueOf(39), result);
    }

// com.fasterxml.jackson.databind.deser.TestSimpleTypes::testLongWrapper
    public void testLongWrapper() throws Exception
    {
        Long result = MAPPER.readValue(new StringReader("12345678901"), Long.class);
        assertEquals(Long.valueOf(12345678901L), result);

        
        result = MAPPER.readValue(new StringReader(" \"-9876\""), Long.class);
        assertEquals(Long.valueOf(-9876), result);

        result = MAPPER.readValue(new StringReader("1918.3"), Long.class);
        assertEquals(Long.valueOf(1918), result);
    }

// com.fasterxml.jackson.databind.deser.TestSimpleTypes::testFloatWrapper
    public void testFloatWrapper() throws Exception
    {
        
        String[] STRS = new String[] {
            "1.0", "0.0", "-0.3", "0.7", "42.012", "-999.0", NAN_STRING
        };

        for (String str : STRS) {
            Float exp = Float.valueOf(str);
            Float result;

            if (NAN_STRING != str) {
                
                result = MAPPER.readValue(new StringReader(str), Float.class);
                assertEquals(exp, result);
            }

            
            result = MAPPER.readValue(new StringReader(" \""+str+"\""), Float.class);
            assertEquals(exp, result);
        }
    }

// com.fasterxml.jackson.databind.deser.TestSimpleTypes::testDoubleWrapper
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
            
            result = MAPPER.readValue(new StringReader(" \""+str+"\""), Double.class);
            assertEquals(exp, result);
        }
    }

// com.fasterxml.jackson.databind.deser.TestSimpleTypes::testBase64Variants
    public void testBase64Variants() throws Exception
    {
        final byte[] INPUT = "abcdefghijklmnopqrstuvwxyz1234567890abcdefghijklmnopqrstuvwxyz1234567890X".getBytes("UTF-8");
        
        
        Assert.assertArrayEquals(INPUT, MAPPER.readValue(
                quote("YWJjZGVmZ2hpamtsbW5vcHFyc3R1dnd4eXoxMjM0NTY3ODkwYWJjZGVmZ2hpamtsbW5vcHFyc3R1dnd4eXoxMjM0NTY3ODkwWA=="),
                byte[].class));
        ObjectReader reader = MAPPER.reader(byte[].class);
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

// com.fasterxml.jackson.databind.deser.TestSimpleTypes::testSingleString
    public void testSingleString() throws Exception
    {
        String value = "FOO!";
        String result = MAPPER.readValue(new StringReader("\""+value+"\""), String.class);
        assertEquals(value, result);
    }

// com.fasterxml.jackson.databind.deser.TestSimpleTypes::testSingleStringWrapped
    public void testSingleStringWrapped() throws Exception
    {
        final ObjectMapper mapper = new ObjectMapper();
        mapper.disable(DeserializationFeature.UNWRAP_SINGLE_VALUE_ARRAYS);
        
        String value = "FOO!";
        try {
            mapper.readValue(new StringReader("[\""+value+"\"]"), String.class);
            fail("Exception not thrown when attempting to unwrap a single value 'String' array into a simple String");
        } catch (JsonMappingException exp) {
            
        }
        
        mapper.enable(DeserializationFeature.UNWRAP_SINGLE_VALUE_ARRAYS);
        
        try {
            mapper.readValue(new StringReader("[\""+value+"\",\""+value+"\"]"), String.class);
            fail("Exception not thrown when attempting to unwrap a single value 'String' array that contained more than one value into a simple String");
        } catch (JsonMappingException exp) {
            
        }
        
        String result = mapper.readValue(new StringReader("[\""+value+"\"]"), String.class);
        assertEquals(value, result);
    }

// com.fasterxml.jackson.databind.deser.TestSimpleTypes::testNull
    public void testNull() throws Exception
    {
        
        Object result = MAPPER.readValue("   null", Object.class);
        assertNull(result);
    }

// com.fasterxml.jackson.databind.deser.TestSimpleTypes::testClass
    public void testClass() throws Exception
    {
        final ObjectMapper mapper = new ObjectMapper();        
        mapper.disable(DeserializationFeature.UNWRAP_SINGLE_VALUE_ARRAYS);        
        
        Class<?> result = mapper.readValue(quote(String.class.getName()), Class.class);
        assertEquals(String.class, result);
        
        
        try {
            mapper.readValue("[" + quote(String.class.getName()) + "]", Class.class);
            fail("Did not throw exception when UNWRAP_SINGLE_VALUE_ARRAYS feature was disabled and attempted to read a Class array containing one element");
        } catch (JsonMappingException exp) {
            
        }
        
        mapper.enable(DeserializationFeature.UNWRAP_SINGLE_VALUE_ARRAYS);
        
        try {
           mapper.readValue("[" + quote(Object.class.getName()) + "," + quote(Object.class.getName()) +"]", Class.class); 
           fail("Did not throw exception when UNWRAP_SINGLE_VALUE_ARRAYS feature was enabled and attempted to read a Class array containing two elements");
        } catch (JsonMappingException exp) {
            
        }               
        result = mapper.readValue("[" + quote(String.class.getName()) + "]", Class.class);
        assertEquals(String.class, result);
    }

// com.fasterxml.jackson.databind.deser.TestSimpleTypes::testBigDecimal
    public void testBigDecimal() throws Exception
    {
        final ObjectMapper mapper = objectMapper();
        mapper.disable(DeserializationFeature.UNWRAP_SINGLE_VALUE_ARRAYS);
        
        BigDecimal value = new BigDecimal("0.001");
        BigDecimal result = mapper.readValue(value.toString(), BigDecimal.class);
        assertEquals(value, result);
        
        
        try {
            mapper.readValue("[" + value.toString() + "]", BigDecimal.class);
            fail("Exception was not thrown when attempting to read a single value array of BigDecimal when UNWRAP_SINGLE_VALUE_ARRAYS feature is disabled");
        } catch (JsonMappingException exp) {
            
        }
        
        mapper.enable(DeserializationFeature.UNWRAP_SINGLE_VALUE_ARRAYS);
        result = mapper.readValue("[" + value.toString() + "]", BigDecimal.class);
        assertEquals(value, result);
        
        try {
            mapper.readValue("[" + value.toString() + "," + value.toString() + "]", BigDecimal.class);
            fail("Exception was not thrown when attempting to read a muti value array of BigDecimal when UNWRAP_SINGLE_VALUE_ARRAYS feature is enabled");
        } catch (JsonMappingException exp) {
            
        }
    }

// com.fasterxml.jackson.databind.deser.TestSimpleTypes::testBigInteger
    public void testBigInteger() throws Exception
    {
        final ObjectMapper mapper = objectMapper();
        mapper.disable(DeserializationFeature.UNWRAP_SINGLE_VALUE_ARRAYS);
        
        BigInteger value = new BigInteger("-1234567890123456789012345567809");
        BigInteger result = mapper.readValue(new StringReader(value.toString()), BigInteger.class);
        assertEquals(value, result);
        
        
        try {
            mapper.readValue("[" + value.toString() + "]", BigInteger.class);
            fail("Exception was not thrown when attempting to read a single value array of BigInteger when UNWRAP_SINGLE_VALUE_ARRAYS feature is disabled");
        } catch (JsonMappingException exp) {
            
        }
        
        mapper.enable(DeserializationFeature.UNWRAP_SINGLE_VALUE_ARRAYS);
        result = mapper.readValue("[" + value.toString() + "]", BigInteger.class);
        assertEquals(value, result);
        
        try {
            mapper.readValue("[" + value.toString() + "," + value.toString() + "]", BigInteger.class);
            fail("Exception was not thrown when attempting to read a muti value array of BigInteger when UNWRAP_SINGLE_VALUE_ARRAYS feature is enabled");
        } catch (JsonMappingException exp) {
            
        }        
    }

// com.fasterxml.jackson.databind.deser.TestSimpleTypes::testUUID
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
            
            try {
                mapper.readValue("[" + quote(value) + "]", UUID.class);
                fail("Exception was not thrown when UNWRAP_SINGLE_VALUE_ARRAYS is disabled and attempted to read a single value array as a single element");
            } catch (JsonMappingException exp) {
                
            }
            
            mapper.enable(DeserializationFeature.UNWRAP_SINGLE_VALUE_ARRAYS);
            
            assertEquals(uuid,
                    mapper.readValue("[" + quote(value) + "]", UUID.class));
            
            try {
                mapper.readValue("[" + quote(value) + "," + quote(value) + "]", UUID.class);
                fail("Exception was not thrown when UNWRAP_SINGLE_VALUE_ARRAYS is enabled and attempted to read a multi value array as a single element");
            } catch (JsonMappingException exp) {
                
            }
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

// com.fasterxml.jackson.databind.deser.TestSimpleTypes::testUUIDAux
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

// com.fasterxml.jackson.databind.deser.TestSimpleTypes::testURL
    public void testURL() throws Exception
    {
        URL value = new URL("http://foo.com");
        assertEquals(value, MAPPER.readValue("\""+value.toString()+"\"", URL.class));

        
        TokenBuffer buf = new TokenBuffer(null, false);
        buf.writeObject(null);
        assertNull(MAPPER.readValue(buf.asParser(), URL.class));
        buf.close();

        
        buf = new TokenBuffer(null, false);
        buf.writeObject(value);
        assertSame(value, MAPPER.readValue(buf.asParser(), URL.class));
        buf.close();
    }

// com.fasterxml.jackson.databind.deser.TestSimpleTypes::testURI
    public void testURI() throws Exception
    {
        final ObjectMapper mapper = new ObjectMapper();
        
        URI value = new URI("http://foo.com");
        assertEquals(value, mapper.readValue("\""+value.toString()+"\"", URI.class));
        
        
        mapper.disable(DeserializationFeature.UNWRAP_SINGLE_VALUE_ARRAYS);
        try {            
            assertEquals(value, mapper.readValue("[\""+value.toString()+"\"]", URI.class));
            fail("Did not throw exception for single value array when UNWRAP_SINGLE_VALUE_ARRAYS is disabled");
        } catch (JsonMappingException exp) {
            
        }
        
        try {
            assertEquals(value, mapper.readValue("[\""+value.toString()+"\",\""+value.toString()+"\"]", URI.class));
            fail("Did not throw exception for single value array when there were multiple values");
        } catch (JsonMappingException exp) {
            
        }
        
        mapper.enable(DeserializationFeature.UNWRAP_SINGLE_VALUE_ARRAYS);
        assertEquals(value, mapper.readValue("[\""+value.toString()+"\"]", URI.class));

        
        value = mapper.readValue(quote(""), URI.class);
        assertNotNull(value);
        assertEquals(URI.create(""), value);
    }

// com.fasterxml.jackson.databind.deser.TestSimpleTypes::testSequenceOfInts
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

// com.fasterxml.jackson.databind.deser.TestStatics::testSimpleIgnore
    public void testSimpleIgnore() throws Exception
    {
        ObjectMapper m = new ObjectMapper();
        
        Bean result = m.readValue("{ \"x\":3}", Bean.class);
        assertEquals(3, result._x);
    }

// com.fasterxml.jackson.databind.deser.TestTimestampDeserialization::testTimestampUtil
    public void testTimestampUtil() throws Exception
    {
        long now = 123456789L;
        java.sql.Timestamp value = new java.sql.Timestamp(now);

        
        assertEquals(value, new ObjectMapper().readValue(""+now, java.sql.Timestamp.class));

        String dateStr = serializeTimestampAsString(value);
        java.sql.Timestamp result = new ObjectMapper().readValue("\""+dateStr+"\"", java.sql.Timestamp.class);

        assertEquals("Date: expect "+value+" ("+value.getTime()+"), got "+result+" ("+result.getTime()+")", value.getTime(), result.getTime());
    }

// com.fasterxml.jackson.databind.deser.TestTimestampDeserialization::testTimestampUtilSingleElementArray
    public void testTimestampUtilSingleElementArray() throws Exception
    {
        final ObjectMapper mapper = new ObjectMapper();
        mapper.enable(DeserializationFeature.UNWRAP_SINGLE_VALUE_ARRAYS);
        
        long now = System.currentTimeMillis();
        java.sql.Timestamp value = new java.sql.Timestamp(now);

        
        assertEquals(value, mapper.readValue("["+now+"]", java.sql.Timestamp.class));

        String dateStr = serializeTimestampAsString(value);
        java.sql.Timestamp result = mapper.readValue("[\""+dateStr+"\"]", java.sql.Timestamp.class);

        assertEquals("Date: expect "+value+" ("+value.getTime()+"), got "+result+" ("+result.getTime()+")", value.getTime(), result.getTime());
    }

// com.fasterxml.jackson.databind.deser.TestUntypedDeserialization::testSampleDoc
    public void testSampleDoc() throws Exception
    {
        final String JSON = SAMPLE_DOC_JSON_SPEC;

        
        Object root = new ObjectMapper().readValue(JSON, Object.class);

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

// com.fasterxml.jackson.databind.deser.TestUntypedDeserialization::testNestedUntypes
    public void testNestedUntypes() throws IOException
    {
        
        final ObjectMapper mapper = new ObjectMapper();
        Object root = mapper.readValue(aposToQuotes("{'a':3,'b':[1,2]}"),
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

// com.fasterxml.jackson.databind.deser.TestUntypedDeserialization::testObjectSerializeWithLong
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

// com.fasterxml.jackson.databind.deser.TestUntypedDeserialization::testUntypedWithCustomScalarDesers
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

// com.fasterxml.jackson.databind.deser.TestUntypedDeserialization::testUntypedWithListDeser
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

// com.fasterxml.jackson.databind.deser.TestUntypedDeserialization::testUntypedWithMapDeser
    public void testUntypedWithMapDeser() throws IOException
    {
        SimpleModule m = new SimpleModule("test-module");
        m.addDeserializer(Map.class, new MapDeserializer());
        final ObjectMapper mapper = new ObjectMapper()
            .registerModule(m);

        
        Object ob = mapper.readValue("{\"a\":true}", Object.class);
        assertTrue(ob instanceof Map<?,?>);
        Map<?,?> map = (Map<?,?>) ob;
        assertEquals(1, map.size());
        assertEquals("Ytrue", map.get("a"));
    }

// com.fasterxml.jackson.databind.deser.TestValueAnnotations::testOverrideClassValid
    public void testOverrideClassValid() throws Exception
    {
        ObjectMapper m = new ObjectMapper();
        CollectionHolder result = m.readValue
            ("{ \"strings\" : [ \"test\" ] }", CollectionHolder.class);

        Collection<String> strs = result._strings;
        assertEquals(1, strs.size());
        assertEquals(TreeSet.class, strs.getClass());
        assertEquals("test", strs.iterator().next());
    }

// com.fasterxml.jackson.databind.deser.TestValueAnnotations::testOverrideMapValid
    public void testOverrideMapValid() throws Exception
    {
        ObjectMapper m = new ObjectMapper();
        
        MapHolder result = m.readValue
            ("{ \"strings\" :  { \"a\" : 3 } }", MapHolder.class);

        Map<String,String> strs = result._data;
        assertEquals(1, strs.size());
        assertEquals(TreeMap.class, strs.getClass());
        String value = strs.get("a");
        assertEquals("3", value);
    }

// com.fasterxml.jackson.databind.deser.TestValueAnnotations::testOverrideArrayClass
    public void testOverrideArrayClass() throws Exception
    {
        ObjectMapper m = new ObjectMapper();
        ArrayHolder result = m.readValue
            ("{ \"strings\" : [ \"test\" ] }", ArrayHolder.class);

        String[] strs = result._strings;
        assertEquals(1, strs.length);
        assertEquals(String[].class, strs.getClass());
        assertEquals("test", strs[0]);
    }

// com.fasterxml.jackson.databind.deser.TestValueAnnotations::testOverrideClassInvalid
    public void testOverrideClassInvalid() throws Exception
    {
        
        try {
            BrokenCollectionHolder result = new ObjectMapper().readValue
                ("{ \"strings\" : [ ] }", BrokenCollectionHolder.class);
            fail("Expected a failure, but got results: "+result);
        } catch (JsonMappingException jme) {
            verifyException(jme, "is not assignable to");
        }
    }

// com.fasterxml.jackson.databind.deser.TestValueAnnotations::testRootInterfaceAs
    public void testRootInterfaceAs() throws Exception
    {
        RootInterface value = new ObjectMapper().readValue("{\"a\":\"abc\" }", RootInterface.class);
        assertTrue(value instanceof RootInterfaceImpl);
        assertEquals("abc", value.getA());
    }

// com.fasterxml.jackson.databind.deser.TestValueAnnotations::testRootInterfaceUsing
    public void testRootInterfaceUsing() throws Exception
    {
        RootString value = new ObjectMapper().readValue("\"xxx\"", RootString.class);
        assertTrue(value instanceof RootString);
        assertEquals("xxx", value.contents());
    }

// com.fasterxml.jackson.databind.deser.TestValueAnnotations::testRootListAs
    public void testRootListAs() throws Exception
    {
        RootMap value = new ObjectMapper().readValue("{\"a\":\"b\"}", RootMap.class);
        assertEquals(1, value.size());
        Object v2 = value.get("a");
        assertEquals(RootStringImpl.class, v2.getClass());
        assertEquals("b", ((RootString) v2).contents());
    }

// com.fasterxml.jackson.databind.deser.TestValueAnnotations::testRootMapAs
    public void testRootMapAs() throws Exception
    {
        RootList value = new ObjectMapper().readValue("[ \"c\" ]", RootList.class);
        assertEquals(1, value.size());
        Object v2 = value.get(0);
        assertEquals(RootStringImpl.class, v2.getClass());
        assertEquals("c", ((RootString) v2).contents());
    }

// com.fasterxml.jackson.databind.deser.TestValueAnnotations::testOverrideKeyClassValid
	public void testOverrideKeyClassValid() throws Exception
    {
        ObjectMapper m = new ObjectMapper();
        MapKeyHolder result = m.readValue("{ \"map\" : { \"xxx\" : \"yyy\" } }", MapKeyHolder.class);
        Map<StringWrapper, String> map = (Map<StringWrapper,String>)(Map<?,?>)result._map;
        assertEquals(1, map.size());
        Map.Entry<StringWrapper, String> en = map.entrySet().iterator().next();

        StringWrapper key = en.getKey();
        assertEquals(StringWrapper.class, key.getClass());
        assertEquals("xxx", key._string);
        assertEquals("yyy", en.getValue());
    }

// com.fasterxml.jackson.databind.deser.TestValueAnnotations::testOverrideKeyClassInvalid
    public void testOverrideKeyClassInvalid() throws Exception
    {
        
        try {
            BrokenMapKeyHolder result = new ObjectMapper().readValue
                ("{ \"123\" : \"xxx\" }", BrokenMapKeyHolder.class);
            fail("Expected a failure, but got results: "+result);
        } catch (JsonMappingException jme) {
            verifyException(jme, "is not assignable to");
        }
    }

// com.fasterxml.jackson.databind.deser.TestValueAnnotations::testOverrideContentClassValid
	public void testOverrideContentClassValid() throws Exception
    {
        ObjectMapper m = new ObjectMapper();
        ListContentHolder result = m.readValue("{ \"list\" : [ \"abc\" ] }", ListContentHolder.class);
        List<StringWrapper> list = (List<StringWrapper>)result._list;
        assertEquals(1, list.size());
        Object value = list.get(0);
        assertEquals(StringWrapper.class, value.getClass());
        assertEquals("abc", ((StringWrapper) value)._string);
    }

// com.fasterxml.jackson.databind.deser.TestValueAnnotations::testOverrideArrayContents
    public void testOverrideArrayContents() throws Exception
    {
        ObjectMapper m = new ObjectMapper();
        ArrayContentHolder result = m.readValue("{ \"data\" : [ 1, 2, 3 ] }",
                                                ArrayContentHolder.class);
        Object[] data = result._data;
        assertEquals(3, data.length);
        assertEquals(Long[].class, data.getClass());
        assertEquals(1L, data[0]);
        assertEquals(2L, data[1]);
        assertEquals(3L, data[2]);
    }

// com.fasterxml.jackson.databind.deser.TestValueAnnotations::testOverrideMapContents
    public void testOverrideMapContents() throws Exception
    {
        ObjectMapper m = new ObjectMapper();
        MapContentHolder result = m.readValue("{ \"map\" : { \"a\" : 9 } }",
                                                MapContentHolder.class);
        Map<Object,Object> map = result._map;
        assertEquals(1, map.size());
        Object ob = map.values().iterator().next();
        assertEquals(Integer.class, ob.getClass());
        assertEquals(Integer.valueOf(9), ob);
    }

// com.fasterxml.jackson.databind.ext.TestCoreXMLTypes::testQNameSer
    public void testQNameSer() throws Exception
    {
        QName qn = new QName("http://abc", "tag", "prefix");
        assertEquals(quote(qn.toString()), serializeAsString(qn));
    }

// com.fasterxml.jackson.databind.ext.TestCoreXMLTypes::testDurationSer
    public void testDurationSer() throws Exception
    {
        DatatypeFactory dtf = DatatypeFactory.newInstance();
        
        Duration dur = dtf.newDurationDayTime(false, 15, 19, 58, 1);
        assertEquals(quote(dur.toString()), serializeAsString(dur));
    }

// com.fasterxml.jackson.databind.ext.TestCoreXMLTypes::testXMLGregorianCalendarSerAndDeser
    public void testXMLGregorianCalendarSerAndDeser() throws Exception
    {
        DatatypeFactory dtf = DatatypeFactory.newInstance();
        XMLGregorianCalendar cal = dtf.newXMLGregorianCalendar
            (1974, 10, 10, 18, 15, 17, 123, 0);
        
        ObjectMapper mapper = new ObjectMapper();
        long timestamp = cal.toGregorianCalendar().getTimeInMillis();
        String numStr = String.valueOf(timestamp);
        assertEquals(numStr, mapper.writeValueAsString(cal));

        
        XMLGregorianCalendar calOut = mapper.readValue(numStr, XMLGregorianCalendar.class);
        assertNotNull(calOut);
        assertEquals(timestamp, calOut.toGregorianCalendar().getTimeInMillis());

        
        mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        
        String exp = cal.toXMLFormat();
        String act = mapper.writeValueAsString(cal);
        act = act.substring(1, act.length() - 1); 
        exp = removeZ(exp);
        act = removeZ(act);
        assertEquals(exp, act);
    }

// com.fasterxml.jackson.databind.ext.TestCoreXMLTypes::testDeserializerLoading
    public void testDeserializerLoading()
    {
        CoreXMLDeserializers sers = new CoreXMLDeserializers();
        TypeFactory f = TypeFactory.defaultInstance();
        sers.findBeanDeserializer(f.constructType(Duration.class), null, null);
        sers.findBeanDeserializer(f.constructType(XMLGregorianCalendar.class), null, null);
        sers.findBeanDeserializer(f.constructType(QName.class), null, null);
    }

// com.fasterxml.jackson.databind.ext.TestCoreXMLTypes::testQNameDeser
    public void testQNameDeser() throws Exception
    {
        QName qn = new QName("http://abc", "tag", "prefix");
        String qstr = qn.toString();
        ObjectMapper mapper = new ObjectMapper();
        assertEquals("Should deserialize to equal QName (exp serialization: '"+qstr+"')",
                     qn, mapper.readValue(quote(qstr), QName.class));
    }

// com.fasterxml.jackson.databind.ext.TestCoreXMLTypes::testCalendarDeser
    public void testCalendarDeser() throws Exception
    {
        DatatypeFactory dtf = DatatypeFactory.newInstance();
        XMLGregorianCalendar cal = dtf.newXMLGregorianCalendar
            (1974, 10, 10, 18, 15, 17, 123, 0);
        String exp = cal.toXMLFormat();
        assertEquals("Should deserialize to equal XMLGregorianCalendar ('"+exp+"')", cal,
                new ObjectMapper().readValue(quote(exp), XMLGregorianCalendar.class));
    }

// com.fasterxml.jackson.databind.ext.TestCoreXMLTypes::testDurationDeser
    public void testDurationDeser() throws Exception
    {
        DatatypeFactory dtf = DatatypeFactory.newInstance();
        
        Duration dur = dtf.newDurationDayTime(true, 27, 5, 15, 59);
        String exp = dur.toString();
        assertEquals("Should deserialize to equal Duration ('"+exp+"')", dur,
                new ObjectMapper().readValue(quote(exp), Duration.class));
    }

// com.fasterxml.jackson.databind.ext.TestDOM::testSerializeSimpleNonNS
    public void testSerializeSimpleNonNS() throws Exception
    {
        
        Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse
            (new InputSource(new StringReader(SIMPLE_XML)));
        assertNotNull(doc);
        ObjectMapper mapper = new ObjectMapper();
        
        String outputRaw = mapper.writeValueAsString(doc);
        
        String output = mapper.readValue(outputRaw, String.class);
        
        assertEquals(SIMPLE_XML, normalizeOutput(output));
    }

// com.fasterxml.jackson.databind.ext.TestDOM::testDeserializeNonNS
    public void testDeserializeNonNS() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        for (int i = 0; i < 2; ++i) {
            Document doc;

            if (i == 0) {
                
                doc = mapper.readValue(quote(SIMPLE_XML), Document.class);
            } else {
                
                Node node = mapper.readValue(quote(SIMPLE_XML), Node.class);
                doc = (Document) node;
            }
            Element root = doc.getDocumentElement();
            assertNotNull(root);
            
            assertEquals("root", root.getTagName());
            assertEquals("3", root.getAttribute("attr"));
            assertEquals(1, root.getAttributes().getLength());
            NodeList nodes = root.getChildNodes();
            assertEquals(2, nodes.getLength());
            Element leaf = (Element) nodes.item(0);
            assertEquals("leaf", leaf.getTagName());
            assertEquals(0, leaf.getAttributes().getLength());
            
            ProcessingInstruction pi = (ProcessingInstruction) nodes.item(1);
            assertEquals("proc", pi.getTarget());
            assertEquals("instr", pi.getData());
        }
    }

// com.fasterxml.jackson.databind.ext.TestDOM::testDeserializeNS
    public void testDeserializeNS() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        Document doc = mapper.readValue(quote(SIMPLE_XML_NS), Document.class);
        Element root = doc.getDocumentElement();
        assertNotNull(root);
        assertEquals("root", root.getTagName());
        
        String uri = root.getNamespaceURI();
        assertTrue((uri == null) || "".equals(uri));
        
        assertEquals(0, root.getChildNodes().getLength());
        
        assertEquals(2, root.getAttributes().getLength());
        assertEquals("abc", root.getAttributeNS("http://foo", "attr"));
    }

// com.fasterxml.jackson.databind.ext.TestJDK16OnlyTypes::test16Types
    public void test16Types() throws Exception
    {
        final ObjectMapper mapper = new ObjectMapper();
        Deque<?> dq = mapper.readValue("[1]", Deque.class);
        assertNotNull(dq);
        assertEquals(1, dq.size());
        assertTrue(dq instanceof Deque<?>);

        NavigableSet<?> ns = mapper.readValue("[ true ]", NavigableSet.class);
        assertEquals(1, ns.size());
        assertTrue(ns instanceof NavigableSet<?>);
    }

// com.fasterxml.jackson.databind.filter.TestAnyGetterFiltering::testAnyGetterFiltering
    public void testAnyGetterFiltering() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        FilterProvider prov = new SimpleFilterProvider().addFilter("anyFilter",
                SimpleBeanPropertyFilter.filterOutAllExcept("b"));
        assertEquals("{\"b\":\"2\"}", mapper.writer(prov).writeValueAsString(new AnyBean()));
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

// com.fasterxml.jackson.databind.filter.TestIgnorePropsForSerialization::testIgnoreViaPropForUntyped
    public void testIgnoreViaPropForUntyped() throws Exception
    {
        assertEquals("{\"value\":{\"z\":3}}",
                MAPPER.writeValueAsString(new WrapperWithPropIgnoreUntyped()));
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

// com.fasterxml.jackson.databind.filter.TestJsonFilter::testSimpleInclusionFilter
    public void testSimpleInclusionFilter() throws Exception
    {
        FilterProvider prov = new SimpleFilterProvider().addFilter("RootFilter",
                SimpleBeanPropertyFilter.filterOutAllExcept("a"));
        assertEquals("{\"a\":\"a\"}", MAPPER.writer(prov).writeValueAsString(new Bean()));

        
        ObjectMapper mapper = new ObjectMapper();
        mapper.setFilters(prov);
        assertEquals("{\"a\":\"a\"}", mapper.writeValueAsString(new Bean()));
    }

// com.fasterxml.jackson.databind.filter.TestJsonFilter::testSimpleExclusionFilter
    public void testSimpleExclusionFilter() throws Exception
    {
        FilterProvider prov = new SimpleFilterProvider().addFilter("RootFilter",
                SimpleBeanPropertyFilter.serializeAllExcept("a"));
        assertEquals("{\"b\":\"b\"}", MAPPER.writer(prov).writeValueAsString(new Bean()));
    }

// com.fasterxml.jackson.databind.filter.TestJsonFilter::testMissingFilter
    public void testMissingFilter() throws Exception
    {
        
        try {
            MAPPER.writeValueAsString(new Bean());
            fail("Should have failed without configured filter");
        } catch (JsonMappingException e) { 
            verifyException(e, "Can not resolve PropertyFilter with id 'RootFilter'");
        }
        
        
        SimpleFilterProvider fp = new SimpleFilterProvider().setFailOnUnknownId(false);
        ObjectMapper mapper = new ObjectMapper();
        mapper.setFilters(fp);
        String json = mapper.writeValueAsString(new Bean());
        assertEquals("{\"a\":\"a\",\"b\":\"b\"}", json);
    }

// com.fasterxml.jackson.databind.filter.TestJsonFilter::testDefaultFilter
    public void testDefaultFilter() throws Exception
    {
        FilterProvider prov = new SimpleFilterProvider().setDefaultFilter(SimpleBeanPropertyFilter.filterOutAllExcept("b"));
        assertEquals("{\"b\":\"b\"}", MAPPER.writer(prov).writeValueAsString(new Bean()));
    }

// com.fasterxml.jackson.databind.filter.TestJsonFilter::testIssue89
    public void testIssue89() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        Pod pod = new Pod();
        pod.username = "Bob";
        pod.userPassword = "s3cr3t!";

        String json = mapper.writeValueAsString(pod);

        assertEquals("{\"username\":\"Bob\"}", json);

        Pod pod2 = mapper.readValue("{\"username\":\"Bill\",\"user_password\":\"foo!\"}", Pod.class);
        assertEquals("Bill", pod2.username);
        assertEquals("foo!", pod2.userPassword);
    }

// com.fasterxml.jackson.databind.filter.TestJsonFilter::testFilterOnProperty
    public void testFilterOnProperty() throws Exception
    {
        FilterProvider prov = new SimpleFilterProvider()
            .addFilter("RootFilter", SimpleBeanPropertyFilter.filterOutAllExcept("a"))
            .addFilter("b", SimpleBeanPropertyFilter.filterOutAllExcept("b"));

        assertEquals("{\"first\":{\"a\":\"a\"},\"second\":{\"b\":\"b\"}}",
                MAPPER.writer(prov).writeValueAsString(new FilteredProps()));
    }

// com.fasterxml.jackson.databind.filter.TestMapFiltering::testMapFilteringViaProps
    public void testMapFilteringViaProps() throws Exception
    {
        FilterProvider prov = new SimpleFilterProvider().addFilter("filterX",
                SimpleBeanPropertyFilter.filterOutAllExcept("b"));
        String json = MAPPER.writer(prov).writeValueAsString(new MapBean());
        assertEquals(aposToQuotes("{'values':{'b':5}}"), json);
    }

// com.fasterxml.jackson.databind.filter.TestMapFiltering::testMapFilteringViaClass
    public void testMapFilteringViaClass() throws Exception
    {
        FilteredBean bean = new FilteredBean();
        bean.put("a", 4);
        bean.put("b", 3);
        FilterProvider prov = new SimpleFilterProvider().addFilter("filterForMaps",
                SimpleBeanPropertyFilter.filterOutAllExcept("b"));
        String json = MAPPER.writer(prov).writeValueAsString(bean);
        assertEquals(aposToQuotes("{'b':3}"), json);
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

// com.fasterxml.jackson.databind.filter.TestUnknownPropertyDeserialization::testUnknownHandlingDefault
    public void testUnknownHandlingDefault()
        throws Exception
    {
        try {
            MAPPER.readValue(new StringReader(JSON_UNKNOWN_FIELD), TestBean.class);
        } catch (JsonMappingException jex) {
            verifyException(jex, "Unrecognized field \"foo\"");
        }
    }

// com.fasterxml.jackson.databind.filter.TestUnknownPropertyDeserialization::testUnknownHandlingIgnoreWithHandler
    public void testUnknownHandlingIgnoreWithHandler()
        throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        mapper.clearProblemHandlers();
        mapper.addHandler(new MyHandler());
        TestBean result = mapper.readValue(new StringReader(JSON_UNKNOWN_FIELD), TestBean.class);
        assertNotNull(result);
        assertEquals(1, result._a);
        assertEquals(-1, result._b);
        assertEquals("foo:START_ARRAY", result._unknown);
    }

// com.fasterxml.jackson.databind.filter.TestUnknownPropertyDeserialization::testUnknownHandlingIgnoreWithHandlerAndObjectReader
    public void testUnknownHandlingIgnoreWithHandlerAndObjectReader()
        throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        mapper.clearProblemHandlers();
        TestBean result = mapper.reader(TestBean.class).withHandler(new MyHandler()).readValue(new StringReader(JSON_UNKNOWN_FIELD));
        assertNotNull(result);
        assertEquals(1, result._a);
        assertEquals(-1, result._b);
        assertEquals("foo:START_ARRAY", result._unknown);
    }

// com.fasterxml.jackson.databind.filter.TestUnknownPropertyDeserialization::testUnknownHandlingIgnoreWithFeature
    public void testUnknownHandlingIgnoreWithFeature()
        throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        TestBean result = null;
        try {
            result = mapper.readValue(new StringReader(JSON_UNKNOWN_FIELD), TestBean.class);
        } catch (JsonMappingException jex) {
            fail("Did not expect a problem, got: "+jex.getMessage());
        }
        assertNotNull(result);
        assertEquals(1, result._a);
        assertNull(result._unknown);
        assertEquals(-1, result._b);
    }

// com.fasterxml.jackson.databind.filter.TestUnknownPropertyDeserialization::testWithClassIgnore
    public void testWithClassIgnore()
        throws Exception
    {
        IgnoreSome result = MAPPER.readValue("{ \"a\":1,\"b\":2,\"c\":\"x\",\"d\":\"y\"}",
                IgnoreSome.class);
        
        assertEquals(1, result.a);
        assertEquals("y", result.d());
        
        assertEquals(0, result.b);
        assertNull(result.c());
    }

// com.fasterxml.jackson.databind.filter.TestUnknownPropertyDeserialization::testClassIgnoreWithMap
    public void testClassIgnoreWithMap() throws Exception
    {
        
        IgnoreMap result = MAPPER.readValue
            ("{ \"a\":[ 1],\n"
                +"\"b\":2,\n"
                +"\"c\": \"x\",\n"
                +"\"d\":false }", IgnoreMap.class);
        assertEquals(2, result.size());
        Object ob = result.get("b");
        assertEquals(Integer.class, ob.getClass());
        assertEquals(Integer.valueOf(2), ob);
        assertEquals("x", result.get("c"));
        assertFalse(result.containsKey("a"));
        assertFalse(result.containsKey("d"));
    }

// com.fasterxml.jackson.databind.filter.TestUnknownPropertyDeserialization::testClassWithIgnoreUnknown
    public void testClassWithIgnoreUnknown() throws Exception
    {
        IgnoreUnknown result = MAPPER.readValue
            ("{\"b\":3,\"c\":[1,2],\"x\":{ },\"a\":-3}", IgnoreUnknown.class);
        assertEquals(-3, result.a);
    }

// com.fasterxml.jackson.databind.filter.TestUnknownPropertyDeserialization::testClassWithUnknownAndIgnore
    public void testClassWithUnknownAndIgnore() throws Exception
    {
        
        ImplicitIgnores result = MAPPER.readValue
            ("{\"a\":1,\"b\":2,\"c\":3 }", ImplicitIgnores.class);
        assertEquals(3, result.c);

        
        try {
            MAPPER.readValue("{\"a\":1,\"b\":2,\"c\":3,\"d\":4 }", ImplicitIgnores.class);            
        } catch (JsonMappingException e) {
            verifyException(e, "Unrecognized field \"d\"");
        }
    }

// com.fasterxml.jackson.databind.filter.TestUnknownPropertyDeserialization::testPropertyIgnoral
    public void testPropertyIgnoral() throws Exception
    {
        XYZWrapper1 result = MAPPER.readValue("{\"value\":{\"y\":2,\"x\":1,\"z\":3}}", XYZWrapper1.class);
        assertEquals(2, result.value.y);
        assertEquals(3, result.value.z);
    }

// com.fasterxml.jackson.databind.filter.TestUnknownPropertyDeserialization::testPropertyIgnoralWithClass
    public void testPropertyIgnoralWithClass() throws Exception
    {
        XYZWrapper2 result = MAPPER.readValue("{\"value\":{\"y\":2,\"x\":1,\"z\":3}}", XYZWrapper2.class);
        assertEquals(1, result.value.x);
    }

// com.fasterxml.jackson.databind.filter.TestUnknownPropertyDeserialization::testPropertyIgnoralForMap
    public void testPropertyIgnoralForMap() throws Exception
    {
        MapWithoutX result = MAPPER.readValue("{\"values\":{\"x\":1,\"y\":2}}", MapWithoutX.class);
        assertNotNull(result.values);
        assertEquals(1, result.values.size());
        assertEquals(Integer.valueOf(2), result.values.get("y"));
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

// com.fasterxml.jackson.databind.interop.TestFormatDetection::testSimpleWithJSON
    public void testSimpleWithJSON() throws Exception
    {
        ObjectReader detecting = READER.withType(POJO.class);
        detecting = detecting.withFormatDetection(detecting);
        POJO pojo = detecting.readValue(utf8Bytes("{\"x\":1}"));
        assertNotNull(pojo);
        assertEquals(1, pojo.x);
    }

// com.fasterxml.jackson.databind.interop.TestFormatDetection::testInvalid
    public void testInvalid() throws Exception
    {
        ObjectReader detecting = READER.withType(POJO.class);
        detecting = detecting.withFormatDetection(detecting);
        try {
            detecting.readValue(utf8Bytes("<POJO><x>1</x></POJO>"));
            fail("Should have failed");
        } catch (JsonProcessingException e) {
            verifyException(e, "Can not detect format from input");
        }
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

// com.fasterxml.jackson.databind.introspect.TestBuilderMethods::testSimple
    public void testSimple()
    {
        POJOPropertiesCollector coll = collector(SimpleBuilder.class, "with");
        Map<String, POJOPropertyBuilder> props = coll.getPropertyMap();
        assertEquals(1, props.size());
        POJOPropertyBuilder prop = props.get("x");
        assertNotNull(prop);
        assertTrue(prop.hasField());
        assertFalse(prop.hasGetter());
        assertTrue(prop.hasSetter());
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

// com.fasterxml.jackson.databind.introspect.TestNameConflicts193And327::testIssue193
    public void testIssue193() throws Exception
    {
        String json = objectWriter().writeValueAsString(new Bean193(1, 2));
        assertNotNull(json);
    }

// com.fasterxml.jackson.databind.introspect.TestNameConflicts193And327::testNonConflict
    public void testNonConflict() throws Exception
    {
        String json = objectMapper().writeValueAsString(new BogusConflictBean());
        assertEquals(aposToQuotes("{'prop1':2,'prop2':1}"), json);
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
        for (Object[] pair : NAME_TRANSLATIONS) {
            String translatedJavaName = PropertyNamingStrategy.CAMEL_CASE_TO_LOWER_CASE_WITH_UNDERSCORES.nameForField(null, null,
                    (String) pair[0]);
            assertEquals((String) pair[1], translatedJavaName);
        }
    }

// com.fasterxml.jackson.databind.introspect.TestNamingStrategyStd::testLowerCaseTranslations
    public void testLowerCaseTranslations() throws Exception
    {
        
        String json = mapper.writeValueAsString(new PersonBean("Joe", "Sixpack", 42));
        assertEquals("{\"first_name\":\"Joe\",\"last_name\":\"Sixpack\",\"age\":42}", json);
        
        
        PersonBean result = mapper.readValue(json, PersonBean.class);
        assertEquals("Joe", result.firstName);
        assertEquals("Sixpack", result.lastName);
        assertEquals(42, result.age);
    }

// com.fasterxml.jackson.databind.introspect.TestNamingStrategyStd::testLowerCaseAcronymsTranslations
    public void testLowerCaseAcronymsTranslations() throws Exception
    {
        
        String json = mapper.writeValueAsString(new Acronyms("world wide web", "http://jackson.codehaus.org", "/path1/,/path2/"));
        assertEquals("{\"www\":\"world wide web\",\"some_url\":\"http://jackson.codehaus.org\",\"some_uris\":\"/path1/,/path2/\"}", json);
        
        
        Acronyms result = mapper.readValue(json, Acronyms.class);
        assertEquals("world wide web", result.WWW);
        assertEquals("http://jackson.codehaus.org", result.someURL);
        assertEquals("/path1/,/path2/", result.someURIs);
    }

// com.fasterxml.jackson.databind.introspect.TestNamingStrategyStd::testLowerCaseOtherNonStandardNamesTranslations
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

// com.fasterxml.jackson.databind.introspect.TestNamingStrategyStd::testLowerCaseUnchangedNames
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

// com.fasterxml.jackson.databind.introspect.TestNamingStrategyStd::testPascalCaseStandAlone
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

// com.fasterxml.jackson.databind.introspect.TestNamingStrategyStd::testIssue428PascalWithOverrides
    public void testIssue428PascalWithOverrides() throws Exception {

        String json = new ObjectMapper()
                            .setPropertyNamingStrategy(PropertyNamingStrategy.PASCAL_CASE_TO_CAMEL_CASE)
                            .writeValueAsString(new Bean428());
        
        if (!json.contains(quote("fooBar"))) {
            fail("Should use name 'fooBar', does not: "+json);
        }
    }

// com.fasterxml.jackson.databind.introspect.TestNamingStrategyStd::testSimpleLowerCase
    public void testSimpleLowerCase() throws Exception
    {
        final BoringBean input = new BoringBean();
        ObjectMapper m = new ObjectMapper();

        assertEquals(aposToQuotes("{'firstname':'Bob','lastname':'Burger'}"),
                m.writeValueAsString(input));
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
        BeanDescription beanDesc = mapper.getDeserializationConfig().introspect(mapper.constructType(Issue744Bean.class));
        assertNotNull(beanDesc);
        AnnotatedMethod setter = beanDesc.findAnySetter();
        assertNotNull(setter);
    }

// com.fasterxml.jackson.databind.introspect.TestPOJOPropertiesCollector::testPropertyDesc
    public void testPropertyDesc() throws Exception
    {
        
        BeanDescription beanDesc = mapper.getDeserializationConfig().introspect(mapper.constructType(PropDescBean.class));
        _verifyProperty(beanDesc, true, false);
        
        beanDesc = mapper.getSerializationConfig().introspect(mapper.constructType(PropDescBean.class));
        _verifyProperty(beanDesc, true, false);
    }

// com.fasterxml.jackson.databind.introspect.TestPOJOPropertiesCollector::testPropertyIndex
    public void testPropertyIndex() throws Exception
    {
        BeanDescription beanDesc = mapper.getDeserializationConfig().introspect(mapper.constructType(PropDescBean.class));
        _verifyProperty(beanDesc, false, true);
        beanDesc = mapper.getSerializationConfig().introspect(mapper.constructType(PropDescBean.class));
        _verifyProperty(beanDesc, false, true);
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

// com.fasterxml.jackson.databind.introspect.TestPropertyRename323::testCreatorPropRenameWithIgnore
    public void testCreatorPropRenameWithIgnore() throws Exception
    {
        Bean323WithIgnore input = new Bean323WithIgnore(7);
        assertEquals("{\"b\":7}", objectWriter().writeValueAsString(input));
    }

// com.fasterxml.jackson.databind.introspect.TestPropertyRename323::testCreatorPropRenameWithCleave
    public void testCreatorPropRenameWithCleave() {}

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

// com.fasterxml.jackson.databind.jsonschema.TestGenerateJsonSchema::testUnwrapping
    public void testUnwrapping()  throws Exception
    {
        JsonSchema jsonSchema = MAPPER.generateJsonSchema(UnwrappingRoot.class);
        String json = jsonSchema.toString().replaceAll("\"", "'");
        String EXP = "{'type':'object',"
                +"'properties':{'age':{'type':'integer'},"
                +"'name.first':{'type':'string'},'name.last':{'type':'string'}}}";
        assertEquals(EXP, json);
    }

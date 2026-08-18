// buggy code
    public <T extends TreeNode> T readTree(JsonParser p) throws IOException {
        return (T) _bindAsTree(p);
    }

    protected final JsonNode _bindAsTree(JsonParser p) throws IOException
    {
        // Need to inline `_initForReading()` due to tree reading handling end-of-input specially
        _config.initialize(p);
        if (_schema != null) {
            p.setSchema(_schema);
        }

        JsonToken t = p.getCurrentToken();
        if (t == null) {
            t = p.nextToken();
            if (t == null) {
                return _config.getNodeFactory().missingNode();
            }
        }
        final JsonNode resultNode;
        if (t == JsonToken.VALUE_NULL) {
            resultNode = _config.getNodeFactory().nullNode();
        } else {
            final DeserializationContext ctxt = createDeserializationContext(p);
            final JsonDeserializer<Object> deser = _findTreeDeserializer(ctxt);
            if (_unwrapRoot) {
                resultNode = (JsonNode) _unwrapAndDeserialize(p, ctxt, JSON_NODE_TYPE, deser);
            } else {
                resultNode = (JsonNode) deser.deserialize(p, ctxt);
                if (_config.isEnabled(DeserializationFeature.FAIL_ON_TRAILING_TOKENS)) {
                    _verifyNoTrailingTokens(p, ctxt, JSON_NODE_TYPE);
                }
            }
        }
        return resultNode;
    }

// relevant test
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

        
        input.brain = null;

        output = mapper.readValue(mapper.writeValueAsString(input), Dog.class);
        assertNull(output.brain);
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

// com.fasterxml.jackson.databind.deser.TestObjectOrArrayDeserialization::testObjectCase
    public void testObjectCase() throws Exception {
        ArrayOrObject arrayOrObject = new ObjectMapper().readValue("{}", ArrayOrObject.class);
        assertNull("expected objects field to be null", arrayOrObject.objects);
        assertNotNull("expected object field not to be null", arrayOrObject.object);
    }

// com.fasterxml.jackson.databind.deser.TestObjectOrArrayDeserialization::testEmptyArrayCase
    public void testEmptyArrayCase() throws Exception {
        ArrayOrObject arrayOrObject = new ObjectMapper().readValue("[]", ArrayOrObject.class);
        assertNotNull("expected objects field not to be null", arrayOrObject.objects);
        assertTrue("expected objects field to be an empty list", arrayOrObject.objects.isEmpty());
        assertNull("expected object field to be null", arrayOrObject.object);
    }

// com.fasterxml.jackson.databind.deser.TestObjectOrArrayDeserialization::testNotEmptyArrayCase
    public void testNotEmptyArrayCase() throws Exception {
        ArrayOrObject arrayOrObject = new ObjectMapper().readValue("[{}, {}]", ArrayOrObject.class);
        assertNotNull("expected objects field not to be null", arrayOrObject.objects);
        assertEquals("expected objects field to have size 2", 2, arrayOrObject.objects.size());
        assertNull("expected object field to be null", arrayOrObject.object);
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
        m = objectMapperBuilder()
                .configure(MapperFeature.USE_GETTERS_AS_SETTERS, false)
                .build();
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
        ObjectMapper m = objectMapperBuilder()
                .configure(MapperFeature.USE_GETTERS_AS_SETTERS, false)
                .build();
        
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
        ObjectMapper m = objectMapperBuilder()
                .configure(MapperFeature.USE_GETTERS_AS_SETTERS, true)
                .build();
        Dual value = m.readValue("{\"list\":[1,2,3]}, valueType)", Dual.class);
        assertNotNull(value);
        assertEquals(3, value.values.size());
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
            verifyException(jme, "not subtype of");
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
            verifyException(jme, "not subtype of");
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

// com.fasterxml.jackson.databind.deser.builder.BuilderAdvancedTest::testWithInjectable
    public void testWithInjectable() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setInjectableValues(new InjectableValues.Std()
            .addValue(String.class, "stuffValue")
            );
        InjectableXY bean = mapper.readValue(aposToQuotes("{'y':3,'x':7}"),
                InjectableXY.class);
        assertEquals(8, bean._x);
        assertEquals(4, bean._y);
        assertEquals("stuffValue", bean._stuff);
    }

// com.fasterxml.jackson.databind.deser.builder.BuilderFailTest::testBuilderMethodReturnInvalidType
    public void testBuilderMethodReturnInvalidType() throws Exception
    {
        final String json = "{\"x\":1}";
        try {
            MAPPER.readValue(json, ValueClassWrongBuildType.class);
            fail("Missing expected JsonProcessingException exception");
        } catch (InvalidDefinitionException e) {
            verifyException(e, "Build method");
            verifyException(e, "has wrong return type");
        }
    }

// com.fasterxml.jackson.databind.deser.builder.BuilderFailTest::testExtraFields
    public void testExtraFields() throws Exception
    {
        final String json = aposToQuotes("{'x':1,'y':2,'z':3}");
        try {
            MAPPER.readValue(json, ValueClassXY.class);
            fail("should not pass");
        } catch (UnrecognizedPropertyException e) {
            verifyException(e, "Unrecognized field \"z\"");
        }
    }

// com.fasterxml.jackson.databind.deser.builder.BuilderInfiniteLoop1978Test::testInfiniteLoop1978
    public void testInfiniteLoop1978() throws Exception
    {
        String json = "{\"sub.el1\":34,\"sub.el2\":\"some text\"}";
        ObjectMapper mapper = new ObjectMapper();
        Bean bean = mapper.readValue( json, Bean.class );
        assertNotNull(bean);
    }

// com.fasterxml.jackson.databind.deser.builder.BuilderSimpleTest::testSimple
    public void testSimple() throws Exception
    {
        String json = aposToQuotes("{'x':1,'y':2}");
        Object o = MAPPER.readValue(json, ValueClassXY.class);
        assertNotNull(o);
        assertSame(ValueClassXY.class, o.getClass());
        ValueClassXY value = (ValueClassXY) o;
        
        assertEquals(value._x, 2);
        assertEquals(value._y, 3);
    }

// com.fasterxml.jackson.databind.deser.builder.BuilderSimpleTest::testSimpleWithIgnores
    public void testSimpleWithIgnores() throws Exception
    {
        
        final String json = aposToQuotes("{'x':1,'y':2,'z':4}");
        Object o = null;

        try {
            o = MAPPER.readValue(json, ValueClassXY.class);
            fail("Should not pass");
        } catch (UnrecognizedPropertyException e) {
            assertEquals("z", e.getPropertyName());
            verifyException(e, "Unrecognized field \"z\"");
        }

        
        ObjectMapper ignorantMapper = new ObjectMapper();
        ignorantMapper.configOverride(SimpleBuilderXY.class)
                .setIgnorals(JsonIgnoreProperties.Value.forIgnoreUnknown(true));
        o = ignorantMapper.readValue(json, ValueClassXY.class);
        assertNotNull(o);
        assertSame(ValueClassXY.class, o.getClass());
        ValueClassXY value = (ValueClassXY) o;
        
        assertEquals(value._x, 2);
        assertEquals(value._y, 3);
    }

// com.fasterxml.jackson.databind.deser.builder.BuilderSimpleTest::testMultiAccess
    public void testMultiAccess() throws Exception
    {
        String json = aposToQuotes("{'c':3,'a':2,'b':-9}");
        ValueClassABC value = MAPPER.readValue(json, ValueClassABC.class);
        assertNotNull(value);
        assertEquals(2, value.a);
        assertEquals(-9, value.b);
        assertEquals(3, value.c);

        
        value = MAPPER.readValue(aposToQuotes("{'c':3,'d':5,'b':-9}"), ValueClassABC.class);
        assertNotNull(value);
        assertEquals(0, value.a);
        assertEquals(-9, value.b);
        assertEquals(3, value.c);
    }

// com.fasterxml.jackson.databind.deser.builder.BuilderSimpleTest::testImmutable
    public void testImmutable() throws Exception
    {
        final String json = "{\"value\":13}";
        ValueImmutable value = MAPPER.readValue(json, ValueImmutable.class);        
        assertEquals(13, value.value);
    }

// com.fasterxml.jackson.databind.deser.builder.BuilderSimpleTest::testCustomWith
    public void testCustomWith() throws Exception
    {
        final String json = "{\"value\":1}";
        ValueFoo value = MAPPER.readValue(json, ValueFoo.class);        
        assertEquals(1, value.value);
    }

// com.fasterxml.jackson.databind.deser.builder.BuilderSimpleTest::testBuilderMethodReturnMoreGeneral
    public void testBuilderMethodReturnMoreGeneral() throws Exception
    {
        final String json = "{\"x\":1}";
        ValueInterface value = MAPPER.readValue(json, ValueInterface.class);
        assertEquals(2, value.getX());
    }

// com.fasterxml.jackson.databind.deser.builder.BuilderSimpleTest::testBuilderMethodReturnMoreSpecific
    public void testBuilderMethodReturnMoreSpecific() throws Exception
    {
        final String json = "{\"x\":1}";
        ValueInterface2 value = MAPPER.readValue(json, ValueInterface2.class);
        assertEquals(2, value.getX());
    }

// com.fasterxml.jackson.databind.deser.builder.BuilderSimpleTest::testSelfBuilder777
    public void testSelfBuilder777() throws Exception
    {
        SelfBuilder777 result = MAPPER.readValue(aposToQuotes("{'x':3}'"),
                SelfBuilder777.class);
        assertNotNull(result);
        assertEquals(3, result.x);
    }

// com.fasterxml.jackson.databind.deser.builder.BuilderSimpleTest::testWithAnySetter822
    public void testWithAnySetter822() throws Exception
    {
        final String json = "{\"extra\":3,\"foobar\":[ ],\"x\":1,\"name\":\"bob\"}";
        ValueClass822 value = MAPPER.readValue(json, ValueClass822.class);
        assertEquals(1, value.x);
        assertNotNull(value.stuff);
        assertEquals(3, value.stuff.size());
        assertEquals(Integer.valueOf(3), value.stuff.get("extra"));
        assertEquals("bob", value.stuff.get("name"));
        Object ob = value.stuff.get("foobar");
        assertNotNull(ob);
        assertTrue(ob instanceof List);
        assertTrue(((List<?>) ob).isEmpty());
    }

// com.fasterxml.jackson.databind.deser.builder.BuilderSimpleTest::testPOJOConfigResolution1557
    public void testPOJOConfigResolution1557() throws Exception
    {
        final String json = "{\"value\":1}";
        MAPPER.registerModule(new NopModule1557());
        ValueFoo value = MAPPER.readValue(json, ValueFoo.class);
        assertEquals(1, value.value);
    }

// com.fasterxml.jackson.databind.deser.builder.BuilderViaUpdateTest::testBuilderUpdateWithValue
    public void testBuilderUpdateWithValue() throws Exception
    {
        try {
             MAPPER.readerFor(ValueClassXY.class)
                    .withValueToUpdate(new ValueClassXY(6, 7))
                    .readValue(aposToQuotes("{'x':1,'y:'2'}"));
            fail("Should not have passed");
        } catch (InvalidDefinitionException e) {
            verifyException(e, "Deserialization of");
            verifyException(e, "by passing existing instance");
            verifyException(e, "ValueClassXY");
        }
    }

// com.fasterxml.jackson.databind.deser.builder.BuilderViaUpdateTest::testBuilderWithWrongType
    public void testBuilderWithWrongType() throws Exception
    {
        try {
             MAPPER.readerFor(ValueClassXY.class)
                    .withValueToUpdate(new SimpleBuilderXY())
                    .readValue(aposToQuotes("{'x':1,'y:'2'}"));
            fail("Should not have passed");
        } catch (InvalidDefinitionException e) {
            verifyException(e, "Deserialization of");
            verifyException(e, "by passing existing Builder");
            verifyException(e, "SimpleBuilderXY");
        }
    }

// com.fasterxml.jackson.databind.deser.builder.BuilderWithCreatorTest::testWithPropertiesCreator
    public void testWithPropertiesCreator() throws Exception
    {
        final String json = aposToQuotes("{'a':1,'c':3,'b':2}");
        PropertyCreatorValue value = MAPPER.readValue(json, PropertyCreatorValue.class);        
        assertEquals(1, value.a);
        assertEquals(2, value.b);
        assertEquals(3, value.c);
    }

// com.fasterxml.jackson.databind.deser.builder.BuilderWithCreatorTest::testWithDelegatingStringCreator
    public void testWithDelegatingStringCreator() throws Exception
    {
        final int EXP = 139;
        IntCreatorValue value = MAPPER.readValue(String.valueOf(EXP),
                IntCreatorValue.class);        
        assertEquals(EXP, value.value);
    }

// com.fasterxml.jackson.databind.deser.builder.BuilderWithCreatorTest::testWithDelegatingIntCreator
    public void testWithDelegatingIntCreator() throws Exception
    {
        final double EXP = -3.75;
        DoubleCreatorValue value = MAPPER.readValue(String.valueOf(EXP),
                DoubleCreatorValue.class);        
        assertEquals(EXP, value.value);
    }

// com.fasterxml.jackson.databind.deser.builder.BuilderWithCreatorTest::testWithDelegatingBooleanCreator
    public void testWithDelegatingBooleanCreator() throws Exception
    {
        final boolean EXP = true;
        BooleanCreatorValue value = MAPPER.readValue(String.valueOf(EXP),
                BooleanCreatorValue.class);        
        assertEquals(EXP, value.value);
    }

// com.fasterxml.jackson.databind.deser.builder.BuilderWithUnwrappedTest::testWithUnwrappedAndCreatorSingleParameterAtBeginning
    public void testWithUnwrappedAndCreatorSingleParameterAtBeginning() throws Exception {
        final String json = aposToQuotes("{'person_id':1234,'first_name':'John','last_name':'Doe','years_old':30,'living':true}");

        final ObjectMapper mapper = new ObjectMapper();
        Person person = mapper.readValue(json, Person.class);
        assertEquals(1234, person.getId());
        assertNotNull(person.getName());
        assertEquals("John", person.getName().getFirst());
        assertEquals("Doe", person.getName().getLast());
        assertEquals(30, person.getAge());
        assertEquals(true, person.isAlive());
    }

// com.fasterxml.jackson.databind.deser.builder.BuilderWithUnwrappedTest::testWithUnwrappedAndCreatorSingleParameterInMiddle
    public void testWithUnwrappedAndCreatorSingleParameterInMiddle() throws Exception {
        final String json = aposToQuotes("{'first_name':'John','last_name':'Doe','person_id':1234,'years_old':30,'living':true}");

        final ObjectMapper mapper = new ObjectMapper();
        Person person = mapper.readValue(json, Person.class);
        assertEquals(1234, person.getId());
        assertNotNull(person.getName());
        assertEquals("John", person.getName().getFirst());
        assertEquals("Doe", person.getName().getLast());
        assertEquals(30, person.getAge());
        assertEquals(true, person.isAlive());
    }

// com.fasterxml.jackson.databind.deser.builder.BuilderWithUnwrappedTest::testWithUnwrappedAndCreatorSingleParameterAtEnd
    public void testWithUnwrappedAndCreatorSingleParameterAtEnd() throws Exception {
        final String json = aposToQuotes("{'first_name':'John','last_name':'Doe','years_old':30,'living':true,'person_id':1234}");

        final ObjectMapper mapper = new ObjectMapper();
        Person person = mapper.readValue(json, Person.class);
        assertEquals(1234, person.getId());
        assertNotNull(person.getName());
        assertEquals("John", person.getName().getFirst());
        assertEquals("Doe", person.getName().getLast());
        assertEquals(30, person.getAge());
        assertEquals(true, person.isAlive());
    }

// com.fasterxml.jackson.databind.deser.builder.BuilderWithUnwrappedTest::testWithUnwrappedAndCreatorMultipleParametersAtBeginning
    public void testWithUnwrappedAndCreatorMultipleParametersAtBeginning() throws Exception {
        final String json = aposToQuotes("{'animal_id':1234,'living':true,'first_name':'John','last_name':'Doe','years_old':30}");

        final ObjectMapper mapper = new ObjectMapper();
        Animal animal = mapper.readValue(json, Animal.class);
        assertEquals(1234, animal.getId());
        assertNotNull(animal.getName());
        assertEquals("John", animal.getName().getFirst());
        assertEquals("Doe", animal.getName().getLast());
        assertEquals(30, animal.getAge());
        assertEquals(true, animal.isAlive());
    }

// com.fasterxml.jackson.databind.deser.builder.BuilderWithUnwrappedTest::testWithUnwrappedAndCreatorMultipleParametersInMiddle
    public void testWithUnwrappedAndCreatorMultipleParametersInMiddle() throws Exception {
        final String json = aposToQuotes("{'first_name':'John','animal_id':1234,'last_name':'Doe','living':true,'years_old':30}");

        final ObjectMapper mapper = new ObjectMapper();
        Animal animal = mapper.readValue(json, Animal.class);
        assertEquals(1234, animal.getId());
        assertNotNull(animal.getName());
        assertEquals("John", animal.getName().getFirst());
        assertEquals("Doe", animal.getName().getLast());
        assertEquals(30, animal.getAge());
        assertEquals(true, animal.isAlive());
    }

// com.fasterxml.jackson.databind.deser.builder.BuilderWithUnwrappedTest::testWithUnwrappedAndCreatorMultipleParametersAtEnd
    public void testWithUnwrappedAndCreatorMultipleParametersAtEnd() throws Exception {
        final String json = aposToQuotes("{'first_name':'John','last_name':'Doe','years_old':30,'living':true,'animal_id':1234}");

        final ObjectMapper mapper = new ObjectMapper();
        Animal animal = mapper.readValue(json, Animal.class);
        assertEquals(1234, animal.getId());
        assertNotNull(animal.getName());
        assertEquals("John", animal.getName().getFirst());
        assertEquals("Doe", animal.getName().getLast());
        assertEquals(30, animal.getAge());
        assertEquals(true, animal.isAlive());
    }

// com.fasterxml.jackson.databind.deser.builder.BuilderWithViewTest::testSimpleViews
    public void testSimpleViews() throws Exception
    {
        final String json = aposToQuotes("{'x':5,'y':10}");
        ValueClassXY resultX = MAPPER.readerFor(ValueClassXY.class)
                .withView(ViewX.class)
                .readValue(json);
        assertEquals(6, resultX._x);
        assertEquals(1, resultX._y);

        ValueClassXY resultY = MAPPER.readerFor(ValueClassXY.class)
                .withView(ViewY.class)
                .readValue(json);
        assertEquals(1, resultY._x);
        assertEquals(11, resultY._y);
    }

// com.fasterxml.jackson.databind.deser.builder.BuilderWithViewTest::testCreatorViews
    public void testCreatorViews() throws Exception
    {
        final String json = aposToQuotes("{'x':5,'y':10,'bogus':false}");
        CreatorValueXY resultX = MAPPER.readerFor(CreatorValueXY.class)
                .withView(ViewX.class)
                .readValue(json);
        assertEquals(5, resultX._x);
        assertEquals(0, resultX._y);

        CreatorValueXY resultY = MAPPER.readerFor(CreatorValueXY.class)
                .withView(ViewY.class)
                .readValue(json);
        assertEquals(0, resultY._x);
        assertEquals(10, resultY._y);
    }

// com.fasterxml.jackson.databind.deser.creators.ArrayDelegatorCreatorForCollectionTest::testUnmodifiable
    public void testUnmodifiable() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        Class<?> unmodSetType = Collections.unmodifiableSet(Collections.<String>emptySet()).getClass();
        mapper.addMixIn(unmodSetType, UnmodifiableSetMixin.class);
        mapper.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL, JsonTypeInfo.As.PROPERTY);

        final String EXPECTED_JSON = "[\""+unmodSetType.getName()+"\",[]]";

        Set<?> foo = mapper.readValue(EXPECTED_JSON, Set.class);
        assertTrue(foo.isEmpty());
    }

// com.fasterxml.jackson.databind.deser.creators.BigCreatorTest::testBigPartial
    public void testBigPartial() throws Exception
    {
        Biggie value = BIGGIE_READER.readValue(aposToQuotes(
                "{'v7':7, 'v8':8,'v29':29, 'v35':35}"
                ));
        int[] stuff = value.stuff;
        for (int i = 0; i < stuff.length; ++i) {
            int exp;
            
            switch (i) {
            case 6: 
            case 7:
            case 28:
            case 34:
                exp = i+1;
                break;
            default:
                exp = 0;
            }
            assertEquals("Entry #"+i, exp, stuff[i]);
        }
    }

// com.fasterxml.jackson.databind.deser.creators.CreatorPropertiesTest::testCreatorPropertiesAnnotation
    public void testCreatorPropertiesAnnotation() throws Exception
    {
        Issue905Bean b = MAPPER.readValue(aposToQuotes("{'y':3,'x':2}"),
                Issue905Bean.class);
        assertEquals(2, b._x);
        assertEquals(3, b._y);
    }

// com.fasterxml.jackson.databind.deser.creators.CreatorPropertiesTest::testPossibleNamingConflict
    public void testPossibleNamingConflict() throws Exception
    {
        String json = "{\"bar\":3}";
        Ambiguity amb = MAPPER.readValue(json, Ambiguity.class);
        assertNotNull(amb);
        assertEquals(3, amb.getFoo());
    }

// com.fasterxml.jackson.databind.deser.creators.CreatorPropertiesTest::testConstructorPropertiesInference
    public void testConstructorPropertiesInference() throws Exception
    {
        final String JSON = aposToQuotes("{'x':3,'y':5}");

        
        assertTrue(MAPPER.isEnabled(MapperFeature.INFER_CREATOR_FROM_CONSTRUCTOR_PROPERTIES));
        Lombok1371Bean result = MAPPER.readValue(JSON, Lombok1371Bean.class);
        assertEquals(4, result.x);
        assertEquals(6, result.y);

        
        ObjectMapper mapper = objectMapperBuilder()
                .disable(MapperFeature.INFER_CREATOR_FROM_CONSTRUCTOR_PROPERTIES)
                .build();
        
        result = mapper.readValue(JSON, Lombok1371Bean.class);
        assertEquals(3, result.x);
        assertEquals(5, result.y);
    }

// com.fasterxml.jackson.databind.deser.creators.CreatorWithNamingStrategyTest::testSnakeCaseWithOneArg
    public void testSnakeCaseWithOneArg() throws Exception
    {
        final String MSG = "1st";
        OneProperty actual = MAPPER.readValue(
                "{\"param_name0\":\""+MSG+"\"}",
                OneProperty.class);
        assertEquals("CTOR:"+MSG, actual.paramName0);
    }

// com.fasterxml.jackson.databind.deser.creators.CreatorWithObjectIdTest::testObjectIdWithCreator
    public void testObjectIdWithCreator() throws Exception
    {
        A a = new A("123", "A");

        ObjectMapper om = new ObjectMapper();
        String json = om.writeValueAsString(a);
        A deser = om.readValue(json, A.class);
        assertEquals(a.name, deser.name);
    }

// com.fasterxml.jackson.databind.deser.creators.DelegatingArrayCreator1804Test::testDelegatingArray1804
    public void testDelegatingArray1804() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        MyType thing = mapper.readValue("[]", MyType.class);
        assertNotNull(thing);
    }

// com.fasterxml.jackson.databind.deser.creators.DelegatingCreatorAnnotations2016Test::testDelegatingWithAs
    public void testDelegatingWithAs() throws Exception
    {
        Wrapper2016As actual = MAPPER.readValue("123", Wrapper2016As.class);
        assertEquals(Date.class, actual.value.getClass());
    }

// com.fasterxml.jackson.databind.deser.creators.DelegatingCreatorAnnotations2016Test::testDelegatingWithContentAs
    public void testDelegatingWithContentAs() throws Exception
    {
        Wrapper2016ContentAs actual = MAPPER.readValue("[123]", Wrapper2016ContentAs.class);
        List<Object> l = actual.value;
        assertEquals(1, l.size());
        assertEquals(Date.class, l.get(0).getClass());
    }

// com.fasterxml.jackson.databind.deser.creators.DelegatingCreatorAnnotations2021Test::testCustomDeserForDelegating
    public void testCustomDeserForDelegating() throws Exception
    {
        DelegatingWithCustomDeser2021 actual = MAPPER.readValue(" true ", DelegatingWithCustomDeser2021.class);
        assertEquals(DelegatingWithCustomDeser2021.DEFAULT, actual.value);
    }

// com.fasterxml.jackson.databind.deser.creators.DelegatingCreatorImplicitNames1001Test::testWithoutNamedParameters
    public void testWithoutNamedParameters() throws Exception
    {
        ObjectMapper sut = new ObjectMapper();

        D d = D.make("abc:def");

        String actualJson = sut.writeValueAsString(d);
        D actualD = sut.readValue(actualJson, D.class);

        assertEquals("\"abc:def\"", actualJson);
        assertEquals(d, actualD);
    }

// com.fasterxml.jackson.databind.deser.creators.DelegatingCreatorImplicitNames1001Test::testWithNamedParameters
    public void testWithNamedParameters() throws Exception
    {
        ObjectMapper sut = new ObjectMapper()
            .setAnnotationIntrospector(new CreatorNameIntrospector());

        D d = D.make("abc:def");

        String actualJson = sut.writeValueAsString(d);
        D actualD = sut.readValue(actualJson, D.class);

        assertEquals("\"abc:def\"", actualJson);
        assertEquals(d, actualD);
    }

// com.fasterxml.jackson.databind.deser.creators.DelegatingExternalProperty1003Test::testExtrnalPropertyDelegatingCreator
    public void testExtrnalPropertyDelegatingCreator() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();

        final String json = mapper.writeValueAsString(new HeroBattle(new Superman()));

        final HeroBattle battle = mapper.readValue(json, HeroBattle.class);

        assertTrue(battle.getHero() instanceof Superman);
    }

// com.fasterxml.jackson.databind.deser.creators.DisablingCreatorsTest::testDisabling
     public void testDisabling() throws Exception
     {
          final ObjectMapper mapper = objectMapper();

          
          NonConflictingCreators value = mapper.readValue(quote("abc"), NonConflictingCreators.class);
          assertNotNull(value);
          assertEquals("abc", value._value);

          
          try {
                mapper.readValue(quote("abc"), ConflictingCreators.class);
               fail("Should have failed with JsonCreator conflict");
          } catch (JsonProcessingException e) {
               verifyException(e, "Conflicting property-based creators");
          }
     }

// com.fasterxml.jackson.databind.deser.creators.EnumCreatorTest::testCreatorEnums
    public void testCreatorEnums() throws Exception {
        EnumWithCreator value = MAPPER.readValue("\"enumA\"", EnumWithCreator.class);
        assertEquals(EnumWithCreator.A, value);
    }

// com.fasterxml.jackson.databind.deser.creators.EnumCreatorTest::testCreatorEnumsFromBigDecimal
    public void testCreatorEnumsFromBigDecimal() throws Exception {
        EnumWithBDCreator value = MAPPER.readValue("\"8.0\"", EnumWithBDCreator.class);
        assertEquals(EnumWithBDCreator.E8, value);
    }

// com.fasterxml.jackson.databind.deser.creators.EnumCreatorTest::testEnumWithCreatorEnumMaps
    public void testEnumWithCreatorEnumMaps() throws Exception {
        EnumMap<EnumWithCreator,String> value = MAPPER.readValue("{\"enumA\":\"value\"}",
                new TypeReference<EnumMap<EnumWithCreator,String>>() {});
        assertEquals("value", value.get(EnumWithCreator.A));
    }

// com.fasterxml.jackson.databind.deser.creators.EnumCreatorTest::testEnumWithCreatorMaps
    public void testEnumWithCreatorMaps() throws Exception {
        HashMap<EnumWithCreator,String> value = MAPPER.readValue("{\"enumA\":\"value\"}",
                new TypeReference<java.util.HashMap<EnumWithCreator,String>>() {});
        assertEquals("value", value.get(EnumWithCreator.A));
    }

// com.fasterxml.jackson.databind.deser.creators.EnumCreatorTest::testEnumWithCreatorEnumSets
    public void testEnumWithCreatorEnumSets() throws Exception {
        EnumSet<EnumWithCreator> value = MAPPER.readValue("[\"enumA\"]",
                new TypeReference<EnumSet<EnumWithCreator>>() {});
        assertTrue(value.contains(EnumWithCreator.A));
    }

// com.fasterxml.jackson.databind.deser.creators.EnumCreatorTest::testJsonCreatorPropertiesWithEnum
    public void testJsonCreatorPropertiesWithEnum() throws Exception
    {
        EnumWithPropertiesModeJsonCreator type1 = MAPPER.readValue("{\"name\":\"TEST1\", \"description\":\"TEST\"}", EnumWithPropertiesModeJsonCreator.class);
        assertSame(EnumWithPropertiesModeJsonCreator.TEST1, type1);
        
        EnumWithPropertiesModeJsonCreator type2 = MAPPER.readValue("{\"name\":\"TEST3\", \"description\":\"TEST\"}", EnumWithPropertiesModeJsonCreator.class);
        assertSame(EnumWithPropertiesModeJsonCreator.TEST3, type2);
     
    }

// com.fasterxml.jackson.databind.deser.creators.EnumCreatorTest::testJsonCreatorDelagateWithEnum
    public void testJsonCreatorDelagateWithEnum() throws Exception {
        final ObjectMapper mapper = new ObjectMapper();
        
        EnumWithDelegateModeJsonCreator type1 = mapper.readValue("{\"name\":\"TEST1\", \"description\":\"TEST\"}", EnumWithDelegateModeJsonCreator.class);
        assertSame(EnumWithDelegateModeJsonCreator.TEST1, type1);
        
        EnumWithDelegateModeJsonCreator type2 = mapper.readValue("{\"name\":\"TEST3\", \"description\":\"TEST\"}", EnumWithDelegateModeJsonCreator.class);
        assertSame(EnumWithDelegateModeJsonCreator.TEST3, type2);
     
    }

// com.fasterxml.jackson.databind.deser.creators.EnumCreatorTest::testEnumsFromInts
    public void testEnumsFromInts() throws Exception
    {
        Object ob = MAPPER.readValue("1 ", TestEnumFromInt.class);
        assertEquals(TestEnumFromInt.class, ob.getClass());
        assertSame(TestEnumFromInt.ENUM_A, ob);
    }

// com.fasterxml.jackson.databind.deser.creators.EnumCreatorTest::testExceptionFromCreator
    public void testExceptionFromCreator() throws Exception
    {
        try {
             MAPPER.readValue(quote("xyz"), TestEnum324.class);
            fail("Should throw exception");
        } catch (JsonMappingException e) {
            verifyException(e, "foobar");
        }
    }

// com.fasterxml.jackson.databind.deser.creators.EnumCreatorTest::testDeserializerForCreatorWithEnumMaps
    public void testDeserializerForCreatorWithEnumMaps() throws Exception
    {
        final ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new DelegatingDeserializersModule());
        EnumMap<EnumWithCreator,String> value = mapper.readValue("{\"enumA\":\"value\"}",
                new TypeReference<EnumMap<EnumWithCreator,String>>() {});
        assertEquals("value", value.get(EnumWithCreator.A));
    }

// com.fasterxml.jackson.databind.deser.creators.EnumCreatorTest::testMultiArgEnumCreator
    public void testMultiArgEnumCreator() throws Exception
    {
        Enum929 v = MAPPER.readValue("{\"id\":3,\"name\":\"B\"}", Enum929.class);
        assertEquals(Enum929.B, v);
    }

// com.fasterxml.jackson.databind.deser.creators.EnumCreatorTest::testNoArgEnumCreator
    public void testNoArgEnumCreator() throws Exception
    {
        MyEnum960 v = MAPPER.readValue("{\"value\":\"bogus\"}", MyEnum960.class);
        assertEquals(MyEnum960.VALUE, v);
    }

// com.fasterxml.jackson.databind.deser.creators.EnumCreatorTest::testEnumCreators1291
    public void testEnumCreators1291() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        String json = mapper.writeValueAsString(Enum1291.V2);
        Enum1291 result = mapper.readValue(json, Enum1291.class);
        assertSame(Enum1291.V2, result);
    }

// com.fasterxml.jackson.databind.deser.creators.EnumCreatorTest::testMultiArgEnumInCollections
    public void testMultiArgEnumInCollections() throws Exception
    {
        EnumSet<Enum929> valueEnumSet = MAPPER.readValue("[{\"id\":3,\"name\":\"B\"}, {\"id\":3,\"name\":\"A\"}]",
                new TypeReference<EnumSet<Enum929>>() {});
        assertEquals(2, valueEnumSet.size());
        assertTrue(valueEnumSet.contains(Enum929.A));
        assertTrue(valueEnumSet.contains(Enum929.B));
        List<Enum929> valueList = MAPPER.readValue("[{\"id\":3,\"name\":\"B\"}, {\"id\":3,\"name\":\"A\"}, {\"id\":3,\"name\":\"B\"}]",
                new TypeReference<List<Enum929>>() {});
        assertEquals(3, valueList.size());
        assertEquals(Enum929.B, valueList.get(2));
    }

// com.fasterxml.jackson.databind.deser.creators.FailOnNullCreatorTest::testRequiredNonNullParam
    public void testRequiredNonNullParam() throws Exception
    {
        Person p;
        
        p = POINT_READER.readValue(aposToQuotes("{}"));
        assertEquals(null, p.name);
        assertEquals(Integer.valueOf(0), p.age);

        
        ObjectReader r = POINT_READER.with(DeserializationFeature.FAIL_ON_NULL_CREATOR_PROPERTIES);
        p = POINT_READER.readValue(aposToQuotes("{'name':'John', 'age': null}"));
        assertEquals("John", p.name);
        assertEquals(Integer.valueOf(0), p.age);

        
        try {
            r.readValue(aposToQuotes("{}"));
            fail("Should not pass third test");
        } catch (JsonMappingException e) {
            verifyException(e, "Null value for creator property 'name'");
        }

        
        try {
            r.readValue(aposToQuotes("{'age': 5, 'name': null}"));
            fail("Should not pass fourth test");
        } catch (JsonMappingException e) {
            verifyException(e, "Null value for creator property 'name'");
        }
    }

// com.fasterxml.jackson.databind.deser.creators.ImplicitNameMatch792Test::testValue
        public int testValue() { return value; }

// com.fasterxml.jackson.databind.deser.creators.ImplicitNameMatch792Test::testBindingOfImplicitCreatorNames
    public void testBindingOfImplicitCreatorNames() throws Exception
    {
        ObjectMapper m = new ObjectMapper();
        m.setAnnotationIntrospector(new ConstructorNameAI());
        String json = m.writeValueAsString(new Issue792Bean("a", "b"));
        assertEquals(aposToQuotes("{'first':'a','other':3}"), json);
    }

// com.fasterxml.jackson.databind.deser.creators.ImplicitNameMatch792Test::testImplicitWithSetterGetter
    public void testImplicitWithSetterGetter() throws Exception
    {
        String json = MAPPER.writeValueAsString(new Bean2());
        assertEquals(aposToQuotes("{'stuff':3}"), json);
    }

// com.fasterxml.jackson.databind.deser.creators.ImplicitNameMatch792Test::testReadWriteWithPrivateField
    public void testReadWriteWithPrivateField() throws Exception
    {
        String json = MAPPER.writeValueAsString(new ReadWriteBean(3));
        assertEquals("{\"value\":3}", json);
    }

// com.fasterxml.jackson.databind.deser.creators.ImplicitNameMatch792Test::testWriteOnly
    public void testWriteOnly() throws Exception
    {
        PasswordBean bean = MAPPER.readValue(aposToQuotes("{'value':7,'password':'foo'}"),
                PasswordBean.class);
        assertEquals("[password='foo',value=7]", bean.asString());
        String json = MAPPER.writeValueAsString(bean);
        assertEquals("{\"value\":7}", json);
    }

// com.fasterxml.jackson.databind.deser.creators.ImplicitParamsForCreatorTest::testNonSingleArgCreator
    public void testNonSingleArgCreator() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setAnnotationIntrospector(new MyParamIntrospector());
        XY value = mapper.readValue(aposToQuotes("{'paramName0':1,'paramName1':2}"), XY.class);
        assertNotNull(value);
        assertEquals(1, value.x);
        assertEquals(2, value.y);
    }

// com.fasterxml.jackson.databind.deser.creators.InnerClassCreatorTest::testIssue1501
    public void testIssue1501() throws Exception
    {
        String ser = MAPPER.writeValueAsString(new Something1501(false));
        try {
            MAPPER.readValue(ser, Something1501.class);
            fail("Should not pass");
        } catch (JsonMappingException e) {
            verifyException(e, "Cannot construct instance");
            verifyException(e, "InnerSomething1501");
            verifyException(e, "can only instantiate non-static inner class by using default");
        }
    }

// com.fasterxml.jackson.databind.deser.creators.InnerClassCreatorTest::testIssue1502
    public void testIssue1502() throws Exception
    {
        String ser = MAPPER.writeValueAsString(new Something1502(null));
        try {
            MAPPER.readValue(ser, Something1502.class);
            fail("Should not pass");
        } catch (JsonMappingException e) {
            verifyException(e, "Cannot construct instance");
            verifyException(e, "InnerSomething1502");
            verifyException(e, "can only instantiate non-static inner class by using default");
        }
    }

// com.fasterxml.jackson.databind.deser.creators.InnerClassCreatorTest::testIssue1503
    public void testIssue1503() throws Exception
    {
        String ser = MAPPER.writeValueAsString(new Outer1503());
        Outer1503 result = MAPPER.readValue(ser, Outer1503.class);
        assertNotNull(result);
    }

// com.fasterxml.jackson.databind.deser.creators.MultiArgConstructorTest::testMultiArgVisible
    public void testMultiArgVisible() throws Exception
    {
        final ObjectMapper mapper = new ObjectMapper();
        mapper.setAnnotationIntrospector(new MyParamIntrospector());
        MultiArgCtorBean bean = mapper.readValue(aposToQuotes("{'b':13, 'c':2, 'a':-99}"),
                MultiArgCtorBean.class);
        assertNotNull(bean);
        assertEquals(13, bean._b);
        assertEquals(-99, bean._a);
        assertEquals(2, bean.c);
    }

// com.fasterxml.jackson.databind.deser.creators.MultiArgConstructorTest::testMultiArgWithPartialOverride
    public void testMultiArgWithPartialOverride() throws Exception
    {
        final ObjectMapper mapper = new ObjectMapper();
        mapper.setAnnotationIntrospector(new MyParamIntrospector());
        MultiArgCtorBeanWithAnnotations bean = mapper.readValue(aposToQuotes("{'b2':7, 'c':222, 'a':-99}"),
                MultiArgCtorBeanWithAnnotations.class);
        assertNotNull(bean);
        assertEquals(7, bean._b);
        assertEquals(-99, bean._a);
        assertEquals(222, bean.c);
    }

// com.fasterxml.jackson.databind.deser.creators.MultiArgConstructorTest::testMultiArgNotVisible
    public void testMultiArgNotVisible() throws Exception
    {
        final ObjectMapper mapper = new ObjectMapper();
        mapper.setAnnotationIntrospector(new MyParamIntrospector());
        mapper.setDefaultVisibility(
                JsonAutoDetect.Value.noOverrides()
                    .withCreatorVisibility(Visibility.NONE));
        try {
             mapper.readValue(aposToQuotes("{'b':13,  'a':-99}"),
                MultiArgCtorBean.class);
            fail("Should not have passed");
        } catch (InvalidDefinitionException e) {
            verifyException(e, "no Creators");
        }
    }

// com.fasterxml.jackson.databind.deser.creators.RequiredCreatorTest::testRequiredAnnotatedParam
    public void testRequiredAnnotatedParam() throws Exception
    {
        FascistPoint p;

        
        p = POINT_READER.readValue(aposToQuotes("{'y':2,'x':1}"));
        assertEquals(1, p.x);
        assertEquals(2, p.y);
        p = POINT_READER.readValue(aposToQuotes("{'x':3,'y':4}"));
        assertEquals(3, p.x);
        assertEquals(4, p.y);

        
        p = POINT_READER.readValue(aposToQuotes("{'x':3}"));
        assertEquals(3, p.x);
        assertEquals(0, p.y);

        
        try {
            POINT_READER.readValue(aposToQuotes("{'y':3}"));
            fail("Should not pass");
        } catch (JsonMappingException e) {
            verifyException(e, "Missing required creator property 'x' (index 0)");
        }
    }

// com.fasterxml.jackson.databind.deser.creators.RequiredCreatorTest::testRequiredGloballyParam
    public void testRequiredGloballyParam() throws Exception
    {
        FascistPoint p;

        
        p = POINT_READER.readValue(aposToQuotes("{'x':2}"));
        assertEquals(2, p.x);
        assertEquals(0, p.y);

        
        ObjectReader r = POINT_READER.with(DeserializationFeature.FAIL_ON_MISSING_CREATOR_PROPERTIES);
        try {
            r.readValue(aposToQuotes("{'x':6}"));
            fail("Should not pass");
        } catch (JsonMappingException e) {
            verifyException(e, "Missing creator property 'y' (index 1)");
        }
    }

// com.fasterxml.jackson.databind.deser.creators.SingleArgCreatorTest::testNamedSingleArg
    public void testNamedSingleArg() throws Exception
    {
        SingleNamedStringBean bean = MAPPER.readValue(quote("foobar"),
                SingleNamedStringBean.class);
        assertEquals("foobar", bean._ss);
    }

// com.fasterxml.jackson.databind.deser.creators.SingleArgCreatorTest::testSingleStringArgWithImplicitName
    public void testSingleStringArgWithImplicitName() throws Exception
    {
        final ObjectMapper mapper = new ObjectMapper();
        mapper.setAnnotationIntrospector(new MyParamIntrospector("value"));
        StringyBean bean = mapper.readValue(quote("foobar"), StringyBean.class);
        assertEquals("foobar", bean.getValue());
    }

// com.fasterxml.jackson.databind.deser.creators.SingleArgCreatorTest::testSingleImplicitlyNamedNotDelegating
    public void testSingleImplicitlyNamedNotDelegating() throws Exception
    {
        final ObjectMapper mapper = new ObjectMapper();
        mapper.setAnnotationIntrospector(new MyParamIntrospector("value"));
        StringyBeanWithProps bean = mapper.readValue("{\"value\":\"x\"}", StringyBeanWithProps.class);
        assertEquals("x", bean.getValue());
    }

// com.fasterxml.jackson.databind.deser.creators.SingleArgCreatorTest::testSingleExplicitlyNamedButDelegating
    public void testSingleExplicitlyNamedButDelegating() throws Exception
    {
        SingleNamedButStillDelegating bean = MAPPER.readValue(quote("xyz"),
                SingleNamedButStillDelegating.class);
        assertEquals("xyz", bean.value);
    }

// com.fasterxml.jackson.databind.deser.creators.SingleArgCreatorTest::testExplicitFactory660a
    public void testExplicitFactory660a() throws Exception
    {
        
        ExplicitFactoryBeanA bean = MAPPER.readValue(quote("abc"), ExplicitFactoryBeanA.class);
        assertNotNull(bean);
        assertEquals("abc", bean.value());
    }

// com.fasterxml.jackson.databind.deser.creators.SingleArgCreatorTest::testExplicitFactory660b
    public void testExplicitFactory660b() throws Exception
    {
        
        ExplicitFactoryBeanB bean2 = MAPPER.readValue(quote("def"), ExplicitFactoryBeanB.class);
        assertNotNull(bean2);
        assertEquals("def", bean2.value());
    }

// com.fasterxml.jackson.databind.deser.creators.SingleArgCreatorTest::testSingleImplicitDelegating
    public void testSingleImplicitDelegating() throws Exception
    {
        final ObjectMapper mapper = new ObjectMapper();
        mapper.setAnnotationIntrospector(new MyParamIntrospector("value"));
        SingleArgWithImplicit bean = mapper.readValue(aposToQuotes("{'x':1,'y':2}"),
                SingleArgWithImplicit.class);
        XY v = bean.getFoobar();
        assertNotNull(v);
        assertEquals(1, v.x);
        assertEquals(2, v.y);
    }

// com.fasterxml.jackson.databind.deser.creators.TestConstructFromMap::testViaConstructor
    public void testViaConstructor() throws Exception
    {
        ObjectMapper m = new ObjectMapper();
        ConstructorFromMap result = m.readValue
            ("{ \"x\":1, \"y\" : \"abc\" }", ConstructorFromMap.class);
        assertEquals(1, result._x);
        assertEquals("abc", result._y);
    }

// com.fasterxml.jackson.databind.deser.creators.TestConstructFromMap::testViaFactory
    public void testViaFactory() throws Exception
    {
        ObjectMapper m = new ObjectMapper();
        FactoryFromPoint result = m.readValue("{ \"x\" : 3, \"y\" : 4 }", FactoryFromPoint.class);
        assertEquals(3, result._x);
        assertEquals(4, result._y);
    }

// com.fasterxml.jackson.databind.deser.creators.TestConstructFromMap::testViaFactoryUsingString
    public void testViaFactoryUsingString() throws Exception
    {
        ObjectMapper m = new ObjectMapper();
        FactoryFromDecimalString result = m.readValue("\"12.57\"", FactoryFromDecimalString.class);
        assertNotNull(result);
        assertEquals(12, result._value);
    }

// com.fasterxml.jackson.databind.deser.creators.TestCreatorNullValue::testUsesDeserializersNullValue
    public void testUsesDeserializersNullValue() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new TestModule());
        Container container = mapper.readValue("{}", Container.class);
        assertEquals(NULL_CONTAINED, container.contained);
    }

// com.fasterxml.jackson.databind.deser.creators.TestCreatorNullValue::testCreatorReturningNull
    public void testCreatorReturningNull() throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        String json = "{ \"type\" : \"     \", \"id\" : \"000c0ffb-a0d6-4d2e-a379-4aeaaf283599\" }";
        try {
            objectMapper.readValue(json, JsonEntity.class);
            fail("Should not have succeeded");
        } catch (JsonMappingException e) {
            verifyException(e, "JSON creator returned null");
        }
    }

// com.fasterxml.jackson.databind.deser.creators.TestCreatorWithNamingStrategy556::testRenameViaCtor
    public void testRenameViaCtor() throws Exception
    {
        RenamingCtorBean bean = MAPPER.readValue(CTOR_JSON, RenamingCtorBean.class);
        assertEquals(42, bean.myAge);
        assertEquals("NotMyRealName", bean.myName);
    }

// com.fasterxml.jackson.databind.deser.creators.TestCreatorWithNamingStrategy556::testRenameViaFactory
    public void testRenameViaFactory() throws Exception
    {
        RenamedFactoryBean bean = MAPPER.readValue(CTOR_JSON, RenamedFactoryBean.class);
        assertEquals(42, bean.myAge);
        assertEquals("NotMyRealName", bean.myName);
    }

// com.fasterxml.jackson.databind.deser.creators.TestCreatorWithPolymorphic113::testSubtypes
    public void testSubtypes() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        String id = "nice dogy";
        String json = mapper.writeValueAsString(new AnimalWrapper(new Dog(id)));

        AnimalWrapper wrapper = mapper.readValue(json, AnimalWrapper.class);
        assertEquals(id, wrapper.getAnimal().getId());
    }

// com.fasterxml.jackson.databind.deser.creators.TestCreators::testSimpleConstructor
    public void testSimpleConstructor() throws Exception
    {
        ConstructorBean bean = MAPPER.readValue("{ \"x\" : 42 }", ConstructorBean.class);
        assertEquals(42, bean.x);
    }

// com.fasterxml.jackson.databind.deser.creators.TestCreators::testNoArgsFactory
    public void testNoArgsFactory() throws Exception
    {
        NoArgFactoryBean value = MAPPER.readValue("{\"y\":13}", NoArgFactoryBean.class);
        assertEquals(13, value.y);
        assertEquals(123, value.x);
    }

// com.fasterxml.jackson.databind.deser.creators.TestCreators::testSimpleDoubleConstructor
    public void testSimpleDoubleConstructor() throws Exception
    {
        Double exp = new Double("0.25");
        DoubleConstructorBean bean = MAPPER.readValue(exp.toString(), DoubleConstructorBean.class);
        assertEquals(exp, bean.d);
    }

// com.fasterxml.jackson.databind.deser.creators.TestCreators::testSimpleBooleanConstructor
    public void testSimpleBooleanConstructor() throws Exception
    {
        BooleanConstructorBean bean = MAPPER.readValue(" true ", BooleanConstructorBean.class);
        assertEquals(Boolean.TRUE, bean.b);

        BooleanConstructorBean2 bean2 = MAPPER.readValue(" true ", BooleanConstructorBean2.class);
        assertTrue(bean2.b);
    }

// com.fasterxml.jackson.databind.deser.creators.TestCreators::testSimpleFactory
    public void testSimpleFactory() throws Exception
    {
        FactoryBean bean = MAPPER.readValue("{ \"f\" : 0.25 }", FactoryBean.class);
        assertEquals(0.25, bean.d);
    }

// com.fasterxml.jackson.databind.deser.creators.TestCreators::testLongFactory
    public void testLongFactory() throws Exception
    {
        long VALUE = 123456789000L;
        LongFactoryBean bean = MAPPER.readValue(String.valueOf(VALUE), LongFactoryBean.class);
        assertEquals(VALUE, bean.value);
    }

// com.fasterxml.jackson.databind.deser.creators.TestCreators::testStringFactory
    public void testStringFactory() throws Exception
    {
        String str = "abc";
        StringFactoryBean bean = MAPPER.readValue(quote(str), StringFactoryBean.class);
        assertEquals(str, bean.value);
    }

// com.fasterxml.jackson.databind.deser.creators.TestCreators::testStringFactoryAlt
    public void testStringFactoryAlt() throws Exception
    {
        String str = "xyz";
        FromStringBean bean = MAPPER.readValue(quote(str), FromStringBean.class);
        assertEquals(str, bean.value);
    }

// com.fasterxml.jackson.databind.deser.creators.TestCreators::testConstructorCreator
    public void testConstructorCreator() throws Exception
    {
        CreatorBean bean = MAPPER.readValue
            ("{ \"a\" : \"xyz\", \"x\" : 12 }", CreatorBean.class);
        assertEquals(13, bean.x);
        assertEquals("ctor:xyz", bean.a);
    }

// com.fasterxml.jackson.databind.deser.creators.TestCreators::testConstructorAndProps
    public void testConstructorAndProps() throws Exception
    {
        ConstructorAndPropsBean bean = MAPPER.readValue
            ("{ \"a\" : \"1\", \"b\": 2, \"c\" : true }", ConstructorAndPropsBean.class);
        assertEquals(1, bean.a);
        assertEquals(2, bean.b);
        assertEquals(true, bean.c);
    }

// com.fasterxml.jackson.databind.deser.creators.TestCreators::testFactoryAndProps
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

// com.fasterxml.jackson.databind.deser.creators.TestCreators::testMultipleCreators
    public void testMultipleCreators() throws Exception
    {
        MultiBean bean = MAPPER.readValue("123", MultiBean.class);
        assertEquals(Integer.valueOf(123), bean.value);
        bean = MAPPER.readValue(quote("abc"), MultiBean.class);
        assertEquals("abc", bean.value);
        bean = MAPPER.readValue("0.25", MultiBean.class);
        assertEquals(Double.valueOf(0.25), bean.value);
    }

// com.fasterxml.jackson.databind.deser.creators.TestCreators::testDeferredConstructorAndProps
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

// com.fasterxml.jackson.databind.deser.creators.TestCreators::testDeferredFactoryAndProps
    public void testDeferredFactoryAndProps() throws Exception
    {
        DeferredFactoryAndPropsBean bean = MAPPER.readValue
            ("{ \"prop\" : \"1\", \"ctor\" : \"2\" }", DeferredFactoryAndPropsBean.class);
        assertEquals("1", bean.prop);
        assertEquals("2", bean.ctor);
    }

// com.fasterxml.jackson.databind.deser.creators.TestCreators::testFactoryCreatorWithMixin
    public void testFactoryCreatorWithMixin() throws Exception
    {
        ObjectMapper m = new ObjectMapper();
        m.addMixIn(CreatorBean.class, MixIn.class);
        CreatorBean bean = m.readValue
            ("{ \"a\" : \"xyz\", \"x\" : 12 }", CreatorBean.class);
        assertEquals(11, bean.x);
        assertEquals("factory:xyz", bean.a);
    }

// com.fasterxml.jackson.databind.deser.creators.TestCreators::testFactoryCreatorWithRenamingMixin
    public void testFactoryCreatorWithRenamingMixin() throws Exception
    {
        ObjectMapper m = new ObjectMapper();
        m.addMixIn(FactoryBean.class, FactoryBeanMixIn.class);
        
        FactoryBean bean = m.readValue("{ \"mixed\" :  20.5 }", FactoryBean.class);
        assertEquals(20.5, bean.d);
    }

// com.fasterxml.jackson.databind.deser.creators.TestCreators::testMapWithConstructor
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

// com.fasterxml.jackson.databind.deser.creators.TestCreators::testMapWithFactory
    public void testMapWithFactory() throws Exception
    {
        MapWithFactory result = MAPPER.readValue
            ("{\"x\":\"...\",\"b\":true  }",
             MapWithFactory.class);
        assertEquals("...", result.get("x"));
        assertEquals(1, result.size());
        assertEquals(Boolean.TRUE, result._b);
    }

// com.fasterxml.jackson.databind.deser.creators.TestCreators::testBrokenConstructor
    public void testBrokenConstructor() throws Exception
    {
        try {
             MAPPER.readValue("{ \"x\" : 42 }", BrokenBean.class);
        } catch (InvalidDefinitionException je) {
            verifyException(je, "has no property name");
        }
    }

// com.fasterxml.jackson.databind.deser.creators.TestCreators2::testExceptionFromConstructor
    public void testExceptionFromConstructor() throws Exception
    {
        try {
            MAPPER.readValue("{}", BustedCtor.class);
            fail("Expected exception");
        } catch (ValueInstantiationException e) {
            verifyException(e, ": foobar");
            
            Throwable t = e.getCause();
            if (t == null) {
                fail("Should have assigned cause for: ("+e.getClass().getSimpleName()+") "+e);
            }
            assertNotNull(t);
            assertEquals(IllegalArgumentException.class, t.getClass());
            assertEquals("foobar", t.getMessage());
        } catch (Exception e) {
            fail("Should have caught ValueInstantiationException, got: "+e);
        }
    }

// com.fasterxml.jackson.databind.deser.creators.TestCreators2::testSimpleConstructor
    public void testSimpleConstructor() throws Exception
    {
        HashTest test = MAPPER.readValue("{\"type\":\"custom\",\"bytes\":\"abc\" }", HashTest.class);
        assertEquals("custom", test.type);
        assertEquals("abc", new String(test.bytes, "UTF-8"));
    }

// com.fasterxml.jackson.databind.deser.creators.TestCreators2::testMissingPrimitives
    public void testMissingPrimitives() throws Exception
    {
        Primitives p = MAPPER.readValue("{}", Primitives.class);
        assertFalse(p.b);
        assertEquals(0, p.x);
        assertEquals(0.0, p.d);
    }

// com.fasterxml.jackson.databind.deser.creators.TestCreators2::testJackson431
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

// com.fasterxml.jackson.databind.deser.creators.TestCreators2::testJackson438
    public void testJackson438() throws Exception
    {
        Exception e = null;
        try {
            MAPPER.readValue("{ \"name\":\"foobar\" }", BeanFor438.class);
            fail("Should have failed");
        } catch (JsonMappingException e0) {
            e = e0;
        }
        if (!(e instanceof ValueInstantiationException)) {
            fail("Should have received ValueInstantiationException, caught "+e.getClass().getName());
        }
        verifyException(e, "don't like that name");
        
        Throwable t = e.getCause();
        if (t == null) {
            fail("Should have assigned cause for: ("+e.getClass().getSimpleName()+") "+e);
        }
        assertEquals(IllegalArgumentException.class, t.getClass());
        verifyException(e, "don't like that name");
    }

// com.fasterxml.jackson.databind.deser.creators.TestCreators2::testCreatorWithDupNames
    public void testCreatorWithDupNames() throws Exception
    {
        try {
            MAPPER.readValue("{\"bar\":\"x\"}", BrokenCreatorBean.class);
            fail("Should have caught duplicate creator parameters");
        } catch (InvalidDefinitionException e) {
            verifyException(e, "duplicate creator property \"bar\"");
            verifyException(e, "for type `com.fasterxml.jackson.databind.");
            verifyException(e, "$BrokenCreatorBean`");
        }
    }

// com.fasterxml.jackson.databind.deser.creators.TestCreators2::testCreatorMultipleArgumentWithoutAnnotation
    public void testCreatorMultipleArgumentWithoutAnnotation() throws Exception {
        AutoDetectConstructorBean value = MAPPER.readValue("{\"bar\":\"bar\",\"foo\":\"foo\"}",
                AutoDetectConstructorBean.class);
        assertEquals("bar", value.bar);
        assertEquals("foo", value.foo);
    }

// com.fasterxml.jackson.databind.deser.creators.TestCreators2::testIgnoredSingleArgCtor
    public void testIgnoredSingleArgCtor() throws Exception
    {
        try {
            MAPPER.readValue(quote("abc"), IgnoredCtor.class);
            fail("Should have caught missing constructor problem");
        } catch (MismatchedInputException e) {
            verifyException(e, "no String-argument constructor/factory method");
        }
    }

// com.fasterxml.jackson.databind.deser.creators.TestCreators2::testAbstractFactory
    public void testAbstractFactory() throws Exception
    {
        AbstractBase bean = MAPPER.readValue("{\"a\":3}", AbstractBase.class);
        assertNotNull(bean);
        AbstractBaseImpl impl = (AbstractBaseImpl) bean;
        assertEquals(1, impl.props.size());
        assertEquals(Integer.valueOf(3), impl.props.get("a"));
    }

// com.fasterxml.jackson.databind.deser.creators.TestCreators2::testCreatorProperties
    public void testCreatorProperties() throws Exception
    {
        Issue700Bean value = MAPPER.readValue("{ \"item\" : \"foo\" }", Issue700Bean.class);
        assertNotNull(value);
    }

// com.fasterxml.jackson.databind.deser.creators.TestCreators2::testConstructorChoice
    public void testConstructorChoice() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        MultiPropCreator1476 pojo = mapper.readValue("{ \"intField\": 1, \"stringField\": \"foo\" }",
                MultiPropCreator1476.class);
        assertEquals(1, pojo.getIntField());
        assertEquals("foo", pojo.getStringField());
    }

// com.fasterxml.jackson.databind.deser.creators.TestCreators3::testCreator541
    public void testCreator541() throws Exception
    {
        ObjectMapper mapper = objectMapperBuilder()
                .disable(
                MapperFeature.AUTO_DETECT_CREATORS,
                MapperFeature.AUTO_DETECT_FIELDS,
                MapperFeature.AUTO_DETECT_GETTERS,
                MapperFeature.AUTO_DETECT_IS_GETTERS,
                MapperFeature.AUTO_DETECT_SETTERS,
                MapperFeature.USE_GETTERS_AS_SETTERS)
                .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                .disable(SerializationFeature.FAIL_ON_EMPTY_BEANS)
                .serializationInclusion(JsonInclude.Include.NON_NULL)
        .build();

        final String JSON = "{\n"
                + "    \"foo\": {\n"
                + "        \"0\": {\n"
                + "            \"p\": 0,\n"
                + "            \"stuff\": [\n"
                + "              \"a\", \"b\" \n"
                + "            ]   \n"
                + "        },\n"
                + "        \"1\": {\n"
                + "            \"p\": 1000,\n"
                + "            \"stuff\": [\n"
                + "              \"c\", \"d\" \n"
                + "            ]   \n"
                + "        },\n"
                + "        \"2\": {\n"
                + "            \"p\": 2000,\n"
                + "            \"stuff\": [\n"
                + "            ]   \n"
                + "        }\n"
                + "    },\n"
                + "    \"anumber\": 25385874\n"
                + "}";

        Foo obj = mapper.readValue(JSON, Foo.class);
        assertNotNull(obj);
        assertNotNull(obj.foo);
        assertEquals(3, obj.foo.size());
        assertEquals(25385874L, obj.getAnumber());
    }

// com.fasterxml.jackson.databind.deser.creators.TestCreators3::testMultiCtor421
    public void testMultiCtor421() throws Exception
    {
        final ObjectMapper mapper = newObjectMapper();
        mapper.setAnnotationIntrospector(new MyParamIntrospector());

        MultiCtor bean = mapper.readValue(aposToQuotes("{'a':'123','b':'foo'}"), MultiCtor.class);
        assertNotNull(bean);
        assertEquals("123", bean._a);
        assertEquals("foo", bean._b);
    }

// com.fasterxml.jackson.databind.deser.creators.TestCreators3::testSerialization
    public void testSerialization() throws Exception {
        assertEquals(quote("testProduct"),
                MAPPER.writeValueAsString(new Product1853(false, "testProduct")));
    }

// com.fasterxml.jackson.databind.deser.creators.TestCreators3::testDeserializationFromObject
    public void testDeserializationFromObject() throws Exception {
        final String EXAMPLE_DATA = "{\"name\":\"dummy\",\"other\":{},\"errors\":{}}";
        assertEquals("PROP:dummy", MAPPER.readValue(EXAMPLE_DATA, Product1853.class).getName());
    }

// com.fasterxml.jackson.databind.deser.creators.TestCreators3::testDeserializationFromString
    public void testDeserializationFromString() throws Exception {
        assertEquals("DELEG:testProduct",
                MAPPER.readValue(quote("testProduct"), Product1853.class).getName());
    }

// com.fasterxml.jackson.databind.deser.creators.TestCreatorsDelegating::testBooleanDelegate
    public void testBooleanDelegate() throws Exception
    {
        
        BooleanBean bb = MAPPER.readValue("true", BooleanBean.class);
        assertEquals(Boolean.TRUE, bb.value);

        
        bb = MAPPER.readValue(quote("true"), BooleanBean.class);
        assertEquals(Boolean.TRUE, bb.value);
    }

// com.fasterxml.jackson.databind.deser.creators.TestCreatorsDelegating::testWithCtorAndDelegate
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

// com.fasterxml.jackson.databind.deser.creators.TestCreatorsDelegating::testWithFactoryAndDelegate
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

// com.fasterxml.jackson.databind.deser.creators.TestCreatorsDelegating::testDelegateWithTokenBuffer
    public void testDelegateWithTokenBuffer() throws Exception
    {
        Value592 value = MAPPER.readValue("{\"a\":1,\"b\":2}", Value592.class);
        assertNotNull(value);
        Object ob = value.stuff;
        assertEquals(TokenBuffer.class, ob.getClass());
        JsonParser jp = ((TokenBuffer) ob).asParser();
        assertToken(JsonToken.START_OBJECT, jp.nextToken());
        assertToken(JsonToken.FIELD_NAME, jp.nextToken());
        assertEquals("a", jp.getCurrentName());
        assertToken(JsonToken.VALUE_NUMBER_INT, jp.nextToken());
        assertEquals(1, jp.getIntValue());
        assertToken(JsonToken.FIELD_NAME, jp.nextToken());
        assertEquals("b", jp.getCurrentName());
        assertToken(JsonToken.VALUE_NUMBER_INT, jp.nextToken());
        assertEquals(2, jp.getIntValue());
        assertToken(JsonToken.END_OBJECT, jp.nextToken());
        jp.close();
    }

// com.fasterxml.jackson.databind.deser.creators.TestCreatorsDelegating::testIssue465
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

// com.fasterxml.jackson.databind.deser.creators.TestCreatorsWithIdentity::testSimple
	public void testSimple() throws IOException
	{
	    String parentStr = "{\"id\" : \"1\", \"parentProp\" : \"parent\"}";
	    String childStr = "{\"childProp\" : \"child\", \"parent\" : " + parentStr + "}";
	    Parent parent = JSON_MAPPER.readValue(parentStr, Parent.class);
	    assertNotNull(parent);
	    Child child = JSON_MAPPER.readValue(childStr, Child.class);
	    assertNotNull(child);
	    assertNotNull(child.parent);
	}

// com.fasterxml.jackson.databind.deser.creators.TestPolymorphicCreators::testManualPolymorphicDog
    public void testManualPolymorphicDog() throws Exception
    {
        
        Animal animal = MAPPER.readValue("{ \"type\":\"dog\", \"name\":\"Fido\", \"barkVolume\" : 95.0 }", Animal.class);
        assertEquals(Dog.class, animal.getClass());
        assertEquals("Fido", animal.name);
        assertEquals(95.0, ((Dog) animal).barkVolume);
    }

// com.fasterxml.jackson.databind.deser.creators.TestPolymorphicCreators::testManualPolymorphicCatBasic
    public void testManualPolymorphicCatBasic() throws Exception
    {
        
        Animal animal = MAPPER.readValue("{ \"name\" : \"Macavity\", \"type\":\"cat\", \"lives\":18, \"likesCream\":false }", Animal.class);
        assertEquals(Cat.class, animal.getClass());
        assertEquals("Macavity", animal.name); 
        Cat cat = (Cat) animal;
        assertEquals(18, cat.lives);
        
        assertEquals(false, cat.likesCream);
    }

// com.fasterxml.jackson.databind.deser.creators.TestPolymorphicCreators::testManualPolymorphicCatWithReorder
    public void testManualPolymorphicCatWithReorder() throws Exception
    {
        
        Animal animal = MAPPER.readValue("{ \"likesCream\":true, \"name\" : \"Venla\", \"type\":\"cat\" }", Animal.class);
        assertEquals(Cat.class, animal.getClass());
        assertEquals("Venla", animal.name);
        
        assertTrue(((Cat) animal).likesCream);
    }

// com.fasterxml.jackson.databind.deser.creators.TestPolymorphicCreators::testManualPolymorphicWithNumbered
    public void testManualPolymorphicWithNumbered() throws Exception
    {
         final ObjectWriter w = MAPPER.writerFor(AbstractRoot.class);
         final ObjectReader r = MAPPER.readerFor(AbstractRoot.class);

         AbstractRoot input = AbstractRoot.make(1, "oh hai!");
         String json = w.writeValueAsString(input);
         AbstractRoot result = r.readValue(json);
         assertNotNull(result);
         assertEquals("oh hai!", result.getOpt());
    }

// com.fasterxml.jackson.databind.deser.creators.TestPolymorphicDelegating::testAbstractDelegateWithCreator
    public void testAbstractDelegateWithCreator() throws Exception
    {
        Issue580Bean input = new Issue580Bean(new Issue580Impl(13));
        ObjectMapper mapper = new ObjectMapper();
        String json = mapper.writeValueAsString(input);

        Issue580Bean result = mapper.readValue(json, Issue580Bean.class);
        assertNotNull(result);
        assertNotNull(result.value);
        assertEquals(13, ((Issue580Impl) result.value).id);
    }

// com.fasterxml.jackson.databind.deser.creators.TestValueInstantiator::testCustomBeanInstantiator
    public void testCustomBeanInstantiator() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new MyModule(MyBean.class, new MyBeanInstantiator()));
        MyBean bean = mapper.readValue("{}", MyBean.class);
        assertNotNull(bean);
        assertEquals("secret!", bean._secret);
    }

// com.fasterxml.jackson.databind.deser.creators.TestValueInstantiator::testCustomListInstantiator
    public void testCustomListInstantiator() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new MyModule(MyList.class, new MyListInstantiator()));
        MyList result = mapper.readValue("[]", MyList.class);
        assertNotNull(result);
        assertEquals(MyList.class, result.getClass());
        assertEquals(0, result.size());
    }

// com.fasterxml.jackson.databind.deser.creators.TestValueInstantiator::testCustomMapInstantiator
    public void testCustomMapInstantiator() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new MyModule(MyMap.class, new MyMapInstantiator()));
        MyMap result = mapper.readValue("{ \"a\":\"b\" }", MyMap.class);
        assertNotNull(result);
        assertEquals(MyMap.class, result.getClass());
        assertEquals(1, result.size());
    }

// com.fasterxml.jackson.databind.deser.creators.TestValueInstantiator::testDelegateBeanInstantiator
    public void testDelegateBeanInstantiator() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new MyModule(MyBean.class, new MyDelegateBeanInstantiator()));
        MyBean bean = mapper.readValue("123", MyBean.class);
        assertNotNull(bean);
        assertEquals("123", bean._secret);
    }

// com.fasterxml.jackson.databind.deser.creators.TestValueInstantiator::testDelegateListInstantiator
    public void testDelegateListInstantiator() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new MyModule(MyList.class, new MyDelegateListInstantiator()));
        MyList result = mapper.readValue("123", MyList.class);
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(Integer.valueOf(123), result.get(0));
    }

// com.fasterxml.jackson.databind.deser.creators.TestValueInstantiator::testDelegateMapInstantiator
    public void testDelegateMapInstantiator() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new MyModule(MyMap.class, new MyDelegateMapInstantiator()));
        MyMap result = mapper.readValue("123", MyMap.class);
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(Integer.valueOf(123), result.values().iterator().next());
    }

// com.fasterxml.jackson.databind.deser.creators.TestValueInstantiator::testCustomDelegateInstantiator
    public void testCustomDelegateInstantiator() throws Exception
    {
        AnnotatedBeanDelegating value = MAPPER.readValue("{\"a\":3}", AnnotatedBeanDelegating.class);
        assertNotNull(value);
        Object ob = value.value;
        assertNotNull(ob);
        assertTrue(ob instanceof Map);
    }

// com.fasterxml.jackson.databind.deser.creators.TestValueInstantiator::testPropertyBasedBeanInstantiator
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

// com.fasterxml.jackson.databind.deser.creators.TestValueInstantiator::testPropertyBasedMapInstantiator
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

// com.fasterxml.jackson.databind.deser.creators.TestValueInstantiator::testBeanFromString
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

// com.fasterxml.jackson.databind.deser.creators.TestValueInstantiator::testBeanFromInt
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

// com.fasterxml.jackson.databind.deser.creators.TestValueInstantiator::testBeanFromLong
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

// com.fasterxml.jackson.databind.deser.creators.TestValueInstantiator::testBeanFromDouble
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

// com.fasterxml.jackson.databind.deser.creators.TestValueInstantiator::testBeanFromBoolean
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

// com.fasterxml.jackson.databind.deser.creators.TestValueInstantiator::testPolymorphicCreatorBean
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

// com.fasterxml.jackson.databind.deser.creators.TestValueInstantiator::testEmptyBean
    public void testEmptyBean() throws Exception
    {
        AnnotatedBean bean = MAPPER.readValue("{}", AnnotatedBean.class);
        assertNotNull(bean);
        assertEquals("foo", bean.a);
        assertEquals(3, bean.b);
    }

// com.fasterxml.jackson.databind.deser.creators.TestValueInstantiator::testErrorMessageForMissingCtor
    public void testErrorMessageForMissingCtor() throws Exception
    {
        
        try {
            MAPPER.readValue("{ }", MyBean.class);
            fail("Should not succeed");
        } catch (JsonMappingException e) {
            verifyException(e, "Cannot construct instance of");
            verifyException(e, "no Creators");
            
            assertEquals(InvalidDefinitionException.class, e.getClass());
        }
    }

// com.fasterxml.jackson.databind.deser.creators.TestValueInstantiator::testErrorMessageForMissingStringCtor
    public void testErrorMessageForMissingStringCtor() throws Exception
    {
        
        try {
            MAPPER.readValue("\"foo\"", MyBean.class);
            fail("Should not succeed");
        } catch (JsonMappingException e) {
            verifyException(e, "Cannot construct instance of");
            verifyException(e, "no String-argument constructor/factory");
            
            assertEquals(InvalidDefinitionException.class, e.getClass());
        }
    }

// com.fasterxml.jackson.databind.deser.dos.HugeIntegerCoerceTest::testMaliciousLongForEnum
    public void testMaliciousLongForEnum() throws Exception
    {
        
        
        
        try {
             MAPPER.readValue(BIG_POS_INTEGER, ABC.class);
            fail("Should not pass");
        } catch (InputCoercionException e) {
            verifyException(e, "out of range of int");
            verifyException(e, "Integer with "+BIG_NUM_LEN+" digits");
        }
    }

// com.fasterxml.jackson.databind.deser.filter.IgnoreCreatorProp1317Test::testThatJsonIgnoreWorksWithConstructorProperties
    public void testThatJsonIgnoreWorksWithConstructorProperties() throws Exception {
        ObjectMapper om = objectMapper();
        Testing testing = new Testing("shouldBeIgnored", "notIgnore");
        String json = om.writeValueAsString(testing);

        assertFalse(json.contains("shouldBeIgnored"));
    }

// com.fasterxml.jackson.databind.deser.filter.IgnorePropertyOnDeserTest::testIgnoreOnProperty1217
    public void testIgnoreOnProperty1217() throws Exception
    {
        TestIgnoreObject result = MAPPER.readValue(
                aposToQuotes("{'obj':{'x': 10, 'y': 20}, 'obj2':{'x': 10, 'y': 20}}"),
                TestIgnoreObject.class);
        assertEquals(20, result.obj.y);
        assertEquals(10, result.obj2.x);

        assertEquals(1, result.obj.x);
        assertEquals(2, result.obj2.y);

        TestIgnoreObject result1 = MAPPER.readValue(
                  aposToQuotes("{'obj':{'x': 20, 'y': 30}, 'obj2':{'x': 20, 'y': 40}}"),
                  TestIgnoreObject.class);
        assertEquals(1, result1.obj.x);
        assertEquals(30, result1.obj.y);
       
        assertEquals(20, result1.obj2.x);
        assertEquals(2, result1.obj2.y);
    }

// com.fasterxml.jackson.databind.deser.filter.IgnorePropertyOnDeserTest::testIgnoreViaConfigOverride1217
    public void testIgnoreViaConfigOverride1217() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configOverride(Point.class)
            .setIgnorals(JsonIgnoreProperties.Value.forIgnoredProperties("y"));
        Point p = mapper.readValue(aposToQuotes("{'x':1,'y':2}"), Point.class);
        
        assertEquals(1, p.x);
        assertEquals(0, p.y);
    }

// com.fasterxml.jackson.databind.deser.filter.IgnorePropertyOnDeserTest::testIgnoreGetterNotSetter1595
    public void testIgnoreGetterNotSetter1595() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        Simple1595 config = new Simple1595();
        config.setId(123);
        config.setName("jack");
        String json = mapper.writeValueAsString(config);
        assertEquals(aposToQuotes("{'id':123}"), json);
        Simple1595 des = mapper.readValue(aposToQuotes("{'id':123,'name':'jack'}"), Simple1595.class);
        assertEquals("jack", des.getName());
    }

// com.fasterxml.jackson.databind.deser.filter.NullConversionsForContentTest::testFailOnNullFromDefaults
    public void testFailOnNullFromDefaults() throws Exception
    {
        final String JSON = aposToQuotes("{'values':[null]}");
        TypeReference<NullContentUndefined<List<String>>> listType = new TypeReference<NullContentUndefined<List<String>>>() { };

        
        NullContentUndefined<List<String>> result = MAPPER.readValue(JSON, listType);
        assertNotNull(result.values);
        assertEquals(1, result.values.size());
        assertNull(result.values.get(0));

        
        ObjectMapper mapper = newObjectMapper();
        mapper.setDefaultSetterInfo(JsonSetter.Value.forContentNulls(Nulls.FAIL));
        try {
            mapper.readValue(JSON, listType);
            fail("Should not pass");
        } catch (InvalidNullException e) {
            verifyException(e, "property \"values\"");
        }

        
        mapper = newObjectMapper();
        mapper.configOverride(List.class)
                .setSetterInfo(JsonSetter.Value.forContentNulls(Nulls.FAIL));
        try {
            mapper.readValue(JSON, listType);
            fail("Should not pass");
        } catch (InvalidNullException e) {
            verifyException(e, "property \"values\"");
        }
    }

// com.fasterxml.jackson.databind.deser.filter.NullConversionsForContentTest::testFailOnNullWithCollections
    public void testFailOnNullWithCollections() throws Exception
    {
        TypeReference<NullContentFail<List<Integer>>> typeRef = new TypeReference<NullContentFail<List<Integer>>>() { };

        
        NullContentFail<List<Integer>> result = MAPPER.readValue(aposToQuotes("{'nullsOk':[null]}"),
                typeRef);
        assertNotNull(result.nullsOk);
        assertEquals(1, result.nullsOk.size());
        assertNull(result.nullsOk.get(0));

        
        
        
        final String JSON = aposToQuotes("{'noNulls':[null]}");
        try {
            MAPPER.readValue(JSON, typeRef);
            fail("Should not pass");
        } catch (InvalidNullException e) {
            verifyException(e, "property \"noNulls\"");
        }

        
        try {
            MAPPER.readValue(JSON, new TypeReference<NullContentFail<List<String>>>() { });
            fail("Should not pass");
        } catch (InvalidNullException e) {
            verifyException(e, "property \"noNulls\"");
        }
    }

// com.fasterxml.jackson.databind.deser.filter.NullConversionsForContentTest::testFailOnNullWithArrays
    public void testFailOnNullWithArrays() throws Exception
    {
        final String JSON = aposToQuotes("{'noNulls':[null]}");
        
        try {
            MAPPER.readValue(JSON, new TypeReference<NullContentFail<Object[]>>() { });
            fail("Should not pass");
        } catch (InvalidNullException e) {
            verifyException(e, "property \"noNulls\"");
        }

        
        try {
            MAPPER.readValue(JSON, new TypeReference<NullContentFail<String[]>>() { });
            fail("Should not pass");
        } catch (InvalidNullException e) {
            verifyException(e, "property \"noNulls\"");
        }
    }

// com.fasterxml.jackson.databind.deser.filter.NullConversionsForContentTest::testFailOnNullWithPrimitiveArrays
    public void testFailOnNullWithPrimitiveArrays() throws Exception
    {
        final String JSON = aposToQuotes("{'noNulls':[null]}");

        
        try {
            MAPPER.readValue(JSON, new TypeReference<NullContentFail<boolean[]>>() { });
            fail("Should not pass");
        } catch (InvalidNullException e) {
            verifyException(e, "property \"noNulls\"");
        }
        
        try {
            MAPPER.readValue(JSON, new TypeReference<NullContentFail<int[]>>() { });
            fail("Should not pass");
        } catch (InvalidNullException e) {
            verifyException(e, "property \"noNulls\"");
        }
        
        try {
            MAPPER.readValue(JSON, new TypeReference<NullContentFail<double[]>>() { });
            fail("Should not pass");
        } catch (InvalidNullException e) {
            verifyException(e, "property \"noNulls\"");
        }
    }

// com.fasterxml.jackson.databind.deser.filter.NullConversionsForContentTest::testFailOnNullWithMaps
    public void testFailOnNullWithMaps() throws Exception
    {
        
        try {
            final String MAP_JSON = aposToQuotes("{'noNulls':{'a':null}}");
            MAPPER.readValue(MAP_JSON, new TypeReference<NullContentFail<Map<String,String>>>() { });
            fail("Should not pass");
        } catch (InvalidNullException e) {
            verifyException(e, "property \"noNulls\"");
        }

        
        try {
            final String MAP_JSON = aposToQuotes("{'noNulls':{'A':null}}");
            MAPPER.readValue(MAP_JSON, new TypeReference<NullContentFail<EnumMap<ABC,String>>>() { });
            fail("Should not pass");
        } catch (InvalidNullException e) {
            verifyException(e, "property \"noNulls\"");
        }
    }

// com.fasterxml.jackson.databind.deser.filter.NullConversionsForContentTest::testNullsAsEmptyWithCollections
    public void testNullsAsEmptyWithCollections() throws Exception
    {
        final String JSON = aposToQuotes("{'values':[null]}");

        
        {
            NullContentAsEmpty<List<Integer>> result = MAPPER.readValue(JSON,
                    new TypeReference<NullContentAsEmpty<List<Integer>>>() { });
            assertEquals(1, result.values.size());
            assertEquals(Integer.valueOf(0), result.values.get(0));
        }

        
        {
            NullContentAsEmpty<List<String>> result = MAPPER.readValue(JSON,
                    new TypeReference<NullContentAsEmpty<List<String>>>() { });
            assertEquals(1, result.values.size());
            assertEquals("", result.values.get(0));
        }
    }

// com.fasterxml.jackson.databind.deser.filter.NullConversionsForContentTest::testNullsAsEmptyUsingDefaults
    public void testNullsAsEmptyUsingDefaults() throws Exception
    {
        final String JSON = aposToQuotes("{'values':[null]}");
        TypeReference<NullContentUndefined<List<Integer>>> listType = new TypeReference<NullContentUndefined<List<Integer>>>() { };

        
        ObjectMapper mapper = newObjectMapper();
        mapper.setDefaultSetterInfo(JsonSetter.Value.forContentNulls(Nulls.AS_EMPTY));
        NullContentUndefined<List<Integer>> result = mapper.readValue(JSON, listType);
        assertEquals(1, result.values.size());
        assertEquals(Integer.valueOf(0), result.values.get(0));

        
        mapper = newObjectMapper();
        mapper.configOverride(List.class)
                .setSetterInfo(JsonSetter.Value.forContentNulls(Nulls.AS_EMPTY));
        result = mapper.readValue(JSON, listType);
        assertEquals(1, result.values.size());
        assertEquals(Integer.valueOf(0), result.values.get(0));
    }

// com.fasterxml.jackson.databind.deser.filter.NullConversionsForContentTest::testNullsAsEmptyWithArrays
    public void testNullsAsEmptyWithArrays() throws Exception
    {
        
        final String JSON = aposToQuotes("{'values':[null]}");

        
        {
            NullContentAsEmpty<String[]> result = MAPPER.readValue(JSON,
                    new TypeReference<NullContentAsEmpty<String[]>>() { });
            assertEquals(1, result.values.length);
            assertEquals("", result.values[0]);
        }
    }

// com.fasterxml.jackson.databind.deser.filter.NullConversionsForContentTest::testNullsAsEmptyWithPrimitiveArrays
    public void testNullsAsEmptyWithPrimitiveArrays() throws Exception
    {
        final String JSON = aposToQuotes("{'values':[null]}");

        
        {
            NullContentAsEmpty<int[]> result = MAPPER.readValue(JSON,
                    new TypeReference<NullContentAsEmpty<int[]>>() { });
            assertEquals(1, result.values.length);
            assertEquals(0, result.values[0]);
        }

        
        {
            NullContentAsEmpty<long[]> result = MAPPER.readValue(JSON,
                    new TypeReference<NullContentAsEmpty<long[]>>() { });
            assertEquals(1, result.values.length);
            assertEquals(0L, result.values[0]);
        }

        
        {
            NullContentAsEmpty<boolean[]> result = MAPPER.readValue(JSON,
                    new TypeReference<NullContentAsEmpty<boolean[]>>() { });
            assertEquals(1, result.values.length);
            assertEquals(false, result.values[0]);
        }
}

// com.fasterxml.jackson.databind.deser.filter.NullConversionsForContentTest::testNullsAsEmptyWithMaps
    public void testNullsAsEmptyWithMaps() throws Exception
    {
        
        final String MAP_JSON = aposToQuotes("{'values':{'A':null}}");
        {
            NullContentAsEmpty<Map<String,String>> result 
                = MAPPER.readValue(MAP_JSON, new TypeReference<NullContentAsEmpty<Map<String,String>>>() { });
            assertEquals(1, result.values.size());
            assertEquals("A", result.values.entrySet().iterator().next().getKey());
            assertEquals("", result.values.entrySet().iterator().next().getValue());
        }

        
        {
            NullContentAsEmpty<EnumMap<ABC,String>> result 
                = MAPPER.readValue(MAP_JSON, new TypeReference<NullContentAsEmpty<EnumMap<ABC,String>>>() { });
            assertEquals(1, result.values.size());
            assertEquals(ABC.A, result.values.entrySet().iterator().next().getKey());
            assertEquals("", result.values.entrySet().iterator().next().getValue());
        }
    }

// com.fasterxml.jackson.databind.deser.filter.NullConversionsForContentTest::testNullsSkipUsingDefaults
    public void testNullsSkipUsingDefaults() throws Exception
    {
        final String JSON = aposToQuotes("{'values':[null]}");
        TypeReference<NullContentUndefined<List<Long>>> listType = new TypeReference<NullContentUndefined<List<Long>>>() { };

        
        ObjectMapper mapper = newObjectMapper();
        mapper.setDefaultSetterInfo(JsonSetter.Value.forContentNulls(Nulls.SKIP));
        NullContentUndefined<List<Long>> result = mapper.readValue(JSON, listType);
        assertEquals(0, result.values.size());

        
        mapper = newObjectMapper();
        mapper.configOverride(List.class)
                .setSetterInfo(JsonSetter.Value.forContentNulls(Nulls.SKIP));
        result = mapper.readValue(JSON, listType);
        assertEquals(0, result.values.size());
    }

// com.fasterxml.jackson.databind.deser.filter.NullConversionsForContentTest::testNullsSkipWithOverrides
    public void testNullsSkipWithOverrides() throws Exception
    {
        final String JSON = aposToQuotes("{'values':[null]}");
        TypeReference<NullContentSkip<List<Long>>> listType = new TypeReference<NullContentSkip<List<Long>>>() { };

        ObjectMapper mapper = newObjectMapper();
        
        mapper.setDefaultSetterInfo(JsonSetter.Value.forContentNulls(Nulls.FAIL));
        NullContentSkip<List<Long>> result = mapper.readValue(JSON, listType);
        assertEquals(0, result.values.size());

        
        mapper = newObjectMapper();
        mapper.configOverride(List.class)
                .setSetterInfo(JsonSetter.Value.forContentNulls(Nulls.FAIL));
        result = mapper.readValue(JSON, listType);
        assertEquals(0, result.values.size());
    }

// com.fasterxml.jackson.databind.deser.filter.NullConversionsForContentTest::testNullsSkipWithCollections
    public void testNullsSkipWithCollections() throws Exception
    {
        
        {
            final String JSON = aposToQuotes("{'values':[1,null,2]}");
            NullContentSkip<List<Integer>> result = MAPPER.readValue(JSON,
                    new TypeReference<NullContentSkip<List<Integer>>>() { });
            assertEquals(2, result.values.size());
            assertEquals(Integer.valueOf(1), result.values.get(0));
            assertEquals(Integer.valueOf(2), result.values.get(1));
        }

        
        {
            final String JSON = aposToQuotes("{'values':['ab',null,'xy']}");
            NullContentSkip<List<String>> result = MAPPER.readValue(JSON,
                    new TypeReference<NullContentSkip<List<String>>>() { });
            assertEquals(2, result.values.size());
            assertEquals("ab", result.values.get(0));
            assertEquals("xy", result.values.get(1));
        }
    }

// com.fasterxml.jackson.databind.deser.filter.NullConversionsForContentTest::testNullsSkipWithArrays
    public void testNullsSkipWithArrays() throws Exception
    {
        final String JSON = aposToQuotes("{'values':['a',null,'xy']}");
        
        {
            NullContentSkip<Object[]> result = MAPPER.readValue(JSON,
                    new TypeReference<NullContentSkip<Object[]>>() { });
            assertEquals(2, result.values.length);
            assertEquals("a", result.values[0]);
            assertEquals("xy", result.values[1]);
        }
        
        {
            NullContentSkip<String[]> result = MAPPER.readValue(JSON,
                    new TypeReference<NullContentSkip<String[]>>() { });
            assertEquals(2, result.values.length);
            assertEquals("a", result.values[0]);
            assertEquals("xy", result.values[1]);
        }
    }

// com.fasterxml.jackson.databind.deser.filter.NullConversionsForContentTest::testNullsSkipWithPrimitiveArrays
    public void testNullsSkipWithPrimitiveArrays() throws Exception
    {
        
        {
            final String JSON = aposToQuotes("{'values':[3,null,7]}");
            NullContentSkip<int[]> result = MAPPER.readValue(JSON,
                    new TypeReference<NullContentSkip<int[]>>() { });
            assertEquals(2, result.values.length);
            assertEquals(3, result.values[0]);
            assertEquals(7, result.values[1]);
        }

        
        {
            final String JSON = aposToQuotes("{'values':[-13,null,999]}");
            NullContentSkip<long[]> result = MAPPER.readValue(JSON,
                    new TypeReference<NullContentSkip<long[]>>() { });
            assertEquals(2, result.values.length);
            assertEquals(-13L, result.values[0]);
            assertEquals(999L, result.values[1]);
        }

        
        {
            final String JSON = aposToQuotes("{'values':[true,null,true]}");
            NullContentSkip<boolean[]> result = MAPPER.readValue(JSON,
                    new TypeReference<NullContentSkip<boolean[]>>() { });
            assertEquals(2, result.values.length);
            assertEquals(true, result.values[0]);
            assertEquals(true, result.values[1]);
        }
    }

// com.fasterxml.jackson.databind.deser.filter.NullConversionsForContentTest::testNullsSkipWithMaps
    public void testNullsSkipWithMaps() throws Exception
    {
        
        final String MAP_JSON = aposToQuotes("{'values':{'A':'foo','B':null,'C':'bar'}}");
        {
            NullContentSkip<Map<String,String>> result 
                = MAPPER.readValue(MAP_JSON, new TypeReference<NullContentSkip<Map<String,String>>>() { });
            assertEquals(2, result.values.size());
            assertEquals("foo", result.values.get("A"));
            assertEquals("bar", result.values.get("C"));
        }

        
        {
            NullContentSkip<EnumMap<ABC,String>> result 
                = MAPPER.readValue(MAP_JSON, new TypeReference<NullContentSkip<EnumMap<ABC,String>>>() { });
            assertEquals(2, result.values.size());
            assertEquals("foo", result.values.get(ABC.A));
            assertEquals("bar", result.values.get(ABC.C));
        }
    }

// com.fasterxml.jackson.databind.deser.filter.NullConversionsGenericTest::testNullsToEmptyPojo
    public void testNullsToEmptyPojo() throws Exception
    {
        GeneralEmpty<Point> result = MAPPER.readValue(aposToQuotes("{'value':null}"),
                new TypeReference<GeneralEmpty<Point>>() { });
        assertNotNull(result.value);
        Point p = result.value;
        assertEquals(0, p.x);
        assertEquals(0, p.y);

        
        try {
             MAPPER.readValue(aposToQuotes("{'value':null}"),
                    NoCtorWrapper.class);
            fail("Should not pass");
        } catch (JsonMappingException e) {
            verifyException(e, "Cannot create empty instance");
        }
    }

// com.fasterxml.jackson.databind.deser.filter.NullConversionsGenericTest::testEmptyStringToNullToEmptyPojo
    public void testEmptyStringToNullToEmptyPojo() throws Exception
    {
        GeneralEmpty<Point> result = MAPPER.readerFor(new TypeReference<GeneralEmpty<Point>>() { })
                .with(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT)
                .readValue(aposToQuotes("{'value':''}"));
        assertNotNull(result.value);
        Point p = result.value;
        assertEquals(0, p.x);
        assertEquals(0, p.y);
    }

// com.fasterxml.jackson.databind.deser.filter.NullConversionsGenericTest::testNullsToEmptyCollection
    public void testNullsToEmptyCollection() throws Exception
    {
        GeneralEmpty<List<String>> result = MAPPER.readValue(aposToQuotes("{'value':null}"),
                new TypeReference<GeneralEmpty<List<String>>>() { });
        assertNotNull(result.value);
        assertEquals(0, result.value.size());

        
        GeneralEmpty<List<Integer>> result2 = MAPPER.readValue(aposToQuotes("{'value':null}"),
                new TypeReference<GeneralEmpty<List<Integer>>>() { });
        assertNotNull(result2.value);
        assertEquals(0, result2.value.size());
    }

// com.fasterxml.jackson.databind.deser.filter.NullConversionsGenericTest::testNullsToEmptyMap
    public void testNullsToEmptyMap() throws Exception
    {
        GeneralEmpty<Map<String,String>> result = MAPPER.readValue(aposToQuotes("{'value':null}"),
                new TypeReference<GeneralEmpty<Map<String,String>>>() { });
        assertNotNull(result.value);
        assertEquals(0, result.value.size());
    }

// com.fasterxml.jackson.databind.deser.filter.NullConversionsGenericTest::testNullsToEmptyArrays
    public void testNullsToEmptyArrays() throws Exception
    {
        final String json = aposToQuotes("{'value':null}");

        GeneralEmpty<Object[]> result = MAPPER.readValue(json,
                new TypeReference<GeneralEmpty<Object[]>>() { });
        assertNotNull(result.value);
        assertEquals(0, result.value.length);

        GeneralEmpty<String[]> result2 = MAPPER.readValue(json,
                new TypeReference<GeneralEmpty<String[]>>() { });
        assertNotNull(result2.value);
        assertEquals(0, result2.value.length);

        GeneralEmpty<int[]> result3 = MAPPER.readValue(json,
                new TypeReference<GeneralEmpty<int[]>>() { });
        assertNotNull(result3.value);
        assertEquals(0, result3.value.length);

        GeneralEmpty<double[]> result4 = MAPPER.readValue(json,
                new TypeReference<GeneralEmpty<double[]>>() { });
        assertNotNull(result4.value);
        assertEquals(0, result4.value.length);

        GeneralEmpty<boolean[]> result5 = MAPPER.readValue(json,
                new TypeReference<GeneralEmpty<boolean[]>>() { });
        assertNotNull(result5.value);
        assertEquals(0, result5.value.length);
    }

// com.fasterxml.jackson.databind.deser.filter.NullConversionsPojoTest::testFailOnNull
    public void testFailOnNull() throws Exception
    {
        
        NullFail result = MAPPER.readValue(aposToQuotes("{'noNulls':'foo', 'nullsOk':null}"),
                NullFail.class);
        assertEquals("foo", result.noNulls);
        assertNull(result.nullsOk);

        
        try {
            result = MAPPER.readValue(aposToQuotes("{'noNulls':null}"),
                    NullFail.class);
            fail("Should not pass");
        } catch (InvalidNullException e) {
            verifyException(e, "property \"noNulls\"");
        }
    }

// com.fasterxml.jackson.databind.deser.filter.NullConversionsPojoTest::testFailOnNullWithDefaults
    public void testFailOnNullWithDefaults() throws Exception
    {
        
        String json = aposToQuotes("{'name':null}");
        NullsForString def = MAPPER.readValue(json, NullsForString.class);
        assertNull(def.getName());
        
        ObjectMapper mapper = newObjectMapper();
        mapper.configOverride(String.class)
            .setSetterInfo(JsonSetter.Value.forValueNulls(Nulls.FAIL));
        try {
            mapper.readValue(json, NullsForString.class);
            fail("Should not pass");
        } catch (InvalidNullException e) {
            verifyException(e, "property \"name\"");
        }
    }

// com.fasterxml.jackson.databind.deser.filter.NullConversionsPojoTest::testNullsToEmptyScalar
    public void testNullsToEmptyScalar() throws Exception
    {
        NullAsEmpty result = MAPPER.readValue(aposToQuotes("{'nullAsEmpty':'foo', 'nullsOk':null}"),
                NullAsEmpty.class);
        assertEquals("foo", result.nullAsEmpty);
        assertNull(result.nullsOk);

        
        result = MAPPER.readValue(aposToQuotes("{'nullAsEmpty':null}"),
                NullAsEmpty.class);
        assertEquals("", result.nullAsEmpty);

        
        String json = aposToQuotes("{'name':null}");
        NullsForString def = MAPPER.readValue(json, NullsForString.class);
        assertNull(def.getName());

        ObjectMapper mapper = newObjectMapper();
        mapper.configOverride(String.class)
            .setSetterInfo(JsonSetter.Value.forValueNulls(Nulls.AS_EMPTY));
        NullsForString named = mapper.readValue(json, NullsForString.class);
        assertEquals("", named.getName());
    }

// com.fasterxml.jackson.databind.deser.filter.NullConversionsSkipTest::testSkipNullField
    public void testSkipNullField() throws Exception
    {
        
        NullSkipField result = MAPPER.readValue(aposToQuotes("{'noNulls':'foo', 'nullsOk':null}"),
                NullSkipField.class);
        assertEquals("foo", result.noNulls);
        assertNull(result.nullsOk);

        
        result = MAPPER.readValue(aposToQuotes("{'noNulls':null}"),
                NullSkipField.class);
        assertEquals("b", result.noNulls);
        assertEquals("a", result.nullsOk);
    }

// com.fasterxml.jackson.databind.deser.filter.NullConversionsSkipTest::testSkipNullMethod
    public void testSkipNullMethod() throws Exception
    {
        NullSkipMethod result = MAPPER.readValue(aposToQuotes("{'noNulls':'foo', 'nullsOk':null}"),
                NullSkipMethod.class);
        assertEquals("foo", result._noNulls);
        assertNull(result._nullsOk);

        result = MAPPER.readValue(aposToQuotes("{'noNulls':null}"),
                NullSkipMethod.class);
        assertEquals("b", result._noNulls);
        assertEquals("a", result._nullsOk);
    }

// com.fasterxml.jackson.databind.deser.filter.NullConversionsSkipTest::testEnumAsNullThenSkip
    public void testEnumAsNullThenSkip() throws Exception
    {    
        Pojo2015 p = MAPPER.readerFor(Pojo2015.class)
                .with(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_AS_NULL)
                .readValue("{\"number\":\"THREE\"}"); 
        assertEquals(NUMS2015.TWO, p.number);
    }

// com.fasterxml.jackson.databind.deser.filter.NullConversionsSkipTest::testSkipNullWithDefaults
    public void testSkipNullWithDefaults() throws Exception
    {
        String json = aposToQuotes("{'value':null}");
        StringValue result = MAPPER.readValue(json, StringValue.class);
        assertNull(result.value);

        ObjectMapper mapper = newObjectMapper();
        mapper.configOverride(String.class)
            .setSetterInfo(JsonSetter.Value.forValueNulls(Nulls.SKIP));
        result = mapper.readValue(json, StringValue.class);
        assertEquals("default", result.value);
    }

// com.fasterxml.jackson.databind.deser.filter.ProblemHandlerLocation1440Test::testIncorrectContext
    public void testIncorrectContext() throws Exception
    {
        
        final String invalidInput = aposToQuotes(
"{'actor': {'id': 'actor_id','type': 'actor_type',"
+"'status': 'actor_status','context':'actor_context','invalid_1': 'actor_invalid_1'},"
+"'verb': 'verb','object': {'id': 'object_id','type': 'object_type',"
+"'invalid_2': 'object_invalid_2','status': 'object_status','context': 'object_context'},"
+"'target': {'id': 'target_id','type': 'target_type','invalid_3': 'target_invalid_3',"
+"'invalid_4': 'target_invalid_4','status': 'target_status','context': 'target_context'}}"
);

        ObjectMapper mapper = newObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        final DeserializationProblemLogger logger = new DeserializationProblemLogger();
        mapper.addHandler(logger);
        mapper.readValue(invalidInput, Activity.class);

        List<String> probs = logger.problems();
        assertEquals(4, probs.size());
        assertEquals("actor.invalid_1#invalid_1", probs.get(0));
        assertEquals("object.invalid_2#invalid_2", probs.get(1));
        assertEquals("target.invalid_3#invalid_3", probs.get(2));
        assertEquals("target.invalid_4#invalid_4", probs.get(3));
    }

// com.fasterxml.jackson.databind.deser.filter.ProblemHandlerTest::testWeirdKeyHandling
    public void testWeirdKeyHandling() throws Exception
    {
        ObjectMapper mapper = newObjectMapper()
            .addHandler(new WeirdKeyHandler(7));
        IntKeyMapWrapper w = mapper.readValue("{\"stuff\":{\"foo\":\"abc\"}}",
                IntKeyMapWrapper.class);
        Map<Integer,String> map = w.stuff;
        assertEquals(1, map.size());
        assertEquals("abc", map.values().iterator().next());
        assertEquals(Integer.valueOf(7), map.keySet().iterator().next());
    }

// com.fasterxml.jackson.databind.deser.filter.ProblemHandlerTest::testWeirdNumberHandling
    public void testWeirdNumberHandling() throws Exception
    {
        ObjectMapper mapper = newObjectMapper()
            .addHandler(new WeirdNumberHandler(SingleValuedEnum.A))
            ;
        SingleValuedEnum result = mapper.readValue("3", SingleValuedEnum.class);
        assertEquals(SingleValuedEnum.A, result);
    }

// com.fasterxml.jackson.databind.deser.filter.ProblemHandlerTest::testWeirdStringHandling
    public void testWeirdStringHandling() throws Exception
    {
        ObjectMapper mapper = newObjectMapper()
            .addHandler(new WeirdStringHandler(SingleValuedEnum.A))
            ;
        SingleValuedEnum result = mapper.readValue("\"B\"", SingleValuedEnum.class);
        assertEquals(SingleValuedEnum.A, result);

        
        mapper = new ObjectMapper()
                .addHandler(new WeirdStringHandler(null));
        UUID result2 = mapper.readValue(quote("not a uuid!"), UUID.class);
        assertNull(result2);
    }

// com.fasterxml.jackson.databind.deser.filter.ProblemHandlerTest::testInvalidTypeId
    public void testInvalidTypeId() throws Exception
    {
        ObjectMapper mapper = newObjectMapper()
            .addHandler(new UnknownTypeIdHandler(BaseImpl.class));
        BaseWrapper w = mapper.readValue("{\"value\":{\"type\":\"foo\",\"a\":4}}",
                BaseWrapper.class);
        assertNotNull(w);
        assertEquals(BaseImpl.class, w.value.getClass());
    }

// com.fasterxml.jackson.databind.deser.filter.ProblemHandlerTest::testInvalidClassAsId
    public void testInvalidClassAsId() throws Exception
    {
        ObjectMapper mapper = newObjectMapper()
            .addHandler(new UnknownTypeIdHandler(Base2Impl.class));
        Base2Wrapper w = mapper.readValue("{\"value\":{\"clazz\":\"com.fizz\",\"a\":4}}",
                Base2Wrapper.class);
        assertNotNull(w);
        assertEquals(Base2Impl.class, w.value.getClass());
    }

// com.fasterxml.jackson.databind.deser.filter.ProblemHandlerTest::testMissingTypeId
    public void testMissingTypeId() throws Exception
    {
        ObjectMapper mapper = newObjectMapper()
            .addHandler(new MissingTypeIdHandler(BaseImpl.class));
        BaseWrapper w = mapper.readValue("{\"value\":{\"a\":4}}",
                BaseWrapper.class);
        assertNotNull(w);
        assertEquals(BaseImpl.class, w.value.getClass());
    }

// com.fasterxml.jackson.databind.deser.filter.ProblemHandlerTest::testMissingClassAsId
    public void testMissingClassAsId() throws Exception
    {
        ObjectMapper mapper = newObjectMapper()
            .addHandler(new MissingTypeIdHandler(Base2Impl.class));
        Base2Wrapper w = mapper.readValue("{\"value\":{\"a\":4}}",
                Base2Wrapper.class);
        assertNotNull(w);
        assertEquals(Base2Impl.class, w.value.getClass());
    }

// com.fasterxml.jackson.databind.deser.filter.ProblemHandlerTest::testInvalidTypeIdFail
    public void testInvalidTypeIdFail() throws Exception
    {
        try {
            MAPPER.readValue("{\"value\":{\"type\":\"foo\",\"a\":4}}",
                BaseWrapper.class);
            fail("Should not pass");
        } catch (InvalidTypeIdException e) {
            verifyException(e, "Could not resolve type id 'foo'");
            assertEquals(Base.class, e.getBaseType().getRawClass());
            assertEquals("foo", e.getTypeId());
        }
    }

// com.fasterxml.jackson.databind.deser.filter.ProblemHandlerTest::testInstantiationExceptionHandling
    public void testInstantiationExceptionHandling() throws Exception
    {
        ObjectMapper mapper = newObjectMapper()
            .addHandler(new InstantiationProblemHandler(BustedCtor.INST));
        BustedCtor w = mapper.readValue("{ }",
                BustedCtor.class);
        assertNotNull(w);
    }

// com.fasterxml.jackson.databind.deser.filter.ProblemHandlerTest::testMissingInstantiatorHandling
    public void testMissingInstantiatorHandling() throws Exception
    {
        ObjectMapper mapper = newObjectMapper()
            .addHandler(new MissingInstantiationHandler(new NoDefaultCtor(13)))
            ;
        NoDefaultCtor w = mapper.readValue("{ \"x\" : true }", NoDefaultCtor.class);
        assertNotNull(w);
        assertEquals(13, w.value);
    }

// com.fasterxml.jackson.databind.deser.filter.ProblemHandlerTest::testUnexpectedTokenHandling
    public void testUnexpectedTokenHandling() throws Exception
    {
        ObjectMapper mapper = newObjectMapper()
            .addHandler(new WeirdTokenHandler(Integer.valueOf(13)))
        ;
        Integer v = mapper.readValue("true", Integer.class);
        assertEquals(Integer.valueOf(13), v);
    }

// com.fasterxml.jackson.databind.deser.filter.ProblemHandlerUnknownTypeId2221Test::testWithDeserializationProblemHandler
    public void testWithDeserializationProblemHandler() throws Exception {
        final ObjectMapper mapper = new ObjectMapper()
                .enableDefaultTyping();
        mapper.addHandler(new DeserializationProblemHandler() {
            @Override
            public JavaType handleUnknownTypeId(DeserializationContext ctxt, JavaType baseType, String subTypeId, TypeIdResolver idResolver, String failureMsg) throws IOException {

                return ctxt.constructType(Void.class);
            }
        });
        GenericContent processableContent = mapper.readValue(JSON, GenericContent.class);
        assertNotNull(processableContent.getInnerObjects());
        assertEquals(2, processableContent.getInnerObjects().size());
    }

// com.fasterxml.jackson.databind.deser.filter.ProblemHandlerUnknownTypeId2221Test::testWithDisabledFAIL_ON_INVALID_SUBTYPE
    public void testWithDisabledFAIL_ON_INVALID_SUBTYPE() throws Exception {
        final ObjectMapper mapper = new ObjectMapper()
                .disable(DeserializationFeature.FAIL_ON_INVALID_SUBTYPE)
                .enableDefaultTyping()
        ;
        GenericContent processableContent = mapper.readValue(JSON, GenericContent.class);
        assertNotNull(processableContent.getInnerObjects());
        assertEquals(2, processableContent.getInnerObjects().size());
    }

// com.fasterxml.jackson.databind.deser.filter.ReadOnlyDeser1890Test::testDeserializeAnnotationsOneField
   public void testDeserializeAnnotationsOneField() throws IOException {
       PersonAnnotations person = MAPPER.readValue("{\"testEnum\":\"\"}", PersonAnnotations.class);
       
       assertEquals(null, person.getTestEnum());
       assertNull(person.name);
   }

// com.fasterxml.jackson.databind.deser.filter.ReadOnlyDeser1890Test::testDeserializeAnnotationsTwoFields
   public void testDeserializeAnnotationsTwoFields() throws IOException {
       PersonAnnotations person = MAPPER.readValue("{\"testEnum\":\"\",\"name\":\"changyong\"}",
               PersonAnnotations.class);
       
       assertEquals(null, person.getTestEnum());
       assertEquals("changyong", person.name);
   }

// com.fasterxml.jackson.databind.deser.filter.ReadOnlyDeser1890Test::testDeserializeOneField
   public void testDeserializeOneField() throws IOException {
       Person person = MAPPER.readValue("{\"testEnum\":\"\"}", Person.class);
       assertEquals(TestEnum.DEFAULT, person.getTestEnum());
       assertNull(person.name);
   }

// com.fasterxml.jackson.databind.deser.filter.ReadOnlyDeser1890Test::testDeserializeTwoFields
   public void testDeserializeTwoFields() throws IOException {
       Person person = MAPPER.readValue("{\"testEnum\":\"\",\"name\":\"changyong\"}",
               Person.class);
       assertEquals(TestEnum.DEFAULT, person.getTestEnum());
       assertEquals("changyong", person.name);
   }

// com.fasterxml.jackson.databind.deser.filter.ReadOnlyDeser95Test::testReadOnlyProp
    public void testReadOnlyProp() throws Exception
    {
        ObjectMapper m = new ObjectMapper();
        String json = m.writeValueAsString(new ReadOnlyBean());
        if (json.indexOf("computed") < 0) {
            fail("Should have property 'computed', didn't: "+json);
        }
        ReadOnlyBean bean = m.readValue(json, ReadOnlyBean.class);
        assertNotNull(bean);
    }

// com.fasterxml.jackson.databind.deser.filter.RecursiveIgnorePropertiesTest::testRecursiveForDeser
    public void testRecursiveForDeser() throws Exception
    {
        String st = aposToQuotes("{ 'name': 'admin',\n"
                + "    'person_z': { 'name': 'wyatt' }"
                + "}");
        Person result = MAPPER.readValue(st, Person.class);
        assertEquals("admin", result.name);
        assertNotNull(result.personZ);
        assertEquals("wyatt", result.personZ.name);
    }

// com.fasterxml.jackson.databind.deser.filter.RecursiveIgnorePropertiesTest::testRecursiveWithCollectionDeser
    public void testRecursiveWithCollectionDeser() throws Exception
    {
        String st = aposToQuotes("{ 'name': 'admin',\n"
                + "    'person_z': [ { 'name': 'Foor' }, { 'name' : 'Bar' } ]"
                + "}");
        Persons result = MAPPER.readValue(st, Persons.class);
        assertEquals("admin", result.name);
        assertNotNull(result.personZ);
        assertEquals(2, result.personZ.size());
    }

// com.fasterxml.jackson.databind.deser.filter.RecursiveIgnorePropertiesTest::testRecursiveForSer
    public void testRecursiveForSer() throws Exception
    {
        Person input = new Person();
        input.name = "Bob";
        Person p2 = new Person();
        p2.name = "Bill";
        input.personZ = p2;
        p2.personZ = input;

        String json = MAPPER.writeValueAsString(input);
        assertNotNull(json);
    }

// com.fasterxml.jackson.databind.deser.filter.TestUnknownPropertyDeserialization::testUnknownHandlingDefault
    public void testUnknownHandlingDefault() throws Exception
    {
        try {
            MAPPER.readValue(new StringReader(JSON_UNKNOWN_FIELD), TestBean.class);
        } catch (JsonMappingException jex) {
            verifyException(jex, "Unrecognized field \"foo\"");
        }
    }

// com.fasterxml.jackson.databind.deser.filter.TestUnknownPropertyDeserialization::testUnknownHandlingIgnoreWithHandler
    public void testUnknownHandlingIgnoreWithHandler() throws Exception
    {
        ObjectMapper mapper = newObjectMapper();
        mapper.clearProblemHandlers();
        mapper.addHandler(new MyHandler());
        TestBean result = mapper.readValue(new StringReader(JSON_UNKNOWN_FIELD), TestBean.class);
        assertNotNull(result);
        assertEquals(1, result._a);
        assertEquals(-1, result._b);
        assertEquals("foo:START_ARRAY", result._unknown);
    }

// com.fasterxml.jackson.databind.deser.filter.TestUnknownPropertyDeserialization::testUnknownHandlingIgnoreWithHandlerAndObjectReader
    public void testUnknownHandlingIgnoreWithHandlerAndObjectReader() throws Exception
    {
        ObjectMapper mapper = newObjectMapper();
        mapper.clearProblemHandlers();
        TestBean result = mapper.readerFor(TestBean.class).withHandler(new MyHandler()).readValue(new StringReader(JSON_UNKNOWN_FIELD));
        assertNotNull(result);
        assertEquals(1, result._a);
        assertEquals(-1, result._b);
        assertEquals("foo:START_ARRAY", result._unknown);
    }

// com.fasterxml.jackson.databind.deser.filter.TestUnknownPropertyDeserialization::testUnknownHandlingIgnoreWithFeature
    public void testUnknownHandlingIgnoreWithFeature() throws Exception
    {
        ObjectMapper mapper = newObjectMapper();
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

// com.fasterxml.jackson.databind.deser.filter.TestUnknownPropertyDeserialization::testWithClassIgnore
    public void testWithClassIgnore() throws Exception
    {
        IgnoreSome result = MAPPER.readValue("{ \"a\":1,\"b\":2,\"c\":\"x\",\"d\":\"y\"}",
                IgnoreSome.class);
        
        assertEquals(1, result.a);
        assertEquals("y", result.d());
        
        assertEquals(0, result.b);
        assertNull(result.c());
    }

// com.fasterxml.jackson.databind.deser.filter.TestUnknownPropertyDeserialization::testClassIgnoreWithMap
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

// com.fasterxml.jackson.databind.deser.filter.TestUnknownPropertyDeserialization::testClassWithIgnoreUnknown
    public void testClassWithIgnoreUnknown() throws Exception
    {
        IgnoreUnknown result = MAPPER.readValue
            ("{\"b\":3,\"c\":[1,2],\"x\":{ },\"a\":-3}", IgnoreUnknown.class);
        assertEquals(-3, result.a);
    }

// com.fasterxml.jackson.databind.deser.filter.TestUnknownPropertyDeserialization::testClassWithUnknownAndIgnore
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

// com.fasterxml.jackson.databind.deser.filter.TestUnknownPropertyDeserialization::testPropertyIgnoral
    public void testPropertyIgnoral() throws Exception
    {
        XYZWrapper1 result = MAPPER.readValue("{\"value\":{\"y\":2,\"x\":1,\"z\":3}}", XYZWrapper1.class);
        assertEquals(2, result.value.y);
        assertEquals(3, result.value.z);
    }

// com.fasterxml.jackson.databind.deser.filter.TestUnknownPropertyDeserialization::testPropertyIgnoralWithClass
    public void testPropertyIgnoralWithClass() throws Exception
    {
        XYZWrapper2 result = MAPPER.readValue("{\"value\":{\"y\":2,\"x\":1,\"z\":3}}",
                XYZWrapper2.class);
        assertEquals(1, result.value.x);
    }

// com.fasterxml.jackson.databind.deser.filter.TestUnknownPropertyDeserialization::testPropertyIgnoralForMap
    public void testPropertyIgnoralForMap() throws Exception
    {
        MapWithoutX result = MAPPER.readValue("{\"values\":{\"x\":1,\"y\":2}}", MapWithoutX.class);
        assertNotNull(result.values);
        assertEquals(1, result.values.size());
        assertEquals(Integer.valueOf(2), result.values.get("y"));
    }

// com.fasterxml.jackson.databind.deser.filter.TestUnknownPropertyDeserialization::testIssue987
    public void testIssue987() throws Exception
    {
        ObjectMapper jsonMapper = newObjectMapper();
        jsonMapper.addHandler(new DeserializationProblemHandler() {
            @Override
            public boolean handleUnknownProperty(DeserializationContext ctxt, JsonParser p, JsonDeserializer<?> deserializer, Object beanOrClass, String propertyName) throws IOException, JsonProcessingException {
                p.skipChildren();
                return true;
            }
        });

        String input = "[{\"aProperty\":\"x\",\"unknown\":{\"unknown\":{}}}]";
        List<Bean987> deserializedList = jsonMapper.readValue(input,
                new TypeReference<List<Bean987>>() { });
        assertEquals(1, deserializedList.size());
    }

// com.fasterxml.jackson.databind.deser.inject.InvalidInjectionTest::testInvalidDup
    public void testInvalidDup() throws Exception
    {
        try {
            MAPPER.readValue("{}", BadBean1.class);
            fail("Should not pass");
        } catch (InvalidDefinitionException e) {
            verifyException(e, "Duplicate injectable value");
        }
        try {
            MAPPER.readValue("{}", BadBean2.class);
            fail("Should not pass");
        } catch (InvalidDefinitionException e) {
            verifyException(e, "Duplicate injectable value");
        }
    }

// com.fasterxml.jackson.databind.deser.inject.TestInjectables::testSimple
    public void testSimple() throws Exception
    {
        ObjectMapper mapper = newObjectMapper();
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

// com.fasterxml.jackson.databind.deser.inject.TestInjectables::testWithCtors
    public void testWithCtors() throws Exception
    {
        CtorBean bean = MAPPER.readerFor(CtorBean.class)
            .with(new InjectableValues.Std()
                .addValue(String.class, "Bubba"))
            .readValue("{\"age\":55}");
        assertEquals(55, bean.age);
        assertEquals("Bubba", bean.name);
    }

// com.fasterxml.jackson.databind.deser.inject.TestInjectables::testTwoInjectablesViaCreator
    public void testTwoInjectablesViaCreator() throws Exception
    {
        CtorBean2 bean = MAPPER.readerFor(CtorBean2.class)
                .with(new InjectableValues.Std()
                    .addValue(String.class, "Bob")
                    .addValue("number", Integer.valueOf(13))
                ).readValue("{ }");
        assertEquals(Integer.valueOf(13), bean.age);
        assertEquals("Bob", bean.name);
    }

// com.fasterxml.jackson.databind.deser.inject.TestInjectables::testIssue471
    public void testIssue471() throws Exception
    {
        final Object constructorInjected = "constructorInjected";
        final Object methodInjected = "methodInjected";
        final Object fieldInjected = "fieldInjected";

        ObjectMapper mapper = newObjectMapper()
                        .setInjectableValues(new InjectableValues.Std()
                                .addValue("constructor_injected", constructorInjected)
                                .addValue("method_injected", methodInjected)
                                .addValue("field_injected", fieldInjected));

        Bean471 bean = mapper.readValue(aposToQuotes(
"{'x':13,'constructor_value':'constructor','method_value':'method','field_value':'field'}"),
                Bean471.class);

        
        assertSame(constructorInjected, bean.constructorInjected);
        assertSame(methodInjected, bean.methodInjected);
        assertSame(fieldInjected, bean.fieldInjected);

        
        assertEquals("constructor", bean.constructorValue);
        assertEquals("method", bean.methodValue);
        assertEquals("field", bean.fieldValue);

        assertEquals(13, bean.x);
    }

// com.fasterxml.jackson.databind.deser.inject.TestInjectables::testTransientField
    public void testTransientField() throws Exception
    {
        TransientBean bean = MAPPER.readerFor(TransientBean.class)
                .with(new InjectableValues.Std()
                        .addValue("transient", "Injected!"))
                .readValue("{\"value\":28}");
        assertEquals(28, bean.value);
        assertEquals("Injected!", bean.injected);
    }

// com.fasterxml.jackson.databind.deser.jdk.Base64DecodingTest::testInvalidBase64
    public void testInvalidBase64() throws Exception
    {
        byte[] b = MAPPER.readValue(quote(BASE64_HELLO), byte[].class);
        assertEquals(HELLO_BYTES, b);

        _testInvalidBase64(MAPPER, BASE64_HELLO+"!");
        _testInvalidBase64(MAPPER, BASE64_HELLO+"!!");
    }

// com.fasterxml.jackson.databind.deser.jdk.CollectionDeserTest::testUntypedList
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

// com.fasterxml.jackson.databind.deser.jdk.CollectionDeserTest::testExactStringCollection
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

// com.fasterxml.jackson.databind.deser.jdk.CollectionDeserTest::testHashSet
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

// com.fasterxml.jackson.databind.deser.jdk.CollectionDeserTest::testCustomDeserializer
    public void testCustomDeserializer() throws IOException
    {
        CustomList result = MAPPER.readValue(quote("abc"), CustomList.class);
        assertEquals(1, result.size());
        assertEquals("abc", result.get(0));
    }

// com.fasterxml.jackson.databind.deser.jdk.CollectionDeserTest::testImplicitArrays
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

// com.fasterxml.jackson.databind.deser.jdk.CollectionDeserTest::testFromEmptyString
    public void testFromEmptyString() throws Exception
    {
        ObjectReader r = MAPPER.reader(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT);
        List<?> result = r.forType(List.class).readValue(quote(""));
        assertNull(result);
    }

// com.fasterxml.jackson.databind.deser.jdk.CollectionDeserTest::testArrayBlockingQueue
    public void testArrayBlockingQueue() throws Exception
    {
        
        ArrayBlockingQueue<?> q = MAPPER.readValue("[1, 2, 3]", ArrayBlockingQueue.class);
        assertNotNull(q);
        assertEquals(3, q.size());
        assertEquals(Integer.valueOf(1), q.take());
        assertEquals(Integer.valueOf(2), q.take());
        assertEquals(Integer.valueOf(3), q.take());
    }

// com.fasterxml.jackson.databind.deser.jdk.CollectionDeserTest::testIterableWithStrings
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

// com.fasterxml.jackson.databind.deser.jdk.CollectionDeserTest::testIterableWithBeans
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

// com.fasterxml.jackson.databind.deser.jdk.CollectionDeserTest::testArrayIndexForExceptions
    public void testArrayIndexForExceptions() throws Exception
    {
        final String OBJECTS_JSON = "[ \"KEY2\", false ]";
        try {
            MAPPER.readValue(OBJECTS_JSON, Key[].class);
            fail("Should not pass");
        } catch (JsonMappingException e) {
            verifyException(e, "Cannot deserialize");
            List<JsonMappingException.Reference> refs = e.getPath();
            assertEquals(1, refs.size());
            assertEquals(1, refs.get(0).getIndex());
        }

        try {
            MAPPER.readValue("[ \"xyz\", { } ]", String[].class);
            fail("Should not pass");
        } catch (JsonMappingException e) {
            verifyException(e, "Cannot deserialize");
            List<JsonMappingException.Reference> refs = e.getPath();
            assertEquals(1, refs.size());
            assertEquals(1, refs.get(0).getIndex());
        }

        try {
            MAPPER.readValue("{\"keys\":"+OBJECTS_JSON+"}", KeyListBean.class);
            fail("Should not pass");
        } catch (JsonMappingException e) {
            verifyException(e, "Cannot deserialize");
            List<JsonMappingException.Reference> refs = e.getPath();
            assertEquals(2, refs.size());
            
            assertEquals(-1, refs.get(0).getIndex());
            assertEquals("keys", refs.get(0).getFieldName());

            
            assertEquals(1, refs.get(1).getIndex());
            assertNull(refs.get(1).getFieldName());
        }
    }

// com.fasterxml.jackson.databind.deser.jdk.CollectionDeserTest::testWrapExceptions
    public void testWrapExceptions() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(DeserializationFeature.WRAP_EXCEPTIONS);

        try {
            mapper.readValue("[{}]", new TypeReference<List<SomeObject>>() {});
        } catch (JsonMappingException exc) {
            assertEquals("I want to catch this exception", exc.getOriginalMessage());
        } catch (RuntimeException exc) {
            fail("The RuntimeException should have been wrapped with a JsonMappingException.");
        }

        ObjectMapper mapperNoWrap = new ObjectMapper();
        mapperNoWrap.disable(DeserializationFeature.WRAP_EXCEPTIONS);

        try {
            mapperNoWrap.readValue("[{}]", new TypeReference<List<SomeObject>>() {});
        } catch (JsonMappingException exc) {
            fail("It should not have wrapped the RuntimeException.");
        } catch (RuntimeException exc) {
            assertEquals("I want to catch this exception", exc.getMessage());
        }
    }

// com.fasterxml.jackson.databind.deser.jdk.DateDeserializationTZTest::testDateUtilISO8601_Timezone
    public void testDateUtilISO8601_Timezone() throws Exception
    {
        
        verify( MAPPER, "2000-01-02T03:04:05.678+01:00", judate(2000, 1, 2,   3, 4, 5, 678, "GMT+1"));
        
        verify( MAPPER, "2000-01-02T03:04:05.678+0100",  judate(2000, 1, 2,   3, 4, 5, 678, "GMT+1"));
        
        verify( MAPPER, "2000-01-02T03:04:05.678+01",    judate(2000, 1, 2,   3, 4, 5, 678, "GMT+1"));

        
        verify( MAPPER, "2000-01-02T03:04:05.678Z",      judate(2000, 1, 2,   3, 4, 5, 678, "UTC"));

        
        
        
        
        
        
        

        failure( MAPPER, "2000-01-02T03:04:05.678+"); 
        failure( MAPPER, "2000-01-02T03:04:05.678+1");

        failure( MAPPER, "2000-01-02T03:04:05.678+001"); 
        failure( MAPPER, "2000-01-02T03:04:05.678+00:");
        failure( MAPPER, "2000-01-02T03:04:05.678+00:001");
        failure( MAPPER, "2000-01-02T03:04:05.678+001:001");

        failure( MAPPER, "2000-01-02T03:04:05.678+1:");
        failure( MAPPER, "2000-01-02T03:04:05.678+00:1");
    }

// com.fasterxml.jackson.databind.deser.jdk.DateDeserializationTZTest::testDateUtilISO8601_DateTimeMillis
    public void testDateUtilISO8601_DateTimeMillis() throws Exception 
    {    
        
    		failure(MAPPER, "2000-01-02T03:04:05.0123456789+01:00");	
        verify( MAPPER, "2000-01-02T03:04:05.6789+01:00", judate(2000, 1, 2,   3, 4, 5, 678, "GMT+1"));
        verify( MAPPER, "2000-01-02T03:04:05.678+01:00",  judate(2000, 1, 2,   3, 4, 5, 678, "GMT+1"));
        verify( MAPPER, "2000-01-02T03:04:05.67+01:00",   judate(2000, 1, 2,   3, 4, 5, 670, "GMT+1"));
        verify( MAPPER, "2000-01-02T03:04:05.6+01:00",    judate(2000, 1, 2,   3, 4, 5, 600, "GMT+1"));
        verify( MAPPER, "2000-01-02T03:04:05+01:00",      judate(2000, 1, 2,   3, 4, 5, 000, "GMT+1"));

        
		failure(MAPPER, "2000-01-02T03:04:05.0123456789Z");	
        verify( MAPPER, "2000-01-02T03:04:05.6789Z",      judate(2000, 1, 2,   3, 4, 5, 678, "UTC"));
        verify( MAPPER, "2000-01-02T03:04:05.678Z",       judate(2000, 1, 2,   3, 4, 5, 678, "UTC"));
        verify( MAPPER, "2000-01-02T03:04:05.67Z",        judate(2000, 1, 2,   3, 4, 5, 670, "UTC"));
        verify( MAPPER, "2000-01-02T03:04:05.6Z",         judate(2000, 1, 2,   3, 4, 5, 600, "UTC"));
        verify( MAPPER, "2000-01-02T03:04:05Z",           judate(2000, 1, 2,   3, 4, 5,   0, "UTC"));
        

        
		failure(MAPPER, "2000-01-02T03:04:05.0123456789");	
        verify( MAPPER, "2000-01-02T03:04:05.6789",       judate(2000, 1, 2,   3, 4,  5, 678, LOCAL_TZ));
        verify( MAPPER, "2000-01-02T03:04:05.678",        judate(2000, 1, 2,   3, 4,  5, 678, LOCAL_TZ));
        verify( MAPPER, "2000-01-02T03:04:05.67",         judate(2000, 1, 2,   3, 4,  5, 670, LOCAL_TZ));
        verify( MAPPER, "2000-01-02T03:04:05.6",          judate(2000, 1, 2,   3, 4,  5, 600, LOCAL_TZ));
        verify( MAPPER, "2000-01-02T03:04:05",            judate(2000, 1, 2,   3, 4,  5, 000, LOCAL_TZ));
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        failure( MAPPER, "2000-01-02T03:04:05.+01:00");
        failure( MAPPER, "2000-01-02T03:04:05.");
        failure( MAPPER, "2000-01-02T03:04:05.Z");
    }

// com.fasterxml.jackson.databind.deser.jdk.DateDeserializationTZTest::testDateUtilISO8601_DateTime
    public void testDateUtilISO8601_DateTime() throws Exception 
    {
        
        verify(MAPPER, "2000-01-02T03:04:05+01:00",  judate(2000, 1, 2,   3, 4, 5, 0, "GMT+1"));

        
        verify(MAPPER, "2000-01-02T03:04:05",        judate(2000, 1, 2,   3, 4, 5, 0, LOCAL_TZ));

        
        failure(MAPPER, "2000-01-02T");
        failure(MAPPER, "2000-01-02T03");
        failure(MAPPER, "2000-01-02T03:");
        verify(MAPPER, "2000-01-02T03:04", judate(2000, 1, 2,  3, 4, 0, 0, LOCAL_TZ));
        failure(MAPPER, "2000-01-02T03:04:");
        
        
        failure(MAPPER, "2000-01-02T+01:00");
        failure(MAPPER, "2000-01-02T03+01:00");
        failure(MAPPER, "2000-01-02T03:+01:00");
        verify( MAPPER, "2000-01-02T03:04+01:00", judate(2000, 1, 2,   3, 4, 0, 0, "GMT+1"));
        failure(MAPPER, "2000-01-02T03:04:+01:00");
        
        failure(MAPPER, "2000-01-02TZ");
        failure(MAPPER, "2000-01-02T03Z");
        failure(MAPPER, "2000-01-02T03:Z");
        verify(MAPPER, "2000-01-02T03:04Z", judate(2000, 1, 2,  3, 4, 0, 0, "UTC"));
        failure(MAPPER, "2000-01-02T03:04:Z");

        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        failure( MAPPER, "2000-01-02T03:04:5");
        failure( MAPPER, "2000-01-02T03:04:5.000");
        failure(MAPPER, "2000-01-02T03:04:005");
        
        
        failure(MAPPER, "2000-01-02T03:04:5+01:00");
        failure(MAPPER, "2000-01-02T03:04:5.000+01:00");
        failure(MAPPER, "2000-01-02T03:04:005+01:00");
        
        
        failure(MAPPER, "2000-01-02T03:04:5Z");
        failure( MAPPER, "2000-01-02T03:04:5.000Z");
        failure(MAPPER, "2000-01-02T03:04:005Z");
        

        
        failure( MAPPER, "2000-01-02T03:4:05");
        failure( MAPPER, "2000-01-02T03:4:05.000");
        failure(MAPPER, "2000-01-02T03:004:05");
        
        
        failure(MAPPER, "2000-01-02T03:4:05+01:00");
        failure(MAPPER, "2000-01-02T03:4:05.000+01:00");
        failure(MAPPER, "2000-01-02T03:004:05+01:00");
        
        
        failure( MAPPER, "2000-01-02T03:4:05Z");
        failure( MAPPER, "2000-01-02T03:4:05.000Z");
        failure( MAPPER, "2000-01-02T03:004:05Z");

        
        failure( MAPPER, "2000-01-02T3:04:05");
        failure( MAPPER, "2000-01-02T3:04:05.000");
        failure(MAPPER, "2000-01-02T003:04:05");

        
        failure(MAPPER, "2000-01-02T3:04:05+01:00");
        failure(MAPPER, "2000-01-02T3:04:05.000+01:00");
        failure(MAPPER, "2000-01-02T003:04:05+01:00");

        
        failure( MAPPER, "2000-01-02T3:04:05Z");
        failure( MAPPER, "2000-01-02T3:04:05.000Z");
        failure( MAPPER, "2000-01-02T003:04:05Z");
    }

// com.fasterxml.jackson.databind.deser.jdk.DateDeserializationTZTest::testDateUtilISO8601_Date
    public void testDateUtilISO8601_Date() throws Exception
    {
        
        verify(MAPPER, "2000-01-02", judate(2000, 1, 2,   0, 0, 0, 0, LOCAL_TZ));
        
        
        
        
        
        
        
        
        
        
        
        

        
        failure( MAPPER, "2000-01-2"); 
        failure( MAPPER, "2000-01-002");
        
        
        failure( MAPPER, "2000-1-02");
        failure( MAPPER, "2000-001-02");
        
        
        failure( MAPPER, "20000-01-02");
        failure( MAPPER, "200-01-02"  );
        failure( MAPPER, "20-01-02"   );
        failure(  MAPPER, "2-01-02");
    }

// com.fasterxml.jackson.databind.deser.jdk.DateDeserializationTZTest::testDateUtil_Numeric
    public void testDateUtil_Numeric() throws Exception
    {
        {
            long now = 123456789L;
            verify( MAPPER,                now, new java.util.Date(now) ); 
            verify( MAPPER, Long.toString(now), new java.util.Date(now) ); 
        }
        {
            
            
            long now = 1321992375446L;
            verify( MAPPER,                now, new java.util.Date(now) );	
            verify( MAPPER, Long.toString(now), new java.util.Date(now) );  
        }
        {
            
            long now = - (24 * 3600 * 1000L);
            verify( MAPPER,                now, new java.util.Date(now) );	
            verify( MAPPER, Long.toString(now), new java.util.Date(now) );  
        }
    	
        
        BigInteger tooLarge = BigInteger.valueOf(Long.MAX_VALUE).add( BigInteger.valueOf(1) );
        failure(MAPPER, tooLarge, InvalidFormatException.class);
        failure(MAPPER, tooLarge.toString(), InvalidFormatException.class);
    	
        
        failure(MAPPER, 0.0, MismatchedInputException.class);
        failure(MAPPER, "0.0", InvalidFormatException.class);
    }

// com.fasterxml.jackson.databind.deser.jdk.DateDeserializationTZTest::testDateUtil_Annotation
    public void testDateUtil_Annotation() throws Exception
    {
        
        String json = aposToQuotes("{'date':'/2005/05/25/'}");
        java.util.Date expected = judate(2005, 05, 25, 0, 0, 0, 0, LOCAL_TZ);
        
        
        
        {
            DateAsStringBean result = MAPPER.readValue(json, DateAsStringBean.class);
            assertNotNull(result);
            assertEquals( expected, result.date );
        }
        {
            DateAsStringBean result = MAPPER.readerFor(DateAsStringBean.class)
                    .with(Locale.GERMANY)
                    .readValue(json);
            assertNotNull(result);
            assertEquals( expected, result.date );
        }
        
        
        {
            DateAsStringBeanGermany result = MAPPER.readerFor(DateAsStringBeanGermany.class)
                                                   .readValue(json);
            assertNotNull(result);
            assertEquals( expected, result.date );
        }
    }

// com.fasterxml.jackson.databind.deser.jdk.DateDeserializationTZTest::testDateUtil_Annotation_PatternAndLocale
    public void testDateUtil_Annotation_PatternAndLocale() throws Exception
    {
        
        
        ObjectMapper mapper = MAPPER.copy();
        mapper.setLocale( Locale.ITALY );

        
        
        String json = aposToQuotes("{ 'pattern': '*1 giu 2000 01:02:03*', 'pattern_FR': '*01 juin 2000 01:02:03*', 'pattern_GMT4': '*1 giu 2000 01:02:03*', 'pattern_FR_GMT4': '*1 juin 2000 01:02:03*'}");
        Annot_Pattern result = mapper.readValue(json, Annot_Pattern.class);

        assertNotNull(result);
        assertEquals( judate(2000, 6, 1, 1, 2, 3, 0, LOCAL_TZ), result.pattern        );
        assertEquals( judate(2000, 6, 1, 1, 2, 3, 0, LOCAL_TZ), result.pattern_FR     );
        assertEquals( judate(2000, 6, 1, 1, 2, 3, 0, "GMT+4"),  result.pattern_GMT4    );
        assertEquals( judate(2000, 6, 1, 1, 2, 3, 0, "GMT+4"),  result.pattern_FR_GMT4 );
    }

// com.fasterxml.jackson.databind.deser.jdk.DateDeserializationTZTest::testDateUtil_Annotation_TimeZone
    public void testDateUtil_Annotation_TimeZone() throws Exception
    {
        
        {
            String json = aposToQuotes("{ 'date': '2000-01-02T03:04:05.678' }");
            Annot_TimeZone result = MAPPER.readValue(json, Annot_TimeZone.class);
            
            assertNotNull(result);
            assertEquals( judate(2000, 1, 2, 3, 4, 5, 678, "GMT+4"), result.date);
        }
        
        
        
        
        {
            String json = aposToQuotes("{ 'date': '2000-01-02T03:04:05.678+01:00' }");
            Annot_TimeZone result = MAPPER.readValue(json, Annot_TimeZone.class);
            
            assertNotNull(result);
            assertEquals( judate(2000, 1, 2, 3, 4, 5, 678, "GMT+1"), result.date);
        }
    }

// com.fasterxml.jackson.databind.deser.jdk.DateDeserializationTZTest::testDateUtil_customDateFormat_withoutTZ
    public void testDateUtil_customDateFormat_withoutTZ() throws Exception
    {
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        {
            DateFormat df = new SimpleDateFormat("yyyy-MM-dd'X'HH:mm:ss");
            df.setTimeZone( TimeZone.getTimeZone("GMT+4") );    
            
            ObjectMapper mapper = new ObjectMapper();
            mapper.setTimeZone( TimeZone.getTimeZone(LOCAL_TZ) );
            mapper.setDateFormat(df);
            
            
            verify(mapper, "2000-01-02X04:00:00", judate(2000, 1, 2, 4, 00, 00, 00, LOCAL_TZ));
        }
        
        
        
        
        
        {
            DateFormat df = new SimpleDateFormat("yyyy-MM-dd'X'HH:mm:ss");
            df.setTimeZone( TimeZone.getTimeZone("GMT+4") );    
            
            ObjectMapper mapper = new ObjectMapper();
            mapper.setDateFormat(df);
            
            
            verify(mapper, "2000-01-02X04:00:00", judate(2000, 1, 2, 4, 00, 00, 00, "GMT+4"));
        }
    }

// com.fasterxml.jackson.databind.deser.jdk.DateDeserializationTZTest::testDateUtil_customDateFormat_withTZ
    public void testDateUtil_customDateFormat_withTZ() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd'X'HH:mm:ssZ");
        df.setTimeZone(TimeZone.getTimeZone("GMT+4"));    
        mapper.setDateFormat(df);

        verify(mapper, "2000-01-02X03:04:05+0300", judate(2000, 1, 2, 3, 4, 5, 00, "GMT+3"));
    }

// com.fasterxml.jackson.databind.deser.jdk.DateDeserializationTest::testDateUtil
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

// com.fasterxml.jackson.databind.deser.jdk.DateDeserializationTest::testDateUtilWithStringTimestamp
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

// com.fasterxml.jackson.databind.deser.jdk.DateDeserializationTest::testDateUtilRFC1123
    public void testDateUtilRFC1123() throws Exception
    {
        DateFormat fmt = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz", Locale.US);
        
        String inputStr = "Sat, 17 Jan 2009 06:13:58 +0000";
        java.util.Date inputDate = fmt.parse(inputStr);
        assertEquals(inputDate, MAPPER.readValue("\""+inputStr+"\"", java.util.Date.class));
    }

// com.fasterxml.jackson.databind.deser.jdk.DateDeserializationTest::testDateUtilRFC1123OnNonUSLocales
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

// com.fasterxml.jackson.databind.deser.jdk.DateDeserializationTest::testDateUtilISO8601
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

// com.fasterxml.jackson.databind.deser.jdk.DateDeserializationTest::testISO8601PartialMilliseconds
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

        
        inputStr = "1997-07-16T19:20:30.45+0100";
        inputDate = MAPPER.readValue(quote(inputStr), java.util.Date.class);
        c.setTime(inputDate);
        assertEquals(1997, c.get(Calendar.YEAR));
        assertEquals(Calendar.JULY, c.get(Calendar.MONTH));
        assertEquals(16, c.get(Calendar.DAY_OF_MONTH));
        assertEquals(19 - 1, c.get(Calendar.HOUR_OF_DAY));
        assertEquals(20, c.get(Calendar.MINUTE));
        assertEquals(30, c.get(Calendar.SECOND));
        assertEquals(450, c.get(Calendar.MILLISECOND));

        
        inputStr = "1997-07-16T19:20:30.45+01";
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

// com.fasterxml.jackson.databind.deser.jdk.DateDeserializationTest::testISO8601FractionalTimezoneOffset
    public void testISO8601FractionalTimezoneOffset() throws Exception
    {
        String inputStr = "1997-07-16T19:20:30.45+01:30";
        java.util.Date inputDate = MAPPER.readValue(quote(inputStr), java.util.Date.class);
        Calendar c = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
        c.setTime(inputDate);
        assertEquals(1997, c.get(Calendar.YEAR));
        assertEquals(Calendar.JULY, c.get(Calendar.MONTH));
        assertEquals(16, c.get(Calendar.DAY_OF_MONTH));
        assertEquals(19 - 2, c.get(Calendar.HOUR_OF_DAY));
        assertEquals(50, c.get(Calendar.MINUTE));
        assertEquals(30, c.get(Calendar.SECOND));
        assertEquals(450, c.get(Calendar.MILLISECOND));
    }

// com.fasterxml.jackson.databind.deser.jdk.DateDeserializationTest::testISO8601FractSecondsLong
    public void testISO8601FractSecondsLong() throws Exception
    {
        String inputStr;
        Date inputDate;
        Calendar c = Calendar.getInstance(TimeZone.getTimeZone("UTC"));

        inputStr = "2014-10-03T18:00:00.3456-05:00";
        inputDate = MAPPER.readValue(quote(inputStr), java.util.Date.class);
        c.setTime(inputDate);
        assertEquals(2014, c.get(Calendar.YEAR));
        assertEquals(Calendar.OCTOBER, c.get(Calendar.MONTH));
        assertEquals(3, c.get(Calendar.DAY_OF_MONTH));
        
        assertEquals(345, c.get(Calendar.MILLISECOND));

        
        try {
            MAPPER.readValue(quote("2014-10-03T18:00:00.1234567890-05:00"), java.util.Date.class);
        } catch (InvalidFormatException e) {
            verifyException(e, "invalid fractional seconds");
            verifyException(e, "can use at most 9 digits");
        }
    }

// com.fasterxml.jackson.databind.deser.jdk.DateDeserializationTest::testISO8601MissingSeconds
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

        
        inputStr = "1997-07-16T19:20+0200";
        inputDate = MAPPER.readValue(quote(inputStr), java.util.Date.class);
        c.setTime(inputDate);
        assertEquals(1997, c.get(Calendar.YEAR));
        assertEquals(Calendar.JULY, c.get(Calendar.MONTH));
        assertEquals(16, c.get(Calendar.DAY_OF_MONTH));
        assertEquals(19 - 2, c.get(Calendar.HOUR_OF_DAY));
        assertEquals(0, c.get(Calendar.SECOND));
        assertEquals(0, c.get(Calendar.MILLISECOND));

        
        inputStr = "1997-07-16T19:20+04";
        inputDate = MAPPER.readValue(quote(inputStr), java.util.Date.class);
        c.setTime(inputDate);
        assertEquals(1997, c.get(Calendar.YEAR));
        assertEquals(Calendar.JULY, c.get(Calendar.MONTH));
        assertEquals(16, c.get(Calendar.DAY_OF_MONTH));
        assertEquals(19 - 4, c.get(Calendar.HOUR_OF_DAY));
        assertEquals(0, c.get(Calendar.SECOND));
        assertEquals(0, c.get(Calendar.MILLISECOND));
    }

// com.fasterxml.jackson.databind.deser.jdk.DateDeserializationTest::testDateUtilISO8601NoTimezone
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

// com.fasterxml.jackson.databind.deser.jdk.DateDeserializationTest::testDateUtilISO8601NoTimezoneNonDefault
    public void testDateUtilISO8601NoTimezoneNonDefault() throws Exception
    {
        
        ObjectReader r = MAPPER.readerFor(Date.class);
        TimeZone tz = TimeZone.getTimeZone("GMT-2");
        Date date1 = r.with(tz)
                .readValue(quote("1970-01-01T00:00:00.000"));
        
        Date date2 = r.with(TimeZone.getTimeZone("GMT+5"))
                .readValue(quote("1970-01-01T00:00:00.000-02:00"));
        assertEquals(date1, date2);

        
        Calendar c = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
        c.setTime(date1);
        assertEquals(1970, c.get(Calendar.YEAR));
        assertEquals(Calendar.JANUARY, c.get(Calendar.MONTH));
        assertEquals(1, c.get(Calendar.DAY_OF_MONTH));
        assertEquals(2, c.get(Calendar.HOUR_OF_DAY));
    }

// com.fasterxml.jackson.databind.deser.jdk.DateDeserializationTest::testFormatAndCtors1722
    public void testFormatAndCtors1722() throws Exception
    {
        Date1722 input = new Date1722(new Date(0L), "bogus");
        String json = MAPPER.writeValueAsString(input);
        Date1722 result = MAPPER.readValue(json, Date1722.class);
        assertNotNull(result);
    }

// com.fasterxml.jackson.databind.deser.jdk.DateDeserializationTest::testDateUtilISO8601NoMilliseconds
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

// com.fasterxml.jackson.databind.deser.jdk.DateDeserializationTest::testDateUtilISO8601JustDate
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

// com.fasterxml.jackson.databind.deser.jdk.DateDeserializationTest::testDateSql
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

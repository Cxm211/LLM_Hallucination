// buggy code
    public Collection<NamedType> collectAndResolveSubtypesByClass(MapperConfig<?> config, 
            AnnotatedMember property, JavaType baseType)
    {
        final AnnotationIntrospector ai = config.getAnnotationIntrospector();
        // for backwards compatibility, must allow null here:
        Class<?> rawBase = (baseType == null) ? property.getRawType() : baseType.getRawClass();
        
        HashMap<NamedType, NamedType> collected = new HashMap<NamedType, NamedType>();
        // start with registered subtypes (which have precedence)
        if (_registeredSubtypes != null) {
            for (NamedType subtype : _registeredSubtypes) {
                // is it a subtype of root type?
                if (rawBase.isAssignableFrom(subtype.getType())) { // yes
                    AnnotatedClass curr = AnnotatedClassResolver.resolveWithoutSuperTypes(config,
                            subtype.getType());
                    _collectAndResolve(curr, subtype, config, ai, collected);
                }
            }
        }
        
        // then annotated types for property itself
            Collection<NamedType> st = ai.findSubtypes(property);
            if (st != null) {
                for (NamedType nt : st) {
                    AnnotatedClass ac = AnnotatedClassResolver.resolveWithoutSuperTypes(config,
                            nt.getType());
                    _collectAndResolve(ac, nt, config, ai, collected);
                }            
        }

        NamedType rootType = new NamedType(rawBase, null);
        AnnotatedClass ac = AnnotatedClassResolver.resolveWithoutSuperTypes(config, rawBase);
            
        // and finally subtypes via annotations from base type (recursively)
        _collectAndResolve(ac, rootType, config, ai, collected);

        return new ArrayList<NamedType>(collected.values());
    }

    public Collection<NamedType> collectAndResolveSubtypesByTypeId(MapperConfig<?> config, 
            AnnotatedMember property, JavaType baseType)
    {
        final AnnotationIntrospector ai = config.getAnnotationIntrospector();
        Class<?> rawBase = baseType.getRawClass();

        // Need to keep track of classes that have been handled already 
        Set<Class<?>> typesHandled = new HashSet<Class<?>>();
        Map<String,NamedType> byName = new LinkedHashMap<String,NamedType>();

        // start with lowest-precedence, which is from type hierarchy
        NamedType rootType = new NamedType(rawBase, null);
        AnnotatedClass ac = AnnotatedClassResolver.resolveWithoutSuperTypes(config,
                rawBase);
        _collectAndResolveByTypeId(ac, rootType, config, typesHandled, byName);
        
        // then with definitions from property
            Collection<NamedType> st = ai.findSubtypes(property);
            if (st != null) {
                for (NamedType nt : st) {
                    ac = AnnotatedClassResolver.resolveWithoutSuperTypes(config, nt.getType());
                    _collectAndResolveByTypeId(ac, nt, config, typesHandled, byName);
                }            
        }
        // and finally explicit type registrations (highest precedence)
        if (_registeredSubtypes != null) {
            for (NamedType subtype : _registeredSubtypes) {
                // is it a subtype of root type?
                if (rawBase.isAssignableFrom(subtype.getType())) { // yes
                    AnnotatedClass curr = AnnotatedClassResolver.resolveWithoutSuperTypes(config,
                            subtype.getType());
                    _collectAndResolveByTypeId(curr, subtype, config, typesHandled, byName);
                }
            }
        }
        return _combineNamedAndUnnamed(rawBase, typesHandled, byName);
    }

// relevant test
// com.fasterxml.jackson.databind.deser.AnySetter349Test::testUnwrappedWithAny
    public void testUnwrappedWithAny() throws Exception
    {
        final ObjectMapper mapper = objectMapper();
        Bean349 value = mapper.readValue(UNWRAPPED_JSON_349,  Bean349.class);
        assertNotNull(value);
        assertEquals(3, value.x);
        assertEquals(4, value.y);
        assertEquals(2, value.props.size());
    }

// com.fasterxml.jackson.databind.deser.AnySetter349Test::testUnwrappedWithAnyAsUpdate
    public void testUnwrappedWithAnyAsUpdate() throws Exception
    {
        final ObjectMapper mapper = objectMapper();
        Bean349 bean = mapper.readerFor(Bean349.class)
                .withValueToUpdate(new Bean349())
                .readValue(UNWRAPPED_JSON_349);
        assertEquals(3, bean.x);
        assertEquals(4, bean.y);
        assertEquals(2, bean.props.size());
    }

// com.fasterxml.jackson.databind.deser.AnySetterTest::testSimpleMapImitation
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

// com.fasterxml.jackson.databind.deser.AnySetterTest::testAnySetterDisable
    public void testAnySetterDisable() throws Exception
    {
        try {
            MAPPER.readValue(aposToQuotes("{'value':3}"),
                    MapImitatorDisabled.class);
            fail("Should not pass");
        } catch (JsonMappingException e) {
            verifyException(e, "Unrecognized field \"value\"");
        }

    }

// com.fasterxml.jackson.databind.deser.AnySetterTest::testSimpleTyped
    public void testSimpleTyped() throws Exception
    {
        MapImitatorWithValue mapHolder = MAPPER.readValue
            ("{ \"a\" : [ 3, -1 ], \"b\" : [ ] }", MapImitatorWithValue.class);
        Map<String,int[]> result = mapHolder._map;
        assertEquals(2, result.size());
        assertEquals(new int[] { 3, -1 }, result.get("a"));
        assertEquals(new int[0], result.get("b"));
    }

// com.fasterxml.jackson.databind.deser.AnySetterTest::testBrokenWithDoubleAnnotations
    public void testBrokenWithDoubleAnnotations() throws Exception
    {
        try {
            @SuppressWarnings("unused")
            Broken b = MAPPER.readValue("{ \"a\" : 3 }", Broken.class);
            fail("Should have gotten an exception");
        } catch (JsonMappingException e) {
            verifyException(e, "Multiple 'any-setter' methods");
        }
    }

// com.fasterxml.jackson.databind.deser.AnySetterTest::testIgnored
    public void testIgnored() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        _testIgnorals(mapper);
    }

// com.fasterxml.jackson.databind.deser.AnySetterTest::testIgnoredPart2
    public void testIgnoredPart2() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        _testIgnorals(mapper);
    }

// com.fasterxml.jackson.databind.deser.AnySetterTest::testProblem744
    public void testProblem744() throws Exception
    {
        Bean744 bean = MAPPER.readValue("{\"name\":\"Bob\"}", Bean744.class);
        assertNotNull(bean.additionalProperties);
        assertEquals(1, bean.additionalProperties.size());
        assertEquals("Bob", bean.additionalProperties.get("name"));
    }

// com.fasterxml.jackson.databind.deser.AnySetterTest::testIssue797
    public void testIssue797() throws Exception
    {
        String json = MAPPER.writeValueAsString(new Bean797BaseImpl());
        assertEquals("{}", json);
    }

// com.fasterxml.jackson.databind.deser.AnySetterTest::testPolymorphic
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

// com.fasterxml.jackson.databind.deser.AnySetterTest::testJsonAnySetterOnMap
	public void testJsonAnySetterOnMap() throws Exception {
		JsonAnySetterOnMap result = MAPPER.readValue("{\"id\":2,\"name\":\"Joe\", \"city\":\"New Jersey\"}",
		        JsonAnySetterOnMap.class);
		assertEquals(2, result.id);
		assertEquals("Joe", result.other.get("name"));
		assertEquals("New Jersey", result.other.get("city"));
	}

// com.fasterxml.jackson.databind.deser.AnySetterTest::testJsonAnySetterOnNullMap
	public void testJsonAnySetterOnNullMap() throws Exception {
		JsonAnySetterOnNullMap result = MAPPER.readValue("{\"id\":2,\"name\":\"Joe\", \"city\":\"New Jersey\"}",
		        JsonAnySetterOnNullMap.class);
		assertEquals(2, result.id);
		assertNull(result.other);
    }

// com.fasterxml.jackson.databind.deser.AnySetterTest::testGenericAnySetter
    public void testGenericAnySetter() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();

        Map<String, Integer> stringGenericMap = new HashMap<String, Integer>();
        stringGenericMap.put("testStringKey", 5);
        Map<Integer, Integer> integerGenericMap = new HashMap<Integer, Integer>();
        integerGenericMap.put(111, 6);

        MyWrapper deserialized = mapper.readValue(aposToQuotes(
                "{'myStringGeneric':{'staticallyMappedProperty':'Test','testStringKey':5},'myIntegerGeneric':{'staticallyMappedProperty':'Test2','111':6}}"
                ), MyWrapper.class);
        MyGeneric<String> stringGeneric = deserialized.getMyStringGeneric();
        MyGeneric<Integer> integerGeneric = deserialized.getMyIntegerGeneric();

        assertNotNull(stringGeneric);
        assertEquals(stringGeneric.getStaticallyMappedProperty(), "Test");
        for(Map.Entry<String, Integer> entry : stringGeneric.getDynamicallyMappedProperties().entrySet()) {
            assertTrue("A key in MyGeneric<String> is not an String.", entry.getKey() instanceof String);
            assertTrue("A value in MyGeneric<Integer> is not an Integer.", entry.getValue() instanceof Integer);
        }
        assertEquals(stringGeneric.getDynamicallyMappedProperties(), stringGenericMap);

        assertNotNull(integerGeneric);
        assertEquals(integerGeneric.getStaticallyMappedProperty(), "Test2");
        for(Map.Entry<Integer, Integer> entry : integerGeneric.getDynamicallyMappedProperties().entrySet()) {
            Object key = entry.getKey();
            assertEquals("A key in MyGeneric<Integer> is not an Integer.", Integer.class, key.getClass());
            Object value = entry.getValue();
            assertEquals("A value in MyGeneric<Integer> is not an Integer.", Integer.class, value.getClass());
        }
        assertEquals(integerGeneric.getDynamicallyMappedProperties(), integerGenericMap);
    }

// com.fasterxml.jackson.databind.deser.IgnoreWithDeserTest::testSimpleIgnore
    public void testSimpleIgnore() throws Exception
    {
        SizeClassIgnore result = MAPPER.readValue("{ \"x\":1, \"y\" : 2 }",
             SizeClassIgnore.class);
        
        assertEquals(1, result._x);
        assertEquals(0, result._y);
    }

// com.fasterxml.jackson.databind.deser.IgnoreWithDeserTest::testFailOnIgnore
    public void testFailOnIgnore() throws Exception
    {
        ObjectReader r = MAPPER.readerFor(NoYOrZ.class);
        
        
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

// com.fasterxml.jackson.databind.deser.NullHandlingTest::testNull
    public void testNull() throws Exception
    {
        
        Object result = MAPPER.readValue("   null", Object.class);
        assertNull(result);
    }

// com.fasterxml.jackson.databind.deser.NullHandlingTest::testAnySetterNulls
    public void testAnySetterNulls() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        SimpleModule module = new SimpleModule("test", Version.unknownVersion());
        module.addDeserializer(String.class, new FunnyNullDeserializer());
        mapper.registerModule(module);

        String fieldName = "fieldName";
        String nullValue = "{\""+fieldName+"\":null}";

        
        AnySetter result = mapper.readValue(nullValue, AnySetter.class);

        assertEquals(1, result.getAny().size());
        assertNotNull(result.getAny().get(fieldName));
        assertEquals("funny", result.getAny().get(fieldName));

        
        ObjectReader reader = mapper.readerFor(AnySetter.class);
        result = reader.readValue(nullValue);

        assertEquals(1, result.getAny().size());
        assertNotNull(result.getAny().get(fieldName));
        assertEquals("funny", result.getAny().get(fieldName));
    }

// com.fasterxml.jackson.databind.deser.NullHandlingTest::testCustomRootNulls
    public void testCustomRootNulls() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        SimpleModule module = new SimpleModule("test", Version.unknownVersion());
        module.addDeserializer(String.class, new FunnyNullDeserializer());
        mapper.registerModule(module);

        
        String str = mapper.readValue("null", String.class);
        assertNotNull(str);
        assertEquals("funny", str);
        
        
        ObjectReader reader = mapper.readerFor(String.class);
        str = reader.readValue("null");
        assertNotNull(str);
        assertEquals("funny", str);
    }

// com.fasterxml.jackson.databind.deser.NullHandlingTest::testListOfNulls
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

        
        ObjectReader reader = mapper.readerFor(type);
        deser = reader.readValue("[null]");
        assertNotNull(deser);
        assertEquals(1, deser.size());
        assertEquals(list.get(0), deser.get(0));
    }

// com.fasterxml.jackson.databind.deser.NullHandlingTest::testMapOfNulls
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

        
        ObjectReader reader = mapper.readerFor(type);
        deser = reader.readValue("{\"key\":null}");
        assertNotNull(deser);
        assertEquals(1, deser.size());
        assertEquals("funny", deser.get("key"));
    }

// com.fasterxml.jackson.databind.deser.NullHandlingTest::testPolymorphicDataNull
    public void testPolymorphicDataNull() throws Exception
    {
        String typeA =
                "{\"name\":\"TypeAData\", \"type\":\"TypeA\", \"proxy\":{\"aValue\":\"This works!\"}}";
        RootData typeAData = MAPPER.readValue(typeA, RootData.class);
        assertEquals("No value for aValue!?", "This works!", ((TypeA) typeAData.proxy).aValue);
        String typeB =
                "{\"name\":\"TypeBData\", \"type\":\"TypeB\", \"proxy\":{\"bValue\":\"This works too!\"}}";
        RootData typeBData = MAPPER.readValue(typeB, RootData.class);
        assertEquals("No value for bValue!?", "This works too!", ((TypeB) typeBData.proxy).bValue);
        String typeBNull =
                "{\"name\":\"TypeBData\", \"type\":\"TypeB\", \"proxy\": null}";
        RootData typeBNullData = MAPPER.readValue(typeBNull, RootData.class);
        assertNull("Proxy should be null!", typeBNullData.proxy);
    }

// com.fasterxml.jackson.databind.deser.PropertyAliasTest::testSimpleAliases
    public void testSimpleAliases() throws Exception
    {
        AliasBean bean;

        
        bean = MAPPER.readValue(aposToQuotes("{'Name':'Foobar','a':3,'xyz':37}"),
                AliasBean.class);
        assertEquals("Foobar", bean.name);
        assertEquals(3, bean._a);
        assertEquals(37, bean._xyz);

        
        bean = MAPPER.readValue(aposToQuotes("{'name':'Foobar','a':3,'Xyz':37}"),
                AliasBean.class);
        assertEquals("Foobar", bean.name);
        assertEquals(3, bean._a);
        assertEquals(37, bean._xyz);
        
        
        bean = MAPPER.readValue(aposToQuotes("{'name':'Foobar','A':3,'xyz':37}"),
                AliasBean.class);
        assertEquals("Foobar", bean.name);
        assertEquals(3, bean._a);
        assertEquals(37, bean._xyz);
    }

// com.fasterxml.jackson.databind.deser.ReadOrWriteOnlyTest::testReadOnlyAndWriteOnly
    public void testReadOnlyAndWriteOnly() throws Exception
    {
        String json = MAPPER.writeValueAsString(new ReadXWriteY());
        assertEquals("{\"x\":1}", json);

        ReadXWriteY result = MAPPER.readValue("{\"x\":5, \"y\":6}", ReadXWriteY.class);
        assertNotNull(result);
        assertEquals(1, result.x);
        assertEquals(6, result.y);
    }

// com.fasterxml.jackson.databind.deser.ReadOrWriteOnlyTest::testReadOnly935
    public void testReadOnly935() throws Exception
    {
        String json = MAPPER.writeValueAsString(new Pojo935());
        Pojo935 result = MAPPER.readValue(json, Pojo935.class);
        assertNotNull(result);
    }

// com.fasterxml.jackson.databind.deser.ReadOrWriteOnlyTest::testReadOnly1345
    public void testReadOnly1345() throws Exception
    {
        Foo1345 result = MAPPER.readValue("{\"name\":\"test\"}", Foo1345.class);
        assertNotNull(result);
        assertEquals("test", result.name);
        assertNull(result.id);
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

// com.fasterxml.jackson.databind.deser.TestArrayDeserialization::testByteArrayTypeOverride890
    public void testByteArrayTypeOverride890() throws Exception
    {
        HiddenBinaryBean890 result = MAPPER.readValue(
                aposToQuotes("{'someBytes':'AQIDBA=='}"), HiddenBinaryBean890.class);
        assertNotNull(result);
        assertNotNull(result.someBytes);
        assertEquals(byte[].class, result.someBytes.getClass());
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

// com.fasterxml.jackson.databind.deser.TestBasicAnnotations::testAnnotationsDisabled
    public void testAnnotationsDisabled() throws Exception
    {
        
        assertTrue(MAPPER.getDeserializationConfig().isEnabled(MapperFeature.USE_ANNOTATIONS));
        
        AnnoBean bean = MAPPER.readValue("{ \"y\" : 0 }", AnnoBean.class);
        assertEquals(0, bean.value);

        ObjectMapper m = new ObjectMapper();
        m.configure(MapperFeature.USE_ANNOTATIONS, false);
        
        bean = m.readValue("{ \"x\" : 0 }", AnnoBean.class);
        assertEquals(0, bean.value);
    }

// com.fasterxml.jackson.databind.deser.TestBasicAnnotations::testEnumsWhenDisabled
    public void testEnumsWhenDisabled() throws Exception
    {
        ObjectMapper m = new ObjectMapper();
        assertEquals(Alpha.B, m.readValue(quote("B"), Alpha.class));

        m = new ObjectMapper();
        m.configure(MapperFeature.USE_ANNOTATIONS, false);
        
        assertEquals(Alpha.B, m.readValue(quote("B"), Alpha.class));
    }

// com.fasterxml.jackson.databind.deser.TestBasicAnnotations::testNoAccessOverrides
    public void testNoAccessOverrides() throws Exception
    {
        ObjectMapper m = new ObjectMapper();
        m.disable(MapperFeature.CAN_OVERRIDE_ACCESS_MODIFIERS);
        SimpleBean bean = m.readValue("{\"x\":1,\"y\":2}", SimpleBean.class);
        assertEquals(1, bean.x);
        assertEquals(2, bean.y);
    }

// com.fasterxml.jackson.databind.deser.TestBeanDeserializer::testAbstractFailure
    public void testAbstractFailure() throws Exception
    {
        try {
            MAPPER.readValue("{ \"x\" : 3 }", Abstract.class);
            fail("Should fail on trying to deserialize abstract type");
        } catch (JsonProcessingException e) {
            verifyException(e, "can not construct");
        }
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
        
        assertFalse(MAPPER.isEnabled(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT));
        try {
            MAPPER.readValue(quote(""), Bean.class);
            fail("Should not accept Empty String for POJO");
        } catch (JsonProcessingException e) {
            verifyException(e, "from String value");
            assertValidLocation(e.getLocation());
        }
        
        ObjectReader r = MAPPER.readerFor(Bean.class)
                .with(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT);
        Bean result = r.readValue(quote(""));
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

// com.fasterxml.jackson.databind.deser.TestBeanDeserializer::testModifyStdScalarDeserializer
    public void testModifyStdScalarDeserializer() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new SimpleModule("test")
            .setDeserializerModifier(new BeanDeserializerModifier() {
                        @Override
                        public JsonDeserializer<?> modifyDeserializer(DeserializationConfig config,
                                BeanDescription beanDesc, JsonDeserializer<?> deser) {
                            if (beanDesc.getBeanClass() == String.class) {
                                return new UCStringDeserializer(deser);
                            }
                            return deser;
                        }
            }));
        Object result = mapper.readValue(quote("abcDEF"), String.class);
        assertEquals("ABCDEF", result);
    }

// com.fasterxml.jackson.databind.deser.TestCachingOfDeser::testCustomMapCaching1
    public void testCustomMapCaching1() throws Exception
    {

        ObjectMapper mapper = new ObjectMapper();
        TestMapWithCustom mapC = mapper.readValue(MAP_INPUT, TestMapWithCustom.class);
        TestMapNoCustom mapStd = mapper.readValue(MAP_INPUT, TestMapNoCustom.class);

        assertNotNull(mapC.map);
        assertNotNull(mapStd.map);
        assertEquals(Integer.valueOf(100), mapC.map.get("a"));
        assertEquals(Integer.valueOf(1), mapStd.map.get("a"));
    }

// com.fasterxml.jackson.databind.deser.TestCachingOfDeser::testCustomMapCaching2
    public void testCustomMapCaching2() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        TestMapNoCustom mapStd = mapper.readValue(MAP_INPUT, TestMapNoCustom.class);
        TestMapWithCustom mapC = mapper.readValue(MAP_INPUT, TestMapWithCustom.class);

        assertNotNull(mapStd.map);
        assertNotNull(mapC.map);
        assertEquals(Integer.valueOf(1), mapStd.map.get("a"));
        assertEquals(Integer.valueOf(100), mapC.map.get("a"));
    }

// com.fasterxml.jackson.databind.deser.TestCachingOfDeser::testCustomListCaching1
    public void testCustomListCaching1() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        TestListWithCustom listC = mapper.readValue(LIST_INPUT, TestListWithCustom.class);
        TestListNoCustom listStd = mapper.readValue(LIST_INPUT, TestListNoCustom.class);

        assertNotNull(listC.list);
        assertNotNull(listStd.list);
        assertEquals(Integer.valueOf(100), listC.list.get(0));
        assertEquals(Integer.valueOf(1), listStd.list.get(0));
    }

// com.fasterxml.jackson.databind.deser.TestCachingOfDeser::testCustomListCaching2
    public void testCustomListCaching2() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        TestListNoCustom listStd = mapper.readValue(LIST_INPUT, TestListNoCustom.class);
        TestListWithCustom listC = mapper.readValue(LIST_INPUT, TestListWithCustom.class);

        assertNotNull(listC.list);
        assertNotNull(listStd.list);
        assertEquals(Integer.valueOf(100), listC.list.get(0));
        assertEquals(Integer.valueOf(1), listStd.list.get(0));
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
        List<?> result = r.forType(List.class).readValue(quote(""));
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

// com.fasterxml.jackson.databind.deser.TestCollectionDeserialization::testWrapExceptions
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

// com.fasterxml.jackson.databind.deser.TestCollectionDeserialization::testSingletonCollections
    public void testSingletonCollections() throws Exception
    {
        final TypeReference<?> xbeanListType = new TypeReference<List<XBean>>() { };

        String json = MAPPER.writeValueAsString(Collections.singleton(new XBean(3)));
        Collection<XBean> result = MAPPER.readValue(json, xbeanListType);
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(3, result.iterator().next().x);

        json = MAPPER.writeValueAsString(Collections.singletonList(new XBean(28)));
        result = MAPPER.readValue(json, xbeanListType);
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(28, result.iterator().next().x);
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

// com.fasterxml.jackson.databind.deser.TestCustomDeserializers::testJsonNodeDelegating
    public void testJsonNodeDelegating() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        SimpleModule module = new SimpleModule("test", Version.unknownVersion());
        module.addDeserializer(Immutable.class,
            new StdNodeBasedDeserializer<Immutable>(Immutable.class) {
                @Override
                public Immutable convert(JsonNode root, DeserializationContext ctxt) throws IOException {
                    int x = root.path("x").asInt();
                    int y = root.path("y").asInt();
                    return new Immutable(x, y);
                }
        });
        mapper.registerModule(module);
        Immutable imm = mapper.readValue("{\"x\":-10,\"y\":3}", Immutable.class);
        assertEquals(-10, imm.x);
        assertEquals(3, imm.y);
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

// com.fasterxml.jackson.databind.deser.TestCustomDeserializers::testCurrentValueAccess
    public void testCurrentValueAccess() throws Exception
    {
        Issue631Bean bean = MAPPER.readValue(aposToQuotes("{'prop':'stuff'}"),
                Issue631Bean.class);
        assertNotNull(bean);
        assertEquals("prop/Issue631Bean", bean.prop);
    }

// com.fasterxml.jackson.databind.deser.TestCustomDeserializers::testCustomStringDeser
    public void testCustomStringDeser() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper().registerModule(
                new SimpleModule().addDeserializer(String.class, new UCStringDeserializer())
                );
        assertEquals("FOO", mapper.readValue(quote("foo"), String.class));
        StringWrapper sw = mapper.readValue("{\"str\":\"foo\"}", StringWrapper.class);
        assertNotNull(sw);
        assertEquals("FOO", sw.str);
    }

// com.fasterxml.jackson.databind.deser.TestCustomDeserializers::testDelegatingDeserializer
    public void testDelegatingDeserializer() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper().registerModule(
                new DelegatingModuleImpl());
        String str = mapper.readValue(quote("foo"), String.class);
        assertEquals("MY:foo", str);
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
        assertEquals(BooleanElement.class, ob.getClass());
        assertFalse(((BooleanElement) ob).b);
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

// com.fasterxml.jackson.databind.deser.filter.IgnoreCreatorProp1317Test::testThatJsonIgnoreWorksWithConstructorProperties
    public void testThatJsonIgnoreWorksWithConstructorProperties() throws Exception {
        ObjectMapper om = objectMapper();
        Testing testing = new Testing("shouldBeIgnored", "notIgnore");
        String json = om.writeValueAsString(testing);

        assertFalse(json.contains("shouldBeIgnored"));
    }

// com.fasterxml.jackson.databind.deser.filter.IgnorePropertyOnDeser1217Test::testIgnoreOnProperty
    public void testIgnoreOnProperty() throws Exception
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

// com.fasterxml.jackson.databind.deser.filter.IgnorePropertyOnDeser1217Test::testIgnoreViaConfigOverride
    public void testIgnoreViaConfigOverride() throws Exception
    {
        ObjectMapper mapper = newObjectMapper();
        mapper.configOverride(Point.class)
            .setIgnorals(JsonIgnoreProperties.Value.forIgnoredProperties("y"));
        Point p = mapper.readValue(aposToQuotes("{'x':1,'y':2}"), Point.class);
        
        assertEquals(1, p.x);
        assertEquals(0, p.y);
    }

// com.fasterxml.jackson.databind.deser.filter.NullConversionsForContentTest::testFailOnNullFromDefaults
    public void testFailOnNullFromDefaults() throws Exception
    {
        final String JSON = aposToQuotes("{'values':[null]}");
        TypeReference<?> listType = new TypeReference<NullContentUndefined<List<String>>>() { };

        
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
        TypeReference<?> typeRef = new TypeReference<NullContentFail<List<Integer>>>() { };

        
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
        TypeReference<?> listType = new TypeReference<NullContentUndefined<List<Integer>>>() { };

        
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
        TypeReference<?> listType = new TypeReference<NullContentUndefined<List<Long>>>() { };

        
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
        TypeReference<?> listType = new TypeReference<NullContentSkip<List<Long>>>() { };

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
            verifyException(e, "Can not create empty instance");
        }
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

// com.fasterxml.jackson.databind.deser.filter.RecursiveIgnorePropertiesTest::testRecursiveForDeser
    public void testRecursiveForDeser() throws Exception
    {
        String st = aposToQuotes("{ 'name': 'admin',\n"

              + "    'person_z': { 'name': 'admin' }"
                + "}");

        ObjectMapper mapper = newObjectMapper();
        Person result = mapper.readValue(st, Person.class);
        assertEquals("admin", result.name);
    }

// com.fasterxml.jackson.databind.deser.filter.RecursiveIgnorePropertiesTest::testRecursiveForSer
    public void testRecursiveForSer() throws Exception
    {
        ObjectMapper mapper = newObjectMapper();
        Person input = new Person();
        input.name = "Bob";
        Person p2 = new Person();
        p2.name = "Bill";
        input.personZ = p2;
        p2.personZ = input;

        String json = mapper.writeValueAsString(input);
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

// com.fasterxml.jackson.databind.deser.inject.TestInjectables::testIssueGH471
    public void testIssueGH471() throws Exception
    {
        final Object constructorInjected = "constructorInjected";
        final Object methodInjected = "methodInjected";
        final Object fieldInjected = "fieldInjected";

        ObjectMapper mapper = newObjectMapper()
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

// com.fasterxml.jackson.databind.deser.jdk.DateAdjustment204Test::testContextTimezone
    public void testContextTimezone() throws Exception
    {
        String inputStr = "1997-07-16T19:20:30.45+0100";

        
        assertTrue(MAPPER.isEnabled(DeserializationFeature.ADJUST_DATES_TO_CONTEXT_TIME_ZONE));
        final ObjectReader r =  MAPPER
                .readerFor(Calendar.class)
                .with(TimeZone.getTimeZone("PST"));

        
        Calendar cal = r.readValue(quote(inputStr));
        TimeZone tz = cal.getTimeZone();
        assertEquals("PST", tz.getID());

        assertEquals(1997, cal.get(Calendar.YEAR));
        assertEquals(Calendar.JULY, cal.get(Calendar.MONTH));
        assertEquals(16, cal.get(Calendar.DAY_OF_MONTH));

        
        assertEquals(11, cal.get(Calendar.HOUR_OF_DAY));
        assertEquals(20, cal.get(Calendar.MINUTE));
        assertEquals(30, cal.get(Calendar.SECOND));

        
        cal = r.without(DeserializationFeature.ADJUST_DATES_TO_CONTEXT_TIME_ZONE)
                .readValue(quote(inputStr));

        
        

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

// com.fasterxml.jackson.databind.deser.jdk.DateDeserializationTest::testCalendar
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

// com.fasterxml.jackson.databind.deser.jdk.DateDeserializationTest::testCustom
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

// com.fasterxml.jackson.databind.deser.jdk.DateDeserializationTest::testDatesWithEmptyStrings
    public void testDatesWithEmptyStrings() throws Exception
    {
        assertNull(MAPPER.readValue(quote(""), java.util.Date.class));
        assertNull(MAPPER.readValue(quote(""), java.util.Calendar.class));
        assertNull(MAPPER.readValue(quote(""), java.sql.Date.class));
    }

// com.fasterxml.jackson.databind.deser.jdk.DateDeserializationTest::test8601DateTimeNoMilliSecs
    public void test8601DateTimeNoMilliSecs() throws Exception
    {
        
        for (String inputStr : new String[] {
               "2010-06-28T23:34:22Z",
               "2010-06-28T23:34:22+0000",
               "2010-06-28T23:34:22+00:00",
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

// com.fasterxml.jackson.databind.deser.jdk.DateDeserializationTest::testTimeZone
    public void testTimeZone() throws Exception
    {
        TimeZone result = MAPPER.readValue(quote("PST"), TimeZone.class);
        assertEquals("PST", result.getID());
    }

// com.fasterxml.jackson.databind.deser.jdk.DateDeserializationTest::testCustomDateWithAnnotation
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

        
        
        result = MAPPER.readerFor(DateAsStringBean.class)
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

        
        DateAsStringBeanGermany result2 = MAPPER.readerFor(DateAsStringBeanGermany.class).readValue(INPUT);
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

// com.fasterxml.jackson.databind.deser.jdk.DateDeserializationTest::testCustomCalendarWithAnnotation
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

// com.fasterxml.jackson.databind.deser.jdk.DateDeserializationTest::testCustomCalendarWithTimeZone
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

// com.fasterxml.jackson.databind.deser.jdk.DateDeserializationTest::testISO8601Directly
    public void testISO8601Directly() throws Exception
    {
        final String TIME_STR = "2015-01-21T08:56:13.533+0000";
        Date d = MAPPER.readValue(quote(TIME_STR), Date.class);
        assertNotNull(d);

        ISO8601DateFormat f = new ISO8601DateFormat();
        Date d2 = f.parse(TIME_STR);
        assertNotNull(d2);
        assertEquals(d.getTime(), d2.getTime());
    }

// com.fasterxml.jackson.databind.deser.jdk.DateDeserializationTest::testCalendarArrayUnwrap
    public void testCalendarArrayUnwrap() throws Exception
    {
        ObjectReader reader = new ObjectMapper()
                .readerFor(CalendarBean.class)
                .without(DeserializationFeature.UNWRAP_SINGLE_VALUE_ARRAYS);
        final String inputDate = "1972-12-28T00:00:00.000+0000";
        final String input = aposToQuotes("{'v':['"+inputDate+"']}");
        try {
            reader.readValue(input);
            fail("Did not throw exception when reading a value from a single value array with the UNWRAP_SINGLE_VALUE_ARRAYS feature disabled");
        } catch (MismatchedInputException exp) {
            verifyException(exp, "Can not deserialize");
            verifyException(exp, "out of START_ARRAY");
        }

        reader = reader.with(DeserializationFeature.UNWRAP_SINGLE_VALUE_ARRAYS);
        CalendarBean bean = reader.readValue(input);
        assertNotNull(bean._v);
        assertEquals(1972, bean._v.get(Calendar.YEAR));

        
        try {
            reader.readValue(aposToQuotes("{'v':['"+inputDate+"','"+inputDate+"']}"));
            fail("Did not throw exception while reading a value from a multi value array with UNWRAP_SINGLE_VALUE_ARRAY feature enabled");
        } catch (JsonMappingException exp) {
            verifyException(exp, "Attempted to unwrap");
        }
    }

// com.fasterxml.jackson.databind.deser.jdk.DateDeserializationTest::testLenientCalendar
    public void testLenientCalendar() throws Exception
    {
        final String JSON = aposToQuotes("{'value':'2015-11-32'}");

        
        LenientCalendarBean lenBean = MAPPER.readValue(JSON, LenientCalendarBean.class);
        assertEquals(Calendar.DECEMBER, lenBean.value.get(Calendar.MONTH));
        assertEquals(2, lenBean.value.get(Calendar.DAY_OF_MONTH));

        
        try {
            MAPPER.readValue(JSON, StrictCalendarBean.class);
            fail("Should not pass with invalid (with strict) date value");
        } catch (MismatchedInputException e) {
            verifyException(e, "Can not deserialize value of type java.util.Calendar");
            verifyException(e, "from String \"2015-11-32\"");
            verifyException(e, "expected format");
        }

        
        ObjectMapper mapper = new ObjectMapper();
        mapper.configOverride(java.util.Date.class)
            .setFormat(JsonFormat.Value.forLeniency(Boolean.FALSE));
        try {
            mapper.readValue(quote("2015-11-32"), java.util.Date.class);
            fail("Should not pass with invalid (with strict) date value");
        } catch (MismatchedInputException e) {
            verifyException(e, "Can not deserialize value of type java.util.Date");
            verifyException(e, "from String \"2015-11-32\"");
            verifyException(e, "expected format");
        }
    }

// com.fasterxml.jackson.databind.deser.jdk.DateDeserializationTest::testInvalidFormat
    public void testInvalidFormat() throws Exception
    {
        try {
            MAPPER.readValue(quote("foobar"), Date.class);
            fail("Should have failed with an exception");
        } catch (InvalidFormatException e) {
            verifyException(e, "Can not deserialize value of type java.util.Date from String");
            assertEquals("foobar", e.getValue());
            assertEquals(Date.class, e.getTargetType());
        } catch (Exception e) {
            fail("Wrong type of exception ("+e.getClass().getName()+"), should get "
                    +InvalidFormatException.class.getName());
        }
    }

// com.fasterxml.jackson.databind.deser.jdk.EnumAltIdTest::testFailWhenCaseSensitiveAndNameIsNotUpperCase
    public void testFailWhenCaseSensitiveAndNameIsNotUpperCase() throws IOException {
        try {
            READER_DEFAULT.forType(TestEnum.class).readValue("\"Jackson\"");
            fail("InvalidFormatException expected");
        } catch (InvalidFormatException e) {
            verifyException(e, "value not one of declared Enum instance names: [JACKSON, OK, RULES]");
        }
    }

// com.fasterxml.jackson.databind.deser.jdk.EnumAltIdTest::testFailWhenCaseSensitiveAndToStringIsUpperCase
    public void testFailWhenCaseSensitiveAndToStringIsUpperCase() throws IOException {
        ObjectReader r = READER_DEFAULT.forType(LowerCaseEnum.class)
                .with(DeserializationFeature.READ_ENUMS_USING_TO_STRING);
        try {
            r.readValue("\"A\"");
            fail("InvalidFormatException expected");
        } catch (InvalidFormatException e) {
            verifyException(e, "value not one of declared Enum instance names: [a, b, c]");
        }
    }

// com.fasterxml.jackson.databind.deser.jdk.EnumAltIdTest::testEnumDesIgnoringCaseWithLowerCaseContent
    public void testEnumDesIgnoringCaseWithLowerCaseContent() throws IOException {
        assertEquals(TestEnum.JACKSON,
                READER_IGNORE_CASE.forType(TestEnum.class).readValue(quote("jackson")));
    }

// com.fasterxml.jackson.databind.deser.jdk.EnumAltIdTest::testEnumDesIgnoringCaseWithUpperCaseToString
    public void testEnumDesIgnoringCaseWithUpperCaseToString() throws IOException {
        ObjectReader r = MAPPER_IGNORE_CASE.readerFor(LowerCaseEnum.class)
                .with(DeserializationFeature.READ_ENUMS_USING_TO_STRING);
        assertEquals(LowerCaseEnum.A, r.readValue("\"A\""));
    }

// com.fasterxml.jackson.databind.deser.jdk.EnumAltIdTest::testIgnoreCaseInEnumList
    public void testIgnoreCaseInEnumList() throws Exception {
        TestEnum[] enums = READER_IGNORE_CASE.forType(TestEnum[].class)
            .readValue("[\"jacksON\", \"ruLes\"]");

        assertEquals(2, enums.length);
        assertEquals(TestEnum.JACKSON, enums[0]);
        assertEquals(TestEnum.RULES, enums[1]);
    }

// com.fasterxml.jackson.databind.deser.jdk.EnumAltIdTest::testIgnoreCaseInEnumSet
    public void testIgnoreCaseInEnumSet() throws IOException {
        ObjectReader r = READER_IGNORE_CASE.forType(new TypeReference<EnumSet<TestEnum>>() { });
        EnumSet<TestEnum> set = r.readValue("[\"jackson\"]");
        assertEquals(1, set.size());
        assertTrue(set.contains(TestEnum.JACKSON));
    }

// com.fasterxml.jackson.databind.deser.jdk.EnumAltIdTest::testIgnoreCaseViaFormat
    public void testIgnoreCaseViaFormat() throws Exception
    {
        final String JSON = aposToQuotes("{'value':'ok'}");

        
        EnumBean pojo = READER_DEFAULT.forType(EnumBean.class)
            .readValue(JSON);
        assertEquals(TestEnum.OK, pojo.value);

        
        try {
            READER_DEFAULT.forType(StrictCaseBean.class)
                    .readValue(JSON);
            fail("Should not pass");
        } catch (InvalidFormatException e) {
            verifyException(e, "value not one of declared Enum instance names: [JACKSON, OK, RULES]");
        }
    }

// com.fasterxml.jackson.databind.deser.jdk.EnumDefaultReadTest::testWithoutCustomFeatures
    public void testWithoutCustomFeatures() throws Exception
    {
        final ObjectReader r = MAPPER.reader();

        _verifyOkDeserialization(r, "ZERO", SimpleEnum.class, SimpleEnum.ZERO);
        _verifyOkDeserialization(r, "ONE", SimpleEnum.class, SimpleEnum.ONE);
        _verifyOkDeserialization(r, "0", SimpleEnum.class, SimpleEnum.ZERO);
        _verifyOkDeserialization(r, "1", SimpleEnum.class, SimpleEnum.ONE);
        _verifyFailingDeserialization(r, "TWO", SimpleEnum.class);
        _verifyFailingDeserialization(r, "2", SimpleEnum.class);

        _verifyOkDeserialization(r, "ZERO", SimpleEnumWithDefault.class, SimpleEnumWithDefault.ZERO);
        _verifyOkDeserialization(r, "ONE", SimpleEnumWithDefault.class, SimpleEnumWithDefault.ONE);
        _verifyFailingDeserialization(r, "TWO", SimpleEnumWithDefault.class);
        _verifyOkDeserialization(r, "0", SimpleEnumWithDefault.class, SimpleEnumWithDefault.ZERO);
        _verifyOkDeserialization(r, "1", SimpleEnumWithDefault.class, SimpleEnumWithDefault.ONE);
        _verifyFailingDeserialization(r, "2", SimpleEnumWithDefault.class);

        _verifyFailingDeserialization(r, "ZERO", CustomEnum.class);
        _verifyFailingDeserialization(r, "ONE", CustomEnum.class);
        _verifyFailingDeserialization(r, "TWO", CustomEnum.class);
        _verifyOkDeserialization(r, "0", CustomEnum.class, CustomEnum.ZERO);
        _verifyOkDeserialization(r, "1", CustomEnum.class, CustomEnum.ONE);
        _verifyFailingDeserialization(r, "2", CustomEnum.class);

        _verifyFailingDeserialization(r, "ZERO", CustomEnumWithDefault.class);
        _verifyFailingDeserialization(r, "ONE", CustomEnumWithDefault.class);
        _verifyFailingDeserialization(r, "TWO", CustomEnumWithDefault.class);
        _verifyOkDeserialization(r, "0", CustomEnumWithDefault.class, CustomEnumWithDefault.ZERO);
        _verifyOkDeserialization(r, "1", CustomEnumWithDefault.class, CustomEnumWithDefault.ONE);
        _verifyFailingDeserialization(r, "2", CustomEnumWithDefault.class);
    }

// com.fasterxml.jackson.databind.deser.jdk.EnumDefaultReadTest::testWithFailOnNumbers
    public void testWithFailOnNumbers() throws Exception
    {
        ObjectReader r = MAPPER.reader()
                .with(DeserializationFeature.FAIL_ON_NUMBERS_FOR_ENUMS);

        _verifyOkDeserialization(r, "ZERO", SimpleEnum.class, SimpleEnum.ZERO);
        _verifyOkDeserialization(r, "ONE", SimpleEnum.class, SimpleEnum.ONE);
        _verifyFailingDeserialization(r, "TWO", SimpleEnum.class);
        _verifyFailingDeserialization(r, "0", SimpleEnum.class);
        _verifyFailingDeserialization(r, "1", SimpleEnum.class);
        _verifyFailingDeserialization(r, "2", SimpleEnum.class);

        _verifyOkDeserialization(r, "ZERO", SimpleEnumWithDefault.class, SimpleEnumWithDefault.ZERO);
        _verifyOkDeserialization(r, "ONE", SimpleEnumWithDefault.class, SimpleEnumWithDefault.ONE);
        _verifyFailingDeserialization(r, "TWO", SimpleEnumWithDefault.class);
        _verifyFailingDeserialization(r, "0", SimpleEnumWithDefault.class);
        _verifyFailingDeserialization(r, "1", SimpleEnumWithDefault.class);
        _verifyFailingDeserialization(r, "2", SimpleEnumWithDefault.class);

        _verifyFailingDeserialization(r, "ZERO", CustomEnum.class);
        _verifyFailingDeserialization(r, "ONE", CustomEnum.class);
        _verifyFailingDeserialization(r, "TWO", CustomEnum.class);
        _verifyOkDeserialization(r, "0", CustomEnum.class, CustomEnum.ZERO);
        _verifyOkDeserialization(r, "1", CustomEnum.class, CustomEnum.ONE);
        _verifyFailingDeserialization(r, "2", CustomEnum.class);

        _verifyFailingDeserialization(r, "ZERO", CustomEnumWithDefault.class);
        _verifyFailingDeserialization(r, "ONE", CustomEnumWithDefault.class);
        _verifyFailingDeserialization(r, "TWO", CustomEnumWithDefault.class);
        _verifyOkDeserialization(r, "0", CustomEnumWithDefault.class, CustomEnumWithDefault.ZERO);
        _verifyOkDeserialization(r, "1", CustomEnumWithDefault.class, CustomEnumWithDefault.ONE);
        _verifyFailingDeserialization(r, "2", CustomEnumWithDefault.class);
    }

// com.fasterxml.jackson.databind.deser.jdk.EnumDefaultReadTest::testWithReadUnknownAsDefault
    public void testWithReadUnknownAsDefault() throws Exception
    {
        ObjectReader r = MAPPER.reader()
                .with(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_USING_DEFAULT_VALUE);

        _verifyOkDeserialization(r, "ZERO", SimpleEnum.class, SimpleEnum.ZERO);
        _verifyOkDeserialization(r, "ONE", SimpleEnum.class, SimpleEnum.ONE);
        _verifyFailingDeserialization(r, "TWO", SimpleEnum.class);
        _verifyOkDeserialization(r, "0", SimpleEnum.class, SimpleEnum.ZERO);
        _verifyOkDeserialization(r, "1", SimpleEnum.class, SimpleEnum.ONE);
        _verifyFailingDeserialization(r, "2", SimpleEnum.class);

        _verifyOkDeserialization(r, "ZERO", SimpleEnumWithDefault.class, SimpleEnumWithDefault.ZERO);
        _verifyOkDeserialization(r, "ONE", SimpleEnumWithDefault.class, SimpleEnumWithDefault.ONE);
        _verifyOkDeserialization(r, "TWO", SimpleEnumWithDefault.class, SimpleEnumWithDefault.ZERO);
        _verifyOkDeserialization(r, "0", SimpleEnumWithDefault.class, SimpleEnumWithDefault.ZERO);
        _verifyOkDeserialization(r, "1", SimpleEnumWithDefault.class, SimpleEnumWithDefault.ONE);
        _verifyOkDeserialization(r, "2", SimpleEnumWithDefault.class, SimpleEnumWithDefault.ZERO);

        _verifyFailingDeserialization(r, "ZERO", CustomEnum.class);
        _verifyFailingDeserialization(r, "ONE", CustomEnum.class);
        _verifyFailingDeserialization(r, "TWO", CustomEnum.class);
        _verifyOkDeserialization(r, "0", CustomEnum.class, CustomEnum.ZERO);
        _verifyOkDeserialization(r, "1", CustomEnum.class, CustomEnum.ONE);
        _verifyFailingDeserialization(r, "2", CustomEnum.class);

        _verifyOkDeserialization(r, "ZERO", CustomEnumWithDefault.class, CustomEnumWithDefault.ZERO);
        _verifyOkDeserialization(r, "ONE", CustomEnumWithDefault.class, CustomEnumWithDefault.ZERO);
        _verifyOkDeserialization(r, "TWO", CustomEnumWithDefault.class, CustomEnumWithDefault.ZERO);
        _verifyOkDeserialization(r, "0", CustomEnumWithDefault.class, CustomEnumWithDefault.ZERO);
        _verifyOkDeserialization(r, "1", CustomEnumWithDefault.class, CustomEnumWithDefault.ONE);
        _verifyOkDeserialization(r, "2", CustomEnumWithDefault.class, CustomEnumWithDefault.ZERO);
    }

// com.fasterxml.jackson.databind.deser.jdk.EnumDefaultReadTest::testWithFailOnNumbersAndReadUnknownAsDefault
    public void testWithFailOnNumbersAndReadUnknownAsDefault()
        throws Exception
    {
        ObjectReader r = MAPPER.reader()
                          .with(DeserializationFeature.FAIL_ON_NUMBERS_FOR_ENUMS)
                          .with(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_USING_DEFAULT_VALUE);

        _verifyOkDeserialization(r, "ZERO", SimpleEnum.class, SimpleEnum.ZERO);
        _verifyOkDeserialization(r, "ONE", SimpleEnum.class, SimpleEnum.ONE);

        _verifyFailingDeserialization(r, "TWO", SimpleEnum.class);
        _verifyFailingDeserialization(r, "0", SimpleEnum.class);
        _verifyFailingDeserialization(r, "1", SimpleEnum.class);
        _verifyFailingDeserialization(r, "2", SimpleEnum.class);

        _verifyOkDeserialization(r, "ZERO", SimpleEnumWithDefault.class, SimpleEnumWithDefault.ZERO);
        _verifyOkDeserialization(r, "ONE", SimpleEnumWithDefault.class, SimpleEnumWithDefault.ONE);
        _verifyOkDeserialization(r, "TWO", SimpleEnumWithDefault.class, SimpleEnumWithDefault.ZERO);

        _verifyFailingDeserialization(r, "ZERO", CustomEnum.class);
        _verifyFailingDeserialization(r, "ONE", CustomEnum.class);
        _verifyFailingDeserialization(r, "TWO", CustomEnum.class);

        _verifyOkDeserialization(r, "0", CustomEnum.class, CustomEnum.ZERO);
        _verifyOkDeserialization(r, "1", CustomEnum.class, CustomEnum.ONE);

        _verifyFailingDeserialization(r, "2", CustomEnum.class);

        _verifyOkDeserialization(r, "ZERO", CustomEnumWithDefault.class, CustomEnumWithDefault.ZERO);
        _verifyOkDeserialization(r, "ONE", CustomEnumWithDefault.class, CustomEnumWithDefault.ZERO);
        _verifyOkDeserialization(r, "TWO", CustomEnumWithDefault.class, CustomEnumWithDefault.ZERO);
        _verifyOkDeserialization(r, "0", CustomEnumWithDefault.class, CustomEnumWithDefault.ZERO);
        _verifyOkDeserialization(r, "1", CustomEnumWithDefault.class, CustomEnumWithDefault.ONE);

        
        
        
        _verifyOkDeserialization(r, "0", SimpleEnumWithDefault.class, SimpleEnumWithDefault.ZERO);
        _verifyOkDeserialization(r, "1", SimpleEnumWithDefault.class, SimpleEnumWithDefault.ZERO);
        _verifyOkDeserialization(r, "2", SimpleEnumWithDefault.class, SimpleEnumWithDefault.ZERO);
        
        
        
        _verifyOkDeserialization(r, "2", CustomEnumWithDefault.class, CustomEnumWithDefault.ZERO);
    }

// com.fasterxml.jackson.databind.deser.jdk.EnumDeserializationTest::testSimple
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

// com.fasterxml.jackson.databind.deser.jdk.EnumDeserializationTest::testComplexEnum
    public void testComplexEnum() throws Exception
    {
        String json = MAPPER.writeValueAsString(TimeUnit.SECONDS);
        assertEquals(quote("SECONDS"), json);
        TimeUnit result = MAPPER.readValue(json, TimeUnit.class);
        assertSame(TimeUnit.SECONDS, result);
    }

// com.fasterxml.jackson.databind.deser.jdk.EnumDeserializationTest::testAnnotated
    public void testAnnotated() throws Exception
    {
        AnnotatedTestEnum e = MAPPER.readValue("\"JACKSON\"", AnnotatedTestEnum.class);
        
        assertEquals(AnnotatedTestEnum.OK, e);
    }

// com.fasterxml.jackson.databind.deser.jdk.EnumDeserializationTest::testSubclassedEnums
    public void testSubclassedEnums() throws Exception
    {
        EnumWithSubClass value = MAPPER.readValue("\"A\"", EnumWithSubClass.class);
        assertEquals(EnumWithSubClass.A, value);
    }

// com.fasterxml.jackson.databind.deser.jdk.EnumDeserializationTest::testToStringEnums
    public void testToStringEnums() throws Exception
    {
        
        ObjectMapper m = new ObjectMapper();
        m.configure(DeserializationFeature.READ_ENUMS_USING_TO_STRING, true);
        LowerCaseEnum value = m.readValue("\"c\"", LowerCaseEnum.class);
        assertEquals(LowerCaseEnum.C, value);
    }

// com.fasterxml.jackson.databind.deser.jdk.EnumDeserializationTest::testNumbersToEnums
    public void testNumbersToEnums() throws Exception
    {
        
        assertFalse(MAPPER.getDeserializationConfig().isEnabled(DeserializationFeature.FAIL_ON_NUMBERS_FOR_ENUMS));
        TestEnum value = MAPPER.readValue("1", TestEnum.class);
        assertSame(TestEnum.RULES, value);

        
        ObjectReader r = MAPPER.readerFor(TestEnum.class)
                .with(DeserializationFeature.FAIL_ON_NUMBERS_FOR_ENUMS);
        try {
            value = r.readValue("1");
            fail("Expected an error");
        } catch (JsonMappingException e) {
            verifyException(e, "Can not deserialize");
            verifyException(e, "not allowed to deserialize Enum value out of number: disable");
        }

        
        try {
            value = r.readValue(quote("1"));
            fail("Expected an error");
        } catch (JsonMappingException e) {
            verifyException(e, "Can not deserialize");
            
            verifyException(e, "value not one of declared Enum");
        }
    }

// com.fasterxml.jackson.databind.deser.jdk.EnumDeserializationTest::testEnumsWithIndex
    public void testEnumsWithIndex() throws Exception
    {
        ObjectMapper m = new ObjectMapper();
        m.enable(SerializationFeature.WRITE_ENUMS_USING_INDEX);
        String json = m.writeValueAsString(TestEnum.RULES);
        assertEquals(String.valueOf(TestEnum.RULES.ordinal()), json);
        TestEnum result = m.readValue(json, TestEnum.class);
        assertSame(TestEnum.RULES, result);
    }

// com.fasterxml.jackson.databind.deser.jdk.EnumDeserializationTest::testEnumsWithJsonValue
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

// com.fasterxml.jackson.databind.deser.jdk.EnumDeserializationTest::testAllowUnknownEnumValuesReadAsNull
    public void testAllowUnknownEnumValuesReadAsNull() throws Exception
    {
        
        ObjectReader reader = MAPPER.reader(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_AS_NULL);
        assertNull(reader.forType(TestEnum.class).readValue("\"NO-SUCH-VALUE\""));
        assertNull(reader.forType(TestEnum.class).readValue(" 4343 "));
    }

// com.fasterxml.jackson.databind.deser.jdk.EnumDeserializationTest::testAllowUnknownEnumValuesForEnumSets
    public void testAllowUnknownEnumValuesForEnumSets() throws Exception
    {
        ObjectReader reader = MAPPER.reader(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_AS_NULL);
        EnumSet<TestEnum> result = reader.forType(new TypeReference<EnumSet<TestEnum>>() { })
                .readValue("[\"NO-SUCH-VALUE\"]");
        assertEquals(0, result.size());
    }

// com.fasterxml.jackson.databind.deser.jdk.EnumDeserializationTest::testAllowUnknownEnumValuesAsMapKeysReadAsNull
    public void testAllowUnknownEnumValuesAsMapKeysReadAsNull() throws Exception
    {
        ObjectReader reader = MAPPER.reader(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_AS_NULL);
        ClassWithEnumMapKey result = reader.forType(ClassWithEnumMapKey.class)
                .readValue("{\"map\":{\"NO-SUCH-VALUE\":\"val\"}}");
        assertTrue(result.map.containsKey(null));
    }

// com.fasterxml.jackson.databind.deser.jdk.EnumDeserializationTest::testDoNotAllowUnknownEnumValuesAsMapKeysWhenReadAsNullDisabled
    public void testDoNotAllowUnknownEnumValuesAsMapKeysWhenReadAsNullDisabled() throws Exception
    {
        assertFalse(MAPPER.isEnabled(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_AS_NULL));
         try {
             MAPPER.readValue("{\"map\":{\"NO-SUCH-VALUE\":\"val\"}}", ClassWithEnumMapKey.class);
             fail("Expected an exception for bogus enum value...");
         } catch (JsonMappingException jex) {
             verifyException(jex, "Can not deserialize Map key of type com.fasterxml.jackson.databind.deser");
         }
    }

// com.fasterxml.jackson.databind.deser.jdk.EnumDeserializationTest::testEnumsWithEmpty
    public void testEnumsWithEmpty() throws Exception
    {
       final ObjectMapper mapper = new ObjectMapper();
       mapper.configure(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, true);
       TestEnum result = mapper.readValue("\"\"", TestEnum.class);
       assertNull(result);
    }

// com.fasterxml.jackson.databind.deser.jdk.EnumDeserializationTest::testGenericEnumDeserialization
    public void testGenericEnumDeserialization() throws Exception
    {
       final ObjectMapper mapper = new ObjectMapper();
       SimpleModule module = new SimpleModule("foobar");
       module.addDeserializer(Enum.class, new LcEnumDeserializer());
       mapper.registerModule(module);
       
       assertEquals(TestEnum.JACKSON, mapper.readValue(quote("jackson"), TestEnum.class));
    }

// com.fasterxml.jackson.databind.deser.jdk.EnumDeserializationTest::testUnwrappedEnum
    public void testUnwrappedEnum() throws Exception {
        final ObjectMapper mapper = new ObjectMapper();
        mapper.enable(DeserializationFeature.UNWRAP_SINGLE_VALUE_ARRAYS);
        
        assertEquals(TestEnum.JACKSON, mapper.readValue("[" + quote("JACKSON") + "]", TestEnum.class));
    }

// com.fasterxml.jackson.databind.deser.jdk.EnumDeserializationTest::testUnwrappedEnumException
    public void testUnwrappedEnumException() throws Exception {
        final ObjectMapper mapper = new ObjectMapper();
        mapper.disable(DeserializationFeature.UNWRAP_SINGLE_VALUE_ARRAYS);
        try {
            Object v = mapper.readValue("[" + quote("JACKSON") + "]",
                    TestEnum.class);
            fail("Exception was not thrown on deserializing a single array element of type enum; instead got: "+v);
        } catch (JsonMappingException exp) {
            
            verifyException(exp, "Can not deserialize");
        }
    }

// com.fasterxml.jackson.databind.deser.jdk.EnumDeserializationTest::testIndexAsString
    public void testIndexAsString() throws Exception
    {
        
        TestEnum en = MAPPER.readValue("2", TestEnum.class);
        assertSame(TestEnum.values()[2], en);

        
        en = MAPPER.readValue(quote("1"), TestEnum.class);
        assertSame(TestEnum.values()[1], en);
    }

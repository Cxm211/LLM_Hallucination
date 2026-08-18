// buggy code
    protected Object _deserializeUsingPropertyBased(final JsonParser p, final DeserializationContext ctxt)
        throws IOException
    {
        final PropertyBasedCreator creator = _propertyBasedCreator;
        PropertyValueBuffer buffer = creator.startBuilding(p, ctxt, _objectIdReader);

        TokenBuffer unknown = null;

        JsonToken t = p.getCurrentToken();
        for (; t == JsonToken.FIELD_NAME; t = p.nextToken()) {
            String propName = p.getCurrentName();
            p.nextToken(); // to point to value
            // creator property?
            SettableBeanProperty creatorProp = creator.findCreatorProperty(propName);
            if (creatorProp != null) {
                // Last creator property to set?
                if (buffer.assignParameter(creatorProp,
                        _deserializeWithErrorWrapping(p, ctxt, creatorProp))) {
                    p.nextToken(); // to move to following FIELD_NAME/END_OBJECT
                    Object bean;
                    try {
                        bean = creator.build(ctxt, buffer);
                    } catch (Exception e) {
                        bean = wrapInstantiationProblem(e, ctxt);
                    }
                    if (bean == null) {
                        return ctxt.handleInstantiationProblem(handledType(), null,
                                _creatorReturnedNullException());
                    }
                    // [databind#631]: Assign current value, to be accessible by custom serializers
                    p.setCurrentValue(bean);

                    //  polymorphic?
                    if (bean.getClass() != _beanType.getRawClass()) {
                        return handlePolymorphic(p, ctxt, bean, unknown);
                    }
                    if (unknown != null) { // nope, just extra unknown stuff...
                        bean = handleUnknownProperties(ctxt, bean, unknown);
                    }
                    // or just clean?
                    return deserialize(p, ctxt, bean);
                }
                continue;
            }
            // Object Id property?
            if (buffer.readIdProperty(propName)) {
                continue;
            }
            // regular property? needs buffering
            SettableBeanProperty prop = _beanProperties.find(propName);
            if (prop != null) {
                    buffer.bufferProperty(prop, _deserializeWithErrorWrapping(p, ctxt, prop));
                    // 14-Jun-2016, tatu: As per [databind#1261], looks like we need additional
                    //    handling of forward references here. Not exactly sure why existing
                    //    facilities did not cover, but this does appear to solve the problem
                continue;
            }
            // Things marked as ignorable should not be passed to any setter
            if (_ignorableProps != null && _ignorableProps.contains(propName)) {
                handleIgnoredProperty(p, ctxt, handledType(), propName);
                continue;
            }
            // "any property"?
            if (_anySetter != null) {
                try {
                    buffer.bufferAnyProperty(_anySetter, propName, _anySetter.deserialize(p, ctxt));
                } catch (Exception e) {
                    wrapAndThrow(e, _beanType.getRawClass(), propName, ctxt);
                }
                continue;
            }
            // Ok then, let's collect the whole field; name and value
            if (unknown == null) {
                unknown = new TokenBuffer(p, ctxt);
            }
            unknown.writeFieldName(propName);
            unknown.copyCurrentStructure(p);
        }

        // We hit END_OBJECT, so:
        Object bean;
        try {
            bean =  creator.build(ctxt, buffer);
        } catch (Exception e) {
            wrapInstantiationProblem(e, ctxt);
            bean = null; // never gets here
        }
        if (unknown != null) {
            // polymorphic?
            if (bean.getClass() != _beanType.getRawClass()) {
                return handlePolymorphic(null, ctxt, bean, unknown);
            }
            // no, just some extra unknown properties
            return handleUnknownProperties(ctxt, bean, unknown);
        }
        return bean;
    }

    protected Exception _creatorReturnedNullException() {
        if (_nullFromCreator == null) {
            _nullFromCreator = new NullPointerException("JSON Creator returned null");
        }
        return _nullFromCreator;
    }

// relevant test
// com.fasterxml.jackson.databind.ser.RawValueTest::testNullStringGetter
    public void testNullStringGetter() throws Exception
    {
        String result = MAPPER.writeValueAsString(new ClassGetter<String>(null));
        String expected = "{\"nonRaw\":null,\"raw\":null,\"value\":null}";
        assertEquals(expected, result);
    }

// com.fasterxml.jackson.databind.ser.RawValueTest::testWithValueToTree
    public void testWithValueToTree() throws Exception
    {
        JsonNode w = MAPPER.valueToTree(new RawWrapped("{ }"));
        assertNotNull(w);
        assertEquals("{\"json\":{ }}", MAPPER.writeValueAsString(w));
    }

// com.fasterxml.jackson.databind.ser.RawValueTest::testRawFromMapToTree
    public void testRawFromMapToTree() throws Exception
    {
        RawValue myType = new RawValue("Jackson");

        Map<String, Object> object = new HashMap<String, Object>();
        object.put("key", myType);
        JsonNode jsonNode = MAPPER.valueToTree(object);
        String json = MAPPER.writeValueAsString(jsonNode);
        assertEquals("{\"key\":Jackson}", json);
    }

// com.fasterxml.jackson.databind.ser.SerializationFeaturesTest::testGlobalAutoDetection
    public void testGlobalAutoDetection() throws IOException
    {
        
        ObjectMapper m = new ObjectMapper();
        Map<String,Object> result = writeAndMap(m, new GetterClass());
        assertEquals(2, result.size());
        assertEquals(Integer.valueOf(-2), result.get("x"));
        assertEquals(Integer.valueOf(1), result.get("y"));

        
        
        m = new ObjectMapper();
        m.configure(MapperFeature.AUTO_DETECT_GETTERS, false);
        result = writeAndMap(m, new GetterClass());
        assertEquals(1, result.size());
        assertTrue(result.containsKey("x"));
    }

// com.fasterxml.jackson.databind.ser.SerializationFeaturesTest::testPerClassAutoDetection
    public void testPerClassAutoDetection() throws IOException
    {
        
        ObjectMapper m = new ObjectMapper();
        Map<String,Object> result = writeAndMap(m, new DisabledGetterClass());
        assertEquals(1, result.size());
        assertTrue(result.containsKey("x"));

        
        m.configure(MapperFeature.AUTO_DETECT_GETTERS, false);
        result = writeAndMap(m, new EnabledGetterClass());
        assertEquals(2, result.size());
        assertTrue(result.containsKey("x"));
        assertTrue(result.containsKey("y"));
    }

// com.fasterxml.jackson.databind.ser.SerializationFeaturesTest::testPerClassAutoDetectionForIsGetter
    public void testPerClassAutoDetectionForIsGetter() throws IOException
    {
        ObjectMapper m = new ObjectMapper();
        
        m.configure(MapperFeature.AUTO_DETECT_GETTERS, true);
        m.configure(MapperFeature.AUTO_DETECT_IS_GETTERS, false);
         Map<String,Object> result = writeAndMap(m, new EnabledIsGetterClass());
        assertEquals(1, result.size());
        assertTrue(result.containsKey("ok"));
        assertEquals(Boolean.TRUE, result.get("ok"));
    }

// com.fasterxml.jackson.databind.ser.SerializationFeaturesTest::testConfigChainability
    public void testConfigChainability()
    {
        ObjectMapper m = new ObjectMapper();
        assertTrue(m.isEnabled(MapperFeature.AUTO_DETECT_SETTERS));
        assertTrue(m.isEnabled(MapperFeature.AUTO_DETECT_GETTERS));
        m.configure(MapperFeature.AUTO_DETECT_SETTERS, false)
            .configure(MapperFeature.AUTO_DETECT_GETTERS, false);
        assertFalse(m.isEnabled(MapperFeature.AUTO_DETECT_SETTERS));
        assertFalse(m.isEnabled(MapperFeature.AUTO_DETECT_GETTERS));
    }

// com.fasterxml.jackson.databind.ser.SerializationFeaturesTest::testCloseCloseable
    public void testCloseCloseable() throws IOException
    {
        ObjectMapper m = new ObjectMapper();
        
        CloseableBean bean = new CloseableBean();
        m.writeValueAsString(bean);
        assertFalse(bean.wasClosed);

        
        m.configure(SerializationFeature.CLOSE_CLOSEABLE, true);
        bean = new CloseableBean();
        m.writeValueAsString(bean);
        assertTrue(bean.wasClosed);

        
        bean = new CloseableBean();
        m.writerFor(CloseableBean.class).writeValueAsString(bean);
        assertTrue(bean.wasClosed);
    }

// com.fasterxml.jackson.databind.ser.SerializationFeaturesTest::testCharArrays
    public void testCharArrays() throws IOException
    {
        char[] chars = new char[] { 'a','b','c' };
        ObjectMapper m = new ObjectMapper();
        
        assertEquals(quote("abc"), m.writeValueAsString(chars));
        
        
        m.configure(SerializationFeature.WRITE_CHAR_ARRAYS_AS_JSON_ARRAYS, true);
        assertEquals("[\"a\",\"b\",\"c\"]", m.writeValueAsString(chars));
    }

// com.fasterxml.jackson.databind.ser.SerializationFeaturesTest::testFlushingAutomatic
    public void testFlushingAutomatic() throws IOException
    {
        ObjectMapper mapper = new ObjectMapper();
        assertTrue(mapper.getSerializationConfig().isEnabled(SerializationFeature.FLUSH_AFTER_WRITE_VALUE));
        
        StringWriter sw = new StringWriter();
        JsonGenerator g = mapper.getFactory().createGenerator(sw);
        mapper.writeValue(g, Integer.valueOf(13));
        assertEquals("13", sw.toString());
        g.close();

        
        sw = new StringWriter();
        g = mapper.getFactory().createGenerator(sw);
        ObjectWriter ow = mapper.writer();
        ow.writeValue(g, Integer.valueOf(99));
        assertEquals("99", sw.toString());
        g.close();
    }

// com.fasterxml.jackson.databind.ser.SerializationFeaturesTest::testFlushingNotAutomatic
    public void testFlushingNotAutomatic() throws IOException
    {
        
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(SerializationFeature.FLUSH_AFTER_WRITE_VALUE, false);
        StringWriter sw = new StringWriter();
        JsonGenerator g = mapper.getFactory().createGenerator(sw);

        mapper.writeValue(g, Integer.valueOf(13));
        
        assertEquals("", sw.toString());
        
        g.flush();
        assertEquals("13", sw.toString());
        g.close();
        
        sw = new StringWriter();
        g = mapper.getFactory().createGenerator(sw);
        ObjectWriter ow = mapper.writer();
        ow.writeValue(g, Integer.valueOf(99));
        assertEquals("", sw.toString());
        
        g.flush();
        assertEquals("99", sw.toString());
        g.close();
    }

// com.fasterxml.jackson.databind.ser.SerializationFeaturesTest::testSingleElementCollections
    public void testSingleElementCollections() throws IOException
    {
        final ObjectWriter writer = objectWriter().with(SerializationFeature.WRITE_SINGLE_ELEM_ARRAYS_UNWRAPPED);

        
        ArrayList<String> strs = new ArrayList<String>();
        strs.add("xyz");
        assertEquals(quote("xyz"), writer.writeValueAsString(strs));
        ArrayList<Integer> ints = new ArrayList<Integer>();
        ints.add(13);
        assertEquals("13", writer.writeValueAsString(ints));

        
        HashSet<Long> longs = new HashSet<Long>();
        longs.add(42L);
        assertEquals("42", writer.writeValueAsString(longs));
        
        final String EXP_STRINGS = "{\"values\":\"foo\"}";
        assertEquals(EXP_STRINGS, writer.writeValueAsString(new StringListBean(Collections.singletonList("foo"))));

        final Set<String> SET = new HashSet<String>();
        SET.add("foo");
        assertEquals(EXP_STRINGS, writer.writeValueAsString(new StringListBean(SET)));
        
        
        assertEquals("true", writer.writeValueAsString(new boolean[] { true }));
        assertEquals("true", writer.writeValueAsString(new Boolean[] { Boolean.TRUE }));
        assertEquals("3", writer.writeValueAsString(new int[] { 3 }));
        assertEquals(quote("foo"), writer.writeValueAsString(new String[] { "foo" }));
    }

// com.fasterxml.jackson.databind.ser.SerializationFeaturesTest::testVisibilityFeatures
    public void testVisibilityFeatures() throws Exception
    {
        ObjectMapper om = new ObjectMapper();
        
        om.configure(MapperFeature.AUTO_DETECT_FIELDS, false);
        om.configure(MapperFeature.AUTO_DETECT_GETTERS, false);
        om.configure(MapperFeature.AUTO_DETECT_SETTERS, false);
        om.configure(MapperFeature.AUTO_DETECT_IS_GETTERS, false);
        om.configure(MapperFeature.USE_GETTERS_AS_SETTERS, false);
        om.configure(MapperFeature.CAN_OVERRIDE_ACCESS_MODIFIERS, true);
        om.configure(MapperFeature.INFER_PROPERTY_MUTATORS, false);
        om.configure(MapperFeature.USE_ANNOTATIONS, true);

        JavaType javaType = om.getTypeFactory().constructType(TCls.class);        
        BeanDescription desc = (BeanDescription) om.getSerializationConfig().introspect(javaType);
        List<BeanPropertyDefinition> props = desc.findProperties();
        if (props.size() != 1) {
            fail("Should find 1 property, not "+props.size()+"; properties = "+props);
        }
    }

// com.fasterxml.jackson.databind.ser.TestAnnotationInheritance::testSimpleGetterInheritance
    public void testSimpleGetterInheritance() throws Exception
    {
        ObjectMapper m = new ObjectMapper();
        Map<String,Object> result = writeAndMap(m, new PojoSubclass());
        assertEquals(2, result.size());
        assertEquals(Integer.valueOf(7), result.get("length"));
        assertEquals(Integer.valueOf(9), result.get("width"));
    }

// com.fasterxml.jackson.databind.ser.TestAnnotationInheritance::testSimpleGetterInterfaceImpl
    public void testSimpleGetterInterfaceImpl() throws Exception
    {
        ObjectMapper m = new ObjectMapper();
        Map<String,Object> result = writeAndMap(m, new PojoImpl());
        
        assertEquals(3, result.size());
        assertEquals(Integer.valueOf(5), result.get("foobar"));
        assertEquals(Integer.valueOf(1), result.get("width"));
        assertEquals(Integer.valueOf(2), result.get("length"));
    }

// com.fasterxml.jackson.databind.ser.TestAnnotations::testSimpleGetter
    public void testSimpleGetter() throws Exception
    {
        Map<String,Object> result = writeAndMap(MAPPER, new SizeClassGetter());
        assertEquals(3, result.size());
        assertEquals(Integer.valueOf(3), result.get("size"));
        assertEquals(Integer.valueOf(-17), result.get("length"));
        assertEquals(Integer.valueOf(0), result.get("value"));
    }

// com.fasterxml.jackson.databind.ser.TestAnnotations::testSimpleGetter2
    public void testSimpleGetter2() throws Exception
    {
        Map<String,Object> result = writeAndMap(MAPPER, new SizeClassGetter2());
        assertEquals(1, result.size());
        assertEquals(Integer.valueOf(3), result.get("x"));
    }

// com.fasterxml.jackson.databind.ser.TestAnnotations::testSimpleGetter3
    public void testSimpleGetter3() throws Exception
    {
        Map<String,Object> result = writeAndMap(MAPPER, new SizeClassGetter3());
        assertEquals(1, result.size());
        assertEquals(Integer.valueOf(8), result.get("y"));
    }

// com.fasterxml.jackson.databind.ser.TestAnnotations::testGetterInheritance
    public void testGetterInheritance() throws Exception
    {
        Map<String,Object> result = writeAndMap(MAPPER, new SubClassBean());
        assertEquals(3, result.size());
        assertEquals(Integer.valueOf(1), result.get("x"));
        assertEquals(Integer.valueOf(2), result.get("y"));
        assertEquals(Integer.valueOf(3), result.get("z"));
    }

// com.fasterxml.jackson.databind.ser.TestAnnotations::testClassSerializer
    public void testClassSerializer() throws Exception
    {
        StringWriter sw = new StringWriter();
        MAPPER.writeValue(sw, new ClassSerializer());
        assertEquals("true", sw.toString());
    }

// com.fasterxml.jackson.databind.ser.TestAnnotations::testActiveMethodSerializer
    public void testActiveMethodSerializer() throws Exception
    {
        StringWriter sw = new StringWriter();
        MAPPER.writeValue(sw, new ClassMethodSerializer(13));
        
        assertEquals("{\"x\":\"X13X\"}", sw.toString());
    }

// com.fasterxml.jackson.databind.ser.TestAnnotations::testInactiveMethodSerializer
    public void testInactiveMethodSerializer() throws Exception
    {
        String json = MAPPER.writeValueAsString(new InactiveClassMethodSerializer(8));
        
        assertEquals("{\"x\":8}", json);
    }

// com.fasterxml.jackson.databind.ser.TestAnnotations::testGettersWithoutSetters
    public void testGettersWithoutSetters() throws Exception
    {
        ObjectMapper m = new ObjectMapper();
        GettersWithoutSetters bean = new GettersWithoutSetters(123);
        assertFalse(m.isEnabled(MapperFeature.REQUIRE_SETTERS_FOR_GETTERS));
    
        
        assertEquals("{\"a\":3,\"b\":4,\"c\":5,\"d\":6}", m.writeValueAsString(bean));

        
        m = new ObjectMapper();
        m.enable(MapperFeature.REQUIRE_SETTERS_FOR_GETTERS);
        assertEquals("{\"a\":3,\"c\":5,\"d\":6}", m.writeValueAsString(bean));
    }

// com.fasterxml.jackson.databind.ser.TestAnnotations::testGettersWithoutSettersOverride
    public void testGettersWithoutSettersOverride() throws Exception
    {
        GettersWithoutSetters2 bean = new GettersWithoutSetters2();
        ObjectMapper m = new ObjectMapper();
        m.enable(MapperFeature.REQUIRE_SETTERS_FOR_GETTERS);
        assertEquals("{\"a\":123}", m.writeValueAsString(bean));
    }

// com.fasterxml.jackson.databind.ser.TestArraySerialization::testLongStringArray
    public void testLongStringArray() throws Exception
    {
        final int SIZE = 40000;

        StringBuilder sb = new StringBuilder(SIZE*2);
        for (int i = 0; i < SIZE; ++i) {
            sb.append((char) i);
        }
        String str = sb.toString();
        byte[] data = MAPPER.writeValueAsBytes(new String[] { "abc", str, null, str });
        JsonParser jp = MAPPER.getFactory().createParser(data);
        assertToken(JsonToken.START_ARRAY, jp.nextToken());
        assertToken(JsonToken.VALUE_STRING, jp.nextToken());
        assertEquals("abc", jp.getText());
        assertToken(JsonToken.VALUE_STRING, jp.nextToken());
        String actual = jp.getText();
        assertEquals(str.length(), actual.length());
        assertEquals(str, actual);
        assertToken(JsonToken.VALUE_NULL, jp.nextToken());
        assertToken(JsonToken.VALUE_STRING, jp.nextToken());
        assertEquals(str, jp.getText());
        assertToken(JsonToken.END_ARRAY, jp.nextToken());
        assertNull(jp.nextToken());
        jp.close();
    }

// com.fasterxml.jackson.databind.ser.TestArraySerialization::testIntArray
    public void testIntArray() throws Exception
    {
        String json = MAPPER.writeValueAsString(new int[] { 1, 2, 3, -7 });
        assertEquals("[1,2,3,-7]", json);
    }

// com.fasterxml.jackson.databind.ser.TestArraySerialization::testBigIntArray
    public void testBigIntArray() throws Exception
    {
        final int SIZE = 99999;
        int[] ints = new int[SIZE];
        for (int i = 0; i < ints.length; ++i) {
            ints[i] = i;
        }

        
        
        
        JsonFactory f = MAPPER.getFactory();
        for (int round = 0; round < 3; ++round) {
            byte[] data = MAPPER.writeValueAsBytes(ints);
            JsonParser jp = f.createParser(data);
            assertToken(JsonToken.START_ARRAY, jp.nextToken());
            for (int i = 0; i < SIZE; ++i) {
                assertToken(JsonToken.VALUE_NUMBER_INT, jp.nextToken());
                assertEquals(i, jp.getIntValue());
            }
            assertToken(JsonToken.END_ARRAY, jp.nextToken());
            jp.close();
        }
    }

// com.fasterxml.jackson.databind.ser.TestArraySerialization::testLongArray
    public void testLongArray() throws Exception
    {
        String json = MAPPER.writeValueAsString(new long[] { Long.MIN_VALUE, 0, Long.MAX_VALUE });
        assertEquals("["+Long.MIN_VALUE+",0,"+Long.MAX_VALUE+"]", json);
    }

// com.fasterxml.jackson.databind.ser.TestArraySerialization::testStringArray
    public void testStringArray() throws Exception
    {
        String json = MAPPER.writeValueAsString(new String[] { "a", "\"foo\"", null });
        assertEquals("[\"a\",\"\\\"foo\\\"\",null]", json);
    }

// com.fasterxml.jackson.databind.ser.TestArraySerialization::testDoubleArray
    public void testDoubleArray() throws Exception
    {
        String json = MAPPER.writeValueAsString(new double[] { 1.01, 2.0, -7, Double.NaN, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY });
        assertEquals("[1.01,2.0,-7.0,\"NaN\",\"-Infinity\",\"Infinity\"]", json);
    }

// com.fasterxml.jackson.databind.ser.TestArraySerialization::testFloatArray
    public void testFloatArray() throws Exception
    {
        String json = MAPPER.writeValueAsString(new float[] { 1.01f, 2.0f, -7f, Float.NaN, Float.NEGATIVE_INFINITY, Float.POSITIVE_INFINITY });
        assertEquals("[1.01,2.0,-7.0,\"NaN\",\"-Infinity\",\"Infinity\"]", json);
    }

// com.fasterxml.jackson.databind.ser.TestAutoDetect::testDefaults
    public void testDefaults() throws Exception
    {
        ObjectMapper m = new ObjectMapper();
        
        assertEquals("{\"p1\":\"public\"}",
                     m.writeValueAsString(new FieldBean()));
        assertEquals("{\"a\":\"a\"}",
                     m.writeValueAsString(new MethodBean()));
    }

// com.fasterxml.jackson.databind.ser.TestAutoDetect::testProtectedViaAnnotations
    public void testProtectedViaAnnotations() throws Exception
    {
        ObjectMapper m = new ObjectMapper();

        Map<String,Object> result = writeAndMap(m, new ProtFieldBean());
        assertEquals(2, result.size());
        assertEquals("public", result.get("p1"));
        assertEquals("protected", result.get("p2"));
        assertNull(result.get("p3"));

        result = writeAndMap(m, new ProtMethodBean());
        assertEquals(2, result.size());
        assertEquals("a", result.get("a"));
        assertEquals("b", result.get("b"));
        assertNull(result.get("c"));
    }

// com.fasterxml.jackson.databind.ser.TestAutoDetect::testPrivateUsingGlobals
    public void testPrivateUsingGlobals() throws Exception
    {
        ObjectMapper m = new ObjectMapper();
        VisibilityChecker<?> vc = m.getVisibilityChecker();
        vc = vc.withFieldVisibility(JsonAutoDetect.Visibility.ANY);
        m.setVisibility(vc);
        
        Map<String,Object> result = writeAndMap(m, new FieldBean());
        assertEquals(3, result.size());
        assertEquals("public", result.get("p1"));
        assertEquals("protected", result.get("p2"));
        assertEquals("private", result.get("p3"));

        m = new ObjectMapper();
        vc = m.getVisibilityChecker();
        vc = vc.withGetterVisibility(JsonAutoDetect.Visibility.ANY);
        m.setVisibility(vc);
        result = writeAndMap(m, new MethodBean());
        assertEquals(3, result.size());
        assertEquals("a", result.get("a"));
        assertEquals("b", result.get("b"));
        assertEquals("c", result.get("c"));
    }

// com.fasterxml.jackson.databind.ser.TestAutoDetect::testBasicSetup
    public void testBasicSetup() throws Exception
    {
        ObjectMapper m = new ObjectMapper();
        VisibilityChecker<?> vc = m.getVisibilityChecker();
        vc = vc.with(JsonAutoDetect.Visibility.ANY);
        m.setVisibility(vc);

        Map<String,Object> result = writeAndMap(m, new FieldBean());
        assertEquals(3, result.size());
        assertEquals("public", result.get("p1"));
        assertEquals("protected", result.get("p2"));
        assertEquals("private", result.get("p3"));
    }

// com.fasterxml.jackson.databind.ser.TestAutoDetect::testMapperShortcutMethods
    public void testMapperShortcutMethods() throws Exception
    {
        ObjectMapper m = new ObjectMapper();
        m.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);

        Map<String,Object> result = writeAndMap(m, new FieldBean());
        assertEquals(3, result.size());
        assertEquals("public", result.get("p1"));
        assertEquals("protected", result.get("p2"));
        assertEquals("private", result.get("p3"));
    }

// com.fasterxml.jackson.databind.ser.TestBeanSerializer::testPropertyRemoval
    public void testPropertyRemoval() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new SerializerModifierModule(new RemovingModifier("a")));
        Bean bean = new Bean();
        assertEquals("{\"b\":\"b\"}", mapper.writeValueAsString(bean));
    }

// com.fasterxml.jackson.databind.ser.TestBeanSerializer::testPropertyReorder
    public void testPropertyReorder() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new SerializerModifierModule(new ReorderingModifier()));
        Bean bean = new Bean();
        assertEquals("{\"a\":\"a\",\"b\":\"b\"}", mapper.writeValueAsString(bean));
    }

// com.fasterxml.jackson.databind.ser.TestBeanSerializer::testBuilderReplacement
    public void testBuilderReplacement() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new SerializerModifierModule(new BuilderModifier(new BogusBeanSerializer(17))));
        Bean bean = new Bean();
        assertEquals("17", mapper.writeValueAsString(bean));
    }

// com.fasterxml.jackson.databind.ser.TestBeanSerializer::testSerializerReplacement
    public void testSerializerReplacement() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new SerializerModifierModule(new ReplacingModifier(new BogusBeanSerializer(123))));
        Bean bean = new Bean();
        assertEquals("123", mapper.writeValueAsString(bean));
    }

// com.fasterxml.jackson.databind.ser.TestBeanSerializer::testEmptyBean
    public void testEmptyBean() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new SimpleModule("test", Version.unknownVersion()) {
            @Override
            public void setupModule(SetupContext context)
            {
                super.setupModule(context);
                context.addBeanSerializerModifier(new EmptyBeanModifier());
            }
        });
        String json = mapper.writeValueAsString(new EmptyBean());
        assertEquals("{\"bogus\":\"foo\"}", json);
    }

// com.fasterxml.jackson.databind.ser.TestBeanSerializer::testEmptyBean539
    public void testEmptyBean539() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new SimpleModule("test", Version.unknownVersion()) {
            @Override
            public void setupModule(SetupContext context)
            {
                super.setupModule(context);
                context.addBeanSerializerModifier(new EmptyBeanModifier539());
            }
        });
        String json = mapper.writeValueAsString(new EmptyBean());
        assertEquals("42", json);
    }

// com.fasterxml.jackson.databind.ser.TestBeanSerializer::testModifyArraySerializer
    public void testModifyArraySerializer() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new SimpleModule("test")
            .setSerializerModifier(new ArraySerializerModifier()));
        assertEquals("123", mapper.writeValueAsString(new Integer[] { 1, 2 }));
    }

// com.fasterxml.jackson.databind.ser.TestBeanSerializer::testModifyCollectionSerializer
    public void testModifyCollectionSerializer() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new SimpleModule("test")
            .setSerializerModifier(new CollectionSerializerModifier()));
        assertEquals("123", mapper.writeValueAsString(new ArrayList<Integer>()));
    }

// com.fasterxml.jackson.databind.ser.TestBeanSerializer::testModifyMapSerializer
    public void testModifyMapSerializer() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new SimpleModule("test")
            .setSerializerModifier(new MapSerializerModifier()));
        assertEquals("123", mapper.writeValueAsString(new HashMap<String,String>()));
    }

// com.fasterxml.jackson.databind.ser.TestBeanSerializer::testModifyEnumSerializer
    public void testModifyEnumSerializer() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new SimpleModule("test")
            .setSerializerModifier(new EnumSerializerModifier()));
        assertEquals("123", mapper.writeValueAsString(EnumABC.C));
    }

// com.fasterxml.jackson.databind.ser.TestBeanSerializer::testModifyKeySerializer
    public void testModifyKeySerializer() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new SimpleModule("test")
            .setSerializerModifier(new KeySerializerModifier()));
        Map<String,Integer> map = new HashMap<String,Integer>();
        map.put("x", 3);
        assertEquals("{\"foo\":3}", mapper.writeValueAsString(map));
    }

// com.fasterxml.jackson.databind.ser.TestCollectionSerialization::testCollections
    public void testCollections() throws IOException
    {
        
        final int entryLen = 98;

        for (int type = 0; type < 4; ++type) {
            Object value;

            if (type == 0) { 
                int[] ints = new int[entryLen];
                for (int i = 0; i < entryLen; ++i) {
                    ints[i] = Integer.valueOf(i);
                }
                value = ints;
            } else {
                Collection<Integer> c;

                switch (type) {
                case 1:
                    c = new LinkedList<Integer>();
                    break;
                case 2:
                    c = new TreeSet<Integer>(); 
                    break;
                default:
                    c = new ArrayList<Integer>();
                    break;
                }
                for (int i = 0; i < entryLen; ++i) {
                    c.add(Integer.valueOf(i));
                }
                value = c;
            }
            String json = MAPPER.writeValueAsString(value);
            
            
            JsonParser jp = new JsonFactory().createParser(json);
            assertToken(JsonToken.START_ARRAY, jp.nextToken());
            for (int i = 0; i < entryLen; ++i) {
                assertToken(JsonToken.VALUE_NUMBER_INT, jp.nextToken());
                assertEquals(i, jp.getIntValue());
            }
            assertToken(JsonToken.END_ARRAY, jp.nextToken());
            jp.close();
        }
    }

// com.fasterxml.jackson.databind.ser.TestCollectionSerialization::testBigCollection
    public void testBigCollection() throws IOException
    {
        final int COUNT = 9999;
        ArrayList<Integer> value = new ArrayList<Integer>();
        for (int i = 0; i <= COUNT; ++i) {
            value.add(i);
        }
        
        for (int mode = 0; mode < 3; ++mode) {
            JsonParser jp = null;
            switch (mode) {
            case 0:
                {
                    byte[] data = MAPPER.writeValueAsBytes(value);
                    jp = new JsonFactory().createParser(data);
                }
                break;
            case 1:
                {
                    StringWriter sw = new StringWriter(value.size());
                    MAPPER.writeValue(sw, value);
                    jp = createParserUsingReader(sw.toString());
                }
                break;
            case 2:
                {
                    String str = MAPPER.writeValueAsString(value);
                    jp = createParserUsingReader(str);
                }
                break;
            }

            
            assertToken(JsonToken.START_ARRAY, jp.nextToken());
            for (int i = 0; i <= COUNT; ++i) {
                assertEquals(JsonToken.VALUE_NUMBER_INT, jp.nextToken());
                assertEquals(i, jp.getIntValue());
            }
            assertToken(JsonToken.END_ARRAY, jp.nextToken());
            jp.close();
        }
    }

// com.fasterxml.jackson.databind.ser.TestCollectionSerialization::testEnumMap
    public void testEnumMap() throws IOException
    {
        EnumMap<Key,String> map = new EnumMap<Key,String>(Key.class);
        map.put(Key.B, "xyz");
        map.put(Key.C, "abc");
        
        String json = MAPPER.writeValueAsString(map);
        assertEquals("{\"B\":\"xyz\",\"C\":\"abc\"}",json.trim());
    }

// com.fasterxml.jackson.databind.ser.TestCollectionSerialization::testEmptyBeanCollection
    public void testEmptyBeanCollection() throws IOException
    {
        Collection<Object> x = new ArrayList<Object>();
        x.add("foobar");
        CollectionBean cb = new CollectionBean(x);
        Map<String,Object> result = writeAndMap(MAPPER, cb);
        assertEquals(1, result.size());
        assertTrue(result.containsKey("values"));
        Collection<Object> x2 = (Collection<Object>) result.get("values");
        assertNotNull(x2);
        assertEquals(x, x2);
    }

// com.fasterxml.jackson.databind.ser.TestCollectionSerialization::testNullBeanCollection
    public void testNullBeanCollection()
        throws IOException
    {
        CollectionBean cb = new CollectionBean(null);
        Map<String,Object> result = writeAndMap(MAPPER, cb);
        assertEquals(1, result.size());
        assertTrue(result.containsKey("values"));
        assertNull(result.get("values"));
    }

// com.fasterxml.jackson.databind.ser.TestCollectionSerialization::testEmptyBeanEnumMap
    public void testEmptyBeanEnumMap() throws IOException
    {
        EnumMap<Key,String> map = new EnumMap<Key,String>(Key.class);
        EnumMapBean b = new EnumMapBean(map);
        Map<String,Object> result = writeAndMap(MAPPER, b);

        assertEquals(1, result.size());
        assertTrue(result.containsKey("map"));
        
        Map<Object,Object> map2 = (Map<Object,Object>) result.get("map");
        assertNotNull(map2);
        assertEquals(0, map2.size());
    }

// com.fasterxml.jackson.databind.ser.TestCollectionSerialization::testNullBeanEnumMap
    public void testNullBeanEnumMap() throws IOException
    {
        EnumMapBean b = new EnumMapBean(null);
        Map<String,Object> result = writeAndMap(MAPPER, b);

        assertEquals(1, result.size());
        assertTrue(result.containsKey("map"));
        assertNull(result.get("map"));
    }

// com.fasterxml.jackson.databind.ser.TestCollectionSerialization::testListSerializer
    public void testListSerializer() throws IOException
    {
        assertEquals("\"[ab, cd, ef]\"",
                MAPPER.writeValueAsString(new PseudoList("ab", "cd", "ef")));
    }

// com.fasterxml.jackson.databind.ser.TestCollectionSerialization::testEmptyListOrArray
    public void testEmptyListOrArray() throws IOException
    {
        
        EmptyListBean list = new EmptyListBean();
        EmptyArrayBean array = new EmptyArrayBean();
        assertTrue(MAPPER.isEnabled(SerializationFeature.WRITE_EMPTY_JSON_ARRAYS));
        assertEquals("{\"empty\":[]}", MAPPER.writeValueAsString(list));
        assertEquals("{\"empty\":[]}", MAPPER.writeValueAsString(array));

        
        ObjectMapper m = new ObjectMapper();
        m.configure(SerializationFeature.WRITE_EMPTY_JSON_ARRAYS, false);
        assertEquals("{}", m.writeValueAsString(list));
        assertEquals("{}", m.writeValueAsString(array));
    }

// com.fasterxml.jackson.databind.ser.TestConfig::testEnumIndexes
    public void testEnumIndexes()
    {
        int max = 0;
        
        for (SerializationFeature f : SerializationFeature.values()) {
            max = Math.max(max, f.ordinal());
        }
        if (max >= 31) { 
            fail("Max number of SerializationFeature enums reached: "+max);
        }
    }

// com.fasterxml.jackson.databind.ser.TestConfig::testDefaults
    public void testDefaults()
    {
        SerializationConfig cfg = MAPPER.getSerializationConfig();

        
        assertTrue(cfg.isEnabled(MapperFeature.USE_ANNOTATIONS));
        assertTrue(cfg.isEnabled(MapperFeature.AUTO_DETECT_GETTERS));
        assertTrue(cfg.isEnabled(MapperFeature.CAN_OVERRIDE_ACCESS_MODIFIERS));

        assertTrue(cfg.isEnabled(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS));

        assertFalse(cfg.isEnabled(SerializationFeature.INDENT_OUTPUT));
        assertFalse(cfg.isEnabled(MapperFeature.USE_STATIC_TYPING));

        
        assertTrue(cfg.isEnabled(MapperFeature.AUTO_DETECT_IS_GETTERS));
        
        
        assertTrue(cfg.isEnabled(SerializationFeature.FAIL_ON_EMPTY_BEANS));
        
        assertTrue(cfg.isEnabled(MapperFeature.DEFAULT_VIEW_INCLUSION));

    }

// com.fasterxml.jackson.databind.ser.TestConfig::testOverrideIntrospectors
    public void testOverrideIntrospectors()
    {
        SerializationConfig cfg = MAPPER.getSerializationConfig();
        
        cfg = cfg.with((ClassIntrospector) null); 
        cfg = cfg.with((AnnotationIntrospector) null);
        assertNull(cfg.getAnnotationIntrospector());
    }

// com.fasterxml.jackson.databind.ser.TestConfig::testMisc
    public void testMisc()
    {
        ObjectMapper m = new ObjectMapper();
        m.setDateFormat(null); 
        assertNotNull(m.getSerializationConfig().toString()); 
    }

// com.fasterxml.jackson.databind.ser.TestConfig::testIndentation
    public void testIndentation() throws Exception
    {
        Map<String,Integer> map = new HashMap<String,Integer>();
        map.put("a", Integer.valueOf(2));
        String result = MAPPER.writer().with(SerializationFeature.INDENT_OUTPUT)
                .writeValueAsString(map);
        
        String lf = getLF();
        assertEquals("{"+lf+"  \"a\" : 2"+lf+"}", result);
    }

// com.fasterxml.jackson.databind.ser.TestConfig::testAnnotationsDisabled
    public void testAnnotationsDisabled() throws Exception
    {
        
        assertTrue(MAPPER.isEnabled(MapperFeature.USE_ANNOTATIONS));
        Map<String,Object> result = writeAndMap(MAPPER, new AnnoBean());
        assertEquals(2, result.size());

        ObjectMapper m2 = new ObjectMapper();
        m2.configure(MapperFeature.USE_ANNOTATIONS, false);
        result = writeAndMap(m2, new AnnoBean());
        assertEquals(1, result.size());
    }

// com.fasterxml.jackson.databind.ser.TestConfig::testProviderConfig
    public void testProviderConfig() throws Exception   
    {
        ObjectMapper mapper = new ObjectMapper();
        DefaultSerializerProvider prov = (DefaultSerializerProvider) mapper.getSerializerProvider();
        assertEquals(0, prov.cachedSerializersCount());
        
        Map<String,Object> result = this.writeAndMap(mapper, new AnnoBean());
        assertEquals(2, result.size());
        assertEquals(Integer.valueOf(1), result.get("x"));
        assertEquals(Integer.valueOf(2), result.get("y"));

        
        
        int count = prov.cachedSerializersCount();
        if (count < 2) {
            fail("Should have at least 2 cached serializers, got "+count);
        }
        prov.flushCachedSerializers();
        assertEquals(0, prov.cachedSerializersCount());
    }

// com.fasterxml.jackson.databind.ser.TestConfig::testIndentWithPassedGenerator
    public void testIndentWithPassedGenerator() throws Exception
    {
        Indentable input = new Indentable();
        assertEquals("{\"a\":3}", MAPPER.writeValueAsString(input));
        String LF = getLF();
        String INDENTED = "{"+LF+"  \"a\" : 3"+LF+"}";
        final ObjectWriter indentWriter = MAPPER.writer().with(SerializationFeature.INDENT_OUTPUT);
        assertEquals(INDENTED, indentWriter.writeValueAsString(input));

        
        StringWriter sw = new StringWriter();
        JsonGenerator jgen = MAPPER.getFactory().createGenerator(sw);
        indentWriter.writeValue(jgen, input);
        jgen.close();
        assertEquals(INDENTED, sw.toString());

        
        sw = new StringWriter();
        ObjectMapper m2 = new ObjectMapper();
        m2.enable(SerializationFeature.INDENT_OUTPUT);
        jgen = m2.getFactory().createGenerator(sw);
        m2.writeValue(jgen, input);
        jgen.close();
        assertEquals(INDENTED, sw.toString());
    }

// com.fasterxml.jackson.databind.ser.TestConfig::testNoAccessOverrides
    public void testNoAccessOverrides() throws Exception
    {
        ObjectMapper m = new ObjectMapper();
        m.disable(MapperFeature.CAN_OVERRIDE_ACCESS_MODIFIERS);
        assertEquals("{\"x\":1}", m.writeValueAsString(new SimpleBean()));
    }

// com.fasterxml.jackson.databind.ser.TestConfig::testDateFormatConfig
    public void testDateFormatConfig() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        TimeZone tz1 = TimeZone.getTimeZone("America/Los_Angeles");
        TimeZone tz2 = TimeZone.getTimeZone("Central Standard Time");

        
        assertEquals(tz1, tz1);
        assertEquals(tz2, tz2);
        if (tz1.equals(tz2)) {
            fail();
        }

        mapper.setTimeZone(tz1);
        assertEquals(tz1, mapper.getSerializationConfig().getTimeZone());
        assertEquals(tz1, mapper.getDeserializationConfig().getTimeZone());

        
        assertEquals(tz1, mapper.writer().getConfig().getTimeZone());
        assertEquals(tz1, mapper.reader().getConfig().getTimeZone());
        
        SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        f.setTimeZone(tz2);
        mapper.setDateFormat(f);

        
        assertEquals(tz1, mapper.getSerializationConfig().getTimeZone());
        assertEquals(tz1, mapper.getDeserializationConfig().getTimeZone());
        assertEquals(tz1, mapper.writer().getConfig().getTimeZone());
        assertEquals(tz1, mapper.reader().getConfig().getTimeZone());
    }

// com.fasterxml.jackson.databind.ser.TestCustomSerializers::testCustomization
    public void testCustomization() throws Exception
    {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.addMixIn(Element.class, ElementMixin.class);
        Element element = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument().createElement("el");
        StringWriter sw = new StringWriter();
        objectMapper.writeValue(sw, element);
        assertEquals(sw.toString(), "\"element\"");
    }

// com.fasterxml.jackson.databind.ser.TestCustomSerializers::testCustomLists
    public void testCustomLists() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        SimpleModule module = new SimpleModule("test", Version.unknownVersion());
        JsonSerializer<?> ser = new CollectionSerializer(null, false, null, null);
        final JsonSerializer<Object> collectionSerializer = (JsonSerializer<Object>) ser;

        module.addSerializer(Collection.class, new JsonSerializer<Collection>() {
            @Override
            public void serialize(Collection value, JsonGenerator jgen, SerializerProvider provider)
                    throws IOException, JsonProcessingException {
                if (value.size() != 0) {
                    collectionSerializer.serialize(value, jgen, provider);
                } else {
                    jgen.writeNull();
                }
            }
        });
        mapper.registerModule(module);
        assertEquals("null", mapper.writeValueAsString(new ArrayList<Object>()));
    }

// com.fasterxml.jackson.databind.ser.TestCustomSerializers::testDelegating
    public void testDelegating() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        SimpleModule module = new SimpleModule("test", Version.unknownVersion());
        module.addSerializer(new StdDelegatingSerializer(Immutable.class,
                new StdConverter<Immutable, Map<String,Integer>>() {
                    @Override
                    public Map<String, Integer> convert(Immutable value)
                    {
                        HashMap<String,Integer> map = new LinkedHashMap<String,Integer>();
                        map.put("x", value.x());
                        map.put("y", value.y());
                        return map;
                    }
        }));
        mapper.registerModule(module);
        assertEquals("{\"x\":3,\"y\":7}", mapper.writeValueAsString(new Immutable()));
    }

// com.fasterxml.jackson.databind.ser.TestCustomSerializers::testCustomEscapes
    public void testCustomEscapes() throws Exception
    {
        assertEquals(quote("foo\\u0062\\Ar"),
                MAPPER.writer(new CustomEscapes()).writeValueAsString("foobar"));
    }

// com.fasterxml.jackson.databind.ser.TestCustomSerializers::testNumberSubclass
    public void testNumberSubclass() throws Exception
    {
        assertEquals(aposToQuotes("{'x':42}"),
                MAPPER.writeValueAsString(new LikeNumber(42)));
    }

// com.fasterxml.jackson.databind.ser.TestCustomSerializers::testWithCurrentValue
    public void testWithCurrentValue() throws Exception
    {
        assertEquals(aposToQuotes("{'prop':'Issue631Bean/42'}"),
                MAPPER.writeValueAsString(new Issue631Bean(42)));
    }

// com.fasterxml.jackson.databind.ser.TestCyclicTypes::testLinked
    public void testLinked() throws Exception
    {
        Bean last = new Bean(null, "last");
        Bean first = new Bean(last, "first");
        Map<String,Object> map = writeAndMap(new ObjectMapper(), first);

        assertEquals(2, map.size());
        assertEquals("first", map.get("name"));

        @SuppressWarnings("unchecked")
        Map<String,Object> map2 = (Map<String,Object>) map.get("next");
        assertNotNull(map2);
        assertEquals(2, map2.size());
        assertEquals("last", map2.get("name"));
        assertNull(map2.get("next"));
    }

// com.fasterxml.jackson.databind.ser.TestCyclicTypes::testSelfReference
    public void testSelfReference() throws Exception
    {
        Bean selfRef = new Bean(null, "self-refs");
        Bean first = new Bean(selfRef, "first");
        selfRef.assignNext(selfRef);
        ObjectMapper m = new ObjectMapper();
        Bean[] wrapper = new Bean[] { first };
        try {
            writeAndMap(m, wrapper);
        } catch (JsonMappingException e) {
            verifyException(e, "Direct self-reference leading to cycle");
        }
    }

// com.fasterxml.jackson.databind.ser.TestEmptyClass::testEmptyWithAnnotations
    public void testEmptyWithAnnotations() throws Exception
    {
        
        try {
            serializeAsString(mapper, new Empty());
        } catch (JsonMappingException e) {
            verifyException(e, "No serializer found for class");
        }

        
        assertEquals("{}", serializeAsString(mapper, new EmptyWithAnno()));

        
        ObjectMapper m2 = new ObjectMapper();
        m2.addMixIn(Empty.class, EmptyWithAnno.class);
        assertEquals("{}", m2.writeValueAsString(new Empty()));
    }

// com.fasterxml.jackson.databind.ser.TestEmptyClass::testEmptyWithFeature
    public void testEmptyWithFeature() throws Exception
    {
        
        assertTrue(mapper.getSerializationConfig().isEnabled(SerializationFeature.FAIL_ON_EMPTY_BEANS));
        mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        assertEquals("{}", serializeAsString(mapper, new Empty()));
    }

// com.fasterxml.jackson.databind.ser.TestEmptyClass::testCustomNoEmpty
    public void testCustomNoEmpty() throws Exception
    {
        
        assertEquals("{\"value\":123}", mapper.writeValueAsString(new NonZeroWrapper(123)));
        
        assertEquals("{}", mapper.writeValueAsString(new NonZeroWrapper(0)));
    }

// com.fasterxml.jackson.databind.ser.TestEnumSerialization::testSimple
    public void testSimple() throws Exception
    {
        assertEquals("\"B\"", MAPPER.writeValueAsString(TestEnum.B));
    }

// com.fasterxml.jackson.databind.ser.TestEnumSerialization::testEnumSet
    public void testEnumSet() throws Exception
    {
        StringWriter sw = new StringWriter();
        EnumSet<TestEnum> value = EnumSet.of(TestEnum.B);
        MAPPER.writeValue(sw, value);
        assertEquals("[\"B\"]", sw.toString());
    }

// com.fasterxml.jackson.databind.ser.TestEnumSerialization::testEnumUsingToString
    public void testEnumUsingToString() throws Exception
    {
        StringWriter sw = new StringWriter();
        MAPPER.writeValue(sw, AnnotatedTestEnum.C2);
        assertEquals("\"c2\"", sw.toString());
    }

// com.fasterxml.jackson.databind.ser.TestEnumSerialization::testSubclassedEnums
    public void testSubclassedEnums() throws Exception
    {
        assertEquals("\"B\"", MAPPER.writeValueAsString(EnumWithSubClass.B));
    }

// com.fasterxml.jackson.databind.ser.TestEnumSerialization::testEnumsWithJsonValue
    public void testEnumsWithJsonValue() throws Exception
    {
        assertEquals("\"value:bar\"", MAPPER.writeValueAsString(EnumWithJsonValue.B));
    }

// com.fasterxml.jackson.databind.ser.TestEnumSerialization::testEnumsWithJsonValueUsingMixin
    public void testEnumsWithJsonValueUsingMixin() throws Exception
    {
        
        ObjectMapper m = new ObjectMapper();
        m.addMixIn(TestEnum.class, ToStringMixin.class);
        assertEquals("\"b\"", m.writeValueAsString(TestEnum.B));
    }

// com.fasterxml.jackson.databind.ser.TestEnumSerialization::testEnumsWithJsonValueInMap
    public void testEnumsWithJsonValueInMap() throws Exception
    {
        EnumMap<EnumWithJsonValue,String> input = new EnumMap<EnumWithJsonValue,String>(EnumWithJsonValue.class);
        input.put(EnumWithJsonValue.B, "x");
        
        assertEquals("{\"value:bar\":\"x\"}", MAPPER.writeValueAsString(input));
    }

// com.fasterxml.jackson.databind.ser.TestEnumSerialization::testSerializableEnum
    public void testSerializableEnum() throws Exception
    {
        assertEquals("\"foo\"", MAPPER.writeValueAsString(SerializableEnum.A));
    }

// com.fasterxml.jackson.databind.ser.TestEnumSerialization::testToStringEnum
    public void testToStringEnum() throws Exception
    {
        ObjectMapper m = new ObjectMapper();
        m.configure(SerializationFeature.WRITE_ENUMS_USING_TO_STRING, true);
        assertEquals("\"b\"", m.writeValueAsString(LowerCaseEnum.B));

        
        assertEquals("\"B\"",
                m.writer().without(SerializationFeature.WRITE_ENUMS_USING_TO_STRING)
                    .writeValueAsString(LowerCaseEnum.B));
    }

// com.fasterxml.jackson.databind.ser.TestEnumSerialization::testToStringEnumWithEnumMap
    public void testToStringEnumWithEnumMap() throws Exception
    {
        ObjectMapper m = new ObjectMapper();
        m.enable(SerializationFeature.WRITE_ENUMS_USING_TO_STRING);
        EnumMap<LowerCaseEnum,String> enums = new EnumMap<LowerCaseEnum,String>(LowerCaseEnum.class);
        enums.put(LowerCaseEnum.C, "value");
        assertEquals("{\"c\":\"value\"}", m.writeValueAsString(enums));
    }

// com.fasterxml.jackson.databind.ser.TestEnumSerialization::testMapWithEnumKeys
    public void testMapWithEnumKeys() throws Exception
    {
        MapBean bean = new MapBean();
        bean.add(TestEnum.B, 3);

        
        String json = MAPPER.writeValueAsString(bean);
        assertEquals("{\"map\":{\"B\":3}}", json);

        ObjectMapper m2 = new ObjectMapper();
        
        m2.enable(SerializationFeature.WRITE_ENUMS_USING_TO_STRING);
        json = m2.writeValueAsString(bean);
        assertEquals("{\"map\":{\"b\":3}}", json);
    }

// com.fasterxml.jackson.databind.ser.TestEnumSerialization::testAsIndex
    public void testAsIndex() throws Exception
    {
        
        ObjectMapper m = new ObjectMapper();
        assertFalse(m.isEnabled(SerializationFeature.WRITE_ENUMS_USING_INDEX));
        assertEquals(quote("B"), m.writeValueAsString(TestEnum.B));

        
        m.enable(SerializationFeature.WRITE_ENUMS_USING_INDEX);
        assertEquals("1", m.writeValueAsString(TestEnum.B));
    }

// com.fasterxml.jackson.databind.ser.TestEnumSerialization::testAnnotationsOnEnumCtor
    public void testAnnotationsOnEnumCtor() throws Exception
    {
        assertEquals(quote("V1"), MAPPER.writeValueAsString(OK.V1));
        assertEquals(quote("V1"), MAPPER.writeValueAsString(NOT_OK.V1));
        assertEquals(quote("V2"), MAPPER.writeValueAsString(NOT_OK2.V2));
    }

// com.fasterxml.jackson.databind.ser.TestEnumSerialization::testEnumAsObjectValid
    public void testEnumAsObjectValid() throws Exception {
        assertEquals("{\"value\":\"a1\"}", MAPPER.writeValueAsString(PoNUM.A));
    }

// com.fasterxml.jackson.databind.ser.TestEnumSerialization::testEnumAsIndexViaAnnotations
    public void testEnumAsIndexViaAnnotations() throws Exception {
        assertEquals("{\"text\":0}", MAPPER.writeValueAsString(new PoNUMContainer()));
    }

// com.fasterxml.jackson.databind.ser.TestEnumSerialization::testEnumAsObjectBroken
    public void testEnumAsObjectBroken() throws Exception
    {
        assertEquals("0", MAPPER.writeValueAsString(PoAsArray.A));
    }

// com.fasterxml.jackson.databind.ser.TestEnumSerialization::testGenericEnumSerializer
    public void testGenericEnumSerializer() throws Exception
    {
        
        ObjectMapper m = new ObjectMapper();
        SimpleModule module = new SimpleModule("foobar");
        module.addSerializer(Enum.class, new LowerCasingEnumSerializer());
        m.registerModule(module);
        assertEquals(quote("b"), m.writeValueAsString(TestEnum.B));
    }

// com.fasterxml.jackson.databind.ser.TestEnumSerialization::testOverrideEnumAsString
    public void testOverrideEnumAsString() throws Exception {
        assertEquals("{\"value\":\"B\"}", MAPPER.writeValueAsString(new PoOverrideAsString()));
    }

// com.fasterxml.jackson.databind.ser.TestEnumSerialization::testOverrideEnumAsNumber
    public void testOverrideEnumAsNumber() throws Exception {
        assertEquals("{\"value\":1}", MAPPER.writeValueAsString(new PoOverrideAsNumber()));
    }

// com.fasterxml.jackson.databind.ser.TestEnumSerialization::testJsonValueForEnumMapKey
    public void testJsonValueForEnumMapKey() throws Exception {
        assertEquals(aposToQuotes("{'stuff':{'longValue':'foo'}}"),
                MAPPER.writeValueAsString(new MyStuff594("foo")));
    }

// com.fasterxml.jackson.databind.ser.TestEnumSerialization::testCustomEnumMapKeySerializer
    public void testCustomEnumMapKeySerializer() throws Exception {
        String json = MAPPER.writeValueAsString(new MyBean661("abc"));
        assertEquals(aposToQuotes("{'X-FOO':'abc'}"), json);
    }

// com.fasterxml.jackson.databind.ser.TestEnumSerialization::testEnumMapSerDefault
    public void testEnumMapSerDefault() throws Exception {
        final ObjectMapper mapper = new ObjectMapper();
        EnumMap<LC749Enum, String> m = new EnumMap<LC749Enum, String>(LC749Enum.class);
        m.put(LC749Enum.A, "value");
        assertEquals("{\"A\":\"value\"}", mapper.writeValueAsString(m));
    }

// com.fasterxml.jackson.databind.ser.TestEnumSerialization::testEnumMapSerDisableToString
    public void testEnumMapSerDisableToString() throws Exception {
        final ObjectMapper mapper = new ObjectMapper();
        ObjectWriter w = mapper.writer().without(SerializationFeature.WRITE_ENUMS_USING_TO_STRING);
        EnumMap<LC749Enum, String> m = new EnumMap<LC749Enum, String>(LC749Enum.class);
        m.put(LC749Enum.A, "value");
        assertEquals("{\"A\":\"value\"}", w.writeValueAsString(m));
    }

// com.fasterxml.jackson.databind.ser.TestEnumSerialization::testEnumMapSerEnableToString
    public void testEnumMapSerEnableToString() throws Exception {
        final ObjectMapper mapper = new ObjectMapper();
        ObjectWriter w = mapper.writer().with(SerializationFeature.WRITE_ENUMS_USING_TO_STRING);
        EnumMap<LC749Enum, String> m = new EnumMap<LC749Enum, String>(LC749Enum.class);
        m.put(LC749Enum.A, "value");
        assertEquals("{\"a\":\"value\"}", w.writeValueAsString(m));
    }

// com.fasterxml.jackson.databind.ser.TestExceptionHandling::testCatchAndRethrow
    public void testCatchAndRethrow()
        throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        SimpleModule module = new SimpleModule("test-exceptions", Version.unknownVersion());
        module.addSerializer(Bean.class, new SerializerWithErrors());
        mapper.registerModule(module);
        try {
            StringWriter sw = new StringWriter();
            
            Bean[] b = { new Bean() };
            List<Bean[]> l = new ArrayList<Bean[]>();
            l.add(b);
            mapper.writeValue(sw, l);
            fail("Should have gotten an exception");
        } catch (IOException e) {
            
            verifyException(e, "test string");
            Throwable root = e.getCause();
            assertNotNull(root);

            if (!(root instanceof IllegalArgumentException)) {
                fail("Wrapped exception not IAE, but "+root.getClass());
            }
        }
    }

// com.fasterxml.jackson.databind.ser.TestExceptionHandling::testExceptionWithSimpleMapper
    public void testExceptionWithSimpleMapper()
        throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        try {
            BrokenStringWriter sw = new BrokenStringWriter("TEST");
            mapper.writeValue(sw, createLongObject());
            fail("Should have gotten an exception");
        } catch (IOException e) {
            verifyException(e, IOException.class, "TEST");
        }
    }

// com.fasterxml.jackson.databind.ser.TestExceptionHandling::testExceptionWithMapperAndGenerator
    public void testExceptionWithMapperAndGenerator()
        throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        JsonFactory f = new MappingJsonFactory();
        BrokenStringWriter sw = new BrokenStringWriter("TEST");
        JsonGenerator jg = f.createGenerator(sw);

        try {
            mapper.writeValue(jg, createLongObject());
            fail("Should have gotten an exception");
        } catch (IOException e) {
            verifyException(e, IOException.class, "TEST");
        }
    }

// com.fasterxml.jackson.databind.ser.TestExceptionHandling::testExceptionWithGeneratorMapping
    public void testExceptionWithGeneratorMapping()
        throws Exception
    {
        JsonFactory f = new MappingJsonFactory();
        JsonGenerator jg = f.createGenerator(new BrokenStringWriter("TEST"));
        try {
            jg.writeObject(createLongObject());
            fail("Should have gotten an exception");
        } catch (Exception e) {
            verifyException(e, IOException.class, "TEST");
        }
    }

// com.fasterxml.jackson.databind.ser.TestExceptionSerialization::testSimple
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

// com.fasterxml.jackson.databind.ser.TestExceptionSerialization::testIgnorals
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

        
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        ExceptionWithIgnoral output = mapper.readValue(json2, ExceptionWithIgnoral.class);
        assertNotNull(output);
        assertEquals("foobar", output.getMessage());
    }

// com.fasterxml.jackson.databind.ser.TestGenericTypes::testIssue468a
    public void testIssue468a() throws Exception
    {
        Person1 p1 = new Person1("John");
        p1.setAccount(new Key<Account>(new Account("something", 42L)));
        
        
        String json = MAPPER.writeValueAsString(p1);

        
        Map<String,Object> map = MAPPER.readValue(json, Map.class);
        assertEquals("John", map.get("name"));
        Object ob = map.get("account");
        assertNotNull(ob);
        Map<String,Object> acct = (Map<String,Object>) ob;
        Object idOb = acct.get("id");
        assertNotNull(idOb);
        Map<String,Object> key = (Map<String,Object>) idOb;
        assertEquals("something", key.get("name"));
        assertEquals(Integer.valueOf(42), key.get("id"));
    }

// com.fasterxml.jackson.databind.ser.TestGenericTypes::testIssue468b
    public void testIssue468b() throws Exception
    {
        Person2 p2 = new Person2("John");
        List<Key<Account>> accounts = new ArrayList<Key<Account>>();
        accounts.add(new Key<Account>(new Account("a", 42L)));
        accounts.add(new Key<Account>(new Account("b", 43L)));
        accounts.add(new Key<Account>(new Account("c", 44L)));
        p2.setAccounts(accounts);

        
        String json = MAPPER.writeValueAsString(p2);

        
        Map<String,Object> map = MAPPER.readValue(json, Map.class);
        assertEquals("John", map.get("name"));
        Object ob = map.get("accounts");
        assertNotNull(ob);
        List<?> acctList = (List<?>) ob;
        assertEquals(3, acctList.size());
        
    }

// com.fasterxml.jackson.databind.ser.TestGenericTypes::testUnboundTypes
    public void testUnboundTypes() throws Exception
    {
        GenericBogusWrapper<Integer> list = new GenericBogusWrapper<Integer>(Integer.valueOf(7));
        String json = MAPPER.writeValueAsString(list);
        assertEquals("{\"wrapped\":{\"value\":7}}", json);
    }

// com.fasterxml.jackson.databind.ser.TestGenericTypes::testRootTypeForCollections727
    public void testRootTypeForCollections727() throws Exception
    {
        List<Base727> input = new ArrayList<Base727>();
        input.add(new Impl727(1, 2));

        final String EXP = aposToQuotes("[{'a':1,'b':2}]");
        
        assertEquals(EXP, MAPPER.writeValueAsString(input));
        assertEquals(EXP, MAPPER.writer().writeValueAsString(input));

        
        TypeReference<?> typeRef = new TypeReference<List<Base727>>() { };
        assertEquals(EXP, MAPPER.writer().forType(typeRef).writeValueAsString(input));
    }

// com.fasterxml.jackson.databind.ser.TestIterable::testIterator
    public void testIterator() throws IOException
    {
        StringWriter sw = new StringWriter();
        ArrayList<Integer> l = new ArrayList<Integer>();
        l.add(1);
        l.add(-9);
        l.add(0);
        MAPPER.writeValue(sw, l.iterator());
        assertEquals("[1,-9,0]", sw.toString().trim());
    }

// com.fasterxml.jackson.databind.ser.TestIterable::testIterable
    public void testIterable() throws IOException
    {
        StringWriter sw = new StringWriter();
        MAPPER.writeValue(sw, new IterableWrapper(new int[] { 1, 2, 3 }));
        assertEquals("[1,2,3]", sw.toString().trim());
    }

// com.fasterxml.jackson.databind.ser.TestIterable::testWithIterable
    public void testWithIterable() throws IOException
    {
        
        assertEquals("{\"values\":[\"value\"]}",
                MAPPER.writeValueAsString(new BeanWithIterable()));
        
        assertEquals("[1,2,3]",
                MAPPER.writeValueAsString(new IntIterable()));
    }

// com.fasterxml.jackson.databind.ser.TestIterable::testIterable358
    public void testIterable358() throws Exception {
        String json = MAPPER.writeValueAsString(new B());
        assertEquals("{\"list\":[[\"Hello world.\"]]}", json);
    }

// com.fasterxml.jackson.databind.ser.TestJacksonTypes::testLocation
    public void testLocation() throws IOException
    {
        File f = new File("/tmp/test.json");
        JsonLocation loc = new JsonLocation(f, -1, 100, 13);
        ObjectMapper mapper = new ObjectMapper();
        Map<String,Object> result = writeAndMap(mapper, loc);
        assertEquals(5, result.size());
        assertEquals(f.getAbsolutePath(), result.get("sourceRef"));
        assertEquals(Integer.valueOf(-1), result.get("charOffset"));
        assertEquals(Integer.valueOf(-1), result.get("byteOffset"));
        assertEquals(Integer.valueOf(100), result.get("lineNr"));
        assertEquals(Integer.valueOf(13), result.get("columnNr"));

    }

// com.fasterxml.jackson.databind.ser.TestJacksonTypes::testTokenBuffer
    public void testTokenBuffer() {}

// com.fasterxml.jackson.databind.ser.TestJdkTypes::testBigDecimal
    public void testBigDecimal() throws Exception
    {
        Map<String, Object> map = new HashMap<String, Object>();
        String PI_STR = "3.14159265";
        map.put("pi", new BigDecimal(PI_STR));
        String str = MAPPER.writeValueAsString(map);
        assertEquals("{\"pi\":3.14159265}", str);
    }

// com.fasterxml.jackson.databind.ser.TestJdkTypes::testBigDecimalAsPlainString
    public void testBigDecimalAsPlainString() throws Exception
    {
        final ObjectMapper mapper = new ObjectMapper();

        mapper.enable(JsonGenerator.Feature.WRITE_BIGDECIMAL_AS_PLAIN);
        Map<String, Object> map = new HashMap<String, Object>();
        String PI_STR = "3.00000000";
        map.put("pi", new BigDecimal(PI_STR));
        String str = mapper.writeValueAsString(map);
        assertEquals("{\"pi\":3.00000000}", str);
    }

// com.fasterxml.jackson.databind.ser.TestJdkTypes::testFile
    public void testFile() throws IOException
    {
        
        File f = new File(new File("/tmp"), "foo.text");
        String str = MAPPER.writeValueAsString(f);
        
        String escapedAbsPath = f.getAbsolutePath().replaceAll("\\\\", "\\\\\\\\"); 
        assertEquals(quote(escapedAbsPath), str);
    }

// com.fasterxml.jackson.databind.ser.TestJdkTypes::testRegexps
    public void testRegexps() throws IOException
    {
        final String PATTERN_STR = "\\s+([a-b]+)\\w?";
        Pattern p = Pattern.compile(PATTERN_STR);
        Map<String,Object> input = new HashMap<String,Object>();
        input.put("p", p);
        Map<String,Object> result = writeAndMap(MAPPER, input);
        assertEquals(p.pattern(), result.get("p"));
    }

// com.fasterxml.jackson.databind.ser.TestJdkTypes::testCurrency
    public void testCurrency() throws IOException
    {
        Currency usd = Currency.getInstance("USD");
        assertEquals(quote("USD"), MAPPER.writeValueAsString(usd));
    }

// com.fasterxml.jackson.databind.ser.TestJdkTypes::testLocale
    public void testLocale() throws IOException
    {
        assertEquals(quote("en"), MAPPER.writeValueAsString(new Locale("en")));
        assertEquals(quote("es_ES"), MAPPER.writeValueAsString(new Locale("es", "ES")));
        assertEquals(quote("fi_FI_savo"), MAPPER.writeValueAsString(new Locale("FI", "fi", "savo")));

        
        assertEquals(quote(""), MAPPER.writeValueAsString(Locale.ROOT));
    }

// com.fasterxml.jackson.databind.ser.TestJdkTypes::testInetAddress
    public void testInetAddress() throws IOException
    {
        assertEquals(quote("127.0.0.1"), MAPPER.writeValueAsString(InetAddress.getByName("127.0.0.1")));
        assertEquals(quote("ning.com"), MAPPER.writeValueAsString(InetAddress.getByName("ning.com")));
    }

// com.fasterxml.jackson.databind.ser.TestJdkTypes::testInetSocketAddress
    public void testInetSocketAddress() throws IOException
    {
        assertEquals(
                quote("127.0.0.1:8080"),
                MAPPER.writeValueAsString(new InetSocketAddress("127.0.0.1", 8080)));
        assertEquals(
                quote("ning.com:6667"),
                MAPPER.writeValueAsString(new InetSocketAddress("ning.com", 6667)));
        assertEquals(
                quote("[2001:db8:85a3:8d3:1319:8a2e:370:7348]:443"),
                MAPPER.writeValueAsString(new InetSocketAddress("2001:db8:85a3:8d3:1319:8a2e:370:7348", 443)));
    }

// com.fasterxml.jackson.databind.ser.TestJdkTypes::testClass
    public void testClass() throws IOException
    {
        assertEquals(quote("java.lang.String"), MAPPER.writeValueAsString(String.class));
        assertEquals(quote("int"), MAPPER.writeValueAsString(Integer.TYPE));
        assertEquals(quote("boolean"), MAPPER.writeValueAsString(Boolean.TYPE));
        assertEquals(quote("void"), MAPPER.writeValueAsString(Void.TYPE));
    }

// com.fasterxml.jackson.databind.ser.TestJdkTypes::testCharset
    public void testCharset() throws IOException
    {
        assertEquals(quote("UTF-8"), MAPPER.writeValueAsString(Charset.forName("UTF-8")));
    }

// com.fasterxml.jackson.databind.ser.TestJdkTypes::testByteBuffer
    public void testByteBuffer() throws IOException
    {
        final byte[] INPUT_BYTES = new byte[] { 1, 2, 3, 4, 5 };
        String exp = MAPPER.writeValueAsString(INPUT_BYTES);
        ByteBuffer bbuf = ByteBuffer.wrap(INPUT_BYTES);
        assertEquals(exp, MAPPER.writeValueAsString(bbuf));

        
        ByteBuffer bbuf2 = ByteBuffer.allocateDirect(5);
        bbuf2.put(INPUT_BYTES);
        assertEquals(exp, MAPPER.writeValueAsString(bbuf2));
    }

// com.fasterxml.jackson.databind.ser.TestJdkTypes::testUUIDs
    public void testUUIDs() throws IOException
    {
        
        for (String value : new String[] {
                "76e6d183-5f68-4afa-b94a-922c1fdb83f8",
                "540a88d1-e2d8-4fb1-9396-9212280d0a7f",
                "2c9e441d-1cd0-472d-9bab-69838f877574",
                "591b2869-146e-41d7-8048-e8131f1fdec5",
                "82994ac2-7b23-49f2-8cc5-e24cf6ed77be",
                "00000007-0000-0000-0000-000000000000"
        }) {
            UUID uuid = UUID.fromString(value);
            String json = MAPPER.writeValueAsString(uuid);
            assertEquals(quote(uuid.toString()), json);

            
            String str = MAPPER.convertValue(uuid, String.class);
            assertEquals(value, str);
        }
        
        
        
        final String TEMPL = "00000000-0000-0000-0000-000000000000";
        final String chars = "123456789abcdef";

        for (int i = 0; i < chars.length(); ++i) {
            String value = TEMPL.replace('0', chars.charAt(i));
            UUID uuid = UUID.fromString(value);
            String json = MAPPER.writeValueAsString(uuid);
            assertEquals(quote(uuid.toString()), json);
        }
    }

// com.fasterxml.jackson.databind.ser.TestJsonSerialize::testSimpleValueDefinition
    public void testSimpleValueDefinition() throws Exception
    {
        Map<String,Object> result = writeAndMap(MAPPER, new WrapperClassForAs());
        assertEquals(1, result.size());
        Object ob = result.get("value");
        
        result = (Map<String,Object>) ob;
        assertEquals(1, result.size());
        assertEquals(Integer.valueOf(3), result.get("x"));
    }

// com.fasterxml.jackson.databind.ser.TestJsonSerialize::testBrokenAnnotation
    public void testBrokenAnnotation() throws Exception
    {
        try {
            serializeAsString(MAPPER, new BrokenClass());
        } catch (Exception e) {
            verifyException(e, "types not related");
        }
    }

// com.fasterxml.jackson.databind.ser.TestJsonSerialize::testStaticTypingForClass
    public void testStaticTypingForClass() throws Exception
    {
        Map<String,Object> result = writeAndMap(MAPPER, new WrapperClassForStaticTyping());
        assertEquals(1, result.size());
        Object ob = result.get("value");
        
        result = (Map<String,Object>) ob;
        assertEquals(1, result.size());
        assertEquals(Integer.valueOf(3), result.get("x"));
    }

// com.fasterxml.jackson.databind.ser.TestJsonSerialize::testMixedTypingForClass
    public void testMixedTypingForClass() throws Exception
    {
        Map<String,Object> result = writeAndMap(MAPPER, new WrapperClassForStaticTyping2());
        assertEquals(2, result.size());

        Object obStatic = result.get("staticValue");
        
        Map<String,Object> stat = (Map<String,Object>) obStatic;
        assertEquals(1, stat.size());
        assertEquals(Integer.valueOf(3), stat.get("x"));

        Object obDynamic = result.get("dynamicValue");
        
        Map<String,Object> dyn = (Map<String,Object>) obDynamic;
        assertEquals(2, dyn.size());
        assertEquals(Integer.valueOf(3), dyn.get("x"));
        assertEquals(Integer.valueOf(5), dyn.get("y"));
    }

// com.fasterxml.jackson.databind.ser.TestJsonSerialize::testStaticTypingWithMap
    public void testStaticTypingWithMap() throws Exception
    {
        ObjectMapper m = new ObjectMapper();
        m.configure(MapperFeature.USE_STATIC_TYPING, true);
        ValueMap map = new ValueMap();
        map.put("a", new ValueClass());
        assertEquals("{\"a\":{\"x\":3}}", serializeAsString(m, map));
    }

// com.fasterxml.jackson.databind.ser.TestJsonSerialize::testStaticTypingWithArrayList
    public void testStaticTypingWithArrayList() throws Exception
    {
        ObjectMapper m = new ObjectMapper();
        m.configure(MapperFeature.USE_STATIC_TYPING, true);
        ValueList list = new ValueList();
        list.add(new ValueClass());
        assertEquals("[{\"x\":3}]", m.writeValueAsString(list));
    }

// com.fasterxml.jackson.databind.ser.TestJsonSerialize::testStaticTypingWithLinkedList
    public void testStaticTypingWithLinkedList() throws Exception
    {
        ObjectMapper m = new ObjectMapper();
        m.configure(MapperFeature.USE_STATIC_TYPING, true);
        ValueLinkedList list = new ValueLinkedList();
        list.add(new ValueClass());
        assertEquals("[{\"x\":3}]", serializeAsString(m, list));
    }

// com.fasterxml.jackson.databind.ser.TestJsonSerialize::testStaticTypingWithArray
    public void testStaticTypingWithArray() throws Exception
    {
        ObjectMapper m = new ObjectMapper();
        m.configure(MapperFeature.USE_STATIC_TYPING, true);
        ValueInterface[] array = new ValueInterface[] { new ValueClass() };
        assertEquals("[{\"x\":3}]", serializeAsString(m, array));
    }

// com.fasterxml.jackson.databind.ser.TestJsonSerialize::testIssue294
    public void testIssue294() throws Exception
    {
        assertEquals("{\"id\":\"fooId\",\"bar\":\"barId\"}",
                MAPPER.writeValueAsString(new Foo294("fooId", "barId")));
    }

// com.fasterxml.jackson.databind.ser.TestJsonSerialize::testWithIsGetter
    public void testWithIsGetter() throws Exception
    {
        ObjectMapper m = new ObjectMapper();
        m.setVisibility(PropertyAccessor.GETTER, Visibility.NONE)
        .setVisibility(PropertyAccessor.FIELD, Visibility.ANY)
        .setVisibility(PropertyAccessor.CREATOR, Visibility.NONE)
        .setVisibility(PropertyAccessor.IS_GETTER, Visibility.NONE)
        .setVisibility(PropertyAccessor.SETTER, Visibility.NONE);        
        final String JSON = m.writeValueAsString(new Response());
        assertEquals(aposToQuotes("{'a':'x','something':true}"), JSON);
    }

// com.fasterxml.jackson.databind.ser.TestJsonSerialize2::testSerializedAsListWithClassAnnotations
    public void testSerializedAsListWithClassAnnotations() throws IOException
    {
        SimpleValueList list = new SimpleValueList();
        list.add(new ActualValue("foo"));
        assertEquals("[{\"value\":\"foo\"}]", MAPPER.writeValueAsString(list));
    }

// com.fasterxml.jackson.databind.ser.TestJsonSerialize2::testSerializedAsMapWithClassAnnotations
    public void testSerializedAsMapWithClassAnnotations() throws IOException
    {
        SimpleValueMap map = new SimpleValueMap();
        map.put(new SimpleKey("x"), new ActualValue("y"));
        assertEquals("{\"toString:x\":{\"value\":\"y\"}}", MAPPER.writeValueAsString(map));
    }

// com.fasterxml.jackson.databind.ser.TestJsonSerialize2::testSerializedAsListWithClassSerializer
    public void testSerializedAsListWithClassSerializer() throws IOException
    {
        ObjectMapper m = new ObjectMapper();
        SimpleValueListWithSerializer list = new SimpleValueListWithSerializer();
        list.add(new ActualValue("foo"));
        assertEquals("[\"value foo\"]", m.writeValueAsString(list));
    }

// com.fasterxml.jackson.databind.ser.TestJsonSerialize2::testSerializedAsListWithPropertyAnnotations
    public void testSerializedAsListWithPropertyAnnotations() throws IOException
    {
        ListWrapperSimple input = new ListWrapperSimple("bar");
        assertEquals("{\"values\":[{\"value\":\"bar\"}]}", MAPPER.writeValueAsString(input));
    }

// com.fasterxml.jackson.databind.ser.TestJsonSerialize2::testSerializedAsMapWithClassSerializer
    public void testSerializedAsMapWithClassSerializer() throws IOException
    {
        SimpleValueMapWithSerializer map = new SimpleValueMapWithSerializer();
        map.put(new SimpleKey("abc"), new ActualValue("123"));
        assertEquals("{\"key abc\":\"value 123\"}", MAPPER.writeValueAsString(map));
    }

// com.fasterxml.jackson.databind.ser.TestJsonSerialize2::testSerializedAsMapWithPropertyAnnotations
    public void testSerializedAsMapWithPropertyAnnotations() throws IOException
    {
        MapWrapperSimple input = new MapWrapperSimple("a", "b");
        assertEquals("{\"values\":{\"toString:a\":{\"value\":\"b\"}}}",
                MAPPER.writeValueAsString(input));
    }

// com.fasterxml.jackson.databind.ser.TestJsonSerialize2::testSerializedAsListWithPropertyAnnotations2
    public void testSerializedAsListWithPropertyAnnotations2() throws IOException
    {
        ListWrapperWithSerializer input = new ListWrapperWithSerializer("abc");
        assertEquals("{\"values\":[\"value abc\"]}", MAPPER.writeValueAsString(input));
    }

// com.fasterxml.jackson.databind.ser.TestJsonSerialize2::testSerializedAsMapWithPropertyAnnotations2
    public void testSerializedAsMapWithPropertyAnnotations2() throws IOException
    {
        MapWrapperWithSerializer input = new MapWrapperWithSerializer("foo", "b");
        assertEquals("{\"values\":{\"key foo\":\"value b\"}}", MAPPER.writeValueAsString(input));
    }

// com.fasterxml.jackson.databind.ser.TestJsonSerialize2::testEmptyInclusionContainers
    public void testEmptyInclusionContainers() throws IOException
    {
        ObjectMapper defMapper = MAPPER;
        ObjectMapper inclMapper = new ObjectMapper().setSerializationInclusion(JsonInclude.Include.NON_EMPTY);

        ListWrapper<String> list = new ListWrapper<String>();
        assertEquals("{\"list\":[]}", defMapper.writeValueAsString(list));
        assertEquals("{}", inclMapper.writeValueAsString(list));
        assertEquals("{}", inclMapper.writeValueAsString(new ListWrapper<String>()));

        MapWrapper<String,Integer> map = new MapWrapper<String,Integer>(new HashMap<String,Integer>());
        assertEquals("{\"map\":{}}", defMapper.writeValueAsString(map));
        assertEquals("{}", inclMapper.writeValueAsString(map));
        assertEquals("{}", inclMapper.writeValueAsString(new MapWrapper<String,Integer>(null)));

        ArrayWrapper<Integer> array = new ArrayWrapper<Integer>(new Integer[0]);
        assertEquals("{\"array\":[]}", defMapper.writeValueAsString(array));
        assertEquals("{}", inclMapper.writeValueAsString(array));
        assertEquals("{}", inclMapper.writeValueAsString(new ArrayWrapper<Integer>(null)));
    }

// com.fasterxml.jackson.databind.ser.TestJsonSerialize2::testNullSerializer
    public void testNullSerializer() throws Exception
    {
        String json = MAPPER.writeValueAsString(new NullBean());
        assertEquals("{\"value\":null}", json);
    }

// com.fasterxml.jackson.databind.ser.TestJsonSerialize3::testCustomContentSerializer
    public void testCustomContentSerializer() throws Exception
    {
        ObjectMapper m = new ObjectMapper();
        MyObject object = new MyObject();
        object.list = Arrays.asList("foo");
        String json = m.writeValueAsString(object);
        assertEquals("{\"list\":[\"bar\"]}", json);
    }

// com.fasterxml.jackson.databind.ser.TestJsonSerializeAs::testSerializeAsInClass
    public void testSerializeAsInClass() throws IOException {
        assertEquals("{\"foo\":42}", WRITER.writeValueAsString(new FooImpl()));
    }

// com.fasterxml.jackson.databind.ser.TestJsonSerializeAs::testSerializeAsForArrayProp
    public void testSerializeAsForArrayProp() throws IOException {
        assertEquals("{\"foos\":[{\"foo\":42}]}",
                WRITER.writeValueAsString(new Fooables()));
    }

// com.fasterxml.jackson.databind.ser.TestJsonSerializeAs::testSerializeAsForSimpleProp
    public void testSerializeAsForSimpleProp() throws IOException {
        assertEquals("{\"foo\":{\"foo\":42}}",
                WRITER.writeValueAsString(new FooableWrapper()));
    }

// com.fasterxml.jackson.databind.ser.TestJsonSerializeAs::testSerializeWithFieldAnno
    public void testSerializeWithFieldAnno() throws IOException {
        assertEquals("{\"foo\":{\"foo\":42}}",
                WRITER.writeValueAsString(new FooableWithFieldWrapper()));
    }

// com.fasterxml.jackson.databind.ser.TestJsonSerializeAs::testSpecializedContentAs
    public void testSpecializedContentAs() throws IOException {
        assertEquals(aposToQuotes("{'values':[{'a':1,'b':2}]}"),
                WRITER.writeValueAsString(new Bean1178Wrapper(1)));
    }

// com.fasterxml.jackson.databind.ser.TestJsonSerializeAs::testSpecializedAsIntermediate
    public void testSpecializedAsIntermediate() throws IOException {
        assertEquals(aposToQuotes("{'value':{'a':1,'b':2}}"),
                WRITER.writeValueAsString(new Bean1178Holder()));
    }

// com.fasterxml.jackson.databind.ser.TestJsonValue::testSimpleJsonValue
    public void testSimpleJsonValue() throws Exception
    {
        String result = MAPPER.writeValueAsString(new ValueClass<String>("abc"));
        assertEquals("\"abc\"", result);
    }

// com.fasterxml.jackson.databind.ser.TestJsonValue::testJsonValueWithUseSerializer
    public void testJsonValueWithUseSerializer() throws Exception
    {
        String result = serializeAsString(MAPPER, new ToStringValueClass<Integer>(Integer.valueOf(123)));
        assertEquals("\"123\"", result);
    }

// com.fasterxml.jackson.databind.ser.TestJsonValue::testMixedJsonValue
    public void testMixedJsonValue() throws Exception
    {
        String result = serializeAsString(MAPPER, new ToStringValueClass2("xyz"));
        assertEquals("\"xyz\"", result);
    }

// com.fasterxml.jackson.databind.ser.TestJsonValue::testValueWithStaticType
    public void testValueWithStaticType() throws Exception
    {
        
        assertEquals("{\"a\":\"a\",\"b\":\"b\"}", MAPPER.writeValueAsString(new ValueWrapper()));

        
        ObjectMapper staticMapper = new ObjectMapper();
        staticMapper.configure(MapperFeature.USE_STATIC_TYPING, true);
        assertEquals("{\"a\":\"a\"}", staticMapper.writeValueAsString(new ValueWrapper()));
    }

// com.fasterxml.jackson.databind.ser.TestJsonValue::testMapWithJsonValue
    public void testMapWithJsonValue() throws Exception {
        assertEquals("{\"a\":\"1\"}", MAPPER.writeValueAsString(new MapBean()));
    }

// com.fasterxml.jackson.databind.ser.TestJsonValue::testWithMap
    public void testWithMap() throws Exception {
        assertEquals("42", MAPPER.writeValueAsString(new MapAsNumber()));

    }

// com.fasterxml.jackson.databind.ser.TestJsonValue::testWithList
    public void testWithList() throws Exception {
        assertEquals("13", MAPPER.writeValueAsString(new ListAsNumber()));
    }

// com.fasterxml.jackson.databind.ser.TestJsonValue::testInList
    public void testInList() throws Exception {
        IntExtBean bean = new IntExtBean();
        bean.add(1);
        bean.add(2);
        String json = MAPPER.writeValueAsString(bean);
        assertEquals(json, "{\"values\":[{\"i\":1},{\"i\":2}]}");
    }

// com.fasterxml.jackson.databind.ser.TestJsonValue::testPolymorphicSerdeWithDelegate
    public void testPolymorphicSerdeWithDelegate() throws Exception
    {
	    AdditionInterface adder = new AdditionInterfaceImpl(1);
	
	    assertEquals(2, adder.add(1));
	    String json = MAPPER.writeValueAsString(adder);
	    assertEquals("{\"boingo\":\"boopsy\",\"toAdd\":1}", json);
	    assertEquals(2, MAPPER.readValue(json, AdditionInterface.class).add(1));
	
	    adder = new NegatingAdditionInterface(adder);
	    assertEquals(0, adder.add(1));
	    json = MAPPER.writeValueAsString(adder);
	    
	    assertEquals("{\"boingo\":\"boopsy\",\"toAdd\":1}", json);
	    assertEquals(2, MAPPER.readValue(json, AdditionInterface.class).add(1));
    }

// com.fasterxml.jackson.databind.ser.TestJsonValue::testJsonValueWithCustomOverride
    public void testJsonValueWithCustomOverride() throws Exception
    {
        final Bean838 INPUT = new Bean838();

        
        assertEquals(quote("value"), MAPPER.writeValueAsString(INPUT));

        
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new SimpleModule()
            .addSerializer(Bean838.class, new Bean838Serializer())
            );
        assertEquals("42", mapper.writeValueAsString(INPUT));
    }

// com.fasterxml.jackson.databind.ser.TestKeySerializers::testNotKarl
    public void testNotKarl() throws IOException {
        final String serialized = MAPPER.writeValueAsString(new NotKarlBean());
        assertEquals("{\"map\":{\"Not Karl\":1}}", serialized);
    }

// com.fasterxml.jackson.databind.ser.TestKeySerializers::testKarl
    public void testKarl() throws IOException {
        final String serialized = MAPPER.writeValueAsString(new KarlBean());
        assertEquals("{\"map\":{\"Karl\":1}}", serialized);
    }

// com.fasterxml.jackson.databind.ser.TestKeySerializers::testBoth
    public void testBoth() throws IOException
    {
        
        final ObjectMapper mapper = new ObjectMapper();
        final String value1 = mapper.writeValueAsString(new NotKarlBean());
        assertEquals("{\"map\":{\"Not Karl\":1}}", value1);
        final String value2 = mapper.writeValueAsString(new KarlBean());
        assertEquals("{\"map\":{\"Karl\":1}}", value2);
    }

// com.fasterxml.jackson.databind.ser.TestKeySerializers::testCustomForEnum
    public void testCustomForEnum() throws IOException
    {
        
        final ObjectMapper mapper = new ObjectMapper();
        SimpleModule mod = new SimpleModule("test");
        mod.addKeySerializer(ABC.class, new ABCKeySerializer());
        mapper.registerModule(mod);

        String json = mapper.writeValueAsString(new ABCMapWrapper());
        assertEquals("{\"stuff\":{\"xxxB\":\"bar\"}}", json);
    }

// com.fasterxml.jackson.databind.ser.TestKeySerializers::testCustomEnumInnerMapKey
    public void testCustomEnumInnerMapKey() throws Exception {
        Map<Outer, Object> outerMap = new HashMap<Outer, Object>();
        Map<ABC, Map<String, String>> map = new EnumMap<ABC, Map<String, String>>(ABC.class);
        Map<String, String> innerMap = new HashMap<String, String>();
        innerMap.put("one", "1");
        map.put(ABC.A, innerMap);
        outerMap.put(Outer.inner, map);
        final ObjectMapper mapper = new ObjectMapper();
        SimpleModule mod = new SimpleModule("test");
        mod.setMixInAnnotation(ABC.class, ABCMixin.class);
        mod.addKeySerializer(ABC.class, new ABCKeySerializer());
        mapper.registerModule(mod);

        JsonNode tree = mapper.convertValue(outerMap, JsonNode.class);

        JsonNode innerNode = tree.get("inner");
        String key = innerNode.fieldNames().next();
        assertEquals("xxxA", key);
    }

// com.fasterxml.jackson.databind.ser.TestKeySerializers::testUnWrappedMapWithDefaultType
    public void testUnWrappedMapWithDefaultType() throws Exception{
        final ObjectMapper mapper = new ObjectMapper();
        SimpleModule mod = new SimpleModule("test");
        mod.addKeySerializer(ABC.class, new ABCKeySerializer());
        mapper.registerModule(mod);

        TypeResolverBuilder<?> typer = new ObjectMapper.DefaultTypeResolverBuilder(ObjectMapper.DefaultTyping.NON_FINAL);
        typer = typer.init(JsonTypeInfo.Id.NAME, null);
        typer = typer.inclusion(JsonTypeInfo.As.PROPERTY);
        
        typer = typer.typeIdVisibility(true);
        mapper.setDefaultTyping(typer);

        Map<ABC,String> stuff = new HashMap<ABC,String>();
        stuff.put(ABC.B, "bar");
        String json = mapper.writerFor(new TypeReference<Map<ABC, String>>() {})
                .writeValueAsString(stuff);
        assertEquals("{\"@type\":\"HashMap\",\"xxxB\":\"bar\"}", json);
    }

// com.fasterxml.jackson.databind.ser.TestKeySerializers::testUnWrappedMapWithKeySerializer
    public void testUnWrappedMapWithKeySerializer() throws Exception{
        SimpleModule mod = new SimpleModule("test");
        mod.addKeySerializer(ABC.class, new ABCKeySerializer());
        final ObjectMapper mapper = new ObjectMapper()
            .registerModule(mod)
            .enable(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT)
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            .disable(SerializationFeature.WRITE_NULL_MAP_VALUES)
            .setSerializationInclusion(JsonInclude.Include.NON_EMPTY)
            ;

        Map<ABC,BAR<?>> stuff = new HashMap<ABC,BAR<?>>();
        stuff.put(ABC.B, new BAR<String>("bar"));
        String json = mapper.writerFor(new TypeReference<Map<ABC,BAR<?>>>() {})
                .writeValueAsString(stuff);
        assertEquals("{\"xxxB\":\"bar\"}", json);
    }

// com.fasterxml.jackson.databind.ser.TestKeySerializers::testDynamicMapKeys
    public void testDynamicMapKeys() throws Exception
    {
        Map<Object,Integer> stuff = new LinkedHashMap<Object,Integer>();
        stuff.put(AbcLC.B, Integer.valueOf(3));
        stuff.put(new UCString("foo"), Integer.valueOf(4));
        String json = MAPPER.writeValueAsString(stuff);
        assertEquals(aposToQuotes("{'b':3,'FOO':4}"), json);
    }

// com.fasterxml.jackson.databind.ser.TestMapSerialization::testUsingObjectWriter
    public void testUsingObjectWriter() throws IOException
    {
        ObjectWriter w = MAPPER.writerFor(Object.class);
        Map<String,Object> map = new LinkedHashMap<String,Object>();
        map.put("a", 1);
        String json = w.writeValueAsString(map);
        assertEquals(aposToQuotes("{'a':1}"), json);
    }

// com.fasterxml.jackson.databind.ser.TestMapSerialization::testMapSerializer
    public void testMapSerializer() throws IOException
    {
        assertEquals("\"{a=b, c=d}\"", MAPPER.writeValueAsString(new PseudoMap("a", "b", "c", "d")));
    }

// com.fasterxml.jackson.databind.ser.TestMapSerialization::testMapKeyValueSerialization
    public void testMapKeyValueSerialization() throws IOException
    {
        Map<String,String> map = new HashMap<String,String>();
        map.put("a", "b");
        assertEquals("[\"a\"]", MAPPER.writeValueAsString(map.keySet()));
        assertEquals("[\"b\"]", MAPPER.writeValueAsString(map.values()));

        
        map = new TreeMap<String,String>();
        map.put("c", "d");
        assertEquals("[\"c\"]", MAPPER.writeValueAsString(map.keySet()));
        assertEquals("[\"d\"]", MAPPER.writeValueAsString(map.values()));

        
        map = new ConcurrentHashMap<String,String>();
        map.put("e", "f");
        assertEquals("[\"e\"]", MAPPER.writeValueAsString(map.keySet()));
        assertEquals("[\"f\"]", MAPPER.writeValueAsString(map.values()));
    }

// com.fasterxml.jackson.databind.ser.TestMapSerialization::testDefaultKeySerializer
    public void testDefaultKeySerializer() throws IOException
    {
        ObjectMapper m = new ObjectMapper();
        m.getSerializerProvider().setDefaultKeySerializer(new DefaultKeySerializer());
        Map<String,String> map = new HashMap<String,String>();
        map.put("a", "b");
        assertEquals("{\"DEFAULT:a\":\"b\"}", m.writeValueAsString(map));
    }

// com.fasterxml.jackson.databind.ser.TestMapSerialization::testOrderByKey
    public void testOrderByKey() throws IOException
    {
        ObjectMapper m = new ObjectMapper();
        assertFalse(m.isEnabled(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS));
        LinkedHashMap<String,Integer> map = new LinkedHashMap<String,Integer>();
        map.put("b", 3);
        map.put("a", 6);
        
        assertEquals("{\"b\":3,\"a\":6}", m.writeValueAsString(map));
        
        assertEquals("{\"a\":6,\"b\":3}", m.writer(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS).writeValueAsString(map));
    }

// com.fasterxml.jackson.databind.ser.TestMapSerialization::testOrderByKeyViaProperty
    public void testOrderByKeyViaProperty() throws IOException
    {
        MapOrderingBean input = new MapOrderingBean("c", "b", "a");
        String json = MAPPER.writeValueAsString(input);
        assertEquals(aposToQuotes("{'map':{'a':3,'b':2,'c':1}}"), json);
    }

// com.fasterxml.jackson.databind.ser.TestMapSerialization::testMapEntry
    public void testMapEntry() throws IOException
    {
        StringIntMapEntry input = new StringIntMapEntry("answer", 42);
        String json = MAPPER.writeValueAsString(input);
        assertEquals(aposToQuotes("{'answer':42}"), json);

        StringIntMapEntry[] array = new StringIntMapEntry[] { input };
        json = MAPPER.writeValueAsString(array);
        assertEquals(aposToQuotes("[{'answer':42}]"), json);

        
        ObjectMapper mapper = new ObjectMapper().enableDefaultTyping(DefaultTyping.NON_FINAL);
        json = mapper.writeValueAsString(input);
        assertEquals(aposToQuotes("['"+StringIntMapEntry.class.getName()+"',{'answer':42}]"),
                json);
    }

// com.fasterxml.jackson.databind.ser.TestMapSerialization::testMapEntryWrapper
    public void testMapEntryWrapper() throws IOException
    {
        StringIntMapEntryWrapper input = new StringIntMapEntryWrapper("answer", 42);
        String json = MAPPER.writeValueAsString(input);
        assertEquals(aposToQuotes("{'value':{'answer':42}}"), json);
    }

// com.fasterxml.jackson.databind.ser.TestMapSerialization::testMapJsonValueKey47
    public void testMapJsonValueKey47() throws Exception
    {
        WatMap input = new WatMap();
        input.put(new Wat("3"), true);

        ObjectMapper mapper = new ObjectMapper();
        String json = mapper.writeValueAsString(input);
        assertEquals(aposToQuotes("{'3':true}"), json);
    }

// com.fasterxml.jackson.databind.ser.TestMapSerialization::testClassKey
    public void testClassKey() throws IOException
    {
        Map<Class<?>,Integer> map = new LinkedHashMap<Class<?>,Integer>();
        map.put(String.class, 2);
        String json = MAPPER.writeValueAsString(map);
        assertEquals(aposToQuotes("{'java.lang.String':2}"), json);
    }

// com.fasterxml.jackson.databind.ser.TestMapSerialization::testNullJsonMapping691
    public void testNullJsonMapping691() throws Exception
    {
        MapWithTypedValues input = new MapWithTypedValues();
        input.put("id", "Test");
        input.put("NULL", null);

        String json = MAPPER.writeValueAsString(input);

        assertEquals(aposToQuotes("{'@type':'mymap','id':'Test','NULL':null}"),
                json);
    }

// com.fasterxml.jackson.databind.ser.TestMapSerialization::testNullJsonInTypedMap691
    public void testNullJsonInTypedMap691() throws Exception {
        Map<String, String> map = new HashMap<String, String>();
        map.put("NULL", null);
    
        ObjectMapper mapper = new ObjectMapper();
        mapper.addMixIn(Object.class, Mixin691.class);
        String json = mapper.writeValueAsString(map);
        assertEquals("{\"@class\":\"java.util.HashMap\",\"NULL\":null}", json);
    }

// com.fasterxml.jackson.databind.ser.TestRootType::testSuperClass
    public void testSuperClass() throws Exception
    {
        ObjectMapper mapper = objectMapper();
        SubType bean = new SubType();

        
        Map<String,Object> result = writeAndMap(mapper, bean);
        assertEquals(4, result.size());
        assertEquals("a", result.get("a"));
        assertEquals(Integer.valueOf(3), result.get("b"));
        assertEquals("x", result.get("a2"));
        assertEquals(Boolean.TRUE, result.get("b2"));

        
        ObjectWriter w = mapper.writerFor(BaseType.class);
        String json = w.writeValueAsString(bean);
        result = (Map<String,Object>)mapper.readValue(json, Map.class);
        assertEquals(2, result.size());
        assertEquals("a", result.get("a"));
        assertEquals(Integer.valueOf(3), result.get("b"));
    }

// com.fasterxml.jackson.databind.ser.TestRootType::testSuperInterface
    public void testSuperInterface() throws Exception
    {
        ObjectMapper mapper = objectMapper();
        SubType bean = new SubType();

        
        ObjectWriter w = mapper.writerFor(BaseInterface.class);
        String json = w.writeValueAsString(bean);
        @SuppressWarnings("unchecked")
        Map<String,Object> result = mapper.readValue(json, Map.class);
        assertEquals(1, result.size());
        assertEquals(Integer.valueOf(3), result.get("b"));
    }

// com.fasterxml.jackson.databind.ser.TestRootType::testInArray
    public void testInArray() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        
        mapper.configure(MapperFeature.USE_STATIC_TYPING, true);
        SubType[] ob = new SubType[] { new SubType() };
        String json = mapper.writerFor(BaseInterface[].class).writeValueAsString(ob);
        
        assertEquals("[{\"b\":3}]", json);
    }

// com.fasterxml.jackson.databind.ser.TestRootType::testIncompatibleRootType
    public void testIncompatibleRootType() throws Exception
    {
        ObjectMapper mapper = objectMapper();
        SubType bean = new SubType();

        
        ObjectWriter w = mapper.writerFor(HashMap.class);
        try {
            w.writeValueAsString(bean);
            fail("Should have failed due to incompatible type");
        } catch (JsonProcessingException e) {
            verifyException(e, "Incompatible types");
        }
    }

// com.fasterxml.jackson.databind.ser.TestRootType::testJackson398
    public void testJackson398() throws Exception
    {
        ObjectMapper mapper = objectMapper();
        JavaType collectionType = TypeFactory.defaultInstance().constructCollectionType(ArrayList.class, BaseClass398.class);
        List<TestClass398> typedList = new ArrayList<TestClass398>();
        typedList.add(new TestClass398());

        final String EXP = "[{\"beanClass\":\"TestRootType$TestClass398\",\"property\":\"aa\"}]";
        
        
        String json = mapper.writerFor(collectionType).writeValueAsString(typedList);
        assertEquals(EXP, json);

        StringWriter out = new StringWriter();
        JsonFactory f = new JsonFactory();
        mapper.writerFor(collectionType).writeValue(f.createGenerator(out), typedList);

        assertEquals(EXP, out.toString());
    }

// com.fasterxml.jackson.databind.ser.TestRootType::testRootWrapping
    public void testRootWrapping() throws Exception
    {
        String json = WRAP_ROOT_MAPPER.writeValueAsString(new StringWrapper("abc"));
        assertEquals("{\"StringWrapper\":{\"str\":\"abc\"}}", json);
    }

// com.fasterxml.jackson.databind.ser.TestRootType::testIssue456WrapperPart
    public void testIssue456WrapperPart() throws Exception
    {
        ObjectMapper mapper = objectMapper();
        assertEquals("123", mapper.writerFor(Integer.TYPE).writeValueAsString(Integer.valueOf(123)));
        assertEquals("456", mapper.writerFor(Long.TYPE).writeValueAsString(Long.valueOf(456L)));
    }

// com.fasterxml.jackson.databind.ser.TestRootType::testRootNameAnnotation
    public void testRootNameAnnotation() throws Exception
    {
        String json = WRAP_ROOT_MAPPER.writeValueAsString(new WithRootName());
        assertEquals("{\"root\":{\"a\":3}}", json);
    }

// com.fasterxml.jackson.databind.ser.TestRootType::testRootNameWithExplicitType
    public void testRootNameWithExplicitType() throws Exception
    {
        TestCommandChild cmd = new TestCommandChild();
        cmd.uuid = "1234";
        cmd.type = 1;

        ObjectWriter writer = WRAP_ROOT_MAPPER.writerFor(TestCommandParent.class);
        String json =  writer.writeValueAsString(cmd);

        assertEquals(json, "{\"TestCommandParent\":{\"uuid\":\"1234\",\"type\":1}}");
    }

// com.fasterxml.jackson.databind.ser.TestSerializationOrder::testImplicitOrderByCreator
    public void testImplicitOrderByCreator() throws Exception
    {
        assertEquals("{\"c\":1,\"a\":2,\"b\":0}", MAPPER.writeValueAsString(new BeanWithCreator(1, 2)));
    }

// com.fasterxml.jackson.databind.ser.TestSerializationOrder::testExplicitOrder
    public void testExplicitOrder() throws Exception
    {
        assertEquals("{\"c\":3,\"a\":1,\"b\":2,\"d\":4}", MAPPER.writeValueAsString(new BeanWithOrder(1, 2, 3, 4)));
    }

// com.fasterxml.jackson.databind.ser.TestSerializationOrder::testAlphabeticOrder
    public void testAlphabeticOrder() throws Exception
    {
        assertEquals("{\"d\":4,\"a\":1,\"b\":2,\"c\":3}", MAPPER.writeValueAsString(new SubBeanWithOrder(1, 2, 3, 4)));
    }

// com.fasterxml.jackson.databind.ser.TestSerializationOrder::testOrderWithMixins
    public void testOrderWithMixins() throws Exception
    {
        ObjectMapper m = new ObjectMapper();
        m.addMixIn(BeanWithOrder.class, OrderMixIn.class);
        assertEquals("{\"b\":2,\"a\":1,\"c\":3,\"d\":4}",
                serializeAsString(m, new BeanWithOrder(1, 2, 3, 4)));
    }

// com.fasterxml.jackson.databind.ser.TestSerializationOrder::testOrderWrt268
    public void testOrderWrt268() throws Exception
    {
        assertEquals("{\"a\":\"a\",\"b\":\"b\",\"x\":\"x\",\"z\":\"z\"}",
                MAPPER.writeValueAsString(new BeanFor268()));
    }

// com.fasterxml.jackson.databind.ser.TestSerializationOrder::testOrderWithFeature
    public void testOrderWithFeature() throws Exception
    {
        ObjectMapper m = new ObjectMapper();
        m.configure(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY, true);
        assertEquals("{\"a\":1,\"b\":2,\"c\":3,\"d\":4}",
                m.writeValueAsString(new BeanFor459()));
    }

// com.fasterxml.jackson.databind.ser.TestSerializationOrder::testAlphaAndCreatorOrdering
    public void testAlphaAndCreatorOrdering() throws Exception
    {
        ObjectMapper m = new ObjectMapper();
        m.configure(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY, true);
        String json = m.writeValueAsString(new BeanForGH311(2, 1));
        assertEquals("{\"a\":1,\"b\":2}", json);
    }

// com.fasterxml.jackson.databind.ser.TestSerializerProvider::testFindExplicit
    public void testFindExplicit() throws JsonMappingException
    {
        ObjectMapper mapper = new ObjectMapper();
        SerializationConfig config = mapper.getSerializationConfig();
        SerializerFactory f = new BeanSerializerFactory(null);
        DefaultSerializerProvider prov = new DefaultSerializerProvider.Impl().createInstance(config, f);

        
        assertNotNull(prov.findKeySerializer(mapper.constructType(String.class), null));
        assertNotNull(prov.getDefaultNullKeySerializer());
        assertNotNull(prov.getDefaultNullValueSerializer());
        
        assertNotNull(prov.getUnknownTypeSerializer(getClass()));

        assertTrue(prov.createInstance(config, f).hasSerializerFor(String.class, null));
        
        assertTrue(prov.createInstance(config, f).hasSerializerFor(String.class, null));

        assertTrue(prov.createInstance(config, f).hasSerializerFor(MyBean.class, null));
        assertTrue(prov.createInstance(config, f).hasSerializerFor(MyBean.class, null));

        
        AtomicReference<Throwable> cause = new AtomicReference<Throwable>();
        assertFalse(prov.createInstance(config, f).hasSerializerFor(NoPropsBean.class, cause));
        Throwable t = cause.get();
        
        assertNull(t);
    }

// com.fasterxml.jackson.databind.ser.TestSimpleAtomicTypes::testAtomicBoolean
    public void testAtomicBoolean() throws Exception
    {
        assertEquals("true", MAPPER.writeValueAsString(new AtomicBoolean(true)));
        assertEquals("false", MAPPER.writeValueAsString(new AtomicBoolean(false)));
    }

// com.fasterxml.jackson.databind.ser.TestSimpleAtomicTypes::testAtomicInteger
    public void testAtomicInteger() throws Exception
    {
        assertEquals("1", MAPPER.writeValueAsString(new AtomicInteger(1)));
        assertEquals("-9", MAPPER.writeValueAsString(new AtomicInteger(-9)));
    }

// com.fasterxml.jackson.databind.ser.TestSimpleAtomicTypes::testAtomicLong
    public void testAtomicLong() throws Exception
    {
        assertEquals("0", MAPPER.writeValueAsString(new AtomicLong(0)));
    }

// com.fasterxml.jackson.databind.ser.TestSimpleAtomicTypes::testAtomicReference
    public void testAtomicReference() throws Exception
    {
        String[] strs = new String[] { "abc" };
        assertEquals("[\"abc\"]", MAPPER.writeValueAsString(new AtomicReference<String[]>(strs)));
    }

// com.fasterxml.jackson.databind.ser.TestSimpleAtomicTypes::testCustomSerializer
    public void testCustomSerializer() throws Exception
    {
        final String VALUE = "fooBAR";
        String json = MAPPER.writeValueAsString(new UCStringWrapper(VALUE));
        assertEquals(json, aposToQuotes("{'value':'FOOBAR'}"));
    }

// com.fasterxml.jackson.databind.ser.TestSimpleTypes::testBoolean
    public void testBoolean() throws Exception
    {
        assertEquals("true", serializeAsString(MAPPER, Boolean.TRUE));
        assertEquals("false", serializeAsString(MAPPER, Boolean.FALSE));
    }

// com.fasterxml.jackson.databind.ser.TestSimpleTypes::testBooleanArray
    public void testBooleanArray() throws Exception
    {
        assertEquals("[true,false]", serializeAsString(MAPPER, new boolean[] { true, false} ));
        assertEquals("[true,false]", serializeAsString(MAPPER, new Boolean[] { Boolean.TRUE, Boolean.FALSE} ));
    }

// com.fasterxml.jackson.databind.ser.TestSimpleTypes::testByteArray
    public void testByteArray() throws Exception
    {
        byte[] data = { 1, 17, -3, 127, -128 };
        Byte[] data2 = new Byte[data.length];
        for (int i = 0; i < data.length; ++i) {
            data2[i] = data[i]; 
        }
        
        String str1 = serializeAsString(MAPPER, data);
        String str2 = serializeAsString(MAPPER, data2);
        assertArrayEquals(data, MAPPER.readValue(str1, byte[].class));
        assertArrayEquals(data2, MAPPER.readValue(str2, Byte[].class));
    }

// com.fasterxml.jackson.databind.ser.TestSimpleTypes::testBase64Variants
    public void testBase64Variants() throws Exception
    {
        final byte[] INPUT = "abcdefghijklmnopqrstuvwxyz1234567890abcdefghijklmnopqrstuvwxyz1234567890X".getBytes("UTF-8");
        
        
        assertEquals(quote("YWJjZGVmZ2hpamtsbW5vcHFyc3R1dnd4eXoxMjM0NTY3ODkwYWJjZGVmZ2hpamtsbW5vcHFyc3R1dnd4eXoxMjM0NTY3ODkwWA=="), MAPPER.writeValueAsString(INPUT));
        assertEquals(quote("YWJjZGVmZ2hpamtsbW5vcHFyc3R1dnd4eXoxMjM0NTY3ODkwYWJjZGVmZ2hpamtsbW5vcHFyc3R1dnd4eXoxMjM0NTY3ODkwWA=="),
                MAPPER.writer(Base64Variants.MIME_NO_LINEFEEDS).writeValueAsString(INPUT));

        
        assertEquals(quote("YWJjZGVmZ2hpamtsbW5vcHFyc3R1dnd4eXoxMjM0NTY3ODkwYWJjZGVmZ2hpamtsbW5vcHFyc3R1\\ndnd4eXoxMjM0NTY3ODkwWA=="),
                MAPPER.writer(Base64Variants.MIME).writeValueAsString(INPUT));
        assertEquals(quote("YWJjZGVmZ2hpamtsbW5vcHFyc3R1dnd4eXoxMjM0NTY3ODkwYWJjZGVmZ2hpamtsbW5vcHFyc3R1dnd4eXoxMjM0NTY3ODkwWA"), 
                MAPPER.writer(Base64Variants.MODIFIED_FOR_URL).writeValueAsString(INPUT));
        
        assertEquals(quote("YWJjZGVmZ2hpamtsbW5vcHFyc3R1dnd4eXoxMjM0NTY3ODkwYWJjZGVmZ2hpamts\\nbW5vcHFyc3R1dnd4eXoxMjM0NTY3ODkwWA=="),
                MAPPER.writer(Base64Variants.PEM).writeValueAsString(INPUT));
    }

// com.fasterxml.jackson.databind.ser.TestSimpleTypes::testShortArray
    public void testShortArray() throws Exception
    {
        assertEquals("[0,1]", serializeAsString(MAPPER, new short[] { 0, 1 }));
        assertEquals("[2,3]", serializeAsString(MAPPER, new Short[] { 2, 3 }));
    }

// com.fasterxml.jackson.databind.ser.TestSimpleTypes::testIntArray
    public void testIntArray() throws Exception
    {
        assertEquals("[0,-3]", serializeAsString(MAPPER, new int[] { 0, -3 }));
        assertEquals("[13,9]", serializeAsString(MAPPER, new Integer[] { 13, 9 }));
    }

// com.fasterxml.jackson.databind.ser.TestSimpleTypes::testFloat
    public void testFloat() throws Exception
    {
        double[] values = new double[] {
            0.0, 1.0, 0.1, -37.01, 999.99, 0.3, 33.3, Double.NaN, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY
        };
        for (double d : values) {
           float f = (float) d;
    	   String expected = String.valueOf(f);
           if (Float.isNaN(f) || Float.isInfinite(f)) {
               expected = "\""+expected+"\"";
       	   }
           assertEquals(expected,serializeAsString(MAPPER, Float.valueOf(f)));
        }
    }

// com.fasterxml.jackson.databind.ser.TestSimpleTypes::testClass
    public void testClass() throws Exception
    {
        String result = MAPPER.writeValueAsString(java.util.List.class);
        assertEquals("\"java.util.List\"", result);
    }

// com.fasterxml.jackson.databind.ser.TestStatics::testStaticFields
    public void testStaticFields() throws Exception
    {
        ObjectMapper m = new ObjectMapper();
        Map<String,Object> result = writeAndMap(m, new FieldBean());
        assertEquals(1, result.size());
        assertEquals(Integer.valueOf(1), result.get("x"));
    }

// com.fasterxml.jackson.databind.ser.TestStatics::testStaticMethods
    public void testStaticMethods() throws Exception
    {
        ObjectMapper m = new ObjectMapper();
        Map<String,Object> result = writeAndMap(m, new GetterBean());
        assertEquals(1, result.size());
        assertEquals(Integer.valueOf(3), result.get("x"));
    }

// com.fasterxml.jackson.databind.ser.TestTreeSerialization::testSimpleViaObjectMapper
	public void testSimpleViaObjectMapper()
        throws IOException
    {
        ObjectMapper mapper = new ObjectMapper();
        
        ObjectNode n = mapper.getNodeFactory().objectNode();
        n.put("number", 15);
        n.put("string", "abc");
        ObjectNode n2 = n.putObject("ob");
        n2.putArray("arr");
        StringWriter sw = new StringWriter();
        JsonGenerator jg = mapper.getFactory().createGenerator(sw);
        mapper.writeTree(jg, n);

        Map<String,Object> result = (Map<String,Object>) mapper.readValue(sw.toString(), Map.class);

        assertEquals(3, result.size());
        assertEquals("abc", result.get("string"));
        assertEquals(Integer.valueOf(15), result.get("number"));
        Map<String,Object> ob = (Map<String,Object>) result.get("ob");
        assertEquals(1, ob.size());
        List<Object> list = (List<Object>) ob.get("arr");
        assertEquals(0, list.size());
        jg.close();
    }

// com.fasterxml.jackson.databind.ser.TestTreeSerialization::testPOJOString
	public void testPOJOString()
        throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        
        ObjectNode n = mapper.getNodeFactory().objectNode();
        n.set("pojo", mapper.getNodeFactory().pojoNode("abc"));
        StringWriter sw = new StringWriter();
        JsonGenerator jg = mapper.getFactory().createGenerator(sw);
        mapper.writeTree(jg, n);
        Map<String,Object> result = (Map<String,Object>) mapper.readValue(sw.toString(), Map.class);
        assertEquals(1, result.size());
        assertEquals("abc", result.get("pojo"));
        jg.close();
    }

// com.fasterxml.jackson.databind.ser.TestTreeSerialization::testPOJOIntArray
    public void testPOJOIntArray()
        throws IOException
    {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode n = mapper.getNodeFactory().objectNode();
        n.set("pojo", mapper.getNodeFactory().pojoNode(new int[] { 1, 2, 3 }));
        StringWriter sw = new StringWriter();
        JsonGenerator jg = mapper.getFactory().createGenerator(sw);
        mapper.writeTree(jg, n);

        Map<String,Object> result = (Map<String,Object>) mapper.readValue(sw.toString(), Map.class);

        assertEquals(1, result.size());
        
        List<Object> list = (List<Object>) result.get("pojo");
        assertEquals(3, list.size());
        for (int i = 0; i < 3; ++i) {
            assertEquals(Integer.valueOf(i+1), list.get(i));
        }
        jg.close();
    }

// com.fasterxml.jackson.databind.ser.TestTreeSerialization::testPOJOBean
    public void testPOJOBean()
        throws IOException
    {
        ObjectMapper mapper = new ObjectMapper();
        
        ObjectNode n = mapper.getNodeFactory().objectNode();
        n.set("pojo", mapper.getNodeFactory().pojoNode(new Bean()));
        StringWriter sw = new StringWriter();
        JsonGenerator jg = mapper.getFactory().createGenerator(sw);
        mapper.writeTree(jg, n);

        Map<String,Object> result = (Map<String,Object>) mapper.readValue(sw.toString(), Map.class);

        assertEquals(1, result.size());
        Map<String,Object> bean = (Map<String,Object>) result.get("pojo");
        assertEquals(2, bean.size());
        assertEquals("y", bean.get("x"));
        assertEquals(Integer.valueOf(13), bean.get("y"));
        jg.close();
    }

// com.fasterxml.jackson.databind.ser.TestTypedRootValueSerialization::testTypedSerialization
    public void testTypedSerialization() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        String singleJson = mapper.writerFor(Issue822Interface.class).writeValueAsString(new Issue822Impl());
        
        assertEquals("{\"a\":3}", singleJson);
    }

// com.fasterxml.jackson.databind.ser.TestTypedRootValueSerialization::testTypedArrays
    public void testTypedArrays() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();

        assertEquals("[{\"a\":3}]", mapper.writerFor(Issue822Interface[].class).writeValueAsString(
                new Issue822Interface[] { new Issue822Impl() }));
    }

// com.fasterxml.jackson.databind.ser.TestTypedRootValueSerialization::testTypedLists
    public void testTypedLists() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
     

        List<Issue822Interface> list = new ArrayList<Issue822Interface>();
        list.add(new Issue822Impl());
        String listJson = mapper.writerFor(new TypeReference<List<Issue822Interface>>(){})
                .writeValueAsString(list);
        assertEquals("[{\"a\":3}]", listJson);
    }

// com.fasterxml.jackson.databind.ser.TestTypedRootValueSerialization::testTypedMaps
    public void testTypedMaps() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        Map<String,Issue822Interface> map = new HashMap<String,Issue822Interface>();
        map.put("a", new Issue822Impl());
        String listJson = mapper.writerFor(new TypeReference<Map<String,Issue822Interface>>(){})
                .writeValueAsString(map);
        assertEquals("{\"a\":{\"a\":3}}", listJson);
    }

// com.fasterxml.jackson.databind.ser.TestUntypedSerialization::testFromArray
    public void testFromArray()
        throws Exception
    {
        ArrayList<Object> doc = new ArrayList<Object>();
        doc.add("Elem1");
        doc.add(Integer.valueOf(3));
        Map<String,Object> struct = new LinkedHashMap<String, Object>();
        struct.put("first", Boolean.TRUE);
        struct.put("Second", new ArrayList<Object>());
        doc.add(struct);
        doc.add(Boolean.FALSE);

        ObjectMapper mapper = new ObjectMapper();
        JsonFactory f =  new JsonFactory();

        
        for (int i = 0; i < 3; ++i) {
            String str = mapper.writeValueAsString(doc);
            
            JsonParser jp = f.createParser(str);
            assertEquals(JsonToken.START_ARRAY, jp.nextToken());
            
            assertEquals(JsonToken.VALUE_STRING, jp.nextToken());
            assertEquals("Elem1", getAndVerifyText(jp));
            
            assertEquals(JsonToken.VALUE_NUMBER_INT, jp.nextToken());
            assertEquals(3, jp.getIntValue());
            
            assertEquals(JsonToken.START_OBJECT, jp.nextToken());
            assertEquals(JsonToken.FIELD_NAME, jp.nextToken());
            assertEquals("first", getAndVerifyText(jp));
            
            assertEquals(JsonToken.VALUE_TRUE, jp.nextToken());
            assertEquals(JsonToken.FIELD_NAME, jp.nextToken());
            assertEquals("Second", getAndVerifyText(jp));
            
            if (jp.nextToken() != JsonToken.START_ARRAY) {
                fail("Expected START_ARRAY: JSON == '"+str+"'");
            }
            assertEquals(JsonToken.END_ARRAY, jp.nextToken());
            assertEquals(JsonToken.END_OBJECT, jp.nextToken());
            
            assertEquals(JsonToken.VALUE_FALSE, jp.nextToken());
            
            assertEquals(JsonToken.END_ARRAY, jp.nextToken());
            assertNull(jp.nextToken());
            jp.close();
        }
    }

// com.fasterxml.jackson.databind.ser.TestUntypedSerialization::testFromMap
    public void testFromMap()
        throws Exception
    {
        LinkedHashMap<String,Object> doc = new LinkedHashMap<String,Object>();
        JsonFactory f =  new JsonFactory();

        doc.put("a1", "\"text\"");
        doc.put("int", Integer.valueOf(137));
        doc.put("foo bar", Long.valueOf(1234567890L));

        ObjectMapper mapper = new ObjectMapper();
        for (int i = 0; i < 3; ++i) {
            String str = mapper.writeValueAsString(doc);
            JsonParser jp = f.createParser(str);
            
            assertEquals(JsonToken.START_OBJECT, jp.nextToken());
            
            assertEquals(JsonToken.FIELD_NAME, jp.nextToken());
            assertEquals("a1", getAndVerifyText(jp));
            assertEquals(JsonToken.VALUE_STRING, jp.nextToken());
            assertEquals("\"text\"", getAndVerifyText(jp));
            
            assertEquals(JsonToken.FIELD_NAME, jp.nextToken());
            assertEquals("int", getAndVerifyText(jp));
            assertEquals(JsonToken.VALUE_NUMBER_INT, jp.nextToken());
            assertEquals(137, jp.getIntValue());
            
            assertEquals(JsonToken.FIELD_NAME, jp.nextToken());
            assertEquals("foo bar", getAndVerifyText(jp));
            assertEquals(JsonToken.VALUE_NUMBER_INT, jp.nextToken());
            assertEquals(1234567890L, jp.getLongValue());
            
            assertEquals(JsonToken.END_OBJECT, jp.nextToken());

            assertNull(jp.nextToken());
            jp.close();
        }
    }

// com.fasterxml.jackson.databind.ser.TestUnwrappedWithTypeInfo::testDefaultUnwrappedWithTypeInfo
	public void testDefaultUnwrappedWithTypeInfo() throws Exception
	{
	    Outer outer = new Outer();
	    outer.setP1("101");

	    Inner inner = new Inner();
	    inner.setP2("202");
	    outer.setInner(inner);

	    ObjectMapper mapper = new ObjectMapper();

	    try {
	        mapper.writeValueAsString(outer);
	         fail("Expected exception to be thrown.");
	    } catch (JsonMappingException ex) {
	        verifyException(ex, "requires use of type information");
	    }
	}

// com.fasterxml.jackson.databind.ser.TestUnwrappedWithTypeInfo::testUnwrappedWithTypeInfoAndFeatureDisabled
	public void testUnwrappedWithTypeInfoAndFeatureDisabled() throws Exception
	{
		Outer outer = new Outer();
		outer.setP1("101");

		Inner inner = new Inner();
		inner.setP2("202");
		outer.setInner(inner);

		ObjectMapper mapper = new ObjectMapper();
		mapper = mapper.disable(SerializationFeature.FAIL_ON_UNWRAPPED_TYPE_IDENTIFIERS);

		String json = mapper.writeValueAsString(outer);
		assertEquals("{\"@type\":\"OuterType\",\"p1\":\"101\",\"p2\":\"202\"}", json);
	}

// com.fasterxml.jackson.databind.ser.TestVirtualProperties::testAttributeProperties
    public void testAttributeProperties() throws Exception
    {
        Map<String,Object> stuff = new LinkedHashMap<String,Object>();
        stuff.put("x", 3);
        stuff.put("y", ABC.B);

        String json = WRITER.withAttribute("id", "abc123")
                .withAttribute("internal", stuff)
                .writeValueAsString(new SimpleBean());
        assertEquals(aposToQuotes("{'value':13,'id':'abc123','extra':{'x':3,'y':'B'}}"), json);

        json = WRITER.withAttribute("id", "abc123")
                .withAttribute("internal", stuff)
                .writeValueAsString(new SimpleBeanPrepend());
        assertEquals(aposToQuotes("{'id':'abc123','extra':{'x':3,'y':'B'},'value':13}"), json);
    }

// com.fasterxml.jackson.databind.ser.TestVirtualProperties::testAttributePropInclusion
    public void testAttributePropInclusion() throws Exception
    {
        
        String json = WRITER.withAttribute("desc", "nice")
                .writeValueAsString(new OptionalsBean());
        assertEquals(aposToQuotes("{'value':28,'desc':'nice'}"), json);

        
        json = WRITER.writeValueAsString(new OptionalsBean());
        assertEquals(aposToQuotes("{'value':28}"), json);

        
        json = WRITER.withAttribute("desc", "")
                .writeValueAsString(new OptionalsBean());
        assertEquals(aposToQuotes("{'value':28}"), json);
    }

// com.fasterxml.jackson.databind.ser.TestVirtualProperties::testCustomProperties
    public void testCustomProperties() throws Exception
    {
        String json = WRITER.withAttribute("desc", "nice")
                .writeValueAsString(new CustomVBean());
        assertEquals(aposToQuotes("{'id':'abc123','extra':[42],'value':72}"), json);
    }

// com.fasterxml.jackson.databind.struct.FormatFeaturesTest::testWithArrayTypes
    public void testWithArrayTypes() throws Exception
    {
        
        assertEquals(aposToQuotes("{'strings':'a','ints':[1],'bools':[true]}"),
                MAPPER.writeValueAsString(new WrapWriteWithArrays()));

        
        assertEquals(aposToQuotes("{'strings':'a','ints':[1],'bools':true}"),
                MAPPER.writer().with(SerializationFeature.WRITE_SINGLE_ELEM_ARRAYS_UNWRAPPED)
                .writeValueAsString(new WrapWriteWithArrays()));

        
        assertEquals(aposToQuotes("{'strings':'a','ints':[1],'bools':[true]}"),
                MAPPER.writer().without(SerializationFeature.WRITE_SINGLE_ELEM_ARRAYS_UNWRAPPED)
                .writeValueAsString(new WrapWriteWithArrays()));

        
        ObjectMapper mapper = new ObjectMapper();
        mapper.configOverride(String[].class).setFormat(JsonFormat.Value.empty()
                .withFeature(JsonFormat.Feature.WRITE_SINGLE_ELEM_ARRAYS_UNWRAPPED));
        assertEquals(aposToQuotes("{'values':'a'}"),
                mapper.writeValueAsString(new StringArrayNotAnnoted("a")));
    }

// com.fasterxml.jackson.databind.struct.FormatFeaturesTest::testWithCollectionTypes
    public void testWithCollectionTypes() throws Exception
    {
        
        assertEquals(aposToQuotes("{'strings':'a','ints':[1],'bools':[true],'enums':'B'}"),
                MAPPER.writeValueAsString(new WrapWriteWithCollections()));

        
        assertEquals(aposToQuotes("{'strings':'a','ints':[1],'bools':true,'enums':'B'}"),
                MAPPER.writer().with(SerializationFeature.WRITE_SINGLE_ELEM_ARRAYS_UNWRAPPED)
                .writeValueAsString(new WrapWriteWithCollections()));

        
        assertEquals(aposToQuotes("{'strings':'a','ints':[1],'bools':[true],'enums':'B'}"),
                MAPPER.writer().without(SerializationFeature.WRITE_SINGLE_ELEM_ARRAYS_UNWRAPPED)
                .writeValueAsString(new WrapWriteWithCollections()));
    }

// com.fasterxml.jackson.databind.struct.FormatFeaturesTest::testSingleStringArrayRead
    public void testSingleStringArrayRead() throws Exception {
        String json = aposToQuotes(
                "{ 'values': 'first' }");
        StringArrayWrapper result = MAPPER.readValue(json, StringArrayWrapper.class);
        assertNotNull(result.values);
        assertEquals(1, result.values.length);
        assertEquals("first", result.values[0]);

        
        ObjectMapper mapper = new ObjectMapper();
        mapper.configOverride(String[].class).setFormat(JsonFormat.Value.empty()
                .withFeature(JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY));
        StringArrayNotAnnoted result2 = mapper.readValue(json, StringArrayNotAnnoted.class);
        assertNotNull(result2.values);
        assertEquals(1, result2.values.length);
        assertEquals("first", result2.values[0]);
    }

// com.fasterxml.jackson.databind.struct.FormatFeaturesTest::testSingleIntArrayRead
    public void testSingleIntArrayRead() throws Exception {
        String json = aposToQuotes(
                "{ 'values': 123 }");
        IntArrayWrapper result = MAPPER.readValue(json, IntArrayWrapper.class);
        assertNotNull(result.values);
        assertEquals(1, result.values.length);
        assertEquals(123, result.values[0]);
    }

// com.fasterxml.jackson.databind.struct.FormatFeaturesTest::testSingleLongArrayRead
    public void testSingleLongArrayRead() throws Exception {
        String json = aposToQuotes(
                "{ 'values': -205 }");
        LongArrayWrapper result = MAPPER.readValue(json, LongArrayWrapper.class);
        assertNotNull(result.values);
        assertEquals(1, result.values.length);
        assertEquals(-205L, result.values[0]);
    }

// com.fasterxml.jackson.databind.struct.FormatFeaturesTest::testSingleElementArrayRead
    public void testSingleElementArrayRead() throws Exception {
        String json = aposToQuotes(
                "{ 'roles': { 'Name': 'User', 'ID': '333' } }");
        RolesInArray response = MAPPER.readValue(json, RolesInArray.class);
        assertNotNull(response.roles);
        assertEquals(1, response.roles.length);
        assertEquals("333", response.roles[0].ID);
    }

// com.fasterxml.jackson.databind.struct.FormatFeaturesTest::testSingleStringListRead
    public void testSingleStringListRead() throws Exception {
        String json = aposToQuotes(
                "{ 'values': 'first' }");
        StringListWrapper result = MAPPER.readValue(json, StringListWrapper.class);
        assertNotNull(result.values);
        assertEquals(1, result.values.size());
        assertEquals("first", result.values.get(0));
    }

// com.fasterxml.jackson.databind.struct.FormatFeaturesTest::testSingleElementListRead
    public void testSingleElementListRead() throws Exception {
        String json = aposToQuotes(
                "{ 'roles': { 'Name': 'User', 'ID': '333' } }");
        RolesInList response = MAPPER.readValue(json, RolesInList.class);
        assertNotNull(response.roles);
        assertEquals(1, response.roles.size());
        assertEquals("333", response.roles.get(0).ID);
    }

// com.fasterxml.jackson.databind.struct.FormatFeaturesTest::testSingleEnumSetRead
    public void testSingleEnumSetRead() throws Exception {
        EnumSetWrapper result = MAPPER.readValue(aposToQuotes("{ 'values': 'B' }"),
                EnumSetWrapper.class);
        assertNotNull(result.values);
        assertEquals(1, result.values.size());
        assertEquals(ABC.B, result.values.iterator().next());
    }

// com.fasterxml.jackson.databind.struct.FormatFeaturesTest::testCaseInsensitive
    public void testCaseInsensitive() throws Exception {
        CaseInsensitiveRoleWrapper w = MAPPER.readValue
                (aposToQuotes("{'role':{'id':'12','name':'Foo'}}"),
                        CaseInsensitiveRoleWrapper.class);
        assertNotNull(w);
        assertEquals("12", w.role.ID);
        assertEquals("Foo", w.role.Name);
    }

// com.fasterxml.jackson.databind.struct.FormatFeaturesTest::testOrderedMaps
    public void testOrderedMaps() throws Exception {
        SortedKeysMap map = new SortedKeysMap()
            .put("b", 2)
            .put("a", 1);
        assertEquals(aposToQuotes("{'values':{'a':1,'b':2}}"),
                MAPPER.writeValueAsString(map));
    }

// com.fasterxml.jackson.databind.struct.PojoAsArray646Test::testWithCustomTypeId
    public void testWithCustomTypeId() throws Exception {

        List<TheItem.NestedItem> nestedList = new ArrayList<TheItem.NestedItem>();
        nestedList.add(new TheItem.NestedItem("foo1"));
        nestedList.add(new TheItem.NestedItem("foo2"));
        TheItem item = new TheItem("first", false, nestedList);
        Outer outer = new Outer();
        outer.getAttributes().put("entry1", item);

        String json = MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(outer);

        Outer result = MAPPER.readValue(json, Outer.class);
        assertNotNull(result);
        assertNotNull(result.attributes);
        assertEquals(1, result.attributes.size());
    }

// com.fasterxml.jackson.databind.struct.TestBackRefsWithPolymorphic::testDeserialize
    public void testDeserialize() throws IOException
    {
        PropertySheet input = MAPPER.readValue(JSON, PropertySheet.class);
        assertEquals(JSON, MAPPER.writeValueAsString(input));
    }

// com.fasterxml.jackson.databind.struct.TestBackRefsWithPolymorphic::testSerialize
    public void testSerialize() throws IOException
    {
        PropertySheet sheet = new PropertySheetImpl();

        sheet.addProperty(new StringPropertyImpl("p1name", "p1value"));
        sheet.addProperty(new StringPropertyImpl("p2name", "p2value"));
        String actual = MAPPER.writeValueAsString(sheet);
        assertEquals(JSON, actual);
    }

// com.fasterxml.jackson.databind.struct.TestFormatForCollections::testListAsObject
    public void testListAsObject() throws Exception
    {
        
        CollectionAsPOJO list = new CollectionAsPOJO();
        list.add("a");
        list.add("b");
        String json = MAPPER.writeValueAsString(list);
        assertEquals("{\"size\":2,\"values\":[\"a\",\"b\"]}", json);

        
        CollectionAsPOJO result = MAPPER.readValue(json, CollectionAsPOJO.class);
        assertEquals(2, result.size());
    }

// com.fasterxml.jackson.databind.struct.TestForwardReference::testForwardRef
	public void testForwardRef() throws IOException {
		MAPPER.readValue("{" +
				"  \"@type\" : \"TestForwardReference$ForwardReferenceContainerClass\"," +
				"  \"frc\" : \"willBeForwardReferenced\"," +
				"  \"yac\" : {" +
				"    \"@type\" : \"TestForwardReference$YetAnotherClass\"," +
				"    \"frc\" : {" +
				"      \"@type\" : \"One\"," +
				"      \"id\" : \"willBeForwardReferenced\"" +
				"    }," +
				"    \"id\" : \"anId\"" +
				"  }," +
				"  \"id\" : \"ForwardReferenceContainerClass1\"" +
				"}", ForwardReferenceContainerClass.class);

	}

// com.fasterxml.jackson.databind.struct.TestPOJOAsArray::testReadSimplePropertyValue
    public void testReadSimplePropertyValue() throws Exception
    {
        String json = "{\"value\":[true,\"Foobar\",42,13]}";
        PojoAsArrayWrapper p = MAPPER.readValue(json, PojoAsArrayWrapper.class);
        assertNotNull(p.value);
        assertTrue(p.value.complete);
        assertEquals("Foobar", p.value.name);
        assertEquals(42, p.value.x);
        assertEquals(13, p.value.y);
    }

// com.fasterxml.jackson.databind.struct.TestPOJOAsArray::testReadSimpleRootValue
    public void testReadSimpleRootValue() throws Exception
    {
        String json = "[false,\"Bubba\",1,2]";
        FlatPojo p = MAPPER.readValue(json, FlatPojo.class);
        assertFalse(p.complete);
        assertEquals("Bubba", p.name);
        assertEquals(1, p.x);
        assertEquals(2, p.y);
    }

// com.fasterxml.jackson.databind.struct.TestPOJOAsArray::testWriteSimplePropertyValue
    public void testWriteSimplePropertyValue() throws Exception
    {
        String json = MAPPER.writeValueAsString(new PojoAsArrayWrapper("Foobar", 42, 13, true));
        
        assertEquals("{\"value\":[true,\"Foobar\",42,13]}", json);
    }

// com.fasterxml.jackson.databind.struct.TestPOJOAsArray::testWriteSimpleRootValue
    public void testWriteSimpleRootValue() throws Exception
    {
        String json = MAPPER.writeValueAsString(new FlatPojo("Bubba", 1, 2, false));
        
        assertEquals("[false,\"Bubba\",1,2]", json);
    }

// com.fasterxml.jackson.databind.struct.TestPOJOAsArray::testNullColumn
    public void testNullColumn() throws Exception
    {
        assertEquals("[null,\"bar\"]", MAPPER.writeValueAsString(new TwoStringsBean()));
    }

// com.fasterxml.jackson.databind.struct.TestPOJOAsArray::testSerializeAsArrayWithSingleProperty
    public void testSerializeAsArrayWithSingleProperty() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(SerializationFeature.WRITE_SINGLE_ELEM_ARRAYS_UNWRAPPED);
        String json = mapper.writeValueAsString(new SingleBean());
        assertEquals("\"foo\"", json);
    }

// com.fasterxml.jackson.databind.struct.TestPOJOAsArray::testBeanAsArrayUnwrapped
    public void testBeanAsArrayUnwrapped() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY);
        SingleBean result = mapper.readValue("[\"foobar\"]", SingleBean.class);
        assertNotNull(result);
        assertEquals("foobar", result.name);
    }

// com.fasterxml.jackson.databind.struct.TestPOJOAsArray::testAnnotationOverride
    public void testAnnotationOverride() throws Exception
    {
        
        assertEquals("{\"value\":{\"x\":1,\"y\":2}}", MAPPER.writeValueAsString(new A()));

        
        ObjectMapper mapper2 = new ObjectMapper();
        mapper2.setAnnotationIntrospector(new ForceArraysIntrospector());
        assertEquals("[[1,2]]", mapper2.writeValueAsString(new A()));

        
    }

// com.fasterxml.jackson.databind.struct.TestPOJOAsArray::testWithMaps
    public void testWithMaps() throws Exception
    {
        AsArrayWithMap input = new AsArrayWithMap(1, 2);
        String json = MAPPER.writeValueAsString(input);
        AsArrayWithMap output = MAPPER.readValue(json, AsArrayWithMap.class);
        assertNotNull(output);
        assertNotNull(output.attrs);
        assertEquals(1, output.attrs.size());
        assertEquals(Integer.valueOf(2), output.attrs.get(1));
    }

// com.fasterxml.jackson.databind.struct.TestPOJOAsArray::testSimpleWithIndex
    public void testSimpleWithIndex() throws Exception
    {
        

        CreatorWithIndex value = MAPPER.readValue(aposToQuotes("[2,1]"),
                CreatorWithIndex.class);
        assertEquals(2, value._a);
        assertEquals(1, value._b);
    }

// com.fasterxml.jackson.databind.struct.TestPOJOAsArray::testWithConfigOverrides
    public void testWithConfigOverrides() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configOverride(NonAnnotatedXY.class)
            .setFormat(JsonFormat.Value.forShape(JsonFormat.Shape.ARRAY));
        String json = mapper.writeValueAsString(new NonAnnotatedXY(2, 3));
        assertEquals("[2,3]", json);

        
        NonAnnotatedXY result = mapper.readValue(json, NonAnnotatedXY.class);
        assertNotNull(result);
        assertEquals(3, result.y);
    }

// com.fasterxml.jackson.databind.struct.TestPOJOAsArrayAdvanced::testWithView
    public void testWithView() throws Exception
    {
        
        AsArrayWithView input = new AsArrayWithView();
        input.a = 1;
        input.b = 2;
        input.c = 3;
        String json = MAPPER.writerWithView(ViewA.class).writeValueAsString(input);
        assertEquals("[1,null,3]", json);

        
        AsArrayWithView output = MAPPER.readerFor(AsArrayWithView.class).withView(ViewB.class)
                .readValue("[1,2,3]");
        
        assertEquals(3, output.c);
        assertEquals(2, output.b);
        assertEquals(0, output.a);
    }

// com.fasterxml.jackson.databind.struct.TestPOJOAsArrayAdvanced::testWithCreatorsOrdered
    public void testWithCreatorsOrdered() throws Exception
    {
        CreatorAsArray input = new CreatorAsArray(3, 4);
        input.a = 1;
        input.b = 2;

        
        String json = MAPPER.writeValueAsString(input);
        assertEquals("[3,4,1,2]", json);

        
        CreatorAsArray output = MAPPER.readValue(json, CreatorAsArray.class);
        assertEquals(1, output.a);
        assertEquals(2, output.b);
        assertEquals(3, output.x);
        assertEquals(4, output.y);
    }

// com.fasterxml.jackson.databind.struct.TestPOJOAsArrayAdvanced::testWithCreatorsShuffled
    public void testWithCreatorsShuffled() throws Exception
    {
        CreatorAsArrayShuffled input = new CreatorAsArrayShuffled(3, 4);
        input.a = 1;
        input.b = 2;

        
        String json = MAPPER.writeValueAsString(input);
        assertEquals("[1,2,3,4]", json);

        
        CreatorAsArrayShuffled output = MAPPER.readValue(json, CreatorAsArrayShuffled.class);
        assertEquals(1, output.a);
        assertEquals(2, output.b);
        assertEquals(3, output.x);
        assertEquals(4, output.y);
    }

// com.fasterxml.jackson.databind.struct.TestPOJOAsArrayWithBuilder::testSimpleBuilder
    public void testSimpleBuilder() throws Exception
    {
        
        ValueClassXY value = MAPPER.readValue("[1,2]", ValueClassXY.class);
        assertEquals(2, value._x);
        assertEquals(3, value._y);
    }

// com.fasterxml.jackson.databind.struct.TestParentChildReferences::testSimpleRefs
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

// com.fasterxml.jackson.databind.struct.TestParentChildReferences::testSimpleRefsWithGetter
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

// com.fasterxml.jackson.databind.struct.TestParentChildReferences::testFullRefs
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

// com.fasterxml.jackson.databind.struct.TestParentChildReferences::testArrayOfRefs
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

// com.fasterxml.jackson.databind.struct.TestParentChildReferences::testListOfRefs
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

// com.fasterxml.jackson.databind.struct.TestParentChildReferences::testMapOfRefs
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

// com.fasterxml.jackson.databind.struct.TestParentChildReferences::testAbstract368
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

// com.fasterxml.jackson.databind.struct.TestParentChildReferences::testIssue693
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

// com.fasterxml.jackson.databind.struct.TestParentChildReferences::testIssue708
    public void testIssue708() throws Exception
    {
        Advertisement708 ad = MAPPER.readValue("{\"title\":\"Hroch\",\"photos\":[{\"id\":3}]}", Advertisement708.class);      
        assertNotNull(ad);
    }

// com.fasterxml.jackson.databind.struct.TestUnwrapped::testSimpleUnwrappingSerialize
    public void testSimpleUnwrappingSerialize() throws Exception {
        assertEquals("{\"name\":\"Tatu\",\"x\":1,\"y\":2}",
                MAPPER.writeValueAsString(new Unwrapping("Tatu", 1, 2)));
    }

// com.fasterxml.jackson.databind.struct.TestUnwrapped::testDeepUnwrappingSerialize
    public void testDeepUnwrappingSerialize() throws Exception {
        assertEquals("{\"name\":\"Tatu\",\"x\":1,\"y\":2}",
                MAPPER.writeValueAsString(new DeepUnwrapping("Tatu", 1, 2)));
    }

// com.fasterxml.jackson.databind.struct.TestUnwrapped::testSimpleUnwrappedDeserialize
    public void testSimpleUnwrappedDeserialize() throws Exception
    {
        Unwrapping bean = MAPPER.readValue("{\"name\":\"Tatu\",\"y\":7,\"x\":-13}",
                Unwrapping.class);
        assertEquals("Tatu", bean.name);
        Location loc = bean.location;
        assertNotNull(loc);
        assertEquals(-13, loc.x);
        assertEquals(7, loc.y);
    }

// com.fasterxml.jackson.databind.struct.TestUnwrapped::testDoubleUnwrapping
    public void testDoubleUnwrapping() throws Exception
    {
        TwoUnwrappedProperties bean = MAPPER.readValue("{\"first\":\"Joe\",\"y\":7,\"last\":\"Smith\",\"x\":-13}",
                TwoUnwrappedProperties.class);
        Location loc = bean.location;
        assertNotNull(loc);
        assertEquals(-13, loc.x);
        assertEquals(7, loc.y);
        Name name = bean.name;
        assertNotNull(name);
        assertEquals("Joe", name.first);
        assertEquals("Smith", name.last);
    }

// com.fasterxml.jackson.databind.struct.TestUnwrapped::testDeepUnwrapping
    public void testDeepUnwrapping() throws Exception
    {
        DeepUnwrapping bean = MAPPER.readValue("{\"x\":3,\"name\":\"Bob\",\"y\":27}",
                DeepUnwrapping.class);
        Unwrapping uw = bean.unwrapped;
        assertNotNull(uw);
        assertEquals("Bob", uw.name);
        Location loc = uw.location;
        assertNotNull(loc);
        assertEquals(3, loc.x);
        assertEquals(27, loc.y);
    }

// com.fasterxml.jackson.databind.struct.TestUnwrapped::testUnwrappedDeserializeWithCreator
    public void testUnwrappedDeserializeWithCreator() throws Exception
    {
        UnwrappingWithCreator bean = MAPPER.readValue("{\"x\":1,\"y\":2,\"name\":\"Tatu\"}",
                UnwrappingWithCreator.class);
        assertEquals("Tatu", bean.name);
        Location loc = bean.location;
        assertNotNull(loc);
        assertEquals(1, loc.x);
        assertEquals(2, loc.y);
    }

// com.fasterxml.jackson.databind.struct.TestUnwrapped::testIssue615
    public void testIssue615() throws Exception
    {
        Parent input = new Parent("name");
        String json = MAPPER.writeValueAsString(input);
        Parent output = MAPPER.readValue(json, Parent.class);
        assertEquals("name", output.c1.field);
    }

// com.fasterxml.jackson.databind.struct.TestUnwrapped::testUnwrappedAsPropertyIndicator
    public void testUnwrappedAsPropertyIndicator() throws Exception
    {
        Inner inner = new Inner();
        inner.animal = "Zebra";

        Outer outer = new Outer();
        outer.inner = inner;

        String actual = MAPPER.writeValueAsString(outer);

        assertTrue(actual.contains("animal"));
        assertTrue(actual.contains("Zebra"));
        assertFalse(actual.contains("inner"));
    }

// com.fasterxml.jackson.databind.struct.TestUnwrappedWithPrefix::testPrefixedUnwrappingSerialize
    public void testPrefixedUnwrappingSerialize() throws Exception
    {
        assertEquals("{\"name\":\"Tatu\",\"_x\":1,\"_y\":2}",
                MAPPER.writeValueAsString(new PrefixUnwrap("Tatu", 1, 2)));
    }

// com.fasterxml.jackson.databind.struct.TestUnwrappedWithPrefix::testDeepPrefixedUnwrappingSerialize
    public void testDeepPrefixedUnwrappingSerialize() throws Exception
    {
        String json = MAPPER.writeValueAsString(new DeepPrefixUnwrap("Bubba", 1, 1));
        assertEquals("{\"u.name\":\"Bubba\",\"u._x\":1,\"u._y\":1}", json);
    }

// com.fasterxml.jackson.databind.struct.TestUnwrappedWithPrefix::testHierarchicConfigSerialize
    public void testHierarchicConfigSerialize() throws Exception
    {
        String json = MAPPER.writeValueAsString(new ConfigRoot("Fred", 25));
        assertEquals("{\"general.names.name\":\"Fred\",\"misc.value\":25}", json);
    }

// com.fasterxml.jackson.databind.struct.TestUnwrappedWithPrefix::testPrefixedUnwrapping
    public void testPrefixedUnwrapping() throws Exception
    {
        PrefixUnwrap bean = MAPPER.readValue("{\"name\":\"Axel\",\"_x\":4,\"_y\":7}", PrefixUnwrap.class);
        assertNotNull(bean);
        assertEquals("Axel", bean.name);
        assertNotNull(bean.location);
        assertEquals(4, bean.location.x);
        assertEquals(7, bean.location.y);
    }

// com.fasterxml.jackson.databind.struct.TestUnwrappedWithPrefix::testDeepPrefixedUnwrappingDeserialize
    public void testDeepPrefixedUnwrappingDeserialize() throws Exception
    {
        DeepPrefixUnwrap bean = MAPPER.readValue("{\"u.name\":\"Bubba\",\"u._x\":2,\"u._y\":3}",
                DeepPrefixUnwrap.class);
        assertNotNull(bean.unwrapped);
        assertNotNull(bean.unwrapped.location);
        assertEquals(2, bean.unwrapped.location.x);
        assertEquals(3, bean.unwrapped.location.y);
        assertEquals("Bubba", bean.unwrapped.name);
    }

// com.fasterxml.jackson.databind.struct.TestUnwrappedWithPrefix::testHierarchicConfigDeserialize
    public void testHierarchicConfigDeserialize() throws Exception
    {
        ConfigRoot root = MAPPER.readValue("{\"general.names.name\":\"Bob\",\"misc.value\":3}",
                ConfigRoot.class);
        assertNotNull(root.general);
        assertNotNull(root.general.names);
        assertNotNull(root.misc);
        assertEquals(3, root.misc.value);
        assertEquals("Bob", root.general.names.name);
    }

// com.fasterxml.jackson.databind.struct.TestUnwrappedWithPrefix::testHierarchicConfigRoundTrip
    public void testHierarchicConfigRoundTrip() throws Exception
    {
        ConfigAlternate input = new ConfigAlternate(123, "Joe", 42);
        String json = MAPPER.writeValueAsString(input);

        ConfigAlternate root = MAPPER.readValue(json, ConfigAlternate.class);
        assertEquals(123, root.id);
        assertNotNull(root.general);
        assertNotNull(root.general.names);
        assertNotNull(root.misc);
        assertEquals("Joe", root.general.names.name);
        assertEquals(42, root.misc.value);
    }

// com.fasterxml.jackson.databind.struct.TestUnwrappedWithPrefix::testIssue226
    public void testIssue226() throws Exception
    {
        Parent input = new Parent();
        input.c1 = new Child();
        input.c1.sc1 = new SubChild();
        input.c1.sc1.value = "a";
        input.c2 = new Child();
        input.c2.sc1 = new SubChild();
        input.c2.sc1.value = "b";

        String json = MAPPER.writeValueAsString(input);

        Parent output = MAPPER.readValue(json, Parent.class);
        assertNotNull(output.c1);
        assertNotNull(output.c2);

        assertNotNull(output.c1.sc1);
        assertNotNull(output.c2.sc1);
        
        assertEquals("a", output.c1.sc1.value);
        assertEquals("b", output.c2.sc1.value);
    }

// com.fasterxml.jackson.databind.struct.TestUnwrappedWithSameName647::testUnwrappedWithSamePropertyName
    public void testUnwrappedWithSamePropertyName() throws Exception {
        final String JSON = "{'mail': {'mail': 'the mail text'}}";
        UnwrappedWithSamePropertyName result = MAPPER.readValue(aposToQuotes(JSON), UnwrappedWithSamePropertyName.class);
        assertNotNull(result.mail);
        assertNotNull(result.mail.mail);
        assertEquals("the mail text", result.mail.mail.mail);
    }

// com.fasterxml.jackson.databind.type.PolymorphicList036Test::testPolymorphicWithOverride
    public void testPolymorphicWithOverride() throws Exception
    {
        JavaType type = MAPPER.getTypeFactory().constructCollectionType(StringyList.class, String.class);
        
        StringyList<String> list = new StringyList<String>();
        list.add("value 1");
        list.add("value 2");
        
        String serialized = MAPPER.writeValueAsString(list);

        
        StringyList<String> deserialized = MAPPER.readValue(serialized, type);

        
        assertNotNull(deserialized);
    }

// com.fasterxml.jackson.databind.type.RecursiveType938Test::testRecursivePair
    public void testRecursivePair() throws Exception
    {
        JavaType t = MAPPER.constructType(ImmutablePair.class);

        assertNotNull(t);
        assertEquals(ImmutablePair.class, t.getRawClass());

        List<ImmutablePair<String, Double>> list = new ArrayList<ImmutablePair<String, Double>>();
        list.add(ImmutablePair.of("Hello World!", 123d));
        String json = MAPPER.writeValueAsString(list);

        assertNotNull(json);

        
    }

// com.fasterxml.jackson.databind.type.TestAnnotatedClass::testFieldIntrospection
    public void testFieldIntrospection()
    {
        SerializationConfig config = MAPPER.getSerializationConfig();
        JavaType t = MAPPER.constructType(FieldBean.class);
        AnnotatedClass ac = AnnotatedClass.construct(t, config);
        
        assertEquals(2, ac.getFieldCount());
        for (AnnotatedField f : ac.fields()) {
            String fname = f.getName();
            if (!"bar".equals(fname) && !"props".equals(fname)) {
                fail("Unexpected field name '"+fname+"'");
            }
        }
    }

// com.fasterxml.jackson.databind.type.TestAnnotatedClass::testConstructorIntrospection
    public void testConstructorIntrospection()
    {
        
        
        Bean1005 bean = new Bean1005(13);
        SerializationConfig config = MAPPER.getSerializationConfig();
        JavaType t = MAPPER.constructType(bean.getClass());
        AnnotatedClass ac = AnnotatedClass.construct(t, config);
        assertEquals(1, ac.getConstructors().size());
    }

// com.fasterxml.jackson.databind.type.TestGenericFieldInSubtype::test677
    public void test677() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        
        JavaType t677 = mapper.constructType(Result677.Success677.class);
        assertNotNull(t677);
        Result677.Success677<Integer> s = new Result677.Success677<Integer>(Integer.valueOf(4));
        String json = mapper.writeValueAsString(s);
        assertEquals("{\"value\":4}", json);
    }

// com.fasterxml.jackson.databind.type.TestGenericFieldInSubtype::testInnerType
    public void testInnerType() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        BaseType.SubType<?> r = mapper.readValue("{}", BaseType.SubType.class);
        assertNotNull(r);
    }

// com.fasterxml.jackson.databind.type.TestGenericsBounded::testLowerBound
    public void testLowerBound() throws Exception
    {
        IntBeanWrapper<?> result = MAPPER.readValue("{\"wrapped\":{\"x\":3}}",
                IntBeanWrapper.class);
        assertNotNull(result);
        assertEquals(IntBean.class, result.wrapped.getClass());
        assertEquals(3, result.wrapped.x);
    }

// com.fasterxml.jackson.databind.type.TestGenericsBounded::testBounded
    public void testBounded() throws Exception
    {
        BoundedWrapper<IntBean> result = MAPPER.readValue
            ("{\"values\":[ {\"x\":3} ] } ", new TypeReference<BoundedWrapper<IntBean>>() {});
        List<?> list = result.values;
        assertEquals(1, list.size());
        Object ob = list.get(0);
        assertEquals(IntBean.class, ob.getClass());
        assertEquals(3, result.values.get(0).x);
    }

// com.fasterxml.jackson.databind.type.TestGenericsBounded::testGenericsComplex
    public void testGenericsComplex() throws Exception
    {
        DoubleRange in = new DoubleRange(-0.5, 0.5);
        String json = MAPPER.writeValueAsString(in);
        DoubleRange out = MAPPER.readValue(json, DoubleRange.class);
        assertNotNull(out);
        assertEquals(-0.5, out.start);
        assertEquals(0.5, out.end);
    }

// com.fasterxml.jackson.databind.type.TestGenericsBounded::testIssue778
    public void testIssue778() throws Exception
    {
        String json = "{\"rows\":[{\"d\":{}}]}";

        final TypeReference<?> typeRef = new TypeReference<ResultSetWithDoc<MyDoc>>() {};

        

        JavaType type = MAPPER.getTypeFactory().constructType(typeRef);
        JavaType resultSetType = type.findSuperType(ResultSet.class);
        assertNotNull(resultSetType);
        assertEquals(1, resultSetType.containedTypeCount());

        JavaType rowType = resultSetType.containedType(0);
        assertNotNull(rowType);
        assertEquals(RowWithDoc.class, rowType.getRawClass());
        
        assertEquals(1, rowType.containedTypeCount());
        JavaType docType = rowType.containedType(0);
        assertEquals(MyDoc.class, docType.getRawClass());

        
        ResultSetWithDoc<MyDoc> rs = MAPPER.readValue(json, type);
        Document d = rs.rows.iterator().next().d;
    
        assertEquals(MyDoc.class, d.getClass()); 
    }

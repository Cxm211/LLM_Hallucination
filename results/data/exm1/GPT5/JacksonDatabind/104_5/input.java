// buggy code
    protected void _format(TimeZone tz, Locale loc, Date date,
            StringBuffer buffer)
    {
        Calendar cal = _getCalendar(tz);
        cal.setTime(date);
        // [databind#2167]: handle range beyond [1, 9999]
        final int year = cal.get(Calendar.YEAR);

        // Assuming GregorianCalendar, special handling needed for BCE (aka BC)
                // 22-Nov-2018, tatu: Handling beyond 4-digits is not well specified wrt ISO-8601, but
                //   it seems that plus prefix IS mandated. Padding is an open question, but since agreeement
                //   for max length would be needed, we ewould need to limit to arbitrary length
                //   like five digits (erroring out if beyond or padding to that as minimum).
                //   Instead, let's just print number out as is and let decoder try to make sense of it.
            pad4(buffer, year);
        buffer.append('-');
        pad2(buffer, cal.get(Calendar.MONTH) + 1);
        buffer.append('-');
        pad2(buffer, cal.get(Calendar.DAY_OF_MONTH));
        buffer.append('T');
        pad2(buffer, cal.get(Calendar.HOUR_OF_DAY));
        buffer.append(':');
        pad2(buffer, cal.get(Calendar.MINUTE));
        buffer.append(':');
        pad2(buffer, cal.get(Calendar.SECOND));
        buffer.append('.');
        pad3(buffer, cal.get(Calendar.MILLISECOND));

        int offset = tz.getOffset(cal.getTimeInMillis());
        if (offset != 0) {
            int hours = Math.abs((offset / (60 * 1000)) / 60);
            int minutes = Math.abs((offset / (60 * 1000)) % 60);
            buffer.append(offset < 0 ? '-' : '+');
            pad2(buffer, hours);
            if( _tzSerializedWithColon ) {
            		buffer.append(':');
            }
            pad2(buffer, minutes);
        } else {
            // 24-Jun-2017, tatu: While `Z` would be conveniently short, older specs
            //   mandate use of full `+0000`
//            formatted.append('Z');
	        	if( _tzSerializedWithColon ) {
	            buffer.append("+00:00");
	        	}
	        	else {
	        		buffer.append("+0000");
	        	}
        }
    }

    private static void pad4(StringBuffer buffer, int value) {
        int h = value / 100;
        if (h == 0) {
            buffer.append('0').append('0');
        } else {
                pad2(buffer, h);
            value -= (100 * h);
        }
        pad2(buffer, value);
    }

// relevant test
// com.fasterxml.jackson.databind.ser.FieldSerializationTest::testIssue240
    public void testIssue240() throws Exception
    {
        Item240 bean = new Item240("a12", null);
        assertEquals(MAPPER.writeValueAsString(bean), "{\"id\":\"a12\"}");
    }

// com.fasterxml.jackson.databind.ser.FieldSerializationTest::testFailureDueToDups
    public void testFailureDueToDups() throws Exception
    {
        try {
            writeAndMap(MAPPER, new DupFieldBean());
        } catch (JsonMappingException e) {
            verifyException(e, "Multiple fields representing");
        }
    }

// com.fasterxml.jackson.databind.ser.FieldSerializationTest::testFailureDueToDupField
    public void testFailureDueToDupField() throws Exception
    {
        try {
            writeAndMap(MAPPER, new DupFieldBean2());
        } catch (JsonMappingException e) {
            verifyException(e, "Multiple fields representing");
        }
    }

// com.fasterxml.jackson.databind.ser.JsonValueTest::testSimpleMethodJsonValue
    public void testSimpleMethodJsonValue() throws Exception
    {
        assertEquals("\"abc\"", MAPPER.writeValueAsString(new ValueClass<String>("abc")));
        assertEquals("null", MAPPER.writeValueAsString(new ValueClass<String>(null)));
    }

// com.fasterxml.jackson.databind.ser.JsonValueTest::testSimpleFieldJsonValue
    public void testSimpleFieldJsonValue() throws Exception
    {
        assertEquals("\"abc\"", MAPPER.writeValueAsString(new FieldValueClass<String>("abc")));
        assertEquals("null", MAPPER.writeValueAsString(new FieldValueClass<String>(null)));
    }

// com.fasterxml.jackson.databind.ser.JsonValueTest::testJsonValueWithUseSerializer
    public void testJsonValueWithUseSerializer() throws Exception
    {
        String result = serializeAsString(MAPPER, new ToStringValueClass<Integer>(Integer.valueOf(123)));
        assertEquals("\"123\"", result);
    }

// com.fasterxml.jackson.databind.ser.JsonValueTest::testMixedJsonValue
    public void testMixedJsonValue() throws Exception
    {
        String result = serializeAsString(MAPPER, new ToStringValueClass2("xyz"));
        assertEquals("\"xyz\"", result);
    }

// com.fasterxml.jackson.databind.ser.JsonValueTest::testDisabling
    public void testDisabling() throws Exception
    {
        assertEquals(aposToQuotes("{'x':1,'y':2}"),
                MAPPER.writeValueAsString(new DisabledJsonValue()));
    }

// com.fasterxml.jackson.databind.ser.JsonValueTest::testValueWithStaticType
    public void testValueWithStaticType() throws Exception
    {
        
        assertEquals("{\"a\":\"a\",\"b\":\"b\"}", MAPPER.writeValueAsString(new ValueWrapper()));

        
        ObjectMapper staticMapper = new ObjectMapper();
        staticMapper.configure(MapperFeature.USE_STATIC_TYPING, true);
        assertEquals("{\"a\":\"a\"}", staticMapper.writeValueAsString(new ValueWrapper()));
    }

// com.fasterxml.jackson.databind.ser.JsonValueTest::testMapWithJsonValue
    public void testMapWithJsonValue() throws Exception {
        
        assertEquals("{\"a\":\"1\"}", MAPPER.writeValueAsString(new MapBean()));

        
        assertEquals("{\"b\":\"2\"}", MAPPER.writeValueAsString(new MapFieldBean()));
    }

// com.fasterxml.jackson.databind.ser.JsonValueTest::testWithMap
    public void testWithMap() throws Exception {
        assertEquals("42", MAPPER.writeValueAsString(new MapAsNumber()));
    }

// com.fasterxml.jackson.databind.ser.JsonValueTest::testWithList
    public void testWithList() throws Exception {
        assertEquals("13", MAPPER.writeValueAsString(new ListAsNumber()));
    }

// com.fasterxml.jackson.databind.ser.JsonValueTest::testInList
    public void testInList() throws Exception {
        IntExtBean bean = new IntExtBean();
        bean.add(1);
        bean.add(2);
        String json = MAPPER.writeValueAsString(bean);
        assertEquals(json, "{\"values\":[{\"i\":1},{\"i\":2}]}");
    }

// com.fasterxml.jackson.databind.ser.JsonValueTest::testPolymorphicSerdeWithDelegate
    public void testPolymorphicSerdeWithDelegate() throws Exception
    {
	    AdditionInterface adder = new AdditionInterfaceImpl(1);
	
	    assertEquals(2, adder.add(1));
	    String json = MAPPER.writeValueAsString(adder);
	    assertEquals("{\"boingo\":\"boopsy\",\"toAdd\":1}", json);
	    assertEquals(2, MAPPER.readValue(json, AdditionInterface.class).add(1));
    }

// com.fasterxml.jackson.databind.ser.JsonValueTest::testJsonValueWithCustomOverride
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

// com.fasterxml.jackson.databind.ser.RawValueTest::testSimpleStringGetter
    public void testSimpleStringGetter() throws Exception
    {
        String value = "abc";
        String result = MAPPER.writeValueAsString(new ClassGetter<String>(value));
        String expected = String.format("{\"nonRaw\":\"%s\",\"raw\":%s,\"value\":%s}", value, value, value);
        assertEquals(expected, result);
    }

// com.fasterxml.jackson.databind.ser.RawValueTest::testSimpleNonStringGetter
    public void testSimpleNonStringGetter() throws Exception
    {
        int value = 123;
        String result = MAPPER.writeValueAsString(new ClassGetter<Integer>(value));
        String expected = String.format("{\"nonRaw\":%d,\"raw\":%d,\"value\":%d}", value, value, value);
        assertEquals(expected, result);
    }

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
        assertEquals("[true,false]", writer.writeValueAsString(new boolean[] { true, false }));
        assertEquals("true", writer.writeValueAsString(new Boolean[] { Boolean.TRUE }));

        assertEquals("3", writer.writeValueAsString(new short[] { 3 }));
        assertEquals("[3,2]", writer.writeValueAsString(new short[] { 3, 2 }));
        
        assertEquals("3", writer.writeValueAsString(new int[] { 3 }));
        assertEquals("[3,2]", writer.writeValueAsString(new int[] { 3, 2 }));

        assertEquals("1", writer.writeValueAsString(new long[] { 1L }));
        assertEquals("[-1,4]", writer.writeValueAsString(new long[] { -1L, 4L }));

        assertEquals("0.5", writer.writeValueAsString(new double[] { 0.5 }));
        assertEquals("[0.5,2.5]", writer.writeValueAsString(new double[] { 0.5, 2.5 }));

        assertEquals("0.5", writer.writeValueAsString(new float[] { 0.5f }));
        assertEquals("[0.5,2.5]", writer.writeValueAsString(new float[] { 0.5f, 2.5f }));
        
        assertEquals(quote("foo"), writer.writeValueAsString(new String[] { "foo" }));
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
        assertEquals("[\"a\",\"\\\"foo\\\"\",null]",
                MAPPER.writeValueAsString(new String[] { "a", "\"foo\"", null }));
        assertEquals("[]",
                MAPPER.writeValueAsString(new String[] { }));
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
            public void serialize(Collection value, JsonGenerator gen, SerializerProvider provider)
                    throws IOException
            {
                if (value.size() != 0) {
                    collectionSerializer.serialize(value, gen, provider);
                } else {
                    gen.writeNull();
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

// com.fasterxml.jackson.databind.ser.TestCustomSerializers::testWithCustomElements
    public void testWithCustomElements() throws Exception
    {
        
        StringListWrapper wr = new StringListWrapper("a", null, "b");
        assertEquals(aposToQuotes("{'list':['A',null,'B']}"),
                MAPPER.writeValueAsString(wr));

        
        
        SimpleModule module = new SimpleModule("test", Version.unknownVersion());
        module.addSerializer(String.class, new UCStringSerializer());
        ObjectMapper mapper = new ObjectMapper()
                .registerModule(module);

        assertEquals(quote("FOOBAR"), mapper.writeValueAsString("foobar"));
        assertEquals(aposToQuotes("['FOO',null]"),
                mapper.writeValueAsString(new String[] { "foo", null }));

        List<String> list = Arrays.asList("foo", null);
        assertEquals(aposToQuotes("['FOO',null]"), mapper.writeValueAsString(list));

        Set<String> set = new LinkedHashSet<String>(Arrays.asList("foo", null));
        assertEquals(aposToQuotes("['FOO',null]"), mapper.writeValueAsString(set));
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
    public void testEnumsWithJsonValue() throws Exception {
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

        
        json = MAPPER.writer()
                .with(SerializationFeature.WRITE_ENUMS_USING_TO_STRING)
                .writeValueAsString(bean);
        assertEquals("{\"map\":{\"b\":3}}", json);

        
        json = MAPPER.writer()
                .with(SerializationFeature.WRITE_ENUMS_USING_INDEX)
                .writeValueAsString(bean);
        assertEquals(aposToQuotes("{'map':{'"+TestEnum.B.ordinal()+"':3}}"), json);
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

// com.fasterxml.jackson.databind.ser.TestEnumSerialization::testGenericEnumSerializer
    public void testGenericEnumSerializer() throws Exception
    {
        
        ObjectMapper m = new ObjectMapper();
        SimpleModule module = new SimpleModule("foobar");
        module.addSerializer(Enum.class, new LowerCasingEnumSerializer());
        m.registerModule(module);
        assertEquals(quote("b"), m.writeValueAsString(TestEnum.B));
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

// com.fasterxml.jackson.databind.ser.TestEnumSerialization::testEnumsWithJsonProperty
    public void testEnumsWithJsonProperty() throws Exception {
        assertEquals(quote("aleph"), MAPPER.writeValueAsString(EnumWithJsonProperty.A));
    }

// com.fasterxml.jackson.databind.ser.TestEnumSerialization::testEnumKeysWithJsonProperty
    public void testEnumKeysWithJsonProperty() throws Exception {
        Map<EnumWithJsonProperty,Integer> input = new HashMap<EnumWithJsonProperty,Integer>();
        input.put(EnumWithJsonProperty.A, 13);
        assertEquals(aposToQuotes("{'aleph':13}"), MAPPER.writeValueAsString(input));
    }

// com.fasterxml.jackson.databind.ser.TestEnumSerialization::testEnumsWithJsonPropertyInSet
    public void testEnumsWithJsonPropertyInSet() throws Exception
    {
        assertEquals("[\"aleph\"]",
                MAPPER.writeValueAsString(EnumSet.of(EnumWithJsonProperty.A)));
    }

// com.fasterxml.jackson.databind.ser.TestEnumSerialization::testEnumsWithJsonPropertyAsKey
    public void testEnumsWithJsonPropertyAsKey() throws Exception
    {
        EnumMap<EnumWithJsonProperty,String> input = new EnumMap<EnumWithJsonProperty,String>(EnumWithJsonProperty.class);
        input.put(EnumWithJsonProperty.A, "b");
        assertEquals("{\"aleph\":\"b\"}", MAPPER.writeValueAsString(input));
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
        ArrayList<Integer> l = new ArrayList<Integer>();
        l.add(1);
        l.add(null);
        l.add(-9);
        l.add(0);
        
        assertEquals("[1,null,-9,0]", MAPPER.writeValueAsString(l.iterator()));
        l.clear();
        assertEquals("[]", MAPPER.writeValueAsString(l.iterator()));
    }

// com.fasterxml.jackson.databind.ser.TestIterable::testIterable
    public void testIterable() throws IOException
    {
        assertEquals("[1,2,3]",
                MAPPER.writeValueAsString(new IterableWrapper(new int[] { 1, 2, 3 })));
    }

// com.fasterxml.jackson.databind.ser.TestIterable::testWithIterable
    public void testWithIterable() throws IOException
    {
        assertEquals("{\"values\":[\"value\"]}",
                STATIC_MAPPER.writeValueAsString(new BeanWithIterable()));
        assertEquals("[1,2,3]",
                STATIC_MAPPER.writeValueAsString(new IntIterable()));
    }

// com.fasterxml.jackson.databind.ser.TestIterable::testWithIterator
    public void testWithIterator() throws IOException
    {
        assertEquals("{\"values\":[\"itValue\"]}",
                STATIC_MAPPER.writeValueAsString(new BeanWithIterator()));

        
        ArrayList<Number> numbersList = new ArrayList<>();
        numbersList.add(1);
        numbersList.add(0.25);
        String json = MAPPER.writeValueAsString(numbersList.iterator());
        assertEquals("[1,0.25]", json);
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
    public void testTokenBuffer() throws Exception
    {
        
        JsonParser jp = createParserUsingReader(SAMPLE_DOC_JSON_SPEC);
        TokenBuffer tb = new TokenBuffer(null, false);
        while (jp.nextToken() != null) {
            tb.copyCurrentEvent(jp);
        }
        jp.close();
        
        String str = serializeAsString(tb);
        tb.close();
        
        verifyJsonSpecSampleDoc(createParserUsingReader(str), true);
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
            fail("Should not succeed");
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

// com.fasterxml.jackson.databind.ser.TestKeySerializers::testCustomNullSerializers
    public void testCustomNullSerializers() throws IOException
    {
        final ObjectMapper mapper = new ObjectMapper();
        mapper.getSerializerProvider().setNullKeySerializer(new NullKeySerializer("NULL-KEY"));
        mapper.getSerializerProvider().setNullValueSerializer(new NullValueSerializer("NULL"));
        Map<String,Integer> input = new HashMap<>();
        input.put(null, 3);
        String json = mapper.writeValueAsString(input);
        assertEquals("{\"NULL-KEY\":3}", json);
        json = mapper.writeValueAsString(new Object[] { 1, null, true });
        assertEquals("[1,\"NULL\",true]", json);
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

// com.fasterxml.jackson.databind.ser.TestMapSerialization::testMapKeySetValuesSerialization
    public void testMapKeySetValuesSerialization() throws IOException
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

// com.fasterxml.jackson.databind.ser.TestMapSerialization::testOrderByKey
    public void testOrderByKey() throws IOException
    {
        ObjectMapper m = new ObjectMapper();
        assertFalse(m.isEnabled(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS));
        LinkedHashMap<String,Integer> map = new LinkedHashMap<String,Integer>();
        map.put("b", 3);
        map.put("a", 6);
        
        assertEquals("{\"b\":3,\"a\":6}", m.writeValueAsString(map));
        
        ObjectWriter sortingW =  m.writer(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS);
        assertEquals("{\"a\":6,\"b\":3}", sortingW.writeValueAsString(map));
    }

// com.fasterxml.jackson.databind.ser.TestMapSerialization::testOrderByWithNulls
    public void testOrderByWithNulls() throws IOException
    {
        ObjectWriter sortingW = MAPPER.writer(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS);
        
        Map<String,Integer> mapWithNullKey = new LinkedHashMap<String,Integer>();
        mapWithNullKey.put(null, 1);
        mapWithNullKey.put("b", 2);
        
        try {
             sortingW.writeValueAsString(mapWithNullKey);
            
        } catch (JsonMappingException e) {
            verifyException(e, "Null key for a Map not allowed");
        }
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

// com.fasterxml.jackson.databind.ser.TestMapSerialization::testConcurrentMaps
    public void testConcurrentMaps() throws Exception
    {
        final ObjectWriter w = MAPPER.writer(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS);

        Map<String,String> input = new ConcurrentSkipListMap<String,String>();
        input.put("x", "y");
        input.put("a", "b");
        String json = w.writeValueAsString(input);
        assertEquals(aposToQuotes("{'a':'b','x':'y'}"), json);

        input = new ConcurrentHashMap<String,String>();
        input.put("x", "y");
        input.put("a", "b");
        json = w.writeValueAsString(input);
        assertEquals(aposToQuotes("{'a':'b','x':'y'}"), json);

        
        input = new Hashtable<String,String>();
        input.put("x", "y");
        input.put("a", "b");
        json = w.writeValueAsString(input);
        assertEquals(aposToQuotes("{'a':'b','x':'y'}"), json);
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

        
        try {
            w.writeValueAsBytes(bean);
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

        assertEquals("{\"TestCommandParent\":{\"uuid\":\"1234\",\"type\":1}}", json);
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

// com.fasterxml.jackson.databind.ser.filter.IgnorePropsForSerTest::testExplicitIgnoralWithBean
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

// com.fasterxml.jackson.databind.ser.filter.IgnorePropsForSerTest::testExplicitIgnoralWithMap
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

// com.fasterxml.jackson.databind.ser.filter.IgnorePropsForSerTest::testIgnoreViaOnlyProps
    public void testIgnoreViaOnlyProps() throws Exception
    {
        assertEquals("{\"value\":{\"x\":1}}",
                MAPPER.writeValueAsString(new WrapperWithPropIgnore()));
    }

// com.fasterxml.jackson.databind.ser.filter.IgnorePropsForSerTest::testIgnoreViaPropForUntyped
    public void testIgnoreViaPropForUntyped() throws Exception
    {
        assertEquals("{\"value\":{\"z\":3}}",
                MAPPER.writeValueAsString(new WrapperWithPropIgnoreUntyped()));
    }

// com.fasterxml.jackson.databind.ser.filter.IgnorePropsForSerTest::testIgnoreWithMapProperty
    public void testIgnoreWithMapProperty() throws Exception
    {
        assertEquals("{\"value\":{\"b\":2}}", MAPPER.writeValueAsString(new MapWrapper()));
    }

// com.fasterxml.jackson.databind.ser.filter.IgnorePropsForSerTest::testIgnoreViaPropsAndClass
    public void testIgnoreViaPropsAndClass() throws Exception
    {
        assertEquals("{\"value\":{\"y\":2}}",
                MAPPER.writeValueAsString(new WrapperWithPropIgnore2()));
    }

// com.fasterxml.jackson.databind.ser.filter.IgnorePropsForSerTest::testIgnoreViaConfigOverride
    public void testIgnoreViaConfigOverride() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configOverride(Point.class)
            .setIgnorals(JsonIgnoreProperties.Value.forIgnoredProperties("x"));
        assertEquals("{\"y\":3}", mapper.writeValueAsString(new Point(2, 3)));
    }

// com.fasterxml.jackson.databind.ser.filter.IgnorePropsForSerTest::testIgnoreForListValues
    public void testIgnoreForListValues() throws Exception
    {
        
        assertEquals(aposToQuotes("{'coordinates':[{'y':2}]}"),
                MAPPER.writeValueAsString(new IgnoreForListValuesXY()));

        
        assertEquals(aposToQuotes("{'coordinates':[{'z':3}]}"),
                MAPPER.writeValueAsString(new IgnoreForListValuesXYZ()));
    }

// com.fasterxml.jackson.databind.ser.filter.JsonInclude1327Test::testClassDefaultsForEmpty
    public void testClassDefaultsForEmpty() throws Exception {
        ObjectMapper om = new ObjectMapper();
        om.setSerializationInclusion(JsonInclude.Include.NON_NULL);

        final String jsonString = om.writeValueAsString(new Issue1327BeanEmpty());

        if (jsonString.contains("myList")) {
            fail("Should not contain `myList`: "+jsonString);
        }
    }

// com.fasterxml.jackson.databind.ser.filter.JsonInclude1327Test::testClassDefaultsForAlways
    public void testClassDefaultsForAlways() throws Exception {
        ObjectMapper om = new ObjectMapper();
        om.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);

        final String jsonString = om.writeValueAsString(new Issue1327BeanAlways());

        if (!jsonString.contains("myList")) {
            fail("Should contain `myList` with Include.ALWAYS: "+jsonString);
        }
    }

// com.fasterxml.jackson.databind.ser.filter.JsonIncludeArrayTest::testByteArray
    public void testByteArray() throws IOException
    {
        assertEquals("{}", MAPPER.writeValueAsString(new NonEmptyByteArray()));
    }

// com.fasterxml.jackson.databind.ser.filter.JsonIncludeArrayTest::testShortArray
    public void testShortArray() throws IOException
    {
        assertEquals("{}", MAPPER.writeValueAsString(new NonEmptyShortArray()));
        assertEquals("{\"value\":[1]}", MAPPER.writeValueAsString(new NonEmptyShortArray((short) 1)));
    }

// com.fasterxml.jackson.databind.ser.filter.JsonIncludeArrayTest::testCharArray
    public void testCharArray() throws IOException
    {
        assertEquals("{}", MAPPER.writeValueAsString(new NonEmptyCharArray()));
        
        assertEquals("{\"value\":\"ab\"}", MAPPER.writeValueAsString(new NonEmptyCharArray('a', 'b')));
        
        assertEquals("{\"value\":[\"a\",\"b\"]}", MAPPER
                .writer().with(SerializationFeature.WRITE_CHAR_ARRAYS_AS_JSON_ARRAYS)
                .writeValueAsString(new NonEmptyCharArray('a', 'b')));
    }

// com.fasterxml.jackson.databind.ser.filter.JsonIncludeArrayTest::testIntArray
    public void testIntArray() throws IOException
    {
        assertEquals("{}", MAPPER.writeValueAsString(new NonEmptyIntArray()));
        assertEquals("{\"value\":[2]}", MAPPER.writeValueAsString(new NonEmptyIntArray(2)));
    }

// com.fasterxml.jackson.databind.ser.filter.JsonIncludeArrayTest::testLongArray
    public void testLongArray() throws IOException
    {
        assertEquals("{}", MAPPER.writeValueAsString(new NonEmptyLongArray()));
        assertEquals("{\"value\":[3,4]}", MAPPER.writeValueAsString(new NonEmptyLongArray(3, 4)));
    }

// com.fasterxml.jackson.databind.ser.filter.JsonIncludeArrayTest::testBooleanArray
    public void testBooleanArray() throws IOException
    {
        assertEquals("{}", MAPPER.writeValueAsString(new NonEmptyBooleanArray()));
        assertEquals("{\"value\":[true,false]}", MAPPER.writeValueAsString(new NonEmptyBooleanArray(true,false)));
    }

// com.fasterxml.jackson.databind.ser.filter.JsonIncludeArrayTest::testDoubleArray
    public void testDoubleArray() throws IOException
    {
        assertEquals("{}", MAPPER.writeValueAsString(new NonEmptyDoubleArray()));
        assertEquals("{\"value\":[0.25,-1.0]}", MAPPER.writeValueAsString(new NonEmptyDoubleArray(0.25,-1.0)));
    }

// com.fasterxml.jackson.databind.ser.filter.JsonIncludeArrayTest::testFloatArray
    public void testFloatArray() throws IOException
    {
        assertEquals("{}", MAPPER.writeValueAsString(new NonEmptyFloatArray()));
        assertEquals("{\"value\":[0.5]}", MAPPER.writeValueAsString(new NonEmptyFloatArray(0.5f)));
    }

// com.fasterxml.jackson.databind.ser.filter.JsonIncludeCollectionTest::testEnumSet
    public void testEnumSet() throws Exception
    {
        assertEquals("{}", MAPPER.writeValueAsString(new NonEmptyEnumSet()));
        assertEquals("{\"v\":[\"B\"]}", MAPPER.writeValueAsString(new NonEmptyEnumSet(ABC.B)));
    }

// com.fasterxml.jackson.databind.ser.filter.JsonIncludeCustomTest::testSimpleCustomFilter
    public void testSimpleCustomFilter() throws Exception
    {
        assertEquals(aposToQuotes("{'value':'x'}"), MAPPER.writeValueAsString(new FooBean("x")));
        assertEquals("{}", MAPPER.writeValueAsString(new FooBean("foo")));
    }

// com.fasterxml.jackson.databind.ser.filter.JsonIncludeCustomTest::testCustomFilterWithMap
    public void testCustomFilterWithMap() throws Exception
    {
        FooMapBean input = new FooMapBean()
                .add("a", "1")
                .add("b", "foo")
                .add("c", "2");
        
        assertEquals(aposToQuotes("{'stuff':{'a':'1','c':'2'}}"), MAPPER.writeValueAsString(input));
    }

// com.fasterxml.jackson.databind.ser.filter.JsonIncludeCustomTest::testBrokenFilter
    public void testBrokenFilter() throws Exception
    {
        try {
            String json = MAPPER.writeValueAsString(new BrokenBean("foo"));
            fail("Should not pass, produced: "+json);
        } catch (InvalidDefinitionException e) {
            verifyException(e, "Problem determining whether filter of type");
            verifyException(e, "filter out `null`");
        }
    }

// com.fasterxml.jackson.databind.ser.filter.JsonIncludeOverrideTest::testPropConfigOverridesForInclude
    public void testPropConfigOverridesForInclude() throws IOException
    {
        ObjectMapper mapper = new ObjectMapper();
        
        JsonIncludeOverrideTest.EmptyListMapBean empty = new JsonIncludeOverrideTest.EmptyListMapBean();
        assertEquals(aposToQuotes("{'list':[],'map':{}}"),
                mapper.writeValueAsString(empty));

        
        mapper = new ObjectMapper();
        mapper.configOverride(Map.class)
            .setInclude(JsonInclude.Value.construct(JsonInclude.Include.NON_EMPTY, null));
        assertEquals(aposToQuotes("{'list':[]}"),
                mapper.writeValueAsString(empty));

        mapper = new ObjectMapper();
        mapper.configOverride(List.class)
            .setInclude(JsonInclude.Value.construct(JsonInclude.Include.NON_EMPTY, null));
        assertEquals(aposToQuotes("{'map':{}}"),
                mapper.writeValueAsString(empty));
    }

// com.fasterxml.jackson.databind.ser.filter.JsonIncludeOverrideTest::testOverrideForIncludeAsPropertyNonNull
    public void testOverrideForIncludeAsPropertyNonNull() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        
        JsonIncludeOverrideTest.MixedTypeAlwaysBean nullValues = new JsonIncludeOverrideTest.MixedTypeAlwaysBean();
        assertEquals(aposToQuotes("{'num':null,'plain':null}"),
                mapper.writeValueAsString(nullValues));

        
        mapper = new ObjectMapper();
        mapper.configOverride(String.class)
                .setIncludeAsProperty(JsonInclude.Value
                        .construct(JsonInclude.Include.NON_NULL, null));
        assertEquals("{\"num\":null}",
                mapper.writeValueAsString(nullValues));

        mapper = new ObjectMapper();
        mapper.configOverride(Integer.class)
                .setIncludeAsProperty(JsonInclude.Value
                        .construct(JsonInclude.Include.NON_NULL, null));
        assertEquals("{\"plain\":null}",
                mapper.writeValueAsString(nullValues));
    }

// com.fasterxml.jackson.databind.ser.filter.JsonIncludeOverrideTest::testOverrideForIncludeAsPropertyAlways
    public void testOverrideForIncludeAsPropertyAlways() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        
        JsonIncludeOverrideTest.MixedTypeNonNullBean nullValues = new JsonIncludeOverrideTest.MixedTypeNonNullBean();
        assertEquals("{\"annotated\":null}",
                mapper.writeValueAsString(nullValues));

        
        mapper = new ObjectMapper();
        mapper.configOverride(String.class)
                .setIncludeAsProperty(JsonInclude.Value
                        .construct(JsonInclude.Include.ALWAYS, null));
        assertEquals(aposToQuotes("{'annotated':null,'plain':null}"),
                mapper.writeValueAsString(nullValues));

        mapper = new ObjectMapper();
        mapper.configOverride(Integer.class)
                .setIncludeAsProperty(JsonInclude.Value
                        .construct(JsonInclude.Include.ALWAYS, null));
        assertEquals(aposToQuotes("{'num':null,'annotated':null}"),
                mapper.writeValueAsString(nullValues));
    }

// com.fasterxml.jackson.databind.ser.filter.JsonIncludeOverrideTest::testOverridesForIncludeAndIncludeAsPropertyNonNull
    public void testOverridesForIncludeAndIncludeAsPropertyNonNull() throws Exception
    {
        
        JsonIncludeOverrideTest.MixedTypeNonNullBean nullValues = new JsonIncludeOverrideTest.MixedTypeNonNullBean();
        ObjectMapper mapper = new ObjectMapper();
        mapper.configOverride(JsonIncludeOverrideTest.MixedTypeNonNullBean.class)
                .setInclude(JsonInclude.Value
                        .construct(JsonInclude.Include.ALWAYS, null));
        assertEquals(aposToQuotes("{'num':null,'annotated':null,'plain':null}"),
                mapper.writeValueAsString(nullValues));

        
        mapper = new ObjectMapper();
        mapper.configOverride(JsonIncludeOverrideTest.MixedTypeNonNullBean.class)
                .setInclude(JsonInclude.Value
                        .construct(JsonInclude.Include.ALWAYS, null));
        mapper.configOverride(String.class)
                .setIncludeAsProperty(JsonInclude.Value
                        .construct(JsonInclude.Include.NON_NULL, null));
        assertEquals(aposToQuotes("{'num':null,'annotated':null}"),
                mapper.writeValueAsString(nullValues));

        mapper = new ObjectMapper();
        mapper.configOverride(JsonIncludeOverrideTest.MixedTypeNonNullBean.class)
                .setInclude(JsonInclude.Value
                        .construct(JsonInclude.Include.ALWAYS, null));
        mapper.configOverride(Integer.class)
                .setIncludeAsProperty(JsonInclude.Value
                        .construct(JsonInclude.Include.NON_NULL, null));
        assertEquals(aposToQuotes("{'annotated':null,'plain':null}"),
                mapper.writeValueAsString(nullValues));
    }

// com.fasterxml.jackson.databind.ser.filter.JsonIncludeOverrideTest::testOverridesForIncludeAndIncludeAsPropertyAlways
    public void testOverridesForIncludeAndIncludeAsPropertyAlways() throws Exception
    {
        
        JsonIncludeOverrideTest.MixedTypeAlwaysBean nullValues = new JsonIncludeOverrideTest.MixedTypeAlwaysBean();
        ObjectMapper mapper = new ObjectMapper();
        mapper.configOverride(JsonIncludeOverrideTest.MixedTypeAlwaysBean.class)
                .setInclude(JsonInclude.Value
                        .construct(JsonInclude.Include.NON_NULL, null));
        assertEquals("{}",
                mapper.writeValueAsString(nullValues));

        
        mapper = new ObjectMapper();
        mapper.configOverride(JsonIncludeOverrideTest.MixedTypeAlwaysBean.class)
                .setInclude(JsonInclude.Value
                        .construct(JsonInclude.Include.NON_NULL, null));
        mapper.configOverride(String.class)
                .setIncludeAsProperty(JsonInclude.Value
                        .construct(JsonInclude.Include.ALWAYS, null));
        assertEquals("{\"plain\":null}",
                mapper.writeValueAsString(nullValues));

        mapper = new ObjectMapper();
        mapper.configOverride(JsonIncludeOverrideTest.MixedTypeAlwaysBean.class)
                .setInclude(JsonInclude.Value
                        .construct(JsonInclude.Include.NON_NULL, null));
        mapper.configOverride(Integer.class)
                .setIncludeAsProperty(JsonInclude.Value
                        .construct(JsonInclude.Include.ALWAYS, null));
        assertEquals("{\"num\":null}",
                mapper.writeValueAsString(nullValues));
    }

// com.fasterxml.jackson.databind.ser.filter.JsonIncludeTest::testGlobal
    public void testGlobal() throws IOException
    {
        Map<String,Object> result = writeAndMap(MAPPER, new SimpleBean());
        assertEquals(2, result.size());
        assertEquals("a", result.get("a"));
        assertNull(result.get("b"));
        assertTrue(result.containsKey("b"));
    }

// com.fasterxml.jackson.databind.ser.filter.JsonIncludeTest::testNonNullByClass
    public void testNonNullByClass() throws IOException
    {
        Map<String,Object> result = writeAndMap(MAPPER, new NoNullsBean());
        assertEquals(1, result.size());
        assertFalse(result.containsKey("a"));
        assertNull(result.get("a"));
        assertTrue(result.containsKey("b"));
        assertNull(result.get("b"));
    }

// com.fasterxml.jackson.databind.ser.filter.JsonIncludeTest::testNonDefaultByClass
    public void testNonDefaultByClass() throws IOException
    {
        NonDefaultBean bean = new NonDefaultBean();
        
        bean._a = "notA";
        Map<String,Object> result = writeAndMap(MAPPER, bean);
        assertEquals(1, result.size());
        assertTrue(result.containsKey("a"));
        assertEquals("notA", result.get("a"));
        assertFalse(result.containsKey("b"));
        assertNull(result.get("b"));
    }

// com.fasterxml.jackson.databind.ser.filter.JsonIncludeTest::testNonDefaultByClassNoCtor
    public void testNonDefaultByClassNoCtor() throws IOException
    {
        NonDefaultBeanXYZ bean = new NonDefaultBeanXYZ(1, 2, 0);
        String json = MAPPER.writeValueAsString(bean);
        assertEquals(aposToQuotes("{'x':1,'y':2}"), json);
    }

// com.fasterxml.jackson.databind.ser.filter.JsonIncludeTest::testMixedMethod
    public void testMixedMethod() throws IOException
    {
        MixedBean bean = new MixedBean();
        bean._a = "xyz";
        bean._b = null;
        Map<String,Object> result = writeAndMap(MAPPER, bean);
        assertEquals(1, result.size());
        assertEquals("xyz", result.get("a"));
        assertFalse(result.containsKey("b"));

        bean._a = "a";
        bean._b = "b";
        result = writeAndMap(MAPPER, bean);
        assertEquals(1, result.size());
        assertEquals("b", result.get("b"));
        assertFalse(result.containsKey("a"));
    }

// com.fasterxml.jackson.databind.ser.filter.JsonIncludeTest::testDefaultForEmptyList
    public void testDefaultForEmptyList() throws IOException
    {
        assertEquals("{}", MAPPER.writeValueAsString(new ListBean()));
    }

// com.fasterxml.jackson.databind.ser.filter.JsonIncludeTest::testNonEmptyDefaultArray
    public void testNonEmptyDefaultArray() throws IOException
    {
        assertEquals("{}", MAPPER.writeValueAsString(new ArrayBean()));
    }

// com.fasterxml.jackson.databind.ser.filter.JsonIncludeTest::testDefaultForIntegers
    public void testDefaultForIntegers() throws IOException
    {
        assertEquals("{}", MAPPER.writeValueAsString(new DefaultIntBean(0, Integer.valueOf(0))));
        assertEquals("{\"i2\":1}", MAPPER.writeValueAsString(new DefaultIntBean(0, Integer.valueOf(1))));
        assertEquals("{\"i1\":3}", MAPPER.writeValueAsString(new DefaultIntBean(3, Integer.valueOf(0))));
    }

// com.fasterxml.jackson.databind.ser.filter.JsonIncludeTest::testEmptyInclusionScalars
    public void testEmptyInclusionScalars() throws IOException
    {
        ObjectMapper defMapper = MAPPER;
        ObjectMapper inclMapper = new ObjectMapper().setSerializationInclusion(JsonInclude.Include.NON_EMPTY);

        
        StringWrapper str = new StringWrapper("");
        assertEquals("{\"str\":\"\"}", defMapper.writeValueAsString(str));
        assertEquals("{}", inclMapper.writeValueAsString(str));
        assertEquals("{}", inclMapper.writeValueAsString(new StringWrapper()));

        assertEquals("{\"value\":\"x\"}", defMapper.writeValueAsString(new NonEmptyString("x")));
        assertEquals("{}", defMapper.writeValueAsString(new NonEmptyString("")));

        
        
        
        assertEquals("{\"value\":12}", defMapper.writeValueAsString(new NonEmptyInt(12)));
        assertEquals("{\"value\":0}", defMapper.writeValueAsString(new NonEmptyInt(0)));

        assertEquals("{\"value\":1.25}", defMapper.writeValueAsString(new NonEmptyDouble(1.25)));
        assertEquals("{\"value\":0.0}", defMapper.writeValueAsString(new NonEmptyDouble(0.0)));

        IntWrapper zero = new IntWrapper(0);
        assertEquals("{\"i\":0}", defMapper.writeValueAsString(zero));
        assertEquals("{\"i\":0}", inclMapper.writeValueAsString(zero));
    }

// com.fasterxml.jackson.databind.ser.filter.JsonIncludeTest::testIssue1351
    public void testIssue1351() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setSerializationInclusion(JsonInclude.Include.NON_DEFAULT);
        assertEquals(aposToQuotes("{}"),
                mapper.writeValueAsString(new Issue1351Bean(null, (double) 0)));
        
        assertEquals(aposToQuotes("{}"),
                mapper.writeValueAsString(new Issue1351NonBean(0)));
    }

// com.fasterxml.jackson.databind.ser.filter.JsonIncludeTest::testInclusionOfDate
    public void testInclusionOfDate() throws Exception
    {
        final Date input = new Date(0L);
        assertEquals(aposToQuotes("{'value':0}"), 
                MAPPER.writeValueAsString(new NonEmptyDate(input)));
        assertEquals("{}", 
                MAPPER.writeValueAsString(new NonDefaultDate(input)));

    
    }

// com.fasterxml.jackson.databind.ser.filter.JsonIncludeTest::testInclusionOfCalendar
    public void testInclusionOfCalendar() throws Exception
    {
        final Calendar input = new GregorianCalendar();
        input.setTimeInMillis(0L);
        assertEquals(aposToQuotes("{'value':0}"), 
                MAPPER.writeValueAsString(new NonEmptyCalendar(input)));
        assertEquals("{}", 
                MAPPER.writeValueAsString(new NonDefaultCalendar(input)));
    }

// com.fasterxml.jackson.databind.ser.filter.MapInclusionTest::testNonEmptyValueMapViaProp
    public void testNonEmptyValueMapViaProp() throws IOException
    {
        String json = MAPPER.writeValueAsString(new NoEmptiesMapContainer()
            .add("a", null)
            .add("b", ""));
        assertEquals(aposToQuotes("{}"), json);
    }

// com.fasterxml.jackson.databind.ser.filter.MapInclusionTest::testNoNullsMap
    public void testNoNullsMap() throws IOException
    {
        NoNullsMapContainer input = new NoNullsMapContainer()
                .add("a", null)
                .add("b", "");
        String json = MAPPER.writeValueAsString(input);
        assertEquals(aposToQuotes("{'stuff':{'b':''}}"), json);
    }

// com.fasterxml.jackson.databind.ser.filter.MapInclusionTest::testNonEmptyNoNullsMap
    public void testNonEmptyNoNullsMap() throws IOException
    {
        NoNullsNotEmptyMapContainer input = new NoNullsNotEmptyMapContainer()
                .add("a", null)
                .add("b", "");
        String json = MAPPER.writeValueAsString(input);
        assertEquals(aposToQuotes("{'stuff':{'b':''}}"), json);

        json = MAPPER.writeValueAsString(new NoNullsNotEmptyMapContainer()
                .add("a", null)
                .add("b", null));
        assertEquals(aposToQuotes("{}"), json);
    }

// com.fasterxml.jackson.databind.ser.filter.NullSerializationTest::testSimple
    public void testSimple() throws Exception
    {
        assertEquals("null", MAPPER.writeValueAsString(null));
    }

// com.fasterxml.jackson.databind.ser.filter.NullSerializationTest::testOverriddenDefaultNulls
    public void testOverriddenDefaultNulls() throws Exception
    {
        DefaultSerializerProvider sp = new DefaultSerializerProvider.Impl();
        sp.setNullValueSerializer(new NullSerializer());
        ObjectMapper m = new ObjectMapper();
        m.setSerializerProvider(sp);
        assertEquals("\"foobar\"", m.writeValueAsString(null));
    }

// com.fasterxml.jackson.databind.ser.filter.NullSerializationTest::testCustomNulls
    public void testCustomNulls() throws Exception
    {
        ObjectMapper m = new ObjectMapper();
        m.setSerializerProvider(new MyNullProvider());
        assertEquals("{\"name\":\"foobar\"}", m.writeValueAsString(new Bean1()));
        assertEquals("{\"type\":null}", m.writeValueAsString(new Bean2()));
    }

// com.fasterxml.jackson.databind.ser.filter.NullSerializationTest::testCustomNullForTrees
    public void testCustomNullForTrees() throws Exception
    {
        ObjectNode root = MAPPER.createObjectNode();
        root.putNull("a");

        
        assertEquals("{\"a\":null}", MAPPER.writeValueAsString(root));

        
        DefaultSerializerProvider prov = new MyNullProvider();
        prov.setNullValueSerializer(new NullSerializer());
        ObjectMapper m = new ObjectMapper();
        m.setSerializerProvider(prov);
        assertEquals("{\"a\":\"foobar\"}", m.writeValueAsString(root));
    }

// com.fasterxml.jackson.databind.ser.filter.NullSerializationTest::testNullSerializerForProperty
    public void testNullSerializerForProperty() throws Exception
    {
        assertEquals("{\"a\":\"foobar\"}", MAPPER.writeValueAsString(new BeanWithNullProps()));
    }

// com.fasterxml.jackson.databind.ser.filter.TestAnyGetterFiltering::testAnyGetterFiltering
    public void testAnyGetterFiltering() throws Exception
    {
        FilterProvider prov = new SimpleFilterProvider().addFilter("anyFilter",
                SimpleBeanPropertyFilter.filterOutAllExcept("b"));
        assertEquals("{\"b\":\"2\"}", MAPPER.writer(prov).writeValueAsString(new AnyBean()));
    }

// com.fasterxml.jackson.databind.ser.filter.TestAnyGetterFiltering::testAnyGetterIgnore
    public void testAnyGetterIgnore() throws Exception
    {
        assertEquals(aposToQuotes("{'a':'1','b':'3'}"),
                MAPPER.writeValueAsString(new AnyBeanWithIgnores()));
    }

// com.fasterxml.jackson.databind.ser.filter.TestAnyGetterFiltering::testAnyGetterPojo1655
    public void testAnyGetterPojo1655() throws Exception
    {
        FilterProvider filters = new SimpleFilterProvider().addFilter("CustomFilter", new CustomFilter());
        String json = MAPPER.writer(filters).writeValueAsString(new OuterObject());
        Map<?,?> stuff = MAPPER.readValue(json, Map.class);
        if (stuff.size() != 2) {
            fail("Should have 2 properties, got: "+stuff);
        }
   }

// com.fasterxml.jackson.databind.ser.filter.TestIgnoredTypes::testIgnoredType
    public void testIgnoredType() throws Exception
    {
        final ObjectMapper mapper = objectMapper();

        
        NonIgnoredType bean = mapper.readValue("{\"value\":13}", NonIgnoredType.class);
        assertNotNull(bean);
        assertEquals(13, bean.value);

        
        bean = mapper.readValue("{ \"ignored\":[1,2,{}], \"value\":9 }", NonIgnoredType.class);
        assertNotNull(bean);
        assertEquals(9, bean.value);
    }

// com.fasterxml.jackson.databind.ser.filter.TestIgnoredTypes::testSingleWithMixins
    public void testSingleWithMixins() throws Exception {
        SimpleModule module = new SimpleModule();
        module.setMixInAnnotation(Person.class, PersonMixin.class);
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(module);
        PersonWrapper input = new PersonWrapper();
        String json = mapper.writeValueAsString(input);
        assertEquals("{\"value\":1}", json);
    }

// com.fasterxml.jackson.databind.ser.filter.TestIgnoredTypes::testListWithMixins
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

// com.fasterxml.jackson.databind.ser.filter.TestIgnoredTypes::testIgnoreUsingConfigOverride
    public void testIgnoreUsingConfigOverride() throws Exception
    {
        final ObjectMapper mapper = objectMapper();
        mapper.configOverride(Wrapped.class).setIsIgnoredType(true);

        
        String json = mapper.writeValueAsString(new Wrapper());
        assertEquals(aposToQuotes("{'value':3}"), json);

        
        Wrapper result = mapper.readValue(aposToQuotes("{'value':5,'wrapped':false}"),
                Wrapper.class);
        assertEquals(5, result.value);
    }

// com.fasterxml.jackson.databind.ser.filter.TestJsonFilter::testCheckSiblingContextFilter
    public void testCheckSiblingContextFilter() {
        FilterProvider prov = new SimpleFilterProvider().addFilter("checkSiblingContextFilter",
                new CheckSiblingContextFilter());

        ObjectMapper mapper = new ObjectMapper();
        mapper.setFilterProvider(prov);
        mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        mapper.valueToTree(new CheckSiblingContextBean());
    }

// com.fasterxml.jackson.databind.ser.filter.TestJsonFilter::testSimpleInclusionFilter
    public void testSimpleInclusionFilter() throws Exception
    {
        FilterProvider prov = new SimpleFilterProvider().addFilter("RootFilter",
                SimpleBeanPropertyFilter.filterOutAllExcept("a"));
        assertEquals("{\"a\":\"a\"}", MAPPER.writer(prov).writeValueAsString(new Bean()));

        
        ObjectMapper mapper = new ObjectMapper();
        mapper.setFilterProvider(prov);
        assertEquals("{\"a\":\"a\"}", mapper.writeValueAsString(new Bean()));
    }

// com.fasterxml.jackson.databind.ser.filter.TestJsonFilter::testIncludeAllFilter
    public void testIncludeAllFilter() throws Exception
    {
        FilterProvider prov = new SimpleFilterProvider().addFilter("RootFilter",
                SimpleBeanPropertyFilter.serializeAll());
        assertEquals("{\"a\":\"a\",\"b\":\"b\"}", MAPPER.writer(prov).writeValueAsString(new Bean()));
    }

// com.fasterxml.jackson.databind.ser.filter.TestJsonFilter::testSimpleExclusionFilter
    public void testSimpleExclusionFilter() throws Exception
    {
        FilterProvider prov = new SimpleFilterProvider().addFilter("RootFilter",
                SimpleBeanPropertyFilter.serializeAllExcept("a"));
        assertEquals("{\"b\":\"b\"}", MAPPER.writer(prov).writeValueAsString(new Bean()));
    }

// com.fasterxml.jackson.databind.ser.filter.TestJsonFilter::testMissingFilter
    public void testMissingFilter() throws Exception
    {
        
        try {
            MAPPER.writeValueAsString(new Bean());
            fail("Should have failed without configured filter");
        } catch (JsonMappingException e) { 
            verifyException(e, "Cannot resolve PropertyFilter with id 'RootFilter'");
        }
        
        
        SimpleFilterProvider fp = new SimpleFilterProvider().setFailOnUnknownId(false);
        ObjectMapper mapper = new ObjectMapper();
        mapper.setFilterProvider(fp);
        String json = mapper.writeValueAsString(new Bean());
        assertEquals("{\"a\":\"a\",\"b\":\"b\"}", json);
    }

// com.fasterxml.jackson.databind.ser.filter.TestJsonFilter::testDefaultFilter
    public void testDefaultFilter() throws Exception
    {
        FilterProvider prov = new SimpleFilterProvider().setDefaultFilter(SimpleBeanPropertyFilter.filterOutAllExcept("b"));
        assertEquals("{\"b\":\"b\"}", MAPPER.writer(prov).writeValueAsString(new Bean()));
    }

// com.fasterxml.jackson.databind.ser.filter.TestJsonFilter::testIssue89
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

// com.fasterxml.jackson.databind.ser.filter.TestJsonFilter::testFilterOnProperty
    public void testFilterOnProperty() throws Exception
    {
        FilterProvider prov = new SimpleFilterProvider()
            .addFilter("RootFilter", SimpleBeanPropertyFilter.filterOutAllExcept("a"))
            .addFilter("b", SimpleBeanPropertyFilter.filterOutAllExcept("b"));

        assertEquals("{\"first\":{\"a\":\"a\"},\"second\":{\"b\":\"b\"}}",
                MAPPER.writer(prov).writeValueAsString(new FilteredProps()));
    }

// com.fasterxml.jackson.databind.ser.filter.TestMapFiltering::testMapFilteringViaProps
    public void testMapFilteringViaProps() throws Exception
    {
        FilterProvider prov = new SimpleFilterProvider().addFilter("filterX",
                SimpleBeanPropertyFilter.filterOutAllExcept("b"));
        String json = MAPPER.writer(prov).writeValueAsString(new MapBean());
        assertEquals(aposToQuotes("{'values':{'b':5}}"), json);
    }

// com.fasterxml.jackson.databind.ser.filter.TestMapFiltering::testMapFilteringViaClass
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

// com.fasterxml.jackson.databind.ser.filter.TestMapFiltering::testNonNullValueMapViaProp
    public void testNonNullValueMapViaProp() throws IOException
    {
        String json = MAPPER.writeValueAsString(new NoNullValuesMapContainer()
            .add("a", "foo")
            .add("b", null)
            .add("c", "bar"));
        assertEquals(aposToQuotes("{'stuff':{'a':'foo','c':'bar'}}"), json);
    }

// com.fasterxml.jackson.databind.ser.filter.TestMapFiltering::testMapFilteringWithAnnotations
    public void testMapFilteringWithAnnotations() throws Exception
    {
        FilterProvider prov = new SimpleFilterProvider().addFilter("filterX",
                new TestMapFilter());
        String json = MAPPER.writer(prov).writeValueAsString(new MapBean());
        
        assertEquals(aposToQuotes("{'values':{'a':2}}"), json);

        
        json = MAPPER.writer(prov).writeValueAsString(new MapBeanNoOffset());
        assertEquals(aposToQuotes("{'values':{'a':1}}"), json);
    }

// com.fasterxml.jackson.databind.ser.filter.TestMapFiltering::testMapNonNullValue
    public void testMapNonNullValue() throws IOException
    {
        String json = MAPPER.writeValueAsString(new NoNullsStringMap()
            .add("a", "foo")
            .add("b", null)
            .add("c", "bar"));
        assertEquals(aposToQuotes("{'a':'foo','c':'bar'}"), json);
    }

// com.fasterxml.jackson.databind.ser.filter.TestMapFiltering::testMapNonEmptyValue
    public void testMapNonEmptyValue() throws IOException
    {
        String json = MAPPER.writeValueAsString(new NoEmptyStringsMap()
            .add("a", "foo")
            .add("b", "bar")
            .add("c", ""));
        assertEquals(aposToQuotes("{'a':'foo','b':'bar'}"), json);
    }

// com.fasterxml.jackson.databind.ser.filter.TestMapFiltering::testMapAbsentValue
    public void testMapAbsentValue() throws IOException
    {
        String json = MAPPER.writeValueAsString(new NoAbsentStringMap()
            .add("a", "foo")
            .add("b", null));
        assertEquals(aposToQuotes("{'a':'foo'}"), json);
    }

// com.fasterxml.jackson.databind.ser.filter.TestMapFiltering::testMapNullSerialization
    public void testMapNullSerialization() throws IOException
    {
        ObjectMapper m = new ObjectMapper();
        Map<String,String> map = new HashMap<String,String>();
        map.put("a", null);
        
        assertEquals("{\"a\":null}", m.writeValueAsString(map));
        

        m = new ObjectMapper();        
        m.disable(SerializationFeature.WRITE_NULL_MAP_VALUES);
        assertEquals("{}", m.writeValueAsString(map));
    }

// com.fasterxml.jackson.databind.ser.filter.TestMapFiltering::testMapWithOnlyEmptyValues
    public void testMapWithOnlyEmptyValues() throws IOException
    {
        String json;

        
        json = MAPPER.writeValueAsString(new Wrapper497(new StringMap497()
            .add("a", "123")));
        assertEquals(aposToQuotes("{'values':{'a':'123'}}"), json);

        
        json = MAPPER.writeValueAsString(new Wrapper497(new StringMap497()
            .add("a", "")
            .add("b", null)));
        assertEquals(aposToQuotes("{}"), json);
    }

// com.fasterxml.jackson.databind.ser.filter.TestMapFiltering::testMapViaGlobalNonEmpty
    public void testMapViaGlobalNonEmpty() throws Exception
    {
        
        ObjectMapper mapper = new ObjectMapper();
        mapper.setDefaultPropertyInclusion(JsonInclude.Value.empty()
                .withContentInclusion(JsonInclude.Include.NON_EMPTY));
        assertEquals(aposToQuotes("{'a':'b'}"), mapper.writeValueAsString(
                new StringMap497()
                    .add("x", "")
                    .add("a", "b")
                    ));
    }

// com.fasterxml.jackson.databind.ser.filter.TestMapFiltering::testMapViaTypeOverride
    public void testMapViaTypeOverride() throws Exception
    {
        
        ObjectMapper mapper = new ObjectMapper();
        mapper.configOverride(Map.class)
            .setInclude(JsonInclude.Value.empty()
                .withContentInclusion(JsonInclude.Include.NON_EMPTY));
        assertEquals(aposToQuotes("{'a':'b'}"), mapper.writeValueAsString(
                new StringMap497()
                    .add("foo", "")
                    .add("a", "b")
                    ));
    }

// com.fasterxml.jackson.databind.ser.filter.TestSimpleSerializationIgnore::testSimpleIgnore
    public void testSimpleIgnore() throws Exception
    {
        
        Map<String,Object> result = writeAndMap(MAPPER, new SizeClassEnabledIgnore());
        assertEquals(1, result.size());
        assertEquals(Integer.valueOf(1), result.get("x"));
        assertNull(result.get("y"));
    }

// com.fasterxml.jackson.databind.ser.filter.TestSimpleSerializationIgnore::testDisabledIgnore
    public void testDisabledIgnore() throws Exception
    {
        
        Map<String,Object> result = writeAndMap(MAPPER, new SizeClassDisabledIgnore());
        assertEquals(2, result.size());
        assertEquals(Integer.valueOf(3), result.get("x"));
        assertEquals(Integer.valueOf(4), result.get("y"));
    }

// com.fasterxml.jackson.databind.ser.filter.TestSimpleSerializationIgnore::testIgnoreOver
    public void testIgnoreOver() throws Exception
    {
        
        Map<String,Object> result = writeAndMap(MAPPER, new BaseClassIgnore());
        assertEquals(1, result.size());
        assertEquals(Integer.valueOf(2), result.get("y"));

        
        result = writeAndMap(MAPPER, new SubClassNonIgnore());
        assertEquals(2, result.size());
        assertEquals(Integer.valueOf(3), result.get("x"));
        assertEquals(Integer.valueOf(2), result.get("y"));
    }

// com.fasterxml.jackson.databind.ser.filter.TestSimpleSerializationIgnore::testIgnoreType
    public void testIgnoreType() throws Exception
    {
        assertEquals("{\"value\":13}", MAPPER.writeValueAsString(new NonIgnoredType()));
    }

// com.fasterxml.jackson.databind.ser.jdk.AtomicTypeSerializationTest::testAtomicBoolean
    public void testAtomicBoolean() throws Exception
    {
        assertEquals("true", MAPPER.writeValueAsString(new AtomicBoolean(true)));
        assertEquals("false", MAPPER.writeValueAsString(new AtomicBoolean(false)));
    }

// com.fasterxml.jackson.databind.ser.jdk.AtomicTypeSerializationTest::testAtomicInteger
    public void testAtomicInteger() throws Exception
    {
        assertEquals("1", MAPPER.writeValueAsString(new AtomicInteger(1)));
        assertEquals("-9", MAPPER.writeValueAsString(new AtomicInteger(-9)));
    }

// com.fasterxml.jackson.databind.ser.jdk.AtomicTypeSerializationTest::testAtomicLong
    public void testAtomicLong() throws Exception
    {
        assertEquals("0", MAPPER.writeValueAsString(new AtomicLong(0)));
    }

// com.fasterxml.jackson.databind.ser.jdk.AtomicTypeSerializationTest::testAtomicReference
    public void testAtomicReference() throws Exception
    {
        String[] strs = new String[] { "abc" };
        assertEquals("[\"abc\"]", MAPPER.writeValueAsString(new AtomicReference<String[]>(strs)));
    }

// com.fasterxml.jackson.databind.ser.jdk.AtomicTypeSerializationTest::testCustomSerializer
    public void testCustomSerializer() throws Exception
    {
        final String VALUE = "fooBAR";
        String json = MAPPER.writeValueAsString(new UCStringWrapper(VALUE));
        assertEquals(json, aposToQuotes("{'value':'FOOBAR'}"));
    }

// com.fasterxml.jackson.databind.ser.jdk.AtomicTypeSerializationTest::testContextualAtomicReference
    public void testContextualAtomicReference() throws Exception
    {
        SimpleDateFormat df = new SimpleDateFormat("yyyy/MM/dd");
        df.setTimeZone(TimeZone.getTimeZone("UTC"));
        final ObjectMapper mapper = objectMapper();
        mapper.setDateFormat(df);
        ContextualOptionals input = new ContextualOptionals();
        input.date = new AtomicReference<>(new Date(0L));
        input.date1 = new AtomicReference<>(new Date(0L));
        input.date2 = new AtomicReference<>(new Date(0L));
        final String json = mapper.writeValueAsString(input);
        assertEquals(aposToQuotes(
                "{'date1':'1970+01+01','date2':'1970*01*01','date':'1970/01/01'}"),
                json);
    }

// com.fasterxml.jackson.databind.ser.jdk.AtomicTypeSerializationTest::testPolymorphicReferenceSimple
    public void testPolymorphicReferenceSimple() throws Exception
    {
        final String EXPECTED = "{\"type\":\"Foo\",\"foo\":42}";
        String json = MAPPER.writeValueAsString(new ContainerA());
        assertEquals("{\"strategy\":" + EXPECTED + "}", json);
    }

// com.fasterxml.jackson.databind.ser.jdk.AtomicTypeSerializationTest::testPolymorphicReferenceListOf
    public void testPolymorphicReferenceListOf() throws Exception
    {
        final String EXPECTED = "{\"type\":\"Foo\",\"foo\":42}";
        
        
        String json = MAPPER.writeValueAsString(new ContainerB());
        assertEquals("{\"strategy\":[" + EXPECTED + "]}", json);
    }

// com.fasterxml.jackson.databind.ser.jdk.CollectionSerializationTest::testCollections
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

// com.fasterxml.jackson.databind.ser.jdk.CollectionSerializationTest::testBigCollection
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

// com.fasterxml.jackson.databind.ser.jdk.CollectionSerializationTest::testEnumMap
    public void testEnumMap() throws IOException
    {
        EnumMap<Key,String> map = new EnumMap<Key,String>(Key.class);
        map.put(Key.B, "xyz");
        map.put(Key.C, "abc");
        
        String json = MAPPER.writeValueAsString(map);
        assertEquals("{\"B\":\"xyz\",\"C\":\"abc\"}",json.trim());
    }

// com.fasterxml.jackson.databind.ser.jdk.CollectionSerializationTest::testEmptyBeanCollection
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

// com.fasterxml.jackson.databind.ser.jdk.CollectionSerializationTest::testNullBeanCollection
    public void testNullBeanCollection()
        throws IOException
    {
        CollectionBean cb = new CollectionBean(null);
        Map<String,Object> result = writeAndMap(MAPPER, cb);
        assertEquals(1, result.size());
        assertTrue(result.containsKey("values"));
        assertNull(result.get("values"));
    }

// com.fasterxml.jackson.databind.ser.jdk.CollectionSerializationTest::testEmptyBeanEnumMap
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

// com.fasterxml.jackson.databind.ser.jdk.CollectionSerializationTest::testNullBeanEnumMap
    public void testNullBeanEnumMap() throws IOException
    {
        EnumMapBean b = new EnumMapBean(null);
        Map<String,Object> result = writeAndMap(MAPPER, b);

        assertEquals(1, result.size());
        assertTrue(result.containsKey("map"));
        assertNull(result.get("map"));
    }

// com.fasterxml.jackson.databind.ser.jdk.CollectionSerializationTest::testListSerializer
    public void testListSerializer() throws IOException
    {
        assertEquals(quote("[ab, cd, ef]"),
                MAPPER.writeValueAsString(new PseudoList("ab", "cd", "ef")));
        assertEquals(quote("[]"),
                MAPPER.writeValueAsString(new PseudoList()));
    }

// com.fasterxml.jackson.databind.ser.jdk.CollectionSerializationTest::testEmptyListOrArray
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

// com.fasterxml.jackson.databind.ser.jdk.CollectionSerializationTest::testStaticList
    public void testStaticList() throws IOException
    {
        
        StaticListWrapper w = new StaticListWrapper("a", "b", "c");
        String json = MAPPER.writeValueAsString(w);
        assertEquals(aposToQuotes("{'list':['a','b','c']}"), json);

        
        ObjectMapper mapper = new ObjectMapper();
        mapper.enableDefaultTyping(DefaultTyping.NON_FINAL);
        json = mapper.writeValueAsString(w);
        assertEquals(aposToQuotes(String.format("['%s',{'list':['%s',['a','b','c']]}]",
                w.getClass().getName(), w.list.getClass().getName())), json);
    }

// com.fasterxml.jackson.databind.ser.jdk.DateSerializationTest::testDateNumeric
    public void testDateNumeric() throws IOException
    {
        
        assertTrue(MAPPER.isEnabled(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS));
        
        String json = MAPPER.writeValueAsString(new Date(199L));
        assertEquals("199", json);
    }

// com.fasterxml.jackson.databind.ser.jdk.DateSerializationTest::testDateISO8601
    public void testDateISO8601() throws IOException
    {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);

        serialize(mapper, judate(1970, 1, 1,  02, 00, 00, 0, "GMT+2"), "1970-01-01T00:00:00.000+0000");
        serialize(mapper, judate(1970, 1, 1,  00, 00, 00, 0, "UTC"),   "1970-01-01T00:00:00.000+0000");

        
        serialize(mapper, judate(911, 1, 1,  00, 00, 00, 0, "UTC"),   "0911-01-01T00:00:00.000+0000");
        serialize(mapper, judate(87, 1, 1,  00, 00, 00, 0, "UTC"),   "0087-01-01T00:00:00.000+0000");
        serialize(mapper, judate(1, 1, 1,  00, 00, 00, 0, "UTC"),   "0001-01-01T00:00:00.000+0000");
    }

// com.fasterxml.jackson.databind.ser.jdk.DateSerializationTest::testDateISO8601_10k
    public void testDateISO8601_10k() throws IOException
    {
        ObjectWriter w = MAPPER.writer()
                .without(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        serialize(w, judate(10204, 1, 1,  00, 00, 00, 0, "UTC"),   "+10204-01-01T00:00:00.000+0000");
        
        serialize(w, judate(123456, 1, 1,  00, 00, 00, 0, "UTC"),   "+123456-01-01T00:00:00.000+0000");
    }

// com.fasterxml.jackson.databind.ser.jdk.DateSerializationTest::testDateISO8601_BCE
    public void testDateISO8601_BCE() throws IOException
    {
        ObjectWriter w = MAPPER.writer()
                .without(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        
        

        serialize(w, judate(0, 1, 1,  00, 00, 00, 0, "UTC"),   "+0000-01-01T00:00:00.000+0000");
        serialize(w, judate(-1, 1, 1,  00, 00, 00, 0, "UTC"),   "-0001-01-01T00:00:00.000+0000");
        serialize(w, judate(-49, 1, 1,  00, 00, 00, 0, "UTC"),   "-0049-01-01T00:00:00.000+0000"); 
        serialize(w, judate(-264, 1, 1,  00, 00, 00, 0, "UTC"),   "-0264-01-01T00:00:00.000+0000"); 
    }

// com.fasterxml.jackson.databind.ser.jdk.DateSerializationTest::testDateISO8601_customTZ
    public void testDateISO8601_customTZ() throws IOException
    {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        mapper.setTimeZone(TimeZone.getTimeZone("GMT+2"));

        serialize( mapper, judate(1970, 1, 1,  00, 00, 00, 0, "GMT+2"), "1970-01-01T00:00:00.000+0200");
        serialize( mapper, judate(1970, 1, 1,  00, 00, 00, 0, "UTC"),   "1970-01-01T02:00:00.000+0200");
    }

// com.fasterxml.jackson.databind.ser.jdk.DateSerializationTest::testDateISO8601_colonInTZ
    public void testDateISO8601_colonInTZ() throws IOException
    {
        StdDateFormat dateFormat = new StdDateFormat();
        assertFalse(dateFormat.isColonIncludedInTimeZone());
        dateFormat = dateFormat.withColonInTimeZone(true);
        assertTrue(dateFormat.isColonIncludedInTimeZone());

        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        mapper.setDateFormat(dateFormat);
        
        serialize( mapper, judate(1970, 1, 1,  02, 00, 00, 0, "GMT+2"), "1970-01-01T00:00:00.000+00:00");
        serialize( mapper, judate(1970, 1, 1,  00, 00, 00, 0, "UTC"),   "1970-01-01T00:00:00.000+00:00");
    }

// com.fasterxml.jackson.databind.ser.jdk.DateSerializationTest::testDateOther
    public void testDateOther() throws IOException
    {
        ObjectMapper mapper = new ObjectMapper();
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd'X'HH:mm:ss");
        mapper.setDateFormat(df);
        mapper.setTimeZone(TimeZone.getTimeZone("PST"));

        
        serialize( mapper, judate(1970, 1, 1,  00, 00, 00, 0, "UTC"), "1969-12-31X16:00:00");
    }

// com.fasterxml.jackson.databind.ser.jdk.DateSerializationTest::testTimeZone
    public void testTimeZone() throws IOException
    {
        TimeZone input = TimeZone.getTimeZone("PST");
        String json = MAPPER.writeValueAsString(input);
        assertEquals(quote("PST"), json);
    }

// com.fasterxml.jackson.databind.ser.jdk.DateSerializationTest::testTimeZoneInBean
    public void testTimeZoneInBean() throws IOException
    {
        String json = MAPPER.writeValueAsString(new TimeZoneBean("PST"));
        assertEquals("{\"tz\":\"PST\"}", json);
    }

// com.fasterxml.jackson.databind.ser.jdk.DateSerializationTest::testDateUsingObjectWriter
    public void testDateUsingObjectWriter() throws IOException
    {
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd'X'HH:mm:ss");
        TimeZone tz = TimeZone.getTimeZone("PST");
        assertEquals(quote("1969-12-31X16:00:00"),
                MAPPER.writer(df)
                    .with(tz)
                    .writeValueAsString(new Date(0L)));
        ObjectWriter w = MAPPER.writer((DateFormat)null);
        assertEquals("0", w.writeValueAsString(new Date(0L)));

        w = w.with(df).with(tz);
        assertEquals(quote("1969-12-31X16:00:00"), w.writeValueAsString(new Date(0L)));
        w = w.with((DateFormat) null);
        assertEquals("0", w.writeValueAsString(new Date(0L)));
    }

// com.fasterxml.jackson.databind.ser.jdk.DateSerializationTest::testDatesAsMapKeys
    public void testDatesAsMapKeys() throws IOException
    {
        ObjectMapper mapper = new ObjectMapper();
        Map<Date,Integer> map = new HashMap<Date,Integer>();
        assertFalse(mapper.isEnabled(SerializationFeature.WRITE_DATE_KEYS_AS_TIMESTAMPS));
        map.put(new Date(0L), Integer.valueOf(1));
        
        assertEquals("{\"1970-01-01T00:00:00.000+0000\":1}", mapper.writeValueAsString(map));
        
        
        mapper.configure(SerializationFeature.WRITE_DATE_KEYS_AS_TIMESTAMPS, true);
        assertEquals("{\"0\":1}", mapper.writeValueAsString(map));
    }

// com.fasterxml.jackson.databind.ser.jdk.DateSerializationTest::testDateWithJsonFormat
    public void testDateWithJsonFormat() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        String json;

        
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        json = mapper.writeValueAsString(new DateAsNumberBean(0L));
        assertEquals(aposToQuotes("{'date':0}"), json);

        
        mapper.enable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        json = mapper.writer().with(getUTCTimeZone()).writeValueAsString(new DateAsStringBean(0L));
        assertEquals("{\"date\":\"1970-01-01\"}", json);

        
        json = mapper.writeValueAsString(new DateInCETBean(0L));
        assertEquals("{\"date\":\"1970-01-01,01:00\"}", json);
        
        
        json = mapper.writer().with(getUTCTimeZone()).writeValueAsString(new CalendarAsStringBean(0L));
        assertEquals("{\"value\":\"1970-01-01\"}", json);

        
        json = mapper.writeValueAsString(new DateAsDefaultStringBean(0L));
        assertEquals("{\"date\":\"1970-01-01T00:00:00.000+0000\"}", json);
    }

// com.fasterxml.jackson.databind.ser.jdk.DateSerializationTest::testWithTimeZoneOverride
    public void testWithTimeZoneOverride() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd/HH:mm z"));
        mapper.setTimeZone(TimeZone.getTimeZone("PST"));
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        
        serialize( mapper, judate(1969, 12, 31, 16, 00, 00, 00, "PST"), "1969-12-31/16:00 PST");

        
        mapper.setLocale(Locale.FRANCE);
        serialize( mapper, judate(1969, 12, 31, 16, 00, 00, 00, "PST"), "1969-12-31/16:00 PST");

        
        ObjectWriter w = mapper.writer();
        w = w.with(TimeZone.getTimeZone("EST"));
        String json = w.writeValueAsString(new Date(0));
        assertEquals(quote("1969-12-31/19:00 EST"), json);
    }

// com.fasterxml.jackson.databind.ser.jdk.DateSerializationTest::testDateDefaultShape
    public void testDateDefaultShape() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        
        mapper.enable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        String json = mapper.writeValueAsString(new DateAsDefaultBean(0L));
        assertEquals(aposToQuotes("{'date':0}"), json);
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        json = mapper.writeValueAsString(new DateAsDefaultBean(0L));
        assertEquals(aposToQuotes("{'date':'1970-01-01T00:00:00.000+0000'}"), json);

        
        mapper.enable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        json = mapper.writeValueAsString(new DateAsDefaultBeanWithEmptyJsonFormat(0L));
        assertEquals(aposToQuotes("{'date':0}"), json);
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        json = mapper.writeValueAsString(new DateAsDefaultBeanWithEmptyJsonFormat(0L));
        assertEquals(aposToQuotes("{'date':'1970-01-01T00:00:00.000+0000'}"), json);

        
        mapper.enable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        json = mapper.writeValueAsString(new DateAsDefaultBeanWithPattern(0L));
        assertEquals(aposToQuotes("{'date':'1970-01-01'}"), json);
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        json = mapper.writeValueAsString(new DateAsDefaultBeanWithPattern(0L));
        assertEquals(aposToQuotes("{'date':'1970-01-01'}"), json);

        
        mapper.enable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        json = mapper.writeValueAsString(new DateAsDefaultBeanWithLocale(0L));
        assertEquals(aposToQuotes("{'date':'1970-01-01T00:00:00.000+0000'}"), json);
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        json = mapper.writeValueAsString(new DateAsDefaultBeanWithLocale(0L));
        assertEquals(aposToQuotes("{'date':'1970-01-01T00:00:00.000+0000'}"), json);

        
        mapper.enable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        json = mapper.writeValueAsString(new DateAsDefaultBeanWithTimezone(0L));
        assertEquals(aposToQuotes("{'date':'1970-01-01T01:00:00.000+0100'}"), json);
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        json = mapper.writeValueAsString(new DateAsDefaultBeanWithTimezone(0L));
        assertEquals(aposToQuotes("{'date':'1970-01-01T01:00:00.000+0100'}"), json);
    }

// com.fasterxml.jackson.databind.ser.jdk.DateSerializationTest::testFormatWithoutPattern
    public void testFormatWithoutPattern() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd'X'HH:mm:ss"));
        String json = mapper.writeValueAsString(new DateAsDefaultBeanWithTimezone(0L));
        assertEquals(aposToQuotes("{'date':'1970-01-01X01:00:00'}"), json);
    }

// com.fasterxml.jackson.databind.ser.jdk.JDKTypeSerializationTest::testBigDecimal
    public void testBigDecimal() throws Exception
    {
        Map<String, Object> map = new HashMap<String, Object>();
        String PI_STR = "3.14159265";
        map.put("pi", new BigDecimal(PI_STR));
        String str = MAPPER.writeValueAsString(map);
        assertEquals("{\"pi\":3.14159265}", str);
    }

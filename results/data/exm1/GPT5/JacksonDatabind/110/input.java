// buggy code
    public static JsonDeserializer<?> findForCollection(DeserializationContext ctxt,
            JavaType type)
        throws JsonMappingException
    {
        JavaUtilCollectionsConverter conv;

        // 10-Jan-2017, tatu: Some types from `java.util.Collections`/`java.util.Arrays` need bit of help...
        if (type.hasRawClass(CLASS_AS_ARRAYS_LIST)) {
            conv = converter(TYPE_AS_LIST, type, List.class);
        } else if (type.hasRawClass(CLASS_SINGLETON_LIST)) {
            conv = converter(TYPE_SINGLETON_LIST, type, List.class);
        } else if (type.hasRawClass(CLASS_SINGLETON_SET)) {
            conv = converter(TYPE_SINGLETON_SET, type, Set.class);
        // [databind#2265]: we may have another impl type for unmodifiable Lists, check both
        } else if (type.hasRawClass(CLASS_UNMODIFIABLE_LIST)) {
            conv = converter(TYPE_UNMODIFIABLE_LIST, type, List.class);
        } else if (type.hasRawClass(CLASS_UNMODIFIABLE_SET)) {
            conv = converter(TYPE_UNMODIFIABLE_SET, type, Set.class);
        } else {
            return null;
        }
        return new StdDelegatingDeserializer<Object>(conv);
    }

// relevant test
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

// com.fasterxml.jackson.databind.convert.TestMapConversions::testMapToProperties
    public void testMapToProperties() throws Exception
    {
        Bean bean = new Bean();
        bean.A = 129;
        bean.B = "13";
        Properties props = MAPPER.convertValue(bean, Properties.class);

        assertEquals(2, props.size());

        assertEquals("13", props.getProperty("B"));
        
        assertEquals("129", props.getProperty("A"));
    }

// com.fasterxml.jackson.databind.convert.TestUpdateViaObjectReader::testBeanUpdate
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

// com.fasterxml.jackson.databind.convert.TestUpdateViaObjectReader::testListUpdate
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

// com.fasterxml.jackson.databind.convert.TestUpdateViaObjectReader::testMapUpdate
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

// com.fasterxml.jackson.databind.convert.TestUpdateViaObjectReader::testUpdateSequence
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

// com.fasterxml.jackson.databind.convert.TestUpdateViaObjectReader::testUpdatingWithViews
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

// com.fasterxml.jackson.databind.convert.TestUpdateViaObjectReader::testIssue744
    public void testIssue744() throws IOException
    {
        ObjectMapper mapper = new ObjectMapper();
        SimpleModule module = new SimpleModule();
        module.addDeserializer(DataA.class, new DataADeserializer());
        mapper.registerModule(module);

        DataB db = new DataB();
        db.da.i = 11;
        db.k = 13;
        String jsonBString = mapper.writeValueAsString(db);
        JsonNode jsonBNode = mapper.valueToTree(db);

        
        DataB dbNewViaString = mapper.readValue(jsonBString, DataB.class);
        assertEquals(5, dbNewViaString.da.i);
        assertEquals(13, dbNewViaString.k);

        DataB dbNewViaNode = mapper.treeToValue(jsonBNode, DataB.class);
        assertEquals(5, dbNewViaNode.da.i);
        assertEquals(13, dbNewViaNode.k);

        
        DataB dbUpdViaString = new DataB();
        DataB dbUpdViaNode = new DataB();

        assertEquals(1, dbUpdViaString.da.i);
        assertEquals(3, dbUpdViaString.k);
        mapper.readerForUpdating(dbUpdViaString).readValue(jsonBString);
        assertEquals(5, dbUpdViaString.da.i);
        assertEquals(13, dbUpdViaString.k);

        assertEquals(1, dbUpdViaNode.da.i);
        assertEquals(3, dbUpdViaNode.k);
        
        mapper.readerForUpdating(dbUpdViaNode).readValue(jsonBNode);
        assertEquals(5, dbUpdViaNode.da.i);
        assertEquals(13, dbUpdViaNode.k);
    }

// com.fasterxml.jackson.databind.convert.TestUpdateViaObjectReader::test1831UsingNode
    public void test1831UsingNode() throws IOException {
        String catJson = MAPPER.writeValueAsString(new Cat());
        JsonNode jsonNode = MAPPER.readTree(catJson);
        AnimalWrapper optionalCat = new AnimalWrapper();
        ObjectReader r = MAPPER.readerForUpdating(optionalCat);
        AnimalWrapper result = r.readValue(jsonNode);
        assertSame(optionalCat, result);
    }

// com.fasterxml.jackson.databind.convert.TestUpdateViaObjectReader::test1831UsingString
    public void test1831UsingString() throws IOException {
        String catJson = MAPPER.writeValueAsString(new Cat());
        AnimalWrapper optionalCat = new AnimalWrapper();
        AnimalWrapper result = MAPPER.readerForUpdating(optionalCat).readValue(catJson);
        assertSame(optionalCat, result);
    }

// com.fasterxml.jackson.databind.convert.UpdateValueTest::testMapUpdate
    public void testMapUpdate() throws Exception
    {
        Map<String,Object> base = new LinkedHashMap<>();
        base.put("a", 345);
        Map<String,Object> overrides = new LinkedHashMap<>();
        overrides.put("xyz", Boolean.TRUE);
        overrides.put("foo", "bar");
        
        Map<String,Object> ob = MAPPER.updateValue(base, overrides);
        
        assertSame(base, ob);
        assertEquals(3, ob.size());
        assertEquals(Integer.valueOf(345), ob.get("a"));
        assertEquals("bar", ob.get("foo"));
        assertEquals(Boolean.TRUE, ob.get("xyz"));
    }

// com.fasterxml.jackson.databind.convert.UpdateValueTest::testListUpdate
    public void testListUpdate() throws Exception
    {
        List<Object> base = new ArrayList<>();
        base.add(123456);
        base.add(Boolean.FALSE);
        Object[] overrides = new Object[] { Boolean.TRUE, "zoink!" };

        List<Object> ob = MAPPER.updateValue(base, overrides);
        
        assertSame(base, ob);
        assertEquals(4, ob.size());
        assertEquals(Integer.valueOf(123456), ob.get(0));
        assertEquals(Boolean.FALSE, ob.get(1));
        assertEquals(overrides[0], ob.get(2));
        assertEquals(overrides[1], ob.get(3));
    }

// com.fasterxml.jackson.databind.convert.UpdateValueTest::testArrayUpdate
    public void testArrayUpdate() throws Exception
    {
        
        Object[] base = new Object[] { Boolean.FALSE, Integer.valueOf(3) };
        Object[] overrides = new Object[] { Boolean.TRUE, "zoink!" };

        Object[] ob = MAPPER.updateValue(base, overrides);
        assertEquals(4, ob.length);
        assertEquals(base[0], ob[0]);
        assertEquals(base[1], ob[1]);
        assertEquals(overrides[0], ob[2]);
        assertEquals(overrides[1], ob[3]);
    }

// com.fasterxml.jackson.databind.convert.UpdateValueTest::testPOJO
    public void testPOJO() throws Exception
    {
        Point base = new Point(42, 28);
        Map<String,Object> overrides = new LinkedHashMap<>();
        overrides.put("y", 1234);
        Point result = MAPPER.updateValue(base, overrides);
        assertSame(base, result);
        assertEquals(42, result.x);
        assertEquals(1234, result.y);
    }

// com.fasterxml.jackson.databind.convert.UpdateValueTest::testMisc
    public void testMisc() throws Exception
    {
        
        assertNull(MAPPER.updateValue(null, "foo"));
        List<String> input = new ArrayList<>();
        assertSame(input, MAPPER.updateValue(input, null));
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

// com.fasterxml.jackson.databind.deser.jdk.MapDeserializationTest::testAbstractMapDefault
    public void testAbstractMapDefault() throws Exception
    {
        final AbstractMapWrapper result = MAPPER.readValue("{\"values\":{\"foo\":42}}",
                AbstractMapWrapper.class);
        assertNotNull(result);
        assertEquals(LinkedHashMap.class, result.values.getClass());
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

// com.fasterxml.jackson.databind.deser.jdk.UtilCollectionsTypesTest::testEmptyList
   public void testEmptyList() throws Exception {
       _verifyCollection(Collections.emptyList());
   }

// com.fasterxml.jackson.databind.deser.jdk.UtilCollectionsTypesTest::testEmptySet
   public void testEmptySet() throws Exception {
       _verifyCollection(Collections.emptySet());
   }

// com.fasterxml.jackson.databind.deser.jdk.UtilCollectionsTypesTest::testEmptyMap
   public void testEmptyMap() throws Exception {
       _verifyMap(Collections.emptyMap());
   }

// com.fasterxml.jackson.databind.deser.jdk.UtilCollectionsTypesTest::testSingletonList
   public void testSingletonList() throws Exception {
       _verifyCollection(Collections.singletonList(Arrays.asList("TheOne")));
   }

// com.fasterxml.jackson.databind.deser.jdk.UtilCollectionsTypesTest::testSingletonSet
   public void testSingletonSet() throws Exception {
       _verifyCollection(Collections.singleton(Arrays.asList("TheOne")));
   }

// com.fasterxml.jackson.databind.deser.jdk.UtilCollectionsTypesTest::testSingletonMap
   public void testSingletonMap() throws Exception {
       _verifyMap(Collections.singletonMap("foo", "bar"));
   }

// com.fasterxml.jackson.databind.deser.jdk.UtilCollectionsTypesTest::testUnmodifiableList
   public void testUnmodifiableList() throws Exception {
       _verifyCollection(Collections.unmodifiableList(Arrays.asList("first", "second")));
   }

// com.fasterxml.jackson.databind.deser.jdk.UtilCollectionsTypesTest::testUnmodifiableListFromLinkedList
   public void testUnmodifiableListFromLinkedList() throws Exception {
       final List<String> input = new LinkedList<>();
       input.add("first");
       input.add("second");

       
       
       Collection<?> act = _writeReadCollection(Collections.unmodifiableList(input));
       assertEquals(input, act);

       
       assertEquals(Collections.unmodifiableList(new ArrayList<>(input)).getClass(), act.getClass());
   }

// com.fasterxml.jackson.databind.deser.jdk.UtilCollectionsTypesTest::testUnmodifiableSet
   public void testUnmodifiableSet() throws Exception
   {
       Set<String> input = new LinkedHashSet<>(Arrays.asList("first", "second"));
       _verifyCollection(Collections.unmodifiableSet(input));
   }

// com.fasterxml.jackson.databind.deser.jdk.UtilCollectionsTypesTest::testUnmodifiableMap
   public void testUnmodifiableMap() throws Exception
   {
       Map<String,String> input = new LinkedHashMap<>();
       input.put("a", "b");
       input.put("c", "d");
       _verifyMap(Collections.unmodifiableMap(input));
   }

// com.fasterxml.jackson.databind.deser.jdk.UtilCollectionsTypesTest::testArraysAsList
   public void testArraysAsList() throws Exception
   {
       
       
       List<String> input = Arrays.asList("a", "bc", "def");
       String json = DEFAULT_MAPPER.writeValueAsString(input);
       List<?> result = DEFAULT_MAPPER.readValue(json, List.class);
       assertEquals(input, result);
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

// com.fasterxml.jackson.databind.jsontype.TestTypeNames::testBaseTypeId1616
    public void testBaseTypeId1616() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        Collection<NamedType> subtypes = new StdSubtypeResolver().collectAndResolveSubtypesByTypeId(
                mapper.getDeserializationConfig(),
                
                null,
                mapper.constructType(Base1616.class));
        assertEquals(2, subtypes.size());
        Set<String> ok = new HashSet<>(Arrays.asList("A", "B"));
        for (NamedType type : subtypes) {
            String id = type.getName();
            if (!ok.contains(id)) {
                fail("Unexpected id '"+id+"' (mapping to: "+type.getType()+"), should be one of: "+ok);
            }
        }
    }

// com.fasterxml.jackson.databind.jsontype.TestTypeNames::testSerialization
    public void testSerialization() throws Exception
    {
        
        
        
        
        assertEquals("[{\"doggy\":{\"name\":\"Spot\",\"ageInYears\":3}}]",
                MAPPER.writeValueAsString(new Animal[] { new Dog("Spot", 3) }));
        assertEquals("[{\"MaineCoon\":{\"name\":\"Belzebub\",\"purrs\":true}}]",
                MAPPER.writeValueAsString(new Animal[] { new MaineCoon("Belzebub", true)}));
    }

// com.fasterxml.jackson.databind.jsontype.TestTypeNames::testRoundTrip
    public void testRoundTrip() throws Exception
    {
        Animal[] input = new Animal[] {
                new Dog("Odie", 7),
                null,
                new MaineCoon("Piru", false),
                new Persian("Khomeini", true)
        };
        String json = MAPPER.writeValueAsString(input);
        List<Animal> output = MAPPER.readValue(json,
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
        AnimalMap input = new AnimalMap();
        input.put("venla", new MaineCoon("Venla", true));
        input.put("ama", new Dog("Amadeus", 13));
        String json = MAPPER.writeValueAsString(input);
        AnimalMap output = MAPPER.readValue(json, AnimalMap.class);
        assertNotNull(output);
        assertEquals(AnimalMap.class, output.getClass());
        assertEquals(input.size(), output.size());

        
        for (String name : input.keySet()) {
            Animal in = input.get(name);
            Animal out = output.get(name);
            if (!in.equals(out)) {
                fail("Animal in input was ["+in+"]; output not matching: ["+out+"]");
            }
        }
    }

// com.fasterxml.jackson.databind.jsontype.TypeRefinementForMapTest::testMapRefinement
    public void testMapRefinement() throws Exception
    {
        String ID1 = "3a6383d4-8123-4c43-8b8d-7cedf3e59404";
        String ID2 = "81c3d978-90c4-4b00-8da1-1c39ffcab02c";
        String json = aposToQuotes(
"{'id':'"+ID1+"','items':[{'id':'"+ID2+"','property':'value'}]}");

        ObjectMapper m = new ObjectMapper();
        Data data = m.readValue(json, Data.class);

        assertEquals(ID1, data.id);
        assertNotNull(data.items);
        assertEquals(1, data.items.size());
        Item value = data.items.get(ID2);
        assertNotNull(value);
        assertEquals("value", value.property);
    }

// com.fasterxml.jackson.databind.jsontype.TypeRefinementForMapTest::testMapKeyRefinement1384
    public void testMapKeyRefinement1384() throws Exception
    {
        final String TEST_INSTANCE_SERIALIZED =
                "{\"mapProperty\":[\"java.util.HashMap\",{\"Compound|Key\":\"Value\"}]}";
        ObjectMapper mapper = new ObjectMapper().enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);

        TestClass testInstance = mapper.readValue(TEST_INSTANCE_SERIALIZED, TestClass.class);
        assertEquals(1, testInstance.mapProperty.size());
        Object key = testInstance.mapProperty.keySet().iterator().next();
        assertEquals(CompoundKey.class, key.getClass());
        String testInstanceSerialized = mapper.writeValueAsString(testInstance);
        assertEquals(TEST_INSTANCE_SERIALIZED, testInstanceSerialized);
    }

// com.fasterxml.jackson.databind.jsontype.deftyping.TestDefaultForMaps::testJackson428
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

// com.fasterxml.jackson.databind.jsontype.deftyping.TestDefaultForMaps::testList
    public void testList() throws Exception
    {
        final ObjectMapper mapper = new ObjectMapper();
        mapper.enableDefaultTyping(ObjectMapper.DefaultTyping.OBJECT_AND_NON_CONCRETE, JsonTypeInfo.As.PROPERTY);
        ItemList child = new ItemList();
        child.value = "I am child";

        ItemList parent = new ItemList();
        parent.value = "I am parent";
        parent.addChildItem(child);
        String json = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(parent);

        Object o = mapper.readValue(json, ItemList.class);
        assertNotNull(o);
    }

// com.fasterxml.jackson.databind.jsontype.deftyping.TestDefaultForMaps::testMap
    public void testMap() throws Exception
    {
        final ObjectMapper mapper = new ObjectMapper();
        mapper.enableDefaultTyping(ObjectMapper.DefaultTyping.OBJECT_AND_NON_CONCRETE, JsonTypeInfo.As.PROPERTY);
        ItemMap child = new ItemMap();
        child.value = "I am child";

        ItemMap parent = new ItemMap();
        parent.value = "I am parent";
        parent.addChildItem("child", child);

        String json = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(parent);
        Object o = mapper.readValue(json, ItemMap.class);
        assertNotNull(o);
    }

// com.fasterxml.jackson.databind.jsontype.deftyping.TestDefaultForScalars::testNumericScalars
    public void testNumericScalars() throws Exception
    {
        ObjectMapper m = new ObjectMapper();
        m.enableDefaultTyping();

        
        assertEquals("[123]", m.writeValueAsString(new Object[] { Integer.valueOf(123) }));
        assertEquals("[[\"java.lang.Long\",37]]", m.writeValueAsString(new Object[] { Long.valueOf(37) }));
        assertEquals("[0.25]", m.writeValueAsString(new Object[] { Double.valueOf(0.25) }));
        assertEquals("[[\"java.lang.Float\",0.5]]", m.writeValueAsString(new Object[] { Float.valueOf(0.5f) }));
    }

// com.fasterxml.jackson.databind.jsontype.deftyping.TestDefaultForScalars::testDateScalars
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

// com.fasterxml.jackson.databind.jsontype.deftyping.TestDefaultForScalars::testMiscScalars
    public void testMiscScalars() throws Exception
    {
        ObjectMapper m = new ObjectMapper();
        m.enableDefaultTyping();

        
        assertEquals("[\"abc\"]", m.writeValueAsString(new Object[] { "abc" }));
        assertEquals("[true,null,false]", m.writeValueAsString(new Boolean[] { true, null, false }));
    }

// com.fasterxml.jackson.databind.jsontype.deftyping.TestDefaultForScalars::testScalarArrays
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

// com.fasterxml.jackson.databind.jsontype.deftyping.TestDefaultForScalars::test417
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

// com.fasterxml.jackson.databind.jsontype.deftyping.TestDefaultForScalars::testDefaultTypingWithLong
    public void testDefaultTypingWithLong() throws Exception
    {
        Data data = new Data();
        data.key = 1L;
        Map<String, Object> mapData = new HashMap<String, Object>();
        mapData.put("longInMap", 2L);
        mapData.put("longAsField", data);

        
        ObjectMapper mapper = new ObjectMapper();
        StdTypeResolverBuilder resolver = new StdTypeResolverBuilder();
        resolver.init(JsonTypeInfo.Id.CLASS, null);
        resolver.inclusion(JsonTypeInfo.As.PROPERTY);
        resolver.typeProperty("__t");
        mapper.setDefaultTyping(resolver);
        mapper.enable(SerializationFeature.INDENT_OUTPUT);

        
        String json = mapper.writeValueAsString(mapData);

        
        Map<?,?> result = mapper.readValue(json, Map.class);
        assertNotNull(result);
        assertEquals(2, result.size());
    }

// com.fasterxml.jackson.databind.module.TestAbstractTypes::testCollectionDefaulting
    public void testCollectionDefaulting() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        SimpleModule mod = new SimpleModule("test", Version.unknownVersion());
        
        mod.addAbstractTypeMapping(Collection.class, List.class);
        mod.addAbstractTypeMapping(List.class, LinkedList.class);
        mapper.registerModule(mod);
        Collection<?> result = mapper.readValue("[]", Collection.class);
        assertEquals(LinkedList.class, result.getClass());
    }

// com.fasterxml.jackson.databind.module.TestAbstractTypes::testMapDefaultingBasic
    public void testMapDefaultingBasic() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        SimpleModule mod = new SimpleModule("test", Version.unknownVersion());
        
        mod.addAbstractTypeMapping(Map.class, TreeMap.class);
        mapper.registerModule(mod);
        Map<?,?> result = mapper.readValue("{}", Map.class);
        assertEquals(TreeMap.class, result.getClass());
    }

// com.fasterxml.jackson.databind.module.TestAbstractTypes::testDefaultingRecursive
    public void testDefaultingRecursive() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        SimpleModule mod = new SimpleModule("test", Version.unknownVersion());

        
        mod.addAbstractTypeMapping(Map.class, TreeMap.class);
        mod.addAbstractTypeMapping(List.class, LinkedList.class);

        mapper.registerModule(mod);
        Object result;

        result = mapper.readValue("[ {} ]", Object.class);
        assertEquals(LinkedList.class, result.getClass());
        Object v = ((List<?>) result).get(0);
        assertNotNull(v);
        assertEquals(TreeMap.class, v.getClass());

        result = mapper.readValue("{ \"x\": [ 3 ] }", Object.class);
        assertEquals(TreeMap.class, result.getClass());
        Map<?,?> map = (Map<?,?>) result;
        assertEquals(1, map.size());
        v = map.get("x");
        assertNotNull(v);
        assertEquals(LinkedList.class, v.getClass());
        assertEquals(1, ((List<?>) v).size());
    }

// com.fasterxml.jackson.databind.module.TestAbstractTypes::testInterfaceDefaulting
    public void testInterfaceDefaulting() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        SimpleModule mod = new SimpleModule("test", Version.unknownVersion());
        
        mod.addAbstractTypeMapping(CharSequence.class, MyString.class);
        mapper.registerModule(mod);
        Object result = mapper.readValue(quote("abc"), CharSequence.class);
        assertEquals(MyString.class, result.getClass());
        assertEquals("abc", ((MyString) result).value);

        
        mod = new SimpleModule();
        mod.addAbstractTypeMapping(Abstract.class, AbstractImpl.class);
        mapper = new ObjectMapper()
                .registerModule(mod);
        Abstract a = mapper.readValue("{}", Abstract.class);
        assertNotNull(a);
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

// com.fasterxml.jackson.databind.type.RecursiveType1658Test::testRecursive1658
    public void testRecursive1658() throws Exception
    {
        Tree<String> t = new Tree<String>(Arrays.asList("hello", "world"));
        ObjectMapper mapper = new ObjectMapper();

        final TypeResolverBuilder<?> typer = new StdTypeResolverBuilder()
                .init(JsonTypeInfo.Id.CLASS, null)
                .inclusion(JsonTypeInfo.As.PROPERTY);
        mapper.setDefaultTyping(typer);

        String res = mapper.writeValueAsString(t);

        Tree<?> tRead = mapper.readValue(res, Tree.class);

        assertNotNull(tRead);
    }

// com.fasterxml.jackson.databind.type.TestTypeFactory::testSimpleTypes
    public void testSimpleTypes()
    {
        Class<?>[] classes = new Class<?>[] {
            boolean.class, byte.class, char.class,
                short.class, int.class, long.class,
                float.class, double.class,

            Boolean.class, Byte.class, Character.class,
                Short.class, Integer.class, Long.class,
                Float.class, Double.class,

                String.class,
                Object.class,

                Calendar.class,
                Date.class,
        };

        TypeFactory tf = TypeFactory.defaultInstance();
        for (Class<?> clz : classes) {
            assertSame(clz, tf.constructType(clz).getRawClass());
            assertSame(clz, tf.constructType(clz).getRawClass());
        }
    }

// com.fasterxml.jackson.databind.type.TestTypeFactory::testArrays
    public void testArrays()
    {
        Class<?>[] classes = new Class<?>[] {
            boolean[].class, byte[].class, char[].class,
                short[].class, int[].class, long[].class,
                float[].class, double[].class,

                String[].class, Object[].class,
                Calendar[].class,
        };

        TypeFactory tf = TypeFactory.defaultInstance();
        for (Class<?> clz : classes) {
            assertSame(clz, tf.constructType(clz).getRawClass());
            Class<?> elemType = clz.getComponentType();
            assertSame(clz, tf.constructArrayType(elemType).getRawClass());
        }
    }

// com.fasterxml.jackson.databind.type.TestTypeFactory::testProperties
    public void testProperties()
    {
        TypeFactory tf = TypeFactory.defaultInstance();
        JavaType t = tf.constructType(Properties.class);
        assertEquals(MapType.class, t.getClass());
        assertSame(Properties.class, t.getRawClass());

        MapType mt = (MapType) t;

        
        assertSame(String.class, mt.getKeyType().getRawClass());
        assertSame(String.class, mt.getContentType().getRawClass());
    }

// com.fasterxml.jackson.databind.type.TestTypeFactory::testIterator
    public void testIterator()
    {
        TypeFactory tf = TypeFactory.defaultInstance();
        JavaType t = tf.constructType(new TypeReference<Iterator<String>>() { });
        assertEquals(SimpleType.class, t.getClass());
        assertSame(Iterator.class, t.getRawClass());
        assertEquals(1, t.containedTypeCount());
        assertEquals(tf.constructType(String.class), t.containedType(0));
        assertNull(t.containedType(1));
    }

// com.fasterxml.jackson.databind.type.TestTypeFactory::testParametricTypes
    public void testParametricTypes()
    {
        TypeFactory tf = TypeFactory.defaultInstance();
        
        JavaType t = tf.constructParametrizedType(ArrayList.class, Collection.class, String.class); 
        assertEquals(CollectionType.class, t.getClass());
        JavaType strC = tf.constructType(String.class);
        assertEquals(1, t.containedTypeCount());
        assertEquals(strC, t.containedType(0));
        assertNull(t.containedType(1));

        
        JavaType t2 = tf.constructParametrizedType(Map.class, Map.class, strC, t); 
        
        assertEquals(MapType.class, t2.getClass());
        assertEquals(2, t2.containedTypeCount());
        assertEquals(strC, t2.containedType(0));
        assertEquals(t, t2.containedType(1));
        assertNull(t2.containedType(2));

        
        JavaType custom = tf.constructParametrizedType(SingleArgGeneric.class, SingleArgGeneric.class,
                String.class);
        assertEquals(SimpleType.class, custom.getClass());
        assertEquals(1, custom.containedTypeCount());
        assertEquals(strC, custom.containedType(0));
        assertNull(custom.containedType(1));

        
        assertEquals("X", custom.containedTypeName(0));

        
        try {
            
            tf.constructParametrizedType(Map.class, Map.class, strC);
        } catch (IllegalArgumentException e) {
            verifyException(e, "Cannot create TypeBindings for class java.util.Map");
        }

        try {
            
            tf.constructParametrizedType(SingleArgGeneric.class, SingleArgGeneric.class, strC, strC);
        } catch (IllegalArgumentException e) {
            verifyException(e, "Cannot create TypeBindings for class ");
        }
    }

// com.fasterxml.jackson.databind.type.TestTypeFactory::testCanonicalNames
    public void testCanonicalNames()
    {
        TypeFactory tf = TypeFactory.defaultInstance();
        JavaType t = tf.constructType(java.util.Calendar.class);
        String can = t.toCanonical();
        assertEquals("java.util.Calendar", can);
        assertEquals(t, tf.constructFromCanonical(can));

        
        t = tf.constructType(java.util.ArrayList.class);
        can = t.toCanonical();
        assertEquals("java.util.ArrayList<java.lang.Object>", can);
        assertEquals(t, tf.constructFromCanonical(can));

        t = tf.constructType(java.util.TreeMap.class);
        can = t.toCanonical();
        assertEquals("java.util.TreeMap<java.lang.Object,java.lang.Object>", can);
        assertEquals(t, tf.constructFromCanonical(can));

        
        t = tf.constructMapType(EnumMap.class, EnumForCanonical.class, String.class);
        can = t.toCanonical();
        assertEquals("java.util.EnumMap<com.fasterxml.jackson.databind.type.TestTypeFactory$EnumForCanonical,java.lang.String>",
                can);
        assertEquals(t, tf.constructFromCanonical(can));

        
        t = tf.constructType(new TypeReference<AtomicReference<Long>>() { });
        can = t.toCanonical();
        assertEquals("java.util.concurrent.atomic.AtomicReference<java.lang.Long>",
                can);
        assertEquals(t, tf.constructFromCanonical(can));

        
        t = tf.constructFromCanonical("java.util.List");
        assertEquals(List.class, t.getRawClass());
        assertEquals(CollectionType.class, t.getClass());
        
        
        
        assertEquals(Object.class, t.getContentType().getRawClass());
        can = t.toCanonical();
        assertEquals("java.util.List<java.lang.Object>", can);
        assertEquals(t, tf.constructFromCanonical(can));
    }

// com.fasterxml.jackson.databind.type.TestTypeFactory::testCanonicalWithSpaces
    public void testCanonicalWithSpaces()
    {
        TypeFactory tf = TypeFactory.defaultInstance();
        Object objects = new TreeMap<Object, Object>() { }; 
        String reflectTypeName = objects.getClass().getGenericSuperclass().toString();
        JavaType t1 = tf.constructType(objects.getClass().getGenericSuperclass());
        
        JavaType t2 = tf.constructFromCanonical(reflectTypeName);
        assertNotNull(t2);
        assertEquals(t2, t1);
    }

// com.fasterxml.jackson.databind.type.TestTypeFactory::testCollections
    public void testCollections()
    {
        
        TypeFactory tf = TypeFactory.defaultInstance();
        JavaType t = tf.constructType(ArrayList.class);
        assertEquals(CollectionType.class, t.getClass());
        assertSame(ArrayList.class, t.getRawClass());

        
        t = tf.constructType(new TypeReference<ArrayList<String>>() { });
        assertEquals(CollectionType.class, t.getClass());
        assertSame(ArrayList.class, t.getRawClass());

        JavaType elemType = ((CollectionType) t).getContentType();
        assertNotNull(elemType);
        assertSame(SimpleType.class, elemType.getClass());
        assertSame(String.class, elemType.getRawClass());

        
        t = tf.constructCollectionType(ArrayList.class, String.class);
        assertEquals(CollectionType.class, t.getClass());
        assertSame(String.class, ((CollectionType) t).getContentType().getRawClass());
    }

// com.fasterxml.jackson.databind.type.TestTypeFactory::testCollectionTypesRefined
    public void testCollectionTypesRefined()
    {
        TypeFactory tf = newTypeFactory();
        JavaType type = tf.constructType(new TypeReference<List<Long>>() { });
        assertEquals(List.class, type.getRawClass());
        assertEquals(Long.class, type.getContentType().getRawClass());
        
        assertNull(type.getSuperClass());

        
        JavaType subtype = tf.constructSpecializedType(type, ArrayList.class);
        assertEquals(ArrayList.class, subtype.getRawClass());
        assertEquals(Long.class, subtype.getContentType().getRawClass());

        
        JavaType superType = subtype.getSuperClass();
        assertNotNull(superType);
        assertEquals(AbstractList.class, superType.getRawClass());
    }

// com.fasterxml.jackson.databind.type.TestTypeFactory::testMaps
    public void testMaps()
    {
        TypeFactory tf = newTypeFactory();

        
        JavaType t = tf.constructType(HashMap.class);
        assertEquals(MapType.class, t.getClass());
        assertSame(HashMap.class, t.getRawClass());

        
        t = tf.constructMapType(TreeMap.class, String.class, Integer.class);
        assertEquals(MapType.class, t.getClass());
        assertSame(String.class, ((MapType) t).getKeyType().getRawClass());
        assertSame(Integer.class, ((MapType) t).getContentType().getRawClass());

        
        t = tf.constructType(new TypeReference<HashMap<String,Integer>>() { });
        assertEquals(MapType.class, t.getClass());
        assertSame(HashMap.class, t.getRawClass());
        MapType mt = (MapType) t;
        assertEquals(tf.constructType(String.class), mt.getKeyType());
        assertEquals(tf.constructType(Integer.class), mt.getContentType());

        t = tf.constructType(new TypeReference<LongValuedMap<Boolean>>() { });
        assertEquals(MapType.class, t.getClass());
        assertSame(LongValuedMap.class, t.getRawClass());
        mt = (MapType) t;
        assertEquals(tf.constructType(Boolean.class), mt.getKeyType());
        assertEquals(tf.constructType(Long.class), mt.getContentType());

        JavaType type = tf.constructType(new TypeReference<Map<String,Boolean>>() { });
        MapType mapType = (MapType) type;
        assertEquals(tf.constructType(String.class), mapType.getKeyType());
        assertEquals(tf.constructType(Boolean.class), mapType.getContentType());
    }

// com.fasterxml.jackson.databind.type.TestTypeFactory::testMapTypesRefined
    public void testMapTypesRefined()
    {
        TypeFactory tf = newTypeFactory();
        JavaType type = tf.constructType(new TypeReference<Map<String,List<Integer>>>() { });
        MapType mapType = (MapType) type;
        assertEquals(Map.class, mapType.getRawClass());
        assertEquals(String.class, mapType.getKeyType().getRawClass());
        assertEquals(List.class, mapType.getContentType().getRawClass());
        assertEquals(Integer.class, mapType.getContentType().getContentType().getRawClass());
        
        assertNull(type.getSuperClass());
        
        
        JavaType subtype = tf.constructSpecializedType(type, LinkedHashMap.class);
        assertEquals(LinkedHashMap.class, subtype.getRawClass());
        assertEquals(String.class, subtype.getKeyType().getRawClass());
        assertEquals(List.class, subtype.getContentType().getRawClass());
        assertEquals(Integer.class, subtype.getContentType().getContentType().getRawClass());

        

        JavaType superType = subtype.getSuperClass();
        assertNotNull(superType);
        assertEquals(HashMap.class, superType.getRawClass());
        
        assertEquals(String.class, superType.getKeyType().getRawClass());
        assertEquals(List.class, superType.getContentType().getRawClass());
        assertEquals(Integer.class, superType.getContentType().getContentType().getRawClass());
    }

// com.fasterxml.jackson.databind.type.TestTypeFactory::testTypeGeneralization
    public void testTypeGeneralization()
    {
        TypeFactory tf = newTypeFactory();
        MapType t = tf.constructMapType(HashMap.class, String.class, Long.class);
        JavaType superT = tf.constructGeneralizedType(t, Map.class);
        assertEquals(String.class, superT.getKeyType().getRawClass());
        assertEquals(Long.class, superT.getContentType().getRawClass());

        assertSame(t, tf.constructGeneralizedType(t, HashMap.class));

        
        try {
            tf.constructGeneralizedType(t, TreeMap.class);
            fail("Should not pass");
        } catch (IllegalArgumentException e) {
            verifyException(e, "not a super-type of");
        }
    }

// com.fasterxml.jackson.databind.type.TestTypeFactory::testMapTypesRaw
    public void testMapTypesRaw()
    {
        TypeFactory tf = TypeFactory.defaultInstance();
        JavaType type = tf.constructType(HashMap.class);
        MapType mapType = (MapType) type;
        assertEquals(tf.constructType(Object.class), mapType.getKeyType());
        assertEquals(tf.constructType(Object.class), mapType.getContentType());        
    }

// com.fasterxml.jackson.databind.type.TestTypeFactory::testMapTypesAdvanced
    public void testMapTypesAdvanced()
    {
        TypeFactory tf = TypeFactory.defaultInstance();
        JavaType type = tf.constructType(MyMap.class);
        MapType mapType = (MapType) type;
        assertEquals(tf.constructType(String.class), mapType.getKeyType());
        assertEquals(tf.constructType(Long.class), mapType.getContentType());
        
        type = tf.constructType(MapInterface.class);
        mapType = (MapType) type;

        assertEquals(tf.constructType(String.class), mapType.getKeyType());
        assertEquals(tf.constructType(Integer.class), mapType.getContentType());

        type = tf.constructType(MyStringIntMap.class);
        mapType = (MapType) type;
        assertEquals(tf.constructType(String.class), mapType.getKeyType());
        assertEquals(tf.constructType(Integer.class), mapType.getContentType());
    }

// com.fasterxml.jackson.databind.type.TestTypeFactory::testMapTypesSneaky
    public void testMapTypesSneaky()
    {
        TypeFactory tf = TypeFactory.defaultInstance();
        JavaType type = tf.constructType(IntLongMap.class);
        MapType mapType = (MapType) type;
        assertEquals(tf.constructType(Integer.class), mapType.getKeyType());
        assertEquals(tf.constructType(Long.class), mapType.getContentType());
    }

// com.fasterxml.jackson.databind.type.TestTypeFactory::testSneakyFieldTypes
    public void testSneakyFieldTypes() throws Exception
    {
        TypeFactory tf = TypeFactory.defaultInstance();
        Field field = SneakyBean.class.getDeclaredField("intMap");
        JavaType type = tf.constructType(field.getGenericType());
        assertTrue(type instanceof MapType);
        MapType mapType = (MapType) type;
        assertEquals(tf.constructType(Integer.class), mapType.getKeyType());
        assertEquals(tf.constructType(Long.class), mapType.getContentType());

        field = SneakyBean.class.getDeclaredField("longList");
        type = tf.constructType(field.getGenericType());
        assertTrue(type instanceof CollectionType);
        CollectionType collectionType = (CollectionType) type;
        assertEquals(tf.constructType(Long.class), collectionType.getContentType());
    }

// com.fasterxml.jackson.databind.type.TestTypeFactory::testSneakyBeanProperties
    public void testSneakyBeanProperties() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        StringLongMapBean bean = mapper.readValue("{\"value\":{\"a\":123}}", StringLongMapBean.class);
        assertNotNull(bean);
        Map<String,Long> map = bean.value;
        assertEquals(1, map.size());
        assertEquals(Long.valueOf(123), map.get("a"));

        StringListBean bean2 = mapper.readValue("{\"value\":[\"...\"]}", StringListBean.class);
        assertNotNull(bean2);
        List<String> list = bean2.value;
        assertSame(GenericList.class, list.getClass());
        assertEquals(1, list.size());
        assertEquals("...", list.get(0));
    }

// com.fasterxml.jackson.databind.type.TestTypeFactory::testSneakySelfRefs
    public void testSneakySelfRefs() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        String json = mapper.writeValueAsString(new SneakyBean2());
        assertEquals("{\"foobar\":null}", json);
    }

// com.fasterxml.jackson.databind.type.TestTypeFactory::testAtomicArrayRefParameters
    public void testAtomicArrayRefParameters()
    {
        TypeFactory tf = TypeFactory.defaultInstance();
        JavaType type = tf.constructType(new TypeReference<AtomicReference<long[]>>() { });
        JavaType[] params = tf.findTypeParameters(type, AtomicReference.class);
        assertNotNull(params);
        assertEquals(1, params.length);
        assertEquals(tf.constructType(long[].class), params[0]);
    }

// com.fasterxml.jackson.databind.type.TestTypeFactory::testMapEntryResolution
    public void testMapEntryResolution()
    {
        TypeFactory tf = TypeFactory.defaultInstance();
        JavaType t = tf.constructType(StringIntMapEntry.class);
        JavaType mapEntryType = t.findSuperType(Map.Entry.class);
        assertNotNull(mapEntryType);
        assertTrue(mapEntryType.hasGenericTypes());
        assertEquals(2, mapEntryType.containedTypeCount());
        assertEquals(String.class, mapEntryType.containedType(0).getRawClass());
        assertEquals(Integer.class, mapEntryType.containedType(1).getRawClass());
    }

// com.fasterxml.jackson.databind.type.TestTypeFactory::testRawCollections
    public void testRawCollections()
    {
        TypeFactory tf = TypeFactory.defaultInstance();
        JavaType type = tf.constructRawCollectionType(ArrayList.class);
        assertTrue(type.isContainerType());
        assertEquals(TypeFactory.unknownType(), type.getContentType());
        type = tf.constructRawCollectionLikeType(CollectionLike.class); 
        assertTrue(type.isCollectionLikeType());
        assertEquals(TypeFactory.unknownType(), type.getContentType());

        
        type = tf.constructRawCollectionLikeType(String.class);
        assertTrue(type.isCollectionLikeType());
        assertEquals(TypeFactory.unknownType(), type.getContentType());
    }

// com.fasterxml.jackson.databind.type.TestTypeFactory::testRawMaps
    public void testRawMaps()
    {
        TypeFactory tf = TypeFactory.defaultInstance();
        JavaType type = tf.constructRawMapType(HashMap.class);
        assertTrue(type.isContainerType());
        assertEquals(TypeFactory.unknownType(), type.getKeyType());
        assertEquals(TypeFactory.unknownType(), type.getContentType());

        type = tf.constructRawMapLikeType(MapLike.class); 
        assertTrue(type.isMapLikeType());
        assertEquals(TypeFactory.unknownType(), type.getKeyType());
        assertEquals(TypeFactory.unknownType(), type.getContentType());

        
        type = tf.constructRawMapLikeType(String.class);
        assertTrue(type.isMapLikeType());
        assertEquals(TypeFactory.unknownType(), type.getKeyType());
        assertEquals(TypeFactory.unknownType(), type.getContentType());
    }

// com.fasterxml.jackson.databind.type.TestTypeFactory::testMoreSpecificType
    public void testMoreSpecificType()
    {
        TypeFactory tf = TypeFactory.defaultInstance();

        JavaType t1 = tf.constructCollectionType(Collection.class, Object.class);
        JavaType t2 = tf.constructCollectionType(List.class, Object.class);
        assertSame(t2, tf.moreSpecificType(t1, t2));
        assertSame(t2, tf.moreSpecificType(t2, t1));

        t1 = tf.constructType(Double.class);
        t2 = tf.constructType(Number.class);
        assertSame(t1, tf.moreSpecificType(t1, t2));
        assertSame(t1, tf.moreSpecificType(t2, t1));

        
        t1 = tf.constructType(Double.class);
        t2 = tf.constructType(String.class);
        assertSame(t1, tf.moreSpecificType(t1, t2));
        assertSame(t2, tf.moreSpecificType(t2, t1));
    }

// com.fasterxml.jackson.databind.type.TestTypeFactory::testCacheClearing
    public void testCacheClearing()
    {
        TypeFactory tf = TypeFactory.defaultInstance().withModifier(null);
        assertEquals(0, tf._typeCache.size());
        tf.constructType(getClass());
        
        assertEquals(6, tf._typeCache.size());
        tf.clearCache();
        assertEquals(0, tf._typeCache.size());
    }

// com.fasterxml.jackson.databind.type.TestTypeFactory::testRawMapType
    public void testRawMapType()
    {
        TypeFactory tf = TypeFactory.defaultInstance().withModifier(null); 

        JavaType type = tf.constructParametricType(Wrapper1297.class, Map.class);
        assertNotNull(type);
        assertEquals(Wrapper1297.class, type.getRawClass());
    }

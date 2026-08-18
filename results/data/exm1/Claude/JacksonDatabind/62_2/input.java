// buggy code
    public CollectionDeserializer createContextual(DeserializationContext ctxt,
            BeanProperty property) throws JsonMappingException
    {
        // May need to resolve types for delegate-based creators:
        JsonDeserializer<Object> delegateDeser = null;
        if (_valueInstantiator != null) {
            if (_valueInstantiator.canCreateUsingDelegate()) {
                JavaType delegateType = _valueInstantiator.getDelegateType(ctxt.getConfig());
                if (delegateType == null) {
                    throw new IllegalArgumentException("Invalid delegate-creator definition for "+_collectionType
                            +": value instantiator ("+_valueInstantiator.getClass().getName()
                            +") returned true for 'canCreateUsingDelegate()', but null for 'getDelegateType()'");
                }
                delegateDeser = findDeserializer(ctxt, delegateType, property);
            }
        }
        // [databind#1043]: allow per-property allow-wrapping of single overrides:
        // 11-Dec-2015, tatu: Should we pass basic `Collection.class`, or more refined? Mostly
        //   comes down to "List vs Collection" I suppose... for now, pass Collection
        Boolean unwrapSingle = findFormatFeature(ctxt, property, Collection.class,
                JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY);
        // also, often value deserializer is resolved here:
        JsonDeserializer<?> valueDeser = _valueDeserializer;
        
        // May have a content converter
        valueDeser = findConvertingContentDeserializer(ctxt, property, valueDeser);
        final JavaType vt = _collectionType.getContentType();
        if (valueDeser == null) {
            valueDeser = ctxt.findContextualValueDeserializer(vt, property);
        } else { // if directly assigned, probably not yet contextual, so:
            valueDeser = ctxt.handleSecondaryContextualization(valueDeser, property, vt);
        }
        // and finally, type deserializer needs context as well
        TypeDeserializer valueTypeDeser = _valueTypeDeserializer;
        if (valueTypeDeser != null) {
            valueTypeDeser = valueTypeDeser.forProperty(property);
        }
        return withResolved(delegateDeser, valueDeser, valueTypeDeser, unwrapSingle);
    }

// relevant test
// com.fasterxml.jackson.databind.filter.JsonIncludeTest::testGlobal
    public void testGlobal() throws IOException
    {
        Map<String,Object> result = writeAndMap(MAPPER, new SimpleBean());
        assertEquals(2, result.size());
        assertEquals("a", result.get("a"));
        assertNull(result.get("b"));
        assertTrue(result.containsKey("b"));
    }

// com.fasterxml.jackson.databind.filter.JsonIncludeTest::testNonNullByClass
    public void testNonNullByClass() throws IOException
    {
        Map<String,Object> result = writeAndMap(MAPPER, new NoNullsBean());
        assertEquals(1, result.size());
        assertFalse(result.containsKey("a"));
        assertNull(result.get("a"));
        assertTrue(result.containsKey("b"));
        assertNull(result.get("b"));
    }

// com.fasterxml.jackson.databind.filter.JsonIncludeTest::testNonDefaultByClass
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

// com.fasterxml.jackson.databind.filter.JsonIncludeTest::testNonDefaultByClassNoCtor
    public void testNonDefaultByClassNoCtor() throws IOException
    {
        NonDefaultBeanXYZ bean = new NonDefaultBeanXYZ(1, 2, 0);
        String json = MAPPER.writeValueAsString(bean);
        assertEquals(aposToQuotes("{'x':1,'y':2}"), json);
    }

// com.fasterxml.jackson.databind.filter.JsonIncludeTest::testMixedMethod
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

// com.fasterxml.jackson.databind.filter.JsonIncludeTest::testDefaultForEmptyList
    public void testDefaultForEmptyList() throws IOException
    {
        assertEquals("{}", MAPPER.writeValueAsString(new ListBean()));
    }

// com.fasterxml.jackson.databind.filter.JsonIncludeTest::testNonEmptyDefaultArray
    public void testNonEmptyDefaultArray() throws IOException
    {
        assertEquals("{}", MAPPER.writeValueAsString(new ArrayBean()));
    }

// com.fasterxml.jackson.databind.filter.JsonIncludeTest::testDefaultForIntegers
    public void testDefaultForIntegers() throws IOException
    {
        assertEquals("{}", MAPPER.writeValueAsString(new DefaultIntBean(0, Integer.valueOf(0))));
        assertEquals("{\"i2\":1}", MAPPER.writeValueAsString(new DefaultIntBean(0, Integer.valueOf(1))));
        assertEquals("{\"i1\":3}", MAPPER.writeValueAsString(new DefaultIntBean(3, Integer.valueOf(0))));
    }

// com.fasterxml.jackson.databind.filter.JsonIncludeTest::testEmptyInclusionScalars
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

// com.fasterxml.jackson.databind.filter.JsonIncludeTest::testIssue1327
    public void testIssue1327() throws Exception {
        ObjectMapper om = new ObjectMapper();
        om.setSerializationInclusion(JsonInclude.Include.NON_NULL);

        final Issues1327Bean input = new Issues1327Bean();
        final String jsonString = om.writeValueAsString(input);

        if (jsonString.contains("myList")) {
            fail("Should not contain `myList`: "+jsonString);
        }
    }

// com.fasterxml.jackson.databind.filter.MapInclusionTest::testNonNullValueMapViaProp
    public void testNonNullValueMapViaProp() throws IOException
    {
        String json = MAPPER.writeValueAsString(new NoEmptiesMapContainer()
            .add("a", null)
            .add("b", ""));
        assertEquals(aposToQuotes("{}"), json);
    }

// com.fasterxml.jackson.databind.filter.NullSerializationTest::testSimple
    public void testSimple() throws Exception
    {
        assertEquals("null", MAPPER.writeValueAsString(null));
    }

// com.fasterxml.jackson.databind.filter.NullSerializationTest::testOverriddenDefaultNulls
    public void testOverriddenDefaultNulls() throws Exception
    {
        DefaultSerializerProvider sp = new DefaultSerializerProvider.Impl();
        sp.setNullValueSerializer(new NullSerializer());
        ObjectMapper m = new ObjectMapper();
        m.setSerializerProvider(sp);
        assertEquals("\"foobar\"", m.writeValueAsString(null));
    }

// com.fasterxml.jackson.databind.filter.NullSerializationTest::testCustomNulls
    public void testCustomNulls() throws Exception
    {
        ObjectMapper m = new ObjectMapper();
        m.setSerializerProvider(new MyNullProvider());
        assertEquals("{\"name\":\"foobar\"}", m.writeValueAsString(new Bean1()));
        assertEquals("{\"type\":null}", m.writeValueAsString(new Bean2()));
    }

// com.fasterxml.jackson.databind.filter.NullSerializationTest::testCustomNullForTrees
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

// com.fasterxml.jackson.databind.filter.NullSerializationTest::testNullSerializerForProperty
    public void testNullSerializerForProperty() throws Exception
    {
        assertEquals("{\"a\":\"foobar\"}", MAPPER.writeValueAsString(new BeanWithNullProps()));
    }

// com.fasterxml.jackson.databind.filter.ReadOnlyProperties95Test::testReadOnlyProp
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

// com.fasterxml.jackson.databind.filter.TestAnyGetterFiltering::testAnyGetterFiltering
    public void testAnyGetterFiltering() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        FilterProvider prov = new SimpleFilterProvider().addFilter("anyFilter",
                SimpleBeanPropertyFilter.filterOutAllExcept("b"));
        assertEquals("{\"b\":\"2\"}", mapper.writer(prov).writeValueAsString(new AnyBean()));
    }

// com.fasterxml.jackson.databind.filter.TestJsonFilter::testSimpleInclusionFilter
    public void testSimpleInclusionFilter() throws Exception
    {
        FilterProvider prov = new SimpleFilterProvider().addFilter("RootFilter",
                SimpleBeanPropertyFilter.filterOutAllExcept("a"));
        assertEquals("{\"a\":\"a\"}", MAPPER.writer(prov).writeValueAsString(new Bean()));

        
        ObjectMapper mapper = new ObjectMapper();
        mapper.setFilterProvider(prov);
        assertEquals("{\"a\":\"a\"}", mapper.writeValueAsString(new Bean()));
    }

// com.fasterxml.jackson.databind.filter.TestJsonFilter::testIncludeAllFilter
    public void testIncludeAllFilter() throws Exception
    {
        FilterProvider prov = new SimpleFilterProvider().addFilter("RootFilter",
                SimpleBeanPropertyFilter.serializeAll());
        assertEquals("{\"a\":\"a\",\"b\":\"b\"}", MAPPER.writer(prov).writeValueAsString(new Bean()));
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
        mapper.setFilterProvider(fp);
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

// com.fasterxml.jackson.databind.filter.TestMapFiltering::testNonNullValueMapViaProp
    public void testNonNullValueMapViaProp() throws IOException
    {
        String json = MAPPER.writeValueAsString(new NoNullValuesMapContainer()
            .add("a", "foo")
            .add("b", null)
            .add("c", "bar"));
        assertEquals(aposToQuotes("{'stuff':{'a':'foo','c':'bar'}}"), json);
    }

// com.fasterxml.jackson.databind.filter.TestMapFiltering::testMapFilteringWithAnnotations
    public void testMapFilteringWithAnnotations() throws Exception
    {
        FilterProvider prov = new SimpleFilterProvider().addFilter("filterX",
                new TestMapFilter());
        String json = MAPPER.writer(prov).writeValueAsString(new MapBean());
        
        assertEquals(aposToQuotes("{'values':{'a':2}}"), json);
    }

// com.fasterxml.jackson.databind.filter.TestMapFiltering::testMapNonNullValue
    public void testMapNonNullValue() throws IOException
    {
        String json = MAPPER.writeValueAsString(new NoNullsStringMap()
            .add("a", "foo")
            .add("b", null)
            .add("c", "bar"));
        assertEquals(aposToQuotes("{'a':'foo','c':'bar'}"), json);
    }

// com.fasterxml.jackson.databind.filter.TestMapFiltering::testMapNonEmptyValue
    public void testMapNonEmptyValue() throws IOException
    {
        String json = MAPPER.writeValueAsString(new NoEmptyStringsMap()
            .add("a", "foo")
            .add("b", "bar")
            .add("c", ""));
        assertEquals(aposToQuotes("{'a':'foo','b':'bar'}"), json);
    }

// com.fasterxml.jackson.databind.filter.TestMapFiltering::testMapAbsentValue
    public void testMapAbsentValue() throws IOException
    {
        String json = MAPPER.writeValueAsString(new NoAbsentStringMap()
            .add("a", "foo")
            .add("b", null));
        assertEquals(aposToQuotes("{'a':'foo'}"), json);
    }

// com.fasterxml.jackson.databind.filter.TestMapFiltering::testMapNullSerialization
    public void testMapNullSerialization() throws IOException
    {
        ObjectMapper m = new ObjectMapper();
        Map<String,String> map = new HashMap<String,String>();
        map.put("a", null);
        
        assertEquals("{\"a\":null}", m.writeValueAsString(map));
        
        m.configure(SerializationFeature.WRITE_NULL_MAP_VALUES, false);
        assertEquals("{}", m.writeValueAsString(map));
    }

// com.fasterxml.jackson.databind.filter.TestMapFiltering::testMapWithOnlyEmptyValues
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

// com.fasterxml.jackson.databind.filter.TestSimpleSerializationIgnore::testSimpleIgnore
    public void testSimpleIgnore() throws Exception
    {
        
        Map<String,Object> result = writeAndMap(MAPPER, new SizeClassEnabledIgnore());
        assertEquals(1, result.size());
        assertEquals(Integer.valueOf(1), result.get("x"));
        assertNull(result.get("y"));
    }

// com.fasterxml.jackson.databind.filter.TestSimpleSerializationIgnore::testDisabledIgnore
    public void testDisabledIgnore() throws Exception
    {
        
        Map<String,Object> result = writeAndMap(MAPPER, new SizeClassDisabledIgnore());
        assertEquals(2, result.size());
        assertEquals(Integer.valueOf(3), result.get("x"));
        assertEquals(Integer.valueOf(4), result.get("y"));
    }

// com.fasterxml.jackson.databind.filter.TestSimpleSerializationIgnore::testIgnoreOver
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

// com.fasterxml.jackson.databind.filter.TestSimpleSerializationIgnore::testIgnoreType
    public void testIgnoreType() throws Exception
    {
        assertEquals("{\"value\":13}", MAPPER.writeValueAsString(new NonIgnoredType()));
    }

// com.fasterxml.jackson.databind.filter.TestUnknownPropertyDeserialization::testUnknownHandlingDefault
    public void testUnknownHandlingDefault() throws Exception
    {
        try {
            MAPPER.readValue(new StringReader(JSON_UNKNOWN_FIELD), TestBean.class);
        } catch (JsonMappingException jex) {
            verifyException(jex, "Unrecognized field \"foo\"");
        }
    }

// com.fasterxml.jackson.databind.filter.TestUnknownPropertyDeserialization::testUnknownHandlingIgnoreWithHandler
    public void testUnknownHandlingIgnoreWithHandler() throws Exception
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
    public void testUnknownHandlingIgnoreWithHandlerAndObjectReader() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        mapper.clearProblemHandlers();
        TestBean result = mapper.readerFor(TestBean.class).withHandler(new MyHandler()).readValue(new StringReader(JSON_UNKNOWN_FIELD));
        assertNotNull(result);
        assertEquals(1, result._a);
        assertEquals(-1, result._b);
        assertEquals("foo:START_ARRAY", result._unknown);
    }

// com.fasterxml.jackson.databind.filter.TestUnknownPropertyDeserialization::testUnknownHandlingIgnoreWithFeature
    public void testUnknownHandlingIgnoreWithFeature() throws Exception
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
    public void testWithClassIgnore() throws Exception
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

// com.fasterxml.jackson.databind.filter.TestUnknownPropertyDeserialization::testIssue987
    public void testIssue987() throws Exception
    {
        ObjectMapper jsonMapper = new ObjectMapper();
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

// com.fasterxml.jackson.databind.interop.TestFormatDetection::testInvalid
    public void testInvalid() throws Exception
    {
        ObjectReader detecting = READER.forType(POJO.class);
        detecting = detecting.withFormatDetection(detecting);
        try {
            detecting.readValue(utf8Bytes("<POJO><x>1</x></POJO>"));
            fail("Should have failed");
        } catch (JsonProcessingException e) {
            verifyException(e, "Can not detect format from input");
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

// com.fasterxml.jackson.databind.introspect.BeanDescriptionTest::testClassDesc
    public void testClassDesc() throws Exception
    {
        BeanDescription beanDesc = MAPPER.getDeserializationConfig().introspect(MAPPER.constructType(DocumentedBean.class));
        assertEquals(CLASS_DESC, beanDesc.findClassDescription());
    }

// com.fasterxml.jackson.databind.introspect.BeanNamingTest::testSimple
    public void testSimple() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        assertFalse(mapper.isEnabled(MapperFeature.USE_STD_BEAN_NAMING));
        assertEquals(aposToQuotes("{'url':'http://foo'}"),
                mapper.writeValueAsString(new URLBean()));
        assertEquals(aposToQuotes("{'a':3}"),
                mapper.writeValueAsString(new ABean()));

        mapper = new ObjectMapper();
        mapper.enable(MapperFeature.USE_STD_BEAN_NAMING);
        assertEquals(aposToQuotes("{'URL':'http://foo'}"),
                mapper.writeValueAsString(new URLBean()));
        assertEquals(aposToQuotes("{'a':3}"),
                mapper.writeValueAsString(new ABean()));
    }

// com.fasterxml.jackson.databind.introspect.IntrospectorPairTest::testInclusionMerging
    public void testInclusionMerging() throws Exception
    {
        
        JsonInclude.Value v12 = introPair12.findPropertyInclusion(null);
        JsonInclude.Value v21 = introPair21.findPropertyInclusion(null);

        assertEquals(JsonInclude.Include.ALWAYS, v12.getContentInclusion());
        assertEquals(JsonInclude.Include.NON_ABSENT, v12.getValueInclusion());

        assertEquals(JsonInclude.Include.NON_EMPTY, v21.getContentInclusion());
        assertEquals(JsonInclude.Include.NON_ABSENT, v21.getValueInclusion());
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

// com.fasterxml.jackson.databind.introspect.TestAnnotionBundles::testKeepAnnotationBundle
    public void testKeepAnnotationBundle() throws Exception
    {
        MAPPER.setAnnotationIntrospector(new BundleAnnotationIntrospector());
        assertEquals("{\"important\":42}", MAPPER.writeValueAsString(new InformingHolder()));
    }

// com.fasterxml.jackson.databind.introspect.TestAnnotionBundles::testRecursiveBundles
    public void testRecursiveBundles() throws Exception
    {
        assertEquals("{\"unimportant\":42}", MAPPER.writeValueAsString(new RecursiveHolder()));
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
            verifyException(e, "no single-String constructor/factory");
        }
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
        ObjectMapper mapper = new ObjectMapper();
        JacksonAnnotationIntrospector ai = new JacksonAnnotationIntrospector();
        AnnotatedClass ac = AnnotatedClass.constructWithoutSuperTypes(TypeResolverBean.class, mapper.getSerializationConfig());
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

// com.fasterxml.jackson.databind.introspect.TestMixinMerging::testDisappearingMixins515
    public void testDisappearingMixins515() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        mapper.disable(MapperFeature.ALLOW_FINAL_FIELDS_AS_MUTATORS)
            .disable(MapperFeature.AUTO_DETECT_FIELDS)
            .disable(MapperFeature.AUTO_DETECT_GETTERS)
            .disable(MapperFeature.AUTO_DETECT_IS_GETTERS)
            .disable(MapperFeature.INFER_PROPERTY_MUTATORS);
        SimpleModule module = new SimpleModule("Test");
        module.setMixInAnnotation(Person.class, PersonMixin.class);        
        mapper.registerModule(module);

        assertEquals("{\"city\":\"Seattle\"}", mapper.writeValueAsString(new PersonImpl()));
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
        String translatedJavaName = PropertyNamingStrategy.UPPER_CAMEL_CASE.nameForField
    	        (null, null, "userName");
        assertEquals("UserName", translatedJavaName);

        translatedJavaName = PropertyNamingStrategy.UPPER_CAMEL_CASE.nameForField
                (null, null, "User");
        assertEquals("User", translatedJavaName);

        translatedJavaName = PropertyNamingStrategy.UPPER_CAMEL_CASE.nameForField
                (null, null, "user");
        assertEquals("User", translatedJavaName);
        translatedJavaName = PropertyNamingStrategy.UPPER_CAMEL_CASE.nameForField
                (null, null, "x");
        assertEquals("X", translatedJavaName);
    }

// com.fasterxml.jackson.databind.introspect.TestNamingStrategyStd::testIssue428PascalWithOverrides
    public void testIssue428PascalWithOverrides() throws Exception {

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

// com.fasterxml.jackson.databind.introspect.TestPOJOPropertiesCollector::testSimple
    public void testSimple()
    {
        POJOPropertiesCollector coll = collector(MAPPER,
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
        
        POJOPropertiesCollector coll = collector(MAPPER,
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
        POJOPropertiesCollector coll = collector(MAPPER,
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
        POJOPropertiesCollector coll = collector(MAPPER,
        		Empty.class, true);
        Map<String, POJOPropertyBuilder> props = coll.getPropertyMap();
        assertEquals(0, props.size());
    }

// com.fasterxml.jackson.databind.introspect.TestPOJOPropertiesCollector::testPartialIgnore
    public void testPartialIgnore()
    {
        POJOPropertiesCollector coll = collector(MAPPER,
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
        POJOPropertiesCollector coll = collector(MAPPER,
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
        POJOPropertiesCollector coll = collector(MAPPER,
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
        POJOPropertiesCollector coll = collector(MAPPER,
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
        POJOPropertiesCollector coll = collector(MAPPER,
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
        POJOPropertiesCollector coll = collector(MAPPER, ImplicitIgnores.class, false);
        
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
        POJOPropertiesCollector coll = collector(MAPPER, SortedProperties.class, false);
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
        
        POJOPropertiesCollector coll = collector(MAPPER, TypeTestBean.class, true);
        List<BeanPropertyDefinition> props = coll.getProperties();
        assertEquals(1, props.size());
        assertEquals("value", props.get(0).getName());
        AnnotatedMember m = props.get(0).getAccessor();
        assertTrue(m instanceof AnnotatedMethod);
        assertEquals(Integer.class, m.getRawType());

        
        coll = collector(MAPPER, TypeTestBean.class, false);
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
        
        beanDesc = MAPPER.getSerializationConfig().introspect(MAPPER.constructType(Issue701Bean.class));
        assertNotNull(beanDesc);
        
        beanDesc = MAPPER.getDeserializationConfig().introspect(MAPPER.constructType(Issue701Bean.class));
        assertNotNull(beanDesc);
    }

// com.fasterxml.jackson.databind.introspect.TestPOJOPropertiesCollector::testUseAnnotationsFalse
    public void testUseAnnotationsFalse() throws Exception
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
        BeanDescription beanDesc = MAPPER.getDeserializationConfig().introspect(MAPPER.constructType(Issue744Bean.class));
        assertNotNull(beanDesc);
        AnnotatedMethod setter = beanDesc.findAnySetter();
        assertNotNull(setter);
    }

// com.fasterxml.jackson.databind.introspect.TestPOJOPropertiesCollector::testPropertyDesc
    public void testPropertyDesc() throws Exception
    {
        
        BeanDescription beanDesc = MAPPER.getDeserializationConfig().introspect(MAPPER.constructType(PropDescBean.class));
        _verifyProperty(beanDesc, true, false, "13");
        
        beanDesc = MAPPER.getSerializationConfig().introspect(MAPPER.constructType(PropDescBean.class));
        _verifyProperty(beanDesc, true, false, "13");
    }

// com.fasterxml.jackson.databind.introspect.TestPOJOPropertiesCollector::testPropertyIndex
    public void testPropertyIndex() throws Exception
    {
        BeanDescription beanDesc = MAPPER.getDeserializationConfig().introspect(MAPPER.constructType(PropDescBean.class));
        _verifyProperty(beanDesc, false, true, "13");
        beanDesc = MAPPER.getSerializationConfig().introspect(MAPPER.constructType(PropDescBean.class));
        _verifyProperty(beanDesc, false, true, "13");
    }

// com.fasterxml.jackson.databind.introspect.TestPOJOPropertiesCollector::testDuplicateGetters
    public void testDuplicateGetters() throws Exception
    {
        POJOPropertiesCollector coll = collector(MAPPER, DuplicateGetterBean.class, true);
        List<BeanPropertyDefinition> props = coll.getProperties();
        assertEquals(1, props.size());
        BeanPropertyDefinition prop = props.get(0);
        assertEquals("bloop", prop.getName());
        assertTrue(prop.getGetter().hasAnnotation(A.class));
        assertTrue(prop.getGetter().hasAnnotation(B.class));
    }

// com.fasterxml.jackson.databind.introspect.TestPOJOPropertiesCollector::testDuplicateGettersCreator
    public void testDuplicateGettersCreator() throws Exception
    {
        POJOPropertiesCollector coll = collector(MAPPER, DuplicateGetterCreatorBean.class, true);
        List<BeanPropertyDefinition> props = coll.getProperties();
        assertEquals(1, props.size());
        POJOPropertyBuilder prop = (POJOPropertyBuilder) props.get(0);
        assertEquals("bloop", prop.getName());
        
        assertTrue(prop._getters.value.hasAnnotation(A.class));
        assertNotNull(prop._getters.next);
        assertTrue(prop._getters.next.value.hasAnnotation(A.class));
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

// com.fasterxml.jackson.databind.introspect.TestPropertyRename::testCreatorPropRenameWithIgnore
    public void testCreatorPropRenameWithIgnore() throws Exception
    {
        Bean323WithIgnore input = new Bean323WithIgnore(7);
        assertEquals("{\"b\":7}", objectWriter().writeValueAsString(input));
    }

// com.fasterxml.jackson.databind.introspect.TestPropertyRename::testCreatorPropRenameWithCleave
    public void testCreatorPropRenameWithCleave() throws Exception
    {
        assertEquals("{\"a\":7,\"b\":7}",
        		objectWriter().writeValueAsString(new Bean323WithExplicitCleave1(7)));
        
        assertEquals("{\"b\":7}", objectWriter().writeValueAsString(new Bean323WithExplicitCleave2(7)));
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

// com.fasterxml.jackson.databind.introspect.TransientTest::testTransientFieldHandling
    public void testTransientFieldHandling() throws Exception
    {
        
        assertEquals(aposToQuotes("{'x':42,'value':3}"),
                MAPPER.writeValueAsString(new ClassyTransient()));

        
        ObjectMapper m = new ObjectMapper()
            .enable(MapperFeature.PROPAGATE_TRANSIENT_MARKER);
        assertEquals(aposToQuotes("{'x':42}"),
                m.writeValueAsString(new ClassyTransient()));
    }

// com.fasterxml.jackson.databind.introspect.TransientTest::testBeanTransient
    public void testBeanTransient() throws Exception
    {
        assertEquals(aposToQuotes("{'y':4}"),
                MAPPER.writeValueAsString(new BeanTransient()));
    }

// com.fasterxml.jackson.databind.jsonschema.NewSchemaTest::testBasicTraversal
    public void testBasicTraversal() throws Exception
    {
        MAPPER.acceptJsonFormatVisitor(POJO.class, new JsonFormatVisitorWrapper.Base());
    }

// com.fasterxml.jackson.databind.jsonschema.NewSchemaTest::testSimpleEnum
    public void testSimpleEnum() throws Exception
    {
        final Set<String> values = new TreeSet<String>();
        ObjectWriter w = MAPPER.writer(SerializationFeature.WRITE_ENUMS_USING_TO_STRING);

        w.acceptJsonFormatVisitor(TestEnum.class, new JsonFormatVisitorWrapper.Base() {
            @Override
            public JsonStringFormatVisitor expectStringFormat(JavaType type) {
                return new JsonStringFormatVisitor() {
                    @Override
                    public void enumTypes(Set<String> enums) {
                        values.addAll(enums);
                    }

                    @Override
                    public void format(JsonValueFormat format) { }
                };
            }
        });

        assertEquals(3, values.size());
        TreeSet<String> exp = new TreeSet<String>(Arrays.asList(
                        "ToString:A",
                        "ToString:B",
                        "ToString:C"
                        ));
        assertEquals(exp, values);
    }

// com.fasterxml.jackson.databind.jsonschema.NewSchemaTest::testEnumWithJsonValue
    public void testEnumWithJsonValue() throws Exception
    {
        final Set<String> values = new TreeSet<String>();
        MAPPER.acceptJsonFormatVisitor(TestEnumWithJsonValue.class, new JsonFormatVisitorWrapper.Base() {
            @Override
            public JsonStringFormatVisitor expectStringFormat(JavaType type) {
                return new JsonStringFormatVisitor() {
                    @Override
                    public void enumTypes(Set<String> enums) {
                        values.addAll(enums);
                    }

                    @Override
                    public void format(JsonValueFormat format) { }
                };
            }
        });

        assertEquals(3, values.size());
        TreeSet<String> exp = new TreeSet<String>(Arrays.asList(
                        "value-A",
                        "value-B",
                        "value-C"
                        ));
        assertEquals(exp, values);
    }

// com.fasterxml.jackson.databind.jsonschema.NewSchemaTest::testJsonValueFormatHandling
    public void testJsonValueFormatHandling() throws Exception
    {
        
        final String EXP = quote("host-name");
        assertEquals(EXP, MAPPER.writeValueAsString(JsonValueFormat.HOST_NAME));

        
        assertSame(JsonValueFormat.HOST_NAME, MAPPER.readValue(EXP, JsonValueFormat.class));
    }

// com.fasterxml.jackson.databind.jsonschema.NewSchemaTest::testSimpleNumbers
    public void testSimpleNumbers() throws Exception
    {
        final StringBuilder sb = new StringBuilder();
        
        MAPPER.acceptJsonFormatVisitor(Numbers.class,
                new JsonFormatVisitorWrapper.Base() {
            @Override
            public JsonObjectFormatVisitor expectObjectFormat(final JavaType type) {
                return new JsonObjectFormatVisitor.Base(getProvider()) {
                    @Override
                    public void optionalProperty(BeanProperty prop) throws JsonMappingException {
                        sb.append("[optProp ").append(prop.getName()).append("(");
                        JsonSerializer<Object> ser = null;
                        if (prop instanceof BeanPropertyWriter) {
                            BeanPropertyWriter bpw = (BeanPropertyWriter) prop;
                            ser = bpw.getSerializer();
                        }
                        final SerializerProvider prov = getProvider();
                        if (ser == null) {
                            ser = prov.findValueSerializer(prop.getType(), prop);
                        }
                        ser.acceptJsonFormatVisitor(new JsonFormatVisitorWrapper.Base() {
                            @Override
                            public JsonNumberFormatVisitor expectNumberFormat(
                                    JavaType t) throws JsonMappingException {
                                return new JsonNumberFormatVisitor() {
                                    @Override
                                    public void format(JsonValueFormat format) {
                                        sb.append("[numberFormat=").append(format).append("]");
                                    }

                                    @Override
                                    public void enumTypes(Set<String> enums) { }

                                    @Override
                                    public void numberType(NumberType numberType) {
                                        sb.append("[numberType=").append(numberType).append("]");
                                    }
                                };
                            }

                            @Override
                            public JsonIntegerFormatVisitor expectIntegerFormat(JavaType t) throws JsonMappingException {
                                return new JsonIntegerFormatVisitor() {
                                    @Override
                                    public void format(JsonValueFormat format) {
                                        sb.append("[integerFormat=").append(format).append("]");
                                    }

                                    @Override
                                    public void enumTypes(Set<String> enums) { }

                                    @Override
                                    public void numberType(NumberType numberType) {
                                        sb.append("[numberType=").append(numberType).append("]");
                                    }
                                };
                            }
                        }, prop.getType());

                        sb.append(")]");
                    }
                };
            }
        });
        assertEquals("[optProp dec([numberType=BIG_DECIMAL])][optProp bigInt([numberType=BIG_INTEGER])]",
                sb.toString());
    }

// com.fasterxml.jackson.databind.jsonschema.TestGenerateJsonSchema::testOldSchemaGeneration
    public void testOldSchemaGeneration() throws Exception
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
    public void testSchemaSerialization() throws Exception
    {
        JsonSchema jsonSchema = MAPPER.generateJsonSchema(SimpleBean.class);
        Map<String,Object> result = writeAndMap(MAPPER, jsonSchema);
        assertNotNull(result);
        
        assertEquals("object", result.get("type"));
        
        assertNull(result.get("required"));
        assertNotNull(result.get("properties"));
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

// com.fasterxml.jackson.databind.jsonschema.TestGenerateJsonSchema::testNumberTypes
    public void testNumberTypes()  throws Exception
    {
        JsonSchema jsonSchema = MAPPER.generateJsonSchema(Numbers.class);
        String json = quotesToApos(jsonSchema.toString());
        String EXP = "{'type':'object',"
                +"'properties':{'dec':{'type':'number'},"
                +"'bigInt':{'type':'integer'}}}";
        assertEquals(EXP, json);
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

// com.fasterxml.jackson.databind.jsontype.AbstracTypeMapping1186Test::testDeserializeMyContainer
    public void testDeserializeMyContainer() throws Exception {
        com.fasterxml.jackson.databind.Module module = new SimpleModule().addAbstractTypeMapping(IContainer.class, MyContainer.class);
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

// com.fasterxml.jackson.databind.jsontype.ExternalTypeId198Test::testFails
    public void testFails() throws Exception {
      String json = "{ \"name\": \"foo\", \"attack\":\"right\" } }";

      Character character = MAPPER.readValue(json, Character.class);

      assertNotNull(character);
      assertNotNull(character.attack);
      assertEquals("foo", character.name);
    }

// com.fasterxml.jackson.databind.jsontype.ExternalTypeId198Test::testWorks
    public void testWorks() throws Exception {
      String json = "{ \"name\": \"foo\", \"preferredAttack\": \"KICK\", \"attack\":\"right\" } }";

      Character character = MAPPER.readValue(json, Character.class);

      assertNotNull(character);
      assertNotNull(character.attack);
      assertEquals("foo", character.name);
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

// com.fasterxml.jackson.databind.jsontype.TestDefaultForMaps::testList
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

// com.fasterxml.jackson.databind.jsontype.TestDefaultForMaps::testMap
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

// com.fasterxml.jackson.databind.jsontype.TestDefaultForObject::testBeanAsObjectUsingAsProperty
    public void testBeanAsObjectUsingAsProperty() throws Exception
    {
        ObjectMapper m = new ObjectMapper();
        m.enableDefaultTypingAsProperty(ObjectMapper.DefaultTyping.NON_FINAL,
                ".hype");
        
        String json = m.writeValueAsString(new StringBean("abc"));
        
        
        Object result = m.readValue(json, Object.class);
        assertNotNull(result);
        assertEquals(StringBean.class, result.getClass());
        assertEquals("abc", ((StringBean) result).name);
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

        
        TokenBuffer buf = new TokenBuffer(mapper, false);
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
        buf.close();

        
        buf = new TokenBuffer(mapper, false);
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
        buf.close();

        
        buf = new TokenBuffer(mapper, false);
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
        buf.close();
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

// com.fasterxml.jackson.databind.jsontype.TestDefaultForObject::testNoGoWithExternalProperty
    public void testNoGoWithExternalProperty() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        try {
            mapper.enableDefaultTyping(ObjectMapper.DefaultTyping.JAVA_LANG_OBJECT,
                    JsonTypeInfo.As.EXTERNAL_PROPERTY);
            fail("Should not have passed");
        } catch (IllegalArgumentException e) {
            verifyException(e, "Can not use includeAs of EXTERNAL_PROPERTY");
        }
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

// com.fasterxml.jackson.databind.jsontype.TestDefaultForTreeNodes::testValueAsStringWithDefaultTyping
    public void testValueAsStringWithDefaultTyping() throws Exception
    {
        Foo foo = new Foo("baz");
        String json = DEFAULT_MAPPER.writeValueAsString(foo);

        JsonNode jsonNode = DEFAULT_MAPPER.readTree(json);
        assertEquals(jsonNode.get("bar").textValue(), foo.bar);
    }

// com.fasterxml.jackson.databind.jsontype.TestDefaultForTreeNodes::testValueToTreeWithDefaultTyping
    public void testValueToTreeWithDefaultTyping() throws Exception
    {
        Foo foo = new Foo("baz");
        JsonNode jsonNode = DEFAULT_MAPPER.valueToTree(foo);
        assertEquals(jsonNode.get("bar").textValue(), foo.bar);
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

// com.fasterxml.jackson.databind.jsontype.TestExternalId::testExternalTypeIdWithNull
    public void testExternalTypeIdWithNull() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerSubtypes(ValueBean.class);
        ExternalBean b;
        b = mapper.readValue(aposToQuotes("{'bean':null,'extType':'vbean'}"),
                ExternalBean.class);
        assertNotNull(b);
        b = mapper.readValue(aposToQuotes("{'extType':'vbean','bean':null}"),
                ExternalBean.class);
        assertNotNull(b);
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

        
        result = mapper.readValue("{\"extType\":\"vbean\", \"bean\":{\"value\":13}}", ExternalBean.class);
        assertNotNull(result);
        assertNotNull(result.bean);
        vb = (ValueBean) result.bean;
        assertEquals(13, vb.value);
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

        result = MAPPER.readValue("{\"i\":4,\"extType\":\"funk\"}",
                FunkyExternalBean.class);
        assertNotNull(result);
        assertEquals(4, result.i);
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

// com.fasterxml.jackson.databind.jsontype.TestExternalId::testExternalTypeWithProp222
    public void testExternalTypeWithProp222() throws Exception
    {
        final ObjectMapper mapper = new ObjectMapper();
        Issue222Bean input = new Issue222Bean(13);
        String json = mapper.writeValueAsString(input);
        assertEquals("{\"value\":{\"x\":13},\"type\":\"foo\"}", json);
    }

// com.fasterxml.jackson.databind.jsontype.TestExternalId::testInverseExternalId928
    public void testInverseExternalId928() throws Exception
    {
        final String CLASS = Payload928.class.getName();

        ObjectMapper mapper = new ObjectMapper();

        final String successCase = "{\"payload\":{\"something\":\"test\"},\"class\":\""+CLASS+"\"}";
        Envelope928 envelope1 = mapper.readValue(successCase, Envelope928.class);
        assertNotNull(envelope1);
        assertEquals(Payload928.class, envelope1._payload.getClass());

        
        final String failCase = "{\"class\":\""+CLASS+"\",\"payload\":{\"something\":\"test\"}}";
        Envelope928 envelope2 = mapper.readValue(failCase, Envelope928.class);
        assertNotNull(envelope2);
        assertEquals(Payload928.class, envelope2._payload.getClass());
    }

// com.fasterxml.jackson.databind.jsontype.TestExternalId::testBigDecimal965
    public void testBigDecimal965() throws Exception
    {

        Wrapper965 w = new Wrapper965();
        w.typeEnum = Type965.BIG_DECIMAL;
        final String NUM_STR = "-10000000000.0000000001";
        w.value = new BigDecimal(NUM_STR);

        String json = MAPPER.writeValueAsString(w);

        
        if (!json.contains(NUM_STR)) {
            fail("JSON content should contain value '"+NUM_STR+"', does not appear to: "+json);
        }
        
        Wrapper965 w2 = MAPPER.readerFor(Wrapper965.class)
                .with(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS)
                .readValue(json);

        assertEquals(w.typeEnum, w2.typeEnum);
        assertTrue(String.format("Expected %s = %s; got back %s = %s",
            w.value.getClass().getSimpleName(), w.value.toString(), w2.value.getClass().getSimpleName(), w2.value.toString()),
            w.value.equals(w2.value));
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

// com.fasterxml.jackson.databind.jsontype.TestOverlappingTypeIdNames312::testOverlappingNameDeser
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

// com.fasterxml.jackson.databind.jsontype.TestOverlappingTypeIdNames312::testOverlappingNameSer
    public void testOverlappingNameSer() throws Exception
    {
        assertEquals(aposToQuotes("{'type':'a','value':1}"),
                MAPPER.writeValueAsString(new Impl312B1()));
        assertEquals(aposToQuotes("{'type':'a','value':1}"),
                MAPPER.writeValueAsString(new Impl312B2()));
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
    }

// com.fasterxml.jackson.databind.jsontype.TestSubtypes::testSerialization
    public void testSerialization() throws Exception
    {
        
        SubB bean = new SubB();
        assertEquals("{\"@type\":\"TypeB\",\"b\":1}", MAPPER.writeValueAsString(bean));

        
        ObjectMapper mapper = new ObjectMapper();
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
        
        SuperTypeWithDefault bean = MAPPER.readValue("{\"a\":13}", SuperTypeWithDefault.class);
        assertEquals(DefaultImpl.class, bean.getClass());
        assertEquals(13, ((DefaultImpl) bean).a);

        
        bean = MAPPER.readValue("{\"a\":14,\"#type\":\"foobar\"}", SuperTypeWithDefault.class);
        assertEquals(DefaultImpl.class, bean.getClass());
        assertEquals(14, ((DefaultImpl) bean).a);

        bean = MAPPER.readValue("{\"#type\":\"foobar\",\"a\":15}", SuperTypeWithDefault.class);
        assertEquals(DefaultImpl.class, bean.getClass());
        assertEquals(15, ((DefaultImpl) bean).a);

        bean = MAPPER.readValue("{\"#type\":\"foobar\"}", SuperTypeWithDefault.class);
        assertEquals(DefaultImpl.class, bean.getClass());
        assertEquals(0, ((DefaultImpl) bean).a);
    }

// com.fasterxml.jackson.databind.jsontype.TestSubtypes::testDefaultImplViaModule
    public void testDefaultImplViaModule() throws Exception
    {
        final String JSON = "{\"a\":123}";
        
        
        try {
            MAPPER.readValue(JSON, SuperTypeWithoutDefault.class);
            fail("Expected an exception");
        } catch (JsonMappingException e) {
            verifyException(e, "missing property");
        }

        
        ObjectMapper mapper = new ObjectMapper();
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

// com.fasterxml.jackson.databind.jsontype.TestSubtypes::testErrorMessage
    public void testErrorMessage() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        try {
            mapper.readValue("{ \"type\": \"z\"}", BaseX.class);
            fail("Should have failed");
        } catch (JsonMappingException e) {
            verifyException(e, "known type ids =");
        }
    }

// com.fasterxml.jackson.databind.jsontype.TestSubtypes::testViaAtomic
    public void testViaAtomic() throws Exception {
        AtomicWrapper input = new AtomicWrapper(3);
        String json = MAPPER.writeValueAsString(input);

        AtomicWrapper output = MAPPER.readValue(json, AtomicWrapper.class);
        assertNotNull(output);
        assertEquals(ImplX.class, output.value.getClass());
        assertEquals(3, ((ImplX) output.value).x);
    }

// com.fasterxml.jackson.databind.jsontype.TestSubtypes::testIssue1125NonDefault
    public void testIssue1125NonDefault() throws Exception
    {
        String json = MAPPER.writeValueAsString(new Issue1125Wrapper(new Impl1125(1, 2, 3)));
        
        Issue1125Wrapper result = MAPPER.readValue(json, Issue1125Wrapper.class);
        assertNotNull(result.value);
        assertEquals(Impl1125.class, result.value.getClass());
        Impl1125 impl = (Impl1125) result.value;
        assertEquals(1, impl.a);
        assertEquals(2, impl.b);
        assertEquals(3, impl.c);
    }

// com.fasterxml.jackson.databind.jsontype.TestSubtypes::testIssue1125WithDefault
    public void testIssue1125WithDefault() throws Exception
    {
        Issue1125Wrapper result = MAPPER.readValue(aposToQuotes("{'value':{'a':3,'def':9,'b':5}}"),
        		Issue1125Wrapper.class);
        assertNotNull(result.value);
        assertEquals(Default1125.class, result.value.getClass());
        Default1125 impl = (Default1125) result.value;
        assertEquals(3, impl.a);
        assertEquals(5, impl.b);
        assertEquals(9, impl.def);
    }

// com.fasterxml.jackson.databind.jsontype.TestSubtypesExistingProperty::testExistingPropertySerializationFruits
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

// com.fasterxml.jackson.databind.jsontype.TestSubtypesExistingProperty::testSimpleClassAsExistingPropertyDeserializationFruits
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

// com.fasterxml.jackson.databind.jsontype.TestSubtypesExistingProperty::testExistingPropertySerializationAnimals
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

// com.fasterxml.jackson.databind.jsontype.TestSubtypesExistingProperty::testSimpleClassAsExistingPropertyDeserializationAnimals
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

// com.fasterxml.jackson.databind.jsontype.TestSubtypesExistingProperty::testExistingPropertySerializationCars
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

// com.fasterxml.jackson.databind.jsontype.TestSubtypesExistingProperty::testSimpleClassAsExistingPropertyDeserializationCars
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

// com.fasterxml.jackson.databind.jsontype.TestTypedArrayDeserialization::testIntList
    public void testIntList() throws Exception
    {
        ObjectMapper m = new ObjectMapper();
        
        String JSON = "{\""+TypedListAsWrapper.class.getName()+"\":[4,5, 6]}";
        JavaType type = TypeFactory.defaultInstance().constructCollectionType(TypedListAsWrapper.class, Integer.class);        
        TypedListAsWrapper<Integer> result = m.readValue(JSON, type);
        assertNotNull(result);
        assertEquals(3, result.size());
        assertEquals(Integer.valueOf(4), result.get(0));
        assertEquals(Integer.valueOf(5), result.get(1));
        assertEquals(Integer.valueOf(6), result.get(2));
    }

// com.fasterxml.jackson.databind.jsontype.TestTypedArrayDeserialization::testBooleanListAsProp
    public void testBooleanListAsProp() throws Exception
    {
        ObjectMapper m = new ObjectMapper();
        
        String JSON = "[\""+TypedListAsProp.class.getName()+"\",[true, false]]";
        JavaType type = TypeFactory.defaultInstance().constructCollectionType(TypedListAsProp.class, Boolean.class);        
        TypedListAsProp<Object> result = m.readValue(JSON, type);
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(Boolean.TRUE, result.get(0));
        assertEquals(Boolean.FALSE, result.get(1));
    }

// com.fasterxml.jackson.databind.jsontype.TestTypedArrayDeserialization::testLongListAsWrapper
    public void testLongListAsWrapper() throws Exception
    {
        ObjectMapper m = new ObjectMapper();
        
        
        String JSON = "{\""+TypedListAsWrapper.class.getName()+"\":[1, 3]}";
        JavaType type = TypeFactory.defaultInstance().constructCollectionType(TypedListAsWrapper.class, Long.class);        
        TypedListAsWrapper<Object> result = m.readValue(JSON, type);
        assertNotNull(result);
        assertEquals(2, result.size());

        assertEquals(Long.class, result.get(0).getClass());
        assertEquals(Long.valueOf(1), result.get(0));
        assertEquals(Long.class, result.get(1).getClass());
        assertEquals(Long.valueOf(3), result.get(1));
    }

// com.fasterxml.jackson.databind.jsontype.TestTypedArrayDeserialization::testLongArray
    public void testLongArray() throws Exception
    {
        ObjectMapper m = new ObjectMapper();
        
        m.addMixIn(long[].class, WrapperMixIn.class);
        String JSON = "{\""+long[].class.getName()+"\":[5, 6, 7]}";
        long[] value = m.readValue(JSON, long[].class);
        assertNotNull(value);
        assertEquals(3, value.length);
        assertArrayEquals(new long[] { 5L, 6L, 7L} , value);
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
        m.addMixIn(int[].class, WrapperMixIn.class);
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

// com.fasterxml.jackson.databind.jsontype.TestTypedContainerSerialization::testPolymorphicWithContainer
    public void testPolymorphicWithContainer() throws Exception
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
            JavaType rootType = TypeFactory.defaultInstance().constructParametrizedType(Iterator.class, Iterator.class, Animal.class);
            String json = mapper.writerFor(rootType).writeValueAsString(animals.iterator());
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
            String json = mapper.writerFor(typeRef).writeValueAsString(l);

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
        m.addMixIn(Animal.class, TypeWithWrapper.class);
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
        m.addMixIn(Animal.class, TypeWithArray.class);
        
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

// com.fasterxml.jackson.databind.jsontype.TestTypedSerialization::testSimpleClassAsProperty
    public void testSimpleClassAsProperty() throws Exception
    {
        Map<String,Object> result = writeAndMap(MAPPER, new Cat("Beelzebub", "tabby"));
        assertEquals(3, result.size());
        assertEquals("Beelzebub", result.get("name"));
        assertEquals("tabby", result.get("furColor"));
        
        String classProp = Id.CLASS.getDefaultPropertyName();
        assertEquals(Cat.class.getName(), result.get(classProp));
    }

// com.fasterxml.jackson.databind.jsontype.TestTypedSerialization::testTypeAsWrapper
    public void testTypeAsWrapper() throws Exception
    {
        ObjectMapper m = new ObjectMapper();
        m.addMixIn(Animal.class, TypeWithWrapper.class);
        Map<String,Object> result = writeAndMap(m, new Cat("Venla", "black"));
        
        assertEquals(1, result.size());
        
        Map<?,?> cat = (Map<?,?>) result.get(".TestTypedSerialization$Cat");
        assertNotNull(cat);
        assertEquals(2, cat.size());
        assertEquals("Venla", cat.get("name"));
        assertEquals("black", cat.get("furColor"));
    }

// com.fasterxml.jackson.databind.jsontype.TestTypedSerialization::testTypeAsArray
    public void testTypeAsArray() throws Exception
    {
        ObjectMapper m = new ObjectMapper();
        m.addMixIn(Animal.class, TypeWithArray.class);
        
        Map<String,Object> result = writeAndMap(m, new AnimalWrapper(new Dog("Amadeus", 7)));
        
        assertEquals(1, result.size());
        List<?> l = (List<?>) result.get("animal");
        assertNotNull(l);
        assertEquals(2, l.size());
        assertEquals(Dog.class.getName(), l.get(0));
        Map<?,?> doggie = (Map<?,?>) l.get(1);
        assertNotNull(doggie);
        assertEquals(2, doggie.size());
        assertEquals("Amadeus", doggie.get("name"));
        assertEquals(Integer.valueOf(7), doggie.get("boneCount"));
    }

// com.fasterxml.jackson.databind.jsontype.TestTypedSerialization::testInArray
    public void testInArray() throws Exception
    {
        
        ObjectMapper m = new ObjectMapper();
        
        m.disableDefaultTyping();
        
        Animal[] animals = new Animal[] { new Cat("Miuku", "white"), new Dog("Murre", 9) };
        Map<String,Object> map = new HashMap<String,Object>();
        map.put("a", animals);
        String json = m.writeValueAsString(map);
        Map<String,Object> result = m.readValue(json, Map.class);
        assertEquals(1, result.size());
        Object ob = result.get("a");
        if (!(ob instanceof List<?>)) {
            
            fail("Did not map to entry with 'a' as List (but as "+ob.getClass().getName()+"): JSON == '"+json+"'");
        }
        List<?> l = (List<?>)ob;
        assertNotNull(l);
        assertEquals(2, l.size());
        Map<?,?> a1 = (Map<?,?>) l.get(0);
        assertEquals(3, a1.size());
        String classProp = Id.CLASS.getDefaultPropertyName();
        assertEquals(Cat.class.getName(), a1.get(classProp));
        Map<?,?> a2 = (Map<?,?>) l.get(1);
        assertEquals(3, a2.size());
        assertEquals(Dog.class.getName(), a2.get(classProp));
    }

// com.fasterxml.jackson.databind.jsontype.TestTypedSerialization::testEmptyBean
    public void testEmptyBean() throws Exception
    {
        ObjectMapper m = new ObjectMapper();
        m.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        assertEquals("{\"@type\":\"empty\"}", m.writeValueAsString(new Empty()));
    }

// com.fasterxml.jackson.databind.jsontype.TestTypedSerialization::testTypedMaps
    public void testTypedMaps() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();

        Map<Long, Collection<Super>> map = new HashMap<Long, Collection<Super>>();
        List<Super> list = new ArrayList<Super>();
        list.add(new A());
        map.put(1L, list);
        String json = mapper.writerFor(new TypeReference<Map<Long, Collection<Super>>>() {}).writeValueAsString(map);
        assertTrue("JSON does not contain '@class': "+json, json.contains("@class"));
    }

// com.fasterxml.jackson.databind.jsontype.TestVisibleTypeId::testVisibleWithProperty
    public void testVisibleWithProperty() throws Exception
    {
        String json = MAPPER.writeValueAsString(new PropertyBean());
        
        assertEquals("{\"type\":\"BaseType\",\"a\":3}", json);
        
        PropertyBean result = MAPPER.readValue(json, PropertyBean.class);
        assertEquals("BaseType", result.type);

        
        result = MAPPER.readValue("{\"a\":7, \"type\":\"BaseType\"}", PropertyBean.class);
        assertEquals(7, result.a);
        assertEquals("BaseType", result.type);
    }

// com.fasterxml.jackson.databind.jsontype.TestVisibleTypeId::testVisibleWithWrapperArray
    public void testVisibleWithWrapperArray() throws Exception
    {
        String json = MAPPER.writeValueAsString(new WrapperArrayBean());
        
        assertEquals("[\"ArrayType\",{\"a\":1}]", json);
        
        WrapperArrayBean result = MAPPER.readValue(json, WrapperArrayBean.class);
        assertEquals("ArrayType", result.type);
        assertEquals(1, result.a);
    }

// com.fasterxml.jackson.databind.jsontype.TestVisibleTypeId::testVisibleWithWrapperObject
    public void testVisibleWithWrapperObject() throws Exception
    {
        String json = MAPPER.writeValueAsString(new WrapperObjectBean());
        assertEquals("{\"ObjectType\":{\"a\":2}}", json);
        
        WrapperObjectBean result = MAPPER.readValue(json, WrapperObjectBean.class);
        assertEquals("ObjectType", result.type);
    }

// com.fasterxml.jackson.databind.jsontype.TestVisibleTypeId::testTypeIdFromProperty
    public void testTypeIdFromProperty() throws Exception
    {
        assertEquals("{\"type\":\"SomeType\",\"a\":3}",
                MAPPER.writeValueAsString(new TypeIdFromFieldProperty()));
    }

// com.fasterxml.jackson.databind.jsontype.TestVisibleTypeId::testTypeIdFromArray
    public void testTypeIdFromArray() throws Exception
    {
        assertEquals("[\"SomeType\",{\"a\":3}]",
                MAPPER.writeValueAsString(new TypeIdFromFieldArray()));
    }

// com.fasterxml.jackson.databind.jsontype.TestVisibleTypeId::testTypeIdFromObject
    public void testTypeIdFromObject() throws Exception
    {
        assertEquals("{\"SomeType\":{\"a\":3}}",
                MAPPER.writeValueAsString(new TypeIdFromMethodObject()));
    }

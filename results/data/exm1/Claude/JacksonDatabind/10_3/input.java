// buggy code
    public AnyGetterWriter(BeanProperty property,
            AnnotatedMember accessor, MapSerializer serializer)
    {
        _accessor = accessor;
        _property = property;
            _mapSerializer = (MapSerializer) serializer;
    }

    public void getAndSerialize(Object bean, JsonGenerator gen, SerializerProvider provider)
        throws Exception
    {
        Object value = _accessor.getValue(bean);
        if (value == null) {
            return;
        }
        if (!(value instanceof Map<?,?>)) {
            throw new JsonMappingException("Value returned by 'any-getter' ("
                    +_accessor.getName()+"()) not java.util.Map but "+value.getClass().getName());
        }
        // 23-Feb-2015, tatu: Nasty, but has to do (for now)
        if (_mapSerializer != null) {
            _mapSerializer.serializeFields((Map<?,?>) value, gen, provider);
            return;
        }
    }

    public void getAndFilter(Object bean, JsonGenerator gen, SerializerProvider provider,
            PropertyFilter filter)
                    throws Exception
    {
        Object value = _accessor.getValue(bean);
        if (value == null) {
            return;
        }
        if (!(value instanceof Map<?,?>)) {
            throw new JsonMappingException("Value returned by 'any-getter' ("
                    +_accessor.getName()+"()) not java.util.Map but "+value.getClass().getName());
        }
        // 19-Oct-2014, tatu: Should we try to support @JsonInclude options here?
        if (_mapSerializer != null) {
            _mapSerializer.serializeFilteredFields((Map<?,?>) value, gen, provider, filter, null);
            return;
        }
        // ... not sure how custom handler would do it
    }

    public void resolve(SerializerProvider provider) throws JsonMappingException
    {
        // 05-Sep-2013, tatu: I _think_ this can be considered a primary property...
        _mapSerializer = (MapSerializer) provider.handlePrimaryContextualization(_mapSerializer, _property);
    }

    protected JsonSerializer<Object> constructBeanSerializer(SerializerProvider prov,
            BeanDescription beanDesc)
        throws JsonMappingException
    {
        // 13-Oct-2010, tatu: quick sanity check: never try to create bean serializer for plain Object
        // 05-Jul-2012, tatu: ... but we should be able to just return "unknown type" serializer, right?
        if (beanDesc.getBeanClass() == Object.class) {
            return prov.getUnknownTypeSerializer(Object.class);
//            throw new IllegalArgumentException("Can not create bean serializer for Object.class");
        }
        final SerializationConfig config = prov.getConfig();
        BeanSerializerBuilder builder = constructBeanSerializerBuilder(beanDesc);
        builder.setConfig(config);

        // First: any detectable (auto-detect, annotations) properties to serialize?
        List<BeanPropertyWriter> props = findBeanProperties(prov, beanDesc, builder);
        if (props == null) {
            props = new ArrayList<BeanPropertyWriter>();
        }
        // [databind#638]: Allow injection of "virtual" properties:
        prov.getAnnotationIntrospector().findAndAddVirtualProperties(config, beanDesc.getClassInfo(), props);

        // [JACKSON-440] Need to allow modification bean properties to serialize:
        if (_factoryConfig.hasSerializerModifiers()) {
            for (BeanSerializerModifier mod : _factoryConfig.serializerModifiers()) {
                props = mod.changeProperties(config, beanDesc, props);
            }
        }

        // Any properties to suppress?
        props = filterBeanProperties(config, beanDesc, props);

        // [JACKSON-440] Need to allow reordering of properties to serialize
        if (_factoryConfig.hasSerializerModifiers()) {
            for (BeanSerializerModifier mod : _factoryConfig.serializerModifiers()) {
                props = mod.orderProperties(config, beanDesc, props);
            }
        }

        /* And if Object Id is needed, some preparation for that as well: better
         * do before view handling, mostly for the custom id case which needs
         * access to a property
         */
        builder.setObjectIdWriter(constructObjectIdHandler(prov, beanDesc, props));
        
        builder.setProperties(props);
        builder.setFilterId(findFilterId(config, beanDesc));
        
        AnnotatedMember anyGetter = beanDesc.findAnyGetter();
        if (anyGetter != null) {
            if (config.canOverrideAccessModifiers()) {
                anyGetter.fixAccess();
            }
            JavaType type = anyGetter.getType(beanDesc.bindingsForBeanType());
            // copied from BasicSerializerFactory.buildMapSerializer():
            boolean staticTyping = config.isEnabled(MapperFeature.USE_STATIC_TYPING);
            JavaType valueType = type.getContentType();
            TypeSerializer typeSer = createTypeSerializer(config, valueType);
            // last 2 nulls; don't know key, value serializers (yet)
            // 23-Feb-2015, tatu: As per [#705], need to support custom serializers
            MapSerializer anySer = MapSerializer.construct(null, type, staticTyping, typeSer, null, null, null);
                // TODO: support '@JsonIgnoreProperties' with any setter?
            // TODO: can we find full PropertyName?
            PropertyName name = new PropertyName(anyGetter.getName());
            BeanProperty.Std anyProp = new BeanProperty.Std(name, valueType, null,
                    beanDesc.getClassAnnotations(), anyGetter, PropertyMetadata.STD_OPTIONAL);
            builder.setAnyGetter(new AnyGetterWriter(anyProp, anyGetter, anySer));
        }
        // Next: need to gather view information, if any:
        processViews(config, builder);

        // Finally: let interested parties mess with the result bit more...
        if (_factoryConfig.hasSerializerModifiers()) {
            for (BeanSerializerModifier mod : _factoryConfig.serializerModifiers()) {
                builder = mod.updateBuilder(config, beanDesc, builder);
            }
        }
        
        JsonSerializer<Object> ser = (JsonSerializer<Object>) builder.build();
        
        if (ser == null) {
            // If we get this far, there were no properties found, so no regular BeanSerializer
            // would be constructed. But, couple of exceptions.
            // First: if there are known annotations, just create 'empty bean' serializer
            if (beanDesc.hasKnownClassAnnotations()) {
                return builder.createDummy();
            }
        }
        return ser;
    }

// relevant test
// com.fasterxml.jackson.databind.node.TestTreeWithType::testIssue353
    public void testIssue353() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();

        mapper.enableDefaultTypingAsProperty(ObjectMapper.DefaultTyping.NON_FINAL, "@class");

         SimpleModule testModule = new SimpleModule("MyModule", new Version(1, 0, 0, null, "TEST", "TEST"));
         testModule.addDeserializer(SavedCookie.class, new SavedCookieDeserializer());
         mapper.registerModule(testModule);

         SavedCookie savedCookie = new SavedCookie("key", "v");
         String json = mapper.writeValueAsString(savedCookie);
         SavedCookie out = mapper.reader(SavedCookie.class).readValue(json);

         assertEquals("key", out.name);
         assertEquals("v", out.value);
    }

// com.fasterxml.jackson.databind.seq.ObjectReaderTest::testParserFeatures
    public void testParserFeatures() throws Exception
    {
        final String JSON = "[  7 ]";
        
        ObjectReader reader = MAPPER.reader(int[].class)
                .with(JsonParser.Feature.ALLOW_COMMENTS);

        int[] value = reader.readValue(JSON);
        assertNotNull(value);
        assertEquals(1, value.length);
        assertEquals(7, value[0]);

        
        try {
            reader.without(JsonParser.Feature.ALLOW_COMMENTS).readValue(JSON);
            fail("Should not have passed");
        } catch (JsonProcessingException e) {
            verifyException(e, "foo");
        }
    }

// com.fasterxml.jackson.databind.seq.ObjectWriterTest::testPrettyPrinter
    public void testPrettyPrinter() throws Exception
    {
        ObjectWriter writer = MAPPER.writer();
        HashMap<String, Integer> data = new HashMap<String,Integer>();
        data.put("a", 1);
        
        
        assertEquals("{\"a\":1}", writer.writeValueAsString(data));

        
        writer = writer.withDefaultPrettyPrinter();

        
        String lf = System.getProperty("line.separator");
        assertEquals("{" + lf + "  \"a\" : 1" + lf + "}", writer.writeValueAsString(data));

        
        writer = writer.with((PrettyPrinter) null);
        assertEquals("{\"a\":1}", writer.writeValueAsString(data));
    }

// com.fasterxml.jackson.databind.seq.ObjectWriterTest::testPrefetch
    public void testPrefetch() throws Exception
    {
        ObjectWriter writer = MAPPER.writer();
        assertFalse(writer.hasPrefetchedSerializer());
        writer = writer.forType(String.class);
        assertTrue(writer.hasPrefetchedSerializer());
    }

// com.fasterxml.jackson.databind.seq.ObjectWriterTest::testObjectWriterFeatures
    public void testObjectWriterFeatures() throws Exception
    {
        ObjectWriter writer = MAPPER.writer()
                .without(JsonGenerator.Feature.QUOTE_FIELD_NAMES);                
        Map<String,Integer> map = new HashMap<String,Integer>();
        map.put("a", 1);
        assertEquals("{a:1}", writer.writeValueAsString(map));
        
        assertEquals("{\"a\":1}", writer.with(JsonGenerator.Feature.QUOTE_FIELD_NAMES)
                .writeValueAsString(map));
    }

// com.fasterxml.jackson.databind.seq.ObjectWriterTest::testObjectWriterWithNode
    public void testObjectWriterWithNode() throws Exception
    {
        ObjectNode stuff = MAPPER.createObjectNode();
        stuff.put("a", 5);
        ObjectWriter writer = MAPPER.writerFor(JsonNode.class);
        String json = writer.writeValueAsString(stuff);
        assertEquals("{\"a\":5}", json);
    }

// com.fasterxml.jackson.databind.seq.ObjectWriterTest::testPolymorpicWithTyping
    public void testPolymorpicWithTyping() throws Exception
    {
        ObjectWriter writer = MAPPER.writerFor(PolyBase.class);
        String json;

        json = writer.writeValueAsString(new ImplA(3));
        assertEquals(aposToQuotes("{'type':'A','value':3}"), json);
        json = writer.writeValueAsString(new ImplB(-5));
        assertEquals(aposToQuotes("{'type':'B','b':-5}"), json);
    }

// com.fasterxml.jackson.databind.seq.ReadValuesTest::testRootBeans
    public void testRootBeans() throws Exception
    {
        final String JSON = "{\"a\":3}{\"a\":27}  ";

        MappingIterator<Bean> it = MAPPER.reader(Bean.class).readValues(JSON);

        assertNotNull(it.getCurrentLocation());
        assertTrue(it.hasNext());
        Bean b = it.next();
        assertEquals(3, b.a);
        assertTrue(it.hasNext());
        b = it.next();
        assertEquals(27, b.a);
        assertFalse(it.hasNext());
        it.close();

        
        it = MAPPER.reader(Bean.class).readValues(JSON);
        List<Bean> all = it.readAll();
        assertEquals(2, all.size());
        it.close();

        it = MAPPER.reader(Bean.class).readValues("{\"a\":3}{\"a\":3}");
        Set<Bean> set = it.readAll(new HashSet<Bean>());
        assertEquals(HashSet.class, set.getClass());
        assertEquals(1, set.size());
        assertEquals(3, set.iterator().next().a);
    }

// com.fasterxml.jackson.databind.seq.ReadValuesTest::testRootBeansInArray
    public void testRootBeansInArray() throws Exception
    {
        final String JSON = "[{\"a\":6}, {\"a\":-7}]";

        MappingIterator<Bean> it = MAPPER.reader(Bean.class).readValues(JSON);

        assertNotNull(it.getCurrentLocation());
        assertTrue(it.hasNext());
        Bean b = it.next();
        assertEquals(6, b.a);
        assertTrue(it.hasNext());
        b = it.next();
        assertEquals(-7, b.a);
        assertFalse(it.hasNext());
        it.close();

        
        it = MAPPER.reader(Bean.class).readValues(JSON);
        List<Bean> all = it.readAll();
        assertEquals(2, all.size());
        it.close();

        it = MAPPER.reader(Bean.class).readValues("[{\"a\":4},{\"a\":4}]");
        Set<Bean> set = it.readAll(new HashSet<Bean>());
        assertEquals(HashSet.class, set.getClass());
        assertEquals(1, set.size());
        assertEquals(4, set.iterator().next().a);
    }

// com.fasterxml.jackson.databind.seq.ReadValuesTest::testRootMaps
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

// com.fasterxml.jackson.databind.seq.ReadValuesTest::testRootBeansWithParser
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

// com.fasterxml.jackson.databind.seq.ReadValuesTest::testRootArraysWithParser
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

// com.fasterxml.jackson.databind.seq.ReadValuesTest::testHasNextWithEndArray
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

// com.fasterxml.jackson.databind.seq.ReadValuesTest::testHasNextWithEndArrayManagedParser
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

// com.fasterxml.jackson.databind.seq.ReadValuesTest::testNonRootBeans
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

// com.fasterxml.jackson.databind.seq.ReadValuesTest::testNonRootMapsWithParser
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

// com.fasterxml.jackson.databind.seq.ReadValuesTest::testNonRootMapsWithObjectReader
    public void testNonRootMapsWithObjectReader() throws Exception
    {
        String JSON = "[{ \"hi\": \"ho\", \"neighbor\": \"Joe\" },\n"
            +"{\"boy\": \"howdy\", \"huh\": \"what\"}]";
        final MappingIterator<Map<String, Object>> iterator = MAPPER
                .reader()
                .forType(new TypeReference<Map<String, Object>>(){})
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

// com.fasterxml.jackson.databind.seq.ReadValuesTest::testNonRootArraysUsingParser
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

// com.fasterxml.jackson.databind.seq.SequenceWriterTest::testSimpleNonArray
    public void testSimpleNonArray() throws Exception
    {
        StringWriter strw = new StringWriter();
        SequenceWriter w = WRITER
                .writeValues(strw);
        w.write(new Bean(13))
        .write(new Bean(-6))
        .writeAll(new Bean[] { new Bean(3), new Bean(1) });
        w.close();
        assertEquals(aposToQuotes("{'a':13}\n{'a':-6}\n{'a':3}\n{'a':1}"),
                strw.toString());
    }

// com.fasterxml.jackson.databind.seq.SequenceWriterTest::testSimpleArray
    public void testSimpleArray() throws Exception
    {
        StringWriter strw = new StringWriter();
        SequenceWriter w = WRITER.writeValuesAsArray(strw);
        w.write(new Bean(1))
        .write(new Bean(2))
        .writeAll(new Bean[] { new Bean(-7), new Bean(2) });
        w.close();
        assertEquals(aposToQuotes("[{'a':1},{'a':2},{'a':-7},{'a':2}]"),
                strw.toString());
    }

// com.fasterxml.jackson.databind.seq.SequenceWriterTest::testPolymorphicNonArrayWithoutType
    public void testPolymorphicNonArrayWithoutType() throws Exception
    {
        StringWriter strw = new StringWriter();
        SequenceWriter w = WRITER
                .writeValues(strw);
        w.write(new ImplA(3))
            .write(new ImplA(4))
            .close();
        assertEquals(aposToQuotes("{'type':'A','value':3}\n{'type':'A','value':4}"),
                strw.toString());
    }

// com.fasterxml.jackson.databind.seq.SequenceWriterTest::testPolymorphicArrayWithoutType
    public void testPolymorphicArrayWithoutType() throws Exception
    {
        StringWriter strw = new StringWriter();
        SequenceWriter w = WRITER
                .writeValuesAsArray(strw);
        w.write(new ImplA(-1))
            .write(new ImplA(6))
            .close();
        assertEquals(aposToQuotes("[{'type':'A','value':-1},{'type':'A','value':6}]"),
                strw.toString());
    }

// com.fasterxml.jackson.databind.seq.SequenceWriterTest::testPolymorphicArrayWithType
    public void testPolymorphicArrayWithType() throws Exception
    {
        StringWriter strw = new StringWriter();
        SequenceWriter w = WRITER
                .forType(PolyBase.class)
                .writeValuesAsArray(strw);
        w.write(new ImplA(-1))
            .write(new ImplB(3))
            .write(new ImplA(7))
            .close();
        assertEquals(aposToQuotes("[{'type':'A','value':-1},{'type':'B','b':3},{'type':'A','value':7}]"),
                strw.toString());
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

// com.fasterxml.jackson.databind.ser.TestAnyGetter::testSimpleJsonValue
    public void testSimpleJsonValue() throws Exception
    {
        String json = MAPPER.writeValueAsString(new Bean());
        Map<?,?> map = MAPPER.readValue(json, Map.class);
        assertEquals(2, map.size());
        assertEquals(Integer.valueOf(3), map.get("x"));
        assertEquals(Boolean.TRUE, map.get("a"));
    }

// com.fasterxml.jackson.databind.ser.TestAnyGetter::testAnyOnly
    public void testAnyOnly() throws Exception
    {
        ObjectMapper m;

        
        m = new ObjectMapper();
        m.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, true);
        String json = serializeAsString(m, new AnyOnlyBean());
        assertEquals("{\"a\":3}", json);

        
        m = new ObjectMapper();
        m.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        json = serializeAsString(m, new AnyOnlyBean());
        assertEquals("{\"a\":3}", json);
    }

// com.fasterxml.jackson.databind.ser.TestAnyGetter::testAnyWithNull
    public void testAnyWithNull() throws Exception
    {
        MapAsAny input = new MapAsAny();
        input.add("bar", null);
        assertEquals(aposToQuotes("{'bar':null}"),
                MAPPER.writeValueAsString(input));
    }

// com.fasterxml.jackson.databind.ser.TestAnyGetter::testIssue705
    public void testIssue705() throws Exception
    {
        Issue705Bean input = new Issue705Bean("key", "value");        
        String json = MAPPER.writeValueAsString(input);
        assertEquals("{\"stuff\":\"[key/value]\"}", json);
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
        m.setVisibilityChecker(vc);
        
        Map<String,Object> result = writeAndMap(m, new FieldBean());
        assertEquals(3, result.size());
        assertEquals("public", result.get("p1"));
        assertEquals("protected", result.get("p2"));
        assertEquals("private", result.get("p3"));

        m = new ObjectMapper();
        vc = m.getVisibilityChecker();
        vc = vc.withGetterVisibility(JsonAutoDetect.Visibility.ANY);
        m.setVisibilityChecker(vc);
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
        m.setVisibilityChecker(vc);

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
    public void testCollections()
        throws IOException
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
    public void testBigCollection()
        throws IOException
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
    public void testEnumMap()
        throws IOException
    {
        EnumMap<Key,String> map = new EnumMap<Key,String>(Key.class);
        map.put(Key.B, "xyz");
        map.put(Key.C, "abc");
        
        String json = MAPPER.writeValueAsString(map);
        assertEquals("{\"B\":\"xyz\",\"C\":\"abc\"}",json.trim());
    }

// com.fasterxml.jackson.databind.ser.TestCollectionSerialization::testIterator
    public void testIterator()
        throws IOException
    {
        StringWriter sw = new StringWriter();
        ArrayList<Integer> l = new ArrayList<Integer>();
        l.add(1);
        l.add(-9);
        l.add(0);
        MAPPER.writeValue(sw, l.iterator());
        assertEquals("[1,-9,0]", sw.toString().trim());
    }

// com.fasterxml.jackson.databind.ser.TestCollectionSerialization::testIterable
    public void testIterable()
        throws IOException
    {
        StringWriter sw = new StringWriter();
        MAPPER.writeValue(sw, new IterableWrapper(new int[] { 1, 2, 3 }));
        assertEquals("[1,2,3]", sw.toString().trim());
    }

// com.fasterxml.jackson.databind.ser.TestCollectionSerialization::testEmptyBeanCollection
    public void testEmptyBeanCollection()
        throws IOException
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
    public void testEmptyBeanEnumMap()
        throws IOException
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
    public void testNullBeanEnumMap()
        throws IOException
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

// com.fasterxml.jackson.databind.ser.TestCollectionSerialization::testWithIterable
    public void testWithIterable() throws IOException
    {
        
        assertEquals("{\"values\":[\"value\"]}",
                MAPPER.writeValueAsString(new BeanWithIterable()));
        
        assertEquals("[1,2,3]",
                MAPPER.writeValueAsString(new IntIterable()));
    }

// com.fasterxml.jackson.databind.ser.TestCollectionSerialization::testIterable358
    public void testIterable358() throws Exception {
        String json = MAPPER.writeValueAsString(new B());
        assertEquals("{\"list\":[[\"Hello world.\"]]}", json);
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
        JsonSerializer<?> ser = new CollectionSerializer(null, false, null, null, null);
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

// com.fasterxml.jackson.databind.ser.TestDateSerialization::testDateNumeric
    public void testDateNumeric() throws IOException
    {
        
        assertTrue(MAPPER.isEnabled(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS));
        
        String json = MAPPER.writeValueAsString(new Date(199L));
        assertEquals("199", json);
    }

// com.fasterxml.jackson.databind.ser.TestDateSerialization::testDateISO8601
    public void testDateISO8601() throws IOException
    {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        
        String json = mapper.writeValueAsString(new Date(0L));
        assertEquals("\"1970-01-01T00:00:00.000+0000\"", json);
    }

// com.fasterxml.jackson.databind.ser.TestDateSerialization::testDateOther
    public void testDateOther() throws IOException
    {
        ObjectMapper mapper = new ObjectMapper();
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd'X'HH:mm:ss");
        df.setTimeZone(TimeZone.getTimeZone("PST"));
        mapper.setDateFormat(df);
        
        assertEquals(quote("1969-12-31X16:00:00"), mapper.writeValueAsString(new Date(0L)));
    }

// com.fasterxml.jackson.databind.ser.TestDateSerialization::testSqlDate
    public void testSqlDate() throws IOException
    {
        
        java.sql.Date date = new java.sql.Date(99, Calendar.APRIL, 1);
        assertEquals(quote("1999-04-01"), MAPPER.writeValueAsString(date));

        java.sql.Date date0 = new java.sql.Date(0L);
        assertEquals(aposToQuotes("{'date':'"+date0.toString()+"'}"),
                MAPPER.writeValueAsString(new SqlDateAsDefaultBean(0L)));

        
        assertEquals(aposToQuotes("{'date':0}"), MAPPER.writeValueAsString(new SqlDateAsNumberBean(0L)));
    }

// com.fasterxml.jackson.databind.ser.TestDateSerialization::testTimeZone
    public void testTimeZone() throws IOException
    {
        TimeZone input = TimeZone.getTimeZone("PST");
        String json = MAPPER.writeValueAsString(input);
        assertEquals(quote("PST"), json);
    }

// com.fasterxml.jackson.databind.ser.TestDateSerialization::testTimeZoneInBean
    public void testTimeZoneInBean() throws IOException
    {
        String json = MAPPER.writeValueAsString(new TimeZoneBean("PST"));
        assertEquals("{\"tz\":\"PST\"}", json);
    }

// com.fasterxml.jackson.databind.ser.TestDateSerialization::testDateUsingObjectWriter
    public void testDateUsingObjectWriter() throws IOException
    {
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd'X'HH:mm:ss");
        df.setTimeZone(TimeZone.getTimeZone("PST"));
        assertEquals(quote("1969-12-31X16:00:00"),
                MAPPER.writer(df).writeValueAsString(new Date(0L)));
        ObjectWriter w = MAPPER.writer((DateFormat)null);
        assertEquals("0", w.writeValueAsString(new Date(0L)));

        w = w.with(df);
        assertEquals(quote("1969-12-31X16:00:00"), w.writeValueAsString(new Date(0L)));
        w = w.with((DateFormat) null);
        assertEquals("0", w.writeValueAsString(new Date(0L)));
    }

// com.fasterxml.jackson.databind.ser.TestDateSerialization::testDatesAsMapKeys
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

// com.fasterxml.jackson.databind.ser.TestDateSerialization::testDateWithJsonFormat
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
    }

// com.fasterxml.jackson.databind.ser.TestDateSerialization::testWithTimeZoneOverride
    public void testWithTimeZoneOverride() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd/HH:mm z"));
        mapper.setTimeZone(TimeZone.getTimeZone("PST"));
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        String json = mapper.writeValueAsString(new Date(0));
        
        assertEquals(quote("1969-12-31/16:00 PST"), json);

        
        mapper.setLocale(Locale.FRANCE);
        json = mapper.writeValueAsString(new Date(0));
        assertEquals(quote("1969-12-31/16:00 PST"), json);
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
        assertEquals("\"B\"", mapper.writeValueAsString(TestEnum.B));
    }

// com.fasterxml.jackson.databind.ser.TestEnumSerialization::testEnumSet
    public void testEnumSet() throws Exception
    {
        StringWriter sw = new StringWriter();
        EnumSet<TestEnum> value = EnumSet.of(TestEnum.B);
        mapper.writeValue(sw, value);
        assertEquals("[\"B\"]", sw.toString());
    }

// com.fasterxml.jackson.databind.ser.TestEnumSerialization::testEnumUsingToString
    public void testEnumUsingToString() throws Exception
    {
        StringWriter sw = new StringWriter();
        mapper.writeValue(sw, AnnotatedTestEnum.C2);
        assertEquals("\"c2\"", sw.toString());
    }

// com.fasterxml.jackson.databind.ser.TestEnumSerialization::testSubclassedEnums
    public void testSubclassedEnums() throws Exception
    {
        assertEquals("\"B\"", mapper.writeValueAsString(EnumWithSubClass.B));
    }

// com.fasterxml.jackson.databind.ser.TestEnumSerialization::testEnumsWithJsonValue
    public void testEnumsWithJsonValue() throws Exception
    {
        assertEquals("\"bar\"", mapper.writeValueAsString(EnumWithJsonValue.B));
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
        assertEquals("{\""+EnumWithJsonValue.B.toString()+"\":\"x\"}", mapper.writeValueAsString(input));
    }

// com.fasterxml.jackson.databind.ser.TestEnumSerialization::testSerializableEnum
    public void testSerializableEnum() throws Exception
    {
        assertEquals("\"foo\"", mapper.writeValueAsString(SerializableEnum.A));
    }

// com.fasterxml.jackson.databind.ser.TestEnumSerialization::testToStringEnum
    public void testToStringEnum() throws Exception
    {
        ObjectMapper m = new ObjectMapper();
        m.configure(SerializationFeature.WRITE_ENUMS_USING_TO_STRING, true);
        assertEquals("\"b\"", m.writeValueAsString(LowerCaseEnum.B));
    }

// com.fasterxml.jackson.databind.ser.TestEnumSerialization::testToStringEnumWithEnumMap
    public void testToStringEnumWithEnumMap() throws Exception
    {
        ObjectMapper m = new ObjectMapper();
        m.configure(SerializationFeature.WRITE_ENUMS_USING_TO_STRING, true);
        EnumMap<LowerCaseEnum,String> enums = new EnumMap<LowerCaseEnum,String>(LowerCaseEnum.class);
        enums.put(LowerCaseEnum.C, "value");
        assertEquals("{\"c\":\"value\"}", m.writeValueAsString(enums));
    }

// com.fasterxml.jackson.databind.ser.TestEnumSerialization::testMapWithEnumKeys
    public void testMapWithEnumKeys() throws Exception
    {
        MapBean bean = new MapBean();
        bean.add(TestEnum.B, 3);
        String json = mapper.writeValueAsString(bean);
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
        assertEquals(quote("V1"), mapper.writeValueAsString(OK.V1));
        assertEquals(quote("V1"), mapper.writeValueAsString(NOT_OK.V1));
        assertEquals(quote("V2"), mapper.writeValueAsString(NOT_OK2.V2));
    }

// com.fasterxml.jackson.databind.ser.TestEnumSerialization::testEnumAsObjectValid
    public void testEnumAsObjectValid() throws Exception {
        assertEquals("{\"value\":\"a1\"}", mapper.writeValueAsString(PoNUM.A));
    }

// com.fasterxml.jackson.databind.ser.TestEnumSerialization::testEnumAsIndexViaAnnotations
    public void testEnumAsIndexViaAnnotations() throws Exception {
        assertEquals("{\"text\":0}", mapper.writeValueAsString(new PoNUMContainer()));
    }

// com.fasterxml.jackson.databind.ser.TestEnumSerialization::testEnumAsObjectBroken
    public void testEnumAsObjectBroken() throws Exception
    {
        assertEquals("0", mapper.writeValueAsString(PoAsArray.A));
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
        assertEquals("{\"value\":\"B\"}", mapper.writeValueAsString(new PoOverrideAsString()));
    }

// com.fasterxml.jackson.databind.ser.TestEnumSerialization::testOverrideEnumAsNumber
    public void testOverrideEnumAsNumber() throws Exception {
        assertEquals("{\"value\":1}", mapper.writeValueAsString(new PoOverrideAsNumber()));
    }

// com.fasterxml.jackson.databind.ser.TestEnumSerialization::testJsonValueForEnumMapKey
    public void testJsonValueForEnumMapKey() throws Exception {
        assertEquals(aposToQuotes("{'stuff':{'longValue':'foo'}}"),
                mapper.writeValueAsString(new MyStuff594("foo")));
    }

// com.fasterxml.jackson.databind.ser.TestEnumSerialization::testCustomEnumMapKeySerializer
    public void testCustomEnumMapKeySerializer() throws Exception {
        String json = mapper.writeValueAsString(new MyBean661("abc"));
        assertEquals(aposToQuotes("{'X-FOO':'abc'}"), json);
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
        ObjectMapper mapper = new ObjectMapper();
        String TEST = "test exception";
        Map<String,Object> result = writeAndMap(mapper, new Exception(TEST));
        
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

// com.fasterxml.jackson.databind.ser.TestFeatures::testGlobalAutoDetection
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

// com.fasterxml.jackson.databind.ser.TestFeatures::testPerClassAutoDetection
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

// com.fasterxml.jackson.databind.ser.TestFeatures::testPerClassAutoDetectionForIsGetter
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

// com.fasterxml.jackson.databind.ser.TestFeatures::testConfigChainability
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

// com.fasterxml.jackson.databind.ser.TestFeatures::testCloseCloseable
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

// com.fasterxml.jackson.databind.ser.TestFeatures::testCharArrays
    public void testCharArrays() throws IOException
    {
        char[] chars = new char[] { 'a','b','c' };
        ObjectMapper m = new ObjectMapper();
        
        assertEquals(quote("abc"), m.writeValueAsString(chars));
        
        
        m.configure(SerializationFeature.WRITE_CHAR_ARRAYS_AS_JSON_ARRAYS, true);
        assertEquals("[\"a\",\"b\",\"c\"]", m.writeValueAsString(chars));
    }

// com.fasterxml.jackson.databind.ser.TestFeatures::testFlushingAutomatic
    public void testFlushingAutomatic() throws IOException
    {
        ObjectMapper mapper = new ObjectMapper();
        assertTrue(mapper.getSerializationConfig().isEnabled(SerializationFeature.FLUSH_AFTER_WRITE_VALUE));
        
        StringWriter sw = new StringWriter();
        JsonGenerator jgen = mapper.getFactory().createGenerator(sw);
        mapper.writeValue(jgen, Integer.valueOf(13));
        assertEquals("13", sw.toString());
        jgen.close();

        
        sw = new StringWriter();
        jgen = mapper.getFactory().createGenerator(sw);
        ObjectWriter ow = mapper.writer();
        ow.writeValue(jgen, Integer.valueOf(99));
        assertEquals("99", sw.toString());
        jgen.close();
    }

// com.fasterxml.jackson.databind.ser.TestFeatures::testFlushingNotAutomatic
    public void testFlushingNotAutomatic() throws IOException
    {
        
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(SerializationFeature.FLUSH_AFTER_WRITE_VALUE, false);
        StringWriter sw = new StringWriter();
        JsonGenerator jgen = mapper.getFactory().createGenerator(sw);

        mapper.writeValue(jgen, Integer.valueOf(13));
        
        assertEquals("", sw.toString());
        
        jgen.flush();
        assertEquals("13", sw.toString());
        jgen.close();
        
        sw = new StringWriter();
        jgen = mapper.getFactory().createGenerator(sw);
        ObjectWriter ow = mapper.writer();
        ow.writeValue(jgen, Integer.valueOf(99));
        assertEquals("", sw.toString());
        
        jgen.flush();
        assertEquals("99", sw.toString());
        jgen.close();
    }

// com.fasterxml.jackson.databind.ser.TestFeatures::testSingleElementCollections
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

// com.fasterxml.jackson.databind.ser.TestFieldSerialization::testSimpleAutoDetect
    public void testSimpleAutoDetect() throws Exception
    {
        SimpleFieldBean bean = new SimpleFieldBean();
        
        bean.x = 13;
        Map<String,Object> result = writeAndMap(MAPPER, bean);
        assertEquals(2, result.size());
        assertEquals(Integer.valueOf(13), result.get("x"));
        assertEquals(Integer.valueOf(0), result.get("y"));
    }

// com.fasterxml.jackson.databind.ser.TestFieldSerialization::testSimpleAnnotation
	public void testSimpleAnnotation() throws Exception
    {
        SimpleFieldBean2 bean = new SimpleFieldBean2();
        bean.values = new String[] { "a", "b" };
        Map<String,Object> result = writeAndMap(MAPPER, bean);
        assertEquals(1, result.size());
        List<String> values = (List<String>) result.get("values");
        assertEquals(2, values.size());
        assertEquals("a", values.get(0));
        assertEquals("b", values.get(1));
    }

// com.fasterxml.jackson.databind.ser.TestFieldSerialization::testTransientAndStatic
    public void testTransientAndStatic() throws Exception
    {
        TransientBean bean = new TransientBean();
        Map<String,Object> result = writeAndMap(MAPPER, bean);
        assertEquals(1, result.size());
        assertEquals(Integer.valueOf(0), result.get("a"));
    }

// com.fasterxml.jackson.databind.ser.TestFieldSerialization::testNoAutoDetect
    public void testNoAutoDetect() throws Exception
    {
        NoAutoDetectBean bean = new NoAutoDetectBean();
        bean._z = -4;
        Map<String,Object> result = writeAndMap(MAPPER, bean);
        assertEquals(1, result.size());
        assertEquals(Integer.valueOf(-4), result.get("z"));
    }

// com.fasterxml.jackson.databind.ser.TestFieldSerialization::testMethodPrecedence
    public void testMethodPrecedence() throws Exception
    {
        FieldAndMethodBean bean = new FieldAndMethodBean();
        bean.z = 9;
        assertEquals(10, bean.getZ());
        assertEquals("{\"z\":10}", MAPPER.writeValueAsString(bean));
    }

// com.fasterxml.jackson.databind.ser.TestFieldSerialization::testOkDupFields
    public void testOkDupFields() throws Exception
    {
        OkDupFieldBean bean = new OkDupFieldBean(1, 2);
        Map<String,Object> json = writeAndMap(MAPPER, bean);
        assertEquals(2, json.size());
        assertEquals(Integer.valueOf(1), json.get("x"));
        assertEquals(Integer.valueOf(2), json.get("y"));
    }

// com.fasterxml.jackson.databind.ser.TestFieldSerialization::testIssue240
    public void testIssue240() throws Exception
    {
        Item240 bean = new Item240("a12", null);
        assertEquals(MAPPER.writeValueAsString(bean), "{\"id\":\"a12\"}");
    }

// com.fasterxml.jackson.databind.ser.TestFieldSerialization::testFailureDueToDups
    public void testFailureDueToDups() throws Exception
    {
        try {
            writeAndMap(MAPPER, new DupFieldBean());
        } catch (JsonMappingException e) {
            verifyException(e, "Multiple fields representing");
        }
    }

// com.fasterxml.jackson.databind.ser.TestFieldSerialization::testFailureDueToDupField
    public void testFailureDueToDupField() throws Exception
    {
        try {
            writeAndMap(MAPPER, new DupFieldBean2());
        } catch (JsonMappingException e) {
            verifyException(e, "Multiple fields representing");
        }
    }

// com.fasterxml.jackson.databind.ser.TestGenericTypes::testIssue468a
    public void testIssue468a() throws Exception
    {
        Person1 p1 = new Person1("John");
        p1.setAccount(new Key<Account>(new Account("something", 42L)));
        
        
        ObjectMapper mapper = new ObjectMapper();
        String json = mapper.writeValueAsString(p1);

        
        Map<String,Object> map = mapper.readValue(json, Map.class);
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

        
        ObjectMapper mapper = new ObjectMapper();               
        String json = mapper.writeValueAsString(p2);

        
        Map<String,Object> map = mapper.readValue(json, Map.class);
        assertEquals("John", map.get("name"));
        Object ob = map.get("accounts");
        assertNotNull(ob);
        List<?> acctList = (List<?>) ob;
        assertEquals(3, acctList.size());
        
    }

// com.fasterxml.jackson.databind.ser.TestGenericTypes::testUnboundIssue572
    public void testUnboundIssue572() throws Exception
    {
        GenericBogusWrapper<Integer> list = new GenericBogusWrapper<Integer>(Integer.valueOf(7));
        String json = new ObjectMapper().writeValueAsString(list);
        assertEquals("{\"wrapped\":{\"value\":7}}", json);
    }

// com.fasterxml.jackson.databind.ser.TestJSONP::testSimpleScalars
    public void testSimpleScalars() throws Exception
    {
        ObjectMapper m = new ObjectMapper();
        assertEquals("callback(\"abc\")",
                serializeAsString(m, new JSONPObject("callback", "abc")));
        assertEquals("calc(123)",
                serializeAsString(m, new JSONPObject("calc", Integer.valueOf(123))));
        assertEquals("dummy(null)",
                serializeAsString(m, new JSONPObject("dummy", null)));
    }

// com.fasterxml.jackson.databind.ser.TestJSONP::testSimpleBean
    public void testSimpleBean() throws Exception
    {
        ObjectMapper m = new ObjectMapper();
        assertEquals("xxx({\"a\":\"123\",\"b\":\"456\"})",
                serializeAsString(m, new JSONPObject("xxx",
                        new Impl("123", "456"))));
    }

// com.fasterxml.jackson.databind.ser.TestJSONP::testWithType
    public void testWithType() throws Exception
    {
        ObjectMapper m = new ObjectMapper();
        Object ob = new Impl("abc", "def");
        JavaType type = TypeFactory.defaultInstance().uncheckedSimpleType(Base.class);
        assertEquals("do({\"a\":\"abc\"})",
                serializeAsString(m, new JSONPObject("do", ob, type)));
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

// com.fasterxml.jackson.databind.ser.TestJdkTypes::testBigDecimal
    public void testBigDecimal()
        throws Exception
    {
        Map<String, Object> map = new HashMap<String, Object>();
        String PI_STR = "3.14159265";
        map.put("pi", new BigDecimal(PI_STR));
        String str = MAPPER.writeValueAsString(map);
        assertEquals("{\"pi\":3.14159265}", str);
    }

// com.fasterxml.jackson.databind.ser.TestJdkTypes::testBigDecimalAsPlainString
    public void testBigDecimalAsPlainString()
        throws Exception
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

// com.fasterxml.jackson.databind.ser.TestJsonRawValue::testSimpleStringGetter
    public void testSimpleStringGetter() throws Exception
    {
        ObjectMapper m = new ObjectMapper();
        String value = "abc";
        String result = m.writeValueAsString(new ClassGetter<String>(value));
        String expected = String.format("{\"nonRaw\":\"%s\",\"raw\":%s,\"value\":%s}", value, value, value);
        assertEquals(expected, result);
    }

// com.fasterxml.jackson.databind.ser.TestJsonRawValue::testSimpleNonStringGetter
    public void testSimpleNonStringGetter() throws Exception
    {
        ObjectMapper m = new ObjectMapper();
        int value = 123;
        String result = m.writeValueAsString(new ClassGetter<Integer>(value));
        String expected = String.format("{\"nonRaw\":%d,\"raw\":%d,\"value\":%d}", value, value, value);
        assertEquals(expected, result);
    }

// com.fasterxml.jackson.databind.ser.TestJsonRawValue::testNullStringGetter
    public void testNullStringGetter() throws Exception
    {
        ObjectMapper m = new ObjectMapper();
        String result = m.writeValueAsString(new ClassGetter<String>(null));
        String expected = "{\"nonRaw\":null,\"raw\":null,\"value\":null}";
        assertEquals(expected, result);
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
            verifyException(e, "not a super-type of");
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

// com.fasterxml.jackson.databind.ser.TestJsonSerialize2::testEmptyInclusion
    public void testEmptyInclusion() throws IOException
    {
        ObjectMapper defMapper = MAPPER;
        ObjectMapper inclMapper = new ObjectMapper().setSerializationInclusion(JsonInclude.Include.NON_EMPTY);

        StringWrapper str = new StringWrapper("");
        assertEquals("{\"str\":\"\"}", defMapper.writeValueAsString(str));
        assertEquals("{}", inclMapper.writeValueAsString(str));
        assertEquals("{}", inclMapper.writeValueAsString(new StringWrapper()));

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
    public void testSerializeAsInClass() throws IOException
    {
        assertEquals("{\"foo\":42}", WRITER.writeValueAsString(new FooImpl()));
    }

// com.fasterxml.jackson.databind.ser.TestJsonSerializeAs::testSerializeAsForArrayProp
    public void testSerializeAsForArrayProp() throws IOException
    {
        assertEquals("{\"foos\":[{\"foo\":42}]}", WRITER.writeValueAsString(new Fooables()));
    }

// com.fasterxml.jackson.databind.ser.TestJsonSerializeAs::testSerializeAsForSimpleProp
    public void testSerializeAsForSimpleProp() throws IOException
    {
        assertEquals("{\"foo\":{\"foo\":42}}", WRITER.writeValueAsString(new FooableWrapper()));
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

// com.fasterxml.jackson.databind.ser.TestKeySerializers::testNotKarl
    public void testNotKarl() throws IOException {
        final ObjectMapper mapper = new ObjectMapper();
        final String serialized = mapper.writeValueAsString(new NotKarlBean());
        assertEquals("{\"map\":{\"Not Karl\":1}}", serialized);
    }

// com.fasterxml.jackson.databind.ser.TestKeySerializers::testKarl
    public void testKarl() throws IOException {
        final ObjectMapper mapper = new ObjectMapper();
        final String serialized = mapper.writeValueAsString(new KarlBean());
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
        mod.addKeySerializer(ABC.class, new ABCSerializer());
        mapper.registerModule(mod);

        String json = mapper.writeValueAsString(new ABCMapWrapper());
        assertEquals("{\"stuff\":{\"xxxB\":\"bar\"}}", json);
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

// com.fasterxml.jackson.databind.ser.TestMapSerialization::testMapNullSerialization
    public void testMapNullSerialization() throws IOException
    {
        ObjectMapper m = new ObjectMapper();
        Map<String,String> map = new HashMap<String,String>();
        map.put("a", null);
        
        assertEquals("{\"a\":null}", m.writeValueAsString(map));
        
        m.configure(SerializationFeature.WRITE_NULL_MAP_VALUES, false);
        assertEquals("{}", m.writeValueAsString(map));
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

// com.fasterxml.jackson.databind.ser.TestMapSerialization::testEnumMapEntry
    public void testEnumMapEntry() throws IOException
    {
        StringIntMapEntry input = new StringIntMapEntry("answer", 42);
        String json = MAPPER.writeValueAsString(input);
        assertEquals(aposToQuotes("{'answer':42}"), json);

        StringIntMapEntry[] array = new StringIntMapEntry[] { input };
        json = MAPPER.writeValueAsString(array);
        assertEquals(aposToQuotes("[{'answer':42}]"), json);
    }

// com.fasterxml.jackson.databind.ser.TestMapSerialization::testNonNullValueMap
    public void testNonNullValueMap() throws IOException
    {
        String json = MAPPER.writeValueAsString(new NoNullsStringMap()
            .add("a", "foo")
            .add("b", null)
            .add("c", "bar"));
        assertEquals(aposToQuotes("{'a':'foo','c':'bar'}"), json);
    }

// com.fasterxml.jackson.databind.ser.TestMapSerialization::testNonEmptyValueMap
    public void testNonEmptyValueMap() throws IOException
    {
        String json = MAPPER.writeValueAsString(new NoEmptyStringsMap()
            .add("a", "foo")
            .add("b", "bar")
            .add("c", ""));
        assertEquals(aposToQuotes("{'a':'foo','b':'bar'}"), json);
    }

// com.fasterxml.jackson.databind.ser.TestMapSerialization::testNonNullValueMapViaProp
    public void testNonNullValueMapViaProp() throws IOException
    {
        String json = MAPPER.writeValueAsString(new NoNullValuesMapContainer()
            .add("a", "foo")
            .add("b", null)
            .add("c", "bar"));
        assertEquals(aposToQuotes("{'stuff':{'a':'foo','c':'bar'}}"), json);
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

// com.fasterxml.jackson.databind.ser.TestNullProperties::testGlobal
    public void testGlobal() throws IOException
    {
        ObjectMapper m = new ObjectMapper();
        Map<String,Object> result = writeAndMap(m, new SimpleBean());
        assertEquals(2, result.size());
        assertEquals("a", result.get("a"));
        assertNull(result.get("b"));
        assertTrue(result.containsKey("b"));
    }

// com.fasterxml.jackson.databind.ser.TestNullProperties::testNonNullByClass
    public void testNonNullByClass() throws IOException
    {
        ObjectMapper m = new ObjectMapper();
        Map<String,Object> result = writeAndMap(m, new NoNullsBean());
        assertEquals(1, result.size());
        assertFalse(result.containsKey("a"));
        assertNull(result.get("a"));
        assertTrue(result.containsKey("b"));
        assertNull(result.get("b"));
    }

// com.fasterxml.jackson.databind.ser.TestNullProperties::testNonDefaultByClass
    public void testNonDefaultByClass() throws IOException
    {
        ObjectMapper m = new ObjectMapper();
        NonDefaultBean bean = new NonDefaultBean();
        
        bean._a = "notA";
        Map<String,Object> result = writeAndMap(m, bean);
        assertEquals(1, result.size());
        assertTrue(result.containsKey("a"));
        assertEquals("notA", result.get("a"));
        assertFalse(result.containsKey("b"));
        assertNull(result.get("b"));
    }

// com.fasterxml.jackson.databind.ser.TestNullProperties::testMixedMethod
    public void testMixedMethod() throws IOException
    {
        ObjectMapper m = new ObjectMapper();

        MixedBean bean = new MixedBean();
        bean._a = "xyz";
        bean._b = null;
        Map<String,Object> result = writeAndMap(m, bean);
        assertEquals(1, result.size());
        assertEquals("xyz", result.get("a"));
        assertFalse(result.containsKey("b"));

        bean._a = "a";
        bean._b = "b";
        result = writeAndMap(m, bean);
        assertEquals(1, result.size());
        assertEquals("b", result.get("b"));
        assertFalse(result.containsKey("a"));
    }

// com.fasterxml.jackson.databind.ser.TestNullProperties::testDefaultForEmptyList
    public void testDefaultForEmptyList() throws IOException
    {
        ObjectMapper m = new ObjectMapper();
        assertEquals("{}", m.writeValueAsString(new ListBean()));
    }

// com.fasterxml.jackson.databind.ser.TestNullProperties::testNonEmptyDefaultArray
    public void testNonEmptyDefaultArray() throws IOException
    {
        ObjectMapper m = new ObjectMapper();
        assertEquals("{}", m.writeValueAsString(new ArrayBean()));
    }

// com.fasterxml.jackson.databind.ser.TestNullSerialization::testSimple
    public void testSimple() throws Exception
    {
        assertEquals("null", MAPPER.writeValueAsString(null));
    }

// com.fasterxml.jackson.databind.ser.TestNullSerialization::testOverriddenDefaultNulls
    public void testOverriddenDefaultNulls() throws Exception
    {
        DefaultSerializerProvider sp = new DefaultSerializerProvider.Impl();
        sp.setNullValueSerializer(new NullSerializer());
        ObjectMapper m = new ObjectMapper();
        m.setSerializerProvider(sp);
        assertEquals("\"foobar\"", m.writeValueAsString(null));
    }

// com.fasterxml.jackson.databind.ser.TestNullSerialization::testCustomNulls
    public void testCustomNulls() throws Exception
    {
        ObjectMapper m = new ObjectMapper();
        m.setSerializerProvider(new MyNullProvider());
        assertEquals("{\"name\":\"foobar\"}", m.writeValueAsString(new Bean1()));
        assertEquals("{\"type\":null}", m.writeValueAsString(new Bean2()));
    }

// com.fasterxml.jackson.databind.ser.TestNullSerialization::testCustomNullForTrees
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

// com.fasterxml.jackson.databind.ser.TestNullSerialization::testNullSerializerForProperty
    public void testNullSerializerForProperty() throws Exception
    {
        assertEquals("{\"a\":\"foobar\"}", MAPPER.writeValueAsString(new BeanWithNullProps()));
    }

// com.fasterxml.jackson.databind.ser.TestNumbers::testDouble
    public void testDouble() throws Exception
    {
        double[] values = new double[] {
            0.0, 1.0, 0.1, -37.01, 999.99, 0.3, 33.3, Double.NaN, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY
        };
        for (double d : values) {
            String expected = String.valueOf(d);
            if (Double.isNaN(d) || Double.isInfinite(d)) {
                expected = "\""+d+"\"";
            }
            assertEquals(expected, MAPPER.writeValueAsString(Double.valueOf(d)));
        }
    }

// com.fasterxml.jackson.databind.ser.TestNumbers::testBigInteger
    public void testBigInteger() throws Exception
    {
        BigInteger[] values = new BigInteger[] {
                BigInteger.ONE, BigInteger.TEN, BigInteger.ZERO,
                BigInteger.valueOf(1234567890L),
                new BigInteger("123456789012345678901234568"),
                new BigInteger("-1250000124326904597090347547457")
                };

        for (BigInteger value : values) {
            String expected = value.toString();
            assertEquals(expected, MAPPER.writeValueAsString(value));
        }
    }

// com.fasterxml.jackson.databind.ser.TestNumbers::testNumbersAsString
    public void testNumbersAsString() throws Exception
    {
        assertEquals(aposToQuotes("{'value':'3'}"), MAPPER.writeValueAsString(new IntAsString()));
        assertEquals(aposToQuotes("{'value':'4'}"), MAPPER.writeValueAsString(new LongAsString()));
        assertEquals(aposToQuotes("{'value':'-0.5'}"), MAPPER.writeValueAsString(new DoubleAsString()));
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
        ObjectMapper mapper = new ObjectMapper();
        assertEquals("true", serializeAsString(mapper, new AtomicBoolean(true)));
        assertEquals("false", serializeAsString(mapper, new AtomicBoolean(false)));
    }

// com.fasterxml.jackson.databind.ser.TestSimpleAtomicTypes::testAtomicInteger
    public void testAtomicInteger() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        assertEquals("1", serializeAsString(mapper, new AtomicInteger(1)));
        assertEquals("-9", serializeAsString(mapper, new AtomicInteger(-9)));
    }

// com.fasterxml.jackson.databind.ser.TestSimpleAtomicTypes::testAtomicLong
    public void testAtomicLong() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        assertEquals("0", serializeAsString(mapper, new AtomicLong(0)));
    }

// com.fasterxml.jackson.databind.ser.TestSimpleAtomicTypes::testAtomicReference
    public void testAtomicReference() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        String[] strs = new String[] { "abc" };
        assertEquals("[\"abc\"]", serializeAsString(mapper, new AtomicReference<String[]>(strs)));
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

// com.fasterxml.jackson.databind.struct.JSOGDeserialize622Test::testStructJSOGRef
    public void testStructJSOGRef() throws Exception
    {
        IdentifiableExampleJSOG result = MAPPER.readValue(EXP_EXAMPLE_JSOG,
                IdentifiableExampleJSOG.class);
        assertEquals(66, result.foo);
        assertSame(result, result.next);
    }

// com.fasterxml.jackson.databind.struct.JSOGDeserialize622Test::testPolymorphicRoundTrip
    public void testPolymorphicRoundTrip() throws Exception
    {
        JSOGWrapper w = new JSOGWrapper(15);
        
        IdentifiableExampleJSOG ex = new IdentifiableExampleJSOG(123);
        ex.next = ex;
        w.jsog = ex;

        String json = MAPPER.writeValueAsString(w);

        JSOGWrapper out = MAPPER.readValue(json, JSOGWrapper.class);
        assertNotNull(out);
        assertEquals(15, out.value);
        assertTrue(out.jsog instanceof IdentifiableExampleJSOG);
        IdentifiableExampleJSOG jsog = (IdentifiableExampleJSOG) out.jsog;
        assertEquals(123, jsog.foo);
        assertSame(jsog, jsog.next);
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

// com.fasterxml.jackson.databind.struct.TestObjectId::testColumnMetadata
    public void testColumnMetadata() throws Exception
    {
        ColumnMetadata col = new ColumnMetadata("Billy", "employee", "comment");
        Wrapper w = new Wrapper();
        w.a = col;
        w.b = col;
        String json = MAPPER.writeValueAsString(w);
        
        Wrapper deserialized = MAPPER.readValue(json, Wrapper.class);
        assertNotNull(deserialized);
        assertNotNull(deserialized.a);
        assertNotNull(deserialized.b);
        
        assertEquals("Billy", deserialized.a.getName());
        assertEquals("employee", deserialized.a.getType());
        assertEquals("comment", deserialized.a.getComment());

        assertSame(deserialized.a, deserialized.b);
    }

// com.fasterxml.jackson.databind.struct.TestObjectId::testMixedRefsIssue188
    public void testMixedRefsIssue188() throws Exception
    {
        Company comp = new Company();
        Employee e1 = new Employee(1, "First", null);
        Employee e2 = new Employee(2, "Second", e1);
        e1.addReport(e2);
        comp.add(e1);
        comp.add(e2);

        String json = MAPPER.writeValueAsString(comp);
        
        assertEquals("{\"employees\":["
                +"{\"id\":1,\"name\":\"First\",\"manager\":null,\"reports\":[2]},"
                +"{\"id\":2,\"name\":\"Second\",\"manager\":1,\"reports\":[]}"
                +"]}",
                json);
    }

// com.fasterxml.jackson.databind.struct.TestObjectIdDeserialization::testSimpleDeserializationClass
    public void testSimpleDeserializationClass() throws Exception
    {
        
        Identifiable result = mapper.readValue(EXP_SIMPLE_INT_CLASS, Identifiable.class);
        assertEquals(13, result.value);
        assertSame(result, result.next);
    }

// com.fasterxml.jackson.databind.struct.TestObjectIdDeserialization::testMissingObjectId
    public void testMissingObjectId() throws Exception
    {
        Identifiable result = mapper.readValue(aposToQuotes("{'value':28, 'next':{'value':29}}"),
                Identifiable.class);
        assertNotNull(result);
        assertEquals(28, result.value);
        assertNotNull(result.next);
        assertEquals(29, result.next.value);
    }

// com.fasterxml.jackson.databind.struct.TestObjectIdDeserialization::testSimpleUUIDForClassRoundTrip
    public void testSimpleUUIDForClassRoundTrip() throws Exception
    {
        UUIDNode root = new UUIDNode(1);
        UUIDNode child1 = new UUIDNode(2);
        UUIDNode child2 = new UUIDNode(3);
        root.first = child1;
        root.second = child2;
        child1.parent = root;
        child2.parent = root;
        child1.first = child2;

        String json = mapper.writeValueAsString(root);

        
        UUIDNode result = mapper.readValue(json, UUIDNode.class);
        assertEquals(1, result.value);
        UUIDNode result2 = result.first;
        UUIDNode result3 = result.second;
        assertNotNull(result2);
        assertNotNull(result3);
        assertEquals(2, result2.value);
        assertEquals(3, result3.value);

        assertSame(result, result2.parent);
        assertSame(result, result3.parent);
        assertSame(result3, result2.first);
    }

// com.fasterxml.jackson.databind.struct.TestObjectIdDeserialization::testSimpleDeserializationProperty
    public void testSimpleDeserializationProperty() throws Exception
    {
        IdWrapper result = mapper.readValue(EXP_SIMPLE_INT_PROP, IdWrapper.class);
        assertEquals(7, result.node.value);
        assertSame(result.node, result.node.next.node);
    }

// com.fasterxml.jackson.databind.struct.TestObjectIdDeserialization::testSimpleDeserWithForwardRefs
    public void testSimpleDeserWithForwardRefs() throws Exception
    {
        IdWrapper result = mapper.readValue("{\"node\":{\"value\":7,\"next\":{\"node\":1}, \"@id\":1}}"
                ,IdWrapper.class);
        assertEquals(7, result.node.value);
        assertSame(result.node, result.node.next.node);
    }

// com.fasterxml.jackson.databind.struct.TestObjectIdDeserialization::testForwardReference
    public void testForwardReference()
        throws Exception
    {
        String json = "{\"employees\":["
                      + "{\"id\":1,\"name\":\"First\",\"manager\":2,\"reports\":[]},"
                      + "{\"id\":2,\"name\":\"Second\",\"manager\":null,\"reports\":[1]}"
                      + "]}";
        Company company = mapper.readValue(json, Company.class);
        assertEquals(2, company.employees.size());
        Employee firstEmployee = company.employees.get(0);
        Employee secondEmployee = company.employees.get(1);
        assertEquals(1, firstEmployee.id);
        assertEquals(2, secondEmployee.id);
        assertEquals(secondEmployee, firstEmployee.manager); 
        assertEquals(firstEmployee, secondEmployee.reports.get(0)); 
    }

// com.fasterxml.jackson.databind.struct.TestObjectIdDeserialization::testForwardReferenceInCollection
    public void testForwardReferenceInCollection()
        throws Exception
    {
        String json = "{\"employees\":["
                      + "{\"id\":1,\"name\":\"First\",\"manager\":null,\"reports\":[2]},"
                      + "{\"id\":2,\"name\":\"Second\",\"manager\":1,\"reports\":[]}"
                      + "]}";
        Company company = mapper.readValue(json, Company.class);
        assertEquals(2, company.employees.size());
        Employee firstEmployee = company.employees.get(0);
        Employee secondEmployee = company.employees.get(1);
        assertEmployees(firstEmployee, secondEmployee);
    }

// com.fasterxml.jackson.databind.struct.TestObjectIdDeserialization::testForwardReferenceInMap
    public void testForwardReferenceInMap()
        throws Exception
    {
        String json = "{\"employees\":{"
                      + "\"1\":{\"id\":1,\"name\":\"First\",\"manager\":null,\"reports\":[2]},"
                      + "\"2\": 2,"
                      + "\"3\":{\"id\":2,\"name\":\"Second\",\"manager\":1,\"reports\":[]}"
                      + "}}";
        MappedCompany company = mapper.readValue(json, MappedCompany.class);
        assertEquals(3, company.employees.size());
        Employee firstEmployee = company.employees.get(1);
        Employee secondEmployee = company.employees.get(3);
        assertEmployees(firstEmployee, secondEmployee);
    }

// com.fasterxml.jackson.databind.struct.TestObjectIdDeserialization::testForwardReferenceAnySetterCombo
    public void testForwardReferenceAnySetterCombo() throws Exception {
        String json = "{\"@id\":1, \"foo\":2, \"bar\":{\"@id\":2, \"foo\":1}}";
        AnySetterObjectId value = mapper.readValue(json, AnySetterObjectId.class);
        assertSame(value.values.get("bar"), value.values.get("foo"));
    }

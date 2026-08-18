// buggy code
    public JsonSerializer<?> createContextual(SerializerProvider prov,
            BeanProperty property) throws JsonMappingException
    {
        JsonFormat.Value format = findFormatOverrides(prov, property, handledType());
        if (format != null) {
            switch (format.getShape()) {
            case STRING:
                // [databind#2264]: Need special handling for `BigDecimal`
                return ToStringSerializer.instance;
            default:
            }
        }
        return this;
    }

    public void acceptJsonFormatVisitor(JsonFormatVisitorWrapper visitor, JavaType typeHint) throws JsonMappingException
    {
        if (_isInt) {
            visitIntFormat(visitor, typeHint, JsonParser.NumberType.BIG_INTEGER);
        } else {
            if (((Class<?>) handledType()) == BigDecimal.class) {
                visitFloatFormat(visitor, typeHint, JsonParser.NumberType.BIG_DECIMAL);
            } else {
                // otherwise bit unclear what to call... but let's try:
                /*JsonNumberFormatVisitor v2 =*/ visitor.expectNumberFormat(typeHint);
            }
        }
    }

        public JsonSerializer<?> createContextual(SerializerProvider prov,
                BeanProperty property) throws JsonMappingException
        {
            JsonFormat.Value format = findFormatOverrides(prov, property, handledType());
            if (format != null) {
                switch (format.getShape()) {
                case STRING:
                    return ToStringSerializer.instance;
                default:
                }
            }
            return this;
        }

// relevant test
// com.fasterxml.jackson.databind.MapperViaParserTest::testPojoReading
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

// com.fasterxml.jackson.databind.MapperViaParserTest::testIncrementalPojoReading
    public void testIncrementalPojoReading() throws IOException
    {
        JsonFactory jf = new MappingJsonFactory();
        final String JSON = "[ 1, true, null, \"abc\" ]";
        JsonParser p = jf.createParser(new StringReader(JSON));

        
        assertToken(JsonToken.START_ARRAY, p.nextToken());

        assertToken(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertEquals(Integer.valueOf(1), p.readValueAs(Integer.class));
        assertEquals(Boolean.TRUE, p.readValueAs(Boolean.class));
        
        assertNull(p.readValueAs(Object.class));
        
        assertEquals(JsonToken.VALUE_NULL, p.getLastClearedToken());

        assertEquals("abc", p.readValueAs(String.class));

        
        assertNull(p.readValueAs(Object.class));
        assertEquals(JsonToken.END_ARRAY, p.getLastClearedToken());

        
        assertNull(p.nextToken());

        p.close();
    }

// com.fasterxml.jackson.databind.MapperViaParserTest::testPojoReadingFailing
    public void testPojoReadingFailing() throws IOException
    {
        
        JsonFactory f = new JsonFactory();
        try {
            final String JSON = "{ \"x\" : 9 }";
            JsonParser p = f.createParser(new StringReader(JSON));
            Pojo pojo = p.readValueAs(Pojo.class);
            fail("Expected an exception: got "+pojo);
        } catch (IllegalStateException e) {
            verifyException(e, "No ObjectCodec defined");
        }
    }

// com.fasterxml.jackson.databind.MapperViaParserTest::testEscapingUsingMapper
    public void testEscapingUsingMapper() throws Exception
    {
        ObjectMapper mapper = JsonMapper.builder()
               .configure(JsonWriteFeature.ESCAPE_NON_ASCII, true)
               .build();
        mapper.writeValueAsString(String.valueOf((char) 257));
    }

// com.fasterxml.jackson.databind.ObjectMapperTest::testFactoryFeatures
    public void testFactoryFeatures()
    {
        assertTrue(MAPPER.isEnabled(JsonFactory.Feature.CANONICALIZE_FIELD_NAMES));
    }

// com.fasterxml.jackson.databind.ObjectMapperTest::testGeneratorFeatures
    public void testGeneratorFeatures()
    {
        
        JsonMapper mapper = new JsonMapper();
        assertTrue(mapper.isEnabled(JsonGenerator.Feature.AUTO_CLOSE_TARGET));
        assertTrue(mapper.isEnabled(StreamWriteFeature.AUTO_CLOSE_TARGET));
        assertFalse(mapper.isEnabled(JsonWriteFeature.ESCAPE_NON_ASCII));
        assertTrue(mapper.isEnabled(JsonWriteFeature.WRITE_NAN_AS_STRINGS));
        mapper = JsonMapper.builder()
                .disable(StreamWriteFeature.FLUSH_PASSED_TO_STREAM)
                .disable(JsonWriteFeature.WRITE_NAN_AS_STRINGS)
                .build();
        assertFalse(mapper.isEnabled(StreamWriteFeature.FLUSH_PASSED_TO_STREAM));
        assertFalse(mapper.isEnabled(JsonWriteFeature.WRITE_NAN_AS_STRINGS));
    }

// com.fasterxml.jackson.databind.ObjectMapperTest::testParserFeatures
    public void testParserFeatures()
    {
        
        ObjectMapper mapper = new ObjectMapper();

        assertTrue(mapper.isEnabled(JsonParser.Feature.AUTO_CLOSE_SOURCE));
        assertTrue(mapper.isEnabled(StreamReadFeature.AUTO_CLOSE_SOURCE));
        assertFalse(mapper.isEnabled(JsonParser.Feature.IGNORE_UNDEFINED));

        mapper.disable(JsonParser.Feature.AUTO_CLOSE_SOURCE,
                JsonParser.Feature.STRICT_DUPLICATE_DETECTION);
        assertFalse(mapper.isEnabled(JsonParser.Feature.AUTO_CLOSE_SOURCE));
        assertFalse(mapper.isEnabled(StreamReadFeature.AUTO_CLOSE_SOURCE));
    }

// com.fasterxml.jackson.databind.ObjectMapperTest::testCopy
    public void testCopy() throws Exception
    {
        ObjectMapper m = new ObjectMapper();
        assertTrue(m.isEnabled(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES));
        m.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        assertFalse(m.isEnabled(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES));
        InjectableValues inj = new InjectableValues.Std();
        m.setInjectableValues(inj);
        assertFalse(m.isEnabled(JsonParser.Feature.IGNORE_UNDEFINED));
        m.enable(JsonParser.Feature.IGNORE_UNDEFINED);
        assertTrue(m.isEnabled(JsonParser.Feature.IGNORE_UNDEFINED));

        
        
        ObjectMapper m2 = m.copy();
        assertFalse(m2.isEnabled(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES));
        m2.enable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        assertTrue(m2.isEnabled(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES));
        assertSame(inj, m2.getInjectableValues());

        
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

        m.addMixIn(String.class, Integer.class);
        assertEquals(1, m.getSerializationConfig().mixInCount());
        assertEquals(0, m2.getSerializationConfig().mixInCount());
        assertEquals(1, m.getDeserializationConfig().mixInCount());
        assertEquals(0, m2.getDeserializationConfig().mixInCount());

        
        assertTrue(m2.isEnabled(JsonParser.Feature.IGNORE_UNDEFINED));
    }

// com.fasterxml.jackson.databind.ObjectMapperTest::testCopyOfConfigOverrides
    public void testCopyOfConfigOverrides() throws Exception
    {
        ObjectMapper m = new ObjectMapper();
        SerializationConfig config = m.getSerializationConfig();
        assertEquals(JsonInclude.Value.empty(), config.getDefaultPropertyInclusion());
        assertEquals(JsonSetter.Value.empty(), config.getDefaultSetterInfo());
        assertNull(config.getDefaultMergeable());
        VisibilityChecker<?> defaultVis = config.getDefaultVisibilityChecker();
        assertEquals(VisibilityChecker.Std.class, defaultVis.getClass());

        
        JsonInclude.Value customIncl = JsonInclude.Value.empty().withValueInclusion(JsonInclude.Include.NON_DEFAULT);
        m.setDefaultPropertyInclusion(customIncl);
        JsonSetter.Value customSetter = JsonSetter.Value.forValueNulls(Nulls.SKIP);
        m.setDefaultSetterInfo(customSetter);
        m.setDefaultMergeable(Boolean.TRUE);
        VisibilityChecker<?> customVis = VisibilityChecker.Std.defaultInstance()
                .withFieldVisibility(Visibility.ANY);
        m.setVisibility(customVis);
        assertSame(customVis, m.getVisibilityChecker());

        
        ObjectMapper m2 = m.copy();
        SerializationConfig config2 = m2.getSerializationConfig();
        assertSame(customIncl, config2.getDefaultPropertyInclusion());
        assertSame(customSetter, config2.getDefaultSetterInfo());
        assertEquals(Boolean.TRUE, config2.getDefaultMergeable());
        assertSame(customVis, config2.getDefaultVisibilityChecker());
    }

// com.fasterxml.jackson.databind.ObjectMapperTest::testFailedCopy
    public void testFailedCopy() throws Exception
    {
        NoCopyMapper src = new NoCopyMapper();
        try {
            src.copy();
            fail("Should not pass");
        } catch (IllegalStateException e) {
            verifyException(e, "does not override copy()");
        }
    }

// com.fasterxml.jackson.databind.ObjectMapperTest::testAnnotationIntrospectorCopyin
    public void testAnnotationIntrospectorCopyin() 
    {
        ObjectMapper m = new ObjectMapper();
        m.setAnnotationIntrospector(new MyAnnotationIntrospector());
        assertEquals(MyAnnotationIntrospector.class,
                m.getDeserializationConfig().getAnnotationIntrospector().getClass());
        ObjectMapper m2 = m.copy();

        assertEquals(MyAnnotationIntrospector.class,
                m2.getDeserializationConfig().getAnnotationIntrospector().getClass());
        assertEquals(MyAnnotationIntrospector.class,
                m2.getSerializationConfig().getAnnotationIntrospector().getClass());
    }

// com.fasterxml.jackson.databind.ObjectMapperTest::testProps
    public void testProps()
    {
        ObjectMapper m = new ObjectMapper();
        
        assertNotNull(m.getNodeFactory());
        JsonNodeFactory nf = new JsonNodeFactory(true);
        m.setNodeFactory(nf);
        assertNull(m.getInjectableValues());
        assertSame(nf, m.getNodeFactory());
    }

// com.fasterxml.jackson.databind.ObjectMapperTest::testConfigForPropertySorting
    public void testConfigForPropertySorting() throws Exception
    {
        ObjectMapper m = new ObjectMapper();
        
        
        assertFalse(m.isEnabled(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY));
        SerializationConfig sc = m.getSerializationConfig();
        assertFalse(sc.isEnabled(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY));
        assertFalse(sc.shouldSortPropertiesAlphabetically());
        DeserializationConfig dc = m.getDeserializationConfig();
        assertFalse(dc.shouldSortPropertiesAlphabetically());

        
        m = objectMapperBuilder()
                .enable(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY)
                .build();
        sc = m.getSerializationConfig();
        assertTrue(sc.isEnabled(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY));
        assertTrue(sc.shouldSortPropertiesAlphabetically());
        dc = m.getDeserializationConfig();
        
        assertTrue(dc.isEnabled(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY));
        assertTrue(dc.shouldSortPropertiesAlphabetically());
    }

// com.fasterxml.jackson.databind.ObjectMapperTest::testJsonFactoryLinkage
    public void testJsonFactoryLinkage()
    {
        
        assertSame(MAPPER, MAPPER.getFactory().getCodec());

        
        JsonFactory f = new JsonFactory();
        ObjectMapper m = new ObjectMapper(f);
        assertSame(f, m.getFactory());
        assertSame(m, f.getCodec());
    }

// com.fasterxml.jackson.databind.ObjectMapperTest::testProviderConfig
    public void testProviderConfig() throws Exception   
    {
        ObjectMapper m = new ObjectMapper();
        final String JSON = "{ \"x\" : 3 }";

        assertEquals(0, m._deserializationContext._cache.cachedDeserializersCount());
        
        Bean bean = m.readValue(JSON, Bean.class);
        assertNotNull(bean);
        
        assertEquals(2, m._deserializationContext._cache.cachedDeserializersCount());
        m._deserializationContext._cache.flushCachedDeserializers();
        assertEquals(0, m._deserializationContext._cache.cachedDeserializersCount());

        
        m = new ObjectMapper();
        List<?> stuff = m.readValue("[ ]", List.class);
        assertNotNull(stuff);
        
        
        assertEquals(4, m._deserializationContext._cache.cachedDeserializersCount());
    }

// com.fasterxml.jackson.databind.ObjectMapperTest::testCustomDefaultPrettyPrinter
    public void testCustomDefaultPrettyPrinter() throws Exception
    {
        final ObjectMapper m = new ObjectMapper();
        final int[] input = new int[] { 1, 2 };

        
        assertEquals("[1,2]", m.writeValueAsString(input));

        
        m.enable(SerializationFeature.INDENT_OUTPUT);
        assertEquals("[ 1, 2 ]", m.writeValueAsString(input));
        assertEquals("[ 1, 2 ]", m.writerWithDefaultPrettyPrinter().writeValueAsString(input));
        assertEquals("[ 1, 2 ]", m.writer().withDefaultPrettyPrinter().writeValueAsString(input));

        
        m.setDefaultPrettyPrinter(new FooPrettyPrinter());
        assertEquals("[1 , 2]", m.writeValueAsString(input));
        assertEquals("[1 , 2]", m.writerWithDefaultPrettyPrinter().writeValueAsString(input));
        assertEquals("[1 , 2]", m.writer().withDefaultPrettyPrinter().writeValueAsString(input));

        
        assertEquals("[1,2]", m.writer().without(SerializationFeature.INDENT_OUTPUT)
                .writeValueAsString(input));
    }

// com.fasterxml.jackson.databind.ObjectMapperTest::testNonSerializabilityOfObject
    public void testNonSerializabilityOfObject()
    {
        ObjectMapper m = new ObjectMapper();
        assertFalse(m.canSerialize(Object.class));
        
        assertFalse(m.canSerialize(Object.class));

        
        m = new ObjectMapper();
        m.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
        assertTrue(m.canSerialize(Object.class));
        assertTrue(MAPPER.writer().without(SerializationFeature.FAIL_ON_EMPTY_BEANS)
                .canSerialize(Object.class));
        assertFalse(MAPPER.writer().with(SerializationFeature.FAIL_ON_EMPTY_BEANS)
                .canSerialize(Object.class));
    }

// com.fasterxml.jackson.databind.ObjectMapperTest::testEmptyBeanSerializability
    public void testEmptyBeanSerializability()
    {
        
        assertFalse(MAPPER.writer().with(SerializationFeature.FAIL_ON_EMPTY_BEANS)
                .canSerialize(EmptyBean.class));
        
        assertTrue(MAPPER.writer().without(SerializationFeature.FAIL_ON_EMPTY_BEANS)
                .canSerialize(EmptyBean.class));
    }

// com.fasterxml.jackson.databind.ObjectMapperTest::testSerializerProviderAccess
    public void testSerializerProviderAccess() throws Exception
    {
        
        ObjectMapper mapper = new ObjectMapper();
        JsonSerializer<?> ser = mapper.getSerializerProviderInstance()
                .findValueSerializer(Bean.class);
        assertNotNull(ser);
        assertEquals(Bean.class, ser.handledType());
    }

// com.fasterxml.jackson.databind.ObjectMapperTest::testCopyOfParserFeatures
    public void testCopyOfParserFeatures() throws Exception
    {
        
        ObjectMapper mapper = new ObjectMapper();
        assertFalse(mapper.isEnabled(JsonParser.Feature.IGNORE_UNDEFINED));
        mapper.configure(JsonParser.Feature.IGNORE_UNDEFINED, true);
        assertTrue(mapper.isEnabled(JsonParser.Feature.IGNORE_UNDEFINED));

        ObjectMapper copy = mapper.copy();
        assertTrue(copy.isEnabled(JsonParser.Feature.IGNORE_UNDEFINED));

        
        copy.configure(JsonParser.Feature.IGNORE_UNDEFINED, false);
        assertFalse(copy.isEnabled(JsonParser.Feature.IGNORE_UNDEFINED));
        assertTrue(mapper.isEnabled(JsonParser.Feature.IGNORE_UNDEFINED));
    }

// com.fasterxml.jackson.databind.ObjectMapperTest::testDataOutputViaMapper
    public void testDataOutputViaMapper() throws Exception
    {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        ObjectNode input = MAPPER.createObjectNode();
        input.put("a", 1);
        final String exp = "{\"a\":1}";
        try (DataOutputStream data = new DataOutputStream(bytes)) {
            MAPPER.writeValue((DataOutput) data, input);
        }
        assertEquals(exp, bytes.toString("UTF-8"));

        
        bytes.reset();
        try (DataOutputStream data = new DataOutputStream(bytes)) {
            MAPPER.writer().writeValue((DataOutput) data, input);
        }
        assertEquals(exp, bytes.toString("UTF-8"));
    }

// com.fasterxml.jackson.databind.ObjectMapperTest::testDataInputViaMapper
    public void testDataInputViaMapper() throws Exception
    {
        byte[] src = "{\"a\":1}".getBytes("UTF-8");
        DataInput input = new DataInputStream(new ByteArrayInputStream(src));
        Map<String,Object> map = (Map<String,Object>) MAPPER.readValue(input, Map.class);
        assertEquals(Integer.valueOf(1), map.get("a"));

        input = new DataInputStream(new ByteArrayInputStream(src));
        
        map = MAPPER.readerFor(Map.class)
                .readValue(input);
        assertEquals(Integer.valueOf(1), map.get("a"));

        input = new DataInputStream(new ByteArrayInputStream(src));
        JsonNode n = MAPPER.readerFor(Map.class)
                .readTree(input);
        assertNotNull(n);
    }

// com.fasterxml.jackson.databind.ObjectReaderTest::testSimpleViaParser
    public void testSimpleViaParser() throws Exception
    {
        final String JSON = "[1]";
        JsonParser p = MAPPER.getFactory().createParser(JSON);
        Object ob = MAPPER.readerFor(Object.class)
                .readValue(p);
        p.close();
        assertTrue(ob instanceof List<?>);
    }

// com.fasterxml.jackson.databind.ObjectReaderTest::testSimpleAltSources
    public void testSimpleAltSources() throws Exception
    {
        final String JSON = "[1]";
        final byte[] BYTES = JSON.getBytes("UTF-8");
        Object ob = MAPPER.readerFor(Object.class)
                .readValue(BYTES);
        assertTrue(ob instanceof List<?>);

        ob = MAPPER.readerFor(Object.class)
                .readValue(BYTES, 0, BYTES.length);
        assertTrue(ob instanceof List<?>);
        assertEquals(1, ((List<?>) ob).size());
    }

// com.fasterxml.jackson.databind.ObjectReaderTest::testParserFeatures
    public void testParserFeatures() throws Exception
    {
        final String JSON = "[  7 ]";
        
        ObjectReader reader = MAPPER.readerFor(int[].class)
                .with(JsonReadFeature.ALLOW_JAVA_COMMENTS);

        int[] value = reader.readValue(JSON);
        assertNotNull(value);
        assertEquals(1, value.length);
        assertEquals(7, value[0]);

        
        try {
            reader.without(JsonReadFeature.ALLOW_JAVA_COMMENTS).readValue(JSON);
            fail("Should not have passed");
        } catch (JsonProcessingException e) {
            verifyException(e, "foo");
        }
    }

// com.fasterxml.jackson.databind.ObjectReaderTest::testNodeHandling
    public void testNodeHandling() throws Exception
    {
        JsonNodeFactory nodes = new JsonNodeFactory(true);
        ObjectReader r = MAPPER.reader().with(nodes);
        
        assertSame(r, r.with(nodes));
        assertTrue(r.createArrayNode().isArray());
        assertTrue(r.createObjectNode().isObject());
    }

// com.fasterxml.jackson.databind.ObjectReaderTest::testFeatureSettings
    public void testFeatureSettings() throws Exception
    {
        ObjectReader r = MAPPER.reader();
        assertFalse(r.isEnabled(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES));
        assertFalse(r.isEnabled(JsonParser.Feature.IGNORE_UNDEFINED));
        
        r = r.withoutFeatures(DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES,
                DeserializationFeature.FAIL_ON_INVALID_SUBTYPE);
        assertFalse(r.isEnabled(DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES));
        assertFalse(r.isEnabled(DeserializationFeature.FAIL_ON_INVALID_SUBTYPE));
        r = r.withFeatures(DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES,
                DeserializationFeature.FAIL_ON_INVALID_SUBTYPE);
        assertTrue(r.isEnabled(DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES));
        assertTrue(r.isEnabled(DeserializationFeature.FAIL_ON_INVALID_SUBTYPE));

        
        assertSame(r, r.with(DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES,
                DeserializationFeature.FAIL_ON_INVALID_SUBTYPE));
    }

// com.fasterxml.jackson.databind.ObjectReaderTest::testMiscSettings
    public void testMiscSettings() throws Exception
    {
        ObjectReader r = MAPPER.reader();
        assertSame(MAPPER.getFactory(), r.getFactory());

        JsonFactory f = new JsonFactory();
        r = r.with(f);
        assertSame(f, r.getFactory());
        assertSame(r, r.with(f));

        assertNotNull(r.getTypeFactory());
        assertNull(r.getInjectableValues());

        r = r.withAttributes(Collections.emptyMap());
        ContextAttributes attrs = r.getAttributes();
        assertNotNull(attrs);
        assertNull(attrs.getAttribute("abc"));
        assertSame(r, r.withoutAttribute("foo"));

        ObjectReader newR = r.forType(MAPPER.constructType(String.class));
        assertNotSame(r, newR);
        assertSame(newR, newR.forType(String.class));

        DeserializationProblemHandler probH = new DeserializationProblemHandler() {
        };
        newR = r.withHandler(probH);
        assertNotSame(r, newR);
        assertSame(newR, newR.withHandler(probH));
        r = newR;
    }

// com.fasterxml.jackson.databind.ObjectReaderTest::testDeprecatedSettings
    public void testDeprecatedSettings() throws Exception
    {
        ObjectReader r = MAPPER.reader();

        
        ObjectReader newR = r.forType(MAPPER.constructType(String.class));
        assertSame(newR, newR.withType(String.class));
        assertSame(newR, newR.withType(MAPPER.constructType(String.class)));

        newR = newR.withRootName(PropertyName.construct("foo"));
        assertNotSame(r, newR);
        assertSame(newR, newR.withRootName(PropertyName.construct("foo")));
    }

// com.fasterxml.jackson.databind.ObjectReaderTest::testNoPrefetch
    public void testNoPrefetch() throws Exception
    {
        ObjectReader r = MAPPER.reader()
                .without(DeserializationFeature.EAGER_DESERIALIZER_FETCH);
        Number n = r.forType(Integer.class).readValue("123 ");
        assertEquals(Integer.valueOf(123), n);
    }

// com.fasterxml.jackson.databind.ObjectReaderTest::testNoPointerLoading
    public void testNoPointerLoading() throws Exception {
        final String source = "{\"foo\":{\"bar\":{\"caller\":{\"name\":{\"value\":1234}}}}}";

        JsonNode tree = MAPPER.readTree(source);
        JsonNode node = tree.at("/foo/bar/caller");
        POJO pojo = MAPPER.treeToValue(node, POJO.class);
        assertTrue(pojo.name.containsKey("value"));
        assertEquals(1234, pojo.name.get("value"));
    }

// com.fasterxml.jackson.databind.ObjectReaderTest::testPointerLoading
    public void testPointerLoading() throws Exception {
        final String source = "{\"foo\":{\"bar\":{\"caller\":{\"name\":{\"value\":1234}}}}}";

        ObjectReader reader = MAPPER.readerFor(POJO.class).at("/foo/bar/caller");

        POJO pojo = reader.readValue(source);
        assertTrue(pojo.name.containsKey("value"));
        assertEquals(1234, pojo.name.get("value"));
    }

// com.fasterxml.jackson.databind.ObjectReaderTest::testPointerLoadingAsJsonNode
    public void testPointerLoadingAsJsonNode() throws Exception {
        final String source = "{\"foo\":{\"bar\":{\"caller\":{\"name\":{\"value\":1234}}}}}";

        ObjectReader reader = MAPPER.readerFor(POJO.class).at(JsonPointer.compile("/foo/bar/caller"));

        JsonNode node = reader.readTree(source);
        assertTrue(node.has("name"));
        assertEquals("{\"value\":1234}", node.get("name").toString());
    }

// com.fasterxml.jackson.databind.ObjectReaderTest::testPointerLoadingMappingIteratorOne
    public void testPointerLoadingMappingIteratorOne() throws Exception {
        final String source = "{\"foo\":{\"bar\":{\"caller\":{\"name\":{\"value\":1234}}}}}";

        ObjectReader reader = MAPPER.readerFor(POJO.class).at("/foo/bar/caller");

        MappingIterator<POJO> itr = reader.readValues(source);

        POJO pojo = itr.next();

        assertTrue(pojo.name.containsKey("value"));
        assertEquals(1234, pojo.name.get("value"));
        assertFalse(itr.hasNext());
        itr.close();
    }

// com.fasterxml.jackson.databind.ObjectReaderTest::testPointerLoadingMappingIteratorMany
    public void testPointerLoadingMappingIteratorMany() throws Exception {
        final String source = "{\"foo\":{\"bar\":{\"caller\":[{\"name\":{\"value\":1234}}, {\"name\":{\"value\":5678}}]}}}";

        ObjectReader reader = MAPPER.readerFor(POJO.class).at("/foo/bar/caller");

        MappingIterator<POJO> itr = reader.readValues(source);

        POJO pojo = itr.next();

        assertTrue(pojo.name.containsKey("value"));
        assertEquals(1234, pojo.name.get("value"));
        assertTrue(itr.hasNext());
        
        pojo = itr.next();

        assertNotNull(pojo.name);
        assertTrue(pojo.name.containsKey("value"));
        assertEquals(5678, pojo.name.get("value"));
        assertFalse(itr.hasNext());
        itr.close();
    }

// com.fasterxml.jackson.databind.ObjectReaderTest::testPointerWithArrays
    public void testPointerWithArrays() throws Exception
    {
        final String json = aposToQuotes("{\n'wrapper1': {\n" +
                "  'set1': ['one', 'two', 'three'],\n" +
                "  'set2': ['four', 'five', 'six']\n" +
                "},\n" +
                "'wrapper2': {\n" +
                "  'set1': ['one', 'two', 'three'],\n" +
                "  'set2': ['four', 'five', 'six']\n" +
                "}\n}");

        final Pojo1637 testObject = MAPPER.readerFor(Pojo1637.class)
                .at("/wrapper1")
                .readValue(json);
        assertNotNull(testObject);

        assertNotNull(testObject.set1);
        assertTrue(!testObject.set1.isEmpty());

        assertNotNull(testObject.set2);
        assertTrue(!testObject.set2.isEmpty());
    }

// com.fasterxml.jackson.databind.ObjectReaderTest::testTreeToValue
    public void testTreeToValue() throws Exception
    {
        ArrayNode n = MAPPER.createArrayNode();
        n.add("xyz");
        ObjectReader r = MAPPER.readerFor(String.class);
        List<?> list = r.treeToValue(n, List.class);
        assertEquals(1, list.size());
    }

// com.fasterxml.jackson.databind.ObjectReaderTest::testCodecUnsupportedWrites
    public void testCodecUnsupportedWrites() throws Exception
    {
        ObjectReader r = MAPPER.readerFor(String.class);
        JsonGenerator g = MAPPER.getFactory().createGenerator(new StringWriter());
        ObjectNode n = MAPPER.createObjectNode();
        try {
            r.writeTree(g, n);
            fail("Should not pass");
        } catch (UnsupportedOperationException e) {
            ;
        }
        try {
            r.writeValue(g, "Foo");
            fail("Should not pass");
        } catch (UnsupportedOperationException e) {
            ;
        }
        g.close();

        g.close();
    }

// com.fasterxml.jackson.databind.ObjectReaderTest::testMissingType
    public void testMissingType() throws Exception
    {
        ObjectReader r = MAPPER.reader();
        try {
            r.readValue("1");
            fail("Should not pass");
        } catch (JsonMappingException e) {
            verifyException(e, "No value type configured");
        }
    }

// com.fasterxml.jackson.databind.ObjectReaderTest::testSchema
    public void testSchema() throws Exception
    {
        ObjectReader r = MAPPER.readerFor(String.class);
        
        
        r = r.with((FormatSchema) null);

        try {
            
            r = r.with(new BogusSchema())
                .readValue(quote("foo"));
            
            fail("Should not pass");
        } catch (IllegalArgumentException e) {
            verifyException(e, "Cannot use FormatSchema");
        }
    }

// com.fasterxml.jackson.databind.ObjectWriterTest::testPrettyPrinter
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

// com.fasterxml.jackson.databind.ObjectWriterTest::testPrefetch
    public void testPrefetch() throws Exception
    {
        ObjectWriter writer = MAPPER.writer();
        assertFalse(writer.hasPrefetchedSerializer());
        writer = writer.forType(String.class);
        assertTrue(writer.hasPrefetchedSerializer());
    }

// com.fasterxml.jackson.databind.ObjectWriterTest::testObjectWriterFeatures
    public void testObjectWriterFeatures() throws Exception
    {
        ObjectWriter writer = MAPPER.writer()
                .without(JsonWriteFeature.QUOTE_FIELD_NAMES);                
        Map<String,Integer> map = new HashMap<String,Integer>();
        map.put("a", 1);
        assertEquals("{a:1}", writer.writeValueAsString(map));
        
        assertEquals("{\"a\":1}", writer.with(JsonWriteFeature.QUOTE_FIELD_NAMES)
                .writeValueAsString(map));
    }

// com.fasterxml.jackson.databind.ObjectWriterTest::testObjectWriterWithNode
    public void testObjectWriterWithNode() throws Exception
    {
        ObjectNode stuff = MAPPER.createObjectNode();
        stuff.put("a", 5);
        ObjectWriter writer = MAPPER.writerFor(JsonNode.class);
        String json = writer.writeValueAsString(stuff);
        assertEquals("{\"a\":5}", json);
    }

// com.fasterxml.jackson.databind.ObjectWriterTest::testPolymorphicWithTyping
    public void testPolymorphicWithTyping() throws Exception
    {
        ObjectWriter writer = MAPPER.writerFor(PolyBase.class);
        String json;

        json = writer.writeValueAsString(new ImplA(3));
        assertEquals(aposToQuotes("{'type':'A','value':3}"), json);
        json = writer.writeValueAsString(new ImplB(-5));
        assertEquals(aposToQuotes("{'type':'B','b':-5}"), json);
    }

// com.fasterxml.jackson.databind.ObjectWriterTest::testCanSerialize
    public void testCanSerialize() throws Exception
    {
        assertTrue(MAPPER.writer().canSerialize(String.class));
        assertTrue(MAPPER.writer().canSerialize(String.class, null));
    }

// com.fasterxml.jackson.databind.ObjectWriterTest::testNoPrefetch
    public void testNoPrefetch() throws Exception
    {
        ObjectWriter w = MAPPER.writer()
                .without(SerializationFeature.EAGER_SERIALIZER_FETCH);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        w.writeValue(out, Integer.valueOf(3));
        out.close();
        assertEquals("3", out.toString("UTF-8"));
    }

// com.fasterxml.jackson.databind.ObjectWriterTest::testWithCloseCloseable
    public void testWithCloseCloseable() throws Exception
    {
        ObjectWriter w = MAPPER.writer()
                .with(SerializationFeature.CLOSE_CLOSEABLE);
        assertTrue(w.isEnabled(SerializationFeature.CLOSE_CLOSEABLE));
        CloseableValue input = new CloseableValue();
        assertFalse(input.closed);
        byte[] json = w.writeValueAsBytes(input);
        assertNotNull(json);
        assertTrue(input.closed);
        input.close();

        
        JsonGenerator g = MAPPER.getFactory().createGenerator(new StringWriter());
        input = new CloseableValue();
        assertFalse(input.closed);
        w.writeValue(g, input);
        assertTrue(input.closed);
        g.close();
        input.close();
    }

// com.fasterxml.jackson.databind.ObjectWriterTest::testViewSettings
    public void testViewSettings() throws Exception
    {
        ObjectWriter w = MAPPER.writer();
        ObjectWriter newW = w.withView(String.class);
        assertNotSame(w, newW);
        assertSame(newW, newW.withView(String.class));

        newW = w.with(Locale.CANADA);
        assertNotSame(w, newW);
        assertSame(newW, newW.with(Locale.CANADA));
    }

// com.fasterxml.jackson.databind.ObjectWriterTest::testMiscSettings
    public void testMiscSettings() throws Exception
    {
        ObjectWriter w = MAPPER.writer();
        assertSame(MAPPER.getFactory(), w.getFactory());
        assertFalse(w.hasPrefetchedSerializer());
        assertNotNull(w.getTypeFactory());

        JsonFactory f = new JsonFactory();
        w = w.with(f);
        assertSame(f, w.getFactory());
        ObjectWriter newW = w.with(Base64Variants.MODIFIED_FOR_URL);
        assertNotSame(w, newW);
        assertSame(newW, newW.with(Base64Variants.MODIFIED_FOR_URL));
        
        w = w.withAttributes(Collections.emptyMap());
        w = w.withAttribute("a", "b");
        assertEquals("b", w.getAttributes().getAttribute("a"));
        w = w.withoutAttribute("a");
        assertNull(w.getAttributes().getAttribute("a"));

        FormatSchema schema = new BogusSchema();
        try {
            newW = w.with(schema);
            fail("Should not pass");
        } catch (IllegalArgumentException e) {
            verifyException(e, "Cannot use FormatSchema");
        }
    }

// com.fasterxml.jackson.databind.ObjectWriterTest::testRootValueSettings
    public void testRootValueSettings() throws Exception
    {
        ObjectWriter w = MAPPER.writer();
        
        
        ObjectWriter newW = w.withRootName("foo");
        assertNotSame(w, newW);
        assertSame(newW, newW.withRootName(PropertyName.construct("foo")));
        w = newW;
        newW = w.withRootName((String) null);
        assertNotSame(w, newW);
        assertSame(newW, newW.withRootName((PropertyName) null));

        

        w = w.withRootValueSeparator(new SerializedString(","));
        assertSame(w, w.withRootValueSeparator(new SerializedString(",")));
        assertSame(w, w.withRootValueSeparator(","));

         newW = w.withRootValueSeparator("/");
        assertNotSame(w, newW);
        assertSame(newW, newW.withRootValueSeparator("/"));

        newW = w.withRootValueSeparator((String) null);
        assertNotSame(w, newW);

        newW = w.withRootValueSeparator((SerializableString) null);
        assertNotSame(w, newW);
    }

// com.fasterxml.jackson.databind.ObjectWriterTest::testFeatureSettings
    public void testFeatureSettings() throws Exception
    {
        ObjectWriter w = MAPPER.writer();
        assertFalse(w.isEnabled(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES));
        assertFalse(w.isEnabled(JsonGenerator.Feature.STRICT_DUPLICATE_DETECTION));
        ObjectWriter newW = w.with(SerializationFeature.FAIL_ON_UNWRAPPED_TYPE_IDENTIFIERS,
                SerializationFeature.INDENT_OUTPUT);
        assertNotSame(w, newW);
        assertTrue(newW.isEnabled(SerializationFeature.FAIL_ON_UNWRAPPED_TYPE_IDENTIFIERS));
        assertTrue(newW.isEnabled(SerializationFeature.INDENT_OUTPUT));
        assertSame(newW, newW.with(SerializationFeature.INDENT_OUTPUT));
        assertSame(newW, newW.withFeatures(SerializationFeature.INDENT_OUTPUT));

        newW = w.withFeatures(SerializationFeature.FAIL_ON_UNWRAPPED_TYPE_IDENTIFIERS,
                SerializationFeature.INDENT_OUTPUT);
        assertNotSame(w, newW);

        newW = w.without(SerializationFeature.FAIL_ON_EMPTY_BEANS,
                SerializationFeature.EAGER_SERIALIZER_FETCH);
        assertNotSame(w, newW);
        assertFalse(newW.isEnabled(SerializationFeature.FAIL_ON_EMPTY_BEANS));
        assertFalse(newW.isEnabled(SerializationFeature.EAGER_SERIALIZER_FETCH));
        assertSame(newW, newW.without(SerializationFeature.FAIL_ON_EMPTY_BEANS));
        assertSame(newW, newW.withoutFeatures(SerializationFeature.FAIL_ON_EMPTY_BEANS));

        assertNotSame(w, w.withoutFeatures(SerializationFeature.FAIL_ON_EMPTY_BEANS,
                SerializationFeature.EAGER_SERIALIZER_FETCH));
    }

// com.fasterxml.jackson.databind.ObjectWriterTest::testGeneratorFeatures
    public void testGeneratorFeatures() throws Exception
    {
        ObjectWriter w = MAPPER.writer();
        assertNotSame(w, w.with(JsonWriteFeature.ESCAPE_NON_ASCII));
        assertNotSame(w, w.withFeatures(JsonWriteFeature.ESCAPE_NON_ASCII));

        assertTrue(w.isEnabled(JsonGenerator.Feature.AUTO_CLOSE_TARGET));
        assertNotSame(w, w.without(JsonGenerator.Feature.AUTO_CLOSE_TARGET));
        assertNotSame(w, w.withoutFeatures(JsonGenerator.Feature.AUTO_CLOSE_TARGET));
    }

// com.fasterxml.jackson.databind.ObjectWriterTest::testArgumentChecking
    public void testArgumentChecking() throws Exception
    {
        final ObjectWriter w = MAPPER.writer();
        try {
            w.acceptJsonFormatVisitor((JavaType) null, null);
            fail("Should not pass");
        } catch (IllegalArgumentException e) {
            verifyException(e, "type must be provided");
        }
    }

// com.fasterxml.jackson.databind.ObjectWriterTest::testSchema
    public void testSchema() throws Exception
    {
        try {
            MAPPER.writerFor(String.class)
                .with(new BogusSchema())
                .writeValueAsBytes("foo");
            fail("Should not pass");
        } catch (IllegalArgumentException e) {
            verifyException(e, "Cannot use FormatSchema");
        }
    }

// com.fasterxml.jackson.databind.RoundtripTest::testMedaItemRoundtrip
    public void testMedaItemRoundtrip() throws Exception
    {
        MediaItem.Content c = new MediaItem.Content();
        c.setBitrate(9600);
        c.setCopyright("none");
        c.setDuration(360000L);
        c.setFormat("lzf");
        c.setHeight(640);
        c.setSize(128000L);
        c.setTitle("Amazing Stuff For Something Or Oth\u00CBr!");
        c.setUri("http://multi.fario.us/index.html");
        c.setWidth(1400);

        c.addPerson("Joe Sixp\u00e2ck");
        c.addPerson("Ezekiel");
        c.addPerson("Sponge-Bob Squarepant\u00DF");
        
        MediaItem input = new MediaItem(c);
        input.addPhoto(new MediaItem.Photo());
        input.addPhoto(new MediaItem.Photo());
        input.addPhoto(new MediaItem.Photo());

        String json = MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(input);

        MediaItem output = MAPPER.readValue(new java.io.StringReader(json), MediaItem.class);
        assertNotNull(output);

        assertNotNull(output.getImages());
        assertEquals(input.getImages().size(), output.getImages().size());
        assertNotNull(output.getContent());
        assertEquals(input.getContent().getTitle(), output.getContent().getTitle());
        assertEquals(input.getContent().getUri(), output.getContent().getUri());

        
        assertEquals(json, MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(output));
    }

// com.fasterxml.jackson.databind.TestFormatSchema::testFormatForParsers
    public void testFormatForParsers() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper(new FactoryWithSchema());
        MySchema s = new MySchema();
        StringReader r = new StringReader("{}");
        
        try {
            mapper.reader(s).forType(Object.class).readValue(r);
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

// com.fasterxml.jackson.databind.TestJDKSerialization::testEnumHandlers
    public void testEnumHandlers() throws IOException
    {
        ObjectMapper mapper = new ObjectMapper();
        
        String json = mapper.writerFor(EnumPOJO.class)
                .writeValueAsString(new EnumPOJO());
        EnumPOJO result = mapper.readerFor(EnumPOJO.class)
                .readValue(json);
        assertNotNull(result);

        
        byte[] bytes = jdkSerialize(mapper);
        ObjectMapper mapper2 = jdkDeserialize(bytes);
        assertNotNull(mapper2);

        bytes = jdkSerialize(mapper.readerFor(EnumPOJO.class));
        ObjectReader r = jdkDeserialize(bytes);
        assertNotNull(r);

        
        bytes = jdkSerialize(mapper.writerFor(EnumPOJO.class));
        ObjectWriter w = jdkDeserialize(bytes);
        assertNotNull(w);

        
        String json2 = w.writeValueAsString(new EnumPOJO());
        assertEquals(json, json2);
        EnumPOJO result2 = r.readValue(json2);
        assertNotNull(result2);
    }

// com.fasterxml.jackson.databind.TestJDKSerialization::testObjectWriter
    public void testObjectWriter() throws IOException
    {
        ObjectWriter origWriter = MAPPER.writer();
        final String EXP_JSON = "{\"x\":2,\"y\":3}";
        final MyPojo p = new MyPojo(2, 3);
        assertEquals(EXP_JSON, origWriter.writeValueAsString(p));
        String json = origWriter.writeValueAsString(new AnyBean()
                .addEntry("a", "b"));
        assertNotNull(json);
        byte[] bytes = jdkSerialize(origWriter);
        ObjectWriter writer2 = jdkDeserialize(bytes);
        assertEquals(EXP_JSON, writer2.writeValueAsString(p));
    }

// com.fasterxml.jackson.databind.TestJDKSerialization::testObjectReader
    public void testObjectReader() throws IOException
    {
        ObjectReader origReader = MAPPER.readerFor(MyPojo.class);
        String JSON = "{\"x\":1,\"y\":2}";
        MyPojo p1 = origReader.readValue(JSON);
        assertEquals(2, p1.y);
        ObjectReader anyReader = MAPPER.readerFor(AnyBean.class);
        AnyBean any = anyReader.readValue(JSON);
        assertEquals(Integer.valueOf(2), any.properties().get("y"));
        
        byte[] readerBytes = jdkSerialize(origReader);
        ObjectReader reader2 = jdkDeserialize(readerBytes);
        MyPojo p2 = reader2.readValue(JSON);
        assertEquals(2, p2.y);

        ObjectReader anyReader2 = jdkDeserialize(jdkSerialize(anyReader));
        AnyBean any2 = anyReader2.readValue(JSON);
        assertEquals(Integer.valueOf(2), any2.properties().get("y"));
    }

// com.fasterxml.jackson.databind.TestJDKSerialization::testObjectMapper
    public void testObjectMapper() throws IOException
    {
        final String EXP_JSON = "{\"x\":2,\"y\":3}";
        final MyPojo p = new MyPojo(2, 3);
        assertEquals(EXP_JSON, MAPPER.writeValueAsString(p));
        assertNotNull(MAPPER.getFactory());
        assertNotNull(MAPPER.getFactory().getCodec());

        byte[] bytes = jdkSerialize(MAPPER);
        ObjectMapper mapper2 = jdkDeserialize(bytes);
        assertEquals(EXP_JSON, mapper2.writeValueAsString(p));
        MyPojo p2 = mapper2.readValue(EXP_JSON, MyPojo.class);
        assertEquals(p.x, p2.x);
        assertEquals(p.y, p2.y);

        
        assertNotNull(mapper2.getFactory());
        assertNotNull(mapper2.getFactory().getCodec());
    }

// com.fasterxml.jackson.databind.TestJDKSerialization::testTypeFactory
    public void testTypeFactory() throws Exception
    {
        TypeFactory orig = TypeFactory.defaultInstance();
        JavaType t = orig.constructType(JavaType.class);
        assertNotNull(t);

        byte[] bytes = jdkSerialize(orig);
        TypeFactory result = jdkDeserialize(bytes);
        assertNotNull(result);
        t = orig.constructType(JavaType.class);
        assertEquals(JavaType.class, t.getRawClass());
    }

// com.fasterxml.jackson.databind.TestJDKSerialization::testLRUMap
    public void testLRUMap() throws Exception
    {
        LRUMap<String,Integer> map = new LRUMap<String,Integer>(32, 32);
        map.put("a", 1);

        byte[] bytes = jdkSerialize(map);
        LRUMap<String,Integer> result = jdkDeserialize(bytes);
        
        assertEquals(0, result.size());

        
        result.put("a", 2);
        assertEquals(1, result.size());
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
        Bean bean = mapper.readerFor(Bean.class).readValue(json);
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
            result = mapper.readerFor(Bean.class).with(DeserializationFeature.UNWRAP_ROOT_VALUE)
                .readValue(jsonUnwrapped);
            fail("Should have failed");
        } catch (JsonMappingException e) {
            verifyException(e, "Root name 'a'");
        }
        
        result = mapper.readerFor(Bean.class).with(DeserializationFeature.UNWRAP_ROOT_VALUE)
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

        ObjectReader reader = mapper.readerFor(Bean.class).withRootName("wrapper");
        Bean bean = reader.readValue(json);
        assertNotNull(bean);

        
        ObjectMapper wrapping = rootMapper();
        json = wrapping.writer().withRootName("something").writeValueAsString(new Bean());
        assertEquals("{\"something\":{\"a\":3}}", json);
        json = wrapping.writer().withRootName("").writeValueAsString(new Bean());
        assertEquals("{\"a\":3}", json);

        
        json = wrapping.writer().withoutRootName().writeValueAsString(new Bean());
        assertEquals("{\"a\":3}", json);

        bean = wrapping.readerFor(Bean.class).withRootName("").readValue(json);
        assertNotNull(bean);
        assertEquals(3, bean.a);

        bean = wrapping.readerFor(Bean.class).withoutRootName().readValue("{\"a\":4}");
        assertNotNull(bean);
        assertEquals(4, bean.a);

        
        bean = wrapping.readerFor(Bean.class).readValue("{\"rudy\":{\"a\":7}}");
        assertNotNull(bean);
        assertEquals(7, bean.a);
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

// com.fasterxml.jackson.databind.access.TestAnyGetterAccess::testDynaBean
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

// com.fasterxml.jackson.databind.access.TestAnyGetterAccess::testPrivate
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

// com.fasterxml.jackson.databind.cfg.ConfigObjectsTest::testSubtypeResolver
    public void testSubtypeResolver() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        SubtypeResolver res = mapper.getSubtypeResolver();
        assertTrue(res instanceof StdSubtypeResolver);

        StdSubtypeResolver repl = new StdSubtypeResolver();
        repl.registerSubtypes(Sub.class);
        mapper.setSubtypeResolver(repl);
        assertSame(repl, mapper.getSubtypeResolver());
    }

// com.fasterxml.jackson.databind.cfg.ConfigObjectsTest::testMics
    public void testMics() throws Exception
    {
        assertFalse(MapperFeature.AUTO_DETECT_FIELDS.enabledIn(0));
        assertTrue(MapperFeature.AUTO_DETECT_FIELDS.enabledIn(-1));
    }

// com.fasterxml.jackson.databind.cfg.DatabindContextTest::testDeserializationContext
    public void testDeserializationContext() throws Exception
    {
        DeserializationContext ctxt = MAPPER.getDeserializationContext();
        
        assertNull(ctxt.constructType((Class<?>) null));
        assertNull(ctxt.constructType((java.lang.reflect.Type) null));
    }

// com.fasterxml.jackson.databind.cfg.DatabindContextTest::testSerializationContext
    public void testSerializationContext() throws Exception
    {
        SerializerProvider ctxt = MAPPER.getSerializerProvider();
        assertNull(ctxt.constructType(null));
    }

// com.fasterxml.jackson.databind.cfg.DeserializationConfigTest::testFeatureDefaults
    public void testFeatureDefaults()
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

// com.fasterxml.jackson.databind.cfg.DeserializationConfigTest::testBasicFeatures
    public void testBasicFeatures() throws Exception
    {
        DeserializationConfig config = MAPPER.getDeserializationConfig();
        assertTrue(config.hasDeserializationFeatures(DeserializationFeature.EAGER_DESERIALIZER_FETCH.getMask()));
        assertFalse(config.hasDeserializationFeatures(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY.getMask()));
        assertTrue(config.hasSomeOfFeatures(DeserializationFeature.EAGER_DESERIALIZER_FETCH.getMask()
                + DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY.getMask()));
        assertFalse(config.hasSomeOfFeatures(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY.getMask()));

        
        assertSame(config, config.without());
        assertSame(config, config.with());
        assertSame(config, config.with(MAPPER.getSubtypeResolver()));

        
        DeserializationConfig newConfig = config.with(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true);
        assertNotSame(config, newConfig);
        config = newConfig;
        
        
        assertSame(config, config.with(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES));
        assertNotSame(config, config.with(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, false));

        assertNotSame(config, config.with(DeserializationFeature.ACCEPT_EMPTY_ARRAY_AS_NULL_OBJECT,
                DeserializationFeature.FAIL_ON_MISSING_CREATOR_PROPERTIES));
    }

// com.fasterxml.jackson.databind.cfg.DeserializationConfigTest::testParserFeatures
    public void testParserFeatures() throws Exception
    {
        DeserializationConfig config = MAPPER.getDeserializationConfig();
        assertNotSame(config, config.with(JsonReadFeature.ALLOW_JAVA_COMMENTS));
        assertNotSame(config, config.withFeatures(JsonReadFeature.ALLOW_JAVA_COMMENTS,
                JsonReadFeature.ALLOW_MISSING_VALUES));

        assertNotSame(config, config.without(JsonReadFeature.ALLOW_JAVA_COMMENTS));
        assertNotSame(config, config.withoutFeatures(JsonReadFeature.ALLOW_JAVA_COMMENTS,
                JsonReadFeature.ALLOW_MISSING_VALUES));
    }

// com.fasterxml.jackson.databind.cfg.DeserializationConfigTest::testFormatFeatures
    public void testFormatFeatures() throws Exception
    {
        DeserializationConfig config = MAPPER.getDeserializationConfig();
        assertNotSame(config, config.with(BogusFormatFeature.FF_DISABLED_BY_DEFAULT));
        assertNotSame(config, config.withFeatures(BogusFormatFeature.FF_DISABLED_BY_DEFAULT,
                BogusFormatFeature.FF_ENABLED_BY_DEFAULT));
        assertNotSame(config, config.without(BogusFormatFeature.FF_ENABLED_BY_DEFAULT));
        assertNotSame(config, config.withoutFeatures(BogusFormatFeature.FF_DISABLED_BY_DEFAULT,
                BogusFormatFeature.FF_ENABLED_BY_DEFAULT));
    }

// com.fasterxml.jackson.databind.cfg.DeserializationConfigTest::testEnumIndexes
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

// com.fasterxml.jackson.databind.cfg.DeserializationConfigTest::testOverrideIntrospectors
    public void testOverrideIntrospectors()
    {
        ObjectMapper m = new ObjectMapper();
        DeserializationConfig cfg = m.getDeserializationConfig();
        
        cfg = cfg.with((ClassIntrospector) null); 
        cfg = cfg.with((AnnotationIntrospector) null);
        assertNull(cfg.getAnnotationIntrospector());
    }

// com.fasterxml.jackson.databind.cfg.DeserializationConfigTest::testMisc
    public void testMisc() throws Exception
    {
        DeserializationConfig config = MAPPER.getDeserializationConfig();
        assertEquals(JsonInclude.Value.empty(), config.getDefaultPropertyInclusion());
        assertEquals(JsonInclude.Value.empty(), config.getDefaultPropertyInclusion(String.class));

        assertSame(config, config.withRootName((PropertyName) null)); 

        DeserializationConfig newConfig = config.withRootName(PropertyName.construct("foobar"));
        assertNotSame(config, newConfig);
        config = newConfig;
        assertSame(config, config.withRootName(PropertyName.construct("foobar")));

        assertSame(config, config.with(config.getAttributes()));
        assertNotSame(config, config.with(new ContextAttributes.Impl(Collections.singletonMap("a", "b"))));

        
        assertNotNull(config.introspectDirectClassAnnotations(getClass()));
    }

// com.fasterxml.jackson.databind.cfg.SerConfigTest::testSerConfig
    public void testSerConfig() throws Exception
    {
        SerializationConfig config = MAPPER.getSerializationConfig();
        assertTrue(config.hasSerializationFeatures(SerializationFeature.FAIL_ON_EMPTY_BEANS.getMask()));
        assertFalse(config.hasSerializationFeatures(SerializationFeature.CLOSE_CLOSEABLE.getMask()));
        assertEquals(JsonInclude.Value.empty(), config.getDefaultPropertyInclusion());
        assertEquals(JsonInclude.Value.empty(), config.getDefaultPropertyInclusion(String.class));
        assertFalse(config.useRootWrapping());

        
        assertSame(config, config.without());
        assertSame(config, config.with());
        assertSame(config, config.with(MAPPER.getSubtypeResolver()));

        
        SerializationConfig newConfig = config.with(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true);
        assertNotSame(config, newConfig);
        config = newConfig;
        assertSame(config, config.with(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES));
        assertNotSame(config, config.with(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, false));

        assertNotSame(config, config.with(SerializationFeature.INDENT_OUTPUT,
                SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS));
        
        assertSame(config, config.withRootName((PropertyName) null)); 

        newConfig = config.withRootName(PropertyName.construct("foobar"));
        assertNotSame(config, newConfig);
        assertTrue(newConfig.useRootWrapping());

        assertSame(config, config.with(config.getAttributes()));
        assertNotSame(config, config.with(new ContextAttributes.Impl(Collections.singletonMap("a", "b"))));

        assertNotNull(config.introspectDirectClassAnnotations(getClass()));
    }

// com.fasterxml.jackson.databind.cfg.SerConfigTest::testGeneratorFeatures
    public void testGeneratorFeatures() throws Exception
    {
        SerializationConfig config = MAPPER.getSerializationConfig();
        assertNotSame(config, config.with(JsonWriteFeature.ESCAPE_NON_ASCII));
        SerializationConfig newConfig = config.withFeatures(JsonGenerator.Feature.IGNORE_UNKNOWN);
        assertNotSame(config, newConfig);

        assertNotSame(config, config.without(JsonWriteFeature.ESCAPE_NON_ASCII));
        assertNotSame(config, config.withoutFeatures(JsonGenerator.Feature.IGNORE_UNKNOWN));
    }

// com.fasterxml.jackson.databind.cfg.SerConfigTest::testFormatFeatures
    public void testFormatFeatures() throws Exception
    {
        SerializationConfig config = MAPPER.getSerializationConfig();
        assertNotSame(config, config.with(BogusFormatFeature.FF_DISABLED_BY_DEFAULT));
        assertNotSame(config, config.withFeatures(BogusFormatFeature.FF_DISABLED_BY_DEFAULT,
                BogusFormatFeature.FF_ENABLED_BY_DEFAULT));
        assertNotSame(config, config.without(BogusFormatFeature.FF_ENABLED_BY_DEFAULT));
        assertNotSame(config, config.withoutFeatures(BogusFormatFeature.FF_DISABLED_BY_DEFAULT,
                BogusFormatFeature.FF_ENABLED_BY_DEFAULT));
    }

// com.fasterxml.jackson.databind.contextual.TestContextAttributeWithDeser::testSimplePerCall
    public void testSimplePerCall() throws Exception
    {
        final String INPUT = aposToQuotes("[{'value':'a'},{'value':'b'}]");
        TestPOJO[] pojos = MAPPER.readerFor(TestPOJO[].class).readValue(INPUT);
        assertEquals(2, pojos.length);
        assertEquals("a/0", pojos[0].value);
        assertEquals("b/1", pojos[1].value);

        
        TestPOJO[] pojos2 = MAPPER.readerFor(TestPOJO[].class).readValue(INPUT);
        assertEquals(2, pojos2.length);
        assertEquals("a/0", pojos2[0].value);
        assertEquals("b/1", pojos2[1].value);
    }

// com.fasterxml.jackson.databind.contextual.TestContextAttributeWithDeser::testSimpleDefaults
    public void testSimpleDefaults() throws Exception
    {
        final String INPUT = aposToQuotes("{'value':'x'}");
        TestPOJO pojo = MAPPER.readerFor(TestPOJO.class)
                .withAttribute(KEY, Integer.valueOf(3))
                .readValue(INPUT);
        assertEquals("x/3", pojo.value);

        
        TestPOJO pojo2 = MAPPER.readerFor(TestPOJO.class)
                .withAttribute(KEY, Integer.valueOf(5))
                .readValue(INPUT);
        assertEquals("x/5", pojo2.value);
    }

// com.fasterxml.jackson.databind.contextual.TestContextAttributeWithDeser::testHierarchic
    public void testHierarchic() throws Exception
    {
        final String INPUT = aposToQuotes("[{'value':'x'},{'value':'y'}]");
        ObjectReader r = MAPPER.readerFor(TestPOJO[].class).withAttribute(KEY, Integer.valueOf(2));
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

// com.fasterxml.jackson.databind.contextual.TestContextualDeserialization::testContextualType
    public void testContextualType() throws Exception {
        GenericBean bean = new ObjectMapper().readValue(aposToQuotes("{'stuff':{'1':'b'}}"),
                GenericBean.class);
        assertNotNull(bean.stuff);
        assertEquals(1, bean.stuff.size());
        assertEquals("String", bean.stuff.get(Integer.valueOf(1)));
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
        String json = mapper.writerFor(TypeFactory.defaultInstance().constructMapType(HashMap.class, String.class, Object.class))
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

// com.fasterxml.jackson.databind.convert.ConvertingAbstractSerializer795Test::testAbstractTypeDeserialization
    public void testAbstractTypeDeserialization() throws Exception {
        String test="{\"customField\": \"customString\"}";
        AbstractCustomTypeUser cu = JSON_MAPPER.readValue(test, AbstractCustomTypeUser.class);
        assertNotNull(cu);
    }

// com.fasterxml.jackson.databind.convert.ConvertingAbstractSerializer795Test::testNonAbstractDeserialization
    public void testNonAbstractDeserialization() throws Exception {
        String test="{\"customField\": \"customString\"}";
        NonAbstractCustomTypeUser cu = JSON_MAPPER.readValue(test, NonAbstractCustomTypeUser.class);
        assertNotNull(cu);
    }

// com.fasterxml.jackson.databind.convert.NumericConversionTest::testDoubleToInt
    public void testDoubleToInt() throws Exception
    {
        
        Integer I = MAPPER.readValue(" 1.25 ", Integer.class);
        assertEquals(1, I.intValue());
        IntWrapper w = MAPPER.readValue("{\"i\":-2.25 }", IntWrapper.class);
        assertEquals(-2, w.i);
        int[] arr = MAPPER.readValue("[ 1.25 ]", int[].class);
        assertEquals(1, arr[0]);

        try {
            R.forType(Integer.class).readValue("1.5");
            fail("Should not pass");
        } catch (JsonMappingException e) {
            verifyException(e, "Cannot coerce a floating-point");
        }
        try {
            R.forType(Integer.TYPE).readValue("1.5");
            fail("Should not pass");
        } catch (JsonMappingException e) {
            verifyException(e, "Cannot coerce a floating-point");
        }
        try {
            R.forType(IntWrapper.class).readValue("{\"i\":-2.25 }");
            fail("Should not pass");
        } catch (JsonMappingException e) {
            verifyException(e, "Cannot coerce a floating-point");
        }
        try {
            R.forType(int[].class).readValue("[ 2.5 ]");
            fail("Should not pass");
        } catch (JsonMappingException e) {
            verifyException(e, "Cannot coerce a floating-point");
        }
    }

// com.fasterxml.jackson.databind.convert.NumericConversionTest::testDoubleToLong
    public void testDoubleToLong() throws Exception
    {
        
        Long L = MAPPER.readValue(" 3.33 ", Long.class);
        assertEquals(3L, L.longValue());
        LongWrapper w = MAPPER.readValue("{\"l\":-2.25 }", LongWrapper.class);
        assertEquals(-2L, w.l);
        long[] arr = MAPPER.readValue("[ 1.25 ]", long[].class);
        assertEquals(1, arr[0]);

        try {
            R.forType(Long.class).readValue("1.5");
            fail("Should not pass");
        } catch (MismatchedInputException e) {
            verifyException(e, "Cannot coerce a floating-point");
        }

        try {
            R.forType(Long.TYPE).readValue("1.5");
            fail("Should not pass");
        } catch (MismatchedInputException e) {
            verifyException(e, "Cannot coerce a floating-point");
        }
        
        try {
            R.forType(LongWrapper.class).readValue("{\"l\": 7.7 }");
            fail("Should not pass");
        } catch (MismatchedInputException e) {
            verifyException(e, "Cannot coerce a floating-point");
        }
        try {
            R.forType(long[].class).readValue("[ 2.5 ]");
            fail("Should not pass");
        } catch (MismatchedInputException e) {
            verifyException(e, "Cannot coerce a floating-point");
        }
    }

// com.fasterxml.jackson.databind.convert.ScalarConversionTest::testConvertValueNullPrimitive
    public void testConvertValueNullPrimitive() throws Exception
    {
        assertEquals(Byte.valueOf((byte) 0), MAPPER.convertValue(null, Byte.TYPE));
        assertEquals(Short.valueOf((short) 0), MAPPER.convertValue(null, Short.TYPE));
        assertEquals(Integer.valueOf(0), MAPPER.convertValue(null, Integer.TYPE));
        assertEquals(Long.valueOf(0L), MAPPER.convertValue(null, Long.TYPE));
        assertEquals(Float.valueOf(0f), MAPPER.convertValue(null, Float.TYPE));
        assertEquals(Double.valueOf(0d), MAPPER.convertValue(null, Double.TYPE));
        assertEquals(Character.valueOf('\0'), MAPPER.convertValue(null, Character.TYPE));
        assertEquals(Boolean.FALSE, MAPPER.convertValue(null, Boolean.TYPE));
    }

// com.fasterxml.jackson.databind.convert.ScalarConversionTest::testConvertValueNullBoxed
    public void testConvertValueNullBoxed() throws Exception
    {
        assertNull(MAPPER.convertValue(null, Byte.class));
        assertNull(MAPPER.convertValue(null, Short.class));
        assertNull(MAPPER.convertValue(null, Integer.class));
        assertNull(MAPPER.convertValue(null, Long.class));
        assertNull(MAPPER.convertValue(null, Float.class));
        assertNull(MAPPER.convertValue(null, Double.class));
        assertNull(MAPPER.convertValue(null, Character.class));
        assertNull(MAPPER.convertValue(null, Boolean.class));
    }

// com.fasterxml.jackson.databind.convert.TestArrayConversions::testNullXform
    public void testNullXform() throws Exception
    {
        
        assertNull(MAPPER.convertValue(null, Integer.class));
        assertNull(MAPPER.convertValue(null, String.class));
        assertNull(MAPPER.convertValue(null, byte[].class));
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
        
        List<Integer> actNums = MAPPER.convertValue(data, new TypeReference<List<Integer>>() {});
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
        List<Long> actNums = MAPPER.convertValue(data, new TypeReference<List<Long>>() {});
        assertEquals(expNums, actNums);        
    }

// com.fasterxml.jackson.databind.convert.TestArrayConversions::testOverflows
    public void testOverflows()
    {
        
        try {
            MAPPER.convertValue(new int[] { 1000 }, byte[].class);
        } catch (IllegalArgumentException e) {
            verifyException(e, OVERFLOW_MSG_BYTE);
        }
        
        try {
            MAPPER.convertValue(new int[] { -99999 }, short[].class);
        } catch (IllegalArgumentException e) {
            verifyException(e, OVERFLOW_MSG);
        }
        
        try {
            MAPPER.convertValue(new long[] { Long.MAX_VALUE }, int[].class);
        } catch (IllegalArgumentException e) {
            verifyException(e, OVERFLOW_MSG_INT);
        }
        
        BigInteger biggie = BigInteger.valueOf(Long.MAX_VALUE);
        biggie.add(BigInteger.ONE);
        List<BigInteger> l = new ArrayList<BigInteger>();
        l.add(biggie);
        try {
            MAPPER.convertValue(l, long[].class);
        } catch (IllegalArgumentException e) {
            verifyException(e, OVERFLOW_MSG_LONG);
        }
    }

// com.fasterxml.jackson.databind.convert.TestBeanConversions::testBeanConvert
    public void testBeanConvert()
    {
        
        PointStrings input = new PointStrings("37", "-9");
        PointZ point = MAPPER.convertValue(input, PointZ.class);
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
            verifyException(e, "Cannot deserialize value of type `boolean` from String");
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
        
        StringBuilder SB = new StringBuilder("test");
        CharSequence seq = MAPPER.convertValue(SB, CharSequence.class);
        assertNotSame(SB, seq);

        
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

// com.fasterxml.jackson.databind.convert.TestBeanConversions::testConversionIssue1433
    public void testConversionIssue1433() throws Exception
    {
        assertNull(MAPPER.convertValue(null, Object.class));
        assertNull(MAPPER.convertValue(null, PointZ.class));
        
        assertSame(NullBean.NULL_INSTANCE,
                MAPPER.convertValue(null, NullBean.class));
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

// com.fasterxml.jackson.databind.convert.TestConvertingDeserializer::testConvertToAbstract
    public void testConvertToAbstract() throws Exception
    {
        Issue795Bean bean = objectReader(Issue795Bean.class)
                .readValue("{\"value\":\"1.25\"}");
        assertNotNull(bean.value);
        assertTrue("Type not BigDecimal but "+bean.value.getClass(),
                bean.value instanceof BigDecimal);
        assertEquals(new BigDecimal("1.25"), bean.value);
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

// com.fasterxml.jackson.databind.convert.TestConvertingSerializer::testConverterForList357
    public void testConverterForList357() throws Exception {
        String json = objectWriter().writeValueAsString(new ListWrapper());
        assertEquals("{\"list\":[[\"Hello world!\"]]}", json);
    }

// com.fasterxml.jackson.databind.convert.TestConvertingSerializer::testIssue359
    public void testIssue359() throws Exception {
        String json = objectWriter().writeValueAsString(new Bean359());
        assertEquals("{\"stuff\":[\"Target\"]}", json);
    }

// com.fasterxml.jackson.databind.convert.TestConvertingSerializer::testIssue731
    public void testIssue731() throws Exception
    {
        String json = objectWriter().writeValueAsString(new ConvertingBeanWithUntypedConverter(1, 2));
        
        assertEquals("{\"a\":2,\"b\":4}", json);
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

// com.fasterxml.jackson.databind.convert.TestStringConversions::testLowerCasingSerializer
    public void testLowerCasingSerializer() throws Exception
    {
        assertEquals("{\"value\":\"abc\"}", MAPPER.writeValueAsString(new StringWrapperWithConvert("ABC")));
    }

// com.fasterxml.jackson.databind.convert.TestStringConversions::testLowerCasingDeserializer
    public void testLowerCasingDeserializer() throws Exception
    {
        StringWrapperWithConvert value = MAPPER.readValue("{\"value\":\"XyZ\"}", StringWrapperWithConvert.class);
        assertNotNull(value);
        assertEquals("xyz", value.value);
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

// com.fasterxml.jackson.databind.deser.PropertyAliasTest::testAliasWithPolymorphic
    public void testAliasWithPolymorphic() throws Exception
    {
        PolyWrapperForAlias value = MAPPER.readValue(aposToQuotes(
                "{'value': ['ab', {'nm' : 'Bob', 'A' : 17} ] }"
                ), PolyWrapperForAlias.class);
        assertNotNull(value.value);
        AliasBean bean = (AliasBean) value.value;
        assertEquals("Bob", bean.name);
        assertEquals(17, bean._a);
    }

// com.fasterxml.jackson.databind.deser.ReadOnlyDeser1805Test::testReadOnly1382
    public void testReadOnly1382() throws Exception
    {
        String payload = "{\"list\":[1,2,3,4]}";
        Foo foo = MAPPER.readValue(payload, Foo.class);
        assertTrue("List should be empty", foo.getList().isEmpty());
    }

// com.fasterxml.jackson.databind.deser.ReadOnlyDeser1805Test::testViaReadOnly
    public void testViaReadOnly() throws Exception {
        UserWithReadOnly user = new UserWithReadOnly();
        user.name = "foo";
        String json = MAPPER.writeValueAsString(user);
        UserWithReadOnly result = MAPPER.readValue(json, UserWithReadOnly.class);
        assertNotNull(result);
    }

// com.fasterxml.jackson.databind.deser.ReadOnlyDeser1805Test::testUsingAllowGetters
    public void testUsingAllowGetters() throws Exception {
        UserAllowGetters user = new UserAllowGetters();
        user.name = "foo";
        String json = MAPPER.writeValueAsString(user);
        assertTrue(json.contains("roles"));
        UserAllowGetters result = MAPPER.readValue(json, UserAllowGetters.class);
        assertNotNull(result);
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

        ObjectMapper m = objectMapperBuilder()
                .configure(MapperFeature.USE_ANNOTATIONS, false)
                .build();
        
        bean = m.readValue("{ \"x\" : 0 }", AnnoBean.class);
        assertEquals(0, bean.value);
    }

// com.fasterxml.jackson.databind.deser.TestBasicAnnotations::testEnumsWhenDisabled
    public void testEnumsWhenDisabled() throws Exception
    {
        ObjectMapper m = new ObjectMapper();
        assertEquals(Alpha.B, m.readValue(quote("B"), Alpha.class));

        m = objectMapperBuilder()
                .configure(MapperFeature.USE_ANNOTATIONS, false)
                .build();
        
        assertEquals(Alpha.B, m.readValue(quote("B"), Alpha.class));
    }

// com.fasterxml.jackson.databind.deser.TestBasicAnnotations::testNoAccessOverrides
    public void testNoAccessOverrides() throws Exception
    {
        ObjectMapper m = objectMapperBuilder()
                .disable(MapperFeature.CAN_OVERRIDE_ACCESS_MODIFIERS)
                .build();
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
            verifyException(e, "cannot construct");
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

// com.fasterxml.jackson.databind.deser.TestBeanDeserializer::testAddOrReplacePropertyIsUsedOnDeserialization
    public void testAddOrReplacePropertyIsUsedOnDeserialization() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new Issue1912Module());

        Issue1912Bean result = mapper.readValue("{\"subBean\": {\"a\":\"foo\"}}", Issue1912Bean.class);
        assertEquals("foo_custom", result.subBean.a);
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

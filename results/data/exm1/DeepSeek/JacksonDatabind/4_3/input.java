// buggy code
    public String[] deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException
    {
        // Ok: must point to START_ARRAY (or equivalent)
        if (!jp.isExpectedStartArrayToken()) {
            return handleNonArray(jp, ctxt);
        }
        if (_elementDeserializer != null) {
            return _deserializeCustom(jp, ctxt);
        }

        final ObjectBuffer buffer = ctxt.leaseObjectBuffer();
        Object[] chunk = buffer.resetAndStart();

        int ix = 0;
        JsonToken t;

            while ((t = jp.nextToken()) != JsonToken.END_ARRAY) {
                // Ok: no need to convert Strings, but must recognize nulls
                String value;
                if (t == JsonToken.VALUE_STRING) {
                    value = jp.getText();
                } else if (t == JsonToken.VALUE_NULL) {
                    value = null; // since we have established that '_elementDeserializer == null' earlier
                } else {
                    value = _parseString(jp, ctxt);
                }
                if (ix >= chunk.length) {
                    chunk = buffer.appendCompletedChunk(chunk);
                    ix = 0;
                }
                chunk[ix++] = value;
            }
            // note: pass String.class, not String[].class, as we need element type for error info
        String[] result = buffer.completeAndClearBuffer(chunk, ix, String.class);
        ctxt.returnObjectBuffer(buffer);
        return result;
    }

    protected final String[] _deserializeCustom(JsonParser jp, DeserializationContext ctxt) throws IOException
    {
        final ObjectBuffer buffer = ctxt.leaseObjectBuffer();
        Object[] chunk = buffer.resetAndStart();
        final JsonDeserializer<String> deser = _elementDeserializer;
        
        int ix = 0;
        JsonToken t;

            while ((t = jp.nextToken()) != JsonToken.END_ARRAY) {
                // Ok: no need to convert Strings, but must recognize nulls
                String value = (t == JsonToken.VALUE_NULL) ? deser.getNullValue() : deser.deserialize(jp, ctxt);
                if (ix >= chunk.length) {
                    chunk = buffer.appendCompletedChunk(chunk);
                    ix = 0;
                }
                chunk[ix++] = value;
            }
            // note: pass String.class, not String[].class, as we need element type for error info
        String[] result = buffer.completeAndClearBuffer(chunk, ix, String.class);
        ctxt.returnObjectBuffer(buffer);
        return result;
    }

// relevant test
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
        objectMapper.addMixInAnnotations(Element.class, ElementMixin.class);
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
        ObjectMapper mapper = new ObjectMapper();
        assertEquals(quote("foo\\u0062\\Ar"),
                mapper.writer(new CustomEscapes()).writeValueAsString("foobar"));
    }

// com.fasterxml.jackson.databind.ser.TestCustomSerializers::testNumberSubclass
    public void testNumberSubclass() throws Exception
    {
        assertEquals(aposToQuotes("{'x':42}"),
                MAPPER.writeValueAsString(new LikeNumber(42)));
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
        m2.addMixInAnnotations(Empty.class, EmptyWithAnno.class);
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
        m.addMixInAnnotations(TestEnum.class, ToStringMixin.class);
        assertEquals("\"b\"", m.writeValueAsString(TestEnum.B));
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
        try {
            String json = mapper.writeValueAsString(BrokenPoNum.A);
            fail("Should not have succeeded, produced: "+json);
        } catch (JsonMappingException e) {
            verifyException(e, "Unsupported serialization shape");
        }
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
        m.writerWithType(CloseableBean.class).writeValueAsString(bean);
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
        MAPPER.enable(SerializationFeature.WRITE_BIGDECIMAL_AS_PLAIN);
        Map<String, Object> map = new HashMap<String, Object>();
        String PI_STR = "3.00000000";
        map.put("pi", new BigDecimal(PI_STR));
        String str = MAPPER.writeValueAsString(map);
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

// com.fasterxml.jackson.databind.ser.TestMapSerialization::testUsingObjectWriter
    public void testUsingObjectWriter() throws IOException
    {
        ObjectWriter w = MAPPER.writerWithType(Object.class);
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

// com.fasterxml.jackson.databind.ser.TestObjectWriter::testPrettyPrinter
    public void testPrettyPrinter() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        ObjectWriter writer = mapper.writer();
        HashMap<String, Integer> data = new HashMap<String,Integer>();
        data.put("a", 1);
        
        
        assertEquals("{\"a\":1}", writer.writeValueAsString(data));

        
        writer = writer.withDefaultPrettyPrinter();

        
        String lf = System.getProperty("line.separator");
        assertEquals("{" + lf + "  \"a\" : 1" + lf + "}", writer.writeValueAsString(data));

        
        writer = writer.with((PrettyPrinter) null);
        assertEquals("{\"a\":1}", writer.writeValueAsString(data));
    }

// com.fasterxml.jackson.databind.ser.TestObjectWriter::testPrefetch
    public void testPrefetch() throws Exception
    {
        ObjectWriter writer = objectWriter();
        assertFalse(writer.hasPrefetchedSerializer());
        writer = objectWriter().withType(String.class);
        assertTrue(writer.hasPrefetchedSerializer());
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

        
        ObjectWriter w = mapper.writerWithType(BaseType.class);
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

        
        ObjectWriter w = mapper.writerWithType(BaseInterface.class);
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
        String json = mapper.writerWithType(BaseInterface[].class).writeValueAsString(ob);
        
        assertEquals("[{\"b\":3}]", json);
    }

// com.fasterxml.jackson.databind.ser.TestRootType::testIncompatibleRootType
    public void testIncompatibleRootType() throws Exception
    {
        ObjectMapper mapper = objectMapper();
        SubType bean = new SubType();

        
        ObjectWriter w = mapper.writerWithType(HashMap.class);
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
        
        
        String json = mapper.writerWithType(collectionType).writeValueAsString(typedList);
        assertEquals(EXP, json);

        StringWriter out = new StringWriter();
        JsonFactory f = new JsonFactory();
        mapper.writerWithType(collectionType).writeValue(f.createGenerator(out), typedList);

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
        assertEquals("123", mapper.writerWithType(Integer.TYPE).writeValueAsString(Integer.valueOf(123)));
        assertEquals("456", mapper.writerWithType(Long.TYPE).writeValueAsString(Long.valueOf(456L)));
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

        ObjectWriter writer = WRAP_ROOT_MAPPER.writerWithType(TestCommandParent.class);
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
        m.addMixInAnnotations(BeanWithOrder.class, OrderMixIn.class);
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
        String singleJson = mapper.writerWithType(Issue822Interface.class).writeValueAsString(new Issue822Impl());
        
        assertEquals("{\"a\":3}", singleJson);
    }

// com.fasterxml.jackson.databind.ser.TestTypedRootValueSerialization::testTypedArrays
    public void testTypedArrays() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();

        assertEquals("[{\"a\":3}]", mapper.writerWithType(Issue822Interface[].class).writeValueAsString(
                new Issue822Interface[] { new Issue822Impl() }));
    }

// com.fasterxml.jackson.databind.ser.TestTypedRootValueSerialization::testTypedLists
    public void testTypedLists() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
     

        List<Issue822Interface> list = new ArrayList<Issue822Interface>();
        list.add(new Issue822Impl());
        String listJson = mapper.writerWithType(new TypeReference<List<Issue822Interface>>(){})
                .writeValueAsString(list);
        assertEquals("[{\"a\":3}]", listJson);
    }

// com.fasterxml.jackson.databind.ser.TestTypedRootValueSerialization::testTypedMaps
    public void testTypedMaps() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        Map<String,Issue822Interface> map = new HashMap<String,Issue822Interface>();
        map.put("a", new Issue822Impl());
        String listJson = mapper.writerWithType(new TypeReference<Map<String,Issue822Interface>>(){})
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

// com.fasterxml.jackson.databind.struct.TestObjectIdDeserialization::testUnresolvedForwardReference
    public void testUnresolvedForwardReference()
        throws Exception
    {
        String json = "{\"employees\":[" 
                      + "{\"id\":1,\"name\":\"First\",\"manager\":null,\"reports\":[3]},"
                      + "{\"id\":2,\"name\":\"Second\",\"manager\":3,\"reports\":[]}" 
                      + "]}";
        try {
            mapper.readValue(json, Company.class);
            fail("Should have thrown.");
        } catch (UnresolvedForwardReference exception) {
            
            List<UnresolvedId> unresolvedIds = exception.getUnresolvedIds();
            assertEquals(2, unresolvedIds.size());
            UnresolvedId firstUnresolvedId = unresolvedIds.get(0);
            assertEquals(3, firstUnresolvedId.getId());
            assertEquals(Employee.class, firstUnresolvedId.getType());
            UnresolvedId secondUnresolvedId = unresolvedIds.get(1);
            assertEquals(firstUnresolvedId.getId(), secondUnresolvedId.getId());
            assertEquals(Employee.class, secondUnresolvedId.getType());
        }
    }

// com.fasterxml.jackson.databind.struct.TestObjectIdDeserialization::testKeepCollectionOrdering
    public void testKeepCollectionOrdering()
        throws Exception
    {
        String json = "{\"employees\":[2,1,"
                + "{\"id\":1,\"name\":\"First\",\"manager\":null,\"reports\":[2]},"
                + "{\"id\":2,\"name\":\"Second\",\"manager\":1,\"reports\":[]}"
                + "]}";
        Company company = mapper.readValue(json, Company.class);
        assertEquals(4, company.employees.size());
        
        Employee firstEmployee = company.employees.get(1);
        Employee secondEmployee = company.employees.get(0);
        assertSame(firstEmployee, company.employees.get(2));
        assertSame(secondEmployee, company.employees.get(3));
        assertEmployees(firstEmployee, secondEmployee);
    }

// com.fasterxml.jackson.databind.struct.TestObjectIdDeserialization::testKeepMapOrdering
    public void testKeepMapOrdering()
        throws Exception
    {
        String json = "{\"employees\":{"
                      + "\"1\":2, \"2\":1,"
                      + "\"3\":{\"id\":1,\"name\":\"First\",\"manager\":null,\"reports\":[2]},"
                      + "\"4\":{\"id\":2,\"name\":\"Second\",\"manager\":1,\"reports\":[]}"
                      + "}}";
        MappedCompany company = mapper.readValue(json, MappedCompany.class);
        assertEquals(4, company.employees.size());
        Employee firstEmployee = company.employees.get(2);
        Employee secondEmployee = company.employees.get(1);
        assertEmployees(firstEmployee, secondEmployee);
        
        
        Iterator<Entry<Integer,Employee>> iterator = company.employees.entrySet().iterator();
        assertSame(secondEmployee, iterator.next().getValue());
        assertSame(firstEmployee, iterator.next().getValue());
        assertSame(firstEmployee, iterator.next().getValue());
        assertSame(secondEmployee, iterator.next().getValue());
    }

// com.fasterxml.jackson.databind.struct.TestObjectIdDeserialization::testCustomDeserializationClass
    public void testCustomDeserializationClass() throws Exception
    {
        
        IdentifiableCustom result = mapper.readValue(EXP_CUSTOM_VIA_CLASS, IdentifiableCustom.class);
        assertEquals(-900, result.value);
        assertSame(result, result.next);
    }

// com.fasterxml.jackson.databind.struct.TestObjectIdDeserialization::testCustomDeserializationProperty
    public void testCustomDeserializationProperty() throws Exception
    {
        
    	IdWrapperExt result = mapper.readValue(EXP_CUSTOM_VIA_PROP, IdWrapperExt.class);
        assertEquals(99, result.node.value);
        assertSame(result.node, result.node.next.node);
        assertEquals(3, result.node.customId);
    }

// com.fasterxml.jackson.databind.struct.TestObjectIdDeserialization::testCustomPoolResolver
    public void testCustomPoolResolver()
        throws Exception
    {
        Map<Object,WithCustomResolution> pool = new HashMap<Object,WithCustomResolution>();
        pool.put(1, new WithCustomResolution(1, 1));
        pool.put(2, new WithCustomResolution(2, 2));
        pool.put(3, new WithCustomResolution(3, 3));
        pool.put(4, new WithCustomResolution(4, 4));
        pool.put(5, new WithCustomResolution(5, 5));
        ContextAttributes attrs = mapper.getDeserializationConfig().getAttributes().withSharedAttribute(POOL_KEY, pool);
        String content = "{\"data\":[1,2,3,4,5]}";
        CustomResolutionWrapper wrapper = mapper.reader(CustomResolutionWrapper.class).with(attrs).readValue(content);
        assertFalse(wrapper.data.isEmpty());
        for (WithCustomResolution ob : wrapper.data) {
            assertSame(pool.get(ob.id), ob);
        }
    }

// com.fasterxml.jackson.databind.struct.TestObjectIdSerialization::testSimpleSerializationClass
    public void testSimpleSerializationClass() throws Exception
    {
        Identifiable src = new Identifiable(13);
        src.next = src;
        
        
        String json = MAPPER.writeValueAsString(src);
        assertEquals(EXP_SIMPLE_INT_CLASS, json);

        
        json = MAPPER.writeValueAsString(src);
        assertEquals(EXP_SIMPLE_INT_CLASS, json);
    }

// com.fasterxml.jackson.databind.struct.TestObjectIdSerialization::testSimpleSerializationProperty
    public void testSimpleSerializationProperty() throws Exception
    {
        IdWrapper src = new IdWrapper(7);
        src.node.next = src;
        
        
        String json = MAPPER.writeValueAsString(src);
        assertEquals(EXP_SIMPLE_INT_PROP, json);
        
        json = MAPPER.writeValueAsString(src);
        assertEquals(EXP_SIMPLE_INT_PROP, json);
    }

// com.fasterxml.jackson.databind.struct.TestObjectIdSerialization::testEmptyObjectWithId
    public void testEmptyObjectWithId() throws Exception
    {
        final ObjectMapper mapper = new ObjectMapper();
        String json = mapper.writeValueAsString(new EmptyObject());
        assertEquals(aposToQuotes("{'@id':1}"), json);
    }

// com.fasterxml.jackson.databind.struct.TestObjectIdSerialization::testCustomPropertyForClass
    public void testCustomPropertyForClass() throws Exception
    {
        IdentifiableWithProp src = new IdentifiableWithProp(123, -19);
        src.next = src;
        
        
        String json = MAPPER.writeValueAsString(src);
        assertEquals(EXP_CUSTOM_PROP, json);

        
        json = MAPPER.writeValueAsString(src);
        assertEquals(EXP_CUSTOM_PROP, json);
    }

// com.fasterxml.jackson.databind.struct.TestObjectIdSerialization::testCustomPropertyViaProperty
    public void testCustomPropertyViaProperty() throws Exception
    {
        IdWrapperCustom src = new IdWrapperCustom(123, 7);
        src.node.next = src;
        
        
        String json = MAPPER.writeValueAsString(src);
        assertEquals(EXP_CUSTOM_PROP_VIA_REF, json);
        
        json = MAPPER.writeValueAsString(src);
        assertEquals(EXP_CUSTOM_PROP_VIA_REF, json);
    }

// com.fasterxml.jackson.databind.struct.TestObjectIdSerialization::testAlwaysAsId
    public void testAlwaysAsId() throws Exception
    {
        String json = MAPPER.writeValueAsString(new AlwaysContainer());
        assertEquals("{\"a\":1,\"b\":2}", json);
    }

// com.fasterxml.jackson.databind.struct.TestObjectIdSerialization::testAlwaysIdForTree
    public void testAlwaysIdForTree() throws Exception
    {
        TreeNode root = new TreeNode(null, 1, "root");     
        TreeNode leaf = new TreeNode(root, 2, "leaf");
        root.child = leaf;
        String json = MAPPER.writeValueAsString(root);

        assertEquals("{\"id\":1,\"name\":\"root\",\"parent\":null,\"child\":"
                +"{\"id\":2,\"name\":\"leaf\",\"parent\":1,\"child\":null}}",
                json);
        		
    }

// com.fasterxml.jackson.databind.struct.TestObjectIdSerialization::testInvalidProp
    public void testInvalidProp() throws Exception
    {
        try {
            MAPPER.writeValueAsString(new Broken());
            fail("Should have thrown an exception");
        } catch (JsonMappingException e) {
            verifyException(e, "can not find property with name 'id'");
        }
    }

// com.fasterxml.jackson.databind.struct.TestObjectIdWithEquals::testSimpleEquals
    public void testSimpleEquals() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        
        assertFalse(mapper.isEnabled(SerializationFeature.USE_EQUALITY_FOR_OBJECT_ID));
        mapper.enable(SerializationFeature.USE_EQUALITY_FOR_OBJECT_ID);

        Foo foo = new Foo(1);

        Bar bar1 = new Bar(1);
        Bar bar2 = new Bar(2);
        
        
        
        Bar anotherBar1 = new Bar(1);

        foo.bars.add(bar1);
        foo.bars.add(bar2);
        
        foo.otherBars.add(anotherBar1);
        foo.otherBars.add(bar2);

        String json = mapper.writeValueAsString(foo);
        assertEquals("{\"id\":1,\"bars\":[{\"id\":1},{\"id\":2}],\"otherBars\":[1,2]}", json);

        Foo foo2 = mapper.readValue(json, Foo.class);       
        assertNotNull(foo2);
        assertEquals(foo.id, foo2.id);
    }

// com.fasterxml.jackson.databind.struct.TestObjectIdWithPolymorphic::testPolymorphicRoundtrip
    public void testPolymorphicRoundtrip() throws Exception
    {
        
        Impl in1 = new Impl(123, 456);
        in1.next = new Impl(111, 222);
        in1.next.next = in1;
        
        String json = mapper.writeValueAsString(in1);
        
        
        Base result0 = mapper.readValue(json, Base.class);
        assertNotNull(result0);
        assertSame(Impl.class, result0.getClass());
        Impl result = (Impl) result0;
        assertEquals(123, result.value);
        assertEquals(456, result.extra);
        Impl result2 = (Impl) result.next;
        assertEquals(111, result2.value);
        assertEquals(222, result2.extra);
        assertSame(result, result2.next);
    }

// com.fasterxml.jackson.databind.struct.TestObjectIdWithPolymorphic::testIssue811
    public void testIssue811() throws Exception
    {
        ObjectMapper om = new ObjectMapper();
        om.disable(MapperFeature.AUTO_DETECT_CREATORS);
        om.disable(MapperFeature.AUTO_DETECT_GETTERS);
        om.disable(MapperFeature.AUTO_DETECT_IS_GETTERS);
        om.setVisibility(PropertyAccessor.FIELD, Visibility.ANY);
        
        om.enable(SerializationFeature.WRITE_ENUMS_USING_INDEX);
        om.enable(SerializationFeature.INDENT_OUTPUT);
        om.enableDefaultTypingAsProperty(DefaultTyping.NON_FINAL, "@class");
    
        Process p = new Process();
        Scope s = new Scope(p, null);
        FaultHandler fh = new FaultHandler(p);
        Catch c = new Catch(p, s);
        fh.catchBlocks.add(c);
        s.faultHandlers.add(fh);
        
        String json = om.writeValueAsString(p);
        Process restored = om.readValue(json, Process.class);
        assertNotNull(restored);

        assertEquals(0, p.id);
        assertEquals(3, p.children.size());
        assertSame(p, p.children.get(0).owner);
        assertSame(p, p.children.get(1).owner);
        assertSame(p, p.children.get(2).owner);
    }

// com.fasterxml.jackson.databind.struct.TestPOJOAsArray::testReadSimplePropertyValue
    public void testReadSimplePropertyValue() throws Exception
    {
        String json = "{\"value\":[true,\"Foobar\",42,13]}";
        Pojo p = MAPPER.readValue(json, Pojo.class);
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
        String json = MAPPER.writeValueAsString(new Pojo("Foobar", 42, 13, true));
        
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

// com.fasterxml.jackson.databind.struct.TestPOJOAsArray::testAnnotationOverride
    public void testAnnotationOverride() throws Exception
    {
        
        assertEquals("{\"value\":{\"x\":1,\"y\":2}}", MAPPER.writeValueAsString(new A()));

        
        ObjectMapper mapper2 = new ObjectMapper();
        mapper2.setAnnotationIntrospector(new ForceArraysIntrospector());
        assertEquals("[[1,2]]", mapper2.writeValueAsString(new A()));
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

        
        AsArrayWithView output = MAPPER.reader(AsArrayWithView.class).withView(ViewB.class)
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

// com.fasterxml.jackson.databind.type.TestAnnotatedClass::testFieldIntrospection
    public void testFieldIntrospection()
    {
        
        AnnotatedClass ac = AnnotatedClass.construct(FieldBean.class, new JacksonAnnotationIntrospector(), null);
        
        assertEquals(2, ac.getFieldCount());
        for (AnnotatedField f : ac.fields()) {
            String fname = f.getName();
            if (!"bar".equals(fname) && !"props".equals(fname)) {
                fail("Unexpected field name '"+fname+"'");
            }
        }
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

// com.fasterxml.jackson.databind.type.TestTypeFactory::testMaps
    public void testMaps()
    {
        TypeFactory tf = TypeFactory.defaultInstance();
        
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
        
        JavaType t = tf.constructParametricType(ArrayList.class, String.class); 
        assertEquals(CollectionType.class, t.getClass());
        JavaType strC = tf.constructType(String.class);
        assertEquals(1, t.containedTypeCount());
        assertEquals(strC, t.containedType(0));
        assertNull(t.containedType(1));

        
        JavaType t2 = tf.constructParametricType(Map.class, strC, t); 
        
        assertEquals(MapType.class, t2.getClass());
        assertEquals(2, t2.containedTypeCount());
        assertEquals(strC, t2.containedType(0));
        assertEquals(t, t2.containedType(1));
        assertNull(t2.containedType(2));

        
        JavaType custom = tf.constructParametricType(SingleArgGeneric.class, String.class);
        assertEquals(SimpleType.class, custom.getClass());
        assertEquals(1, custom.containedTypeCount());
        assertEquals(strC, custom.containedType(0));
        assertNull(custom.containedType(1));
        
        assertEquals("X", custom.containedTypeName(0));

        
        try {
            
            tf.constructParametricType(Map.class, strC);
        } catch (IllegalArgumentException e) {
            verifyException(e, "Need exactly 2 parameter types for Map types");
        }

        try {
            
            tf.constructParametricType(SingleArgGeneric.class, strC, strC);
        } catch (IllegalArgumentException e) {
            verifyException(e, "expected 1 parameters, was given 2");
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
        
    }

// com.fasterxml.jackson.databind.type.TestTypeFactory::testSuperTypeDetectionClass
    public void testSuperTypeDetectionClass()
    {
        TypeFactory tf = TypeFactory.defaultInstance();
        HierarchicType sub = tf._findSuperTypeChain(MyStringIntMap.class, HashMap.class);
        assertNotNull(sub);
        assertEquals(2, _countSupers(sub));
        assertSame(MyStringIntMap.class, sub.getRawClass());
        HierarchicType sup = sub.getSuperType();
        assertSame(MyStringXMap.class, sup.getRawClass());
        HierarchicType sup2 = sup.getSuperType();
        assertSame(HashMap.class, sup2.getRawClass());
        assertNull(sup2.getSuperType());
    }

// com.fasterxml.jackson.databind.type.TestTypeFactory::testSuperTypeDetectionInterface
    public void testSuperTypeDetectionInterface()
    {
        
        TypeFactory tf = TypeFactory.defaultInstance();
        HierarchicType sub = tf._findSuperTypeChain(MyList.class, List.class);
        assertNotNull(sub);
        assertEquals(2, _countSupers(sub));
        assertSame(MyList.class, sub.getRawClass());
        HierarchicType sup = sub.getSuperType();
        assertSame(IntermediateList.class, sup.getRawClass());
        HierarchicType sup2 = sup.getSuperType();
        assertSame(List.class, sup2.getRawClass());
        assertNull(sup2.getSuperType());
        
        
        sub = tf._findSuperTypeChain(MyMap.class, Map.class);
        assertNotNull(sub);
        assertEquals(2, _countSupers(sub));
        assertSame(MyMap.class, sub.getRawClass());
        sup = sub.getSuperType();
        assertSame(IntermediateMap.class, sup.getRawClass());
        sup2 = sup.getSuperType();
        assertSame(Map.class, sup2.getRawClass());
        assertNull(sup2.getSuperType());
    }

// com.fasterxml.jackson.databind.type.TestTypeFactory::testAtomicArrayRefParameterDetection
    public void testAtomicArrayRefParameterDetection()
    {
        TypeFactory tf = TypeFactory.defaultInstance();
        JavaType type = tf.constructType(new TypeReference<AtomicReference<long[]>>() { });
        HierarchicType sub = tf._findSuperTypeChain(type.getRawClass(), AtomicReference.class);
        assertNotNull(sub);
        assertEquals(0, _countSupers(sub));
        assertTrue(AtomicReference.class.isAssignableFrom(type.getRawClass()));
        assertNull(sub.getSuperType());
    }

// com.fasterxml.jackson.databind.type.TestTypeFactory::testMapTypesSimple
    public void testMapTypesSimple()
    {
        TypeFactory tf = TypeFactory.defaultInstance();
        JavaType type = tf.constructType(new TypeReference<Map<String,Boolean>>() { });
        MapType mapType = (MapType) type;
        assertEquals(tf.constructType(String.class), mapType.getKeyType());
        assertEquals(tf.constructType(Boolean.class), mapType.getContentType());
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

// com.fasterxml.jackson.databind.type.TestTypeFactory::testSneakySelfRefs
    public void testSneakySelfRefs() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        String json = mapper.writeValueAsString(new SneakyBean2());
        assertEquals("{\"foobar\":null}", json);
    }

// com.fasterxml.jackson.databind.type.TestTypeFactory::testRawCollections
    public void testRawCollections()
    {
        TypeFactory tf = TypeFactory.defaultInstance();
        JavaType type = tf.constructRawCollectionType(ArrayList.class);
        assertTrue(type.isContainerType());
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
        assertEquals(1, tf._typeCache.size());
        tf.clearCache();
        assertEquals(0, tf._typeCache.size());
    }

// com.fasterxml.jackson.databind.util.ArrayBuildersTest::testInsertInListNoDup
	public void testInsertInListNoDup()
	{
        String [] arr = new String[]{"me", "you", "him"};
        String [] newarr;
        
        newarr = ArrayBuilders.insertInListNoDup(arr, "you");
        Assert.assertArrayEquals(new String[]{"you", "me", "him"}, newarr);

        newarr = ArrayBuilders.insertInListNoDup(arr, "me");
        Assert.assertArrayEquals(new String[]{"me", "you","him"}, newarr);

        newarr = ArrayBuilders.insertInListNoDup(arr, "him");
        Assert.assertArrayEquals(new String[]{"him", "me", "you"}, newarr);

        newarr = ArrayBuilders.insertInListNoDup(arr, "foobar");
        Assert.assertArrayEquals(new String[]{"foobar", "me", "you", "him"}, newarr);
	}

// com.fasterxml.jackson.databind.util.ISO8601DateFormatTest::testFormat
    public void testFormat() {
        String result = df.format(date);
        assertEquals("2007-08-13T19:51:23Z", result);
    }

// com.fasterxml.jackson.databind.util.ISO8601DateFormatTest::testParse
    public void testParse() throws Exception {
        Date result = df.parse("2007-08-13T19:51:23Z");
        assertEquals(date, result);
    }

// com.fasterxml.jackson.databind.util.ISO8601DateFormatTest::testPartialParse
    public void testPartialParse() throws Exception {
        java.text.ParsePosition pos = new java.text.ParsePosition(0);
        String timestamp = "2007-08-13T19:51:23Z";
        Date result = df.parse(timestamp + "hello", pos);
        
        assertEquals(date, result);
        assertEquals(timestamp.length(), pos.getIndex());
    }

// com.fasterxml.jackson.databind.util.ISO8601DateFormatTest::testCloneObject
    public void testCloneObject() throws Exception {
        DateFormat clone = (DateFormat)df.clone();
        assertSame(df, clone);
    }

// com.fasterxml.jackson.databind.util.ISO8601UtilsTest::testFormat
    public void testFormat() {
        String result = ISO8601Utils.format(date);
        assertEquals("2007-08-13T19:51:23Z", result);
    }

// com.fasterxml.jackson.databind.util.ISO8601UtilsTest::testFormatMillis
    public void testFormatMillis() {
        String result = ISO8601Utils.format(date, true);
        assertEquals("2007-08-13T19:51:23.789Z", result);

        result = ISO8601Utils.format(date, false);
        assertEquals("2007-08-13T19:51:23Z", result);
    }

// com.fasterxml.jackson.databind.util.ISO8601UtilsTest::testFormatTimeZone
    public void testFormatTimeZone() {
        String result = ISO8601Utils.format(date, false, TimeZone.getTimeZone("GMT+02:00"));
        assertEquals("2007-08-13T21:51:23+02:00", result);
        result = ISO8601Utils.format(date, true, TimeZone.getTimeZone("GMT+02:00"));
        assertEquals("2007-08-13T21:51:23.789+02:00", result);
        result = ISO8601Utils.format(date, true, TimeZone.getTimeZone("GMT"));
        assertEquals("2007-08-13T19:51:23.789Z", result);
    }

// com.fasterxml.jackson.databind.util.ISO8601UtilsTest::testParse
    public void testParse() throws java.text.ParseException {
        Date d = ISO8601Utils.parse("2007-08-13T19:51:23.789Z", new ParsePosition(0));
        assertEquals(date, d);

        d = ISO8601Utils.parse("2007-08-13T19:51:23Z", new ParsePosition(0));
        assertEquals(dateZeroMillis, d);

        d = ISO8601Utils.parse("2007-08-13T21:51:23.789+02:00", new ParsePosition(0));
        assertEquals(date, d);
    }

// com.fasterxml.jackson.databind.util.TestClassUtil::testSuperTypes
    public void testSuperTypes()
    {
        Collection<Class<?>> result = ClassUtil.findSuperTypes(SubClass.class, null);
        Class<?>[] classes = result.toArray(new Class<?>[result.size()]);
        Class<?>[] exp = new Class[] {
            SubInt.class, BaseInt.class,
            BaseClass.class,
            Comparable.class
        };
        assertArrayEquals(exp, classes);
    }

// com.fasterxml.jackson.databind.util.TestClassUtil::testSuperInterfaces
    public void testSuperInterfaces()
    {
        Collection<Class<?>> result = ClassUtil.findSuperTypes(SubInt.class, null);
        Class<?>[] classes = result.toArray(new Class<?>[result.size()]);
        Class<?>[] exp = new Class[] {
            BaseInt.class
        };
        assertArrayEquals(exp, classes);
    }

// com.fasterxml.jackson.databind.util.TestClassUtil::testIsConcrete
    public void testIsConcrete()
    {
        assertTrue(ClassUtil.isConcrete(getClass()));
        assertFalse(ClassUtil.isConcrete(BaseClass.class));
        assertFalse(ClassUtil.isConcrete(BaseInt.class));
    }

// com.fasterxml.jackson.databind.util.TestClassUtil::testCanBeABeanType
    public void testCanBeABeanType()
    {
        assertEquals("annotation", ClassUtil.canBeABeanType(java.lang.annotation.Retention.class));
        assertEquals("array", ClassUtil.canBeABeanType(String[].class));
        assertEquals("enum", ClassUtil.canBeABeanType(TestEnum.class));
        assertEquals("primitive", ClassUtil.canBeABeanType(Integer.TYPE));
        assertNull(ClassUtil.canBeABeanType(Integer.class));

        assertEquals("non-static member class", ClassUtil.isLocalType(InnerNonStatic.class, false));
        assertNull(ClassUtil.isLocalType(Integer.class, false));
    }

// com.fasterxml.jackson.databind.util.TestClassUtil::testExceptionHelpers
    public void testExceptionHelpers()
    {
        RuntimeException e = new RuntimeException("test");
        RuntimeException wrapper = new RuntimeException(e);

        assertSame(e, ClassUtil.getRootCause(wrapper));

        try {
            ClassUtil.throwAsIAE(e);
            fail("Shouldn't get this far");
        } catch (RuntimeException e2) {
            assertSame(e, e2);
        }

        try {
            ClassUtil.unwrapAndThrowAsIAE(wrapper);
            fail("Shouldn't get this far");
        } catch (RuntimeException e2) {
            assertSame(e, e2);
        }
    }

// com.fasterxml.jackson.databind.util.TestClassUtil::testFailedCreateInstance
    public void testFailedCreateInstance()
    {
        try {
            ClassUtil.createInstance(BaseClass.class, true);
        } catch (IllegalArgumentException e) {
            verifyException(e, "has no default");
        }

        try {
            
            ClassUtil.createInstance(Inner.class, false);
        } catch (IllegalArgumentException e) {
            verifyException(e, "is not accessible");
        }

        
        try {
            ClassUtil.createInstance(Inner.class, true);
        } catch (IllegalStateException e) {
            verifyException(e, "test");
        }
    }

// com.fasterxml.jackson.databind.util.TestTokenBuffer::testSimpleWrites
    public void testSimpleWrites() throws IOException
    {
        TokenBuffer buf = new TokenBuffer(null, false); 

        
        JsonParser jp = buf.asParser();
        assertNull(jp.getCurrentToken());
        assertNull(jp.nextToken());
        jp.close();

        
        buf.writeString("abc");

        
        jp = buf.asParser();
        assertNull(jp.getCurrentToken());
        assertToken(JsonToken.VALUE_STRING, jp.nextToken());
        assertEquals("abc", jp.getText());
        assertNull(jp.nextToken());
        jp.close();

        
        buf.writeNumber(13);
        jp = buf.asParser();
        assertNull(jp.getCurrentToken());
        assertToken(JsonToken.VALUE_STRING, jp.nextToken());
        assertToken(JsonToken.VALUE_NUMBER_INT, jp.nextToken());
        assertEquals(13, jp.getIntValue());
        assertNull(jp.nextToken());
        jp.close();
        buf.close();
    }

// com.fasterxml.jackson.databind.util.TestTokenBuffer::testSimpleArray
    public void testSimpleArray() throws IOException
    {
        TokenBuffer buf = new TokenBuffer(null, false); 

        
        assertTrue(buf.getOutputContext().inRoot());
        buf.writeStartArray();
        assertTrue(buf.getOutputContext().inArray());
        buf.writeEndArray();
        assertTrue(buf.getOutputContext().inRoot());

        JsonParser jp = buf.asParser();
        assertNull(jp.getCurrentToken());
        assertTrue(jp.getParsingContext().inRoot());
        assertToken(JsonToken.START_ARRAY, jp.nextToken());
        assertTrue(jp.getParsingContext().inArray());
        assertToken(JsonToken.END_ARRAY, jp.nextToken());
        assertTrue(jp.getParsingContext().inRoot());
        assertNull(jp.nextToken());
        jp.close();
        buf.close();

        
        buf = new TokenBuffer(null, false);
        buf.writeStartArray();
        buf.writeBoolean(true);
        buf.writeNull();
        buf.writeEndArray();
        jp = buf.asParser();
        assertToken(JsonToken.START_ARRAY, jp.nextToken());
        assertToken(JsonToken.VALUE_TRUE, jp.nextToken());
        assertTrue(jp.getBooleanValue());
        assertToken(JsonToken.VALUE_NULL, jp.nextToken());
        assertToken(JsonToken.END_ARRAY, jp.nextToken());
        assertNull(jp.nextToken());
        jp.close();
        buf.close();

        
        buf = new TokenBuffer(null, false);
        buf.writeStartArray();
        buf.writeStartArray();
        buf.writeBinary(new byte[3]);
        buf.writeEndArray();
        buf.writeEndArray();
        jp = buf.asParser();
        assertToken(JsonToken.START_ARRAY, jp.nextToken());
        assertToken(JsonToken.START_ARRAY, jp.nextToken());
        
        assertToken(JsonToken.VALUE_EMBEDDED_OBJECT, jp.nextToken());
        Object ob = jp.getEmbeddedObject();
        assertNotNull(ob);
        assertTrue(ob instanceof byte[]);
        assertEquals(3, ((byte[]) ob).length);
        assertToken(JsonToken.END_ARRAY, jp.nextToken());
        assertToken(JsonToken.END_ARRAY, jp.nextToken());
        assertNull(jp.nextToken());
        jp.close();
        buf.close();
    }

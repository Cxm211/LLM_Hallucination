// buggy code
    protected JsonSerializer<?> buildContainerSerializer(SerializerProvider prov,
            JavaType type, BeanDescription beanDesc, boolean staticTyping)
        throws JsonMappingException
    {
        final SerializationConfig config = prov.getConfig();

        /* [databind#23], 15-Mar-2013, tatu: must force static handling of root value type,
         *   with just one important exception: if value type is "untyped", let's
         *   leave it as is; no clean way to make it work.
         */
        if (!staticTyping && type.useStaticType()) {
            if (!type.isContainerType() || type.getContentType().getRawClass() != Object.class) {
                staticTyping = true;
            }
        }
        
        // Let's see what we can learn about element/content/value type, type serializer for it:
        JavaType elementType = type.getContentType();
        TypeSerializer elementTypeSerializer = createTypeSerializer(config,
                elementType);

        // if elements have type serializer, can not force static typing:
        if (elementTypeSerializer != null) {
            staticTyping = false;
        }
        JsonSerializer<Object> elementValueSerializer = _findContentSerializer(prov,
                beanDesc.getClassInfo());
        if (type.isMapLikeType()) { // implements java.util.Map
            MapLikeType mlt = (MapLikeType) type;
            /* 29-Sep-2012, tatu: This is actually too early to (try to) find
             *  key serializer from property annotations, and can lead to caching
             *  issues (see [databind#75]). Instead, must be done from 'createContextual()' call.
             *  But we do need to check class annotations.
             */
            JsonSerializer<Object> keySerializer = _findKeySerializer(prov, beanDesc.getClassInfo());
            if (mlt.isTrueMapType()) {
                return buildMapSerializer(config, (MapType) mlt, beanDesc, staticTyping,
                        keySerializer, elementTypeSerializer, elementValueSerializer);
            }
            // With Map-like, just 2 options: (1) Custom, (2) Annotations
            JsonSerializer<?> ser = null;
            for (Serializers serializers : customSerializers()) { // (1) Custom
                MapLikeType mlType = (MapLikeType) type;
                ser = serializers.findMapLikeSerializer(config,
                        mlType, beanDesc, keySerializer, elementTypeSerializer, elementValueSerializer);
            if (ser != null) {
                if (_factoryConfig.hasSerializerModifiers()) {
                    for (BeanSerializerModifier mod : _factoryConfig.serializerModifiers()) {
                        ser = mod.modifyMapLikeSerializer(config, mlType, beanDesc, ser);
                    }
                    }
                    return ser;
                }
            }
            return null;
        }
        if (type.isCollectionLikeType()) {
            CollectionLikeType clt = (CollectionLikeType) type;
            if (clt.isTrueCollectionType()) {
                return buildCollectionSerializer(config,  (CollectionType) clt, beanDesc, staticTyping,
                        elementTypeSerializer, elementValueSerializer);
            }
            // With Map-like, just 2 options: (1) Custom, (2) Annotations
            JsonSerializer<?> ser = null;
            CollectionLikeType clType = (CollectionLikeType) type;
            for (Serializers serializers : customSerializers()) { // (1) Custom
                ser = serializers.findCollectionLikeSerializer(config,
                        clType, beanDesc, elementTypeSerializer, elementValueSerializer);
            if (ser != null) {
                if (_factoryConfig.hasSerializerModifiers()) {
                    for (BeanSerializerModifier mod : _factoryConfig.serializerModifiers()) {
                        ser = mod.modifyCollectionLikeSerializer(config, clType, beanDesc, ser);
                        }
                    }
                    return ser;
                }
            }
            return null;
        }
        if (type.isArrayType()) {
            return buildArraySerializer(config, (ArrayType) type, beanDesc, staticTyping,
                    elementTypeSerializer, elementValueSerializer);
        }
        return null;
    }

    protected JsonSerializer<?> buildCollectionSerializer(SerializationConfig config,
            CollectionType type, BeanDescription beanDesc, boolean staticTyping,
            TypeSerializer elementTypeSerializer, JsonSerializer<Object> elementValueSerializer) 
        throws JsonMappingException
    {
        JsonSerializer<?> ser = null;
        // Order of lookups:
        // 1. Custom serializers
        // 2. Annotations (@JsonValue, @JsonDeserialize)
        // 3. Defaults
        for (Serializers serializers : customSerializers()) { // (1) Custom
            ser = serializers.findCollectionSerializer(config,
                    type, beanDesc, elementTypeSerializer, elementValueSerializer);
            if (ser != null) {
                break;
            }
        }

        if (ser == null) {
                // We may also want to use serialize Collections "as beans", if (and only if)
                // this is specified with `@JsonFormat(shape=Object)`
                JsonFormat.Value format = beanDesc.findExpectedFormat(null);
                if (format != null && format.getShape() == JsonFormat.Shape.OBJECT) {
                    return null;
                }
                Class<?> raw = type.getRawClass();
                if (EnumSet.class.isAssignableFrom(raw)) {
                    // this may or may not be available (Class doesn't; type of field/method does)
                    JavaType enumType = type.getContentType();
                    // and even if nominally there is something, only use if it really is enum
                    if (!enumType.isEnumType()) {
                        enumType = null;
                    }
                    ser = buildEnumSetSerializer(enumType);
                } else {
                    Class<?> elementRaw = type.getContentType().getRawClass();
                    if (isIndexedList(raw)) {
                        if (elementRaw == String.class) {
                            // [JACKSON-829] Must NOT use if we have custom serializer
                            if (elementValueSerializer == null || ClassUtil.isJacksonStdImpl(elementValueSerializer)) {
                                ser = IndexedStringListSerializer.instance;
                            }
                        } else {
                            ser = buildIndexedListSerializer(type.getContentType(), staticTyping,
                                elementTypeSerializer, elementValueSerializer);
                        }
                    } else if (elementRaw == String.class) {
                        // [JACKSON-829] Must NOT use if we have custom serializer
                        if (elementValueSerializer == null || ClassUtil.isJacksonStdImpl(elementValueSerializer)) {
                            ser = StringCollectionSerializer.instance;
                        }
                    }
                    if (ser == null) {
                        ser = buildCollectionSerializer(type.getContentType(), staticTyping,
                                elementTypeSerializer, elementValueSerializer);
                }
            }
        }
        // [databind#120]: Allow post-processing
        if (_factoryConfig.hasSerializerModifiers()) {
            for (BeanSerializerModifier mod : _factoryConfig.serializerModifiers()) {
                ser = mod.modifyCollectionSerializer(config, type, beanDesc, ser);
            }
        }
        return ser;
    }

    protected JsonSerializer<?> buildMapSerializer(SerializationConfig config,
            MapType type, BeanDescription beanDesc,
            boolean staticTyping, JsonSerializer<Object> keySerializer,
            TypeSerializer elementTypeSerializer, JsonSerializer<Object> elementValueSerializer)
        throws JsonMappingException
    {
        JsonSerializer<?> ser = null;

        // Order of lookups:
        // 1. Custom serializers
        // 2. Annotations (@JsonValue, @JsonDeserialize)
        // 3. Defaults
        
        for (Serializers serializers : customSerializers()) { // (1) Custom
            ser = serializers.findMapSerializer(config, type, beanDesc,
                    keySerializer, elementTypeSerializer, elementValueSerializer);
            if (ser != null) { break; }
        }
        if (ser == null) {
                // 08-Nov-2014, tatu: As per [databind#601], better just use default Map serializer
                /*
                if (EnumMap.class.isAssignableFrom(type.getRawClass())
                        && ((keySerializer == null) || ClassUtil.isJacksonStdImpl(keySerializer))) {
                    JavaType keyType = type.getKeyType();
                    // Need to find key enum values...
                    EnumValues enums = null;
                    if (keyType.isEnumType()) { // non-enum if we got it as type erased class (from instance)
                        @SuppressWarnings("unchecked")
                        Class<Enum<?>> enumClass = (Class<Enum<?>>) keyType.getRawClass();
                        enums = EnumValues.construct(config, enumClass);
                    }
                    ser = new EnumMapSerializer(type.getContentType(), staticTyping, enums,
                        elementTypeSerializer, elementValueSerializer);
                } else {
                */
                Object filterId = findFilterId(config, beanDesc);
                AnnotationIntrospector ai = config.getAnnotationIntrospector();
                MapSerializer mapSer = MapSerializer.construct(ai.findPropertiesToIgnore(beanDesc.getClassInfo(), true),
                        type, staticTyping, elementTypeSerializer,
                        keySerializer, elementValueSerializer, filterId);
                Object suppressableValue = findSuppressableContentValue(config,
                        type.getContentType(), beanDesc);
                if (suppressableValue != null) {
                    mapSer = mapSer.withContentInclusion(suppressableValue);
                }
                ser = mapSer;
            }
        // [databind#120]: Allow post-processing
        if (_factoryConfig.hasSerializerModifiers()) {
            for (BeanSerializerModifier mod : _factoryConfig.serializerModifiers()) {
                ser = mod.modifyMapSerializer(config, type, beanDesc, ser);
            }
        }
        return ser;
    }

    protected JsonSerializer<?> buildArraySerializer(SerializationConfig config,
            ArrayType type, BeanDescription beanDesc,
            boolean staticTyping,
            TypeSerializer elementTypeSerializer, JsonSerializer<Object> elementValueSerializer)
        throws JsonMappingException
    {
        // 25-Jun-2015, tatu: Note that unlike with Collection(Like) and Map(Like) types, array
        //   types can not be annotated (in theory I guess we could have mix-ins but... ?)
        //   so we need not do primary annotation lookup here.
        //   So all we need is (1) Custom, (2) Default array serializers
        JsonSerializer<?> ser = null;

        for (Serializers serializers : customSerializers()) { // (1) Custom
             ser = serializers.findArraySerializer(config,
                     type, beanDesc, elementTypeSerializer, elementValueSerializer);
             if (ser != null) {
                 break;
             }
        }
        
        if (ser == null) {
             Class<?> raw = type.getRawClass();
             // Important: do NOT use standard serializers if non-standard element value serializer specified
             if (elementValueSerializer == null || ClassUtil.isJacksonStdImpl(elementValueSerializer)) {
                 if (String[].class == raw) {
                     ser = StringArraySerializer.instance;
                 } else {
                     // other standard types?
                     ser = StdArraySerializers.findStandardImpl(raw);
                 }
             }
             if (ser == null) {
                 ser = new ObjectArraySerializer(type.getContentType(), staticTyping, elementTypeSerializer,
                         elementValueSerializer);
             }
         }
         // [databind#120]: Allow post-processing
         if (_factoryConfig.hasSerializerModifiers()) {
             for (BeanSerializerModifier mod : _factoryConfig.serializerModifiers()) {
                 ser = mod.modifyArraySerializer(config, type, beanDesc, ser);
             }
         }
         return ser;
    }

    protected JsonSerializer<?> _createSerializer2(SerializerProvider prov,
            JavaType type, BeanDescription beanDesc, boolean staticTyping)
        throws JsonMappingException
    {
        JsonSerializer<?> ser = findSerializerByAnnotations(prov, type, beanDesc);
        if (ser != null) {
            return ser;
        }
        final SerializationConfig config = prov.getConfig();
        
        // Container types differ from non-container types
        // (note: called method checks for module-provided serializers)
        if (type.isContainerType()) {
            if (!staticTyping) {
                staticTyping = usesStaticTyping(config, beanDesc, null);
                // [Issue#23]: Need to figure out how to force passed parameterization
                //  to stick...
                /*
                if (property == null) {
                    JavaType t = origType.getContentType();
                    if (t != null && !t.hasRawClass(Object.class)) {
                        staticTyping = true;
                    }
                }
                */
            }
            // 03-Aug-2012, tatu: As per [Issue#40], may require POJO serializer...
            ser =  buildContainerSerializer(prov, type, beanDesc, staticTyping);
            // Will return right away, since called method does post-processing:
            if (ser != null) {
                return ser;
            }
        } else {
            // Modules may provide serializers of POJO types:
            for (Serializers serializers : customSerializers()) {
                ser = serializers.findSerializer(config, type, beanDesc);
                if (ser != null) {
                    break;
                }
            }
            // 25-Jun-2015, tatu: Then JsonSerializable, @JsonValue etc. NOTE! Prior to 2.6,
            //    this call was BEFORE custom serializer lookup, which was wrong.
        }
        
        if (ser == null) {
            // Otherwise, we will check "primary types"; both marker types that
            // indicate specific handling (JsonSerializable), or main types that have
            // precedence over container types
            ser = findSerializerByLookup(type, config, beanDesc, staticTyping);
            if (ser == null) {
                ser = findSerializerByPrimaryType(prov, type, beanDesc, staticTyping);
                if (ser == null) {
                    // And this is where this class comes in: if type is not a
                    // known "primary JDK type", perhaps it's a bean? We can still
                    // get a null, if we can't find a single suitable bean property.
                    ser = findBeanSerializer(prov, type, beanDesc);
                    // Finally: maybe we can still deal with it as an implementation of some basic JDK interface?
                    if (ser == null) {
                        ser = findSerializerByAddonType(config, type, beanDesc, staticTyping);
                        // 18-Sep-2014, tatu: Actually, as per [jackson-databind#539], need to get
                        //   'unknown' serializer assigned earlier, here, so that it gets properly
                        //   post-processed
                        if (ser == null) {
                            ser = prov.getUnknownTypeSerializer(beanDesc.getBeanClass());
                        }
                    }
                }
            }
        }
        if (ser != null) {
            // [databind#120]: Allow post-processing
            if (_factoryConfig.hasSerializerModifiers()) {
                for (BeanSerializerModifier mod : _factoryConfig.serializerModifiers()) {
                    ser = mod.modifySerializer(config, beanDesc, ser);
                }
            }
        }
        return ser;
    }

// relevant test
// com.fasterxml.jackson.databind.ObjectMapperTest::testProps
    public void testProps()
    {
        ObjectMapper m = new ObjectMapper();
        
        assertNotNull(m.getNodeFactory());
        JsonNodeFactory nf = JsonNodeFactory.instance;
        m.setNodeFactory(nf);
        assertNull(m.getInjectableValues());
        assertSame(nf, m.getNodeFactory());
    }

// com.fasterxml.jackson.databind.ObjectMapperTest::testSupport
    public void testSupport()
    {
        assertTrue(MAPPER.canSerialize(String.class));
        assertTrue(MAPPER.canDeserialize(TypeFactory.defaultInstance().constructType(String.class)));
    }

// com.fasterxml.jackson.databind.ObjectMapperTest::testTreeRead
    public void testTreeRead() throws Exception
    {
        String JSON = "{ }";
        JsonNode n = MAPPER.readTree(JSON);
        assertTrue(n instanceof ObjectNode);

        n = MAPPER.readTree(new StringReader(JSON));
        assertTrue(n instanceof ObjectNode);

        n = MAPPER.readTree(new ByteArrayInputStream(JSON.getBytes("UTF-8")));
        assertTrue(n instanceof ObjectNode);
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

        
        m.enable(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY);
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

// com.fasterxml.jackson.databind.ObjectMapperTest::testCopy
    public void testCopy() throws Exception
    {
        ObjectMapper m = new ObjectMapper();
        assertTrue(m.isEnabled(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES));
        m.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        assertFalse(m.isEnabled(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES));
        InjectableValues inj = new InjectableValues.Std();
        m.setInjectableValues(inj);

        
        
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
    }

// com.fasterxml.jackson.databind.ObjectMapperTest::testEmptyBeanSerializability
    public void testEmptyBeanSerializability()
    {
        
        
        
        assertTrue(MAPPER.writer().without(SerializationFeature.FAIL_ON_EMPTY_BEANS)
                .canSerialize(EmptyBean.class));
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

// com.fasterxml.jackson.databind.TestGeneratorUsingMapper::testPojoWriting
    public void testPojoWriting()
        throws IOException
    {
        JsonFactory jf = new MappingJsonFactory();
        StringWriter sw = new StringWriter();
        JsonGenerator gen = jf.createGenerator(sw);
        gen.writeObject(new Pojo());
        gen.close();
        
        String act = sw.toString().trim();
        assertEquals("{\"x\":4}", act);
    }

// com.fasterxml.jackson.databind.TestGeneratorUsingMapper::testPojoWritingFailing
    public void testPojoWritingFailing()
        throws IOException
    {
        
        JsonFactory jf = new JsonFactory();
        try {
            StringWriter sw = new StringWriter();
            JsonGenerator gen = jf.createGenerator(sw);
            gen.writeObject(new Pojo());
            gen.close();
            fail("Expected an exception: got sw '"+sw.toString()+"'");
        } catch (IllegalStateException e) {
            verifyException(e, "No ObjectCodec defined");
        }
    }

// com.fasterxml.jackson.databind.TestGeneratorUsingMapper::testIssue820
    public void testIssue820() throws IOException
    {
        StringBuffer sb = new StringBuffer();
        while (sb.length() <= 5000) {
            sb.append("Yet another line of text...\n");
        }
        String sampleText = sb.toString();
        assertTrue(
                "Sanity check so I don't mess up the sample text later.",
                sampleText.contains("\n"));

        final ObjectMapper mapper = new ObjectMapper();
        final CharacterEscapes defaultCharacterEscapes = new CharacterEscapes() {
            private static final long serialVersionUID = 1L;

            @Override
            public int[] getEscapeCodesForAscii() {
                return standardAsciiEscapesForJSON();
            }

            @Override
            public SerializableString getEscapeSequence(final int ch) {
                return null;
            }
        };

        mapper.getFactory().setCharacterEscapes(defaultCharacterEscapes);
        String jacksonJson = mapper.writeValueAsString(sampleText);
        boolean hasLFs = jacksonJson.indexOf('\n') > 0;
        assertFalse("Should NOT contain linefeeds, should have been escaped", hasLFs);
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

// com.fasterxml.jackson.databind.TestJDKSerialization::testObjectWriter
    public void testObjectWriter() throws IOException
    {
        ObjectWriter origWriter = MAPPER.writer();
        final String EXP_JSON = "{\"x\":2,\"y\":3}";
        final MyPojo p = new MyPojo(2, 3);
        assertEquals(EXP_JSON, origWriter.writeValueAsString(p));
        byte[] bytes = jdkSerialize(origWriter);
        ObjectWriter writer2 = jdkDeserialize(bytes);
        assertEquals(EXP_JSON, writer2.writeValueAsString(p));
    }

// com.fasterxml.jackson.databind.TestJDKSerialization::testObjectReader
    public void testObjectReader() throws IOException
    {
        ObjectReader origReader = MAPPER.readerFor(MyPojo.class);
        final String JSON = "{\"x\":1,\"y\":2}";
        MyPojo p1 = origReader.readValue(JSON);
        assertEquals(2, p1.y);
        byte[] bytes = jdkSerialize(origReader);
        ObjectReader reader2 = jdkDeserialize(bytes);
        MyPojo p2 = reader2.readValue(JSON);
        assertEquals(2, p2.y);
    }

// com.fasterxml.jackson.databind.TestJDKSerialization::testObjectMapper
    public void testObjectMapper() throws IOException
    {
        final String EXP_JSON = "{\"x\":2,\"y\":3}";
        final MyPojo p = new MyPojo(2, 3);
        assertEquals(EXP_JSON, MAPPER.writeValueAsString(p));

        byte[] bytes = jdkSerialize(MAPPER);
        ObjectMapper mapper2 = jdkDeserialize(bytes);
        assertEquals(EXP_JSON, mapper2.writeValueAsString(p));
        MyPojo p2 = mapper2.readValue(EXP_JSON, MyPojo.class);
        assertEquals(p.x, p2.x);
        assertEquals(p.y, p2.y);
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

// com.fasterxml.jackson.databind.TestObjectMapperBeanDeserializer::testFromStringCtor
    public void testFromStringCtor() throws Exception
    {
        CtorValueBean result = MAPPER.readValue("\"abc\"", CtorValueBean.class);
        assertEquals("abc", result.toString());
    }

// com.fasterxml.jackson.databind.TestObjectMapperBeanDeserializer::testFromIntCtor
    public void testFromIntCtor() throws Exception
    {
        CtorValueBean result = MAPPER.readValue("13", CtorValueBean.class);
        assertEquals("13", result.toString());

        try {
            OtherBean otherResult = MAPPER.readValue("13", OtherBean.class);
            fail("Expected an exception, but got result value: "+otherResult.o);
        } catch (JsonMappingException e) {
            verifyException(e, "from Integral number", "no single-int-arg constructor/factory method");
            assertValidLocation(e.getLocation());
        }
    }

// com.fasterxml.jackson.databind.TestObjectMapperBeanDeserializer::testFromLongCtor
    public void testFromLongCtor() throws Exception
    {
        
        long value = 12345678901244L;
        CtorValueBean result = MAPPER.readValue(""+value, CtorValueBean.class);
        assertEquals(""+value, result.toString());

        try {
            OtherBean otherResult = MAPPER.readValue(""+value, OtherBean.class);
            fail("Expected an exception, but got result value: "+otherResult.o);
        } catch (JsonMappingException e) {
            verifyException(e, "from Long integral number", "no single-long-arg constructor/factory method");
            assertValidLocation(e.getLocation());
        }
    }

// com.fasterxml.jackson.databind.TestObjectMapperBeanDeserializer::testFromDoubleCtor
    public void testFromDoubleCtor() throws Exception
    {
        CtorValueBean result = MAPPER.readValue("13.5", CtorValueBean.class);
        assertEquals("13.5", result.toString());

        try {
            OtherBean otherResult = MAPPER.readValue("13.5", OtherBean.class);
            fail("Expected an exception, but got result value: "+otherResult.o);
        } catch (JsonMappingException e) {
            verifyException(e, "from Floating-point number", "no one-double/Double-arg constructor/factory method");
            assertValidLocation(e.getLocation());
        }
    }

// com.fasterxml.jackson.databind.TestObjectMapperBeanDeserializer::testFromStringFactory
    public void testFromStringFactory() throws Exception
    {
        FactoryValueBean result = MAPPER.readValue("\"abc\"", FactoryValueBean.class);
        assertEquals("abc", result.toString());
    }

// com.fasterxml.jackson.databind.TestObjectMapperBeanDeserializer::testFromIntFactory
    public void testFromIntFactory() throws Exception
    {
        FactoryValueBean result = MAPPER.readValue("13", FactoryValueBean.class);
        assertEquals("13", result.toString());
    }

// com.fasterxml.jackson.databind.TestObjectMapperBeanDeserializer::testFromLongFactory
    public void testFromLongFactory() throws Exception
    {
        
        long value = 12345678901244L;
        FactoryValueBean result = MAPPER.readValue(""+value, FactoryValueBean.class);
        assertEquals(""+value, result.toString());
    }

// com.fasterxml.jackson.databind.TestObjectMapperBeanDeserializer::testSimpleBean
    public void testSimpleBean() throws Exception
    {
        ArrayList<Object> misc = new ArrayList<Object>();
        misc.add("xyz");
        misc.add(42);
        misc.add(null);
        misc.add(Boolean.TRUE);
        TestBean bean = new TestBean(13, -900L, "\"test\"", new URI("http://foobar.com"), misc);

        
        String json = MAPPER.writeValueAsString(bean);

        TestBean result = MAPPER.readValue(json, TestBean.class);
        assertEquals(bean, result);
    }

// com.fasterxml.jackson.databind.TestObjectMapperBeanDeserializer::testListBean
    public void testListBean() throws Exception
    {
        final int COUNT = 13;
        ArrayList<CtorValueBean> beans = new ArrayList<CtorValueBean>();
        for (int i = 0; i < COUNT; ++i) {
            beans.add(new CtorValueBean(i));
        }
        BeanWithList bean = new BeanWithList(beans);

        StringWriter sw = new StringWriter();
        MAPPER.writeValue(sw, bean);

        BeanWithList result = MAPPER.readValue(sw.toString(), BeanWithList.class);
        assertEquals(bean, result);
    }

// com.fasterxml.jackson.databind.TestObjectMapperBeanDeserializer::testUnknownFields
    public void testUnknownFields() throws Exception
    {
        try {
            TestBean bean = MAPPER.readValue("{ \"foobar\" : 3 }", TestBean.class);
            fail("Expected an exception, got bean: "+bean);
        } catch (JsonMappingException jse) {
            ;
        }
    }

// com.fasterxml.jackson.databind.TestObjectMapperBeanSerializer::testComplexObject
    public void testComplexObject()
        throws Exception
    {
        FixtureObject  aTestObj = new FixtureObject();
        ObjectMapper aMapper  = new ObjectMapper();
        StringWriter aWriter = new StringWriter();
        JsonGenerator aGen = new JsonFactory().createGenerator(aWriter);
        aMapper.writeValue(aGen, aTestObj);
        aGen.close();
        JsonParser jp = new JsonFactory().createParser(new StringReader(aWriter.toString()));

        assertEquals(JsonToken.START_OBJECT, jp.nextToken());

        while (jp.nextToken() != JsonToken.END_OBJECT) {
            assertEquals(JsonToken.FIELD_NAME, jp.getCurrentToken());
            String name = jp.getCurrentName();
            JsonToken t = jp.nextToken();

            if (name.equals("uri")) {
                assertToken(JsonToken.VALUE_STRING, t);
                assertEquals(FixtureObjectBase.VALUE_URSTR, getAndVerifyText(jp));
            } else if (name.equals("url")) {
                assertToken(JsonToken.VALUE_STRING, t);
                assertEquals(FixtureObjectBase.VALUE_URSTR, getAndVerifyText(jp));
            } else if (name.equals("testNull")) {
                assertToken(JsonToken.VALUE_NULL, t);
            } else if (name.equals("testString")) {
                assertToken(JsonToken.VALUE_STRING, t);
                assertEquals(FixtureObjectBase.VALUE_STRING, getAndVerifyText(jp));
            } else if (name.equals("testBoolean")) {
                assertToken(JsonToken.VALUE_TRUE, t);
            } else if (name.equals("testEnum")) {
                assertToken(JsonToken.VALUE_STRING, t);
                assertEquals(FixtureObjectBase.VALUE_ENUM.toString(),getAndVerifyText(jp));
            } else if (name.equals("testInteger")) {
                assertToken(JsonToken.VALUE_NUMBER_INT, t);
                assertEquals(jp.getIntValue(),FixtureObjectBase.VALUE_INT);
            } else if (name.equals("testLong")) {
                assertToken(JsonToken.VALUE_NUMBER_INT, t);
                assertEquals(jp.getLongValue(),FixtureObjectBase.VALUE_LONG);
            } else if (name.equals("testBigInteger")) {
                assertToken(JsonToken.VALUE_NUMBER_INT, t);
                assertEquals(jp.getLongValue(),FixtureObjectBase.VALUE_BIGINT.longValue());
            } else if (name.equals("testBigDecimal")) {
                assertToken(JsonToken.VALUE_NUMBER_FLOAT, t);
                assertEquals(jp.getText(), FixtureObjectBase.VALUE_BIGDEC.toString());
            } else if (name.equals("testCharacter")) {
                assertToken(JsonToken.VALUE_STRING, t);
                assertEquals(String.valueOf(FixtureObjectBase.VALUE_CHAR), getAndVerifyText(jp));
            } else if (name.equals("testShort")) {
                assertToken(JsonToken.VALUE_NUMBER_INT, t);
                assertEquals(jp.getIntValue(),FixtureObjectBase.VALUE_SHORT);
            } else if (name.equals("testByte")) {
                assertToken(JsonToken.VALUE_NUMBER_INT, t);
                assertEquals(jp.getIntValue(),FixtureObjectBase.VALUE_BYTE);
            } else if (name.equals("testFloat")) {
                assertToken(JsonToken.VALUE_NUMBER_FLOAT, t);
                assertEquals(jp.getDecimalValue().floatValue(),FixtureObjectBase.VALUE_FLOAT);
            } else if (name.equals("testDouble")) {
                assertToken(JsonToken.VALUE_NUMBER_FLOAT, t);
                assertEquals(jp.getDoubleValue(),FixtureObjectBase.VALUE_DBL);
            } else if (name.equals("testStringBuffer")) {
                assertToken(JsonToken.VALUE_STRING, t);
                assertEquals(FixtureObjectBase.VALUE_STRING, getAndVerifyText(jp));
            } else if (name.equals("testError")) {
                
                assertToken(JsonToken.START_OBJECT, t);

                
                
                while (jp.nextToken() == JsonToken.FIELD_NAME) {
                    name = jp.getCurrentName();
                    if (name.equals("cause")) {
                        assertEquals(JsonToken.VALUE_NULL, jp.nextToken());
                    } else if (name.equals("message")) {
                        assertEquals(JsonToken.VALUE_STRING, jp.nextToken());
                        assertEquals(FixtureObjectBase.VALUE_ERRTXT, getAndVerifyText(jp));
                    } else if (name.equals("localizedMessage")) {
                        assertEquals(JsonToken.VALUE_STRING, jp.nextToken());
                    } else if (name.equals("stackTrace")) {
                        assertEquals(JsonToken.START_ARRAY,jp.nextToken());
                        int i = 0;
                        while(jp.nextToken() != JsonToken.END_ARRAY) {
                            if(i >= 100000) {
                                assertTrue("Probably run away loop in test. StackTrack Array was not properly closed.",false);
                            }
                        }
                    } else if (name.equals("suppressed")) {
                        
                        assertEquals(JsonToken.START_ARRAY,jp.nextToken());
                        assertEquals(JsonToken.END_ARRAY,jp.nextToken());
                    } else {
                        fail("Unexpected field name '"+name+"'");
                    }
                }
                
                assertEquals(JsonToken.END_OBJECT, jp.getCurrentToken());
            } else {
                fail("Unexpected field, name '"+name+"'");
            }
        }

        
        assertNull(jp.nextToken());
        jp.close();
    }

// com.fasterxml.jackson.databind.TestParserUsingMapper::testReadingArrayAsTree
    public void testReadingArrayAsTree() throws IOException
    {
        JsonFactory jf = new MappingJsonFactory();
        final String JSON = "[ 1, 2, false ]";

        for (int i = 0; i < 2; ++i) {
            JsonParser jp = jf.createParser(new StringReader(JSON));
            
            if (i == 0) {
                assertToken(JsonToken.START_ARRAY, jp.nextToken());
            }
            JsonNode root = (JsonNode) jp.readValueAsTree();
            jp.close();
            assertTrue(root.isArray());
            assertEquals(3, root.size());
            assertEquals(1, root.get(0).intValue());
            assertEquals(2, root.get(1).intValue());
            assertFalse(root.get(2).booleanValue());
        }
    }

// com.fasterxml.jackson.databind.TestParserUsingMapper::testPojoReading
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

// com.fasterxml.jackson.databind.TestParserUsingMapper::testIncrementalPojoReading
    public void testIncrementalPojoReading()
        throws IOException
    {
        JsonFactory jf = new MappingJsonFactory();
        final String JSON = "[ 1, true, null, \"abc\" ]";
        JsonParser jp = jf.createParser(new StringReader(JSON));

        
        assertToken(JsonToken.START_ARRAY, jp.nextToken());

        assertToken(JsonToken.VALUE_NUMBER_INT, jp.nextToken());
        assertEquals(Integer.valueOf(1), jp.readValueAs(Integer.class));
        assertEquals(Boolean.TRUE, jp.readValueAs(Boolean.class));
        
        assertNull(jp.readValueAs(Object.class));
        
        assertEquals(JsonToken.VALUE_NULL, jp.getLastClearedToken());

        assertEquals("abc", jp.readValueAs(String.class));

        
        assertNull(jp.readValueAs(Object.class));
        assertEquals(JsonToken.END_ARRAY, jp.getLastClearedToken());

        
        assertNull(jp.nextToken());

        jp.close();
    }

// com.fasterxml.jackson.databind.TestParserUsingMapper::testPojoReadingFailing
    public void testPojoReadingFailing()
        throws IOException
    {
        
        JsonFactory jf = new JsonFactory();
        try {
            final String JSON = "{ \"x\" : 9 }";
            JsonParser jp = jf.createParser(new StringReader(JSON));
            Pojo p = jp.readValueAs(Pojo.class);
            fail("Expected an exception: got "+p);
        } catch (IllegalStateException e) {
            verifyException(e, "No ObjectCodec defined");
        }
    }

// com.fasterxml.jackson.databind.TestParserUsingMapper::testEscapingUsingMapper
    public void testEscapingUsingMapper() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(JsonGenerator.Feature.ESCAPE_NON_ASCII, true);
        mapper.writeValueAsString(String.valueOf((char) 257));
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

// com.fasterxml.jackson.databind.TestStdDateFormat::testFactories
    public void testFactories() {
        TimeZone tz = TimeZone.getTimeZone("GMT");
        Locale loc = Locale.US;
        assertNotNull(StdDateFormat.getISO8601Format(tz, loc));
        assertNotNull(StdDateFormat.getRFC1123Format(tz, loc));
    }

// com.fasterxml.jackson.databind.TestStdDateFormat::testInvalid
    public void testInvalid() {
        StdDateFormat std = new StdDateFormat();
        try {
            std.parse("foobar");
        } catch (java.text.ParseException e) {
            verifyException(e, "Can not parse");
        }
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

// com.fasterxml.jackson.databind.access.TestSerAnyGetter::testDynaBean
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

// com.fasterxml.jackson.databind.access.TestSerAnyGetter::testPrivate
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

        try {
            R.forType(Integer.class).readValue("1.5");
            fail("Should not pass");
        } catch (JsonMappingException e) {
            verifyException(e, "Can not coerce a floating-point");
        }

        try {
            R.forType(Integer.TYPE).readValue("1.5");
            fail("Should not pass");
        } catch (JsonMappingException e) {
            verifyException(e, "Can not coerce a floating-point");
        }
        
        try {
            R.forType(IntWrapper.class).readValue("{\"i\":-2.25 }");
            fail("Should not pass");
        } catch (JsonMappingException e) {
            verifyException(e, "Can not coerce a floating-point");
        }
    }

// com.fasterxml.jackson.databind.convert.NumericConversionTest::testDoubleToLong
    public void testDoubleToLong() throws Exception
    {
        
        Long L = MAPPER.readValue(" 3.33 ", Long.class);
        assertEquals(3L, L.longValue());
        LongWrapper w = MAPPER.readValue("{\"l\":-2.25 }", LongWrapper.class);
        assertEquals(-2L, w.l);

        try {
            R.forType(Long.class).readValue("1.5");
            fail("Should not pass");
        } catch (JsonMappingException e) {
            verifyException(e, "Can not coerce a floating-point");
        }

        try {
            R.forType(Long.TYPE).readValue("1.5");
            fail("Should not pass");
        } catch (JsonMappingException e) {
            verifyException(e, "Can not coerce a floating-point");
        }
        
        try {
            R.forType(LongWrapper.class).readValue("{\"l\": 7.7 }");
            fail("Should not pass");
        } catch (JsonMappingException e) {
            verifyException(e, "Can not coerce a floating-point");
        }
    }

// com.fasterxml.jackson.databind.convert.TestArrayConversions::testNullXform
    public void testNullXform() throws Exception
    {
        
        assertNull(mapper.convertValue(null, Integer.class));
        assertNull(mapper.convertValue(null, String.class));
        assertNull(mapper.convertValue(null, byte[].class));
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
        
        List<Integer> actNums = mapper.convertValue(data, new TypeReference<List<Integer>>() {});
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
        List<Long> actNums = mapper.convertValue(data, new TypeReference<List<Long>>() {});
        assertEquals(expNums, actNums);        
    }

// com.fasterxml.jackson.databind.convert.TestArrayConversions::testOverflows
    public void testOverflows()
    {
        
        try {
            mapper.convertValue(new int[] { 1000 }, byte[].class);
        } catch (IllegalArgumentException e) {
            verifyException(e, OVERFLOW_MSG_BYTE);
        }
        
        try {
            mapper.convertValue(new int[] { -99999 }, short[].class);
        } catch (IllegalArgumentException e) {
            verifyException(e, OVERFLOW_MSG);
        }
        
        try {
            mapper.convertValue(new long[] { Long.MAX_VALUE }, int[].class);
        } catch (IllegalArgumentException e) {
            verifyException(e, OVERFLOW_MSG);
        }
        
        BigInteger biggie = BigInteger.valueOf(Long.MAX_VALUE);
        biggie.add(BigInteger.ONE);
        List<BigInteger> l = new ArrayList<BigInteger>();
        l.add(biggie);
        try {
            mapper.convertValue(l, int[].class);
        } catch (IllegalArgumentException e) {
            verifyException(e, OVERFLOW_MSG);
        }
        
    }

// com.fasterxml.jackson.databind.convert.TestBeanConversions::testBeanConvert
    public void testBeanConvert()
    {
        
        PointStrings input = new PointStrings("37", "-9");
        Point point = MAPPER.convertValue(input, Point.class);
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
            verifyException(e, "from String value 'foobar'");
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
        
        ObjectNode root = MAPPER.createObjectNode();
        JsonNode n = root;
        ObjectNode ob2 = MAPPER.convertValue(n, ObjectNode.class);
        assertSame(root, ob2);

        JsonNode n2 = MAPPER.convertValue(n, JsonNode.class);
        assertSame(root, n2);
        
        
        String STR = "test";
        CharSequence seq = MAPPER.convertValue(STR, CharSequence.class);
        assertSame(STR, seq);

        
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

// com.fasterxml.jackson.databind.convert.TestUpdateValue::testBeanUpdate
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

// com.fasterxml.jackson.databind.convert.TestUpdateValue::testListUpdate
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

// com.fasterxml.jackson.databind.convert.TestUpdateValue::testMapUpdate
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

// com.fasterxml.jackson.databind.convert.TestUpdateValue::testUpdateSequence
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

// com.fasterxml.jackson.databind.convert.TestUpdateValue::testUpdatingWithViews
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

// com.fasterxml.jackson.databind.convert.TestUpdateValue::testIssue744
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

// com.fasterxml.jackson.databind.creators.BigCreatorTest::testBigPartial
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

// com.fasterxml.jackson.databind.creators.BuilderSimpleTest::testSimple
    public void testSimple() throws Exception
    {
        String json = "{\"x\":1,\"y\":2}";
        Object o = MAPPER.readValue(json, ValueClassXY.class);
        assertNotNull(o);
    	    assertSame(ValueClassXY.class, o.getClass());
    	    ValueClassXY value = (ValueClassXY) o;
    	    
    	    assertEquals(value._x, 2);
    	    assertEquals(value._y, 3);
    }

// com.fasterxml.jackson.databind.creators.BuilderSimpleTest::testMultiAccess
    public void testMultiAccess() throws Exception
    {
        String json = "{\"c\":3,\"a\":2,\"b\":-9}";
        ValueClassABC value = MAPPER.readValue(json, ValueClassABC.class);
        assertNotNull(value);
    	    
        assertEquals(value.a, 2);
        assertEquals(value.b, -9);
        assertEquals(value.c, 3);
    }

// com.fasterxml.jackson.databind.creators.BuilderSimpleTest::testImmutable
    public void testImmutable() throws Exception
    {
        final String json = "{\"value\":13}";
        ValueImmutable value = MAPPER.readValue(json, ValueImmutable.class);        
        assertEquals(13, value.value);
    }

// com.fasterxml.jackson.databind.creators.BuilderSimpleTest::testCustomWith
    public void testCustomWith() throws Exception
    {
        final String json = "{\"value\":1}";
        ValueFoo value = MAPPER.readValue(json, ValueFoo.class);        
        assertEquals(1, value.value);
    }

// com.fasterxml.jackson.databind.creators.BuilderSimpleTest::testWithCreator
    public void testWithCreator() throws Exception
    {
        final String json = "{\"a\":1,\"c\":3,\"b\":2}";
        CreatorValue value = MAPPER.readValue(json, CreatorValue.class);        
        assertEquals(1, value.a);
        assertEquals(2, value.b);
        assertEquals(3, value.c);
    }

// com.fasterxml.jackson.databind.creators.BuilderSimpleTest::testBuilderMethodReturnMoreGeneral
    public void testBuilderMethodReturnMoreGeneral() throws Exception
    {
        final String json = "{\"x\":1}";
        ValueInterface value = MAPPER.readValue(json, ValueInterface.class);
        assertEquals(2, value.getX());
    }

// com.fasterxml.jackson.databind.creators.BuilderSimpleTest::testBuilderMethodReturnMoreSpecific
    public void testBuilderMethodReturnMoreSpecific() throws Exception
    {
        final String json = "{\"x\":1}";
        ValueInterface2 value = MAPPER.readValue(json, ValueInterface2.class);
        assertEquals(2, value.getX());
    }

// com.fasterxml.jackson.databind.creators.BuilderSimpleTest::testBuilderMethodReturnInvalidType
    public void testBuilderMethodReturnInvalidType() throws Exception
    {
        final String json = "{\"x\":1}";
        try {
            MAPPER.readValue(json, ValueClassWrongBuildType.class);
            fail("Missing expected JsonProcessingException exception");
        } catch(JsonProcessingException e) {
            assertTrue(
                    "Exception cause must be IllegalArgumentException",
                    e.getCause() instanceof IllegalArgumentException);
        }
    }

// com.fasterxml.jackson.databind.creators.BuilderSimpleTest::testSelfBuilder777
    public void testSelfBuilder777() throws Exception
    {
        SelfBuilder777 result = MAPPER.readValue(aposToQuotes("{'x':3}'"),
                SelfBuilder777.class);
        assertNotNull(result);
        assertEquals(3, result.x);
    }

// com.fasterxml.jackson.databind.creators.BuilderSimpleTest::testWithAnySetter822
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

// com.fasterxml.jackson.databind.creators.DisablingCreatorsTest::testDisabling
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

// com.fasterxml.jackson.databind.creators.ImplicitNameMatch792Test::testValue
        public int testValue() { return value; }

// com.fasterxml.jackson.databind.creators.ImplicitNameMatch792Test::testBindingOfImplicitCreatorNames
    public void testBindingOfImplicitCreatorNames() throws Exception
    {
        ObjectMapper m = new ObjectMapper();
        m.setAnnotationIntrospector(new ConstructorNameAI());
        String json = m.writeValueAsString(new Issue792Bean("a", "b"));
        assertEquals(aposToQuotes("{'first':'a','other':3}"), json);
    }

// com.fasterxml.jackson.databind.creators.ImplicitNameMatch792Test::testImplicitWithSetterGetter
    public void testImplicitWithSetterGetter() throws Exception
    {
        String json = MAPPER.writeValueAsString(new Bean2());
        assertEquals(aposToQuotes("{'stuff':3}"), json);
    }

// com.fasterxml.jackson.databind.creators.ImplicitNameMatch792Test::testReadWriteWithPrivateField
    public void testReadWriteWithPrivateField() throws Exception
    {
        String json = MAPPER.writeValueAsString(new ReadWriteBean(3));
        assertEquals("{\"value\":3}", json);
    }

// com.fasterxml.jackson.databind.creators.ImplicitNameMatch792Test::testWriteOnly
    public void testWriteOnly() throws Exception
    {
        PasswordBean bean = MAPPER.readValue(aposToQuotes("{'value':7,'password':'foo'}"),
                PasswordBean.class);
        assertEquals("[password='foo',value=7]", bean.asString());
        String json = MAPPER.writeValueAsString(bean);
        assertEquals("{\"value\":7}", json);
    }

// com.fasterxml.jackson.databind.creators.ImplicitParamsForCreatorTest::testNonSingleArgCreator
    public void testNonSingleArgCreator() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setAnnotationIntrospector(new MyParamIntrospector());
        XY value = mapper.readValue(aposToQuotes("{'paramName0':1,'paramName1':2}"), XY.class);
        assertNotNull(value);
        assertEquals(1, value.x);
        assertEquals(2, value.y);
    }

// com.fasterxml.jackson.databind.creators.MultiArgConstructorTest::testMultiArgVisible
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

// com.fasterxml.jackson.databind.creators.MultiArgConstructorTest::testMultiArgWithPartialOverride
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

// com.fasterxml.jackson.databind.creators.MultiArgConstructorTest::testMultiArgNotVisible
    public void testMultiArgNotVisible() throws Exception
    {
        final ObjectMapper mapper = new ObjectMapper();
        mapper.setAnnotationIntrospector(new MyParamIntrospector());
        mapper.setVisibility(PropertyAccessor.CREATOR, Visibility.NONE);
        try {
             mapper.readValue(aposToQuotes("{'b':13,  'a':-99}"),
                MultiArgCtorBean.class);
            fail("Should not have passed");
        } catch (JsonMappingException e) {
            verifyException(e, "No suitable constructor");
        }
    }

// com.fasterxml.jackson.databind.creators.RequiredCreatorTest::testRequiredAnnotatedParam
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

// com.fasterxml.jackson.databind.creators.RequiredCreatorTest::testRequiredGloballyParam
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

// com.fasterxml.jackson.databind.creators.SingleArgCreatorTest::testNamedSingleArg
    public void testNamedSingleArg() throws Exception
    {
        SingleNamedStringBean bean = MAPPER.readValue(quote("foobar"),
                SingleNamedStringBean.class);
        assertEquals("foobar", bean._ss);
    }

// com.fasterxml.jackson.databind.creators.SingleArgCreatorTest::testSingleStringArgWithImplicitName
    public void testSingleStringArgWithImplicitName() throws Exception
    {
        final ObjectMapper mapper = new ObjectMapper();
        mapper.setAnnotationIntrospector(new MyParamIntrospector("value"));
        StringyBean bean = mapper.readValue(quote("foobar"), StringyBean.class);
        assertEquals("foobar", bean.getValue());
    }

// com.fasterxml.jackson.databind.creators.SingleArgCreatorTest::testSingleImplicitlyNamedNotDelegating
    public void testSingleImplicitlyNamedNotDelegating() throws Exception
    {
        final ObjectMapper mapper = new ObjectMapper();
        mapper.setAnnotationIntrospector(new MyParamIntrospector("value"));
        StringyBeanWithProps bean = mapper.readValue("{\"value\":\"x\"}", StringyBeanWithProps.class);
        assertEquals("x", bean.getValue());
    }

// com.fasterxml.jackson.databind.creators.SingleArgCreatorTest::testSingleExplicitlyNamedButDelegating
    public void testSingleExplicitlyNamedButDelegating() throws Exception
    {
        SingleNamedButStillDelegating bean = MAPPER.readValue(quote("xyz"),
                SingleNamedButStillDelegating.class);
        assertEquals("xyz", bean.value);
    }

// com.fasterxml.jackson.databind.creators.SingleArgCreatorTest::testExplicitFactory660a
    public void testExplicitFactory660a() throws Exception
    {
        
        ExplicitFactoryBeanA bean = MAPPER.readValue(quote("abc"), ExplicitFactoryBeanA.class);
        assertNotNull(bean);
        assertEquals("abc", bean.value());
    }

// com.fasterxml.jackson.databind.creators.SingleArgCreatorTest::testExplicitFactory660b
    public void testExplicitFactory660b() throws Exception
    {
        
        ExplicitFactoryBeanB bean2 = MAPPER.readValue(quote("def"), ExplicitFactoryBeanB.class);
        assertNotNull(bean2);
        assertEquals("def", bean2.value());
    }

// com.fasterxml.jackson.databind.creators.TestConstructFromMap::testViaConstructor
    public void testViaConstructor() throws Exception
    {
        ObjectMapper m = new ObjectMapper();
        ConstructorFromMap result = m.readValue
            ("{ \"x\":1, \"y\" : \"abc\" }", ConstructorFromMap.class);
        assertEquals(1, result._x);
        assertEquals("abc", result._y);
    }

// com.fasterxml.jackson.databind.creators.TestConstructFromMap::testViaFactory
    public void testViaFactory() throws Exception
    {
        ObjectMapper m = new ObjectMapper();
        FactoryFromPoint result = m.readValue("{ \"x\" : 3, \"y\" : 4 }", FactoryFromPoint.class);
        assertEquals(3, result._x);
        assertEquals(4, result._y);
    }

// com.fasterxml.jackson.databind.creators.TestConstructFromMap::testViaFactoryUsingString
    public void testViaFactoryUsingString() throws Exception
    {
        ObjectMapper m = new ObjectMapper();
        FactoryFromDecimalString result = m.readValue("\"12.57\"", FactoryFromDecimalString.class);
        assertNotNull(result);
        assertEquals(12, result._value);
    }

// com.fasterxml.jackson.databind.creators.TestCreatorNullValue::testUsesDeserializersNullValue
    public void testUsesDeserializersNullValue() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new TestModule());
        Container container = mapper.readValue("{}", Container.class);
        assertEquals(NULL_CONTAINED, container.contained);
    }

// com.fasterxml.jackson.databind.creators.TestCreatorNullValue::testCreatorReturningNull
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

// com.fasterxml.jackson.databind.creators.TestCreatorWithNamingStrategy556::testRenameViaCtor
    public void testRenameViaCtor() throws Exception
    {
        RenamingCtorBean bean = MAPPER.readValue(CTOR_JSON, RenamingCtorBean.class);
        assertEquals(42, bean.myAge);
        assertEquals("NotMyRealName", bean.myName);
    }

// com.fasterxml.jackson.databind.creators.TestCreatorWithNamingStrategy556::testRenameViaFactory
    public void testRenameViaFactory() throws Exception
    {
        RenamedFactoryBean bean = MAPPER.readValue(CTOR_JSON, RenamedFactoryBean.class);
        assertEquals(42, bean.myAge);
        assertEquals("NotMyRealName", bean.myName);
    }

// com.fasterxml.jackson.databind.creators.TestCreatorWithPolymorphic113::testSubtypes
    public void testSubtypes() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        String id = "nice dogy";
        String json = mapper.writeValueAsString(new AnimalWrapper(new Dog(id)));

        AnimalWrapper wrapper = mapper.readValue(json, AnimalWrapper.class);
        assertEquals(id, wrapper.getAnimal().getId());
    }

// com.fasterxml.jackson.databind.creators.TestCreators::testSimpleConstructor
    public void testSimpleConstructor() throws Exception
    {
        ConstructorBean bean = MAPPER.readValue("{ \"x\" : 42 }", ConstructorBean.class);
        assertEquals(42, bean.x);
    }

// com.fasterxml.jackson.databind.creators.TestCreators::testNoArgsFactory
    public void testNoArgsFactory() throws Exception
    {
        NoArgFactoryBean value = MAPPER.readValue("{\"y\":13}", NoArgFactoryBean.class);
        assertEquals(13, value.y);
        assertEquals(123, value.x);
    }

// com.fasterxml.jackson.databind.creators.TestCreators::testSimpleDoubleConstructor
    public void testSimpleDoubleConstructor() throws Exception
    {
        Double exp = new Double("0.25");
        DoubleConstructorBean bean = MAPPER.readValue(exp.toString(), DoubleConstructorBean.class);
        assertEquals(exp, bean.d);
    }

// com.fasterxml.jackson.databind.creators.TestCreators::testSimpleBooleanConstructor
    public void testSimpleBooleanConstructor() throws Exception
    {
        BooleanConstructorBean bean = MAPPER.readValue(" true ", BooleanConstructorBean.class);
        assertEquals(Boolean.TRUE, bean.b);

        BooleanConstructorBean2 bean2 = MAPPER.readValue(" true ", BooleanConstructorBean2.class);
        assertTrue(bean2.b);
    }

// com.fasterxml.jackson.databind.creators.TestCreators::testSimpleFactory
    public void testSimpleFactory() throws Exception
    {
        FactoryBean bean = MAPPER.readValue("{ \"f\" : 0.25 }", FactoryBean.class);
        assertEquals(0.25, bean.d);
    }

// com.fasterxml.jackson.databind.creators.TestCreators::testLongFactory
    public void testLongFactory() throws Exception
    {
        long VALUE = 123456789000L;
        LongFactoryBean bean = MAPPER.readValue(String.valueOf(VALUE), LongFactoryBean.class);
        assertEquals(VALUE, bean.value);
    }

// com.fasterxml.jackson.databind.creators.TestCreators::testStringFactory
    public void testStringFactory() throws Exception
    {
        String str = "abc";
        StringFactoryBean bean = MAPPER.readValue(quote(str), StringFactoryBean.class);
        assertEquals(str, bean.value);
    }

// com.fasterxml.jackson.databind.creators.TestCreators::testStringFactoryAlt
    public void testStringFactoryAlt() throws Exception
    {
        String str = "xyz";
        FromStringBean bean = MAPPER.readValue(quote(str), FromStringBean.class);
        assertEquals(str, bean.value);
    }

// com.fasterxml.jackson.databind.creators.TestCreators::testConstructorCreator
    public void testConstructorCreator() throws Exception
    {
        CreatorBean bean = MAPPER.readValue
            ("{ \"a\" : \"xyz\", \"x\" : 12 }", CreatorBean.class);
        assertEquals(13, bean.x);
        assertEquals("ctor:xyz", bean.a);
    }

// com.fasterxml.jackson.databind.creators.TestCreators::testConstructorAndProps
    public void testConstructorAndProps() throws Exception
    {
        ConstructorAndPropsBean bean = MAPPER.readValue
            ("{ \"a\" : \"1\", \"b\": 2, \"c\" : true }", ConstructorAndPropsBean.class);
        assertEquals(1, bean.a);
        assertEquals(2, bean.b);
        assertEquals(true, bean.c);
    }

// com.fasterxml.jackson.databind.creators.TestCreators::testFactoryAndProps
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

// com.fasterxml.jackson.databind.creators.TestCreators::testMultipleCreators
    public void testMultipleCreators() throws Exception
    {
        MultiBean bean = MAPPER.readValue("123", MultiBean.class);
        assertEquals(Integer.valueOf(123), bean.value);
        bean = MAPPER.readValue(quote("abc"), MultiBean.class);
        assertEquals("abc", bean.value);
        bean = MAPPER.readValue("0.25", MultiBean.class);
        assertEquals(Double.valueOf(0.25), bean.value);
    }

// com.fasterxml.jackson.databind.creators.TestCreators::testDeferredConstructorAndProps
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

// com.fasterxml.jackson.databind.creators.TestCreators::testDeferredFactoryAndProps
    public void testDeferredFactoryAndProps() throws Exception
    {
        DeferredFactoryAndPropsBean bean = MAPPER.readValue
            ("{ \"prop\" : \"1\", \"ctor\" : \"2\" }", DeferredFactoryAndPropsBean.class);
        assertEquals("1", bean.prop);
        assertEquals("2", bean.ctor);
    }

// com.fasterxml.jackson.databind.creators.TestCreators::testFactoryCreatorWithMixin
    public void testFactoryCreatorWithMixin() throws Exception
    {
        ObjectMapper m = new ObjectMapper();
        m.addMixIn(CreatorBean.class, MixIn.class);
        CreatorBean bean = m.readValue
            ("{ \"a\" : \"xyz\", \"x\" : 12 }", CreatorBean.class);
        assertEquals(11, bean.x);
        assertEquals("factory:xyz", bean.a);
    }

// com.fasterxml.jackson.databind.creators.TestCreators::testFactoryCreatorWithRenamingMixin
    public void testFactoryCreatorWithRenamingMixin() throws Exception
    {
        ObjectMapper m = new ObjectMapper();
        m.addMixIn(FactoryBean.class, FactoryBeanMixIn.class);
        
        FactoryBean bean = m.readValue("{ \"mixed\" :  20.5 }", FactoryBean.class);
        assertEquals(20.5, bean.d);
    }

// com.fasterxml.jackson.databind.creators.TestCreators::testMapWithConstructor
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

// com.fasterxml.jackson.databind.creators.TestCreators::testMapWithFactory
    public void testMapWithFactory() throws Exception
    {
        MapWithFactory result = MAPPER.readValue
            ("{\"x\":\"...\",\"b\":true  }",
             MapWithFactory.class);
        assertEquals("...", result.get("x"));
        assertEquals(1, result.size());
        assertEquals(Boolean.TRUE, result._b);
    }

// com.fasterxml.jackson.databind.creators.TestCreators::testBrokenConstructor
    public void testBrokenConstructor() throws Exception
    {
        try {
             MAPPER.readValue("{ \"x\" : 42 }", BrokenBean.class);
        } catch (JsonMappingException je) {
            verifyException(je, "has no property name");
        }
    }

// com.fasterxml.jackson.databind.creators.TestCreators2::testExceptionFromConstructor
    public void testExceptionFromConstructor() throws Exception
    {
        try {
            MAPPER.readValue("{}", BustedCtor.class);
            fail("Expected exception");
        } catch (JsonMappingException e) {
            verifyException(e, ": foobar");
            
            Throwable t = e.getCause();
            assertNotNull(t);
            assertEquals(IllegalArgumentException.class, t.getClass());
            assertEquals("foobar", t.getMessage());
        }
    }

// com.fasterxml.jackson.databind.creators.TestCreators2::testSimpleConstructor
    public void testSimpleConstructor() throws Exception
    {
        HashTest test = MAPPER.readValue("{\"type\":\"custom\",\"bytes\":\"abc\" }", HashTest.class);
        assertEquals("custom", test.type);
        assertEquals("abc", new String(test.bytes, "UTF-8"));
    }

// com.fasterxml.jackson.databind.creators.TestCreators2::testMissingPrimitives
    public void testMissingPrimitives() throws Exception
    {
        Primitives p = MAPPER.readValue("{}", Primitives.class);
        assertFalse(p.b);
        assertEquals(0, p.x);
        assertEquals(0.0, p.d);
    }

// com.fasterxml.jackson.databind.creators.TestCreators2::testJackson431
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

// com.fasterxml.jackson.databind.creators.TestCreators2::testJackson438
    public void testJackson438() throws Exception
    {
        try {
            MAPPER.readValue("{ \"name\":\"foobar\" }", BeanFor438.class);
            fail("Should have failed");
        } catch (Exception e) {
            if (!(e instanceof JsonMappingException)) {
                fail("Should have received JsonMappingException, caught "+e.getClass().getName());
            }
            verifyException(e, "don't like that name");
            
            Throwable t = e.getCause();
            assertNotNull(t);
            assertEquals(IllegalArgumentException.class, t.getClass());
            verifyException(e, "don't like that name");
        }
    }

// com.fasterxml.jackson.databind.creators.TestCreators2::testIssue465
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

// com.fasterxml.jackson.databind.creators.TestCreators2::testCreatorWithDupNames
    public void testCreatorWithDupNames() throws Exception
    {
        try {
            MAPPER.readValue("{\"bar\":\"x\"}", BrokenCreatorBean.class);
            fail("Should have caught duplicate creator parameters");
        } catch (JsonMappingException e) {
            verifyException(e, "duplicate creator property \"bar\"");
        }
    }

// com.fasterxml.jackson.databind.creators.TestCreators2::testCreatorMultipleArgumentWithoutAnnotation
    public void testCreatorMultipleArgumentWithoutAnnotation() throws Exception {
        AutoDetectConstructorBean value = MAPPER.readValue("{\"bar\":\"bar\",\"foo\":\"foo\"}",
                AutoDetectConstructorBean.class);
        assertEquals("bar", value.bar);
        assertEquals("foo", value.foo);
    }

// com.fasterxml.jackson.databind.creators.TestCreators2::testIgnoredSingleArgCtor
    public void testIgnoredSingleArgCtor() throws Exception
    {
        try {
            MAPPER.readValue(quote("abc"), IgnoredCtor.class);
            fail("Should have caught missing constructor problem");
        } catch (JsonMappingException e) {
            verifyException(e, "no single-String constructor/factory method");
        }
    }

// com.fasterxml.jackson.databind.creators.TestCreators2::testAbstractFactory
    public void testAbstractFactory() throws Exception
    {
        AbstractBase bean = MAPPER.readValue("{\"a\":3}", AbstractBase.class);
        assertNotNull(bean);
        AbstractBaseImpl impl = (AbstractBaseImpl) bean;
        assertEquals(1, impl.props.size());
        assertEquals(Integer.valueOf(3), impl.props.get("a"));
    }

// com.fasterxml.jackson.databind.creators.TestCreators2::testCreatorProperties
    public void testCreatorProperties() throws Exception
    {
        Issue700Bean value = MAPPER.readValue("{ \"item\" : \"foo\" }", Issue700Bean.class);
        assertNotNull(value);
    }

// com.fasterxml.jackson.databind.creators.TestCreators421::testMultiCtor421
    public void testMultiCtor421() throws Exception
    {
        final ObjectMapper mapper = new ObjectMapper();
        mapper.setAnnotationIntrospector(new MyParamIntrospector());

        MultiCtor bean = mapper.readValue(aposToQuotes("{'a':'123','b':'foo'}"), MultiCtor.class);
        assertNotNull(bean);
        assertEquals("123", bean._a);
        assertEquals("foo", bean._b);
    }

// com.fasterxml.jackson.databind.creators.TestCreators541::testCreator541
    public void testCreator541() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();

        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        mapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
        mapper.disable(
                MapperFeature.AUTO_DETECT_CREATORS,
                MapperFeature.AUTO_DETECT_FIELDS,
                MapperFeature.AUTO_DETECT_GETTERS,
                MapperFeature.AUTO_DETECT_IS_GETTERS,
                MapperFeature.AUTO_DETECT_SETTERS,
                MapperFeature.USE_GETTERS_AS_SETTERS
        );
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);  

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

// com.fasterxml.jackson.databind.creators.TestCreatorsDelegating::testBooleanDelegate
    public void testBooleanDelegate() throws Exception
    {
        ObjectMapper m = new ObjectMapper();
        
        BooleanBean bb = m.readValue("true", BooleanBean.class);
        assertEquals(Boolean.TRUE, bb.value);

        
        bb = m.readValue(quote("true"), BooleanBean.class);
        assertEquals(Boolean.TRUE, bb.value);
    }

// com.fasterxml.jackson.databind.creators.TestCreatorsDelegating::testWithCtorAndDelegate
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

// com.fasterxml.jackson.databind.creators.TestCreatorsDelegating::testWithFactoryAndDelegate
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

// com.fasterxml.jackson.databind.creators.TestCreatorsDelegating::testDelegateWithTokenBuffer
    public void testDelegateWithTokenBuffer() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        Value592 value = mapper.readValue("{\"a\":1,\"b\":2}", Value592.class);
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

// com.fasterxml.jackson.databind.creators.TestPolymorphicCreators::testManualPolymorphicDog
    public void testManualPolymorphicDog() throws Exception
    {
        
        Animal animal = MAPPER.readValue("{ \"type\":\"dog\", \"name\":\"Fido\", \"barkVolume\" : 95.0 }", Animal.class);
        assertEquals(Dog.class, animal.getClass());
        assertEquals("Fido", animal.name);
        assertEquals(95.0, ((Dog) animal).barkVolume);
    }

// com.fasterxml.jackson.databind.creators.TestPolymorphicCreators::testManualPolymorphicCatBasic
    public void testManualPolymorphicCatBasic() throws Exception
    {
        
        Animal animal = MAPPER.readValue("{ \"name\" : \"Macavity\", \"type\":\"cat\", \"lives\":18, \"likesCream\":false }", Animal.class);
        assertEquals(Cat.class, animal.getClass());
        assertEquals("Macavity", animal.name); 
        Cat cat = (Cat) animal;
        assertEquals(18, cat.lives);
        
        assertEquals(false, cat.likesCream);
    }

// com.fasterxml.jackson.databind.creators.TestPolymorphicCreators::testManualPolymorphicCatWithReorder
    public void testManualPolymorphicCatWithReorder() throws Exception
    {
        
        Animal animal = MAPPER.readValue("{ \"likesCream\":true, \"name\" : \"Venla\", \"type\":\"cat\" }", Animal.class);
        assertEquals(Cat.class, animal.getClass());
        assertEquals("Venla", animal.name);
        
        assertTrue(((Cat) animal).likesCream);
    }

// com.fasterxml.jackson.databind.creators.TestPolymorphicCreators::testManualPolymorphicWithNumbered
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

// com.fasterxml.jackson.databind.creators.TestPolymorphicDelegating::testAbstractDelegateWithCreator
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

// com.fasterxml.jackson.databind.creators.TestValueInstantiator::testCustomBeanInstantiator
    public void testCustomBeanInstantiator() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new MyModule(MyBean.class, new MyBeanInstantiator()));
        MyBean bean = mapper.readValue("{}", MyBean.class);
        assertNotNull(bean);
        assertEquals("secret!", bean._secret);
    }

// com.fasterxml.jackson.databind.creators.TestValueInstantiator::testCustomListInstantiator
    public void testCustomListInstantiator() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new MyModule(MyList.class, new MyListInstantiator()));
        MyList result = mapper.readValue("[]", MyList.class);
        assertNotNull(result);
        assertEquals(MyList.class, result.getClass());
        assertEquals(0, result.size());
    }

// com.fasterxml.jackson.databind.creators.TestValueInstantiator::testCustomMapInstantiator
    public void testCustomMapInstantiator() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new MyModule(MyMap.class, new MyMapInstantiator()));
        MyMap result = mapper.readValue("{ \"a\":\"b\" }", MyMap.class);
        assertNotNull(result);
        assertEquals(MyMap.class, result.getClass());
        assertEquals(1, result.size());
    }

// com.fasterxml.jackson.databind.creators.TestValueInstantiator::testDelegateBeanInstantiator
    public void testDelegateBeanInstantiator() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new MyModule(MyBean.class, new MyDelegateBeanInstantiator()));
        MyBean bean = mapper.readValue("123", MyBean.class);
        assertNotNull(bean);
        assertEquals("123", bean._secret);
    }

// com.fasterxml.jackson.databind.creators.TestValueInstantiator::testDelegateListInstantiator
    public void testDelegateListInstantiator() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new MyModule(MyList.class, new MyDelegateListInstantiator()));
        MyList result = mapper.readValue("123", MyList.class);
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(Integer.valueOf(123), result.get(0));
    }

// com.fasterxml.jackson.databind.creators.TestValueInstantiator::testDelegateMapInstantiator
    public void testDelegateMapInstantiator() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new MyModule(MyMap.class, new MyDelegateMapInstantiator()));
        MyMap result = mapper.readValue("123", MyMap.class);
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(Integer.valueOf(123), result.values().iterator().next());
    }

// com.fasterxml.jackson.databind.creators.TestValueInstantiator::testCustomDelegateInstantiator
    public void testCustomDelegateInstantiator() throws Exception
    {
        AnnotatedBeanDelegating value = MAPPER.readValue("{\"a\":3}", AnnotatedBeanDelegating.class);
        assertNotNull(value);
        Object ob = value.value;
        assertNotNull(ob);
        assertTrue(ob instanceof Map);
    }

// com.fasterxml.jackson.databind.creators.TestValueInstantiator::testPropertyBasedBeanInstantiator
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

// com.fasterxml.jackson.databind.creators.TestValueInstantiator::testPropertyBasedMapInstantiator
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

// com.fasterxml.jackson.databind.creators.TestValueInstantiator::testBeanFromString
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

// com.fasterxml.jackson.databind.creators.TestValueInstantiator::testBeanFromInt
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

// com.fasterxml.jackson.databind.creators.TestValueInstantiator::testBeanFromLong
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

// com.fasterxml.jackson.databind.creators.TestValueInstantiator::testBeanFromDouble
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

// com.fasterxml.jackson.databind.creators.TestValueInstantiator::testBeanFromBoolean
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

// com.fasterxml.jackson.databind.creators.TestValueInstantiator::testPolymorphicCreatorBean
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

// com.fasterxml.jackson.databind.creators.TestValueInstantiator::testEmptyBean
    public void testEmptyBean() throws Exception
    {
        AnnotatedBean bean = MAPPER.readValue("{}", AnnotatedBean.class);
        assertNotNull(bean);
        assertEquals("foo", bean.a);
        assertEquals(3, bean.b);
    }

// com.fasterxml.jackson.databind.creators.TestValueUpdate::testValueUpdateWithCreator
    public void testValueUpdateWithCreator() throws Exception
    {
        Bean bean = new Bean("abc", "def");
        new ObjectMapper().readerFor(Bean.class).withValueToUpdate(bean).readValue("{\"a\":\"ghi\",\"b\":\"jkl\"}");
        assertEquals("ghi", bean.getA());
        assertEquals("jkl", bean.getB());
    }

// com.fasterxml.jackson.databind.deprecated.MiscDeprecatedTest::testOldEnumMapSerializer
    public void testOldEnumMapSerializer() throws Exception
    {
        
        
        Class<?> enumClass = ABC.class;
        EnumMapSerializer ser = new EnumMapSerializer(MAPPER.constructType(String.class), true,
                EnumValues.construct(MAPPER.getSerializationConfig(), (Class<Enum<?>>) enumClass),
                null,  null);
        
        
        assertNotNull(ser);
    }

// com.fasterxml.jackson.databind.deser.TestAbstract::testAbstractFailure
    public void testAbstractFailure() throws Exception
    {
        ObjectMapper m = new ObjectMapper();
        try {
            m.readValue("{ \"x\" : 3 }", Abstract.class);
            fail("Should fail on trying to deserialize abstract type");
        } catch (JsonProcessingException e) {
            verifyException(e, "can not construct");
        }
    }

// com.fasterxml.jackson.databind.deser.TestAnnotationIgnore::testSimpleIgnore
    public void testSimpleIgnore() throws Exception
    {
        SizeClassIgnore result = MAPPER.readValue
            ("{ \"x\":1, \"y\" : 2 }",
             SizeClassIgnore.class);
        
        assertEquals(1, result._x);
        assertEquals(0, result._y);
    }

// com.fasterxml.jackson.databind.deser.TestAnnotationIgnore::testFailOnIgnore
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

// com.fasterxml.jackson.databind.deser.TestAnyProperties::testSimpleMapImitation
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

// com.fasterxml.jackson.databind.deser.TestAnyProperties::testSimpleTyped
    public void testSimpleTyped() throws Exception
    {
        MapImitatorWithValue mapHolder = MAPPER.readValue
            ("{ \"a\" : [ 3, -1 ], \"b\" : [ ] }", MapImitatorWithValue.class);
        Map<String,int[]> result = mapHolder._map;
        assertEquals(2, result.size());
        assertEquals(new int[] { 3, -1 }, result.get("a"));
        assertEquals(new int[0], result.get("b"));
    }

// com.fasterxml.jackson.databind.deser.TestAnyProperties::testBrokenWithDoubleAnnotations
    public void testBrokenWithDoubleAnnotations() throws Exception
    {
        try {
            @SuppressWarnings("unused")
            Broken b = MAPPER.readValue("{ \"a\" : 3 }", Broken.class);
            fail("Should have gotten an exception");
        } catch (JsonMappingException e) {
            verifyException(e, "Multiple 'any-setters'");
        }
    }

// com.fasterxml.jackson.databind.deser.TestAnyProperties::testIgnored
    public void testIgnored() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        _testIgnorals(mapper);
    }

// com.fasterxml.jackson.databind.deser.TestAnyProperties::testIgnoredPart2
    public void testIgnoredPart2() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        _testIgnorals(mapper);
    }

// com.fasterxml.jackson.databind.deser.TestAnyProperties::testProblem744
    public void testProblem744() throws Exception
    {
        Bean744 bean = MAPPER.readValue("{\"name\":\"Bob\"}", Bean744.class);
        assertNotNull(bean.additionalProperties);
        assertEquals(1, bean.additionalProperties.size());
        assertEquals("Bob", bean.additionalProperties.get("name"));
    }

// com.fasterxml.jackson.databind.deser.TestAnyProperties::testIssue797
    public void testIssue797() throws Exception
    {
        String json = MAPPER.writeValueAsString(new Bean797BaseImpl());
        assertEquals("{}", json);
    }

// com.fasterxml.jackson.databind.deser.TestAnyProperties::testPolymorphic
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

// com.fasterxml.jackson.databind.deser.TestBeanDeserializer::testPOJOFromEmptyArray
    public void testPOJOFromEmptyArray() throws Exception
    {
        final String JSON = "  [\n]";
        assertFalse(MAPPER.isEnabled(DeserializationFeature.ACCEPT_EMPTY_ARRAY_AS_NULL_OBJECT));
        
        ObjectMapper mapper = new ObjectMapper();
        try {
            mapper.readValue(JSON, Bean.class);
            fail("Should not accept Empty Array for POJO by default");
        } catch (JsonProcessingException e) {
            verifyException(e, "START_ARRAY token");
            assertValidLocation(e.getLocation());
        }

        
        ObjectReader r = MAPPER.readerFor(Bean.class)
                .with(DeserializationFeature.ACCEPT_EMPTY_ARRAY_AS_NULL_OBJECT);
        Bean result = r.readValue(JSON);
        assertNull(result);
    }

// com.fasterxml.jackson.databind.deser.TestBeanDeserializer::testCaseInsensitiveDeserialization
    public void testCaseInsensitiveDeserialization() throws Exception
    {
    	final String JSON = "{\"Value1\" : {\"nAme\" : \"fruit\", \"vALUe\" : \"apple\"}, \"valUE2\" : {\"NAME\" : \"color\", \"value\" : \"red\"}}";
        
        
        ObjectMapper mapper = new ObjectMapper();
        assertFalse(mapper.isEnabled(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES));
        
        try {
            mapper.readValue(JSON, Issue476Bean.class);
            
            fail("Should not accept improper case properties by default");
        } catch (JsonProcessingException e) {
            verifyException(e, "Unrecognized field");
            assertValidLocation(e.getLocation());
        }

        
        mapper = new ObjectMapper();
        mapper.configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true);
        ObjectReader r = mapper.readerFor(Issue476Bean.class);
        Issue476Bean result = r.readValue(JSON);
        assertEquals(result.value1.name, "fruit");
        assertEquals(result.value1.value, "apple");
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

// com.fasterxml.jackson.databind.deser.TestConfig::testEnumIndexes
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

// buggy code
    public ObjectIdInfo findObjectReferenceInfo(Annotated ann, ObjectIdInfo objectIdInfo) {
        JsonIdentityReference ref = _findAnnotation(ann, JsonIdentityReference.class);
        if (ref != null) {
            objectIdInfo = objectIdInfo.withAlwaysAsId(ref.alwaysAsId());
        }
        return objectIdInfo;
    }

    protected ObjectIdInfo(PropertyName prop, Class<?> scope, Class<? extends ObjectIdGenerator<?>> gen,
            boolean alwaysAsId, Class<? extends ObjectIdResolver> resolver)
    {
        _propertyName = prop;
        _scope = scope;
        _generator = gen;
        _alwaysAsId = alwaysAsId;
        if (resolver == null) {
            resolver = SimpleObjectIdResolver.class;
        }
        _resolver = resolver;
    }

    public JsonSerializer<?> createContextual(SerializerProvider provider,
            BeanProperty property)
        throws JsonMappingException
    {
        final AnnotationIntrospector intr = provider.getAnnotationIntrospector();
        final AnnotatedMember accessor = (property == null || intr == null)
                ? null : property.getMember();
        final SerializationConfig config = provider.getConfig();
        
        // Let's start with one big transmutation: Enums that are annotated
        // to serialize as Objects may want to revert
        JsonFormat.Shape shape = null;
        if (accessor != null) {
            JsonFormat.Value format = intr.findFormat((Annotated) accessor);

            if (format != null) {
                shape = format.getShape();
                // or, alternatively, asked to revert "back to" other representations...
                if (shape != _serializationShape) {
                    if (_handledType.isEnum()) {
                        switch (shape) {
                        case STRING:
                        case NUMBER:
                        case NUMBER_INT:
                            // 12-Oct-2014, tatu: May need to introspect full annotations... but
                            //   for now, just do class ones
                            BeanDescription desc = config.introspectClassAnnotations(_handledType);
                            JsonSerializer<?> ser = EnumSerializer.construct(_handledType,
                                    provider.getConfig(), desc, format);
                            return provider.handlePrimaryContextualization(ser, property);
                        }
                    }
                }
            }
        }

        ObjectIdWriter oiw = _objectIdWriter;
        String[] ignorals = null;
        Object newFilterId = null;
        
        // Then we may have an override for Object Id
        if (accessor != null) {
            ignorals = intr.findPropertiesToIgnore(accessor, true);
            ObjectIdInfo objectIdInfo = intr.findObjectIdInfo(accessor);
            if (objectIdInfo == null) {
                // no ObjectId override, but maybe ObjectIdRef?
                if (oiw != null) {
                    objectIdInfo = intr.findObjectReferenceInfo(accessor,
                            new ObjectIdInfo(NAME_FOR_OBJECT_REF, null, null, null));
                        oiw = _objectIdWriter.withAlwaysAsId(objectIdInfo.getAlwaysAsId());
                }
            } else {
                // Ugh: mostly copied from BeanDeserializerBase: but can't easily change it
                // to be able to move to SerializerProvider (where it really belongs)
                
                // 2.1: allow modifications by "id ref" annotations as well:
                objectIdInfo = intr.findObjectReferenceInfo(accessor, objectIdInfo);
                ObjectIdGenerator<?> gen;
                Class<?> implClass = objectIdInfo.getGeneratorType();
                JavaType type = provider.constructType(implClass);
                JavaType idType = provider.getTypeFactory().findTypeParameters(type, ObjectIdGenerator.class)[0];
                // Property-based generator is trickier
                if (implClass == ObjectIdGenerators.PropertyGenerator.class) { // most special one, needs extra work
                    String propName = objectIdInfo.getPropertyName().getSimpleName();
                    BeanPropertyWriter idProp = null;

                    for (int i = 0, len = _props.length ;; ++i) {
                        if (i == len) {
                            throw new IllegalArgumentException("Invalid Object Id definition for "+_handledType.getName()
                                    +": can not find property with name '"+propName+"'");
                        }
                        BeanPropertyWriter prop = _props[i];
                        if (propName.equals(prop.getName())) {
                            idProp = prop;
                            /* Let's force it to be the first property to output
                             * (although it may still get rearranged etc)
                             */
                            if (i > 0) { // note: must shuffle both regular properties and filtered
                                System.arraycopy(_props, 0, _props, 1, i);
                                _props[0] = idProp;
                                if (_filteredProps != null) {
                                    BeanPropertyWriter fp = _filteredProps[i];
                                    System.arraycopy(_filteredProps, 0, _filteredProps, 1, i);
                                    _filteredProps[0] = fp;
                                }
                            }
                            break;
                        }
                    }
                    idType = idProp.getType();
                    gen = new PropertyBasedObjectIdGenerator(objectIdInfo, idProp);
                    oiw = ObjectIdWriter.construct(idType, (PropertyName) null, gen, objectIdInfo.getAlwaysAsId());
                } else { // other types need to be simpler
                    gen = provider.objectIdGeneratorInstance(accessor, objectIdInfo);
                    oiw = ObjectIdWriter.construct(idType, objectIdInfo.getPropertyName(), gen,
                            objectIdInfo.getAlwaysAsId());
                }
            }
            
            // Or change Filter Id in use?
            Object filterId = intr.findFilterId(accessor);
            if (filterId != null) {
                // but only consider case of adding a new filter id (no removal via annotation)
                if (_propertyFilterId == null || !filterId.equals(_propertyFilterId)) {
                    newFilterId = filterId;
                }
            }
        }
        // either way, need to resolve serializer:
        BeanSerializerBase contextual = this;
        if (oiw != null) {
            JsonSerializer<?> ser = provider.findValueSerializer(oiw.idType, property);
            oiw = oiw.withSerializer(ser);
            if (oiw != _objectIdWriter) {
                contextual = contextual.withObjectIdWriter(oiw);
            }
        }
        // And possibly add more properties to ignore
        if (ignorals != null && ignorals.length != 0) {
            contextual = contextual.withIgnorals(ignorals);
        }
        if (newFilterId != null) {
            contextual = contextual.withFilterId(newFilterId);
        }
        if (shape == null) {
            shape = _serializationShape;
        }
        if (shape == JsonFormat.Shape.ARRAY) {
            return contextual.asArraySerializer();
        }
        return contextual;
    }

// relevant test
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

// com.fasterxml.jackson.databind.type.TestTypeFactoryWithClassLoader::testUsesCorrectClassLoaderWhenThreadClassLoaderIsNull
public void testUsesCorrectClassLoaderWhenThreadClassLoaderIsNull() throws ClassNotFoundException {
	Thread.currentThread().setContextClassLoader(null);
	TypeFactory spySut = spy(objectMapper.getTypeFactory().withModifier(typeModifier).withClassLoader(classLoader));
	Class<?> clazz = spySut.findClass(aClassName);
	verify(spySut).getClassLoader();
	verify(spySut).classForName(any(String.class), any(Boolean.class), eq(classLoader));
	Assert.assertNotNull(clazz);
	Assert.assertEquals(classLoader, spySut.getClassLoader());
	Assert.assertEquals(typeModifier,spySut._modifiers[0]);
	Assert.assertEquals(null, Thread.currentThread().getContextClassLoader());
}

// com.fasterxml.jackson.databind.type.TestTypeFactoryWithClassLoader::testUsesCorrectClassLoaderWhenThreadClassLoaderIsNotNull
public void testUsesCorrectClassLoaderWhenThreadClassLoaderIsNotNull() throws ClassNotFoundException {
	TypeFactory spySut = spy(objectMapper.getTypeFactory().withModifier(typeModifier).withClassLoader(classLoader));
	Class<?> clazz = spySut.findClass(aClassName);
	verify(spySut).getClassLoader();
	verify(spySut).classForName(any(String.class), any(Boolean.class), eq(classLoader));
	Assert.assertNotNull(clazz);
	Assert.assertEquals(classLoader, spySut.getClassLoader());
	Assert.assertEquals(typeModifier,spySut._modifiers[0]);
}

// com.fasterxml.jackson.databind.type.TestTypeFactoryWithClassLoader::testCallingOnlyWithModifierGivesExpectedResults
public void testCallingOnlyWithModifierGivesExpectedResults(){
	TypeFactory sut = objectMapper.getTypeFactory().withModifier(typeModifier);
	Assert.assertNull(sut.getClassLoader());
	Assert.assertEquals(typeModifier,sut._modifiers[0]);
}

// com.fasterxml.jackson.databind.type.TestTypeFactoryWithClassLoader::testCallingOnlyWithClassLoaderGivesExpectedResults
public void testCallingOnlyWithClassLoaderGivesExpectedResults(){
	TypeFactory sut = objectMapper.getTypeFactory().withClassLoader(classLoader);
	Assert.assertNotNull(sut.getClassLoader());
	Assert.assertArrayEquals(null,sut._modifiers);
}

// com.fasterxml.jackson.databind.type.TestTypeFactoryWithClassLoader::testDefaultTypeFactoryNotAffectedByWithConstructors
public void testDefaultTypeFactoryNotAffectedByWithConstructors() {
	TypeFactory sut = objectMapper.getTypeFactory().withModifier(typeModifier).withClassLoader(classLoader);
	Assert.assertEquals(classLoader, sut.getClassLoader());
	Assert.assertEquals(typeModifier,sut._modifiers[0]);
	Assert.assertNull(objectMapper.getTypeFactory().getClassLoader());
	Assert.assertArrayEquals(null,objectMapper.getTypeFactory()._modifiers);
}

// com.fasterxml.jackson.databind.type.TestTypeFactoryWithClassLoader::testSetsTheCorrectClassLoderIfUsingWithModifierFollowedByWithClassLoader
public void testSetsTheCorrectClassLoderIfUsingWithModifierFollowedByWithClassLoader() {
	TypeFactory sut = objectMapper.getTypeFactory().withModifier(typeModifier).withClassLoader(classLoader);
	Assert.assertNotNull(sut.getClassLoader());
}

// com.fasterxml.jackson.databind.type.TestTypeFactoryWithClassLoader::testSetsTheCorrectClassLoderIfUsingWithClassLoaderFollowedByWithModifier
public void testSetsTheCorrectClassLoderIfUsingWithClassLoaderFollowedByWithModifier() {
	TypeFactory sut = objectMapper.getTypeFactory().withClassLoader(classLoader).withModifier(typeModifier);
	Assert.assertNotNull(sut.getClassLoader());
}

// com.fasterxml.jackson.databind.type.TestTypeFactoryWithClassLoader::testThreadContextClassLoaderIsUsedIfNotUsingWithClassLoader
public void testThreadContextClassLoaderIsUsedIfNotUsingWithClassLoader() throws ClassNotFoundException {
	TypeFactory spySut = spy(objectMapper.getTypeFactory());
	Assert.assertNull(spySut.getClassLoader());
	Class<?> clazz = spySut.findClass(aClassName);
	Assert.assertNotNull(clazz);
	verify(spySut).classForName(any(String.class), any(Boolean.class), eq(threadClassLoader));
}

// com.fasterxml.jackson.databind.type.TestTypeFactoryWithClassLoader::testUsesFallBackClassLoaderIfNoThreadClassLoaderAndNoWithClassLoader
public void testUsesFallBackClassLoaderIfNoThreadClassLoaderAndNoWithClassLoader() throws ClassNotFoundException {
	Thread.currentThread().setContextClassLoader(null);
	TypeFactory spySut = spy(objectMapper.getTypeFactory());
	Assert.assertNull(spySut.getClassLoader());
	Assert.assertArrayEquals(null,spySut._modifiers);
	Class<?> clazz = spySut.findClass(aClassName);
	Assert.assertNotNull(clazz);
	verify(spySut).classForName(any(String.class));
}

// com.fasterxml.jackson.databind.type.TestTypeResolution::testMaps
    public void testMaps()
    {
        TypeFactory tf = TypeFactory.defaultInstance();
        JavaType t = tf.constructType(new TypeReference<LongValuedMap<String>>() { });
        MapType type = (MapType) t;
        assertSame(LongValuedMap.class, type.getRawClass());
        assertEquals(tf.constructType(String.class), type.getKeyType());
        assertEquals(tf.constructType(Long.class), type.getContentType());        
    }

// com.fasterxml.jackson.databind.type.TestTypeResolution::testListViaTypeRef
    public void testListViaTypeRef()
    {
        TypeFactory tf = TypeFactory.defaultInstance();
        JavaType t = tf.constructType(new TypeReference<MyLongList<Integer>>() {});
        CollectionType type = (CollectionType) t;
        assertSame(MyLongList.class, type.getRawClass());
        assertEquals(tf.constructType(Long.class), type.getContentType());        
    }

// com.fasterxml.jackson.databind.type.TestTypeResolution::testListViaClass
    public void testListViaClass()
    {
        TypeFactory tf = TypeFactory.defaultInstance();
        JavaType t = tf.constructType(LongList.class);
        JavaType type = (CollectionType) t;
        assertSame(LongList.class, type.getRawClass());
        assertEquals(tf.constructType(Long.class), type.getContentType());        
    }

// com.fasterxml.jackson.databind.type.TestTypeResolution::testGeneric
    public void testGeneric()
    {
        TypeFactory tf = TypeFactory.defaultInstance();

        
        JavaType t = tf.constructType(DoubleRange.class);
        JavaType rangeParams = t.findSuperType(Range.class);
        assertEquals(1, rangeParams.containedTypeCount());
        assertEquals(Double.class, rangeParams.containedType(0).getRawClass());

        
        t = tf.constructType(new TypeReference<DoubleRange>() { });
        rangeParams = t.findSuperType(Range.class);
        assertEquals(1, rangeParams.containedTypeCount());
        assertEquals(Double.class, rangeParams.containedType(0).getRawClass());
    }

// com.fasterxml.jackson.databind.type.TypeAliasesTest::testAliasResolutionIssue743
    public void testAliasResolutionIssue743() throws Exception
    {
        String s3 = "{\"dataObj\" : [ \"one\", \"two\", \"three\" ] }";
        ObjectMapper m = new ObjectMapper();
   
        Child.ChildData d = m.readValue(s3, Child.ChildData.class);
        assertNotNull(d.dataObj);
        assertEquals(3, d.dataObj.size());
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

        
        Date dateOnly = df.parse("2007-08-14");
        Calendar cal = new GregorianCalendar(2007, 8-1, 14);
        assertEquals(cal.getTime(), dateOnly);

        dateOnly = df.parse("2007-08-14Z");
        cal = new GregorianCalendar(2007, 8-1, 14);
        cal.setTimeZone(TimeZone.getTimeZone("GMT"));
        assertEquals(cal.getTime(), dateOnly);
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

// com.fasterxml.jackson.databind.util.ISO8601DateFormatTest::testHashCodeEquals
    public void testHashCodeEquals() throws Exception {
        
        DateFormat defaultDF = StdDateFormat.instance;
        defaultDF.hashCode();
        assertTrue(defaultDF.equals(defaultDF));
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

// com.fasterxml.jackson.databind.util.ISO8601UtilsTest::testParseShortDate
    public void testParseShortDate() throws java.text.ParseException {
        Date d = ISO8601Utils.parse("20070813T19:51:23.789Z", new ParsePosition(0));
        assertEquals(date, d);

        d = ISO8601Utils.parse("20070813T19:51:23Z", new ParsePosition(0));
        assertEquals(dateZeroMillis, d);

        d = ISO8601Utils.parse("20070813T21:51:23.789+02:00", new ParsePosition(0));
        assertEquals(date, d);
    }

// com.fasterxml.jackson.databind.util.ISO8601UtilsTest::testParseShortTime
    public void testParseShortTime() throws java.text.ParseException {
        Date d = ISO8601Utils.parse("2007-08-13T195123.789Z", new ParsePosition(0));
        assertEquals(date, d);

        d = ISO8601Utils.parse("2007-08-13T195123Z", new ParsePosition(0));
        assertEquals(dateZeroMillis, d);

        d = ISO8601Utils.parse("2007-08-13T215123.789+02:00", new ParsePosition(0));
        assertEquals(date, d);
    }

// com.fasterxml.jackson.databind.util.ISO8601UtilsTest::testParseShortDateTime
    public void testParseShortDateTime() throws java.text.ParseException {
        Date d = ISO8601Utils.parse("20070813T195123.789Z", new ParsePosition(0));
        assertEquals(date, d);

        d = ISO8601Utils.parse("20070813T195123Z", new ParsePosition(0));
        assertEquals(dateZeroMillis, d);

        d = ISO8601Utils.parse("20070813T215123.789+02:00", new ParsePosition(0));
        assertEquals(date, d);
    }

// com.fasterxml.jackson.databind.util.ISO8601UtilsTest::testParseWithoutTime
    public void testParseWithoutTime() throws ParseException {
        Date d = ISO8601Utils.parse("2007-08-13Z", new ParsePosition(0));
        assertEquals(dateWithoutTime, d);

        d = ISO8601Utils.parse("20070813Z", new ParsePosition(0));
        assertEquals(dateWithoutTime, d);

        d = ISO8601Utils.parse("2007-08-13+00:00", new ParsePosition(0));
        assertEquals(dateWithoutTime, d);

        d = ISO8601Utils.parse("20070813+00:00", new ParsePosition(0));
        assertEquals(dateWithoutTime, d);
    }

// com.fasterxml.jackson.databind.util.ISO8601UtilsTest::testParseOptional
    public void testParseOptional() throws java.text.ParseException {
        Date d = ISO8601Utils.parse("2007-08-13T19:51Z", new ParsePosition(0));
        assertEquals(dateZeroSecondAndMillis, d);

        d = ISO8601Utils.parse("2007-08-13T1951Z", new ParsePosition(0));
        assertEquals(dateZeroSecondAndMillis, d);

        d = ISO8601Utils.parse("2007-08-13T21:51+02:00", new ParsePosition(0));
        assertEquals(dateZeroSecondAndMillis, d);
    }

// com.fasterxml.jackson.databind.util.ISO8601UtilsTest::testParseRfc3339Examples
    public void testParseRfc3339Examples() throws java.text.ParseException {
        
        Date d = ISO8601Utils.parse("1985-04-12T23:20:50.52Z", new ParsePosition(0));
        assertEquals(newDate(1985, 4, 12, 23, 20, 50, 520, 0), d);

        d = ISO8601Utils.parse("1996-12-19T16:39:57-08:00", new ParsePosition(0));
        assertEquals(newDate(1996, 12, 19, 16, 39, 57, 0, -8 * 60), d);

        
        d = ISO8601Utils.parse("1990-12-31T23:59:60Z", new ParsePosition(0));
        assertEquals(newDate(1990, 12, 31, 23, 59, 59, 0, 0), d);

        
        d = ISO8601Utils.parse("1990-12-31T15:59:60-08:00", new ParsePosition(0));
        assertEquals(newDate(1990, 12, 31, 15, 59, 59, 0, -8 * 60), d);

        
        d = ISO8601Utils.parse("1937-01-01T12:00:27.87+00:20", new ParsePosition(0));
        assertEquals(newDate(1937, 1, 1, 12, 0, 27, 870, 20), d);
    }

// com.fasterxml.jackson.databind.util.ISO8601UtilsTest::testFractionalSeconds
    public void testFractionalSeconds() throws java.text.ParseException {
        Date d = ISO8601Utils.parse("1970-01-01T00:00:00.9Z", new ParsePosition(0));
        assertEquals(newDate(1970, 1, 1, 0, 0, 0, 900, 0), d);

        d = ISO8601Utils.parse("1970-01-01T00:00:00.09Z", new ParsePosition(0));
        assertEquals(newDate(1970, 1, 1, 0, 0, 0, 90, 0), d);

        d = ISO8601Utils.parse("1970-01-01T00:00:00.009Z", new ParsePosition(0));
        assertEquals(newDate(1970, 1, 1, 0, 0, 0, 9, 0), d);

        d = ISO8601Utils.parse("1970-01-01T00:00:00.0009Z", new ParsePosition(0));
        assertEquals(newDate(1970, 1, 1, 0, 0, 0, 0, 0), d);

        d = ISO8601Utils.parse("1970-01-01T00:00:00.2147483647Z", new ParsePosition(0));
        assertEquals(newDate(1970, 1, 1, 0, 0, 0, 214, 0), d);

        d = ISO8601Utils.parse("1970-01-01T00:00:00.2147483648Z", new ParsePosition(0));
        assertEquals(newDate(1970, 1, 1, 0, 0, 0, 214, 0), d);

        d = ISO8601Utils.parse("1970-01-01T00:00:00.9+02:00", new ParsePosition(0));
        assertEquals(newDate(1970, 1, 1, 0, 0, 0, 900, 2 * 60), d);

        d = ISO8601Utils.parse("1970-01-01T00:00:00.09+02:00", new ParsePosition(0));
        assertEquals(newDate(1970, 1, 1, 0, 0, 0, 90, 2 * 60), d);

        d = ISO8601Utils.parse("1970-01-01T00:00:00.009+02:00", new ParsePosition(0));
        assertEquals(newDate(1970, 1, 1, 0, 0, 0, 9, 2 * 60), d);

        d = ISO8601Utils.parse("1970-01-01T00:00:00.0009+02:00", new ParsePosition(0));
        assertEquals(newDate(1970, 1, 1, 0, 0, 0, 0, 2 * 60), d);

        d = ISO8601Utils.parse("1970-01-01T00:00:00.2147483648+02:00", new ParsePosition(0));
        assertEquals(newDate(1970, 1, 1, 0, 0, 0, 214, 2 * 60), d);
    }

// com.fasterxml.jackson.databind.util.ISO8601UtilsTest::testDecimalWithoutDecimalPointButNoFractionalSeconds
    public void testDecimalWithoutDecimalPointButNoFractionalSeconds() throws java.text.ParseException {
        try {
            ISO8601Utils.parse("1970-01-01T00:00:00.Z", new ParsePosition(0));
            fail();
        } catch (ParseException expected) {
        }
    }

// com.fasterxml.jackson.databind.util.TestClassUtil::testIsConcrete
    public void testIsConcrete() throws Exception
    {
        assertTrue(ClassUtil.isConcrete(getClass()));
        assertFalse(ClassUtil.isConcrete(BaseClass.class));
        assertFalse(ClassUtil.isConcrete(BaseInt.class));

        assertFalse(ClassUtil.isConcrete(ConcreteAndAbstract.class.getDeclaredMethod("a")));
        assertTrue(ClassUtil.isConcrete(ConcreteAndAbstract.class.getDeclaredMethod("c")));
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
            ClassUtil.throwRootCause(e);
            fail("Shouldn't get this far");
        } catch (Exception eAct) {
            assertSame(e, eAct);
        }

        Error err = new Error();
        try {
            ClassUtil.throwAsIAE(err);
            fail("Shouldn't get this far");
        } catch (Error errAct) {
            assertSame(err, errAct);
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

// com.fasterxml.jackson.databind.util.TestClassUtil::testPrimiteDefaultValue
    public void testPrimiteDefaultValue()
    {
        assertEquals(Integer.valueOf(0), ClassUtil.defaultValue(Integer.TYPE));
        assertEquals(Long.valueOf(0L), ClassUtil.defaultValue(Long.TYPE));
        assertEquals(Character.valueOf('\0'), ClassUtil.defaultValue(Character.TYPE));
        assertEquals(Short.valueOf((short) 0), ClassUtil.defaultValue(Short.TYPE));
        assertEquals(Byte.valueOf((byte) 0), ClassUtil.defaultValue(Byte.TYPE));

        assertEquals(Double.valueOf(0.0), ClassUtil.defaultValue(Double.TYPE));
        assertEquals(Float.valueOf(0.0f), ClassUtil.defaultValue(Float.TYPE));

        try {
            ClassUtil.defaultValue(String.class);
        } catch (IllegalArgumentException e) {
            verifyException(e, "String is not a primitive type");
        }
    }

// com.fasterxml.jackson.databind.util.TestClassUtil::testPrimiteWrapperType
    public void testPrimiteWrapperType()
    {
        assertEquals(Integer.class, ClassUtil.wrapperType(Integer.TYPE));
        assertEquals(Long.class, ClassUtil.wrapperType(Long.TYPE));
        assertEquals(Character.class, ClassUtil.wrapperType(Character.TYPE));
        assertEquals(Short.class, ClassUtil.wrapperType(Short.TYPE));
        assertEquals(Byte.class, ClassUtil.wrapperType(Byte.TYPE));

        assertEquals(Double.class, ClassUtil.wrapperType(Double.TYPE));
        assertEquals(Float.class, ClassUtil.wrapperType(Float.TYPE));

        try {
            ClassUtil.wrapperType(String.class);
        } catch (IllegalArgumentException e) {
            verifyException(e, "String is not a primitive type");
        }
    }

// com.fasterxml.jackson.databind.util.TestClassUtil::testFindEnumType
    public void testFindEnumType()
    {
        assertEquals(TestEnum.class, ClassUtil.findEnumType(TestEnum.A));
        assertEquals(TestEnum.class, ClassUtil.findEnumType(EnumSet.allOf(TestEnum.class)));
        assertEquals(TestEnum.class, ClassUtil.findEnumType(new EnumMap<TestEnum,Integer>(TestEnum.class)));
    }

// com.fasterxml.jackson.databind.util.TestClassUtil::testDescs
    public void testDescs()
    {
        final String exp = String.class.getName();
        assertEquals(exp, ClassUtil.getClassDescription("foo"));
        assertEquals(exp, ClassUtil.getClassDescription(String.class));
    }

// com.fasterxml.jackson.databind.util.TestObjectBuffer::testUntyped
    public void testUntyped()
    {
        _testObjectBuffer(null);
    }

// com.fasterxml.jackson.databind.util.TestObjectBuffer::testTyped
    public void testTyped()
    {
        _testObjectBuffer(Integer.class);
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

// com.fasterxml.jackson.databind.util.TestTokenBuffer::testSimpleObject
    public void testSimpleObject() throws IOException
    {
        TokenBuffer buf = new TokenBuffer(null, false);

        
        assertTrue(buf.getOutputContext().inRoot());
        buf.writeStartObject();
        assertTrue(buf.getOutputContext().inObject());
        buf.writeEndObject();
        assertTrue(buf.getOutputContext().inRoot());

        JsonParser jp = buf.asParser();
        assertNull(jp.getCurrentToken());
        assertTrue(jp.getParsingContext().inRoot());
        assertToken(JsonToken.START_OBJECT, jp.nextToken());
        assertTrue(jp.getParsingContext().inObject());
        assertToken(JsonToken.END_OBJECT, jp.nextToken());
        assertTrue(jp.getParsingContext().inRoot());
        assertNull(jp.nextToken());
        jp.close();
        buf.close();

        
        buf = new TokenBuffer(null, false);
        buf.writeStartObject();
        buf.writeNumberField("num", 1.25);
        buf.writeEndObject();

        jp = buf.asParser();
        assertNull(jp.getCurrentToken());
        assertToken(JsonToken.START_OBJECT, jp.nextToken());
        assertNull(jp.getCurrentName());
        assertToken(JsonToken.FIELD_NAME, jp.nextToken());
        assertEquals("num", jp.getCurrentName());
        
        jp.overrideCurrentName("bah");
        assertEquals("bah", jp.getCurrentName());
        
        assertToken(JsonToken.VALUE_NUMBER_FLOAT, jp.nextToken());
        assertEquals(1.25, jp.getDoubleValue());
        
        assertEquals("bah", jp.getCurrentName());
        assertToken(JsonToken.END_OBJECT, jp.nextToken());
        
        assertNull(jp.getCurrentName());
        assertNull(jp.nextToken());
        jp.close();
        buf.close();
    }

// com.fasterxml.jackson.databind.util.TestTokenBuffer::testWithJSONSampleDoc
    public void testWithJSONSampleDoc() throws Exception
    {
        
        JsonParser jp = createParserUsingReader(SAMPLE_DOC_JSON_SPEC);
        TokenBuffer tb = new TokenBuffer(null, false);
        while (jp.nextToken() != null) {
            tb.copyCurrentEvent(jp);
        }

        
        verifyJsonSpecSampleDoc(tb.asParser(), false);

        
        verifyJsonSpecSampleDoc(tb.asParser(), true);
        tb.close();
        jp.close();
    }

// com.fasterxml.jackson.databind.util.TestTokenBuffer::testAppend
    public void testAppend() throws IOException
    {
        TokenBuffer buf1 = new TokenBuffer(null, false);
        buf1.writeStartObject();
        buf1.writeFieldName("a");
        buf1.writeBoolean(true);
        
        TokenBuffer buf2 = new TokenBuffer(null, false);
        buf2.writeFieldName("b");
        buf2.writeNumber(13);
        buf2.writeEndObject();
        
        buf1.append(buf2);
        
        
        JsonParser jp = buf1.asParser();
        assertToken(JsonToken.START_OBJECT, jp.nextToken());
        assertToken(JsonToken.FIELD_NAME, jp.nextToken());
        assertEquals("a", jp.getCurrentName());
        assertToken(JsonToken.VALUE_TRUE, jp.nextToken());
        assertToken(JsonToken.FIELD_NAME, jp.nextToken());
        assertEquals("b", jp.getCurrentName());
        assertToken(JsonToken.VALUE_NUMBER_INT, jp.nextToken());
        assertEquals(13, jp.getIntValue());
        assertToken(JsonToken.END_OBJECT, jp.nextToken());
        jp.close();
        buf1.close();
        buf2.close();
    }

// com.fasterxml.jackson.databind.util.TestTokenBuffer::testWithUUID
    public void testWithUUID() throws IOException
    {
        for (String value : new String[] {
                "00000007-0000-0000-0000-000000000000",
                "76e6d183-5f68-4afa-b94a-922c1fdb83f8",
                "540a88d1-e2d8-4fb1-9396-9212280d0a7f",
                "2c9e441d-1cd0-472d-9bab-69838f877574",
                "591b2869-146e-41d7-8048-e8131f1fdec5",
                "82994ac2-7b23-49f2-8cc5-e24cf6ed77be",
        }) {
            TokenBuffer buf = new TokenBuffer(MAPPER, false); 
            UUID uuid = UUID.fromString(value);
            MAPPER.writeValue(buf, uuid);
            buf.close();
    
            
            UUID out = MAPPER.readValue(buf.asParser(), UUID.class);
            assertEquals(uuid.toString(), out.toString());

            
            JsonParser jp = buf.asParser();
            assertEquals(JsonToken.VALUE_STRING, jp.nextToken());
            String str = jp.getText();
            assertEquals(value, str);
            jp.close();
        }
    }

// com.fasterxml.jackson.databind.util.TestTokenBuffer::testOutputContext
    public void testOutputContext() throws IOException
    {
        TokenBuffer buf = new TokenBuffer(null, false); 
        StringWriter w = new StringWriter();
        JsonGenerator gen = MAPPER.getFactory().createGenerator(w);
 
        

        buf.writeStartArray();
        gen.writeStartArray();
        _verifyOutputContext(buf, gen);
        
        buf.writeStartObject();
        gen.writeStartObject();
        _verifyOutputContext(buf, gen);
        
        buf.writeFieldName("a");
        gen.writeFieldName("a");
        _verifyOutputContext(buf, gen);

        buf.writeNumber(1);
        gen.writeNumber(1);
        _verifyOutputContext(buf, gen);

        buf.writeFieldName("b");
        gen.writeFieldName("b");
        _verifyOutputContext(buf, gen);

        buf.writeStartObject();
        gen.writeStartObject();
        _verifyOutputContext(buf, gen);
        
        buf.writeFieldName("c");
        gen.writeFieldName("c");
        _verifyOutputContext(buf, gen);

        buf.writeNumber(2);
        gen.writeNumber(2);
        _verifyOutputContext(buf, gen);

        buf.writeEndObject();
        gen.writeEndObject();
        _verifyOutputContext(buf, gen);

        buf.writeEndObject();
        gen.writeEndObject();
        _verifyOutputContext(buf, gen);

        buf.writeEndArray();
        gen.writeEndArray();
        _verifyOutputContext(buf, gen);
        
        buf.close();
        gen.close();
    }

// com.fasterxml.jackson.databind.util.TestTokenBuffer::testWithJsonParserSequenceSimple
    public void testWithJsonParserSequenceSimple() throws IOException
    {
        
        TokenBuffer buf = new TokenBuffer(null, false);
        buf.writeStartArray();
        buf.writeString("test");
        JsonParser jp = createParserUsingReader("[ true, null ]");
        
        JsonParserSequence seq = JsonParserSequence.createFlattened(buf.asParser(), jp);
        assertEquals(2, seq.containedParsersCount());

        assertFalse(jp.isClosed());
        
        assertFalse(seq.hasCurrentToken());
        assertNull(seq.getCurrentToken());
        assertNull(seq.getCurrentName());

        assertToken(JsonToken.START_ARRAY, seq.nextToken());
        assertToken(JsonToken.VALUE_STRING, seq.nextToken());
        assertEquals("test", seq.getText());
        
        
        assertToken(JsonToken.START_ARRAY, seq.nextToken());
        assertToken(JsonToken.VALUE_TRUE, seq.nextToken());
        assertToken(JsonToken.VALUE_NULL, seq.nextToken());
        assertToken(JsonToken.END_ARRAY, seq.nextToken());

        

        
        assertNull(seq.nextToken());
        
        assertNull(seq.nextToken());

        
        assertTrue(jp.isClosed());
        jp.close();
        buf.close();
        seq.close();
    }

// com.fasterxml.jackson.databind.util.TestTokenBuffer::testWithMultipleJsonParserSequences
    public void testWithMultipleJsonParserSequences() throws IOException
    {
        TokenBuffer buf1 = new TokenBuffer(null, false);
        buf1.writeStartArray();
        TokenBuffer buf2 = new TokenBuffer(null, false);
        buf2.writeString("a");
        TokenBuffer buf3 = new TokenBuffer(null, false);
        buf3.writeNumber(13);
        TokenBuffer buf4 = new TokenBuffer(null, false);
        buf4.writeEndArray();

        JsonParserSequence seq1 = JsonParserSequence.createFlattened(buf1.asParser(), buf2.asParser());
        assertEquals(2, seq1.containedParsersCount());
        JsonParserSequence seq2 = JsonParserSequence.createFlattened(buf3.asParser(), buf4.asParser());
        assertEquals(2, seq2.containedParsersCount());
        JsonParserSequence combo = JsonParserSequence.createFlattened(seq1, seq2);
        
        assertEquals(4, combo.containedParsersCount());

        assertToken(JsonToken.START_ARRAY, combo.nextToken());
        assertToken(JsonToken.VALUE_STRING, combo.nextToken());
        assertEquals("a", combo.getText());
        assertToken(JsonToken.VALUE_NUMBER_INT, combo.nextToken());
        assertEquals(13, combo.getIntValue());
        assertToken(JsonToken.END_ARRAY, combo.nextToken());
        assertNull(combo.nextToken());        
        buf1.close();
        buf2.close();
        buf3.close();
        buf4.close();
    }

// com.fasterxml.jackson.databind.util.TestTokenBuffer::testRawValues
    public void testRawValues() throws Exception
    {
        final String RAW = "{\"a\":1}";
        TokenBuffer buf = new TokenBuffer(null, false);
        buf.writeRawValue(RAW);
        
        JsonParser p = buf.asParser();
        assertToken(JsonToken.VALUE_EMBEDDED_OBJECT, p.nextToken());
        assertEquals(RawValue.class, p.getEmbeddedObject().getClass());
        assertNull(p.nextToken());
        p.close();
        buf.close();

        
        assertEquals(RAW, MAPPER.writeValueAsString(buf));
    }

// com.fasterxml.jackson.databind.views.TestViewDeserialization::testSimple
    public void testSimple() throws Exception
    {
        
        Bean bean = mapper
                .readValue("{\"a\":3, \"aa\":\"foo\", \"b\": 9 }", Bean.class);
        assertEquals(3, bean.a);
        assertEquals("foo", bean.aa);
        assertEquals(9, bean.b);
        
        
        bean = mapper.readerWithView(ViewAA.class)
                .forType(Bean.class)
                .readValue("{\"a\":3, \"aa\":\"foo\", \"b\": 9 }");
        
        assertEquals(3, bean.a);
        assertEquals("foo", bean.aa);
        
        assertEquals(0, bean.b);

        bean = mapper.readerWithView(ViewA.class)
                .forType(Bean.class)
                .readValue("{\"a\":1, \"aa\":\"x\", \"b\": 3 }");
        assertEquals(1, bean.a);
        assertNull(bean.aa);
        assertEquals(0, bean.b);
        
        bean = mapper.readerFor(Bean.class)
                .withView(ViewB.class)
                .readValue("{\"a\":-3, \"aa\":\"y\", \"b\": 2 }");
        assertEquals(0, bean.a);
        assertEquals("y", bean.aa);
        assertEquals(2, bean.b);
    }

// com.fasterxml.jackson.databind.views.TestViewDeserialization::testWithoutDefaultInclusion
    public void testWithoutDefaultInclusion() throws Exception
    {
        
        DefaultsBean bean = mapper
                .readValue("{\"a\":3, \"b\": 9 }", DefaultsBean.class);
        assertEquals(3, bean.a);
        assertEquals(9, bean.b);

        ObjectMapper myMapper = new ObjectMapper();
        myMapper.disable(MapperFeature.DEFAULT_VIEW_INCLUSION);

        
        bean = myMapper.readerWithView(ViewAA.class)
                .forType(DefaultsBean.class)
                .readValue("{\"a\":1, \"b\": 2 }");
        
        assertEquals(0, bean.a);
        assertEquals(2, bean.b);
    }

// com.fasterxml.jackson.databind.views.TestViewSerialization::testSimple
    public void testSimple() throws IOException
    {
        StringWriter sw = new StringWriter();
        ObjectMapper mapper = new ObjectMapper();
        
        Bean bean = new Bean();
        Map<String,Object> map = writeAndMap(mapper, bean);
        assertEquals(3, map.size());

        
        sw = new StringWriter();
        mapper.writerWithView(ViewA.class).writeValue(sw, bean);
        map = mapper.readValue(sw.toString(), Map.class);
        assertEquals(1, map.size());
        assertEquals("1", map.get("a"));

        
        sw = new StringWriter();
        mapper.writerWithView(ViewAA.class).writeValue(sw, bean);
        map = mapper.readValue(sw.toString(), Map.class);
        assertEquals(2, map.size());
        assertEquals("1", map.get("a"));
        assertEquals("2", map.get("aa"));

        
        String json = mapper.writerWithView(ViewB.class).writeValueAsString(bean);
        map = mapper.readValue(json, Map.class);
        assertEquals(2, map.size());
        assertEquals("2", map.get("aa"));
        assertEquals("3", map.get("b"));

        
        json = mapper.writerWithView(ViewBB.class).writeValueAsString(bean);
        map = mapper.readValue(json, Map.class);
        assertEquals(2, map.size());
        assertEquals("2", map.get("aa"));
        assertEquals("3", map.get("b"));
    }

// com.fasterxml.jackson.databind.views.TestViewSerialization::testDefaultExclusion
    public void testDefaultExclusion() throws IOException
    {
        MixedBean bean = new MixedBean();
        StringWriter sw = new StringWriter();

        ObjectMapper mapper = new ObjectMapper();
        
        mapper.writerWithView(ViewA.class).writeValue(sw, bean);
        Map<String,Object> map = mapper.readValue(sw.toString(), Map.class);
        assertEquals(2, map.size());
        assertEquals("1", map.get("a"));
        assertEquals("2", map.get("b"));

        
        mapper = new ObjectMapper();
        mapper.configure(MapperFeature.DEFAULT_VIEW_INCLUSION, false);
        
        String json = mapper.writerWithView(ViewA.class).writeValueAsString(bean);
        map = mapper.readValue(json, Map.class);
        assertEquals(1, map.size());
        assertEquals("1", map.get("a"));
        assertNull(map.get("b"));
    }

// com.fasterxml.jackson.databind.views.TestViewSerialization::testImplicitAutoDetection
    public void testImplicitAutoDetection() throws Exception
    {
    	assertEquals("{\"a\":1}", serializeAsString(new ImplicitBean()));
    }

// com.fasterxml.jackson.databind.views.TestViewSerialization::testVisibility
    public void testVisibility() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        VisibilityBean bean = new VisibilityBean();
        
        String json = mapper.writerWithView(Object.class).writeValueAsString(bean);
        
        assertEquals("{\"id\":\"id\"}", json);
    }

// com.fasterxml.jackson.databind.views.TestViewSerialization::test868
    public void test868() throws IOException
    {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setSerializationInclusion(JsonInclude.Include.NON_DEFAULT);
        String json = mapper.writerWithView(OtherView.class).writeValueAsString(new Foo());
        assertEquals(json, "{}");
    }

// com.fasterxml.jackson.databind.views.TestViewsSerialization2::testDataBindingUsage
  public void testDataBindingUsage( ) throws Exception
  {
    ObjectMapper objectMapper = createObjectMapper( null );
    String result = serializeWithObjectMapper(new ComplexTestData( ), Views.View.class, objectMapper );
    assertEquals(-1, result.indexOf( "nameHidden" ));
  }

// com.fasterxml.jackson.databind.views.TestViewsSerialization2::testDataBindingUsageWithoutView
  public void testDataBindingUsageWithoutView( ) throws Exception
  {
    ObjectMapper objectMapper = createObjectMapper( null );
    String json = serializeWithObjectMapper(new ComplexTestData( ), null, objectMapper);
    assertTrue(json.indexOf( "nameHidden" ) > 0);
  }

// com.fasterxml.jackson.databind.views.ViewsWithSchemaTest::testSchemaWithViews
    public void testSchemaWithViews() throws Exception
    {
        ListingVisitor v = new ListingVisitor();
        MAPPER.writerWithView(ViewBC.class)
            .acceptJsonFormatVisitor(POJO.class, v);
        assertEquals(Arrays.asList("b", "c"), v.names);

        v = new ListingVisitor();
        MAPPER.writerWithView(ViewAB.class)
            .acceptJsonFormatVisitor(POJO.class, v);
        assertEquals(Arrays.asList("a", "b"), v.names);
    }

// com.fasterxml.jackson.databind.views.ViewsWithSchemaTest::testSchemaWithoutViews
    public void testSchemaWithoutViews() throws Exception
    {
        ListingVisitor v = new ListingVisitor();
        MAPPER.acceptJsonFormatVisitor(POJO.class, v);
        assertEquals(Arrays.asList("a", "b", "c"), v.names);
    }

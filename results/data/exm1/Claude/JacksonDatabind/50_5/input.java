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
// com.fasterxml.jackson.databind.type.TestGenericsBounded::test
    public void test() throws Exception
    {
        AnnotatedValueSimple<Integer> item = new AnnotatedValueSimple<Integer>(5);
        CbFailing<AnnotatedValueSimple<Integer>, Integer> codebook = new CbFailing<AnnotatedValueSimple<Integer>, Integer>(item);
        String json = MAPPER.writeValueAsString(codebook);
        assertNotNull(json);
    }

// com.fasterxml.jackson.databind.type.TestLocalType609::testLocalPartialType609
    public void testLocalPartialType609() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        
        EntityContainer input = new EntityContainer(); 
        input.entity = new RuleForm(12);
        String json = mapper.writeValueAsString(input);
        
        EntityContainer output = mapper.readValue(json, EntityContainer.class);
        assertEquals(12, output.getEntity().value);
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
            verifyException(e, "Can not create TypeBindings for class java.util.Map");
        }

        try {
            
            tf.constructParametrizedType(SingleArgGeneric.class, SingleArgGeneric.class, strC, strC);
        } catch (IllegalArgumentException e) {
            verifyException(e, "Can not create TypeBindings for class ");
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

// com.fasterxml.jackson.databind.type.TypeAliasesTest::testAliasResolutionIssue743
    public void testAliasResolutionIssue743() throws Exception
    {
        String s3 = "{\"dataObj\" : [ \"one\", \"two\", \"three\" ] }";
        ObjectMapper m = new ObjectMapper();
   
        Child.ChildData d = m.readValue(s3, Child.ChildData.class);
        assertNotNull(d.dataObj);
        assertEquals(3, d.dataObj.size());
    }

// com.fasterxml.jackson.databind.util.TestTokenBuffer::testSimpleWrites
    public void testSimpleWrites() throws IOException
    {
        TokenBuffer buf = new TokenBuffer(null, false); 

        
        JsonParser p = buf.asParser();
        assertNull(p.getCurrentToken());
        assertNull(p.nextToken());
        p.close();

        
        buf.writeString("abc");

        
        p = buf.asParser();
        assertNull(p.getCurrentToken());
        assertToken(JsonToken.VALUE_STRING, p.nextToken());
        assertEquals("abc", p.getText());
        assertNull(p.nextToken());
        p.close();

        
        buf.writeNumber(13);
        p = buf.asParser();
        assertNull(p.getCurrentToken());
        assertToken(JsonToken.VALUE_STRING, p.nextToken());
        assertToken(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertEquals(13, p.getIntValue());
        assertNull(p.nextToken());
        p.close();
        buf.close();
    }

// com.fasterxml.jackson.databind.util.TestTokenBuffer::testParentContext
    public void testParentContext() throws IOException
    {
        TokenBuffer buf = new TokenBuffer(null, false); 
        buf.writeStartObject();
        buf.writeFieldName("b");
        buf.writeStartObject();
        buf.writeFieldName("c");
        
        assertEquals("b", buf.getOutputContext().getParent().getCurrentName());
        buf.writeString("cval");
        buf.writeEndObject();
        buf.writeEndObject();
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

        JsonParser p = buf.asParser();
        assertNull(p.getCurrentToken());
        assertTrue(p.getParsingContext().inRoot());
        assertToken(JsonToken.START_ARRAY, p.nextToken());
        assertTrue(p.getParsingContext().inArray());
        assertToken(JsonToken.END_ARRAY, p.nextToken());
        assertTrue(p.getParsingContext().inRoot());
        assertNull(p.nextToken());
        p.close();
        buf.close();

        
        buf = new TokenBuffer(null, false);
        buf.writeStartArray();
        buf.writeBoolean(true);
        buf.writeNull();
        buf.writeEndArray();
        p = buf.asParser();
        assertToken(JsonToken.START_ARRAY, p.nextToken());
        assertToken(JsonToken.VALUE_TRUE, p.nextToken());
        assertTrue(p.getBooleanValue());
        assertToken(JsonToken.VALUE_NULL, p.nextToken());
        assertToken(JsonToken.END_ARRAY, p.nextToken());
        assertNull(p.nextToken());
        p.close();
        buf.close();

        
        buf = new TokenBuffer(null, false);
        buf.writeStartArray();
        buf.writeStartArray();
        buf.writeBinary(new byte[3]);
        buf.writeEndArray();
        buf.writeEndArray();
        p = buf.asParser();
        assertToken(JsonToken.START_ARRAY, p.nextToken());
        assertToken(JsonToken.START_ARRAY, p.nextToken());
        
        assertToken(JsonToken.VALUE_EMBEDDED_OBJECT, p.nextToken());
        Object ob = p.getEmbeddedObject();
        assertNotNull(ob);
        assertTrue(ob instanceof byte[]);
        assertEquals(3, ((byte[]) ob).length);
        assertToken(JsonToken.END_ARRAY, p.nextToken());
        assertToken(JsonToken.END_ARRAY, p.nextToken());
        assertNull(p.nextToken());
        p.close();
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

        JsonParser p = buf.asParser();
        assertNull(p.getCurrentToken());
        assertTrue(p.getParsingContext().inRoot());
        assertToken(JsonToken.START_OBJECT, p.nextToken());
        assertTrue(p.getParsingContext().inObject());
        assertToken(JsonToken.END_OBJECT, p.nextToken());
        assertTrue(p.getParsingContext().inRoot());
        assertNull(p.nextToken());
        p.close();
        buf.close();

        
        buf = new TokenBuffer(null, false);
        buf.writeStartObject();
        buf.writeNumberField("num", 1.25);
        buf.writeEndObject();

        p = buf.asParser();
        assertNull(p.getCurrentToken());
        assertToken(JsonToken.START_OBJECT, p.nextToken());
        assertNull(p.getCurrentName());
        assertToken(JsonToken.FIELD_NAME, p.nextToken());
        assertEquals("num", p.getCurrentName());
        
        p.overrideCurrentName("bah");
        assertEquals("bah", p.getCurrentName());
        
        assertToken(JsonToken.VALUE_NUMBER_FLOAT, p.nextToken());
        assertEquals(1.25, p.getDoubleValue());
        
        assertEquals("bah", p.getCurrentName());
        assertToken(JsonToken.END_OBJECT, p.nextToken());
        
        assertNull(p.getCurrentName());
        assertNull(p.nextToken());
        p.close();
        buf.close();
    }

// com.fasterxml.jackson.databind.util.TestTokenBuffer::testWithJSONSampleDoc
    public void testWithJSONSampleDoc() throws Exception
    {
        
        JsonParser p = createParserUsingReader(SAMPLE_DOC_JSON_SPEC);
        TokenBuffer tb = new TokenBuffer(null, false);
        while (p.nextToken() != null) {
            tb.copyCurrentEvent(p);
        }

        
        verifyJsonSpecSampleDoc(tb.asParser(), false);

        
        verifyJsonSpecSampleDoc(tb.asParser(), true);
        tb.close();
        p.close();
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
        
        
        JsonParser p = buf1.asParser();
        assertToken(JsonToken.START_OBJECT, p.nextToken());
        assertToken(JsonToken.FIELD_NAME, p.nextToken());
        assertEquals("a", p.getCurrentName());
        assertToken(JsonToken.VALUE_TRUE, p.nextToken());
        assertToken(JsonToken.FIELD_NAME, p.nextToken());
        assertEquals("b", p.getCurrentName());
        assertToken(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertEquals(13, p.getIntValue());
        assertToken(JsonToken.END_OBJECT, p.nextToken());
        p.close();
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

            
            JsonParser p = buf.asParser();
            assertEquals(JsonToken.VALUE_STRING, p.nextToken());
            String str = p.getText();
            assertEquals(value, str);
            p.close();
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

// com.fasterxml.jackson.databind.util.TestTokenBuffer::testParentSiblingContext
    public void testParentSiblingContext() throws IOException
    {
        TokenBuffer buf = new TokenBuffer(null, false); 

        
        
        buf.writeStartObject();
        buf.writeFieldName("a");
        buf.writeStartObject();
        buf.writeEndObject();

        buf.writeFieldName("b");
        buf.writeStartObject();
        buf.writeFieldName("c");
        
        assertEquals("b", buf.getOutputContext().getParent().getCurrentName());
        buf.writeString("cval");
        buf.writeEndObject();
        buf.writeEndObject();
        buf.close();
    }

// com.fasterxml.jackson.databind.util.TestTokenBuffer::testWithJsonParserSequenceSimple
    public void testWithJsonParserSequenceSimple() throws IOException
    {
        
        TokenBuffer buf = new TokenBuffer(null, false);
        buf.writeStartArray();
        buf.writeString("test");
        JsonParser p = createParserUsingReader("[ true, null ]");
        
        JsonParserSequence seq = JsonParserSequence.createFlattened(buf.asParser(), p);
        assertEquals(2, seq.containedParsersCount());

        assertFalse(p.isClosed());
        
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

        
        assertTrue(p.isClosed());
        p.close();
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
    public void testImplicitAutoDetection() {}

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

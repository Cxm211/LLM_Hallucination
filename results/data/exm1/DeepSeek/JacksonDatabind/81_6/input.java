// buggy code
    public JavaType refineSerializationType(final MapperConfig<?> config,
            final Annotated a, final JavaType baseType) throws JsonMappingException
    {
        JavaType type = baseType;
        final TypeFactory tf = config.getTypeFactory();

        final JsonSerialize jsonSer = _findAnnotation(a, JsonSerialize.class);
        
        // Ok: start by refining the main type itself; common to all types

        final Class<?> serClass = (jsonSer == null) ? null : _classIfExplicit(jsonSer.as());
        if (serClass != null) {
            if (type.hasRawClass(serClass)) {
                // 30-Nov-2015, tatu: As per [databind#1023], need to allow forcing of
                //    static typing this way
                type = type.withStaticTyping();
            } else {
                Class<?> currRaw = type.getRawClass();
                try {
                    // 11-Oct-2015, tatu: For deser, we call `TypeFactory.constructSpecializedType()`,
                    //   may be needed here too in future?
                    if (serClass.isAssignableFrom(currRaw)) { // common case
                        type = tf.constructGeneralizedType(type, serClass);
                    } else if (currRaw.isAssignableFrom(serClass)) { // specialization, ok as well
                        type = tf.constructSpecializedType(type, serClass);
                        // 27-Apr-2017, tatu: [databind#1592] ignore primitive<->wrapper refinements
                    } else {
                        throw new JsonMappingException(null,
                                String.format("Can not refine serialization type %s into %s; types not related",
                                        type, serClass.getName()));
                    }
                } catch (IllegalArgumentException iae) {
                    throw new JsonMappingException(null,
                            String.format("Failed to widen type %s with annotation (value %s), from '%s': %s",
                                    type, serClass.getName(), a.getName(), iae.getMessage()),
                                    iae);
                }
            }
        }
        // Then further processing for container types

        // First, key type (for Maps, Map-like types):
        if (type.isMapLikeType()) {
            JavaType keyType = type.getKeyType();
            final Class<?> keyClass = (jsonSer == null) ? null : _classIfExplicit(jsonSer.keyAs());
            if (keyClass != null) {
                if (keyType.hasRawClass(keyClass)) {
                    keyType = keyType.withStaticTyping();
                } else {
                    Class<?> currRaw = keyType.getRawClass();
                    try {
                        // 19-May-2016, tatu: As per [databind#1231], [databind#1178] may need to actually
                        //   specialize (narrow) type sometimes, even if more commonly opposite
                        //   is needed.
                        if (keyClass.isAssignableFrom(currRaw)) { // common case
                            keyType = tf.constructGeneralizedType(keyType, keyClass);
                        } else if (currRaw.isAssignableFrom(keyClass)) { // specialization, ok as well
                            keyType = tf.constructSpecializedType(keyType, keyClass);
                            // 27-Apr-2017, tatu: [databind#1592] ignore primitive<->wrapper refinements
                        } else {
                            throw new JsonMappingException(null,
                                    String.format("Can not refine serialization key type %s into %s; types not related",
                                            keyType, keyClass.getName()));
                        }
                    } catch (IllegalArgumentException iae) {
                        throw new JsonMappingException(null,
                                String.format("Failed to widen key type of %s with concrete-type annotation (value %s), from '%s': %s",
                                        type, keyClass.getName(), a.getName(), iae.getMessage()),
                                        iae);
                    }
                }
                type = ((MapLikeType) type).withKeyType(keyType);
            }
        }

        JavaType contentType = type.getContentType();
        if (contentType != null) { // collection[like], map[like], array, reference
            // And then value types for all containers:
           final Class<?> contentClass = (jsonSer == null) ? null : _classIfExplicit(jsonSer.contentAs());
           if (contentClass != null) {
               if (contentType.hasRawClass(contentClass)) {
                   contentType = contentType.withStaticTyping();
               } else {
                   // 03-Apr-2016, tatu: As per [databind#1178], may need to actually
                   //   specialize (narrow) type sometimes, even if more commonly opposite
                   //   is needed.
                   Class<?> currRaw = contentType.getRawClass();
                   try {
                       if (contentClass.isAssignableFrom(currRaw)) { // common case
                           contentType = tf.constructGeneralizedType(contentType, contentClass);
                       } else if (currRaw.isAssignableFrom(contentClass)) { // specialization, ok as well
                           contentType = tf.constructSpecializedType(contentType, contentClass);
                           // 27-Apr-2017, tatu: [databind#1592] ignore primitive<->wrapper refinements
                       } else {
                           throw new JsonMappingException(null,
                                   String.format("Can not refine serialization content type %s into %s; types not related",
                                           contentType, contentClass.getName()));
                       }
                   } catch (IllegalArgumentException iae) { // shouldn't really happen
                       throw new JsonMappingException(null,
                               String.format("Internal error: failed to refine value type of %s with concrete-type annotation (value %s), from '%s': %s",
                                       type, contentClass.getName(), a.getName(), iae.getMessage()),
                                       iae);
                   }
               }
               type = type.withContentType(contentType);
           }
        }
        return type;
    }

    public JavaType refineDeserializationType(final MapperConfig<?> config,
            final Annotated a, final JavaType baseType) throws JsonMappingException
    {
        JavaType type = baseType;
        final TypeFactory tf = config.getTypeFactory();

        final JsonDeserialize jsonDeser = _findAnnotation(a, JsonDeserialize.class);
        
        // Ok: start by refining the main type itself; common to all types
        final Class<?> valueClass = (jsonDeser == null) ? null : _classIfExplicit(jsonDeser.as());
        if ((valueClass != null) && !type.hasRawClass(valueClass)) {
            try {
                type = tf.constructSpecializedType(type, valueClass);
            } catch (IllegalArgumentException iae) {
                throw new JsonMappingException(null,
                        String.format("Failed to narrow type %s with annotation (value %s), from '%s': %s",
                                type, valueClass.getName(), a.getName(), iae.getMessage()),
                                iae);
            }
        }
        // Then further processing for container types

        // First, key type (for Maps, Map-like types):
        if (type.isMapLikeType()) {
            JavaType keyType = type.getKeyType();
            final Class<?> keyClass = (jsonDeser == null) ? null : _classIfExplicit(jsonDeser.keyAs());
            if (keyClass != null) {
                try {
                    keyType = tf.constructSpecializedType(keyType, keyClass);
                    type = ((MapLikeType) type).withKeyType(keyType);
                } catch (IllegalArgumentException iae) {
                    throw new JsonMappingException(null,
                            String.format("Failed to narrow key type of %s with concrete-type annotation (value %s), from '%s': %s",
                                    type, keyClass.getName(), a.getName(), iae.getMessage()),
                                    iae);
                }
            }
        }
        JavaType contentType = type.getContentType();
        if (contentType != null) { // collection[like], map[like], array, reference
            // And then value types for all containers:
            final Class<?> contentClass = (jsonDeser == null) ? null : _classIfExplicit(jsonDeser.contentAs());
            if (contentClass != null) {
                try {
                    contentType = tf.constructSpecializedType(contentType, contentClass);
                    type = type.withContentType(contentType);
                } catch (IllegalArgumentException iae) {
                    throw new JsonMappingException(null,
                            String.format("Failed to narrow value type of %s with concrete-type annotation (value %s), from '%s': %s",
                                    type, contentClass.getName(), a.getName(), iae.getMessage()),
                            iae);
                }
            }
        }
        return type;
    }

    protected StdTypeResolverBuilder _constructNoTypeResolverBuilder() {
        return StdTypeResolverBuilder.noTypeInfoBuilder();
    }

// relevant test
// com.fasterxml.jackson.databind.type.DeprecatedConstructType1456Test::testGenericResolutionUsingDeprecated
    public void testGenericResolutionUsingDeprecated() throws Exception
    {
        Method proceed = BaseController.class.getMethod("process", BaseEntity.class);
        Type entityType = proceed.getGenericParameterTypes()[0];

        JavaType resolvedType = MAPPER.getTypeFactory().constructType(entityType, ImplController.class);
        assertEquals(ImplEntity.class, resolvedType.getRawClass());
    }

// com.fasterxml.jackson.databind.type.DeprecatedConstructType1456Test::testGenericParameterViaClass
    public void testGenericParameterViaClass() throws Exception
    {
        BeanDescription desc = MAPPER.getDeserializationConfig().introspect(
                MAPPER.constructType(ImplController.class));
        AnnotatedClass ac = desc.getClassInfo();
        AnnotatedMethod m = ac.findMethod("process", new Class<?>[] { BaseEntity.class });
        assertNotNull(m);
        assertEquals(1, m.getParameterCount());
        AnnotatedParameter param = m.getParameter(0);
        assertEquals(ImplEntity.class, param.getType().getRawClass());
    }

// com.fasterxml.jackson.databind.type.LocalTypeTest::testLocalPartialType609
    public void testLocalPartialType609() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        
        EntityContainer input = new EntityContainer(); 
        input.entity = new RuleForm(12);
        String json = mapper.writeValueAsString(input);
        
        EntityContainer output = mapper.readValue(json, EntityContainer.class);
        assertEquals(12, output.getEntity().value);
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

// com.fasterxml.jackson.databind.type.RecursiveTypeTest::testRecursiveType
    public void testRecursiveType()
    {
        TypeFactory tf = TypeFactory.defaultInstance();
        JavaType type = tf.constructType(HashTree.class);
        assertNotNull(type);
    }

// com.fasterxml.jackson.databind.type.RecursiveTypeTest::testRecursivePair
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

// com.fasterxml.jackson.databind.type.RecursiveTypeTest::testJavaTypeToString
    public void testJavaTypeToString() throws Exception
    {
        TypeFactory tf = objectMapper().getTypeFactory();
        String desc = tf.constructType(DataDefinition.class).toString();
        assertNotNull(desc);
        
        if (!desc.contains("map type")) {
            fail("Description should contain 'map type', did not: "+desc);
        }
        if (!desc.contains("recursive type")) {
            fail("Description should contain 'recursive type', did not: "+desc);
        }
    }

// com.fasterxml.jackson.databind.type.TestAnnotatedClass::testFieldIntrospection
    public void testFieldIntrospection()
    {
        SerializationConfig config = MAPPER.getSerializationConfig();
        JavaType t = MAPPER.constructType(FieldBean.class);
        AnnotatedClass ac = AnnotatedClassResolver.resolve(config, t, config);
        
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
        AnnotatedClass ac = AnnotatedClassResolver.resolve(config, t, config);
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

// com.fasterxml.jackson.databind.type.TestGenericsBounded::test
    public void test() throws Exception
    {
        AnnotatedValueSimple<Integer> item = new AnnotatedValueSimple<Integer>(5);
        CbFailing<AnnotatedValueSimple<Integer>, Integer> codebook = new CbFailing<AnnotatedValueSimple<Integer>, Integer>(item);
        String json = MAPPER.writeValueAsString(codebook);
        assertNotNull(json);
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

// com.fasterxml.jackson.databind.type.TestTypeFactory::testRawMapType
    public void testRawMapType()
    {
        TypeFactory tf = TypeFactory.defaultInstance().withModifier(null); 

        JavaType type = tf.constructParametricType(Wrapper1297.class, Map.class);
        assertNotNull(type);
        assertEquals(Wrapper1297.class, type.getRawClass());
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

// com.fasterxml.jackson.databind.util.EnumValuesTest::testConstructFromName
    public void testConstructFromName() {
        SerializationConfig cfg = MAPPER.getSerializationConfig()
                .without(SerializationFeature.WRITE_ENUMS_USING_TO_STRING);
        Class<Enum<?>> enumClass = (Class<Enum<?>>)(Class<?>) ABC.class;
        EnumValues values = EnumValues.construct(cfg, enumClass);
        assertEquals("A", values.serializedValueFor(ABC.A).toString());
        assertEquals("B", values.serializedValueFor(ABC.B).toString());
        assertEquals("C", values.serializedValueFor(ABC.C).toString());
        assertEquals(3, values.values().size());
        assertEquals(3, values.internalMap().size());
    }

// com.fasterxml.jackson.databind.util.EnumValuesTest::testConstructWithToString
    public void testConstructWithToString() {
        SerializationConfig cfg = MAPPER.getSerializationConfig()
                .with(SerializationFeature.WRITE_ENUMS_USING_TO_STRING);
        Class<Enum<?>> enumClass = (Class<Enum<?>>)(Class<?>) ABC.class;
        EnumValues values = EnumValues.construct(cfg, enumClass);
        assertEquals("A", values.serializedValueFor(ABC.A).toString());
        assertEquals("b", values.serializedValueFor(ABC.B).toString());
        assertEquals("C", values.serializedValueFor(ABC.C).toString());
        assertEquals(3, values.values().size());
        assertEquals(3, values.internalMap().size());
    }

// com.fasterxml.jackson.databind.util.EnumValuesTest::testEnumResolver
    public void testEnumResolver()
    {
        EnumResolver enumRes = EnumResolver.constructUnsafeUsingToString(ABC.class, null);
        assertEquals(ABC.B, enumRes.getEnum(1));
        assertNull(enumRes.getEnum(-1));
        assertNull(enumRes.getEnum(3));
        assertEquals(2, enumRes.lastValidIndex());
        List<Enum<?>> enums = enumRes.getEnums();
        assertEquals(3, enums.size());
        assertEquals(ABC.A, enums.get(0));
        assertEquals(ABC.B, enums.get(1));
        assertEquals(ABC.C, enums.get(2));
    }

// com.fasterxml.jackson.databind.util.JSONPObjectTest::testU2028Escaped
  public void testU2028Escaped() throws IOException {
    String containsU2028 = String.format("This string contains %c char", '\u2028');
    JSONPObject jsonpObject = new JSONPObject(CALLBACK, containsU2028);
    String valueAsString = MAPPER.writeValueAsString(jsonpObject);
    assertFalse(valueAsString.contains("\u2028"));
  }

// com.fasterxml.jackson.databind.util.JSONPObjectTest::testU2029Escaped
  public void testU2029Escaped() throws IOException {
    String containsU2029 = String.format("This string contains %c char", '\u2029');
    JSONPObject jsonpObject = new JSONPObject(CALLBACK, containsU2029);
    String valueAsString = MAPPER.writeValueAsString(jsonpObject);
    assertFalse(valueAsString.contains("\u2029"));
  }

// com.fasterxml.jackson.databind.util.JSONPObjectTest::testU2030NotEscaped
  public void testU2030NotEscaped() throws IOException {
    String containsU2030 = String.format("This string contains %c char", '\u2030');
    JSONPObject jsonpObject = new JSONPObject(CALLBACK, containsU2030);
    String valueAsString = MAPPER.writeValueAsString(jsonpObject);
    assertTrue(valueAsString.contains("\u2030"));
  }

// com.fasterxml.jackson.databind.util.TestTokenBuffer::testBasicConfig
    public void testBasicConfig() throws IOException
    {
        TokenBuffer buf;

        buf = new TokenBuffer(MAPPER, false);
        assertEquals(MAPPER.version(), buf.version());
        assertSame(MAPPER, buf.getCodec());
        assertNotNull(buf.getOutputContext());
        assertFalse(buf.isClosed());

        buf.setCodec(null);
        assertNull(buf.getCodec());

        assertFalse(buf.isEnabled(JsonGenerator.Feature.ESCAPE_NON_ASCII));
        buf.enable(JsonGenerator.Feature.ESCAPE_NON_ASCII);
        assertTrue(buf.isEnabled(JsonGenerator.Feature.ESCAPE_NON_ASCII));
        buf.disable(JsonGenerator.Feature.ESCAPE_NON_ASCII);
        assertFalse(buf.isEnabled(JsonGenerator.Feature.ESCAPE_NON_ASCII));

        buf.close();
        assertTrue(buf.isClosed());
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

// com.fasterxml.jackson.databind.util.TestTokenBuffer::testSimpleNumberWrites
    public void testSimpleNumberWrites() throws IOException
    {
        TokenBuffer buf = new TokenBuffer(null, false);

        double[] values1 = new double[] {
                0.25, Double.NaN, -2.0, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY
        };
        float[] values2 = new float[] {
                Float.NEGATIVE_INFINITY,
                0.25f,
                Float.POSITIVE_INFINITY
        };

        for (double v : values1) {
            buf.writeNumber(v);
        }
        for (float v : values2) {
            buf.writeNumber(v);
        }

        JsonParser p = buf.asParser();
        assertNull(p.getCurrentToken());

        for (double v : values1) {
            assertToken(JsonToken.VALUE_NUMBER_FLOAT, p.nextToken());
            double actual = p.getDoubleValue();
            boolean expNan = Double.isNaN(v) || Double.isInfinite(v);
            assertEquals(expNan, p.isNaN());
            assertEquals(0, Double.compare(v, actual));
        }
        for (float v : values2) {
            assertToken(JsonToken.VALUE_NUMBER_FLOAT, p.nextToken());
            float actual = p.getFloatValue();
            boolean expNan = Float.isNaN(v) || Float.isInfinite(v);
            assertEquals(expNan, p.isNaN());
            assertEquals(0, Float.compare(v, actual));
        }
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

    
        
        String desc = tb.toString();
        assertNotNull(desc);
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

// com.fasterxml.jackson.databind.util.TestTokenBuffer::testBasicSerialize
    public void testBasicSerialize() throws IOException
    {
        TokenBuffer buf;

        
        buf = new TokenBuffer(MAPPER, false);
        assertEquals("", MAPPER.writeValueAsString(buf));
        buf.close();
        
        buf = new TokenBuffer(MAPPER, false);
        buf.writeStartArray();
        buf.writeBoolean(true);
        buf.writeBoolean(false);
        long l = 1L + Integer.MAX_VALUE;
        buf.writeNumber(l);
        buf.writeNumber((short) 4);
        buf.writeNumber(0.5);
        buf.writeEndArray();
        assertEquals(aposToQuotes("[true,false,"+l+",4,0.5]"), MAPPER.writeValueAsString(buf));
        buf.close();

        buf = new TokenBuffer(MAPPER, false);
        buf.writeStartObject();
        buf.writeFieldName(new SerializedString("foo"));
        buf.writeNull();
        buf.writeFieldName("bar");
        buf.writeNumber(BigInteger.valueOf(123));
        buf.writeFieldName("dec");
        buf.writeNumber(BigDecimal.valueOf(5).movePointLeft(2));
        assertEquals(aposToQuotes("{'foo':null,'bar':123,'dec':0.05}"), MAPPER.writeValueAsString(buf));
        buf.close();
    }

// com.fasterxml.jackson.databind.util.TestTokenBuffer::testWithJsonParserSequenceSimple
    public void testWithJsonParserSequenceSimple() throws IOException
    {
        
        TokenBuffer buf = new TokenBuffer(null, false);
        buf.writeStartArray();
        buf.writeString("test");
        JsonParser p = createParserUsingReader("[ true, null ]");
        
        JsonParserSequence seq = JsonParserSequence.createFlattened(false, buf.asParser(), p);
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

        JsonParserSequence seq1 = JsonParserSequence.createFlattened(false, buf1.asParser(), buf2.asParser());
        assertEquals(2, seq1.containedParsersCount());
        JsonParserSequence seq2 = JsonParserSequence.createFlattened(false, buf3.asParser(), buf4.asParser());
        assertEquals(2, seq2.containedParsersCount());
        JsonParserSequence combo = JsonParserSequence.createFlattened(false, seq1, seq2);
        
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

// com.fasterxml.jackson.databind.views.DefaultViewTest::testDeserialization
    public void testDeserialization() throws IOException
    {
        final String JSON = aposToQuotes("{'a':1,'b':2}");

        
        Defaulting result = MAPPER.readerFor(Defaulting.class)
                .readValue(JSON);
        assertEquals(result.a, 1);
        assertEquals(result.b, 2);

        
        result = MAPPER.readerFor(Defaulting.class)
                .withView(ViewA.class)
                .readValue(JSON);
        assertEquals(result.a, 1);
        assertEquals(result.b, 5);

        result = MAPPER.readerFor(Defaulting.class)
                .withView(ViewBB.class)
                .readValue(JSON);
        assertEquals(result.a, 3);
        assertEquals(result.b, 2);
    }

// com.fasterxml.jackson.databind.views.DefaultViewTest::testSerialization
    public void testSerialization() throws IOException
    {
        assertEquals(aposToQuotes("{'a':3,'b':5}"),
                MAPPER.writeValueAsString(new Defaulting()));

        assertEquals(aposToQuotes("{'a':3}"),
                MAPPER.writerWithView(ViewA.class)
                    .writeValueAsString(new Defaulting()));
        assertEquals(aposToQuotes("{'b':5}"),
                MAPPER.writerWithView(ViewB.class)
                    .writeValueAsString(new Defaulting()));
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

// com.fasterxml.jackson.databind.views.TestViewDeserialization::testWithCreatorAndViews
    public void testWithCreatorAndViews() throws Exception
    {
        ViewsAndCreatorBean result; 

        result = mapper.readerFor(ViewsAndCreatorBean.class)
                .withView(ViewA.class)
                .readValue(aposToQuotes("{'a':1,'b':2}"));
        assertEquals(1, result.a);
        assertEquals(0, result.b);

        result = mapper.readerFor(ViewsAndCreatorBean.class)
                .withView(ViewB.class)
                .readValue(aposToQuotes("{'a':1,'b':2}"));
        assertEquals(0, result.a);
        assertEquals(2, result.b);

        
        result = mapper.readerFor(ViewsAndCreatorBean.class)
                .withView(ViewB.class)
                .readValue(aposToQuotes("{'a':[ 1, 23, { } ],'b':2}"));
        assertEquals(0, result.a);
        assertEquals(2, result.b);
    }

// com.fasterxml.jackson.databind.views.TestViewSerialization::testSimple
    public void testSimple() throws IOException
    {
        StringWriter sw = new StringWriter();
        
        Bean bean = new Bean();
        Map<String,Object> map = writeAndMap(MAPPER, bean);
        assertEquals(3, map.size());

        
        sw = new StringWriter();
        MAPPER.writerWithView(ViewA.class).writeValue(sw, bean);
        map = MAPPER.readValue(sw.toString(), Map.class);
        assertEquals(1, map.size());
        assertEquals("1", map.get("a"));

        
        sw = new StringWriter();
        MAPPER.writerWithView(ViewAA.class).writeValue(sw, bean);
        map = MAPPER.readValue(sw.toString(), Map.class);
        assertEquals(2, map.size());
        assertEquals("1", map.get("a"));
        assertEquals("2", map.get("aa"));

        
        String json = MAPPER.writerWithView(ViewB.class).writeValueAsString(bean);
        map = MAPPER.readValue(json, Map.class);
        assertEquals(2, map.size());
        assertEquals("2", map.get("aa"));
        assertEquals("3", map.get("b"));

        
        json = MAPPER.writerWithView(ViewBB.class).writeValueAsString(bean);
        map = MAPPER.readValue(json, Map.class);
        assertEquals(2, map.size());
        assertEquals("2", map.get("aa"));
        assertEquals("3", map.get("b"));

        
        json = MAPPER.writerWithView(null).writeValueAsString(bean);
        map = MAPPER.readValue(json, Map.class);
        assertEquals(3, map.size());
    }

// com.fasterxml.jackson.databind.views.TestViewSerialization::testDefaultExclusion
    public void testDefaultExclusion() throws IOException
    {
        MixedBean bean = new MixedBean();

        
        String json = MAPPER.writerWithView(ViewA.class).writeValueAsString(bean);
        Map<String,Object> map = MAPPER.readValue(json, Map.class);
        assertEquals(2, map.size());
        assertEquals("1", map.get("a"));
        assertEquals("2", map.get("b"));

        
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(MapperFeature.DEFAULT_VIEW_INCLUSION, false);

        
        json = mapper.writerWithView(ViewA.class).writeValueAsString(bean);
        map = mapper.readValue(json, Map.class);
        assertEquals(1, map.size());
        assertEquals("1", map.get("a"));
        assertNull(map.get("b"));

        
        json = mapper.writer().withView(null).writeValueAsString(bean);
        map = mapper.readValue(json, Map.class);
        assertEquals(2, map.size());
        assertEquals("1", map.get("a"));
        assertEquals("2", map.get("b"));
    }

// com.fasterxml.jackson.databind.views.TestViewSerialization::testImplicitAutoDetection
    public void testImplicitAutoDetection() throws Exception
    {
        assertEquals("{\"a\":1}",
                MAPPER.writeValueAsString(new ImplicitBean()));
    }

// com.fasterxml.jackson.databind.views.TestViewSerialization::testVisibility
    public void testVisibility() throws Exception
    {
        VisibilityBean bean = new VisibilityBean();
        
        String json = MAPPER.writerWithView(Object.class).writeValueAsString(bean);
        
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

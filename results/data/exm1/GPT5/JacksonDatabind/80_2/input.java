// buggy code
    public Collection<NamedType> collectAndResolveSubtypesByClass(MapperConfig<?> config, 
            AnnotatedMember property, JavaType baseType)
    {
        final AnnotationIntrospector ai = config.getAnnotationIntrospector();
        // for backwards compatibility, must allow null here:
        Class<?> rawBase = (baseType == null) ? property.getRawType() : baseType.getRawClass();
        
        HashMap<NamedType, NamedType> collected = new HashMap<NamedType, NamedType>();
        // start with registered subtypes (which have precedence)
        if (_registeredSubtypes != null) {
            for (NamedType subtype : _registeredSubtypes) {
                // is it a subtype of root type?
                if (rawBase.isAssignableFrom(subtype.getType())) { // yes
                    AnnotatedClass curr = AnnotatedClassResolver.resolveWithoutSuperTypes(config,
                            subtype.getType());
                    _collectAndResolve(curr, subtype, config, ai, collected);
                }
            }
        }
        
        // then annotated types for property itself
            Collection<NamedType> st = ai.findSubtypes(property);
            if (st != null) {
                for (NamedType nt : st) {
                    AnnotatedClass ac = AnnotatedClassResolver.resolveWithoutSuperTypes(config,
                            nt.getType());
                    _collectAndResolve(ac, nt, config, ai, collected);
                }            
        }

        NamedType rootType = new NamedType(rawBase, null);
        AnnotatedClass ac = AnnotatedClassResolver.resolveWithoutSuperTypes(config, rawBase);
            
        // and finally subtypes via annotations from base type (recursively)
        _collectAndResolve(ac, rootType, config, ai, collected);

        return new ArrayList<NamedType>(collected.values());
    }

    public Collection<NamedType> collectAndResolveSubtypesByTypeId(MapperConfig<?> config, 
            AnnotatedMember property, JavaType baseType)
    {
        final AnnotationIntrospector ai = config.getAnnotationIntrospector();
        Class<?> rawBase = baseType.getRawClass();

        // Need to keep track of classes that have been handled already 
        Set<Class<?>> typesHandled = new HashSet<Class<?>>();
        Map<String,NamedType> byName = new LinkedHashMap<String,NamedType>();

        // start with lowest-precedence, which is from type hierarchy
        NamedType rootType = new NamedType(rawBase, null);
        AnnotatedClass ac = AnnotatedClassResolver.resolveWithoutSuperTypes(config,
                rawBase);
        _collectAndResolveByTypeId(ac, rootType, config, typesHandled, byName);
        
        // then with definitions from property
            Collection<NamedType> st = ai.findSubtypes(property);
            if (st != null) {
                for (NamedType nt : st) {
                    ac = AnnotatedClassResolver.resolveWithoutSuperTypes(config, nt.getType());
                    _collectAndResolveByTypeId(ac, nt, config, typesHandled, byName);
                }            
        }
        // and finally explicit type registrations (highest precedence)
        if (_registeredSubtypes != null) {
            for (NamedType subtype : _registeredSubtypes) {
                // is it a subtype of root type?
                if (rawBase.isAssignableFrom(subtype.getType())) { // yes
                    AnnotatedClass curr = AnnotatedClassResolver.resolveWithoutSuperTypes(config,
                            subtype.getType());
                    _collectAndResolveByTypeId(curr, subtype, config, typesHandled, byName);
                }
            }
        }
        return _combineNamedAndUnnamed(rawBase, typesHandled, byName);
    }

// relevant test
// com.fasterxml.jackson.databind.deser.jdk.EnumDeserializationTest::testEnumWithJsonPropertyRename
    public void testEnumWithJsonPropertyRename() throws Exception
    {
        String json = MAPPER.writeValueAsString(new EnumWithPropertyAnno[] {
                EnumWithPropertyAnno.B, EnumWithPropertyAnno.A
        });
        assertEquals("[\"b\",\"a\"]", json);

        
        EnumWithPropertyAnno[] result = MAPPER.readValue(json, EnumWithPropertyAnno[].class);
        assertNotNull(result);
        assertEquals(2, result.length);
        assertSame(EnumWithPropertyAnno.B, result[0]);
        assertSame(EnumWithPropertyAnno.A, result[1]);
    }

// com.fasterxml.jackson.databind.deser.jdk.EnumDeserializationTest::testDeserWithToString1161
    public void testDeserWithToString1161() throws Exception
    {
        Enum1161 result = MAPPER.readerFor(Enum1161.class)
                .readValue(quote("A"));
        assertSame(Enum1161.A, result);

        result = MAPPER.readerFor(Enum1161.class)
                .with(DeserializationFeature.READ_ENUMS_USING_TO_STRING)
                .readValue(quote("a"));
        assertSame(Enum1161.A, result);

        
        result = MAPPER.readerFor(Enum1161.class)
                .without(DeserializationFeature.READ_ENUMS_USING_TO_STRING)
                .readValue(quote("A"));
        assertSame(Enum1161.A, result);
    }

// com.fasterxml.jackson.databind.deser.jdk.EnumDeserializationTest::testEnumWithDefaultAnnotation
    public void testEnumWithDefaultAnnotation() throws Exception {
        final ObjectMapper mapper = new ObjectMapper();
        mapper.enable(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_USING_DEFAULT_VALUE);

        EnumWithDefaultAnno myEnum = mapper.readValue("\"foo\"", EnumWithDefaultAnno.class);
        assertSame(EnumWithDefaultAnno.OTHER, myEnum);
    }

// com.fasterxml.jackson.databind.deser.jdk.EnumDeserializationTest::testEnumWithDefaultAnnotationUsingIndexInBound1
    public void testEnumWithDefaultAnnotationUsingIndexInBound1() throws Exception {
        final ObjectMapper mapper = new ObjectMapper();
        mapper.enable(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_USING_DEFAULT_VALUE);

        EnumWithDefaultAnno myEnum = mapper.readValue("1", EnumWithDefaultAnno.class);
        assertSame(EnumWithDefaultAnno.B, myEnum);
    }

// com.fasterxml.jackson.databind.deser.jdk.EnumDeserializationTest::testEnumWithDefaultAnnotationUsingIndexInBound2
    public void testEnumWithDefaultAnnotationUsingIndexInBound2() throws Exception {
        final ObjectMapper mapper = new ObjectMapper();
        mapper.enable(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_USING_DEFAULT_VALUE);

        EnumWithDefaultAnno myEnum = mapper.readValue("2", EnumWithDefaultAnno.class);
        assertSame(EnumWithDefaultAnno.OTHER, myEnum);
    }

// com.fasterxml.jackson.databind.deser.jdk.EnumDeserializationTest::testEnumWithDefaultAnnotationUsingIndexSameAsLength
    public void testEnumWithDefaultAnnotationUsingIndexSameAsLength() throws Exception {
        final ObjectMapper mapper = new ObjectMapper();
        mapper.enable(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_USING_DEFAULT_VALUE);

        EnumWithDefaultAnno myEnum = mapper.readValue("3", EnumWithDefaultAnno.class);
        assertSame(EnumWithDefaultAnno.OTHER, myEnum);
    }

// com.fasterxml.jackson.databind.deser.jdk.EnumDeserializationTest::testEnumWithDefaultAnnotationUsingIndexOutOfBound
    public void testEnumWithDefaultAnnotationUsingIndexOutOfBound() throws Exception {
        final ObjectMapper mapper = new ObjectMapper();
        mapper.enable(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_USING_DEFAULT_VALUE);

        EnumWithDefaultAnno myEnum = mapper.readValue("4", EnumWithDefaultAnno.class);
        assertSame(EnumWithDefaultAnno.OTHER, myEnum);
    }

// com.fasterxml.jackson.databind.deser.jdk.EnumDeserializationTest::testEnumWithDefaultAnnotationWithConstructor
    public void testEnumWithDefaultAnnotationWithConstructor() throws Exception {
        final ObjectMapper mapper = new ObjectMapper();
        mapper.enable(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_USING_DEFAULT_VALUE);

        EnumWithDefaultAnnoAndConstructor myEnum = mapper.readValue("\"foo\"", EnumWithDefaultAnnoAndConstructor.class);
        assertNull("When using a constructor, the default value annotation shouldn't be used.", myEnum);
    }

// com.fasterxml.jackson.databind.deser.jdk.EnumDeserializationTest::testExceptionFromCustomEnumKeyDeserializer
    public void testExceptionFromCustomEnumKeyDeserializer() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new EnumModule());
        try {
            objectMapper.readValue("{\"TWO\": \"dumpling\"}",
                    new TypeReference<Map<AnEnum, String>>() {});
            fail("No exception");
        } catch (IOException e) {
            assertTrue(e.getMessage().contains("Undefined AnEnum"));
        }
    }

// com.fasterxml.jackson.databind.deser.jdk.EnumMapDeserializationTest::testEnumMaps
    public void testEnumMaps() throws Exception
    {
        EnumMap<TestEnum,String> value = MAPPER.readValue("{\"OK\":\"value\"}",
                new TypeReference<EnumMap<TestEnum,String>>() { });
        assertEquals("value", value.get(TestEnum.OK));
    }

// com.fasterxml.jackson.databind.deser.jdk.EnumMapDeserializationTest::testToStringEnumMaps
    public void testToStringEnumMaps() throws Exception
    {
        
        ObjectReader r = MAPPER.reader()
                .with(DeserializationFeature.READ_ENUMS_USING_TO_STRING);
        EnumMap<LowerCaseEnum,String> value = r.forType(
            new TypeReference<EnumMap<LowerCaseEnum,String>>() { })
                .readValue("{\"a\":\"value\"}");
        assertEquals("value", value.get(LowerCaseEnum.A));
    }

// com.fasterxml.jackson.databind.deser.jdk.EnumMapDeserializationTest::testCustomEnumMapWithDefaultCtor
    public void testCustomEnumMapWithDefaultCtor() throws Exception
    {
        MySimpleEnumMap map = MAPPER.readValue(aposToQuotes("{'RULES':'waves'}"),
                MySimpleEnumMap.class);   
        assertEquals(1, map.size());
        assertEquals("waves", map.get(TestEnum.RULES));
    }

// com.fasterxml.jackson.databind.deser.jdk.EnumMapDeserializationTest::testCustomEnumMapFromString
    public void testCustomEnumMapFromString() throws Exception
    {
        FromStringEnumMap map = MAPPER.readValue(quote("kewl"), FromStringEnumMap.class);   
        assertEquals(1, map.size());
        assertEquals("kewl", map.get(TestEnum.JACKSON));
    }

// com.fasterxml.jackson.databind.deser.jdk.EnumMapDeserializationTest::testCustomEnumMapWithDelegate
    public void testCustomEnumMapWithDelegate() throws Exception
    {
        FromDelegateEnumMap map = MAPPER.readValue(aposToQuotes("{'foo':'bar'}"), FromDelegateEnumMap.class);   
        assertEquals(1, map.size());
        assertEquals("{foo=bar}", map.get(TestEnum.OK));
    }

// com.fasterxml.jackson.databind.deser.jdk.EnumMapDeserializationTest::testCustomEnumMapFromProps
    public void testCustomEnumMapFromProps() throws Exception
    {
        FromPropertiesEnumMap map = MAPPER.readValue(aposToQuotes(
                "{'a':13,'RULES':'jackson','b':-731,'OK':'yes'}"),
                FromPropertiesEnumMap.class);

        assertEquals(13, map.a0);
        assertEquals(-731, map.b0);

        assertEquals("jackson", map.get(TestEnum.RULES));
        assertEquals("yes", map.get(TestEnum.OK));
        assertEquals(2, map.size());
    }

// com.fasterxml.jackson.databind.deser.jdk.JDKAtomicTypesTest::testAtomicBoolean
    public void testAtomicBoolean() throws Exception
    {
        AtomicBoolean b = MAPPER.readValue("true", AtomicBoolean.class);
        assertTrue(b.get());
    }

// com.fasterxml.jackson.databind.deser.jdk.JDKAtomicTypesTest::testAtomicInt
    public void testAtomicInt() throws Exception
    {
        AtomicInteger value = MAPPER.readValue("13", AtomicInteger.class);
        assertEquals(13, value.get());
    }

// com.fasterxml.jackson.databind.deser.jdk.JDKAtomicTypesTest::testAtomicLong
    public void testAtomicLong() throws Exception
    {
        AtomicLong value = MAPPER.readValue("12345678901", AtomicLong.class);
        assertEquals(12345678901L, value.get());
    }

// com.fasterxml.jackson.databind.deser.jdk.JDKAtomicTypesTest::testAtomicReference
    public void testAtomicReference() throws Exception
    {
        AtomicReference<long[]> value = MAPPER.readValue("[1,2]",
                new com.fasterxml.jackson.core.type.TypeReference<AtomicReference<long[]>>() { });
        Object ob = value.get();
        assertNotNull(ob);
        assertEquals(long[].class, ob.getClass());
        long[] longs = (long[]) ob;
        assertNotNull(longs);
        assertEquals(2, longs.length);
        assertEquals(1, longs[0]);
        assertEquals(2, longs[1]);
    }

// com.fasterxml.jackson.databind.deser.jdk.JDKAtomicTypesTest::testAbsentExclusion
    public void testAbsentExclusion() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setSerializationInclusion(JsonInclude.Include.NON_ABSENT);
        assertEquals(aposToQuotes("{'value':true}"),
                mapper.writeValueAsString(new SimpleWrapper(Boolean.TRUE)));
        assertEquals(aposToQuotes("{}"),
                mapper.writeValueAsString(new SimpleWrapper(null)));
    }

// com.fasterxml.jackson.databind.deser.jdk.JDKAtomicTypesTest::testSerPropInclusionAlways
    public void testSerPropInclusionAlways() throws Exception
    {
        JsonInclude.Value incl =
                JsonInclude.Value.construct(JsonInclude.Include.NON_ABSENT, JsonInclude.Include.ALWAYS);
        ObjectMapper mapper = new ObjectMapper();
        mapper.setDefaultPropertyInclusion(incl);
        assertEquals(aposToQuotes("{'value':true}"),
                mapper.writeValueAsString(new SimpleWrapper(Boolean.TRUE)));
    }

// com.fasterxml.jackson.databind.deser.jdk.JDKAtomicTypesTest::testSerPropInclusionNonNull
    public void testSerPropInclusionNonNull() throws Exception
    {
        JsonInclude.Value incl =
                JsonInclude.Value.construct(JsonInclude.Include.NON_ABSENT, JsonInclude.Include.NON_NULL);
        ObjectMapper mapper = new ObjectMapper();
        mapper.setDefaultPropertyInclusion(incl);
        assertEquals(aposToQuotes("{'value':true}"),
                mapper.writeValueAsString(new SimpleWrapper(Boolean.TRUE)));
    }

// com.fasterxml.jackson.databind.deser.jdk.JDKAtomicTypesTest::testSerPropInclusionNonAbsent
    public void testSerPropInclusionNonAbsent() throws Exception
    {
        JsonInclude.Value incl =
                JsonInclude.Value.construct(JsonInclude.Include.NON_ABSENT, JsonInclude.Include.NON_ABSENT);
        ObjectMapper mapper = new ObjectMapper();
        mapper.setDefaultPropertyInclusion(incl);
        assertEquals(aposToQuotes("{'value':true}"),
                mapper.writeValueAsString(new SimpleWrapper(Boolean.TRUE)));
    }

// com.fasterxml.jackson.databind.deser.jdk.JDKAtomicTypesTest::testSerPropInclusionNonEmpty
    public void testSerPropInclusionNonEmpty() throws Exception
    {
        JsonInclude.Value incl =
                JsonInclude.Value.construct(JsonInclude.Include.NON_ABSENT, JsonInclude.Include.NON_EMPTY);
        ObjectMapper mapper = new ObjectMapper();
        mapper.setDefaultPropertyInclusion(incl);
        assertEquals(aposToQuotes("{'value':true}"),
                mapper.writeValueAsString(new SimpleWrapper(Boolean.TRUE)));
    }

// com.fasterxml.jackson.databind.deser.jdk.JDKAtomicTypesTest::testPolymorphicAtomicReference
    public void testPolymorphicAtomicReference() throws Exception
    {
        RefWrapper input = new RefWrapper(13);
        String json = MAPPER.writeValueAsString(input);
        
        RefWrapper result = MAPPER.readValue(json, RefWrapper.class);
        assertNotNull(result.w);
        Object ob = result.w.get();
        assertEquals(Impl.class, ob.getClass());
        assertEquals(13, ((Impl) ob).value);
    }

// com.fasterxml.jackson.databind.deser.jdk.JDKAtomicTypesTest::testFilteringOfAtomicReference
    public void testFilteringOfAtomicReference() throws Exception
    {
        SimpleWrapper input = new SimpleWrapper(null);
        ObjectMapper mapper = MAPPER;

        
        assertEquals("{\"value\":null}", mapper.writeValueAsString(input));

        
        mapper = new ObjectMapper().setSerializationInclusion(JsonInclude
                .Include.NON_NULL);
        assertEquals("{\"value\":null}", mapper.writeValueAsString(input));

        
        mapper = new ObjectMapper().setSerializationInclusion(JsonInclude
                .Include.NON_EMPTY);
        assertEquals("{}", mapper.writeValueAsString(input));
    }

// com.fasterxml.jackson.databind.deser.jdk.JDKAtomicTypesTest::testTypeRefinement
    public void testTypeRefinement() throws Exception
    {
        RefiningWrapper input = new RefiningWrapper();
        BigDecimal bd = new BigDecimal("0.25");
        input.value = new AtomicReference<Serializable>(bd);
        String json = MAPPER.writeValueAsString(input);

        
        RefiningWrapper result = MAPPER.readValue(json, RefiningWrapper.class);
        assertNotNull(result.value);
        Object ob = result.value.get();
        assertEquals(BigDecimal.class, ob.getClass());
        assertEquals(bd, ob);
    }

// com.fasterxml.jackson.databind.deser.jdk.JDKAtomicTypesTest::testDeserializeWithContentAs
    public void testDeserializeWithContentAs() throws Exception
    {
        AtomicRefReadWrapper result = MAPPER.readValue(aposToQuotes("{'value':'abc'}"),
                AtomicRefReadWrapper.class);
         Object v = result.value.get();
         assertNotNull(v);
         assertEquals(WrappedString.class, v.getClass());
         assertEquals("abc", ((WrappedString)v).value);
    }

// com.fasterxml.jackson.databind.deser.jdk.JDKAtomicTypesTest::testWithUnwrapping
    public void testWithUnwrapping() throws Exception
    {
         String jsonExp = aposToQuotes("{'XX.name':'Bob'}");
         String jsonAct = MAPPER.writeValueAsString(new UnwrappingRefParent());
         assertEquals(jsonExp, jsonAct);
    }

// com.fasterxml.jackson.databind.deser.jdk.JDKAtomicTypesTest::testWithCustomDeserializer
    public void testWithCustomDeserializer() throws Exception
    {
        LCStringWrapper w = MAPPER.readValue(aposToQuotes("{'value':'FoobaR'}"),
                LCStringWrapper.class);
        assertEquals("foobar", w.value.get());
    }

// com.fasterxml.jackson.databind.deser.jdk.JDKAtomicTypesTest::testEmpty1256
    public void testEmpty1256() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setSerializationInclusion(JsonInclude.Include.NON_ABSENT);

        String json = mapper.writeValueAsString(new Issue1256Bean());
        assertEquals("{}", json);
    }

// com.fasterxml.jackson.databind.deser.jdk.JDKAtomicTypesTest::testNullValueHandling
    public void testNullValueHandling() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        AtomicReference<Double> inputData = new AtomicReference<Double>();
        String json = mapper.writeValueAsString(inputData);
        AtomicReference<Double> readData = (AtomicReference<Double>) mapper.readValue(json, AtomicReference.class);
        assertNotNull(readData);
        assertNull(readData.get());
    }

// com.fasterxml.jackson.databind.deser.jdk.JDKNumberDeserTest::testNaN
    public void testNaN() throws Exception
    {
        Float result = MAPPER.readValue(" \"NaN\"", Float.class);
        assertEquals(Float.valueOf(Float.NaN), result);

        Double d = MAPPER.readValue(" \"NaN\"", Double.class);
        assertEquals(Double.valueOf(Double.NaN), d);

        Number num = MAPPER.readValue(" \"NaN\"", Number.class);
        assertEquals(Double.valueOf(Double.NaN), num);
    }

// com.fasterxml.jackson.databind.deser.jdk.JDKNumberDeserTest::testDoubleInf
    public void testDoubleInf() throws Exception
    {
        Double result = MAPPER.readValue(" \""+Double.POSITIVE_INFINITY+"\"", Double.class);
        assertEquals(Double.valueOf(Double.POSITIVE_INFINITY), result);

        result = MAPPER.readValue(" \""+Double.NEGATIVE_INFINITY+"\"", Double.class);
        assertEquals(Double.valueOf(Double.NEGATIVE_INFINITY), result);
    }

// com.fasterxml.jackson.databind.deser.jdk.JDKNumberDeserTest::testEmptyAsNumber
    public void testEmptyAsNumber() throws Exception
    {
        assertNull(MAPPER.readValue(quote(""), Byte.class));
        assertNull(MAPPER.readValue(quote(""), Short.class));
        assertNull(MAPPER.readValue(quote(""), Integer.class));
        assertNull(MAPPER.readValue(quote(""), Integer.class));
        assertNull(MAPPER.readValue(quote(""), Long.class));
        assertNull(MAPPER.readValue(quote(""), Float.class));
        assertNull(MAPPER.readValue(quote(""), Double.class));

        assertNull(MAPPER.readValue(quote(""), BigInteger.class));
        assertNull(MAPPER.readValue(quote(""), BigDecimal.class));
    }

// com.fasterxml.jackson.databind.deser.jdk.JDKNumberDeserTest::testDeserializeDecimalHappyPath
    public void testDeserializeDecimalHappyPath() throws Exception {
        String json = "{\"defaultValue\": { \"value\": 123 } }";
        MyBeanHolder result = MAPPER.readValue(json, MyBeanHolder.class);
        assertEquals(BigDecimal.valueOf(123), result.defaultValue.value.decimal);
    }

// com.fasterxml.jackson.databind.deser.jdk.JDKNumberDeserTest::testDeserializeDecimalProperException
    public void testDeserializeDecimalProperException() throws Exception {
        String json = "{\"defaultValue\": { \"value\": \"123\" } }";
        try {
            MAPPER.readValue(json, MyBeanHolder.class);
            fail("should have raised exception");
        } catch (JsonProcessingException e) {
            verifyException(e, "not numeric");
        }
    }

// com.fasterxml.jackson.databind.deser.jdk.JDKNumberDeserTest::testDeserializeDecimalProperExceptionWhenIdSet
    public void testDeserializeDecimalProperExceptionWhenIdSet() throws Exception {
        String json = "{\"id\": 5, \"defaultValue\": { \"value\": \"123\" } }";
        try {
            MyBeanHolder result = MAPPER.readValue(json, MyBeanHolder.class);
            fail("should have raised exception instead value was set to " + result.defaultValue.value.decimal.toString());
        } catch (JsonProcessingException e) {
            verifyException(e, "not numeric");
        }
    }

// com.fasterxml.jackson.databind.deser.jdk.JDKNumberDeserTest::testScientificNotationAsStringForNumber
    public void testScientificNotationAsStringForNumber() throws Exception
    {
        Object ob = MAPPER.readValue("\"3E-8\"", Number.class);
        assertEquals(Double.class, ob.getClass());
        ob = MAPPER.readValue("\"3e-8\"", Number.class);
        assertEquals(Double.class, ob.getClass());
        ob = MAPPER.readValue("\"300000000\"", Number.class);
        assertEquals(Integer.class, ob.getClass());
        ob = MAPPER.readValue("\"123456789012\"", Number.class);
        assertEquals(Long.class, ob.getClass());
    }

// com.fasterxml.jackson.databind.deser.jdk.JDKNumberDeserTest::testIntAsNumber
    public void testIntAsNumber() throws Exception
    {
        
        Number result = MAPPER.readValue(" 123 ", Number.class);
        assertEquals(Integer.valueOf(123), result);
    }

// com.fasterxml.jackson.databind.deser.jdk.JDKNumberDeserTest::testLongAsNumber
    public void testLongAsNumber() throws Exception
    {
        
        long exp = 1234567890123L;
        Number result = MAPPER.readValue(String.valueOf(exp), Number.class);
        assertEquals(Long.valueOf(exp), result);
    }

// com.fasterxml.jackson.databind.deser.jdk.JDKNumberDeserTest::testBigIntAsNumber
    public void testBigIntAsNumber() throws Exception
    {
        
        BigInteger biggie = new BigInteger("1234567890123456789012345678901234567890");
        Number result = MAPPER.readValue(biggie.toString(), Number.class);
        assertEquals(BigInteger.class, biggie.getClass());
        assertEquals(biggie, result);
    }

// com.fasterxml.jackson.databind.deser.jdk.JDKNumberDeserTest::testIntTypeOverride
    public void testIntTypeOverride() throws Exception
    {
        
        ObjectReader r = MAPPER.reader(DeserializationFeature.USE_BIG_INTEGER_FOR_INTS);

        BigInteger exp = BigInteger.valueOf(123L);

        
        Number result = r.forType(Number.class).readValue(" 123 ");
        assertEquals(BigInteger.class, result.getClass());
        assertEquals(exp, result);

        
         r.forType(Object.class).readValue("123");
        assertEquals(BigInteger.class, result.getClass());
        assertEquals(exp, result);

        
        JsonNode node = r.readTree("  123");
        assertTrue(node.isBigInteger());
        assertEquals(123, node.asInt());
    }

// com.fasterxml.jackson.databind.deser.jdk.JDKNumberDeserTest::testDoubleAsNumber
    public void testDoubleAsNumber() throws Exception
    {
        Number result = MAPPER.readValue(new StringReader(" 1.0 "), Number.class);
        assertEquals(Double.valueOf(1.0), result);
    }

// com.fasterxml.jackson.databind.deser.jdk.JDKNumberDeserTest::testFpTypeOverrideSimple
    public void testFpTypeOverrideSimple() throws Exception
    {
        ObjectReader r = MAPPER.reader(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS);
        BigDecimal dec = new BigDecimal("0.1");

        
        Number result = r.forType(Number.class).readValue(dec.toString());
        assertEquals(BigDecimal.class, result.getClass());
        assertEquals(dec, result);

        
        Object value = r.forType(Object.class).readValue(dec.toString());
        assertEquals(BigDecimal.class, result.getClass());
        assertEquals(dec, value);

        JsonNode node = r.readTree(dec.toString());
        assertTrue(node.isBigDecimal());
        assertEquals(dec.doubleValue(), node.asDouble());
    }

// com.fasterxml.jackson.databind.deser.jdk.JDKNumberDeserTest::testFpTypeOverrideStructured
    public void testFpTypeOverrideStructured() throws Exception
    {
        ObjectReader r = MAPPER.reader(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS);

        BigDecimal dec = new BigDecimal("-19.37");
        
        @SuppressWarnings("unchecked")
        List<Object> list = (List<Object>) r.forType(List.class).readValue("[ "+dec.toString()+" ]");
        assertEquals(1, list.size());
        Object val = list.get(0);
        assertEquals(BigDecimal.class, val.getClass());
        assertEquals(dec, val);

        
        Map<?,?> map = r.forType(Map.class).readValue("{ \"a\" : "+dec.toString()+" }");
        assertEquals(1, map.size());
        val = map.get("a");
        assertEquals(BigDecimal.class, val.getClass());
        assertEquals(dec, val);
    }

// com.fasterxml.jackson.databind.deser.jdk.JDKNumberDeserTest::testForceIntsToLongs
    public void testForceIntsToLongs() throws Exception
    {
        ObjectReader r = MAPPER.reader(DeserializationFeature.USE_LONG_FOR_INTS);

        Object ob = r.forType(Object.class).readValue("42");
        assertEquals(Long.class, ob.getClass());
        assertEquals(Long.valueOf(42L), ob);

        Number n = r.forType(Number.class).readValue("42");
        assertEquals(Long.class, n.getClass());
        assertEquals(Long.valueOf(42L), n);

        
        JsonNode node = r.readTree("42");
        if (!node.isLong()) {
            fail("Expected LongNode, got: "+node.getClass().getName());
        }
        assertEquals(42, node.asInt());
    }

// com.fasterxml.jackson.databind.deser.jdk.JDKScalarsTest::testBooleanPrimitive
    public void testBooleanPrimitive() throws Exception
    {
        
        BooleanBean result = MAPPER.readValue("{\"v\":true}", BooleanBean.class);
        assertTrue(result._v);
        result = MAPPER.readValue("{\"v\":null}", BooleanBean.class);
        assertNotNull(result);
        assertFalse(result._v);
        result = MAPPER.readValue("{\"v\":1}", BooleanBean.class);
        assertNotNull(result);
        assertTrue(result._v);

        
        boolean[] array = MAPPER.readValue("[ null, false ]", boolean[].class);
        assertNotNull(array);
        assertEquals(2, array.length);
        assertFalse(array[0]);
        assertFalse(array[1]);
    }

// com.fasterxml.jackson.databind.deser.jdk.JDKScalarsTest::testBooleanWrapper
    public void testBooleanWrapper() throws Exception
    {
        Boolean result = MAPPER.readValue("true", Boolean.class);
        assertEquals(Boolean.TRUE, result);
        result = MAPPER.readValue("false", Boolean.class);
        assertEquals(Boolean.FALSE, result);

        
        result = MAPPER.readValue("0", Boolean.class);
        assertEquals(Boolean.FALSE, result);
        result = MAPPER.readValue("1", Boolean.class);
        assertEquals(Boolean.TRUE, result);
    }

// com.fasterxml.jackson.databind.deser.jdk.JDKScalarsTest::testLongToBoolean
    public void testLongToBoolean() throws Exception
    {
        long value = 1L + Integer.MAX_VALUE;
        BooleanWrapper b = MAPPER.readValue("{\"primitive\" : "+value+", \"wrapper\":"+value+", \"ctor\":"+value+"}",
                BooleanWrapper.class);
        assertEquals(Boolean.TRUE, b.wrapper);
        assertTrue(b.primitive);
        assertEquals(Boolean.TRUE, b.ctor);

        
        b = MAPPER.readValue("{\"primitive\" : 0 , \"wrapper\":0, \"ctor\":0}",
                BooleanWrapper.class);
        assertEquals(Boolean.FALSE, b.wrapper);
        assertFalse(b.primitive);
        assertEquals(Boolean.FALSE, b.ctor);

        boolean[] boo = MAPPER.readValue("[ 0, 15, \"\", \"false\", \"True\" ]",
                boolean[].class);
        assertEquals(5, boo.length);
        assertFalse(boo[0]);
        assertTrue(boo[1]);
        assertFalse(boo[2]);
        assertFalse(boo[3]);
        assertTrue(boo[4]);
    }

// com.fasterxml.jackson.databind.deser.jdk.JDKScalarsTest::testByteWrapper
    public void testByteWrapper() throws Exception
    {
        Byte result = MAPPER.readValue("   -42\t", Byte.class);
        assertEquals(Byte.valueOf((byte)-42), result);

        
        result = MAPPER.readValue(" \"-12\"", Byte.class);
        assertEquals(Byte.valueOf((byte)-12), result);

        result = MAPPER.readValue(" 39.07", Byte.class);
        assertEquals(Byte.valueOf((byte)39), result);
    }

// com.fasterxml.jackson.databind.deser.jdk.JDKScalarsTest::testShortWrapper
    public void testShortWrapper() throws Exception
    {
        Short result = MAPPER.readValue("37", Short.class);
        assertEquals(Short.valueOf((short)37), result);

        
        result = MAPPER.readValue(" \"-1009\"", Short.class);
        assertEquals(Short.valueOf((short)-1009), result);

        result = MAPPER.readValue("-12.9", Short.class);
        assertEquals(Short.valueOf((short)-12), result);
    }

// com.fasterxml.jackson.databind.deser.jdk.JDKScalarsTest::testCharacterWrapper
    public void testCharacterWrapper() throws Exception
    {
        
        Character result = MAPPER.readValue("\"a\"", Character.class);
        assertEquals(Character.valueOf('a'), result);

        
        result = MAPPER.readValue(" "+((int) 'X'), Character.class);
        assertEquals(Character.valueOf('X'), result);
        
        final CharacterWrapperBean wrapper = MAPPER.readValue("{\"v\":null}", CharacterWrapperBean.class);
        assertNotNull(wrapper);
        assertNull(wrapper.getV());
        
        final ObjectMapper mapper = new ObjectMapper();
        mapper.enable(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES);
        try {
            mapper.readValue("{\"v\":null}", CharacterBean.class);
            fail("Attempting to deserialize a 'null' JSON reference into a 'char' property did not throw an exception");
        } catch (JsonMappingException e) {
            verifyException(e, "can not map `null`");
            
        }

        mapper.disable(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES);  
        final CharacterBean charBean = MAPPER.readValue("{\"v\":null}", CharacterBean.class);
        assertNotNull(wrapper);
        assertEquals('\u0000', charBean.getV());
    }

// com.fasterxml.jackson.databind.deser.jdk.JDKScalarsTest::testIntWrapper
    public void testIntWrapper() throws Exception
    {
        Integer result = MAPPER.readValue("   -42\t", Integer.class);
        assertEquals(Integer.valueOf(-42), result);

        
        result = MAPPER.readValue(" \"-1200\"", Integer.class);
        assertEquals(Integer.valueOf(-1200), result);

        result = MAPPER.readValue(" 39.07", Integer.class);
        assertEquals(Integer.valueOf(39), result);
    }

// com.fasterxml.jackson.databind.deser.jdk.JDKScalarsTest::testIntPrimitive
    public void testIntPrimitive() throws Exception
    {
        
        IntBean result = MAPPER.readValue("{\"v\":3}", IntBean.class);
        assertEquals(3, result._v);

        result = MAPPER.readValue("{\"v\":null}", IntBean.class);
        assertNotNull(result);
        assertEquals(0, result._v);

        
        int[] array = MAPPER.readValue("[ null ]", int[].class);
        assertNotNull(array);
        assertEquals(1, array.length);
        assertEquals(0, array[0]);
        
        
        final ObjectMapper mapper = new ObjectMapper();
        mapper.disable(DeserializationFeature.UNWRAP_SINGLE_VALUE_ARRAYS);
        try {
            mapper.readValue("{\"v\":[3]}", IntBean.class);
            fail("Did not throw exception when reading a value from a single value array with the UNWRAP_SINGLE_VALUE_ARRAYS feature disabled");
        } catch (JsonMappingException exp) {
            
        }
        
        mapper.enable(DeserializationFeature.UNWRAP_SINGLE_VALUE_ARRAYS);
        
        result = mapper.readValue("{\"v\":[3]}", IntBean.class);
        assertEquals(3, result._v);
        
        result = mapper.readValue("[{\"v\":[3]}]", IntBean.class);
        assertEquals(3, result._v);
        
        try {
            mapper.readValue("[{\"v\":[3,3]}]", IntBean.class);
            fail("Did not throw exception while reading a value from a multi value array with UNWRAP_SINGLE_VALUE_ARRAY feature enabled");
        } catch (JsonMappingException exp) {
            
        }
        
        result = mapper.readValue("{\"v\":[null]}", IntBean.class);
        assertNotNull(result);
        assertEquals(0, result._v);

        array = mapper.readValue("[ [ null ] ]", int[].class);
        assertNotNull(array);
        assertEquals(1, array.length);
        assertEquals(0, array[0]);
    }

// com.fasterxml.jackson.databind.deser.jdk.JDKScalarsTest::testLongWrapper
    public void testLongWrapper() throws Exception
    {
        Long result = MAPPER.readValue("12345678901", Long.class);
        assertEquals(Long.valueOf(12345678901L), result);

        
        result = MAPPER.readValue(" \"-9876\"", Long.class);
        assertEquals(Long.valueOf(-9876), result);

        result = MAPPER.readValue("1918.3", Long.class);
        assertEquals(Long.valueOf(1918), result);
    }

// com.fasterxml.jackson.databind.deser.jdk.JDKScalarsTest::testLongPrimitive
    public void testLongPrimitive() throws Exception
    {
        
        LongBean result = MAPPER.readValue("{\"v\":3}", LongBean.class);
        assertEquals(3, result._v);
        result = MAPPER.readValue("{\"v\":null}", LongBean.class);
        assertNotNull(result);
        assertEquals(0, result._v);

        
        long[] array = MAPPER.readValue("[ null ]", long[].class);
        assertNotNull(array);
        assertEquals(1, array.length);
        assertEquals(0, array[0]);
        
        
        final ObjectMapper mapper = new ObjectMapper();
        mapper.disable(DeserializationFeature.UNWRAP_SINGLE_VALUE_ARRAYS);
        try {
            mapper.readValue("{\"v\":[3]}", LongBean.class);
            fail("Did not throw exception when reading a value from a single value array with the UNWRAP_SINGLE_VALUE_ARRAYS feature disabled");
        } catch (JsonMappingException exp) {
            
        }
        
        mapper.enable(DeserializationFeature.UNWRAP_SINGLE_VALUE_ARRAYS);
        
        result = mapper.readValue("{\"v\":[3]}", LongBean.class);
        assertEquals(3, result._v);
        
        result = mapper.readValue("[{\"v\":[3]}]", LongBean.class);
        assertEquals(3, result._v);
        
        try {
            mapper.readValue("[{\"v\":[3,3]}]", LongBean.class);
            fail("Did not throw exception while reading a value from a multi value array with UNWRAP_SINGLE_VALUE_ARRAY feature enabled");
        } catch (JsonMappingException exp) {
            
        }
        
        result = mapper.readValue("{\"v\":[null]}", LongBean.class);
        assertNotNull(result);
        assertEquals(0, result._v);

        array = mapper.readValue("[ [ null ] ]", long[].class);
        assertNotNull(array);
        assertEquals(1, array.length);
        assertEquals(0, array[0]);
    }

// com.fasterxml.jackson.databind.deser.jdk.JDKScalarsTest::testIntWithOverride
    public void testIntWithOverride() throws Exception
    {
        IntBean2 result = MAPPER.readValue("{\"v\":8}", IntBean2.class);
        assertEquals(9, result._v);
    }

// com.fasterxml.jackson.databind.deser.jdk.JDKScalarsTest::testDoublePrimitive
    public void testDoublePrimitive() throws Exception
    {
        
        
        final double value = 0.016;
        DoubleBean result = MAPPER.readValue("{\"v\":"+value+"}", DoubleBean.class);
        assertEquals(value, result._v);
        
        result = MAPPER.readValue("{\"v\":null}", DoubleBean.class);
        assertNotNull(result);
        assertEquals(0.0, result._v);

        
        double[] array = MAPPER.readValue("[ null ]", double[].class);
        assertNotNull(array);
        assertEquals(1, array.length);
        assertEquals(0.0, array[0]);
    }

// com.fasterxml.jackson.databind.deser.jdk.JDKScalarsTest::testFloatWrapper
    public void testFloatWrapper() throws Exception
    {
        
        String[] STRS = new String[] {
            "1.0", "0.0", "-0.3", "0.7", "42.012", "-999.0", NAN_STRING
        };

        for (String str : STRS) {
            Float exp = Float.valueOf(str);
            Float result;

            if (NAN_STRING != str) {
                
                result = MAPPER.readValue(str, Float.class);
                assertEquals(exp, result);
            }

            
            result = MAPPER.readValue(" \""+str+"\"", Float.class);
            assertEquals(exp, result);
        }
    }

// com.fasterxml.jackson.databind.deser.jdk.JDKScalarsTest::testDoubleWrapper
    public void testDoubleWrapper() throws Exception
    {
        
        String[] STRS = new String[] {
            "1.0", "0.0", "-0.3", "0.7", "42.012", "-999.0", NAN_STRING
        };

        for (String str : STRS) {
            Double exp = Double.valueOf(str);
            Double result;

            
            if (NAN_STRING != str) {
                result = MAPPER.readValue(str, Double.class);
               assertEquals(exp, result);
            }
            
            result = MAPPER.readValue(" \""+str+"\"", Double.class);
            assertEquals(exp, result);
        }
    }

// com.fasterxml.jackson.databind.deser.jdk.JDKScalarsTest::testDoubleAsArray
    public void testDoubleAsArray() throws Exception
    {
        final ObjectMapper mapper = new ObjectMapper();
        mapper.disable(DeserializationFeature.UNWRAP_SINGLE_VALUE_ARRAYS);
        final double value = 0.016;
        try {
            mapper.readValue("{\"v\":[" + value + "]}", DoubleBean.class);
            fail("Did not throw exception when reading a value from a single value array with the UNWRAP_SINGLE_VALUE_ARRAYS feature disabled");
        } catch (JsonMappingException exp) {
            
        }
        
        mapper.enable(DeserializationFeature.UNWRAP_SINGLE_VALUE_ARRAYS);
        
        DoubleBean result = mapper.readValue("{\"v\":[" + value + "]}",
                DoubleBean.class);
        assertEquals(value, result._v);
        
        result = mapper.readValue("[{\"v\":[" + value + "]}]", DoubleBean.class);
        assertEquals(value, result._v);
        
        try {
            mapper.readValue("[{\"v\":[" + value + "," + value + "]}]", DoubleBean.class);
            fail("Did not throw exception while reading a value from a multi value array with UNWRAP_SINGLE_VALUE_ARRAY feature enabled");
        } catch (JsonMappingException exp) {
            
        }
        
        result = mapper.readValue("{\"v\":[null]}", DoubleBean.class);
        assertNotNull(result);
        assertEquals(0d, result._v);

        double[] array = mapper.readValue("[ [ null ] ]", double[].class);
        assertNotNull(array);
        assertEquals(1, array.length);
        assertEquals(0d, array[0]);
    }

// com.fasterxml.jackson.databind.deser.jdk.JDKScalarsTest::testDoublePrimitiveNonNumeric
    public void testDoublePrimitiveNonNumeric() throws Exception
    {
        
        
        double value = Double.POSITIVE_INFINITY;
        DoubleBean result = MAPPER.readValue("{\"v\":\""+value+"\"}", DoubleBean.class);
        assertEquals(value, result._v);
        
        
        double[] array = MAPPER.readValue("[ \"Infinity\" ]", double[].class);
        assertNotNull(array);
        assertEquals(1, array.length);
        assertEquals(Double.POSITIVE_INFINITY, array[0]);
    }

// com.fasterxml.jackson.databind.deser.jdk.JDKScalarsTest::testFloatPrimitiveNonNumeric
    public void testFloatPrimitiveNonNumeric() throws Exception
    {
        
        float value = Float.POSITIVE_INFINITY;
        FloatBean result = MAPPER.readValue("{\"v\":\""+value+"\"}", FloatBean.class);
        assertEquals(value, result._v);
        
        
        float[] array = MAPPER.readValue("[ \"Infinity\" ]", float[].class);
        assertNotNull(array);
        assertEquals(1, array.length);
        assertEquals(Float.POSITIVE_INFINITY, array[0]);
    }

// com.fasterxml.jackson.databind.deser.jdk.JDKScalarsTest::testEmptyToNullCoercionForPrimitives
    public void testEmptyToNullCoercionForPrimitives() throws Exception {
        _testEmptyToNullCoercion(int.class, Integer.valueOf(0));
        _testEmptyToNullCoercion(long.class, Long.valueOf(0));
        _testEmptyToNullCoercion(double.class, Double.valueOf(0.0));
        _testEmptyToNullCoercion(float.class, Float.valueOf(0.0f));
    }

// com.fasterxml.jackson.databind.deser.jdk.JDKScalarsTest::testBase64Variants
    public void testBase64Variants() throws Exception
    {
        final byte[] INPUT = "abcdefghijklmnopqrstuvwxyz1234567890abcdefghijklmnopqrstuvwxyz1234567890X".getBytes("UTF-8");
        
        
        Assert.assertArrayEquals(INPUT, MAPPER.readValue(
                quote("YWJjZGVmZ2hpamtsbW5vcHFyc3R1dnd4eXoxMjM0NTY3ODkwYWJjZGVmZ2hpamtsbW5vcHFyc3R1dnd4eXoxMjM0NTY3ODkwWA=="),
                byte[].class));
        ObjectReader reader = MAPPER.readerFor(byte[].class);
        Assert.assertArrayEquals(INPUT, (byte[]) reader.with(Base64Variants.MIME_NO_LINEFEEDS).readValue(
                quote("YWJjZGVmZ2hpamtsbW5vcHFyc3R1dnd4eXoxMjM0NTY3ODkwYWJjZGVmZ2hpamtsbW5vcHFyc3R1dnd4eXoxMjM0NTY3ODkwWA=="
        )));

        
        Assert.assertArrayEquals(INPUT, (byte[]) reader.with(Base64Variants.MIME).readValue(
                quote("YWJjZGVmZ2hpamtsbW5vcHFyc3R1dnd4eXoxMjM0NTY3ODkwYWJjZGVmZ2hpamtsbW5vcHFyc3R1\\ndnd4eXoxMjM0NTY3ODkwWA=="
        )));
        Assert.assertArrayEquals(INPUT, (byte[]) reader.with(Base64Variants.MODIFIED_FOR_URL).readValue(
                quote("YWJjZGVmZ2hpamtsbW5vcHFyc3R1dnd4eXoxMjM0NTY3ODkwYWJjZGVmZ2hpamtsbW5vcHFyc3R1dnd4eXoxMjM0NTY3ODkwWA"
        )));
        
        Assert.assertArrayEquals(INPUT, (byte[]) reader.with(Base64Variants.PEM).readValue(
                quote("YWJjZGVmZ2hpamtsbW5vcHFyc3R1dnd4eXoxMjM0NTY3ODkwYWJjZGVmZ2hpamts\\nbW5vcHFyc3R1dnd4eXoxMjM0NTY3ODkwWA=="
        )));
    }

// com.fasterxml.jackson.databind.deser.jdk.JDKScalarsTest::testSequenceOfInts
    public void testSequenceOfInts() throws Exception
    {
        final int NR_OF_INTS = 100;

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < NR_OF_INTS; ++i) {
            sb.append(" ");
            sb.append(i);
        }
        JsonParser jp = MAPPER.getFactory().createParser(sb.toString());
        for (int i = 0; i < NR_OF_INTS; ++i) {
            Integer result = MAPPER.readValue(jp, Integer.class);
            assertEquals(Integer.valueOf(i), result);
        }
        jp.close();
    }

// com.fasterxml.jackson.databind.deser.jdk.JDKScalarsTest::testEmptyStringForWrappers
    public void testEmptyStringForWrappers() throws IOException
    {
        WrappersBean bean;

        bean = MAPPER.readValue("{\"booleanValue\":\"\"}", WrappersBean.class);
        assertNull(bean.booleanValue);
        bean = MAPPER.readValue("{\"byteValue\":\"\"}", WrappersBean.class);
        assertNull(bean.byteValue);

        
        bean = MAPPER.readValue("{\"charValue\":\"\"}", WrappersBean.class);
        assertNull(bean.charValue);

        bean = MAPPER.readValue("{\"shortValue\":\"\"}", WrappersBean.class);
        assertNull(bean.shortValue);
        bean = MAPPER.readValue("{\"intValue\":\"\"}", WrappersBean.class);
        assertNull(bean.intValue);
        bean = MAPPER.readValue("{\"longValue\":\"\"}", WrappersBean.class);
        assertNull(bean.longValue);
        bean = MAPPER.readValue("{\"floatValue\":\"\"}", WrappersBean.class);
        assertNull(bean.floatValue);
        bean = MAPPER.readValue("{\"doubleValue\":\"\"}", WrappersBean.class);
        assertNull(bean.doubleValue);
    }

// com.fasterxml.jackson.databind.deser.jdk.JDKScalarsTest::testEmptyStringForPrimitives
    public void testEmptyStringForPrimitives() throws IOException
    {
        PrimitivesBean bean;
        bean = MAPPER.readValue("{\"booleanValue\":\"\"}", PrimitivesBean.class);
        assertFalse(bean.booleanValue);
        bean = MAPPER.readValue("{\"byteValue\":\"\"}", PrimitivesBean.class);
        assertEquals((byte) 0, bean.byteValue);
        bean = MAPPER.readValue("{\"charValue\":\"\"}", PrimitivesBean.class);
        assertEquals((char) 0, bean.charValue);
        bean = MAPPER.readValue("{\"shortValue\":\"\"}", PrimitivesBean.class);
        assertEquals((short) 0, bean.shortValue);
        bean = MAPPER.readValue("{\"intValue\":\"\"}", PrimitivesBean.class);
        assertEquals(0, bean.intValue);
        bean = MAPPER.readValue("{\"longValue\":\"\"}", PrimitivesBean.class);
        assertEquals(0L, bean.longValue);
        bean = MAPPER.readValue("{\"floatValue\":\"\"}", PrimitivesBean.class);
        assertEquals(0.0f, bean.floatValue);
        bean = MAPPER.readValue("{\"doubleValue\":\"\"}", PrimitivesBean.class);
        assertEquals(0.0, bean.doubleValue);
    }

// com.fasterxml.jackson.databind.deser.jdk.JDKScalarsTest::testEmptyStringFailForPrimitives
    public void testEmptyStringFailForPrimitives() throws IOException
    {
        _verifyEmptyStringFailForPrimitives("booleanValue");
        _verifyEmptyStringFailForPrimitives("byteValue");
        _verifyEmptyStringFailForPrimitives("charValue");
        _verifyEmptyStringFailForPrimitives("shortValue");
        _verifyEmptyStringFailForPrimitives("intValue");
        _verifyEmptyStringFailForPrimitives("longValue");
        _verifyEmptyStringFailForPrimitives("floatValue");
        _verifyEmptyStringFailForPrimitives("doubleValue");
    }

// com.fasterxml.jackson.databind.deser.jdk.JDKScalarsTest::testNullForPrimitives
    public void testNullForPrimitives() throws IOException
    {
        
        PrimitivesBean bean = MAPPER.readValue(
                "{\"intValue\":null, \"booleanValue\":null, \"doubleValue\":null}",
                PrimitivesBean.class);
        assertNotNull(bean);
        assertEquals(0, bean.intValue);
        assertEquals(false, bean.booleanValue);
        assertEquals(0.0, bean.doubleValue);

        bean = MAPPER.readValue("{\"byteValue\":null, \"longValue\":null, \"floatValue\":null}",
                PrimitivesBean.class);
        assertNotNull(bean);
        assertEquals((byte) 0, bean.byteValue);
        assertEquals(0L, bean.longValue);
        assertEquals(0.0f, bean.floatValue);

        
        final ObjectReader reader = MAPPER
                .readerFor(PrimitivesBean.class)
                .with(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES);
        
        try {
            reader.readValue("{\"booleanValue\":null}");
            fail("Expected failure for boolean + null");
        } catch (JsonMappingException e) {
            verifyException(e, "Can not map `null` into type boolean");
        }
        
        try {
            reader.readValue("{\"byteValue\":null}");
            fail("Expected failure for byte + null");
        } catch (JsonMappingException e) {
            verifyException(e, "Can not map `null` into type byte");
        }
        try {
            reader.readValue("{\"charValue\":null}");
            fail("Expected failure for char + null");
        } catch (JsonMappingException e) {
            verifyException(e, "Can not map `null` into type char");
        }
        try {
            reader.readValue("{\"shortValue\":null}");
            fail("Expected failure for short + null");
        } catch (JsonMappingException e) {
            verifyException(e, "Can not map `null` into type short");
        }
        try {
            reader.readValue("{\"intValue\":null}");
            fail("Expected failure for int + null");
        } catch (JsonMappingException e) {
            verifyException(e, "Can not map `null` into type int");
        }
        try {
            reader.readValue("{\"longValue\":null}");
            fail("Expected failure for long + null");
        } catch (JsonMappingException e) {
            verifyException(e, "Can not map `null` into type long");
        }

        
        try {
            reader.readValue("{\"floatValue\":null}");
            fail("Expected failure for float + null");
        } catch (JsonMappingException e) {
            verifyException(e, "Can not map `null` into type float");
        }
        try {
            reader.readValue("{\"doubleValue\":null}");
            fail("Expected failure for double + null");
        } catch (JsonMappingException e) {
            verifyException(e, "Can not map `null` into type double");
        }
    }

// com.fasterxml.jackson.databind.deser.jdk.JDKScalarsTest::testNullForPrimitiveArrays
    public void testNullForPrimitiveArrays() throws IOException
    {
        _testNullForPrimitiveArrays(boolean[].class, Boolean.FALSE);
        _testNullForPrimitiveArrays(byte[].class, Byte.valueOf((byte) 0));
        _testNullForPrimitiveArrays(char[].class, Character.valueOf((char) 0), false);
        _testNullForPrimitiveArrays(short[].class, Short.valueOf((short)0));
        _testNullForPrimitiveArrays(int[].class, Integer.valueOf(0));
        _testNullForPrimitiveArrays(long[].class, Long.valueOf(0L));
        _testNullForPrimitiveArrays(float[].class, Float.valueOf(0f));
        _testNullForPrimitiveArrays(double[].class, Double.valueOf(0d));
    }

// com.fasterxml.jackson.databind.deser.jdk.JDKScalarsTest::testInvalidStringCoercionFail
    public void testInvalidStringCoercionFail() throws IOException
    {
        _testInvalidStringCoercionFail(boolean[].class);
        _testInvalidStringCoercionFail(byte[].class);

        

        _testInvalidStringCoercionFail(short[].class);
        _testInvalidStringCoercionFail(int[].class);
        _testInvalidStringCoercionFail(long[].class);
        _testInvalidStringCoercionFail(float[].class);
        _testInvalidStringCoercionFail(double[].class);
    }

// com.fasterxml.jackson.databind.deser.jdk.JDKStringLikeTypesTest::testByteBuffer
    public void testByteBuffer() throws Exception
    {
        byte[] INPUT = new byte[] { 1, 3, 9, -1, 6 };
        String exp = MAPPER.writeValueAsString(INPUT);
        ByteBuffer result = MAPPER.readValue(exp,  ByteBuffer.class); 
        assertNotNull(result);
        assertEquals(INPUT.length, result.remaining());
        for (int i = 0; i < INPUT.length; ++i) {
            assertEquals(INPUT[i], result.get());
        }
        assertEquals(0, result.remaining());
    }

// com.fasterxml.jackson.databind.deser.jdk.JDKStringLikeTypesTest::testCharset
    public void testCharset() throws Exception
    {
        Charset UTF8 = Charset.forName("UTF-8");
        assertSame(UTF8, MAPPER.readValue(quote("UTF-8"), Charset.class));
    }

// com.fasterxml.jackson.databind.deser.jdk.JDKStringLikeTypesTest::testClass
    public void testClass() throws IOException
    {
        ObjectMapper mapper = new ObjectMapper();
        assertSame(String.class, mapper.readValue(quote("java.lang.String"), Class.class));

        
        assertSame(Boolean.TYPE, mapper.readValue(quote("boolean"), Class.class));
        assertSame(Byte.TYPE, mapper.readValue(quote("byte"), Class.class));
        assertSame(Short.TYPE, mapper.readValue(quote("short"), Class.class));
        assertSame(Character.TYPE, mapper.readValue(quote("char"), Class.class));
        assertSame(Integer.TYPE, mapper.readValue(quote("int"), Class.class));
        assertSame(Long.TYPE, mapper.readValue(quote("long"), Class.class));
        assertSame(Float.TYPE, mapper.readValue(quote("float"), Class.class));
        assertSame(Double.TYPE, mapper.readValue(quote("double"), Class.class));
        assertSame(Void.TYPE, mapper.readValue(quote("void"), Class.class));
    }

// com.fasterxml.jackson.databind.deser.jdk.JDKStringLikeTypesTest::testClassWithParams
    public void testClassWithParams() throws IOException
    {
        String json = MAPPER.writeValueAsString(new ParamClassBean("Foobar"));

        ParamClassBean result = MAPPER.readValue(json, ParamClassBean.class);
        assertEquals("Foobar", result.name);
        assertSame(String.class, result.clazz);
    }

// com.fasterxml.jackson.databind.deser.jdk.JDKStringLikeTypesTest::testCurrency
    public void testCurrency() throws IOException
    {
        Currency usd = Currency.getInstance("USD");
        assertEquals(usd, new ObjectMapper().readValue(quote("USD"), Currency.class));
    }

// com.fasterxml.jackson.databind.deser.jdk.JDKStringLikeTypesTest::testFile
    public void testFile() throws Exception
    {
        
        File src = new File("/test").getAbsoluteFile();
        String abs = src.getAbsolutePath();

        
        String json = MAPPER.writeValueAsString(abs);
        File result = MAPPER.readValue(json, File.class);
        assertEquals(abs, result.getAbsolutePath());
    }

// com.fasterxml.jackson.databind.deser.jdk.JDKStringLikeTypesTest::testLocale
    public void testLocale() throws IOException
    {
        assertEquals(new Locale("en"), MAPPER.readValue(quote("en"), Locale.class));
        assertEquals(new Locale("es", "ES"), MAPPER.readValue(quote("es_ES"), Locale.class));
        assertEquals(new Locale("FI", "fi", "savo"),
                MAPPER.readValue(quote("fi_FI_savo"), Locale.class));
        assertEquals(new Locale("en", "US"),
                MAPPER.readValue(quote("en-US"), Locale.class));

        
        Locale loc = MAPPER.readValue(quote(""), Locale.class);
        assertSame(Locale.ROOT, loc);
    }

// com.fasterxml.jackson.databind.deser.jdk.JDKStringLikeTypesTest::testCharSequence
    public void testCharSequence() throws IOException
    {
        CharSequence cs = MAPPER.readValue("\"abc\"", CharSequence.class);
        assertEquals(String.class, cs.getClass());
        assertEquals("abc", cs.toString());
    }

// com.fasterxml.jackson.databind.deser.jdk.JDKStringLikeTypesTest::testInetAddress
    public void testInetAddress() throws IOException
    {
        InetAddress address = MAPPER.readValue(quote("127.0.0.1"), InetAddress.class);
        assertEquals("127.0.0.1", address.getHostAddress());

        
        final String HOST = "google.com";
        address = MAPPER.readValue(quote(HOST), InetAddress.class);
        assertEquals(HOST, address.getHostName());
    }

// com.fasterxml.jackson.databind.deser.jdk.JDKStringLikeTypesTest::testInetSocketAddress
    public void testInetSocketAddress() throws IOException
    {
        InetSocketAddress address = MAPPER.readValue(quote("127.0.0.1"), InetSocketAddress.class);
        assertEquals("127.0.0.1", address.getAddress().getHostAddress());

        InetSocketAddress ip6 = MAPPER.readValue(
                quote("2001:db8:85a3:8d3:1319:8a2e:370:7348"), InetSocketAddress.class);
        assertEquals("2001:db8:85a3:8d3:1319:8a2e:370:7348", ip6.getAddress().getHostAddress());

        InetSocketAddress ip6port = MAPPER.readValue(
                quote("[2001:db8:85a3:8d3:1319:8a2e:370:7348]:443"), InetSocketAddress.class);
        assertEquals("2001:db8:85a3:8d3:1319:8a2e:370:7348", ip6port.getAddress().getHostAddress());
        assertEquals(443, ip6port.getPort());

        
        final String HOST = "www.google.com";
        address = MAPPER.readValue(quote(HOST), InetSocketAddress.class);
        assertEquals(HOST, address.getHostName());

        final String HOST_AND_PORT = HOST+":80";
        address = MAPPER.readValue(quote(HOST_AND_PORT), InetSocketAddress.class);
        assertEquals(HOST, address.getHostName());
        assertEquals(80, address.getPort());
    }

// com.fasterxml.jackson.databind.deser.jdk.JDKStringLikeTypesTest::testRegexps
    public void testRegexps() throws IOException
    {
        final String PATTERN_STR = "abc:\\s?(\\d+)";
        Pattern exp = Pattern.compile(PATTERN_STR);
        
        String json = MAPPER.writeValueAsString(exp);
        Pattern result = MAPPER.readValue(json, Pattern.class);
        assertEquals(exp.pattern(), result.pattern());
    }

// com.fasterxml.jackson.databind.deser.jdk.JDKStringLikeTypesTest::testStackTraceElement
    public void testStackTraceElement() throws Exception
    {
        StackTraceElement elem = null;
        try {
            throw new IllegalStateException();
        } catch (Exception e) {
            elem = e.getStackTrace()[0];
        }
        String json = MAPPER.writeValueAsString(elem);
        StackTraceElement back = MAPPER.readValue(json, StackTraceElement.class);
        
        assertEquals("testStackTraceElement", back.getMethodName());
        assertEquals(elem.getLineNumber(), back.getLineNumber());
        assertEquals(elem.getClassName(), back.getClassName());
        assertEquals(elem.isNativeMethod(), back.isNativeMethod());
        assertTrue(back.getClassName().endsWith("JDKStringLikeTypesTest"));
        assertFalse(back.isNativeMethod());
    }

// com.fasterxml.jackson.databind.deser.jdk.JDKStringLikeTypesTest::testStackTraceElementWithCustom
    public void testStackTraceElementWithCustom() throws Exception
    {
        
        StackTraceBean bean = MAPPER.readValue(aposToQuotes("{'Location':'foobar'}"),
                StackTraceBean.class);
        assertNotNull(bean);
        assertNotNull(bean.location);
        assertEquals(StackTraceBean.NUM, bean.location.getLineNumber());

        
        ObjectMapper mapper = new ObjectMapper();
        SimpleModule module = new SimpleModule();
        module.addDeserializer(StackTraceElement.class, new MyStackTraceElementDeserializer());
        mapper.registerModule(module);
        
        StackTraceElement elem = mapper.readValue("123", StackTraceElement.class);
        assertNotNull(elem);
        assertEquals(StackTraceBean.NUM, elem.getLineNumber());
 
        
        
        IOException ioe = mapper.readValue(aposToQuotes("{'stackTrace':[ 123, 456 ]}"),
                IOException.class);
        assertNotNull(ioe);
        StackTraceElement[] traces = ioe.getStackTrace();
        assertNotNull(traces);
        assertEquals(2, traces.length);
        assertEquals(StackTraceBean.NUM, traces[0].getLineNumber());
        assertEquals(StackTraceBean.NUM, traces[1].getLineNumber());
    }

// com.fasterxml.jackson.databind.deser.jdk.JDKStringLikeTypesTest::testStringBuilder
    public void testStringBuilder() throws Exception
    {
        StringBuilder sb = MAPPER.readValue(quote("abc"), StringBuilder.class);
        assertEquals("abc", sb.toString());
    }

// com.fasterxml.jackson.databind.deser.jdk.JDKStringLikeTypesTest::testURI
    public void testURI() throws Exception
    {
        final ObjectReader reader = MAPPER.readerFor(URI.class);
        final URI value = new URI("http://foo.com");
        assertEquals(value, reader.readValue("\""+value.toString()+"\""));

        
        URI result = reader.readValue(quote(""));
        assertNotNull(result);
        assertEquals(URI.create(""), result);
        
        
        try {
            result = reader.readValue(quote("a b"));
            fail("Should not accept malformed URI, instead got: "+result);
        } catch (InvalidFormatException e) {
            verifyException(e, "not a valid textual representation");
        }
    }

// com.fasterxml.jackson.databind.deser.jdk.JDKStringLikeTypesTest::testURL
    public void testURL() throws Exception
    {
        URL exp = new URL("http://foo.com");
        assertEquals(exp, MAPPER.readValue("\""+exp.toString()+"\"", URL.class));

        
        TokenBuffer buf = new TokenBuffer(null, false);
        buf.writeObject(null);
        assertNull(MAPPER.readValue(buf.asParser(), URL.class));
        buf.close();

        
        buf = new TokenBuffer(null, false);
        buf.writeObject(exp);
        assertSame(exp, MAPPER.readValue(buf.asParser(), URL.class));
        buf.close();

        
        try {
            URL result = MAPPER.readValue(quote("a b"), URL.class);
            fail("Should not accept malformed URI, instead got: "+result);
        } catch (InvalidFormatException e) {
            verifyException(e, "not a valid textual representation");
        }
    }

// com.fasterxml.jackson.databind.deser.jdk.JDKStringLikeTypesTest::testUUID
    public void testUUID() throws Exception
    {
        final ObjectMapper mapper = objectMapper();
        
        final String NULL_UUID = "00000000-0000-0000-0000-000000000000";
        
        for (String value : new String[] {
                "76e6d183-5f68-4afa-b94a-922c1fdb83f8",
                "540a88d1-e2d8-4fb1-9396-9212280d0a7f",
                "2c9e441d-1cd0-472d-9bab-69838f877574",
                "591b2869-146e-41d7-8048-e8131f1fdec5",
                "82994ac2-7b23-49f2-8cc5-e24cf6ed77be",
                "00000007-0000-0000-0000-000000000000"
        }) {
            
            mapper.disable(DeserializationFeature.UNWRAP_SINGLE_VALUE_ARRAYS);
            
            UUID uuid = UUID.fromString(value);
            assertEquals(uuid,
                    mapper.readValue(quote(value), UUID.class));
        }
        
        
        final String TEMPL = NULL_UUID;
        final String chars = "123456789abcdefABCDEF";

        for (int i = 0; i < chars.length(); ++i) {
            String value = TEMPL.replace('0', chars.charAt(i));
            assertEquals(UUID.fromString(value).toString(),
                    mapper.readValue(quote(value), UUID.class).toString());
        }

        
        String base64 = Base64Variants.getDefaultVariant().encode(new byte[16]);
        assertEquals(UUID.fromString(NULL_UUID),
                mapper.readValue(quote(base64), UUID.class));
    }

// com.fasterxml.jackson.databind.deser.jdk.JDKStringLikeTypesTest::testUUIDInvalid
    public void testUUIDInvalid() throws Exception
    {
        
        try {
            MAPPER.readValue(quote("abcde"), UUID.class);
            fail("Should fail on invalid UUID string");
        } catch (InvalidFormatException e) {
            verifyException(e, "UUID has to be represented by standard");
        }
        try {
            MAPPER.readValue(quote("76e6d183-5f68-4afa-b94a-922c1fdb83fx"), UUID.class);
            fail("Should fail on invalid UUID string");
        } catch (InvalidFormatException e) {
            verifyException(e, "non-hex character 'x'");
        }
        
    }

// com.fasterxml.jackson.databind.deser.jdk.JDKStringLikeTypesTest::testUUIDAux
    public void testUUIDAux() throws Exception
    {
        
        final UUID value = UUID.fromString("76e6d183-5f68-4afa-b94a-922c1fdb83f8");

        
        TokenBuffer buf = new TokenBuffer(null, false);
        buf.writeObject(null);
        assertNull(MAPPER.readValue(buf.asParser(), UUID.class));
        buf.close();

        
        buf = new TokenBuffer(null, false);
        buf.writeObject(value);
        assertSame(value, MAPPER.readValue(buf.asParser(), UUID.class));

        
        
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(bytes);
        out.writeLong(value.getMostSignificantBits());
        out.writeLong(value.getLeastSignificantBits());
        byte[] data = bytes.toByteArray();
        assertEquals(16, data.length);
        
        buf.writeObject(data);

        UUID value2 = MAPPER.readValue(buf.asParser(), UUID.class);
        
        assertEquals(value, value2);
        buf.close();
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
        Map<String,Integer> result = MAPPER.readValue
            (JSON, new TypeReference<HashMap<Integer,Boolean>>() { });

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

// com.fasterxml.jackson.databind.deser.jdk.MapKeyDeserializationTest::testBooleanMapKeyDeserialization
    public void testBooleanMapKeyDeserialization() throws Exception
    {
        TypeReference<?> type = new TypeReference<MapWrapper<Boolean, String>>() { };
        MapWrapper<byte[], String> result = MAPPER.readValue(aposToQuotes("{'map':{'true':'foobar'}}"), type);
                
        assertEquals(1, result.map.size());
        Assert.assertEquals(Boolean.TRUE, result.map.entrySet().iterator().next().getKey());

        result = MAPPER.readValue(aposToQuotes("{'map':{'false':'foobar'}}"), type);
        assertEquals(1, result.map.size());
        Assert.assertEquals(Boolean.FALSE, result.map.entrySet().iterator().next().getKey());
    }

// com.fasterxml.jackson.databind.deser.jdk.MapKeyDeserializationTest::testByteMapKeyDeserialization
    public void testByteMapKeyDeserialization() throws Exception
    {
        TypeReference<?> type = new TypeReference<MapWrapper<Byte, String>>() { };
        MapWrapper<byte[], String> result = MAPPER.readValue(aposToQuotes("{'map':{'13':'foobar'}}"), type);
        assertEquals(1, result.map.size());
        Assert.assertEquals(Byte.valueOf((byte) 13), result.map.entrySet().iterator().next().getKey());
    }

// com.fasterxml.jackson.databind.deser.jdk.MapKeyDeserializationTest::testShortMapKeyDeserialization
    public void testShortMapKeyDeserialization() throws Exception
    {
        TypeReference<?> type = new TypeReference<MapWrapper<Short, String>>() { };
        MapWrapper<byte[], String> result = MAPPER.readValue(aposToQuotes("{'map':{'13':'foobar'}}"), type);
        assertEquals(1, result.map.size());
        Assert.assertEquals(Short.valueOf((short) 13), result.map.entrySet().iterator().next().getKey());
    }

// com.fasterxml.jackson.databind.deser.jdk.MapKeyDeserializationTest::testIntegerMapKeyDeserialization
    public void testIntegerMapKeyDeserialization() throws Exception
    {
        TypeReference<?> type = new TypeReference<MapWrapper<Integer, String>>() { };
        MapWrapper<byte[], String> result = MAPPER.readValue(aposToQuotes("{'map':{'-3':'foobar'}}"), type);
        assertEquals(1, result.map.size());
        Assert.assertEquals(Integer.valueOf(-3), result.map.entrySet().iterator().next().getKey());
    }

// com.fasterxml.jackson.databind.deser.jdk.MapKeyDeserializationTest::testLongMapKeyDeserialization
    public void testLongMapKeyDeserialization() throws Exception
    {
        TypeReference<?> type = new TypeReference<MapWrapper<Long, String>>() { };
        MapWrapper<byte[], String> result = MAPPER.readValue(aposToQuotes("{'map':{'42':'foobar'}}"), type);
        assertEquals(1, result.map.size());
        Assert.assertEquals(Long.valueOf(42), result.map.entrySet().iterator().next().getKey());
    }

// com.fasterxml.jackson.databind.deser.jdk.MapKeyDeserializationTest::testFloatMapKeyDeserialization
    public void testFloatMapKeyDeserialization() throws Exception
    {
        TypeReference<?> type = new TypeReference<MapWrapper<Float, String>>() { };
        MapWrapper<byte[], String> result = MAPPER.readValue(aposToQuotes("{'map':{'3.5':'foobar'}}"), type);
        assertEquals(1, result.map.size());
        Assert.assertEquals(Float.valueOf(3.5f), result.map.entrySet().iterator().next().getKey());
    }

// com.fasterxml.jackson.databind.deser.jdk.MapKeyDeserializationTest::testDoubleMapKeyDeserialization
    public void testDoubleMapKeyDeserialization() throws Exception
    {
        TypeReference<?> type = new TypeReference<MapWrapper<Double, String>>() { };
        MapWrapper<byte[], String> result = MAPPER.readValue(aposToQuotes("{'map':{'0.25':'foobar'}}"), type);
        assertEquals(1, result.map.size());
        Assert.assertEquals(Double.valueOf(0.25), result.map.entrySet().iterator().next().getKey());
    }

// com.fasterxml.jackson.databind.deser.jdk.MapKeyDeserializationTest::testDeserializeKeyViaFactory
    public void testDeserializeKeyViaFactory() throws Exception
    {
        Map<FullName, Double> map =
            MAPPER.readValue("{\"first.last\": 42}",
                    new TypeReference<Map<FullName, Double>>() { });
        Map.Entry<FullName, Double> entry = map.entrySet().iterator().next();
        FullName key = entry.getKey();
        assertEquals(key._firstname, "first");
        assertEquals(key._lastname, "last");
        assertEquals(entry.getValue().doubleValue(), 42, 0);
    }

// com.fasterxml.jackson.databind.deser.jdk.MapKeyDeserializationTest::testByteArrayMapKeyDeserialization
    public void testByteArrayMapKeyDeserialization() throws Exception
    {
        byte[] binary = new byte[] { 1, 2, 4, 8, 16, 33, 79 };
        String encoded = Base64Variants.MIME.encode(binary);

        MapWrapper<byte[], String> result = MAPPER.readValue(
                aposToQuotes("{'map':{'"+encoded+"':'foobar'}}"),
                new TypeReference<MapWrapper<byte[], String>>() { });
        assertEquals(1, result.map.size());
        Map.Entry<byte[],String> entry = result.map.entrySet().iterator().next();
        assertEquals("foobar", entry.getValue());
        byte[] key = entry.getKey();
        Assert.assertArrayEquals(binary, key);
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

// com.fasterxml.jackson.databind.deser.jdk.UntypedDeserializationTest::testSampleDoc
    public void testSampleDoc() throws Exception
    {
        final String JSON = SAMPLE_DOC_JSON_SPEC;

        
        Object root = MAPPER.readValue(JSON, Object.class);

        assertType(root, Map.class);
        Map<?,?> rootMap = (Map<?,?>) root;
        assertEquals(1, rootMap.size());
        Map.Entry<?,?> rootEntry =  rootMap.entrySet().iterator().next();
        assertEquals("Image", rootEntry.getKey());
        Object image = rootEntry.getValue();
        assertType(image, Map.class);
        Map<?,?> imageMap = (Map<?,?>) image;
        assertEquals(5, imageMap.size());

        Object value = imageMap.get("Width");
        assertType(value, Integer.class);
        assertEquals(Integer.valueOf(SAMPLE_SPEC_VALUE_WIDTH), value);

        value = imageMap.get("Height");
        assertType(value, Integer.class);
        assertEquals(Integer.valueOf(SAMPLE_SPEC_VALUE_HEIGHT), value);

        assertEquals(SAMPLE_SPEC_VALUE_TITLE, imageMap.get("Title"));

        
        value = imageMap.get("Thumbnail");
        assertType(value, Map.class);
        Map<?,?> tnMap = (Map<?,?>) value;
        assertEquals(3, tnMap.size());

        assertEquals(Integer.valueOf(SAMPLE_SPEC_VALUE_TN_HEIGHT), tnMap.get("Height"));
        
        assertEquals(SAMPLE_SPEC_VALUE_TN_WIDTH, tnMap.get("Width"));
        assertEquals(SAMPLE_SPEC_VALUE_TN_URL, tnMap.get("Url"));

        
        value = imageMap.get("IDs");
        assertType(value, List.class);
        List<Object> ids = (List<Object>) value;
        assertEquals(4, ids.size());
        assertEquals(Integer.valueOf(SAMPLE_SPEC_VALUE_TN_ID1), ids.get(0));
        assertEquals(Integer.valueOf(SAMPLE_SPEC_VALUE_TN_ID2), ids.get(1));
        assertEquals(Integer.valueOf(SAMPLE_SPEC_VALUE_TN_ID3), ids.get(2));
        assertEquals(Integer.valueOf(SAMPLE_SPEC_VALUE_TN_ID4), ids.get(3));

        
    }

// com.fasterxml.jackson.databind.deser.jdk.UntypedDeserializationTest::testUntypedMap
    public void testUntypedMap() throws Exception
    {
        
        String JSON = "{ \"foo\" : \"bar\", \"crazy\" : true, \"null\" : null }";

        
        @SuppressWarnings("unchecked")
        Map<String,Object> result = (Map<String,Object>)MAPPER.readValue(JSON, Object.class);
        assertNotNull(result);
        assertTrue(result instanceof Map<?,?>);

        assertEquals(3, result.size());

        assertEquals("bar", result.get("foo"));
        assertEquals(Boolean.TRUE, result.get("crazy"));
        assertNull(result.get("null"));

        
        assertNull(result.get("bar"));
        assertNull(result.get(3));
    }

// com.fasterxml.jackson.databind.deser.jdk.UntypedDeserializationTest::testNestedUntypes
    public void testNestedUntypes() throws IOException
    {
        
        Object root = MAPPER.readValue(aposToQuotes("{'a':3,'b':[1,2]}"),
                Object.class);
        assertTrue(root instanceof Map<?,?>);
        Map<?,?> map = (Map<?,?>) root;
        assertEquals(2, map.size());
        assertEquals(Integer.valueOf(3), map.get("a"));
        Object ob = map.get("b");
        assertTrue(ob instanceof List<?>);
        List<?> l = (List<?>) ob;
        assertEquals(2, l.size());
        assertEquals(Integer.valueOf(2), l.get(1));
    }

// com.fasterxml.jackson.databind.deser.jdk.UntypedDeserializationTest::testObjectSerializeWithLong
    public void testObjectSerializeWithLong() throws IOException
    {
        final ObjectMapper mapper = new ObjectMapper();
        mapper.enableDefaultTyping(DefaultTyping.JAVA_LANG_OBJECT, As.PROPERTY);
        final long VALUE = 1337800584532L;

        String serialized = "{\"timestamp\":"+VALUE+"}";
        
        JsonNode deserialized = mapper.readTree(serialized);
        assertEquals(VALUE, deserialized.get("timestamp").asLong());
        
        Map<?,?> deserMap = mapper.readValue(serialized, Map.class);
        Number n = (Number) deserMap.get("timestamp");
        assertNotNull(n);
        assertSame(Long.class, n.getClass());
        assertEquals(Long.valueOf(VALUE), n);
    }

// com.fasterxml.jackson.databind.deser.jdk.UntypedDeserializationTest::testUntypedWithCustomScalarDesers
    public void testUntypedWithCustomScalarDesers() throws IOException
    {
        SimpleModule m = new SimpleModule("test-module");
        m.addDeserializer(String.class, new UCStringDeserializer());
        m.addDeserializer(Number.class, new CustomNumberDeserializer(13));
        final ObjectMapper mapper = new ObjectMapper()
            .registerModule(m);

        Object ob = mapper.readValue("{\"a\":\"b\", \"nr\":1 }", Object.class);
        assertTrue(ob instanceof Map);
        Object value = ((Map<?,?>) ob).get("a");
        assertNotNull(value);
        assertTrue(value instanceof String);
        assertEquals("B", value);

        value = ((Map<?,?>) ob).get("nr");
        assertNotNull(value);
        assertTrue(value instanceof Number);
        assertEquals(Integer.valueOf(13), value);
    }

// com.fasterxml.jackson.databind.deser.jdk.UntypedDeserializationTest::testNonVanilla
    public void testNonVanilla() throws IOException
    {
        SimpleModule m = new SimpleModule("test-module");
        m.addDeserializer(String.class, new UCStringDeserializer());
        final ObjectMapper mapper = new ObjectMapper()
            .registerModule(m);

        
        List<?> l = (List<?>) mapper.readValue("[ true, false, 7, 0.5, \"foo\"]", Object.class);
        assertEquals(5, l.size());
        assertEquals(Boolean.TRUE, l.get(0));
        assertEquals(Boolean.FALSE, l.get(1));
        assertEquals(Integer.valueOf(7), l.get(2));
        assertEquals(Double.valueOf(0.5), l.get(3));
        assertEquals("FOO", l.get(4));

        l = (List<?>) mapper.readValue("[ {}, [] ]", Object.class);
        assertEquals(2, l.size());
        assertTrue(l.get(0) instanceof Map<?,?>);
        assertTrue(l.get(1) instanceof List<?>);

        ObjectReader rDefault = mapper.readerFor(WrappedPolymorphicUntyped.class);
        ObjectReader rAlt = rDefault
                .with(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS,
                        DeserializationFeature.USE_BIG_INTEGER_FOR_INTS);
        WrappedPolymorphicUntyped w;

        w = rDefault.readValue(aposToQuotes("{'value':10}"));
        assertEquals(Integer.valueOf(10), w.value);
        w = rAlt.readValue(aposToQuotes("{'value':10}"));
        assertEquals(BigInteger.TEN, w.value);

        w = rDefault.readValue(aposToQuotes("{'value':5.0}"));
        assertEquals(Double.valueOf(5.0), w.value);
        w = rAlt.readValue(aposToQuotes("{'value':5.0}"));
        assertEquals(new BigDecimal("5.0"), w.value);

        StringBuilder sb = new StringBuilder(100).append("[0");
        for (int i = 1; i < 100; ++i) {
            sb.append(", ").append(i);
        }
        sb.append("]");
        final String INT_ARRAY_JSON = sb.toString();

        
        Object ob = mapper.readerFor(Object.class)
                .with(DeserializationFeature.USE_JAVA_ARRAY_FOR_JSON_ARRAY)
                .readValue(INT_ARRAY_JSON);
        assertTrue(ob instanceof Object[]);
        Object[] obs = (Object[]) ob;
        for (int i = 0; i < 100; ++i) {
            assertEquals(Integer.valueOf(i), obs[i]);
        }
    }

// com.fasterxml.jackson.databind.deser.jdk.UntypedDeserializationTest::testUntypedWithListDeser
    public void testUntypedWithListDeser() throws IOException
    {
        SimpleModule m = new SimpleModule("test-module");
        m.addDeserializer(List.class, new ListDeserializer());
        final ObjectMapper mapper = new ObjectMapper()
            .registerModule(m);

        
        Object ob = mapper.readValue("[1, 2, true]", Object.class);
        assertTrue(ob instanceof List<?>);
        List<?> l = (List<?>) ob;
        assertEquals(3, l.size());
        assertEquals("X1", l.get(0));
        assertEquals("X2", l.get(1));
        assertEquals("Xtrue", l.get(2));
    }

// com.fasterxml.jackson.databind.deser.jdk.UntypedDeserializationTest::testUntypedWithMapDeser
    public void testUntypedWithMapDeser() throws IOException
    {
        SimpleModule m = new SimpleModule("test-module");
        m.addDeserializer(Map.class, new YMapDeserializer());
        final ObjectMapper mapper = new ObjectMapper()
            .registerModule(m);

        
        Object ob = mapper.readValue("{\"a\":true}", Object.class);
        assertTrue(ob instanceof Map<?,?>);
        Map<?,?> map = (Map<?,?>) ob;
        assertEquals(1, map.size());
        assertEquals("Ytrue", map.get("a"));
    }

// com.fasterxml.jackson.databind.deser.jdk.UntypedDeserializationTest::testNestedUntyped989
    public void testNestedUntyped989() throws IOException
    {
        DelegatingUntyped pojo;
        ObjectReader r = MAPPER.readerFor(DelegatingUntyped.class);

        pojo = r.readValue("[]");
        assertTrue(pojo.value instanceof List);
        pojo = r.readValue("[{}]");
        assertTrue(pojo.value instanceof List);
        
        pojo = r.readValue("{}");
        assertTrue(pojo.value instanceof Map);
        pojo = r.readValue("{\"a\":[]}");
        assertTrue(pojo.value instanceof Map);
    }

// com.fasterxml.jackson.databind.deser.jdk.UntypedDeserializationTest::testUntypedWithJsonArrays
    public void testUntypedWithJsonArrays() throws Exception
    {
        
        Object ob = MAPPER.readValue("[1]", Object.class);
        assertTrue(ob instanceof List<?>);

        
        MAPPER.configure(DeserializationFeature.USE_JAVA_ARRAY_FOR_JSON_ARRAY, true);
        ob = MAPPER.readValue("[1]", Object.class);
        assertEquals(Object[].class, ob.getClass());
    }

// com.fasterxml.jackson.databind.deser.jdk.UntypedDeserializationTest::testUntypedIntAsLong
    public void testUntypedIntAsLong() throws Exception
    {
        final String JSON = aposToQuotes("{'value':3}");
        WrappedUntyped1460 w = MAPPER.readerFor(WrappedUntyped1460.class)
                .readValue(JSON);
        assertEquals(Integer.valueOf(3), w.value);

        w = MAPPER.readerFor(WrappedUntyped1460.class)
                .with(DeserializationFeature.USE_LONG_FOR_INTS)
                .readValue(JSON);
        assertEquals(Long.valueOf(3), w.value);
    }

// com.fasterxml.jackson.databind.deser.merge.ArrayMergeTest::testObjectArrayMerging
    public void testObjectArrayMerging() throws Exception
    {
        MergedX<Object[]> input = new MergedX<Object[]>(new Object[] {
                "foo"
        });
        final JavaType type = MAPPER.getTypeFactory().constructType(new TypeReference<MergedX<Object[]>>() {});
        MergedX<Object[]> result = MAPPER.readerFor(type)
                .withValueToUpdate(input)
                .readValue(aposToQuotes("{'value':['bar']}"));
        assertSame(input, result);
        assertEquals(2, result.value.length);
        assertEquals("foo", result.value[0]);
        assertEquals("bar", result.value[1]);

        
        result = MAPPER.readerFor(type)
                .with(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
                .withValueToUpdate(input)
                .readValue(aposToQuotes("{'value':'zap'}"));
        assertSame(input, result);
        assertEquals(3, result.value.length);
        assertEquals("foo", result.value[0]);
        assertEquals("bar", result.value[1]);
        assertEquals("zap", result.value[2]);
    }

// com.fasterxml.jackson.databind.deser.merge.ArrayMergeTest::testStringArrayMerging
    public void testStringArrayMerging() throws Exception
    {
        MergedX<String[]> input = new MergedX<String[]>(new String[] { "foo" });
        MergedX<String[]> result = MAPPER
                .readerFor(new TypeReference<MergedX<String[]>>() {})
                .withValueToUpdate(input)
                .readValue(aposToQuotes("{'value':['bar']}"));
        assertSame(input, result);
        assertEquals(2, result.value.length);
        assertEquals("foo", result.value[0]);
        assertEquals("bar", result.value[1]);
    }

// com.fasterxml.jackson.databind.deser.merge.ArrayMergeTest::testBooleanArrayMerging
    public void testBooleanArrayMerging() throws Exception
    {
        MergedX<boolean[]> input = new MergedX<boolean[]>(new boolean[] { true, false });
        MergedX<boolean[]> result = MAPPER
                .readerFor(new TypeReference<MergedX<boolean[]>>() {})
                .withValueToUpdate(input)
                .readValue(aposToQuotes("{'value':[true]}"));
        assertSame(input, result);
        assertEquals(3, result.value.length);
        Assert.assertArrayEquals(new boolean[] { true, false, true }, result.value);
    }

// com.fasterxml.jackson.databind.deser.merge.ArrayMergeTest::testByteArrayMerging
    public void testByteArrayMerging() throws Exception
    {
        MergedX<byte[]> input = new MergedX<byte[]>(new byte[] { 1, 2 });
        MergedX<byte[]> result = MAPPER
                .readerFor(new TypeReference<MergedX<byte[]>>() {})
                .withValueToUpdate(input)
                .readValue(aposToQuotes("{'value':[4, 6.0, null]}"));
        assertSame(input, result);
        assertEquals(5, result.value.length);
        Assert.assertArrayEquals(new byte[] { 1, 2, 4, 6, 0 }, result.value);
    }

// com.fasterxml.jackson.databind.deser.merge.ArrayMergeTest::testShortArrayMerging
    public void testShortArrayMerging() throws Exception
    {
        MergedX<short[]> input = new MergedX<short[]>(new short[] { 1, 2 });
        MergedX<short[]> result = MAPPER
                .readerFor(new TypeReference<MergedX<short[]>>() {})
                .withValueToUpdate(input)
                .readValue(aposToQuotes("{'value':[4, 6]}"));
        assertSame(input, result);
        assertEquals(4, result.value.length);
        Assert.assertArrayEquals(new short[] { 1, 2, 4, 6 }, result.value);
    }

// com.fasterxml.jackson.databind.deser.merge.ArrayMergeTest::testCharArrayMerging
    public void testCharArrayMerging() throws Exception
    {
        MergedX<char[]> input = new MergedX<char[]>(new char[] { 'a', 'b' });
        MergedX<char[]> result = MAPPER
                .readerFor(new TypeReference<MergedX<char[]>>() {})
                .withValueToUpdate(input)
                .readValue(aposToQuotes("{'value':['c']}"));
        assertSame(input, result);
        Assert.assertArrayEquals(new char[] { 'a', 'b', 'c' }, result.value);

        
        input = new MergedX<char[]>(new char[] { });
        result = MAPPER
                .readerFor(new TypeReference<MergedX<char[]>>() {})
                .withValueToUpdate(input)
                .readValue(aposToQuotes("{'value':['c']}"));
        assertSame(input, result);
        Assert.assertArrayEquals(new char[] { 'c' }, result.value);
    }

// com.fasterxml.jackson.databind.deser.merge.ArrayMergeTest::testIntArrayMerging
    public void testIntArrayMerging() throws Exception
    {
        MergedX<int[]> input = new MergedX<int[]>(new int[] { 1, 2 });
        MergedX<int[]> result = MAPPER
                .readerFor(new TypeReference<MergedX<int[]>>() {})
                .withValueToUpdate(input)
                .readValue(aposToQuotes("{'value':[4, 6]}"));
        assertSame(input, result);
        assertEquals(4, result.value.length);
        Assert.assertArrayEquals(new int[] { 1, 2, 4, 6 }, result.value);

        
        input = new MergedX<int[]>(new int[] { 3, 4, 6 });
        result = MAPPER
                .readerFor(new TypeReference<MergedX<int[]>>() {})
                .withValueToUpdate(input)
                .readValue(aposToQuotes("{'value':[ ]}"));
        assertSame(input, result);
        Assert.assertArrayEquals(new int[] { 3, 4, 6 }, result.value);
    }

// com.fasterxml.jackson.databind.deser.merge.ArrayMergeTest::testLongArrayMerging
    public void testLongArrayMerging() throws Exception
    {
        MergedX<long[]> input = new MergedX<long[]>(new long[] { 1, 2 });
        MergedX<long[]> result = MAPPER
                .readerFor(new TypeReference<MergedX<long[]>>() {})
                .withValueToUpdate(input)
                .readValue(aposToQuotes("{'value':[4, 6]}"));
        assertSame(input, result);
        assertEquals(4, result.value.length);
        Assert.assertArrayEquals(new long[] { 1, 2, 4, 6 }, result.value);
    }

// com.fasterxml.jackson.databind.deser.merge.CollectionMergeTest::testCollectionMerging
    public void testCollectionMerging() throws Exception
    {
        CollectionWrapper w = MAPPER.readValue(aposToQuotes("{'bag':['b']}"), CollectionWrapper.class);
        assertEquals(2, w.bag.size());
        assertTrue(w.bag.contains("a"));
        assertTrue(w.bag.contains("b"));
    }

// com.fasterxml.jackson.databind.deser.merge.CollectionMergeTest::testListMerging
    public void testListMerging() throws Exception
    {
        MergedList w = MAPPER.readValue(aposToQuotes("{'values':['x']}"), MergedList.class);
        assertEquals(2, w.values.size());
        assertTrue(w.values.contains("a"));
        assertTrue(w.values.contains("x"));
    }

// com.fasterxml.jackson.databind.deser.merge.CollectionMergeTest::testGenericListMerging
    public void testGenericListMerging() throws Exception
    {
        Collection<String> l = new ArrayList<>();
        l.add("foo");
        MergedX<Collection<String>> input = new MergedX<Collection<String>>(l);

        MergedX<Collection<String>> result = MAPPER
                .readerFor(new TypeReference<MergedX<Collection<String>>>() {})
                .withValueToUpdate(input)
                .readValue(aposToQuotes("{'value':['bar']}"));
        assertSame(input, result);
        assertEquals(2, result.value.size());
        Iterator<String> it = result.value.iterator();
        assertEquals("foo", it.next());
        assertEquals("bar", it.next());
    }

// com.fasterxml.jackson.databind.deser.merge.CollectionMergeTest::testEnumSetMerging
    public void testEnumSetMerging() throws Exception
    {
        MergedEnumSet result = MAPPER.readValue(aposToQuotes("{'abc':['A']}"), MergedEnumSet.class);
        assertEquals(2, result.abc.size());
        assertTrue(result.abc.contains(ABC.B)); 
        assertTrue(result.abc.contains(ABC.A)); 
    }

// com.fasterxml.jackson.databind.deser.merge.MapMergeTest::testShallowMapMerging
    public void testShallowMapMerging() throws Exception
    {
        MergedMap v = MAPPER.readValue(aposToQuotes("{'values':{'c':'y'}}"), MergedMap.class);
        assertEquals(2, v.values.size());
        assertEquals("y", v.values.get("c"));
        assertEquals("x", v.values.get("a"));
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

// com.fasterxml.jackson.databind.deser.merge.MergeWithNullTest::testBeanMergingWithNullDefault
    public void testBeanMergingWithNullDefault() throws Exception
    {
        
        ConfigDefault config = MAPPER.readerForUpdating(new ConfigDefault(5, 7))
                .readValue(aposToQuotes("{'loc':null}"));
        assertNotNull(config);
        assertNull(config.loc);

        

        
        
        ObjectMapper mapper = newObjectMapper();
        mapper.configOverride(AB.class)
            .setSetterInfo(JsonSetter.Value.forValueNulls(Nulls.SKIP));
        config = mapper.readerForUpdating(new ConfigDefault(137, -3))
                .readValue(aposToQuotes("{'loc':null}"));
        assertNotNull(config.loc);
        assertEquals(137, config.loc.a);
        assertEquals(-3, config.loc.b);

        
        mapper = newObjectMapper();
        mapper.setDefaultSetterInfo(JsonSetter.Value.forValueNulls(Nulls.SKIP));
        config = mapper.readerForUpdating(new ConfigDefault(12, 34))
                .readValue(aposToQuotes("{'loc':null}"));
        assertNotNull(config.loc);
        assertEquals(12, config.loc.a);
        assertEquals(34, config.loc.b);
    }

// com.fasterxml.jackson.databind.deser.merge.MergeWithNullTest::testBeanMergingWithNullSkip
    public void testBeanMergingWithNullSkip() throws Exception
    {
        ConfigSkipNull config = MAPPER.readerForUpdating(new ConfigSkipNull(5, 7))
                .readValue(aposToQuotes("{'loc':null}"));
        assertNotNull(config);
        assertNotNull(config.loc);
        assertEquals(5, config.loc.a);
        assertEquals(7, config.loc.b);
    }

// com.fasterxml.jackson.databind.deser.merge.MergeWithNullTest::testBeanMergingWithNullSet
    public void testBeanMergingWithNullSet() throws Exception
    {
        ConfigAllowNullOverwrite config = MAPPER.readerForUpdating(new ConfigAllowNullOverwrite(5, 7))
                .readValue(aposToQuotes("{'loc':null}"));
        assertNotNull(config);
        assertNull(config.loc);
    }

// com.fasterxml.jackson.databind.deser.merge.MergeWithNullTest::testSetterlessMergingWithNull
    public void testSetterlessMergingWithNull() throws Exception
    {
        NoSetterConfig input = new NoSetterConfig();
        NoSetterConfig result = MAPPER.readerForUpdating(input)
                .readValue(aposToQuotes("{'value':null}"));
        assertNotNull(result.getValue());
        assertEquals(2, result.getValue().a);
        assertEquals(3, result.getValue().b);
        assertSame(input, result);
    }

// com.fasterxml.jackson.databind.deser.merge.NodeMergeTest::testObjectNodeUpdateValue
    public void testObjectNodeUpdateValue() throws Exception
    {
        ObjectNode base = MAPPER.createObjectNode();
        base.put("first", "foo");
        assertSame(base,
                MAPPER.readerForUpdating(base)
                .readValue(aposToQuotes("{'second':'bar', 'third':5, 'fourth':true}")));
        assertEquals(4, base.size());
        assertEquals("bar", base.path("second").asText());
        assertEquals("foo", base.path("first").asText());
        assertEquals(5, base.path("third").asInt());
        assertTrue(base.path("fourth").asBoolean());
    }

// com.fasterxml.jackson.databind.deser.merge.NodeMergeTest::testObjectNodeMerge
    public void testObjectNodeMerge() throws Exception
    {
        ObjectNodeWrapper w = MAPPER.readValue(aposToQuotes("{'props':{'stuff':'xyz'}}"),
                ObjectNodeWrapper.class);
        assertEquals(2, w.props.size());
        assertEquals("enabled", w.props.path("default").asText());
        assertEquals("xyz", w.props.path("stuff").asText());
    }

// com.fasterxml.jackson.databind.deser.merge.NodeMergeTest::testObjectDeepUpdate
    public void testObjectDeepUpdate() throws Exception
    {
        ObjectNode base = MAPPER.createObjectNode();
        ObjectNode props = base.putObject("props");
        props.put("base", 123);
        props.put("value", 456);
        ArrayNode a = props.putArray("array");
        a.add(true);
        base.putNull("misc");
        assertSame(base,
                MAPPER.readerForUpdating(base)
                .readValue(aposToQuotes(
                        "{'props':{'value':true, 'extra':25.5, 'array' : [ 3 ]}}")));
        assertEquals(2, base.size());
        ObjectNode resultProps = (ObjectNode) base.get("props");
        assertEquals(4, resultProps.size());
        
        assertEquals(123, resultProps.path("base").asInt());
        assertTrue(resultProps.path("value").asBoolean());
        assertEquals(25.5, resultProps.path("extra").asDouble());
        JsonNode n = resultProps.get("array");
        assertEquals(ArrayNode.class, n.getClass());
        assertEquals(2, n.size());
        assertEquals(3, n.get(1).asInt());
    }

// com.fasterxml.jackson.databind.deser.merge.NodeMergeTest::testArrayNodeUpdateValue
    public void testArrayNodeUpdateValue() throws Exception
    {
        ArrayNode base = MAPPER.createArrayNode();
        base.add("first");
        assertSame(base,
                MAPPER.readerForUpdating(base)
                .readValue(aposToQuotes("['second',false,null]")));
        assertEquals(4, base.size());
        assertEquals("first", base.path(0).asText());
        assertEquals("second", base.path(1).asText());
        assertFalse(base.path(2).asBoolean());
        assertTrue(base.path(3).isNull());
    }

// com.fasterxml.jackson.databind.deser.merge.NodeMergeTest::testArrayNodeMerge
    public void testArrayNodeMerge() throws Exception
    {
        ArrayNodeWrapper w = MAPPER.readValue(aposToQuotes("{'list':[456,true,{},  [], 'foo']}"),
                ArrayNodeWrapper.class);
        assertEquals(6, w.list.size());
        assertEquals(123, w.list.get(0).asInt());
        assertEquals(456, w.list.get(1).asInt());
        assertTrue(w.list.get(2).asBoolean());
        JsonNode n = w.list.get(3);
        assertTrue(n.isObject());
        assertEquals(0, n.size());
        n = w.list.get(4);
        assertTrue(n.isArray());
        assertEquals(0, n.size());
        assertEquals("foo", w.list.get(5).asText());
    }

// com.fasterxml.jackson.databind.deser.merge.PropertyMergeTest::testBeanMergingViaProp
    public void testBeanMergingViaProp() throws Exception
    {
        Config config = MAPPER.readValue(aposToQuotes("{'loc':{'b':3}}"), Config.class);
        assertEquals(1, config.loc.a);
        assertEquals(3, config.loc.b);

        config = MAPPER.readerForUpdating(new Config(5, 7))
                .readValue(aposToQuotes("{'loc':{'b':2}}"));
        assertEquals(5, config.loc.a);
        assertEquals(2, config.loc.b);
    }

// com.fasterxml.jackson.databind.deser.merge.PropertyMergeTest::testBeanMergingViaType
    public void testBeanMergingViaType() throws Exception
    {
        
        NonMergeConfig config = MAPPER.readValue(aposToQuotes("{'loc':{'a':3}}"), NonMergeConfig.class);
        assertEquals(3, config.loc.a);
        assertEquals(0, config.loc.b); 

        
        ObjectMapper mapper = newObjectMapper();
        mapper.configOverride(AB.class).setMergeable(true);
        config = mapper.readValue(aposToQuotes("{'loc':{'a':3}}"), NonMergeConfig.class);
        assertEquals(3, config.loc.a);
        assertEquals(2, config.loc.b); 
    }

// com.fasterxml.jackson.databind.deser.merge.PropertyMergeTest::testBeanMergingViaGlobal
    public void testBeanMergingViaGlobal() throws Exception
    {
        
        ObjectMapper mapper = newObjectMapper()
                .setDefaultMergeable(true);
        NonMergeConfig config = mapper.readValue(aposToQuotes("{'loc':{'a':3}}"), NonMergeConfig.class);
        assertEquals(3, config.loc.a);
        assertEquals(2, config.loc.b); 

        
        FiveMinuteUser user0 = new FiveMinuteUser("Bob", "Bush", true, FiveMinuteUser.Gender.MALE,
                new byte[] { 1, 2, 3, 4, 5 });
        FiveMinuteUser user = mapper.readerFor(FiveMinuteUser.class)
                .withValueToUpdate(user0)
                .readValue(aposToQuotes("{'name':{'last':'Brown'}}"));
        assertEquals("Bob", user.getName().getFirst());
        assertEquals("Brown", user.getName().getLast());
    }

// com.fasterxml.jackson.databind.deser.merge.PropertyMergeTest::testBeanMergingWithoutSetter
    public void testBeanMergingWithoutSetter() throws Exception
    {
        NoSetterConfig config = MAPPER.readValue(aposToQuotes("{'value':{'b':99}}"),
                NoSetterConfig.class);
        assertEquals(99, config._value.b);
        assertEquals(1, config._value.a);
    }

// com.fasterxml.jackson.databind.deser.merge.PropertyMergeTest::testBeanAsArrayMerging
    public void testBeanAsArrayMerging() throws Exception
    {
        ABAsArray input = new ABAsArray();
        input.a = 4;
        input.b = 6;

        assertSame(input, MAPPER.readerForUpdating(input)
                .readValue("[1, 3]"));
        assertEquals(1, input.a);
        assertEquals(3, input.b);

        
        assertSame(input, MAPPER.readerForUpdating(input)
                .readValue("[9]"));
        assertEquals(9, input.a);
        assertEquals(3, input.b);

        
        try {
            MAPPER.readerForUpdating(input)
                .readValue("[9, 8, 14]");
            fail("Should not pass");
        } catch (MismatchedInputException e) {
            verifyException(e, "expected at most 2 properties");
        }

        try {
            MAPPER.readerForUpdating(input)
                .readValue("\"blob\"");
            fail("Should not pass");
        } catch (MismatchedInputException e) {
            verifyException(e, "Can not deserialize");
            verifyException(e, "from non-Array representation");
        }
    }

// com.fasterxml.jackson.databind.deser.merge.PropertyMergeTest::testReferenceMerging
    public void testReferenceMerging() throws Exception
    {
        MergedReference result = MAPPER.readValue(aposToQuotes("{'value':'override'}"),
                MergedReference.class);
        assertEquals("override", result.value.get());
    }

// com.fasterxml.jackson.databind.deser.merge.PropertyMergeTest::testInvalidPropertyMerge
    public void testInvalidPropertyMerge() throws Exception
    {
        ObjectMapper mapper = newObjectMapper()
                .disable(MapperFeature.IGNORE_MERGE_FOR_UNMERGEABLE);
        
        try {
            mapper.readValue("{\"value\":3}", CantMergeInts.class);
            fail("Should not pass");
        } catch (InvalidDefinitionException e) {
            verifyException(e, "can not be merged");
        }
    }

// com.fasterxml.jackson.databind.deser.merge.UpdateValueTest::testValueUpdateWithCreator
    public void testValueUpdateWithCreator() throws Exception
    {
        Bean bean = new Bean("abc", "def");
        MAPPER.readerFor(Bean.class).withValueToUpdate(bean).readValue("{\"a\":\"ghi\",\"b\":\"jkl\"}");
        assertEquals("ghi", bean.getA());
        assertEquals("jkl", bean.getB());
    }

// com.fasterxml.jackson.databind.deser.merge.UpdateValueTest::testValueUpdateOther
    public void testValueUpdateOther() throws Exception
    {
        Bean bean = new Bean("abc", "def");
        ObjectReader r = MAPPER.readerFor(Bean.class).withValueToUpdate(bean);
        
        r = r.withValueToUpdate(null);
        
        Bean result = r.readValue(aposToQuotes("{'a':'x'}"));
        assertNotNull(result);
    }

// com.fasterxml.jackson.databind.exc.BasicExceptionTest::testBadDefinition
    public void testBadDefinition() throws Exception
    {
        JavaType t = TypeFactory.defaultInstance().constructType(String.class);
        JsonParser p = JSON_F.createParser("[]");
        InvalidDefinitionException e = new InvalidDefinitionException(p,
               "Testing", t);
        assertEquals("Testing", e.getOriginalMessage());
        assertEquals(String.class, e.getType().getRawClass());
        assertNull(e.getBeanDescription());
        assertNull(e.getProperty());
        assertSame(p, e.getProcessor());
        p.close();

        
        BeanDescription beanDef = MAPPER.getSerializationConfig().introspectClassAnnotations(getClass());
        e = InvalidDefinitionException.from(p, "Testing",
                beanDef, (BeanPropertyDefinition) null);
        assertEquals(beanDef.getType(), e.getType());
        assertNotNull(e);
        
        
        JsonGenerator g = JSON_F.createGenerator(new StringWriter());
        e = new InvalidDefinitionException(p,
                "Testing", t);
        assertEquals("Testing", e.getOriginalMessage());
        assertEquals(String.class, e.getType().getRawClass());

        
        e = InvalidDefinitionException.from(g, "Testing",
                beanDef, (BeanPropertyDefinition) null);
        assertEquals(beanDef.getType(), e.getType());
        assertNotNull(e);
        
        g.close();
    }

// com.fasterxml.jackson.databind.exc.BasicExceptionTest::testInvalidFormat
    public void testInvalidFormat() throws Exception
    {
        
        InvalidFormatException e = new InvalidFormatException("Testing", Boolean.TRUE,
                String.class);
        assertSame(Boolean.TRUE, e.getValue());
        assertNull(e.getProcessor());
        assertNotNull(e);

        e = new InvalidFormatException("Testing", JsonLocation.NA,
                Boolean.TRUE, String.class);
        assertSame(Boolean.TRUE, e.getValue());
        assertNull(e.getProcessor());
        assertNotNull(e);
    }

// com.fasterxml.jackson.databind.exc.BasicExceptionTest::testIgnoredProperty
    public void testIgnoredProperty() throws Exception
    {
        
        JsonParser p = JSON_F.createParser("{ }");
        IgnoredPropertyException e = IgnoredPropertyException.from(p,
                this, 
                "testProp", Collections.<Object>singletonList("x"));
        assertNotNull(e);

        e = IgnoredPropertyException.from(p,
                getClass(),
                "testProp", null);
        assertNotNull(e);
        assertNull(e.getKnownPropertyIds());
        p.close();

        
        try {
            IgnoredPropertyException.from(p, null,
                    "testProp", Collections.<Object>singletonList("x"));
            fail("Should not pass");
        } catch (NullPointerException e2) {
        }
    }

// com.fasterxml.jackson.databind.exc.BasicExceptionTest::testUnrecognizedProperty
    public void testUnrecognizedProperty() throws Exception
    {
        JsonParser p = JSON_F.createParser("{ }");
        UnrecognizedPropertyException e = UnrecognizedPropertyException.from(p, this,
                "testProp", Collections.<Object>singletonList("y"));
        assertNotNull(e);
        assertEquals(getClass(), e.getReferringClass());
        Collection<Object> ids = e.getKnownPropertyIds();
        assertNotNull(ids);
        assertEquals(1, ids.size());
        assertTrue(ids.contains("y"));

        e = UnrecognizedPropertyException.from(p, getClass(),
                "testProp", Collections.<Object>singletonList("y"));

        assertEquals(getClass(), e.getReferringClass());
        p.close();
    }

// com.fasterxml.jackson.databind.exc.DeserExceptionTypeTest::testHandlingOfUnrecognized
    public void testHandlingOfUnrecognized() throws Exception
    {
        UnrecognizedPropertyException exc = null;
        try {
            MAPPER.readValue("{\"bar\":3}", Bean.class);
        } catch (UnrecognizedPropertyException e) {
            exc = e;
        }
        if (exc == null) {
            fail("Should have failed binding");
        }
        assertEquals("bar", exc.getPropertyName());
        assertEquals(Bean.class, exc.getReferringClass());
        
        verifyException(exc, "propX");
    }

// com.fasterxml.jackson.databind.exc.DeserExceptionTypeTest::testExceptionWithEmpty
    public void testExceptionWithEmpty() throws Exception
    {
        try {
            Object result = MAPPER.readValue("    ", Object.class);
            fail("Expected an exception, but got result value: "+result);
        } catch (Exception e) {
            verifyException(e, MismatchedInputException.class, "No content");
        }
    }

// com.fasterxml.jackson.databind.exc.DeserExceptionTypeTest::testExceptionWithIncomplete
    public void testExceptionWithIncomplete()
        throws Exception
    {
        BrokenStringReader r = new BrokenStringReader("[ 1, ", "TEST");
        JsonParser p = MAPPER.getFactory().createParser(r);
        try {
            @SuppressWarnings("unused")
            Object ob = MAPPER.readValue(p, Object.class);
            fail("Should have gotten an exception");
        } catch (IOException e) {
            
            verifyException(e, IOException.class, "TEST");
        }
    }

// com.fasterxml.jackson.databind.exc.DeserExceptionTypeTest::testExceptionWithEOF
    public void testExceptionWithEOF() throws Exception
    {
        JsonParser p = MAPPER.getFactory().createParser("  3");

        Integer I = MAPPER.readValue(p, Integer.class);
        assertEquals(3, I.intValue());

        
        try {
            I = MAPPER.readValue(p, Integer.class);
            fail("Should have gotten an exception");
        } catch (IOException e) {
            verifyException(e, MismatchedInputException.class, "No content");
        }
        
        JsonToken t = p.getCurrentToken();
        if (t != null) {
            fail("Expected current token to be null after end-of-stream, was: "+t);
        }
        p.close();
    }

// com.fasterxml.jackson.databind.exc.DeserExceptionTypeTest::testExceptionForNoCreators
    public void testExceptionForNoCreators() throws Exception
    {
        try {
            NoCreatorsBean b = MAPPER.readValue("{}", NoCreatorsBean.class);
            fail("Should not succeed, got: "+b);
        } catch (JsonMappingException e) {
            verifyException(e, InvalidDefinitionException.class, "no Creators");
        }
    }

// com.fasterxml.jackson.databind.exc.ExceptionPathTest::testReferenceChainForInnerClass
    public void testReferenceChainForInnerClass() throws Exception
    {
        String json = MAPPER.writeValueAsString(new Outer());
        try {
            MAPPER.readValue(json, Outer.class);
            fail("Should not pass");
        } catch (JsonMappingException e) {
            JsonMappingException.Reference reference = e.getPath().get(0);
            assertEquals(getClass().getName()+"$Outer[\"inner\"]",
                    reference.toString());
        }
    }

// com.fasterxml.jackson.databind.exc.ExceptionSerializationTest::testSimple
    public void testSimple() throws Exception
    {
        String TEST = "test exception";
        Map<String,Object> result = writeAndMap(MAPPER, new Exception(TEST));
        
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

// com.fasterxml.jackson.databind.exc.ExceptionSerializationTest::testSimpleOther
    public void testSimpleOther() throws Exception
    {
        JsonParser p = MAPPER.getFactory().createParser("{ }");
        InvalidFormatException exc = InvalidFormatException.from(p, "Test", getClass(), String.class);
        String json = MAPPER.writeValueAsString(exc);
        p.close();
        assertNotNull(json);
    }

// com.fasterxml.jackson.databind.exc.ExceptionSerializationTest::testIgnorals
    public void testIgnorals() throws Exception
    {
        ExceptionWithIgnoral input = new ExceptionWithIgnoral("foobar");
        input.initCause(new IOException("surprise!"));

        
        String json = MAPPER
                .writerWithDefaultPrettyPrinter()
                .writeValueAsString(input);

        Map<String,Object> result = MAPPER.readValue(json, Map.class);
        assertEquals("foobar", result.get("message"));

        assertNull(result.get("bogus1"));
        assertNotNull(result.get("bogus2"));

        
        ObjectMapper mapper = new ObjectMapper();
        mapper.configOverride(ExceptionWithIgnoral.class)
            .setIgnorals(JsonIgnoreProperties.Value.forIgnoredProperties("bogus2"));
        String json2 = mapper
                .writeValueAsString(new ExceptionWithIgnoral("foobar"));

        Map<String,Object> result2 = mapper.readValue(json2, Map.class);
        assertNull(result2.get("bogus1"));
        assertNull(result2.get("bogus2"));

        
        ExceptionWithIgnoral output = mapper.readValue(json2, ExceptionWithIgnoral.class);
        assertNotNull(output);
        assertEquals("foobar", output.getMessage());
    }

// com.fasterxml.jackson.databind.exc.ExceptionSerializationTest::testJsonMappingExceptionSerialization
    public void testJsonMappingExceptionSerialization() throws IOException {
        Exception e = null;
        
        try {
            MAPPER.readValue( "{ \"val\": \"foo\" }", NoSerdeConstructor.class );
            fail("Should not pass");
        } catch (JsonMappingException e0) {
            verifyException(e0, "can not deserialize from Object");
            e = e0;
        }
        
        String json = MAPPER.writeValueAsString(e);
        JsonNode root = MAPPER.readTree(json);
        String msg = root.path("message").asText();
        String MATCH = "can not construct instance";
        if (!msg.toLowerCase().contains(MATCH)) {
            fail("Exception should contain '"+MATCH+"', does not: '"+msg+"'");
        }
    }

// com.fasterxml.jackson.databind.exc.TestExceptionDeserialization::testIOException
    public void testIOException() throws IOException
    {
        IOException ioe = new IOException("TEST");
        String json = MAPPER.writeValueAsString(ioe);
        IOException result = MAPPER.readValue(json, IOException.class);
        assertEquals(ioe.getMessage(), result.getMessage());
    }

// com.fasterxml.jackson.databind.exc.TestExceptionDeserialization::testWithCreator
    public void testWithCreator() throws IOException
    {
        final String MSG = "the message";
        String json = MAPPER.writeValueAsString(new MyException(MSG, 3));

        MyException result = MAPPER.readValue(json, MyException.class);
        assertEquals(MSG, result.getMessage());
        assertEquals(3, result.value);
        assertEquals(1, result.stuff.size());
        assertEquals(result.getFoo(), result.stuff.get("foo"));
    }

// com.fasterxml.jackson.databind.exc.TestExceptionDeserialization::testWithNullMessage
    public void testWithNullMessage() throws IOException
    {
        final ObjectMapper mapper = new ObjectMapper();
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        String json = mapper.writeValueAsString(new IOException((String) null));
        IOException result = mapper.readValue(json, IOException.class);
        assertNotNull(result);
        assertNull(result.getMessage());
    }

// com.fasterxml.jackson.databind.exc.TestExceptionDeserialization::testNoArgsException
    public void testNoArgsException() throws IOException
    {
        MyNoArgException exc = MAPPER.readValue("{}", MyNoArgException.class);
        assertNotNull(exc);
    }

// com.fasterxml.jackson.databind.exc.TestExceptionDeserialization::testJDK7SuppressionProperty
    public void testJDK7SuppressionProperty() throws IOException
    {
        Exception exc = MAPPER.readValue("{\"suppressed\":[]}", IOException.class);
        assertNotNull(exc);
    }

// com.fasterxml.jackson.databind.exc.TestExceptionDeserialization::testSingleValueArrayDeserialization
    public void testSingleValueArrayDeserialization() {}

// com.fasterxml.jackson.databind.exc.TestExceptionDeserialization::testSingleValueArrayDeserializationException
    public void testSingleValueArrayDeserializationException() throws Exception {
        final ObjectMapper mapper = new ObjectMapper();
        mapper.disable(DeserializationFeature.UNWRAP_SINGLE_VALUE_ARRAYS);
        
        final IOException exp;
        try {
            throw new IOException("testing");
        } catch (IOException internal) {
            exp = internal;
        }
        final String value = "[" + mapper.writeValueAsString(exp) + "]";
        
        try {
            mapper.readValue(value, IOException.class);
            fail("Exception not thrown when attempting to deserialize an IOException wrapped in a single value array with UNWRAP_SINGLE_VALUE_ARRAYS disabled");
        } catch (JsonMappingException exp2) {
            
        }
    }

// com.fasterxml.jackson.databind.exc.TestExceptionHandlingWithDefaultDeserialization::testShouldThrowJsonMappingExceptionWithPathReference
    public void testShouldThrowJsonMappingExceptionWithPathReference() throws IOException {
        
        ObjectMapper mapper = new ObjectMapper();
        String input = "{\"bar\":{\"baz\":{qux:\"quxValue\"))}";
        final String THIS = getClass().getName();

        
        try {
            mapper.readValue(input, Foo.class);
            fail("Upsss! Exception has not been thrown.");
        } catch (JsonMappingException ex) {
            
            assertEquals(THIS+"$Foo[\"bar\"]->"+THIS+"$Bar[\"baz\"]",
                    ex.getPathReference());
        }
    }

// com.fasterxml.jackson.databind.exc.TestExceptionHandlingWithJsonCreatorDeserialization::testShouldThrowJsonMappingExceptionWithPathReference
    public void testShouldThrowJsonMappingExceptionWithPathReference() throws IOException {
        
        ObjectMapper mapper = new ObjectMapper();
        String input = "{\"bar\":{\"baz\":{qux:\"quxValue\"))}";
        final String THIS = getClass().getName();

        
        try {
            mapper.readValue(input, Foo.class);
            fail("Upsss! Exception has not been thrown.");
        } catch (JsonMappingException ex) {
            
            assertEquals(THIS+"$Foo[\"bar\"]->"+THIS+"$Bar[\"baz\"]",
                    ex.getPathReference());
        }
    }

// com.fasterxml.jackson.databind.exc.TestExceptionsDuringWriting::testCatchAndRethrow
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

// com.fasterxml.jackson.databind.exc.TestExceptionsDuringWriting::testExceptionWithSimpleMapper
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

// com.fasterxml.jackson.databind.exc.TestExceptionsDuringWriting::testExceptionWithMapperAndGenerator
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

// com.fasterxml.jackson.databind.exc.TestExceptionsDuringWriting::testExceptionWithGeneratorMapping
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

// com.fasterxml.jackson.databind.ext.TestCoreXMLTypes::testQNameSer
    public void testQNameSer() throws Exception
    {
        QName qn = new QName("http://abc", "tag", "prefix");
        assertEquals(quote(qn.toString()), serializeAsString(qn));
    }

// com.fasterxml.jackson.databind.ext.TestCoreXMLTypes::testDurationSer
    public void testDurationSer() throws Exception
    {
        DatatypeFactory dtf = DatatypeFactory.newInstance();
        
        Duration dur = dtf.newDurationDayTime(false, 15, 19, 58, 1);
        assertEquals(quote(dur.toString()), serializeAsString(dur));
    }

// com.fasterxml.jackson.databind.ext.TestCoreXMLTypes::testXMLGregorianCalendarSerAndDeser
    public void testXMLGregorianCalendarSerAndDeser() throws Exception
    {
        DatatypeFactory dtf = DatatypeFactory.newInstance();
        XMLGregorianCalendar cal = dtf.newXMLGregorianCalendar
            (1974, 10, 10, 18, 15, 17, 123, 0);
        
        ObjectMapper mapper = new ObjectMapper();
        long timestamp = cal.toGregorianCalendar().getTimeInMillis();
        String numStr = String.valueOf(timestamp);
        assertEquals(numStr, mapper.writeValueAsString(cal));

        
        XMLGregorianCalendar calOut = mapper.readValue(numStr, XMLGregorianCalendar.class);
        assertNotNull(calOut);
        assertEquals(timestamp, calOut.toGregorianCalendar().getTimeInMillis());

        
        mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        
        String exp = cal.toXMLFormat();
        String act = mapper.writeValueAsString(cal);
        act = act.substring(1, act.length() - 1); 
        exp = removeZ(exp);
        act = removeZ(act);
        assertEquals(exp, act);
    }

// com.fasterxml.jackson.databind.ext.TestCoreXMLTypes::testDeserializerLoading
    public void testDeserializerLoading()
    {
        CoreXMLDeserializers sers = new CoreXMLDeserializers();
        TypeFactory f = TypeFactory.defaultInstance();
        sers.findBeanDeserializer(f.constructType(Duration.class), null, null);
        sers.findBeanDeserializer(f.constructType(XMLGregorianCalendar.class), null, null);
        sers.findBeanDeserializer(f.constructType(QName.class), null, null);
    }

// com.fasterxml.jackson.databind.ext.TestCoreXMLTypes::testQNameDeser
    public void testQNameDeser() throws Exception
    {
        QName qn = new QName("http://abc", "tag", "prefix");
        String qstr = qn.toString();
        ObjectMapper mapper = new ObjectMapper();
        assertEquals("Should deserialize to equal QName (exp serialization: '"+qstr+"')",
                     qn, mapper.readValue(quote(qstr), QName.class));
    }

// com.fasterxml.jackson.databind.ext.TestCoreXMLTypes::testCalendarDeser
    public void testCalendarDeser() throws Exception
    {
        DatatypeFactory dtf = DatatypeFactory.newInstance();
        XMLGregorianCalendar cal = dtf.newXMLGregorianCalendar
            (1974, 10, 10, 18, 15, 17, 123, 0);
        String exp = cal.toXMLFormat();
        assertEquals("Should deserialize to equal XMLGregorianCalendar ('"+exp+"')", cal,
                new ObjectMapper().readValue(quote(exp), XMLGregorianCalendar.class));
    }

// com.fasterxml.jackson.databind.ext.TestCoreXMLTypes::testDurationDeser
    public void testDurationDeser() throws Exception
    {
        DatatypeFactory dtf = DatatypeFactory.newInstance();
        
        Duration dur = dtf.newDurationDayTime(true, 27, 5, 15, 59);
        String exp = dur.toString();
        assertEquals("Should deserialize to equal Duration ('"+exp+"')", dur,
                new ObjectMapper().readValue(quote(exp), Duration.class));
    }

// com.fasterxml.jackson.databind.ext.TestDOM::testSerializeSimpleNonNS
    public void testSerializeSimpleNonNS() throws Exception
    {
        
        Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse
            (new InputSource(new StringReader(SIMPLE_XML)));
        assertNotNull(doc);
        
        String outputRaw = MAPPER.writeValueAsString(doc);
        
        String output = MAPPER.readValue(outputRaw, String.class);
        
        assertEquals(SIMPLE_XML, normalizeOutput(output));
    }

// com.fasterxml.jackson.databind.ext.TestDOM::testDeserializeNonNS
    public void testDeserializeNonNS() throws Exception
    {
        for (int i = 0; i < 2; ++i) {
            Document doc;

            if (i == 0) {
                
                doc = MAPPER.readValue(quote(SIMPLE_XML), Document.class);
            } else {
                
                Node node = MAPPER.readValue(quote(SIMPLE_XML), Node.class);
                doc = (Document) node;
            }
            Element root = doc.getDocumentElement();
            assertNotNull(root);
            
            assertEquals("root", root.getTagName());
            assertEquals("3", root.getAttribute("attr"));
            assertEquals(1, root.getAttributes().getLength());
            NodeList nodes = root.getChildNodes();
            assertEquals(2, nodes.getLength());
            Element leaf = (Element) nodes.item(0);
            assertEquals("leaf", leaf.getTagName());
            assertEquals(0, leaf.getAttributes().getLength());
            
            ProcessingInstruction pi = (ProcessingInstruction) nodes.item(1);
            assertEquals("proc", pi.getTarget());
            assertEquals("instr", pi.getData());
        }
    }

// com.fasterxml.jackson.databind.ext.TestDOM::testDeserializeNS
    public void testDeserializeNS() throws Exception
    {
        Document doc = MAPPER.readValue(quote(SIMPLE_XML_NS), Document.class);
        Element root = doc.getDocumentElement();
        assertNotNull(root);
        assertEquals("root", root.getTagName());
        
        String uri = root.getNamespaceURI();
        assertTrue((uri == null) || "".equals(uri));
        
        assertEquals(0, root.getChildNodes().getLength());
        
        assertEquals(2, root.getAttributes().getLength());
        assertEquals("abc", root.getAttributeNS("http://foo", "attr"));
    }

// com.fasterxml.jackson.databind.ext.TestJava6Types::test16Types
    public void test16Types() throws Exception
    {
        final ObjectMapper mapper = new ObjectMapper();
        Deque<?> dq = mapper.readValue("[1]", Deque.class);
        assertNotNull(dq);
        assertEquals(1, dq.size());
        assertTrue(dq instanceof Deque<?>);

        NavigableSet<?> ns = mapper.readValue("[ true ]", NavigableSet.class);
        assertEquals(1, ns.size());
        assertTrue(ns instanceof NavigableSet<?>);
    }

// com.fasterxml.jackson.databind.format.BooleanFormatTest::testShapeViaDefaults
    public void testShapeViaDefaults() throws Exception
    {
        assertEquals(aposToQuotes("{'b':true}"),
                MAPPER.writeValueAsString(new BooleanWrapper(true)));
        ObjectMapper m = newObjectMapper();
        m.configOverride(Boolean.class)
            .setFormat(JsonFormat.Value.forShape(JsonFormat.Shape.NUMBER));
        assertEquals(aposToQuotes("{'b':1}"),
                m.writeValueAsString(new BooleanWrapper(true)));
    }

// com.fasterxml.jackson.databind.format.BooleanFormatTest::testShapeOnProperty
    public void testShapeOnProperty() throws Exception
    {
        assertEquals(aposToQuotes("{'b1':1,'b2':0,'b3':true}"),
                MAPPER.writeValueAsString(new BeanWithBoolean(true, false, true)));
    }

// com.fasterxml.jackson.databind.format.ColletionFormatShapeTest::testListAsObjectRoundtrip
    public void testListAsObjectRoundtrip() throws Exception
    {
        
        CollectionAsPOJO list = new CollectionAsPOJO();
        list.add("a");
        list.add("b");
        String json = MAPPER.writeValueAsString(list);
        assertEquals("{\"size\":2,\"values\":[\"a\",\"b\"]}", json);

        
        CollectionAsPOJO result = MAPPER.readValue(json, CollectionAsPOJO.class);
        assertEquals(2, result.size());
    }

// com.fasterxml.jackson.databind.format.DateFormatTest::testTypeDefaults
    public void testTypeDefaults() throws Exception
    {
        ObjectMapper mapper = newObjectMapper();
        mapper.configOverride(Date.class)
            .setFormat(JsonFormat.Value.forPattern("yyyy.dd.MM"));
        
        String json = mapper.writeValueAsString(new DateWrapper(0L));
        assertEquals(aposToQuotes("{'value':'1970.01.01'}"), json);

        
        DateWrapper w = mapper.readValue(aposToQuotes("{'value':'1981.13.3'}"), DateWrapper.class);
        assertNotNull(w);
        
        Calendar c = Calendar.getInstance();
        c.setTime(w.value);
        assertEquals(1981, c.get(Calendar.YEAR));
        assertEquals(Calendar.MARCH, c.get(Calendar.MONTH));
    }

// com.fasterxml.jackson.databind.format.EnumFormatShapeTest::testEnumAsObjectValid
    public void testEnumAsObjectValid() throws Exception {
        assertEquals("{\"value\":\"a1\"}", MAPPER.writeValueAsString(PoNUM.A));
    }

// com.fasterxml.jackson.databind.format.EnumFormatShapeTest::testEnumAsIndexViaAnnotations
    public void testEnumAsIndexViaAnnotations() throws Exception {
        assertEquals("{\"text\":0}", MAPPER.writeValueAsString(new PoNUMContainer()));
    }

// com.fasterxml.jackson.databind.format.EnumFormatShapeTest::testEnumAsObjectBroken
    public void testEnumAsObjectBroken() throws Exception
    {
        assertEquals("0", MAPPER.writeValueAsString(PoAsArray.A));
    }

// com.fasterxml.jackson.databind.format.EnumFormatShapeTest::testOverrideEnumAsString
    public void testOverrideEnumAsString() throws Exception {
        assertEquals("{\"value\":\"B\"}", MAPPER.writeValueAsString(new PoOverrideAsString()));
    }

// com.fasterxml.jackson.databind.format.EnumFormatShapeTest::testOverrideEnumAsNumber
    public void testOverrideEnumAsNumber() throws Exception {
        assertEquals("{\"value\":1}", MAPPER.writeValueAsString(new PoOverrideAsNumber()));
    }

// com.fasterxml.jackson.databind.format.EnumFormatShapeTest::testEnumValueAsNumber
    public void testEnumValueAsNumber() throws Exception {
        assertEquals(String.valueOf(Color.GREEN.ordinal()),
                MAPPER.writeValueAsString(Color.GREEN));
    }

// com.fasterxml.jackson.databind.format.EnumFormatShapeTest::testEnumPropertyAsNumber
    public void testEnumPropertyAsNumber() throws Exception {
        assertEquals(String.format(aposToQuotes("{'color':%s}"), Color.GREEN.ordinal()),
                MAPPER.writeValueAsString(new ColorWrapper(Color.GREEN)));
    }

// com.fasterxml.jackson.databind.format.MapEntryFormatTest::testAsNaturalRoundtrip
    public void testAsNaturalRoundtrip() throws Exception
    {
        BeanWithMapEntry input = new BeanWithMapEntry("foo" ,"bar");
        String json = MAPPER.writeValueAsString(input);
        assertEquals(aposToQuotes("{'entry':{'foo':'bar'}}"), json);
        BeanWithMapEntry result = MAPPER.readValue(json, BeanWithMapEntry.class);
        assertEquals("foo", result.entry.getKey());
        assertEquals("bar", result.entry.getValue());
    }

// com.fasterxml.jackson.databind.format.MapEntryFormatTest::testAsObjectRoundtrip
    public void testAsObjectRoundtrip() throws Exception
    {
        MapEntryAsObject input = new MapEntryAsObject("foo" ,"bar");
        String json = MAPPER.writeValueAsString(input);
        assertEquals(aposToQuotes("{'key':'foo','value':'bar'}"), json);

        
        
        
        MapEntryAsObject result = MAPPER.readValue(json, MapEntryAsObject.class);
        assertEquals("foo", result.getKey());
        assertEquals("bar", result.getValue());
    }

// com.fasterxml.jackson.databind.format.MapEntryFormatTest::testInclusion
    public void testInclusion() throws Exception
    {
        assertEquals(aposToQuotes("{'entry':{'a':'b'}}"),
                MAPPER.writeValueAsString(new EmptyEntryWrapper("a", "b")));
        assertEquals(aposToQuotes("{'entry':{'a':'b'}}"),
                MAPPER.writeValueAsString(new EntryWithDefaultWrapper("a", "b")));
        assertEquals(aposToQuotes("{'entry':{'a':'b'}}"),
                MAPPER.writeValueAsString(new EntryWithNullWrapper("a", "b")));

        assertEquals(aposToQuotes("{}"),
                MAPPER.writeValueAsString(new EmptyEntryWrapper("a", "")));
        assertEquals(aposToQuotes("{}"),
                MAPPER.writeValueAsString(new EntryWithDefaultWrapper("a", "")));
        assertEquals(aposToQuotes("{'entry':{'a':''}}"),
                MAPPER.writeValueAsString(new EntryWithNullWrapper("a", "")));
        assertEquals(aposToQuotes("{}"),
                MAPPER.writeValueAsString(new EntryWithNullWrapper("a", null)));
    }

// com.fasterxml.jackson.databind.format.MapEntryFormatTest::testInclusionWithReference
    public void testInclusionWithReference() throws Exception
    {
        assertEquals(aposToQuotes("{'entry':{'a':'b'}}"),
                MAPPER.writeValueAsString(new EntryWithNonAbsentWrapper("a", "b")));
        
        
        assertEquals(aposToQuotes("{'entry':{'a':''}}"),
                MAPPER.writeValueAsString(new EntryWithNonAbsentWrapper("a", "")));
        assertEquals(aposToQuotes("{}"),
                MAPPER.writeValueAsString(new EntryWithNonAbsentWrapper("a", null)));
    }

// com.fasterxml.jackson.databind.format.MapFormatShapeTest::testSerializeAsPOJOViaClass
    public void testSerializeAsPOJOViaClass() throws Exception
    {
        String result = MAPPER.writeValueAsString(new Bean476Container(1,2,0));
        assertEquals(aposToQuotes("{'a':{'extra':13,'empty':false},'b':{'value':2}}"),
                result);
    }

// com.fasterxml.jackson.databind.format.MapFormatShapeTest::testRoundTrip
    public void testRoundTrip() throws Exception
    {
        Map1540Implementation input = new Map1540Implementation();
        input.property = 55;
        input.put(12, 45);
        input.put(6, 88);

        String json = MAPPER.writeValueAsString(input);

        assertEquals(aposToQuotes("{'property':55,'map':{'6':88,'12':45}}"), json);

        Map1540Implementation result = MAPPER.readValue(json, Map1540Implementation.class);
        assertEquals(result.property, input.property);
        assertEquals(input.getMap(), input.getMap());
   }

// com.fasterxml.jackson.databind.format.MapFormatShapeTest::testDeserializeAsPOJOViaClass
    public void testDeserializeAsPOJOViaClass() throws Exception
    {
        Map476AsPOJO result = MAPPER.readValue(aposToQuotes("{'extra':42}"),
                Map476AsPOJO.class);
        assertEquals(0, result.size());
        assertEquals(42, result.extra);
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

// com.fasterxml.jackson.databind.interop.IllegalTypesCheckTest::testIssue1599
    public void testIssue1599() throws Exception
    {
        final String NASTY_CLASS = "com.sun.org.apache.xalan.internal.xsltc.trax.TemplatesImpl";
        final String JSON = aposToQuotes(
 "{'id': 124,\n"
+" 'obj':[ '"+NASTY_CLASS+"',\n"
+"  {\n"
+"    'transletBytecodes' : [ 'AAIAZQ==' ],\n"
+"    'transletName' : 'a.b',\n"
+"    'outputProperties' : { }\n"
+"  }\n"
+" ]\n"
+"}"
        );
        ObjectMapper mapper = new ObjectMapper();
        mapper.enableDefaultTyping();
        try {
            mapper.readValue(JSON, Bean1599.class);
            fail("Should not pass");
        } catch (InvalidDefinitionException e) {
            verifyException(e, "Illegal type");
            verifyException(e, "to deserialize");
            verifyException(e, "prevented for security reasons");
            BeanDescription desc = e.getBeanDescription();
            assertNotNull(desc);
            assertEquals(NASTY_CLASS, desc.getBeanClass().getName());
        }
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

// com.fasterxml.jackson.databind.interop.TestFormatDetection::testSequenceWithJSON
    public void testSequenceWithJSON() throws Exception
    {
        ObjectReader detecting = READER.forType(POJO.class);
        detecting = detecting.withFormatDetection(detecting);
        MappingIterator<POJO> it = detecting.
                readValues(utf8Bytes(aposToQuotes("{'x':1}\n{'x':2,'y':5}")));

        assertTrue(it.hasNextValue());
        POJO pojo = it.nextValue();
        assertEquals(1, pojo.x);

        assertTrue(it.hasNextValue());
        pojo = it.nextValue();
        assertEquals(2, pojo.x);
        assertEquals(5, pojo.y);
        
        assertFalse(it.hasNextValue());
        it.close();

        
        ObjectReader r2 = READER.forType(JsonNode.class);
        r2 = r2.withFormatDetection(r2);
        MappingIterator<JsonNode> nodes = r2.
                readValues(utf8Bytes(aposToQuotes("{'x':1}\n{'x':2,'y':5}")));

        assertTrue(nodes.hasNextValue());
        JsonNode n = nodes.nextValue();
        assertEquals(1, n.size());

        assertTrue(nodes.hasNextValue());
        n = nodes.nextValue();
        assertEquals(2, n.size());
        assertEquals(2, n.path("x").asInt());
        assertEquals(5, n.path("y").asInt());

        assertFalse(nodes.hasNextValue());
        nodes.close();
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

// com.fasterxml.jackson.databind.introspect.TestAnnotationBundles::testKeepAnnotationBundle
    public void testKeepAnnotationBundle() throws Exception
    {
        MAPPER.setAnnotationIntrospector(new BundleAnnotationIntrospector());
        assertEquals("{\"important\":42}", MAPPER.writeValueAsString(new InformingHolder()));
    }

// com.fasterxml.jackson.databind.introspect.TestAnnotationBundles::testRecursiveBundlesField
    public void testRecursiveBundlesField() throws Exception {
        assertEquals("{\"unimportant\":42}", MAPPER.writeValueAsString(new RecursiveHolder()));
    }

// com.fasterxml.jackson.databind.introspect.TestAnnotationBundles::testRecursiveBundlesMethod
    public void testRecursiveBundlesMethod() throws Exception {
        assertEquals("{\"value\":28}", MAPPER.writeValueAsString(new RecursiveHolder2()));
    }

// com.fasterxml.jackson.databind.introspect.TestAnnotationBundles::testRecursiveBundlesConstructor
    public void testRecursiveBundlesConstructor() throws Exception {
        RecursiveHolder3 result = MAPPER.readValue("17", RecursiveHolder3.class);
        assertNotNull(result);
        assertEquals(17, result.x);
    }

// com.fasterxml.jackson.databind.introspect.TestAnnotationBundles::testBundledIgnore
    public void testBundledIgnore() throws Exception
    {
        assertEquals("{\"foobar\":13}", MAPPER.writeValueAsString(new Bean()));
    }

// com.fasterxml.jackson.databind.introspect.TestAnnotationBundles::testVisibilityBundle
    public void testVisibilityBundle() throws Exception
    {
        assertEquals("{\"b\":5}", MAPPER.writeValueAsString(new NoAutoDetect()));
    }

// com.fasterxml.jackson.databind.introspect.TestAnnotationBundles::testIssue92
    public void testIssue92() throws Exception
    {
        assertEquals("{\"_id\":\"abc\"}", MAPPER.writeValueAsString(new Bean92()));
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
            verifyException(e, "no String-argument constructor/factory");
        }
    }

// com.fasterxml.jackson.databind.introspect.TestAutoDetect::testVisibilityConfigOverridesForSer
    public void testVisibilityConfigOverridesForSer() throws Exception
    {
        
        final Feature1347SerBean input = new Feature1347SerBean();
        assertEquals(aposToQuotes("{'field':2,'value':3}"),
                MAPPER.writeValueAsString(input));

        ObjectMapper mapper = new ObjectMapper();
        mapper.configOverride(Feature1347SerBean.class)
            .setVisibility(JsonAutoDetect.Value.construct(PropertyAccessor.GETTER,
                            Visibility.NONE));
        assertEquals(aposToQuotes("{'field':2}"),
                mapper.writeValueAsString(input));
    }

// com.fasterxml.jackson.databind.introspect.TestAutoDetect::testVisibilityConfigOverridesForDeser
    public void testVisibilityConfigOverridesForDeser() throws Exception
    {
        final String JSON = aposToQuotes("{'value':3}");

        
        try {
            
            MAPPER.readValue(JSON, Feature1347DeserBean.class);
            fail("Should not pass");
        } catch (JsonMappingException e) {
            verifyException(e, "Should NOT get called");
        }

        
        ObjectMapper mapper = new ObjectMapper();
        mapper.configOverride(Feature1347DeserBean.class)
            .setVisibility(JsonAutoDetect.Value.construct(PropertyAccessor.SETTER,
                        Visibility.NONE));
        Feature1347DeserBean result = mapper.readValue(JSON, Feature1347DeserBean.class);
        assertEquals(3, result.value);
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
        AnnotatedClass ac = AnnotatedClassResolver.resolveWithoutSuperTypes(mapper.getSerializationConfig(),
                TypeResolverBean.class);
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

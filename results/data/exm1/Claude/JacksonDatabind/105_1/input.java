// buggy code
    public static JsonDeserializer<?> find(Class<?> rawType, String clsName)
    {
        if (_classNames.contains(clsName)) {
            JsonDeserializer<?> d = FromStringDeserializer.findDeserializer(rawType);
            if (d != null) {
                return d;
            }
            if (rawType == UUID.class) {
                return new UUIDDeserializer();
            }
            if (rawType == StackTraceElement.class) {
                return new StackTraceElementDeserializer();
            }
            if (rawType == AtomicBoolean.class) {
                // (note: AtomicInteger/Long work due to single-arg constructor. For now?
                return new AtomicBooleanDeserializer();
            }
            if (rawType == ByteBuffer.class) {
                return new ByteBufferDeserializer();
            }
        }
        return null;
    }

// relevant test
// com.fasterxml.jackson.databind.deser.creators.InnerClassCreatorTest::testIssue1503
    public void testIssue1503() throws Exception
    {
        String ser = MAPPER.writeValueAsString(new Outer1503());
        Outer1503 result = MAPPER.readValue(ser, Outer1503.class);
        assertNotNull(result);
    }

// com.fasterxml.jackson.databind.deser.creators.MultiArgConstructorTest::testMultiArgVisible
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

// com.fasterxml.jackson.databind.deser.creators.MultiArgConstructorTest::testMultiArgWithPartialOverride
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

// com.fasterxml.jackson.databind.deser.creators.MultiArgConstructorTest::testMultiArgNotVisible
    public void testMultiArgNotVisible() throws Exception
    {
        final ObjectMapper mapper = new ObjectMapper();
        mapper.setAnnotationIntrospector(new MyParamIntrospector());
        mapper.setDefaultVisibility(
                JsonAutoDetect.Value.noOverrides()
                    .withCreatorVisibility(Visibility.NONE));
        try {
             mapper.readValue(aposToQuotes("{'b':13,  'a':-99}"),
                MultiArgCtorBean.class);
            fail("Should not have passed");
        } catch (InvalidDefinitionException e) {
            verifyException(e, "no Creators");
        }
    }

// com.fasterxml.jackson.databind.deser.creators.RequiredCreatorTest::testRequiredAnnotatedParam
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

// com.fasterxml.jackson.databind.deser.creators.RequiredCreatorTest::testRequiredGloballyParam
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

// com.fasterxml.jackson.databind.deser.creators.SingleArgCreatorTest::testNamedSingleArg
    public void testNamedSingleArg() throws Exception
    {
        SingleNamedStringBean bean = MAPPER.readValue(quote("foobar"),
                SingleNamedStringBean.class);
        assertEquals("foobar", bean._ss);
    }

// com.fasterxml.jackson.databind.deser.creators.SingleArgCreatorTest::testSingleStringArgWithImplicitName
    public void testSingleStringArgWithImplicitName() throws Exception
    {
        final ObjectMapper mapper = new ObjectMapper();
        mapper.setAnnotationIntrospector(new MyParamIntrospector("value"));
        StringyBean bean = mapper.readValue(quote("foobar"), StringyBean.class);
        assertEquals("foobar", bean.getValue());
    }

// com.fasterxml.jackson.databind.deser.creators.SingleArgCreatorTest::testSingleImplicitlyNamedNotDelegating
    public void testSingleImplicitlyNamedNotDelegating() throws Exception
    {
        final ObjectMapper mapper = new ObjectMapper();
        mapper.setAnnotationIntrospector(new MyParamIntrospector("value"));
        StringyBeanWithProps bean = mapper.readValue("{\"value\":\"x\"}", StringyBeanWithProps.class);
        assertEquals("x", bean.getValue());
    }

// com.fasterxml.jackson.databind.deser.creators.SingleArgCreatorTest::testSingleExplicitlyNamedButDelegating
    public void testSingleExplicitlyNamedButDelegating() throws Exception
    {
        SingleNamedButStillDelegating bean = MAPPER.readValue(quote("xyz"),
                SingleNamedButStillDelegating.class);
        assertEquals("xyz", bean.value);
    }

// com.fasterxml.jackson.databind.deser.creators.SingleArgCreatorTest::testExplicitFactory660a
    public void testExplicitFactory660a() throws Exception
    {
        
        ExplicitFactoryBeanA bean = MAPPER.readValue(quote("abc"), ExplicitFactoryBeanA.class);
        assertNotNull(bean);
        assertEquals("abc", bean.value());
    }

// com.fasterxml.jackson.databind.deser.creators.SingleArgCreatorTest::testExplicitFactory660b
    public void testExplicitFactory660b() throws Exception
    {
        
        ExplicitFactoryBeanB bean2 = MAPPER.readValue(quote("def"), ExplicitFactoryBeanB.class);
        assertNotNull(bean2);
        assertEquals("def", bean2.value());
    }

// com.fasterxml.jackson.databind.deser.creators.SingleArgCreatorTest::testSingleImplicitDelegating
    public void testSingleImplicitDelegating() throws Exception
    {
        final ObjectMapper mapper = new ObjectMapper();
        mapper.setAnnotationIntrospector(new MyParamIntrospector("value"));
        SingleArgWithImplicit bean = mapper.readValue(aposToQuotes("{'x':1,'y':2}"),
                SingleArgWithImplicit.class);
        XY v = bean.getFoobar();
        assertNotNull(v);
        assertEquals(1, v.x);
        assertEquals(2, v.y);
    }

// com.fasterxml.jackson.databind.deser.creators.TestConstructFromMap::testViaConstructor
    public void testViaConstructor() throws Exception
    {
        ObjectMapper m = new ObjectMapper();
        ConstructorFromMap result = m.readValue
            ("{ \"x\":1, \"y\" : \"abc\" }", ConstructorFromMap.class);
        assertEquals(1, result._x);
        assertEquals("abc", result._y);
    }

// com.fasterxml.jackson.databind.deser.creators.TestConstructFromMap::testViaFactory
    public void testViaFactory() throws Exception
    {
        ObjectMapper m = new ObjectMapper();
        FactoryFromPoint result = m.readValue("{ \"x\" : 3, \"y\" : 4 }", FactoryFromPoint.class);
        assertEquals(3, result._x);
        assertEquals(4, result._y);
    }

// com.fasterxml.jackson.databind.deser.creators.TestConstructFromMap::testViaFactoryUsingString
    public void testViaFactoryUsingString() throws Exception
    {
        ObjectMapper m = new ObjectMapper();
        FactoryFromDecimalString result = m.readValue("\"12.57\"", FactoryFromDecimalString.class);
        assertNotNull(result);
        assertEquals(12, result._value);
    }

// com.fasterxml.jackson.databind.deser.creators.TestCreatorNullValue::testUsesDeserializersNullValue
    public void testUsesDeserializersNullValue() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new TestModule());
        Container container = mapper.readValue("{}", Container.class);
        assertEquals(NULL_CONTAINED, container.contained);
    }

// com.fasterxml.jackson.databind.deser.creators.TestCreatorNullValue::testCreatorReturningNull
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

// com.fasterxml.jackson.databind.deser.creators.TestCreatorWithNamingStrategy556::testRenameViaCtor
    public void testRenameViaCtor() throws Exception
    {
        RenamingCtorBean bean = MAPPER.readValue(CTOR_JSON, RenamingCtorBean.class);
        assertEquals(42, bean.myAge);
        assertEquals("NotMyRealName", bean.myName);
    }

// com.fasterxml.jackson.databind.deser.creators.TestCreatorWithNamingStrategy556::testRenameViaFactory
    public void testRenameViaFactory() throws Exception
    {
        RenamedFactoryBean bean = MAPPER.readValue(CTOR_JSON, RenamedFactoryBean.class);
        assertEquals(42, bean.myAge);
        assertEquals("NotMyRealName", bean.myName);
    }

// com.fasterxml.jackson.databind.deser.creators.TestCreatorWithPolymorphic113::testSubtypes
    public void testSubtypes() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        String id = "nice dogy";
        String json = mapper.writeValueAsString(new AnimalWrapper(new Dog(id)));

        AnimalWrapper wrapper = mapper.readValue(json, AnimalWrapper.class);
        assertEquals(id, wrapper.getAnimal().getId());
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

// com.fasterxml.jackson.databind.deser.creators.TestCreators2::testExceptionFromConstructor
    public void testExceptionFromConstructor() throws Exception
    {
        try {
            MAPPER.readValue("{}", BustedCtor.class);
            fail("Expected exception");
        } catch (JsonMappingException e) {
            verifyException(e, ": foobar");
            
            Throwable t = e.getCause();
            if (t == null) {
                fail("Should have assigned cause for: ("+e.getClass().getSimpleName()+") "+e);
            }
            assertNotNull(t);
            assertEquals(IllegalArgumentException.class, t.getClass());
            assertEquals("foobar", t.getMessage());
        }
    }

// com.fasterxml.jackson.databind.deser.creators.TestCreators2::testSimpleConstructor
    public void testSimpleConstructor() throws Exception
    {
        HashTest test = MAPPER.readValue("{\"type\":\"custom\",\"bytes\":\"abc\" }", HashTest.class);
        assertEquals("custom", test.type);
        assertEquals("abc", new String(test.bytes, "UTF-8"));
    }

// com.fasterxml.jackson.databind.deser.creators.TestCreators2::testMissingPrimitives
    public void testMissingPrimitives() throws Exception
    {
        Primitives p = MAPPER.readValue("{}", Primitives.class);
        assertFalse(p.b);
        assertEquals(0, p.x);
        assertEquals(0.0, p.d);
    }

// com.fasterxml.jackson.databind.deser.creators.TestCreators2::testJackson431
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

// com.fasterxml.jackson.databind.deser.creators.TestCreators2::testJackson438
    public void testJackson438() throws Exception
    {
        Exception e = null;
        try {
            MAPPER.readValue("{ \"name\":\"foobar\" }", BeanFor438.class);
            fail("Should have failed");
        } catch (Exception e0) {
            e = e0;
        }
        if (!(e instanceof JsonMappingException)) {
            fail("Should have received JsonMappingException, caught "+e.getClass().getName());
        }
        verifyException(e, "don't like that name");
        
        Throwable t = e.getCause();
        if (t == null) {
            fail("Should have assigned cause for: ("+e.getClass().getSimpleName()+") "+e);
        }
        assertEquals(IllegalArgumentException.class, t.getClass());
        verifyException(e, "don't like that name");
    }

// com.fasterxml.jackson.databind.deser.creators.TestCreators2::testCreatorWithDupNames
    public void testCreatorWithDupNames() throws Exception
    {
        try {
            MAPPER.readValue("{\"bar\":\"x\"}", BrokenCreatorBean.class);
            fail("Should have caught duplicate creator parameters");
        } catch (JsonMappingException e) {
            verifyException(e, "duplicate creator property \"bar\"");
            verifyException(e, "for type `com.fasterxml.jackson.databind.");
            verifyException(e, "$BrokenCreatorBean`");
        }
    }

// com.fasterxml.jackson.databind.deser.creators.TestCreators2::testCreatorMultipleArgumentWithoutAnnotation
    public void testCreatorMultipleArgumentWithoutAnnotation() throws Exception {
        AutoDetectConstructorBean value = MAPPER.readValue("{\"bar\":\"bar\",\"foo\":\"foo\"}",
                AutoDetectConstructorBean.class);
        assertEquals("bar", value.bar);
        assertEquals("foo", value.foo);
    }

// com.fasterxml.jackson.databind.deser.creators.TestCreators2::testIgnoredSingleArgCtor
    public void testIgnoredSingleArgCtor() throws Exception
    {
        try {
            MAPPER.readValue(quote("abc"), IgnoredCtor.class);
            fail("Should have caught missing constructor problem");
        } catch (JsonMappingException e) {
            verifyException(e, "no String-argument constructor/factory method");
        }
    }

// com.fasterxml.jackson.databind.deser.creators.TestCreators2::testAbstractFactory
    public void testAbstractFactory() throws Exception
    {
        AbstractBase bean = MAPPER.readValue("{\"a\":3}", AbstractBase.class);
        assertNotNull(bean);
        AbstractBaseImpl impl = (AbstractBaseImpl) bean;
        assertEquals(1, impl.props.size());
        assertEquals(Integer.valueOf(3), impl.props.get("a"));
    }

// com.fasterxml.jackson.databind.deser.creators.TestCreators2::testCreatorProperties
    public void testCreatorProperties() throws Exception
    {
        Issue700Bean value = MAPPER.readValue("{ \"item\" : \"foo\" }", Issue700Bean.class);
        assertNotNull(value);
    }

// com.fasterxml.jackson.databind.deser.creators.TestCreators2::testConstructorChoice
    public void testConstructorChoice() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        MultiPropCreator1476 pojo = mapper.readValue("{ \"intField\": 1, \"stringField\": \"foo\" }",
                MultiPropCreator1476.class);
        assertEquals(1, pojo.getIntField());
        assertEquals("foo", pojo.getStringField());
    }

// com.fasterxml.jackson.databind.deser.creators.TestCreators3::testCreator541
    public void testCreator541() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        mapper.disable(
                MapperFeature.AUTO_DETECT_CREATORS,
                MapperFeature.AUTO_DETECT_FIELDS,
                MapperFeature.AUTO_DETECT_GETTERS,
                MapperFeature.AUTO_DETECT_IS_GETTERS,
                MapperFeature.AUTO_DETECT_SETTERS,
                MapperFeature.USE_GETTERS_AS_SETTERS
        );
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        mapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
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

// com.fasterxml.jackson.databind.deser.creators.TestCreators3::testMultiCtor421
    public void testMultiCtor421() throws Exception
    {
        final ObjectMapper mapper = newObjectMapper();
        mapper.setAnnotationIntrospector(new MyParamIntrospector());

        MultiCtor bean = mapper.readValue(aposToQuotes("{'a':'123','b':'foo'}"), MultiCtor.class);
        assertNotNull(bean);
        assertEquals("123", bean._a);
        assertEquals("foo", bean._b);
    }

// com.fasterxml.jackson.databind.deser.creators.TestCreators3::testSerialization
    public void testSerialization() throws Exception {
        assertEquals(quote("testProduct"),
                MAPPER.writeValueAsString(new Product1853(false, "testProduct")));
    }

// com.fasterxml.jackson.databind.deser.creators.TestCreators3::testDeserializationFromObject
    public void testDeserializationFromObject() throws Exception {
        final String EXAMPLE_DATA = "{\"name\":\"dummy\",\"other\":{},\"errors\":{}}";
        assertEquals("PROP:dummy", MAPPER.readValue(EXAMPLE_DATA, Product1853.class).getName());
    }

// com.fasterxml.jackson.databind.deser.creators.TestCreators3::testDeserializationFromString
    public void testDeserializationFromString() throws Exception {
        assertEquals("DELEG:testProduct",
                MAPPER.readValue(quote("testProduct"), Product1853.class).getName());
    }

// com.fasterxml.jackson.databind.deser.creators.TestCreatorsDelegating::testBooleanDelegate
    public void testBooleanDelegate() throws Exception
    {
        
        BooleanBean bb = MAPPER.readValue("true", BooleanBean.class);
        assertEquals(Boolean.TRUE, bb.value);

        
        bb = MAPPER.readValue(quote("true"), BooleanBean.class);
        assertEquals(Boolean.TRUE, bb.value);
    }

// com.fasterxml.jackson.databind.deser.creators.TestCreatorsDelegating::testWithCtorAndDelegate
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

// com.fasterxml.jackson.databind.deser.creators.TestCreatorsDelegating::testWithFactoryAndDelegate
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

// com.fasterxml.jackson.databind.deser.creators.TestCreatorsDelegating::testDelegateWithTokenBuffer
    public void testDelegateWithTokenBuffer() throws Exception
    {
        Value592 value = MAPPER.readValue("{\"a\":1,\"b\":2}", Value592.class);
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

// com.fasterxml.jackson.databind.deser.creators.TestCreatorsDelegating::testIssue465
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

// com.fasterxml.jackson.databind.deser.creators.TestCreatorsWithIdentity::testSimple
	public void testSimple() throws IOException
	{
	    String parentStr = "{\"id\" : \"1\", \"parentProp\" : \"parent\"}";
	    String childStr = "{\"childProp\" : \"child\", \"parent\" : " + parentStr + "}";
	    Parent parent = JSON_MAPPER.readValue(parentStr, Parent.class);
	    assertNotNull(parent);
	    Child child = JSON_MAPPER.readValue(childStr, Child.class);
	    assertNotNull(child);
	    assertNotNull(child.parent);
	}

// com.fasterxml.jackson.databind.deser.creators.TestCustomValueInstDefaults::testAllPresent
    public void testAllPresent() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new BucketModule());

        Bucket allPresent = mapper.readValue(
                "{\"a\":8,\"b\":9,\"c\":\"y\",\"d\":\"z\"}",
                Bucket.class);

        assertEquals(8, allPresent.a);
        assertEquals(9, allPresent.b);
        assertEquals("y", allPresent.c);
        assertEquals("z", allPresent.d);
    }

// com.fasterxml.jackson.databind.deser.creators.TestCustomValueInstDefaults::testAllAbsent
    public void testAllAbsent() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new BucketModule());

        Bucket allAbsent = mapper.readValue(
                "{}",
                Bucket.class);

        assertEquals(Bucket.DEFAULT_A, allAbsent.a);
        assertEquals(Bucket.DEFAULT_B, allAbsent.b);
        assertEquals(Bucket.DEFAULT_C, allAbsent.c);
        assertEquals(Bucket.DEFAULT_D, allAbsent.d);
    }

// com.fasterxml.jackson.databind.deser.creators.TestCustomValueInstDefaults::testMixedPresentAndAbsent
    public void testMixedPresentAndAbsent() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new BucketModule());

        Bucket aAbsent = mapper.readValue(
                "{\"b\":9,\"c\":\"y\",\"d\":\"z\"}",
                Bucket.class);

        assertEquals(Bucket.DEFAULT_A, aAbsent.a);
        assertEquals(9, aAbsent.b);
        assertEquals("y", aAbsent.c);
        assertEquals("z", aAbsent.d);

        Bucket bAbsent = mapper.readValue(
                "{\"a\":8,\"c\":\"y\",\"d\":\"z\"}",
                Bucket.class);

        assertEquals(8, bAbsent.a);
        assertEquals(Bucket.DEFAULT_B, bAbsent.b);
        assertEquals("y", bAbsent.c);
        assertEquals("z", bAbsent.d);

        Bucket cAbsent = mapper.readValue(
                "{\"a\":8,\"b\":9,\"d\":\"z\"}",
                Bucket.class);

        assertEquals(8, cAbsent.a);
        assertEquals(9, cAbsent.b);
        assertEquals(Bucket.DEFAULT_C, cAbsent.c);
        assertEquals("z", cAbsent.d);

        Bucket dAbsent = mapper.readValue(
                "{\"a\":8,\"b\":9,\"c\":\"y\"}",
                Bucket.class);

        assertEquals(8, dAbsent.a);
        assertEquals(9, dAbsent.b);
        assertEquals("y", dAbsent.c);
        assertEquals(Bucket.DEFAULT_D, dAbsent.d);
    }

// com.fasterxml.jackson.databind.deser.creators.TestCustomValueInstDefaults::testPresentZeroPrimitive
    public void testPresentZeroPrimitive() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new BucketModule());

        Bucket aZeroRestAbsent = mapper.readValue(
                "{\"a\":0}",
                Bucket.class);

        assertEquals(0, aZeroRestAbsent.a);
        assertEquals(Bucket.DEFAULT_B, aZeroRestAbsent.b);
        assertEquals(Bucket.DEFAULT_C, aZeroRestAbsent.c);
        assertEquals(Bucket.DEFAULT_D, aZeroRestAbsent.d);
    }

// com.fasterxml.jackson.databind.deser.creators.TestCustomValueInstDefaults::testPresentNullReference
    public void testPresentNullReference() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new BucketModule());

        Bucket cNullRestAbsent = mapper.readValue(
                "{\"c\":null}",
                Bucket.class);

        assertEquals(Bucket.DEFAULT_A, cNullRestAbsent.a);
        assertEquals(Bucket.DEFAULT_B, cNullRestAbsent.b);
        assertEquals(null, cNullRestAbsent.c);
        assertEquals(Bucket.DEFAULT_D, cNullRestAbsent.d);
    }

// com.fasterxml.jackson.databind.deser.creators.TestCustomValueInstDefaults::testMoreThan32CreatorParams
    public void testMoreThan32CreatorParams() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new BucketModule());

        BigBucket big = mapper.readValue(
                "{\"i03\":0,\"i11\":1,\"s05\":null,\"s08\":\"x\"}",
                BigBucket.class);

        assertEquals(BigBucket.DEFAULT_I, big.i01);
        assertEquals(BigBucket.DEFAULT_I, big.i02);
        assertEquals(0, big.i03);
        assertEquals(BigBucket.DEFAULT_I, big.i04);
        assertEquals(BigBucket.DEFAULT_I, big.i05);
        assertEquals(BigBucket.DEFAULT_I, big.i06);
        assertEquals(BigBucket.DEFAULT_I, big.i07);
        assertEquals(BigBucket.DEFAULT_I, big.i08);
        assertEquals(BigBucket.DEFAULT_I, big.i09);
        assertEquals(BigBucket.DEFAULT_I, big.i10);
        assertEquals(1, big.i11);
        assertEquals(BigBucket.DEFAULT_I, big.i12);
        assertEquals(BigBucket.DEFAULT_I, big.i13);
        assertEquals(BigBucket.DEFAULT_I, big.i14);
        assertEquals(BigBucket.DEFAULT_I, big.i15);
        assertEquals(BigBucket.DEFAULT_I, big.i16);
        assertEquals(BigBucket.DEFAULT_S, big.s01);
        assertEquals(BigBucket.DEFAULT_S, big.s02);
        assertEquals(BigBucket.DEFAULT_S, big.s03);
        assertEquals(BigBucket.DEFAULT_S, big.s04);
        assertEquals(null, big.s05);
        assertEquals(BigBucket.DEFAULT_S, big.s06);
        assertEquals(BigBucket.DEFAULT_S, big.s07);
        assertEquals("x", big.s08);
        assertEquals(BigBucket.DEFAULT_S, big.s09);
        assertEquals(BigBucket.DEFAULT_S, big.s10);
        assertEquals(BigBucket.DEFAULT_S, big.s11);
        assertEquals(BigBucket.DEFAULT_S, big.s12);
        assertEquals(BigBucket.DEFAULT_S, big.s13);
        assertEquals(BigBucket.DEFAULT_S, big.s14);
        assertEquals(BigBucket.DEFAULT_S, big.s15);
        assertEquals(BigBucket.DEFAULT_S, big.s16);
    }

// com.fasterxml.jackson.databind.deser.creators.TestCustomValueInstDefaults::testClassWith32CreatorParams
    public void testClassWith32CreatorParams() throws Exception
    {
        StringBuilder sb = new StringBuilder()
                .append("{\n");
        for (int i = 1; i <= 32; ++i) {
            sb.append("\"p").append(i).append("\" : \"NotNull")
                .append(i).append("\"");
            if (i < 32) {
                sb.append(",\n");
            }
        }
        sb.append("\n}\n");
        String json = sb.toString();
        ObjectMapper mapper = new ObjectMapper()
                .registerModule(new ClassWith32Module());
        ClassWith32Props result = mapper.readValue(json, ClassWith32Props.class);
        
        assertEquals("NotNull1", result.p1);
        assertEquals("NotNull2", result.p2);
        assertEquals("NotNull31", result.p31);
        assertEquals("NotNull32", result.p32);
    }

// com.fasterxml.jackson.databind.deser.creators.TestPolymorphicCreators::testManualPolymorphicDog
    public void testManualPolymorphicDog() throws Exception
    {
        
        Animal animal = MAPPER.readValue("{ \"type\":\"dog\", \"name\":\"Fido\", \"barkVolume\" : 95.0 }", Animal.class);
        assertEquals(Dog.class, animal.getClass());
        assertEquals("Fido", animal.name);
        assertEquals(95.0, ((Dog) animal).barkVolume);
    }

// com.fasterxml.jackson.databind.deser.creators.TestPolymorphicCreators::testManualPolymorphicCatBasic
    public void testManualPolymorphicCatBasic() throws Exception
    {
        
        Animal animal = MAPPER.readValue("{ \"name\" : \"Macavity\", \"type\":\"cat\", \"lives\":18, \"likesCream\":false }", Animal.class);
        assertEquals(Cat.class, animal.getClass());
        assertEquals("Macavity", animal.name); 
        Cat cat = (Cat) animal;
        assertEquals(18, cat.lives);
        
        assertEquals(false, cat.likesCream);
    }

// com.fasterxml.jackson.databind.deser.creators.TestPolymorphicCreators::testManualPolymorphicCatWithReorder
    public void testManualPolymorphicCatWithReorder() throws Exception
    {
        
        Animal animal = MAPPER.readValue("{ \"likesCream\":true, \"name\" : \"Venla\", \"type\":\"cat\" }", Animal.class);
        assertEquals(Cat.class, animal.getClass());
        assertEquals("Venla", animal.name);
        
        assertTrue(((Cat) animal).likesCream);
    }

// com.fasterxml.jackson.databind.deser.creators.TestPolymorphicCreators::testManualPolymorphicWithNumbered
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

// com.fasterxml.jackson.databind.deser.creators.TestPolymorphicDelegating::testAbstractDelegateWithCreator
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

// com.fasterxml.jackson.databind.deser.filter.IgnorePropertyOnDeserTest::testIgnoreOnProperty1217
    public void testIgnoreOnProperty1217() throws Exception
    {
        TestIgnoreObject result = MAPPER.readValue(
                aposToQuotes("{'obj':{'x': 10, 'y': 20}, 'obj2':{'x': 10, 'y': 20}}"),
                TestIgnoreObject.class);
        assertEquals(20, result.obj.y);
        assertEquals(10, result.obj2.x);

        assertEquals(1, result.obj.x);
        assertEquals(2, result.obj2.y);

        TestIgnoreObject result1 = MAPPER.readValue(
                  aposToQuotes("{'obj':{'x': 20, 'y': 30}, 'obj2':{'x': 20, 'y': 40}}"),
                  TestIgnoreObject.class);
        assertEquals(1, result1.obj.x);
        assertEquals(30, result1.obj.y);
       
        assertEquals(20, result1.obj2.x);
        assertEquals(2, result1.obj2.y);
    }

// com.fasterxml.jackson.databind.deser.filter.IgnorePropertyOnDeserTest::testIgnoreViaConfigOverride1217
    public void testIgnoreViaConfigOverride1217() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configOverride(Point.class)
            .setIgnorals(JsonIgnoreProperties.Value.forIgnoredProperties("y"));
        Point p = mapper.readValue(aposToQuotes("{'x':1,'y':2}"), Point.class);
        
        assertEquals(1, p.x);
        assertEquals(0, p.y);
    }

// com.fasterxml.jackson.databind.deser.filter.IgnorePropertyOnDeserTest::testIgnoreGetterNotSetter1595
    public void testIgnoreGetterNotSetter1595() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        Simple1595 config = new Simple1595();
        config.setId(123);
        config.setName("jack");
        String json = mapper.writeValueAsString(config);
        assertEquals(aposToQuotes("{'id':123}"), json);
        Simple1595 des = mapper.readValue(aposToQuotes("{'id':123,'name':'jack'}"), Simple1595.class);
        assertEquals("jack", des.getName());
    }

// com.fasterxml.jackson.databind.deser.filter.NullConversionsForContentTest::testFailOnNullFromDefaults
    public void testFailOnNullFromDefaults() throws Exception
    {
        final String JSON = aposToQuotes("{'values':[null]}");
        TypeReference<?> listType = new TypeReference<NullContentUndefined<List<String>>>() { };

        
        NullContentUndefined<List<String>> result = MAPPER.readValue(JSON, listType);
        assertNotNull(result.values);
        assertEquals(1, result.values.size());
        assertNull(result.values.get(0));

        
        ObjectMapper mapper = newObjectMapper();
        mapper.setDefaultSetterInfo(JsonSetter.Value.forContentNulls(Nulls.FAIL));
        try {
            mapper.readValue(JSON, listType);
            fail("Should not pass");
        } catch (InvalidNullException e) {
            verifyException(e, "property \"values\"");
        }

        
        mapper = newObjectMapper();
        mapper.configOverride(List.class)
                .setSetterInfo(JsonSetter.Value.forContentNulls(Nulls.FAIL));
        try {
            mapper.readValue(JSON, listType);
            fail("Should not pass");
        } catch (InvalidNullException e) {
            verifyException(e, "property \"values\"");
        }
    }

// com.fasterxml.jackson.databind.deser.filter.NullConversionsForContentTest::testFailOnNullWithCollections
    public void testFailOnNullWithCollections() throws Exception
    {
        TypeReference<?> typeRef = new TypeReference<NullContentFail<List<Integer>>>() { };

        
        NullContentFail<List<Integer>> result = MAPPER.readValue(aposToQuotes("{'nullsOk':[null]}"),
                typeRef);
        assertNotNull(result.nullsOk);
        assertEquals(1, result.nullsOk.size());
        assertNull(result.nullsOk.get(0));

        
        
        
        final String JSON = aposToQuotes("{'noNulls':[null]}");
        try {
            MAPPER.readValue(JSON, typeRef);
            fail("Should not pass");
        } catch (InvalidNullException e) {
            verifyException(e, "property \"noNulls\"");
        }

        
        try {
            MAPPER.readValue(JSON, new TypeReference<NullContentFail<List<String>>>() { });
            fail("Should not pass");
        } catch (InvalidNullException e) {
            verifyException(e, "property \"noNulls\"");
        }
    }

// com.fasterxml.jackson.databind.deser.filter.NullConversionsForContentTest::testFailOnNullWithArrays
    public void testFailOnNullWithArrays() throws Exception
    {
        final String JSON = aposToQuotes("{'noNulls':[null]}");
        
        try {
            MAPPER.readValue(JSON, new TypeReference<NullContentFail<Object[]>>() { });
            fail("Should not pass");
        } catch (InvalidNullException e) {
            verifyException(e, "property \"noNulls\"");
        }

        
        try {
            MAPPER.readValue(JSON, new TypeReference<NullContentFail<String[]>>() { });
            fail("Should not pass");
        } catch (InvalidNullException e) {
            verifyException(e, "property \"noNulls\"");
        }
    }

// com.fasterxml.jackson.databind.deser.filter.NullConversionsForContentTest::testFailOnNullWithPrimitiveArrays
    public void testFailOnNullWithPrimitiveArrays() throws Exception
    {
        final String JSON = aposToQuotes("{'noNulls':[null]}");

        
        try {
            MAPPER.readValue(JSON, new TypeReference<NullContentFail<boolean[]>>() { });
            fail("Should not pass");
        } catch (InvalidNullException e) {
            verifyException(e, "property \"noNulls\"");
        }
        
        try {
            MAPPER.readValue(JSON, new TypeReference<NullContentFail<int[]>>() { });
            fail("Should not pass");
        } catch (InvalidNullException e) {
            verifyException(e, "property \"noNulls\"");
        }
        
        try {
            MAPPER.readValue(JSON, new TypeReference<NullContentFail<double[]>>() { });
            fail("Should not pass");
        } catch (InvalidNullException e) {
            verifyException(e, "property \"noNulls\"");
        }
    }

// com.fasterxml.jackson.databind.deser.filter.NullConversionsForContentTest::testFailOnNullWithMaps
    public void testFailOnNullWithMaps() throws Exception
    {
        
        try {
            final String MAP_JSON = aposToQuotes("{'noNulls':{'a':null}}");
            MAPPER.readValue(MAP_JSON, new TypeReference<NullContentFail<Map<String,String>>>() { });
            fail("Should not pass");
        } catch (InvalidNullException e) {
            verifyException(e, "property \"noNulls\"");
        }

        
        try {
            final String MAP_JSON = aposToQuotes("{'noNulls':{'A':null}}");
            MAPPER.readValue(MAP_JSON, new TypeReference<NullContentFail<EnumMap<ABC,String>>>() { });
            fail("Should not pass");
        } catch (InvalidNullException e) {
            verifyException(e, "property \"noNulls\"");
        }
    }

// com.fasterxml.jackson.databind.deser.filter.NullConversionsForContentTest::testNullsAsEmptyWithCollections
    public void testNullsAsEmptyWithCollections() throws Exception
    {
        final String JSON = aposToQuotes("{'values':[null]}");

        
        {
            NullContentAsEmpty<List<Integer>> result = MAPPER.readValue(JSON,
                    new TypeReference<NullContentAsEmpty<List<Integer>>>() { });
            assertEquals(1, result.values.size());
            assertEquals(Integer.valueOf(0), result.values.get(0));
        }

        
        {
            NullContentAsEmpty<List<String>> result = MAPPER.readValue(JSON,
                    new TypeReference<NullContentAsEmpty<List<String>>>() { });
            assertEquals(1, result.values.size());
            assertEquals("", result.values.get(0));
        }
    }

// com.fasterxml.jackson.databind.deser.filter.NullConversionsForContentTest::testNullsAsEmptyUsingDefaults
    public void testNullsAsEmptyUsingDefaults() throws Exception
    {
        final String JSON = aposToQuotes("{'values':[null]}");
        TypeReference<?> listType = new TypeReference<NullContentUndefined<List<Integer>>>() { };

        
        ObjectMapper mapper = newObjectMapper();
        mapper.setDefaultSetterInfo(JsonSetter.Value.forContentNulls(Nulls.AS_EMPTY));
        NullContentUndefined<List<Integer>> result = mapper.readValue(JSON, listType);
        assertEquals(1, result.values.size());
        assertEquals(Integer.valueOf(0), result.values.get(0));

        
        mapper = newObjectMapper();
        mapper.configOverride(List.class)
                .setSetterInfo(JsonSetter.Value.forContentNulls(Nulls.AS_EMPTY));
        result = mapper.readValue(JSON, listType);
        assertEquals(1, result.values.size());
        assertEquals(Integer.valueOf(0), result.values.get(0));
    }

// com.fasterxml.jackson.databind.deser.filter.NullConversionsForContentTest::testNullsAsEmptyWithArrays
    public void testNullsAsEmptyWithArrays() throws Exception
    {
        
        final String JSON = aposToQuotes("{'values':[null]}");

        
        {
            NullContentAsEmpty<String[]> result = MAPPER.readValue(JSON,
                    new TypeReference<NullContentAsEmpty<String[]>>() { });
            assertEquals(1, result.values.length);
            assertEquals("", result.values[0]);
        }
    }

// com.fasterxml.jackson.databind.deser.filter.NullConversionsForContentTest::testNullsAsEmptyWithPrimitiveArrays
    public void testNullsAsEmptyWithPrimitiveArrays() throws Exception
    {
        final String JSON = aposToQuotes("{'values':[null]}");

        
        {
            NullContentAsEmpty<int[]> result = MAPPER.readValue(JSON,
                    new TypeReference<NullContentAsEmpty<int[]>>() { });
            assertEquals(1, result.values.length);
            assertEquals(0, result.values[0]);
        }

        
        {
            NullContentAsEmpty<long[]> result = MAPPER.readValue(JSON,
                    new TypeReference<NullContentAsEmpty<long[]>>() { });
            assertEquals(1, result.values.length);
            assertEquals(0L, result.values[0]);
        }

        
        {
            NullContentAsEmpty<boolean[]> result = MAPPER.readValue(JSON,
                    new TypeReference<NullContentAsEmpty<boolean[]>>() { });
            assertEquals(1, result.values.length);
            assertEquals(false, result.values[0]);
        }
}

// com.fasterxml.jackson.databind.deser.filter.NullConversionsForContentTest::testNullsAsEmptyWithMaps
    public void testNullsAsEmptyWithMaps() throws Exception
    {
        
        final String MAP_JSON = aposToQuotes("{'values':{'A':null}}");
        {
            NullContentAsEmpty<Map<String,String>> result 
                = MAPPER.readValue(MAP_JSON, new TypeReference<NullContentAsEmpty<Map<String,String>>>() { });
            assertEquals(1, result.values.size());
            assertEquals("A", result.values.entrySet().iterator().next().getKey());
            assertEquals("", result.values.entrySet().iterator().next().getValue());
        }

        
        {
            NullContentAsEmpty<EnumMap<ABC,String>> result 
                = MAPPER.readValue(MAP_JSON, new TypeReference<NullContentAsEmpty<EnumMap<ABC,String>>>() { });
            assertEquals(1, result.values.size());
            assertEquals(ABC.A, result.values.entrySet().iterator().next().getKey());
            assertEquals("", result.values.entrySet().iterator().next().getValue());
        }
    }

// com.fasterxml.jackson.databind.deser.filter.NullConversionsForContentTest::testNullsSkipUsingDefaults
    public void testNullsSkipUsingDefaults() throws Exception
    {
        final String JSON = aposToQuotes("{'values':[null]}");
        TypeReference<?> listType = new TypeReference<NullContentUndefined<List<Long>>>() { };

        
        ObjectMapper mapper = newObjectMapper();
        mapper.setDefaultSetterInfo(JsonSetter.Value.forContentNulls(Nulls.SKIP));
        NullContentUndefined<List<Long>> result = mapper.readValue(JSON, listType);
        assertEquals(0, result.values.size());

        
        mapper = newObjectMapper();
        mapper.configOverride(List.class)
                .setSetterInfo(JsonSetter.Value.forContentNulls(Nulls.SKIP));
        result = mapper.readValue(JSON, listType);
        assertEquals(0, result.values.size());
    }

// com.fasterxml.jackson.databind.deser.filter.NullConversionsForContentTest::testNullsSkipWithOverrides
    public void testNullsSkipWithOverrides() throws Exception
    {
        final String JSON = aposToQuotes("{'values':[null]}");
        TypeReference<?> listType = new TypeReference<NullContentSkip<List<Long>>>() { };

        ObjectMapper mapper = newObjectMapper();
        
        mapper.setDefaultSetterInfo(JsonSetter.Value.forContentNulls(Nulls.FAIL));
        NullContentSkip<List<Long>> result = mapper.readValue(JSON, listType);
        assertEquals(0, result.values.size());

        
        mapper = newObjectMapper();
        mapper.configOverride(List.class)
                .setSetterInfo(JsonSetter.Value.forContentNulls(Nulls.FAIL));
        result = mapper.readValue(JSON, listType);
        assertEquals(0, result.values.size());
    }

// com.fasterxml.jackson.databind.deser.filter.NullConversionsForContentTest::testNullsSkipWithCollections
    public void testNullsSkipWithCollections() throws Exception
    {
        
        {
            final String JSON = aposToQuotes("{'values':[1,null,2]}");
            NullContentSkip<List<Integer>> result = MAPPER.readValue(JSON,
                    new TypeReference<NullContentSkip<List<Integer>>>() { });
            assertEquals(2, result.values.size());
            assertEquals(Integer.valueOf(1), result.values.get(0));
            assertEquals(Integer.valueOf(2), result.values.get(1));
        }

        
        {
            final String JSON = aposToQuotes("{'values':['ab',null,'xy']}");
            NullContentSkip<List<String>> result = MAPPER.readValue(JSON,
                    new TypeReference<NullContentSkip<List<String>>>() { });
            assertEquals(2, result.values.size());
            assertEquals("ab", result.values.get(0));
            assertEquals("xy", result.values.get(1));
        }
    }

// com.fasterxml.jackson.databind.deser.filter.NullConversionsForContentTest::testNullsSkipWithArrays
    public void testNullsSkipWithArrays() throws Exception
    {
        final String JSON = aposToQuotes("{'values':['a',null,'xy']}");
        
        {
            NullContentSkip<Object[]> result = MAPPER.readValue(JSON,
                    new TypeReference<NullContentSkip<Object[]>>() { });
            assertEquals(2, result.values.length);
            assertEquals("a", result.values[0]);
            assertEquals("xy", result.values[1]);
        }
        
        {
            NullContentSkip<String[]> result = MAPPER.readValue(JSON,
                    new TypeReference<NullContentSkip<String[]>>() { });
            assertEquals(2, result.values.length);
            assertEquals("a", result.values[0]);
            assertEquals("xy", result.values[1]);
        }
    }

// com.fasterxml.jackson.databind.deser.filter.NullConversionsForContentTest::testNullsSkipWithPrimitiveArrays
    public void testNullsSkipWithPrimitiveArrays() throws Exception
    {
        
        {
            final String JSON = aposToQuotes("{'values':[3,null,7]}");
            NullContentSkip<int[]> result = MAPPER.readValue(JSON,
                    new TypeReference<NullContentSkip<int[]>>() { });
            assertEquals(2, result.values.length);
            assertEquals(3, result.values[0]);
            assertEquals(7, result.values[1]);
        }

        
        {
            final String JSON = aposToQuotes("{'values':[-13,null,999]}");
            NullContentSkip<long[]> result = MAPPER.readValue(JSON,
                    new TypeReference<NullContentSkip<long[]>>() { });
            assertEquals(2, result.values.length);
            assertEquals(-13L, result.values[0]);
            assertEquals(999L, result.values[1]);
        }

        
        {
            final String JSON = aposToQuotes("{'values':[true,null,true]}");
            NullContentSkip<boolean[]> result = MAPPER.readValue(JSON,
                    new TypeReference<NullContentSkip<boolean[]>>() { });
            assertEquals(2, result.values.length);
            assertEquals(true, result.values[0]);
            assertEquals(true, result.values[1]);
        }
    }

// com.fasterxml.jackson.databind.deser.filter.NullConversionsForContentTest::testNullsSkipWithMaps
    public void testNullsSkipWithMaps() throws Exception
    {
        
        final String MAP_JSON = aposToQuotes("{'values':{'A':'foo','B':null,'C':'bar'}}");
        {
            NullContentSkip<Map<String,String>> result 
                = MAPPER.readValue(MAP_JSON, new TypeReference<NullContentSkip<Map<String,String>>>() { });
            assertEquals(2, result.values.size());
            assertEquals("foo", result.values.get("A"));
            assertEquals("bar", result.values.get("C"));
        }

        
        {
            NullContentSkip<EnumMap<ABC,String>> result 
                = MAPPER.readValue(MAP_JSON, new TypeReference<NullContentSkip<EnumMap<ABC,String>>>() { });
            assertEquals(2, result.values.size());
            assertEquals("foo", result.values.get(ABC.A));
            assertEquals("bar", result.values.get(ABC.C));
        }
    }

// com.fasterxml.jackson.databind.deser.filter.NullConversionsGenericTest::testNullsToEmptyPojo
    public void testNullsToEmptyPojo() throws Exception
    {
        GeneralEmpty<Point> result = MAPPER.readValue(aposToQuotes("{'value':null}"),
                new TypeReference<GeneralEmpty<Point>>() { });
        assertNotNull(result.value);
        Point p = result.value;
        assertEquals(0, p.x);
        assertEquals(0, p.y);

        
        try {
             MAPPER.readValue(aposToQuotes("{'value':null}"),
                    NoCtorWrapper.class);
            fail("Should not pass");
        } catch (JsonMappingException e) {
            verifyException(e, "Cannot create empty instance");
        }
    }

// com.fasterxml.jackson.databind.deser.filter.NullConversionsGenericTest::testEmptyStringToNullToEmptyPojo
    public void testEmptyStringToNullToEmptyPojo() throws Exception
    {
        GeneralEmpty<Point> result = MAPPER.readerFor(new TypeReference<GeneralEmpty<Point>>() { })
                .with(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT)
                .readValue(aposToQuotes("{'value':''}"));
        assertNotNull(result.value);
        Point p = result.value;
        assertEquals(0, p.x);
        assertEquals(0, p.y);
    }

// com.fasterxml.jackson.databind.deser.filter.NullConversionsGenericTest::testNullsToEmptyCollection
    public void testNullsToEmptyCollection() throws Exception
    {
        GeneralEmpty<List<String>> result = MAPPER.readValue(aposToQuotes("{'value':null}"),
                new TypeReference<GeneralEmpty<List<String>>>() { });
        assertNotNull(result.value);
        assertEquals(0, result.value.size());

        
        GeneralEmpty<List<Integer>> result2 = MAPPER.readValue(aposToQuotes("{'value':null}"),
                new TypeReference<GeneralEmpty<List<Integer>>>() { });
        assertNotNull(result2.value);
        assertEquals(0, result2.value.size());
    }

// com.fasterxml.jackson.databind.deser.filter.NullConversionsGenericTest::testNullsToEmptyMap
    public void testNullsToEmptyMap() throws Exception
    {
        GeneralEmpty<Map<String,String>> result = MAPPER.readValue(aposToQuotes("{'value':null}"),
                new TypeReference<GeneralEmpty<Map<String,String>>>() { });
        assertNotNull(result.value);
        assertEquals(0, result.value.size());
    }

// com.fasterxml.jackson.databind.deser.filter.NullConversionsGenericTest::testNullsToEmptyArrays
    public void testNullsToEmptyArrays() throws Exception
    {
        final String json = aposToQuotes("{'value':null}");

        GeneralEmpty<Object[]> result = MAPPER.readValue(json,
                new TypeReference<GeneralEmpty<Object[]>>() { });
        assertNotNull(result.value);
        assertEquals(0, result.value.length);

        GeneralEmpty<String[]> result2 = MAPPER.readValue(json,
                new TypeReference<GeneralEmpty<String[]>>() { });
        assertNotNull(result2.value);
        assertEquals(0, result2.value.length);

        GeneralEmpty<int[]> result3 = MAPPER.readValue(json,
                new TypeReference<GeneralEmpty<int[]>>() { });
        assertNotNull(result3.value);
        assertEquals(0, result3.value.length);

        GeneralEmpty<double[]> result4 = MAPPER.readValue(json,
                new TypeReference<GeneralEmpty<double[]>>() { });
        assertNotNull(result4.value);
        assertEquals(0, result4.value.length);

        GeneralEmpty<boolean[]> result5 = MAPPER.readValue(json,
                new TypeReference<GeneralEmpty<boolean[]>>() { });
        assertNotNull(result5.value);
        assertEquals(0, result5.value.length);
    }

// com.fasterxml.jackson.databind.deser.filter.NullConversionsPojoTest::testFailOnNull
    public void testFailOnNull() throws Exception
    {
        
        NullFail result = MAPPER.readValue(aposToQuotes("{'noNulls':'foo', 'nullsOk':null}"),
                NullFail.class);
        assertEquals("foo", result.noNulls);
        assertNull(result.nullsOk);

        
        try {
            result = MAPPER.readValue(aposToQuotes("{'noNulls':null}"),
                    NullFail.class);
            fail("Should not pass");
        } catch (InvalidNullException e) {
            verifyException(e, "property \"noNulls\"");
        }
    }

// com.fasterxml.jackson.databind.deser.filter.NullConversionsPojoTest::testFailOnNullWithDefaults
    public void testFailOnNullWithDefaults() throws Exception
    {
        
        String json = aposToQuotes("{'name':null}");
        NullsForString def = MAPPER.readValue(json, NullsForString.class);
        assertNull(def.getName());
        
        ObjectMapper mapper = newObjectMapper();
        mapper.configOverride(String.class)
            .setSetterInfo(JsonSetter.Value.forValueNulls(Nulls.FAIL));
        try {
            mapper.readValue(json, NullsForString.class);
            fail("Should not pass");
        } catch (InvalidNullException e) {
            verifyException(e, "property \"name\"");
        }
    }

// com.fasterxml.jackson.databind.deser.filter.NullConversionsPojoTest::testNullsToEmptyScalar
    public void testNullsToEmptyScalar() throws Exception
    {
        NullAsEmpty result = MAPPER.readValue(aposToQuotes("{'nullAsEmpty':'foo', 'nullsOk':null}"),
                NullAsEmpty.class);
        assertEquals("foo", result.nullAsEmpty);
        assertNull(result.nullsOk);

        
        result = MAPPER.readValue(aposToQuotes("{'nullAsEmpty':null}"),
                NullAsEmpty.class);
        assertEquals("", result.nullAsEmpty);

        
        String json = aposToQuotes("{'name':null}");
        NullsForString def = MAPPER.readValue(json, NullsForString.class);
        assertNull(def.getName());

        ObjectMapper mapper = newObjectMapper();
        mapper.configOverride(String.class)
            .setSetterInfo(JsonSetter.Value.forValueNulls(Nulls.AS_EMPTY));
        NullsForString named = mapper.readValue(json, NullsForString.class);
        assertEquals("", named.getName());
    }

// com.fasterxml.jackson.databind.deser.filter.NullConversionsSkipTest::testSkipNullField
    public void testSkipNullField() throws Exception
    {
        
        NullSkipField result = MAPPER.readValue(aposToQuotes("{'noNulls':'foo', 'nullsOk':null}"),
                NullSkipField.class);
        assertEquals("foo", result.noNulls);
        assertNull(result.nullsOk);

        
        result = MAPPER.readValue(aposToQuotes("{'noNulls':null}"),
                NullSkipField.class);
        assertEquals("b", result.noNulls);
        assertEquals("a", result.nullsOk);
    }

// com.fasterxml.jackson.databind.deser.filter.NullConversionsSkipTest::testSkipNullMethod
    public void testSkipNullMethod() throws Exception
    {
        NullSkipMethod result = MAPPER.readValue(aposToQuotes("{'noNulls':'foo', 'nullsOk':null}"),
                NullSkipMethod.class);
        assertEquals("foo", result._noNulls);
        assertNull(result._nullsOk);

        result = MAPPER.readValue(aposToQuotes("{'noNulls':null}"),
                NullSkipMethod.class);
        assertEquals("b", result._noNulls);
        assertEquals("a", result._nullsOk);
    }

// com.fasterxml.jackson.databind.deser.filter.NullConversionsSkipTest::testEnumAsNullThenSkip
    public void testEnumAsNullThenSkip() throws Exception
    {    
        Pojo2015 p = MAPPER.readerFor(Pojo2015.class)
                .with(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_AS_NULL)
                .readValue("{\"number\":\"THREE\"}"); 
        assertEquals(NUMS2015.TWO, p.number);
    }

// com.fasterxml.jackson.databind.deser.filter.NullConversionsSkipTest::testSkipNullWithDefaults
    public void testSkipNullWithDefaults() throws Exception
    {
        String json = aposToQuotes("{'value':null}");
        StringValue result = MAPPER.readValue(json, StringValue.class);
        assertNull(result.value);

        ObjectMapper mapper = newObjectMapper();
        mapper.configOverride(String.class)
            .setSetterInfo(JsonSetter.Value.forValueNulls(Nulls.SKIP));
        result = mapper.readValue(json, StringValue.class);
        assertEquals("default", result.value);
    }

// com.fasterxml.jackson.databind.deser.filter.ProblemHandlerLocation1440Test::testIncorrectContext
    public void testIncorrectContext() throws Exception
    {
        
        final String invalidInput = aposToQuotes(
"{'actor': {'id': 'actor_id','type': 'actor_type',"
+"'status': 'actor_status','context':'actor_context','invalid_1': 'actor_invalid_1'},"
+"'verb': 'verb','object': {'id': 'object_id','type': 'object_type',"
+"'invalid_2': 'object_invalid_2','status': 'object_status','context': 'object_context'},"
+"'target': {'id': 'target_id','type': 'target_type','invalid_3': 'target_invalid_3',"
+"'invalid_4': 'target_invalid_4','status': 'target_status','context': 'target_context'}}"
);

        ObjectMapper mapper = newObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        final DeserializationProblemLogger logger = new DeserializationProblemLogger();
        mapper.addHandler(logger);
        mapper.readValue(invalidInput, Activity.class);

        List<String> probs = logger.problems();
        assertEquals(4, probs.size());
        assertEquals("actor.invalid_1#invalid_1", probs.get(0));
        assertEquals("object.invalid_2#invalid_2", probs.get(1));
        assertEquals("target.invalid_3#invalid_3", probs.get(2));
        assertEquals("target.invalid_4#invalid_4", probs.get(3));
    }

// com.fasterxml.jackson.databind.deser.filter.ProblemHandlerTest::testWeirdKeyHandling
    public void testWeirdKeyHandling() throws Exception
    {
        ObjectMapper mapper = newObjectMapper()
            .addHandler(new WeirdKeyHandler(7));
        IntKeyMapWrapper w = mapper.readValue("{\"stuff\":{\"foo\":\"abc\"}}",
                IntKeyMapWrapper.class);
        Map<Integer,String> map = w.stuff;
        assertEquals(1, map.size());
        assertEquals("abc", map.values().iterator().next());
        assertEquals(Integer.valueOf(7), map.keySet().iterator().next());
    }

// com.fasterxml.jackson.databind.deser.filter.ProblemHandlerTest::testWeirdNumberHandling
    public void testWeirdNumberHandling() throws Exception
    {
        ObjectMapper mapper = newObjectMapper()
            .addHandler(new WeirdNumberHandler(SingleValuedEnum.A))
            ;
        SingleValuedEnum result = mapper.readValue("3", SingleValuedEnum.class);
        assertEquals(SingleValuedEnum.A, result);
    }

// com.fasterxml.jackson.databind.deser.filter.ProblemHandlerTest::testWeirdStringHandling
    public void testWeirdStringHandling() throws Exception
    {
        ObjectMapper mapper = newObjectMapper()
            .addHandler(new WeirdStringHandler(SingleValuedEnum.A))
            ;
        SingleValuedEnum result = mapper.readValue("\"B\"", SingleValuedEnum.class);
        assertEquals(SingleValuedEnum.A, result);

        
        mapper = new ObjectMapper()
                .addHandler(new WeirdStringHandler(null));
        UUID result2 = mapper.readValue(quote("not a uuid!"), UUID.class);
        assertNull(result2);
    }

// com.fasterxml.jackson.databind.deser.filter.ProblemHandlerTest::testInvalidTypeId
    public void testInvalidTypeId() throws Exception
    {
        ObjectMapper mapper = newObjectMapper()
            .addHandler(new UnknownTypeIdHandler(BaseImpl.class));
        BaseWrapper w = mapper.readValue("{\"value\":{\"type\":\"foo\",\"a\":4}}",
                BaseWrapper.class);
        assertNotNull(w);
        assertEquals(BaseImpl.class, w.value.getClass());
    }

// com.fasterxml.jackson.databind.deser.filter.ProblemHandlerTest::testInvalidClassAsId
    public void testInvalidClassAsId() throws Exception
    {
        ObjectMapper mapper = newObjectMapper()
            .addHandler(new UnknownTypeIdHandler(Base2Impl.class));
        Base2Wrapper w = mapper.readValue("{\"value\":{\"clazz\":\"com.fizz\",\"a\":4}}",
                Base2Wrapper.class);
        assertNotNull(w);
        assertEquals(Base2Impl.class, w.value.getClass());
    }

// com.fasterxml.jackson.databind.deser.filter.ProblemHandlerTest::testMissingTypeId
    public void testMissingTypeId() throws Exception
    {
        ObjectMapper mapper = newObjectMapper()
            .addHandler(new MissingTypeIdHandler(BaseImpl.class));
        BaseWrapper w = mapper.readValue("{\"value\":{\"a\":4}}",
                BaseWrapper.class);
        assertNotNull(w);
        assertEquals(BaseImpl.class, w.value.getClass());
    }

// com.fasterxml.jackson.databind.deser.filter.ProblemHandlerTest::testMissingClassAsId
    public void testMissingClassAsId() throws Exception
    {
        ObjectMapper mapper = newObjectMapper()
            .addHandler(new MissingTypeIdHandler(Base2Impl.class));
        Base2Wrapper w = mapper.readValue("{\"value\":{\"a\":4}}",
                Base2Wrapper.class);
        assertNotNull(w);
        assertEquals(Base2Impl.class, w.value.getClass());
    }

// com.fasterxml.jackson.databind.deser.filter.ProblemHandlerTest::testInvalidTypeIdFail
    public void testInvalidTypeIdFail() throws Exception
    {
        try {
            MAPPER.readValue("{\"value\":{\"type\":\"foo\",\"a\":4}}",
                BaseWrapper.class);
            fail("Should not pass");
        } catch (InvalidTypeIdException e) {
            verifyException(e, "Could not resolve type id 'foo'");
            assertEquals(Base.class, e.getBaseType().getRawClass());
            assertEquals("foo", e.getTypeId());
        }
    }

// com.fasterxml.jackson.databind.deser.filter.ProblemHandlerTest::testInstantiationExceptionHandling
    public void testInstantiationExceptionHandling() throws Exception
    {
        ObjectMapper mapper = newObjectMapper()
            .addHandler(new InstantiationProblemHandler(BustedCtor.INST));
        BustedCtor w = mapper.readValue("{ }",
                BustedCtor.class);
        assertNotNull(w);
    }

// com.fasterxml.jackson.databind.deser.filter.ProblemHandlerTest::testMissingInstantiatorHandling
    public void testMissingInstantiatorHandling() throws Exception
    {
        ObjectMapper mapper = newObjectMapper()
            .addHandler(new MissingInstantiationHandler(new NoDefaultCtor(13)))
            ;
        NoDefaultCtor w = mapper.readValue("{ \"x\" : true }", NoDefaultCtor.class);
        assertNotNull(w);
        assertEquals(13, w.value);
    }

// com.fasterxml.jackson.databind.deser.filter.ProblemHandlerTest::testUnexpectedTokenHandling
    public void testUnexpectedTokenHandling() throws Exception
    {
        ObjectMapper mapper = newObjectMapper()
            .addHandler(new WeirdTokenHandler(Integer.valueOf(13)))
        ;
        Integer v = mapper.readValue("true", Integer.class);
        assertEquals(Integer.valueOf(13), v);
    }

// com.fasterxml.jackson.databind.deser.filter.ReadOnlyDeser1890Test::testDeserializeAnnotationsOneField
   public void testDeserializeAnnotationsOneField() throws IOException {
       PersonAnnotations person = MAPPER.readValue("{\"testEnum\":\"\"}", PersonAnnotations.class);
       
       assertEquals(null, person.getTestEnum());
       assertNull(person.name);
   }

// com.fasterxml.jackson.databind.deser.filter.ReadOnlyDeser1890Test::testDeserializeAnnotationsTwoFields
   public void testDeserializeAnnotationsTwoFields() throws IOException {
       PersonAnnotations person = MAPPER.readValue("{\"testEnum\":\"\",\"name\":\"changyong\"}",
               PersonAnnotations.class);
       
       assertEquals(null, person.getTestEnum());
       assertEquals("changyong", person.name);
   }

// com.fasterxml.jackson.databind.deser.filter.ReadOnlyDeser1890Test::testDeserializeOneField
   public void testDeserializeOneField() throws IOException {
       Person person = MAPPER.readValue("{\"testEnum\":\"\"}", Person.class);
       assertEquals(TestEnum.DEFAULT, person.getTestEnum());
       assertNull(person.name);
   }

// com.fasterxml.jackson.databind.deser.filter.ReadOnlyDeser1890Test::testDeserializeTwoFields
   public void testDeserializeTwoFields() throws IOException {
       Person person = MAPPER.readValue("{\"testEnum\":\"\",\"name\":\"changyong\"}",
               Person.class);
       assertEquals(TestEnum.DEFAULT, person.getTestEnum());
       assertEquals("changyong", person.name);
   }

// com.fasterxml.jackson.databind.deser.filter.ReadOnlyDeser95Test::testReadOnlyProp
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

// com.fasterxml.jackson.databind.deser.filter.RecursiveIgnorePropertiesTest::testRecursiveForDeser
    public void testRecursiveForDeser() throws Exception
    {
        String st = aposToQuotes("{ 'name': 'admin',\n"
                + "    'person_z': { 'name': 'wyatt' }"
                + "}");
        Person result = MAPPER.readValue(st, Person.class);
        assertEquals("admin", result.name);
        assertNotNull(result.personZ);
        assertEquals("wyatt", result.personZ.name);
    }

// com.fasterxml.jackson.databind.deser.filter.RecursiveIgnorePropertiesTest::testRecursiveWithCollectionDeser
    public void testRecursiveWithCollectionDeser() throws Exception
    {
        String st = aposToQuotes("{ 'name': 'admin',\n"
                + "    'person_z': [ { 'name': 'Foor' }, { 'name' : 'Bar' } ]"
                + "}");
        Persons result = MAPPER.readValue(st, Persons.class);
        assertEquals("admin", result.name);
        assertNotNull(result.personZ);
        assertEquals(2, result.personZ.size());
    }

// com.fasterxml.jackson.databind.deser.filter.RecursiveIgnorePropertiesTest::testRecursiveForSer
    public void testRecursiveForSer() throws Exception
    {
        Person input = new Person();
        input.name = "Bob";
        Person p2 = new Person();
        p2.name = "Bill";
        input.personZ = p2;
        p2.personZ = input;

        String json = MAPPER.writeValueAsString(input);
        assertNotNull(json);
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

// com.fasterxml.jackson.databind.deser.inject.InvalidInjectionTest::testInvalidDup
    public void testInvalidDup() throws Exception
    {
        try {
            MAPPER.readValue("{}", BadBean1.class);
            fail("Should not pass");
        } catch (InvalidDefinitionException e) {
            verifyException(e, "Duplicate injectable value");
        }
        try {
            MAPPER.readValue("{}", BadBean2.class);
            fail("Should not pass");
        } catch (InvalidDefinitionException e) {
            verifyException(e, "Duplicate injectable value");
        }
    }

// com.fasterxml.jackson.databind.deser.inject.TestInjectables::testSimple
    public void testSimple() throws Exception
    {
        ObjectMapper mapper = newObjectMapper();
        mapper.setInjectableValues(new InjectableValues.Std()
            .addValue(String.class, "stuffValue")
            .addValue("myId", "xyz")
            .addValue(Long.TYPE, Long.valueOf(37))
            );
        InjectedBean bean = mapper.readValue("{\"value\":3}", InjectedBean.class);
        assertEquals(3, bean.value);
        assertEquals("stuffValue", bean.stuff);
        assertEquals("xyz", bean.otherStuff);
        assertEquals(37L, bean.third);
    }

// com.fasterxml.jackson.databind.deser.inject.TestInjectables::testWithCtors
    public void testWithCtors() throws Exception
    {
        CtorBean bean = MAPPER.readerFor(CtorBean.class)
            .with(new InjectableValues.Std()
                .addValue(String.class, "Bubba"))
            .readValue("{\"age\":55}");
        assertEquals(55, bean.age);
        assertEquals("Bubba", bean.name);
    }

// com.fasterxml.jackson.databind.deser.inject.TestInjectables::testTwoInjectablesViaCreator
    public void testTwoInjectablesViaCreator() throws Exception
    {
        CtorBean2 bean = MAPPER.readerFor(CtorBean2.class)
                .with(new InjectableValues.Std()
                    .addValue(String.class, "Bob")
                    .addValue("number", Integer.valueOf(13))
                ).readValue("{ }");
        assertEquals(Integer.valueOf(13), bean.age);
        assertEquals("Bob", bean.name);
    }

// com.fasterxml.jackson.databind.deser.inject.TestInjectables::testIssue471
    public void testIssue471() throws Exception
    {
        final Object constructorInjected = "constructorInjected";
        final Object methodInjected = "methodInjected";
        final Object fieldInjected = "fieldInjected";

        ObjectMapper mapper = newObjectMapper()
                        .setInjectableValues(new InjectableValues.Std()
                                .addValue("constructor_injected", constructorInjected)
                                .addValue("method_injected", methodInjected)
                                .addValue("field_injected", fieldInjected));

        Bean471 bean = mapper.readValue(aposToQuotes(
"{'x':13,'constructor_value':'constructor','method_value':'method','field_value':'field'}"),
                Bean471.class);

        
        assertSame(constructorInjected, bean.constructorInjected);
        assertSame(methodInjected, bean.methodInjected);
        assertSame(fieldInjected, bean.fieldInjected);

        
        assertEquals("constructor", bean.constructorValue);
        assertEquals("method", bean.methodValue);
        assertEquals("field", bean.fieldValue);

        assertEquals(13, bean.x);
    }

// com.fasterxml.jackson.databind.deser.inject.TestInjectables::testTransientField
    public void testTransientField() throws Exception
    {
        TransientBean bean = MAPPER.readerFor(TransientBean.class)
                .with(new InjectableValues.Std()
                        .addValue("transient", "Injected!"))
                .readValue("{\"value\":28}");
        assertEquals(28, bean.value);
        assertEquals("Injected!", bean.injected);
    }

// com.fasterxml.jackson.databind.deser.jdk.CollectionDeserTest::testUntypedList
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

// com.fasterxml.jackson.databind.deser.jdk.CollectionDeserTest::testExactStringCollection
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

// com.fasterxml.jackson.databind.deser.jdk.CollectionDeserTest::testHashSet
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

// com.fasterxml.jackson.databind.deser.jdk.CollectionDeserTest::testCustomDeserializer
    public void testCustomDeserializer() throws IOException
    {
        CustomList result = MAPPER.readValue(quote("abc"), CustomList.class);
        assertEquals(1, result.size());
        assertEquals("abc", result.get(0));
    }

// com.fasterxml.jackson.databind.deser.jdk.CollectionDeserTest::testImplicitArrays
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

// com.fasterxml.jackson.databind.deser.jdk.CollectionDeserTest::testFromEmptyString
    public void testFromEmptyString() throws Exception
    {
        ObjectReader r = MAPPER.reader(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT);
        List<?> result = r.forType(List.class).readValue(quote(""));
        assertNull(result);
    }

// com.fasterxml.jackson.databind.deser.jdk.CollectionDeserTest::testArrayBlockingQueue
    public void testArrayBlockingQueue() throws Exception
    {
        
        ArrayBlockingQueue<?> q = MAPPER.readValue("[1, 2, 3]", ArrayBlockingQueue.class);
        assertNotNull(q);
        assertEquals(3, q.size());
        assertEquals(Integer.valueOf(1), q.take());
        assertEquals(Integer.valueOf(2), q.take());
        assertEquals(Integer.valueOf(3), q.take());
    }

// com.fasterxml.jackson.databind.deser.jdk.CollectionDeserTest::testIterableWithStrings
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

// com.fasterxml.jackson.databind.deser.jdk.CollectionDeserTest::testIterableWithBeans
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

// com.fasterxml.jackson.databind.deser.jdk.CollectionDeserTest::testArrayIndexForExceptions
    public void testArrayIndexForExceptions() throws Exception
    {
        final String OBJECTS_JSON = "[ \"KEY2\", false ]";
        try {
            MAPPER.readValue(OBJECTS_JSON, Key[].class);
            fail("Should not pass");
        } catch (JsonMappingException e) {
            verifyException(e, "Cannot deserialize");
            List<JsonMappingException.Reference> refs = e.getPath();
            assertEquals(1, refs.size());
            assertEquals(1, refs.get(0).getIndex());
        }

        try {
            MAPPER.readValue("[ \"xyz\", { } ]", String[].class);
            fail("Should not pass");
        } catch (JsonMappingException e) {
            verifyException(e, "Cannot deserialize");
            List<JsonMappingException.Reference> refs = e.getPath();
            assertEquals(1, refs.size());
            assertEquals(1, refs.get(0).getIndex());
        }

        try {
            MAPPER.readValue("{\"keys\":"+OBJECTS_JSON+"}", KeyListBean.class);
            fail("Should not pass");
        } catch (JsonMappingException e) {
            verifyException(e, "Cannot deserialize");
            List<JsonMappingException.Reference> refs = e.getPath();
            assertEquals(2, refs.size());
            
            assertEquals(-1, refs.get(0).getIndex());
            assertEquals("keys", refs.get(0).getFieldName());

            
            assertEquals(1, refs.get(1).getIndex());
            assertNull(refs.get(1).getFieldName());
        }
    }

// com.fasterxml.jackson.databind.deser.jdk.CollectionDeserTest::testWrapExceptions
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

// com.fasterxml.jackson.databind.deser.jdk.DateDeserializationTZTest::testDateUtilISO8601_Timezone
    public void testDateUtilISO8601_Timezone() throws Exception
    {
        
        verify( MAPPER, "2000-01-02T03:04:05.678+01:00", judate(2000, 1, 2,   3, 4, 5, 678, "GMT+1"));
        
        verify( MAPPER, "2000-01-02T03:04:05.678+0100",  judate(2000, 1, 2,   3, 4, 5, 678, "GMT+1"));
        
        verify( MAPPER, "2000-01-02T03:04:05.678+01",    judate(2000, 1, 2,   3, 4, 5, 678, "GMT+1"));

        
        verify( MAPPER, "2000-01-02T03:04:05.678Z",      judate(2000, 1, 2,   3, 4, 5, 678, "UTC"));

        
        
        
        
        
        
        

        failure( MAPPER, "2000-01-02T03:04:05.678+"); 
        failure( MAPPER, "2000-01-02T03:04:05.678+1");

        failure( MAPPER, "2000-01-02T03:04:05.678+001"); 
        failure( MAPPER, "2000-01-02T03:04:05.678+00:");
        failure( MAPPER, "2000-01-02T03:04:05.678+00:001");
        failure( MAPPER, "2000-01-02T03:04:05.678+001:001");

        failure( MAPPER, "2000-01-02T03:04:05.678+1:");
        failure( MAPPER, "2000-01-02T03:04:05.678+00:1");
    }

// com.fasterxml.jackson.databind.deser.jdk.DateDeserializationTZTest::testDateUtilISO8601_DateTimeMillis
    public void testDateUtilISO8601_DateTimeMillis() throws Exception 
    {    
        
    		failure(MAPPER, "2000-01-02T03:04:05.0123456789+01:00");	
        verify( MAPPER, "2000-01-02T03:04:05.6789+01:00", judate(2000, 1, 2,   3, 4, 5, 678, "GMT+1"));
        verify( MAPPER, "2000-01-02T03:04:05.678+01:00",  judate(2000, 1, 2,   3, 4, 5, 678, "GMT+1"));
        verify( MAPPER, "2000-01-02T03:04:05.67+01:00",   judate(2000, 1, 2,   3, 4, 5, 670, "GMT+1"));
        verify( MAPPER, "2000-01-02T03:04:05.6+01:00",    judate(2000, 1, 2,   3, 4, 5, 600, "GMT+1"));
        verify( MAPPER, "2000-01-02T03:04:05+01:00",      judate(2000, 1, 2,   3, 4, 5, 000, "GMT+1"));

        
		failure(MAPPER, "2000-01-02T03:04:05.0123456789Z");	
        verify( MAPPER, "2000-01-02T03:04:05.6789Z",      judate(2000, 1, 2,   3, 4, 5, 678, "UTC"));
        verify( MAPPER, "2000-01-02T03:04:05.678Z",       judate(2000, 1, 2,   3, 4, 5, 678, "UTC"));
        verify( MAPPER, "2000-01-02T03:04:05.67Z",        judate(2000, 1, 2,   3, 4, 5, 670, "UTC"));
        verify( MAPPER, "2000-01-02T03:04:05.6Z",         judate(2000, 1, 2,   3, 4, 5, 600, "UTC"));
        verify( MAPPER, "2000-01-02T03:04:05Z",           judate(2000, 1, 2,   3, 4, 5,   0, "UTC"));
        

        
		failure(MAPPER, "2000-01-02T03:04:05.0123456789");	
        verify( MAPPER, "2000-01-02T03:04:05.6789",       judate(2000, 1, 2,   3, 4,  5, 678, LOCAL_TZ));
        verify( MAPPER, "2000-01-02T03:04:05.678",        judate(2000, 1, 2,   3, 4,  5, 678, LOCAL_TZ));
        verify( MAPPER, "2000-01-02T03:04:05.67",         judate(2000, 1, 2,   3, 4,  5, 670, LOCAL_TZ));
        verify( MAPPER, "2000-01-02T03:04:05.6",          judate(2000, 1, 2,   3, 4,  5, 600, LOCAL_TZ));
        verify( MAPPER, "2000-01-02T03:04:05",            judate(2000, 1, 2,   3, 4,  5, 000, LOCAL_TZ));
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        failure( MAPPER, "2000-01-02T03:04:05.+01:00");
        failure( MAPPER, "2000-01-02T03:04:05.");
        failure( MAPPER, "2000-01-02T03:04:05.Z");
    }

// com.fasterxml.jackson.databind.deser.jdk.DateDeserializationTZTest::testDateUtilISO8601_DateTime
    public void testDateUtilISO8601_DateTime() throws Exception 
    {
        
        verify(MAPPER, "2000-01-02T03:04:05+01:00",  judate(2000, 1, 2,   3, 4, 5, 0, "GMT+1"));

        
        verify(MAPPER, "2000-01-02T03:04:05",        judate(2000, 1, 2,   3, 4, 5, 0, LOCAL_TZ));

        
        failure(MAPPER, "2000-01-02T");
        failure(MAPPER, "2000-01-02T03");
        failure(MAPPER, "2000-01-02T03:");
        verify(MAPPER, "2000-01-02T03:04", judate(2000, 1, 2,  3, 4, 0, 0, LOCAL_TZ));
        failure(MAPPER, "2000-01-02T03:04:");
        
        
        failure(MAPPER, "2000-01-02T+01:00");
        failure(MAPPER, "2000-01-02T03+01:00");
        failure(MAPPER, "2000-01-02T03:+01:00");
        verify( MAPPER, "2000-01-02T03:04+01:00", judate(2000, 1, 2,   3, 4, 0, 0, "GMT+1"));
        failure(MAPPER, "2000-01-02T03:04:+01:00");
        
        failure(MAPPER, "2000-01-02TZ");
        failure(MAPPER, "2000-01-02T03Z");
        failure(MAPPER, "2000-01-02T03:Z");
        verify(MAPPER, "2000-01-02T03:04Z", judate(2000, 1, 2,  3, 4, 0, 0, "UTC"));
        failure(MAPPER, "2000-01-02T03:04:Z");

        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        failure( MAPPER, "2000-01-02T03:04:5");
        failure( MAPPER, "2000-01-02T03:04:5.000");
        failure(MAPPER, "2000-01-02T03:04:005");
        
        
        failure(MAPPER, "2000-01-02T03:04:5+01:00");
        failure(MAPPER, "2000-01-02T03:04:5.000+01:00");
        failure(MAPPER, "2000-01-02T03:04:005+01:00");
        
        
        failure(MAPPER, "2000-01-02T03:04:5Z");
        failure( MAPPER, "2000-01-02T03:04:5.000Z");
        failure(MAPPER, "2000-01-02T03:04:005Z");
        

        
        failure( MAPPER, "2000-01-02T03:4:05");
        failure( MAPPER, "2000-01-02T03:4:05.000");
        failure(MAPPER, "2000-01-02T03:004:05");
        
        
        failure(MAPPER, "2000-01-02T03:4:05+01:00");
        failure(MAPPER, "2000-01-02T03:4:05.000+01:00");
        failure(MAPPER, "2000-01-02T03:004:05+01:00");
        
        
        failure( MAPPER, "2000-01-02T03:4:05Z");
        failure( MAPPER, "2000-01-02T03:4:05.000Z");
        failure( MAPPER, "2000-01-02T03:004:05Z");

        
        failure( MAPPER, "2000-01-02T3:04:05");
        failure( MAPPER, "2000-01-02T3:04:05.000");
        failure(MAPPER, "2000-01-02T003:04:05");

        
        failure(MAPPER, "2000-01-02T3:04:05+01:00");
        failure(MAPPER, "2000-01-02T3:04:05.000+01:00");
        failure(MAPPER, "2000-01-02T003:04:05+01:00");

        
        failure( MAPPER, "2000-01-02T3:04:05Z");
        failure( MAPPER, "2000-01-02T3:04:05.000Z");
        failure( MAPPER, "2000-01-02T003:04:05Z");
    }

// com.fasterxml.jackson.databind.deser.jdk.DateDeserializationTZTest::testDateUtilISO8601_Date
    public void testDateUtilISO8601_Date() throws Exception
    {
        
        verify(MAPPER, "2000-01-02", judate(2000, 1, 2,   0, 0, 0, 0, LOCAL_TZ));
        
        
        
        
        
        
        
        
        
        
        
        

        
        failure( MAPPER, "2000-01-2"); 
        failure( MAPPER, "2000-01-002");
        
        
        failure( MAPPER, "2000-1-02");
        failure( MAPPER, "2000-001-02");
        
        
        failure( MAPPER, "20000-01-02");
        failure( MAPPER, "200-01-02"  );
        failure( MAPPER, "20-01-02"   );
        failure(  MAPPER, "2-01-02");
    }

// com.fasterxml.jackson.databind.deser.jdk.DateDeserializationTZTest::testDateUtil_Numeric
    public void testDateUtil_Numeric() throws Exception
    {
        {
            long now = 123456789L;
            verify( MAPPER,                now, new java.util.Date(now) ); 
            verify( MAPPER, Long.toString(now), new java.util.Date(now) ); 
        }
        {
            
            
            long now = 1321992375446L;
            verify( MAPPER,                now, new java.util.Date(now) );	
            verify( MAPPER, Long.toString(now), new java.util.Date(now) );  
        }
        {
            
            long now = - (24 * 3600 * 1000L);
            verify( MAPPER,                now, new java.util.Date(now) );	
            verify( MAPPER, Long.toString(now), new java.util.Date(now) );  
        }
    	
        
        BigInteger tooLarge = BigInteger.valueOf(Long.MAX_VALUE).add( BigInteger.valueOf(1) );
        failure(MAPPER, tooLarge, InvalidFormatException.class);
        failure(MAPPER, tooLarge.toString(), InvalidFormatException.class);
    	
        
        failure(MAPPER, 0.0, MismatchedInputException.class);
        failure(MAPPER, "0.0", InvalidFormatException.class);
    }

// com.fasterxml.jackson.databind.deser.jdk.DateDeserializationTZTest::testDateUtil_Annotation
    public void testDateUtil_Annotation() throws Exception
    {
        
        String json = aposToQuotes("{'date':'/2005/05/25/'}");
        java.util.Date expected = judate(2005, 05, 25, 0, 0, 0, 0, LOCAL_TZ);
        
        
        
        {
            DateAsStringBean result = MAPPER.readValue(json, DateAsStringBean.class);
            assertNotNull(result);
            assertEquals( expected, result.date );
        }
        {
            DateAsStringBean result = MAPPER.readerFor(DateAsStringBean.class)
                    .with(Locale.GERMANY)
                    .readValue(json);
            assertNotNull(result);
            assertEquals( expected, result.date );
        }
        
        
        {
            DateAsStringBeanGermany result = MAPPER.readerFor(DateAsStringBeanGermany.class)
                                                   .readValue(json);
            assertNotNull(result);
            assertEquals( expected, result.date );
        }
    }

// com.fasterxml.jackson.databind.deser.jdk.DateDeserializationTZTest::testDateUtil_Annotation_PatternAndLocale
    public void testDateUtil_Annotation_PatternAndLocale() throws Exception
    {
        
        
        ObjectMapper mapper = MAPPER.copy();
        mapper.setLocale( Locale.ITALY );

        
        
        String json = aposToQuotes("{ 'pattern': '*1 giu 2000 01:02:03*', 'pattern_FR': '*01 juin 2000 01:02:03*', 'pattern_GMT4': '*1 giu 2000 01:02:03*', 'pattern_FR_GMT4': '*1 juin 2000 01:02:03*'}");
        Annot_Pattern result = mapper.readValue(json, Annot_Pattern.class);

        assertNotNull(result);
        assertEquals( judate(2000, 6, 1, 1, 2, 3, 0, LOCAL_TZ), result.pattern        );
        assertEquals( judate(2000, 6, 1, 1, 2, 3, 0, LOCAL_TZ), result.pattern_FR     );
        assertEquals( judate(2000, 6, 1, 1, 2, 3, 0, "GMT+4"),  result.pattern_GMT4    );
        assertEquals( judate(2000, 6, 1, 1, 2, 3, 0, "GMT+4"),  result.pattern_FR_GMT4 );
    }

// com.fasterxml.jackson.databind.deser.jdk.DateDeserializationTZTest::testDateUtil_Annotation_TimeZone
    public void testDateUtil_Annotation_TimeZone() throws Exception
    {
        
        {
            String json = aposToQuotes("{ 'date': '2000-01-02T03:04:05.678' }");
            Annot_TimeZone result = MAPPER.readValue(json, Annot_TimeZone.class);
            
            assertNotNull(result);
            assertEquals( judate(2000, 1, 2, 3, 4, 5, 678, "GMT+4"), result.date);
        }
        
        
        
        
        {
            String json = aposToQuotes("{ 'date': '2000-01-02T03:04:05.678+01:00' }");
            Annot_TimeZone result = MAPPER.readValue(json, Annot_TimeZone.class);
            
            assertNotNull(result);
            assertEquals( judate(2000, 1, 2, 3, 4, 5, 678, "GMT+1"), result.date);
        }
    }

// com.fasterxml.jackson.databind.deser.jdk.DateDeserializationTZTest::testDateUtil_customDateFormat_withoutTZ
    public void testDateUtil_customDateFormat_withoutTZ() throws Exception
    {
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        {
            DateFormat df = new SimpleDateFormat("yyyy-MM-dd'X'HH:mm:ss");
            df.setTimeZone( TimeZone.getTimeZone("GMT+4") );    
            
            ObjectMapper mapper = new ObjectMapper();
            mapper.setTimeZone( TimeZone.getTimeZone(LOCAL_TZ) );
            mapper.setDateFormat(df);
            
            
            verify(mapper, "2000-01-02X04:00:00", judate(2000, 1, 2, 4, 00, 00, 00, LOCAL_TZ));
        }
        
        
        
        
        
        {
            DateFormat df = new SimpleDateFormat("yyyy-MM-dd'X'HH:mm:ss");
            df.setTimeZone( TimeZone.getTimeZone("GMT+4") );    
            
            ObjectMapper mapper = new ObjectMapper();
            mapper.setDateFormat(df);
            
            
            verify(mapper, "2000-01-02X04:00:00", judate(2000, 1, 2, 4, 00, 00, 00, "GMT+4"));
        }
    }

// com.fasterxml.jackson.databind.deser.jdk.DateDeserializationTZTest::testDateUtil_customDateFormat_withTZ
    public void testDateUtil_customDateFormat_withTZ() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd'X'HH:mm:ssZ");
        df.setTimeZone(TimeZone.getTimeZone("GMT+4"));    
        mapper.setDateFormat(df);

        verify(mapper, "2000-01-02X03:04:05+0300", judate(2000, 1, 2, 3, 4, 5, 00, "GMT+3"));
    }

// com.fasterxml.jackson.databind.deser.jdk.DateDeserializationTest::testDateUtil
    public void testDateUtil() throws Exception
    {
        long now = 123456789L;
        java.util.Date value = new java.util.Date(now);

        
        assertEquals(value, MAPPER.readValue(""+now, java.util.Date.class));

        
        String dateStr = dateToString(value);
        java.util.Date result = MAPPER.readValue("\""+dateStr+"\"", java.util.Date.class);

        assertEquals("Date: expect "+value+" ("+value.getTime()+"), got "+result+" ("+result.getTime()+")",
                value.getTime(), result.getTime());
    }

// com.fasterxml.jackson.databind.deser.jdk.DateDeserializationTest::testDateUtilWithStringTimestamp
    public void testDateUtilWithStringTimestamp() throws Exception
    {
        long now = 1321992375446L;
        
        String json = quote(String.valueOf(now));
        java.util.Date value = MAPPER.readValue(json, java.util.Date.class);
        assertEquals(now, value.getTime());

        
        long before = - (24 * 3600 * 1000L);
        json = quote(String.valueOf(before));
        value = MAPPER.readValue(json, java.util.Date.class);
        assertEquals(before, value.getTime());
    }

// com.fasterxml.jackson.databind.deser.jdk.DateDeserializationTest::testDateUtilRFC1123
    public void testDateUtilRFC1123() throws Exception
    {
        DateFormat fmt = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz", Locale.US);
        
        String inputStr = "Sat, 17 Jan 2009 06:13:58 +0000";
        java.util.Date inputDate = fmt.parse(inputStr);
        assertEquals(inputDate, MAPPER.readValue("\""+inputStr+"\"", java.util.Date.class));
    }

// com.fasterxml.jackson.databind.deser.jdk.DateDeserializationTest::testDateUtilRFC1123OnNonUSLocales
    public void testDateUtilRFC1123OnNonUSLocales() throws Exception
    {
        Locale old = Locale.getDefault();
        Locale.setDefault(Locale.GERMAN);
        DateFormat fmt = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz", Locale.US);
        
        String inputStr = "Sat, 17 Jan 2009 06:13:58 +0000";
        java.util.Date inputDate = fmt.parse(inputStr);
        assertEquals(inputDate, MAPPER.readValue("\""+inputStr+"\"", java.util.Date.class));
        Locale.setDefault(old);
    }

// com.fasterxml.jackson.databind.deser.jdk.DateDeserializationTest::testDateUtilISO8601
    public void testDateUtilISO8601() throws Exception
    {
        
        String inputStr = "1972-12-28T00:00:00.000+0000";
        Date inputDate = MAPPER.readValue("\""+inputStr+"\"", java.util.Date.class);
        Calendar c = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
        c.setTime(inputDate);
        assertEquals(1972, c.get(Calendar.YEAR));
        assertEquals(Calendar.DECEMBER, c.get(Calendar.MONTH));
        assertEquals(28, c.get(Calendar.DAY_OF_MONTH));

        
        inputStr = "1972-12-28T00:00:00.000Z";
        inputDate = MAPPER.readValue(quote(inputStr), java.util.Date.class);
        c.setTime(inputDate);
        assertEquals(1972, c.get(Calendar.YEAR));
        assertEquals(Calendar.DECEMBER, c.get(Calendar.MONTH));
        assertEquals(28, c.get(Calendar.DAY_OF_MONTH));

        
        inputStr = "1972-12-28T00:00:00.000+00:00";
        inputDate = MAPPER.readValue(quote(inputStr), java.util.Date.class);
        c.setTime(inputDate);
        assertEquals(1972, c.get(Calendar.YEAR));
        assertEquals(Calendar.DECEMBER, c.get(Calendar.MONTH));
        assertEquals(28, c.get(Calendar.DAY_OF_MONTH));

        
        inputStr = "1972-12-28T00:00:00.000+00";
        inputDate = MAPPER.readValue(quote(inputStr), java.util.Date.class);
        c.setTime(inputDate);
        assertEquals(1972, c.get(Calendar.YEAR));
        assertEquals(Calendar.DECEMBER, c.get(Calendar.MONTH));
        assertEquals(28, c.get(Calendar.DAY_OF_MONTH));

        inputStr = "1984-11-30T00:00:00.000Z";
        inputDate = MAPPER.readValue(quote(inputStr), java.util.Date.class);
        c.setTime(inputDate);
        assertEquals(1984, c.get(Calendar.YEAR));
        assertEquals(Calendar.NOVEMBER, c.get(Calendar.MONTH));
        assertEquals(30, c.get(Calendar.DAY_OF_MONTH));
    }

// com.fasterxml.jackson.databind.deser.jdk.DateDeserializationTest::testISO8601PartialMilliseconds
    public void testISO8601PartialMilliseconds() throws Exception
    {
        String inputStr;
        Date inputDate;
        Calendar c = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
        
        inputStr = "2014-10-03T18:00:00.6-05:00";
        inputDate = MAPPER.readValue(quote(inputStr), java.util.Date.class);
        c.setTime(inputDate);
        assertEquals(2014, c.get(Calendar.YEAR));
        assertEquals(Calendar.OCTOBER, c.get(Calendar.MONTH));
        assertEquals(3, c.get(Calendar.DAY_OF_MONTH));
        assertEquals(600, c.get(Calendar.MILLISECOND));

        inputStr = "2014-10-03T18:00:00.61-05:00";
        inputDate = MAPPER.readValue(quote(inputStr), java.util.Date.class);
        c.setTime(inputDate);
        assertEquals(2014, c.get(Calendar.YEAR));
        assertEquals(Calendar.OCTOBER, c.get(Calendar.MONTH));
        assertEquals(3, c.get(Calendar.DAY_OF_MONTH));
        assertEquals(18 + 5, c.get(Calendar.HOUR_OF_DAY));
        assertEquals(0, c.get(Calendar.MINUTE));
        assertEquals(0, c.get(Calendar.SECOND));
        assertEquals(610, c.get(Calendar.MILLISECOND));

        inputStr = "1997-07-16T19:20:30.45+01:00";
        inputDate = MAPPER.readValue(quote(inputStr), java.util.Date.class);
        c.setTime(inputDate);
        assertEquals(1997, c.get(Calendar.YEAR));
        assertEquals(Calendar.JULY, c.get(Calendar.MONTH));
        assertEquals(16, c.get(Calendar.DAY_OF_MONTH));
        assertEquals(19 - 1, c.get(Calendar.HOUR_OF_DAY));
        assertEquals(20, c.get(Calendar.MINUTE));
        assertEquals(30, c.get(Calendar.SECOND));
        assertEquals(450, c.get(Calendar.MILLISECOND));

        
        inputStr = "1997-07-16T19:20:30.45+0100";
        inputDate = MAPPER.readValue(quote(inputStr), java.util.Date.class);
        c.setTime(inputDate);
        assertEquals(1997, c.get(Calendar.YEAR));
        assertEquals(Calendar.JULY, c.get(Calendar.MONTH));
        assertEquals(16, c.get(Calendar.DAY_OF_MONTH));
        assertEquals(19 - 1, c.get(Calendar.HOUR_OF_DAY));
        assertEquals(20, c.get(Calendar.MINUTE));
        assertEquals(30, c.get(Calendar.SECOND));
        assertEquals(450, c.get(Calendar.MILLISECOND));

        
        inputStr = "1997-07-16T19:20:30.45+01";
        inputDate = MAPPER.readValue(quote(inputStr), java.util.Date.class);
        c.setTime(inputDate);
        assertEquals(1997, c.get(Calendar.YEAR));
        assertEquals(Calendar.JULY, c.get(Calendar.MONTH));
        assertEquals(16, c.get(Calendar.DAY_OF_MONTH));
        assertEquals(19 - 1, c.get(Calendar.HOUR_OF_DAY));
        assertEquals(20, c.get(Calendar.MINUTE));
        assertEquals(30, c.get(Calendar.SECOND));
        assertEquals(450, c.get(Calendar.MILLISECOND));
    }

// com.fasterxml.jackson.databind.deser.jdk.DateDeserializationTest::testISO8601FractionalTimezoneOffset
    public void testISO8601FractionalTimezoneOffset() throws Exception
    {
        String inputStr = "1997-07-16T19:20:30.45+01:30";
        java.util.Date inputDate = MAPPER.readValue(quote(inputStr), java.util.Date.class);
        Calendar c = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
        c.setTime(inputDate);
        assertEquals(1997, c.get(Calendar.YEAR));
        assertEquals(Calendar.JULY, c.get(Calendar.MONTH));
        assertEquals(16, c.get(Calendar.DAY_OF_MONTH));
        assertEquals(19 - 2, c.get(Calendar.HOUR_OF_DAY));
        assertEquals(50, c.get(Calendar.MINUTE));
        assertEquals(30, c.get(Calendar.SECOND));
        assertEquals(450, c.get(Calendar.MILLISECOND));
    }

// com.fasterxml.jackson.databind.deser.jdk.DateDeserializationTest::testISO8601FractSecondsLong
    public void testISO8601FractSecondsLong() throws Exception
    {
        String inputStr;
        Date inputDate;
        Calendar c = Calendar.getInstance(TimeZone.getTimeZone("UTC"));

        inputStr = "2014-10-03T18:00:00.3456-05:00";
        inputDate = MAPPER.readValue(quote(inputStr), java.util.Date.class);
        c.setTime(inputDate);
        assertEquals(2014, c.get(Calendar.YEAR));
        assertEquals(Calendar.OCTOBER, c.get(Calendar.MONTH));
        assertEquals(3, c.get(Calendar.DAY_OF_MONTH));
        
        assertEquals(345, c.get(Calendar.MILLISECOND));

        
        try {
            MAPPER.readValue(quote("2014-10-03T18:00:00.1234567890-05:00"), java.util.Date.class);
        } catch (InvalidFormatException e) {
            verifyException(e, "invalid fractional seconds");
            verifyException(e, "can use at most 9 digits");
        }
    }

// com.fasterxml.jackson.databind.deser.jdk.DateDeserializationTest::testISO8601MissingSeconds
    public void testISO8601MissingSeconds() throws Exception
    {
        String inputStr;
        Date inputDate;

        
        Calendar c = Calendar.getInstance(TimeZone.getTimeZone("GMT"));

        inputStr = "1997-07-16T19:20+01:00";
        inputDate = MAPPER.readValue(quote(inputStr), java.util.Date.class);
        c.setTime(inputDate);
        assertEquals(1997, c.get(Calendar.YEAR));
        assertEquals(Calendar.JULY, c.get(Calendar.MONTH));
        assertEquals(16, c.get(Calendar.DAY_OF_MONTH));
        assertEquals(19 - 1, c.get(Calendar.HOUR_OF_DAY));
        assertEquals(0, c.get(Calendar.SECOND));
        assertEquals(0, c.get(Calendar.MILLISECOND));

        
        inputStr = "1997-07-16T19:20+0200";
        inputDate = MAPPER.readValue(quote(inputStr), java.util.Date.class);
        c.setTime(inputDate);
        assertEquals(1997, c.get(Calendar.YEAR));
        assertEquals(Calendar.JULY, c.get(Calendar.MONTH));
        assertEquals(16, c.get(Calendar.DAY_OF_MONTH));
        assertEquals(19 - 2, c.get(Calendar.HOUR_OF_DAY));
        assertEquals(0, c.get(Calendar.SECOND));
        assertEquals(0, c.get(Calendar.MILLISECOND));

        
        inputStr = "1997-07-16T19:20+04";
        inputDate = MAPPER.readValue(quote(inputStr), java.util.Date.class);
        c.setTime(inputDate);
        assertEquals(1997, c.get(Calendar.YEAR));
        assertEquals(Calendar.JULY, c.get(Calendar.MONTH));
        assertEquals(16, c.get(Calendar.DAY_OF_MONTH));
        assertEquals(19 - 4, c.get(Calendar.HOUR_OF_DAY));
        assertEquals(0, c.get(Calendar.SECOND));
        assertEquals(0, c.get(Calendar.MILLISECOND));
    }

// com.fasterxml.jackson.databind.deser.jdk.DateDeserializationTest::testDateUtilISO8601NoTimezone
    public void testDateUtilISO8601NoTimezone() throws Exception
    {
        
        String inputStr = "1984-11-13T00:00:09";
        Date inputDate = MAPPER.readValue(quote(inputStr), java.util.Date.class);
        Calendar c = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
        c.setTime(inputDate);
        assertEquals(1984, c.get(Calendar.YEAR));
        assertEquals(Calendar.NOVEMBER, c.get(Calendar.MONTH));
        assertEquals(13, c.get(Calendar.DAY_OF_MONTH));
        assertEquals(0, c.get(Calendar.HOUR_OF_DAY));
        assertEquals(0, c.get(Calendar.MINUTE));
        assertEquals(9, c.get(Calendar.SECOND));
        assertEquals(0, c.get(Calendar.MILLISECOND));
    }

// com.fasterxml.jackson.databind.deser.jdk.DateDeserializationTest::testDateUtilISO8601NoTimezoneNonDefault
    public void testDateUtilISO8601NoTimezoneNonDefault() throws Exception
    {
        
        ObjectReader r = MAPPER.readerFor(Date.class);
        TimeZone tz = TimeZone.getTimeZone("GMT-2");
        Date date1 = r.with(tz)
                .readValue(quote("1970-01-01T00:00:00.000"));
        
        Date date2 = r.with(TimeZone.getTimeZone("GMT+5"))
                .readValue(quote("1970-01-01T00:00:00.000-02:00"));
        assertEquals(date1, date2);

        
        Calendar c = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
        c.setTime(date1);
        assertEquals(1970, c.get(Calendar.YEAR));
        assertEquals(Calendar.JANUARY, c.get(Calendar.MONTH));
        assertEquals(1, c.get(Calendar.DAY_OF_MONTH));
        assertEquals(2, c.get(Calendar.HOUR_OF_DAY));
    }

// com.fasterxml.jackson.databind.deser.jdk.DateDeserializationTest::testFormatAndCtors1722
    public void testFormatAndCtors1722() throws Exception
    {
        Date1722 input = new Date1722(new Date(0L), "bogus");
        String json = MAPPER.writeValueAsString(input);
        Date1722 result = MAPPER.readValue(json, Date1722.class);
        assertNotNull(result);
    }

// com.fasterxml.jackson.databind.deser.jdk.DateDeserializationTest::testDateUtilISO8601NoMilliseconds
    public void testDateUtilISO8601NoMilliseconds() throws Exception
    {
        final String INPUT_STR = "2013-10-31T17:27:00";
        Date inputDate;
        Calendar c;
        
        inputDate = MAPPER.readValue(quote(INPUT_STR), java.util.Date.class);
        c = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
        c.setTime(inputDate);
        assertEquals(2013, c.get(Calendar.YEAR));
        assertEquals(Calendar.OCTOBER, c.get(Calendar.MONTH));
        assertEquals(31, c.get(Calendar.DAY_OF_MONTH));
        assertEquals(17, c.get(Calendar.HOUR_OF_DAY));
        assertEquals(27, c.get(Calendar.MINUTE));
        assertEquals(0, c.get(Calendar.SECOND));
        assertEquals(0, c.get(Calendar.MILLISECOND));

        
        
        
    }

// com.fasterxml.jackson.databind.deser.jdk.DateDeserializationTest::testDateUtilISO8601JustDate
    public void testDateUtilISO8601JustDate() throws Exception
    {
        
        String inputStr = "1972-12-28";
        Date inputDate = MAPPER.readValue(quote(inputStr), java.util.Date.class);
        Calendar c = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
        c.setTime(inputDate);
        assertEquals(1972, c.get(Calendar.YEAR));
        assertEquals(Calendar.DECEMBER, c.get(Calendar.MONTH));
        assertEquals(28, c.get(Calendar.DAY_OF_MONTH));
    }

// com.fasterxml.jackson.databind.deser.jdk.DateDeserializationTest::testDateSql
    public void testDateSql() throws Exception
    {
        java.sql.Date value = new java.sql.Date(0L);
        value.setYear(99); 
        value.setDate(19);
        value.setMonth(Calendar.APRIL);
        long now = value.getTime();

        
        assertEquals(value, MAPPER.readValue(String.valueOf(now), java.sql.Date.class));

        
        
        java.sql.Date result = MAPPER.readValue(quote(value.toString()), java.sql.Date.class);
        Calendar c = gmtCalendar(result.getTime());
        assertEquals(1999, c.get(Calendar.YEAR));
        assertEquals(Calendar.APRIL, c.get(Calendar.MONTH));
        assertEquals(19, c.get(Calendar.DAY_OF_MONTH));

        
        String expStr = "1981-07-13";
        result = MAPPER.readValue(quote(expStr), java.sql.Date.class);
        c.setTimeInMillis(result.getTime());
        assertEquals(1981, c.get(Calendar.YEAR));
        assertEquals(Calendar.JULY, c.get(Calendar.MONTH));
        assertEquals(13, c.get(Calendar.DAY_OF_MONTH));

        

    }

// com.fasterxml.jackson.databind.deser.jdk.DateDeserializationTest::testCalendar
    public void testCalendar() throws Exception
    {
        
        java.util.Calendar value = Calendar.getInstance();
        long l = 12345678L;
        value.setTimeInMillis(l);

        
        Calendar result = MAPPER.readValue(""+l, Calendar.class);
        assertEquals(l, result.getTimeInMillis());

        
        String dateStr = dateToString(new Date(l));
        result = MAPPER.readValue(quote(dateStr), Calendar.class);

        
        if (l != result.getTimeInMillis()) {
            fail(String.format("Expected timestamp %d, got %d, for '%s'",
                    l, result.getTimeInMillis(), dateStr));
        }
    }

// com.fasterxml.jackson.databind.deser.jdk.DateDeserializationTest::testCustom
    public void testCustom() throws Exception
    {
        final ObjectMapper mapper = new ObjectMapper();
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd'X'HH:mm:ss");
        df.setTimeZone(TimeZone.getTimeZone("PST"));
        mapper.setDateFormat(df);

        String dateStr = "1972-12-28X15:45:00";
        java.util.Date exp = df.parse(dateStr);
        java.util.Date result = mapper.readValue("\""+dateStr+"\"", java.util.Date.class);
        assertEquals(exp, result);
    }

// com.fasterxml.jackson.databind.deser.jdk.DateDeserializationTest::testDatesWithEmptyStrings
    public void testDatesWithEmptyStrings() throws Exception
    {
        assertNull(MAPPER.readValue(quote(""), java.util.Date.class));
        assertNull(MAPPER.readValue(quote(""), java.util.Calendar.class));
        assertNull(MAPPER.readValue(quote(""), java.sql.Date.class));
    }

// com.fasterxml.jackson.databind.deser.jdk.DateDeserializationTest::test8601DateTimeNoMilliSecs
    public void test8601DateTimeNoMilliSecs() throws Exception
    {
        
        for (String inputStr : new String[] {
               "2010-06-28T23:34:22Z",
               "2010-06-28T23:34:22+0000",
               "2010-06-28T23:34:22+00:00",
               "2010-06-28T23:34:22+00",
        }) {
            Date inputDate = MAPPER.readValue(quote(inputStr), java.util.Date.class);
            Calendar c = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
            c.setTime(inputDate);
            assertEquals(2010, c.get(Calendar.YEAR));
            assertEquals(Calendar.JUNE, c.get(Calendar.MONTH));
            assertEquals(28, c.get(Calendar.DAY_OF_MONTH));
            assertEquals(23, c.get(Calendar.HOUR_OF_DAY));
            assertEquals(34, c.get(Calendar.MINUTE));
            assertEquals(22, c.get(Calendar.SECOND));
            assertEquals(0, c.get(Calendar.MILLISECOND));
        }
    }

// com.fasterxml.jackson.databind.deser.jdk.DateDeserializationTest::testTimeZone
    public void testTimeZone() throws Exception
    {
        TimeZone result = MAPPER.readValue(quote("PST"), TimeZone.class);
        assertEquals("PST", result.getID());
    }

// com.fasterxml.jackson.databind.deser.jdk.DateDeserializationTest::testCustomDateWithAnnotation
    public void testCustomDateWithAnnotation() throws Exception
    {
        final String INPUT = "{\"date\":\"/2005/05/25/\"}";
        DateAsStringBean result = MAPPER.readValue(INPUT, DateAsStringBean.class);
        assertNotNull(result);
        assertNotNull(result.date);
        Calendar c = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
        long l = result.date.getTime();
        if (l == 0L) {
            fail("Should not get null date");
        }
        c.setTimeInMillis(l);
        assertEquals(2005, c.get(Calendar.YEAR));
        assertEquals(Calendar.MAY, c.get(Calendar.MONTH));
        assertEquals(25, c.get(Calendar.DAY_OF_MONTH));

        
        
        result = MAPPER.readerFor(DateAsStringBean.class)
                .with(Locale.GERMANY)
                .readValue(INPUT);
        assertNotNull(result);
        assertNotNull(result.date);
        l = result.date.getTime();
        if (l == 0L) {
            fail("Should not get null date");
        }
        c.setTimeInMillis(l);
        assertEquals(2005, c.get(Calendar.YEAR));
        assertEquals(Calendar.MAY, c.get(Calendar.MONTH));
        assertEquals(25, c.get(Calendar.DAY_OF_MONTH));

        
        DateAsStringBeanGermany result2 = MAPPER.readerFor(DateAsStringBeanGermany.class).readValue(INPUT);
        assertNotNull(result2);
        assertNotNull(result2.date);
        l = result2.date.getTime();
        if (l == 0L) {
            fail("Should not get null date");
        }
        c.setTimeInMillis(l);
        assertEquals(2005, c.get(Calendar.YEAR));
        assertEquals(Calendar.MAY, c.get(Calendar.MONTH));
        assertEquals(25, c.get(Calendar.DAY_OF_MONTH));
    }

// com.fasterxml.jackson.databind.deser.jdk.DateDeserializationTest::testCustomCalendarWithAnnotation
    public void testCustomCalendarWithAnnotation() throws Exception
    {
        CalendarAsStringBean cbean = MAPPER.readValue("{\"cal\":\";2007/07/13;\"}",
                CalendarAsStringBean.class);
        assertNotNull(cbean);
        assertNotNull(cbean.cal);
        Calendar c = cbean.cal;
        assertEquals(2007, c.get(Calendar.YEAR));
        assertEquals(Calendar.JULY, c.get(Calendar.MONTH));
        assertEquals(13, c.get(Calendar.DAY_OF_MONTH));
    }

// com.fasterxml.jackson.databind.deser.jdk.DateDeserializationTest::testCustomCalendarWithTimeZone
    public void testCustomCalendarWithTimeZone() throws Exception
    {
        
        DateInCETBean cet = MAPPER.readValue("{\"date\":\"2001-01-01,10\"}",
                DateInCETBean.class);
        Calendar c = Calendar.getInstance(getUTCTimeZone());
        c.setTimeInMillis(cet.date.getTime());
        
        assertEquals(2001, c.get(Calendar.YEAR));
        assertEquals(Calendar.JANUARY, c.get(Calendar.MONTH));
        assertEquals(1, c.get(Calendar.DAY_OF_MONTH));
        assertEquals(9, c.get(Calendar.HOUR_OF_DAY));
    }

// com.fasterxml.jackson.databind.deser.jdk.DateDeserializationTest::testDateEndingWithZNonDefTZ1651
    public void testDateEndingWithZNonDefTZ1651() throws Exception
    {
        String json = quote("1970-01-01T00:00:00.000Z");

        
        
        ObjectMapper mapper = newObjectMapper();
        Date dateUTC = mapper.readValue(json, Date.class);  
    
        
        
        mapper = new ObjectMapper();
        mapper.setTimeZone(TimeZone.getTimeZone("GMT-2"));
        Date dateGMT1 = mapper.readValue(json, Date.class);  
    
        
        assertEquals(dateUTC.getTime(), dateGMT1.getTime());
    }

// com.fasterxml.jackson.databind.deser.jdk.DateDeserializationTest::testContextTimezone
    public void testContextTimezone() throws Exception
    {
        String inputStr = "1997-07-16T19:20:30.45+0100";
        final String tzId = "PST";

        
        assertTrue(MAPPER.isEnabled(DeserializationFeature.ADJUST_DATES_TO_CONTEXT_TIME_ZONE));
        final ObjectReader r = MAPPER
                .readerFor(Calendar.class)
                .with(TimeZone.getTimeZone(tzId));

        
        Calendar cal = r.readValue(quote(inputStr));
        TimeZone tz = cal.getTimeZone();
        assertEquals(tzId, tz.getID());

        assertEquals(1997, cal.get(Calendar.YEAR));
        assertEquals(Calendar.JULY, cal.get(Calendar.MONTH));
        assertEquals(16, cal.get(Calendar.DAY_OF_MONTH));

        
        assertEquals(20, cal.get(Calendar.MINUTE));
        assertEquals(30, cal.get(Calendar.SECOND));
        assertEquals(11, cal.get(Calendar.HOUR_OF_DAY));

        
        cal = r.without(DeserializationFeature.ADJUST_DATES_TO_CONTEXT_TIME_ZONE)
                .readValue(quote(inputStr));

        
        
        
        
        

    }

// com.fasterxml.jackson.databind.deser.jdk.DateDeserializationTest::testCalendarArrayUnwrap
    public void testCalendarArrayUnwrap() throws Exception
    {
        ObjectReader reader = new ObjectMapper()
                .readerFor(CalendarBean.class)
                .without(DeserializationFeature.UNWRAP_SINGLE_VALUE_ARRAYS);
        final String inputDate = "1972-12-28T00:00:00.000+0000";
        final String input = aposToQuotes("{'v':['"+inputDate+"']}");
        try {
            reader.readValue(input);
            fail("Did not throw exception when reading a value from a single value array with the UNWRAP_SINGLE_VALUE_ARRAYS feature disabled");
        } catch (MismatchedInputException exp) {
            verifyException(exp, "Cannot deserialize");
            verifyException(exp, "out of START_ARRAY");
        }

        reader = reader.with(DeserializationFeature.UNWRAP_SINGLE_VALUE_ARRAYS);
        CalendarBean bean = reader.readValue(input);
        assertNotNull(bean._v);
        assertEquals(1972, bean._v.get(Calendar.YEAR));

        
        try {
            reader.readValue(aposToQuotes("{'v':['"+inputDate+"','"+inputDate+"']}"));
            fail("Did not throw exception while reading a value from a multi value array with UNWRAP_SINGLE_VALUE_ARRAY feature enabled");
        } catch (JsonMappingException exp) {
            verifyException(exp, "Attempted to unwrap");
        }
    }

// com.fasterxml.jackson.databind.deser.jdk.DateDeserializationTest::testLenientCalendar
    public void testLenientCalendar() throws Exception
    {
        final String JSON = aposToQuotes("{'value':'2015-11-32'}");

        
        LenientCalendarBean lenBean = MAPPER.readValue(JSON, LenientCalendarBean.class);
        assertEquals(Calendar.DECEMBER, lenBean.value.get(Calendar.MONTH));
        assertEquals(2, lenBean.value.get(Calendar.DAY_OF_MONTH));

        
        try {
            MAPPER.readValue(JSON, StrictCalendarBean.class);
            fail("Should not pass with invalid (with strict) date value");
        } catch (MismatchedInputException e) {
            verifyException(e, "Cannot deserialize value of type `java.util.Calendar`");
            verifyException(e, "from String \"2015-11-32\"");
            verifyException(e, "expected format");
        }

        
        ObjectMapper mapper = new ObjectMapper();
        mapper.configOverride(java.util.Date.class)
            .setFormat(JsonFormat.Value.forLeniency(Boolean.FALSE));
        try {
            mapper.readValue(quote("2015-11-32"), java.util.Date.class);
            fail("Should not pass with invalid (with strict) date value");
        } catch (MismatchedInputException e) {
            verifyException(e, "Cannot deserialize value of type `java.util.Date`");
            verifyException(e, "from String \"2015-11-32\"");
            verifyException(e, "expected format");
        }
    }

// com.fasterxml.jackson.databind.deser.jdk.DateDeserializationTest::testInvalidFormat
    public void testInvalidFormat() throws Exception
    {
        try {
            MAPPER.readValue(quote("foobar"), Date.class);
            fail("Should have failed with an exception");
        } catch (InvalidFormatException e) {
            verifyException(e, "Cannot deserialize value of type `java.util.Date` from String");
            assertEquals("foobar", e.getValue());
            assertEquals(Date.class, e.getTargetType());
        } catch (Exception e) {
            fail("Wrong type of exception ("+e.getClass().getName()+"), should get "
                    +InvalidFormatException.class.getName());
        }
    }

// com.fasterxml.jackson.databind.deser.jdk.EnumAltIdTest::testFailWhenCaseSensitiveAndNameIsNotUpperCase
    public void testFailWhenCaseSensitiveAndNameIsNotUpperCase() throws IOException {
        try {
            READER_DEFAULT.forType(TestEnum.class).readValue("\"Jackson\"");
            fail("InvalidFormatException expected");
        } catch (InvalidFormatException e) {
            verifyException(e, "value not one of declared Enum instance names: [JACKSON, OK, RULES]");
        }
    }

// com.fasterxml.jackson.databind.deser.jdk.EnumAltIdTest::testFailWhenCaseSensitiveAndToStringIsUpperCase
    public void testFailWhenCaseSensitiveAndToStringIsUpperCase() throws IOException {
        ObjectReader r = READER_DEFAULT.forType(LowerCaseEnum.class)
                .with(DeserializationFeature.READ_ENUMS_USING_TO_STRING);
        try {
            r.readValue("\"A\"");
            fail("InvalidFormatException expected");
        } catch (InvalidFormatException e) {
            verifyException(e, "value not one of declared Enum instance names: [a, b, c]");
        }
    }

// com.fasterxml.jackson.databind.deser.jdk.EnumAltIdTest::testEnumDesIgnoringCaseWithLowerCaseContent
    public void testEnumDesIgnoringCaseWithLowerCaseContent() throws IOException {
        assertEquals(TestEnum.JACKSON,
                READER_IGNORE_CASE.forType(TestEnum.class).readValue(quote("jackson")));
    }

// com.fasterxml.jackson.databind.deser.jdk.EnumAltIdTest::testEnumDesIgnoringCaseWithUpperCaseToString
    public void testEnumDesIgnoringCaseWithUpperCaseToString() throws IOException {
        ObjectReader r = MAPPER_IGNORE_CASE.readerFor(LowerCaseEnum.class)
                .with(DeserializationFeature.READ_ENUMS_USING_TO_STRING);
        assertEquals(LowerCaseEnum.A, r.readValue("\"A\""));
    }

// com.fasterxml.jackson.databind.deser.jdk.EnumAltIdTest::testIgnoreCaseInEnumList
    public void testIgnoreCaseInEnumList() throws Exception {
        TestEnum[] enums = READER_IGNORE_CASE.forType(TestEnum[].class)
            .readValue("[\"jacksON\", \"ruLes\"]");

        assertEquals(2, enums.length);
        assertEquals(TestEnum.JACKSON, enums[0]);
        assertEquals(TestEnum.RULES, enums[1]);
    }

// com.fasterxml.jackson.databind.deser.jdk.EnumAltIdTest::testIgnoreCaseInEnumSet
    public void testIgnoreCaseInEnumSet() throws IOException {
        ObjectReader r = READER_IGNORE_CASE.forType(new TypeReference<EnumSet<TestEnum>>() { });
        EnumSet<TestEnum> set = r.readValue("[\"jackson\"]");
        assertEquals(1, set.size());
        assertTrue(set.contains(TestEnum.JACKSON));
    }

// com.fasterxml.jackson.databind.deser.jdk.EnumAltIdTest::testIgnoreCaseViaFormat
    public void testIgnoreCaseViaFormat() throws Exception
    {
        final String JSON = aposToQuotes("{'value':'ok'}");

        
        EnumBean pojo = READER_DEFAULT.forType(EnumBean.class)
            .readValue(JSON);
        assertEquals(TestEnum.OK, pojo.value);

        
        try {
            READER_DEFAULT.forType(StrictCaseBean.class)
                    .readValue(JSON);
            fail("Should not pass");
        } catch (InvalidFormatException e) {
            verifyException(e, "value not one of declared Enum instance names: [JACKSON, OK, RULES]");
        }
    }

// com.fasterxml.jackson.databind.deser.jdk.EnumDeserializationTest::testSimple
    public void testSimple() throws Exception
    {
        
        String JSON = "\"OK\" \"RULES\"  null";
        
        JsonParser jp = MAPPER.getFactory().createParser(JSON);

        assertEquals(TestEnum.OK, MAPPER.readValue(jp, TestEnum.class));
        assertEquals(TestEnum.RULES, MAPPER.readValue(jp, TestEnum.class));

        
        assertNull(MAPPER.readValue(jp, TestEnum.class));

        
        assertFalse(jp.hasCurrentToken());

        
        assertEquals(TestEnum.JACKSON, MAPPER.readValue(" 0 ", TestEnum.class));

        
        try {
             MAPPER.readValue("\"NO-SUCH-VALUE\"", TestEnum.class);
            fail("Expected an exception for bogus enum value...");
        } catch (MismatchedInputException jex) {
            verifyException(jex, "value not one of declared");
        }
        jp.close();
    }

// com.fasterxml.jackson.databind.deser.jdk.EnumDeserializationTest::testComplexEnum
    public void testComplexEnum() throws Exception
    {
        String json = MAPPER.writeValueAsString(TimeUnit.SECONDS);
        assertEquals(quote("SECONDS"), json);
        TimeUnit result = MAPPER.readValue(json, TimeUnit.class);
        assertSame(TimeUnit.SECONDS, result);
    }

// com.fasterxml.jackson.databind.deser.jdk.EnumDeserializationTest::testAnnotated
    public void testAnnotated() throws Exception
    {
        AnnotatedTestEnum e = MAPPER.readValue("\"JACKSON\"", AnnotatedTestEnum.class);
        
        assertEquals(AnnotatedTestEnum.OK, e);
    }

// com.fasterxml.jackson.databind.deser.jdk.EnumDeserializationTest::testSubclassedEnums
    public void testSubclassedEnums() throws Exception
    {
        EnumWithSubClass value = MAPPER.readValue("\"A\"", EnumWithSubClass.class);
        assertEquals(EnumWithSubClass.A, value);
    }

// com.fasterxml.jackson.databind.deser.jdk.EnumDeserializationTest::testToStringEnums
    public void testToStringEnums() throws Exception
    {
        
        ObjectMapper m = new ObjectMapper();
        m.configure(DeserializationFeature.READ_ENUMS_USING_TO_STRING, true);
        LowerCaseEnum value = m.readValue("\"c\"", LowerCaseEnum.class);
        assertEquals(LowerCaseEnum.C, value);
    }

// com.fasterxml.jackson.databind.deser.jdk.EnumDeserializationTest::testNumbersToEnums
    public void testNumbersToEnums() throws Exception
    {
        
        assertFalse(MAPPER.getDeserializationConfig().isEnabled(DeserializationFeature.FAIL_ON_NUMBERS_FOR_ENUMS));
        TestEnum value = MAPPER.readValue("1", TestEnum.class);
        assertSame(TestEnum.RULES, value);

        
        ObjectReader r = MAPPER.readerFor(TestEnum.class)
                .with(DeserializationFeature.FAIL_ON_NUMBERS_FOR_ENUMS);
        try {
            value = r.readValue("1");
            fail("Expected an error");
        } catch (MismatchedInputException e) {
            verifyException(e, "Cannot deserialize");
            verifyException(e, "not allowed to deserialize Enum value out of number: disable");
        }

        
        try {
            value = r.readValue(quote("1"));
            fail("Expected an error");
        } catch (MismatchedInputException e) {
            verifyException(e, "Cannot deserialize");
            
            verifyException(e, "value not one of declared Enum");
        }
    }

// com.fasterxml.jackson.databind.deser.jdk.EnumDeserializationTest::testEnumsWithIndex
    public void testEnumsWithIndex() throws Exception
    {
        ObjectMapper m = new ObjectMapper();
        m.enable(SerializationFeature.WRITE_ENUMS_USING_INDEX);
        String json = m.writeValueAsString(TestEnum.RULES);
        assertEquals(String.valueOf(TestEnum.RULES.ordinal()), json);
        TestEnum result = m.readValue(json, TestEnum.class);
        assertSame(TestEnum.RULES, result);
    }

// com.fasterxml.jackson.databind.deser.jdk.EnumDeserializationTest::testEnumsWithJsonValue
    public void testEnumsWithJsonValue() throws Exception
    {
        
        EnumWithJsonValue e = MAPPER.readValue(quote("foo"), EnumWithJsonValue.class);
        assertSame(EnumWithJsonValue.A, e);
        e = MAPPER.readValue(quote("bar"), EnumWithJsonValue.class);
        assertSame(EnumWithJsonValue.B, e);

        
        EnumSet<EnumWithJsonValue> set = MAPPER.readValue("[\"bar\"]",
                new TypeReference<EnumSet<EnumWithJsonValue>>() { });
        assertNotNull(set);
        assertEquals(1, set.size());
        assertTrue(set.contains(EnumWithJsonValue.B));
        assertFalse(set.contains(EnumWithJsonValue.A));

        
        EnumMap<EnumWithJsonValue,Integer> map = MAPPER.readValue("{\"foo\":13}",
                new TypeReference<EnumMap<EnumWithJsonValue, Integer>>() { });
        assertNotNull(map);
        assertEquals(1, map.size());
        assertEquals(Integer.valueOf(13), map.get(EnumWithJsonValue.A));
    }

// com.fasterxml.jackson.databind.deser.jdk.EnumDeserializationTest::testAllowUnknownEnumValuesReadAsNull
    public void testAllowUnknownEnumValuesReadAsNull() throws Exception
    {
        
        ObjectReader reader = MAPPER.reader(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_AS_NULL);
        assertNull(reader.forType(TestEnum.class).readValue("\"NO-SUCH-VALUE\""));
        assertNull(reader.forType(TestEnum.class).readValue(" 4343 "));
    }

// com.fasterxml.jackson.databind.deser.jdk.EnumDeserializationTest::testAllowUnknownEnumValuesReadAsNullWithCreatorMethod
    public void testAllowUnknownEnumValuesReadAsNullWithCreatorMethod() throws Exception
    {
        
        ObjectReader reader = MAPPER.reader(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_AS_NULL);
        assertNull(reader.forType(StrictEnumCreator.class).readValue("\"NO-SUCH-VALUE\""));
        assertNull(reader.forType(StrictEnumCreator.class).readValue(" 4343 "));
    }

// com.fasterxml.jackson.databind.deser.jdk.EnumDeserializationTest::testAllowUnknownEnumValuesForEnumSets
    public void testAllowUnknownEnumValuesForEnumSets() throws Exception
    {
        ObjectReader reader = MAPPER.reader(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_AS_NULL);
        EnumSet<TestEnum> result = reader.forType(new TypeReference<EnumSet<TestEnum>>() { })
                .readValue("[\"NO-SUCH-VALUE\"]");
        assertEquals(0, result.size());
    }

// com.fasterxml.jackson.databind.deser.jdk.EnumDeserializationTest::testAllowUnknownEnumValuesAsMapKeysReadAsNull
    public void testAllowUnknownEnumValuesAsMapKeysReadAsNull() throws Exception
    {
        ObjectReader reader = MAPPER.reader(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_AS_NULL);
        ClassWithEnumMapKey result = reader.forType(ClassWithEnumMapKey.class)
                .readValue("{\"map\":{\"NO-SUCH-VALUE\":\"val\"}}");
        assertTrue(result.map.containsKey(null));
    }

// com.fasterxml.jackson.databind.deser.jdk.EnumDeserializationTest::testDoNotAllowUnknownEnumValuesAsMapKeysWhenReadAsNullDisabled
    public void testDoNotAllowUnknownEnumValuesAsMapKeysWhenReadAsNullDisabled() throws Exception
    {
        assertFalse(MAPPER.isEnabled(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_AS_NULL));
         try {
             MAPPER.readValue("{\"map\":{\"NO-SUCH-VALUE\":\"val\"}}", ClassWithEnumMapKey.class);
             fail("Expected an exception for bogus enum value...");
         } catch (InvalidFormatException jex) {
             verifyException(jex, "Cannot deserialize Map key of type `com.fasterxml.jackson.databind.deser.jdk.EnumDeserializationTest$TestEnum`");
         }
    }

// com.fasterxml.jackson.databind.deser.jdk.EnumDeserializationTest::testEnumsWithEmpty
    public void testEnumsWithEmpty() throws Exception
    {
       final ObjectMapper mapper = new ObjectMapper();
       mapper.configure(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, true);
       TestEnum result = mapper.readValue("\"\"", TestEnum.class);
       assertNull(result);
    }

// com.fasterxml.jackson.databind.deser.jdk.EnumDeserializationTest::testGenericEnumDeserialization
    public void testGenericEnumDeserialization() throws Exception
    {
       final ObjectMapper mapper = new ObjectMapper();
       SimpleModule module = new SimpleModule("foobar");
       module.addDeserializer(Enum.class, new LcEnumDeserializer());
       mapper.registerModule(module);
       
       assertEquals(TestEnum.JACKSON, mapper.readValue(quote("jackson"), TestEnum.class));
    }

// com.fasterxml.jackson.databind.deser.jdk.EnumDeserializationTest::testUnwrappedEnum
    public void testUnwrappedEnum() throws Exception {
        final ObjectMapper mapper = newObjectMapper();
        mapper.enable(DeserializationFeature.UNWRAP_SINGLE_VALUE_ARRAYS);
        
        assertEquals(TestEnum.JACKSON, mapper.readValue("[" + quote("JACKSON") + "]", TestEnum.class));
    }

// com.fasterxml.jackson.databind.deser.jdk.EnumDeserializationTest::testUnwrappedEnumException
    public void testUnwrappedEnumException() throws Exception {
        final ObjectMapper mapper = newObjectMapper();
        mapper.disable(DeserializationFeature.UNWRAP_SINGLE_VALUE_ARRAYS);
        try {
            Object v = mapper.readValue("[" + quote("JACKSON") + "]",
                    TestEnum.class);
            fail("Exception was not thrown on deserializing a single array element of type enum; instead got: "+v);
        } catch (MismatchedInputException exp) {
            
            verifyException(exp, "Cannot deserialize");
        }
    }

// com.fasterxml.jackson.databind.deser.jdk.EnumDeserializationTest::testIndexAsString
    public void testIndexAsString() throws Exception
    {
        
        TestEnum en = MAPPER.readValue("2", TestEnum.class);
        assertSame(TestEnum.values()[2], en);

        
        en = MAPPER.readValue(quote("1"), TestEnum.class);
        assertSame(TestEnum.values()[1], en);

        
        final ObjectMapper mapper = newObjectMapper();
        mapper.disable(MapperFeature.ALLOW_COERCION_OF_SCALARS);
        try {
            en = mapper.readValue(quote("1"), TestEnum.class);
            fail("Should not pass");
        } catch (MismatchedInputException e) {
            verifyException(e, "Cannot deserialize value of type");
            verifyException(e, "EnumDeserializationTest$TestEnum");
            verifyException(e, "value looks like quoted Enum index");
        }
    }

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
    public void testExceptionFromCustomEnumKeyDeserializer() throws Exception {
        ObjectMapper mapper = newObjectMapper()
                .registerModule(new EnumModule());
        try {
            mapper.readValue("{\"TWO\": \"dumpling\"}",
                    new TypeReference<Map<AnEnum, String>>() {});
            fail("No exception");
        } catch (MismatchedInputException e) {
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

// com.fasterxml.jackson.databind.deser.jdk.EnumMapDeserializationTest::testEnumMapAsPolymorphic
    public void testEnumMapAsPolymorphic() throws Exception
    {
        EnumMap<Enum1859, String> enumMap = new EnumMap<>(Enum1859.class);
        enumMap.put(Enum1859.A, "Test");
        enumMap.put(Enum1859.B, "stuff");
        Pojo1859 input = new Pojo1859(enumMap);

        ObjectMapper mapper = new ObjectMapper();
        mapper.enableDefaultTypingAsProperty(ObjectMapper.DefaultTyping.NON_FINAL, "@type");

        
         

        String json = mapper.writeValueAsString(input);
        Pojo1859 result = mapper.readValue(json, Pojo1859.class);
        assertNotNull(result);
        assertNotNull(result.values);
        assertEquals(2, result.values.size());
    }

// com.fasterxml.jackson.databind.deser.jdk.EnumMapDeserializationTest::testUnknownKeyAsDefault
    public void testUnknownKeyAsDefault() throws Exception
    {
        
        EnumMap<TestEnumWithDefault,String> value = MAPPER
                .readerFor(new TypeReference<EnumMap<TestEnumWithDefault,String>>() { })
                .with(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_USING_DEFAULT_VALUE)
                .readValue("{\"unknown\":\"value\"}");
        assertEquals(1, value.size());
        assertEquals("value", value.get(TestEnumWithDefault.OK));

        Map<TestEnumWithDefault,String> value2 = MAPPER
                .readerFor(new TypeReference<Map<TestEnumWithDefault,String>>() { })
                .with(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_USING_DEFAULT_VALUE)
                .readValue("{\"unknown\":\"value\"}");
        assertEquals(1, value2.size());
        assertEquals("value", value2.get(TestEnumWithDefault.OK));
    }

// com.fasterxml.jackson.databind.deser.jdk.EnumMapDeserializationTest::testUnknownKeyAsNull
    public void testUnknownKeyAsNull() throws Exception
    {
        
        EnumMap<TestEnumWithDefault,String> value = MAPPER
                .readerFor(new TypeReference<EnumMap<TestEnumWithDefault,String>>() { })
                .with(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_AS_NULL)
                .readValue("{\"unknown\":\"value\"}");
        assertEquals(0, value.size());

        
        Map<TestEnumWithDefault,String> value2 = MAPPER
                .readerFor(new TypeReference<Map<TestEnumWithDefault,String>>() { })
                .with(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_AS_NULL)
                .readValue("{\"unknown\":\"value\"}");
        
        
        assertEquals(1, value2.size());
        assertEquals("value", value2.get(null));
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

        
        assertEquals(aposToQuotes("{'value':null}"), mapper.writeValueAsString(input));

        
        mapper = new ObjectMapper().setSerializationInclusion(JsonInclude
                .Include.NON_NULL);
        assertEquals(aposToQuotes("{'value':null}"), mapper.writeValueAsString(input));

        
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

// com.fasterxml.jackson.databind.deser.jdk.JDKCollectionsDeserTest::testSingletonCollections
    public void testSingletonCollections() throws Exception
    {
        final TypeReference<?> xbeanListType = new TypeReference<List<XBean>>() { };

        String json = MAPPER.writeValueAsString(Collections.singleton(new XBean(3)));
        Collection<XBean> result = MAPPER.readValue(json, xbeanListType);
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(3, result.iterator().next().x);

        json = MAPPER.writeValueAsString(Collections.singletonList(new XBean(28)));
        result = MAPPER.readValue(json, xbeanListType);
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(28, result.iterator().next().x);
    }

// com.fasterxml.jackson.databind.deser.jdk.JDKCollectionsDeserTest::testUnmodifiableSet
    public void testUnmodifiableSet() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        mapper.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL, JsonTypeInfo.As.PROPERTY);

        Set<String> theSet = Collections.unmodifiableSet(Collections.singleton("a"));
        String json = mapper.writeValueAsString(theSet);

        assertEquals("[\"java.util.Collections$UnmodifiableSet\",[\"a\"]]", json);

        
         
         
         
         
        
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
        assertNull(MAPPER.readValue(quote(""), Character.class));
        assertNull(MAPPER.readValue(quote(""), Integer.class));
        assertNull(MAPPER.readValue(quote(""), Long.class));
        assertNull(MAPPER.readValue(quote(""), Float.class));
        assertNull(MAPPER.readValue(quote(""), Double.class));

        assertNull(MAPPER.readValue(quote(""), BigInteger.class));
        assertNull(MAPPER.readValue(quote(""), BigDecimal.class));
    }

// com.fasterxml.jackson.databind.deser.jdk.JDKNumberDeserTest::testTextualNullAsNumber
    public void testTextualNullAsNumber() throws Exception
    {
        final String NULL_JSON = quote("null");
        assertNull(MAPPER.readValue(NULL_JSON, Byte.class));
        assertNull(MAPPER.readValue(NULL_JSON, Short.class));
        

        assertNull(MAPPER.readValue(NULL_JSON, Integer.class));
        assertNull(MAPPER.readValue(NULL_JSON, Long.class));
        assertNull(MAPPER.readValue(NULL_JSON, Float.class));
        assertNull(MAPPER.readValue(NULL_JSON, Double.class));

        assertEquals(Byte.valueOf((byte) 0), MAPPER.readValue(NULL_JSON, Byte.TYPE));
        assertEquals(Short.valueOf((short) 0), MAPPER.readValue(NULL_JSON, Short.TYPE));
        

        assertEquals(Integer.valueOf(0), MAPPER.readValue(NULL_JSON, Integer.TYPE));
        assertEquals(Long.valueOf(0L), MAPPER.readValue(NULL_JSON, Long.TYPE));
        assertEquals(Float.valueOf(0f), MAPPER.readValue(NULL_JSON, Float.TYPE));
        assertEquals(Double.valueOf(0d), MAPPER.readValue(NULL_JSON, Double.TYPE));
        
        assertNull(MAPPER.readValue(NULL_JSON, BigInteger.class));
        assertNull(MAPPER.readValue(NULL_JSON, BigDecimal.class));

        
        try {
            MAPPER.readerFor(Integer.TYPE).with(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES)
                .readValue(NULL_JSON);
            fail("Should not have passed");
        } catch (MismatchedInputException e) {
            verifyException(e, "Cannot coerce String \"null\"");
        }

        ObjectMapper noCoerceMapper = new ObjectMapper();
        noCoerceMapper.disable(MapperFeature.ALLOW_COERCION_OF_SCALARS);
        try {
            noCoerceMapper.readValue(NULL_JSON, Integer.TYPE);
            fail("Should not have passed");
        } catch (MismatchedInputException e) {
            verifyException(e, "Cannot coerce String \"null\"");
        }
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

        try {
            MAPPER.readerFor(CharacterBean.class)
                .with(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES)
                .readValue("{\"v\":null}");
            fail("Attempting to deserialize a 'null' JSON reference into a 'char' property did not throw an exception");
        } catch (MismatchedInputException e) {
            verifyException(e, "cannot map `null`");
        }
        final CharacterBean charBean = MAPPER.readerFor(CharacterBean.class)
                .without(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES)
                .readValue("{\"v\":null}");
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
        } catch (MismatchedInputException exp) {
            
        }
        
        mapper.enable(DeserializationFeature.UNWRAP_SINGLE_VALUE_ARRAYS);
        
        result = mapper.readValue("{\"v\":[3]}", IntBean.class);
        assertEquals(3, result._v);
        
        result = mapper.readValue("[{\"v\":[3]}]", IntBean.class);
        assertEquals(3, result._v);
        
        try {
            mapper.readValue("[{\"v\":[3,3]}]", IntBean.class);
            fail("Did not throw exception while reading a value from a multi value array with UNWRAP_SINGLE_VALUE_ARRAY feature enabled");
        } catch (MismatchedInputException exp) {
            
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
        } catch (MismatchedInputException exp) {
            
        }
        
        mapper.enable(DeserializationFeature.UNWRAP_SINGLE_VALUE_ARRAYS);
        
        result = mapper.readValue("{\"v\":[3]}", LongBean.class);
        assertEquals(3, result._v);
        
        result = mapper.readValue("[{\"v\":[3]}]", LongBean.class);
        assertEquals(3, result._v);
        
        try {
            mapper.readValue("[{\"v\":[3,3]}]", LongBean.class);
            fail("Did not throw exception while reading a value from a multi value array with UNWRAP_SINGLE_VALUE_ARRAY feature enabled");
        } catch (MismatchedInputException exp) {
            
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

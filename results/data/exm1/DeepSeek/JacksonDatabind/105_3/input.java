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
        } catch (InvalidTypeIdException e) {
            verifyException(e, "missing type id property '#type'");
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

// com.fasterxml.jackson.databind.jsontype.TestSubtypes::testSubclassLimits
    public void testSubclassLimits() throws Exception
    {
        try {
            MAPPER.readValue(aposToQuotes("{'value':['"
                    +TheBomb.class.getName()+"',{'a':13}] }"), DateWrapper.class);
            fail("Should not pass");
        } catch (InvalidTypeIdException e) {
            verifyException(e, "not a subtype");
            verifyException(e, TheBomb.class.getName());
        } catch (Exception e) {
            fail("Should have hit `InvalidTypeIdException`, not `"+e.getClass().getName()+"`: "+e);
        }
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

// com.fasterxml.jackson.databind.jsontype.TestTypeNames::testBaseTypeId1616
    public void testBaseTypeId1616() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        Collection<NamedType> subtypes = new StdSubtypeResolver().collectAndResolveSubtypesByTypeId(
                mapper.getDeserializationConfig(),
                
                null,
                mapper.constructType(Base1616.class));
        assertEquals(2, subtypes.size());
        Set<String> ok = new HashSet<>(Arrays.asList("A", "B"));
        for (NamedType type : subtypes) {
            String id = type.getName();
            if (!ok.contains(id)) {
                fail("Unexpected id '"+id+"' (mapping to: "+type.getType()+"), should be one of: "+ok);
            }
        }
    }

// com.fasterxml.jackson.databind.jsontype.TestTypeNames::testSerialization
    public void testSerialization() throws Exception
    {
        
        
        
        
        assertEquals("[{\"doggy\":{\"name\":\"Spot\",\"ageInYears\":3}}]",
                MAPPER.writeValueAsString(new Animal[] { new Dog("Spot", 3) }));
        assertEquals("[{\"MaineCoon\":{\"name\":\"Belzebub\",\"purrs\":true}}]",
                MAPPER.writeValueAsString(new Animal[] { new MaineCoon("Belzebub", true)}));
    }

// com.fasterxml.jackson.databind.jsontype.TestTypeNames::testRoundTrip
    public void testRoundTrip() throws Exception
    {
        Animal[] input = new Animal[] {
                new Dog("Odie", 7),
                null,
                new MaineCoon("Piru", false),
                new Persian("Khomeini", true)
        };
        String json = MAPPER.writeValueAsString(input);
        List<Animal> output = MAPPER.readValue(json,
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
        AnimalMap input = new AnimalMap();
        input.put("venla", new MaineCoon("Venla", true));
        input.put("ama", new Dog("Amadeus", 13));
        String json = MAPPER.writeValueAsString(input);
        AnimalMap output = MAPPER.readValue(json, AnimalMap.class);
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
        JavaType rootType = mapper.getTypeFactory().constructParametricType(Iterator.class, Animal.class);
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

// com.fasterxml.jackson.databind.jsontype.TestVisibleTypeId::testTypeIdFromExternal
    public void testTypeIdFromExternal() throws Exception
    {
        String json = MAPPER.writeValueAsString(new ExternalIdWrapper2());
        
        assertEquals("{\"bean\":{\"a\":2},\"type\":\"SomeType\"}", json);
        
    }

// com.fasterxml.jackson.databind.jsontype.TestVisibleTypeId::testIssue263
    public void testIssue263() throws Exception
    {
        
        assertEquals("{\"name\":\"bob\",\"age\":41}", MAPPER.writeValueAsString(new I263Impl()));
        
        
        I263Base result = MAPPER.readValue("{\"age\":19,\"name\":\"bob\"}", I263Base.class);
        assertTrue(result instanceof I263Impl);
        assertEquals(19, ((I263Impl) result).age);
    }

// com.fasterxml.jackson.databind.jsontype.TestVisibleTypeId::testVisibleTypeId408
    public void testVisibleTypeId408() throws Exception
    {
        String json = MAPPER.writeValueAsString(new ExternalBeanWithId(3));
        ExternalBeanWithId result = MAPPER.readValue(json, ExternalBeanWithId.class);
        assertNotNull(result);
        assertNotNull(result.bean);
        assertEquals(3, result.bean.value);
        assertEquals("vbean", result._type);
    }

// com.fasterxml.jackson.databind.jsontype.TestVisibleTypeId::testInvalidMultipleTypeIds
    public void testInvalidMultipleTypeIds() throws Exception
    {
        try {
            MAPPER.writeValueAsString(new MultipleIds());
            fail("Should have failed");
        } catch (JsonMappingException e) {
            verifyException(e, "multiple type ids");
        }
    }

// com.fasterxml.jackson.databind.jsontype.TestWithGenerics::testWrapperWithGetter
    public void testWrapperWithGetter() throws Exception
    {
        Dog dog = new Dog("Fluffy", 3);
        String json = MAPPER.writeValueAsString(new ContainerWithGetter<Animal>(dog));
        if (json.indexOf("\"object-type\":\"doggy\"") < 0) {
            fail("polymorphic type not kept, result == "+json+"; should contain 'object-type':'...'");
        }
    }

// com.fasterxml.jackson.databind.jsontype.TestWithGenerics::testWrapperWithField
    public void testWrapperWithField() throws Exception
    {
        Dog dog = new Dog("Fluffy", 3);
        String json = MAPPER.writeValueAsString(new ContainerWithField<Animal>(dog));
        if (json.indexOf("\"object-type\":\"doggy\"") < 0) {
            fail("polymorphic type not kept, result == "+json+"; should contain 'object-type':'...'");
        }
    }

// com.fasterxml.jackson.databind.jsontype.TestWithGenerics::testWrapperWithExplicitType
    public void testWrapperWithExplicitType() throws Exception
    {
        Dog dog = new Dog("Fluffy", 3);
        ContainerWithGetter<Animal> c2 = new ContainerWithGetter<Animal>(dog);
        String json = MAPPER.writerFor(MAPPER.getTypeFactory().constructParametricType(ContainerWithGetter.class,
                Animal.class)).writeValueAsString(c2);
        if (json.indexOf("\"object-type\":\"doggy\"") < 0) {
            fail("polymorphic type not kept, result == "+json+"; should contain 'object-type':'...'");
        }
    }

// com.fasterxml.jackson.databind.jsontype.TestWithGenerics::testJackson387
    public void testJackson387() throws Exception
    {
        ObjectMapper om = new ObjectMapper();
        om.enableDefaultTyping( ObjectMapper.DefaultTyping.JAVA_LANG_OBJECT, JsonTypeInfo.As.PROPERTY );
        om.setSerializationInclusion(JsonInclude.Include.NON_NULL );
        om.enable( SerializationFeature.INDENT_OUTPUT);

        MyClass mc = new MyClass();

        MyParam<Integer> moc1 = new MyParam<Integer>(1);
        MyParam<String> moc2 = new MyParam<String>("valueX");

        SomeObject so = new SomeObject();
        so.someValue = "xxxxxx"; 
        MyParam<SomeObject> moc3 = new MyParam<SomeObject>(so);

        List<SomeObject> colist = new ArrayList<SomeObject>();
        colist.add( new SomeObject() );
        colist.add( new SomeObject() );
        colist.add( new SomeObject() );
        MyParam<List<SomeObject>> moc4 = new MyParam<List<SomeObject>>(colist);

        mc.params.add( moc1 );
        mc.params.add( moc2 );
        mc.params.add( moc3 );
        mc.params.add( moc4 );

        String json = om.writeValueAsString( mc );
        
        MyClass mc2 = om.readValue(json, MyClass.class );
        assertNotNull(mc2);
        assertNotNull(mc2.params);
        assertEquals(4, mc2.params.size());
    }

// com.fasterxml.jackson.databind.jsontype.TestWithGenerics::testJackson430
    public void testJackson430() throws Exception
    {
        ObjectMapper om = new ObjectMapper();

        om.setSerializerFactory( new CustomJsonSerializerFactory() );
        MyClass mc = new MyClass();
        mc.params.add(new MyParam<Integer>(1));

        String str = om.writeValueAsString( mc );

        
        MyClass mc2 = om.readValue( str, MyClass.class );
        assertNotNull(mc2);
        assertNotNull(mc2.params);
        assertEquals(1, mc2.params.size());
    }

// com.fasterxml.jackson.databind.jsontype.TestWithGenerics::testValueWithMoreGenericParameters
    public void testValueWithMoreGenericParameters() throws Exception
    {
        WrappedContainerWithField wrappedContainerWithField = new WrappedContainerWithField();
        wrappedContainerWithField.animalContainer = new ContainerWithTwoAnimals<Dog,Dog>(new Dog("d1",1), new Dog("d2",2));
        String json = MAPPER.writeValueAsString(wrappedContainerWithField);
        assertNotNull(json);
    }

// com.fasterxml.jackson.databind.jsontype.TypeRefinementForMapTest::testMapRefinement
    public void testMapRefinement() throws Exception
    {
        String ID1 = "3a6383d4-8123-4c43-8b8d-7cedf3e59404";
        String ID2 = "81c3d978-90c4-4b00-8da1-1c39ffcab02c";
        String json = aposToQuotes(
"{'id':'"+ID1+"','items':[{'id':'"+ID2+"','property':'value'}]}");

        ObjectMapper m = new ObjectMapper();
        Data data = m.readValue(json, Data.class);

        assertEquals(ID1, data.id);
        assertNotNull(data.items);
        assertEquals(1, data.items.size());
        Item value = data.items.get(ID2);
        assertNotNull(value);
        assertEquals("value", value.property);
    }

// com.fasterxml.jackson.databind.jsontype.TypeRefinementForMapTest::testMapKeyRefinement1384
    public void testMapKeyRefinement1384() throws Exception
    {
        final String TEST_INSTANCE_SERIALIZED =
                "{\"mapProperty\":[\"java.util.HashMap\",{\"Compound|Key\":\"Value\"}]}";
        ObjectMapper mapper = new ObjectMapper().enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);

        TestClass testInstance = mapper.readValue(TEST_INSTANCE_SERIALIZED, TestClass.class);
        assertEquals(1, testInstance.mapProperty.size());
        Object key = testInstance.mapProperty.keySet().iterator().next();
        assertEquals(CompoundKey.class, key.getClass());
        String testInstanceSerialized = mapper.writeValueAsString(testInstance);
        assertEquals(TEST_INSTANCE_SERIALIZED, testInstanceSerialized);
    }

// com.fasterxml.jackson.databind.jsontype.UnknownSubClassTest::testUnknownClassAsSubtype
    public void testUnknownClassAsSubtype() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_INVALID_SUBTYPE, false);
        BaseWrapper w = mapper.readValue(aposToQuotes
                ("{'value':{'clazz':'com.foobar.Nothing'}}'"),
                BaseWrapper.class);
        assertNotNull(w);
    }

// com.fasterxml.jackson.databind.jsontype.WrapperObjectWithObjectIdTest::testSimple
    public void testSimple() throws Exception
    {
        Company comp = new Company();
        comp.addComputer(new DesktopComputer("computer-1", "Bangkok"));
        comp.addComputer(new DesktopComputer("computer-2", "Pattaya"));
        comp.addComputer(new LaptopComputer("computer-3", "Apple"));

        final ObjectMapper mapper = new ObjectMapper();

        String json = mapper.writerWithDefaultPrettyPrinter()
                .writeValueAsString(comp);

        Company result = mapper.readValue(json, Company.class);
        assertNotNull(result);
        assertNotNull(result.computers);
        assertEquals(3, result.computers.size());
    }

// com.fasterxml.jackson.databind.jsontype.deftyping.TestDefaultForArrays::testArrayTypingSimple
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

// com.fasterxml.jackson.databind.jsontype.deftyping.TestDefaultForArrays::testArrayTypingNested
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

// com.fasterxml.jackson.databind.jsontype.deftyping.TestDefaultForArrays::testNodeInArray
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

// com.fasterxml.jackson.databind.jsontype.deftyping.TestDefaultForArrays::testNodeInEmptyArray
    public void testNodeInEmptyArray() throws Exception {
        Map<String, List<String>> outerMap = new HashMap<String, List<String>>();
        outerMap.put("inner", new ArrayList<String>());
        ObjectMapper m = new ObjectMapper().disable(SerializationFeature.WRITE_EMPTY_JSON_ARRAYS);
        JsonNode tree = m.convertValue(outerMap, JsonNode.class);
        
        String json = m.writeValueAsString(tree);
        assertEquals("{}", json);
        
        JsonNode node = new ObjectMapper().readTree("{\"a\":[]}");
        
        m.enableDefaultTyping(DefaultTyping.JAVA_LANG_OBJECT);
        Object[] obs = new Object[] { node };
        json = m.writeValueAsString(obs);
        Object[] result = m.readValue(json, Object[].class);
        assertEquals("{}", result[0].toString());
    }

// com.fasterxml.jackson.databind.jsontype.deftyping.TestDefaultForArrays::testArraysOfArrays
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

// com.fasterxml.jackson.databind.jsontype.deftyping.TestDefaultForArrays::testArrayTypingForPrimitiveArrays
    public void testArrayTypingForPrimitiveArrays() throws Exception
    {
        ObjectMapper m = new ObjectMapper();
        m.enableDefaultTyping(DefaultTyping.NON_CONCRETE_AND_ARRAYS);
        _testArrayTypingForPrimitiveArrays(m, new int[] { 1, 2, 3 });
        _testArrayTypingForPrimitiveArrays(m, new long[] { 1, 2, 3 });
        _testArrayTypingForPrimitiveArrays(m, new short[] { 1, 2, 3 });
        _testArrayTypingForPrimitiveArrays(m, new double[] { 0.5, 5.5, -1.0 });
        _testArrayTypingForPrimitiveArrays(m, new float[] { 0.5f, 5.5f, -1.0f });
        _testArrayTypingForPrimitiveArrays(m, new boolean[] { true, false });
        _testArrayTypingForPrimitiveArrays(m, new byte[] { 1, 2, 3 });

        _testArrayTypingForPrimitiveArrays(m, new char[] { 'a', 'b' });
    }

// com.fasterxml.jackson.databind.jsontype.deftyping.TestDefaultForEnums::testSimpleEnumBean
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

// com.fasterxml.jackson.databind.jsontype.deftyping.TestDefaultForEnums::testSimpleEnumsInObjectArray
    public void testSimpleEnumsInObjectArray() throws Exception
    {
        ObjectMapper m = new ObjectMapper();
        m.enableDefaultTyping();
        
        
        String json = m.writeValueAsString(new Object[] { TestEnum.A });
        assertEquals("[[\"com.fasterxml.jackson.databind.jsontype.deftyping.TestDefaultForEnums$TestEnum\",\"A\"]]", json);

        
        Object[] value = m.readValue(json, Object[].class);
        assertEquals(1, value.length);
        assertSame(TestEnum.A, value[0]);
    }

// com.fasterxml.jackson.databind.jsontype.deftyping.TestDefaultForEnums::testSimpleEnumsAsField
    public void testSimpleEnumsAsField() throws Exception
    {
        ObjectMapper m = new ObjectMapper();
        m.enableDefaultTyping();
        String json = m.writeValueAsString(new EnumHolder(TestEnum.B));
        assertEquals("{\"value\":[\"com.fasterxml.jackson.databind.jsontype.deftyping.TestDefaultForEnums$TestEnum\",\"B\"]}", json);
        EnumHolder holder = m.readValue(json, EnumHolder.class);
        assertSame(TestEnum.B, holder.value);
    }

// com.fasterxml.jackson.databind.jsontype.deftyping.TestDefaultForLists::testListOfLongs
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

// com.fasterxml.jackson.databind.jsontype.deftyping.TestDefaultForLists::testListOfNumbers
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

// com.fasterxml.jackson.databind.jsontype.deftyping.TestDefaultForLists::testDateTypes
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

// com.fasterxml.jackson.databind.jsontype.deftyping.TestDefaultForLists::testJackson628
    public void testJackson628() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        mapper.enableDefaultTyping(DefaultTyping.NON_FINAL);
        ArrayList<Foo> data = new ArrayList<Foo>();
        String json = mapper.writeValueAsString(data);
        List<?> output = mapper.readValue(json, List.class);
        assertTrue(output.isEmpty());
    }

// com.fasterxml.jackson.databind.jsontype.deftyping.TestDefaultForLists::testJackson667
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

// com.fasterxml.jackson.databind.jsontype.deftyping.TestDefaultForMaps::testJackson428
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

// com.fasterxml.jackson.databind.jsontype.deftyping.TestDefaultForMaps::testList
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

// com.fasterxml.jackson.databind.jsontype.deftyping.TestDefaultForMaps::testMap
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

// com.fasterxml.jackson.databind.jsontype.deftyping.TestDefaultForObject::testBeanAsObject
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

// com.fasterxml.jackson.databind.jsontype.deftyping.TestDefaultForObject::testBeanAsObjectUsingAsProperty
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

// com.fasterxml.jackson.databind.jsontype.deftyping.TestDefaultForObject::testAbstractBean
    public void testAbstractBean() throws Exception
    {
        
        ObjectMapper m = new ObjectMapper();
        AbstractBean[] input = new AbstractBean[] { new StringBean("xyz") };
        String serial = m.writeValueAsString(input);
        try {
            m.readValue(serial, AbstractBean[].class);
            fail("Should have failed");
        } catch (JsonMappingException e) {
            
            verifyException(e, "cannot construct");
        }
        
        
        m = new ObjectMapper();
        m.enableDefaultTyping(ObjectMapper.DefaultTyping.OBJECT_AND_NON_CONCRETE);
        serial = m.writeValueAsString(input);
        AbstractBean[] beans = m.readValue(serial, AbstractBean[].class);
        assertEquals(1, beans.length);
        assertEquals(StringBean.class, beans[0].getClass());
        assertEquals("xyz", ((StringBean) beans[0]).name);
    }

// com.fasterxml.jackson.databind.jsontype.deftyping.TestDefaultForObject::testNonFinalBean
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

// com.fasterxml.jackson.databind.jsontype.deftyping.TestDefaultForObject::testNullValue
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

// com.fasterxml.jackson.databind.jsontype.deftyping.TestDefaultForObject::testEnumAsObject
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

// com.fasterxml.jackson.databind.jsontype.deftyping.TestDefaultForObject::testEnumSet
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

// com.fasterxml.jackson.databind.jsontype.deftyping.TestDefaultForObject::testEnumMap
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

// com.fasterxml.jackson.databind.jsontype.deftyping.TestDefaultForObject::testJackson311
    public void testJackson311() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        mapper.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);
        String json = mapper.writeValueAsString(new PolymorphicType("hello", 2));
        PolymorphicType value = mapper.readValue(json, PolymorphicType.class);
        assertEquals("hello", value.foo);
        assertEquals(Integer.valueOf(2), value.bar);
    }

// com.fasterxml.jackson.databind.jsontype.deftyping.TestDefaultForObject::testTokenBuffer
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

// com.fasterxml.jackson.databind.jsontype.deftyping.TestDefaultForObject::testIssue352
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

// com.fasterxml.jackson.databind.jsontype.deftyping.TestDefaultForObject::testFeature432
    public void testFeature432() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        mapper.enableDefaultTypingAsProperty(ObjectMapper.DefaultTyping.OBJECT_AND_NON_CONCRETE, "*CLASS*");
        String json = mapper.writeValueAsString(new BeanHolder(new StringBean("punny")));
        assertEquals("{\"bean\":{\"*CLASS*\":\"com.fasterxml.jackson.databind.jsontype.deftyping.TestDefaultForObject$StringBean\",\"name\":\"punny\"}}", json);
    }

// com.fasterxml.jackson.databind.jsontype.deftyping.TestDefaultForObject::testNoGoWithExternalProperty
    public void testNoGoWithExternalProperty() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        try {
            mapper.enableDefaultTyping(ObjectMapper.DefaultTyping.JAVA_LANG_OBJECT,
                    JsonTypeInfo.As.EXTERNAL_PROPERTY);
            fail("Should not have passed");
        } catch (IllegalArgumentException e) {
            verifyException(e, "Cannot use includeAs of EXTERNAL_PROPERTY");
        }
    }

// com.fasterxml.jackson.databind.jsontype.deftyping.TestDefaultForScalars::testNumericScalars
    public void testNumericScalars() throws Exception
    {
        ObjectMapper m = new ObjectMapper();
        m.enableDefaultTyping();

        
        assertEquals("[123]", m.writeValueAsString(new Object[] { Integer.valueOf(123) }));
        assertEquals("[[\"java.lang.Long\",37]]", m.writeValueAsString(new Object[] { Long.valueOf(37) }));
        assertEquals("[0.25]", m.writeValueAsString(new Object[] { Double.valueOf(0.25) }));
        assertEquals("[[\"java.lang.Float\",0.5]]", m.writeValueAsString(new Object[] { Float.valueOf(0.5f) }));
    }

// com.fasterxml.jackson.databind.jsontype.deftyping.TestDefaultForScalars::testDateScalars
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

// com.fasterxml.jackson.databind.jsontype.deftyping.TestDefaultForScalars::testMiscScalars
    public void testMiscScalars() throws Exception
    {
        ObjectMapper m = new ObjectMapper();
        m.enableDefaultTyping();

        
        assertEquals("[\"abc\"]", m.writeValueAsString(new Object[] { "abc" }));
        assertEquals("[true,null,false]", m.writeValueAsString(new Boolean[] { true, null, false }));
    }

// com.fasterxml.jackson.databind.jsontype.deftyping.TestDefaultForScalars::testScalarArrays
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

// com.fasterxml.jackson.databind.jsontype.deftyping.TestDefaultForScalars::test417
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

// com.fasterxml.jackson.databind.jsontype.deftyping.TestDefaultForScalars::testDefaultTypingWithLong
    public void testDefaultTypingWithLong() throws Exception
    {
        Data data = new Data();
        data.key = 1L;
        Map<String, Object> mapData = new HashMap<String, Object>();
        mapData.put("longInMap", 2L);
        mapData.put("longAsField", data);

        
        ObjectMapper mapper = new ObjectMapper();
        StdTypeResolverBuilder resolver = new StdTypeResolverBuilder();
        resolver.init(JsonTypeInfo.Id.CLASS, null);
        resolver.inclusion(JsonTypeInfo.As.PROPERTY);
        resolver.typeProperty("__t");
        mapper.setDefaultTyping(resolver);
        mapper.enable(SerializationFeature.INDENT_OUTPUT);

        
        String json = mapper.writeValueAsString(mapData);

        
        Map<?,?> result = mapper.readValue(json, Map.class);
        assertNotNull(result);
        assertEquals(2, result.size());
    }

// com.fasterxml.jackson.databind.jsontype.deftyping.TestDefaultWithCreators::testWithCreators
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

// com.fasterxml.jackson.databind.jsontype.deftyping.TestDefaultWithCreators::testWithCreatorAndJsonValue
    public void testWithCreatorAndJsonValue() throws Exception
    {
        final byte[] BYTES = new byte[] { 1, 2, 3, 4, 5 };
        ObjectMapper mapper = new ObjectMapper();
        mapper.enableDefaultTyping();
        String json = mapper.writeValueAsString(new Bean1385Wrapper(
                new Bean1385(BYTES)
        ));
        Bean1385Wrapper result = mapper.readValue(json, Bean1385Wrapper.class);
        assertNotNull(result);
        assertNotNull(result.value);
        assertEquals(Bean1385.class, result.value.getClass());
        Bean1385 b = (Bean1385) result.value;
        Assert.assertArrayEquals(BYTES, b.raw);
    }

// com.fasterxml.jackson.databind.jsontype.ext.ExternalTypeId198Test::testFails
    public void testFails() throws Exception {
        String json = "{ \"name\": \"foo\", \"attack\":\"right\" } }";

        Character character = MAPPER.readValue(json, Character.class);

        assertNotNull(character);
        assertNotNull(character.attack);
        assertEquals("foo", character.name);
    }

// com.fasterxml.jackson.databind.jsontype.ext.ExternalTypeId198Test::testWorks
    public void testWorks() throws Exception {
        String json = "{ \"name\": \"foo\", \"preferredAttack\": \"KICK\", \"attack\":\"right\" } }";

        Character character = MAPPER.readValue(json, Character.class);

        assertNotNull(character);
        assertNotNull(character.attack);
        assertEquals("foo", character.name);
    }

// com.fasterxml.jackson.databind.jsontype.ext.ExternalTypeId999Test::testExternalTypeId
    public void testExternalTypeId() throws Exception
    {
        TypeReference<?> type = new TypeReference<Message<FooPayload>>() { };

        Message<?> msg = MAPPER.readValue(aposToQuotes("{ 'type':'foo', 'payload': {} }"), type);
        assertNotNull(msg);
        assertNotNull(msg.payload);
        assertEquals("foo", msg.type);

        
        msg = MAPPER.readValue(aposToQuotes("{'payload': {}, 'type':'foo' }"), type);
        assertNotNull(msg);
        assertNotNull(msg.payload);
        assertEquals("foo", msg.type);
    }

// com.fasterxml.jackson.databind.jsontype.ext.ExternalTypeIdTest::testSimpleSerialization
    public void testSimpleSerialization() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerSubtypes(ValueBean.class);
        
        
        
        
        
        
        assertEquals("{\"bean\":{\"value\":11},\"extType\":\"vbean\"}",
                mapper.writeValueAsString(new ExternalBean(11)));
    }

// com.fasterxml.jackson.databind.jsontype.ext.ExternalTypeIdTest::testImproperExternalIdSerialization
    public void testImproperExternalIdSerialization() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        assertEquals("{\"extType\":\"funk\",\"i\":3}",
                mapper.writeValueAsString(new FunkyExternalBean()));
    }

// com.fasterxml.jackson.databind.jsontype.ext.ExternalTypeIdTest::testExternalTypeIdWithNull
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

// com.fasterxml.jackson.databind.jsontype.ext.ExternalTypeIdTest::testSimpleDeserialization
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

// com.fasterxml.jackson.databind.jsontype.ext.ExternalTypeIdTest::testMultipleTypeIdsDeserialization
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

// com.fasterxml.jackson.databind.jsontype.ext.ExternalTypeIdTest::testExternalTypeWithCreator
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

// com.fasterxml.jackson.databind.jsontype.ext.ExternalTypeIdTest::testImproperExternalIdDeserialization
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

// com.fasterxml.jackson.databind.jsontype.ext.ExternalTypeIdTest::testIssue798
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

// com.fasterxml.jackson.databind.jsontype.ext.ExternalTypeIdTest::testIssue831
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

// com.fasterxml.jackson.databind.jsontype.ext.ExternalTypeIdTest::testWithScalar118
    public void testWithScalar118() throws Exception
    {
        ExternalTypeWithNonPOJO input = new ExternalTypeWithNonPOJO(new java.util.Date(123L));
        String json = MAPPER.writeValueAsString(input);
        assertNotNull(json);

        
        ExternalTypeWithNonPOJO result = MAPPER.readValue(json, ExternalTypeWithNonPOJO.class);
        assertNotNull(result.value);
        assertTrue(result.value instanceof java.util.Date);
    }

// com.fasterxml.jackson.databind.jsontype.ext.ExternalTypeIdTest::testWithNaturalScalar118
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

// com.fasterxml.jackson.databind.jsontype.ext.ExternalTypeIdTest::testWithAsValue
    public void testWithAsValue() throws Exception
    {
        ExternalTypeWithNonPOJO input = new ExternalTypeWithNonPOJO(new AsValueThingy(12345L));
        String json = MAPPER.writeValueAsString(input);
        assertNotNull(json);
        assertEquals("{\"value\":12345,\"type\":\"thingy\"}", json);

        
        ExternalTypeWithNonPOJO result = MAPPER.readValue(json, ExternalTypeWithNonPOJO.class);
        assertNotNull(result);
        assertNotNull(result.value);
        assertEquals(AsValueThingy.class, result.value.getClass());
        assertEquals(12345L, ((AsValueThingy) result.value).rawDate);
    }

// com.fasterxml.jackson.databind.jsontype.ext.ExternalTypeIdTest::testExternalTypeWithProp222
    public void testExternalTypeWithProp222() throws Exception
    {
        final ObjectMapper mapper = new ObjectMapper();
        Issue222Bean input = new Issue222Bean(13);
        String json = mapper.writeValueAsString(input);
        assertEquals("{\"value\":{\"x\":13},\"type\":\"foo\"}", json);
    }

// com.fasterxml.jackson.databind.jsontype.ext.ExternalTypeIdTest::testInverseExternalId928
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

// com.fasterxml.jackson.databind.jsontype.ext.ExternalTypeIdTest::testBigDecimal965
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

// com.fasterxml.jackson.databind.jsontype.ext.ExternalTypeIdWithEnum1328Test::testExample
    public void testExample() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        
        String json = mapper.writerWithDefaultPrettyPrinter()
                .writeValueAsString(Arrays.asList(new AnimalAndType(AnimalType.Dog, new Dog())));
        List<AnimalAndType> list = mapper.readerFor(new TypeReference<List<AnimalAndType>>() { })
            .readValue(json);
        assertNotNull(list);
    }

// com.fasterxml.jackson.databind.jsontype.ext.MultipleExternalIds291Test::testMultipleValuesSingleExtId
    public void testMultipleValuesSingleExtId() throws Exception
    {
        
        _testMultipleValuesSingleExtId(
"{'type' : '1',\n"
+"'field1' : { 'a' : 'AAA' },\n"
+"'field2' : { 'c' : 'CCC' }\n"
+"}"
);

        
        _testMultipleValuesSingleExtId(
"{\n"
+"'field1' : { 'a' : 'AAA' },\n"
+"'field2' : { 'c' : 'CCC' },\n"
+"'type' : '1'\n"
+"}"
);
        
        _testMultipleValuesSingleExtId(
"{\n"
+"'field1' : { 'a' : 'AAA' },\n"
+"'type' : '1',\n"
+"'field2' : { 'c' : 'CCC' }\n"
+"}"
);
    }

// com.fasterxml.jackson.databind.jsontype.ext.TestSubtypesExternalPropertyMissingProperty::testDeserializationPresent
    public void testDeserializationPresent() throws Exception {
        MAPPER.disable(DeserializationFeature.FAIL_ON_MISSING_EXTERNAL_TYPE_ID_PROPERTY);
        checkOrangeBox();
        checkAppleBox();

        MAPPER.enable(DeserializationFeature.FAIL_ON_MISSING_EXTERNAL_TYPE_ID_PROPERTY);
        checkOrangeBox();
        checkAppleBox();
    }

// com.fasterxml.jackson.databind.jsontype.ext.TestSubtypesExternalPropertyMissingProperty::testDeserializationNull
    public void testDeserializationNull() throws Exception {
        MAPPER.disable(DeserializationFeature.FAIL_ON_MISSING_EXTERNAL_TYPE_ID_PROPERTY);
        checkOrangeBoxNull(orangeBoxNullJson);
        checkAppleBoxNull(appleBoxNullJson);

        MAPPER.enable(DeserializationFeature.FAIL_ON_MISSING_EXTERNAL_TYPE_ID_PROPERTY);
        checkOrangeBoxNull(orangeBoxNullJson);
        checkAppleBoxNull(appleBoxNullJson);
    }

// com.fasterxml.jackson.databind.jsontype.ext.TestSubtypesExternalPropertyMissingProperty::testDeserializationEmpty
    public void testDeserializationEmpty() throws Exception {
        MAPPER.disable(DeserializationFeature.FAIL_ON_MISSING_EXTERNAL_TYPE_ID_PROPERTY);
        checkOrangeBoxEmpty(orangeBoxEmptyJson);
        checkAppleBoxEmpty(appleBoxEmptyJson);

        MAPPER.enable(DeserializationFeature.FAIL_ON_MISSING_EXTERNAL_TYPE_ID_PROPERTY);
        checkOrangeBoxEmpty(orangeBoxEmptyJson);
        checkAppleBoxEmpty(appleBoxEmptyJson);
    }

// com.fasterxml.jackson.databind.jsontype.ext.TestSubtypesExternalPropertyMissingProperty::testDeserializationMissing
    public void testDeserializationMissing() throws Exception {
        MAPPER.disable(DeserializationFeature.FAIL_ON_MISSING_EXTERNAL_TYPE_ID_PROPERTY);
        checkOrangeBoxNull(orangeBoxMissingJson);
        checkAppleBoxNull(appleBoxMissingJson);

        MAPPER.enable(DeserializationFeature.FAIL_ON_MISSING_EXTERNAL_TYPE_ID_PROPERTY);
        checkBoxJsonMappingException(orangeBoxMissingJson);
        checkBoxJsonMappingException(appleBoxMissingJson);
    }

// com.fasterxml.jackson.databind.jsontype.ext.TestSubtypesExternalPropertyMissingProperty::testDeserializationMissingRequired
    public void testDeserializationMissingRequired() throws Exception {
        MAPPER.disable(DeserializationFeature.FAIL_ON_MISSING_EXTERNAL_TYPE_ID_PROPERTY);
        checkReqBoxJsonMappingException(orangeBoxMissingJson);
        checkReqBoxJsonMappingException(appleBoxMissingJson);

        MAPPER.enable(DeserializationFeature.FAIL_ON_MISSING_EXTERNAL_TYPE_ID_PROPERTY);
        checkReqBoxJsonMappingException(orangeBoxMissingJson);
        checkReqBoxJsonMappingException(appleBoxMissingJson);
    }

// com.fasterxml.jackson.databind.misc.AccessFixTest::testCauseOfThrowableIgnoral
    public void testCauseOfThrowableIgnoral() throws Exception
    {
        final SecurityManager origSecMan = System.getSecurityManager();
        ObjectMapper mapper = new ObjectMapper();
        mapper.disable(MapperFeature.OVERRIDE_PUBLIC_ACCESS_MODIFIERS);
        try {
            System.setSecurityManager(new CauseBlockingSecurityManager());
            _testCauseOfThrowableIgnoral(mapper);
        } finally {
            System.setSecurityManager(origSecMan);
        }
    }

// com.fasterxml.jackson.databind.misc.CaseInsensitive1854Test::testIssue1854
    public void testIssue1854() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES);
        final String DOC = aposToQuotes("{'ID': 1, 'Items': [ { 'ChildID': 10 } ]}");
        Obj1854 result = mapper.readValue(DOC, Obj1854.class);
        assertNotNull(result);
        assertEquals(1, result.getId());
        assertNotNull(result.getItems());
        assertEquals(1, result.getItems().size());
    }

// com.fasterxml.jackson.databind.misc.CaseInsensitiveDeserTest::testCaseInsensitiveDeserialization
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

        
        ObjectReader r = INSENSITIVE_MAPPER.readerFor(Issue476Bean.class);
        Issue476Bean result = r.readValue(JSON);
        assertEquals(result.value1.name, "fruit");
        assertEquals(result.value1.value, "apple");
    }

// com.fasterxml.jackson.databind.misc.CaseInsensitiveDeserTest::testCaseInsensitive1036
    public void testCaseInsensitive1036() throws Exception
    {
        final String json = "{\"ErrorCode\":2,\"DebugMessage\":\"Signature not valid!\"}";

        BaseResponse response = INSENSITIVE_MAPPER.readValue(json, BaseResponse.class);
        assertEquals(2, response.errorCode);
        assertEquals("Signature not valid!", response.debugMessage);
    }

// com.fasterxml.jackson.databind.misc.CaseInsensitiveDeserTest::testCaseInsensitiveWithFormat
    public void testCaseInsensitiveWithFormat() throws Exception {
        CaseInsensitiveRoleWrapper w = MAPPER.readValue
                (aposToQuotes("{'role':{'id':'12','name':'Foo'}}"),
                        CaseInsensitiveRoleWrapper.class);
        assertNotNull(w);
        assertEquals("12", w.role.ID);
        assertEquals("Foo", w.role.Name);
    }

// com.fasterxml.jackson.databind.misc.CaseInsensitiveDeserTest::testCreatorWithInsensitive
    public void testCreatorWithInsensitive() throws Exception
    {
        final String json = aposToQuotes("{'VALUE':3}");
        InsensitiveCreator bean = INSENSITIVE_MAPPER.readValue(json, InsensitiveCreator.class);
        assertEquals(3, bean.v);
    }

// com.fasterxml.jackson.databind.misc.CaseInsensitiveDeserTest::testCaseInsensitiveWithClassFormat
    public void testCaseInsensitiveWithClassFormat() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configOverride(Role.class)
            .setFormat(JsonFormat.Value.empty()
                    .withFeature(JsonFormat.Feature.ACCEPT_CASE_INSENSITIVE_PROPERTIES));
        Role role = mapper.readValue
                (aposToQuotes("{'id':'12','name':'Foo'}"),
                        Role.class);
        assertNotNull(role);
        assertEquals("12", role.ID);
        assertEquals("Foo", role.Name);
    }

// com.fasterxml.jackson.databind.mixins.TestMixinDeserForClass::testClassMixInsTopLevel
    public void testClassMixInsTopLevel() throws IOException
    {
        ObjectMapper m = new ObjectMapper();
        
        LeafClass result = m.readValue("{\"a\":\"value\"}", LeafClass.class);
        assertEquals("XXXvalue", result.a);

        
        
        m = new ObjectMapper();
        m.addMixIn(LeafClass.class, MixIn.class);
        result = m.readValue("{\"a\":\"value\"}", LeafClass.class);
        assertEquals("value", result.a);
    }

// com.fasterxml.jackson.databind.mixins.TestMixinDeserForClass::testClassMixInsMidLevel
    public void testClassMixInsMidLevel() throws IOException
    {
        ObjectMapper m = new ObjectMapper();
        m.addMixIn(BaseClass.class, MixIn.class);
        {
            BaseClass result = m.readValue("{\"a\":\"value\"}", BaseClass.class);
            assertEquals("value", result.a);
        }

        
        {
            LeafClass result = m.readValue("{\"a\":\"value\"}", LeafClass.class);
            assertEquals("XXXvalue", result.a);
        }
    }

// com.fasterxml.jackson.databind.mixins.TestMixinDeserForClass::testClassMixInsForObjectClass
    public void testClassMixInsForObjectClass() throws IOException
    {
        ObjectMapper m = new ObjectMapper();
        m.addMixIn(Object.class, MixIn.class);
        
        {
            BaseClass result = m.readValue("{\"a\":\"\"}", BaseClass.class);
            assertEquals("", result.a);
        }

        
        {
            LeafClass result = m.readValue("{\"a\":\"\"}", LeafClass.class);
            assertEquals("XXX", result.a);
        }
    }

// com.fasterxml.jackson.databind.mixins.TestMixinDeserForClass::testHashCodeViaObject
    public void testHashCodeViaObject() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper()
                .addMixIn(Object.class, HashCodeMixIn.class);

        
        assertEquals( "{\"hashCode\":13}",
                mapper.writeValueAsString(new Bean1990WithHashCode()));

        
        String prefix = "{\"hashCode\":";
        String json = mapper.writeValueAsString(new Bean1990WithoutHashCode());
        if (!json.startsWith(prefix)) {
            fail("Should start with ["+prefix+"], does not: ["+json+"]");
        }
    }

// com.fasterxml.jackson.databind.mixins.TestMixinDeserForCreators::testForConstructor
    public void testForConstructor() throws IOException
    {
        ObjectMapper m = new ObjectMapper();
        m.addMixIn(BaseClassWithPrivateCtor.class, MixInForPrivate.class);
        BaseClassWithPrivateCtor result = m.readValue("\"?\"", BaseClassWithPrivateCtor.class);
        assertEquals("?...", result._a);
    }

// com.fasterxml.jackson.databind.mixins.TestMixinDeserForCreators::testForFactoryAndCtor
    public void testForFactoryAndCtor() throws IOException
    {
        ObjectMapper m = new ObjectMapper();
        BaseClass result;

        
        result = m.readValue("\"string\"", BaseClass.class);
        assertEquals("string...", result._a);

        
        m = new ObjectMapper();
        m.addMixIn(BaseClass.class, MixIn.class);
        result = m.readValue("\"string\"", BaseClass.class);
        assertEquals("stringX", result._a);
    }

// com.fasterxml.jackson.databind.mixins.TestMixinDeserForCreators::testFactoryDelegateMixIn
    public void testFactoryDelegateMixIn() throws IOException
    {
        ObjectMapper m = new ObjectMapper();
        m.addMixIn(StringWrapper.class, StringWrapperMixIn.class);
        StringWrapper result = m.readValue("\"a\"", StringWrapper.class);
        assertEquals("a", result._value);
    }

// com.fasterxml.jackson.databind.mixins.TestMixinDeserForCreators::testFactoryPropertyMixin
    public void testFactoryPropertyMixin() throws Exception
    {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.addMixIn(Pair2020.class, MyPairMixIn8.class);

        String doc = aposToQuotes( "{'value0' : 456, 'value1' : 789}");
        Pair2020 pair2 = objectMapper.readValue(doc, Pair2020.class);
        assertEquals(456, pair2.x);
        assertEquals(789, pair2.y);
    }

// com.fasterxml.jackson.databind.mixins.TestMixinDeserForMethods::testWithAnySetter
    public void testWithAnySetter() throws IOException
    {
        ObjectMapper m = new ObjectMapper();
        m.addMixIn(BaseClass.class, MixIn.class);
        BaseClass result = m.readValue("{ \"a\" : 3, \"b\" : true }", BaseClass.class);
        assertNotNull(result);
        assertEquals(2, result.values.size());
        assertEquals(Integer.valueOf(3), result.values.get("a"));
        assertEquals(Boolean.TRUE, result.values.get("b"));
    }

// com.fasterxml.jackson.databind.module.SimpleModuleTest::testWithoutModule
    public void testWithoutModule()
    {
        ObjectMapper mapper = new ObjectMapper();
        
        try {
            mapper.writeValueAsString(new CustomBean("foo", 3));
            fail("Should have caused an exception");
        } catch (IOException e) {
            verifyException(e, "No serializer found");
        }

        
        try {
            mapper.readValue("{\"str\":\"ab\",\"num\":2}", CustomBean.class);
            fail("Should have caused an exception");
        } catch (IOException e) {
            verifyException(e, "Cannot construct");
            verifyException(e, "no creators");
        }
    }

// com.fasterxml.jackson.databind.module.SimpleModuleTest::testSimpleBeanSerializer
    public void testSimpleBeanSerializer() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        SimpleModule mod = new SimpleModule("test", Version.unknownVersion());
        mod.addSerializer(new CustomBeanSerializer());
        mapper.registerModule(mod);
        assertEquals(quote("abcde|5"), mapper.writeValueAsString(new CustomBean("abcde", 5)));
    }

// com.fasterxml.jackson.databind.module.SimpleModuleTest::testSimpleEnumSerializer
    public void testSimpleEnumSerializer() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        SimpleModule mod = new SimpleModule("test", Version.unknownVersion());
        mod.addSerializer(new SimpleEnumSerializer());
        
        mapper.registerModules(mod);
        assertEquals(quote("b"), mapper.writeValueAsString(SimpleEnum.B));
    }

// com.fasterxml.jackson.databind.module.SimpleModuleTest::testSimpleInterfaceSerializer
    public void testSimpleInterfaceSerializer() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        SimpleModule mod = new SimpleModule("test", Version.unknownVersion());
        mod.addSerializer(new BaseSerializer());
        
        List<SimpleModule> mods = Arrays.asList(mod);
        mapper.registerModules(mods);
        assertEquals(quote("Base:1"), mapper.writeValueAsString(new Impl1()));
        assertEquals(quote("Base:2"), mapper.writeValueAsString(new Impl2()));
    }

// com.fasterxml.jackson.databind.module.SimpleModuleTest::testSimpleBeanDeserializer
    public void testSimpleBeanDeserializer() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        SimpleModule mod = new SimpleModule("test", Version.unknownVersion());
        mod.addDeserializer(CustomBean.class, new CustomBeanDeserializer());
        mapper.registerModule(mod);
        CustomBean bean = mapper.readValue(quote("xyz|3"), CustomBean.class);
        assertEquals("xyz", bean.str);
        assertEquals(3, bean.num);
    }

// com.fasterxml.jackson.databind.module.SimpleModuleTest::testSimpleEnumDeserializer
    public void testSimpleEnumDeserializer() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        SimpleModule mod = new SimpleModule("test", Version.unknownVersion());
        mod.addDeserializer(SimpleEnum.class, new SimpleEnumDeserializer());
        mapper.registerModule(mod);
        SimpleEnum result = mapper.readValue(quote("a"), SimpleEnum.class);
        assertSame(SimpleEnum.A, result);
    }

// com.fasterxml.jackson.databind.module.SimpleModuleTest::testMultipleModules
    public void testMultipleModules() throws Exception
    {
        MySimpleModule mod1 = new MySimpleModule("test1", Version.unknownVersion());
        SimpleModule mod2 = new SimpleModule("test2", Version.unknownVersion());
        mod1.addSerializer(SimpleEnum.class, new SimpleEnumSerializer());
        mod1.addDeserializer(CustomBean.class, new CustomBeanDeserializer());

        Map<Class<?>,JsonDeserializer<?>> desers = new HashMap<>();
        desers.put(SimpleEnum.class, new SimpleEnumDeserializer());
        mod2.setDeserializers(new SimpleDeserializers(desers));
        mod2.addSerializer(CustomBean.class, new CustomBeanSerializer());

        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(mod1);
        mapper.registerModule(mod2);
        assertEquals(quote("b"), mapper.writeValueAsString(SimpleEnum.B));
        SimpleEnum result = mapper.readValue(quote("a"), SimpleEnum.class);
        assertSame(SimpleEnum.A, result);

        
        mapper = new ObjectMapper();
        mapper.registerModule(mod2);
        mapper.registerModule(mod1);
        assertEquals(quote("b"), mapper.writeValueAsString(SimpleEnum.B));
        result = mapper.readValue(quote("a"), SimpleEnum.class);
        assertSame(SimpleEnum.A, result);
    }

// com.fasterxml.jackson.databind.module.SimpleModuleTest::testGetRegisteredModules
    public void testGetRegisteredModules()
    {
        MySimpleModule mod1 = new MySimpleModule("test1", Version.unknownVersion());
        AnotherSimpleModule mod2 = new AnotherSimpleModule("test2", Version.unknownVersion());

        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(mod1);
        mapper.registerModule(mod2);

        Set<Object> registeredModuleIds = mapper.getRegisteredModuleIds();
        assertEquals(2, registeredModuleIds.size());
        assertTrue(registeredModuleIds.contains(mod1.getTypeId()));
        assertTrue(registeredModuleIds.contains(mod2.getTypeId()));
    }

// com.fasterxml.jackson.databind.module.SimpleModuleTest::testMixIns
    public void testMixIns() throws Exception
    {
        SimpleModule module = new SimpleModule("test", Version.unknownVersion());
        module.setMixInAnnotation(MixableBean.class, MixInForOrder.class);
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(module);
        Map<String,Object> props = this.writeAndMap(mapper, new MixableBean());
        assertEquals(3, props.size());
        assertEquals(Integer.valueOf(3), props.get("c"));
        assertEquals(Integer.valueOf(1), props.get("a"));
        assertEquals(Integer.valueOf(2), props.get("b"));
    }

// com.fasterxml.jackson.databind.module.SimpleModuleTest::testAccessToMapper
    public void testAccessToMapper() throws Exception
    {
        ContextVerifierModule module = new ContextVerifierModule();        
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(module);
    }

// com.fasterxml.jackson.databind.module.SimpleModuleTest::testMixIns626
    public void testMixIns626() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        
        mapper.registerModule(new TestModule626(Object.class, String.class));
        Class<?> found = mapper.findMixInClassFor(Object.class);
        assertEquals(String.class, found);
    }

// com.fasterxml.jackson.databind.module.SimpleModuleTest::testAutoDiscovery
    public void testAutoDiscovery() throws Exception
    {
        List<?> mods = ObjectMapper.findModules();
        assertEquals(0, mods.size());
    }

// com.fasterxml.jackson.databind.module.TestAbstractTypes::testCollectionDefaulting
    public void testCollectionDefaulting() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        SimpleModule mod = new SimpleModule("test", Version.unknownVersion());
        
        mod.addAbstractTypeMapping(Collection.class, List.class);
        mod.addAbstractTypeMapping(List.class, LinkedList.class);
        mapper.registerModule(mod);
        Collection<?> result = mapper.readValue("[]", Collection.class);
        assertEquals(LinkedList.class, result.getClass());
    }

// com.fasterxml.jackson.databind.module.TestAbstractTypes::testMapDefaultingBasic
    public void testMapDefaultingBasic() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        SimpleModule mod = new SimpleModule("test", Version.unknownVersion());
        
        mod.addAbstractTypeMapping(Map.class, TreeMap.class);
        mapper.registerModule(mod);
        Map<?,?> result = mapper.readValue("{}", Map.class);
        assertEquals(TreeMap.class, result.getClass());
    }

// com.fasterxml.jackson.databind.module.TestAbstractTypes::testDefaultingRecursive
    public void testDefaultingRecursive() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        SimpleModule mod = new SimpleModule("test", Version.unknownVersion());

        
        mod.addAbstractTypeMapping(Map.class, TreeMap.class);
        mod.addAbstractTypeMapping(List.class, LinkedList.class);

        mapper.registerModule(mod);
        Object result;

        result = mapper.readValue("[ {} ]", Object.class);
        assertEquals(LinkedList.class, result.getClass());
        Object v = ((List<?>) result).get(0);
        assertNotNull(v);
        assertEquals(TreeMap.class, v.getClass());

        result = mapper.readValue("{ \"x\": [ 3 ] }", Object.class);
        assertEquals(TreeMap.class, result.getClass());
        Map<?,?> map = (Map<?,?>) result;
        assertEquals(1, map.size());
        v = map.get("x");
        assertNotNull(v);
        assertEquals(LinkedList.class, v.getClass());
        assertEquals(1, ((List<?>) v).size());
    }

// com.fasterxml.jackson.databind.module.TestAbstractTypes::testInterfaceDefaulting
    public void testInterfaceDefaulting() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        SimpleModule mod = new SimpleModule("test", Version.unknownVersion());
        
        mod.addAbstractTypeMapping(CharSequence.class, MyString.class);
        mapper.registerModule(mod);
        Object result = mapper.readValue(quote("abc"), CharSequence.class);
        assertEquals(MyString.class, result.getClass());
        assertEquals("abc", ((MyString) result).value);

        
        mod = new SimpleModule();
        mod.addAbstractTypeMapping(Abstract.class, AbstractImpl.class);
        mapper = new ObjectMapper()
                .registerModule(mod);
        Abstract a = mapper.readValue("{}", Abstract.class);
        assertNotNull(a);
    }

// com.fasterxml.jackson.databind.module.TestCustomEnumKeyDeserializer::testWithEnumKeys
    public void testWithEnumKeys() throws Exception {
        ObjectMapper plainObjectMapper = new ObjectMapper();
        JsonNode tree = plainObjectMapper.readTree(aposToQuotes("{'red' : [ 'a', 'b']}"));

        ObjectMapper fancyObjectMapper = new ObjectMapper().registerModule(new TestEnumModule());

        
        Map<TestEnum, Set<String>> map = fancyObjectMapper.convertValue(tree,
                new TypeReference<Map<TestEnum, Set<String>>>() { } );
        assertNotNull(map);
    }

// com.fasterxml.jackson.databind.module.TestCustomEnumKeyDeserializer::testCustomEnumKeySerializerWithPolymorphic
    public void testCustomEnumKeySerializerWithPolymorphic() throws IOException
    {
        SimpleModule simpleModule = new SimpleModule();
        simpleModule.addDeserializer(SuperTypeEnum.class, new JsonDeserializer<SuperTypeEnum>() {
            @Override
            public SuperTypeEnum deserialize(JsonParser p, DeserializationContext deserializationContext)
                    throws IOException
            {
                return SuperTypeEnum.valueOf(p.getText());
            }
        });
        ObjectMapper mapper = new ObjectMapper()
                .registerModule(simpleModule);

        SuperType superType = mapper.readValue("{\"someMap\": {\"FOO\": \"bar\"}}",
                SuperType.class);
        assertEquals("Deserialized someMap.FOO should equal bar", "bar",
                superType.someMap.get(SuperTypeEnum.FOO));
    }

// com.fasterxml.jackson.databind.module.TestCustomEnumKeyDeserializer::testCustomEnumValueAndKeyViaModifier
    public void testCustomEnumValueAndKeyViaModifier() throws IOException
    {
        SimpleModule module = new SimpleModule();
        module.setDeserializerModifier(new BeanDeserializerModifier() {        
            @Override
            public JsonDeserializer<Enum> modifyEnumDeserializer(DeserializationConfig config,
                    final JavaType type, BeanDescription beanDesc,
                    final JsonDeserializer<?> deserializer) {
                return new JsonDeserializer<Enum>() {
                    @Override
                    public Enum deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
                        Class<? extends Enum> rawClass = (Class<Enum<?>>) type.getRawClass();
                        final String str = p.getValueAsString().toLowerCase();
                        return KeyEnum.valueOf(rawClass, str);
                    }
                };
            }

            @Override
            public KeyDeserializer modifyKeyDeserializer(DeserializationConfig config,
                    final JavaType type, KeyDeserializer deserializer)
            {
                if (!type.isEnumType()) {
                    return deserializer;
                }
                return new KeyDeserializer() {
                    @Override
                    public Object deserializeKey(String key, DeserializationContext ctxt)
                            throws IOException
                    {
                        Class<? extends Enum> rawClass = (Class<Enum<?>>) type.getRawClass();
                        return Enum.valueOf(rawClass, key.toLowerCase());
                    }
                };
            }
        });
        ObjectMapper mapper = new ObjectMapper()
                .registerModule(module);

        
        KeyEnum key = mapper.readValue(quote(KeyEnum.replacements.name().toUpperCase()),
                KeyEnum.class);
        assertSame(KeyEnum.replacements, key);

        
        EnumMap<KeyEnum,String> map = mapper.readValue(
                aposToQuotes("{'REPlaceMENTS':'foobar'}"),
                new TypeReference<EnumMap<KeyEnum,String>>() { });
        assertEquals(1, map.size());
        assertSame(KeyEnum.replacements, map.keySet().iterator().next());
    }

// com.fasterxml.jackson.databind.node.ObjectNodeTest::testSimpleObject
    public void testSimpleObject() throws Exception
    {
        String JSON = "{ \"key\" : 1, \"b\" : \"x\" }";
        JsonNode root = MAPPER.readTree(JSON);

        
        assertFalse(root.isValueNode());
        assertTrue(root.isContainerNode());
        assertFalse(root.isArray());
        assertTrue(root.isObject());
        assertEquals(2, root.size());

        Iterator<JsonNode> it = root.iterator();
        assertNotNull(it);
        assertTrue(it.hasNext());
        JsonNode n = it.next();
        assertNotNull(n);
        assertEquals(IntNode.valueOf(1), n);

        assertTrue(it.hasNext());
        n = it.next();
        assertNotNull(n);
        assertEquals(TextNode.valueOf("x"), n);

        assertFalse(it.hasNext());

        
        ObjectNode obNode = (ObjectNode) root;
        Iterator<Map.Entry<String,JsonNode>> fit = obNode.fields();
        
        assertTrue(fit.hasNext());
        Map.Entry<String,JsonNode> en = fit.next();
        assertEquals("key", en.getKey());
        assertEquals(IntNode.valueOf(1), en.getValue());

        assertTrue(fit.hasNext());
        en = fit.next();
        assertEquals("b", en.getKey());
        assertEquals(TextNode.valueOf("x"), en.getValue());

        
        fit.remove();
        assertEquals(1, obNode.size());
        assertEquals(IntNode.valueOf(1), root.get("key"));
        assertNull(root.get("b"));
    }

// com.fasterxml.jackson.databind.node.ObjectNodeTest::testEmptyNodeAsValue
    public void testEmptyNodeAsValue() throws Exception
    {
        Data w = MAPPER.readValue("{}", Data.class);
        assertNotNull(w);
    }

// com.fasterxml.jackson.databind.node.ObjectNodeTest::testBasics
    public void testBasics()
    {
        ObjectNode n = new ObjectNode(JsonNodeFactory.instance);
        assertStandardEquals(n);

        assertFalse(n.elements().hasNext());
        assertFalse(n.fields().hasNext());
        assertFalse(n.fieldNames().hasNext());
        assertNull(n.get("a"));
        assertTrue(n.path("a").isMissingNode());

        TextNode text = TextNode.valueOf("x");
        assertSame(n, n.set("a", text));
        
        assertEquals(1, n.size());
        assertTrue(n.elements().hasNext());
        assertTrue(n.fields().hasNext());
        assertTrue(n.fieldNames().hasNext());
        assertSame(text, n.get("a"));
        assertSame(text, n.path("a"));
        assertNull(n.get("b"));
        assertNull(n.get(0)); 

        assertFalse(n.has(0));
        assertFalse(n.hasNonNull(0));
        assertTrue(n.has("a"));
        assertTrue(n.hasNonNull("a"));
        assertFalse(n.has("b"));
        assertFalse(n.hasNonNull("b"));

        ObjectNode n2 = new ObjectNode(JsonNodeFactory.instance);
        n2.put("b", 13);
        assertFalse(n.equals(n2));
        n.setAll(n2);
        
        assertEquals(2, n.size());
        n.set("null", (JsonNode)null);
        assertEquals(3, n.size());
        
        assertTrue(n.has("null"));
        assertFalse(n.hasNonNull("null"));
        
        n.put("null", "notReallNull");
        assertEquals(3, n.size());
        assertNotNull(n.remove("null"));
        assertEquals(2, n.size());

        Map<String,JsonNode> nodes = new HashMap<String,JsonNode>();
        nodes.put("d", text);
        n.setAll(nodes);
        assertEquals(3, n.size());

        n.removeAll();
        assertEquals(0, n.size());
    }

// com.fasterxml.jackson.databind.node.ObjectNodeTest::testBigNumbers
    public void testBigNumbers()
    {
        ObjectNode n = new ObjectNode(JsonNodeFactory.instance);
        assertStandardEquals(n);
        BigInteger I = BigInteger.valueOf(3);
        BigDecimal DEC = new BigDecimal("0.1");

        n.put("a", DEC);
        n.put("b", I);

        assertEquals(2, n.size());

        assertTrue(n.path("a").isBigDecimal());
        assertEquals(DEC, n.get("a").decimalValue());
        assertTrue(n.path("b").isBigInteger());
        assertEquals(I, n.get("b").bigIntegerValue());
    }

// com.fasterxml.jackson.databind.node.ObjectNodeTest::testNullChecking
    public void testNullChecking()
    {
        ObjectNode o1 = JsonNodeFactory.instance.objectNode();
        ObjectNode o2 = JsonNodeFactory.instance.objectNode();
        
        o1.setAll(o2);
        assertEquals(0, o1.size());
        assertEquals(0, o2.size());

        
        o1.set("x", null);
        JsonNode n = o1.get("x");
        assertNotNull(n);
        assertSame(n, NullNode.instance);

        o1.put("str", (String) null);
        n = o1.get("str");
        assertNotNull(n);
        assertSame(n, NullNode.instance);

        o1.put("d", (BigDecimal) null);
        n = o1.get("d");
        assertNotNull(n);
        assertSame(n, NullNode.instance);

        o1.put("3", (BigInteger) null);
        n = o1.get("3");
        assertNotNull(3);
        assertSame(n, NullNode.instance);

        assertEquals(4, o1.size());
    }

// com.fasterxml.jackson.databind.node.ObjectNodeTest::testNullChecking2
    public void testNullChecking2()
    {
        ObjectNode src = MAPPER.createObjectNode();
        ObjectNode dest = MAPPER.createObjectNode();
        src.put("a", "b");
        dest.setAll(src);
    }

// com.fasterxml.jackson.databind.node.ObjectNodeTest::testRemove
    public void testRemove()
    {
        ObjectNode ob = MAPPER.createObjectNode();
        ob.put("a", "a");
        ob.put("b", "b");
        ob.put("c", "c");
        assertEquals(3, ob.size());
        assertSame(ob, ob.without(Arrays.asList("a", "c")));
        assertEquals(1, ob.size());
        assertEquals("b", ob.get("b").textValue());
    }

// com.fasterxml.jackson.databind.node.ObjectNodeTest::testRetain
    public void testRetain()
    {
        ObjectNode ob = MAPPER.createObjectNode();
        ob.put("a", "a");
        ob.put("b", "b");
        ob.put("c", "c");
        assertEquals(3, ob.size());
        assertSame(ob, ob.retain("a", "c"));
        assertEquals(2, ob.size());
        assertEquals("a", ob.get("a").textValue());
        assertNull(ob.get("b"));
        assertEquals("c", ob.get("c").textValue());
    }

// com.fasterxml.jackson.databind.node.ObjectNodeTest::testValidWith
    public void testValidWith() throws Exception
    {
        ObjectNode root = MAPPER.createObjectNode();
        assertEquals("{}", MAPPER.writeValueAsString(root));
        JsonNode child = root.with("prop");
        assertTrue(child instanceof ObjectNode);
        assertEquals("{\"prop\":{}}", MAPPER.writeValueAsString(root));
    }

// com.fasterxml.jackson.databind.node.ObjectNodeTest::testValidWithArray
    public void testValidWithArray() throws Exception
    {
        ObjectNode root = MAPPER.createObjectNode();
        assertEquals("{}", MAPPER.writeValueAsString(root));
        JsonNode child = root.withArray("arr");
        assertTrue(child instanceof ArrayNode);
        assertEquals("{\"arr\":[]}", MAPPER.writeValueAsString(root));
    }

// com.fasterxml.jackson.databind.node.ObjectNodeTest::testInvalidWith
    public void testInvalidWith() throws Exception
    {
        JsonNode root = MAPPER.createArrayNode();
        try { 
            root.with("prop");
            fail("Expected exception");
        } catch (UnsupportedOperationException e) {
            verifyException(e, "not of type ObjectNode");
        }
        
        ObjectNode root2 = MAPPER.createObjectNode();
        root2.put("prop", 13);
        try { 
            root2.with("prop");
            fail("Expected exception");
        } catch (UnsupportedOperationException e) {
            verifyException(e, "has value that is not");
        }
    }

// com.fasterxml.jackson.databind.node.ObjectNodeTest::testInvalidWithArray
    public void testInvalidWithArray() throws Exception
    {
        JsonNode root = MAPPER.createArrayNode();
        try { 
            root.withArray("prop");
            fail("Expected exception");
        } catch (UnsupportedOperationException e) {
            verifyException(e, "not of type ObjectNode");
        }
        
        ObjectNode root2 = MAPPER.createObjectNode();
        root2.put("prop", 13);
        try { 
            root2.withArray("prop");
            fail("Expected exception");
        } catch (UnsupportedOperationException e) {
            verifyException(e, "has value that is not");
        }
    }

// com.fasterxml.jackson.databind.node.ObjectNodeTest::testSetAll
    public void testSetAll() throws Exception
    {
        ObjectNode root = MAPPER.createObjectNode();
        assertEquals(0, root.size());
        HashMap<String,JsonNode> map = new HashMap<String,JsonNode>();
        map.put("a", root.numberNode(1));
        root.setAll(map);
        assertEquals(1, root.size());
        assertTrue(root.has("a"));
        assertFalse(root.has("b"));

        map.put("b", root.numberNode(2));
        root.setAll(map);
        assertEquals(2, root.size());
        assertTrue(root.has("a"));
        assertTrue(root.has("b"));
        assertEquals(2, root.path("b").intValue());

        
        ObjectNode root2 = MAPPER.createObjectNode();
        root2.setAll(root);
        assertEquals(2, root.size());
        assertEquals(2, root2.size());

        root2.setAll(root);
        assertEquals(2, root.size());
        assertEquals(2, root2.size());

        ObjectNode root3 = MAPPER.createObjectNode();
        root3.put("a", 2);
        root3.put("c", 3);
        assertEquals(2, root3.path("a").intValue());
        root3.setAll(root2);
        assertEquals(3, root3.size());
        assertEquals(1, root3.path("a").intValue());
    }

// com.fasterxml.jackson.databind.node.ObjectNodeTest::testFailOnDupKeys
    public void testFailOnDupKeys() throws Exception
    {
        final String DUP_JSON = "{ \"a\":1, \"a\":2 }";
        
        
        ObjectMapper mapper = new ObjectMapper();
        assertFalse(mapper.isEnabled(DeserializationFeature.FAIL_ON_READING_DUP_TREE_KEY));
        ObjectNode root = (ObjectNode) mapper.readTree(DUP_JSON);
        assertEquals(2, root.path("a").asInt());
        
        
        try {
            mapper.reader(DeserializationFeature.FAIL_ON_READING_DUP_TREE_KEY).readTree(DUP_JSON);
            fail("Should have thrown exception!");
        } catch (JsonMappingException e) {
            verifyException(e, "duplicate field 'a'");
        }
    }

// com.fasterxml.jackson.databind.node.ObjectNodeTest::testEqualityWrtOrder
    public void testEqualityWrtOrder() throws Exception
    {
        ObjectNode ob1 = MAPPER.createObjectNode();
        ObjectNode ob2 = MAPPER.createObjectNode();

        
        
        ob1.put("a", 1);
        ob1.put("b", 2);
        ob1.put("c", 3);

        ob2.put("b", 2);
        ob2.put("c", 3);
        ob2.put("a", 1);

        assertTrue(ob1.equals(ob2));
        assertTrue(ob2.equals(ob1));
    }

// com.fasterxml.jackson.databind.node.ObjectNodeTest::testSimplePath
    public void testSimplePath() throws Exception
    {
        JsonNode root = MAPPER.readTree("{ \"results\" : { \"a\" : 3 } }");
        assertTrue(root.isObject());
        JsonNode rnode = root.path("results");
        assertNotNull(rnode);
        assertTrue(rnode.isObject());
        assertEquals(3, rnode.path("a").intValue());
    }

// com.fasterxml.jackson.databind.node.ObjectNodeTest::testNonEmptySerialization
    public void testNonEmptySerialization() throws Exception
    {
        ObNodeWrapper w = new ObNodeWrapper(MAPPER.createObjectNode()
                .put("a", 3));
        assertEquals("{\"node\":{\"a\":3}}", MAPPER.writeValueAsString(w));
        w = new ObNodeWrapper(MAPPER.createObjectNode());
        assertEquals("{}", MAPPER.writeValueAsString(w));
    }

// com.fasterxml.jackson.databind.node.ObjectNodeTest::testIssue941
    public void testIssue941() throws Exception
    {
        ObjectNode object = MAPPER.createObjectNode();

        String json = MAPPER.writeValueAsString(object);

        ObjectNode de1 = MAPPER.readValue(json, ObjectNode.class);  

        assertNotNull(de1);

        MyValue de2 = MAPPER.readValue(json, MyValue.class);  

        assertNotNull(de2);
    }

// com.fasterxml.jackson.databind.node.ObjectNodeTest::testSimpleMismatch
    public void testSimpleMismatch() throws Exception
    {
        ObjectMapper mapper = objectMapper();
        try {
            mapper.readValue("[ 1, 2, 3 ]", ObjectNode.class);
            fail("Should not pass");
        } catch (MismatchedInputException e) {
            verifyException(e, "out of START_ARRAY token");
        }
    }

// com.fasterxml.jackson.databind.node.TestConversions::testAsInt
    public void testAsInt() throws Exception
    {
        assertEquals(9, IntNode.valueOf(9).asInt());
        assertEquals(7, LongNode.valueOf(7L).asInt());
        assertEquals(13, new TextNode("13").asInt());
        assertEquals(0, new TextNode("foobar").asInt());
        assertEquals(27, new TextNode("foobar").asInt(27));
        assertEquals(1, BooleanNode.TRUE.asInt());
    }

// com.fasterxml.jackson.databind.node.TestConversions::testAsBoolean
    public void testAsBoolean() throws Exception
    {
        assertEquals(false, BooleanNode.FALSE.asBoolean());
        assertEquals(true, BooleanNode.TRUE.asBoolean());
        assertEquals(false, IntNode.valueOf(0).asBoolean());
        assertEquals(true, IntNode.valueOf(1).asBoolean());
        assertEquals(false, LongNode.valueOf(0).asBoolean());
        assertEquals(true, LongNode.valueOf(-34L).asBoolean());
        assertEquals(true, new TextNode("true").asBoolean());
        assertEquals(false, new TextNode("false").asBoolean());
        assertEquals(false, new TextNode("barf").asBoolean());
        assertEquals(true, new TextNode("barf").asBoolean(true));

        assertEquals(true, new POJONode(Boolean.TRUE).asBoolean());
    }

// com.fasterxml.jackson.databind.node.TestConversions::testTreeToValue
    public void testTreeToValue() throws Exception
    {
        String JSON = "{\"leaf\":{\"value\":13}}";
        ObjectMapper mapper = new ObjectMapper();
        mapper.addMixIn(Leaf.class, LeafMixIn.class);
        JsonNode root = mapper.readTree(JSON);
        
        Root r1 = mapper.treeToValue(root, Root.class);
        assertNotNull(r1);
        assertEquals(13, r1.leaf.value);
    }

// com.fasterxml.jackson.databind.node.TestConversions::testTreeToValueWithPOJO
    public void testTreeToValueWithPOJO() throws Exception
    {
        Calendar c = Calendar.getInstance();
        c.setTime(new java.util.Date(0));
        ValueNode pojoNode = MAPPER.getNodeFactory().pojoNode(c);        
        Calendar result = MAPPER.treeToValue(pojoNode, Calendar.class);
        assertNotNull(result);
        assertEquals(result.getTimeInMillis(), c.getTimeInMillis());
    }

// com.fasterxml.jackson.databind.node.TestConversions::testBase64Text
    public void testBase64Text() throws Exception
    {
        
        
        final int[] LENS = { 1, 2, 3, 4, 7, 9, 32, 33, 34, 35 };
        final Base64Variant[] VARIANTS = {
                Base64Variants.MIME,
                Base64Variants.MIME_NO_LINEFEEDS,
                Base64Variants.MODIFIED_FOR_URL,
                Base64Variants.PEM
        };

        for (int len : LENS) {
            byte[] input = new byte[len];
            for (int i = 0; i < input.length; ++i) {
                input[i] = (byte) i;
            }
            for (Base64Variant variant : VARIANTS) {
                TextNode n = new TextNode(variant.encode(input));
                byte[] data = null;
                try {
                    data = n.getBinaryValue(variant);
                } catch (Exception e) {
                    fail("Failed (variant "+variant+", data length "+len+"): "+e.getMessage());
                }
                assertNotNull(data);
                assertArrayEquals(data, input);

                
                JsonParser p = new TreeTraversingParser(n);
                assertEquals(JsonToken.VALUE_STRING, p.nextToken());
                try {
                    data = p.getBinaryValue(variant);
                } catch (Exception e) {
                    fail("Failed (variant "+variant+", data length "+len+"): "+e.getMessage());
                }
                assertNotNull(data);
                assertArrayEquals(data, input);
                p.close();
            }
        }
    }

// com.fasterxml.jackson.databind.node.TestConversions::testIssue709
    public void testIssue709() throws Exception
    {
        byte[] inputData = new byte[] { 1, 2, 3 };
        ObjectNode node = MAPPER.createObjectNode();
        node.put("data", inputData);
        Issue709Bean result = MAPPER.treeToValue(node, Issue709Bean.class);
        String json = MAPPER.writeValueAsString(node);
        Issue709Bean resultFromString = MAPPER.readValue(json, Issue709Bean.class);
        Issue709Bean resultFromConvert = MAPPER.convertValue(node, Issue709Bean.class);
        
        
        Assert.assertArrayEquals(inputData, resultFromString.data);
        Assert.assertArrayEquals(inputData, resultFromConvert.data);
        Assert.assertArrayEquals(inputData, result.data);
    }

// com.fasterxml.jackson.databind.node.TestConversions::testEmbeddedByteArray
    public void testEmbeddedByteArray() throws Exception
    {
        TokenBuffer buf = new TokenBuffer(MAPPER, false);
        buf.writeObject(new byte[3]);
        JsonNode node = MAPPER.readTree(buf.asParser());
        buf.close();
        assertTrue(node.isBinary());
        byte[] data = node.binaryValue();
        assertNotNull(data);
        assertEquals(3, data.length);
    }

// com.fasterxml.jackson.databind.node.TestConversions::testBigDecimalAsPlainStringTreeConversion
    public void testBigDecimalAsPlainStringTreeConversion() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(JsonGenerator.Feature.WRITE_BIGDECIMAL_AS_PLAIN);
        Map<String, Object> map = new HashMap<String, Object>();
        String PI_STR = "3.00000000";
        map.put("pi", new BigDecimal(PI_STR));
        JsonNode tree = mapper.valueToTree(map);
        assertNotNull(tree);
        assertEquals(1, tree.size());
        assertTrue(tree.has("pi"));
    }

// com.fasterxml.jackson.databind.node.TestConversions::testBeanToTree
    public void testBeanToTree() throws Exception
    {
        final CustomSerializedPojo pojo = new CustomSerializedPojo();
        pojo.setFoo("bar");
        final JsonNode node = MAPPER.valueToTree(pojo);
        assertEquals(JsonNodeType.OBJECT, node.getNodeType());
    }

// com.fasterxml.jackson.databind.node.TestConversions::testConversionOfPojos
    public void testConversionOfPojos() throws Exception
    {
        final Issue467Bean input = new Issue467Bean(13);
        final String EXP = "{\"x\":13}";
        
        
        String json = MAPPER.writeValueAsString(input);
        assertEquals(EXP, json);

        
        JsonNode tree = MAPPER.valueToTree(input);
        assertTrue("Expected Object, got "+tree.getNodeType(), tree.isObject());
        assertEquals(EXP, MAPPER.writeValueAsString(tree));
    }

// com.fasterxml.jackson.databind.node.TestConversions::testConversionOfTrees
    public void testConversionOfTrees() throws Exception
    {
        final Issue467Tree input = new Issue467Tree();
        final String EXP = "true";

        
        String json = MAPPER.writeValueAsString(input);
        assertEquals(EXP, json);

        
        JsonNode tree = MAPPER.valueToTree(input);
        assertTrue("Expected Object, got "+tree.getNodeType(), tree.isBoolean());
        assertEquals(EXP, MAPPER.writeValueAsString(tree));
    }

// com.fasterxml.jackson.databind.node.TestConversions::testBufferedLongViaCoercion
    public void testBufferedLongViaCoercion() throws Exception {
        long EXP = 1519348261000L;
        JsonNode tree = MAPPER.readTree("{\"longObj\": "+EXP+".0, \"_class\": \""+LongContainer1940.class.getName()+"\"}");
        LongContainer1940 obj = MAPPER.treeToValue(tree, LongContainer1940.class);
        assertEquals(Long.valueOf(EXP), obj.longObj);
    }

// com.fasterxml.jackson.databind.node.TestNullNode::testBasicsWithNullNode
    public void testBasicsWithNullNode() throws Exception
    {
        
        NullNode n = NullNode.instance;

        
        assertFalse(n.isContainerNode());
        assertFalse(n.isBigDecimal());
        assertFalse(n.isBigInteger());
        assertFalse(n.isBinary());
        assertFalse(n.isBoolean());
        assertFalse(n.isPojo());
        assertFalse(n.isMissingNode());

        
        assertFalse(n.booleanValue());
        assertNull(n.numberValue());
        assertEquals(0, n.intValue());
        assertEquals(0L, n.longValue());
        assertEquals(BigDecimal.ZERO, n.decimalValue());
        assertEquals(BigInteger.ZERO, n.bigIntegerValue());

        assertEquals(0, n.size());
        assertFalse(n.elements().hasNext());
        assertFalse(n.fieldNames().hasNext());
        
        assertNotNull(n.path("xyz"));
        assertTrue(n.path("xyz").isMissingNode());

        assertFalse(n.has("field"));
        assertFalse(n.has(3));

        assertNodeNumbersForNonNumeric(n);

        
        assertEquals("foo", n.asText("foo"));
    }

// com.fasterxml.jackson.databind.node.TestNullNode::testNullHandling
    public void testNullHandling() throws Exception
    {
        
        JsonNode n = objectReader().readTree("null");
        assertNotNull(n);
        assertTrue(n.isNull());
        assertFalse(n.isNumber());
        assertFalse(n.isTextual());
        assertEquals("null", n.asText());
        assertEquals(n, NullNode.instance);

        n = objectMapper().readTree("null");
        assertNotNull(n);
        assertTrue(n.isNull());
        
        
        ObjectNode root = (ObjectNode) objectReader().readTree("{\"x\":null}");
        assertEquals(1, root.size());
        n = root.get("x");
        assertNotNull(n);
        assertTrue(n.isNull());
    }

// com.fasterxml.jackson.databind.node.TestNullNode::testNullSerialization
    public void testNullSerialization() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        StringWriter sw = new StringWriter();
        mapper.writeValue(sw, NullNode.instance);
        assertEquals("null", sw.toString());
    }

// com.fasterxml.jackson.databind.node.TestNullNode::testNullHandlingCovariance
    public void testNullHandlingCovariance() throws Exception
    {
        String JSON = "{\"object\" : null, \"array\" : null }";
        CovarianceBean bean = objectMapper().readValue(JSON, CovarianceBean.class);

        ObjectNode on = bean._object;
        assertNull(on);

        ArrayNode an = bean._array;
        assertNull(an);
    }

// com.fasterxml.jackson.databind.node.TestTreeTraversingParser::testSimple
    public void testSimple() throws Exception
    {
        
        final String JSON =
            "{ \"a\" : 123, \"list\" : [ 12.25, null, true, { }, [ ] ] }";
        ObjectMapper m = new ObjectMapper();
        JsonNode tree = m.readTree(JSON);
        JsonParser p = tree.traverse();

        assertNull(p.getCurrentToken());
        assertNull(p.getCurrentName());

        assertToken(JsonToken.START_OBJECT, p.nextToken());
        assertNull(p.getCurrentName());
        assertEquals("Expected START_OBJECT", JsonToken.START_OBJECT.asString(), p.getText());

        assertToken(JsonToken.FIELD_NAME, p.nextToken());
        assertEquals("a", p.getCurrentName());
        assertEquals("a", p.getText());

        assertToken(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertEquals("a", p.getCurrentName());
        assertEquals(123, p.getIntValue());
        assertEquals("123", p.getText());

        assertToken(JsonToken.FIELD_NAME, p.nextToken());
        assertEquals("list", p.getCurrentName());
        assertEquals("list", p.getText());

        assertToken(JsonToken.START_ARRAY, p.nextToken());
        assertEquals("list", p.getCurrentName());
        assertEquals(JsonToken.START_ARRAY.asString(), p.getText());

        assertToken(JsonToken.VALUE_NUMBER_FLOAT, p.nextToken());
        assertNull(p.getCurrentName());
        assertEquals(12.25, p.getDoubleValue(), 0);
        assertEquals("12.25", p.getText());

        assertToken(JsonToken.VALUE_NULL, p.nextToken());
        assertNull(p.getCurrentName());
        assertEquals(JsonToken.VALUE_NULL.asString(), p.getText());

        assertToken(JsonToken.VALUE_TRUE, p.nextToken());
        assertNull(p.getCurrentName());
        assertTrue(p.getBooleanValue());
        assertEquals(JsonToken.VALUE_TRUE.asString(), p.getText());

        assertToken(JsonToken.START_OBJECT, p.nextToken());
        assertNull(p.getCurrentName());
        assertToken(JsonToken.END_OBJECT, p.nextToken());
        assertNull(p.getCurrentName());

        assertToken(JsonToken.START_ARRAY, p.nextToken());
        assertNull(p.getCurrentName());
        assertToken(JsonToken.END_ARRAY, p.nextToken());
        assertNull(p.getCurrentName());

        assertToken(JsonToken.END_ARRAY, p.nextToken());

        assertToken(JsonToken.END_OBJECT, p.nextToken());
        assertNull(p.getCurrentName());

        assertNull(p.nextToken());

        p.close();
        assertTrue(p.isClosed());
    }

// com.fasterxml.jackson.databind.node.TestTreeTraversingParser::testArray
    public void testArray() throws Exception
    {
        
        ObjectMapper m = new ObjectMapper();

        JsonParser p = m.readTree("[]").traverse();
        assertToken(JsonToken.START_ARRAY, p.nextToken());
        assertToken(JsonToken.END_ARRAY, p.nextToken());
        p.close();

        p = m.readTree("[[]]").traverse();
        assertToken(JsonToken.START_ARRAY, p.nextToken());
        assertToken(JsonToken.START_ARRAY, p.nextToken());
        assertToken(JsonToken.END_ARRAY, p.nextToken());
        assertToken(JsonToken.END_ARRAY, p.nextToken());
        p.close();

        p = m.readTree("[[ 12.1 ]]").traverse();
        assertToken(JsonToken.START_ARRAY, p.nextToken());
        assertToken(JsonToken.START_ARRAY, p.nextToken());
        assertToken(JsonToken.VALUE_NUMBER_FLOAT, p.nextToken());
        assertToken(JsonToken.END_ARRAY, p.nextToken());
        assertToken(JsonToken.END_ARRAY, p.nextToken());
        p.close();
    }

// com.fasterxml.jackson.databind.node.TestTreeTraversingParser::testNested
    public void testNested() throws Exception
    {
        
        final String JSON =
            "{\"coordinates\":[[[-3,\n1],[179.859681,51.175092]]]}"
            ;
        ObjectMapper m = new ObjectMapper();
        JsonNode tree = m.readTree(JSON);
        JsonParser p = tree.traverse();
        assertToken(JsonToken.START_OBJECT, p.nextToken());
        assertToken(JsonToken.FIELD_NAME, p.nextToken());

        assertToken(JsonToken.START_ARRAY, p.nextToken());
        assertToken(JsonToken.START_ARRAY, p.nextToken());

        assertToken(JsonToken.START_ARRAY, p.nextToken());
        assertToken(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertToken(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertToken(JsonToken.END_ARRAY, p.nextToken());

        assertToken(JsonToken.START_ARRAY, p.nextToken());
        assertToken(JsonToken.VALUE_NUMBER_FLOAT, p.nextToken());
        assertToken(JsonToken.VALUE_NUMBER_FLOAT, p.nextToken());
        assertToken(JsonToken.END_ARRAY, p.nextToken());
        
        assertToken(JsonToken.END_ARRAY, p.nextToken());
        assertToken(JsonToken.END_ARRAY, p.nextToken());

        assertToken(JsonToken.END_OBJECT, p.nextToken());
        p.close();
    }

// com.fasterxml.jackson.databind.node.TestTreeTraversingParser::testSpecDoc
    public void testSpecDoc() throws Exception
    {
        ObjectMapper m = new ObjectMapper();
        JsonNode tree = m.readTree(SAMPLE_DOC_JSON_SPEC);
        JsonParser p = tree.traverse();
        verifyJsonSpecSampleDoc(p, true);
        p.close();
    }

// com.fasterxml.jackson.databind.node.TestTreeTraversingParser::testBinaryPojo
    public void testBinaryPojo() throws Exception
    {
        byte[] inputBinary = new byte[] { 1, 2, 100 };
        POJONode n = new POJONode(inputBinary);
        JsonParser p = n.traverse();

        assertNull(p.getCurrentToken());
        assertToken(JsonToken.VALUE_EMBEDDED_OBJECT, p.nextToken());
        byte[] data = p.getBinaryValue();
        assertNotNull(data);
        assertArrayEquals(inputBinary, data);
        Object pojo = p.getEmbeddedObject();
        assertSame(data, pojo);
        p.close();
    }

// com.fasterxml.jackson.databind.node.TestTreeTraversingParser::testBinaryNode
    public void testBinaryNode() throws Exception
    {
        byte[] inputBinary = new byte[] { 0, -5 };
        BinaryNode n = new BinaryNode(inputBinary);
        JsonParser p = n.traverse();

        assertNull(p.getCurrentToken());
        
        assertToken(JsonToken.VALUE_EMBEDDED_OBJECT, p.nextToken());
        byte[] data = p.getBinaryValue();
        assertNotNull(data);
        assertArrayEquals(inputBinary, data);

        
        assertEquals("APs=", p.getText());

        assertNull(p.nextToken());
        p.close();
    }

// com.fasterxml.jackson.databind.node.TestTreeTraversingParser::testTextAsBinary
    public void testTextAsBinary() throws Exception
    {
        TextNode n = new TextNode("   APs=\n");
        JsonParser p = n.traverse();
        assertNull(p.getCurrentToken());
        assertToken(JsonToken.VALUE_STRING, p.nextToken());
        byte[] data = p.getBinaryValue();
        assertNotNull(data);
        assertArrayEquals(new byte[] { 0, -5 }, data);

        assertNull(p.nextToken());
        p.close();
        assertTrue(p.isClosed());

        
        n = new TextNode("?!??");
        p = n.traverse();
        assertToken(JsonToken.VALUE_STRING, p.nextToken());
        try {
            p.getBinaryValue();
        } catch (InvalidFormatException e) {
            verifyException(e, "Illegal character");
        }
        p.close();
    }

// com.fasterxml.jackson.databind.node.TestTreeTraversingParser::testDataBind
    public void testDataBind() throws Exception
    {
        ObjectMapper m = new ObjectMapper();
        JsonNode tree = m.readTree
            ("{ \"name\" : \"Tatu\", \n"
             +"\"magicNumber\" : 42,"
             +"\"kids\" : [ \"Leo\", \"Lila\", \"Leia\" ] \n"
             +"}");
        Person tatu = m.treeToValue(tree, Person.class);
        assertNotNull(tatu);
        assertEquals(42, tatu.magicNumber);
        assertEquals("Tatu", tatu.name);
        assertNotNull(tatu.kids);
        assertEquals(3, tatu.kids.size());
        assertEquals("Leo", tatu.kids.get(0));
        assertEquals("Lila", tatu.kids.get(1));
        assertEquals("Leia", tatu.kids.get(2));
    }

// com.fasterxml.jackson.databind.node.TestTreeTraversingParser::testSkipChildrenWrt370
    public void testSkipChildrenWrt370() throws Exception
    {
        ObjectMapper o = new ObjectMapper();
        ObjectNode n = o.createObjectNode();
        n.putObject("inner").put("value", "test");
        n.putObject("unknown").putNull("inner");
        Jackson370Bean obj = o.readValue(n.traverse(), Jackson370Bean.class);
        assertNotNull(obj.inner);
        assertEquals("test", obj.inner.value);        
    }

// com.fasterxml.jackson.databind.node.TreeReadViaMapperTest::testSimple
    public void testSimple() throws Exception
    {
        final String JSON = SAMPLE_DOC_JSON_SPEC;

        for (int type = 0; type < 2; ++type) {
            JsonNode result;

            if (type == 0) {
                result = objectMapper().readTree(new StringReader(JSON));
            } else {
                result = objectMapper().readTree(JSON);
            }

            assertType(result, ObjectNode.class);
            assertEquals(1, result.size());
            assertTrue(result.isObject());
            
            ObjectNode main = (ObjectNode) result;
            assertEquals("Image", main.fieldNames().next());
            JsonNode ob = main.elements().next();
            assertType(ob, ObjectNode.class);
            ObjectNode imageMap = (ObjectNode) ob;
            
            assertEquals(5, imageMap.size());
            ob = imageMap.get("Width");
            assertTrue(ob.isIntegralNumber());
            assertFalse(ob.isFloatingPointNumber());
            assertEquals(SAMPLE_SPEC_VALUE_WIDTH, ob.intValue());
            ob = imageMap.get("Height");
            assertTrue(ob.isIntegralNumber());
            assertEquals(SAMPLE_SPEC_VALUE_HEIGHT, ob.intValue());
            
            ob = imageMap.get("Title");
            assertTrue(ob.isTextual());
            assertEquals(SAMPLE_SPEC_VALUE_TITLE, ob.textValue());
            
            ob = imageMap.get("Thumbnail");
            assertType(ob, ObjectNode.class);
            ObjectNode tn = (ObjectNode) ob;
            ob = tn.get("Url");
            assertTrue(ob.isTextual());
            assertEquals(SAMPLE_SPEC_VALUE_TN_URL, ob.textValue());
            ob = tn.get("Height");
            assertTrue(ob.isIntegralNumber());
            assertEquals(SAMPLE_SPEC_VALUE_TN_HEIGHT, ob.intValue());
            ob = tn.get("Width");
            assertTrue(ob.isTextual());
            assertEquals(SAMPLE_SPEC_VALUE_TN_WIDTH, ob.textValue());
            
            ob = imageMap.get("IDs");
            assertTrue(ob.isArray());
            ArrayNode idList = (ArrayNode) ob;
            assertEquals(4, idList.size());
            assertEquals(4, calcLength(idList.elements()));
            assertEquals(4, calcLength(idList.iterator()));
            {
                int[] values = new int[] {
                    SAMPLE_SPEC_VALUE_TN_ID1,
                    SAMPLE_SPEC_VALUE_TN_ID2,
                    SAMPLE_SPEC_VALUE_TN_ID3,
                    SAMPLE_SPEC_VALUE_TN_ID4
                };
                for (int i = 0; i < values.length; ++i) {
                    assertEquals(values[i], idList.get(i).intValue());
                }
                int i = 0;
                for (JsonNode n : idList) {
                    assertEquals(values[i], n.intValue());
                    ++i;
                }
            }
        }
    }

// com.fasterxml.jackson.databind.node.TreeReadViaMapperTest::testMixed
    public void testMixed() throws IOException
    {
        ObjectMapper om = new ObjectMapper();
        String JSON = "{\"node\" : { \"a\" : 3 }, \"x\" : 9 }";
        Bean bean = om.readValue(JSON, Bean.class);

        assertEquals(9, bean._x);
        JsonNode n = bean._node;
        assertNotNull(n);
        assertEquals(1, n.size());
        ObjectNode on = (ObjectNode) n;
        assertEquals(3, on.get("a").intValue());
    }

// com.fasterxml.jackson.databind.node.TreeReadViaMapperTest::testEOF
    public void testEOF() throws Exception
    {
        String JSON =
            "{ \"key\": [ { \"a\" : { \"name\": \"foo\",  \"type\": 1\n"
            +"},  \"type\": 3, \"url\": \"http://www.google.com\" } ],\n"
            +"\"name\": \"xyz\", \"type\": 1, \"url\" : null }\n  "
            ;
        JsonFactory jf = new JsonFactory();
        JsonParser p = jf.createParser(new StringReader(JSON));
        JsonNode result = objectMapper().readTree(p);

        assertTrue(result.isObject());
        assertEquals(4, result.size());

        assertNull(objectMapper().readTree(p));
        p.close();
    }

// com.fasterxml.jackson.databind.node.TreeReadViaMapperTest::testMultiple
    public void testMultiple() throws Exception
    {
        String JSON = "12  \"string\" [ 1, 2, 3 ]";
        JsonFactory jf = new JsonFactory();
        JsonParser p = jf.createParser(new StringReader(JSON));
        final ObjectMapper mapper = objectMapper();
        JsonNode result = mapper.readTree(p);

        assertTrue(result.isIntegralNumber());
        assertTrue(result.isInt());
        assertFalse(result.isTextual());
        assertEquals(12, result.intValue());

        result = mapper.readTree(p);
        assertTrue(result.isTextual());
        assertFalse(result.isIntegralNumber());
        assertFalse(result.isInt());
        assertEquals("string", result.textValue());

        result = mapper.readTree(p);
        assertTrue(result.isArray());
        assertEquals(3, result.size());

        assertNull(mapper.readTree(p));
        p.close();
    }

// com.fasterxml.jackson.databind.node.TreeReadViaMapperTest::testNullFromEOFViaMapper
    public void testNullFromEOFViaMapper() throws Exception
    {
        final ObjectMapper mapper = objectMapper();

        assertNull(mapper.readTree(new StringReader("")));
        assertNull(mapper.readTree(new ByteArrayInputStream(new byte[0])));
    }

// com.fasterxml.jackson.databind.node.TreeReadViaMapperTest::testNullFromEOFViaObjectReader
    public void testNullFromEOFViaObjectReader() throws Exception
    {
        final ObjectMapper mapper = objectMapper();

        assertNull(mapper.readTree(new StringReader("")));
        assertNull(mapper.readTree(new ByteArrayInputStream(new byte[0])));
        assertNull(mapper.readerFor(JsonNode.class)
                .readTree(new StringReader("")));
        assertNull(mapper.readerFor(JsonNode.class)
                .readTree(new ByteArrayInputStream(new byte[0])));
    }

// com.fasterxml.jackson.databind.objectid.AlwaysAsReferenceFirstTest::testIssue1255
    public void testIssue1255() throws Exception
    {
        Foo mo = new Foo();
        mo.bar1 = new Bar();
        mo.bar2 = mo.bar1;

        String json = MAPPER.writeValueAsString(mo);

        Foo result = MAPPER.readValue(json, Foo.class);
        assertNotNull(result);
    }

// com.fasterxml.jackson.databind.objectid.AlwaysAsReferenceFirstTest::testIssue1607
    public void testIssue1607() throws Exception
    {
        String json = MAPPER.writeValueAsString(new ReallyAlwaysContainer());
        assertEquals(aposToQuotes("{'alwaysClass':1,'alwaysProp':2}"), json);
    }

// com.fasterxml.jackson.databind.objectid.JSOGDeserialize622Test::testStructJSOGRef
    public void testStructJSOGRef() throws Exception
    {
        IdentifiableExampleJSOG result = MAPPER.readValue(EXP_EXAMPLE_JSOG,
                IdentifiableExampleJSOG.class);
        assertEquals(66, result.foo);
        assertSame(result, result.next);
    }

// com.fasterxml.jackson.databind.objectid.JSOGDeserialize622Test::testPolymorphicRoundTrip
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

// com.fasterxml.jackson.databind.objectid.JSOGDeserialize622Test::testAlterativePolymorphicRoundTrip669
    public void testAlterativePolymorphicRoundTrip669() throws Exception
    {
        Outer outer = new Outer();
        outer.foo = "foo";
        outer.inner1 = outer.inner2 = new SubInner("bar", "extra");

        String jsog = MAPPER.writeValueAsString(outer);
        
        Outer back = MAPPER.readValue(jsog, Outer.class);

        assertSame(back.inner1, back.inner2);
    }

// com.fasterxml.jackson.databind.objectid.ObjectId687Test::testSerializeDeserializeWithCreator
    public void testSerializeDeserializeWithCreator() throws IOException {
        ReferredWithCreator base = new ReferredWithCreator("label1");
        ReferringToObjWithCreator r = new ReferringToObjWithCreator();
        r.addRef(base);
        EnclosingForRefsWithCreator e = new EnclosingForRefsWithCreator();
        e.baseRef = base;
        e.nextRef = r;

        String json = MAPPER.writeValueAsString(e);

        EnclosingForRefsWithCreator result = MAPPER.readValue(json,
                EnclosingForRefsWithCreator.class);
        assertNotNull(result);
        assertEquals(result.label, e.label);

        
        assertEquals(json, MAPPER.writeValueAsString(result));
    }

// com.fasterxml.jackson.databind.objectid.ObjectId687Test::testSerializeDeserializeNoCreator
    public void testSerializeDeserializeNoCreator() throws IOException {
        ReferredWithNoCreator base = new ReferredWithNoCreator();
        ReferringToObjWithNoCreator r = new ReferringToObjWithNoCreator();
        r.addRef(base);
        EnclosingForRefWithNoCreator e = new EnclosingForRefWithNoCreator();
        e.baseRef = base;
        e.nextRef = r;

        String json = MAPPER.writeValueAsString(e);

        EnclosingForRefWithNoCreator result = MAPPER.readValue(json,
                EnclosingForRefWithNoCreator.class);
        assertNotNull(result);
        assertEquals(result.label, e.label);

        
        assertEquals(json, MAPPER.writeValueAsString(result));
    }

// com.fasterxml.jackson.databind.objectid.ObjectId825BTest::testFull825
    public void testFull825() throws Exception
    {
        final ObjectMapper mapper = new ObjectMapper();
        mapper.enableDefaultTyping(ObjectMapper.DefaultTyping.OBJECT_AND_NON_CONCRETE);
        mapper.enable(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT);

        String INPUT = aposToQuotes(
"{\n"+
"    '@class': '_PKG_CTC',\n"+
"     'var': [{\n"+
"      'ch': {\n"+
"        '@class': '_PKG_Ch',\n"+
"         'act': [{\n"+
"            '@class': '_PKG_CTD',\n"+
"            'oidString': 'oid1',\n"+
"            'dec': [{\n"+
"              '@class': '_PKG_Dec',\n"+
"                'oidString': 'oid2',\n"+
"                'outTr': [{\n"+
"                  '@class': '_PKG_Tr',\n"+
"                  'target': {\n"+
"                    '@class': '_PKG_Ti',\n"+
"                    'oidString': 'oid3',\n"+
"                    'timer': 'problemoid',\n"+
"                    'outTr': [{\n"+
"                      '@class': '_PKG_Tr',\n"+
"                      'target': {\n"+
"                        '@class': '_PKG_Ti',\n"+
"                        'oidString': 'oid4',\n"+
"                        'timer': {\n"+
"                          '@class': '_PKG_V',\n"+
"                          'oidString': 'problemoid'\n"+
"                        }\n"+
"                      }\n"+
"                    }]\n"+
"                  }\n"+
"                }]\n"+
"              }]\n"+
"         }],\n"+
"         'oidString': 'oid5'\n"+
"      },\n"+
"       '@class': '_PKG_CTV',\n"+
"       'oidString': 'oid6',\n"+
"       'locV': ['problemoid']\n"+
"    }],\n"+
"     'oidString': 'oid7'\n"+
"}\n"
                );

        
        final String newPkg = getClass().getName() + "\\$";
        INPUT = INPUT.replaceAll("_PKG_", newPkg);
        
        CTC result = mapper.readValue(INPUT, CTC.class);
        assertNotNull(result);
    }

// com.fasterxml.jackson.databind.objectid.ObjectId825Test::testDeserialize
    public void testDeserialize() throws Exception {
        TestA a = new TestA();
        a.oidString = "oidA";

        TestC c = new TestC();
        c.oidString = "oidC";

        a.testAbst = c;

        TestD d = new TestD();
        d.oidString = "oidD";

        c.d = d;
        a.d = d;

        String json = DEF_TYPING_MAPPER.writeValueAsString(a);

        TestA testADeserialized = DEF_TYPING_MAPPER.readValue(json, TestA.class);

        assertNotNull(testADeserialized);
        assertNotNull(testADeserialized.d);
        assertEquals("oidD", testADeserialized.d.oidString);
    }

// com.fasterxml.jackson.databind.objectid.ObjectIdReordering1388Test::testDeserializationFinalClassJSOG
    public void testDeserializationFinalClassJSOG() throws Exception
    {
        final ObjectMapper mapper = new ObjectMapper();
        final UUID id = UUID.fromString("a59aa02c-fe3c-43f8-9b5a-5fe01878a818");
        final NamedThing thing = new NamedThing(id, "Hello");

        final TypeReference<?> namedThingListType = new TypeReference<List<NamedThing>>() { };

        {
            final String jsog = mapper.writeValueAsString(Arrays.asList(thing, thing, thing));
            final List<NamedThing> list = mapper.readValue(jsog, namedThingListType);
            _assertAllSame(list);
            
            assertTrue(jsog.equals("[{\"@id\":1,\"id\":\"a59aa02c-fe3c-43f8-9b5a-5fe01878a818\",\"name\":\"Hello\"},1,1]"));
        }

        
        
        {
            final String json = "[1,1,{\"@id\":1,\"id\":\"a59aa02c-fe3c-43f8-9b5a-5fe01878a818\",\"name\":\"Hello\"}]";
            final List<NamedThing> forward = mapper.readValue(json, namedThingListType);
            _assertAllSame(forward);
        }

        
        {
            final String json = aposToQuotes("[{'id':'a59aa02c-fe3c-43f8-9b5a-5fe01878a818','@id':1,'name':'Hello'}, 1, 1]");
            final List<NamedThing> forward = mapper.readValue(json, namedThingListType);
            _assertAllSame(forward);
        }

        
        {
            final String json = aposToQuotes("[{'id':'a59aa02c-fe3c-43f8-9b5a-5fe01878a818','name':'Hello','@id':1}, 1, 1]");
            final List<NamedThing> forward = mapper.readValue(json, namedThingListType);
            _assertAllSame(forward);
        }
    }

// com.fasterxml.jackson.databind.objectid.ObjectWithCreator1261Test::testObjectIds1261
    public void testObjectIds1261() throws Exception
    {
         ObjectMapper mapper = new ObjectMapper();
         mapper.enable(SerializationFeature.INDENT_OUTPUT);
         mapper.enable(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY);

         Answer initialAnswer = createInitialAnswer();
         String initialAnswerString = mapper.writeValueAsString(initialAnswer);

         JsonNode tree = mapper.readTree(initialAnswerString);
         Answer deserializedAnswer = mapper.readValue(initialAnswerString,
               Answer.class);
         String reserializedAnswerString = mapper
               .writeValueAsString(deserializedAnswer);
         JsonNode newTree = mapper.readTree(reserializedAnswerString);
         if (!tree.equals(newTree)) {
                  fail("Original and recovered Json are different. Recovered = \n"
                        + reserializedAnswerString + "\n");
         }
   }

// com.fasterxml.jackson.databind.objectid.PolymorphicWithObjectId1551Test::testWithAbstractUsingProp
    public void testWithAbstractUsingProp() throws Exception
    {
        Car c = new Car();
        c.vehicleId = "123";
        c.numberOfDoors = 2;
        
        VehicleOwnerViaProp v1 = new VehicleOwnerViaProp();
        v1.ownedVehicle = c;
        VehicleOwnerViaProp v2 = new VehicleOwnerViaProp();
        v2.ownedVehicle = c;

        ObjectMapper objectMapper = new ObjectMapper();
        String serialized = objectMapper.writer()
                .writeValueAsString(new VehicleOwnerViaProp[] { v1, v2 });
        

        VehicleOwnerViaProp[] deserialized = objectMapper.readValue(serialized, VehicleOwnerViaProp[].class);
        assertEquals(2, deserialized.length);
        assertSame(deserialized[0].ownedVehicle, deserialized[1].ownedVehicle);
    }

// com.fasterxml.jackson.databind.objectid.PolymorphicWithObjectId1551Test::testFailingAbstractUsingProp
    public void testFailingAbstractUsingProp() throws Exception
    {
        Car c = new Car();
        c.vehicleId = "123";
        c.numberOfDoors = 2;
        
        VehicleOwnerBroken v1 = new VehicleOwnerBroken();
        v1.ownedVehicle = c;
        VehicleOwnerBroken v2 = new VehicleOwnerBroken();
        v2.ownedVehicle = c;

        ObjectMapper objectMapper = new ObjectMapper();
        try {
            objectMapper.writer()
                .writeValueAsString(new VehicleOwnerBroken[] { v1, v2 });
        } catch (InvalidDefinitionException e) {
            
            assertEquals(Car.class, e.getType().getRawClass());
            verifyException(e, "Invalid Object Id definition");
            verifyException(e, "cannot find property with name 'bogus'");
        }

        
        final String JSON = aposToQuotes(
"[{'ownedVehicle':{'@class':'com.fasterxml.jackson.failing.PolymorphicWithObjectId1551Test$Car','vehicleId':'123',"
+"'numberOfDoors':2}},{'ownedVehicle':'123'}]"
                );
        try {
            objectMapper.readValue(JSON, VehicleOwnerBroken[].class);
            fail("Should not pass");
        } catch (InvalidDefinitionException e) {
            assertEquals(Vehicle.class, e.getType().getRawClass());
            verifyException(e, "Invalid Object Id definition");
            verifyException(e, "cannot find property with name 'bogus'");
        }
    }

// com.fasterxml.jackson.databind.objectid.ReferentialWithObjectIdTest::testAtomicWithObjectId
    public void testAtomicWithObjectId() throws Exception
    {
        Employee first = new Employee();
        first.id = 1;
        first.name = "Alice";

        Employee second = new Employee();
        second.id = 2;
        second.name = "Bob";

        first.next(second);
        second.next(first);

        EmployeeList input = new EmployeeList();
        input.first = new AtomicReference<Employee>(first);

        String json = MAPPER.writeValueAsString(input);

        
 
        EmployeeList result = MAPPER.readValue(json, EmployeeList.class);
        Employee firstB = result.first.get();
        assertNotNull(firstB);
        assertEquals("Alice", firstB.name);
        Employee secondB = firstB.next.get();
        assertNotNull(secondB);
        assertEquals("Bob", secondB.name);
        assertNotNull(secondB.next.get());
        assertSame(firstB, secondB.next.get());
    }

// com.fasterxml.jackson.databind.objectid.TestAbstractWithObjectId::testIssue877
    public void testIssue877() throws Exception
    {
        
        BaseInterfaceImpl one = new BaseInterfaceImpl();
        BaseInterfaceImpl two = new BaseInterfaceImpl();

        
        one.addInstance(two);
        two.addInstance(one);

        
        ListWrapper<BaseInterfaceImpl> myList = new ListWrapper<BaseInterfaceImpl>();
        myList.add(one);
        myList.add(two);

        
        ObjectMapper om = new ObjectMapper();
        om.enableDefaultTypingAsProperty(ObjectMapper.DefaultTyping.NON_FINAL, "@class");

        
        String json = om.writerWithDefaultPrettyPrinter().writeValueAsString(myList);
        ListWrapper<BaseInterfaceImpl> result;
        
        result = om.readValue(json, new TypeReference<ListWrapper<BaseInterfaceImpl>>() { });

        assertNotNull(result);
        
        assertEquals(2, result.size());
    }

// com.fasterxml.jackson.databind.objectid.TestObjectId::testColumnMetadata
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

// com.fasterxml.jackson.databind.objectid.TestObjectId::testMixedRefsIssue188
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

// com.fasterxml.jackson.databind.objectid.TestObjectId::testObjectAndTypeId
    public void testObjectAndTypeId() throws Exception
    {
        final ObjectMapper mapper = new ObjectMapper();

        Bar inputRoot = new Bar();
        Foo inputChild = new Foo();
        inputRoot.next = inputChild;
        inputChild.ref = inputRoot;

        String json = mapper.writerWithDefaultPrettyPrinter()
                .writeValueAsString(inputRoot);
        
        BaseEntity resultRoot = mapper.readValue(json, BaseEntity.class);
        assertNotNull(resultRoot);
        assertTrue(resultRoot instanceof Bar);
        Bar first = (Bar) resultRoot;

        assertNotNull(first.next);
        assertTrue(first.next instanceof Foo);
        Foo second = (Foo) first.next;
        assertNotNull(second.ref);
        assertSame(first, second.ref);
    }

// com.fasterxml.jackson.databind.objectid.TestObjectId::testWithFieldsInBaseClass1083
    public void testWithFieldsInBaseClass1083() throws Exception {
          final String json = aposToQuotes("{'schemas': [{\n"
              + "  'name': 'FoodMart'\n"
              + "}]}\n");
          MAPPER.readValue(json, JsonRoot.class);
    }

// com.fasterxml.jackson.databind.objectid.TestObjectIdDeserialization::testSimpleDeserializationClass
    public void testSimpleDeserializationClass() throws Exception
    {
        
        Identifiable result = MAPPER.readValue(EXP_SIMPLE_INT_CLASS, Identifiable.class);
        assertEquals(13, result.value);
        assertSame(result, result.next);
    }

// com.fasterxml.jackson.databind.objectid.TestObjectIdDeserialization::testMissingObjectId
    public void testMissingObjectId() throws Exception
    {
        Identifiable result = MAPPER.readValue(aposToQuotes("{'value':28, 'next':{'value':29}}"),
                Identifiable.class);
        assertNotNull(result);
        assertEquals(28, result.value);
        assertNotNull(result.next);
        assertEquals(29, result.next.value);
    }

// com.fasterxml.jackson.databind.objectid.TestObjectIdDeserialization::testSimpleUUIDForClassRoundTrip
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

        String json = MAPPER.writeValueAsString(root);

        
        UUIDNode result = MAPPER.readValue(json, UUIDNode.class);
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

// com.fasterxml.jackson.databind.objectid.TestObjectIdDeserialization::testSimpleDeserializationProperty
    public void testSimpleDeserializationProperty() throws Exception
    {
        IdWrapper result = MAPPER.readValue(EXP_SIMPLE_INT_PROP, IdWrapper.class);
        assertEquals(7, result.node.value);
        assertSame(result.node, result.node.next.node);
    }

// com.fasterxml.jackson.databind.objectid.TestObjectIdDeserialization::testSimpleDeserWithForwardRefs
    public void testSimpleDeserWithForwardRefs() throws Exception
    {
        IdWrapper result = MAPPER.readValue("{\"node\":{\"value\":7,\"next\":{\"node\":1}, \"@id\":1}}"
                ,IdWrapper.class);
        assertEquals(7, result.node.value);
        assertSame(result.node, result.node.next.node);
    }

// com.fasterxml.jackson.databind.objectid.TestObjectIdDeserialization::testForwardReference
    public void testForwardReference()
        throws Exception
    {
        String json = "{\"employees\":["
                      + "{\"id\":1,\"name\":\"First\",\"manager\":2,\"reports\":[]},"
                      + "{\"id\":2,\"name\":\"Second\",\"manager\":null,\"reports\":[1]}"
                      + "]}";
        Company company = MAPPER.readValue(json, Company.class);
        assertEquals(2, company.employees.size());
        Employee firstEmployee = company.employees.get(0);
        Employee secondEmployee = company.employees.get(1);
        assertEquals(1, firstEmployee.id);
        assertEquals(2, secondEmployee.id);
        assertEquals(secondEmployee, firstEmployee.manager); 
        assertEquals(firstEmployee, secondEmployee.reports.get(0)); 
    }

// com.fasterxml.jackson.databind.objectid.TestObjectIdDeserialization::testForwardReferenceInCollection
    public void testForwardReferenceInCollection()
        throws Exception
    {
        String json = "{\"employees\":["
                      + "{\"id\":1,\"name\":\"First\",\"manager\":null,\"reports\":[2]},"
                      + "{\"id\":2,\"name\":\"Second\",\"manager\":1,\"reports\":[]}"
                      + "]}";
        Company company = MAPPER.readValue(json, Company.class);
        assertEquals(2, company.employees.size());
        Employee firstEmployee = company.employees.get(0);
        Employee secondEmployee = company.employees.get(1);
        assertEmployees(firstEmployee, secondEmployee);
    }

// com.fasterxml.jackson.databind.objectid.TestObjectIdDeserialization::testForwardReferenceInMap
    public void testForwardReferenceInMap()
        throws Exception
    {
        String json = "{\"employees\":{"
                      + "\"1\":{\"id\":1,\"name\":\"First\",\"manager\":null,\"reports\":[2]},"
                      + "\"2\": 2,"
                      + "\"3\":{\"id\":2,\"name\":\"Second\",\"manager\":1,\"reports\":[]}"
                      + "}}";
        MappedCompany company = MAPPER.readValue(json, MappedCompany.class);
        assertEquals(3, company.employees.size());
        Employee firstEmployee = company.employees.get(1);
        Employee secondEmployee = company.employees.get(3);
        assertEmployees(firstEmployee, secondEmployee);
    }

// com.fasterxml.jackson.databind.objectid.TestObjectIdDeserialization::testForwardReferenceAnySetterCombo
    public void testForwardReferenceAnySetterCombo() throws Exception {
        String json = "{\"@id\":1, \"foo\":2, \"bar\":{\"@id\":2, \"foo\":1}}";
        AnySetterObjectId value = MAPPER.readValue(json, AnySetterObjectId.class);
        assertSame(value.values.get("bar"), value.values.get("foo"));
    }

// com.fasterxml.jackson.databind.objectid.TestObjectIdDeserialization::testUnresolvedForwardReference
    public void testUnresolvedForwardReference()
        throws Exception
    {
        String json = "{\"employees\":[" 
                      + "{\"id\":1,\"name\":\"First\",\"manager\":null,\"reports\":[3]},"
                      + "{\"id\":2,\"name\":\"Second\",\"manager\":3,\"reports\":[]}" 
                      + "]}";
        try {
            MAPPER.readValue(json, Company.class);
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

// com.fasterxml.jackson.databind.objectid.TestObjectIdDeserialization::testUnresolvableAsNull
    public void testUnresolvableAsNull() throws Exception
    {
        IdWrapper w = MAPPER.readerFor(IdWrapper.class)
                .without(DeserializationFeature.FAIL_ON_UNRESOLVED_OBJECT_IDS)
                .readValue(aposToQuotes("{'node':123}"));
        assertNotNull(w);
        assertNull(w.node);
    }

// com.fasterxml.jackson.databind.objectid.TestObjectIdDeserialization::testKeepCollectionOrdering
    public void testKeepCollectionOrdering() throws Exception
    {
        String json = "{\"employees\":[2,1,"
                + "{\"id\":1,\"name\":\"First\",\"manager\":null,\"reports\":[2]},"
                + "{\"id\":2,\"name\":\"Second\",\"manager\":1,\"reports\":[]}"
                + "]}";
        Company company = MAPPER.readValue(json, Company.class);
        assertEquals(4, company.employees.size());
        
        Employee firstEmployee = company.employees.get(1);
        Employee secondEmployee = company.employees.get(0);
        assertSame(firstEmployee, company.employees.get(2));
        assertSame(secondEmployee, company.employees.get(3));
        assertEmployees(firstEmployee, secondEmployee);
    }

// com.fasterxml.jackson.databind.objectid.TestObjectIdDeserialization::testKeepMapOrdering
    public void testKeepMapOrdering()
        throws Exception
    {
        String json = "{\"employees\":{"
                      + "\"1\":2, \"2\":1,"
                      + "\"3\":{\"id\":1,\"name\":\"First\",\"manager\":null,\"reports\":[2]},"
                      + "\"4\":{\"id\":2,\"name\":\"Second\",\"manager\":1,\"reports\":[]}"
                      + "}}";
        MappedCompany company = MAPPER.readValue(json, MappedCompany.class);
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

// com.fasterxml.jackson.databind.objectid.TestObjectIdDeserialization::testCustomDeserializationClass
    public void testCustomDeserializationClass() throws Exception
    {
        
        IdentifiableCustom result = MAPPER.readValue(EXP_CUSTOM_VIA_CLASS, IdentifiableCustom.class);
        assertEquals(-900, result.value);
        assertSame(result, result.next);
    }

// com.fasterxml.jackson.databind.objectid.TestObjectIdDeserialization::testCustomDeserializationProperty
    public void testCustomDeserializationProperty() throws Exception
    {
        
        IdWrapperExt result = MAPPER.readValue(EXP_CUSTOM_VIA_PROP, IdWrapperExt.class);
        assertEquals(99, result.node.value);
        assertSame(result.node, result.node.next.node);
        assertEquals(3, result.node.customId);
    }

// com.fasterxml.jackson.databind.objectid.TestObjectIdDeserialization::testCustomPoolResolver
    public void testCustomPoolResolver() throws Exception
    {
        Map<Object,WithCustomResolution> pool = new HashMap<Object,WithCustomResolution>();
        pool.put(1, new WithCustomResolution(1, 1));
        pool.put(2, new WithCustomResolution(2, 2));
        pool.put(3, new WithCustomResolution(3, 3));
        pool.put(4, new WithCustomResolution(4, 4));
        pool.put(5, new WithCustomResolution(5, 5));
        ContextAttributes attrs = MAPPER.getDeserializationConfig().getAttributes().withSharedAttribute(POOL_KEY, pool);
        String content = "{\"data\":[1,2,3,4,5]}";
        CustomResolutionWrapper wrapper = MAPPER.readerFor(CustomResolutionWrapper.class).with(attrs).readValue(content);
        assertFalse(wrapper.data.isEmpty());
        for (WithCustomResolution ob : wrapper.data) {
            assertSame(pool.get(ob.id), ob);
        }
    }

// com.fasterxml.jackson.databind.objectid.TestObjectIdDeserialization::testNullObjectId
    public void testNullObjectId() throws Exception
    {
        
        
        Identifiable value = MAPPER.readValue
                (aposToQuotes("{'value':3, 'next':null, 'id':null}"), Identifiable.class);
        assertNotNull(value);
        assertEquals(3, value.value);
    }

// com.fasterxml.jackson.databind.objectid.TestObjectIdSerialization::testSimpleSerializationClass
    public void testSimpleSerializationClass() throws Exception
    {
        Identifiable src = new Identifiable(13);
        src.next = src;
        
        
        String json = MAPPER.writeValueAsString(src);
        assertEquals(EXP_SIMPLE_INT_CLASS, json);

        
        json = MAPPER.writeValueAsString(src);
        assertEquals(EXP_SIMPLE_INT_CLASS, json);
    }

// com.fasterxml.jackson.databind.objectid.TestObjectIdSerialization::testSimpleSerializationProperty
    public void testSimpleSerializationProperty() throws Exception
    {
        IdWrapper src = new IdWrapper(7);
        src.node.next = src;
        
        
        String json = MAPPER.writeValueAsString(src);
        assertEquals(EXP_SIMPLE_INT_PROP, json);
        
        json = MAPPER.writeValueAsString(src);
        assertEquals(EXP_SIMPLE_INT_PROP, json);
    }

// com.fasterxml.jackson.databind.objectid.TestObjectIdSerialization::testEmptyObjectWithId
    public void testEmptyObjectWithId() throws Exception
    {
        final ObjectMapper mapper = new ObjectMapper();
        String json = mapper.writeValueAsString(new EmptyObject());
        assertEquals(aposToQuotes("{'@id':1}"), json);
    }

// com.fasterxml.jackson.databind.objectid.TestObjectIdSerialization::testSerializeWithOpaqueStringId
    public void testSerializeWithOpaqueStringId() throws Exception
    {
        StringIdentifiable ob1 = new StringIdentifiable(12);
        StringIdentifiable ob2 = new StringIdentifiable(34);
        ob1.next = ob2;
        ob2.next = ob1;

        
        String json = MAPPER.writeValueAsString(ob1);
        assertNotNull(json);

        
        StringIdentifiable output = MAPPER.readValue(json, StringIdentifiable.class);
        assertNotNull(output);
        assertEquals(12, output.value);
        assertNotNull(output.next);
        assertEquals(34, output.next.value);
        assertSame(output.next.next, output);

        String json2 = aposToQuotes("{'id':'foobar','value':3, 'next':{'id':'barf','value':5,'next':'foobar'}}");
        output = MAPPER.readValue(json2, StringIdentifiable.class);
        assertNotNull(output);
        assertEquals(3, output.value);
        assertNotNull(output.next);
        assertEquals(5, output.next.value);
        assertSame(output.next.next, output);
    }

// com.fasterxml.jackson.databind.objectid.TestObjectIdSerialization::testCustomPropertyForClass
    public void testCustomPropertyForClass() throws Exception
    {
        IdentifiableWithProp src = new IdentifiableWithProp(123, -19);
        src.next = src;
        
        
        String json = MAPPER.writeValueAsString(src);
        assertEquals(EXP_CUSTOM_PROP, json);

        
        json = MAPPER.writeValueAsString(src);
        assertEquals(EXP_CUSTOM_PROP, json);
    }

// com.fasterxml.jackson.databind.objectid.TestObjectIdSerialization::testCustomPropertyViaProperty
    public void testCustomPropertyViaProperty() throws Exception
    {
        IdWrapperCustom src = new IdWrapperCustom(123, 7);
        src.node.next = src;
        
        
        String json = MAPPER.writeValueAsString(src);
        assertEquals(EXP_CUSTOM_PROP_VIA_REF, json);
        
        json = MAPPER.writeValueAsString(src);
        assertEquals(EXP_CUSTOM_PROP_VIA_REF, json);
    }

// com.fasterxml.jackson.databind.objectid.TestObjectIdSerialization::testAlwaysAsId
    public void testAlwaysAsId() throws Exception
    {
        String json = MAPPER.writeValueAsString(new AlwaysContainer());
        assertEquals("{\"a\":1,\"b\":2}", json);
    }

// com.fasterxml.jackson.databind.objectid.TestObjectIdSerialization::testAlwaysIdForTree
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

// com.fasterxml.jackson.databind.objectid.TestObjectIdSerialization::testNullStringPropertyId
    public void testNullStringPropertyId() throws Exception
    {
        IdentifiableStringId value = MAPPER.readValue
                (aposToQuotes("{'value':3, 'next':null, 'id':null}"), IdentifiableStringId.class);
        assertNotNull(value);
        assertEquals(3, value.value);
    }

// com.fasterxml.jackson.databind.objectid.TestObjectIdSerialization::testInvalidProp
    public void testInvalidProp() throws Exception
    {
        try {
            MAPPER.writeValueAsString(new Broken());
            fail("Should have thrown an exception");
        } catch (JsonMappingException e) {
            verifyException(e, "cannot find property with name 'id'");
        }
    }

// com.fasterxml.jackson.databind.objectid.TestObjectIdWithEquals::testSimpleEquals
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

// com.fasterxml.jackson.databind.objectid.TestObjectIdWithEquals::testEqualObjectIdsExternal
    public void testEqualObjectIdsExternal() throws Exception
    {
        Element element = new Element();
        element.uri = URI.create("URI");
        element.name = "Element1";

        Element element2 = new Element();
        element2.uri = URI.create("URI");
        element2.name = "Element2";

        
        

        List<Element> input = Arrays.asList(element, element2);

        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(SerializationFeature.USE_EQUALITY_FOR_OBJECT_ID);

        String json = mapper.writerFor(new TypeReference<List<Element>>() { })
                .writeValueAsString(input);

        Element[] output = mapper.readValue(json, Element[].class);
        assertNotNull(output);
        assertEquals(2, output.length);
    }

// com.fasterxml.jackson.databind.objectid.TestObjectIdWithInjectables538::testWithInjectables538
    public void testWithInjectables538() throws Exception
    {
        A a = new A("a");
        B b = new B("b");
        a.b = b;
        b.a = a;

        String json = MAPPER.writeValueAsString(a);

        InjectableValues.Std inject = new InjectableValues.Std();
        inject.addValue("i1", "e1");
        inject.addValue("i2", "e2");
        A output = null;

        try {
            output = MAPPER.reader(inject).forType(A.class).readValue(json);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to deserialize from JSON '"+json+"'", e);
        }
        assertNotNull(output);
        assertNotNull(output.b);
        assertSame(output, output.b.a);
    }

// com.fasterxml.jackson.databind.objectid.TestObjectIdWithPolymorphic::testPolymorphicRoundtrip
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

// com.fasterxml.jackson.databind.objectid.TestObjectIdWithPolymorphic::testIssue811
    public void testIssue811() throws Exception
    {
        ObjectMapper om = new ObjectMapper();
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

// com.fasterxml.jackson.databind.seq.ReadRecoveryTest::testRootBeans
    public void testRootBeans() throws Exception
    {
        final String JSON = aposToQuotes("{'a':3} {'x':5}");
        MappingIterator<Bean> it = MAPPER.readerFor(Bean.class).readValues(JSON);
        
        assertTrue(it.hasNextValue());
        Bean bean = it.nextValue();
        assertEquals(3, bean.a);
        
        try {
            bean = it.nextValue();
            fail("Should not have succeeded");
        } catch (JsonMappingException e) {
            verifyException(e, "Unrecognized field \"x\"");
        }
        
        assertFalse(it.hasNextValue());

        it.close();
    }

// com.fasterxml.jackson.databind.seq.ReadRecoveryTest::testSimpleRootRecovery
    public void testSimpleRootRecovery() throws Exception
    {
        final String JSON = aposToQuotes("{'a':3}{'a':27,'foo':[1,2],'b':{'x':3}}  {'a':1,'b':2} ");

        MappingIterator<Bean> it = MAPPER.readerFor(Bean.class).readValues(JSON);
        Bean bean = it.nextValue();

        assertNotNull(bean);
        assertEquals(3, bean.a);

        
        try {
            it.nextValue();
        } catch (JsonMappingException e) {
            verifyException(e, "Unrecognized field \"foo\"");
        }

        
        bean = it.nextValue();
        assertNotNull(bean);
        assertEquals(1, bean.a);
        assertEquals(2, bean.b);

        assertFalse(it.hasNextValue());
        
        it.close();
    }

// com.fasterxml.jackson.databind.seq.ReadRecoveryTest::testSimpleArrayRecovery
    public void testSimpleArrayRecovery() throws Exception
    {
        final String JSON = aposToQuotes("[{'a':3},{'a':27,'foo':[1,2],'b':{'x':3}}  ,{'a':1,'b':2}  ]");

        MappingIterator<Bean> it = MAPPER.readerFor(Bean.class).readValues(JSON);
        Bean bean = it.nextValue();

        assertNotNull(bean);
        assertEquals(3, bean.a);

        
        try {
            it.nextValue();
        } catch (JsonMappingException e) {
            verifyException(e, "Unrecognized field \"foo\"");
        }

        
        bean = it.nextValue();
        assertNotNull(bean);
        assertEquals(1, bean.a);
        assertEquals(2, bean.b);

        assertFalse(it.hasNextValue());
        
        it.close();
    }

// com.fasterxml.jackson.databind.seq.ReadValuesTest::testRootBeans
    public void testRootBeans() throws Exception
    {
        for (Source src : Source.values()) {
            _testRootBeans(src);
        }
    }

// com.fasterxml.jackson.databind.seq.ReadValuesTest::testRootBeansInArray
    public void testRootBeansInArray() throws Exception
    {
        final String JSON = "[{\"a\":6}, {\"a\":-7}]";

        MappingIterator<Bean> it = MAPPER.readerFor(Bean.class).readValues(JSON);

        assertNotNull(it.getCurrentLocation());
        assertTrue(it.hasNext());
        Bean b = it.next();
        assertEquals(6, b.a);
        assertTrue(it.hasNext());
        b = it.next();
        assertEquals(-7, b.a);
        assertFalse(it.hasNext());
        it.close();

        
        it = MAPPER.readerFor(Bean.class).readValues(JSON);
        List<Bean> all = it.readAll();
        assertEquals(2, all.size());
        it.close();

        it = MAPPER.readerFor(Bean.class).readValues("[{\"a\":4},{\"a\":4}]");
        Set<Bean> set = it.readAll(new HashSet<Bean>());
        assertEquals(HashSet.class, set.getClass());
        assertEquals(1, set.size());
        assertEquals(4, set.iterator().next().a);
    }

// com.fasterxml.jackson.databind.seq.ReadValuesTest::testRootMaps
    public void testRootMaps() throws Exception
    {
        final String JSON = "{\"a\":3}{\"a\":27}  ";
        Iterator<Map<?,?>> it = MAPPER.readerFor(Map.class).readValues(JSON);

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
        
        Iterator<int[]> it = MAPPER.readerFor(int[].class).readValues(jp);
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
        
        Iterator<Integer> it = MAPPER.readerFor(Integer.class).readValues(jp);
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

        Iterator<Integer> it = MAPPER.readerFor(Integer.class).readValues(JSON);
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
        
        Iterator<Bean> it = MAPPER.readerFor(Bean.class).readValues(jp);

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
        
        Iterator<Map<?,?>> it = MAPPER.readerFor(Map.class).readValues(jp);

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
        JsonParser p = MAPPER.getFactory().createParser(JSON);
        assertToken(JsonToken.START_ARRAY, p.nextToken());
        
        
        
        
        assertToken(JsonToken.START_ARRAY, p.nextToken());
        
        Iterator<int[]> it = MAPPER.readValues(p, int[].class);

        assertTrue(it.hasNext());
        int[] array = it.next();
        assertEquals(1, array.length);
        assertEquals(1, array[0]);
        assertTrue(it.hasNext());
        array = it.next();
        assertEquals(1, array.length);
        assertEquals(3, array[0]);
        assertFalse(it.hasNext());
        p.close();
    }

// com.fasterxml.jackson.databind.seq.TestInnerClassReaderFor::testReaderFor
    public void testReaderFor() throws IOException {

        X x = new X("dummy");
        objectMapper().readerForUpdating(x).readValue("{\"value\": \"updatedX\"}");
        assertEquals(x.getValue(), "updatedX");

        Y y = new Y("dummy");
        objectMapper().readerForUpdating(y).readValue("{\"value\": \"updatedY\"}");
        assertEquals(y.getValue(), "updatedY");

    }

// com.fasterxml.jackson.databind.ser.JsonValueTest::testSimpleMethodJsonValue
    public void testSimpleMethodJsonValue() throws Exception
    {
        assertEquals("\"abc\"", MAPPER.writeValueAsString(new ValueClass<String>("abc")));
        assertEquals("null", MAPPER.writeValueAsString(new ValueClass<String>(null)));
    }

// com.fasterxml.jackson.databind.ser.JsonValueTest::testSimpleFieldJsonValue
    public void testSimpleFieldJsonValue() throws Exception
    {
        assertEquals("\"abc\"", MAPPER.writeValueAsString(new FieldValueClass<String>("abc")));
        assertEquals("null", MAPPER.writeValueAsString(new FieldValueClass<String>(null)));
    }

// com.fasterxml.jackson.databind.ser.JsonValueTest::testJsonValueWithUseSerializer
    public void testJsonValueWithUseSerializer() throws Exception
    {
        String result = serializeAsString(MAPPER, new ToStringValueClass<Integer>(Integer.valueOf(123)));
        assertEquals("\"123\"", result);
    }

// com.fasterxml.jackson.databind.ser.JsonValueTest::testMixedJsonValue
    public void testMixedJsonValue() throws Exception
    {
        String result = serializeAsString(MAPPER, new ToStringValueClass2("xyz"));
        assertEquals("\"xyz\"", result);
    }

// com.fasterxml.jackson.databind.ser.JsonValueTest::testDisabling
    public void testDisabling() throws Exception
    {
        assertEquals(aposToQuotes("{'x':1,'y':2}"),
                MAPPER.writeValueAsString(new DisabledJsonValue()));
    }

// com.fasterxml.jackson.databind.ser.JsonValueTest::testValueWithStaticType
    public void testValueWithStaticType() throws Exception
    {
        
        assertEquals("{\"a\":\"a\",\"b\":\"b\"}", MAPPER.writeValueAsString(new ValueWrapper()));

        
        ObjectMapper staticMapper = new ObjectMapper();
        staticMapper.configure(MapperFeature.USE_STATIC_TYPING, true);
        assertEquals("{\"a\":\"a\"}", staticMapper.writeValueAsString(new ValueWrapper()));
    }

// com.fasterxml.jackson.databind.ser.JsonValueTest::testMapWithJsonValue
    public void testMapWithJsonValue() throws Exception {
        
        assertEquals("{\"a\":\"1\"}", MAPPER.writeValueAsString(new MapBean()));

        
        assertEquals("{\"b\":\"2\"}", MAPPER.writeValueAsString(new MapFieldBean()));
    }

// com.fasterxml.jackson.databind.ser.JsonValueTest::testWithMap
    public void testWithMap() throws Exception {
        assertEquals("42", MAPPER.writeValueAsString(new MapAsNumber()));
    }

// com.fasterxml.jackson.databind.ser.JsonValueTest::testWithList
    public void testWithList() throws Exception {
        assertEquals("13", MAPPER.writeValueAsString(new ListAsNumber()));
    }

// com.fasterxml.jackson.databind.ser.JsonValueTest::testInList
    public void testInList() throws Exception {
        IntExtBean bean = new IntExtBean();
        bean.add(1);
        bean.add(2);
        String json = MAPPER.writeValueAsString(bean);
        assertEquals(json, "{\"values\":[{\"i\":1},{\"i\":2}]}");
    }

// com.fasterxml.jackson.databind.ser.JsonValueTest::testPolymorphicSerdeWithDelegate
    public void testPolymorphicSerdeWithDelegate() throws Exception
    {
	    AdditionInterface adder = new AdditionInterfaceImpl(1);
	
	    assertEquals(2, adder.add(1));
	    String json = MAPPER.writeValueAsString(adder);
	    assertEquals("{\"boingo\":\"boopsy\",\"toAdd\":1}", json);
	    assertEquals(2, MAPPER.readValue(json, AdditionInterface.class).add(1));
    }

// com.fasterxml.jackson.databind.ser.JsonValueTest::testJsonValueWithCustomOverride
    public void testJsonValueWithCustomOverride() throws Exception
    {
        final Bean838 INPUT = new Bean838();

        
        assertEquals(quote("value"), MAPPER.writeValueAsString(INPUT));

        
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new SimpleModule()
            .addSerializer(Bean838.class, new Bean838Serializer())
            );
        assertEquals("42", mapper.writeValueAsString(INPUT));
    }

// com.fasterxml.jackson.databind.ser.filter.TestIgnoredTypes::testIgnoredType
    public void testIgnoredType() throws Exception
    {
        final ObjectMapper mapper = objectMapper();

        
        NonIgnoredType bean = mapper.readValue("{\"value\":13}", NonIgnoredType.class);
        assertNotNull(bean);
        assertEquals(13, bean.value);

        
        bean = mapper.readValue("{ \"ignored\":[1,2,{}], \"value\":9 }", NonIgnoredType.class);
        assertNotNull(bean);
        assertEquals(9, bean.value);
    }

// com.fasterxml.jackson.databind.ser.filter.TestIgnoredTypes::testSingleWithMixins
    public void testSingleWithMixins() throws Exception {
        SimpleModule module = new SimpleModule();
        module.setMixInAnnotation(Person.class, PersonMixin.class);
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(module);
        PersonWrapper input = new PersonWrapper();
        String json = mapper.writeValueAsString(input);
        assertEquals("{\"value\":1}", json);
    }

// com.fasterxml.jackson.databind.ser.filter.TestIgnoredTypes::testListWithMixins
    public void testListWithMixins() throws Exception {
        SimpleModule module = new SimpleModule();
        module.setMixInAnnotation(Person.class, PersonMixin.class);
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(module);
        List<Person> persons = new ArrayList<Person>();
        persons.add(new Person("Bob"));
        String json = mapper.writeValueAsString(persons);
        assertEquals("[{\"name\":\"Bob\"}]", json);
    }

// com.fasterxml.jackson.databind.ser.filter.TestIgnoredTypes::testIgnoreUsingConfigOverride
    public void testIgnoreUsingConfigOverride() throws Exception
    {
        final ObjectMapper mapper = objectMapper();
        mapper.configOverride(Wrapped.class).setIsIgnoredType(true);

        
        String json = mapper.writeValueAsString(new Wrapper());
        assertEquals(aposToQuotes("{'value':3}"), json);

        
        Wrapper result = mapper.readValue(aposToQuotes("{'value':5,'wrapped':false}"),
                Wrapper.class);
        assertEquals(5, result.value);
    }

// com.fasterxml.jackson.databind.ser.filter.TestJsonFilter::testCheckSiblingContextFilter
    public void testCheckSiblingContextFilter() {
        FilterProvider prov = new SimpleFilterProvider().addFilter("checkSiblingContextFilter",
                new CheckSiblingContextFilter());

        ObjectMapper mapper = new ObjectMapper();
        mapper.setFilterProvider(prov);
        mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        mapper.valueToTree(new CheckSiblingContextBean());
    }

// com.fasterxml.jackson.databind.ser.filter.TestJsonFilter::testSimpleInclusionFilter
    public void testSimpleInclusionFilter() throws Exception
    {
        FilterProvider prov = new SimpleFilterProvider().addFilter("RootFilter",
                SimpleBeanPropertyFilter.filterOutAllExcept("a"));
        assertEquals("{\"a\":\"a\"}", MAPPER.writer(prov).writeValueAsString(new Bean()));

        
        ObjectMapper mapper = new ObjectMapper();
        mapper.setFilterProvider(prov);
        assertEquals("{\"a\":\"a\"}", mapper.writeValueAsString(new Bean()));
    }

// com.fasterxml.jackson.databind.ser.filter.TestJsonFilter::testIncludeAllFilter
    public void testIncludeAllFilter() throws Exception
    {
        FilterProvider prov = new SimpleFilterProvider().addFilter("RootFilter",
                SimpleBeanPropertyFilter.serializeAll());
        assertEquals("{\"a\":\"a\",\"b\":\"b\"}", MAPPER.writer(prov).writeValueAsString(new Bean()));
    }

// com.fasterxml.jackson.databind.ser.filter.TestJsonFilter::testSimpleExclusionFilter
    public void testSimpleExclusionFilter() throws Exception
    {
        FilterProvider prov = new SimpleFilterProvider().addFilter("RootFilter",
                SimpleBeanPropertyFilter.serializeAllExcept("a"));
        assertEquals("{\"b\":\"b\"}", MAPPER.writer(prov).writeValueAsString(new Bean()));
    }

// com.fasterxml.jackson.databind.ser.filter.TestJsonFilter::testMissingFilter
    public void testMissingFilter() throws Exception
    {
        
        try {
            MAPPER.writeValueAsString(new Bean());
            fail("Should have failed without configured filter");
        } catch (JsonMappingException e) { 
            verifyException(e, "Cannot resolve PropertyFilter with id 'RootFilter'");
        }
        
        
        SimpleFilterProvider fp = new SimpleFilterProvider().setFailOnUnknownId(false);
        ObjectMapper mapper = new ObjectMapper();
        mapper.setFilterProvider(fp);
        String json = mapper.writeValueAsString(new Bean());
        assertEquals("{\"a\":\"a\",\"b\":\"b\"}", json);
    }

// com.fasterxml.jackson.databind.ser.filter.TestJsonFilter::testDefaultFilter
    public void testDefaultFilter() throws Exception
    {
        FilterProvider prov = new SimpleFilterProvider().setDefaultFilter(SimpleBeanPropertyFilter.filterOutAllExcept("b"));
        assertEquals("{\"b\":\"b\"}", MAPPER.writer(prov).writeValueAsString(new Bean()));
    }

// com.fasterxml.jackson.databind.ser.filter.TestJsonFilter::testIssue89
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

// com.fasterxml.jackson.databind.ser.filter.TestJsonFilter::testFilterOnProperty
    public void testFilterOnProperty() throws Exception
    {
        FilterProvider prov = new SimpleFilterProvider()
            .addFilter("RootFilter", SimpleBeanPropertyFilter.filterOutAllExcept("a"))
            .addFilter("b", SimpleBeanPropertyFilter.filterOutAllExcept("b"));

        assertEquals("{\"first\":{\"a\":\"a\"},\"second\":{\"b\":\"b\"}}",
                MAPPER.writer(prov).writeValueAsString(new FilteredProps()));
    }

// com.fasterxml.jackson.databind.struct.BackReference1878Test::testChildDeserialization
    public void testChildDeserialization() throws Exception {
        Child child = MAPPER.readValue("{\"b\": {}}", Child.class);
        assertNotNull(child.b);
    }

// com.fasterxml.jackson.databind.struct.EmptyArrayAsNullTest::testSettings
    public void testSettings() {
        assertFalse(MAPPER.isEnabled(DeserializationFeature.ACCEPT_EMPTY_ARRAY_AS_NULL_OBJECT));
        assertFalse(DEFAULT_READER.isEnabled(DeserializationFeature.ACCEPT_EMPTY_ARRAY_AS_NULL_OBJECT));
        assertTrue(READER_WITH_ARRAYS.isEnabled(DeserializationFeature.ACCEPT_EMPTY_ARRAY_AS_NULL_OBJECT));
    }

// com.fasterxml.jackson.databind.struct.EmptyArrayAsNullTest::testPOJOFromEmptyArray
    public void testPOJOFromEmptyArray() throws Exception
    {
        
        try {
            DEFAULT_READER.forType(Bean.class)
                .readValue(EMPTY_ARRAY);
            fail("Should not accept Empty Array for POJO by default");
        } catch (JsonMappingException e) {
            verifyException(e, "START_ARRAY token");
            assertValidLocation(e.getLocation());
        }

        
        Bean result = READER_WITH_ARRAYS.forType(Bean.class)
                .readValue(EMPTY_ARRAY);
        assertNull(result);
    }

// com.fasterxml.jackson.databind.struct.EmptyArrayAsNullTest::testMapFromEmptyArray
    public void testMapFromEmptyArray() throws Exception
    {
        
        try {
            DEFAULT_READER.forType(Map.class)
                .readValue(EMPTY_ARRAY);
            fail("Should not accept Empty Array for Map by default");
        } catch (JsonMappingException e) {
            verifyException(e, "START_ARRAY token");
        }
        
        Map<?,?> result = READER_WITH_ARRAYS.forType(Map.class)
                .readValue(EMPTY_ARRAY);
        assertNull(result);
    }

// com.fasterxml.jackson.databind.struct.EmptyArrayAsNullTest::testEnumMapFromEmptyArray
    public void testEnumMapFromEmptyArray() throws Exception
    {
    
        EnumMap<?,?> result2 = READER_WITH_ARRAYS.forType(new TypeReference<EnumMap<ABC,String>>() { })
                .readValue(EMPTY_ARRAY);
        assertNull(result2);
    }

// com.fasterxml.jackson.databind.struct.EmptyArrayAsNullTest::testWrapperFromEmptyArray
    public void testWrapperFromEmptyArray() throws Exception
    {
        _testNullWrapper(Boolean.class);
        _testNullWrapper(Byte.class);
        _testNullWrapper(Character.class);
        _testNullWrapper(Short.class);
        _testNullWrapper(Integer.class);
        _testNullWrapper(Long.class);
        _testNullWrapper(Float.class);
        _testNullWrapper(Double.class);
    }

// com.fasterxml.jackson.databind.struct.EmptyArrayAsNullTest::testNullStringFromEmptyArray
    public void testNullStringFromEmptyArray() throws Exception {
        _testNullWrapper(String.class);
    }

// com.fasterxml.jackson.databind.struct.EmptyArrayAsNullTest::testNullEnumFromEmptyArray
    public void testNullEnumFromEmptyArray() throws Exception {
        _testNullWrapper(ABC.class);
    }

// com.fasterxml.jackson.databind.struct.EmptyArrayAsNullTest::testStdJdkTypesFromEmptyArray
    public void testStdJdkTypesFromEmptyArray() throws Exception
    {
        _testNullWrapper(BigInteger.class);
        _testNullWrapper(BigDecimal.class);

        _testNullWrapper(UUID.class);

        _testNullWrapper(Date.class);
        _testNullWrapper(Calendar.class);
    }

// com.fasterxml.jackson.databind.struct.FormatFeatureAcceptSingleTest::testSingleStringArrayRead
    public void testSingleStringArrayRead() throws Exception {
        String json = aposToQuotes(
                "{ 'values': 'first' }");
        StringArrayWrapper result = MAPPER.readValue(json, StringArrayWrapper.class);
        assertNotNull(result.values);
        assertEquals(1, result.values.length);
        assertEquals("first", result.values[0]);

        
        ObjectMapper mapper = new ObjectMapper();
        mapper.configOverride(String[].class).setFormat(JsonFormat.Value.empty()
                .withFeature(JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY));
        StringArrayNotAnnoted result2 = mapper.readValue(json, StringArrayNotAnnoted.class);
        assertNotNull(result2.values);
        assertEquals(1, result2.values.length);
        assertEquals("first", result2.values[0]);
    }

// com.fasterxml.jackson.databind.struct.FormatFeatureAcceptSingleTest::testSingleIntArrayRead
    public void testSingleIntArrayRead() throws Exception {
        String json = aposToQuotes(
                "{ 'values': 123 }");
        IntArrayWrapper result = MAPPER.readValue(json, IntArrayWrapper.class);
        assertNotNull(result.values);
        assertEquals(1, result.values.length);
        assertEquals(123, result.values[0]);
    }

// com.fasterxml.jackson.databind.struct.FormatFeatureAcceptSingleTest::testSingleLongArrayRead
    public void testSingleLongArrayRead() throws Exception {
        String json = aposToQuotes(
                "{ 'values': -205 }");
        LongArrayWrapper result = MAPPER.readValue(json, LongArrayWrapper.class);
        assertNotNull(result.values);
        assertEquals(1, result.values.length);
        assertEquals(-205L, result.values[0]);
    }

// com.fasterxml.jackson.databind.struct.FormatFeatureAcceptSingleTest::testSingleBooleanArrayRead
    public void testSingleBooleanArrayRead() throws Exception {
        String json = aposToQuotes(
                "{ 'values': true }");
        BooleanArrayWrapper result = MAPPER.readValue(json, BooleanArrayWrapper.class);
        assertNotNull(result.values);
        assertEquals(1, result.values.length);
        assertEquals(true, result.values[0]);
    }

// com.fasterxml.jackson.databind.struct.FormatFeatureAcceptSingleTest::testSingleDoubleArrayRead
    public void testSingleDoubleArrayRead() throws Exception {
        String json = aposToQuotes(
                "{ 'values': -0.5 }");
        DoubleArrayWrapper result = MAPPER.readValue(json, DoubleArrayWrapper.class);
        assertNotNull(result.values);
        assertEquals(1, result.values.length);
        assertEquals(-0.5, result.values[0]);
    }

// com.fasterxml.jackson.databind.struct.FormatFeatureAcceptSingleTest::testSingleFloatArrayRead
    public void testSingleFloatArrayRead() throws Exception {
        String json = aposToQuotes(
                "{ 'values': 0.25 }");
        FloatArrayWrapper result = MAPPER.readValue(json, FloatArrayWrapper.class);
        assertNotNull(result.values);
        assertEquals(1, result.values.length);
        assertEquals(0.25f, result.values[0]);
    }

// com.fasterxml.jackson.databind.struct.FormatFeatureAcceptSingleTest::testSingleElementArrayRead
    public void testSingleElementArrayRead() throws Exception {
        String json = aposToQuotes(
                "{ 'roles': { 'Name': 'User', 'ID': '333' } }");
        RolesInArray response = MAPPER.readValue(json, RolesInArray.class);
        assertNotNull(response.roles);
        assertEquals(1, response.roles.length);
        assertEquals("333", response.roles[0].ID);
    }

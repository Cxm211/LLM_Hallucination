// buggy code
    public Object generateId(Object forPojo) {
        // 04-Jun-2016, tatu: As per [databind#1255], need to consider possibility of
        //    id being generated for "alwaysAsId", but not being written as POJO; regardless,
        //    need to use existing id if there is one:
            id = generator.generateId(forPojo);
        return id;
    }

// relevant test
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

// com.fasterxml.jackson.databind.objectid.AlwaysAsReferenceFirstTest::testIssue1255
    public void testIssue1255() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        Foo mo = new Foo();
        mo.bar1 = new Bar();
        mo.bar2 = mo.bar1;

        String json = mapper.writeValueAsString(mo);

        Foo result = mapper.readValue(json, Foo.class);
        assertNotNull(result);
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

// com.fasterxml.jackson.databind.objectid.TestObjectId154::testObjectAndTypeId
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

// com.fasterxml.jackson.databind.objectid.TestObjectIdSerialization::testInvalidProp
    public void testInvalidProp() throws Exception
    {
        try {
            MAPPER.writeValueAsString(new Broken());
            fail("Should have thrown an exception");
        } catch (JsonMappingException e) {
            verifyException(e, "can not find property with name 'id'");
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

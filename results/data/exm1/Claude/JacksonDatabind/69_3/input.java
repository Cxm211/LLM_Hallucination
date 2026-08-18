// buggy code
    public void addDelegatingCreator(AnnotatedWithParams creator, boolean explicit,
            SettableBeanProperty[] injectables)
    {
        if (creator.getParameterType(0).isCollectionLikeType()) {
            verifyNonDup(creator, C_ARRAY_DELEGATE, explicit);
                _arrayDelegateArgs = injectables;
        } else {
            verifyNonDup(creator, C_DELEGATE, explicit);
                _delegateArgs = injectables;
        }
    }

    public void addPropertyCreator(AnnotatedWithParams creator, boolean explicit,
            SettableBeanProperty[] properties)
    {
        verifyNonDup(creator, C_PROPS, explicit);
            // Better ensure we have no duplicate names either...
            if (properties.length > 1) {
                HashMap<String,Integer> names = new HashMap<String,Integer>();
                for (int i = 0, len = properties.length; i < len; ++i) {
                    String name = properties[i].getName();
                    /* [Issue-13]: Need to consider Injectables, which may not have
                     *   a name at all, and need to be skipped
                     */
                    if (name.length() == 0 && properties[i].getInjectableValueId() != null) {
                        continue;
                    }
                    Integer old = names.put(name, Integer.valueOf(i));
                    if (old != null) {
                        throw new IllegalArgumentException("Duplicate creator property \""+name+"\" (index "+old+" vs "+i+")");
                    }
                }
            }
            _propertyBasedArgs = properties;
    }

    protected void verifyNonDup(AnnotatedWithParams newOne, int typeIndex, boolean explicit)
    {
        final int mask = (1 << typeIndex);
        _hasNonDefaultCreator = true;
        AnnotatedWithParams oldOne = _creators[typeIndex];
        // already had an explicitly marked one?
        if (oldOne != null) {
            boolean verify;
            if ((_explicitCreators & mask) != 0) { // already had explicitly annotated, leave as-is
                // but skip, if new one not annotated
                if (!explicit) {
                    return;
                }
                // both explicit: verify
                verify = true;
            } else {
                // otherwise only verify if neither explicitly annotated.
                verify = !explicit;
            }

            // one more thing: ok to override in sub-class
            if (verify && (oldOne.getClass() == newOne.getClass())) {
                // [databind#667]: avoid one particular class of bogus problems
                Class<?> oldType = oldOne.getRawParameterType(0);
                Class<?> newType = newOne.getRawParameterType(0);

                if (oldType == newType) {
                    throw new IllegalArgumentException("Conflicting "+TYPE_DESCS[typeIndex]
                            +" creators: already had explicitly marked "+oldOne+", encountered "+newOne);
                }
                // otherwise, which one to choose?
                if (newType.isAssignableFrom(oldType)) {
                    // new type more generic, use old
                    return;
                }
                // new type more specific, use it
            }
        }
        if (explicit) {
            _explicitCreators |= mask;
        }
        _creators[typeIndex] = _fixAccess(newOne);
    }

// relevant test
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

// com.fasterxml.jackson.databind.seq.ObjectReaderTest::testParserFeatures
    public void testParserFeatures() throws Exception
    {
        final String JSON = "[  7 ]";
        
        ObjectReader reader = MAPPER.readerFor(int[].class)
                .with(JsonParser.Feature.ALLOW_COMMENTS);

        int[] value = reader.readValue(JSON);
        assertNotNull(value);
        assertEquals(1, value.length);
        assertEquals(7, value[0]);

        
        try {
            reader.without(JsonParser.Feature.ALLOW_COMMENTS).readValue(JSON);
            fail("Should not have passed");
        } catch (JsonProcessingException e) {
            verifyException(e, "foo");
        }
    }

// com.fasterxml.jackson.databind.seq.ObjectReaderTest::testNoPointerLoading
    public void testNoPointerLoading() throws Exception {
        final String source = "{\"foo\":{\"bar\":{\"caller\":{\"name\":{\"value\":1234}}}}}";

        JsonNode tree = MAPPER.readTree(source);
        JsonNode node = tree.at("/foo/bar/caller");
        POJO pojo = MAPPER.treeToValue(node, POJO.class);
        assertTrue(pojo.name.containsKey("value"));
        assertEquals(1234, pojo.name.get("value"));
    }

// com.fasterxml.jackson.databind.seq.ObjectReaderTest::testPointerLoading
    public void testPointerLoading() throws Exception {
        final String source = "{\"foo\":{\"bar\":{\"caller\":{\"name\":{\"value\":1234}}}}}";

        ObjectReader reader = MAPPER.readerFor(POJO.class).at("/foo/bar/caller");

        POJO pojo = reader.readValue(source);
        assertTrue(pojo.name.containsKey("value"));
        assertEquals(1234, pojo.name.get("value"));
    }

// com.fasterxml.jackson.databind.seq.ObjectReaderTest::testPointerLoadingAsJsonNode
    public void testPointerLoadingAsJsonNode() throws Exception {
        final String source = "{\"foo\":{\"bar\":{\"caller\":{\"name\":{\"value\":1234}}}}}";

        ObjectReader reader = MAPPER.readerFor(POJO.class).at("/foo/bar/caller");

        JsonNode node = reader.readTree(source);
        assertTrue(node.has("name"));
        assertEquals("{\"value\":1234}", node.get("name").toString());
    }

// com.fasterxml.jackson.databind.seq.ObjectReaderTest::testPointerLoadingMappingIteratorOne
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

// com.fasterxml.jackson.databind.seq.ObjectReaderTest::testPointerLoadingMappingIteratorMany
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

// com.fasterxml.jackson.databind.ser.AnyGetterTest::testSimpleJsonValue
    public void testSimpleJsonValue() throws Exception
    {
        String json = MAPPER.writeValueAsString(new Bean());
        Map<?,?> map = MAPPER.readValue(json, Map.class);
        assertEquals(2, map.size());
        assertEquals(Integer.valueOf(3), map.get("x"));
        assertEquals(Boolean.TRUE, map.get("a"));
    }

// com.fasterxml.jackson.databind.ser.AnyGetterTest::testAnyOnly
    public void testAnyOnly() throws Exception
    {
        ObjectMapper m;

        
        m = new ObjectMapper();
        m.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, true);
        String json = serializeAsString(m, new AnyOnlyBean());
        assertEquals("{\"a\":3}", json);

        
        m = new ObjectMapper();
        m.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        json = serializeAsString(m, new AnyOnlyBean());
        assertEquals("{\"a\":3}", json);
    }

// com.fasterxml.jackson.databind.ser.AnyGetterTest::testAnyWithNull
    public void testAnyWithNull() throws Exception
    {
        MapAsAny input = new MapAsAny();
        input.add("bar", null);
        assertEquals(aposToQuotes("{'bar':null}"),
                MAPPER.writeValueAsString(input));
    }

// com.fasterxml.jackson.databind.ser.AnyGetterTest::testIssue705
    public void testIssue705() throws Exception
    {
        Issue705Bean input = new Issue705Bean("key", "value");        
        String json = MAPPER.writeValueAsString(input);
        assertEquals("{\"stuff\":\"[key/value]\"}", json);
    }

// com.fasterxml.jackson.databind.ser.AnyGetterTest::testAnyGetterWithValueSerializer
    public void testAnyGetterWithValueSerializer() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        Bean1124 input = new Bean1124();
        input.addAdditionalProperty("key", "value");
        String json = mapper.writeValueAsString(input);
        assertEquals("{\"key\":\"VALUE\"}", json);
    }

// com.fasterxml.jackson.databind.ser.FieldSerializationTest::testSimpleAutoDetect
    public void testSimpleAutoDetect() throws Exception
    {
        SimpleFieldBean bean = new SimpleFieldBean();
        
        bean.x = 13;
        Map<String,Object> result = writeAndMap(MAPPER, bean);
        assertEquals(2, result.size());
        assertEquals(Integer.valueOf(13), result.get("x"));
        assertEquals(Integer.valueOf(0), result.get("y"));
    }

// com.fasterxml.jackson.databind.ser.FieldSerializationTest::testSimpleAnnotation
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

// com.fasterxml.jackson.databind.ser.FieldSerializationTest::testTransientAndStatic
    public void testTransientAndStatic() throws Exception
    {
        TransientBean bean = new TransientBean();
        Map<String,Object> result = writeAndMap(MAPPER, bean);
        assertEquals(1, result.size());
        assertEquals(Integer.valueOf(0), result.get("a"));
    }

// com.fasterxml.jackson.databind.ser.FieldSerializationTest::testNoAutoDetect
    public void testNoAutoDetect() throws Exception
    {
        NoAutoDetectBean bean = new NoAutoDetectBean();
        bean._z = -4;
        Map<String,Object> result = writeAndMap(MAPPER, bean);
        assertEquals(1, result.size());
        assertEquals(Integer.valueOf(-4), result.get("z"));
    }

// com.fasterxml.jackson.databind.ser.FieldSerializationTest::testMethodPrecedence
    public void testMethodPrecedence() throws Exception
    {
        FieldAndMethodBean bean = new FieldAndMethodBean();
        bean.z = 9;
        assertEquals(10, bean.getZ());
        assertEquals("{\"z\":10}", MAPPER.writeValueAsString(bean));
    }

// com.fasterxml.jackson.databind.ser.FieldSerializationTest::testOkDupFields
    public void testOkDupFields() throws Exception
    {
        OkDupFieldBean bean = new OkDupFieldBean(1, 2);
        Map<String,Object> json = writeAndMap(MAPPER, bean);
        assertEquals(2, json.size());
        assertEquals(Integer.valueOf(1), json.get("x"));
        assertEquals(Integer.valueOf(2), json.get("y"));
    }

// com.fasterxml.jackson.databind.ser.FieldSerializationTest::testIssue240
    public void testIssue240() throws Exception
    {
        Item240 bean = new Item240("a12", null);
        assertEquals(MAPPER.writeValueAsString(bean), "{\"id\":\"a12\"}");
    }

// com.fasterxml.jackson.databind.ser.FieldSerializationTest::testFailureDueToDups
    public void testFailureDueToDups() throws Exception
    {
        try {
            writeAndMap(MAPPER, new DupFieldBean());
        } catch (JsonMappingException e) {
            verifyException(e, "Multiple fields representing");
        }
    }

// com.fasterxml.jackson.databind.ser.FieldSerializationTest::testFailureDueToDupField
    public void testFailureDueToDupField() throws Exception
    {
        try {
            writeAndMap(MAPPER, new DupFieldBean2());
        } catch (JsonMappingException e) {
            verifyException(e, "Multiple fields representing");
        }
    }

// com.fasterxml.jackson.databind.ser.TestAnnotationInheritance::testSimpleGetterInheritance
    public void testSimpleGetterInheritance() throws Exception
    {
        ObjectMapper m = new ObjectMapper();
        Map<String,Object> result = writeAndMap(m, new PojoSubclass());
        assertEquals(2, result.size());
        assertEquals(Integer.valueOf(7), result.get("length"));
        assertEquals(Integer.valueOf(9), result.get("width"));
    }

// com.fasterxml.jackson.databind.ser.TestAnnotationInheritance::testSimpleGetterInterfaceImpl
    public void testSimpleGetterInterfaceImpl() throws Exception
    {
        ObjectMapper m = new ObjectMapper();
        Map<String,Object> result = writeAndMap(m, new PojoImpl());
        
        assertEquals(3, result.size());
        assertEquals(Integer.valueOf(5), result.get("foobar"));
        assertEquals(Integer.valueOf(1), result.get("width"));
        assertEquals(Integer.valueOf(2), result.get("length"));
    }

// com.fasterxml.jackson.databind.ser.TestAnnotations::testSimpleGetter
    public void testSimpleGetter() throws Exception
    {
        Map<String,Object> result = writeAndMap(MAPPER, new SizeClassGetter());
        assertEquals(3, result.size());
        assertEquals(Integer.valueOf(3), result.get("size"));
        assertEquals(Integer.valueOf(-17), result.get("length"));
        assertEquals(Integer.valueOf(0), result.get("value"));
    }

// com.fasterxml.jackson.databind.ser.TestAnnotations::testSimpleGetter2
    public void testSimpleGetter2() throws Exception
    {
        Map<String,Object> result = writeAndMap(MAPPER, new SizeClassGetter2());
        assertEquals(1, result.size());
        assertEquals(Integer.valueOf(3), result.get("x"));
    }

// com.fasterxml.jackson.databind.ser.TestAnnotations::testSimpleGetter3
    public void testSimpleGetter3() throws Exception
    {
        Map<String,Object> result = writeAndMap(MAPPER, new SizeClassGetter3());
        assertEquals(1, result.size());
        assertEquals(Integer.valueOf(8), result.get("y"));
    }

// com.fasterxml.jackson.databind.ser.TestAnnotations::testGetterInheritance
    public void testGetterInheritance() throws Exception
    {
        Map<String,Object> result = writeAndMap(MAPPER, new SubClassBean());
        assertEquals(3, result.size());
        assertEquals(Integer.valueOf(1), result.get("x"));
        assertEquals(Integer.valueOf(2), result.get("y"));
        assertEquals(Integer.valueOf(3), result.get("z"));
    }

// com.fasterxml.jackson.databind.ser.TestAnnotations::testClassSerializer
    public void testClassSerializer() throws Exception
    {
        StringWriter sw = new StringWriter();
        MAPPER.writeValue(sw, new ClassSerializer());
        assertEquals("true", sw.toString());
    }

// com.fasterxml.jackson.databind.ser.TestAnnotations::testActiveMethodSerializer
    public void testActiveMethodSerializer() throws Exception
    {
        StringWriter sw = new StringWriter();
        MAPPER.writeValue(sw, new ClassMethodSerializer(13));
        
        assertEquals("{\"x\":\"X13X\"}", sw.toString());
    }

// com.fasterxml.jackson.databind.ser.TestAnnotations::testInactiveMethodSerializer
    public void testInactiveMethodSerializer() throws Exception
    {
        String json = MAPPER.writeValueAsString(new InactiveClassMethodSerializer(8));
        
        assertEquals("{\"x\":8}", json);
    }

// com.fasterxml.jackson.databind.ser.TestAnnotations::testGettersWithoutSetters
    public void testGettersWithoutSetters() throws Exception
    {
        ObjectMapper m = new ObjectMapper();
        GettersWithoutSetters bean = new GettersWithoutSetters(123);
        assertFalse(m.isEnabled(MapperFeature.REQUIRE_SETTERS_FOR_GETTERS));
    
        
        assertEquals("{\"a\":3,\"b\":4,\"c\":5,\"d\":6}", m.writeValueAsString(bean));

        
        m = new ObjectMapper();
        m.enable(MapperFeature.REQUIRE_SETTERS_FOR_GETTERS);
        assertEquals("{\"a\":3,\"c\":5,\"d\":6}", m.writeValueAsString(bean));
    }

// com.fasterxml.jackson.databind.ser.TestAnnotations::testGettersWithoutSettersOverride
    public void testGettersWithoutSettersOverride() throws Exception
    {
        GettersWithoutSetters2 bean = new GettersWithoutSetters2();
        ObjectMapper m = new ObjectMapper();
        m.enable(MapperFeature.REQUIRE_SETTERS_FOR_GETTERS);
        assertEquals("{\"a\":123}", m.writeValueAsString(bean));
    }

// com.fasterxml.jackson.databind.ser.TestAutoDetect::testDefaults
    public void testDefaults() throws Exception
    {
        ObjectMapper m = new ObjectMapper();
        
        assertEquals("{\"p1\":\"public\"}",
                     m.writeValueAsString(new FieldBean()));
        assertEquals("{\"a\":\"a\"}",
                     m.writeValueAsString(new MethodBean()));
    }

// com.fasterxml.jackson.databind.ser.TestAutoDetect::testProtectedViaAnnotations
    public void testProtectedViaAnnotations() throws Exception
    {
        ObjectMapper m = new ObjectMapper();

        Map<String,Object> result = writeAndMap(m, new ProtFieldBean());
        assertEquals(2, result.size());
        assertEquals("public", result.get("p1"));
        assertEquals("protected", result.get("p2"));
        assertNull(result.get("p3"));

        result = writeAndMap(m, new ProtMethodBean());
        assertEquals(2, result.size());
        assertEquals("a", result.get("a"));
        assertEquals("b", result.get("b"));
        assertNull(result.get("c"));
    }

// com.fasterxml.jackson.databind.ser.TestAutoDetect::testPrivateUsingGlobals
    public void testPrivateUsingGlobals() throws Exception
    {
        ObjectMapper m = new ObjectMapper();
        VisibilityChecker<?> vc = m.getVisibilityChecker();
        vc = vc.withFieldVisibility(JsonAutoDetect.Visibility.ANY);
        m.setVisibility(vc);
        
        Map<String,Object> result = writeAndMap(m, new FieldBean());
        assertEquals(3, result.size());
        assertEquals("public", result.get("p1"));
        assertEquals("protected", result.get("p2"));
        assertEquals("private", result.get("p3"));

        m = new ObjectMapper();
        vc = m.getVisibilityChecker();
        vc = vc.withGetterVisibility(JsonAutoDetect.Visibility.ANY);
        m.setVisibility(vc);
        result = writeAndMap(m, new MethodBean());
        assertEquals(3, result.size());
        assertEquals("a", result.get("a"));
        assertEquals("b", result.get("b"));
        assertEquals("c", result.get("c"));
    }

// com.fasterxml.jackson.databind.ser.TestAutoDetect::testBasicSetup
    public void testBasicSetup() throws Exception
    {
        ObjectMapper m = new ObjectMapper();
        VisibilityChecker<?> vc = m.getVisibilityChecker();
        vc = vc.with(JsonAutoDetect.Visibility.ANY);
        m.setVisibility(vc);

        Map<String,Object> result = writeAndMap(m, new FieldBean());
        assertEquals(3, result.size());
        assertEquals("public", result.get("p1"));
        assertEquals("protected", result.get("p2"));
        assertEquals("private", result.get("p3"));
    }

// com.fasterxml.jackson.databind.ser.TestAutoDetect::testMapperShortcutMethods
    public void testMapperShortcutMethods() throws Exception
    {
        ObjectMapper m = new ObjectMapper();
        m.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);

        Map<String,Object> result = writeAndMap(m, new FieldBean());
        assertEquals(3, result.size());
        assertEquals("public", result.get("p1"));
        assertEquals("protected", result.get("p2"));
        assertEquals("private", result.get("p3"));
    }

// com.fasterxml.jackson.databind.ser.TestCollectionSerialization::testCollections
    public void testCollections() throws IOException
    {
        
        final int entryLen = 98;

        for (int type = 0; type < 4; ++type) {
            Object value;

            if (type == 0) { 
                int[] ints = new int[entryLen];
                for (int i = 0; i < entryLen; ++i) {
                    ints[i] = Integer.valueOf(i);
                }
                value = ints;
            } else {
                Collection<Integer> c;

                switch (type) {
                case 1:
                    c = new LinkedList<Integer>();
                    break;
                case 2:
                    c = new TreeSet<Integer>(); 
                    break;
                default:
                    c = new ArrayList<Integer>();
                    break;
                }
                for (int i = 0; i < entryLen; ++i) {
                    c.add(Integer.valueOf(i));
                }
                value = c;
            }
            String json = MAPPER.writeValueAsString(value);
            
            
            JsonParser jp = new JsonFactory().createParser(json);
            assertToken(JsonToken.START_ARRAY, jp.nextToken());
            for (int i = 0; i < entryLen; ++i) {
                assertToken(JsonToken.VALUE_NUMBER_INT, jp.nextToken());
                assertEquals(i, jp.getIntValue());
            }
            assertToken(JsonToken.END_ARRAY, jp.nextToken());
            jp.close();
        }
    }

// com.fasterxml.jackson.databind.ser.TestCollectionSerialization::testBigCollection
    public void testBigCollection() throws IOException
    {
        final int COUNT = 9999;
        ArrayList<Integer> value = new ArrayList<Integer>();
        for (int i = 0; i <= COUNT; ++i) {
            value.add(i);
        }
        
        for (int mode = 0; mode < 3; ++mode) {
            JsonParser jp = null;
            switch (mode) {
            case 0:
                {
                    byte[] data = MAPPER.writeValueAsBytes(value);
                    jp = new JsonFactory().createParser(data);
                }
                break;
            case 1:
                {
                    StringWriter sw = new StringWriter(value.size());
                    MAPPER.writeValue(sw, value);
                    jp = createParserUsingReader(sw.toString());
                }
                break;
            case 2:
                {
                    String str = MAPPER.writeValueAsString(value);
                    jp = createParserUsingReader(str);
                }
                break;
            }

            
            assertToken(JsonToken.START_ARRAY, jp.nextToken());
            for (int i = 0; i <= COUNT; ++i) {
                assertEquals(JsonToken.VALUE_NUMBER_INT, jp.nextToken());
                assertEquals(i, jp.getIntValue());
            }
            assertToken(JsonToken.END_ARRAY, jp.nextToken());
            jp.close();
        }
    }

// com.fasterxml.jackson.databind.ser.TestCollectionSerialization::testEnumMap
    public void testEnumMap() throws IOException
    {
        EnumMap<Key,String> map = new EnumMap<Key,String>(Key.class);
        map.put(Key.B, "xyz");
        map.put(Key.C, "abc");
        
        String json = MAPPER.writeValueAsString(map);
        assertEquals("{\"B\":\"xyz\",\"C\":\"abc\"}",json.trim());
    }

// com.fasterxml.jackson.databind.ser.TestCollectionSerialization::testEmptyBeanCollection
    public void testEmptyBeanCollection() throws IOException
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
    public void testEmptyBeanEnumMap() throws IOException
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
    public void testNullBeanEnumMap() throws IOException
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

// com.fasterxml.jackson.databind.ser.TestConfig::testDateFormatConfig
    public void testDateFormatConfig() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        TimeZone tz1 = TimeZone.getTimeZone("America/Los_Angeles");
        TimeZone tz2 = TimeZone.getTimeZone("Central Standard Time");

        
        assertEquals(tz1, tz1);
        assertEquals(tz2, tz2);
        if (tz1.equals(tz2)) {
            fail();
        }

        mapper.setTimeZone(tz1);
        assertEquals(tz1, mapper.getSerializationConfig().getTimeZone());
        assertEquals(tz1, mapper.getDeserializationConfig().getTimeZone());

        
        assertEquals(tz1, mapper.writer().getConfig().getTimeZone());
        assertEquals(tz1, mapper.reader().getConfig().getTimeZone());
        
        SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        f.setTimeZone(tz2);
        mapper.setDateFormat(f);

        
        assertEquals(tz1, mapper.getSerializationConfig().getTimeZone());
        assertEquals(tz1, mapper.getDeserializationConfig().getTimeZone());
        assertEquals(tz1, mapper.writer().getConfig().getTimeZone());
        assertEquals(tz1, mapper.reader().getConfig().getTimeZone());
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
        m.writerFor(CloseableBean.class).writeValueAsString(bean);
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

// com.fasterxml.jackson.databind.ser.TestFeatures::testVisibilityFeatures
    public void testVisibilityFeatures() throws Exception
    {
        ObjectMapper om = new ObjectMapper();
        
        om.configure(MapperFeature.AUTO_DETECT_FIELDS, false);
        om.configure(MapperFeature.AUTO_DETECT_GETTERS, false);
        om.configure(MapperFeature.AUTO_DETECT_SETTERS, false);
        om.configure(MapperFeature.AUTO_DETECT_IS_GETTERS, false);
        om.configure(MapperFeature.USE_GETTERS_AS_SETTERS, false);
        om.configure(MapperFeature.CAN_OVERRIDE_ACCESS_MODIFIERS, true);
        om.configure(MapperFeature.INFER_PROPERTY_MUTATORS, false);
        om.configure(MapperFeature.USE_ANNOTATIONS, true);

        JavaType javaType = om.getTypeFactory().constructType(TCls.class);        
        BeanDescription desc = (BeanDescription) om.getSerializationConfig().introspect(javaType);
        List<BeanPropertyDefinition> props = desc.findProperties();
        if (props.size() != 1) {
            fail("Should find 1 property, not "+props.size()+"; properties = "+props);
        }
    }

// com.fasterxml.jackson.databind.ser.TestGenericTypes::testIssue468a
    public void testIssue468a() throws Exception
    {
        Person1 p1 = new Person1("John");
        p1.setAccount(new Key<Account>(new Account("something", 42L)));
        
        
        String json = MAPPER.writeValueAsString(p1);

        
        Map<String,Object> map = MAPPER.readValue(json, Map.class);
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

        
        String json = MAPPER.writeValueAsString(p2);

        
        Map<String,Object> map = MAPPER.readValue(json, Map.class);
        assertEquals("John", map.get("name"));
        Object ob = map.get("accounts");
        assertNotNull(ob);
        List<?> acctList = (List<?>) ob;
        assertEquals(3, acctList.size());
        
    }

// com.fasterxml.jackson.databind.ser.TestGenericTypes::testUnboundTypes
    public void testUnboundTypes() throws Exception
    {
        GenericBogusWrapper<Integer> list = new GenericBogusWrapper<Integer>(Integer.valueOf(7));
        String json = MAPPER.writeValueAsString(list);
        assertEquals("{\"wrapped\":{\"value\":7}}", json);
    }

// com.fasterxml.jackson.databind.ser.TestGenericTypes::testRootTypeForCollections727
    public void testRootTypeForCollections727() throws Exception
    {
        List<Base727> input = new ArrayList<Base727>();
        input.add(new Impl727(1, 2));

        final String EXP = aposToQuotes("[{'a':1,'b':2}]");
        
        assertEquals(EXP, MAPPER.writeValueAsString(input));
        assertEquals(EXP, MAPPER.writer().writeValueAsString(input));

        
        TypeReference<?> typeRef = new TypeReference<List<Base727>>() { };
        assertEquals(EXP, MAPPER.writer().forType(typeRef).writeValueAsString(input));
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
    public void testBigDecimal() throws Exception
    {
        Map<String, Object> map = new HashMap<String, Object>();
        String PI_STR = "3.14159265";
        map.put("pi", new BigDecimal(PI_STR));
        String str = MAPPER.writeValueAsString(map);
        assertEquals("{\"pi\":3.14159265}", str);
    }

// com.fasterxml.jackson.databind.ser.TestJdkTypes::testBigDecimalAsPlainString
    public void testBigDecimalAsPlainString() throws Exception
    {
        final ObjectMapper mapper = new ObjectMapper();

        mapper.enable(JsonGenerator.Feature.WRITE_BIGDECIMAL_AS_PLAIN);
        Map<String, Object> map = new HashMap<String, Object>();
        String PI_STR = "3.00000000";
        map.put("pi", new BigDecimal(PI_STR));
        String str = mapper.writeValueAsString(map);
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

        assertEquals(quote("en_US"), MAPPER.writeValueAsString(Locale.US));

        
        assertEquals(quote(""), MAPPER.writeValueAsString(Locale.ROOT));
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
            verifyException(e, "types not related");
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

// com.fasterxml.jackson.databind.ser.TestJsonValue::testJsonValueWithCustomOverride
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

        
        ObjectWriter w = mapper.writerFor(BaseType.class);
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

        
        ObjectWriter w = mapper.writerFor(BaseInterface.class);
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
        String json = mapper.writerFor(BaseInterface[].class).writeValueAsString(ob);
        
        assertEquals("[{\"b\":3}]", json);
    }

// com.fasterxml.jackson.databind.ser.TestRootType::testIncompatibleRootType
    public void testIncompatibleRootType() throws Exception
    {
        ObjectMapper mapper = objectMapper();
        SubType bean = new SubType();

        
        ObjectWriter w = mapper.writerFor(HashMap.class);
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
        
        
        String json = mapper.writerFor(collectionType).writeValueAsString(typedList);
        assertEquals(EXP, json);

        StringWriter out = new StringWriter();
        JsonFactory f = new JsonFactory();
        mapper.writerFor(collectionType).writeValue(f.createGenerator(out), typedList);

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
        assertEquals("123", mapper.writerFor(Integer.TYPE).writeValueAsString(Integer.valueOf(123)));
        assertEquals("456", mapper.writerFor(Long.TYPE).writeValueAsString(Long.valueOf(456L)));
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

        ObjectWriter writer = WRAP_ROOT_MAPPER.writerFor(TestCommandParent.class);
        String json =  writer.writeValueAsString(cmd);

        assertEquals(json, "{\"TestCommandParent\":{\"uuid\":\"1234\",\"type\":1}}");
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

// com.fasterxml.jackson.databind.struct.FormatFeaturesTest::testWithArrayTypes
    public void testWithArrayTypes() throws Exception
    {
        
        assertEquals(aposToQuotes("{'strings':'a','ints':[1],'bools':[true]}"),
                MAPPER.writeValueAsString(new WrapWriteWithArrays()));

        
        assertEquals(aposToQuotes("{'strings':'a','ints':[1],'bools':true}"),
                MAPPER.writer().with(SerializationFeature.WRITE_SINGLE_ELEM_ARRAYS_UNWRAPPED)
                .writeValueAsString(new WrapWriteWithArrays()));

        
        assertEquals(aposToQuotes("{'strings':'a','ints':[1],'bools':[true]}"),
                MAPPER.writer().without(SerializationFeature.WRITE_SINGLE_ELEM_ARRAYS_UNWRAPPED)
                .writeValueAsString(new WrapWriteWithArrays()));
    }

// com.fasterxml.jackson.databind.struct.FormatFeaturesTest::testWithCollectionTypes
    public void testWithCollectionTypes() throws Exception
    {
        
        assertEquals(aposToQuotes("{'strings':'a','ints':[1],'bools':[true],'enums':'B'}"),
                MAPPER.writeValueAsString(new WrapWriteWithCollections()));

        
        assertEquals(aposToQuotes("{'strings':'a','ints':[1],'bools':true,'enums':'B'}"),
                MAPPER.writer().with(SerializationFeature.WRITE_SINGLE_ELEM_ARRAYS_UNWRAPPED)
                .writeValueAsString(new WrapWriteWithCollections()));

        
        assertEquals(aposToQuotes("{'strings':'a','ints':[1],'bools':[true],'enums':'B'}"),
                MAPPER.writer().without(SerializationFeature.WRITE_SINGLE_ELEM_ARRAYS_UNWRAPPED)
                .writeValueAsString(new WrapWriteWithCollections()));
    }

// com.fasterxml.jackson.databind.struct.FormatFeaturesTest::testSingleStringArrayRead
    public void testSingleStringArrayRead() throws Exception {
        String json = aposToQuotes(
                "{ 'values': 'first' }");
        StringArrayWrapper result = MAPPER.readValue(json, StringArrayWrapper.class);
        assertNotNull(result.values);
        assertEquals(1, result.values.length);
        assertEquals("first", result.values[0]);
    }

// com.fasterxml.jackson.databind.struct.FormatFeaturesTest::testSingleIntArrayRead
    public void testSingleIntArrayRead() throws Exception {
        String json = aposToQuotes(
                "{ 'values': 123 }");
        IntArrayWrapper result = MAPPER.readValue(json, IntArrayWrapper.class);
        assertNotNull(result.values);
        assertEquals(1, result.values.length);
        assertEquals(123, result.values[0]);
    }

// com.fasterxml.jackson.databind.struct.FormatFeaturesTest::testSingleLongArrayRead
    public void testSingleLongArrayRead() throws Exception {
        String json = aposToQuotes(
                "{ 'values': -205 }");
        LongArrayWrapper result = MAPPER.readValue(json, LongArrayWrapper.class);
        assertNotNull(result.values);
        assertEquals(1, result.values.length);
        assertEquals(-205L, result.values[0]);
    }

// com.fasterxml.jackson.databind.struct.FormatFeaturesTest::testSingleElementArrayRead
    public void testSingleElementArrayRead() throws Exception {
        String json = aposToQuotes(
                "{ 'roles': { 'Name': 'User', 'ID': '333' } }");
        RolesInArray response = MAPPER.readValue(json, RolesInArray.class);
        assertNotNull(response.roles);
        assertEquals(1, response.roles.length);
        assertEquals("333", response.roles[0].ID);
    }

// com.fasterxml.jackson.databind.struct.FormatFeaturesTest::testSingleStringListRead
    public void testSingleStringListRead() throws Exception {
        String json = aposToQuotes(
                "{ 'values': 'first' }");
        StringListWrapper result = MAPPER.readValue(json, StringListWrapper.class);
        assertNotNull(result.values);
        assertEquals(1, result.values.size());
        assertEquals("first", result.values.get(0));
    }

// com.fasterxml.jackson.databind.struct.FormatFeaturesTest::testSingleElementListRead
    public void testSingleElementListRead() throws Exception {
        String json = aposToQuotes(
                "{ 'roles': { 'Name': 'User', 'ID': '333' } }");
        RolesInList response = MAPPER.readValue(json, RolesInList.class);
        assertNotNull(response.roles);
        assertEquals(1, response.roles.size());
        assertEquals("333", response.roles.get(0).ID);
    }

// com.fasterxml.jackson.databind.struct.FormatFeaturesTest::testSingleEnumSetRead
    public void testSingleEnumSetRead() throws Exception {
        String json = aposToQuotes("{ 'values': 'B' }");
        EnumSetWrapper result = MAPPER.readValue(json, EnumSetWrapper.class);
        assertNotNull(result.values);
        assertEquals(1, result.values.size());
        assertEquals(ABC.B, result.values.iterator().next());
    }

// com.fasterxml.jackson.databind.struct.PojoAsArray646Test::testWithCustomTypeId
    public void testWithCustomTypeId() throws Exception {

        List<TheItem.NestedItem> nestedList = new ArrayList<TheItem.NestedItem>();
        nestedList.add(new TheItem.NestedItem("foo1"));
        nestedList.add(new TheItem.NestedItem("foo2"));
        TheItem item = new TheItem("first", false, nestedList);
        Outer outer = new Outer();
        outer.getAttributes().put("entry1", item);

        String json = MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(outer);

        Outer result = MAPPER.readValue(json, Outer.class);
        assertNotNull(result);
        assertNotNull(result.attributes);
        assertEquals(1, result.attributes.size());
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

// com.fasterxml.jackson.databind.struct.TestForwardReference::testForwardRef
	public void testForwardRef() throws IOException {
		MAPPER.readValue("{" +
				"  \"@type\" : \"TestForwardReference$ForwardReferenceContainerClass\"," +
				"  \"frc\" : \"willBeForwardReferenced\"," +
				"  \"yac\" : {" +
				"    \"@type\" : \"TestForwardReference$YetAnotherClass\"," +
				"    \"frc\" : {" +
				"      \"@type\" : \"One\"," +
				"      \"id\" : \"willBeForwardReferenced\"" +
				"    }," +
				"    \"id\" : \"anId\"" +
				"  }," +
				"  \"id\" : \"ForwardReferenceContainerClass1\"" +
				"}", ForwardReferenceContainerClass.class);

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

// com.fasterxml.jackson.databind.struct.TestPOJOAsArray::testBeanAsArrayUnwrapped
    public void testBeanAsArrayUnwrapped() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY);
        SingleBean result = mapper.readValue("[\"foobar\"]", SingleBean.class);
        assertNotNull(result);
        assertEquals("foobar", result.name);
    }

// com.fasterxml.jackson.databind.struct.TestPOJOAsArray::testAnnotationOverride
    public void testAnnotationOverride() throws Exception
    {
        
        assertEquals("{\"value\":{\"x\":1,\"y\":2}}", MAPPER.writeValueAsString(new A()));

        
        ObjectMapper mapper2 = new ObjectMapper();
        mapper2.setAnnotationIntrospector(new ForceArraysIntrospector());
        assertEquals("[[1,2]]", mapper2.writeValueAsString(new A()));
    }

// com.fasterxml.jackson.databind.struct.TestPOJOAsArray::testWithMaps
    public void testWithMaps() throws Exception
    {
        AsArrayWithMap input = new AsArrayWithMap(1, 2);
        String json = MAPPER.writeValueAsString(input);
        AsArrayWithMap output = MAPPER.readValue(json, AsArrayWithMap.class);
        assertNotNull(output);
        assertNotNull(output.attrs);
        assertEquals(1, output.attrs.size());
        assertEquals(Integer.valueOf(2), output.attrs.get(1));
    }

// com.fasterxml.jackson.databind.struct.TestPOJOAsArray::testSimpleWithIndex
    public void testSimpleWithIndex() throws Exception
    {
        

        CreatorWithIndex value = MAPPER.readValue(aposToQuotes("[2,1]"),
                CreatorWithIndex.class);
        assertEquals(2, value._a);
        assertEquals(1, value._b);
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

        
        AsArrayWithView output = MAPPER.readerFor(AsArrayWithView.class).withView(ViewB.class)
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

// com.fasterxml.jackson.databind.struct.TestParentChildReferences::testSimpleRefs
    public void testSimpleRefs() throws Exception
    {
        SimpleTreeNode root = new SimpleTreeNode("root");
        SimpleTreeNode child = new SimpleTreeNode("kid");
        root.child = child;
        child.parent = root;
        
        String json = MAPPER.writeValueAsString(root);
        
        SimpleTreeNode resultNode = MAPPER.readValue(json, SimpleTreeNode.class);
        assertEquals("root", resultNode.name);
        SimpleTreeNode resultChild = resultNode.child;
        assertNotNull(resultChild);
        assertEquals("kid", resultChild.name);
        assertSame(resultChild.parent, resultNode);
    }

// com.fasterxml.jackson.databind.struct.TestParentChildReferences::testSimpleRefsWithGetter
    public void testSimpleRefsWithGetter() throws Exception
    {
        SimpleTreeNode2 root = new SimpleTreeNode2("root");
        SimpleTreeNode2 child = new SimpleTreeNode2("kid");
        root.child = child;
        child.parent = root;
        
        String json = MAPPER.writeValueAsString(root);
        
        SimpleTreeNode2 resultNode = MAPPER.readValue(json, SimpleTreeNode2.class);
        assertEquals("root", resultNode.name);
        SimpleTreeNode2 resultChild = resultNode.child;
        assertNotNull(resultChild);
        assertEquals("kid", resultChild.name);
        assertSame(resultChild.parent, resultNode);
    }

// com.fasterxml.jackson.databind.struct.TestParentChildReferences::testFullRefs
    public void testFullRefs() throws Exception
    {
        FullTreeNode root = new FullTreeNode("root");
        FullTreeNode child1 = new FullTreeNode("kid1");
        FullTreeNode child2 = new FullTreeNode("kid2");
        root.firstChild = child1;
        child1.parent = root;
        child1.next = child2;
        child2.prev = child1;
        
        String json = MAPPER.writeValueAsString(root);
        
        FullTreeNode resultNode = MAPPER.readValue(json, FullTreeNode.class);
        assertEquals("root", resultNode.name);
        FullTreeNode resultChild = resultNode.firstChild;
        assertNotNull(resultChild);
        assertEquals("kid1", resultChild.name);
        assertSame(resultChild.parent, resultNode);

        
        assertNull(resultChild.prev);
        FullTreeNode resultChild2 = resultChild.next;
        assertNotNull(resultChild2);
        assertEquals("kid2", resultChild2.name);
        assertSame(resultChild, resultChild2.prev);
        assertNull(resultChild2.next);
    }

// com.fasterxml.jackson.databind.struct.TestParentChildReferences::testArrayOfRefs
    public void testArrayOfRefs() throws Exception
    {
        NodeArray root = new NodeArray();
        ArrayNode node1 = new ArrayNode("a");
        ArrayNode node2 = new ArrayNode("b");
        root.nodes = new ArrayNode[] { node1, node2 };
        String json = MAPPER.writeValueAsString(root);
        
        NodeArray result = MAPPER.readValue(json, NodeArray.class);
        ArrayNode[] kids = result.nodes;
        assertNotNull(kids);
        assertEquals(2, kids.length);
        assertEquals("a", kids[0].name);
        assertEquals("b", kids[1].name);
        assertSame(result, kids[0].parent);
        assertSame(result, kids[1].parent);
    }

// com.fasterxml.jackson.databind.struct.TestParentChildReferences::testListOfRefs
    public void testListOfRefs() throws Exception
    {
        NodeList root = new NodeList();
        NodeForList node1 = new NodeForList("a");
        NodeForList node2 = new NodeForList("b");
        root.nodes = Arrays.asList(node1, node2);
        String json = MAPPER.writeValueAsString(root);
        
        NodeList result = MAPPER.readValue(json, NodeList.class);
        List<NodeForList> kids = result.nodes;
        assertNotNull(kids);
        assertEquals(2, kids.size());
        assertEquals("a", kids.get(0).name);
        assertEquals("b", kids.get(1).name);
        assertSame(result, kids.get(0).parent);
        assertSame(result, kids.get(1).parent);
    }

// com.fasterxml.jackson.databind.struct.TestParentChildReferences::testMapOfRefs
    public void testMapOfRefs() throws Exception
    {
        NodeMap root = new NodeMap();
        NodeForMap node1 = new NodeForMap("a");
        NodeForMap node2 = new NodeForMap("b");
        Map<String,NodeForMap> nodes = new HashMap<String, NodeForMap>();
        nodes.put("a1", node1);
        nodes.put("b2", node2);
        root.nodes = nodes;
        String json = MAPPER.writeValueAsString(root);
        
        NodeMap result = MAPPER.readValue(json, NodeMap.class);
        Map<String,NodeForMap> kids = result.nodes;
        assertNotNull(kids);
        assertEquals(2, kids.size());
        assertNotNull(kids.get("a1"));
        assertNotNull(kids.get("b2"));
        assertEquals("a", kids.get("a1").name);
        assertEquals("b", kids.get("b2").name);
        assertSame(result, kids.get("a1").parent);
        assertSame(result, kids.get("b2").parent);
    }

// com.fasterxml.jackson.databind.struct.TestParentChildReferences::testAbstract368
    public void testAbstract368() throws Exception
    {
        AbstractNode parent = new ConcreteNode("p");
        AbstractNode child = new ConcreteNode("c");
        parent.next = child;
        child.prev = parent;

        
        String json = MAPPER.writeValueAsString(parent);

        AbstractNode root = MAPPER.readValue(json, AbstractNode.class);

        assertEquals(ConcreteNode.class, root.getClass());
        assertEquals("p", root.id);
        assertNull(root.prev);
        AbstractNode leaf = root.next;
        assertNotNull(leaf);
        assertEquals("c", leaf.id);
        assertSame(root, leaf.prev);
    }

// com.fasterxml.jackson.databind.struct.TestParentChildReferences::testIssue693
    public void testIssue693() throws Exception
    {
        Parent parent = new Parent();
        parent.addChild(new Child("foo"));
        parent.addChild(new Child("bar"));
        byte[] bytes = MAPPER.writeValueAsBytes(parent);
        Parent value = MAPPER.readValue(bytes, Parent.class); 
        for (Child child : value.children) {
            assertEquals(value, child.getParent());
        }
    }

// com.fasterxml.jackson.databind.struct.TestParentChildReferences::testIssue708
    public void testIssue708() throws Exception
    {
        Advertisement708 ad = MAPPER.readValue("{\"title\":\"Hroch\",\"photos\":[{\"id\":3}]}", Advertisement708.class);      
        assertNotNull(ad);
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

// com.fasterxml.jackson.databind.struct.TestUnwrapped::testIssue615
    public void testIssue615() throws Exception
    {
        Parent input = new Parent("name");
        String json = MAPPER.writeValueAsString(input);
        Parent output = MAPPER.readValue(json, Parent.class);
        assertEquals("name", output.c1.field);
    }

// com.fasterxml.jackson.databind.struct.TestUnwrapped::testUnwrappedAsPropertyIndicator
    public void testUnwrappedAsPropertyIndicator() throws Exception
    {
        Inner inner = new Inner();
        inner.animal = "Zebra";

        Outer outer = new Outer();
        outer.inner = inner;

        String actual = MAPPER.writeValueAsString(outer);

        assertTrue(actual.contains("animal"));
        assertTrue(actual.contains("Zebra"));
        assertFalse(actual.contains("inner"));
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

// com.fasterxml.jackson.databind.struct.TestUnwrappedWithSameName647::testUnwrappedWithSamePropertyName
    public void testUnwrappedWithSamePropertyName() throws Exception {
        final String JSON = "{'mail': {'mail': 'the mail text'}}";
        UnwrappedWithSamePropertyName result = MAPPER.readValue(aposToQuotes(JSON), UnwrappedWithSamePropertyName.class);
        assertNotNull(result.mail);
        assertNotNull(result.mail.mail);
        assertEquals("the mail text", result.mail.mail.mail);
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

// com.fasterxml.jackson.databind.type.TypeAliasesTest::testAliasResolutionIssue743
    public void testAliasResolutionIssue743() throws Exception
    {
        String s3 = "{\"dataObj\" : [ \"one\", \"two\", \"three\" ] }";
        ObjectMapper m = new ObjectMapper();
   
        Child.ChildData d = m.readValue(s3, Child.ChildData.class);
        assertNotNull(d.dataObj);
        assertEquals(3, d.dataObj.size());
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

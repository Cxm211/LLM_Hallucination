// buggy code
    public Object deserializeSetAndReturn(JsonParser p,
    		DeserializationContext ctxt, Object instance) throws IOException
    {
        Object id = _valueDeserializer.deserialize(p, ctxt);
        /* 02-Apr-2015, tatu: Actually, as per [databind#742], let it be;
         *  missing or null id is needed for some cases, such as cases where id
         *  will be generated externally, at a later point, and is not available
         *  quite yet. Typical use case is with DB inserts.
         */
        // note: no null checks (unlike usually); deserializer should fail if one found
        if (id == null) {
            return null;
        }
        ReadableObjectId roid = ctxt.findObjectId(id, _objectIdReader.generator, _objectIdReader.resolver);
        roid.bindItem(instance);
        // also: may need to set a property value as well
        SettableBeanProperty idProp = _objectIdReader.idProperty;
        if (idProp != null) {
            return idProp.setAndReturn(instance, id);
        }
        return instance;
    }

// relevant test
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

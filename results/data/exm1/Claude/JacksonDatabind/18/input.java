// buggy code
    protected MappingIterator(JavaType type, JsonParser p, DeserializationContext ctxt,
            JsonDeserializer<?> deser,
            boolean managedParser, Object valueToUpdate)
    {
        _type = type;
        _parser = p;
        _context = ctxt;
        _deserializer = (JsonDeserializer<T>) deser;
        _closeParser = managedParser;
        if (valueToUpdate == null) {
            _updatedValue = null;
        } else {
            _updatedValue = (T) valueToUpdate;
        }

        /* Ok: one more thing; we may have to skip START_ARRAY, assuming
         * "wrapped" sequence; but this is ONLY done for 'managed' parsers
         * and never if JsonParser was directly passed by caller (if it
         * was, caller must have either positioned it over first token of
         * the first element, or cleared the START_ARRAY token explicitly).
         * Note, however, that we do not try to guess whether this could be
         * an unwrapped sequence of arrays/Lists: we just assume it is wrapped;
         * and if not, caller needs to hand us JsonParser instead, pointing to
         * the first token of the first element.
         */
        if (managedParser && (p != null) && p.isExpectedStartArrayToken()) {
                // If pointing to START_ARRAY, context should be that ARRAY
                p.clearCurrentToken();
                // regardless, recovery context should be whatever context we have now,
                // with sole exception of pointing to a start marker, in which case it's
                // the parent
        }
    }

    public void close() throws IOException {
            if (_parser != null) {
                _parser.close();
            }
    }

    public boolean hasNextValue() throws IOException
    {
        if (_parser == null) {
            return false;
            // fall-through
        }
        if (!_hasNextChecked) {
            JsonToken t = _parser.getCurrentToken();
            _hasNextChecked = true;
            if (t == null) { // un-initialized or cleared; find next
                t = _parser.nextToken();
                // If EOF, no more, or if we hit END_ARRAY (although we don't clear the token).
                if (t == null || t == JsonToken.END_ARRAY) {
                    JsonParser jp = _parser;
                    _parser = null;
                    if (_closeParser) {
                        jp.close();
                    }
                    return false;
                }
            }
            // fall through
        }
        return true;
    }

    public T nextValue() throws IOException
    {
        if (!_hasNextChecked) {
            if (!hasNextValue()) {
                return _throwNoSuchElement();
            }
        }
        if (_parser == null) {
            return _throwNoSuchElement();
        }
        _hasNextChecked = false;

        try {
            T value;
            if (_updatedValue == null) {
                value = _deserializer.deserialize(_parser, _context);
            } else{
                _deserializer.deserialize(_parser, _context, _updatedValue);
                value = _updatedValue;
            }
            return value;
        } finally {
            /* 24-Mar-2015, tatu: As per [#733], need to mark token consumed no
             *   matter what, to avoid infinite loop for certain failure cases.
             *   For 2.6 need to improve further.
             */
            _parser.clearCurrentToken();
        }
    }

    public JsonLocation getCurrentLocation() {
        return _parser.getCurrentLocation();
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
        final String JSON = "{\"a\":3}{\"a\":27}  ";

        MappingIterator<Bean> it = MAPPER.readerFor(Bean.class).readValues(JSON);

        assertNotNull(it.getCurrentLocation());
        assertTrue(it.hasNext());
        Bean b = it.next();
        assertEquals(3, b.a);
        assertTrue(it.hasNext());
        b = it.next();
        assertEquals(27, b.a);
        assertFalse(it.hasNext());
        it.close();

        
        it = MAPPER.readerFor(Bean.class).readValues(JSON);
        List<Bean> all = it.readAll();
        assertEquals(2, all.size());
        it.close();

        it = MAPPER.readerFor(Bean.class).readValues("{\"a\":3}{\"a\":3}");
        Set<Bean> set = it.readAll(new HashSet<Bean>());
        assertEquals(HashSet.class, set.getClass());
        assertEquals(1, set.size());
        assertEquals(3, set.iterator().next().a);
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
        JsonParser jp = MAPPER.getFactory().createParser(JSON);
        assertToken(JsonToken.START_ARRAY, jp.nextToken());
        
        
        
        
        assertToken(JsonToken.START_ARRAY, jp.nextToken());
        
        Iterator<int[]> it = MAPPER.readValues(jp, int[].class);

        assertTrue(it.hasNext());
        int[] array = it.next();
        assertEquals(1, array.length);
        assertEquals(1, array[0]);
        assertTrue(it.hasNext());
        array = it.next();
        assertEquals(1, array.length);
        assertEquals(3, array[0]);
        assertFalse(it.hasNext());
        jp.close();
    }

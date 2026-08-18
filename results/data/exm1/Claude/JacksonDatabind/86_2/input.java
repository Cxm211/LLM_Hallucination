// buggy code
    public void setReference(JavaType ref)
    {
        // sanity check; should not be called multiple times
        if (_referencedType != null) {
            throw new IllegalStateException("Trying to re-set self reference; old value = "+_referencedType+", new = "+ref);
        }
        _referencedType = ref;
    }

// relevant test
// com.fasterxml.jackson.databind.deser.TestUntypedDeserialization::testNestedUntyped989
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

// com.fasterxml.jackson.databind.deser.TestUntypedDeserialization::testUntypedWithJsonArrays
    public void testUntypedWithJsonArrays() throws Exception
    {
        
        Object ob = MAPPER.readValue("[1]", Object.class);
        assertTrue(ob instanceof List<?>);

        
        MAPPER.configure(DeserializationFeature.USE_JAVA_ARRAY_FOR_JSON_ARRAY, true);
        ob = MAPPER.readValue("[1]", Object.class);
        assertEquals(Object[].class, ob.getClass());
    }

// com.fasterxml.jackson.databind.deser.TestUntypedDeserialization::testUntypedIntAsLong
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

// com.fasterxml.jackson.databind.deser.TestValueAnnotations::testOverrideClassValid
    public void testOverrideClassValid() throws Exception
    {
        ObjectMapper m = new ObjectMapper();
        CollectionHolder result = m.readValue
            ("{ \"strings\" : [ \"test\" ] }", CollectionHolder.class);

        Collection<String> strs = result._strings;
        assertEquals(1, strs.size());
        assertEquals(TreeSet.class, strs.getClass());
        assertEquals("test", strs.iterator().next());
    }

// com.fasterxml.jackson.databind.deser.TestValueAnnotations::testOverrideMapValid
    public void testOverrideMapValid() throws Exception
    {
        ObjectMapper m = new ObjectMapper();
        
        MapHolder result = m.readValue
            ("{ \"strings\" :  { \"a\" : 3 } }", MapHolder.class);

        Map<String,String> strs = result._data;
        assertEquals(1, strs.size());
        assertEquals(TreeMap.class, strs.getClass());
        String value = strs.get("a");
        assertEquals("3", value);
    }

// com.fasterxml.jackson.databind.deser.TestValueAnnotations::testOverrideArrayClass
    public void testOverrideArrayClass() throws Exception
    {
        ObjectMapper m = new ObjectMapper();
        ArrayHolder result = m.readValue
            ("{ \"strings\" : [ \"test\" ] }", ArrayHolder.class);

        String[] strs = result._strings;
        assertEquals(1, strs.length);
        assertEquals(String[].class, strs.getClass());
        assertEquals("test", strs[0]);
    }

// com.fasterxml.jackson.databind.deser.TestValueAnnotations::testOverrideClassInvalid
    public void testOverrideClassInvalid() throws Exception
    {
        
        try {
            BrokenCollectionHolder result = new ObjectMapper().readValue
                ("{ \"strings\" : [ ] }", BrokenCollectionHolder.class);
            fail("Expected a failure, but got results: "+result);
        } catch (JsonMappingException jme) {
            verifyException(jme, "not subtype of");
        }
    }

// com.fasterxml.jackson.databind.deser.TestValueAnnotations::testRootInterfaceAs
    public void testRootInterfaceAs() throws Exception
    {
        RootInterface value = new ObjectMapper().readValue("{\"a\":\"abc\" }", RootInterface.class);
        assertTrue(value instanceof RootInterfaceImpl);
        assertEquals("abc", value.getA());
    }

// com.fasterxml.jackson.databind.deser.TestValueAnnotations::testRootInterfaceUsing
    public void testRootInterfaceUsing() throws Exception
    {
        RootString value = new ObjectMapper().readValue("\"xxx\"", RootString.class);
        assertTrue(value instanceof RootString);
        assertEquals("xxx", value.contents());
    }

// com.fasterxml.jackson.databind.deser.TestValueAnnotations::testRootListAs
    public void testRootListAs() throws Exception
    {
        RootMap value = new ObjectMapper().readValue("{\"a\":\"b\"}", RootMap.class);
        assertEquals(1, value.size());
        Object v2 = value.get("a");
        assertEquals(RootStringImpl.class, v2.getClass());
        assertEquals("b", ((RootString) v2).contents());
    }

// com.fasterxml.jackson.databind.deser.TestValueAnnotations::testRootMapAs
    public void testRootMapAs() throws Exception
    {
        RootList value = new ObjectMapper().readValue("[ \"c\" ]", RootList.class);
        assertEquals(1, value.size());
        Object v2 = value.get(0);
        assertEquals(RootStringImpl.class, v2.getClass());
        assertEquals("c", ((RootString) v2).contents());
    }

// com.fasterxml.jackson.databind.deser.TestValueAnnotations::testOverrideKeyClassValid
	public void testOverrideKeyClassValid() throws Exception
    {
        ObjectMapper m = new ObjectMapper();
        MapKeyHolder result = m.readValue("{ \"map\" : { \"xxx\" : \"yyy\" } }", MapKeyHolder.class);
        Map<StringWrapper, String> map = (Map<StringWrapper,String>)(Map<?,?>)result._map;
        assertEquals(1, map.size());
        Map.Entry<StringWrapper, String> en = map.entrySet().iterator().next();

        StringWrapper key = en.getKey();
        assertEquals(StringWrapper.class, key.getClass());
        assertEquals("xxx", key._string);
        assertEquals("yyy", en.getValue());
    }

// com.fasterxml.jackson.databind.deser.TestValueAnnotations::testOverrideKeyClassInvalid
    public void testOverrideKeyClassInvalid() throws Exception
    {
        
        try {
            BrokenMapKeyHolder result = new ObjectMapper().readValue
                ("{ \"123\" : \"xxx\" }", BrokenMapKeyHolder.class);
            fail("Expected a failure, but got results: "+result);
        } catch (JsonMappingException jme) {
            verifyException(jme, "not subtype of");
        }
    }

// com.fasterxml.jackson.databind.deser.TestValueAnnotations::testOverrideContentClassValid
	public void testOverrideContentClassValid() throws Exception
    {
        ObjectMapper m = new ObjectMapper();
        ListContentHolder result = m.readValue("{ \"list\" : [ \"abc\" ] }", ListContentHolder.class);
        List<StringWrapper> list = (List<StringWrapper>)result._list;
        assertEquals(1, list.size());
        Object value = list.get(0);
        assertEquals(StringWrapper.class, value.getClass());
        assertEquals("abc", ((StringWrapper) value)._string);
    }

// com.fasterxml.jackson.databind.deser.TestValueAnnotations::testOverrideArrayContents
    public void testOverrideArrayContents() throws Exception
    {
        ObjectMapper m = new ObjectMapper();
        ArrayContentHolder result = m.readValue("{ \"data\" : [ 1, 2, 3 ] }",
                                                ArrayContentHolder.class);
        Object[] data = result._data;
        assertEquals(3, data.length);
        assertEquals(Long[].class, data.getClass());
        assertEquals(1L, data[0]);
        assertEquals(2L, data[1]);
        assertEquals(3L, data[2]);
    }

// com.fasterxml.jackson.databind.deser.TestValueAnnotations::testOverrideMapContents
    public void testOverrideMapContents() throws Exception
    {
        ObjectMapper m = new ObjectMapper();
        MapContentHolder result = m.readValue("{ \"map\" : { \"a\" : 9 } }",
                                                MapContentHolder.class);
        Map<Object,Object> map = result._map;
        assertEquals(1, map.size());
        Object ob = map.values().iterator().next();
        assertEquals(Integer.class, ob.getClass());
        assertEquals(Integer.valueOf(9), ob);
    }

// com.fasterxml.jackson.databind.deser.builder.BuilderWithUnwrappedTest::testWithUnwrappedAndCreatorSingleParameterAtBeginning
    public void testWithUnwrappedAndCreatorSingleParameterAtBeginning() throws Exception {
        final String json = aposToQuotes("{'person_id':1234,'first_name':'John','last_name':'Doe','years_old':30,'living':true}");

        final ObjectMapper mapper = new ObjectMapper();
        Person person = mapper.readValue(json, Person.class);
        assertEquals(1234, person.getId());
        assertNotNull(person.getName());
        assertEquals("John", person.getName().getFirst());
        assertEquals("Doe", person.getName().getLast());
        assertEquals(30, person.getAge());
        assertEquals(true, person.isAlive());
    }

// com.fasterxml.jackson.databind.deser.builder.BuilderWithUnwrappedTest::testWithUnwrappedAndCreatorSingleParameterInMiddle
    public void testWithUnwrappedAndCreatorSingleParameterInMiddle() throws Exception {
        final String json = aposToQuotes("{'first_name':'John','last_name':'Doe','person_id':1234,'years_old':30,'living':true}");

        final ObjectMapper mapper = new ObjectMapper();
        Person person = mapper.readValue(json, Person.class);
        assertEquals(1234, person.getId());
        assertNotNull(person.getName());
        assertEquals("John", person.getName().getFirst());
        assertEquals("Doe", person.getName().getLast());
        assertEquals(30, person.getAge());
        assertEquals(true, person.isAlive());
    }

// com.fasterxml.jackson.databind.deser.builder.BuilderWithUnwrappedTest::testWithUnwrappedAndCreatorSingleParameterAtEnd
    public void testWithUnwrappedAndCreatorSingleParameterAtEnd() throws Exception {
        final String json = aposToQuotes("{'first_name':'John','last_name':'Doe','years_old':30,'living':true,'person_id':1234}");

        final ObjectMapper mapper = new ObjectMapper();
        Person person = mapper.readValue(json, Person.class);
        assertEquals(1234, person.getId());
        assertNotNull(person.getName());
        assertEquals("John", person.getName().getFirst());
        assertEquals("Doe", person.getName().getLast());
        assertEquals(30, person.getAge());
        assertEquals(true, person.isAlive());
    }

// com.fasterxml.jackson.databind.deser.builder.BuilderWithUnwrappedTest::testWithUnwrappedAndCreatorMultipleParametersAtBeginning
    public void testWithUnwrappedAndCreatorMultipleParametersAtBeginning() throws Exception {
        final String json = aposToQuotes("{'animal_id':1234,'living':true,'first_name':'John','last_name':'Doe','years_old':30}");

        final ObjectMapper mapper = new ObjectMapper();
        Animal animal = mapper.readValue(json, Animal.class);
        assertEquals(1234, animal.getId());
        assertNotNull(animal.getName());
        assertEquals("John", animal.getName().getFirst());
        assertEquals("Doe", animal.getName().getLast());
        assertEquals(30, animal.getAge());
        assertEquals(true, animal.isAlive());
    }

// com.fasterxml.jackson.databind.deser.builder.BuilderWithUnwrappedTest::testWithUnwrappedAndCreatorMultipleParametersInMiddle
    public void testWithUnwrappedAndCreatorMultipleParametersInMiddle() throws Exception {
        final String json = aposToQuotes("{'first_name':'John','animal_id':1234,'last_name':'Doe','living':true,'years_old':30}");

        final ObjectMapper mapper = new ObjectMapper();
        Animal animal = mapper.readValue(json, Animal.class);
        assertEquals(1234, animal.getId());
        assertNotNull(animal.getName());
        assertEquals("John", animal.getName().getFirst());
        assertEquals("Doe", animal.getName().getLast());
        assertEquals(30, animal.getAge());
        assertEquals(true, animal.isAlive());
    }

// com.fasterxml.jackson.databind.deser.builder.BuilderWithUnwrappedTest::testWithUnwrappedAndCreatorMultipleParametersAtEnd
    public void testWithUnwrappedAndCreatorMultipleParametersAtEnd() throws Exception {
        final String json = aposToQuotes("{'first_name':'John','last_name':'Doe','years_old':30,'living':true,'animal_id':1234}");

        final ObjectMapper mapper = new ObjectMapper();
        Animal animal = mapper.readValue(json, Animal.class);
        assertEquals(1234, animal.getId());
        assertNotNull(animal.getName());
        assertEquals("John", animal.getName().getFirst());
        assertEquals("Doe", animal.getName().getLast());
        assertEquals(30, animal.getAge());
        assertEquals(true, animal.isAlive());
    }

// com.fasterxml.jackson.databind.deser.exc.ExceptionPathTest::testReferenceChainForInnerClass
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

// com.fasterxml.jackson.databind.deser.exc.TestExceptionDeserialization::testIOException
    public void testIOException() throws IOException
    {
        IOException ioe = new IOException("TEST");
        String json = MAPPER.writeValueAsString(ioe);
        IOException result = MAPPER.readValue(json, IOException.class);
        assertEquals(ioe.getMessage(), result.getMessage());
    }

// com.fasterxml.jackson.databind.deser.exc.TestExceptionDeserialization::testWithCreator
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

// com.fasterxml.jackson.databind.deser.exc.TestExceptionDeserialization::testWithNullMessage
    public void testWithNullMessage() throws IOException
    {
        final ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        String json = mapper.writeValueAsString(new IOException((String) null));
        IOException result = mapper.readValue(json, IOException.class);
        assertNotNull(result);
        assertNull(result.getMessage());
    }

// com.fasterxml.jackson.databind.deser.exc.TestExceptionDeserialization::testNoArgsException
    public void testNoArgsException() throws IOException
    {
        MyNoArgException exc = MAPPER.readValue("{}", MyNoArgException.class);
        assertNotNull(exc);
    }

// com.fasterxml.jackson.databind.deser.exc.TestExceptionDeserialization::testJDK7SuppressionProperty
    public void testJDK7SuppressionProperty() throws IOException
    {
        Exception exc = MAPPER.readValue("{\"suppressed\":[]}", IOException.class);
        assertNotNull(exc);
    }

// com.fasterxml.jackson.databind.deser.exc.TestExceptionDeserialization::testSingleValueArrayDeserialization
    public void testSingleValueArrayDeserialization() {}

// com.fasterxml.jackson.databind.deser.exc.TestExceptionDeserialization::testSingleValueArrayDeserializationException
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

// com.fasterxml.jackson.databind.deser.exc.TestExceptionHandling::testHandlingOfUnrecognized
    public void testHandlingOfUnrecognized() throws Exception
    {
        UnrecognizedPropertyException exc = null;
        try {
            new ObjectMapper().readValue("{\"bar\":3}", Bean.class);
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

// com.fasterxml.jackson.databind.deser.exc.TestExceptionHandling::testExceptionWithEmpty
    public void testExceptionWithEmpty() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        try {
            Object result = mapper.readValue("    ", Object.class);
            fail("Expected an exception, but got result value: "+result);
        } catch (Exception e) {
            verifyException(e, JsonMappingException.class, "No content");
        }
    }

// com.fasterxml.jackson.databind.deser.exc.TestExceptionHandling::testExceptionWithIncomplete
    public void testExceptionWithIncomplete()
        throws Exception
    {
        BrokenStringReader r = new BrokenStringReader("[ 1, ", "TEST");
        JsonFactory f = new JsonFactory();
        JsonParser jp = f.createParser(r);
        ObjectMapper mapper = new ObjectMapper();
        try {
            @SuppressWarnings("unused")
            Object ob = mapper.readValue(jp, Object.class);
            fail("Should have gotten an exception");
        } catch (IOException e) {
            
            verifyException(e, IOException.class, "TEST");
        }
    }

// com.fasterxml.jackson.databind.deser.exc.TestExceptionHandling::testExceptionWithEOF
    public void testExceptionWithEOF()
        throws Exception
    {
        StringReader r = new StringReader("  3");
        JsonFactory f = new JsonFactory();
        JsonParser jp = f.createParser(r);
        ObjectMapper mapper = new ObjectMapper();

        Integer I = mapper.readValue(jp, Integer.class);
        assertEquals(3, I.intValue());

        
        try {
            I = mapper.readValue(jp, Integer.class);
            fail("Should have gotten an exception");
        } catch (IOException e) {
            verifyException(e, JsonMappingException.class, "No content");
        }
        
        JsonToken t = jp.getCurrentToken();
        if (t != null) {
            fail("Expected current token to be null after end-of-stream, was: "+t);
        }
        jp.close();
    }

// com.fasterxml.jackson.databind.deser.exc.TestExceptionHandlingWithDefaultDeserialization::testShouldThrowJsonMappingExceptionWithPathReference
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

// com.fasterxml.jackson.databind.deser.exc.TestExceptionHandlingWithJsonCreatorDeserialization::testShouldThrowJsonMappingExceptionWithPathReference
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

// com.fasterxml.jackson.databind.deser.exc.TestExceptionSerialization::testSimple
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

// com.fasterxml.jackson.databind.deser.exc.TestExceptionSerialization::testIgnorals
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

        
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        ExceptionWithIgnoral output = mapper.readValue(json2, ExceptionWithIgnoral.class);
        assertNotNull(output);
        assertEquals("foobar", output.getMessage());
    }

// com.fasterxml.jackson.databind.deser.exc.TestExceptionSerialization::testJsonMappingExceptionSerialization
    public void testJsonMappingExceptionSerialization() throws IOException {
        Exception e = null;
        
        try {
            MAPPER.readValue( "{ \"val\": \"foo\" }", NoSerdeConstructor.class );
            fail("Should not pass");
        } catch (JsonMappingException e0) {
            verifyException(e0, "no suitable constructor");
            e = e0;
        }
        
        String json = MAPPER.writeValueAsString(e);
        JsonNode root = MAPPER.readTree(json);
        String msg = root.path("message").asText();
        String MATCH = "no suitable constructor";
        if (!msg.contains(MATCH)) {
            fail("Exception should contain '"+MATCH+"', does not: '"+msg+"'");
        }
    }

// com.fasterxml.jackson.databind.deser.exc.TestExceptionsDuringWriting::testCatchAndRethrow
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

// com.fasterxml.jackson.databind.deser.exc.TestExceptionsDuringWriting::testExceptionWithSimpleMapper
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

// com.fasterxml.jackson.databind.deser.exc.TestExceptionsDuringWriting::testExceptionWithMapperAndGenerator
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

// com.fasterxml.jackson.databind.deser.exc.TestExceptionsDuringWriting::testExceptionWithGeneratorMapping
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
    public void testQNameSer() {}

// com.fasterxml.jackson.databind.ext.TestCoreXMLTypes::testDurationSer
    public void testDurationSer() {}

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

// com.fasterxml.jackson.databind.filter.IgnoreCreatorProp1317Test::testThatJsonIgnoreWorksWithConstructorProperties
    public void testThatJsonIgnoreWorksWithConstructorProperties() throws Exception {
        Testing testing = new Testing("shouldBeIgnored", "notIgnore");
        ObjectMapper om = new ObjectMapper();
        String json = om.writeValueAsString(testing);
        System.out.println(json);
        assertFalse(json.contains("shouldBeIgnored"));
    }

// com.fasterxml.jackson.databind.filter.IgnorePropertiesDeser1575Test::testIgnorePropDeser1575
    public void testIgnorePropDeser1575() throws Exception
    {
        String st = aposToQuotes("{ 'name': 'admin',\n"

              + "    'person_z': { 'name': 'admin' }"
                + "}");

        ObjectMapper mapper = new ObjectMapper();
        Person result = mapper.readValue(st, Person.class);
        assertEquals("admin", result.name);
    }

// com.fasterxml.jackson.databind.filter.IgnorePropertyOnDeserTest::testIgnoreOnProperty1217
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

// com.fasterxml.jackson.databind.filter.IgnorePropertyOnDeserTest::testIgnoreViaConfigOverride1217
    public void testIgnoreViaConfigOverride1217() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configOverride(Point.class)
            .setIgnorals(JsonIgnoreProperties.Value.forIgnoredProperties("y"));
        Point p = mapper.readValue(aposToQuotes("{'x':1,'y':2}"), Point.class);
        
        assertEquals(1, p.x);
        assertEquals(0, p.y);
    }

// com.fasterxml.jackson.databind.filter.IgnorePropertyOnDeserTest::testIgnoreGetterNotSetter1595
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

// com.fasterxml.jackson.databind.filter.IgnorePropsForSerTest::testExplicitIgnoralWithBean
    public void testExplicitIgnoralWithBean() throws Exception
    {
        IgnoreSome value = new IgnoreSome();
        Map<String,Object> result = writeAndMap(MAPPER, value);
        assertEquals(2, result.size());
        
        assertFalse(result.containsKey("b"));
        assertFalse(result.containsKey("c"));
        
        assertEquals(Integer.valueOf(value.a), result.get("a"));
        assertEquals(value.getD(), result.get("d"));
    }

// com.fasterxml.jackson.databind.filter.IgnorePropsForSerTest::testExplicitIgnoralWithMap
    public void testExplicitIgnoralWithMap() throws Exception
    {
        
        MyMap value = new MyMap();
        value.put("a", "b");
        value.put("@class", MyMap.class.getName());
        Map<String,Object> result = writeAndMap(MAPPER, value);
        assertEquals(1, result.size());
        
        assertFalse(result.containsKey("@class"));
        
        assertEquals(value.get("a"), result.get("a"));
    }

// com.fasterxml.jackson.databind.filter.IgnorePropsForSerTest::testIgnoreViaOnlyProps
    public void testIgnoreViaOnlyProps() throws Exception
    {
        assertEquals("{\"value\":{\"x\":1}}",
                MAPPER.writeValueAsString(new WrapperWithPropIgnore()));
    }

// com.fasterxml.jackson.databind.filter.IgnorePropsForSerTest::testIgnoreViaPropForUntyped
    public void testIgnoreViaPropForUntyped() throws Exception
    {
        assertEquals("{\"value\":{\"z\":3}}",
                MAPPER.writeValueAsString(new WrapperWithPropIgnoreUntyped()));
    }

// com.fasterxml.jackson.databind.filter.IgnorePropsForSerTest::testIgnoreWithMapProperty
    public void testIgnoreWithMapProperty() throws Exception
    {
        assertEquals("{\"value\":{\"b\":2}}", MAPPER.writeValueAsString(new MapWrapper()));
    }

// com.fasterxml.jackson.databind.filter.IgnorePropsForSerTest::testIgnoreViaPropsAndClass
    public void testIgnoreViaPropsAndClass() throws Exception
    {
        assertEquals("{\"value\":{\"y\":2}}",
                MAPPER.writeValueAsString(new WrapperWithPropIgnore2()));
    }

// com.fasterxml.jackson.databind.filter.IgnorePropsForSerTest::testIgnoreViaConfigOverride
    public void testIgnoreViaConfigOverride() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configOverride(Point.class)
            .setIgnorals(JsonIgnoreProperties.Value.forIgnoredProperties("x"));
        assertEquals("{\"y\":3}", mapper.writeValueAsString(new Point(2, 3)));
    }

// com.fasterxml.jackson.databind.filter.JsonInclude1327Test::testClassDefaultsForEmpty
    public void testClassDefaultsForEmpty() throws Exception {
        ObjectMapper om = new ObjectMapper();
        om.setSerializationInclusion(JsonInclude.Include.NON_NULL);

        final String jsonString = om.writeValueAsString(new Issue1327BeanEmpty());

        if (jsonString.contains("myList")) {
            fail("Should not contain `myList`: "+jsonString);
        }
    }

// com.fasterxml.jackson.databind.filter.JsonInclude1327Test::testClassDefaultsForAlways
    public void testClassDefaultsForAlways() throws Exception {
        ObjectMapper om = new ObjectMapper();
        om.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);

        final String jsonString = om.writeValueAsString(new Issue1327BeanAlways());

        if (!jsonString.contains("myList")) {
            fail("Should contain `myList` with Include.ALWAYS: "+jsonString);
        }
    }

// com.fasterxml.jackson.databind.filter.JsonIncludeTest::testGlobal
    public void testGlobal() throws IOException
    {
        Map<String,Object> result = writeAndMap(MAPPER, new SimpleBean());
        assertEquals(2, result.size());
        assertEquals("a", result.get("a"));
        assertNull(result.get("b"));
        assertTrue(result.containsKey("b"));
    }

// com.fasterxml.jackson.databind.filter.JsonIncludeTest::testNonNullByClass
    public void testNonNullByClass() throws IOException
    {
        Map<String,Object> result = writeAndMap(MAPPER, new NoNullsBean());
        assertEquals(1, result.size());
        assertFalse(result.containsKey("a"));
        assertNull(result.get("a"));
        assertTrue(result.containsKey("b"));
        assertNull(result.get("b"));
    }

// com.fasterxml.jackson.databind.filter.JsonIncludeTest::testNonDefaultByClass
    public void testNonDefaultByClass() throws IOException
    {
        NonDefaultBean bean = new NonDefaultBean();
        
        bean._a = "notA";
        Map<String,Object> result = writeAndMap(MAPPER, bean);
        assertEquals(1, result.size());
        assertTrue(result.containsKey("a"));
        assertEquals("notA", result.get("a"));
        assertFalse(result.containsKey("b"));
        assertNull(result.get("b"));
    }

// com.fasterxml.jackson.databind.filter.JsonIncludeTest::testNonDefaultByClassNoCtor
    public void testNonDefaultByClassNoCtor() throws IOException
    {
        NonDefaultBeanXYZ bean = new NonDefaultBeanXYZ(1, 2, 0);
        String json = MAPPER.writeValueAsString(bean);
        assertEquals(aposToQuotes("{'x':1,'y':2}"), json);
    }

// com.fasterxml.jackson.databind.filter.JsonIncludeTest::testMixedMethod
    public void testMixedMethod() throws IOException
    {
        MixedBean bean = new MixedBean();
        bean._a = "xyz";
        bean._b = null;
        Map<String,Object> result = writeAndMap(MAPPER, bean);
        assertEquals(1, result.size());
        assertEquals("xyz", result.get("a"));
        assertFalse(result.containsKey("b"));

        bean._a = "a";
        bean._b = "b";
        result = writeAndMap(MAPPER, bean);
        assertEquals(1, result.size());
        assertEquals("b", result.get("b"));
        assertFalse(result.containsKey("a"));
    }

// com.fasterxml.jackson.databind.filter.JsonIncludeTest::testDefaultForEmptyList
    public void testDefaultForEmptyList() throws IOException
    {
        assertEquals("{}", MAPPER.writeValueAsString(new ListBean()));
    }

// com.fasterxml.jackson.databind.filter.JsonIncludeTest::testNonEmptyDefaultArray
    public void testNonEmptyDefaultArray() throws IOException
    {
        assertEquals("{}", MAPPER.writeValueAsString(new ArrayBean()));
    }

// com.fasterxml.jackson.databind.filter.JsonIncludeTest::testDefaultForIntegers
    public void testDefaultForIntegers() throws IOException
    {
        assertEquals("{}", MAPPER.writeValueAsString(new DefaultIntBean(0, Integer.valueOf(0))));
        assertEquals("{\"i2\":1}", MAPPER.writeValueAsString(new DefaultIntBean(0, Integer.valueOf(1))));
        assertEquals("{\"i1\":3}", MAPPER.writeValueAsString(new DefaultIntBean(3, Integer.valueOf(0))));
    }

// com.fasterxml.jackson.databind.filter.JsonIncludeTest::testEmptyInclusionScalars
    public void testEmptyInclusionScalars() throws IOException
    {
        ObjectMapper defMapper = MAPPER;
        ObjectMapper inclMapper = new ObjectMapper().setSerializationInclusion(JsonInclude.Include.NON_EMPTY);

        
        StringWrapper str = new StringWrapper("");
        assertEquals("{\"str\":\"\"}", defMapper.writeValueAsString(str));
        assertEquals("{}", inclMapper.writeValueAsString(str));
        assertEquals("{}", inclMapper.writeValueAsString(new StringWrapper()));

        assertEquals("{\"value\":\"x\"}", defMapper.writeValueAsString(new NonEmptyString("x")));
        assertEquals("{}", defMapper.writeValueAsString(new NonEmptyString("")));

        
        
        
        assertEquals("{\"value\":12}", defMapper.writeValueAsString(new NonEmptyInt(12)));
        assertEquals("{\"value\":0}", defMapper.writeValueAsString(new NonEmptyInt(0)));

        assertEquals("{\"value\":1.25}", defMapper.writeValueAsString(new NonEmptyDouble(1.25)));
        assertEquals("{\"value\":0.0}", defMapper.writeValueAsString(new NonEmptyDouble(0.0)));

        
        IntWrapper zero = new IntWrapper(0);
        assertEquals("{\"i\":0}", defMapper.writeValueAsString(zero));
        assertEquals("{\"i\":0}", inclMapper.writeValueAsString(zero));
    }

// com.fasterxml.jackson.databind.filter.JsonIncludeTest::testPropConfigOverridesForInclude
    public void testPropConfigOverridesForInclude() throws IOException
    {
        
        EmptyListMapBean empty = new EmptyListMapBean();
        assertEquals(aposToQuotes("{'list':[],'map':{}}"),
                MAPPER.writeValueAsString(empty));
        ObjectMapper mapper;

        
        mapper = new ObjectMapper();
        mapper.configOverride(Map.class)
            .setInclude(JsonInclude.Value.construct(JsonInclude.Include.NON_EMPTY, null));
        assertEquals(aposToQuotes("{'list':[]}"),
                mapper.writeValueAsString(empty));

        mapper = new ObjectMapper();
        mapper.configOverride(List.class)
            .setInclude(JsonInclude.Value.construct(JsonInclude.Include.NON_EMPTY, null));
        assertEquals(aposToQuotes("{'map':{}}"),
                mapper.writeValueAsString(empty));
    }

// com.fasterxml.jackson.databind.filter.JsonIncludeTest::testIssue1351
    public void testIssue1351() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setSerializationInclusion(JsonInclude.Include.NON_DEFAULT);
        assertEquals(aposToQuotes("{}"),
                mapper.writeValueAsString(new Issue1351Bean(null, (double) 0)));
        
        assertEquals(aposToQuotes("{}"),
                mapper.writeValueAsString(new Issue1351NonBean(0)));
    }

// com.fasterxml.jackson.databind.filter.MapInclusionTest::testNonNullValueMapViaProp
    public void testNonNullValueMapViaProp() throws IOException
    {
        String json = MAPPER.writeValueAsString(new NoEmptiesMapContainer()
            .add("a", null)
            .add("b", ""));
        assertEquals(aposToQuotes("{}"), json);
    }

// com.fasterxml.jackson.databind.filter.NullSerializationTest::testSimple
    public void testSimple() throws Exception
    {
        assertEquals("null", MAPPER.writeValueAsString(null));
    }

// com.fasterxml.jackson.databind.filter.NullSerializationTest::testOverriddenDefaultNulls
    public void testOverriddenDefaultNulls() throws Exception
    {
        DefaultSerializerProvider sp = new DefaultSerializerProvider.Impl();
        sp.setNullValueSerializer(new NullSerializer());
        ObjectMapper m = new ObjectMapper();
        m.setSerializerProvider(sp);
        assertEquals("\"foobar\"", m.writeValueAsString(null));
    }

// com.fasterxml.jackson.databind.filter.NullSerializationTest::testCustomNulls
    public void testCustomNulls() throws Exception
    {
        ObjectMapper m = new ObjectMapper();
        m.setSerializerProvider(new MyNullProvider());
        assertEquals("{\"name\":\"foobar\"}", m.writeValueAsString(new Bean1()));
        assertEquals("{\"type\":null}", m.writeValueAsString(new Bean2()));
    }

// com.fasterxml.jackson.databind.filter.NullSerializationTest::testCustomNullForTrees
    public void testCustomNullForTrees() throws Exception
    {
        ObjectNode root = MAPPER.createObjectNode();
        root.putNull("a");

        
        assertEquals("{\"a\":null}", MAPPER.writeValueAsString(root));

        
        DefaultSerializerProvider prov = new MyNullProvider();
        prov.setNullValueSerializer(new NullSerializer());
        ObjectMapper m = new ObjectMapper();
        m.setSerializerProvider(prov);
        assertEquals("{\"a\":\"foobar\"}", m.writeValueAsString(root));
    }

// com.fasterxml.jackson.databind.filter.NullSerializationTest::testNullSerializerForProperty
    public void testNullSerializerForProperty() throws Exception
    {
        assertEquals("{\"a\":\"foobar\"}", MAPPER.writeValueAsString(new BeanWithNullProps()));
    }

// com.fasterxml.jackson.databind.filter.ProblemHandlerTest::testWeirdKeyHandling
    public void testWeirdKeyHandling() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper()
            .addHandler(new WeirdKeyHandler(7));
        IntKeyMapWrapper w = mapper.readValue("{\"stuff\":{\"foo\":\"abc\"}}",
                IntKeyMapWrapper.class);
        Map<Integer,String> map = w.stuff;
        assertEquals(1, map.size());
        assertEquals("abc", map.values().iterator().next());
        assertEquals(Integer.valueOf(7), map.keySet().iterator().next());
    }

// com.fasterxml.jackson.databind.filter.ProblemHandlerTest::testWeirdNumberHandling
    public void testWeirdNumberHandling() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper()
            .addHandler(new WeirdNumberHandler(SingleValuedEnum.A))
            ;
        SingleValuedEnum result = mapper.readValue("3", SingleValuedEnum.class);
        assertEquals(SingleValuedEnum.A, result);
    }

// com.fasterxml.jackson.databind.filter.ProblemHandlerTest::testWeirdStringHandling
    public void testWeirdStringHandling() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper()
            .addHandler(new WeirdStringHandler(SingleValuedEnum.A))
            ;
        SingleValuedEnum result = mapper.readValue("\"B\"", SingleValuedEnum.class);
        assertEquals(SingleValuedEnum.A, result);

        
        mapper = new ObjectMapper()
                .addHandler(new WeirdStringHandler(null));
        UUID result2 = mapper.readValue(quote("not a uuid!"), UUID.class);
        assertNull(result2);
    }

// com.fasterxml.jackson.databind.filter.ProblemHandlerTest::testInvalidTypeId
    public void testInvalidTypeId() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper()
            .addHandler(new TypeIdHandler(BaseImpl.class));
        BaseWrapper w = mapper.readValue("{\"value\":{\"type\":\"foo\",\"a\":4}}",
                BaseWrapper.class);
        assertNotNull(w);
        assertEquals(BaseImpl.class, w.value.getClass());
    }

// com.fasterxml.jackson.databind.filter.ProblemHandlerTest::testInvalidClassAsId
    public void testInvalidClassAsId() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper()
            .addHandler(new TypeIdHandler(Base2Impl.class));
        Base2Wrapper w = mapper.readValue("{\"value\":{\"clazz\":\"com.fizz\",\"a\":4}}",
                Base2Wrapper.class);
        assertNotNull(w);
        assertEquals(Base2Impl.class, w.value.getClass());
    }

// com.fasterxml.jackson.databind.filter.ProblemHandlerTest::testInvalidTypeIdFail
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

// com.fasterxml.jackson.databind.filter.ProblemHandlerTest::testInstantiationExceptionHandling
    public void testInstantiationExceptionHandling() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper()
            .addHandler(new InstantiationProblemHandler(BustedCtor.INST));
        BustedCtor w = mapper.readValue("{ }",
                BustedCtor.class);
        assertNotNull(w);
    }

// com.fasterxml.jackson.databind.filter.ProblemHandlerTest::testMissingInstantiatorHandling
    public void testMissingInstantiatorHandling() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper()
            .addHandler(new MissingInstantiationHandler(new NoDefaultCtor(13)))
            ;
        NoDefaultCtor w = mapper.readValue("{ \"x\" : true }", NoDefaultCtor.class);
        assertNotNull(w);
        assertEquals(13, w.value);
    }

// com.fasterxml.jackson.databind.filter.ProblemHandlerTest::testUnexpectedTokenHandling
    public void testUnexpectedTokenHandling() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper()
            .addHandler(new WeirdTokenHandler(Integer.valueOf(13)))
        ;
        Integer v = mapper.readValue("true", Integer.class);
        assertEquals(Integer.valueOf(13), v);
    }

// com.fasterxml.jackson.databind.filter.ReadOnlyProperties95Test::testReadOnlyProp
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

// com.fasterxml.jackson.databind.filter.TestAnyGetterFiltering::testAnyGetterFiltering
    public void testAnyGetterFiltering() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        FilterProvider prov = new SimpleFilterProvider().addFilter("anyFilter",
                SimpleBeanPropertyFilter.filterOutAllExcept("b"));
        assertEquals("{\"b\":\"2\"}", mapper.writer(prov).writeValueAsString(new AnyBean()));
    }

// com.fasterxml.jackson.databind.filter.TestAnyGetterFiltering::testAnyGetterIgnore
    public void testAnyGetterIgnore() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        assertEquals(aposToQuotes("{'a':'1','b':'3'}"),
                mapper.writeValueAsString(new AnyBeanWithIgnores()));
    }

// com.fasterxml.jackson.databind.filter.TestJsonFilter::testCheckSiblingContextFilter
    public void testCheckSiblingContextFilter() {
        FilterProvider prov = new SimpleFilterProvider().addFilter("checkSiblingContextFilter",
                new CheckSiblingContextFilter());

        ObjectMapper mapper = new ObjectMapper();
        mapper.setFilterProvider(prov);
        mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        mapper.valueToTree(new CheckSiblingContextBean());
    }

// com.fasterxml.jackson.databind.filter.TestJsonFilter::testSimpleInclusionFilter
    public void testSimpleInclusionFilter() throws Exception
    {
        FilterProvider prov = new SimpleFilterProvider().addFilter("RootFilter",
                SimpleBeanPropertyFilter.filterOutAllExcept("a"));
        assertEquals("{\"a\":\"a\"}", MAPPER.writer(prov).writeValueAsString(new Bean()));

        
        ObjectMapper mapper = new ObjectMapper();
        mapper.setFilterProvider(prov);
        assertEquals("{\"a\":\"a\"}", mapper.writeValueAsString(new Bean()));
    }

// com.fasterxml.jackson.databind.filter.TestJsonFilter::testIncludeAllFilter
    public void testIncludeAllFilter() throws Exception
    {
        FilterProvider prov = new SimpleFilterProvider().addFilter("RootFilter",
                SimpleBeanPropertyFilter.serializeAll());
        assertEquals("{\"a\":\"a\",\"b\":\"b\"}", MAPPER.writer(prov).writeValueAsString(new Bean()));
    }

// com.fasterxml.jackson.databind.filter.TestJsonFilter::testSimpleExclusionFilter
    public void testSimpleExclusionFilter() throws Exception
    {
        FilterProvider prov = new SimpleFilterProvider().addFilter("RootFilter",
                SimpleBeanPropertyFilter.serializeAllExcept("a"));
        assertEquals("{\"b\":\"b\"}", MAPPER.writer(prov).writeValueAsString(new Bean()));
    }

// com.fasterxml.jackson.databind.filter.TestJsonFilter::testMissingFilter
    public void testMissingFilter() throws Exception
    {
        
        try {
            MAPPER.writeValueAsString(new Bean());
            fail("Should have failed without configured filter");
        } catch (JsonMappingException e) { 
            verifyException(e, "Can not resolve PropertyFilter with id 'RootFilter'");
        }
        
        
        SimpleFilterProvider fp = new SimpleFilterProvider().setFailOnUnknownId(false);
        ObjectMapper mapper = new ObjectMapper();
        mapper.setFilterProvider(fp);
        String json = mapper.writeValueAsString(new Bean());
        assertEquals("{\"a\":\"a\",\"b\":\"b\"}", json);
    }

// com.fasterxml.jackson.databind.filter.TestJsonFilter::testDefaultFilter
    public void testDefaultFilter() throws Exception
    {
        FilterProvider prov = new SimpleFilterProvider().setDefaultFilter(SimpleBeanPropertyFilter.filterOutAllExcept("b"));
        assertEquals("{\"b\":\"b\"}", MAPPER.writer(prov).writeValueAsString(new Bean()));
    }

// com.fasterxml.jackson.databind.filter.TestJsonFilter::testIssue89
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

// com.fasterxml.jackson.databind.filter.TestJsonFilter::testFilterOnProperty
    public void testFilterOnProperty() throws Exception
    {
        FilterProvider prov = new SimpleFilterProvider()
            .addFilter("RootFilter", SimpleBeanPropertyFilter.filterOutAllExcept("a"))
            .addFilter("b", SimpleBeanPropertyFilter.filterOutAllExcept("b"));

        assertEquals("{\"first\":{\"a\":\"a\"},\"second\":{\"b\":\"b\"}}",
                MAPPER.writer(prov).writeValueAsString(new FilteredProps()));
    }

// com.fasterxml.jackson.databind.filter.TestMapFiltering::testMapFilteringViaProps
    public void testMapFilteringViaProps() throws Exception
    {
        FilterProvider prov = new SimpleFilterProvider().addFilter("filterX",
                SimpleBeanPropertyFilter.filterOutAllExcept("b"));
        String json = MAPPER.writer(prov).writeValueAsString(new MapBean());
        assertEquals(aposToQuotes("{'values':{'b':5}}"), json);
    }

// com.fasterxml.jackson.databind.filter.TestMapFiltering::testMapFilteringViaClass
    public void testMapFilteringViaClass() throws Exception
    {
        FilteredBean bean = new FilteredBean();
        bean.put("a", 4);
        bean.put("b", 3);
        FilterProvider prov = new SimpleFilterProvider().addFilter("filterForMaps",
                SimpleBeanPropertyFilter.filterOutAllExcept("b"));
        String json = MAPPER.writer(prov).writeValueAsString(bean);
        assertEquals(aposToQuotes("{'b':3}"), json);
    }

// com.fasterxml.jackson.databind.filter.TestMapFiltering::testNonNullValueMapViaProp
    public void testNonNullValueMapViaProp() throws IOException
    {
        String json = MAPPER.writeValueAsString(new NoNullValuesMapContainer()
            .add("a", "foo")
            .add("b", null)
            .add("c", "bar"));
        assertEquals(aposToQuotes("{'stuff':{'a':'foo','c':'bar'}}"), json);
    }

// com.fasterxml.jackson.databind.filter.TestMapFiltering::testMapFilteringWithAnnotations
    public void testMapFilteringWithAnnotations() throws Exception
    {
        FilterProvider prov = new SimpleFilterProvider().addFilter("filterX",
                new TestMapFilter());
        String json = MAPPER.writer(prov).writeValueAsString(new MapBean());
        
        assertEquals(aposToQuotes("{'values':{'a':2}}"), json);
    }

// com.fasterxml.jackson.databind.filter.TestMapFiltering::testMapNonNullValue
    public void testMapNonNullValue() throws IOException
    {
        String json = MAPPER.writeValueAsString(new NoNullsStringMap()
            .add("a", "foo")
            .add("b", null)
            .add("c", "bar"));
        assertEquals(aposToQuotes("{'a':'foo','c':'bar'}"), json);
    }

// com.fasterxml.jackson.databind.filter.TestMapFiltering::testMapNonEmptyValue
    public void testMapNonEmptyValue() throws IOException
    {
        String json = MAPPER.writeValueAsString(new NoEmptyStringsMap()
            .add("a", "foo")
            .add("b", "bar")
            .add("c", ""));
        assertEquals(aposToQuotes("{'a':'foo','b':'bar'}"), json);
    }

// com.fasterxml.jackson.databind.filter.TestMapFiltering::testMapAbsentValue
    public void testMapAbsentValue() throws IOException
    {
        String json = MAPPER.writeValueAsString(new NoAbsentStringMap()
            .add("a", "foo")
            .add("b", null));
        assertEquals(aposToQuotes("{'a':'foo'}"), json);
    }

// com.fasterxml.jackson.databind.filter.TestMapFiltering::testMapNullSerialization
    public void testMapNullSerialization() throws IOException
    {
        ObjectMapper m = new ObjectMapper();
        Map<String,String> map = new HashMap<String,String>();
        map.put("a", null);
        
        assertEquals("{\"a\":null}", m.writeValueAsString(map));
        
        m.configure(SerializationFeature.WRITE_NULL_MAP_VALUES, false);
        assertEquals("{}", m.writeValueAsString(map));
    }

// com.fasterxml.jackson.databind.filter.TestMapFiltering::testMapWithOnlyEmptyValues
    public void testMapWithOnlyEmptyValues() throws IOException
    {
        String json;

        
        json = MAPPER.writeValueAsString(new Wrapper497(new StringMap497()
            .add("a", "123")));
        assertEquals(aposToQuotes("{'values':{'a':'123'}}"), json);

        
        json = MAPPER.writeValueAsString(new Wrapper497(new StringMap497()
            .add("a", "")
            .add("b", null)));
        assertEquals(aposToQuotes("{}"), json);
    }

// com.fasterxml.jackson.databind.filter.TestSimpleSerializationIgnore::testSimpleIgnore
    public void testSimpleIgnore() throws Exception
    {
        
        Map<String,Object> result = writeAndMap(MAPPER, new SizeClassEnabledIgnore());
        assertEquals(1, result.size());
        assertEquals(Integer.valueOf(1), result.get("x"));
        assertNull(result.get("y"));
    }

// com.fasterxml.jackson.databind.filter.TestSimpleSerializationIgnore::testDisabledIgnore
    public void testDisabledIgnore() throws Exception
    {
        
        Map<String,Object> result = writeAndMap(MAPPER, new SizeClassDisabledIgnore());
        assertEquals(2, result.size());
        assertEquals(Integer.valueOf(3), result.get("x"));
        assertEquals(Integer.valueOf(4), result.get("y"));
    }

// com.fasterxml.jackson.databind.filter.TestSimpleSerializationIgnore::testIgnoreOver
    public void testIgnoreOver() throws Exception
    {
        
        Map<String,Object> result = writeAndMap(MAPPER, new BaseClassIgnore());
        assertEquals(1, result.size());
        assertEquals(Integer.valueOf(2), result.get("y"));

        
        result = writeAndMap(MAPPER, new SubClassNonIgnore());
        assertEquals(2, result.size());
        assertEquals(Integer.valueOf(3), result.get("x"));
        assertEquals(Integer.valueOf(2), result.get("y"));
    }

// com.fasterxml.jackson.databind.filter.TestSimpleSerializationIgnore::testIgnoreType
    public void testIgnoreType() throws Exception
    {
        assertEquals("{\"value\":13}", MAPPER.writeValueAsString(new NonIgnoredType()));
    }

// com.fasterxml.jackson.databind.filter.TestUnknownPropertyDeserialization::testUnknownHandlingDefault
    public void testUnknownHandlingDefault() throws Exception
    {
        try {
            MAPPER.readValue(new StringReader(JSON_UNKNOWN_FIELD), TestBean.class);
        } catch (JsonMappingException jex) {
            verifyException(jex, "Unrecognized field \"foo\"");
        }
    }

// com.fasterxml.jackson.databind.filter.TestUnknownPropertyDeserialization::testUnknownHandlingIgnoreWithHandler
    public void testUnknownHandlingIgnoreWithHandler() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        mapper.clearProblemHandlers();
        mapper.addHandler(new MyHandler());
        TestBean result = mapper.readValue(new StringReader(JSON_UNKNOWN_FIELD), TestBean.class);
        assertNotNull(result);
        assertEquals(1, result._a);
        assertEquals(-1, result._b);
        assertEquals("foo:START_ARRAY", result._unknown);
    }

// com.fasterxml.jackson.databind.filter.TestUnknownPropertyDeserialization::testUnknownHandlingIgnoreWithHandlerAndObjectReader
    public void testUnknownHandlingIgnoreWithHandlerAndObjectReader() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        mapper.clearProblemHandlers();
        TestBean result = mapper.readerFor(TestBean.class).withHandler(new MyHandler()).readValue(new StringReader(JSON_UNKNOWN_FIELD));
        assertNotNull(result);
        assertEquals(1, result._a);
        assertEquals(-1, result._b);
        assertEquals("foo:START_ARRAY", result._unknown);
    }

// com.fasterxml.jackson.databind.filter.TestUnknownPropertyDeserialization::testUnknownHandlingIgnoreWithFeature
    public void testUnknownHandlingIgnoreWithFeature() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
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

// com.fasterxml.jackson.databind.filter.TestUnknownPropertyDeserialization::testWithClassIgnore
    public void testWithClassIgnore() throws Exception
    {
        IgnoreSome result = MAPPER.readValue("{ \"a\":1,\"b\":2,\"c\":\"x\",\"d\":\"y\"}",
                IgnoreSome.class);
        
        assertEquals(1, result.a);
        assertEquals("y", result.d());
        
        assertEquals(0, result.b);
        assertNull(result.c());
    }

// com.fasterxml.jackson.databind.filter.TestUnknownPropertyDeserialization::testClassIgnoreWithMap
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

// com.fasterxml.jackson.databind.filter.TestUnknownPropertyDeserialization::testClassWithIgnoreUnknown
    public void testClassWithIgnoreUnknown() throws Exception
    {
        IgnoreUnknown result = MAPPER.readValue
            ("{\"b\":3,\"c\":[1,2],\"x\":{ },\"a\":-3}", IgnoreUnknown.class);
        assertEquals(-3, result.a);
    }

// com.fasterxml.jackson.databind.filter.TestUnknownPropertyDeserialization::testClassWithUnknownAndIgnore
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

// com.fasterxml.jackson.databind.filter.TestUnknownPropertyDeserialization::testPropertyIgnoral
    public void testPropertyIgnoral() throws Exception
    {
        XYZWrapper1 result = MAPPER.readValue("{\"value\":{\"y\":2,\"x\":1,\"z\":3}}", XYZWrapper1.class);
        assertEquals(2, result.value.y);
        assertEquals(3, result.value.z);
    }

// com.fasterxml.jackson.databind.filter.TestUnknownPropertyDeserialization::testPropertyIgnoralWithClass
    public void testPropertyIgnoralWithClass() throws Exception
    {
        XYZWrapper2 result = MAPPER.readValue("{\"value\":{\"y\":2,\"x\":1,\"z\":3}}",
                XYZWrapper2.class);
        assertEquals(1, result.value.x);
    }

// com.fasterxml.jackson.databind.filter.TestUnknownPropertyDeserialization::testPropertyIgnoralForMap
    public void testPropertyIgnoralForMap() throws Exception
    {
        MapWithoutX result = MAPPER.readValue("{\"values\":{\"x\":1,\"y\":2}}", MapWithoutX.class);
        assertNotNull(result.values);
        assertEquals(1, result.values.size());
        assertEquals(Integer.valueOf(2), result.values.get("y"));
    }

// com.fasterxml.jackson.databind.filter.TestUnknownPropertyDeserialization::testIssue987
    public void testIssue987() throws Exception
    {
        ObjectMapper jsonMapper = new ObjectMapper();
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

// com.fasterxml.jackson.databind.format.DateFormatTest::testTypeDefaults
    public void testTypeDefaults() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
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
        final String JSON = aposToQuotes(
 "{'id': 124,\n"
+" 'obj':[ 'com.sun.org.apache.xalan.internal.xsltc.trax.TemplatesImpl',\n"
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
        } catch (JsonMappingException e) {
            verifyException(e, "Illegal type");
            verifyException(e, "to deserialize");
            verifyException(e, "prevented for security reasons");
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

// com.fasterxml.jackson.databind.introspect.TestAnnotionBundles::testKeepAnnotationBundle
    public void testKeepAnnotationBundle() throws Exception
    {
        MAPPER.setAnnotationIntrospector(new BundleAnnotationIntrospector());
        assertEquals("{\"important\":42}", MAPPER.writeValueAsString(new InformingHolder()));
    }

// com.fasterxml.jackson.databind.introspect.TestAnnotionBundles::testRecursiveBundles
    public void testRecursiveBundles() throws Exception
    {
        assertEquals("{\"unimportant\":42}", MAPPER.writeValueAsString(new RecursiveHolder()));
    }

// com.fasterxml.jackson.databind.introspect.TestAnnotionBundles::testBundledIgnore
    public void testBundledIgnore() throws Exception
    {
        assertEquals("{\"foobar\":13}", MAPPER.writeValueAsString(new Bean()));
    }

// com.fasterxml.jackson.databind.introspect.TestAnnotionBundles::testVisibilityBundle
    public void testVisibilityBundle() throws Exception
    {
        assertEquals("{\"b\":5}", MAPPER.writeValueAsString(new NoAutoDetect()));
    }

// com.fasterxml.jackson.databind.introspect.TestAnnotionBundles::testIssue92
    public void testIssue92() throws Exception
    {
        assertEquals("{\"_id\":\"abc\"}", MAPPER.writeValueAsString(new Bean92()));
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
        AnnotatedClass ac = AnnotatedClass.constructWithoutSuperTypes(TypeResolverBean.class, mapper.getSerializationConfig());
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

// com.fasterxml.jackson.databind.introspect.TestPOJOPropertiesCollector::testSimpleIgnoreAndRename
    public void testSimpleIgnoreAndRename()
    {
        POJOPropertiesCollector coll = collector(MAPPER,
        		IgnoredRenamedSetter.class, true);
        Map<String, POJOPropertyBuilder> props = coll.getPropertyMap();
        assertEquals(1, props.size());
        POJOPropertyBuilder prop = props.get("y");
        assertNotNull(prop);
        assertTrue(prop.hasSetter());
        assertFalse(prop.hasGetter());
        assertFalse(prop.hasField());
    }

// com.fasterxml.jackson.databind.introspect.TestPOJOPropertiesCollector::testGlobalVisibilityForGetters
    public void testGlobalVisibilityForGetters()
    {
        ObjectMapper m = new ObjectMapper();
        m.configure(MapperFeature.AUTO_DETECT_GETTERS, false);
        POJOPropertiesCollector coll = collector(m, SimpleGetterVisibility.class, true);
        
        Map<String, POJOPropertyBuilder> props = coll.getPropertyMap();
        assertEquals(0, props.size());
    }

// com.fasterxml.jackson.databind.introspect.TestPOJOPropertiesCollector::testCollectionOfIgnored
    public void testCollectionOfIgnored()
    {
        POJOPropertiesCollector coll = collector(MAPPER, ImplicitIgnores.class, false);
        
        Map<String, POJOPropertyBuilder> props = coll.getPropertyMap();
        assertEquals(1, props.size());
        
        Collection<String> ign = coll.getIgnoredPropertyNames();
        assertEquals(2, ign.size());
        assertTrue(ign.contains("a"));
        assertTrue(ign.contains("b"));
    }

// com.fasterxml.jackson.databind.introspect.TestPOJOPropertiesCollector::testSimpleOrderingForDeserialization
    public void testSimpleOrderingForDeserialization()
    {
        POJOPropertiesCollector coll = collector(MAPPER, SortedProperties.class, false);
        List<BeanPropertyDefinition> props = coll.getProperties();
        assertEquals(4, props.size());
        assertEquals("a", props.get(0).getName());
        assertEquals("b", props.get(1).getName());
        assertEquals("c", props.get(2).getName());
        assertEquals("d", props.get(3).getName());
    }

// com.fasterxml.jackson.databind.introspect.TestPOJOPropertiesCollector::testSimpleWithType
    public void testSimpleWithType()
    {
        
        POJOPropertiesCollector coll = collector(MAPPER, TypeTestBean.class, true);
        List<BeanPropertyDefinition> props = coll.getProperties();
        assertEquals(1, props.size());
        assertEquals("value", props.get(0).getName());
        AnnotatedMember m = props.get(0).getAccessor();
        assertTrue(m instanceof AnnotatedMethod);
        assertEquals(Integer.class, m.getRawType());

        
        coll = collector(MAPPER, TypeTestBean.class, false);
        props = coll.getProperties();
        assertEquals(1, props.size());
        assertEquals("value", props.get(0).getName());
        m = props.get(0).getMutator();
        assertEquals(AnnotatedParameter.class, m.getClass());
        assertEquals(String.class, m.getRawType());
    }

// com.fasterxml.jackson.databind.introspect.TestPOJOPropertiesCollector::testInnerClassWithAnnotationsInCreator
    public void testInnerClassWithAnnotationsInCreator() throws Exception
    {
        BasicBeanDescription beanDesc;
        
        beanDesc = MAPPER.getSerializationConfig().introspect(MAPPER.constructType(Issue701Bean.class));
        assertNotNull(beanDesc);
        
        beanDesc = MAPPER.getDeserializationConfig().introspect(MAPPER.constructType(Issue701Bean.class));
        assertNotNull(beanDesc);
    }

// com.fasterxml.jackson.databind.introspect.TestPOJOPropertiesCollector::testUseAnnotationsFalse
    public void testUseAnnotationsFalse() throws Exception
    {
        
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(MapperFeature.USE_ANNOTATIONS, false);
        BasicBeanDescription beanDesc = mapper.getSerializationConfig().introspect(mapper.constructType(Jackson703.class));
        assertNotNull(beanDesc);

        Jackson703 bean = new Jackson703();
        String json = mapper.writeValueAsString(bean);
        assertNotNull(json);
    }

// com.fasterxml.jackson.databind.introspect.TestPOJOPropertiesCollector::testJackson744
    public void testJackson744() throws Exception
    {
        BeanDescription beanDesc = MAPPER.getDeserializationConfig().introspect(MAPPER.constructType(Issue744Bean.class));
        assertNotNull(beanDesc);
        AnnotatedMethod setter = beanDesc.findAnySetter();
        assertNotNull(setter);
    }

// com.fasterxml.jackson.databind.introspect.TestPOJOPropertiesCollector::testPropertyDesc
    public void testPropertyDesc() throws Exception
    {
        
        BeanDescription beanDesc = MAPPER.getDeserializationConfig().introspect(MAPPER.constructType(PropDescBean.class));
        _verifyProperty(beanDesc, true, false, "13");
        
        beanDesc = MAPPER.getSerializationConfig().introspect(MAPPER.constructType(PropDescBean.class));
        _verifyProperty(beanDesc, true, false, "13");
    }

// com.fasterxml.jackson.databind.introspect.TestPOJOPropertiesCollector::testPropertyIndex
    public void testPropertyIndex() throws Exception
    {
        BeanDescription beanDesc = MAPPER.getDeserializationConfig().introspect(MAPPER.constructType(PropDescBean.class));
        _verifyProperty(beanDesc, false, true, "13");
        beanDesc = MAPPER.getSerializationConfig().introspect(MAPPER.constructType(PropDescBean.class));
        _verifyProperty(beanDesc, false, true, "13");
    }

// com.fasterxml.jackson.databind.introspect.TestPOJOPropertiesCollector::testDuplicateGetters
    public void testDuplicateGetters() throws Exception
    {
        POJOPropertiesCollector coll = collector(MAPPER, DuplicateGetterBean.class, true);
        List<BeanPropertyDefinition> props = coll.getProperties();
        assertEquals(1, props.size());
        BeanPropertyDefinition prop = props.get(0);
        assertEquals("bloop", prop.getName());
        assertTrue(prop.getGetter().hasAnnotation(A.class));
        assertTrue(prop.getGetter().hasAnnotation(B.class));
    }

// com.fasterxml.jackson.databind.introspect.TestPOJOPropertiesCollector::testDuplicateGettersCreator
    public void testDuplicateGettersCreator() throws Exception
    {
        POJOPropertiesCollector coll = collector(MAPPER, DuplicateGetterCreatorBean.class, true);
        List<BeanPropertyDefinition> props = coll.getProperties();
        assertEquals(1, props.size());
        POJOPropertyBuilder prop = (POJOPropertyBuilder) props.get(0);
        assertEquals("bloop", prop.getName());
        
        assertTrue(prop._getters.value.hasAnnotation(A.class));
        assertNotNull(prop._getters.next);
        assertTrue(prop._getters.next.value.hasAnnotation(A.class));
    }

// com.fasterxml.jackson.databind.introspect.TestPropertyConflicts::testFailWithDupProps
    public void testFailWithDupProps() throws Exception
    {
        BeanWithConflict bean = new BeanWithConflict();
        try {
            String json = objectWriter().writeValueAsString(bean);
            fail("Should have failed due to conflicting accessor definitions; got JSON = "+json);
        } catch (JsonProcessingException e) {
            verifyException(e, "Conflicting getter definitions");
        }
    }

// com.fasterxml.jackson.databind.introspect.TestPropertyConflicts::testRegularAndIsGetter
    public void testRegularAndIsGetter() throws Exception
    {
        final ObjectWriter writer = objectWriter();
        
        
        assertEquals("{\"value\":4}", writer.writeValueAsString(new Getters1A()));
        assertEquals("{\"value\":4}", writer.writeValueAsString(new Getters1B()));

        
        ObjectMapper mapper = objectMapper();
        assertEquals(1, mapper.readValue("{\"value\":1}", Getters1A.class).value);
        assertEquals(2, mapper.readValue("{\"value\":2}", Getters1B.class).value);
    }

// com.fasterxml.jackson.databind.introspect.TestPropertyConflicts::testInferredNameConflictsWithGetters
    public void testInferredNameConflictsWithGetters() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setAnnotationIntrospector(new InferingIntrospector());
        String json = mapper.writeValueAsString(new Infernal());
        assertEquals(aposToQuotes("{'name':'Bob'}"), json);
    }

// com.fasterxml.jackson.databind.introspect.TestPropertyConflicts::testInferredNameConflictsWithSetters
    public void testInferredNameConflictsWithSetters() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setAnnotationIntrospector(new InferingIntrospector());
        Infernal inf = mapper.readValue(aposToQuotes("{'stuff':'Bob'}"), Infernal.class);
        assertNotNull(inf);
    }

// com.fasterxml.jackson.databind.introspect.TestPropertyConflicts::testIssue541
    public void testIssue541() throws Exception {
        final ObjectMapper mapper = new ObjectMapper();
        mapper.disable(
                MapperFeature.AUTO_DETECT_CREATORS,
                MapperFeature.AUTO_DETECT_FIELDS,
                MapperFeature.AUTO_DETECT_GETTERS,
                MapperFeature.AUTO_DETECT_IS_GETTERS,
                MapperFeature.AUTO_DETECT_SETTERS,
                MapperFeature.USE_GETTERS_AS_SETTERS
        );
        Bean541 data = mapper.readValue("{\"str\":\"the string\"}", Bean541.class);
        if (data == null) {
            throw new IllegalStateException("data is null");
        }
        if (!"the string".equals(data.getStr())) {
            throw new IllegalStateException("bad value for data.str");
        }
    }

// com.fasterxml.jackson.databind.introspect.TestPropertyRename::testCreatorPropRenameWithIgnore
    public void testCreatorPropRenameWithIgnore() throws Exception
    {
        Bean323WithIgnore input = new Bean323WithIgnore(7);
        assertEquals("{\"b\":7}", objectWriter().writeValueAsString(input));
    }

// com.fasterxml.jackson.databind.introspect.TestPropertyRename::testCreatorPropRenameWithCleave
    public void testCreatorPropRenameWithCleave() throws Exception
    {
        assertEquals("{\"a\":7,\"b\":7}",
        		objectWriter().writeValueAsString(new Bean323WithExplicitCleave1(7)));
        
        assertEquals("{\"b\":7}", objectWriter().writeValueAsString(new Bean323WithExplicitCleave2(7)));
    }

// com.fasterxml.jackson.databind.introspect.TestScalaLikeImplicitProperties::testValProperty
    public void testValProperty() throws Exception
    {
        ObjectMapper m = manglingMapper();

        assertEquals("{\"prop\":\"val\"}", m.writeValueAsString(new ValProperty("val")));
    }

// com.fasterxml.jackson.databind.introspect.TestScalaLikeImplicitProperties::testValWithBeanProperty
    public void testValWithBeanProperty() throws Exception
    {
        ObjectMapper m = manglingMapper();

        assertEquals("{\"prop\":\"val\"}", m.writeValueAsString(new ValWithBeanProperty("val")));
    }

// com.fasterxml.jackson.databind.introspect.TestScalaLikeImplicitProperties::testVarProperty
    public void testVarProperty() throws Exception
    {
        ObjectMapper m = manglingMapper();

        assertEquals("{\"prop\":\"var\"}", m.writeValueAsString(new VarProperty("var")));
        VarProperty result = m.readValue("{\"prop\":\"read\"}", VarProperty.class);
        assertEquals("read", result.prop());
    }

// com.fasterxml.jackson.databind.introspect.TestScalaLikeImplicitProperties::testVarWithBeanProperty
    public void testVarWithBeanProperty() throws Exception
    {
        ObjectMapper m = manglingMapper();

        assertEquals("{\"prop\":\"var\"}", m.writeValueAsString(new VarWithBeanProperty("var")));
        VarWithBeanProperty result = m.readValue("{\"prop\":\"read\"}", VarWithBeanProperty.class);
        assertEquals("read", result.prop());
    }

// com.fasterxml.jackson.databind.introspect.TestScalaLikeImplicitProperties::testGetterSetterProperty
    public void testGetterSetterProperty() throws Exception
    {
        ObjectMapper m = manglingMapper();

        assertEquals("{\"prop\":\"get/set\"}", m.writeValueAsString(new GetterSetterProperty()));
        GetterSetterProperty result = m.readValue("{\"prop\":\"read\"}", GetterSetterProperty.class);
        assertEquals("read", result.prop());
    }

// com.fasterxml.jackson.databind.introspect.TransientTest::testTransientFieldHandling
    public void testTransientFieldHandling() throws Exception
    {
        
        assertEquals(aposToQuotes("{'x':42,'value':3}"),
                MAPPER.writeValueAsString(new ClassyTransient()));
        assertEquals(aposToQuotes("{'a':1}"),
                MAPPER.writeValueAsString(new SimplePrunableTransient()));

        
        ObjectMapper m = new ObjectMapper()
            .enable(MapperFeature.PROPAGATE_TRANSIENT_MARKER);
        assertEquals(aposToQuotes("{'x':42}"),
                m.writeValueAsString(new ClassyTransient()));
    }

// com.fasterxml.jackson.databind.introspect.TransientTest::testBeanTransient
    public void testBeanTransient() throws Exception
    {
        assertEquals(aposToQuotes("{'y':4}"),
                MAPPER.writeValueAsString(new BeanTransient()));
    }

// com.fasterxml.jackson.databind.introspect.TransientTest::testOverridingTransient
    public void testOverridingTransient() throws Exception
    {
        assertEquals(aposToQuotes("{'tValue':38}"),
                MAPPER.writeValueAsString(new OverridableTransient(38)));
    }

// com.fasterxml.jackson.databind.jsonschema.NewSchemaTest::testBasicTraversal
    public void testBasicTraversal() throws Exception
    {
        MAPPER.acceptJsonFormatVisitor(POJO.class, new JsonFormatVisitorWrapper.Base());
    }

// com.fasterxml.jackson.databind.jsonschema.NewSchemaTest::testSimpleEnum
    public void testSimpleEnum() throws Exception
    {
        final Set<String> values = new TreeSet<String>();
        ObjectWriter w = MAPPER.writer(SerializationFeature.WRITE_ENUMS_USING_TO_STRING);

        w.acceptJsonFormatVisitor(TestEnum.class, new JsonFormatVisitorWrapper.Base() {
            @Override
            public JsonStringFormatVisitor expectStringFormat(JavaType type) {
                return new JsonStringFormatVisitor() {
                    @Override
                    public void enumTypes(Set<String> enums) {
                        values.addAll(enums);
                    }

                    @Override
                    public void format(JsonValueFormat format) { }
                };
            }
        });

        assertEquals(3, values.size());
        TreeSet<String> exp = new TreeSet<String>(Arrays.asList(
                        "ToString:A",
                        "ToString:B",
                        "ToString:C"
                        ));
        assertEquals(exp, values);
    }

// com.fasterxml.jackson.databind.jsonschema.NewSchemaTest::testEnumWithJsonValue
    public void testEnumWithJsonValue() throws Exception
    {
        final Set<String> values = new TreeSet<String>();
        MAPPER.acceptJsonFormatVisitor(TestEnumWithJsonValue.class,
                new JsonFormatVisitorWrapper.Base() {
            @Override
            public JsonStringFormatVisitor expectStringFormat(JavaType type) {
                return new JsonStringFormatVisitor() {
                    @Override
                    public void enumTypes(Set<String> enums) {
                        values.addAll(enums);
                    }

                    @Override
                    public void format(JsonValueFormat format) { }
                };
            }
        });

        assertEquals(3, values.size());
        TreeSet<String> exp = new TreeSet<String>(Arrays.asList(
                        "value-A",
                        "value-B",
                        "value-C"
                        ));
        assertEquals(exp, values);
    }

// com.fasterxml.jackson.databind.jsonschema.NewSchemaTest::testJsonValueFormatHandling
    public void testJsonValueFormatHandling() throws Exception
    {
        
        final String EXP = quote("host-name");
        assertEquals(EXP, MAPPER.writeValueAsString(JsonValueFormat.HOST_NAME));

        
        assertSame(JsonValueFormat.HOST_NAME, MAPPER.readValue(EXP, JsonValueFormat.class));
    }

// com.fasterxml.jackson.databind.jsonschema.NewSchemaTest::testSimpleNumbers
    public void testSimpleNumbers() throws Exception
    {
        final StringBuilder sb = new StringBuilder();
        
        MAPPER.acceptJsonFormatVisitor(Numbers.class,
                new JsonFormatVisitorWrapper.Base() {
            @Override
            public JsonObjectFormatVisitor expectObjectFormat(final JavaType type) {
                return new JsonObjectFormatVisitor.Base(getProvider()) {
                    @Override
                    public void optionalProperty(BeanProperty prop) throws JsonMappingException {
                        sb.append("[optProp ").append(prop.getName()).append("(");
                        JsonSerializer<Object> ser = null;
                        if (prop instanceof BeanPropertyWriter) {
                            BeanPropertyWriter bpw = (BeanPropertyWriter) prop;
                            ser = bpw.getSerializer();
                        }
                        final SerializerProvider prov = getProvider();
                        if (ser == null) {
                            ser = prov.findValueSerializer(prop.getType(), prop);
                        }
                        ser.acceptJsonFormatVisitor(new JsonFormatVisitorWrapper.Base() {
                            @Override
                            public JsonNumberFormatVisitor expectNumberFormat(
                                    JavaType t) throws JsonMappingException {
                                return new JsonNumberFormatVisitor() {
                                    @Override
                                    public void format(JsonValueFormat format) {
                                        sb.append("[numberFormat=").append(format).append("]");
                                    }

                                    @Override
                                    public void enumTypes(Set<String> enums) { }

                                    @Override
                                    public void numberType(NumberType numberType) {
                                        sb.append("[numberType=").append(numberType).append("]");
                                    }
                                };
                            }

                            @Override
                            public JsonIntegerFormatVisitor expectIntegerFormat(JavaType t) throws JsonMappingException {
                                return new JsonIntegerFormatVisitor() {
                                    @Override
                                    public void format(JsonValueFormat format) {
                                        sb.append("[integerFormat=").append(format).append("]");
                                    }

                                    @Override
                                    public void enumTypes(Set<String> enums) { }

                                    @Override
                                    public void numberType(NumberType numberType) {
                                        sb.append("[numberType=").append(numberType).append("]");
                                    }
                                };
                            }
                        }, prop.getType());

                        sb.append(")]");
                    }
                };
            }
        });
        assertEquals("[optProp dec([numberType=BIG_DECIMAL])][optProp bigInt([numberType=BIG_INTEGER])]",
                sb.toString());
    }

// com.fasterxml.jackson.databind.jsonschema.TestGenerateJsonSchema::testOldSchemaGeneration
    public void testOldSchemaGeneration() throws Exception
    {
        JsonSchema jsonSchema = MAPPER.generateJsonSchema(SimpleBean.class);
        
        assertNotNull(jsonSchema);

        
        assertTrue(jsonSchema.equals(jsonSchema));
        assertFalse(jsonSchema.equals(null));
        assertFalse(jsonSchema.equals("foo"));

        
        assertNotNull(jsonSchema.toString());
        assertNotNull(JsonSchema.getDefaultSchemaNode());

        ObjectNode root = jsonSchema.getSchemaNode();
        assertEquals("object", root.get("type").asText());
        assertEquals(false, root.path("required").booleanValue());
        JsonNode propertiesSchema = root.get("properties");
        assertNotNull(propertiesSchema);
        JsonNode property1Schema = propertiesSchema.get("property1");
        assertNotNull(property1Schema);
        assertEquals("integer", property1Schema.get("type").asText());
        assertEquals(false, property1Schema.path("required").booleanValue());
        JsonNode property2Schema = propertiesSchema.get("property2");
        assertNotNull(property2Schema);
        assertEquals("string", property2Schema.get("type").asText());
        assertEquals(false, property2Schema.path("required").booleanValue());
        JsonNode property3Schema = propertiesSchema.get("property3");
        assertNotNull(property3Schema);
        assertEquals("array", property3Schema.get("type").asText());
        assertEquals(false, property3Schema.path("required").booleanValue());
        assertEquals("string", property3Schema.get("items").get("type").asText());
        JsonNode property4Schema = propertiesSchema.get("property4");
        assertNotNull(property4Schema);
        assertEquals("array", property4Schema.get("type").asText());
        assertEquals(false, property4Schema.path("required").booleanValue());
        assertEquals("number", property4Schema.get("items").get("type").asText());
    }

// com.fasterxml.jackson.databind.jsonschema.TestGenerateJsonSchema::testGeneratingJsonSchemaWithFilters
    public void testGeneratingJsonSchemaWithFilters() throws Exception {
    	ObjectMapper mapper = new ObjectMapper();
    	mapper.setFilters(secretFilterProvider);
    	JsonSchema schema = mapper.generateJsonSchema(FilteredBean.class);
    	JsonNode node = schema.getSchemaNode().get("properties");
    	assertTrue(node.has("obvious"));
    	assertFalse(node.has("secret"));
    }

// com.fasterxml.jackson.databind.jsonschema.TestGenerateJsonSchema::testSchemaSerialization
    public void testSchemaSerialization() throws Exception
    {
        JsonSchema jsonSchema = MAPPER.generateJsonSchema(SimpleBean.class);
        Map<String,Object> result = writeAndMap(MAPPER, jsonSchema);
        assertNotNull(result);
        
        assertEquals("object", result.get("type"));
        
        assertNull(result.get("required"));
        assertNotNull(result.get("properties"));
    }

// com.fasterxml.jackson.databind.jsonschema.TestGenerateJsonSchema::testThatObjectsHaveNoItems
    public void testThatObjectsHaveNoItems() throws Exception
    {
        JsonSchema jsonSchema = MAPPER.generateJsonSchema(TrivialBean.class);
        String json = jsonSchema.toString().replaceAll("\"", "'");
        
        
        assertEquals("{'type':'object','properties':{'name':{'type':'string'}}}",
                json);
    }

// com.fasterxml.jackson.databind.jsonschema.TestGenerateJsonSchema::testSchemaId
    public void testSchemaId() throws Exception
    {
        JsonSchema jsonSchema = MAPPER.generateJsonSchema(BeanWithId.class);
        String json = jsonSchema.toString().replaceAll("\"", "'");
        assertEquals("{'type':'object','id':'myType','properties':{'value':{'type':'string'}}}",
                json);
    }

// com.fasterxml.jackson.databind.jsonschema.TestGenerateJsonSchema::testUnwrapping
    public void testUnwrapping()  throws Exception
    {
        JsonSchema jsonSchema = MAPPER.generateJsonSchema(UnwrappingRoot.class);
        String json = jsonSchema.toString().replaceAll("\"", "'");
        String EXP = "{'type':'object',"
                +"'properties':{'age':{'type':'integer'},"
                +"'name.first':{'type':'string'},'name.last':{'type':'string'}}}";
        assertEquals(EXP, json);
    }

// com.fasterxml.jackson.databind.jsonschema.TestGenerateJsonSchema::testNumberTypes
    public void testNumberTypes()  throws Exception
    {
        JsonSchema jsonSchema = MAPPER.generateJsonSchema(Numbers.class);
        String json = quotesToApos(jsonSchema.toString());
        String EXP = "{'type':'object',"
                +"'properties':{'dec':{'type':'number'},"
                +"'bigInt':{'type':'integer'}}}";
        assertEquals(EXP, json);
    }

// com.fasterxml.jackson.databind.jsonschema.TestReadJsonSchema::testDeserializeSimple
    public void testDeserializeSimple() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        JsonSchema schema = mapper.generateJsonSchema(Schemable.class);
        assertNotNull(schema);

        String schemaStr = mapper.writeValueAsString(schema);
        assertNotNull(schemaStr);
        JsonSchema result = mapper.readValue(schemaStr, JsonSchema.class);
        assertEquals("Trying to read from '"+schemaStr+"'", schema, result);
    }

// com.fasterxml.jackson.databind.jsontype.AbstracTypeMapping1186Test::testDeserializeMyContainer
    public void testDeserializeMyContainer() throws Exception {
        com.fasterxml.jackson.databind.Module module = new SimpleModule().addAbstractTypeMapping(IContainer.class, MyContainer.class);
        final ObjectMapper mapper = new ObjectMapper().registerModule(module);
        String json = "{\"ts\": [ { \"msg\": \"hello\"} ] }";
        final Object o = mapper.readValue(json,
                mapper.getTypeFactory().constructParametricType(IContainer.class, MyObject.class));
        assertEquals(MyContainer.class, o.getClass());
        MyContainer<?> myc = (MyContainer<?>) o;
        assertEquals(1, myc.ts.size());
        Object value = myc.ts.get(0);
        assertEquals(MyObject.class, value.getClass());
    }

// com.fasterxml.jackson.databind.jsontype.DefaultTypingWithPrimitivesTest::testDefaultTypingWithLong
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

// com.fasterxml.jackson.databind.jsontype.Generic1128Test::testIssue1128
    public void testIssue1128() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);

        final DevMContainer devMContainer1 = new DevMContainer();
        final DevM entity = new DevM();
        final Dev parent = new Dev();
        parent.id = 2L;
        entity.parent = parent;
        devMContainer1.entity = entity;
    
        String json = mapper.writeValueAsString(devMContainer1);

        final DevMContainer devMContainer = mapper.readValue(json, DevMContainer.class);
        long id = devMContainer.entity.parent.id;

        assertEquals(2, id);
    }

// com.fasterxml.jackson.databind.jsontype.PolymorphicList1451SerTest::testCollectionWithTypeInfo
    public void testCollectionWithTypeInfo() throws Exception {
        ObjectMapper mapper = new ObjectMapper()
                .disable(SerializationFeature.EAGER_SERIALIZER_FETCH)

                ;

        List<A> input = new ArrayList<A>();
        A a = new A();
        a.a = "a1";
        input.add(a);

        B b = new B();
        b.b = "b";
        b.a = "a2";
        input.add(b);

        final TypeReference<?> typeRef = 
                new TypeReference<Collection<A>>(){};
        ObjectWriter writer = mapper.writerFor(typeRef);

        String result = writer.writeValueAsString(input);

        assertEquals(aposToQuotes(
"[{'@class':'."+CLASS_NAME+"$A','a':'a1'},{'@class':'."+CLASS_NAME+"$B','a':'a2','b':'b'}]"
), result);

        List<A> output = mapper.readerFor(typeRef)
                .readValue(result);
        assertEquals(2, output.size());
        assertEquals(A.class, output.get(0).getClass());
        assertEquals(B.class, output.get(1).getClass());
    }

// com.fasterxml.jackson.databind.jsontype.PolymorphicViaRefTypeTest::testPolymorphicAtomicRefProperty
    public void testPolymorphicAtomicRefProperty() throws Exception
    {
        TypeInfoAtomic data = new TypeInfoAtomic();
        data.value = new AtomicReference<BaseForAtomic>(new ImplForAtomic(42));
        String json = MAPPER.writeValueAsString(data);
        TypeInfoAtomic result = MAPPER.readValue(json, TypeInfoAtomic.class);
        assertNotNull(result);
        BaseForAtomic value = result.value.get();
        assertNotNull(value);
        assertEquals(ImplForAtomic.class, value.getClass());
        assertEquals(42, ((ImplForAtomic) value).x);
    }

// com.fasterxml.jackson.databind.jsontype.PolymorphicViaRefTypeTest::testAtomicRefViaDefaultTyping
    public void testAtomicRefViaDefaultTyping() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        mapper.enableDefaultTyping(DefaultTyping.NON_FINAL);
        AtomicStringWrapper data = new AtomicStringWrapper("foo");
        String json = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(data);
        AtomicStringWrapper result = mapper.readValue(json, AtomicStringWrapper.class);
        assertNotNull(result);
        assertNotNull(result.wrapper);
        assertEquals(AtomicReference.class, result.wrapper.getClass());
        StringWrapper w = result.wrapper.get();
        assertEquals("foo", w.str);
    }

// com.fasterxml.jackson.databind.jsontype.TestAbstractContainers::testAbstractLists
    public void testAbstractLists() throws Exception
    {
        ListWrapper w = new ListWrapper();
        w.list.add("x");

        String json = MAPPER.writeValueAsString(w);
        Object o = MAPPER.readValue(json, ListWrapper.class);
        assertEquals(ListWrapper.class, o.getClass());
        ListWrapper out = (ListWrapper) o;
        assertNotNull(out.list);
        assertEquals(1, out.list.size());
        assertEquals("x", out.list.get(0));
   }

// com.fasterxml.jackson.databind.jsontype.TestAbstractContainers::testAbstractMaps
    public void testAbstractMaps() throws Exception
    {
        MapWrapper w = new MapWrapper();
        w.map.put("key1", "name1");

        String json = MAPPER.writeValueAsString(w);
        Object o = MAPPER.readValue(json, MapWrapper.class);
        assertEquals(MapWrapper.class, o.getClass());
        MapWrapper out = (MapWrapper) o;
        assertEquals(1, out.map.size());
   }

// com.fasterxml.jackson.databind.jsontype.TestAbstractTypeNames::testEmptyCollection
    public void testEmptyCollection() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(SerializationFeature.INDENT_OUTPUT, true);
        List<User>friends = new ArrayList<User>();
        friends.add(new DefaultUser("Joe Hildebrandt", null));
        friends.add(new DefaultEmployee("Richard Nasr",null,"MDA"));

        User user = new DefaultEmployee("John Vanspronssen", friends, "MDA");
        String json = mapper.writeValueAsString(user);

        
        mapper = new ObjectMapper();
        mapper.registerSubtypes(DefaultEmployee.class);
        mapper.registerSubtypes(DefaultUser.class);
        
        User result = mapper.readValue(json, User.class);
        assertNotNull(result);
        assertEquals(DefaultEmployee.class, result.getClass());

        friends = result.getFriends();
        assertEquals(2, friends.size());
        assertEquals(DefaultUser.class, friends.get(0).getClass());
        assertEquals(DefaultEmployee.class, friends.get(1).getClass());
    }

// com.fasterxml.jackson.databind.jsontype.TestAbstractTypeNames::testInnerClassWithType
    public void testInnerClassWithType() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        mapper.enableDefaultTyping(DefaultTyping.NON_FINAL);
        String json = mapper.writeValueAsString(new BeanWithAnon());
        BeanWithAnon result = mapper.readValue(json, BeanWithAnon.class);
        assertEquals(BeanWithAnon.class, result.getClass());
    }

// com.fasterxml.jackson.databind.jsontype.TestCustomTypeIdResolver::testCustomTypeIdResolver
    public void testCustomTypeIdResolver() throws Exception
    {
        List<JavaType> types = new ArrayList<JavaType>();
        CustomResolver.initTypes = types;
        String json = MAPPER.writeValueAsString(new CustomBean[] { new CustomBeanImpl(28) });
        assertEquals("[{\"*\":{\"x\":28}}]", json);
        assertEquals(1, types.size());
        assertEquals(CustomBean.class, types.get(0).getRawClass());

        types = new ArrayList<JavaType>();
        CustomResolver.initTypes = types;
        CustomBean[] result = MAPPER.readValue(json, CustomBean[].class);
        assertNotNull(result);
        assertEquals(1, result.length);
        assertEquals(28, ((CustomBeanImpl) result[0]).x);
        assertEquals(1, types.size());
        assertEquals(CustomBean.class, types.get(0).getRawClass());
    }

// com.fasterxml.jackson.databind.jsontype.TestCustomTypeIdResolver::testCustomWithExternal
    public void testCustomWithExternal() throws Exception
    {
        ExtBeanWrapper w = new ExtBeanWrapper();
        w.value = new ExtBeanImpl(12);

        String json = MAPPER.writeValueAsString(w);

        ExtBeanWrapper out = MAPPER.readValue(json, ExtBeanWrapper.class);
        assertNotNull(out);
        
        assertEquals(12, ((ExtBeanImpl) out.value).y);
    }

// com.fasterxml.jackson.databind.jsontype.TestCustomTypeIdResolver::testPolymorphicTypeViaCustom
    public void testPolymorphicTypeViaCustom() throws Exception {
        Base1270<Poly1> req = new Base1270<Poly1>();
        Poly1 o = new Poly1();
        o.val = "optionValue";
        req.options = o;
        req.val = "some value";
        Top1270 top = new Top1270();
        top.b = req;
        String json = MAPPER.writeValueAsString(top);
        JsonNode tree = MAPPER.readTree(json);
        assertNotNull(tree.get("b"));
        assertNotNull(tree.get("b").get("options"));
        assertNotNull(tree.get("b").get("options").get("val"));

        
        Top1270 itemRead = MAPPER.readValue(json, Top1270.class);
        assertNotNull(itemRead);
        assertNotNull(itemRead.b);
    }

// com.fasterxml.jackson.databind.jsontype.TestDefaultForArrays::testArrayTypingSimple
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

// com.fasterxml.jackson.databind.jsontype.TestDefaultForArrays::testArrayTypingNested
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

// com.fasterxml.jackson.databind.jsontype.TestDefaultForArrays::testNodeInArray
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

// com.fasterxml.jackson.databind.jsontype.TestDefaultForArrays::testNodeInEmptyArray
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

// com.fasterxml.jackson.databind.jsontype.TestDefaultForArrays::testArraysOfArrays
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

// com.fasterxml.jackson.databind.jsontype.TestDefaultForEnums::testSimpleEnumBean
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

// com.fasterxml.jackson.databind.jsontype.TestDefaultForEnums::testSimpleEnumsInObjectArray
    public void testSimpleEnumsInObjectArray() throws Exception
    {
        ObjectMapper m = new ObjectMapper();
        m.enableDefaultTyping();
        
        
        String json = m.writeValueAsString(new Object[] { TestEnum.A });
        assertEquals("[[\"com.fasterxml.jackson.databind.jsontype.TestDefaultForEnums$TestEnum\",\"A\"]]", json);

        
        Object[] value = m.readValue(json, Object[].class);
        assertEquals(1, value.length);
        assertSame(TestEnum.A, value[0]);
    }

// com.fasterxml.jackson.databind.jsontype.TestDefaultForEnums::testSimpleEnumsAsField
    public void testSimpleEnumsAsField() throws Exception
    {
        ObjectMapper m = new ObjectMapper();
        m.enableDefaultTyping();
        String json = m.writeValueAsString(new EnumHolder(TestEnum.B));
        assertEquals("{\"value\":[\"com.fasterxml.jackson.databind.jsontype.TestDefaultForEnums$TestEnum\",\"B\"]}", json);
        EnumHolder holder = m.readValue(json, EnumHolder.class);
        assertSame(TestEnum.B, holder.value);
    }

// com.fasterxml.jackson.databind.jsontype.TestDefaultForLists::testListOfLongs
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

// com.fasterxml.jackson.databind.jsontype.TestDefaultForLists::testListOfNumbers
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

// com.fasterxml.jackson.databind.jsontype.TestDefaultForLists::testDateTypes
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

// com.fasterxml.jackson.databind.jsontype.TestDefaultForLists::testJackson628
    public void testJackson628() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        mapper.enableDefaultTyping(DefaultTyping.NON_FINAL);
        ArrayList<Foo> data = new ArrayList<Foo>();
        String json = mapper.writeValueAsString(data);
        List<?> output = mapper.readValue(json, List.class);
        assertTrue(output.isEmpty());
    }

// com.fasterxml.jackson.databind.jsontype.TestDefaultForLists::testJackson667
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

// com.fasterxml.jackson.databind.jsontype.TestDefaultForMaps::testJackson428
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

// com.fasterxml.jackson.databind.jsontype.TestDefaultForMaps::testList
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

// com.fasterxml.jackson.databind.jsontype.TestDefaultForMaps::testMap
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

// com.fasterxml.jackson.databind.jsontype.TestDefaultForObject::testBeanAsObject
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

// com.fasterxml.jackson.databind.jsontype.TestDefaultForObject::testBeanAsObjectUsingAsProperty
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

// com.fasterxml.jackson.databind.jsontype.TestDefaultForObject::testAbstractBean
    public void testAbstractBean() throws Exception
    {
        
        ObjectMapper m = new ObjectMapper();
        AbstractBean[] input = new AbstractBean[] { new StringBean("xyz") };
        String serial = m.writeValueAsString(input);
        try {
            m.readValue(serial, AbstractBean[].class);
            fail("Should have failed");
        } catch (JsonMappingException e) {
            
            verifyException(e, "can not construct");
        }
        
        
        m = new ObjectMapper();
        m.enableDefaultTyping(ObjectMapper.DefaultTyping.OBJECT_AND_NON_CONCRETE);
        serial = m.writeValueAsString(input);
        AbstractBean[] beans = m.readValue(serial, AbstractBean[].class);
        assertEquals(1, beans.length);
        assertEquals(StringBean.class, beans[0].getClass());
        assertEquals("xyz", ((StringBean) beans[0]).name);
    }

// com.fasterxml.jackson.databind.jsontype.TestDefaultForObject::testNonFinalBean
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

// com.fasterxml.jackson.databind.jsontype.TestDefaultForObject::testNullValue
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

// com.fasterxml.jackson.databind.jsontype.TestDefaultForObject::testEnumAsObject
    public void testEnumAsObject() {}

// com.fasterxml.jackson.databind.jsontype.TestDefaultForObject::testEnumSet
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

// com.fasterxml.jackson.databind.jsontype.TestDefaultForObject::testEnumMap
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

// com.fasterxml.jackson.databind.jsontype.TestDefaultForObject::testJackson311
    public void testJackson311() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        mapper.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);
        String json = mapper.writeValueAsString(new PolymorphicType("hello", 2));
        PolymorphicType value = mapper.readValue(json, PolymorphicType.class);
        assertEquals("hello", value.foo);
        assertEquals(Integer.valueOf(2), value.bar);
    }

// com.fasterxml.jackson.databind.jsontype.TestDefaultForObject::testTokenBuffer
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

// com.fasterxml.jackson.databind.jsontype.TestDefaultForObject::testIssue352
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

// com.fasterxml.jackson.databind.jsontype.TestDefaultForObject::testFeature432
    public void testFeature432() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        mapper.enableDefaultTypingAsProperty(ObjectMapper.DefaultTyping.OBJECT_AND_NON_CONCRETE, "*CLASS*");
        String json = mapper.writeValueAsString(new BeanHolder(new StringBean("punny")));
        assertEquals("{\"bean\":{\"*CLASS*\":\"com.fasterxml.jackson.databind.jsontype.TestDefaultForObject$StringBean\",\"name\":\"punny\"}}", json);
    }

// com.fasterxml.jackson.databind.jsontype.TestDefaultForObject::testNoGoWithExternalProperty
    public void testNoGoWithExternalProperty() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        try {
            mapper.enableDefaultTyping(ObjectMapper.DefaultTyping.JAVA_LANG_OBJECT,
                    JsonTypeInfo.As.EXTERNAL_PROPERTY);
            fail("Should not have passed");
        } catch (IllegalArgumentException e) {
            verifyException(e, "Can not use includeAs of EXTERNAL_PROPERTY");
        }
    }

// com.fasterxml.jackson.databind.jsontype.TestDefaultForScalars::testNumericScalars
    public void testNumericScalars() throws Exception
    {
        ObjectMapper m = new ObjectMapper();
        m.enableDefaultTyping();

        
        assertEquals("[123]", m.writeValueAsString(new Object[] { Integer.valueOf(123) }));
        assertEquals("[[\"java.lang.Long\",37]]", m.writeValueAsString(new Object[] { Long.valueOf(37) }));
        assertEquals("[0.25]", m.writeValueAsString(new Object[] { Double.valueOf(0.25) }));
        assertEquals("[[\"java.lang.Float\",0.5]]", m.writeValueAsString(new Object[] { Float.valueOf(0.5f) }));
    }

// com.fasterxml.jackson.databind.jsontype.TestDefaultForScalars::testDateScalars
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

// com.fasterxml.jackson.databind.jsontype.TestDefaultForScalars::testMiscScalars
    public void testMiscScalars() throws Exception
    {
        ObjectMapper m = new ObjectMapper();
        m.enableDefaultTyping();

        
        assertEquals("[\"abc\"]", m.writeValueAsString(new Object[] { "abc" }));
        assertEquals("[true,null,false]", m.writeValueAsString(new Boolean[] { true, null, false }));
    }

// com.fasterxml.jackson.databind.jsontype.TestDefaultForScalars::testScalarArrays
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

// com.fasterxml.jackson.databind.jsontype.TestDefaultForScalars::test417
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

// com.fasterxml.jackson.databind.jsontype.TestDefaultForTreeNodes::testValueAsStringWithDefaultTyping
    public void testValueAsStringWithDefaultTyping() throws Exception
    {
        Foo foo = new Foo("baz");
        String json = DEFAULT_MAPPER.writeValueAsString(foo);

        JsonNode jsonNode = DEFAULT_MAPPER.readTree(json);
        assertEquals(jsonNode.get("bar").textValue(), foo.bar);
    }

// com.fasterxml.jackson.databind.jsontype.TestDefaultForTreeNodes::testValueToTreeWithDefaultTyping
    public void testValueToTreeWithDefaultTyping() throws Exception
    {
        Foo foo = new Foo("baz");
        JsonNode jsonNode = DEFAULT_MAPPER.valueToTree(foo);
        assertEquals(jsonNode.get("bar").textValue(), foo.bar);
    }

// com.fasterxml.jackson.databind.jsontype.TestDefaultWithCreators::testWithCreators
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

// com.fasterxml.jackson.databind.jsontype.TestDefaultWithCreators::testWithCreatorAndJsonValue
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

// com.fasterxml.jackson.databind.jsontype.TestEnumTyping::testTagList
    public void testTagList() throws Exception
    {
        ObjectMapper m = new ObjectMapper();
        TagList list = new TagList();
        list.add(Tag.A);
        list.add(Tag.B);
        String json = m.writeValueAsString(list);

        TagList result = m.readValue(json, TagList.class);
        assertEquals(2, result.size());
        assertSame(Tag.A, result.get(0));
        assertSame(Tag.B, result.get(1));
    }

// com.fasterxml.jackson.databind.jsontype.TestEnumTyping::testEnumInterface
    public void testEnumInterface() throws Exception
    {
        ObjectMapper m = new ObjectMapper();
        String json = m.writeValueAsString(Tag.B);
        EnumInterface result = m.readValue(json, EnumInterface.class);
        assertSame(Tag.B, result);
    }

// com.fasterxml.jackson.databind.jsontype.TestEnumTyping::testEnumInterfaceList
    public void testEnumInterfaceList() throws Exception
    {
        ObjectMapper m = new ObjectMapper();
        EnumInterfaceList list = new EnumInterfaceList();
        list.add(Tag.A);
        list.add(Tag.B);
        String json = m.writeValueAsString(list);
        
        EnumInterfaceList result = m.readValue(json, EnumInterfaceList.class);
        assertEquals(2, result.size());
        assertSame(Tag.A, result.get(0));
        assertSame(Tag.B, result.get(1));
    }

// com.fasterxml.jackson.databind.jsontype.TestEnumTyping::testUntypedEnum
    public void testUntypedEnum() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        String str = mapper.writeValueAsString(new UntypedEnumBean(TestEnum.B));
        UntypedEnumBean result = mapper.readValue(str, UntypedEnumBean.class);
        assertNotNull(result);
        assertNotNull(result.value);
        Object ob = result.value;
        assertSame(TestEnum.class, ob.getClass());
        assertEquals(TestEnum.B, result.value);
    }

// com.fasterxml.jackson.databind.jsontype.TestGenericListSerialization::testSubTypesFor356
    public void testSubTypesFor356() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        
        JSONResponse<List<Parent>> input = new JSONResponse<List<Parent>>();

        List<Parent> embedded = new ArrayList<Parent>();
        embedded.add(new Child1());
        embedded.add(new Child2());
        input.setResult(embedded);
        mapper.configure(MapperFeature.USE_STATIC_TYPING, true);

        JavaType rootType = TypeFactory.defaultInstance().constructType(new TypeReference<JSONResponse<List<Parent>>>() { });
        byte[] json = mapper.writerFor(rootType).writeValueAsBytes(input);
        
        JSONResponse<List<Parent>> out = mapper.readValue(json, 0, json.length, rootType);

        List<Parent> deserializedContent = out.getResult();

        assertEquals(2, deserializedContent.size());
        assertTrue(deserializedContent.get(0) instanceof Parent);
        assertTrue(deserializedContent.get(0) instanceof Child1);
        assertFalse(deserializedContent.get(0) instanceof Child2);
        assertTrue(deserializedContent.get(1) instanceof Child2);
        assertFalse(deserializedContent.get(1) instanceof Child1);

        assertEquals("PARENT", ((Child1) deserializedContent.get(0)).parentContent);
        assertEquals("PARENT", ((Child2) deserializedContent.get(1)).parentContent);
        assertEquals("CHILD1", ((Child1) deserializedContent.get(0)).childContent1);
        assertEquals("CHILD2", ((Child2) deserializedContent.get(1)).childContent2);
    }

// com.fasterxml.jackson.databind.jsontype.TestNoTypeInfo::testWithIdNone
    public void testWithIdNone() throws Exception
    {
        final ObjectMapper mapper = new ObjectMapper();
        mapper.enableDefaultTyping();
        
        String json = mapper.writeValueAsString(new NoType());
        assertEquals("{\"a\":3}", json);

        
        NoTypeInterface bean = mapper.readValue("{\"a\":6}", NoTypeInterface.class);
        assertNotNull(bean);
        NoType impl = (NoType) bean;
        assertEquals(6, impl.a);
    }

// com.fasterxml.jackson.databind.jsontype.TestOverlappingTypeIdNames::testOverlappingNameDeser
    public void testOverlappingNameDeser() throws Exception
    {
        Base312 value;

        

        value = MAPPER.readValue(aposToQuotes("{'type':'a','x':7}"), Base312.class);
        assertNotNull(value);
        assertEquals(Impl312.class, value.getClass());
        assertEquals(7, ((Impl312) value).x);
        
        value = MAPPER.readValue(aposToQuotes("{'type':'b','x':3}"), Base312.class);
        assertNotNull(value);
        assertEquals(Impl312.class, value.getClass());
        assertEquals(3, ((Impl312) value).x);
    }

// com.fasterxml.jackson.databind.jsontype.TestOverlappingTypeIdNames::testOverlappingNameSer
    public void testOverlappingNameSer() throws Exception
    {
        assertEquals(aposToQuotes("{'type':'a','value':1}"),
                MAPPER.writeValueAsString(new Impl312B1()));
        assertEquals(aposToQuotes("{'type':'a','value':1}"),
                MAPPER.writeValueAsString(new Impl312B2()));
    }

// com.fasterxml.jackson.databind.jsontype.TestPolymorphicWithDefaultImpl::testDeserializationWithObject
    public void testDeserializationWithObject() throws Exception
    {
        Inter inter = MAPPER.readerFor(Inter.class).readValue("{\"type\": \"mine\", \"blah\": [\"a\", \"b\", \"c\"]}");
        assertTrue(inter instanceof MyInter);
        assertFalse(inter instanceof LegacyInter);
        assertEquals(Arrays.asList("a", "b", "c"), ((MyInter) inter).blah);
    }

// com.fasterxml.jackson.databind.jsontype.TestPolymorphicWithDefaultImpl::testDeserializationWithString
    public void testDeserializationWithString() throws Exception
    {
        Inter inter = MAPPER.readerFor(Inter.class).readValue("\"a,b,c,d\"");
        assertTrue(inter instanceof LegacyInter);
        assertEquals(Arrays.asList("a", "b", "c", "d"), ((MyInter) inter).blah);
    }

// com.fasterxml.jackson.databind.jsontype.TestPolymorphicWithDefaultImpl::testDeserializationWithArray
    public void testDeserializationWithArray() throws Exception
    {
        Inter inter = MAPPER.readerFor(Inter.class).readValue("[\"a\", \"b\", \"c\", \"d\"]");
        assertTrue(inter instanceof LegacyInter);
        assertEquals(Arrays.asList("a", "b", "c", "d"), ((MyInter) inter).blah);
    }

// com.fasterxml.jackson.databind.jsontype.TestPolymorphicWithDefaultImpl::testDeserializationWithArrayOfSize2
    public void testDeserializationWithArrayOfSize2() throws Exception
    {
        Inter inter = MAPPER.readerFor(Inter.class).readValue("[\"a\", \"b\"]");
        assertTrue(inter instanceof LegacyInter);
        assertEquals(Arrays.asList("a", "b"), ((MyInter) inter).blah);
    }

// com.fasterxml.jackson.databind.jsontype.TestPolymorphicWithDefaultImpl::testDefaultAsNoClass
    public void testDefaultAsNoClass() throws Exception
    {
        Object ob = MAPPER.readerFor(DefaultWithNoClass.class).readValue("{ }");
        assertNull(ob);
        ob = MAPPER.readerFor(DefaultWithNoClass.class).readValue("{ \"bogus\":3 }");
        assertNull(ob);
    }

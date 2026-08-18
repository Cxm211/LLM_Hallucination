// buggy code
    protected JavaType _narrow(Class<?> subclass)
    {
        if (_class == subclass) {
            return this;
        }
        // Should we check that there is a sub-class relationship?
        // 15-Jan-2016, tatu: Almost yes, but there are some complications with
        //    placeholder values, so no.
        /*
        if (!_class.isAssignableFrom(subclass)) {
            throw new IllegalArgumentException("Class "+subclass.getName()+" not sub-type of "
                    +_class.getName());
        }
        */
        // 15-Jan-2015, tatu: Not correct; should really re-resolve...
        return new SimpleType(subclass, _bindings, _superClass, _superInterfaces,
                _valueHandler, _typeHandler, _asStatic);
    }

// relevant test
// com.fasterxml.jackson.databind.util.TestTokenBuffer::testWithJSONSampleDoc
    public void testWithJSONSampleDoc() throws Exception
    {
        
        JsonParser jp = createParserUsingReader(SAMPLE_DOC_JSON_SPEC);
        TokenBuffer tb = new TokenBuffer(null, false);
        while (jp.nextToken() != null) {
            tb.copyCurrentEvent(jp);
        }

        
        verifyJsonSpecSampleDoc(tb.asParser(), false);

        
        verifyJsonSpecSampleDoc(tb.asParser(), true);
        tb.close();
        jp.close();
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
        
        
        JsonParser jp = buf1.asParser();
        assertToken(JsonToken.START_OBJECT, jp.nextToken());
        assertToken(JsonToken.FIELD_NAME, jp.nextToken());
        assertEquals("a", jp.getCurrentName());
        assertToken(JsonToken.VALUE_TRUE, jp.nextToken());
        assertToken(JsonToken.FIELD_NAME, jp.nextToken());
        assertEquals("b", jp.getCurrentName());
        assertToken(JsonToken.VALUE_NUMBER_INT, jp.nextToken());
        assertEquals(13, jp.getIntValue());
        assertToken(JsonToken.END_OBJECT, jp.nextToken());
        jp.close();
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

            
            JsonParser jp = buf.asParser();
            assertEquals(JsonToken.VALUE_STRING, jp.nextToken());
            String str = jp.getText();
            assertEquals(value, str);
            jp.close();
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

// com.fasterxml.jackson.databind.util.TestTokenBuffer::testWithJsonParserSequenceSimple
    public void testWithJsonParserSequenceSimple() throws IOException
    {
        
        TokenBuffer buf = new TokenBuffer(null, false);
        buf.writeStartArray();
        buf.writeString("test");
        JsonParser jp = createParserUsingReader("[ true, null ]");
        
        JsonParserSequence seq = JsonParserSequence.createFlattened(buf.asParser(), jp);
        assertEquals(2, seq.containedParsersCount());

        assertFalse(jp.isClosed());
        
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

        
        assertTrue(jp.isClosed());
        jp.close();
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

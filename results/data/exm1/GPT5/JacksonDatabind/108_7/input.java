// buggy code
    public <T extends TreeNode> T readTree(JsonParser p) throws IOException {
        return (T) _bindAsTree(p);
    }

    protected final JsonNode _bindAsTree(JsonParser p) throws IOException
    {
        // Need to inline `_initForReading()` due to tree reading handling end-of-input specially
        _config.initialize(p);
        if (_schema != null) {
            p.setSchema(_schema);
        }

        JsonToken t = p.getCurrentToken();
        if (t == null) {
            t = p.nextToken();
            if (t == null) {
                return _config.getNodeFactory().missingNode();
            }
        }
        final JsonNode resultNode;
        if (t == JsonToken.VALUE_NULL) {
            resultNode = _config.getNodeFactory().nullNode();
        } else {
            final DeserializationContext ctxt = createDeserializationContext(p);
            final JsonDeserializer<Object> deser = _findTreeDeserializer(ctxt);
            if (_unwrapRoot) {
                resultNode = (JsonNode) _unwrapAndDeserialize(p, ctxt, JSON_NODE_TYPE, deser);
            } else {
                resultNode = (JsonNode) deser.deserialize(p, ctxt);
                if (_config.isEnabled(DeserializationFeature.FAIL_ON_TRAILING_TOKENS)) {
                    _verifyNoTrailingTokens(p, ctxt, JSON_NODE_TYPE);
                }
            }
        }
        return resultNode;
    }

// relevant test
// com.fasterxml.jackson.databind.util.TestStdDateFormat::testISO8601RegexpDateOnly
    public void testISO8601RegexpDateOnly() throws Exception
    {
        Pattern p = StdDateFormat.PATTERN_PLAIN;
        Matcher m = p.matcher("1997-07-16");
        assertTrue(m.matches());
        
    }

// com.fasterxml.jackson.databind.util.TestStdDateFormat::testISO8601RegexpFull
    public void testISO8601RegexpFull() throws Exception
    {
        
        final Pattern p = StdDateFormat.PATTERN_ISO8601;
        Matcher m;

        
        m = p.matcher("1997-07-16T19:20:00+01:00");
        assertTrue(m.matches());
        assertEquals(2, m.groupCount());
        assertNull(m.group(1)); 
        assertEquals("+01:00", m.group(2));

        
        m = p.matcher("1997-07-16T19:20:00Z");
        assertTrue(m.matches());
        assertNull(m.group(1));
        assertEquals("Z", m.group(2));

        
        m = p.matcher("1997-07-16T19:20+01:00");
        assertTrue(m.matches());
        assertNull(m.group(1));
        assertEquals("+01:00", m.group(2));

        
        m = p.matcher("1997-07-16T19:20:00.2+03:00");
        assertTrue(m.matches());
        assertEquals(2, m.groupCount());
        assertEquals(".2", m.group(1));
        assertEquals("+03:00", m.group(2));
        
        m = p.matcher("1972-12-28T00:00:00.01-0300");
        assertTrue(m.matches());
        assertEquals(".01", m.group(1));
        assertEquals("-0300", m.group(2));

        m = p.matcher("1972-12-28T00:00:00.400+00");
        assertTrue(m.matches());
        assertEquals(".400", m.group(1));
        assertEquals("+00", m.group(2));

        
        m = p.matcher("1972-12-28T04:15");
        assertTrue(m.matches());
        assertNull(m.group(1));
        assertNull(m.group(2));
    }

// com.fasterxml.jackson.databind.util.TestStdDateFormat::testLenientParsing
    public void testLenientParsing() throws Exception
    {
        StdDateFormat f = StdDateFormat.instance.clone();
        f.setLenient(false);

        
        Date dt = f.parse("2015-11-30");
        assertNotNull(dt);

        
        try {
            f.parse("2015-11-32");
            fail("Should not pass");
        } catch (ParseException e) {
            verifyException(e, "Cannot parse date");
        }

        
        f.setLenient(true);
        dt = f.parse("2015-11-32");
        assertNotNull(dt);
    }

// com.fasterxml.jackson.databind.util.TestStdDateFormat::testInvalid
    public void testInvalid() {
        StdDateFormat std = new StdDateFormat();
        try {
            std.parse("foobar");
        } catch (java.text.ParseException e) {
            verifyException(e, "Cannot parse");
        }
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

        assertFalse(buf.isEnabled(JsonGenerator.Feature.WRITE_BIGDECIMAL_AS_PLAIN));
        buf.enable(JsonGenerator.Feature.WRITE_BIGDECIMAL_AS_PLAIN);
        assertTrue(buf.isEnabled(JsonGenerator.Feature.WRITE_BIGDECIMAL_AS_PLAIN));
        buf.disable(JsonGenerator.Feature.WRITE_BIGDECIMAL_AS_PLAIN);
        assertFalse(buf.isEnabled(JsonGenerator.Feature.WRITE_BIGDECIMAL_AS_PLAIN));

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

// com.fasterxml.jackson.databind.util.TestTokenBuffer::testNumberOverflowInt
    public void testNumberOverflowInt() throws IOException
    {
        try (TokenBuffer buf = new TokenBuffer(null, false)) {
            long big = 1L + Integer.MAX_VALUE;
            buf.writeNumber(big);
            try (JsonParser p = buf.asParser()) {
                assertToken(JsonToken.VALUE_NUMBER_INT, p.nextToken());
                assertEquals(NumberType.LONG, p.getNumberType());
                try {
                    p.getIntValue();
                    fail("Expected failure for `int` overflow");
                } catch (InputCoercionException e) {
                    verifyException(e, "Numeric value ("+big+") out of range of int");
                }
            }
        }
        
        try (TokenBuffer buf = new TokenBuffer(null, false)) {
            long big = 1L + Integer.MAX_VALUE;
            buf.writeNumber(String.valueOf(big));
            try (JsonParser p = buf.asParser()) {
                
                assertToken(JsonToken.VALUE_NUMBER_FLOAT, p.nextToken());
                try {
                    p.getIntValue();
                    fail("Expected failure for `int` overflow");
                } catch (InputCoercionException e) {
                    verifyException(e, "Numeric value ("+big+") out of range of int");
                }
            }
        }
    }

// com.fasterxml.jackson.databind.util.TestTokenBuffer::testNumberOverflowLong
    public void testNumberOverflowLong() throws IOException
    {
        try (TokenBuffer buf = new TokenBuffer(null, false)) {
            BigInteger big = BigInteger.valueOf(Long.MAX_VALUE).add(BigInteger.ONE);
            buf.writeNumber(big);
            try (JsonParser p = buf.asParser()) {
                assertToken(JsonToken.VALUE_NUMBER_INT, p.nextToken());
                assertEquals(NumberType.BIG_INTEGER, p.getNumberType());
                try {
                    p.getLongValue();
                    fail("Expected failure for `long` overflow");
                } catch (InputCoercionException e) {
                    verifyException(e, "Numeric value ("+big+") out of range of long");
                }
            }
        }
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

// com.fasterxml.jackson.databind.util.TestTokenBuffer::testEmbeddedObjectCoerceCheck
    public void testEmbeddedObjectCoerceCheck() throws Exception
    {
        TokenBuffer buf = new TokenBuffer(null, false);
        Object inputPojo = new Sub1730();
        buf.writeEmbeddedObject(inputPojo);

        
        JsonParser p = buf.asParser();
        Base1730 out = MAPPER.readValue(p, Base1730.class);

        assertSame(inputPojo, out);
        p.close();
        buf.close();
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

        ObjectMapper myMapper = objectMapperBuilder()
                .disable(MapperFeature.DEFAULT_VIEW_INCLUSION)
                .build();

        
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

        
        ObjectMapper mapper = objectMapperBuilder()
                .configure(MapperFeature.DEFAULT_VIEW_INCLUSION, false)
                .build();

        
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
        ObjectMapper mapper = createMapper();
        String result = serializeWithObjectMapper(new ComplexTestData( ), Views.View.class, mapper);
        assertEquals(-1, result.indexOf( "nameHidden" ));
    }

// com.fasterxml.jackson.databind.views.TestViewsSerialization2::testDataBindingUsageWithoutView
    public void testDataBindingUsageWithoutView( ) throws Exception
    {
        ObjectMapper mapper = createMapper();
        String json = serializeWithObjectMapper(new ComplexTestData( ), null, mapper);
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

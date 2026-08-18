// buggy code
    public int writeValue() {
        // Most likely, object:
        if (_type == TYPE_OBJECT) {
            _gotName = false;
            ++_index;
            return STATUS_OK_AFTER_COLON;
        }

        // Ok, array?
        if (_type == TYPE_ARRAY) {
            int ix = _index;
            ++_index;
            return (ix < 0) ? STATUS_OK_AS_IS : STATUS_OK_AFTER_COMMA;
        }
        
        // Nope, root context
        // No commas within root context, but need space
        ++_index;
        return (_index == 0) ? STATUS_OK_AS_IS : STATUS_OK_AFTER_SPACE;
    }

// relevant test
// com.fasterxml.jackson.core.TestJDKSerializability::testJsonFactorySerializable
    public void testJsonFactorySerializable() throws Exception
    {
        JsonFactory f = new JsonFactory();
        String origJson = "{\"simple\":[1,true,{}]}";
        assertEquals(origJson, _copyJson(f, origJson, false));

        
        byte[] frozen = jdkSerialize(f);
        JsonFactory f2 = jdkDeserialize(frozen);
        assertNotNull(f2);
        assertEquals(origJson, _copyJson(f2, origJson, false));

        
        assertEquals(origJson, _copyJson(f2, origJson, true));
    }

// com.fasterxml.jackson.core.TestJDKSerializability::testBase64Variant
    public void testBase64Variant() throws Exception
    {
        Base64Variant orig = Base64Variants.PEM;
        byte[] stuff = jdkSerialize(orig);
        Base64Variant back = jdkDeserialize(stuff);
        assertSame(orig, back);
    }

// com.fasterxml.jackson.core.TestJDKSerializability::testPrettyPrinter
    public void testPrettyPrinter() throws Exception
    {
        PrettyPrinter p = new DefaultPrettyPrinter();
        byte[] stuff = jdkSerialize(p);
        PrettyPrinter back = jdkDeserialize(stuff);
        
        assertNotNull(back);
    }

// com.fasterxml.jackson.core.TestVersions::testCoreVersions
    public void testCoreVersions() throws Exception
    {
        assertVersion(new JsonFactory().version());
        ReaderBasedJsonParser jp = new ReaderBasedJsonParser(getIOContext(), 0, null, null,
                CharsToNameCanonicalizer.createRoot());
        assertVersion(jp.version());
        jp.close();
        JsonGenerator jgen = new WriterBasedJsonGenerator(getIOContext(), 0, null, null);
        assertVersion(jgen.version());
        jgen.close();
    }

// com.fasterxml.jackson.core.base64.TestBase64Generation::testStreamingWrites
    public void testStreamingWrites() throws Exception
    {
        final JsonFactory f = new JsonFactory();
        _testStreamingWrites(f, true);
        _testStreamingWrites(f, false);
    }

// com.fasterxml.jackson.core.base64.TestBase64Generation::testIssue55
    public void testIssue55() throws Exception
    {
        final JsonFactory f = new JsonFactory();

        
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();

        JsonGenerator gen = f.createGenerator(bytes);
        ByteArrayInputStream data = new ByteArrayInputStream(new byte[2000]);
        gen.writeBinary(data, 1999);       
        gen.close();

        final int EXP_LEN = 2670;
        
        assertEquals(EXP_LEN, bytes.size());

        
        StringWriter sw = new StringWriter();
        
        gen = f.createGenerator(sw);
        data = new ByteArrayInputStream(new byte[2000]);
        gen.writeBinary(data, 1999);       
        gen.close();
        
        assertEquals(EXP_LEN, sw.toString().length());
    }

// com.fasterxml.jackson.core.base64.TestBase64Parsing::testBase64UsingInputStream
    public void testBase64UsingInputStream() throws Exception
    {
        _testBase64Text(true);
    }

// com.fasterxml.jackson.core.base64.TestBase64Parsing::testBase64UsingReader
    public void testBase64UsingReader() throws Exception
    {
        _testBase64Text(false);
    }

// com.fasterxml.jackson.core.base64.TestBase64Parsing::testStreaming
    public void testStreaming() throws IOException
    {
        _testStreaming(false);
        _testStreaming(true);
    }

// com.fasterxml.jackson.core.base64.TestJsonParserBinary::testSimple
    public void testSimple()
        throws IOException
    {
        
        _testSimple(false);
        _testSimple(true);
    }

// com.fasterxml.jackson.core.base64.TestJsonParserBinary::testInArray
    public void testInArray()
        throws IOException
    {
        
        _testInArray(false);
        _testInArray(true);
    }

// com.fasterxml.jackson.core.base64.TestJsonParserBinary::testWithEscaped
    public void testWithEscaped() throws IOException
    {
        
        _testEscaped(false);
        _testEscaped(true);
    }

// com.fasterxml.jackson.core.io.TestJsonStringEncoder::testQuoteAsString
    public void testQuoteAsString() throws Exception
    {
        JsonStringEncoder encoder = new JsonStringEncoder();
        char[] result = encoder.quoteAsString("foobar");
        assertArrayEquals("foobar".toCharArray(), result);
        result = encoder.quoteAsString("\"x\"");
        assertArrayEquals("\\\"x\\\"".toCharArray(), result);
    }

// com.fasterxml.jackson.core.io.TestJsonStringEncoder::testQuoteLongAsString
    public void testQuoteLongAsString() throws Exception
    {
        JsonStringEncoder encoder = new JsonStringEncoder();
        StringBuilder sb = new StringBuilder();
        StringBuilder sb2 = new StringBuilder();
        for (int i = 0; i < 1111; ++i) {
            sb.append('"');
            sb2.append("\\\"");
        }
        String input = sb.toString();
        String exp = sb2.toString();
        char[] result = encoder.quoteAsString(input);
        assertEquals(2*input.length(), result.length);
        assertEquals(exp, new String(result));
        
    }

// com.fasterxml.jackson.core.io.TestJsonStringEncoder::testQuoteAsUTF8
    public void testQuoteAsUTF8() throws Exception
    {
        
        JsonFactory f = new JsonFactory();
        JsonStringEncoder encoder = new JsonStringEncoder();
        int[] lengths = new int[] {
            5, 19, 200, 7000, 21000, 37000
        };
        for (int length : lengths) {
            String str = generateRandom(length);
            StringWriter sw = new StringWriter(length*2);
            JsonGenerator jgen = f.createGenerator(sw);
            jgen.writeString(str);
            jgen.close();
            String encoded = sw.toString();
            
            encoded = encoded.substring(1, encoded.length() - 1);
            byte[] expected = encoded.getBytes("UTF-8");
            byte[] actual = encoder.quoteAsUTF8(str);
            assertArrayEquals(expected, actual);
        }
    }

// com.fasterxml.jackson.core.io.TestJsonStringEncoder::testEncodeAsUTF8
    public void testEncodeAsUTF8() throws Exception
    {
        JsonStringEncoder encoder = new JsonStringEncoder();
        String[] strings = new String[] {
                "a", "foobar", "p\u00f6ll\u00f6", "\"foo\"",
                generateRandom(200),
                generateRandom(5000),
                generateRandom(39000)
        };
        for (String str : strings) {
            assertArrayEquals(str.getBytes("UTF-8"), encoder.encodeAsUTF8(str));
        }
    }

// com.fasterxml.jackson.core.io.TestJsonStringEncoder::testCtrlChars
    public void testCtrlChars() throws Exception
    {
        char[] input = new char[] { 0, 1, 2, 3, 4 };
        char[] quoted = JsonStringEncoder.getInstance().quoteAsString(new String(input));
        assertEquals("\\u0000\\u0001\\u0002\\u0003\\u0004", new String(quoted));
    }

// com.fasterxml.jackson.core.json.GeneratorFailTest::testDupFieldNameWrites
    public void testDupFieldNameWrites() throws Exception
    {
        _testDupFieldNameWrites(F, false);
        _testDupFieldNameWrites(F, true);        
    }

// com.fasterxml.jackson.core.json.GeneratorFailTest::testFailOnWritingStringNotFieldNameBytes
    public void testFailOnWritingStringNotFieldNameBytes() throws Exception {
        _testFailOnWritingStringNotFieldName(F, false);
    }

// com.fasterxml.jackson.core.json.GeneratorFailTest::testFailOnWritingStringNotFieldNameChars
    public void testFailOnWritingStringNotFieldNameChars() throws Exception {
        _testFailOnWritingStringNotFieldName(F, true);        
    }

// com.fasterxml.jackson.core.json.TestCharEscaping::testMissingEscaping
    public void testMissingEscaping()
        throws Exception
    {
        
        final String DOC = "["
            +"\"Linefeed: \n.\""
            +"]";
        JsonParser jp = createParserUsingReader(DOC);
        assertToken(JsonToken.START_ARRAY, jp.nextToken());
        try {
            
            JsonToken t = jp.nextToken();
            assertToken(JsonToken.VALUE_STRING, t);
            
            jp.getText();
            fail("Expected an exception for un-escaped linefeed in string value");
        } catch (JsonParseException jex) {
            verifyException(jex, "has to be escaped");
        }
        jp.close();
    }

// com.fasterxml.jackson.core.json.TestCharEscaping::testSimpleEscaping
    public void testSimpleEscaping()
        throws Exception
    {
        String DOC = "["
            +"\"LF=\\n\""
            +"]";

        JsonParser jp = createParserUsingReader(DOC);
        assertToken(JsonToken.START_ARRAY, jp.nextToken());
        assertToken(JsonToken.VALUE_STRING, jp.nextToken());
        assertEquals("LF=\n", jp.getText());
        jp.close();

        
        DOC = "[\"NULL:\\u0000!\"]";

        jp = createParserUsingReader(DOC);
        assertToken(JsonToken.START_ARRAY, jp.nextToken());
        assertToken(JsonToken.VALUE_STRING, jp.nextToken());
        assertEquals("NULL:\0!", jp.getText());
        jp.close();

        
        jp = createParserUsingReader("[\"\\u0123\"]");
        assertToken(JsonToken.START_ARRAY, jp.nextToken());
        assertToken(JsonToken.VALUE_STRING, jp.nextToken());
        assertEquals("\u0123", jp.getText());
        jp.close();

        
        jp = createParserUsingReader("[\"\\u0041\\u0043\"]");
        assertToken(JsonToken.START_ARRAY, jp.nextToken());
        assertToken(JsonToken.VALUE_STRING, jp.nextToken());
        assertEquals("AC", jp.getText());
        jp.close();
    }

// com.fasterxml.jackson.core.json.TestCharEscaping::testInvalid
    public void testInvalid()
        throws Exception
    {
        
        String DOC = "[\"\\u41=A\"]";
        JsonParser jp = createParserUsingReader(DOC);
        assertToken(JsonToken.START_ARRAY, jp.nextToken());
        try {
            jp.nextToken();
            jp.getText();
            fail("Expected an exception for unclosed ARRAY");
        } catch (JsonParseException jpe) {
            verifyException(jpe, "for character escape");
        }
        jp.close();
    }

// com.fasterxml.jackson.core.json.TestCharEscaping::test8DigitSequence
    public void test8DigitSequence()
        throws Exception
    {
        String DOC = "[\"\\u00411234\"]";
        JsonParser jp = createParserUsingReader(DOC);
        assertToken(JsonToken.START_ARRAY, jp.nextToken());
        assertToken(JsonToken.VALUE_STRING, jp.nextToken());
        assertEquals("A1234", jp.getText());
        jp.close();
    }

// com.fasterxml.jackson.core.json.TestCharEscaping::testWriteLongCustomEscapes
    public void testWriteLongCustomEscapes() throws Exception
    {
        JsonFactory jf = new JsonFactory();
        jf.setCharacterEscapes(ESC_627); 
        StringBuilder longString = new StringBuilder();
        while (longString.length() < 2000) {
          longString.append("\u65e5\u672c\u8a9e");
        }

        StringWriter writer = new StringWriter();
        
        JsonGenerator jgen = jf.createGenerator(writer);
        jgen.setHighestNonEscapedChar(127); 
        jgen.writeString(longString.toString());
        jgen.close();
    }

// com.fasterxml.jackson.core.json.TestCharEscaping::testEscapesForCharArrays
    public void testEscapesForCharArrays() throws Exception {
        JsonFactory jf = new JsonFactory();
        StringWriter writer = new StringWriter();
        JsonGenerator jgen = jf.createGenerator(writer);
        
        jgen.writeString(new char[] { '\0' }, 0, 1);
        jgen.close();
        assertEquals("\"\\u0000\"", writer.toString());
    }

// com.fasterxml.jackson.core.json.TestCustomEscaping::testAboveAsciiEscapeWithReader
    public void testAboveAsciiEscapeWithReader() throws Exception
    {
        _testEscapeAboveAscii(false); 
    }

// com.fasterxml.jackson.core.json.TestCustomEscaping::testAboveAsciiEscapeWithUTF8Stream
    public void testAboveAsciiEscapeWithUTF8Stream() throws Exception
    {
        _testEscapeAboveAscii(true); 
    }

// com.fasterxml.jackson.core.json.TestCustomEscaping::testEscapeCustomWithReader
    public void testEscapeCustomWithReader() throws Exception
    {
        _testEscapeCustom(false); 
    }

// com.fasterxml.jackson.core.json.TestCustomEscaping::testEscapeCustomWithUTF8Stream
    public void testEscapeCustomWithUTF8Stream() throws Exception
    {
        _testEscapeCustom(true); 
    }

// com.fasterxml.jackson.core.json.TestDecorators::testInputDecoration
    public void testInputDecoration() throws IOException
    {
        JsonFactory f = new JsonFactory();
        f.setInputDecorator(new SimpleInputDecorator());
        JsonParser jp;
        
        jp = f.createParser(new StringReader("{ }"));
        
        assertEquals(JsonToken.VALUE_NUMBER_INT, jp.nextToken());
        assertEquals(789, jp.getIntValue());
        jp.close();

        
        jp = f.createParser(new ByteArrayInputStream("[ ]".getBytes("UTF-8")));
        
        assertEquals(JsonToken.VALUE_NUMBER_INT, jp.nextToken());
        assertEquals(123, jp.getIntValue());
        jp.close();

        
        jp = f.createParser("[ ]".getBytes("UTF-8"));
        
        assertEquals(JsonToken.VALUE_NUMBER_INT, jp.nextToken());
        assertEquals(456, jp.getIntValue());
        jp.close();
    }

// com.fasterxml.jackson.core.json.TestDecorators::testOutputDecoration
    public void testOutputDecoration() throws IOException
    {
        JsonFactory f = new JsonFactory();
        f.setOutputDecorator(new SimpleOutputDecorator());
        JsonGenerator jg;

        StringWriter sw = new StringWriter();
        jg = f.createGenerator(sw);
        jg.close();
        assertEquals("567", sw.toString());

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        jg = f.createGenerator(out, JsonEncoding.UTF8);
        jg.close();
        assertEquals("123", out.toString("UTF-8"));
    }

// com.fasterxml.jackson.core.json.TestGeneratorDupHandling::testSimpleDupsEagerlyBytes
    public void testSimpleDupsEagerlyBytes() throws Exception {
        _testSimpleDups(true, false, new JsonFactory());
    }

// com.fasterxml.jackson.core.json.TestGeneratorDupHandling::testSimpleDupsEagerlyChars
    public void testSimpleDupsEagerlyChars() throws Exception {
        _testSimpleDups(false, false, new JsonFactory());
    }

// com.fasterxml.jackson.core.json.TestGeneratorDupHandling::testSimpleDupsLazilyBytes
    public void testSimpleDupsLazilyBytes() throws Exception {
        final JsonFactory f = new JsonFactory();
        assertFalse(f.isEnabled(JsonGenerator.Feature.STRICT_DUPLICATE_DETECTION));
        _testSimpleDups(true, true, f);
    }

// com.fasterxml.jackson.core.json.TestGeneratorDupHandling::testSimpleDupsLazilyChars
    public void testSimpleDupsLazilyChars() throws Exception {
        final JsonFactory f = new JsonFactory();
        _testSimpleDups(false, true, f);
    }

// com.fasterxml.jackson.core.json.TestGeneratorWithSerializedString::testSimple
    public void testSimple() throws Exception
    {
        JsonFactory jf = new JsonFactory();

        
        StringWriter sw = new StringWriter();
        JsonGenerator jgen = jf.createGenerator(sw);
        _writeSimple(jgen);
        jgen.close();
        String json = sw.toString();
        _verifySimple(jf.createParser(json));

        
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        jgen = jf.createGenerator(out, JsonEncoding.UTF8);
        _writeSimple(jgen);
        jgen.close();
        byte[] jsonB = out.toByteArray();
        _verifySimple(jf.createParser(jsonB));
    }

// com.fasterxml.jackson.core.json.TestJsonFactory::testGeneratorFeatures
    public void testGeneratorFeatures() throws Exception
    {
        JsonFactory f = new JsonFactory();
        assertNull(f.getCodec());

        f.configure(JsonGenerator.Feature.QUOTE_FIELD_NAMES, true);
        assertTrue(f.isEnabled(JsonGenerator.Feature.QUOTE_FIELD_NAMES));
        f.configure(JsonGenerator.Feature.QUOTE_FIELD_NAMES, false);
        assertFalse(f.isEnabled(JsonGenerator.Feature.QUOTE_FIELD_NAMES));
    }

// com.fasterxml.jackson.core.json.TestJsonFactory::testParserFeatures
    public void testParserFeatures() throws Exception
    {
        JsonFactory f = new JsonFactory();
        assertNull(f.getCodec());

        f.configure(JsonFactory.Feature.INTERN_FIELD_NAMES, true);
        assertTrue(f.isEnabled(JsonFactory.Feature.INTERN_FIELD_NAMES));
        f.configure(JsonFactory.Feature.INTERN_FIELD_NAMES, false);
        assertFalse(f.isEnabled(JsonFactory.Feature.INTERN_FIELD_NAMES));
    }

// com.fasterxml.jackson.core.json.TestJsonFactory::testJsonWithFiles
    public void testJsonWithFiles() throws Exception
    {
        File file = File.createTempFile("jackson-test", null);
        file.deleteOnExit();
        
        JsonFactory f = new JsonFactory();

        
        JsonGenerator jg = f.createGenerator(file, JsonEncoding.UTF16_LE);
        jg.writeStartObject();
        jg.writeRaw("   ");
        jg.writeEndObject();
        jg.close();

        
        JsonParser jp = f.createParser(file);
        assertToken(JsonToken.START_OBJECT, jp.nextToken());
        assertToken(JsonToken.END_OBJECT, jp.nextToken());
        assertNull(jp.nextToken());
        jp.close();

        
        jp = f.createParser(file.toURI().toURL());
        assertToken(JsonToken.START_OBJECT, jp.nextToken());
        assertToken(JsonToken.END_OBJECT, jp.nextToken());
        assertNull(jp.nextToken());
        jp.close();

        
        file.delete();
    }

// com.fasterxml.jackson.core.json.TestJsonFactory::testCopy
    public void testCopy() throws Exception
    {
        JsonFactory jf = new JsonFactory();
        
        assertTrue(jf.isEnabled(JsonFactory.Feature.INTERN_FIELD_NAMES));
        assertFalse(jf.isEnabled(JsonParser.Feature.ALLOW_COMMENTS));
        assertFalse(jf.isEnabled(JsonGenerator.Feature.ESCAPE_NON_ASCII));
        jf.disable(JsonFactory.Feature.INTERN_FIELD_NAMES);
        jf.enable(JsonParser.Feature.ALLOW_COMMENTS);
        jf.enable(JsonGenerator.Feature.ESCAPE_NON_ASCII);
        
        assertFalse(jf.isEnabled(JsonFactory.Feature.INTERN_FIELD_NAMES));
        assertTrue(jf.isEnabled(JsonParser.Feature.ALLOW_COMMENTS));
        assertTrue(jf.isEnabled(JsonGenerator.Feature.ESCAPE_NON_ASCII));

        JsonFactory jf2 = jf.copy();
        assertFalse(jf2.isEnabled(JsonFactory.Feature.INTERN_FIELD_NAMES));
        assertTrue(jf.isEnabled(JsonParser.Feature.ALLOW_COMMENTS));
        assertTrue(jf.isEnabled(JsonGenerator.Feature.ESCAPE_NON_ASCII));
    }

// com.fasterxml.jackson.core.json.TestJsonGenerator::testStringWrite
    public void testStringWrite() throws Exception
    {
        JsonFactory jf = new JsonFactory();
        String[] inputStrings = new String[] { "", "X", "1234567890" };
        for (int useReader = 0; useReader < 2; ++useReader) {
            for (int writeString = 0; writeString < 2; ++writeString) {
                for (int strIx = 0; strIx < inputStrings.length; ++strIx) {
                    String input = inputStrings[strIx];
                    JsonGenerator gen;
                    ByteArrayOutputStream bout = new ByteArrayOutputStream();
                    if (useReader != 0) {
                        gen = jf.createGenerator(new OutputStreamWriter(bout, "UTF-8"));
                    } else {
                        gen = jf.createGenerator(bout, JsonEncoding.UTF8);
                    }
                    if (writeString > 0) {
                        gen.writeString(input);
                    } else {
                        int len = input.length();
                        char[] buffer = new char[len + 20];
                        
                        input.getChars(0, len, buffer, strIx);
                        gen.writeString(buffer, strIx, len);
                    }
                    gen.flush();
                    gen.close();
                    JsonParser jp = jf.createParser(new ByteArrayInputStream(bout.toByteArray()));
                
                    JsonToken t = jp.nextToken();
                    assertNotNull("Document \""+bout.toString("UTF-8")+"\" yielded no tokens", t);
                    assertEquals(JsonToken.VALUE_STRING, t);
                    assertEquals(input, jp.getText());
                    assertEquals(null, jp.nextToken());
                    jp.close();
                }
            }
        }
    }

// com.fasterxml.jackson.core.json.TestJsonGenerator::testIntWrite
    public void testIntWrite() throws Exception
    {
        doTestIntWrite(false);
        doTestIntWrite(true);
    }

// com.fasterxml.jackson.core.json.TestJsonGenerator::testLongWrite
    public void testLongWrite() throws Exception
    {
        doTestLongWrite(false);
        doTestLongWrite(true);
    }

// com.fasterxml.jackson.core.json.TestJsonGenerator::testBooleanWrite
    public void testBooleanWrite() throws Exception
    {
        for (int i = 0; i < 4; ++i) {
            boolean state = (i & 1) == 0;
            boolean pad = (i & 2) == 0;
            StringWriter sw = new StringWriter();
            JsonGenerator gen = new JsonFactory().createGenerator(sw);
            gen.writeBoolean(state);
            if (pad) {
                gen.writeRaw(" ");
            }
            gen.close();
            String docStr = sw.toString();
            JsonParser jp = createParserUsingReader(docStr);
            JsonToken t = jp.nextToken();
            String exp = Boolean.valueOf(state).toString();
            if (!exp.equals(jp.getText())) {
                fail("Expected '"+exp+"', got '"+jp.getText());
            }
            assertEquals(state ? JsonToken.VALUE_TRUE : JsonToken.VALUE_FALSE, t);
            assertEquals(null, jp.nextToken());
            jp.close();
        }
    }

// com.fasterxml.jackson.core.json.TestJsonGenerator::testNullWrite
    public void testNullWrite()
        throws Exception
    {
        for (int i = 0; i < 2; ++i) {
            boolean pad = (i & 1) == 0;
            StringWriter sw = new StringWriter();
            JsonGenerator gen = new JsonFactory().createGenerator(sw);
            gen.writeNull();
            if (pad) {
                gen.writeRaw(" ");
            }
            gen.close();
            String docStr = sw.toString();
            JsonParser jp = createParserUsingReader(docStr);
            JsonToken t = jp.nextToken();
            String exp = "null";
            if (!exp.equals(jp.getText())) {
                fail("Expected '"+exp+"', got '"+jp.getText());
            }
            assertEquals(JsonToken.VALUE_NULL, t);
            assertEquals(null, jp.nextToken());
            jp.close();
        }
    }

// com.fasterxml.jackson.core.json.TestJsonGenerator::testRootIntsWrite
     public void testRootIntsWrite()
         throws Exception
     {
         StringWriter sw = new StringWriter();
         JsonGenerator gen = new JsonFactory().createGenerator(sw);
         gen.writeNumber(1);
         gen.writeNumber(2);
         gen.writeNumber(-13);
         gen.close();

         String docStr = sw.toString();

         JsonParser jp = createParserUsingReader(docStr);
         assertEquals(JsonToken.VALUE_NUMBER_INT, jp.nextToken());
         assertEquals(1, jp.getIntValue());
         assertEquals(JsonToken.VALUE_NUMBER_INT, jp.nextToken());
         assertEquals(2, jp.getIntValue());
         assertEquals(JsonToken.VALUE_NUMBER_INT, jp.nextToken());
         assertEquals(-13, jp.getIntValue());
         jp.close();
     }

// com.fasterxml.jackson.core.json.TestJsonGenerator::testFieldValueWrites
    public void testFieldValueWrites()
         throws Exception
     {
         StringWriter sw = new StringWriter();
         JsonGenerator gen = new JsonFactory().createGenerator(sw);
         gen.writeStartObject();
         gen.writeNumberField("long", 3L);
         gen.writeNumberField("double", 0.25);
         gen.writeNumberField("float", -0.25f);
         gen.writeEndObject();
         gen.close();

         assertEquals("{\"long\":3,\"double\":0.25,\"float\":-0.25}", sw.toString().trim());
     }

// com.fasterxml.jackson.core.json.TestJsonGenerator::testOutputContext
    public void testOutputContext() throws Exception
    {
        StringWriter sw = new StringWriter();
        JsonGenerator gen = new JsonFactory().createGenerator(sw);
        JsonStreamContext ctxt = gen.getOutputContext();
        assertTrue(ctxt.inRoot());

        gen.writeStartObject();
        assertTrue(gen.getOutputContext().inObject());

        gen.writeFieldName("a");
        assertEquals("a", gen.getOutputContext().getCurrentName());

        gen.writeStartArray();
        assertTrue(gen.getOutputContext().inArray());

        gen.writeStartObject();
        assertTrue(gen.getOutputContext().inObject());

        gen.writeFieldName("b");
        ctxt = gen.getOutputContext();
        assertEquals("b", ctxt.getCurrentName());
        gen.writeNumber(123);
        assertEquals("b", ctxt.getCurrentName());

        gen.writeFieldName("c");
        assertEquals("c", gen.getOutputContext().getCurrentName());
        gen.writeNumber(5);

        gen.writeFieldName("d");
        assertEquals("d", gen.getOutputContext().getCurrentName());

        gen.writeStartArray();
        ctxt = gen.getOutputContext();
        assertTrue(ctxt.inArray());
        assertEquals(0, ctxt.getCurrentIndex());
        assertEquals(0, ctxt.getEntryCount());

        gen.writeBoolean(true);
        ctxt = gen.getOutputContext();
        assertTrue(ctxt.inArray());
        
        assertEquals(0, ctxt.getCurrentIndex());
        assertEquals(1, ctxt.getEntryCount());

        gen.writeNumber(3);
        ctxt = gen.getOutputContext();
        assertTrue(ctxt.inArray());
        assertEquals(1, ctxt.getCurrentIndex());
        assertEquals(2, ctxt.getEntryCount());
        
        gen.writeEndArray();
        assertTrue(gen.getOutputContext().inObject());
        
        gen.writeEndObject();
        assertTrue(gen.getOutputContext().inArray());

        gen.writeEndArray();
        assertTrue(gen.getOutputContext().inObject());

        gen.writeEndObject();

        assertTrue(gen.getOutputContext().inRoot());
        
        gen.close();
    }

// com.fasterxml.jackson.core.json.TestJsonGeneratorFeatures::testConfigDefaults
    public void testConfigDefaults() throws IOException
    {
        JsonFactory jf = new JsonFactory();
        JsonGenerator jg = jf.createGenerator(new StringWriter());
        assertFalse(jg.isEnabled(JsonGenerator.Feature.WRITE_NUMBERS_AS_STRINGS));
        assertFalse(jg.isEnabled(JsonGenerator.Feature.WRITE_BIGDECIMAL_AS_PLAIN));
        jg.close();
    }

// com.fasterxml.jackson.core.json.TestJsonGeneratorFeatures::testFieldNameQuoting
    public void testFieldNameQuoting() throws IOException
    {
        JsonFactory jf = new JsonFactory();
        
        _testFieldNameQuoting(jf, true);
        
        jf.disable(JsonGenerator.Feature.QUOTE_FIELD_NAMES);
        _testFieldNameQuoting(jf, false);
        
        jf.enable(JsonGenerator.Feature.QUOTE_FIELD_NAMES);
        _testFieldNameQuoting(jf, true);
    }

// com.fasterxml.jackson.core.json.TestJsonGeneratorFeatures::testNonNumericQuoting
    public void testNonNumericQuoting()
        throws IOException
    {
        JsonFactory jf = new JsonFactory();
        
        _testNonNumericQuoting(jf, true);
        
        jf.disable(JsonGenerator.Feature.QUOTE_NON_NUMERIC_NUMBERS);
        _testNonNumericQuoting(jf, false);
        
        jf.enable(JsonGenerator.Feature.QUOTE_NON_NUMERIC_NUMBERS);
        _testNonNumericQuoting(jf, true);
    }

// com.fasterxml.jackson.core.json.TestJsonGeneratorFeatures::testNumbersAsJSONStrings
    public void testNumbersAsJSONStrings() throws IOException
    {
        JsonFactory jf = new JsonFactory();
        
        assertEquals("[1,2,1.25,2.25,3001,0.5,-1]", _writeNumbers(jf));        

        
        jf.configure(JsonGenerator.Feature.WRITE_NUMBERS_AS_STRINGS, true);
        assertEquals("[\"1\",\"2\",\"1.25\",\"2.25\",\"3001\",\"0.5\",\"-1\"]",
                     _writeNumbers(jf));
    }

// com.fasterxml.jackson.core.json.TestJsonGeneratorFeatures::testBigDecimalAsPlain
    public void testBigDecimalAsPlain() throws IOException
    {
        JsonFactory jf = new JsonFactory();
        BigDecimal ENG = new BigDecimal("1E+2");

        StringWriter sw = new StringWriter();
        JsonGenerator jg = jf.createGenerator(sw);
        jg.writeNumber(ENG);
        jg.close();
        assertEquals("1E+2", sw.toString());

        jf.configure(JsonGenerator.Feature.WRITE_BIGDECIMAL_AS_PLAIN, true);
        sw = new StringWriter();
        jg = jf.createGenerator(sw);
        jg.writeNumber(ENG);
        jg.close();
        assertEquals("100", sw.toString());
    }

// com.fasterxml.jackson.core.json.TestJsonParser::testConfig
    public void testConfig() throws Exception
    {
        JsonParser jp = createParserUsingReader("[ ]");
        jp.enable(JsonParser.Feature.AUTO_CLOSE_SOURCE);
        assertTrue(jp.isEnabled(JsonParser.Feature.AUTO_CLOSE_SOURCE));
        jp.disable(JsonParser.Feature.AUTO_CLOSE_SOURCE);
        assertFalse(jp.isEnabled(JsonParser.Feature.AUTO_CLOSE_SOURCE));

        jp.configure(JsonParser.Feature.AUTO_CLOSE_SOURCE, true);
        assertTrue(jp.isEnabled(JsonParser.Feature.AUTO_CLOSE_SOURCE));
        jp.configure(JsonParser.Feature.AUTO_CLOSE_SOURCE, false);
        assertFalse(jp.isEnabled(JsonParser.Feature.AUTO_CLOSE_SOURCE));
        jp.close();
    }

// com.fasterxml.jackson.core.json.TestJsonParser::testInterningWithStreams
    public void testInterningWithStreams() throws Exception
    {
        _testIntern(true, true, "a");
        _testIntern(true, false, "b");
    }

// com.fasterxml.jackson.core.json.TestJsonParser::testInterningWithReaders
    public void testInterningWithReaders() throws Exception
    {
        _testIntern(false, true, "c");
        _testIntern(false, false, "d");
    }

// com.fasterxml.jackson.core.json.TestJsonParser::testSpecExampleSkipping
    public void testSpecExampleSkipping()
        throws Exception
    {
        doTestSpec(false);
    }

// com.fasterxml.jackson.core.json.TestJsonParser::testSpecExampleFully
    public void testSpecExampleFully()
        throws Exception
    {
        doTestSpec(true);
    }

// com.fasterxml.jackson.core.json.TestJsonParser::testKeywords
    public void testKeywords()
        throws Exception
    {
        final String DOC = "{\n"
            +"\"key1\" : null,\n"
            +"\"key2\" : true,\n"
            +"\"key3\" : false,\n"
            +"\"key4\" : [ false, null, true ]\n"
            +"}"
            ;

        JsonParser jp = createParserUsingStream(DOC, "UTF-8");

        JsonStreamContext ctxt = jp.getParsingContext();
        assertTrue(ctxt.inRoot());
        assertFalse(ctxt.inArray());
        assertFalse(ctxt.inObject());
        assertEquals(0, ctxt.getEntryCount());
        assertEquals(0, ctxt.getCurrentIndex());

        
        assertFalse(jp.hasCurrentToken());
        assertNull(jp.getText());
        assertNull(jp.getTextCharacters());
        assertEquals(0, jp.getTextLength());
        
        assertEquals(0, jp.getTextOffset());

        assertToken(JsonToken.START_OBJECT, jp.nextToken());

        assertTrue(jp.hasCurrentToken());
        JsonLocation loc = jp.getTokenLocation();
        assertNotNull(loc);
        assertEquals(1, loc.getLineNr());
        assertEquals(1, loc.getColumnNr());

        ctxt = jp.getParsingContext();
        assertFalse(ctxt.inRoot());
        assertFalse(ctxt.inArray());
        assertTrue(ctxt.inObject());
        assertEquals(0, ctxt.getEntryCount());
        assertEquals(0, ctxt.getCurrentIndex());

        assertToken(JsonToken.FIELD_NAME, jp.nextToken());
        verifyFieldName(jp, "key1");
        assertEquals(2, jp.getTokenLocation().getLineNr());

        ctxt = jp.getParsingContext();
        assertFalse(ctxt.inRoot());
        assertFalse(ctxt.inArray());
        assertTrue(ctxt.inObject());
        assertEquals(1, ctxt.getEntryCount());
        assertEquals(0, ctxt.getCurrentIndex());
        assertEquals("key1", ctxt.getCurrentName());

        assertToken(JsonToken.VALUE_NULL, jp.nextToken());
        assertEquals("key1", ctxt.getCurrentName());

        ctxt = jp.getParsingContext();
        assertEquals(1, ctxt.getEntryCount());
        assertEquals(0, ctxt.getCurrentIndex());

        assertToken(JsonToken.FIELD_NAME, jp.nextToken());
        verifyFieldName(jp, "key2");
        ctxt = jp.getParsingContext();
        assertEquals(2, ctxt.getEntryCount());
        assertEquals(1, ctxt.getCurrentIndex());
        assertEquals("key2", ctxt.getCurrentName());

        assertToken(JsonToken.VALUE_TRUE, jp.nextToken());
        assertEquals("key2", ctxt.getCurrentName());

        assertToken(JsonToken.FIELD_NAME, jp.nextToken());
        verifyFieldName(jp, "key3");
        assertToken(JsonToken.VALUE_FALSE, jp.nextToken());

        assertToken(JsonToken.FIELD_NAME, jp.nextToken());
        verifyFieldName(jp, "key4");

        assertToken(JsonToken.START_ARRAY, jp.nextToken());
        ctxt = jp.getParsingContext();
        assertTrue(ctxt.inArray());
        assertNull(ctxt.getCurrentName());
        assertEquals("key4", ctxt.getParent().getCurrentName());
        
        assertToken(JsonToken.VALUE_FALSE, jp.nextToken());
        assertToken(JsonToken.VALUE_NULL, jp.nextToken());
        assertToken(JsonToken.VALUE_TRUE, jp.nextToken());
        assertToken(JsonToken.END_ARRAY, jp.nextToken());

        ctxt = jp.getParsingContext();
        assertTrue(ctxt.inObject());

        assertToken(JsonToken.END_OBJECT, jp.nextToken());
        ctxt = jp.getParsingContext();
        assertTrue(ctxt.inRoot());
        assertNull(ctxt.getCurrentName());

        jp.close();
    }

// com.fasterxml.jackson.core.json.TestJsonParser::testSkipping
    public void testSkipping() throws Exception
    {
        String DOC =
            "[ 1, 3, [ true, null ], 3, { \"a\":\"b\" }, [ [ ] ], { } ]";
            ;
        JsonParser jp = createParserUsingStream(DOC, "UTF-8");

        
        assertToken(JsonToken.START_ARRAY, jp.nextToken());
        jp.skipChildren();
        assertEquals(JsonToken.END_ARRAY, jp.getCurrentToken());
        JsonToken t = jp.nextToken();
        if (t != null) {
            fail("Expected null at end of doc, got "+t);
        }
        jp.close();

        
        jp = createParserUsingStream(DOC, "UTF-8");
        assertToken(JsonToken.START_ARRAY, jp.nextToken());

        assertToken(JsonToken.VALUE_NUMBER_INT, jp.nextToken());
        jp.skipChildren();
        
        assertToken(JsonToken.VALUE_NUMBER_INT, jp.getCurrentToken());
        assertEquals(1, jp.getIntValue());

        assertToken(JsonToken.VALUE_NUMBER_INT, jp.nextToken());
        
        assertToken(JsonToken.START_ARRAY, jp.nextToken());
        jp.skipChildren();
        assertToken(JsonToken.END_ARRAY, jp.getCurrentToken());

        assertToken(JsonToken.VALUE_NUMBER_INT, jp.nextToken());
        assertToken(JsonToken.START_OBJECT, jp.nextToken());
        jp.skipChildren();
        assertToken(JsonToken.END_OBJECT, jp.getCurrentToken());

        assertToken(JsonToken.START_ARRAY, jp.nextToken());
        jp.skipChildren();
        assertToken(JsonToken.END_ARRAY, jp.getCurrentToken());

        assertToken(JsonToken.START_OBJECT, jp.nextToken());
        jp.skipChildren();
        assertToken(JsonToken.END_OBJECT, jp.getCurrentToken());

        assertToken(JsonToken.END_ARRAY, jp.nextToken());

        jp.close();
    }

// com.fasterxml.jackson.core.json.TestJsonParser::testNameEscaping
    public void testNameEscaping() throws IOException
    {
        _testNameEscaping(false);
        _testNameEscaping(true);
    }

// com.fasterxml.jackson.core.json.TestJsonParser::testLongText
    public void testLongText() throws Exception
    {
        
        _testLongText(7700);
        _testLongText(49000);
        _testLongText(96000);
    }

// com.fasterxml.jackson.core.json.TestJsonParser::testBytesAsSource
    public void testBytesAsSource() throws Exception
    {
        String JSON = "[ 1, 2, 3, 4 ]";
        byte[] b = JSON.getBytes("UTF-8");
        int offset = 50;
        int len = b.length;
        byte[] src = new byte[offset + len + offset];

        System.arraycopy(b, 0, src, offset, len);

        JsonParser jp = JSON_FACTORY.createParser(src, offset, len);

        assertToken(JsonToken.START_ARRAY, jp.nextToken());
        assertToken(JsonToken.VALUE_NUMBER_INT, jp.nextToken());
        assertEquals(1, jp.getIntValue());
        assertToken(JsonToken.VALUE_NUMBER_INT, jp.nextToken());
        assertEquals(2, jp.getIntValue());
        assertToken(JsonToken.VALUE_NUMBER_INT, jp.nextToken());
        assertEquals(3, jp.getIntValue());
        assertToken(JsonToken.VALUE_NUMBER_INT, jp.nextToken());
        assertEquals(4, jp.getIntValue());
        assertToken(JsonToken.END_ARRAY, jp.nextToken());
        assertNull(jp.nextToken());

        jp.close();
    }

// com.fasterxml.jackson.core.json.TestJsonParser::testUtf8BOMHandling
    public void testUtf8BOMHandling() throws Exception
    {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        
        bytes.write(0xEF);
        bytes.write(0xBB);
        bytes.write(0xBF);
        bytes.write("[ 1 ]".getBytes("UTF-8"));
        JsonParser jp = JSON_FACTORY.createParser(bytes.toByteArray());
        assertEquals(JsonToken.START_ARRAY, jp.nextToken());
        
        
        JsonLocation loc = jp.getTokenLocation();
        
        assertEquals(0, loc.getByteOffset());
        assertEquals(-1, loc.getCharOffset());
        jp.close();
    }

// com.fasterxml.jackson.core.json.TestJsonParser::testSpacesInURL
    public void testSpacesInURL() throws Exception
    {
        File f = File.createTempFile("pre fix&stuff", ".txt");
        BufferedWriter w = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(f), "UTF-8"));
        w.write("{ }");
        w.close();
        URL url = f.toURI().toURL();

        JsonParser jp = JSON_FACTORY.createParser(url);
        assertToken(JsonToken.START_OBJECT, jp.nextToken());
        assertToken(JsonToken.END_OBJECT, jp.nextToken());
        jp.close();
    }

// com.fasterxml.jackson.core.json.TestJsonParser::testHandlingOfInvalidSpaceBytes
    public void testHandlingOfInvalidSpaceBytes() throws Exception {
        _testHandlingOfInvalidSpace(true);
        _testHandlingOfInvalidSpaceFromResource(true);
    }

// com.fasterxml.jackson.core.json.TestJsonParser::testHandlingOfInvalidSpaceChars
    public void testHandlingOfInvalidSpaceChars() throws Exception {
        _testHandlingOfInvalidSpace(false);
        _testHandlingOfInvalidSpaceFromResource(false);
    }

// com.fasterxml.jackson.core.json.TestRootValues::testSimpleNumbers
    public void testSimpleNumbers() throws Exception
    {
        _testSimpleNumbers(false);
        _testSimpleNumbers(true);
    }

// com.fasterxml.jackson.core.json.TestRootValues::testSimpleBooleans
    public void testSimpleBooleans() throws Exception
    {
        _testSimpleBooleans(false);
        _testSimpleBooleans(true);
    }

// com.fasterxml.jackson.core.json.TestRootValues::testSimpleWrites
    public void testSimpleWrites() throws Exception
    {
        _testSimpleWrites(false);
        _testSimpleWrites(true);
    }

// com.fasterxml.jackson.core.json.TestUtf8Generator::testUtf8Issue462
    public void testUtf8Issue462() throws Exception
    {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        IOContext ioc = new IOContext(new BufferRecycler(), bytes, true);
        JsonGenerator gen = new UTF8JsonGenerator(ioc, 0, null, bytes);
        String str = "Natuurlijk is alles gelukt en weer een tevreden klant\uD83D\uDE04";
        int length = 4000 - 38;

        for (int i = 1; i <= length; ++i) {
            gen.writeNumber(1);
        }
        gen.writeString(str);
        gen.flush();
        gen.close();
        
        
        JsonFactory f = new JsonFactory();
        JsonParser p = f.createParser(bytes.toByteArray());
        for (int i = 1; i <= length; ++i) {
            assertToken(JsonToken.VALUE_NUMBER_INT, p.nextToken());
            assertEquals(1, p.getIntValue());
        }
        assertToken(JsonToken.VALUE_STRING, p.nextToken());
        assertEquals(str, p.getText());
        assertNull(p.nextToken());
        p.close();
    }

// com.fasterxml.jackson.core.json.TestUtf8Generator::testSurrogatesWithRaw
    public void testSurrogatesWithRaw() throws Exception
    {
        final String VALUE = quote("\ud83d\ude0c");
        JsonFactory f = new JsonFactory();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        JsonGenerator jgen = f.createGenerator(out);
        jgen.writeStartArray();
        jgen.writeRaw(VALUE);
        jgen.writeEndArray();
        jgen.close();

        final byte[] JSON = out.toByteArray();

        JsonParser jp = f.createParser(JSON);
        assertToken(JsonToken.START_ARRAY, jp.nextToken());
        assertToken(JsonToken.VALUE_STRING, jp.nextToken());
        String str = jp.getText();
        assertEquals(2, str.length());
        assertEquals((char) 0xD83D, str.charAt(0));
        assertEquals((char) 0xDE0C, str.charAt(1));
        assertToken(JsonToken.END_ARRAY, jp.nextToken());
        jp.close();
    }

// com.fasterxml.jackson.core.main.TestGeneratorArray::testEmptyArrayWrite
    public void testEmptyArrayWrite()
        throws Exception
    {
        StringWriter sw = new StringWriter();
        JsonGenerator gen = new JsonFactory().createGenerator(sw);

        JsonStreamContext ctxt = gen.getOutputContext();
        assertTrue(ctxt.inRoot());
        assertFalse(ctxt.inArray());
        assertFalse(ctxt.inObject());
        assertEquals(0, ctxt.getEntryCount());
        assertEquals(0, ctxt.getCurrentIndex());

        gen.writeStartArray();

        ctxt = gen.getOutputContext();
        assertFalse(ctxt.inRoot());
        assertTrue(ctxt.inArray());
        assertFalse(ctxt.inObject());
        assertEquals(0, ctxt.getEntryCount());
        assertEquals(0, ctxt.getCurrentIndex());

        gen.writeEndArray();

        ctxt = gen.getOutputContext();
        assertTrue("Should be in root, was "+ctxt.getTypeDesc(), ctxt.inRoot());
        assertFalse(ctxt.inArray());
        assertFalse(ctxt.inObject());
        assertEquals(1, ctxt.getEntryCount());
        
        assertEquals(0, ctxt.getCurrentIndex());

        gen.close();
        String docStr = sw.toString();
        JsonParser jp = createParserUsingReader(docStr);
        assertEquals(JsonToken.START_ARRAY, jp.nextToken());
        assertEquals(JsonToken.END_ARRAY, jp.nextToken());
        jp.close();

        
        sw = new StringWriter();
        gen = new JsonFactory().createGenerator(sw);
        gen.writeStartArray();
        gen.writeStartArray();
        gen.writeEndArray();
        gen.writeEndArray();
        gen.close();
        docStr = sw.toString();
        jp = createParserUsingReader(docStr);
        assertEquals(JsonToken.START_ARRAY, jp.nextToken());
        assertEquals(JsonToken.START_ARRAY, jp.nextToken());
        assertEquals(JsonToken.END_ARRAY, jp.nextToken());
        assertEquals(JsonToken.END_ARRAY, jp.nextToken());
        assertEquals(null, jp.nextToken());
        jp.close();
    }

// com.fasterxml.jackson.core.main.TestGeneratorArray::testInvalidArrayWrite
    public void testInvalidArrayWrite()
        throws Exception
    {
        StringWriter sw = new StringWriter();
        JsonGenerator gen = new JsonFactory().createGenerator(sw);
        gen.writeStartArray();
        
        try {
            gen.writeEndObject();
            fail("Expected an exception for mismatched array/object write");
        } catch (JsonGenerationException e) {
            verifyException(e, "Current context not an object");
        }
        gen.close();
    }

// com.fasterxml.jackson.core.main.TestGeneratorArray::testSimpleArrayWrite
    public void testSimpleArrayWrite()
        throws Exception
    {
        StringWriter sw = new StringWriter();
        JsonGenerator gen = new JsonFactory().createGenerator(sw);
        gen.writeStartArray();
        gen.writeNumber(13);
        gen.writeBoolean(true);
        gen.writeString("foobar");
        gen.writeEndArray();
        gen.close();
        String docStr = sw.toString();
        JsonParser jp = createParserUsingReader(docStr);
        assertEquals(JsonToken.START_ARRAY, jp.nextToken());
        assertEquals(JsonToken.VALUE_NUMBER_INT, jp.nextToken());
        assertEquals(13, jp.getIntValue());
        assertEquals(JsonToken.VALUE_TRUE, jp.nextToken());
        assertEquals(JsonToken.VALUE_STRING, jp.nextToken());
        assertEquals("foobar", jp.getText());
        assertEquals(JsonToken.END_ARRAY, jp.nextToken());
        assertEquals(null, jp.nextToken());
        jp.close();
    }

// com.fasterxml.jackson.core.main.TestGeneratorClosing::testNoAutoCloseGenerator
    public void testNoAutoCloseGenerator() throws Exception
    {
        JsonFactory f = new JsonFactory();

        
        assertTrue(f.isEnabled(JsonGenerator.Feature.AUTO_CLOSE_TARGET));
        
        f.disable(JsonGenerator.Feature.AUTO_CLOSE_TARGET);
        assertFalse(f.isEnabled(JsonGenerator.Feature.AUTO_CLOSE_TARGET));
        @SuppressWarnings("resource")
        MyWriter output = new MyWriter();
        JsonGenerator jg = f.createGenerator(output);

        
        assertFalse(output.isClosed());
        jg.writeNumber(39);
        
        jg.close();
        assertFalse(output.isClosed());
    }

// com.fasterxml.jackson.core.main.TestGeneratorClosing::testCloseGenerator
    public void testCloseGenerator() throws Exception
    {
        JsonFactory f = new JsonFactory();
        f.enable(JsonGenerator.Feature.AUTO_CLOSE_TARGET);
        @SuppressWarnings("resource")
        MyWriter output = new MyWriter();
        JsonGenerator jg = f.createGenerator(output);

        
        assertFalse(output.isClosed());
        jg.writeNumber(39);
        
        jg.close();
        assertTrue(output.isClosed());
    }

// com.fasterxml.jackson.core.main.TestGeneratorClosing::testNoAutoCloseOutputStream
    public void testNoAutoCloseOutputStream() throws Exception
    {
        JsonFactory f = new JsonFactory();
        f.disable(JsonGenerator.Feature.AUTO_CLOSE_TARGET);
        @SuppressWarnings("resource")
        MyStream output = new MyStream();
        JsonGenerator jg = f.createGenerator(output, JsonEncoding.UTF8);

        assertFalse(output.isClosed());
        jg.writeNumber(39);
        jg.close();
        assertFalse(output.isClosed());
    }

// com.fasterxml.jackson.core.main.TestGeneratorClosing::testAutoCloseArraysAndObjects
    public void testAutoCloseArraysAndObjects()
        throws Exception
    {
        JsonFactory f = new JsonFactory();
        
        assertTrue(f.isEnabled(JsonGenerator.Feature.AUTO_CLOSE_JSON_CONTENT));
        StringWriter sw = new StringWriter();

        
        JsonGenerator jg = f.createGenerator(sw);
        jg.writeStartArray();
        jg.close();
        assertEquals("[]", sw.toString());

        
        sw = new StringWriter();
        jg = f.createGenerator(sw);
        jg.writeStartObject();
        jg.close();
        assertEquals("{}", sw.toString());
    }

// com.fasterxml.jackson.core.main.TestGeneratorClosing::testNoAutoCloseArraysAndObjects
    public void testNoAutoCloseArraysAndObjects()
        throws Exception
    {
        JsonFactory f = new JsonFactory();
        f.disable(JsonGenerator.Feature.AUTO_CLOSE_JSON_CONTENT);
        StringWriter sw = new StringWriter();
        JsonGenerator jg = f.createGenerator(sw);
        jg.writeStartArray();
        jg.close();
        
        assertEquals("[", sw.toString());

        
        sw = new StringWriter();
        jg = f.createGenerator(sw);
        jg.writeStartObject();
        jg.close();
        assertEquals("{", sw.toString());
    }

// com.fasterxml.jackson.core.main.TestGeneratorClosing::testAutoFlushOrNot
    public void testAutoFlushOrNot() throws Exception
    {
        JsonFactory f = new JsonFactory();
        assertTrue(f.isEnabled(JsonGenerator.Feature.FLUSH_PASSED_TO_STREAM));
        MyChars sw = new MyChars();
        JsonGenerator jg = f.createGenerator(sw);
        jg.writeStartArray();
        jg.writeEndArray();
        assertEquals(0, sw.flushed);
        jg.flush();
        assertEquals(1, sw.flushed);
        jg.close();
        
        
        MyBytes bytes = new MyBytes();
        jg = f.createGenerator(bytes, JsonEncoding.UTF8);
        jg.writeStartArray();
        jg.writeEndArray();
        assertEquals(0, bytes.flushed);
        jg.flush();
        assertEquals(1, bytes.flushed);
        assertEquals(2, bytes.toByteArray().length);
        jg.close();

        
        f.disable(JsonGenerator.Feature.FLUSH_PASSED_TO_STREAM);
        
        sw = new MyChars();
        jg = f.createGenerator(sw);
        jg.writeStartArray();
        jg.writeEndArray();
        assertEquals(0, sw.flushed);
        jg.flush();
        assertEquals(0, sw.flushed);
        jg.close();
        assertEquals("[]", sw.toString());

        
        bytes = new MyBytes();
        jg = f.createGenerator(bytes, JsonEncoding.UTF8);
        jg.writeStartArray();
        jg.writeEndArray();
        assertEquals(0, bytes.flushed);
        jg.flush();
        assertEquals(0, bytes.flushed);
        jg.close();
        assertEquals(2, bytes.toByteArray().length);
    }

// com.fasterxml.jackson.core.main.TestGeneratorCopy::testCopyRootTokens
    public void testCopyRootTokens()
        throws IOException
    {
        JsonFactory jf = new JsonFactory();
        final String DOC = "\"text\\non two lines\" true false 2.0";
        JsonParser jp = jf.createParser(new StringReader(DOC));
        StringWriter sw = new StringWriter();
        JsonGenerator gen = jf.createGenerator(sw);

        JsonToken t;

        while ((t = jp.nextToken()) != null) {
            gen.copyCurrentEvent(jp);
            
            assertToken(t, jp.getCurrentToken());
        }
        jp.close();
        gen.close();

        assertEquals("\"text\\non two lines\" true false 2.0", sw.toString());
    }

// com.fasterxml.jackson.core.main.TestGeneratorCopy::testCopyArrayTokens
    public void testCopyArrayTokens()
        throws IOException
    {
        JsonFactory jf = new JsonFactory();
        final String DOC = "123 [ 1, null, [ false ] ]";
        JsonParser jp = jf.createParser(new StringReader(DOC));
        StringWriter sw = new StringWriter();
        JsonGenerator gen = jf.createGenerator(sw);

        assertToken(JsonToken.VALUE_NUMBER_INT, jp.nextToken());
        gen.copyCurrentEvent(jp);
        
        assertToken(JsonToken.VALUE_NUMBER_INT, jp.getCurrentToken());
        assertEquals(123, jp.getIntValue());

        
        assertToken(JsonToken.START_ARRAY, jp.nextToken());
        gen.copyCurrentStructure(jp);
        
        assertToken(JsonToken.END_ARRAY, jp.getCurrentToken());
        jp.close();
        gen.close();

        assertEquals("123 [1,null,[false]]", sw.toString());
    }

// com.fasterxml.jackson.core.main.TestGeneratorCopy::testCopyObjectTokens
    public void testCopyObjectTokens()
        throws IOException
    {
        JsonFactory jf = new JsonFactory();
        final String DOC = "{ \"a\":1, \"b\":[{ \"c\" : null }] }";
        JsonParser jp = jf.createParser(new StringReader(DOC));
        StringWriter sw = new StringWriter();
        JsonGenerator gen = jf.createGenerator(sw);

        assertToken(JsonToken.START_OBJECT, jp.nextToken());
        gen.copyCurrentStructure(jp);
        
        assertToken(JsonToken.END_OBJECT, jp.getCurrentToken());
        jp.close();
        gen.close();

        assertEquals("{\"a\":1,\"b\":[{\"c\":null}]}", sw.toString());
    }

// com.fasterxml.jackson.core.main.TestGeneratorMisc::testIsClosed
    public void testIsClosed()
        throws IOException
    {
        JsonFactory jf = new JsonFactory();
        for (int i = 0; i < 2; ++i) {
            boolean stream = ((i & 1) == 0);
            JsonGenerator jg = stream ?
                jf.createGenerator(new StringWriter())
                : jf.createGenerator(new ByteArrayOutputStream(), JsonEncoding.UTF8)
                ;
            assertFalse(jg.isClosed());
            jg.writeStartArray();
            jg.writeNumber(-1);
            jg.writeEndArray();
            assertFalse(jg.isClosed());
            jg.close();
            assertTrue(jg.isClosed());
            jg.close();
            assertTrue(jg.isClosed());
        }
    }

// com.fasterxml.jackson.core.main.TestGeneratorMisc::testSimpleWriteObject
    public void testSimpleWriteObject() throws IOException
    {
        
        JsonFactory jf = new JsonFactory();
        StringWriter sw = new StringWriter();
        JsonGenerator gen = jf.createGenerator(sw);
        gen.writeStartArray();

        
        gen.writeObject(1);
        gen.writeObject((short) -2);
        gen.writeObject((long) 3);
        gen.writeObject((byte) -4);
        gen.writeObject(0.25);
        gen.writeObject(-0.125f);
        gen.writeObject(Boolean.TRUE);
        gen.close();
        String act = sw.toString().trim();
        assertEquals("[1,-2,3,-4,0.25,-0.125,true]", act);
        
        
        sw = new StringWriter();
        gen = jf.createGenerator(sw);
        gen.writeStartArray();
        gen.writeObject(BigInteger.valueOf(1234));
        gen.writeObject(new BigDecimal(0.5));
        gen.writeEndArray();
        gen.close();
        act = sw.toString().trim();
        assertEquals("[1234,0.5]", act);

        
        sw = new StringWriter();
        gen = jf.createGenerator(sw);
        gen.writeStartArray();
        gen.writeObject(new AtomicBoolean(false));
        gen.writeObject(new AtomicInteger(13));
        gen.writeObject(new AtomicLong(-127L));
        gen.writeEndArray();
        gen.close();
        act = sw.toString().trim();
        assertEquals("[false,13,-127]", act);
    }

// com.fasterxml.jackson.core.main.TestGeneratorMisc::testRaw
    public void testRaw() throws IOException
    {
        JsonFactory jf = new JsonFactory();
        StringWriter sw = new StringWriter();
        JsonGenerator gen = jf.createGenerator(sw);
        gen.writeStartArray();
        gen.writeRaw("-123, true");
        gen.writeRaw(", \"x\"  ");
        gen.writeEndArray();
        gen.close();

                
        JsonParser jp = createParserUsingReader(sw.toString());
        assertToken(JsonToken.START_ARRAY, jp.nextToken());
        assertToken(JsonToken.VALUE_NUMBER_INT, jp.nextToken());
        assertEquals(-123, jp.getIntValue());
        assertToken(JsonToken.VALUE_TRUE, jp.nextToken());
        assertToken(JsonToken.VALUE_STRING, jp.nextToken());
        assertEquals("x", jp.getText());
        assertToken(JsonToken.END_ARRAY, jp.nextToken());
        jp.close();
    }

// com.fasterxml.jackson.core.main.TestGeneratorMisc::testRawValue
    public void testRawValue() throws IOException
    {
        JsonFactory jf = new JsonFactory();
        StringWriter sw = new StringWriter();
        JsonGenerator gen = jf.createGenerator(sw);
        gen.writeStartArray();
        gen.writeRawValue("7");
        gen.writeRawValue("[ null ]");
        gen.writeRawValue("false");
        gen.writeEndArray();
        gen.close();

        JsonParser jp = createParserUsingReader(sw.toString());
        assertToken(JsonToken.START_ARRAY, jp.nextToken());

        assertToken(JsonToken.VALUE_NUMBER_INT, jp.nextToken());
        assertEquals(7, jp.getIntValue());
        assertToken(JsonToken.START_ARRAY, jp.nextToken());
        assertToken(JsonToken.VALUE_NULL, jp.nextToken());
        assertToken(JsonToken.END_ARRAY, jp.nextToken());
        assertToken(JsonToken.VALUE_FALSE, jp.nextToken());

        assertToken(JsonToken.END_ARRAY, jp.nextToken());
        jp.close();
    }

// com.fasterxml.jackson.core.main.TestGeneratorMisc::testBinaryWrite
    public void testBinaryWrite() throws Exception
    {
        _testBinaryWrite(false);
        _testBinaryWrite(true);
    }

// com.fasterxml.jackson.core.main.TestGeneratorMisc::testLongerObjects
    public void testLongerObjects() throws Exception
    {
        JsonFactory jf = new JsonFactory();
        for (int i = 0; i < 2; ++i) {
            boolean useChars = (i == 0);
            JsonGenerator jgen;
            ByteArrayOutputStream bout = new ByteArrayOutputStream(200);
            if (useChars) {
                jgen = jf.createGenerator(new OutputStreamWriter(bout, "UTF-8"));
            } else {
                jgen = jf.createGenerator(bout, JsonEncoding.UTF8);
            }

            jgen.writeStartObject();

            for (int rounds = 0; rounds < 1500; ++rounds) {
                for (int letter = 'a'; letter <= 'z'; ++letter) {
                    for (int index = 0; index < 20; ++index) {
                        String name;
                        if (letter > 'f') {
                            name = "X"+letter+index;
                        } else if (letter > 'p') {
                            name = ""+letter+index;
                        } else {
                            name = "__"+index+letter;
                        }
                        jgen.writeFieldName(name);
                        jgen.writeNumber(index-1);
                    }
                    jgen.writeRaw('\n');
                }
            }
            jgen.writeEndObject();
            jgen.close();

            byte[] json = bout.toByteArray();
            JsonParser jp = jf.createParser(json);
            assertToken(JsonToken.START_OBJECT, jp.nextToken());
            for (int rounds = 0; rounds < 1500; ++rounds) {
            for (int letter = 'a'; letter <= 'z'; ++letter) {
                for (int index = 0; index < 20; ++index) {
                    assertToken(JsonToken.FIELD_NAME, jp.nextToken());
                    String name;
                    if (letter > 'f') {
                        name = "X"+letter+index;
                    } else if (letter > 'p') {
                        name = ""+letter+index;
                    } else {
                        name = "__"+index+letter;
                    }
                    assertEquals(name, jp.getCurrentName());
                    assertToken(JsonToken.VALUE_NUMBER_INT, jp.nextToken());
                    assertEquals(index-1, jp.getIntValue());
                }
            }
            }
            assertToken(JsonToken.END_OBJECT, jp.nextToken());
            jp.close();
        }
    }

// com.fasterxml.jackson.core.main.TestGeneratorObject::testEmptyObjectWrite
    public void testEmptyObjectWrite()
        throws Exception
    {
        StringWriter sw = new StringWriter();
        JsonGenerator gen = new JsonFactory().createGenerator(sw);

        JsonStreamContext ctxt = gen.getOutputContext();
        assertTrue(ctxt.inRoot());
        assertFalse(ctxt.inArray());
        assertFalse(ctxt.inObject());
        assertEquals(0, ctxt.getEntryCount());
        assertEquals(0, ctxt.getCurrentIndex());

        gen.writeStartObject();

        ctxt = gen.getOutputContext();
        assertFalse(ctxt.inRoot());
        assertFalse(ctxt.inArray());
        assertTrue(ctxt.inObject());
        assertEquals(0, ctxt.getEntryCount());
        assertEquals(0, ctxt.getCurrentIndex());

        gen.writeEndObject();

        ctxt = gen.getOutputContext();
        assertTrue(ctxt.inRoot());
        assertFalse(ctxt.inArray());
        assertFalse(ctxt.inObject());
        assertEquals(1, ctxt.getEntryCount());
        
        assertEquals(0, ctxt.getCurrentIndex());

        gen.close();

        String docStr = sw.toString();
        JsonParser jp = createParserUsingReader(docStr);
        assertEquals(JsonToken.START_OBJECT, jp.nextToken());
        assertEquals(JsonToken.END_OBJECT, jp.nextToken());
        assertEquals(null, jp.nextToken());
        jp.close();
    }

// com.fasterxml.jackson.core.main.TestGeneratorObject::testInvalidObjectWrite
    public void testInvalidObjectWrite()
        throws Exception
    {
        StringWriter sw = new StringWriter();
        JsonGenerator gen = new JsonFactory().createGenerator(sw);
        gen.writeStartObject();
        
        try {
            gen.writeEndArray();
            fail("Expected an exception for mismatched array/object write");
        } catch (JsonGenerationException e) {
            verifyException(e, "Current context not an array");
        }
        gen.close();
    }

// com.fasterxml.jackson.core.main.TestGeneratorObject::testSimpleObjectWrite
    public void testSimpleObjectWrite()
        throws Exception
    {
        StringWriter sw = new StringWriter();
        JsonGenerator gen = new JsonFactory().createGenerator(sw);
        gen.writeStartObject();
        gen.writeFieldName("first");
        gen.writeNumber(-901);
        gen.writeFieldName("sec");
        gen.writeBoolean(false);
        gen.writeFieldName("3rd!"); 
        gen.writeString("yee-haw");
        gen.writeEndObject();
        gen.close();
        String docStr = sw.toString();
        JsonParser jp = createParserUsingReader(docStr);
        assertEquals(JsonToken.START_OBJECT, jp.nextToken());
        assertEquals(JsonToken.FIELD_NAME, jp.nextToken());
        assertEquals("first", jp.getText());
        assertEquals(JsonToken.VALUE_NUMBER_INT, jp.nextToken());
        assertEquals(-901, jp.getIntValue());
        assertEquals(JsonToken.FIELD_NAME, jp.nextToken());
        assertEquals("sec", jp.getText());
        assertEquals(JsonToken.VALUE_FALSE, jp.nextToken());
        assertEquals(JsonToken.FIELD_NAME, jp.nextToken());
        assertEquals("3rd!", jp.getText());
        assertEquals(JsonToken.VALUE_STRING, jp.nextToken());
        assertEquals("yee-haw", jp.getText());
        assertEquals(JsonToken.END_OBJECT, jp.nextToken());
        assertEquals(null, jp.nextToken());
        jp.close();
    }

// com.fasterxml.jackson.core.main.TestGeneratorObject::testConvenienceMethods
    public void testConvenienceMethods()
        throws Exception
    {
        StringWriter sw = new StringWriter();
        JsonGenerator gen = new JsonFactory().createGenerator(sw);
        gen.writeStartObject();

        final BigDecimal dec = new BigDecimal("0.1");
        final String TEXT = "\"some\nString!\"";

        gen.writeNullField("null");
        gen.writeBooleanField("bt", true);
        gen.writeBooleanField("bf", false);
        gen.writeNumberField("int", -1289);
        gen.writeNumberField("dec", dec);

        gen.writeObjectFieldStart("ob");
        gen.writeStringField("str", TEXT);
        gen.writeEndObject();

        gen.writeArrayFieldStart("arr");
        gen.writeEndArray();

        gen.writeEndObject();
        gen.close();

        String docStr = sw.toString();
        JsonParser jp = createParserUsingReader(docStr);
        assertEquals(JsonToken.START_OBJECT, jp.nextToken());

        assertEquals(JsonToken.FIELD_NAME, jp.nextToken());
        assertEquals("null", jp.getText());
        assertEquals(JsonToken.VALUE_NULL, jp.nextToken());
        assertEquals(JsonToken.FIELD_NAME, jp.nextToken());
        assertEquals("bt", jp.getText());
        assertEquals(JsonToken.VALUE_TRUE, jp.nextToken());
        assertEquals(JsonToken.FIELD_NAME, jp.nextToken());
        assertEquals("bf", jp.getText());
        assertEquals(JsonToken.VALUE_FALSE, jp.nextToken());
        assertEquals(JsonToken.FIELD_NAME, jp.nextToken());
        assertEquals("int", jp.getText());
        assertEquals(JsonToken.VALUE_NUMBER_INT, jp.nextToken());
        assertEquals(JsonToken.FIELD_NAME, jp.nextToken());
        assertEquals("dec", jp.getText());
        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, jp.nextToken());

        assertEquals(JsonToken.FIELD_NAME, jp.nextToken());
        assertEquals("ob", jp.getText());
        assertEquals(JsonToken.START_OBJECT, jp.nextToken());
        assertEquals(JsonToken.FIELD_NAME, jp.nextToken());
        assertEquals("str", jp.getText());
        assertEquals(JsonToken.VALUE_STRING, jp.nextToken());
        assertEquals(TEXT, getAndVerifyText(jp));
        assertEquals(JsonToken.END_OBJECT, jp.nextToken());

        assertEquals(JsonToken.FIELD_NAME, jp.nextToken());
        assertEquals("arr", jp.getText());
        assertEquals(JsonToken.START_ARRAY, jp.nextToken());
        assertEquals(JsonToken.END_ARRAY, jp.nextToken());

        assertEquals(JsonToken.END_OBJECT, jp.nextToken());
        assertEquals(null, jp.nextToken());
        jp.close();
    }

// com.fasterxml.jackson.core.main.TestGeneratorObject::testConvenienceMethodsWithNulls
    public void testConvenienceMethodsWithNulls()
        throws Exception
    {
        StringWriter sw = new StringWriter();
        JsonGenerator gen = new JsonFactory().createGenerator(sw);
        gen.writeStartObject();

        gen.writeStringField("str", null);
        gen.writeNumberField("num", null);
        gen.writeObjectField("obj", null);

        gen.writeEndObject();
        gen.close();

        String docStr = sw.toString();
        JsonParser jp = createParserUsingReader(docStr);
        assertEquals(JsonToken.START_OBJECT, jp.nextToken());

        assertEquals(JsonToken.FIELD_NAME, jp.nextToken());
        assertEquals("str", jp.getCurrentName());
        assertEquals(JsonToken.VALUE_NULL, jp.nextToken());

        assertEquals(JsonToken.FIELD_NAME, jp.nextToken());
        assertEquals("num", jp.getCurrentName());
        assertEquals(JsonToken.VALUE_NULL, jp.nextToken());

        assertEquals(JsonToken.FIELD_NAME, jp.nextToken());
        assertEquals("obj", jp.getCurrentName());
        assertEquals(JsonToken.VALUE_NULL, jp.nextToken());

        assertEquals(JsonToken.END_OBJECT, jp.nextToken());
        jp.close();
    }

// com.fasterxml.jackson.core.main.TestPrettyPrinter::testObjectCount
    public void testObjectCount() throws Exception
    {
        final String EXP = "{\"x\":{\"a\":1,\"b\":2(2)}(1)}";
        final JsonFactory jf = new JsonFactory();

        for (int i = 0; i < 2; ++i) {
            boolean useBytes = (i > 0);
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            StringWriter sw = new StringWriter();
            JsonGenerator gen = useBytes ? jf.createGenerator(bytes)
                    : jf.createGenerator(sw);
            gen.setPrettyPrinter(new CountPrinter());
            gen.writeStartObject();
            gen.writeFieldName("x");
            gen.writeStartObject();
            gen.writeNumberField("a", 1);
            gen.writeNumberField("b", 2);
            gen.writeEndObject();
            gen.writeEndObject();
            gen.close();

            String json = useBytes ? bytes.toString("UTF-8") : sw.toString();
            assertEquals(EXP, json);
        }
    }

// com.fasterxml.jackson.core.main.TestPrettyPrinter::testArrayCount
    public void testArrayCount() throws Exception
    {
        final String EXP = "[6,[1,2,9(3)](2)]";
        
        final JsonFactory jf = new JsonFactory();

        for (int i = 0; i < 2; ++i) {
            boolean useBytes = (i > 0);
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            StringWriter sw = new StringWriter();
            JsonGenerator gen = useBytes ? jf.createGenerator(bytes)
                    : jf.createGenerator(sw);
            gen.setPrettyPrinter(new CountPrinter());
            gen.writeStartArray();
            gen.writeNumber(6);
            gen.writeStartArray();
            gen.writeNumber(1);
            gen.writeNumber(2);
            gen.writeNumber(9);
            gen.writeEndArray();
            gen.writeEndArray();
            gen.close();

            String json = useBytes ? bytes.toString("UTF-8") : sw.toString();
            assertEquals(EXP, json);
        }
    }

// com.fasterxml.jackson.core.main.TestPrettyPrinter::testSimpleDocWithDefault
    public void testSimpleDocWithDefault() throws Exception
    {
        StringWriter sw = new StringWriter();
        JsonGenerator gen = new JsonFactory().createGenerator(sw);
        gen.useDefaultPrettyPrinter();
        _verifyPrettyPrinter(gen, sw);
        gen.close();
    }

// com.fasterxml.jackson.core.main.TestPrettyPrinter::testSimpleDocWithMinimal
    public void testSimpleDocWithMinimal() throws Exception
    {
        StringWriter sw = new StringWriter();
        JsonGenerator gen = new JsonFactory().createGenerator(sw);
        
        gen.setPrettyPrinter(new MinimalPrettyPrinter());
        String docStr = _verifyPrettyPrinter(gen, sw);
        
        assertEquals(-1, docStr.indexOf('\n'));
        assertEquals(-1, docStr.indexOf('\t'));

        
        gen = new JsonFactory().createGenerator(sw);
        gen.setPrettyPrinter(new MinimalPrettyPrinter() {
            @Override
            
            public void beforeArrayValues(JsonGenerator jg) throws IOException, JsonGenerationException
            {
                jg.writeRaw("\t");
            }
        });
        docStr = _verifyPrettyPrinter(gen, sw);
        assertEquals(-1, docStr.indexOf('\n'));
        assertTrue(docStr.indexOf('\t') >= 0);
        gen.close();
    }

// com.fasterxml.jackson.core.main.TestPrettyPrinter::testCustomRootSeparatorWithPP
    public void testCustomRootSeparatorWithPP() throws Exception
    {
        JsonFactory jf = new JsonFactory();
        
        assertEquals("{} {} []", _generateRoot(jf, null));
        
        assertEquals("{ } { } [ ]", _generateRoot(jf, new DefaultPrettyPrinter()));
        
        assertEquals("{ }|{ }|[ ]", _generateRoot(jf, new DefaultPrettyPrinter("|")));
    }

// com.fasterxml.jackson.core.main.TestPrettyPrinter::testCustomRootSeparatorWithFactory
    public void testCustomRootSeparatorWithFactory() throws Exception
    {
        JsonFactory jf = new JsonFactory();
        jf.setRootValueSeparator("##");
        StringWriter sw = new StringWriter();
        JsonGenerator gen = jf.createGenerator(sw);
        gen.writeNumber(13);
        gen.writeBoolean(false);
        gen.writeNull();
        gen.close();
        assertEquals("13##false##null", sw.toString());
    }

// com.fasterxml.jackson.core.main.TestRawStringWriting::testUtf8RawStrings
    public void testUtf8RawStrings() throws Exception
    {
        
        List<byte[]> strings = generateStrings(new Random(28), 750000, false);
        ByteArrayOutputStream out = new ByteArrayOutputStream(16000);
        JsonFactory jf = new JsonFactory();
        JsonGenerator jgen = jf.createGenerator(out, JsonEncoding.UTF8);
        jgen.writeStartArray();
        for (byte[] str : strings) {
            jgen.writeRawUTF8String(str, 0, str.length);
        }
        jgen.writeEndArray();
        jgen.close();
        byte[] json = out.toByteArray();
        
        
        JsonParser jp = jf.createParser(json);
        assertToken(JsonToken.START_ARRAY, jp.nextToken());
        for (byte[] inputBytes : strings) {
            assertToken(JsonToken.VALUE_STRING, jp.nextToken());
            String string = jp.getText();
            byte[] outputBytes = string.getBytes("UTF-8");
            assertEquals(inputBytes.length, outputBytes.length);
            assertArrayEquals(inputBytes, outputBytes);
        }
        assertToken(JsonToken.END_ARRAY, jp.nextToken());
        jp.close();
    }

// com.fasterxml.jackson.core.main.TestRawStringWriting::testUtf8StringsWithEscaping
    public void testUtf8StringsWithEscaping() throws Exception
    {
        
        List<byte[]> strings = generateStrings(new Random(28), 720000, true);
        ByteArrayOutputStream out = new ByteArrayOutputStream(16000);
        JsonFactory jf = new JsonFactory();
        JsonGenerator jgen = jf.createGenerator(out, JsonEncoding.UTF8);
        jgen.writeStartArray();

        for (byte[] str : strings) {
            jgen.writeUTF8String(str, 0, str.length);
            jgen.writeRaw('\n');
        }
        jgen.writeEndArray();
        jgen.close();
        byte[] json = out.toByteArray();
        
        
        JsonParser jp = jf.createParser(json);
        assertToken(JsonToken.START_ARRAY, jp.nextToken());
        for (byte[] inputBytes : strings) {
            assertToken(JsonToken.VALUE_STRING, jp.nextToken());
            String string = jp.getText();

            byte[] outputBytes = string.getBytes("UTF-8");
            assertEquals(inputBytes.length, outputBytes.length);
            assertArrayEquals(inputBytes, outputBytes);
        }
        assertToken(JsonToken.END_ARRAY, jp.nextToken());
        jp.close();
    }

// com.fasterxml.jackson.core.main.TestRawStringWriting::testWriteRawWithSerializable
    public void testWriteRawWithSerializable() throws Exception
    {
        JsonFactory jf = new JsonFactory();
        
        _testWithRaw(jf, true);
        _testWithRaw(jf, false);
    }

// com.fasterxml.jackson.core.main.TestStringGeneration::testBasicEscaping
    public void testBasicEscaping()
        throws Exception
    {
        doTestBasicEscaping(false);
        doTestBasicEscaping(true);
    }

// com.fasterxml.jackson.core.main.TestStringGeneration::testLongerRandomSingleChunk
    public void testLongerRandomSingleChunk()
        throws Exception
    {
        
        for (int round = 0; round < 80; ++round) {
            String content = generateRandom(75000+round);
            doTestLongerRandom(content, false);
            doTestLongerRandom(content, true);
        }
    }

// com.fasterxml.jackson.core.main.TestStringGeneration::testLongerRandomMultiChunk
    public void testLongerRandomMultiChunk()
        throws Exception
    {
        
        for (int round = 0; round < 70; ++round) {
            String content = generateRandom(73000+round);
            doTestLongerRandomMulti(content, false, round);
            doTestLongerRandomMulti(content, true, round);
        }
    }

// com.fasterxml.jackson.core.util.TestDefaultPrettyPrinter::testSystemLinefeed
    public void testSystemLinefeed() throws IOException
    {
        PrettyPrinter pp = new DefaultPrettyPrinter();
        String LF = System.getProperty("line.separator");
        assertEquals(
            "{" + LF +
            "  \"name\" : \"John Doe\"," + LF +
            "  \"age\" : 3.14" + LF +
            "}", _printTestData(pp));
    }

// com.fasterxml.jackson.core.util.TestDefaultPrettyPrinter::testWithLineFeed
    public void testWithLineFeed() throws IOException
    {
        PrettyPrinter pp = new DefaultPrettyPrinter()
        .withObjectIndenter(new DefaultIndenter().withLinefeed("\n"));
        assertEquals(
            "{\n" +
            "  \"name\" : \"John Doe\",\n" +
            "  \"age\" : 3.14\n" +
            "}", _printTestData(pp));
    }

// com.fasterxml.jackson.core.util.TestDefaultPrettyPrinter::testWithIndent
    public void testWithIndent() throws IOException
    {
        PrettyPrinter pp = new DefaultPrettyPrinter()
        .withObjectIndenter(new DefaultIndenter().withIndent(" "));
        assertEquals(
            "{\n" +
            " \"name\" : \"John Doe\",\n" +
            " \"age\" : 3.14\n" +
            "}", _printTestData(pp));
    }

// com.fasterxml.jackson.core.util.TestDefaultPrettyPrinter::testUnixLinefeed
    public void testUnixLinefeed() throws IOException
    {
        PrettyPrinter pp = new DefaultPrettyPrinter()
                .withObjectIndenter(new DefaultIndenter("  ", "\n"));
        assertEquals(
            "{\n" +
            "  \"name\" : \"John Doe\",\n" +
            "  \"age\" : 3.14\n" +
            "}", _printTestData(pp));
    }

// com.fasterxml.jackson.core.util.TestDefaultPrettyPrinter::testWindowsLinefeed
    public void testWindowsLinefeed() throws IOException
    {
        PrettyPrinter pp = new DefaultPrettyPrinter()
        .withObjectIndenter(new DefaultIndenter("  ", "\r\n"));
        assertEquals(
            "{\r\n" +
            "  \"name\" : \"John Doe\",\r\n" +
            "  \"age\" : 3.14\r\n" +
            "}", _printTestData(pp));
    }

// com.fasterxml.jackson.core.util.TestDefaultPrettyPrinter::testTabIndent
    public void testTabIndent() throws IOException
    {
        PrettyPrinter pp = new DefaultPrettyPrinter()
        .withObjectIndenter(new DefaultIndenter("\t", "\n"));
        assertEquals(
            "{\n" +
            "\t\"name\" : \"John Doe\",\n" +
            "\t\"age\" : 3.14\n" +
            "}", _printTestData(pp));
    }

// com.fasterxml.jackson.core.util.TestDelegates::testParserDelegate
    public void testParserDelegate() throws IOException
    {
        JsonParser jp = new JsonFactory().createParser("[ 1, true ]");
        assertNull(jp.getCurrentToken());
        assertToken(JsonToken.START_ARRAY, jp.nextToken());
        assertEquals("[", jp.getText());
        assertToken(JsonToken.VALUE_NUMBER_INT, jp.nextToken());
        assertEquals(1, jp.getIntValue());
        assertToken(JsonToken.VALUE_TRUE, jp.nextToken());
        assertTrue(jp.getBooleanValue());
        assertToken(JsonToken.END_ARRAY, jp.nextToken());
        jp.close();
        assertTrue(jp.isClosed());
    }

// com.fasterxml.jackson.core.util.TestDelegates::testGeneratorDelegate
    public void testGeneratorDelegate() throws IOException
    {
        StringWriter sw = new StringWriter();
        JsonGenerator jg = new JsonFactory().createGenerator(sw);
        jg.writeStartArray();
        jg.writeNumber(13);
        jg.writeNull();
        jg.writeBoolean(false);
        jg.writeEndArray();
        jg.close();
        assertTrue(jg.isClosed());        
        assertEquals("[13,null,false]", sw.toString());
    }

// com.fasterxml.jackson.core.util.TestDelegates::testNotDelegateCopyMethods
    public void testNotDelegateCopyMethods() throws IOException
    {
        JsonParser jp = new JsonFactory().createParser("[{\"a\":[1,2,{\"b\":3}],\"c\":\"d\"},{\"e\":false},null]");
        StringWriter sw = new StringWriter();
        JsonGenerator jg = new JsonGeneratorDelegate(new JsonFactory().createGenerator(sw), false) {
            @Override
            public void writeFieldName(String name) throws IOException, JsonGenerationException {
                super.writeFieldName(name+"-test");
                super.writeBoolean(true);
                super.writeFieldName(name);
            }
        };
        jp.nextToken();
        jg.copyCurrentStructure(jp);
        jg.flush();
        assertEquals("[{\"a-test\":true,\"a\":[1,2,{\"b-test\":true,\"b\":3}],\"c-test\":true,\"c\":\"d\"},{\"e-test\":true,\"e\":false},null]", sw.toString());
        jp.close();
        jg.close();
    }

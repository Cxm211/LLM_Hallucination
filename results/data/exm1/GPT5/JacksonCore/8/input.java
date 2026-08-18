// buggy code
    public char[] getTextBuffer()
    {
        // Are we just using shared input buffer?
        if (_inputStart >= 0) return _inputBuffer;
        if (_resultArray != null)  return _resultArray;
        if (_resultString != null) {
            return (_resultArray = _resultString.toCharArray());
        }
        // Nope; but does it fit in just one segment?
        if (!_hasSegments)  return _currentSegment;
        // Nope, need to have/create a non-segmented array and return it
        return contentsAsArray();
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

// com.fasterxml.jackson.core.format.TestJsonFormatDetection::testSimpleValidArray
    public void testSimpleValidArray() throws Exception
    {
        JsonFactory jsonF = new JsonFactory();
        DataFormatDetector detector = new DataFormatDetector(jsonF);
        final String ARRAY_JSON = "[ 1, 2 ]";
        DataFormatMatcher matcher = detector.findFormat(new ByteArrayInputStream(ARRAY_JSON.getBytes("UTF-8")));
        
        assertTrue(matcher.hasMatch());
        assertEquals("JSON", matcher.getMatchedFormatName());
        assertSame(jsonF, matcher.getMatch());
        
        assertEquals(MatchStrength.SOLID_MATCH, matcher.getMatchStrength());
        
        JsonParser jp = matcher.createParserWithMatch();
        assertToken(JsonToken.START_ARRAY, jp.nextToken());
        assertToken(JsonToken.VALUE_NUMBER_INT, jp.nextToken());
        assertToken(JsonToken.VALUE_NUMBER_INT, jp.nextToken());
        assertToken(JsonToken.END_ARRAY, jp.nextToken());
        assertNull(jp.nextToken());
        jp.close();
    }

// com.fasterxml.jackson.core.format.TestJsonFormatDetection::testSimpleValidObject
    public void testSimpleValidObject() throws Exception
    {
        JsonFactory jsonF = new JsonFactory();
        DataFormatDetector detector = new DataFormatDetector(jsonF);
        final String JSON = "{  \"field\" : true }";
        DataFormatMatcher matcher = detector.findFormat(new ByteArrayInputStream(JSON.getBytes("UTF-8")));
        
        assertTrue(matcher.hasMatch());
        assertEquals("JSON", matcher.getMatchedFormatName());
        assertSame(jsonF, matcher.getMatch());
        
        assertEquals(MatchStrength.SOLID_MATCH, matcher.getMatchStrength());
        
        JsonParser jp = matcher.createParserWithMatch();
        assertToken(JsonToken.START_OBJECT, jp.nextToken());
        assertToken(JsonToken.FIELD_NAME, jp.nextToken());
        assertEquals("field", jp.getCurrentName());
        assertToken(JsonToken.VALUE_TRUE, jp.nextToken());
        assertToken(JsonToken.END_OBJECT, jp.nextToken());
        assertNull(jp.nextToken());
        jp.close();
    }

// com.fasterxml.jackson.core.format.TestJsonFormatDetection::testSimpleValidString
    public void testSimpleValidString() throws Exception
    {
        JsonFactory jsonF = new JsonFactory();
        DataFormatDetector detector = new DataFormatDetector(jsonF);
        final String JSON = "\"JSON!\"";
        DataFormatMatcher matcher = detector.findFormat(new ByteArrayInputStream(JSON.getBytes("UTF-8")));
        
        assertTrue(matcher.hasMatch());
        assertEquals("JSON", matcher.getMatchedFormatName());
        assertSame(jsonF, matcher.getMatch());
        assertEquals(MatchStrength.WEAK_MATCH, matcher.getMatchStrength());
        JsonParser jp = matcher.createParserWithMatch();
        assertToken(JsonToken.VALUE_STRING, jp.nextToken());
        assertEquals("JSON!", jp.getText());
        assertNull(jp.nextToken());
        jp.close();
    }

// com.fasterxml.jackson.core.format.TestJsonFormatDetection::testSimpleInvalid
    public void testSimpleInvalid() throws Exception
    {
        DataFormatDetector detector = new DataFormatDetector(new JsonFactory());
        final String NON_JSON = "<root />";
        DataFormatMatcher matcher = detector.findFormat(new ByteArrayInputStream(NON_JSON.getBytes("UTF-8")));
        
        assertFalse(matcher.hasMatch());
        
        assertEquals(MatchStrength.INCONCLUSIVE, matcher.getMatchStrength());
        
        assertNull(matcher.createParserWithMatch());
    }

// com.fasterxml.jackson.core.io.TestJDKSerializable::testLocationSerializability
    public void testLocationSerializability() throws Exception
    {
        JsonFactory jf = new JsonFactory();
        JsonParser jp = jf.createParser("  { }");
        assertToken(JsonToken.START_OBJECT, jp.nextToken());
        JsonLocation loc = jp.getCurrentLocation();

        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        ObjectOutputStream out = new ObjectOutputStream(bytes);
        out.writeObject(loc);
        out.close();
        byte[] stuff = bytes.toByteArray();
        
        ObjectInputStream obIn = new ObjectInputStream(new ByteArrayInputStream(stuff));
        JsonLocation loc2 = (JsonLocation) obIn.readObject();
        assertNotNull(loc2);
        
        assertEquals(loc.getLineNr(), loc2.getLineNr());
        assertEquals(loc.getColumnNr(), loc2.getColumnNr());
        jp.close();
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

// com.fasterxml.jackson.core.json.TestComments::testDefaultSettings
    public void testDefaultSettings()
        throws Exception
    {
        JsonFactory jf = new JsonFactory();
        assertFalse(jf.isEnabled(JsonParser.Feature.ALLOW_COMMENTS));
        JsonParser jp = jf.createParser(new StringReader("[ 1 ]"));
        assertFalse(jp.isEnabled(JsonParser.Feature.ALLOW_COMMENTS));
        jp.close();
    }

// com.fasterxml.jackson.core.json.TestComments::testCommentsDisabled
    public void testCommentsDisabled()
        throws Exception
    {
        _testDisabled(DOC_WITH_SLASHSTAR_COMMENT, false);
        _testDisabled(DOC_WITH_SLASHSLASH_COMMENT, false);
        _testDisabled(DOC_WITH_SLASHSTAR_COMMENT, true);
        _testDisabled(DOC_WITH_SLASHSLASH_COMMENT, true);
    }

// com.fasterxml.jackson.core.json.TestComments::testCommentsEnabled
    public void testCommentsEnabled()
        throws Exception
    {
        _testEnabled(DOC_WITH_SLASHSTAR_COMMENT, false);
        _testEnabled(DOC_WITH_SLASHSLASH_COMMENT, false);
        _testEnabled(DOC_WITH_SLASHSTAR_COMMENT, true);
        _testEnabled(DOC_WITH_SLASHSLASH_COMMENT, true);
    }

// com.fasterxml.jackson.core.json.TestComments::testCommentsWithUTF8
    public void testCommentsWithUTF8() throws Exception
    {
        final String JSON = "\n [ \"bar? \u00a9\" ]\n";
        _testWithUTF8Chars(JSON, false);
        _testWithUTF8Chars(JSON, true);
    }

// com.fasterxml.jackson.core.json.TestComments::testYAMLCommentsBytes
    public void testYAMLCommentsBytes() throws Exception {
        JsonFactory f = new JsonFactory();
        f.configure(JsonParser.Feature.ALLOW_YAML_COMMENTS, true);
        _testYAMLComments(f, true);
        _testCommentsBeforePropValue(f, true, "# foo\n");
    }

// com.fasterxml.jackson.core.json.TestComments::testYAMLCommentsChars
    public void testYAMLCommentsChars() throws Exception {
        JsonFactory f = new JsonFactory();
        f.configure(JsonParser.Feature.ALLOW_YAML_COMMENTS, true);
        _testYAMLComments(f, false);
        final String COMMENT = "# foo\n";
        _testCommentsBeforePropValue(f, false, COMMENT);
        _testCommentsBetweenArrayValues(f, false, COMMENT);
    }

// com.fasterxml.jackson.core.json.TestComments::testCCommentsBytes
    public void testCCommentsBytes() throws Exception {
        JsonFactory f = new JsonFactory();
        f.configure(JsonParser.Feature.ALLOW_COMMENTS, true);
        final String COMMENT = "\n";
        _testCommentsBeforePropValue(f, true, COMMENT);
    }

// com.fasterxml.jackson.core.json.TestComments::testCCommentsChars
    public void testCCommentsChars() throws Exception {
        JsonFactory f = new JsonFactory();
        f.configure(JsonParser.Feature.ALLOW_COMMENTS, true);
        final String COMMENT = "\n";
        _testCommentsBeforePropValue(f, false, COMMENT);
    }

// com.fasterxml.jackson.core.json.TestComments::testCppCommentsBytes
    public void testCppCommentsBytes() throws Exception {
        JsonFactory f = new JsonFactory();
        f.configure(JsonParser.Feature.ALLOW_COMMENTS, true);
        final String COMMENT = "// foo\n";
        _testCommentsBeforePropValue(f, true, COMMENT);
    }

// com.fasterxml.jackson.core.json.TestComments::testCppCommentsChars
    public void testCppCommentsChars() throws Exception {
        JsonFactory f = new JsonFactory();
        f.configure(JsonParser.Feature.ALLOW_COMMENTS, true);
        final String COMMENT = "// foo \n";
        _testCommentsBeforePropValue(f, false, COMMENT);
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

// com.fasterxml.jackson.core.json.TestLocation::testSimpleInitialOffsets
    public void testSimpleInitialOffsets() throws Exception
    {
        final JsonFactory f = new JsonFactory();
        JsonLocation loc;
        JsonParser p;
        final String DOC = "{ }";

        
        p = f.createParser(DOC);
        assertToken(JsonToken.START_OBJECT, p.nextToken());

        loc = p.getTokenLocation();
        assertEquals(-1L, loc.getByteOffset());
        assertEquals(0L, loc.getCharOffset());
        assertEquals(1, loc.getLineNr());
        assertEquals(1, loc.getColumnNr());
        
        loc = p.getCurrentLocation();
        assertEquals(-1L, loc.getByteOffset());
        assertEquals(1L, loc.getCharOffset());
        assertEquals(1, loc.getLineNr());
        assertEquals(2, loc.getColumnNr());

        p.close();

        
        
        p = f.createParser(DOC.getBytes("UTF-8"));
        assertToken(JsonToken.START_OBJECT, p.nextToken());

        loc = p.getTokenLocation();
        assertEquals(0L, loc.getByteOffset());
        assertEquals(-1L, loc.getCharOffset());
        assertEquals(1, loc.getLineNr());
        assertEquals(1, loc.getColumnNr());
        
        loc = p.getCurrentLocation();
        assertEquals(1L, loc.getByteOffset());
        assertEquals(-1L, loc.getCharOffset());
        assertEquals(1, loc.getLineNr());
        assertEquals(2, loc.getColumnNr());

        p.close();
    }

// com.fasterxml.jackson.core.json.TestLocation::testOffsetWithInputOffset
    public void testOffsetWithInputOffset() throws Exception
    {
        final JsonFactory f = new JsonFactory();
        JsonLocation loc;
        JsonParser p;
        
        byte[] b = "   { }  ".getBytes("UTF-8");

        
        p = f.createParser(b, 3, b.length-5);
        assertToken(JsonToken.START_OBJECT, p.nextToken());

        loc = p.getTokenLocation();
        assertEquals(0L, loc.getByteOffset());
        assertEquals(-1L, loc.getCharOffset());
        assertEquals(1, loc.getLineNr());
        assertEquals(1, loc.getColumnNr());
        
        loc = p.getCurrentLocation();
        assertEquals(1L, loc.getByteOffset());
        assertEquals(-1L, loc.getCharOffset());
        assertEquals(1, loc.getLineNr());
        assertEquals(2, loc.getColumnNr());

        p.close();
    }

// com.fasterxml.jackson.core.json.TestNextXxx::testIsNextTokenName
    public void testIsNextTokenName() throws Exception
    {
        _testIsNextTokenName1(false);
        _testIsNextTokenName1(true);
        _testIsNextTokenName2(false);
        _testIsNextTokenName2(true);
        _testIsNextTokenName3(false);
        _testIsNextTokenName3(true);
    }

// com.fasterxml.jackson.core.json.TestNextXxx::testIssue34
    public void testIssue34() throws Exception
    {
        _testIssue34(false);
        _testIssue34(true);
    }

// com.fasterxml.jackson.core.json.TestNextXxx::testIssue38
    public void testIssue38() throws Exception
    {
        _testIssue38(false);
        _testIssue38(true);
    }

// com.fasterxml.jackson.core.json.TestNextXxx::testNextNameWithLongContent
    public void testNextNameWithLongContent() throws Exception
    {
        final JsonFactory jf = new JsonFactory();

        _testLong(jf, false);
        _testLong(jf, true);
    }

// com.fasterxml.jackson.core.json.TestNumericValues::testSimpleBoolean
    public void testSimpleBoolean() throws Exception
    {
        JsonParser jp = FACTORY.createParser("[ true ]");
        assertToken(JsonToken.START_ARRAY, jp.nextToken());
        assertToken(JsonToken.VALUE_TRUE, jp.nextToken());
        assertEquals(true, jp.getBooleanValue());
        jp.close();
    }

// com.fasterxml.jackson.core.json.TestNumericValues::testSimpleInt
    public void testSimpleInt() throws Exception
    {
        for (int EXP_I : new int[] { 1234, -999, 0, 1, -2 }) {
            _testSimpleInt(EXP_I, false);
            _testSimpleInt(EXP_I, true);
        }
    }

// com.fasterxml.jackson.core.json.TestNumericValues::testIntRange
    public void testIntRange() throws Exception
    {
        
        for (int i = 0; i < 2; ++i) {
            String input = "[ "+Integer.MAX_VALUE+","+Integer.MIN_VALUE+" ]";
            JsonParser jp;
            if (i == 0) {
                jp = FACTORY.createParser(input);                
            } else {
                jp = createParserUsingStream(input, "UTF-8");
            }
            assertToken(JsonToken.START_ARRAY, jp.nextToken());
            assertToken(JsonToken.VALUE_NUMBER_INT, jp.nextToken());
            assertEquals(JsonParser.NumberType.INT, jp.getNumberType());
            assertEquals(Integer.MAX_VALUE, jp.getIntValue());
    
            assertToken(JsonToken.VALUE_NUMBER_INT, jp.nextToken());
            assertEquals(JsonParser.NumberType.INT, jp.getNumberType());
            assertEquals(Integer.MIN_VALUE, jp.getIntValue());
            jp.close();
        }
    }

// com.fasterxml.jackson.core.json.TestNumericValues::testSimpleLong
    public void testSimpleLong() throws Exception
    {
        long EXP_L = 12345678907L;

        JsonParser jp = FACTORY.createParser("[ "+EXP_L+" ]");
        assertToken(JsonToken.START_ARRAY, jp.nextToken());
        assertToken(JsonToken.VALUE_NUMBER_INT, jp.nextToken());
        
        assertEquals(JsonParser.NumberType.LONG, jp.getNumberType());
        assertEquals(""+EXP_L, jp.getText());

        assertEquals(EXP_L, jp.getLongValue());
        
        try {
            jp.getIntValue();
        } catch (JsonParseException jpe) {
            verifyException(jpe, "out of range");
        }
        assertEquals((double) EXP_L, jp.getDoubleValue());
        assertEquals(BigDecimal.valueOf((long) EXP_L), jp.getDecimalValue());
        jp.close();
    }

// com.fasterxml.jackson.core.json.TestNumericValues::testLongRange
    public void testLongRange() throws Exception
    {
        for (int i = 0; i < 2; ++i) {
            long belowMinInt = -1L + Integer.MIN_VALUE;
            long aboveMaxInt = 1L + Integer.MAX_VALUE;
            String input = "[ "+Long.MAX_VALUE+","+Long.MIN_VALUE+","+aboveMaxInt+", "+belowMinInt+" ]";
            JsonParser jp;
            if (i == 0) {
                jp = FACTORY.createParser(input);                
            } else {
                jp = this.createParserUsingStream(input, "UTF-8");
            }
            assertToken(JsonToken.START_ARRAY, jp.nextToken());
            assertToken(JsonToken.VALUE_NUMBER_INT, jp.nextToken());
            assertEquals(JsonParser.NumberType.LONG, jp.getNumberType());
            assertEquals(Long.MAX_VALUE, jp.getLongValue());
        
            assertToken(JsonToken.VALUE_NUMBER_INT, jp.nextToken());
            assertEquals(JsonParser.NumberType.LONG, jp.getNumberType());
            assertEquals(Long.MIN_VALUE, jp.getLongValue());

            assertToken(JsonToken.VALUE_NUMBER_INT, jp.nextToken());
            assertEquals(JsonParser.NumberType.LONG, jp.getNumberType());
            assertEquals(aboveMaxInt, jp.getLongValue());

            assertToken(JsonToken.VALUE_NUMBER_INT, jp.nextToken());
            assertEquals(JsonParser.NumberType.LONG, jp.getNumberType());
            assertEquals(belowMinInt, jp.getLongValue());

            
            assertToken(JsonToken.END_ARRAY, jp.nextToken());        
            jp.close();
        }
    }

// com.fasterxml.jackson.core.json.TestNumericValues::testBigDecimalRange
    public void testBigDecimalRange()
        throws Exception
    {
        for (int i = 0; i < 2; ++i) {
            
            BigInteger small = new BigDecimal(Long.MIN_VALUE).toBigInteger();
            small = small.subtract(BigInteger.ONE);
            BigInteger big = new BigDecimal(Long.MAX_VALUE).toBigInteger();
            big = big.add(BigInteger.ONE);
            String input = "[ "+small+"  ,  "+big+"]";
            JsonParser jp;
            if (i == 0) {
                jp = FACTORY.createParser(input);                
            } else {
                jp = this.createParserUsingStream(input, "UTF-8");
            }
            assertToken(JsonToken.START_ARRAY, jp.nextToken());
            assertToken(JsonToken.VALUE_NUMBER_INT, jp.nextToken());
            assertEquals(JsonParser.NumberType.BIG_INTEGER, jp.getNumberType());
            assertEquals(small, jp.getBigIntegerValue());
            assertToken(JsonToken.VALUE_NUMBER_INT, jp.nextToken());
            assertEquals(JsonParser.NumberType.BIG_INTEGER, jp.getNumberType());
            assertEquals(big, jp.getBigIntegerValue());
            assertToken(JsonToken.END_ARRAY, jp.nextToken());        
            jp.close();
        }
    }

// com.fasterxml.jackson.core.json.TestNumericValues::testBigNumbers
    public void testBigNumbers() throws Exception
    {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 520; ++i) { 
            sb.append('1');
        }
        final String NUMBER_STR = sb.toString();
        BigInteger biggie = new BigInteger(NUMBER_STR);
        
        for (int i = 0; i < 2; ++i) {
            JsonParser jp;
            if (i == 0) {
                jp = FACTORY.createParser(NUMBER_STR);                
            } else {
                jp = this.createParserUsingStream(NUMBER_STR, "UTF-8");
            }
            assertToken(JsonToken.VALUE_NUMBER_INT, jp.nextToken());
            assertEquals(JsonParser.NumberType.BIG_INTEGER, jp.getNumberType());
            assertEquals(NUMBER_STR, jp.getText());
            assertEquals(biggie, jp.getBigIntegerValue());
            jp.close();
        }
    }

// com.fasterxml.jackson.core.json.TestNumericValues::testSimpleDouble
    public void testSimpleDouble() throws Exception
    {
        final String[] INPUTS = new String[] {
            "1234.00", "2.1101567E-16", "1.0e5", "0.0", "1.0", "-1.0", 
            "-0.5", "-12.9", "-999.0",
            "2.5e+5", "9e4", "-12e-3", "0.25",
        };
        for (int input = 0; input < 2; ++input) {
            for (int i = 0; i < INPUTS.length; ++i) {

                
                
                String STR = INPUTS[i];
                double EXP_D = Double.parseDouble(STR);
                String DOC = "["+STR+"]";

                JsonParser jp;
                
                if (input == 0) {
                    jp = createParserUsingStream(DOC, "UTF-8");
                } else {
                    jp = FACTORY.createParser(DOC);
                }
                assertToken(JsonToken.START_ARRAY, jp.nextToken());
                assertToken(JsonToken.VALUE_NUMBER_FLOAT, jp.nextToken());
                assertEquals(STR, jp.getText());
                assertEquals(EXP_D, jp.getDoubleValue());
                assertToken(JsonToken.END_ARRAY, jp.nextToken());
                assertNull(jp.nextToken());
                jp.close();

                
                if (input == 0) {
                    jp = createParserUsingStream(STR, "UTF-8");
                } else {
                    jp = FACTORY.createParser(STR);
                }
                JsonToken t = null;

                try {
                    t = jp.nextToken();
                } catch (Exception e) {
                    throw new Exception("Failed to parse input '"+STR+"' (parser of type "+jp.getClass().getSimpleName()+")", e);
                }
                
                assertToken(JsonToken.VALUE_NUMBER_FLOAT, t);
                assertEquals(STR, jp.getText());
                assertNull(jp.nextToken());
                jp.close();
            }
        }
    }

// com.fasterxml.jackson.core.json.TestNumericValues::testNumbers
    public void testNumbers() throws Exception
    {
        final String DOC = "[ -13, 8100200300, 13.5, 0.00010, -2.033 ]";

        for (int input = 0; input < 2; ++input) {
            JsonParser jp;

            if (input == 0) {
                jp = createParserUsingStream(DOC, "UTF-8");
            } else {
                jp = FACTORY.createParser(DOC);
            }

            assertToken(JsonToken.START_ARRAY, jp.nextToken());
            
            assertToken(JsonToken.VALUE_NUMBER_INT, jp.nextToken());
            assertEquals(-13, jp.getIntValue());
            assertEquals(-13L, jp.getLongValue());
            assertEquals(-13., jp.getDoubleValue());
            assertEquals("-13", jp.getText());
            
            assertToken(JsonToken.VALUE_NUMBER_INT, jp.nextToken());
            assertEquals(8100200300L, jp.getLongValue());
            
            try {
                 jp.getIntValue();
                fail("Expected an exception for overflow");
            } catch (Exception e) {
                verifyException(e, "out of range of int");
            }
            assertEquals(8100200300., jp.getDoubleValue());
            assertEquals("8100200300", jp.getText());
            
            assertToken(JsonToken.VALUE_NUMBER_FLOAT, jp.nextToken());
            assertEquals(13, jp.getIntValue());
            assertEquals(13L, jp.getLongValue());
            assertEquals(13.5, jp.getDoubleValue());
            assertEquals("13.5", jp.getText());
            
            assertToken(JsonToken.VALUE_NUMBER_FLOAT, jp.nextToken());
            assertEquals(0, jp.getIntValue());
            assertEquals(0L, jp.getLongValue());
            assertEquals(0.00010, jp.getDoubleValue());
            assertEquals("0.00010", jp.getText());
            
            assertToken(JsonToken.VALUE_NUMBER_FLOAT, jp.nextToken());
            assertEquals(-2, jp.getIntValue());
            assertEquals(-2L, jp.getLongValue());
            assertEquals(-2.033, jp.getDoubleValue());
            assertEquals("-2.033", jp.getText());

            assertToken(JsonToken.END_ARRAY, jp.nextToken());

            jp.close();
        }
    }

// com.fasterxml.jackson.core.json.TestNumericValues::testLongOverflow
    public void testLongOverflow() throws Exception
    {
        BigInteger below = BigInteger.valueOf(Long.MIN_VALUE);
        below = below.subtract(BigInteger.ONE);
        BigInteger above = BigInteger.valueOf(Long.MAX_VALUE);
        above = above.add(BigInteger.ONE);

        String DOC_BELOW = below.toString() + " ";
        String DOC_ABOVE = below.toString() + " ";
        for (int input = 0; input < 2; ++input) {
            JsonParser jp;

            if (input == 0) {
                jp = createParserUsingStream(DOC_BELOW, "UTF-8");
            } else {
                jp = FACTORY.createParser(DOC_BELOW);
            }
            jp.nextToken();
            try {
                long x = jp.getLongValue();
                fail("Expected an exception for underflow (input "+jp.getText()+"): instead, got long value: "+x);
            } catch (JsonParseException e) {
                verifyException(e, "out of range of long");
            }
            jp.close();

            if (input == 0) {
                jp = createParserUsingStream(DOC_ABOVE, "UTF-8");
            } else {
                jp = createParserUsingReader(DOC_ABOVE);
            }
            jp.nextToken();
            try {
                long x = jp.getLongValue();
                fail("Expected an exception for underflow (input "+jp.getText()+"): instead, got long value: "+x);
            } catch (JsonParseException e) {
                verifyException(e, "out of range of long");
            }
            jp.close();
            
        }
    }

// com.fasterxml.jackson.core.json.TestNumericValues::testParsingOfLongerSequences
    public void testParsingOfLongerSequences()
        throws Exception
    {
        double[] values = new double[] { 0.01, -10.5, 2.1e9, 4.0e-8 };
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < values.length; ++i) {
            if (i > 0) {
                sb.append(',');
            }
            sb.append(values[i]);
        }
        String segment = sb.toString();

        int COUNT = 1000;
        sb = new StringBuilder(COUNT * segment.length() + 20);
        sb.append("[");
        for (int i = 0; i < COUNT; ++i) {
            if (i > 0) {
                sb.append(',');
            }
            sb.append(segment);
            sb.append('\n');
            
            int x = (i & 3);
            if (i > 300) {
                x += i % 5;
            }
            while (--x > 0) {
                sb.append(' ');
            }
        }
        sb.append("]");
        String DOC = sb.toString();

        for (int input = 0; input < 2; ++input) {
            JsonParser jp;

            if (input == 0) {
                jp = createParserUsingStream(DOC, "UTF-8");
            } else {
                jp = FACTORY.createParser(DOC);
            }

            assertToken(JsonToken.START_ARRAY, jp.nextToken());
            for (int i = 0; i < COUNT; ++i) {
                for (double d : values) {
                    assertToken(JsonToken.VALUE_NUMBER_FLOAT, jp.nextToken());
                    assertEquals(d, jp.getDoubleValue());
                }
            }
            assertToken(JsonToken.END_ARRAY, jp.nextToken());
            jp.close();
        }
    }

// com.fasterxml.jackson.core.json.TestNumericValues::testLongNumbers
    public void testLongNumbers() throws Exception
    {
        StringBuilder sb = new StringBuilder(9000);
        for (int i = 0; i < 9000; ++i) {
            sb.append('9');
        }
        String NUM = sb.toString();
        
        JsonFactory f = new JsonFactory();
        _testLongNumbers(f, NUM, false);
        _testLongNumbers(f, NUM, true);
    }

// com.fasterxml.jackson.core.json.TestNumericValues::testLongNumbers2
    public void testLongNumbers2() throws Exception
    {
        StringBuilder input = new StringBuilder();
        
        input.append('-');
        for (int i = 0; i < 2100; i++) {
            input.append(1);
        }
        final String DOC = input.toString();
        JsonFactory f = new JsonFactory();
        _testIssue160LongNumbers(f, DOC, false);
        _testIssue160LongNumbers(f, DOC, true);
    }

// com.fasterxml.jackson.core.json.TestNumericValues::testParsingOfLongerSequencesWithNonNumeric
    public void testParsingOfLongerSequencesWithNonNumeric() throws Exception
    {
        JsonFactory factory = new JsonFactory();
        factory.enable(JsonParser.Feature.ALLOW_NON_NUMERIC_NUMBERS);
        double[] values = new double[] {
                0.01, -10.5, 2.1e9, 4.0e-8,
                Double.NaN, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY
        };
        for (int i = 0; i < values.length; ++i) {
            int COUNT = 4096;
            
            int VCOUNT = 2 * COUNT;
            String arrayJson = toJsonArray(values[i], VCOUNT);
            StringBuilder sb = new StringBuilder(COUNT + arrayJson.length() + 20);
            for (int j = 0; j < COUNT; ++j) {
                sb.append(' ');
            }
            sb.append(arrayJson);
            String DOC = sb.toString();
            for (int input = 0; input < 2; ++input) {
                JsonParser jp;
                if (input == 0) {
                    jp = createParserUsingStream(factory, DOC, "UTF-8");
                } else {
                    jp = factory.createParser(DOC);
                }
                assertToken(JsonToken.START_ARRAY, jp.nextToken());
                for (int j = 0; j < VCOUNT; ++j) {
                    assertToken(JsonToken.VALUE_NUMBER_FLOAT, jp.nextToken());
                    assertEquals(values[i], jp.getDoubleValue());
                }
                assertToken(JsonToken.END_ARRAY, jp.nextToken());
                jp.close();
            }
        }
    }

// com.fasterxml.jackson.core.json.TestNumericValues::testInvalidBooleanAccess
    public void testInvalidBooleanAccess() throws Exception
    {
        JsonParser jp = FACTORY.createParser("[ \"abc\" ]");
        assertToken(JsonToken.START_ARRAY, jp.nextToken());
        assertToken(JsonToken.VALUE_STRING, jp.nextToken());
        try {
            jp.getBooleanValue();
            fail("Expected error trying to call getBooleanValue on non-boolean value");
        } catch (JsonParseException e) {
            verifyException(e, "not of boolean type");
        }
        jp.close();
    }

// com.fasterxml.jackson.core.json.TestNumericValues::testInvalidIntAccess
    public void testInvalidIntAccess() throws Exception
    {
        JsonParser jp = FACTORY.createParser("[ \"abc\" ]");
        assertToken(JsonToken.START_ARRAY, jp.nextToken());
        assertToken(JsonToken.VALUE_STRING, jp.nextToken());
        try {
            jp.getIntValue();
            fail("Expected error trying to call getIntValue on non-numeric value");
        } catch (JsonParseException e) {
            verifyException(e, "can not use numeric value accessors");
        }
        jp.close();
    }

// com.fasterxml.jackson.core.json.TestParserDupHandling::testSimpleDupsDisabled
    public void testSimpleDupsDisabled() throws Exception
    {
        
        final JsonFactory f = new JsonFactory();
        assertFalse(f.isEnabled(JsonParser.Feature.STRICT_DUPLICATE_DETECTION));
        for (String doc : DUP_DOCS) {
            _testSimpleDupsOk(doc, f, false);
            _testSimpleDupsOk(doc, f, true);
        }
    }

// com.fasterxml.jackson.core.json.TestParserDupHandling::testSimpleDupsBytes
    public void testSimpleDupsBytes() throws Exception
    {
        JsonFactory nonDupF = new JsonFactory();
        JsonFactory dupF = new JsonFactory();
        dupF.enable(JsonParser.Feature.STRICT_DUPLICATE_DETECTION);
        for (String doc : DUP_DOCS) {
            
            _testSimpleDupsFail(doc, dupF, true, "a", false);

            
            _testSimpleDupsFail(doc, nonDupF, true, "a", true);
        }
    }

// com.fasterxml.jackson.core.json.TestParserDupHandling::testSimpleDupsChars
    public void testSimpleDupsChars() throws Exception
    {
        JsonFactory nonDupF = new JsonFactory();
        JsonFactory dupF = new JsonFactory();
        dupF.enable(JsonParser.Feature.STRICT_DUPLICATE_DETECTION);
        for (String doc : DUP_DOCS) {
            _testSimpleDupsFail(doc, dupF, false, "a", false);
            _testSimpleDupsFail(doc, nonDupF, false, "a", true);
        }
    }

// com.fasterxml.jackson.core.json.TestParserErrorHandling::testInvalidKeywordsStream
    public void testInvalidKeywordsStream() throws Exception {
        _testInvalidKeywords(true);
    }

// com.fasterxml.jackson.core.json.TestParserErrorHandling::testInvalidKeywordsReader
    public void testInvalidKeywordsReader() throws Exception {
        _testInvalidKeywords(false);
    }

// com.fasterxml.jackson.core.json.TestParserErrorHandling::testMangledNumbersBytes
    public void testMangledNumbersBytes() throws Exception {
        _testMangledNumbers(true);
    }

// com.fasterxml.jackson.core.json.TestParserErrorHandling::testMangledNumbersChars
    public void testMangledNumbersChars() throws Exception {
        _testMangledNumbers(false);
    }

// com.fasterxml.jackson.core.json.TestParserNonStandard::testSimpleUnquotedBytes
    public void testSimpleUnquotedBytes() throws Exception {
        _testSimpleUnquoted(true);
    }

// com.fasterxml.jackson.core.json.TestParserNonStandard::testSimpleUnquotedChars
    public void testSimpleUnquotedChars() throws Exception {
        _testSimpleUnquoted(false);
    }

// com.fasterxml.jackson.core.json.TestParserNonStandard::testLargeUnquoted
    public void testLargeUnquoted() throws Exception
    {
        _testLargeUnquoted(false);
        _testLargeUnquoted(true);
    }

// com.fasterxml.jackson.core.json.TestParserNonStandard::testSingleQuotesDefault
    public void testSingleQuotesDefault() throws Exception
    {
        _testSingleQuotesDefault(false);
        _testSingleQuotesDefault(true);
    }

// com.fasterxml.jackson.core.json.TestParserNonStandard::testSingleQuotesEnabled
    public void testSingleQuotesEnabled() throws Exception
    {
        _testSingleQuotesEnabled(false);
        _testSingleQuotesEnabled(true);
        _testSingleQuotesEscaped(false);
        _testSingleQuotesEscaped(true);
    }

// com.fasterxml.jackson.core.json.TestParserNonStandard::testNonStandardNameChars
    public void testNonStandardNameChars() throws Exception
    {
        _testNonStandardNameChars(false);
        _testNonStandardNameChars(true);
    }

// com.fasterxml.jackson.core.json.TestParserNonStandard::testNonStandardAnyCharQuoting
    public void testNonStandardAnyCharQuoting() throws Exception
    {
        _testNonStandarBackslashQuoting(false);
        _testNonStandarBackslashQuoting(true);
    }

// com.fasterxml.jackson.core.json.TestParserNonStandard::testLeadingZeroesUTF8
    public void testLeadingZeroesUTF8() throws Exception {
        _testLeadingZeroes(true, false);
        _testLeadingZeroes(true, true);
    }

// com.fasterxml.jackson.core.json.TestParserNonStandard::testLeadingZeroesReader
    public void testLeadingZeroesReader() throws Exception {
        _testLeadingZeroes(false, false);
        _testLeadingZeroes(false, true);
    }

// com.fasterxml.jackson.core.json.TestParserNonStandard::testAllowNaN
    public void testAllowNaN() throws Exception {
        _testAllowNaN(false);
        _testAllowNaN(true);
    }

// com.fasterxml.jackson.core.json.TestParserNonStandard::testAllowInfinity
    public void testAllowInfinity() throws Exception {
        _testAllowInf(false);
        _testAllowInf(true);
    }

// com.fasterxml.jackson.core.json.TestParserOverrides::testTokenAccess
    public void testTokenAccess() throws Exception
    {
        JsonFactory jf = new JsonFactory();
        _testTokenAccess(jf, false);
        _testTokenAccess(jf, true);
    }

// com.fasterxml.jackson.core.json.TestParserOverrides::testCurrentName
    public void testCurrentName() throws Exception
    {
        JsonFactory jf = new JsonFactory();

        
        _testCurrentName(jf, false);

        
        _testCurrentName(jf, true);
    }

// com.fasterxml.jackson.core.json.TestParserSymbols::testSymbolsWithNullBytes
    public void testSymbolsWithNullBytes() throws Exception {
        JsonFactory f = new JsonFactory();
        _testSymbolsWithNull(f, true);
        
        _testSymbolsWithNull(f, true);
    }

// com.fasterxml.jackson.core.json.TestParserSymbols::testSymbolsWithNullChars
    public void testSymbolsWithNullChars() throws Exception {
        JsonFactory f = new JsonFactory();
        _testSymbolsWithNull(f, false);
        _testSymbolsWithNull(f, false);
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

// com.fasterxml.jackson.core.json.TestUnicode::testSurrogates
    public void testSurrogates() throws Exception
    {
        JsonFactory f = new JsonFactory();
        _testSurrogates(f, true);
        _testSurrogates(f, false);
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

// com.fasterxml.jackson.core.json.TestUtf8Parser::testEmptyName
    public void testEmptyName()
        throws Exception
    {
        final String DOC = "{ \"\" : \"\" }";

        JsonParser jp = createParserUsingStream(DOC, "UTF-8");
        assertToken(JsonToken.START_OBJECT, jp.nextToken());
        assertToken(JsonToken.FIELD_NAME, jp.nextToken());
        assertEquals("", jp.getCurrentName());
        assertToken(JsonToken.VALUE_STRING, jp.nextToken());
        assertEquals("", jp.getText());
        assertToken(JsonToken.END_OBJECT, jp.nextToken());
        jp.close();
    }

// com.fasterxml.jackson.core.json.TestUtf8Parser::testUtf8Name2Bytes
    public void testUtf8Name2Bytes() throws Exception
    {
        final String[] NAMES = UTF8_2BYTE_STRINGS;

        for (int i = 0; i < NAMES.length; ++i) {
            String NAME = NAMES[i];
            String DOC = "{ \""+NAME+"\" : 0 }";
            JsonParser jp = createParserUsingStream(DOC, "UTF-8");
            assertToken(JsonToken.START_OBJECT, jp.nextToken());

            assertToken(JsonToken.FIELD_NAME, jp.nextToken());

            assertTrue(jp.hasToken(JsonToken.FIELD_NAME));
            assertTrue(jp.hasTokenId(JsonTokenId.ID_FIELD_NAME));
            
            assertEquals(NAME, jp.getCurrentName());
            assertToken(JsonToken.VALUE_NUMBER_INT, jp.nextToken());
            assertTrue(jp.hasToken(JsonToken.VALUE_NUMBER_INT));
            assertTrue(jp.hasTokenId(JsonTokenId.ID_NUMBER_INT));

            
            assertEquals(NAME, jp.getCurrentName());
            
            assertToken(JsonToken.END_OBJECT, jp.nextToken());
            jp.close();
        }
    }

// com.fasterxml.jackson.core.json.TestUtf8Parser::testUtf8Name3Bytes
    public void testUtf8Name3Bytes() throws Exception
    {
        final String[] NAMES = UTF8_3BYTE_STRINGS;

        for (int i = 0; i < NAMES.length; ++i) {
            String NAME = NAMES[i];
            String DOC = "{ \""+NAME+"\" : true }";

            JsonParser jp = createParserUsingStream(DOC, "UTF-8");
            assertToken(JsonToken.START_OBJECT, jp.nextToken());
            
            assertToken(JsonToken.FIELD_NAME, jp.nextToken());
            assertEquals(NAME, jp.getCurrentName());
            assertToken(JsonToken.VALUE_TRUE, jp.nextToken());
            assertEquals(NAME, jp.getCurrentName());
            
            assertToken(JsonToken.END_OBJECT, jp.nextToken());
            
            jp.close();
        }
    }

// com.fasterxml.jackson.core.json.TestUtf8Parser::testUtf8StringTrivial
    public void testUtf8StringTrivial() throws Exception
    {
        String[] VALUES = UTF8_2BYTE_STRINGS;
        for (int i = 0; i < VALUES.length; ++i) {
            String VALUE = VALUES[i];
            String DOC = "[ \""+VALUE+"\" ]";
            JsonParser jp = createParserUsingStream(DOC, "UTF-8");
            assertToken(JsonToken.START_ARRAY, jp.nextToken());
            assertToken(JsonToken.VALUE_STRING, jp.nextToken());
            String act = getAndVerifyText(jp);
            if (act.length() != VALUE.length()) {
                fail("Failed for value #"+(i+1)+"/"+VALUES.length+": length was "+act.length()+", should be "+VALUE.length());
            }
            assertEquals(VALUE, act);
            assertToken(JsonToken.END_ARRAY, jp.nextToken());
            jp.close();
        }

        VALUES = UTF8_3BYTE_STRINGS;
        for (int i = 0; i < VALUES.length; ++i) {
            String VALUE = VALUES[i];
            String DOC = "[ \""+VALUE+"\" ]";
            JsonParser jp = createParserUsingStream(DOC, "UTF-8");
            assertToken(JsonToken.START_ARRAY, jp.nextToken());
            assertToken(JsonToken.VALUE_STRING, jp.nextToken());
            assertEquals(VALUE, getAndVerifyText(jp));
            assertToken(JsonToken.END_ARRAY, jp.nextToken());
            jp.close();
        }
    }

// com.fasterxml.jackson.core.json.TestUtf8Parser::testUtf8StringValue
    public void testUtf8StringValue() throws Exception
    {
        Random r = new Random(13);
        
        int LEN = 720;
        StringBuilder sb = new StringBuilder(LEN + 20);
        while (sb.length() < LEN) {
            int c;
            if (r.nextBoolean()) { 
                c = 32 + (r.nextInt() & 0x3F);
                if (c == '"' || c == '\\') {
                    c = ' ';
                }
            } else if (r.nextBoolean()) { 
                c = 160 + (r.nextInt() & 0x3FF);
            } else if (r.nextBoolean()) { 
                c = 8000 + (r.nextInt() & 0x7FFF);
            } else { 
                int value = r.nextInt() & 0x3FFFF; 
                sb.append((char) (0xD800 + (value >> 10)));
                c = (0xDC00 + (value & 0x3FF));

            }
            sb.append((char) c);
        }

        ByteArrayOutputStream bout = new ByteArrayOutputStream(LEN);
        OutputStreamWriter out = new OutputStreamWriter(bout, "UTF-8");
        out.write("[\"");
        String VALUE = sb.toString();
        out.write(VALUE);
        out.write("\"]");
        out.close();

        byte[] data = bout.toByteArray();

        JsonParser jp = new JsonFactory().createParser(new ByteArrayInputStream(data));
        assertToken(JsonToken.START_ARRAY, jp.nextToken());
        assertToken(JsonToken.VALUE_STRING, jp.nextToken());
        String act = jp.getText();

        assertEquals(VALUE.length(), act.length());
        assertEquals(VALUE, act);
        jp.close();
    }

// com.fasterxml.jackson.core.json.TestUtf8Parser::testNextFieldName
	public void testNextFieldName() throws IOException
	{
		JsonFactory f = new JsonFactory();
		SerializedString id = new SerializedString("id");

		ByteArrayOutputStream os = new ByteArrayOutputStream();
		os.write('{');
		for (int i = 0; i < 3994; i++) {
			os.write(' ');
		}
		os.write("\"id\":2".getBytes("UTF-8"));
		os.write('}');

		JsonParser parser = f.createParser(new ByteArrayInputStream(os.toByteArray()));
		assertEquals(parser.nextToken(), JsonToken.START_OBJECT);
		assertTrue(parser.nextFieldName(id));
		assertEquals(parser.nextToken(), JsonToken.VALUE_NUMBER_INT);
		assertEquals(parser.nextToken(), JsonToken.END_OBJECT);
		parser.close();
	}

// com.fasterxml.jackson.core.json.TestValueConversions::testAsInt
    public void testAsInt() throws Exception
    {
        final String input = "[ 1, -3, 4.98, true, false, null, \"-17\", \"foo\" ]";
        for (int i = 0; i < 2; ++i) {
            JsonParser jp;
            if (i == 0) {
                jp = createParserUsingReader(input);                
            } else {
                jp = this.createParserUsingStream(input, "UTF-8");
            }
            assertToken(JsonToken.START_ARRAY, jp.nextToken());
            assertEquals(0, jp.getValueAsLong());
            assertEquals(9, jp.getValueAsLong(9));

            assertToken(JsonToken.VALUE_NUMBER_INT, jp.nextToken());
            assertEquals(1, jp.getValueAsLong());
            assertEquals(1, jp.getValueAsLong(-99));
            assertToken(JsonToken.VALUE_NUMBER_INT, jp.nextToken());
            assertEquals(-3, jp.getValueAsLong());
            assertToken(JsonToken.VALUE_NUMBER_FLOAT, jp.nextToken());
            assertEquals(4, jp.getValueAsLong());
            assertEquals(4, jp.getValueAsLong(99));
            assertToken(JsonToken.VALUE_TRUE, jp.nextToken());
            assertEquals(1, jp.getValueAsLong());
            assertToken(JsonToken.VALUE_FALSE, jp.nextToken());
            assertEquals(0, jp.getValueAsLong());
            assertToken(JsonToken.VALUE_NULL, jp.nextToken());
            assertEquals(0, jp.getValueAsLong());
            assertEquals(0, jp.getValueAsLong(27));
            assertToken(JsonToken.VALUE_STRING, jp.nextToken());
            assertEquals(-17, jp.getValueAsLong());
            assertEquals(-17, jp.getValueAsLong(3));
            assertToken(JsonToken.VALUE_STRING, jp.nextToken());
            assertEquals(0, jp.getValueAsLong());
            assertEquals(9, jp.getValueAsLong(9));
            
            assertToken(JsonToken.END_ARRAY, jp.nextToken());
            assertEquals(0, jp.getValueAsLong());
            assertEquals(9, jp.getValueAsLong(9));

            jp.close();
        }     
    }

// com.fasterxml.jackson.core.json.TestValueConversions::testAsBoolean
    public void testAsBoolean() throws Exception
    {
        final String input = "[ true, false, null, 1, 0, \"true\", \"false\", \"foo\" ]";
        for (int i = 0; i < 2; ++i) {
            JsonParser jp;
            if (i == 0) {
                jp = createParserUsingReader(input);                
            } else {
                jp = this.createParserUsingStream(input, "UTF-8");
            }
            assertToken(JsonToken.START_ARRAY, jp.nextToken());
            assertEquals(false, jp.getValueAsBoolean());
            assertEquals(true, jp.getValueAsBoolean(true));

            assertToken(JsonToken.VALUE_TRUE, jp.nextToken());
            assertEquals(true, jp.getValueAsBoolean());
            assertToken(JsonToken.VALUE_FALSE, jp.nextToken());
            assertEquals(false, jp.getValueAsBoolean());
            assertToken(JsonToken.VALUE_NULL, jp.nextToken());
            assertEquals(false, jp.getValueAsBoolean());
            assertToken(JsonToken.VALUE_NUMBER_INT, jp.nextToken());
            assertEquals(1, jp.getIntValue());
            assertEquals(true, jp.getValueAsBoolean());
            assertToken(JsonToken.VALUE_NUMBER_INT, jp.nextToken());
            assertEquals(0, jp.getIntValue());
            assertEquals(false, jp.getValueAsBoolean());

            assertToken(JsonToken.VALUE_STRING, jp.nextToken()); 
            assertEquals(true, jp.getValueAsBoolean());
            assertToken(JsonToken.VALUE_STRING, jp.nextToken());
            assertEquals(false, jp.getValueAsBoolean());
            assertToken(JsonToken.VALUE_STRING, jp.nextToken());
            assertEquals(false, jp.getValueAsBoolean());
            
            assertToken(JsonToken.END_ARRAY, jp.nextToken());
            assertEquals(false, jp.getValueAsBoolean());
            assertEquals(true, jp.getValueAsBoolean(true));

            jp.close();
        }     
    }

// com.fasterxml.jackson.core.json.TestValueConversions::testAsLong
    public void testAsLong() throws Exception
    {
        final String input = "[ 1, -3, 4.98, true, false, null, \"-17\", \"foo\" ]";
        for (int i = 0; i < 2; ++i) {
            JsonParser jp;
            if (i == 0) {
                jp = createParserUsingReader(input);                
            } else {
                jp = createParserUsingStream(input, "UTF-8");
            }
            assertToken(JsonToken.START_ARRAY, jp.nextToken());
            assertEquals(0L, jp.getValueAsLong());
            assertEquals(9L, jp.getValueAsLong(9L));

            assertToken(JsonToken.VALUE_NUMBER_INT, jp.nextToken());
            assertEquals(1L, jp.getValueAsLong());
            assertEquals(1L, jp.getValueAsLong(-99L));
            assertToken(JsonToken.VALUE_NUMBER_INT, jp.nextToken());
            assertEquals(-3L, jp.getValueAsLong());
            assertToken(JsonToken.VALUE_NUMBER_FLOAT, jp.nextToken());
            assertEquals(4L, jp.getValueAsLong());
            assertEquals(4L, jp.getValueAsLong(99L));
            assertToken(JsonToken.VALUE_TRUE, jp.nextToken());
            assertEquals(1L, jp.getValueAsLong());
            assertToken(JsonToken.VALUE_FALSE, jp.nextToken());
            assertEquals(0L, jp.getValueAsLong());
            assertToken(JsonToken.VALUE_NULL, jp.nextToken());
            assertEquals(0L, jp.getValueAsLong());
            assertEquals(0L, jp.getValueAsLong(27L));
            assertToken(JsonToken.VALUE_STRING, jp.nextToken());
            assertEquals(-17L, jp.getValueAsLong());
            assertEquals(-17L, jp.getValueAsLong(3L));
            assertToken(JsonToken.VALUE_STRING, jp.nextToken());
            assertEquals(0L, jp.getValueAsLong());
            assertEquals(9L, jp.getValueAsLong(9L));
            
            assertToken(JsonToken.END_ARRAY, jp.nextToken());
            assertEquals(0L, jp.getValueAsLong());
            assertEquals(9L, jp.getValueAsLong(9L));

            jp.close();
        }     
    }

// com.fasterxml.jackson.core.json.TestValueConversions::testAsDouble
    public void testAsDouble() throws Exception
    {
        final String input = "[ 1, -3, 4.98, true, false, null, \"-17.25\", \"foo\" ]";
        for (int i = 0; i < 2; ++i) {
            JsonParser jp;
            if (i == 0) {
                jp = createParserUsingReader(input);                
            } else {
                jp = this.createParserUsingStream(input, "UTF-8");
            }
            assertToken(JsonToken.START_ARRAY, jp.nextToken());
            assertEquals(0.0, jp.getValueAsDouble());
            assertEquals(9.0, jp.getValueAsDouble(9.0));

            assertToken(JsonToken.VALUE_NUMBER_INT, jp.nextToken());
            assertEquals(1., jp.getValueAsDouble());
            assertEquals(1., jp.getValueAsDouble(-99.0));
            assertToken(JsonToken.VALUE_NUMBER_INT, jp.nextToken());
            assertEquals(-3., jp.getValueAsDouble());
            assertToken(JsonToken.VALUE_NUMBER_FLOAT, jp.nextToken());
            assertEquals(4.98, jp.getValueAsDouble());
            assertEquals(4.98, jp.getValueAsDouble(12.5));
            assertToken(JsonToken.VALUE_TRUE, jp.nextToken());
            assertEquals(1.0, jp.getValueAsDouble());
            assertToken(JsonToken.VALUE_FALSE, jp.nextToken());
            assertEquals(0.0, jp.getValueAsDouble());
            assertToken(JsonToken.VALUE_NULL, jp.nextToken());
            assertEquals(0.0, jp.getValueAsDouble());
            assertEquals(0.0, jp.getValueAsDouble(27.8));
            assertToken(JsonToken.VALUE_STRING, jp.nextToken());
            assertEquals(-17.25, jp.getValueAsDouble());
            assertEquals(-17.25, jp.getValueAsDouble(1.9));
            assertToken(JsonToken.VALUE_STRING, jp.nextToken());
            assertEquals(0.0, jp.getValueAsDouble());
            assertEquals(1.25, jp.getValueAsDouble(1.25));
            
            assertToken(JsonToken.END_ARRAY, jp.nextToken());
            assertEquals(0.0, jp.getValueAsDouble());
            assertEquals(7.5, jp.getValueAsDouble(7.5));

            jp.close();
        }     
    }

// com.fasterxml.jackson.core.json.TestWithTonsaSymbols::testStreamReaderParser
    public void testStreamReaderParser() throws Exception {
        _testWith(true);
    }

// com.fasterxml.jackson.core.json.TestWithTonsaSymbols::testReaderParser
    public void testReaderParser() throws Exception {
        _testWith(false);
    }

// com.fasterxml.jackson.core.main.TestArrayParsing::testValidEmpty
    public void testValidEmpty() throws Exception
    {
        final String DOC = "[   \n  ]";

        JsonParser jp = createParserUsingStream(DOC, "UTF-8");
        assertToken(JsonToken.START_ARRAY, jp.nextToken());
        assertToken(JsonToken.END_ARRAY, jp.nextToken());
        assertNull(jp.nextToken());
        jp.close();
    }

// com.fasterxml.jackson.core.main.TestArrayParsing::testInvalidEmptyMissingClose
    public void testInvalidEmptyMissingClose() throws Exception
    {
        final String DOC = "[ ";

        JsonParser jp = createParserUsingStream(DOC, "UTF-8");
        assertToken(JsonToken.START_ARRAY, jp.nextToken());

        try {
            jp.nextToken();
            fail("Expected a parsing error for missing array close marker");
        } catch (JsonParseException jex) {
            verifyException(jex, "expected close marker for ARRAY");
        }
        jp.close();
    }

// com.fasterxml.jackson.core.main.TestArrayParsing::testInvalidMissingFieldName
    public void testInvalidMissingFieldName() throws Exception
    {
        final String DOC = "[  : 3 ] ";

        JsonParser jp = createParserUsingStream(DOC, "UTF-8");
        assertToken(JsonToken.START_ARRAY, jp.nextToken());

        try {
            jp.nextToken();
            fail("Expected a parsing error for odd character");
        } catch (JsonParseException jex) {
            verifyException(jex, "Unexpected character");
        }
        jp.close();
    }

// com.fasterxml.jackson.core.main.TestArrayParsing::testInvalidExtraComma
    public void testInvalidExtraComma() throws Exception
    {
        final String DOC = "[ 24, ] ";

        JsonParser jp = createParserUsingStream(DOC, "UTF-8");
        assertToken(JsonToken.START_ARRAY, jp.nextToken());
        assertToken(JsonToken.VALUE_NUMBER_INT, jp.nextToken());
        assertEquals(24, jp.getIntValue());

        try {
            jp.nextToken();
            fail("Expected a parsing error for missing array close marker");
        } catch (JsonParseException jex) {
            verifyException(jex, "expected a value");
        }
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

// com.fasterxml.jackson.core.main.TestNumberParsing::testIntParsing
    public void testIntParsing() throws Exception
    {
        char[] testChars = "123456789".toCharArray();

        assertEquals(3, NumberInput.parseInt(testChars, 2, 1));
        assertEquals(123, NumberInput.parseInt(testChars, 0, 3));
        assertEquals(2345, NumberInput.parseInt(testChars, 1, 4));
        assertEquals(9, NumberInput.parseInt(testChars, 8, 1));
        assertEquals(456789, NumberInput.parseInt(testChars, 3, 6));
        assertEquals(23456, NumberInput.parseInt(testChars, 1, 5));
        assertEquals(123456789, NumberInput.parseInt(testChars, 0, 9));

        testChars = "32".toCharArray();
        assertEquals(32, NumberInput.parseInt(testChars, 0, 2));
        testChars = "189".toCharArray();
        assertEquals(189, NumberInput.parseInt(testChars, 0, 3));

        testChars = "10".toCharArray();
        assertEquals(10, NumberInput.parseInt(testChars, 0, 2));
        assertEquals(0, NumberInput.parseInt(testChars, 1, 1));
    }

// com.fasterxml.jackson.core.main.TestNumberParsing::testIntParsingWithStrings
    public void testIntParsingWithStrings() throws Exception
    {
        assertEquals(3, NumberInput.parseInt("3"));
        assertEquals(0, NumberInput.parseInt("0"));
        assertEquals(-3, NumberInput.parseInt("-3"));
        assertEquals(27, NumberInput.parseInt("27"));
        assertEquals(-31, NumberInput.parseInt("-31"));
        assertEquals(271, NumberInput.parseInt("271"));
        assertEquals(-131, NumberInput.parseInt("-131"));
        assertEquals(2709, NumberInput.parseInt("2709"));
        assertEquals(-9999, NumberInput.parseInt("-9999"));
        assertEquals(Integer.MIN_VALUE, NumberInput.parseInt(""+Integer.MIN_VALUE));
        assertEquals(Integer.MAX_VALUE, NumberInput.parseInt(""+Integer.MAX_VALUE));
    }

// com.fasterxml.jackson.core.main.TestNumberParsing::testLongParsing
    public void testLongParsing() throws Exception
    {
        char[] testChars = "123456789012345678".toCharArray();

        assertEquals(123456789012345678L, NumberInput.parseLong(testChars, 0, testChars.length));
    }

// com.fasterxml.jackson.core.main.TestNumberParsing::testLongBoundsChecks
    public void testLongBoundsChecks() throws Exception
    {
        String minLong = String.valueOf(Long.MIN_VALUE).substring(1);
        String maxLong = String.valueOf(Long.MAX_VALUE);
        final String VALUE_491 = "1323372036854775807"; 
        final String OVERFLOW =  "9999999999999999999"; 

        assertTrue(NumberInput.inLongRange(minLong, true));
        assertTrue(NumberInput.inLongRange(maxLong, false));
        assertTrue(NumberInput.inLongRange(VALUE_491, true));
        assertTrue(NumberInput.inLongRange(VALUE_491, false));
        assertFalse(NumberInput.inLongRange(OVERFLOW, false));
        assertFalse(NumberInput.inLongRange(OVERFLOW, true));

        char[] cbuf = minLong.toCharArray();
        assertTrue(NumberInput.inLongRange(cbuf, 0, cbuf.length, true));
        cbuf = maxLong.toCharArray();
        assertTrue(NumberInput.inLongRange(cbuf, 0, cbuf.length, false));
        cbuf = VALUE_491.toCharArray();
        assertTrue(NumberInput.inLongRange(cbuf, 0, cbuf.length, true));
        assertTrue(NumberInput.inLongRange(cbuf, 0, cbuf.length, false));
        cbuf = OVERFLOW.toCharArray();
        assertFalse(NumberInput.inLongRange(cbuf, 0, cbuf.length, true));
        assertFalse(NumberInput.inLongRange(cbuf, 0, cbuf.length, false));
    }

// com.fasterxml.jackson.core.main.TestNumberParsing::testFloatBoundary146Chars
    public void testFloatBoundary146Chars() throws Exception
    {
        final char[] arr = new char[50005];
        final JsonFactory f = new JsonFactory();
        for(int i = 500; i != 9000; ++i) {
          java.util.Arrays.fill(arr, 0, i, ' ');
          arr[i] = '-';
          arr[i + 1] = '1';
          arr[i + 2] = 'e';
          arr[i + 3] = '-';
          arr[i + 4] = '1';
          CharArrayReader r = new CharArrayReader(arr, 0, i+5);
          JsonParser p = f.createParser(r);
          assertToken(JsonToken.VALUE_NUMBER_FLOAT, p.nextToken());
          p.close();
        }        
    }

// com.fasterxml.jackson.core.main.TestNumberParsing::testFloatBoundary146Bytes
    public void testFloatBoundary146Bytes() throws Exception
    {
        final byte[] arr = new byte[50005];
        final JsonFactory f = new JsonFactory();
        for(int i = 500; i != 9000; ++i) {
          java.util.Arrays.fill(arr, 0, i, (byte) 0x20);
          arr[i] = '-';
          arr[i + 1] = '1';
          arr[i + 2] = 'e';
          arr[i + 3] = '-';
          arr[i + 4] = '1';
          ByteArrayInputStream in = new ByteArrayInputStream(arr, 0, i+5);
          JsonParser p = f.createParser(in);
          assertToken(JsonToken.VALUE_NUMBER_FLOAT, p.nextToken());
          p.close();
        }        
    }

// com.fasterxml.jackson.core.main.TestParserClosing::testNoAutoCloseReader
    public void testNoAutoCloseReader()
        throws Exception
    {
        final String DOC = "[ 1 ]";

        JsonFactory f = new JsonFactory();

        
        assertTrue(f.isEnabled(JsonParser.Feature.AUTO_CLOSE_SOURCE));
        
        f.disable(JsonParser.Feature.AUTO_CLOSE_SOURCE);
        assertFalse(f.isEnabled(JsonParser.Feature.AUTO_CLOSE_SOURCE));
        @SuppressWarnings("resource")
        MyReader input = new MyReader(DOC);
        JsonParser jp = f.createParser(input);

        
        assertFalse(input.isClosed());
        assertToken(JsonToken.START_ARRAY, jp.nextToken());
        assertToken(JsonToken.VALUE_NUMBER_INT, jp.nextToken());
        assertToken(JsonToken.END_ARRAY, jp.nextToken());
        assertNull(jp.nextToken());
        
        assertFalse(input.isClosed());
        
        jp.close();
        assertFalse(input.isClosed());
    }

// com.fasterxml.jackson.core.main.TestParserClosing::testAutoCloseReader
    public void testAutoCloseReader() throws Exception
    {
        final String DOC = "[ 1 ]";

        JsonFactory f = new JsonFactory();
        f.enable(JsonParser.Feature.AUTO_CLOSE_SOURCE);
        assertTrue(f.isEnabled(JsonParser.Feature.AUTO_CLOSE_SOURCE));
        MyReader input = new MyReader(DOC);
        JsonParser jp = f.createParser(input);
        assertFalse(input.isClosed());
        assertToken(JsonToken.START_ARRAY, jp.nextToken());
        
        jp.close();
        assertTrue(input.isClosed());

        
        input = new MyReader(DOC);
        jp = f.createParser(input);
        assertFalse(input.isClosed());
        assertToken(JsonToken.START_ARRAY, jp.nextToken());
        assertToken(JsonToken.VALUE_NUMBER_INT, jp.nextToken());
        assertToken(JsonToken.END_ARRAY, jp.nextToken());
        assertNull(jp.nextToken());
        assertTrue(input.isClosed());
    }

// com.fasterxml.jackson.core.main.TestParserClosing::testNoAutoCloseInputStream
    public void testNoAutoCloseInputStream() throws Exception
    {
        final String DOC = "[ 1 ]";
        JsonFactory f = new JsonFactory();

        f.disable(JsonParser.Feature.AUTO_CLOSE_SOURCE);
        MyStream input = new MyStream(DOC.getBytes("UTF-8"));
        JsonParser jp = f.createParser(input);

        
        assertFalse(input.isClosed());
        assertToken(JsonToken.START_ARRAY, jp.nextToken());
        assertToken(JsonToken.VALUE_NUMBER_INT, jp.nextToken());
        assertToken(JsonToken.END_ARRAY, jp.nextToken());
        assertNull(jp.nextToken());
        
        assertFalse(input.isClosed());
        
        jp.close();
        assertFalse(input.isClosed());
    }

// com.fasterxml.jackson.core.main.TestParserClosing::testReleaseContentBytes
    public void testReleaseContentBytes() throws Exception
    {
        byte[] input = "[1]foobar".getBytes("UTF-8");
        JsonParser jp = new JsonFactory().createParser(input);
        assertToken(JsonToken.START_ARRAY, jp.nextToken());
        assertToken(JsonToken.VALUE_NUMBER_INT, jp.nextToken());
        assertToken(JsonToken.END_ARRAY, jp.nextToken());
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        
        assertEquals(6, jp.releaseBuffered(out));
        assertArrayEquals("foobar".getBytes("UTF-8"), out.toByteArray());
        jp.close();
    }

// com.fasterxml.jackson.core.main.TestParserClosing::testReleaseContentChars
    public void testReleaseContentChars() throws Exception
    {
        JsonParser jp = new JsonFactory().createParser("[true]xyz");
        assertToken(JsonToken.START_ARRAY, jp.nextToken());
        assertToken(JsonToken.VALUE_TRUE, jp.nextToken());
        assertToken(JsonToken.END_ARRAY, jp.nextToken());
        StringWriter sw = new StringWriter();
        
        assertEquals(3, jp.releaseBuffered(sw));
        assertEquals("xyz", sw.toString());
        jp.close();
    }

// com.fasterxml.jackson.core.main.TestParserFeatures::testDefaultSettings
    public void testDefaultSettings()
    {
        JsonFactory f = new JsonFactory();
        assertTrue(f.isEnabled(JsonParser.Feature.AUTO_CLOSE_SOURCE));
        assertFalse(f.isEnabled(JsonParser.Feature.ALLOW_COMMENTS));
        assertFalse(f.isEnabled(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES));
        assertFalse(f.isEnabled(JsonParser.Feature.ALLOW_SINGLE_QUOTES));
        assertFalse(f.isEnabled(JsonParser.Feature.ALLOW_UNQUOTED_CONTROL_CHARS));
    }

// com.fasterxml.jackson.core.main.TestParserFeatures::testQuotesRequired
    public void testQuotesRequired() throws Exception
    {
        _testQuotesRequired(false);
        _testQuotesRequired(true);
    }

// com.fasterxml.jackson.core.main.TestParserFeatures::testTabsDefault
    public void testTabsDefault() throws Exception
    {
        _testTabsDefault(false);
        _testTabsDefault(true);
    }

// com.fasterxml.jackson.core.main.TestParserFeatures::testTabsEnabled
    public void testTabsEnabled() throws Exception
    {
        _testTabsEnabled(false);
        _testTabsEnabled(true);
    }

// com.fasterxml.jackson.core.main.TestParserLinefeeds::testCR
    public void testCR() throws Exception
    {
        _testLinefeeds("\r", true);
        _testLinefeeds("\r", false);
    }

// com.fasterxml.jackson.core.main.TestParserLinefeeds::testLF
    public void testLF() throws Exception
    {
        _testLinefeeds("\n", true);
        _testLinefeeds("\n", false);
    }

// com.fasterxml.jackson.core.main.TestParserLinefeeds::testCRLF
    public void testCRLF() throws Exception
    {
        _testLinefeeds("\r\n", true);
        _testLinefeeds("\r\n", false);
    }

// com.fasterxml.jackson.core.main.TestParserWithObjects::testNextValue
    public void testNextValue() throws IOException
    {
        
        _testNextValueBasic(false);
        _testNextValueBasic(true);
    }

// com.fasterxml.jackson.core.main.TestParserWithObjects::testNextValueNested
    public void testNextValueNested() throws IOException
    {
        
        _testNextValueNested(false);
        _testNextValueNested(true);
    }

// com.fasterxml.jackson.core.main.TestParserWithObjects::testIsClosed
    public void testIsClosed() throws IOException
    {
        for (int i = 0; i < 4; ++i) {
            String JSON = "[ 1, 2, 3 ]";
            boolean stream = ((i & 1) == 0);
            JsonParser jp = stream ?
                createParserUsingStream(JSON, "UTF-8")
                : createParserUsingReader(JSON);
            boolean partial = ((i & 2) == 0);

            assertFalse(jp.isClosed());
            assertToken(JsonToken.START_ARRAY, jp.nextToken());

            assertToken(JsonToken.VALUE_NUMBER_INT, jp.nextToken());
            assertToken(JsonToken.VALUE_NUMBER_INT, jp.nextToken());
            assertFalse(jp.isClosed());

            if (partial) {
                jp.close();
                assertTrue(jp.isClosed());
            } else {
                assertToken(JsonToken.VALUE_NUMBER_INT, jp.nextToken());
                assertToken(JsonToken.END_ARRAY, jp.nextToken());
                assertNull(jp.nextToken());
                assertTrue(jp.isClosed());
            }
        }
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

// com.fasterxml.jackson.core.main.TestScopeMatching::testUnclosedArray
    public void testUnclosedArray() throws Exception
    {
        @SuppressWarnings("resource")
        JsonParser jp = createParserUsingReader("[ 1, 2");
        assertToken(JsonToken.START_ARRAY, jp.nextToken());
        assertToken(JsonToken.VALUE_NUMBER_INT, jp.nextToken());
        assertToken(JsonToken.VALUE_NUMBER_INT, jp.nextToken());

        try {
            jp.nextToken();
            fail("Expected an exception for unclosed ARRAY");
        } catch (JsonParseException jpe) {
            verifyException(jpe, "expected close marker for ARRAY");
        }
    }

// com.fasterxml.jackson.core.main.TestScopeMatching::testUnclosedObject
    public void testUnclosedObject() throws Exception
    {
        @SuppressWarnings("resource")
        JsonParser jp = createParserUsingReader("{ \"key\" : 3  ");
        assertToken(JsonToken.START_OBJECT, jp.nextToken());
        assertToken(JsonToken.FIELD_NAME, jp.nextToken());
        assertToken(JsonToken.VALUE_NUMBER_INT, jp.nextToken());

        try {
            jp.nextToken();
            fail("Expected an exception for unclosed OBJECT");
        } catch (JsonParseException jpe) {
            verifyException(jpe, "expected close marker for OBJECT");
        }
    }

// com.fasterxml.jackson.core.main.TestScopeMatching::testEOFInName
    public void testEOFInName()
        throws Exception
    {
        final String JSON = "{ \"abcd";
        for (int i = 0; i < 2; ++i) {
            JsonParser jp = (i == 0) ? createParserUsingReader(JSON)
                : createParserUsingStream(JSON, "UTF-8");
            assertToken(JsonToken.START_OBJECT, jp.nextToken());
            try {
                jp.nextToken();
                fail("Expected an exception for EOF");
            } catch (JsonParseException jpe) {
                verifyException(jpe, "Unexpected end-of-input");
            }
            jp.close();
        }
    }

// com.fasterxml.jackson.core.main.TestScopeMatching::testWeirdToken
    public void testWeirdToken()
        throws Exception
    {
        final String JSON = "[ nil ]";
        for (int i = 0; i < 2; ++i) {
            JsonParser jp = (i == 0) ? createParserUsingReader(JSON)
                : createParserUsingStream(JSON, "UTF-8");
            assertToken(JsonToken.START_ARRAY, jp.nextToken());
            try {
                jp.nextToken();
                fail("Expected an exception for weird token");
            } catch (JsonParseException jpe) {
                verifyException(jpe, "Unrecognized token");
            }
            jp.close();
        }
    }

// com.fasterxml.jackson.core.main.TestScopeMatching::testMismatchArrayToObject
    public void testMismatchArrayToObject()
        throws Exception
    {
        final String JSON = "[ 1, 2 }";
        for (int i = 0; i < 2; ++i) {
            JsonParser jp = (i == 0) ? createParserUsingReader(JSON)
                : createParserUsingStream(JSON, "UTF-8");
            assertToken(JsonToken.START_ARRAY, jp.nextToken());
            assertToken(JsonToken.VALUE_NUMBER_INT, jp.nextToken());
            assertToken(JsonToken.VALUE_NUMBER_INT, jp.nextToken());
            try {
                jp.nextToken();
                fail("Expected an exception for incorrectly closed ARRAY");
            } catch (JsonParseException jpe) {
                verifyException(jpe, "Unexpected close marker '}': expected ']'");
            }
            jp.close();
        }
    }

// com.fasterxml.jackson.core.main.TestScopeMatching::testMismatchObjectToArray
    public void testMismatchObjectToArray()
        throws Exception
    {
        final String JSON = "{ ]";
        for (int i = 0; i < 2; ++i) {
            JsonParser jp = (i == 0) ? createParserUsingReader(JSON)
                : createParserUsingStream(JSON, "UTF-8");
            assertToken(JsonToken.START_OBJECT, jp.nextToken());
            
            try {
                jp.nextToken();
                fail("Expected an exception for incorrectly closed OBJECT");
            } catch (JsonParseException jpe) {
                verifyException(jpe, "Unexpected close marker ']': expected '}'");
            }
            jp.close();
        }
    }

// com.fasterxml.jackson.core.main.TestScopeMatching::testMisssingColon
    public void testMisssingColon()
        throws Exception
    {
        final String JSON = "{ \"a\" \"b\" }";
        for (int i = 0; i < 2; ++i) {
            JsonParser jp = (i == 0) ? createParserUsingReader(JSON)
                : createParserUsingStream(JSON, "UTF-8");
            assertToken(JsonToken.START_OBJECT, jp.nextToken());
            try {
                
                assertToken(JsonToken.FIELD_NAME, jp.nextToken());
                jp.nextToken();
                fail("Expected an exception for missing semicolon");
            } catch (JsonParseException jpe) {
                verifyException(jpe, "was expecting a colon");
            }
            jp.close();
        }
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

// com.fasterxml.jackson.core.sym.TestByteBasedSymbols::testSharedSymbols
    public void testSharedSymbols() throws Exception
    {
        
        JsonFactory jf = new JsonFactory();

        
        String DOC0 = "{ \"a\" : 1, \"x\" : [ ] }";
        JsonParser jp0 = createParser(jf, DOC0);

        
        while (jp0.nextToken() != JsonToken.START_ARRAY) { }

        String doc1 = createDoc(FIELD_NAMES, true);
        String doc2 = createDoc(FIELD_NAMES, false);

        
        for (int x = 0; x < 2; ++x) {
            JsonParser jp1 = createParser(jf, doc1);
            JsonParser jp2 = createParser(jf, doc2);

            assertToken(JsonToken.START_OBJECT, jp1.nextToken());
            assertToken(JsonToken.START_OBJECT, jp2.nextToken());
            
            int len = FIELD_NAMES.length;
            for (int i = 0; i < len; ++i) {
                assertToken(JsonToken.FIELD_NAME, jp1.nextToken());
                assertToken(JsonToken.FIELD_NAME, jp2.nextToken());
                assertEquals(FIELD_NAMES[i], jp1.getCurrentName());
                assertEquals(FIELD_NAMES[len-(i+1)], jp2.getCurrentName());
                assertToken(JsonToken.VALUE_NUMBER_INT, jp1.nextToken());
                assertToken(JsonToken.VALUE_NUMBER_INT, jp2.nextToken());
                assertEquals(i, jp1.getIntValue());
                assertEquals(i, jp2.getIntValue());
            }
            
            assertToken(JsonToken.END_OBJECT, jp1.nextToken());
            assertToken(JsonToken.END_OBJECT, jp2.nextToken());
            
            jp1.close();
            jp2.close();
        }
        jp0.close();
    }

// com.fasterxml.jackson.core.sym.TestByteBasedSymbols::testAuxMethodsWithNewSymboTable
    public void testAuxMethodsWithNewSymboTable() throws Exception
    {
        final int A_BYTES = 0x41414141; 
        final int B_BYTES = 0x42424242; 

        ByteQuadsCanonicalizer nc = ByteQuadsCanonicalizer.createRoot()
                .makeChild(JsonFactory.Feature.collectDefaults());
        assertNull(nc.findName(A_BYTES));
        assertNull(nc.findName(A_BYTES, B_BYTES));

        nc.addName("AAAA", new int[] { A_BYTES }, 1);
        String n1 = nc.findName(A_BYTES);
        assertEquals("AAAA", n1);
        nc.addName("AAAABBBB", new int[] { A_BYTES, B_BYTES }, 2);
        String n2 = nc.findName(A_BYTES, B_BYTES);
        assertEquals("AAAABBBB", n2);
        assertNotNull(n2);

        
        assertNotNull(nc.toString());
    }

// com.fasterxml.jackson.core.sym.TestHashCollisionChars::testReaderCollisions
    public void testReaderCollisions() throws Exception
    {
        StringBuilder sb = new StringBuilder();
        List<String> coll = collisions();
        
        for (String field : coll) {
            if (sb.length() == 0) {
                sb.append("{");
            } else {
                sb.append(",\n");
            }
            sb.append('"');
            sb.append(field);
            sb.append("\":3");
        }
        sb.append("}");

        

        JsonFactory jf = new JsonFactory();
        JsonParser jp = jf.createParser(sb.toString());
        jf.enable(JsonFactory.Feature.FAIL_ON_SYMBOL_HASH_OVERFLOW);

        try {
            while (jp.nextToken() != null) {
                ;
            }
            fail("Should have failed");
        } catch (IllegalStateException e) {
            verifyException(e, "hash collision");
        }
        jp.close();

        
        jf = new JsonFactory();
        jf.disable(JsonFactory.Feature.FAIL_ON_SYMBOL_HASH_OVERFLOW);
        jp = jf.createParser(sb.toString());
        while (jp.nextToken() != null) {
            ;
        }
        jp.close();
    }

// com.fasterxml.jackson.core.sym.TestJsonParserSymbols::testByteSymbolsWithClose
    public void testByteSymbolsWithClose() throws Exception
    {
        _testWithClose(true);
    }

// com.fasterxml.jackson.core.sym.TestJsonParserSymbols::testByteSymbolsWithEOF
    public void testByteSymbolsWithEOF() throws Exception
    {
        MyJsonFactory f = new MyJsonFactory();
        JsonParser jp = _getParser(f, JSON, true);
        while (jp.nextToken() != null) {
            
            assertEquals(0, f.byteSymbolCount());
        }
        
        assertEquals(3, f.byteSymbolCount());
        jp.close();
        assertEquals(3, f.byteSymbolCount());
    }

// com.fasterxml.jackson.core.sym.TestJsonParserSymbols::testHashCalc
    public void testHashCalc() throws Exception
    {
        CharsToNameCanonicalizer sym = CharsToNameCanonicalizer.createRoot(123);
        char[] str1 = "foo".toCharArray();
        char[] str2 = " foo ".toCharArray();

        assertEquals(sym.calcHash(str1, 0, 3), sym.calcHash(str2, 1, 3));
    }

// com.fasterxml.jackson.core.sym.TestJsonParserSymbols::testCharSymbolsWithClose
    public void testCharSymbolsWithClose() throws Exception
    {
        _testWithClose(false);
    }

// com.fasterxml.jackson.core.sym.TestJsonParserSymbols::testCharSymbolsWithEOF
    public void testCharSymbolsWithEOF() throws Exception
    {
        MyJsonFactory f = new MyJsonFactory();
        JsonParser jp = _getParser(f, JSON, false);
        while (jp.nextToken() != null) {
            
            assertEquals(0, f.charSymbolCount());
        }
        
        assertEquals(3, f.charSymbolCount());
        jp.close();
        assertEquals(3, f.charSymbolCount());
    }

// com.fasterxml.jackson.core.sym.TestSymbolTables::testSyntheticWithChars
    public void testSyntheticWithChars()
    {
        
        CharsToNameCanonicalizer symbols = CharsToNameCanonicalizer.createRoot(1);
        final int COUNT = 6000;
        for (int i = 0; i < COUNT; ++i) {
            String id = fieldNameFor(i);
            char[] ch = id.toCharArray();
            symbols.findSymbol(ch, 0, ch.length, symbols.calcHash(id));
        }

        assertEquals(8192, symbols.bucketCount());
        assertEquals(COUNT, symbols.size());
        

        
        
        

        assertEquals(1401, symbols.collisionCount()); 

        
        

        assertEquals(4, symbols.maxCollisionLength()); 

    }

// com.fasterxml.jackson.core.sym.TestSymbolTables::testSyntheticWithBytesOld
    public void testSyntheticWithBytesOld() throws IOException
    {
        
        final int SEED = 33333;
        BytesToNameCanonicalizer symbols =
                BytesToNameCanonicalizer.createRoot(SEED).makeChild(JsonFactory.Feature.collectDefaults());

        final int COUNT = 6000;
        for (int i = 0; i < COUNT; ++i) {
            String id = fieldNameFor(i);
            int[] quads = calcQuads(id.getBytes("UTF-8"));
            symbols.addName(id, quads, quads.length);
        }
        assertEquals(COUNT, symbols.size());
        assertEquals(8192, symbols.bucketCount());

    
        
        assertEquals(1715, symbols.collisionCount());
        
        assertEquals(9, symbols.maxCollisionLength());

        
    }

// com.fasterxml.jackson.core.sym.TestSymbolTables::testSyntheticWithBytesNew
    public void testSyntheticWithBytesNew() throws IOException
    {
        
        final int SEED = 33333;
        ByteQuadsCanonicalizer symbols =
                ByteQuadsCanonicalizer.createRoot(SEED).makeChild(JsonFactory.Feature.collectDefaults());

        final int COUNT = 6000;
        for (int i = 0; i < COUNT; ++i) {
            String id = fieldNameFor(i);
            int[] quads = calcQuads(id.getBytes("UTF-8"));
            symbols.addName(id, quads, quads.length);
        }
        assertEquals(COUNT, symbols.size());
        assertEquals(8192, symbols.bucketCount());

        
        
        assertEquals(4270, symbols.primaryCount());
        
        assertEquals(1234, symbols.secondaryCount());
        
        assertEquals(496, symbols.tertiaryCount());
        
        assertEquals(0, symbols.spilloverCount());
    }

// com.fasterxml.jackson.core.sym.TestSymbolTables::testThousandsOfSymbolsWithChars
    public void testThousandsOfSymbolsWithChars() throws IOException
    {
        final int SEED = 33333;

        CharsToNameCanonicalizer symbolsCRoot = CharsToNameCanonicalizer.createRoot(SEED);
        int exp = 0;
        
        for (int doc = 0; doc < 100; ++doc) {
            CharsToNameCanonicalizer symbolsC =
                    symbolsCRoot.makeChild(JsonFactory.Feature.collectDefaults());
            for (int i = 0; i < 250; ++i) {
                String name = "f_"+doc+"_"+i;
                char[] ch = name.toCharArray();
                String str = symbolsC.findSymbol(ch, 0, ch.length,
                        symbolsC.calcHash(name));
                assertNotNull(str);
            }
            symbolsC.release();
            exp += 250;
            if (exp > CharsToNameCanonicalizer.MAX_ENTRIES_FOR_REUSE) {
                exp = 0;
            }
            assertEquals(exp, symbolsCRoot.size());
        }
    }

// com.fasterxml.jackson.core.sym.TestSymbolTables::testThousandsOfSymbolsWithOldBytes
    public void testThousandsOfSymbolsWithOldBytes() throws IOException
    {
        final int SEED = 33333;

        BytesToNameCanonicalizer symbolsBRoot = BytesToNameCanonicalizer.createRoot(SEED);
        final Charset utf8 = Charset.forName("UTF-8");
        int exp = 0;
        
        for (int doc = 0; doc < 100; ++doc) {
            BytesToNameCanonicalizer symbolsB =
                    symbolsBRoot.makeChild(JsonFactory.Feature.collectDefaults());
            for (int i = 0; i < 250; ++i) {
                String name = "f_"+doc+"_"+i;

                int[] quads = BytesToNameCanonicalizer.calcQuads(name.getBytes(utf8));
                symbolsB.addName(name, quads, quads.length);
                Name n = symbolsB.findName(quads, quads.length);
                assertEquals(name, n.getName());
            }
            symbolsB.release();
            exp += 250;
            if (exp > BytesToNameCanonicalizer.MAX_ENTRIES_FOR_REUSE) {
                exp = 0;
            }
            assertEquals(exp, symbolsBRoot.size());
        }
    }

// com.fasterxml.jackson.core.sym.TestSymbolTables::testThousandsOfSymbolsWithNew
    public void testThousandsOfSymbolsWithNew() throws IOException
    {
        final int SEED = 33333;

        ByteQuadsCanonicalizer symbolsBRoot = ByteQuadsCanonicalizer.createRoot(SEED);
        final Charset utf8 = Charset.forName("UTF-8");
        int exp = 0;
        ByteQuadsCanonicalizer symbolsB = null;

        
        for (int doc = 0; doc < 100; ++doc) {
            symbolsB = symbolsBRoot.makeChild(JsonFactory.Feature.collectDefaults());
            for (int i = 0; i < 250; ++i) {
                String name = "f_"+doc+"_"+i;

                int[] quads = calcQuads(name.getBytes(utf8));
                
                symbolsB.addName(name, quads, quads.length);
                String n = symbolsB.findName(quads, quads.length);
                assertEquals(name, n);
            }

            symbolsB.release();
            
            exp += 250;
            if (exp > ByteQuadsCanonicalizer.MAX_ENTRIES_FOR_REUSE) {
                exp = 0;
            }
            assertEquals(exp, symbolsBRoot.size());
        }

        
        assertEquals(6250, symbolsB.size());
        assertEquals(4851, symbolsB.primaryCount()); 
        assertEquals(872, symbolsB.secondaryCount()); 
        assertEquals(510, symbolsB.tertiaryCount()); 
        assertEquals(17, symbolsB.spilloverCount()); 
    }

// com.fasterxml.jackson.core.sym.TestSymbolTables::testByteBasedSymbolTable
    public void testByteBasedSymbolTable() throws Exception
    {
        
        final String JSON = aposToQuotes("{'abc':1, 'abc\\u0000':2, '\\u0000abc':3, "
                
                +"'abc123':4,'abcd1234':5,"
                +"'abcd1234a':6,'abcd1234abcd':7,"
                +"'abcd1234abcd1':8"
                +"}");

        JsonFactory f = new JsonFactory();
        JsonParser p = f.createParser(JSON.getBytes("UTF-8"));
        ByteQuadsCanonicalizer symbols = _findSymbols(p);
        assertEquals(0, symbols.size());
        _streamThrough(p);
        assertEquals(8, symbols.size());
        p.close();

        
        p = f.createParser(JSON.getBytes("UTF-8"));
        _streamThrough(p);
        symbols = _findSymbols(p);
        assertEquals(8, symbols.size());
        p.close();

        p = f.createParser(JSON.getBytes("UTF-8"));
        _streamThrough(p);
        symbols = _findSymbols(p);
        assertEquals(8, symbols.size());
        p.close();
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

// com.fasterxml.jackson.core.util.TestSerializedString::testAppending
    public void testAppending() throws IOException
    {
        final String INPUT = "\"quo\\ted\"";
        final String QUOTED = "\\\"quo\\\\ted\\\"";
        
        SerializableString sstr = new SerializedString(INPUT);
        
        assertEquals(sstr.getValue(), INPUT);
        assertEquals(QUOTED, new String(sstr.asQuotedChars()));

        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        assertEquals(QUOTED.length(), sstr.writeQuotedUTF8(bytes));
        assertEquals(QUOTED, bytes.toString("UTF-8"));
        bytes.reset();
        assertEquals(INPUT.length(), sstr.writeUnquotedUTF8(bytes));
        assertEquals(INPUT, bytes.toString("UTF-8"));

        byte[] buffer = new byte[100];
        assertEquals(QUOTED.length(), sstr.appendQuotedUTF8(buffer, 3));
        assertEquals(QUOTED, new String(buffer, 3, QUOTED.length()));
        Arrays.fill(buffer, (byte) 0);
        assertEquals(INPUT.length(), sstr.appendUnquotedUTF8(buffer, 5));
        assertEquals(INPUT, new String(buffer, 5, INPUT.length()));
    }

// com.fasterxml.jackson.core.util.TestSerializedString::testFailedAccess
    public void testFailedAccess() throws IOException
    {
        final String INPUT = "Bit longer text";
        SerializableString sstr = new SerializedString(INPUT);

        final byte[] buffer = new byte[INPUT.length() - 2];
        final char[] ch = new char[INPUT.length() - 2];
        final ByteBuffer bbuf = ByteBuffer.allocate(INPUT.length() - 2);
        
        assertEquals(-1, sstr.appendQuotedUTF8(buffer, 0));
        assertEquals(-1, sstr.appendQuoted(ch, 0));
        assertEquals(-1, sstr.putQuotedUTF8(bbuf));

        bbuf.rewind();
        assertEquals(-1, sstr.appendUnquotedUTF8(buffer, 0));
        assertEquals(-1, sstr.appendUnquoted(ch, 0));
        assertEquals(-1, sstr.putUnquotedUTF8(bbuf));
    }

// com.fasterxml.jackson.core.util.TestTextBuffer::testSimple
    public void testSimple()
    {
        TextBuffer tb = new TextBuffer(new BufferRecycler());
        tb.append('a');
        tb.append(new char[] { 'X', 'b' }, 1, 1);
        tb.append("c", 0, 1);
        assertEquals(3, tb.contentsAsArray().length);
        assertEquals("abc", tb.toString());

        assertNotNull(tb.expandCurrentSegment());
    }

// com.fasterxml.jackson.core.util.TestTextBuffer::testLonger
    public void testLonger()
    {
        TextBuffer tb = new TextBuffer(new BufferRecycler());
        for (int i = 0; i < 2000; ++i) {
            tb.append("abc", 0, 3);
        }
        String str = tb.contentsAsString();
        assertEquals(6000, str.length());
        assertEquals(6000, tb.contentsAsArray().length);

        tb.resetWithShared(new char[] { 'a' }, 0, 1);
        assertEquals(1, tb.toString().length());
    }

// com.fasterxml.jackson.core.util.TestTextBuffer::testLongAppend
      public void testLongAppend()
      {
          final int len = TextBuffer.MAX_SEGMENT_LEN * 3 / 2;
          StringBuilder sb = new StringBuilder(len);
          for (int i = 0; i < len; ++i) {
              sb.append('x');
          }
         final String STR = sb.toString();
         final String EXP = "a" + STR + "c";
 
         
         TextBuffer tb = new TextBuffer(new BufferRecycler());
         tb.append('a');
         tb.append(STR, 0, len);
         tb.append('c');
         assertEquals(len+2, tb.size());
         assertEquals(EXP, tb.contentsAsString());
 
         
         tb = new TextBuffer(new BufferRecycler());
         tb.append('a');
         tb.append(STR.toCharArray(), 0, len);
         tb.append('c');
         assertEquals(len+2, tb.size());
         assertEquals(EXP, tb.contentsAsString());
      }

// com.fasterxml.jackson.core.util.TestTextBuffer::testExpand
      public void testExpand()
      {
          TextBuffer tb = new TextBuffer(new BufferRecycler());
          char[] buf = tb.getCurrentSegment();

          while (buf.length < 500 * 1000) {
              char[] old = buf;
              buf = tb.expandCurrentSegment();
              if (old.length >= buf.length) {
                  fail("Expected buffer of "+old.length+" to expand, did not, length now "+buf.length);
              }
          }
      }

// com.fasterxml.jackson.core.util.TestTextBuffer::testEmpty
    public void testEmpty() {
        TextBuffer tb = new TextBuffer(new BufferRecycler());
        tb.resetWithEmpty();

        assertTrue(tb.getTextBuffer().length == 0);
        tb.contentsAsString();
        assertTrue(tb.getTextBuffer().length == 0);
    }

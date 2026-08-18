// buggy code
    private String _handleOddName2(int startPtr, int hash, int[] codes) throws IOException
    {
        _textBuffer.resetWithShared(_inputBuffer, startPtr, (_inputPtr - startPtr));
        char[] outBuf = _textBuffer.getCurrentSegment();
        int outPtr = _textBuffer.getCurrentSegmentSize();
        final int maxCode = codes.length;

        while (true) {
            if (_inputPtr >= _inputEnd) {
                if (!_loadMore()) { // acceptable for now (will error out later)
                    break;
                }
            }
            char c = _inputBuffer[_inputPtr];
            int i = (int) c;
            if (i <= maxCode) {
                if (codes[i] != 0) {
                    break;
                }
            } else if (!Character.isJavaIdentifierPart(c)) {
                break;
            }
            ++_inputPtr;
            hash = (hash * CharsToNameCanonicalizer.HASH_MULT) + i;
            // Ok, let's add char to output:
            outBuf[outPtr++] = c;

            // Need more room?
            if (outPtr >= outBuf.length) {
                outBuf = _textBuffer.finishCurrentSegment();
                outPtr = 0;
            }
        }
        _textBuffer.setCurrentLength(outPtr);
        {
            TextBuffer tb = _textBuffer;
            char[] buf = tb.getTextBuffer();
            int start = tb.getTextOffset();
            int len = tb.size();

            return _symbols.findSymbol(buf, start, len, hash);
        }
    }

// relevant test
// com.fasterxml.jackson.core.PointerFromContextTest::testViaParser
    public void testViaParser() throws Exception
    {
        final String SIMPLE = aposToQuotes("{'a':123,'array':[1,2,[3],5,{'obInArray':4}],"
                +"'ob':{'first':[false,true],'second':{'sub':37}},'b':true}");
        JsonParser p = JSON_F.createParser(SIMPLE);

        
        assertSame(JsonPointer.EMPTY, p.getParsingContext().pathAsPointer());

        
        assertToken(JsonToken.START_OBJECT, p.nextToken());
        assertSame(JsonPointer.EMPTY, p.getParsingContext().pathAsPointer());

        assertToken(JsonToken.FIELD_NAME, p.nextToken()); 
        assertEquals("/a", p.getParsingContext().pathAsPointer().toString());

        assertToken(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertEquals("/a", p.getParsingContext().pathAsPointer().toString());

        assertToken(JsonToken.FIELD_NAME, p.nextToken()); 
        assertEquals("/array", p.getParsingContext().pathAsPointer().toString());
        assertToken(JsonToken.START_ARRAY, p.nextToken());
        assertEquals("/array", p.getParsingContext().pathAsPointer().toString());
        assertToken(JsonToken.VALUE_NUMBER_INT, p.nextToken()); 
        assertEquals("/array/0", p.getParsingContext().pathAsPointer().toString());
        assertToken(JsonToken.VALUE_NUMBER_INT, p.nextToken()); 
        assertEquals("/array/1", p.getParsingContext().pathAsPointer().toString());
        assertToken(JsonToken.START_ARRAY, p.nextToken());
        assertEquals("/array/2", p.getParsingContext().pathAsPointer().toString());
        assertToken(JsonToken.VALUE_NUMBER_INT, p.nextToken()); 
        assertEquals("/array/2/0", p.getParsingContext().pathAsPointer().toString());
        assertToken(JsonToken.END_ARRAY, p.nextToken());
        assertEquals("/array/2", p.getParsingContext().pathAsPointer().toString());
        assertToken(JsonToken.VALUE_NUMBER_INT, p.nextToken()); 
        assertEquals("/array/3", p.getParsingContext().pathAsPointer().toString());
        assertToken(JsonToken.START_OBJECT, p.nextToken());
        assertEquals("/array/4", p.getParsingContext().pathAsPointer().toString());
        assertToken(JsonToken.FIELD_NAME, p.nextToken()); 
        assertEquals("/array/4/obInArray", p.getParsingContext().pathAsPointer().toString());
        assertToken(JsonToken.VALUE_NUMBER_INT, p.nextToken()); 
        assertEquals("/array/4/obInArray", p.getParsingContext().pathAsPointer().toString());
        assertToken(JsonToken.END_OBJECT, p.nextToken());
        assertEquals("/array/4", p.getParsingContext().pathAsPointer().toString());
        assertToken(JsonToken.END_ARRAY, p.nextToken()); 
        assertEquals("/array", p.getParsingContext().pathAsPointer().toString());

        assertToken(JsonToken.FIELD_NAME, p.nextToken()); 
        assertEquals("/ob", p.getParsingContext().pathAsPointer().toString());
        assertToken(JsonToken.START_OBJECT, p.nextToken());
        assertEquals("/ob", p.getParsingContext().pathAsPointer().toString());
        assertToken(JsonToken.FIELD_NAME, p.nextToken()); 
        assertEquals("/ob/first", p.getParsingContext().pathAsPointer().toString());
        assertToken(JsonToken.START_ARRAY, p.nextToken());
        assertEquals("/ob/first", p.getParsingContext().pathAsPointer().toString());
        assertToken(JsonToken.VALUE_FALSE, p.nextToken());
        assertEquals("/ob/first/0", p.getParsingContext().pathAsPointer().toString());
        assertToken(JsonToken.VALUE_TRUE, p.nextToken());
        assertEquals("/ob/first/1", p.getParsingContext().pathAsPointer().toString());
        assertToken(JsonToken.END_ARRAY, p.nextToken());
        assertEquals("/ob/first", p.getParsingContext().pathAsPointer().toString());
        assertToken(JsonToken.FIELD_NAME, p.nextToken()); 
        assertEquals("/ob/second", p.getParsingContext().pathAsPointer().toString());
        assertToken(JsonToken.START_OBJECT, p.nextToken());
        assertEquals("/ob/second", p.getParsingContext().pathAsPointer().toString());
        assertToken(JsonToken.FIELD_NAME, p.nextToken()); 
        assertEquals("/ob/second/sub", p.getParsingContext().pathAsPointer().toString());
        assertToken(JsonToken.VALUE_NUMBER_INT, p.nextToken()); 
        assertEquals("/ob/second/sub", p.getParsingContext().pathAsPointer().toString());
        assertToken(JsonToken.END_OBJECT, p.nextToken());
        assertEquals("/ob/second", p.getParsingContext().pathAsPointer().toString());
        assertToken(JsonToken.END_OBJECT, p.nextToken()); 
        assertEquals("/ob", p.getParsingContext().pathAsPointer().toString());

        assertToken(JsonToken.FIELD_NAME, p.nextToken()); 
        assertEquals("/b", p.getParsingContext().pathAsPointer().toString());
        assertToken(JsonToken.VALUE_TRUE, p.nextToken());
        assertEquals("/b", p.getParsingContext().pathAsPointer().toString());

        assertToken(JsonToken.END_OBJECT, p.nextToken());
        assertSame(JsonPointer.EMPTY, p.getParsingContext().pathAsPointer());

        assertNull(p.nextToken());
        p.close();
    }

// com.fasterxml.jackson.core.PointerFromContextTest::testViaGenerator
    public void testViaGenerator() throws Exception
    {
        StringWriter w = new StringWriter();
        JsonGenerator g = JSON_F.createGenerator(w);
        assertSame(JsonPointer.EMPTY, g.getOutputContext().pathAsPointer());

        g.writeStartArray();
        
        assertSame(JsonPointer.EMPTY, g.getOutputContext().pathAsPointer());
        g.writeBoolean(true);
        assertEquals("/0", g.getOutputContext().pathAsPointer().toString());

        g.writeStartObject();
        assertEquals("/1", g.getOutputContext().pathAsPointer().toString());
        g.writeFieldName("x");
        assertEquals("/1/x", g.getOutputContext().pathAsPointer().toString());
        g.writeString("foo");
        assertEquals("/1/x", g.getOutputContext().pathAsPointer().toString());
        g.writeFieldName("stats");
        assertEquals("/1/stats", g.getOutputContext().pathAsPointer().toString());
        g.writeStartObject();
        assertEquals("/1/stats", g.getOutputContext().pathAsPointer().toString());
        g.writeFieldName("rate");
        assertEquals("/1/stats/rate", g.getOutputContext().pathAsPointer().toString());
        g.writeNumber(13);
        assertEquals("/1/stats/rate", g.getOutputContext().pathAsPointer().toString());
        g.writeEndObject();
        assertEquals("/1/stats", g.getOutputContext().pathAsPointer().toString());

        g.writeEndObject();
        assertEquals("/1", g.getOutputContext().pathAsPointer().toString());

        g.writeEndArray();
        assertSame(JsonPointer.EMPTY, g.getOutputContext().pathAsPointer());
        g.close();
        w.close();
    }

// com.fasterxml.jackson.core.PointerFromContextTest::testParserWithRoot
    public void testParserWithRoot() throws Exception
    {
        final String JSON = aposToQuotes("{'a':1,'b':3}\n"
                +"{'a':5,'c':[1,2]}\n[1,2]\n");
        JsonParser p = JSON_F.createParser(JSON);
        
        assertSame(JsonPointer.EMPTY, p.getParsingContext().pathAsPointer(true));

        
        assertToken(JsonToken.START_OBJECT, p.nextToken());
        assertEquals("/0", p.getParsingContext().pathAsPointer(true).toString());
        assertToken(JsonToken.FIELD_NAME, p.nextToken()); 
        assertEquals("/0/a", p.getParsingContext().pathAsPointer(true).toString());
        assertToken(JsonToken.VALUE_NUMBER_INT, p.nextToken()); 
        assertEquals("/0/a", p.getParsingContext().pathAsPointer(true).toString());
        assertToken(JsonToken.FIELD_NAME, p.nextToken()); 
        assertEquals("/0/b", p.getParsingContext().pathAsPointer(true).toString());
        assertToken(JsonToken.VALUE_NUMBER_INT, p.nextToken()); 
        assertEquals("/0/b", p.getParsingContext().pathAsPointer(true).toString());
        assertToken(JsonToken.END_OBJECT, p.nextToken());
        assertEquals("/0", p.getParsingContext().pathAsPointer(true).toString());

        assertToken(JsonToken.START_OBJECT, p.nextToken());
        assertEquals("/1", p.getParsingContext().pathAsPointer(true).toString());
        assertToken(JsonToken.FIELD_NAME, p.nextToken()); 
        assertEquals("/1/a", p.getParsingContext().pathAsPointer(true).toString());
        assertToken(JsonToken.VALUE_NUMBER_INT, p.nextToken()); 
        assertEquals("/1/a", p.getParsingContext().pathAsPointer(true).toString());
        assertToken(JsonToken.FIELD_NAME, p.nextToken()); 
        assertEquals("/1/c", p.getParsingContext().pathAsPointer(true).toString());

        assertToken(JsonToken.START_ARRAY, p.nextToken());
        assertEquals("/1/c", p.getParsingContext().pathAsPointer(true).toString());
        assertToken(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertEquals("/1/c/0", p.getParsingContext().pathAsPointer(true).toString());
        assertToken(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertEquals("/1/c/1", p.getParsingContext().pathAsPointer(true).toString());
        assertToken(JsonToken.END_ARRAY, p.nextToken());
        assertEquals("/1/c", p.getParsingContext().pathAsPointer(true).toString());
        assertToken(JsonToken.END_OBJECT, p.nextToken());
        assertEquals("/1", p.getParsingContext().pathAsPointer(true).toString());

        assertToken(JsonToken.START_ARRAY, p.nextToken());
        assertEquals("/2", p.getParsingContext().pathAsPointer(true).toString());
        assertToken(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertEquals("/2/0", p.getParsingContext().pathAsPointer(true).toString());
        assertToken(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertEquals("/2/1", p.getParsingContext().pathAsPointer(true).toString());
        assertToken(JsonToken.END_ARRAY, p.nextToken());
        assertEquals("/2", p.getParsingContext().pathAsPointer(true).toString());

        assertNull(p.nextToken());

        
        
        
        

        assertEquals("/2", p.getParsingContext().pathAsPointer(true).toString());

        p.close();
    }

// com.fasterxml.jackson.core.PointerFromContextTest::testGeneratorWithRoot
    public void testGeneratorWithRoot() throws Exception
    {
        StringWriter w = new StringWriter();
        JsonGenerator g = JSON_F.createGenerator(w);
        assertSame(JsonPointer.EMPTY, g.getOutputContext().pathAsPointer(true));

        g.writeStartArray();
        assertEquals("/0", g.getOutputContext().pathAsPointer(true).toString());
        g.writeBoolean(true);
        assertEquals("/0/0", g.getOutputContext().pathAsPointer(true).toString());

        g.writeStartObject();
        assertEquals("/0/1", g.getOutputContext().pathAsPointer(true).toString());
        g.writeFieldName("x");
        assertEquals("/0/1/x", g.getOutputContext().pathAsPointer(true).toString());
        g.writeString("foo");
        assertEquals("/0/1/x", g.getOutputContext().pathAsPointer(true).toString());
        g.writeEndObject();
        assertEquals("/0/1", g.getOutputContext().pathAsPointer(true).toString());
        g.writeEndArray();
        assertEquals("/0", g.getOutputContext().pathAsPointer(true).toString());

        g.writeBoolean(true);
        assertEquals("/1", g.getOutputContext().pathAsPointer(true).toString());

        g.writeStartArray();
        assertEquals("/2", g.getOutputContext().pathAsPointer(true).toString());
        g.writeString("foo");
        assertEquals("/2/0", g.getOutputContext().pathAsPointer(true).toString());
        g.writeString("bar");
        assertEquals("/2/1", g.getOutputContext().pathAsPointer(true).toString());
        g.writeEndArray();
        assertEquals("/2", g.getOutputContext().pathAsPointer(true).toString());

        
        assertEquals("/2", g.getOutputContext().pathAsPointer(true).toString());
        
        g.close();
    }

// com.fasterxml.jackson.core.TestExceptions::testOriginalMesssage
    public void testOriginalMesssage()
    {
        JsonProcessingException exc = new JsonParseException(null, "Foobar", JsonLocation.NA);
        String msg = exc.getMessage();
        String orig = exc.getOriginalMessage();
        assertEquals("Foobar", orig);
        assertTrue(msg.length() > orig.length());

        
        JsonProcessingException exc2 = new JsonProcessingException("Second",
                JsonLocation.NA, exc);
        assertSame(exc, exc2.getCause());
        exc2.clearLocation();
        assertNull(exc2.getLocation());

        
        JsonProcessingException exc3 = new JsonProcessingException(exc);
        assertNull(exc3.getOriginalMessage());
        assertEquals("N/A", exc3.getMessage());

        assertEquals("com.fasterxml.jackson.core.JsonProcessingException: N/A", exc3.toString());
    }

// com.fasterxml.jackson.core.TestExceptions::testAccessToParser
    public void testAccessToParser() throws Exception
    {
        JsonParser p = JSON_F.createParser("{}");
        assertToken(JsonToken.START_OBJECT, p.nextToken());
        JsonParseException e = new JsonParseException(p, "Test!");
        assertSame(p, e.getProcessor());
        assertEquals("Test!", e.getOriginalMessage());
        JsonLocation loc = e.getLocation();
        assertNotNull(loc);
        assertEquals(2, loc.getColumnNr());
        assertEquals(1, loc.getLineNr());
        p.close();
    }

// com.fasterxml.jackson.core.TestExceptions::testAccessToGenerator
    public void testAccessToGenerator() throws Exception
    {
        StringWriter w = new StringWriter();
        JsonGenerator g = JSON_F.createGenerator(w);
        g.writeStartObject();
        JsonGenerationException e = new JsonGenerationException("Test!", g);
        assertSame(g, e.getProcessor());
        assertEquals("Test!", e.getOriginalMessage());
        g.close();
    }

// com.fasterxml.jackson.core.TestExceptions::testEofExceptionsBytes
    public void testEofExceptionsBytes() throws Exception {
        _testEofExceptions(MODE_INPUT_STREAM);
    }

// com.fasterxml.jackson.core.TestExceptions::testEofExceptionsChars
    public void testEofExceptionsChars() throws Exception {
        _testEofExceptions(MODE_READER);
    }

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

// com.fasterxml.jackson.core.TestJDKSerializability::testLocation
    public void testLocation() throws Exception
    {
        JsonFactory jf = new JsonFactory();
        JsonParser jp = jf.createParser("  { }");
        assertToken(JsonToken.START_OBJECT, jp.nextToken());
        JsonLocation loc = jp.getCurrentLocation();

        byte[] stuff = jdkSerialize(loc);
        JsonLocation loc2 = jdkDeserialize(stuff);
        assertNotNull(loc2);
        
        assertEquals(loc.getLineNr(), loc2.getLineNr());
        assertEquals(loc.getColumnNr(), loc2.getColumnNr());
        jp.close();
    }

// com.fasterxml.jackson.core.TestJDKSerializability::testParseException
    public void testParseException() throws Exception
    {
        JsonFactory jf = new JsonFactory();
        JsonParser p = jf.createParser("  { garbage! }");
        JsonParseException exc = null;
        try {
            p.nextToken();
            p.nextToken();
            fail("Should not get here");
        } catch (JsonParseException e) {
            exc = e;
        }
        p.close();
        byte[] stuff = jdkSerialize(exc);
        JsonParseException result = jdkDeserialize(stuff);
        assertNotNull(result);
    }

// com.fasterxml.jackson.core.TestJDKSerializability::testGenerationException
    public void testGenerationException() throws Exception
    {
        JsonFactory jf = new JsonFactory();
        JsonGenerator g = jf.createGenerator(new ByteArrayOutputStream());
        JsonGenerationException exc = null;
        g.writeStartObject();
        try {
            g.writeNumber(4);
            fail("Should not get here");
        } catch (JsonGenerationException e) {
            exc = e;
        }
        g.close();
        byte[] stuff = jdkSerialize(exc);
        JsonGenerationException result = jdkDeserialize(stuff);
        assertNotNull(result);
    }

// com.fasterxml.jackson.core.TestJsonPointer::testSimplePath
    public void testSimplePath() throws Exception
    {
        final String INPUT = "/Image/15/name";

        JsonPointer ptr = JsonPointer.compile(INPUT);
        assertFalse(ptr.matches());
        assertEquals(-1, ptr.getMatchingIndex());
        assertEquals("Image", ptr.getMatchingProperty());
        assertEquals("/Image/15", ptr.head().toString());
        assertEquals(INPUT, ptr.toString());

        ptr = ptr.tail();
        assertNotNull(ptr);
        assertFalse(ptr.matches());
        assertEquals(15, ptr.getMatchingIndex());
        assertEquals("15", ptr.getMatchingProperty());
        assertEquals("/15/name", ptr.toString());
        assertEquals("/15", ptr.head().toString());

        assertEquals("", ptr.head().head().toString());
        assertNull(ptr.head().head().head());

        ptr = ptr.tail();
        assertNotNull(ptr);
        assertFalse(ptr.matches());
        assertEquals(-1, ptr.getMatchingIndex());
        assertEquals("name", ptr.getMatchingProperty());
        assertEquals("/name", ptr.toString());
        assertEquals("", ptr.head().toString());
        assertSame(JsonPointer.EMPTY, ptr.head());

        
        ptr = ptr.tail();
        assertTrue(ptr.matches());
        assertNull(ptr.tail());
        assertNull(ptr.head());
        assertEquals("", ptr.getMatchingProperty());
        assertEquals(-1, ptr.getMatchingIndex());
    }

// com.fasterxml.jackson.core.TestJsonPointer::testSimplePathLonger
    public void testSimplePathLonger() throws Exception
    {
        final String INPUT = "/a/b/c/d/e/f/0";
        JsonPointer ptr = JsonPointer.compile(INPUT);
        assertFalse(ptr.matches());
        assertEquals(-1, ptr.getMatchingIndex());
        assertEquals("a", ptr.getMatchingProperty());
        assertEquals("/a/b/c/d/e/f", ptr.head().toString());
        assertEquals("/b/c/d/e/f/0", ptr.tail().toString());
        assertEquals("/0", ptr.last().toString());
        assertEquals(INPUT, ptr.toString());
    }

// com.fasterxml.jackson.core.TestJsonPointer::testWonkyNumber173
    public void testWonkyNumber173() throws Exception
    {
        JsonPointer ptr = JsonPointer.compile("/1e0");
        assertFalse(ptr.matches());
    }

// com.fasterxml.jackson.core.TestJsonPointer::testIZeroIndex
    public void testIZeroIndex() throws Exception
    {
        JsonPointer ptr = JsonPointer.compile("/0");
        assertEquals(0, ptr.getMatchingIndex());
        ptr = JsonPointer.compile("/00");
        assertEquals(-1, ptr.getMatchingIndex());
    }

// com.fasterxml.jackson.core.TestJsonPointer::testLast
    public void testLast()
    {
        final String INPUT = "/Image/15/name";

        JsonPointer ptr = JsonPointer.compile(INPUT);
        JsonPointer leaf = ptr.last();

        assertEquals("name", leaf.getMatchingProperty());
    }

// com.fasterxml.jackson.core.TestJsonPointer::testEmpty
    public void testEmpty()
    {
        
        
        JsonPointer ptr = JsonPointer.compile("/");
        assertNotNull(ptr);
        assertNotSame(JsonPointer.EMPTY, ptr);
        assertEquals("/", ptr.toString());
    }

// com.fasterxml.jackson.core.TestJsonPointer::testAppend
    public void testAppend()
    {
        final String INPUT = "/Image/15/name";
        final String APPEND = "/extension";

        JsonPointer ptr = JsonPointer.compile(INPUT);
        JsonPointer apd = JsonPointer.compile(APPEND);

        JsonPointer appended = ptr.append(apd);

        assertEquals("extension", appended.last().getMatchingProperty());
    }

// com.fasterxml.jackson.core.TestJsonPointer::testAppendWithFinalSlash
    public void testAppendWithFinalSlash()
    {
        final String INPUT = "/Image/15/name/";
        final String APPEND = "/extension";

        JsonPointer ptr = JsonPointer.compile(INPUT);
        JsonPointer apd = JsonPointer.compile(APPEND);

        JsonPointer appended = ptr.append(apd);

        assertEquals("extension", appended.last().getMatchingProperty());
    }

// com.fasterxml.jackson.core.TestJsonPointer::testQuotedPath
    public void testQuotedPath() throws Exception
    {
        final String INPUT = "/w~1out/til~0de/a~1b";

        JsonPointer ptr = JsonPointer.compile(INPUT);
        assertFalse(ptr.matches());
        assertEquals(-1, ptr.getMatchingIndex());
        assertEquals("w/out", ptr.getMatchingProperty());
        assertEquals("/w~1out/til~0de", ptr.head().toString());
        assertEquals(INPUT, ptr.toString());

        ptr = ptr.tail();
        assertNotNull(ptr);
        assertFalse(ptr.matches());
        assertEquals(-1, ptr.getMatchingIndex());
        assertEquals("til~de", ptr.getMatchingProperty());
        assertEquals("/til~0de", ptr.head().toString());
        assertEquals("/til~0de/a~1b", ptr.toString());

        ptr = ptr.tail();
        assertNotNull(ptr);
        assertFalse(ptr.matches());
        assertEquals(-1, ptr.getMatchingIndex());
        assertEquals("a/b", ptr.getMatchingProperty());
        assertEquals("/a~1b", ptr.toString());
        assertEquals("", ptr.head().toString());

        
        ptr = ptr.tail();
        assertTrue(ptr.matches());
        assertNull(ptr.tail());
    }

// com.fasterxml.jackson.core.TestJsonPointer::testLongNumbers
    public void testLongNumbers() throws Exception
    {
        final long LONG_ID = ((long) Integer.MAX_VALUE) + 1L;
        
        final String INPUT = "/User/"+LONG_ID;

        JsonPointer ptr = JsonPointer.compile(INPUT);
        assertEquals("User", ptr.getMatchingProperty());
        assertEquals(INPUT, ptr.toString());

        ptr = ptr.tail();
        assertNotNull(ptr);
        assertFalse(ptr.matches());
        
        assertEquals(-1, ptr.getMatchingIndex());
        assertEquals(String.valueOf(LONG_ID), ptr.getMatchingProperty());

        
        ptr = ptr.tail();
        assertTrue(ptr.matches());
        assertNull(ptr.tail());
    }

// com.fasterxml.jackson.core.TestLocation::testBasics
    public void testBasics()
    {
        JsonLocation loc1 = new JsonLocation("src", 10L, 10L, 1, 2);
        JsonLocation loc2 = new JsonLocation(null, 10L, 10L, 3, 2);
        assertEquals(loc1, loc1);
        assertFalse(loc1.equals(null));
        assertFalse(loc1.equals(loc2));
        assertFalse(loc2.equals(loc1));

        
        assertTrue(loc1.hashCode() != 0);
        assertTrue(loc2.hashCode() != 0);
    }

// com.fasterxml.jackson.core.TestLocation::testBasicToString
    public void testBasicToString() throws Exception
    {
        
        assertEquals("[Source: UNKNOWN; line: 3, column: 2]",
                new JsonLocation(null, 10L, 10L, 3, 2).toString());

        
        assertEquals("[Source: (String)\"string-source\"; line: 1, column: 2]",
                new JsonLocation("string-source", 10L, 10L, 1, 2).toString());

        
        assertEquals("[Source: (char[])\"chars-source\"; line: 1, column: 2]",
                new JsonLocation("chars-source".toCharArray(), 10L, 10L, 1, 2).toString());

        
        assertEquals("[Source: (byte[])\"bytes-source\"; line: 1, column: 2]",
                new JsonLocation("bytes-source".getBytes("UTF-8"), 10L, 10L, 1, 2).toString());

        
        assertEquals("[Source: (ByteArrayInputStream); line: 1, column: 2]",
                new JsonLocation(new ByteArrayInputStream(new byte[0]), 10L, 10L, 1, 2).toString());

        
        assertEquals("[Source: (InputStream); line: 1, column: 2]",
                new JsonLocation(InputStream.class, 10L, 10L, 1, 2).toString());

        
        Foobar srcRef = new Foobar();
        assertEquals("[Source: ("+srcRef.getClass().getName()+"); line: 1, column: 2]",
                new JsonLocation(srcRef, 10L, 10L, 1, 2).toString());
    }

// com.fasterxml.jackson.core.TestLocation::testTruncatedSource
    public void testTruncatedSource() throws Exception
    {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < JsonLocation.MAX_CONTENT_SNIPPET; ++i) {
            sb.append("x");
        }
        String main = sb.toString();
        String json = main + "yyy";
        JsonLocation loc = new JsonLocation(json, 0L, 0L, 1, 1);
        String desc = loc.sourceDescription();
        assertEquals(String.format("(String)\"%s\"[truncated 3 chars]", main), desc);

        
        loc = new JsonLocation(json.getBytes("UTF-8"), 0L, 0L, 1, 1);
        desc = loc.sourceDescription();
        assertEquals(String.format("(byte[])\"%s\"[truncated 3 bytes]", main), desc);
    }

// com.fasterxml.jackson.core.TestLocation::testDisableSourceInclusion
    public void testDisableSourceInclusion() throws Exception
    {
        JsonFactory f = new JsonFactory();
        f.disable(JsonParser.Feature.INCLUDE_SOURCE_IN_LOCATION);

        JsonParser p = f.createParser("[ foobar ]");
        assertToken(JsonToken.START_ARRAY, p.nextToken());
        try {
            p.nextToken();
            fail("Shouldn't have passed");
        } catch (JsonParseException e) {
            verifyException(e, "unrecognized token");
            JsonLocation loc = e.getLocation();
            assertNull(loc.getSourceRef());
            assertEquals("UNKNOWN", loc.sourceDescription());
        }
        p.close();

        
        p = f.createParser("[ foobar ]".getBytes("UTF-8"));
        assertToken(JsonToken.START_ARRAY, p.nextToken());
        try {
            p.nextToken();
            fail("Shouldn't have passed");
        } catch (JsonParseException e) {
            verifyException(e, "unrecognized token");
            JsonLocation loc = e.getLocation();
            assertNull(loc.getSourceRef());
            assertEquals("UNKNOWN", loc.sourceDescription());
        }
        p.close();
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

// com.fasterxml.jackson.core.TestVersions::testMisc
    public void testMisc() {
        Version unk = Version.unknownVersion();
        assertEquals("0.0.0", unk.toString());
        assertEquals("//0.0.0", unk.toFullString());
        assertTrue(unk.equals(unk));

        Version other = new Version(2, 8, 4, "",
                "groupId", "artifactId");
        assertEquals("2.8.4", other.toString());
        assertEquals("groupId/artifactId/2.8.4", other.toFullString());
    }

// com.fasterxml.jackson.core.base64.Base64BinaryParsingTest::testBase64UsingInputStream
    public void testBase64UsingInputStream() throws Exception
    {
        _testBase64Text(MODE_INPUT_STREAM);
        _testBase64Text(MODE_INPUT_STREAM_THROTTLED);
        _testBase64Text(MODE_DATA_INPUT);
    }

// com.fasterxml.jackson.core.base64.Base64BinaryParsingTest::testBase64UsingReader
    public void testBase64UsingReader() throws Exception
    {
        _testBase64Text(MODE_READER);
    }

// com.fasterxml.jackson.core.base64.Base64BinaryParsingTest::testStreaming
    public void testStreaming() throws IOException
    {
        _testStreaming(MODE_INPUT_STREAM);
        _testStreaming(MODE_INPUT_STREAM_THROTTLED);
        _testStreaming(MODE_DATA_INPUT);
        _testStreaming(MODE_READER);
    }

// com.fasterxml.jackson.core.base64.Base64BinaryParsingTest::testSimple
    public void testSimple() throws IOException
    {
        for (int mode : ALL_MODES) {
            
            _testSimple(mode, false, false);
            _testSimple(mode, true, false);
            _testSimple(mode, false, true);
            _testSimple(mode, true, true);
        }
    }

// com.fasterxml.jackson.core.base64.Base64BinaryParsingTest::testInArray
    public void testInArray() throws IOException
    {
        for (int mode : ALL_MODES) {
            _testInArray(mode);
        }
    }

// com.fasterxml.jackson.core.base64.Base64BinaryParsingTest::testWithEscaped
    public void testWithEscaped() throws IOException {
        for (int mode : ALL_MODES) {
            _testEscaped(mode);
        }
    }

// com.fasterxml.jackson.core.base64.Base64BinaryParsingTest::testWithEscapedPadding
    public void testWithEscapedPadding() throws IOException {
        for (int mode : ALL_MODES) {
            _testEscapedPadding(mode);
        }
    }

// com.fasterxml.jackson.core.base64.Base64BinaryParsingTest::testInvalidTokenForBase64
    public void testInvalidTokenForBase64() throws IOException
    {
        for (int mode : ALL_MODES) {

            
            JsonParser p = createParser(mode, "[ ]");
            assertToken(JsonToken.START_ARRAY, p.nextToken());
            try {
                p.getBinaryValue();
                fail("Should not pass");
            } catch (JsonParseException e) {
                verifyException(e, "current token");
                verifyException(e, "can not access as binary");
            }
            p.close();
        }
    }

// com.fasterxml.jackson.core.base64.Base64BinaryParsingTest::testInvalidChar
    public void testInvalidChar() throws IOException
    {
        for (int mode : ALL_MODES) {

            
            JsonParser p = createParser(mode, quote("a==="));
            assertToken(JsonToken.VALUE_STRING, p.nextToken());
            try {
                p.getBinaryValue(Base64Variants.MIME);
                fail("Should not pass");
            } catch (JsonParseException e) {
                verifyException(e, "padding only legal");
            }
            p.close();

            
            p = createParser(mode, quote("ab de"));
            assertToken(JsonToken.VALUE_STRING, p.nextToken());
            try {
                p.getBinaryValue(Base64Variants.MIME);
                fail("Should not pass");
            } catch (JsonParseException e) {
                verifyException(e, "illegal white space");
            }
            p.close();

            
            p = createParser(mode, quote("ab#?"));
            assertToken(JsonToken.VALUE_STRING, p.nextToken());
            try {
                p.getBinaryValue(Base64Variants.MIME);
                fail("Should not pass");
            } catch (JsonParseException e) {
                verifyException(e, "illegal character '#'");
            }
            p.close();
        }
    }

// com.fasterxml.jackson.core.base64.Base64BinaryParsingTest::testOkMissingPadding
    public void testOkMissingPadding() throws IOException {
        final byte[] DOC1 = new byte[] { (byte) 0xAD };
        _testOkMissingPadding(DOC1, MODE_INPUT_STREAM);
        _testOkMissingPadding(DOC1, MODE_INPUT_STREAM_THROTTLED);
        _testOkMissingPadding(DOC1, MODE_READER);
        _testOkMissingPadding(DOC1, MODE_DATA_INPUT);

        final byte[] DOC2 = new byte[] { (byte) 0xAC, (byte) 0xDC };
        _testOkMissingPadding(DOC2, MODE_INPUT_STREAM);
        _testOkMissingPadding(DOC2, MODE_INPUT_STREAM_THROTTLED);
        _testOkMissingPadding(DOC2, MODE_READER);
        _testOkMissingPadding(DOC2, MODE_DATA_INPUT);
    }

// com.fasterxml.jackson.core.base64.Base64BinaryParsingTest::testFailDueToMissingPadding
    public void testFailDueToMissingPadding() throws IOException {
        final String DOC1 = quote("fQ"); 
        _testFailDueToMissingPadding(DOC1, MODE_INPUT_STREAM);
        _testFailDueToMissingPadding(DOC1, MODE_INPUT_STREAM_THROTTLED);
        _testFailDueToMissingPadding(DOC1, MODE_READER);
        _testFailDueToMissingPadding(DOC1, MODE_DATA_INPUT);

        final String DOC2 = quote("A/A"); 
        _testFailDueToMissingPadding(DOC2, MODE_INPUT_STREAM);
        _testFailDueToMissingPadding(DOC2, MODE_INPUT_STREAM_THROTTLED);
        _testFailDueToMissingPadding(DOC2, MODE_READER);
        _testFailDueToMissingPadding(DOC2, MODE_DATA_INPUT);
    }

// com.fasterxml.jackson.core.base64.Base64CodecTest::testVariantAccess
    public void testVariantAccess()
    {
        for (Base64Variant var : new Base64Variant[] {
                Base64Variants.MIME,
                Base64Variants.MIME_NO_LINEFEEDS,
                Base64Variants.MODIFIED_FOR_URL,
                Base64Variants.PEM
        }) {
            assertSame(var, Base64Variants.valueOf(var.getName()));
        }

        try {
            Base64Variants.valueOf("foobar");
            fail("Should not pass");
        } catch (IllegalArgumentException e) {
            verifyException(e, "No Base64Variant with name 'foobar'");
        }
    }

// com.fasterxml.jackson.core.base64.Base64CodecTest::testProps
    public void testProps()
    {
        Base64Variant std = Base64Variants.MIME;
        
        assertEquals("MIME", std.getName());
        assertEquals("MIME", std.toString());
        assertTrue(std.usesPadding());
        assertFalse(std.usesPaddingChar('X'));
        assertEquals('=', std.getPaddingChar());
        assertTrue(std.usesPaddingChar('='));
        assertEquals((byte) '=', std.getPaddingByte());
        assertEquals(76, std.getMaxLineLength());
    }

// com.fasterxml.jackson.core.base64.Base64CodecTest::testCharEncoding
    public void testCharEncoding() throws Exception
    {
        Base64Variant std = Base64Variants.MIME;
        assertEquals(Base64Variant.BASE64_VALUE_INVALID, std.decodeBase64Char('?'));
        assertEquals(Base64Variant.BASE64_VALUE_INVALID, std.decodeBase64Char((int) '?'));
        assertEquals(Base64Variant.BASE64_VALUE_INVALID, std.decodeBase64Char((char) 0xA0));
        assertEquals(Base64Variant.BASE64_VALUE_INVALID, std.decodeBase64Char(0xA0));

        assertEquals(Base64Variant.BASE64_VALUE_INVALID, std.decodeBase64Byte((byte) '?'));
        assertEquals(Base64Variant.BASE64_VALUE_INVALID, std.decodeBase64Byte((byte) 0xA0));
        
        assertEquals(0, std.decodeBase64Char('A'));
        assertEquals(1, std.decodeBase64Char((int) 'B'));
        assertEquals(2, std.decodeBase64Char((byte)'C'));

        assertEquals(0, std.decodeBase64Byte((byte) 'A'));
        assertEquals(1, std.decodeBase64Byte((byte) 'B'));
        assertEquals(2, std.decodeBase64Byte((byte)'C'));
        
        assertEquals('/', std.encodeBase64BitsAsChar(63));
        assertEquals((byte) 'b', std.encodeBase64BitsAsByte(27));

        String EXP_STR = "HwdJ";
        int TRIPLET = 0x1F0749;
        StringBuilder sb = new StringBuilder();
        std.encodeBase64Chunk(sb, TRIPLET);
        assertEquals(EXP_STR, sb.toString());

        byte[] exp = EXP_STR.getBytes("UTF-8");
        byte[] act = new byte[exp.length];
        std.encodeBase64Chunk(TRIPLET, act, 0);
        Assert.assertArrayEquals(exp, act);
    }

// com.fasterxml.jackson.core.base64.Base64CodecTest::testConvenienceMethods
    public void testConvenienceMethods() throws Exception
    {
        Base64Variant std = Base64Variants.MIME;

        byte[] input = new byte[] { 1, 2, 34, 127, -1 };
        String encoded = std.encode(input, false);
        byte[] decoded = std.decode(encoded);    
        Assert.assertArrayEquals(input, decoded);

        assertEquals(quote(encoded), std.encode(input, true));

        
        decoded = std.decode("\n"+encoded);
        Assert.assertArrayEquals(input, decoded);
        decoded = std.decode("   "+encoded);
        Assert.assertArrayEquals(input, decoded);
        decoded = std.decode(encoded + "   ");
        Assert.assertArrayEquals(input, decoded);
        decoded = std.decode(encoded + "\n");
        Assert.assertArrayEquals(input, decoded);
    }

// com.fasterxml.jackson.core.base64.Base64CodecTest::testErrors
    public void testErrors() throws Exception
    {
        try {
            Base64Variant b = new Base64Variant("foobar", "xyz", false, '!', 24);
            fail("Should not pass");
        } catch (IllegalArgumentException iae) {
            verifyException(iae, "length must be exactly");
        }
        try {
            Base64Variants.MIME.decode("!@##@%$#%&*^(&)(*");
        } catch (IllegalArgumentException iae) {
            verifyException(iae, "Illegal character");
        }

        
        final String BASE64_HELLO = "aGVsbG8=!";
        try {
            Base64Variants.MIME.decode(BASE64_HELLO);
            fail("Should not pass");
        } catch (IllegalArgumentException iae) {
            verifyException(iae, "Illegal character");
        }
    }

// com.fasterxml.jackson.core.base64.Base64GenerationTest::testStreamingBinaryWrites
    public void testStreamingBinaryWrites() throws Exception
    {
        _testStreamingWrites(JSON_F, true);
        _testStreamingWrites(JSON_F, false);
    }

// com.fasterxml.jackson.core.base64.Base64GenerationTest::testIssue55
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

// com.fasterxml.jackson.core.base64.Base64GenerationTest::testSimpleBinaryWrite
    public void testSimpleBinaryWrite() throws Exception
    {
        _testSimpleBinaryWrite(false);
        _testSimpleBinaryWrite(true);
    }

// com.fasterxml.jackson.core.base64.Base64GenerationTest::testBinaryAsEmbeddedObject
    public void testBinaryAsEmbeddedObject() throws Exception
    {
        JsonGenerator g;

        StringWriter sw = new StringWriter();
        g = JSON_F.createGenerator(sw);
        g.writeEmbeddedObject(WIKIPEDIA_BASE64_AS_BYTES);
        g.close();
        assertEquals(quote(WIKIPEDIA_BASE64_ENCODED), sw.toString());

        ByteArrayOutputStream bytes =  new ByteArrayOutputStream(100);
        g = JSON_F.createGenerator(bytes);
        g.writeEmbeddedObject(WIKIPEDIA_BASE64_AS_BYTES);
        g.close();
        assertEquals(quote(WIKIPEDIA_BASE64_ENCODED), bytes.toString("UTF-8"));
    }

// com.fasterxml.jackson.core.filter.BasicGeneratorFilteringTest::testNonFiltering
    public void testNonFiltering() throws Exception
    {
        
        StringWriter w = new StringWriter();
        JsonGenerator gen = JSON_F.createGenerator(w);
        final String JSON = "{'a':123,'array':[1,2],'ob':{'value0':2,'value':3,'value2':4},'b':true}";
        writeJsonDoc(JSON_F, JSON, gen);
        assertEquals(aposToQuotes(
                "{'a':123,'array':[1,2],'ob':{'value0':2,'value':3,'value2':4},'b':true}"),
                w.toString());
    }

// com.fasterxml.jackson.core.filter.BasicGeneratorFilteringTest::testSingleMatchFilteringWithoutPath
    public void testSingleMatchFilteringWithoutPath() throws Exception
    {
        StringWriter w = new StringWriter();
        JsonGenerator gen = new FilteringGeneratorDelegate(JSON_F.createGenerator(w),
                new NameMatchFilter("value"),
                false, 
                false 
                );
        final String JSON = "{'a':123,'array':[1,2],'ob':{'value0':2,'value':3,'value2':4},'b':true}";
        writeJsonDoc(JSON_F, JSON, gen);

        
        
        

        assertEquals(aposToQuotes("3"), w.toString());
    }

// com.fasterxml.jackson.core.filter.BasicGeneratorFilteringTest::testSingleMatchFilteringWithPath
    public void testSingleMatchFilteringWithPath() throws Exception
    {
        StringWriter w = new StringWriter();
        JsonGenerator origGen = JSON_F.createGenerator(w);
        NameMatchFilter filter = new NameMatchFilter("value");
        FilteringGeneratorDelegate gen = new FilteringGeneratorDelegate(origGen,
                filter,
                true, 
                false 
                );

        
        assertSame(w, gen.getOutputTarget());
        assertNotNull(gen.getFilterContext());
        assertSame(filter, gen.getFilter());

        final String JSON = "{'a':123,'array':[1,2],'ob':{'value0':2,'value':3,'value2':4},'b':true}";
        writeJsonDoc(JSON_F, JSON, gen);
        assertEquals(aposToQuotes("{'ob':{'value':3}}"), w.toString());

        assertEquals(1, gen.getMatchCount());
    }

// com.fasterxml.jackson.core.filter.BasicGeneratorFilteringTest::testSingleMatchFilteringWithPathSkippedArray
    public void testSingleMatchFilteringWithPathSkippedArray() throws Exception
    {
        StringWriter w = new StringWriter();
        JsonGenerator origGen = JSON_F.createGenerator(w);
        NameMatchFilter filter = new NameMatchFilter("value");
        FilteringGeneratorDelegate gen = new FilteringGeneratorDelegate(origGen,
                filter,
                true, 
                false 
                );

        
        assertSame(w, gen.getOutputTarget());
        assertNotNull(gen.getFilterContext());
        assertSame(filter, gen.getFilter());

        final String JSON = "{'array':[1,[2,3]],'ob':[{'value':'bar'}],'b':{'foo':[1,'foo']}}";
        writeJsonDoc(JSON_F, JSON, gen);
        assertEquals(aposToQuotes("{'ob':[{'value':'bar'}]}"), w.toString());
        assertEquals(1, gen.getMatchCount());
    }

// com.fasterxml.jackson.core.filter.BasicGeneratorFilteringTest::testSingleMatchFilteringWithPathAlternate1
    public void testSingleMatchFilteringWithPathAlternate1() throws Exception
    {
        StringWriter w = new StringWriter();
        FilteringGeneratorDelegate gen = new FilteringGeneratorDelegate(JSON_F.createGenerator(w),
                new NameMatchFilter("value"),
                true, 
                false 
                );
        

        gen.writeStartObject();
        gen.writeFieldName(new SerializedString("a"));
        gen.writeNumber(123);

        gen.writeFieldName("array");
        gen.writeStartArray(2);
        gen.writeNumber("1");
        gen.writeNumber((short) 2);
        gen.writeEndArray();

        gen.writeFieldName(new SerializedString("ob"));
        gen.writeStartObject();
        gen.writeNumberField("value0", 2);
        gen.writeFieldName(new SerializedString("value"));
        gen.writeStartArray(1);
        gen.writeString(new SerializedString("x")); 
        gen.writeEndArray();
        gen.writeStringField("value2", "foo");

        gen.writeEndObject();

        gen.writeBooleanField("b", true);
        
        gen.writeEndObject();
        gen.close();

        assertEquals(aposToQuotes("{'ob':{'value':['x']}}"), w.toString());
        assertEquals(1, gen.getMatchCount());
    }

// com.fasterxml.jackson.core.filter.BasicGeneratorFilteringTest::testSingleMatchFilteringWithPathRawBinary
    public void testSingleMatchFilteringWithPathRawBinary() throws Exception
    {
        StringWriter w = new StringWriter();
        FilteringGeneratorDelegate gen = new FilteringGeneratorDelegate(JSON_F.createGenerator(w),
                new NameMatchFilter("array"),
                true, 
                false 
                );
        

        gen.writeStartObject();

        gen.writeFieldName("header");
        gen.writeStartArray();
        gen.writeBinary(new byte[] { 1 });
        gen.writeRawValue(new SerializedString("1"));
        gen.writeRawValue("2");
        gen.writeEndArray();
        
        gen.writeFieldName("array");

        gen.writeStartArray();
        gen.writeBinary(new byte[] { 1 });
        gen.writeNumber((short) 1);
        gen.writeNumber((int) 2);
        gen.writeNumber((long) 3);
        gen.writeNumber(BigInteger.valueOf(4));
        gen.writeRaw(" ");
        gen.writeNumber(new BigDecimal("5.0"));
        gen.writeRaw(new SerializedString(" "));
        gen.writeNumber(6.25f);
        gen.writeNumber(7.5);
        gen.writeEndArray();

        gen.writeArrayFieldStart("extra");
        gen.writeNumber((short) 1);
        gen.writeNumber((int) 2);
        gen.writeNumber((long) 3);
        gen.writeNumber(BigInteger.valueOf(4));
        gen.writeNumber(new BigDecimal("5.0"));
        gen.writeNumber(6.25f);
        gen.writeNumber(7.5);
        gen.writeEndArray();
        
        gen.writeEndObject();
        gen.close();

        assertEquals(aposToQuotes("{'array':['AQ==',1,2,3,4 ,5.0 ,6.25,7.5]}"), w.toString());
        assertEquals(1, gen.getMatchCount());
    }

// com.fasterxml.jackson.core.filter.BasicGeneratorFilteringTest::testMultipleMatchFilteringWithPath1
    public void testMultipleMatchFilteringWithPath1() throws Exception
    {
        StringWriter w = new StringWriter();
        FilteringGeneratorDelegate gen = new FilteringGeneratorDelegate(JSON_F.createGenerator(w),
                new NameMatchFilter("value0", "value2"),
                true,  true  );
        final String JSON = "{'a':123,'array':[1,2],'ob':{'value0':2,'value':3,'value2':4},'b':true}";
        writeJsonDoc(JSON_F, JSON, gen);
        assertEquals(aposToQuotes("{'ob':{'value0':2,'value2':4}}"), w.toString());
        assertEquals(2, gen.getMatchCount());
    }

// com.fasterxml.jackson.core.filter.BasicGeneratorFilteringTest::testMultipleMatchFilteringWithPath2
    public void testMultipleMatchFilteringWithPath2() throws Exception
    {
        StringWriter w = new StringWriter();

        FilteringGeneratorDelegate gen = new FilteringGeneratorDelegate(JSON_F.createGenerator(w),
                new NameMatchFilter("array", "b", "value"),
                true, true);
        final String JSON = "{'a':123,'array':[1,2],'ob':{'value0':2,'value':3,'value2':4},'b':true}";
        writeJsonDoc(JSON_F, JSON, gen);
        assertEquals(aposToQuotes("{'array':[1,2],'ob':{'value':3},'b':true}"), w.toString());
        assertEquals(3, gen.getMatchCount());
    }

// com.fasterxml.jackson.core.filter.BasicGeneratorFilteringTest::testMultipleMatchFilteringWithPath3
    public void testMultipleMatchFilteringWithPath3() throws Exception
    {
        StringWriter w = new StringWriter();

        FilteringGeneratorDelegate gen = new FilteringGeneratorDelegate(JSON_F.createGenerator(w),
                new NameMatchFilter("value"),
                true, true);
        final String JSON = "{'root':{'a0':true,'a':{'value':3},'b':{'value':4}},'b0':false}";
        writeJsonDoc(JSON_F, JSON, gen);
        assertEquals(aposToQuotes("{'root':{'a':{'value':3},'b':{'value':4}}}"), w.toString());
        assertEquals(2, gen.getMatchCount());
    }

// com.fasterxml.jackson.core.filter.BasicGeneratorFilteringTest::testIndexMatchWithPath1
    public void testIndexMatchWithPath1() throws Exception
    {
        StringWriter w = new StringWriter();
        FilteringGeneratorDelegate gen = new FilteringGeneratorDelegate(JSON_F.createGenerator(w),
                new IndexMatchFilter(1),
                true, true);
        final String JSON = "{'a':123,'array':[1,2],'ob':{'value0':2,'value':3,'value2':4},'b':true}";
        writeJsonDoc(JSON_F, JSON, gen);
        assertEquals(aposToQuotes("{'array':[2]}"), w.toString());

        w = new StringWriter();
        gen = new FilteringGeneratorDelegate(JSON_F.createGenerator(w),
                new IndexMatchFilter(0),
                true, true);
        writeJsonDoc(JSON_F, JSON, gen);
        assertEquals(aposToQuotes("{'array':[1]}"), w.toString());
        assertEquals(1, gen.getMatchCount());
    }

// com.fasterxml.jackson.core.filter.BasicGeneratorFilteringTest::testIndexMatchWithPath2
    public void testIndexMatchWithPath2() throws Exception
    {
        StringWriter w = new StringWriter();
        FilteringGeneratorDelegate gen = new FilteringGeneratorDelegate(JSON_F.createGenerator(w),
                new IndexMatchFilter(0,1),
                true, true);
        final String JSON = "{'a':123,'array':[1,2],'ob':{'value0':2,'value':3,'value2':4},'b':true}";
        writeJsonDoc(JSON_F, JSON, gen);
        assertEquals(aposToQuotes("{'array':[1,2]}"), w.toString());
        assertEquals(2, gen.getMatchCount());
    }

// com.fasterxml.jackson.core.filter.BasicGeneratorFilteringTest::testWriteStartObjectWithObject
    public void testWriteStartObjectWithObject() throws Exception
    {
        StringWriter w = new StringWriter();

        FilteringGeneratorDelegate gen = new FilteringGeneratorDelegate(JSON_F.createGenerator(w),
                TokenFilter.INCLUDE_ALL,
                true, true);

        String value = "val";

        gen.writeStartObject(new Object());
        gen.writeFieldName("field1");
        {
            gen.writeStartObject(value);
            gen.writeEndObject();
        }

        gen.writeFieldName("field2");
        gen.writeString("val2");

        gen.writeEndObject();
        gen.close();
        assertEquals(aposToQuotes("{'field1':{},'field2':'val2'}"), w.toString());
    }

// com.fasterxml.jackson.core.filter.BasicParserFilteringTest::testNonFiltering
    public void testNonFiltering() throws Exception
    {
        JsonParser p = JSON_F.createParser(SIMPLE);
        String result = readAndWrite(JSON_F, p);
        assertEquals(SIMPLE, result);
    }

// com.fasterxml.jackson.core.filter.BasicParserFilteringTest::testSingleMatchFilteringWithoutPath
    public void testSingleMatchFilteringWithoutPath() throws Exception
    {
        JsonParser p0 = JSON_F.createParser(SIMPLE);
        FilteringParserDelegate p = new FilteringParserDelegate(p0,
               new NameMatchFilter("value"),
                   false, 
                   false 
                );
        String result = readAndWrite(JSON_F, p);
        assertEquals(aposToQuotes("3"), result);
        assertEquals(1, p.getMatchCount());
    }

// com.fasterxml.jackson.core.filter.BasicParserFilteringTest::testSingleMatchFilteringWithPath1
    public void testSingleMatchFilteringWithPath1() throws Exception
    {
        String jsonString = aposToQuotes("{'a':123,'array':[1,2],'ob':{'value0':2,'value':3,'value2':4},'b':true}");
        JsonParser p0 = JSON_F.createParser(jsonString);
        FilteringParserDelegate p = new FilteringParserDelegate(p0,
                new NameMatchFilter("a"),
                true, 
                false 
        );
        String result = readAndWrite(JSON_F, p);
        assertEquals(aposToQuotes("{'a':123}"), result);
        assertEquals(1, p.getMatchCount());
    }

// com.fasterxml.jackson.core.filter.BasicParserFilteringTest::testSingleMatchFilteringWithPath2
    public void testSingleMatchFilteringWithPath2() throws Exception
    {
        String jsonString = aposToQuotes("{'a':123,'array':[1,2],'ob':{'value0':2,'value':3,'value2':4},'b':true}");
        JsonParser p0 = JSON_F.createParser(jsonString);
        FilteringParserDelegate p = new FilteringParserDelegate(p0,
                new NameMatchFilter("value"),
                true, 
                false 
        );
        String result = readAndWrite(JSON_F, p);
        assertEquals(aposToQuotes("{\"ob\":{\"value\":3}}"), result);
        assertEquals(1, p.getMatchCount());
    }

// com.fasterxml.jackson.core.filter.BasicParserFilteringTest::testSingleMatchFilteringWithPath3
    public void testSingleMatchFilteringWithPath3() throws Exception
    {
        String jsonString = aposToQuotes("{'a':123,'ob':{'value0':2,'value':3,'value2':4},'array':[1,2],'b':true}");
        JsonParser p0 = JSON_F.createParser(jsonString);
        FilteringParserDelegate p = new FilteringParserDelegate(p0,
                new NameMatchFilter("ob"),
                true, 
                false 
        );
        String result = readAndWrite(JSON_F, p);
        assertEquals(aposToQuotes("{'ob':{'value0':2,'value':3,'value2':4}}"), result);
        assertEquals(1, p.getMatchCount());
    }

// com.fasterxml.jackson.core.filter.BasicParserFilteringTest::testNotAllowMultipleMatchesWithoutPath1
    public void testNotAllowMultipleMatchesWithoutPath1() throws Exception
    {
        String jsonString = aposToQuotes("{'a':123,'array':[1,2],'ob':{'value0':2,'value':3,'value2':4,'value':{'value0':2}},'b':true}");
        JsonParser p0 = JSON_F.createParser(jsonString);
        FilteringParserDelegate p = new FilteringParserDelegate(p0,
               new NameMatchFilter("value"),
                   false, 
                   false 
                );
        String result = readAndWrite(JSON_F, p);
        assertEquals(aposToQuotes("3"), result);
        assertEquals(1, p.getMatchCount());
    }

// com.fasterxml.jackson.core.filter.BasicParserFilteringTest::testNotAllowMultipleMatchesWithoutPath2
    public void testNotAllowMultipleMatchesWithoutPath2() throws Exception
    {
        String jsonString = aposToQuotes("{'a':123,'array':[1,2],'array':[3,4],'ob':{'value0':2,'value':3,'value2':4,'value':{'value0':2}},'value':\"val\",'b':true}");
        JsonParser p0 = JSON_F.createParser(jsonString);
        FilteringParserDelegate p = new FilteringParserDelegate(p0,
                new IndexMatchFilter(1),
                false, 
                false 
        );
        String result = readAndWrite(JSON_F, p);
        assertEquals(aposToQuotes("2"), result);
        assertEquals(1, p.getMatchCount());
    }

// com.fasterxml.jackson.core.filter.BasicParserFilteringTest::testNotAllowMultipleMatchesWithPath1
    public void testNotAllowMultipleMatchesWithPath1() throws Exception
    {
        String jsonString = aposToQuotes("{'a':123,'array':[1,2],'array':[3,4],'ob':{'value':3,'array':[5,6],'value':{'value0':2}},'value':\"val\",'b':true}");
        JsonParser p0 = JSON_F.createParser(jsonString);
        FilteringParserDelegate p = new FilteringParserDelegate(p0,
                new IndexMatchFilter(1),
                true, 
                false 
        );
        String result = readAndWrite(JSON_F, p);
        assertEquals(aposToQuotes("{\"array\":[2]}"), result);
        assertEquals(1, p.getMatchCount());
    }

// com.fasterxml.jackson.core.filter.BasicParserFilteringTest::testNotAllowMultipleMatchesWithPath2
    public void testNotAllowMultipleMatchesWithPath2() throws Exception
    {
        String jsonString = aposToQuotes("{'a':123,'ob':{'value':3,'array':[1,2],'value':{'value0':2}},'array':[3,4]}");
        JsonParser p0 = JSON_F.createParser(jsonString);
        FilteringParserDelegate p = new FilteringParserDelegate(p0,
                new IndexMatchFilter(1),
                true, 
                false 
        );
        String result = readAndWrite(JSON_F, p);
        assertEquals(aposToQuotes("{\"ob\":{\"array\":[2]}}"), result);
        assertEquals(1, p.getMatchCount());
    }

// com.fasterxml.jackson.core.filter.BasicParserFilteringTest::testNotAllowMultipleMatchesWithPath3
    public void testNotAllowMultipleMatchesWithPath3() throws Exception
    {
        String jsonString = aposToQuotes("{'ob':{'value':3,'ob':{'value':2}},'value':\"val\"}");
        JsonParser p0 = JSON_F.createParser(jsonString);
        FilteringParserDelegate p = new FilteringParserDelegate(p0,
                new NameMatchFilter("value"),
                true, 
                false 
        );
        String result = readAndWrite(JSON_F, p);
        assertEquals(aposToQuotes("{'ob':{'value':3}}"), result);
        assertEquals(1, p.getMatchCount());
    }

// com.fasterxml.jackson.core.filter.BasicParserFilteringTest::testNotAllowMultipleMatchesWithPath4
    public void testNotAllowMultipleMatchesWithPath4() throws Exception
    {
        String jsonString = aposToQuotes("{'a':123,'array':[1,2],'ob':{'value1':1},'ob2':{'ob':{'value2':2}},'value':\"val\",'b':true}");
        JsonParser p0 = JSON_F.createParser(jsonString);
        FilteringParserDelegate p = new FilteringParserDelegate(p0,
                new NameMatchFilter("ob"),
                true, 
                false 
        );
        String result = readAndWrite(JSON_F, p);
        assertEquals(aposToQuotes("{'ob':{'value1':1}}"), result);
        assertEquals(1, p.getMatchCount());
    }

// com.fasterxml.jackson.core.filter.BasicParserFilteringTest::testAllowMultipleMatchesWithoutPath
    public void testAllowMultipleMatchesWithoutPath() throws Exception
    {
        String jsonString = aposToQuotes("{'a':123,'array':[1,2],'ob':{'value0':2,'value':3,'value2':4,'value':{'value0':2}},'value':\"val\",'b':true}");
        JsonParser p0 = JSON_F.createParser(jsonString);
        FilteringParserDelegate p = new FilteringParserDelegate(p0,
               new NameMatchFilter("value"),
                   false, 
                   true 
                );
        String result = readAndWrite(JSON_F, p);
        assertEquals(aposToQuotes("3 {\"value0\":2} \"val\""), result);
        assertEquals(3, p.getMatchCount());
    }

// com.fasterxml.jackson.core.filter.BasicParserFilteringTest::testAllowMultipleMatchesWithPath1
    public void testAllowMultipleMatchesWithPath1() throws Exception
    {
        String jsonString = aposToQuotes("{'a':123,'array':[1,2],'ob':{'value0':2,'value':3,'value2':4,'value':{'value0':2}},'value':\"val\",'b':true}");
        JsonParser p0 = JSON_F.createParser(jsonString);
        FilteringParserDelegate p = new FilteringParserDelegate(p0,
                new NameMatchFilter("value"),
                true, 
                true 
        );
        String result = readAndWrite(JSON_F, p);
        assertEquals(aposToQuotes("{\"ob\":{\"value\":3,\"value\":{\"value0\":2}},\"value\":\"val\"}"), result);
        assertEquals(3, p.getMatchCount());
    }

// com.fasterxml.jackson.core.filter.BasicParserFilteringTest::testAllowMultipleMatchesWithPath2
    public void testAllowMultipleMatchesWithPath2() throws Exception
    {
        String jsonString = aposToQuotes("{'a':123,'array':[1,2],'ob':{'value0':2,'value':3,'array':[3,4],'value':{'value0':2}},'value':\"val\",'b':true}");
        JsonParser p0 = JSON_F.createParser(jsonString);
        FilteringParserDelegate p = new FilteringParserDelegate(p0,
                new IndexMatchFilter(1),
                true, 
                true 
        );
        String result = readAndWrite(JSON_F, p);
        assertEquals(aposToQuotes("{\"array\":[2],\"ob\":{\"array\":[4]}}"), result);
        assertEquals(2, p.getMatchCount());
    }

// com.fasterxml.jackson.core.filter.BasicParserFilteringTest::testMultipleMatchFilteringWithPath1
    public void testMultipleMatchFilteringWithPath1() throws Exception
    {
        JsonParser p0 = JSON_F.createParser(SIMPLE);
        FilteringParserDelegate p = new FilteringParserDelegate(p0,
                new NameMatchFilter("value0", "value2"),
                true,  true  );
        String result = readAndWrite(JSON_F, p);
        assertEquals(aposToQuotes("{'ob':{'value0':2,'value2':4}}"), result);
        assertEquals(2, p.getMatchCount());

    }

// com.fasterxml.jackson.core.filter.BasicParserFilteringTest::testMultipleMatchFilteringWithPath2
    public void testMultipleMatchFilteringWithPath2() throws Exception
    {
        String INPUT = aposToQuotes("{'a':123,'ob':{'value0':2,'value':3,'value2':4},'b':true}");
        JsonParser p0 = JSON_F.createParser(INPUT);
        FilteringParserDelegate p = new FilteringParserDelegate(p0,
                new NameMatchFilter("b", "value"),
                true, true);

        String result = readAndWrite(JSON_F, p);
        assertEquals(aposToQuotes("{'ob':{'value':3},'b':true}"), result);
        assertEquals(2, p.getMatchCount());
    }

// com.fasterxml.jackson.core.filter.BasicParserFilteringTest::testMultipleMatchFilteringWithPath3
    public void testMultipleMatchFilteringWithPath3() throws Exception
    {
        final String JSON = aposToQuotes("{'root':{'a0':true,'a':{'value':3},'b':{'value':\"foo\"}},'b0':false}");
        JsonParser p0 = JSON_F.createParser(JSON);
        FilteringParserDelegate p = new FilteringParserDelegate(p0,
                new NameMatchFilter("value"),
                true, true);
        String result = readAndWrite(JSON_F, p);
        assertEquals(aposToQuotes("{'root':{'a':{'value':3},'b':{'value':\"foo\"}}}"), result);
        assertEquals(2, p.getMatchCount());
    }

// com.fasterxml.jackson.core.filter.BasicParserFilteringTest::testIndexMatchWithPath1
    public void testIndexMatchWithPath1() throws Exception
    {
        FilteringParserDelegate p = new FilteringParserDelegate(JSON_F.createParser(SIMPLE),
                new IndexMatchFilter(1), true, true);
        String result = readAndWrite(JSON_F, p);
        assertEquals(aposToQuotes("{'array':[2]}"), result);
        assertEquals(1, p.getMatchCount());

        p = new FilteringParserDelegate(JSON_F.createParser(SIMPLE),
                new IndexMatchFilter(0), true, true);
        result = readAndWrite(JSON_F, p);
        assertEquals(aposToQuotes("{'array':[1]}"), result);
        assertEquals(1, p.getMatchCount());
    }

// com.fasterxml.jackson.core.filter.BasicParserFilteringTest::testIndexMatchWithPath2
    public void testIndexMatchWithPath2() throws Exception
    {
        FilteringParserDelegate p = new FilteringParserDelegate(JSON_F.createParser(SIMPLE),
                new IndexMatchFilter(0, 1), true, true);
        assertEquals(aposToQuotes("{'array':[1,2]}"), readAndWrite(JSON_F, p));
        assertEquals(2, p.getMatchCount());
    
        String JSON = aposToQuotes("{'a':123,'array':[1,2,3,4,5],'b':[1,2,3]}");
        p = new FilteringParserDelegate(JSON_F.createParser(JSON),
                new IndexMatchFilter(1, 3), true, true);
        assertEquals(aposToQuotes("{'array':[2,4],'b':[2]}"), readAndWrite(JSON_F, p));
        assertEquals(3, p.getMatchCount());
    }

// com.fasterxml.jackson.core.filter.BasicParserFilteringTest::testBasicSingleMatchFilteringWithPath
    public void testBasicSingleMatchFilteringWithPath() throws Exception
    {
        JsonParser p0 = JSON_F.createParser(SIMPLE);
        JsonParser p = new FilteringParserDelegate(p0,
                new NameMatchFilter("value"),
                true, 
                false 
        );

        String result = readAndWrite(JSON_F, p);
        assertEquals(aposToQuotes("{'ob':{'value':3}}"), result);
    }

// com.fasterxml.jackson.core.filter.BasicParserFilteringTest::testTokensSingleMatchWithPath
    public void testTokensSingleMatchWithPath() throws Exception
    {
        JsonParser p0 = JSON_F.createParser(SIMPLE);
        JsonParser p = new FilteringParserDelegate(p0,
                new NameMatchFilter("value"),
                true, 
                false 
        );

        assertFalse(p.hasCurrentToken());
        assertNull(p.getCurrentToken());
        assertEquals(JsonTokenId.ID_NO_TOKEN, p.getCurrentTokenId());
        assertFalse(p.isExpectedStartObjectToken());
        assertFalse(p.isExpectedStartArrayToken());

        assertToken(JsonToken.START_OBJECT, p.nextToken());
        assertEquals(JsonToken.START_OBJECT, p.getCurrentToken());
        assertTrue(p.isExpectedStartObjectToken());
        assertFalse(p.isExpectedStartArrayToken());

        assertToken(JsonToken.FIELD_NAME, p.nextToken());
        assertEquals(JsonToken.FIELD_NAME, p.getCurrentToken());
        assertEquals("ob", p.getCurrentName());

        assertToken(JsonToken.START_OBJECT, p.nextToken());
        assertEquals("ob", p.getCurrentName());

        assertToken(JsonToken.FIELD_NAME, p.nextToken());
        assertEquals("value", p.getCurrentName());
        assertEquals("value", p.getText());

        assertToken(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertEquals(JsonToken.VALUE_NUMBER_INT, p.getCurrentToken());
        assertEquals(JsonParser.NumberType.INT, p.getNumberType());
        assertEquals(3, p.getIntValue());
        assertEquals("value", p.getCurrentName());

        assertToken(JsonToken.END_OBJECT, p.nextToken());
        assertEquals(JsonToken.END_OBJECT, p.getCurrentToken());

        assertToken(JsonToken.END_OBJECT, p.nextToken());
        assertEquals(JsonToken.END_OBJECT, p.getCurrentToken());

        p.clearCurrentToken();
        assertNull(p.getCurrentToken());

        p.close();
    }

// com.fasterxml.jackson.core.filter.BasicParserFilteringTest::testSkippingForSingleWithPath
    public void testSkippingForSingleWithPath() throws Exception
    {
        JsonParser p0 = JSON_F.createParser(SIMPLE);
        JsonParser p = new FilteringParserDelegate(p0,
                new NameMatchFilter("value"),
                true, 
                false 
        );

        assertToken(JsonToken.START_OBJECT, p.nextToken());
        p.skipChildren();
        assertEquals(JsonToken.END_OBJECT, p.getCurrentToken());
        assertNull(p.nextToken());
    }

// com.fasterxml.jackson.core.filter.JsonPointerGeneratorFilteringTest::testSimplePropertyWithPath
    public void testSimplePropertyWithPath() throws Exception
    {
        _assert(SIMPLE_INPUT, "/c", true, "{'c':{'d':{'a':true}}}");
        _assert(SIMPLE_INPUT, "/c/d", true, "{'c':{'d':{'a':true}}}");
        _assert(SIMPLE_INPUT, "/c/d/a", true, "{'c':{'d':{'a':true}}}");

        _assert(SIMPLE_INPUT, "/c/d/a", true, "{'c':{'d':{'a':true}}}");
        
        _assert(SIMPLE_INPUT, "/a", true, "{'a':1}");
        _assert(SIMPLE_INPUT, "/d", true, "{'d':null}");

        
        _assert(SIMPLE_INPUT, "/x", true, "");
    }

// com.fasterxml.jackson.core.filter.JsonPointerGeneratorFilteringTest::testSimplePropertyWithoutPath
    public void testSimplePropertyWithoutPath() throws Exception
    {
        _assert(SIMPLE_INPUT, "/c", false, "{'d':{'a':true}}");
        _assert(SIMPLE_INPUT, "/c/d", false, "{'a':true}");
        _assert(SIMPLE_INPUT, "/c/d/a", false, "true");
        
        _assert(SIMPLE_INPUT, "/a", false, "1");
        _assert(SIMPLE_INPUT, "/d", false, "null");

        
        _assert(SIMPLE_INPUT, "/x", false, "");
    }

// com.fasterxml.jackson.core.filter.JsonPointerGeneratorFilteringTest::testArrayElementWithPath
    public void testArrayElementWithPath() throws Exception
    {
        _assert(SIMPLE_INPUT, "/b", true, "{'b':[1,2,3]}");
        _assert(SIMPLE_INPUT, "/b/1", true, "{'b':[2]}");
        _assert(SIMPLE_INPUT, "/b/2", true, "{'b':[3]}");
        
        
        _assert(SIMPLE_INPUT, "/b/8", true, "");
    }

// com.fasterxml.jackson.core.filter.JsonPointerGeneratorFilteringTest::testArrayNestedWithPath
    public void testArrayNestedWithPath() throws Exception
    {
        _assert("{'a':[true,{'b':3,'d':2},false]}", "/a/1/b", true, "{'a':[{'b':3}]}");
        _assert("[true,[1]]", "/0", true, "[true]");
        _assert("[true,[1]]", "/1", true, "[[1]]");
        _assert("[true,[1,2,[true],3],0]", "/0", true, "[true]");
        _assert("[true,[1,2,[true],3],0]", "/1", true, "[[1,2,[true],3]]");

        _assert("[true,[1,2,[true],3],0]", "/1/2", true, "[[[true]]]");
        _assert("[true,[1,2,[true],3],0]", "/1/2/0", true, "[[[true]]]");
        _assert("[true,[1,2,[true],3],0]", "/1/3/0", true, "");
    }

// com.fasterxml.jackson.core.filter.JsonPointerGeneratorFilteringTest::testArrayNestedWithoutPath
    public void testArrayNestedWithoutPath() throws Exception
    {
        _assert("{'a':[true,{'b':3,'d':2},false]}", "/a/1/b", false, "3");
        _assert("[true,[1,2,[true],3],0]", "/0", false, "true");
        _assert("[true,[1,2,[true],3],0]", "/1", false,
                "[1,2,[true],3]");

        _assert("[true,[1,2,[true],3],0]", "/1/2", false, "[true]");
        _assert("[true,[1,2,[true],3],0]", "/1/2/0", false, "true");
        _assert("[true,[1,2,[true],3],0]", "/1/3/0", false, "");
    }

// com.fasterxml.jackson.core.filter.JsonPointerGeneratorFilteringTest::testArrayElementWithoutPath
    public void testArrayElementWithoutPath() throws Exception
    {
        _assert(SIMPLE_INPUT, "/b", false, "[1,2,3]");
        _assert(SIMPLE_INPUT, "/b/1", false, "2");
        _assert(SIMPLE_INPUT, "/b/2", false, "3");

        _assert(SIMPLE_INPUT, "/b/8", false, "");

        
        _assert(SIMPLE_INPUT, "/x", false, "");
    }

// com.fasterxml.jackson.core.filter.JsonPointerParserFilteringTest::testSimplestWithPath
    public void testSimplestWithPath() throws Exception
    {
        _assert(SIMPLEST_INPUT, "/a", true, "{'a':1}");
        _assert(SIMPLEST_INPUT, "/b", true, "{'b':2}");
        _assert(SIMPLEST_INPUT, "/c", true, "{'c':3}");
        _assert(SIMPLEST_INPUT, "/c/0", true, "");
        _assert(SIMPLEST_INPUT, "/d", true, "");
    }

// com.fasterxml.jackson.core.filter.JsonPointerParserFilteringTest::testSimplestNoPath
    public void testSimplestNoPath() throws Exception
    {
        _assert(SIMPLEST_INPUT, "/a", false, "1");
        _assert(SIMPLEST_INPUT, "/b", false, "2");
        _assert(SIMPLEST_INPUT, "/b/2", false, "");
        _assert(SIMPLEST_INPUT, "/c", false, "3");
        _assert(SIMPLEST_INPUT, "/d", false, "");
    }

// com.fasterxml.jackson.core.filter.JsonPointerParserFilteringTest::testSimpleWithPath
    public void testSimpleWithPath() throws Exception
    {
        _assert(SIMPLE_INPUT, "/c", true, "{'c':{'d':{'a':true}}}");
        _assert(SIMPLE_INPUT, "/c/d", true, "{'c':{'d':{'a':true}}}");
        _assert(SIMPLE_INPUT, "/a", true, "{'a':1}");
        _assert(SIMPLE_INPUT, "/b", true, "{'b':[1,2,3]}");
        _assert(SIMPLE_INPUT, "/b/0", true, "{'b':[1]}");
        _assert(SIMPLE_INPUT, "/b/1", true, "{'b':[2]}");
        _assert(SIMPLE_INPUT, "/b/2", true, "{'b':[3]}");
        _assert(SIMPLE_INPUT, "/b/3", true, "");
    }

// com.fasterxml.jackson.core.filter.JsonPointerParserFilteringTest::testSimpleNoPath
    public void testSimpleNoPath() throws Exception
    {
        _assert(SIMPLE_INPUT, "/c", false, "{'d':{'a':true}}");

        _assert(SIMPLE_INPUT, "/c/d", false, "{'a':true}");
        _assert(SIMPLE_INPUT, "/a", false, "1");
        _assert(SIMPLE_INPUT, "/b", false, "[1,2,3]");
        _assert(SIMPLE_INPUT, "/b/0", false, "1");
        _assert(SIMPLE_INPUT, "/b/1", false, "2");
        _assert(SIMPLE_INPUT, "/b/2", false, "3");
        _assert(SIMPLE_INPUT, "/b/3", false, "");
    }

// com.fasterxml.jackson.core.format.DataFormatMatcherTest::testGetDataStream
  public void testGetDataStream() throws IOException {
    byte[] byteArray = new byte[2];
    MatchStrength matchStrength = MatchStrength.WEAK_MATCH;
    DataFormatMatcher dataFormatMatcher = new DataFormatMatcher(null,
            byteArray,
            1,
            0,
            null,
            matchStrength);
    InputStream inputStream = dataFormatMatcher.getDataStream();
    assertEquals(0, inputStream.available());
    inputStream.close();
  }

// com.fasterxml.jackson.core.format.DataFormatMatcherTest::testCreatesDataFormatMatcherTwo
  public void testCreatesDataFormatMatcherTwo() throws IOException {
    JsonFactory jsonFactory = new JsonFactory();
    try {
        @SuppressWarnings("unused")
        DataFormatMatcher dataFormatMatcher = new DataFormatMatcher(null,
                new byte[0], 2, 1,
                jsonFactory, MatchStrength.NO_MATCH);
    } catch (IllegalArgumentException e) {
        verifyException(e, "Illegal start/length");
    }
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

// com.fasterxml.jackson.core.io.SegmentedStringWriterTest::testSimple
    public void testSimple() throws Exception
    {
        BufferRecycler br = new BufferRecycler();
        SegmentedStringWriter w = new SegmentedStringWriter(br);

        StringBuilder exp = new StringBuilder();

        for (int i = 0; exp.length() < 100; ++i) {
            String nr = String.valueOf(i);
            exp.append(' ').append(nr);
            w.append(' ');
            switch (i % 4) {
            case 0:
                w.append(nr);
                break;
            case 1:
                {
                    String str = "  "+nr;
                    w.append(str, 2, str.length());
                }
                break;
            case 2:
                w.write(nr.toCharArray());
                break;
            default:
                {
                    char[] ch = (" "+nr+" ").toCharArray();
                    w.write(ch, 1, nr.length());
                }
                break;
            }
        }
        
        w.flush();
        w.close();

        String act = w.getAndClear();
        assertEquals(exp.toString(), act);
    }

// com.fasterxml.jackson.core.io.TestCharTypes::testAppendQuoted0_31
    public void testAppendQuoted0_31 ()
    {
        final String[] inputs =    { "\u0000",  "\u001F",  "abcd", "\u0001ABCD\u0002",   "WX\u000F\u0010YZ"   };
        final String[] expecteds = { "\\u0000", "\\u001F", "abcd", "\\u0001ABCD\\u0002", "WX\\u000F\\u0010YZ" };
        assert inputs.length == expecteds.length;

        for (int i = 0; i < inputs.length; i++) {
            final String input = inputs[i];
            final String expected = expecteds[i];

            final StringBuilder sb = new StringBuilder();
            CharTypes.appendQuoted(sb, input);
            final String actual = sb.toString();

            assertEquals(expected, actual);
        }
    }

// com.fasterxml.jackson.core.io.TestIOContext::testAllocations
    public void testAllocations() throws Exception
    {
        IOContext ctxt = new IOContext(new BufferRecycler(), "N/A", true);

        

        
        assertNotNull(ctxt.allocReadIOBuffer());
        
        try {
            ctxt.allocReadIOBuffer();
        } catch (IllegalStateException e) {
            verifyException(e, "second time");
        }
        
        try {
            ctxt.releaseReadIOBuffer(new byte[1]);
        } catch (IllegalArgumentException e) {
            verifyException(e, "smaller than original");
        }
        
        ctxt.releaseReadIOBuffer(null);

        

        assertNotNull(ctxt.allocWriteEncodingBuffer());
        try {
            ctxt.allocWriteEncodingBuffer();
        } catch (IllegalStateException e) {
            verifyException(e, "second time");
        }
        try {
            ctxt.releaseWriteEncodingBuffer(new byte[1]);
        } catch (IllegalArgumentException e) {
            verifyException(e, "smaller than original");
        }
        ctxt.releaseWriteEncodingBuffer(null);

        

        assertNotNull(ctxt.allocTokenBuffer());
        try {
            ctxt.allocTokenBuffer();
        } catch (IllegalStateException e) {
            verifyException(e, "second time");
        }
        try {
            ctxt.releaseTokenBuffer(new char[1]);
        } catch (IllegalArgumentException e) {
            verifyException(e, "smaller than original");
        }
        ctxt.releaseTokenBuffer(null);

        

        assertNotNull(ctxt.allocConcatBuffer());
        try {
            ctxt.allocConcatBuffer();
        } catch (IllegalStateException e) {
            verifyException(e, "second time");
        }
        try {
            ctxt.releaseConcatBuffer(new char[1]);
        } catch (IllegalArgumentException e) {
            verifyException(e, "smaller than original");
        }
        ctxt.releaseConcatBuffer(null);

        

        assertNotNull(ctxt.allocNameCopyBuffer(100));
        try {
            ctxt.allocNameCopyBuffer(100);
        } catch (IllegalStateException e) {
            verifyException(e, "second time");
        }
        try {
            ctxt.releaseNameCopyBuffer(new char[1]);
        } catch (IllegalArgumentException e) {
            verifyException(e, "smaller than original");
        }
        ctxt.releaseNameCopyBuffer(null);
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

// com.fasterxml.jackson.core.io.TestJsonStringEncoder::testQuoteCharSequenceAsString
    public void testQuoteCharSequenceAsString() throws Exception
    {
        StringBuilder output = new StringBuilder();
        StringBuilder builder = new StringBuilder();
        builder.append("foobar");
        BufferRecyclers.quoteAsJsonText(builder, output);
        assertEquals("foobar", output.toString());
        builder.setLength(0);
        output.setLength(0);
        builder.append("\"x\"");
        BufferRecyclers.quoteAsJsonText(builder, output);
        assertEquals("\\\"x\\\"", output.toString());
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

// com.fasterxml.jackson.core.io.TestJsonStringEncoder::testQuoteLongCharSequenceAsString
    public void testQuoteLongCharSequenceAsString() throws Exception
    {
        StringBuilder output = new StringBuilder();
        StringBuilder input = new StringBuilder();
        StringBuilder sb2 = new StringBuilder();
        for (int i = 0; i < 1111; ++i) {
            input.append('"');
            sb2.append("\\\"");
        }
        String exp = sb2.toString();
        BufferRecyclers.quoteAsJsonText(input, output);
        assertEquals(2*input.length(), output.length());
        assertEquals(exp, output.toString());

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
        char[] quoted = BufferRecyclers.quoteAsJsonText(new String(input));
        assertEquals("\\u0000\\u0001\\u0002\\u0003\\u0004", new String(quoted));
    }

// com.fasterxml.jackson.core.io.TestJsonStringEncoder::testCharSequenceWithCtrlChars
    public void testCharSequenceWithCtrlChars() throws Exception
    {
        char[] input = new char[] { 0, 1, 2, 3, 4 };
        StringBuilder builder = new StringBuilder();
        builder.append(input);
        StringBuilder output = new StringBuilder();
        BufferRecyclers.quoteAsJsonText(builder, output);
        assertEquals("\\u0000\\u0001\\u0002\\u0003\\u0004", output.toString());
    }

// com.fasterxml.jackson.core.io.TestMergedStream::testSimple
    public void testSimple() throws Exception
    {
        BufferRecycler rec = new BufferRecycler();
        IOContext ctxt = new IOContext(rec, null, false);
        
        byte[] first = ctxt.allocReadIOBuffer();
        System.arraycopy("ABCDE".getBytes("UTF-8"), 0, first, 99, 5);
        byte[] second = "FGHIJ".getBytes("UTF-8");

        assertNull(ctxt.getSourceReference());
        assertFalse(ctxt.isResourceManaged());
        ctxt.setEncoding(JsonEncoding.UTF8);
        MergedStream ms = new MergedStream(ctxt, new ByteArrayInputStream(second),
                                           first, 99, 99+5);
        
        assertEquals(5, ms.available());
        
        assertFalse(ms.markSupported());
        
        ms.mark(1);
        assertEquals((byte) 'A', ms.read());
        assertEquals(3, ms.skip(3));
        byte[] buffer = new byte[5];
        
        assertEquals(1, ms.read(buffer, 1, 3));
        assertEquals((byte) 'E', buffer[1]);
        
        assertEquals(3, ms.read(buffer, 0, 3));
        assertEquals((byte) 'F', buffer[0]);
        assertEquals((byte) 'G', buffer[1]);
        assertEquals((byte) 'H', buffer[2]);
        assertEquals(2, ms.available());
        
        assertEquals(2, ms.skip(200));

        ms.close();
    }

// com.fasterxml.jackson.core.io.UTF8WriterTest::testSimple
    public void testSimple() throws Exception
    {
        BufferRecycler rec = new BufferRecycler();
        IOContext ctxt = new IOContext(rec, null, false);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        UTF8Writer w = new UTF8Writer(ctxt, out);

        String str = "AB\u00A0\u1AE9\uFFFC";
        char[] ch = str.toCharArray();

        
        w.write(str);

        w.append(ch[0]);
        w.write(ch[1]);
        w.write(ch, 2, 3);
        w.flush();

        w.write(str, 0, str.length());
        w.close();

        
        byte[] data = out.toByteArray();
        assertEquals(3*10, data.length);
        String act = out.toString("UTF-8");
        assertEquals(15, act.length());

        assertEquals(3 * str.length(), act.length());
        assertEquals(str+str+str, act);
    }

// com.fasterxml.jackson.core.io.UTF8WriterTest::testSimpleAscii
    public void testSimpleAscii() throws Exception
    {
        BufferRecycler rec = new BufferRecycler();
        IOContext ctxt = new IOContext(rec, null, false);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        UTF8Writer w = new UTF8Writer(ctxt, out);

        String str = "abcdefghijklmnopqrst\u00A0";
        char[] ch = str.toCharArray();

        w.write(ch, 0, ch.length);
        w.flush(); 
        w.close();

        byte[] data = out.toByteArray();
        
        assertEquals(ch.length+1, data.length);
        String act = out.toString("UTF-8");
        assertEquals(str, act);
    }

// com.fasterxml.jackson.core.io.UTF8WriterTest::testFlushAfterClose
    public void testFlushAfterClose() throws Exception
    {
        BufferRecycler rec = new BufferRecycler();
        IOContext ctxt = new IOContext(rec, null, false);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        UTF8Writer w = new UTF8Writer(ctxt, out);
        
        w.write('X');
        char[] ch = { 'Y' };
        w.write(ch);
        
        w.close();
        assertEquals(2, out.size());

        
        w.flush();
        
        w.close();
        w.flush();
    }

// com.fasterxml.jackson.core.io.UTF8WriterTest::testSurrogatesOk
    public void testSurrogatesOk() throws Exception
    {
        BufferRecycler rec = new BufferRecycler();
        IOContext ctxt = new IOContext(rec, null, false);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        UTF8Writer w = new UTF8Writer(ctxt, out);

        
        w.write(0xD83D);
        w.write(0xDE03);
        w.close();
        assertEquals(4, out.size());
        final byte[] EXP_SURROGATES = new byte[] { (byte) 0xF0, (byte) 0x9F,
               (byte) 0x98, (byte) 0x83 };
        Assert.assertArrayEquals(EXP_SURROGATES, out.toByteArray());

        
        ctxt = new IOContext(rec, null, false);
        out = new ByteArrayOutputStream();
        w = new UTF8Writer(ctxt, out);
        w.write("\uD83D\uDE03");
        w.close();
        assertEquals(4, out.size());
        Assert.assertArrayEquals(EXP_SURROGATES, out.toByteArray());
    }

// com.fasterxml.jackson.core.io.UTF8WriterTest::testSurrogatesFail
    public void testSurrogatesFail() throws Exception
    {
        BufferRecycler rec = new BufferRecycler();
        IOContext ctxt;
        ByteArrayOutputStream out;
        UTF8Writer w;

        ctxt = new IOContext(rec, null, false);
        out = new ByteArrayOutputStream();
        w = new UTF8Writer(ctxt, out);
        try {
            w.write(0xDE03);
            fail("should not pass");
        } catch (IOException e) {
            verifyException(e, "Unmatched second part");
        }

        ctxt = new IOContext(rec, null, false);
        out = new ByteArrayOutputStream();
        w = new UTF8Writer(ctxt, out);
        w.write(0xD83D);
        try {
            w.write('a');
            fail("should not pass");
        } catch (IOException e) {
            verifyException(e, "Broken surrogate pair");
        }

        ctxt = new IOContext(rec, null, false);
        out = new ByteArrayOutputStream();
        w = new UTF8Writer(ctxt, out);
        try {
            w.write("\uDE03");
            fail("should not pass");
        } catch (IOException e) {
            verifyException(e, "Unmatched second part");
        }
        
        ctxt = new IOContext(rec, null, false);
        out = new ByteArrayOutputStream();
        w = new UTF8Writer(ctxt, out);
        try {
            w.write("\uD83Da");
            fail("should not pass");
        } catch (IOException e) {
            verifyException(e, "Broken surrogate pair");
        }
    }

// com.fasterxml.jackson.core.json.ArrayGenerationTest::testIntArray
    public void testIntArray() throws Exception
    {
        _testIntArray(false);
        _testIntArray(true);
    }

// com.fasterxml.jackson.core.json.ArrayGenerationTest::testLongArray
    public void testLongArray() throws Exception
    {
        _testLongArray(false);
        _testLongArray(true);
    }

// com.fasterxml.jackson.core.json.ArrayGenerationTest::testDoubleArray
    public void testDoubleArray() throws Exception
    {
        _testDoubleArray(false);
        _testDoubleArray(true);
    }

// com.fasterxml.jackson.core.json.GeneratorBasicTest::testStringWrite
    public void testStringWrite() throws Exception
    {
        String[] inputStrings = new String[] { "", "X", "1234567890" };
        for (int useReader = 0; useReader < 2; ++useReader) {
            for (int writeString = 0; writeString < 2; ++writeString) {
                for (int strIx = 0; strIx < inputStrings.length; ++strIx) {
                    String input = inputStrings[strIx];
                    JsonGenerator gen;
                    ByteArrayOutputStream bout = new ByteArrayOutputStream();
                    if (useReader != 0) {
                        gen = JSON_F.createGenerator(new OutputStreamWriter(bout, "UTF-8"));
                    } else {
                        gen = JSON_F.createGenerator(bout, JsonEncoding.UTF8);
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
                    JsonParser jp = JSON_F.createParser(new ByteArrayInputStream(bout.toByteArray()));
                
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

// com.fasterxml.jackson.core.json.GeneratorBasicTest::testIntValueWrite
    public void testIntValueWrite() throws Exception
    {
        
        doTestIntValueWrite(false, false);
        doTestIntValueWrite(true, false);
        
        doTestIntValueWrite(false, true);
        doTestIntValueWrite(true, true);
    }

// com.fasterxml.jackson.core.json.GeneratorBasicTest::testLongValueWrite
    public void testLongValueWrite() throws Exception
    {
        
        doTestLongValueWrite(false, false);
        doTestLongValueWrite(true, false);
        
        doTestLongValueWrite(false, true);
        doTestLongValueWrite(true, true);
    }

// com.fasterxml.jackson.core.json.GeneratorBasicTest::testBooleanWrite
    public void testBooleanWrite() throws Exception
    {
        for (int i = 0; i < 4; ++i) {
            boolean state = (i & 1) == 0;
            boolean pad = (i & 2) == 0;
            StringWriter sw = new StringWriter();
            JsonGenerator gen = JSON_F.createGenerator(sw);
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

// com.fasterxml.jackson.core.json.GeneratorBasicTest::testNullWrite
    public void testNullWrite()
        throws Exception
    {
        for (int i = 0; i < 2; ++i) {
            boolean pad = (i & 1) == 0;
            StringWriter sw = new StringWriter();
            JsonGenerator gen = JSON_F.createGenerator(sw);
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

// com.fasterxml.jackson.core.json.GeneratorBasicTest::testRootIntsWrite
     public void testRootIntsWrite()
         throws Exception
     {
         StringWriter sw = new StringWriter();
         JsonGenerator gen = JSON_F.createGenerator(sw);
         gen.writeNumber(1);
         gen.writeNumber(2);
         gen.writeNumber(-13);
         gen.close();

         String docStr = sw.toString();

         try {
             JsonParser jp = createParserUsingReader(docStr);
             assertEquals(JsonToken.VALUE_NUMBER_INT, jp.nextToken());
             assertEquals(1, jp.getIntValue());
             assertEquals(JsonToken.VALUE_NUMBER_INT, jp.nextToken());
             assertEquals(2, jp.getIntValue());
             assertEquals(JsonToken.VALUE_NUMBER_INT, jp.nextToken());
             assertEquals(-13, jp.getIntValue());
             jp.close();
         } catch (IOException e) {
             fail("Problem with document ["+docStr+"]: "+e.getMessage());
         }
     }

// com.fasterxml.jackson.core.json.GeneratorBasicTest::testFieldValueWrites
    public void testFieldValueWrites()
         throws Exception
     {
         StringWriter sw = new StringWriter();
         JsonGenerator gen = JSON_F.createGenerator(sw);
         gen.writeStartObject();
         gen.writeNumberField("long", 3L);
         gen.writeNumberField("double", 0.25);
         gen.writeNumberField("float", -0.25f);
         gen.writeEndObject();
         gen.close();

         assertEquals("{\"long\":3,\"double\":0.25,\"float\":-0.25}", sw.toString().trim());
     }

// com.fasterxml.jackson.core.json.GeneratorBasicTest::testOutputContext
    public void testOutputContext() throws Exception
    {
        StringWriter sw = new StringWriter();
        JsonGenerator gen = JSON_F.createGenerator(sw);
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

// com.fasterxml.jackson.core.json.GeneratorBasicTest::testGetOutputTarget
    public void testGetOutputTarget() throws Exception
    {
        OutputStream out = new ByteArrayOutputStream();
        JsonGenerator gen = JSON_F.createGenerator(out);
        assertSame(out, gen.getOutputTarget());
        gen.close();

        StringWriter sw = new StringWriter();
        gen = JSON_F.createGenerator(sw);
        assertSame(sw, gen.getOutputTarget());
        gen.close();
    }

// com.fasterxml.jackson.core.json.GeneratorBasicTest::testGetOutputBufferd
    public void testGetOutputBufferd() throws Exception
    {
        OutputStream out = new ByteArrayOutputStream();
        JsonGenerator gen = JSON_F.createGenerator(out);
        _testOutputBuffered(gen);
        gen.close();

        StringWriter sw = new StringWriter();
        gen = JSON_F.createGenerator(sw);
        _testOutputBuffered(gen);
        gen.close();
    }

// com.fasterxml.jackson.core.json.GeneratorFailFromReaderTest::testFailOnWritingStringNotFieldNameBytes
    public void testFailOnWritingStringNotFieldNameBytes() throws Exception {
        _testFailOnWritingStringNotFieldName(F, false);
    }

// com.fasterxml.jackson.core.json.GeneratorFailFromReaderTest::testFailOnWritingStringNotFieldNameChars
    public void testFailOnWritingStringNotFieldNameChars() throws Exception {
        _testFailOnWritingStringNotFieldName(F, true);        
    }

// com.fasterxml.jackson.core.json.GeneratorFailFromReaderTest::testFailOnWritingStringFromReaderWithTooFewCharacters
    public void testFailOnWritingStringFromReaderWithTooFewCharacters() throws Exception {
        _testFailOnWritingStringFromReaderWithTooFewCharacters(F, true);
        _testFailOnWritingStringFromReaderWithTooFewCharacters(F, false);
    }

// com.fasterxml.jackson.core.json.GeneratorFailFromReaderTest::testFailOnWritingStringFromNullReader
    public void testFailOnWritingStringFromNullReader() throws Exception {
        _testFailOnWritingStringFromNullReader(F, true);
        _testFailOnWritingStringFromNullReader(F, false);
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

// com.fasterxml.jackson.core.json.GeneratorFailTest::testFailOnWritingFieldNameInRoot
    public void testFailOnWritingFieldNameInRoot() throws Exception {
        _testFailOnWritingFieldNameInRoot(F, false);
        _testFailOnWritingFieldNameInRoot(F, true);
    }

// com.fasterxml.jackson.core.json.GeneratorFeaturesTest::testConfigDefaults
    public void testConfigDefaults() throws IOException
    {
        JsonGenerator g = JSON_F.createGenerator(new StringWriter());
        assertFalse(g.isEnabled(JsonGenerator.Feature.WRITE_NUMBERS_AS_STRINGS));
        assertFalse(g.isEnabled(JsonGenerator.Feature.WRITE_BIGDECIMAL_AS_PLAIN));

        assertTrue(g.canOmitFields());
        assertFalse(g.canWriteBinaryNatively());
        assertTrue(g.canWriteFormattedNumbers());
        assertFalse(g.canWriteObjectId());
        assertFalse(g.canWriteTypeId());
        
        g.close();
    }

// com.fasterxml.jackson.core.json.GeneratorFeaturesTest::testConfigOverrides
    public void testConfigOverrides() throws IOException
    {
        
        JsonGenerator g = JSON_F.createGenerator(new StringWriter());
        int mask = JsonGenerator.Feature.WRITE_NUMBERS_AS_STRINGS.getMask()
                | JsonGenerator.Feature.WRITE_BIGDECIMAL_AS_PLAIN.getMask();
        g.overrideStdFeatures(mask, mask);
        assertTrue(g.isEnabled(JsonGenerator.Feature.WRITE_NUMBERS_AS_STRINGS));
        assertTrue(g.isEnabled(JsonGenerator.Feature.WRITE_BIGDECIMAL_AS_PLAIN));

        
        g.setFeatureMask(0);
        assertFalse(g.isEnabled(JsonGenerator.Feature.WRITE_NUMBERS_AS_STRINGS));
        assertFalse(g.isEnabled(JsonGenerator.Feature.WRITE_BIGDECIMAL_AS_PLAIN));
        g.close();
    }

// com.fasterxml.jackson.core.json.GeneratorFeaturesTest::testFieldNameQuoting
    public void testFieldNameQuoting() throws IOException
    {
        JsonFactory f = new JsonFactory();
        
        _testFieldNameQuoting(f, true);
        
        f.disable(JsonGenerator.Feature.QUOTE_FIELD_NAMES);
        _testFieldNameQuoting(f, false);
        
        f.enable(JsonGenerator.Feature.QUOTE_FIELD_NAMES);
        _testFieldNameQuoting(f, true);
    }

// com.fasterxml.jackson.core.json.GeneratorFeaturesTest::testNonNumericQuoting
    public void testNonNumericQuoting() throws IOException
    {
        JsonFactory f = new JsonFactory();
        
        _testNonNumericQuoting(f, true);
        
        f.disable(JsonGenerator.Feature.QUOTE_NON_NUMERIC_NUMBERS);
        _testNonNumericQuoting(f, false);
        
        f.enable(JsonGenerator.Feature.QUOTE_NON_NUMERIC_NUMBERS);
        _testNonNumericQuoting(f, true);
    }

// com.fasterxml.jackson.core.json.GeneratorFeaturesTest::testNumbersAsJSONStrings
    public void testNumbersAsJSONStrings() throws IOException
    {
        JsonFactory f = new JsonFactory();
        
        assertEquals("[1,2,1.25,2.25,3001,0.5,-1]", _writeNumbers(f));        

        
        f.configure(JsonGenerator.Feature.WRITE_NUMBERS_AS_STRINGS, true);
        assertEquals("[\"1\",\"2\",\"1.25\",\"2.25\",\"3001\",\"0.5\",\"-1\"]",
                     _writeNumbers(f));

        
    }

// com.fasterxml.jackson.core.json.GeneratorFeaturesTest::testBigDecimalAsPlain
    public void testBigDecimalAsPlain() throws IOException
    {
        JsonFactory f = new JsonFactory();
        BigDecimal ENG = new BigDecimal("1E+2");

        StringWriter sw = new StringWriter();
        JsonGenerator g = f.createGenerator(sw);
        g.writeNumber(ENG);
        g.close();
        assertEquals("1E+2", sw.toString());

        f.configure(JsonGenerator.Feature.WRITE_BIGDECIMAL_AS_PLAIN, true);
        sw = new StringWriter();
        g = f.createGenerator(sw);
        g.writeNumber(ENG);
        g.close();
        assertEquals("100", sw.toString());
    }

// com.fasterxml.jackson.core.json.GeneratorFeaturesTest::testBigDecimalAsPlainString
    public void testBigDecimalAsPlainString() throws Exception
    {
        JsonFactory f = new JsonFactory();
        BigDecimal ENG = new BigDecimal("1E+2");
        f.enable(JsonGenerator.Feature.WRITE_BIGDECIMAL_AS_PLAIN);
        f.enable(JsonGenerator.Feature.WRITE_NUMBERS_AS_STRINGS);

        StringWriter sw = new StringWriter();
        JsonGenerator g = f.createGenerator(sw);
        g.writeNumber(ENG);
        g.close();
        assertEquals(quote("100"), sw.toString());

        
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        g = f.createGenerator(bos);
        g.writeNumber(ENG);
        g.close();
        assertEquals(quote("100"), bos.toString("UTF-8"));
    }

// com.fasterxml.jackson.core.json.GeneratorFeaturesTest::testTooBigBigDecimal
    public void testTooBigBigDecimal() throws Exception
    {
        JsonFactory f = new JsonFactory();
        f.enable(JsonGenerator.Feature.WRITE_BIGDECIMAL_AS_PLAIN);

        
        BigDecimal BIG = new BigDecimal("1E+9999");
        BigDecimal TOO_BIG = new BigDecimal("1E+10000");
        BigDecimal SMALL = new BigDecimal("1E-9999");
        BigDecimal TOO_SMALL = new BigDecimal("1E-10000");

        for (boolean useBytes : new boolean[] { false, true } ) {
            for (boolean asString : new boolean[] { false, true } ) {
                JsonGenerator g;
                
                if (useBytes) {
                    g = f.createGenerator(new ByteArrayOutputStream());
                } else {
                    g = f.createGenerator(new StringWriter());
                }
                if (asString) {
                    g.enable(JsonGenerator.Feature.WRITE_NUMBERS_AS_STRINGS);
                }

                
                g.writeStartArray();
                g.writeNumber(BIG);
                g.writeNumber(SMALL);
                g.writeEndArray();
                g.close();

                
                for (BigDecimal input : new BigDecimal[] { TOO_BIG, TOO_SMALL }) {
                    if (useBytes) {
                        g = f.createGenerator(new ByteArrayOutputStream());
                    } else {
                        g = f.createGenerator(new StringWriter());
                    }
                    if (asString) {
                        g.enable(JsonGenerator.Feature.WRITE_NUMBERS_AS_STRINGS);
                    }
                    try {
                        g.writeNumber(input);
                        fail("Should not have written without exception: "+input);
                    } catch (JsonGenerationException e) {
                        verifyException(e, "Attempt to write plain `java.math.BigDecimal`");
                        verifyException(e, "illegal scale");
                    }
                    g.close();
                }
            }
        }
    }

// com.fasterxml.jackson.core.json.GeneratorFeaturesTest::testFieldNameQuotingEnabled
    public void testFieldNameQuotingEnabled() throws IOException
    {
        
        
        
        _testFieldNameQuotingEnabled(JSON_F, true, true, "{\"foo\":1}");
        _testFieldNameQuotingEnabled(JSON_F, false, true, "{\"foo\":1}");

        
        _testFieldNameQuotingEnabled(JSON_F, true, false, "{foo:1}");
        _testFieldNameQuotingEnabled(JSON_F, false, false, "{foo:1}");

        

        JsonFactory f2 = new JsonFactory();
        f2.disable(JsonGenerator.Feature.QUOTE_FIELD_NAMES);

        _testFieldNameQuotingEnabled(f2, true, true, "{\"foo\":1}");
        _testFieldNameQuotingEnabled(f2, false, true, "{\"foo\":1}");

        
        _testFieldNameQuotingEnabled(f2, true, false, "{foo:1}");
        _testFieldNameQuotingEnabled(f2, false, false, "{foo:1}");
    }

// com.fasterxml.jackson.core.json.GeneratorFeaturesTest::testChangeOnGenerator
    public void testChangeOnGenerator() throws IOException
    {
        StringWriter w = new StringWriter();

        JsonGenerator g = JSON_F.createGenerator(w);
        g.enable(JsonGenerator.Feature.WRITE_NUMBERS_AS_STRINGS);
        g.writeNumber(123);
        g.close();
        assertEquals(quote("123"), w.toString());

        
        w = new StringWriter();
        g = JSON_F.createGenerator(w);
        g.enable(JsonGenerator.Feature.WRITE_NUMBERS_AS_STRINGS);
        g.disable(JsonGenerator.Feature.WRITE_NUMBERS_AS_STRINGS);
        g.writeNumber(123);
        g.close();
        assertEquals("123", w.toString());
    }

// com.fasterxml.jackson.core.json.JsonFactoryTest::testGeneratorFeatures
    public void testGeneratorFeatures() throws Exception
    {
        JsonFactory f = new JsonFactory();
        assertNull(f.getCodec());

        f.configure(JsonGenerator.Feature.QUOTE_FIELD_NAMES, true);
        assertTrue(f.isEnabled(JsonGenerator.Feature.QUOTE_FIELD_NAMES));
        f.configure(JsonGenerator.Feature.QUOTE_FIELD_NAMES, false);
        assertFalse(f.isEnabled(JsonGenerator.Feature.QUOTE_FIELD_NAMES));
    }

// com.fasterxml.jackson.core.json.JsonFactoryTest::testFactoryFeatures
    public void testFactoryFeatures() throws Exception
    {
        JsonFactory f = new JsonFactory();

        f.configure(JsonFactory.Feature.INTERN_FIELD_NAMES, true);
        assertTrue(f.isEnabled(JsonFactory.Feature.INTERN_FIELD_NAMES));
        f.configure(JsonFactory.Feature.INTERN_FIELD_NAMES, false);
        assertFalse(f.isEnabled(JsonFactory.Feature.INTERN_FIELD_NAMES));

        
        assertTrue(f.isEnabled(JsonFactory.Feature.USE_THREAD_LOCAL_FOR_BUFFER_RECYCLING));
        f.configure(JsonFactory.Feature.USE_THREAD_LOCAL_FOR_BUFFER_RECYCLING, false);
        assertFalse(f.isEnabled(JsonFactory.Feature.USE_THREAD_LOCAL_FOR_BUFFER_RECYCLING));
    }

// com.fasterxml.jackson.core.json.JsonFactoryTest::testDisablingBufferRecycling
    public void testDisablingBufferRecycling() throws Exception
    {
        JsonFactory f = new JsonFactory();

        f.disable(JsonFactory.Feature.USE_THREAD_LOCAL_FOR_BUFFER_RECYCLING);

        
        for (int i = 0; i < 3; ++i) {
            StringWriter w = new StringWriter();
            JsonGenerator gen = f.createGenerator(w);
            gen.writeStartObject();
            gen.writeEndObject();
            gen.close();
            assertEquals("{}", w.toString());
        }
    
        for (int i = 0; i < 3; ++i) {
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            JsonGenerator gen = f.createGenerator(bytes);
            gen.writeStartArray();
            gen.writeEndArray();
            gen.close();
            assertEquals("[]", bytes.toString("UTF-8"));
        }

        
        for (int i = 0; i < 3; ++i) {
            JsonParser p = f.createParser("{}");
            assertToken(JsonToken.START_OBJECT, p.nextToken());
            assertToken(JsonToken.END_OBJECT, p.nextToken());
            assertNull(p.nextToken());
            p.close();

            p = f.createParser("{}".getBytes("UTF-8"));
            assertToken(JsonToken.START_OBJECT, p.nextToken());
            assertToken(JsonToken.END_OBJECT, p.nextToken());
            assertNull(p.nextToken());
            p.close();
        }
    }

// com.fasterxml.jackson.core.json.JsonFactoryTest::testJsonWithFiles
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

// com.fasterxml.jackson.core.json.JsonFactoryTest::testCopy
    public void testCopy() throws Exception
    {
        JsonFactory jf = new JsonFactory();
        
        assertNull(jf.getCodec());
        assertTrue(jf.isEnabled(JsonFactory.Feature.INTERN_FIELD_NAMES));
        assertFalse(jf.isEnabled(JsonParser.Feature.ALLOW_COMMENTS));
        assertFalse(jf.isEnabled(JsonGenerator.Feature.ESCAPE_NON_ASCII));

        
        jf.disable(JsonFactory.Feature.INTERN_FIELD_NAMES);
        jf.enable(JsonParser.Feature.ALLOW_COMMENTS);
        jf.enable(JsonGenerator.Feature.ESCAPE_NON_ASCII);
        ObjectCodec codec = new BogusCodec();
        jf.setCodec(codec);

        assertFalse(jf.isEnabled(JsonFactory.Feature.INTERN_FIELD_NAMES));
        assertTrue(jf.isEnabled(JsonParser.Feature.ALLOW_COMMENTS));
        assertTrue(jf.isEnabled(JsonGenerator.Feature.ESCAPE_NON_ASCII));
        assertSame(codec, jf.getCodec());

        JsonFactory jf2 = jf.copy();
        assertFalse(jf2.isEnabled(JsonFactory.Feature.INTERN_FIELD_NAMES));
        assertTrue(jf2.isEnabled(JsonParser.Feature.ALLOW_COMMENTS));
        assertTrue(jf2.isEnabled(JsonGenerator.Feature.ESCAPE_NON_ASCII));
        
        assertNull(jf2.getCodec());

        
        JsonFactory jf3 = new CustomFactory(jf, codec);
        assertSame(codec, jf3.getCodec());
    }

// com.fasterxml.jackson.core.json.JsonParserClosedCaseTest::testNullReturnedOnClosedParserOnNextFieldName
    public void testNullReturnedOnClosedParserOnNextFieldName() throws Exception {
        Assert.assertNull(parser.nextFieldName());
    }

// com.fasterxml.jackson.core.json.JsonParserClosedCaseTest::testFalseReturnedOnClosedParserOnNextFieldNameSerializedString
    public void testFalseReturnedOnClosedParserOnNextFieldNameSerializedString() throws Exception {
        Assert.assertFalse(parser.nextFieldName(new SerializedString("")));
    }

// com.fasterxml.jackson.core.json.JsonParserClosedCaseTest::testNullReturnedOnClosedParserOnNextToken
    public void testNullReturnedOnClosedParserOnNextToken() throws Exception {
        Assert.assertNull(parser.nextToken());
    }

// com.fasterxml.jackson.core.json.JsonParserClosedCaseTest::testNullReturnedOnClosedParserOnNextValue
    public void testNullReturnedOnClosedParserOnNextValue() throws Exception {
        Assert.assertNull(parser.nextValue());
    }

// com.fasterxml.jackson.core.json.LocationInArrayTest::testOffsetInArraysBytes
    public void testOffsetInArraysBytes() throws Exception {
        _testOffsetInArrays(true);
    }

// com.fasterxml.jackson.core.json.LocationInArrayTest::testOffsetInArraysChars
    public void testOffsetInArraysChars() throws Exception {
        _testOffsetInArrays(false);
    }

// com.fasterxml.jackson.core.json.LocationInObjectTest::testOffsetWithObjectFieldsUsingUTF8
    public void testOffsetWithObjectFieldsUsingUTF8() throws Exception
    {
        final JsonFactory f = new JsonFactory();
        byte[] b = "{\"f1\":\"v1\",\"f2\":{\"f3\":\"v3\"},\"f4\":[true,false],\"f5\":5}".getBytes("UTF-8");
        
        JsonParser p = f.createParser(b);

        assertEquals(JsonToken.START_OBJECT, p.nextToken());

        assertEquals(JsonToken.FIELD_NAME, p.nextToken());
        assertEquals(1L, p.getTokenLocation().getByteOffset());
        assertEquals(JsonToken.VALUE_STRING, p.nextToken());
        assertEquals(6L, p.getTokenLocation().getByteOffset());

        assertEquals("f2", p.nextFieldName());
        assertEquals(11L, p.getTokenLocation().getByteOffset());
        assertEquals(JsonToken.START_OBJECT, p.nextValue());
        assertEquals(16L, p.getTokenLocation().getByteOffset());

        assertEquals("f3", p.nextFieldName());
        assertEquals(17L, p.getTokenLocation().getByteOffset());
        assertEquals(JsonToken.VALUE_STRING, p.nextValue());
        assertEquals(22L, p.getTokenLocation().getByteOffset());
        assertEquals(JsonToken.END_OBJECT, p.nextToken());

        assertEquals("f4", p.nextFieldName());
        assertEquals(28L, p.getTokenLocation().getByteOffset());
        assertEquals(JsonToken.START_ARRAY, p.nextValue());
        assertEquals(33L, p.getTokenLocation().getByteOffset());

        assertEquals(JsonToken.VALUE_TRUE, p.nextValue());
        assertEquals(34L, p.getTokenLocation().getByteOffset());

        assertEquals(JsonToken.VALUE_FALSE, p.nextValue());
        assertEquals(39L, p.getTokenLocation().getByteOffset());
        assertEquals(JsonToken.END_ARRAY, p.nextToken());

        assertEquals("f5", p.nextFieldName());
        assertEquals(46L, p.getTokenLocation().getByteOffset());
        assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertEquals(51L, p.getTokenLocation().getByteOffset());
        assertEquals(JsonToken.END_OBJECT, p.nextToken());

        p.close();
    }

// com.fasterxml.jackson.core.json.LocationInObjectTest::testOffsetWithObjectFieldsUsingReader
    public void testOffsetWithObjectFieldsUsingReader() throws Exception
    {
        final JsonFactory f = new JsonFactory();
        char[] c = "{\"f1\":\"v1\",\"f2\":{\"f3\":\"v3\"},\"f4\":[true,false],\"f5\":5}".toCharArray();
        
        JsonParser p = f.createParser(c);

        assertEquals(JsonToken.START_OBJECT, p.nextToken());

        assertEquals(JsonToken.FIELD_NAME, p.nextToken());
        assertEquals(1L, p.getTokenLocation().getCharOffset());
        assertEquals(JsonToken.VALUE_STRING, p.nextToken());
        assertEquals(6L, p.getTokenLocation().getCharOffset());

        assertEquals("f2", p.nextFieldName());
        assertEquals(11L, p.getTokenLocation().getCharOffset());
        assertEquals(JsonToken.START_OBJECT, p.nextValue());
        assertEquals(16L, p.getTokenLocation().getCharOffset());

        assertEquals("f3", p.nextFieldName());
        assertEquals(17L, p.getTokenLocation().getCharOffset());
        assertEquals(JsonToken.VALUE_STRING, p.nextValue());
        assertEquals(22L, p.getTokenLocation().getCharOffset());
        assertEquals(JsonToken.END_OBJECT, p.nextToken());

        assertEquals("f4", p.nextFieldName());
        assertEquals(28L, p.getTokenLocation().getCharOffset());
        assertEquals(JsonToken.START_ARRAY, p.nextValue());
        assertEquals(33L, p.getTokenLocation().getCharOffset());

        assertEquals(JsonToken.VALUE_TRUE, p.nextValue());
        assertEquals(34L, p.getTokenLocation().getCharOffset());

        assertEquals(JsonToken.VALUE_FALSE, p.nextValue());
        assertEquals(39L, p.getTokenLocation().getCharOffset());
        assertEquals(JsonToken.END_ARRAY, p.nextToken());

        assertEquals("f5", p.nextFieldName());
        assertEquals(46L, p.getTokenLocation().getCharOffset());
        assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertEquals(51L, p.getTokenLocation().getCharOffset());
        assertEquals(JsonToken.END_OBJECT, p.nextToken());

        p.close();
    }

// com.fasterxml.jackson.core.json.LocationOffsetsTest::testSimpleInitialOffsets
    public void testSimpleInitialOffsets() throws Exception
    {
        JsonLocation loc;
        JsonParser p;
        final String DOC = "{ }";

        
        p = JSON_F.createParser(DOC);
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

        
        
        p = JSON_F.createParser(DOC.getBytes("UTF-8"));
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

// com.fasterxml.jackson.core.json.LocationOffsetsTest::testOffsetWithInputOffset
    public void testOffsetWithInputOffset() throws Exception
    {
        JsonLocation loc;
        JsonParser p;
        
        byte[] b = "   { }  ".getBytes("UTF-8");

        
        p = JSON_F.createParser(b, 3, b.length-5);
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

// com.fasterxml.jackson.core.json.ParserSequenceTest::testSimple
    public void testSimple() throws Exception
    {
        JsonParser p1 = JSON_FACTORY.createParser("[ 1 ]");
        JsonParser p2 = JSON_FACTORY.createParser("[ 2 ]");
        JsonParserSequence seq = JsonParserSequence.createFlattened(false, p1, p2);
        assertEquals(2, seq.containedParsersCount());

        assertFalse(p1.isClosed());
        assertFalse(p2.isClosed());
        assertFalse(seq.isClosed());
        assertToken(JsonToken.START_ARRAY, seq.nextToken());
        assertToken(JsonToken.VALUE_NUMBER_INT, seq.nextToken());
        assertEquals(1, seq.getIntValue());
        assertToken(JsonToken.END_ARRAY, seq.nextToken());
        assertFalse(p1.isClosed());
        assertFalse(p2.isClosed());
        assertFalse(seq.isClosed());
        assertToken(JsonToken.START_ARRAY, seq.nextToken());

        
        assertTrue(p1.isClosed());
        assertFalse(p2.isClosed());
        assertFalse(seq.isClosed());

        assertToken(JsonToken.VALUE_NUMBER_INT, seq.nextToken());
        assertEquals(2, seq.getIntValue());
        assertToken(JsonToken.END_ARRAY, seq.nextToken());
        assertTrue(p1.isClosed());
        assertFalse(p2.isClosed());
        assertFalse(seq.isClosed());

        assertNull(seq.nextToken());
        assertTrue(p1.isClosed());
        assertTrue(p2.isClosed());
        assertTrue(seq.isClosed());

        seq.close();
    }

// com.fasterxml.jackson.core.json.ParserSequenceTest::testMultiLevel
    public void testMultiLevel() throws Exception
    {
        JsonParser p1 = JSON_FACTORY.createParser("[ 1 ] ");
        JsonParser p2 = JSON_FACTORY.createParser(" 5");
        JsonParser p3 = JSON_FACTORY.createParser(" { } ");
        JsonParserSequence seq1 = JsonParserSequence.createFlattened(true, p1, p2);
        JsonParserSequence seq = JsonParserSequence.createFlattened(false, seq1, p3);
        assertEquals(3, seq.containedParsersCount());

        assertToken(JsonToken.START_ARRAY, seq.nextToken());
        assertToken(JsonToken.VALUE_NUMBER_INT, seq.nextToken());
        assertToken(JsonToken.END_ARRAY, seq.nextToken());

        assertToken(JsonToken.VALUE_NUMBER_INT, seq.nextToken());
        
        assertToken(JsonToken.START_OBJECT, seq.nextToken());
        assertToken(JsonToken.END_OBJECT, seq.nextToken());

        assertNull(seq.nextToken());
        assertTrue(p1.isClosed());
        assertTrue(p2.isClosed());
        assertTrue(p3.isClosed());
        assertTrue(seq.isClosed());
    }

// com.fasterxml.jackson.core.json.ParserSequenceTest::testInitializationDisabled
    public void testInitializationDisabled() throws Exception
    {
        

        JsonParser p1 = JSON_FACTORY.createParser("1 2");
        JsonParser p2 = JSON_FACTORY.createParser("3 true");
        assertToken(JsonToken.VALUE_NUMBER_INT, p1.nextToken());
        assertEquals(1, p1.getIntValue());
        assertToken(JsonToken.VALUE_NUMBER_INT, p2.nextToken());
        assertEquals(3, p2.getIntValue());

        
        
        JsonParserSequence seq = JsonParserSequence.createFlattened(false, p1, p2);
        assertToken(JsonToken.VALUE_NUMBER_INT, seq.nextToken());
        assertEquals(2, seq.getIntValue());
        assertToken(JsonToken.VALUE_TRUE, seq.nextToken());
        assertNull(seq.nextToken());
        seq.close();
    }

// com.fasterxml.jackson.core.json.ParserSequenceTest::testInitializationEnabled
    public void testInitializationEnabled() throws Exception
    {
        
        JsonParser p1 = JSON_FACTORY.createParser("1 2");
        JsonParser p2 = JSON_FACTORY.createParser("3 true");
        assertToken(JsonToken.VALUE_NUMBER_INT, p1.nextToken());
        assertEquals(1, p1.getIntValue());
        assertToken(JsonToken.VALUE_NUMBER_INT, p2.nextToken());
        assertEquals(3, p2.getIntValue());

        
        
        JsonParserSequence seq = JsonParserSequence.createFlattened(true, p1, p2);
        assertToken(JsonToken.VALUE_NUMBER_INT, seq.nextToken());
        assertEquals(1, seq.getIntValue());
        assertToken(JsonToken.VALUE_NUMBER_INT, seq.nextToken());
        assertEquals(2, seq.getIntValue());
        assertToken(JsonToken.VALUE_NUMBER_INT, seq.nextToken());
        assertEquals(3, seq.getIntValue());
        assertToken(JsonToken.VALUE_TRUE, seq.nextToken());
        assertNull(seq.nextToken());
        seq.close();
    }

// com.fasterxml.jackson.core.json.RawValueWithSurrogatesTest::testRawWithSurrogatesString
    public void testRawWithSurrogatesString() throws Exception {
        _testRawWithSurrogatesString(false);
    }

// com.fasterxml.jackson.core.json.RawValueWithSurrogatesTest::testRawWithSurrogatesCharArray
    public void testRawWithSurrogatesCharArray() throws Exception {
        _testRawWithSurrogatesString(true);
    }

// com.fasterxml.jackson.core.json.RequestPayloadOnExceptionTest::testRequestPayloadAsBytesOnParseException
    public void testRequestPayloadAsBytesOnParseException() throws Exception {
        testRequestPayloadAsBytesOnParseExceptionInternal(true, "nul");
        testRequestPayloadAsBytesOnParseExceptionInternal(false, "nul");
    }

// com.fasterxml.jackson.core.json.RequestPayloadOnExceptionTest::testRequestPayloadAsStringOnParseException
    public void testRequestPayloadAsStringOnParseException() throws Exception {
        testRequestPayloadAsStringOnParseExceptionInternal(true, "nul");
        testRequestPayloadAsStringOnParseExceptionInternal(false, "nul");
    }

// com.fasterxml.jackson.core.json.RequestPayloadOnExceptionTest::testRawRequestPayloadOnParseException
    public void testRawRequestPayloadOnParseException() throws Exception {
        testRawRequestPayloadOnParseExceptionInternal(true, "nul");
        testRawRequestPayloadOnParseExceptionInternal(false, "nul");
    }

// com.fasterxml.jackson.core.json.RequestPayloadOnExceptionTest::testNoRequestPayloadOnParseException
    public void testNoRequestPayloadOnParseException() throws Exception {
        testNoRequestPayloadOnParseExceptionInternal(true, "nul");
        testNoRequestPayloadOnParseExceptionInternal(false, "nul");
    }

// com.fasterxml.jackson.core.json.RequestPayloadOnExceptionTest::testNullRequestPayloadOnParseException
    public void testNullRequestPayloadOnParseException() throws Exception {
        testNullRequestPayloadOnParseExceptionInternal(true, "nul");
        testNullRequestPayloadOnParseExceptionInternal(false, "nul");
    }

// com.fasterxml.jackson.core.json.RequestPayloadOnExceptionTest::testNullCharsetOnParseException
    public void testNullCharsetOnParseException() throws Exception {
        testNullCharsetOnParseExceptionInternal(true, "nul");
        testNullCharsetOnParseExceptionInternal(false, "nul");
    }

// com.fasterxml.jackson.core.json.StringGenerationFromReaderTest::testBasicEscaping
    public void testBasicEscaping() throws Exception
    {
        doTestBasicEscaping();
    }

// com.fasterxml.jackson.core.json.StringGenerationFromReaderTest::testMediumStringsBytes
    public void testMediumStringsBytes() throws Exception
    {
        for (int mode : ALL_BINARY_MODES) {
            for (int size : new int[] { 1100, 2300, 3800, 7500, 19000 }) {
                _testMediumStrings(mode, size);
            }
        }
    }

// com.fasterxml.jackson.core.json.StringGenerationFromReaderTest::testMediumStringsChars
    public void testMediumStringsChars() throws Exception
    {
        for (int mode : ALL_TEXT_MODES) {
            for (int size : new int[] { 1100, 2300, 3800, 7500, 19000 }) {
                _testMediumStrings(mode, size);
            }
        }
    }

// com.fasterxml.jackson.core.json.StringGenerationFromReaderTest::testLongerRandomSingleChunk
    public void testLongerRandomSingleChunk() throws Exception
    {
        
        for (int mode : ALL_TEXT_MODES) {
            for (int round = 0; round < 80; ++round) {
                String content = generateRandom(75000+round);
                _testLongerRandom(mode, content);
            }
        }
    }

// com.fasterxml.jackson.core.json.StringGenerationFromReaderTest::testLongerRandomMultiChunk
    public void testLongerRandomMultiChunk() throws Exception
    {
        
        for (int mode : ALL_TEXT_MODES) {
            for (int round = 0; round < 70; ++round) {
                String content = generateRandom(73000+round);
                _testLongerRandomMulti(mode, content, round);
            }
        }
    }

// com.fasterxml.jackson.core.json.StringGenerationTest::testBasicEscaping
    public void testBasicEscaping() throws Exception
    {
        doTestBasicEscaping(false);
        doTestBasicEscaping(true);
    }

// com.fasterxml.jackson.core.json.StringGenerationTest::testMediumStringsBytes
    public void testMediumStringsBytes() throws Exception
    {
        for (int mode : ALL_BINARY_MODES) {
            for (int size : new int[] { 1100, 2300, 3800, 7500, 19000 }) {
                _testMediumStrings(mode, size);
            }
        }
    }

// com.fasterxml.jackson.core.json.StringGenerationTest::testMediumStringsChars
    public void testMediumStringsChars() throws Exception
    {
        for (int mode : ALL_TEXT_MODES) {
            for (int size : new int[] { 1100, 2300, 3800, 7500, 19000 }) {
                _testMediumStrings(mode, size);
            }
        }
    }

// com.fasterxml.jackson.core.json.StringGenerationTest::testLongerRandomSingleChunk
    public void testLongerRandomSingleChunk() throws Exception
    {
        
        for (int mode : ALL_TEXT_MODES) {
            for (int round = 0; round < 80; ++round) {
                String content = generateRandom(75000+round);
                _testLongerRandom(mode, content, false);
                _testLongerRandom(mode, content, true);
            }
        }
    }

// com.fasterxml.jackson.core.json.StringGenerationTest::testLongerRandomMultiChunk
    public void testLongerRandomMultiChunk() throws Exception
    {
        
        for (int mode : ALL_TEXT_MODES) {
            for (int round = 0; round < 70; ++round) {
                String content = generateRandom(73000+round);
                _testLongerRandomMulti(mode, content, false, round);
                _testLongerRandomMulti(mode, content, true, round);
            }
        }
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

// com.fasterxml.jackson.core.json.TestCustomEscaping::testJsonpEscapes
    public void testJsonpEscapes() throws Exception {
        _testJsonpEscapes(false);
        _testJsonpEscapes(true);
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

// com.fasterxml.jackson.core.json.TestMaxErrorSize::testLongErrorMessage
    public void testLongErrorMessage() throws Exception
    {
        _testLongErrorMessage(MODE_INPUT_STREAM);
        _testLongErrorMessage(MODE_INPUT_STREAM_THROTTLED);
    }

// com.fasterxml.jackson.core.json.TestMaxErrorSize::testLongErrorMessageReader
    public void testLongErrorMessageReader() throws Exception
    {
_testLongErrorMessage(MODE_READER);
    }

// com.fasterxml.jackson.core.json.TestMaxErrorSize::testShortErrorMessage
    public void testShortErrorMessage() throws Exception
    {
        _testShortErrorMessage(MODE_INPUT_STREAM);
        _testShortErrorMessage(MODE_INPUT_STREAM_THROTTLED);
        _testShortErrorMessage(MODE_READER);
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

// com.fasterxml.jackson.core.json.TestRootValues::testSimpleNumbers
    public void testSimpleNumbers() throws Exception
    {
        _testSimpleNumbers(false);
        _testSimpleNumbers(true);
    }

// com.fasterxml.jackson.core.json.TestRootValues::testBrokenNumber
    public void testBrokenNumber() throws Exception
    {
        _testBrokenNumber(false);
        _testBrokenNumber(true);
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
        
        
        JsonParser p = JSON_F.createParser(bytes.toByteArray());
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
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        JsonGenerator g = JSON_F.createGenerator(out);
        g.writeStartArray();
        g.writeRaw(VALUE);
        g.writeEndArray();
        g.close();

        final byte[] JSON = out.toByteArray();

        JsonParser jp = JSON_F.createParser(JSON);
        assertToken(JsonToken.START_ARRAY, jp.nextToken());
        assertToken(JsonToken.VALUE_STRING, jp.nextToken());
        String str = jp.getText();
        assertEquals(2, str.length());
        assertEquals((char) 0xD83D, str.charAt(0));
        assertEquals((char) 0xDE0C, str.charAt(1));
        assertToken(JsonToken.END_ARRAY, jp.nextToken());
        jp.close();
    }

// com.fasterxml.jackson.core.json.TestUtf8Generator::testFilteringWithEscapedChars
    public void testFilteringWithEscapedChars() throws Exception
    {
        final String SAMPLE_WITH_QUOTES = "\b\t\f\n\r\"foo\"\u0000";

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        @SuppressWarnings("resource")
        JsonGenerator g = JSON_F.createGenerator(out);

        FilteringGeneratorDelegate gen = new FilteringGeneratorDelegate(g,
                new JsonPointerBasedFilter("/escapes"),
                true, 
                false 
        );

        

        gen.writeStartObject();

        gen.writeFieldName("a");
        gen.writeNumber((int) 123);

        gen.writeFieldName("array");
        gen.writeStartArray();
        gen.writeNumber((short) 1);
        gen.writeNumber((short) 2);
        gen.writeEndArray();

        gen.writeFieldName("escapes");

        final byte[] raw = SAMPLE_WITH_QUOTES.toString().getBytes("UTF-8");
        gen.writeUTF8String(raw, 0, raw.length);

        gen.writeEndObject();
        gen.close();

        JsonParser p = JSON_F.createParser(out.toByteArray());

        assertToken(JsonToken.START_OBJECT, p.nextToken());
        assertToken(JsonToken.FIELD_NAME, p.nextToken());
        assertEquals("escapes", p.getCurrentName());

        assertToken(JsonToken.VALUE_STRING, p.nextToken());
        assertEquals(SAMPLE_WITH_QUOTES, p.getText());
        assertToken(JsonToken.END_OBJECT, p.nextToken());
        assertNull(p.nextToken());
        p.close();
    }

// com.fasterxml.jackson.core.json.TestWithTonsaSymbols::testStreamReaderParser
    public void testStreamReaderParser() throws Exception {
        _testWith(true);
    }

// com.fasterxml.jackson.core.json.TestWithTonsaSymbols::testReaderParser
    public void testReaderParser() throws Exception {
        _testWith(false);
    }

// com.fasterxml.jackson.core.json.async.AsyncBinaryParseTest::testRawAsRootValue
    public void testRawAsRootValue() throws IOException {
        _testBinaryAsRoot(JSON_F);
    }

// com.fasterxml.jackson.core.json.async.AsyncBinaryParseTest::testRawAsArray
    public void testRawAsArray() throws IOException {
        _testBinaryAsArray(JSON_F);
    }

// com.fasterxml.jackson.core.json.async.AsyncBinaryParseTest::testRawAsObject
    public void testRawAsObject() throws IOException {
        _testBinaryAsObject(JSON_F);
    }

// com.fasterxml.jackson.core.json.async.AsyncCharEscapingTest::testMissingLinefeedEscaping
    public void testMissingLinefeedEscaping() throws Exception
    {
        byte[] doc = _jsonDoc(aposToQuotes("['Linefeed: \n.']"));
        _testMissingLinefeedEscaping(doc, 0, 99);
        _testMissingLinefeedEscaping(doc, 0, 5);
        _testMissingLinefeedEscaping(doc, 0, 3);
        _testMissingLinefeedEscaping(doc, 0, 2);
        _testMissingLinefeedEscaping(doc, 0, 1);

        _testMissingLinefeedEscaping(doc, 1, 99);
        _testMissingLinefeedEscaping(doc, 1, 3);
        _testMissingLinefeedEscaping(doc, 1, 1);
    }

// com.fasterxml.jackson.core.json.async.AsyncCharEscapingTest::testSimpleEscaping
    public void testSimpleEscaping() throws Exception
    {
        _testSimpleEscaping(0, 99);
        _testSimpleEscaping(0, 5);
        _testSimpleEscaping(0, 3);
        _testSimpleEscaping(0, 2);
        _testSimpleEscaping(0, 1);

        _testSimpleEscaping(1, 99);
        _testSimpleEscaping(1, 3);
        _testSimpleEscaping(1, 1);
    }

// com.fasterxml.jackson.core.json.async.AsyncCharEscapingTest::test8DigitSequence
    public void test8DigitSequence() throws Exception
    {
        String DOC = "[\"\\u00411234\"]";
        AsyncReaderWrapper r = asyncForBytes(JSON_F, 1, _jsonDoc(DOC), 1);
        assertToken(JsonToken.START_ARRAY, r.nextToken());
        assertToken(JsonToken.VALUE_STRING, r.nextToken());
        assertEquals("A1234", r.currentText());
        r.close();
    }

// com.fasterxml.jackson.core.json.async.AsyncCommentParsingTest::testCommentsDisabled
    public void testCommentsDisabled() throws Exception
    {
        _testDisabled(DOC_WITH_SLASHSTAR_COMMENT);
        _testDisabled(DOC_WITH_SLASHSLASH_COMMENT);
    }

// com.fasterxml.jackson.core.json.async.AsyncCommentParsingTest::testCommentsEnabled
    public void testCommentsEnabled() throws Exception
    {
        _testEnabled(DOC_WITH_SLASHSTAR_COMMENT, 99);
        _testEnabled(DOC_WITH_SLASHSTAR_COMMENT, 3);
        _testEnabled(DOC_WITH_SLASHSTAR_COMMENT, 1);

        _testEnabled(DOC_WITH_SLASHSLASH_COMMENT, 99);
        _testEnabled(DOC_WITH_SLASHSLASH_COMMENT, 3);
        _testEnabled(DOC_WITH_SLASHSLASH_COMMENT, 1);
    }

// com.fasterxml.jackson.core.json.async.AsyncCommentParsingTest::testCCommentsWithUTF8
    public void testCCommentsWithUTF8() throws Exception
    {
        final String JSON = "\n [ \"bar? \u00a9\" ]\n";

        _testWithUTF8Chars(JSON, 99);
        _testWithUTF8Chars(JSON, 5);
        _testWithUTF8Chars(JSON, 3);
        _testWithUTF8Chars(JSON, 2);
        _testWithUTF8Chars(JSON, 1);
    }

// com.fasterxml.jackson.core.json.async.AsyncCommentParsingTest::testYAMLCommentsEnabled
    public void testYAMLCommentsEnabled() throws Exception
    {
        JsonFactory f = new JsonFactory();
        f.enable(JsonParser.Feature.ALLOW_YAML_COMMENTS);

        _testYAMLComments(f, 99);
        _testYAMLComments(f, 3);
        _testYAMLComments(f, 1);

        _testCommentsBeforePropValue(f, "# foo\n", 99);
        _testCommentsBeforePropValue(f, "# foo\n", 3);
        _testCommentsBeforePropValue(f, "# foo\n", 1);

        _testCommentsBetweenArrayValues(f, "# foo\n", 99);
        _testCommentsBetweenArrayValues(f, "# foo\n", 3);
        _testCommentsBetweenArrayValues(f, "# foo\n", 1);
    }

// com.fasterxml.jackson.core.json.async.AsyncCommentParsingTest::testCCommentsEnabled
    public void testCCommentsEnabled() throws Exception {
        JsonFactory f = new JsonFactory();
        f.enable(JsonParser.Feature.ALLOW_COMMENTS);
        final String COMMENT = "\n";
        _testCommentsBeforePropValue(f, COMMENT, 99);
        _testCommentsBeforePropValue(f, COMMENT, 3);
        _testCommentsBeforePropValue(f, COMMENT, 1);
    }

// com.fasterxml.jackson.core.json.async.AsyncCommentParsingTest::testCppCommentsEnabled
    public void testCppCommentsEnabled() throws Exception {
        JsonFactory f = new JsonFactory();
        f.enable(JsonParser.Feature.ALLOW_COMMENTS);
        final String COMMENT = "// foo\n";
        _testCommentsBeforePropValue(f, COMMENT, 99);
        _testCommentsBeforePropValue(f, COMMENT, 3);
        _testCommentsBeforePropValue(f, COMMENT, 1);
    }

// com.fasterxml.jackson.core.json.async.AsyncConcurrencyTest::testConcurrentAsync
    public void testConcurrentAsync() throws Exception
    {
        final int MAX_ROUNDS = 30;
        for (int i = 0; i < MAX_ROUNDS; ++i) {
            _testConcurrentAsyncOnce(i, MAX_ROUNDS);
        }
    }

// com.fasterxml.jackson.core.json.async.AsyncFieldNamesTest::testSimpleFieldNames
    public void testSimpleFieldNames() throws IOException
    {
        for (String name : new String[] { "", "a", "ab", "abc", "abcd",
                "abcd1", "abcd12", "abcd123", "abcd1234",
                "abcd1234a",  "abcd1234ab",  "abcd1234abc",  "abcd1234abcd",
                "abcd1234abcd1"
            }) {
            _testSimpleFieldName(name);
        }
    }

// com.fasterxml.jackson.core.json.async.AsyncFieldNamesTest::testEscapedFieldNames
    public void testEscapedFieldNames() throws IOException
    {
        _testEscapedFieldNames("\\'foo\\'", "'foo'");
        _testEscapedFieldNames("\\'foobar\\'", "'foobar'");
        _testEscapedFieldNames("\\'foo \\u0026 bar\\'", "'foo & bar'");
        _testEscapedFieldNames("Something \\'longer\\'?", "Something 'longer'?");
        _testEscapedFieldNames("\\u00A7", "\u00A7");
        _testEscapedFieldNames("\\u4567", "\u4567");
        _testEscapedFieldNames("Unicode: \\u00A7 and \\u4567?", "Unicode: \u00A7 and \u4567?");
    }

// com.fasterxml.jackson.core.json.async.AsyncInvalidCharsTest::testUtf8BOMHandling
    public void testUtf8BOMHandling() throws Exception
    {
        _testUtf8BOMHandling(0, 99);
        _testUtf8BOMHandling(0, 5);
        _testUtf8BOMHandling(0, 3);
        _testUtf8BOMHandling(0, 2);
        _testUtf8BOMHandling(0, 1);

        _testUtf8BOMHandling(2, 99);
        _testUtf8BOMHandling(2, 1);
    }

// com.fasterxml.jackson.core.json.async.AsyncInvalidCharsTest::testHandlingOfInvalidSpace
    public void testHandlingOfInvalidSpace() throws Exception
    {
        _testHandlingOfInvalidSpace(0, 99);
        _testHandlingOfInvalidSpace(0, 3);
        _testHandlingOfInvalidSpace(0, 1);

        _testHandlingOfInvalidSpace(1, 99);
        _testHandlingOfInvalidSpace(2, 1);
    }

// com.fasterxml.jackson.core.json.async.AsyncNonStdNumbersTest::testDisallowNaN
    public void testDisallowNaN() throws Exception
    {
        final String JSON = "[ NaN]";
        assertFalse(DEFAULT_F.isEnabled(JsonParser.Feature.ALLOW_NON_NUMERIC_NUMBERS));

        
        AsyncReaderWrapper p = createParser(DEFAULT_F, JSON, 1);
        assertToken(JsonToken.START_ARRAY, p.nextToken());
        try {
            p.nextToken();
            fail("Expected exception");
        } catch (Exception e) {
            verifyException(e, "non-standard");
        } finally {
            p.close();
        }
    }

// com.fasterxml.jackson.core.json.async.AsyncNonStdNumbersTest::testAllowNaN
    public void testAllowNaN() throws Exception
    {
        final String JSON = "[ NaN]";
        JsonFactory f = new JsonFactory();
        f.configure(JsonParser.Feature.ALLOW_NON_NUMERIC_NUMBERS, true);

        _testAllowNaN(f, JSON, 99);
        _testAllowNaN(f, JSON, 5);
        _testAllowNaN(f, JSON, 3);
        _testAllowNaN(f, JSON, 2);
        _testAllowNaN(f, JSON, 1);
    }

// com.fasterxml.jackson.core.json.async.AsyncNonStdNumbersTest::testDisallowInf
    public void testDisallowInf() throws Exception
    {
        
        _testDisallowInf(DEFAULT_F, "Infinity", 99);
        _testDisallowInf(DEFAULT_F, "Infinity", 1);
        _testDisallowInf(DEFAULT_F, "-Infinity", 99);
        _testDisallowInf(DEFAULT_F, "-Infinity", 1);
        
        _testDisallowInf(DEFAULT_F, "+Infinity", 99);
        _testDisallowInf(DEFAULT_F, "+Infinity", 1);

        

        
        

    }

// com.fasterxml.jackson.core.json.async.AsyncNonStdNumbersTest::testAllowInf
    public void testAllowInf() throws Exception
    {
        JsonFactory f = new JsonFactory();
        f.configure(JsonParser.Feature.ALLOW_NON_NUMERIC_NUMBERS, true);

        String JSON = "[ Infinity, +Infinity, -Infinity ]";
        _testAllowInf(f, JSON, 99);
        _testAllowInf(f, JSON, 5);
        _testAllowInf(f, JSON, 3);
        _testAllowInf(f, JSON, 2);
        _testAllowInf(f, JSON, 1);

        JSON = "[Infinity,+Infinity,-Infinity]";
        _testAllowInf(f, JSON, 99);
        _testAllowInf(f, JSON, 1);

        JSON = "[Infinity  ,   +Infinity   ,   -Infinity]";
        _testAllowInf(f, JSON, 99);
        _testAllowInf(f, JSON, 1);
    }

// com.fasterxml.jackson.core.json.async.AsyncNonStdParsingTest::testLargeUnquotedNames
    public void testLargeUnquotedNames() throws Exception
    {
        JsonFactory f = new JsonFactory();
        f.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);

        StringBuilder sb = new StringBuilder(5000);
        sb.append("[\n");
        final int REPS = 1050;
        for (int i = 0; i < REPS; ++i) {
            if (i > 0) {
                sb.append(',');
                if ((i & 7) == 0) {
                    sb.append('\n');
                }
            }
            sb.append("{");
            sb.append("abc").append(i&127).append(':');
            sb.append((i & 1) != 0);
            sb.append("}\n");
        }
        sb.append("]");
        String doc = sb.toString();

        _testLargeUnquoted(f, REPS, doc, 0, 99);
        _testLargeUnquoted(f, REPS, doc, 0, 5);
        _testLargeUnquoted(f, REPS, doc, 0, 3);
        _testLargeUnquoted(f, REPS, doc, 0, 2);
        _testLargeUnquoted(f, REPS, doc, 0, 1);

        _testLargeUnquoted(f, REPS, doc, 1, 99);
        _testLargeUnquoted(f, REPS, doc, 1, 1);
    }

// com.fasterxml.jackson.core.json.async.AsyncNonStdParsingTest::testSimpleUnquotedNames
    public void testSimpleUnquotedNames() throws Exception
    {
        final JsonFactory f = new JsonFactory();
        f.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);

        _testSimpleUnquoted(f, 0, 99);
        _testSimpleUnquoted(f, 0, 5);
        _testSimpleUnquoted(f, 0, 3);
        _testSimpleUnquoted(f, 0, 2);
        _testSimpleUnquoted(f, 0, 1);

        _testSimpleUnquoted(f, 1, 99);
        _testSimpleUnquoted(f, 1, 3);
        _testSimpleUnquoted(f, 1, 1);
    }

// com.fasterxml.jackson.core.json.async.AsyncNonStdParsingTest::testAposQuotingDisabled
    public void testAposQuotingDisabled() throws Exception
    {
        JsonFactory f = new JsonFactory();
        _testSingleQuotesDefault(f, 0, 99);
        _testSingleQuotesDefault(f, 0, 5);
        _testSingleQuotesDefault(f, 0, 3);
        _testSingleQuotesDefault(f, 0, 1);

        _testSingleQuotesDefault(f, 1, 99);
        _testSingleQuotesDefault(f, 1, 1);
    }

// com.fasterxml.jackson.core.json.async.AsyncNonStdParsingTest::testAposQuotingEnabled
    public void testAposQuotingEnabled() throws Exception
    {
        JsonFactory f = new JsonFactory();
        f.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);

        _testAposQuotingEnabled(f, 0, 99);
        _testAposQuotingEnabled(f, 0, 5);
        _testAposQuotingEnabled(f, 0, 3);
        _testAposQuotingEnabled(f, 0, 2);
        _testAposQuotingEnabled(f, 0, 1);

        _testAposQuotingEnabled(f, 1, 99);
        _testAposQuotingEnabled(f, 2, 1);
        _testAposQuotingEnabled(f, 1, 1);
    }

// com.fasterxml.jackson.core.json.async.AsyncNonStdParsingTest::testSingleQuotesEscaped
    public void testSingleQuotesEscaped() throws Exception
    {
        JsonFactory f = new JsonFactory();
        f.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);

        _testSingleQuotesEscaped(f, 0, 99);
        _testSingleQuotesEscaped(f, 0, 5);
        _testSingleQuotesEscaped(f, 0, 3);
        _testSingleQuotesEscaped(f, 0, 1);

        _testSingleQuotesEscaped(f, 1, 99);
        _testSingleQuotesEscaped(f, 1, 1);
    }

// com.fasterxml.jackson.core.json.async.AsyncNonStdParsingTest::testNonStandardNameChars
    public void testNonStandardNameChars() throws Exception
    {
        JsonFactory f = new JsonFactory();
        f.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);

        _testNonStandardNameChars(f, 0, 99);
        _testNonStandardNameChars(f, 0, 6);
        _testNonStandardNameChars(f, 0, 3);
        _testNonStandardNameChars(f, 0, 1);

        _testNonStandardNameChars(f, 1, 99);
        _testNonStandardNameChars(f, 2, 1);
    }

// com.fasterxml.jackson.core.json.async.AsyncNonStdParsingTest::testNonStandarBackslashQuotingForValues
    public void testNonStandarBackslashQuotingForValues(int mode) throws Exception
    {
        _testNonStandarBackslashQuoting(0, 99);
        _testNonStandarBackslashQuoting(0, 6);
        _testNonStandarBackslashQuoting(0, 3);
        _testNonStandarBackslashQuoting(0, 1);

        _testNonStandarBackslashQuoting(2, 99);
        _testNonStandarBackslashQuoting(1, 1);
    }

// com.fasterxml.jackson.core.json.async.AsyncNumberCoercionTest::testToIntCoercion
    public void testToIntCoercion() throws Exception
    {
        AsyncReaderWrapper p;

        
        p = createParser("1");
        assertToken(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertEquals(1L, p.getLongValue());
        assertEquals(1, p.getIntValue());
        p.close();

        
        p = createParser("10");
        assertToken(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertEquals(BigInteger.TEN, p.getBigIntegerValue());
        assertEquals(10, p.getIntValue());
        p.close();

        
        p = createParser("2");
        assertToken(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertEquals(2.0, p.getDoubleValue());
        assertEquals(2, p.getIntValue());
        p.close();

        p = createParser("0.1");
        assertToken(JsonToken.VALUE_NUMBER_FLOAT, p.nextToken());
        assertEquals(0.1, p.getDoubleValue());
        assertEquals(0, p.getIntValue());
        p.close();
        
        
        p = createParser("10");
        assertToken(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertEquals(BigDecimal.TEN, p.getDecimalValue());
        assertEquals(10, p.getIntValue());
        p.close();
    }

// com.fasterxml.jackson.core.json.async.AsyncNumberCoercionTest::testToIntFailing
    public void testToIntFailing() throws Exception
    {
        AsyncReaderWrapper p;

        
        long big = 1L + Integer.MAX_VALUE;
        p = createParser(String.valueOf(big));
        assertToken(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertEquals(big, p.getLongValue());
        try {
            p.getIntValue();
            fail("Should not pass");
        } catch (JsonParseException e) {
            verifyException(e, "out of range of int");
        }
        long small = -1L + Integer.MIN_VALUE;
        p = createParser(String.valueOf(small));
        assertToken(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertEquals(Long.valueOf(small), p.getNumberValue());
        assertEquals(small, p.getLongValue());
        try {
            p.getIntValue();
            fail("Should not pass");
        } catch (JsonParseException e) {
            verifyException(e, "out of range of int");
        }

        
        p = createParser(String.valueOf(big)+".0");
        assertToken(JsonToken.VALUE_NUMBER_FLOAT, p.nextToken());
        assertEquals((double) big, p.getDoubleValue());
        try {
            p.getIntValue();
            fail("Should not pass");
        } catch (JsonParseException e) {
            verifyException(e, "out of range of int");
        }
        p = createParser(String.valueOf(small)+".0");
        assertToken(JsonToken.VALUE_NUMBER_FLOAT, p.nextToken());
        assertEquals((double) small, p.getDoubleValue());
        try {
            p.getIntValue();
            fail("Should not pass");
        } catch (JsonParseException e) {
            verifyException(e, "out of range of int");
        }

        
        p = createParser(String.valueOf(big));
        assertToken(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertEquals(BigInteger.valueOf(big), p.getBigIntegerValue());
        try {
            p.getIntValue();
            fail("Should not pass");
        } catch (JsonParseException e) {
            verifyException(e, "out of range of int");
        }
        p = createParser(String.valueOf(small));
        assertToken(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertEquals(BigInteger.valueOf(small), p.getBigIntegerValue());
        try {
            p.getIntValue();
            fail("Should not pass");
        } catch (JsonParseException e) {
            verifyException(e, "out of range of int");
        }
    }

// com.fasterxml.jackson.core.json.async.AsyncNumberCoercionTest::testToLongCoercion
    public void testToLongCoercion() throws Exception
    {
        AsyncReaderWrapper p;

        
        p = createParser("1");
        assertToken(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertEquals(1, p.getIntValue());
        assertEquals(1L, p.getLongValue());
        p.close();

        
        long biggish = 12345678901L;
        p = createParser(String.valueOf(biggish));
        assertToken(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertEquals(BigInteger.valueOf(biggish), p.getBigIntegerValue());
        assertEquals(biggish, p.getLongValue());
        p.close();

        
        p = createParser("2");
        assertToken(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertEquals(2.0, p.getDoubleValue());
        assertEquals(2L, p.getLongValue());
        p.close();

        
        p = createParser("10");
        assertToken(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertEquals(BigDecimal.TEN, p.getDecimalValue());
        assertEquals(10, p.getLongValue());
        p.close();
    }

// com.fasterxml.jackson.core.json.async.AsyncNumberCoercionTest::testToLongFailing
    public void testToLongFailing() throws Exception
    {
        AsyncReaderWrapper p;

        
        BigInteger big = BigInteger.valueOf(Long.MAX_VALUE).add(BigInteger.TEN);
        p = createParser(String.valueOf(big));
        assertToken(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertEquals(NumberType.BIG_INTEGER, p.getNumberType());
        assertEquals(big, p.getBigIntegerValue());
        assertEquals(big, p.getNumberValue());
        try {
            p.getLongValue();
            fail("Should not pass");
        } catch (JsonParseException e) {
            verifyException(e, "out of range of long");
        }
        BigInteger small = BigInteger.valueOf(Long.MIN_VALUE).subtract(BigInteger.TEN);
        p = createParser(String.valueOf(small));
        assertToken(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertEquals(small, p.getBigIntegerValue());
        try {
            p.getLongValue();
            fail("Should not pass");
        } catch (JsonParseException e) {
            verifyException(e, "out of range of long");
        }
    }

// com.fasterxml.jackson.core.json.async.AsyncNumberCoercionTest::testToBigIntegerCoercion
    public void testToBigIntegerCoercion() throws Exception
    {
        AsyncReaderWrapper p;

        p = createParser("1");
        assertToken(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        
        assertEquals(1, p.getIntValue());
        assertEquals(BigInteger.ONE, p.getBigIntegerValue());
        p.close();

        p = createParser("2.0");
        assertToken(JsonToken.VALUE_NUMBER_FLOAT, p.nextToken());
        
        assertEquals(2.0, p.getDoubleValue());
        assertEquals(BigInteger.valueOf(2L), p.getBigIntegerValue());
        p.close();
        
        p = createParser(String.valueOf(Long.MAX_VALUE));
        assertToken(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        
        assertEquals(Long.MAX_VALUE, p.getLongValue());
        assertEquals(BigInteger.valueOf(Long.MAX_VALUE), p.getBigIntegerValue());
        p.close();

        p = createParser(" 200.0");
        assertToken(JsonToken.VALUE_NUMBER_FLOAT, p.nextToken());
        
        assertEquals(new BigDecimal("200.0"), p.getDecimalValue());
        assertEquals(BigInteger.valueOf(200L), p.getBigIntegerValue());
        p.close();
    }

// com.fasterxml.jackson.core.json.async.AsyncNumberCoercionTest::testToDoubleCoercion
    public void testToDoubleCoercion() throws Exception
    {
        AsyncReaderWrapper p;

        
        p = createParser("100.5");
        assertToken(JsonToken.VALUE_NUMBER_FLOAT, p.nextToken());
        assertEquals(new BigDecimal("100.5"), p.getDecimalValue());
        assertEquals(100.5, p.getDoubleValue());
        p.close();

        p = createParser("10");
        assertToken(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertEquals(BigInteger.TEN, p.getBigIntegerValue());
        assertEquals(10.0, p.getDoubleValue());
        p.close();
    }

// com.fasterxml.jackson.core.json.async.AsyncNumberCoercionTest::testToBigDecimalCoercion
    public void testToBigDecimalCoercion() throws Exception
    {
        AsyncReaderWrapper p;

        p = createParser("1");
        assertToken(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        
        assertEquals(1, p.getIntValue());
        assertEquals(BigDecimal.ONE, p.getDecimalValue());
        p.close();

        p = createParser(String.valueOf(Long.MAX_VALUE));
        assertToken(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        
        assertEquals(Long.MAX_VALUE, p.getLongValue());
        assertEquals(BigDecimal.valueOf(Long.MAX_VALUE), p.getDecimalValue());
        p.close();

        BigInteger biggie = BigInteger.valueOf(Long.MAX_VALUE).multiply(BigInteger.TEN);
        p = createParser(String.valueOf(biggie));
        assertToken(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        
        assertEquals(biggie, p.getBigIntegerValue());
        assertEquals(new BigDecimal(biggie), p.getDecimalValue());
        p.close();
    }

// com.fasterxml.jackson.core.json.async.AsyncNumberLeadingZeroesTest::testLeadingZeroesInt
    public void testLeadingZeroesInt() throws Exception
    {
        _testLeadingZeroesInt("00003", 3);
        _testLeadingZeroesInt("00003 ", 3);
        _testLeadingZeroesInt(" 00003", 3);

        _testLeadingZeroesInt("-00007", -7);
        _testLeadingZeroesInt("-00007 ", -7);
        _testLeadingZeroesInt(" -00007", -7);

        _testLeadingZeroesInt("056", 56);
        _testLeadingZeroesInt("056 ", 56);
        _testLeadingZeroesInt(" 056", 56);

        _testLeadingZeroesInt("-04", -4);
        _testLeadingZeroesInt("-04  ", -4);
        _testLeadingZeroesInt(" -04", -4);

        _testLeadingZeroesInt("0"+Integer.MAX_VALUE, Integer.MAX_VALUE);
        _testLeadingZeroesInt(" 0"+Integer.MAX_VALUE, Integer.MAX_VALUE);
        _testLeadingZeroesInt("0"+Integer.MAX_VALUE+" ", Integer.MAX_VALUE);
    }

// com.fasterxml.jackson.core.json.async.AsyncNumberLeadingZeroesTest::testLeadingZeroesFloat
    public void testLeadingZeroesFloat() throws Exception
    {
        _testLeadingZeroesFloat("00.25", 0.25);
        _testLeadingZeroesFloat("  00.25", 0.25);
        _testLeadingZeroesFloat("00.25  ", 0.25);

        _testLeadingZeroesFloat("-000.5", -0.5);
        _testLeadingZeroesFloat("  -000.5", -0.5);
        _testLeadingZeroesFloat("-000.5  ", -0.5);
    }

// com.fasterxml.jackson.core.json.async.AsyncParserNamesTest::testLongNames
    public void testLongNames() throws IOException
    {
        _testWithName(generateName(5000));
    }

// com.fasterxml.jackson.core.json.async.AsyncParserNamesTest::testEvenLongerName
    public void testEvenLongerName() throws Exception
    {
        StringBuilder nameBuf = new StringBuilder("longString");
        int minLength = 9000;
        for (int i = 1; nameBuf.length() < minLength; ++i) {
            nameBuf.append("." + i);
        }
        String name = nameBuf.toString();
        _testWithName(name);
    }

// com.fasterxml.jackson.core.json.async.AsyncParserNamesTest::testSymbolTable
    public void testSymbolTable() throws IOException
    {
        final String STR1 = "a";

        byte[] doc = _jsonDoc("{ "+quote(STR1)+":1, \"foobar\":2, \"longername\":3 }");
        JsonFactory f = JSON_F;
        AsyncReaderWrapper p = asyncForBytes(f, 5, doc, 0);
        final ByteQuadsCanonicalizer symbols1 = ((NonBlockingJsonParserBase) p.parser()).symbolTableForTests();
        assertEquals(0, symbols1.size());
        assertEquals(JsonToken.START_OBJECT, p.nextToken());
        assertEquals(JsonToken.FIELD_NAME, p.nextToken());
        
        assertSame(STR1, p.currentName());
        assertEquals(1, symbols1.size());
        assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertEquals(JsonToken.FIELD_NAME, p.nextToken());
        assertSame("foobar", p.currentName());
        assertEquals(2, symbols1.size());
        assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertEquals(JsonToken.FIELD_NAME, p.nextToken());
        assertSame("longername", p.currentName());
        assertEquals(3, symbols1.size());
        assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertEquals(JsonToken.END_OBJECT, p.nextToken());
        assertNull(p.nextToken());
        assertEquals(3, symbols1.size());
        p.close();

        
        p = asyncForBytes(f, 5, doc, 0);

        final ByteQuadsCanonicalizer symbols2 = ((NonBlockingJsonParserBase) p.parser()).symbolTableForTests();
        
        assertNotSame(symbols1, symbols2);
        assertEquals(3, symbols2.size());

        assertEquals(JsonToken.START_OBJECT, p.nextToken());
        assertEquals(JsonToken.FIELD_NAME, p.nextToken());
        
        assertSame(STR1, p.currentName());
        assertEquals(3, symbols2.size());
        assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertEquals(JsonToken.FIELD_NAME, p.nextToken());
        assertSame("foobar", p.currentName());
        assertEquals(3, symbols2.size());
        assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertEquals(JsonToken.FIELD_NAME, p.nextToken());
        assertSame("longername", p.currentName());
        assertEquals(3, symbols2.size());
        assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertEquals(JsonToken.END_OBJECT, p.nextToken());
        assertNull(p.nextToken());
        assertEquals(3, symbols2.size());
        p.close();

        assertEquals(3, symbols2.size());
        p.close();
    }

// com.fasterxml.jackson.core.json.async.AsyncRootNumbersTest::testRootInts
    public void testRootInts() throws Exception {
        _testRootInts("10", 10);
        _testRootInts(" 10", 10);
        _testRootInts("10   ", 10);

        _testRootInts("0", 0);
        _testRootInts("    0", 0);
        _testRootInts("0 ", 0);

        _testRootInts("-1234", -1234);
        _testRootInts("  -1234", -1234);
        _testRootInts(" -1234  ", -1234);
    }

// com.fasterxml.jackson.core.json.async.AsyncRootNumbersTest::testRootDoublesSimple
    public void testRootDoublesSimple() throws Exception {
        _testRootDoubles("10.0", 10.0);
        _testRootDoubles(" 10.0", 10.0);
        _testRootDoubles("10.0   ", 10.0);

        _testRootDoubles("-1234.25", -1234.25);
        _testRootDoubles("  -1234.25", -1234.25);
        _testRootDoubles(" -1234.25  ", -1234.25);

        _testRootDoubles("0.25", 0.25);
        _testRootDoubles(" 0.25", 0.25);
        _testRootDoubles("0.25   ", 0.25);
    }

// com.fasterxml.jackson.core.json.async.AsyncRootNumbersTest::testRootDoublesScientific
    public void testRootDoublesScientific() throws Exception
    {
        _testRootDoubles("9e3", 9e3);
        _testRootDoubles("  9e3", 9e3);
        _testRootDoubles("9e3  ", 9e3);

        _testRootDoubles("9e-2", 9e-2);
        _testRootDoubles("  9e-2", 9e-2);
        _testRootDoubles("9e-2  ", 9e-2);
        
        _testRootDoubles("-12.5e3", -12.5e3);
        _testRootDoubles("  -12.5e3", -12.5e3);
        _testRootDoubles(" -12.5e3  ", -12.5e3);

        _testRootDoubles("-12.5E3", -12.5e3);
        _testRootDoubles("  -12.5E3", -12.5e3);
        _testRootDoubles("-12.5E3  ", -12.5e3);

        _testRootDoubles("-12.5E-2", -12.5e-2);
        _testRootDoubles("  -12.5E-2", -12.5e-2);
        _testRootDoubles(" -12.5E-2  ", -12.5e-2);

        _testRootDoubles("0e-05", 0e-5);
        _testRootDoubles("0e-5  ", 0e-5);
        _testRootDoubles("  0e-5", 0e-5);

        _testRootDoubles("0e1", 0e1);
        _testRootDoubles("0e1  ", 0e1);
        _testRootDoubles("  0e1", 0e1);
    }

// com.fasterxml.jackson.core.json.async.AsyncRootValuesTest::testTokenRootTokens
    public void testTokenRootTokens() throws Exception {
        _testTokenRootTokens(JsonToken.VALUE_TRUE, "true");
        _testTokenRootTokens(JsonToken.VALUE_FALSE, "false");
        _testTokenRootTokens(JsonToken.VALUE_NULL, "null");

        _testTokenRootTokens(JsonToken.VALUE_TRUE, "true  ");
        _testTokenRootTokens(JsonToken.VALUE_FALSE, "false  ");
        _testTokenRootTokens(JsonToken.VALUE_NULL, "null  ");
    }

// com.fasterxml.jackson.core.json.async.AsyncRootValuesTest::testTokenRootSequence
    public void testTokenRootSequence() throws Exception
    {
        byte[] input = _jsonDoc("\n[ true, false,\nnull  ,null\n,true,false]");

        JsonFactory f = JSON_F;
        _testTokenRootSequence(f, input, 0, 900);
        _testTokenRootSequence(f, input, 0, 3);
        _testTokenRootSequence(f, input, 0, 1);

        _testTokenRootSequence(f, input, 1, 900);
        _testTokenRootSequence(f, input, 1, 3);
        _testTokenRootSequence(f, input, 1, 1);
    }

// com.fasterxml.jackson.core.json.async.AsyncRootValuesTest::testMixedRootSequence
    public void testMixedRootSequence() throws Exception
    {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream(100);

        
        bytes.write(_jsonDoc("{ \"a\" : 4 }"));
        bytes.write(_jsonDoc("[ 12, -987,false ]"));
        bytes.write(_jsonDoc(" 12356"));
        bytes.write(_jsonDoc(" true"));
        byte[] input = bytes.toByteArray();

        JsonFactory f = JSON_F;
        _testMixedRootSequence(f, input, 0, 100);
        _testMixedRootSequence(f, input, 0, 3);
        _testMixedRootSequence(f, input, 0, 1);

        _testMixedRootSequence(f, input, 1, 100);
        _testMixedRootSequence(f, input, 1, 3);
        _testMixedRootSequence(f, input, 1, 1);
    }

// com.fasterxml.jackson.core.json.async.AsyncScalarArrayTest::testTokens
    public void testTokens() throws IOException
    {
        byte[] data = _jsonDoc("  [ true, false  ,true   , null,false , null]");
        JsonFactory f = JSON_F;

        
        _testTokens(f, data, 0, 100);
        _testTokens(f, data, 0, 5);
        _testTokens(f, data, 0, 3);
        _testTokens(f, data, 0, 2);
        _testTokens(f, data, 0, 1);

        
        _testTokens(f, data, 1, 100);
        _testTokens(f, data, 1, 3);
        _testTokens(f, data, 1, 1);
    }

// com.fasterxml.jackson.core.json.async.AsyncScalarArrayTest::testInts
    public void testInts() throws IOException
    {
        final int[] input = new int[] { 1, -1, 16, -17, 0, 131, -0, -155, 1000, -3000, 0xFFFF, -99999,
                Integer.MAX_VALUE, 0, Integer.MIN_VALUE };
        StringBuilder sb = new StringBuilder().append("[");
        for (int i = 0; i < input.length; ++i) {
            if (i > 0) sb.append(',');
            sb.append(input[i]);
        }
        byte[] data = _jsonDoc(sb.append(']').toString());
        JsonFactory f = JSON_F;
        _testInts(f, input, data, 0, 100);
        _testInts(f, input, data, 0, 3);
        _testInts(f, input, data, 0, 1);

        _testInts(f, input, data, 1, 100);
        _testInts(f, input, data, 1, 3);
        _testInts(f, input, data, 1, 1);
    }

// com.fasterxml.jackson.core.json.async.AsyncScalarArrayTest::testLong
    public void testLong() throws IOException
    {
        final long[] input = new long[] {
                

                -1L + Integer.MIN_VALUE, 1L + Integer.MAX_VALUE,
                19L * Integer.MIN_VALUE, 27L * Integer.MAX_VALUE,
                Long.MIN_VALUE, Long.MAX_VALUE };
        ByteArrayOutputStream bytes = new ByteArrayOutputStream(100);
        JsonFactory f = JSON_F;
        JsonGenerator g = f.createGenerator(bytes);
        g.writeStartArray();
        for (int i = 0; i < input.length; ++i) {
            g.writeNumber(input[i]);
        }
        g.writeEndArray();
        g.close();
        byte[] data = bytes.toByteArray();
        _testLong(f, input, data, 0, 100);
        _testLong(f, input, data, 0, 3);
        _testLong(f, input, data, 0, 1);

        _testLong(f, input, data, 1, 100);
        _testLong(f, input, data, 1, 3);
        _testLong(f, input, data, 1, 1);
    }

// com.fasterxml.jackson.core.json.async.AsyncScalarArrayTest::testFloats
    public void testFloats() throws IOException
    {
        final float[] input = new float[] { 0.0f, 0.25f, -0.5f, 10000.125f, - 99999.075f };
        ByteArrayOutputStream bytes = new ByteArrayOutputStream(100);
        JsonFactory f = JSON_F;
        JsonGenerator g = f.createGenerator(bytes);
        g.writeStartArray();
        for (int i = 0; i < input.length; ++i) {
            g.writeNumber(input[i]);
        }
        g.writeEndArray();
        g.close();
        byte[] data = bytes.toByteArray();
        _testFloats(f, input, data, 0, 100);
        _testFloats(f, input, data, 0, 3);
        _testFloats(f, input, data, 0, 1);

        _testFloats(f, input, data, 1, 100);
        _testFloats(f, input, data, 1, 3);
        _testFloats(f, input, data, 1, 1);
    }

// com.fasterxml.jackson.core.json.async.AsyncScalarArrayTest::testDoubles
    public void testDoubles() throws IOException
    {
        final double[] input = new double[] { 0.0, 0.25, -0.5, 10000.125,
                -99999.075 };
        ByteArrayOutputStream bytes = new ByteArrayOutputStream(100);
        JsonFactory f = JSON_F;
        JsonGenerator g = f.createGenerator(bytes);
        g.writeStartArray();
        for (int i = 0; i < input.length; ++i) {
            g.writeNumber(input[i]);
        }
        g.writeEndArray();
        g.close();
        byte[] data = bytes.toByteArray();
        _testDoubles(f, input, data, 0, 99);
        _testDoubles(f, input, data, 0, 3);
        _testDoubles(f, input, data, 0, 1);

        _testDoubles(f, input, data, 1, 99);
        _testDoubles(f, input, data, 1, 3);
        _testDoubles(f, input, data, 1, 1);
    }

// com.fasterxml.jackson.core.json.async.AsyncScalarArrayTest::testBigIntegers
    public void testBigIntegers() throws IOException
    {
        BigInteger bigBase = BigInteger.valueOf(Long.MAX_VALUE);
        final BigInteger[] input = new BigInteger[] {
                
                
                
                bigBase.shiftLeft(100).add(BigInteger.valueOf(123456789L)),
                bigBase.add(bigBase),
                bigBase.multiply(BigInteger.valueOf(17)),
                bigBase.negate().subtract(BigInteger.TEN)
        };
        ByteArrayOutputStream bytes = new ByteArrayOutputStream(100);
        JsonFactory f = JSON_F;
        JsonGenerator g = f.createGenerator(bytes);
        g.writeStartArray();
        for (int i = 0; i < input.length; ++i) {
            g.writeNumber(input[i]);
        }
        g.writeEndArray();
        g.close();
        byte[] data = bytes.toByteArray();
        _testBigIntegers(f, input, data, 0, 100);
        _testBigIntegers(f, input, data, 0, 3);
        _testBigIntegers(f, input, data, 0, 1);

        _testBigIntegers(f, input, data, 1, 100);
        _testBigIntegers(f, input, data, 2, 3);
        _testBigIntegers(f, input, data, 3, 1);
    }

// com.fasterxml.jackson.core.json.async.AsyncScalarArrayTest::testBigDecimals
    public void testBigDecimals() throws IOException
    {
        BigDecimal bigBase = new BigDecimal("1234567890344656736.125");
        final BigDecimal[] input = new BigDecimal[] {
                

                BigDecimal.valueOf(-999.25),
                bigBase,
                bigBase.divide(new BigDecimal("5")),
                bigBase.add(bigBase),
                bigBase.multiply(new BigDecimal("1.23")),
                bigBase.negate()
        };
        ByteArrayOutputStream bytes = new ByteArrayOutputStream(100);
        JsonFactory f = JSON_F;
        JsonGenerator g = f.createGenerator(bytes);
        g.writeStartArray();
        for (int i = 0; i < input.length; ++i) {
            g.writeNumber(input[i]);
        }
        g.writeEndArray();
        g.close();
        byte[] data = bytes.toByteArray();

        _testBigDecimals(f, input, data, 0, 100);
        _testBigDecimals(f, input, data, 0, 3);
        _testBigDecimals(f, input, data, 0, 1);

        _testBigDecimals(f, input, data, 1, 100);
        _testBigDecimals(f, input, data, 2, 3);
        _testBigDecimals(f, input, data, 3, 1);
    }

// com.fasterxml.jackson.core.json.async.AsyncScopeMatchingTest::testUnclosedArray
    public void testUnclosedArray(int mode) throws Exception
    {
        AsyncReaderWrapper p = asyncForBytes(JSON_F, 3, _jsonDoc("[ 1, 2 "), 0);
        assertToken(JsonToken.START_ARRAY, p.nextToken());
        assertToken(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertToken(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertEquals(2, p.getIntValue());

        try {
            p.nextToken();
            fail("Expected an exception for unclosed ARRAY (mode: "+mode+")");
        } catch (JsonParseException pe) {
            verifyException(pe, "expected close marker for ARRAY");
        }
    }

// com.fasterxml.jackson.core.json.async.AsyncScopeMatchingTest::testUnclosedObject
    public void testUnclosedObject(int mode) throws Exception
    {
        AsyncReaderWrapper p = asyncForBytes(JSON_F, 3, _jsonDoc("{ \"key\" : 3  "), 0);
        assertToken(JsonToken.START_OBJECT, p.nextToken());
        assertToken(JsonToken.FIELD_NAME, p.nextToken());
        assertToken(JsonToken.VALUE_NUMBER_INT, p.nextToken());

        try {
            p.nextToken();
            fail("Expected an exception for unclosed OBJECT (mode: "+mode+")");
        } catch (JsonParseException pe) {
            verifyException(pe, "expected close marker for OBJECT");
        }
    }

// com.fasterxml.jackson.core.json.async.AsyncScopeMatchingTest::testEOFInName
    public void testEOFInName(int mode) throws Exception
    {
        final String JSON = "{ \"abcd";
        AsyncReaderWrapper p = asyncForBytes(JSON_F, 3, _jsonDoc(JSON), 0);
        assertToken(JsonToken.START_OBJECT, p.nextToken());
        try {
            p.nextToken();
            fail("Expected an exception for EOF");
        } catch (JsonParseException pe) {
            verifyException(pe, "Unexpected end-of-input");
        } catch (IOException ie) {
            
            if (mode == MODE_DATA_INPUT) {
                verifyException(ie, "end-of-input");
                return;
            }
        }
    }

// com.fasterxml.jackson.core.json.async.AsyncScopeMatchingTest::testMismatchArrayToObject
    public void testMismatchArrayToObject() throws Exception
    {
        final String JSON = "[ 1, 2 }";
        AsyncReaderWrapper p = asyncForBytes(JSON_F, 3, _jsonDoc(JSON), 0);
        assertToken(JsonToken.START_ARRAY, p.nextToken());
        assertToken(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertToken(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        try {
            p.nextToken();
            fail("Expected an exception for incorrectly closed ARRAY");
        } catch (JsonParseException pe) {
            verifyException(pe, "Unexpected close marker '}': expected ']'");
        }
        p.close();
    }

// com.fasterxml.jackson.core.json.async.AsyncScopeMatchingTest::testMismatchObjectToArray
    public void testMismatchObjectToArray() throws Exception
    {
        final String JSON = "{ ]";
        AsyncReaderWrapper p = asyncForBytes(JSON_F, 3, _jsonDoc(JSON), 0);

        assertToken(JsonToken.START_OBJECT, p.nextToken());
            
        try {
            p.nextToken();
            fail("Expected an exception for incorrectly closed OBJECT");
        } catch (JsonParseException pe) {
            verifyException(pe, "Unexpected close marker ']': expected '}'");
        }
        p.close();
    }

// com.fasterxml.jackson.core.json.async.AsyncScopeMatchingTest::testMisssingColon
    public void testMisssingColon(int mode) throws Exception
    {
        final String JSON = "{ \"a\" \"b\" }";
        AsyncReaderWrapper p = asyncForBytes(JSON_F, 3, _jsonDoc(JSON), 0);

        assertToken(JsonToken.START_OBJECT, p.nextToken());
        try {
            
            assertToken(JsonToken.FIELD_NAME, p.nextToken());
            p.nextToken();
            fail("Expected an exception for missing semicolon");
        } catch (JsonParseException pe) {
            verifyException(pe, "was expecting a colon");
        }
        p.close();
    }

// com.fasterxml.jackson.core.json.async.AsyncSimpleNestedTest::testStuffInObject
    public void testStuffInObject() throws Exception
    {
        byte[] data = _jsonDoc(aposToQuotes(
                "{'foobar':[1,2,-999],'emptyObject':{},'emptyArray':[], 'other':{'':null} }"));

        JsonFactory f = JSON_F;
        _testStuffInObject(f, data, 0, 100);
        _testStuffInObject(f, data, 0, 3);
        _testStuffInObject(f, data, 0, 1);

        _testStuffInObject(f, data, 1, 100);
        _testStuffInObject(f, data, 1, 3);
        _testStuffInObject(f, data, 1, 1);
    }

// com.fasterxml.jackson.core.json.async.AsyncSimpleNestedTest::testStuffInArray
    public void testStuffInArray() throws Exception
    {
        byte[] data = _jsonDoc(aposToQuotes("[true,{'moreStuff':0},[null],{'extraOrdinary':23}]"));
        JsonFactory f = JSON_F;

        _testStuffInArray(f, data, 0, 100);
        _testStuffInArray(f, data, 0, 3);
        _testStuffInArray(f, data, 0, 1);

        _testStuffInArray(f, data, 3, 100);
        _testStuffInArray(f, data, 3, 3);
        _testStuffInArray(f, data, 3, 1);
    }

// com.fasterxml.jackson.core.json.async.AsyncSimpleNestedTest::testStuffInArray2
    public void testStuffInArray2() throws Exception
    {
        byte[] data = _jsonDoc(aposToQuotes(String.format(
                "[{'%s':true},{'%s':false},{'%s':true},{'%s':false}]",
                SHORT_NAME, LONG_NAME, LONG_NAME, SHORT_NAME)));
        JsonFactory f = JSON_F;

        _testStuffInArray2(f, data, 0, 100);
        _testStuffInArray2(f, data, 0, 3);
        _testStuffInArray2(f, data, 0, 1);

        _testStuffInArray2(f, data, 3, 100);
        _testStuffInArray2(f, data, 3, 3);
        _testStuffInArray2(f, data, 3, 1);
    }

// com.fasterxml.jackson.core.json.async.AsyncSimpleNestedTest::testMismatchedArray
    public void testMismatchedArray() throws Exception
    {
        byte[] data = _jsonDoc(aposToQuotes("[  }"));

        JsonFactory f = JSON_F;
        _testMismatchedArray(f, data, 0, 99);
        _testMismatchedArray(f, data, 0, 3);
        _testMismatchedArray(f, data, 0, 2);
        _testMismatchedArray(f, data, 0, 1);

        _testMismatchedArray(f, data, 1, 3);
        _testMismatchedArray(f, data, 1, 1);
    }

// com.fasterxml.jackson.core.json.async.AsyncSimpleNestedTest::testMismatchedObject
    public void testMismatchedObject() throws Exception
    {
        byte[] data = _jsonDoc(aposToQuotes("{ ]"));

        JsonFactory f = JSON_F;
        _testMismatchedObject(f, data, 0, 99);
        _testMismatchedObject(f, data, 0, 3);
        _testMismatchedObject(f, data, 0, 2);
        _testMismatchedObject(f, data, 0, 1);

        _testMismatchedObject(f, data, 1, 3);
        _testMismatchedObject(f, data, 1, 1);
    }

// com.fasterxml.jackson.core.json.async.AsyncSimpleObjectTest::testBooleans
    public void testBooleans() throws IOException
    {
        final JsonFactory f = JSON_F;
        byte[] data = _jsonDoc(aposToQuotes(
"{ 'a':true, 'b':false, 'acdc':true, '"+UNICODE_SHORT_NAME+"':true, 'a1234567':false,"
+"'"+UNICODE_LONG_NAME+"':   true }"));
        
        _testBooleans(f, data, 0, 100);
        _testBooleans(f, data, 0, 3);
        _testBooleans(f, data, 0, 1);

        
        _testBooleans(f, data, 1, 100);
        _testBooleans(f, data, 1, 3);
        _testBooleans(f, data, 1, 1);
    }

// com.fasterxml.jackson.core.json.async.AsyncSimpleObjectTest::testNumbers
    public void testNumbers() throws IOException
    {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream(100);
        JsonFactory f = JSON_F;
        JsonGenerator g = f.createGenerator(bytes);
        g.writeStartObject();
        g.writeNumberField("i1", NUMBER_EXP_I);
        g.writeNumberField("doubley", NUMBER_EXP_D);
        g.writeFieldName("biggieDecimal");
        g.writeNumber(NUMBER_EXP_BD.toString());
        g.writeEndObject();
        g.close();
        byte[] data = bytes.toByteArray();

        
        _testNumbers(f, data, 0, 100);
        _testNumbers(f, data, 0, 5);
        _testNumbers(f, data, 0, 3);
        _testNumbers(f, data, 0, 2);
        _testNumbers(f, data, 0, 1);

        
        _testNumbers(f, data, 1, 100);
        _testNumbers(f, data, 1, 3);
        _testNumbers(f, data, 1, 1);
    }

// com.fasterxml.jackson.core.json.async.AsyncStringArrayTest::testShortAsciiStrings
    public void testShortAsciiStrings() throws IOException
    {

        final String[] input = new String[] {
                "Test", "", "1",
                
                String.format("%s%s%s%s%s%s",
                        str0to9,str0to9,str0to9,str0to9,str0to9,str0to9,str0to9),

                
                "Test", "124"
        };
        JsonFactory f = JSON_F;
        byte[] data = _stringDoc(f, input);

        
        _testStrings(f, input, data, 0, 100);
        _testStrings(f, input, data, 0, 3);
        _testStrings(f, input, data, 0, 1);

        
        _testStrings(f, input, data, 1, 100);
        _testStrings(f, input, data, 1, 3);
        _testStrings(f, input, data, 1, 1);
    }

// com.fasterxml.jackson.core.json.async.AsyncStringArrayTest::testShortUnicodeStrings
    public void testShortUnicodeStrings() throws IOException
    {
        final String repeat = "Test: "+UNICODE_2BYTES;
        final String[] input = new String[] {
                repeat, "",
                ""+UNICODE_3BYTES,
                ""+UNICODE_2BYTES,
                
                String.format("%s %c %s %c %s",
                        str0to9, UNICODE_3BYTES,
                        str0to9, UNICODE_2BYTES, str0to9),
                "Test", repeat,
                "!"
        };
        JsonFactory f = JSON_F;
        byte[] data = _stringDoc(f, input);

        
        _testStrings(f, input, data, 0, 100);
        _testStrings(f, input, data, 0, 3);
        _testStrings(f, input, data, 0, 1);

        
        _testStrings(f, input, data, 1, 100);
        _testStrings(f, input, data, 1, 3);
        _testStrings(f, input, data, 1, 1);
    }

// com.fasterxml.jackson.core.json.async.AsyncStringArrayTest::testLongAsciiStrings
    public void testLongAsciiStrings() throws IOException
    {
        final String[] input = new String[] {
                
                String.format("%s %s %s %s %s %s %s %s %s %s %s %s",
                        str0to9,str0to9,"...",str0to9,"/", str0to9,
                        str0to9,"",str0to9,str0to9,"...",str0to9),
                LONG_ASCII
        };
        JsonFactory f = JSON_F;
        byte[] data = _stringDoc(f, input);

        
        _testStrings(f, input, data, 0, 9000);
        _testStrings(f, input, data, 0, 1);
        _testStrings(f, input, data, 0, 3);

        
        _testStrings(f, input, data, 1, 9000);
        _testStrings(f, input, data, 1, 3);
        _testStrings(f, input, data, 1, 1);
    }

// com.fasterxml.jackson.core.json.async.AsyncStringArrayTest::testLongUnicodeStrings
    public void testLongUnicodeStrings() throws IOException
    {
        
        final String LONG = String.format("%s %s %s %s %s%s %s %s %s %s %s %s%c %s",
                str0to9,str0to9,UNICODE_2BYTES,str0to9,UNICODE_3BYTES,UNICODE_3BYTES, str0to9,
                str0to9,UNICODE_3BYTES,str0to9,str0to9,UNICODE_2BYTES,UNICODE_2BYTES,str0to9);

        final String[] input = new String[] {
                
                LONG,
                LONG + ".",
                LONG + "..",
                LONG + "..."
        };
        JsonFactory f = JSON_F;
        byte[] data = _stringDoc(f, input);

        
        _testStrings(f, input, data, 0, 9000);
        _testStrings(f, input, data, 0, 3);
        _testStrings(f, input, data, 0, 1);

        
        _testStrings(f, input, data, 1, 9000);
        _testStrings(f, input, data, 1, 3);
        _testStrings(f, input, data, 1, 1);
    }

// com.fasterxml.jackson.core.json.async.AsyncStringObjectTest::testBasicFieldsNames
    public void testBasicFieldsNames() throws IOException
    {
        final String json = aposToQuotes(String.format("{'%s':'%s','%s':'%s','%s':'%s'}",
            UNICODE_SHORT_NAME, UNICODE_LONG_NAME,
            UNICODE_LONG_NAME, UNICODE_SHORT_NAME,
            ASCII_SHORT_NAME, ASCII_SHORT_NAME));

        final JsonFactory f = JSON_F;

        byte[] data = _jsonDoc(json);
        _testBasicFieldsNames(f, data, 0, 100);
        _testBasicFieldsNames(f, data, 0, 3);
        _testBasicFieldsNames(f, data, 0, 1);

        _testBasicFieldsNames(f, data, 1, 100);
        _testBasicFieldsNames(f, data, 1, 3);
        _testBasicFieldsNames(f, data, 1, 1);
    }

// com.fasterxml.jackson.core.json.async.AsyncTokenFilterTest::testFilteredNonBlockingParserAllContent
    public void testFilteredNonBlockingParserAllContent() throws IOException
    {
        NonBlockingJsonParser nonBlockingParser = (NonBlockingJsonParser) JSON_F.createNonBlockingByteArrayParser();
        FilteringParserDelegate filteredParser = new FilteringParserDelegate(nonBlockingParser,
                TOKEN_FILTER, true, true);
        nonBlockingParser.feedInput(INPUT_BYTES, 0, INPUT_BYTES.length);
        int expectedIdx = 0;
        while (expectedIdx < EXPECTED_TOKENS.length) {
            
            JsonToken actual = filteredParser.nextToken();

            
            assertToken(EXPECTED_TOKENS[expectedIdx], actual);
            expectedIdx++;
        }

        filteredParser.close();
        nonBlockingParser.close();
    }

// com.fasterxml.jackson.core.json.async.AsyncTokenFilterTest::testSkipChildrenFailOnSplit
    public void testSkipChildrenFailOnSplit() throws IOException
    {
        NonBlockingJsonParser nbParser = (NonBlockingJsonParser) JSON_F.createNonBlockingByteArrayParser();
        FilteringParserDelegate filteredParser = new FilteringParserDelegate(nbParser,
                TOKEN_FILTER, true, true);
        nbParser.feedInput(INPUT_BYTES, 0, 5);

        assertToken(JsonToken.START_OBJECT, nbParser.nextToken());
        try {
            nbParser.skipChildren();
            fail("Should not pass!");
        } catch (JsonParseException e) {
            verifyException(e, "not enough content available");
            verifyException(e, "skipChildren()");
        }
        nbParser.close();
        filteredParser.close();
    }

// com.fasterxml.jackson.core.json.async.AsyncUnicodeHandlingTest::testShortUnicodeWithSurrogates
    public void testShortUnicodeWithSurrogates() throws IOException
    {
        JsonFactory f = JSON_F;

        
        _testUnicodeWithSurrogates(f, 28, 99);
        _testUnicodeWithSurrogates(f, 53, 99);

        
        _testUnicodeWithSurrogates(f, 28, 3);
        _testUnicodeWithSurrogates(f, 53, 5);

        
        _testUnicodeWithSurrogates(f, 28, 1);
        _testUnicodeWithSurrogates(f, 53, 1);
    }

// com.fasterxml.jackson.core.json.async.AsyncUnicodeHandlingTest::testLongUnicodeWithSurrogates
    public void testLongUnicodeWithSurrogates() throws IOException
    {
        JsonFactory f = JSON_F;

        _testUnicodeWithSurrogates(f, 230, Integer.MAX_VALUE);
        _testUnicodeWithSurrogates(f, 700, Integer.MAX_VALUE);
        _testUnicodeWithSurrogates(f, 9600, Integer.MAX_VALUE);

        _testUnicodeWithSurrogates(f, 230, 3);
        _testUnicodeWithSurrogates(f, 700, 3);
        _testUnicodeWithSurrogates(f, 9600, 3);

        _testUnicodeWithSurrogates(f, 230, 1);
        _testUnicodeWithSurrogates(f, 700, 1);
        _testUnicodeWithSurrogates(f, 9600, 1);
    }

// com.fasterxml.jackson.core.json.async.ConfigTest::testFactoryDefaults
    public void testFactoryDefaults() throws IOException
    {
        assertTrue(DEFAULT_F.canParseAsync());
    }

// com.fasterxml.jackson.core.json.async.ConfigTest::testAsyncParerDefaults
    public void testAsyncParerDefaults() throws IOException
    {
        byte[] data = _jsonDoc("[true,false]");
        AsyncReaderWrapper r = asyncForBytes(DEFAULT_F, 100, data, 0);
        JsonParser p = r.parser();

        assertTrue(p.canParseAsync());
        assertNull(p.getCodec());
        assertNull(p.getInputSource());
        assertEquals(-1, p.releaseBuffered(new StringWriter()));
        assertEquals(0, p.releaseBuffered(new ByteArrayOutputStream()));

        assertToken(JsonToken.START_ARRAY, r.nextToken());
        assertEquals(11, p.releaseBuffered(new ByteArrayOutputStream()));
        
        p.close();
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

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
// com.fasterxml.jackson.core.main.TestArrayParsing::testMissingValueAsNullByEnablingFeature
    public void testMissingValueAsNullByEnablingFeature() throws Exception
    {
    	_testMissingValueByEnablingFeature(true);
    	_testMissingValueByEnablingFeature(false);
    }

// com.fasterxml.jackson.core.main.TestArrayParsing::testMissingValueAsNullByNotEnablingFeature
    public void testMissingValueAsNullByNotEnablingFeature() throws Exception
    {
    	_testMissingValueNotEnablingFeature(true);
    	_testMissingValueNotEnablingFeature(false);
    }

// com.fasterxml.jackson.core.main.TestArrayParsing::testNotMissingValueByEnablingFeature
    public void testNotMissingValueByEnablingFeature() throws Exception
    {
    	_testNotMissingValueByEnablingFeature(true);
    	_testNotMissingValueByEnablingFeature(false);
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
        assertTrue("Should be in root, was "+ctxt.typeDesc(), ctxt.inRoot());
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
            verifyException(e, "Current context not Object");
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
        ByteOutputStreamForTesting output = new ByteOutputStreamForTesting();
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
        ByteOutputStreamForTesting output = new ByteOutputStreamForTesting();
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
        ByteOutputStreamForTesting output = new ByteOutputStreamForTesting();
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
        StringWriterForTesting sw = new StringWriterForTesting();
        JsonGenerator jg = f.createGenerator(sw);
        jg.writeStartArray();
        jg.writeEndArray();
        assertEquals(0, sw.flushCount);
        jg.flush();
        assertEquals(1, sw.flushCount);
        jg.close();
        
        
        ByteOutputStreamForTesting bytes = new ByteOutputStreamForTesting();
        jg = f.createGenerator(bytes, JsonEncoding.UTF8);
        jg.writeStartArray();
        jg.writeEndArray();
        assertEquals(0, bytes.flushCount);
        jg.flush();
        assertEquals(1, bytes.flushCount);
        assertEquals(2, bytes.toByteArray().length);
        jg.close();

        
        f.disable(JsonGenerator.Feature.FLUSH_PASSED_TO_STREAM);
        
        sw = new StringWriterForTesting();
        jg = f.createGenerator(sw);
        jg.writeStartArray();
        jg.writeEndArray();
        assertEquals(0, sw.flushCount);
        jg.flush();
        assertEquals(0, sw.flushCount);
        jg.close();
        assertEquals("[]", sw.toString());

        
        bytes = new ByteOutputStreamForTesting();
        jg = f.createGenerator(bytes, JsonEncoding.UTF8);
        jg.writeStartArray();
        jg.writeEndArray();
        assertEquals(0, bytes.flushCount);
        jg.flush();
        assertEquals(0, bytes.flushCount);
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
            
            assertToken(t, jp.currentToken());
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
        
        assertToken(JsonToken.VALUE_NUMBER_INT, jp.currentToken());
        assertEquals(123, jp.getIntValue());

        
        assertToken(JsonToken.START_ARRAY, jp.nextToken());
        gen.copyCurrentStructure(jp);
        
        assertToken(JsonToken.END_ARRAY, jp.currentToken());
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
        
        assertToken(JsonToken.END_OBJECT, jp.currentToken());
        jp.close();
        gen.close();

        assertEquals("{\"a\":1,\"b\":[{\"c\":null}]}", sw.toString());
    }

// com.fasterxml.jackson.core.main.TestGeneratorMisc::testIsClosed
    public void testIsClosed() throws IOException
    {
        for (int i = 0; i < 2; ++i) {
            boolean stream = ((i & 1) == 0);
            JsonGenerator jg = stream ?
                    JSON_F.createGenerator(new StringWriter())
                : JSON_F.createGenerator(new ByteArrayOutputStream(), JsonEncoding.UTF8)
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
        
        StringWriter sw = new StringWriter();
        JsonGenerator gen = JSON_F.createGenerator(sw);
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
        gen = JSON_F.createGenerator(sw);
        gen.writeStartArray();
        gen.writeObject(BigInteger.valueOf(1234));
        gen.writeObject(new BigDecimal(0.5));
        gen.writeEndArray();
        gen.close();
        act = sw.toString().trim();
        assertEquals("[1234,0.5]", act);

        
        sw = new StringWriter();
        gen = JSON_F.createGenerator(sw);
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
        StringWriter sw = new StringWriter();
        JsonGenerator gen = JSON_F.createGenerator(sw);
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
        StringWriter sw = new StringWriter();
        JsonGenerator gen = JSON_F.createGenerator(sw);
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

// com.fasterxml.jackson.core.main.TestGeneratorMisc::testLongerObjects
    public void testLongerObjects() throws Exception
    {
        _testLongerObjects(JSON_F, 0);
        _testLongerObjects(JSON_F, 1);
        _testLongerObjects(JSON_F, 2);
    }

// com.fasterxml.jackson.core.main.TestGeneratorMisc::testAsEmbedded
    public void testAsEmbedded() throws Exception
    {
        JsonGenerator g;

        StringWriter sw = new StringWriter();
        g = JSON_F.createGenerator(sw);
        g.writeEmbeddedObject(null);
        g.close();
        assertEquals("null", sw.toString());

        ByteArrayOutputStream bytes =  new ByteArrayOutputStream(100);
        g = JSON_F.createGenerator(bytes);
        g.writeEmbeddedObject(null);
        g.close();
        assertEquals("null", bytes.toString("UTF-8"));

        

        try {
            g = JSON_F.createGenerator(bytes);
            
            g.writeEmbeddedObject(getClass());
            fail("Expected an exception");
            g.close(); 
        } catch (JsonGenerationException e) {
            verifyException(e, "No native support for");
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
            verifyException(e, "Current context not Array");
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
    public void testDefaultSettings() throws Exception
    {
        JsonFactory f = new JsonFactory();
        assertTrue(f.isEnabled(JsonParser.Feature.AUTO_CLOSE_SOURCE));
        assertFalse(f.isEnabled(JsonParser.Feature.ALLOW_COMMENTS));
        assertFalse(f.isEnabled(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES));
        assertFalse(f.isEnabled(JsonParser.Feature.ALLOW_SINGLE_QUOTES));
        assertFalse(f.isEnabled(JsonParser.Feature.ALLOW_UNQUOTED_CONTROL_CHARS));

        JsonParser p = f.createParser(new StringReader("{}"));
        _testDefaultSettings(p);
        p.close();
        p = f.createParser(new ByteArrayInputStream("{}".getBytes("UTF-8")));
        _testDefaultSettings(p);
        p.close();
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

// com.fasterxml.jackson.core.main.TestPrettyPrinter::testCustomSeparatorsWithMinimal
    public void testCustomSeparatorsWithMinimal() throws Exception
    {
        StringWriter sw = new StringWriter();
        JsonGenerator gen = new JsonFactory().createGenerator(sw);
        gen.setPrettyPrinter(new MinimalPrettyPrinter().setSeparators(Separators.createDefaultInstance()
                .withObjectFieldValueSeparator('=')
                .withObjectEntrySeparator(';')
                .withArrayValueSeparator('|')));

        _writeTestDocument(gen);

        assertEquals("[3|\"abc\"|[true]|{\"f\"=null;\"f2\"=null}]", sw.toString());
    }

// com.fasterxml.jackson.core.main.TestPrettyPrinter::testCustomSeparatorsWithPP
    public void testCustomSeparatorsWithPP() throws Exception
    {
        StringWriter sw = new StringWriter();
        JsonGenerator gen = new JsonFactory().createGenerator(sw);
        gen.setPrettyPrinter(new DefaultPrettyPrinter().withSeparators(Separators.createDefaultInstance()
                .withObjectFieldValueSeparator('=')
                .withObjectEntrySeparator(';')
                .withArrayValueSeparator('|')));

        _writeTestDocument(gen);

        assertEquals("[ 3| \"abc\"| [ true ]| {" + DefaultIndenter.SYS_LF +
                "  \"f\" = null;" + DefaultIndenter.SYS_LF +
                "  \"f2\" = null" + DefaultIndenter.SYS_LF +
                "} ]", sw.toString());
    }

// com.fasterxml.jackson.core.main.TestPrettyPrinter::testCustomSeparatorsWithPPWithoutSpaces
    public void testCustomSeparatorsWithPPWithoutSpaces() throws Exception
    {
        StringWriter sw = new StringWriter();
        JsonGenerator gen = new JsonFactory().createGenerator(sw);
        gen.setPrettyPrinter(new DefaultPrettyPrinter().withSeparators(Separators.createDefaultInstance()
                .withObjectFieldValueSeparator('=')
                .withObjectEntrySeparator(';')
                .withArrayValueSeparator('|'))
            .withoutSpacesInObjectEntries());

        _writeTestDocument(gen);

        assertEquals("[ 3| \"abc\"| [ true ]| {" + DefaultIndenter.SYS_LF +
                "  \"f\"=null;" + DefaultIndenter.SYS_LF +
                "  \"f2\"=null" + DefaultIndenter.SYS_LF +
                "} ]", sw.toString());
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

// com.fasterxml.jackson.core.read.CommentParsingTest::testDefaultSettings
    public void testDefaultSettings() throws Exception
    {
        JsonFactory jf = new JsonFactory();
        assertFalse(jf.isEnabled(JsonParser.Feature.ALLOW_COMMENTS));
        JsonParser p = jf.createParser(new StringReader("[ 1 ]"));
        assertFalse(p.isEnabled(JsonParser.Feature.ALLOW_COMMENTS));
        p.close();
    }

// com.fasterxml.jackson.core.read.CommentParsingTest::testCommentsDisabled
    public void testCommentsDisabled() throws Exception
    {
        _testDisabled(DOC_WITH_SLASHSTAR_COMMENT, MODE_INPUT_STREAM);
        _testDisabled(DOC_WITH_SLASHSLASH_COMMENT, MODE_INPUT_STREAM);
        _testDisabled(DOC_WITH_SLASHSTAR_COMMENT, MODE_INPUT_STREAM_THROTTLED);
        _testDisabled(DOC_WITH_SLASHSLASH_COMMENT, MODE_INPUT_STREAM_THROTTLED);
        _testDisabled(DOC_WITH_SLASHSTAR_COMMENT, MODE_READER);
        _testDisabled(DOC_WITH_SLASHSLASH_COMMENT, MODE_READER);
        _testDisabled(DOC_WITH_SLASHSTAR_COMMENT, MODE_DATA_INPUT);
        _testDisabled(DOC_WITH_SLASHSLASH_COMMENT, MODE_DATA_INPUT);
    }

// com.fasterxml.jackson.core.read.CommentParsingTest::testCommentsEnabled
    public void testCommentsEnabled() throws Exception
    {
        _testEnabled(DOC_WITH_SLASHSTAR_COMMENT, MODE_INPUT_STREAM);
        _testEnabled(DOC_WITH_SLASHSLASH_COMMENT, MODE_INPUT_STREAM);
        _testEnabled(DOC_WITH_SLASHSTAR_COMMENT, MODE_INPUT_STREAM_THROTTLED);
        _testEnabled(DOC_WITH_SLASHSLASH_COMMENT, MODE_INPUT_STREAM_THROTTLED);
        _testEnabled(DOC_WITH_SLASHSTAR_COMMENT, MODE_READER);
        _testEnabled(DOC_WITH_SLASHSLASH_COMMENT, MODE_READER);
        _testEnabled(DOC_WITH_SLASHSTAR_COMMENT, MODE_DATA_INPUT);
        _testEnabled(DOC_WITH_SLASHSLASH_COMMENT, MODE_DATA_INPUT);
    }

// com.fasterxml.jackson.core.read.CommentParsingTest::testCommentsWithUTF8
    public void testCommentsWithUTF8() throws Exception
    {
        final String JSON = "\n [ \"bar? \u00a9\" ]\n";
        _testWithUTF8Chars(JSON, MODE_INPUT_STREAM);
        _testWithUTF8Chars(JSON, MODE_INPUT_STREAM_THROTTLED);
        _testWithUTF8Chars(JSON, MODE_READER);
        _testWithUTF8Chars(JSON, MODE_DATA_INPUT);
    }

// com.fasterxml.jackson.core.read.CommentParsingTest::testYAMLCommentsBytes
    public void testYAMLCommentsBytes() throws Exception {
        JsonFactory f = new JsonFactory();
        f.configure(JsonParser.Feature.ALLOW_YAML_COMMENTS, true);

        _testYAMLComments(f, MODE_INPUT_STREAM);
        _testCommentsBeforePropValue(f, MODE_INPUT_STREAM, "# foo\n");
        _testYAMLComments(f, MODE_INPUT_STREAM_THROTTLED);
        _testCommentsBeforePropValue(f, MODE_INPUT_STREAM_THROTTLED, "# foo\n");
        _testYAMLComments(f, MODE_DATA_INPUT);
        _testCommentsBeforePropValue(f, MODE_DATA_INPUT, "# foo\n");
    }

// com.fasterxml.jackson.core.read.CommentParsingTest::testYAMLCommentsChars
    public void testYAMLCommentsChars() throws Exception {
        JsonFactory f = new JsonFactory();
        f.configure(JsonParser.Feature.ALLOW_YAML_COMMENTS, true);
        _testYAMLComments(f, MODE_READER);
        final String COMMENT = "# foo\n";
        _testCommentsBeforePropValue(f, MODE_READER, COMMENT);
        _testCommentsBetweenArrayValues(f, MODE_READER, COMMENT);
    }

// com.fasterxml.jackson.core.read.CommentParsingTest::testCCommentsBytes
    public void testCCommentsBytes() throws Exception {
        JsonFactory f = new JsonFactory();
        f.configure(JsonParser.Feature.ALLOW_COMMENTS, true);
        final String COMMENT = "\n";
        _testCommentsBeforePropValue(f, MODE_INPUT_STREAM, COMMENT);
        _testCommentsBeforePropValue(f, MODE_INPUT_STREAM_THROTTLED, COMMENT);
        _testCommentsBeforePropValue(f, MODE_DATA_INPUT, COMMENT);
    }

// com.fasterxml.jackson.core.read.CommentParsingTest::testCCommentsChars
    public void testCCommentsChars() throws Exception {
        JsonFactory f = new JsonFactory();
        f.configure(JsonParser.Feature.ALLOW_COMMENTS, true);
        final String COMMENT = "\n";
        _testCommentsBeforePropValue(f, MODE_READER, COMMENT);
    }

// com.fasterxml.jackson.core.read.CommentParsingTest::testCppCommentsBytes
    public void testCppCommentsBytes() throws Exception {
        JsonFactory f = new JsonFactory();
        f.configure(JsonParser.Feature.ALLOW_COMMENTS, true);
        final String COMMENT = "// foo\n";
        _testCommentsBeforePropValue(f, MODE_INPUT_STREAM, COMMENT);
        _testCommentsBeforePropValue(f, MODE_INPUT_STREAM_THROTTLED, COMMENT);
        _testCommentsBeforePropValue(f, MODE_DATA_INPUT, COMMENT);
    }

// com.fasterxml.jackson.core.read.CommentParsingTest::testCppCommentsChars
    public void testCppCommentsChars() throws Exception {
        JsonFactory f = new JsonFactory();
        f.configure(JsonParser.Feature.ALLOW_COMMENTS, true);
        final String COMMENT = "// foo \n";
        _testCommentsBeforePropValue(f, MODE_READER, COMMENT);
    }

// com.fasterxml.jackson.core.read.DataInputTest::testEOFAfterArray
    public void testEOFAfterArray() throws Exception
    {
        JsonParser p = createParser(JSON_F, MODE_DATA_INPUT, "[ 1 ]  ");
        assertToken(JsonToken.START_ARRAY, p.nextToken());
        assertToken(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertToken(JsonToken.END_ARRAY, p.nextToken());
        assertNull(p.nextToken());
        p.close();
    }

// com.fasterxml.jackson.core.read.DataInputTest::testEOFAfterObject
    public void testEOFAfterObject() throws Exception
    {
        JsonParser p = createParser(JSON_F, MODE_DATA_INPUT, "{ \"value\" : true }");
        assertToken(JsonToken.START_OBJECT, p.nextToken());
        assertToken(JsonToken.FIELD_NAME, p.nextToken());
        assertToken(JsonToken.VALUE_TRUE, p.nextToken());
        assertToken(JsonToken.END_OBJECT, p.nextToken());
        assertNull(p.nextToken());
        p.close();
    }

// com.fasterxml.jackson.core.read.DataInputTest::testEOFAfterScalar
    public void testEOFAfterScalar() throws Exception
    {
        JsonParser p = createParser(JSON_F, MODE_DATA_INPUT, "\"foobar\" ");
        assertToken(JsonToken.VALUE_STRING, p.nextToken());
        assertEquals("foobar", p.getText());
        assertNull(p.nextToken());
        p.close();
    }

// com.fasterxml.jackson.core.read.JsonParserTest::testConfig
    public void testConfig() throws Exception
    {
        JsonParser p = createParserUsingReader("[ ]");
        p.enable(JsonParser.Feature.AUTO_CLOSE_SOURCE);
        assertTrue(p.isEnabled(JsonParser.Feature.AUTO_CLOSE_SOURCE));
        p.disable(JsonParser.Feature.AUTO_CLOSE_SOURCE);
        assertFalse(p.isEnabled(JsonParser.Feature.AUTO_CLOSE_SOURCE));

        p.configure(JsonParser.Feature.AUTO_CLOSE_SOURCE, true);
        assertTrue(p.isEnabled(JsonParser.Feature.AUTO_CLOSE_SOURCE));
        p.configure(JsonParser.Feature.AUTO_CLOSE_SOURCE, false);
        assertFalse(p.isEnabled(JsonParser.Feature.AUTO_CLOSE_SOURCE));
        p.close();
    }

// com.fasterxml.jackson.core.read.JsonParserTest::testInterningWithStreams
    public void testInterningWithStreams() throws Exception
    {
        _testIntern(true, true, "a");
        _testIntern(true, false, "b");
    }

// com.fasterxml.jackson.core.read.JsonParserTest::testInterningWithReaders
    public void testInterningWithReaders() throws Exception
    {
        _testIntern(false, true, "c");
        _testIntern(false, false, "d");
    }

// com.fasterxml.jackson.core.read.JsonParserTest::testSpecExampleSkipping
    public void testSpecExampleSkipping() throws Exception
    {
        _doTestSpec(false);
    }

// com.fasterxml.jackson.core.read.JsonParserTest::testSpecExampleFully
    public void testSpecExampleFully() throws Exception
    {
        _doTestSpec(true);
    }

// com.fasterxml.jackson.core.read.JsonParserTest::testKeywords
    public void testKeywords() throws Exception
    {
        final String DOC = "{\n"
            +"\"key1\" : null,\n"
            +"\"key2\" : true,\n"
            +"\"key3\" : false,\n"
            +"\"key4\" : [ false, null, true ]\n"
            +"}"
            ;

        JsonParser p = createParserUsingStream(JSON_FACTORY, DOC, "UTF-8");
        _testKeywords(p, true);
        p.close();

        p = createParserUsingReader(JSON_FACTORY, DOC);
        _testKeywords(p, true);
        p.close();

        p = createParserForDataInput(JSON_FACTORY, new MockDataInput(DOC));
        _testKeywords(p, false);
        p.close();
    }

// com.fasterxml.jackson.core.read.JsonParserTest::testSkipping
    public void testSkipping() throws Exception {
        _testSkipping(MODE_INPUT_STREAM);
        _testSkipping(MODE_INPUT_STREAM_THROTTLED);
        _testSkipping(MODE_READER);
        _testSkipping(MODE_DATA_INPUT);
    }

// com.fasterxml.jackson.core.read.JsonParserTest::testNameEscaping
    public void testNameEscaping() throws IOException
    {
        _testNameEscaping(MODE_INPUT_STREAM);
        _testNameEscaping(MODE_READER);
        _testNameEscaping(MODE_DATA_INPUT);
    }

// com.fasterxml.jackson.core.read.JsonParserTest::testLongText
    public void testLongText() throws Exception {
        
        _testLongText(310);
        _testLongText(7700);
        _testLongText(49000);
        _testLongText(96000);
    }

// com.fasterxml.jackson.core.read.JsonParserTest::testBytesAsSource
    public void testBytesAsSource() throws Exception
    {
        String JSON = "[ 1, 2, 3, 4 ]";
        byte[] b = JSON.getBytes("UTF-8");
        int offset = 50;
        int len = b.length;
        byte[] src = new byte[offset + len + offset];

        System.arraycopy(b, 0, src, offset, len);

        JsonParser p = JSON_FACTORY.createParser(src, offset, len);

        assertToken(JsonToken.START_ARRAY, p.nextToken());
        assertToken(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertEquals(1, p.getIntValue());
        assertToken(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertEquals(2, p.getIntValue());
        assertToken(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertEquals(3, p.getIntValue());
        assertToken(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertEquals(4, p.getIntValue());
        assertToken(JsonToken.END_ARRAY, p.nextToken());
        assertNull(p.nextToken());

        p.close();
    }

// com.fasterxml.jackson.core.read.JsonParserTest::testUtf8BOMHandling
    public void testUtf8BOMHandling() throws Exception
    {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        
        bytes.write(0xEF);
        bytes.write(0xBB);
        bytes.write(0xBF);
        bytes.write("[ 1 ]".getBytes("UTF-8"));
        byte[] input = bytes.toByteArray();

        JsonParser p = JSON_FACTORY.createParser(input);
        assertEquals(JsonToken.START_ARRAY, p.nextToken());
        
        
        JsonLocation loc = p.getTokenLocation();
        
        assertEquals(0, loc.getByteOffset());
        assertEquals(-1, loc.getCharOffset());
        assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertEquals(JsonToken.END_ARRAY, p.nextToken());
        p.close();

        p = JSON_FACTORY.createParser(new MockDataInput(input));
        assertEquals(JsonToken.START_ARRAY, p.nextToken());
        
        
        loc = p.getTokenLocation();
        assertNotNull(loc);
        assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertEquals(JsonToken.END_ARRAY, p.nextToken());
        p.close();
    }

// com.fasterxml.jackson.core.read.JsonParserTest::testSpacesInURL
    public void testSpacesInURL() throws Exception
    {
        File f = File.createTempFile("pre fix&stuff", ".txt");
        BufferedWriter w = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(f), "UTF-8"));
        w.write("{ }");
        w.close();
        URL url = f.toURI().toURL();

        JsonParser p = JSON_FACTORY.createParser(url);
        assertToken(JsonToken.START_OBJECT, p.nextToken());
        assertToken(JsonToken.END_OBJECT, p.nextToken());
        p.close();
    }

// com.fasterxml.jackson.core.read.JsonParserTest::testHandlingOfInvalidSpaceByteStream
    public void testHandlingOfInvalidSpaceByteStream() throws Exception {
        _testHandlingOfInvalidSpace(MODE_INPUT_STREAM);
        _testHandlingOfInvalidSpaceFromResource(true);
    }

// com.fasterxml.jackson.core.read.JsonParserTest::testHandlingOfInvalidSpaceChars
    public void testHandlingOfInvalidSpaceChars() throws Exception {
        _testHandlingOfInvalidSpace(MODE_READER);
        _testHandlingOfInvalidSpaceFromResource(false);
    }

// com.fasterxml.jackson.core.read.JsonParserTest::testHandlingOfInvalidSpaceDataInput
    public void testHandlingOfInvalidSpaceDataInput() throws Exception {
        _testHandlingOfInvalidSpace(MODE_DATA_INPUT);
    }

// com.fasterxml.jackson.core.read.JsonParserTest::testGetValueAsTextBytes
    public void testGetValueAsTextBytes() throws Exception
    {
        _testGetValueAsText(MODE_INPUT_STREAM, false);
        _testGetValueAsText(MODE_INPUT_STREAM, true);
    }

// com.fasterxml.jackson.core.read.JsonParserTest::testGetValueAsTextDataInput
    public void testGetValueAsTextDataInput() throws Exception
    {
        _testGetValueAsText(MODE_DATA_INPUT, false);
        _testGetValueAsText(MODE_DATA_INPUT, true);
    }

// com.fasterxml.jackson.core.read.JsonParserTest::testGetValueAsTextChars
    public void testGetValueAsTextChars() throws Exception
    {
        _testGetValueAsText(MODE_READER, false);
        _testGetValueAsText(MODE_READER, true);
    }

// com.fasterxml.jackson.core.read.JsonParserTest::testGetTextViaWriter
    public void testGetTextViaWriter() throws Exception
    {
        for (int mode : ALL_MODES) {
            _testGetTextViaWriter(mode);
        }
    }

// com.fasterxml.jackson.core.read.JsonParserTest::testLongerReadText
    public void testLongerReadText() throws Exception
    {
        for (int mode : ALL_MODES) {
            _testLongerReadText(mode);
        }
    }

// com.fasterxml.jackson.core.read.NextXxxAccessTest::testIsNextTokenName
    public void testIsNextTokenName() throws Exception
    {
        _testIsNextTokenName1(MODE_INPUT_STREAM);
        _testIsNextTokenName1(MODE_INPUT_STREAM_THROTTLED);
        _testIsNextTokenName1(MODE_DATA_INPUT);
        _testIsNextTokenName1(MODE_READER);
    }

// com.fasterxml.jackson.core.read.NextXxxAccessTest::testIsNextTokenName2
    public void testIsNextTokenName2() throws Exception {
        _testIsNextTokenName2(MODE_INPUT_STREAM);
        _testIsNextTokenName2(MODE_INPUT_STREAM_THROTTLED);
        _testIsNextTokenName2(MODE_DATA_INPUT);
        _testIsNextTokenName2(MODE_READER);
    }

// com.fasterxml.jackson.core.read.NextXxxAccessTest::testIsNextTokenName3
    public void testIsNextTokenName3() throws Exception {
        _testIsNextTokenName3(MODE_INPUT_STREAM);
        _testIsNextTokenName3(MODE_INPUT_STREAM_THROTTLED);
        _testIsNextTokenName3(MODE_DATA_INPUT);
        _testIsNextTokenName3(MODE_READER);
    }

// com.fasterxml.jackson.core.read.NextXxxAccessTest::testIsNextTokenName4
    public void testIsNextTokenName4() throws Exception {
        _testIsNextTokenName4(MODE_INPUT_STREAM);
        _testIsNextTokenName4(MODE_INPUT_STREAM_THROTTLED);
        _testIsNextTokenName4(MODE_DATA_INPUT);
        _testIsNextTokenName4(MODE_READER);
    }

// com.fasterxml.jackson.core.read.NextXxxAccessTest::testIssue34
    public void testIssue34() throws Exception
    {
        _testIssue34(MODE_INPUT_STREAM);
        _testIssue34(MODE_INPUT_STREAM_THROTTLED);
        _testIssue34(MODE_DATA_INPUT);
        _testIssue34(MODE_READER);
    }

// com.fasterxml.jackson.core.read.NextXxxAccessTest::testIssue38
    public void testIssue38() throws Exception
    {
        _testIssue38(MODE_INPUT_STREAM);
        _testIssue38(MODE_INPUT_STREAM_THROTTLED);
        _testIssue38(MODE_DATA_INPUT);
        _testIssue38(MODE_READER);
    }

// com.fasterxml.jackson.core.read.NextXxxAccessTest::testNextNameWithLongContent
    public void testNextNameWithLongContent() throws Exception
    {
        _testNextNameWithLong(MODE_INPUT_STREAM);
        _testNextNameWithLong(MODE_INPUT_STREAM_THROTTLED);
        _testNextNameWithLong(MODE_DATA_INPUT);
        _testNextNameWithLong(MODE_READER);
    }

// com.fasterxml.jackson.core.read.NextXxxAccessTest::testNextNameWithIndentation
    public void testNextNameWithIndentation() throws Exception
    {
        _testNextFieldNameIndent(MODE_INPUT_STREAM);
        _testNextFieldNameIndent(MODE_INPUT_STREAM_THROTTLED);
        _testNextFieldNameIndent(MODE_DATA_INPUT);
        _testNextFieldNameIndent(MODE_READER);
    }

// com.fasterxml.jackson.core.read.NextXxxAccessTest::testNextTextValue
    public void testNextTextValue() throws Exception
    {
        _textNextText(MODE_INPUT_STREAM);
        _textNextText(MODE_INPUT_STREAM_THROTTLED);
        _textNextText(MODE_DATA_INPUT);
        _textNextText(MODE_READER);
    }

// com.fasterxml.jackson.core.read.NextXxxAccessTest::testNextIntValue
    public void testNextIntValue() throws Exception
    {
        _textNextInt(MODE_INPUT_STREAM);
        _textNextInt(MODE_INPUT_STREAM_THROTTLED);
        _textNextInt(MODE_DATA_INPUT);
        _textNextInt(MODE_READER);
    }

// com.fasterxml.jackson.core.read.NextXxxAccessTest::testNextLongValue
    public void testNextLongValue() throws Exception
    {
        _textNextLong(MODE_INPUT_STREAM);
        _textNextLong(MODE_INPUT_STREAM_THROTTLED);
        _textNextLong(MODE_DATA_INPUT);
        _textNextLong(MODE_READER);
    }

// com.fasterxml.jackson.core.read.NextXxxAccessTest::testNextBooleanValue
    public void testNextBooleanValue() throws Exception
    {
        _textNextBoolean(MODE_INPUT_STREAM);
        _textNextBoolean(MODE_INPUT_STREAM_THROTTLED);
        _textNextBoolean(MODE_DATA_INPUT);
        _textNextBoolean(MODE_READER);
    }

// com.fasterxml.jackson.core.read.NonStandardParserFeaturesTest::testSingleQuotesDefault
    public void testSingleQuotesDefault() throws Exception
    {
        _testSingleQuotesDefault(MODE_INPUT_STREAM);
        _testSingleQuotesDefault(MODE_INPUT_STREAM_THROTTLED);
        _testSingleQuotesDefault(MODE_DATA_INPUT);
        _testSingleQuotesDefault(MODE_READER);
    }

// com.fasterxml.jackson.core.read.NonStandardParserFeaturesTest::testSingleQuotesEnabled
    public void testSingleQuotesEnabled() throws Exception
    {
        _testSingleQuotesEnabled(MODE_INPUT_STREAM);
        _testSingleQuotesEnabled(MODE_INPUT_STREAM_THROTTLED);
        _testSingleQuotesEnabled(MODE_DATA_INPUT);
        _testSingleQuotesEnabled(MODE_READER);

        _testSingleQuotesEscaped(MODE_INPUT_STREAM);
        _testSingleQuotesEscaped(MODE_INPUT_STREAM_THROTTLED);
        _testSingleQuotesEscaped(MODE_DATA_INPUT);
        _testSingleQuotesEscaped(MODE_READER);
    }

// com.fasterxml.jackson.core.read.NonStandardParserFeaturesTest::testNonStandardNameChars
    public void testNonStandardNameChars() throws Exception
    {
        _testNonStandardNameChars(MODE_INPUT_STREAM);
        _testNonStandardNameChars(MODE_INPUT_STREAM_THROTTLED);
        _testNonStandardNameChars(MODE_DATA_INPUT);
        _testNonStandardNameChars(MODE_READER);
    }

// com.fasterxml.jackson.core.read.NonStandardParserFeaturesTest::testNonStandardAnyCharQuoting
    public void testNonStandardAnyCharQuoting() throws Exception
    {
        _testNonStandarBackslashQuoting(MODE_INPUT_STREAM);
        _testNonStandarBackslashQuoting(MODE_INPUT_STREAM_THROTTLED);
        _testNonStandarBackslashQuoting(MODE_DATA_INPUT);
        _testNonStandarBackslashQuoting(MODE_READER);
    }

// com.fasterxml.jackson.core.read.NonStandardParserFeaturesTest::testLeadingZeroesUTF8
    public void testLeadingZeroesUTF8() throws Exception {
        _testLeadingZeroes(MODE_INPUT_STREAM, false);
        _testLeadingZeroes(MODE_INPUT_STREAM, true);
        _testLeadingZeroes(MODE_INPUT_STREAM_THROTTLED, false);
        _testLeadingZeroes(MODE_INPUT_STREAM_THROTTLED, true);

        
        
        _testLeadingZeroes(MODE_DATA_INPUT, true);
    }

// com.fasterxml.jackson.core.read.NonStandardParserFeaturesTest::testLeadingZeroesReader
    public void testLeadingZeroesReader() throws Exception {
        _testLeadingZeroes(MODE_READER, false);
        _testLeadingZeroes(MODE_READER, true);
    }

// com.fasterxml.jackson.core.read.NonStandardParserFeaturesTest::testAllowNaN
    public void testAllowNaN() throws Exception {
        _testAllowNaN(MODE_INPUT_STREAM);
        _testAllowNaN(MODE_INPUT_STREAM_THROTTLED);
        _testAllowNaN(MODE_DATA_INPUT);
        _testAllowNaN(MODE_READER);
    }

// com.fasterxml.jackson.core.read.NonStandardParserFeaturesTest::testAllowInfinity
    public void testAllowInfinity() throws Exception {
        _testAllowInf(MODE_INPUT_STREAM);
        _testAllowInf(MODE_INPUT_STREAM_THROTTLED);
        _testAllowInf(MODE_DATA_INPUT);
        _testAllowInf(MODE_READER);
    }

// com.fasterxml.jackson.core.read.NonStandardUnquotedNamesTest::testSimpleUnquotedBytes
    public void testSimpleUnquotedBytes() throws Exception {
        _testSimpleUnquoted(MODE_INPUT_STREAM);
        _testSimpleUnquoted(MODE_INPUT_STREAM_THROTTLED);
        _testSimpleUnquoted(MODE_DATA_INPUT);
    }

// com.fasterxml.jackson.core.read.NonStandardUnquotedNamesTest::testSimpleUnquotedChars
    public void testSimpleUnquotedChars() throws Exception {
        _testSimpleUnquoted(MODE_READER);
    }

// com.fasterxml.jackson.core.read.NonStandardUnquotedNamesTest::testLargeUnquoted
    public void testLargeUnquoted() throws Exception
    {
        _testLargeUnquoted(MODE_INPUT_STREAM);
        _testLargeUnquoted(MODE_INPUT_STREAM_THROTTLED);
        _testLargeUnquoted(MODE_DATA_INPUT);
        _testLargeUnquoted(MODE_READER);
    }

// com.fasterxml.jackson.core.read.NonStandardUnquotedNamesTest::testUnquotedIssue510
    public void testUnquotedIssue510() throws Exception
    {
        
        char[] fullChars = new char[4001];
        for (int i = 0; i < 3998; i++) {
             fullChars[i] = ' ';
        }
        fullChars[3998] = '{';
        fullChars[3999] = 'a';
        fullChars[4000] = 256;

        JsonParser p = UNQUOTED_FIELDS_F.createParser(new java.io.StringReader(new String(fullChars)));
        assertToken(JsonToken.START_OBJECT, p.nextToken());
        try {
            p.nextToken();
            fail("Should not pass");
        } catch (JsonParseException e) {
            ; 
        }
        p.close();
    }

// com.fasterxml.jackson.core.read.NumberCoercionTest::testToIntCoercion
    public void testToIntCoercion() throws Exception
    {
        for (int mode : ALL_STREAMING_MODES) {
            JsonParser p;

            
            p = createParser(mode, "1");
            assertToken(JsonToken.VALUE_NUMBER_INT, p.nextToken());
            assertEquals(1L, p.getLongValue());
            assertEquals(1, p.getIntValue());
            p.close();

            
            p = createParser(mode, "10");
            assertToken(JsonToken.VALUE_NUMBER_INT, p.nextToken());
            assertEquals(BigInteger.TEN, p.getBigIntegerValue());
            assertEquals(10, p.getIntValue());
            p.close();

            
            p = createParser(mode, "2");
            assertToken(JsonToken.VALUE_NUMBER_INT, p.nextToken());
            assertEquals(2.0, p.getDoubleValue());
            assertEquals(2, p.getIntValue());
            p.close();

            
            p = createParser(mode, "10");
            assertToken(JsonToken.VALUE_NUMBER_INT, p.nextToken());
            assertEquals(BigDecimal.TEN, p.getDecimalValue());
            assertEquals(10, p.getIntValue());
            p.close();
        }
    }

// com.fasterxml.jackson.core.read.NumberCoercionTest::testToIntFailing
    public void testToIntFailing() throws Exception
    {
        for (int mode : ALL_STREAMING_MODES) {
            JsonParser p;

            
            long big = 1L + Integer.MAX_VALUE;
            p = createParser(mode, String.valueOf(big));
            assertToken(JsonToken.VALUE_NUMBER_INT, p.nextToken());
            assertEquals(big, p.getLongValue());
            try {
                p.getIntValue();
                fail("Should not pass");
            } catch (JsonParseException e) {
                verifyException(e, "out of range of int");
            }
            long small = -1L + Integer.MIN_VALUE;
            p = createParser(mode, String.valueOf(small));
            assertToken(JsonToken.VALUE_NUMBER_INT, p.nextToken());
            assertEquals(Long.valueOf(small), p.getNumberValue());
            assertEquals(small, p.getLongValue());
            try {
                p.getIntValue();
                fail("Should not pass");
            } catch (JsonParseException e) {
                verifyException(e, "out of range of int");
            }

            
            p = createParser(mode, String.valueOf(big)+".0");
            assertToken(JsonToken.VALUE_NUMBER_FLOAT, p.nextToken());
            assertEquals((double) big, p.getDoubleValue());
            try {
                p.getIntValue();
                fail("Should not pass");
            } catch (JsonParseException e) {
                verifyException(e, "out of range of int");
            }
            p = createParser(mode, String.valueOf(small)+".0");
            assertToken(JsonToken.VALUE_NUMBER_FLOAT, p.nextToken());
            assertEquals((double) small, p.getDoubleValue());
            try {
                p.getIntValue();
                fail("Should not pass");
            } catch (JsonParseException e) {
                verifyException(e, "out of range of int");
            }

            
            p = createParser(mode, String.valueOf(big));
            assertToken(JsonToken.VALUE_NUMBER_INT, p.nextToken());
            assertEquals(BigInteger.valueOf(big), p.getBigIntegerValue());
            try {
                p.getIntValue();
                fail("Should not pass");
            } catch (JsonParseException e) {
                verifyException(e, "out of range of int");
            }
            p = createParser(mode, String.valueOf(small));
            assertToken(JsonToken.VALUE_NUMBER_INT, p.nextToken());
            assertEquals(BigInteger.valueOf(small), p.getBigIntegerValue());
            try {
                p.getIntValue();
                fail("Should not pass");
            } catch (JsonParseException e) {
                verifyException(e, "out of range of int");
            }
        }
    }

// com.fasterxml.jackson.core.read.NumberCoercionTest::testToLongCoercion
    public void testToLongCoercion() throws Exception
    {
        for (int mode : ALL_STREAMING_MODES) {
            JsonParser p;

            
            p = createParser(mode, "1");
            assertToken(JsonToken.VALUE_NUMBER_INT, p.nextToken());
            assertEquals(1, p.getIntValue());
            assertEquals(1L, p.getLongValue());
            p.close();

            
            long biggish = 12345678901L;
            p = createParser(mode, String.valueOf(biggish));
            assertToken(JsonToken.VALUE_NUMBER_INT, p.nextToken());
            assertEquals(BigInteger.valueOf(biggish), p.getBigIntegerValue());
            assertEquals(biggish, p.getLongValue());
            p.close();

            
            p = createParser(mode, "2");
            assertToken(JsonToken.VALUE_NUMBER_INT, p.nextToken());
            assertEquals(2.0, p.getDoubleValue());
            assertEquals(2L, p.getLongValue());
            p.close();

            
            p = createParser(mode, "10");
            assertToken(JsonToken.VALUE_NUMBER_INT, p.nextToken());
            assertEquals(BigDecimal.TEN, p.getDecimalValue());
            assertEquals(10, p.getLongValue());
            p.close();
        }
    }

// com.fasterxml.jackson.core.read.NumberCoercionTest::testToLongFailing
    public void testToLongFailing() throws Exception
    {
        for (int mode : ALL_STREAMING_MODES) {
            JsonParser p;

            
            BigInteger big = BigInteger.valueOf(Long.MAX_VALUE).add(BigInteger.TEN);
            p = createParser(mode, String.valueOf(big));
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
            p = createParser(mode, String.valueOf(small));
            assertToken(JsonToken.VALUE_NUMBER_INT, p.nextToken());
            assertEquals(small, p.getBigIntegerValue());
            try {
                p.getLongValue();
                fail("Should not pass");
            } catch (JsonParseException e) {
                verifyException(e, "out of range of long");
            }
        }
    }

// com.fasterxml.jackson.core.read.NumberCoercionTest::testToBigIntegerCoercion
    public void testToBigIntegerCoercion() throws Exception
    {
        for (int mode : ALL_STREAMING_MODES) {
            JsonParser p;

            p = createParser(mode, "1");
            assertToken(JsonToken.VALUE_NUMBER_INT, p.nextToken());
            
            assertEquals(1, p.getIntValue());
            assertEquals(BigInteger.ONE, p.getBigIntegerValue());
            p.close();

            p = createParser(mode, "2.0");
            assertToken(JsonToken.VALUE_NUMBER_FLOAT, p.nextToken());
            
            assertEquals(2.0, p.getDoubleValue());
            assertEquals(BigInteger.valueOf(2L), p.getBigIntegerValue());
            p.close();
            
            p = createParser(mode, String.valueOf(Long.MAX_VALUE));
            assertToken(JsonToken.VALUE_NUMBER_INT, p.nextToken());
            
            assertEquals(Long.MAX_VALUE, p.getLongValue());
            assertEquals(BigInteger.valueOf(Long.MAX_VALUE), p.getBigIntegerValue());
            p.close();

            p = createParser(mode, " 200.0");
            assertToken(JsonToken.VALUE_NUMBER_FLOAT, p.nextToken());
            
            assertEquals(new BigDecimal("200.0"), p.getDecimalValue());
            assertEquals(BigInteger.valueOf(200L), p.getBigIntegerValue());
            p.close();
        }
    }

// com.fasterxml.jackson.core.read.NumberCoercionTest::testToDoubleCoercion
    public void testToDoubleCoercion() throws Exception
    {
        for (int mode : ALL_STREAMING_MODES) {
            JsonParser p;

            
            p = createParser(mode, "100.5");
            assertToken(JsonToken.VALUE_NUMBER_FLOAT, p.nextToken());
            assertEquals(new BigDecimal("100.5"), p.getDecimalValue());
            assertEquals(100.5, p.getDoubleValue());
            p.close();

            p = createParser(mode, "10");
            assertToken(JsonToken.VALUE_NUMBER_INT, p.nextToken());
            assertEquals(BigInteger.TEN, p.getBigIntegerValue());
            assertEquals(10.0, p.getDoubleValue());
            p.close();
        }
    }

// com.fasterxml.jackson.core.read.NumberCoercionTest::testToBigDecimalCoercion
    public void testToBigDecimalCoercion() throws Exception
    {
        for (int mode : ALL_STREAMING_MODES) {
            JsonParser p;

            p = createParser(mode, "1");
            assertToken(JsonToken.VALUE_NUMBER_INT, p.nextToken());
            
            assertEquals(1, p.getIntValue());
            assertEquals(BigDecimal.ONE, p.getDecimalValue());
            p.close();

            p = createParser(mode, String.valueOf(Long.MAX_VALUE));
            assertToken(JsonToken.VALUE_NUMBER_INT, p.nextToken());
            
            assertEquals(Long.MAX_VALUE, p.getLongValue());
            assertEquals(BigDecimal.valueOf(Long.MAX_VALUE), p.getDecimalValue());
            p.close();

            BigInteger biggie = BigInteger.valueOf(Long.MAX_VALUE).multiply(BigInteger.TEN);
            p = createParser(mode, String.valueOf(biggie));
            assertToken(JsonToken.VALUE_NUMBER_INT, p.nextToken());
            
            assertEquals(biggie, p.getBigIntegerValue());
            assertEquals(new BigDecimal(biggie), p.getDecimalValue());
            p.close();

        }
    }

// com.fasterxml.jackson.core.read.NumberOverflowTest::testSimpleLongOverflow
    public void testSimpleLongOverflow() throws Exception
    {
        BigInteger below = BigInteger.valueOf(Long.MIN_VALUE);
        below = below.subtract(BigInteger.ONE);
        BigInteger above = BigInteger.valueOf(Long.MAX_VALUE);
        above = above.add(BigInteger.ONE);

        String DOC_BELOW = below.toString() + " ";
        String DOC_ABOVE = below.toString() + " ";

        for (int mode : ALL_MODES) {
            JsonParser p = createParser(FACTORY, mode, DOC_BELOW);
            p.nextToken();
            try {
                long x = p.getLongValue();
                fail("Expected an exception for underflow (input "+p.getText()+"): instead, got long value: "+x);
            } catch (JsonParseException e) {
                verifyException(e, "out of range of long");
            }
            p.close();

            p = createParser(mode, DOC_ABOVE);
            p.nextToken();
            try {
                long x = p.getLongValue();
                fail("Expected an exception for underflow (input "+p.getText()+"): instead, got long value: "+x);
            } catch (JsonParseException e) {
                verifyException(e, "out of range of long");
            }
            p.close();
        }
    }

// com.fasterxml.jackson.core.read.NumberOverflowTest::testMaliciousLongOverflow
    public void testMaliciousLongOverflow() throws Exception
    {
        for (int mode : ALL_STREAMING_MODES) {
            for (String doc : new String[] { BIG_POS_DOC, BIG_NEG_DOC }) {
                JsonParser p = createParser(mode, doc);
                assertToken(JsonToken.START_ARRAY, p.nextToken());
                assertToken(JsonToken.VALUE_NUMBER_INT, p.nextToken());
                try {
                    p.getLongValue();
                    fail("Should not pass");
                } catch (JsonParseException e) {
                    verifyException(e, "out of range of long");
                    verifyException(e, "Integer with "+BIG_NUM_LEN+" digits");
                }
                p.close();
            }
        }
    }

// com.fasterxml.jackson.core.read.NumberOverflowTest::testMaliciousIntOverflow
    public void testMaliciousIntOverflow() throws Exception
    {
        for (int mode : ALL_STREAMING_MODES) {
            for (String doc : new String[] { BIG_POS_DOC, BIG_NEG_DOC }) {
                JsonParser p = createParser(mode, doc);
                assertToken(JsonToken.START_ARRAY, p.nextToken());
                assertToken(JsonToken.VALUE_NUMBER_INT, p.nextToken());
                try {
                    p.getIntValue();
                    fail("Should not pass");
                } catch (JsonParseException e) {
                    verifyException(e, "out of range of int");
                    verifyException(e, "Integer with "+BIG_NUM_LEN+" digits");
                }
                p.close();
            }
        }
    }

// com.fasterxml.jackson.core.read.NumberOverflowTest::testMaliciousBigIntToDouble
    public void testMaliciousBigIntToDouble() throws Exception
    {
        for (int mode : ALL_STREAMING_MODES) {
            final String doc = BIG_POS_DOC;
            JsonParser p = createParser(mode, doc);
            assertToken(JsonToken.START_ARRAY, p.nextToken());
            assertToken(JsonToken.VALUE_NUMBER_INT, p.nextToken());
            double d = p.getDoubleValue();
            assertEquals(Double.valueOf(BIG_POS_INTEGER), d);
            assertToken(JsonToken.END_ARRAY, p.nextToken());
            p.close();
        }
    }

// com.fasterxml.jackson.core.read.NumberOverflowTest::testMaliciousBigIntToFloat
    public void testMaliciousBigIntToFloat() throws Exception
    {
        for (int mode : ALL_STREAMING_MODES) {
            final String doc = BIG_POS_DOC;
            JsonParser p = createParser(mode, doc);
            assertToken(JsonToken.START_ARRAY, p.nextToken());
            assertToken(JsonToken.VALUE_NUMBER_INT, p.nextToken());
            float f = p.getFloatValue();
            assertEquals(Float.valueOf(BIG_POS_INTEGER), f);
            assertToken(JsonToken.END_ARRAY, p.nextToken());
            p.close();
        }
    }

// com.fasterxml.jackson.core.read.NumberParsingTest::testSimpleBoolean
    public void testSimpleBoolean() throws Exception
    {
        _testSimpleBoolean(MODE_INPUT_STREAM);
        _testSimpleBoolean(MODE_INPUT_STREAM_THROTTLED);
        _testSimpleBoolean(MODE_READER);
        _testSimpleBoolean(MODE_DATA_INPUT);
    }

// com.fasterxml.jackson.core.read.NumberParsingTest::testSimpleInt
    public void testSimpleInt() throws Exception
    {
        for (int EXP_I : new int[] { 1234, -999, 0, 1, -2 }) {
            _testSimpleInt(EXP_I, MODE_INPUT_STREAM);
            _testSimpleInt(EXP_I, MODE_INPUT_STREAM_THROTTLED);
            _testSimpleInt(EXP_I, MODE_READER);
            _testSimpleInt(EXP_I, MODE_DATA_INPUT);
        }
    }

// com.fasterxml.jackson.core.read.NumberParsingTest::testIntRange
    public void testIntRange() throws Exception
    {
        
        for (int mode : ALL_MODES) {
            String DOC = "[ "+Integer.MAX_VALUE+","+Integer.MIN_VALUE+" ]";
            JsonParser p = createParser(mode, DOC);
            assertToken(JsonToken.START_ARRAY, p.nextToken());
            assertToken(JsonToken.VALUE_NUMBER_INT, p.nextToken());
            assertEquals(JsonParser.NumberType.INT, p.getNumberType());
            assertEquals(Integer.MAX_VALUE, p.getIntValue());
    
            assertToken(JsonToken.VALUE_NUMBER_INT, p.nextToken());
            assertEquals(JsonParser.NumberType.INT, p.getNumberType());
            assertEquals(Integer.MIN_VALUE, p.getIntValue());
            p.close();
        }
    }

// com.fasterxml.jackson.core.read.NumberParsingTest::testSimpleLong
    public void testSimpleLong() throws Exception
    {
        _testSimpleLong(MODE_INPUT_STREAM);
        _testSimpleLong(MODE_INPUT_STREAM_THROTTLED);
        _testSimpleLong(MODE_READER);
        _testSimpleLong(MODE_DATA_INPUT);
    }

// com.fasterxml.jackson.core.read.NumberParsingTest::testLongRange
    public void testLongRange() throws Exception
    {
        for (int mode : ALL_MODES) {
            long belowMinInt = -1L + Integer.MIN_VALUE;
            long aboveMaxInt = 1L + Integer.MAX_VALUE;
            String input = "[ "+Long.MAX_VALUE+","+Long.MIN_VALUE+","+aboveMaxInt+", "+belowMinInt+" ]";
            JsonParser p = createParser(mode, input);
            assertToken(JsonToken.START_ARRAY, p.nextToken());
            assertToken(JsonToken.VALUE_NUMBER_INT, p.nextToken());
            assertEquals(JsonParser.NumberType.LONG, p.getNumberType());
            assertEquals(Long.MAX_VALUE, p.getLongValue());
        
            assertToken(JsonToken.VALUE_NUMBER_INT, p.nextToken());
            assertEquals(JsonParser.NumberType.LONG, p.getNumberType());
            assertEquals(Long.MIN_VALUE, p.getLongValue());

            assertToken(JsonToken.VALUE_NUMBER_INT, p.nextToken());
            assertEquals(JsonParser.NumberType.LONG, p.getNumberType());
            assertEquals(aboveMaxInt, p.getLongValue());

            assertToken(JsonToken.VALUE_NUMBER_INT, p.nextToken());
            assertEquals(JsonParser.NumberType.LONG, p.getNumberType());
            assertEquals(belowMinInt, p.getLongValue());

            
            assertToken(JsonToken.END_ARRAY, p.nextToken());        
            p.close();
        }
    }

// com.fasterxml.jackson.core.read.NumberParsingTest::testBigDecimalRange
    public void testBigDecimalRange() throws Exception
    {
        for (int mode : ALL_MODES) {
            
            BigInteger small = new BigDecimal(Long.MIN_VALUE).toBigInteger();
            small = small.subtract(BigInteger.ONE);
            BigInteger big = new BigDecimal(Long.MAX_VALUE).toBigInteger();
            big = big.add(BigInteger.ONE);
            String input = "[ "+small+"  ,  "+big+"]";
            JsonParser p = createParser(mode, input);
            assertToken(JsonToken.START_ARRAY, p.nextToken());
            assertToken(JsonToken.VALUE_NUMBER_INT, p.nextToken());
            assertEquals(JsonParser.NumberType.BIG_INTEGER, p.getNumberType());
            assertEquals(small, p.getBigIntegerValue());
            assertToken(JsonToken.VALUE_NUMBER_INT, p.nextToken());
            assertEquals(JsonParser.NumberType.BIG_INTEGER, p.getNumberType());
            assertEquals(big, p.getBigIntegerValue());
            assertToken(JsonToken.END_ARRAY, p.nextToken());        
            p.close();
        }
    }

// com.fasterxml.jackson.core.read.NumberParsingTest::testBigNumbers
    public void testBigNumbers() throws Exception
    {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 520; ++i) { 
            sb.append('1');
        }
        final String NUMBER_STR = sb.toString();
        BigInteger biggie = new BigInteger(NUMBER_STR);

        for (int mode : ALL_MODES) {
            JsonParser p = createParser(mode, NUMBER_STR +" ");
            assertToken(JsonToken.VALUE_NUMBER_INT, p.nextToken());
            assertEquals(JsonParser.NumberType.BIG_INTEGER, p.getNumberType());
            assertEquals(NUMBER_STR, p.getText());
            assertEquals(biggie, p.getBigIntegerValue());
            p.close();
        }
    }

// com.fasterxml.jackson.core.read.NumberParsingTest::testSimpleDouble
    public void testSimpleDouble() throws Exception
    {
        final String[] INPUTS = new String[] {
            "1234.00", "2.1101567E-16", "1.0e5", "0.0", "1.0", "-1.0", 
            "-0.5", "-12.9", "-999.0",
            "2.5e+5", "9e4", "-12e-3", "0.25",
        };
        for (int mode : ALL_MODES) {
            for (int i = 0; i < INPUTS.length; ++i) {

                
                
                String STR = INPUTS[i];
                double EXP_D = Double.parseDouble(STR);
                String DOC = "["+STR+"]";

                JsonParser p = createParser(mode, DOC+" ");
                assertToken(JsonToken.START_ARRAY, p.nextToken());

                assertToken(JsonToken.VALUE_NUMBER_FLOAT, p.nextToken());
                assertEquals(STR, p.getText());
                assertEquals(EXP_D, p.getDoubleValue());
                assertToken(JsonToken.END_ARRAY, p.nextToken());
                if (mode != MODE_DATA_INPUT) {
                    assertNull(p.nextToken());
                }
                p.close();

                
                p = createParser(mode, STR + " ");
                JsonToken t = null;

                try {
                    t = p.nextToken();
                } catch (Exception e) {
                    throw new Exception("Failed to parse input '"+STR+"' (parser of type "+p.getClass().getSimpleName()+")", e);
                }
                
                assertToken(JsonToken.VALUE_NUMBER_FLOAT, t);
                assertEquals(STR, p.getText());
                if (mode != MODE_DATA_INPUT) {
                    assertNull(p.nextToken());
                }
                p.close();
            }
        }
    }

// com.fasterxml.jackson.core.read.NumberParsingTest::testNumbers
    public void testNumbers() throws Exception
    {
        _testNumbers(MODE_INPUT_STREAM);
        _testNumbers(MODE_INPUT_STREAM_THROTTLED);
        _testNumbers(MODE_READER);
        _testNumbers(MODE_DATA_INPUT);
    }

// com.fasterxml.jackson.core.read.NumberParsingTest::testParsingOfLongerSequences
    public void testParsingOfLongerSequences() throws Exception
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
            JsonParser p;

            if (input == 0) {
                p = createParserUsingStream(DOC, "UTF-8");
            } else {
                p = FACTORY.createParser(DOC);
            }

            assertToken(JsonToken.START_ARRAY, p.nextToken());
            for (int i = 0; i < COUNT; ++i) {
                for (double d : values) {
                    assertToken(JsonToken.VALUE_NUMBER_FLOAT, p.nextToken());
                    assertEquals(d, p.getDoubleValue());
                }
            }
            assertToken(JsonToken.END_ARRAY, p.nextToken());
            p.close();
        }
    }

// com.fasterxml.jackson.core.read.NumberParsingTest::testLongNumbers
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

// com.fasterxml.jackson.core.read.NumberParsingTest::testLongNumbers2
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

// com.fasterxml.jackson.core.read.NumberParsingTest::testParsingOfLongerSequencesWithNonNumeric
    public void testParsingOfLongerSequencesWithNonNumeric() throws Exception
    {
        JsonFactory f = new JsonFactory();
        f.enable(JsonParser.Feature.ALLOW_NON_NUMERIC_NUMBERS);
        _testParsingOfLongerSequencesWithNonNumeric(f, MODE_INPUT_STREAM);
        _testParsingOfLongerSequencesWithNonNumeric(f, MODE_INPUT_STREAM_THROTTLED);
        _testParsingOfLongerSequencesWithNonNumeric(f, MODE_READER);
        _testParsingOfLongerSequencesWithNonNumeric(f, MODE_DATA_INPUT);
    }

// com.fasterxml.jackson.core.read.NumberParsingTest::testInvalidBooleanAccess
    public void testInvalidBooleanAccess() throws Exception {
        _testInvalidBooleanAccess(MODE_INPUT_STREAM);
        _testInvalidBooleanAccess(MODE_INPUT_STREAM_THROTTLED);
        _testInvalidBooleanAccess(MODE_READER);
        _testInvalidBooleanAccess(MODE_DATA_INPUT);
    }

// com.fasterxml.jackson.core.read.NumberParsingTest::testInvalidIntAccess
    public void testInvalidIntAccess() throws Exception {
        _testInvalidIntAccess(MODE_INPUT_STREAM);
        _testInvalidIntAccess(MODE_INPUT_STREAM_THROTTLED);
        _testInvalidIntAccess(MODE_READER);
        _testInvalidIntAccess(MODE_DATA_INPUT);
    }

// com.fasterxml.jackson.core.read.NumberParsingTest::testInvalidLongAccess
    public void testInvalidLongAccess() throws Exception {
        _testInvalidLongAccess(MODE_INPUT_STREAM);
        _testInvalidLongAccess(MODE_INPUT_STREAM_THROTTLED);
        _testInvalidLongAccess(MODE_READER);
        _testInvalidLongAccess(MODE_DATA_INPUT);
    }

// com.fasterxml.jackson.core.read.NumberParsingTest::testLongerFloatingPoint
    public void testLongerFloatingPoint() throws Exception
    {
        StringBuilder input = new StringBuilder();
        for (int i = 1; i < 201; i++) {
            input.append(1);
        }
        input.append(".0");
        final String DOC = input.toString();

        
        JsonParser p;

        p = FACTORY.createParser(new StringReader(DOC));
        _testLongerFloat(p, DOC);
        p.close();
        
        p = FACTORY.createParser(new ByteArrayInputStream(DOC.getBytes("UTF-8")));
        _testLongerFloat(p, DOC);
        p.close();
    }

// com.fasterxml.jackson.core.read.NumberParsingTest::testInvalidNumber
    public void testInvalidNumber() throws Exception {
        for (int mode : ALL_MODES) {
            JsonParser p = createParser(mode, " -foo ");
            try {
                p.nextToken();
                fail("Should not pass");
            } catch (JsonParseException e) {
                verifyException(e, "Unexpected character ('f'");
            }
            p.close();
        }
    }

// com.fasterxml.jackson.core.read.ParserDupHandlingTest::testSimpleDupCheckDisabled
    public void testSimpleDupCheckDisabled() throws Exception
    {
        
        final JsonFactory f = new JsonFactory();
        assertFalse(f.isEnabled(JsonParser.Feature.STRICT_DUPLICATE_DETECTION));
        for (String doc : DUP_DOCS) {
            _testSimpleDupsOk(doc, f, MODE_INPUT_STREAM);
            _testSimpleDupsOk(doc, f, MODE_INPUT_STREAM_THROTTLED);
            _testSimpleDupsOk(doc, f, MODE_READER);
            _testSimpleDupsOk(doc, f, MODE_DATA_INPUT);
        }
    }

// com.fasterxml.jackson.core.read.ParserDupHandlingTest::testSimpleDupsBytes
    public void testSimpleDupsBytes() throws Exception
    {
        JsonFactory nonDupF = new JsonFactory();
        JsonFactory dupF = new JsonFactory();
        dupF.enable(JsonParser.Feature.STRICT_DUPLICATE_DETECTION);
        for (String doc : DUP_DOCS) {
            
            _testSimpleDupsFail(doc, dupF, MODE_INPUT_STREAM, "a", false);
            
            _testSimpleDupsFail(doc, nonDupF, MODE_INPUT_STREAM, "a", true);

            _testSimpleDupsFail(doc, dupF, MODE_INPUT_STREAM_THROTTLED, "a", false);
            _testSimpleDupsFail(doc, nonDupF, MODE_INPUT_STREAM_THROTTLED, "a", true);
        }
    }

// com.fasterxml.jackson.core.read.ParserDupHandlingTest::testSimpleDupsDataInput
    public void testSimpleDupsDataInput() throws Exception
    {
        JsonFactory nonDupF = new JsonFactory();
        JsonFactory dupF = new JsonFactory();
        dupF.enable(JsonParser.Feature.STRICT_DUPLICATE_DETECTION);
        for (String doc : DUP_DOCS) {
            _testSimpleDupsFail(doc, dupF, MODE_DATA_INPUT, "a", false);
            _testSimpleDupsFail(doc, nonDupF, MODE_DATA_INPUT, "a", true);
        }
    }

// com.fasterxml.jackson.core.read.ParserDupHandlingTest::testSimpleDupsChars
    public void testSimpleDupsChars() throws Exception
    {
        JsonFactory nonDupF = new JsonFactory();
        JsonFactory dupF = new JsonFactory();
        dupF.enable(JsonParser.Feature.STRICT_DUPLICATE_DETECTION);
        for (String doc : DUP_DOCS) {
            _testSimpleDupsFail(doc, dupF, MODE_READER, "a", false);
            _testSimpleDupsFail(doc, nonDupF, MODE_READER, "a", true);
        }
    }

// com.fasterxml.jackson.core.read.ParserErrorHandlingTest::testInvalidKeywordsBytes
    public void testInvalidKeywordsBytes() throws Exception {
        _testInvalidKeywords(MODE_INPUT_STREAM);
        _testInvalidKeywords(MODE_INPUT_STREAM_THROTTLED);
        _testInvalidKeywords(MODE_DATA_INPUT);
    }

// com.fasterxml.jackson.core.read.ParserErrorHandlingTest::testInvalidKeywordsChars
    public void testInvalidKeywordsChars() throws Exception {
        _testInvalidKeywords(MODE_READER);
    }

// com.fasterxml.jackson.core.read.ParserErrorHandlingTest::testMangledIntsBytes
    public void testMangledIntsBytes() throws Exception {
        _testMangledNumbersInt(MODE_INPUT_STREAM);
        _testMangledNumbersInt(MODE_INPUT_STREAM_THROTTLED);

        
        

    }

// com.fasterxml.jackson.core.read.ParserErrorHandlingTest::testMangledFloatsBytes
    public void testMangledFloatsBytes() throws Exception {
        _testMangledNumbersFloat(MODE_INPUT_STREAM);
        _testMangledNumbersFloat(MODE_INPUT_STREAM_THROTTLED);

        
        _testMangledNumbersFloat(MODE_DATA_INPUT);
    }

// com.fasterxml.jackson.core.read.ParserErrorHandlingTest::testMangledNumbersChars
    public void testMangledNumbersChars() throws Exception {
        _testMangledNumbersInt(MODE_READER);
        _testMangledNumbersFloat(MODE_READER);
    }

// com.fasterxml.jackson.core.read.ParserScopeMatchingTest::testUnclosedArray
    public void testUnclosedArray() throws Exception
    {
        for (int mode : ALL_MODES) {
            _testUnclosedArray(mode);
        }
    }

// com.fasterxml.jackson.core.read.ParserScopeMatchingTest::testUnclosedObject
    public void testUnclosedObject() throws Exception
    {
        for (int mode : ALL_MODES) {
            _testUnclosedObject(mode);
        }
    }

// com.fasterxml.jackson.core.read.ParserScopeMatchingTest::testEOFInName
    public void testEOFInName() throws Exception
    {
        for (int mode : ALL_MODES) {
            _testEOFInName(mode);
        }
    }

// com.fasterxml.jackson.core.read.ParserScopeMatchingTest::testWeirdToken
    public void testWeirdToken() throws Exception
    {
        for (int mode : ALL_MODES) {
            _testWeirdToken(mode);
        }
    }

// com.fasterxml.jackson.core.read.ParserScopeMatchingTest::testMismatchArrayToObject
    public void testMismatchArrayToObject() throws Exception
    {
        for (int mode : ALL_MODES) {
            _testMismatchArrayToObject(mode);
        }
    }

// com.fasterxml.jackson.core.read.ParserScopeMatchingTest::testMismatchObjectToArray
    public void testMismatchObjectToArray() throws Exception
    {
        for (int mode : ALL_MODES) {
            _testMismatchObjectToArray(mode);
        }
    }

// com.fasterxml.jackson.core.read.ParserScopeMatchingTest::testMisssingColon
    public void testMisssingColon() throws Exception
    {
        for (int mode : ALL_MODES) {
            _testMisssingColon(mode);
        }
    }

// com.fasterxml.jackson.core.read.ParserSymbolHandlingTest::testSymbolsWithNullBytes
    public void testSymbolsWithNullBytes() throws Exception {
        JsonFactory f = new JsonFactory();
        _testSymbolsWithNull(f, true);
        
        _testSymbolsWithNull(f, true);
    }

// com.fasterxml.jackson.core.read.ParserSymbolHandlingTest::testSymbolsWithNullChars
    public void testSymbolsWithNullChars() throws Exception {
        JsonFactory f = new JsonFactory();
        _testSymbolsWithNull(f, false);
        _testSymbolsWithNull(f, false);
    }

// com.fasterxml.jackson.core.read.UTF32ParseTest::testSimpleEOFs
    public void testSimpleEOFs() throws Exception
    {
        
        byte[] data = { 0x00, 0x00, 0x00, 0x20,
                0x00, 0x00, 0x00, 0x20
        };

        for (int len = 5; len <= 7; ++len) {
            JsonParser parser = FACTORY.createParser(data, 0, len);
            try {
                parser.nextToken();
                fail("Should not pass");
            } catch (CharConversionException e) {
                verifyException(e, "Unexpected EOF");
                verifyException(e, "of a 4-byte UTF-32 char");
            }
            parser.close();
        }
    }

// com.fasterxml.jackson.core.read.UTF32ParseTest::testSimpleInvalidUTF32
    public void testSimpleInvalidUTF32() throws Exception
    {
        
        byte[] data = {
                0x00,
                0x00,
                0x00,
                0x20,
                (byte) 0xFE,
                (byte) 0xFF,
                0x00,
                0x01
        };

        JsonParser parser = FACTORY.createParser(data);

        try {
            parser.nextToken();
            fail("Should not pass");
        } catch (CharConversionException e) {
            verifyException(e, "Invalid UTF-32 character 0xfefe0001");
        }
        parser.close();
    }

// com.fasterxml.jackson.core.read.UTF32ParseTest::testSimpleSevenNullBytes
    public void testSimpleSevenNullBytes() throws Exception {
        byte[] data = new byte[7];
        JsonParser parser = FACTORY.createParser(data);
        try {
            parser.nextToken();
            fail("Should not pass");
        } catch (JsonParseException e) {
            verifyException(e, "Illegal character ((CTRL-CHAR, code 0))");
        }
        parser.close();
    }

// com.fasterxml.jackson.core.read.UTF8NamesParseTest::testEmptyName
    public void testEmptyName() throws Exception
    {
        _testEmptyName(MODE_INPUT_STREAM);
        _testEmptyName(MODE_INPUT_STREAM_THROTTLED);
        _testEmptyName(MODE_DATA_INPUT);
    }

// com.fasterxml.jackson.core.read.UTF8NamesParseTest::testUtf8Name2Bytes
    public void testUtf8Name2Bytes() throws Exception
    {
        _testUtf8Name2Bytes(MODE_INPUT_STREAM);
        _testUtf8Name2Bytes(MODE_INPUT_STREAM_THROTTLED);
        _testUtf8Name2Bytes(MODE_DATA_INPUT);
    }

// com.fasterxml.jackson.core.read.UTF8NamesParseTest::testUtf8Name3Bytes
    public void testUtf8Name3Bytes() throws Exception
    {
        _testUtf8Name3Bytes(MODE_INPUT_STREAM);
        _testUtf8Name3Bytes(MODE_DATA_INPUT);
        _testUtf8Name3Bytes(MODE_INPUT_STREAM_THROTTLED);
    }

// com.fasterxml.jackson.core.read.UTF8NamesParseTest::testUtf8StringTrivial
    public void testUtf8StringTrivial() throws Exception
    {
        _testUtf8StringTrivial(MODE_INPUT_STREAM);
        _testUtf8StringTrivial(MODE_DATA_INPUT);
        _testUtf8StringTrivial(MODE_INPUT_STREAM_THROTTLED);
    }

// com.fasterxml.jackson.core.read.UTF8NamesParseTest::testUtf8StringValue
    public void testUtf8StringValue() throws Exception
    {
        _testUtf8StringValue(MODE_INPUT_STREAM);
        _testUtf8StringValue(MODE_DATA_INPUT);
        _testUtf8StringValue(MODE_INPUT_STREAM_THROTTLED);
    }

// com.fasterxml.jackson.core.read.UTF8NamesParseTest::testNextFieldName
    public void testNextFieldName() throws IOException
    {
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		os.write('{');
		for (int i = 0; i < 3994; i++) {
			os.write(' ');
		}
		os.write("\"id\":2".getBytes("UTF-8"));
		os.write('}');
		byte[] data = os.toByteArray();

		_testNextFieldName(MODE_INPUT_STREAM, data);
          _testNextFieldName(MODE_DATA_INPUT, data);
          _testNextFieldName(MODE_INPUT_STREAM_THROTTLED, data);
    }

// com.fasterxml.jackson.core.read.ValueConversionsTest::testAsInt
    public void testAsInt() throws Exception
    {
        for (int mode : ALL_MODES) {
            _testAsInt(mode);
        }
    }

// com.fasterxml.jackson.core.read.ValueConversionsTest::testAsBoolean
    public void testAsBoolean() throws Exception
    {
        for (int mode : ALL_MODES) {
            _testAsBoolean(mode);
        }
    }

// com.fasterxml.jackson.core.read.ValueConversionsTest::testAsLong
    public void testAsLong() throws Exception
    {
        for (int mode : ALL_MODES) {
            _testAsLong(mode);
        }
    }

// com.fasterxml.jackson.core.read.ValueConversionsTest::testAsDouble
    public void testAsDouble() throws Exception
    {
        for (int mode : ALL_MODES) {
            _testAsDouble(mode);
        }
    }

// com.fasterxml.jackson.core.sym.SymbolTableMergingTest::testByteSymbolsWithClose
    public void testByteSymbolsWithClose() throws Exception
    {
        _testWithClose(true);
    }

// com.fasterxml.jackson.core.sym.SymbolTableMergingTest::testByteSymbolsWithEOF
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

// com.fasterxml.jackson.core.sym.SymbolTableMergingTest::testHashCalc
    public void testHashCalc() throws Exception
    {
        CharsToNameCanonicalizer sym = CharsToNameCanonicalizer.createRoot(123);
        char[] str1 = "foo".toCharArray();
        char[] str2 = " foo ".toCharArray();

        assertEquals(sym.calcHash(str1, 0, 3), sym.calcHash(str2, 1, 3));
    }

// com.fasterxml.jackson.core.sym.SymbolTableMergingTest::testCharSymbolsWithClose
    public void testCharSymbolsWithClose() throws Exception
    {
        _testWithClose(false);
    }

// com.fasterxml.jackson.core.sym.SymbolTableMergingTest::testCharSymbolsWithEOF
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

// com.fasterxml.jackson.core.sym.SymbolsViaParserTest::test17CharSymbols
    public void test17CharSymbols() throws Exception {
        _test17Chars(false);
    }

// com.fasterxml.jackson.core.sym.SymbolsViaParserTest::test17ByteSymbols
    public void test17ByteSymbols() throws Exception {
        _test17Chars(true);
    }

// com.fasterxml.jackson.core.sym.SymbolsViaParserTest::testSymbolTableExpansionChars
    public void testSymbolTableExpansionChars() throws Exception {
        _testSymbolTableExpansion(false);
    }

// com.fasterxml.jackson.core.sym.SymbolsViaParserTest::testSymbolTableExpansionBytes
    public void testSymbolTableExpansionBytes() throws Exception {
        _testSymbolTableExpansion(true);
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

// com.fasterxml.jackson.core.sym.TestByteBasedSymbols::testIssue207
    public void testIssue207() throws Exception
    {
        ByteQuadsCanonicalizer nc = ByteQuadsCanonicalizer.createRoot(-523743345);
        Field byteSymbolCanonicalizerField = JsonFactory.class.getDeclaredField("_byteSymbolCanonicalizer");
        byteSymbolCanonicalizerField.setAccessible(true);
        JsonFactory jsonF = new JsonFactory();
        byteSymbolCanonicalizerField.set(jsonF, nc);

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("{\n");
        stringBuilder.append("    \"expectedGCperPosition\": null");
        for (int i = 0; i < 60; ++i) {
            stringBuilder.append(",\n    \"").append(i + 1).append("\": null");
        }
        stringBuilder.append("\n}");

        JsonParser p = jsonF.createParser(stringBuilder.toString().getBytes("UTF-8"));
        while (p.nextToken() != null) { }
        p.close();
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

// com.fasterxml.jackson.core.sym.TestSymbolTables::testSyntheticWithChars
    public void testSyntheticWithChars()
    {
        
        CharsToNameCanonicalizer symbols = CharsToNameCanonicalizer.createRoot(1).makeChild(-1);
        final int COUNT = 12000;
        for (int i = 0; i < COUNT; ++i) {
            String id = fieldNameFor(i);
            char[] ch = id.toCharArray();
            symbols.findSymbol(ch, 0, ch.length, symbols.calcHash(id));
        }

        assertEquals(16384, symbols.bucketCount());
        assertEquals(COUNT, symbols.size());
        

        
        
        
        
        assertEquals(3431, symbols.collisionCount());

        assertEquals(6, symbols.maxCollisionLength());
    }

// com.fasterxml.jackson.core.sym.TestSymbolTables::testSyntheticWithBytesNew
    public void testSyntheticWithBytesNew() throws IOException
    {
        
        final int SEED = 33333;
        ByteQuadsCanonicalizer symbols =
                ByteQuadsCanonicalizer.createRoot(SEED).makeChild(JsonFactory.Feature.collectDefaults());

        final int COUNT = 12000;
        for (int i = 0; i < COUNT; ++i) {
            String id = fieldNameFor(i);
            int[] quads = calcQuads(id.getBytes("UTF-8"));
            symbols.addName(id, quads, quads.length);
        }
        assertEquals(COUNT, symbols.size());
        assertEquals(16384, symbols.bucketCount());
        
        
        
        assertEquals(8534, symbols.primaryCount());
        
        assertEquals(2534, symbols.secondaryCount());
        
        assertEquals(932, symbols.tertiaryCount());
        
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
        assertEquals(4761, symbolsB.primaryCount()); 
        assertEquals(1190, symbolsB.secondaryCount()); 
        assertEquals(299, symbolsB.tertiaryCount()); 
        assertEquals(0, symbolsB.spilloverCount()); 
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

// com.fasterxml.jackson.core.sym.TestSymbolTables::testCollisionsWithChars187
    public void testCollisionsWithChars187() throws IOException
    {
        CharsToNameCanonicalizer symbols = CharsToNameCanonicalizer.createRoot(1).makeChild(-1);
        final int COUNT = 30000;
        for (int i = 0; i < COUNT; ++i) {
            String id = String.valueOf(10000 + i);
            char[] ch = id.toCharArray();
            symbols.findSymbol(ch, 0, ch.length, symbols.calcHash(id));
        }
        assertEquals(COUNT, symbols.size());
        assertEquals(65536, symbols.bucketCount());

        
        assertEquals(7127, symbols.collisionCount());
        
        assertEquals(4, symbols.maxCollisionLength());
    }

// com.fasterxml.jackson.core.sym.TestSymbolTables::testCollisionsWithBytesNew187a
    public void testCollisionsWithBytesNew187a() throws IOException
    {
        ByteQuadsCanonicalizer symbols =
                ByteQuadsCanonicalizer.createRoot(1).makeChild(JsonFactory.Feature.collectDefaults());

        final int COUNT = 43000;
        for (int i = 0; i < COUNT; ++i) {
            String id = String.valueOf(10000 + i);
            int[] quads = calcQuads(id.getBytes("UTF-8"));
            symbols.addName(id, quads, quads.length);
        }

        assertEquals(COUNT, symbols.size());
        assertEquals(65536, symbols.bucketCount());

        
        assertEquals(32342, symbols.primaryCount());
        assertEquals(8863, symbols.secondaryCount());
        assertEquals(1795, symbols.tertiaryCount());

        
        assertEquals(0, symbols.spilloverCount());
    }

// com.fasterxml.jackson.core.sym.TestSymbolTables::testCollisionsWithBytesNew187b
    public void testCollisionsWithBytesNew187b() throws IOException
    {
        ByteQuadsCanonicalizer symbols =
                ByteQuadsCanonicalizer.createRoot(1).makeChild(JsonFactory.Feature.collectDefaults());

        final int COUNT = 10000;
        for (int i = 0; i < COUNT; ++i) {
            String id = String.valueOf(i);
            int[] quads = calcQuads(id.getBytes("UTF-8"));
            symbols.addName(id, quads, quads.length);
        }
        assertEquals(COUNT, symbols.size());
        
        assertEquals(16384, symbols.bucketCount());

        
        
        assertEquals(5402, symbols.primaryCount());
        
        assertEquals(2744, symbols.secondaryCount());
        
        assertEquals(1834, symbols.tertiaryCount());
        
        assertEquals(20, symbols.spilloverCount());
    }

// com.fasterxml.jackson.core.sym.TestSymbolTables::testShortNameCollisionsViaParser
    public void testShortNameCollisionsViaParser() throws Exception
    {
        JsonFactory f = new JsonFactory();
        String json = _shortDoc191();
        JsonParser p;

        
        p = f.createParser(json);
        while (p.nextToken() != null) { }
        p.close();

        
        p = f.createParser(json.getBytes("UTF-8"));
        while (p.nextToken() != null) { }
        p.close();
    }

// com.fasterxml.jackson.core.sym.TestSymbolTables::testShortQuotedDirectChars
    public void testShortQuotedDirectChars() throws IOException
    {
        final int COUNT = 400;
        
        CharsToNameCanonicalizer symbols = CharsToNameCanonicalizer.createRoot(1).makeChild(-1);
        for (int i = 0; i < COUNT; ++i) {
            String id = String.format("\\u%04x", i);
            char[] ch = id.toCharArray();
            symbols.findSymbol(ch, 0, ch.length, symbols.calcHash(id));
        }
        assertEquals(COUNT, symbols.size());
        assertEquals(1024, symbols.bucketCount());

        assertEquals(50, symbols.collisionCount());
        assertEquals(2, symbols.maxCollisionLength());
    }

// com.fasterxml.jackson.core.sym.TestSymbolTables::testShortQuotedDirectBytes
    public void testShortQuotedDirectBytes() throws IOException
    {
        final int COUNT = 400;
        ByteQuadsCanonicalizer symbols =
                ByteQuadsCanonicalizer.createRoot(123).makeChild(JsonFactory.Feature.collectDefaults());
        for (int i = 0; i < COUNT; ++i) {
            String id = String.format("\\u%04x", i);
            int[] quads = calcQuads(id.getBytes("UTF-8"));
            symbols.addName(id, quads, quads.length);
        }
        assertEquals(COUNT, symbols.size());
        assertEquals(512, symbols.bucketCount());

        assertEquals(285, symbols.primaryCount());
        assertEquals(90, symbols.secondaryCount());
        assertEquals(25, symbols.tertiaryCount());
        assertEquals(0, symbols.spilloverCount());
    }

// com.fasterxml.jackson.core.sym.TestSymbolTables::testShortNameCollisionsDirect
    public void testShortNameCollisionsDirect() throws IOException
    {
        final int COUNT = 600;

        
        {
            CharsToNameCanonicalizer symbols = CharsToNameCanonicalizer.createRoot(1).makeChild(-1);
            for (int i = 0; i < COUNT; ++i) {
                String id = String.valueOf((char) i);
                char[] ch = id.toCharArray();
                symbols.findSymbol(ch, 0, ch.length, symbols.calcHash(id));
            }
            assertEquals(COUNT, symbols.size());
            assertEquals(1024, symbols.bucketCount());
    
            assertEquals(16, symbols.collisionCount());
            assertEquals(1, symbols.maxCollisionLength());
        }
    }

// com.fasterxml.jackson.core.sym.TestSymbolTables::testShortNameCollisionsDirectNew
    public void testShortNameCollisionsDirectNew() throws IOException
    {
        final int COUNT = 700;
        {
            ByteQuadsCanonicalizer symbols =
                    ByteQuadsCanonicalizer.createRoot(333).makeChild(JsonFactory.Feature.collectDefaults());
            for (int i = 0; i < COUNT; ++i) {
                String id = String.valueOf((char) i);
                int[] quads = calcQuads(id.getBytes("UTF-8"));
                symbols.addName(id, quads, quads.length);
            }
            assertEquals(COUNT, symbols.size());

            assertEquals(1024, symbols.bucketCount());

            
            assertEquals(564, symbols.primaryCount());
            assertEquals(122, symbols.secondaryCount());
            assertEquals(14, symbols.tertiaryCount());
            assertEquals(0, symbols.spilloverCount());

            assertEquals(COUNT,
                    symbols.primaryCount() + symbols.secondaryCount() + symbols.tertiaryCount() + symbols.spilloverCount());
        }
    }

// com.fasterxml.jackson.core.sym.TestSymbolTables::testLongSymbols17Bytes
    public void testLongSymbols17Bytes() throws Exception
    {
        ByteQuadsCanonicalizer symbolsB =
                ByteQuadsCanonicalizer.createRoot(3).makeChild(JsonFactory.Feature.collectDefaults());
        CharsToNameCanonicalizer symbolsC = CharsToNameCanonicalizer.createRoot(3).makeChild(-1);

        for (int i = 1001; i <= 1050; ++i) {
            String id = "lengthmatters"+i;
            int[] quads = calcQuads(id.getBytes("UTF-8"));
            symbolsB.addName(id, quads, quads.length);
            char[] idChars = id.toCharArray();
            symbolsC.findSymbol(idChars, 0, idChars.length, symbolsC.calcHash(id));
        }
        assertEquals(50, symbolsB.size());
        assertEquals(50, symbolsC.size());
    }

// com.fasterxml.jackson.core.sym.TestSymbolsWithMediaItem::testSmallSymbolSetWithBytes
    public void testSmallSymbolSetWithBytes() throws IOException
    {
        final int SEED = 33333;

        ByteQuadsCanonicalizer symbolsRoot = ByteQuadsCanonicalizer.createRoot(SEED);
        ByteQuadsCanonicalizer symbols = symbolsRoot.makeChild(JsonFactory.Feature.collectDefaults());
        JsonFactory f = new JsonFactory();
        JsonParser p = f.createParser(JSON.getBytes("UTF-8"));

        JsonToken t;
        while ((t = p.nextToken()) != null) {
            if (t != JsonToken.FIELD_NAME) {
                continue;
            }
            String name = p.getCurrentName();
            int[] quads = calcQuads(name.getBytes("UTF-8"));

            if (symbols.findName(quads, quads.length) != null) {
                continue;
            }
            symbols.addName(name, quads, quads.length);
        }
        p.close();
        
        assertEquals(13, symbols.size());
        assertEquals(12, symbols.primaryCount()); 
        assertEquals(1, symbols.secondaryCount()); 
        assertEquals(0, symbols.tertiaryCount()); 
        assertEquals(0, symbols.spilloverCount()); 
    }

// com.fasterxml.jackson.core.sym.TestSymbolsWithMediaItem::testSmallSymbolSetWithChars
    public void testSmallSymbolSetWithChars() throws IOException
    {
        final int SEED = 33333;

        CharsToNameCanonicalizer symbols = CharsToNameCanonicalizer.createRoot(SEED).makeChild(-1);
        JsonFactory f = new JsonFactory();
        JsonParser p = f.createParser(JSON);

        JsonToken t;
        while ((t = p.nextToken()) != null) {
            if (t != JsonToken.FIELD_NAME) {
                continue;
            }
            String name = p.getCurrentName();
            char[] ch = name.toCharArray();
            symbols.findSymbol(ch, 0, ch.length, symbols.calcHash(name));
        }
        p.close();
        
        assertEquals(13, symbols.size());
        assertEquals(13, symbols.size());
        assertEquals(64, symbols.bucketCount());

        
        
        assertEquals(0, symbols.collisionCount());
        assertEquals(0, symbols.maxCollisionLength());
    }

// com.fasterxml.jackson.core.type.TypeReferenceTest::testSimple
    public void testSimple()
    {
        TypeReference<?> ref = new TypeReference<List<String>>() { };
        assertNotNull(ref);
        ref.equals(null);
    }

// com.fasterxml.jackson.core.type.TypeReferenceTest::testInvalid
    public void testInvalid()
    {
        try { 
            Object ob = new TypeReference() { };
            fail("Should not pass, got: "+ob);
        } catch (IllegalArgumentException e) {
            verifyException(e, "without actual type information");
        }
    }

// com.fasterxml.jackson.core.type.TypeReferenceTest::testResolvedType
    public void testResolvedType() {
        ResolvedType type1 = new BogusResolvedType(false);
        assertFalse(type1.isReferenceType());
        ResolvedType type2 = new BogusResolvedType(true);
        assertTrue(type2.isReferenceType());
    }

// com.fasterxml.jackson.core.util.ByteArrayBuilderTest::testSimple
    public void testSimple() throws Exception
    {
        ByteArrayBuilder b = new ByteArrayBuilder(null, 20);
        Assert.assertArrayEquals(new byte[0], b.toByteArray());

        b.write((byte) 0);
        b.append(1);

        byte[] foo = new byte[98];
        for (int i = 0; i < foo.length; ++i) {
            foo[i] = (byte) (2 + i);
        }
        b.write(foo);

        byte[] result = b.toByteArray();
        assertEquals(100, result.length);
        for (int i = 0; i < 100; ++i) {
            assertEquals(i, (int) result[i]);
        }
        
        b.release();
        b.close();
    }

// com.fasterxml.jackson.core.util.TestCharTypes::testQuoting
    public void testQuoting()
    {
        StringBuilder sb = new StringBuilder();
        CharTypes.appendQuoted(sb, "\n");
        assertEquals("\\n", sb.toString());
        sb = new StringBuilder();
        CharTypes.appendQuoted(sb, "\u0000");
        assertEquals("\\u0000", sb.toString());
    }

// com.fasterxml.jackson.core.util.TestDefaultPrettyPrinter::testSystemLinefeed
    public void testSystemLinefeed() throws IOException
    {
        PrettyPrinter pp = new DefaultPrettyPrinter();
        String LF = System.getProperty("line.separator");
        String EXP = "{" + LF +
            "  \"name\" : \"John Doe\"," + LF +
            "  \"age\" : 3.14" + LF +
            "}";
        assertEquals(EXP, _printTestData(pp, false));
        assertEquals(EXP, _printTestData(pp, true));
    }

// com.fasterxml.jackson.core.util.TestDefaultPrettyPrinter::testWithLineFeed
    public void testWithLineFeed() throws IOException
    {
        PrettyPrinter pp = new DefaultPrettyPrinter()
        .withObjectIndenter(new DefaultIndenter().withLinefeed("\n"));
        String EXP = "{\n" +
            "  \"name\" : \"John Doe\",\n" +
            "  \"age\" : 3.14\n" +
            "}";
        assertEquals(EXP, _printTestData(pp, false));
        assertEquals(EXP, _printTestData(pp, true));
    }

// com.fasterxml.jackson.core.util.TestDefaultPrettyPrinter::testWithIndent
    public void testWithIndent() throws IOException
    {
        PrettyPrinter pp = new DefaultPrettyPrinter()
        .withObjectIndenter(new DefaultIndenter().withLinefeed("\n").withIndent(" "));
        String EXP = "{\n" +
            " \"name\" : \"John Doe\",\n" +
            " \"age\" : 3.14\n" +
            "}";
        assertEquals(EXP, _printTestData(pp, false));
        assertEquals(EXP, _printTestData(pp, true));
    }

// com.fasterxml.jackson.core.util.TestDefaultPrettyPrinter::testUnixLinefeed
    public void testUnixLinefeed() throws IOException
    {
        PrettyPrinter pp = new DefaultPrettyPrinter()
                .withObjectIndenter(new DefaultIndenter("  ", "\n"));
        String EXP = "{\n" +
            "  \"name\" : \"John Doe\",\n" +
            "  \"age\" : 3.14\n" +
            "}";
        assertEquals(EXP, _printTestData(pp, false));
        assertEquals(EXP, _printTestData(pp, true));
    }

// com.fasterxml.jackson.core.util.TestDefaultPrettyPrinter::testWindowsLinefeed
    public void testWindowsLinefeed() throws IOException
    {
        PrettyPrinter pp = new DefaultPrettyPrinter()
        .withObjectIndenter(new DefaultIndenter("  ", "\r\n"));
        String EXP = "{\r\n" +
            "  \"name\" : \"John Doe\",\r\n" +
            "  \"age\" : 3.14\r\n" +
            "}";
        assertEquals(EXP, _printTestData(pp, false));
        assertEquals(EXP, _printTestData(pp, true));
    }

// com.fasterxml.jackson.core.util.TestDefaultPrettyPrinter::testTabIndent
    public void testTabIndent() throws IOException
    {
        PrettyPrinter pp = new DefaultPrettyPrinter()
        .withObjectIndenter(new DefaultIndenter("\t", "\n"));
        String EXP = "{\n" +
            "\t\"name\" : \"John Doe\",\n" +
            "\t\"age\" : 3.14\n" +
            "}";
        assertEquals(EXP, _printTestData(pp, false));
        assertEquals(EXP, _printTestData(pp, true));
    }

// com.fasterxml.jackson.core.util.TestDefaultPrettyPrinter::testRootSeparator
    public void testRootSeparator() throws IOException
    {
        DefaultPrettyPrinter pp = new DefaultPrettyPrinter()
            .withRootSeparator("|");
        final String EXP = "1|2|3";

        StringWriter sw = new StringWriter();
        JsonGenerator gen = JSON_F.createGenerator(sw);
        gen.setPrettyPrinter(pp);

        gen.writeNumber(1);
        gen.writeNumber(2);
        gen.writeNumber(3);
        gen.close();
        assertEquals(EXP, sw.toString());

        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        gen = JSON_F.createGenerator(bytes);
        gen.setPrettyPrinter(pp);

        gen.writeNumber(1);
        gen.writeNumber(2);
        gen.writeNumber(3);
        gen.close();
        assertEquals(EXP, bytes.toString("UTF-8"));

        
        pp = pp.withRootSeparator((String) null)
                .withArrayIndenter(null)
                .withObjectIndenter(null)
                .withoutSpacesInObjectEntries();
        sw = new StringWriter();
        gen = JSON_F.createGenerator(sw);
        gen.setPrettyPrinter(pp);

        gen.writeNumber(1);
        gen.writeStartArray();
        gen.writeNumber(2);
        gen.writeEndArray();
        gen.writeStartObject();
        gen.writeFieldName("a");
        gen.writeNumber(3);
        gen.writeEndObject();
        gen.close();
        
        assertEquals("1[2]{\"a\":3}", sw.toString());
    }

// com.fasterxml.jackson.core.util.TestDelegates::testParserDelegate
    public void testParserDelegate() throws IOException
    {
        final String TOKEN ="foo";

        JsonParser parser = JSON_F.createParser("[ 1, true, null, { } ]");
        JsonParserDelegate del = new JsonParserDelegate(parser);
        
        assertNull(del.currentToken());
        assertToken(JsonToken.START_ARRAY, del.nextToken());
        assertEquals("[", del.getText());
        assertToken(JsonToken.VALUE_NUMBER_INT, del.nextToken());
        assertEquals(1, del.getIntValue());

        assertToken(JsonToken.VALUE_TRUE, del.nextToken());
        assertTrue(del.getBooleanValue());

        assertToken(JsonToken.VALUE_NULL, del.nextToken());
        assertNull(del.getCurrentValue());
        del.setCurrentValue(TOKEN);

        assertToken(JsonToken.START_OBJECT, del.nextToken());
        assertNull(del.getCurrentValue());

        assertToken(JsonToken.END_OBJECT, del.nextToken());
        assertEquals(TOKEN, del.getCurrentValue());

        assertToken(JsonToken.END_ARRAY, del.nextToken());

        del.close();
        assertTrue(del.isClosed());
        assertTrue(parser.isClosed());

        parser.close();
    }

// com.fasterxml.jackson.core.util.TestDelegates::testGeneratorDelegate
    public void testGeneratorDelegate() throws IOException
    {
        final String TOKEN ="foo";

        StringWriter sw = new StringWriter();
        JsonGenerator g0 = JSON_F.createGenerator(sw);
        JsonGeneratorDelegate del = new JsonGeneratorDelegate(g0);
        del.writeStartArray();

        assertEquals(1, del.getOutputBuffered());
        
        del.writeNumber(13);
        del.writeNull();
        del.writeBoolean(false);
        del.writeString("foo");

        
        assertNull(del.getCurrentValue());
        del.setCurrentValue(TOKEN);

        del.writeStartObject();
        assertNull(del.getCurrentValue());
        del.writeEndObject();
        assertEquals(TOKEN, del.getCurrentValue());

        del.writeStartArray(0);
        del.writeEndArray();

        del.writeEndArray();
        
        del.flush();
        del.close();
        assertTrue(del.isClosed());        
        assertTrue(g0.isClosed());        
        assertEquals("[13,null,false,\"foo\",{},[]]", sw.toString());

        g0.close();
    }

// com.fasterxml.jackson.core.util.TestDelegates::testNotDelegateCopyMethods
    public void testNotDelegateCopyMethods() throws IOException
    {
        JsonParser jp = JSON_F.createParser("[{\"a\":[1,2,{\"b\":3}],\"c\":\"d\"},{\"e\":false},null]");
        StringWriter sw = new StringWriter();
        JsonGenerator jg = new JsonGeneratorDelegate(JSON_F.createGenerator(sw), false) {
            @Override
            public void writeFieldName(String name) throws IOException {
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

// com.fasterxml.jackson.core.util.TestDelegates::testGeneratorWithCodec
    public void testGeneratorWithCodec() throws IOException
    {
        BogusCodec codec = new BogusCodec();
        StringWriter sw = new StringWriter();
        JsonGenerator g0 = JSON_F.createGenerator(sw);
        g0.setCodec(codec);
        JsonGeneratorDelegate del = new JsonGeneratorDelegate(g0, false);
        del.writeStartArray();
        POJO pojo = new POJO();
        del.writeObject(pojo);
        TreeNode tree = new BogusTree();
        del.writeTree(tree);
        del.writeEndArray();
        del.close();

        assertEquals("[\"pojo\",\"tree\"]", sw.toString());

        assertSame(tree, codec.treeWritten);
        assertSame(pojo, codec.pojoWritten);
    }

// com.fasterxml.jackson.core.util.TestNumberPrinting::testIntPrinting
    public void testIntPrinting() throws Exception
    {
        assertIntPrint(0);
        assertIntPrint(-3);
        assertIntPrint(1234);
        assertIntPrint(-1234);
        assertIntPrint(56789);
        assertIntPrint(-56789);
        assertIntPrint(999999);
        assertIntPrint(-999999);
        assertIntPrint(1000000);
        assertIntPrint(-1000000);
        assertIntPrint(10000001);
        assertIntPrint(-10000001);
        assertIntPrint(-100000012);
        assertIntPrint(100000012);
        assertIntPrint(1999888777);
        assertIntPrint(-1999888777);
        assertIntPrint(Integer.MAX_VALUE);
        assertIntPrint(Integer.MIN_VALUE);

        Random rnd = new Random(12345L);
        for (int i = 0; i < 251000; ++i) {
            assertIntPrint(rnd.nextInt());
        }
    }

// com.fasterxml.jackson.core.util.TestNumberPrinting::testLongPrinting
    public void testLongPrinting() throws Exception
    {
        
        assertLongPrint(0L, 0);
        assertLongPrint(1L, 0);
        assertLongPrint(-1L, 0);
        assertLongPrint(Long.MAX_VALUE, 0);
        assertLongPrint(Long.MIN_VALUE, 0);
        assertLongPrint(Long.MAX_VALUE-1L, 0);
        assertLongPrint(Long.MIN_VALUE+1L, 0);

        Random rnd = new Random(12345L);
        
        for (int i = 0; i < 678000; ++i) {
            long l = ((long) rnd.nextInt() << 32) | (long) rnd.nextInt();
            assertLongPrint(l, i);
        }
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
        
        assertTrue(tb.hasTextAsCharacters());

        assertEquals(3, tb.contentsAsArray().length);
        assertEquals("abc", tb.toString());

        assertNotNull(tb.expandCurrentSegment());
    }

// com.fasterxml.jackson.core.util.TestTextBuffer::testLonger
    public void testLonger()
    {
        TextBuffer tb = new TextBuffer(null);
        for (int i = 0; i < 2000; ++i) {
            tb.append("abc", 0, 3);
        }
        String str = tb.contentsAsString();
        assertEquals(6000, str.length());
        assertEquals(6000, tb.contentsAsArray().length);

        tb.resetWithShared(new char[] { 'a' }, 0, 1);
        assertEquals(1, tb.toString().length());
        assertTrue(tb.hasTextAsCharacters());
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
        tb.resetWithString("Foobar");
        assertEquals("Foobar", tb.contentsAsString());
    }

// com.fasterxml.jackson.core.util.TestTextBuffer::testEmpty
    public void testEmpty() {
        TextBuffer tb = new TextBuffer(new BufferRecycler());
        tb.resetWithEmpty();

        assertTrue(tb.getTextBuffer().length == 0);
        tb.contentsAsString();
        assertTrue(tb.getTextBuffer().length == 0);
    }

// com.fasterxml.jackson.core.util.TestTextBuffer::testResetWithAndSetCurrentAndReturn
    public void testResetWithAndSetCurrentAndReturn() {
        TextBuffer textBuffer = new TextBuffer(null);
        textBuffer.resetWith('l');
        textBuffer.setCurrentAndReturn(349);
    }

// com.fasterxml.jackson.core.util.TestTextBuffer::testGetCurrentSegment
    public void testGetCurrentSegment() {
        TextBuffer textBuffer = new TextBuffer(null);
        textBuffer.emptyAndGetCurrentSegment();
        textBuffer.setCurrentAndReturn(1000);
        textBuffer.getCurrentSegment();

        assertEquals(1000, textBuffer.size());
    }

// com.fasterxml.jackson.core.util.TestTextBuffer::testAppendTakingTwoAndThreeInts
    public void testAppendTakingTwoAndThreeInts() {
        BufferRecycler bufferRecycler = new BufferRecycler();
        TextBuffer textBuffer = new TextBuffer(bufferRecycler);
        textBuffer.ensureNotShared();
        char[] charArray = textBuffer.getTextBuffer();
        textBuffer.append(charArray, 0, 200);
        textBuffer.append("5rmk0rx(C@aVYGN@Q", 2, 3);

        assertEquals(3, textBuffer.getCurrentSegmentSize());
    }

// com.fasterxml.jackson.core.util.TestTextBuffer::testEnsureNotSharedAndResetWithString
    public void testEnsureNotSharedAndResetWithString() {
        BufferRecycler bufferRecycler = new BufferRecycler();
        TextBuffer textBuffer = new TextBuffer(bufferRecycler);
        textBuffer.resetWithString("");

        assertFalse(textBuffer.hasTextAsCharacters());

        textBuffer.ensureNotShared();

        assertEquals(0, textBuffer.getCurrentSegmentSize());
    }

// com.fasterxml.jackson.core.util.TestTextBuffer::testContentsAsDecimalThrowsNumberFormatException
    public void testContentsAsDecimalThrowsNumberFormatException() {
        TextBuffer textBuffer = new TextBuffer( null);

        try {
            textBuffer.contentsAsDecimal();
            fail("Expecting exception: NumberFormatException");
        } catch(NumberFormatException e) {
            assertEquals(NumberInput.class.getName(), e.getStackTrace()[0].getClassName());
        }
    }

// com.fasterxml.jackson.core.util.TestTextBuffer::testGetTextBufferAndEmptyAndGetCurrentSegmentAndFinishCurrentSegment
    public void testGetTextBufferAndEmptyAndGetCurrentSegmentAndFinishCurrentSegment() {
        BufferRecycler bufferRecycler = new BufferRecycler();
        TextBuffer textBuffer = new TextBuffer(bufferRecycler);
        textBuffer.emptyAndGetCurrentSegment();
        textBuffer.finishCurrentSegment();
        textBuffer.getTextBuffer();

        assertEquals(200, textBuffer.size());
    }

// com.fasterxml.jackson.core.util.TestTextBuffer::testGetTextBufferAndAppendTakingCharAndContentsAsArray
    public void testGetTextBufferAndAppendTakingCharAndContentsAsArray() {
        BufferRecycler bufferRecycler = new BufferRecycler();
        TextBuffer textBuffer = new TextBuffer(bufferRecycler);
        textBuffer.append('(');
        textBuffer.contentsAsArray();
        textBuffer.getTextBuffer();

        assertEquals(1, textBuffer.getCurrentSegmentSize());
    }

// com.fasterxml.jackson.core.util.TestTextBuffer::testGetTextBufferAndResetWithString
    public void testGetTextBufferAndResetWithString() {
        BufferRecycler bufferRecycler = new BufferRecycler();
        TextBuffer textBuffer = new TextBuffer(bufferRecycler);
        textBuffer.resetWithString("");

        assertFalse(textBuffer.hasTextAsCharacters());

        textBuffer.getTextBuffer();

        assertTrue(textBuffer.hasTextAsCharacters());
    }

// com.fasterxml.jackson.core.util.TestTextBuffer::testResetWithString
    public void testResetWithString() {
        BufferRecycler bufferRecycler = new BufferRecycler();
        TextBuffer textBuffer = new TextBuffer(bufferRecycler);
        textBuffer.ensureNotShared();
        textBuffer.finishCurrentSegment();

        assertEquals(200, textBuffer.size());

        textBuffer.resetWithString("asdf");

        assertEquals(0, textBuffer.getTextOffset());
    }

// com.fasterxml.jackson.core.util.TestTextBuffer::testGetCurrentSegmentSizeResetWith
    public void testGetCurrentSegmentSizeResetWith() {
        TextBuffer textBuffer = new TextBuffer(null);
        textBuffer.resetWith('.');
        textBuffer.resetWith('q');

        assertEquals(1, textBuffer.getCurrentSegmentSize());
    }

// com.fasterxml.jackson.core.util.TestTextBuffer::testGetSizeFinishCurrentSegmentAndResetWith
    public void testGetSizeFinishCurrentSegmentAndResetWith() {
        TextBuffer textBuffer = new TextBuffer(null);
        textBuffer.resetWith('.');
        textBuffer.finishCurrentSegment();
        textBuffer.resetWith('q');

        assertEquals(2, textBuffer.size());
    }

// com.fasterxml.jackson.core.util.TestVersionUtil::testVersionPartParsing
    public void testVersionPartParsing()
    {
        assertEquals(13, VersionUtil.parseVersionPart("13"));
        assertEquals(27, VersionUtil.parseVersionPart("27.8"));
        assertEquals(0, VersionUtil.parseVersionPart("-3"));
    }

// com.fasterxml.jackson.core.util.TestVersionUtil::testVersionParsing
    public void testVersionParsing()
    {
        assertEquals(new Version(1, 2, 15, "foo", "group", "artifact"),
                VersionUtil.parseVersion("1.2.15-foo", "group", "artifact"));
    }

// com.fasterxml.jackson.core.util.TestVersionUtil::testMavenVersionParsing
    public void testMavenVersionParsing() {
        assertEquals(new Version(1, 2, 3, "SNAPSHOT", "foo.bar", "foo-bar"),
                VersionUtil.mavenVersionFor(TestVersionUtil.class.getClassLoader(), "foo.bar", "foo-bar"));
    }

// com.fasterxml.jackson.core.util.TestVersionUtil::testPackageVersionMatches
    public void testPackageVersionMatches() {
        assertEquals(PackageVersion.VERSION, VersionUtil.versionFor(UTF8JsonGenerator.class));
    }

// com.fasterxml.jackson.core.util.TestVersionUtil::testVersionForUnknownVersion
    public void testVersionForUnknownVersion() {
        
        assertEquals(Version.unknownVersion(), VersionUtil.versionFor(TestVersionUtil.class));
    }

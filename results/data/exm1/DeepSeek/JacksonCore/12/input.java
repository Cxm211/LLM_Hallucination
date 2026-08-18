// buggy code
    public final JsonToken nextToken() throws IOException
    {
        /* First: field names are special -- we will always tokenize
         * (part of) value along with field name to simplify
         * state handling. If so, can and need to use secondary token:
         */
        if (_currToken == JsonToken.FIELD_NAME) {
            return _nextAfterName();
        }
        // But if we didn't already have a name, and (partially?) decode number,
        // need to ensure no numeric information is leaked
        _numTypesValid = NR_UNKNOWN;
        if (_tokenIncomplete) {
            _skipString(); // only strings can be partial
        }
        int i = _skipWSOrEnd();
        if (i < 0) { // end-of-input
            /* 19-Feb-2009, tatu: Should actually close/release things
             *    like input source, symbol table and recyclable buffers now.
             */
            close();
            return (_currToken = null);
        }
        // clear any data retained so far
        _binaryValue = null;

        // Closing scope?
        if (i == INT_RBRACKET) {
            _updateLocation();
            if (!_parsingContext.inArray()) {
                _reportMismatchedEndMarker(i, '}');
            }
            _parsingContext = _parsingContext.getParent();
            return (_currToken = JsonToken.END_ARRAY);
        }
        if (i == INT_RCURLY) {
            _updateLocation();
            if (!_parsingContext.inObject()) {
                _reportMismatchedEndMarker(i, ']');
            }
            _parsingContext = _parsingContext.getParent();
            return (_currToken = JsonToken.END_OBJECT);
        }

        // Nope: do we then expect a comma?
        if (_parsingContext.expectComma()) {
            i = _skipComma(i);
        }
        _updateLocation();

        /* And should we now have a name? Always true for Object contexts, since
         * the intermediate 'expect-value' state is never retained.
         */
        boolean inObject = _parsingContext.inObject();
        if (inObject) {
            // First, field name itself:
            String name = (i == INT_QUOTE) ? _parseName() : _handleOddName(i);
            _parsingContext.setCurrentName(name);
            _currToken = JsonToken.FIELD_NAME;
            i = _skipColon();
        }

        // Ok: we must have a value... what is it?

        JsonToken t;

        switch (i) {
        case '"':
            _tokenIncomplete = true;
            t = JsonToken.VALUE_STRING;
            break;
        case '[':
            if (!inObject) {
                _parsingContext = _parsingContext.createChildArrayContext(_tokenInputRow, _tokenInputCol);
            }
            t = JsonToken.START_ARRAY;
            break;
        case '{':
            if (!inObject) {
                _parsingContext = _parsingContext.createChildObjectContext(_tokenInputRow, _tokenInputCol);
            }
            t = JsonToken.START_OBJECT;
            break;
        case ']':
        case '}':
            // Error: neither is valid at this point; valid closers have
            // been handled earlier
            _reportUnexpectedChar(i, "expected a value");
        case 't':
            _matchTrue();
            t = JsonToken.VALUE_TRUE;
            break;
        case 'f':
            _matchFalse();
            t = JsonToken.VALUE_FALSE;
            break;
        case 'n':
            _matchNull();
            t = JsonToken.VALUE_NULL;
            break;

        case '-':
            /* Should we have separate handling for plus? Although
             * it is not allowed per se, it may be erroneously used,
             * and could be indicate by a more specific error message.
             */
            t = _parseNegNumber();
            break;
        case '0':
        case '1':
        case '2':
        case '3':
        case '4':
        case '5':
        case '6':
        case '7':
        case '8':
        case '9':
            t = _parsePosNumber(i);
            break;
        default:
            t = _handleOddValue(i);
            break;
        }

        if (inObject) {
            _nextToken = t;
            return _currToken;
        }
        _currToken = t;
        return t;
    }

    public boolean nextFieldName(SerializableString sstr) throws IOException
    {
        // // // Note: most of code below is copied from nextToken()

        _numTypesValid = NR_UNKNOWN;
        if (_currToken == JsonToken.FIELD_NAME) {
            _nextAfterName();
            return false;
        }
        if (_tokenIncomplete) {
            _skipString();
        }
        int i = _skipWSOrEnd();
        if (i < 0) {
            close();
            _currToken = null;
            return false;
        }
        _binaryValue = null;

        if (i == INT_RBRACKET) {
            _updateLocation();
            if (!_parsingContext.inArray()) {
                _reportMismatchedEndMarker(i, '}');
            }
            _parsingContext = _parsingContext.getParent();
            _currToken = JsonToken.END_ARRAY;
            return false;
        }
        if (i == INT_RCURLY) {
            _updateLocation();
            if (!_parsingContext.inObject()) {
                _reportMismatchedEndMarker(i, ']');
            }
            _parsingContext = _parsingContext.getParent();
            _currToken = JsonToken.END_OBJECT;
            return false;
        }
        if (_parsingContext.expectComma()) {
            i = _skipComma(i);
        }
        _updateLocation();

        if (!_parsingContext.inObject()) {
            _nextTokenNotInObject(i);
            return false;
        }

        if (i == INT_QUOTE) {
            // when doing literal match, must consider escaping:
            char[] nameChars = sstr.asQuotedChars();
            final int len = nameChars.length;

            // Require 4 more bytes for faster skipping of colon that follows name
            if ((_inputPtr + len + 4) < _inputEnd) { // maybe...
                // first check length match by
                final int end = _inputPtr+len;
                if (_inputBuffer[end] == '"') {
                    int offset = 0;
                    int ptr = _inputPtr;
                    while (true) {
                        if (ptr == end) { // yes, match!
                            _parsingContext.setCurrentName(sstr.getValue());
                            _isNextTokenNameYes(_skipColonFast(ptr+1));
                            return true;
                        }
                        if (nameChars[offset] != _inputBuffer[ptr]) {
                            break;
                        }
                        ++offset;
                        ++ptr;
                    }
                }
            }
        }
        return _isNextTokenNameMaybe(i, sstr.getValue());
    }

    public String nextFieldName() throws IOException
    {
        // // // Note: this is almost a verbatim copy of nextToken() (minus comments)

        _numTypesValid = NR_UNKNOWN;
        if (_currToken == JsonToken.FIELD_NAME) {
            _nextAfterName();
            return null;
        }
        if (_tokenIncomplete) {
            _skipString();
        }
        int i = _skipWSOrEnd();
        if (i < 0) {
            close();
            _currToken = null;
            return null;
        }
        _binaryValue = null;
        if (i == INT_RBRACKET) {
            _updateLocation();
            if (!_parsingContext.inArray()) {
                _reportMismatchedEndMarker(i, '}');
            }
            _parsingContext = _parsingContext.getParent();
            _currToken = JsonToken.END_ARRAY;
            return null;
        }
        if (i == INT_RCURLY) {
            _updateLocation();
            if (!_parsingContext.inObject()) {
                _reportMismatchedEndMarker(i, ']');
            }
            _parsingContext = _parsingContext.getParent();
            _currToken = JsonToken.END_OBJECT;
            return null;
        }
        if (_parsingContext.expectComma()) {
            i = _skipComma(i);
        }
        _updateLocation();
        if (!_parsingContext.inObject()) {
            _nextTokenNotInObject(i);
            return null;
        }

        String name = (i == INT_QUOTE) ? _parseName() : _handleOddName(i);
        _parsingContext.setCurrentName(name);
        _currToken = JsonToken.FIELD_NAME;
        i = _skipColon();

        if (i == INT_QUOTE) {
            _tokenIncomplete = true;
            _nextToken = JsonToken.VALUE_STRING;
            return name;
        }
        
        // Ok: we must have a value... what is it?

        JsonToken t;

        switch (i) {
        case '-':
            t = _parseNegNumber();
            break;
        case '0':
        case '1':
        case '2':
        case '3':
        case '4':
        case '5':
        case '6':
        case '7':
        case '8':
        case '9':
            t = _parsePosNumber(i);
            break;
        case 'f':
            _matchFalse();
            t = JsonToken.VALUE_FALSE;
            break;
        case 'n':
            _matchNull();
            t = JsonToken.VALUE_NULL;
            break;
        case 't':
            _matchTrue();
            t = JsonToken.VALUE_TRUE;
            break;
        case '[':
            t = JsonToken.START_ARRAY;
            break;
        case '{':
            t = JsonToken.START_OBJECT;
            break;
        default:
            t = _handleOddValue(i);
            break;
        }
        _nextToken = t;
        return name;
    }

    private final void _isNextTokenNameYes(int i) throws IOException
    {
        _currToken = JsonToken.FIELD_NAME;

        switch (i) {
        case '"':
            _tokenIncomplete = true;
            _nextToken = JsonToken.VALUE_STRING;
            return;
        case '[':
            _nextToken = JsonToken.START_ARRAY;
            return;
        case '{':
            _nextToken = JsonToken.START_OBJECT;
            return;
        case 't':
            _matchToken("true", 1);
            _nextToken = JsonToken.VALUE_TRUE;
            return;
        case 'f':
            _matchToken("false", 1);
            _nextToken = JsonToken.VALUE_FALSE;
            return;
        case 'n':
            _matchToken("null", 1);
            _nextToken = JsonToken.VALUE_NULL;
            return;
        case '-':
            _nextToken = _parseNegNumber();
            return;
        case '0':
        case '1':
        case '2':
        case '3':
        case '4':
        case '5':
        case '6':
        case '7':
        case '8':
        case '9':
            _nextToken = _parsePosNumber(i);
            return;
        }
        _nextToken = _handleOddValue(i);
    }

    protected boolean _isNextTokenNameMaybe(int i, String nameToMatch) throws IOException
    {
        // // // and this is back to standard nextToken()
        String name = (i == INT_QUOTE) ? _parseName() : _handleOddName(i);
        _parsingContext.setCurrentName(name);
        _currToken = JsonToken.FIELD_NAME;
        i = _skipColon();
        if (i == INT_QUOTE) {
            _tokenIncomplete = true;
            _nextToken = JsonToken.VALUE_STRING;
            return nameToMatch.equals(name);
        }
        // Ok: we must have a value... what is it?
        JsonToken t;
        switch (i) {
        case '-':
            t = _parseNegNumber();
            break;
        case '0':
        case '1':
        case '2':
        case '3':
        case '4':
        case '5':
        case '6':
        case '7':
        case '8':
        case '9':
            t = _parsePosNumber(i);
            break;
        case 'f':
            _matchFalse();
            t = JsonToken.VALUE_FALSE;
            break;
        case 'n':
            _matchNull();
            t = JsonToken.VALUE_NULL;
            break;
        case 't':
            _matchTrue();
            t = JsonToken.VALUE_TRUE;
            break;
        case '[':
            t = JsonToken.START_ARRAY;
            break;
        case '{':
            t = JsonToken.START_OBJECT;
            break;
        default:
            t = _handleOddValue(i);
            break;
        }
        _nextToken = t;
        return nameToMatch.equals(name);
    }

    public JsonLocation getTokenLocation()
    {
        final Object src = _ioContext.getSourceReference();
        return new JsonLocation(src,
                -1L, getTokenCharacterOffset(),
                getTokenLineNr(),
                getTokenColumnNr());
    }

    public JsonLocation getTokenLocation()
    {
        final Object src = _ioContext.getSourceReference();
        if (_currToken == JsonToken.FIELD_NAME) {
            return new JsonLocation(src,
                    _nameInputTotal, -1L, _nameInputRow, _tokenInputCol);
        }
        return new JsonLocation(src,
                getTokenCharacterOffset(), -1L, getTokenLineNr(),
                getTokenColumnNr());
    }

// relevant test
// com.fasterxml.jackson.core.TestExceptions::testOriginalMesssage
    public void testOriginalMesssage()
    {
        JsonProcessingException exc = new JsonParseException(null, "Foobar", JsonLocation.NA);
        String msg = exc.getMessage();
        String orig = exc.getOriginalMessage();
        assertEquals("Foobar", orig);
        assertTrue(msg.length() > orig.length());
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
    }

// com.fasterxml.jackson.core.filter.BasicGeneratorFilteringTest::testMultipleMatchFilteringWithPath1
    public void testMultipleMatchFilteringWithPath1() throws Exception
    {
        StringWriter w = new StringWriter();
        JsonGenerator gen = new FilteringGeneratorDelegate(JSON_F.createGenerator(w),
                new NameMatchFilter("value0", "value2"),
                true,  true  );
        final String JSON = "{'a':123,'array':[1,2],'ob':{'value0':2,'value':3,'value2':4},'b':true}";
        writeJsonDoc(JSON_F, JSON, gen);
        assertEquals(aposToQuotes("{'ob':{'value0':2,'value2':4}}"), w.toString());
    }

// com.fasterxml.jackson.core.filter.BasicGeneratorFilteringTest::testMultipleMatchFilteringWithPath2
    public void testMultipleMatchFilteringWithPath2() throws Exception
    {
        StringWriter w = new StringWriter();
        
        JsonGenerator gen = new FilteringGeneratorDelegate(JSON_F.createGenerator(w),
                new NameMatchFilter("array", "b", "value"),
                true, true);
        final String JSON = "{'a':123,'array':[1,2],'ob':{'value0':2,'value':3,'value2':4},'b':true}";
        writeJsonDoc(JSON_F, JSON, gen);
        assertEquals(aposToQuotes("{'array':[1,2],'ob':{'value':3},'b':true}"), w.toString());
    }

// com.fasterxml.jackson.core.filter.BasicGeneratorFilteringTest::testMultipleMatchFilteringWithPath3
    public void testMultipleMatchFilteringWithPath3() throws Exception
    {
        StringWriter w = new StringWriter();
        
        JsonGenerator gen = new FilteringGeneratorDelegate(JSON_F.createGenerator(w),
                new NameMatchFilter("value"),
                true, true);
        final String JSON = "{'root':{'a0':true,'a':{'value':3},'b':{'value':4}},'b0':false}";
        writeJsonDoc(JSON_F, JSON, gen);
        assertEquals(aposToQuotes("{'root':{'a':{'value':3},'b':{'value':4}}}"), w.toString());
    }

// com.fasterxml.jackson.core.filter.BasicGeneratorFilteringTest::testIndexMatchWithPath1
    public void testIndexMatchWithPath1() throws Exception
    {
        StringWriter w = new StringWriter();
        JsonGenerator gen = new FilteringGeneratorDelegate(JSON_F.createGenerator(w),
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
    }

// com.fasterxml.jackson.core.filter.BasicGeneratorFilteringTest::testIndexMatchWithPath2
    public void testIndexMatchWithPath2() throws Exception
    {
        StringWriter w = new StringWriter();
        JsonGenerator gen = new FilteringGeneratorDelegate(JSON_F.createGenerator(w),
                new IndexMatchFilter(0,1),
                true, true);
        final String JSON = "{'a':123,'array':[1,2],'ob':{'value0':2,'value':3,'value2':4},'b':true}";
        writeJsonDoc(JSON_F, JSON, gen);
        assertEquals(aposToQuotes("{'array':[1,2]}"), w.toString());
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
        JsonParser p = new FilteringParserDelegate(p0,
               new NameMatchFilter("value"),
                   false, 
                   false 
                );
        String result = readAndWrite(JSON_F, p);
        assertEquals(aposToQuotes("3"), result);
    }

// com.fasterxml.jackson.core.filter.BasicParserFilteringTest::testSingleMatchFilteringWithPath
    public void testSingleMatchFilteringWithPath() throws Exception
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

// com.fasterxml.jackson.core.filter.BasicParserFilteringTest::testMultipleMatchFilteringWithPath1
    public void testMultipleMatchFilteringWithPath1() throws Exception
    {
        JsonParser p0 = JSON_F.createParser(SIMPLE);
        JsonParser p = new FilteringParserDelegate(p0,
                new NameMatchFilter("value0", "value2"),
                true,  true  );
        String result = readAndWrite(JSON_F, p);
        assertEquals(aposToQuotes("{'ob':{'value0':2,'value2':4}}"), result);
    }

// com.fasterxml.jackson.core.filter.BasicParserFilteringTest::testMultipleMatchFilteringWithPath2
    public void testMultipleMatchFilteringWithPath2() throws Exception
    {
        String INPUT = aposToQuotes("{'a':123,'ob':{'value0':2,'value':3,'value2':4},'b':true}");
        JsonParser p0 = JSON_F.createParser(INPUT);
        JsonParser p = new FilteringParserDelegate(p0,
                new NameMatchFilter("b", "value"),
                true, true);

        String result = readAndWrite(JSON_F, p);
        assertEquals(aposToQuotes("{'ob':{'value':3},'b':true}"), result);
    }

// com.fasterxml.jackson.core.filter.BasicParserFilteringTest::testMultipleMatchFilteringWithPath3
    public void testMultipleMatchFilteringWithPath3() throws Exception
    {
        final String JSON = aposToQuotes("{'root':{'a0':true,'a':{'value':3},'b':{'value':4}},'b0':false}");
        JsonParser p0 = JSON_F.createParser(JSON);
        JsonParser p = new FilteringParserDelegate(p0,
                new NameMatchFilter("value"),
                true, true);
        String result = readAndWrite(JSON_F, p);
        assertEquals(aposToQuotes("{'root':{'a':{'value':3},'b':{'value':4}}}"), result);
    }

// com.fasterxml.jackson.core.filter.BasicParserFilteringTest::testIndexMatchWithPath1
    public void testIndexMatchWithPath1() throws Exception
    {
        JsonParser p = new FilteringParserDelegate(JSON_F.createParser(SIMPLE),
                new IndexMatchFilter(1), true, true);
        String result = readAndWrite(JSON_F, p);
        assertEquals(aposToQuotes("{'array':[2]}"), result);

        p = new FilteringParserDelegate(JSON_F.createParser(SIMPLE),
                new IndexMatchFilter(0), true, true);
        result = readAndWrite(JSON_F, p);
        assertEquals(aposToQuotes("{'array':[1]}"), result);
    }

// com.fasterxml.jackson.core.filter.BasicParserFilteringTest::testIndexMatchWithPath2
    public void testIndexMatchWithPath2() throws Exception
    {
        JsonParser p = new FilteringParserDelegate(JSON_F.createParser(SIMPLE),
                new IndexMatchFilter(0, 1), true, true);
        assertEquals(aposToQuotes("{'array':[1,2]}"), readAndWrite(JSON_F, p));
    
        String JSON = aposToQuotes("{'a':123,'array':[1,2,3,4,5],'b':[1,2,3]}");
        p = new FilteringParserDelegate(JSON_F.createParser(JSON),
                new IndexMatchFilter(1, 3), true, true);
        assertEquals(aposToQuotes("{'array':[2,4],'b':[2]}"), readAndWrite(JSON_F, p));
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
        doTestIntValueWrite(false);
        doTestIntValueWrite(true);
    }

// com.fasterxml.jackson.core.json.GeneratorBasicTest::testLongValueWrite
    public void testLongValueWrite() throws Exception
    {
        doTestLongValueWrite(false);
        doTestLongValueWrite(true);
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

         JsonParser jp = createParserUsingReader(docStr);
         assertEquals(JsonToken.VALUE_NUMBER_INT, jp.nextToken());
         assertEquals(1, jp.getIntValue());
         assertEquals(JsonToken.VALUE_NUMBER_INT, jp.nextToken());
         assertEquals(2, jp.getIntValue());
         assertEquals(JsonToken.VALUE_NUMBER_INT, jp.nextToken());
         assertEquals(-13, jp.getIntValue());
         jp.close();
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

// com.fasterxml.jackson.core.json.ParserSequenceTest::testSimple
    public void testSimple() throws Exception
    {
        JsonParser p1 = JSON_FACTORY.createParser("[ 1 ]");
        JsonParser p2 = JSON_FACTORY.createParser("[ 2 ]");
        JsonParserSequence seq = JsonParserSequence.createFlattened(p1, p2);
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
        
        p1.close();
        p2.close();
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
        _testMediumStrings(true, 1100);
        _testMediumStrings(true, 2300);
        _testMediumStrings(true, 3800);
        _testMediumStrings(true, 7500);
        _testMediumStrings(true, 19000);
    }

// com.fasterxml.jackson.core.json.StringGenerationTest::testMediumStringsChars
    public void testMediumStringsChars() throws Exception
    {
        _testMediumStrings(false, 1100);
        _testMediumStrings(false, 2300);
        _testMediumStrings(false, 3800);
        _testMediumStrings(false, 7500);
        _testMediumStrings(false, 19000);
    }

// com.fasterxml.jackson.core.json.StringGenerationTest::testLongerRandomSingleChunk
    public void testLongerRandomSingleChunk() throws Exception
    {
        
        for (int round = 0; round < 80; ++round) {
            String content = generateRandom(75000+round);
            doTestLongerRandom(content, false);
            doTestLongerRandom(content, true);
        }
    }

// com.fasterxml.jackson.core.json.StringGenerationTest::testLongerRandomMultiChunk
    public void testLongerRandomMultiChunk() throws Exception
    {
        
        for (int round = 0; round < 70; ++round) {
            String content = generateRandom(73000+round);
            doTestLongerRandomMulti(content, false, round);
            doTestLongerRandomMulti(content, true, round);
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

// com.fasterxml.jackson.core.json.TestJsonGeneratorFeatures::testBigDecimalAsPlainString
    public void testBigDecimalAsPlainString() throws Exception
    {
        JsonFactory jf = new JsonFactory();
        BigDecimal ENG = new BigDecimal("1E+2");
        jf.enable(JsonGenerator.Feature.WRITE_BIGDECIMAL_AS_PLAIN);
        jf.enable(JsonGenerator.Feature.WRITE_NUMBERS_AS_STRINGS);

        StringWriter sw = new StringWriter();
        JsonGenerator jg = jf.createGenerator(sw);
        jg.writeNumber(ENG);
        jg.close();
        assertEquals(quote("100"), sw.toString());

        
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        jg = jf.createGenerator(bos);
        jg.writeNumber(ENG);
        jg.close();
        assertEquals(quote("100"), bos.toString("UTF-8"));
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

// com.fasterxml.jackson.core.json.TestJsonParser::testGetValueAsTextBytes
    public void testGetValueAsTextBytes() throws Exception
    {
        JsonFactory f = new JsonFactory();
        _testGetValueAsText(f, true, false);
        _testGetValueAsText(f, true, true);
    }

// com.fasterxml.jackson.core.json.TestJsonParser::testGetValueAsTextChars
    public void testGetValueAsTextChars() throws Exception
    {
        JsonFactory f = new JsonFactory();
        _testGetValueAsText(f, false, false);
        _testGetValueAsText(f, false, true);
    }

// com.fasterxml.jackson.core.json.TestLocation::testSimpleInitialOffsets
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

// com.fasterxml.jackson.core.json.TestLocation::testOffsetWithInputOffset
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

// com.fasterxml.jackson.core.json.TestNextXxx::testIsNextTokenName
    public void testIsNextTokenName() throws Exception
    {
        _testIsNextTokenName1(false);
        _testIsNextTokenName1(true);
    }

// com.fasterxml.jackson.core.json.TestNextXxx::testIsNextTokenName2
    public void testIsNextTokenName2() throws Exception {
        _testIsNextTokenName2(false);
        _testIsNextTokenName2(true);
    }

// com.fasterxml.jackson.core.json.TestNextXxx::testIsNextTokenName3
    public void testIsNextTokenName3() throws Exception {
        _testIsNextTokenName3(false);
        _testIsNextTokenName3(true);
    }

// com.fasterxml.jackson.core.json.TestNextXxx::testIsNextTokenName4
    public void testIsNextTokenName4() throws Exception {
        _testIsNextTokenName4(false);
        _testIsNextTokenName4(true);
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
        _testNextNameWithLong(false);
        _testNextNameWithLong(true);
    }

// com.fasterxml.jackson.core.json.TestNextXxx::testNextNameWithIndentation
    public void testNextNameWithIndentation() throws Exception
    {
        _testNextFieldNameIndent(false);
        _testNextFieldNameIndent(true);
    }

// com.fasterxml.jackson.core.json.TestNextXxx::testNextTextValue
    public void testNextTextValue() throws Exception
    {
        _textNextText(false);
        _textNextText(true);
    }

// com.fasterxml.jackson.core.json.TestNextXxx::testNextIntValue
    public void testNextIntValue() throws Exception
    {
        _textNextInt(false);
        _textNextInt(true);
    }

// com.fasterxml.jackson.core.json.TestNextXxx::testNextLongValue
    public void testNextLongValue() throws Exception
    {
        _textNextLong(false);
        _textNextLong(true);
    }

// com.fasterxml.jackson.core.json.TestNextXxx::testNextBooleanValue
    public void testNextBooleanValue() throws Exception
    {
        _textNextBoolean(false);
        _textNextBoolean(true);
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

// com.fasterxml.jackson.core.json.TestRootValues::testBrokeanNumber
    public void testBrokeanNumber() throws Exception
    {
    	_testBrokeanNumber(false);
    	_testBrokeanNumber(true);
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
        JsonGenerator jgen = JSON_F.createGenerator(out);
        jgen.writeStartArray();
        jgen.writeRaw(VALUE);
        jgen.writeEndArray();
        jgen.close();

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
        
        CharsToNameCanonicalizer symbols = CharsToNameCanonicalizer.createRoot(1);
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
        CharsToNameCanonicalizer symbols = CharsToNameCanonicalizer.createRoot(1);
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
        
        CharsToNameCanonicalizer symbols = CharsToNameCanonicalizer.createRoot(1);
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
            CharsToNameCanonicalizer symbols = CharsToNameCanonicalizer.createRoot(1);
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
        CharsToNameCanonicalizer symbolsC = CharsToNameCanonicalizer.createRoot(3);

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

        CharsToNameCanonicalizer symbols = CharsToNameCanonicalizer.createRoot(SEED);
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

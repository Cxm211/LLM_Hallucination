// buggy code
    protected JsonToken _parseNumber(int ch) throws IOException
    {
        /* Although we will always be complete with respect to textual
         * representation (that is, all characters will be parsed),
         * actual conversion to a number is deferred. Thus, need to
         * note that no representations are valid yet
         */
        boolean negative = (ch == INT_MINUS);
        int ptr = _inputPtr;
        int startPtr = ptr-1; // to include sign/digit already read
        final int inputLen = _inputEnd;

        dummy_loop:
        do { // dummy loop, to be able to break out
            if (negative) { // need to read the next digit
                if (ptr >= _inputEnd) {
                    break dummy_loop;
                }
                ch = _inputBuffer[ptr++];
                // First check: must have a digit to follow minus sign
                if (ch > INT_9 || ch < INT_0) {
                    _inputPtr = ptr;
                    return _handleInvalidNumberStart(ch, true);
                }
                /* (note: has been checked for non-negative already, in
                 * the dispatching code that determined it should be
                 * a numeric value)
                 */
            }
            // One special case, leading zero(es):
            if (ch == INT_0) {
                break dummy_loop;
            }
            
            /* First, let's see if the whole number is contained within
             * the input buffer unsplit. This should be the common case;
             * and to simplify processing, we will just reparse contents
             * in the alternative case (number split on buffer boundary)
             */
            
            int intLen = 1; // already got one
            
            // First let's get the obligatory integer part:
            
            int_loop:
            while (true) {
                if (ptr >= _inputEnd) {
                    break dummy_loop;
                }
                ch = (int) _inputBuffer[ptr++];
                if (ch < INT_0 || ch > INT_9) {
                    break int_loop;
                }
                ++intLen;
            }

            int fractLen = 0;
            
            // And then see if we get other parts
            if (ch == '.') { // yes, fraction
                fract_loop:
                while (true) {
                    if (ptr >= inputLen) {
                        break dummy_loop;
                    }
                    ch = (int) _inputBuffer[ptr++];
                    if (ch < INT_0 || ch > INT_9) {
                        break fract_loop;
                    }
                    ++fractLen;
                }
                // must be followed by sequence of ints, one minimum
                if (fractLen == 0) {
                    reportUnexpectedNumberChar(ch, "Decimal point not followed by a digit");
                }
            }

            int expLen = 0;
            if (ch == 'e' || ch == 'E') { // and/or exponent
                if (ptr >= inputLen) {
                    break dummy_loop;
                }
                // Sign indicator?
                ch = (int) _inputBuffer[ptr++];
                if (ch == INT_MINUS || ch == INT_PLUS) { // yup, skip for now
                    if (ptr >= inputLen) {
                        break dummy_loop;
                    }
                    ch = (int) _inputBuffer[ptr++];
                }
                while (ch <= INT_9 && ch >= INT_0) {
                    ++expLen;
                    if (ptr >= inputLen) {
                        break dummy_loop;
                    }
                    ch = (int) _inputBuffer[ptr++];
                }
                // must be followed by sequence of ints, one minimum
                if (expLen == 0) {
                    reportUnexpectedNumberChar(ch, "Exponent indicator not followed by a digit");
                }
            }
            // Got it all: let's add to text buffer for parsing, access
            --ptr; // need to push back following separator
            _inputPtr = ptr;
            // As per #105, need separating space between root values; check here
            int len = ptr-startPtr;
            _textBuffer.resetWithShared(_inputBuffer, startPtr, len);
            return reset(negative, intLen, fractLen, expLen);
        } while (false);

        _inputPtr = negative ? (startPtr+1) : startPtr;
        return _parseNumber2(negative);
    }

    private JsonToken _parseNumber2(boolean negative) throws IOException
    {
        char[] outBuf = _textBuffer.emptyAndGetCurrentSegment();
        int outPtr = 0;

        // Need to prepend sign?
        if (negative) {
            outBuf[outPtr++] = '-';
        }

        // This is the place to do leading-zero check(s) too:
        int intLen = 0;
        char c = (_inputPtr < _inputEnd) ? _inputBuffer[_inputPtr++] : getNextChar("No digit following minus sign");
        if (c == '0') {
            c = _verifyNoLeadingZeroes();
        }
        boolean eof = false;

        // Ok, first the obligatory integer part:
        int_loop:
        while (c >= '0' && c <= '9') {
            ++intLen;
            if (outPtr >= outBuf.length) {
                outBuf = _textBuffer.finishCurrentSegment();
                outPtr = 0;
            }
            outBuf[outPtr++] = c;
            if (_inputPtr >= _inputEnd && !loadMore()) {
                // EOF is legal for main level int values
                c = CHAR_NULL;
                eof = true;
                break int_loop;
            }
            c = _inputBuffer[_inputPtr++];
        }
        // Also, integer part is not optional
        if (intLen == 0) {
            reportInvalidNumber("Missing integer part (next char "+_getCharDesc(c)+")");
        }

        int fractLen = 0;
        // And then see if we get other parts
        if (c == '.') { // yes, fraction
            outBuf[outPtr++] = c;

            fract_loop:
            while (true) {
                if (_inputPtr >= _inputEnd && !loadMore()) {
                    eof = true;
                    break fract_loop;
                }
                c = _inputBuffer[_inputPtr++];
                if (c < INT_0 || c > INT_9) {
                    break fract_loop;
                }
                ++fractLen;
                if (outPtr >= outBuf.length) {
                    outBuf = _textBuffer.finishCurrentSegment();
                    outPtr = 0;
                }
                outBuf[outPtr++] = c;
            }
            // must be followed by sequence of ints, one minimum
            if (fractLen == 0) {
                reportUnexpectedNumberChar(c, "Decimal point not followed by a digit");
            }
        }

        int expLen = 0;
        if (c == 'e' || c == 'E') { // exponent?
            if (outPtr >= outBuf.length) {
                outBuf = _textBuffer.finishCurrentSegment();
                outPtr = 0;
            }
            outBuf[outPtr++] = c;
            // Not optional, can require that we get one more char
            c = (_inputPtr < _inputEnd) ? _inputBuffer[_inputPtr++]
                : getNextChar("expected a digit for number exponent");
            // Sign indicator?
            if (c == '-' || c == '+') {
                if (outPtr >= outBuf.length) {
                    outBuf = _textBuffer.finishCurrentSegment();
                    outPtr = 0;
                }
                outBuf[outPtr++] = c;
                // Likewise, non optional:
                c = (_inputPtr < _inputEnd) ? _inputBuffer[_inputPtr++]
                    : getNextChar("expected a digit for number exponent");
            }

            exp_loop:
            while (c <= INT_9 && c >= INT_0) {
                ++expLen;
                if (outPtr >= outBuf.length) {
                    outBuf = _textBuffer.finishCurrentSegment();
                    outPtr = 0;
                }
                outBuf[outPtr++] = c;
                if (_inputPtr >= _inputEnd && !loadMore()) {
                    eof = true;
                    break exp_loop;
                }
                c = _inputBuffer[_inputPtr++];
            }
            // must be followed by sequence of ints, one minimum
            if (expLen == 0) {
                reportUnexpectedNumberChar(c, "Exponent indicator not followed by a digit");
            }
        }

        // Ok; unless we hit end-of-input, need to push last char read back
        if (!eof) {
            --_inputPtr;
        }
        _textBuffer.setCurrentLength(outPtr);
        // And there we have it!
        return reset(negative, intLen, fractLen, expLen);
    }

    protected JsonToken _handleInvalidNumberStart(int ch, boolean negative) throws IOException
    {
        if (ch == 'I') {
            if (_inputPtr >= _inputEnd) {
                if (!loadMore()) {
                    _reportInvalidEOFInValue();
                }
            }
            ch = _inputBuffer[_inputPtr++];
            if (ch == 'N') {
                String match = negative ? "-INF" :"+INF";
                _matchToken(match, 3);
                if (isEnabled(Feature.ALLOW_NON_NUMERIC_NUMBERS)) {
                    return resetAsNaN(match, negative ? Double.NEGATIVE_INFINITY : Double.POSITIVE_INFINITY);
                }
                _reportError("Non-standard token '"+match+"': enable JsonParser.Feature.ALLOW_NON_NUMERIC_NUMBERS to allow");
            } else if (ch == 'n') {
                String match = negative ? "-Infinity" :"+Infinity";
                _matchToken(match, 3);
                if (isEnabled(Feature.ALLOW_NON_NUMERIC_NUMBERS)) {
                    return resetAsNaN(match, negative ? Double.NEGATIVE_INFINITY : Double.POSITIVE_INFINITY);
                }
                _reportError("Non-standard token '"+match+"': enable JsonParser.Feature.ALLOW_NON_NUMERIC_NUMBERS to allow");
            }
        }
        reportUnexpectedNumberChar(ch, "expected digit (0-9) to follow minus sign, for valid numeric value");
        return null;
    }

    protected JsonToken _parseNumber(int c)
        throws IOException, JsonParseException
    {
        char[] outBuf = _textBuffer.emptyAndGetCurrentSegment();
        int outPtr = 0;
        boolean negative = (c == INT_MINUS);

        // Need to prepend sign?
        if (negative) {
            outBuf[outPtr++] = '-';
            // Must have something after sign too
            if (_inputPtr >= _inputEnd) {
                loadMoreGuaranteed();
            }
            c = (int) _inputBuffer[_inputPtr++] & 0xFF;
            // Note: must be followed by a digit
            if (c < INT_0 || c > INT_9) {
                return _handleInvalidNumberStart(c, true);
            }
        }

        // One special case: if first char is 0, must not be followed by a digit
        if (c == INT_0) {
            c = _verifyNoLeadingZeroes();
        }
        
        // Ok: we can first just add digit we saw first:
        outBuf[outPtr++] = (char) c;
        int intLen = 1;

        // And then figure out how far we can read without further checks:
        int end = _inputPtr + outBuf.length;
        if (end > _inputEnd) {
            end = _inputEnd;
        }

        // With this, we have a nice and tight loop:
        while (true) {
            if (_inputPtr >= end) {
                // Long enough to be split across boundary, so:
                return _parserNumber2(outBuf, outPtr, negative, intLen);
            }
            c = (int) _inputBuffer[_inputPtr++] & 0xFF;
            if (c < INT_0 || c > INT_9) {
                break;
            }
            ++intLen;
            if (outPtr >= outBuf.length) {
                outBuf = _textBuffer.finishCurrentSegment();
                outPtr = 0;
            }
            outBuf[outPtr++] = (char) c;
        }
        if (c == '.' || c == 'e' || c == 'E') {
            return _parseFloat(outBuf, outPtr, c, negative, intLen);
        }
        
        --_inputPtr; // to push back trailing char (comma etc)
        _textBuffer.setCurrentLength(outPtr);
        // As per #105, need separating space between root values; check here

        // And there we have it!
        return resetInt(negative, intLen);
    }

    private JsonToken _parserNumber2(char[] outBuf, int outPtr, boolean negative,
            int intPartLength)
        throws IOException, JsonParseException
    {
        // Ok, parse the rest
        while (true) {
            if (_inputPtr >= _inputEnd && !loadMore()) {
                _textBuffer.setCurrentLength(outPtr);
                return resetInt(negative, intPartLength);
            }
            int c = (int) _inputBuffer[_inputPtr++] & 0xFF;
            if (c > INT_9 || c < INT_0) {
                if (c == '.' || c == 'e' || c == 'E') {
                    return _parseFloat(outBuf, outPtr, c, negative, intPartLength);
                }
                break;
            }
            if (outPtr >= outBuf.length) {
                outBuf = _textBuffer.finishCurrentSegment();
                outPtr = 0;
            }
            outBuf[outPtr++] = (char) c;
            ++intPartLength;
        }
        --_inputPtr; // to push back trailing char (comma etc)
        _textBuffer.setCurrentLength(outPtr);
        // As per #105, need separating space between root values; check here

        // And there we have it!
        return resetInt(negative, intPartLength);
        
    }

    private JsonToken _parseFloat(char[] outBuf, int outPtr, int c,
            boolean negative, int integerPartLength)
        throws IOException, JsonParseException
    {
        int fractLen = 0;
        boolean eof = false;

        // And then see if we get other parts
        if (c == '.') { // yes, fraction
            outBuf[outPtr++] = (char) c;

            fract_loop:
            while (true) {
                if (_inputPtr >= _inputEnd && !loadMore()) {
                    eof = true;
                    break fract_loop;
                }
                c = (int) _inputBuffer[_inputPtr++] & 0xFF;
                if (c < INT_0 || c > INT_9) {
                    break fract_loop;
                }
                ++fractLen;
                if (outPtr >= outBuf.length) {
                    outBuf = _textBuffer.finishCurrentSegment();
                    outPtr = 0;
                }
                outBuf[outPtr++] = (char) c;
            }
            // must be followed by sequence of ints, one minimum
            if (fractLen == 0) {
                reportUnexpectedNumberChar(c, "Decimal point not followed by a digit");
            }
        }

        int expLen = 0;
        if (c == 'e' || c == 'E') { // exponent?
            if (outPtr >= outBuf.length) {
                outBuf = _textBuffer.finishCurrentSegment();
                outPtr = 0;
            }
            outBuf[outPtr++] = (char) c;
            // Not optional, can require that we get one more char
            if (_inputPtr >= _inputEnd) {
                loadMoreGuaranteed();
            }
            c = (int) _inputBuffer[_inputPtr++] & 0xFF;
            // Sign indicator?
            if (c == '-' || c == '+') {
                if (outPtr >= outBuf.length) {
                    outBuf = _textBuffer.finishCurrentSegment();
                    outPtr = 0;
                }
                outBuf[outPtr++] = (char) c;
                // Likewise, non optional:
                if (_inputPtr >= _inputEnd) {
                    loadMoreGuaranteed();
                }
                c = (int) _inputBuffer[_inputPtr++] & 0xFF;
            }

            exp_loop:
            while (c <= INT_9 && c >= INT_0) {
                ++expLen;
                if (outPtr >= outBuf.length) {
                    outBuf = _textBuffer.finishCurrentSegment();
                    outPtr = 0;
                }
                outBuf[outPtr++] = (char) c;
                if (_inputPtr >= _inputEnd && !loadMore()) {
                    eof = true;
                    break exp_loop;
                }
                c = (int) _inputBuffer[_inputPtr++] & 0xFF;
            }
            // must be followed by sequence of ints, one minimum
            if (expLen == 0) {
                reportUnexpectedNumberChar(c, "Exponent indicator not followed by a digit");
            }
        }

        // Ok; unless we hit end-of-input, need to push last char read back
        if (!eof) {
            --_inputPtr;
            // As per #105, need separating space between root values; check here
        }
        _textBuffer.setCurrentLength(outPtr);

        // And there we have it!
        return resetFloat(negative, integerPartLength, fractLen, expLen);
    }

    private int _skipWSOrEnd() throws IOException
    {
        final int[] codes = _icWS;
        while ((_inputPtr < _inputEnd) || loadMore()) {
            final int i = _inputBuffer[_inputPtr++] & 0xFF;
            switch (codes[i]) {
            case 0: // done!
                return i;
            case 1: // skip
                continue;
            case 2: // 2-byte UTF
                _skipUtf8_2(i);
                break;
            case 3: // 3-byte UTF
                _skipUtf8_3(i);
                break;
            case 4: // 4-byte UTF
                _skipUtf8_4(i);
                break;
            case INT_LF:
                ++_currInputRow;
                _currInputRowStart = _inputPtr;
                break;
            case INT_CR:
                _skipCR();
                break;
            case '/':
                _skipComment();
                break;
            case '#':
                if (!_skipYAMLComment()) {
                    return i;
                }
                break;
            default: // e.g. -1
                if (i < 32) {
                    _throwInvalidSpace(i);
                }
                _reportInvalidChar(i);
            }
        }
        // We ran out of input...
        _handleEOF();
        return -1;
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
        JsonParser jp = new ReaderBasedJsonParser(getIOContext(), 0, null, null,
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
        _testYAMLComments(true);
    }

// com.fasterxml.jackson.core.json.TestComments::testYAMLCommentsChars
    public void testYAMLCommentsChars() throws Exception {
        _testYAMLComments(false);
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
    public void testStringWrite()
        throws Exception
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
    public void testIntWrite()
        throws Exception
    {
        doTestIntWrite(false);
        doTestIntWrite(true);
    }

// com.fasterxml.jackson.core.json.TestJsonGenerator::testLongWrite
    public void testLongWrite()
        throws Exception
    {
        doTestLongWrite(false);
        doTestLongWrite(true);
    }

// com.fasterxml.jackson.core.json.TestJsonGenerator::testBooleanWrite
    public void testBooleanWrite()
        throws Exception
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
    public void testSkipping()
        throws Exception
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
        final int LEN = 96000;
        StringBuilder sb = new StringBuilder(LEN + 100);
        Random r = new Random(99);
        while (sb.length() < LEN) {
            sb.append(r.nextInt());
            sb.append(" xyz foo");
            if (r.nextBoolean()) {
                sb.append(" and \"bar\"");
            } else if (r.nextBoolean()) {
                sb.append(" [whatever].... ");
            } else {
                
                sb.append(" UTF-8-fu: try this {\u00E2/\u0BF8/\uA123!} (look funny?)");
            }
            if (r.nextBoolean()) {
                if (r.nextBoolean()) {
                    sb.append('\n');
                } else if (r.nextBoolean()) {
                    sb.append('\r');
                } else {
                    sb.append("\r\n");
                }
            }
        }
        final String VALUE = sb.toString();

        JsonFactory jf = new JsonFactory();
        
        
        StringWriter sw = new StringWriter(LEN + (LEN >> 2));
        JsonGenerator jg = jf.createGenerator(sw);
        jg.writeStartObject();
        jg.writeFieldName("doc");
        jg.writeString(VALUE);
        jg.writeEndObject();
        jg.close();
        
        final String DOC = sw.toString();

        for (int type = 0; type < 3; ++type) {
            JsonParser jp;

            switch (type) {
            default:
                jp = jf.createParser(DOC.getBytes("UTF-8"));
                break;
            case 1:
                jp = jf.createParser(DOC);
                break;
            case 2: 
                jp = jf.createParser(encodeInUTF32BE(DOC));
                break;
            }
            assertToken(JsonToken.START_OBJECT, jp.nextToken());
            assertToken(JsonToken.FIELD_NAME, jp.nextToken());
            assertEquals("doc", jp.getCurrentName());
            assertToken(JsonToken.VALUE_STRING, jp.nextToken());
            
            String act = getAndVerifyText(jp);
            if (act.length() != VALUE.length()) {
                fail("Expected length "+VALUE.length()+", got "+act.length());
            }
            if (!act.equals(VALUE)) {
                fail("Long text differs");
            }

            
            assertEquals("doc", jp.getCurrentName());
            assertToken(JsonToken.END_OBJECT, jp.nextToken());
            assertNull(jp.nextToken());
            jp.close();
        }
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

        JsonFactory jf = new JsonFactory();
        JsonParser jp = jf.createParser(src, offset, len);

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
        JsonFactory jf = new JsonFactory();
        JsonParser jp = jf.createParser(bytes.toByteArray());
        assertEquals(JsonToken.START_ARRAY, jp.nextToken());
        
        
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

        JsonFactory jf = new JsonFactory();
        JsonParser jp = jf.createParser(url);
        assertToken(JsonToken.START_OBJECT, jp.nextToken());
        assertToken(JsonToken.END_OBJECT, jp.nextToken());
        jp.close();
    }

// com.fasterxml.jackson.core.json.TestNextXxx::testIsNextTokenName
    public void testIsNextTokenName() throws Exception
    {
        _testIsNextTokenName1(false);
        _testIsNextTokenName1(true);
        _testIsNextTokenName2(false);
        _testIsNextTokenName2(true);
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

// com.fasterxml.jackson.core.json.TestNumericValues::testSimpleBoolean
    public void testSimpleBoolean() throws Exception
    {
        JsonParser jp = createParserUsingReader("[ true ]");
        assertToken(JsonToken.START_ARRAY, jp.nextToken());
        assertToken(JsonToken.VALUE_TRUE, jp.nextToken());
        assertEquals(true, jp.getBooleanValue());
        jp.close();
    }

// com.fasterxml.jackson.core.json.TestNumericValues::testSimpleInt
    public void testSimpleInt() throws Exception
    {
        int EXP_I = 1234;

        JsonParser jp = createParserUsingReader("[ "+EXP_I+" ]");
        assertToken(JsonToken.START_ARRAY, jp.nextToken());
        assertToken(JsonToken.VALUE_NUMBER_INT, jp.nextToken());
        assertEquals(JsonParser.NumberType.INT, jp.getNumberType());
        assertEquals(""+EXP_I, jp.getText());

        assertEquals(EXP_I, jp.getIntValue());
        assertEquals((long) EXP_I, jp.getLongValue());
        assertEquals((double) EXP_I, jp.getDoubleValue());
        assertEquals(BigDecimal.valueOf((long) EXP_I), jp.getDecimalValue());
        jp.close();
    }

// com.fasterxml.jackson.core.json.TestNumericValues::testIntRange
    public void testIntRange() throws Exception
    {
        
        for (int i = 0; i < 2; ++i) {
            String input = "[ "+Integer.MAX_VALUE+","+Integer.MIN_VALUE+" ]";
            JsonParser jp;
            if (i == 0) {
                jp = createParserUsingReader(input);                
            } else {
                jp = this.createParserUsingStream(input, "UTF-8");
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
    public void testSimpleLong()
        throws Exception
    {
        long EXP_L = 12345678907L;

        JsonParser jp = createParserUsingReader("[ "+EXP_L+" ]");
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
    public void testLongRange()
        throws Exception
    {
        for (int i = 0; i < 2; ++i) {
            long belowMinInt = -1L + Integer.MIN_VALUE;
            long aboveMaxInt = 1L + Integer.MAX_VALUE;
            String input = "[ "+Long.MAX_VALUE+","+Long.MIN_VALUE+","+aboveMaxInt+", "+belowMinInt+" ]";
            JsonParser jp;
            if (i == 0) {
                jp = createParserUsingReader(input);                
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
                jp = createParserUsingReader(input);                
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
                jp = createParserUsingReader(NUMBER_STR);                
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
    public void testSimpleDouble()
        throws Exception
    {
        final String[] INPUTS = new String[] {
            "1234.00", "2.1101567E-16", "1.0e5", "2.5e+5", "9e4", "-12e-3", "0.25"
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
                    jp = createParserUsingReader(DOC);
                }
                assertToken(JsonToken.START_ARRAY, jp.nextToken());
                assertToken(JsonToken.VALUE_NUMBER_FLOAT, jp.nextToken());
                assertEquals(STR, jp.getText());
                assertEquals(EXP_D, jp.getDoubleValue());
                assertToken(JsonToken.END_ARRAY, jp.nextToken());
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
                jp = createParserUsingReader(DOC);
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
                jp = createParserUsingReader(DOC_BELOW);
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
                jp = createParserUsingReader(DOC);
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

// com.fasterxml.jackson.core.json.TestNumericValues::testInvalidBooleanAccess
    public void testInvalidBooleanAccess()
        throws Exception
    {
        JsonParser jp = createParserUsingReader("[ \"abc\" ]");
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
        JsonParser jp = createParserUsingReader("[ \"abc\" ]");
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

// com.fasterxml.jackson.core.json.TestParserNonStandard::testSimpleUnquoted
    public void testSimpleUnquoted() throws Exception
    {
        _testSimpleUnquoted(false);
        _testSimpleUnquoted(true);
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

// com.fasterxml.jackson.core.json.TestRootValueParsing::testSimpleNumbers
    public void testSimpleNumbers() throws Exception
    {
        _testSimpleNumbers(false);
        _testSimpleNumbers(true);
    }

// com.fasterxml.jackson.core.json.TestRootValueParsing::testSimpleBooleans
    public void testSimpleBooleans() throws Exception
    {
        _testSimpleBooleans(false);
        _testSimpleBooleans(true);
    }

// com.fasterxml.jackson.core.json.TestUnicode::testSurrogates
    public void testSurrogates() throws Exception
    {
        JsonFactory f = new JsonFactory();
        _testSurrogates(f, true);
        _testSurrogates(f, false);
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
    public void testUtf8Name2Bytes()
        throws Exception
    {
        final String[] NAMES = UTF8_2BYTE_STRINGS;

        for (int i = 0; i < NAMES.length; ++i) {
            String NAME = NAMES[i];
            String DOC = "{ \""+NAME+"\" : 0 }";
            JsonParser jp = createParserUsingStream(DOC, "UTF-8");
            assertToken(JsonToken.START_OBJECT, jp.nextToken());
            
            assertToken(JsonToken.FIELD_NAME, jp.nextToken());
            assertEquals(NAME, jp.getCurrentName());
            assertToken(JsonToken.VALUE_NUMBER_INT, jp.nextToken());
            
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
                jp = this.createParserUsingStream(input, "UTF-8");
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
    public void testStreamReaderParser() throws Exception
    {
        _testWith(true);
    }

// com.fasterxml.jackson.core.json.TestWithTonsaSymbols::testReaderParser
    public void testReaderParser() throws Exception
    {
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
    public void testSharedSymbols()
        throws Exception
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

// com.fasterxml.jackson.core.sym.TestByteBasedSymbols::testAuxMethods
    public void testAuxMethods()
        throws Exception
    {
        final int A_BYTES = 0x41414141; 
        final int B_BYTES = 0x42424242; 

        BytesToNameCanonicalizer nc = BytesToNameCanonicalizer.createRoot()
                .makeChild(true, true);
        assertNull(nc.findName(A_BYTES));
        assertNull(nc.findName(A_BYTES, B_BYTES));

        nc.addName("AAAA", new int[] { A_BYTES }, 1);
        Name n1 = nc.findName(A_BYTES);
        assertNotNull(n1);
        assertEquals("AAAA", n1.getName());
        nc.addName("AAAABBBB", new int[] { A_BYTES, B_BYTES }, 2);
        Name n2 = nc.findName(A_BYTES, B_BYTES);
        assertEquals("AAAABBBB", n2.getName());
        assertNotNull(n2);

        
        assertNotNull(nc.toString());
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

// buggy code
    private final JsonToken _parseNumber2(boolean neg, int startPtr) throws IOException
    {
        _inputPtr = neg ? (startPtr+1) : startPtr;
        char[] outBuf = _textBuffer.emptyAndGetCurrentSegment();
        int outPtr = 0;

        // Need to prepend sign?
        if (neg) {
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
            return _handleInvalidNumberStart(c, neg);
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
            if (_parsingContext.inRoot()) {
                _verifyRootSpace(c);
            }
        }
        _textBuffer.setCurrentLength(outPtr);
        // And there we have it!
        return reset(neg, intLen, fractLen, expLen);
    }

    private final JsonToken _parseFloat(char[] outBuf, int outPtr, int c,
            boolean negative, int integerPartLength) throws IOException
    {
        int fractLen = 0;
        boolean eof = false;

        // And then see if we get other parts
        if (c == INT_PERIOD) { // yes, fraction
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
        if (c == INT_e || c == INT_E) { // exponent?
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
            // As per [core#105], need separating space between root values; check here
            if (_parsingContext.inRoot()) {
                _verifyRootSpace(c);
            }
        }
        _textBuffer.setCurrentLength(outPtr);

        // And there we have it!
        return resetFloat(negative, integerPartLength, fractLen, expLen);
    }

// relevant test
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
        
        assertNull(del.getCurrentToken());
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

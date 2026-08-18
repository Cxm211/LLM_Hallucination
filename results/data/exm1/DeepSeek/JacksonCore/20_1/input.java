// buggy code
    public void writeEmbeddedObject(Object object) throws IOException {
        // 01-Sep-2016, tatu: As per [core#318], handle small number of cases
        throw new JsonGenerationException("No native support for writing embedded objects",
                this);
    }

// relevant test
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

// com.fasterxml.jackson.core.read.ParserErrorHandlingTest::testMangledNumbersBytes
    public void testMangledNumbersBytes() throws Exception {
        _testMangledNumbers(MODE_INPUT_STREAM);
        _testMangledNumbers(MODE_INPUT_STREAM_THROTTLED);
        _testInvalidKeywords(MODE_DATA_INPUT);
    }

// com.fasterxml.jackson.core.read.ParserErrorHandlingTest::testMangledNumbersChars
    public void testMangledNumbersChars() throws Exception {
        _testMangledNumbers(MODE_READER);
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

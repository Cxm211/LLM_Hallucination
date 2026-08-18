// buggy code
    public int read() throws IOException {
        int current = super.read();
        if (current == '\n') {
            lineCounter++;
        }
        lastChar = current;
        return lastChar;
    }

// relevant test
// org.apache.commons.csv.CSVLexerTest::testNextToken1
    public void testNextToken1() throws IOException {
        String code = "abc,def, hijk,  lmnop,   qrst,uv ,wxy   ,z , ,";
        CSVLexer parser = getLexer(code, CSVFormat.DEFAULT.withSurroundingSpacesIgnored(true));
        assertTokenEquals(TOKEN, "abc", parser.nextToken(new Token()));
        assertTokenEquals(TOKEN, "def", parser.nextToken(new Token()));
        assertTokenEquals(TOKEN, "hijk", parser.nextToken(new Token()));
        assertTokenEquals(TOKEN, "lmnop", parser.nextToken(new Token()));
        assertTokenEquals(TOKEN, "qrst", parser.nextToken(new Token()));
        assertTokenEquals(TOKEN, "uv", parser.nextToken(new Token()));
        assertTokenEquals(TOKEN, "wxy", parser.nextToken(new Token()));
        assertTokenEquals(TOKEN, "z", parser.nextToken(new Token()));
        assertTokenEquals(TOKEN, "", parser.nextToken(new Token()));
        assertTokenEquals(EOF, "", parser.nextToken(new Token()));
    }

// org.apache.commons.csv.CSVLexerTest::testNextToken2
    public void testNextToken2() throws IOException {
        
        String code = "1,2,3,\na,b x,c\n#foo\n\nd,e,\n\n";
        CSVFormat format = CSVFormat.DEFAULT.withCommentStart('#');
        
        CSVLexer parser = getLexer(code, format);

        assertTokenEquals(TOKEN, "1", parser.nextToken(new Token()));
        assertTokenEquals(TOKEN, "2", parser.nextToken(new Token()));
        assertTokenEquals(TOKEN, "3", parser.nextToken(new Token()));
        assertTokenEquals(EORECORD, "", parser.nextToken(new Token()));
        assertTokenEquals(TOKEN, "a", parser.nextToken(new Token()));
        assertTokenEquals(TOKEN, "b x", parser.nextToken(new Token()));
        assertTokenEquals(EORECORD, "c", parser.nextToken(new Token()));
        assertTokenEquals(EORECORD, "", parser.nextToken(new Token()));
        assertTokenEquals(TOKEN, "d", parser.nextToken(new Token()));
        assertTokenEquals(TOKEN, "e", parser.nextToken(new Token()));
        assertTokenEquals(EORECORD, "", parser.nextToken(new Token()));
        assertTokenEquals(EOF, "", parser.nextToken(new Token()));
        assertTokenEquals(EOF, "", parser.nextToken(new Token()));

    }

// org.apache.commons.csv.CSVLexerTest::testNextToken3
    public void testNextToken3() throws IOException {
        
        String code = "a,\\,,b\n\\,,";
        CSVFormat format = CSVFormat.DEFAULT.withCommentStart('#');
        CSVLexer parser = getLexer(code, format);

        assertTokenEquals(TOKEN, "a", parser.nextToken(new Token()));
        
        assertTokenEquals(TOKEN, "\\", parser.nextToken(new Token()));
        assertTokenEquals(TOKEN, "", parser.nextToken(new Token()));
        assertTokenEquals(EORECORD, "b", parser.nextToken(new Token()));
        
        assertTokenEquals(TOKEN, "\\", parser.nextToken(new Token()));
        assertTokenEquals(TOKEN, "", parser.nextToken(new Token()));
        assertTokenEquals(EOF, "", parser.nextToken(new Token()));
    }

// org.apache.commons.csv.CSVLexerTest::testNextToken4
    public void testNextToken4() throws IOException {
        
        String code = "a,\"foo\",b\na,   \" foo\",b\na,\"foo \"  ,b\na,  \" foo \"  ,b";
        CSVLexer parser = getLexer(code, CSVFormat.DEFAULT.withSurroundingSpacesIgnored(true));
        assertTokenEquals(TOKEN, "a", parser.nextToken(new Token()));
        assertTokenEquals(TOKEN, "foo", parser.nextToken(new Token()));
        assertTokenEquals(EORECORD, "b", parser.nextToken(new Token()));
        assertTokenEquals(TOKEN, "a", parser.nextToken(new Token()));
        assertTokenEquals(TOKEN, " foo", parser.nextToken(new Token()));
        assertTokenEquals(EORECORD, "b", parser.nextToken(new Token()));
        assertTokenEquals(TOKEN, "a", parser.nextToken(new Token()));
        assertTokenEquals(TOKEN, "foo ", parser.nextToken(new Token()));
        assertTokenEquals(EORECORD, "b", parser.nextToken(new Token()));
        assertTokenEquals(TOKEN, "a", parser.nextToken(new Token()));
        assertTokenEquals(TOKEN, " foo ", parser.nextToken(new Token()));

        assertTokenEquals(EOF, "b", parser.nextToken(new Token()));
    }

// org.apache.commons.csv.CSVLexerTest::testNextToken5
    public void testNextToken5() throws IOException {
        String code = "a,\"foo\n\",b\n\"foo\n  baar ,,,\"\n\"\n\t \n\"";
        CSVLexer parser = getLexer(code, CSVFormat.DEFAULT);
        assertTokenEquals(TOKEN, "a", parser.nextToken(new Token()));
        assertTokenEquals(TOKEN, "foo\n", parser.nextToken(new Token()));
        assertTokenEquals(EORECORD, "b", parser.nextToken(new Token()));
        assertTokenEquals(EORECORD, "foo\n  baar ,,,", parser.nextToken(new Token()));
        assertTokenEquals(EOF, "\n\t \n", parser.nextToken(new Token()));

    }

// org.apache.commons.csv.CSVLexerTest::testNextToken6
    public void testNextToken6() throws IOException {
        
        String code = "a;'b and '' more\n'\n!comment;;;;\n;;";
        CSVFormat format = CSVFormat.DEFAULT.withDelimiter(';').withEncapsulator('\'').withCommentStart('!');
        CSVLexer parser = getLexer(code, format);
        assertTokenEquals(TOKEN, "a", parser.nextToken(new Token()));
        assertTokenEquals(EORECORD, "b and ' more\n", parser.nextToken(new Token()));
    }

// org.apache.commons.csv.CSVLexerTest::testDelimiterIsWhitespace
    public void testDelimiterIsWhitespace() throws IOException {
        String code = "one\ttwo\t\tfour \t five\t six";
        CSVLexer parser = getLexer(code, CSVFormat.TDF);
        assertTokenEquals(TOKEN, "one", parser.nextToken(new Token()));
        assertTokenEquals(TOKEN, "two", parser.nextToken(new Token()));
        assertTokenEquals(TOKEN, "", parser.nextToken(new Token()));
        assertTokenEquals(TOKEN, "four", parser.nextToken(new Token()));
        assertTokenEquals(TOKEN, "five", parser.nextToken(new Token()));
        assertTokenEquals(EOF, "six", parser.nextToken(new Token()));
    }

// org.apache.commons.csv.CSVParserTest::testGetLine
    public void testGetLine() throws IOException {
        CSVParser parser = new CSVParser(new StringReader(code), CSVFormat.DEFAULT.withSurroundingSpacesIgnored(true));
        for (String[] re : res) {
            assertArrayEquals(re, parser.getRecord().values());
        }
        
        assertNull(parser.getRecord());
    }

// org.apache.commons.csv.CSVParserTest::testGetRecords
    public void testGetRecords() throws IOException {
        CSVParser parser = new CSVParser(new StringReader(code), CSVFormat.DEFAULT.withSurroundingSpacesIgnored(true));
        List<CSVRecord> records = parser.getRecords();
        assertEquals(res.length, records.size());
        assertTrue(records.size() > 0);
        for (int i = 0; i < res.length; i++) {
            assertArrayEquals(res[i], records.get(i).values());
        }
    }

// org.apache.commons.csv.CSVParserTest::testExcelFormat1
    public void testExcelFormat1() throws IOException {
        String code =
                "value1,value2,value3,value4\r\na,b,c,d\r\n  x,,,"
                        + "\r\n\r\n\"\"\"hello\"\"\",\"  \"\"world\"\"\",\"abc\ndef\",\r\n";
        String[][] res = {
                {"value1", "value2", "value3", "value4"},
                {"a", "b", "c", "d"},
                {"  x", "", "", ""},
                {""},
                {"\"hello\"", "  \"world\"", "abc\ndef", ""}
        };
        CSVParser parser = new CSVParser(code, CSVFormat.EXCEL);
        List<CSVRecord> records = parser.getRecords();
        assertEquals(res.length, records.size());
        assertTrue(records.size() > 0);
        for (int i = 0; i < res.length; i++) {
            assertArrayEquals(res[i], records.get(i).values());
        }
    }

// org.apache.commons.csv.CSVParserTest::testExcelFormat2
    public void testExcelFormat2() throws Exception {
        String code = "foo,baar\r\n\r\nhello,\r\n\r\nworld,\r\n";
        String[][] res = {
                {"foo", "baar"},
                {""},
                {"hello", ""},
                {""},
                {"world", ""}
        };
        CSVParser parser = new CSVParser(code, CSVFormat.EXCEL);
        List<CSVRecord> records = parser.getRecords();
        assertEquals(res.length, records.size());
        assertTrue(records.size() > 0);
        for (int i = 0; i < res.length; i++) {
            assertArrayEquals(res[i], records.get(i).values());
        }
    }

// org.apache.commons.csv.CSVParserTest::testEndOfFileBehaviourExcel
    public void testEndOfFileBehaviourExcel() throws Exception {
        String[] codes = {
                "hello,\r\n\r\nworld,\r\n",
                "hello,\r\n\r\nworld,",
                "hello,\r\n\r\nworld,\"\"\r\n",
                "hello,\r\n\r\nworld,\"\"",
                "hello,\r\n\r\nworld,\n",
                "hello,\r\n\r\nworld,",
                "hello,\r\n\r\nworld,\"\"\n",
                "hello,\r\n\r\nworld,\"\""
        };
        String[][] res = {
                {"hello", ""},
                {""},  
                {"world", ""}
        };
        
        for (String code : codes) {
            CSVParser parser = new CSVParser(code, CSVFormat.EXCEL);
            List<CSVRecord> records = parser.getRecords();
            assertEquals(res.length, records.size());
            assertTrue(records.size() > 0);
            for (int i = 0; i < res.length; i++) {
                assertArrayEquals(res[i], records.get(i).values());
            }
        }
    }

// org.apache.commons.csv.CSVParserTest::testEndOfFileBehaviorCSV
    public void testEndOfFileBehaviorCSV() throws Exception {
        String[] codes = {
                "hello,\r\n\r\nworld,\r\n",
                "hello,\r\n\r\nworld,",
                "hello,\r\n\r\nworld,\"\"\r\n",
                "hello,\r\n\r\nworld,\"\"",
                "hello,\r\n\r\nworld,\n",
                "hello,\r\n\r\nworld,",
                "hello,\r\n\r\nworld,\"\"\n",
                "hello,\r\n\r\nworld,\"\""
        };
        String[][] res = {
                {"hello", ""},  
                {"world", ""}
        };
        for (String code : codes) {
            CSVParser parser = new CSVParser(new StringReader(code));
            List<CSVRecord> records = parser.getRecords();
            assertEquals(res.length, records.size());
            assertTrue(records.size() > 0);
            for (int i = 0; i < res.length; i++) {
                assertArrayEquals(res[i], records.get(i).values());
            }
        }
    }

// org.apache.commons.csv.CSVParserTest::testEmptyLineBehaviourExcel
    public void testEmptyLineBehaviourExcel() throws Exception {
        String[] codes = {
                "hello,\r\n\r\n\r\n",
                "hello,\n\n\n",
                "hello,\"\"\r\n\r\n\r\n",
                "hello,\"\"\n\n\n"
        };
        String[][] res = {
                {"hello", ""},
                {""},  
                {""}
        };
        for (String code : codes) {
            CSVParser parser = new CSVParser(code, CSVFormat.EXCEL);
            List<CSVRecord> records = parser.getRecords();
            assertEquals(res.length, records.size());
            assertTrue(records.size() > 0);
            for (int i = 0; i < res.length; i++) {
                assertArrayEquals(res[i], records.get(i).values());
            }
        }
    }

// org.apache.commons.csv.CSVParserTest::testEmptyLineBehaviourCSV
    public void testEmptyLineBehaviourCSV() throws Exception {
        String[] codes = {
                "hello,\r\n\r\n\r\n",
                "hello,\n\n\n",
                "hello,\"\"\r\n\r\n\r\n",
                "hello,\"\"\n\n\n"
        };
        String[][] res = {
                {"hello", ""}  
        };
        for (String code : codes) {
            CSVParser parser = new CSVParser(new StringReader(code));
            List<CSVRecord> records = parser.getRecords();
            assertEquals(res.length, records.size());
            assertTrue(records.size() > 0);
            for (int i = 0; i < res.length; i++) {
                assertArrayEquals(res[i], records.get(i).values());
            }
        }
    }

// org.apache.commons.csv.CSVParserTest::testEmptyFile
    public void testEmptyFile() throws Exception {
        CSVParser parser = new CSVParser("", CSVFormat.DEFAULT);
        assertNull(parser.getRecord());
    }

// org.apache.commons.csv.CSVParserTest::testBackslashEscapingOld
    public void testBackslashEscapingOld() throws IOException {
        String code =
                "one,two,three\n"
                        + "on\\\"e,two\n"
                        + "on\"e,two\n"
                        + "one,\"tw\\\"o\"\n"
                        + "one,\"t\\,wo\"\n"
                        + "one,two,\"th,ree\"\n"
                        + "\"a\\\\\"\n"
                        + "a\\,b\n"
                        + "\"a\\\\,b\"";
        String[][] res = {
                {"one", "two", "three"},
                {"on\\\"e", "two"},
                {"on\"e", "two"},
                {"one", "tw\"o"},
                {"one", "t\\,wo"},  
                {"one", "two", "th,ree"},
                {"a\\\\"},     
                {"a\\", "b"},  
                {"a\\\\,b"}    
        };
        CSVParser parser = new CSVParser(new StringReader(code));
        List<CSVRecord> records = parser.getRecords();
        assertEquals(res.length, records.size());
        assertTrue(records.size() > 0);
        for (int i = 0; i < res.length; i++) {
            assertArrayEquals(res[i], records.get(i).values());
        }
    }

// org.apache.commons.csv.CSVParserTest::testBackslashEscaping
    public void testBackslashEscaping() throws IOException {

        
        
        

        String code =
                "one,two,three\n" 
                        + "'',''\n"       
                        + "/',/'\n"       
                        + "'/'','/''\n"   
                        + "'''',''''\n"   
                        + "/,,/,\n"       
                        + "//,//\n"       
                        + "'//','//'\n"   
                        + "   8   ,   \"quoted \"\" /\" 
                        + "9,   /\n   \n"  
                        + "";
        String[][] res = {
                {"one", "two", "three"}, 
                {"", ""},                
                {"'", "'"},              
                {"'", "'"},              
                {"'", "'"},              
                {",", ","},              
                {"/", "/"},              
                {"/", "/"},              
                {"   8   ", "   \"quoted \"\" \" / string\"   "},
                {"9", "   \n   "},
        };

        CSVFormat format = new CSVFormat(',', '\'', CSVFormat.DISABLED, '/', false, true, "\r\n", null);

        CSVParser parser = new CSVParser(code, format);
        List<CSVRecord> records = parser.getRecords();
        assertTrue(records.size() > 0);
        for (int i = 0; i < res.length; i++) {
            assertArrayEquals(res[i], records.get(i).values());
        }
    }

// org.apache.commons.csv.CSVParserTest::testBackslashEscaping2
    public void testBackslashEscaping2() throws IOException {

        
        
        

        String code = ""
                + " , , \n"           
                + " \t ,  , \n"       
                + " 
                + "";
        String[][] res = {
                {" ", " ", " "},         
                {" \t ", "  ", " "},     
                {" / ", " , ", " ,"},    
        };

        CSVFormat format = new CSVFormat(',',  CSVFormat.DISABLED,  CSVFormat.DISABLED, '/', false, true, "\r\n", null);

        CSVParser parser = new CSVParser(code, format);
        List<CSVRecord> records = parser.getRecords();
        assertTrue(records.size() > 0);

        assertTrue(CSVPrinterTest.equals(res, records));
    }

// org.apache.commons.csv.CSVParserTest::testDefaultFormat
    public void testDefaultFormat() throws IOException {
        String code = ""
                + "a,b\n"            
                + "\"\n\",\" \"\n"   
                + "\"\",#\n"   
                ;
        String[][] res = {
                {"a", "b"},
                {"\n", " "},
                {"", "#"},
        };

        CSVFormat format = CSVFormat.DEFAULT;
        assertEquals(CSVFormat.DISABLED, format.getCommentStart());

        CSVParser parser = new CSVParser(code, format);
        List<CSVRecord> records = parser.getRecords();
        assertTrue(records.size() > 0);

        assertTrue(CSVPrinterTest.equals(res, records));

        String[][] res_comments = {
                {"a", "b"},
                {"\n", " "},
                {""},
        };

        format = CSVFormat.DEFAULT.withCommentStart('#');
        parser = new CSVParser(code, format);
        records = parser.getRecords();
        
        assertTrue(CSVPrinterTest.equals(res_comments, records));
    }

// org.apache.commons.csv.CSVParserTest::testCarriageReturnLineFeedEndings
    public void testCarriageReturnLineFeedEndings() throws IOException {
        String code = "foo\r\nbaar,\r\nhello,world\r\n,kanu";
        CSVParser parser = new CSVParser(new StringReader(code));
        List<CSVRecord> records = parser.getRecords();
        assertEquals(4, records.size());
    }

// org.apache.commons.csv.CSVParserTest::testCarriageReturnEndings
    public void testCarriageReturnEndings() throws IOException {
        String code = "foo\rbaar,\rhello,world\r,kanu";
        CSVParser parser = new CSVParser(new StringReader(code));
        List<CSVRecord> records = parser.getRecords();
        assertEquals(4, records.size());
    }

// org.apache.commons.csv.CSVParserTest::testLineFeedEndings
    public void testLineFeedEndings() throws IOException {
        String code = "foo\nbaar,\nhello,world\n,kanu";
        CSVParser parser = new CSVParser(new StringReader(code));
        List<CSVRecord> records = parser.getRecords();
        assertEquals(4, records.size());
    }

// org.apache.commons.csv.CSVParserTest::testIgnoreEmptyLines
    public void testIgnoreEmptyLines() throws IOException {
        String code = "\nfoo,baar\n\r\n,\n\n,world\r\n\n";
        
        
        CSVParser parser = new CSVParser(new StringReader(code));
        List<CSVRecord> records = parser.getRecords();
        assertEquals(3, records.size());
    }

// org.apache.commons.csv.CSVParserTest::testForEach
    public void testForEach() throws Exception {
        List<CSVRecord> records = new ArrayList<CSVRecord>();
        
        Reader in = new StringReader("a,b,c\n1,2,3\nx,y,z");
        
        for (CSVRecord record : CSVFormat.DEFAULT.parse(in)) {
            records.add(record);
        }
        
        assertEquals(3, records.size());
        assertArrayEquals(new String[]{"a", "b", "c"}, records.get(0).values());
        assertArrayEquals(new String[]{"1", "2", "3"}, records.get(1).values());
        assertArrayEquals(new String[]{"x", "y", "z"}, records.get(2).values());
    }

// org.apache.commons.csv.CSVParserTest::testIterator
    public void testIterator() throws Exception {
        Reader in = new StringReader("a,b,c\n1,2,3\nx,y,z");
        
        Iterator<CSVRecord> iterator = CSVFormat.DEFAULT.parse(in).iterator();
        
        assertTrue(iterator.hasNext());
        try {
            iterator.remove();
            fail("expected UnsupportedOperationException");
        } catch (UnsupportedOperationException expected) {
        }
        assertArrayEquals(new String[]{"a", "b", "c"}, iterator.next().values());
        assertArrayEquals(new String[]{"1", "2", "3"}, iterator.next().values());
        assertTrue(iterator.hasNext());
        assertTrue(iterator.hasNext());
        assertTrue(iterator.hasNext());
        assertArrayEquals(new String[]{"x", "y", "z"}, iterator.next().values());
        assertFalse(iterator.hasNext());
        
        try {
            iterator.next();
            fail("NoSuchElementException expected");
        } catch (NoSuchElementException e) {
            
        }
    }

// org.apache.commons.csv.CSVParserTest::testHeader
    public void testHeader() throws Exception {
        Reader in = new StringReader("a,b,c\n1,2,3\nx,y,z");

        Iterator<CSVRecord> records = CSVFormat.DEFAULT.withHeader().parse(in).iterator();
        
        for (int i = 0; i < 2; i++) {
            assertTrue(records.hasNext());
            CSVRecord record = records.next();
            assertEquals(record.get(0), record.get("a"));
            assertEquals(record.get(1), record.get("b"));
            assertEquals(record.get(2), record.get("c"));
        }
        
        assertFalse(records.hasNext());
    }

// org.apache.commons.csv.CSVParserTest::testProvidedHeader
    public void testProvidedHeader() throws Exception {
        Reader in = new StringReader("a,b,c\n1,2,3\nx,y,z");

        Iterator<CSVRecord> records = CSVFormat.DEFAULT.withHeader("A", "B", "C").parse(in).iterator();

        for (int i = 0; i < 3; i++) {
            assertTrue(records.hasNext());
            CSVRecord record = records.next();
            assertEquals(record.get(0), record.get("A"));
            assertEquals(record.get(1), record.get("B"));
            assertEquals(record.get(2), record.get("C"));
        }

        assertFalse(records.hasNext());
    }

// org.apache.commons.csv.CSVParserTest::testGetLineNumberWithLF
    public void testGetLineNumberWithLF() throws Exception {
        CSVParser parser = new CSVParser("a\nb\nc", CSVFormat.DEFAULT.withLineSeparator("\n"));
        
        assertEquals(0, parser.getLineNumber());
        assertNotNull(parser.getRecord());
        assertEquals(1, parser.getLineNumber());
        assertNotNull(parser.getRecord());
        assertEquals(2, parser.getLineNumber());
        assertNotNull(parser.getRecord());
        assertEquals(2, parser.getLineNumber());
        assertNull(parser.getRecord());
    }

// org.apache.commons.csv.CSVParserTest::testGetLineNumberWithCRLF
    public void testGetLineNumberWithCRLF() throws Exception {
        CSVParser parser = new CSVParser("a\r\nb\r\nc", CSVFormat.DEFAULT.withLineSeparator("\r\n"));
        
        assertEquals(0, parser.getLineNumber());
        assertNotNull(parser.getRecord());
        assertEquals(1, parser.getLineNumber());
        assertNotNull(parser.getRecord());
        assertEquals(2, parser.getLineNumber());
        assertNotNull(parser.getRecord());
        assertEquals(2, parser.getLineNumber());
        assertNull(parser.getRecord());
    }

// org.apache.commons.csv.CSVParserTest::testGetLineNumberWithCR
    public void testGetLineNumberWithCR() throws Exception {
        CSVParser parser = new CSVParser("a\rb\rc", CSVFormat.DEFAULT.withLineSeparator("\r"));
        
        assertEquals(0, parser.getLineNumber());
        assertNotNull(parser.getRecord());
        assertEquals(1, parser.getLineNumber());
        assertNotNull(parser.getRecord());
        assertEquals(2, parser.getLineNumber());
        assertNotNull(parser.getRecord());
        assertEquals(2, parser.getLineNumber());
        assertNull(parser.getRecord());
    }

// org.apache.commons.csv.CSVPrinterTest::testPrinter1
    public void testPrinter1() throws IOException {
        StringWriter sw = new StringWriter();
        CSVPrinter printer = new CSVPrinter(sw, CSVFormat.DEFAULT);
        printer.println("a", "b");
        assertEquals("a,b" + lineSeparator, sw.toString());
    }

// org.apache.commons.csv.CSVPrinterTest::testPrinter2
    public void testPrinter2() throws IOException {
        StringWriter sw = new StringWriter();
        CSVPrinter printer = new CSVPrinter(sw, CSVFormat.DEFAULT);
        printer.println("a,b", "b");
        assertEquals("\"a,b\",b" + lineSeparator, sw.toString());
    }

// org.apache.commons.csv.CSVPrinterTest::testPrinter3
    public void testPrinter3() throws IOException {
        StringWriter sw = new StringWriter();
        CSVPrinter printer = new CSVPrinter(sw, CSVFormat.DEFAULT);
        printer.println("a, b", "b ");
        assertEquals("\"a, b\",\"b \"" + lineSeparator, sw.toString());
    }

// org.apache.commons.csv.CSVPrinterTest::testPrinter4
    public void testPrinter4() throws IOException {
        StringWriter sw = new StringWriter();
        CSVPrinter printer = new CSVPrinter(sw, CSVFormat.DEFAULT);
        printer.println("a", "b\"c");
        assertEquals("a,\"b\"\"c\"" + lineSeparator, sw.toString());
    }

// org.apache.commons.csv.CSVPrinterTest::testPrinter5
    public void testPrinter5() throws IOException {
        StringWriter sw = new StringWriter();
        CSVPrinter printer = new CSVPrinter(sw, CSVFormat.DEFAULT);
        printer.println("a", "b\nc");
        assertEquals("a,\"b\nc\"" + lineSeparator, sw.toString());
    }

// org.apache.commons.csv.CSVPrinterTest::testPrinter6
    public void testPrinter6() throws IOException {
        StringWriter sw = new StringWriter();
        CSVPrinter printer = new CSVPrinter(sw, CSVFormat.DEFAULT);
        printer.println("a", "b\r\nc");
        assertEquals("a,\"b\r\nc\"" + lineSeparator, sw.toString());
    }

// org.apache.commons.csv.CSVPrinterTest::testPrinter7
    public void testPrinter7() throws IOException {
        StringWriter sw = new StringWriter();
        CSVPrinter printer = new CSVPrinter(sw, CSVFormat.DEFAULT);
        printer.println("a", "b\\c");
        assertEquals("a,b\\c" + lineSeparator, sw.toString());
    }

// org.apache.commons.csv.CSVPrinterTest::testExcelPrinter1
    public void testExcelPrinter1() throws IOException {
        StringWriter sw = new StringWriter();
        CSVPrinter printer = new CSVPrinter(sw, CSVFormat.EXCEL);
        printer.println("a", "b");
        assertEquals("a,b" + lineSeparator, sw.toString());
    }

// org.apache.commons.csv.CSVPrinterTest::testExcelPrinter2
    public void testExcelPrinter2() throws IOException {
        StringWriter sw = new StringWriter();
        CSVPrinter printer = new CSVPrinter(sw, CSVFormat.EXCEL);
        printer.println("a,b", "b");
        assertEquals("\"a,b\",b" + lineSeparator, sw.toString());
    }

// org.apache.commons.csv.CSVPrinterTest::testPrintNullValues
    public void testPrintNullValues() throws IOException {
        StringWriter sw = new StringWriter();
        CSVPrinter printer = new CSVPrinter(sw, CSVFormat.DEFAULT);
        printer.println("a", null, "b");
        assertEquals("a,,b" + lineSeparator, sw.toString());
    }

// org.apache.commons.csv.CSVPrinterTest::testDisabledComment
    public void testDisabledComment() throws IOException {
        StringWriter sw = new StringWriter();
        CSVPrinter printer = new CSVPrinter(sw, CSVFormat.DEFAULT);
        printer.printComment("This is a comment");
        
        assertEquals("", sw.toString());
    }

// org.apache.commons.csv.CSVPrinterTest::testSingleLineComment
    public void testSingleLineComment() throws IOException {
        StringWriter sw = new StringWriter();
        CSVPrinter printer = new CSVPrinter(sw, CSVFormat.DEFAULT.withCommentStart('#'));
        printer.printComment("This is a comment");
        
        assertEquals("# This is a comment" + lineSeparator, sw.toString());
    }

// org.apache.commons.csv.CSVPrinterTest::testMultiLineComment
    public void testMultiLineComment() throws IOException {
        StringWriter sw = new StringWriter();
        CSVPrinter printer = new CSVPrinter(sw, CSVFormat.DEFAULT.withCommentStart('#'));
        printer.printComment("This is a comment\non multiple lines");
        
        assertEquals("# This is a comment" + lineSeparator + "# on multiple lines" + lineSeparator, sw.toString());
    }

// org.apache.commons.csv.CSVPrinterTest::testRandom
    public void testRandom() throws Exception {
        int iter = 10000;
        doRandom(CSVFormat.DEFAULT, iter);
        doRandom(CSVFormat.EXCEL, iter);
        doRandom(CSVFormat.MYSQL, iter);
    }

// org.apache.commons.csv.ExtendedBufferedReaderTest::testEmptyInput
    public void testEmptyInput() throws Exception {
        ExtendedBufferedReader br = getBufferedReader("");
        assertEquals(ExtendedBufferedReader.END_OF_STREAM, br.read());
        assertEquals(ExtendedBufferedReader.END_OF_STREAM, br.lookAhead());
        assertEquals(ExtendedBufferedReader.END_OF_STREAM, br.readAgain());
        assertNull(br.readLine());
        assertEquals(0, br.read(new char[10], 0, 0));
    }

// org.apache.commons.csv.ExtendedBufferedReaderTest::testReadLookahead1
    public void testReadLookahead1() {}

// org.apache.commons.csv.ExtendedBufferedReaderTest::testReadLookahead2
    public void testReadLookahead2() throws Exception {
        char[] ref = new char[5];
        char[] res = new char[5];
        
        ExtendedBufferedReader br = getBufferedReader("abcdefg");
        ref[0] = 'a';
        ref[1] = 'b';
        ref[2] = 'c';
        assertEquals(3, br.read(res, 0, 3));
        assertArrayEquals(ref, res);
        assertEquals('c', br.readAgain());

        assertEquals('d', br.lookAhead());
        ref[4] = 'd';
        assertEquals(1, br.read(res, 4, 1));
        assertArrayEquals(ref, res);
        assertEquals('d', br.readAgain());
    }

// org.apache.commons.csv.ExtendedBufferedReaderTest::testReadLine
    public void testReadLine() throws Exception {
        ExtendedBufferedReader br = getBufferedReader("");
        assertNull(br.readLine());

        br = getBufferedReader("\n");
        assertEquals("",br.readLine());
        assertNull(br.readLine());

        br = getBufferedReader("foo\n\nhello");
        assertEquals(0, br.getLineNumber());
        assertEquals("foo",br.readLine());
        assertEquals(1, br.getLineNumber());
        assertEquals("",br.readLine());
        assertEquals(2, br.getLineNumber());
        assertEquals("hello",br.readLine());
        assertEquals(3, br.getLineNumber());
        assertNull(br.readLine());
        assertEquals(3, br.getLineNumber());

        br = getBufferedReader("foo\n\nhello");
        assertEquals('f', br.read());
        assertEquals('o', br.lookAhead());
        assertEquals("oo",br.readLine());
        assertEquals(1, br.getLineNumber());
        assertEquals('\n', br.lookAhead());
        assertEquals("",br.readLine());
        assertEquals(2, br.getLineNumber());
        assertEquals('h', br.lookAhead());
        assertEquals("hello",br.readLine());
        assertNull(br.readLine());
        assertEquals(3, br.getLineNumber());

        br = getBufferedReader("foo\rbaar\r\nfoo");
        assertEquals("foo",br.readLine());
        assertEquals('b', br.lookAhead());
        assertEquals("baar",br.readLine());
        assertEquals('f', br.lookAhead());
        assertEquals("foo",br.readLine());
        assertNull(br.readLine());
    }

// org.apache.commons.csv.ExtendedBufferedReaderTest::testReadChar
    public void testReadChar() {}

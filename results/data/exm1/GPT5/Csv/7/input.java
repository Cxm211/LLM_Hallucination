// buggy code
    private Map<String, Integer> initializeHeader() throws IOException {
        Map<String, Integer> hdrMap = null;
        final String[] formatHeader = this.format.getHeader();
        if (formatHeader != null) {
            hdrMap = new LinkedHashMap<String, Integer>();

            String[] header = null;
            if (formatHeader.length == 0) {
                // read the header from the first line of the file
                final CSVRecord nextRecord = this.nextRecord();
                if (nextRecord != null) {
                    header = nextRecord.values();
                }
            } else {
                if (this.format.getSkipHeaderRecord()) {
                    this.nextRecord();
                }
                header = formatHeader;
            }

            // build the name to index mappings
            if (header != null) {
                for (int i = 0; i < header.length; i++) {
                    hdrMap.put(header[i], Integer.valueOf(i));
                }
            }
        }
        return hdrMap;
    }

// relevant test
// org.apache.commons.csv.CSVFileParserTest::testCSVFile
    public void testCSVFile() throws Exception {
        String line = readTestData();
        assertNotNull("file must contain config line", line);
        final String[] split = line.split(" ");
        assertTrue(testName+" require 1 param", split.length >= 1);
         
        CSVFormat format = CSVFormat.newFormat(',').withQuoteChar('"');
        boolean checkComments = false;
        for(int i=1; i < split.length; i++) {
            final String option = split[i];
            final String[] option_parts = option.split("=",2);
            if ("IgnoreEmpty".equalsIgnoreCase(option_parts[0])){
                format = format.withIgnoreEmptyLines(Boolean.parseBoolean(option_parts[1]));
            } else if ("IgnoreSpaces".equalsIgnoreCase(option_parts[0])) {
                format = format.withIgnoreSurroundingSpaces(Boolean.parseBoolean(option_parts[1]));
            } else if ("CommentStart".equalsIgnoreCase(option_parts[0])) {
                format = format.withCommentStart(option_parts[1].charAt(0));
            } else if ("CheckComments".equalsIgnoreCase(option_parts[0])) {
                checkComments = true;
            } else {
                fail(testName+" unexpected option: "+option);
            }
        }
        line = readTestData(); 
        assertEquals(testName+" Expected format ", line, format.toString());

        
        
        final CSVParser parser = CSVParser.parse(new File(BASE, split[0]), format);
        for(final CSVRecord record : parser) {
            String parsed = record.toString();
            if (checkComments) {
                final String comment = record.getComment().replace("\n", "\\n");
                if (comment != null) {
                    parsed += "#" + comment;
                }
            }
            final int count = record.size();
            assertEquals(testName, readTestData(), count+":"+parsed);
        }
        parser.close();
    }

// org.apache.commons.csv.CSVFileParserTest::testCSVUrl
    public void testCSVUrl() {}

// org.apache.commons.csv.CSVFormatTest::testDelimiterSameAsCommentStartThrowsException
    public void testDelimiterSameAsCommentStartThrowsException() {
        CSVFormat.DEFAULT.withDelimiter('!').withCommentStart('!').validate();
    }

// org.apache.commons.csv.CSVFormatTest::testDelimiterSameAsEscapeThrowsException
    public void testDelimiterSameAsEscapeThrowsException() {
        CSVFormat.DEFAULT.withDelimiter('!').withEscape('!').validate();
    }

// org.apache.commons.csv.CSVFormatTest::testDuplicateHeaderElements
    public void testDuplicateHeaderElements() {
        CSVFormat.DEFAULT.withHeader("A", "A").validate();
    }

// org.apache.commons.csv.CSVFormatTest::testEquals
    public void testEquals() {
        final CSVFormat right = CSVFormat.DEFAULT;
        final CSVFormat left = copy(right);

        assertFalse(right.equals(null));
        assertFalse(right.equals("A String Instance"));

        assertEquals(right, right);
        assertEquals(right, left);
        assertEquals(left, right);

        assertEquals(right.hashCode(), right.hashCode());
        assertEquals(right.hashCode(), left.hashCode());
    }

// org.apache.commons.csv.CSVFormatTest::testEqualsCommentStart
    public void testEqualsCommentStart() {
        final CSVFormat right = CSVFormat.newFormat('\'')
                .withQuoteChar('"')
                .withCommentStart('#')
                .withQuotePolicy(Quote.ALL);
        final CSVFormat left = right
                .withCommentStart('!');

        assertNotEquals(right, left);
    }

// org.apache.commons.csv.CSVFormatTest::testEqualsDelimiter
    public void testEqualsDelimiter() {
        final CSVFormat right = CSVFormat.newFormat('!');
        final CSVFormat left = CSVFormat.newFormat('?');

        assertNotEquals(right, left);
    }

// org.apache.commons.csv.CSVFormatTest::testEqualsEscape
    public void testEqualsEscape() {
        final CSVFormat right = CSVFormat.newFormat('\'')
                .withQuoteChar('"')
                .withCommentStart('#')
                .withEscape('+')
                .withQuotePolicy(Quote.ALL);
        final CSVFormat left = right
                .withEscape('!');

        assertNotEquals(right, left);
    }

// org.apache.commons.csv.CSVFormatTest::testEqualsHeader
    public void testEqualsHeader() {
        final CSVFormat right = CSVFormat.newFormat('\'')
                .withRecordSeparator('*')
                .withCommentStart('#')
                .withEscape('+')
                .withHeader("One", "Two", "Three")
                .withIgnoreEmptyLines(true)
                .withIgnoreSurroundingSpaces(true)
                .withQuoteChar('"')
                .withQuotePolicy(Quote.ALL);
        final CSVFormat left = right
                .withHeader("Three", "Two", "One");

        assertNotEquals(right, left);
    }

// org.apache.commons.csv.CSVFormatTest::testEqualsIgnoreEmptyLines
    public void testEqualsIgnoreEmptyLines() {
        final CSVFormat right = CSVFormat.newFormat('\'')
                .withCommentStart('#')
                .withEscape('+')
                .withIgnoreEmptyLines(true)
                .withIgnoreSurroundingSpaces(true)
                .withQuoteChar('"')
                .withQuotePolicy(Quote.ALL);
        final CSVFormat left = right
                .withIgnoreEmptyLines(false);

        assertNotEquals(right, left);
    }

// org.apache.commons.csv.CSVFormatTest::testEqualsIgnoreSurroundingSpaces
    public void testEqualsIgnoreSurroundingSpaces() {
        final CSVFormat right = CSVFormat.newFormat('\'')
                .withCommentStart('#')
                .withEscape('+')
                .withIgnoreSurroundingSpaces(true)
                .withQuoteChar('"')
                .withQuotePolicy(Quote.ALL);
        final CSVFormat left = right
                .withIgnoreSurroundingSpaces(false);

        assertNotEquals(right, left);
    }

// org.apache.commons.csv.CSVFormatTest::testEqualsQuoteChar
    public void testEqualsQuoteChar() {
        final CSVFormat right = CSVFormat.newFormat('\'').withQuoteChar('"');
        final CSVFormat left = right.withQuoteChar('!');

        assertNotEquals(right, left);
    }

// org.apache.commons.csv.CSVFormatTest::testEqualsQuotePolicy
    public void testEqualsQuotePolicy() {
        final CSVFormat right = CSVFormat.newFormat('\'')
                .withQuoteChar('"')
                .withQuotePolicy(Quote.ALL);
        final CSVFormat left = right
                .withQuotePolicy(Quote.MINIMAL);

        assertNotEquals(right, left);
    }

// org.apache.commons.csv.CSVFormatTest::testEqualsRecordSeparator
    public void testEqualsRecordSeparator() {
        final CSVFormat right = CSVFormat.newFormat('\'')
                .withRecordSeparator('*')
                .withCommentStart('#')
                .withEscape('+')
                .withIgnoreEmptyLines(true)
                .withIgnoreSurroundingSpaces(true)
                .withQuoteChar('"')
                .withQuotePolicy(Quote.ALL);
        final CSVFormat left = right
                .withRecordSeparator('!');

        assertNotEquals(right, left);
    }

// org.apache.commons.csv.CSVFormatTest::testEscapeSameAsCommentStartThrowsException
    public void testEscapeSameAsCommentStartThrowsException() {
        CSVFormat.DEFAULT.withEscape('!').withCommentStart('!').validate();
    }

// org.apache.commons.csv.CSVFormatTest::testEscapeSameAsCommentStartThrowsExceptionForWrapperType
    public void testEscapeSameAsCommentStartThrowsExceptionForWrapperType() {
        
        CSVFormat.DEFAULT.withEscape(new Character('!')).withCommentStart(new Character('!')).validate();
    }

// org.apache.commons.csv.CSVFormatTest::testFormat
    public void testFormat() {
        final CSVFormat format = CSVFormat.DEFAULT;

        assertEquals("", format.format());
        assertEquals("a,b,c", format.format("a", "b", "c"));
        assertEquals("\"x,y\",z", format.format("x,y", "z"));
    }

// org.apache.commons.csv.CSVFormatTest::testGetHeader
    public void testGetHeader() throws Exception {
        final String[] header = new String[]{"one", "two", "three"};
        final CSVFormat formatWithHeader = CSVFormat.DEFAULT.withHeader(header);
        
        final String[] headerCopy = formatWithHeader.getHeader();
        headerCopy[0] = "A";
        headerCopy[1] = "B";
        headerCopy[2] = "C";
        assertFalse(Arrays.equals(formatWithHeader.getHeader(), headerCopy));
        assertNotSame(formatWithHeader.getHeader(), headerCopy);
    }

// org.apache.commons.csv.CSVFormatTest::testNullRecordSeparatorCsv106
    public void testNullRecordSeparatorCsv106() {
        final CSVFormat format = CSVFormat.newFormat(';').withSkipHeaderRecord(true).withHeader("H1", "H2");
        final String formatStr = format.format("A", "B");
        assertNotNull(formatStr);
        assertFalse(formatStr.endsWith("null"));
    }

// org.apache.commons.csv.CSVFormatTest::testQuoteCharSameAsCommentStartThrowsException
    public void testQuoteCharSameAsCommentStartThrowsException() {
        CSVFormat.DEFAULT.withQuoteChar('!').withCommentStart('!').validate();
    }

// org.apache.commons.csv.CSVFormatTest::testQuoteCharSameAsCommentStartThrowsExceptionForWrapperType
    public void testQuoteCharSameAsCommentStartThrowsExceptionForWrapperType() {
        
        CSVFormat.DEFAULT.withQuoteChar(new Character('!')).withCommentStart('!').validate();
    }

// org.apache.commons.csv.CSVFormatTest::testQuoteCharSameAsDelimiterThrowsException
    public void testQuoteCharSameAsDelimiterThrowsException() {
        CSVFormat.DEFAULT.withQuoteChar('!').withDelimiter('!').validate();
    }

// org.apache.commons.csv.CSVFormatTest::testQuotePolicyNoneWithoutEscapeThrowsException
    public void testQuotePolicyNoneWithoutEscapeThrowsException() {
        CSVFormat.newFormat('!').withQuotePolicy(Quote.NONE).validate();
    }

// org.apache.commons.csv.CSVFormatTest::testRFC4180
    public void testRFC4180() {
        assertEquals(null, RFC4180.getCommentStart());
        assertEquals(',', RFC4180.getDelimiter());
        assertEquals(null, RFC4180.getEscape());
        assertFalse(RFC4180.getIgnoreEmptyLines());
        assertEquals(Character.valueOf('"'), RFC4180.getQuoteChar());
        assertEquals(null, RFC4180.getQuotePolicy());
        assertEquals("\r\n", RFC4180.getRecordSeparator());
    }

// org.apache.commons.csv.CSVFormatTest::testSerialization
    public void testSerialization() throws Exception {
        final ByteArrayOutputStream out = new ByteArrayOutputStream();

        final ObjectOutputStream oos = new ObjectOutputStream(out);
        oos.writeObject(CSVFormat.DEFAULT);
        oos.flush();
        oos.close();

        final ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(out.toByteArray()));
        final CSVFormat format = (CSVFormat) in.readObject();

        assertNotNull(format);
        assertEquals("delimiter", CSVFormat.DEFAULT.getDelimiter(), format.getDelimiter());
        assertEquals("encapsulator", CSVFormat.DEFAULT.getQuoteChar(), format.getQuoteChar());
        assertEquals("comment start", CSVFormat.DEFAULT.getCommentStart(), format.getCommentStart());
        assertEquals("line separator", CSVFormat.DEFAULT.getRecordSeparator(), format.getRecordSeparator());
        assertEquals("escape", CSVFormat.DEFAULT.getEscape(), format.getEscape());
        assertEquals("trim", CSVFormat.DEFAULT.getIgnoreSurroundingSpaces(), format.getIgnoreSurroundingSpaces());
        assertEquals("empty lines", CSVFormat.DEFAULT.getIgnoreEmptyLines(), format.getIgnoreEmptyLines());
    }

// org.apache.commons.csv.CSVFormatTest::testWithCommentStart
    public void testWithCommentStart() throws Exception {
        final CSVFormat formatWithCommentStart = CSVFormat.DEFAULT.withCommentStart('#');
        assertEquals( Character.valueOf('#'), formatWithCommentStart.getCommentStart());
    }

// org.apache.commons.csv.CSVFormatTest::testWithCommentStartCRThrowsException
    public void testWithCommentStartCRThrowsException() {
        CSVFormat.DEFAULT.withCommentStart(CR).validate();
    }

// org.apache.commons.csv.CSVFormatTest::testWithDelimiter
    public void testWithDelimiter() throws Exception {
        final CSVFormat formatWithDelimiter = CSVFormat.DEFAULT.withDelimiter('!');
        assertEquals('!', formatWithDelimiter.getDelimiter());
    }

// org.apache.commons.csv.CSVFormatTest::testWithDelimiterLFThrowsException
    public void testWithDelimiterLFThrowsException() {
        CSVFormat.DEFAULT.withDelimiter(LF).validate();
    }

// org.apache.commons.csv.CSVFormatTest::testWithEscape
    public void testWithEscape() throws Exception {
        final CSVFormat formatWithEscape = CSVFormat.DEFAULT.withEscape('&');
        assertEquals(Character.valueOf('&'), formatWithEscape.getEscape());
    }

// org.apache.commons.csv.CSVFormatTest::testWithEscapeCRThrowsExceptions
    public void testWithEscapeCRThrowsExceptions() {
        CSVFormat.DEFAULT.withEscape(CR).validate();
    }

// org.apache.commons.csv.CSVFormatTest::testWithHeader
    public void testWithHeader() throws Exception {
        final String[] header = new String[]{"one", "two", "three"};
        
        final CSVFormat formatWithHeader = CSVFormat.DEFAULT.withHeader(header);
        assertArrayEquals(header, formatWithHeader.getHeader());
        assertNotSame(header, formatWithHeader.getHeader());
        header[0] = "A";
        header[1] = "B";
        header[2] = "C";
        assertFalse(Arrays.equals(formatWithHeader.getHeader(), header));
    }

// org.apache.commons.csv.CSVFormatTest::testWithIgnoreEmptyLines
    public void testWithIgnoreEmptyLines() throws Exception {
        assertFalse(CSVFormat.DEFAULT.withIgnoreEmptyLines(false).getIgnoreEmptyLines());
        assertTrue(CSVFormat.DEFAULT.withIgnoreEmptyLines(true).getIgnoreEmptyLines());
    }

// org.apache.commons.csv.CSVFormatTest::testWithIgnoreSurround
    public void testWithIgnoreSurround() throws Exception {
        assertFalse(CSVFormat.DEFAULT.withIgnoreSurroundingSpaces(false).getIgnoreSurroundingSpaces());
        assertTrue(CSVFormat.DEFAULT.withIgnoreSurroundingSpaces(true).getIgnoreSurroundingSpaces());
    }

// org.apache.commons.csv.CSVFormatTest::testWithNullString
    public void testWithNullString() throws Exception {
        final CSVFormat formatWithNullString = CSVFormat.DEFAULT.withNullString("null");
        assertEquals("null", formatWithNullString.getNullString());
    }

// org.apache.commons.csv.CSVFormatTest::testWithQuoteChar
    public void testWithQuoteChar() throws Exception {
        final CSVFormat formatWithQuoteChar = CSVFormat.DEFAULT.withQuoteChar('"');
        assertEquals(Character.valueOf('"'), formatWithQuoteChar.getQuoteChar());
    }

// org.apache.commons.csv.CSVFormatTest::testWithQuoteLFThrowsException
    public void testWithQuoteLFThrowsException() {
        CSVFormat.DEFAULT.withQuoteChar(LF).validate();
    }

// org.apache.commons.csv.CSVFormatTest::testWithQuotePolicy
    public void testWithQuotePolicy() throws Exception {
        final CSVFormat formatWithQuotePolicy = CSVFormat.DEFAULT.withQuotePolicy(Quote.ALL);
        assertEquals(Quote.ALL, formatWithQuotePolicy.getQuotePolicy());
    }

// org.apache.commons.csv.CSVFormatTest::testWithRecordSeparator
    public void testWithRecordSeparator() throws Exception {
        final CSVFormat formatWithRecordSeparator = CSVFormat.DEFAULT.withRecordSeparator('!');
        assertEquals("!", formatWithRecordSeparator.getRecordSeparator());
    }

// org.apache.commons.csv.CSVParserTest::testBackslashEscaping
    public void testBackslashEscaping() throws IOException {

        
        
        

        final String code =
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
        final String[][] res = {
                {"one", "two", "three"}, 
                {"", ""},                
                {"'", "'"},              
                {"'", "'"},              
                {"'", "'"},              
                {",", ","},              
                {"/", "/"},              
                {"/", "/"},              
                {"   8   ", "   \"quoted \"\" /\" / string\"   "},
                {"9", "   \n   "},
        };

        final CSVFormat format = CSVFormat.newFormat(',').withQuoteChar('\'')
                               .withRecordSeparator(CRLF).withEscape('/').withIgnoreEmptyLines(true);

        final CSVParser parser = CSVParser.parse(code, format);
        final List<CSVRecord> records = parser.getRecords();
        assertTrue(records.size() > 0);

        Utils.compare("Records do not match expected result", res, records);
        parser.close();
    }

// org.apache.commons.csv.CSVParserTest::testBackslashEscaping2
    public void testBackslashEscaping2() throws IOException {

        
        
        

        final String code = ""
                + " , , \n"           
                + " \t ,  , \n"       
                + " 
                + "";
        final String[][] res = {
                {" ", " ", " "},         
                {" \t ", "  ", " "},     
                {" / ", " , ", " ,"},    
        };

        final CSVFormat format = CSVFormat.newFormat(',')
                .withRecordSeparator(CRLF).withEscape('/').withIgnoreEmptyLines(true);

        final CSVParser parser = CSVParser.parse(code, format);
        final List<CSVRecord> records = parser.getRecords();
        assertTrue(records.size() > 0);

        Utils.compare("", res, records);
        parser.close();
    }

// org.apache.commons.csv.CSVParserTest::testBackslashEscapingOld
    public void testBackslashEscapingOld() throws IOException {
        final String code =
                "one,two,three\n"
                        + "on\\\"e,two\n"
                        + "on\"e,two\n"
                        + "one,\"tw\\\"o\"\n"
                        + "one,\"t\\,wo\"\n"
                        + "one,two,\"th,ree\"\n"
                        + "\"a\\\\\"\n"
                        + "a\\,b\n"
                        + "\"a\\\\,b\"";
        final String[][] res = {
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
        final CSVParser parser = CSVParser.parse(code, CSVFormat.DEFAULT);
        final List<CSVRecord> records = parser.getRecords();
        assertEquals(res.length, records.size());
        assertTrue(records.size() > 0);
        for (int i = 0; i < res.length; i++) {
            assertArrayEquals(res[i], records.get(i).values());
        }
        parser.close();
    }

// org.apache.commons.csv.CSVParserTest::testBOM
    public void testBOM() throws IOException {
        URL url = ClassLoader.getSystemClassLoader().getResource("CSVFileParser/bom.csv");
        final CSVParser parser = CSVParser.parse(url, null, CSVFormat.EXCEL.withHeader());
        try {
            for (CSVRecord record : parser) {
                final String string = record.get("Date");
                Assert.assertNotNull(string);
                
            }
        } finally {
            parser.close();
        }
    }

// org.apache.commons.csv.CSVParserTest::testBOMInputStream
    public void testBOMInputStream() {}

// org.apache.commons.csv.CSVParserTest::testCarriageReturnEndings
    public void testCarriageReturnEndings() throws IOException {
        final String code = "foo\rbaar,\rhello,world\r,kanu";
        final CSVParser parser = CSVParser.parse(code, CSVFormat.DEFAULT);
        final List<CSVRecord> records = parser.getRecords();
        assertEquals(4, records.size());
        parser.close();
    }

// org.apache.commons.csv.CSVParserTest::testCarriageReturnLineFeedEndings
    public void testCarriageReturnLineFeedEndings() throws IOException {
        final String code = "foo\r\nbaar,\r\nhello,world\r\n,kanu";
        final CSVParser parser = CSVParser.parse(code, CSVFormat.DEFAULT);
        final List<CSVRecord> records = parser.getRecords();
        assertEquals(4, records.size());
        parser.close();
    }

// org.apache.commons.csv.CSVParserTest::testClose
    public void testClose() throws Exception {
        final Reader in = new StringReader("# comment\na,b,c\n1,2,3\nx,y,z");
        final CSVParser parser = CSVFormat.DEFAULT.withCommentStart('#').withHeader().parse(in);
        final Iterator<CSVRecord> records = parser.iterator();
        assertTrue(records.hasNext());
        parser.close();
        assertFalse(records.hasNext());
        records.next();
    }

// org.apache.commons.csv.CSVParserTest::testCSV57
    public void testCSV57() throws Exception {
        final CSVParser parser = CSVParser.parse("", CSVFormat.DEFAULT);
        final List<CSVRecord> list = parser.getRecords();
        assertNotNull(list);
        assertEquals(0, list.size());
        parser.close();
    }

// org.apache.commons.csv.CSVParserTest::testDefaultFormat
    public void testDefaultFormat() throws IOException {
        final String code = ""
                + "a,b#\n"           
                + "\"\n\",\" \",#\n"   
                + "#,\"\"\n"         
                + "# Final comment\n"// 4)
                ;
        final String[][] res = {
                {"a", "b#"},
                {"\n", " ", "#"},
                {"#", ""},
                {"# Final comment"}
        };

        CSVFormat format = CSVFormat.DEFAULT;
        assertFalse(format.isCommentingEnabled());

        CSVParser parser = CSVParser.parse(code, format);
        List<CSVRecord> records = parser.getRecords();
        assertTrue(records.size() > 0);

        Utils.compare("Failed to parse without comments", res, records);

        final String[][] res_comments = {
                {"a", "b#"},
                {"\n", " ", "#"},
        };

        format = CSVFormat.DEFAULT.withCommentStart('#');
        parser.close();
        parser = CSVParser.parse(code, format);
        records = parser.getRecords();

        Utils.compare("Failed to parse with comments", res_comments, records);
        parser.close();
    }

// org.apache.commons.csv.CSVParserTest::testEmptyFile
    public void testEmptyFile() throws Exception {
        final CSVParser parser = CSVParser.parse("", CSVFormat.DEFAULT);
        assertNull(parser.nextRecord());
        parser.close();
    }

// org.apache.commons.csv.CSVParserTest::testEmptyLineBehaviourCSV
    public void testEmptyLineBehaviourCSV() throws Exception {
        final String[] codes = {
                "hello,\r\n\r\n\r\n",
                "hello,\n\n\n",
                "hello,\"\"\r\n\r\n\r\n",
                "hello,\"\"\n\n\n"
        };
        final String[][] res = {
                {"hello", ""}  
        };
        for (final String code : codes) {
            final CSVParser parser = CSVParser.parse(code, CSVFormat.DEFAULT);
            final List<CSVRecord> records = parser.getRecords();
            assertEquals(res.length, records.size());
            assertTrue(records.size() > 0);
            for (int i = 0; i < res.length; i++) {
                assertArrayEquals(res[i], records.get(i).values());
            }
            parser.close();
        }
    }

// org.apache.commons.csv.CSVParserTest::testEmptyLineBehaviourExcel
    public void testEmptyLineBehaviourExcel() throws Exception {
        final String[] codes = {
                "hello,\r\n\r\n\r\n",
                "hello,\n\n\n",
                "hello,\"\"\r\n\r\n\r\n",
                "hello,\"\"\n\n\n"
        };
        final String[][] res = {
                {"hello", ""},
                {""},  
                {""}
        };
        for (final String code : codes) {
            final CSVParser parser = CSVParser.parse(code, CSVFormat.EXCEL);
            final List<CSVRecord> records = parser.getRecords();
            assertEquals(res.length, records.size());
            assertTrue(records.size() > 0);
            for (int i = 0; i < res.length; i++) {
                assertArrayEquals(res[i], records.get(i).values());
            }
            parser.close();
        }
    }

// org.apache.commons.csv.CSVParserTest::testEndOfFileBehaviorCSV
    public void testEndOfFileBehaviorCSV() throws Exception {
        final String[] codes = {
                "hello,\r\n\r\nworld,\r\n",
                "hello,\r\n\r\nworld,",
                "hello,\r\n\r\nworld,\"\"\r\n",
                "hello,\r\n\r\nworld,\"\"",
                "hello,\r\n\r\nworld,\n",
                "hello,\r\n\r\nworld,",
                "hello,\r\n\r\nworld,\"\"\n",
                "hello,\r\n\r\nworld,\"\""
        };
        final String[][] res = {
                {"hello", ""},  
                {"world", ""}
        };
        for (final String code : codes) {
            final CSVParser parser = CSVParser.parse(code, CSVFormat.DEFAULT);
            final List<CSVRecord> records = parser.getRecords();
            assertEquals(res.length, records.size());
            assertTrue(records.size() > 0);
            for (int i = 0; i < res.length; i++) {
                assertArrayEquals(res[i], records.get(i).values());
            }
            parser.close();
        }
    }

// org.apache.commons.csv.CSVParserTest::testEndOfFileBehaviourExcel
    public void testEndOfFileBehaviourExcel() throws Exception {
        final String[] codes = {
                "hello,\r\n\r\nworld,\r\n",
                "hello,\r\n\r\nworld,",
                "hello,\r\n\r\nworld,\"\"\r\n",
                "hello,\r\n\r\nworld,\"\"",
                "hello,\r\n\r\nworld,\n",
                "hello,\r\n\r\nworld,",
                "hello,\r\n\r\nworld,\"\"\n",
                "hello,\r\n\r\nworld,\"\""
        };
        final String[][] res = {
                {"hello", ""},
                {""},  
                {"world", ""}
        };

        for (final String code : codes) {
            final CSVParser parser = CSVParser.parse(code, CSVFormat.EXCEL);
            final List<CSVRecord> records = parser.getRecords();
            assertEquals(res.length, records.size());
            assertTrue(records.size() > 0);
            for (int i = 0; i < res.length; i++) {
                assertArrayEquals(res[i], records.get(i).values());
            }
            parser.close();
        }
    }

// org.apache.commons.csv.CSVParserTest::testExcelFormat1
    public void testExcelFormat1() throws IOException {
        final String code =
                "value1,value2,value3,value4\r\na,b,c,d\r\n  x,,,"
                        + "\r\n\r\n\"\"\"hello\"\"\",\"  \"\"world\"\"\",\"abc\ndef\",\r\n";
        final String[][] res = {
                {"value1", "value2", "value3", "value4"},
                {"a", "b", "c", "d"},
                {"  x", "", "", ""},
                {""},
                {"\"hello\"", "  \"world\"", "abc\ndef", ""}
        };
        final CSVParser parser = CSVParser.parse(code, CSVFormat.EXCEL);
        final List<CSVRecord> records = parser.getRecords();
        assertEquals(res.length, records.size());
        assertTrue(records.size() > 0);
        for (int i = 0; i < res.length; i++) {
            assertArrayEquals(res[i], records.get(i).values());
        }
        parser.close();
    }

// org.apache.commons.csv.CSVParserTest::testExcelFormat2
    public void testExcelFormat2() throws Exception {
        final String code = "foo,baar\r\n\r\nhello,\r\n\r\nworld,\r\n";
        final String[][] res = {
                {"foo", "baar"},
                {""},
                {"hello", ""},
                {""},
                {"world", ""}
        };
        final CSVParser parser = CSVParser.parse(code, CSVFormat.EXCEL);
        final List<CSVRecord> records = parser.getRecords();
        assertEquals(res.length, records.size());
        assertTrue(records.size() > 0);
        for (int i = 0; i < res.length; i++) {
            assertArrayEquals(res[i], records.get(i).values());
        }
        parser.close();
    }

// org.apache.commons.csv.CSVParserTest::testForEach
    public void testForEach() throws Exception {
        final List<CSVRecord> records = new ArrayList<CSVRecord>();

        final Reader in = new StringReader("a,b,c\n1,2,3\nx,y,z");

        for (final CSVRecord record : CSVFormat.DEFAULT.parse(in)) {
            records.add(record);
        }

        assertEquals(3, records.size());
        assertArrayEquals(new String[]{"a", "b", "c"}, records.get(0).values());
        assertArrayEquals(new String[]{"1", "2", "3"}, records.get(1).values());
        assertArrayEquals(new String[]{"x", "y", "z"}, records.get(2).values());
    }

// org.apache.commons.csv.CSVParserTest::testGetHeaderMap
    public void testGetHeaderMap() throws Exception {
        final CSVParser parser = CSVParser.parse("a,b,c\n1,2,3\nx,y,z", CSVFormat.DEFAULT.withHeader("A", "B", "C"));
        final Map<String, Integer> headerMap = parser.getHeaderMap();
        final Iterator<String> columnNames = headerMap.keySet().iterator();
        
        Assert.assertEquals("A", columnNames.next());
        Assert.assertEquals("B", columnNames.next());
        Assert.assertEquals("C", columnNames.next());
        final Iterator<CSVRecord> records = parser.iterator();

        
        for (int i = 0; i < 3; i++) {
            assertTrue(records.hasNext());
            final CSVRecord record = records.next();
            assertEquals(record.get(0), record.get("A"));
            assertEquals(record.get(1), record.get("B"));
            assertEquals(record.get(2), record.get("C"));
        }

        assertFalse(records.hasNext());
        parser.close();
    }

// org.apache.commons.csv.CSVParserTest::testDuplicateHeaderEntries
    public void testDuplicateHeaderEntries() throws Exception {
        CSVParser.parse("a,b,a\n1,2,3\nx,y,z", CSVFormat.DEFAULT.withHeader(new String[]{}));
    }

// org.apache.commons.csv.CSVParserTest::testGetLine
    public void testGetLine() throws IOException {
        final CSVParser parser = CSVParser.parse(CSV_INPUT, CSVFormat.DEFAULT.withIgnoreSurroundingSpaces(true));
        for (final String[] re : RESULT) {
            assertArrayEquals(re, parser.nextRecord().values());
        }

        assertNull(parser.nextRecord());
        parser.close();
    }

// org.apache.commons.csv.CSVParserTest::testGetLineNumberWithCR
    public void testGetLineNumberWithCR() throws Exception {
        this.validateLineNumbers(String.valueOf(CR));
    }

// org.apache.commons.csv.CSVParserTest::testGetLineNumberWithCRLF
    public void testGetLineNumberWithCRLF() throws Exception {
        this.validateLineNumbers(CRLF);
    }

// org.apache.commons.csv.CSVParserTest::testGetLineNumberWithLF
    public void testGetLineNumberWithLF() throws Exception {
        this.validateLineNumbers(String.valueOf(LF));
    }

// org.apache.commons.csv.CSVParserTest::testGetOneLine
    public void testGetOneLine() throws IOException {
        final CSVParser parser = CSVParser.parse(CSV_INPUT_1, CSVFormat.DEFAULT);
        final CSVRecord record = parser.getRecords().get(0);
        assertArrayEquals(RESULT[0], record.values());
        parser.close();
    }

// org.apache.commons.csv.CSVParserTest::testGetOneLineCustomCollection
    public void testGetOneLineCustomCollection() throws IOException {
        final CSVParser parser = CSVParser.parse(CSV_INPUT_1, CSVFormat.DEFAULT);
        final CSVRecord record = parser.getRecords(new LinkedList<CSVRecord>()).getFirst();
        assertArrayEquals(RESULT[0], record.values());
        parser.close();
    }

// org.apache.commons.csv.CSVParserTest::testGetOneLineOneParser
    public void testGetOneLineOneParser() throws IOException {
        PipedWriter writer = new PipedWriter();
        PipedReader reader = new PipedReader(writer);
        final CSVFormat format = CSVFormat.DEFAULT;
        final CSVParser parser = new CSVParser(reader, format);
        try {
            writer.append(CSV_INPUT_1);
            writer.append(format.getRecordSeparator());
            final CSVRecord record1 = parser.nextRecord();
            assertArrayEquals(RESULT[0], record1.values());
            writer.append(CSV_INPUT_2);
            writer.append(format.getRecordSeparator());
            final CSVRecord record2 = parser.nextRecord();
            assertArrayEquals(RESULT[1], record2.values());
        } finally {
            parser.close();
        }
    }

// org.apache.commons.csv.CSVParserTest::testGetRecordNumberWithCR
    public void testGetRecordNumberWithCR() throws Exception {
        this.validateRecordNumbers(String.valueOf(CR));
    }

// org.apache.commons.csv.CSVParserTest::testGetRecordNumberWithCRLF
    public void testGetRecordNumberWithCRLF() throws Exception {
        this.validateRecordNumbers(CRLF);
    }

// org.apache.commons.csv.CSVParserTest::testGetRecordNumberWithLF
    public void testGetRecordNumberWithLF() throws Exception {
        this.validateRecordNumbers(String.valueOf(LF));
    }

// org.apache.commons.csv.CSVParserTest::testGetRecords
    public void testGetRecords() throws IOException {
        final CSVParser parser = CSVParser.parse(CSV_INPUT, CSVFormat.DEFAULT.withIgnoreSurroundingSpaces(true));
        final List<CSVRecord> records = parser.getRecords();
        assertEquals(RESULT.length, records.size());
        assertTrue(records.size() > 0);
        for (int i = 0; i < RESULT.length; i++) {
            assertArrayEquals(RESULT[i], records.get(i).values());
        }
        parser.close();
    }

// org.apache.commons.csv.CSVParserTest::testGetRecordWithMultiLineValues
    public void testGetRecordWithMultiLineValues() throws Exception {
        final CSVParser parser = CSVParser.parse("\"a\r\n1\",\"a\r\n2\"" + CRLF + "\"b\r\n1\",\"b\r\n2\"" + CRLF + "\"c\r\n1\",\"c\r\n2\"",
                CSVFormat.DEFAULT.withRecordSeparator(CRLF));
        CSVRecord record;
        assertEquals(0, parser.getRecordNumber());
        assertEquals(0, parser.getCurrentLineNumber());
        assertNotNull(record = parser.nextRecord());
        assertEquals(3, parser.getCurrentLineNumber());
        assertEquals(1, record.getRecordNumber());
        assertEquals(1, parser.getRecordNumber());
        assertNotNull(record = parser.nextRecord());
        assertEquals(6, parser.getCurrentLineNumber());
        assertEquals(2, record.getRecordNumber());
        assertEquals(2, parser.getRecordNumber());
        assertNotNull(record = parser.nextRecord());
        assertEquals(8, parser.getCurrentLineNumber());
        assertEquals(3, record.getRecordNumber());
        assertEquals(3, parser.getRecordNumber());
        assertNull(record = parser.nextRecord());
        assertEquals(8, parser.getCurrentLineNumber());
        assertEquals(3, parser.getRecordNumber());
        parser.close();
    }

// org.apache.commons.csv.CSVParserTest::testHeader
    public void testHeader() throws Exception {
        final Reader in = new StringReader("a,b,c\n1,2,3\nx,y,z");

        final Iterator<CSVRecord> records = CSVFormat.DEFAULT.withHeader().parse(in).iterator();

        for (int i = 0; i < 2; i++) {
            assertTrue(records.hasNext());
            final CSVRecord record = records.next();
            assertEquals(record.get(0), record.get("a"));
            assertEquals(record.get(1), record.get("b"));
            assertEquals(record.get(2), record.get("c"));
        }

        assertFalse(records.hasNext());
    }

// org.apache.commons.csv.CSVParserTest::testHeaderComment
    public void testHeaderComment() throws Exception {
        final Reader in = new StringReader("# comment\na,b,c\n1,2,3\nx,y,z");

        final Iterator<CSVRecord> records = CSVFormat.DEFAULT.withCommentStart('#').withHeader().parse(in).iterator();

        for (int i = 0; i < 2; i++) {
            assertTrue(records.hasNext());
            final CSVRecord record = records.next();
            assertEquals(record.get(0), record.get("a"));
            assertEquals(record.get(1), record.get("b"));
            assertEquals(record.get(2), record.get("c"));
        }

        assertFalse(records.hasNext());
    }

// org.apache.commons.csv.CSVParserTest::testIgnoreEmptyLines
    public void testIgnoreEmptyLines() throws IOException {
        final String code = "\nfoo,baar\n\r\n,\n\n,world\r\n\n";
        
        
        final CSVParser parser = CSVParser.parse(code, CSVFormat.DEFAULT);
        final List<CSVRecord> records = parser.getRecords();
        assertEquals(3, records.size());
        parser.close();
    }

// org.apache.commons.csv.CSVParserTest::testInvalidFormat
    public void testInvalidFormat() throws Exception {
        final CSVFormat invalidFormat = CSVFormat.DEFAULT.withDelimiter(CR);
        new CSVParser(null, invalidFormat).close();
    }

// org.apache.commons.csv.CSVParserTest::testIterator
    public void testIterator() throws Exception {
        final Reader in = new StringReader("a,b,c\n1,2,3\nx,y,z");

        final Iterator<CSVRecord> iterator = CSVFormat.DEFAULT.parse(in).iterator();

        assertTrue(iterator.hasNext());
        try {
            iterator.remove();
            fail("expected UnsupportedOperationException");
        } catch (final UnsupportedOperationException expected) {
            
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
        } catch (final NoSuchElementException e) {
            
        }
    }

// org.apache.commons.csv.CSVParserTest::testLineFeedEndings
    public void testLineFeedEndings() throws IOException {
        final String code = "foo\nbaar,\nhello,world\n,kanu";
        final CSVParser parser = CSVParser.parse(code, CSVFormat.DEFAULT);
        final List<CSVRecord> records = parser.getRecords();
        assertEquals(4, records.size());
        parser.close();
    }

// org.apache.commons.csv.CSVParserTest::testMappedButNotSetAsOutlook2007ContactExport
    public void testMappedButNotSetAsOutlook2007ContactExport() throws Exception {
        final Reader in = new StringReader("a,b,c\n1,2\nx,y,z");
        final Iterator<CSVRecord> records = CSVFormat.DEFAULT.withHeader("A", "B", "C").withSkipHeaderRecord(true)
                .parse(in).iterator();
        CSVRecord record;

        
        record = records.next();
        assertTrue(record.isMapped("A"));
        assertTrue(record.isMapped("B"));
        assertTrue(record.isMapped("C"));
        assertTrue(record.isSet("A"));
        assertTrue(record.isSet("B"));
        assertFalse(record.isSet("C"));
        assertEquals("1", record.get("A"));
        assertEquals("2", record.get("B"));
        assertFalse(record.isConsistent());

        
        record = records.next();
        assertTrue(record.isMapped("A"));
        assertTrue(record.isMapped("B"));
        assertTrue(record.isMapped("C"));
        assertTrue(record.isSet("A"));
        assertTrue(record.isSet("B"));
        assertTrue(record.isSet("C"));
        assertEquals("x", record.get("A"));
        assertEquals("y", record.get("B"));
        assertEquals("z", record.get("C"));
        assertTrue(record.isConsistent());

        assertFalse(records.hasNext());
    }

// org.apache.commons.csv.CSVParserTest::testMultipleIterators
    public void testMultipleIterators() throws Exception {
        final CSVParser parser = CSVParser.parse("a,b,c" + CR + "d,e,f", CSVFormat.DEFAULT);

        final Iterator<CSVRecord> itr1 = parser.iterator();
        final Iterator<CSVRecord> itr2 = parser.iterator();

        final CSVRecord first = itr1.next();
        assertEquals("a", first.get(0));
        assertEquals("b", first.get(1));
        assertEquals("c", first.get(2));

        final CSVRecord second = itr2.next();
        assertEquals("d", second.get(0));
        assertEquals("e", second.get(1));
        assertEquals("f", second.get(2));
        parser.close();
    }

// org.apache.commons.csv.CSVParserTest::testNewCSVParserNullReaderFormat
    public void testNewCSVParserNullReaderFormat() throws Exception {
        new CSVParser(null, CSVFormat.DEFAULT).close();
    }

// org.apache.commons.csv.CSVParserTest::testNewCSVParserReaderNullFormat
    public void testNewCSVParserReaderNullFormat() throws Exception {
        new CSVParser(new StringReader(""), null).close();
    }

// org.apache.commons.csv.CSVParserTest::testNoHeaderMap
    public void testNoHeaderMap() throws Exception {
        final CSVParser parser = CSVParser.parse("a,b,c\n1,2,3\nx,y,z", CSVFormat.DEFAULT);
        Assert.assertNull(parser.getHeaderMap());
        parser.close();
    }

// org.apache.commons.csv.CSVParserTest::testParseFileNullFormat
    public void testParseFileNullFormat() throws Exception {
        CSVParser.parse(new File(""), null);
    }

// org.apache.commons.csv.CSVParserTest::testParseNullFileFormat
    public void testParseNullFileFormat() throws Exception {
        CSVParser.parse((File) null, CSVFormat.DEFAULT);
    }

// org.apache.commons.csv.CSVParserTest::testParseNullStringFormat
    public void testParseNullStringFormat() throws Exception {
        CSVParser.parse((String) null, CSVFormat.DEFAULT);
    }

// org.apache.commons.csv.CSVParserTest::testParseNullUrlCharsetFormat
    public void testParseNullUrlCharsetFormat() throws Exception {
        CSVParser.parse(null, Charset.defaultCharset(), CSVFormat.DEFAULT);
    }

// org.apache.commons.csv.CSVParserTest::testParserUrlNullCharsetFormat
    public void testParserUrlNullCharsetFormat() throws Exception {
        final CSVParser parser = CSVParser.parse(new URL("http://commons.apache.org"), null, CSVFormat.DEFAULT);
        parser.close();
    }

// org.apache.commons.csv.CSVParserTest::testParseStringNullFormat
    public void testParseStringNullFormat() throws Exception {
        CSVParser.parse("csv data", null);
    }

// org.apache.commons.csv.CSVParserTest::testParseUrlCharsetNullFormat
    public void testParseUrlCharsetNullFormat() throws Exception {
        final CSVParser parser = CSVParser.parse(new URL("http://commons.apache.org"), Charset.defaultCharset(), null);
        parser.close();
    }

// org.apache.commons.csv.CSVParserTest::testProvidedHeader
    public void testProvidedHeader() throws Exception {
        final Reader in = new StringReader("a,b,c\n1,2,3\nx,y,z");

        final Iterator<CSVRecord> records = CSVFormat.DEFAULT.withHeader("A", "B", "C").parse(in).iterator();

        for (int i = 0; i < 3; i++) {
            assertTrue(records.hasNext());
            final CSVRecord record = records.next();
            assertTrue(record.isMapped("A"));
            assertTrue(record.isMapped("B"));
            assertTrue(record.isMapped("C"));
            assertFalse(record.isMapped("NOT MAPPED"));
            assertEquals(record.get(0), record.get("A"));
            assertEquals(record.get(1), record.get("B"));
            assertEquals(record.get(2), record.get("C"));
        }

        assertFalse(records.hasNext());
    }

// org.apache.commons.csv.CSVParserTest::testProvidedHeaderAuto
    public void testProvidedHeaderAuto() throws Exception {
        final Reader in = new StringReader("a,b,c\n1,2,3\nx,y,z");

        final Iterator<CSVRecord> records = CSVFormat.DEFAULT.withHeader().parse(in).iterator();

        for (int i = 0; i < 2; i++) {
            assertTrue(records.hasNext());
            final CSVRecord record = records.next();
            assertTrue(record.isMapped("a"));
            assertTrue(record.isMapped("b"));
            assertTrue(record.isMapped("c"));
            assertFalse(record.isMapped("NOT MAPPED"));
            assertEquals(record.get(0), record.get("a"));
            assertEquals(record.get(1), record.get("b"));
            assertEquals(record.get(2), record.get("c"));
        }

        assertFalse(records.hasNext());
    }

// org.apache.commons.csv.CSVParserTest::testRoundtrip
    public void testRoundtrip() throws Exception {
        final StringWriter out = new StringWriter();
        final CSVPrinter printer = new CSVPrinter(out, CSVFormat.DEFAULT);
        final String input = "a,b,c\r\n1,2,3\r\nx,y,z\r\n";
        for (final CSVRecord record : CSVParser.parse(input, CSVFormat.DEFAULT)) {
            printer.printRecord(record);
        }
        assertEquals(input, out.toString());
        printer.close();
    }

// org.apache.commons.csv.CSVParserTest::testSkipAutoHeader
    public void testSkipAutoHeader() throws Exception {
        final Reader in = new StringReader("a,b,c\n1,2,3\nx,y,z");
        final Iterator<CSVRecord> records = CSVFormat.DEFAULT.withHeader().parse(in).iterator();
        final CSVRecord record = records.next();
        assertEquals("1", record.get("a"));
        assertEquals("2", record.get("b"));
        assertEquals("3", record.get("c"));
    }

// org.apache.commons.csv.CSVParserTest::testSkipSetHeader
    public void testSkipSetHeader() throws Exception {
        final Reader in = new StringReader("a,b,c\n1,2,3\nx,y,z");
        final Iterator<CSVRecord> records = CSVFormat.DEFAULT.withHeader("a", "b", "c").withSkipHeaderRecord(true)
                .parse(in).iterator();
        final CSVRecord record = records.next();
        assertEquals("1", record.get("a"));
        assertEquals("2", record.get("b"));
        assertEquals("3", record.get("c"));
    }

// org.apache.commons.csv.CSVPrinterTest::testDisabledComment
    public void testDisabledComment() throws IOException {
        final StringWriter sw = new StringWriter();
        final CSVPrinter printer = new CSVPrinter(sw, CSVFormat.DEFAULT);
        printer.printComment("This is a comment");

        assertEquals("", sw.toString());
        printer.close();
    }

// org.apache.commons.csv.CSVPrinterTest::testExcelPrintAllArrayOfArrays
    public void testExcelPrintAllArrayOfArrays() throws IOException {
        final StringWriter sw = new StringWriter();
        final CSVPrinter printer = new CSVPrinter(sw, CSVFormat.EXCEL);
        printer.printRecords(new String[][] { { "r1c1", "r1c2" }, { "r2c1", "r2c2" } });
        assertEquals("r1c1,r1c2" + recordSeparator + "r2c1,r2c2" + recordSeparator, sw.toString());
        printer.close();
    }

// org.apache.commons.csv.CSVPrinterTest::testExcelPrintAllArrayOfLists
    public void testExcelPrintAllArrayOfLists() throws IOException {
        final StringWriter sw = new StringWriter();
        final CSVPrinter printer = new CSVPrinter(sw, CSVFormat.EXCEL);
        printer.printRecords(new List[] { Arrays.asList("r1c1", "r1c2"), Arrays.asList("r2c1", "r2c2") });
        assertEquals("r1c1,r1c2" + recordSeparator + "r2c1,r2c2" + recordSeparator, sw.toString());
        printer.close();
    }

// org.apache.commons.csv.CSVPrinterTest::testExcelPrintAllIterableOfArrays
    public void testExcelPrintAllIterableOfArrays() throws IOException {
        final StringWriter sw = new StringWriter();
        final CSVPrinter printer = new CSVPrinter(sw, CSVFormat.EXCEL);
        printer.printRecords(Arrays.asList(new String[][] { { "r1c1", "r1c2" }, { "r2c1", "r2c2" } }));
        assertEquals("r1c1,r1c2" + recordSeparator + "r2c1,r2c2" + recordSeparator, sw.toString());
        printer.close();
    }

// org.apache.commons.csv.CSVPrinterTest::testExcelPrintAllIterableOfLists
    public void testExcelPrintAllIterableOfLists() throws IOException {
        final StringWriter sw = new StringWriter();
        final CSVPrinter printer = new CSVPrinter(sw, CSVFormat.EXCEL);
        printer.printRecords(Arrays.asList(new List[] { Arrays.asList("r1c1", "r1c2"),
                Arrays.asList("r2c1", "r2c2") }));
        assertEquals("r1c1,r1c2" + recordSeparator + "r2c1,r2c2" + recordSeparator, sw.toString());
        printer.close();
    }

// org.apache.commons.csv.CSVPrinterTest::testExcelPrinter1
    public void testExcelPrinter1() throws IOException {
        final StringWriter sw = new StringWriter();
        final CSVPrinter printer = new CSVPrinter(sw, CSVFormat.EXCEL);
        printer.printRecord("a", "b");
        assertEquals("a,b" + recordSeparator, sw.toString());
        printer.close();
    }

// org.apache.commons.csv.CSVPrinterTest::testExcelPrinter2
    public void testExcelPrinter2() throws IOException {
        final StringWriter sw = new StringWriter();
        final CSVPrinter printer = new CSVPrinter(sw, CSVFormat.EXCEL);
        printer.printRecord("a,b", "b");
        assertEquals("\"a,b\",b" + recordSeparator, sw.toString());
        printer.close();
    }

// org.apache.commons.csv.CSVPrinterTest::testJdbcPrinter
    public void testJdbcPrinter() throws IOException, ClassNotFoundException, SQLException {
        final StringWriter sw = new StringWriter();
        Class.forName("org.h2.Driver");
        final Connection connection = DriverManager.getConnection("jdbc:h2:mem:my_test;", "sa", "");
        try {
            final Statement stmt = connection.createStatement();
            stmt.execute("CREATE TABLE TEST(ID INT PRIMARY KEY, NAME VARCHAR(255))");
            stmt.execute("insert into TEST values(1, 'r1')");
            stmt.execute("insert into TEST values(2, 'r2')");
            final CSVPrinter printer = new CSVPrinter(sw, CSVFormat.DEFAULT);
            printer.printRecords(stmt.executeQuery("select ID, NAME from TEST"));
            assertEquals("1,r1" + recordSeparator + "2,r2" + recordSeparator, sw.toString());
            printer.close();
        } finally {
            connection.close();
        }
    }

// org.apache.commons.csv.CSVPrinterTest::testMultiLineComment
    public void testMultiLineComment() throws IOException {
        final StringWriter sw = new StringWriter();
        final CSVPrinter printer = new CSVPrinter(sw, CSVFormat.DEFAULT.withCommentStart('#'));
        printer.printComment("This is a comment\non multiple lines");

        assertEquals("# This is a comment" + recordSeparator + "# on multiple lines" + recordSeparator, sw.toString());
        printer.close();
    }

// org.apache.commons.csv.CSVPrinterTest::testPrinter1
    public void testPrinter1() throws IOException {
        final StringWriter sw = new StringWriter();
        final CSVPrinter printer = new CSVPrinter(sw, CSVFormat.DEFAULT);
        printer.printRecord("a", "b");
        assertEquals("a,b" + recordSeparator, sw.toString());
        printer.close();
    }

// org.apache.commons.csv.CSVPrinterTest::testPrinter2
    public void testPrinter2() throws IOException {
        final StringWriter sw = new StringWriter();
        final CSVPrinter printer = new CSVPrinter(sw, CSVFormat.DEFAULT);
        printer.printRecord("a,b", "b");
        assertEquals("\"a,b\",b" + recordSeparator, sw.toString());
        printer.close();
    }

// org.apache.commons.csv.CSVPrinterTest::testPrinter3
    public void testPrinter3() throws IOException {
        final StringWriter sw = new StringWriter();
        final CSVPrinter printer = new CSVPrinter(sw, CSVFormat.DEFAULT);
        printer.printRecord("a, b", "b ");
        assertEquals("\"a, b\",\"b \"" + recordSeparator, sw.toString());
        printer.close();
    }

// org.apache.commons.csv.CSVPrinterTest::testPrinter4
    public void testPrinter4() throws IOException {
        final StringWriter sw = new StringWriter();
        final CSVPrinter printer = new CSVPrinter(sw, CSVFormat.DEFAULT);
        printer.printRecord("a", "b\"c");
        assertEquals("a,\"b\"\"c\"" + recordSeparator, sw.toString());
        printer.close();
    }

// org.apache.commons.csv.CSVPrinterTest::testPrinter5
    public void testPrinter5() throws IOException {
        final StringWriter sw = new StringWriter();
        final CSVPrinter printer = new CSVPrinter(sw, CSVFormat.DEFAULT);
        printer.printRecord("a", "b\nc");
        assertEquals("a,\"b\nc\"" + recordSeparator, sw.toString());
        printer.close();
    }

// org.apache.commons.csv.CSVPrinterTest::testPrinter6
    public void testPrinter6() throws IOException {
        final StringWriter sw = new StringWriter();
        final CSVPrinter printer = new CSVPrinter(sw, CSVFormat.DEFAULT);
        printer.printRecord("a", "b\r\nc");
        assertEquals("a,\"b\r\nc\"" + recordSeparator, sw.toString());
        printer.close();
    }

// org.apache.commons.csv.CSVPrinterTest::testPrinter7
    public void testPrinter7() throws IOException {
        final StringWriter sw = new StringWriter();
        final CSVPrinter printer = new CSVPrinter(sw, CSVFormat.DEFAULT);
        printer.printRecord("a", "b\\c");
        assertEquals("a,b\\c" + recordSeparator, sw.toString());
        printer.close();
    }

// org.apache.commons.csv.CSVPrinterTest::testPrintNullValues
    public void testPrintNullValues() throws IOException {
        final StringWriter sw = new StringWriter();
        final CSVPrinter printer = new CSVPrinter(sw, CSVFormat.DEFAULT);
        printer.printRecord("a", null, "b");
        assertEquals("a,,b" + recordSeparator, sw.toString());
        printer.close();
    }

// org.apache.commons.csv.CSVPrinterTest::testPrintCustomNullValues
    public void testPrintCustomNullValues() throws IOException {
        final StringWriter sw = new StringWriter();
        final CSVPrinter printer = new CSVPrinter(sw, CSVFormat.DEFAULT.withNullString("NULL"));
        printer.printRecord("a", null, "b");
        assertEquals("a,NULL,b" + recordSeparator, sw.toString());
        printer.close();
    }

// org.apache.commons.csv.CSVPrinterTest::testParseCustomNullValues
    public void testParseCustomNullValues() throws IOException {
        final StringWriter sw = new StringWriter();
        final CSVFormat format = CSVFormat.DEFAULT.withNullString("NULL");
        final CSVPrinter printer = new CSVPrinter(sw, format);
        printer.printRecord("a", null, "b");
        printer.close();
        final String csvString = sw.toString();
        assertEquals("a,NULL,b" + recordSeparator, csvString);
        final Iterable<CSVRecord> iterable = format.parse(new StringReader(csvString));
        final Iterator<CSVRecord> iterator = iterable.iterator();
        final CSVRecord record = iterator.next();
        assertEquals("a", record.get(0));
        assertEquals(null, record.get(1));
        assertEquals("b", record.get(2));
        assertFalse(iterator.hasNext());
        ((CSVParser) iterable).close();
    }

// org.apache.commons.csv.CSVPrinterTest::testQuoteAll
    public void testQuoteAll() throws IOException {
        final StringWriter sw = new StringWriter();
        final CSVPrinter printer = new CSVPrinter(sw, CSVFormat.DEFAULT.withQuotePolicy(Quote.ALL));
        printer.printRecord("a", "b\nc", "d");
        assertEquals("\"a\",\"b\nc\",\"d\"" + recordSeparator, sw.toString());
        printer.close();
    }

// org.apache.commons.csv.CSVPrinterTest::testQuoteNonNumeric
    public void testQuoteNonNumeric() throws IOException {
        final StringWriter sw = new StringWriter();
        final CSVPrinter printer = new CSVPrinter(sw, CSVFormat.DEFAULT.withQuotePolicy(Quote.NON_NUMERIC));
        printer.printRecord("a", "b\nc", Integer.valueOf(1));
        assertEquals("\"a\",\"b\nc\",1" + recordSeparator, sw.toString());
        printer.close();
    }

// org.apache.commons.csv.CSVPrinterTest::testRandom
    public void testRandom() throws Exception {
        final int iter = 10000;
        doRandom(CSVFormat.DEFAULT, iter);
        doRandom(CSVFormat.EXCEL, iter);
        doRandom(CSVFormat.MYSQL, iter);
    }

// org.apache.commons.csv.CSVPrinterTest::testPlainQuoted
    public void testPlainQuoted() throws IOException {
        final StringWriter sw = new StringWriter();
        final CSVPrinter printer = new CSVPrinter(sw, CSVFormat.DEFAULT.withQuoteChar('\''));
        printer.print("abc");
        assertEquals("abc", sw.toString());
        printer.close();
    }

// org.apache.commons.csv.CSVPrinterTest::testSingleLineComment
    public void testSingleLineComment() throws IOException {
        final StringWriter sw = new StringWriter();
        final CSVPrinter printer = new CSVPrinter(sw, CSVFormat.DEFAULT.withCommentStart('#'));
        printer.printComment("This is a comment");

        assertEquals("# This is a comment" + recordSeparator, sw.toString());
        printer.close();
    }

// org.apache.commons.csv.CSVPrinterTest::testSingleQuoteQuoted
    public void testSingleQuoteQuoted() throws IOException {
        final StringWriter sw = new StringWriter();
        final CSVPrinter printer = new CSVPrinter(sw, CSVFormat.DEFAULT.withQuoteChar('\''));
        printer.print("a'b'c");
        printer.print("xyz");
        assertEquals("'a''b''c',xyz", sw.toString());
        printer.close();
    }

// org.apache.commons.csv.CSVPrinterTest::testDelimeterQuoted
    public void testDelimeterQuoted() throws IOException {
        final StringWriter sw = new StringWriter();
        final CSVPrinter printer = new CSVPrinter(sw, CSVFormat.DEFAULT.withQuoteChar('\''));
        printer.print("a,b,c");
        printer.print("xyz");
        assertEquals("'a,b,c',xyz", sw.toString());
        printer.close();
    }

// org.apache.commons.csv.CSVPrinterTest::testDelimeterQuoteNONE
    public void testDelimeterQuoteNONE() throws IOException {
        final StringWriter sw = new StringWriter();
        final CSVFormat format = CSVFormat.DEFAULT.withEscape('!').withQuotePolicy(Quote.NONE);
        final CSVPrinter printer = new CSVPrinter(sw, format);
        printer.print("a,b,c");
        printer.print("xyz");
        assertEquals("a!,b!,c,xyz", sw.toString());
        printer.close();
    }

// org.apache.commons.csv.CSVPrinterTest::testEOLQuoted
    public void testEOLQuoted() throws IOException {
        final StringWriter sw = new StringWriter();
        final CSVPrinter printer = new CSVPrinter(sw, CSVFormat.DEFAULT.withQuoteChar('\''));
        printer.print("a\rb\nc");
        printer.print("x\by\fz");
        assertEquals("'a\rb\nc',x\by\fz", sw.toString());
        printer.close();
    }

// org.apache.commons.csv.CSVPrinterTest::testPlainEscaped
    public void testPlainEscaped() throws IOException {
        final StringWriter sw = new StringWriter();
        final CSVPrinter printer = new CSVPrinter(sw, CSVFormat.DEFAULT.withQuoteChar(null).withEscape('!'));
        printer.print("abc");
        printer.print("xyz");
        assertEquals("abc,xyz", sw.toString());
        printer.close();
    }

// org.apache.commons.csv.CSVPrinterTest::testDelimiterEscaped
    public void testDelimiterEscaped() throws IOException {
        final StringWriter sw = new StringWriter();
        final CSVPrinter printer = new CSVPrinter(sw, CSVFormat.DEFAULT.withEscape('!').withQuoteChar(null));
        printer.print("a,b,c");
        printer.print("xyz");
        assertEquals("a!,b!,c,xyz", sw.toString());
        printer.close();
    }

// org.apache.commons.csv.CSVPrinterTest::testEOLEscaped
    public void testEOLEscaped() throws IOException {
        final StringWriter sw = new StringWriter();
        final CSVPrinter printer = new CSVPrinter(sw, CSVFormat.DEFAULT.withQuoteChar(null).withEscape('!'));
        printer.print("a\rb\nc");
        printer.print("x\fy\bz");
        assertEquals("a!rb!nc,x\fy\bz", sw.toString());
        printer.close();
    }

// org.apache.commons.csv.CSVPrinterTest::testPlainPlain
    public void testPlainPlain() throws IOException {
        final StringWriter sw = new StringWriter();
        final CSVPrinter printer = new CSVPrinter(sw, CSVFormat.DEFAULT.withQuoteChar(null));
        printer.print("abc");
        printer.print("xyz");
        assertEquals("abc,xyz", sw.toString());
        printer.close();
    }

// org.apache.commons.csv.CSVPrinterTest::testDelimiterPlain
    public void testDelimiterPlain() throws IOException {
        final StringWriter sw = new StringWriter();
        final CSVPrinter printer = new CSVPrinter(sw, CSVFormat.DEFAULT.withQuoteChar(null));
        printer.print("a,b,c");
        printer.print("xyz");
        assertEquals("a,b,c,xyz", sw.toString());
        printer.close();
    }

// org.apache.commons.csv.CSVPrinterTest::testEOLPlain
    public void testEOLPlain() throws IOException {
        final StringWriter sw = new StringWriter();
        final CSVPrinter printer = new CSVPrinter(sw, CSVFormat.DEFAULT.withQuoteChar(null));
        printer.print("a\rb\nc");
        printer.print("x\fy\bz");
        assertEquals("a\rb\nc,x\fy\bz", sw.toString());
        printer.close();
    }

// org.apache.commons.csv.CSVPrinterTest::testInvalidFormat
    public void testInvalidFormat() throws Exception {
        final CSVFormat invalidFormat = CSVFormat.DEFAULT.withDelimiter(CR);
        new CSVPrinter(new StringWriter(), invalidFormat).close();
    }

// org.apache.commons.csv.CSVPrinterTest::testNewCSVPrinterNullAppendableFormat
    public void testNewCSVPrinterNullAppendableFormat() throws Exception {
        new CSVPrinter(null, CSVFormat.DEFAULT).close();
    }

// org.apache.commons.csv.CSVPrinterTest::testNewCsvPrinterAppendableNullFormat
    public void testNewCsvPrinterAppendableNullFormat() throws Exception {
        new CSVPrinter(new StringWriter(), null).close();
    }

// org.apache.commons.csv.CSVRecordTest::testGetInt
    public void testGetInt() {
        assertEquals(values[0], record.get(0));
        assertEquals(values[1], record.get(1));
        assertEquals(values[2], record.get(2));
    }

// org.apache.commons.csv.CSVRecordTest::testGetString
    public void testGetString() {
        assertEquals(values[0], recordWithHeader.get("first"));
        assertEquals(values[1], recordWithHeader.get("second"));
        assertEquals(values[2], recordWithHeader.get("third"));
    }

// org.apache.commons.csv.CSVRecordTest::testGetStringInconsistentRecord
    public void testGetStringInconsistentRecord() {
        header.put("fourth", Integer.valueOf(4));
        recordWithHeader.get("fourth");
    }

// org.apache.commons.csv.CSVRecordTest::testGetStringNoHeader
    public void testGetStringNoHeader() {
        record.get("first");
    }

// org.apache.commons.csv.CSVRecordTest::testGetUnmappedEnum
    public void testGetUnmappedEnum() {
        assertNull(recordWithHeader.get(EnumFixture.UNKNOWN_COLUMN));
    }

// org.apache.commons.csv.CSVRecordTest::testGetUnmappedName
    public void testGetUnmappedName() {
        assertNull(recordWithHeader.get("fourth"));
    }

// org.apache.commons.csv.CSVRecordTest::testGetUnmappedNegativeInt
    public void testGetUnmappedNegativeInt() {
        assertNull(recordWithHeader.get(Integer.MIN_VALUE));
    }

// org.apache.commons.csv.CSVRecordTest::testGetUnmappedPositiveInt
    public void testGetUnmappedPositiveInt() {
        assertNull(recordWithHeader.get(Integer.MAX_VALUE));
    }

// org.apache.commons.csv.CSVRecordTest::testIsConsistent
    public void testIsConsistent() {
        assertTrue(record.isConsistent());
        assertTrue(recordWithHeader.isConsistent());

        header.put("fourth", Integer.valueOf(4));
        assertFalse(recordWithHeader.isConsistent());
    }

// org.apache.commons.csv.CSVRecordTest::testIsMapped
    public void testIsMapped() {
        assertFalse(record.isMapped("first"));
        assertTrue(recordWithHeader.isMapped("first"));
        assertFalse(recordWithHeader.isMapped("fourth"));
    }

// org.apache.commons.csv.CSVRecordTest::testIsSet
    public void testIsSet() {
        assertFalse(record.isSet("first"));
        assertTrue(recordWithHeader.isSet("first"));
        assertFalse(recordWithHeader.isSet("fourth"));
    }

// org.apache.commons.csv.CSVRecordTest::testIterator
    public void testIterator() {
        int i = 0;
        for (final String value : record) {
            assertEquals(values[i], value);
            i++;
        }
    }

// org.apache.commons.csv.CSVRecordTest::testPutInMap
    public void testPutInMap() {
        final Map<String, String> map = new ConcurrentHashMap<String, String>();
        this.recordWithHeader.putIn(map);
        this.validateMap(map, false);
        
        final TreeMap<String, String> map2 = recordWithHeader.putIn(new TreeMap<String, String>());
        this.validateMap(map2, false);
    }

// org.apache.commons.csv.CSVRecordTest::testRemoveAndAddColumns
    public void testRemoveAndAddColumns() throws IOException {
        
        final CSVPrinter printer = new CSVPrinter(new StringBuilder(), CSVFormat.DEFAULT);
        final Map<String, String> map = recordWithHeader.toMap();
        map.remove("OldColumn");
        map.put("ZColumn", "NewValue");
        
        final ArrayList<String> list = new ArrayList<String>(map.values());
        Collections.sort(list);
        printer.printRecord(list);
        Assert.assertEquals("A,B,C,NewValue" + CSVFormat.DEFAULT.getRecordSeparator(), printer.getOut().toString());
        printer.close();
    }

// org.apache.commons.csv.CSVRecordTest::testToMap
    public void testToMap() {
        final Map<String, String> map = this.recordWithHeader.toMap();
        this.validateMap(map, true);
    }

// org.apache.commons.csv.CSVRecordTest::testToMapWithShortRecord
    public void testToMapWithShortRecord() throws Exception {
       final CSVParser parser =  CSVParser.parse("a,b", CSVFormat.DEFAULT.withHeader("A", "B", "C"));
       final CSVRecord shortRec = parser.iterator().next();
       shortRec.toMap();
    }

// buggy code
    int readEscape() throws IOException {
        // the escape char has just been read (normally a backslash)
        final int c = in.read();
        switch (c) {
        case 'r':
            return CR;
        case 'n':
            return LF;
        case 't':
            return TAB;
        case 'b':
            return BACKSPACE;
        case 'f':
            return FF;
        case CR:
        case LF:
        case FF: // TODO is this correct?
        case TAB: // TODO is this correct? Do tabs need to be escaped?
        case BACKSPACE: // TODO is this correct?
            return c;
        case END_OF_STREAM:
            throw new IOException("EOF whilst processing escape sequence");
        default:
            // Now check for meta-characters
                return c;
            // indicate unexpected char - available from in.getLastChar()
        }
    }

// relevant test
// org.apache.commons.csv.CSVFileParserTest::testCSVFile
    public void testCSVFile() throws Exception {
        String line = readTestData();
        assertNotNull("file must contain config line", line);
        final String[] split = line.split(" ");
        assertTrue(testName+" require 1 param", split.length >= 1);
         
        final BufferedReader csvFile = new BufferedReader(new FileReader(new File(BASE, split[0])));
        final CSVFormatBuilder builder = CSVFormat.newBuilder(',').withQuoteChar('"');
        CSVFormat format = builder.build(); 
        boolean checkComments = false;
        for(int i=1; i < split.length; i++) {
            final String option = split[i];
            final String[] option_parts = option.split("=",2);
            if ("IgnoreEmpty".equalsIgnoreCase(option_parts[0])){
                format = builder.withIgnoreEmptyLines(Boolean.parseBoolean(option_parts[1])).build();
            } else if ("IgnoreSpaces".equalsIgnoreCase(option_parts[0])) {
                format = builder.withIgnoreSurroundingSpaces(Boolean.parseBoolean(option_parts[1])).build();
            } else if ("CommentStart".equalsIgnoreCase(option_parts[0])) {
                format = builder.withCommentStart(option_parts[1].charAt(0)).build();
            } else if ("CheckComments".equalsIgnoreCase(option_parts[0])) {
                checkComments = true;
            } else {
                fail(testName+" unexpected option: "+option);
            }
        }
        line = readTestData(); 
        assertEquals(testName+" Expected format ", line, format.toString());

        
        for(final CSVRecord record : format.parse(csvFile)) {
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
    }

// org.apache.commons.csv.CSVLexerTest::testSurroundingSpacesAreDeleted
    public void testSurroundingSpacesAreDeleted() throws IOException {
        final String code = "noSpaces,  leadingSpaces,trailingSpaces  ,  surroundingSpaces  ,  ,,";
        final Lexer parser = getLexer(code, CSVFormat.newBuilder().withIgnoreSurroundingSpaces(true).build());
        assertThat(parser.nextToken(new Token()), matches(TOKEN, "noSpaces"));
        assertThat(parser.nextToken(new Token()), matches(TOKEN, "leadingSpaces"));
        assertThat(parser.nextToken(new Token()), matches(TOKEN, "trailingSpaces"));
        assertThat(parser.nextToken(new Token()), matches(TOKEN, "surroundingSpaces"));
        assertThat(parser.nextToken(new Token()), matches(TOKEN, ""));
        assertThat(parser.nextToken(new Token()), matches(TOKEN, ""));
        assertThat(parser.nextToken(new Token()), matches(EOF, ""));
    }

// org.apache.commons.csv.CSVLexerTest::testSurroundingTabsAreDeleted
    public void testSurroundingTabsAreDeleted() throws IOException {
        final String code = "noTabs,\tleadingTab,trailingTab\t,\tsurroundingTabs\t,\t\t,,";
        final Lexer parser = getLexer(code, CSVFormat.newBuilder().withIgnoreSurroundingSpaces(true).build());
        assertThat(parser.nextToken(new Token()), matches(TOKEN, "noTabs"));
        assertThat(parser.nextToken(new Token()), matches(TOKEN, "leadingTab"));
        assertThat(parser.nextToken(new Token()), matches(TOKEN, "trailingTab"));
        assertThat(parser.nextToken(new Token()), matches(TOKEN, "surroundingTabs"));
        assertThat(parser.nextToken(new Token()), matches(TOKEN, ""));
        assertThat(parser.nextToken(new Token()), matches(TOKEN, ""));
        assertThat(parser.nextToken(new Token()), matches(EOF, ""));
    }

// org.apache.commons.csv.CSVLexerTest::testIgnoreEmptyLines
    public void testIgnoreEmptyLines() throws IOException {
        final String code =
                "first,line,\n"+
                "\n"+
                "\n"+
                "second,line\n"+
                "\n"+
                "\n"+
                "third line \n"+
                "\n"+
                "\n"+
                "last, line \n"+
                "\n"+
                "\n"+
                "\n";
        final CSVFormat format = CSVFormat.newBuilder().withIgnoreEmptyLines(true).build();
        final Lexer parser = getLexer(code, format);

        assertThat(parser.nextToken(new Token()), matches(TOKEN, "first"));
        assertThat(parser.nextToken(new Token()), matches(TOKEN, "line"));
        assertThat(parser.nextToken(new Token()), matches(EORECORD, ""));
        assertThat(parser.nextToken(new Token()), matches(TOKEN, "second"));
        assertThat(parser.nextToken(new Token()), matches(EORECORD, "line"));
        assertThat(parser.nextToken(new Token()), matches(EORECORD, "third line "));
        assertThat(parser.nextToken(new Token()), matches(TOKEN, "last"));
        assertThat(parser.nextToken(new Token()), matches(EORECORD, " line "));
        assertThat(parser.nextToken(new Token()), matches(EOF, ""));
        assertThat(parser.nextToken(new Token()), matches(EOF, ""));
    }

// org.apache.commons.csv.CSVLexerTest::testComments
    public void testComments() throws IOException {
        final String code =
                "first,line,\n"+
                "second,line,tokenWith#no-comment\n"+
                "# comment line \n"+
                "third,line,#no-comment\n"+
                "# penultimate comment\n"+
                "# Final comment\n";
        final CSVFormat format = CSVFormat.newBuilder().withCommentStart('#').build();
        final Lexer parser = getLexer(code, format);

        assertThat(parser.nextToken(new Token()), matches(TOKEN, "first"));
        assertThat(parser.nextToken(new Token()), matches(TOKEN, "line"));
        assertThat(parser.nextToken(new Token()), matches(EORECORD, ""));
        assertThat(parser.nextToken(new Token()), matches(TOKEN, "second"));
        assertThat(parser.nextToken(new Token()), matches(TOKEN, "line"));
        assertThat(parser.nextToken(new Token()), matches(EORECORD, "tokenWith#no-comment"));
        assertThat(parser.nextToken(new Token()), matches(COMMENT, "comment line"));
        assertThat(parser.nextToken(new Token()), matches(TOKEN, "third"));
        assertThat(parser.nextToken(new Token()), matches(TOKEN, "line"));
        assertThat(parser.nextToken(new Token()), matches(EORECORD, "#no-comment"));
        assertThat(parser.nextToken(new Token()), matches(COMMENT, "penultimate comment"));
        assertThat(parser.nextToken(new Token()), matches(COMMENT, "Final comment"));
        assertThat(parser.nextToken(new Token()), matches(EOF, ""));
        assertThat(parser.nextToken(new Token()), matches(EOF, ""));
    }

// org.apache.commons.csv.CSVLexerTest::testCommentsAndEmptyLines
    public void testCommentsAndEmptyLines() throws IOException {
        final String code =
                "1,2,3,\n"+                
                "\n"+                      
                "\n"+                      
                "a,b x,c#no-comment\n"+    
                "#foo\n"+                  
                "\n"+                      
                "\n"+                      
                "d,e,#no-comment\n"+       
                "\n"+                      
                "\n"+                      
                "# penultimate comment\n"+ 
                "\n"+                      
                "\n"+                      
                "# Final comment\n";       
        final CSVFormat format = CSVFormat.newBuilder().withCommentStart('#').withIgnoreEmptyLines(false).build();
        assertFalse("Should not ignore empty lines", format.getIgnoreEmptyLines());

        final Lexer parser = getLexer(code, format);

        assertThat(parser.nextToken(new Token()), matches(TOKEN, "1"));
        assertThat(parser.nextToken(new Token()), matches(TOKEN, "2"));
        assertThat(parser.nextToken(new Token()), matches(TOKEN, "3"));
        assertThat(parser.nextToken(new Token()), matches(EORECORD, ""));             
        assertThat(parser.nextToken(new Token()), matches(EORECORD, ""));             
        assertThat(parser.nextToken(new Token()), matches(EORECORD, ""));             
        assertThat(parser.nextToken(new Token()), matches(TOKEN, "a"));
        assertThat(parser.nextToken(new Token()), matches(TOKEN, "b x"));
        assertThat(parser.nextToken(new Token()), matches(EORECORD, "c#no-comment")); 
        assertThat(parser.nextToken(new Token()), matches(COMMENT, "foo"));           
        assertThat(parser.nextToken(new Token()), matches(EORECORD, ""));             
        assertThat(parser.nextToken(new Token()), matches(EORECORD, ""));             
        assertThat(parser.nextToken(new Token()), matches(TOKEN, "d"));
        assertThat(parser.nextToken(new Token()), matches(TOKEN, "e"));
        assertThat(parser.nextToken(new Token()), matches(EORECORD, "#no-comment"));  
        assertThat(parser.nextToken(new Token()), matches(EORECORD, ""));             
        assertThat(parser.nextToken(new Token()), matches(EORECORD, ""));             
        assertThat(parser.nextToken(new Token()), matches(COMMENT, "penultimate comment"));              
        assertThat(parser.nextToken(new Token()), matches(EORECORD, ""));             
        assertThat(parser.nextToken(new Token()), matches(EORECORD, ""));             
        assertThat(parser.nextToken(new Token()), matches(COMMENT, "Final comment"));              
        assertThat(parser.nextToken(new Token()), matches(EOF, ""));
        assertThat(parser.nextToken(new Token()), matches(EOF, ""));

    }

// org.apache.commons.csv.CSVLexerTest::testBackslashWithoutEscaping
    public void testBackslashWithoutEscaping() throws IOException {
        
        final String code = "a,\\,,b\\\n\\,,";
        final CSVFormat format = CSVFormat.DEFAULT;
        assertFalse(format.isEscaping());
        final Lexer parser = getLexer(code, format);

        assertThat(parser.nextToken(new Token()), matches(TOKEN, "a"));
        
        assertThat(parser.nextToken(new Token()), matches(TOKEN, "\\"));
        assertThat(parser.nextToken(new Token()), matches(TOKEN, ""));
        assertThat(parser.nextToken(new Token()), matches(EORECORD, "b\\"));
        
        assertThat(parser.nextToken(new Token()), matches(TOKEN, "\\"));
        assertThat(parser.nextToken(new Token()), matches(TOKEN, ""));
        assertThat(parser.nextToken(new Token()), matches(EOF, ""));
    }

// org.apache.commons.csv.CSVLexerTest::testBackslashWithEscaping
    public void testBackslashWithEscaping() throws IOException {
        
        final String code = "a,\\,,b\\\\\n\\,,\\\nc,d\\\r\ne";
        final CSVFormat format = formatWithEscaping.toBuilder().withIgnoreEmptyLines(false).build();
        assertTrue(format.isEscaping());
        final Lexer parser = getLexer(code, format);

        assertThat(parser.nextToken(new Token()), matches(TOKEN, "a"));
        assertThat(parser.nextToken(new Token()), matches(TOKEN, ","));
        assertThat(parser.nextToken(new Token()), matches(EORECORD, "b\\"));
        assertThat(parser.nextToken(new Token()), matches(TOKEN, ","));
        assertThat(parser.nextToken(new Token()), matches(TOKEN, "\nc"));
        assertThat(parser.nextToken(new Token()), matches(EORECORD, "d\r"));
        assertThat(parser.nextToken(new Token()), matches(EOF, "e"));
    }

// org.apache.commons.csv.CSVLexerTest::testNextToken4
    public void testNextToken4() throws IOException {
        
        final String code = "a,\"foo\",b\na,   \" foo\",b\na,\"foo \"  ,b\na,  \" foo \"  ,b";
        final Lexer parser = getLexer(code, CSVFormat.newBuilder().withIgnoreSurroundingSpaces(true).build());
        assertThat(parser.nextToken(new Token()), matches(TOKEN, "a"));
        assertThat(parser.nextToken(new Token()), matches(TOKEN, "foo"));
        assertThat(parser.nextToken(new Token()), matches(EORECORD, "b"));
        assertThat(parser.nextToken(new Token()), matches(TOKEN, "a"));
        assertThat(parser.nextToken(new Token()), matches(TOKEN, " foo"));
        assertThat(parser.nextToken(new Token()), matches(EORECORD, "b"));
        assertThat(parser.nextToken(new Token()), matches(TOKEN, "a"));
        assertThat(parser.nextToken(new Token()), matches(TOKEN, "foo "));
        assertThat(parser.nextToken(new Token()), matches(EORECORD, "b"));
        assertThat(parser.nextToken(new Token()), matches(TOKEN, "a"));
        assertThat(parser.nextToken(new Token()), matches(TOKEN, " foo "));

        assertThat(parser.nextToken(new Token()), matches(EOF, "b"));
    }

// org.apache.commons.csv.CSVLexerTest::testNextToken5
    public void testNextToken5() throws IOException {
        final String code = "a,\"foo\n\",b\n\"foo\n  baar ,,,\"\n\"\n\t \n\"";
        final Lexer parser = getLexer(code, CSVFormat.DEFAULT);
        assertThat(parser.nextToken(new Token()), matches(TOKEN, "a"));
        assertThat(parser.nextToken(new Token()), matches(TOKEN, "foo\n"));
        assertThat(parser.nextToken(new Token()), matches(EORECORD, "b"));
        assertThat(parser.nextToken(new Token()), matches(EORECORD, "foo\n  baar ,,,"));
        assertThat(parser.nextToken(new Token()), matches(EOF, "\n\t \n"));

    }

// org.apache.commons.csv.CSVLexerTest::testNextToken6
    public void testNextToken6() throws IOException {
        
        final String code = "a;'b and '' more\n'\n!comment;;;;\n;;";
        final CSVFormat format = CSVFormat.newBuilder().withDelimiter(';').withQuoteChar('\'').withCommentStart('!').build();
        final Lexer parser = getLexer(code, format);
        assertThat(parser.nextToken(new Token()), matches(TOKEN, "a"));
        assertThat(parser.nextToken(new Token()), matches(EORECORD, "b and ' more\n"));
    }

// org.apache.commons.csv.CSVLexerTest::testDelimiterIsWhitespace
    public void testDelimiterIsWhitespace() throws IOException {
        final String code = "one\ttwo\t\tfour \t five\t six";
        final Lexer parser = getLexer(code, CSVFormat.TDF);
        assertThat(parser.nextToken(new Token()), matches(TOKEN, "one"));
        assertThat(parser.nextToken(new Token()), matches(TOKEN, "two"));
        assertThat(parser.nextToken(new Token()), matches(TOKEN, ""));
        assertThat(parser.nextToken(new Token()), matches(TOKEN, "four"));
        assertThat(parser.nextToken(new Token()), matches(TOKEN, "five"));
        assertThat(parser.nextToken(new Token()), matches(EOF, "six"));
    }

// org.apache.commons.csv.CSVLexerTest::testEscapedCR
    public void testEscapedCR() throws Exception {
        final Lexer lexer = getLexer("character\\" + CR + "Escaped", formatWithEscaping);
        assertThat(lexer.nextToken(new Token()), hasContent("character" + CR + "Escaped"));
    }

// org.apache.commons.csv.CSVLexerTest::testEscapedLF
    public void testEscapedLF() throws Exception {
        final Lexer lexer = getLexer("character\\" + LF + "Escaped", formatWithEscaping);
        assertThat(lexer.nextToken(new Token()), hasContent("character" + LF + "Escaped"));
    }

// org.apache.commons.csv.CSVLexerTest::testEscapedTab
    public void testEscapedTab() throws Exception {
        final Lexer lexer = getLexer("character\\" + TAB + "Escaped", formatWithEscaping);
        assertThat(lexer.nextToken(new Token()), hasContent("character" + TAB + "Escaped"));
    }

// org.apache.commons.csv.CSVLexerTest::testEscapeBackspace
    public void testEscapeBackspace() throws Exception {
        final Lexer lexer = getLexer("character\\" + BACKSPACE + "Escaped", formatWithEscaping);
        assertThat(lexer.nextToken(new Token()), hasContent("character" + BACKSPACE + "Escaped"));
    }

// org.apache.commons.csv.CSVLexerTest::testEscapeFF
    public void testEscapeFF() throws Exception {
        final Lexer lexer = getLexer("character\\" + FF + "Escaped", formatWithEscaping);
        assertThat(lexer.nextToken(new Token()), hasContent("character" + FF + "Escaped"));
    }

// org.apache.commons.csv.CSVLexerTest::testEscapedMySqlNullValue
    public void testEscapedMySqlNullValue() throws Exception {
        
        final Lexer lexer = getLexer("character\\NEscaped", formatWithEscaping);
        assertThat(lexer.nextToken(new Token()), hasContent("character\\NEscaped"));
    }

// org.apache.commons.csv.CSVLexerTest::testEscapedCharacter
    public void testEscapedCharacter() throws Exception {
        final Lexer lexer = getLexer("character\\aEscaped", formatWithEscaping);
        assertThat(lexer.nextToken(new Token()), hasContent("character\\aEscaped"));
    }

// org.apache.commons.csv.CSVLexerTest::testEscapedControlCharacter
    public void testEscapedControlCharacter() throws Exception {
        
        final Lexer lexer = getLexer("character!rEscaped", CSVFormat.newBuilder().withEscape('!').build());
        assertThat(lexer.nextToken(new Token()), hasContent("character" + CR + "Escaped"));
    }

// org.apache.commons.csv.CSVLexerTest::testEscapedControlCharacter2
    public void testEscapedControlCharacter2() throws Exception {
        final Lexer lexer = getLexer("character\\rEscaped", CSVFormat.newBuilder().withEscape('\\').build());
        assertThat(lexer.nextToken(new Token()), hasContent("character" + CR + "Escaped"));
    }

// org.apache.commons.csv.CSVLexerTest::testEscapingAtEOF
    public void testEscapingAtEOF() throws Exception {
        final String code = "escaping at EOF is evil\\";
        final Lexer lexer = getLexer(code, formatWithEscaping);

        lexer.nextToken(new Token());
    }

// org.apache.commons.csv.CSVParserTest::testGetLine
    public void testGetLine() throws IOException {
        final CSVParser parser = new CSVParser(new StringReader(CSVINPUT), CSVFormat.newBuilder().withIgnoreSurroundingSpaces(true).build());
        for (final String[] re : RESULT) {
            assertArrayEquals(re, parser.nextRecord().values());
        }

        assertNull(parser.nextRecord());
    }

// org.apache.commons.csv.CSVParserTest::testGetRecords
    public void testGetRecords() throws IOException {
        final CSVParser parser = new CSVParser(new StringReader(CSVINPUT), CSVFormat.newBuilder().withIgnoreSurroundingSpaces(true).build());
        final List<CSVRecord> records = parser.getRecords();
        assertEquals(RESULT.length, records.size());
        assertTrue(records.size() > 0);
        for (int i = 0; i < RESULT.length; i++) {
            assertArrayEquals(RESULT[i], records.get(i).values());
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
        final CSVParser parser = new CSVParser(code, CSVFormat.EXCEL);
        final List<CSVRecord> records = parser.getRecords();
        assertEquals(res.length, records.size());
        assertTrue(records.size() > 0);
        for (int i = 0; i < res.length; i++) {
            assertArrayEquals(res[i], records.get(i).values());
        }
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
        final CSVParser parser = new CSVParser(code, CSVFormat.EXCEL);
        final List<CSVRecord> records = parser.getRecords();
        assertEquals(res.length, records.size());
        assertTrue(records.size() > 0);
        for (int i = 0; i < res.length; i++) {
            assertArrayEquals(res[i], records.get(i).values());
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
            final CSVParser parser = new CSVParser(code, CSVFormat.EXCEL);
            final List<CSVRecord> records = parser.getRecords();
            assertEquals(res.length, records.size());
            assertTrue(records.size() > 0);
            for (int i = 0; i < res.length; i++) {
                assertArrayEquals(res[i], records.get(i).values());
            }
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
            final CSVParser parser = new CSVParser(new StringReader(code));
            final List<CSVRecord> records = parser.getRecords();
            assertEquals(res.length, records.size());
            assertTrue(records.size() > 0);
            for (int i = 0; i < res.length; i++) {
                assertArrayEquals(res[i], records.get(i).values());
            }
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
            final CSVParser parser = new CSVParser(code, CSVFormat.EXCEL);
            final List<CSVRecord> records = parser.getRecords();
            assertEquals(res.length, records.size());
            assertTrue(records.size() > 0);
            for (int i = 0; i < res.length; i++) {
                assertArrayEquals(res[i], records.get(i).values());
            }
        }
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
            final CSVParser parser = new CSVParser(new StringReader(code));
            final List<CSVRecord> records = parser.getRecords();
            assertEquals(res.length, records.size());
            assertTrue(records.size() > 0);
            for (int i = 0; i < res.length; i++) {
                assertArrayEquals(res[i], records.get(i).values());
            }
        }
    }

// org.apache.commons.csv.CSVParserTest::testEmptyFile
    public void testEmptyFile() throws Exception {
        final CSVParser parser = new CSVParser("", CSVFormat.DEFAULT);
        assertNull(parser.nextRecord());
    }

// org.apache.commons.csv.CSVParserTest::testCSV57
    public void testCSV57() throws Exception {
        final CSVParser parser = new CSVParser("", CSVFormat.DEFAULT);
        final List<CSVRecord> list = parser.getRecords();
        assertNotNull(list);
        assertEquals(0, list.size());
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
        final CSVParser parser = new CSVParser(new StringReader(code));
        final List<CSVRecord> records = parser.getRecords();
        assertEquals(res.length, records.size());
        assertTrue(records.size() > 0);
        for (int i = 0; i < res.length; i++) {
            assertArrayEquals(res[i], records.get(i).values());
        }
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

        final CSVFormat format = CSVFormat.newBuilder(',').withQuoteChar('\'').withEscape('/')
                               .withIgnoreEmptyLines(true).withRecordSeparator(CRLF).build();

        final CSVParser parser = new CSVParser(code, format);
        final List<CSVRecord> records = parser.getRecords();
        assertTrue(records.size() > 0);

        Utils.compare("Records do not match expected result", res, records);
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

        final CSVFormat format = CSVFormat.newBuilder(',').withEscape('/')
                .withIgnoreEmptyLines(true).withRecordSeparator(CRLF).build();

        final CSVParser parser = new CSVParser(code, format);
        final List<CSVRecord> records = parser.getRecords();
        assertTrue(records.size() > 0);

        Utils.compare("", res, records);
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

        CSVParser parser = new CSVParser(code, format);
        List<CSVRecord> records = parser.getRecords();
        assertTrue(records.size() > 0);

        Utils.compare("Failed to parse without comments", res, records);

        final String[][] res_comments = {
                {"a", "b#"},
                {"\n", " ", "#"},
        };

        format = CSVFormat.newBuilder().withCommentStart('#').build();
        parser = new CSVParser(code, format);
        records = parser.getRecords();

        Utils.compare("Failed to parse with comments", res_comments, records);
    }

// org.apache.commons.csv.CSVParserTest::testCarriageReturnLineFeedEndings
    public void testCarriageReturnLineFeedEndings() throws IOException {
        final String code = "foo\r\nbaar,\r\nhello,world\r\n,kanu";
        final CSVParser parser = new CSVParser(new StringReader(code));
        final List<CSVRecord> records = parser.getRecords();
        assertEquals(4, records.size());
    }

// org.apache.commons.csv.CSVParserTest::testCarriageReturnEndings
    public void testCarriageReturnEndings() throws IOException {
        final String code = "foo\rbaar,\rhello,world\r,kanu";
        final CSVParser parser = new CSVParser(new StringReader(code));
        final List<CSVRecord> records = parser.getRecords();
        assertEquals(4, records.size());
    }

// org.apache.commons.csv.CSVParserTest::testLineFeedEndings
    public void testLineFeedEndings() throws IOException {
        final String code = "foo\nbaar,\nhello,world\n,kanu";
        final CSVParser parser = new CSVParser(new StringReader(code));
        final List<CSVRecord> records = parser.getRecords();
        assertEquals(4, records.size());
    }

// org.apache.commons.csv.CSVParserTest::testIgnoreEmptyLines
    public void testIgnoreEmptyLines() throws IOException {
        final String code = "\nfoo,baar\n\r\n,\n\n,world\r\n\n";
        
        
        final CSVParser parser = new CSVParser(new StringReader(code));
        final List<CSVRecord> records = parser.getRecords();
        assertEquals(3, records.size());
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

// org.apache.commons.csv.CSVParserTest::testRoundtrip
    public void testRoundtrip() throws Exception {
        final StringWriter out = new StringWriter();
        final CSVPrinter printer = new CSVPrinter(out, CSVFormat.DEFAULT);
        final String input = "a,b,c\r\n1,2,3\r\nx,y,z\r\n";
        for (final CSVRecord record : CSVFormat.DEFAULT.parse(new StringReader(input))) {
            printer.printRecord(record);
        }
        assertEquals(input, out.toString());
        printer.close();
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

// org.apache.commons.csv.CSVParserTest::testHeader
    public void testHeader() throws Exception {
        final Reader in = new StringReader("a,b,c\n1,2,3\nx,y,z");

        final Iterator<CSVRecord> records = CSVFormat.newBuilder().withHeader().parse(in).iterator();

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

        final Iterator<CSVRecord> records = CSVFormat.newBuilder().withCommentStart('#').withHeader().parse(in).iterator();

        for (int i = 0; i < 2; i++) {
            assertTrue(records.hasNext());
            final CSVRecord record = records.next();
            assertEquals(record.get(0), record.get("a"));
            assertEquals(record.get(1), record.get("b"));
            assertEquals(record.get(2), record.get("c"));
        }

        assertFalse(records.hasNext());
    }

// org.apache.commons.csv.CSVParserTest::testProvidedHeader
    public void testProvidedHeader() throws Exception {
        final Reader in = new StringReader("a,b,c\n1,2,3\nx,y,z");

        final Iterator<CSVRecord> records = CSVFormat.newBuilder().withHeader("A", "B", "C").parse(in).iterator();

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

// org.apache.commons.csv.CSVParserTest::testMappedButNotSetAsOutlook2007ContactExport
    public void testMappedButNotSetAsOutlook2007ContactExport() throws Exception {
        final Reader in = new StringReader("a,b,c\n1,2\nx,y,z");

        final Iterator<CSVRecord> records = CSVFormat.newBuilder().withHeader("A", "B", "C").parse(in).iterator();

        
        assertTrue(records.hasNext());
        CSVRecord record = records.next();
        assertTrue(record.isMapped("A"));
        assertTrue(record.isMapped("B"));
        assertTrue(record.isMapped("C"));
        assertTrue(record.isSet("A"));
        assertTrue(record.isSet("B"));
        assertTrue(record.isSet("C"));
        assertEquals("a", record.get("A"));
        assertEquals("b", record.get("B"));
        assertEquals("c", record.get("C"));
        assertTrue(record.isConsistent());

        
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

// org.apache.commons.csv.CSVParserTest::testGetHeaderMap
    public void testGetHeaderMap() throws Exception {
        final CSVParser parser = new CSVParser("a,b,c\n1,2,3\nx,y,z", CSVFormat.newBuilder().withHeader("A", "B", "C").build());
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
    }

// org.apache.commons.csv.CSVParserTest::testGetLineNumberWithLF
    public void testGetLineNumberWithLF() throws Exception {
        validateLineNumbers(String.valueOf(LF));
    }

// org.apache.commons.csv.CSVParserTest::testGetLineNumberWithCRLF
    public void testGetLineNumberWithCRLF() throws Exception {
        validateLineNumbers(CRLF);
    }

// org.apache.commons.csv.CSVParserTest::testGetLineNumberWithCR
    public void testGetLineNumberWithCR() throws Exception {
        validateLineNumbers(String.valueOf(CR));
    }

// org.apache.commons.csv.CSVParserTest::testGetRecordNumberWithLF
    public void testGetRecordNumberWithLF() throws Exception {
        validateRecordNumbers(String.valueOf(LF));
    }

// org.apache.commons.csv.CSVParserTest::testGetRecordWithMultiiLineValues
    public void testGetRecordWithMultiiLineValues() throws Exception {
        final CSVParser parser = new CSVParser("\"a\r\n1\",\"a\r\n2\"" + CRLF + "\"b\r\n1\",\"b\r\n2\"" + CRLF + "\"c\r\n1\",\"c\r\n2\"",
                CSVFormat.newBuilder().withRecordSeparator(CRLF).build());
        CSVRecord record;
        assertEquals(0, parser.getRecordNumber());
        assertEquals(0, parser.getLineNumber());
        assertNotNull(record = parser.nextRecord());
        assertEquals(3, parser.getLineNumber());
        assertEquals(1, record.getRecordNumber());
        assertEquals(1, parser.getRecordNumber());
        assertNotNull(record = parser.nextRecord());
        assertEquals(6, parser.getLineNumber());
        assertEquals(2, record.getRecordNumber());
        assertEquals(2, parser.getRecordNumber());
        assertNotNull(record = parser.nextRecord());
        assertEquals(8, parser.getLineNumber());
        assertEquals(3, record.getRecordNumber());
        assertEquals(3, parser.getRecordNumber());
        assertNull(record = parser.nextRecord());
        assertEquals(8, parser.getLineNumber());
        assertEquals(3, parser.getRecordNumber());
    }

// org.apache.commons.csv.CSVParserTest::testGetRecordNumberWithCRLF
    public void testGetRecordNumberWithCRLF() throws Exception {
        validateRecordNumbers(CRLF);
    }

// org.apache.commons.csv.CSVParserTest::testGetRecordNumberWithCR
    public void testGetRecordNumberWithCR() throws Exception {
        validateRecordNumbers(String.valueOf(CR));
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
        printer.printRecords(new List[] { Arrays.asList(new String[] { "r1c1", "r1c2" }), Arrays.asList(new String[] { "r2c1", "r2c2" }) });
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
        printer.printRecords(Arrays.asList(new List[] { Arrays.asList(new String[] { "r1c1", "r1c2" }),
                Arrays.asList(new String[] { "r2c1", "r2c2" }) }));
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
        final CSVPrinter printer = new CSVPrinter(sw, CSVFormat.newBuilder().withCommentStart('#').build());
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
        final CSVPrinter printer = new CSVPrinter(sw, CSVFormat.DEFAULT.toBuilder().withNullToString("NULL").build());
        printer.printRecord("a", null, "b");
        assertEquals("a,NULL,b" + recordSeparator, sw.toString());
        printer.close();
    }

// org.apache.commons.csv.CSVPrinterTest::testQuoteAll
    public void testQuoteAll() throws IOException {
        final StringWriter sw = new StringWriter();
        final CSVPrinter printer = new CSVPrinter(sw, CSVFormat.newBuilder().withQuotePolicy(Quote.ALL).build());
        printer.printRecord("a", "b\nc", "d");
        assertEquals("\"a\",\"b\nc\",\"d\"" + recordSeparator, sw.toString());
        printer.close();
    }

// org.apache.commons.csv.CSVPrinterTest::testQuoteNonNumeric
    public void testQuoteNonNumeric() throws IOException {
        final StringWriter sw = new StringWriter();
        final CSVPrinter printer = new CSVPrinter(sw, CSVFormat.newBuilder().withQuotePolicy(Quote.NON_NUMERIC).build());
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

// org.apache.commons.csv.CSVPrinterTest::testSingleLineComment
    public void testSingleLineComment() throws IOException {
        final StringWriter sw = new StringWriter();
        final CSVPrinter printer = new CSVPrinter(sw, CSVFormat.newBuilder().withCommentStart('#').build());
        printer.printComment("This is a comment");

        assertEquals("# This is a comment" + recordSeparator, sw.toString());
        printer.close();
    }

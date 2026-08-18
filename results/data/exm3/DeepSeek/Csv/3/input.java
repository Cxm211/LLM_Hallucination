// buggy function
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

// trigger testcase
// org/apache/commons/csv/CSVLexerTest.java::testEscapedCharacter
@Test
    public void testEscapedCharacter() throws Exception {
        final Lexer lexer = getLexer("character\\aEscaped", formatWithEscaping);
        assertThat(lexer.nextToken(new Token()), hasContent("character\\aEscaped"));
    }

// org/apache/commons/csv/CSVLexerTest.java::testEscapedMySqlNullValue
@Test
    public void testEscapedMySqlNullValue() throws Exception {
        // MySQL uses \N to symbolize null values. We have to restore this
        final Lexer lexer = getLexer("character\\NEscaped", formatWithEscaping);
        assertThat(lexer.nextToken(new Token()), hasContent("character\\NEscaped"));
    }

// org/apache/commons/csv/CSVParserTest.java::testBackslashEscaping
@Test
    public void testBackslashEscaping() throws IOException {

        // To avoid confusion over the need for escaping chars in java code,
        // We will test with a forward slash as the escape char, and a single
        // quote as the encapsulator.

        final String code =
                "one,two,three\n" // 0
                        + "'',''\n"       // 1) empty encapsulators
                        + "/',/'\n"       // 2) single encapsulators
                        + "'/'','/''\n"   // 3) single encapsulators encapsulated via escape
                        + "'''',''''\n"   // 4) single encapsulators encapsulated via doubling
                        + "/,,/,\n"       // 5) separator escaped
                        + "//,//\n"       // 6) escape escaped
                        + "'//','//'\n"   // 7) escape escaped in encapsulation
                        + "   8   ,   \"quoted \"\" /\" // string\"   \n"     // don't eat spaces
                        + "9,   /\n   \n"  // escaped newline
                        + "";
        final String[][] res = {
                {"one", "two", "three"}, // 0
                {"", ""},                // 1
                {"'", "'"},              // 2
                {"'", "'"},              // 3
                {"'", "'"},              // 4
                {",", ","},              // 5
                {"/", "/"},              // 6
                {"/", "/"},              // 7
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

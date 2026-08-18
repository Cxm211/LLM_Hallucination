// buggy code
    public static CSVParser parse(final URL url, final Charset charset, final CSVFormat format) throws IOException {
        Assertions.notNull(url, "url");
        Assertions.notNull(charset, "charset");
        Assertions.notNull(format, "format");

        return new CSVParser(new InputStreamReader(url.openStream(), charset), format);
    }

    public CSVParser(final Reader reader, final CSVFormat format, final long characterOffset, final long recordNumber)
            throws IOException {
        Assertions.notNull(reader, "reader");
        Assertions.notNull(format, "format");

        this.format = format;
        this.lexer = new Lexer(format, new ExtendedBufferedReader(reader));
        this.headerMap = this.initializeHeader();
        this.characterOffset = characterOffset;
        this.recordNumber = recordNumber - 1;
    }

    public Iterator<CSVRecord> iterator() {
        return new Iterator<CSVRecord>() {
        private CSVRecord current;
  
        private CSVRecord getNextRecord() {
            try {
                return CSVParser.this.nextRecord();
            } catch (final IOException e) {
                throw new IllegalStateException(
                        e.getClass().getSimpleName() + " reading next record: " + e.toString(), e);
            }
        }
  
        @Override
        public boolean hasNext() {
            if (CSVParser.this.isClosed()) {
                return false;
            }
            if (this.current == null) {
                this.current = this.getNextRecord();
            }
  
            return this.current != null;
        }
  
        @Override
        public CSVRecord next() {
            if (CSVParser.this.isClosed()) {
                throw new NoSuchElementException("CSVParser has been closed");
            }
            CSVRecord next = this.current;
            this.current = null;
  
            if (next == null) {
                // hasNext() wasn't called before
                next = this.getNextRecord();
                if (next == null) {
                    throw new NoSuchElementException("No more CSV records available");
                }
            }
  
            return next;
        }
  
        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
    };
    }

// relevant test
// org.apache.commons.csv.CSVFileParserTest::testCSVFile
    public void testCSVFile() throws Exception {
        String line = readTestData();
        assertNotNull("file must contain config line", line);
        final String[] split = line.split(" ");
        assertTrue(testName + " require 1 param", split.length >= 1);
        
        CSVFormat format = CSVFormat.newFormat(',').withQuote('"');
        boolean checkComments = false;
        for (int i = 1; i < split.length; i++) {
            final String option = split[i];
            final String[] option_parts = option.split("=", 2);
            if ("IgnoreEmpty".equalsIgnoreCase(option_parts[0])) {
                format = format.withIgnoreEmptyLines(Boolean.parseBoolean(option_parts[1]));
            } else if ("IgnoreSpaces".equalsIgnoreCase(option_parts[0])) {
                format = format.withIgnoreSurroundingSpaces(Boolean.parseBoolean(option_parts[1]));
            } else if ("CommentStart".equalsIgnoreCase(option_parts[0])) {
                format = format.withCommentMarker(option_parts[1].charAt(0));
            } else if ("CheckComments".equalsIgnoreCase(option_parts[0])) {
                checkComments = true;
            } else {
                fail(testName + " unexpected option: " + option);
            }
        }
        line = readTestData(); 
        assertEquals(testName + " Expected format ", line, format.toString());

        
        
        try (final CSVParser parser = CSVParser.parse(new File(BASE, split[0]), Charset.defaultCharset(), format)) {
            for (final CSVRecord record : parser) {
                String parsed = Arrays.toString(record.values());
                if (checkComments) {
                    final String comment = record.getComment().replace("\n", "\\n");
                    if (comment != null) {
                        parsed += "#" + comment;
                    }
                }
                final int count = record.size();
                assertEquals(testName, readTestData(), count + ":" + parsed);
            }
        }
    }

// org.apache.commons.csv.CSVFileParserTest::testCSVUrl
    public void testCSVUrl() {}

// org.apache.commons.csv.CSVFormatTest::testDelimiterSameAsCommentStartThrowsException
    public void testDelimiterSameAsCommentStartThrowsException() {
        CSVFormat.DEFAULT.withDelimiter('!').withCommentMarker('!');
    }

// org.apache.commons.csv.CSVFormatTest::testDelimiterSameAsEscapeThrowsException
    public void testDelimiterSameAsEscapeThrowsException() {
        CSVFormat.DEFAULT.withDelimiter('!').withEscape('!');
    }

// org.apache.commons.csv.CSVFormatTest::testDuplicateHeaderElements
    public void testDuplicateHeaderElements() {
        CSVFormat.DEFAULT.withHeader("A", "A");
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
                .withQuote('"')
                .withCommentMarker('#')
                .withQuoteMode(QuoteMode.ALL);
        final CSVFormat left = right
                .withCommentMarker('!');

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
                .withQuote('"')
                .withCommentMarker('#')
                .withEscape('+')
                .withQuoteMode(QuoteMode.ALL);
        final CSVFormat left = right
                .withEscape('!');

        assertNotEquals(right, left);
    }

// org.apache.commons.csv.CSVFormatTest::testEqualsHeader
    public void testEqualsHeader() {
        final CSVFormat right = CSVFormat.newFormat('\'')
                .withRecordSeparator(CR)
                .withCommentMarker('#')
                .withEscape('+')
                .withHeader("One", "Two", "Three")
                .withIgnoreEmptyLines()
                .withIgnoreSurroundingSpaces()
                .withQuote('"')
                .withQuoteMode(QuoteMode.ALL);
        final CSVFormat left = right
                .withHeader("Three", "Two", "One");

        assertNotEquals(right, left);
    }

// org.apache.commons.csv.CSVFormatTest::testEqualsIgnoreEmptyLines
    public void testEqualsIgnoreEmptyLines() {
        final CSVFormat right = CSVFormat.newFormat('\'')
                .withCommentMarker('#')
                .withEscape('+')
                .withIgnoreEmptyLines()
                .withIgnoreSurroundingSpaces()
                .withQuote('"')
                .withQuoteMode(QuoteMode.ALL);
        final CSVFormat left = right
                .withIgnoreEmptyLines(false);

        assertNotEquals(right, left);
    }

// org.apache.commons.csv.CSVFormatTest::testEqualsIgnoreSurroundingSpaces
    public void testEqualsIgnoreSurroundingSpaces() {
        final CSVFormat right = CSVFormat.newFormat('\'')
                .withCommentMarker('#')
                .withEscape('+')
                .withIgnoreSurroundingSpaces()
                .withQuote('"')
                .withQuoteMode(QuoteMode.ALL);
        final CSVFormat left = right
                .withIgnoreSurroundingSpaces(false);

        assertNotEquals(right, left);
    }

// org.apache.commons.csv.CSVFormatTest::testEqualsLeftNoQuoteRightQuote
    public void testEqualsLeftNoQuoteRightQuote() {
    	final CSVFormat left = CSVFormat.newFormat(',').withQuote(null);
    	final CSVFormat right = left.withQuote('#');

    	assertNotEquals(left, right);
    }

// org.apache.commons.csv.CSVFormatTest::testEqualsNoQuotes
    public void testEqualsNoQuotes() {
    	final CSVFormat left = CSVFormat.newFormat(',').withQuote(null);
    	final CSVFormat right = left.withQuote(null);

    	assertEquals(left, right);
    }

// org.apache.commons.csv.CSVFormatTest::testEqualsNullString
    public void testEqualsNullString() {
        final CSVFormat right = CSVFormat.newFormat('\'')
                .withRecordSeparator(CR)
                .withCommentMarker('#')
                .withEscape('+')
                .withIgnoreEmptyLines()
                .withIgnoreSurroundingSpaces()
                .withQuote('"')
                .withQuoteMode(QuoteMode.ALL)
                .withNullString("null");
        final CSVFormat left = right
                .withNullString("---");

        assertNotEquals(right, left);
    }

// org.apache.commons.csv.CSVFormatTest::testEqualsOne
    public void testEqualsOne() {

        final CSVFormat cSVFormatOne = CSVFormat.INFORMIX_UNLOAD;
        final CSVFormat cSVFormatTwo = CSVFormat.MYSQL;

        assertEquals('\\', (char)cSVFormatOne.getEscapeCharacter());
        assertNull(cSVFormatOne.getQuoteMode());

        assertTrue(cSVFormatOne.getIgnoreEmptyLines());
        assertFalse(cSVFormatOne.getSkipHeaderRecord());

        assertFalse(cSVFormatOne.getIgnoreHeaderCase());
        assertNull(cSVFormatOne.getCommentMarker());

        assertFalse(cSVFormatOne.isCommentMarkerSet());
        assertTrue(cSVFormatOne.isQuoteCharacterSet());

        assertEquals('|', cSVFormatOne.getDelimiter());
        assertFalse(cSVFormatOne.getAllowMissingColumnNames());

        assertTrue(cSVFormatOne.isEscapeCharacterSet());
        assertEquals("\n", cSVFormatOne.getRecordSeparator());

        assertEquals('\"', (char)cSVFormatOne.getQuoteCharacter());
        assertFalse(cSVFormatOne.getTrailingDelimiter());

        assertFalse(cSVFormatOne.getTrim());
        assertFalse(cSVFormatOne.isNullStringSet());

        assertNull(cSVFormatOne.getNullString());
        assertFalse(cSVFormatOne.getIgnoreSurroundingSpaces());

        assertTrue(cSVFormatTwo.isEscapeCharacterSet());
        assertNull(cSVFormatTwo.getQuoteCharacter());

        assertFalse(cSVFormatTwo.getAllowMissingColumnNames());
        assertEquals(QuoteMode.ALL_NON_NULL, cSVFormatTwo.getQuoteMode());

        assertEquals('\t', cSVFormatTwo.getDelimiter());
        assertEquals("\n", cSVFormatTwo.getRecordSeparator());

        assertFalse(cSVFormatTwo.isQuoteCharacterSet());
        assertTrue(cSVFormatTwo.isNullStringSet());

        assertEquals('\\', (char)cSVFormatTwo.getEscapeCharacter());
        assertFalse(cSVFormatTwo.getIgnoreHeaderCase());

        assertFalse(cSVFormatTwo.getTrim());
        assertFalse(cSVFormatTwo.getIgnoreEmptyLines());

        assertEquals("\\N", cSVFormatTwo.getNullString());
        assertFalse(cSVFormatTwo.getIgnoreSurroundingSpaces());

        assertFalse(cSVFormatTwo.getTrailingDelimiter());
        assertFalse(cSVFormatTwo.getSkipHeaderRecord());

        assertNull(cSVFormatTwo.getCommentMarker());
        assertFalse(cSVFormatTwo.isCommentMarkerSet());

        assertNotSame(cSVFormatTwo, cSVFormatOne);
        assertFalse(cSVFormatTwo.equals(cSVFormatOne));

        assertEquals('\\', (char)cSVFormatOne.getEscapeCharacter());
        assertNull(cSVFormatOne.getQuoteMode());

        assertTrue(cSVFormatOne.getIgnoreEmptyLines());
        assertFalse(cSVFormatOne.getSkipHeaderRecord());

        assertFalse(cSVFormatOne.getIgnoreHeaderCase());
        assertNull(cSVFormatOne.getCommentMarker());

        assertFalse(cSVFormatOne.isCommentMarkerSet());
        assertTrue(cSVFormatOne.isQuoteCharacterSet());

        assertEquals('|', cSVFormatOne.getDelimiter());
        assertFalse(cSVFormatOne.getAllowMissingColumnNames());

        assertTrue(cSVFormatOne.isEscapeCharacterSet());
        assertEquals("\n", cSVFormatOne.getRecordSeparator());

        assertEquals('\"', (char)cSVFormatOne.getQuoteCharacter());
        assertFalse(cSVFormatOne.getTrailingDelimiter());

        assertFalse(cSVFormatOne.getTrim());
        assertFalse(cSVFormatOne.isNullStringSet());

        assertNull(cSVFormatOne.getNullString());
        assertFalse(cSVFormatOne.getIgnoreSurroundingSpaces());

        assertTrue(cSVFormatTwo.isEscapeCharacterSet());
        assertNull(cSVFormatTwo.getQuoteCharacter());

        assertFalse(cSVFormatTwo.getAllowMissingColumnNames());
        assertEquals(QuoteMode.ALL_NON_NULL, cSVFormatTwo.getQuoteMode());

        assertEquals('\t', cSVFormatTwo.getDelimiter());
        assertEquals("\n", cSVFormatTwo.getRecordSeparator());

        assertFalse(cSVFormatTwo.isQuoteCharacterSet());
        assertTrue(cSVFormatTwo.isNullStringSet());

        assertEquals('\\', (char)cSVFormatTwo.getEscapeCharacter());
        assertFalse(cSVFormatTwo.getIgnoreHeaderCase());

        assertFalse(cSVFormatTwo.getTrim());
        assertFalse(cSVFormatTwo.getIgnoreEmptyLines());

        assertEquals("\\N", cSVFormatTwo.getNullString());
        assertFalse(cSVFormatTwo.getIgnoreSurroundingSpaces());

        assertFalse(cSVFormatTwo.getTrailingDelimiter());
        assertFalse(cSVFormatTwo.getSkipHeaderRecord());

        assertNull(cSVFormatTwo.getCommentMarker());
        assertFalse(cSVFormatTwo.isCommentMarkerSet());

        assertNotSame(cSVFormatOne, cSVFormatTwo);
        assertNotSame(cSVFormatTwo, cSVFormatOne);

        assertFalse(cSVFormatOne.equals(cSVFormatTwo));
        assertFalse(cSVFormatTwo.equals(cSVFormatOne));

        assertFalse(cSVFormatTwo.equals(cSVFormatOne));

    }

// org.apache.commons.csv.CSVFormatTest::testEqualsQuoteChar
    public void testEqualsQuoteChar() {
        final CSVFormat right = CSVFormat.newFormat('\'').withQuote('"');
        final CSVFormat left = right.withQuote('!');

        assertNotEquals(right, left);
    }

// org.apache.commons.csv.CSVFormatTest::testEqualsQuotePolicy
    public void testEqualsQuotePolicy() {
        final CSVFormat right = CSVFormat.newFormat('\'')
                .withQuote('"')
                .withQuoteMode(QuoteMode.ALL);
        final CSVFormat left = right
                .withQuoteMode(QuoteMode.MINIMAL);

        assertNotEquals(right, left);
    }

// org.apache.commons.csv.CSVFormatTest::testEqualsRecordSeparator
    public void testEqualsRecordSeparator() {
        final CSVFormat right = CSVFormat.newFormat('\'')
                .withRecordSeparator(CR)
                .withCommentMarker('#')
                .withEscape('+')
                .withIgnoreEmptyLines()
                .withIgnoreSurroundingSpaces()
                .withQuote('"')
                .withQuoteMode(QuoteMode.ALL);
        final CSVFormat left = right
                .withRecordSeparator(LF);

        assertNotEquals(right, left);
    }

// org.apache.commons.csv.CSVFormatTest::testEqualsSkipHeaderRecord
    public void testEqualsSkipHeaderRecord() {
        final CSVFormat right = CSVFormat.newFormat('\'')
                .withRecordSeparator(CR)
                .withCommentMarker('#')
                .withEscape('+')
                .withIgnoreEmptyLines()
                .withIgnoreSurroundingSpaces()
                .withQuote('"')
                .withQuoteMode(QuoteMode.ALL)
                .withNullString("null")
                .withSkipHeaderRecord();
        final CSVFormat left = right
                .withSkipHeaderRecord(false);

        assertNotEquals(right, left);
    }

// org.apache.commons.csv.CSVFormatTest::testEqualsWithNull
    public void testEqualsWithNull() {

        final CSVFormat cSVFormat = CSVFormat.POSTGRESQL_TEXT;

        assertEquals('\"', (char)cSVFormat.getEscapeCharacter());
        assertFalse(cSVFormat.getIgnoreSurroundingSpaces());

        assertFalse(cSVFormat.getTrailingDelimiter());
        assertFalse(cSVFormat.getTrim());

        assertTrue(cSVFormat.isQuoteCharacterSet());
        assertEquals("\\N", cSVFormat.getNullString());

        assertFalse(cSVFormat.getIgnoreHeaderCase());
        assertTrue(cSVFormat.isEscapeCharacterSet());

        assertFalse(cSVFormat.isCommentMarkerSet());
        assertNull(cSVFormat.getCommentMarker());

        assertFalse(cSVFormat.getAllowMissingColumnNames());
        assertEquals(QuoteMode.ALL_NON_NULL, cSVFormat.getQuoteMode());

        assertEquals('\t', cSVFormat.getDelimiter());
        assertFalse(cSVFormat.getSkipHeaderRecord());

        assertEquals("\n", cSVFormat.getRecordSeparator());
        assertFalse(cSVFormat.getIgnoreEmptyLines());

        assertEquals('\"', (char)cSVFormat.getQuoteCharacter());
        assertTrue(cSVFormat.isNullStringSet());

        assertEquals('\"', (char)cSVFormat.getEscapeCharacter());
        assertFalse(cSVFormat.getIgnoreSurroundingSpaces());

        assertFalse(cSVFormat.getTrailingDelimiter());
        assertFalse(cSVFormat.getTrim());

        assertTrue(cSVFormat.isQuoteCharacterSet());
        assertEquals("\\N", cSVFormat.getNullString());

        assertFalse(cSVFormat.getIgnoreHeaderCase());
        assertTrue(cSVFormat.isEscapeCharacterSet());

        assertFalse(cSVFormat.isCommentMarkerSet());
        assertNull(cSVFormat.getCommentMarker());

        assertFalse(cSVFormat.getAllowMissingColumnNames());
        assertEquals(QuoteMode.ALL_NON_NULL, cSVFormat.getQuoteMode());

        assertEquals('\t', cSVFormat.getDelimiter());
        assertFalse(cSVFormat.getSkipHeaderRecord());

        assertEquals("\n", cSVFormat.getRecordSeparator());
        assertFalse(cSVFormat.getIgnoreEmptyLines());

        assertEquals('\"', (char)cSVFormat.getQuoteCharacter());
        assertTrue(cSVFormat.isNullStringSet());

        assertFalse(cSVFormat.equals( null));

    }

// org.apache.commons.csv.CSVFormatTest::testEscapeSameAsCommentStartThrowsException
    public void testEscapeSameAsCommentStartThrowsException() {
        CSVFormat.DEFAULT.withEscape('!').withCommentMarker('!');
    }

// org.apache.commons.csv.CSVFormatTest::testEscapeSameAsCommentStartThrowsExceptionForWrapperType
    public void testEscapeSameAsCommentStartThrowsExceptionForWrapperType() {
        
        CSVFormat.DEFAULT.withEscape(new Character('!')).withCommentMarker(new Character('!'));
    }

// org.apache.commons.csv.CSVFormatTest::testFormat
    public void testFormat() {
        final CSVFormat format = CSVFormat.DEFAULT;

        assertEquals("", format.format());
        assertEquals("a,b,c", format.format("a", "b", "c"));
        assertEquals("\"x,y\",z", format.format("x,y", "z"));
    }

// org.apache.commons.csv.CSVFormatTest::testFormatThrowsNullPointerException
    public void testFormatThrowsNullPointerException() {

        final CSVFormat cSVFormat = CSVFormat.MYSQL;

        try {
            cSVFormat.format(null);
            fail("Expecting exception: NullPointerException");
        } catch(final NullPointerException e) {
            assertEquals(CSVFormat.class.getName(), e.getStackTrace()[0].getClassName());
        }

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

// org.apache.commons.csv.CSVFormatTest::testHashCodeAndWithIgnoreHeaderCase
    public void testHashCodeAndWithIgnoreHeaderCase() {

        final CSVFormat cSVFormat = CSVFormat.INFORMIX_UNLOAD_CSV;
        final CSVFormat cSVFormatTwo = cSVFormat.withIgnoreHeaderCase();
        cSVFormatTwo.hashCode();

        assertTrue(cSVFormatTwo.getIgnoreHeaderCase());
        assertFalse(cSVFormatTwo.getTrailingDelimiter());

        assertTrue(cSVFormatTwo.equals(cSVFormat));
        assertFalse(cSVFormatTwo.getAllowMissingColumnNames());

        assertFalse(cSVFormatTwo.getTrim());

    }

// org.apache.commons.csv.CSVFormatTest::testNewFormat
    public void testNewFormat() {

        final CSVFormat cSVFormat = CSVFormat.newFormat('X');

        assertFalse(cSVFormat.getSkipHeaderRecord());
        assertFalse(cSVFormat.isEscapeCharacterSet());

        assertNull(cSVFormat.getRecordSeparator());
        assertNull(cSVFormat.getQuoteMode());

        assertNull(cSVFormat.getCommentMarker());
        assertFalse(cSVFormat.getIgnoreHeaderCase());

        assertFalse(cSVFormat.getAllowMissingColumnNames());
        assertFalse(cSVFormat.getTrim());

        assertFalse(cSVFormat.isNullStringSet());
        assertNull(cSVFormat.getEscapeCharacter());

        assertFalse(cSVFormat.getIgnoreSurroundingSpaces());
        assertFalse(cSVFormat.getTrailingDelimiter());

        assertEquals('X', cSVFormat.getDelimiter());
        assertNull(cSVFormat.getNullString());

        assertFalse(cSVFormat.isQuoteCharacterSet());
        assertFalse(cSVFormat.isCommentMarkerSet());

        assertNull(cSVFormat.getQuoteCharacter());
        assertFalse(cSVFormat.getIgnoreEmptyLines());

        assertFalse(cSVFormat.getSkipHeaderRecord());
        assertFalse(cSVFormat.isEscapeCharacterSet());

        assertNull(cSVFormat.getRecordSeparator());
        assertNull(cSVFormat.getQuoteMode());

        assertNull(cSVFormat.getCommentMarker());
        assertFalse(cSVFormat.getIgnoreHeaderCase());

        assertFalse(cSVFormat.getAllowMissingColumnNames());
        assertFalse(cSVFormat.getTrim());

        assertFalse(cSVFormat.isNullStringSet());
        assertNull(cSVFormat.getEscapeCharacter());

        assertFalse(cSVFormat.getIgnoreSurroundingSpaces());
        assertFalse(cSVFormat.getTrailingDelimiter());

        assertEquals('X', cSVFormat.getDelimiter());
        assertNull(cSVFormat.getNullString());

        assertFalse(cSVFormat.isQuoteCharacterSet());
        assertFalse(cSVFormat.isCommentMarkerSet());

        assertNull(cSVFormat.getQuoteCharacter());
        assertFalse(cSVFormat.getIgnoreEmptyLines());

    }

// org.apache.commons.csv.CSVFormatTest::testNullRecordSeparatorCsv106
    public void testNullRecordSeparatorCsv106() {
        final CSVFormat format = CSVFormat.newFormat(';').withSkipHeaderRecord().withHeader("H1", "H2");
        final String formatStr = format.format("A", "B");
        assertNotNull(formatStr);
        assertFalse(formatStr.endsWith("null"));
    }

// org.apache.commons.csv.CSVFormatTest::testQuoteCharSameAsCommentStartThrowsException
    public void testQuoteCharSameAsCommentStartThrowsException() {
        CSVFormat.DEFAULT.withQuote('!').withCommentMarker('!');
    }

// org.apache.commons.csv.CSVFormatTest::testQuoteCharSameAsCommentStartThrowsExceptionForWrapperType
    public void testQuoteCharSameAsCommentStartThrowsExceptionForWrapperType() {
        
        CSVFormat.DEFAULT.withQuote(new Character('!')).withCommentMarker('!');
    }

// org.apache.commons.csv.CSVFormatTest::testQuoteCharSameAsDelimiterThrowsException
    public void testQuoteCharSameAsDelimiterThrowsException() {
        CSVFormat.DEFAULT.withQuote('!').withDelimiter('!');
    }

// org.apache.commons.csv.CSVFormatTest::testQuotePolicyNoneWithoutEscapeThrowsException
    public void testQuotePolicyNoneWithoutEscapeThrowsException() {
        CSVFormat.newFormat('!').withQuoteMode(QuoteMode.NONE);
    }

// org.apache.commons.csv.CSVFormatTest::testRFC4180
    public void testRFC4180() {
        assertEquals(null, RFC4180.getCommentMarker());
        assertEquals(',', RFC4180.getDelimiter());
        assertEquals(null, RFC4180.getEscapeCharacter());
        assertFalse(RFC4180.getIgnoreEmptyLines());
        assertEquals(Character.valueOf('"'), RFC4180.getQuoteCharacter());
        assertEquals(null, RFC4180.getQuoteMode());
        assertEquals("\r\n", RFC4180.getRecordSeparator());
    }

// org.apache.commons.csv.CSVFormatTest::testSerialization
    public void testSerialization() throws Exception {
        final ByteArrayOutputStream out = new ByteArrayOutputStream();

        try (final ObjectOutputStream oos = new ObjectOutputStream(out)) {
            oos.writeObject(CSVFormat.DEFAULT);
            oos.flush();
        }

        final ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(out.toByteArray()));
        final CSVFormat format = (CSVFormat) in.readObject();

        assertNotNull(format);
        assertEquals("delimiter", CSVFormat.DEFAULT.getDelimiter(), format.getDelimiter());
        assertEquals("encapsulator", CSVFormat.DEFAULT.getQuoteCharacter(), format.getQuoteCharacter());
        assertEquals("comment start", CSVFormat.DEFAULT.getCommentMarker(), format.getCommentMarker());
        assertEquals("record separator", CSVFormat.DEFAULT.getRecordSeparator(), format.getRecordSeparator());
        assertEquals("escape", CSVFormat.DEFAULT.getEscapeCharacter(), format.getEscapeCharacter());
        assertEquals("trim", CSVFormat.DEFAULT.getIgnoreSurroundingSpaces(), format.getIgnoreSurroundingSpaces());
        assertEquals("empty lines", CSVFormat.DEFAULT.getIgnoreEmptyLines(), format.getIgnoreEmptyLines());
    }

// org.apache.commons.csv.CSVFormatTest::testToString
    public void testToString() {

        final CSVFormat cSVFormat = CSVFormat.POSTGRESQL_TEXT;
        final String string = CSVFormat.INFORMIX_UNLOAD.toString();

        assertEquals("Delimiter=<|> Escape=<\\> QuoteChar=<\"> RecordSeparator=<\n> EmptyLines:ignored SkipHeaderRecord:false", string);

    }

// org.apache.commons.csv.CSVFormatTest::testToStringAndWithCommentMarkerTakingCharacter
    public void testToStringAndWithCommentMarkerTakingCharacter() {

        final CSVFormat.Predefined cSVFormat_Predefined = CSVFormat.Predefined.Default;
        final CSVFormat cSVFormat = cSVFormat_Predefined.getFormat();

        assertNull(cSVFormat.getEscapeCharacter());
        assertTrue(cSVFormat.isQuoteCharacterSet());

        assertFalse(cSVFormat.getTrim());
        assertFalse(cSVFormat.getIgnoreSurroundingSpaces());

        assertFalse(cSVFormat.getTrailingDelimiter());
        assertEquals(',', cSVFormat.getDelimiter());

        assertFalse(cSVFormat.getIgnoreHeaderCase());
        assertEquals("\r\n", cSVFormat.getRecordSeparator());

        assertFalse(cSVFormat.isCommentMarkerSet());
        assertNull(cSVFormat.getCommentMarker());

        assertFalse(cSVFormat.isNullStringSet());
        assertFalse(cSVFormat.getAllowMissingColumnNames());

        assertFalse(cSVFormat.isEscapeCharacterSet());
        assertFalse(cSVFormat.getSkipHeaderRecord());

        assertNull(cSVFormat.getNullString());
        assertNull(cSVFormat.getQuoteMode());

        assertTrue(cSVFormat.getIgnoreEmptyLines());
        assertEquals('\"', (char)cSVFormat.getQuoteCharacter());

        final Character character = Character.valueOf('n');

        final CSVFormat cSVFormatTwo = cSVFormat.withCommentMarker(character);

        assertNull(cSVFormat.getEscapeCharacter());
        assertTrue(cSVFormat.isQuoteCharacterSet());

        assertFalse(cSVFormat.getTrim());
        assertFalse(cSVFormat.getIgnoreSurroundingSpaces());

        assertFalse(cSVFormat.getTrailingDelimiter());
        assertEquals(',', cSVFormat.getDelimiter());

        assertFalse(cSVFormat.getIgnoreHeaderCase());
        assertEquals("\r\n", cSVFormat.getRecordSeparator());

        assertFalse(cSVFormat.isCommentMarkerSet());
        assertNull(cSVFormat.getCommentMarker());

        assertFalse(cSVFormat.isNullStringSet());
        assertFalse(cSVFormat.getAllowMissingColumnNames());

        assertFalse(cSVFormat.isEscapeCharacterSet());
        assertFalse(cSVFormat.getSkipHeaderRecord());

        assertNull(cSVFormat.getNullString());
        assertNull(cSVFormat.getQuoteMode());

        assertTrue(cSVFormat.getIgnoreEmptyLines());
        assertEquals('\"', (char)cSVFormat.getQuoteCharacter());

        assertFalse(cSVFormatTwo.isNullStringSet());
        assertFalse(cSVFormatTwo.getAllowMissingColumnNames());

        assertEquals('\"', (char)cSVFormatTwo.getQuoteCharacter());
        assertNull(cSVFormatTwo.getNullString());

        assertEquals(',', cSVFormatTwo.getDelimiter());
        assertFalse(cSVFormatTwo.getTrailingDelimiter());

        assertTrue(cSVFormatTwo.isCommentMarkerSet());
        assertFalse(cSVFormatTwo.getIgnoreHeaderCase());

        assertFalse(cSVFormatTwo.getTrim());
        assertNull(cSVFormatTwo.getEscapeCharacter());

        assertTrue(cSVFormatTwo.isQuoteCharacterSet());
        assertFalse(cSVFormatTwo.getIgnoreSurroundingSpaces());

        assertEquals("\r\n", cSVFormatTwo.getRecordSeparator());
        assertNull(cSVFormatTwo.getQuoteMode());

        assertEquals('n', (char)cSVFormatTwo.getCommentMarker());
        assertFalse(cSVFormatTwo.getSkipHeaderRecord());

        assertFalse(cSVFormatTwo.isEscapeCharacterSet());
        assertTrue(cSVFormatTwo.getIgnoreEmptyLines());

        assertNotSame(cSVFormat, cSVFormatTwo);
        assertNotSame(cSVFormatTwo, cSVFormat);

        assertFalse(cSVFormatTwo.equals(cSVFormat));

        assertNull(cSVFormat.getEscapeCharacter());
        assertTrue(cSVFormat.isQuoteCharacterSet());

        assertFalse(cSVFormat.getTrim());
        assertFalse(cSVFormat.getIgnoreSurroundingSpaces());

        assertFalse(cSVFormat.getTrailingDelimiter());
        assertEquals(',', cSVFormat.getDelimiter());

        assertFalse(cSVFormat.getIgnoreHeaderCase());
        assertEquals("\r\n", cSVFormat.getRecordSeparator());

        assertFalse(cSVFormat.isCommentMarkerSet());
        assertNull(cSVFormat.getCommentMarker());

        assertFalse(cSVFormat.isNullStringSet());
        assertFalse(cSVFormat.getAllowMissingColumnNames());

        assertFalse(cSVFormat.isEscapeCharacterSet());
        assertFalse(cSVFormat.getSkipHeaderRecord());

        assertNull(cSVFormat.getNullString());
        assertNull(cSVFormat.getQuoteMode());

        assertTrue(cSVFormat.getIgnoreEmptyLines());
        assertEquals('\"', (char)cSVFormat.getQuoteCharacter());

        assertFalse(cSVFormatTwo.isNullStringSet());
        assertFalse(cSVFormatTwo.getAllowMissingColumnNames());

        assertEquals('\"', (char)cSVFormatTwo.getQuoteCharacter());
        assertNull(cSVFormatTwo.getNullString());

        assertEquals(',', cSVFormatTwo.getDelimiter());
        assertFalse(cSVFormatTwo.getTrailingDelimiter());

        assertTrue(cSVFormatTwo.isCommentMarkerSet());
        assertFalse(cSVFormatTwo.getIgnoreHeaderCase());

        assertFalse(cSVFormatTwo.getTrim());
        assertNull(cSVFormatTwo.getEscapeCharacter());

        assertTrue(cSVFormatTwo.isQuoteCharacterSet());
        assertFalse(cSVFormatTwo.getIgnoreSurroundingSpaces());

        assertEquals("\r\n", cSVFormatTwo.getRecordSeparator());
        assertNull(cSVFormatTwo.getQuoteMode());

        assertEquals('n', (char)cSVFormatTwo.getCommentMarker());
        assertFalse(cSVFormatTwo.getSkipHeaderRecord());

        assertFalse(cSVFormatTwo.isEscapeCharacterSet());
        assertTrue(cSVFormatTwo.getIgnoreEmptyLines());

        assertNotSame(cSVFormat, cSVFormatTwo);
        assertNotSame(cSVFormatTwo, cSVFormat);

        assertFalse(cSVFormat.equals(cSVFormatTwo));

        assertFalse(cSVFormatTwo.equals(cSVFormat));
        assertEquals("Delimiter=<,> QuoteChar=<\"> CommentStart=<n> " +
                        "RecordSeparator=<\r\n> EmptyLines:ignored SkipHeaderRecord:false"
                , cSVFormatTwo.toString());

    }

// org.apache.commons.csv.CSVFormatTest::testWithCommentStart
    public void testWithCommentStart() throws Exception {
        final CSVFormat formatWithCommentStart = CSVFormat.DEFAULT.withCommentMarker('#');
        assertEquals( Character.valueOf('#'), formatWithCommentStart.getCommentMarker());
    }

// org.apache.commons.csv.CSVFormatTest::testWithCommentStartCRThrowsException
    public void testWithCommentStartCRThrowsException() {
        CSVFormat.DEFAULT.withCommentMarker(CR);
    }

// org.apache.commons.csv.CSVFormatTest::testWithDelimiter
    public void testWithDelimiter() throws Exception {
        final CSVFormat formatWithDelimiter = CSVFormat.DEFAULT.withDelimiter('!');
        assertEquals('!', formatWithDelimiter.getDelimiter());
    }

// org.apache.commons.csv.CSVFormatTest::testWithDelimiterLFThrowsException
    public void testWithDelimiterLFThrowsException() {
        CSVFormat.DEFAULT.withDelimiter(LF);
    }

// org.apache.commons.csv.CSVFormatTest::testWithEmptyEnum
    public void testWithEmptyEnum() throws Exception {
        final CSVFormat formatWithHeader = CSVFormat.DEFAULT.withHeader(EmptyEnum.class);
        Assert.assertTrue(formatWithHeader.getHeader().length == 0);
    }

// org.apache.commons.csv.CSVFormatTest::testWithEscape
    public void testWithEscape() throws Exception {
        final CSVFormat formatWithEscape = CSVFormat.DEFAULT.withEscape('&');
        assertEquals(Character.valueOf('&'), formatWithEscape.getEscapeCharacter());
    }

// org.apache.commons.csv.CSVFormatTest::testWithEscapeCRThrowsExceptions
    public void testWithEscapeCRThrowsExceptions() {
        CSVFormat.DEFAULT.withEscape(CR);
    }

// org.apache.commons.csv.CSVFormatTest::testWithFirstRecordAsHeader
    public void testWithFirstRecordAsHeader() throws Exception {
        final CSVFormat formatWithFirstRecordAsHeader = CSVFormat.DEFAULT.withFirstRecordAsHeader();
        assertTrue(formatWithFirstRecordAsHeader.getSkipHeaderRecord());
        assertTrue(formatWithFirstRecordAsHeader.getHeader().length == 0);
    }

// org.apache.commons.csv.CSVFormatTest::testWithHeader
    public void testWithHeader() throws Exception {
        final String[] header = new String[]{"one", "two", "three"};
        
        final CSVFormat formatWithHeader = CSVFormat.DEFAULT.withHeader(header);
        assertArrayEquals(header, formatWithHeader.getHeader());
        assertNotSame(header, formatWithHeader.getHeader());
    }

// org.apache.commons.csv.CSVFormatTest::testWithHeaderComments
    public void testWithHeaderComments() {

        final CSVFormat cSVFormat = CSVFormat.DEFAULT;

        assertEquals('\"', (char)cSVFormat.getQuoteCharacter());
        assertFalse(cSVFormat.isCommentMarkerSet());

        assertFalse(cSVFormat.isEscapeCharacterSet());
        assertTrue(cSVFormat.isQuoteCharacterSet());

        assertFalse(cSVFormat.getSkipHeaderRecord());
        assertNull(cSVFormat.getQuoteMode());

        assertEquals(',', cSVFormat.getDelimiter());
        assertTrue(cSVFormat.getIgnoreEmptyLines());

        assertFalse(cSVFormat.getIgnoreHeaderCase());
        assertNull(cSVFormat.getCommentMarker());

        assertEquals("\r\n", cSVFormat.getRecordSeparator());
        assertFalse(cSVFormat.getTrailingDelimiter());

        assertFalse(cSVFormat.getAllowMissingColumnNames());
        assertFalse(cSVFormat.getTrim());

        assertFalse(cSVFormat.isNullStringSet());
        assertNull(cSVFormat.getNullString());

        assertFalse(cSVFormat.getIgnoreSurroundingSpaces());
        assertNull(cSVFormat.getEscapeCharacter());

        final Object[] objectArray = new Object[8];
        final CSVFormat cSVFormatTwo = cSVFormat.withHeaderComments(objectArray);

        assertEquals('\"', (char)cSVFormat.getQuoteCharacter());
        assertFalse(cSVFormat.isCommentMarkerSet());

        assertFalse(cSVFormat.isEscapeCharacterSet());
        assertTrue(cSVFormat.isQuoteCharacterSet());

        assertFalse(cSVFormat.getSkipHeaderRecord());
        assertNull(cSVFormat.getQuoteMode());

        assertEquals(',', cSVFormat.getDelimiter());
        assertTrue(cSVFormat.getIgnoreEmptyLines());

        assertFalse(cSVFormat.getIgnoreHeaderCase());
        assertNull(cSVFormat.getCommentMarker());

        assertEquals("\r\n", cSVFormat.getRecordSeparator());
        assertFalse(cSVFormat.getTrailingDelimiter());

        assertFalse(cSVFormat.getAllowMissingColumnNames());
        assertFalse(cSVFormat.getTrim());

        assertFalse(cSVFormat.isNullStringSet());
        assertNull(cSVFormat.getNullString());

        assertFalse(cSVFormat.getIgnoreSurroundingSpaces());
        assertNull(cSVFormat.getEscapeCharacter());

        assertFalse(cSVFormatTwo.getIgnoreHeaderCase());
        assertNull(cSVFormatTwo.getQuoteMode());

        assertTrue(cSVFormatTwo.getIgnoreEmptyLines());
        assertFalse(cSVFormatTwo.getIgnoreSurroundingSpaces());

        assertNull(cSVFormatTwo.getEscapeCharacter());
        assertFalse(cSVFormatTwo.getTrim());

        assertFalse(cSVFormatTwo.isEscapeCharacterSet());
        assertTrue(cSVFormatTwo.isQuoteCharacterSet());

        assertFalse(cSVFormatTwo.getSkipHeaderRecord());
        assertEquals('\"', (char)cSVFormatTwo.getQuoteCharacter());

        assertFalse(cSVFormatTwo.getAllowMissingColumnNames());
        assertNull(cSVFormatTwo.getNullString());

        assertFalse(cSVFormatTwo.isNullStringSet());
        assertFalse(cSVFormatTwo.getTrailingDelimiter());

        assertEquals("\r\n", cSVFormatTwo.getRecordSeparator());
        assertEquals(',', cSVFormatTwo.getDelimiter());

        assertNull(cSVFormatTwo.getCommentMarker());
        assertFalse(cSVFormatTwo.isCommentMarkerSet());

        assertNotSame(cSVFormat, cSVFormatTwo);
        assertNotSame(cSVFormatTwo, cSVFormat);

        assertTrue(cSVFormatTwo.equals(cSVFormat));

        final String string = cSVFormatTwo.format(objectArray);

        assertEquals('\"', (char)cSVFormat.getQuoteCharacter());
        assertFalse(cSVFormat.isCommentMarkerSet());

        assertFalse(cSVFormat.isEscapeCharacterSet());
        assertTrue(cSVFormat.isQuoteCharacterSet());

        assertFalse(cSVFormat.getSkipHeaderRecord());
        assertNull(cSVFormat.getQuoteMode());

        assertEquals(',', cSVFormat.getDelimiter());
        assertTrue(cSVFormat.getIgnoreEmptyLines());

        assertFalse(cSVFormat.getIgnoreHeaderCase());
        assertNull(cSVFormat.getCommentMarker());

        assertEquals("\r\n", cSVFormat.getRecordSeparator());
        assertFalse(cSVFormat.getTrailingDelimiter());

        assertFalse(cSVFormat.getAllowMissingColumnNames());
        assertFalse(cSVFormat.getTrim());

        assertFalse(cSVFormat.isNullStringSet());
        assertNull(cSVFormat.getNullString());

        assertFalse(cSVFormat.getIgnoreSurroundingSpaces());
        assertNull(cSVFormat.getEscapeCharacter());

        assertFalse(cSVFormatTwo.getIgnoreHeaderCase());
        assertNull(cSVFormatTwo.getQuoteMode());

        assertTrue(cSVFormatTwo.getIgnoreEmptyLines());
        assertFalse(cSVFormatTwo.getIgnoreSurroundingSpaces());

        assertNull(cSVFormatTwo.getEscapeCharacter());
        assertFalse(cSVFormatTwo.getTrim());

        assertFalse(cSVFormatTwo.isEscapeCharacterSet());
        assertTrue(cSVFormatTwo.isQuoteCharacterSet());

        assertFalse(cSVFormatTwo.getSkipHeaderRecord());
        assertEquals('\"', (char)cSVFormatTwo.getQuoteCharacter());

        assertFalse(cSVFormatTwo.getAllowMissingColumnNames());
        assertNull(cSVFormatTwo.getNullString());

        assertFalse(cSVFormatTwo.isNullStringSet());
        assertFalse(cSVFormatTwo.getTrailingDelimiter());

        assertEquals("\r\n", cSVFormatTwo.getRecordSeparator());
        assertEquals(',', cSVFormatTwo.getDelimiter());

        assertNull(cSVFormatTwo.getCommentMarker());
        assertFalse(cSVFormatTwo.isCommentMarkerSet());

        assertNotSame(cSVFormat, cSVFormatTwo);
        assertNotSame(cSVFormatTwo, cSVFormat);

        assertNotNull(string);
        assertTrue(cSVFormat.equals(cSVFormatTwo));

        assertTrue(cSVFormatTwo.equals(cSVFormat));
        assertEquals(",,,,,,,", string);

    }

// org.apache.commons.csv.CSVFormatTest::testWithHeaderEnum
    public void testWithHeaderEnum() throws Exception {
        final CSVFormat formatWithHeader = CSVFormat.DEFAULT.withHeader(Header.class);
        assertArrayEquals(new String[]{ "Name", "Email", "Phone" }, formatWithHeader.getHeader());
    }

// org.apache.commons.csv.CSVFormatTest::testWithIgnoreEmptyLines
    public void testWithIgnoreEmptyLines() throws Exception {
        assertFalse(CSVFormat.DEFAULT.withIgnoreEmptyLines(false).getIgnoreEmptyLines());
        assertTrue(CSVFormat.DEFAULT.withIgnoreEmptyLines().getIgnoreEmptyLines());
    }

// org.apache.commons.csv.CSVFormatTest::testWithIgnoreSurround
    public void testWithIgnoreSurround() throws Exception {
        assertFalse(CSVFormat.DEFAULT.withIgnoreSurroundingSpaces(false).getIgnoreSurroundingSpaces());
        assertTrue(CSVFormat.DEFAULT.withIgnoreSurroundingSpaces().getIgnoreSurroundingSpaces());
    }

// org.apache.commons.csv.CSVFormatTest::testWithNullString
    public void testWithNullString() throws Exception {
        final CSVFormat formatWithNullString = CSVFormat.DEFAULT.withNullString("null");
        assertEquals("null", formatWithNullString.getNullString());
    }

// org.apache.commons.csv.CSVFormatTest::testWithQuoteChar
    public void testWithQuoteChar() throws Exception {
        final CSVFormat formatWithQuoteChar = CSVFormat.DEFAULT.withQuote('"');
        assertEquals(Character.valueOf('"'), formatWithQuoteChar.getQuoteCharacter());
    }

// org.apache.commons.csv.CSVFormatTest::testWithQuoteLFThrowsException
    public void testWithQuoteLFThrowsException() {
        CSVFormat.DEFAULT.withQuote(LF);
    }

// org.apache.commons.csv.CSVFormatTest::testWithQuotePolicy
    public void testWithQuotePolicy() throws Exception {
        final CSVFormat formatWithQuotePolicy = CSVFormat.DEFAULT.withQuoteMode(QuoteMode.ALL);
        assertEquals(QuoteMode.ALL, formatWithQuotePolicy.getQuoteMode());
    }

// org.apache.commons.csv.CSVFormatTest::testWithRecordSeparatorCR
    public void testWithRecordSeparatorCR() throws Exception {
        final CSVFormat formatWithRecordSeparator = CSVFormat.DEFAULT.withRecordSeparator(CR);
        assertEquals(String.valueOf(CR), formatWithRecordSeparator.getRecordSeparator());
    }

// org.apache.commons.csv.CSVFormatTest::testWithRecordSeparatorCRLF
    public void testWithRecordSeparatorCRLF() throws Exception {
        final CSVFormat formatWithRecordSeparator = CSVFormat.DEFAULT.withRecordSeparator(CRLF);
        assertEquals(CRLF, formatWithRecordSeparator.getRecordSeparator());
    }

// org.apache.commons.csv.CSVFormatTest::testWithRecordSeparatorLF
    public void testWithRecordSeparatorLF() throws Exception {
        final CSVFormat formatWithRecordSeparator = CSVFormat.DEFAULT.withRecordSeparator(LF);
        assertEquals(String.valueOf(LF), formatWithRecordSeparator.getRecordSeparator());
    }

// org.apache.commons.csv.CSVFormatTest::testWithSystemRecordSeparator
    public void testWithSystemRecordSeparator() throws Exception {
        final CSVFormat formatWithRecordSeparator = CSVFormat.DEFAULT.withSystemRecordSeparator();
        assertEquals(System.getProperty("line.separator"), formatWithRecordSeparator.getRecordSeparator());
    }

// org.apache.commons.csv.CSVParserTest::testBackslashEscaping
    public void testBackslashEscaping() throws IOException {

        
        
        

        final String code = "one,two,three\n" 
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
        final String[][] res = { { "one", "two", "three" }, 
                { "", "" }, 
                { "'", "'" }, 
                { "'", "'" }, 
                { "'", "'" }, 
                { ",", "," }, 
                { "/", "/" }, 
                { "/", "/" }, 
                { "   8   ", "   \"quoted \"\" /\" / string\"   " }, { "9", "   \n   " }, };

        final CSVFormat format = CSVFormat.newFormat(',').withQuote('\'').withRecordSeparator(CRLF).withEscape('/')
                .withIgnoreEmptyLines();

        try (final CSVParser parser = CSVParser.parse(code, format)) {
            final List<CSVRecord> records = parser.getRecords();
            assertTrue(records.size() > 0);

            Utils.compare("Records do not match expected result", res, records);
        }
    }

// org.apache.commons.csv.CSVParserTest::testBackslashEscaping2
    public void testBackslashEscaping2() throws IOException {

        
        
        

        final String code = "" + " , , \n" 
                + " \t ,  , \n" 
                + " 
                + "";
        final String[][] res = { { " ", " ", " " }, 
                { " \t ", "  ", " " }, 
                { " / ", " , ", " ," }, 
        };

        final CSVFormat format = CSVFormat.newFormat(',').withRecordSeparator(CRLF).withEscape('/')
                .withIgnoreEmptyLines();

        try (final CSVParser parser = CSVParser.parse(code, format)) {
            final List<CSVRecord> records = parser.getRecords();
            assertTrue(records.size() > 0);

            Utils.compare("", res, records);
        }
    }

// org.apache.commons.csv.CSVParserTest::testBackslashEscapingOld
    public void testBackslashEscapingOld() throws IOException {
        final String code = "one,two,three\n" + "on\\\"e,two\n" + "on\"e,two\n" + "one,\"tw\\\"o\"\n" +
                "one,\"t\\,wo\"\n" + "one,two,\"th,ree\"\n" + "\"a\\\\\"\n" + "a\\,b\n" + "\"a\\\\,b\"";
        final String[][] res = { { "one", "two", "three" }, { "on\\\"e", "two" }, { "on\"e", "two" },
                { "one", "tw\"o" }, { "one", "t\\,wo" }, 
                { "one", "two", "th,ree" }, { "a\\\\" }, 
                { "a\\", "b" }, 
                { "a\\\\,b" } 
        };
        try (final CSVParser parser = CSVParser.parse(code, CSVFormat.DEFAULT)) {
            final List<CSVRecord> records = parser.getRecords();
            assertEquals(res.length, records.size());
            assertTrue(records.size() > 0);
            for (int i = 0; i < res.length; i++) {
                assertArrayEquals(res[i], records.get(i).values());
            }
        }
    }

// org.apache.commons.csv.CSVParserTest::testBOM
    public void testBOM() throws IOException {
        final URL url = ClassLoader.getSystemClassLoader().getResource("CSVFileParser/bom.csv");
        try (final CSVParser parser = CSVParser.parse(url, Charset.forName(UTF_8_NAME), CSVFormat.EXCEL.withHeader())) {
            for (final CSVRecord record : parser) {
                final String string = record.get("Date");
                Assert.assertNotNull(string);
                
            }
        }
    }

// org.apache.commons.csv.CSVParserTest::testBOMInputStream_ParserWithReader
    public void testBOMInputStream_ParserWithReader() {}

// org.apache.commons.csv.CSVParserTest::testBOMInputStream_parseWithReader
    public void testBOMInputStream_parseWithReader() {}

// org.apache.commons.csv.CSVParserTest::testBOMInputStream_ParserWithInputStream
    public void testBOMInputStream_ParserWithInputStream() {}

// org.apache.commons.csv.CSVParserTest::testCarriageReturnEndings
    public void testCarriageReturnEndings() throws IOException {
        final String code = "foo\rbaar,\rhello,world\r,kanu";
        try (final CSVParser parser = CSVParser.parse(code, CSVFormat.DEFAULT)) {
            final List<CSVRecord> records = parser.getRecords();
            assertEquals(4, records.size());
        }
    }

// org.apache.commons.csv.CSVParserTest::testCarriageReturnLineFeedEndings
    public void testCarriageReturnLineFeedEndings() throws IOException {
        final String code = "foo\r\nbaar,\r\nhello,world\r\n,kanu";
        try (final CSVParser parser = CSVParser.parse(code, CSVFormat.DEFAULT)) {
            final List<CSVRecord> records = parser.getRecords();
            assertEquals(4, records.size());
        }
    }

// org.apache.commons.csv.CSVParserTest::testFirstEndOfLineCrLf
    public void testFirstEndOfLineCrLf() throws IOException {
        final String data = "foo\r\nbaar,\r\nhello,world\r\n,kanu";
        try (final CSVParser parser = CSVParser.parse(data, CSVFormat.DEFAULT)) {
            final List<CSVRecord> records = parser.getRecords();
            assertEquals(4, records.size());
            assertEquals("\r\n", parser.getFirstEndOfLine());
        }
    }

// org.apache.commons.csv.CSVParserTest::testFirstEndOfLineLf
    public void testFirstEndOfLineLf() throws IOException {
        final String data = "foo\nbaar,\nhello,world\n,kanu";
        try (final CSVParser parser = CSVParser.parse(data, CSVFormat.DEFAULT)) {
            final List<CSVRecord> records = parser.getRecords();
            assertEquals(4, records.size());
            assertEquals("\n", parser.getFirstEndOfLine());
        }
    }

// org.apache.commons.csv.CSVParserTest::testFirstEndOfLineCr
    public void testFirstEndOfLineCr() throws IOException {
        final String data = "foo\rbaar,\rhello,world\r,kanu";
        try (final CSVParser parser = CSVParser.parse(data, CSVFormat.DEFAULT)) {
            final List<CSVRecord> records = parser.getRecords();
            assertEquals(4, records.size());
            assertEquals("\r", parser.getFirstEndOfLine());
        }
    }

// org.apache.commons.csv.CSVParserTest::testClose
    public void testClose() throws Exception {
        final Reader in = new StringReader("# comment\na,b,c\n1,2,3\nx,y,z");
        final Iterator<CSVRecord> records;
        try (final CSVParser parser = CSVFormat.DEFAULT.withCommentMarker('#').withHeader().parse(in)) {
            records = parser.iterator();
            assertTrue(records.hasNext());
        }
        assertFalse(records.hasNext());
        records.next();
    }

// org.apache.commons.csv.CSVParserTest::testCSV57
    public void testCSV57() throws Exception {
        try (final CSVParser parser = CSVParser.parse("", CSVFormat.DEFAULT)) {
            final List<CSVRecord> list = parser.getRecords();
            assertNotNull(list);
            assertEquals(0, list.size());
        }
    }

// org.apache.commons.csv.CSVParserTest::testDefaultFormat
    public void testDefaultFormat() throws IOException {
        final String code = "" + "a,b#\n" 
                + "\"\n\",\" \",#\n" 
                + "#,\"\"\n" 
                + "# Final comment\n"// 4)
                ;
        final String[][] res = { { "a", "b#" }, { "\n", " ", "#" }, { "#", "" }, { "# Final comment" } };

        CSVFormat format = CSVFormat.DEFAULT;
        assertFalse(format.isCommentMarkerSet());
        final String[][] res_comments = { { "a", "b#" }, { "\n", " ", "#" }, };

        try (final CSVParser parser = CSVParser.parse(code, format)) {
            final List<CSVRecord> records = parser.getRecords();
            assertTrue(records.size() > 0);

            Utils.compare("Failed to parse without comments", res, records);

            format = CSVFormat.DEFAULT.withCommentMarker('#');
        }
        try (final CSVParser parser = CSVParser.parse(code, format)) {
            final List<CSVRecord> records = parser.getRecords();

            Utils.compare("Failed to parse with comments", res_comments, records);
        }
    }

// org.apache.commons.csv.CSVParserTest::testDuplicateHeaders
    public void testDuplicateHeaders() throws Exception {
        CSVParser.parse("a,b,a\n1,2,3\nx,y,z", CSVFormat.DEFAULT.withHeader(new String[] {}));
    }

// org.apache.commons.csv.CSVParserTest::testEmptyFile
    public void testEmptyFile() throws Exception {
        try (final CSVParser parser = CSVParser.parse("", CSVFormat.DEFAULT)) {
            assertNull(parser.nextRecord());
        }
    }

// org.apache.commons.csv.CSVParserTest::testEmptyLineBehaviourCSV
    public void testEmptyLineBehaviourCSV() throws Exception {
        final String[] codes = { "hello,\r\n\r\n\r\n", "hello,\n\n\n", "hello,\"\"\r\n\r\n\r\n", "hello,\"\"\n\n\n" };
        final String[][] res = { { "hello", "" } 
        };
        for (final String code : codes) {
            try (final CSVParser parser = CSVParser.parse(code, CSVFormat.DEFAULT)) {
                final List<CSVRecord> records = parser.getRecords();
                assertEquals(res.length, records.size());
                assertTrue(records.size() > 0);
                for (int i = 0; i < res.length; i++) {
                    assertArrayEquals(res[i], records.get(i).values());
                }
            }
        }
    }

// org.apache.commons.csv.CSVParserTest::testEmptyLineBehaviourExcel
    public void testEmptyLineBehaviourExcel() throws Exception {
        final String[] codes = { "hello,\r\n\r\n\r\n", "hello,\n\n\n", "hello,\"\"\r\n\r\n\r\n", "hello,\"\"\n\n\n" };
        final String[][] res = { { "hello", "" }, { "" }, 
                { "" } };
        for (final String code : codes) {
            try (final CSVParser parser = CSVParser.parse(code, CSVFormat.EXCEL)) {
                final List<CSVRecord> records = parser.getRecords();
                assertEquals(res.length, records.size());
                assertTrue(records.size() > 0);
                for (int i = 0; i < res.length; i++) {
                    assertArrayEquals(res[i], records.get(i).values());
                }
            }
        }
    }

// org.apache.commons.csv.CSVParserTest::testEndOfFileBehaviorCSV
    public void testEndOfFileBehaviorCSV() throws Exception {
        final String[] codes = { "hello,\r\n\r\nworld,\r\n", "hello,\r\n\r\nworld,", "hello,\r\n\r\nworld,\"\"\r\n",
                "hello,\r\n\r\nworld,\"\"", "hello,\r\n\r\nworld,\n", "hello,\r\n\r\nworld,",
                "hello,\r\n\r\nworld,\"\"\n", "hello,\r\n\r\nworld,\"\"" };
        final String[][] res = { { "hello", "" }, 
                { "world", "" } };
        for (final String code : codes) {
            try (final CSVParser parser = CSVParser.parse(code, CSVFormat.DEFAULT)) {
                final List<CSVRecord> records = parser.getRecords();
                assertEquals(res.length, records.size());
                assertTrue(records.size() > 0);
                for (int i = 0; i < res.length; i++) {
                    assertArrayEquals(res[i], records.get(i).values());
                }
            }
        }
    }

// org.apache.commons.csv.CSVParserTest::testEndOfFileBehaviourExcel
    public void testEndOfFileBehaviourExcel() throws Exception {
        final String[] codes = { "hello,\r\n\r\nworld,\r\n", "hello,\r\n\r\nworld,", "hello,\r\n\r\nworld,\"\"\r\n",
                "hello,\r\n\r\nworld,\"\"", "hello,\r\n\r\nworld,\n", "hello,\r\n\r\nworld,",
                "hello,\r\n\r\nworld,\"\"\n", "hello,\r\n\r\nworld,\"\"" };
        final String[][] res = { { "hello", "" }, { "" }, 
                { "world", "" } };

        for (final String code : codes) {
            try (final CSVParser parser = CSVParser.parse(code, CSVFormat.EXCEL)) {
                final List<CSVRecord> records = parser.getRecords();
                assertEquals(res.length, records.size());
                assertTrue(records.size() > 0);
                for (int i = 0; i < res.length; i++) {
                    assertArrayEquals(res[i], records.get(i).values());
                }
            }
        }
    }

// org.apache.commons.csv.CSVParserTest::testExcelFormat1
    public void testExcelFormat1() throws IOException {
        final String code = "value1,value2,value3,value4\r\na,b,c,d\r\n  x,,," +
                "\r\n\r\n\"\"\"hello\"\"\",\"  \"\"world\"\"\",\"abc\ndef\",\r\n";
        final String[][] res = { { "value1", "value2", "value3", "value4" }, { "a", "b", "c", "d" },
                { "  x", "", "", "" }, { "" }, { "\"hello\"", "  \"world\"", "abc\ndef", "" } };
        try (final CSVParser parser = CSVParser.parse(code, CSVFormat.EXCEL)) {
            final List<CSVRecord> records = parser.getRecords();
            assertEquals(res.length, records.size());
            assertTrue(records.size() > 0);
            for (int i = 0; i < res.length; i++) {
                assertArrayEquals(res[i], records.get(i).values());
            }
        }
    }

// org.apache.commons.csv.CSVParserTest::testExcelFormat2
    public void testExcelFormat2() throws Exception {
        final String code = "foo,baar\r\n\r\nhello,\r\n\r\nworld,\r\n";
        final String[][] res = { { "foo", "baar" }, { "" }, { "hello", "" }, { "" }, { "world", "" } };
        try (final CSVParser parser = CSVParser.parse(code, CSVFormat.EXCEL)) {
            final List<CSVRecord> records = parser.getRecords();
            assertEquals(res.length, records.size());
            assertTrue(records.size() > 0);
            for (int i = 0; i < res.length; i++) {
                assertArrayEquals(res[i], records.get(i).values());
            }
        }
    }

// org.apache.commons.csv.CSVParserTest::testExcelHeaderCountLessThanData
    public void testExcelHeaderCountLessThanData() throws Exception {
        final String code = "A,B,C,,\r\na,b,c,d,e\r\n";
        try (final CSVParser parser = CSVParser.parse(code, CSVFormat.EXCEL.withHeader())) {
            for (final CSVRecord record : parser.getRecords()) {
                Assert.assertEquals("a", record.get("A"));
                Assert.assertEquals("b", record.get("B"));
                Assert.assertEquals("c", record.get("C"));
            }
        }
    }

// org.apache.commons.csv.CSVParserTest::testForEach
    public void testForEach() throws Exception {
        final List<CSVRecord> records = new ArrayList<>();
        try (final Reader in = new StringReader("a,b,c\n1,2,3\nx,y,z")) {
            for (final CSVRecord record : CSVFormat.DEFAULT.parse(in)) {
                records.add(record);
            }
            assertEquals(3, records.size());
            assertArrayEquals(new String[] { "a", "b", "c" }, records.get(0).values());
            assertArrayEquals(new String[] { "1", "2", "3" }, records.get(1).values());
            assertArrayEquals(new String[] { "x", "y", "z" }, records.get(2).values());
        }
    }

// org.apache.commons.csv.CSVParserTest::testGetHeaderMap
    public void testGetHeaderMap() throws Exception {
        try (final CSVParser parser = CSVParser.parse("a,b,c\n1,2,3\nx,y,z",
                CSVFormat.DEFAULT.withHeader("A", "B", "C"))) {
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
    }

// org.apache.commons.csv.CSVParserTest::testGetLine
    public void testGetLine() throws IOException {
        try (final CSVParser parser = CSVParser.parse(CSV_INPUT, CSVFormat.DEFAULT.withIgnoreSurroundingSpaces())) {
            for (final String[] re : RESULT) {
                assertArrayEquals(re, parser.nextRecord().values());
            }

            assertNull(parser.nextRecord());
        }
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
        try (final CSVParser parser = CSVParser.parse(CSV_INPUT_1, CSVFormat.DEFAULT)) {
            final CSVRecord record = parser.getRecords().get(0);
            assertArrayEquals(RESULT[0], record.values());
        }
    }

// org.apache.commons.csv.CSVParserTest::testGetOneLineOneParser
    public void testGetOneLineOneParser() throws IOException {
        final CSVFormat format = CSVFormat.DEFAULT;
        try (final PipedWriter writer = new PipedWriter();
                final CSVParser parser = new CSVParser(new PipedReader(writer), format)) {
            writer.append(CSV_INPUT_1);
            writer.append(format.getRecordSeparator());
            final CSVRecord record1 = parser.nextRecord();
            assertArrayEquals(RESULT[0], record1.values());
            writer.append(CSV_INPUT_2);
            writer.append(format.getRecordSeparator());
            final CSVRecord record2 = parser.nextRecord();
            assertArrayEquals(RESULT[1], record2.values());
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

// org.apache.commons.csv.CSVParserTest::testGetRecordPositionWithCRLF
    public void testGetRecordPositionWithCRLF() throws Exception {
        this.validateRecordPosition(CRLF);
    }

// org.apache.commons.csv.CSVParserTest::testGetRecordPositionWithLF
    public void testGetRecordPositionWithLF() throws Exception {
        this.validateRecordPosition(String.valueOf(LF));
    }

// org.apache.commons.csv.CSVParserTest::testGetRecords
    public void testGetRecords() throws IOException {
        try (final CSVParser parser = CSVParser.parse(CSV_INPUT, CSVFormat.DEFAULT.withIgnoreSurroundingSpaces())) {
            final List<CSVRecord> records = parser.getRecords();
            assertEquals(RESULT.length, records.size());
            assertTrue(records.size() > 0);
            for (int i = 0; i < RESULT.length; i++) {
                assertArrayEquals(RESULT[i], records.get(i).values());
            }
        }
    }

// org.apache.commons.csv.CSVParserTest::testGetRecordWithMultiLineValues
    public void testGetRecordWithMultiLineValues() throws Exception {
        try (final CSVParser parser = CSVParser.parse(
                "\"a\r\n1\",\"a\r\n2\"" + CRLF + "\"b\r\n1\",\"b\r\n2\"" + CRLF + "\"c\r\n1\",\"c\r\n2\"",
                CSVFormat.DEFAULT.withRecordSeparator(CRLF))) {
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
        }
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

        final Iterator<CSVRecord> records = CSVFormat.DEFAULT.withCommentMarker('#').withHeader().parse(in).iterator();

        for (int i = 0; i < 2; i++) {
            assertTrue(records.hasNext());
            final CSVRecord record = records.next();
            assertEquals(record.get(0), record.get("a"));
            assertEquals(record.get(1), record.get("b"));
            assertEquals(record.get(2), record.get("c"));
        }

        assertFalse(records.hasNext());
    }

// org.apache.commons.csv.CSVParserTest::testHeaderMissing
    public void testHeaderMissing() throws Exception {
        final Reader in = new StringReader("a,,c\n1,2,3\nx,y,z");

        final Iterator<CSVRecord> records = CSVFormat.DEFAULT.withHeader().parse(in).iterator();

        for (int i = 0; i < 2; i++) {
            assertTrue(records.hasNext());
            final CSVRecord record = records.next();
            assertEquals(record.get(0), record.get("a"));
            assertEquals(record.get(2), record.get("c"));
        }

        assertFalse(records.hasNext());
    }

// org.apache.commons.csv.CSVParserTest::testHeaderMissingWithNull
    public void testHeaderMissingWithNull() throws Exception {
        final Reader in = new StringReader("a,,c,,d\n1,2,3,4\nx,y,z,zz");
        CSVFormat.DEFAULT.withHeader().withNullString("").withAllowMissingColumnNames().parse(in).iterator();
    }

// org.apache.commons.csv.CSVParserTest::testHeadersMissing
    public void testHeadersMissing() throws Exception {
        final Reader in = new StringReader("a,,c,,d\n1,2,3,4\nx,y,z,zz");
        CSVFormat.DEFAULT.withHeader().withAllowMissingColumnNames().parse(in).iterator();
    }

// org.apache.commons.csv.CSVParserTest::testHeadersMissingException
    public void testHeadersMissingException() throws Exception {
        final Reader in = new StringReader("a,,c,,d\n1,2,3,4\nx,y,z,zz");
        CSVFormat.DEFAULT.withHeader().parse(in).iterator();
    }

// org.apache.commons.csv.CSVParserTest::testIgnoreCaseHeaderMapping
    public void testIgnoreCaseHeaderMapping() throws Exception {
        final Reader in = new StringReader("1,2,3");
        final Iterator<CSVRecord> records = CSVFormat.DEFAULT.withHeader("One", "TWO", "three").withIgnoreHeaderCase()
                .parse(in).iterator();
        final CSVRecord record = records.next();
        assertEquals("1", record.get("one"));
        assertEquals("2", record.get("two"));
        assertEquals("3", record.get("THREE"));
    }

// org.apache.commons.csv.CSVParserTest::testIgnoreEmptyLines
    public void testIgnoreEmptyLines() throws IOException {
        final String code = "\nfoo,baar\n\r\n,\n\n,world\r\n\n";
        
        
        try (final CSVParser parser = CSVParser.parse(code, CSVFormat.DEFAULT)) {
            final List<CSVRecord> records = parser.getRecords();
            assertEquals(3, records.size());
        }
    }

// org.apache.commons.csv.CSVParserTest::testInvalidFormat
    public void testInvalidFormat() throws Exception {
        final CSVFormat invalidFormat = CSVFormat.DEFAULT.withDelimiter(CR);
        try (final CSVParser parser = new CSVParser(null, invalidFormat)) {
            Assert.fail("This test should have thrown an exception.");
        }
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
        assertArrayEquals(new String[] { "a", "b", "c" }, iterator.next().values());
        assertArrayEquals(new String[] { "1", "2", "3" }, iterator.next().values());
        assertTrue(iterator.hasNext());
        assertTrue(iterator.hasNext());
        assertTrue(iterator.hasNext());
        assertArrayEquals(new String[] { "x", "y", "z" }, iterator.next().values());
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
        try (final CSVParser parser = CSVParser.parse(code, CSVFormat.DEFAULT)) {
            final List<CSVRecord> records = parser.getRecords();
            assertEquals(4, records.size());
        }
    }

// org.apache.commons.csv.CSVParserTest::testMappedButNotSetAsOutlook2007ContactExport
    public void testMappedButNotSetAsOutlook2007ContactExport() throws Exception {
        final Reader in = new StringReader("a,b,c\n1,2\nx,y,z");
        final Iterator<CSVRecord> records = CSVFormat.DEFAULT.withHeader("A", "B", "C").withSkipHeaderRecord().parse(in)
                .iterator();
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
        try (final CSVParser parser = CSVParser.parse("a,b,c" + CR + "d,e,f", CSVFormat.DEFAULT)) {
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
        }
    }

// org.apache.commons.csv.CSVParserTest::testNewCSVParserNullReaderFormat
    public void testNewCSVParserNullReaderFormat() throws Exception {
        try (final CSVParser parser = new CSVParser(null, CSVFormat.DEFAULT)) {
            Assert.fail("This test should have thrown an exception.");
        }
    }

// org.apache.commons.csv.CSVParserTest::testNewCSVParserReaderNullFormat
    public void testNewCSVParserReaderNullFormat() throws Exception {
        try (final CSVParser parser = new CSVParser(new StringReader(""), null)) {
            Assert.fail("This test should have thrown an exception.");
        }
    }

// org.apache.commons.csv.CSVParserTest::testNoHeaderMap
    public void testNoHeaderMap() throws Exception {
        try (final CSVParser parser = CSVParser.parse("a,b,c\n1,2,3\nx,y,z", CSVFormat.DEFAULT)) {
            Assert.assertNull(parser.getHeaderMap());
        }
    }

// org.apache.commons.csv.CSVParserTest::testParseFileNullFormat
    public void testParseFileNullFormat() throws Exception {
        CSVParser.parse(new File(""), Charset.defaultCharset(), null);
    }

// org.apache.commons.csv.CSVParserTest::testParseNullFileFormat
    public void testParseNullFileFormat() throws Exception {
        CSVParser.parse((File) null, Charset.defaultCharset(), CSVFormat.DEFAULT);
    }

// org.apache.commons.csv.CSVParserTest::testParseNullStringFormat
    public void testParseNullStringFormat() throws Exception {
        CSVParser.parse((String) null, CSVFormat.DEFAULT);
    }

// org.apache.commons.csv.CSVParserTest::testParseNullUrlCharsetFormat
    public void testParseNullUrlCharsetFormat() throws Exception {
        CSVParser.parse((File) null, Charset.defaultCharset(), CSVFormat.DEFAULT);
    }

// org.apache.commons.csv.CSVParserTest::testParserUrlNullCharsetFormat
    public void testParserUrlNullCharsetFormat() throws Exception {
        try (final CSVParser parser = CSVParser.parse(new URL("http://commons.apache.org"), null, CSVFormat.DEFAULT)) {
            Assert.fail("This test should have thrown an exception.");
        }
    }

// org.apache.commons.csv.CSVParserTest::testParseStringNullFormat
    public void testParseStringNullFormat() throws Exception {
        CSVParser.parse("csv data", null);
    }

// org.apache.commons.csv.CSVParserTest::testParseUrlCharsetNullFormat
    public void testParseUrlCharsetNullFormat() throws Exception {
        try (final CSVParser parser = CSVParser.parse(new URL("http://commons.apache.org"), Charset.defaultCharset(), null)) {
            Assert.fail("This test should have thrown an exception.");
        }
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
        try (final CSVPrinter printer = new CSVPrinter(out, CSVFormat.DEFAULT)) {
            final String input = "a,b,c\r\n1,2,3\r\nx,y,z\r\n";
            for (final CSVRecord record : CSVParser.parse(input, CSVFormat.DEFAULT)) {
                printer.printRecord(record);
            }
            assertEquals(input, out.toString());
        }
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

// org.apache.commons.csv.CSVParserTest::testSkipHeaderOverrideDuplicateHeaders
    public void testSkipHeaderOverrideDuplicateHeaders() throws Exception {
        final Reader in = new StringReader("a,a,a\n1,2,3\nx,y,z");
        final Iterator<CSVRecord> records = CSVFormat.DEFAULT.withHeader("X", "Y", "Z").withSkipHeaderRecord().parse(in)
                .iterator();
        final CSVRecord record = records.next();
        assertEquals("1", record.get("X"));
        assertEquals("2", record.get("Y"));
        assertEquals("3", record.get("Z"));
    }

// org.apache.commons.csv.CSVParserTest::testSkipSetAltHeaders
    public void testSkipSetAltHeaders() throws Exception {
        final Reader in = new StringReader("a,b,c\n1,2,3\nx,y,z");
        final Iterator<CSVRecord> records = CSVFormat.DEFAULT.withHeader("X", "Y", "Z").withSkipHeaderRecord().parse(in)
                .iterator();
        final CSVRecord record = records.next();
        assertEquals("1", record.get("X"));
        assertEquals("2", record.get("Y"));
        assertEquals("3", record.get("Z"));
    }

// org.apache.commons.csv.CSVParserTest::testSkipSetHeader
    public void testSkipSetHeader() throws Exception {
        final Reader in = new StringReader("a,b,c\n1,2,3\nx,y,z");
        final Iterator<CSVRecord> records = CSVFormat.DEFAULT.withHeader("a", "b", "c").withSkipHeaderRecord().parse(in)
                .iterator();
        final CSVRecord record = records.next();
        assertEquals("1", record.get("a"));
        assertEquals("2", record.get("b"));
        assertEquals("3", record.get("c"));
    }

// org.apache.commons.csv.CSVParserTest::testStartWithEmptyLinesThenHeaders
    public void testStartWithEmptyLinesThenHeaders() throws Exception {
        final String[] codes = { "\r\n\r\n\r\nhello,\r\n\r\n\r\n", "hello,\n\n\n", "hello,\"\"\r\n\r\n\r\n",
                "hello,\"\"\n\n\n" };
        final String[][] res = { { "hello", "" }, { "" }, 
                { "" } };
        for (final String code : codes) {
            try (final CSVParser parser = CSVParser.parse(code, CSVFormat.EXCEL)) {
                final List<CSVRecord> records = parser.getRecords();
                assertEquals(res.length, records.size());
                assertTrue(records.size() > 0);
                for (int i = 0; i < res.length; i++) {
                    assertArrayEquals(res[i], records.get(i).values());
                }
            }
        }
    }

// org.apache.commons.csv.CSVParserTest::testTrailingDelimiter
    public void testTrailingDelimiter() throws Exception {
        final Reader in = new StringReader("a,a,a,\n\"1\",\"2\",\"3\",\nx,y,z,");
        final Iterator<CSVRecord> records = CSVFormat.DEFAULT.withHeader("X", "Y", "Z").withSkipHeaderRecord()
                .withTrailingDelimiter().parse(in).iterator();
        final CSVRecord record = records.next();
        assertEquals("1", record.get("X"));
        assertEquals("2", record.get("Y"));
        assertEquals("3", record.get("Z"));
        Assert.assertEquals(3, record.size());
    }

// org.apache.commons.csv.CSVParserTest::testTrim
    public void testTrim() throws Exception {
        final Reader in = new StringReader("a,a,a\n\" 1 \",\" 2 \",\" 3 \"\nx,y,z");
        final Iterator<CSVRecord> records = CSVFormat.DEFAULT.withHeader("X", "Y", "Z").withSkipHeaderRecord()
                .withTrim().parse(in).iterator();
        final CSVRecord record = records.next();
        assertEquals("1", record.get("X"));
        assertEquals("2", record.get("Y"));
        assertEquals("3", record.get("Z"));
        Assert.assertEquals(3, record.size());
    }

// org.apache.commons.csv.CSVParserTest::testIteratorSequenceBreaking
    public void testIteratorSequenceBreaking() throws IOException {
        final String fiveRows = "1\n2\n3\n4\n5\n";

        
        CSVParser parser = CSVFormat.DEFAULT.parse(new StringReader(fiveRows));
        int recordNumber = 0;
        Iterator<CSVRecord> iter = parser.iterator();
        recordNumber = 0;
        while (iter.hasNext()) {
            CSVRecord record = iter.next();
            recordNumber++;
            assertEquals(String.valueOf(recordNumber), record.get(0));
            if (recordNumber >= 2) {
                break;
            }
        }
        iter.hasNext();
        while (iter.hasNext()) {
            CSVRecord record = iter.next();
            recordNumber++;
            assertEquals(String.valueOf(recordNumber), record.get(0));
        }

        
        parser = CSVFormat.DEFAULT.parse(new StringReader(fiveRows));
        recordNumber = 0;
        for (CSVRecord record : parser) {
            recordNumber++;
            assertEquals(String.valueOf(recordNumber), record.get(0));
            if (recordNumber >= 2) {
                break;
            }
        }
        for (CSVRecord record : parser) {
            recordNumber++;
            assertEquals(String.valueOf(recordNumber), record.get(0));
        }

        
        parser = CSVFormat.DEFAULT.parse(new StringReader(fiveRows));
        recordNumber = 0;
        for (CSVRecord record : parser) {
            recordNumber++;
            assertEquals(String.valueOf(recordNumber), record.get(0));
            if (recordNumber >= 2) {
                break;
            }
        }
        parser.iterator().hasNext();
        for (CSVRecord record : parser) {
            recordNumber++;
            assertEquals(String.valueOf(recordNumber), record.get(0));
        }
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
        final Map<String, String> map = new ConcurrentHashMap<>();
        this.recordWithHeader.putIn(map);
        this.validateMap(map, false);
        
        final TreeMap<String, String> map2 = recordWithHeader.putIn(new TreeMap<String, String>());
        this.validateMap(map2, false);
    }

// org.apache.commons.csv.CSVRecordTest::testRemoveAndAddColumns
    public void testRemoveAndAddColumns() throws IOException {
        
        try (final CSVPrinter printer = new CSVPrinter(new StringBuilder(), CSVFormat.DEFAULT)) {
            final Map<String, String> map = recordWithHeader.toMap();
            map.remove("OldColumn");
            map.put("ZColumn", "NewValue");
            
            final ArrayList<String> list = new ArrayList<>(map.values());
            Collections.sort(list);
            printer.printRecord(list);
            Assert.assertEquals("A,B,C,NewValue" + CSVFormat.DEFAULT.getRecordSeparator(), printer.getOut().toString());
        }
    }

// org.apache.commons.csv.CSVRecordTest::testToMap
    public void testToMap() {
        final Map<String, String> map = this.recordWithHeader.toMap();
        this.validateMap(map, true);
    }

// org.apache.commons.csv.CSVRecordTest::testToMapWithShortRecord
    public void testToMapWithShortRecord() throws Exception {
        try (final CSVParser parser = CSVParser.parse("a,b", CSVFormat.DEFAULT.withHeader("A", "B", "C"))) {
            final CSVRecord shortRec = parser.iterator().next();
            shortRec.toMap();
        }
    }

// org.apache.commons.csv.CSVRecordTest::testToMapWithNoHeader
    public void testToMapWithNoHeader() throws Exception {
        try (final CSVParser parser = CSVParser.parse("a,b", CSVFormat.newFormat(','))) {
            final CSVRecord shortRec = parser.iterator().next();
            final Map<String, String> map = shortRec.toMap();
            assertNotNull("Map is not null.", map);
            assertTrue("Map is empty.", map.isEmpty());
        }
    }

// org.apache.commons.csv.issues.JiraCsv198Test::test
    public void test() throws UnsupportedEncodingException, IOException {
        final InputStream pointsOfReference = getClass().getResourceAsStream("/CSV-198/optd_por_public.csv");
        Assert.assertNotNull(pointsOfReference);
        try (@SuppressWarnings("resource")
        CSVParser parser = CSV_FORMAT.parse(new InputStreamReader(pointsOfReference, "UTF-8"))) {
            for (final CSVRecord record : parser) {
                final String locationType = record.get("location_type");
                Assert.assertNotNull(locationType);
            }
        }
    }

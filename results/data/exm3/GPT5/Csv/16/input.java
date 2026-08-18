// buggy function
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

// trigger testcase
// org/apache/commons/csv/CSVParserTest.java::testIteratorSequenceBreaking
@Test
    public void testIteratorSequenceBreaking() throws IOException {
        final String fiveRows = "1\n2\n3\n4\n5\n";

        // Iterator hasNext() shouldn't break sequence
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

        // Consecutive enhanced for loops shouldn't break sequence
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

        // Consecutive enhanced for loops with hasNext() peeking shouldn't break sequence
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

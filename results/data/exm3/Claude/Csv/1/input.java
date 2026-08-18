// buggy function
    public int read() throws IOException {
        int current = super.read();
        if (current == '\n') {
            lineCounter++;
        }
        lastChar = current;
        return lastChar;
    }

// trigger testcase
// org/apache/commons/csv/CSVParserTest.java::testGetLineNumberWithCR
@Test
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

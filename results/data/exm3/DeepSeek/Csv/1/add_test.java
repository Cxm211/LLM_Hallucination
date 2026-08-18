// org/apache/commons/csv/CSVParserTest.java
@Test
    public void testGetLineNumberWithCRAtEOF() throws Exception {
        CSVParser parser = new CSVParser("a\r", CSVFormat.DEFAULT.withLineSeparator("\r"));
        assertEquals(0, parser.getLineNumber());
        assertNotNull(parser.getRecord());
        assertEquals(1, parser.getLineNumber());
        assertNull(parser.getRecord());
    }

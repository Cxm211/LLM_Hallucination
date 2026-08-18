// org/apache/commons/csv/CSVParserTest.java
@Test
    public void testGetLineNumberWithCRLFAndLineSeparatorCR() throws Exception {
        CSVParser parser = new CSVParser("a\r\n", CSVFormat.DEFAULT.withLineSeparator("\r"));
        assertEquals(0, parser.getLineNumber());
        assertNotNull(parser.getRecord());
        assertEquals(1, parser.getLineNumber());
        assertNull(parser.getRecord());
    }

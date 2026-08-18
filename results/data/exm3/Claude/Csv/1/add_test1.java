// org/apache/commons/csv/CSVParserTest.java
@Test
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
// org/apache/commons/csv/CSVParserTest.java
@Test
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
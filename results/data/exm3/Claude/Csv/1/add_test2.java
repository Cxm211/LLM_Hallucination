// org/apache/commons/csv/CSVParserTest.java
@Test
public void testGetLineNumberWithMixedLineEndings() throws Exception {
    CSVParser parser = new CSVParser("a\rb\nc\r\nd", CSVFormat.DEFAULT);
    
    assertEquals(0, parser.getLineNumber());
    assertNotNull(parser.getRecord());
    assertEquals(1, parser.getLineNumber());
    assertNotNull(parser.getRecord());
    assertEquals(2, parser.getLineNumber());
    assertNotNull(parser.getRecord());
    assertEquals(3, parser.getLineNumber());
    assertNotNull(parser.getRecord());
    assertEquals(3, parser.getLineNumber());
}
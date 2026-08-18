// org/apache/commons/csv/CSVParserTest.java::testGetLineNumberWithCRConsecutive
@Test
public void testGetLineNumberWithCRConsecutive() throws Exception {
    CSVParser parser = new CSVParser("a\r\rb\rc", CSVFormat.DEFAULT.withLineSeparator("\r"));

    assertEquals(0, parser.getLineNumber());
    assertNotNull(parser.getRecord());
    assertEquals(1, parser.getLineNumber());
    assertNotNull(parser.getRecord());
    assertEquals(2, parser.getLineNumber());
    assertNotNull(parser.getRecord());
    assertEquals(3, parser.getLineNumber());
    assertNotNull(parser.getRecord());
    assertEquals(3, parser.getLineNumber());
    assertNull(parser.getRecord());
}
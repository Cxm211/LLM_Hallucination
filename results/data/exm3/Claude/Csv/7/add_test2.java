// org/apache/commons/csv/CSVParserTest.java
@Test
public void testNoDuplicateHeaderEntriesWithEmptyHeader() throws Exception {
    CSVParser parser = CSVParser.parse("a,b,c\n1,2,3", CSVFormat.DEFAULT.withHeader(new String[]{}));
    assertNotNull(parser);
    parser.close();
}
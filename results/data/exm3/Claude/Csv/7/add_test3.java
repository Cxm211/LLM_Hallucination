// org/apache/commons/csv/CSVParserTest.java
@Test
public void testNoDuplicateHeaderEntriesWithExplicitHeader() throws Exception {
    CSVParser parser = CSVParser.parse("1,2,3", CSVFormat.DEFAULT.withHeader("a", "b", "c"));
    assertNotNull(parser);
    parser.close();
}
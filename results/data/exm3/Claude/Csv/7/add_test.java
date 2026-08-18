// org/apache/commons/csv/CSVParserTest.java
@Test(expected = IllegalArgumentException.class)
public void testDuplicateHeaderEntriesWithExplicitHeader() throws Exception {
    CSVParser.parse("1,2,3\nx,y,z", CSVFormat.DEFAULT.withHeader("a", "b", "a"));
}
// org/apache/commons/csv/CSVParserTest.java
@Test(expected = IllegalArgumentException.class)
public void testDuplicateHeaderEntriesMultipleDuplicates() throws Exception {
    CSVParser.parse("a,b,a,c,b\n1,2,3,4,5", CSVFormat.DEFAULT.withHeader(new String[]{}));
}
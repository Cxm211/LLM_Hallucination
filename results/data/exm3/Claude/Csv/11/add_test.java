// org/apache/commons/csv/CSVParserTest.java
@Test(expected = IllegalArgumentException.class)
public void testHeaderDuplicateNonEmpty() throws Exception {
    final Reader in = new StringReader("a,b,a\n1,2,3");
    CSVFormat.DEFAULT.withHeader().parse(in).iterator();
}
// org/apache/commons/csv/CSVParserTest.java
@Test(expected = IllegalArgumentException.class)
public void testHeaderDuplicateEmptyWithIgnoreFalse() throws Exception {
    final Reader in = new StringReader("a,,c,,d\n1,2,3,4,5");
    CSVFormat.DEFAULT.withHeader().withIgnoreEmptyHeaders(false).parse(in).iterator();
}
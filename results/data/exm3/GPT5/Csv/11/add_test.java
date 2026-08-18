// org/apache/commons/csv/CSVParserTest.java
@Test(expected = IllegalArgumentException.class)
public void testHeaderMissingWithNullNoIgnore() throws Exception {
    final Reader in = new StringReader("a,,c,,d\n1,2,3,4\nx,y,z,zz");
    CSVFormat.DEFAULT.withHeader().withNullString("").withIgnoreEmptyHeaders(false).parse(in).iterator();
}
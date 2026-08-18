// org/apache/commons/lang3/time/FastDateParserTest.java
@Test(expected=IllegalArgumentException.class)
public void testUnterminatedQuoteAtEnd() throws Exception {
    new FastDateParser("yyyy-MM-dd'", TimeZone.getDefault(), Locale.getDefault());
}
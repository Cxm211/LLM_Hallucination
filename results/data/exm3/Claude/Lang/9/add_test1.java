// org/apache/commons/lang3/time/FastDateParserTest.java
@Test(expected=IllegalArgumentException.class)
public void testUnterminatedQuoteMiddle() throws Exception {
    new FastDateParser("'test", TimeZone.getDefault(), Locale.getDefault());
}
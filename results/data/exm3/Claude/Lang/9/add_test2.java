// org/apache/commons/lang3/time/FastDateParserTest.java
@Test
public void testProperlyTerminatedQuote() throws Exception {
    FastDateParser parser = new FastDateParser("'dd'dd", TimeZone.getDefault(), Locale.getDefault());
    assertNotNull(parser);
}
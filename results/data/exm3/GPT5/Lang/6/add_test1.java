// org/apache/commons/lang3/StringUtilsTest.java
@Test
public void testEscapeUnpairedLowSurrogate() throws Exception {
    final String s = "\uDE30"; // unpaired low surrogate
    assertEquals(s, StringEscapeUtils.escapeCsv(s));
}
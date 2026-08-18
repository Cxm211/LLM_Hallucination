// org/apache/commons/lang3/StringUtilsTest.java
@Test
public void testEscapeUnpairedHighSurrogate() throws Exception {
    final String s = "\uD83D" + "X"; // unpaired high surrogate
    assertEquals(s, StringEscapeUtils.escapeCsv(s));
}
// org/jsoup/helper/DataUtilTest.java
@Test
public void testCharsetWithWhitespaceInQuotes() {
    assertEquals("utf-8", DataUtil.getCharsetFromContentType("text/html; charset=\" utf-8 \""));
    assertEquals("ISO-8859-1", DataUtil.getCharsetFromContentType("text/html;charset=\" ISO-8859-1 \""));
}
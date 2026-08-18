// org/jsoup/helper/DataUtilTest.java
@Test public void discardsSpuriousByteOrderMarkWithExplicitCharset() {
    String html = "\uFEFF<html><head><title>One</title></head><body>Two</body></html>";
    ByteBuffer buffer = Charset.forName("UTF-8").encode(html);
    Document doc = DataUtil.parseByteData(buffer, "ISO-8859-1", "http://foo.com/", Parser.htmlParser());
    assertEquals("One", doc.head().text());
    assertEquals("UTF-8", doc.outputSettings().charset().displayName());
}
// org/jsoup/helper/DataUtilTest.java
@Test public void discardsSpuriousByteOrderMarkWithCharsetInMeta() {
    String html = "\uFEFF<html><head><meta charset=\"ISO-8859-1\"><title>One</title></head><body>Two</body></html>";
    ByteBuffer buffer = Charset.forName("UTF-8").encode(html);
    Document doc = DataUtil.parseByteData(buffer, null, "http://foo.com/", Parser.htmlParser());
    assertEquals("One", doc.head().text());
    assertEquals("UTF-8", doc.outputSettings().charset().displayName());
}
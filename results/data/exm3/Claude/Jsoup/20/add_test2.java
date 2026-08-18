// org/jsoup/helper/DataUtilTest.java
@Test public void handlesEmptyDocumentWithBOM() {
    String html = "\uFEFF";
    ByteBuffer buffer = Charset.forName("UTF-8").encode(html);
    Document doc = DataUtil.parseByteData(buffer, "UTF-8", "http://foo.com/", Parser.htmlParser());
    assertNotNull(doc);
}
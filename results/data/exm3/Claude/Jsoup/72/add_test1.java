// org/jsoup/parser/HtmlParserTest.java
@Test
public void parseEmptyCommentLikeTag() throws Exception {
    Document doc = Jsoup.parse("<!>");
    assertTrue(doc.childNode(0) instanceof Comment);
    Comment comment = (Comment) doc.childNode(0);
    assertEquals("", comment.getData());
}
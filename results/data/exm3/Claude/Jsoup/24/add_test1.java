// org/jsoup/parser/HtmlParserTest.java
@Test public void handlesEndTagWithWhitespaceInScript() {
    String html = "<script>x = '</script >';</script>";
    Document node = Jsoup.parseBodyFragment(html);
    assertEquals("<script>x = '</script >';</script>", node.body().html());
}
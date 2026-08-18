// org/jsoup/parser/HtmlParserTest.java
@Test public void handlesPartialEndTagInScript() {
    String html = "<script>var x = '</script';</script>";
    Document node = Jsoup.parseBodyFragment(html);
    assertEquals("<script>var x = '</script';</script>", node.body().html());
}
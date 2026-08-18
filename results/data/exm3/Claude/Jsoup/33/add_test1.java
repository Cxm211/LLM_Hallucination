// org/jsoup/parser/HtmlParserTest.java
@Test public void handlesKnownVoidElementsSelfClosing() {
    String h = "<img src='test.jpg' /><br /><input type='text' />";
    Document doc = Jsoup.parse(h);
    assertEquals("<img src=\"test.jpg\" /><br /><input type=\"text\" />", TextUtil.stripNewlines(doc.body().html()));
}
// org/jsoup/parser/HtmlParserTest.java
@Test public void handlesNestedKnownTagsSelfClosing() {
    String h = "<div><span /></div>";
    Document doc = Jsoup.parse(h);
    assertEquals("<div><span></span></div>", TextUtil.stripNewlines(doc.body().html()));
}
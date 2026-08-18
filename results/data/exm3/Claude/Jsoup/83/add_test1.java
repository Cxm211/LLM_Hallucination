// org/jsoup/parser/HtmlParserTest.java
@Test public void handlesLessInTagNameFollowedByGreater() {
    String html = "<div<>content";
    Document doc = Jsoup.parse(html);
    assertEquals("<div></div>content", TextUtil.stripNewlines(doc.body().html()));
}
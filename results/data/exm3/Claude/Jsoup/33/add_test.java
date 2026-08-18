// org/jsoup/parser/HtmlParserTest.java
@Test public void handlesUnknownSelfClosingTags() {
    String h = "<custom /><unknown id='test' />content<regular>text</regular>";
    Document doc = Jsoup.parse(h);
    assertEquals("<custom /><unknown id=\"test\" />content<regular>text</regular>", TextUtil.stripNewlines(doc.body().html()));
}
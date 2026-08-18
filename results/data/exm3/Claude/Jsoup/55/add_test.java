// org/jsoup/parser/AttributeParseTest.java
@Test public void selfClosingWithMultipleSlashes() {
    String html = "<img //class='test'/>";
    Document doc = Jsoup.parse(html);
    assertEquals("<img class=\"test\">", doc.body().html());
}
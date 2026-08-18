// org/jsoup/parser/HtmlParserTest.java
@Test public void convertsImageToImgInNestedSvg() {
    String h = "<body><div><svg><image /></svg></div></body>";
    Document doc = Jsoup.parse(h);
    assertEquals("<div>\n <svg>\n  <image />\n </svg>\n</div>", doc.body().html());
}
// org/jsoup/parser/HtmlParserTest.java
@Test public void convertsImageToImgOutsideSvg() {
    String h = "<body><image /><svg></svg></body>";
    Document doc = Jsoup.parse(h);
    assertEquals("<img />\n<svg></svg>", doc.body().html());
}
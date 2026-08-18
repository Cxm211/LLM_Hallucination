// org/jsoup/parser/ParserTest.java
@Test public void handlesTextAfterStyle() {
    String h = "<div><style>body { color: red; }</style> after style text</div>";
    Document doc = Jsoup.parse(h);
    assertEquals("<div><style>body { color: red; }</style> after style text</div>", TextUtil.stripNewlines(doc.html()));
}

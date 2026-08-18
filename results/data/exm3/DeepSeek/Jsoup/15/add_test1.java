// org/jsoup/parser/ParserTest.java
@Test public void handlesScriptWithAttributes() {
    String html = "<script id=\"s\" type=\"text/javascript\">console.log('hi')</script>";
    Document doc = Jsoup.parse(html);
    Element script = doc.select("script").first();
    assertEquals("console.log('hi')", script.data());
    assertEquals("", script.text());
    assertEquals("s", script.id());
    assertEquals("text/javascript", script.attr("type"));
}

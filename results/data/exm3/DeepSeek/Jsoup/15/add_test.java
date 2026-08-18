// org/jsoup/parser/ParserTest.java
@Test public void handlesScriptInParagraph() {
    String html = "<p><script>var x = 1;</script></p>";
    Document doc = Jsoup.parse(html);
    Element script = doc.select("script").first();
    assertEquals("var x = 1;", script.data());
    assertEquals("", script.text());
}

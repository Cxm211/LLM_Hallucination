// org/jsoup/parser/ParserTest.java
@Test public void handlesScriptInDiv() {
    String html = "<div><script>alert('div');</script></div>";
    Document doc = Jsoup.parse(html);
    Element script = doc.select("script").first();
    assertEquals("alert('div');", script.data());
    assertEquals("", script.text());
}

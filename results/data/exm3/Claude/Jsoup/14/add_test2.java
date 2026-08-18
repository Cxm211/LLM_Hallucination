// org/jsoup/parser/ParserTest.java
@Test public void handlesTextareaWithUppercaseClosingTag() {
    Document doc = Jsoup.parse("<textarea>some text</TEXTAREA><p>after</p>");
    Element textarea = doc.select("textarea").first();
    assertEquals("some text", textarea.text());
    assertEquals("after", doc.select("p").first().text());
}
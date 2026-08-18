// org/jsoup/parser/ParserTest.java
@Test public void handlesTextareaWithMixedCaseClosingTag() {
    Document doc = Jsoup.parse("<textarea>content</TextArea>");
    Element textarea = doc.select("textarea").first();
    assertEquals("content", textarea.text());
}
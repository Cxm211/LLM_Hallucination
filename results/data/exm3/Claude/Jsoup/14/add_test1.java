// org/jsoup/parser/ParserTest.java
@Test public void handlesTitleWithAllUppercaseClosingTag() {
    Document doc = Jsoup.parse("<title>My Title</TITLE>");
    assertEquals("My Title", doc.title());
}
// org/jsoup/parser/ParserTest.java
@Test public void handlesMixedCaseTitle() {
    Document doc = Jsoup.parse("<TITLE>One</title>");
    assertEquals("One", doc.title());
}

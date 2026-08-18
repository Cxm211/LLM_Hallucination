// org/jsoup/parser/ParserTest.java
@Test public void handlesMixedCaseTextarea() {
    Document doc = Jsoup.parse("<TEXTAREA>One</textarea>");
    Element t = doc.select("textarea").first();
    assertEquals("One", t.text());
}

// org/jsoup/parser/HtmlParserTest.java
@Test public void listingSkipsFirstNewline() {
    Document doc = Jsoup.parse("<listing>\n\nData\nHere\n</listing>");
    Element listing = doc.selectFirst("listing");
    assertEquals("Data Here", listing.text());
    assertEquals("\nData\nHere\n", listing.wholeText());
}
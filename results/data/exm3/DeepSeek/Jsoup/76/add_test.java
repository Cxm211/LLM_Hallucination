// org/jsoup/parser/HtmlParserTest.java
@Test public void listingSkipsFirstNewline() {
        Document doc = Jsoup.parse("<listing>\n\nOne\nTwo\n</listing>");
        Element listing = doc.selectFirst("listing");
        assertEquals("One\nTwo", listing.text());
        assertEquals("\nOne\nTwo\n", listing.wholeText());
    }

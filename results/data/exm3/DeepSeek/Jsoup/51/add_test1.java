// org/jsoup/parser/HtmlParserTest.java
@Test public void testSupportsNonAsciiCyrillicTag() {
        String body = "<тег>Да</тег>";
        Document doc = Jsoup.parse(body);
        Elements els = doc.select("тег");
        assertEquals("Да", els.text());
    }

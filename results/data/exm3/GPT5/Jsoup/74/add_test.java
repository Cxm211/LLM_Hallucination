// org/jsoup/nodes/ElementTest.java
@Test public void testStripsLeadingAndInlineInvisibles() {
        Document doc = Jsoup.parse("<p>\u200B\u200D\u00ADHello\u200C\u200DWorld</p>");
        Element p = doc.select("p").first();
        assertEquals("HelloWorld", p.text());
    }
// org/jsoup/parser/HtmlParserTest.java
@Test public void testSupportsNonAsciiGreekTag() {
        String body = "<αtest>Yes</αtest>";
        Document doc = Jsoup.parse(body);
        Elements els = doc.select("αtest");
        assertEquals("Yes", els.text());
    }

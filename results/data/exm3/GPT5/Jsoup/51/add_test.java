// org/jsoup/parser/HtmlParserTest.java::testSupportsNonAsciiTags
@Test public void testSupportsSupplementaryPlaneTags() {
        String body = "<\uD801\uDC00>Yes</\uD801\uDC00>"; // U+10400 Deseret Capital Letter Long I
        Document doc = Jsoup.parse(body);
        Elements els = doc.select("\uD801\uDC00");
        assertEquals("Yes", els.text());
    }
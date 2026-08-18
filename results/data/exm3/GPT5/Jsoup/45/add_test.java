// org/jsoup/parser/HtmlParserTest.java::testReinsertionModeForThWithLeadingContent
@Test public void testReinsertionModeForThWithLeadingContent() {
        String body = "<body><table><tr><th><div></div><table><tr><td></td></tr></table></th></tr></table></body>";
        Document doc = Jsoup.parse(body);
        assertEquals(1, doc.body().children().size());
    }
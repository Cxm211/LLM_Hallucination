// org/jsoup/parser/HtmlParserTest.java::selfClosingVoidIsNotAnError
@Test public void selfClosingOnUnknownIsNotVoid() {
        String html = "<p>One</p><foo />";
        Parser parser = Parser.htmlParser().setTrackErrors(10);
        Document doc = Jsoup.parse(html, "", parser);
        assertEquals(0, parser.getErrors().size());
        doc.outputSettings().prettyPrint(false);
        assertEquals("<p>One</p><foo></foo>", doc.body().html());
    }
// org/jsoup/parser/HtmlParserTest.java::dropsDuplicateAttributes
@Test public void dropsDuplicateBooleanAttributes() {
        String html = "<p One One=Two one three THREE four=Four four></p>";
        Parser parser = Parser.htmlParser().setTrackErrors(10);
        Document doc = parser.parseInput(html, "");
        Element p = doc.selectFirst("p");
        assertEquals("<p one three four=\"Four\"></p>", p.outerHtml());
    }
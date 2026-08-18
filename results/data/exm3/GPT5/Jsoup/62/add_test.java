// org/jsoup/parser/HtmlParserTest.java::caseSensitiveParseTreeNested
@Test public void caseSensitiveParseTreeNested() {
        String html = "<r><X><i>A</i></X></r>";
        Parser parser = Parser.htmlParser();
        parser.settings(ParseSettings.preserveCase);
        Document doc = parser.parseInput(html, "");
        assertEquals("<r> <X> <i> A </i> </X> </r>", StringUtil.normaliseWhitespace(doc.body().html()));
    }
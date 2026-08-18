// org/jsoup/parser/HtmlParserTest.java::preservedCaseListItemsDontNest
@Test public void preservedCaseListItemsDontNest() {
        String html = "<UL><LI>One<LI>Two</UL>";
        Document doc = Parser.htmlParser()
            .settings(ParseSettings.preserveCase)
            .parseInput(html, "");
        assertEquals("<UL> <LI> One </LI> <LI> Two </LI> </UL>", StringUtil.normaliseWhitespace(doc.body().html()));
    }
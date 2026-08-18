// org/jsoup/parser/HtmlParserTest.java
@Test public void preservedCaseBoldCantNest() {
        String html = "<B>ONE <B>Two</B></B>";
        Document doc = Parser.htmlParser()
            .settings(ParseSettings.preserveCase)
            .parseInput(html, "");
        assertEquals("<B> ONE </B> <B> Two </B>", StringUtil.normaliseWhitespace(doc.body().html()));
    }

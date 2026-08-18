// org/jsoup/parser/HtmlParserTest.java
@Test public void preservedCaseTableWithSelect() {
        String html = "<TABLE><SELECT></SELECT></TABLE><DIV>After</DIV>";
        Document doc = Parser.htmlParser()
            .settings(ParseSettings.preserveCase)
            .parseInput(html, "");
        Element table = doc.body().select("table").first();
        Element div = doc.body().select("div").first();
        assertNotNull(table);
        assertNotNull(div);
    }

// org/jsoup/parser/HtmlParserTest.java::testTemplateInsideTable
@Test public void testTemplateInsideTableRow() {
        String html = "<table><tr><template><td>One</td></template><td>Two</td></tr></table>";
        Document doc = Jsoup.parse(html);
        Elements templates = doc.body().getElementsByTag("template");
        for (Element template : templates) {
            assertTrue(template.childNodeSize() > 1);
        }
    }
// org/jsoup/parser/HtmlParserTest.java::handlesMixedCaseScriptEndTag
@Test public void handlesMixedCaseScriptEndTag() {
        String html = "<script>var a=1;</ScRipT>";
        Document node = Jsoup.parseBodyFragment(html);
        assertEquals("<script>var a=1;</script>", node.body().html());
    }
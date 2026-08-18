// org/jsoup/nodes/ElementTest.java::testWrapMovesAllRemainders
@Test public void testWrapMovesAllRemainders() {
        Document doc = Jsoup.parse("<span id=1>One</span>");
        Element el = doc.select("span").first();
        el.wrap("<div></div><p>two</p><b>three</b>");
        assertEquals("<div><span id=\"1\">One</span><p>two</p><b>three</b></div>", TextUtil.stripNewlines(el.parent().outerHtml()));
    }
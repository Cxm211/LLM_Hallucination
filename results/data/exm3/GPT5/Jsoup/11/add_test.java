// org/jsoup/select/SelectorTest.java::notMultiple
@Test public void notMultiple() {
        Document doc = Jsoup.parse("<div id=1><span>One</span></div><p>Two</p>");

        Elements els = doc.body().select(":not(div, span)");
        assertEquals(2, els.size());
        assertEquals("body", els.first().tagName());
        assertEquals("p", els.last().tagName());
    }
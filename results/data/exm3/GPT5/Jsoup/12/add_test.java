// org/jsoup/select/SelectorTest.java
@Test public void testByAttributeRegexCombinedChild() {
        Document doc = Jsoup.parse("<div><table class=x><td>Hello</td></table></div>");
        Elements els = doc.select("div > table[class~=x|y]");
        assertEquals(1, els.size());
        assertEquals("Hello", els.text());
    }
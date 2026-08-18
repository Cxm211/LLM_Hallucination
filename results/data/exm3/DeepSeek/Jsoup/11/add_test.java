// org/jsoup/select/SelectorTest.java
@Test public void commaInsideHas() {
        Document doc = Jsoup.parse("<div><span></span></div><div><p></p></div><div class=class></div>");
        Elements els = doc.select("div:has(span, p), .class");
        assertEquals(3, els.size());
        assertEquals("div", els.get(0).tagName());
        assertEquals("div", els.get(1).tagName());
        assertEquals("div", els.get(2).tagName());
        assertEquals("class", els.get(2).className());
    }

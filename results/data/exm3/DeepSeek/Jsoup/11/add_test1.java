// org/jsoup/select/SelectorTest.java
@Test public void commaInsideAttribute() {
        Document doc = Jsoup.parse("<div data=\"a,b\"></div><div data=c></div>");
        Elements els = doc.select("[data=\"a,b\"]");
        assertEquals(1, els.size());
        assertEquals("div", els.first().tagName());
    }

// org/jsoup/select/SelectorTest.java
@Test public void matchTextVariants() {
        // case 1: no br
        Document doc = Jsoup.parse("<p>Single text</p>");
        Elements els = doc.select("p:matchText");
        assertEquals(1, els.size());
        assertEquals("Single text", els.first().text());
        
        // case 2: with br and nested element
        doc = Jsoup.parse("<div>Start<br><span>Middle</span><br>End</div>");
        els = doc.select("div:matchText");
        assertEquals(2, els.size());
        assertEquals("Start", els.get(0).text());
        assertEquals("End", els.get(1).text());
    }

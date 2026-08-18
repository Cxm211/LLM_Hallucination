// org/jsoup/nodes/EntitiesTest.java::letterDigitEntitiesNoSemicolon
@Test public void letterDigitEntitiesNoSemicolon() {
        String html = "<p>&sup1 &frac12</p>";
        Document doc = Jsoup.parse(html);
        Element p = doc.select("p").first();
        assertEquals("¹ ½", p.text());
    }
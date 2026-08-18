// org/jsoup/nodes/ElementTest.java
@Test public void testElementSiblingIndexEmptyDuplicates() {
        Document doc = Jsoup.parse("<div><p></p><p></p><p></p></div>");
        Elements ps = doc.select("p");
        assertEquals(0, ps.get(0).elementSiblingIndex());
        assertEquals(1, ps.get(1).elementSiblingIndex());
        assertEquals(2, ps.get(2).elementSiblingIndex());
    }
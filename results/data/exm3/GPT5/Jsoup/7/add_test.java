// org/jsoup/nodes/DocumentTest.java::testNormalisesStructure
@Test public void testMovesMultipleElementsFromNoscriptInHead() {
        Document doc = Jsoup.parse("<html><head><noscript><div>one</div><p>two</p></noscript></head><body><p>three</p></body></html>");
        assertEquals("<html><head><noscript></noscript></head><body><div>one</div><p>two</p><p>three</p></body></html>", TextUtil.stripNewlines(doc.html()));
    }
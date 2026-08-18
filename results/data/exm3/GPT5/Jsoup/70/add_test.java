// org/jsoup/nodes/ElementTest.java
@Test public void testKeepsPreTextDirectTextNode() {
        String h = "<pre>code\n\ncode</pre>";
        Document doc = Jsoup.parse(h);
        assertEquals("code\n\ncode", doc.text());
        assertEquals("<pre>code\n\ncode</pre>", doc.body().html());
    }
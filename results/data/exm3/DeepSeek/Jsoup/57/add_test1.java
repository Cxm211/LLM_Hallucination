// org/jsoup/nodes/ElementTest.java
@Test
    public void testRemoveSingleAttribute() {
        String html = "<a href='#'>Link</a>";
        Document doc = Jsoup.parse(html);
        Element a = doc.select("a").first();
        a.removeAttr("href");
        assertEquals("<a>Link</a>", a.outerHtml());
    }

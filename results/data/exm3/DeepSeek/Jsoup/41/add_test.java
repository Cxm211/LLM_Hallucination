// org/jsoup/nodes/ElementTest.java
@Test
    public void testEqualsNoAttributes() {
        String html = "<span>Hello</span><span>Hello</span>";
        Document doc = Jsoup.parse(html);
        Elements spans = doc.select("span");
        assertEquals(2, spans.size());
        assertEquals(spans.get(0), spans.get(1));
    }

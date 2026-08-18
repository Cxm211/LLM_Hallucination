// org/jsoup/nodes/ElementTest.java
@Test
    public void testEqualsNestedChildren() {
        String html = "<div><p>One</p></div><div><p>One</p></div>";
        Document doc = Jsoup.parse(html);
        Elements divs = doc.select("div");
        assertEquals(2, divs.size());
        assertEquals(divs.get(0), divs.get(1));
    }

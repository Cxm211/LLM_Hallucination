// org/jsoup/select/SelectorTest.java::matchTextWithPositionFilters
@Test public void matchTextWithPositionFilters() {
        String html = "<p>A<br>B<br>C<br>D</p>";
        Document doc = Jsoup.parse(html);

        Elements els = doc.select("p:matchText:lt(2)");
        assertEquals(2, els.size());
        assertEquals("A", els.get(0).text());
        assertEquals("B", els.get(1).text());
    }
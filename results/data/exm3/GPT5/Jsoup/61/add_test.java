// org/jsoup/select/ElementsTest.java::hasClassCaseInsensitive
@Test public void hasClassNoAttributeReturnsFalse() {
        Elements els = Jsoup.parse("<p>One <p class=Two>Two").select("p");
        Element noClass = els.get(0);
        Element hasClass = els.get(1);
        assertFalse(noClass.hasClass("foo"));
        assertTrue(hasClass.hasClass("two"));
    }
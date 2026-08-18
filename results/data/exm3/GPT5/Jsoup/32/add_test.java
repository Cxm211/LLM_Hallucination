// org/jsoup/nodes/ElementTest.java::testCloneNoClassnamesIndependence
@Test public void testCloneNoClassnamesIndependence() {
        Document doc = Jsoup.parse("<div></div>");
        Element div = doc.selectFirst("div");
        Set<String> classes = div.classNames();
        assertEquals(0, classes.size());

        Element copy = div.clone();
        Set<String> copyClasses = copy.classNames();
        copyClasses.add("one");

        assertFalse(classes.contains("one"));
        assertTrue(copyClasses.contains("one"));
    }
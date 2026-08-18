// org/jsoup/nodes/ElementTest.java
@Test public void testSiblingElementsForAllChildren() {
    Document doc = Jsoup.parse("<div><p>One</p><p>Two</p><p>Three</p></div>");
    Element div = doc.select("div").first();
    List<Element> children = div.children();
    for (int i = 0; i < children.size(); i++) {
        Element p = children.get(i);
        Elements siblings = p.siblingElements();
        assertEquals(2, siblings.size());
        assertFalse(siblings.contains(p));
        int index = 0;
        for (int j = 0; j < children.size(); j++) {
            if (j != i) {
                assertEquals(children.get(j), siblings.get(index));
                index++;
            }
        }
    }
    Element parent2 = Jsoup.parse("<div><p>Only</p></div>").select("div").first();
    Element onlyChild = parent2.children().first();
    assertEquals(0, onlyChild.siblingElements().size());
    assertNull(onlyChild.nextElementSibling());
    assertNull(onlyChild.previousElementSibling());
}

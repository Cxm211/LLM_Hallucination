// org/jsoup/select/SelectorTest.java
@Test public void testGeneralSiblingCombinatorWithAttrContainingTilde() {
    Document doc = Jsoup.parse("<div></div><span attr=\"a~b\">Hello</span>");
    Elements els = doc.select("div ~ span[attr=\"a~b\"]");
    assertEquals(1, els.size());
    assertEquals("Hello", els.text());
}

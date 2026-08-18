// org/jsoup/select/SelectorTest.java
@Test public void testChildCombinatorWithAttrContainingGt() {
    Document doc = Jsoup.parse("<div><span attr=\"a>b\">Hello</span></div>");
    Elements els = doc.select("div > span[attr=\"a>b\"]");
    assertEquals(1, els.size());
    assertEquals("Hello", els.text());
}

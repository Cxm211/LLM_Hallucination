// org/jsoup/nodes/ElementTest.java
@Test public void testDoesNotKeepPreTextAtDepthSix() {
    String h = "<pre><div><div><div><div><div><b>code\n\ncode</b></div></div></div></div></div></pre>";
    Document doc = Jsoup.parse(h);
    assertEquals("code code", doc.text());
}
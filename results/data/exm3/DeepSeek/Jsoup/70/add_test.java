// org/jsoup/nodes/ElementTest.java
@Test public void testKeepsPreTextAtDepth2() {
    String h = "<pre><span><b>code\n\ncode</b></span></pre>";
    Document doc = Jsoup.parse(h);
    assertEquals("code\n\ncode", doc.text());
    assertEquals("<pre><span><b>code\n\ncode</b></span></pre>", doc.body().html());
}

// org/jsoup/nodes/ElementTest.java
@Test public void testKeepsPreTextAtDepth5() {
    String h = "<pre><div><div><div><div><b>code\n\ncode</b></div></div></div></div></pre>";
    Document doc = Jsoup.parse(h);
    assertEquals("code\n\ncode", doc.text());
    assertEquals("<pre><div><div><div><div><b>code\n\ncode</b></div></div></div></div></pre>", doc.body().html());
}

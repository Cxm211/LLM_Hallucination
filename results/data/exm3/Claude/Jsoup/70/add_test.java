// org/jsoup/nodes/ElementTest.java
@Test public void testKeepsPreTextAtDepthFive() {
    String h = "<pre><div><div><div><div><b>code\n\ncode</b></div></div></div></div></pre>";
    Document doc = Jsoup.parse(h);
    assertEquals("code\n\ncode", doc.text());
    assertEquals("<pre>\n <div>\n  <div>\n   <div>\n    <div>\n     <b>code\n\ncode</b>\n    </div>\n   </div>\n  </div>\n </div>\n</pre>", doc.body().html());
}
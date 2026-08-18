// org/jsoup/nodes/DocumentTest.java
@Test public void testNormalisesMultipleHeads() {
    Document doc = Jsoup.parse("<html><head><title>First</title></head><head><meta charset='utf-8'></head><body><p>Content</p></body></html>");
    assertEquals("<html><head><title>First</title><meta charset=\"utf-8\"></head><body><p>Content</p></body></html>", TextUtil.stripNewlines(doc.html()));
}
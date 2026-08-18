// org/jsoup/helper/DataUtilTest.java
@Test
public void supportsUTF8BOM() throws IOException {
    File in = getFile("/bomtests/bom_utf8.html");
    Document doc = Jsoup.parse(in, null, "http://example.com");
    assertTrue(doc.title().contains("UTF-8"));
    assertTrue(doc.text().contains("가각갂갃간갅"));
}
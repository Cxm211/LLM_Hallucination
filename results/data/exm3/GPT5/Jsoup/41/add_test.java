// org/jsoup/nodes/ElementTest.java
@Test
public void testEqualsAttributeOrder() {
    String a = "<p class=one id=x>One</p>";
    String b = "<p id=x class=one>One</p>";

    Document da = Jsoup.parse(a);
    Document db = Jsoup.parse(b);

    Element ea = da.selectFirst("p");
    Element eb = db.selectFirst("p");

    assertEquals(ea, eb);
    assertEquals(ea.hashCode(), eb.hashCode());
}

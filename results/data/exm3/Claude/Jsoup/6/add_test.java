// org/jsoup/nodes/EntitiesTest.java
@Test public void unescapeHighUnicodeValue() {
    String escaped = "&#x10000;";
    String original = "&#x10000;";
    assertEquals(original, Entities.unescape(escaped));
}
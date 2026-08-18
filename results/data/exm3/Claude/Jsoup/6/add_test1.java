// org/jsoup/nodes/EntitiesTest.java
@Test public void unescapeInvalidCharValue() {
    String escaped = "&#-1;";
    String original = "&#-1;";
    assertEquals(original, Entities.unescape(escaped));
}
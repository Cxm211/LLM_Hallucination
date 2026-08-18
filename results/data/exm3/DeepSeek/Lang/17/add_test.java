// org/apache/commons/lang3/StringEscapeUtilsTest.java
public void testLang721() {
    String input = "A" + "\ud842\udfb7" + "B";
    String escaped = StringEscapeUtils.escapeXml(input);
    assertEquals(input, escaped);
}

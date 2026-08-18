// org/apache/commons/lang3/StringEscapeUtilsTest.java
public void testLang720_SupplementaryAtEnd() {
    String input = new StringBuilder("A").append("\ud842\udfb7").toString();
    String escaped = StringEscapeUtils.escapeXml(input);
    assertEquals(input, escaped);
}
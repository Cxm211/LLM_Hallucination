// org/apache/commons/lang3/StringEscapeUtilsTest.java
public void testLang720_MultipleSupplementaryCharacters() {
    String input = new StringBuilder("\ud842\udfb7").append("\ud842\udfb7").toString();
    String escaped = StringEscapeUtils.escapeXml(input);
    assertEquals(input, escaped);
}
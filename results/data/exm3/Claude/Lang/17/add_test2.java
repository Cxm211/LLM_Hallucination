// org/apache/commons/lang3/StringEscapeUtilsTest.java
public void testLang720_MixedCharacters() {
    String input = new StringBuilder("A").append("\ud842\udfb7").append("B").append("\ud842\udfb7").append("C").toString();
    String escaped = StringEscapeUtils.escapeXml(input);
    assertEquals(input, escaped);
}
// org/apache/commons/lang3/text/translate/NumericEntityUnescaperTest.java
public void testInvalidCodePoint() {
    NumericEntityUnescaper neu = new NumericEntityUnescaper();
    String input1 = "&#1114112;";
    String result1 = neu.translate(input1);
    assertEquals("Should not unescape invalid code point 1114112", "&#1114112;", result1);

    String input2 = "&#xFFFFFF;";
    String result2 = neu.translate(input2);
    assertEquals("Should not unescape invalid hex code point", "&#xFFFFFF;", result2);
}
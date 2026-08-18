// org/apache/commons/lang3/text/translate/NumericEntityUnescaperTest.java
public void testOutOfBoundsProtection() {
    NumericEntityUnescaper neu = new NumericEntityUnescaper();
    String input1 = "&";
    String result1 = neu.translate(input1);
    assertEquals("Should handle single & without error", "&", result1);

    String input2 = "&#";
    String result2 = neu.translate(input2);
    assertEquals("Should handle &# without semicolon", "&#", result2);

    String input3 = "&#x";
    String result3 = neu.translate(input3);
    assertEquals("Should handle &#x without semicolon", "&#x", result3);

    String input4 = "&#65";
    String result4 = neu.translate(input4);
    assertEquals("Should handle &#65 without semicolon", "&#65", result4);
}
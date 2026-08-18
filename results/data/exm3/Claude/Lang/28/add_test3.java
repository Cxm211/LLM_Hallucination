// org/apache/commons/lang3/text/translate/NumericEntityUnescaperTest.java
public void testEmptyNumericEntity() {
    NumericEntityUnescaper neu = new NumericEntityUnescaper();
    String input1 = "&#;";
    String result1 = neu.translate(input1);
    assertEquals("Should handle empty numeric entity", "&#;", result1);

    String input2 = "&#x;";
    String result2 = neu.translate(input2);
    assertEquals("Should handle empty hex entity", "&#x;", result2);
}
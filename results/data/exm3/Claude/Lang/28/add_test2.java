// org/apache/commons/lang3/text/translate/NumericEntityUnescaperTest.java
public void testHexadecimalEntities() {
    NumericEntityUnescaper neu = new NumericEntityUnescaper();
    String input1 = "&#x41;";
    String expected1 = "A";
    String result1 = neu.translate(input1);
    assertEquals("Failed to unescape hex entity &#x41;", expected1, result1);

    String input2 = "&#X41;";
    String expected2 = "A";
    String result2 = neu.translate(input2);
    assertEquals("Failed to unescape hex entity &#X41;", expected2, result2);
}
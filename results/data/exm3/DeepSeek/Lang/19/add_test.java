// org/apache/commons/lang3/text/translate/NumericEntityUnescaperTest.java
public void testDecimalUnfinishedEntity() {
    NumericEntityUnescaper neu = new NumericEntityUnescaper();
    String input = "Test &#65 is A";
    String expected = "Test A is A";
    String result = neu.translate(input);
    assertEquals("Failed to translate decimal entity without semicolon", expected, result);
}

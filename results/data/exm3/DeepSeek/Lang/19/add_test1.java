// org/apache/commons/lang3/text/translate/NumericEntityUnescaperTest.java
public void testLargeHexUnfinishedEntity() {
    NumericEntityUnescaper neu = new NumericEntityUnescaper();
    String input = "&#x10000 is high";
    String expected = new String(Character.toChars(0x10000)) + " is high";
    String result = neu.translate(input);
    assertEquals("Failed to translate large hex entity without semicolon", expected, result);
}

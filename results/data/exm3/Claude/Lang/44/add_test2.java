// org/apache/commons/lang/NumberUtilsTest.java
public void testLang457Additional3() {
    String input = ".";
    try {
        NumberUtils.createNumber(input);
        fail("NumberFormatException was expected for " + input);
    } catch (NumberFormatException e) {
        // expected
    }
}
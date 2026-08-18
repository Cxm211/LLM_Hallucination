// org/apache/commons/lang/NumberUtilsTest.java
public void testLang457Additional2() {
    String[] validInputs = new String[] { "123L", "-456l", "1.5f", "2.5F", "3.5d", "4.5D" };
    for(int i=0; i<validInputs.length; i++) {
        try {
            Number result = NumberUtils.createNumber(validInputs[i]);
            assertNotNull("Expected valid number for " + validInputs[i], result);
        } catch (NumberFormatException e) {
            fail("NumberFormatException was not expected for " + validInputs[i]);
        }
    }
}
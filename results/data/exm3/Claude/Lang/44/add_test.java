// org/apache/commons/lang/NumberUtilsTest.java
public void testLang457Additional1() {
    String[] badInputs = new String[] { "d", "D", "123x", "0.5g", "-456z" };
    for(int i=0; i<badInputs.length; i++) {
        try {
            NumberUtils.createNumber(badInputs[i]);
            fail("NumberFormatException was expected for " + badInputs[i]);
        } catch (NumberFormatException e) {
            // expected
        }
    }
}
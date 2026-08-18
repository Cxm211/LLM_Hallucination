// org/apache/commons/lang/NumberUtilsTest.java::testLang457_upperL
public void testLang457_upperL() {
        try {
            NumberUtils.createNumber("L");
            fail("NumberFormatException was expected for L");
        } catch (NumberFormatException e) {
            // expected
        }
    }
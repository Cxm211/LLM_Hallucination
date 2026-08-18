// org/apache/commons/lang3/math/NumberUtilsTest.java::testIsNumber
public void testIsNumber_plusSign() {
        String val = "+12345";
        assertTrue("isNumber(String) plus sign failed", NumberUtils.isNumber(val));
    }
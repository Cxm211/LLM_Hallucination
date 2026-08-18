// org/apache/commons/lang3/math/NumberUtilsTest.java
@Test(expected = NumberFormatException.class)
    public void testCreateNumberInvalidBothExponentMarkersWithSuffix() {
        NumberUtils.createNumber("1.0e2E3f");
    }

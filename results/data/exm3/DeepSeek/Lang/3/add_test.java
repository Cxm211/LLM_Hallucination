// org/apache/commons/lang3/math/NumberUtilsTest.java
@Test(expected = NumberFormatException.class)
    public void testCreateNumberInvalidBothExponentMarkers() {
        NumberUtils.createNumber("1e2E3");
    }

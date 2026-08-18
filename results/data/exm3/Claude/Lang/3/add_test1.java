// org/apache/commons/lang3/math/NumberUtilsTest.java
@Test
    public void testCreateNumberEdgeCasesInfinity() {
        String exceedsFloat = "3.5e+38";
        String exceedsDouble = "1.8e+308";
        String zeroWithExp = "0.0e10";
        
        assertTrue(NumberUtils.createNumber(exceedsFloat) instanceof Double);
        assertTrue(NumberUtils.createNumber(exceedsDouble) instanceof BigDecimal);
        assertTrue(NumberUtils.createNumber(zeroWithExp) instanceof Float);
    }
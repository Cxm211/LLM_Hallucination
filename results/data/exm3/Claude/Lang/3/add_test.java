// org/apache/commons/lang3/math/NumberUtilsTest.java
@Test
    public void testCreateNumberWithTypeSuffixPrecisionLoss() {
        String floatSuffix = "1.23f";
        String doubleSuffix = "3.40282354e+38d";
        String bigDecimalFallthrough = "1.797693134862315759e+308f";
        
        assertTrue(NumberUtils.createNumber(floatSuffix) instanceof Float);
        assertTrue(NumberUtils.createNumber(doubleSuffix) instanceof Double);
        assertTrue(NumberUtils.createNumber(bigDecimalFallthrough) instanceof BigDecimal);
    }
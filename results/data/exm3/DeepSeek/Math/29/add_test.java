// org/apache/commons/math3/linear/RealVectorAbstractTest.java
@Test
    public void testEbeDivisionAndMultiplicationSpecialCases() {
        // Create a sparse vector with zeros and non-zeros
        OpenMapRealVector sparse = new OpenMapRealVector(new double[]{0, 1, 0, 2, 0, 0, 3});
        // Create a dense vector with special values
        ArrayRealVector special = new ArrayRealVector(new double[]{0, 2, Double.NaN, 0, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY, Double.NaN});
        
        // Test division
        OpenMapRealVector divResult = sparse.ebeDivide(special);
        // Check each entry
        Assert.assertTrue(Double.isNaN(divResult.getEntry(0))); // 0/0 -> NaN
        Assert.assertEquals(0.5, divResult.getEntry(1), 0.0); // 1/2 = 0.5
        Assert.assertTrue(Double.isNaN(divResult.getEntry(2))); // 0/NaN -> NaN
        Assert.assertTrue(Double.isInfinite(divResult.getEntry(3)) && divResult.getEntry(3) > 0); // 2/0 -> +Infinity
        Assert.assertEquals(0.0, divResult.getEntry(4), 0.0); // 0/Infinity -> 0
        Assert.assertEquals(0.0, divResult.getEntry(5), 0.0); // 0/-Infinity -> 0
        Assert.assertTrue(Double.isNaN(divResult.getEntry(6))); // 3/NaN -> NaN
        
        // Test multiplication
        OpenMapRealVector mulResult = sparse.ebeMultiply(special);
        Assert.assertEquals(0.0, mulResult.getEntry(0), 0.0); // 0*0 = 0
        Assert.assertEquals(2.0, mulResult.getEntry(1), 0.0); // 1*2 = 2
        Assert.assertTrue(Double.isNaN(mulResult.getEntry(2))); // 0*NaN -> NaN
        Assert.assertEquals(0.0, mulResult.getEntry(3), 0.0); // 2*0 = 0
        Assert.assertTrue(Double.isNaN(mulResult.getEntry(4))); // 0*Infinity -> NaN
        Assert.assertTrue(Double.isNaN(mulResult.getEntry(5))); // 0*-Infinity -> NaN
        Assert.assertTrue(Double.isNaN(mulResult.getEntry(6))); // 3*NaN -> NaN
    }

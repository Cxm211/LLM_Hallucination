// org/apache/commons/math3/fraction/FractionTest.java
@Test
    public void testMaxDenominatorConstraint() {
        // Test with small maxDenominator to ensure previous convergent is used
        try {
            Fraction f = new Fraction(Math.PI, 0.00001, 10, 100);
            Assert.assertTrue("Denominator should be <= 10", f.getDenominator() <= 10);
            Assert.assertTrue("Should approximate PI reasonably", 
                Math.abs(f.doubleValue() - Math.PI) < 0.01);
        } catch (Exception e) {
            Assert.fail("Should not throw exception: " + e.getMessage());
        }
    }
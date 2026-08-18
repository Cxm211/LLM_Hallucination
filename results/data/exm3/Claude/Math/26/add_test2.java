// org/apache/commons/math3/fraction/FractionTest.java
@Test
    public void testSmallPositiveValue() {
        // Test small positive value that might cause issues
        try {
            Fraction f = new Fraction(0.00001, 1.0e-10, 10000, 100);
            Assert.assertTrue("Should handle small positive value", f.getNumerator() > 0);
            Assert.assertTrue("Denominator should be reasonable", f.getDenominator() <= 10000);
        } catch (Exception e) {
            Assert.fail("Should not throw exception: " + e.getMessage());
        }
    }
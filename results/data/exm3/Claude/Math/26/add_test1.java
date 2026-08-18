// org/apache/commons/math3/fraction/FractionTest.java
@Test
    public void testEpsilonConvergence() {
        // Test that epsilon constraint is respected
        try {
            Fraction f = new Fraction(0.333333, 0.001, 1000, 100);
            Assert.assertTrue("Should converge within epsilon", 
                Math.abs(f.doubleValue() - 0.333333) <= 0.001);
        } catch (Exception e) {
            Assert.fail("Should not throw exception: " + e.getMessage());
        }
    }
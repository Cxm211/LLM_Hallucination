// org/apache/commons/math3/util/FastMathTest.java
@Test
    public void testPowNegativeBaseAndZero() {
        final double twoPower52 = 4503599627370496.0;
        final double twoPower53 = 9007199254740992.0;
        
        // Test cases for negative base large y
        double yOdd = twoPower52 + 1; // odd integer in (2^52, 2^53)
        Assert.assertEquals(Math.pow(-1, yOdd), FastMath.pow(-1, yOdd), 0.0);
        Assert.assertEquals(Math.pow(-1, -yOdd), FastMath.pow(-1, -yOdd), 0.0);
        
        double yNonInt = twoPower52 + 0.5; // non-integer >= 2^52
        Assert.assertTrue(Double.isNaN(Math.pow(-1, yNonInt)));
        Assert.assertTrue(Double.isNaN(FastMath.pow(-1, yNonInt)));
        Assert.assertTrue(Double.isNaN(Math.pow(-1, -yNonInt)));
        Assert.assertTrue(Double.isNaN(FastMath.pow(-1, -yNonInt)));
        
        double yEvenLarge = twoPower53; // even integer >= 2^53
        Assert.assertEquals(Math.pow(-1, yEvenLarge), FastMath.pow(-1, yEvenLarge), 0.0);
        
        // Test cases for negative zero
        double posNonInt = 0.5;
        double negNonInt = -0.5;
        Assert.assertTrue(Double.isNaN(Math.pow(-0.0, posNonInt)));
        Assert.assertTrue(Double.isNaN(FastMath.pow(-0.0, posNonInt)));
        Assert.assertTrue(Double.isNaN(Math.pow(-0.0, negNonInt)));
        Assert.assertTrue(Double.isNaN(FastMath.pow(-0.0, negNonInt)));
        
        // Negative zero with odd integer
        Assert.assertEquals(Math.pow(-0.0, 3.0), FastMath.pow(-0.0, 3.0), 0.0);
        Assert.assertEquals(Math.pow(-0.0, -3.0), FastMath.pow(-0.0, -3.0), 0.0);
        // Negative zero with even integer
        Assert.assertEquals(Math.pow(-0.0, 4.0), FastMath.pow(-0.0, 4.0), 0.0);
        Assert.assertEquals(Math.pow(-0.0, -4.0), FastMath.pow(-0.0, -4.0), 0.0);
    }

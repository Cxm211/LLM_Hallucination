// buggy code
    public Dfp multiply(final int x) {
            return multiplyFast(x);
    }

// relevant test
// org.apache.commons.math3.analysis.function.SincTest::testShortcut
   public void testShortcut() {
       final Sinc s = new Sinc();
       final UnivariateFunction f = new UnivariateFunction() {
           public double value(double x) {
               Dfp dfpX = new DfpField(25).newDfp(x);
               return DfpMath.sin(dfpX).divide(dfpX).toDouble();
           }
       };

       for (double x = 1e-30; x < 1e10; x *= 2) {
           final double fX = f.value(x);
           final double sX = s.value(x);
           Assert.assertEquals("x=" + x, fX, sX, 2.0e-16);
       }
   }

// org.apache.commons.math3.analysis.function.SincTest::testCrossings
   public void testCrossings() {
       final Sinc s = new Sinc(true);
       final int numCrossings = 1000;
       final double tol = 2e-16;
       for (int i = 1; i <= numCrossings; i++) {
           Assert.assertEquals("i=" + i, 0, s.value(i), tol);
       }
   }

// org.apache.commons.math3.analysis.function.SincTest::testZero
   public void testZero() {
       final Sinc s = new Sinc();
       Assert.assertEquals(1d, s.value(0), 0);
   }

// org.apache.commons.math3.analysis.function.SincTest::testEuler
   public void testEuler() {
       final Sinc s = new Sinc();
       final double x = 123456.789;
       double prod = 1;
       double xOverPow2 = x / 2;
       while (xOverPow2 > 0) {
           prod *= FastMath.cos(xOverPow2);
           xOverPow2 /= 2;
       }
       Assert.assertEquals(prod, s.value(x), 1e-13);
   }

// org.apache.commons.math3.analysis.function.SincTest::testDerivativeZero
   public void testDerivativeZero() {
       final DerivativeStructure s0 = new Sinc(true).value(new DerivativeStructure(1, 1, 0, 0.0));
       Assert.assertEquals(0, s0.getPartialDerivative(1), 0);
   }

// org.apache.commons.math3.analysis.function.SincTest::testDerivatives1Dot2Unnormalized
   public void testDerivatives1Dot2Unnormalized() {
       DerivativeStructure s = new Sinc(false).value(new DerivativeStructure(1, 5, 0, 1.2));
       Assert.assertEquals( 0.77669923830602195806, s.getPartialDerivative(0), 1.0e-16);
       Assert.assertEquals(-0.34528456985779031701, s.getPartialDerivative(1), 1.0e-16);
       Assert.assertEquals(-0.2012249552097047631,  s.getPartialDerivative(2), 1.0e-16);
       Assert.assertEquals( 0.2010975926270339262,  s.getPartialDerivative(3), 4.0e-16);
       Assert.assertEquals( 0.106373929549242204,   s.getPartialDerivative(4), 1.0e-15);
       Assert.assertEquals(-0.1412599110579478695,  s.getPartialDerivative(5), 3.0e-15);
   }

// org.apache.commons.math3.analysis.function.SincTest::testDerivatives1Dot2Normalized
   public void testDerivatives1Dot2Normalized() {
       DerivativeStructure s = new Sinc(true).value(new DerivativeStructure(1, 5, 0, 1.2));
       Assert.assertEquals(-0.15591488063143983888, s.getPartialDerivative(0), 6.0e-17);
       Assert.assertEquals(-0.54425176145292298767, s.getPartialDerivative(1), 2.0e-16);
       Assert.assertEquals(2.4459044611635856107,   s.getPartialDerivative(2), 9.0e-16);
       Assert.assertEquals(0.5391369206235909586,   s.getPartialDerivative(3), 7.0e-16);
       Assert.assertEquals(-16.984649869728849865,  s.getPartialDerivative(4), 8.0e-15);
       Assert.assertEquals(5.0980327462666316586,   s.getPartialDerivative(5), 9.0e-15);
   }

// org.apache.commons.math3.analysis.function.SincTest::testDerivativeShortcut
   public void testDerivativeShortcut() {
       final Sinc sinc = new Sinc();
       final UnivariateFunction f = new UnivariateFunction() {
               public double value(double x) {
                   Dfp dfpX = new DfpField(25).newDfp(x);
                   return DfpMath.cos(dfpX).subtract(DfpMath.sin(dfpX).divide(dfpX)).divide(dfpX).toDouble();
               }
           };

       for (double x = 1e-30; x < 1e10; x *= 2) {
           final double fX = f.value(x);
           final DerivativeStructure sX = sinc.value(new DerivativeStructure(1, 1, 0, x));
           Assert.assertEquals("x=" + x, fX, sX.getPartialDerivative(1), 3.0e-13);
       }
   }

// org.apache.commons.math3.dfp.BracketingNthOrderBrentSolverDFPTest::testInsufficientOrder3
    public void testInsufficientOrder3() {
        new BracketingNthOrderBrentSolverDFP(relativeAccuracy, absoluteAccuracy,
                                             functionValueAccuracy, 1);
    }

// org.apache.commons.math3.dfp.BracketingNthOrderBrentSolverDFPTest::testConstructorOK
    public void testConstructorOK() {
        BracketingNthOrderBrentSolverDFP solver =
                new BracketingNthOrderBrentSolverDFP(relativeAccuracy, absoluteAccuracy,
                                                     functionValueAccuracy, 2);
        Assert.assertEquals(2, solver.getMaximalOrder());
    }

// org.apache.commons.math3.dfp.BracketingNthOrderBrentSolverDFPTest::testConvergenceOnFunctionAccuracy
    public void testConvergenceOnFunctionAccuracy() {
        BracketingNthOrderBrentSolverDFP solver =
                new BracketingNthOrderBrentSolverDFP(relativeAccuracy, absoluteAccuracy,
                                                     field.newDfp(1.0e-20), 20);
        UnivariateDfpFunction f = new UnivariateDfpFunction() {
            public Dfp value(Dfp x) {
                Dfp one     = field.getOne();
                Dfp oneHalf = one.divide(2);
                Dfp xMo     = x.subtract(one);
                Dfp xMh     = x.subtract(oneHalf);
                Dfp xPh     = x.add(oneHalf);
                Dfp xPo     = x.add(one);
                return xMo.multiply(xMh).multiply(x).multiply(xPh).multiply(xPo);
            }
        };

        Dfp result = solver.solve(20, f, field.newDfp(0.2), field.newDfp(0.9),
                                  field.newDfp(0.4), AllowedSolution.BELOW_SIDE);
        Assert.assertTrue(f.value(result).abs().lessThan(solver.getFunctionValueAccuracy()));
        Assert.assertTrue(f.value(result).negativeOrNull());
        Assert.assertTrue(result.subtract(field.newDfp(0.5)).subtract(solver.getAbsoluteAccuracy()).positiveOrNull());
        result = solver.solve(20, f, field.newDfp(-0.9), field.newDfp(-0.2),
                              field.newDfp(-0.4), AllowedSolution.ABOVE_SIDE);
        Assert.assertTrue(f.value(result).abs().lessThan(solver.getFunctionValueAccuracy()));
        Assert.assertTrue(f.value(result).positiveOrNull());
        Assert.assertTrue(result.add(field.newDfp(0.5)).subtract(solver.getAbsoluteAccuracy()).negativeOrNull());
    }

// org.apache.commons.math3.dfp.BracketingNthOrderBrentSolverDFPTest::testNeta
    public void testNeta() {

        
        
        
        
        for (AllowedSolution allowed : AllowedSolution.values()) {
            check(new UnivariateDfpFunction() {
                public Dfp value(Dfp x) {
                    return DfpMath.sin(x).subtract(x.divide(2));
                }
            }, 200, -2.0, 2.0, allowed);

            check(new UnivariateDfpFunction() {
                public Dfp value(Dfp x) {
                    return DfpMath.pow(x, 5).add(x).subtract(field.newDfp(10000));
                }
            }, 200, -5.0, 10.0, allowed);

            check(new UnivariateDfpFunction() {
                public Dfp value(Dfp x) {
                    return x.sqrt().subtract(field.getOne().divide(x)).subtract(field.newDfp(3));
                }
            }, 200, 0.001, 10.0, allowed);

            check(new UnivariateDfpFunction() {
                public Dfp value(Dfp x) {
                    return DfpMath.exp(x).add(x).subtract(field.newDfp(20));
                }
            }, 200, -5.0, 5.0, allowed);

            check(new UnivariateDfpFunction() {
                public Dfp value(Dfp x) {
                    return DfpMath.log(x).add(x.sqrt()).subtract(field.newDfp(5));
                }
            }, 200, 0.001, 10.0, allowed);

            check(new UnivariateDfpFunction() {
                public Dfp value(Dfp x) {
                    return x.subtract(field.getOne()).multiply(x).multiply(x).subtract(field.getOne());
                }
            }, 200, -0.5, 1.5, allowed);
        }

    }

// org.apache.commons.math3.dfp.DfpDecTest::testRound
    public void testRound()
    {
        field.setRoundingMode(DfpField.RoundingMode.ROUND_HALF_EVEN);

        test(new DfpDec(field, "12345678901234567890"),
             new DfpDec(field, "12345678901234568000"),
             DfpField.FLAG_INEXACT, "Round #1");

        test(new DfpDec(field, "0.12345678901234567890"),
             new DfpDec(field, "0.12345678901234568"),
             DfpField.FLAG_INEXACT, "Round #2");

        test(new DfpDec(field, "0.12345678901234567500"),
             new DfpDec(field, "0.12345678901234568"),
             DfpField.FLAG_INEXACT, "Round #3");

        test(new DfpDec(field, "0.12345678901234568500"),
             new DfpDec(field, "0.12345678901234568"),
             DfpField.FLAG_INEXACT, "Round #4");

        test(new DfpDec(field, "0.12345678901234568501"),
             new DfpDec(field, "0.12345678901234569"),
             DfpField.FLAG_INEXACT, "Round #5");

        test(new DfpDec(field, "0.12345678901234568499"),
             new DfpDec(field, "0.12345678901234568"),
             DfpField.FLAG_INEXACT, "Round #6");

        test(new DfpDec(field, "1.2345678901234567890"),
             new DfpDec(field, "1.2345678901234568"),
             DfpField.FLAG_INEXACT, "Round #7");

        test(new DfpDec(field, "1.2345678901234567500"),
             new DfpDec(field, "1.2345678901234568"),
             DfpField.FLAG_INEXACT, "Round #8");

        test(new DfpDec(field, "1.2345678901234568500"),
             new DfpDec(field, "1.2345678901234568"),
             DfpField.FLAG_INEXACT, "Round #9");

        test(new DfpDec(field, "1.2345678901234568000").add(new DfpDec(field, ".0000000000000000501")),
             new DfpDec(field, "1.2345678901234569"),
             DfpField.FLAG_INEXACT, "Round #10");

        test(new DfpDec(field, "1.2345678901234568499"),
             new DfpDec(field, "1.2345678901234568"),
             DfpField.FLAG_INEXACT, "Round #11");

        test(new DfpDec(field, "12.345678901234567890"),
             new DfpDec(field, "12.345678901234568"),
             DfpField.FLAG_INEXACT, "Round #12");

        test(new DfpDec(field, "12.345678901234567500"),
             new DfpDec(field, "12.345678901234568"),
             DfpField.FLAG_INEXACT, "Round #13");

        test(new DfpDec(field, "12.345678901234568500"),
             new DfpDec(field, "12.345678901234568"),
             DfpField.FLAG_INEXACT, "Round #14");

        test(new DfpDec(field, "12.345678901234568").add(new DfpDec(field, ".000000000000000501")),
             new DfpDec(field, "12.345678901234569"),
             DfpField.FLAG_INEXACT, "Round #15");

        test(new DfpDec(field, "12.345678901234568499"),
             new DfpDec(field, "12.345678901234568"),
             DfpField.FLAG_INEXACT, "Round #16");

        test(new DfpDec(field, "123.45678901234567890"),
             new DfpDec(field, "123.45678901234568"),
             DfpField.FLAG_INEXACT, "Round #17");

        test(new DfpDec(field, "123.45678901234567500"),
             new DfpDec(field, "123.45678901234568"),
             DfpField.FLAG_INEXACT, "Round #18");

        test(new DfpDec(field, "123.45678901234568500"),
             new DfpDec(field, "123.45678901234568"),
             DfpField.FLAG_INEXACT, "Round #19");

        test(new DfpDec(field, "123.456789012345685").add(new DfpDec(field, ".00000000000000501")),
             new DfpDec(field, "123.45678901234569"),
             DfpField.FLAG_INEXACT, "Round #20");

        test(new DfpDec(field, "123.45678901234568499"),
             new DfpDec(field, "123.45678901234568"),
             DfpField.FLAG_INEXACT, "Round #21");

        field.setRoundingMode(DfpField.RoundingMode.ROUND_DOWN);

        
        test(new DfpDec(field, "12345678901234567").add(new DfpDec(field, "0.9")),
             new DfpDec(field, "12345678901234567"),
             DfpField.FLAG_INEXACT, "Round #22");

        test(new DfpDec(field, "12345678901234567").add(new DfpDec(field, "0.99999999")),
             new DfpDec(field, "12345678901234567"),
             DfpField.FLAG_INEXACT, "Round #23");

        test(new DfpDec(field, "-12345678901234567").add(new DfpDec(field, "-0.99999999")),
             new DfpDec(field, "-12345678901234567"),
             DfpField.FLAG_INEXACT, "Round #24");

        field.setRoundingMode(DfpField.RoundingMode.ROUND_UP);

        
        test(new DfpDec(field, "12345678901234567").add(new DfpDec(field, "0.1")),
             new DfpDec(field, "12345678901234568"),
             DfpField.FLAG_INEXACT, "Round #25");

        test(new DfpDec(field, "12345678901234567").add(new DfpDec(field, "0.0001")),
             new DfpDec(field, "12345678901234568"),
             DfpField.FLAG_INEXACT, "Round #26");

        test(new DfpDec(field, "-12345678901234567").add(new DfpDec(field, "-0.1")),
             new DfpDec(field, "-12345678901234568"),
             DfpField.FLAG_INEXACT, "Round #27");

        test(new DfpDec(field, "-12345678901234567").add(new DfpDec(field, "-0.0001")),
             new DfpDec(field, "-12345678901234568"),
             DfpField.FLAG_INEXACT, "Round #28");

        test(new DfpDec(field, "-12345678901234567").add(new DfpDec(field, "0")),
             new DfpDec(field, "-12345678901234567"),
             0, "Round #28.5");

        field.setRoundingMode(DfpField.RoundingMode.ROUND_HALF_UP);

        
        test(new DfpDec(field, "12345678901234567").add(new DfpDec(field, "0.499999999999")),
             new DfpDec(field, "12345678901234567"),
             DfpField.FLAG_INEXACT, "Round #29");

        test(new DfpDec(field, "12345678901234567").add(new DfpDec(field, "0.50000001")),
             new DfpDec(field, "12345678901234568"),
             DfpField.FLAG_INEXACT, "Round #30");

        test(new DfpDec(field, "12345678901234567").add(new DfpDec(field, "0.5")),
             new DfpDec(field, "12345678901234568"),
             DfpField.FLAG_INEXACT, "Round #30.5");

        test(new DfpDec(field, "-12345678901234567").add(new DfpDec(field, "-0.499999999999")),
             new DfpDec(field, "-12345678901234567"),
             DfpField.FLAG_INEXACT, "Round #31");

        test(new DfpDec(field, "-12345678901234567").add(new DfpDec(field, "-0.50000001")),
             new DfpDec(field, "-12345678901234568"),
             DfpField.FLAG_INEXACT, "Round #32");

        field.setRoundingMode(DfpField.RoundingMode.ROUND_HALF_DOWN);

        
        test(new DfpDec(field, "12345678901234567").add(new DfpDec(field, "0.5001")),
             new DfpDec(field, "12345678901234568"),
             DfpField.FLAG_INEXACT, "Round #33");

        test(new DfpDec(field, "12345678901234567").add(new DfpDec(field, "0.5000")),
             new DfpDec(field, "12345678901234567"),
             DfpField.FLAG_INEXACT, "Round #34");

        test(new DfpDec(field, "-12345678901234567").add(new DfpDec(field, "-0.5001")),
             new DfpDec(field, "-12345678901234568"),
             DfpField.FLAG_INEXACT, "Round #35");

        test(new DfpDec(field, "-12345678901234567").add(new DfpDec(field, "-0.6")),
             new DfpDec(field, "-12345678901234568"),
             DfpField.FLAG_INEXACT, "Round #35.5");

        test(new DfpDec(field, "-12345678901234567").add(new DfpDec(field, "-0.5000")),
             new DfpDec(field, "-12345678901234567"),
             DfpField.FLAG_INEXACT, "Round #36");

        field.setRoundingMode(DfpField.RoundingMode.ROUND_HALF_ODD);

        
        test(new DfpDec(field, "12345678901234568").add(new DfpDec(field, "0.5000")),
             new DfpDec(field, "12345678901234569"),
             DfpField.FLAG_INEXACT, "Round #37");

        test(new DfpDec(field, "12345678901234567").add(new DfpDec(field, "0.5000")),
             new DfpDec(field, "12345678901234567"),
             DfpField.FLAG_INEXACT, "Round #38");

        test(new DfpDec(field, "-12345678901234568").add(new DfpDec(field, "-0.5000")),
             new DfpDec(field, "-12345678901234569"),
             DfpField.FLAG_INEXACT, "Round #39");

        test(new DfpDec(field, "-12345678901234567").add(new DfpDec(field, "-0.5000")),
             new DfpDec(field, "-12345678901234567"),
             DfpField.FLAG_INEXACT, "Round #40");

        field.setRoundingMode(DfpField.RoundingMode.ROUND_CEIL);

        
        test(new DfpDec(field, "12345678901234567").add(new DfpDec(field, "0.0001")),
             new DfpDec(field, "12345678901234568"),
             DfpField.FLAG_INEXACT, "Round #41");

        test(new DfpDec(field, "-12345678901234567").add(new DfpDec(field, "-0.9999")),
             new DfpDec(field, "-12345678901234567"),
             DfpField.FLAG_INEXACT, "Round #42");

        field.setRoundingMode(DfpField.RoundingMode.ROUND_FLOOR);

        
        test(new DfpDec(field, "12345678901234567").add(new DfpDec(field, "0.9999")),
             new DfpDec(field, "12345678901234567"),
             DfpField.FLAG_INEXACT, "Round #43");

        test(new DfpDec(field, "-12345678901234567").add(new DfpDec(field, "-0.0001")),
             new DfpDec(field, "-12345678901234568"),
             DfpField.FLAG_INEXACT, "Round #44");

        field.setRoundingMode(DfpField.RoundingMode.ROUND_HALF_EVEN);  
    }

// org.apache.commons.math3.dfp.DfpDecTest::testRoundDecimal10
    public void testRoundDecimal10()
    {
        field.setRoundingMode(DfpField.RoundingMode.ROUND_HALF_EVEN);

        test(new Decimal10(field, "1234567891234567890"),
             new Decimal10(field, "1234567891000000000"),
             DfpField.FLAG_INEXACT, "RoundDecimal10 #1");

        test(new Decimal10(field, "0.1234567891634567890"),
             new Decimal10(field, "0.1234567892"),
             DfpField.FLAG_INEXACT, "RoundDecimal10 #2");

        test(new Decimal10(field, "0.1234567891500000000"),
             new Decimal10(field, "0.1234567892"),
             DfpField.FLAG_INEXACT, "RoundDecimal10 #3");

        test(new Decimal10(field, "0.1234567890500"),
             new Decimal10(field, "0.1234567890"),
             DfpField.FLAG_INEXACT, "RoundDecimal10 #4");

        test(new Decimal10(field, "0.1234567890501"),
             new Decimal10(field, "0.1234567891"),
             DfpField.FLAG_INEXACT, "RoundDecimal10 #5");

        test(new Decimal10(field, "0.1234567890499"),
             new Decimal10(field, "0.1234567890"),
             DfpField.FLAG_INEXACT, "RoundDecimal10 #6");

        test(new Decimal10(field, "1.234567890890"),
             new Decimal10(field, "1.234567891"),
             DfpField.FLAG_INEXACT, "RoundDecimal10 #7");

        test(new Decimal10(field, "1.234567891500"),
             new Decimal10(field, "1.234567892"),
             DfpField.FLAG_INEXACT, "RoundDecimal10 #8");

        test(new Decimal10(field, "1.234567890500"),
             new Decimal10(field, "1.234567890"),
             DfpField.FLAG_INEXACT, "RoundDecimal10 #9");

        test(new Decimal10(field, "1.234567890000").add(new Decimal10(field, ".000000000501")),
             new Decimal10(field, "1.234567891"),
             DfpField.FLAG_INEXACT, "RoundDecimal10 #10");

        test(new Decimal10(field, "1.234567890499"),
             new Decimal10(field, "1.234567890"),
             DfpField.FLAG_INEXACT, "RoundDecimal10 #11");

        test(new Decimal10(field, "12.34567890890"),
             new Decimal10(field, "12.34567891"),
             DfpField.FLAG_INEXACT, "RoundDecimal10 #12");

        test(new Decimal10(field, "12.34567891500"),
             new Decimal10(field, "12.34567892"),
             DfpField.FLAG_INEXACT, "RoundDecimal10 #13");

        test(new Decimal10(field, "12.34567890500"),
             new Decimal10(field, "12.34567890"),
             DfpField.FLAG_INEXACT, "RoundDecimal10 #14");

        test(new Decimal10(field, "12.34567890").add(new Decimal10(field, ".00000000501")),
             new Decimal10(field, "12.34567891"),
             DfpField.FLAG_INEXACT, "RoundDecimal10 #15");

        test(new Decimal10(field, "12.34567890499"),
             new Decimal10(field, "12.34567890"),
             DfpField.FLAG_INEXACT, "RoundDecimal10 #16");

        test(new Decimal10(field, "123.4567890890"),
             new Decimal10(field, "123.4567891"),
             DfpField.FLAG_INEXACT, "RoundDecimal10 #17");

        test(new Decimal10(field, "123.4567891500"),
             new Decimal10(field, "123.4567892"),
             DfpField.FLAG_INEXACT, "RoundDecimal10 #18");

        test(new Decimal10(field, "123.4567890500"),
             new Decimal10(field, "123.4567890"),
             DfpField.FLAG_INEXACT, "RoundDecimal10 #19");

        test(new Decimal10(field, "123.4567890").add(new Decimal10(field, ".0000000501")),
             new Decimal10(field, "123.4567891"),
             DfpField.FLAG_INEXACT, "RoundDecimal10 #20");

        test(new Decimal10(field, "123.4567890499"),
             new Decimal10(field, "123.4567890"),
             DfpField.FLAG_INEXACT, "RoundDecimal10 #21");

        field.setRoundingMode(DfpField.RoundingMode.ROUND_DOWN);

        
        test(new Decimal10(field, "1234567890").add(new Decimal10(field, "0.9")),
             new Decimal10(field, "1234567890"),
             DfpField.FLAG_INEXACT, "RoundDecimal10 #22");

        test(new Decimal10(field, "1234567890").add(new Decimal10(field, "0.99999999")),
             new Decimal10(field, "1234567890"),
             DfpField.FLAG_INEXACT, "RoundDecimal10 #23");

        test(new Decimal10(field, "-1234567890").add(new Decimal10(field, "-0.99999999")),
             new Decimal10(field, "-1234567890"),
             DfpField.FLAG_INEXACT, "RoundDecimal10 #24");

        field.setRoundingMode(DfpField.RoundingMode.ROUND_UP);

        
        test(new Decimal10(field, 1234567890).add(new Decimal10(field, "0.1")),
             new Decimal10(field, 1234567891l),
             DfpField.FLAG_INEXACT, "RoundDecimal10 #25");

        test(new Decimal10(field, "1234567890").add(new Decimal10(field, "0.0001")),
             new Decimal10(field, "1234567891"),
             DfpField.FLAG_INEXACT, "RoundDecimal10 #26");

        test(new Decimal10(field, "-1234567890").add(new Decimal10(field, "-0.1")),
             new Decimal10(field, "-1234567891"),
             DfpField.FLAG_INEXACT, "RoundDecimal10 #27");

        test(new Decimal10(field, "-1234567890").add(new Decimal10(field, "-0.0001")),
             new Decimal10(field, "-1234567891"),
             DfpField.FLAG_INEXACT, "RoundDecimal10 #28");

        test(new Decimal10(field, "-1234567890").add(new Decimal10(field, "0")),
             new Decimal10(field, "-1234567890"),
             0, "RoundDecimal10 #28.5");

        field.setRoundingMode(DfpField.RoundingMode.ROUND_HALF_UP);

        
        test(new Decimal10(field, "1234567890").add(new Decimal10(field, "0.4999999999")),
             new Decimal10(field, "1234567890"),
             DfpField.FLAG_INEXACT, "RoundDecimal10 #29");

        test(new Decimal10(field, "1234567890").add(new Decimal10(field, "0.50000001")),
             new Decimal10(field, "1234567891"),
             DfpField.FLAG_INEXACT, "RoundDecimal10 #30");

        test(new Decimal10(field, "1234567890").add(new Decimal10(field, "0.5")),
             new Decimal10(field, "1234567891"),
             DfpField.FLAG_INEXACT, "RoundDecimal10 #30.5");

        test(new Decimal10(field, "-1234567890").add(new Decimal10(field, "-0.4999999999")),
             new Decimal10(field, "-1234567890"),
             DfpField.FLAG_INEXACT, "RoundDecimal10 #31");

        test(new Decimal10(field, "-1234567890").add(new Decimal10(field, "-0.50000001")),
             new Decimal10(field, "-1234567891"),
             DfpField.FLAG_INEXACT, "RoundDecimal10 #32");

        field.setRoundingMode(DfpField.RoundingMode.ROUND_HALF_DOWN);

        
        test(new Decimal10(field, "1234567890").add(new Decimal10(field, "0.5001")),
             new Decimal10(field, "1234567890"),
             DfpField.FLAG_INEXACT, "RoundDecimal10 #33");

        test(new Decimal10(field, "1234567890").add(new Decimal10(field, "0.5000")),
             new Decimal10(field, "1234567890"),
             DfpField.FLAG_INEXACT, "RoundDecimal10 #34");

        test(new Decimal10(field, "-1234567890").add(new Decimal10(field, "-0.5001")),
             new Decimal10(field, "-1234567890"),
             DfpField.FLAG_INEXACT, "RoundDecimal10 #35");

        test(new Decimal10(field, "-1234567890").add(new Decimal10(field, "-0.6")),
             new Decimal10(field, "-1234567891"),
             DfpField.FLAG_INEXACT, "RoundDecimal10 #35.5");

        test(new Decimal10(field, "-1234567890").add(new Decimal10(field, "-0.5000")),
             new Decimal10(field, "-1234567890"),
             DfpField.FLAG_INEXACT, "RoundDecimal10 #36");

        field.setRoundingMode(DfpField.RoundingMode.ROUND_HALF_ODD);

        
        test(new Decimal10(field, "1234567890").add(new Decimal10(field, "0.5000")),
             new Decimal10(field, "1234567891"),
             DfpField.FLAG_INEXACT, "RoundDecimal10 #37");

        test(new Decimal10(field, "1234567891").add(new Decimal10(field, "0.5000")),
             new Decimal10(field, "1234567891"),
             DfpField.FLAG_INEXACT, "RoundDecimal10 #38");

        test(new Decimal10(field, "-1234567890").add(new Decimal10(field, "-0.5000")),
             new Decimal10(field, "-1234567891"),
             DfpField.FLAG_INEXACT, "RoundDecimal10 #39");

        test(new Decimal10(field, "-1234567891").add(new Decimal10(field, "-0.5000")),
             new Decimal10(field, "-1234567891"),
             DfpField.FLAG_INEXACT, "RoundDecimal10 #40");

        field.setRoundingMode(DfpField.RoundingMode.ROUND_CEIL);

        
        test(new Decimal10(field, "1234567890").add(new Decimal10(field, "0.0001")),
             new Decimal10(field, "1234567891"),
             DfpField.FLAG_INEXACT, "RoundDecimal10 #41");

        test(new Decimal10(field, "-1234567890").add(new Decimal10(field, "-0.9999")),
             new Decimal10(field, "-1234567890"),
             DfpField.FLAG_INEXACT, "RoundDecimal10 #42");

        field.setRoundingMode(DfpField.RoundingMode.ROUND_FLOOR);

        
        test(new Decimal10(field, "1234567890").add(new Decimal10(field, "0.9999")),
             new Decimal10(field, "1234567890"),
             DfpField.FLAG_INEXACT, "RoundDecimal10 #43");

        test(new Decimal10(field, "-1234567890").add(new Decimal10(field, "-0.0001")),
             new Decimal10(field, "-1234567891"),
             DfpField.FLAG_INEXACT, "RoundDecimal10 #44");

        field.setRoundingMode(DfpField.RoundingMode.ROUND_HALF_EVEN);  
    }

// org.apache.commons.math3.dfp.DfpDecTest::testNextAfter
    public void testNextAfter()
    {
        test(new DfpDec(field, 1).nextAfter(pinf),
             new DfpDec(field, "1.0000000000000001"),
             0, "NextAfter #1");

        test(new DfpDec(field, "1.0000000000000001").nextAfter(ninf),
             new DfpDec(field, 1),
             0, "NextAfter #1.5");

        test(new DfpDec(field, 1).nextAfter(ninf),
             new DfpDec(field, "0.99999999999999999"),
             0, "NextAfter #2");

        test(new DfpDec(field, "0.99999999999999999").nextAfter(new DfpDec(field, 2)),
             new DfpDec(field, 1),
             0, "NextAfter #3");

        test(new DfpDec(field, -1).nextAfter(ninf),
             new DfpDec(field, "-1.0000000000000001"),
             0, "NextAfter #4");

        test(new DfpDec(field, -1).nextAfter(pinf),
             new DfpDec(field, "-0.99999999999999999"),
             0, "NextAfter #5");

        test(new DfpDec(field, "-0.99999999999999999").nextAfter(new DfpDec(field, -2)),
             new DfpDec(field, (byte) -1),
             0, "NextAfter #6");

        test(new DfpDec(field, (byte) 2).nextAfter(new DfpDec(field, 2)),
             new DfpDec(field, 2l),
             0, "NextAfter #7");

        test(new DfpDec(field, 0).nextAfter(new DfpDec(field, 0)),
             new DfpDec(field, 0),
             0, "NextAfter #8");

        test(new DfpDec(field, -2).nextAfter(new DfpDec(field, -2)),
             new DfpDec(field, -2),
             0, "NextAfter #9");

        test(new DfpDec(field, 0).nextAfter(new DfpDec(field, 1)),
             new DfpDec(field, "1e-131092"),
             DfpField.FLAG_UNDERFLOW, "NextAfter #10");

        test(new DfpDec(field, 0).nextAfter(new DfpDec(field, -1)),
             new DfpDec(field, "-1e-131092"),
             DfpField.FLAG_UNDERFLOW, "NextAfter #11");

        test(new DfpDec(field, "-1e-131092").nextAfter(pinf),
             new DfpDec(field, "-0"),
             DfpField.FLAG_UNDERFLOW|DfpField.FLAG_INEXACT, "Next After #12");

        test(new DfpDec(field, "1e-131092").nextAfter(ninf), 
             new DfpDec(field, "0"),
             DfpField.FLAG_UNDERFLOW|DfpField.FLAG_INEXACT, "Next After #13");

        test(new DfpDec(field, "9.9999999999999999e131078").nextAfter(pinf),
             pinf,
             DfpField.FLAG_OVERFLOW|DfpField.FLAG_INEXACT, "Next After #14");
    }

// org.apache.commons.math3.dfp.DfpMathTest::testPow
    public void testPow()  
    {
        
        test(DfpMath.pow(factory.newDfp("0"), factory.newDfp("0")),      
             factory.newDfp("1"), 
             0, "pow #1");

        test(DfpMath.pow(factory.newDfp("0"), factory.newDfp("-0")),      
             factory.newDfp("1"), 
             0, "pow #2");

        test(DfpMath.pow(factory.newDfp("2"), factory.newDfp("0")),      
             factory.newDfp("1"), 
             0, "pow #3");

        test(DfpMath.pow(factory.newDfp("-2"), factory.newDfp("-0")),      
             factory.newDfp("1"), 
             0, "pow #4");

        test(DfpMath.pow(pinf, factory.newDfp("-0")),      
             factory.newDfp("1"), 
             0, "pow #5");

        test(DfpMath.pow(pinf, factory.newDfp("0")),
             factory.newDfp("1"), 
             0, "pow #6");

        test(DfpMath.pow(ninf, factory.newDfp("-0")),      
             factory.newDfp("1"), 
             0, "pow #7");

        test(DfpMath.pow(ninf, factory.newDfp("0")),
             factory.newDfp("1"), 
             0, "pow #8");

        test(DfpMath.pow(qnan, factory.newDfp("0")),
             factory.newDfp("1"), 
             0, "pow #8");

        
        test(DfpMath.pow(factory.newDfp("0"), factory.newDfp("1")),
             factory.newDfp("0"), 
             0, "pow #9");

        test(DfpMath.pow(factory.newDfp("-0"), factory.newDfp("1")),      
             factory.newDfp("-0"), 
             0, "pow #10");

        test(DfpMath.pow(factory.newDfp("2"), factory.newDfp("1")),
             factory.newDfp("2"), 
             0, "pow #11");

        test(DfpMath.pow(factory.newDfp("-2"), factory.newDfp("1")),
             factory.newDfp("-2"), 
             0, "pow #12");

        test(DfpMath.pow(pinf, factory.newDfp("1")),      
             pinf, 
             0, "pow #13");

        test(DfpMath.pow(ninf, factory.newDfp("1")),
             ninf, 
             0, "pow #14");

        test(DfpMath.pow(qnan, factory.newDfp("1")),
             qnan, 
             DfpField.FLAG_INVALID, "pow #14.1");

        
        test(DfpMath.pow(factory.newDfp("0"), qnan),
             qnan, 
             DfpField.FLAG_INVALID, "pow #15");

        test(DfpMath.pow(factory.newDfp("-0"), qnan),      
             qnan, 
             DfpField.FLAG_INVALID, "pow #16");

        test(DfpMath.pow(factory.newDfp("2"), qnan),
             qnan, 
             DfpField.FLAG_INVALID, "pow #17");

        test(DfpMath.pow(factory.newDfp("-2"), qnan),
             qnan, 
             DfpField.FLAG_INVALID, "pow #18");

        test(DfpMath.pow(pinf, qnan),      
             qnan, 
             DfpField.FLAG_INVALID, "pow #19");

        test(DfpMath.pow(ninf, qnan),
             qnan, 
             DfpField.FLAG_INVALID, "pow #20");

        test(DfpMath.pow(qnan, qnan),
             qnan, 
             DfpField.FLAG_INVALID, "pow #21");

        
        test(DfpMath.pow(qnan, factory.newDfp("1")),
             qnan, 
             DfpField.FLAG_INVALID, "pow #22");

        test(DfpMath.pow(qnan, factory.newDfp("-1")),      
             qnan,
             DfpField.FLAG_INVALID, "pow #23");

        test(DfpMath.pow(qnan, pinf),
             qnan,
             DfpField.FLAG_INVALID, "pow #24");

        test(DfpMath.pow(qnan, ninf),
             qnan, 
             DfpField.FLAG_INVALID, "pow #25");

        test(DfpMath.pow(qnan, qnan),
             qnan, 
             DfpField.FLAG_INVALID, "pow #26");

        
        test(DfpMath.pow(factory.newDfp("2"), pinf),
             pinf, 
             0, "pow #27");

        test(DfpMath.pow(factory.newDfp("-2"), pinf),      
             pinf,
             0, "pow #28");

        test(DfpMath.pow(pinf, pinf),
             pinf,
             0, "pow #29");

        test(DfpMath.pow(ninf, pinf),
             pinf, 
             0, "pow #30");

        
        test(DfpMath.pow(factory.newDfp("2"), ninf),
             factory.getZero(), 
             0, "pow #31");

        test(DfpMath.pow(factory.newDfp("-2"), ninf),      
             factory.getZero(),
             0, "pow #32");

        test(DfpMath.pow(pinf, ninf),
             factory.getZero(),
             0, "pow #33");

        test(DfpMath.pow(ninf, ninf),
             factory.getZero(), 
             0, "pow #34");

        
        test(DfpMath.pow(factory.newDfp("0.5"), pinf),
             factory.getZero(), 
             0, "pow #35");

        test(DfpMath.pow(factory.newDfp("-0.5"), pinf),      
             factory.getZero(),
             0, "pow #36");

        
        test(DfpMath.pow(factory.newDfp("0.5"), ninf),
             pinf, 
             0, "pow #37");

        test(DfpMath.pow(factory.newDfp("-0.5"), ninf),      
             pinf,
             0, "pow #38");

        
        test(DfpMath.pow(factory.getOne(), pinf),
             qnan, 
             DfpField.FLAG_INVALID, "pow #39");

        test(DfpMath.pow(factory.getOne(), ninf),      
             qnan,
             DfpField.FLAG_INVALID, "pow #40");

        test(DfpMath.pow(factory.newDfp("-1"), pinf),
             qnan, 
             DfpField.FLAG_INVALID, "pow #41");

        test(DfpMath.pow(factory.getOne().negate(), ninf),      
             qnan,
             DfpField.FLAG_INVALID, "pow #42");

        

        test(DfpMath.pow(factory.newDfp("0"), factory.newDfp("1")),
             factory.newDfp("0"),
             0, "pow #43");

        test(DfpMath.pow(factory.newDfp("0"), factory.newDfp("1e30")),
             factory.newDfp("0"),
             0, "pow #44");

        test(DfpMath.pow(factory.newDfp("0"), factory.newDfp("1e-30")),
             factory.newDfp("0"),
             0, "pow #45");

        test(DfpMath.pow(factory.newDfp("0"), pinf),
             factory.newDfp("0"),
             0, "pow #46");

        

        test(DfpMath.pow(factory.newDfp("-0"), factory.newDfp("2")),
             factory.newDfp("0"),
             0, "pow #47");

        test(DfpMath.pow(factory.newDfp("-0"), factory.newDfp("1e30")),
             factory.newDfp("0"),
             0, "pow #48");

        test(DfpMath.pow(factory.newDfp("-0"), factory.newDfp("1e-30")),
             factory.newDfp("0"),
             DfpField.FLAG_INEXACT, "pow #49");

        test(DfpMath.pow(factory.newDfp("-0"), pinf),
             factory.newDfp("0"),
             0, "pow #50");

        

        test(DfpMath.pow(factory.newDfp("0"), factory.newDfp("-1")),
             pinf,
             0, "pow #51");

        test(DfpMath.pow(factory.newDfp("0"), factory.newDfp("-1e30")),
             pinf,
             0, "pow #52");

        test(DfpMath.pow(factory.newDfp("0"), factory.newDfp("-1e-30")),
             pinf,
             0, "pow #53");

        test(DfpMath.pow(factory.newDfp("0"), ninf),
             pinf,
             0, "pow #54");

        

        test(DfpMath.pow(factory.newDfp("-0"), factory.newDfp("-2")),
             pinf,
             0, "pow #55");

        test(DfpMath.pow(factory.newDfp("-0"), factory.newDfp("-1e30")),
             pinf,
             0, "pow #56");

        test(DfpMath.pow(factory.newDfp("-0"), factory.newDfp("-1e-30")),
             pinf,
             DfpField.FLAG_INEXACT, "pow #57");

        test(DfpMath.pow(factory.newDfp("-0"), ninf),
             pinf,
             0, "pow #58");

        
        test(DfpMath.pow(factory.newDfp("-0"), factory.newDfp("-1")),
             ninf,
             DfpField.FLAG_INEXACT, "pow #59");

        test(DfpMath.pow(factory.newDfp("-0"), factory.newDfp("-12345")),
             ninf,
             DfpField.FLAG_INEXACT, "pow #60");

        
        test(DfpMath.pow(factory.newDfp("-0"), factory.newDfp("3")),
             factory.newDfp("-0"),
             DfpField.FLAG_INEXACT, "pow #61");

        test(DfpMath.pow(factory.newDfp("-0"), factory.newDfp("12345")),
             factory.newDfp("-0"),
             DfpField.FLAG_INEXACT, "pow #62");

        
        test(DfpMath.pow(pinf, factory.newDfp("3")),
             pinf,
             0, "pow #63");

        test(DfpMath.pow(pinf, factory.newDfp("1e30")),
             pinf,
             0, "pow #64");

        test(DfpMath.pow(pinf, factory.newDfp("1e-30")),
             pinf,
             0, "pow #65");

        test(DfpMath.pow(pinf, pinf),
             pinf,
             0, "pow #66");

        

        test(DfpMath.pow(pinf, factory.newDfp("-3")),
             factory.getZero(),
             0, "pow #67");

        test(DfpMath.pow(pinf, factory.newDfp("-1e30")),
             factory.getZero(),
             0, "pow #68");

        test(DfpMath.pow(pinf, factory.newDfp("-1e-30")),
             factory.getZero(),
             0, "pow #69");

        test(DfpMath.pow(pinf, ninf),
             factory.getZero(),
             0, "pow #70");

        
        

        test(DfpMath.pow(ninf, factory.newDfp("-2")),
             factory.newDfp("0"),
             0, "pow #71");

        test(DfpMath.pow(ninf, factory.newDfp("-1e30")),
             factory.newDfp("0"),
             0, "pow #72");

        test(DfpMath.pow(ninf, factory.newDfp("-1e-30")),
             factory.newDfp("0"),
             DfpField.FLAG_INEXACT, "pow #73");

        test(DfpMath.pow(ninf, ninf),
             factory.newDfp("0"),
             0, "pow #74");

        

        test(DfpMath.pow(ninf, factory.newDfp("2")),
             pinf,
             0, "pow #75");

        test(DfpMath.pow(ninf, factory.newDfp("1e30")),
             pinf,
             0, "pow #76");

        test(DfpMath.pow(ninf, factory.newDfp("1e-30")),
             pinf,
             DfpField.FLAG_INEXACT, "pow #77");

        test(DfpMath.pow(ninf, pinf),
             pinf,
             0, "pow #78");

        
        test(DfpMath.pow(ninf, factory.newDfp("3")),
             ninf,
             DfpField.FLAG_INEXACT, "pow #79");

        test(DfpMath.pow(ninf, factory.newDfp("12345")),
             ninf,
             DfpField.FLAG_INEXACT, "pow #80");

        
        test(DfpMath.pow(ninf, factory.newDfp("-3")),
             factory.newDfp("-0"),
             DfpField.FLAG_INEXACT, "pow #81");

        test(DfpMath.pow(ninf, factory.newDfp("-12345")),
             factory.newDfp("-0"),
             DfpField.FLAG_INEXACT, "pow #82");

        
        test(DfpMath.pow(factory.newDfp("-2"), factory.newDfp("3")),
             factory.newDfp("-8"),
             DfpField.FLAG_INEXACT, "pow #83");

        test(DfpMath.pow(factory.newDfp("-2"), factory.newDfp("16")),
             factory.newDfp("65536"),
             0, "pow #84");

        test(DfpMath.pow(factory.newDfp("-2"), factory.newDfp("-3")),
             factory.newDfp("-0.125"),
             DfpField.FLAG_INEXACT, "pow #85");

        test(DfpMath.pow(factory.newDfp("-2"), factory.newDfp("-4")),
             factory.newDfp("0.0625"),
             0, "pow #86");

        

        test(DfpMath.pow(factory.newDfp("-2"), factory.newDfp("-4.1")),
             qnan,
             DfpField.FLAG_INVALID|DfpField.FLAG_INEXACT, "pow #87");

        
        test(DfpMath.pow(factory.newDfp("2"),factory.newDfp("1.5")),
             factory.newDfp("2.8284271247461901"), 
             DfpField.FLAG_INEXACT, "pow #88");
    }

// org.apache.commons.math3.dfp.DfpMathTest::testSin
    public void testSin()
    {
        test(DfpMath.sin(pinf),
             nan,
             DfpField.FLAG_INVALID|DfpField.FLAG_INEXACT, "sin #1");

        test(DfpMath.sin(nan),
             nan,
             DfpField.FLAG_INVALID|DfpField.FLAG_INEXACT, "sin #2");

        test(DfpMath.sin(factory.getZero()),
             factory.getZero(),
             DfpField.FLAG_INEXACT, "sin #3");

        test(DfpMath.sin(factory.getPi()),
             factory.getZero(),
             DfpField.FLAG_INEXACT, "sin #4");

        test(DfpMath.sin(factory.getPi().negate()),
             factory.newDfp("-0"),
             DfpField.FLAG_INEXACT, "sin #5");

        test(DfpMath.sin(factory.getPi().multiply(2)),
             factory.getZero(),
             DfpField.FLAG_INEXACT, "sin #6");

        test(DfpMath.sin(factory.getPi().divide(2)),
             factory.getOne(),
             DfpField.FLAG_INEXACT, "sin #7");

        test(DfpMath.sin(factory.getPi().divide(2).negate()),
             factory.getOne().negate(),
             DfpField.FLAG_INEXACT, "sin #8");

        test(DfpMath.sin(DfpMath.atan(factory.getOne())),  
             factory.newDfp("0.5").sqrt(),
             DfpField.FLAG_INEXACT, "sin #9");

        test(DfpMath.sin(DfpMath.atan(factory.getOne())).negate(),  
             factory.newDfp("0.5").sqrt().negate(),
             DfpField.FLAG_INEXACT, "sin #10");

        test(DfpMath.sin(DfpMath.atan(factory.getOne())).negate(),  
             factory.newDfp("0.5").sqrt().negate(),
             DfpField.FLAG_INEXACT, "sin #11");

        test(DfpMath.sin(factory.newDfp("0.1")),
             factory.newDfp("0.0998334166468281523"),
             DfpField.FLAG_INEXACT, "sin #12");

        test(DfpMath.sin(factory.newDfp("0.2")),
             factory.newDfp("0.19866933079506121546"),
             DfpField.FLAG_INEXACT, "sin #13");

        test(DfpMath.sin(factory.newDfp("0.3")),
             factory.newDfp("0.2955202066613395751"),
             DfpField.FLAG_INEXACT, "sin #14");

        test(DfpMath.sin(factory.newDfp("0.4")),
             factory.newDfp("0.38941834230865049166"),
             DfpField.FLAG_INEXACT, "sin #15");

        test(DfpMath.sin(factory.newDfp("0.5")),
             factory.newDfp("0.47942553860420300026"),  
             DfpField.FLAG_INEXACT, "sin #16");

        test(DfpMath.sin(factory.newDfp("0.6")),
             factory.newDfp("0.56464247339503535721"),  
             DfpField.FLAG_INEXACT, "sin #17");

        test(DfpMath.sin(factory.newDfp("0.7")),
             factory.newDfp("0.64421768723769105367"),  
             DfpField.FLAG_INEXACT, "sin #18");

        test(DfpMath.sin(factory.newDfp("0.8")),        
             factory.newDfp("0.71735609089952276163"),
             DfpField.FLAG_INEXACT, "sin #19");

        test(DfpMath.sin(factory.newDfp("0.9")),        
             factory.newDfp("0.78332690962748338847"),
             DfpField.FLAG_INEXACT, "sin #20");

        test(DfpMath.sin(factory.newDfp("1.0")),
             factory.newDfp("0.84147098480789650666"),
             DfpField.FLAG_INEXACT, "sin #21");

        test(DfpMath.sin(factory.newDfp("1.1")),
             factory.newDfp("0.89120736006143533995"),
             DfpField.FLAG_INEXACT, "sin #22");

        test(DfpMath.sin(factory.newDfp("1.2")),
             factory.newDfp("0.93203908596722634968"),
             DfpField.FLAG_INEXACT, "sin #23");

        test(DfpMath.sin(factory.newDfp("1.3")),
             factory.newDfp("0.9635581854171929647"),
             DfpField.FLAG_INEXACT, "sin #24");

        test(DfpMath.sin(factory.newDfp("1.4")),
             factory.newDfp("0.98544972998846018066"),
             DfpField.FLAG_INEXACT, "sin #25");

        test(DfpMath.sin(factory.newDfp("1.5")),
             factory.newDfp("0.99749498660405443096"),
             DfpField.FLAG_INEXACT, "sin #26");

        test(DfpMath.sin(factory.newDfp("1.6")),
             factory.newDfp("0.99957360304150516323"),
             DfpField.FLAG_INEXACT, "sin #27");
    }

// org.apache.commons.math3.dfp.DfpTest::testByteConstructor
    public void testByteConstructor() {
        Assert.assertEquals("0.", new Dfp(field, (byte) 0).toString());
        Assert.assertEquals("1.", new Dfp(field, (byte) 1).toString());
        Assert.assertEquals("-1.", new Dfp(field, (byte) -1).toString());
        Assert.assertEquals("-128.", new Dfp(field, Byte.MIN_VALUE).toString());
        Assert.assertEquals("127.", new Dfp(field, Byte.MAX_VALUE).toString());
    }

// org.apache.commons.math3.dfp.DfpTest::testIntConstructor
    public void testIntConstructor() {
        Assert.assertEquals("0.", new Dfp(field, 0).toString());
        Assert.assertEquals("1.", new Dfp(field, 1).toString());
        Assert.assertEquals("-1.", new Dfp(field, -1).toString());
        Assert.assertEquals("1234567890.", new Dfp(field, 1234567890).toString());
        Assert.assertEquals("-1234567890.", new Dfp(field, -1234567890).toString());
        Assert.assertEquals("-2147483648.", new Dfp(field, Integer.MIN_VALUE).toString());
        Assert.assertEquals("2147483647.", new Dfp(field, Integer.MAX_VALUE).toString());
    }

// org.apache.commons.math3.dfp.DfpTest::testLongConstructor
    public void testLongConstructor() {
        Assert.assertEquals("0.", new Dfp(field, 0l).toString());
        Assert.assertEquals("1.", new Dfp(field, 1l).toString());
        Assert.assertEquals("-1.", new Dfp(field, -1l).toString());
        Assert.assertEquals("1234567890.", new Dfp(field, 1234567890l).toString());
        Assert.assertEquals("-1234567890.", new Dfp(field, -1234567890l).toString());
        Assert.assertEquals("-9223372036854775808.", new Dfp(field, Long.MIN_VALUE).toString());
        Assert.assertEquals("9223372036854775807.", new Dfp(field, Long.MAX_VALUE).toString());
    }

// org.apache.commons.math3.dfp.DfpTest::testAdd
    public void testAdd()
    {
        test(field.newDfp("1").add(field.newDfp("1")),      
             field.newDfp("2"),
             0, "Add #1");

        test(field.newDfp("1").add(field.newDfp("-1")),     
             field.newDfp("0"),
             0, "Add #2");

        test(field.newDfp("-1").add(field.newDfp("1")),     
             field.newDfp("0"),
             0, "Add #3");

        test(field.newDfp("-1").add(field.newDfp("-1")),     
             field.newDfp("-2"),
             0, "Add #4");

        

        test(field.newDfp("1").add(field.newDfp("1e-16")),     
             field.newDfp("1.0000000000000001"),
             0, "Add #5");

        test(field.newDfp("1").add(field.newDfp("1e-17")),     
             field.newDfp("1"),
             DfpField.FLAG_INEXACT, "Add #6");

        test(field.newDfp("0.90999999999999999999").add(field.newDfp("0.1")),     
             field.newDfp("1.01"),
             DfpField.FLAG_INEXACT, "Add #7");

        test(field.newDfp(".10000000000000005000").add(field.newDfp(".9")),     
             field.newDfp("1."),
             DfpField.FLAG_INEXACT, "Add #8");

        test(field.newDfp(".10000000000000015000").add(field.newDfp(".9")),     
             field.newDfp("1.0000000000000002"),
             DfpField.FLAG_INEXACT, "Add #9");

        test(field.newDfp(".10000000000000014999").add(field.newDfp(".9")),     
             field.newDfp("1.0000000000000001"),
             DfpField.FLAG_INEXACT, "Add #10");

        test(field.newDfp(".10000000000000015001").add(field.newDfp(".9")),     
             field.newDfp("1.0000000000000002"),
             DfpField.FLAG_INEXACT, "Add #11");

        test(field.newDfp(".11111111111111111111").add(field.newDfp("11.1111111111111111")), 
             field.newDfp("11.22222222222222222222"),
             DfpField.FLAG_INEXACT, "Add #12");

        test(field.newDfp(".11111111111111111111").add(field.newDfp("1111111111111111.1111")), 
             field.newDfp("1111111111111111.2222"),
             DfpField.FLAG_INEXACT, "Add #13");

        test(field.newDfp(".11111111111111111111").add(field.newDfp("11111111111111111111")), 
             field.newDfp("11111111111111111111"),
             DfpField.FLAG_INEXACT, "Add #14");

        test(field.newDfp("9.9999999999999999999e131071").add(field.newDfp("-1e131052")), 
             field.newDfp("9.9999999999999999998e131071"),
             0, "Add #15");

        test(field.newDfp("9.9999999999999999999e131071").add(field.newDfp("1e131052")), 
             pinf,
             DfpField.FLAG_OVERFLOW, "Add #16");

        test(field.newDfp("-9.9999999999999999999e131071").add(field.newDfp("-1e131052")), 
             ninf,
             DfpField.FLAG_OVERFLOW, "Add #17");

        test(field.newDfp("-9.9999999999999999999e131071").add(field.newDfp("1e131052")), 
             field.newDfp("-9.9999999999999999998e131071"),
             0, "Add #18");

        test(field.newDfp("1e-131072").add(field.newDfp("1e-131072")), 
             field.newDfp("2e-131072"),
             0, "Add #19");

        test(field.newDfp("1.0000000000000001e-131057").add(field.newDfp("-1e-131057")), 
             field.newDfp("1e-131073"),
             DfpField.FLAG_UNDERFLOW, "Add #20");

        test(field.newDfp("1.1e-131072").add(field.newDfp("-1e-131072")), 
             field.newDfp("1e-131073"),
             DfpField.FLAG_UNDERFLOW, "Add #21");

        test(field.newDfp("1.0000000000000001e-131072").add(field.newDfp("-1e-131072")), 
             field.newDfp("1e-131088"),
             DfpField.FLAG_UNDERFLOW, "Add #22");

        test(field.newDfp("1.0000000000000001e-131078").add(field.newDfp("-1e-131078")), 
             field.newDfp("0"),
             DfpField.FLAG_UNDERFLOW, "Add #23");

        test(field.newDfp("1.0").add(field.newDfp("-1e-20")), 
             field.newDfp("0.99999999999999999999"),
             0, "Add #23.1");

        test(field.newDfp("-0.99999999999999999999").add(field.newDfp("1")), 
             field.newDfp("0.00000000000000000001"),
             0, "Add #23.2");

        test(field.newDfp("1").add(field.newDfp("0")), 
             field.newDfp("1"),
             0, "Add #24");

        test(field.newDfp("0").add(field.newDfp("0")), 
             field.newDfp("0"),
             0, "Add #25");

        test(field.newDfp("-0").add(field.newDfp("0")), 
             field.newDfp("0"),
             0, "Add #26");

        test(field.newDfp("0").add(field.newDfp("-0")), 
             field.newDfp("0"),
             0, "Add #27");

        test(field.newDfp("-0").add(field.newDfp("-0")), 
             field.newDfp("-0"),
             0, "Add #28");

        test(field.newDfp("1e-20").add(field.newDfp("0")), 
             field.newDfp("1e-20"),
             0, "Add #29");

        test(field.newDfp("1e-40").add(field.newDfp("0")), 
             field.newDfp("1e-40"),
             0, "Add #30");

        test(pinf.add(ninf), 
             nan,
             DfpField.FLAG_INVALID, "Add #31");

        test(ninf.add(pinf), 
             nan,
             DfpField.FLAG_INVALID, "Add #32");

        test(ninf.add(ninf), 
             ninf,
             0, "Add #33");

        test(pinf.add(pinf), 
             pinf,
             0, "Add #34");

        test(pinf.add(field.newDfp("0")), 
             pinf,
             0, "Add #35");

        test(pinf.add(field.newDfp("-1e131071")), 
             pinf,
             0, "Add #36");

        test(pinf.add(field.newDfp("1e131071")), 
             pinf,
             0, "Add #37");

        test(field.newDfp("0").add(pinf), 
             pinf,
             0, "Add #38");

        test(field.newDfp("-1e131071").add(pinf), 
             pinf,
             0, "Add #39");

        test(field.newDfp("1e131071").add(pinf), 
             pinf,
             0, "Add #40");

        test(ninf.add(field.newDfp("0")), 
             ninf,
             0, "Add #41");

        test(ninf.add(field.newDfp("-1e131071")), 
             ninf,
             0, "Add #42");

        test(ninf.add(field.newDfp("1e131071")), 
             ninf,
             0, "Add #43");

        test(field.newDfp("0").add(ninf), 
             ninf,
             0, "Add #44");

        test(field.newDfp("-1e131071").add(ninf), 
             ninf,
             0, "Add #45");

        test(field.newDfp("1e131071").add(ninf), 
             ninf,
             0, "Add #46");

        test(field.newDfp("9.9999999999999999999e131071").add(field.newDfp("5e131051")),  
             pinf,
             DfpField.FLAG_OVERFLOW, "Add #47");

        test(field.newDfp("9.9999999999999999999e131071").add(field.newDfp("4.9999999999999999999e131051")),  
             field.newDfp("9.9999999999999999999e131071"),
             DfpField.FLAG_INEXACT, "Add #48");

        test(nan.add(field.newDfp("1")),
             nan,
             0, "Add #49");

        test(field.newDfp("1").add(nan),
             nan,
             0, "Add #50");

        test(field.newDfp("12345678123456781234").add(field.newDfp("0.12345678123456781234")),
             field.newDfp("12345678123456781234"),
             DfpField.FLAG_INEXACT, "Add #51");

        test(field.newDfp("12345678123456781234").add(field.newDfp("123.45678123456781234")),
             field.newDfp("12345678123456781357"),
             DfpField.FLAG_INEXACT, "Add #52");

        test(field.newDfp("123.45678123456781234").add(field.newDfp("12345678123456781234")),
             field.newDfp("12345678123456781357"),
             DfpField.FLAG_INEXACT, "Add #53");

        test(field.newDfp("12345678123456781234").add(field.newDfp(".00001234567812345678")),
             field.newDfp("12345678123456781234"),
             DfpField.FLAG_INEXACT, "Add #54");

        test(field.newDfp("12345678123456781234").add(field.newDfp(".00000000123456781234")),
             field.newDfp("12345678123456781234"),
             DfpField.FLAG_INEXACT, "Add #55");

        test(field.newDfp("-0").add(field.newDfp("-0")),
             field.newDfp("-0"),
             0, "Add #56");

        test(field.newDfp("0").add(field.newDfp("-0")),
             field.newDfp("0"),
             0, "Add #57");

        test(field.newDfp("-0").add(field.newDfp("0")),
             field.newDfp("0"),
             0, "Add #58");

        test(field.newDfp("0").add(field.newDfp("0")),
             field.newDfp("0"),
             0, "Add #59");
    }

// org.apache.commons.math3.dfp.DfpTest::testCompare
    public void  testCompare()
    {
        
        
        field.clearIEEEFlags();

        cmptst(field.newDfp("0"), field.newDfp("0"), "equal", true, 1);         
        cmptst(field.newDfp("0"), field.newDfp("-0"), "equal", true, 2);        
        cmptst(field.newDfp("-0"), field.newDfp("-0"), "equal", true, 3);       
        cmptst(field.newDfp("-0"), field.newDfp("0"), "equal", true, 4);        

        

        cmptst(field.newDfp("0"), field.newDfp("1"), "equal", false, 5);         
        cmptst(field.newDfp("1"), field.newDfp("0"), "equal", false, 6);         
        cmptst(field.newDfp("-1"), field.newDfp("0"), "equal", false, 7);        
        cmptst(field.newDfp("0"), field.newDfp("-1"), "equal", false, 8);        
        cmptst(field.newDfp("0"), field.newDfp("1e-131072"), "equal", false, 9); 
        
        if (field.getIEEEFlags() != 0)
            Assert.fail("assersion failed.  compare flags = "+field.getIEEEFlags());

        cmptst(field.newDfp("0"), field.newDfp("1e-131078"), "equal", false, 10); 

        
        if (field.getIEEEFlags() != DfpField.FLAG_UNDERFLOW)
            Assert.fail("assersion failed.  compare flags = "+field.getIEEEFlags());

        field.clearIEEEFlags();

        cmptst(field.newDfp("0"), field.newDfp("1e+131071"), "equal", false, 11); 

        

        cmptst(field.newDfp("0"), pinf, "equal", false, 12);    
        cmptst(field.newDfp("0"), ninf, "equal", false, 13);    
        cmptst(field.newDfp("-0"), pinf, "equal", false, 14);   
        cmptst(field.newDfp("-0"), ninf, "equal", false, 15);   
        cmptst(pinf, field.newDfp("0"), "equal", false, 16);    
        cmptst(ninf, field.newDfp("0"), "equal", false, 17);    
        cmptst(pinf, field.newDfp("-0"), "equal", false, 18);   
        cmptst(ninf, field.newDfp("-0"), "equal", false, 19);   
        cmptst(ninf, pinf, "equal", false, 19.10);     
        cmptst(pinf, ninf, "equal", false, 19.11);     
        cmptst(pinf, pinf, "equal", true, 19.12);     
        cmptst(ninf, ninf, "equal", true, 19.13);     

        
        cmptst(field.newDfp("1"), field.newDfp("1"), "equal", true, 20);   
        cmptst(field.newDfp("1"), field.newDfp("-1"), "equal", false, 21);   
        cmptst(field.newDfp("-1"), field.newDfp("-1"), "equal", true, 22);   
        cmptst(field.newDfp("1"), field.newDfp("1.0000000000000001"), "equal", false, 23);   

        
        
        cmptst(field.newDfp("1e20"), field.newDfp("1.0000000000000001"), "equal", false, 24);
        if (field.getIEEEFlags() != 0)
            Assert.fail("assersion failed.  compare flags = "+field.getIEEEFlags());

        cmptst(field.newDfp("0.000001"), field.newDfp("1e-6"), "equal", true, 25);

        

        cmptst(snan, snan, "equal", false, 27);
        cmptst(qnan, qnan, "equal", false, 28);
        cmptst(snan, qnan, "equal", false, 29);
        cmptst(qnan, snan, "equal", false, 30);
        cmptst(qnan, field.newDfp("0"), "equal", false, 31);
        cmptst(snan, field.newDfp("0"), "equal", false, 32);
        cmptst(field.newDfp("0"), snan, "equal", false, 33);
        cmptst(field.newDfp("0"), qnan, "equal", false, 34);
        cmptst(qnan, pinf, "equal", false, 35);
        cmptst(snan, pinf, "equal", false, 36);
        cmptst(pinf, snan, "equal", false, 37);
        cmptst(pinf, qnan, "equal", false, 38);
        cmptst(qnan, ninf, "equal", false, 39);
        cmptst(snan, ninf, "equal", false, 40);
        cmptst(ninf, snan, "equal", false, 41);
        cmptst(ninf, qnan, "equal", false, 42);
        cmptst(qnan, field.newDfp("-1"), "equal", false, 43);
        cmptst(snan, field.newDfp("-1"), "equal", false, 44);
        cmptst(field.newDfp("-1"), snan, "equal", false, 45);
        cmptst(field.newDfp("-1"), qnan, "equal", false, 46);
        cmptst(qnan, field.newDfp("1"), "equal", false, 47);
        cmptst(snan, field.newDfp("1"), "equal", false, 48);
        cmptst(field.newDfp("1"), snan, "equal", false, 49);
        cmptst(field.newDfp("1"), qnan, "equal", false, 50);
        cmptst(snan.negate(), snan, "equal", false, 51);
        cmptst(qnan.negate(), qnan, "equal", false, 52);

        
        
        

        cmptst(field.newDfp("0"), field.newDfp("0"), "unequal", false, 1);         
        cmptst(field.newDfp("0"), field.newDfp("-0"), "unequal", false, 2);        
        cmptst(field.newDfp("-0"), field.newDfp("-0"), "unequal", false, 3);       
        cmptst(field.newDfp("-0"), field.newDfp("0"), "unequal", false, 4);        

        

        cmptst(field.newDfp("0"), field.newDfp("1"), "unequal", true, 5);         
        cmptst(field.newDfp("1"), field.newDfp("0"), "unequal", true, 6);         
        cmptst(field.newDfp("-1"), field.newDfp("0"), "unequal", true, 7);        
        cmptst(field.newDfp("0"), field.newDfp("-1"), "unequal", true, 8);        
        cmptst(field.newDfp("0"), field.newDfp("1e-131072"), "unequal", true, 9); 
        
        if (field.getIEEEFlags() != 0)
            Assert.fail("assersion failed.  compare flags = "+field.getIEEEFlags());

        cmptst(field.newDfp("0"), field.newDfp("1e-131078"), "unequal", true, 10); 

        
        if (field.getIEEEFlags() != DfpField.FLAG_UNDERFLOW)
            Assert.fail("assersion failed.  compare flags = "+field.getIEEEFlags());

        field.clearIEEEFlags();

        cmptst(field.newDfp("0"), field.newDfp("1e+131071"), "unequal", true, 11); 

        

        cmptst(field.newDfp("0"), pinf, "unequal", true, 12);    
        cmptst(field.newDfp("0"), ninf, "unequal", true, 13);    
        cmptst(field.newDfp("-0"), pinf, "unequal", true, 14);   
        cmptst(field.newDfp("-0"), ninf, "unequal", true, 15);   
        cmptst(pinf, field.newDfp("0"), "unequal", true, 16);    
        cmptst(ninf, field.newDfp("0"), "unequal", true, 17);    
        cmptst(pinf, field.newDfp("-0"), "unequal", true, 18);   
        cmptst(ninf, field.newDfp("-0"), "unequal", true, 19);   
        cmptst(ninf, pinf, "unequal", true, 19.10);     
        cmptst(pinf, ninf, "unequal", true, 19.11);     
        cmptst(pinf, pinf, "unequal", false, 19.12);     
        cmptst(ninf, ninf, "unequal", false, 19.13);     

        
        cmptst(field.newDfp("1"), field.newDfp("1"), "unequal", false, 20);   
        cmptst(field.newDfp("1"), field.newDfp("-1"), "unequal", true, 21);   
        cmptst(field.newDfp("-1"), field.newDfp("-1"), "unequal", false, 22);   
        cmptst(field.newDfp("1"), field.newDfp("1.0000000000000001"), "unequal", true, 23);   

        
        
        cmptst(field.newDfp("1e20"), field.newDfp("1.0000000000000001"), "unequal", true, 24);
        if (field.getIEEEFlags() != 0)
            Assert.fail("assersion failed.  compare flags = "+field.getIEEEFlags());

        cmptst(field.newDfp("0.000001"), field.newDfp("1e-6"), "unequal", false, 25);

        

        cmptst(snan, snan, "unequal", false, 27);
        cmptst(qnan, qnan, "unequal", false, 28);
        cmptst(snan, qnan, "unequal", false, 29);
        cmptst(qnan, snan, "unequal", false, 30);
        cmptst(qnan, field.newDfp("0"), "unequal", false, 31);
        cmptst(snan, field.newDfp("0"), "unequal", false, 32);
        cmptst(field.newDfp("0"), snan, "unequal", false, 33);
        cmptst(field.newDfp("0"), qnan, "unequal", false, 34);
        cmptst(qnan, pinf, "unequal", false, 35);
        cmptst(snan, pinf, "unequal", false, 36);
        cmptst(pinf, snan, "unequal", false, 37);
        cmptst(pinf, qnan, "unequal", false, 38);
        cmptst(qnan, ninf, "unequal", false, 39);
        cmptst(snan, ninf, "unequal", false, 40);
        cmptst(ninf, snan, "unequal", false, 41);
        cmptst(ninf, qnan, "unequal", false, 42);
        cmptst(qnan, field.newDfp("-1"), "unequal", false, 43);
        cmptst(snan, field.newDfp("-1"), "unequal", false, 44);
        cmptst(field.newDfp("-1"), snan, "unequal", false, 45);
        cmptst(field.newDfp("-1"), qnan, "unequal", false, 46);
        cmptst(qnan, field.newDfp("1"), "unequal", false, 47);
        cmptst(snan, field.newDfp("1"), "unequal", false, 48);
        cmptst(field.newDfp("1"), snan, "unequal", false, 49);
        cmptst(field.newDfp("1"), qnan, "unequal", false, 50);
        cmptst(snan.negate(), snan, "unequal", false, 51);
        cmptst(qnan.negate(), qnan, "unequal", false, 52);

        if (field.getIEEEFlags() != 0)
            Assert.fail("assersion failed.  compare unequal flags = "+field.getIEEEFlags());

        
        
        

        cmptst(field.newDfp("0"), field.newDfp("0"), "lessThan", false, 1);         
        cmptst(field.newDfp("0"), field.newDfp("-0"), "lessThan", false, 2);        
        cmptst(field.newDfp("-0"), field.newDfp("-0"), "lessThan", false, 3);       
        cmptst(field.newDfp("-0"), field.newDfp("0"), "lessThan", false, 4);        

        

        cmptst(field.newDfp("0"), field.newDfp("1"), "lessThan", true, 5);         
        cmptst(field.newDfp("1"), field.newDfp("0"), "lessThan", false, 6);         
        cmptst(field.newDfp("-1"), field.newDfp("0"), "lessThan", true, 7);        
        cmptst(field.newDfp("0"), field.newDfp("-1"), "lessThan", false, 8);        
        cmptst(field.newDfp("0"), field.newDfp("1e-131072"), "lessThan", true, 9); 
        
        if (field.getIEEEFlags() != 0)
            Assert.fail("assersion failed.  compare flags = "+field.getIEEEFlags());

        cmptst(field.newDfp("0"), field.newDfp("1e-131078"), "lessThan", true, 10); 

        
        if (field.getIEEEFlags() != DfpField.FLAG_UNDERFLOW)
            Assert.fail("assersion failed.  compare flags = "+field.getIEEEFlags());
        field.clearIEEEFlags();

        cmptst(field.newDfp("0"), field.newDfp("1e+131071"), "lessThan", true, 11); 

        

        cmptst(field.newDfp("0"), pinf, "lessThan", true, 12);    
        cmptst(field.newDfp("0"), ninf, "lessThan", false, 13);    
        cmptst(field.newDfp("-0"), pinf, "lessThan", true, 14);   
        cmptst(field.newDfp("-0"), ninf, "lessThan", false, 15);   
        cmptst(pinf, field.newDfp("0"), "lessThan", false, 16);    
        cmptst(ninf, field.newDfp("0"), "lessThan", true, 17);    
        cmptst(pinf, field.newDfp("-0"), "lessThan", false, 18);   
        cmptst(ninf, field.newDfp("-0"), "lessThan", true, 19);   
        cmptst(ninf, pinf, "lessThan", true, 19.10);     
        cmptst(pinf, ninf, "lessThan", false, 19.11);     
        cmptst(pinf, pinf, "lessThan", false, 19.12);     
        cmptst(ninf, ninf, "lessThan", false, 19.13);     

        
        cmptst(field.newDfp("1"), field.newDfp("1"), "lessThan", false, 20);   
        cmptst(field.newDfp("1"), field.newDfp("-1"), "lessThan", false, 21);   
        cmptst(field.newDfp("-1"), field.newDfp("-1"), "lessThan", false, 22);   
        cmptst(field.newDfp("1"), field.newDfp("1.0000000000000001"), "lessThan", true, 23);   

        
        
        cmptst(field.newDfp("1e20"), field.newDfp("1.0000000000000001"), "lessThan", false, 24);
        if (field.getIEEEFlags() != 0)
            Assert.fail("assersion failed.  compare flags = "+field.getIEEEFlags());

        cmptst(field.newDfp("0.000001"), field.newDfp("1e-6"), "lessThan", false, 25);

        
        cmptst(snan, snan, "lessThan", false, 27);
        cmptst(qnan, qnan, "lessThan", false, 28);
        cmptst(snan, qnan, "lessThan", false, 29);
        cmptst(qnan, snan, "lessThan", false, 30);
        cmptst(qnan, field.newDfp("0"), "lessThan", false, 31);
        cmptst(snan, field.newDfp("0"), "lessThan", false, 32);
        cmptst(field.newDfp("0"), snan, "lessThan", false, 33);
        cmptst(field.newDfp("0"), qnan, "lessThan", false, 34);
        cmptst(qnan, pinf, "lessThan", false, 35);
        cmptst(snan, pinf, "lessThan", false, 36);
        cmptst(pinf, snan, "lessThan", false, 37);
        cmptst(pinf, qnan, "lessThan", false, 38);
        cmptst(qnan, ninf, "lessThan", false, 39);
        cmptst(snan, ninf, "lessThan", false, 40);
        cmptst(ninf, snan, "lessThan", false, 41);
        cmptst(ninf, qnan, "lessThan", false, 42);
        cmptst(qnan, field.newDfp("-1"), "lessThan", false, 43);
        cmptst(snan, field.newDfp("-1"), "lessThan", false, 44);
        cmptst(field.newDfp("-1"), snan, "lessThan", false, 45);
        cmptst(field.newDfp("-1"), qnan, "lessThan", false, 46);
        cmptst(qnan, field.newDfp("1"), "lessThan", false, 47);
        cmptst(snan, field.newDfp("1"), "lessThan", false, 48);
        cmptst(field.newDfp("1"), snan, "lessThan", false, 49);
        cmptst(field.newDfp("1"), qnan, "lessThan", false, 50);
        cmptst(snan.negate(), snan, "lessThan", false, 51);
        cmptst(qnan.negate(), qnan, "lessThan", false, 52);

        
        if (field.getIEEEFlags() != DfpField.FLAG_INVALID)
            Assert.fail("assersion failed.  compare lessThan flags = "+field.getIEEEFlags());
        field.clearIEEEFlags();

        
        
        

        cmptst(field.newDfp("0"), field.newDfp("0"), "greaterThan", false, 1);         
        cmptst(field.newDfp("0"), field.newDfp("-0"), "greaterThan", false, 2);        
        cmptst(field.newDfp("-0"), field.newDfp("-0"), "greaterThan", false, 3);       
        cmptst(field.newDfp("-0"), field.newDfp("0"), "greaterThan", false, 4);        

        

        cmptst(field.newDfp("0"), field.newDfp("1"), "greaterThan", false, 5);         
        cmptst(field.newDfp("1"), field.newDfp("0"), "greaterThan", true, 6);         
        cmptst(field.newDfp("-1"), field.newDfp("0"), "greaterThan", false, 7);        
        cmptst(field.newDfp("0"), field.newDfp("-1"), "greaterThan", true, 8);        
        cmptst(field.newDfp("0"), field.newDfp("1e-131072"), "greaterThan", false, 9); 
        
        if (field.getIEEEFlags() != 0)
            Assert.fail("assersion failed.  compare flags = "+field.getIEEEFlags());

        cmptst(field.newDfp("0"), field.newDfp("1e-131078"), "greaterThan", false, 10); 

        
        if (field.getIEEEFlags() != DfpField.FLAG_UNDERFLOW)
            Assert.fail("assersion failed.  compare flags = "+field.getIEEEFlags());
        field.clearIEEEFlags();

        cmptst(field.newDfp("0"), field.newDfp("1e+131071"), "greaterThan", false, 11); 

        

        cmptst(field.newDfp("0"), pinf, "greaterThan", false, 12);    
        cmptst(field.newDfp("0"), ninf, "greaterThan", true, 13);    
        cmptst(field.newDfp("-0"), pinf, "greaterThan", false, 14);   
        cmptst(field.newDfp("-0"), ninf, "greaterThan", true, 15);   
        cmptst(pinf, field.newDfp("0"), "greaterThan", true, 16);    
        cmptst(ninf, field.newDfp("0"), "greaterThan", false, 17);    
        cmptst(pinf, field.newDfp("-0"), "greaterThan", true, 18);   
        cmptst(ninf, field.newDfp("-0"), "greaterThan", false, 19);   
        cmptst(ninf, pinf, "greaterThan", false, 19.10);     
        cmptst(pinf, ninf, "greaterThan", true, 19.11);     
        cmptst(pinf, pinf, "greaterThan", false, 19.12);     
        cmptst(ninf, ninf, "greaterThan", false, 19.13);     

        
        cmptst(field.newDfp("1"), field.newDfp("1"), "greaterThan", false, 20);   
        cmptst(field.newDfp("1"), field.newDfp("-1"), "greaterThan", true, 21);   
        cmptst(field.newDfp("-1"), field.newDfp("-1"), "greaterThan", false, 22);   
        cmptst(field.newDfp("1"), field.newDfp("1.0000000000000001"), "greaterThan", false, 23);   

        
        
        cmptst(field.newDfp("1e20"), field.newDfp("1.0000000000000001"), "greaterThan", true, 24);
        if (field.getIEEEFlags() != 0)
            Assert.fail("assersion failed.  compare flags = "+field.getIEEEFlags());

        cmptst(field.newDfp("0.000001"), field.newDfp("1e-6"), "greaterThan", false, 25);

        
        cmptst(snan, snan, "greaterThan", false, 27);
        cmptst(qnan, qnan, "greaterThan", false, 28);
        cmptst(snan, qnan, "greaterThan", false, 29);
        cmptst(qnan, snan, "greaterThan", false, 30);
        cmptst(qnan, field.newDfp("0"), "greaterThan", false, 31);
        cmptst(snan, field.newDfp("0"), "greaterThan", false, 32);
        cmptst(field.newDfp("0"), snan, "greaterThan", false, 33);
        cmptst(field.newDfp("0"), qnan, "greaterThan", false, 34);
        cmptst(qnan, pinf, "greaterThan", false, 35);
        cmptst(snan, pinf, "greaterThan", false, 36);
        cmptst(pinf, snan, "greaterThan", false, 37);
        cmptst(pinf, qnan, "greaterThan", false, 38);
        cmptst(qnan, ninf, "greaterThan", false, 39);
        cmptst(snan, ninf, "greaterThan", false, 40);
        cmptst(ninf, snan, "greaterThan", false, 41);
        cmptst(ninf, qnan, "greaterThan", false, 42);
        cmptst(qnan, field.newDfp("-1"), "greaterThan", false, 43);
        cmptst(snan, field.newDfp("-1"), "greaterThan", false, 44);
        cmptst(field.newDfp("-1"), snan, "greaterThan", false, 45);
        cmptst(field.newDfp("-1"), qnan, "greaterThan", false, 46);
        cmptst(qnan, field.newDfp("1"), "greaterThan", false, 47);
        cmptst(snan, field.newDfp("1"), "greaterThan", false, 48);
        cmptst(field.newDfp("1"), snan, "greaterThan", false, 49);
        cmptst(field.newDfp("1"), qnan, "greaterThan", false, 50);
        cmptst(snan.negate(), snan, "greaterThan", false, 51);
        cmptst(qnan.negate(), qnan, "greaterThan", false, 52);

        
        if (field.getIEEEFlags() != DfpField.FLAG_INVALID)
            Assert.fail("assersion failed.  compare greaterThan flags = "+field.getIEEEFlags());
        field.clearIEEEFlags();
    }

// org.apache.commons.math3.dfp.DfpTest::testMultiply
    public void testMultiply()
    {
        test(field.newDfp("1").multiply(field.newDfp("1")),      
             field.newDfp("1"),
             0, "Multiply #1");

        test(field.newDfp("1").multiply(1),             
             field.newDfp("1"),
             0, "Multiply #2");

        test(field.newDfp("-1").multiply(field.newDfp("1")),     
             field.newDfp("-1"),
             0, "Multiply #3");

        test(field.newDfp("-1").multiply(1),            
             field.newDfp("-1"),
             0, "Multiply #4");

        
        test(field.newDfp("2").multiply(field.newDfp("3")),
             field.newDfp("6"),
             0, "Multiply #5");

        test(field.newDfp("2").multiply(3),
             field.newDfp("6"),
             0, "Multiply #6");

        test(field.newDfp("-2").multiply(field.newDfp("3")),
             field.newDfp("-6"),
             0, "Multiply #7");

        test(field.newDfp("-2").multiply(3),
             field.newDfp("-6"),
             0, "Multiply #8");

        test(field.newDfp("2").multiply(field.newDfp("-3")),
             field.newDfp("-6"),
             0, "Multiply #9");

        test(field.newDfp("-2").multiply(field.newDfp("-3")),
             field.newDfp("6"),
             0, "Multiply #10");

        

        test(field.newDfp("-2").multiply(field.newDfp("0")),
             field.newDfp("-0"),
             0, "Multiply #11");

        test(field.newDfp("-2").multiply(0),
             field.newDfp("-0"),
             0, "Multiply #12");

        test(field.newDfp("2").multiply(field.newDfp("0")),
             field.newDfp("0"),
             0, "Multiply #13");

        test(field.newDfp("2").multiply(0),
             field.newDfp("0"),
             0, "Multiply #14");

        test(field.newDfp("2").multiply(pinf),
             pinf,
             0, "Multiply #15");

        test(field.newDfp("2").multiply(ninf),
             ninf,
             0, "Multiply #16");

        test(field.newDfp("-2").multiply(pinf),
             ninf,
             0, "Multiply #17");

        test(field.newDfp("-2").multiply(ninf),
             pinf,
             0, "Multiply #18");

        test(ninf.multiply(field.newDfp("-2")),
             pinf,
             0, "Multiply #18.1");

        test(field.newDfp("5e131071").multiply(2),
             pinf,
             DfpField.FLAG_OVERFLOW, "Multiply #19");

        test(field.newDfp("5e131071").multiply(field.newDfp("1.999999999999999")),
             field.newDfp("9.9999999999999950000e131071"),
             0, "Multiply #20");

        test(field.newDfp("-5e131071").multiply(2),
             ninf,
             DfpField.FLAG_OVERFLOW, "Multiply #22");

        test(field.newDfp("-5e131071").multiply(field.newDfp("1.999999999999999")),
             field.newDfp("-9.9999999999999950000e131071"),
             0, "Multiply #23");

        test(field.newDfp("1e-65539").multiply(field.newDfp("1e-65539")),
             field.newDfp("1e-131078"),
             DfpField.FLAG_UNDERFLOW, "Multiply #24");

        test(field.newDfp("1").multiply(nan),
             nan,
             0, "Multiply #25");

        test(nan.multiply(field.newDfp("1")),
             nan,
             0, "Multiply #26");

        test(nan.multiply(pinf),
             nan,
             0, "Multiply #27");

        test(pinf.multiply(nan),
             nan,
             0, "Multiply #27");

        test(pinf.multiply(field.newDfp("0")),
             nan,
             DfpField.FLAG_INVALID, "Multiply #28");

        test(field.newDfp("0").multiply(pinf),
             nan,
             DfpField.FLAG_INVALID, "Multiply #29");

        test(pinf.multiply(pinf),
             pinf,
             0, "Multiply #30");

        test(ninf.multiply(pinf),
             ninf,
             0, "Multiply #31");

        test(pinf.multiply(ninf),
             ninf,
             0, "Multiply #32");

        test(ninf.multiply(ninf),
             pinf,
             0, "Multiply #33");

        test(pinf.multiply(1),
             pinf,
             0, "Multiply #34");

        test(pinf.multiply(0),
             nan,
             DfpField.FLAG_INVALID, "Multiply #35");

        test(nan.multiply(1),
             nan,
             0, "Multiply #36");

        test(field.newDfp("1").multiply(10000),
             field.newDfp("10000"),
             0, "Multiply #37");

        test(field.newDfp("2").multiply(1000000),
             field.newDfp("2000000"),
             0, "Multiply #38");

        test(field.newDfp("1").multiply(-1),
             field.newDfp("-1"),
             0, "Multiply #39");
    }

// org.apache.commons.math3.dfp.DfpTest::testDivide
    public void testDivide()
    {
        test(field.newDfp("1").divide(nan),      
             nan,
             0, "Divide #1");

        test(nan.divide(field.newDfp("1")),      
             nan,
             0, "Divide #2");

        test(pinf.divide(field.newDfp("1")),
             pinf,
             0, "Divide #3");

        test(pinf.divide(field.newDfp("-1")),
             ninf,
             0, "Divide #4");

        test(pinf.divide(pinf),
             nan,
             DfpField.FLAG_INVALID, "Divide #5");

        test(ninf.divide(pinf),
             nan,
             DfpField.FLAG_INVALID, "Divide #6");

        test(pinf.divide(ninf),
             nan,
             DfpField.FLAG_INVALID, "Divide #7");

        test(ninf.divide(ninf),
             nan,
             DfpField.FLAG_INVALID, "Divide #8");

        test(field.newDfp("0").divide(field.newDfp("0")),
             nan,
             DfpField.FLAG_DIV_ZERO, "Divide #9");

        test(field.newDfp("1").divide(field.newDfp("0")),
             pinf,
             DfpField.FLAG_DIV_ZERO, "Divide #10");

        test(field.newDfp("1").divide(field.newDfp("-0")),
             ninf,
             DfpField.FLAG_DIV_ZERO, "Divide #11");

        test(field.newDfp("-1").divide(field.newDfp("0")),
             ninf,
             DfpField.FLAG_DIV_ZERO, "Divide #12");

        test(field.newDfp("-1").divide(field.newDfp("-0")),
             pinf,
             DfpField.FLAG_DIV_ZERO, "Divide #13");

        test(field.newDfp("1").divide(field.newDfp("3")),
             field.newDfp("0.33333333333333333333"),
             DfpField.FLAG_INEXACT, "Divide #14");

        test(field.newDfp("1").divide(field.newDfp("6")),
             field.newDfp("0.16666666666666666667"),
             DfpField.FLAG_INEXACT, "Divide #15");

        test(field.newDfp("10").divide(field.newDfp("6")),
             field.newDfp("1.6666666666666667"),
             DfpField.FLAG_INEXACT, "Divide #16");

        test(field.newDfp("100").divide(field.newDfp("6")),
             field.newDfp("16.6666666666666667"),
             DfpField.FLAG_INEXACT, "Divide #17");

        test(field.newDfp("1000").divide(field.newDfp("6")),
             field.newDfp("166.6666666666666667"),
             DfpField.FLAG_INEXACT, "Divide #18");

        test(field.newDfp("10000").divide(field.newDfp("6")),
             field.newDfp("1666.6666666666666667"),
             DfpField.FLAG_INEXACT, "Divide #19");

        test(field.newDfp("1").divide(field.newDfp("1")),
             field.newDfp("1"),
             0, "Divide #20");

        test(field.newDfp("1").divide(field.newDfp("-1")),
             field.newDfp("-1"),
             0, "Divide #21");

        test(field.newDfp("-1").divide(field.newDfp("1")),
             field.newDfp("-1"),
             0, "Divide #22");

        test(field.newDfp("-1").divide(field.newDfp("-1")),
             field.newDfp("1"),
             0, "Divide #23");

        test(field.newDfp("1e-65539").divide(field.newDfp("1e65539")),
             field.newDfp("1e-131078"),
             DfpField.FLAG_UNDERFLOW, "Divide #24");

        test(field.newDfp("1e65539").divide(field.newDfp("1e-65539")),
             pinf,
             DfpField.FLAG_OVERFLOW, "Divide #24");

        test(field.newDfp("2").divide(field.newDfp("1.5")),     
             field.newDfp("1.3333333333333333"),
             DfpField.FLAG_INEXACT, "Divide #25");

        test(field.newDfp("2").divide(pinf),
             field.newDfp("0"),
             0, "Divide #26");

        test(field.newDfp("2").divide(ninf),
             field.newDfp("-0"),
             0, "Divide #27");

        test(field.newDfp("0").divide(field.newDfp("1")),
             field.newDfp("0"),
             0, "Divide #28");
    }

// org.apache.commons.math3.dfp.DfpTest::testReciprocal
    public void testReciprocal()
    {
        test(nan.reciprocal(),
             nan,
             0, "Reciprocal #1");

        test(field.newDfp("0").reciprocal(),
             pinf,
             DfpField.FLAG_DIV_ZERO, "Reciprocal #2");

        test(field.newDfp("-0").reciprocal(),
             ninf,
             DfpField.FLAG_DIV_ZERO, "Reciprocal #3");

        test(field.newDfp("3").reciprocal(),
             field.newDfp("0.33333333333333333333"),
             DfpField.FLAG_INEXACT, "Reciprocal #4");

        test(field.newDfp("6").reciprocal(),
             field.newDfp("0.16666666666666666667"),
             DfpField.FLAG_INEXACT, "Reciprocal #5");

        test(field.newDfp("1").reciprocal(),
             field.newDfp("1"),
             0, "Reciprocal #6");

        test(field.newDfp("-1").reciprocal(),
             field.newDfp("-1"),
             0, "Reciprocal #7");

        test(pinf.reciprocal(),
             field.newDfp("0"),
             0, "Reciprocal #8");

        test(ninf.reciprocal(),
             field.newDfp("-0"),
             0, "Reciprocal #9");
    }

// org.apache.commons.math3.dfp.DfpTest::testDivideInt
    public void testDivideInt()
    {
        test(nan.divide(1),      
             nan,
             0, "DivideInt #1");

        test(pinf.divide(1),
             pinf,
             0, "DivideInt #2");

        test(field.newDfp("0").divide(0),
             nan,
             DfpField.FLAG_DIV_ZERO, "DivideInt #3");

        test(field.newDfp("1").divide(0),
             pinf,
             DfpField.FLAG_DIV_ZERO, "DivideInt #4");

        test(field.newDfp("-1").divide(0),
             ninf,
             DfpField.FLAG_DIV_ZERO, "DivideInt #5");

        test(field.newDfp("1").divide(3),
             field.newDfp("0.33333333333333333333"),
             DfpField.FLAG_INEXACT, "DivideInt #6");

        test(field.newDfp("1").divide(6),
             field.newDfp("0.16666666666666666667"),
             DfpField.FLAG_INEXACT, "DivideInt #7");

        test(field.newDfp("10").divide(6),
             field.newDfp("1.6666666666666667"),
             DfpField.FLAG_INEXACT, "DivideInt #8");

        test(field.newDfp("100").divide(6),
             field.newDfp("16.6666666666666667"),
             DfpField.FLAG_INEXACT, "DivideInt #9");

        test(field.newDfp("1000").divide(6),
             field.newDfp("166.6666666666666667"),
             DfpField.FLAG_INEXACT, "DivideInt #10");

        test(field.newDfp("10000").divide(6),
             field.newDfp("1666.6666666666666667"),
             DfpField.FLAG_INEXACT, "DivideInt #20");

        test(field.newDfp("1").divide(1),
             field.newDfp("1"),
             0, "DivideInt #21");

        test(field.newDfp("1e-131077").divide(10),
             field.newDfp("1e-131078"),
             DfpField.FLAG_UNDERFLOW, "DivideInt #22");

        test(field.newDfp("0").divide(1),
             field.newDfp("0"),
             0, "DivideInt #23");

        test(field.newDfp("1").divide(10000),
             nan,
             DfpField.FLAG_INVALID, "DivideInt #24");

        test(field.newDfp("1").divide(-1),
             nan,
             DfpField.FLAG_INVALID, "DivideInt #25");
    }

// org.apache.commons.math3.dfp.DfpTest::testNextAfter
    public void testNextAfter()
    {
        test(field.newDfp("1").nextAfter(pinf),
             field.newDfp("1.0000000000000001"),
             0, "NextAfter #1");

        test(field.newDfp("1.0000000000000001").nextAfter(ninf),
             field.newDfp("1"),
             0, "NextAfter #1.5");

        test(field.newDfp("1").nextAfter(ninf),
             field.newDfp("0.99999999999999999999"),
             0, "NextAfter #2");

        test(field.newDfp("0.99999999999999999999").nextAfter(field.newDfp("2")),
             field.newDfp("1"),
             0, "NextAfter #3");

        test(field.newDfp("-1").nextAfter(ninf),
             field.newDfp("-1.0000000000000001"),
             0, "NextAfter #4");

        test(field.newDfp("-1").nextAfter(pinf),
             field.newDfp("-0.99999999999999999999"),
             0, "NextAfter #5");

        test(field.newDfp("-0.99999999999999999999").nextAfter(field.newDfp("-2")),
             field.newDfp("-1"),
             0, "NextAfter #6");

        test(field.newDfp("2").nextAfter(field.newDfp("2")),
             field.newDfp("2"),
             0, "NextAfter #7");

        test(field.newDfp("0").nextAfter(field.newDfp("0")),
             field.newDfp("0"),
             0, "NextAfter #8");

        test(field.newDfp("-2").nextAfter(field.newDfp("-2")),
             field.newDfp("-2"),
             0, "NextAfter #9");

        test(field.newDfp("0").nextAfter(field.newDfp("1")),
             field.newDfp("1e-131092"),
             DfpField.FLAG_UNDERFLOW, "NextAfter #10");

        test(field.newDfp("0").nextAfter(field.newDfp("-1")),
             field.newDfp("-1e-131092"),
             DfpField.FLAG_UNDERFLOW, "NextAfter #11");

        test(field.newDfp("-1e-131092").nextAfter(pinf),
             field.newDfp("-0"),
             DfpField.FLAG_UNDERFLOW|DfpField.FLAG_INEXACT, "Next After #12");

        test(field.newDfp("1e-131092").nextAfter(ninf),
             field.newDfp("0"),
             DfpField.FLAG_UNDERFLOW|DfpField.FLAG_INEXACT, "Next After #13");

        test(field.newDfp("9.9999999999999999999e131078").nextAfter(pinf),
             pinf,
             DfpField.FLAG_OVERFLOW|DfpField.FLAG_INEXACT, "Next After #14");
    }

// org.apache.commons.math3.dfp.DfpTest::testToString
    public void testToString()
    {
        Assert.assertEquals("toString #1", "Infinity", pinf.toString());
        Assert.assertEquals("toString #2", "-Infinity", ninf.toString());
        Assert.assertEquals("toString #3", "NaN", nan.toString());
        Assert.assertEquals("toString #4", "NaN", field.newDfp((byte) 1, Dfp.QNAN).toString());
        Assert.assertEquals("toString #5", "NaN", field.newDfp((byte) 1, Dfp.SNAN).toString());
        Assert.assertEquals("toString #6", "1.2300000000000000e100", field.newDfp("1.23e100").toString());
        Assert.assertEquals("toString #7", "-1.2300000000000000e100", field.newDfp("-1.23e100").toString());
        Assert.assertEquals("toString #8", "12345678.1234", field.newDfp("12345678.1234").toString());
        Assert.assertEquals("toString #9", "0.00001234", field.newDfp("0.00001234").toString());
    }

// org.apache.commons.math3.dfp.DfpTest::testRound
    public void testRound()
    {
        field.setRoundingMode(DfpField.RoundingMode.ROUND_DOWN);

        
        test(field.newDfp("12345678901234567890").add(field.newDfp("0.9")),
             field.newDfp("12345678901234567890"),
             DfpField.FLAG_INEXACT, "Round #1");

        test(field.newDfp("12345678901234567890").add(field.newDfp("0.99999999")),
             field.newDfp("12345678901234567890"),
             DfpField.FLAG_INEXACT, "Round #2");

        test(field.newDfp("-12345678901234567890").add(field.newDfp("-0.99999999")),
             field.newDfp("-12345678901234567890"),
             DfpField.FLAG_INEXACT, "Round #3");

        field.setRoundingMode(DfpField.RoundingMode.ROUND_UP);

        
        test(field.newDfp("12345678901234567890").add(field.newDfp("0.1")),
             field.newDfp("12345678901234567891"),
             DfpField.FLAG_INEXACT, "Round #4");

        test(field.newDfp("12345678901234567890").add(field.newDfp("0.0001")),
             field.newDfp("12345678901234567891"),
             DfpField.FLAG_INEXACT, "Round #5");

        test(field.newDfp("-12345678901234567890").add(field.newDfp("-0.1")),
             field.newDfp("-12345678901234567891"),
             DfpField.FLAG_INEXACT, "Round #6");

        test(field.newDfp("-12345678901234567890").add(field.newDfp("-0.0001")),
             field.newDfp("-12345678901234567891"),
             DfpField.FLAG_INEXACT, "Round #7");

        field.setRoundingMode(DfpField.RoundingMode.ROUND_HALF_UP);

        
        test(field.newDfp("12345678901234567890").add(field.newDfp("0.4999")),
             field.newDfp("12345678901234567890"),
             DfpField.FLAG_INEXACT, "Round #8");

        test(field.newDfp("12345678901234567890").add(field.newDfp("0.5000")),
             field.newDfp("12345678901234567891"),
             DfpField.FLAG_INEXACT, "Round #9");

        test(field.newDfp("-12345678901234567890").add(field.newDfp("-0.4999")),
             field.newDfp("-12345678901234567890"),
             DfpField.FLAG_INEXACT, "Round #10");

        test(field.newDfp("-12345678901234567890").add(field.newDfp("-0.5000")),
             field.newDfp("-12345678901234567891"),
             DfpField.FLAG_INEXACT, "Round #11");

        field.setRoundingMode(DfpField.RoundingMode.ROUND_HALF_DOWN);

        
        test(field.newDfp("12345678901234567890").add(field.newDfp("0.5001")),
             field.newDfp("12345678901234567891"),
             DfpField.FLAG_INEXACT, "Round #12");

        test(field.newDfp("12345678901234567890").add(field.newDfp("0.5000")),
             field.newDfp("12345678901234567890"),
             DfpField.FLAG_INEXACT, "Round #13");

        test(field.newDfp("-12345678901234567890").add(field.newDfp("-0.5001")),
             field.newDfp("-12345678901234567891"),
             DfpField.FLAG_INEXACT, "Round #14");

        test(field.newDfp("-12345678901234567890").add(field.newDfp("-0.5000")),
             field.newDfp("-12345678901234567890"),
             DfpField.FLAG_INEXACT, "Round #15");

        field.setRoundingMode(DfpField.RoundingMode.ROUND_HALF_ODD);

        
        test(field.newDfp("12345678901234567890").add(field.newDfp("0.5000")),
             field.newDfp("12345678901234567891"),
             DfpField.FLAG_INEXACT, "Round #16");

        test(field.newDfp("12345678901234567891").add(field.newDfp("0.5000")),
             field.newDfp("12345678901234567891"),
             DfpField.FLAG_INEXACT, "Round #17");

        test(field.newDfp("-12345678901234567890").add(field.newDfp("-0.5000")),
             field.newDfp("-12345678901234567891"),
             DfpField.FLAG_INEXACT, "Round #18");

        test(field.newDfp("-12345678901234567891").add(field.newDfp("-0.5000")),
             field.newDfp("-12345678901234567891"),
             DfpField.FLAG_INEXACT, "Round #19");

        field.setRoundingMode(DfpField.RoundingMode.ROUND_CEIL);

        
        test(field.newDfp("12345678901234567890").add(field.newDfp("0.0001")),
             field.newDfp("12345678901234567891"),
             DfpField.FLAG_INEXACT, "Round #20");

        test(field.newDfp("-12345678901234567890").add(field.newDfp("-0.9999")),
             field.newDfp("-12345678901234567890"),
             DfpField.FLAG_INEXACT, "Round #21");

        field.setRoundingMode(DfpField.RoundingMode.ROUND_FLOOR);

        
        test(field.newDfp("12345678901234567890").add(field.newDfp("0.9999")),
             field.newDfp("12345678901234567890"),
             DfpField.FLAG_INEXACT, "Round #22");

        test(field.newDfp("-12345678901234567890").add(field.newDfp("-0.0001")),
             field.newDfp("-12345678901234567891"),
             DfpField.FLAG_INEXACT, "Round #23");

        field.setRoundingMode(DfpField.RoundingMode.ROUND_HALF_EVEN);  
    }

// org.apache.commons.math3.dfp.DfpTest::testCeil
    public void testCeil()
    {
        test(field.newDfp("1234.0000000000000001").ceil(),
             field.newDfp("1235"),
             DfpField.FLAG_INEXACT, "Ceil #1");
    }

// org.apache.commons.math3.dfp.DfpTest::testFloor
    public void testFloor()
    {
        test(field.newDfp("1234.9999999999999999").floor(),
             field.newDfp("1234"),
             DfpField.FLAG_INEXACT, "Floor #1");
    }

// org.apache.commons.math3.dfp.DfpTest::testRint
    public void testRint()
    {
        test(field.newDfp("1234.50000000001").rint(),
             field.newDfp("1235"),
             DfpField.FLAG_INEXACT, "Rint #1");

        test(field.newDfp("1234.5000").rint(),
             field.newDfp("1234"),
             DfpField.FLAG_INEXACT, "Rint #2");

        test(field.newDfp("1235.5000").rint(),
             field.newDfp("1236"),
             DfpField.FLAG_INEXACT, "Rint #3");
    }

// org.apache.commons.math3.dfp.DfpTest::testCopySign
    public void testCopySign()
    {
        test(Dfp.copysign(field.newDfp("1234."), field.newDfp("-1")),
             field.newDfp("-1234"),
             0, "CopySign #1");

        test(Dfp.copysign(field.newDfp("-1234."), field.newDfp("-1")),
             field.newDfp("-1234"),
             0, "CopySign #2");

        test(Dfp.copysign(field.newDfp("-1234."), field.newDfp("1")),
             field.newDfp("1234"),
             0, "CopySign #3");

        test(Dfp.copysign(field.newDfp("1234."), field.newDfp("1")),
             field.newDfp("1234"),
             0, "CopySign #4");
    }

// org.apache.commons.math3.dfp.DfpTest::testIntValue
    public void testIntValue()
    {
        Assert.assertEquals("intValue #1", 1234, field.newDfp("1234").intValue());
        Assert.assertEquals("intValue #2", -1234, field.newDfp("-1234").intValue());
        Assert.assertEquals("intValue #3", 1234, field.newDfp("1234.5").intValue());
        Assert.assertEquals("intValue #4", 1235, field.newDfp("1234.500001").intValue());
        Assert.assertEquals("intValue #5", 2147483647, field.newDfp("1e1000").intValue());
        Assert.assertEquals("intValue #6", -2147483648, field.newDfp("-1e1000").intValue());
    }

// org.apache.commons.math3.dfp.DfpTest::testLog10K
    public void testLog10K()
    {
        Assert.assertEquals("log10K #1", 1, field.newDfp("123456").log10K());
        Assert.assertEquals("log10K #2", 2, field.newDfp("123456789").log10K());
        Assert.assertEquals("log10K #3", 0, field.newDfp("2").log10K());
        Assert.assertEquals("log10K #3", 0, field.newDfp("1").log10K());
        Assert.assertEquals("log10K #4", -1, field.newDfp("0.1").log10K());
    }

// org.apache.commons.math3.dfp.DfpTest::testPower10K
    public void testPower10K()
    {
        Dfp d = field.newDfp();

        test(d.power10K(0), field.newDfp("1"), 0, "Power10 #1");
        test(d.power10K(1), field.newDfp("10000"), 0, "Power10 #2");
        test(d.power10K(2), field.newDfp("100000000"), 0, "Power10 #3");

        test(d.power10K(-1), field.newDfp("0.0001"), 0, "Power10 #4");
        test(d.power10K(-2), field.newDfp("0.00000001"), 0, "Power10 #5");
        test(d.power10K(-3), field.newDfp("0.000000000001"), 0, "Power10 #6");
    }

// org.apache.commons.math3.dfp.DfpTest::testLog10
    public void testLog10()
    {

        Assert.assertEquals("log10 #1", 1, field.newDfp("12").log10());
        Assert.assertEquals("log10 #2", 2, field.newDfp("123").log10());
        Assert.assertEquals("log10 #3", 3, field.newDfp("1234").log10());
        Assert.assertEquals("log10 #4", 4, field.newDfp("12345").log10());
        Assert.assertEquals("log10 #5", 5, field.newDfp("123456").log10());
        Assert.assertEquals("log10 #6", 6, field.newDfp("1234567").log10());
        Assert.assertEquals("log10 #6", 7, field.newDfp("12345678").log10());
        Assert.assertEquals("log10 #7", 8, field.newDfp("123456789").log10());
        Assert.assertEquals("log10 #8", 9, field.newDfp("1234567890").log10());
        Assert.assertEquals("log10 #9", 10, field.newDfp("12345678901").log10());
        Assert.assertEquals("log10 #10", 11, field.newDfp("123456789012").log10());
        Assert.assertEquals("log10 #11", 12, field.newDfp("1234567890123").log10());

        Assert.assertEquals("log10 #12", 0, field.newDfp("2").log10());
        Assert.assertEquals("log10 #13", 0, field.newDfp("1").log10());
        Assert.assertEquals("log10 #14", -1, field.newDfp("0.12").log10());
        Assert.assertEquals("log10 #15", -2, field.newDfp("0.012").log10());
    }

// org.apache.commons.math3.dfp.DfpTest::testPower10
    public void testPower10()
    {
        Dfp d = field.newDfp();

        test(d.power10(0), field.newDfp("1"), 0, "Power10 #1");
        test(d.power10(1), field.newDfp("10"), 0, "Power10 #2");
        test(d.power10(2), field.newDfp("100"), 0, "Power10 #3");
        test(d.power10(3), field.newDfp("1000"), 0, "Power10 #4");
        test(d.power10(4), field.newDfp("10000"), 0, "Power10 #5");
        test(d.power10(5), field.newDfp("100000"), 0, "Power10 #6");
        test(d.power10(6), field.newDfp("1000000"), 0, "Power10 #7");
        test(d.power10(7), field.newDfp("10000000"), 0, "Power10 #8");
        test(d.power10(8), field.newDfp("100000000"), 0, "Power10 #9");
        test(d.power10(9), field.newDfp("1000000000"), 0, "Power10 #10");

        test(d.power10(-1), field.newDfp(".1"), 0, "Power10 #11");
        test(d.power10(-2), field.newDfp(".01"), 0, "Power10 #12");
        test(d.power10(-3), field.newDfp(".001"), 0, "Power10 #13");
        test(d.power10(-4), field.newDfp(".0001"), 0, "Power10 #14");
        test(d.power10(-5), field.newDfp(".00001"), 0, "Power10 #15");
        test(d.power10(-6), field.newDfp(".000001"), 0, "Power10 #16");
        test(d.power10(-7), field.newDfp(".0000001"), 0, "Power10 #17");
        test(d.power10(-8), field.newDfp(".00000001"), 0, "Power10 #18");
        test(d.power10(-9), field.newDfp(".000000001"), 0, "Power10 #19");
        test(d.power10(-10), field.newDfp(".0000000001"), 0, "Power10 #20");
    }

// org.apache.commons.math3.dfp.DfpTest::testRemainder
    public void testRemainder()
    {
        test(field.newDfp("10").remainder(field.newDfp("3")),
             field.newDfp("1"),
             DfpField.FLAG_INEXACT, "Remainder #1");

        test(field.newDfp("9").remainder(field.newDfp("3")),
             field.newDfp("0"),
             0, "Remainder #2");

        test(field.newDfp("-9").remainder(field.newDfp("3")),
             field.newDfp("-0"),
             0, "Remainder #3");
    }

// org.apache.commons.math3.dfp.DfpTest::testSqrt
    public void testSqrt()
    {
        test(field.newDfp("0").sqrt(),
             field.newDfp("0"),
             0, "Sqrt #1");

        test(field.newDfp("-0").sqrt(),
             field.newDfp("-0"),
             0, "Sqrt #2");

        test(field.newDfp("1").sqrt(),
             field.newDfp("1"),
             0, "Sqrt #3");

        test(field.newDfp("2").sqrt(),
             field.newDfp("1.4142135623730950"),
             DfpField.FLAG_INEXACT, "Sqrt #4");

        test(field.newDfp("3").sqrt(),
             field.newDfp("1.7320508075688773"),
             DfpField.FLAG_INEXACT, "Sqrt #5");

        test(field.newDfp("5").sqrt(),
             field.newDfp("2.2360679774997897"),
             DfpField.FLAG_INEXACT, "Sqrt #6");

        test(field.newDfp("500").sqrt(),
             field.newDfp("22.3606797749978970"),
             DfpField.FLAG_INEXACT, "Sqrt #6.2");

        test(field.newDfp("50000").sqrt(),
             field.newDfp("223.6067977499789696"),
             DfpField.FLAG_INEXACT, "Sqrt #6.3");

        test(field.newDfp("-1").sqrt(),
             nan,
             DfpField.FLAG_INVALID, "Sqrt #7");

        test(pinf.sqrt(),
             pinf,
             0, "Sqrt #8");

        test(field.newDfp((byte) 1, Dfp.QNAN).sqrt(),
             nan,
             0, "Sqrt #9");

        test(field.newDfp((byte) 1, Dfp.SNAN).sqrt(),
             nan,
             DfpField.FLAG_INVALID, "Sqrt #9");
    }

// org.apache.commons.math3.dfp.DfpTest::testIssue567
    public void testIssue567() {
        DfpField field = new DfpField(100);
        Assert.assertEquals(0.0, field.getZero().toDouble(), Precision.SAFE_MIN);
        Assert.assertEquals(0.0, field.newDfp(0.0).toDouble(), Precision.SAFE_MIN);
        Assert.assertEquals(-1, FastMath.copySign(1, field.newDfp(-0.0).toDouble()), Precision.EPSILON);
        Assert.assertEquals(+1, FastMath.copySign(1, field.newDfp(+0.0).toDouble()), Precision.EPSILON);
    }

// org.apache.commons.math3.dfp.DfpTest::testIsZero
    public void testIsZero() {
        Assert.assertTrue(field.getZero().isZero());
        Assert.assertTrue(field.getZero().negate().isZero());
        Assert.assertTrue(field.newDfp(+0.0).isZero());
        Assert.assertTrue(field.newDfp(-0.0).isZero());
        Assert.assertFalse(field.newDfp(1.0e-90).isZero());
        Assert.assertFalse(nan.isZero());
        Assert.assertFalse(nan.negate().isZero());
        Assert.assertFalse(pinf.isZero());
        Assert.assertFalse(pinf.negate().isZero());
        Assert.assertFalse(ninf.isZero());
        Assert.assertFalse(ninf.negate().isZero());
    }

// org.apache.commons.math3.dfp.DfpTest::testSignPredicates
    public void testSignPredicates() {

        Assert.assertTrue(field.getZero().negativeOrNull());
        Assert.assertTrue(field.getZero().positiveOrNull());
        Assert.assertFalse(field.getZero().strictlyNegative());
        Assert.assertFalse(field.getZero().strictlyPositive());

        Assert.assertTrue(field.getZero().negate().negativeOrNull());
        Assert.assertTrue(field.getZero().negate().positiveOrNull());
        Assert.assertFalse(field.getZero().negate().strictlyNegative());
        Assert.assertFalse(field.getZero().negate().strictlyPositive());

        Assert.assertFalse(field.getOne().negativeOrNull());
        Assert.assertTrue(field.getOne().positiveOrNull());
        Assert.assertFalse(field.getOne().strictlyNegative());
        Assert.assertTrue(field.getOne().strictlyPositive());

        Assert.assertTrue(field.getOne().negate().negativeOrNull());
        Assert.assertFalse(field.getOne().negate().positiveOrNull());
        Assert.assertTrue(field.getOne().negate().strictlyNegative());
        Assert.assertFalse(field.getOne().negate().strictlyPositive());

        Assert.assertFalse(nan.negativeOrNull());
        Assert.assertFalse(nan.positiveOrNull());
        Assert.assertFalse(nan.strictlyNegative());
        Assert.assertFalse(nan.strictlyPositive());

        Assert.assertFalse(nan.negate().negativeOrNull());
        Assert.assertFalse(nan.negate().positiveOrNull());
        Assert.assertFalse(nan.negate().strictlyNegative());
        Assert.assertFalse(nan.negate().strictlyPositive());

        Assert.assertFalse(pinf.negativeOrNull());
        Assert.assertTrue(pinf.positiveOrNull());
        Assert.assertFalse(pinf.strictlyNegative());
        Assert.assertTrue(pinf.strictlyPositive());

        Assert.assertTrue(pinf.negate().negativeOrNull());
        Assert.assertFalse(pinf.negate().positiveOrNull());
        Assert.assertTrue(pinf.negate().strictlyNegative());
        Assert.assertFalse(pinf.negate().strictlyPositive());

        Assert.assertTrue(ninf.negativeOrNull());
        Assert.assertFalse(ninf.positiveOrNull());
        Assert.assertTrue(ninf.strictlyNegative());
        Assert.assertFalse(ninf.strictlyPositive());

        Assert.assertFalse(ninf.negate().negativeOrNull());
        Assert.assertTrue(ninf.negate().positiveOrNull());
        Assert.assertFalse(ninf.negate().strictlyNegative());
        Assert.assertTrue(ninf.negate().strictlyPositive());

    }

// org.apache.commons.math3.util.FastMathTest::testMinMaxDouble
    public void testMinMaxDouble() {
        double[][] pairs = {
            { -50.0, 50.0 },
            {  Double.POSITIVE_INFINITY, 1.0 },
            {  Double.NEGATIVE_INFINITY, 1.0 },
            {  Double.NaN, 1.0 },
            {  Double.POSITIVE_INFINITY, 0.0 },
            {  Double.NEGATIVE_INFINITY, 0.0 },
            {  Double.NaN, 0.0 },
            {  Double.NaN, Double.NEGATIVE_INFINITY },
            {  Double.NaN, Double.POSITIVE_INFINITY },
            { Precision.SAFE_MIN, Precision.EPSILON }
        };
        for (double[] pair : pairs) {
            Assert.assertEquals("min(" + pair[0] + ", " + pair[1] + ")",
                                Math.min(pair[0], pair[1]),
                                FastMath.min(pair[0], pair[1]),
                                Precision.EPSILON);
            Assert.assertEquals("min(" + pair[1] + ", " + pair[0] + ")",
                                Math.min(pair[1], pair[0]),
                                FastMath.min(pair[1], pair[0]),
                                Precision.EPSILON);
            Assert.assertEquals("max(" + pair[0] + ", " + pair[1] + ")",
                                Math.max(pair[0], pair[1]),
                                FastMath.max(pair[0], pair[1]),
                                Precision.EPSILON);
            Assert.assertEquals("max(" + pair[1] + ", " + pair[0] + ")",
                                Math.max(pair[1], pair[0]),
                                FastMath.max(pair[1], pair[0]),
                                Precision.EPSILON);
        }
    }

// org.apache.commons.math3.util.FastMathTest::testMinMaxFloat
    public void testMinMaxFloat() {
        float[][] pairs = {
            { -50.0f, 50.0f },
            {  Float.POSITIVE_INFINITY, 1.0f },
            {  Float.NEGATIVE_INFINITY, 1.0f },
            {  Float.NaN, 1.0f },
            {  Float.POSITIVE_INFINITY, 0.0f },
            {  Float.NEGATIVE_INFINITY, 0.0f },
            {  Float.NaN, 0.0f },
            {  Float.NaN, Float.NEGATIVE_INFINITY },
            {  Float.NaN, Float.POSITIVE_INFINITY }
        };
        for (float[] pair : pairs) {
            Assert.assertEquals("min(" + pair[0] + ", " + pair[1] + ")",
                                Math.min(pair[0], pair[1]),
                                FastMath.min(pair[0], pair[1]),
                                Precision.EPSILON);
            Assert.assertEquals("min(" + pair[1] + ", " + pair[0] + ")",
                                Math.min(pair[1], pair[0]),
                                FastMath.min(pair[1], pair[0]),
                                Precision.EPSILON);
            Assert.assertEquals("max(" + pair[0] + ", " + pair[1] + ")",
                                Math.max(pair[0], pair[1]),
                                FastMath.max(pair[0], pair[1]),
                                Precision.EPSILON);
            Assert.assertEquals("max(" + pair[1] + ", " + pair[0] + ")",
                                Math.max(pair[1], pair[0]),
                                FastMath.max(pair[1], pair[0]),
                                Precision.EPSILON);
        }
    }

// org.apache.commons.math3.util.FastMathTest::testConstants
    public void testConstants() {
        Assert.assertEquals(Math.PI, FastMath.PI, 1.0e-20);
        Assert.assertEquals(Math.E, FastMath.E, 1.0e-20);
    }

// org.apache.commons.math3.util.FastMathTest::testAtan2
    public void testAtan2() {
        double y1 = 1.2713504628280707e10;
        double x1 = -5.674940885228782e-10;
        Assert.assertEquals(Math.atan2(y1, x1), FastMath.atan2(y1, x1), 2 * Precision.EPSILON);
        double y2 = 0.0;
        double x2 = Double.POSITIVE_INFINITY;
        Assert.assertEquals(Math.atan2(y2, x2), FastMath.atan2(y2, x2), Precision.SAFE_MIN);
    }

// org.apache.commons.math3.util.FastMathTest::testHyperbolic
    public void testHyperbolic() {
        double maxErr = 0;
        for (double x = -30; x < 30; x += 0.001) {
            double tst = FastMath.sinh(x);
            double ref = Math.sinh(x);
            maxErr = FastMath.max(maxErr, FastMath.abs(ref - tst) / FastMath.ulp(ref));
        }
        Assert.assertEquals(0, maxErr, 2);

        maxErr = 0;
        for (double x = -30; x < 30; x += 0.001) {
            double tst = FastMath.cosh(x);
            double ref = Math.cosh(x);
            maxErr = FastMath.max(maxErr, FastMath.abs(ref - tst) / FastMath.ulp(ref));
        }
        Assert.assertEquals(0, maxErr, 2);

        maxErr = 0;
        for (double x = -0.5; x < 0.5; x += 0.001) {
            double tst = FastMath.tanh(x);
            double ref = Math.tanh(x);
            maxErr = FastMath.max(maxErr, FastMath.abs(ref - tst) / FastMath.ulp(ref));
        }
        Assert.assertEquals(0, maxErr, 4);

    }

// org.apache.commons.math3.util.FastMathTest::testHyperbolicInverses
    public void testHyperbolicInverses() {
        double maxErr = 0;
        for (double x = -30; x < 30; x += 0.01) {
            maxErr = FastMath.max(maxErr, FastMath.abs(x - FastMath.sinh(FastMath.asinh(x))) / (2 * FastMath.ulp(x)));
        }
        Assert.assertEquals(0, maxErr, 3);

        maxErr = 0;
        for (double x = 1; x < 30; x += 0.01) {
            maxErr = FastMath.max(maxErr, FastMath.abs(x - FastMath.cosh(FastMath.acosh(x))) / (2 * FastMath.ulp(x)));
        }
        Assert.assertEquals(0, maxErr, 2);

        maxErr = 0;
        for (double x = -1 + Precision.EPSILON; x < 1 - Precision.EPSILON; x += 0.0001) {
            maxErr = FastMath.max(maxErr, FastMath.abs(x - FastMath.tanh(FastMath.atanh(x))) / (2 * FastMath.ulp(x)));
        }
        Assert.assertEquals(0, maxErr, 2);

    }

// org.apache.commons.math3.util.FastMathTest::testLogAccuracy
    public void testLogAccuracy() {
        double maxerrulp = 0.0;

        for (int i = 0; i < NUMBER_OF_TRIALS; i++) {
            double x = Math.exp(generator.nextDouble() * 1416.0 - 708.0) * generator.nextDouble();
            
            double tst = FastMath.log(x);
            double ref = DfpMath.log(field.newDfp(x)).toDouble();
            double err = (tst - ref) / ref;

            if (err != 0.0) {
                double ulp = Math.abs(ref -
                                      Double.longBitsToDouble((Double
                                          .doubleToLongBits(ref) ^ 1)));
                double errulp = field.newDfp(tst).subtract(DfpMath.log(field.newDfp(x))).divide(field.newDfp(ulp)).toDouble();

                maxerrulp = Math.max(maxerrulp, Math.abs(errulp));
            }
        }

        Assert.assertTrue("log() had errors in excess of " + MAX_ERROR_ULP + " ULP", maxerrulp < MAX_ERROR_ULP);
    }

// org.apache.commons.math3.util.FastMathTest::testLog10Accuracy
    public void testLog10Accuracy() {
        double maxerrulp = 0.0;

        for (int i = 0; i < NUMBER_OF_TRIALS; i++) {
            double x = Math.exp(generator.nextDouble() * 1416.0 - 708.0) * generator.nextDouble();
            
            double tst = FastMath.log10(x);
            double ref = DfpMath.log(field.newDfp(x)).divide(DfpMath.log(field.newDfp("10"))).toDouble();
            double err = (tst - ref) / ref;

            if (err != 0.0) {
                double ulp = Math.abs(ref -
                                      Double.longBitsToDouble((Double.doubleToLongBits(ref) ^ 1)));
                double errulp = field.newDfp(tst).subtract(DfpMath.log(field.newDfp(x)).divide(DfpMath.log(field.newDfp("10")))).divide(field.newDfp(ulp)).toDouble();

                maxerrulp = Math.max(maxerrulp, Math.abs(errulp));
            }
        }

        Assert.assertTrue("log10() had errors in excess of " + MAX_ERROR_ULP + " ULP", maxerrulp < MAX_ERROR_ULP);
    }

// org.apache.commons.math3.util.FastMathTest::testLog1pAccuracy
    public void testLog1pAccuracy() {
        double maxerrulp = 0.0;

        for (int i = 0; i < NUMBER_OF_TRIALS; i++) {
            double x = Math.exp(generator.nextDouble() * 10.0 - 5.0) * generator.nextDouble();
            
            double tst = FastMath.log1p(x);
            double ref = DfpMath.log(field.newDfp(x).add(field.getOne())).toDouble();
            double err = (tst - ref) / ref;

            if (err != 0.0) {
                double ulp = Math.abs(ref -
                                      Double.longBitsToDouble((Double.doubleToLongBits(ref) ^ 1)));
                double errulp = field.newDfp(tst).subtract(DfpMath.log(field.newDfp(x).add(field.getOne()))).divide(field.newDfp(ulp)).toDouble();

                maxerrulp = Math.max(maxerrulp, Math.abs(errulp));
            }
        }

        Assert.assertTrue("log1p() had errors in excess of " + MAX_ERROR_ULP + " ULP", maxerrulp < MAX_ERROR_ULP);
    }

// org.apache.commons.math3.util.FastMathTest::testLog1pSpecialCases
    public void testLog1pSpecialCases() {

        Assert.assertTrue("Logp of -1.0 should be -Inf", Double.isInfinite(FastMath.log1p(-1.0)));

    }

// org.apache.commons.math3.util.FastMathTest::testLogSpecialCases
    public void testLogSpecialCases() {

        Assert.assertTrue("Log of zero should be -Inf", Double.isInfinite(FastMath.log(0.0)));

        Assert.assertTrue("Log of -zero should be -Inf", Double.isInfinite(FastMath.log(-0.0)));

        Assert.assertTrue("Log of NaN should be NaN", Double.isNaN(FastMath.log(Double.NaN)));

        Assert.assertTrue("Log of negative number should be NaN", Double.isNaN(FastMath.log(-1.0)));

        Assert.assertEquals("Log of Double.MIN_VALUE should be -744.4400719213812", -744.4400719213812, FastMath.log(Double.MIN_VALUE), Precision.EPSILON);

        Assert.assertTrue("Log of infinity should be infinity", Double.isInfinite(FastMath.log(Double.POSITIVE_INFINITY)));
    }

// org.apache.commons.math3.util.FastMathTest::testExpSpecialCases
    public void testExpSpecialCases() {

        
        Assert.assertEquals(Double.MIN_VALUE, FastMath.exp(-745.1332191019411), Precision.EPSILON);

        Assert.assertEquals("exp(-745.1332191019412) should be 0.0", 0.0, FastMath.exp(-745.1332191019412), Precision.EPSILON);

        Assert.assertTrue("exp of NaN should be NaN", Double.isNaN(FastMath.exp(Double.NaN)));

        Assert.assertTrue("exp of infinity should be infinity", Double.isInfinite(FastMath.exp(Double.POSITIVE_INFINITY)));

        Assert.assertEquals("exp of -infinity should be 0.0", 0.0, FastMath.exp(Double.NEGATIVE_INFINITY), Precision.EPSILON);

        Assert.assertEquals("exp(1) should be Math.E", Math.E, FastMath.exp(1.0), Precision.EPSILON);
    }

// org.apache.commons.math3.util.FastMathTest::testPowSpecialCases
    public void testPowSpecialCases() {

        Assert.assertEquals("pow(-1, 0) should be 1.0", 1.0, FastMath.pow(-1.0, 0.0), Precision.EPSILON);

        Assert.assertEquals("pow(-1, -0) should be 1.0", 1.0, FastMath.pow(-1.0, -0.0), Precision.EPSILON);

        Assert.assertEquals("pow(PI, 1.0) should be PI", FastMath.PI, FastMath.pow(FastMath.PI, 1.0), Precision.EPSILON);

        Assert.assertEquals("pow(-PI, 1.0) should be -PI", -FastMath.PI, FastMath.pow(-FastMath.PI, 1.0), Precision.EPSILON);

        Assert.assertTrue("pow(PI, NaN) should be NaN", Double.isNaN(FastMath.pow(Math.PI, Double.NaN)));

        Assert.assertTrue("pow(NaN, PI) should be NaN", Double.isNaN(FastMath.pow(Double.NaN, Math.PI)));

        Assert.assertTrue("pow(2.0, Infinity) should be Infinity", Double.isInfinite(FastMath.pow(2.0, Double.POSITIVE_INFINITY)));

        Assert.assertTrue("pow(0.5, -Infinity) should be Infinity", Double.isInfinite(FastMath.pow(0.5, Double.NEGATIVE_INFINITY)));

        Assert.assertEquals("pow(0.5, Infinity) should be 0.0", 0.0, FastMath.pow(0.5, Double.POSITIVE_INFINITY), Precision.EPSILON);

        Assert.assertEquals("pow(2.0, -Infinity) should be 0.0", 0.0, FastMath.pow(2.0, Double.NEGATIVE_INFINITY), Precision.EPSILON);

        Assert.assertEquals("pow(0.0, 0.5) should be 0.0", 0.0, FastMath.pow(0.0, 0.5), Precision.EPSILON);

        Assert.assertEquals("pow(Infinity, -0.5) should be 0.0", 0.0, FastMath.pow(Double.POSITIVE_INFINITY, -0.5), Precision.EPSILON);

        Assert.assertTrue("pow(0.0, -0.5) should be Inf", Double.isInfinite(FastMath.pow(0.0, -0.5)));

        Assert.assertTrue("pow(Inf, 0.5) should be Inf", Double.isInfinite(FastMath.pow(Double.POSITIVE_INFINITY, 0.5)));

        Assert.assertTrue("pow(-0.0, -3.0) should be -Inf", Double.isInfinite(FastMath.pow(-0.0, -3.0)));

        Assert.assertTrue("pow(-Inf, -3.0) should be -Inf", Double.isInfinite(FastMath.pow(Double.NEGATIVE_INFINITY, 3.0)));

        Assert.assertTrue("pow(-0.0, -3.5) should be Inf", Double.isInfinite(FastMath.pow(-0.0, -3.5)));

        Assert.assertTrue("pow(Inf, 3.5) should be Inf", Double.isInfinite(FastMath.pow(Double.POSITIVE_INFINITY, 3.5)));

        Assert.assertEquals("pow(-2.0, 3.0) should be -8.0", -8.0, FastMath.pow(-2.0, 3.0), Precision.EPSILON);

        Assert.assertTrue("pow(-2.0, 3.5) should be NaN", Double.isNaN(FastMath.pow(-2.0, 3.5)));

        

        Assert.assertTrue("pow(+Inf, NaN) should be NaN", Double.isNaN(FastMath.pow(Double.POSITIVE_INFINITY, Double.NaN)));

        Assert.assertTrue("pow(1.0, +Inf) should be NaN", Double.isNaN(FastMath.pow(1.0, Double.POSITIVE_INFINITY)));

        Assert.assertTrue("pow(-Inf, NaN) should be NaN", Double.isNaN(FastMath.pow(Double.NEGATIVE_INFINITY, Double.NaN)));

        Assert.assertEquals("pow(-Inf, -1.0) should be 0.0", 0.0, FastMath.pow(Double.NEGATIVE_INFINITY, -1.0), Precision.EPSILON);

        Assert.assertEquals("pow(-Inf, -2.0) should be 0.0", 0.0, FastMath.pow(Double.NEGATIVE_INFINITY, -2.0), Precision.EPSILON);

        Assert.assertTrue("pow(-Inf, 1.0) should be -Inf", Double.isInfinite(FastMath.pow(Double.NEGATIVE_INFINITY, 1.0)));

        Assert.assertTrue("pow(-Inf, 2.0) should be +Inf", Double.isInfinite(FastMath.pow(Double.NEGATIVE_INFINITY, 2.0)));

        Assert.assertTrue("pow(1.0, -Inf) should be NaN", Double.isNaN(FastMath.pow(1.0, Double.NEGATIVE_INFINITY)));

    }

// org.apache.commons.math3.util.FastMathTest::testAtan2SpecialCases
    public void testAtan2SpecialCases() {

        Assert.assertTrue("atan2(NaN, 0.0) should be NaN", Double.isNaN(FastMath.atan2(Double.NaN, 0.0)));

        Assert.assertTrue("atan2(0.0, NaN) should be NaN", Double.isNaN(FastMath.atan2(0.0, Double.NaN)));

        Assert.assertEquals("atan2(0.0, 0.0) should be 0.0", 0.0, FastMath.atan2(0.0, 0.0), Precision.EPSILON);

        Assert.assertEquals("atan2(0.0, 0.001) should be 0.0", 0.0, FastMath.atan2(0.0, 0.001), Precision.EPSILON);

        Assert.assertEquals("atan2(0.1, +Inf) should be 0.0", 0.0, FastMath.atan2(0.1, Double.POSITIVE_INFINITY), Precision.EPSILON);

        Assert.assertEquals("atan2(-0.0, 0.0) should be -0.0", -0.0, FastMath.atan2(-0.0, 0.0), Precision.EPSILON);

        Assert.assertEquals("atan2(-0.0, 0.001) should be -0.0", -0.0, FastMath.atan2(-0.0, 0.001), Precision.EPSILON);

        Assert.assertEquals("atan2(-0.0, +Inf) should be -0.0", -0.0, FastMath.atan2(-0.1, Double.POSITIVE_INFINITY), Precision.EPSILON);

        Assert.assertEquals("atan2(0.0, -0.0) should be PI", FastMath.PI, FastMath.atan2(0.0, -0.0), Precision.EPSILON);

        Assert.assertEquals("atan2(0.1, -Inf) should be PI", FastMath.PI, FastMath.atan2(0.1, Double.NEGATIVE_INFINITY), Precision.EPSILON);

        Assert.assertEquals("atan2(-0.0, -0.0) should be -PI", -FastMath.PI, FastMath.atan2(-0.0, -0.0), Precision.EPSILON);

        Assert.assertEquals("atan2(0.1, -Inf) should be -PI", -FastMath.PI, FastMath.atan2(-0.1, Double.NEGATIVE_INFINITY), Precision.EPSILON);

        Assert.assertEquals("atan2(0.1, 0.0) should be PI/2", FastMath.PI / 2.0, FastMath.atan2(0.1, 0.0), Precision.EPSILON);

        Assert.assertEquals("atan2(0.1, -0.0) should be PI/2", FastMath.PI / 2.0, FastMath.atan2(0.1, -0.0), Precision.EPSILON);

        Assert.assertEquals("atan2(Inf, 0.1) should be PI/2", FastMath.PI / 2.0, FastMath.atan2(Double.POSITIVE_INFINITY, 0.1), Precision.EPSILON);

        Assert.assertEquals("atan2(Inf, -0.1) should be PI/2", FastMath.PI / 2.0, FastMath.atan2(Double.POSITIVE_INFINITY, -0.1), Precision.EPSILON);

        Assert.assertEquals("atan2(-0.1, 0.0) should be -PI/2", -FastMath.PI / 2.0, FastMath.atan2(-0.1, 0.0), Precision.EPSILON);

        Assert.assertEquals("atan2(-0.1, -0.0) should be -PI/2", -FastMath.PI / 2.0, FastMath.atan2(-0.1, -0.0), Precision.EPSILON);

        Assert.assertEquals("atan2(-Inf, 0.1) should be -PI/2", -FastMath.PI / 2.0, FastMath.atan2(Double.NEGATIVE_INFINITY, 0.1), Precision.EPSILON);

        Assert.assertEquals("atan2(-Inf, -0.1) should be -PI/2", -FastMath.PI / 2.0, FastMath.atan2(Double.NEGATIVE_INFINITY, -0.1), Precision.EPSILON);

        Assert.assertEquals("atan2(Inf, Inf) should be PI/4", FastMath.PI / 4.0, FastMath.atan2(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY),
                Precision.EPSILON);

        Assert.assertEquals("atan2(Inf, -Inf) should be PI * 3/4", FastMath.PI * 3.0 / 4.0,
                FastMath.atan2(Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY), Precision.EPSILON);

        Assert.assertEquals("atan2(-Inf, Inf) should be -PI/4", -FastMath.PI / 4.0, FastMath.atan2(Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY),
                Precision.EPSILON);

        Assert.assertEquals("atan2(-Inf, -Inf) should be -PI * 3/4", - FastMath.PI * 3.0 / 4.0,
                FastMath.atan2(Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY), Precision.EPSILON);
    }

// org.apache.commons.math3.util.FastMathTest::testPowAccuracy
    public void testPowAccuracy() {
        double maxerrulp = 0.0;

        for (int i = 0; i < NUMBER_OF_TRIALS; i++) {
            double x = (generator.nextDouble() * 2.0 + 0.25);
            double y = (generator.nextDouble() * 1200.0 - 600.0) * generator.nextDouble();
            

            
            double tst = FastMath.pow(x, y);
            double ref = DfpMath.pow(field.newDfp(x), field.newDfp(y)).toDouble();
            double err = (tst - ref) / ref;

            if (err != 0) {
                double ulp = Math.abs(ref -
                                      Double.longBitsToDouble((Double
                                          .doubleToLongBits(ref) ^ 1)));
                double errulp = field.newDfp(tst).subtract(DfpMath.pow(field.newDfp(x), field.newDfp(y))).divide(field.newDfp(ulp)).toDouble();

                maxerrulp = Math.max(maxerrulp, Math.abs(errulp));
            }
        }

        Assert.assertTrue("pow() had errors in excess of " + MAX_ERROR_ULP + " ULP", maxerrulp < MAX_ERROR_ULP);
    }

// org.apache.commons.math3.util.FastMathTest::testExpAccuracy
    public void testExpAccuracy() {
        double maxerrulp = 0.0;

        for (int i = 0; i < NUMBER_OF_TRIALS; i++) {
            
            double x = ((generator.nextDouble() * 1416.0) - 708.0) * generator.nextDouble();
            
            
            
            double tst = FastMath.exp(x);
            double ref = DfpMath.exp(field.newDfp(x)).toDouble();
            double err = (tst - ref) / ref;

            if (err != 0) {
                double ulp = Math.abs(ref -
                                      Double.longBitsToDouble((Double.doubleToLongBits(ref) ^ 1)));
                double errulp = field.newDfp(tst).subtract(DfpMath.exp(field.newDfp(x))).divide(field.newDfp(ulp)).toDouble();

                maxerrulp = Math.max(maxerrulp, Math.abs(errulp));
            }
        }

        Assert.assertTrue("exp() had errors in excess of " + MAX_ERROR_ULP + " ULP", maxerrulp < MAX_ERROR_ULP);
    }

// org.apache.commons.math3.util.FastMathTest::testSinAccuracy
    public void testSinAccuracy() {
        double maxerrulp = 0.0;

        for (int i = 0; i < NUMBER_OF_TRIALS; i++) {
            
            
            double x = ((generator.nextDouble() * Math.PI) - Math.PI / 2.0) *
                       Math.pow(2, 21) * generator.nextDouble();
            
            
            
            double tst = FastMath.sin(x);
            double ref = DfpMath.sin(field.newDfp(x)).toDouble();
            double err = (tst - ref) / ref;

            if (err != 0) {
                double ulp = Math.abs(ref -
                                      Double.longBitsToDouble((Double.doubleToLongBits(ref) ^ 1)));
                double errulp = field.newDfp(tst).subtract(DfpMath.sin(field.newDfp(x))).divide(field.newDfp(ulp)).toDouble();

                maxerrulp = Math.max(maxerrulp, Math.abs(errulp));
            }
        }

        Assert.assertTrue("sin() had errors in excess of " + MAX_ERROR_ULP + " ULP", maxerrulp < MAX_ERROR_ULP);
    }

// org.apache.commons.math3.util.FastMathTest::testCosAccuracy
    public void testCosAccuracy() {
        double maxerrulp = 0.0;

        for (int i = 0; i < NUMBER_OF_TRIALS; i++) {
            
            
            double x = ((generator.nextDouble() * Math.PI) - Math.PI / 2.0) *
                       Math.pow(2, 21) * generator.nextDouble();
            
            
            
            double tst = FastMath.cos(x);
            double ref = DfpMath.cos(field.newDfp(x)).toDouble();
            double err = (tst - ref) / ref;

            if (err != 0) {
                double ulp = Math.abs(ref -
                                      Double.longBitsToDouble((Double.doubleToLongBits(ref) ^ 1)));
                double errulp = field.newDfp(tst).subtract(DfpMath.cos(field.newDfp(x))).divide(field.newDfp(ulp)).toDouble();

                maxerrulp = Math.max(maxerrulp, Math.abs(errulp));
            }
        }

        Assert.assertTrue("cos() had errors in excess of " + MAX_ERROR_ULP + " ULP", maxerrulp < MAX_ERROR_ULP);
    }

// org.apache.commons.math3.util.FastMathTest::testTanAccuracy
    public void testTanAccuracy() {
        double maxerrulp = 0.0;

        for (int i = 0; i < NUMBER_OF_TRIALS; i++) {
            
            
            double x = ((generator.nextDouble() * Math.PI) - Math.PI / 2.0) *
                       Math.pow(2, 12) * generator.nextDouble();
            
            
            
            double tst = FastMath.tan(x);
            double ref = DfpMath.tan(field.newDfp(x)).toDouble();
            double err = (tst - ref) / ref;

            if (err != 0) {
                double ulp = Math.abs(ref -
                                      Double.longBitsToDouble((Double.doubleToLongBits(ref) ^ 1)));
                double errulp = field.newDfp(tst).subtract(DfpMath.tan(field.newDfp(x))).divide(field.newDfp(ulp)).toDouble();

                maxerrulp = Math.max(maxerrulp, Math.abs(errulp));
            }
        }

        Assert.assertTrue("tan() had errors in excess of " + MAX_ERROR_ULP + " ULP", maxerrulp < MAX_ERROR_ULP);
    }

// org.apache.commons.math3.util.FastMathTest::testAtanAccuracy
    public void testAtanAccuracy() {
        double maxerrulp = 0.0;

        for (int i = 0; i < NUMBER_OF_TRIALS; i++) {
            
            
            
            
            double x = ((generator.nextDouble() * 16.0) - 8.0) * generator.nextDouble();

            
            
            
            double tst = FastMath.atan(x);
            double ref = DfpMath.atan(field.newDfp(x)).toDouble();
            double err = (tst - ref) / ref;

            if (err != 0) {
                double ulp = Math.abs(ref -
                                      Double.longBitsToDouble((Double.doubleToLongBits(ref) ^ 1)));
                double errulp = field.newDfp(tst).subtract(DfpMath.atan(field.newDfp(x))).divide(field.newDfp(ulp)).toDouble();

                maxerrulp = Math.max(maxerrulp, Math.abs(errulp));
            }
        }

        Assert.assertTrue("atan() had errors in excess of " + MAX_ERROR_ULP + " ULP", maxerrulp < MAX_ERROR_ULP);
    }

// org.apache.commons.math3.util.FastMathTest::testAtan2Accuracy
    public void testAtan2Accuracy() {
        double maxerrulp = 0.0;

        for (int i = 0; i < NUMBER_OF_TRIALS; i++) {
            
            
            double x = generator.nextDouble() - 0.5;
            double y = generator.nextDouble() - 0.5;
            
            
            
            double tst = FastMath.atan2(y, x);
            Dfp refdfp = DfpMath.atan(field.newDfp(y)
                .divide(field.newDfp(x)));
            
            if (x < 0.0) {
                if (y > 0.0)
                    refdfp = field.getPi().add(refdfp);
                else
                    refdfp = refdfp.subtract(field.getPi());
            }

            double ref = refdfp.toDouble();
            double err = (tst - ref) / ref;

            if (err != 0) {
                double ulp = Math.abs(ref -
                                      Double.longBitsToDouble((Double
                                          .doubleToLongBits(ref) ^ 1)));
                double errulp = field.newDfp(tst).subtract(refdfp).divide(field.newDfp(ulp)).toDouble();

                maxerrulp = Math.max(maxerrulp, Math.abs(errulp));
            }
        }

        Assert.assertTrue("atan2() had errors in excess of " + MAX_ERROR_ULP + " ULP", maxerrulp < MAX_ERROR_ULP);
    }

// org.apache.commons.math3.util.FastMathTest::testExpm1Accuracy
    public void testExpm1Accuracy() {
        double maxerrulp = 0.0;

        for (int i = 0; i < NUMBER_OF_TRIALS; i++) {
            
            
            double x = ((generator.nextDouble() * 16.0) - 8.0) * generator.nextDouble();
            
            double tst = FastMath.expm1(x);
            double ref = DfpMath.exp(field.newDfp(x)).subtract(field.getOne()).toDouble();
            double err = (tst - ref) / ref;

            if (err != 0) {
                double ulp = Math.abs(ref -
                                      Double.longBitsToDouble((Double
                                          .doubleToLongBits(ref) ^ 1)));
                double errulp = field.newDfp(tst).subtract(DfpMath.exp(field.newDfp(x)).subtract(field.getOne())).divide(field.newDfp(ulp)).toDouble();

                maxerrulp = Math.max(maxerrulp, Math.abs(errulp));
            }
        }

        Assert.assertTrue("expm1() had errors in excess of " + MAX_ERROR_ULP + " ULP", maxerrulp < MAX_ERROR_ULP);
    }

// org.apache.commons.math3.util.FastMathTest::testAsinAccuracy
    public void testAsinAccuracy() {
        double maxerrulp = 0.0;

        for (int i=0; i<10000; i++) {
            double x = ((generator.nextDouble() * 2.0) - 1.0) * generator.nextDouble();

            double tst = FastMath.asin(x);
            double ref = DfpMath.asin(field.newDfp(x)).toDouble();
            double err = (tst - ref) / ref;

            if (err != 0) {
                double ulp = Math.abs(ref - Double.longBitsToDouble((Double.doubleToLongBits(ref) ^ 1)));
                double errulp = field.newDfp(tst).subtract(DfpMath.asin(field.newDfp(x))).divide(field.newDfp(ulp)).toDouble();
                

                maxerrulp = Math.max(maxerrulp, Math.abs(errulp));
            }
        }

        Assert.assertTrue("asin() had errors in excess of " + MAX_ERROR_ULP + " ULP", maxerrulp < MAX_ERROR_ULP);
    }

// org.apache.commons.math3.util.FastMathTest::testAcosAccuracy
    public void testAcosAccuracy() {
        double maxerrulp = 0.0;

        for (int i=0; i<10000; i++) {
            double x = ((generator.nextDouble() * 2.0) - 1.0) * generator.nextDouble();

            double tst = FastMath.acos(x);
            double ref = DfpMath.acos(field.newDfp(x)).toDouble();
            double err = (tst - ref) / ref;

            if (err != 0) {
                double ulp = Math.abs(ref - Double.longBitsToDouble((Double.doubleToLongBits(ref) ^ 1)));
                double errulp = field.newDfp(tst).subtract(DfpMath.acos(field.newDfp(x))).divide(field.newDfp(ulp)).toDouble();
                

                maxerrulp = Math.max(maxerrulp, Math.abs(errulp));
            }
        }

        Assert.assertTrue("acos() had errors in excess of " + MAX_ERROR_ULP + " ULP", maxerrulp < MAX_ERROR_ULP);
    }

// org.apache.commons.math3.util.FastMathTest::testAcosSpecialCases
    public void testAcosSpecialCases() {
        
        Assert.assertTrue("acos(NaN) should be NaN", Double.isNaN(FastMath.acos(Double.NaN)));
        
        Assert.assertTrue("acos(-1.1) should be NaN", Double.isNaN(FastMath.acos(-1.1)));

        Assert.assertTrue("acos(-1.1) should be NaN", Double.isNaN(FastMath.acos(1.1)));
        
        Assert.assertEquals("acos(-1.0) should be PI", FastMath.acos(-1.0), FastMath.PI, Precision.EPSILON);

        Assert.assertEquals("acos(1.0) should be 0.0", FastMath.acos(1.0), 0.0, Precision.EPSILON);

        Assert.assertEquals("acos(0.0) should be PI/2", FastMath.acos(0.0), FastMath.PI / 2.0, Precision.EPSILON);
    }

// org.apache.commons.math3.util.FastMathTest::testAsinSpecialCases
    public void testAsinSpecialCases() {
   
        Assert.assertTrue("asin(NaN) should be NaN", Double.isNaN(FastMath.asin(Double.NaN)));
        
        Assert.assertTrue("asin(1.1) should be NaN", Double.isNaN(FastMath.asin(1.1)));
        
        Assert.assertTrue("asin(-1.1) should be NaN", Double.isNaN(FastMath.asin(-1.1)));
        
        Assert.assertEquals("asin(1.0) should be PI/2", FastMath.asin(1.0), FastMath.PI / 2.0, Precision.EPSILON);

        Assert.assertEquals("asin(-1.0) should be -PI/2", FastMath.asin(-1.0), -FastMath.PI / 2.0, Precision.EPSILON);

        Assert.assertEquals("asin(0.0) should be 0.0", FastMath.asin(0.0), 0.0, Precision.EPSILON);
    }

// org.apache.commons.math3.util.FastMathTest::testSinhAccuracy
    public void testSinhAccuracy() {
        double maxerrulp = 0.0;

        for (int i=0; i<10000; i++) {
            double x = ((generator.nextDouble() * 16.0) - 8.0) * generator.nextDouble();

            double tst = FastMath.sinh(x);
            double ref = sinh(field.newDfp(x)).toDouble();
            double err = (tst - ref) / ref;

            if (err != 0) {
                double ulp = Math.abs(ref - Double.longBitsToDouble((Double.doubleToLongBits(ref) ^ 1)));
                double errulp = field.newDfp(tst).subtract(sinh(field.newDfp(x))).divide(field.newDfp(ulp)).toDouble();
                
                maxerrulp = Math.max(maxerrulp, Math.abs(errulp));
            }
        }

        Assert.assertTrue("sinh() had errors in excess of " + MAX_ERROR_ULP + " ULP", maxerrulp < MAX_ERROR_ULP);
    }

// org.apache.commons.math3.util.FastMathTest::testCoshAccuracy
    public void testCoshAccuracy() {
        double maxerrulp = 0.0;

        for (int i=0; i<10000; i++) {
            double x = ((generator.nextDouble() * 16.0) - 8.0) * generator.nextDouble();

            double tst = FastMath.cosh(x);
            double ref = cosh(field.newDfp(x)).toDouble();
            double err = (tst - ref) / ref;

            if (err != 0) {
                double ulp = Math.abs(ref - Double.longBitsToDouble((Double.doubleToLongBits(ref) ^ 1)));
                double errulp = field.newDfp(tst).subtract(cosh(field.newDfp(x))).divide(field.newDfp(ulp)).toDouble();
                
                maxerrulp = Math.max(maxerrulp, Math.abs(errulp));
            }
        }

        Assert.assertTrue("cosh() had errors in excess of " + MAX_ERROR_ULP + " ULP", maxerrulp < MAX_ERROR_ULP);
    }

// org.apache.commons.math3.util.FastMathTest::testTanhAccuracy
    public void testTanhAccuracy() {
        double maxerrulp = 0.0;

        for (int i=0; i<10000; i++) {
            double x = ((generator.nextDouble() * 16.0) - 8.0) * generator.nextDouble();

            double tst = FastMath.tanh(x);
            double ref = tanh(field.newDfp(x)).toDouble();
            double err = (tst - ref) / ref;

            if (err != 0) {
                double ulp = Math.abs(ref - Double.longBitsToDouble((Double.doubleToLongBits(ref) ^ 1)));
                double errulp = field.newDfp(tst).subtract(tanh(field.newDfp(x))).divide(field.newDfp(ulp)).toDouble();
                
                maxerrulp = Math.max(maxerrulp, Math.abs(errulp));
            }
        }

        Assert.assertTrue("tanh() had errors in excess of " + MAX_ERROR_ULP + " ULP", maxerrulp < MAX_ERROR_ULP);
    }

// org.apache.commons.math3.util.FastMathTest::testCbrtAccuracy
    public void testCbrtAccuracy() {
        double maxerrulp = 0.0;

        for (int i=0; i<10000; i++) {
            double x = ((generator.nextDouble() * 200.0) - 100.0) * generator.nextDouble();

            double tst = FastMath.cbrt(x);
            double ref = cbrt(field.newDfp(x)).toDouble();
            double err = (tst - ref) / ref;

            if (err != 0) {
                double ulp = Math.abs(ref - Double.longBitsToDouble((Double.doubleToLongBits(ref) ^ 1)));
                double errulp = field.newDfp(tst).subtract(cbrt(field.newDfp(x))).divide(field.newDfp(ulp)).toDouble();
                
                maxerrulp = Math.max(maxerrulp, Math.abs(errulp));
            }
        }

        Assert.assertTrue("cbrt() had errors in excess of " + MAX_ERROR_ULP + " ULP", maxerrulp < MAX_ERROR_ULP);
    }

// org.apache.commons.math3.util.FastMathTest::testToDegrees
    public void testToDegrees() {
        double maxerrulp = 0.0;
        for (int i = 0; i < NUMBER_OF_TRIALS; i++) {
            double x = generator.nextDouble();
            double tst = field.newDfp(x).multiply(180).divide(field.getPi()).toDouble();
            double ref = FastMath.toDegrees(x);
            double err = (tst - ref) / ref;

            if (err != 0) {
                double ulp = Math.abs(ref -
                                      Double.longBitsToDouble((Double.doubleToLongBits(ref) ^ 1)));
                double errulp = field.newDfp(tst).subtract(DfpMath.exp(field.newDfp(x)).subtract(field.getOne())).divide(field.newDfp(ulp)).toDouble();

                maxerrulp = Math.max(maxerrulp, Math.abs(errulp));
            }
        }
        Assert.assertTrue("toDegrees() had errors in excess of " + MAX_ERROR_ULP + " ULP", maxerrulp < MAX_ERROR_ULP);

    }

// org.apache.commons.math3.util.FastMathTest::testToRadians
    public void testToRadians() {
        double maxerrulp = 0.0;
        for (int i = 0; i < NUMBER_OF_TRIALS; i++) {
            double x = generator.nextDouble();
            double tst = field.newDfp(x).multiply(field.getPi()).divide(180).toDouble();
            double ref = FastMath.toRadians(x);
            double err = (tst - ref) / ref;

            if (err != 0) {
                double ulp = Math.abs(ref -
                                      Double.longBitsToDouble((Double
                                          .doubleToLongBits(ref) ^ 1)));
                double errulp = field.newDfp(tst).subtract(DfpMath.exp(field.newDfp(x)).subtract(field.getOne())).divide(field.newDfp(ulp)).toDouble();

                maxerrulp = Math.max(maxerrulp, Math.abs(errulp));
            }
        }
        Assert.assertTrue("toRadians() had errors in excess of " + MAX_ERROR_ULP + " ULP", maxerrulp < MAX_ERROR_ULP);

    }

// org.apache.commons.math3.util.FastMathTest::testNextAfter
    public void testNextAfter() {
        
        Assert.assertEquals(16.0, FastMath.nextAfter(15.999999999999998, 34.27555555555555), 0.0);

        
        Assert.assertEquals(-15.999999999999996, FastMath.nextAfter(-15.999999999999998, 34.27555555555555), 0.0);

        
        Assert.assertEquals(15.999999999999996, FastMath.nextAfter(15.999999999999998, 2.142222222222222), 0.0);

        
        Assert.assertEquals(-15.999999999999996, FastMath.nextAfter(-15.999999999999998, 2.142222222222222), 0.0);

        
        Assert.assertEquals(8.000000000000002, FastMath.nextAfter(8.0, 34.27555555555555), 0.0);

        
        Assert.assertEquals(-7.999999999999999, FastMath.nextAfter(-8.0, 34.27555555555555), 0.0);

        
        Assert.assertEquals(7.999999999999999, FastMath.nextAfter(8.0, 2.142222222222222), 0.0);

        
        Assert.assertEquals(-7.999999999999999, FastMath.nextAfter(-8.0, 2.142222222222222), 0.0);

        
        Assert.assertEquals(2.308922399667661E-4, FastMath.nextAfter(2.3089223996676606E-4, 2.308922399667661E-4), 0.0);

        
        Assert.assertEquals(2.3089223996676606E-4, FastMath.nextAfter(2.3089223996676606E-4, 2.3089223996676606E-4), 0.0);

        
        Assert.assertEquals(2.3089223996676603E-4, FastMath.nextAfter(2.3089223996676606E-4, 2.3089223996676603E-4), 0.0);

        
        Assert.assertEquals(2.3089223996676603E-4, FastMath.nextAfter(2.3089223996676606E-4, -2.308922399667661E-4), 0.0);

        
        Assert.assertEquals(2.3089223996676603E-4, FastMath.nextAfter(2.3089223996676606E-4, -2.3089223996676606E-4), 0.0);

        
        Assert.assertEquals(2.3089223996676603E-4, FastMath.nextAfter(2.3089223996676606E-4, -2.3089223996676603E-4), 0.0);

        
        Assert.assertEquals(-2.3089223996676603E-4, FastMath.nextAfter(-2.3089223996676606E-4, 2.308922399667661E-4), 0.0);

        
        Assert.assertEquals(-2.3089223996676603E-4, FastMath.nextAfter(-2.3089223996676606E-4, 2.3089223996676606E-4), 0.0);

        
        Assert.assertEquals(-2.3089223996676603E-4, FastMath.nextAfter(-2.3089223996676606E-4, 2.3089223996676603E-4), 0.0);

        
        Assert.assertEquals(-2.308922399667661E-4, FastMath.nextAfter(-2.3089223996676606E-4, -2.308922399667661E-4), 0.0);

        
        Assert.assertEquals(-2.3089223996676606E-4, FastMath.nextAfter(-2.3089223996676606E-4, -2.3089223996676606E-4), 0.0);

        
        Assert.assertEquals(-2.3089223996676603E-4, FastMath.nextAfter(-2.3089223996676606E-4, -2.3089223996676603E-4), 0.0);

    }

// org.apache.commons.math3.util.FastMathTest::testDoubleNextAfterSpecialCases
    public void testDoubleNextAfterSpecialCases() {
        Assert.assertEquals(-Double.MAX_VALUE,FastMath.nextAfter(Double.NEGATIVE_INFINITY, 0D), 0D);
        Assert.assertEquals(Double.MAX_VALUE,FastMath.nextAfter(Double.POSITIVE_INFINITY, 0D), 0D);
        Assert.assertEquals(Double.NaN,FastMath.nextAfter(Double.NaN, 0D), 0D);
        Assert.assertEquals(Double.POSITIVE_INFINITY,FastMath.nextAfter(Double.MAX_VALUE, Double.POSITIVE_INFINITY), 0D);
        Assert.assertEquals(Double.NEGATIVE_INFINITY,FastMath.nextAfter(-Double.MAX_VALUE, Double.NEGATIVE_INFINITY), 0D);
        Assert.assertEquals(Double.MIN_VALUE, FastMath.nextAfter(0D, 1D), 0D);
        Assert.assertEquals(-Double.MIN_VALUE, FastMath.nextAfter(0D, -1D), 0D);
        Assert.assertEquals(0D, FastMath.nextAfter(Double.MIN_VALUE, -1), 0D);
        Assert.assertEquals(0D, FastMath.nextAfter(-Double.MIN_VALUE, 1), 0D);
    }

// org.apache.commons.math3.util.FastMathTest::testFloatNextAfterSpecialCases
    public void testFloatNextAfterSpecialCases() {
        Assert.assertEquals(-Float.MAX_VALUE,FastMath.nextAfter(Float.NEGATIVE_INFINITY, 0F), 0F);
        Assert.assertEquals(Float.MAX_VALUE,FastMath.nextAfter(Float.POSITIVE_INFINITY, 0F), 0F);
        Assert.assertEquals(Float.NaN,FastMath.nextAfter(Float.NaN, 0F), 0F);
        Assert.assertEquals(Float.POSITIVE_INFINITY,FastMath.nextAfter(Float.MAX_VALUE, Float.POSITIVE_INFINITY), 0F);
        Assert.assertEquals(Float.NEGATIVE_INFINITY,FastMath.nextAfter(-Float.MAX_VALUE, Float.NEGATIVE_INFINITY), 0F);
        Assert.assertEquals(Float.MIN_VALUE, FastMath.nextAfter(0F, 1F), 0F);
        Assert.assertEquals(-Float.MIN_VALUE, FastMath.nextAfter(0F, -1F), 0F);
        Assert.assertEquals(0F, FastMath.nextAfter(Float.MIN_VALUE, -1F), 0F);
        Assert.assertEquals(0F, FastMath.nextAfter(-Float.MIN_VALUE, 1F), 0F);
    }

// org.apache.commons.math3.util.FastMathTest::testDoubleScalbSpecialCases
    public void testDoubleScalbSpecialCases() {
        Assert.assertEquals(2.5269841324701218E-175,  FastMath.scalb(2.2250738585072014E-308, 442), 0D);
        Assert.assertEquals(1.307993905256674E297,    FastMath.scalb(1.1102230246251565E-16, 1040), 0D);
        Assert.assertEquals(7.2520887996488946E-217,  FastMath.scalb(Double.MIN_VALUE,        356), 0D);
        Assert.assertEquals(8.98846567431158E307,     FastMath.scalb(Double.MIN_VALUE,       2097), 0D);
        Assert.assertEquals(Double.POSITIVE_INFINITY, FastMath.scalb(Double.MIN_VALUE,       2098), 0D);
        Assert.assertEquals(1.1125369292536007E-308,  FastMath.scalb(2.225073858507201E-308,   -1), 0D);
        Assert.assertEquals(1.0E-323,                 FastMath.scalb(Double.MAX_VALUE,      -2097), 0D);
        Assert.assertEquals(Double.MIN_VALUE,         FastMath.scalb(Double.MAX_VALUE,      -2098), 0D);
        Assert.assertEquals(0,                        FastMath.scalb(Double.MAX_VALUE,      -2099), 0D);
        Assert.assertEquals(Double.POSITIVE_INFINITY, FastMath.scalb(Double.POSITIVE_INFINITY, -1000000), 0D);
        Assert.assertEquals(Double.NEGATIVE_INFINITY, FastMath.scalb(-1.1102230246251565E-16, 1078), 0D);
        Assert.assertEquals(Double.NEGATIVE_INFINITY, FastMath.scalb(-1.1102230246251565E-16,  1079), 0D);
        Assert.assertEquals(Double.NEGATIVE_INFINITY, FastMath.scalb(-2.2250738585072014E-308, 2047), 0D);
        Assert.assertEquals(Double.NEGATIVE_INFINITY, FastMath.scalb(-2.2250738585072014E-308, 2048), 0D);
        Assert.assertEquals(Double.NEGATIVE_INFINITY, FastMath.scalb(-1.7976931348623157E308,  2147483647), 0D);
        Assert.assertEquals(Double.POSITIVE_INFINITY, FastMath.scalb( 1.7976931348623157E308,  2147483647), 0D);
        Assert.assertEquals(Double.NEGATIVE_INFINITY, FastMath.scalb(-1.1102230246251565E-16,  2147483647), 0D);
        Assert.assertEquals(Double.POSITIVE_INFINITY, FastMath.scalb( 1.1102230246251565E-16,  2147483647), 0D);
        Assert.assertEquals(Double.NEGATIVE_INFINITY, FastMath.scalb(-2.2250738585072014E-308, 2147483647), 0D);
        Assert.assertEquals(Double.POSITIVE_INFINITY, FastMath.scalb( 2.2250738585072014E-308, 2147483647), 0D);
    }

// org.apache.commons.math3.util.FastMathTest::testFloatScalbSpecialCases
    public void testFloatScalbSpecialCases() {
        Assert.assertEquals(0f,                       FastMath.scalb(Float.MIN_VALUE,  -30), 0F);
        Assert.assertEquals(2 * Float.MIN_VALUE,      FastMath.scalb(Float.MIN_VALUE,    1), 0F);
        Assert.assertEquals(7.555786e22f,             FastMath.scalb(Float.MAX_VALUE,  -52), 0F);
        Assert.assertEquals(1.7014118e38f,            FastMath.scalb(Float.MIN_VALUE,  276), 0F);
        Assert.assertEquals(Float.POSITIVE_INFINITY,  FastMath.scalb(Float.MIN_VALUE,  277), 0F);
        Assert.assertEquals(5.8774718e-39f,           FastMath.scalb(1.1754944e-38f,    -1), 0F);
        Assert.assertEquals(2 * Float.MIN_VALUE,      FastMath.scalb(Float.MAX_VALUE, -276), 0F);
        Assert.assertEquals(Float.MIN_VALUE,          FastMath.scalb(Float.MAX_VALUE, -277), 0F);
        Assert.assertEquals(0,                        FastMath.scalb(Float.MAX_VALUE, -278), 0F);
        Assert.assertEquals(Float.POSITIVE_INFINITY,  FastMath.scalb(Float.POSITIVE_INFINITY, -1000000), 0F);
        Assert.assertEquals(-3.13994498e38f,          FastMath.scalb(-1.1e-7f,         151), 0F);
        Assert.assertEquals(Float.NEGATIVE_INFINITY,  FastMath.scalb(-1.1e-7f,         152), 0F);
        Assert.assertEquals(Float.POSITIVE_INFINITY,  FastMath.scalb(3.4028235E38f,  2147483647), 0F);
        Assert.assertEquals(Float.NEGATIVE_INFINITY,  FastMath.scalb(-3.4028235E38f, 2147483647), 0F);
    }

// org.apache.commons.math3.util.FastMathTest::checkMissingFastMathClasses
    public void checkMissingFastMathClasses() {}

// org.apache.commons.math3.util.FastMathTest::checkExtraFastMathClasses
    public void checkExtraFastMathClasses() {
        compareClassMethods( FastMath.class, StrictMath.class);
    }

// org.apache.commons.math3.util.FastMathTest::testSignumDouble
    public void testSignumDouble() {
        final double delta = 0.0;
        Assert.assertEquals(1.0, FastMath.signum(2.0), delta);
        Assert.assertEquals(0.0, FastMath.signum(0.0), delta);
        Assert.assertEquals(-1.0, FastMath.signum(-2.0), delta);
        TestUtils.assertSame(-0. / 0., FastMath.signum(Double.NaN));
    }

// org.apache.commons.math3.util.FastMathTest::testSignumFloat
    public void testSignumFloat() {
        final float delta = 0.0F;
        Assert.assertEquals(1.0F, FastMath.signum(2.0F), delta);
        Assert.assertEquals(0.0F, FastMath.signum(0.0F), delta);
        Assert.assertEquals(-1.0F, FastMath.signum(-2.0F), delta);
        TestUtils.assertSame(Float.NaN, FastMath.signum(Float.NaN));
    }

// org.apache.commons.math3.util.FastMathTest::testLogWithBase
    public void testLogWithBase() {
        Assert.assertEquals(2.0, FastMath.log(2, 4), 0);
        Assert.assertEquals(3.0, FastMath.log(2, 8), 0);
        Assert.assertTrue(Double.isNaN(FastMath.log(-1, 1)));
        Assert.assertTrue(Double.isNaN(FastMath.log(1, -1)));
        Assert.assertTrue(Double.isNaN(FastMath.log(0, 0)));
        Assert.assertEquals(0, FastMath.log(0, 10), 0);
        Assert.assertEquals(Double.NEGATIVE_INFINITY, FastMath.log(10, 0), 0);
    }

// org.apache.commons.math3.util.FastMathTest::testIndicatorDouble
    public void testIndicatorDouble() {
        double delta = 0.0;
        Assert.assertEquals(1.0, FastMath.copySign(1d, 2.0), delta);
        Assert.assertEquals(1.0, FastMath.copySign(1d, 0.0), delta);
        Assert.assertEquals(-1.0, FastMath.copySign(1d, -2.0), delta);
    }

// org.apache.commons.math3.util.FastMathTest::testIndicatorFloat
    public void testIndicatorFloat() {
        float delta = 0.0F;
        Assert.assertEquals(1.0F, FastMath.copySign(1d, 2.0F), delta);
        Assert.assertEquals(1.0F, FastMath.copySign(1d, 0.0F), delta);
        Assert.assertEquals(-1.0F, FastMath.copySign(1d, -2.0F), delta);
    }

// org.apache.commons.math3.util.FastMathTest::testIntPow
    public void testIntPow() {
        final int maxExp = 300;
        DfpField field = new DfpField(40);
        final double base = 1.23456789;
        Dfp baseDfp = field.newDfp(base);
        Dfp dfpPower = field.getOne();
        for (int i = 0; i < maxExp; i++) {
            Assert.assertEquals("exp=" + i, dfpPower.toDouble(), FastMath.pow(base, i),
                                0.6 * FastMath.ulp(dfpPower.toDouble()));
            dfpPower = dfpPower.multiply(baseDfp);
        }
    }

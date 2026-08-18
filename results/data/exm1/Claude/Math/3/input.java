// buggy code
    public static double linearCombination(final double[] a, final double[] b)
        throws DimensionMismatchException {
        final int len = a.length;
        if (len != b.length) {
            throw new DimensionMismatchException(len, b.length);
        }

            // Revert to scalar multiplication.

        final double[] prodHigh = new double[len];
        double prodLowSum = 0;

        for (int i = 0; i < len; i++) {
            final double ai = a[i];
            final double ca = SPLIT_FACTOR * ai;
            final double aHigh = ca - (ca - ai);
            final double aLow = ai - aHigh;

            final double bi = b[i];
            final double cb = SPLIT_FACTOR * bi;
            final double bHigh = cb - (cb - bi);
            final double bLow = bi - bHigh;
            prodHigh[i] = ai * bi;
            final double prodLow = aLow * bLow - (((prodHigh[i] -
                                                    aHigh * bHigh) -
                                                   aLow * bHigh) -
                                                  aHigh * bLow);
            prodLowSum += prodLow;
        }


        final double prodHighCur = prodHigh[0];
        double prodHighNext = prodHigh[1];
        double sHighPrev = prodHighCur + prodHighNext;
        double sPrime = sHighPrev - prodHighNext;
        double sLowSum = (prodHighNext - (sHighPrev - sPrime)) + (prodHighCur - sPrime);

        final int lenMinusOne = len - 1;
        for (int i = 1; i < lenMinusOne; i++) {
            prodHighNext = prodHigh[i + 1];
            final double sHighCur = sHighPrev + prodHighNext;
            sPrime = sHighCur - prodHighNext;
            sLowSum += (prodHighNext - (sHighCur - sPrime)) + (sHighPrev - sPrime);
            sHighPrev = sHighCur;
        }

        double result = sHighPrev + (prodLowSum + sLowSum);

        if (Double.isNaN(result)) {
            // either we have split infinite numbers or some coefficients were NaNs,
            // just rely on the naive implementation and let IEEE754 handle this
            result = 0;
            for (int i = 0; i < len; ++i) {
                result += a[i] * b[i];
            }
        }

        return result;
    }

// relevant test
// org.apache.commons.math3.analysis.differentiation.DerivativeStructureTest::testWrongVariableIndex
    public void testWrongVariableIndex() {
        new DerivativeStructure(3, 1, 3, 1.0);
    }

// org.apache.commons.math3.analysis.differentiation.DerivativeStructureTest::testMissingOrders
    public void testMissingOrders() {
        new DerivativeStructure(3, 1, 0, 1.0).getPartialDerivative(0, 1);
    }

// org.apache.commons.math3.analysis.differentiation.DerivativeStructureTest::testTooLargeOrder
    public void testTooLargeOrder() {
        new DerivativeStructure(3, 1, 0, 1.0).getPartialDerivative(1, 1, 2);
    }

// org.apache.commons.math3.analysis.differentiation.DerivativeStructureTest::testVariableWithoutDerivative0
    public void testVariableWithoutDerivative0() {
        DerivativeStructure v = new DerivativeStructure(1, 0, 0, 1.0);
        Assert.assertEquals(1.0, v.getValue(), 1.0e-15);
    }

// org.apache.commons.math3.analysis.differentiation.DerivativeStructureTest::testVariableWithoutDerivative1
    public void testVariableWithoutDerivative1() {
        DerivativeStructure v = new DerivativeStructure(1, 0, 0, 1.0);
        Assert.assertEquals(1.0, v.getPartialDerivative(1), 1.0e-15);
    }

// org.apache.commons.math3.analysis.differentiation.DerivativeStructureTest::testVariable
    public void testVariable() {
        for (int maxOrder = 1; maxOrder < 5; ++maxOrder) {
            checkF0F1(new DerivativeStructure(3, maxOrder, 0, 1.0),
                      1.0, 1.0, 0.0, 0.0);
            checkF0F1(new DerivativeStructure(3, maxOrder, 1, 2.0),
                      2.0, 0.0, 1.0, 0.0);
            checkF0F1(new DerivativeStructure(3, maxOrder, 2, 3.0),
                      3.0, 0.0, 0.0, 1.0);
        }
    }

// org.apache.commons.math3.analysis.differentiation.DerivativeStructureTest::testConstant
    public void testConstant() {
        for (int maxOrder = 1; maxOrder < 5; ++maxOrder) {
            checkF0F1(new DerivativeStructure(3, maxOrder, FastMath.PI),
                      FastMath.PI, 0.0, 0.0, 0.0);
        }
    }

// org.apache.commons.math3.analysis.differentiation.DerivativeStructureTest::testPrimitiveAdd
    public void testPrimitiveAdd() {
        for (int maxOrder = 1; maxOrder < 5; ++maxOrder) {
            checkF0F1(new DerivativeStructure(3, maxOrder, 0, 1.0).add(5), 6.0, 1.0, 0.0, 0.0);
            checkF0F1(new DerivativeStructure(3, maxOrder, 1, 2.0).add(5), 7.0, 0.0, 1.0, 0.0);
            checkF0F1(new DerivativeStructure(3, maxOrder, 2, 3.0).add(5), 8.0, 0.0, 0.0, 1.0);
        }
    }

// org.apache.commons.math3.analysis.differentiation.DerivativeStructureTest::testAdd
    public void testAdd() {
        for (int maxOrder = 1; maxOrder < 5; ++maxOrder) {
            DerivativeStructure x = new DerivativeStructure(3, maxOrder, 0, 1.0);
            DerivativeStructure y = new DerivativeStructure(3, maxOrder, 1, 2.0);
            DerivativeStructure z = new DerivativeStructure(3, maxOrder, 2, 3.0);
            DerivativeStructure xyz = x.add(y.add(z));
            checkF0F1(xyz, x.getValue() + y.getValue() + z.getValue(), 1.0, 1.0, 1.0);
        }
    }

// org.apache.commons.math3.analysis.differentiation.DerivativeStructureTest::testPrimitiveSubtract
    public void testPrimitiveSubtract() {
        for (int maxOrder = 1; maxOrder < 5; ++maxOrder) {
            checkF0F1(new DerivativeStructure(3, maxOrder, 0, 1.0).subtract(5), -4.0, 1.0, 0.0, 0.0);
            checkF0F1(new DerivativeStructure(3, maxOrder, 1, 2.0).subtract(5), -3.0, 0.0, 1.0, 0.0);
            checkF0F1(new DerivativeStructure(3, maxOrder, 2, 3.0).subtract(5), -2.0, 0.0, 0.0, 1.0);
        }
    }

// org.apache.commons.math3.analysis.differentiation.DerivativeStructureTest::testSubtract
    public void testSubtract() {
        for (int maxOrder = 1; maxOrder < 5; ++maxOrder) {
            DerivativeStructure x = new DerivativeStructure(3, maxOrder, 0, 1.0);
            DerivativeStructure y = new DerivativeStructure(3, maxOrder, 1, 2.0);
            DerivativeStructure z = new DerivativeStructure(3, maxOrder, 2, 3.0);
            DerivativeStructure xyz = x.subtract(y.subtract(z));
            checkF0F1(xyz, x.getValue() - (y.getValue() - z.getValue()), 1.0, -1.0, 1.0);
        }
    }

// org.apache.commons.math3.analysis.differentiation.DerivativeStructureTest::testPrimitiveMultiply
    public void testPrimitiveMultiply() {
        for (int maxOrder = 1; maxOrder < 5; ++maxOrder) {
            checkF0F1(new DerivativeStructure(3, maxOrder, 0, 1.0).multiply(5),  5.0, 5.0, 0.0, 0.0);
            checkF0F1(new DerivativeStructure(3, maxOrder, 1, 2.0).multiply(5), 10.0, 0.0, 5.0, 0.0);
            checkF0F1(new DerivativeStructure(3, maxOrder, 2, 3.0).multiply(5), 15.0, 0.0, 0.0, 5.0);
        }
    }

// org.apache.commons.math3.analysis.differentiation.DerivativeStructureTest::testMultiply
    public void testMultiply() {
        for (int maxOrder = 1; maxOrder < 5; ++maxOrder) {
            DerivativeStructure x = new DerivativeStructure(3, maxOrder, 0, 1.0);
            DerivativeStructure y = new DerivativeStructure(3, maxOrder, 1, 2.0);
            DerivativeStructure z = new DerivativeStructure(3, maxOrder, 2, 3.0);
            DerivativeStructure xyz = x.multiply(y.multiply(z));
            for (int i = 0; i <= maxOrder; ++i) {
                for (int j = 0; j <= maxOrder; ++j) {
                    for (int k = 0; k <= maxOrder; ++k) {
                        if (i + j + k <= maxOrder) {
                            Assert.assertEquals((i == 0 ? x.getValue() : (i == 1 ? 1.0 : 0.0)) *
                                                (j == 0 ? y.getValue() : (j == 1 ? 1.0 : 0.0)) *
                                                (k == 0 ? z.getValue() : (k == 1 ? 1.0 : 0.0)),
                                                xyz.getPartialDerivative(i, j, k),
                                                1.0e-15);
                        }
                    }
                }
            }
        }
    }

// org.apache.commons.math3.analysis.differentiation.DerivativeStructureTest::testNegate
    public void testNegate() {
        for (int maxOrder = 1; maxOrder < 5; ++maxOrder) {
            checkF0F1(new DerivativeStructure(3, maxOrder, 0, 1.0).negate(), -1.0, -1.0, 0.0, 0.0);
            checkF0F1(new DerivativeStructure(3, maxOrder, 1, 2.0).negate(), -2.0, 0.0, -1.0, 0.0);
            checkF0F1(new DerivativeStructure(3, maxOrder, 2, 3.0).negate(), -3.0, 0.0, 0.0, -1.0);
        }
    }

// org.apache.commons.math3.analysis.differentiation.DerivativeStructureTest::testReciprocal
    public void testReciprocal() {
        for (double x = 0.1; x < 1.2; x += 0.1) {
            DerivativeStructure r = new DerivativeStructure(1, 6, 0, x).reciprocal();
            Assert.assertEquals(1 / x, r.getValue(), 1.0e-15);
            for (int i = 1; i < r.getOrder(); ++i) {
                double expected = ArithmeticUtils.pow(-1, i) * ArithmeticUtils.factorial(i) /
                                  FastMath.pow(x, i + 1);
                Assert.assertEquals(expected, r.getPartialDerivative(i), 1.0e-15 * FastMath.abs(expected));
            }
        }
    }

// org.apache.commons.math3.analysis.differentiation.DerivativeStructureTest::testPow
    public void testPow() {
        for (int maxOrder = 1; maxOrder < 5; ++maxOrder) {
            for (int n = 0; n < 10; ++n) {

                DerivativeStructure x = new DerivativeStructure(3, maxOrder, 0, 1.0);
                DerivativeStructure y = new DerivativeStructure(3, maxOrder, 1, 2.0);
                DerivativeStructure z = new DerivativeStructure(3, maxOrder, 2, 3.0);
                List<DerivativeStructure> list = Arrays.asList(x, y, z,
                                                               x.add(y).add(z),
                                                               x.multiply(y).multiply(z));

                if (n == 0) {
                    for (DerivativeStructure ds : list) {
                        checkEquals(ds.getField().getOne(), ds.pow(n), 1.0e-15);
                    }
                } else if (n == 1) {
                    for (DerivativeStructure ds : list) {
                        checkEquals(ds, ds.pow(n), 1.0e-15);
                    }
                } else {
                    for (DerivativeStructure ds : list) {
                        DerivativeStructure p = ds.getField().getOne();
                        for (int i = 0; i < n; ++i) {
                            p = p.multiply(ds);
                        }
                        checkEquals(p, ds.pow(n), 1.0e-15);
                    }
                }
            }
        }
    }

// org.apache.commons.math3.analysis.differentiation.DerivativeStructureTest::testExpression
    public void testExpression() {
        double epsilon = 2.5e-13;
        for (double x = 0; x < 2; x += 0.2) {
            DerivativeStructure dsX = new DerivativeStructure(3, 5, 0, x);
            for (double y = 0; y < 2; y += 0.2) {
                DerivativeStructure dsY = new DerivativeStructure(3, 5, 1, y);
                for (double z = 0; z >- 2; z -= 0.2) {
                    DerivativeStructure dsZ = new DerivativeStructure(3, 5, 2, z);

                    
                    DerivativeStructure ds =
                            new DerivativeStructure(1, dsX,
                                                    5, dsX.multiply(dsY),
                                                    -2, dsZ,
                                                    1, new DerivativeStructure(8, dsZ.multiply(dsX),
                                                                               -1, dsY).pow(3));
                    DerivativeStructure dsOther =
                            new DerivativeStructure(1, dsX,
                                                    5, dsX.multiply(dsY),
                                                    -2, dsZ).add(new DerivativeStructure(8, dsZ.multiply(dsX),
                                                                                         -1, dsY).pow(3));
                    double f = x + 5 * x * y - 2 * z + FastMath.pow(8 * z * x - y, 3);
                    Assert.assertEquals(f, ds.getValue(),
                                        FastMath.abs(epsilon * f));
                    Assert.assertEquals(f, dsOther.getValue(),
                                        FastMath.abs(epsilon * f));

                    
                    double dfdx = 1 + 5 * y + 24 * z * FastMath.pow(8 * z * x - y, 2);
                    Assert.assertEquals(dfdx, ds.getPartialDerivative(1, 0, 0),
                                        FastMath.abs(epsilon * dfdx));
                    Assert.assertEquals(dfdx, dsOther.getPartialDerivative(1, 0, 0),
                                        FastMath.abs(epsilon * dfdx));

                    
                    double dfdxdy = 5 + 48 * z * (y - 8 * z * x);
                    Assert.assertEquals(dfdxdy, ds.getPartialDerivative(1, 1, 0),
                                        FastMath.abs(epsilon * dfdxdy));
                    Assert.assertEquals(dfdxdy, dsOther.getPartialDerivative(1, 1, 0),
                                        FastMath.abs(epsilon * dfdxdy));

                    
                    double dfdxdydz = 48 * (y - 16 * z * x);
                    Assert.assertEquals(dfdxdydz, ds.getPartialDerivative(1, 1, 1),
                                        FastMath.abs(epsilon * dfdxdydz));
                    Assert.assertEquals(dfdxdydz, dsOther.getPartialDerivative(1, 1, 1),
                                        FastMath.abs(epsilon * dfdxdydz));

                }
                
            }
        }
    }

// org.apache.commons.math3.analysis.differentiation.DerivativeStructureTest::testCompositionOneVariableX
    public void testCompositionOneVariableX() {
        double epsilon = 1.0e-13;
        for (int maxOrder = 0; maxOrder < 5; ++maxOrder) {
            for (double x = 0.1; x < 1.2; x += 0.1) {
                DerivativeStructure dsX = new DerivativeStructure(1, maxOrder, 0, x);
                for (double y = 0.1; y < 1.2; y += 0.1) {
                    DerivativeStructure dsY = new DerivativeStructure(1, maxOrder, y);
                    DerivativeStructure f = dsX.divide(dsY).sqrt();
                    double f0 = FastMath.sqrt(x / y);
                    Assert.assertEquals(f0, f.getValue(), FastMath.abs(epsilon * f0));
                    if (f.getOrder() > 0) {
                        double f1 = 1 / (2 * FastMath.sqrt(x * y));
                        Assert.assertEquals(f1, f.getPartialDerivative(1), FastMath.abs(epsilon * f1));
                        if (f.getOrder() > 1) {
                            double f2 = -f1 / (2 * x); 
                            Assert.assertEquals(f2, f.getPartialDerivative(2), FastMath.abs(epsilon * f2));
                            if (f.getOrder() > 2) {
                                double f3 = (f0 + x / (2 * y * f0)) / (4 * x * x * x); 
                                Assert.assertEquals(f3, f.getPartialDerivative(3), FastMath.abs(epsilon * f3));
                            }
                        }
                    }
                }
            }
        }        
    }

// org.apache.commons.math3.analysis.differentiation.DerivativeStructureTest::testTrigo
    public void testTrigo() {
        double epsilon = 2.0e-12;
        for (int maxOrder = 0; maxOrder < 5; ++maxOrder) {
            for (double x = 0.1; x < 1.2; x += 0.1) {
                DerivativeStructure dsX = new DerivativeStructure(3, maxOrder, 0, x);
                for (double y = 0.1; y < 1.2; y += 0.1) {
                    DerivativeStructure dsY = new DerivativeStructure(3, maxOrder, 1, y);
                    for (double z = 0.1; z < 1.2; z += 0.1) {
                        DerivativeStructure dsZ = new DerivativeStructure(3, maxOrder, 2, z);
                        DerivativeStructure f = dsX.divide(dsY.cos().add(dsZ.tan())).sin();
                        double a = FastMath.cos(y) + FastMath.tan(z);
                        double f0 = FastMath.sin(x / a);
                        Assert.assertEquals(f0, f.getValue(), FastMath.abs(epsilon * f0));
                        if (f.getOrder() > 0) {
                            double dfdx = FastMath.cos(x / a) / a;
                            Assert.assertEquals(dfdx, f.getPartialDerivative(1, 0, 0), FastMath.abs(epsilon * dfdx));
                            double dfdy =  x * FastMath.sin(y) * dfdx / a;
                            Assert.assertEquals(dfdy, f.getPartialDerivative(0, 1, 0), FastMath.abs(epsilon * dfdy));
                            double cz = FastMath.cos(z);
                            double cz2 = cz * cz;
                            double dfdz = -x * dfdx / (a * cz2);
                            Assert.assertEquals(dfdz, f.getPartialDerivative(0, 0, 1), FastMath.abs(epsilon * dfdz));
                            if (f.getOrder() > 1) {
                                double df2dx2 = -(f0 / (a * a));
                                Assert.assertEquals(df2dx2, f.getPartialDerivative(2, 0, 0), FastMath.abs(epsilon * df2dx2));
                                double df2dy2 = x * FastMath.cos(y) * dfdx / a -
                                                x * x * FastMath.sin(y) * FastMath.sin(y) * f0 / (a * a * a * a) +
                                                2 * FastMath.sin(y) * dfdy / a;
                                Assert.assertEquals(df2dy2, f.getPartialDerivative(0, 2, 0), FastMath.abs(epsilon * df2dy2));
                                double c4 = cz2 * cz2;
                                double df2dz2 = x * (2 * a * (1 - a * cz * FastMath.sin(z)) * dfdx - x * f0 / a ) / (a * a * a * c4);
                                Assert.assertEquals(df2dz2, f.getPartialDerivative(0, 0, 2), FastMath.abs(epsilon * df2dz2));
                                double df2dxdy = dfdy / x  - x * FastMath.sin(y) * f0 / (a * a * a);
                                Assert.assertEquals(df2dxdy, f.getPartialDerivative(1, 1, 0), FastMath.abs(epsilon * df2dxdy));
                            }
                        }
                    }
                }
            }        
        }
    }

// org.apache.commons.math3.analysis.differentiation.DerivativeStructureTest::testSqrtDefinition
    public void testSqrtDefinition() {
        double[] epsilon = new double[] { 5.0e-16, 5.0e-16, 2.0e-15, 5.0e-14, 2.0e-12 };
        for (int maxOrder = 0; maxOrder < 5; ++maxOrder) {
            for (double x = 0.1; x < 1.2; x += 0.001) {
                DerivativeStructure dsX = new DerivativeStructure(1, maxOrder, 0, x);
                DerivativeStructure sqrt1 = dsX.pow(0.5);
                DerivativeStructure sqrt2 = dsX.sqrt();
                DerivativeStructure zero = sqrt1.subtract(sqrt2);
                for (int n = 0; n <= maxOrder; ++n) {
                    Assert.assertEquals(0, zero.getPartialDerivative(n), epsilon[n]);
                }
            }
        }
    }

// org.apache.commons.math3.analysis.differentiation.DerivativeStructureTest::testRootNSingularity
    public void testRootNSingularity() {
        for (int n = 2; n < 10; ++n) {
            for (int maxOrder = 0; maxOrder < 12; ++maxOrder) {
                DerivativeStructure dsZero = new DerivativeStructure(1, maxOrder, 0, 0.0);
                DerivativeStructure rootN  = dsZero.rootN(n);
                Assert.assertEquals(0.0, rootN.getValue(), 1.0e-20);
                if (maxOrder > 0) {
                    Assert.assertTrue(Double.isInfinite(rootN.getPartialDerivative(1)));
                    Assert.assertTrue(rootN.getPartialDerivative(1) > 0);
                    for (int order = 2; order <= maxOrder; ++order) {
                        
                        
                        
                        
                        
                        
                        
                        
                        
                        
                        
                        
                        
                        
                        
                        
                        
                        
                        
                        Assert.assertTrue(Double.isNaN(rootN.getPartialDerivative(order)));
                    }
                }

                
                
                
                double[] gDerivatives = new double[ 1 + maxOrder];
                gDerivatives[0] = 0.0;
                for (int k = 1; k <= maxOrder; ++k) {
                    gDerivatives[k] = FastMath.pow(-1.0, k + 1);
                }
                DerivativeStructure correctRoot = new DerivativeStructure(1, maxOrder, gDerivatives).rootN(n);
                Assert.assertEquals(0.0, correctRoot.getValue(), 1.0e-20);
                if (maxOrder > 0) {
                    Assert.assertTrue(Double.isInfinite(correctRoot.getPartialDerivative(1)));
                    Assert.assertTrue(correctRoot.getPartialDerivative(1) > 0);
                    for (int order = 2; order <= maxOrder; ++order) {
                        Assert.assertTrue(Double.isInfinite(correctRoot.getPartialDerivative(order)));
                        if ((order % 2) == 0) {
                            Assert.assertTrue(correctRoot.getPartialDerivative(order) < 0);
                        } else {
                            Assert.assertTrue(correctRoot.getPartialDerivative(order) > 0);
                        }
                    }
                }

            }

        }

    }

// org.apache.commons.math3.analysis.differentiation.DerivativeStructureTest::testSqrtPow2
    public void testSqrtPow2() {
        double[] epsilon = new double[] { 1.0e-16, 3.0e-16, 2.0e-15, 6.0e-14, 6.0e-12 };
        for (int maxOrder = 0; maxOrder < 5; ++maxOrder) {
            for (double x = 0.1; x < 1.2; x += 0.001) {
                DerivativeStructure dsX = new DerivativeStructure(1, maxOrder, 0, x);
                DerivativeStructure rebuiltX = dsX.multiply(dsX).sqrt();
                DerivativeStructure zero = rebuiltX.subtract(dsX);
                for (int n = 0; n <= maxOrder; ++n) {
                    Assert.assertEquals(0.0, zero.getPartialDerivative(n), epsilon[n]);
                }
            }
        }
    }

// org.apache.commons.math3.analysis.differentiation.DerivativeStructureTest::testCbrtDefinition
    public void testCbrtDefinition() {
        double[] epsilon = new double[] { 4.0e-16, 9.0e-16, 6.0e-15, 2.0e-13, 4.0e-12 };
        for (int maxOrder = 0; maxOrder < 5; ++maxOrder) {
            for (double x = 0.1; x < 1.2; x += 0.001) {
                DerivativeStructure dsX = new DerivativeStructure(1, maxOrder, 0, x);
                DerivativeStructure cbrt1 = dsX.pow(1.0 / 3.0);
                DerivativeStructure cbrt2 = dsX.cbrt();
                DerivativeStructure zero = cbrt1.subtract(cbrt2);
                for (int n = 0; n <= maxOrder; ++n) {
                    Assert.assertEquals(0, zero.getPartialDerivative(n), epsilon[n]);
                }
            }
        }
    }

// org.apache.commons.math3.analysis.differentiation.DerivativeStructureTest::testCbrtPow3
    public void testCbrtPow3() {
        double[] epsilon = new double[] { 1.0e-16, 5.0e-16, 8.0e-15, 3.0e-13, 4.0e-11 };
        for (int maxOrder = 0; maxOrder < 5; ++maxOrder) {
            for (double x = 0.1; x < 1.2; x += 0.001) {
                DerivativeStructure dsX = new DerivativeStructure(1, maxOrder, 0, x);
                DerivativeStructure rebuiltX = dsX.multiply(dsX.multiply(dsX)).cbrt();
                DerivativeStructure zero = rebuiltX.subtract(dsX);
                for (int n = 0; n <= maxOrder; ++n) {
                    Assert.assertEquals(0.0, zero.getPartialDerivative(n), epsilon[n]);
                }
            }
        }
    }

// org.apache.commons.math3.analysis.differentiation.DerivativeStructureTest::testPowReciprocalPow
    public void testPowReciprocalPow() {
        double[] epsilon = new double[] { 2.0e-15, 2.0e-14, 3.0e-13, 8.0e-12, 3.0e-10 };
        for (int maxOrder = 0; maxOrder < 5; ++maxOrder) {
            for (double x = 0.1; x < 1.2; x += 0.01) {
                DerivativeStructure dsX = new DerivativeStructure(2, maxOrder, 0, x);
                for (double y = 0.1; y < 1.2; y += 0.01) {
                    DerivativeStructure dsY = new DerivativeStructure(2, maxOrder, 1, y);
                    DerivativeStructure rebuiltX = dsX.pow(dsY).pow(dsY.reciprocal());
                    DerivativeStructure zero = rebuiltX.subtract(dsX);
                    for (int n = 0; n <= maxOrder; ++n) {
                        for (int m = 0; m <= maxOrder; ++m) {
                            if (n + m <= maxOrder) {
                                Assert.assertEquals(0.0, zero.getPartialDerivative(n, m), epsilon[n + m]);
                            }
                        }
                    }
                }
            }
        }
    }

// org.apache.commons.math3.analysis.differentiation.DerivativeStructureTest::testHypotDefinition
    public void testHypotDefinition() {
        double epsilon = 1.0e-20;
        for (int maxOrder = 0; maxOrder < 5; ++maxOrder) {
            for (double x = -1.7; x < 2; x += 0.2) {
                DerivativeStructure dsX = new DerivativeStructure(2, maxOrder, 0, x);
                for (double y = -1.7; y < 2; y += 0.2) {
                    DerivativeStructure dsY = new DerivativeStructure(2, maxOrder, 1, y);
                    DerivativeStructure hypot = DerivativeStructure.hypot(dsY, dsX);
                    DerivativeStructure ref = dsX.multiply(dsX).add(dsY.multiply(dsY)).sqrt();
                    DerivativeStructure zero = hypot.subtract(ref);
                    for (int n = 0; n <= maxOrder; ++n) {
                        for (int m = 0; m <= maxOrder; ++m) {
                            if (n + m <= maxOrder) {
                                Assert.assertEquals(0, zero.getPartialDerivative(n, m), epsilon);
                            }
                        }
                    }
                }
            }
        }
    }

// org.apache.commons.math3.analysis.differentiation.DerivativeStructureTest::testHypotNoOverflow
    public void testHypotNoOverflow() {

        DerivativeStructure dsX = new DerivativeStructure(2, 5, 0, +3.0e250);
        DerivativeStructure dsY = new DerivativeStructure(2, 5, 1, -4.0e250);
        DerivativeStructure hypot = DerivativeStructure.hypot(dsX, dsY);
        Assert.assertEquals(5.0e250, hypot.getValue(), 1.0e235);
        Assert.assertEquals(dsX.getValue() / hypot.getValue(), hypot.getPartialDerivative(1, 0), 1.0e-10);
        Assert.assertEquals(dsY.getValue() / hypot.getValue(), hypot.getPartialDerivative(0, 1), 1.0e-10);

        DerivativeStructure sqrt  = dsX.multiply(dsX).add(dsY.multiply(dsY)).sqrt();
        Assert.assertTrue(Double.isInfinite(sqrt.getValue()));

    }

// org.apache.commons.math3.analysis.differentiation.DerivativeStructureTest::testHypotNeglectible
    public void testHypotNeglectible() {

        DerivativeStructure dsSmall = new DerivativeStructure(2, 5, 0, +3.0e-10);
        DerivativeStructure dsLarge = new DerivativeStructure(2, 5, 1, -4.0e25);

        Assert.assertEquals(dsLarge.abs().getValue(),
                            DerivativeStructure.hypot(dsSmall, dsLarge).getValue(),
                            1.0e-10);
        Assert.assertEquals(0,
                            DerivativeStructure.hypot(dsSmall, dsLarge).getPartialDerivative(1, 0),
                            1.0e-10);
        Assert.assertEquals(-1,
                            DerivativeStructure.hypot(dsSmall, dsLarge).getPartialDerivative(0, 1),
                            1.0e-10);

        Assert.assertEquals(dsLarge.abs().getValue(),
                            DerivativeStructure.hypot(dsLarge, dsSmall).getValue(),
                            1.0e-10);
        Assert.assertEquals(0,
                            DerivativeStructure.hypot(dsLarge, dsSmall).getPartialDerivative(1, 0),
                            1.0e-10);
        Assert.assertEquals(-1,
                            DerivativeStructure.hypot(dsLarge, dsSmall).getPartialDerivative(0, 1),
                            1.0e-10);

    }

// org.apache.commons.math3.analysis.differentiation.DerivativeStructureTest::testHypotSpecial
    public void testHypotSpecial() {
        Assert.assertTrue(Double.isNaN(DerivativeStructure.hypot(new DerivativeStructure(2, 5, 0, Double.NaN),
                                                                 new DerivativeStructure(2, 5, 0, +3.0e250)).getValue()));
        Assert.assertTrue(Double.isNaN(DerivativeStructure.hypot(new DerivativeStructure(2, 5, 0, +3.0e250),
                                                                 new DerivativeStructure(2, 5, 0, Double.NaN)).getValue()));
        Assert.assertTrue(Double.isInfinite(DerivativeStructure.hypot(new DerivativeStructure(2, 5, 0, Double.POSITIVE_INFINITY),
                                                                      new DerivativeStructure(2, 5, 0, +3.0e250)).getValue()));
        Assert.assertTrue(Double.isInfinite(DerivativeStructure.hypot(new DerivativeStructure(2, 5, 0, +3.0e250),
                                                                      new DerivativeStructure(2, 5, 0, Double.POSITIVE_INFINITY)).getValue()));
    }

// org.apache.commons.math3.analysis.differentiation.DerivativeStructureTest::testPrimitiveRemainder
    public void testPrimitiveRemainder() {
        double epsilon = 1.0e-15;
        for (int maxOrder = 0; maxOrder < 5; ++maxOrder) {
            for (double x = -1.7; x < 2; x += 0.2) {
                DerivativeStructure dsX = new DerivativeStructure(2, maxOrder, 0, x);
                for (double y = -1.7; y < 2; y += 0.2) {
                    DerivativeStructure remainder = dsX.remainder(y);
                    DerivativeStructure ref = dsX.subtract(x - FastMath.IEEEremainder(x, y));
                    DerivativeStructure zero = remainder.subtract(ref);
                    for (int n = 0; n <= maxOrder; ++n) {
                        for (int m = 0; m <= maxOrder; ++m) {
                            if (n + m <= maxOrder) {
                                Assert.assertEquals(0, zero.getPartialDerivative(n, m), epsilon);
                            }
                        }
                    }
                }
            }
        }
    }

// org.apache.commons.math3.analysis.differentiation.DerivativeStructureTest::testRemainder
    public void testRemainder() {
        double epsilon = 2.0e-15;
        for (int maxOrder = 0; maxOrder < 5; ++maxOrder) {
            for (double x = -1.7; x < 2; x += 0.2) {
                DerivativeStructure dsX = new DerivativeStructure(2, maxOrder, 0, x);
                for (double y = -1.7; y < 2; y += 0.2) {
                    DerivativeStructure dsY = new DerivativeStructure(2, maxOrder, 1, y);
                    DerivativeStructure remainder = dsX.remainder(dsY);
                    DerivativeStructure ref = dsX.subtract(dsY.multiply((x - FastMath.IEEEremainder(x, y)) / y));
                    DerivativeStructure zero = remainder.subtract(ref);
                    for (int n = 0; n <= maxOrder; ++n) {
                        for (int m = 0; m <= maxOrder; ++m) {
                            if (n + m <= maxOrder) {
                                Assert.assertEquals(0, zero.getPartialDerivative(n, m), epsilon);
                            }
                        }
                    }
                }
            }
        }
    }

// org.apache.commons.math3.analysis.differentiation.DerivativeStructureTest::testExp
    public void testExp() {
        double[] epsilon = new double[] { 1.0e-16, 1.0e-16, 1.0e-16, 1.0e-16, 1.0e-16 };
        for (int maxOrder = 0; maxOrder < 5; ++maxOrder) {
            for (double x = 0.1; x < 1.2; x += 0.001) {
                double refExp = FastMath.exp(x);
                DerivativeStructure exp = new DerivativeStructure(1, maxOrder, 0, x).exp();
                for (int n = 0; n <= maxOrder; ++n) {
                    Assert.assertEquals(refExp, exp.getPartialDerivative(n), epsilon[n]);
                }
            }
        }
    }

// org.apache.commons.math3.analysis.differentiation.DerivativeStructureTest::testExpm1Definition
    public void testExpm1Definition() {
        double epsilon = 3.0e-16;
        for (int maxOrder = 0; maxOrder < 5; ++maxOrder) {
            for (double x = 0.1; x < 1.2; x += 0.001) {
                DerivativeStructure dsX = new DerivativeStructure(1, maxOrder, 0, x);
                DerivativeStructure expm11 = dsX.expm1();
                DerivativeStructure expm12 = dsX.exp().subtract(dsX.getField().getOne());
                DerivativeStructure zero = expm11.subtract(expm12);
                for (int n = 0; n <= maxOrder; ++n) {
                    Assert.assertEquals(0, zero.getPartialDerivative(n), epsilon);
                }
            }
        }
    }

// org.apache.commons.math3.analysis.differentiation.DerivativeStructureTest::testLog
    public void testLog() {
        double[] epsilon = new double[] { 1.0e-16, 1.0e-16, 3.0e-14, 7.0e-13, 3.0e-11 };
        for (int maxOrder = 0; maxOrder < 5; ++maxOrder) {
            for (double x = 0.1; x < 1.2; x += 0.001) {
                DerivativeStructure log = new DerivativeStructure(1, maxOrder, 0, x).log();
                Assert.assertEquals(FastMath.log(x), log.getValue(), epsilon[0]);
                for (int n = 1; n <= maxOrder; ++n) {
                    double refDer = -ArithmeticUtils.factorial(n - 1) / FastMath.pow(-x, n);
                    Assert.assertEquals(refDer, log.getPartialDerivative(n), epsilon[n]);
                }
            }
        }
    }

// org.apache.commons.math3.analysis.differentiation.DerivativeStructureTest::testLog1pDefinition
    public void testLog1pDefinition() {
        double epsilon = 3.0e-16;
        for (int maxOrder = 0; maxOrder < 5; ++maxOrder) {
            for (double x = 0.1; x < 1.2; x += 0.001) {
                DerivativeStructure dsX = new DerivativeStructure(1, maxOrder, 0, x);
                DerivativeStructure log1p1 = dsX.log1p();
                DerivativeStructure log1p2 = dsX.add(dsX.getField().getOne()).log();
                DerivativeStructure zero = log1p1.subtract(log1p2);
                for (int n = 0; n <= maxOrder; ++n) {
                    Assert.assertEquals(0, zero.getPartialDerivative(n), epsilon);
                }
            }
        }
    }

// org.apache.commons.math3.analysis.differentiation.DerivativeStructureTest::testLog10Definition
    public void testLog10Definition() {
        double[] epsilon = new double[] { 3.0e-16, 3.0e-16, 8.0e-15, 3.0e-13, 8.0e-12 };
        for (int maxOrder = 0; maxOrder < 5; ++maxOrder) {
            for (double x = 0.1; x < 1.2; x += 0.001) {
                DerivativeStructure dsX = new DerivativeStructure(1, maxOrder, 0, x);
                DerivativeStructure log101 = dsX.log10();
                DerivativeStructure log102 = dsX.log().divide(FastMath.log(10.0));
                DerivativeStructure zero = log101.subtract(log102);
                for (int n = 0; n <= maxOrder; ++n) {
                    Assert.assertEquals(0, zero.getPartialDerivative(n), epsilon[n]);
                }
            }
        }
    }

// org.apache.commons.math3.analysis.differentiation.DerivativeStructureTest::testLogExp
    public void testLogExp() {
        double[] epsilon = new double[] { 2.0e-16, 2.0e-16, 3.0e-16, 2.0e-15, 6.0e-15 };
        for (int maxOrder = 0; maxOrder < 5; ++maxOrder) {
            for (double x = 0.1; x < 1.2; x += 0.001) {
                DerivativeStructure dsX = new DerivativeStructure(1, maxOrder, 0, x);
                DerivativeStructure rebuiltX = dsX.exp().log();
                DerivativeStructure zero = rebuiltX.subtract(dsX);
                for (int n = 0; n <= maxOrder; ++n) {
                    Assert.assertEquals(0.0, zero.getPartialDerivative(n), epsilon[n]);
                }
            }
        }
    }

// org.apache.commons.math3.analysis.differentiation.DerivativeStructureTest::testLog1pExpm1
    public void testLog1pExpm1() {
        double[] epsilon = new double[] { 6.0e-17, 3.0e-16, 5.0e-16, 9.0e-16, 6.0e-15 };
        for (int maxOrder = 0; maxOrder < 5; ++maxOrder) {
            for (double x = 0.1; x < 1.2; x += 0.001) {
                DerivativeStructure dsX = new DerivativeStructure(1, maxOrder, 0, x);
                DerivativeStructure rebuiltX = dsX.expm1().log1p();
                DerivativeStructure zero = rebuiltX.subtract(dsX);
                for (int n = 0; n <= maxOrder; ++n) {
                    Assert.assertEquals(0.0, zero.getPartialDerivative(n), epsilon[n]);
                }
            }
        }
    }

// org.apache.commons.math3.analysis.differentiation.DerivativeStructureTest::testLog10Power
    public void testLog10Power() {
        double[] epsilon = new double[] { 3.0e-16, 3.0e-16, 9.0e-16, 6.0e-15, 6.0e-14 };
        for (int maxOrder = 0; maxOrder < 5; ++maxOrder) {
            for (double x = 0.1; x < 1.2; x += 0.001) {
                DerivativeStructure dsX = new DerivativeStructure(1, maxOrder, 0, x);
                DerivativeStructure rebuiltX = new DerivativeStructure(1, maxOrder, 10.0).pow(dsX).log10();
                DerivativeStructure zero = rebuiltX.subtract(dsX);
                for (int n = 0; n <= maxOrder; ++n) {
                    Assert.assertEquals(0, zero.getPartialDerivative(n), epsilon[n]);
                }
            }
        }
    }

// org.apache.commons.math3.analysis.differentiation.DerivativeStructureTest::testSinCos
    public void testSinCos() {
        double epsilon = 5.0e-16;
        for (int maxOrder = 0; maxOrder < 6; ++maxOrder) {
            for (double x = 0.1; x < 1.2; x += 0.001) {
                DerivativeStructure dsX = new DerivativeStructure(1, maxOrder, 0, x);
                DerivativeStructure sin = dsX.sin();
                DerivativeStructure cos = dsX.cos();
                double s = FastMath.sin(x);
                double c = FastMath.cos(x);
                for (int n = 0; n <= maxOrder; ++n) {
                    switch (n % 4) {
                    case 0 :
                        Assert.assertEquals( s, sin.getPartialDerivative(n), epsilon);
                        Assert.assertEquals( c, cos.getPartialDerivative(n), epsilon);
                        break;
                    case 1 :
                        Assert.assertEquals( c, sin.getPartialDerivative(n), epsilon);
                        Assert.assertEquals(-s, cos.getPartialDerivative(n), epsilon);
                        break;
                    case 2 :
                        Assert.assertEquals(-s, sin.getPartialDerivative(n), epsilon);
                        Assert.assertEquals(-c, cos.getPartialDerivative(n), epsilon);
                        break;
                    default :
                        Assert.assertEquals(-c, sin.getPartialDerivative(n), epsilon);
                        Assert.assertEquals( s, cos.getPartialDerivative(n), epsilon);
                        break;
                    }
                }
            }
        }
    }

// org.apache.commons.math3.analysis.differentiation.DerivativeStructureTest::testSinAsin
    public void testSinAsin() {
        double[] epsilon = new double[] { 3.0e-16, 5.0e-16, 3.0e-15, 2.0e-14, 4.0e-13 };
        for (int maxOrder = 0; maxOrder < 5; ++maxOrder) {
            for (double x = 0.1; x < 1.2; x += 0.001) {
                DerivativeStructure dsX = new DerivativeStructure(1, maxOrder, 0, x);
                DerivativeStructure rebuiltX = dsX.sin().asin();
                DerivativeStructure zero = rebuiltX.subtract(dsX);
                for (int n = 0; n <= maxOrder; ++n) {
                    Assert.assertEquals(0.0, zero.getPartialDerivative(n), epsilon[n]);
                }
            }
        }
    }

// org.apache.commons.math3.analysis.differentiation.DerivativeStructureTest::testCosAcos
    public void testCosAcos() {
        double[] epsilon = new double[] { 6.0e-16, 6.0e-15, 2.0e-13, 4.0e-12, 2.0e-10 };
        for (int maxOrder = 0; maxOrder < 5; ++maxOrder) {
            for (double x = 0.1; x < 1.2; x += 0.001) {
                DerivativeStructure dsX = new DerivativeStructure(1, maxOrder, 0, x);
                DerivativeStructure rebuiltX = dsX.cos().acos();
                DerivativeStructure zero = rebuiltX.subtract(dsX);
                for (int n = 0; n <= maxOrder; ++n) {
                    Assert.assertEquals(0.0, zero.getPartialDerivative(n), epsilon[n]);
                }
            }
        }
    }

// org.apache.commons.math3.analysis.differentiation.DerivativeStructureTest::testTanAtan
    public void testTanAtan() {
        double[] epsilon = new double[] { 6.0e-17, 2.0e-16, 2.0e-15, 4.0e-14, 2.0e-12 };
        for (int maxOrder = 0; maxOrder < 5; ++maxOrder) {
            for (double x = 0.1; x < 1.2; x += 0.001) {
                DerivativeStructure dsX = new DerivativeStructure(1, maxOrder, 0, x);
                DerivativeStructure rebuiltX = dsX.tan().atan();
                DerivativeStructure zero = rebuiltX.subtract(dsX);
                for (int n = 0; n <= maxOrder; ++n) {
                    Assert.assertEquals(0.0, zero.getPartialDerivative(n), epsilon[n]);
                }
            }
        }
    }

// org.apache.commons.math3.analysis.differentiation.DerivativeStructureTest::testTangentDefinition
    public void testTangentDefinition() {
        double[] epsilon = new double[] { 5.0e-16, 2.0e-15, 3.0e-14, 5.0e-13, 2.0e-11 };
        for (int maxOrder = 0; maxOrder < 5; ++maxOrder) {
            for (double x = 0.1; x < 1.2; x += 0.001) {
                DerivativeStructure dsX = new DerivativeStructure(1, maxOrder, 0, x);
                DerivativeStructure tan1 = dsX.sin().divide(dsX.cos());
                DerivativeStructure tan2 = dsX.tan();
                DerivativeStructure zero = tan1.subtract(tan2);
                for (int n = 0; n <= maxOrder; ++n) {
                    Assert.assertEquals(0, zero.getPartialDerivative(n), epsilon[n]);
                }
            }
        }
    }

// org.apache.commons.math3.analysis.differentiation.DerivativeStructureTest::testAtan2
    public void testAtan2() {
        double[] epsilon = new double[] { 5.0e-16, 3.0e-15, 2.2e-14, 1.0e-12, 8.0e-11 };
        for (int maxOrder = 0; maxOrder < 5; ++maxOrder) {
            for (double x = -1.7; x < 2; x += 0.2) {
                DerivativeStructure dsX = new DerivativeStructure(2, maxOrder, 0, x);
                for (double y = -1.7; y < 2; y += 0.2) {
                    DerivativeStructure dsY = new DerivativeStructure(2, maxOrder, 1, y);
                    DerivativeStructure atan2 = DerivativeStructure.atan2(dsY, dsX);
                    DerivativeStructure ref = dsY.divide(dsX).atan();
                    if (x < 0) {
                        ref = (y < 0) ? ref.subtract(FastMath.PI) : ref.add(FastMath.PI);
                    }
                    DerivativeStructure zero = atan2.subtract(ref);
                    for (int n = 0; n <= maxOrder; ++n) {
                        for (int m = 0; m <= maxOrder; ++m) {
                            if (n + m <= maxOrder) {
                                Assert.assertEquals(0, zero.getPartialDerivative(n, m), epsilon[n + m]);
                            }
                        }
                    }
                }
            }
        }
    }

// org.apache.commons.math3.analysis.differentiation.DerivativeStructureTest::testAtan2SpecialCases
    public void testAtan2SpecialCases() {

        DerivativeStructure pp =
                DerivativeStructure.atan2(new DerivativeStructure(2, 2, 1, +0.0),
                                          new DerivativeStructure(2, 2, 1, +0.0));
        Assert.assertEquals(0, pp.getValue(), 1.0e-15);
        Assert.assertEquals(+1, FastMath.copySign(1, pp.getValue()), 1.0e-15);

        DerivativeStructure pn =
                DerivativeStructure.atan2(new DerivativeStructure(2, 2, 1, +0.0),
                                          new DerivativeStructure(2, 2, 1, -0.0));
        Assert.assertEquals(FastMath.PI, pn.getValue(), 1.0e-15);

        DerivativeStructure np =
                DerivativeStructure.atan2(new DerivativeStructure(2, 2, 1, -0.0),
                                          new DerivativeStructure(2, 2, 1, +0.0));
        Assert.assertEquals(0, np.getValue(), 1.0e-15);
        Assert.assertEquals(-1, FastMath.copySign(1, np.getValue()), 1.0e-15);

        DerivativeStructure nn =
                DerivativeStructure.atan2(new DerivativeStructure(2, 2, 1, -0.0),
                                          new DerivativeStructure(2, 2, 1, -0.0));
        Assert.assertEquals(-FastMath.PI, nn.getValue(), 1.0e-15);

    }

// org.apache.commons.math3.analysis.differentiation.DerivativeStructureTest::testSinhDefinition
    public void testSinhDefinition() {
        double[] epsilon = new double[] { 3.0e-16, 3.0e-16, 5.0e-16, 2.0e-15, 6.0e-15 };
        for (int maxOrder = 0; maxOrder < 5; ++maxOrder) {
            for (double x = 0.1; x < 1.2; x += 0.001) {
                DerivativeStructure dsX = new DerivativeStructure(1, maxOrder, 0, x);
                DerivativeStructure sinh1 = dsX.exp().subtract(dsX.exp().reciprocal()).multiply(0.5);
                DerivativeStructure sinh2 = dsX.sinh();
                DerivativeStructure zero = sinh1.subtract(sinh2);
                for (int n = 0; n <= maxOrder; ++n) {
                    Assert.assertEquals(0, zero.getPartialDerivative(n), epsilon[n]);
                }
            }
        }
    }

// org.apache.commons.math3.analysis.differentiation.DerivativeStructureTest::testCoshDefinition
    public void testCoshDefinition() {
        double[] epsilon = new double[] { 3.0e-16, 3.0e-16, 5.0e-16, 2.0e-15, 6.0e-15 };
        for (int maxOrder = 0; maxOrder < 5; ++maxOrder) {
            for (double x = 0.1; x < 1.2; x += 0.001) {
                DerivativeStructure dsX = new DerivativeStructure(1, maxOrder, 0, x);
                DerivativeStructure cosh1 = dsX.exp().add(dsX.exp().reciprocal()).multiply(0.5);
                DerivativeStructure cosh2 = dsX.cosh();
                DerivativeStructure zero = cosh1.subtract(cosh2);
                for (int n = 0; n <= maxOrder; ++n) {
                    Assert.assertEquals(0, zero.getPartialDerivative(n), epsilon[n]);
                }
            }
        }
    }

// org.apache.commons.math3.analysis.differentiation.DerivativeStructureTest::testTanhDefinition
    public void testTanhDefinition() {
        double[] epsilon = new double[] { 3.0e-16, 5.0e-16, 7.0e-16, 3.0e-15, 2.0e-14 };
        for (int maxOrder = 0; maxOrder < 5; ++maxOrder) {
            for (double x = 0.1; x < 1.2; x += 0.001) {
                DerivativeStructure dsX = new DerivativeStructure(1, maxOrder, 0, x);
                DerivativeStructure tanh1 = dsX.exp().subtract(dsX.exp().reciprocal()).divide(dsX.exp().add(dsX.exp().reciprocal()));
                DerivativeStructure tanh2 = dsX.tanh();
                DerivativeStructure zero = tanh1.subtract(tanh2);
                for (int n = 0; n <= maxOrder; ++n) {
                    Assert.assertEquals(0, zero.getPartialDerivative(n), epsilon[n]);
                }
            }
        }
    }

// org.apache.commons.math3.analysis.differentiation.DerivativeStructureTest::testSinhAsinh
    public void testSinhAsinh() {
        double[] epsilon = new double[] { 3.0e-16, 3.0e-16, 4.0e-16, 7.0e-16, 3.0e-15, 8.0e-15 };
        for (int maxOrder = 0; maxOrder < 6; ++maxOrder) {
            for (double x = 0.1; x < 1.2; x += 0.001) {
                DerivativeStructure dsX = new DerivativeStructure(1, maxOrder, 0, x);
                DerivativeStructure rebuiltX = dsX.sinh().asinh();
                DerivativeStructure zero = rebuiltX.subtract(dsX);
                for (int n = 0; n <= maxOrder; ++n) {
                    Assert.assertEquals(0.0, zero.getPartialDerivative(n), epsilon[n]);
                }
            }
        }
    }

// org.apache.commons.math3.analysis.differentiation.DerivativeStructureTest::testCoshAcosh
    public void testCoshAcosh() {
        double[] epsilon = new double[] { 2.0e-15, 1.0e-14, 2.0e-13, 6.0e-12, 3.0e-10, 2.0e-8 };
        for (int maxOrder = 0; maxOrder < 6; ++maxOrder) {
            for (double x = 0.1; x < 1.2; x += 0.001) {
                DerivativeStructure dsX = new DerivativeStructure(1, maxOrder, 0, x);
                DerivativeStructure rebuiltX = dsX.cosh().acosh();
                DerivativeStructure zero = rebuiltX.subtract(dsX);
                for (int n = 0; n <= maxOrder; ++n) {
                    Assert.assertEquals(0.0, zero.getPartialDerivative(n), epsilon[n]);
                }
            }
        }
    }

// org.apache.commons.math3.analysis.differentiation.DerivativeStructureTest::testTanhAtanh
    public void testTanhAtanh() {
        double[] epsilon = new double[] { 3.0e-16, 2.0e-16, 7.0e-16, 4.0e-15, 3.0e-14, 4.0e-13 };
        for (int maxOrder = 0; maxOrder < 6; ++maxOrder) {
            for (double x = 0.1; x < 1.2; x += 0.001) {
                DerivativeStructure dsX = new DerivativeStructure(1, maxOrder, 0, x);
                DerivativeStructure rebuiltX = dsX.tanh().atanh();
                DerivativeStructure zero = rebuiltX.subtract(dsX);
                for (int n = 0; n <= maxOrder; ++n) {
                    Assert.assertEquals(0.0, zero.getPartialDerivative(n), epsilon[n]);
                }
            }
        }
    }

// org.apache.commons.math3.analysis.differentiation.DerivativeStructureTest::testCompositionOneVariableY
    public void testCompositionOneVariableY() {
        double epsilon = 1.0e-13;
        for (int maxOrder = 0; maxOrder < 5; ++maxOrder) {
            for (double x = 0.1; x < 1.2; x += 0.1) {
                DerivativeStructure dsX = new DerivativeStructure(1, maxOrder, x);
                for (double y = 0.1; y < 1.2; y += 0.1) {
                    DerivativeStructure dsY = new DerivativeStructure(1, maxOrder, 0, y);
                    DerivativeStructure f = dsX.divide(dsY).sqrt();
                    double f0 = FastMath.sqrt(x / y);
                    Assert.assertEquals(f0, f.getValue(), FastMath.abs(epsilon * f0));
                    if (f.getOrder() > 0) {
                        double f1 = -x / (2 * y * y * f0);
                        Assert.assertEquals(f1, f.getPartialDerivative(1), FastMath.abs(epsilon * f1));
                        if (f.getOrder() > 1) {
                            double f2 = (f0 - x / (4 * y * f0)) / (y * y); 
                            Assert.assertEquals(f2, f.getPartialDerivative(2), FastMath.abs(epsilon * f2));
                            if (f.getOrder() > 2) {
                                double f3 = (x / (8 * y * f0) - 2 * f0) / (y * y * y); 
                                Assert.assertEquals(f3, f.getPartialDerivative(3), FastMath.abs(epsilon * f3));
                            }
                        }
                    }
                }
            }
        }        
    }

// org.apache.commons.math3.analysis.differentiation.DerivativeStructureTest::testTaylorPolynomial
    public void testTaylorPolynomial() {
        for (double x = 0; x < 1.2; x += 0.1) {
            DerivativeStructure dsX = new DerivativeStructure(3, 4, 0, x);
            for (double y = 0; y < 1.2; y += 0.2) {
                DerivativeStructure dsY = new DerivativeStructure(3, 4, 1, y);
                for (double z = 0; z < 1.2; z += 0.2) {
                    DerivativeStructure dsZ = new DerivativeStructure(3, 4, 2, z);
                    DerivativeStructure f = dsX.multiply(dsY).add(dsZ).multiply(dsX).multiply(dsY);
                    for (double dx = -0.2; dx < 0.2; dx += 0.2) {
                        for (double dy = -0.2; dy < 0.2; dy += 0.1) {
                            for (double dz = -0.2; dz < 0.2; dz += 0.1) {
                                double ref = (x + dx) * (y + dy) * ((x + dx) * (y + dy) + (z + dz));
                                Assert.assertEquals(ref, f.taylor(dx, dy, dz), 2.0e-15);
                            }
                        }
                    }
                }
            }
        }
    }

// org.apache.commons.math3.analysis.differentiation.DerivativeStructureTest::testTaylorAtan2
    public void testTaylorAtan2() {
        double[] expected = new double[] { 0.214, 0.0241, 0.00422, 6.48e-4, 8.04e-5 };
        double x0 =  0.1;
        double y0 = -0.3;
        for (int maxOrder = 0; maxOrder < 5; ++maxOrder) {
            DerivativeStructure dsX   = new DerivativeStructure(2, maxOrder, 0, x0);
            DerivativeStructure dsY   = new DerivativeStructure(2, maxOrder, 1, y0);
            DerivativeStructure atan2 = DerivativeStructure.atan2(dsY, dsX);
            double maxError = 0;
            for (double dx = -0.05; dx < 0.05; dx += 0.001) {
                for (double dy = -0.05; dy < 0.05; dy += 0.001) {
                    double ref = FastMath.atan2(y0 + dy, x0 + dx);
                    maxError = FastMath.max(maxError, FastMath.abs(ref - atan2.taylor(dx, dy)));
                }
            }
            Assert.assertEquals(0.0, expected[maxOrder] - maxError, 0.01 * expected[maxOrder]);
        }
    }

// org.apache.commons.math3.analysis.differentiation.DerivativeStructureTest::testAbs
    public void testAbs() {

        DerivativeStructure minusOne = new DerivativeStructure(1, 1, 0, -1.0);
        Assert.assertEquals(+1.0, minusOne.abs().getPartialDerivative(0), 1.0e-15);
        Assert.assertEquals(-1.0, minusOne.abs().getPartialDerivative(1), 1.0e-15);

        DerivativeStructure plusOne = new DerivativeStructure(1, 1, 0, +1.0);
        Assert.assertEquals(+1.0, plusOne.abs().getPartialDerivative(0), 1.0e-15);
        Assert.assertEquals(+1.0, plusOne.abs().getPartialDerivative(1), 1.0e-15);

        DerivativeStructure minusZero = new DerivativeStructure(1, 1, 0, -0.0);
        Assert.assertEquals(+0.0, minusZero.abs().getPartialDerivative(0), 1.0e-15);
        Assert.assertEquals(-1.0, minusZero.abs().getPartialDerivative(1), 1.0e-15);

        DerivativeStructure plusZero = new DerivativeStructure(1, 1, 0, +0.0);
        Assert.assertEquals(+0.0, plusZero.abs().getPartialDerivative(0), 1.0e-15);
        Assert.assertEquals(+1.0, plusZero.abs().getPartialDerivative(1), 1.0e-15);

    }

// org.apache.commons.math3.analysis.differentiation.DerivativeStructureTest::testSignum
    public void testSignum() {

        DerivativeStructure minusOne = new DerivativeStructure(1, 1, 0, -1.0);
        Assert.assertEquals(-1.0, minusOne.signum().getPartialDerivative(0), 1.0e-15);
        Assert.assertEquals( 0.0, minusOne.signum().getPartialDerivative(1), 1.0e-15);

        DerivativeStructure plusOne = new DerivativeStructure(1, 1, 0, +1.0);
        Assert.assertEquals(+1.0, plusOne.signum().getPartialDerivative(0), 1.0e-15);
        Assert.assertEquals( 0.0, plusOne.signum().getPartialDerivative(1), 1.0e-15);

        DerivativeStructure minusZero = new DerivativeStructure(1, 1, 0, -0.0);
        Assert.assertEquals(-0.0, minusZero.signum().getPartialDerivative(0), 1.0e-15);
        Assert.assertTrue(Double.doubleToLongBits(minusZero.signum().getValue()) < 0);
        Assert.assertEquals( 0.0, minusZero.signum().getPartialDerivative(1), 1.0e-15);

        DerivativeStructure plusZero = new DerivativeStructure(1, 1, 0, +0.0);
        Assert.assertEquals(+0.0, plusZero.signum().getPartialDerivative(0), 1.0e-15);
        Assert.assertTrue(Double.doubleToLongBits(plusZero.signum().getValue()) == 0);
        Assert.assertEquals( 0.0, plusZero.signum().getPartialDerivative(1), 1.0e-15);

    }

// org.apache.commons.math3.analysis.differentiation.DerivativeStructureTest::testCeilFloorRintLong
    public void testCeilFloorRintLong() {

        DerivativeStructure x = new DerivativeStructure(1, 1, 0, -1.5);
        Assert.assertEquals(-1.5, x.getPartialDerivative(0), 1.0e-15);
        Assert.assertEquals(+1.0, x.getPartialDerivative(1), 1.0e-15);
        Assert.assertEquals(-1.0, x.ceil().getPartialDerivative(0), 1.0e-15);
        Assert.assertEquals(+0.0, x.ceil().getPartialDerivative(1), 1.0e-15);
        Assert.assertEquals(-2.0, x.floor().getPartialDerivative(0), 1.0e-15);
        Assert.assertEquals(+0.0, x.floor().getPartialDerivative(1), 1.0e-15);
        Assert.assertEquals(-2.0, x.rint().getPartialDerivative(0), 1.0e-15);
        Assert.assertEquals(+0.0, x.rint().getPartialDerivative(1), 1.0e-15);
        Assert.assertEquals(-2.0, x.subtract(x.getField().getOne()).rint().getPartialDerivative(0), 1.0e-15);
        Assert.assertEquals(-1l, x.round());

    }

// org.apache.commons.math3.analysis.differentiation.DerivativeStructureTest::testCopySign
    public void testCopySign() {

        DerivativeStructure minusOne = new DerivativeStructure(1, 1, 0, -1.0);
        Assert.assertEquals(+1.0, minusOne.copySign(+1.0).getPartialDerivative(0), 1.0e-15);
        Assert.assertEquals(-1.0, minusOne.copySign(+1.0).getPartialDerivative(1), 1.0e-15);
        Assert.assertEquals(-1.0, minusOne.copySign(-1.0).getPartialDerivative(0), 1.0e-15);
        Assert.assertEquals(+1.0, minusOne.copySign(-1.0).getPartialDerivative(1), 1.0e-15);
        Assert.assertEquals(+1.0, minusOne.copySign(+0.0).getPartialDerivative(0), 1.0e-15);
        Assert.assertEquals(-1.0, minusOne.copySign(+0.0).getPartialDerivative(1), 1.0e-15);
        Assert.assertEquals(-1.0, minusOne.copySign(-0.0).getPartialDerivative(0), 1.0e-15);
        Assert.assertEquals(+1.0, minusOne.copySign(-0.0).getPartialDerivative(1), 1.0e-15);
        Assert.assertEquals(+1.0, minusOne.copySign(Double.NaN).getPartialDerivative(0), 1.0e-15);
        Assert.assertEquals(-1.0, minusOne.copySign(Double.NaN).getPartialDerivative(1), 1.0e-15);

        DerivativeStructure plusOne = new DerivativeStructure(1, 1, 0, +1.0);
        Assert.assertEquals(+1.0, plusOne.copySign(+1.0).getPartialDerivative(0), 1.0e-15);
        Assert.assertEquals(+1.0, plusOne.copySign(+1.0).getPartialDerivative(1), 1.0e-15);
        Assert.assertEquals(-1.0, plusOne.copySign(-1.0).getPartialDerivative(0), 1.0e-15);
        Assert.assertEquals(-1.0, plusOne.copySign(-1.0).getPartialDerivative(1), 1.0e-15);
        Assert.assertEquals(+1.0, plusOne.copySign(+0.0).getPartialDerivative(0), 1.0e-15);
        Assert.assertEquals(+1.0, plusOne.copySign(+0.0).getPartialDerivative(1), 1.0e-15);
        Assert.assertEquals(-1.0, plusOne.copySign(-0.0).getPartialDerivative(0), 1.0e-15);
        Assert.assertEquals(-1.0, plusOne.copySign(-0.0).getPartialDerivative(1), 1.0e-15);
        Assert.assertEquals(+1.0, plusOne.copySign(Double.NaN).getPartialDerivative(0), 1.0e-15);
        Assert.assertEquals(+1.0, plusOne.copySign(Double.NaN).getPartialDerivative(1), 1.0e-15);

    }

// org.apache.commons.math3.analysis.differentiation.DerivativeStructureTest::testToDegreesDefinition
    public void testToDegreesDefinition() {
        double epsilon = 3.0e-16;
        for (int maxOrder = 0; maxOrder < 6; ++maxOrder) {
            for (double x = 0.1; x < 1.2; x += 0.001) {
                DerivativeStructure dsX = new DerivativeStructure(1, maxOrder, 0, x);
                Assert.assertEquals(FastMath.toDegrees(x), dsX.toDegrees().getValue(), epsilon);
                for (int n = 1; n <= maxOrder; ++n) {
                    if (n == 1) {
                        Assert.assertEquals(180 / FastMath.PI, dsX.toDegrees().getPartialDerivative(1), epsilon);
                    } else {
                        Assert.assertEquals(0.0, dsX.toDegrees().getPartialDerivative(n), epsilon);
                    }
                }
            }
        }
    }

// org.apache.commons.math3.analysis.differentiation.DerivativeStructureTest::testToRadiansDefinition
    public void testToRadiansDefinition() {
        double epsilon = 3.0e-16;
        for (int maxOrder = 0; maxOrder < 6; ++maxOrder) {
            for (double x = 0.1; x < 1.2; x += 0.001) {
                DerivativeStructure dsX = new DerivativeStructure(1, maxOrder, 0, x);
                Assert.assertEquals(FastMath.toRadians(x), dsX.toRadians().getValue(), epsilon);
                for (int n = 1; n <= maxOrder; ++n) {
                    if (n == 1) {
                        Assert.assertEquals(FastMath.PI / 180, dsX.toRadians().getPartialDerivative(1), epsilon);
                    } else {
                        Assert.assertEquals(0.0, dsX.toRadians().getPartialDerivative(n), epsilon);
                    }
                }
            }
        }
    }

// org.apache.commons.math3.analysis.differentiation.DerivativeStructureTest::testDegRad
    public void testDegRad() {
        double epsilon = 3.0e-16;
        for (int maxOrder = 0; maxOrder < 6; ++maxOrder) {
            for (double x = 0.1; x < 1.2; x += 0.001) {
                DerivativeStructure dsX = new DerivativeStructure(1, maxOrder, 0, x);
                DerivativeStructure rebuiltX = dsX.toDegrees().toRadians();
                DerivativeStructure zero = rebuiltX.subtract(dsX);
                for (int n = 0; n <= maxOrder; ++n) {
                    Assert.assertEquals(0.0, zero.getPartialDerivative(n), epsilon);
                }
            }
        }
    }

// org.apache.commons.math3.analysis.differentiation.DerivativeStructureTest::testComposeMismatchedDimensions
    public void testComposeMismatchedDimensions() {
        new DerivativeStructure(1, 3, 0, 1.2).compose(new double[3]);
    }

// org.apache.commons.math3.analysis.differentiation.DerivativeStructureTest::testCompose
    public void testCompose() {
        double[] epsilon = new double[] { 1.0e-20, 5.0e-14, 2.0e-13, 3.0e-13, 2.0e-13, 1.0e-20 };
        PolynomialFunction poly =
                new PolynomialFunction(new double[] { 1.0, 2.0, 3.0, 4.0, 5.0, 6.0 });
        for (int maxOrder = 0; maxOrder < 6; ++maxOrder) {
            PolynomialFunction[] p = new PolynomialFunction[maxOrder + 1];
            p[0] = poly;
            for (int i = 1; i <= maxOrder; ++i) {
                p[i] = p[i - 1].polynomialDerivative();
            }
            for (double x = 0.1; x < 1.2; x += 0.001) {
                DerivativeStructure dsX = new DerivativeStructure(1, maxOrder, 0, x);
                DerivativeStructure dsY1 = dsX.getField().getZero();
                for (int i = poly.degree(); i >= 0; --i) {
                    dsY1 = dsY1.multiply(dsX).add(poly.getCoefficients()[i]);
                }
                double[] f = new double[maxOrder + 1];
                for (int i = 0; i < f.length; ++i) {
                    f[i] = p[i].value(x);
                }
                DerivativeStructure dsY2 = dsX.compose(f);
                DerivativeStructure zero = dsY1.subtract(dsY2);
                for (int n = 0; n <= maxOrder; ++n) {
                    Assert.assertEquals(0.0, zero.getPartialDerivative(n), epsilon[n]);
                }
            }
        }
    }

// org.apache.commons.math3.analysis.differentiation.DerivativeStructureTest::testField
    public void testField() {
        for (int maxOrder = 1; maxOrder < 5; ++maxOrder) {
            DerivativeStructure x = new DerivativeStructure(3, maxOrder, 0, 1.0);
            checkF0F1(x.getField().getZero(), 0.0, 0.0, 0.0, 0.0);
            checkF0F1(x.getField().getOne(), 1.0, 0.0, 0.0, 0.0);
            Assert.assertEquals(maxOrder, x.getField().getZero().getOrder());
            Assert.assertEquals(3, x.getField().getZero().getFreeParameters());
            Assert.assertEquals(DerivativeStructure.class, x.getField().getRuntimeClass());
        }
    }

// org.apache.commons.math3.analysis.differentiation.DerivativeStructureTest::testOneParameterConstructor
    public void testOneParameterConstructor() {
        double x = 1.2;
        double cos = FastMath.cos(x);
        double sin = FastMath.sin(x);
        DerivativeStructure yRef = new DerivativeStructure(1, 4, 0, x).cos();
        try {
            new DerivativeStructure(1, 4, 0.0, 0.0);
            Assert.fail("an exception should have been thrown");
        } catch (DimensionMismatchException dme) {
            
        } catch (Exception e) {
            Assert.fail("wrong exceptionc caught " + e.getClass().getName());
        }
        double[] derivatives = new double[] { cos, -sin, -cos, sin, cos };
        DerivativeStructure y = new DerivativeStructure(1,  4, derivatives);
        checkEquals(yRef, y, 1.0e-15);
        TestUtils.assertEquals(derivatives, y.getAllDerivatives(), 1.0e-15);
    }

// org.apache.commons.math3.analysis.differentiation.DerivativeStructureTest::testOneOrderConstructor
    public void testOneOrderConstructor() {
        double x =  1.2;
        double y =  2.4;
        double z = 12.5;
        DerivativeStructure xRef = new DerivativeStructure(3, 1, 0, x);
        DerivativeStructure yRef = new DerivativeStructure(3, 1, 1, y);
        DerivativeStructure zRef = new DerivativeStructure(3, 1, 2, z);
        try {
            new DerivativeStructure(3, 1, x + y - z, 1.0, 1.0);
            Assert.fail("an exception should have been thrown");
        } catch (DimensionMismatchException dme) {
            
        } catch (Exception e) {
            Assert.fail("wrong exceptionc caught " + e.getClass().getName());
        }
        double[] derivatives = new double[] { x + y - z, 1.0, 1.0, -1.0 };
        DerivativeStructure t = new DerivativeStructure(3, 1, derivatives);
        checkEquals(xRef.add(yRef.subtract(zRef)), t, 1.0e-15);
        TestUtils.assertEquals(derivatives, xRef.add(yRef.subtract(zRef)).getAllDerivatives(), 1.0e-15);
    }

// org.apache.commons.math3.analysis.differentiation.DerivativeStructureTest::testLinearCombination1DSDS
    public void testLinearCombination1DSDS() {
        final DerivativeStructure[] a = new DerivativeStructure[] {
            new DerivativeStructure(6, 1, 0, -1321008684645961.0 / 268435456.0),
            new DerivativeStructure(6, 1, 1, -5774608829631843.0 / 268435456.0),
            new DerivativeStructure(6, 1, 2, -7645843051051357.0 / 8589934592.0)
        };
        final DerivativeStructure[] b = new DerivativeStructure[] {
            new DerivativeStructure(6, 1, 3, -5712344449280879.0 / 2097152.0),
            new DerivativeStructure(6, 1, 4, -4550117129121957.0 / 2097152.0),
            new DerivativeStructure(6, 1, 5, 8846951984510141.0 / 131072.0)
        };

        final DerivativeStructure abSumInline = a[0].linearCombination(a[0], b[0], a[1], b[1], a[2], b[2]);
        final DerivativeStructure abSumArray = a[0].linearCombination(a, b);

        Assert.assertEquals(abSumInline.getValue(), abSumArray.getValue(), 0);
        Assert.assertEquals(-1.8551294182586248737720779899, abSumInline.getValue(), 1.0e-15);
        Assert.assertEquals(b[0].getValue(), abSumInline.getPartialDerivative(1, 0, 0, 0, 0, 0), 1.0e-15);
        Assert.assertEquals(b[1].getValue(), abSumInline.getPartialDerivative(0, 1, 0, 0, 0, 0), 1.0e-15);
        Assert.assertEquals(b[2].getValue(), abSumInline.getPartialDerivative(0, 0, 1, 0, 0, 0), 1.0e-15);
        Assert.assertEquals(a[0].getValue(), abSumInline.getPartialDerivative(0, 0, 0, 1, 0, 0), 1.0e-15);
        Assert.assertEquals(a[1].getValue(), abSumInline.getPartialDerivative(0, 0, 0, 0, 1, 0), 1.0e-15);
        Assert.assertEquals(a[2].getValue(), abSumInline.getPartialDerivative(0, 0, 0, 0, 0, 1), 1.0e-15);

    }

// org.apache.commons.math3.analysis.differentiation.DerivativeStructureTest::testLinearCombination1DoubleDS
    public void testLinearCombination1DoubleDS() {
        final double[] a = new double[] {
            -1321008684645961.0 / 268435456.0,
            -5774608829631843.0 / 268435456.0,
            -7645843051051357.0 / 8589934592.0
        };
        final DerivativeStructure[] b = new DerivativeStructure[] {
            new DerivativeStructure(3, 1, 0, -5712344449280879.0 / 2097152.0),
            new DerivativeStructure(3, 1, 1, -4550117129121957.0 / 2097152.0),
            new DerivativeStructure(3, 1, 2, 8846951984510141.0 / 131072.0)
        };

        final DerivativeStructure abSumInline = b[0].linearCombination(a[0], b[0],
                                                                       a[1], b[1],
                                                                       a[2], b[2]);
        final DerivativeStructure abSumArray = b[0].linearCombination(a, b);

        Assert.assertEquals(abSumInline.getValue(), abSumArray.getValue(), 0);
        Assert.assertEquals(-1.8551294182586248737720779899, abSumInline.getValue(), 1.0e-15);
        Assert.assertEquals(a[0], abSumInline.getPartialDerivative(1, 0, 0), 1.0e-15);
        Assert.assertEquals(a[1], abSumInline.getPartialDerivative(0, 1, 0), 1.0e-15);
        Assert.assertEquals(a[2], abSumInline.getPartialDerivative(0, 0, 1), 1.0e-15);

    }

// org.apache.commons.math3.analysis.differentiation.DerivativeStructureTest::testLinearCombination2DSDS
    public void testLinearCombination2DSDS() {
        
        
        Well1024a random = new Well1024a(0xc6af886975069f11l);

        for (int i = 0; i < 10000; ++i) {
            final DerivativeStructure[] u = new DerivativeStructure[4];
            final DerivativeStructure[] v = new DerivativeStructure[4];
            for (int j = 0; j < u.length; ++j) {
                u[j] = new DerivativeStructure(u.length, 1, j, 1e17 * random.nextDouble());
                v[j] = new DerivativeStructure(u.length, 1, 1e17 * random.nextDouble());
            }

            DerivativeStructure lin = u[0].linearCombination(u[0], v[0], u[1], v[1]);
            double ref = u[0].getValue() * v[0].getValue() +
                         u[1].getValue() * v[1].getValue();
            Assert.assertEquals(ref, lin.getValue(), 1.0e-15 * FastMath.abs(ref));
            Assert.assertEquals(v[0].getValue(), lin.getPartialDerivative(1, 0, 0, 0), 1.0e-15 * FastMath.abs(v[0].getValue()));
            Assert.assertEquals(v[1].getValue(), lin.getPartialDerivative(0, 1, 0, 0), 1.0e-15 * FastMath.abs(v[1].getValue()));

            lin = u[0].linearCombination(u[0], v[0], u[1], v[1], u[2], v[2]);
            ref = u[0].getValue() * v[0].getValue() +
                  u[1].getValue() * v[1].getValue() +
                  u[2].getValue() * v[2].getValue();
            Assert.assertEquals(ref, lin.getValue(), 1.0e-15 * FastMath.abs(ref));
            Assert.assertEquals(v[0].getValue(), lin.getPartialDerivative(1, 0, 0, 0), 1.0e-15 * FastMath.abs(v[0].getValue()));
            Assert.assertEquals(v[1].getValue(), lin.getPartialDerivative(0, 1, 0, 0), 1.0e-15 * FastMath.abs(v[1].getValue()));
            Assert.assertEquals(v[2].getValue(), lin.getPartialDerivative(0, 0, 1, 0), 1.0e-15 * FastMath.abs(v[2].getValue()));

            lin = u[0].linearCombination(u[0], v[0], u[1], v[1], u[2], v[2], u[3], v[3]);
            ref = u[0].getValue() * v[0].getValue() +
                  u[1].getValue() * v[1].getValue() +
                  u[2].getValue() * v[2].getValue() +
                  u[3].getValue() * v[3].getValue();
            Assert.assertEquals(ref, lin.getValue(), 1.0e-15 * FastMath.abs(ref));
            Assert.assertEquals(v[0].getValue(), lin.getPartialDerivative(1, 0, 0, 0), 1.0e-15 * FastMath.abs(v[0].getValue()));
            Assert.assertEquals(v[1].getValue(), lin.getPartialDerivative(0, 1, 0, 0), 1.0e-15 * FastMath.abs(v[1].getValue()));
            Assert.assertEquals(v[2].getValue(), lin.getPartialDerivative(0, 0, 1, 0), 1.0e-15 * FastMath.abs(v[2].getValue()));
            Assert.assertEquals(v[3].getValue(), lin.getPartialDerivative(0, 0, 0, 1), 1.0e-15 * FastMath.abs(v[3].getValue()));

        }
    }

// org.apache.commons.math3.analysis.differentiation.DerivativeStructureTest::testLinearCombination2DoubleDS
    public void testLinearCombination2DoubleDS() {
        
        
        Well1024a random = new Well1024a(0xc6af886975069f11l);

        for (int i = 0; i < 10000; ++i) {
            final double[] u = new double[4];
            final DerivativeStructure[] v = new DerivativeStructure[4];
            for (int j = 0; j < u.length; ++j) {
                u[j] = 1e17 * random.nextDouble();
                v[j] = new DerivativeStructure(u.length, 1, j, 1e17 * random.nextDouble());
            }

            DerivativeStructure lin = v[0].linearCombination(u[0], v[0], u[1], v[1]);
            double ref = u[0] * v[0].getValue() +
                         u[1] * v[1].getValue();
            Assert.assertEquals(ref, lin.getValue(), 1.0e-15 * FastMath.abs(ref));
            Assert.assertEquals(u[0], lin.getPartialDerivative(1, 0, 0, 0), 1.0e-15 * FastMath.abs(v[0].getValue()));
            Assert.assertEquals(u[1], lin.getPartialDerivative(0, 1, 0, 0), 1.0e-15 * FastMath.abs(v[1].getValue()));

            lin = v[0].linearCombination(u[0], v[0], u[1], v[1], u[2], v[2]);
            ref = u[0] * v[0].getValue() +
                  u[1] * v[1].getValue() +
                  u[2] * v[2].getValue();
            Assert.assertEquals(ref, lin.getValue(), 1.0e-15 * FastMath.abs(ref));
            Assert.assertEquals(u[0], lin.getPartialDerivative(1, 0, 0, 0), 1.0e-15 * FastMath.abs(v[0].getValue()));
            Assert.assertEquals(u[1], lin.getPartialDerivative(0, 1, 0, 0), 1.0e-15 * FastMath.abs(v[1].getValue()));
            Assert.assertEquals(u[2], lin.getPartialDerivative(0, 0, 1, 0), 1.0e-15 * FastMath.abs(v[2].getValue()));

            lin = v[0].linearCombination(u[0], v[0], u[1], v[1], u[2], v[2], u[3], v[3]);
            ref = u[0] * v[0].getValue() +
                  u[1] * v[1].getValue() +
                  u[2] * v[2].getValue() +
                  u[3] * v[3].getValue();
            Assert.assertEquals(ref, lin.getValue(), 1.0e-15 * FastMath.abs(ref));
            Assert.assertEquals(u[0], lin.getPartialDerivative(1, 0, 0, 0), 1.0e-15 * FastMath.abs(v[0].getValue()));
            Assert.assertEquals(u[1], lin.getPartialDerivative(0, 1, 0, 0), 1.0e-15 * FastMath.abs(v[1].getValue()));
            Assert.assertEquals(u[2], lin.getPartialDerivative(0, 0, 1, 0), 1.0e-15 * FastMath.abs(v[2].getValue()));
            Assert.assertEquals(u[3], lin.getPartialDerivative(0, 0, 0, 1), 1.0e-15 * FastMath.abs(v[3].getValue()));

        }
    }

// org.apache.commons.math3.analysis.differentiation.DerivativeStructureTest::testSerialization
    public void testSerialization() {
        DerivativeStructure a = new DerivativeStructure(3, 2, 0, 1.3);
        DerivativeStructure b = (DerivativeStructure) TestUtils.serializeAndRecover(a);
        Assert.assertEquals(a.getFreeParameters(), b.getFreeParameters());
        Assert.assertEquals(a.getOrder(), b.getOrder());
        checkEquals(a, b, 1.0e-15);
    }

// org.apache.commons.math3.analysis.function.StepFunctionTest::testPreconditions1
    public void testPreconditions1() {
        new StepFunction(null, new double[] {0, -1, -2});
    }

// org.apache.commons.math3.analysis.function.StepFunctionTest::testPreconditions2
    public void testPreconditions2() {
        new StepFunction(new double[] {0, 1}, null);
    }

// org.apache.commons.math3.analysis.function.StepFunctionTest::testPreconditions3
    public void testPreconditions3() {
        new StepFunction(new double[] {0}, new double[] {});
    }

// org.apache.commons.math3.analysis.function.StepFunctionTest::testPreconditions4
    public void testPreconditions4() {
        new StepFunction(new double[] {}, new double[] {0});
    }

// org.apache.commons.math3.analysis.function.StepFunctionTest::testPreconditions5
    public void testPreconditions5() {
        new StepFunction(new double[] {0, 1}, new double[] {0, -1, -2});
    }

// org.apache.commons.math3.analysis.function.StepFunctionTest::testPreconditions6
    public void testPreconditions6() {
        new StepFunction(new double[] {1, 0, 1}, new double[] {0, -1, -2});
    }

// org.apache.commons.math3.analysis.function.StepFunctionTest::testSomeValues
    public void testSomeValues() {
        final double[] x = { -2, -0.5, 0, 1.9, 7.4, 21.3 };
        final double[] y = { 4, -1, -5.5, 0.4, 5.8, 51.2 };

        final UnivariateFunction f = new StepFunction(x, y);

        Assert.assertEquals(4, f.value(Double.NEGATIVE_INFINITY), EPS);
        Assert.assertEquals(4, f.value(-10), EPS);
        Assert.assertEquals(-1, f.value(-0.4), EPS);
        Assert.assertEquals(-5.5, f.value(0), EPS);
        Assert.assertEquals(0.4, f.value(2), EPS);
        Assert.assertEquals(5.8, f.value(10), EPS);
        Assert.assertEquals(51.2, f.value(30), EPS);
        Assert.assertEquals(51.2, f.value(Double.POSITIVE_INFINITY), EPS);
    }

// org.apache.commons.math3.analysis.function.StepFunctionTest::testEndpointBehavior
    public void testEndpointBehavior() {
        final double[] x = {0, 1, 2, 3};
        final double[] xp = {-8, 1, 2, 3};
        final double[] y = {1, 2, 3, 4};
        final UnivariateFunction f = new StepFunction(x, y);
        final UnivariateFunction fp = new StepFunction(xp, y);
        Assert.assertEquals(f.value(-8), fp.value(-8), EPS);
        Assert.assertEquals(f.value(-10), fp.value(-10), EPS);
        Assert.assertEquals(f.value(0), fp.value(0), EPS);
        Assert.assertEquals(f.value(0.5), fp.value(0.5), EPS);
        for (int i = 0; i < x.length; i++) {
           Assert.assertEquals(y[i], f.value(x[i]), EPS);
           if (i > 0) {
               Assert.assertEquals(y[i - 1], f.value(x[i] - 0.5), EPS); 
           } else {
               Assert.assertEquals(y[0], f.value(x[i] - 0.5), EPS); 
           }
        }
    }

// org.apache.commons.math3.analysis.function.StepFunctionTest::testHeaviside
    public void testHeaviside() {   
        final UnivariateFunction h = new StepFunction(new double[] {-1, 0},
                                                          new double[] {0, 1});

        Assert.assertEquals(0, h.value(Double.NEGATIVE_INFINITY), 0);
        Assert.assertEquals(0, h.value(-Double.MAX_VALUE), 0);
        Assert.assertEquals(0, h.value(-2), 0);
        Assert.assertEquals(0, h.value(-Double.MIN_VALUE), 0);
        Assert.assertEquals(1, h.value(0), 0);
        Assert.assertEquals(1, h.value(2), 0);
        Assert.assertEquals(1, h.value(Double.POSITIVE_INFINITY), 0);
    }

// org.apache.commons.math3.analysis.integration.IterativeLegendreGaussIntegratorTest::testSinFunction
    public void testSinFunction() {
        UnivariateFunction f = new Sin();
        BaseAbstractUnivariateIntegrator integrator
            = new IterativeLegendreGaussIntegrator(5, 1.0e-14, 1.0e-10, 2, 15);
        double min, max, expected, result, tolerance;

        min = 0; max = FastMath.PI; expected = 2;
        tolerance = FastMath.max(integrator.getAbsoluteAccuracy(),
                             FastMath.abs(expected * integrator.getRelativeAccuracy()));
        result = integrator.integrate(10000, f, min, max);
        Assert.assertEquals(expected, result, tolerance);

        min = -FastMath.PI/3; max = 0; expected = -0.5;
        tolerance = FastMath.max(integrator.getAbsoluteAccuracy(),
                FastMath.abs(expected * integrator.getRelativeAccuracy()));
        result = integrator.integrate(10000, f, min, max);
        Assert.assertEquals(expected, result, tolerance);
    }

// org.apache.commons.math3.analysis.integration.IterativeLegendreGaussIntegratorTest::testQuinticFunction
    public void testQuinticFunction() {
        UnivariateFunction f = new QuinticFunction();
        UnivariateIntegrator integrator =
                new IterativeLegendreGaussIntegrator(3,
                                                     BaseAbstractUnivariateIntegrator.DEFAULT_RELATIVE_ACCURACY,
                                                     BaseAbstractUnivariateIntegrator.DEFAULT_ABSOLUTE_ACCURACY,
                                                     BaseAbstractUnivariateIntegrator.DEFAULT_MIN_ITERATIONS_COUNT,
                                                     64);
        double min, max, expected, result;

        min = 0; max = 1; expected = -1.0/48;
        result = integrator.integrate(10000, f, min, max);
        Assert.assertEquals(expected, result, 1.0e-16);

        min = 0; max = 0.5; expected = 11.0/768;
        result = integrator.integrate(10000, f, min, max);
        Assert.assertEquals(expected, result, 1.0e-16);

        min = -1; max = 4; expected = 2048/3.0 - 78 + 1.0/48;
        result = integrator.integrate(10000, f, min, max);
        Assert.assertEquals(expected, result, 1.0e-16);
    }

// org.apache.commons.math3.analysis.integration.IterativeLegendreGaussIntegratorTest::testExactIntegration
    public void testExactIntegration() {
        Random random = new Random(86343623467878363l);
        for (int n = 2; n < 6; ++n) {
            IterativeLegendreGaussIntegrator integrator =
                new IterativeLegendreGaussIntegrator(n,
                                                     BaseAbstractUnivariateIntegrator.DEFAULT_RELATIVE_ACCURACY,
                                                     BaseAbstractUnivariateIntegrator.DEFAULT_ABSOLUTE_ACCURACY,
                                                     BaseAbstractUnivariateIntegrator.DEFAULT_MIN_ITERATIONS_COUNT,
                                                     64);

            
            for (int degree = 0; degree <= 2 * n - 1; ++degree) {
                for (int i = 0; i < 10; ++i) {
                    double[] coeff = new double[degree + 1];
                    for (int k = 0; k < coeff.length; ++k) {
                        coeff[k] = 2 * random.nextDouble() - 1;
                    }
                    PolynomialFunction p = new PolynomialFunction(coeff);
                    double result    = integrator.integrate(10000, p, -5.0, 15.0);
                    double reference = exactIntegration(p, -5.0, 15.0);
                    Assert.assertEquals(n + " " + degree + " " + i, reference, result, 1.0e-12 * (1.0 + FastMath.abs(reference)));
                }
            }

        }
    }

// org.apache.commons.math3.analysis.integration.IterativeLegendreGaussIntegratorTest::testNormalDistributionWithLargeSigma
    public void testNormalDistributionWithLargeSigma() {
        final double sigma = 1000;
        final double mean = 0;
        final double factor = 1 / (sigma * FastMath.sqrt(2 * FastMath.PI));
        final UnivariateFunction normal = new Gaussian(factor, mean, sigma);

        final double tol = 1e-2;
        final IterativeLegendreGaussIntegrator integrator =
            new IterativeLegendreGaussIntegrator(5, tol, tol);

        final double a = -5000;
        final double b = 5000;
        final double s = integrator.integrate(50, normal, a, b);
        Assert.assertEquals(1, s, 1e-5);
    }

// org.apache.commons.math3.analysis.integration.IterativeLegendreGaussIntegratorTest::testIssue464
    public void testIssue464() {
        final double value = 0.2;
        UnivariateFunction f = new UnivariateFunction() {
            public double value(double x) {
                return (x >= 0 && x <= 5) ? value : 0.0;
            }
        };
        IterativeLegendreGaussIntegrator gauss
            = new IterativeLegendreGaussIntegrator(5, 3, 100);

        
        double maxX = 0.32462367623786328;
        Assert.assertEquals(maxX * value, gauss.integrate(Integer.MAX_VALUE, f, -10, maxX), 1.0e-7);
        Assert.assertTrue(gauss.getEvaluations() > 37000000);
        Assert.assertTrue(gauss.getIterations() < 30);

        
        try {
            gauss.integrate(1000, f, -10, maxX);
            Assert.fail("expected TooManyEvaluationsException");
        } catch (TooManyEvaluationsException tmee) {
            
            Assert.assertEquals(1000, tmee.getMax());
        }

        
        double sum1 = gauss.integrate(1000, f, -10, 0);
        int eval1   = gauss.getEvaluations();
        double sum2 = gauss.integrate(1000, f, 0, maxX);
        int eval2   = gauss.getEvaluations();
        Assert.assertEquals(maxX * value, sum1 + sum2, 1.0e-7);
        Assert.assertTrue(eval1 + eval2 < 200);

    }

// org.apache.commons.math3.analysis.integration.gauss.GaussIntegratorTest::testGetWeights
    public void testGetWeights() {
        final double[] points = { 0, 1.2, 3.4 };
        final double[] weights = { 9.8, 7.6, 5.4 };

        final GaussIntegrator integrator
            = new GaussIntegrator(new Pair<double[], double[]>(points, weights));

        Assert.assertEquals(weights.length, integrator.getNumberOfPoints());

        for (int i = 0; i < integrator.getNumberOfPoints(); i++) {
            Assert.assertEquals(weights[i], integrator.getWeight(i), 0d);
        }
    }

// org.apache.commons.math3.analysis.integration.gauss.GaussIntegratorTest::testGetPoints
    public void testGetPoints() {
        final double[] points = { 0, 1.2, 3.4 };
        final double[] weights = { 9.8, 7.6, 5.4 };

        final GaussIntegrator integrator
            = new GaussIntegrator(new Pair<double[], double[]>(points, weights));

        Assert.assertEquals(points.length, integrator.getNumberOfPoints());

        for (int i = 0; i < integrator.getNumberOfPoints(); i++) {
            Assert.assertEquals(points[i], integrator.getPoint(i), 0d);
        }
    }

// org.apache.commons.math3.analysis.integration.gauss.GaussIntegratorTest::testIntegrate
    public void testIntegrate() {
        final double[] points = { 0, 1, 2, 3, 4, 5 };
        final double[] weights = { 1, 1, 1, 1, 1, 1 };

        final GaussIntegrator integrator
            = new GaussIntegrator(new Pair<double[], double[]>(points, weights));

        final double val = 123.456;
        final UnivariateFunction c = new Constant(val);

        final double s = integrator.integrate(c);
        Assert.assertEquals(points.length * val, s, 0d);
    }

// org.apache.commons.math3.analysis.integration.gauss.HermiteTest::testNormalDistribution
    public void testNormalDistribution() {
        final double oneOverSqrtPi = 1 / FastMath.sqrt(Math.PI);

        final double mu = 12345.6789;
        final double sigma = 987.654321;
        
        
        final int numPoints = 1;

        
        
        
        
        
        
        final UnivariateFunction f = new UnivariateFunction() {
                public double value(double y) {
                    return oneOverSqrtPi; 
                }
            };

        final GaussIntegrator integrator = factory.hermite(numPoints);
        final double result = integrator.integrate(f);
        final double expected = 1;
        Assert.assertEquals(expected, result, Math.ulp(expected));
    }

// org.apache.commons.math3.analysis.integration.gauss.HermiteTest::testNormalMean
    public void testNormalMean() {
        final double sqrtTwo = FastMath.sqrt(2);
        final double oneOverSqrtPi = 1 / FastMath.sqrt(Math.PI);

        final double mu = 12345.6789;
        final double sigma = 987.654321;
        final int numPoints = 5;

        
        
        
        
        
        
        final UnivariateFunction f = new UnivariateFunction() {
                public double value(double y) {
                    return oneOverSqrtPi * (sqrtTwo * sigma * y + mu);
                }
            };

        final GaussIntegrator integrator = factory.hermite(numPoints);
        final double result = integrator.integrate(f);
        final double expected = mu;
        Assert.assertEquals(expected, result, Math.ulp(expected));
    }

// org.apache.commons.math3.analysis.integration.gauss.HermiteTest::testNormalVariance
    public void testNormalVariance() {
        final double twoOverSqrtPi = 2 / FastMath.sqrt(Math.PI);

        final double mu = 12345.6789;
        final double sigma = 987.654321;
        final double sigma2 = sigma * sigma;
        final int numPoints = 5;

        
        
        
        
        
        
        final UnivariateFunction f = new UnivariateFunction() {
                public double value(double y) {
                    return twoOverSqrtPi * sigma2 * y * y;
                }
            };

        final GaussIntegrator integrator = factory.hermite(numPoints);
        final double result = integrator.integrate(f);
        final double expected = sigma2;
        Assert.assertEquals(expected, result, 10 * Math.ulp(expected));
    }

// org.apache.commons.math3.analysis.integration.gauss.LegendreHighPrecisionTest::testCos
    public void testCos() {
        final UnivariateFunction cos = new Cos();

        final GaussIntegrator integrator = factory.legendreHighPrecision(7, 0, Math.PI / 2);
        final double s = integrator.integrate(cos);
        
        Assert.assertEquals(1, s, Math.ulp(1d));
    }

// org.apache.commons.math3.analysis.integration.gauss.LegendreHighPrecisionTest::testInverse
    public void testInverse() {
        final UnivariateFunction inv = new Inverse();
        final UnivariateFunction log = new Log();

        final double lo = 12.34;
        final double hi = 456.78;

        final GaussIntegrator integrator = factory.legendreHighPrecision(60, lo, hi);
        final double s = integrator.integrate(inv);
        final double expected = log.value(hi) - log.value(lo);
        
        Assert.assertEquals(expected, s, 1e-15);
    }

// org.apache.commons.math3.analysis.integration.gauss.LegendreTest::testCos
    public void testCos() {
        final UnivariateFunction cos = new Cos();

        final GaussIntegrator integrator = factory.legendre(7, 0, Math.PI / 2);
        final double s = integrator.integrate(cos);
        
        Assert.assertEquals(1, s, Math.ulp(1d));
    }

// org.apache.commons.math3.analysis.integration.gauss.LegendreTest::testInverse
    public void testInverse() {
        final UnivariateFunction inv = new Inverse();
        final UnivariateFunction log = new Log();

        final double lo = 12.34;
        final double hi = 456.78;

        final GaussIntegrator integrator = factory.legendre(60, lo, hi);
        final double s = integrator.integrate(inv);
        final double expected = log.value(hi) - log.value(lo);
        
        Assert.assertEquals(expected, s, 1e-14);
    }

// org.apache.commons.math3.analysis.interpolation.BicubicSplineInterpolatingFunctionTest::testPreconditions
    public void testPreconditions() {
        double[] xval = new double[] {3, 4, 5, 6.5};
        double[] yval = new double[] {-4, -3, -1, 2.5};
        double[][] zval = new double[xval.length][yval.length];

        @SuppressWarnings("unused")
        BivariateFunction bcf = new BicubicSplineInterpolatingFunction(xval, yval, zval,
                                                                           zval, zval, zval);
        
        double[] wxval = new double[] {3, 2, 5, 6.5};
        try {
            bcf = new BicubicSplineInterpolatingFunction(wxval, yval, zval, zval, zval, zval);
            Assert.fail("an exception should have been thrown");
        } catch (MathIllegalArgumentException e) {
            
        }
        double[] wyval = new double[] {-4, -1, -1, 2.5};
        try {
            bcf = new BicubicSplineInterpolatingFunction(xval, wyval, zval, zval, zval, zval);
            Assert.fail("an exception should have been thrown");
        } catch (MathIllegalArgumentException e) {
            
        }
        double[][] wzval = new double[xval.length][yval.length - 1];
        try {
            bcf = new BicubicSplineInterpolatingFunction(xval, yval, wzval, zval, zval, zval);
            Assert.fail("an exception should have been thrown");
        } catch (DimensionMismatchException e) {
            
        }
        try {
            bcf = new BicubicSplineInterpolatingFunction(xval, yval, zval, wzval, zval, zval);
            Assert.fail("an exception should have been thrown");
        } catch (DimensionMismatchException e) {
            
        }
        try {
            bcf = new BicubicSplineInterpolatingFunction(xval, yval, zval, zval, wzval, zval);
            Assert.fail("an exception should have been thrown");
        } catch (DimensionMismatchException e) {
            
        }
        try {
            bcf = new BicubicSplineInterpolatingFunction(xval, yval, zval, zval, zval, wzval);
            Assert.fail("an exception should have been thrown");
        } catch (DimensionMismatchException e) {
            
        }

        wzval = new double[xval.length - 1][yval.length];
        try {
            bcf = new BicubicSplineInterpolatingFunction(xval, yval, wzval, zval, zval, zval);
            Assert.fail("an exception should have been thrown");
        } catch (DimensionMismatchException e) {
            
        }
        try {
            bcf = new BicubicSplineInterpolatingFunction(xval, yval, zval, wzval, zval, zval);
            Assert.fail("an exception should have been thrown");
        } catch (DimensionMismatchException e) {
            
        }
        try {
            bcf = new BicubicSplineInterpolatingFunction(xval, yval, zval, zval, wzval, zval);
            Assert.fail("an exception should have been thrown");
        } catch (DimensionMismatchException e) {
            
        }
        try {
            bcf = new BicubicSplineInterpolatingFunction(xval, yval, zval, zval, zval, wzval);
            Assert.fail("an exception should have been thrown");
        } catch (DimensionMismatchException e) {
            
        }
    }

// org.apache.commons.math3.analysis.interpolation.BicubicSplineInterpolatingFunctionTest::testPlane
    public void testPlane() {
        double[] xval = new double[] {3, 4, 5, 6.5};
        double[] yval = new double[] {-4, -3, -1, 2, 2.5};
        
        BivariateFunction f = new BivariateFunction() {
                public double value(double x, double y) {
                    return 2 * x - 3 * y + 5;
                }
            };
        double[][] zval = new double[xval.length][yval.length];
        for (int i = 0; i < xval.length; i++) {
            for (int j = 0; j < yval.length; j++) {
                zval[i][j] = f.value(xval[i], yval[j]);
            }
        }
        
        double[][] dZdX = new double[xval.length][yval.length];
        for (int i = 0; i < xval.length; i++) {
            for (int j = 0; j < yval.length; j++) {
                dZdX[i][j] = 2;
            }
        }
        
        double[][] dZdY = new double[xval.length][yval.length];
        for (int i = 0; i < xval.length; i++) {
            for (int j = 0; j < yval.length; j++) {
                dZdY[i][j] = -3;
            }
        }
        
        double[][] dZdXdY = new double[xval.length][yval.length];
        for (int i = 0; i < xval.length; i++) {
            for (int j = 0; j < yval.length; j++) {
                dZdXdY[i][j] = 0;
            }
        }

        BivariateFunction bcf = new BicubicSplineInterpolatingFunction(xval, yval, zval,
                                                                           dZdX, dZdY, dZdXdY);
        double x, y;
        double expected, result;

        x = 4;
        y = -3;
        expected = f.value(x, y);
        result = bcf.value(x, y);
        Assert.assertEquals("On sample point",
                            expected, result, 1e-15);

        x = 4.5;
        y = -1.5;
        expected = f.value(x, y);
        result = bcf.value(x, y);
        Assert.assertEquals("Half-way between sample points (middle of the patch)",
                            expected, result, 0.3);

        x = 3.5;
        y = -3.5;
        expected = f.value(x, y);
        result = bcf.value(x, y);
        Assert.assertEquals("Half-way between sample points (border of the patch)",
                            expected, result, 0.3);
    }

// org.apache.commons.math3.analysis.interpolation.BicubicSplineInterpolatingFunctionTest::testParaboloid
    public void testParaboloid() {
        double[] xval = new double[] {3, 4, 5, 6.5};
        double[] yval = new double[] {-4, -3, -1, 2, 2.5};
        
        BivariateFunction f = new BivariateFunction() {
                public double value(double x, double y) {
                    return 2 * x * x - 3 * y * y + 4 * x * y - 5;
                }
            };
        double[][] zval = new double[xval.length][yval.length];
        for (int i = 0; i < xval.length; i++) {
            for (int j = 0; j < yval.length; j++) {
                zval[i][j] = f.value(xval[i], yval[j]);
            }
        }
        
        double[][] dZdX = new double[xval.length][yval.length];
        BivariateFunction dfdX = new BivariateFunction() {
                public double value(double x, double y) {
                    return 4 * (x + y);
                }
            };
        for (int i = 0; i < xval.length; i++) {
            for (int j = 0; j < yval.length; j++) {
                dZdX[i][j] = dfdX.value(xval[i], yval[j]);
            }
        }
        
        double[][] dZdY = new double[xval.length][yval.length];
        BivariateFunction dfdY = new BivariateFunction() {
                public double value(double x, double y) {
                    return 4 * x - 6 * y;
                }
            };
        for (int i = 0; i < xval.length; i++) {
            for (int j = 0; j < yval.length; j++) {
                dZdY[i][j] = dfdY.value(xval[i], yval[j]);
            }
        }
        
        double[][] dZdXdY = new double[xval.length][yval.length];
        for (int i = 0; i < xval.length; i++) {
            for (int j = 0; j < yval.length; j++) {
                dZdXdY[i][j] = 4;
            }
        }

        BivariateFunction bcf = new BicubicSplineInterpolatingFunction(xval, yval, zval,
                                                                           dZdX, dZdY, dZdXdY);
        double x, y;
        double expected, result;
        
        x = 4;
        y = -3;
        expected = f.value(x, y);
        result = bcf.value(x, y);
        Assert.assertEquals("On sample point",
                            expected, result, 1e-15);

        x = 4.5;
        y = -1.5;
        expected = f.value(x, y);
        result = bcf.value(x, y);
        Assert.assertEquals("Half-way between sample points (middle of the patch)",
                            expected, result, 2);

        x = 3.5;
        y = -3.5;
        expected = f.value(x, y);
        result = bcf.value(x, y);
        Assert.assertEquals("Half-way between sample points (border of the patch)",
                            expected, result, 2);
    }

// org.apache.commons.math3.analysis.interpolation.BicubicSplineInterpolatingFunctionTest::testSplinePartialDerivatives
    public void testSplinePartialDerivatives() {
        final int N = 4;
        final double[] coeff = new double[16];

        for (int i = 0; i < N; i++) {
            for (int j = 0; j < N; j++) {
                coeff[i + N * j] = (i + 1) * (j + 2);
            }
        }

        final BicubicSplineFunction f = new BicubicSplineFunction(coeff);
        BivariateFunction derivative;
        final double x = 0.435;
        final double y = 0.776;
        final double tol = 1e-13;

        derivative = new BivariateFunction() {
                public double value(double x, double y) {
                    final double x2 = x * x;
                    final double y2 = y * y;
                    final double y3 = y2 * y;
                    final double yFactor = 2 + 3 * y + 4 * y2 + 5 * y3;
                    return yFactor * (2 + 6 * x + 12 * x2);
                }
            };
        Assert.assertEquals("dFdX", derivative.value(x, y),
                            f.partialDerivativeX().value(x, y), tol);
        
        derivative = new BivariateFunction() {
                public double value(double x, double y) {
                    final double x2 = x * x;
                    final double x3 = x2 * x;
                    final double y2 = y * y;
                    final double xFactor = 1 + 2 * x + 3 * x2 + 4 * x3;
                    return xFactor * (3 + 8 * y + 15 * y2);
                }
            };
        Assert.assertEquals("dFdY", derivative.value(x, y),
                            f.partialDerivativeY().value(x, y), tol);

        derivative = new BivariateFunction() {
                public double value(double x, double y) {
                    final double y2 = y * y;
                    final double y3 = y2 * y;
                    final double yFactor = 2 + 3 * y + 4 * y2 + 5 * y3;
                    return yFactor * (6 + 24 * x);
                }
            };
        Assert.assertEquals("d2FdX2", derivative.value(x, y),
                            f.partialDerivativeXX().value(x, y), tol);

        derivative = new BivariateFunction() {
                public double value(double x, double y) {
                    final double x2 = x * x;
                    final double x3 = x2 * x;
                    final double xFactor = 1 + 2 * x + 3 * x2 + 4 * x3;
                    return xFactor * (8 + 30 * y);
                }
            };
        Assert.assertEquals("d2FdY2", derivative.value(x, y),
                            f.partialDerivativeYY().value(x, y), tol);

        derivative = new BivariateFunction() {
                public double value(double x, double y) {
                    final double x2 = x * x;
                    final double y2 = y * y;
                    final double yFactor = 3 + 8 * y + 15 * y2;
                    return yFactor * (2 + 6 * x + 12 * x2);
                }
            };
        Assert.assertEquals("d2FdXdY", derivative.value(x, y),
                            f.partialDerivativeXY().value(x, y), tol);
    }

// org.apache.commons.math3.analysis.interpolation.BicubicSplineInterpolatingFunctionTest::testMatchingPartialDerivatives
    public void testMatchingPartialDerivatives() {
        final int sz = 21;
        double[] val = new double[sz];
        
        final double delta = 1d / (sz - 1);
        for (int i = 0; i < sz; i++) {
            val[i] = i * delta;
        }
        
        BivariateFunction f = new BivariateFunction() {
                public double value(double x, double y) {
                    final double x2 = x * x;
                    final double x3 = x2 * x;
                    final double y2 = y * y;
                    final double y3 = y2 * y;

                    return 5
                        - 3 * x + 2 * y
                        - x * y + 2 * x2 - 3 * y2
                        + 4 * x2 * y - x * y2 - 3 * x3 + y3;
                }
            };
        double[][] fval = new double[sz][sz];
        for (int i = 0; i < sz; i++) {
            for (int j = 0; j < sz; j++) {
                fval[i][j] = f.value(val[i], val[j]);
            }
        }
        
        double[][] dFdX = new double[sz][sz];
        BivariateFunction dfdX = new BivariateFunction() {
                public double value(double x, double y) {
                    final double x2 = x * x;
                    final double y2 = y * y;                    
                    return - 3 - y + 4 * x + 8 * x * y - y2 - 9 * x2;
                }
            };
        for (int i = 0; i < sz; i++) {
            for (int j = 0; j < sz; j++) {
                dFdX[i][j] = dfdX.value(val[i], val[j]);
            }
        }
        
        double[][] dFdY = new double[sz][sz];
        BivariateFunction dfdY = new BivariateFunction() {
                public double value(double x, double y) {
                    final double x2 = x * x;
                    final double y2 = y * y;                    
                    return 2 - x - 6 * y + 4 * x2 - 2 * x * y + 3 * y2;
                }
            };
        for (int i = 0; i < sz; i++) {
            for (int j = 0; j < sz; j++) {
                dFdY[i][j] = dfdY.value(val[i], val[j]);
            }
        }
        
        double[][] d2FdXdY = new double[sz][sz];
        BivariateFunction d2fdXdY = new BivariateFunction() {
                public double value(double x, double y) {
                    return -1 + 8 * x - 2 * y;
                }
            };
        for (int i = 0; i < sz; i++) {
            for (int j = 0; j < sz; j++) {
                d2FdXdY[i][j] = d2fdXdY.value(val[i], val[j]);
            }
        }

        BicubicSplineInterpolatingFunction bcf
            = new BicubicSplineInterpolatingFunction(val, val, fval, dFdX, dFdY, d2FdXdY);

        double x, y;
        double expected, result;

        final double tol = 1e-12;
        for (int i = 0; i < sz; i++) {
            x = val[i];
            for (int j = 0; j < sz; j++) {
                y = val[j];
                
                expected = dfdX.value(x, y);
                result = bcf.partialDerivativeX(x, y);
                Assert.assertEquals(x + " " + y + " dFdX", expected, result, tol);

                expected = dfdY.value(x, y);
                result = bcf.partialDerivativeY(x, y);
                Assert.assertEquals(x + " " + y + " dFdY", expected, result, tol);
                
                expected = d2fdXdY.value(x, y);
                result = bcf.partialDerivativeXY(x, y);
                Assert.assertEquals(x + " " + y + " d2FdXdY", expected, result, tol);
            }
        }
    }

// org.apache.commons.math3.analysis.interpolation.BicubicSplineInterpolatingFunctionTest::testInterpolation1
    public void testInterpolation1() {
        final int sz = 21;
        double[] xval = new double[sz];
        double[] yval = new double[sz];
        
        final double delta = 1d / (sz - 1);
        for (int i = 0; i < sz; i++) {
            xval[i] = -1 + 15 * i * delta;
            yval[i] = -20 + 30 * i * delta;
        }

        
        BivariateFunction f = new BivariateFunction() {
                public double value(double x, double y) {
                    return 2 * x - 3 * y + 5;
                }
            };
        double[][] zval = new double[xval.length][yval.length];
        for (int i = 0; i < xval.length; i++) {
            for (int j = 0; j < yval.length; j++) {
                zval[i][j] = f.value(xval[i], yval[j]);
            }
        }
        
        double[][] dZdX = new double[xval.length][yval.length];
        for (int i = 0; i < xval.length; i++) {
            for (int j = 0; j < yval.length; j++) {
                dZdX[i][j] = 2;
            }
        }
        
        double[][] dZdY = new double[xval.length][yval.length];
        for (int i = 0; i < xval.length; i++) {
            for (int j = 0; j < yval.length; j++) {
                dZdY[i][j] = -3;
            }
        }
        
        double[][] dZdXdY = new double[xval.length][yval.length];
        for (int i = 0; i < xval.length; i++) {
            for (int j = 0; j < yval.length; j++) {
                dZdXdY[i][j] = 0;
            }
        }

        final BivariateFunction bcf
            = new BicubicSplineInterpolatingFunction(xval, yval, zval,
                                                     dZdX, dZdY, dZdXdY);
        double x, y;

        final RandomGenerator rng = new Well19937c(1234567L); 
        final UniformRealDistribution distX
            = new UniformRealDistribution(rng, xval[0], xval[xval.length - 1]);
        final UniformRealDistribution distY
            = new UniformRealDistribution(rng, yval[0], yval[yval.length - 1]);

        final int numSamples = 50;
        final double tol = 6;
        for (int i = 0; i < numSamples; i++) {
            x = distX.sample();
            for (int j = 0; j < numSamples; j++) {
                y = distY.sample();

                Assert.assertEquals(f.value(x, y),  bcf.value(x, y), tol);
            }

        }
    }

// org.apache.commons.math3.analysis.interpolation.BicubicSplineInterpolatingFunctionTest::testInterpolation2
    public void testInterpolation2() {
        final int sz = 21;
        double[] xval = new double[sz];
        double[] yval = new double[sz];
        
        final double delta = 1d / (sz - 1);
        for (int i = 0; i < sz; i++) {
            xval[i] = -1 + 15 * i * delta;
            yval[i] = -20 + 30 * i * delta;
        }

        
        BivariateFunction f = new BivariateFunction() {
                public double value(double x, double y) {
                    return 2 * x * x - 3 * y * y + 4 * x * y - 5;
                }
            };
        double[][] zval = new double[xval.length][yval.length];
        for (int i = 0; i < xval.length; i++) {
            for (int j = 0; j < yval.length; j++) {
                zval[i][j] = f.value(xval[i], yval[j]);
            }
        }
        
        double[][] dZdX = new double[xval.length][yval.length];
        BivariateFunction dfdX = new BivariateFunction() {
                public double value(double x, double y) {
                    return 4 * (x + y);
                }
            };
        for (int i = 0; i < xval.length; i++) {
            for (int j = 0; j < yval.length; j++) {
                dZdX[i][j] = dfdX.value(xval[i], yval[j]);
            }
        }
        
        double[][] dZdY = new double[xval.length][yval.length];
        BivariateFunction dfdY = new BivariateFunction() {
                public double value(double x, double y) {
                    return 4 * x - 6 * y;
                }
            };
        for (int i = 0; i < xval.length; i++) {
            for (int j = 0; j < yval.length; j++) {
                dZdY[i][j] = dfdY.value(xval[i], yval[j]);
            }
        }
        
        double[][] dZdXdY = new double[xval.length][yval.length];
        for (int i = 0; i < xval.length; i++) {
            for (int j = 0; j < yval.length; j++) {
                dZdXdY[i][j] = 4;
            }
        }

        BivariateFunction bcf = new BicubicSplineInterpolatingFunction(xval, yval, zval,
                                                                       dZdX, dZdY, dZdXdY);
        double x, y;

        final RandomGenerator rng = new Well19937c(1234567L); 
        final UniformRealDistribution distX
            = new UniformRealDistribution(rng, xval[0], xval[xval.length - 1]);
        final UniformRealDistribution distY
            = new UniformRealDistribution(rng, yval[0], yval[yval.length - 1]);

        final double tol = 224;
        double max = 0;
        for (int i = 0; i < sz; i++) {
            x = distX.sample();
            for (int j = 0; j < sz; j++) {
                y = distY.sample();

                Assert.assertEquals(f.value(x, y),  bcf.value(x, y), tol);
            }

        }
    }

// org.apache.commons.math3.analysis.interpolation.BicubicSplineInterpolatingFunctionTest::testIsValidPoint
    public void testIsValidPoint() {
        final double xMin = -12;
        final double xMax = 34;
        final double yMin = 5;
        final double yMax = 67;
        final double[] xval = new double[] { xMin, xMax };
        final double[] yval = new double[] { yMin, yMax };
        final double[][] f = new double[][] { { 1, 2 },
                                              { 3, 4 } };
        final double[][] dFdX = f;
        final double[][] dFdY = f;
        final double[][] dFdXdY = f;

        final BicubicSplineInterpolatingFunction bcf
            = new BicubicSplineInterpolatingFunction(xval, yval, f,
                                                     dFdX, dFdY, dFdXdY);

        double x, y;

        x = xMin;
        y = yMin;
        Assert.assertTrue(bcf.isValidPoint(x, y));
        
        bcf.value(x, y);

        x = xMax;
        y = yMax;
        Assert.assertTrue(bcf.isValidPoint(x, y));
        
        bcf.value(x, y);
 
        final double xRange = xMax - xMin;
        final double yRange = yMax - yMin;
        x = xMin + xRange / 3.4;
        y = yMin + yRange / 1.2;
        Assert.assertTrue(bcf.isValidPoint(x, y));
        
        bcf.value(x, y);

        final double small = 1e-8;
        x = xMin - small;
        y = yMax;
        Assert.assertFalse(bcf.isValidPoint(x, y));
        
        try {
            bcf.value(x, y);
            Assert.fail("OutOfRangeException expected");
        } catch (OutOfRangeException expected) {}

        x = xMin;
        y = yMax + small;
        Assert.assertFalse(bcf.isValidPoint(x, y));
        
        try {
            bcf.value(x, y);
            Assert.fail("OutOfRangeException expected");
        } catch (OutOfRangeException expected) {}
    }

// org.apache.commons.math3.analysis.interpolation.BicubicSplineInterpolatorTest::testPreconditions
    public void testPreconditions() {
        double[] xval = new double[] {3, 4, 5, 6.5};
        double[] yval = new double[] {-4, -3, -1, 2.5};
        double[][] zval = new double[xval.length][yval.length];

        BivariateGridInterpolator interpolator = new BicubicSplineInterpolator();
        
        @SuppressWarnings("unused")
        BivariateFunction p = interpolator.interpolate(xval, yval, zval);
        
        double[] wxval = new double[] {3, 2, 5, 6.5};
        try {
            p = interpolator.interpolate(wxval, yval, zval);
            Assert.fail("an exception should have been thrown");
        } catch (MathIllegalArgumentException e) {
            
        }

        double[] wyval = new double[] {-4, -3, -1, -1};
        try {
            p = interpolator.interpolate(xval, wyval, zval);
            Assert.fail("an exception should have been thrown");
        } catch (MathIllegalArgumentException e) {
            
        }

        double[][] wzval = new double[xval.length][yval.length + 1];
        try {
            p = interpolator.interpolate(xval, yval, wzval);
            Assert.fail("an exception should have been thrown");
        } catch (DimensionMismatchException e) {
            
        }
        wzval = new double[xval.length - 1][yval.length];
        try {
            p = interpolator.interpolate(xval, yval, wzval);
            Assert.fail("an exception should have been thrown");
        } catch (DimensionMismatchException e) {
            
        }
    }

// org.apache.commons.math3.analysis.interpolation.BicubicSplineInterpolatorTest::testInterpolation1
    public void testInterpolation1() {
        final int sz = 21;
        double[] xval = new double[sz];
        double[] yval = new double[sz];
        
        final double delta = 1d / (sz - 1);
        for (int i = 0; i < sz; i++) {
            xval[i] = -1 + 15 * i * delta;
            yval[i] = -20 + 30 * i * delta;
        }

        
        BivariateFunction f = new BivariateFunction() {
                public double value(double x, double y) {
                    return 2 * x - 3 * y + 5;
                }
            };
        double[][] zval = new double[xval.length][yval.length];
        for (int i = 0; i < xval.length; i++) {
            for (int j = 0; j < yval.length; j++) {
                zval[i][j] = f.value(xval[i], yval[j]);
            }
        }

        BivariateGridInterpolator interpolator = new BicubicSplineInterpolator();
        BivariateFunction p = interpolator.interpolate(xval, yval, zval);
        double x, y;

        final RandomGenerator rng = new Well19937c(1234567L); 
        final UniformRealDistribution distX
            = new UniformRealDistribution(rng, xval[0], xval[xval.length - 1]);
        final UniformRealDistribution distY
            = new UniformRealDistribution(rng, yval[0], yval[yval.length - 1]);

        final int numSamples = 50;
        final double tol = 6;
        for (int i = 0; i < numSamples; i++) {
            x = distX.sample();
            for (int j = 0; j < numSamples; j++) {
                y = distY.sample();

                Assert.assertEquals(f.value(x, y),  p.value(x, y), tol);
            }

        }
    }

// org.apache.commons.math3.analysis.interpolation.BicubicSplineInterpolatorTest::testInterpolation2
    public void testInterpolation2() {
        final int sz = 21;
        double[] xval = new double[sz];
        double[] yval = new double[sz];
        
        final double delta = 1d / (sz - 1);
        for (int i = 0; i < sz; i++) {
            xval[i] = -1 + 15 * i * delta;
            yval[i] = -20 + 30 * i * delta;
        }

        
        BivariateFunction f = new BivariateFunction() {
                public double value(double x, double y) {
                    return 2 * x * x - 3 * y * y + 4 * x * y - 5;
                }
            };
        double[][] zval = new double[xval.length][yval.length];
        for (int i = 0; i < xval.length; i++) {
            for (int j = 0; j < yval.length; j++) {
                zval[i][j] = f.value(xval[i], yval[j]);
            }
        }

        BivariateGridInterpolator interpolator = new BicubicSplineInterpolator();
        BivariateFunction p = interpolator.interpolate(xval, yval, zval);
        double x, y;

        final RandomGenerator rng = new Well19937c(1234567L); 
        final UniformRealDistribution distX
            = new UniformRealDistribution(rng, xval[0], xval[xval.length - 1]);
        final UniformRealDistribution distY
            = new UniformRealDistribution(rng, yval[0], yval[yval.length - 1]);

        final int numSamples = 50;
        final double tol = 251;
        for (int i = 0; i < numSamples; i++) {
            x = distX.sample();
            for (int j = 0; j < numSamples; j++) {
                y = distY.sample();

                Assert.assertEquals(f.value(x, y),  p.value(x, y), tol);
            }

        }
    }

// org.apache.commons.math3.analysis.interpolation.DividedDifferenceInterpolatorTest::testSinFunction
    public void testSinFunction() {
        UnivariateFunction f = new Sin();
        UnivariateInterpolator interpolator = new DividedDifferenceInterpolator();
        double x[], y[], z, expected, result, tolerance;

        
        int n = 6;
        double min = 0.0, max = 2 * FastMath.PI;
        x = new double[n];
        y = new double[n];
        for (int i = 0; i < n; i++) {
            x[i] = min + i * (max - min) / n;
            y[i] = f.value(x[i]);
        }
        double derivativebound = 1.0;
        UnivariateFunction p = interpolator.interpolate(x, y);

        z = FastMath.PI / 4; expected = f.value(z); result = p.value(z);
        tolerance = FastMath.abs(derivativebound * partialerror(x, z));
        Assert.assertEquals(expected, result, tolerance);

        z = FastMath.PI * 1.5; expected = f.value(z); result = p.value(z);
        tolerance = FastMath.abs(derivativebound * partialerror(x, z));
        Assert.assertEquals(expected, result, tolerance);
    }

// org.apache.commons.math3.analysis.interpolation.DividedDifferenceInterpolatorTest::testExpm1Function
    public void testExpm1Function() {
        UnivariateFunction f = new Expm1();
        UnivariateInterpolator interpolator = new DividedDifferenceInterpolator();
        double x[], y[], z, expected, result, tolerance;

        
        int n = 5;
        double min = -1.0, max = 1.0;
        x = new double[n];
        y = new double[n];
        for (int i = 0; i < n; i++) {
            x[i] = min + i * (max - min) / n;
            y[i] = f.value(x[i]);
        }
        double derivativebound = FastMath.E;
        UnivariateFunction p = interpolator.interpolate(x, y);

        z = 0.0; expected = f.value(z); result = p.value(z);
        tolerance = FastMath.abs(derivativebound * partialerror(x, z));
        Assert.assertEquals(expected, result, tolerance);

        z = 0.5; expected = f.value(z); result = p.value(z);
        tolerance = FastMath.abs(derivativebound * partialerror(x, z));
        Assert.assertEquals(expected, result, tolerance);

        z = -0.5; expected = f.value(z); result = p.value(z);
        tolerance = FastMath.abs(derivativebound * partialerror(x, z));
        Assert.assertEquals(expected, result, tolerance);
    }

// org.apache.commons.math3.analysis.interpolation.DividedDifferenceInterpolatorTest::testParameters
    public void testParameters() {
        UnivariateInterpolator interpolator = new DividedDifferenceInterpolator();

        try {
            
            double x[] = { 1.0, 2.0, 2.0, 4.0 };
            double y[] = { 0.0, 4.0, 4.0, 2.5 };
            UnivariateFunction p = interpolator.interpolate(x, y);
            p.value(0.0);
            Assert.fail("Expecting NonMonotonicSequenceException - bad abscissas array");
        } catch (NonMonotonicSequenceException ex) {
            
        }
    }

// org.apache.commons.math3.analysis.interpolation.FieldHermiteInterpolatorTest::testZero
    public void testZero() {
        FieldHermiteInterpolator<BigFraction> interpolator = new FieldHermiteInterpolator<BigFraction>();
        interpolator.addSamplePoint(new BigFraction(0), new BigFraction[] { new BigFraction(0) });
        for (int x = -10; x < 10; x++) {
            BigFraction y = interpolator.value(new BigFraction(x))[0];
            Assert.assertEquals(BigFraction.ZERO, y);
            BigFraction[][] derivatives = interpolator.derivatives(new BigFraction(x), 1);
            Assert.assertEquals(BigFraction.ZERO, derivatives[0][0]);
            Assert.assertEquals(BigFraction.ZERO, derivatives[1][0]);
        }
    }

// org.apache.commons.math3.analysis.interpolation.FieldHermiteInterpolatorTest::testQuadratic
    public void testQuadratic() {
        FieldHermiteInterpolator<BigFraction> interpolator = new FieldHermiteInterpolator<BigFraction>();
        interpolator.addSamplePoint(new BigFraction(0), new BigFraction[] { new BigFraction(2) });
        interpolator.addSamplePoint(new BigFraction(1), new BigFraction[] { new BigFraction(0) });
        interpolator.addSamplePoint(new BigFraction(2), new BigFraction[] { new BigFraction(0) });
        for (double x = -10; x < 10; x += 1.0) {
            BigFraction y = interpolator.value(new BigFraction(x))[0];
            Assert.assertEquals((x - 1) * (x - 2), y.doubleValue(), 1.0e-15);
            BigFraction[][] derivatives = interpolator.derivatives(new BigFraction(x), 3);
            Assert.assertEquals((x - 1) * (x - 2), derivatives[0][0].doubleValue(), 1.0e-15);
            Assert.assertEquals(2 * x - 3, derivatives[1][0].doubleValue(), 1.0e-15);
            Assert.assertEquals(2, derivatives[2][0].doubleValue(), 1.0e-15);
            Assert.assertEquals(0, derivatives[3][0].doubleValue(), 1.0e-15);
        }
    }

// org.apache.commons.math3.analysis.interpolation.FieldHermiteInterpolatorTest::testMixedDerivatives
    public void testMixedDerivatives() {
        FieldHermiteInterpolator<BigFraction> interpolator = new FieldHermiteInterpolator<BigFraction>();
        interpolator.addSamplePoint(new BigFraction(0), new BigFraction[] { new BigFraction(1) }, new BigFraction[] { new BigFraction(2) });
        interpolator.addSamplePoint(new BigFraction(1), new BigFraction[] { new BigFraction(4) });
        interpolator.addSamplePoint(new BigFraction(2), new BigFraction[] { new BigFraction(5) }, new BigFraction[] { new BigFraction(2) });
        BigFraction[][] derivatives = interpolator.derivatives(new BigFraction(0), 5);
        Assert.assertEquals(new BigFraction(  1), derivatives[0][0]);
        Assert.assertEquals(new BigFraction(  2), derivatives[1][0]);
        Assert.assertEquals(new BigFraction(  8), derivatives[2][0]);
        Assert.assertEquals(new BigFraction(-24), derivatives[3][0]);
        Assert.assertEquals(new BigFraction( 24), derivatives[4][0]);
        Assert.assertEquals(new BigFraction(  0), derivatives[5][0]);
        derivatives = interpolator.derivatives(new BigFraction(1), 5);
        Assert.assertEquals(new BigFraction(  4), derivatives[0][0]);
        Assert.assertEquals(new BigFraction(  2), derivatives[1][0]);
        Assert.assertEquals(new BigFraction( -4), derivatives[2][0]);
        Assert.assertEquals(new BigFraction(  0), derivatives[3][0]);
        Assert.assertEquals(new BigFraction( 24), derivatives[4][0]);
        Assert.assertEquals(new BigFraction(  0), derivatives[5][0]);
        derivatives = interpolator.derivatives(new BigFraction(2), 5);
        Assert.assertEquals(new BigFraction(  5), derivatives[0][0]);
        Assert.assertEquals(new BigFraction(  2), derivatives[1][0]);
        Assert.assertEquals(new BigFraction(  8), derivatives[2][0]);
        Assert.assertEquals(new BigFraction( 24), derivatives[3][0]);
        Assert.assertEquals(new BigFraction( 24), derivatives[4][0]);
        Assert.assertEquals(new BigFraction(  0), derivatives[5][0]);
    }

// org.apache.commons.math3.analysis.interpolation.FieldHermiteInterpolatorTest::testRandomPolynomialsValuesOnly
    public void testRandomPolynomialsValuesOnly() {

        Random random = new Random(0x42b1e7dbd361a932l);

        for (int i = 0; i < 100; ++i) {

            int maxDegree = 0;
            PolynomialFunction[] p = new PolynomialFunction[5];
            for (int k = 0; k < p.length; ++k) {
                int degree = random.nextInt(7);
                p[k] = randomPolynomial(degree, random);
                maxDegree = FastMath.max(maxDegree, degree);
            }

            DfpField field = new DfpField(30);
            Dfp step = field.getOne().divide(field.newDfp(10));
            FieldHermiteInterpolator<Dfp> interpolator = new FieldHermiteInterpolator<Dfp>();
            for (int j = 0; j < 1 + maxDegree; ++j) {
                Dfp x = field.newDfp(j).multiply(step);
                Dfp[] values = new Dfp[p.length];
                for (int k = 0; k < p.length; ++k) {
                    values[k] = field.newDfp(p[k].value(x.getReal()));
                }
                interpolator.addSamplePoint(x, values);
            }

            for (int j = 0; j < 20; ++j) {
                Dfp x = field.newDfp(j).multiply(step);
                Dfp[] values = interpolator.value(x);
                Assert.assertEquals(p.length, values.length);
                for (int k = 0; k < p.length; ++k) {
                    Assert.assertEquals(p[k].value(x.getReal()),
                                        values[k].getReal(),
                                        1.0e-8 * FastMath.abs(p[k].value(x.getReal())));
                }
            }

        }

    }

// org.apache.commons.math3.analysis.interpolation.FieldHermiteInterpolatorTest::testRandomPolynomialsFirstDerivative
    public void testRandomPolynomialsFirstDerivative() {

        Random random = new Random(0x570803c982ca5d3bl);

        for (int i = 0; i < 100; ++i) {

            int maxDegree = 0;
            PolynomialFunction[] p      = new PolynomialFunction[5];
            PolynomialFunction[] pPrime = new PolynomialFunction[5];
            for (int k = 0; k < p.length; ++k) {
                int degree = random.nextInt(7);
                p[k]      = randomPolynomial(degree, random);
                pPrime[k] = p[k].polynomialDerivative();
                maxDegree = FastMath.max(maxDegree, degree);
            }

            DfpField field = new DfpField(30);
            Dfp step = field.getOne().divide(field.newDfp(10));
            FieldHermiteInterpolator<Dfp> interpolator = new FieldHermiteInterpolator<Dfp>();
            for (int j = 0; j < 1 + maxDegree / 2; ++j) {
                Dfp x = field.newDfp(j).multiply(step);
                Dfp[] values      = new Dfp[p.length];
                Dfp[] derivatives = new Dfp[p.length];
                for (int k = 0; k < p.length; ++k) {
                    values[k]      = field.newDfp(p[k].value(x.getReal()));
                    derivatives[k] = field.newDfp(pPrime[k].value(x.getReal()));
                }
                interpolator.addSamplePoint(x, values, derivatives);
            }

            Dfp h = step.divide(field.newDfp(100000));
            for (int j = 0; j < 20; ++j) {
                Dfp x = field.newDfp(j).multiply(step);
                Dfp[] y  = interpolator.value(x);
                Dfp[] yP = interpolator.value(x.add(h));
                Dfp[] yM = interpolator.value(x.subtract(h));
                Assert.assertEquals(p.length, y.length);
                for (int k = 0; k < p.length; ++k) {
                    Assert.assertEquals(p[k].value(x.getReal()),
                                        y[k].getReal(),
                                        1.0e-8 * FastMath.abs(p[k].value(x.getReal())));
                    Assert.assertEquals(pPrime[k].value(x.getReal()),
                                        yP[k].subtract(yM[k]).divide(h.multiply(2)).getReal(),
                                        4.0e-8 * FastMath.abs(p[k].value(x.getReal())));
                }
            }

        }
    }

// org.apache.commons.math3.analysis.interpolation.FieldHermiteInterpolatorTest::testSine
    public void testSine() {
        DfpField field = new DfpField(30);
        FieldHermiteInterpolator<Dfp> interpolator = new FieldHermiteInterpolator<Dfp>();
        for (Dfp x = field.getZero(); x.getReal() < FastMath.PI; x = x.add(0.5)) {
            interpolator.addSamplePoint(x, new Dfp[] { x.sin() });
        }
        for (Dfp x = field.newDfp(0.1); x.getReal() < 2.9; x = x.add(0.01)) {
            Dfp y = interpolator.value(x)[0];
            Assert.assertEquals( x.sin().getReal(), y.getReal(), 3.5e-5);
        }
    }

// org.apache.commons.math3.analysis.interpolation.FieldHermiteInterpolatorTest::testSquareRoot
    public void testSquareRoot() {
        DfpField field = new DfpField(30);
        FieldHermiteInterpolator<Dfp> interpolator = new FieldHermiteInterpolator<Dfp>();
        for (Dfp x = field.getOne(); x.getReal() < 3.6; x = x.add(0.5)) {
            interpolator.addSamplePoint(x, new Dfp[] { x.sqrt() });
        }
        for (Dfp x = field.newDfp(1.1); x.getReal() < 3.5; x = x.add(0.01)) {
            Dfp y = interpolator.value(x)[0];
            Assert.assertEquals(x.sqrt().getReal(), y.getReal(), 1.5e-4);
        }
    }

// org.apache.commons.math3.analysis.interpolation.FieldHermiteInterpolatorTest::testWikipedia
    public void testWikipedia() {
        
        
        FieldHermiteInterpolator<BigFraction> interpolator = new FieldHermiteInterpolator<BigFraction>();
        interpolator.addSamplePoint(new BigFraction(-1),
                                    new BigFraction[] { new BigFraction( 2) },
                                    new BigFraction[] { new BigFraction(-8) },
                                    new BigFraction[] { new BigFraction(56) });
        interpolator.addSamplePoint(new BigFraction( 0),
                                    new BigFraction[] { new BigFraction( 1) },
                                    new BigFraction[] { new BigFraction( 0) },
                                    new BigFraction[] { new BigFraction( 0) });
        interpolator.addSamplePoint(new BigFraction( 1),
                                    new BigFraction[] { new BigFraction( 2) },
                                    new BigFraction[] { new BigFraction( 8) },
                                    new BigFraction[] { new BigFraction(56) });
        for (BigFraction x = new BigFraction(-1); x.doubleValue() <= 1.0; x = x.add(new BigFraction(1, 8))) {
            BigFraction y = interpolator.value(x)[0];
            BigFraction x2 = x.multiply(x);
            BigFraction x4 = x2.multiply(x2);
            BigFraction x8 = x4.multiply(x4);
            Assert.assertEquals(x8.add(new BigFraction(1)), y);
        }
    }

// org.apache.commons.math3.analysis.interpolation.FieldHermiteInterpolatorTest::testOnePointParabola
    public void testOnePointParabola() {
        FieldHermiteInterpolator<BigFraction> interpolator = new FieldHermiteInterpolator<BigFraction>();
        interpolator.addSamplePoint(new BigFraction(0),
                                    new BigFraction[] { new BigFraction(1) },
                                    new BigFraction[] { new BigFraction(1) },
                                    new BigFraction[] { new BigFraction(2) });
        for (BigFraction x = new BigFraction(-1); x.doubleValue() <= 1.0; x = x.add(new BigFraction(1, 8))) {
            BigFraction y = interpolator.value(x)[0];
            Assert.assertEquals(BigFraction.ONE.add(x.multiply(BigFraction.ONE.add(x))), y);
        }
    }

// org.apache.commons.math3.analysis.interpolation.FieldHermiteInterpolatorTest::testEmptySampleValue
    public void testEmptySampleValue() {
        new FieldHermiteInterpolator<BigFraction>().value(BigFraction.ZERO);
    }

// org.apache.commons.math3.analysis.interpolation.FieldHermiteInterpolatorTest::testEmptySampleDerivative
    public void testEmptySampleDerivative() {
        new FieldHermiteInterpolator<BigFraction>().derivatives(BigFraction.ZERO, 1);
    }

// org.apache.commons.math3.analysis.interpolation.FieldHermiteInterpolatorTest::testDuplicatedAbscissa
    public void testDuplicatedAbscissa() {
        FieldHermiteInterpolator<BigFraction> interpolator = new FieldHermiteInterpolator<BigFraction>();
        interpolator.addSamplePoint(new BigFraction(1), new BigFraction[] { new BigFraction(0) });
        interpolator.addSamplePoint(new BigFraction(1), new BigFraction[] { new BigFraction(1) });
    }

// org.apache.commons.math3.analysis.interpolation.LinearInterpolatorTest::testInterpolateLinearDegenerateTwoSegment
    public void testInterpolateLinearDegenerateTwoSegment()
        {
        double x[] = { 0.0, 0.5, 1.0 };
        double y[] = { 0.0, 0.5, 1.0 };
        UnivariateInterpolator i = new LinearInterpolator();
        UnivariateFunction f = i.interpolate(x, y);
        verifyInterpolation(f, x, y);

        
        PolynomialFunction polynomials[] = ((PolynomialSplineFunction) f).getPolynomials();
        double target[] = {y[0], 1d};
        TestUtils.assertEquals(polynomials[0].getCoefficients(), target, coefficientTolerance);
        target = new double[]{y[1], 1d};
        TestUtils.assertEquals(polynomials[1].getCoefficients(), target, coefficientTolerance);

        
        Assert.assertEquals(0.0,f.value(0.0), interpolationTolerance);
        Assert.assertEquals(0.4,f.value(0.4), interpolationTolerance);
        Assert.assertEquals(1.0,f.value(1.0), interpolationTolerance);
    }

// org.apache.commons.math3.analysis.interpolation.LinearInterpolatorTest::testInterpolateLinearDegenerateThreeSegment
    public void testInterpolateLinearDegenerateThreeSegment()
        {
        double x[] = { 0.0, 0.5, 1.0, 1.5 };
        double y[] = { 0.0, 0.5, 1.0, 1.5 };
        UnivariateInterpolator i = new LinearInterpolator();
        UnivariateFunction f = i.interpolate(x, y);
        verifyInterpolation(f, x, y);

        
        PolynomialFunction polynomials[] = ((PolynomialSplineFunction) f).getPolynomials();
        double target[] = {y[0], 1d};
        TestUtils.assertEquals(polynomials[0].getCoefficients(), target, coefficientTolerance);
        target = new double[]{y[1], 1d};
        TestUtils.assertEquals(polynomials[1].getCoefficients(), target, coefficientTolerance);
        target = new double[]{y[2], 1d};
        TestUtils.assertEquals(polynomials[2].getCoefficients(), target, coefficientTolerance);

        
        Assert.assertEquals(0,f.value(0), interpolationTolerance);
        Assert.assertEquals(1.4,f.value(1.4), interpolationTolerance);
        Assert.assertEquals(1.5,f.value(1.5), interpolationTolerance);
    }

// org.apache.commons.math3.analysis.interpolation.LinearInterpolatorTest::testInterpolateLinear
    public void testInterpolateLinear() {
        double x[] = { 0.0, 0.5, 1.0 };
        double y[] = { 0.0, 0.5, 0.0 };
        UnivariateInterpolator i = new LinearInterpolator();
        UnivariateFunction f = i.interpolate(x, y);
        verifyInterpolation(f, x, y);

        
        PolynomialFunction polynomials[] = ((PolynomialSplineFunction) f).getPolynomials();
        double target[] = {y[0], 1d};
        TestUtils.assertEquals(polynomials[0].getCoefficients(), target, coefficientTolerance);
        target = new double[]{y[1], -1d};
        TestUtils.assertEquals(polynomials[1].getCoefficients(), target, coefficientTolerance);
    }

// org.apache.commons.math3.analysis.interpolation.LinearInterpolatorTest::testIllegalArguments
    public void testIllegalArguments() {
        
        UnivariateInterpolator i = new LinearInterpolator();
        try {
            double xval[] = { 0.0, 1.0 };
            double yval[] = { 0.0, 1.0, 2.0 };
            i.interpolate(xval, yval);
            Assert.fail("Failed to detect data set array with different sizes.");
        } catch (DimensionMismatchException iae) {
            
        }
        
        try {
            double xval[] = { 0.0, 1.0, 0.5 };
            double yval[] = { 0.0, 1.0, 2.0 };
            i.interpolate(xval, yval);
            Assert.fail("Failed to detect unsorted arguments.");
        } catch (NonMonotonicSequenceException iae) {
            
        }
        
        try {
            double xval[] = { 0.0 };
            double yval[] = { 0.0 };
            i.interpolate(xval, yval);
            Assert.fail("Failed to detect unsorted arguments.");
        } catch (NumberIsTooSmallException iae) {
            
        }
    }

// org.apache.commons.math3.analysis.interpolation.LoessInterpolatorTest::testOnOnePoint
    public void testOnOnePoint() {
        double[] xval = {0.5};
        double[] yval = {0.7};
        double[] res = new LoessInterpolator().smooth(xval, yval);
        Assert.assertEquals(1, res.length);
        Assert.assertEquals(0.7, res[0], 0.0);
    }

// org.apache.commons.math3.analysis.interpolation.LoessInterpolatorTest::testOnTwoPoints
    public void testOnTwoPoints() {
        double[] xval = {0.5, 0.6};
        double[] yval = {0.7, 0.8};
        double[] res = new LoessInterpolator().smooth(xval, yval);
        Assert.assertEquals(2, res.length);
        Assert.assertEquals(0.7, res[0], 0.0);
        Assert.assertEquals(0.8, res[1], 0.0);
    }

// org.apache.commons.math3.analysis.interpolation.LoessInterpolatorTest::testOnStraightLine
    public void testOnStraightLine() {
        double[] xval = {1,2,3,4,5};
        double[] yval = {2,4,6,8,10};
        LoessInterpolator li = new LoessInterpolator(0.6, 2, 1e-12);
        double[] res = li.smooth(xval, yval);
        Assert.assertEquals(5, res.length);
        for(int i = 0; i < 5; ++i) {
            Assert.assertEquals(yval[i], res[i], 1e-8);
        }
    }

// org.apache.commons.math3.analysis.interpolation.LoessInterpolatorTest::testOnDistortedSine
    public void testOnDistortedSine() {
        int numPoints = 100;
        double[] xval = new double[numPoints];
        double[] yval = new double[numPoints];
        double xnoise = 0.1;
        double ynoise = 0.2;

        generateSineData(xval, yval, xnoise, ynoise);

        LoessInterpolator li = new LoessInterpolator(0.3, 4, 1e-12);

        double[] res = li.smooth(xval, yval);

        
        

        double noisyResidualSum = 0;
        double fitResidualSum = 0;

        for(int i = 0; i < numPoints; ++i) {
            double expected = FastMath.sin(xval[i]);
            double noisy = yval[i];
            double fit = res[i];

            noisyResidualSum += FastMath.pow(noisy - expected, 2);
            fitResidualSum += FastMath.pow(fit - expected, 2);
        }

        Assert.assertTrue(fitResidualSum < noisyResidualSum);
    }

// org.apache.commons.math3.analysis.interpolation.LoessInterpolatorTest::testIncreasingBandwidthIncreasesSmoothness
    public void testIncreasingBandwidthIncreasesSmoothness() {
        int numPoints = 100;
        double[] xval = new double[numPoints];
        double[] yval = new double[numPoints];
        double xnoise = 0.1;
        double ynoise = 0.1;

        generateSineData(xval, yval, xnoise, ynoise);

        

        double[] bandwidths = {0.1, 0.5, 1.0};
        double[] variances = new double[bandwidths.length];
        for (int i = 0; i < bandwidths.length; i++) {
            double bw = bandwidths[i];

            LoessInterpolator li = new LoessInterpolator(bw, 4, 1e-12);

            double[] res = li.smooth(xval, yval);

            for (int j = 1; j < res.length; ++j) {
                variances[i] += FastMath.pow(res[j] - res[j-1], 2);
            }
        }

        for(int i = 1; i < variances.length; ++i) {
            Assert.assertTrue(variances[i] < variances[i-1]);
        }
    }

// org.apache.commons.math3.analysis.interpolation.LoessInterpolatorTest::testIncreasingRobustnessItersIncreasesSmoothnessWithOutliers
    public void testIncreasingRobustnessItersIncreasesSmoothnessWithOutliers() {
        int numPoints = 100;
        double[] xval = new double[numPoints];
        double[] yval = new double[numPoints];
        double xnoise = 0.1;
        double ynoise = 0.1;

        generateSineData(xval, yval, xnoise, ynoise);

        
        yval[numPoints/3] *= 100;
        yval[2 * numPoints/3] *= -100;

        
        

        double[] variances = new double[4];
        for (int i = 0; i < 4; i++) {
            LoessInterpolator li = new LoessInterpolator(0.3, i, 1e-12);

            double[] res = li.smooth(xval, yval);

            for (int j = 1; j < res.length; ++j) {
                variances[i] += FastMath.abs(res[j] - res[j-1]);
            }
        }

        for(int i = 1; i < variances.length; ++i) {
            Assert.assertTrue(variances[i] < variances[i-1]);
        }
    }

// org.apache.commons.math3.analysis.interpolation.LoessInterpolatorTest::testUnequalSizeArguments
    public void testUnequalSizeArguments() {
        new LoessInterpolator().smooth(new double[] {1,2,3}, new double[] {1,2,3,4});
    }

// org.apache.commons.math3.analysis.interpolation.LoessInterpolatorTest::testEmptyData
    public void testEmptyData() {
        new LoessInterpolator().smooth(new double[] {}, new double[] {});
    }

// org.apache.commons.math3.analysis.interpolation.LoessInterpolatorTest::testNonStrictlyIncreasing1
    public void testNonStrictlyIncreasing1() {
        new LoessInterpolator().smooth(new double[] {4,3,1,2}, new double[] {3,4,5,6});
    }

// org.apache.commons.math3.analysis.interpolation.LoessInterpolatorTest::testNonStrictlyIncreasing2
    public void testNonStrictlyIncreasing2() {
        new LoessInterpolator().smooth(new double[] {1,2,2,3}, new double[] {3,4,5,6});
    }

// org.apache.commons.math3.analysis.interpolation.LoessInterpolatorTest::testNotAllFiniteReal1
    public void testNotAllFiniteReal1() {
        new LoessInterpolator().smooth(new double[] {1,2,Double.NaN}, new double[] {3,4,5});
    }

// org.apache.commons.math3.analysis.interpolation.LoessInterpolatorTest::testNotAllFiniteReal2
    public void testNotAllFiniteReal2() {
        new LoessInterpolator().smooth(new double[] {1,2,Double.POSITIVE_INFINITY}, new double[] {3,4,5});
    }

// org.apache.commons.math3.analysis.interpolation.LoessInterpolatorTest::testNotAllFiniteReal3
    public void testNotAllFiniteReal3() {
        new LoessInterpolator().smooth(new double[] {1,2,Double.NEGATIVE_INFINITY}, new double[] {3,4,5});
    }

// org.apache.commons.math3.analysis.interpolation.LoessInterpolatorTest::testNotAllFiniteReal4
    public void testNotAllFiniteReal4() {
        new LoessInterpolator().smooth(new double[] {3,4,5}, new double[] {1,2,Double.NaN});
    }

// org.apache.commons.math3.analysis.interpolation.LoessInterpolatorTest::testNotAllFiniteReal5
    public void testNotAllFiniteReal5() {
        new LoessInterpolator().smooth(new double[] {3,4,5}, new double[] {1,2,Double.POSITIVE_INFINITY});
    }

// org.apache.commons.math3.analysis.interpolation.LoessInterpolatorTest::testNotAllFiniteReal6
    public void testNotAllFiniteReal6() {
        new LoessInterpolator().smooth(new double[] {3,4,5}, new double[] {1,2,Double.NEGATIVE_INFINITY});
    }

// org.apache.commons.math3.analysis.interpolation.LoessInterpolatorTest::testInsufficientBandwidth
    public void testInsufficientBandwidth() {
        LoessInterpolator li = new LoessInterpolator(0.1, 3, 1e-12);
        li.smooth(new double[] {1,2,3,4,5,6,7,8,9,10,11,12}, new double[] {1,2,3,4,5,6,7,8,9,10,11,12});
    }

// org.apache.commons.math3.analysis.interpolation.LoessInterpolatorTest::testCompletelyIncorrectBandwidth1
    public void testCompletelyIncorrectBandwidth1() {
        new LoessInterpolator(-0.2, 3, 1e-12);
    }

// org.apache.commons.math3.analysis.interpolation.LoessInterpolatorTest::testCompletelyIncorrectBandwidth2
    public void testCompletelyIncorrectBandwidth2() {
        new LoessInterpolator(1.1, 3, 1e-12);
    }

// org.apache.commons.math3.analysis.interpolation.LoessInterpolatorTest::testMath296withoutWeights
    public void testMath296withoutWeights() {
        double[] xval = {
                0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9, 1.0,
                 1.1, 1.2, 1.3, 1.4, 1.5, 1.6, 1.7, 1.8, 1.9, 2.0};
        double[] yval = {
                0.47, 0.48, 0.55, 0.56, -0.08, -0.04, -0.07, -0.07,
                -0.56, -0.46, -0.56, -0.52, -3.03, -3.08, -3.09,
                -3.04, 3.54, 3.46, 3.36, 3.35};
        
        double[] yref = {
                0.461, 0.499, 0.541, 0.308, 0.175, -0.042, -0.072,
                -0.196, -0.311, -0.446, -0.557, -1.497, -2.133,
                -3.08, -3.09, -0.621, 0.982, 3.449, 3.389, 3.336
        };
        LoessInterpolator li = new LoessInterpolator(0.3, 4, 1e-12);
        double[] res = li.smooth(xval, yval);
        Assert.assertEquals(xval.length, res.length);
        for(int i = 0; i < res.length; ++i) {
            Assert.assertEquals(yref[i], res[i], 0.02);
        }
    }

// org.apache.commons.math3.analysis.interpolation.NevilleInterpolatorTest::testSinFunction
    public void testSinFunction() {
        UnivariateFunction f = new Sin();
        UnivariateInterpolator interpolator = new NevilleInterpolator();
        double x[], y[], z, expected, result, tolerance;

        
        int n = 6;
        double min = 0.0, max = 2 * FastMath.PI;
        x = new double[n];
        y = new double[n];
        for (int i = 0; i < n; i++) {
            x[i] = min + i * (max - min) / n;
            y[i] = f.value(x[i]);
        }
        double derivativebound = 1.0;
        UnivariateFunction p = interpolator.interpolate(x, y);

        z = FastMath.PI / 4; expected = f.value(z); result = p.value(z);
        tolerance = FastMath.abs(derivativebound * partialerror(x, z));
        Assert.assertEquals(expected, result, tolerance);

        z = FastMath.PI * 1.5; expected = f.value(z); result = p.value(z);
        tolerance = FastMath.abs(derivativebound * partialerror(x, z));
        Assert.assertEquals(expected, result, tolerance);
    }

// org.apache.commons.math3.analysis.interpolation.NevilleInterpolatorTest::testExpm1Function
    public void testExpm1Function() {
        UnivariateFunction f = new Expm1();
        UnivariateInterpolator interpolator = new NevilleInterpolator();
        double x[], y[], z, expected, result, tolerance;

        
        int n = 5;
        double min = -1.0, max = 1.0;
        x = new double[n];
        y = new double[n];
        for (int i = 0; i < n; i++) {
            x[i] = min + i * (max - min) / n;
            y[i] = f.value(x[i]);
        }
        double derivativebound = FastMath.E;
        UnivariateFunction p = interpolator.interpolate(x, y);

        z = 0.0; expected = f.value(z); result = p.value(z);
        tolerance = FastMath.abs(derivativebound * partialerror(x, z));
        Assert.assertEquals(expected, result, tolerance);

        z = 0.5; expected = f.value(z); result = p.value(z);
        tolerance = FastMath.abs(derivativebound * partialerror(x, z));
        Assert.assertEquals(expected, result, tolerance);

        z = -0.5; expected = f.value(z); result = p.value(z);
        tolerance = FastMath.abs(derivativebound * partialerror(x, z));
        Assert.assertEquals(expected, result, tolerance);
    }

// org.apache.commons.math3.analysis.interpolation.NevilleInterpolatorTest::testParameters
    public void testParameters() {
        UnivariateInterpolator interpolator = new NevilleInterpolator();

        try {
            
            double x[] = { 1.0, 2.0, 2.0, 4.0 };
            double y[] = { 0.0, 4.0, 4.0, 2.5 };
            UnivariateFunction p = interpolator.interpolate(x, y);
            p.value(0.0);
            Assert.fail("Expecting NonMonotonicSequenceException - bad abscissas array");
        } catch (NonMonotonicSequenceException ex) {
            
        }
    }

// org.apache.commons.math3.analysis.interpolation.SmoothingPolynomialBicubicSplineInterpolatorTest::testPreconditions
    public void testPreconditions() {
        double[] xval = new double[] {3, 4, 5, 6.5};
        double[] yval = new double[] {-4, -3, -1, 2.5};
        double[][] zval = new double[xval.length][yval.length];

        BivariateGridInterpolator interpolator = new SmoothingPolynomialBicubicSplineInterpolator(0);
        
        @SuppressWarnings("unused")
        BivariateFunction p = interpolator.interpolate(xval, yval, zval);
        
        double[] wxval = new double[] {3, 2, 5, 6.5};
        try {
            p = interpolator.interpolate(wxval, yval, zval);
            Assert.fail("an exception should have been thrown");
        } catch (MathIllegalArgumentException e) {
            
        }

        double[] wyval = new double[] {-4, -3, -1, -1};
        try {
            p = interpolator.interpolate(xval, wyval, zval);
            Assert.fail("an exception should have been thrown");
        } catch (MathIllegalArgumentException e) {
            
        }

        double[][] wzval = new double[xval.length][yval.length + 1];
        try {
            p = interpolator.interpolate(xval, yval, wzval);
            Assert.fail("an exception should have been thrown");
        } catch (DimensionMismatchException e) {
            
        }
        wzval = new double[xval.length - 1][yval.length];
        try {
            p = interpolator.interpolate(xval, yval, wzval);
            Assert.fail("an exception should have been thrown");
        } catch (DimensionMismatchException e) {
            
        }
        wzval = new double[xval.length][yval.length - 1];
        try {
            p = interpolator.interpolate(xval, yval, wzval);
            Assert.fail("an exception should have been thrown");
        } catch (DimensionMismatchException e) {
            
        }
    }

// org.apache.commons.math3.analysis.interpolation.SmoothingPolynomialBicubicSplineInterpolatorTest::testPlane
    public void testPlane() {
        BivariateFunction f = new BivariateFunction() {
                public double value(double x, double y) {
                    return 2 * x - 3 * y + 5
                        + ((int) (FastMath.abs(5 * x + 3 * y)) % 2 == 0 ? 1 : -1);
                }
            };

        BivariateGridInterpolator interpolator = new SmoothingPolynomialBicubicSplineInterpolator(1);

        double[] xval = new double[] {3, 4, 5, 6.5};
        double[] yval = new double[] {-4, -3, -1, 2, 2.5};
        double[][] zval = new double[xval.length][yval.length];
        for (int i = 0; i < xval.length; i++) {
            for (int j = 0; j < yval.length; j++) {
                zval[i][j] = f.value(xval[i], yval[j]);
            }
        }

        BivariateFunction p = interpolator.interpolate(xval, yval, zval);
        double x, y;
        double expected, result;
        
        x = 4;
        y = -3;
        expected = f.value(x, y);
        result = p.value(x, y);
        Assert.assertEquals("On sample point", expected, result, 2);

        x = 4.5;
        y = -1.5;
        expected = f.value(x, y);
        result = p.value(x, y);
        Assert.assertEquals("half-way between sample points (middle of the patch)", expected, result, 2);

        x = 3.5;
        y = -3.5;
        expected = f.value(x, y);
        result = p.value(x, y);
        Assert.assertEquals("half-way between sample points (border of the patch)", expected, result, 2);
    }

// org.apache.commons.math3.analysis.interpolation.SmoothingPolynomialBicubicSplineInterpolatorTest::testParaboloid
    public void testParaboloid() {
        BivariateFunction f = new BivariateFunction() {
                public double value(double x, double y) {
                    return 2 * x * x - 3 * y * y + 4 * x * y - 5
                        + ((int) (FastMath.abs(5 * x + 3 * y)) % 2 == 0 ? 1 : -1);
                }
            };

        BivariateGridInterpolator interpolator = new SmoothingPolynomialBicubicSplineInterpolator(4);

        double[] xval = new double[] {3, 4, 5, 6.5};
        double[] yval = new double[] {-4, -3, -2, -1, 0.5, 2.5};
        double[][] zval = new double[xval.length][yval.length];
        for (int i = 0; i < xval.length; i++) {
            for (int j = 0; j < yval.length; j++) {
                zval[i][j] = f.value(xval[i], yval[j]);
            }
        }

        BivariateFunction p = interpolator.interpolate(xval, yval, zval);
        double x, y;
        double expected, result;

        x = 5;
        y = 0.5;
        expected = f.value(x, y);
        result = p.value(x, y);
        Assert.assertEquals("On sample point", expected, result, 2);

        x = 4.5;
        y = -1.5;
        expected = f.value(x, y);
        result = p.value(x, y);
        Assert.assertEquals("half-way between sample points (middle of the patch)", expected, result, 2);

        x = 3.5;
        y = -3.5;
        expected = f.value(x, y);
        result = p.value(x, y);
        Assert.assertEquals("half-way between sample points (border of the patch)", expected, result, 2);
    }

// org.apache.commons.math3.analysis.interpolation.SplineInterpolatorTest::testInterpolateLinearDegenerateTwoSegment
    public void testInterpolateLinearDegenerateTwoSegment()
        {
        double x[] = { 0.0, 0.5, 1.0 };
        double y[] = { 0.0, 0.5, 1.0 };
        UnivariateInterpolator i = new SplineInterpolator();
        UnivariateFunction f = i.interpolate(x, y);
        verifyInterpolation(f, x, y);
        verifyConsistency((PolynomialSplineFunction) f, x);

        
        PolynomialFunction polynomials[] = ((PolynomialSplineFunction) f).getPolynomials();
        double target[] = {y[0], 1d};
        TestUtils.assertEquals(polynomials[0].getCoefficients(), target, coefficientTolerance);
        target = new double[]{y[1], 1d};
        TestUtils.assertEquals(polynomials[1].getCoefficients(), target, coefficientTolerance);

        
        Assert.assertEquals(0.0,f.value(0.0), interpolationTolerance);
        Assert.assertEquals(0.4,f.value(0.4), interpolationTolerance);
        Assert.assertEquals(1.0,f.value(1.0), interpolationTolerance);
    }

// org.apache.commons.math3.analysis.interpolation.SplineInterpolatorTest::testInterpolateLinearDegenerateThreeSegment
    public void testInterpolateLinearDegenerateThreeSegment()
        {
        double x[] = { 0.0, 0.5, 1.0, 1.5 };
        double y[] = { 0.0, 0.5, 1.0, 1.5 };
        UnivariateInterpolator i = new SplineInterpolator();
        UnivariateFunction f = i.interpolate(x, y);
        verifyInterpolation(f, x, y);

        
        PolynomialFunction polynomials[] = ((PolynomialSplineFunction) f).getPolynomials();
        double target[] = {y[0], 1d};
        TestUtils.assertEquals(polynomials[0].getCoefficients(), target, coefficientTolerance);
        target = new double[]{y[1], 1d};
        TestUtils.assertEquals(polynomials[1].getCoefficients(), target, coefficientTolerance);
        target = new double[]{y[2], 1d};
        TestUtils.assertEquals(polynomials[2].getCoefficients(), target, coefficientTolerance);

        
        Assert.assertEquals(0,f.value(0), interpolationTolerance);
        Assert.assertEquals(1.4,f.value(1.4), interpolationTolerance);
        Assert.assertEquals(1.5,f.value(1.5), interpolationTolerance);
    }

// org.apache.commons.math3.analysis.interpolation.SplineInterpolatorTest::testInterpolateLinear
    public void testInterpolateLinear() {
        double x[] = { 0.0, 0.5, 1.0 };
        double y[] = { 0.0, 0.5, 0.0 };
        UnivariateInterpolator i = new SplineInterpolator();
        UnivariateFunction f = i.interpolate(x, y);
        verifyInterpolation(f, x, y);
        verifyConsistency((PolynomialSplineFunction) f, x);

        
        PolynomialFunction polynomials[] = ((PolynomialSplineFunction) f).getPolynomials();
        double target[] = {y[0], 1.5d, 0d, -2d};
        TestUtils.assertEquals(polynomials[0].getCoefficients(), target, coefficientTolerance);
        target = new double[]{y[1], 0d, -3d, 2d};
        TestUtils.assertEquals(polynomials[1].getCoefficients(), target, coefficientTolerance);
    }

// org.apache.commons.math3.analysis.interpolation.SplineInterpolatorTest::testInterpolateSin
    public void testInterpolateSin() {
        double x[] =
            {
                0.0,
                FastMath.PI / 6d,
                FastMath.PI / 2d,
                5d * FastMath.PI / 6d,
                FastMath.PI,
                7d * FastMath.PI / 6d,
                3d * FastMath.PI / 2d,
                11d * FastMath.PI / 6d,
                2.d * FastMath.PI };
        double y[] = { 0d, 0.5d, 1d, 0.5d, 0d, -0.5d, -1d, -0.5d, 0d };
        UnivariateInterpolator i = new SplineInterpolator();
        UnivariateFunction f = i.interpolate(x, y);
        verifyInterpolation(f, x, y);
        verifyConsistency((PolynomialSplineFunction) f, x);

        
        PolynomialFunction polynomials[] = ((PolynomialSplineFunction) f).getPolynomials();
        double target[] = {y[0], 1.002676d, 0d, -0.17415829d};
        TestUtils.assertEquals(polynomials[0].getCoefficients(), target, coefficientTolerance);
        target = new double[]{y[1], 8.594367e-01, -2.735672e-01, -0.08707914};
        TestUtils.assertEquals(polynomials[1].getCoefficients(), target, coefficientTolerance);
        target = new double[]{y[2], 1.471804e-17,-5.471344e-01, 0.08707914};
        TestUtils.assertEquals(polynomials[2].getCoefficients(), target, coefficientTolerance);
        target = new double[]{y[3], -8.594367e-01, -2.735672e-01, 0.17415829};
        TestUtils.assertEquals(polynomials[3].getCoefficients(), target, coefficientTolerance);
        target = new double[]{y[4], -1.002676, 6.548562e-17, 0.17415829};
        TestUtils.assertEquals(polynomials[4].getCoefficients(), target, coefficientTolerance);
        target = new double[]{y[5], -8.594367e-01, 2.735672e-01, 0.08707914};
        TestUtils.assertEquals(polynomials[5].getCoefficients(), target, coefficientTolerance);
        target = new double[]{y[6], 3.466465e-16, 5.471344e-01, -0.08707914};
        TestUtils.assertEquals(polynomials[6].getCoefficients(), target, coefficientTolerance);
        target = new double[]{y[7], 8.594367e-01, 2.735672e-01, -0.17415829};
        TestUtils.assertEquals(polynomials[7].getCoefficients(), target, coefficientTolerance);

        
        Assert.assertEquals(FastMath.sqrt(2d) / 2d,f.value(FastMath.PI/4d),interpolationTolerance);
        Assert.assertEquals(FastMath.sqrt(2d) / 2d,f.value(3d*FastMath.PI/4d),interpolationTolerance);
    }

// org.apache.commons.math3.analysis.interpolation.SplineInterpolatorTest::testIllegalArguments
    public void testIllegalArguments() {
        
        UnivariateInterpolator i = new SplineInterpolator();
        try {
            double xval[] = { 0.0, 1.0 };
            double yval[] = { 0.0, 1.0, 2.0 };
            i.interpolate(xval, yval);
            Assert.fail("Failed to detect data set array with different sizes.");
        } catch (DimensionMismatchException iae) {
            
        }
        
        try {
            double xval[] = { 0.0, 1.0, 0.5 };
            double yval[] = { 0.0, 1.0, 2.0 };
            i.interpolate(xval, yval);
            Assert.fail("Failed to detect unsorted arguments.");
        } catch (NonMonotonicSequenceException iae) {
            
        }
        
        try {
            double xval[] = { 0.0, 1.0 };
            double yval[] = { 0.0, 1.0 };
            i.interpolate(xval, yval);
            Assert.fail("Failed to detect unsorted arguments.");
        } catch (NumberIsTooSmallException iae) {
            
        }
    }

// org.apache.commons.math3.analysis.interpolation.TricubicSplineInterpolatingFunctionTest::testPreconditions
    public void testPreconditions() {
        double[] xval = new double[] {3, 4, 5, 6.5};
        double[] yval = new double[] {-4, -3, -1, 2.5};
        double[] zval = new double[] {-12, -8, -5.5, -3, 0, 2.5};
        double[][][] fval = new double[xval.length][yval.length][zval.length];

        @SuppressWarnings("unused")
        TrivariateFunction tcf = new TricubicSplineInterpolatingFunction(xval, yval, zval,
                                                                             fval, fval, fval, fval,
                                                                             fval, fval, fval, fval);
        
        double[] wxval = new double[] {3, 2, 5, 6.5};
        try {
            tcf = new TricubicSplineInterpolatingFunction(wxval, yval, zval,
                                                          fval, fval, fval, fval,
                                                          fval, fval, fval, fval);
            Assert.fail("an exception should have been thrown");
        } catch (MathIllegalArgumentException e) {
            
        }
        double[] wyval = new double[] {-4, -1, -1, 2.5};
        try {
            tcf = new TricubicSplineInterpolatingFunction(xval, wyval, zval,
                                                          fval, fval, fval, fval,
                                                          fval, fval, fval, fval);
            Assert.fail("an exception should have been thrown");
        } catch (MathIllegalArgumentException e) {
            
        }
        double[] wzval = new double[] {-12, -8, -9, -3, 0, 2.5};
        try {
            tcf = new TricubicSplineInterpolatingFunction(xval, yval, wzval,
                                                          fval, fval, fval, fval,
                                                          fval, fval, fval, fval);
            Assert.fail("an exception should have been thrown");
        } catch (MathIllegalArgumentException e) {
            
        }
        double[][][] wfval = new double[xval.length - 1][yval.length - 1][zval.length];
        try {
            tcf = new TricubicSplineInterpolatingFunction(xval, yval, zval,
                                                          wfval, fval, fval, fval,
                                                          fval, fval, fval, fval);
            Assert.fail("an exception should have been thrown");
        } catch (DimensionMismatchException e) {
            
        }
        try {
            tcf = new TricubicSplineInterpolatingFunction(xval, yval, zval,
                                                          fval, wfval, fval, fval,
                                                          fval, fval, fval, fval);
            Assert.fail("an exception should have been thrown");
        } catch (DimensionMismatchException e) {
            
        }
        try {
            tcf = new TricubicSplineInterpolatingFunction(xval, yval, zval,
                                                          fval, fval, wfval, fval,
                                                          fval, fval, fval, fval);
            Assert.fail("an exception should have been thrown");
        } catch (DimensionMismatchException e) {
            
        }
        try {
            tcf = new TricubicSplineInterpolatingFunction(xval, yval, zval,
                                                          fval, fval, fval, wfval,
                                                          fval, fval, fval, fval);
            Assert.fail("an exception should have been thrown");
        } catch (DimensionMismatchException e) {
            
        }
        try {
            tcf = new TricubicSplineInterpolatingFunction(xval, yval, zval,
                                                          fval, fval, fval, fval,
                                                          wfval, fval, fval, fval);
            Assert.fail("an exception should have been thrown");
        } catch (DimensionMismatchException e) {
            
        }
        try {
            tcf = new TricubicSplineInterpolatingFunction(xval, yval, zval,
                                                          fval, fval, fval, fval,
                                                          fval, wfval, fval, fval);
            Assert.fail("an exception should have been thrown");
        } catch (DimensionMismatchException e) {
            
        }
        try {
            tcf = new TricubicSplineInterpolatingFunction(xval, yval, zval,
                                                          fval, fval, fval, fval,
                                                          fval, fval, wfval, fval);
            Assert.fail("an exception should have been thrown");
        } catch (DimensionMismatchException e) {
            
        }
        try {
            tcf = new TricubicSplineInterpolatingFunction(xval, yval, zval,
                                                          fval, fval, fval, fval,
                                                          fval, fval, fval, wfval);
            Assert.fail("an exception should have been thrown");
        } catch (DimensionMismatchException e) {
            
        }
        wfval = new double[xval.length][yval.length - 1][zval.length];
        try {
            tcf = new TricubicSplineInterpolatingFunction(xval, yval, zval,
                                                          wfval, fval, fval, fval,
                                                          fval, fval, fval, fval);
            Assert.fail("an exception should have been thrown");
        } catch (DimensionMismatchException e) {
            
        }
        try {
            tcf = new TricubicSplineInterpolatingFunction(xval, yval, zval,
                                                          fval, wfval, fval, fval,
                                                          fval, fval, fval, fval);
            Assert.fail("an exception should have been thrown");
        } catch (DimensionMismatchException e) {
            
        }
        try {
            tcf = new TricubicSplineInterpolatingFunction(xval, yval, zval,
                                                          fval, fval, wfval, fval,
                                                          fval, fval, fval, fval);
            Assert.fail("an exception should have been thrown");
        } catch (DimensionMismatchException e) {
            
        }
        try {
            tcf = new TricubicSplineInterpolatingFunction(xval, yval, zval,
                                                          fval, fval, fval, wfval,
                                                          fval, fval, fval, fval);
            Assert.fail("an exception should have been thrown");
        } catch (DimensionMismatchException e) {
            
        }
        try {
            tcf = new TricubicSplineInterpolatingFunction(xval, yval, zval,
                                                          fval, fval, fval, fval,
                                                          wfval, fval, fval, fval);
            Assert.fail("an exception should have been thrown");
        } catch (DimensionMismatchException e) {
            
        }
        try {
            tcf = new TricubicSplineInterpolatingFunction(xval, yval, zval,
                                                          fval, fval, fval, fval,
                                                          fval, wfval, fval, fval);
            Assert.fail("an exception should have been thrown");
        } catch (DimensionMismatchException e) {
            
        }
        try {
            tcf = new TricubicSplineInterpolatingFunction(xval, yval, zval,
                                                          fval, fval, fval, fval,
                                                          fval, fval, wfval, fval);
            Assert.fail("an exception should have been thrown");
        } catch (DimensionMismatchException e) {
            
        }
        try {
            tcf = new TricubicSplineInterpolatingFunction(xval, yval, zval,
                                                          fval, fval, fval, fval,
                                                          fval, fval, fval, wfval);
            Assert.fail("an exception should have been thrown");
        } catch (DimensionMismatchException e) {
            
        }
        wfval = new double[xval.length][yval.length][zval.length - 1];
        try {
            tcf = new TricubicSplineInterpolatingFunction(xval, yval, zval,
                                                          wfval, fval, fval, fval,
                                                          fval, fval, fval, fval);
            Assert.fail("an exception should have been thrown");
        } catch (DimensionMismatchException e) {
            
        }
        try {
            tcf = new TricubicSplineInterpolatingFunction(xval, yval, zval,
                                                          fval, wfval, fval, fval,
                                                          fval, fval, fval, fval);
            Assert.fail("an exception should have been thrown");
        } catch (DimensionMismatchException e) {
            
        }
        try {
            tcf = new TricubicSplineInterpolatingFunction(xval, yval, zval,
                                                          fval, fval, wfval, fval,
                                                          fval, fval, fval, fval);
            Assert.fail("an exception should have been thrown");
        } catch (DimensionMismatchException e) {
            
        }
        try {
            tcf = new TricubicSplineInterpolatingFunction(xval, yval, zval,
                                                          fval, fval, fval, wfval,
                                                          fval, fval, fval, fval);
            Assert.fail("an exception should have been thrown");
        } catch (DimensionMismatchException e) {
            
        }
        try {
            tcf = new TricubicSplineInterpolatingFunction(xval, yval, zval,
                                                          fval, fval, fval, fval,
                                                          wfval, fval, fval, fval);
            Assert.fail("an exception should have been thrown");
        } catch (DimensionMismatchException e) {
            
        }
        try {
            tcf = new TricubicSplineInterpolatingFunction(xval, yval, zval,
                                                          fval, fval, fval, fval,
                                                          fval, wfval, fval, fval);
            Assert.fail("an exception should have been thrown");
        } catch (DimensionMismatchException e) {
            
        }
        try {
            tcf = new TricubicSplineInterpolatingFunction(xval, yval, zval,
                                                          fval, fval, fval, fval,
                                                          fval, fval, wfval, fval);
            Assert.fail("an exception should have been thrown");
        } catch (DimensionMismatchException e) {
            
        }
        try {
            tcf = new TricubicSplineInterpolatingFunction(xval, yval, zval,
                                                          fval, fval, fval, fval,
                                                          fval, fval, fval, wfval);
            Assert.fail("an exception should have been thrown");
        } catch (DimensionMismatchException e) {
            
        }
    }

// org.apache.commons.math3.analysis.interpolation.TricubicSplineInterpolatingFunctionTest::testPlane
    public void testPlane() {
        double[] xval = new double[] {3, 4, 5, 6.5};
        double[] yval = new double[] {-4, -3, -1, 2, 2.5};
        double[] zval = new double[] {-12, -8, -5.5, -3, 0, 2.5};

        
        TrivariateFunction f = new TrivariateFunction() {
                public double value(double x, double y, double z) {
                    return 2 * x - 3 * y - 4 * z + 5;
                }
            };

        double[][][] fval = new double[xval.length][yval.length][zval.length];

        for (int i = 0; i < xval.length; i++) {
            for (int j = 0; j < yval.length; j++) {
                for (int k = 0; k < zval.length; k++) {
                    fval[i][j][k] = f.value(xval[i], yval[j], zval[k]);
                }
            }
        }
        
        double[][][] dFdX = new double[xval.length][yval.length][zval.length];
        for (int i = 0; i < xval.length; i++) {
            for (int j = 0; j < yval.length; j++) {
                for (int k = 0; k < zval.length; k++) {
                    dFdX[i][j][k] = 2;
                }
            }
        }
        
        double[][][] dFdY = new double[xval.length][yval.length][zval.length];
        for (int i = 0; i < xval.length; i++) {
            for (int j = 0; j < yval.length; j++) {
                for (int k = 0; k < zval.length; k++) {
                    dFdY[i][j][k] = -3;
                }
            }
        }

        
        double[][][] dFdZ = new double[xval.length][yval.length][zval.length];
        for (int i = 0; i < xval.length; i++) {
            for (int j = 0; j < yval.length; j++) {
                for (int k = 0; k < zval.length; k++) {
                    dFdZ[i][j][k] = -4;
                }
            }
        }
        
        double[][][] d2FdXdY = new double[xval.length][yval.length][zval.length];
        double[][][] d2FdXdZ = new double[xval.length][yval.length][zval.length];
        double[][][] d2FdYdZ = new double[xval.length][yval.length][zval.length];
        double[][][] d3FdXdYdZ = new double[xval.length][yval.length][zval.length];
        for (int i = 0; i < xval.length; i++) {
            for (int j = 0; j < yval.length; j++) {
                for (int k = 0; k < zval.length; k++) {
                    d2FdXdY[i][j][k] = 0;
                    d2FdXdZ[i][j][k] = 0;
                    d2FdYdZ[i][j][k] = 0;
                    d3FdXdYdZ[i][j][k] = 0;
                }
            }
        }

        TrivariateFunction tcf = new TricubicSplineInterpolatingFunction(xval, yval, zval,
                                                                             fval, dFdX, dFdY, dFdZ,
                                                                             d2FdXdY, d2FdXdZ, d2FdYdZ,
                                                                             d3FdXdYdZ);
        double x, y, z;
        double expected, result;

        x = 4;
        y = -3;
        z = 0;
        expected = f.value(x, y, z);
        result = tcf.value(x, y, z);
        Assert.assertEquals("On sample point",
                            expected, result, 1e-15);

        x = 4.5;
        y = -1.5;
        z = -4.25;
        expected = f.value(x, y, z);
        result = tcf.value(x, y, z);
        Assert.assertEquals("Half-way between sample points (middle of the patch)",
                            expected, result, 0.3);

        x = 3.5;
        y = -3.5;
        z = -10;
        expected = f.value(x, y, z);
        result = tcf.value(x, y, z);
        Assert.assertEquals("Half-way between sample points (border of the patch)",
                            expected, result, 0.3);
    }

// org.apache.commons.math3.analysis.interpolation.TricubicSplineInterpolatingFunctionTest::testWave
    public void testWave() {
        double[] xval = new double[] {3, 4, 5, 6.5};
        double[] yval = new double[] {-4, -3, -1, 2, 2.5};
        double[] zval = new double[] {-12, -8, -5.5, -3, 0, 4};
        
        final double a = 0.2;
        final double omega = 0.5;
        final double kx = 2;
        final double ky = 1;
        
        
        TrivariateFunction f = new TrivariateFunction() {
                public double value(double x, double y, double z) {
                    return a * FastMath.cos(omega * z - kx * x - ky * y);
                }
            };
        
        double[][][] fval = new double[xval.length][yval.length][zval.length];
        for (int i = 0; i < xval.length; i++) {
            for (int j = 0; j < yval.length; j++) {
                for (int k = 0; k < zval.length; k++) {
                    fval[i][j][k] = f.value(xval[i], yval[j], zval[k]);
                }
            }
        }
        
        
        double[][][] dFdX = new double[xval.length][yval.length][zval.length];
        TrivariateFunction dFdX_f = new TrivariateFunction() {
                public double value(double x, double y, double z) {
                    return a * FastMath.sin(omega * z - kx * x - ky * y) * kx;
                }
            };
        for (int i = 0; i < xval.length; i++) {
            for (int j = 0; j < yval.length; j++) {
                for (int k = 0; k < zval.length; k++) {
                    dFdX[i][j][k] = dFdX_f.value(xval[i], yval[j], zval[k]);
                }
            }
        }
            
        
        double[][][] dFdY = new double[xval.length][yval.length][zval.length];
        TrivariateFunction dFdY_f = new TrivariateFunction() {
                public double value(double x, double y, double z) {
                    return a * FastMath.sin(omega * z - kx * x - ky * y) * ky;
                }
            };
        for (int i = 0; i < xval.length; i++) {
            for (int j = 0; j < yval.length; j++) {
                for (int k = 0; k < zval.length; k++) {
                    dFdY[i][j][k] = dFdY_f.value(xval[i], yval[j], zval[k]);
                }
            }
        }

        
        double[][][] dFdZ = new double[xval.length][yval.length][zval.length];
        TrivariateFunction dFdZ_f = new TrivariateFunction() {
                public double value(double x, double y, double z) {
                    return -a * FastMath.sin(omega * z - kx * x - ky * y) * omega;
                }
            };
        for (int i = 0; i < xval.length; i++) {
            for (int j = 0; j < yval.length; j++) {
                for (int k = 0; k < zval.length; k++) {
                    dFdZ[i][j][k] = dFdZ_f.value(xval[i], yval[j], zval[k]);
                }
            }
        }

        
        double[][][] d2FdXdY = new double[xval.length][yval.length][zval.length];
        TrivariateFunction d2FdXdY_f = new TrivariateFunction() {
                public double value(double x, double y, double z) {
                    return -a * FastMath.cos(omega * z - kx * x - ky * y) * kx * ky;
                }
            };
        for (int i = 0; i < xval.length; i++) {
            for (int j = 0; j < yval.length; j++) {
                for (int k = 0; k < zval.length; k++) {
                    d2FdXdY[i][j][k] = d2FdXdY_f.value(xval[i], yval[j], zval[k]);
                }
            }
        }

        
        double[][][] d2FdXdZ = new double[xval.length][yval.length][zval.length];
        TrivariateFunction d2FdXdZ_f = new TrivariateFunction() {
                public double value(double x, double y, double z) {
                    return a * FastMath.cos(omega * z - kx * x - ky * y) * kx * omega;
                }
            };
        for (int i = 0; i < xval.length; i++) {
            for (int j = 0; j < yval.length; j++) {
                for (int k = 0; k < zval.length; k++) {
                    d2FdXdZ[i][j][k] = d2FdXdZ_f.value(xval[i], yval[j], zval[k]);
                }
            }
        }

        
        double[][][] d2FdYdZ = new double[xval.length][yval.length][zval.length];
        TrivariateFunction d2FdYdZ_f = new TrivariateFunction() {
                public double value(double x, double y, double z) {
                    return a * FastMath.cos(omega * z - kx * x - ky * y) * ky * omega;
                }
            };
        for (int i = 0; i < xval.length; i++) {
            for (int j = 0; j < yval.length; j++) {
                for (int k = 0; k < zval.length; k++) {
                    d2FdYdZ[i][j][k] = d2FdYdZ_f.value(xval[i], yval[j], zval[k]);
                }
            }
        }

        
        double[][][] d3FdXdYdZ = new double[xval.length][yval.length][zval.length];
        TrivariateFunction d3FdXdYdZ_f = new TrivariateFunction() {
                public double value(double x, double y, double z) {
                    return a * FastMath.sin(omega * z - kx * x - ky * y) * kx * ky * omega;
                }
            };
        for (int i = 0; i < xval.length; i++) {
            for (int j = 0; j < yval.length; j++) {
                for (int k = 0; k < zval.length; k++) {
                    d3FdXdYdZ[i][j][k] = d3FdXdYdZ_f.value(xval[i], yval[j], zval[k]);
                }
            }
        }

        TrivariateFunction tcf = new TricubicSplineInterpolatingFunction(xval, yval, zval,
                                                                             fval, dFdX, dFdY, dFdZ,
                                                                             d2FdXdY, d2FdXdZ, d2FdYdZ,
                                                                             d3FdXdYdZ);
        double x, y, z;
        double expected, result;
        
        x = 4;
        y = -3;
        z = 0;
        expected = f.value(x, y, z);
        result = tcf.value(x, y, z);
        Assert.assertEquals("On sample point",
                            expected, result, 1e-14);

        x = 4.5;
        y = -1.5;
        z = -4.25;
        expected = f.value(x, y, z);
        result = tcf.value(x, y, z);
        Assert.assertEquals("Half-way between sample points (middle of the patch)",
                            expected, result, 0.1);

        x = 3.5;
        y = -3.5;
        z = -10;
        expected = f.value(x, y, z);
        result = tcf.value(x, y, z);
        Assert.assertEquals("Half-way between sample points (border of the patch)",
                            expected, result, 0.1);
    }

// org.apache.commons.math3.analysis.interpolation.TricubicSplineInterpolatorTest::testPreconditions
    public void testPreconditions() {
        double[] xval = new double[] {3, 4, 5, 6.5};
        double[] yval = new double[] {-4, -3, -1, 2.5};
        double[] zval = new double[] {-12, -8, -5.5, -3, 0, 2.5};
        double[][][] fval = new double[xval.length][yval.length][zval.length];

        TrivariateGridInterpolator interpolator = new TricubicSplineInterpolator();
        
        @SuppressWarnings("unused")
        TrivariateFunction p = interpolator.interpolate(xval, yval, zval, fval);
        
        double[] wxval = new double[] {3, 2, 5, 6.5};
        try {
            p = interpolator.interpolate(wxval, yval, zval, fval);
            Assert.fail("an exception should have been thrown");
        } catch (MathIllegalArgumentException e) {
            
        }

        double[] wyval = new double[] {-4, -3, -1, -1};
        try {
            p = interpolator.interpolate(xval, wyval, zval, fval);
            Assert.fail("an exception should have been thrown");
        } catch (MathIllegalArgumentException e) {
            
        }

        double[] wzval = new double[] {-12, -8, -5.5, -3, -4, 2.5};
        try {
            p = interpolator.interpolate(xval, yval, wzval, fval);
            Assert.fail("an exception should have been thrown");
        } catch (MathIllegalArgumentException e) {
            
        }

        double[][][] wfval = new double[xval.length][yval.length + 1][zval.length];
        try {
            p = interpolator.interpolate(xval, yval, zval, wfval);
            Assert.fail("an exception should have been thrown");
        } catch (DimensionMismatchException e) {
            
        }
        wfval = new double[xval.length - 1][yval.length][zval.length];
        try {
            p = interpolator.interpolate(xval, yval, zval, wfval);
            Assert.fail("an exception should have been thrown");
        } catch (DimensionMismatchException e) {
            
        }
        wfval = new double[xval.length][yval.length][zval.length - 1];
        try {
            p = interpolator.interpolate(xval, yval, zval, wfval);
            Assert.fail("an exception should have been thrown");
        } catch (DimensionMismatchException e) {
            
        }
    }

// org.apache.commons.math3.analysis.interpolation.TricubicSplineInterpolatorTest::testPlane
    public void testPlane() {
        TrivariateFunction f = new TrivariateFunction() {
                public double value(double x, double y, double z) {
                    return 2 * x - 3 * y - z + 5;
                }
            };

        TrivariateGridInterpolator interpolator = new TricubicSplineInterpolator();

        double[] xval = new double[] {3, 4, 5, 6.5};
        double[] yval = new double[] {-4, -3, -1, 2, 2.5};
        double[] zval = new double[] {-12, -8, -5.5, -3, 0, 2.5};
        double[][][] fval = new double[xval.length][yval.length][zval.length];
        for (int i = 0; i < xval.length; i++) {
            for (int j = 0; j < yval.length; j++) {
                for (int k = 0; k < zval.length; k++) {
                    fval[i][j][k] = f.value(xval[i], yval[j], zval[k]);
                }
            }
        }

        TrivariateFunction p = interpolator.interpolate(xval, yval, zval, fval);
        double x, y, z;
        double expected, result;
        
        x = 4;
        y = -3;
        z = 0;
        expected = f.value(x, y, z);
        result = p.value(x, y, z);
        Assert.assertEquals("On sample point", expected, result, 1e-15);

        x = 4.5;
        y = -1.5;
        z = -4.25;
        expected = f.value(x, y, z);
        result = p.value(x, y, z);
        Assert.assertEquals("half-way between sample points (middle of the patch)", expected, result, 0.3);

        x = 3.5;
        y = -3.5;
        z = -10;
        expected = f.value(x, y, z);
        result = p.value(x, y, z);
        Assert.assertEquals("half-way between sample points (border of the patch)", expected, result, 0.3);
    }

// org.apache.commons.math3.analysis.interpolation.TricubicSplineInterpolatorTest::testWave
    public void testWave() {
        double[] xval = new double[] {3, 4, 5, 6.5};
        double[] yval = new double[] {-4, -3, -1, 2, 2.5};
        double[] zval = new double[] {-12, -8, -5.5, -3, 0, 4};

        final double a = 0.2;
        final double omega = 0.5;
        final double kx = 2;
        final double ky = 1;

        
        TrivariateFunction f = new TrivariateFunction() {
                public double value(double x, double y, double z) {
                    return a * FastMath.cos(omega * z - kx * x - ky * y);
                }
            };
        
        double[][][] fval = new double[xval.length][yval.length][zval.length];
        for (int i = 0; i < xval.length; i++) {
            for (int j = 0; j < yval.length; j++) {
                for (int k = 0; k < zval.length; k++) {
                    fval[i][j][k] = f.value(xval[i], yval[j], zval[k]);
                }
            }
        }

        TrivariateGridInterpolator interpolator = new TricubicSplineInterpolator();

        TrivariateFunction p = interpolator.interpolate(xval, yval, zval, fval);
        double x, y, z;
        double expected, result;
        
        x = 4;
        y = -3;
        z = 0;
        expected = f.value(x, y, z);
        result = p.value(x, y, z);
        Assert.assertEquals("On sample point",
                            expected, result, 1e-12);

        x = 4.5;
        y = -1.5;
        z = -4.25;
        expected = f.value(x, y, z);
        result = p.value(x, y, z);
        Assert.assertEquals("Half-way between sample points (middle of the patch)",
                            expected, result, 0.1);

        x = 3.5;
        y = -3.5;
        z = -10;
        expected = f.value(x, y, z);
        result = p.value(x, y, z);
        Assert.assertEquals("Half-way between sample points (border of the patch)",
                            expected, result, 0.1);
    }

// org.apache.commons.math3.analysis.interpolation.UnivariatePeriodicInterpolatorTest::testSine
    public void testSine() {
        final int n = 30;
        final double[] xval = new double[n];
        final double[] yval = new double[n];
        final double period = 12.3;
        final double offset = 45.67;

        double delta = 0;
        for (int i = 0; i < n; i++) {
            delta += rng.nextDouble() * period / n;
            xval[i] = offset + delta;
            yval[i] = FastMath.sin(xval[i]);
        }

        final UnivariateInterpolator inter = new LinearInterpolator();
        final UnivariateFunction f = inter.interpolate(xval, yval);

        final UnivariateInterpolator interP
            = new UnivariatePeriodicInterpolator(new LinearInterpolator(),
                                                     period, 1);
        final UnivariateFunction fP = interP.interpolate(xval, yval);

        
        final double xMin = xval[0];
        final double xMax = xval[n - 1];
        for (int i = 0; i < n; i++) {
            final double x = xMin + (xMax - xMin) * rng.nextDouble();
            final double y = f.value(x);
            final double yP = fP.value(x);

            Assert.assertEquals("x=" + x, y, yP, Math.ulp(1d));
        }

        
        for (int i = 0; i < n; i++) {
            final double xIn = offset + rng.nextDouble() * period;
            final double xOut = xIn + rng.nextInt(123456789) * period;
            final double yIn = fP.value(xIn);
            final double yOut = fP.value(xOut);

            Assert.assertEquals(yIn, yOut, 1e-7);
        }
    }

// org.apache.commons.math3.analysis.interpolation.UnivariatePeriodicInterpolatorTest::testLessThanOnePeriodCoverage
    public void testLessThanOnePeriodCoverage() {
        final int n = 30;
        final double[] xval = new double[n];
        final double[] yval = new double[n];
        final double period = 12.3;
        final double offset = 45.67;

        double delta = period / 2;
        for (int i = 0; i < n; i++) {
            delta += period / (2 * n) * rng.nextDouble();
            xval[i] = offset + delta;
            yval[i] = FastMath.sin(xval[i]);
        }

        final UnivariateInterpolator interP
            = new UnivariatePeriodicInterpolator(new LinearInterpolator(),
                                                     period, 1);
        final UnivariateFunction fP = interP.interpolate(xval, yval);

        
        for (int i = 0; i < n; i++) {
            final double xIn = offset + rng.nextDouble() * period;
            final double xOut = xIn + rng.nextInt(123456789) * period;
            final double yIn = fP.value(xIn);
            final double yOut = fP.value(xOut);

            Assert.assertEquals(yIn, yOut, 1e-7);
        }
    }

// org.apache.commons.math3.analysis.interpolation.UnivariatePeriodicInterpolatorTest::testMoreThanOnePeriodCoverage
    public void testMoreThanOnePeriodCoverage() {
        final int n = 30;
        final double[] xval = new double[n];
        final double[] yval = new double[n];
        final double period = 12.3;
        final double offset = 45.67;

        double delta = period / 2;
        for (int i = 0; i < n; i++) {
            delta += 10 * period / n * rng.nextDouble();
            xval[i] = offset + delta;
            yval[i] = FastMath.sin(xval[i]);
        }

        final UnivariateInterpolator interP
            = new UnivariatePeriodicInterpolator(new LinearInterpolator(),
                                                     period, 1);
        final UnivariateFunction fP = interP.interpolate(xval, yval);

        
        for (int i = 0; i < n; i++) {
            final double xIn = offset + rng.nextDouble() * period;
            final double xOut = xIn + rng.nextInt(123456789) * period;
            final double yIn = fP.value(xIn);
            final double yOut = fP.value(xOut);

            Assert.assertEquals(yIn, yOut, 1e-6);
        }
    }

// org.apache.commons.math3.analysis.interpolation.UnivariatePeriodicInterpolatorTest::testTooFewSamples
    public void testTooFewSamples() {
        final double[] xval = { 2, 3, 7 };
        final double[] yval = { 1, 6, 5 };
        final double period = 10;

        final UnivariateInterpolator interpolator
            = new UnivariatePeriodicInterpolator(new LinearInterpolator(), period);
        interpolator.interpolate(xval, yval);
    }

// org.apache.commons.math3.analysis.interpolation.UnivariatePeriodicInterpolatorTest::testUnsortedSamples
    public void testUnsortedSamples() {
        final double[] xval = { 2, 3, 7, 4, 6 };
        final double[] yval = { 1, 6, 5, -1, -2 };
        final double period = 10;

        final UnivariateInterpolator interpolator
            = new UnivariatePeriodicInterpolator(new LinearInterpolator(), period);
        interpolator.interpolate(xval, yval);
    }

// org.apache.commons.math3.analysis.polynomials.PolynomialFunctionLagrangeFormTest::testLinearFunction
    public void testLinearFunction() {
        PolynomialFunctionLagrangeForm p;
        double c[], z, expected, result, tolerance = 1E-12;

        
        double x[] = { 0.0, 3.0 };
        double y[] = { -4.0, 0.5 };
        p = new PolynomialFunctionLagrangeForm(x, y);

        z = 2.0; expected = -1.0; result = p.value(z);
        Assert.assertEquals(expected, result, tolerance);

        z = 4.5; expected = 2.75; result = p.value(z);
        Assert.assertEquals(expected, result, tolerance);

        z = 6.0; expected = 5.0; result = p.value(z);
        Assert.assertEquals(expected, result, tolerance);

        Assert.assertEquals(1, p.degree());

        c = p.getCoefficients();
        Assert.assertEquals(2, c.length);
        Assert.assertEquals(-4.0, c[0], tolerance);
        Assert.assertEquals(1.5, c[1], tolerance);
    }

// org.apache.commons.math3.analysis.polynomials.PolynomialFunctionLagrangeFormTest::testQuadraticFunction
    public void testQuadraticFunction() {
        PolynomialFunctionLagrangeForm p;
        double c[], z, expected, result, tolerance = 1E-12;

        
        double x[] = { 0.0, -1.0, 0.5 };
        double y[] = { -3.0, -6.0, 0.0 };
        p = new PolynomialFunctionLagrangeForm(x, y);

        z = 1.0; expected = 4.0; result = p.value(z);
        Assert.assertEquals(expected, result, tolerance);

        z = 2.5; expected = 22.0; result = p.value(z);
        Assert.assertEquals(expected, result, tolerance);

        z = -2.0; expected = -5.0; result = p.value(z);
        Assert.assertEquals(expected, result, tolerance);

        Assert.assertEquals(2, p.degree());

        c = p.getCoefficients();
        Assert.assertEquals(3, c.length);
        Assert.assertEquals(-3.0, c[0], tolerance);
        Assert.assertEquals(5.0, c[1], tolerance);
        Assert.assertEquals(2.0, c[2], tolerance);
    }

// org.apache.commons.math3.analysis.polynomials.PolynomialFunctionLagrangeFormTest::testQuinticFunction
    public void testQuinticFunction() {
        PolynomialFunctionLagrangeForm p;
        double c[], z, expected, result, tolerance = 1E-12;

        
        double x[] = { 1.0, -1.0, 2.0, 3.0, -3.0, 0.5 };
        double y[] = { 0.0, 0.0, -24.0, 0.0, -144.0, 2.34375 };
        p = new PolynomialFunctionLagrangeForm(x, y);

        z = 0.0; expected = 0.0; result = p.value(z);
        Assert.assertEquals(expected, result, tolerance);

        z = -2.0; expected = 0.0; result = p.value(z);
        Assert.assertEquals(expected, result, tolerance);

        z = 4.0; expected = 360.0; result = p.value(z);
        Assert.assertEquals(expected, result, tolerance);

        Assert.assertEquals(5, p.degree());

        c = p.getCoefficients();
        Assert.assertEquals(6, c.length);
        Assert.assertEquals(0.0, c[0], tolerance);
        Assert.assertEquals(6.0, c[1], tolerance);
        Assert.assertEquals(1.0, c[2], tolerance);
        Assert.assertEquals(-7.0, c[3], tolerance);
        Assert.assertEquals(-1.0, c[4], tolerance);
        Assert.assertEquals(1.0, c[5], tolerance);
    }

// org.apache.commons.math3.analysis.polynomials.PolynomialFunctionLagrangeFormTest::testParameters
    public void testParameters() {

        try {
            
            double x[] = { 1.0 };
            double y[] = { 2.0 };
            new PolynomialFunctionLagrangeForm(x, y);
            Assert.fail("Expecting MathIllegalArgumentException - bad input array length");
        } catch (MathIllegalArgumentException ex) {
            
        }
        try {
            
            double x[] = { 1.0, 2.0, 3.0, 4.0 };
            double y[] = { 0.0, -4.0, -24.0 };
            new PolynomialFunctionLagrangeForm(x, y);
            Assert.fail("Expecting MathIllegalArgumentException - mismatch input arrays");
        } catch (MathIllegalArgumentException ex) {
            
        }
    }

// org.apache.commons.math3.analysis.polynomials.PolynomialSplineFunctionTest::testConstructor
    public void testConstructor() {
        PolynomialSplineFunction spline =
            new PolynomialSplineFunction(knots, polynomials);
        Assert.assertTrue(Arrays.equals(knots, spline.getKnots()));
        Assert.assertEquals(1d, spline.getPolynomials()[0].getCoefficients()[2], 0);
        Assert.assertEquals(3, spline.getN());

        try { 
            new PolynomialSplineFunction(new double[] {0}, polynomials);
            Assert.fail("Expecting MathIllegalArgumentException");
        } catch (MathIllegalArgumentException ex) {
            
        }

        try { 
            new PolynomialSplineFunction(new double[] {0,1,2,3,4}, polynomials);
            Assert.fail("Expecting MathIllegalArgumentException");
        } catch (MathIllegalArgumentException ex) {
            
        }

        try { 
            new PolynomialSplineFunction(new double[] {0,1, 3, 2}, polynomials);
            Assert.fail("Expecting MathIllegalArgumentException");
        } catch (MathIllegalArgumentException ex) {
            
        }
    }

// org.apache.commons.math3.analysis.polynomials.PolynomialSplineFunctionTest::testValues
    public void testValues() {
        PolynomialSplineFunction spline =
            new PolynomialSplineFunction(knots, polynomials);
        UnivariateFunction dSpline = spline.derivative();

        
        double x = -1;
        int index = 0;
        for (int i = 0; i < 10; i++) {
           x+=0.25;
           index = findKnot(knots, x);
           Assert.assertEquals("spline function evaluation failed for x=" + x,
                   polynomials[index].value(x - knots[index]), spline.value(x), tolerance);
           Assert.assertEquals("spline derivative evaluation failed for x=" + x,
                   dp.value(x - knots[index]), dSpline.value(x), tolerance);
        }

        
        for (int i = 0; i < 3; i++) {
            Assert.assertEquals("spline function evaluation failed for knot=" + knots[i],
                    polynomials[i].value(0), spline.value(knots[i]), tolerance);
            Assert.assertEquals("spline function evaluation failed for knot=" + knots[i],
                    dp.value(0), dSpline.value(knots[i]), tolerance);
        }

        try { 
            x = spline.value(-1.5);
            Assert.fail("Expecting OutOfRangeException");
        } catch (OutOfRangeException ex) {
            
        }

        try { 
            x = spline.value(2.5);
            Assert.fail("Expecting OutOfRangeException");
        } catch (OutOfRangeException ex) {
            
        }
    }

// org.apache.commons.math3.analysis.polynomials.PolynomialSplineFunctionTest::testIsValidPoint
    public void testIsValidPoint() {
        final PolynomialSplineFunction spline =
            new PolynomialSplineFunction(knots, polynomials);
        final double xMin = knots[0];
        final double xMax = knots[knots.length - 1];

        double x;

        x = xMin;
        Assert.assertTrue(spline.isValidPoint(x));
        
        spline.value(x);

        x = xMax;
        Assert.assertTrue(spline.isValidPoint(x));
        
        spline.value(x);
 
        final double xRange = xMax - xMin;
        x = xMin + xRange / 3.4;
        Assert.assertTrue(spline.isValidPoint(x));
        
        spline.value(x);

        final double small = 1e-8;
        x = xMin - small;
        Assert.assertFalse(spline.isValidPoint(x));
        
        try {
            spline.value(x);
            Assert.fail("OutOfRangeException expected");
        } catch (OutOfRangeException expected) {}
    }

// org.apache.commons.math3.analysis.polynomials.PolynomialsUtilsTest::testFirstChebyshevPolynomials
    public void testFirstChebyshevPolynomials() {
        checkPolynomial(PolynomialsUtils.createChebyshevPolynomial(3), "-3 x + 4 x^3");
        checkPolynomial(PolynomialsUtils.createChebyshevPolynomial(2), "-1 + 2 x^2");
        checkPolynomial(PolynomialsUtils.createChebyshevPolynomial(1), "x");
        checkPolynomial(PolynomialsUtils.createChebyshevPolynomial(0), "1");

        checkPolynomial(PolynomialsUtils.createChebyshevPolynomial(7), "-7 x + 56 x^3 - 112 x^5 + 64 x^7");
        checkPolynomial(PolynomialsUtils.createChebyshevPolynomial(6), "-1 + 18 x^2 - 48 x^4 + 32 x^6");
        checkPolynomial(PolynomialsUtils.createChebyshevPolynomial(5), "5 x - 20 x^3 + 16 x^5");
        checkPolynomial(PolynomialsUtils.createChebyshevPolynomial(4), "1 - 8 x^2 + 8 x^4");

    }

// org.apache.commons.math3.analysis.polynomials.PolynomialsUtilsTest::testChebyshevBounds
    public void testChebyshevBounds() {
        for (int k = 0; k < 12; ++k) {
            PolynomialFunction Tk = PolynomialsUtils.createChebyshevPolynomial(k);
            for (double x = -1; x <= 1; x += 0.02) {
                Assert.assertTrue(k + " " + Tk.value(x), FastMath.abs(Tk.value(x)) < (1 + 1e-12));
            }
        }
    }

// org.apache.commons.math3.analysis.polynomials.PolynomialsUtilsTest::testChebyshevDifferentials
    public void testChebyshevDifferentials() {
        for (int k = 0; k < 12; ++k) {

            PolynomialFunction Tk0 = PolynomialsUtils.createChebyshevPolynomial(k);
            PolynomialFunction Tk1 = Tk0.polynomialDerivative();
            PolynomialFunction Tk2 = Tk1.polynomialDerivative();

            PolynomialFunction g0 = new PolynomialFunction(new double[] { k * k });
            PolynomialFunction g1 = new PolynomialFunction(new double[] { 0, -1});
            PolynomialFunction g2 = new PolynomialFunction(new double[] { 1, 0, -1 });

            PolynomialFunction Tk0g0 = Tk0.multiply(g0);
            PolynomialFunction Tk1g1 = Tk1.multiply(g1);
            PolynomialFunction Tk2g2 = Tk2.multiply(g2);

            checkNullPolynomial(Tk0g0.add(Tk1g1.add(Tk2g2)));

        }
    }

// org.apache.commons.math3.analysis.polynomials.PolynomialsUtilsTest::testChebyshevOrthogonality
    public void testChebyshevOrthogonality() {
        UnivariateFunction weight = new UnivariateFunction() {
            public double value(double x) {
                return 1 / FastMath.sqrt(1 - x * x);
            }
        };
        for (int i = 0; i < 10; ++i) {
            PolynomialFunction pi = PolynomialsUtils.createChebyshevPolynomial(i);
            for (int j = 0; j <= i; ++j) {
                PolynomialFunction pj = PolynomialsUtils.createChebyshevPolynomial(j);
                checkOrthogonality(pi, pj, weight, -0.9999, 0.9999, 1.5, 0.03);
            }
        }
    }

// org.apache.commons.math3.analysis.polynomials.PolynomialsUtilsTest::testFirstHermitePolynomials
    public void testFirstHermitePolynomials() {
        checkPolynomial(PolynomialsUtils.createHermitePolynomial(3), "-12 x + 8 x^3");
        checkPolynomial(PolynomialsUtils.createHermitePolynomial(2), "-2 + 4 x^2");
        checkPolynomial(PolynomialsUtils.createHermitePolynomial(1), "2 x");
        checkPolynomial(PolynomialsUtils.createHermitePolynomial(0), "1");

        checkPolynomial(PolynomialsUtils.createHermitePolynomial(7), "-1680 x + 3360 x^3 - 1344 x^5 + 128 x^7");
        checkPolynomial(PolynomialsUtils.createHermitePolynomial(6), "-120 + 720 x^2 - 480 x^4 + 64 x^6");
        checkPolynomial(PolynomialsUtils.createHermitePolynomial(5), "120 x - 160 x^3 + 32 x^5");
        checkPolynomial(PolynomialsUtils.createHermitePolynomial(4), "12 - 48 x^2 + 16 x^4");

    }

// org.apache.commons.math3.analysis.polynomials.PolynomialsUtilsTest::testHermiteDifferentials
    public void testHermiteDifferentials() {
        for (int k = 0; k < 12; ++k) {

            PolynomialFunction Hk0 = PolynomialsUtils.createHermitePolynomial(k);
            PolynomialFunction Hk1 = Hk0.polynomialDerivative();
            PolynomialFunction Hk2 = Hk1.polynomialDerivative();

            PolynomialFunction g0 = new PolynomialFunction(new double[] { 2 * k });
            PolynomialFunction g1 = new PolynomialFunction(new double[] { 0, -2 });
            PolynomialFunction g2 = new PolynomialFunction(new double[] { 1 });

            PolynomialFunction Hk0g0 = Hk0.multiply(g0);
            PolynomialFunction Hk1g1 = Hk1.multiply(g1);
            PolynomialFunction Hk2g2 = Hk2.multiply(g2);

            checkNullPolynomial(Hk0g0.add(Hk1g1.add(Hk2g2)));

        }
    }

// org.apache.commons.math3.analysis.polynomials.PolynomialsUtilsTest::testHermiteOrthogonality
    public void testHermiteOrthogonality() {
        UnivariateFunction weight = new UnivariateFunction() {
            public double value(double x) {
                return FastMath.exp(-x * x);
            }
        };
        for (int i = 0; i < 10; ++i) {
            PolynomialFunction pi = PolynomialsUtils.createHermitePolynomial(i);
            for (int j = 0; j <= i; ++j) {
                PolynomialFunction pj = PolynomialsUtils.createHermitePolynomial(j);
                checkOrthogonality(pi, pj, weight, -50, 50, 1.5, 1.0e-8);
            }
        }
    }

// org.apache.commons.math3.analysis.polynomials.PolynomialsUtilsTest::testFirstLaguerrePolynomials
    public void testFirstLaguerrePolynomials() {
        checkPolynomial(PolynomialsUtils.createLaguerrePolynomial(3), 6l, "6 - 18 x + 9 x^2 - x^3");
        checkPolynomial(PolynomialsUtils.createLaguerrePolynomial(2), 2l, "2 - 4 x + x^2");
        checkPolynomial(PolynomialsUtils.createLaguerrePolynomial(1), 1l, "1 - x");
        checkPolynomial(PolynomialsUtils.createLaguerrePolynomial(0), 1l, "1");

        checkPolynomial(PolynomialsUtils.createLaguerrePolynomial(7), 5040l,
                "5040 - 35280 x + 52920 x^2 - 29400 x^3"
                + " + 7350 x^4 - 882 x^5 + 49 x^6 - x^7");
        checkPolynomial(PolynomialsUtils.createLaguerrePolynomial(6),  720l,
                "720 - 4320 x + 5400 x^2 - 2400 x^3 + 450 x^4"
                + " - 36 x^5 + x^6");
        checkPolynomial(PolynomialsUtils.createLaguerrePolynomial(5),  120l,
        "120 - 600 x + 600 x^2 - 200 x^3 + 25 x^4 - x^5");
        checkPolynomial(PolynomialsUtils.createLaguerrePolynomial(4),   24l,
        "24 - 96 x + 72 x^2 - 16 x^3 + x^4");

    }

// org.apache.commons.math3.analysis.polynomials.PolynomialsUtilsTest::testLaguerreDifferentials
    public void testLaguerreDifferentials() {
        for (int k = 0; k < 12; ++k) {

            PolynomialFunction Lk0 = PolynomialsUtils.createLaguerrePolynomial(k);
            PolynomialFunction Lk1 = Lk0.polynomialDerivative();
            PolynomialFunction Lk2 = Lk1.polynomialDerivative();

            PolynomialFunction g0 = new PolynomialFunction(new double[] { k });
            PolynomialFunction g1 = new PolynomialFunction(new double[] { 1, -1 });
            PolynomialFunction g2 = new PolynomialFunction(new double[] { 0, 1 });

            PolynomialFunction Lk0g0 = Lk0.multiply(g0);
            PolynomialFunction Lk1g1 = Lk1.multiply(g1);
            PolynomialFunction Lk2g2 = Lk2.multiply(g2);

            checkNullPolynomial(Lk0g0.add(Lk1g1.add(Lk2g2)));

        }
    }

// org.apache.commons.math3.analysis.polynomials.PolynomialsUtilsTest::testLaguerreOrthogonality
    public void testLaguerreOrthogonality() {
        UnivariateFunction weight = new UnivariateFunction() {
            public double value(double x) {
                return FastMath.exp(-x);
            }
        };
        for (int i = 0; i < 10; ++i) {
            PolynomialFunction pi = PolynomialsUtils.createLaguerrePolynomial(i);
            for (int j = 0; j <= i; ++j) {
                PolynomialFunction pj = PolynomialsUtils.createLaguerrePolynomial(j);
                checkOrthogonality(pi, pj, weight, 0.0, 100.0, 0.99999, 1.0e-13);
            }
        }
    }

// org.apache.commons.math3.analysis.polynomials.PolynomialsUtilsTest::testFirstLegendrePolynomials
    public void testFirstLegendrePolynomials() {
        checkPolynomial(PolynomialsUtils.createLegendrePolynomial(3),  2l, "-3 x + 5 x^3");
        checkPolynomial(PolynomialsUtils.createLegendrePolynomial(2),  2l, "-1 + 3 x^2");
        checkPolynomial(PolynomialsUtils.createLegendrePolynomial(1),  1l, "x");
        checkPolynomial(PolynomialsUtils.createLegendrePolynomial(0),  1l, "1");

        checkPolynomial(PolynomialsUtils.createLegendrePolynomial(7), 16l, "-35 x + 315 x^3 - 693 x^5 + 429 x^7");
        checkPolynomial(PolynomialsUtils.createLegendrePolynomial(6), 16l, "-5 + 105 x^2 - 315 x^4 + 231 x^6");
        checkPolynomial(PolynomialsUtils.createLegendrePolynomial(5),  8l, "15 x - 70 x^3 + 63 x^5");
        checkPolynomial(PolynomialsUtils.createLegendrePolynomial(4),  8l, "3 - 30 x^2 + 35 x^4");

    }

// org.apache.commons.math3.analysis.polynomials.PolynomialsUtilsTest::testLegendreDifferentials
    public void testLegendreDifferentials() {
        for (int k = 0; k < 12; ++k) {

            PolynomialFunction Pk0 = PolynomialsUtils.createLegendrePolynomial(k);
            PolynomialFunction Pk1 = Pk0.polynomialDerivative();
            PolynomialFunction Pk2 = Pk1.polynomialDerivative();

            PolynomialFunction g0 = new PolynomialFunction(new double[] { k * (k + 1) });
            PolynomialFunction g1 = new PolynomialFunction(new double[] { 0, -2 });
            PolynomialFunction g2 = new PolynomialFunction(new double[] { 1, 0, -1 });

            PolynomialFunction Pk0g0 = Pk0.multiply(g0);
            PolynomialFunction Pk1g1 = Pk1.multiply(g1);
            PolynomialFunction Pk2g2 = Pk2.multiply(g2);

            checkNullPolynomial(Pk0g0.add(Pk1g1.add(Pk2g2)));

        }
    }

// org.apache.commons.math3.analysis.polynomials.PolynomialsUtilsTest::testLegendreOrthogonality
    public void testLegendreOrthogonality() {
        UnivariateFunction weight = new UnivariateFunction() {
            public double value(double x) {
                return 1;
            }
        };
        for (int i = 0; i < 10; ++i) {
            PolynomialFunction pi = PolynomialsUtils.createLegendrePolynomial(i);
            for (int j = 0; j <= i; ++j) {
                PolynomialFunction pj = PolynomialsUtils.createLegendrePolynomial(j);
                checkOrthogonality(pi, pj, weight, -1, 1, 0.1, 1.0e-13);
            }
        }
    }

// org.apache.commons.math3.analysis.polynomials.PolynomialsUtilsTest::testHighDegreeLegendre
    public void testHighDegreeLegendre() {
        PolynomialsUtils.createLegendrePolynomial(40);
        double[] l40 = PolynomialsUtils.createLegendrePolynomial(40).getCoefficients();
        double denominator = 274877906944d;
        double[] numerators = new double[] {
                          +34461632205d,            -28258538408100d,          +3847870979902950d,        -207785032914759300d,
                  +5929294332103310025d,     -103301483474866556880d,    +1197358103913226000200d,    -9763073770369381232400d,
              +58171647881784229843050d,  -260061484647976556945400d,  +888315281771246239250340d, -2345767627188139419665400d,
            +4819022625419112503443050d, -7710436200670580005508880d, +9566652323054238154983240d, -9104813935044723209570256d,
            +6516550296251767619752905d, -3391858621221953912598660d, +1211378079007840683070950d,  -265365894974690562152100d,
              +26876802183334044115405d
        };
        for (int i = 0; i < l40.length; ++i) {
            if (i % 2 == 0) {
                double ci = numerators[i / 2] / denominator;
                Assert.assertEquals(ci, l40[i], FastMath.abs(ci) * 1e-15);
            } else {
                Assert.assertEquals(0, l40[i], 0);
            }
        }
    }

// org.apache.commons.math3.analysis.polynomials.PolynomialsUtilsTest::testJacobiLegendre
    public void testJacobiLegendre() {
        for (int i = 0; i < 10; ++i) {
            PolynomialFunction legendre = PolynomialsUtils.createLegendrePolynomial(i);
            PolynomialFunction jacobi   = PolynomialsUtils.createJacobiPolynomial(i, 0, 0);
            checkNullPolynomial(legendre.subtract(jacobi));
        }
    }

// org.apache.commons.math3.analysis.polynomials.PolynomialsUtilsTest::testJacobiEvaluationAt1
    public void testJacobiEvaluationAt1() {
        for (int v = 0; v < 10; ++v) {
            for (int w = 0; w < 10; ++w) {
                for (int i = 0; i < 10; ++i) {
                    PolynomialFunction jacobi = PolynomialsUtils.createJacobiPolynomial(i, v, w);
                    double binomial = ArithmeticUtils.binomialCoefficient(v + i, i);
                    Assert.assertTrue(Precision.equals(binomial, jacobi.value(1.0), 1));
                }
            }
        }
    }

// org.apache.commons.math3.analysis.polynomials.PolynomialsUtilsTest::testJacobiOrthogonality
    public void testJacobiOrthogonality() {
        for (int v = 0; v < 5; ++v) {
            for (int w = v; w < 5; ++w) {
                final int vv = v;
                final int ww = w;
                UnivariateFunction weight = new UnivariateFunction() {
                    public double value(double x) {
                        return FastMath.pow(1 - x, vv) * FastMath.pow(1 + x, ww);
                    }
                };
                for (int i = 0; i < 10; ++i) {
                    PolynomialFunction pi = PolynomialsUtils.createJacobiPolynomial(i, v, w);
                    for (int j = 0; j <= i; ++j) {
                        PolynomialFunction pj = PolynomialsUtils.createJacobiPolynomial(j, v, w);
                        checkOrthogonality(pi, pj, weight, -1, 1, 0.1, 1.0e-12);
                    }
                }
            }
        }
    }

// org.apache.commons.math3.analysis.polynomials.PolynomialsUtilsTest::testShift
    public void testShift() {
        
        PolynomialFunction f1x = new PolynomialFunction(new double[] { 1, 1, 2 });

        PolynomialFunction f1x1
            = new PolynomialFunction(PolynomialsUtils.shift(f1x.getCoefficients(), 1));
        checkPolynomial(f1x1, "4 + 5 x + 2 x^2");

        PolynomialFunction f1xM1
            = new PolynomialFunction(PolynomialsUtils.shift(f1x.getCoefficients(), -1));
        checkPolynomial(f1xM1, "2 - 3 x + 2 x^2");
        
        PolynomialFunction f1x3
            = new PolynomialFunction(PolynomialsUtils.shift(f1x.getCoefficients(), 3));
        checkPolynomial(f1x3, "22 + 13 x + 2 x^2");

        
        PolynomialFunction f2x = new PolynomialFunction(new double[]{2, 0, 3, 8, 0, 121});

        PolynomialFunction f2x1
            = new PolynomialFunction(PolynomialsUtils.shift(f2x.getCoefficients(), 1));
        checkPolynomial(f2x1, "134 + 635 x + 1237 x^2 + 1218 x^3 + 605 x^4 + 121 x^5");

        PolynomialFunction f2x3
            = new PolynomialFunction(PolynomialsUtils.shift(f2x.getCoefficients(), 3));
        checkPolynomial(f2x3, "29648 + 49239 x + 32745 x^2 + 10898 x^3 + 1815 x^4 + 121 x^5");
    }

// org.apache.commons.math3.complex.QuaternionTest::testWrongDimension
    public void testWrongDimension() {
        new Quaternion(new double[] { 1, 2 });
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

        Assert.assertEquals("log10 #1", 1, field.newDfp("12").intLog10());
        Assert.assertEquals("log10 #2", 2, field.newDfp("123").intLog10());
        Assert.assertEquals("log10 #3", 3, field.newDfp("1234").intLog10());
        Assert.assertEquals("log10 #4", 4, field.newDfp("12345").intLog10());
        Assert.assertEquals("log10 #5", 5, field.newDfp("123456").intLog10());
        Assert.assertEquals("log10 #6", 6, field.newDfp("1234567").intLog10());
        Assert.assertEquals("log10 #6", 7, field.newDfp("12345678").intLog10());
        Assert.assertEquals("log10 #7", 8, field.newDfp("123456789").intLog10());
        Assert.assertEquals("log10 #8", 9, field.newDfp("1234567890").intLog10());
        Assert.assertEquals("log10 #9", 10, field.newDfp("12345678901").intLog10());
        Assert.assertEquals("log10 #10", 11, field.newDfp("123456789012").intLog10());
        Assert.assertEquals("log10 #11", 12, field.newDfp("1234567890123").intLog10());

        Assert.assertEquals("log10 #12", 0, field.newDfp("2").intLog10());
        Assert.assertEquals("log10 #13", 0, field.newDfp("1").intLog10());
        Assert.assertEquals("log10 #14", -1, field.newDfp("0.12").intLog10());
        Assert.assertEquals("log10 #15", -2, field.newDfp("0.012").intLog10());
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

// org.apache.commons.math3.dfp.DfpTest::testSpecialConstructors
    public void testSpecialConstructors() {
        Assert.assertEquals(ninf, field.newDfp(Double.NEGATIVE_INFINITY));
        Assert.assertEquals(ninf, field.newDfp("-Infinity"));
        Assert.assertEquals(pinf, field.newDfp(Double.POSITIVE_INFINITY));
        Assert.assertEquals(pinf, field.newDfp("Infinity"));
        Assert.assertTrue(field.newDfp(Double.NaN).isNaN());
        Assert.assertTrue(field.newDfp("NaN").isNaN());
    }

// org.apache.commons.math3.distribution.BinomialDistributionTest::testDegenerate0
    public void testDegenerate0() {
        BinomialDistribution dist = new BinomialDistribution(5, 0.0d);
        setDistribution(dist);
        setCumulativeTestPoints(new int[] { -1, 0, 1, 5, 10 });
        setCumulativeTestValues(new double[] { 0d, 1d, 1d, 1d, 1d });
        setDensityTestPoints(new int[] { -1, 0, 1, 10, 11 });
        setDensityTestValues(new double[] { 0d, 1d, 0d, 0d, 0d });
        setInverseCumulativeTestPoints(new double[] { 0.1d, 0.5d });
        setInverseCumulativeTestValues(new int[] { 0, 0 });
        verifyDensities();
        verifyCumulativeProbabilities();
        verifyInverseCumulativeProbabilities();
        Assert.assertEquals(dist.getSupportLowerBound(), 0);
        Assert.assertEquals(dist.getSupportUpperBound(), 0);
    }

// org.apache.commons.math3.distribution.BinomialDistributionTest::testDegenerate1
    public void testDegenerate1() {
        BinomialDistribution dist = new BinomialDistribution(5, 1.0d);
        setDistribution(dist);
        setCumulativeTestPoints(new int[] { -1, 0, 1, 2, 5, 10 });
        setCumulativeTestValues(new double[] { 0d, 0d, 0d, 0d, 1d, 1d });
        setDensityTestPoints(new int[] { -1, 0, 1, 2, 5, 10 });
        setDensityTestValues(new double[] { 0d, 0d, 0d, 0d, 1d, 0d });
        setInverseCumulativeTestPoints(new double[] { 0.1d, 0.5d });
        setInverseCumulativeTestValues(new int[] { 5, 5 });
        verifyDensities();
        verifyCumulativeProbabilities();
        verifyInverseCumulativeProbabilities();
        Assert.assertEquals(dist.getSupportLowerBound(), 5);
        Assert.assertEquals(dist.getSupportUpperBound(), 5);
    }

// org.apache.commons.math3.distribution.BinomialDistributionTest::testMoments
    public void testMoments() {
        final double tol = 1e-9;
        BinomialDistribution dist;

        dist = new BinomialDistribution(10, 0.5);
        Assert.assertEquals(dist.getNumericalMean(), 10d * 0.5d, tol);
        Assert.assertEquals(dist.getNumericalVariance(), 10d * 0.5d * 0.5d, tol);

        dist = new BinomialDistribution(30, 0.3);
        Assert.assertEquals(dist.getNumericalMean(), 30d * 0.3d, tol);
        Assert.assertEquals(dist.getNumericalVariance(), 30d * 0.3d * (1d - 0.3d), tol);
    }

// org.apache.commons.math3.distribution.BinomialDistributionTest::testMath718
    public void testMath718() {
        
        
        

        for (int trials = 500000; trials < 20000000; trials += 100000) {
            BinomialDistribution dist = new BinomialDistribution(trials, 0.5);
            int p = dist.inverseCumulativeProbability(0.5);
            Assert.assertEquals(trials / 2, p);
        }
    }

// org.apache.commons.math3.distribution.CauchyDistributionTest::testInverseCumulativeProbabilityExtremes
    public void testInverseCumulativeProbabilityExtremes() {
        setInverseCumulativeTestPoints(new double[] {0.0, 1.0});
        setInverseCumulativeTestValues(
                new double[] {Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY});
        verifyInverseCumulativeProbabilities();
    }

// org.apache.commons.math3.distribution.CauchyDistributionTest::testMedian
    public void testMedian() {
        CauchyDistribution distribution = (CauchyDistribution) getDistribution();
        Assert.assertEquals(1.2, distribution.getMedian(), 0.0);
    }

// org.apache.commons.math3.distribution.CauchyDistributionTest::testScale
    public void testScale() {
        CauchyDistribution distribution = (CauchyDistribution) getDistribution();
        Assert.assertEquals(2.1, distribution.getScale(), 0.0);
    }

// org.apache.commons.math3.distribution.CauchyDistributionTest::testPreconditions
    public void testPreconditions() {
        try {
            new CauchyDistribution(0, 0);
            Assert.fail("Cannot have zero scale");
        } catch (NotStrictlyPositiveException ex) {
            
        }
        try {
            new CauchyDistribution(0, -1);
            Assert.fail("Cannot have negative scale");
        } catch (NotStrictlyPositiveException ex) {
            
        }
    }

// org.apache.commons.math3.distribution.CauchyDistributionTest::testMoments
    public void testMoments() {
        CauchyDistribution dist;

        dist = new CauchyDistribution(10.2, 0.15);
        Assert.assertTrue(Double.isNaN(dist.getNumericalMean()));
        Assert.assertTrue(Double.isNaN(dist.getNumericalVariance()));

        dist = new CauchyDistribution(23.12, 2.12);
        Assert.assertTrue(Double.isNaN(dist.getNumericalMean()));
        Assert.assertTrue(Double.isNaN(dist.getNumericalVariance()));
    }

// org.apache.commons.math3.distribution.CauchyDistributionTest::testSampling
    public void testSampling() {}

// org.apache.commons.math3.distribution.ChiSquaredDistributionTest::testSmallDf
    public void testSmallDf() {
        setDistribution(new ChiSquaredDistribution(0.1d));
        setTolerance(1E-4);
        
        setCumulativeTestPoints(new double[] {1.168926E-60, 1.168926E-40, 1.063132E-32,
                1.144775E-26, 1.168926E-20, 5.472917, 2.175255, 1.13438,
                0.5318646, 0.1526342});
        setInverseCumulativeTestValues(getCumulativeTestPoints());
        setInverseCumulativeTestPoints(getCumulativeTestValues());
        verifyCumulativeProbabilities();
        verifyInverseCumulativeProbabilities();
    }

// org.apache.commons.math3.distribution.ChiSquaredDistributionTest::testDfAccessors
    public void testDfAccessors() {
        ChiSquaredDistribution distribution = (ChiSquaredDistribution) getDistribution();
        Assert.assertEquals(5d, distribution.getDegreesOfFreedom(), Double.MIN_VALUE);
    }

// org.apache.commons.math3.distribution.ChiSquaredDistributionTest::testDensity
    public void testDensity() {
        double[] x = new double[]{-0.1, 1e-6, 0.5, 1, 2, 5};
        
        checkDensity(1, x, new double[]{0.00000000000, 398.94208093034, 0.43939128947, 0.24197072452, 0.10377687436, 0.01464498256});
        
        checkDensity(0.1, x, new double[]{0.000000000e+00, 2.486453997e+04, 7.464238732e-02, 3.009077718e-02, 9.447299159e-03, 8.827199396e-04});
        
        checkDensity(2, x, new double[]{0.00000000000, 0.49999975000, 0.38940039154, 0.30326532986, 0.18393972059, 0.04104249931});
        
        checkDensity(10, x, new double[]{0.000000000e+00, 1.302082682e-27, 6.337896998e-05, 7.897534632e-04, 7.664155024e-03, 6.680094289e-02});
    }

// org.apache.commons.math3.distribution.ChiSquaredDistributionTest::testMoments
    public void testMoments() {
        final double tol = 1e-9;
        ChiSquaredDistribution dist;

        dist = new ChiSquaredDistribution(1500);
        Assert.assertEquals(dist.getNumericalMean(), 1500, tol);
        Assert.assertEquals(dist.getNumericalVariance(), 3000, tol);

        dist = new ChiSquaredDistribution(1.12);
        Assert.assertEquals(dist.getNumericalMean(), 1.12, tol);
        Assert.assertEquals(dist.getNumericalVariance(), 2.24, tol);
    }

// org.apache.commons.math3.distribution.EnumeratedIntegerDistributionTest::testExceptions
    public void testExceptions() {
        EnumeratedIntegerDistribution invalid = null;
        try {
            new EnumeratedIntegerDistribution(new int[]{1, 2}, new double[]{0.0});
            Assert.fail("Expected DimensionMismatchException");
        } catch (DimensionMismatchException e) {
        }
        try {
            new EnumeratedIntegerDistribution(new int[]{1, 2}, new double[]{0.0, -1.0});
            Assert.fail("Expected NotPositiveException");
        } catch (NotPositiveException e) {
        }
        try {
            new EnumeratedIntegerDistribution(new int[]{1, 2}, new double[]{0.0, 0.0});
            Assert.fail("Expected MathArithmeticException");
        } catch (MathArithmeticException e) {
        }
        try {
          new EnumeratedIntegerDistribution(new int[]{1, 2}, new double[]{0.0, Double.NaN});
            Assert.fail("Expected NotANumberException");
        } catch (NotANumberException e) {
        }
        try {
        new EnumeratedIntegerDistribution(new int[]{1, 2}, new double[]{0.0, Double.POSITIVE_INFINITY});
            Assert.fail("Expected NotFiniteNumberException");
        } catch (NotFiniteNumberException e) {
        }
        Assert.assertNull("Expected non-initialized DiscreteRealDistribution", invalid);
    }

// org.apache.commons.math3.distribution.EnumeratedIntegerDistributionTest::testProbability
    public void testProbability() {
        int[] points = new int[]{-2, -1, 0, 1, 2, 3, 4, 5, 6, 7, 8};
        double[] results = new double[]{0, 0.2, 0, 0, 0, 0.5, 0, 0, 0, 0.3, 0};
        for (int p = 0; p < points.length; p++) {
            double probability = testDistribution.probability(points[p]);
            Assert.assertEquals(results[p], probability, 0.0);
        }
    }

// org.apache.commons.math3.distribution.EnumeratedIntegerDistributionTest::testCumulativeProbability
    public void testCumulativeProbability() {
        int[] points = new int[]{-2, -1, 0, 1, 2, 3, 4, 5, 6, 7, 8};
        double[] results = new double[]{0, 0.2, 0.2, 0.2, 0.2, 0.7, 0.7, 0.7, 0.7, 1.0, 1.0};
        for (int p = 0; p < points.length; p++) {
            double probability = testDistribution.cumulativeProbability(points[p]);
            Assert.assertEquals(results[p], probability, 1e-10);
        }
    }

// org.apache.commons.math3.distribution.EnumeratedIntegerDistributionTest::testGetNumericalMean
    public void testGetNumericalMean() {
        Assert.assertEquals(3.4, testDistribution.getNumericalMean(), 1e-10);
    }

// org.apache.commons.math3.distribution.EnumeratedIntegerDistributionTest::testGetNumericalVariance
    public void testGetNumericalVariance() {
        Assert.assertEquals(7.84, testDistribution.getNumericalVariance(), 1e-10);
    }

// org.apache.commons.math3.distribution.EnumeratedIntegerDistributionTest::testGetSupportLowerBound
    public void testGetSupportLowerBound() {
        Assert.assertEquals(-1, testDistribution.getSupportLowerBound());
    }

// org.apache.commons.math3.distribution.EnumeratedIntegerDistributionTest::testGetSupportUpperBound
    public void testGetSupportUpperBound() {
        Assert.assertEquals(7, testDistribution.getSupportUpperBound());
    }

// org.apache.commons.math3.distribution.EnumeratedIntegerDistributionTest::testIsSupportConnected
    public void testIsSupportConnected() {
        Assert.assertTrue(testDistribution.isSupportConnected());
    }

// org.apache.commons.math3.distribution.EnumeratedIntegerDistributionTest::testSample
    public void testSample() {
        final int n = 1000000;
        testDistribution.reseedRandomGenerator(-334759360); 
        final int[] samples = testDistribution.sample(n);
        Assert.assertEquals(n, samples.length);
        double sum = 0;
        double sumOfSquares = 0;
        for (int i = 0; i < samples.length; i++) {
            sum += samples[i];
            sumOfSquares += samples[i] * samples[i];
        }
        Assert.assertEquals(testDistribution.getNumericalMean(),
                sum / n, 1e-2);
        Assert.assertEquals(testDistribution.getNumericalVariance(),
                sumOfSquares / n - FastMath.pow(sum / n, 2), 1e-2);
    }

// org.apache.commons.math3.distribution.EnumeratedRealDistributionTest::testExceptions
    public void testExceptions() {
        EnumeratedRealDistribution invalid = null;
        try {
            invalid = new EnumeratedRealDistribution(new double[]{1.0, 2.0}, new double[]{0.0});
            Assert.fail("Expected DimensionMismatchException");
        } catch (DimensionMismatchException e) {
        }
        try{
        invalid = new EnumeratedRealDistribution(new double[]{1.0, 2.0}, new double[]{0.0, -1.0});
            Assert.fail("Expected NotPositiveException");
        } catch (NotPositiveException e) {
        }
        try {
            invalid = new EnumeratedRealDistribution(new double[]{1.0, 2.0}, new double[]{0.0, 0.0});
            Assert.fail("Expected MathArithmeticException");
        } catch (MathArithmeticException e) {
        }
        try {
            invalid = new EnumeratedRealDistribution(new double[]{1.0, 2.0}, new double[]{0.0, Double.NaN});
            Assert.fail("Expected NotANumberException");
        } catch (NotANumberException e) {
        }
        try {
            invalid = new EnumeratedRealDistribution(new double[]{1.0, 2.0}, new double[]{0.0, Double.POSITIVE_INFINITY});
            Assert.fail("Expected NotFiniteNumberException");
        } catch (NotFiniteNumberException e) {
        }
        Assert.assertNull("Expected non-initialized DiscreteRealDistribution", invalid);
    }

// org.apache.commons.math3.distribution.EnumeratedRealDistributionTest::testProbability
    public void testProbability() {
        double[] points = new double[]{-2.0, -1.0, 0.0, 1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0};
        double[] results = new double[]{0, 0.2, 0, 0, 0, 0.5, 0, 0, 0, 0.3, 0};
        for (int p = 0; p < points.length; p++) {
            double density = testDistribution.probability(points[p]);
            Assert.assertEquals(results[p], density, 0.0);
        }
    }

// org.apache.commons.math3.distribution.EnumeratedRealDistributionTest::testDensity
    public void testDensity() {
        double[] points = new double[]{-2.0, -1.0, 0.0, 1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0};
        double[] results = new double[]{0, 0.2, 0, 0, 0, 0.5, 0, 0, 0, 0.3, 0};
        for (int p = 0; p < points.length; p++) {
            double density = testDistribution.density(points[p]);
            Assert.assertEquals(results[p], density, 0.0);
        }
    }

// org.apache.commons.math3.distribution.EnumeratedRealDistributionTest::testCumulativeProbability
    public void testCumulativeProbability() {
        double[] points = new double[]{-2.0, -1.0, 0.0, 1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0};
        double[] results = new double[]{0, 0.2, 0.2, 0.2, 0.2, 0.7, 0.7, 0.7, 0.7, 1.0, 1.0};
        for (int p = 0; p < points.length; p++) {
            double probability = testDistribution.cumulativeProbability(points[p]);
            Assert.assertEquals(results[p], probability, 1e-10);
        }
    }

// org.apache.commons.math3.distribution.EnumeratedRealDistributionTest::testGetNumericalMean
    public void testGetNumericalMean() {
        Assert.assertEquals(3.4, testDistribution.getNumericalMean(), 1e-10);
    }

// org.apache.commons.math3.distribution.EnumeratedRealDistributionTest::testGetNumericalVariance
    public void testGetNumericalVariance() {
        Assert.assertEquals(7.84, testDistribution.getNumericalVariance(), 1e-10);
    }

// org.apache.commons.math3.distribution.EnumeratedRealDistributionTest::testGetSupportLowerBound
    public void testGetSupportLowerBound() {
        Assert.assertEquals(-1, testDistribution.getSupportLowerBound(), 0);
    }

// org.apache.commons.math3.distribution.EnumeratedRealDistributionTest::testGetSupportUpperBound
    public void testGetSupportUpperBound() {
        Assert.assertEquals(7, testDistribution.getSupportUpperBound(), 0);
    }

// org.apache.commons.math3.distribution.EnumeratedRealDistributionTest::testIsSupportLowerBoundInclusive
    public void testIsSupportLowerBoundInclusive() {
        Assert.assertTrue(testDistribution.isSupportLowerBoundInclusive());
    }

// org.apache.commons.math3.distribution.EnumeratedRealDistributionTest::testIsSupportUpperBoundInclusive
    public void testIsSupportUpperBoundInclusive() {
        Assert.assertTrue(testDistribution.isSupportUpperBoundInclusive());
    }

// org.apache.commons.math3.distribution.EnumeratedRealDistributionTest::testIsSupportConnected
    public void testIsSupportConnected() {
        Assert.assertTrue(testDistribution.isSupportConnected());
    }

// org.apache.commons.math3.distribution.EnumeratedRealDistributionTest::testSample
    public void testSample() {
        final int n = 1000000;
        testDistribution.reseedRandomGenerator(-334759360); 
        final double[] samples = testDistribution.sample(n);
        Assert.assertEquals(n, samples.length);
        double sum = 0;
        double sumOfSquares = 0;
        for (int i = 0; i < samples.length; i++) {
            sum += samples[i];
            sumOfSquares += samples[i] * samples[i];
        }
        Assert.assertEquals(testDistribution.getNumericalMean(),
                sum / n, 1e-2);
        Assert.assertEquals(testDistribution.getNumericalVariance(),
                sumOfSquares / n - FastMath.pow(sum / n, 2), 1e-2);
    }

// org.apache.commons.math3.distribution.EnumeratedRealDistributionTest::testIssue942
    public void testIssue942() {
        List<Pair<Object,Double>> list = new ArrayList<Pair<Object, Double>>();
        list.add(new Pair<Object, Double>(new Object() {}, new Double(0)));
        list.add(new Pair<Object, Double>(new Object() {}, new Double(1)));
        Assert.assertEquals(1, new EnumeratedDistribution<Object>(list).sample(1).length);
    }

// org.apache.commons.math3.distribution.ExponentialDistributionTest::testCumulativeProbabilityExtremes
    public void testCumulativeProbabilityExtremes() {
        setCumulativeTestPoints(new double[] {-2, 0});
        setCumulativeTestValues(new double[] {0, 0});
        verifyCumulativeProbabilities();
    }

// org.apache.commons.math3.distribution.ExponentialDistributionTest::testInverseCumulativeProbabilityExtremes
    public void testInverseCumulativeProbabilityExtremes() {
         setInverseCumulativeTestPoints(new double[] {0, 1});
         setInverseCumulativeTestValues(new double[] {0, Double.POSITIVE_INFINITY});
         verifyInverseCumulativeProbabilities();
    }

// org.apache.commons.math3.distribution.ExponentialDistributionTest::testCumulativeProbability2
    public void testCumulativeProbability2() {
        double actual = getDistribution().cumulativeProbability(0.25, 0.75);
        Assert.assertEquals(0.0905214, actual, 10e-4);
    }

// org.apache.commons.math3.distribution.ExponentialDistributionTest::testDensity
    public void testDensity() {
        ExponentialDistribution d1 = new ExponentialDistribution(1);
        Assert.assertTrue(Precision.equals(0.0, d1.density(-1e-9), 1));
        Assert.assertTrue(Precision.equals(1.0, d1.density(0.0), 1));
        Assert.assertTrue(Precision.equals(0.0, d1.density(1000.0), 1));
        Assert.assertTrue(Precision.equals(FastMath.exp(-1), d1.density(1.0), 1));
        Assert.assertTrue(Precision.equals(FastMath.exp(-2), d1.density(2.0), 1));

        ExponentialDistribution d2 = new ExponentialDistribution(3);
        Assert.assertTrue(Precision.equals(1/3.0, d2.density(0.0), 1));
        
        Assert.assertEquals(0.2388437702, d2.density(1.0), 1e-8);

        
        Assert.assertEquals(0.1711390397, d2.density(2.0), 1e-8);
    }

// org.apache.commons.math3.distribution.ExponentialDistributionTest::testMeanAccessors
    public void testMeanAccessors() {
        ExponentialDistribution distribution = (ExponentialDistribution) getDistribution();
        Assert.assertEquals(5d, distribution.getMean(), Double.MIN_VALUE);
    }

// org.apache.commons.math3.distribution.ExponentialDistributionTest::testPreconditions
    public void testPreconditions() {
        new ExponentialDistribution(0);
    }

// org.apache.commons.math3.distribution.ExponentialDistributionTest::testMoments
    public void testMoments() {
        final double tol = 1e-9;
        ExponentialDistribution dist;

        dist = new ExponentialDistribution(11d);
        Assert.assertEquals(dist.getNumericalMean(), 11d, tol);
        Assert.assertEquals(dist.getNumericalVariance(), 11d * 11d, tol);

        dist = new ExponentialDistribution(10.5d);
        Assert.assertEquals(dist.getNumericalMean(), 10.5d, tol);
        Assert.assertEquals(dist.getNumericalVariance(), 10.5d * 10.5d, tol);
    }

// org.apache.commons.math3.distribution.FDistributionTest::testCumulativeProbabilityExtremes
    public void testCumulativeProbabilityExtremes() {
        setCumulativeTestPoints(new double[] {-2, 0});
        setCumulativeTestValues(new double[] {0, 0});
        verifyCumulativeProbabilities();
    }

// org.apache.commons.math3.distribution.FDistributionTest::testInverseCumulativeProbabilityExtremes
    public void testInverseCumulativeProbabilityExtremes() {
        setInverseCumulativeTestPoints(new double[] {0, 1});
        setInverseCumulativeTestValues(new double[] {0, Double.POSITIVE_INFINITY});
        verifyInverseCumulativeProbabilities();
    }

// org.apache.commons.math3.distribution.FDistributionTest::testDfAccessors
    public void testDfAccessors() {
        FDistribution dist = (FDistribution) getDistribution();
        Assert.assertEquals(5d, dist.getNumeratorDegreesOfFreedom(), Double.MIN_VALUE);
        Assert.assertEquals(6d, dist.getDenominatorDegreesOfFreedom(), Double.MIN_VALUE);
    }

// org.apache.commons.math3.distribution.FDistributionTest::testPreconditions
    public void testPreconditions() {
        try {
            new FDistribution(0, 1);
            Assert.fail("Expecting NotStrictlyPositiveException for df = 0");
        } catch (NotStrictlyPositiveException ex) {
            
        }
        try {
            new FDistribution(1, 0);
            Assert.fail("Expecting NotStrictlyPositiveException for df = 0");
        } catch (NotStrictlyPositiveException ex) {
            
        }
    }

// org.apache.commons.math3.distribution.FDistributionTest::testLargeDegreesOfFreedom
    public void testLargeDegreesOfFreedom() {
        FDistribution fd = new FDistribution(100000, 100000);
        double p = fd.cumulativeProbability(.999);
        double x = fd.inverseCumulativeProbability(p);
        Assert.assertEquals(.999, x, 1.0e-5);
    }

// org.apache.commons.math3.distribution.FDistributionTest::testSmallDegreesOfFreedom
    public void testSmallDegreesOfFreedom() {
        FDistribution fd = new FDistribution(1, 1);
        double p = fd.cumulativeProbability(0.975);
        double x = fd.inverseCumulativeProbability(p);
        Assert.assertEquals(0.975, x, 1.0e-5);

        fd = new FDistribution(1, 2);
        p = fd.cumulativeProbability(0.975);
        x = fd.inverseCumulativeProbability(p);
        Assert.assertEquals(0.975, x, 1.0e-5);
    }

// org.apache.commons.math3.distribution.FDistributionTest::testMoments
    public void testMoments() {
        final double tol = 1e-9;
        FDistribution dist;

        dist = new FDistribution(1, 2);
        Assert.assertTrue(Double.isNaN(dist.getNumericalMean()));
        Assert.assertTrue(Double.isNaN(dist.getNumericalVariance()));

        dist = new FDistribution(1, 3);
        Assert.assertEquals(dist.getNumericalMean(), 3d / (3d - 2d), tol);
        Assert.assertTrue(Double.isNaN(dist.getNumericalVariance()));

        dist = new FDistribution(1, 5);
        Assert.assertEquals(dist.getNumericalMean(), 5d / (5d - 2d), tol);
        Assert.assertEquals(dist.getNumericalVariance(), (2d * 5d * 5d * 4d) / 9d, tol);
    }

// org.apache.commons.math3.distribution.FDistributionTest::testMath785
    public void testMath785() {
        

        try {
            double prob = 0.01;
            FDistribution f = new FDistribution(200000, 200000);
            double result = f.inverseCumulativeProbability(prob);
            Assert.assertTrue(result < 1.0);
        } catch (Exception e) {
            Assert.fail("Failing to calculate inverse cumulative probability");
        }
    }

// org.apache.commons.math3.distribution.GammaDistributionTest::testParameterAccessors
    public void testParameterAccessors() {
        GammaDistribution distribution = (GammaDistribution) getDistribution();
        Assert.assertEquals(4d, distribution.getAlpha(), 0);
        Assert.assertEquals(2d, distribution.getBeta(), 0);
    }

// org.apache.commons.math3.distribution.GammaDistributionTest::testPreconditions
    public void testPreconditions() {
        try {
            new GammaDistribution(0, 1);
            Assert.fail("Expecting NotStrictlyPositiveException for alpha = 0");
        } catch (NotStrictlyPositiveException ex) {
            
        }
        try {
            new GammaDistribution(1, 0);
            Assert.fail("Expecting NotStrictlyPositiveException for alpha = 0");
        } catch (NotStrictlyPositiveException ex) {
            
        }
    }

// org.apache.commons.math3.distribution.GammaDistributionTest::testProbabilities
    public void testProbabilities() {
        testProbability(-1.000, 4.0, 2.0, .0000);
        testProbability(15.501, 4.0, 2.0, .9499);
        testProbability(0.504, 4.0, 1.0, .0018);
        testProbability(10.011, 1.0, 2.0, .9933);
        testProbability(5.000, 2.0, 2.0, .7127);
    }

// org.apache.commons.math3.distribution.GammaDistributionTest::testValues
    public void testValues() {
        testValue(15.501, 4.0, 2.0, .9499);
        testValue(0.504, 4.0, 1.0, .0018);
        testValue(10.011, 1.0, 2.0, .9933);
        testValue(5.000, 2.0, 2.0, .7127);
    }

// org.apache.commons.math3.distribution.GammaDistributionTest::testDensity
    public void testDensity() {
        double[] x = new double[]{-0.1, 1e-6, 0.5, 1, 2, 5};
        
        checkDensity(1, 1, x, new double[]{0.000000000000, 0.999999000001, 0.606530659713, 0.367879441171, 0.135335283237, 0.006737946999});
        
        checkDensity(2, 1, x, new double[]{0.000000000000, 0.000000999999, 0.303265329856, 0.367879441171, 0.270670566473, 0.033689734995});
        
        checkDensity(4, 1, x, new double[]{0.000000000e+00, 1.666665000e-19, 1.263605541e-02, 6.131324020e-02, 1.804470443e-01, 1.403738958e-01});
        
        checkDensity(4, 10, x, new double[]{0.000000000e+00, 1.666650000e-15, 1.403738958e+00, 7.566654960e-02, 2.748204830e-05, 4.018228850e-17});
        
        checkDensity(0.1, 10, x, new double[]{0.000000000e+00, 3.323953832e+04, 1.663849010e-03, 6.007786726e-06, 1.461647647e-10, 5.996008322e-24});
        
        checkDensity(0.1, 20, x, new double[]{0.000000000e+00, 3.562489883e+04, 1.201557345e-05, 2.923295295e-10, 3.228910843e-19, 1.239484589e-45});
        
        checkDensity(0.1, 4, x, new double[]{0.000000000e+00, 3.032938388e+04, 3.049322494e-02, 2.211502311e-03, 2.170613371e-05, 5.846590589e-11});
        
        checkDensity(0.1, 1, x, new double[]{0.000000000e+00, 2.640334143e+04, 1.189704437e-01, 3.866916944e-02, 7.623306235e-03, 1.663849010e-04});
    }

// org.apache.commons.math3.distribution.GammaDistributionTest::testInverseCumulativeProbabilityExtremes
    public void testInverseCumulativeProbabilityExtremes() {
        setInverseCumulativeTestPoints(new double[] {0, 1});
        setInverseCumulativeTestValues(new double[] {0, Double.POSITIVE_INFINITY});
        verifyInverseCumulativeProbabilities();
    }

// org.apache.commons.math3.distribution.GammaDistributionTest::testMoments
    public void testMoments() {
        final double tol = 1e-9;
        GammaDistribution dist;

        dist = new GammaDistribution(1, 2);
        Assert.assertEquals(dist.getNumericalMean(), 2, tol);
        Assert.assertEquals(dist.getNumericalVariance(), 4, tol);

        dist = new GammaDistribution(1.1, 4.2);
        Assert.assertEquals(dist.getNumericalMean(), 1.1d * 4.2d, tol);
        Assert.assertEquals(dist.getNumericalVariance(), 1.1d * 4.2d * 4.2d, tol);
    }

// org.apache.commons.math3.distribution.GammaDistributionTest::testMath753Shape1
    public void testMath753Shape1() throws IOException {
        doTestMath753(1.0, 1.5, 0.5, 0.0, 0.0, "gamma-distribution-shape-1.csv");
    }

// org.apache.commons.math3.distribution.GammaDistributionTest::testMath753Shape8
    public void testMath753Shape8() throws IOException {
        doTestMath753(8.0, 1.5, 1.0, 0.0, 0.0, "gamma-distribution-shape-8.csv");
    }

// org.apache.commons.math3.distribution.GammaDistributionTest::testMath753Shape10
    public void testMath753Shape10() throws IOException {
        doTestMath753(10.0, 1.0, 1.0, 0.0, 0.0, "gamma-distribution-shape-10.csv");
    }

// org.apache.commons.math3.distribution.GammaDistributionTest::testMath753Shape100
    public void testMath753Shape100() throws IOException {
        doTestMath753(100.0, 1.5, 1.0, 0.0, 0.0, "gamma-distribution-shape-100.csv");
    }

// org.apache.commons.math3.distribution.GammaDistributionTest::testMath753Shape142
    public void testMath753Shape142() throws IOException {
        doTestMath753(142.0, 0.5, 1.5, 40.0, 40.0, "gamma-distribution-shape-142.csv");
    }

// org.apache.commons.math3.distribution.GammaDistributionTest::testMath753Shape1000
    public void testMath753Shape1000() throws IOException {
        doTestMath753(1000.0, 1.0, 1.0, 160.0, 220.0, "gamma-distribution-shape-1000.csv");
    }

// org.apache.commons.math3.distribution.GeometricDistributionTest::testMoments
    public void testMoments() {
        final double tol = 1e-9;
        GeometricDistribution dist;

        dist = new GeometricDistribution(0.5);
        Assert.assertEquals(dist.getNumericalMean(), (1.0d - 0.5d) / 0.5d, tol);
        Assert.assertEquals(dist.getNumericalVariance(), (1.0d - 0.5d) / (0.5d * 0.5d), tol);

        dist = new GeometricDistribution(0.3);
        Assert.assertEquals(dist.getNumericalMean(), (1.0d - 0.3d) / 0.3d, tol);
        Assert.assertEquals(dist.getNumericalVariance(), (1.0d - 0.3d) / (0.3d * 0.3d), tol);
    }

// org.apache.commons.math3.distribution.HypergeometricDistributionTest::testDegenerateNoFailures
    public void testDegenerateNoFailures() {
        HypergeometricDistribution dist = new HypergeometricDistribution(5,5,3);
        setDistribution(dist);
        setCumulativeTestPoints(new int[] {-1, 0, 1, 3, 10 });
        setCumulativeTestValues(new double[] {0d, 0d, 0d, 1d, 1d});
        setDensityTestPoints(new int[] {-1, 0, 1, 3, 10});
        setDensityTestValues(new double[] {0d, 0d, 0d, 1d, 0d});
        setInverseCumulativeTestPoints(new double[] {0.1d, 0.5d});
        setInverseCumulativeTestValues(new int[] {3, 3});
        verifyDensities();
        verifyCumulativeProbabilities();
        verifyInverseCumulativeProbabilities();
        Assert.assertEquals(dist.getSupportLowerBound(), 3);
        Assert.assertEquals(dist.getSupportUpperBound(), 3);
    }

// org.apache.commons.math3.distribution.HypergeometricDistributionTest::testDegenerateNoSuccesses
    public void testDegenerateNoSuccesses() {
        HypergeometricDistribution dist = new HypergeometricDistribution(5,0,3);
        setDistribution(dist);
        setCumulativeTestPoints(new int[] {-1, 0, 1, 3, 10 });
        setCumulativeTestValues(new double[] {0d, 1d, 1d, 1d, 1d});
        setDensityTestPoints(new int[] {-1, 0, 1, 3, 10});
        setDensityTestValues(new double[] {0d, 1d, 0d, 0d, 0d});
        setInverseCumulativeTestPoints(new double[] {0.1d, 0.5d});
        setInverseCumulativeTestValues(new int[] {0, 0});
        verifyDensities();
        verifyCumulativeProbabilities();
        verifyInverseCumulativeProbabilities();
        Assert.assertEquals(dist.getSupportLowerBound(), 0);
        Assert.assertEquals(dist.getSupportUpperBound(), 0);
    }

// org.apache.commons.math3.distribution.HypergeometricDistributionTest::testDegenerateFullSample
    public void testDegenerateFullSample() {
        HypergeometricDistribution dist = new HypergeometricDistribution(5,3,5);
        setDistribution(dist);
        setCumulativeTestPoints(new int[] {-1, 0, 1, 3, 10 });
        setCumulativeTestValues(new double[] {0d, 0d, 0d, 1d, 1d});
        setDensityTestPoints(new int[] {-1, 0, 1, 3, 10});
        setDensityTestValues(new double[] {0d, 0d, 0d, 1d, 0d});
        setInverseCumulativeTestPoints(new double[] {0.1d, 0.5d});
        setInverseCumulativeTestValues(new int[] {3, 3});
        verifyDensities();
        verifyCumulativeProbabilities();
        verifyInverseCumulativeProbabilities();
        Assert.assertEquals(dist.getSupportLowerBound(), 3);
        Assert.assertEquals(dist.getSupportUpperBound(), 3);
    }

// org.apache.commons.math3.distribution.HypergeometricDistributionTest::testPreconditions
    public void testPreconditions() {
        try {
            new HypergeometricDistribution(0, 3, 5);
            Assert.fail("negative population size. NotStrictlyPositiveException expected");
        } catch(NotStrictlyPositiveException ex) {
            
        }
        try {
            new HypergeometricDistribution(5, -1, 5);
            Assert.fail("negative number of successes. NotPositiveException expected");
        } catch(NotPositiveException ex) {
            
        }
        try {
            new HypergeometricDistribution(5, 3, -1);
            Assert.fail("negative sample size. NotPositiveException expected");
        } catch(NotPositiveException ex) {
            
        }
        try {
            new HypergeometricDistribution(5, 6, 5);
            Assert.fail("numberOfSuccesses > populationSize. NumberIsTooLargeException expected");
        } catch(NumberIsTooLargeException ex) {
            
        }
        try {
            new HypergeometricDistribution(5, 3, 6);
            Assert.fail("sampleSize > populationSize. NumberIsTooLargeException expected");
        } catch(NumberIsTooLargeException ex) {
            
        }
    }

// org.apache.commons.math3.distribution.HypergeometricDistributionTest::testAccessors
    public void testAccessors() {
        HypergeometricDistribution dist = new HypergeometricDistribution(5, 3, 4);
        Assert.assertEquals(5, dist.getPopulationSize());
        Assert.assertEquals(3, dist.getNumberOfSuccesses());
        Assert.assertEquals(4, dist.getSampleSize());
    }

// org.apache.commons.math3.distribution.HypergeometricDistributionTest::testLargeValues
    public void testLargeValues() {
        int populationSize = 3456;
        int sampleSize = 789;
        int numberOfSucceses = 101;
        double[][] data = {
            {0.0, 2.75646034603961e-12, 2.75646034603961e-12, 1.0},
            {1.0, 8.55705370142386e-11, 8.83269973602783e-11, 0.999999999997244},
            {2.0, 1.31288129219665e-9, 1.40120828955693e-9, 0.999999999911673},
            {3.0, 1.32724172984193e-8, 1.46736255879763e-8, 0.999999998598792},
            {4.0, 9.94501711734089e-8, 1.14123796761385e-7, 0.999999985326375},
            {5.0, 5.89080768883643e-7, 7.03204565645028e-7, 0.999999885876203},
            {20.0, 0.0760051397707708, 0.27349758476299, 0.802507555007781},
            {21.0, 0.087144222047629, 0.360641806810619, 0.72650241523701},
            {22.0, 0.0940378846881819, 0.454679691498801, 0.639358193189381},
            {23.0, 0.0956897500614809, 0.550369441560282, 0.545320308501199},
            {24.0, 0.0919766921922999, 0.642346133752582, 0.449630558439718},
            {25.0, 0.083641637261095, 0.725987771013677, 0.357653866247418},
            {96.0, 5.93849188852098e-57, 1.0, 6.01900244560712e-57},
            {97.0, 7.96593036832547e-59, 1.0, 8.05105570861321e-59},
            {98.0, 8.44582921934367e-61, 1.0, 8.5125340287733e-61},
            {99.0, 6.63604297068222e-63, 1.0, 6.670480942963e-63},
            {100.0, 3.43501099007557e-65, 1.0, 3.4437972280786e-65},
            {101.0, 8.78623800302957e-68, 1.0, 8.78623800302957e-68},
        };

        testHypergeometricDistributionProbabilities(populationSize, sampleSize, numberOfSucceses, data);
    }

// org.apache.commons.math3.distribution.HypergeometricDistributionTest::testMoreLargeValues
    public void testMoreLargeValues() {
        int populationSize = 26896;
        int sampleSize = 895;
        int numberOfSucceses = 55;
        double[][] data = {
            {0.0, 0.155168304750504, 0.155168304750504, 1.0},
            {1.0, 0.29437545000746, 0.449543754757964, 0.844831695249496},
            {2.0, 0.273841321577003, 0.723385076334967, 0.550456245242036},
            {3.0, 0.166488572570786, 0.889873648905753, 0.276614923665033},
            {4.0, 0.0743969744713231, 0.964270623377076, 0.110126351094247},
            {5.0, 0.0260542785784855, 0.990324901955562, 0.0357293766229237},
            {20.0, 3.57101101678792e-16, 1.0, 3.78252101622096e-16},
            {21.0, 2.00551638598312e-17, 1.0, 2.11509999433041e-17},
            {22.0, 1.04317070180562e-18, 1.0, 1.09583608347287e-18},
            {23.0, 5.03153504903308e-20, 1.0, 5.266538166725e-20},
            {24.0, 2.2525984149695e-21, 1.0, 2.35003117691919e-21},
            {25.0, 9.3677424515947e-23, 1.0, 9.74327619496943e-23},
            {50.0, 9.83633962945521e-69, 1.0, 9.8677629437617e-69},
            {51.0, 3.13448949497553e-71, 1.0, 3.14233143064882e-71},
            {52.0, 7.82755221928122e-74, 1.0, 7.84193567329055e-74},
            {53.0, 1.43662126065532e-76, 1.0, 1.43834540093295e-76},
            {54.0, 1.72312692517348e-79, 1.0, 1.7241402776278e-79},
            {55.0, 1.01335245432581e-82, 1.0, 1.01335245432581e-82},
        };
        testHypergeometricDistributionProbabilities(populationSize, sampleSize, numberOfSucceses, data);
    }

// org.apache.commons.math3.distribution.HypergeometricDistributionTest::testMoments
    public void testMoments() {
        final double tol = 1e-9;
        HypergeometricDistribution dist;

        dist = new HypergeometricDistribution(1500, 40, 100);
        Assert.assertEquals(dist.getNumericalMean(), 40d * 100d / 1500d, tol);
        Assert.assertEquals(dist.getNumericalVariance(), ( 100d * 40d * (1500d - 100d) * (1500d - 40d) ) / ( (1500d * 1500d * 1499d) ), tol);

        dist = new HypergeometricDistribution(3000, 55, 200);
        Assert.assertEquals(dist.getNumericalMean(), 55d * 200d / 3000d, tol);
        Assert.assertEquals(dist.getNumericalVariance(), ( 200d * 55d * (3000d - 200d) * (3000d - 55d) ) / ( (3000d * 3000d * 2999d) ), tol);
    }

// org.apache.commons.math3.distribution.HypergeometricDistributionTest::testMath644
    public void testMath644() {
        int N = 14761461;  
        int m = 1035;      
        int n = 1841;      

        int k = 0;
        final HypergeometricDistribution dist = new HypergeometricDistribution(N, m, n);
        
        Assert.assertTrue(Precision.compareTo(1.0, dist.upperCumulativeProbability(k), 1) == 0);
        Assert.assertTrue(Precision.compareTo(dist.cumulativeProbability(k), 0.0, 1) > 0);
        
        
        double upper = 1.0 - dist.cumulativeProbability(k) + dist.probability(k);
        Assert.assertTrue(Precision.compareTo(1.0, upper, 1) == 0);
    }

// org.apache.commons.math3.distribution.KolmogorovSmirnovDistributionTest::testCumulativeDensityFunction
    public void testCumulativeDensityFunction() {
        
        KolmogorovSmirnovDistribution dist;
        
        
        
        

        
        dist = new KolmogorovSmirnovDistribution(200);
        Assert.assertEquals(4.907829957616471622388047046469198862537e-86, dist.cdf(0.005, false), TOLERANCE);

        
        dist = new KolmogorovSmirnovDistribution(200);
        Assert.assertEquals(5.151982014280041957199687829849210629618e-06, dist.cdf(0.02, false), TOLERANCE);

        
        dist = new KolmogorovSmirnovDistribution(200);
        Assert.assertEquals(0.01291614648162886340443389343590752105229, dist.cdf(0.031111, false), TOLERANCE);

        
        dist = new KolmogorovSmirnovDistribution(200);
        Assert.assertEquals(0.1067137011362679355208626930107129737735, dist.cdf(0.04, false), TOLERANCE);

        
        dist = new KolmogorovSmirnovDistribution(341);
        Assert.assertEquals(1.914734701559404553985102395145063418825e-53, dist.cdf(0.005, false), TOLERANCE);

        
        dist = new KolmogorovSmirnovDistribution(341);
        Assert.assertEquals(0.001171328985781981343872182321774744195864, dist.cdf(0.02, false), TOLERANCE);

        
        dist = new KolmogorovSmirnovDistribution(341);
        Assert.assertEquals(0.1142955196267499418105728636874118819833, dist.cdf(0.031111, false), TOLERANCE);

        
        dist = new KolmogorovSmirnovDistribution(341);
        Assert.assertEquals(0.3685529520496805266915885113121476024389, dist.cdf(0.04, false), TOLERANCE);

        
        dist = new KolmogorovSmirnovDistribution(389);
        Assert.assertEquals(1.810657144595055888918455512707637574637e-47, dist.cdf(0.005, false), TOLERANCE);

        
        dist = new KolmogorovSmirnovDistribution(389);
        Assert.assertEquals(0.003068542559702356568168690742481885536108, dist.cdf(0.02, false), TOLERANCE);

        
        dist = new KolmogorovSmirnovDistribution(389);
        Assert.assertEquals(0.1658291700122746237244797384846606291831, dist.cdf(0.031111, false), TOLERANCE);

        
        dist = new KolmogorovSmirnovDistribution(389);
        Assert.assertEquals(0.4513143712128902529379104180407011881471, dist.cdf(0.04, false), TOLERANCE);

    }

// org.apache.commons.math3.distribution.LevyDistributionTest::testParameters
    public void testParameters() {
        LevyDistribution d = makeDistribution();
        Assert.assertEquals(1.2, d.getLocation(), Precision.EPSILON);
        Assert.assertEquals(0.4,   d.getScale(),  Precision.EPSILON);
    }

// org.apache.commons.math3.distribution.LevyDistributionTest::testSupport
    public void testSupport() {
        LevyDistribution d = makeDistribution();
        Assert.assertEquals(d.getLocation(), d.getSupportLowerBound(), Precision.EPSILON);
        Assert.assertTrue(Double.isInfinite(d.getSupportUpperBound()));
        Assert.assertTrue(d.isSupportConnected());
    }

// org.apache.commons.math3.distribution.LogNormalDistributionTest::testQuantiles
    public void testQuantiles() {
        setCumulativeTestValues(new double[] {0, 0.0396495152787,
                                              0.16601209243, 0.272533253269,
                                              0.357618409638, 0.426488363093,
                                              0.483255136841, 0.530823013877});
        setDensityTestValues(new double[] {0, 0.0873055825147, 0.0847676303432,
                                           0.0677935186237, 0.0544105523058,
                                           0.0444614628804, 0.0369750288945,
                                           0.0312206409653});
        verifyQuantiles();
        verifyDensities();

        setDistribution(new LogNormalDistribution(0, 1));
        setCumulativeTestValues(new double[] {0, 0, 0, 0.5, 0.755891404214,
                                              0.864031392359, 0.917171480998,
                                              0.946239689548});
        setDensityTestValues(new double[] {0, 0, 0, 0.398942280401,
                                           0.156874019279, 0.07272825614,
                                           0.0381534565119, 0.0218507148303});
        verifyQuantiles();
        verifyDensities();

        setDistribution(new LogNormalDistribution(0, 0.1));
        setCumulativeTestValues(new double[] {0, 0, 0, 1.28417563064e-117,
                                              1.39679883412e-58,
                                              1.09839325447e-33,
                                              2.52587961726e-20,
                                              2.0824223487e-12});
        setDensityTestValues(new double[] {0, 0, 0, 2.96247992535e-114,
                                           1.1283370232e-55, 4.43812313223e-31,
                                           5.85346445002e-18,
                                           2.9446618076e-10});
        verifyQuantiles();
        verifyDensities();
    }

// org.apache.commons.math3.distribution.LogNormalDistributionTest::testInverseCumulativeProbabilityExtremes
    public void testInverseCumulativeProbabilityExtremes() {
        setInverseCumulativeTestPoints(new double[] {0, 1});
        setInverseCumulativeTestValues(
                new double[] {0, Double.POSITIVE_INFINITY});
        verifyInverseCumulativeProbabilities();
    }

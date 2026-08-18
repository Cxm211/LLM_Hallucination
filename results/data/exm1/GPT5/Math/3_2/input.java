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
// org.apache.commons.math3.linear.ConjugateGradientTest::testMismatchedOperatorDimensions
    public void testMismatchedOperatorDimensions() {
        final Array2DRowRealMatrix a = new Array2DRowRealMatrix(2, 2);
        final RealLinearOperator m = new RealLinearOperator() {

            @Override
            public RealVector operate(final RealVector x) {
                throw new UnsupportedOperationException();
            }

            @Override
            public int getRowDimension() {
                return 3;
            }

            @Override
            public int getColumnDimension() {
                return 3;
            }
        };
        final PreconditionedIterativeLinearSolver solver;
        solver = new ConjugateGradient(10, 0d, false);
        final ArrayRealVector b = new ArrayRealVector(a.getRowDimension());
        solver.solve(a, m, b);
    }

// org.apache.commons.math3.linear.ConjugateGradientTest::testNonPositiveDefinitePreconditioner
    public void testNonPositiveDefinitePreconditioner() {
        final Array2DRowRealMatrix a = new Array2DRowRealMatrix(2, 2);
        a.setEntry(0, 0, 1d);
        a.setEntry(0, 1, 2d);
        a.setEntry(1, 0, 3d);
        a.setEntry(1, 1, 4d);
        final RealLinearOperator m = new RealLinearOperator() {

            @Override
            public RealVector operate(final RealVector x) {
                final ArrayRealVector y = new ArrayRealVector(2);
                y.setEntry(0, -x.getEntry(0));
                y.setEntry(1, x.getEntry(1));
                return y;
            }

            @Override
            public int getRowDimension() {
                return 2;
            }

            @Override
            public int getColumnDimension() {
                return 2;
            }
        };
        final PreconditionedIterativeLinearSolver solver;
        solver = new ConjugateGradient(10, 0d, true);
        final ArrayRealVector b = new ArrayRealVector(2);
        b.setEntry(0, -1d);
        b.setEntry(1, -1d);
        solver.solve(a, m, b);
    }

// org.apache.commons.math3.linear.ConjugateGradientTest::testPreconditionedSolution
    public void testPreconditionedSolution() {
        final int n = 8;
        final int maxIterations = 100;
        final RealLinearOperator a = new HilbertMatrix(n);
        final InverseHilbertMatrix ainv = new InverseHilbertMatrix(n);
        final RealLinearOperator m = JacobiPreconditioner.create(a);
        final PreconditionedIterativeLinearSolver solver;
        solver = new ConjugateGradient(maxIterations, 1E-15, true);
        final RealVector b = new ArrayRealVector(n);
        for (int j = 0; j < n; j++) {
            b.set(0.);
            b.setEntry(j, 1.);
            final RealVector x = solver.solve(a, m, b);
            for (int i = 0; i < n; i++) {
                final double actual = x.getEntry(i);
                final double expected = ainv.getEntry(i, j);
                final double delta = 1E-6 * Math.abs(expected);
                final String msg = String.format("coefficient (%d, %d)", i, j);
                Assert.assertEquals(msg, expected, actual, delta);
            }
        }
    }

// org.apache.commons.math3.linear.ConjugateGradientTest::testPreconditionedResidual
    public void testPreconditionedResidual() {
        final int n = 10;
        final int maxIterations = n;
        final RealLinearOperator a = new HilbertMatrix(n);
        final RealLinearOperator m = JacobiPreconditioner.create(a);
        final ConjugateGradient solver;
        solver = new ConjugateGradient(maxIterations, 1E-15, true);
        final RealVector r = new ArrayRealVector(n);
        final RealVector x = new ArrayRealVector(n);
        final IterationListener listener = new IterationListener() {

            public void terminationPerformed(final IterationEvent e) {
                
            }

            public void iterationStarted(final IterationEvent e) {
                
            }

            public void iterationPerformed(final IterationEvent e) {
                final IterativeLinearSolverEvent evt;
                evt = (IterativeLinearSolverEvent) e;
                RealVector v = evt.getResidual();
                r.setSubVector(0, v);
                v = evt.getSolution();
                x.setSubVector(0, v);
            }

            public void initializationPerformed(final IterationEvent e) {
                
            }
        };
        solver.getIterationManager().addIterationListener(listener);
        final RealVector b = new ArrayRealVector(n);

        for (int j = 0; j < n; j++) {
            b.set(0.);
            b.setEntry(j, 1.);

            boolean caught = false;
            try {
                solver.solve(a, m, b);
            } catch (MaxCountExceededException e) {
                caught = true;
                final RealVector y = a.operate(x);
                for (int i = 0; i < n; i++) {
                    final double actual = b.getEntry(i) - y.getEntry(i);
                    final double expected = r.getEntry(i);
                    final double delta = 1E-6 * Math.abs(expected);
                    final String msg = String
                        .format("column %d, residual %d", i, j);
                    Assert.assertEquals(msg, expected, actual, delta);
                }
            }
            Assert
                .assertTrue("MaxCountExceededException should have been caught",
                            caught);
        }
    }

// org.apache.commons.math3.linear.ConjugateGradientTest::testPreconditionedSolution2
    public void testPreconditionedSolution2() {
        final int n = 100;
        final int maxIterations = 100000;
        final Array2DRowRealMatrix a = new Array2DRowRealMatrix(n, n);
        double daux = 1.;
        for (int i = 0; i < n; i++) {
            a.setEntry(i, i, daux);
            daux *= 1.2;
            for (int j = i + 1; j < n; j++) {
                if (i == j) {
                } else {
                    final double value = 1.0;
                    a.setEntry(i, j, value);
                    a.setEntry(j, i, value);
                }
            }
        }
        final RealLinearOperator m = JacobiPreconditioner.create(a);
        final PreconditionedIterativeLinearSolver pcg;
        final IterativeLinearSolver cg;
        pcg = new ConjugateGradient(maxIterations, 1E-6, true);
        cg = new ConjugateGradient(maxIterations, 1E-6, true);
        final RealVector b = new ArrayRealVector(n);
        final String pattern = "preconditioned gradient (%d iterations) should"
                               + " have been faster than unpreconditioned (%d iterations)";
        String msg;
        for (int j = 0; j < 1; j++) {
            b.set(0.);
            b.setEntry(j, 1.);
            final RealVector px = pcg.solve(a, m, b);
            final RealVector x = cg.solve(a, b);
            final int npcg = pcg.getIterationManager().getIterations();
            final int ncg = cg.getIterationManager().getIterations();
            msg = String.format(pattern, npcg, ncg);
            Assert.assertTrue(msg, npcg < ncg);
            for (int i = 0; i < n; i++) {
                msg = String.format("row %d, column %d", i, j);
                final double expected = x.getEntry(i);
                final double actual = px.getEntry(i);
                final double delta = 1E-6 * Math.abs(expected);
                Assert.assertEquals(msg, expected, actual, delta);
            }
        }
    }

// org.apache.commons.math3.linear.ConjugateGradientTest::testEventManagement
    public void testEventManagement() {
        final int n = 5;
        final int maxIterations = 100;
        final RealLinearOperator a = new HilbertMatrix(n);
        final IterativeLinearSolver solver;
        
        final int[] count = new int[] {0, 0, 0, 0};
        final IterationListener listener = new IterationListener() {
            private void doTestVectorsAreUnmodifiable(final IterationEvent e) {
                final IterativeLinearSolverEvent evt;
                evt = (IterativeLinearSolverEvent) e;
                try {
                    evt.getResidual().set(0.0);
                    Assert.fail("r is modifiable");
                } catch (MathUnsupportedOperationException exc){
                    
                }
                try {
                    evt.getRightHandSideVector().set(0.0);
                    Assert.fail("b is modifiable");
                } catch (MathUnsupportedOperationException exc){
                    
                }
                try {
                    evt.getSolution().set(0.0);
                    Assert.fail("x is modifiable");
                } catch (MathUnsupportedOperationException exc){
                    
                }
            }

            public void initializationPerformed(final IterationEvent e) {
                ++count[0];
                doTestVectorsAreUnmodifiable(e);
            }

            public void iterationPerformed(final IterationEvent e) {
                ++count[2];
                Assert.assertEquals("iteration performed",
                    count[2], e.getIterations() - 1);
                doTestVectorsAreUnmodifiable(e);
            }

            public void iterationStarted(final IterationEvent e) {
                ++count[1];
                Assert.assertEquals("iteration started",
                    count[1], e.getIterations() - 1);
                doTestVectorsAreUnmodifiable(e);
            }

            public void terminationPerformed(final IterationEvent e) {
                ++count[3];
                doTestVectorsAreUnmodifiable(e);
            }
        };
        solver = new ConjugateGradient(maxIterations, 1E-10, true);
        solver.getIterationManager().addIterationListener(listener);
        final RealVector b = new ArrayRealVector(n);
        for (int j = 0; j < n; j++) {
            Arrays.fill(count, 0);
            b.set(0.);
            b.setEntry(j, 1.);
            solver.solve(a, b);
            String msg = String.format("column %d (initialization)", j);
            Assert.assertEquals(msg, 1, count[0]);
            msg = String.format("column %d (finalization)", j);
            Assert.assertEquals(msg, 1, count[3]);
        }
    }

// org.apache.commons.math3.linear.ConjugateGradientTest::testUnpreconditionedNormOfResidual
    public void testUnpreconditionedNormOfResidual() {
        final int n = 5;
        final int maxIterations = 100;
        final RealLinearOperator a = new HilbertMatrix(n);
        final IterativeLinearSolver solver;
        final IterationListener listener = new IterationListener() {

            private void doTestNormOfResidual(final IterationEvent e) {
                final IterativeLinearSolverEvent evt;
                evt = (IterativeLinearSolverEvent) e;
                final RealVector x = evt.getSolution();
                final RealVector b = evt.getRightHandSideVector();
                final RealVector r = b.subtract(a.operate(x));
                final double rnorm = r.getNorm();
                Assert.assertEquals("iteration performed (residual)",
                    rnorm, evt.getNormOfResidual(),
                    FastMath.max(1E-5 * rnorm, 1E-10));
            }

            public void initializationPerformed(final IterationEvent e) {
                doTestNormOfResidual(e);
            }

            public void iterationPerformed(final IterationEvent e) {
                doTestNormOfResidual(e);
            }

            public void iterationStarted(final IterationEvent e) {
                doTestNormOfResidual(e);
            }

            public void terminationPerformed(final IterationEvent e) {
                doTestNormOfResidual(e);
            }
        };
        solver = new ConjugateGradient(maxIterations, 1E-10, true);
        solver.getIterationManager().addIterationListener(listener);
        final RealVector b = new ArrayRealVector(n);
        for (int j = 0; j < n; j++) {
            b.set(0.);
            b.setEntry(j, 1.);
            solver.solve(a, b);
        }
    }

// org.apache.commons.math3.linear.ConjugateGradientTest::testPreconditionedNormOfResidual
    public void testPreconditionedNormOfResidual() {
        final int n = 5;
        final int maxIterations = 100;
        final RealLinearOperator a = new HilbertMatrix(n);
        final RealLinearOperator m = JacobiPreconditioner.create(a);
        final PreconditionedIterativeLinearSolver solver;
        final IterationListener listener = new IterationListener() {

            private void doTestNormOfResidual(final IterationEvent e) {
                final IterativeLinearSolverEvent evt;
                evt = (IterativeLinearSolverEvent) e;
                final RealVector x = evt.getSolution();
                final RealVector b = evt.getRightHandSideVector();
                final RealVector r = b.subtract(a.operate(x));
                final double rnorm = r.getNorm();
                Assert.assertEquals("iteration performed (residual)",
                    rnorm, evt.getNormOfResidual(),
                    FastMath.max(1E-5 * rnorm, 1E-10));
            }

            public void initializationPerformed(final IterationEvent e) {
                doTestNormOfResidual(e);
            }

            public void iterationPerformed(final IterationEvent e) {
                doTestNormOfResidual(e);
            }

            public void iterationStarted(final IterationEvent e) {
                doTestNormOfResidual(e);
            }

            public void terminationPerformed(final IterationEvent e) {
                doTestNormOfResidual(e);
            }
        };
        solver = new ConjugateGradient(maxIterations, 1E-10, true);
        solver.getIterationManager().addIterationListener(listener);
        final RealVector b = new ArrayRealVector(n);
        for (int j = 0; j < n; j++) {
            b.set(0.);
            b.setEntry(j, 1.);
            solver.solve(a, m, b);
        }
    }

// org.apache.commons.math3.linear.FieldLUDecompositionTest::testDimensions
    public void testDimensions() {
        FieldMatrix<Fraction> matrix =
            new Array2DRowFieldMatrix<Fraction>(FractionField.getInstance(), testData);
        FieldLUDecomposition<Fraction> LU = new FieldLUDecomposition<Fraction>(matrix);
        Assert.assertEquals(testData.length, LU.getL().getRowDimension());
        Assert.assertEquals(testData.length, LU.getL().getColumnDimension());
        Assert.assertEquals(testData.length, LU.getU().getRowDimension());
        Assert.assertEquals(testData.length, LU.getU().getColumnDimension());
        Assert.assertEquals(testData.length, LU.getP().getRowDimension());
        Assert.assertEquals(testData.length, LU.getP().getColumnDimension());

    }

// org.apache.commons.math3.linear.FieldLUDecompositionTest::testNonSquare
    public void testNonSquare() {
        try {
            
            new FieldLUDecomposition<Fraction>(new Array2DRowFieldMatrix<Fraction>(new Fraction[][] {
                    { Fraction.ZERO, Fraction.ZERO },
                    { Fraction.ZERO, Fraction.ZERO },
                    { Fraction.ZERO, Fraction.ZERO }
            }));
            Assert.fail("Expected NonSquareMatrixException");
        } catch (NonSquareMatrixException ime) {
            
        }
    }

// org.apache.commons.math3.linear.FieldLUDecompositionTest::testPAEqualLU
    public void testPAEqualLU() {
        FieldMatrix<Fraction> matrix = new Array2DRowFieldMatrix<Fraction>(FractionField.getInstance(), testData);
        FieldLUDecomposition<Fraction> lu = new FieldLUDecomposition<Fraction>(matrix);
        FieldMatrix<Fraction> l = lu.getL();
        FieldMatrix<Fraction> u = lu.getU();
        FieldMatrix<Fraction> p = lu.getP();
        TestUtils.assertEquals(p.multiply(matrix), l.multiply(u));

        matrix = new Array2DRowFieldMatrix<Fraction>(FractionField.getInstance(), testDataMinus);
        lu = new FieldLUDecomposition<Fraction>(matrix);
        l = lu.getL();
        u = lu.getU();
        p = lu.getP();
        TestUtils.assertEquals(p.multiply(matrix), l.multiply(u));

        matrix = new Array2DRowFieldMatrix<Fraction>(FractionField.getInstance(), 17, 17);
        for (int i = 0; i < matrix.getRowDimension(); ++i) {
            matrix.setEntry(i, i, Fraction.ONE);
        }
        lu = new FieldLUDecomposition<Fraction>(matrix);
        l = lu.getL();
        u = lu.getU();
        p = lu.getP();
        TestUtils.assertEquals(p.multiply(matrix), l.multiply(u));

        matrix = new Array2DRowFieldMatrix<Fraction>(FractionField.getInstance(), singular);
        lu = new FieldLUDecomposition<Fraction>(matrix);
        Assert.assertFalse(lu.getSolver().isNonSingular());
        Assert.assertNull(lu.getL());
        Assert.assertNull(lu.getU());
        Assert.assertNull(lu.getP());

        matrix = new Array2DRowFieldMatrix<Fraction>(FractionField.getInstance(), bigSingular);
        lu = new FieldLUDecomposition<Fraction>(matrix);
        Assert.assertFalse(lu.getSolver().isNonSingular());
        Assert.assertNull(lu.getL());
        Assert.assertNull(lu.getU());
        Assert.assertNull(lu.getP());

    }

// org.apache.commons.math3.linear.FieldLUDecompositionTest::testLLowerTriangular
    public void testLLowerTriangular() {
        FieldMatrix<Fraction> matrix = new Array2DRowFieldMatrix<Fraction>(FractionField.getInstance(), testData);
        FieldMatrix<Fraction> l = new FieldLUDecomposition<Fraction>(matrix).getL();
        for (int i = 0; i < l.getRowDimension(); i++) {
            Assert.assertEquals(Fraction.ONE, l.getEntry(i, i));
            for (int j = i + 1; j < l.getColumnDimension(); j++) {
                Assert.assertEquals(Fraction.ZERO, l.getEntry(i, j));
            }
        }
    }

// org.apache.commons.math3.linear.FieldLUDecompositionTest::testUUpperTriangular
    public void testUUpperTriangular() {
        FieldMatrix<Fraction> matrix = new Array2DRowFieldMatrix<Fraction>(FractionField.getInstance(), testData);
        FieldMatrix<Fraction> u = new FieldLUDecomposition<Fraction>(matrix).getU();
        for (int i = 0; i < u.getRowDimension(); i++) {
            for (int j = 0; j < i; j++) {
                Assert.assertEquals(Fraction.ZERO, u.getEntry(i, j));
            }
        }
    }

// org.apache.commons.math3.linear.FieldLUDecompositionTest::testPPermutation
    public void testPPermutation() {
        FieldMatrix<Fraction> matrix = new Array2DRowFieldMatrix<Fraction>(FractionField.getInstance(), testData);
        FieldMatrix<Fraction> p   = new FieldLUDecomposition<Fraction>(matrix).getP();

        FieldMatrix<Fraction> ppT = p.multiply(p.transpose());
        FieldMatrix<Fraction> id  =
            new Array2DRowFieldMatrix<Fraction>(FractionField.getInstance(),
                                          p.getRowDimension(), p.getRowDimension());
        for (int i = 0; i < id.getRowDimension(); ++i) {
            id.setEntry(i, i, Fraction.ONE);
        }
        TestUtils.assertEquals(id, ppT);

        for (int i = 0; i < p.getRowDimension(); i++) {
            int zeroCount  = 0;
            int oneCount   = 0;
            int otherCount = 0;
            for (int j = 0; j < p.getColumnDimension(); j++) {
                final Fraction e = p.getEntry(i, j);
                if (e.equals(Fraction.ZERO)) {
                    ++zeroCount;
                } else if (e.equals(Fraction.ONE)) {
                    ++oneCount;
                } else {
                    ++otherCount;
                }
            }
            Assert.assertEquals(p.getColumnDimension() - 1, zeroCount);
            Assert.assertEquals(1, oneCount);
            Assert.assertEquals(0, otherCount);
        }

        for (int j = 0; j < p.getColumnDimension(); j++) {
            int zeroCount  = 0;
            int oneCount   = 0;
            int otherCount = 0;
            for (int i = 0; i < p.getRowDimension(); i++) {
                final Fraction e = p.getEntry(i, j);
                if (e.equals(Fraction.ZERO)) {
                    ++zeroCount;
                } else if (e.equals(Fraction.ONE)) {
                    ++oneCount;
                } else {
                    ++otherCount;
                }
            }
            Assert.assertEquals(p.getRowDimension() - 1, zeroCount);
            Assert.assertEquals(1, oneCount);
            Assert.assertEquals(0, otherCount);
        }

    }

// org.apache.commons.math3.linear.FieldLUDecompositionTest::testSingular
    public void testSingular() {
        FieldLUDecomposition<Fraction> lu =
            new FieldLUDecomposition<Fraction>(new Array2DRowFieldMatrix<Fraction>(FractionField.getInstance(), testData));
        Assert.assertTrue(lu.getSolver().isNonSingular());
        lu = new FieldLUDecomposition<Fraction>(new Array2DRowFieldMatrix<Fraction>(FractionField.getInstance(), singular));
        Assert.assertFalse(lu.getSolver().isNonSingular());
        lu = new FieldLUDecomposition<Fraction>(new Array2DRowFieldMatrix<Fraction>(FractionField.getInstance(), bigSingular));
        Assert.assertFalse(lu.getSolver().isNonSingular());
    }

// org.apache.commons.math3.linear.FieldLUDecompositionTest::testMatricesValues1
    public void testMatricesValues1() {
       FieldLUDecomposition<Fraction> lu =
            new FieldLUDecomposition<Fraction>(new Array2DRowFieldMatrix<Fraction>(FractionField.getInstance(), testData));
        FieldMatrix<Fraction> lRef = new Array2DRowFieldMatrix<Fraction>(FractionField.getInstance(), new Fraction[][] {
                { new Fraction(1), new Fraction(0), new Fraction(0) },
                { new Fraction(2), new Fraction(1), new Fraction(0) },
                { new Fraction(1), new Fraction(-2), new Fraction(1) }
        });
        FieldMatrix<Fraction> uRef = new Array2DRowFieldMatrix<Fraction>(FractionField.getInstance(), new Fraction[][] {
                { new Fraction(1),  new Fraction(2), new Fraction(3) },
                { new Fraction(0), new Fraction(1), new Fraction(-3) },
                { new Fraction(0),  new Fraction(0), new Fraction(-1) }
        });
        FieldMatrix<Fraction> pRef = new Array2DRowFieldMatrix<Fraction>(FractionField.getInstance(), new Fraction[][] {
                { new Fraction(1), new Fraction(0), new Fraction(0) },
                { new Fraction(0), new Fraction(1), new Fraction(0) },
                { new Fraction(0), new Fraction(0), new Fraction(1) }
        });
        int[] pivotRef = { 0, 1, 2 };

        
        FieldMatrix<Fraction> l = lu.getL();
        TestUtils.assertEquals(lRef, l);
        FieldMatrix<Fraction> u = lu.getU();
        TestUtils.assertEquals(uRef, u);
        FieldMatrix<Fraction> p = lu.getP();
        TestUtils.assertEquals(pRef, p);
        int[] pivot = lu.getPivot();
        for (int i = 0; i < pivotRef.length; ++i) {
            Assert.assertEquals(pivotRef[i], pivot[i]);
        }

        
        Assert.assertTrue(l == lu.getL());
        Assert.assertTrue(u == lu.getU());
        Assert.assertTrue(p == lu.getP());

    }

// org.apache.commons.math3.linear.FieldLUDecompositionTest::testMatricesValues2
    public void testMatricesValues2() {
       FieldLUDecomposition<Fraction> lu =
            new FieldLUDecomposition<Fraction>(new Array2DRowFieldMatrix<Fraction>(FractionField.getInstance(), luData));
        FieldMatrix<Fraction> lRef = new Array2DRowFieldMatrix<Fraction>(FractionField.getInstance(), new Fraction[][] {
                { new Fraction(1), new Fraction(0), new Fraction(0) },
                { new Fraction(3), new Fraction(1), new Fraction(0) },
                { new Fraction(1), new Fraction(0), new Fraction(1) }
        });
        FieldMatrix<Fraction> uRef = new Array2DRowFieldMatrix<Fraction>(FractionField.getInstance(), new Fraction[][] {
                { new Fraction(2), new Fraction(3), new Fraction(3)    },
                { new Fraction(0), new Fraction(-3), new Fraction(-1)  },
                { new Fraction(0), new Fraction(0), new Fraction(4) }
        });
        FieldMatrix<Fraction> pRef = new Array2DRowFieldMatrix<Fraction>(FractionField.getInstance(), new Fraction[][] {
                { new Fraction(1), new Fraction(0), new Fraction(0) },
                { new Fraction(0), new Fraction(0), new Fraction(1) },
                { new Fraction(0), new Fraction(1), new Fraction(0) }
        });
        int[] pivotRef = { 0, 2, 1 };

        
        FieldMatrix<Fraction> l = lu.getL();
        TestUtils.assertEquals(lRef, l);
        FieldMatrix<Fraction> u = lu.getU();
        TestUtils.assertEquals(uRef, u);
        FieldMatrix<Fraction> p = lu.getP();
        TestUtils.assertEquals(pRef, p);
        int[] pivot = lu.getPivot();
        for (int i = 0; i < pivotRef.length; ++i) {
            Assert.assertEquals(pivotRef[i], pivot[i]);
        }

        
        Assert.assertTrue(l == lu.getL());
        Assert.assertTrue(u == lu.getU());
        Assert.assertTrue(p == lu.getP());
    }

// org.apache.commons.math3.linear.FieldLUSolverTest::testSingular
    public void testSingular() {
        FieldDecompositionSolver<Fraction> solver;
        solver = new FieldLUDecomposition<Fraction>(createFractionMatrix(testData))
            .getSolver();
        Assert.assertTrue(solver.isNonSingular());
        solver = new FieldLUDecomposition<Fraction>(createFractionMatrix(singular))
            .getSolver();
        Assert.assertFalse(solver.isNonSingular());
        solver = new FieldLUDecomposition<Fraction>(createFractionMatrix(bigSingular))
            .getSolver();
        Assert.assertFalse(solver.isNonSingular());
    }

// org.apache.commons.math3.linear.FieldLUSolverTest::testSolveDimensionErrors
    public void testSolveDimensionErrors() {
        FieldDecompositionSolver<Fraction> solver;
        solver = new FieldLUDecomposition<Fraction>(createFractionMatrix(testData))
            .getSolver();
        FieldMatrix<Fraction> b = createFractionMatrix(new int[2][2]);
        try {
            solver.solve(b);
            Assert.fail("an exception should have been thrown");
        } catch (MathIllegalArgumentException iae) {
            
        }
        try {
            solver.solve(b.getColumnVector(0));
            Assert.fail("an exception should have been thrown");
        } catch (MathIllegalArgumentException iae) {
            
        }
    }

// org.apache.commons.math3.linear.FieldLUSolverTest::testSolveSingularityErrors
    public void testSolveSingularityErrors() {
        FieldDecompositionSolver<Fraction> solver;
        solver = new FieldLUDecomposition<Fraction>(createFractionMatrix(singular))
            .getSolver();
        FieldMatrix<Fraction> b = createFractionMatrix(new int[2][2]);
        try {
            solver.solve(b);
            Assert.fail("an exception should have been thrown");
        } catch (SingularMatrixException ime) {
            
        }
        try {
            solver.solve(b.getColumnVector(0));
            Assert.fail("an exception should have been thrown");
        } catch (SingularMatrixException ime) {
            
        }
    }

// org.apache.commons.math3.linear.FieldLUSolverTest::testSolve
    public void testSolve() {
        FieldDecompositionSolver<Fraction> solver;
        solver = new FieldLUDecomposition<Fraction>(createFractionMatrix(testData))
            .getSolver();
        FieldMatrix<Fraction> b = createFractionMatrix(new int[][] {
                { 1, 0 }, { 2, -5 }, { 3, 1 }
        });
        FieldMatrix<Fraction> xRef = createFractionMatrix(new int[][] {
                { 19, -71 }, { -6, 22 }, { -2, 9 }
        });

        
        FieldMatrix<Fraction> x = solver.solve(b);
        for (int i = 0; i < x.getRowDimension(); i++){
            for (int j = 0; j < x.getColumnDimension(); j++){
                Assert.assertEquals("(" + i + ", " + j + ")",
                                    xRef.getEntry(i, j), x.getEntry(i, j));
            }
        }

        
        for (int j = 0; j < b.getColumnDimension(); j++) {
            final FieldVector<Fraction> xj = solver.solve(b.getColumnVector(j));
            for (int i = 0; i < xj.getDimension(); i++){
                Assert.assertEquals("(" + i + ", " + j + ")",
                                    xRef.getEntry(i, j), xj.getEntry(i));
            }
        }

        
        for (int j = 0; j < b.getColumnDimension(); j++) {
            final SparseFieldVector<Fraction> bj;
            bj = new SparseFieldVector<Fraction>(FractionField.getInstance(),
                                                 b.getColumn(j));
            final FieldVector<Fraction> xj = solver.solve(bj);
            for (int i = 0; i < xj.getDimension(); i++) {
                Assert.assertEquals("(" + i + ", " + j + ")",
                                    xRef.getEntry(i, j), xj.getEntry(i));
            }
        }
    }

// org.apache.commons.math3.linear.FieldLUSolverTest::testDeterminant
    public void testDeterminant() {
        Assert.assertEquals( -1, getDeterminant(createFractionMatrix(testData)), 1E-15);
        Assert.assertEquals(-10, getDeterminant(createFractionMatrix(luData)), 1E-14);
        Assert.assertEquals(  0, getDeterminant(createFractionMatrix(singular)), 1E-15);
        Assert.assertEquals(  0, getDeterminant(createFractionMatrix(bigSingular)), 1E-15);
    }

// org.apache.commons.math3.linear.FieldMatrixImplTest::testDimensions
    public void testDimensions() {
        Array2DRowFieldMatrix<Fraction> m = new Array2DRowFieldMatrix<Fraction>(testData);
        Array2DRowFieldMatrix<Fraction> m2 = new Array2DRowFieldMatrix<Fraction>(testData2);
        Assert.assertEquals("testData row dimension",3,m.getRowDimension());
        Assert.assertEquals("testData column dimension",3,m.getColumnDimension());
        Assert.assertTrue("testData is square",m.isSquare());
        Assert.assertEquals("testData2 row dimension",m2.getRowDimension(),2);
        Assert.assertEquals("testData2 column dimension",m2.getColumnDimension(),3);
        Assert.assertTrue("testData2 is not square",!m2.isSquare());
    }

// org.apache.commons.math3.linear.FieldMatrixImplTest::testCopyFunctions
    public void testCopyFunctions() {
        Array2DRowFieldMatrix<Fraction> m1 = new Array2DRowFieldMatrix<Fraction>(testData);
        Array2DRowFieldMatrix<Fraction> m2 = new Array2DRowFieldMatrix<Fraction>(m1.getData());
        Assert.assertEquals(m2,m1);
        Array2DRowFieldMatrix<Fraction> m3 = new Array2DRowFieldMatrix<Fraction>(testData);
        Array2DRowFieldMatrix<Fraction> m4 = new Array2DRowFieldMatrix<Fraction>(m3.getData(), false);
        Assert.assertEquals(m4,m3);
    }

// org.apache.commons.math3.linear.FieldMatrixImplTest::testAdd
    public void testAdd() {
        Array2DRowFieldMatrix<Fraction> m = new Array2DRowFieldMatrix<Fraction>(testData);
        Array2DRowFieldMatrix<Fraction> mInv = new Array2DRowFieldMatrix<Fraction>(testDataInv);
        FieldMatrix<Fraction> mPlusMInv = m.add(mInv);
        Fraction[][] sumEntries = mPlusMInv.getData();
        for (int row = 0; row < m.getRowDimension(); row++) {
            for (int col = 0; col < m.getColumnDimension(); col++) {
                Assert.assertEquals(testDataPlusInv[row][col],sumEntries[row][col]);
            }
        }
    }

// org.apache.commons.math3.linear.FieldMatrixImplTest::testAddFail
    public void testAddFail() {
        Array2DRowFieldMatrix<Fraction> m = new Array2DRowFieldMatrix<Fraction>(testData);
        Array2DRowFieldMatrix<Fraction> m2 = new Array2DRowFieldMatrix<Fraction>(testData2);
        try {
            m.add(m2);
            Assert.fail("MathIllegalArgumentException expected");
        } catch (MathIllegalArgumentException ex) {
            
        }
    }

// org.apache.commons.math3.linear.FieldMatrixImplTest::testPlusMinus
    public void testPlusMinus() {
        Array2DRowFieldMatrix<Fraction> m = new Array2DRowFieldMatrix<Fraction>(testData);
        Array2DRowFieldMatrix<Fraction> m2 = new Array2DRowFieldMatrix<Fraction>(testDataInv);
        TestUtils.assertEquals(m.subtract(m2),m2.scalarMultiply(new Fraction(-1)).add(m));
        try {
            m.subtract(new Array2DRowFieldMatrix<Fraction>(testData2));
            Assert.fail("Expecting illegalArgumentException");
        } catch (MathIllegalArgumentException ex) {
            
        }
    }

// org.apache.commons.math3.linear.FieldMatrixImplTest::testMultiply
     public void testMultiply() {
        Array2DRowFieldMatrix<Fraction> m = new Array2DRowFieldMatrix<Fraction>(testData);
        Array2DRowFieldMatrix<Fraction> mInv = new Array2DRowFieldMatrix<Fraction>(testDataInv);
        Array2DRowFieldMatrix<Fraction> identity = new Array2DRowFieldMatrix<Fraction>(id);
        Array2DRowFieldMatrix<Fraction> m2 = new Array2DRowFieldMatrix<Fraction>(testData2);
        TestUtils.assertEquals(m.multiply(mInv), identity);
        TestUtils.assertEquals(mInv.multiply(m), identity);
        TestUtils.assertEquals(m.multiply(identity), m);
        TestUtils.assertEquals(identity.multiply(mInv), mInv);
        TestUtils.assertEquals(m2.multiply(identity), m2);
        try {
            m.multiply(new Array2DRowFieldMatrix<Fraction>(bigSingular));
            Assert.fail("Expecting illegalArgumentException");
        } catch (MathIllegalArgumentException ex) {
            
        }
    }

// org.apache.commons.math3.linear.FieldMatrixImplTest::testMultiply2
    public void testMultiply2() {
       FieldMatrix<Fraction> m3 = new Array2DRowFieldMatrix<Fraction>(d3);
       FieldMatrix<Fraction> m4 = new Array2DRowFieldMatrix<Fraction>(d4);
       FieldMatrix<Fraction> m5 = new Array2DRowFieldMatrix<Fraction>(d5);
       TestUtils.assertEquals(m3.multiply(m4), m5);
   }

// org.apache.commons.math3.linear.FieldMatrixImplTest::testPower
    public void testPower() {
        FieldMatrix<Fraction> m = new Array2DRowFieldMatrix<Fraction>(testData);
        FieldMatrix<Fraction> mInv = new Array2DRowFieldMatrix<Fraction>(testDataInv);
        FieldMatrix<Fraction> mPlusInv = new Array2DRowFieldMatrix<Fraction>(testDataPlusInv);
        FieldMatrix<Fraction> identity = new Array2DRowFieldMatrix<Fraction>(id);

        TestUtils.assertEquals(m.power(0), identity);
        TestUtils.assertEquals(mInv.power(0), identity);
        TestUtils.assertEquals(mPlusInv.power(0), identity);

        TestUtils.assertEquals(m.power(1), m);
        TestUtils.assertEquals(mInv.power(1), mInv);
        TestUtils.assertEquals(mPlusInv.power(1), mPlusInv);

        FieldMatrix<Fraction> C1 = m.copy();
        FieldMatrix<Fraction> C2 = mInv.copy();
        FieldMatrix<Fraction> C3 = mPlusInv.copy();

        
        for (int i = 2; i <= 5; ++i) {
            C1 = C1.multiply(m);
            C2 = C2.multiply(mInv);
            C3 = C3.multiply(mPlusInv);

            TestUtils.assertEquals(m.power(i), C1);
            TestUtils.assertEquals(mInv.power(i), C2);
            TestUtils.assertEquals(mPlusInv.power(i), C3);
        }

        try {
            FieldMatrix<Fraction> mNotSquare = new Array2DRowFieldMatrix<Fraction>(testData2T);
            mNotSquare.power(2);
            Assert.fail("Expecting NonSquareMatrixException");
        } catch (NonSquareMatrixException ex) {
            
        }

        try {
            m.power(-1);
            Assert.fail("Expecting IllegalArgumentException");
        } catch (IllegalArgumentException ex) {
            
        }
    }

// org.apache.commons.math3.linear.FieldMatrixImplTest::testTrace
    public void testTrace() {
        FieldMatrix<Fraction> m = new Array2DRowFieldMatrix<Fraction>(id);
        Assert.assertEquals("identity trace",new Fraction(3),m.getTrace());
        m = new Array2DRowFieldMatrix<Fraction>(testData2);
        try {
            m.getTrace();
            Assert.fail("Expecting NonSquareMatrixException");
        } catch (NonSquareMatrixException ex) {
            
        }
    }

// org.apache.commons.math3.linear.FieldMatrixImplTest::testScalarAdd
    public void testScalarAdd() {
        FieldMatrix<Fraction> m = new Array2DRowFieldMatrix<Fraction>(testData);
        TestUtils.assertEquals(new Array2DRowFieldMatrix<Fraction>(testDataPlus2), m.scalarAdd(new Fraction(2)));
    }

// org.apache.commons.math3.linear.FieldMatrixImplTest::testOperate
    public void testOperate() {
        FieldMatrix<Fraction> m = new Array2DRowFieldMatrix<Fraction>(id);
        TestUtils.assertEquals(testVector, m.operate(testVector));
        TestUtils.assertEquals(testVector, m.operate(new ArrayFieldVector<Fraction>(testVector)).getData());
        m = new Array2DRowFieldMatrix<Fraction>(bigSingular);
        try {
            m.operate(testVector);
            Assert.fail("Expecting illegalArgumentException");
        } catch (MathIllegalArgumentException ex) {
            
        }
    }

// org.apache.commons.math3.linear.FieldMatrixImplTest::testMath209
    public void testMath209() {
        FieldMatrix<Fraction> a = new Array2DRowFieldMatrix<Fraction>(new Fraction[][] {
                { new Fraction(1), new Fraction(2) }, { new Fraction(3), new Fraction(4) }, { new Fraction(5), new Fraction(6) }
        }, false);
        Fraction[] b = a.operate(new Fraction[] { new Fraction(1), new Fraction(1) });
        Assert.assertEquals(a.getRowDimension(), b.length);
        Assert.assertEquals( new Fraction(3), b[0]);
        Assert.assertEquals( new Fraction(7), b[1]);
        Assert.assertEquals(new Fraction(11), b[2]);
    }

// org.apache.commons.math3.linear.FieldMatrixImplTest::testTranspose
    public void testTranspose() {
        FieldMatrix<Fraction> m = new Array2DRowFieldMatrix<Fraction>(testData);
        FieldMatrix<Fraction> mIT = new FieldLUDecomposition<Fraction>(m).getSolver().getInverse().transpose();
        FieldMatrix<Fraction> mTI = new FieldLUDecomposition<Fraction>(m.transpose()).getSolver().getInverse();
        TestUtils.assertEquals(mIT, mTI);
        m = new Array2DRowFieldMatrix<Fraction>(testData2);
        FieldMatrix<Fraction> mt = new Array2DRowFieldMatrix<Fraction>(testData2T);
        TestUtils.assertEquals(mt, m.transpose());
    }

// org.apache.commons.math3.linear.FieldMatrixImplTest::testPremultiplyVector
    public void testPremultiplyVector() {
        FieldMatrix<Fraction> m = new Array2DRowFieldMatrix<Fraction>(testData);
        TestUtils.assertEquals(m.preMultiply(testVector), preMultTest);
        TestUtils.assertEquals(m.preMultiply(new ArrayFieldVector<Fraction>(testVector).getData()),
                               preMultTest);
        m = new Array2DRowFieldMatrix<Fraction>(bigSingular);
        try {
            m.preMultiply(testVector);
            Assert.fail("expecting MathIllegalArgumentException");
        } catch (MathIllegalArgumentException ex) {
            
        }
    }

// org.apache.commons.math3.linear.FieldMatrixImplTest::testPremultiply
    public void testPremultiply() {
        FieldMatrix<Fraction> m3 = new Array2DRowFieldMatrix<Fraction>(d3);
        FieldMatrix<Fraction> m4 = new Array2DRowFieldMatrix<Fraction>(d4);
        FieldMatrix<Fraction> m5 = new Array2DRowFieldMatrix<Fraction>(d5);
        TestUtils.assertEquals(m4.preMultiply(m3), m5);

        Array2DRowFieldMatrix<Fraction> m = new Array2DRowFieldMatrix<Fraction>(testData);
        Array2DRowFieldMatrix<Fraction> mInv = new Array2DRowFieldMatrix<Fraction>(testDataInv);
        Array2DRowFieldMatrix<Fraction> identity = new Array2DRowFieldMatrix<Fraction>(id);
        TestUtils.assertEquals(m.preMultiply(mInv), identity);
        TestUtils.assertEquals(mInv.preMultiply(m), identity);
        TestUtils.assertEquals(m.preMultiply(identity), m);
        TestUtils.assertEquals(identity.preMultiply(mInv), mInv);
        try {
            m.preMultiply(new Array2DRowFieldMatrix<Fraction>(bigSingular));
            Assert.fail("Expecting illegalArgumentException");
        } catch (MathIllegalArgumentException ex) {
            
        }
    }

// org.apache.commons.math3.linear.FieldMatrixImplTest::testGetVectors
    public void testGetVectors() {
        FieldMatrix<Fraction> m = new Array2DRowFieldMatrix<Fraction>(testData);
        TestUtils.assertEquals(m.getRow(0), testDataRow1);
        TestUtils.assertEquals(m.getColumn(2), testDataCol3);
        try {
            m.getRow(10);
            Assert.fail("expecting OutOfRangeException");
        } catch (OutOfRangeException ex) {
            
        }
        try {
            m.getColumn(-1);
            Assert.fail("expecting OutOfRangeException");
        } catch (OutOfRangeException ex) {
            
        }
    }

// org.apache.commons.math3.linear.FieldMatrixImplTest::testGetEntry
    public void testGetEntry() {
        FieldMatrix<Fraction> m = new Array2DRowFieldMatrix<Fraction>(testData);
        Assert.assertEquals("get entry", m.getEntry(0,1), new Fraction(2));
        try {
            m.getEntry(10, 4);
            Assert.fail ("Expecting OutOfRangeException");
        } catch (OutOfRangeException ex) {
            
        }
    }

// org.apache.commons.math3.linear.FieldMatrixImplTest::testExamples
    public void testExamples() {
        
        Fraction[][] matrixData = {
                {new Fraction(1),new Fraction(2),new Fraction(3)},
                {new Fraction(2),new Fraction(5),new Fraction(3)}
        };
        FieldMatrix<Fraction> m = new Array2DRowFieldMatrix<Fraction>(matrixData);
        
        Fraction[][] matrixData2 = {
                {new Fraction(1),new Fraction(2)},
                {new Fraction(2),new Fraction(5)},
                {new Fraction(1), new Fraction(7)}
        };
        FieldMatrix<Fraction> n = new Array2DRowFieldMatrix<Fraction>(matrixData2);
        
        FieldMatrix<Fraction> p = m.multiply(n);
        Assert.assertEquals(2, p.getRowDimension());
        Assert.assertEquals(2, p.getColumnDimension());
        
        FieldMatrix<Fraction> pInverse = new FieldLUDecomposition<Fraction>(p).getSolver().getInverse();
        Assert.assertEquals(2, pInverse.getRowDimension());
        Assert.assertEquals(2, pInverse.getColumnDimension());

        
        Fraction[][] coefficientsData = {
                {new Fraction(2), new Fraction(3), new Fraction(-2)},
                {new Fraction(-1), new Fraction(7), new Fraction(6)},
                {new Fraction(4), new Fraction(-3), new Fraction(-5)}
        };
        FieldMatrix<Fraction> coefficients = new Array2DRowFieldMatrix<Fraction>(coefficientsData);
        Fraction[] constants = {
            new Fraction(1), new Fraction(-2), new Fraction(1)
        };
        Fraction[] solution;
        solution = new FieldLUDecomposition<Fraction>(coefficients)
            .getSolver()
            .solve(new ArrayFieldVector<Fraction>(constants, false)).toArray();
        Assert.assertEquals(new Fraction(2).multiply(solution[0]).
                     add(new Fraction(3).multiply(solution[1])).
                     subtract(new Fraction(2).multiply(solution[2])), constants[0]);
        Assert.assertEquals(new Fraction(-1).multiply(solution[0]).
                     add(new Fraction(7).multiply(solution[1])).
                     add(new Fraction(6).multiply(solution[2])), constants[1]);
        Assert.assertEquals(new Fraction(4).multiply(solution[0]).
                     subtract(new Fraction(3).multiply(solution[1])).
                     subtract(new Fraction(5).multiply(solution[2])), constants[2]);

    }

// org.apache.commons.math3.linear.FieldMatrixImplTest::testGetSubMatrix
    public void testGetSubMatrix() {
        FieldMatrix<Fraction> m = new Array2DRowFieldMatrix<Fraction>(subTestData);
        checkGetSubMatrix(m, subRows23Cols00,  2 , 3 , 0, 0);
        checkGetSubMatrix(m, subRows00Cols33,  0 , 0 , 3, 3);
        checkGetSubMatrix(m, subRows01Cols23,  0 , 1 , 2, 3);
        checkGetSubMatrix(m, subRows02Cols13,  new int[] { 0, 2 }, new int[] { 1, 3 });
        checkGetSubMatrix(m, subRows03Cols12,  new int[] { 0, 3 }, new int[] { 1, 2 });
        checkGetSubMatrix(m, subRows03Cols123, new int[] { 0, 3 }, new int[] { 1, 2, 3 });
        checkGetSubMatrix(m, subRows20Cols123, new int[] { 2, 0 }, new int[] { 1, 2, 3 });
        checkGetSubMatrix(m, subRows31Cols31,  new int[] { 3, 1 }, new int[] { 3, 1 });
        checkGetSubMatrix(m, subRows31Cols31,  new int[] { 3, 1 }, new int[] { 3, 1 });
        checkGetSubMatrix(m, null,  1, 0, 2, 4);
        checkGetSubMatrix(m, null, -1, 1, 2, 2);
        checkGetSubMatrix(m, null,  1, 0, 2, 2);
        checkGetSubMatrix(m, null,  1, 0, 2, 4);
        checkGetSubMatrix(m, null, new int[] {},    new int[] { 0 });
        checkGetSubMatrix(m, null, new int[] { 0 }, new int[] { 4 });
    }

// org.apache.commons.math3.linear.FieldMatrixImplTest::testCopySubMatrix
    public void testCopySubMatrix() {
        FieldMatrix<Fraction> m = new Array2DRowFieldMatrix<Fraction>(subTestData);
        checkCopy(m, subRows23Cols00,  2 , 3 , 0, 0);
        checkCopy(m, subRows00Cols33,  0 , 0 , 3, 3);
        checkCopy(m, subRows01Cols23,  0 , 1 , 2, 3);
        checkCopy(m, subRows02Cols13,  new int[] { 0, 2 }, new int[] { 1, 3 });
        checkCopy(m, subRows03Cols12,  new int[] { 0, 3 }, new int[] { 1, 2 });
        checkCopy(m, subRows03Cols123, new int[] { 0, 3 }, new int[] { 1, 2, 3 });
        checkCopy(m, subRows20Cols123, new int[] { 2, 0 }, new int[] { 1, 2, 3 });
        checkCopy(m, subRows31Cols31,  new int[] { 3, 1 }, new int[] { 3, 1 });
        checkCopy(m, subRows31Cols31,  new int[] { 3, 1 }, new int[] { 3, 1 });

        checkCopy(m, null,  1, 0, 2, 4);
        checkCopy(m, null, -1, 1, 2, 2);
        checkCopy(m, null,  1, 0, 2, 2);
        checkCopy(m, null,  1, 0, 2, 4);
        checkCopy(m, null, new int[] {},    new int[] { 0 });
        checkCopy(m, null, new int[] { 0 }, new int[] { 4 });
    }

// org.apache.commons.math3.linear.FieldMatrixImplTest::testGetRowMatrix
    public void testGetRowMatrix() {
        FieldMatrix<Fraction> m = new Array2DRowFieldMatrix<Fraction>(subTestData);
        FieldMatrix<Fraction> mRow0 = new Array2DRowFieldMatrix<Fraction>(subRow0);
        FieldMatrix<Fraction> mRow3 = new Array2DRowFieldMatrix<Fraction>(subRow3);
        Assert.assertEquals("Row0", mRow0,
                m.getRowMatrix(0));
        Assert.assertEquals("Row3", mRow3,
                m.getRowMatrix(3));
        try {
            m.getRowMatrix(-1);
            Assert.fail("Expecting OutOfRangeException");
        } catch (OutOfRangeException ex) {
            
        }
        try {
            m.getRowMatrix(4);
            Assert.fail("Expecting OutOfRangeException");
        } catch (OutOfRangeException ex) {
            
        }
    }

// org.apache.commons.math3.linear.FieldMatrixImplTest::testSetRowMatrix
    public void testSetRowMatrix() {
        FieldMatrix<Fraction> m = new Array2DRowFieldMatrix<Fraction>(subTestData);
        FieldMatrix<Fraction> mRow3 = new Array2DRowFieldMatrix<Fraction>(subRow3);
        Assert.assertNotSame(mRow3, m.getRowMatrix(0));
        m.setRowMatrix(0, mRow3);
        Assert.assertEquals(mRow3, m.getRowMatrix(0));
        try {
            m.setRowMatrix(-1, mRow3);
            Assert.fail("Expecting OutOfRangeException");
        } catch (OutOfRangeException ex) {
            
        }
        try {
            m.setRowMatrix(0, m);
            Assert.fail("Expecting MatrixDimensionMismatchException");
        } catch (MatrixDimensionMismatchException ex) {
            
        }
    }

// org.apache.commons.math3.linear.FieldMatrixImplTest::testGetColumnMatrix
    public void testGetColumnMatrix() {
        FieldMatrix<Fraction> m = new Array2DRowFieldMatrix<Fraction>(subTestData);
        FieldMatrix<Fraction> mColumn1 = new Array2DRowFieldMatrix<Fraction>(subColumn1);
        FieldMatrix<Fraction> mColumn3 = new Array2DRowFieldMatrix<Fraction>(subColumn3);
        Assert.assertEquals("Column1", mColumn1,
                m.getColumnMatrix(1));
        Assert.assertEquals("Column3", mColumn3,
                m.getColumnMatrix(3));
        try {
            m.getColumnMatrix(-1);
            Assert.fail("Expecting OutOfRangeException");
        } catch (OutOfRangeException ex) {
            
        }
        try {
            m.getColumnMatrix(4);
            Assert.fail("Expecting OutOfRangeException");
        } catch (OutOfRangeException ex) {
            
        }
    }

// org.apache.commons.math3.linear.FieldMatrixImplTest::testSetColumnMatrix
    public void testSetColumnMatrix() {
        FieldMatrix<Fraction> m = new Array2DRowFieldMatrix<Fraction>(subTestData);
        FieldMatrix<Fraction> mColumn3 = new Array2DRowFieldMatrix<Fraction>(subColumn3);
        Assert.assertNotSame(mColumn3, m.getColumnMatrix(1));
        m.setColumnMatrix(1, mColumn3);
        Assert.assertEquals(mColumn3, m.getColumnMatrix(1));
        try {
            m.setColumnMatrix(-1, mColumn3);
            Assert.fail("Expecting OutOfRangeException");
        } catch (OutOfRangeException ex) {
            
        }
        try {
            m.setColumnMatrix(0, m);
            Assert.fail("Expecting MatrixDimensionMismatchException");
        } catch (MatrixDimensionMismatchException ex) {
            
        }
    }

// org.apache.commons.math3.linear.FieldMatrixImplTest::testGetRowVector
    public void testGetRowVector() {
        FieldMatrix<Fraction> m = new Array2DRowFieldMatrix<Fraction>(subTestData);
        FieldVector<Fraction> mRow0 = new ArrayFieldVector<Fraction>(subRow0[0]);
        FieldVector<Fraction> mRow3 = new ArrayFieldVector<Fraction>(subRow3[0]);
        Assert.assertEquals("Row0", mRow0, m.getRowVector(0));
        Assert.assertEquals("Row3", mRow3, m.getRowVector(3));
        try {
            m.getRowVector(-1);
            Assert.fail("Expecting OutOfRangeException");
        } catch (OutOfRangeException ex) {
            
        }
        try {
            m.getRowVector(4);
            Assert.fail("Expecting OutOfRangeException");
        } catch (OutOfRangeException ex) {
            
        }
    }

// org.apache.commons.math3.linear.FieldMatrixImplTest::testSetRowVector
    public void testSetRowVector() {
        FieldMatrix<Fraction> m = new Array2DRowFieldMatrix<Fraction>(subTestData);
        FieldVector<Fraction> mRow3 = new ArrayFieldVector<Fraction>(subRow3[0]);
        Assert.assertNotSame(mRow3, m.getRowMatrix(0));
        m.setRowVector(0, mRow3);
        Assert.assertEquals(mRow3, m.getRowVector(0));
        try {
            m.setRowVector(-1, mRow3);
            Assert.fail("Expecting OutOfRangeException");
        } catch (OutOfRangeException ex) {
            
        }
        try {
            m.setRowVector(0, new ArrayFieldVector<Fraction>(FractionField.getInstance(), 5));
            Assert.fail("Expecting MatrixDimensionMismatchException");
        } catch (MatrixDimensionMismatchException ex) {
            
        }
    }

// org.apache.commons.math3.linear.FieldMatrixImplTest::testGetColumnVector
    public void testGetColumnVector() {
        FieldMatrix<Fraction> m = new Array2DRowFieldMatrix<Fraction>(subTestData);
        FieldVector<Fraction> mColumn1 = columnToVector(subColumn1);
        FieldVector<Fraction> mColumn3 = columnToVector(subColumn3);
        Assert.assertEquals("Column1", mColumn1, m.getColumnVector(1));
        Assert.assertEquals("Column3", mColumn3, m.getColumnVector(3));
        try {
            m.getColumnVector(-1);
            Assert.fail("Expecting OutOfRangeException");
        } catch (OutOfRangeException ex) {
            
        }
        try {
            m.getColumnVector(4);
            Assert.fail("Expecting OutOfRangeException");
        } catch (OutOfRangeException ex) {
            
        }
    }

// org.apache.commons.math3.linear.FieldMatrixImplTest::testSetColumnVector
    public void testSetColumnVector() {
        FieldMatrix<Fraction> m = new Array2DRowFieldMatrix<Fraction>(subTestData);
        FieldVector<Fraction> mColumn3 = columnToVector(subColumn3);
        Assert.assertNotSame(mColumn3, m.getColumnVector(1));
        m.setColumnVector(1, mColumn3);
        Assert.assertEquals(mColumn3, m.getColumnVector(1));
        try {
            m.setColumnVector(-1, mColumn3);
            Assert.fail("Expecting OutOfRangeException");
        } catch (OutOfRangeException ex) {
            
        }
        try {
            m.setColumnVector(0, new ArrayFieldVector<Fraction>(FractionField.getInstance(), 5));
            Assert.fail("Expecting MatrixDimensionMismatchException");
        } catch (MatrixDimensionMismatchException ex) {
            
        }
    }

// org.apache.commons.math3.linear.FieldMatrixImplTest::testGetRow
    public void testGetRow() {
        FieldMatrix<Fraction> m = new Array2DRowFieldMatrix<Fraction>(subTestData);
        checkArrays(subRow0[0], m.getRow(0));
        checkArrays(subRow3[0], m.getRow(3));
        try {
            m.getRow(-1);
            Assert.fail("Expecting OutOfRangeException");
        } catch (OutOfRangeException ex) {
            
        }
        try {
            m.getRow(4);
            Assert.fail("Expecting OutOfRangeException");
        } catch (OutOfRangeException ex) {
            
        }
    }

// org.apache.commons.math3.linear.FieldMatrixImplTest::testSetRow
    public void testSetRow() {
        FieldMatrix<Fraction> m = new Array2DRowFieldMatrix<Fraction>(subTestData);
        Assert.assertTrue(subRow3[0][0] != m.getRow(0)[0]);
        m.setRow(0, subRow3[0]);
        checkArrays(subRow3[0], m.getRow(0));
        try {
            m.setRow(-1, subRow3[0]);
            Assert.fail("Expecting OutOfRangeException");
        } catch (OutOfRangeException ex) {
            
        }
        try {
            m.setRow(0, new Fraction[5]);
            Assert.fail("Expecting MatrixDimensionMismatchException");
        } catch (MatrixDimensionMismatchException ex) {
            
        }
    }

// org.apache.commons.math3.linear.FieldMatrixImplTest::testGetColumn
    public void testGetColumn() {
        FieldMatrix<Fraction> m = new Array2DRowFieldMatrix<Fraction>(subTestData);
        Fraction[] mColumn1 = columnToArray(subColumn1);
        Fraction[] mColumn3 = columnToArray(subColumn3);
        checkArrays(mColumn1, m.getColumn(1));
        checkArrays(mColumn3, m.getColumn(3));
        try {
            m.getColumn(-1);
            Assert.fail("Expecting OutOfRangeException");
        } catch (OutOfRangeException ex) {
            
        }
        try {
            m.getColumn(4);
            Assert.fail("Expecting OutOfRangeException");
        } catch (OutOfRangeException ex) {
            
        }
    }

// org.apache.commons.math3.linear.FieldMatrixImplTest::testSetColumn
    public void testSetColumn() {
        FieldMatrix<Fraction> m = new Array2DRowFieldMatrix<Fraction>(subTestData);
        Fraction[] mColumn3 = columnToArray(subColumn3);
        Assert.assertTrue(mColumn3[0] != m.getColumn(1)[0]);
        m.setColumn(1, mColumn3);
        checkArrays(mColumn3, m.getColumn(1));
        try {
            m.setColumn(-1, mColumn3);
            Assert.fail("Expecting OutOfRangeException");
        } catch (OutOfRangeException ex) {
            
        }
        try {
            m.setColumn(0, new Fraction[5]);
            Assert.fail("Expecting MatrixDimensionMismatchException");
        } catch (MatrixDimensionMismatchException ex) {
            
        }
    }

// org.apache.commons.math3.linear.FieldMatrixImplTest::testEqualsAndHashCode
    public void testEqualsAndHashCode() {
        Array2DRowFieldMatrix<Fraction> m = new Array2DRowFieldMatrix<Fraction>(testData);
        Array2DRowFieldMatrix<Fraction> m1 = (Array2DRowFieldMatrix<Fraction>) m.copy();
        Array2DRowFieldMatrix<Fraction> mt = (Array2DRowFieldMatrix<Fraction>) m.transpose();
        Assert.assertTrue(m.hashCode() != mt.hashCode());
        Assert.assertEquals(m.hashCode(), m1.hashCode());
        Assert.assertEquals(m, m);
        Assert.assertEquals(m, m1);
        Assert.assertFalse(m.equals(null));
        Assert.assertFalse(m.equals(mt));
        Assert.assertFalse(m.equals(new Array2DRowFieldMatrix<Fraction>(bigSingular)));
    }

// org.apache.commons.math3.linear.FieldMatrixImplTest::testToString
    public void testToString() {
        Array2DRowFieldMatrix<Fraction> m = new Array2DRowFieldMatrix<Fraction>(testData);
        Assert.assertEquals("Array2DRowFieldMatrix{{1,2,3},{2,5,3},{1,0,8}}", m.toString());
        m = new Array2DRowFieldMatrix<Fraction>(FractionField.getInstance());
        Assert.assertEquals("Array2DRowFieldMatrix{}", m.toString());
    }

// org.apache.commons.math3.linear.FieldMatrixImplTest::testSetSubMatrix
    public void testSetSubMatrix() {
        Array2DRowFieldMatrix<Fraction> m = new Array2DRowFieldMatrix<Fraction>(testData);
        m.setSubMatrix(detData2,1,1);
        FieldMatrix<Fraction> expected = new Array2DRowFieldMatrix<Fraction>
            (new Fraction[][] {
                    {new Fraction(1),new Fraction(2),new Fraction(3)},
                    {new Fraction(2),new Fraction(1),new Fraction(3)},
                    {new Fraction(1),new Fraction(2),new Fraction(4)}
             });
        Assert.assertEquals(expected, m);

        m.setSubMatrix(detData2,0,0);
        expected = new Array2DRowFieldMatrix<Fraction>
            (new Fraction[][] {
                    {new Fraction(1),new Fraction(3),new Fraction(3)},
                    {new Fraction(2),new Fraction(4),new Fraction(3)},
                    {new Fraction(1),new Fraction(2),new Fraction(4)}
             });
        Assert.assertEquals(expected, m);

        m.setSubMatrix(testDataPlus2,0,0);
        expected = new Array2DRowFieldMatrix<Fraction>
            (new Fraction[][] {
                    {new Fraction(3),new Fraction(4),new Fraction(5)},
                    {new Fraction(4),new Fraction(7),new Fraction(5)},
                    {new Fraction(3),new Fraction(2),new Fraction(10)}
             });
        Assert.assertEquals(expected, m);

        
        try {
            m.setSubMatrix(testData,1,1);
            Assert.fail("expecting OutOfRangeException");
        } catch (OutOfRangeException e) {
            
        }
        
        try {
            m.setSubMatrix(testData,-1,1);
            Assert.fail("expecting OutOfRangeException");
        } catch (OutOfRangeException e) {
            
        }
        try {
            m.setSubMatrix(testData,1,-1);
            Assert.fail("expecting OutOfRangeException");
        } catch (OutOfRangeException e) {
            
        }

        
        try {
            m.setSubMatrix(null, 1, 1);
            Assert.fail("expecting NullArgumentException");
        } catch (NullArgumentException e) {
            
        }
        Array2DRowFieldMatrix<Fraction> m2 = new Array2DRowFieldMatrix<Fraction>(FractionField.getInstance());
        try {
            m2.setSubMatrix(testData,0,1);
            Assert.fail("expecting MathIllegalStateException");
        } catch (MathIllegalStateException e) {
            
        }
        try {
            m2.setSubMatrix(testData,1,0);
            Assert.fail("expecting MathIllegalStateException");
        } catch (MathIllegalStateException e) {
            
        }

        
        try {
            m.setSubMatrix(new Fraction[][] {{new Fraction(1)}, {new Fraction(2), new Fraction(3)}}, 0, 0);
            Assert.fail("expecting MathIllegalArgumentException");
        } catch (MathIllegalArgumentException e) {
            
        }

        
        try {
            m.setSubMatrix(new Fraction[][] {{}}, 0, 0);
            Assert.fail("expecting MathIllegalArgumentException");
        } catch (MathIllegalArgumentException e) {
            
        }

    }

// org.apache.commons.math3.linear.FieldMatrixImplTest::testWalk
    public void testWalk() {
        int rows    = 150;
        int columns = 75;

        FieldMatrix<Fraction> m =
            new Array2DRowFieldMatrix<Fraction>(FractionField.getInstance(), rows, columns);
        m.walkInRowOrder(new SetVisitor());
        GetVisitor getVisitor = new GetVisitor();
        m.walkInOptimizedOrder(getVisitor);
        Assert.assertEquals(rows * columns, getVisitor.getCount());

        m = new Array2DRowFieldMatrix<Fraction>(FractionField.getInstance(), rows, columns);
        m.walkInRowOrder(new SetVisitor(), 1, rows - 2, 1, columns - 2);
        getVisitor = new GetVisitor();
        m.walkInOptimizedOrder(getVisitor, 1, rows - 2, 1, columns - 2);
        Assert.assertEquals((rows - 2) * (columns - 2), getVisitor.getCount());
        for (int i = 0; i < rows; ++i) {
            Assert.assertEquals(new Fraction(0), m.getEntry(i, 0));
            Assert.assertEquals(new Fraction(0), m.getEntry(i, columns - 1));
        }
        for (int j = 0; j < columns; ++j) {
            Assert.assertEquals(new Fraction(0), m.getEntry(0, j));
            Assert.assertEquals(new Fraction(0), m.getEntry(rows - 1, j));
        }

        m = new Array2DRowFieldMatrix<Fraction>(FractionField.getInstance(), rows, columns);
        m.walkInColumnOrder(new SetVisitor());
        getVisitor = new GetVisitor();
        m.walkInOptimizedOrder(getVisitor);
        Assert.assertEquals(rows * columns, getVisitor.getCount());

        m = new Array2DRowFieldMatrix<Fraction>(FractionField.getInstance(), rows, columns);
        m.walkInColumnOrder(new SetVisitor(), 1, rows - 2, 1, columns - 2);
        getVisitor = new GetVisitor();
        m.walkInOptimizedOrder(getVisitor, 1, rows - 2, 1, columns - 2);
        Assert.assertEquals((rows - 2) * (columns - 2), getVisitor.getCount());
        for (int i = 0; i < rows; ++i) {
            Assert.assertEquals(new Fraction(0), m.getEntry(i, 0));
            Assert.assertEquals(new Fraction(0), m.getEntry(i, columns - 1));
        }
        for (int j = 0; j < columns; ++j) {
            Assert.assertEquals(new Fraction(0), m.getEntry(0, j));
            Assert.assertEquals(new Fraction(0), m.getEntry(rows - 1, j));
        }

        m = new Array2DRowFieldMatrix<Fraction>(FractionField.getInstance(), rows, columns);
        m.walkInOptimizedOrder(new SetVisitor());
        getVisitor = new GetVisitor();
        m.walkInRowOrder(getVisitor);
        Assert.assertEquals(rows * columns, getVisitor.getCount());

        m = new Array2DRowFieldMatrix<Fraction>(FractionField.getInstance(), rows, columns);
        m.walkInOptimizedOrder(new SetVisitor(), 1, rows - 2, 1, columns - 2);
        getVisitor = new GetVisitor();
        m.walkInRowOrder(getVisitor, 1, rows - 2, 1, columns - 2);
        Assert.assertEquals((rows - 2) * (columns - 2), getVisitor.getCount());
        for (int i = 0; i < rows; ++i) {
            Assert.assertEquals(new Fraction(0), m.getEntry(i, 0));
            Assert.assertEquals(new Fraction(0), m.getEntry(i, columns - 1));
        }
        for (int j = 0; j < columns; ++j) {
            Assert.assertEquals(new Fraction(0), m.getEntry(0, j));
            Assert.assertEquals(new Fraction(0), m.getEntry(rows - 1, j));
        }

        m = new Array2DRowFieldMatrix<Fraction>(FractionField.getInstance(), rows, columns);
        m.walkInOptimizedOrder(new SetVisitor());
        getVisitor = new GetVisitor();
        m.walkInColumnOrder(getVisitor);
        Assert.assertEquals(rows * columns, getVisitor.getCount());

        m = new Array2DRowFieldMatrix<Fraction>(FractionField.getInstance(), rows, columns);
        m.walkInOptimizedOrder(new SetVisitor(), 1, rows - 2, 1, columns - 2);
        getVisitor = new GetVisitor();
        m.walkInColumnOrder(getVisitor, 1, rows - 2, 1, columns - 2);
        Assert.assertEquals((rows - 2) * (columns - 2), getVisitor.getCount());
        for (int i = 0; i < rows; ++i) {
            Assert.assertEquals(new Fraction(0), m.getEntry(i, 0));
            Assert.assertEquals(new Fraction(0), m.getEntry(i, columns - 1));
        }
        for (int j = 0; j < columns; ++j) {
            Assert.assertEquals(new Fraction(0), m.getEntry(0, j));
            Assert.assertEquals(new Fraction(0), m.getEntry(rows - 1, j));
        }
    }

// org.apache.commons.math3.linear.FieldMatrixImplTest::testSerial
    public void testSerial()  {
        Array2DRowFieldMatrix<Fraction> m = new Array2DRowFieldMatrix<Fraction>(testData);
        Assert.assertEquals(m,TestUtils.serializeAndRecover(m));
    }

// org.apache.commons.math3.linear.MatrixUtilsTest::testCreateRealMatrix
    public void testCreateRealMatrix() {
        Assert.assertEquals(new BlockRealMatrix(testData),
                MatrixUtils.createRealMatrix(testData));
        try {
            MatrixUtils.createRealMatrix(new double[][] {{1}, {1,2}});  
            Assert.fail("Expecting MathIllegalArgumentException");
        } catch (MathIllegalArgumentException ex) {
            
        }
        try {
            MatrixUtils.createRealMatrix(new double[][] {{}, {}});  
            Assert.fail("Expecting MathIllegalArgumentException");
        } catch (MathIllegalArgumentException ex) {
            
        }
        try {
            MatrixUtils.createRealMatrix(null);  
            Assert.fail("Expecting NullArgumentException");
        } catch (NullArgumentException ex) {
            
        }
    }

// org.apache.commons.math3.linear.MatrixUtilsTest::testcreateFieldMatrix
    public void testcreateFieldMatrix() {
        Assert.assertEquals(new Array2DRowFieldMatrix<Fraction>(asFraction(testData)),
                     MatrixUtils.createFieldMatrix(asFraction(testData)));
        Assert.assertEquals(new Array2DRowFieldMatrix<Fraction>(FractionField.getInstance(), fractionColMatrix),
                     MatrixUtils.createFieldMatrix(fractionColMatrix));
        try {
            MatrixUtils.createFieldMatrix(asFraction(new double[][] {{1}, {1,2}}));  
            Assert.fail("Expecting MathIllegalArgumentException");
        } catch (MathIllegalArgumentException ex) {
            
        }
        try {
            MatrixUtils.createFieldMatrix(asFraction(new double[][] {{}, {}}));  
            Assert.fail("Expecting MathIllegalArgumentException");
        } catch (MathIllegalArgumentException ex) {
            
        }
        try {
            MatrixUtils.createFieldMatrix((Fraction[][])null);  
            Assert.fail("Expecting NullArgumentException");
        } catch (NullArgumentException ex) {
            
        }
    }

// org.apache.commons.math3.linear.MatrixUtilsTest::testCreateRowRealMatrix
    public void testCreateRowRealMatrix() {
        Assert.assertEquals(MatrixUtils.createRowRealMatrix(row),
                     new BlockRealMatrix(rowMatrix));
        try {
            MatrixUtils.createRowRealMatrix(new double[] {});  
            Assert.fail("Expecting NotStrictlyPositiveException");
        } catch (NotStrictlyPositiveException ex) {
            
        }
        try {
            MatrixUtils.createRowRealMatrix(null);  
            Assert.fail("Expecting NullArgumentException");
        } catch (NullArgumentException ex) {
            
        }
    }

// org.apache.commons.math3.linear.MatrixUtilsTest::testCreateRowFieldMatrix
    public void testCreateRowFieldMatrix() {
        Assert.assertEquals(MatrixUtils.createRowFieldMatrix(asFraction(row)),
                     new Array2DRowFieldMatrix<Fraction>(asFraction(rowMatrix)));
        Assert.assertEquals(MatrixUtils.createRowFieldMatrix(fractionRow),
                     new Array2DRowFieldMatrix<Fraction>(fractionRowMatrix));
        try {
            MatrixUtils.createRowFieldMatrix(new Fraction[] {});  
            Assert.fail("Expecting MathIllegalArgumentException");
        } catch (MathIllegalArgumentException ex) {
            
        }
        try {
            MatrixUtils.createRowFieldMatrix((Fraction[]) null);  
            Assert.fail("Expecting NullArgumentException");
        } catch (NullArgumentException ex) {
            
        }
    }

// org.apache.commons.math3.linear.MatrixUtilsTest::testCreateColumnRealMatrix
    public void testCreateColumnRealMatrix() {
        Assert.assertEquals(MatrixUtils.createColumnRealMatrix(col),
                     new BlockRealMatrix(colMatrix));
        try {
            MatrixUtils.createColumnRealMatrix(new double[] {});  
            Assert.fail("Expecting MathIllegalArgumentException");
        } catch (MathIllegalArgumentException ex) {
            
        }
        try {
            MatrixUtils.createColumnRealMatrix(null);  
            Assert.fail("Expecting NullArgumentException");
        } catch (NullArgumentException ex) {
            
        }
    }

// org.apache.commons.math3.linear.MatrixUtilsTest::testCreateColumnFieldMatrix
    public void testCreateColumnFieldMatrix() {
        Assert.assertEquals(MatrixUtils.createColumnFieldMatrix(asFraction(col)),
                     new Array2DRowFieldMatrix<Fraction>(asFraction(colMatrix)));
        Assert.assertEquals(MatrixUtils.createColumnFieldMatrix(fractionCol),
                     new Array2DRowFieldMatrix<Fraction>(fractionColMatrix));

        try {
            MatrixUtils.createColumnFieldMatrix(new Fraction[] {});  
            Assert.fail("Expecting MathIllegalArgumentException");
        } catch (MathIllegalArgumentException ex) {
            
        }
        try {
            MatrixUtils.createColumnFieldMatrix((Fraction[]) null);  
            Assert.fail("Expecting NullArgumentException");
        } catch (NullArgumentException ex) {
            
        }
    }

// org.apache.commons.math3.linear.MatrixUtilsTest::testCreateIdentityMatrix
    public void testCreateIdentityMatrix() {
        checkIdentityMatrix(MatrixUtils.createRealIdentityMatrix(3));
        checkIdentityMatrix(MatrixUtils.createRealIdentityMatrix(2));
        checkIdentityMatrix(MatrixUtils.createRealIdentityMatrix(1));
        try {
            MatrixUtils.createRealIdentityMatrix(0);
            Assert.fail("Expecting MathIllegalArgumentException");
        } catch (MathIllegalArgumentException ex) {
            
        }
    }

// org.apache.commons.math3.linear.MatrixUtilsTest::testcreateFieldIdentityMatrix
    public void testcreateFieldIdentityMatrix() {
        checkIdentityFieldMatrix(MatrixUtils.createFieldIdentityMatrix(FractionField.getInstance(), 3));
        checkIdentityFieldMatrix(MatrixUtils.createFieldIdentityMatrix(FractionField.getInstance(), 2));
        checkIdentityFieldMatrix(MatrixUtils.createFieldIdentityMatrix(FractionField.getInstance(), 1));
        try {
            MatrixUtils.createRealIdentityMatrix(0);
            Assert.fail("Expecting MathIllegalArgumentException");
        } catch (MathIllegalArgumentException ex) {
            
        }
    }

// org.apache.commons.math3.linear.MatrixUtilsTest::testBigFractionConverter
    public void testBigFractionConverter() {
        BigFraction[][] bfData = {
                { new BigFraction(1), new BigFraction(2), new BigFraction(3) },
                { new BigFraction(2), new BigFraction(5), new BigFraction(3) },
                { new BigFraction(1), new BigFraction(0), new BigFraction(8) }
        };
        FieldMatrix<BigFraction> m = new Array2DRowFieldMatrix<BigFraction>(bfData, false);
        RealMatrix converted = MatrixUtils.bigFractionMatrixToRealMatrix(m);
        RealMatrix reference = new Array2DRowRealMatrix(testData, false);
        Assert.assertEquals(0.0, converted.subtract(reference).getNorm(), 0.0);
    }

// org.apache.commons.math3.linear.MatrixUtilsTest::testFractionConverter
    public void testFractionConverter() {
        Fraction[][] fData = {
                { new Fraction(1), new Fraction(2), new Fraction(3) },
                { new Fraction(2), new Fraction(5), new Fraction(3) },
                { new Fraction(1), new Fraction(0), new Fraction(8) }
        };
        FieldMatrix<Fraction> m = new Array2DRowFieldMatrix<Fraction>(fData, false);
        RealMatrix converted = MatrixUtils.fractionMatrixToRealMatrix(m);
        RealMatrix reference = new Array2DRowRealMatrix(testData, false);
        Assert.assertEquals(0.0, converted.subtract(reference).getNorm(), 0.0);
    }

// org.apache.commons.math3.linear.MatrixUtilsTest::testSolveLowerTriangularSystem
    public void testSolveLowerTriangularSystem(){
        RealMatrix rm = new Array2DRowRealMatrix(
                new double[][] { {2,0,0,0 }, { 1,1,0,0 }, { 3,3,3,0 }, { 3,3,3,4 } },
                       false);
        RealVector b = new ArrayRealVector(new double[] { 2,3,4,8 }, false);
        MatrixUtils.solveLowerTriangularSystem(rm, b);
        TestUtils.assertEquals( new double[]{1,2,-1.66666666666667, 1.0}  , b.toArray() , 1.0e-12);
    }

// org.apache.commons.math3.linear.MatrixUtilsTest::testSolveUpperTriangularSystem
    public void testSolveUpperTriangularSystem(){
        RealMatrix rm = new Array2DRowRealMatrix(
                new double[][] { {1,2,3 }, { 0,1,1 }, { 0,0,2 } },
                       false);
        RealVector b = new ArrayRealVector(new double[] { 8,4,2 }, false);
        MatrixUtils.solveUpperTriangularSystem(rm, b);
        TestUtils.assertEquals( new double[]{-1,3,1}  , b.toArray() , 1.0e-12);
    }

// org.apache.commons.math3.linear.MatrixUtilsTest::testBlockInverse
    public void testBlockInverse() {
        final double[][] data = {
            { -1, 0, 123, 4 },
            { -56, 78.9, -0.1, -23.4 },
            { 5.67, 8, -9, 1011 },
            { 12, 345, -67.8, 9 },
        };

        final RealMatrix m = new Array2DRowRealMatrix(data);
        final int len = data.length;
        final double tol = 1e-14;

        for (int splitIndex = 0; splitIndex < 3; splitIndex++) {
            final RealMatrix mInv = MatrixUtils.blockInverse(m, splitIndex);
            final RealMatrix id = m.multiply(mInv);

            
            for (int i = 0; i < len; i++) {
                for (int j = 0; j < len; j++) {
                    final double entry = id.getEntry(i, j);
                    if (i == j) {
                        Assert.assertEquals("[" + i + "][" + j + "]",
                                            1, entry, tol);
                    } else {
                        Assert.assertEquals("[" + i + "][" + j + "]",
                                            0, entry, tol);
                    }
                }
            }
        }
    }

// org.apache.commons.math3.linear.MatrixUtilsTest::testBlockInverseNonInvertible
    public void testBlockInverseNonInvertible() {
        final double[][] data = {
            { -1, 0, 123, 4 },
            { -56, 78.9, -0.1, -23.4 },
            { 5.67, 8, -9, 1011 },
            { 5.67, 8, -9, 1011 },
        };

        MatrixUtils.blockInverse(new Array2DRowRealMatrix(data), 2);
    }

// org.apache.commons.math3.linear.MatrixUtilsTest::testIsSymmetric
    public void testIsSymmetric() {
        final double eps = Math.ulp(1d);

        final double[][] dataSym = {
            { 1, 2, 3 },
            { 2, 2, 5 },
            { 3, 5, 6 },
        };
        Assert.assertTrue(MatrixUtils.isSymmetric(MatrixUtils.createRealMatrix(dataSym), eps));

        final double[][] dataNonSym = {
            { 1, 2, -3 },
            { 2, 2, 5 },
            { 3, 5, 6 },
        };
        Assert.assertFalse(MatrixUtils.isSymmetric(MatrixUtils.createRealMatrix(dataNonSym), eps));
    }

// org.apache.commons.math3.linear.MatrixUtilsTest::testIsSymmetricTolerance
    public void testIsSymmetricTolerance() {
        final double eps = 1e-4;

        final double[][] dataSym1 = {
            { 1,   1, 1.00009 },
            { 1,   1, 1       },
            { 1.0, 1, 1       },
        };
        Assert.assertTrue(MatrixUtils.isSymmetric(MatrixUtils.createRealMatrix(dataSym1), eps));
        final double[][] dataSym2 = {
            { 1,   1, 0.99990 },
            { 1,   1, 1       },
            { 1.0, 1, 1       },
        };
        Assert.assertTrue(MatrixUtils.isSymmetric(MatrixUtils.createRealMatrix(dataSym2), eps));

        final double[][] dataNonSym1 = {
            { 1,   1, 1.00011 },
            { 1,   1, 1       },
            { 1.0, 1, 1       },
        };
        Assert.assertFalse(MatrixUtils.isSymmetric(MatrixUtils.createRealMatrix(dataNonSym1), eps));
        final double[][] dataNonSym2 = {
            { 1,   1, 0.99989 },
            { 1,   1, 1       },
            { 1.0, 1, 1       },
        };
        Assert.assertFalse(MatrixUtils.isSymmetric(MatrixUtils.createRealMatrix(dataNonSym2), eps));
    }

// org.apache.commons.math3.linear.MatrixUtilsTest::testCheckSymmetric1
    public void testCheckSymmetric1() {
        final double[][] dataSym = {
            { 1, 2, 3 },
            { 2, 2, 5 },
            { 3, 5, 6 },
        };
        MatrixUtils.checkSymmetric(MatrixUtils.createRealMatrix(dataSym), Math.ulp(1d));
    }

// org.apache.commons.math3.linear.MatrixUtilsTest::testCheckSymmetric2
    public void testCheckSymmetric2() {
        final double[][] dataNonSym = {
            { 1, 2, -3 },
            { 2, 2, 5 },
            { 3, 5, 6 },
        };
        MatrixUtils.checkSymmetric(MatrixUtils.createRealMatrix(dataNonSym), Math.ulp(1d));
    }

// org.apache.commons.math3.linear.RealVectorTest::testAppendVector
    public void testAppendVector() {
        
    }

// org.apache.commons.math3.linear.RealVectorTest::testAppendScalar
    public void testAppendScalar() {
        
    }

// org.apache.commons.math3.linear.RealVectorTest::testGetSubVector
    public void testGetSubVector() {
        
    }

// org.apache.commons.math3.linear.RealVectorTest::testGetSubVectorInvalidIndex1
    public void testGetSubVectorInvalidIndex1() {
        
    }

// org.apache.commons.math3.linear.RealVectorTest::testGetSubVectorInvalidIndex2
    public void testGetSubVectorInvalidIndex2() {
        
    }

// org.apache.commons.math3.linear.RealVectorTest::testGetSubVectorInvalidIndex3
    public void testGetSubVectorInvalidIndex3() {
        
    }

// org.apache.commons.math3.linear.RealVectorTest::testGetSubVectorInvalidIndex4
    public void testGetSubVectorInvalidIndex4() {
        
    }

// org.apache.commons.math3.linear.RealVectorTest::testSetSubVectorSameType
    public void testSetSubVectorSameType() {
        
    }

// org.apache.commons.math3.linear.RealVectorTest::testSetSubVectorMixedType
    public void testSetSubVectorMixedType() {
        
    }

// org.apache.commons.math3.linear.RealVectorTest::testSetSubVectorInvalidIndex1
    public void testSetSubVectorInvalidIndex1() {
        
    }

// org.apache.commons.math3.linear.RealVectorTest::testSetSubVectorInvalidIndex2
    public void testSetSubVectorInvalidIndex2() {
        
    }

// org.apache.commons.math3.linear.RealVectorTest::testSetSubVectorInvalidIndex3
    public void testSetSubVectorInvalidIndex3() {
        
    }

// org.apache.commons.math3.linear.RealVectorTest::testIsNaN
    public void testIsNaN() {
        
    }

// org.apache.commons.math3.linear.RealVectorTest::testIsInfinite
    public void testIsInfinite() {
        
    }

// org.apache.commons.math3.linear.RealVectorTest::testEbeMultiplySameType
    public void testEbeMultiplySameType() {
        
    }

// org.apache.commons.math3.linear.RealVectorTest::testEbeMultiplyMixedTypes
    public void testEbeMultiplyMixedTypes() {
        
    }

// org.apache.commons.math3.linear.RealVectorTest::testEbeMultiplyDimensionMismatch
    public void testEbeMultiplyDimensionMismatch() {
        
    }

// org.apache.commons.math3.linear.RealVectorTest::testEbeDivideSameType
    public void testEbeDivideSameType() {
        
    }

// org.apache.commons.math3.linear.RealVectorTest::testEbeDivideMixedTypes
    public void testEbeDivideMixedTypes() {
        
    }

// org.apache.commons.math3.linear.RealVectorTest::testEbeDivideDimensionMismatch
    public void testEbeDivideDimensionMismatch() {
        
    }

// org.apache.commons.math3.linear.RealVectorTest::testGetL1Norm
    public void testGetL1Norm() {
        
    }

// org.apache.commons.math3.linear.RealVectorTest::testGetLInfNorm
    public void testGetLInfNorm() {
        
    }

// org.apache.commons.math3.linear.RealVectorTest::testSparseIterator
    public void testSparseIterator() {
        
        final double x = getPreferredEntryValue();
        final double[] data = {
            x, x + 1d, x, x, x + 2d, x + 3d, x + 4d, x, x, x, x + 5d, x + 6d, x
        };

        RealVector v = create(data);
        Entry e;
        int i = 0;
        final double[] nonDefault = {
            x + 1d, x + 2d, x + 3d, x + 4d, x + 5d, x + 6d
        };
        for (Iterator<Entry> it = v.sparseIterator(); it.hasNext(); i++) {
            e = it.next();
            Assert.assertEquals(nonDefault[i], e.getValue(), 0);
        }
        double [] onlyOne = {x, x + 1d, x};
        v = create(onlyOne);
        for(Iterator<Entry> it = v.sparseIterator(); it.hasNext(); ) {
            e = it.next();
            Assert.assertEquals(onlyOne[1], e.getValue(), 0);
        }
    }

// org.apache.commons.math3.linear.RealVectorTest::testSerial
    public void testSerial() {
        
    }

// org.apache.commons.math3.linear.RealVectorTest::testEquals
    public void testEquals() {
        
    }

// org.apache.commons.math3.linear.SparseFieldMatrixTest::testDimensions
    public void testDimensions() {
        SparseFieldMatrix<Fraction> m = createSparseMatrix(testData);
        SparseFieldMatrix<Fraction> m2 = createSparseMatrix(testData2);
        Assert.assertEquals("testData row dimension", 3, m.getRowDimension());
        Assert.assertEquals("testData column dimension", 3, m.getColumnDimension());
        Assert.assertTrue("testData is square", m.isSquare());
        Assert.assertEquals("testData2 row dimension", m2.getRowDimension(), 2);
        Assert.assertEquals("testData2 column dimension", m2.getColumnDimension(), 3);
        Assert.assertTrue("testData2 is not square", !m2.isSquare());
    }

// org.apache.commons.math3.linear.SparseFieldMatrixTest::testCopyFunctions
    public void testCopyFunctions() {
        SparseFieldMatrix<Fraction> m1 = createSparseMatrix(testData);
        FieldMatrix<Fraction> m2 = m1.copy();
        Assert.assertEquals(m1.getClass(), m2.getClass());
        Assert.assertEquals((m2), m1);
        SparseFieldMatrix<Fraction> m3 = createSparseMatrix(testData);
        FieldMatrix<Fraction> m4 = m3.copy();
        Assert.assertEquals(m3.getClass(), m4.getClass());
        Assert.assertEquals((m4), m3);
    }

// org.apache.commons.math3.linear.SparseFieldMatrixTest::testAdd
    public void testAdd() {
        SparseFieldMatrix<Fraction> m = createSparseMatrix(testData);
        SparseFieldMatrix<Fraction> mInv = createSparseMatrix(testDataInv);
        SparseFieldMatrix<Fraction> mDataPlusInv = createSparseMatrix(testDataPlusInv);
        FieldMatrix<Fraction> mPlusMInv = m.add(mInv);
        for (int row = 0; row < m.getRowDimension(); row++) {
            for (int col = 0; col < m.getColumnDimension(); col++) {
                Assert.assertEquals("sum entry entry",
                    mDataPlusInv.getEntry(row, col).doubleValue(), mPlusMInv.getEntry(row, col).doubleValue(),
                    entryTolerance);
            }
        }
    }

// org.apache.commons.math3.linear.SparseFieldMatrixTest::testAddFail
    public void testAddFail() {
        SparseFieldMatrix<Fraction> m = createSparseMatrix(testData);
        SparseFieldMatrix<Fraction> m2 = createSparseMatrix(testData2);
        try {
            m.add(m2);
            Assert.fail("MathIllegalArgumentException expected");
        } catch (MathIllegalArgumentException ex) {
            
        }
    }

// org.apache.commons.math3.linear.SparseFieldMatrixTest::testPlusMinus
    public void testPlusMinus() {
        SparseFieldMatrix<Fraction> m = createSparseMatrix(testData);
        SparseFieldMatrix<Fraction> n = createSparseMatrix(testDataInv);
        assertClose("m-n = m + -n", m.subtract(n),
            n.scalarMultiply(new Fraction(-1)).add(m), entryTolerance);
        try {
            m.subtract(createSparseMatrix(testData2));
            Assert.fail("Expecting illegalArgumentException");
        } catch (MathIllegalArgumentException ex) {
            
        }
    }

// org.apache.commons.math3.linear.SparseFieldMatrixTest::testMultiply
    public void testMultiply() {
        SparseFieldMatrix<Fraction> m = createSparseMatrix(testData);
        SparseFieldMatrix<Fraction> mInv = createSparseMatrix(testDataInv);
        SparseFieldMatrix<Fraction> identity = createSparseMatrix(id);
        SparseFieldMatrix<Fraction> m2 = createSparseMatrix(testData2);
        assertClose("inverse multiply", m.multiply(mInv), identity,
                entryTolerance);
        assertClose("inverse multiply", m.multiply(new Array2DRowFieldMatrix<Fraction>(FractionField.getInstance(), testDataInv)), identity,
                    entryTolerance);
        assertClose("inverse multiply", mInv.multiply(m), identity,
                entryTolerance);
        assertClose("identity multiply", m.multiply(identity), m,
                entryTolerance);
        assertClose("identity multiply", identity.multiply(mInv), mInv,
                entryTolerance);
        assertClose("identity multiply", m2.multiply(identity), m2,
                entryTolerance);
        try {
            m.multiply(createSparseMatrix(bigSingular));
            Assert.fail("Expecting illegalArgumentException");
        } catch (MathIllegalArgumentException ex) {
            
        }
    }

// org.apache.commons.math3.linear.SparseFieldMatrixTest::testMultiply2
    public void testMultiply2() {
        FieldMatrix<Fraction> m3 = createSparseMatrix(d3);
        FieldMatrix<Fraction> m4 = createSparseMatrix(d4);
        FieldMatrix<Fraction> m5 = createSparseMatrix(d5);
        assertClose("m3*m4=m5", m3.multiply(m4), m5, entryTolerance);
    }

// org.apache.commons.math3.linear.SparseFieldMatrixTest::testTrace
    public void testTrace() {
        FieldMatrix<Fraction> m = createSparseMatrix(id);
        Assert.assertEquals("identity trace", 3d, m.getTrace().doubleValue(), entryTolerance);
        m = createSparseMatrix(testData2);
        try {
            m.getTrace();
            Assert.fail("Expecting NonSquareMatrixException");
        } catch (NonSquareMatrixException ex) {
            
        }
    }

// org.apache.commons.math3.linear.SparseFieldMatrixTest::testScalarAdd
    public void testScalarAdd() {
        FieldMatrix<Fraction> m = createSparseMatrix(testData);
        assertClose("scalar add", createSparseMatrix(testDataPlus2),
            m.scalarAdd(new Fraction(2)), entryTolerance);
    }

// org.apache.commons.math3.linear.SparseFieldMatrixTest::testOperate
    public void testOperate() {
        FieldMatrix<Fraction> m = createSparseMatrix(id);
        assertClose("identity operate", testVector, m.operate(testVector),
                entryTolerance);
        assertClose("identity operate", testVector, m.operate(
                new ArrayFieldVector<Fraction>(testVector)).getData(), entryTolerance);
        m = createSparseMatrix(bigSingular);
        try {
            m.operate(testVector);
            Assert.fail("Expecting illegalArgumentException");
        } catch (MathIllegalArgumentException ex) {
            
        }
    }

// org.apache.commons.math3.linear.SparseFieldMatrixTest::testMath209
    public void testMath209() {
        FieldMatrix<Fraction> a = createSparseMatrix(new Fraction[][] {
                { new Fraction(1), new Fraction(2) }, { new Fraction(3), new Fraction(4) }, { new Fraction(5), new Fraction(6) } });
        Fraction[] b = a.operate(new Fraction[] { new Fraction(1), new Fraction(1) });
        Assert.assertEquals(a.getRowDimension(), b.length);
        Assert.assertEquals(3.0, b[0].doubleValue(), 1.0e-12);
        Assert.assertEquals(7.0, b[1].doubleValue(), 1.0e-12);
        Assert.assertEquals(11.0, b[2].doubleValue(), 1.0e-12);
    }

// org.apache.commons.math3.linear.SparseFieldMatrixTest::testTranspose
    public void testTranspose() {
        FieldMatrix<Fraction> m = createSparseMatrix(testData);
        FieldMatrix<Fraction> mIT = new FieldLUDecomposition<Fraction>(m).getSolver().getInverse().transpose();
        FieldMatrix<Fraction> mTI = new FieldLUDecomposition<Fraction>(m.transpose()).getSolver().getInverse();
        assertClose("inverse-transpose", mIT, mTI, normTolerance);
        m = createSparseMatrix(testData2);
        FieldMatrix<Fraction> mt = createSparseMatrix(testData2T);
        assertClose("transpose",mt,m.transpose(),normTolerance);
    }

// org.apache.commons.math3.linear.SparseFieldMatrixTest::testPremultiplyVector
    public void testPremultiplyVector() {
        FieldMatrix<Fraction> m = createSparseMatrix(testData);
        assertClose("premultiply", m.preMultiply(testVector), preMultTest,
            normTolerance);
        assertClose("premultiply", m.preMultiply(
            new ArrayFieldVector<Fraction>(testVector).getData()), preMultTest, normTolerance);
        m = createSparseMatrix(bigSingular);
        try {
            m.preMultiply(testVector);
            Assert.fail("expecting MathIllegalArgumentException");
        } catch (MathIllegalArgumentException ex) {
            
        }
    }

// org.apache.commons.math3.linear.SparseFieldMatrixTest::testPremultiply
    public void testPremultiply() {
        FieldMatrix<Fraction> m3 = createSparseMatrix(d3);
        FieldMatrix<Fraction> m4 = createSparseMatrix(d4);
        FieldMatrix<Fraction> m5 = createSparseMatrix(d5);
        assertClose("m3*m4=m5", m4.preMultiply(m3), m5, entryTolerance);

        SparseFieldMatrix<Fraction> m = createSparseMatrix(testData);
        SparseFieldMatrix<Fraction> mInv = createSparseMatrix(testDataInv);
        SparseFieldMatrix<Fraction> identity = createSparseMatrix(id);
        assertClose("inverse multiply", m.preMultiply(mInv), identity,
                entryTolerance);
        assertClose("inverse multiply", mInv.preMultiply(m), identity,
                entryTolerance);
        assertClose("identity multiply", m.preMultiply(identity), m,
                entryTolerance);
        assertClose("identity multiply", identity.preMultiply(mInv), mInv,
                entryTolerance);
        try {
            m.preMultiply(createSparseMatrix(bigSingular));
            Assert.fail("Expecting illegalArgumentException");
        } catch (MathIllegalArgumentException ex) {
            
        }
    }

// org.apache.commons.math3.linear.SparseFieldMatrixTest::testGetVectors
    public void testGetVectors() {
        FieldMatrix<Fraction> m = createSparseMatrix(testData);
        assertClose("get row", m.getRow(0), testDataRow1, entryTolerance);
        assertClose("get col", m.getColumn(2), testDataCol3, entryTolerance);
        try {
            m.getRow(10);
            Assert.fail("expecting OutOfRangeException");
        } catch (OutOfRangeException ex) {
            
        }
        try {
            m.getColumn(-1);
            Assert.fail("expecting OutOfRangeException");
        } catch (OutOfRangeException ex) {
            
        }
    }

// org.apache.commons.math3.linear.SparseFieldMatrixTest::testGetEntry
    public void testGetEntry() {
        FieldMatrix<Fraction> m = createSparseMatrix(testData);
        Assert.assertEquals("get entry", m.getEntry(0, 1).doubleValue(), 2d, entryTolerance);
        try {
            m.getEntry(10, 4);
            Assert.fail("Expecting OutOfRangeException");
        } catch (OutOfRangeException ex) {
            
        }
    }

// org.apache.commons.math3.linear.SparseFieldMatrixTest::testExamples
    public void testExamples() {
        
        Fraction[][] matrixData = { { new Fraction(1), new Fraction(2), new Fraction(3) }, { new Fraction(2), new Fraction(5), new Fraction(3) } };
        FieldMatrix<Fraction> m = createSparseMatrix(matrixData);
        
        Fraction[][] matrixData2 = { { new Fraction(1), new Fraction(2) }, { new Fraction(2), new Fraction(5) }, { new Fraction(1), new Fraction(7) } };
        FieldMatrix<Fraction> n = createSparseMatrix(matrixData2);
        
        FieldMatrix<Fraction> p = m.multiply(n);
        Assert.assertEquals(2, p.getRowDimension());
        Assert.assertEquals(2, p.getColumnDimension());
        
        FieldMatrix<Fraction> pInverse = new FieldLUDecomposition<Fraction>(p).getSolver().getInverse();
        Assert.assertEquals(2, pInverse.getRowDimension());
        Assert.assertEquals(2, pInverse.getColumnDimension());

        
        Fraction[][] coefficientsData = { { new Fraction(2), new Fraction(3), new Fraction(-2) }, { new Fraction(-1), new Fraction(7), new Fraction(6) },
                { new Fraction(4), new Fraction(-3), new Fraction(-5) } };
        FieldMatrix<Fraction> coefficients = createSparseMatrix(coefficientsData);
        Fraction[] constants = { new Fraction(1), new Fraction(-2), new Fraction(1) };
        Fraction[] solution;
        solution = new FieldLUDecomposition<Fraction>(coefficients)
            .getSolver()
            .solve(new ArrayFieldVector<Fraction>(constants, false)).toArray();
        Assert.assertEquals((new Fraction(2).multiply((solution[0])).add(new Fraction(3).multiply(solution[1])).subtract(new Fraction(2).multiply(solution[2]))).doubleValue(),
                constants[0].doubleValue(), 1E-12);
        Assert.assertEquals(((new Fraction(-1).multiply(solution[0])).add(new Fraction(7).multiply(solution[1])).add(new Fraction(6).multiply(solution[2]))).doubleValue(),
                constants[1].doubleValue(), 1E-12);
        Assert.assertEquals(((new Fraction(4).multiply(solution[0])).subtract(new Fraction(3).multiply( solution[1])).subtract(new Fraction(5).multiply(solution[2]))).doubleValue(),
                constants[2].doubleValue(), 1E-12);

    }

// org.apache.commons.math3.linear.SparseFieldMatrixTest::testSubMatrix
    public void testSubMatrix() {
        FieldMatrix<Fraction> m = createSparseMatrix(subTestData);
        FieldMatrix<Fraction> mRows23Cols00 = createSparseMatrix(subRows23Cols00);
        FieldMatrix<Fraction> mRows00Cols33 = createSparseMatrix(subRows00Cols33);
        FieldMatrix<Fraction> mRows01Cols23 = createSparseMatrix(subRows01Cols23);
        FieldMatrix<Fraction> mRows02Cols13 = createSparseMatrix(subRows02Cols13);
        FieldMatrix<Fraction> mRows03Cols12 = createSparseMatrix(subRows03Cols12);
        FieldMatrix<Fraction> mRows03Cols123 = createSparseMatrix(subRows03Cols123);
        FieldMatrix<Fraction> mRows20Cols123 = createSparseMatrix(subRows20Cols123);
        FieldMatrix<Fraction> mRows31Cols31 = createSparseMatrix(subRows31Cols31);
        Assert.assertEquals("Rows23Cols00", mRows23Cols00, m.getSubMatrix(2, 3, 0, 0));
        Assert.assertEquals("Rows00Cols33", mRows00Cols33, m.getSubMatrix(0, 0, 3, 3));
        Assert.assertEquals("Rows01Cols23", mRows01Cols23, m.getSubMatrix(0, 1, 2, 3));
        Assert.assertEquals("Rows02Cols13", mRows02Cols13,
            m.getSubMatrix(new int[] { 0, 2 }, new int[] { 1, 3 }));
        Assert.assertEquals("Rows03Cols12", mRows03Cols12,
            m.getSubMatrix(new int[] { 0, 3 }, new int[] { 1, 2 }));
        Assert.assertEquals("Rows03Cols123", mRows03Cols123,
            m.getSubMatrix(new int[] { 0, 3 }, new int[] { 1, 2, 3 }));
        Assert.assertEquals("Rows20Cols123", mRows20Cols123,
            m.getSubMatrix(new int[] { 2, 0 }, new int[] { 1, 2, 3 }));
        Assert.assertEquals("Rows31Cols31", mRows31Cols31,
            m.getSubMatrix(new int[] { 3, 1 }, new int[] { 3, 1 }));
        Assert.assertEquals("Rows31Cols31", mRows31Cols31,
            m.getSubMatrix(new int[] { 3, 1 }, new int[] { 3, 1 }));

        try {
            m.getSubMatrix(1, 0, 2, 4);
            Assert.fail("Expecting NumberIsTooSmallException");
        } catch (NumberIsTooSmallException ex) {
            
        }
        try {
            m.getSubMatrix(-1, 1, 2, 2);
            Assert.fail("Expecting OutOfRangeException");
        } catch (OutOfRangeException ex) {
            
        }
        try {
            m.getSubMatrix(1, 0, 2, 2);
            Assert.fail("Expecting NumberIsTooSmallException");
        } catch (NumberIsTooSmallException ex) {
            
        }
        try {
            m.getSubMatrix(1, 0, 2, 4);
            Assert.fail("Expecting NumberIsTooSmallException");
        } catch (NumberIsTooSmallException ex) {
            
        }
        try {
            m.getSubMatrix(new int[] {}, new int[] { 0 });
            Assert.fail("Expecting NoDataException");
        } catch (NoDataException ex) {
            
        }
        try {
            m.getSubMatrix(new int[] { 0 }, new int[] { 4 });
            Assert.fail("Expecting OutOfRangeException");
        } catch (OutOfRangeException ex) {
            
        }
    }

// org.apache.commons.math3.linear.SparseFieldMatrixTest::testGetRowMatrix
    public void testGetRowMatrix() {
        FieldMatrix<Fraction> m = createSparseMatrix(subTestData);
        FieldMatrix<Fraction> mRow0 = createSparseMatrix(subRow0);
        FieldMatrix<Fraction> mRow3 = createSparseMatrix(subRow3);
        Assert.assertEquals("Row0", mRow0, m.getRowMatrix(0));
        Assert.assertEquals("Row3", mRow3, m.getRowMatrix(3));
        try {
            m.getRowMatrix(-1);
            Assert.fail("Expecting OutOfRangeException");
        } catch (OutOfRangeException ex) {
            
        }
        try {
            m.getRowMatrix(4);
            Assert.fail("Expecting OutOfRangeException");
        } catch (OutOfRangeException ex) {
            
        }
    }

// org.apache.commons.math3.linear.SparseFieldMatrixTest::testGetColumnMatrix
    public void testGetColumnMatrix() {
        FieldMatrix<Fraction> m = createSparseMatrix(subTestData);
        FieldMatrix<Fraction> mColumn1 = createSparseMatrix(subColumn1);
        FieldMatrix<Fraction> mColumn3 = createSparseMatrix(subColumn3);
        Assert.assertEquals("Column1", mColumn1, m.getColumnMatrix(1));
        Assert.assertEquals("Column3", mColumn3, m.getColumnMatrix(3));
        try {
            m.getColumnMatrix(-1);
            Assert.fail("Expecting OutOfRangeException");
        } catch (OutOfRangeException ex) {
            
        }
        try {
            m.getColumnMatrix(4);
            Assert.fail("Expecting OutOfRangeException");
        } catch (OutOfRangeException ex) {
            
        }
    }

// org.apache.commons.math3.linear.SparseFieldMatrixTest::testGetRowVector
    public void testGetRowVector() {
        FieldMatrix<Fraction> m = createSparseMatrix(subTestData);
        FieldVector<Fraction> mRow0 = new ArrayFieldVector<Fraction>(subRow0[0]);
        FieldVector<Fraction> mRow3 = new ArrayFieldVector<Fraction>(subRow3[0]);
        Assert.assertEquals("Row0", mRow0, m.getRowVector(0));
        Assert.assertEquals("Row3", mRow3, m.getRowVector(3));
        try {
            m.getRowVector(-1);
            Assert.fail("Expecting OutOfRangeException");
        } catch (OutOfRangeException ex) {
            
        }
        try {
            m.getRowVector(4);
            Assert.fail("Expecting OutOfRangeException");
        } catch (OutOfRangeException ex) {
            
        }
    }

// org.apache.commons.math3.linear.SparseFieldMatrixTest::testGetColumnVector
    public void testGetColumnVector() {
        FieldMatrix<Fraction> m = createSparseMatrix(subTestData);
        FieldVector<Fraction> mColumn1 = columnToVector(subColumn1);
        FieldVector<Fraction> mColumn3 = columnToVector(subColumn3);
        Assert.assertEquals("Column1", mColumn1, m.getColumnVector(1));
        Assert.assertEquals("Column3", mColumn3, m.getColumnVector(3));
        try {
            m.getColumnVector(-1);
            Assert.fail("Expecting OutOfRangeException");
        } catch (OutOfRangeException ex) {
            
        }
        try {
            m.getColumnVector(4);
            Assert.fail("Expecting OutOfRangeException");
        } catch (OutOfRangeException ex) {
            
        }
    }

// org.apache.commons.math3.linear.SparseFieldMatrixTest::testEqualsAndHashCode
    public void testEqualsAndHashCode() {
        SparseFieldMatrix<Fraction> m = createSparseMatrix(testData);
        SparseFieldMatrix<Fraction> m1 = (SparseFieldMatrix<Fraction>) m.copy();
        SparseFieldMatrix<Fraction> mt = (SparseFieldMatrix<Fraction>) m.transpose();
        Assert.assertTrue(m.hashCode() != mt.hashCode());
        Assert.assertEquals(m.hashCode(), m1.hashCode());
        Assert.assertEquals(m, m);
        Assert.assertEquals(m, m1);
        Assert.assertFalse(m.equals(null));
        Assert.assertFalse(m.equals(mt));
        Assert.assertFalse(m.equals(createSparseMatrix(bigSingular)));
    }

// org.apache.commons.math3.linear.SparseFieldMatrixTest::testSetSubMatrix
    public void testSetSubMatrix() {
        SparseFieldMatrix<Fraction> m = createSparseMatrix(testData);
        m.setSubMatrix(detData2, 1, 1);
        FieldMatrix<Fraction> expected = createSparseMatrix(new Fraction[][] {
                { new Fraction(1), new Fraction(2), new Fraction(3) }, { new Fraction(2), new Fraction(1), new Fraction(3) }, { new Fraction(1), new Fraction(2), new Fraction(4) } });
        Assert.assertEquals(expected, m);

        m.setSubMatrix(detData2, 0, 0);
        expected = createSparseMatrix(new Fraction[][] {
                { new Fraction(1), new Fraction(3), new Fraction(3) }, { new Fraction(2), new Fraction(4), new Fraction(3) }, { new Fraction(1), new Fraction(2), new Fraction(4) } });
        Assert.assertEquals(expected, m);

        m.setSubMatrix(testDataPlus2, 0, 0);
        expected = createSparseMatrix(new Fraction[][] {
                { new Fraction(3), new Fraction(4), new Fraction(5) }, { new Fraction(4), new Fraction(7), new Fraction(5) }, { new Fraction(3), new Fraction(2), new Fraction(10) } });
        Assert.assertEquals(expected, m);

        
        SparseFieldMatrix<Fraction> matrix =
            createSparseMatrix(new Fraction[][] {
        { new Fraction(1), new Fraction(2), new Fraction(3), new Fraction(4) }, { new Fraction(5), new Fraction(6), new Fraction(7), new Fraction(8) }, { new Fraction(9), new Fraction(0), new Fraction(1), new Fraction(2) } });
        matrix.setSubMatrix(new Fraction[][] { { new Fraction(3), new Fraction(4) }, { new Fraction(5), new Fraction(6) } }, 1, 1);
        expected = createSparseMatrix(new Fraction[][] {
                { new Fraction(1), new Fraction(2), new Fraction(3), new Fraction(4) }, { new Fraction(5), new Fraction(3), new Fraction(4), new Fraction(8) }, { new Fraction(9), new Fraction(5), new Fraction(6), new Fraction(2) } });
        Assert.assertEquals(expected, matrix);

        
        try {
            m.setSubMatrix(testData, 1, 1);
            Assert.fail("expecting OutOfRangeException");
        } catch (OutOfRangeException e) {
            
        }
        
        try {
            m.setSubMatrix(testData, -1, 1);
            Assert.fail("expecting OutOfRangeException");
        } catch (OutOfRangeException e) {
            
        }
        try {
            m.setSubMatrix(testData, 1, -1);
            Assert.fail("expecting OutOfRangeException");
        } catch (OutOfRangeException e) {
            
        }

        
        try {
            m.setSubMatrix(null, 1, 1);
            Assert.fail("expecting NullArgumentException");
        } catch (NullArgumentException e) {
            
        }
        try {
            new SparseFieldMatrix<Fraction>(field, 0, 0);
            Assert.fail("expecting MathIllegalArgumentException");
        } catch (MathIllegalArgumentException e) {
            
        }

        
        try {
            m.setSubMatrix(new Fraction[][] { { new Fraction(1) }, { new Fraction(2), new Fraction(3) } }, 0, 0);
            Assert.fail("expecting MathIllegalArgumentException");
        } catch (MathIllegalArgumentException e) {
            
        }

        
        try {
            m.setSubMatrix(new Fraction[][] { {} }, 0, 0);
            Assert.fail("expecting MathIllegalArgumentException");
        } catch (MathIllegalArgumentException e) {
            
        }
    }

// org.apache.commons.math3.linear.SparseFieldVectorTest::testMapFunctions
    public void testMapFunctions() throws FractionConversionException {
        SparseFieldVector<Fraction> v1 = new SparseFieldVector<Fraction>(field,vec1);

        
        FieldVector<Fraction> v_mapAdd = v1.mapAdd(new Fraction(2));
        Fraction[] result_mapAdd = {new Fraction(3), new Fraction(4), new Fraction(5)};
        Assert.assertArrayEquals("compare vectors" ,result_mapAdd,v_mapAdd.getData());

        
        FieldVector<Fraction> v_mapAddToSelf = v1.copy();
        v_mapAddToSelf.mapAddToSelf(new Fraction(2));
        Fraction[] result_mapAddToSelf = {new Fraction(3), new Fraction(4), new Fraction(5)};
        Assert.assertArrayEquals("compare vectors" ,result_mapAddToSelf,v_mapAddToSelf.getData());

        
        FieldVector<Fraction> v_mapSubtract = v1.mapSubtract(new Fraction(2));
        Fraction[] result_mapSubtract = {new Fraction(-1), new Fraction(0), new Fraction(1)};
        Assert.assertArrayEquals("compare vectors" ,result_mapSubtract,v_mapSubtract.getData());

        
        FieldVector<Fraction> v_mapSubtractToSelf = v1.copy();
        v_mapSubtractToSelf.mapSubtractToSelf(new Fraction(2));
        Fraction[] result_mapSubtractToSelf = {new Fraction(-1), new Fraction(0), new Fraction(1)};
        Assert.assertArrayEquals("compare vectors" ,result_mapSubtractToSelf,v_mapSubtractToSelf.getData());

        
        FieldVector<Fraction> v_mapMultiply = v1.mapMultiply(new Fraction(2));
        Fraction[] result_mapMultiply = {new Fraction(2), new Fraction(4), new Fraction(6)};
        Assert.assertArrayEquals("compare vectors" ,result_mapMultiply,v_mapMultiply.getData());

        
        FieldVector<Fraction> v_mapMultiplyToSelf = v1.copy();
        v_mapMultiplyToSelf.mapMultiplyToSelf(new Fraction(2));
        Fraction[] result_mapMultiplyToSelf = {new Fraction(2), new Fraction(4), new Fraction(6)};
        Assert.assertArrayEquals("compare vectors" ,result_mapMultiplyToSelf,v_mapMultiplyToSelf.getData());

        
        FieldVector<Fraction> v_mapDivide = v1.mapDivide(new Fraction(2));
        Fraction[] result_mapDivide = {new Fraction(.5d), new Fraction(1), new Fraction(1.5d)};
        Assert.assertArrayEquals("compare vectors" ,result_mapDivide,v_mapDivide.getData());

        
        FieldVector<Fraction> v_mapDivideToSelf = v1.copy();
        v_mapDivideToSelf.mapDivideToSelf(new Fraction(2));
        Fraction[] result_mapDivideToSelf = {new Fraction(.5d), new Fraction(1), new Fraction(1.5d)};
        Assert.assertArrayEquals("compare vectors" ,result_mapDivideToSelf,v_mapDivideToSelf.getData());

        
        FieldVector<Fraction> v_mapInv = v1.mapInv();
        Fraction[] result_mapInv = {new Fraction(1),new Fraction(0.5d),new Fraction(3.333333333333333e-01d)};
        Assert.assertArrayEquals("compare vectors" ,result_mapInv,v_mapInv.getData());

        
        FieldVector<Fraction> v_mapInvToSelf = v1.copy();
        v_mapInvToSelf.mapInvToSelf();
        Fraction[] result_mapInvToSelf = {new Fraction(1),new Fraction(0.5d),new Fraction(3.333333333333333e-01d)};
        Assert.assertArrayEquals("compare vectors" ,result_mapInvToSelf,v_mapInvToSelf.getData());

    }

// org.apache.commons.math3.linear.SparseFieldVectorTest::testBasicFunctions
    public void testBasicFunctions() throws FractionConversionException {
        SparseFieldVector<Fraction> v1 = new SparseFieldVector<Fraction>(field,vec1);
        SparseFieldVector<Fraction> v2 = new SparseFieldVector<Fraction>(field,vec2);

        FieldVector<Fraction> v2_t = new ArrayFieldVectorTest.FieldVectorTestImpl<Fraction>(vec2);

        
        FieldVector<Fraction> v_add = v1.add(v2);
        Fraction[] result_add = {new Fraction(5), new Fraction(7), new Fraction(9)};
        Assert.assertArrayEquals("compare vect" ,v_add.getData(),result_add);

        FieldVector<Fraction> vt2 = new ArrayFieldVectorTest.FieldVectorTestImpl<Fraction>(vec2);
        FieldVector<Fraction> v_add_i = v1.add(vt2);
        Fraction[] result_add_i = {new Fraction(5), new Fraction(7), new Fraction(9)};
        Assert.assertArrayEquals("compare vect" ,v_add_i.getData(),result_add_i);

        
        SparseFieldVector<Fraction> v_subtract = v1.subtract(v2);
        Fraction[] result_subtract = {new Fraction(-3), new Fraction(-3), new Fraction(-3)};
        assertClose("compare vect" ,v_subtract.getData(),result_subtract,normTolerance);

        FieldVector<Fraction> v_subtract_i = v1.subtract(vt2);
        Fraction[] result_subtract_i = {new Fraction(-3), new Fraction(-3), new Fraction(-3)};
        assertClose("compare vect" ,v_subtract_i.getData(),result_subtract_i,normTolerance);

        
        FieldVector<Fraction>  v_ebeMultiply = v1.ebeMultiply(v2);
        Fraction[] result_ebeMultiply = {new Fraction(4), new Fraction(10), new Fraction(18)};
        assertClose("compare vect" ,v_ebeMultiply.getData(),result_ebeMultiply,normTolerance);

        FieldVector<Fraction>  v_ebeMultiply_2 = v1.ebeMultiply(v2_t);
        Fraction[] result_ebeMultiply_2 = {new Fraction(4), new Fraction(10), new Fraction(18)};
        assertClose("compare vect" ,v_ebeMultiply_2.getData(),result_ebeMultiply_2,normTolerance);

        
        FieldVector<Fraction>  v_ebeDivide = v1.ebeDivide(v2);
        Fraction[] result_ebeDivide = {new Fraction(0.25d), new Fraction(0.4d), new Fraction(0.5d)};
        assertClose("compare vect" ,v_ebeDivide.getData(),result_ebeDivide,normTolerance);

        FieldVector<Fraction>  v_ebeDivide_2 = v1.ebeDivide(v2_t);
        Fraction[] result_ebeDivide_2 = {new Fraction(0.25d), new Fraction(0.4d), new Fraction(0.5d)};
        assertClose("compare vect" ,v_ebeDivide_2.getData(),result_ebeDivide_2,normTolerance);

        
        Fraction dot =  v1.dotProduct(v2);
        Assert.assertEquals("compare val ",new Fraction(32), dot);

        
        Fraction dot_2 =  v1.dotProduct(v2_t);
        Assert.assertEquals("compare val ",new Fraction(32), dot_2);

        FieldMatrix<Fraction> m_outerProduct = v1.outerProduct(v2);
        Assert.assertEquals("compare val ",new Fraction(4), m_outerProduct.getEntry(0,0));

        FieldMatrix<Fraction> m_outerProduct_2 = v1.outerProduct(v2_t);
        Assert.assertEquals("compare val ",new Fraction(4), m_outerProduct_2.getEntry(0,0));

    }

// org.apache.commons.math3.linear.SparseFieldVectorTest::testOuterProduct
    public void testOuterProduct() {
        final SparseFieldVector<Fraction> u
            = new SparseFieldVector<Fraction>(FractionField.getInstance(),
                                              new Fraction[] {new Fraction(1),
                                                              new Fraction(2),
                                                              new Fraction(-3)});
        final SparseFieldVector<Fraction> v
            = new SparseFieldVector<Fraction>(FractionField.getInstance(),
                                              new Fraction[] {new Fraction(4),
                                                              new Fraction(-2)});

        final FieldMatrix<Fraction> uv = u.outerProduct(v);

        final double tol = Math.ulp(1d);
        Assert.assertEquals(new Fraction(4).doubleValue(), uv.getEntry(0, 0).doubleValue(), tol);
        Assert.assertEquals(new Fraction(-2).doubleValue(), uv.getEntry(0, 1).doubleValue(), tol);
        Assert.assertEquals(new Fraction(8).doubleValue(), uv.getEntry(1, 0).doubleValue(), tol);
        Assert.assertEquals(new Fraction(-4).doubleValue(), uv.getEntry(1, 1).doubleValue(), tol);
        Assert.assertEquals(new Fraction(-12).doubleValue(), uv.getEntry(2, 0).doubleValue(), tol);
        Assert.assertEquals(new Fraction(6).doubleValue(), uv.getEntry(2, 1).doubleValue(), tol);
    }

// org.apache.commons.math3.linear.SparseFieldVectorTest::testMisc
    public void testMisc() {
        SparseFieldVector<Fraction> v1 = new SparseFieldVector<Fraction>(field,vec1);

        String out1 = v1.toString();
        Assert.assertTrue("some output ",  out1.length()!=0);
        try {
            v1.checkVectorDimensions(2);
            Assert.fail("MathIllegalArgumentException expected");
        } catch (MathIllegalArgumentException ex) {
            
        }

    }

// org.apache.commons.math3.linear.SparseFieldVectorTest::testPredicates
    public void testPredicates() {

        SparseFieldVector<Fraction> v = new SparseFieldVector<Fraction>(field, new Fraction[] { new Fraction(0), new Fraction(1), new Fraction(2) });

        v.setEntry(0, field.getZero());
        Assert.assertEquals(v, new SparseFieldVector<Fraction>(field, new Fraction[] { new Fraction(0), new Fraction(1), new Fraction(2) }));
        Assert.assertNotSame(v, new SparseFieldVector<Fraction>(field, new Fraction[] { new Fraction(0), new Fraction(1), new Fraction(2), new Fraction(3) }));

    }

// org.apache.commons.math3.linear.SparseRealVectorTest::testConstructors
    public void testConstructors() {
        final double[] vec1 = {1d, 2d, 3d};
        final Double[] dvec1 = {1d, 2d, 3d, 4d, 5d, 6d, 7d, 8d, 9d};

        OpenMapRealVector v0 = new OpenMapRealVector();
        Assert.assertEquals("testData len", 0, v0.getDimension());

        OpenMapRealVector v1 = new OpenMapRealVector(7);
        Assert.assertEquals("testData len", 7, v1.getDimension());
        Assert.assertEquals("testData is 0.0 ", 0.0, v1.getEntry(6), 0);

        OpenMapRealVector v3 = new OpenMapRealVector(vec1);
        Assert.assertEquals("testData len", 3, v3.getDimension());
        Assert.assertEquals("testData is 2.0 ", 2.0, v3.getEntry(1), 0);

        
        
        
        
        
        
        
            
        

        RealVector v5_i = new OpenMapRealVector(dvec1);
        Assert.assertEquals("testData len", 9, v5_i.getDimension());
        Assert.assertEquals("testData is 9.0 ", 9.0, v5_i.getEntry(8), 0);

        OpenMapRealVector v5 = new OpenMapRealVector(dvec1);
        Assert.assertEquals("testData len", 9, v5.getDimension());
        Assert.assertEquals("testData is 9.0 ", 9.0, v5.getEntry(8), 0);

        OpenMapRealVector v7 = new OpenMapRealVector(v1);
        Assert.assertEquals("testData len", 7, v7.getDimension());
        Assert.assertEquals("testData is 0.0 ", 0.0, v7.getEntry(6), 0);

        RealVectorTestImpl v7_i = new RealVectorTestImpl(vec1);

        OpenMapRealVector v7_2 = new OpenMapRealVector(v7_i);
        Assert.assertEquals("testData len", 3, v7_2.getDimension());
        Assert.assertEquals("testData is 0.0 ", 2.0d, v7_2.getEntry(1), 0);

        OpenMapRealVector v8 = new OpenMapRealVector(v1);
        Assert.assertEquals("testData len", 7, v8.getDimension());
        Assert.assertEquals("testData is 0.0 ", 0.0, v8.getEntry(6), 0);

    }

// org.apache.commons.math3.linear.SparseRealVectorTest::testConcurrentModification
    public void testConcurrentModification() {
        final RealVector u = new OpenMapRealVector(3, 1e-6);
        u.setEntry(0, 1);
        u.setEntry(1, 0);
        u.setEntry(2, 2);

        final RealVector v1 = new OpenMapRealVector(3, 1e-6);
        v1.setEntry(0, 0);
        v1.setEntry(1, 3);
        v1.setEntry(2, 0);

        u.ebeMultiply(v1);
        u.ebeDivide(v1);
    }

// org.apache.commons.math3.linear.SparseRealVectorTest::testMap
    public void testMap() {}

// org.apache.commons.math3.linear.SparseRealVectorTest::testMapToSelf
    public void testMapToSelf() {}

// org.apache.commons.math3.linear.SymmLQTest::testSolveSaunders1
    public void testSolveSaunders1() {
        saundersTest(1, false, false, 0., 0.);
    }

// org.apache.commons.math3.linear.SymmLQTest::testSolveSaunders2
    public void testSolveSaunders2() {
        saundersTest(2, false, false, 0., 0.);
    }

// org.apache.commons.math3.linear.SymmLQTest::testSolveSaunders3
    public void testSolveSaunders3() {
        saundersTest(1, false, true, 0., 0.);
    }

// org.apache.commons.math3.linear.SymmLQTest::testSolveSaunders4
    public void testSolveSaunders4() {
        saundersTest(2, false, true, 0., 0.);
    }

// org.apache.commons.math3.linear.SymmLQTest::testSolveSaunders5
    public void testSolveSaunders5() {
        saundersTest(5, false, true, 0., 0.);
    }

// org.apache.commons.math3.linear.SymmLQTest::testSolveSaunders6
    public void testSolveSaunders6() {
        saundersTest(5, false, true, 0.25, 0.);
    }

// org.apache.commons.math3.linear.SymmLQTest::testSolveSaunders7
    public void testSolveSaunders7() {
        saundersTest(50, false, false, 0., 0.);
    }

// org.apache.commons.math3.linear.SymmLQTest::testSolveSaunders8
    public void testSolveSaunders8() {
        saundersTest(50, false, false, 0.25, 0.);
    }

// org.apache.commons.math3.linear.SymmLQTest::testSolveSaunders9
    public void testSolveSaunders9() {
        saundersTest(50, false, true, 0., 0.10);
    }

// org.apache.commons.math3.linear.SymmLQTest::testSolveSaunders10
    public void testSolveSaunders10() {
        saundersTest(50, false, true, 0.25, 0.10);
    }

// org.apache.commons.math3.linear.SymmLQTest::testSolveSaunders11
    public void testSolveSaunders11() {
        saundersTest(1, true, false, 0., 0.);
    }

// org.apache.commons.math3.linear.SymmLQTest::testSolveSaunders12
    public void testSolveSaunders12() {
        saundersTest(2, true, false, 0., 0.);
    }

// org.apache.commons.math3.linear.SymmLQTest::testSolveSaunders13
    public void testSolveSaunders13() {
        saundersTest(1, true, true, 0., 0.);
    }

// org.apache.commons.math3.linear.SymmLQTest::testSolveSaunders14
    public void testSolveSaunders14() {
        saundersTest(2, true, true, 0., 0.);
    }

// org.apache.commons.math3.linear.SymmLQTest::testSolveSaunders15
    public void testSolveSaunders15() {
        saundersTest(5, true, true, 0., 0.);
    }

// org.apache.commons.math3.linear.SymmLQTest::testSolveSaunders16
    public void testSolveSaunders16() {
        saundersTest(5, true, true, 0.25, 0.);
    }

// org.apache.commons.math3.linear.SymmLQTest::testSolveSaunders17
    public void testSolveSaunders17() {
        saundersTest(50, true, false, 0., 0.);
    }

// org.apache.commons.math3.linear.SymmLQTest::testSolveSaunders18
    public void testSolveSaunders18() {
        saundersTest(50, true, false, 0.25, 0.);
    }

// org.apache.commons.math3.linear.SymmLQTest::testSolveSaunders19
    public void testSolveSaunders19() {
        saundersTest(50, true, true, 0., 0.10);
    }

// org.apache.commons.math3.linear.SymmLQTest::testSolveSaunders20
    public void testSolveSaunders20() {
        saundersTest(50, true, true, 0.25, 0.10);
    }

// org.apache.commons.math3.linear.SymmLQTest::testNonSquareOperator
    public void testNonSquareOperator() {
        final Array2DRowRealMatrix a = new Array2DRowRealMatrix(2, 3);
        final IterativeLinearSolver solver;
        solver = new SymmLQ(10, 0., false);
        final ArrayRealVector b = new ArrayRealVector(a.getRowDimension());
        final ArrayRealVector x = new ArrayRealVector(a.getColumnDimension());
        solver.solve(a, b, x);
    }

// org.apache.commons.math3.linear.SymmLQTest::testDimensionMismatchRightHandSide
    public void testDimensionMismatchRightHandSide() {
        final Array2DRowRealMatrix a = new Array2DRowRealMatrix(3, 3);
        final IterativeLinearSolver solver;
        solver = new SymmLQ(10, 0., false);
        final ArrayRealVector b = new ArrayRealVector(2);
        solver.solve(a, b);
    }

// org.apache.commons.math3.linear.SymmLQTest::testDimensionMismatchSolution
    public void testDimensionMismatchSolution() {
        final Array2DRowRealMatrix a = new Array2DRowRealMatrix(3, 3);
        final IterativeLinearSolver solver;
        solver = new SymmLQ(10, 0., false);
        final ArrayRealVector b = new ArrayRealVector(3);
        final ArrayRealVector x = new ArrayRealVector(2);
        solver.solve(a, b, x);
    }

// org.apache.commons.math3.linear.SymmLQTest::testUnpreconditionedSolution
    public void testUnpreconditionedSolution() {
        final int n = 5;
        final int maxIterations = 100;
        final RealLinearOperator a = new HilbertMatrix(n);
        final InverseHilbertMatrix ainv = new InverseHilbertMatrix(n);
        final IterativeLinearSolver solver;
        solver = new SymmLQ(maxIterations, 1E-10, true);
        final RealVector b = new ArrayRealVector(n);
        for (int j = 0; j < n; j++) {
            b.set(0.);
            b.setEntry(j, 1.);
            final RealVector x = solver.solve(a, b);
            for (int i = 0; i < n; i++) {
                final double actual = x.getEntry(i);
                final double expected = ainv.getEntry(i, j);
                final double delta = 1E-6 * Math.abs(expected);
                final String msg = String.format("entry[%d][%d]", i, j);
                Assert.assertEquals(msg, expected, actual, delta);
            }
        }
    }

// org.apache.commons.math3.linear.SymmLQTest::testUnpreconditionedInPlaceSolutionWithInitialGuess
    public void testUnpreconditionedInPlaceSolutionWithInitialGuess() {
        final int n = 5;
        final int maxIterations = 100;
        final RealLinearOperator a = new HilbertMatrix(n);
        final InverseHilbertMatrix ainv = new InverseHilbertMatrix(n);
        final IterativeLinearSolver solver;
        solver = new SymmLQ(maxIterations, 1E-10, true);
        final RealVector b = new ArrayRealVector(n);
        for (int j = 0; j < n; j++) {
            b.set(0.);
            b.setEntry(j, 1.);
            final RealVector x0 = new ArrayRealVector(n);
            x0.set(1.);
            final RealVector x = solver.solveInPlace(a, b, x0);
            Assert.assertSame("x should be a reference to x0", x0, x);
            for (int i = 0; i < n; i++) {
                final double actual = x.getEntry(i);
                final double expected = ainv.getEntry(i, j);
                final double delta = 1E-6 * Math.abs(expected);
                final String msg = String.format("entry[%d][%d)", i, j);
                Assert.assertEquals(msg, expected, actual, delta);
            }
        }
    }

// org.apache.commons.math3.linear.SymmLQTest::testUnpreconditionedSolutionWithInitialGuess
    public void testUnpreconditionedSolutionWithInitialGuess() {
        final int n = 5;
        final int maxIterations = 100;
        final RealLinearOperator a = new HilbertMatrix(n);
        final InverseHilbertMatrix ainv = new InverseHilbertMatrix(n);
        final IterativeLinearSolver solver;
        solver = new SymmLQ(maxIterations, 1E-10, true);
        final RealVector b = new ArrayRealVector(n);
        for (int j = 0; j < n; j++) {
            b.set(0.);
            b.setEntry(j, 1.);
            final RealVector x0 = new ArrayRealVector(n);
            x0.set(1.);
            final RealVector x = solver.solve(a, b, x0);
            Assert.assertNotSame("x should not be a reference to x0", x0, x);
            for (int i = 0; i < n; i++) {
                final double actual = x.getEntry(i);
                final double expected = ainv.getEntry(i, j);
                final double delta = 1E-6 * Math.abs(expected);
                final String msg = String.format("entry[%d][%d]", i, j);
                Assert.assertEquals(msg, expected, actual, delta);
                Assert.assertEquals(msg, x0.getEntry(i), 1., Math.ulp(1.));
            }
        }
    }

// org.apache.commons.math3.linear.SymmLQTest::testNonSquarePreconditioner
    public void testNonSquarePreconditioner() {
        final Array2DRowRealMatrix a = new Array2DRowRealMatrix(2, 2);
        final RealLinearOperator m = new RealLinearOperator() {

            @Override
            public RealVector operate(final RealVector x) {
                throw new UnsupportedOperationException();
            }

            @Override
            public int getRowDimension() {
                return 2;
            }

            @Override
            public int getColumnDimension() {
                return 3;
            }
        };
        final PreconditionedIterativeLinearSolver solver;
        solver = new SymmLQ(10, 0., false);
        final ArrayRealVector b = new ArrayRealVector(a.getRowDimension());
        solver.solve(a, m, b);
    }

// org.apache.commons.math3.linear.SymmLQTest::testMismatchedOperatorDimensions
    public void testMismatchedOperatorDimensions() {
        final Array2DRowRealMatrix a = new Array2DRowRealMatrix(2, 2);
        final RealLinearOperator m = new RealLinearOperator() {

            @Override
            public RealVector operate(final RealVector x) {
                throw new UnsupportedOperationException();
            }

            @Override
            public int getRowDimension() {
                return 3;
            }

            @Override
            public int getColumnDimension() {
                return 3;
            }
        };
        final PreconditionedIterativeLinearSolver solver;
        solver = new SymmLQ(10, 0d, false);
        final ArrayRealVector b = new ArrayRealVector(a.getRowDimension());
        solver.solve(a, m, b);
    }

// org.apache.commons.math3.linear.SymmLQTest::testNonPositiveDefinitePreconditioner
    public void testNonPositiveDefinitePreconditioner() {
        final Array2DRowRealMatrix a = new Array2DRowRealMatrix(2, 2);
        a.setEntry(0, 0, 1d);
        a.setEntry(0, 1, 2d);
        a.setEntry(1, 0, 3d);
        a.setEntry(1, 1, 4d);
        final RealLinearOperator m = new RealLinearOperator() {

            @Override
            public RealVector operate(final RealVector x) {
                final ArrayRealVector y = new ArrayRealVector(2);
                y.setEntry(0, -x.getEntry(0));
                y.setEntry(1, -x.getEntry(1));
                return y;
            }

            @Override
            public int getRowDimension() {
                return 2;
            }

            @Override
            public int getColumnDimension() {
                return 2;
            }
        };
        final PreconditionedIterativeLinearSolver solver;
        solver = new SymmLQ(10, 0d, true);
        final ArrayRealVector b = new ArrayRealVector(2);
        b.setEntry(0, -1d);
        b.setEntry(1, -1d);
        solver.solve(a, m, b);
    }

// org.apache.commons.math3.linear.SymmLQTest::testPreconditionedSolution
    public void testPreconditionedSolution() {
        final int n = 8;
        final int maxIterations = 100;
        final RealLinearOperator a = new HilbertMatrix(n);
        final InverseHilbertMatrix ainv = new InverseHilbertMatrix(n);
        final RealLinearOperator m = JacobiPreconditioner.create(a);
        final PreconditionedIterativeLinearSolver solver;
        solver = new SymmLQ(maxIterations, 1E-15, true);
        final RealVector b = new ArrayRealVector(n);
        for (int j = 0; j < n; j++) {
            b.set(0.);
            b.setEntry(j, 1.);
            final RealVector x = solver.solve(a, m, b);
            for (int i = 0; i < n; i++) {
                final double actual = x.getEntry(i);
                final double expected = ainv.getEntry(i, j);
                final double delta = 1E-6 * Math.abs(expected);
                final String msg = String.format("coefficient (%d, %d)", i, j);
                Assert.assertEquals(msg, expected, actual, delta);
            }
        }
    }

// org.apache.commons.math3.linear.SymmLQTest::testPreconditionedSolution2
    public void testPreconditionedSolution2() {
        final int n = 100;
        final int maxIterations = 100000;
        final Array2DRowRealMatrix a = new Array2DRowRealMatrix(n, n);
        double daux = 1.;
        for (int i = 0; i < n; i++) {
            a.setEntry(i, i, daux);
            daux *= 1.2;
            for (int j = i + 1; j < n; j++) {
                if (i == j) {
                } else {
                    final double value = 1.0;
                    a.setEntry(i, j, value);
                    a.setEntry(j, i, value);
                }
            }
        }
        final RealLinearOperator m = JacobiPreconditioner.create(a);
        final PreconditionedIterativeLinearSolver prec;
        final IterativeLinearSolver unprec;
        prec = new SymmLQ(maxIterations, 1E-15, true);
        unprec = new SymmLQ(maxIterations, 1E-15, true);
        final RealVector b = new ArrayRealVector(n);
        final String pattern = "preconditioned SymmLQ (%d iterations) should"
                               + " have been faster than unpreconditioned (%d iterations)";
        String msg;
        for (int j = 0; j < 1; j++) {
            b.set(0.);
            b.setEntry(j, 1.);
            final RealVector px = prec.solve(a, m, b);
            final RealVector x = unprec.solve(a, b);
            final int np = prec.getIterationManager().getIterations();
            final int nup = unprec.getIterationManager().getIterations();
            msg = String.format(pattern, np, nup);
            for (int i = 0; i < n; i++) {
                msg = String.format("row %d, column %d", i, j);
                final double expected = x.getEntry(i);
                final double actual = px.getEntry(i);
                final double delta = 5E-5 * Math.abs(expected);
                Assert.assertEquals(msg, expected, actual, delta);
            }
        }
    }

// org.apache.commons.math3.linear.SymmLQTest::testEventManagement
    public void testEventManagement() {
        final int n = 5;
        final int maxIterations = 100;
        final RealLinearOperator a = new HilbertMatrix(n);
        final IterativeLinearSolver solver;
        
        final int[] count = new int[] {0, 0, 0, 0};
        final RealVector xFromListener = new ArrayRealVector(n);
        final IterationListener listener = new IterationListener() {

            public void initializationPerformed(final IterationEvent e) {
                ++count[0];
            }

            public void iterationPerformed(final IterationEvent e) {
                ++count[2];
                Assert.assertEquals("iteration performed",
                                    count[2],
                                    e.getIterations() - 1);
            }

            public void iterationStarted(final IterationEvent e) {
                ++count[1];
                Assert.assertEquals("iteration started",
                                    count[1],
                                    e.getIterations() - 1);
            }

            public void terminationPerformed(final IterationEvent e) {
                ++count[3];
                final IterativeLinearSolverEvent ilse;
                ilse = (IterativeLinearSolverEvent) e;
                xFromListener.setSubVector(0, ilse.getSolution());
            }
        };
        solver = new SymmLQ(maxIterations, 1E-10, true);
        solver.getIterationManager().addIterationListener(listener);
        final RealVector b = new ArrayRealVector(n);
        for (int j = 0; j < n; j++) {
            Arrays.fill(count, 0);
            b.set(0.);
            b.setEntry(j, 1.);
            final RealVector xFromSolver = solver.solve(a, b);
            String msg = String.format("column %d (initialization)", j);
            Assert.assertEquals(msg, 1, count[0]);
            msg = String.format("column %d (finalization)", j);
            Assert.assertEquals(msg, 1, count[3]);
            
            for (int i = 0; i < n; i++){
                msg = String.format("row %d, column %d", i, j);
                final double expected = xFromSolver.getEntry(i);
                final double actual = xFromListener.getEntry(i);
                Assert.assertEquals(msg, expected, actual, 0.0);
            }
        }
    }

// org.apache.commons.math3.linear.SymmLQTest::testNonSelfAdjointOperator
    public void testNonSelfAdjointOperator() {
        final RealLinearOperator a;
        a = new Array2DRowRealMatrix(new double[][] {
            {1., 2., 3.},
            {2., 4., 5.},
            {2.999, 5., 6.}
        });
        final RealVector b;
        b = new ArrayRealVector(new double[] {1., 1., 1.});
        new SymmLQ(100, 1., true).solve(a, b);
    }

// org.apache.commons.math3.linear.SymmLQTest::testNonSelfAdjointPreconditioner
    public void testNonSelfAdjointPreconditioner() {
        final RealLinearOperator a = new Array2DRowRealMatrix(new double[][] {
            {1., 2., 3.},
            {2., 4., 5.},
            {3., 5., 6.}
        });
        final Array2DRowRealMatrix mMat;
        mMat = new Array2DRowRealMatrix(new double[][] {
            {1., 0., 1.},
            {0., 1., 0.},
            {0., 0., 1.}
        });
        final DecompositionSolver mSolver;
        mSolver = new LUDecomposition(mMat).getSolver();
        final RealLinearOperator minv = new RealLinearOperator() {

            @Override
            public RealVector operate(final RealVector x) {
                return mSolver.solve(x);
            }

            @Override
            public int getRowDimension() {
                return mMat.getRowDimension();
            }

            @Override
            public int getColumnDimension() {
                return mMat.getColumnDimension();
            }
        };
        final RealVector b = new ArrayRealVector(new double[] {
            1., 1., 1.
        });
        new SymmLQ(100, 1., true).solve(a, minv, b);
    }

// org.apache.commons.math3.linear.SymmLQTest::testUnpreconditionedNormOfResidual
    public void testUnpreconditionedNormOfResidual() {
        final int n = 5;
        final int maxIterations = 100;
        final RealLinearOperator a = new HilbertMatrix(n);
        final IterativeLinearSolver solver;
        final IterationListener listener = new IterationListener() {

            private void doTestNormOfResidual(final IterationEvent e) {
                final IterativeLinearSolverEvent evt;
                evt = (IterativeLinearSolverEvent) e;
                final RealVector x = evt.getSolution();
                final RealVector b = evt.getRightHandSideVector();
                final RealVector r = b.subtract(a.operate(x));
                final double rnorm = r.getNorm();
                Assert.assertEquals("iteration performed (residual)",
                    rnorm, evt.getNormOfResidual(),
                    FastMath.max(1E-5 * rnorm, 1E-10));
            }

            public void initializationPerformed(final IterationEvent e) {
                doTestNormOfResidual(e);
            }

            public void iterationPerformed(final IterationEvent e) {
                doTestNormOfResidual(e);
            }

            public void iterationStarted(final IterationEvent e) {
                doTestNormOfResidual(e);
            }

            public void terminationPerformed(final IterationEvent e) {
                doTestNormOfResidual(e);
            }
        };
        solver = new SymmLQ(maxIterations, 1E-10, true);
        solver.getIterationManager().addIterationListener(listener);
        final RealVector b = new ArrayRealVector(n);
        for (int j = 0; j < n; j++) {
            b.set(0.);
            b.setEntry(j, 1.);
            solver.solve(a, b);
        }
    }

// org.apache.commons.math3.linear.SymmLQTest::testPreconditionedNormOfResidual
    public void testPreconditionedNormOfResidual() {
        final int n = 5;
        final int maxIterations = 100;
        final RealLinearOperator a = new HilbertMatrix(n);
        final JacobiPreconditioner m = JacobiPreconditioner.create(a);
        final RealLinearOperator p = m.sqrt();
        final PreconditionedIterativeLinearSolver solver;
        final IterationListener listener = new IterationListener() {

            private void doTestNormOfResidual(final IterationEvent e) {
                final IterativeLinearSolverEvent evt;
                evt = (IterativeLinearSolverEvent) e;
                final RealVector x = evt.getSolution();
                final RealVector b = evt.getRightHandSideVector();
                final RealVector r = b.subtract(a.operate(x));
                final double rnorm = p.operate(r).getNorm();
                Assert.assertEquals("iteration performed (residual)",
                    rnorm, evt.getNormOfResidual(),
                    FastMath.max(1E-5 * rnorm, 1E-10));
            }

            public void initializationPerformed(final IterationEvent e) {
                doTestNormOfResidual(e);
            }

            public void iterationPerformed(final IterationEvent e) {
                doTestNormOfResidual(e);
            }

            public void iterationStarted(final IterationEvent e) {
                doTestNormOfResidual(e);
            }

            public void terminationPerformed(final IterationEvent e) {
                doTestNormOfResidual(e);
            }
        };
        solver = new SymmLQ(maxIterations, 1E-10, true);
        solver.getIterationManager().addIterationListener(listener);
        final RealVector b = new ArrayRealVector(n);
        for (int j = 0; j < n; j++) {
            b.set(0.);
            b.setEntry(j, 1.);
            solver.solve(a, m, b);
        }
    }

// org.apache.commons.math3.ml.clustering.DBSCANClustererTest::testCluster
    public void testCluster() {
        
        final DoublePoint[] points = new DoublePoint[] {
                new DoublePoint(new double[] { 83.08303244924173, 58.83387754182331 }),
                new DoublePoint(new double[] { 45.05445510940626, 23.469642649637535 }),
                new DoublePoint(new double[] { 14.96417921432294, 69.0264096390456 }),
                new DoublePoint(new double[] { 73.53189604333602, 34.896145021310076 }),
                new DoublePoint(new double[] { 73.28498173551634, 33.96860806993209 }),
                new DoublePoint(new double[] { 73.45828098873608, 33.92584423092194 }),
                new DoublePoint(new double[] { 73.9657889183145, 35.73191006924026 }),
                new DoublePoint(new double[] { 74.0074097183533, 36.81735596177168 }),
                new DoublePoint(new double[] { 73.41247541410848, 34.27314856695011 }),
                new DoublePoint(new double[] { 73.9156256353017, 36.83206791547127 }),
                new DoublePoint(new double[] { 74.81499205809087, 37.15682749846019 }),
                new DoublePoint(new double[] { 74.03144880081527, 37.57399178552441 }),
                new DoublePoint(new double[] { 74.51870941207744, 38.674258946906775 }),
                new DoublePoint(new double[] { 74.50754595105536, 35.58903978415765 }),
                new DoublePoint(new double[] { 74.51322752749547, 36.030572259100154 }),
                new DoublePoint(new double[] { 59.27900996617973, 46.41091720294207 }),
                new DoublePoint(new double[] { 59.73744793841615, 46.20015558367595 }),
                new DoublePoint(new double[] { 58.81134076672606, 45.71150126331486 }),
                new DoublePoint(new double[] { 58.52225539437495, 47.416083617601544 }),
                new DoublePoint(new double[] { 58.218626647023484, 47.36228902172297 }),
                new DoublePoint(new double[] { 60.27139669447206, 46.606106348801404 }),
                new DoublePoint(new double[] { 60.894962462363765, 46.976924697402865 }),
                new DoublePoint(new double[] { 62.29048673878424, 47.66970563563518 }),
                new DoublePoint(new double[] { 61.03857608977705, 46.212924720020965 }),
                new DoublePoint(new double[] { 60.16916214139201, 45.18193661351688 }),
                new DoublePoint(new double[] { 59.90036905976012, 47.555364347063005 }),
                new DoublePoint(new double[] { 62.33003634144552, 47.83941489877179 }),
                new DoublePoint(new double[] { 57.86035536718555, 47.31117930193432 }),
                new DoublePoint(new double[] { 58.13715479685925, 48.985960494028404 }),
                new DoublePoint(new double[] { 56.131923963548616, 46.8508904252667 }),
                new DoublePoint(new double[] { 55.976329887053, 47.46384037658572 }),
                new DoublePoint(new double[] { 56.23245975235477, 47.940035191131756 }),
                new DoublePoint(new double[] { 58.51687048212625, 46.622885352699086 }),
                new DoublePoint(new double[] { 57.85411081905477, 45.95394361577928 }),
                new DoublePoint(new double[] { 56.445776311447844, 45.162093662656844 }),
                new DoublePoint(new double[] { 57.36691949656233, 47.50097194337286 }),
                new DoublePoint(new double[] { 58.243626387557015, 46.114052729681134 }),
                new DoublePoint(new double[] { 56.27224595635198, 44.799080066150054 }),
                new DoublePoint(new double[] { 57.606924816500396, 46.94291057763621 }),
                new DoublePoint(new double[] { 30.18714230041951, 13.877149710431695 }),
                new DoublePoint(new double[] { 30.449448810657486, 13.490778346545994 }),
                new DoublePoint(new double[] { 30.295018390286714, 13.264889000216499 }),
                new DoublePoint(new double[] { 30.160201832884923, 11.89278262341395 }),
                new DoublePoint(new double[] { 31.341509791789576, 15.282655921997502 }),
                new DoublePoint(new double[] { 31.68601630325429, 14.756873246748 }),
                new DoublePoint(new double[] { 29.325963742565364, 12.097849250072613 }),
                new DoublePoint(new double[] { 29.54820742388256, 13.613295356975868 }),
                new DoublePoint(new double[] { 28.79359608888626, 10.36352064087987 }),
                new DoublePoint(new double[] { 31.01284597092308, 12.788479208014905 }),
                new DoublePoint(new double[] { 27.58509216737002, 11.47570110601373 }),
                new DoublePoint(new double[] { 28.593799561727792, 10.780998203903437 }),
                new DoublePoint(new double[] { 31.356105766724795, 15.080316198524088 }),
                new DoublePoint(new double[] { 31.25948503636755, 13.674329151166603 }),
                new DoublePoint(new double[] { 32.31590076372959, 14.95261758659035 }),
                new DoublePoint(new double[] { 30.460413702763617, 15.88402809202671 }),
                new DoublePoint(new double[] { 32.56178203062154, 14.586076852632686 }),
                new DoublePoint(new double[] { 32.76138648530468, 16.239837325178087 }),
                new DoublePoint(new double[] { 30.1829453331884, 14.709592407103628 }),
                new DoublePoint(new double[] { 29.55088173528202, 15.0651247180067 }),
                new DoublePoint(new double[] { 29.004155302187428, 14.089665298582986 }),
                new DoublePoint(new double[] { 29.339624439831823, 13.29096065578051 }),
                new DoublePoint(new double[] { 30.997460327576846, 14.551914158277214 }),
                new DoublePoint(new double[] { 30.66784126125276, 16.269703107886016 })
        };

        final DBSCANClusterer<DoublePoint> transformer =
                new DBSCANClusterer<DoublePoint>(2.0, 5);
        final List<Cluster<DoublePoint>> clusters = transformer.cluster(Arrays.asList(points));

        final List<DoublePoint> clusterOne =
                Arrays.asList(points[3], points[4], points[5], points[6], points[7], points[8], points[9], points[10],
                              points[11], points[12], points[13], points[14]);
        final List<DoublePoint> clusterTwo =
                Arrays.asList(points[15], points[16], points[17], points[18], points[19], points[20], points[21],
                              points[22], points[23], points[24], points[25], points[26], points[27], points[28],
                              points[29], points[30], points[31], points[32], points[33], points[34], points[35],
                              points[36], points[37], points[38]);
        final List<DoublePoint> clusterThree =
                Arrays.asList(points[39], points[40], points[41], points[42], points[43], points[44], points[45],
                              points[46], points[47], points[48], points[49], points[50], points[51], points[52],
                              points[53], points[54], points[55], points[56], points[57], points[58], points[59],
                              points[60], points[61], points[62]);

        boolean cluster1Found = false;
        boolean cluster2Found = false;
        boolean cluster3Found = false;
        Assert.assertEquals(3, clusters.size());
        for (final Cluster<DoublePoint> cluster : clusters) {
            if (cluster.getPoints().containsAll(clusterOne)) {
                cluster1Found = true;
            }
            if (cluster.getPoints().containsAll(clusterTwo)) {
                cluster2Found = true;
            }
            if (cluster.getPoints().containsAll(clusterThree)) {
                cluster3Found = true;
            }
        }
        Assert.assertTrue(cluster1Found);
        Assert.assertTrue(cluster2Found);
        Assert.assertTrue(cluster3Found);
    }

// org.apache.commons.math3.ml.clustering.DBSCANClustererTest::testSingleLink
    public void testSingleLink() {
        final DoublePoint[] points = {
                new DoublePoint(new int[] {10, 10}), 
                new DoublePoint(new int[] {12, 9}),
                new DoublePoint(new int[] {10, 8}),
                new DoublePoint(new int[] {8, 8}),
                new DoublePoint(new int[] {8, 6}),
                new DoublePoint(new int[] {7, 7}),
                new DoublePoint(new int[] {5, 6}),  
                new DoublePoint(new int[] {14, 8}), 
                new DoublePoint(new int[] {7, 15}), 
                new DoublePoint(new int[] {17, 8}), 
                
        };
        
        final DBSCANClusterer<DoublePoint> clusterer = new DBSCANClusterer<DoublePoint>(3, 3);
        List<Cluster<DoublePoint>> clusters = clusterer.cluster(Arrays.asList(points));
        
        Assert.assertEquals(1, clusters.size());
        
        final List<DoublePoint> clusterOne =
                Arrays.asList(points[0], points[1], points[2], points[3], points[4], points[5], points[6], points[7]);
        Assert.assertTrue(clusters.get(0).getPoints().containsAll(clusterOne));
    }

// org.apache.commons.math3.ml.clustering.DBSCANClustererTest::testGetEps
    public void testGetEps() {
        final DBSCANClusterer<DoublePoint> transformer = new DBSCANClusterer<DoublePoint>(2.0, 5);
        Assert.assertEquals(2.0, transformer.getEps(), 0.0);
    }

// org.apache.commons.math3.ml.clustering.DBSCANClustererTest::testGetMinPts
    public void testGetMinPts() {
        final DBSCANClusterer<DoublePoint> transformer = new DBSCANClusterer<DoublePoint>(2.0, 5);
        Assert.assertEquals(5, transformer.getMinPts());
    }

// org.apache.commons.math3.ml.clustering.DBSCANClustererTest::testNegativeEps
    public void testNegativeEps() {
        new DBSCANClusterer<DoublePoint>(-2.0, 5);
    }

// org.apache.commons.math3.ml.clustering.DBSCANClustererTest::testNegativeMinPts
    public void testNegativeMinPts() {
        new DBSCANClusterer<DoublePoint>(2.0, -5);
    }

// org.apache.commons.math3.ml.clustering.DBSCANClustererTest::testNullDataset
    public void testNullDataset() {
        DBSCANClusterer<DoublePoint> clusterer = new DBSCANClusterer<DoublePoint>(2.0, 5);
        clusterer.cluster(null);
    }

// org.apache.commons.math3.ml.clustering.FuzzyKMeansClustererTest::testCluster
    public void testCluster() {
        final List<DoublePoint> points = new ArrayList<DoublePoint>();

        
        for (int i = 1; i <= 10; i++) {
            final DoublePoint p = new DoublePoint(new double[] { i } );
            points.add(p);
        }

        final FuzzyKMeansClusterer<DoublePoint> transformer =
                new FuzzyKMeansClusterer<DoublePoint>(3, 2.0);
        final List<CentroidCluster<DoublePoint>> clusters = transformer.cluster(points);

        
        
        
        
        final List<DoublePoint> clusterOne = Arrays.asList(points.get(0), points.get(1), points.get(2));
        final List<DoublePoint> clusterTwo = Arrays.asList(points.get(3), points.get(4), points.get(5), points.get(6));
        final List<DoublePoint> clusterThree = Arrays.asList(points.get(7), points.get(8), points.get(9));

        boolean cluster1Found = false;
        boolean cluster2Found = false;
        boolean cluster3Found = false;
        Assert.assertEquals(3, clusters.size());
        for (final Cluster<DoublePoint> cluster : clusters) {
            if (cluster.getPoints().containsAll(clusterOne)) {
                cluster1Found = true;
            }
            if (cluster.getPoints().containsAll(clusterTwo)) {
                cluster2Found = true;
            }
            if (cluster.getPoints().containsAll(clusterThree)) {
                cluster3Found = true;
            }
        }
        Assert.assertTrue(cluster1Found);
        Assert.assertTrue(cluster2Found);
        Assert.assertTrue(cluster3Found);
    }

// org.apache.commons.math3.ml.clustering.FuzzyKMeansClustererTest::testTooSmallFuzzynessFactor
    public void testTooSmallFuzzynessFactor() {
        new FuzzyKMeansClusterer<DoublePoint>(3, 1.0);
    }

// org.apache.commons.math3.ml.clustering.FuzzyKMeansClustererTest::testNullDataset
    public void testNullDataset() {
        final FuzzyKMeansClusterer<DoublePoint> clusterer = new FuzzyKMeansClusterer<DoublePoint>(3, 2.0);
        clusterer.cluster(null);
    }

// org.apache.commons.math3.ml.clustering.FuzzyKMeansClustererTest::testGetters
    public void testGetters() {
        final DistanceMeasure measure = new CanberraDistance();
        final RandomGenerator random = new JDKRandomGenerator();
        final FuzzyKMeansClusterer<DoublePoint> clusterer =
                new FuzzyKMeansClusterer<DoublePoint>(3, 2.0, 100, measure, 1e-6, random);

        Assert.assertEquals(3, clusterer.getK());
        Assert.assertEquals(2.0, clusterer.getFuzziness(), 1e-6);
        Assert.assertEquals(100, clusterer.getMaxIterations());
        Assert.assertEquals(1e-6, clusterer.getEpsilon(), 1e-12);
        Assert.assertThat(clusterer.getDistanceMeasure(), CoreMatchers.is(measure));
        Assert.assertThat(clusterer.getRandomGenerator(), CoreMatchers.is(random));
    }

// org.apache.commons.math3.ml.clustering.KMeansPlusPlusClustererTest::testPerformClusterAnalysisDegenerate
    public void testPerformClusterAnalysisDegenerate() {
        KMeansPlusPlusClusterer<DoublePoint> transformer =
                new KMeansPlusPlusClusterer<DoublePoint>(1, 1);

        DoublePoint[] points = new DoublePoint[] {
                new DoublePoint(new int[] { 1959, 325100 }),
                new DoublePoint(new int[] { 1960, 373200 }), };
        List<? extends Cluster<DoublePoint>> clusters = transformer.cluster(Arrays.asList(points));
        Assert.assertEquals(1, clusters.size());
        Assert.assertEquals(2, (clusters.get(0).getPoints().size()));
        DoublePoint pt1 = new DoublePoint(new int[] { 1959, 325100 });
        DoublePoint pt2 = new DoublePoint(new int[] { 1960, 373200 });
        Assert.assertTrue(clusters.get(0).getPoints().contains(pt1));
        Assert.assertTrue(clusters.get(0).getPoints().contains(pt2));

    }

// org.apache.commons.math3.ml.clustering.KMeansPlusPlusClustererTest::testCertainSpace
    public void testCertainSpace() {
        KMeansPlusPlusClusterer.EmptyClusterStrategy[] strategies = {
            KMeansPlusPlusClusterer.EmptyClusterStrategy.LARGEST_VARIANCE,
            KMeansPlusPlusClusterer.EmptyClusterStrategy.LARGEST_POINTS_NUMBER,
            KMeansPlusPlusClusterer.EmptyClusterStrategy.FARTHEST_POINT
        };
        for (KMeansPlusPlusClusterer.EmptyClusterStrategy strategy : strategies) {
            int numberOfVariables = 27;
            
            int position1 = 1;
            int position2 = position1 + numberOfVariables;
            int position3 = position2 + numberOfVariables;
            int position4 = position3 + numberOfVariables;
            
            int multiplier = 1000000;

            DoublePoint[] breakingPoints = new DoublePoint[numberOfVariables];
            
            for (int i = 0; i < numberOfVariables; i++) {
                int points[] = { position1, position2, position3, position4 };
                
                for (int j = 0; j < points.length; j++) {
                    points[j] = points[j] * multiplier;
                }
                DoublePoint DoublePoint = new DoublePoint(points);
                breakingPoints[i] = DoublePoint;
                position1 = position1 + numberOfVariables;
                position2 = position2 + numberOfVariables;
                position3 = position3 + numberOfVariables;
                position4 = position4 + numberOfVariables;
            }

            for (int n = 2; n < 27; ++n) {
                KMeansPlusPlusClusterer<DoublePoint> transformer =
                    new KMeansPlusPlusClusterer<DoublePoint>(n, 100, new EuclideanDistance(), random, strategy);

                List<? extends Cluster<DoublePoint>> clusters =
                        transformer.cluster(Arrays.asList(breakingPoints));

                Assert.assertEquals(n, clusters.size());
                int sum = 0;
                for (Cluster<DoublePoint> cluster : clusters) {
                    sum += cluster.getPoints().size();
                }
                Assert.assertEquals(numberOfVariables, sum);
            }
        }

    }

// org.apache.commons.math3.ml.clustering.KMeansPlusPlusClustererTest::testSmallDistances
    public void testSmallDistances() {
        
        
        int[] repeatedArray = { 0 };
        int[] uniqueArray = { 1 };
        DoublePoint repeatedPoint = new DoublePoint(repeatedArray);
        DoublePoint uniquePoint = new DoublePoint(uniqueArray);

        Collection<DoublePoint> points = new ArrayList<DoublePoint>();
        final int NUM_REPEATED_POINTS = 10 * 1000;
        for (int i = 0; i < NUM_REPEATED_POINTS; ++i) {
            points.add(repeatedPoint);
        }
        points.add(uniquePoint);

        
        
        final long RANDOM_SEED = 0;
        final int NUM_CLUSTERS = 2;
        final int NUM_ITERATIONS = 0;
        random.setSeed(RANDOM_SEED);
        
        KMeansPlusPlusClusterer<DoublePoint> clusterer =
            new KMeansPlusPlusClusterer<DoublePoint>(NUM_CLUSTERS, NUM_ITERATIONS,
                    new CloseDistance(), random);
        List<CentroidCluster<DoublePoint>> clusters = clusterer.cluster(points);

        
        boolean uniquePointIsCenter = false;
        for (CentroidCluster<DoublePoint> cluster : clusters) {
            if (cluster.getCenter().equals(uniquePoint)) {
                uniquePointIsCenter = true;
            }
        }
        Assert.assertTrue(uniquePointIsCenter);
    }

// org.apache.commons.math3.ml.clustering.KMeansPlusPlusClustererTest::testPerformClusterAnalysisToManyClusters
    public void testPerformClusterAnalysisToManyClusters() {
        KMeansPlusPlusClusterer<DoublePoint> transformer = 
            new KMeansPlusPlusClusterer<DoublePoint>(3, 1, new EuclideanDistance(), random);
        
        DoublePoint[] points = new DoublePoint[] {
            new DoublePoint(new int[] {
                1959, 325100
            }), new DoublePoint(new int[] {
                1960, 373200
            })
        };
        
        transformer.cluster(Arrays.asList(points));

    }

// org.apache.commons.math3.ml.clustering.MultiKMeansPlusPlusClustererTest::dimension2
    public void dimension2() {
        MultiKMeansPlusPlusClusterer<DoublePoint> transformer =
            new MultiKMeansPlusPlusClusterer<DoublePoint>(
                    new KMeansPlusPlusClusterer<DoublePoint>(3, 10), 5);
        
        DoublePoint[] points = new DoublePoint[] {

                
                new DoublePoint(new int[] { -15,  3 }),
                new DoublePoint(new int[] { -15,  4 }),
                new DoublePoint(new int[] { -15,  5 }),
                new DoublePoint(new int[] { -14,  3 }),
                new DoublePoint(new int[] { -14,  5 }),
                new DoublePoint(new int[] { -13,  3 }),
                new DoublePoint(new int[] { -13,  4 }),
                new DoublePoint(new int[] { -13,  5 }),

                
                new DoublePoint(new int[] { -1,  0 }),
                new DoublePoint(new int[] { -1, -1 }),
                new DoublePoint(new int[] {  0, -1 }),
                new DoublePoint(new int[] {  1, -1 }),
                new DoublePoint(new int[] {  1, -2 }),

                
                new DoublePoint(new int[] { 13,  3 }),
                new DoublePoint(new int[] { 13,  4 }),
                new DoublePoint(new int[] { 14,  4 }),
                new DoublePoint(new int[] { 14,  7 }),
                new DoublePoint(new int[] { 16,  5 }),
                new DoublePoint(new int[] { 16,  6 }),
                new DoublePoint(new int[] { 17,  4 }),
                new DoublePoint(new int[] { 17,  7 })

        };
        List<CentroidCluster<DoublePoint>> clusters = transformer.cluster(Arrays.asList(points));

        Assert.assertEquals(3, clusters.size());
        boolean cluster1Found = false;
        boolean cluster2Found = false;
        boolean cluster3Found = false;
        double epsilon = 1e-6;
        for (CentroidCluster<DoublePoint> cluster : clusters) {
            Clusterable center = cluster.getCenter();
            double[] point = center.getPoint();
            if (point[0] < 0) {
                cluster1Found = true;
                Assert.assertEquals(8, cluster.getPoints().size());
                Assert.assertEquals(-14, point[0], epsilon);
                Assert.assertEquals( 4, point[1], epsilon);
            } else if (point[1] < 0) {
                cluster2Found = true;
                Assert.assertEquals(5, cluster.getPoints().size());
                Assert.assertEquals( 0, point[0], epsilon);
                Assert.assertEquals(-1, point[1], epsilon);
            } else {
                cluster3Found = true;
                Assert.assertEquals(8, cluster.getPoints().size());
                Assert.assertEquals(15, point[0], epsilon);
                Assert.assertEquals(5, point[1], epsilon);
            }
        }
        Assert.assertTrue(cluster1Found);
        Assert.assertTrue(cluster2Found);
        Assert.assertTrue(cluster3Found);

    }

// org.apache.commons.math3.ml.distance.ChebyshevDistanceTest::testZero
    public void testZero() {
        final double[] a = { 0, 1, -2, 3.4, 5, -6.7, 89 };
        Assert.assertEquals(0, distance.compute(a, a), 0d);
    }

// org.apache.commons.math3.ml.distance.ChebyshevDistanceTest::test
    public void test() {
        final double[] a = { 1, 2, 3, 4 };
        final double[] b = { -5, -6, 7, 8 };
        final double expected = 8;
        Assert.assertEquals(expected, distance.compute(a, b), 0d);
        Assert.assertEquals(expected, distance.compute(b, a), 0d);
    }

// org.apache.commons.math3.ml.distance.EuclideanDistanceTest::testZero
    public void testZero() {
        final double[] a = { 0, 1, -2, 3.4, 5, -6.7, 89 };
        Assert.assertEquals(0, distance.compute(a, a), 0d);
    }

// org.apache.commons.math3.ml.distance.EuclideanDistanceTest::test
    public void test() {
        final double[] a = { 1, -2, 3, 4 };
        final double[] b = { -5, -6, 7, 8 };
        final double expected = FastMath.sqrt(84);
        Assert.assertEquals(expected, distance.compute(a, b), 0d);
        Assert.assertEquals(expected, distance.compute(b, a), 0d);
    }

// org.apache.commons.math3.ml.distance.ManhattanDistanceTest::testZero
    public void testZero() {
        final double[] a = { 0, 1, -2, 3.4, 5, -6.7, 89 };
        Assert.assertEquals(0, distance.compute(a, a), 0d);
    }

// org.apache.commons.math3.ml.distance.ManhattanDistanceTest::test
    public void test() {
        final double[] a = { 1, -2, 3, 4 };
        final double[] b = { -5, -6, 7, 8 };
        final double expected = 18;
        Assert.assertEquals(expected, distance.compute(a, b), 0d);
        Assert.assertEquals(expected, distance.compute(b, a), 0d);
    }

// org.apache.commons.math3.ode.JacobianMatricesTest::testLowAccuracyExternalDifferentiation
    public void testLowAccuracyExternalDifferentiation()
        throws NumberIsTooSmallException, DimensionMismatchException,
               MaxCountExceededException, NoBracketingException {
        
        
        
        
        
        
        
        
        
        FirstOrderIntegrator integ =
            new DormandPrince54Integrator(1.0e-8, 100.0, new double[] { 1.0e-4, 1.0e-4 }, new double[] { 1.0e-4, 1.0e-4 });
        double hP = 1.0e-12;
        SummaryStatistics residualsP0 = new SummaryStatistics();
        SummaryStatistics residualsP1 = new SummaryStatistics();
        for (double b = 2.88; b < 3.08; b += 0.001) {
            Brusselator brusselator = new Brusselator(b);
            double[] y = { 1.3, b };
            integ.integrate(brusselator, 0, y, 20.0, y);
            double[] yP = { 1.3, b + hP };
            integ.integrate(brusselator, 0, yP, 20.0, yP);
            residualsP0.addValue((yP[0] - y[0]) / hP - brusselator.dYdP0());
            residualsP1.addValue((yP[1] - y[1]) / hP - brusselator.dYdP1());
        }
        Assert.assertTrue((residualsP0.getMax() - residualsP0.getMin()) > 500);
        Assert.assertTrue(residualsP0.getStandardDeviation() > 30);
        Assert.assertTrue((residualsP1.getMax() - residualsP1.getMin()) > 700);
        Assert.assertTrue(residualsP1.getStandardDeviation() > 40);
    }

// org.apache.commons.math3.ode.JacobianMatricesTest::testHighAccuracyExternalDifferentiation
    public void testHighAccuracyExternalDifferentiation()
        throws NumberIsTooSmallException, DimensionMismatchException,
               MaxCountExceededException, NoBracketingException, UnknownParameterException {
        FirstOrderIntegrator integ =
            new DormandPrince54Integrator(1.0e-8, 100.0, new double[] { 1.0e-10, 1.0e-10 }, new double[] { 1.0e-10, 1.0e-10 });
        double hP = 1.0e-12;
        SummaryStatistics residualsP0 = new SummaryStatistics();
        SummaryStatistics residualsP1 = new SummaryStatistics();
        for (double b = 2.88; b < 3.08; b += 0.001) {
            ParamBrusselator brusselator = new ParamBrusselator(b);
            double[] y = { 1.3, b };
            integ.integrate(brusselator, 0, y, 20.0, y);
            double[] yP = { 1.3, b + hP };
            brusselator.setParameter("b", b + hP);
            integ.integrate(brusselator, 0, yP, 20.0, yP);
            residualsP0.addValue((yP[0] - y[0]) / hP - brusselator.dYdP0());
            residualsP1.addValue((yP[1] - y[1]) / hP - brusselator.dYdP1());
        }
        Assert.assertTrue((residualsP0.getMax() - residualsP0.getMin()) > 0.02);
        Assert.assertTrue((residualsP0.getMax() - residualsP0.getMin()) < 0.03);
        Assert.assertTrue(residualsP0.getStandardDeviation() > 0.003);
        Assert.assertTrue(residualsP0.getStandardDeviation() < 0.004);
        Assert.assertTrue((residualsP1.getMax() - residualsP1.getMin()) > 0.04);
        Assert.assertTrue((residualsP1.getMax() - residualsP1.getMin()) < 0.05);
        Assert.assertTrue(residualsP1.getStandardDeviation() > 0.007);
        Assert.assertTrue(residualsP1.getStandardDeviation() < 0.008);
    }

// org.apache.commons.math3.ode.JacobianMatricesTest::testInternalDifferentiation
    public void testInternalDifferentiation()
        throws NumberIsTooSmallException, DimensionMismatchException,
               MaxCountExceededException, NoBracketingException,
               UnknownParameterException, MismatchedEquations {
        AbstractIntegrator integ =
            new DormandPrince54Integrator(1.0e-8, 100.0, new double[] { 1.0e-4, 1.0e-4 }, new double[] { 1.0e-4, 1.0e-4 });
        double hP = 1.0e-12;
        double hY = 1.0e-12;
        SummaryStatistics residualsP0 = new SummaryStatistics();
        SummaryStatistics residualsP1 = new SummaryStatistics();
        for (double b = 2.88; b < 3.08; b += 0.001) {
            ParamBrusselator brusselator = new ParamBrusselator(b);
            brusselator.setParameter(ParamBrusselator.B, b);
            double[] z = { 1.3, b };
            double[][] dZdZ0 = new double[2][2];
            double[]   dZdP  = new double[2];

            JacobianMatrices jacob = new JacobianMatrices(brusselator, new double[] { hY, hY }, ParamBrusselator.B);
            jacob.setParameterizedODE(brusselator);
            jacob.setParameterStep(ParamBrusselator.B, hP);
            jacob.setInitialParameterJacobian(ParamBrusselator.B, new double[] { 0.0, 1.0 });

            ExpandableStatefulODE efode = new ExpandableStatefulODE(brusselator);
            efode.setTime(0);
            efode.setPrimaryState(z);
            jacob.registerVariationalEquations(efode);

            integ.setMaxEvaluations(5000);
            integ.integrate(efode, 20.0);
            jacob.getCurrentMainSetJacobian(dZdZ0);
            jacob.getCurrentParameterJacobian(ParamBrusselator.B, dZdP);

            residualsP0.addValue(dZdP[0] - brusselator.dYdP0());
            residualsP1.addValue(dZdP[1] - brusselator.dYdP1());
        }
        Assert.assertTrue((residualsP0.getMax() - residualsP0.getMin()) < 0.02);
        Assert.assertTrue(residualsP0.getStandardDeviation() < 0.003);
        Assert.assertTrue((residualsP1.getMax() - residualsP1.getMin()) < 0.05);
        Assert.assertTrue(residualsP1.getStandardDeviation() < 0.01);
    }

// org.apache.commons.math3.ode.JacobianMatricesTest::testAnalyticalDifferentiation
    public void testAnalyticalDifferentiation()
        throws MaxCountExceededException, DimensionMismatchException,
               NumberIsTooSmallException, NoBracketingException,
               UnknownParameterException, MismatchedEquations {
        AbstractIntegrator integ =
            new DormandPrince54Integrator(1.0e-8, 100.0, new double[] { 1.0e-4, 1.0e-4 }, new double[] { 1.0e-4, 1.0e-4 });
        SummaryStatistics residualsP0 = new SummaryStatistics();
        SummaryStatistics residualsP1 = new SummaryStatistics();
        for (double b = 2.88; b < 3.08; b += 0.001) {
            Brusselator brusselator = new Brusselator(b);
            double[] z = { 1.3, b };
            double[][] dZdZ0 = new double[2][2];
            double[]   dZdP  = new double[2];

            JacobianMatrices jacob = new JacobianMatrices(brusselator, Brusselator.B);
            jacob.addParameterJacobianProvider(brusselator);
            jacob.setInitialParameterJacobian(Brusselator.B, new double[] { 0.0, 1.0 });

            ExpandableStatefulODE efode = new ExpandableStatefulODE(brusselator);
            efode.setTime(0);
            efode.setPrimaryState(z);
            jacob.registerVariationalEquations(efode);

            integ.setMaxEvaluations(5000);
            integ.integrate(efode, 20.0);
            jacob.getCurrentMainSetJacobian(dZdZ0);
            jacob.getCurrentParameterJacobian(Brusselator.B, dZdP);

            residualsP0.addValue(dZdP[0] - brusselator.dYdP0());
            residualsP1.addValue(dZdP[1] - brusselator.dYdP1());
        }
        Assert.assertTrue((residualsP0.getMax() - residualsP0.getMin()) < 0.014);
        Assert.assertTrue(residualsP0.getStandardDeviation() < 0.003);
        Assert.assertTrue((residualsP1.getMax() - residualsP1.getMin()) < 0.05);
        Assert.assertTrue(residualsP1.getStandardDeviation() < 0.01);
    }

// org.apache.commons.math3.ode.JacobianMatricesTest::testFinalResult
    public void testFinalResult()
        throws MaxCountExceededException, DimensionMismatchException,
               NumberIsTooSmallException, NoBracketingException,
               UnknownParameterException, MismatchedEquations {

        AbstractIntegrator integ =
            new DormandPrince54Integrator(1.0e-8, 100.0, new double[] { 1.0e-10, 1.0e-10 }, new double[] { 1.0e-10, 1.0e-10 });
        double[] y = new double[] { 0.0, 1.0 };
        Circle circle = new Circle(y, 1.0, 1.0, 0.1);

        JacobianMatrices jacob = new JacobianMatrices(circle, Circle.CX, Circle.CY, Circle.OMEGA);
        jacob.addParameterJacobianProvider(circle);
        jacob.setInitialMainStateJacobian(circle.exactDyDy0(0));
        jacob.setInitialParameterJacobian(Circle.CX, circle.exactDyDcx(0));
        jacob.setInitialParameterJacobian(Circle.CY, circle.exactDyDcy(0));
        jacob.setInitialParameterJacobian(Circle.OMEGA, circle.exactDyDom(0));

        ExpandableStatefulODE efode = new ExpandableStatefulODE(circle);
        efode.setTime(0);
        efode.setPrimaryState(y);
        jacob.registerVariationalEquations(efode);

        integ.setMaxEvaluations(5000);

        double t = 18 * FastMath.PI;
        integ.integrate(efode, t);
        y = efode.getPrimaryState();
        for (int i = 0; i < y.length; ++i) {
            Assert.assertEquals(circle.exactY(t)[i], y[i], 1.0e-9);
        }

        double[][] dydy0 = new double[2][2];
        jacob.getCurrentMainSetJacobian(dydy0);
        for (int i = 0; i < dydy0.length; ++i) {
            for (int j = 0; j < dydy0[i].length; ++j) {
                Assert.assertEquals(circle.exactDyDy0(t)[i][j], dydy0[i][j], 1.0e-9);
            }
        }
        double[] dydcx = new double[2];
        jacob.getCurrentParameterJacobian(Circle.CX, dydcx);
        for (int i = 0; i < dydcx.length; ++i) {
            Assert.assertEquals(circle.exactDyDcx(t)[i], dydcx[i], 1.0e-7);
        }
        double[] dydcy = new double[2];
        jacob.getCurrentParameterJacobian(Circle.CY, dydcy);
        for (int i = 0; i < dydcy.length; ++i) {
            Assert.assertEquals(circle.exactDyDcy(t)[i], dydcy[i], 1.0e-7);
        }
        double[] dydom = new double[2];
        jacob.getCurrentParameterJacobian(Circle.OMEGA, dydom);
        for (int i = 0; i < dydom.length; ++i) {
            Assert.assertEquals(circle.exactDyDom(t)[i], dydom[i], 1.0e-7);
        }
    }

// org.apache.commons.math3.ode.JacobianMatricesTest::testParameterizable
    public void testParameterizable()
        throws MaxCountExceededException, DimensionMismatchException,
               NumberIsTooSmallException, NoBracketingException,
               UnknownParameterException, MismatchedEquations {

        AbstractIntegrator integ =
            new DormandPrince54Integrator(1.0e-8, 100.0, new double[] { 1.0e-10, 1.0e-10 }, new double[] { 1.0e-10, 1.0e-10 });
        double[] y = new double[] { 0.0, 1.0 };
        ParameterizedCircle pcircle = new ParameterizedCircle(y, 1.0, 1.0, 0.1);

        double hP = 1.0e-12;
        double hY = 1.0e-12;

        JacobianMatrices jacob = new JacobianMatrices(pcircle, new double[] { hY, hY },
                                                      ParameterizedCircle.CX, ParameterizedCircle.CY,
                                                      ParameterizedCircle.OMEGA);
        jacob.setParameterizedODE(pcircle);
        jacob.setParameterStep(ParameterizedCircle.CX,    hP);
        jacob.setParameterStep(ParameterizedCircle.CY,    hP);
        jacob.setParameterStep(ParameterizedCircle.OMEGA, hP);
        jacob.setInitialMainStateJacobian(pcircle.exactDyDy0(0));
        jacob.setInitialParameterJacobian(ParameterizedCircle.CX, pcircle.exactDyDcx(0));
        jacob.setInitialParameterJacobian(ParameterizedCircle.CY, pcircle.exactDyDcy(0));
        jacob.setInitialParameterJacobian(ParameterizedCircle.OMEGA, pcircle.exactDyDom(0));

        ExpandableStatefulODE efode = new ExpandableStatefulODE(pcircle);
        efode.setTime(0);
        efode.setPrimaryState(y);
        jacob.registerVariationalEquations(efode);

        integ.setMaxEvaluations(50000);

        double t = 18 * FastMath.PI;
        integ.integrate(efode, t);
        y = efode.getPrimaryState();
        for (int i = 0; i < y.length; ++i) {
            Assert.assertEquals(pcircle.exactY(t)[i], y[i], 1.0e-9);
        }

        double[][] dydy0 = new double[2][2];
        jacob.getCurrentMainSetJacobian(dydy0);
        for (int i = 0; i < dydy0.length; ++i) {
            for (int j = 0; j < dydy0[i].length; ++j) {
                Assert.assertEquals(pcircle.exactDyDy0(t)[i][j], dydy0[i][j], 5.0e-4);
            }
        }

        double[] dydp0 = new double[2];
        jacob.getCurrentParameterJacobian(ParameterizedCircle.CX, dydp0);
        for (int i = 0; i < dydp0.length; ++i) {
            Assert.assertEquals(pcircle.exactDyDcx(t)[i], dydp0[i], 5.0e-4);
        }

        double[] dydp1 = new double[2];
        jacob.getCurrentParameterJacobian(ParameterizedCircle.OMEGA, dydp1);
        for (int i = 0; i < dydp1.length; ++i) {
            Assert.assertEquals(pcircle.exactDyDom(t)[i], dydp1[i], 1.0e-2);
        }
    }

// org.apache.commons.math3.ode.nonstiff.AdamsBashforthIntegratorTest::dimensionCheck
    public void dimensionCheck() throws NumberIsTooSmallException, DimensionMismatchException, MaxCountExceededException, NoBracketingException {
        TestProblem1 pb = new TestProblem1();
        FirstOrderIntegrator integ =
            new AdamsBashforthIntegrator(2, 0.0, 1.0, 1.0e-10, 1.0e-10);
        integ.integrate(pb,
                        0.0, new double[pb.getDimension()+10],
                        1.0, new double[pb.getDimension()+10]);
    }

// org.apache.commons.math3.ode.nonstiff.AdamsBashforthIntegratorTest::testMinStep
    public void testMinStep() throws DimensionMismatchException, NumberIsTooSmallException, MaxCountExceededException, NoBracketingException {

          TestProblem1 pb = new TestProblem1();
          double minStep = 0.1 * (pb.getFinalTime() - pb.getInitialTime());
          double maxStep = pb.getFinalTime() - pb.getInitialTime();
          double[] vecAbsoluteTolerance = { 1.0e-15, 1.0e-16 };
          double[] vecRelativeTolerance = { 1.0e-15, 1.0e-16 };

          FirstOrderIntegrator integ = new AdamsBashforthIntegrator(4, minStep, maxStep,
                                                                    vecAbsoluteTolerance,
                                                                    vecRelativeTolerance);
          TestProblemHandler handler = new TestProblemHandler(pb, integ);
          integ.addStepHandler(handler);
          integ.integrate(pb,
                          pb.getInitialTime(), pb.getInitialState(),
                          pb.getFinalTime(), new double[pb.getDimension()]);

    }

// org.apache.commons.math3.ode.nonstiff.AdamsBashforthIntegratorTest::testIncreasingTolerance
    public void testIncreasingTolerance() throws DimensionMismatchException, NumberIsTooSmallException, MaxCountExceededException, NoBracketingException
        {

        int previousCalls = Integer.MAX_VALUE;
        for (int i = -12; i < -5; ++i) {
            TestProblem1 pb = new TestProblem1();
            double minStep = 0;
            double maxStep = pb.getFinalTime() - pb.getInitialTime();
            double scalAbsoluteTolerance = FastMath.pow(10.0, i);
            double scalRelativeTolerance = 0.01 * scalAbsoluteTolerance;

            FirstOrderIntegrator integ = new AdamsBashforthIntegrator(4, minStep, maxStep,
                                                                      scalAbsoluteTolerance,
                                                                      scalRelativeTolerance);
            TestProblemHandler handler = new TestProblemHandler(pb, integ);
            integ.addStepHandler(handler);
            integ.integrate(pb,
                            pb.getInitialTime(), pb.getInitialState(),
                            pb.getFinalTime(), new double[pb.getDimension()]);

            
            
            
            Assert.assertTrue(handler.getMaximalValueError() > (50.0 * scalAbsoluteTolerance));
            Assert.assertTrue(handler.getMaximalValueError() < (300.0 * scalAbsoluteTolerance));
            Assert.assertEquals(0, handler.getMaximalTimeError(), 1.0e-16);

            int calls = pb.getCalls();
            Assert.assertEquals(integ.getEvaluations(), calls);
            Assert.assertTrue(calls <= previousCalls);
            previousCalls = calls;

        }

    }

// org.apache.commons.math3.ode.nonstiff.AdamsBashforthIntegratorTest::exceedMaxEvaluations
    public void exceedMaxEvaluations() throws DimensionMismatchException, NumberIsTooSmallException, MaxCountExceededException, NoBracketingException {

        TestProblem1 pb  = new TestProblem1();
        double range = pb.getFinalTime() - pb.getInitialTime();

        AdamsBashforthIntegrator integ = new AdamsBashforthIntegrator(2, 0, range, 1.0e-12, 1.0e-12);
        TestProblemHandler handler = new TestProblemHandler(pb, integ);
        integ.addStepHandler(handler);
        integ.setMaxEvaluations(650);
        integ.integrate(pb,
                        pb.getInitialTime(), pb.getInitialState(),
                        pb.getFinalTime(), new double[pb.getDimension()]);

    }

// org.apache.commons.math3.ode.nonstiff.AdamsBashforthIntegratorTest::backward
    public void backward() throws DimensionMismatchException, NumberIsTooSmallException, MaxCountExceededException, NoBracketingException {

        TestProblem5 pb = new TestProblem5();
        double range = FastMath.abs(pb.getFinalTime() - pb.getInitialTime());

        FirstOrderIntegrator integ = new AdamsBashforthIntegrator(4, 0, range, 1.0e-12, 1.0e-12);
        TestProblemHandler handler = new TestProblemHandler(pb, integ);
        integ.addStepHandler(handler);
        integ.integrate(pb, pb.getInitialTime(), pb.getInitialState(),
                        pb.getFinalTime(), new double[pb.getDimension()]);

        Assert.assertTrue(handler.getLastError() < 1.5e-8);
        Assert.assertTrue(handler.getMaximalValueError() < 1.5e-8);
        Assert.assertEquals(0, handler.getMaximalTimeError(), 1.0e-16);
        Assert.assertEquals("Adams-Bashforth", integ.getName());
    }

// org.apache.commons.math3.ode.nonstiff.AdamsBashforthIntegratorTest::polynomial
    public void polynomial() throws DimensionMismatchException, NumberIsTooSmallException, MaxCountExceededException, NoBracketingException {
        TestProblem6 pb = new TestProblem6();
        double range = FastMath.abs(pb.getFinalTime() - pb.getInitialTime());

        for (int nSteps = 2; nSteps < 8; ++nSteps) {
            AdamsBashforthIntegrator integ =
                new AdamsBashforthIntegrator(nSteps, 1.0e-6 * range, 0.1 * range, 1.0e-5, 1.0e-5);
            TestProblemHandler handler = new TestProblemHandler(pb, integ);
            integ.addStepHandler(handler);
            integ.integrate(pb, pb.getInitialTime(), pb.getInitialState(),
                            pb.getFinalTime(), new double[pb.getDimension()]);
            if (nSteps < 4) {
                Assert.assertTrue(handler.getMaximalValueError() > 1.0e-03);
            } else {
                Assert.assertTrue(handler.getMaximalValueError() < 4.0e-12);
            }
        }

    }

// org.apache.commons.math3.ode.nonstiff.AdamsMoultonIntegratorTest::dimensionCheck
    public void dimensionCheck()
        throws DimensionMismatchException, NumberIsTooSmallException,
               MaxCountExceededException, NoBracketingException {
        TestProblem1 pb = new TestProblem1();
        FirstOrderIntegrator integ =
            new AdamsMoultonIntegrator(2, 0.0, 1.0, 1.0e-10, 1.0e-10);
        integ.integrate(pb,
                        0.0, new double[pb.getDimension()+10],
                        1.0, new double[pb.getDimension()+10]);
    }

// org.apache.commons.math3.ode.nonstiff.AdamsMoultonIntegratorTest::testMinStep
    public void testMinStep()
            throws DimensionMismatchException, NumberIsTooSmallException,
            MaxCountExceededException, NoBracketingException {

          TestProblem1 pb = new TestProblem1();
          double minStep = 0.1 * (pb.getFinalTime() - pb.getInitialTime());
          double maxStep = pb.getFinalTime() - pb.getInitialTime();
          double[] vecAbsoluteTolerance = { 1.0e-15, 1.0e-16 };
          double[] vecRelativeTolerance = { 1.0e-15, 1.0e-16 };

          FirstOrderIntegrator integ = new AdamsMoultonIntegrator(4, minStep, maxStep,
                                                                  vecAbsoluteTolerance,
                                                                  vecRelativeTolerance);
          TestProblemHandler handler = new TestProblemHandler(pb, integ);
          integ.addStepHandler(handler);
          integ.integrate(pb,
                          pb.getInitialTime(), pb.getInitialState(),
                          pb.getFinalTime(), new double[pb.getDimension()]);

    }

// org.apache.commons.math3.ode.nonstiff.AdamsMoultonIntegratorTest::testIncreasingTolerance
    public void testIncreasingTolerance()
            throws DimensionMismatchException, NumberIsTooSmallException,
            MaxCountExceededException, NoBracketingException {

        int previousCalls = Integer.MAX_VALUE;
        for (int i = -12; i < -2; ++i) {
            TestProblem1 pb = new TestProblem1();
            double minStep = 0;
            double maxStep = pb.getFinalTime() - pb.getInitialTime();
            double scalAbsoluteTolerance = FastMath.pow(10.0, i);
            double scalRelativeTolerance = 0.01 * scalAbsoluteTolerance;

            FirstOrderIntegrator integ = new AdamsMoultonIntegrator(4, minStep, maxStep,
                                                                    scalAbsoluteTolerance,
                                                                    scalRelativeTolerance);
            TestProblemHandler handler = new TestProblemHandler(pb, integ);
            integ.addStepHandler(handler);
            integ.integrate(pb,
                            pb.getInitialTime(), pb.getInitialState(),
                            pb.getFinalTime(), new double[pb.getDimension()]);

            
            
            
            Assert.assertTrue(handler.getMaximalValueError() > ( 0.5 * scalAbsoluteTolerance));
            Assert.assertTrue(handler.getMaximalValueError() < (11.0 * scalAbsoluteTolerance));
            Assert.assertEquals(0, handler.getMaximalTimeError(), 1.0e-16);

            int calls = pb.getCalls();
            Assert.assertEquals(integ.getEvaluations(), calls);
            Assert.assertTrue(calls <= previousCalls);
            previousCalls = calls;

        }

    }

// org.apache.commons.math3.ode.nonstiff.AdamsMoultonIntegratorTest::exceedMaxEvaluations
    public void exceedMaxEvaluations()
            throws DimensionMismatchException, NumberIsTooSmallException,
            MaxCountExceededException, NoBracketingException {

        TestProblem1 pb  = new TestProblem1();
        double range = pb.getFinalTime() - pb.getInitialTime();

        AdamsMoultonIntegrator integ = new AdamsMoultonIntegrator(2, 0, range, 1.0e-12, 1.0e-12);
        TestProblemHandler handler = new TestProblemHandler(pb, integ);
        integ.addStepHandler(handler);
        integ.setMaxEvaluations(650);
        integ.integrate(pb,
                        pb.getInitialTime(), pb.getInitialState(),
                        pb.getFinalTime(), new double[pb.getDimension()]);

    }

// org.apache.commons.math3.ode.nonstiff.AdamsMoultonIntegratorTest::backward
    public void backward()
            throws DimensionMismatchException, NumberIsTooSmallException,
            MaxCountExceededException, NoBracketingException {

        TestProblem5 pb = new TestProblem5();
        double range = FastMath.abs(pb.getFinalTime() - pb.getInitialTime());

        FirstOrderIntegrator integ = new AdamsMoultonIntegrator(4, 0, range, 1.0e-12, 1.0e-12);
        TestProblemHandler handler = new TestProblemHandler(pb, integ);
        integ.addStepHandler(handler);
        integ.integrate(pb, pb.getInitialTime(), pb.getInitialState(),
                        pb.getFinalTime(), new double[pb.getDimension()]);

        Assert.assertTrue(handler.getLastError() < 1.0e-9);
        Assert.assertTrue(handler.getMaximalValueError() < 1.0e-9);
        Assert.assertEquals(0, handler.getMaximalTimeError(), 1.0e-16);
        Assert.assertEquals("Adams-Moulton", integ.getName());
    }

// org.apache.commons.math3.ode.nonstiff.AdamsMoultonIntegratorTest::polynomial
    public void polynomial()
            throws DimensionMismatchException, NumberIsTooSmallException,
            MaxCountExceededException, NoBracketingException {
        TestProblem6 pb = new TestProblem6();
        double range = FastMath.abs(pb.getFinalTime() - pb.getInitialTime());

        for (int nSteps = 2; nSteps < 8; ++nSteps) {
            AdamsMoultonIntegrator integ =
                new AdamsMoultonIntegrator(nSteps, 1.0e-6 * range, 0.1 * range, 1.0e-5, 1.0e-5);
            TestProblemHandler handler = new TestProblemHandler(pb, integ);
            integ.addStepHandler(handler);
            integ.integrate(pb, pb.getInitialTime(), pb.getInitialState(),
                            pb.getFinalTime(), new double[pb.getDimension()]);
            if (nSteps < 4) {
                Assert.assertTrue(handler.getMaximalValueError() > 7.0e-04);
            } else {
                Assert.assertTrue(handler.getMaximalValueError() < 3.0e-13);
            }
        }

    }

// org.apache.commons.math3.ode.sampling.NordsieckStepInterpolatorTest::derivativesConsistency
    public void derivativesConsistency()
        throws NumberIsTooSmallException, DimensionMismatchException,
               MaxCountExceededException, NoBracketingException {
        TestProblem3 pb = new TestProblem3();
        AdamsBashforthIntegrator integ = new AdamsBashforthIntegrator(4, 0.0, 1.0, 1.0e-10, 1.0e-10);
        StepInterpolatorTestUtils.checkDerivativesConsistency(integ, pb, 5e-9);
    }

// org.apache.commons.math3.ode.sampling.NordsieckStepInterpolatorTest::serialization
    public void serialization()
    throws IOException, ClassNotFoundException,
           NumberIsTooSmallException, DimensionMismatchException,
           MaxCountExceededException, NoBracketingException {

        TestProblem1 pb = new TestProblem1();
        AdamsBashforthIntegrator integ = new AdamsBashforthIntegrator(4, 0.0, 1.0, 1.0e-10, 1.0e-10);
        integ.addStepHandler(new ContinuousOutputModel());
        integ.integrate(pb,
                        pb.getInitialTime(), pb.getInitialState(),
                        pb.getFinalTime(), new double[pb.getDimension()]);

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream    oos = new ObjectOutputStream(bos);
        for (StepHandler handler : integ.getStepHandlers()) {
            oos.writeObject(handler);
        }

        Assert.assertTrue(bos.size () >  25500);
        Assert.assertTrue(bos.size () <  26500);

        ByteArrayInputStream  bis = new ByteArrayInputStream(bos.toByteArray());
        ObjectInputStream     ois = new ObjectInputStream(bis);
        ContinuousOutputModel cm  = (ContinuousOutputModel) ois.readObject();

        Random random = new Random(347588535632l);
        double maxError = 0.0;
        for (int i = 0; i < 1000; ++i) {
            double r = random.nextDouble();
            double time = r * pb.getInitialTime() + (1.0 - r) * pb.getFinalTime();
            cm.setInterpolatedTime(time);
            double[] interpolatedY = cm.getInterpolatedState ();
            double[] theoreticalY  = pb.computeTheoreticalState(time);
            double dx = interpolatedY[0] - theoreticalY[0];
            double dy = interpolatedY[1] - theoreticalY[1];
            double error = dx * dx + dy * dy;
            if (error > maxError) {
                maxError = error;
            }
        }

        Assert.assertTrue(maxError < 1.0e-6);

    }

// org.apache.commons.math3.optim.nonlinear.scalar.noderiv.CMAESOptimizerTest::testInitOutofbounds1
    public void testInitOutofbounds1() {
        double[] startPoint = point(DIM,3);
        double[] insigma = point(DIM, 0.3);
        double[][] boundaries = boundaries(DIM,-1,2);
        PointValuePair expected =
            new PointValuePair(point(DIM,1.0),0.0);
        doTest(new Rosen(), startPoint, insigma, boundaries,
                GoalType.MINIMIZE, LAMBDA, true, 0, 1e-13,
                1e-13, 1e-6, 100000, expected);
    }

// org.apache.commons.math3.optim.nonlinear.scalar.noderiv.CMAESOptimizerTest::testInitOutofbounds2
    public void testInitOutofbounds2() {
        double[] startPoint = point(DIM, -2);
        double[] insigma = point(DIM, 0.3);
        double[][] boundaries = boundaries(DIM,-1,2);
        PointValuePair expected =
            new PointValuePair(point(DIM,1.0),0.0);
        doTest(new Rosen(), startPoint, insigma, boundaries,
                GoalType.MINIMIZE, LAMBDA, true, 0, 1e-13,
                1e-13, 1e-6, 100000, expected);
    }

// org.apache.commons.math3.optim.nonlinear.scalar.noderiv.CMAESOptimizerTest::testBoundariesDimensionMismatch
    public void testBoundariesDimensionMismatch() {
        double[] startPoint = point(DIM,0.5);
        double[] insigma = point(DIM, 0.3);
        double[][] boundaries = boundaries(DIM+1,-1,2);
        PointValuePair expected =
            new PointValuePair(point(DIM,1.0),0.0);
        doTest(new Rosen(), startPoint, insigma, boundaries,
                GoalType.MINIMIZE, LAMBDA, true, 0, 1e-13,
                1e-13, 1e-6, 100000, expected);
    }

// org.apache.commons.math3.optim.nonlinear.scalar.noderiv.CMAESOptimizerTest::testInputSigmaNegative
    public void testInputSigmaNegative() {
        double[] startPoint = point(DIM,0.5);
        double[] insigma = point(DIM,-0.5);
        double[][] boundaries = null;
        PointValuePair expected =
            new PointValuePair(point(DIM,1.0),0.0);
        doTest(new Rosen(), startPoint, insigma, boundaries,
                GoalType.MINIMIZE, LAMBDA, true, 0, 1e-13,
                1e-13, 1e-6, 100000, expected);
    }

// org.apache.commons.math3.optim.nonlinear.scalar.noderiv.CMAESOptimizerTest::testInputSigmaOutOfRange
    public void testInputSigmaOutOfRange() {
        double[] startPoint = point(DIM,0.5);
        double[] insigma = point(DIM, 1.1);
        double[][] boundaries = boundaries(DIM,-0.5,0.5);
        PointValuePair expected =
            new PointValuePair(point(DIM,1.0),0.0);
        doTest(new Rosen(), startPoint, insigma, boundaries,
                GoalType.MINIMIZE, LAMBDA, true, 0, 1e-13,
                1e-13, 1e-6, 100000, expected);
    }

// org.apache.commons.math3.optim.nonlinear.scalar.noderiv.CMAESOptimizerTest::testInputSigmaDimensionMismatch
    public void testInputSigmaDimensionMismatch() {
        double[] startPoint = point(DIM,0.5);
        double[] insigma = point(DIM + 1, 0.5);
        double[][] boundaries = null;
        PointValuePair expected =
            new PointValuePair(point(DIM,1.0),0.0);
        doTest(new Rosen(), startPoint, insigma, boundaries,
                GoalType.MINIMIZE, LAMBDA, true, 0, 1e-13,
                1e-13, 1e-6, 100000, expected);
    }

// org.apache.commons.math3.optim.nonlinear.scalar.noderiv.CMAESOptimizerTest::testRosen
    public void testRosen() {
        double[] startPoint = point(DIM,0.1);
        double[] insigma = point(DIM,0.1);
        double[][] boundaries = null;
        PointValuePair expected =
            new PointValuePair(point(DIM,1.0),0.0);
        doTest(new Rosen(), startPoint, insigma, boundaries,
                GoalType.MINIMIZE, LAMBDA, true, 0, 1e-13,
                1e-13, 1e-6, 100000, expected);
        doTest(new Rosen(), startPoint, insigma, boundaries,
                GoalType.MINIMIZE, LAMBDA, false, 0, 1e-13,
                1e-13, 1e-6, 100000, expected);
    }

// org.apache.commons.math3.optim.nonlinear.scalar.noderiv.CMAESOptimizerTest::testMaximize
    public void testMaximize() {
        double[] startPoint = point(DIM,1.0);
        double[] insigma = point(DIM,0.1);
        double[][] boundaries = null;
        PointValuePair expected =
            new PointValuePair(point(DIM,0.0),1.0);
        doTest(new MinusElli(), startPoint, insigma, boundaries,
                GoalType.MAXIMIZE, LAMBDA, true, 0, 1.0-1e-13,
                2e-10, 5e-6, 100000, expected);
        doTest(new MinusElli(), startPoint, insigma, boundaries,
                GoalType.MAXIMIZE, LAMBDA, false, 0, 1.0-1e-13,
                2e-10, 5e-6, 100000, expected);
        boundaries = boundaries(DIM,-0.3,0.3); 
        startPoint = point(DIM,0.1);
        doTest(new MinusElli(), startPoint, insigma, boundaries,
                GoalType.MAXIMIZE, LAMBDA, true, 0, 1.0-1e-13,
                2e-10, 5e-6, 100000, expected);
    }

// org.apache.commons.math3.optim.nonlinear.scalar.noderiv.CMAESOptimizerTest::testEllipse
    public void testEllipse() {
        double[] startPoint = point(DIM,1.0);
        double[] insigma = point(DIM,0.1);
        double[][] boundaries = null;
        PointValuePair expected =
            new PointValuePair(point(DIM,0.0),0.0);
        doTest(new Elli(), startPoint, insigma, boundaries,
                GoalType.MINIMIZE, LAMBDA, true, 0, 1e-13,
                1e-13, 1e-6, 100000, expected);
        doTest(new Elli(), startPoint, insigma, boundaries,
                GoalType.MINIMIZE, LAMBDA, false, 0, 1e-13,
                1e-13, 1e-6, 100000, expected);
    }

// org.apache.commons.math3.optim.nonlinear.scalar.noderiv.CMAESOptimizerTest::testElliRotated
    public void testElliRotated() {
        double[] startPoint = point(DIM,1.0);
        double[] insigma = point(DIM,0.1);
        double[][] boundaries = null;
        PointValuePair expected =
            new PointValuePair(point(DIM,0.0),0.0);
        doTest(new ElliRotated(), startPoint, insigma, boundaries,
                GoalType.MINIMIZE, LAMBDA, true, 0, 1e-13,
                1e-13, 1e-6, 100000, expected);
        doTest(new ElliRotated(), startPoint, insigma, boundaries,
                GoalType.MINIMIZE, LAMBDA, false, 0, 1e-13,
                1e-13, 1e-6, 100000, expected);
    }

// org.apache.commons.math3.optim.nonlinear.scalar.noderiv.CMAESOptimizerTest::testCigar
    public void testCigar() {
        double[] startPoint = point(DIM,1.0);
        double[] insigma = point(DIM,0.1);
        double[][] boundaries = null;
        PointValuePair expected =
            new PointValuePair(point(DIM,0.0),0.0);
        doTest(new Cigar(), startPoint, insigma, boundaries,
                GoalType.MINIMIZE, LAMBDA, true, 0, 1e-13,
                1e-13, 1e-6, 200000, expected);
        doTest(new Cigar(), startPoint, insigma, boundaries,
                GoalType.MINIMIZE, LAMBDA, false, 0, 1e-13,
                1e-13, 1e-6, 100000, expected);
    }

// org.apache.commons.math3.optim.nonlinear.scalar.noderiv.CMAESOptimizerTest::testCigarWithBoundaries
    public void testCigarWithBoundaries() {
        double[] startPoint = point(DIM,1.0);
        double[] insigma = point(DIM,0.1);
        double[][] boundaries = boundaries(DIM, -1e100, Double.POSITIVE_INFINITY);
        PointValuePair expected =
            new PointValuePair(point(DIM,0.0),0.0);
        doTest(new Cigar(), startPoint, insigma, boundaries,
                GoalType.MINIMIZE, LAMBDA, true, 0, 1e-13,
                1e-13, 1e-6, 200000, expected);
        doTest(new Cigar(), startPoint, insigma, boundaries,
                GoalType.MINIMIZE, LAMBDA, false, 0, 1e-13,
                1e-13, 1e-6, 100000, expected);
    }

// org.apache.commons.math3.optim.nonlinear.scalar.noderiv.CMAESOptimizerTest::testTwoAxes
    public void testTwoAxes() {
        double[] startPoint = point(DIM,1.0);
        double[] insigma = point(DIM,0.1);
        double[][] boundaries = null;
        PointValuePair expected =
            new PointValuePair(point(DIM,0.0),0.0);
        doTest(new TwoAxes(), startPoint, insigma, boundaries,
                GoalType.MINIMIZE, 2*LAMBDA, true, 0, 1e-13,
                1e-13, 1e-6, 200000, expected);
        doTest(new TwoAxes(), startPoint, insigma, boundaries,
                GoalType.MINIMIZE, 2*LAMBDA, false, 0, 1e-13,
                1e-8, 1e-3, 200000, expected);
    }

// org.apache.commons.math3.optim.nonlinear.scalar.noderiv.CMAESOptimizerTest::testCigTab
    public void testCigTab() {
        double[] startPoint = point(DIM,1.0);
        double[] insigma = point(DIM,0.3);
        double[][] boundaries = null;
        PointValuePair expected =
            new PointValuePair(point(DIM,0.0),0.0);
        doTest(new CigTab(), startPoint, insigma, boundaries,
                GoalType.MINIMIZE, LAMBDA, true, 0, 1e-13,
                1e-13, 5e-5, 100000, expected);
        doTest(new CigTab(), startPoint, insigma, boundaries,
                GoalType.MINIMIZE, LAMBDA, false, 0, 1e-13,
                1e-13, 5e-5, 100000, expected);
    }

// org.apache.commons.math3.optim.nonlinear.scalar.noderiv.CMAESOptimizerTest::testSphere
    public void testSphere() {
        double[] startPoint = point(DIM,1.0);
        double[] insigma = point(DIM,0.1);
        double[][] boundaries = null;
        PointValuePair expected =
            new PointValuePair(point(DIM,0.0),0.0);
        doTest(new Sphere(), startPoint, insigma, boundaries,
                GoalType.MINIMIZE, LAMBDA, true, 0, 1e-13,
                1e-13, 1e-6, 100000, expected);
        doTest(new Sphere(), startPoint, insigma, boundaries,
                GoalType.MINIMIZE, LAMBDA, false, 0, 1e-13,
                1e-13, 1e-6, 100000, expected);
    }

// org.apache.commons.math3.optim.nonlinear.scalar.noderiv.CMAESOptimizerTest::testTablet
    public void testTablet() {
        double[] startPoint = point(DIM,1.0);
        double[] insigma = point(DIM,0.1);
        double[][] boundaries = null;
        PointValuePair expected =
            new PointValuePair(point(DIM,0.0),0.0);
        doTest(new Tablet(), startPoint, insigma, boundaries,
                GoalType.MINIMIZE, LAMBDA, true, 0, 1e-13,
                1e-13, 1e-6, 100000, expected);
        doTest(new Tablet(), startPoint, insigma, boundaries,
                GoalType.MINIMIZE, LAMBDA, false, 0, 1e-13,
                1e-13, 1e-6, 100000, expected);
    }

// org.apache.commons.math3.optim.nonlinear.scalar.noderiv.CMAESOptimizerTest::testDiffPow
    public void testDiffPow() {
        double[] startPoint = point(DIM,1.0);
        double[] insigma = point(DIM,0.1);
        double[][] boundaries = null;
        PointValuePair expected =
            new PointValuePair(point(DIM,0.0),0.0);
        doTest(new DiffPow(), startPoint, insigma, boundaries,
                GoalType.MINIMIZE, 10, true, 0, 1e-13,
                1e-8, 1e-1, 100000, expected);
        doTest(new DiffPow(), startPoint, insigma, boundaries,
                GoalType.MINIMIZE, 10, false, 0, 1e-13,
                1e-8, 2e-1, 100000, expected);
    }

// org.apache.commons.math3.optim.nonlinear.scalar.noderiv.CMAESOptimizerTest::testSsDiffPow
    public void testSsDiffPow() {
        double[] startPoint = point(DIM,1.0);
        double[] insigma = point(DIM,0.1);
        double[][] boundaries = null;
        PointValuePair expected =
            new PointValuePair(point(DIM,0.0),0.0);
        doTest(new SsDiffPow(), startPoint, insigma, boundaries,
                GoalType.MINIMIZE, 10, true, 0, 1e-13,
                1e-4, 1e-1, 200000, expected);
        doTest(new SsDiffPow(), startPoint, insigma, boundaries,
                GoalType.MINIMIZE, 10, false, 0, 1e-13,
                1e-4, 1e-1, 200000, expected);
    }

// org.apache.commons.math3.optim.nonlinear.scalar.noderiv.CMAESOptimizerTest::testAckley
    public void testAckley() {
        double[] startPoint = point(DIM,1.0);
        double[] insigma = point(DIM,1.0);
        double[][] boundaries = null;
        PointValuePair expected =
            new PointValuePair(point(DIM,0.0),0.0);
        doTest(new Ackley(), startPoint, insigma, boundaries,
                GoalType.MINIMIZE, 2*LAMBDA, true, 0, 1e-13,
                1e-9, 1e-5, 100000, expected);
        doTest(new Ackley(), startPoint, insigma, boundaries,
                GoalType.MINIMIZE, 2*LAMBDA, false, 0, 1e-13,
                1e-9, 1e-5, 100000, expected);
    }

// org.apache.commons.math3.optim.nonlinear.scalar.noderiv.CMAESOptimizerTest::testRastrigin
    public void testRastrigin() {
        double[] startPoint = point(DIM,0.1);
        double[] insigma = point(DIM,0.1);
        double[][] boundaries = null;
        PointValuePair expected =
            new PointValuePair(point(DIM,0.0),0.0);
        doTest(new Rastrigin(), startPoint, insigma, boundaries,
                GoalType.MINIMIZE, (int)(200*Math.sqrt(DIM)), true, 0, 1e-13,
                1e-13, 1e-6, 200000, expected);
        doTest(new Rastrigin(), startPoint, insigma, boundaries,
                GoalType.MINIMIZE, (int)(200*Math.sqrt(DIM)), false, 0, 1e-13,
                1e-13, 1e-6, 200000, expected);
    }

// org.apache.commons.math3.optim.nonlinear.scalar.noderiv.CMAESOptimizerTest::testConstrainedRosen
    public void testConstrainedRosen() {
        double[] startPoint = point(DIM, 0.1);
        double[] insigma = point(DIM, 0.1);
        double[][] boundaries = boundaries(DIM, -1, 2);
        PointValuePair expected =
            new PointValuePair(point(DIM,1.0),0.0);
        doTest(new Rosen(), startPoint, insigma, boundaries,
                GoalType.MINIMIZE, 2*LAMBDA, true, 0, 1e-13,
                1e-13, 1e-6, 100000, expected);
        doTest(new Rosen(), startPoint, insigma, boundaries,
                GoalType.MINIMIZE, 2*LAMBDA, false, 0, 1e-13,
                1e-13, 1e-6, 100000, expected);
    }

// org.apache.commons.math3.optim.nonlinear.scalar.noderiv.CMAESOptimizerTest::testDiagonalRosen
    public void testDiagonalRosen() {
        double[] startPoint = point(DIM,0.1);
        double[] insigma = point(DIM,0.1);
        double[][] boundaries = null;
        PointValuePair expected =
            new PointValuePair(point(DIM,1.0),0.0);
        doTest(new Rosen(), startPoint, insigma, boundaries,
                GoalType.MINIMIZE, LAMBDA, false, 1, 1e-13,
                1e-10, 1e-4, 1000000, expected);
     }

// org.apache.commons.math3.optim.nonlinear.scalar.noderiv.CMAESOptimizerTest::testMath864
    public void testMath864() {
        final CMAESOptimizer optimizer
            = new CMAESOptimizer(30000, 0, true, 10,
                                 0, new MersenneTwister(), false, null);
        final MultivariateFunction fitnessFunction = new MultivariateFunction() {
                public double value(double[] parameters) {
                    final double target = 1;
                    final double error = target - parameters[0];
                    return error * error;
                }
            };

        final double[] start = { 0 };
        final double[] lower = { -1e6 };
        final double[] upper = { 1.5 };
        final double[] sigma = { 1e-1 };
        final double[] result = optimizer.optimize(new MaxEval(10000),
                                                   new ObjectiveFunction(fitnessFunction),
                                                   GoalType.MINIMIZE,
                                                   new CMAESOptimizer.PopulationSize(5),
                                                   new CMAESOptimizer.Sigma(sigma),
                                                   new InitialGuess(start),
                                                   new SimpleBounds(lower, upper)).getPoint();
        Assert.assertTrue("Out of bounds (" + result[0] + " > " + upper[0] + ")",
                          result[0] <= upper[0]);
    }

// org.apache.commons.math3.optim.nonlinear.scalar.noderiv.CMAESOptimizerTest::testFitAccuracyDependsOnBoundary
    public void testFitAccuracyDependsOnBoundary() {
        final CMAESOptimizer optimizer
            = new CMAESOptimizer(30000, 0, true, 10,
                                 0, new MersenneTwister(), false, null);
        final MultivariateFunction fitnessFunction = new MultivariateFunction() {
                public double value(double[] parameters) {
                    final double target = 11.1;
                    final double error = target - parameters[0];
                    return error * error;
                }
            };

        final double[] start = { 1 };
 
        
        PointValuePair result = optimizer.optimize(new MaxEval(100000),
                                                   new ObjectiveFunction(fitnessFunction),
                                                   GoalType.MINIMIZE,
                                                   SimpleBounds.unbounded(1),
                                                   new CMAESOptimizer.PopulationSize(5),
                                                   new CMAESOptimizer.Sigma(new double[] { 1e-1 }),
                                                   new InitialGuess(start));
        final double resNoBound = result.getPoint()[0];

        
        final double[] lower = { -20 };
        final double[] upper = { 5e16 };
        final double[] sigma = { 10 };
        result = optimizer.optimize(new MaxEval(100000),
                                    new ObjectiveFunction(fitnessFunction),
                                    GoalType.MINIMIZE,
                                    new CMAESOptimizer.PopulationSize(5),
                                    new CMAESOptimizer.Sigma(sigma),
                                    new InitialGuess(start),
                                    new SimpleBounds(lower, upper));
        final double resNearLo = result.getPoint()[0];

        
        lower[0] = -5e16;
        upper[0] = 20;
        result = optimizer.optimize(new MaxEval(100000),
                                    new ObjectiveFunction(fitnessFunction),
                                    GoalType.MINIMIZE,
                                    new CMAESOptimizer.PopulationSize(5),
                                    new CMAESOptimizer.Sigma(sigma),
                                    new InitialGuess(start),
                                    new SimpleBounds(lower, upper));
        final double resNearHi = result.getPoint()[0];

        
        
        

        
        
        Assert.assertEquals(resNoBound, resNearLo, 1e-3);
        Assert.assertEquals(resNoBound, resNearHi, 1e-3);
    }

// org.apache.commons.math3.optim.nonlinear.scalar.noderiv.PowellOptimizerTest::testBoundsUnsupported
    public void testBoundsUnsupported() {
        final MultivariateFunction func = new SumSincFunction(-1);
        final PowellOptimizer optim = new PowellOptimizer(1e-8, 1e-5,
                                                          1e-4, 1e-4);

        optim.optimize(new MaxEval(100),
                       new ObjectiveFunction(func),
                       GoalType.MINIMIZE,
                       new InitialGuess(new double[] { -3, 0 }),
                       new SimpleBounds(new double[] { -5, -1 },
                                        new double[] { 5, 1 }));
    }

// org.apache.commons.math3.optim.nonlinear.scalar.noderiv.PowellOptimizerTest::testSumSinc
    public void testSumSinc() {
        final MultivariateFunction func = new SumSincFunction(-1);

        int dim = 2;
        final double[] minPoint = new double[dim];
        for (int i = 0; i < dim; i++) {
            minPoint[i] = 0;
        }

        double[] init = new double[dim];

        
        for (int i = 0; i < dim; i++) {
            init[i] = minPoint[i];
        }
        doTest(func, minPoint, init, GoalType.MINIMIZE, 1e-9, 1e-9);

        
        for (int i = 0; i < dim; i++) {
            init[i] = minPoint[i] + 3;
        }
        doTest(func, minPoint, init, GoalType.MINIMIZE, 1e-9, 1e-5);
        
        
        doTest(func, minPoint, init, GoalType.MINIMIZE, 1e-9, 1e-9, 1e-7);
    }

// org.apache.commons.math3.optim.nonlinear.scalar.noderiv.PowellOptimizerTest::testQuadratic
    public void testQuadratic() {
        final MultivariateFunction func = new MultivariateFunction() {
                public double value(double[] x) {
                    final double a = x[0] - 1;
                    final double b = x[1] - 1;
                    return a * a + b * b + 1;
                }
            };

        int dim = 2;
        final double[] minPoint = new double[dim];
        for (int i = 0; i < dim; i++) {
            minPoint[i] = 1;
        }

        double[] init = new double[dim];

        
        for (int i = 0; i < dim; i++) {
            init[i] = minPoint[i];
        }
        doTest(func, minPoint, init, GoalType.MINIMIZE, 1e-9, 1e-8);

        
        for (int i = 0; i < dim; i++) {
            init[i] = minPoint[i] - 20;
        }
        doTest(func, minPoint, init, GoalType.MINIMIZE, 1e-9, 1e-8);
    }

// org.apache.commons.math3.optim.nonlinear.scalar.noderiv.PowellOptimizerTest::testMaximizeQuadratic
    public void testMaximizeQuadratic() {
        final MultivariateFunction func = new MultivariateFunction() {
                public double value(double[] x) {
                    final double a = x[0] - 1;
                    final double b = x[1] - 1;
                    return -a * a - b * b + 1;
                }
            };

        int dim = 2;
        final double[] maxPoint = new double[dim];
        for (int i = 0; i < dim; i++) {
            maxPoint[i] = 1;
        }

        double[] init = new double[dim];

        
        for (int i = 0; i < dim; i++) {
            init[i] = maxPoint[i];
        }
        doTest(func, maxPoint, init,  GoalType.MAXIMIZE, 1e-9, 1e-8);

        
        for (int i = 0; i < dim; i++) {
            init[i] = maxPoint[i] - 20;
        }
        doTest(func, maxPoint, init, GoalType.MAXIMIZE, 1e-9, 1e-8);
    }

// org.apache.commons.math3.optim.nonlinear.scalar.noderiv.PowellOptimizerTest::testRelativeToleranceOnScaledValues
    public void testRelativeToleranceOnScaledValues() {
        final MultivariateFunction func = new MultivariateFunction() {
                public double value(double[] x) {
                    final double a = x[0] - 1;
                    final double b = x[1] - 1;
                    return a * a * FastMath.sqrt(FastMath.abs(a)) + b * b + 1;
                }
            };

        int dim = 2;
        final double[] minPoint = new double[dim];
        for (int i = 0; i < dim; i++) {
            minPoint[i] = 1;
        }

        double[] init = new double[dim];
        
        for (int i = 0; i < dim; i++) {
            init[i] = minPoint[i] - 20;
        }

        final double relTol = 1e-10;

        final int maxEval = 1000;
        
        
        final PowellOptimizer optim = new PowellOptimizer(relTol, 1e-100);

        final PointValuePair funcResult = optim.optimize(new MaxEval(maxEval),
                                                         new ObjectiveFunction(func),
                                                         GoalType.MINIMIZE,
                                                         new InitialGuess(init));
        final double funcValue = func.value(funcResult.getPoint());
        final int funcEvaluations = optim.getEvaluations();

        final double scale = 1e10;
        final MultivariateFunction funcScaled = new MultivariateFunction() {
                public double value(double[] x) {
                    return scale * func.value(x);
                }
            };

        final PointValuePair funcScaledResult = optim.optimize(new MaxEval(maxEval),
                                                               new ObjectiveFunction(funcScaled),
                                                               GoalType.MINIMIZE,
                                                               new InitialGuess(init));
        final double funcScaledValue = funcScaled.value(funcScaledResult.getPoint());
        final int funcScaledEvaluations = optim.getEvaluations();

        
        
        Assert.assertEquals(1, funcScaledValue / (scale * funcValue), relTol);

        
        Assert.assertEquals(funcEvaluations, funcScaledEvaluations);
    }

// org.apache.commons.math3.optim.nonlinear.vector.jacobian.GaussNewtonOptimizerTest::testConstraintsUnsupported
    public void testConstraintsUnsupported() {
        createOptimizer().optimize(new MaxEval(100),
                                   new Target(new double[] { 2 }),
                                   new Weight(new double[] { 1 }),
                                   new InitialGuess(new double[] { 1, 2 }),
                                   new SimpleBounds(new double[] { -10, 0 },
                                                    new double[] { 20, 30 }));
    }

// org.apache.commons.math3.optim.nonlinear.vector.jacobian.GaussNewtonOptimizerTest::testMoreEstimatedParametersSimple
    public void testMoreEstimatedParametersSimple() {
        
        super.testMoreEstimatedParametersSimple();
    }

// org.apache.commons.math3.optim.nonlinear.vector.jacobian.GaussNewtonOptimizerTest::testMoreEstimatedParametersUnsorted
    public void testMoreEstimatedParametersUnsorted() {
        
        super.testMoreEstimatedParametersUnsorted();
    }

// org.apache.commons.math3.optim.nonlinear.vector.jacobian.GaussNewtonOptimizerTest::testMaxEvaluations
    public void testMaxEvaluations() throws Exception {
        CircleVectorial circle = new CircleVectorial();
        circle.addPoint( 30.0,  68.0);
        circle.addPoint( 50.0,  -6.0);
        circle.addPoint(110.0, -20.0);
        circle.addPoint( 35.0,  15.0);
        circle.addPoint( 45.0,  97.0);

        GaussNewtonOptimizer optimizer
            = new GaussNewtonOptimizer(new SimpleVectorValueChecker(1e-30, 1e-30));

        optimizer.optimize(new MaxEval(100),
                           circle.getModelFunction(),
                           circle.getModelFunctionJacobian(),
                           new Target(new double[] { 0, 0, 0, 0, 0 }),
                           new Weight(new double[] { 1, 1, 1, 1, 1 }),
                           new InitialGuess(new double[] { 98.680, 47.345 }));
    }

// org.apache.commons.math3.optim.nonlinear.vector.jacobian.GaussNewtonOptimizerTest::testCircleFittingBadInit
    public void testCircleFittingBadInit() {
        
        super.testCircleFittingBadInit();
    }

// org.apache.commons.math3.optim.nonlinear.vector.jacobian.GaussNewtonOptimizerTest::testHahn1
    public void testHahn1()
        throws IOException {
        
        super.testHahn1();
    }

// org.apache.commons.math3.optim.nonlinear.vector.jacobian.LevenbergMarquardtOptimizerTest::testConstraintsUnsupported
    public void testConstraintsUnsupported() {
        createOptimizer().optimize(new MaxEval(100),
                                   new Target(new double[] { 2 }),
                                   new Weight(new double[] { 1 }),
                                   new InitialGuess(new double[] { 1, 2 }),
                                   new SimpleBounds(new double[] { -10, 0 },
                                                    new double[] { 20, 30 }));
    }

// org.apache.commons.math3.optim.nonlinear.vector.jacobian.LevenbergMarquardtOptimizerTest::testNonInvertible
    public void testNonInvertible() {
        
        LinearProblem problem = new LinearProblem(new double[][] {
                {  1, 2, -3 },
                {  2, 1,  3 },
                { -3, 0, -9 }
        }, new double[] { 1, 1, 1 });

        AbstractLeastSquaresOptimizer optimizer = createOptimizer();
        PointVectorValuePair optimum
            = optimizer.optimize(new MaxEval(100),
                                 problem.getModelFunction(),
                                 problem.getModelFunctionJacobian(),
                                 problem.getTarget(),
                                 new Weight(new double[] { 1, 1, 1 }),
                                 new InitialGuess(new double[] { 0, 0, 0 }));
        Assert.assertTrue(FastMath.sqrt(optimizer.getTargetSize()) * optimizer.getRMS() > 0.6);

        optimizer.computeCovariances(optimum.getPoint(), 1.5e-14);
    }

// org.apache.commons.math3.optim.nonlinear.vector.jacobian.LevenbergMarquardtOptimizerTest::testControlParameters
    public void testControlParameters() {
        CircleVectorial circle = new CircleVectorial();
        circle.addPoint( 30.0,  68.0);
        circle.addPoint( 50.0,  -6.0);
        circle.addPoint(110.0, -20.0);
        circle.addPoint( 35.0,  15.0);
        circle.addPoint( 45.0,  97.0);
        checkEstimate(circle.getModelFunction(),
                      circle.getModelFunctionJacobian(),
                      0.1, 10, 1.0e-14, 1.0e-16, 1.0e-10, false);
        checkEstimate(circle.getModelFunction(),
                      circle.getModelFunctionJacobian(),
                      0.1, 10, 1.0e-15, 1.0e-17, 1.0e-10, true);
        checkEstimate(circle.getModelFunction(),
                      circle.getModelFunctionJacobian(),
                      0.1,  5, 1.0e-15, 1.0e-16, 1.0e-10, true);
        circle.addPoint(300, -300);
        checkEstimate(circle.getModelFunction(),
                      circle.getModelFunctionJacobian(),
                      0.1, 20, 1.0e-18, 1.0e-16, 1.0e-10, true);
    }

// org.apache.commons.math3.optim.nonlinear.vector.jacobian.LevenbergMarquardtOptimizerTest::testBevington
    public void testBevington() {
        final double[][] dataPoints = {
            
            { 15, 30, 45, 60, 75, 90, 105, 120, 135, 150,
              165, 180, 195, 210, 225, 240, 255, 270, 285, 300,
              315, 330, 345, 360, 375, 390, 405, 420, 435, 450,
              465, 480, 495, 510, 525, 540, 555, 570, 585, 600,
              615, 630, 645, 660, 675, 690, 705, 720, 735, 750,
              765, 780, 795, 810, 825, 840, 855, 870, 885, },
            
            { 775, 479, 380, 302, 185, 157, 137, 119, 110, 89,
              74, 61, 66, 68, 48, 54, 51, 46, 55, 29,
              28, 37, 49, 26, 35, 29, 31, 24, 25, 35,
              24, 30, 26, 28, 21, 18, 20, 27, 17, 17,
              14, 17, 24, 11, 22, 17, 12, 10, 13, 16,
              9, 9, 14, 21, 17, 13, 12, 18, 10, },
        };

        final BevingtonProblem problem = new BevingtonProblem();

        final int len = dataPoints[0].length;
        final double[] weights = new double[len];
        for (int i = 0; i < len; i++) {
            problem.addPoint(dataPoints[0][i],
                             dataPoints[1][i]);

            weights[i] = 1 / dataPoints[1][i];
        }

        final LevenbergMarquardtOptimizer optimizer
            = new LevenbergMarquardtOptimizer();

        final PointVectorValuePair optimum
            = optimizer.optimize(new MaxEval(100),
                                 problem.getModelFunction(),
                                 problem.getModelFunctionJacobian(),
                                 new Target(dataPoints[1]),
                                 new Weight(weights),
                                 new InitialGuess(new double[] { 10, 900, 80, 27, 225 }));

        final double[] solution = optimum.getPoint();
        final double[] expectedSolution = { 10.4, 958.3, 131.4, 33.9, 205.0 };

        final double[][] covarMatrix = optimizer.computeCovariances(solution, 1e-14);
        final double[][] expectedCovarMatrix = {
            { 3.38, -3.69, 27.98, -2.34, -49.24 },
            { -3.69, 2492.26, 81.89, -69.21, -8.9 },
            { 27.98, 81.89, 468.99, -44.22, -615.44 },
            { -2.34, -69.21, -44.22, 6.39, 53.80 },
            { -49.24, -8.9, -615.44, 53.8, 929.45 }
        };

        final int numParams = expectedSolution.length;

        
        for (int i = 0; i < numParams; i++) {
            final double error = FastMath.sqrt(expectedCovarMatrix[i][i]);
            Assert.assertEquals("Parameter " + i, expectedSolution[i], solution[i], error);
        }

        
        
        for (int i = 0; i < numParams; i++) {
            for (int j = 0; j < numParams; j++) {
                Assert.assertEquals("Covariance matrix [" + i + "][" + j + "]",
                                    expectedCovarMatrix[i][j],
                                    covarMatrix[i][j],
                                    FastMath.abs(0.1 * expectedCovarMatrix[i][j]));
            }
        }
    }

// org.apache.commons.math3.optim.nonlinear.vector.jacobian.LevenbergMarquardtOptimizerTest::testCircleFitting2
    public void testCircleFitting2() {
        final double xCenter = 123.456;
        final double yCenter = 654.321;
        final double xSigma = 10;
        final double ySigma = 15;
        final double radius = 111.111;
        
        final long seed = 59421061L;
        final RandomCirclePointGenerator factory
            = new RandomCirclePointGenerator(xCenter, yCenter, radius,
                                             xSigma, ySigma,
                                             seed);
        final CircleProblem circle = new CircleProblem(xSigma, ySigma);

        final int numPoints = 10;
        for (Vector2D p : factory.generate(numPoints)) {
            circle.addPoint(p.getX(), p.getY());
        }

        
        final double[] init = { 90, 659, 115 };

        final LevenbergMarquardtOptimizer optimizer
            = new LevenbergMarquardtOptimizer();
        final PointVectorValuePair optimum = optimizer.optimize(new MaxEval(100),
                                                                circle.getModelFunction(),
                                                                circle.getModelFunctionJacobian(),
                                                                new Target(circle.target()),
                                                                new Weight(circle.weight()),
                                                                new InitialGuess(init));

        final double[] paramFound = optimum.getPoint();

        
        final double[] asymptoticStandardErrorFound = optimizer.computeSigma(paramFound, 1e-14);

        
        Assert.assertEquals(xCenter, paramFound[0], asymptoticStandardErrorFound[0]);
        Assert.assertEquals(yCenter, paramFound[1], asymptoticStandardErrorFound[1]);
        Assert.assertEquals(radius, paramFound[2], asymptoticStandardErrorFound[2]);
    }

// org.apache.commons.math3.optim.univariate.BrentOptimizerTest::testSinMin
    public void testSinMin() {
        UnivariateFunction f = new Sin();
        UnivariateOptimizer optimizer = new BrentOptimizer(1e-10, 1e-14);
        Assert.assertEquals(3 * Math.PI / 2, optimizer.optimize(new MaxEval(200),
                                                                new UnivariateObjectiveFunction(f),
                                                                GoalType.MINIMIZE,
                                                                new SearchInterval(4, 5)).getPoint(), 1e-8);
        Assert.assertTrue(optimizer.getEvaluations() <= 50);
        Assert.assertEquals(200, optimizer.getMaxEvaluations());
        Assert.assertEquals(3 * Math.PI / 2, optimizer.optimize(new MaxEval(200),
                                                                new UnivariateObjectiveFunction(f),
                                                                GoalType.MINIMIZE,
                                                                new SearchInterval(1, 5)).getPoint(), 1e-8);
        Assert.assertTrue(optimizer.getEvaluations() <= 100);
        Assert.assertTrue(optimizer.getEvaluations() >= 15);
        try {
            optimizer.optimize(new MaxEval(10),
                               new UnivariateObjectiveFunction(f),
                               GoalType.MINIMIZE,
                               new SearchInterval(4, 5));
            Assert.fail("an exception should have been thrown");
        } catch (TooManyEvaluationsException fee) {
            
        }
    }

// org.apache.commons.math3.optim.univariate.BrentOptimizerTest::testSinMinWithValueChecker
    public void testSinMinWithValueChecker() {
        final UnivariateFunction f = new Sin();
        final ConvergenceChecker<UnivariatePointValuePair> checker = new SimpleUnivariateValueChecker(1e-5, 1e-14);
        
        
        
        final UnivariateOptimizer optimizer = new BrentOptimizer(1e-10, 1e-14, checker);
        final UnivariatePointValuePair result = optimizer.optimize(new MaxEval(200),
                                                                   new UnivariateObjectiveFunction(f),
                                                                   GoalType.MINIMIZE,
                                                                   new SearchInterval(4, 5));
        Assert.assertEquals(3 * Math.PI / 2, result.getPoint(), 1e-3);
    }

// org.apache.commons.math3.optim.univariate.BrentOptimizerTest::testBoundaries
    public void testBoundaries() {
        final double lower = -1.0;
        final double upper = +1.0;
        UnivariateFunction f = new UnivariateFunction() {            
            public double value(double x) {
                if (x < lower) {
                    throw new NumberIsTooSmallException(x, lower, true);
                } else if (x > upper) {
                    throw new NumberIsTooLargeException(x, upper, true);
                } else {
                    return x;
                }
            }
        };
        UnivariateOptimizer optimizer = new BrentOptimizer(1e-10, 1e-14);
        Assert.assertEquals(lower,
                            optimizer.optimize(new MaxEval(100),
                                               new UnivariateObjectiveFunction(f),
                                               GoalType.MINIMIZE,
                                               new SearchInterval(lower, upper)).getPoint(),
                            1.0e-8);
        Assert.assertEquals(upper,
                            optimizer.optimize(new MaxEval(100),
                                               new UnivariateObjectiveFunction(f),
                                               GoalType.MAXIMIZE,
                                               new SearchInterval(lower, upper)).getPoint(),
                            1.0e-8);
    }

// org.apache.commons.math3.optim.univariate.BrentOptimizerTest::testQuinticMin
    public void testQuinticMin() {
        
        UnivariateFunction f = new QuinticFunction();
        UnivariateOptimizer optimizer = new BrentOptimizer(1e-10, 1e-14);
        Assert.assertEquals(-0.27195613, optimizer.optimize(new MaxEval(200),
                                                            new UnivariateObjectiveFunction(f),
                                                            GoalType.MINIMIZE,
                                                            new SearchInterval(-0.3, -0.2)).getPoint(), 1.0e-8);
        Assert.assertEquals( 0.82221643, optimizer.optimize(new MaxEval(200),
                                                            new UnivariateObjectiveFunction(f),
                                                            GoalType.MINIMIZE,
                                                            new SearchInterval(0.3,  0.9)).getPoint(), 1.0e-8);
        Assert.assertTrue(optimizer.getEvaluations() <= 50);

        
        Assert.assertEquals(-0.27195613, optimizer.optimize(new MaxEval(200),
                                                            new UnivariateObjectiveFunction(f),
                                                            GoalType.MINIMIZE,
                                                            new SearchInterval(-1.0, 0.2)).getPoint(), 1.0e-8);
        Assert.assertTrue(optimizer.getEvaluations() <= 50);
    }

// org.apache.commons.math3.optim.univariate.BrentOptimizerTest::testQuinticMinStatistics
    public void testQuinticMinStatistics() {
        
        UnivariateFunction f = new QuinticFunction();
        UnivariateOptimizer optimizer = new BrentOptimizer(1e-11, 1e-14);

        final DescriptiveStatistics[] stat = new DescriptiveStatistics[2];
        for (int i = 0; i < stat.length; i++) {
            stat[i] = new DescriptiveStatistics();
        }

        final double min = -0.75;
        final double max = 0.25;
        final int nSamples = 200;
        final double delta = (max - min) / nSamples;
        for (int i = 0; i < nSamples; i++) {
            final double start = min + i * delta;
            stat[0].addValue(optimizer.optimize(new MaxEval(40),
                                                new UnivariateObjectiveFunction(f),
                                                GoalType.MINIMIZE,
                                                new SearchInterval(min, max, start)).getPoint());
            stat[1].addValue(optimizer.getEvaluations());
        }

        final double meanOptValue = stat[0].getMean();
        final double medianEval = stat[1].getPercentile(50);
        Assert.assertTrue(meanOptValue > -0.2719561281);
        Assert.assertTrue(meanOptValue < -0.2719561280);
        Assert.assertEquals(23, (int) medianEval);
    }

// org.apache.commons.math3.optim.univariate.BrentOptimizerTest::testQuinticMax
    public void testQuinticMax() {
        
        
        UnivariateFunction f = new QuinticFunction();
        UnivariateOptimizer optimizer = new BrentOptimizer(1e-12, 1e-14);
        Assert.assertEquals(0.27195613, optimizer.optimize(new MaxEval(100),
                                                           new UnivariateObjectiveFunction(f),
                                                           GoalType.MAXIMIZE,
                                                           new SearchInterval(0.2, 0.3)).getPoint(), 1e-8);
        try {
            optimizer.optimize(new MaxEval(5),
                               new UnivariateObjectiveFunction(f),
                               GoalType.MAXIMIZE,
                               new SearchInterval(0.2, 0.3));
            Assert.fail("an exception should have been thrown");
        } catch (TooManyEvaluationsException miee) {
            
        }
    }

// org.apache.commons.math3.optim.univariate.BrentOptimizerTest::testMinEndpoints
    public void testMinEndpoints() {
        UnivariateFunction f = new Sin();
        UnivariateOptimizer optimizer = new BrentOptimizer(1e-8, 1e-14);

        
        double result = optimizer.optimize(new MaxEval(50),
                                           new UnivariateObjectiveFunction(f),
                                           GoalType.MINIMIZE,
                                           new SearchInterval(3 * Math.PI / 2, 5)).getPoint();
        Assert.assertEquals(3 * Math.PI / 2, result, 1e-6);

        result = optimizer.optimize(new MaxEval(50),
                                    new UnivariateObjectiveFunction(f),
                                    GoalType.MINIMIZE,
                                    new SearchInterval(4, 3 * Math.PI / 2)).getPoint();
        Assert.assertEquals(3 * Math.PI / 2, result, 1e-6);
    }

// org.apache.commons.math3.optim.univariate.BrentOptimizerTest::testMath832
    public void testMath832() {
        final UnivariateFunction f = new UnivariateFunction() {
                public double value(double x) {
                    final double sqrtX = FastMath.sqrt(x);
                    final double a = 1e2 * sqrtX;
                    final double b = 1e6 / x;
                    final double c = 1e4 / sqrtX;

                    return a + b + c;
                }
            };

        UnivariateOptimizer optimizer = new BrentOptimizer(1e-10, 1e-8);
        final double result = optimizer.optimize(new MaxEval(1483),
                                                 new UnivariateObjectiveFunction(f),
                                                 GoalType.MINIMIZE,
                                                 new SearchInterval(Double.MIN_VALUE,
                                                                    Double.MAX_VALUE)).getPoint();

        Assert.assertEquals(804.9355825, result, 1e-6);
    }

// org.apache.commons.math3.optim.univariate.BrentOptimizerTest::testKeepInitIfBest
    public void testKeepInitIfBest() {
        final double minSin = 3 * Math.PI / 2;
        final double offset = 1e-8;
        final double delta = 1e-7;
        final UnivariateFunction f1 = new Sin();
        final UnivariateFunction f2 = new StepFunction(new double[] { minSin, minSin + offset, minSin + 2 * offset},
                                                       new double[] { 0, -1, 0 });
        final UnivariateFunction f = FunctionUtils.add(f1, f2);
        
        
        final double relTol = 1e-8;
        final UnivariateOptimizer optimizer = new BrentOptimizer(relTol, 1e-100);
        final double init = minSin + 1.5 * offset;
        final UnivariatePointValuePair result
            = optimizer.optimize(new MaxEval(200),
                                 new UnivariateObjectiveFunction(f),
                                 GoalType.MINIMIZE,
                                 new SearchInterval(minSin - 6.789 * delta,
                                                    minSin + 9.876 * delta,
                                                    init));
        final int numEval = optimizer.getEvaluations();

        final double sol = result.getPoint();
        final double expected = init;

        Assert.assertTrue("Best point not reported", f.value(sol) <= f.value(expected));
    }

// org.apache.commons.math3.optim.univariate.BrentOptimizerTest::testMath855
    public void testMath855() {
        final double minSin = 3 * Math.PI / 2;
        final double offset = 1e-8;
        final double delta = 1e-7;
        final UnivariateFunction f1 = new Sin();
        final UnivariateFunction f2 = new StepFunction(new double[] { minSin, minSin + offset, minSin + 5 * offset },
                                                       new double[] { 0, -1, 0 });
        final UnivariateFunction f = FunctionUtils.add(f1, f2);
        final UnivariateOptimizer optimizer = new BrentOptimizer(1e-8, 1e-100);
        final UnivariatePointValuePair result
            = optimizer.optimize(new MaxEval(200),
                                 new UnivariateObjectiveFunction(f),
                                 GoalType.MINIMIZE,
                                 new SearchInterval(minSin - 6.789 * delta,
                                                    minSin + 9.876 * delta));
        final int numEval = optimizer.getEvaluations();

        final double sol = result.getPoint();
        final double expected = 4.712389027602411;

        
        
        

        Assert.assertTrue("Best point not reported", f.value(sol) <= f.value(expected));
    }

// org.apache.commons.math3.optimization.direct.CMAESOptimizerTest::testInitOutofbounds1
    public void testInitOutofbounds1() {
        double[] startPoint = point(DIM,3);
        double[] insigma = point(DIM, 0.3);
        double[][] boundaries = boundaries(DIM,-1,2);
        PointValuePair expected =
            new PointValuePair(point(DIM,1.0),0.0);
        doTest(new Rosen(), startPoint, insigma, boundaries,
                GoalType.MINIMIZE, LAMBDA, true, 0, 1e-13,
                1e-13, 1e-6, 100000, expected);
    }

// org.apache.commons.math3.optimization.direct.CMAESOptimizerTest::testInitOutofbounds2
    public void testInitOutofbounds2() {
        double[] startPoint = point(DIM, -2);
        double[] insigma = point(DIM, 0.3);
        double[][] boundaries = boundaries(DIM,-1,2);
        PointValuePair expected =
            new PointValuePair(point(DIM,1.0),0.0);
        doTest(new Rosen(), startPoint, insigma, boundaries,
                GoalType.MINIMIZE, LAMBDA, true, 0, 1e-13,
                1e-13, 1e-6, 100000, expected);
    }

// org.apache.commons.math3.optimization.direct.CMAESOptimizerTest::testBoundariesDimensionMismatch
    public void testBoundariesDimensionMismatch() {
        double[] startPoint = point(DIM,0.5);
        double[] insigma = point(DIM, 0.3);
        double[][] boundaries = boundaries(DIM+1,-1,2);
        PointValuePair expected =
            new PointValuePair(point(DIM,1.0),0.0);
        doTest(new Rosen(), startPoint, insigma, boundaries,
                GoalType.MINIMIZE, LAMBDA, true, 0, 1e-13,
                1e-13, 1e-6, 100000, expected);
    }

// org.apache.commons.math3.optimization.direct.CMAESOptimizerTest::testInputSigmaNegative
    public void testInputSigmaNegative() {
        double[] startPoint = point(DIM,0.5);
        double[] insigma = point(DIM,-0.5);
        double[][] boundaries = null;
        PointValuePair expected =
            new PointValuePair(point(DIM,1.0),0.0);
        doTest(new Rosen(), startPoint, insigma, boundaries,
                GoalType.MINIMIZE, LAMBDA, true, 0, 1e-13,
                1e-13, 1e-6, 100000, expected);
    }

// org.apache.commons.math3.optimization.direct.CMAESOptimizerTest::testInputSigmaOutOfRange
    public void testInputSigmaOutOfRange() {
        double[] startPoint = point(DIM,0.5);
        double[] insigma = point(DIM, 1.1);
        double[][] boundaries = boundaries(DIM,-0.5,0.5);
        PointValuePair expected =
            new PointValuePair(point(DIM,1.0),0.0);
        doTest(new Rosen(), startPoint, insigma, boundaries,
                GoalType.MINIMIZE, LAMBDA, true, 0, 1e-13,
                1e-13, 1e-6, 100000, expected);
    }

// org.apache.commons.math3.optimization.direct.CMAESOptimizerTest::testInputSigmaDimensionMismatch
    public void testInputSigmaDimensionMismatch() {
        double[] startPoint = point(DIM,0.5);
        double[] insigma = point(DIM + 1, 0.5);
        double[][] boundaries = null;
        PointValuePair expected =
            new PointValuePair(point(DIM,1.0),0.0);
        doTest(new Rosen(), startPoint, insigma, boundaries,
                GoalType.MINIMIZE, LAMBDA, true, 0, 1e-13,
                1e-13, 1e-6, 100000, expected);
    }

// org.apache.commons.math3.optimization.direct.CMAESOptimizerTest::testRosen
    public void testRosen() {
        double[] startPoint = point(DIM,0.1);
        double[] insigma = point(DIM,0.1);
        double[][] boundaries = null;
        PointValuePair expected =
            new PointValuePair(point(DIM,1.0),0.0);
        doTest(new Rosen(), startPoint, insigma, boundaries,
                GoalType.MINIMIZE, LAMBDA, true, 0, 1e-13,
                1e-13, 1e-6, 100000, expected);
        doTest(new Rosen(), startPoint, insigma, boundaries,
                GoalType.MINIMIZE, LAMBDA, false, 0, 1e-13,
                1e-13, 1e-6, 100000, expected);
    }

// org.apache.commons.math3.optimization.direct.CMAESOptimizerTest::testMaximize
    public void testMaximize() {}

// org.apache.commons.math3.optimization.direct.CMAESOptimizerTest::testEllipse
    public void testEllipse() {
        double[] startPoint = point(DIM,1.0);
        double[] insigma = point(DIM,0.1);
        double[][] boundaries = null;
        PointValuePair expected =
            new PointValuePair(point(DIM,0.0),0.0);
        doTest(new Elli(), startPoint, insigma, boundaries,
                GoalType.MINIMIZE, LAMBDA, true, 0, 1e-13,
                1e-13, 1e-6, 100000, expected);
        doTest(new Elli(), startPoint, insigma, boundaries,
                GoalType.MINIMIZE, LAMBDA, false, 0, 1e-13,
                1e-13, 1e-6, 100000, expected);
    }

// org.apache.commons.math3.optimization.direct.CMAESOptimizerTest::testElliRotated
    public void testElliRotated() {
        double[] startPoint = point(DIM,1.0);
        double[] insigma = point(DIM,0.1);
        double[][] boundaries = null;
        PointValuePair expected =
            new PointValuePair(point(DIM,0.0),0.0);
        doTest(new ElliRotated(), startPoint, insigma, boundaries,
                GoalType.MINIMIZE, LAMBDA, true, 0, 1e-13,
                1e-13, 1e-6, 100000, expected);
        doTest(new ElliRotated(), startPoint, insigma, boundaries,
                GoalType.MINIMIZE, LAMBDA, false, 0, 1e-13,
                1e-13, 1e-6, 100000, expected);
    }

// org.apache.commons.math3.optimization.direct.CMAESOptimizerTest::testCigar
    public void testCigar() {
        double[] startPoint = point(DIM,1.0);
        double[] insigma = point(DIM,0.1);
        double[][] boundaries = null;
        PointValuePair expected =
            new PointValuePair(point(DIM,0.0),0.0);
        doTest(new Cigar(), startPoint, insigma, boundaries,
                GoalType.MINIMIZE, LAMBDA, true, 0, 1e-13,
                1e-13, 1e-6, 200000, expected);
        doTest(new Cigar(), startPoint, insigma, boundaries,
                GoalType.MINIMIZE, LAMBDA, false, 0, 1e-13,
                1e-13, 1e-6, 100000, expected);
    }

// org.apache.commons.math3.optimization.direct.CMAESOptimizerTest::testCigarWithBoundaries
    public void testCigarWithBoundaries() {
        double[] startPoint = point(DIM,1.0);
        double[] insigma = point(DIM,0.1);
        double[][] boundaries = boundaries(DIM, -1e100, Double.POSITIVE_INFINITY);
        PointValuePair expected =
            new PointValuePair(point(DIM,0.0),0.0);
        doTest(new Cigar(), startPoint, insigma, boundaries,
                GoalType.MINIMIZE, LAMBDA, true, 0, 1e-13,
                1e-13, 1e-6, 200000, expected);
        doTest(new Cigar(), startPoint, insigma, boundaries,
                GoalType.MINIMIZE, LAMBDA, false, 0, 1e-13,
                1e-13, 1e-6, 100000, expected);
    }

// org.apache.commons.math3.optimization.direct.CMAESOptimizerTest::testTwoAxes
    public void testTwoAxes() {
        double[] startPoint = point(DIM,1.0);
        double[] insigma = point(DIM,0.1);
        double[][] boundaries = null;
        PointValuePair expected =
            new PointValuePair(point(DIM,0.0),0.0);
        doTest(new TwoAxes(), startPoint, insigma, boundaries,
                GoalType.MINIMIZE, 2*LAMBDA, true, 0, 1e-13,
                1e-13, 1e-6, 200000, expected);
        doTest(new TwoAxes(), startPoint, insigma, boundaries,
                GoalType.MINIMIZE, 2*LAMBDA, false, 0, 1e-13,
                1e-8, 1e-3, 200000, expected);
    }

// org.apache.commons.math3.optimization.direct.CMAESOptimizerTest::testCigTab
    public void testCigTab() {}

// org.apache.commons.math3.optimization.direct.CMAESOptimizerTest::testSphere
    public void testSphere() {
        double[] startPoint = point(DIM,1.0);
        double[] insigma = point(DIM,0.1);
        double[][] boundaries = null;
        PointValuePair expected =
            new PointValuePair(point(DIM,0.0),0.0);
        doTest(new Sphere(), startPoint, insigma, boundaries,
                GoalType.MINIMIZE, LAMBDA, true, 0, 1e-13,
                1e-13, 1e-6, 100000, expected);
        doTest(new Sphere(), startPoint, insigma, boundaries,
                GoalType.MINIMIZE, LAMBDA, false, 0, 1e-13,
                1e-13, 1e-6, 100000, expected);
    }

// org.apache.commons.math3.optimization.direct.CMAESOptimizerTest::testTablet
    public void testTablet() {
        double[] startPoint = point(DIM,1.0);
        double[] insigma = point(DIM,0.1);
        double[][] boundaries = null;
        PointValuePair expected =
            new PointValuePair(point(DIM,0.0),0.0);
        doTest(new Tablet(), startPoint, insigma, boundaries,
                GoalType.MINIMIZE, LAMBDA, true, 0, 1e-13,
                1e-13, 1e-6, 100000, expected);
        doTest(new Tablet(), startPoint, insigma, boundaries,
                GoalType.MINIMIZE, LAMBDA, false, 0, 1e-13,
                1e-13, 1e-6, 100000, expected);
    }

// org.apache.commons.math3.optimization.direct.CMAESOptimizerTest::testDiffPow
    public void testDiffPow() {
        double[] startPoint = point(DIM,1.0);
        double[] insigma = point(DIM,0.1);
        double[][] boundaries = null;
        PointValuePair expected =
            new PointValuePair(point(DIM,0.0),0.0);
        doTest(new DiffPow(), startPoint, insigma, boundaries,
                GoalType.MINIMIZE, 10, true, 0, 1e-13,
                1e-8, 1e-1, 100000, expected);
        doTest(new DiffPow(), startPoint, insigma, boundaries,
                GoalType.MINIMIZE, 10, false, 0, 1e-13,
                1e-8, 2e-1, 100000, expected);
    }

// org.apache.commons.math3.optimization.direct.CMAESOptimizerTest::testSsDiffPow
    public void testSsDiffPow() {
        double[] startPoint = point(DIM,1.0);
        double[] insigma = point(DIM,0.1);
        double[][] boundaries = null;
        PointValuePair expected =
            new PointValuePair(point(DIM,0.0),0.0);
        doTest(new SsDiffPow(), startPoint, insigma, boundaries,
                GoalType.MINIMIZE, 10, true, 0, 1e-13,
                1e-4, 1e-1, 200000, expected);
        doTest(new SsDiffPow(), startPoint, insigma, boundaries,
                GoalType.MINIMIZE, 10, false, 0, 1e-13,
                1e-4, 1e-1, 200000, expected);
    }

// org.apache.commons.math3.optimization.direct.CMAESOptimizerTest::testAckley
    public void testAckley() {
        double[] startPoint = point(DIM,1.0);
        double[] insigma = point(DIM,1.0);
        double[][] boundaries = null;
        PointValuePair expected =
            new PointValuePair(point(DIM,0.0),0.0);
        doTest(new Ackley(), startPoint, insigma, boundaries,
                GoalType.MINIMIZE, 2*LAMBDA, true, 0, 1e-13,
                1e-9, 1e-5, 100000, expected);
        doTest(new Ackley(), startPoint, insigma, boundaries,
                GoalType.MINIMIZE, 2*LAMBDA, false, 0, 1e-13,
                1e-9, 1e-5, 100000, expected);
    }

// org.apache.commons.math3.optimization.direct.CMAESOptimizerTest::testRastrigin
    public void testRastrigin() {
        double[] startPoint = point(DIM,0.1);
        double[] insigma = point(DIM,0.1);
        double[][] boundaries = null;
        PointValuePair expected =
            new PointValuePair(point(DIM,0.0),0.0);
        doTest(new Rastrigin(), startPoint, insigma, boundaries,
                GoalType.MINIMIZE, (int)(200*Math.sqrt(DIM)), true, 0, 1e-13,
                1e-13, 1e-6, 200000, expected);
        doTest(new Rastrigin(), startPoint, insigma, boundaries,
                GoalType.MINIMIZE, (int)(200*Math.sqrt(DIM)), false, 0, 1e-13,
                1e-13, 1e-6, 200000, expected);
    }

// org.apache.commons.math3.optimization.direct.CMAESOptimizerTest::testConstrainedRosen
    public void testConstrainedRosen() {
        double[] startPoint = point(DIM, 0.1);
        double[] insigma = point(DIM, 0.1);
        double[][] boundaries = boundaries(DIM, -1, 2);
        PointValuePair expected =
            new PointValuePair(point(DIM,1.0),0.0);
        doTest(new Rosen(), startPoint, insigma, boundaries,
                GoalType.MINIMIZE, 2*LAMBDA, true, 0, 1e-13,
                1e-13, 1e-6, 100000, expected);
        doTest(new Rosen(), startPoint, insigma, boundaries,
                GoalType.MINIMIZE, 2*LAMBDA, false, 0, 1e-13,
                1e-13, 1e-6, 100000, expected);
    }

// org.apache.commons.math3.optimization.direct.CMAESOptimizerTest::testDiagonalRosen
    public void testDiagonalRosen() {}

// org.apache.commons.math3.optimization.direct.CMAESOptimizerTest::testMath864
    public void testMath864() {
        final CMAESOptimizer optimizer = new CMAESOptimizer();
        final MultivariateFunction fitnessFunction = new MultivariateFunction() {
                public double value(double[] parameters) {
                    final double target = 1;
                    final double error = target - parameters[0];
                    return error * error;
                }
            };

        final double[] start = { 0 };
        final double[] lower = { -1e6 };
        final double[] upper = { 1.5 };
        final double[] result = optimizer.optimize(10000, fitnessFunction, GoalType.MINIMIZE,
                                                   start, lower, upper).getPoint();
        Assert.assertTrue("Out of bounds (" + result[0] + " > " + upper[0] + ")",
                          result[0] <= upper[0]);
    }

// org.apache.commons.math3.optimization.direct.CMAESOptimizerTest::testFitAccuracyDependsOnBoundary
    public void testFitAccuracyDependsOnBoundary() {
        final CMAESOptimizer optimizer = new CMAESOptimizer();
        final MultivariateFunction fitnessFunction = new MultivariateFunction() {
                public double value(double[] parameters) {
                    final double target = 11.1;
                    final double error = target - parameters[0];
                    return error * error;
                }
            };

        final double[] start = { 1 };
 
        
        PointValuePair result = optimizer.optimize(100000, fitnessFunction, GoalType.MINIMIZE,
                                                   start);
        final double resNoBound = result.getPoint()[0];

        
        final double[] lower = { -20 };
        final double[] upper = { 5e16 };
        result = optimizer.optimize(100000, fitnessFunction, GoalType.MINIMIZE,
                                    start, lower, upper);
        final double resNearLo = result.getPoint()[0];

        
        lower[0] = -5e16;
        upper[0] = 20;
        result = optimizer.optimize(100000, fitnessFunction, GoalType.MINIMIZE,
                                    start, lower, upper);
        final double resNearHi = result.getPoint()[0];

        
        
        

        
        
        Assert.assertEquals(resNoBound, resNearLo, 1e-3);
        Assert.assertEquals(resNoBound, resNearHi, 1e-3);
    }

// org.apache.commons.math3.optimization.direct.PowellOptimizerTest::testSumSinc
    public void testSumSinc() {
        final MultivariateFunction func = new SumSincFunction(-1);

        int dim = 2;
        final double[] minPoint = new double[dim];
        for (int i = 0; i < dim; i++) {
            minPoint[i] = 0;
        }

        double[] init = new double[dim];

        
        for (int i = 0; i < dim; i++) {
            init[i] = minPoint[i];
        }
        doTest(func, minPoint, init, GoalType.MINIMIZE, 1e-9, 1e-9);

        
        for (int i = 0; i < dim; i++) {
            init[i] = minPoint[i] + 3;
        }
        doTest(func, minPoint, init, GoalType.MINIMIZE, 1e-9, 1e-5);
        
        
        doTest(func, minPoint, init, GoalType.MINIMIZE, 1e-9, 1e-9, 1e-7);
    }

// org.apache.commons.math3.optimization.direct.PowellOptimizerTest::testQuadratic
    public void testQuadratic() {
        final MultivariateFunction func = new MultivariateFunction() {
                public double value(double[] x) {
                    final double a = x[0] - 1;
                    final double b = x[1] - 1;
                    return a * a + b * b + 1;
                }
            };

        int dim = 2;
        final double[] minPoint = new double[dim];
        for (int i = 0; i < dim; i++) {
            minPoint[i] = 1;
        }

        double[] init = new double[dim];

        
        for (int i = 0; i < dim; i++) {
            init[i] = minPoint[i];
        }
        doTest(func, minPoint, init, GoalType.MINIMIZE, 1e-9, 1e-8);

        
        for (int i = 0; i < dim; i++) {
            init[i] = minPoint[i] - 20;
        }
        doTest(func, minPoint, init, GoalType.MINIMIZE, 1e-9, 1e-8);
    }

// org.apache.commons.math3.optimization.direct.PowellOptimizerTest::testMaximizeQuadratic
    public void testMaximizeQuadratic() {
        final MultivariateFunction func = new MultivariateFunction() {
                public double value(double[] x) {
                    final double a = x[0] - 1;
                    final double b = x[1] - 1;
                    return -a * a - b * b + 1;
                }
            };

        int dim = 2;
        final double[] maxPoint = new double[dim];
        for (int i = 0; i < dim; i++) {
            maxPoint[i] = 1;
        }

        double[] init = new double[dim];

        
        for (int i = 0; i < dim; i++) {
            init[i] = maxPoint[i];
        }
        doTest(func, maxPoint, init,  GoalType.MAXIMIZE, 1e-9, 1e-8);

        
        for (int i = 0; i < dim; i++) {
            init[i] = maxPoint[i] - 20;
        }
        doTest(func, maxPoint, init, GoalType.MAXIMIZE, 1e-9, 1e-8);
    }

// org.apache.commons.math3.optimization.direct.PowellOptimizerTest::testRelativeToleranceOnScaledValues
    public void testRelativeToleranceOnScaledValues() {
        final MultivariateFunction func = new MultivariateFunction() {
                public double value(double[] x) {
                    final double a = x[0] - 1;
                    final double b = x[1] - 1;
                    return a * a * FastMath.sqrt(FastMath.abs(a)) + b * b + 1;
                }
            };

        int dim = 2;
        final double[] minPoint = new double[dim];
        for (int i = 0; i < dim; i++) {
            minPoint[i] = 1;
        }

        double[] init = new double[dim];
        
        for (int i = 0; i < dim; i++) {
            init[i] = minPoint[i] - 20;
        }

        final double relTol = 1e-10;

        final int maxEval = 1000;
        
        
        final MultivariateOptimizer optim = new PowellOptimizer(relTol, 1e-100);

        final PointValuePair funcResult = optim.optimize(maxEval, func, GoalType.MINIMIZE, init);
        final double funcValue = func.value(funcResult.getPoint());
        final int funcEvaluations = optim.getEvaluations();

        final double scale = 1e10;
        final MultivariateFunction funcScaled = new MultivariateFunction() {
                public double value(double[] x) {
                    return scale * func.value(x);
                }
            };

        final PointValuePair funcScaledResult = optim.optimize(maxEval, funcScaled, GoalType.MINIMIZE, init);
        final double funcScaledValue = funcScaled.value(funcScaledResult.getPoint());
        final int funcScaledEvaluations = optim.getEvaluations();

        
        
        Assert.assertEquals(1, funcScaledValue / (scale * funcValue), relTol);

        
        Assert.assertEquals(funcEvaluations, funcScaledEvaluations);
    }

// org.apache.commons.math3.optimization.general.GaussNewtonOptimizerTest::testMoreEstimatedParametersSimple
    public void testMoreEstimatedParametersSimple() {
        
        super.testMoreEstimatedParametersSimple();
    }

// org.apache.commons.math3.optimization.general.GaussNewtonOptimizerTest::testMoreEstimatedParametersUnsorted
    public void testMoreEstimatedParametersUnsorted() {
        
        super.testMoreEstimatedParametersUnsorted();
    }

// org.apache.commons.math3.optimization.general.GaussNewtonOptimizerTest::testMaxEvaluations
    public void testMaxEvaluations() throws Exception {
        CircleVectorial circle = new CircleVectorial();
        circle.addPoint( 30.0,  68.0);
        circle.addPoint( 50.0,  -6.0);
        circle.addPoint(110.0, -20.0);
        circle.addPoint( 35.0,  15.0);
        circle.addPoint( 45.0,  97.0);

        GaussNewtonOptimizer optimizer
            = new GaussNewtonOptimizer(new SimpleVectorValueChecker(1.0e-30, 1.0e-30));

        optimizer.optimize(100, circle, new double[] { 0, 0, 0, 0, 0 },
                           new double[] { 1, 1, 1, 1, 1 },
                           new double[] { 98.680, 47.345 });
    }

// org.apache.commons.math3.optimization.general.GaussNewtonOptimizerTest::testCircleFittingBadInit
    public void testCircleFittingBadInit() {
        
        super.testCircleFittingBadInit();
    }

// org.apache.commons.math3.optimization.general.GaussNewtonOptimizerTest::testHahn1
    public void testHahn1()
        throws IOException {
        
        super.testHahn1();
    }

// org.apache.commons.math3.optimization.general.LevenbergMarquardtOptimizerTest::testNonInvertible
    public void testNonInvertible() {
        
        LinearProblem problem = new LinearProblem(new double[][] {
                {  1, 2, -3 },
                {  2, 1,  3 },
                { -3, 0, -9 }
        }, new double[] { 1, 1, 1 });

        AbstractLeastSquaresOptimizer optimizer = createOptimizer();
        PointVectorValuePair optimum = optimizer.optimize(100, problem, problem.target, new double[] { 1, 1, 1 }, new double[] { 0, 0, 0 });
        Assert.assertTrue(FastMath.sqrt(problem.target.length) * optimizer.getRMS() > 0.6);

        optimizer.computeCovariances(optimum.getPoint(), 1.5e-14);
    }

// org.apache.commons.math3.optimization.general.LevenbergMarquardtOptimizerTest::testControlParameters
    public void testControlParameters() {
        CircleVectorial circle = new CircleVectorial();
        circle.addPoint( 30.0,  68.0);
        circle.addPoint( 50.0,  -6.0);
        circle.addPoint(110.0, -20.0);
        circle.addPoint( 35.0,  15.0);
        circle.addPoint( 45.0,  97.0);
        checkEstimate(circle, 0.1, 10, 1.0e-14, 1.0e-16, 1.0e-10, false);
        checkEstimate(circle, 0.1, 10, 1.0e-15, 1.0e-17, 1.0e-10, true);
        checkEstimate(circle, 0.1,  5, 1.0e-15, 1.0e-16, 1.0e-10, true);
        circle.addPoint(300, -300);
        checkEstimate(circle, 0.1, 20, 1.0e-18, 1.0e-16, 1.0e-10, true);
    }

// org.apache.commons.math3.optimization.general.LevenbergMarquardtOptimizerTest::testMath199
    public void testMath199() {
        try {
            QuadraticProblem problem = new QuadraticProblem();
            problem.addPoint (0, -3.182591015485607);
            problem.addPoint (1, -2.5581184967730577);
            problem.addPoint (2, -2.1488478161387325);
            problem.addPoint (3, -1.9122489313410047);
            problem.addPoint (4, 1.7785661310051026);
            LevenbergMarquardtOptimizer optimizer
                = new LevenbergMarquardtOptimizer(100, 1e-10, 1e-10, 1e-10, 0);
            optimizer.optimize(100, problem,
                               new double[] { 0, 0, 0, 0, 0 },
                               new double[] { 0.0, 4.4e-323, 1.0, 4.4e-323, 0.0 },
                               new double[] { 0, 0, 0 });
            Assert.fail("an exception should have been thrown");
        } catch (ConvergenceException ee) {
            
        }
    }

// org.apache.commons.math3.optimization.general.LevenbergMarquardtOptimizerTest::testBevington
    public void testBevington() {
        final double[][] dataPoints = {
            
            { 15, 30, 45, 60, 75, 90, 105, 120, 135, 150,
              165, 180, 195, 210, 225, 240, 255, 270, 285, 300,
              315, 330, 345, 360, 375, 390, 405, 420, 435, 450,
              465, 480, 495, 510, 525, 540, 555, 570, 585, 600,
              615, 630, 645, 660, 675, 690, 705, 720, 735, 750,
              765, 780, 795, 810, 825, 840, 855, 870, 885, },
            
            { 775, 479, 380, 302, 185, 157, 137, 119, 110, 89,
              74, 61, 66, 68, 48, 54, 51, 46, 55, 29,
              28, 37, 49, 26, 35, 29, 31, 24, 25, 35,
              24, 30, 26, 28, 21, 18, 20, 27, 17, 17,
              14, 17, 24, 11, 22, 17, 12, 10, 13, 16,
              9, 9, 14, 21, 17, 13, 12, 18, 10, },
        };

        final BevingtonProblem problem = new BevingtonProblem();

        final int len = dataPoints[0].length;
        final double[] weights = new double[len];
        for (int i = 0; i < len; i++) {
            problem.addPoint(dataPoints[0][i],
                             dataPoints[1][i]);

            weights[i] = 1 / dataPoints[1][i];
        }

        final LevenbergMarquardtOptimizer optimizer
            = new LevenbergMarquardtOptimizer();

        final PointVectorValuePair optimum
            = optimizer.optimize(100, problem, dataPoints[1], weights,
                               new double[] { 10, 900, 80, 27, 225 });

        final double[] solution = optimum.getPoint();
        final double[] expectedSolution = { 10.4, 958.3, 131.4, 33.9, 205.0 };

        final double[][] covarMatrix = optimizer.computeCovariances(solution, 1e-14);
        final double[][] expectedCovarMatrix = {
            { 3.38, -3.69, 27.98, -2.34, -49.24 },
            { -3.69, 2492.26, 81.89, -69.21, -8.9 },
            { 27.98, 81.89, 468.99, -44.22, -615.44 },
            { -2.34, -69.21, -44.22, 6.39, 53.80 },
            { -49.24, -8.9, -615.44, 53.8, 929.45 }
        };

        final int numParams = expectedSolution.length;

        
        for (int i = 0; i < numParams; i++) {
            final double error = FastMath.sqrt(expectedCovarMatrix[i][i]);
            Assert.assertEquals("Parameter " + i, expectedSolution[i], solution[i], error);
        }

        
        
        for (int i = 0; i < numParams; i++) {
            for (int j = 0; j < numParams; j++) {
                Assert.assertEquals("Covariance matrix [" + i + "][" + j + "]",
                                    expectedCovarMatrix[i][j],
                                    covarMatrix[i][j],
                                    FastMath.abs(0.1 * expectedCovarMatrix[i][j]));
            }
        }
    }

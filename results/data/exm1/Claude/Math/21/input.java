// buggy code
    public RectangularCholeskyDecomposition(RealMatrix matrix, double small)
        throws NonPositiveDefiniteMatrixException {

        final int order = matrix.getRowDimension();
        final double[][] c = matrix.getData();
        final double[][] b = new double[order][order];

        int[] swap  = new int[order];
        int[] index = new int[order];
        for (int i = 0; i < order; ++i) {
            index[i] = i;
        }

        int r = 0;
        for (boolean loop = true; loop;) {

            // find maximal diagonal element
            swap[r] = r;
            for (int i = r + 1; i < order; ++i) {
                int ii  = index[i];
                int isi = index[swap[i]];
                if (c[ii][ii] > c[isi][isi]) {
                    swap[r] = i;
                }
            }


            // swap elements
            if (swap[r] != r) {
                int tmp = index[r];
                index[r] = index[swap[r]];
                index[swap[r]] = tmp;
            }

            // check diagonal element
            int ir = index[r];
            if (c[ir][ir] < small) {

                if (r == 0) {
                    throw new NonPositiveDefiniteMatrixException(c[ir][ir], ir, small);
                }

                // check remaining diagonal elements
                for (int i = r; i < order; ++i) {
                    if (c[index[i]][index[i]] < -small) {
                        // there is at least one sufficiently negative diagonal element,
                        // the symmetric positive semidefinite matrix is wrong
                        throw new NonPositiveDefiniteMatrixException(c[index[i]][index[i]], i, small);
                    }
                }

                // all remaining diagonal elements are close to zero, we consider we have
                // found the rank of the symmetric positive semidefinite matrix
                ++r;
                loop = false;

            } else {

                // transform the matrix
                final double sqrt = FastMath.sqrt(c[ir][ir]);
                b[r][r] = sqrt;
                final double inverse  = 1 / sqrt;
                for (int i = r + 1; i < order; ++i) {
                    final int ii = index[i];
                    final double e = inverse * c[ii][ir];
                    b[i][r] = e;
                    c[ii][ii] -= e * e;
                    for (int j = r + 1; j < i; ++j) {
                        final int ij = index[j];
                        final double f = c[ii][ij] - e * b[j][r];
                        c[ii][ij] = f;
                        c[ij][ii] = f;
                    }
                }

                // prepare next iteration
                loop = ++r < order;
            }
        }

        // build the root matrix
        rank = r;
        root = MatrixUtils.createRealMatrix(order, r);
        for (int i = 0; i < order; ++i) {
            for (int j = 0; j < r; ++j) {
                root.setEntry(index[i], j, b[i][j]);
            }
        }

    }

// relevant test
// org.apache.commons.math3.linear.RectangularCholeskyDecompositionTest::testDecomposition3x3
    public void testDecomposition3x3() {

        RealMatrix m = MatrixUtils.createRealMatrix(new double[][] {
            { 1,   9,   9 },
            { 9, 225, 225 },
            { 9, 225, 625 }
        });

        RectangularCholeskyDecomposition d =
                new RectangularCholeskyDecomposition(m, 1.0e-6);

        
        
        
        Assert.assertEquals(0.8,  d.getRootMatrix().getEntry(0, 2), 1.0e-15);
        Assert.assertEquals(25.0, d.getRootMatrix().getEntry(2, 0), 1.0e-15);
        Assert.assertEquals(0.0,  d.getRootMatrix().getEntry(2, 2), 1.0e-15);

        RealMatrix root = d.getRootMatrix();
        RealMatrix rebuiltM = root.multiply(root.transpose());
        Assert.assertEquals(0.0, m.subtract(rebuiltM).getNorm(), 1.0e-15);

    }

// org.apache.commons.math3.linear.RectangularCholeskyDecompositionTest::testFullRank
    public void testFullRank() {

        RealMatrix base = MatrixUtils.createRealMatrix(new double[][] {
            { 0.1159548705,      0.,           0.,           0.      },
            { 0.0896442724, 0.1223540781,      0.,           0.      },
            { 0.0852155322, 4.558668e-3,  0.1083577299,      0.      },
            { 0.0905486674, 0.0213768077, 0.0128878333, 0.1014155693 }
        });

        RealMatrix m = base.multiply(base.transpose());

        RectangularCholeskyDecomposition d =
                new RectangularCholeskyDecomposition(m, 1.0e-10);

        RealMatrix root = d.getRootMatrix();
        RealMatrix rebuiltM = root.multiply(root.transpose());
        Assert.assertEquals(0.0, m.subtract(rebuiltM).getNorm(), 1.0e-15);

        
        
        Assert.assertTrue(root.subtract(base).getNorm() > 0.3);

    }

// org.apache.commons.math3.linear.RectangularCholeskyDecompositionTest::testMath789
    public void testMath789() {

        final RealMatrix m1 = MatrixUtils.createRealMatrix(new double[][]{
            {0.013445532, 0.010394690, 0.009881156, 0.010499559},
            {0.010394690, 0.023006616, 0.008196856, 0.010732709},
            {0.009881156, 0.008196856, 0.019023866, 0.009210099},
            {0.010499559, 0.010732709, 0.009210099, 0.019107243}
        });
        RealMatrix root1 = new RectangularCholeskyDecomposition(m1, 1.0e-10).getRootMatrix();
        RealMatrix rebuiltM1 = root1.multiply(root1.transpose());
        Assert.assertEquals(0.0, m1.subtract(rebuiltM1).getNorm(), 1.0e-16);

        final RealMatrix m2 = MatrixUtils.createRealMatrix(new double[][]{
            {0.0, 0.0, 0.0, 0.0, 0.0},
            {0.0, 0.013445532, 0.010394690, 0.009881156, 0.010499559},
            {0.0, 0.010394690, 0.023006616, 0.008196856, 0.010732709},
            {0.0, 0.009881156, 0.008196856, 0.019023866, 0.009210099},
            {0.0, 0.010499559, 0.010732709, 0.009210099, 0.019107243}
        });
        RealMatrix root2 = new RectangularCholeskyDecomposition(m2, 1.0e-10).getRootMatrix();
        RealMatrix rebuiltM2 = root2.multiply(root2.transpose());
        Assert.assertEquals(0.0, m2.subtract(rebuiltM2).getNorm(), 1.0e-16);

        final RealMatrix m3 = MatrixUtils.createRealMatrix(new double[][]{
            {0.013445532, 0.010394690, 0.0, 0.009881156, 0.010499559},
            {0.010394690, 0.023006616, 0.0, 0.008196856, 0.010732709},
            {0.0, 0.0, 0.0, 0.0, 0.0},
            {0.009881156, 0.008196856, 0.0, 0.019023866, 0.009210099},
            {0.010499559, 0.010732709, 0.0, 0.009210099, 0.019107243}
        });
        RealMatrix root3 = new RectangularCholeskyDecomposition(m3, 1.0e-10).getRootMatrix();
        RealMatrix rebuiltM3 = root3.multiply(root3.transpose());
        Assert.assertEquals(0.0, m3.subtract(rebuiltM3).getNorm(), 1.0e-16);

    }

// org.apache.commons.math3.stat.regression.GLSMultipleLinearRegressionTest::cannotAddXSampleData
    public void cannotAddXSampleData() {
        createRegression().newSampleData(new double[]{}, null, null);
    }

// org.apache.commons.math3.stat.regression.GLSMultipleLinearRegressionTest::cannotAddNullYSampleData
    public void cannotAddNullYSampleData() {
        createRegression().newSampleData(null, new double[][]{}, null);
    }

// org.apache.commons.math3.stat.regression.GLSMultipleLinearRegressionTest::cannotAddSampleDataWithSizeMismatch
    public void cannotAddSampleDataWithSizeMismatch() {
        double[] y = new double[]{1.0, 2.0};
        double[][] x = new double[1][];
        x[0] = new double[]{1.0, 0};
        createRegression().newSampleData(y, x, null);
    }

// org.apache.commons.math3.stat.regression.GLSMultipleLinearRegressionTest::cannotAddNullCovarianceData
    public void cannotAddNullCovarianceData() {
        createRegression().newSampleData(new double[]{}, new double[][]{}, null);
    }

// org.apache.commons.math3.stat.regression.GLSMultipleLinearRegressionTest::notEnoughData
    public void notEnoughData() {
        double[]   reducedY = new double[y.length - 1];
        double[][] reducedX = new double[x.length - 1][];
        double[][] reducedO = new double[omega.length - 1][];
        System.arraycopy(y,     0, reducedY, 0, reducedY.length);
        System.arraycopy(x,     0, reducedX, 0, reducedX.length);
        System.arraycopy(omega, 0, reducedO, 0, reducedO.length);
        createRegression().newSampleData(reducedY, reducedX, reducedO);
    }

// org.apache.commons.math3.stat.regression.GLSMultipleLinearRegressionTest::cannotAddCovarianceDataWithSampleSizeMismatch
    public void cannotAddCovarianceDataWithSampleSizeMismatch() {
        double[] y = new double[]{1.0, 2.0};
        double[][] x = new double[2][];
        x[0] = new double[]{1.0, 0};
        x[1] = new double[]{0, 1.0};
        double[][] omega = new double[1][];
        omega[0] = new double[]{1.0, 0};
        createRegression().newSampleData(y, x, omega);
    }

// org.apache.commons.math3.stat.regression.GLSMultipleLinearRegressionTest::cannotAddCovarianceDataThatIsNotSquare
    public void cannotAddCovarianceDataThatIsNotSquare() {
        double[] y = new double[]{1.0, 2.0};
        double[][] x = new double[2][];
        x[0] = new double[]{1.0, 0};
        x[1] = new double[]{0, 1.0};
        double[][] omega = new double[3][];
        omega[0] = new double[]{1.0, 0};
        omega[1] = new double[]{0, 1.0};
        omega[2] = new double[]{0, 2.0};
        createRegression().newSampleData(y, x, omega);
    }

// org.apache.commons.math3.stat.regression.GLSMultipleLinearRegressionTest::testYVariance
    public void testYVariance() {

        

        GLSMultipleLinearRegression model = new GLSMultipleLinearRegression();
        model.newSampleData(y, x, omega);
        TestUtils.assertEquals(model.calculateYVariance(), 3.5, 0);
    }

// org.apache.commons.math3.stat.regression.GLSMultipleLinearRegressionTest::testNewSample2
    public void testNewSample2() {
        double[] y = new double[] {1, 2, 3, 4}; 
        double[][] x = new double[][] {
          {19, 22, 33},
          {20, 30, 40},
          {25, 35, 45},
          {27, 37, 47}   
        };
        double[][] covariance = MatrixUtils.createRealIdentityMatrix(4).scalarMultiply(2).getData();
        GLSMultipleLinearRegression regression = new GLSMultipleLinearRegression();
        regression.newSampleData(y, x, covariance);
        RealMatrix combinedX = regression.getX().copy();
        RealVector combinedY = regression.getY().copy();
        RealMatrix combinedCovInv = regression.getOmegaInverse();
        regression.newXSampleData(x);
        regression.newYSampleData(y);
        Assert.assertEquals(combinedX, regression.getX());
        Assert.assertEquals(combinedY, regression.getY());
        Assert.assertEquals(combinedCovInv, regression.getOmegaInverse());
    }

// org.apache.commons.math3.stat.regression.GLSMultipleLinearRegressionTest::testGLSOLSConsistency
    public void testGLSOLSConsistency() {      
        RealMatrix identityCov = MatrixUtils.createRealIdentityMatrix(16);
        GLSMultipleLinearRegression glsModel = new GLSMultipleLinearRegression();
        OLSMultipleLinearRegression olsModel = new OLSMultipleLinearRegression();
        glsModel.newSampleData(longley, 16, 6);
        olsModel.newSampleData(longley, 16, 6);
        glsModel.newCovarianceData(identityCov.getData());
        double[] olsBeta = olsModel.calculateBeta().toArray();
        double[] glsBeta = glsModel.calculateBeta().toArray();
        
        
        for (int i = 0; i < olsBeta.length; i++) {
            TestUtils.assertRelativelyEquals(olsBeta[i], glsBeta[i], 10E-7);
        }
    }

// org.apache.commons.math3.stat.regression.GLSMultipleLinearRegressionTest::testGLSEfficiency
    public void testGLSEfficiency() {
        RandomGenerator rg = new JDKRandomGenerator();
        rg.setSeed(200);  
        
        
        
        final int nObs = 16;
        double[] sigma = new double[nObs];
        for (int i = 0; i < nObs; i++) {
            sigma[i] = 10 * rg.nextDouble();
        }
        
        
        
        final int numSeeds = 1000;
        RealMatrix errorSeeds = MatrixUtils.createRealMatrix(numSeeds, nObs);
        for (int i = 0; i < numSeeds; i++) {
            for (int j = 0; j < nObs; j++) {
                errorSeeds.setEntry(i, j, rg.nextGaussian() * sigma[j]);
            }
        }
        
        
        RealMatrix cov = (new Covariance(errorSeeds)).getCovarianceMatrix();
          
        
        GaussianRandomGenerator rawGenerator = new GaussianRandomGenerator(rg);
        double[] errorMeans = new double[nObs];  
        CorrelatedRandomVectorGenerator gen = new CorrelatedRandomVectorGenerator(errorMeans, cov,
         1.0e-12 * cov.getNorm(), rawGenerator);
        
        
        
        
        
        OLSMultipleLinearRegression ols = new OLSMultipleLinearRegression();
        ols.newSampleData(longley, nObs, 6);
        final RealVector b = ols.calculateBeta().copy();
        final RealMatrix x = ols.getX().copy();
        
        
        GLSMultipleLinearRegression gls = new GLSMultipleLinearRegression();
        gls.newSampleData(longley, nObs, 6);
        gls.newCovarianceData(cov.getData());
        
        
        DescriptiveStatistics olsBetaStats = new DescriptiveStatistics();
        DescriptiveStatistics glsBetaStats = new DescriptiveStatistics();
        
        
        
        final int nModels = 10000;
        for (int i = 0; i < nModels; i++) {
            
            
            RealVector u = MatrixUtils.createRealVector(gen.nextVector());
            double[] y = u.add(x.operate(b)).toArray();
            
            
            ols.newYSampleData(y);
            RealVector olsBeta = ols.calculateBeta();
            
            
            gls.newYSampleData(y);
            RealVector glsBeta = gls.calculateBeta();
            
            
            double dist = olsBeta.getDistance(b);
            olsBetaStats.addValue(dist * dist);
            dist = glsBeta.getDistance(b);
            glsBetaStats.addValue(dist * dist);
            
        }
        
        
        assert(olsBetaStats.getMean() > 1.5 * glsBetaStats.getMean());
        assert(olsBetaStats.getStandardDeviation() > glsBetaStats.getStandardDeviation());  
    }

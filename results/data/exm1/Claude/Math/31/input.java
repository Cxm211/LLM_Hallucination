// buggy code
    public double evaluate(double x, double epsilon, int maxIterations) {
        final double small = 1e-50;
        double hPrev = getA(0, x);

        // use the value of small as epsilon criteria for zero checks
        if (Precision.equals(hPrev, 0.0, small)) {
            hPrev = small;
        }

        int n = 1;
        double dPrev = 0.0;
        double p0 = 1.0;
        double q1 = 1.0;
        double cPrev = hPrev;
        double hN = hPrev;

        while (n < maxIterations) {
            final double a = getA(n, x);
            final double b = getB(n, x);

            double cN = a * hPrev + b * p0;
            double q2 = a * q1 + b * dPrev;
            if (Double.isInfinite(cN) || Double.isInfinite(q2)) {
                double scaleFactor = 1d;
                double lastScaleFactor = 1d;
                final int maxPower = 5;
                final double scale = FastMath.max(a,b);
                if (scale <= 0) {  // Can't scale
                    throw new ConvergenceException(LocalizedFormats.CONTINUED_FRACTION_INFINITY_DIVERGENCE, x);
                }
                for (int i = 0; i < maxPower; i++) {
                    lastScaleFactor = scaleFactor;
                    scaleFactor *= scale;
                    if (a != 0.0 && a > b) {
                        cN = hPrev / lastScaleFactor + (b / scaleFactor * p0);
                        q2 = q1 / lastScaleFactor + (b / scaleFactor * dPrev);
                    } else if (b != 0) {
                        cN = (a / scaleFactor * hPrev) + p0 / lastScaleFactor;
                        q2 = (a / scaleFactor * q1) + dPrev / lastScaleFactor;
                    }
                    if (!(Double.isInfinite(cN) || Double.isInfinite(q2))) {
                        break;
                    }
                }
            }

            final double deltaN = cN / q2 / cPrev;
            hN = cPrev * deltaN;

            if (Double.isInfinite(hN)) {
                throw new ConvergenceException(LocalizedFormats.CONTINUED_FRACTION_INFINITY_DIVERGENCE,
                                               x);
            }
            if (Double.isNaN(hN)) {
                throw new ConvergenceException(LocalizedFormats.CONTINUED_FRACTION_NAN_DIVERGENCE,
                                               x);
            }

            if (FastMath.abs(deltaN - 1.0) < epsilon) {
                break;
            }

            dPrev = q1;
            cPrev = cN / q2;
            p0 = hPrev;
            hPrev = cN;
            q1 = q2;
            n++;
        }

        if (n >= maxIterations) {
            throw new MaxCountExceededException(LocalizedFormats.NON_CONVERGENT_CONTINUED_FRACTION,
                                                maxIterations, x);
        }

        return hN;
    }

// relevant test
// org.apache.commons.math3.distribution.BetaDistributionTest::testCumulative
    public void testCumulative() {
        double[] x = new double[]{-0.1, 0.0, 0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9, 1.0, 1.1};
        
        checkCumulative(0.1, 0.1,
                x, new double[]{
                0.0000000000, 0.0000000000, 0.4063850939, 0.4397091902, 0.4628041861,
                0.4821200456, 0.5000000000, 0.5178799544, 0.5371958139, 0.5602908098,
                0.5936149061, 1.0000000000, 1.0000000000});
        checkCumulative(0.1, 0.5,
                x, new double[]{
                0.0000000000, 0.0000000000, 0.7048336221, 0.7593042194, 0.7951765304,
                0.8234948385, 0.8480017124, 0.8706034370, 0.8926585878, 0.9156406404,
                0.9423662883, 1.0000000000, 1.0000000000});
        checkCumulative(0.1, 1.0,
                x, new double[]{
                0.0000000000, 0.0000000000, 0.7943282347, 0.8513399225, 0.8865681506,
                0.9124435366, 0.9330329915, 0.9502002165, 0.9649610951, 0.9779327685,
                0.9895192582, 1.0000000000, 1.0000000000});
        checkCumulative(0.1, 2.0,
                x, new double[]{
                0.0000000000, 0.0000000000, 0.8658177758, 0.9194471163, 0.9486279211,
                0.9671901487, 0.9796846411, 0.9882082252, 0.9939099280, 0.9974914239,
                0.9994144508, 1.0000000000, 1.0000000000});
        checkCumulative(0.1, 4.0,
                x, new double[]{
                0.0000000000, 0.0000000000, 0.9234991121, 0.9661958941, 0.9842285085,
                0.9928444112, 0.9970040660, 0.9989112804, 0.9996895625, 0.9999440793,
                0.9999967829, 1.0000000000, 1.0000000000});
        checkCumulative(0.5, 0.1,
                x, new double[]{
                0.00000000000, 0.00000000000, 0.05763371168, 0.08435935962,
                0.10734141216, 0.12939656302, 0.15199828760, 0.17650516146,
                0.20482346963, 0.24069578055, 0.29516637795, 1.00000000000, 1.00000000000});

        checkCumulative(0.5, 0.5,
                x, new double[]{
                0.0000000000, 0.0000000000, 0.2048327647, 0.2951672353, 0.3690101196,
                0.4359057832, 0.5000000000, 0.5640942168, 0.6309898804, 0.7048327647,
                0.7951672353, 1.0000000000, 1.0000000000});
        checkCumulative(0.5, 1.0,
                x, new double[]{
                0.0000000000, 0.0000000000, 0.3162277660, 0.4472135955, 0.5477225575,
                0.6324555320, 0.7071067812, 0.7745966692, 0.8366600265, 0.8944271910,
                0.9486832981, 1.0000000000, 1.0000000000});
        checkCumulative(0.5, 2.0,
                x, new double[]{
                0.0000000000, 0.0000000000, 0.4585302607, 0.6260990337, 0.7394254526,
                0.8221921916, 0.8838834765, 0.9295160031, 0.9621590305, 0.9838699101,
                0.9961174630, 1.0000000000, 1.0000000000});
        checkCumulative(0.5, 4.0,
                x, new double[]{
                0.0000000000, 0.0000000000, 0.6266250826, 0.8049844719, 0.8987784842,
                0.9502644369, 0.9777960959, 0.9914837366, 0.9974556254, 0.9995223859,
                0.9999714889, 1.0000000000, 1.0000000000});
        checkCumulative(1.0, 0.1,
                x, new double[]{
                0.00000000000, 0.00000000000, 0.01048074179, 0.02206723146,
                0.03503890488, 0.04979978349, 0.06696700846, 0.08755646344,
                0.11343184943, 0.14866007748, 0.20567176528, 1.00000000000, 1.00000000000});
        checkCumulative(1.0, 0.5,
                x, new double[]{
                0.00000000000, 0.00000000000, 0.05131670195, 0.10557280900,
                0.16333997347, 0.22540333076, 0.29289321881, 0.36754446797,
                0.45227744249, 0.55278640450, 0.68377223398, 1.00000000000, 1.00000000000});
        checkCumulative(1, 1,
                x, new double[]{
                0.0, 0.0, 0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9, 1.0, 1.0});
        checkCumulative(1, 2,
                x, new double[]{
                0.00, 0.00, 0.19, 0.36, 0.51, 0.64, 0.75, 0.84, 0.91, 0.96, 0.99, 1.00, 1.00});
        checkCumulative(1, 4,
                x, new double[]{
                0.0000, 0.0000, 0.3439, 0.5904, 0.7599, 0.8704, 0.9375, 0.9744, 0.9919,
                0.9984, 0.9999, 1.0000, 1.0000});
        checkCumulative(2.0, 0.1,
                x, new double[]{
                0.0000000000000, 0.0000000000000, 0.0005855492117, 0.0025085760862,
                0.0060900720266, 0.0117917748341, 0.0203153588864, 0.0328098512512,
                0.0513720788952, 0.0805528836776, 0.1341822241505, 1.0000000000000, 1.0000000000000});
        checkCumulative(2, 1,
                x, new double[]{
                0.00, 0.00, 0.01, 0.04, 0.09, 0.16, 0.25, 0.36, 0.49, 0.64, 0.81, 1.00, 1.00});
        checkCumulative(2.0, 0.5,
                x, new double[]{
                0.000000000000, 0.000000000000, 0.003882537047, 0.016130089900,
                0.037840969486, 0.070483996910, 0.116116523517, 0.177807808356,
                0.260574547368, 0.373900966300, 0.541469739276, 1.000000000000, 1.000000000000});
        checkCumulative(2, 2,
                x, new double[]{
                0.000, 0.000, 0.028, 0.104, 0.216, 0.352, 0.500, 0.648, 0.784, 0.896, 0.972, 1.000, 1.000});
        checkCumulative(2, 4,
                x, new double[]{
                0.00000, 0.00000, 0.08146, 0.26272, 0.47178, 0.66304, 0.81250, 0.91296,
                0.96922, 0.99328, 0.99954, 1.00000, 1.00000});
        checkCumulative(4.0, 0.1,
                x, new double[]{
                0.000000000e+00, 0.000000000e+00, 3.217128269e-06, 5.592070271e-05,
                3.104375474e-04, 1.088719595e-03, 2.995933981e-03, 7.155588777e-03,
                1.577149153e-02, 3.380410585e-02, 7.650088789e-02, 1.000000000e+00, 1.000000000e+00});
        checkCumulative(4.0, 0.5,
                x, new double[]{
                0.000000000e+00, 0.000000000e+00, 2.851114863e-05, 4.776140576e-04,
                2.544374616e-03, 8.516263371e-03, 2.220390414e-02, 4.973556312e-02,
                1.012215158e-01, 1.950155281e-01, 3.733749174e-01, 1.000000000e+00, 1.000000000e+00});
        checkCumulative(4, 1,
                x, new double[]{
                0.0000, 0.0000, 0.0001, 0.0016, 0.0081, 0.0256, 0.0625, 0.1296, 0.2401,
                0.4096, 0.6561, 1.0000, 1.0000});
        checkCumulative(4, 2,
                x, new double[]{
                0.00000, 0.00000, 0.00046, 0.00672, 0.03078, 0.08704, 0.18750, 0.33696,
                0.52822, 0.73728, 0.91854, 1.00000, 1.00000});
        checkCumulative(4, 4,
                x, new double[]{
                0.000000, 0.000000, 0.002728, 0.033344, 0.126036, 0.289792, 0.500000,
                0.710208, 0.873964, 0.966656, 0.997272, 1.000000, 1.000000});

    }

// org.apache.commons.math3.distribution.BetaDistributionTest::testDensity
    public void testDensity() {
        double[] x = new double[]{1e-6, 0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9};
        checkDensity(0.1, 0.1,
                x, new double[]{
                12741.2357380649, 0.4429889586665234, 2.639378715e-01, 2.066393611e-01,
                1.832401831e-01, 1.766302780e-01, 1.832404579e-01, 2.066400696e-01,
                2.639396531e-01, 4.429925026e-01});
        checkDensity(0.1, 0.5,
                x, new double[]{
                2.218377102e+04, 7.394524202e-01, 4.203020268e-01, 3.119435533e-01,
                2.600787829e-01, 2.330648626e-01, 2.211408259e-01, 2.222728708e-01,
                2.414013907e-01, 3.070567405e-01});
        checkDensity(0.1, 1.0,
                x, new double[]{
                2.511886432e+04, 7.943210858e-01, 4.256680458e-01, 2.955218303e-01,
                2.281103709e-01, 1.866062624e-01, 1.583664652e-01, 1.378514078e-01,
                1.222414585e-01, 1.099464743e-01});
        checkDensity(0.1, 2.0,
                x, new double[]{
                2.763072312e+04, 7.863770012e-01, 3.745874120e-01, 2.275514842e-01,
                1.505525939e-01, 1.026332391e-01, 6.968107049e-02, 4.549081293e-02,
                2.689298641e-02, 1.209399123e-02});
        checkDensity(0.1, 4.0,
                x, new double[]{
                2.997927462e+04, 6.911058917e-01, 2.601128486e-01, 1.209774010e-01,
                5.880564714e-02, 2.783915474e-02, 1.209657335e-02, 4.442148268e-03,
                1.167143939e-03, 1.312171805e-04});
        checkDensity(0.5, 0.1,
                x, new double[]{
                88.3152184726, 0.3070542841, 0.2414007269, 0.2222727015,
                0.2211409364, 0.2330652355, 0.2600795198, 0.3119449793,
                0.4203052841, 0.7394649088});
        checkDensity(0.5, 0.5,
                x, new double[]{
                318.3100453389, 1.0610282383, 0.7957732234, 0.6946084565,
                0.6497470636, 0.6366197724, 0.6497476051, 0.6946097796,
                0.7957762075, 1.0610376697});
        checkDensity(0.5, 1.0,
                x, new double[]{
                500.0000000000, 1.5811309244, 1.1180311937, 0.9128694077,
                0.7905684268, 0.7071060741, 0.6454966865, 0.5976138778,
                0.5590166450, 0.5270459839});
        checkDensity(0.5, 2.0,
                x, new double[]{
                749.99925000000, 2.134537420613655, 1.34163575536, 0.95851150881,
                0.71151039830, 0.53032849490, 0.38729704363, 0.26892534859,
                0.16770415497, 0.07905610701});
        checkDensity(0.5, 4.0,
                x, new double[]{
                1.093746719e+03, 2.52142232809988, 1.252190241e+00, 6.849343920e-01,
                3.735417140e-01, 1.933481570e-01, 9.036885833e-02, 3.529621669e-02,
                9.782644546e-03, 1.152878503e-03});
        checkDensity(1.0, 0.1,
                x, new double[]{
                0.1000000900, 0.1099466942, 0.1222417336, 0.1378517623, 0.1583669403,
                0.1866069342, 0.2281113974, 0.2955236034, 0.4256718768,
                0.7943353837});
        checkDensity(1.0, 0.5,
                x, new double[]{
                0.5000002500, 0.5270465695, 0.5590173438, 0.5976147315, 0.6454977623,
                0.7071074883, 0.7905704033, 0.9128724506,
                1.1180367838, 1.5811467358});
        checkDensity(1, 1,
                x, new double[]{
                1, 1, 1,
                1, 1, 1, 1,
                1, 1, 1});
        checkDensity(1, 2,
                x, new double[]{
                1.999998, 1.799998, 1.599998, 1.399998, 1.199998, 0.999998, 0.799998,
                0.599998, 0.399998,
                0.199998});
        checkDensity(1, 4,
                x, new double[]{
                3.999988000012, 2.915990280011, 2.047992320010, 1.371994120008,
                0.863995680007, 0.499997000006, 0.255998080005, 0.107998920004,
                0.031999520002, 0.003999880001});
        checkDensity(2.0, 0.1,
                x, new double[]{
                1.100000990e-07, 1.209425730e-02, 2.689331586e-02, 4.549123318e-02,
                6.968162794e-02, 1.026340191e-01, 1.505537732e-01, 2.275534997e-01,
                3.745917198e-01, 7.863929037e-01});
        checkDensity(2.0, 0.5,
                x, new double[]{
                7.500003750e-07, 7.905777599e-02, 1.677060417e-01, 2.689275256e-01,
                3.872996256e-01, 5.303316769e-01, 7.115145488e-01, 9.585174425e-01,
                1.341645818e+00, 2.134537420613655});
        checkDensity(2, 1,
                x, new double[]{
                0.000002, 0.200002, 0.400002, 0.600002, 0.800002, 1.000002, 1.200002,
                1.400002, 1.600002,
                1.800002});
        checkDensity(2, 2,
                x, new double[]{
                5.9999940e-06, 5.4000480e-01, 9.6000360e-01, 1.2600024e+00,
                1.4400012e+00, 1.5000000e+00, 1.4399988e+00, 1.2599976e+00,
                9.5999640e-01, 5.3999520e-01});
        checkDensity(2, 4,
                x, new double[]{
                0.00001999994, 1.45800971996, 2.04800255997, 2.05799803998,
                1.72799567999, 1.24999500000, 0.76799552000, 0.37799676001,
                0.12799824001, 0.01799948000});
        checkDensity(4.0, 0.1,
                x, new double[]{
                1.193501074e-19, 1.312253162e-04, 1.167181580e-03, 4.442248535e-03,
                1.209679109e-02, 2.783958903e-02, 5.880649983e-02, 1.209791638e-01,
                2.601171405e-01, 6.911229392e-01});
        checkDensity(4.0, 0.5,
                x, new double[]{
                1.093750547e-18, 1.152948959e-03, 9.782950259e-03, 3.529697305e-02,
                9.037036449e-02, 1.933508639e-01, 3.735463833e-01, 6.849425461e-01,
                1.252205894e+00, 2.52142232809988});
        checkDensity(4, 1,
                x, new double[]{
                4.000000000e-18, 4.000120001e-03, 3.200048000e-02, 1.080010800e-01,
                2.560019200e-01, 5.000030000e-01, 8.640043200e-01, 1.372005880e+00,
                2.048007680e+00, 2.916009720e+00});
        checkDensity(4, 2,
                x, new double[]{
                1.999998000e-17, 1.800052000e-02, 1.280017600e-01, 3.780032400e-01,
                7.680044800e-01, 1.250005000e+00, 1.728004320e+00, 2.058001960e+00,
                2.047997440e+00, 1.457990280e+00});
        checkDensity(4, 4,
                x, new double[]{
                1.399995800e-16, 1.020627216e-01, 5.734464512e-01, 1.296547409e+00,
                1.935364838e+00, 2.187500000e+00, 1.935355162e+00, 1.296532591e+00,
                5.734335488e-01, 1.020572784e-01});

    }

// org.apache.commons.math3.distribution.BetaDistributionTest::testMoments
    public void testMoments() {
        final double tol = 1e-9;
        BetaDistribution dist;

        dist = new BetaDistribution(1, 1);
        Assert.assertEquals(dist.getNumericalMean(), 0.5, tol);
        Assert.assertEquals(dist.getNumericalVariance(), 1.0 / 12.0, tol);

        dist = new BetaDistribution(2, 5);
        Assert.assertEquals(dist.getNumericalMean(), 2.0 / 7.0, tol);
        Assert.assertEquals(dist.getNumericalVariance(), 10.0 / (49.0 * 8.0), tol);
    }

// org.apache.commons.math3.distribution.BinomialDistributionTest::testDegenerate0
    public void testDegenerate0() throws Exception {
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
    public void testDegenerate1() throws Exception {
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
    public void testInverseCumulativeProbabilityExtremes() throws Exception {
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
    public void testSmallDf() throws Exception {
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

// org.apache.commons.math3.distribution.ExponentialDistributionTest::testCumulativeProbabilityExtremes
    public void testCumulativeProbabilityExtremes() throws Exception {
        setCumulativeTestPoints(new double[] {-2, 0});
        setCumulativeTestValues(new double[] {0, 0});
        verifyCumulativeProbabilities();
    }

// org.apache.commons.math3.distribution.ExponentialDistributionTest::testInverseCumulativeProbabilityExtremes
    public void testInverseCumulativeProbabilityExtremes() throws Exception {
         setInverseCumulativeTestPoints(new double[] {0, 1});
         setInverseCumulativeTestValues(new double[] {0, Double.POSITIVE_INFINITY});
         verifyInverseCumulativeProbabilities();
    }

// org.apache.commons.math3.distribution.ExponentialDistributionTest::testCumulativeProbability2
    public void testCumulativeProbability2() throws Exception {
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
    public void testCumulativeProbabilityExtremes() throws Exception {
        setCumulativeTestPoints(new double[] {-2, 0});
        setCumulativeTestValues(new double[] {0, 0});
        verifyCumulativeProbabilities();
    }

// org.apache.commons.math3.distribution.FDistributionTest::testInverseCumulativeProbabilityExtremes
    public void testInverseCumulativeProbabilityExtremes() throws Exception {
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
    public void testLargeDegreesOfFreedom() throws Exception {
        FDistribution fd = new FDistribution(100000, 100000);
        double p = fd.cumulativeProbability(.999);
        double x = fd.inverseCumulativeProbability(p);
        Assert.assertEquals(.999, x, 1.0e-5);
    }

// org.apache.commons.math3.distribution.FDistributionTest::testSmallDegreesOfFreedom
    public void testSmallDegreesOfFreedom() throws Exception {
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
    public void testProbabilities() throws Exception {
        testProbability(-1.000, 4.0, 2.0, .0000);
        testProbability(15.501, 4.0, 2.0, .9499);
        testProbability(0.504, 4.0, 1.0, .0018);
        testProbability(10.011, 1.0, 2.0, .9933);
        testProbability(5.000, 2.0, 2.0, .7127);
    }

// org.apache.commons.math3.distribution.GammaDistributionTest::testValues
    public void testValues() throws Exception {
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
    public void testInverseCumulativeProbabilityExtremes() throws Exception {
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

// org.apache.commons.math3.distribution.HypergeometricDistributionTest::testDegenerateNoFailures
    public void testDegenerateNoFailures() throws Exception {
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
    public void testDegenerateNoSuccesses() throws Exception {
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
    public void testDegenerateFullSample() throws Exception {
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

// org.apache.commons.math3.distribution.LogNormalDistributionTest::testQuantiles
    public void testQuantiles() throws Exception {
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
    public void testInverseCumulativeProbabilityExtremes() throws Exception {
        setInverseCumulativeTestPoints(new double[] {0, 1});
        setInverseCumulativeTestValues(
                new double[] {0, Double.POSITIVE_INFINITY});
        verifyInverseCumulativeProbabilities();
    }

// org.apache.commons.math3.distribution.LogNormalDistributionTest::testGetScale
    public void testGetScale() {
        LogNormalDistribution distribution = (LogNormalDistribution)getDistribution();
        Assert.assertEquals(2.1, distribution.getScale(), 0);
    }

// org.apache.commons.math3.distribution.LogNormalDistributionTest::testGetShape
    public void testGetShape() {
        LogNormalDistribution distribution = (LogNormalDistribution)getDistribution();
        Assert.assertEquals(1.4, distribution.getShape(), 0);
    }

// org.apache.commons.math3.distribution.LogNormalDistributionTest::testPreconditions
    public void testPreconditions() {
        new LogNormalDistribution(1, 0);
    }

// org.apache.commons.math3.distribution.LogNormalDistributionTest::testDensity
    public void testDensity() {
        double [] x = new double[]{-2, -1, 0, 1, 2};
        
        checkDensity(0, 1, x, new double[] { 0.0000000000, 0.0000000000,
                                             0.0000000000, 0.3989422804,
                                             0.1568740193 });
        
        checkDensity(1.1, 1, x, new double[] { 0.0000000000, 0.0000000000,
                                               0.0000000000, 0.2178521770,
                                               0.1836267118});
    }

// org.apache.commons.math3.distribution.LogNormalDistributionTest::testExtremeValues
    public void testExtremeValues() throws Exception {
        LogNormalDistribution d = new LogNormalDistribution(0, 1);
        for (int i = 0; i < 1e5; i++) { 
            double upperTail = d.cumulativeProbability(i);
            if (i <= 72) { 
                Assert.assertTrue(upperTail < 1.0d);
            }
            else { 
                Assert.assertTrue(upperTail > 0.99999);
            }
        }

        Assert.assertEquals(d.cumulativeProbability(Double.MAX_VALUE), 1, 0);
        Assert.assertEquals(d.cumulativeProbability(-Double.MAX_VALUE), 0, 0);
        Assert.assertEquals(d.cumulativeProbability(Double.POSITIVE_INFINITY), 1, 0);
        Assert.assertEquals(d.cumulativeProbability(Double.NEGATIVE_INFINITY), 0, 0);
    }

// org.apache.commons.math3.distribution.LogNormalDistributionTest::testMeanVariance
    public void testMeanVariance() {
        final double tol = 1e-9;
        LogNormalDistribution dist;

        dist = new LogNormalDistribution(0, 1);
        Assert.assertEquals(dist.getNumericalMean(), 1.6487212707001282, tol);
        Assert.assertEquals(dist.getNumericalVariance(),
                            4.670774270471604, tol);

        dist = new LogNormalDistribution(2.2, 1.4);
        Assert.assertEquals(dist.getNumericalMean(), 24.046753552064498, tol);
        Assert.assertEquals(dist.getNumericalVariance(),
                            3526.913651880464, tol);

        dist = new LogNormalDistribution(-2000.9, 10.4);
        Assert.assertEquals(dist.getNumericalMean(), 0.0, tol);
        Assert.assertEquals(dist.getNumericalVariance(), 0.0, tol);
    }

// org.apache.commons.math3.distribution.NormalDistributionTest::testQuantiles
    public void testQuantiles() throws Exception {
        setDensityTestValues(new double[] {0.0385649760808, 0.172836231799, 0.284958771715, 0.172836231799, 0.0385649760808,
                0.00316560600853, 9.55930184035e-05, 1.06194251052e-06});
        verifyQuantiles();
        verifyDensities();

        setDistribution(new NormalDistribution(0, 1));
        setDensityTestValues(new double[] {0.0539909665132, 0.241970724519, 0.398942280401, 0.241970724519, 0.0539909665132,
                0.00443184841194, 0.000133830225765, 1.48671951473e-06});
        verifyQuantiles();
        verifyDensities();

        setDistribution(new NormalDistribution(0, 0.1));
        setDensityTestValues(new double[] {0.539909665132, 2.41970724519, 3.98942280401, 2.41970724519,
                0.539909665132, 0.0443184841194, 0.00133830225765, 1.48671951473e-05});
        verifyQuantiles();
        verifyDensities();
    }

// org.apache.commons.math3.distribution.NormalDistributionTest::testInverseCumulativeProbabilityExtremes
    public void testInverseCumulativeProbabilityExtremes() throws Exception {
        setInverseCumulativeTestPoints(new double[] {0, 1});
        setInverseCumulativeTestValues(
                new double[] {Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY});
        verifyInverseCumulativeProbabilities();
    }

// org.apache.commons.math3.distribution.NormalDistributionTest::testGetMean
    public void testGetMean() {
        NormalDistribution distribution = (NormalDistribution) getDistribution();
        Assert.assertEquals(2.1, distribution.getMean(), 0);
    }

// org.apache.commons.math3.distribution.NormalDistributionTest::testGetStandardDeviation
    public void testGetStandardDeviation() {
        NormalDistribution distribution = (NormalDistribution) getDistribution();
        Assert.assertEquals(1.4, distribution.getStandardDeviation(), 0);
    }

// org.apache.commons.math3.distribution.NormalDistributionTest::testPreconditions
    public void testPreconditions() {
        new NormalDistribution(1, 0);
    }

// org.apache.commons.math3.distribution.NormalDistributionTest::testDensity
    public void testDensity() {
        double [] x = new double[]{-2, -1, 0, 1, 2};
        
        checkDensity(0, 1, x, new double[]{0.05399096651, 0.24197072452, 0.39894228040, 0.24197072452, 0.05399096651});
        
        checkDensity(1.1, 1, x, new double[]{0.003266819056,0.043983595980,0.217852177033,0.396952547477,0.266085249899});
    }

// org.apache.commons.math3.distribution.NormalDistributionTest::testExtremeValues
    public void testExtremeValues() throws Exception {
        NormalDistribution distribution = new NormalDistribution(0, 1);
        for (int i = 0; i < 100; i++) { 
            double lowerTail = distribution.cumulativeProbability(-i);
            double upperTail = distribution.cumulativeProbability(i);
            if (i < 9) { 
                
                
                Assert.assertTrue(lowerTail > 0.0d);
                Assert.assertTrue(upperTail < 1.0d);
            }
            else { 
                Assert.assertTrue(lowerTail < 0.00001);
                Assert.assertTrue(upperTail > 0.99999);
            }
        }

        Assert.assertEquals(distribution.cumulativeProbability(Double.MAX_VALUE), 1, 0);
        Assert.assertEquals(distribution.cumulativeProbability(-Double.MAX_VALUE), 0, 0);
        Assert.assertEquals(distribution.cumulativeProbability(Double.POSITIVE_INFINITY), 1, 0);
        Assert.assertEquals(distribution.cumulativeProbability(Double.NEGATIVE_INFINITY), 0, 0);
    }

// org.apache.commons.math3.distribution.NormalDistributionTest::testMath280
    public void testMath280() {
        NormalDistribution normal = new NormalDistribution(0,1);
        double result = normal.inverseCumulativeProbability(0.9986501019683698);
        Assert.assertEquals(3.0, result, defaultTolerance);
        result = normal.inverseCumulativeProbability(0.841344746068543);
        Assert.assertEquals(1.0, result, defaultTolerance);
        result = normal.inverseCumulativeProbability(0.9999683287581673);
        Assert.assertEquals(4.0, result, defaultTolerance);
        result = normal.inverseCumulativeProbability(0.9772498680518209);
        Assert.assertEquals(2.0, result, defaultTolerance);
    }

// org.apache.commons.math3.distribution.NormalDistributionTest::testMoments
    public void testMoments() {
        final double tol = 1e-9;
        NormalDistribution dist;

        dist = new NormalDistribution(0, 1);
        Assert.assertEquals(dist.getNumericalMean(), 0, tol);
        Assert.assertEquals(dist.getNumericalVariance(), 1, tol);

        dist = new NormalDistribution(2.2, 1.4);
        Assert.assertEquals(dist.getNumericalMean(), 2.2, tol);
        Assert.assertEquals(dist.getNumericalVariance(), 1.4 * 1.4, tol);

        dist = new NormalDistribution(-2000.9, 10.4);
        Assert.assertEquals(dist.getNumericalMean(), -2000.9, tol);
        Assert.assertEquals(dist.getNumericalVariance(), 10.4 * 10.4, tol);
    }

// org.apache.commons.math3.distribution.PascalDistributionTest::testDegenerate0
    public void testDegenerate0() throws Exception {
        setDistribution(new PascalDistribution(5, 0.0d));
        setCumulativeTestPoints(new int[] {-1, 0, 1, 5, 10 });
        setCumulativeTestValues(new double[] {0d, 0d, 0d, 0d, 0d});
        setDensityTestPoints(new int[] {-1, 0, 1, 10, 11});
        setDensityTestValues(new double[] {0d, 0d, 0d, 0d, 0d});
        setInverseCumulativeTestPoints(new double[] {0.1d, 0.5d});
        setInverseCumulativeTestValues(new int[] {Integer.MAX_VALUE, Integer.MAX_VALUE});
        verifyDensities();
        verifyCumulativeProbabilities();
        verifyInverseCumulativeProbabilities();
    }

// org.apache.commons.math3.distribution.PascalDistributionTest::testDegenerate1
    public void testDegenerate1() throws Exception {
        setDistribution(new PascalDistribution(5, 1.0d));
        setCumulativeTestPoints(new int[] {-1, 0, 1, 2, 5, 10 });
        setCumulativeTestValues(new double[] {0d, 1d, 1d, 1d, 1d, 1d});
        setDensityTestPoints(new int[] {-1, 0, 1, 2, 5, 10});
        setDensityTestValues(new double[] {0d, 1d, 0d, 0d, 0d, 0d});
        setInverseCumulativeTestPoints(new double[] {0.1d, 0.5d});
        setInverseCumulativeTestValues(new int[] {0, 0});
        verifyDensities();
        verifyCumulativeProbabilities();
        verifyInverseCumulativeProbabilities();
    }

// org.apache.commons.math3.distribution.PascalDistributionTest::testMoments
    public void testMoments() {
        final double tol = 1e-9;
        PascalDistribution dist;

        dist = new PascalDistribution(10, 0.5);
        Assert.assertEquals(dist.getNumericalMean(), ( 10d * 0.5d ) / 0.5d, tol);
        Assert.assertEquals(dist.getNumericalVariance(), ( 10d * 0.5d ) / (0.5d * 0.5d), tol);

        dist = new PascalDistribution(25, 0.7);
        Assert.assertEquals(dist.getNumericalMean(), ( 25d * 0.3d ) / 0.7d, tol);
        Assert.assertEquals(dist.getNumericalVariance(), ( 25d * 0.3d ) / (0.7d * 0.7d), tol);
    }

// org.apache.commons.math3.distribution.PoissonDistributionTest::testNormalApproximateProbability
    public void testNormalApproximateProbability() throws Exception {
        PoissonDistribution dist = new PoissonDistribution(100);
        double result = dist.normalApproximateProbability(110)
                - dist.normalApproximateProbability(89);
        Assert.assertEquals(0.706281887248, result, 1E-10);

        dist = new PoissonDistribution(10000);
        result = dist.normalApproximateProbability(10200)
        - dist.normalApproximateProbability(9899);
        Assert.assertEquals(0.820070051552, result, 1E-10);
    }

// org.apache.commons.math3.distribution.PoissonDistributionTest::testDegenerateInverseCumulativeProbability
    public void testDegenerateInverseCumulativeProbability() throws Exception {
        PoissonDistribution dist = new PoissonDistribution(DEFAULT_TEST_POISSON_PARAMETER);
        Assert.assertEquals(Integer.MAX_VALUE, dist.inverseCumulativeProbability(1.0d));
        Assert.assertEquals(0, dist.inverseCumulativeProbability(0d));
    }

// org.apache.commons.math3.distribution.PoissonDistributionTest::testNegativeMean
    public void testNegativeMean() {
        new PoissonDistribution(-1);
    }

// org.apache.commons.math3.distribution.PoissonDistributionTest::testMean
    public void testMean() {
        PoissonDistribution dist = new PoissonDistribution(10.0);
        Assert.assertEquals(10.0, dist.getMean(), 0.0);
    }

// org.apache.commons.math3.distribution.PoissonDistributionTest::testLargeMeanCumulativeProbability
    public void testLargeMeanCumulativeProbability() {
        double mean = 1.0;
        while (mean <= 10000000.0) {
            PoissonDistribution dist = new PoissonDistribution(mean);

            double x = mean * 2.0;
            double dx = x / 10.0;
            double p = Double.NaN;
            double sigma = FastMath.sqrt(mean);
            while (x >= 0) {
                try {
                    p = dist.cumulativeProbability((int) x);
                    Assert.assertFalse("NaN cumulative probability returned for mean = " +
                            mean + " x = " + x,Double.isNaN(p));
                    if (x > mean - 2 * sigma) {
                        Assert.assertTrue("Zero cum probaility returned for mean = " +
                                mean + " x = " + x, p > 0);
                    }
                } catch (Exception ex) {
                    Assert.fail("mean of " + mean + " and x of " + x + " caused " + ex.getMessage());
                }
                x -= dx;
            }

            mean *= 10.0;
        }
    }

// org.apache.commons.math3.distribution.PoissonDistributionTest::testCumulativeProbabilitySpecial
    public void testCumulativeProbabilitySpecial() throws Exception {
        PoissonDistribution dist;
        dist = new PoissonDistribution(9120);
        checkProbability(dist, 9075);
        checkProbability(dist, 9102);
        dist = new PoissonDistribution(5058);
        checkProbability(dist, 5044);
        dist = new PoissonDistribution(6986);
        checkProbability(dist, 6950);
    }

// org.apache.commons.math3.distribution.PoissonDistributionTest::testLargeMeanInverseCumulativeProbability
    public void testLargeMeanInverseCumulativeProbability() throws Exception {
        double mean = 1.0;
        while (mean <= 100000.0) { 
            PoissonDistribution dist = new PoissonDistribution(mean);
            double p = 0.1;
            double dp = p;
            while (p < .99) {
                try {
                    int ret = dist.inverseCumulativeProbability(p);
                    
                    Assert.assertTrue(p <= dist.cumulativeProbability(ret));
                    Assert.assertTrue(p > dist.cumulativeProbability(ret - 1));
                } catch (Exception ex) {
                    Assert.fail("mean of " + mean + " and p of " + p + " caused " + ex.getMessage());
                }
                p += dp;
            }
            mean *= 10.0;
        }
    }

// org.apache.commons.math3.distribution.PoissonDistributionTest::testMoments
    public void testMoments() {
        final double tol = 1e-9;
        PoissonDistribution dist;

        dist = new PoissonDistribution(1);
        Assert.assertEquals(dist.getNumericalMean(), 1, tol);
        Assert.assertEquals(dist.getNumericalVariance(), 1, tol);

        dist = new PoissonDistribution(11.23);
        Assert.assertEquals(dist.getNumericalMean(), 11.23, tol);
        Assert.assertEquals(dist.getNumericalVariance(), 11.23, tol);
    }

// org.apache.commons.math3.distribution.TDistributionTest::testCumulativeProbabilityAgainstStackOverflow
    public void testCumulativeProbabilityAgainstStackOverflow() throws Exception {
        TDistribution td = new TDistribution(5.);
        td.cumulativeProbability(.1);
        td.cumulativeProbability(.01);
    }

// org.apache.commons.math3.distribution.TDistributionTest::testSmallDf
    public void testSmallDf() throws Exception {
        setDistribution(new TDistribution(1d));
        
        setCumulativeTestPoints(new double[] {-318.308838986, -31.8205159538, -12.7062047362,
                -6.31375151468, -3.07768353718, 318.308838986, 31.8205159538, 12.7062047362,
                 6.31375151468, 3.07768353718});
        setDensityTestValues(new double[] {3.14158231817e-06, 0.000314055924703, 0.00195946145194,
                0.00778959736375, 0.0303958893917, 3.14158231817e-06, 0.000314055924703,
                0.00195946145194, 0.00778959736375, 0.0303958893917});
        setInverseCumulativeTestValues(getCumulativeTestPoints());
        verifyCumulativeProbabilities();
        verifyInverseCumulativeProbabilities();
        verifyDensities();
    }

// org.apache.commons.math3.distribution.TDistributionTest::testInverseCumulativeProbabilityExtremes
    public void testInverseCumulativeProbabilityExtremes() throws Exception {
        setInverseCumulativeTestPoints(new double[] {0, 1});
        setInverseCumulativeTestValues(
                new double[] {Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY});
        verifyInverseCumulativeProbabilities();
    }

// org.apache.commons.math3.distribution.TDistributionTest::testDfAccessors
    public void testDfAccessors() {
        TDistribution dist = (TDistribution) getDistribution();
        Assert.assertEquals(5d, dist.getDegreesOfFreedom(), Double.MIN_VALUE);
    }

// org.apache.commons.math3.distribution.TDistributionTest::testPreconditions
    public void testPreconditions() {
        new TDistribution(0);
    }

// org.apache.commons.math3.distribution.TDistributionTest::testMoments
    public void testMoments() {
        final double tol = 1e-9;
        TDistribution dist;

        dist = new TDistribution(1);
        Assert.assertTrue(Double.isNaN(dist.getNumericalMean()));
        Assert.assertTrue(Double.isNaN(dist.getNumericalVariance()));

        dist = new TDistribution(1.5);
        Assert.assertEquals(dist.getNumericalMean(), 0, tol);
        Assert.assertTrue(Double.isInfinite(dist.getNumericalVariance()));

        dist = new TDistribution(5);
        Assert.assertEquals(dist.getNumericalMean(), 0, tol);
        Assert.assertEquals(dist.getNumericalVariance(), 5d / (5d - 2d), tol);
    }

// org.apache.commons.math3.distribution.TDistributionTest::nistData
    public void nistData(){
        double[] prob = new double[]{ 0.10,0.05,0.025,0.01,0.005,0.001};
        double[] args2 = new double[]{1.886,2.920,4.303,6.965,9.925,22.327};
        double[] args10 = new double[]{1.372,1.812,2.228,2.764,3.169,4.143};
        double[] args30 = new double[]{1.310,1.697,2.042,2.457,2.750,3.385};
        double[] args100= new double[]{1.290,1.660,1.984,2.364,2.626,3.174};
        TestUtils.assertEquals(prob, makeNistResults(args2, 2), 1.0e-4);
        TestUtils.assertEquals(prob, makeNistResults(args10, 10), 1.0e-4);
        TestUtils.assertEquals(prob, makeNistResults(args30, 30), 1.0e-4);
        TestUtils.assertEquals(prob, makeNistResults(args100, 100), 1.0e-4);
        return;
    }

// org.apache.commons.math3.distribution.TriangularDistributionTest::testGetLowerBound
    public void testGetLowerBound() {
        TriangularDistribution distribution = makeDistribution();
        Assert.assertEquals(-3.0, distribution.getSupportLowerBound(), 0);
    }

// org.apache.commons.math3.distribution.TriangularDistributionTest::testGetUpperBound
    public void testGetUpperBound() {
        TriangularDistribution distribution = makeDistribution();
        Assert.assertEquals(12.0, distribution.getSupportUpperBound(), 0);
    }

// org.apache.commons.math3.distribution.TriangularDistributionTest::testPreconditions1
    public void testPreconditions1() {
        new TriangularDistribution(0, 0, 0);
    }

// org.apache.commons.math3.distribution.TriangularDistributionTest::testPreconditions2
    public void testPreconditions2() {
        new TriangularDistribution(1, 1, 0);
    }

// org.apache.commons.math3.distribution.TriangularDistributionTest::testPreconditions3
    public void testPreconditions3() {
        new TriangularDistribution(0, 2, 1);
    }

// org.apache.commons.math3.distribution.TriangularDistributionTest::testPreconditions4
    public void testPreconditions4() {
        new TriangularDistribution(2, 1, 3);
    }

// org.apache.commons.math3.distribution.TriangularDistributionTest::testMeanVariance
    public void testMeanVariance() {
        TriangularDistribution dist;

        dist = new TriangularDistribution(0, 0.5, 1.0);
        Assert.assertEquals(dist.getNumericalMean(), 0.5, 0);
        Assert.assertEquals(dist.getNumericalVariance(), 1 / 24.0, 0);

        dist = new TriangularDistribution(0, 1, 1);
        Assert.assertEquals(dist.getNumericalMean(), 2 / 3.0, 0);
        Assert.assertEquals(dist.getNumericalVariance(), 1 / 18.0, 0);

        dist = new TriangularDistribution(-3, 2, 12);
        Assert.assertEquals(dist.getNumericalMean(), 3 + (2 / 3.0), 0);
        Assert.assertEquals(dist.getNumericalVariance(), 175 / 18.0, 0);
    }

// org.apache.commons.math3.distribution.UniformIntegerDistributionTest::testMoments
    public void testMoments() {
        UniformIntegerDistribution dist;

        dist = new UniformIntegerDistribution(0, 5);
        Assert.assertEquals(dist.getNumericalMean(), 2.5, 0);
        Assert.assertEquals(dist.getNumericalVariance(), 35 / 12.0, 0);

        dist = new UniformIntegerDistribution(0, 1);
        Assert.assertEquals(dist.getNumericalMean(), 0.5, 0);
        Assert.assertEquals(dist.getNumericalVariance(), 3 / 12.0, 0);
    }

// org.apache.commons.math3.distribution.UniformRealDistributionTest::testGetLowerBound
    public void testGetLowerBound() {
        UniformRealDistribution distribution = makeDistribution();
        Assert.assertEquals(-0.5, distribution.getSupportLowerBound(), 0);
    }

// org.apache.commons.math3.distribution.UniformRealDistributionTest::testGetUpperBound
    public void testGetUpperBound() {
        UniformRealDistribution distribution = makeDistribution();
        Assert.assertEquals(1.25, distribution.getSupportUpperBound(), 0);
    }

// org.apache.commons.math3.distribution.UniformRealDistributionTest::testPreconditions1
    public void testPreconditions1() {
        new UniformRealDistribution(0, 0);
    }

// org.apache.commons.math3.distribution.UniformRealDistributionTest::testPreconditions2
    public void testPreconditions2() {
        new UniformRealDistribution(1, 0);
    }

// org.apache.commons.math3.distribution.UniformRealDistributionTest::testMeanVariance
    public void testMeanVariance() {
        UniformRealDistribution dist;

        dist = new UniformRealDistribution(0, 1);
        Assert.assertEquals(dist.getNumericalMean(), 0.5, 0);
        Assert.assertEquals(dist.getNumericalVariance(), 1/12.0, 0);

        dist = new UniformRealDistribution(-1.5, 0.6);
        Assert.assertEquals(dist.getNumericalMean(), -0.45, 0);
        Assert.assertEquals(dist.getNumericalVariance(), 0.3675, 0);

        dist = new UniformRealDistribution(-0.5, 1.25);
        Assert.assertEquals(dist.getNumericalMean(), 0.375, 0);
        Assert.assertEquals(dist.getNumericalVariance(), 0.2552083333333333, 0);
    }

// org.apache.commons.math3.distribution.WeibullDistributionTest::testInverseCumulativeProbabilityExtremes
    public void testInverseCumulativeProbabilityExtremes() throws Exception {
        setInverseCumulativeTestPoints(new double[] {0.0, 1.0});
        setInverseCumulativeTestValues(
                new double[] {0.0, Double.POSITIVE_INFINITY});
        verifyInverseCumulativeProbabilities();
    }

// org.apache.commons.math3.distribution.WeibullDistributionTest::testAlpha
    public void testAlpha() {
        WeibullDistribution dist = new WeibullDistribution(1, 2);
        Assert.assertEquals(1, dist.getShape(), 0);
        try {
            dist = new WeibullDistribution(0, 2);
            Assert.fail("NotStrictlyPositiveException expected");
        } catch (NotStrictlyPositiveException e) {
            
        }
    }

// org.apache.commons.math3.distribution.WeibullDistributionTest::testBeta
    public void testBeta() {
        WeibullDistribution dist = new WeibullDistribution(1, 2);
        Assert.assertEquals(2, dist.getScale(), 0);
        try {
            dist = new WeibullDistribution(1, 0);
            Assert.fail("NotStrictlyPositiveException expected");
        } catch (NotStrictlyPositiveException e) {
            
        }
    }

// org.apache.commons.math3.distribution.WeibullDistributionTest::testMoments
    public void testMoments() {
        final double tol = 1e-9;
        WeibullDistribution dist;

        dist = new WeibullDistribution(2.5, 3.5);
        
        Assert.assertEquals(dist.getNumericalMean(), 3.5 * FastMath.exp(Gamma.logGamma(1 + (1 / 2.5))), tol);
        Assert.assertEquals(dist.getNumericalVariance(), (3.5 * 3.5) *
                FastMath.exp(Gamma.logGamma(1 + (2 / 2.5))) -
                (dist.getNumericalMean() * dist.getNumericalMean()), tol);

        dist = new WeibullDistribution(10.4, 2.222);
        Assert.assertEquals(dist.getNumericalMean(), 2.222 * FastMath.exp(Gamma.logGamma(1 + (1 / 10.4))), tol);
        Assert.assertEquals(dist.getNumericalVariance(), (2.222 * 2.222) *
                FastMath.exp(Gamma.logGamma(1 + (2 / 10.4))) -
                (dist.getNumericalMean() * dist.getNumericalMean()), tol);
    }

// org.apache.commons.math3.distribution.WeibullDistributionTest::testSampling
    public void testSampling() {}

// org.apache.commons.math3.distribution.ZipfDistributionTest::testPreconditions1
    public void testPreconditions1() {
        new ZipfDistribution(0, 1);
    }

// org.apache.commons.math3.distribution.ZipfDistributionTest::testPreconditions2
    public void testPreconditions2() {
        new ZipfDistribution(1, 0);
    }

// org.apache.commons.math3.distribution.ZipfDistributionTest::testMoments
    public void testMoments() {
        final double tol = 1e-9;
        ZipfDistribution dist;

        dist = new ZipfDistribution(2, 0.5);
        Assert.assertEquals(dist.getNumericalMean(), FastMath.sqrt(2), tol);
        Assert.assertEquals(dist.getNumericalVariance(), 0.24264068711928521, tol);
    }

// org.apache.commons.math3.special.BetaTest::testRegularizedBetaNanPositivePositive
    public void testRegularizedBetaNanPositivePositive() {
        testRegularizedBeta(Double.NaN, Double.NaN, 1.0, 1.0);
    }

// org.apache.commons.math3.special.BetaTest::testRegularizedBetaPositiveNanPositive
    public void testRegularizedBetaPositiveNanPositive() {
        testRegularizedBeta(Double.NaN, 0.5, Double.NaN, 1.0);
    }

// org.apache.commons.math3.special.BetaTest::testRegularizedBetaPositivePositiveNan
    public void testRegularizedBetaPositivePositiveNan() {
        testRegularizedBeta(Double.NaN, 0.5, 1.0, Double.NaN);
    }

// org.apache.commons.math3.special.BetaTest::testRegularizedBetaNegativePositivePositive
    public void testRegularizedBetaNegativePositivePositive() {
        testRegularizedBeta(Double.NaN, -0.5, 1.0, 2.0);
    }

// org.apache.commons.math3.special.BetaTest::testRegularizedBetaPositiveNegativePositive
    public void testRegularizedBetaPositiveNegativePositive() {
        testRegularizedBeta(Double.NaN, 0.5, -1.0, 2.0);
    }

// org.apache.commons.math3.special.BetaTest::testRegularizedBetaPositivePositiveNegative
    public void testRegularizedBetaPositivePositiveNegative() {
        testRegularizedBeta(Double.NaN, 0.5, 1.0, -2.0);
    }

// org.apache.commons.math3.special.BetaTest::testRegularizedBetaZeroPositivePositive
    public void testRegularizedBetaZeroPositivePositive() {
        testRegularizedBeta(0.0, 0.0, 1.0, 2.0);
    }

// org.apache.commons.math3.special.BetaTest::testRegularizedBetaPositiveZeroPositive
    public void testRegularizedBetaPositiveZeroPositive() {
        testRegularizedBeta(Double.NaN, 0.5, 0.0, 2.0);
    }

// org.apache.commons.math3.special.BetaTest::testRegularizedBetaPositivePositiveZero
    public void testRegularizedBetaPositivePositiveZero() {
        testRegularizedBeta(Double.NaN, 0.5, 1.0, 0.0);
    }

// org.apache.commons.math3.special.BetaTest::testRegularizedBetaPositivePositivePositive
    public void testRegularizedBetaPositivePositivePositive() {
        testRegularizedBeta(0.75, 0.5, 1.0, 2.0);
    }

// org.apache.commons.math3.special.BetaTest::testLogBetaNanPositive
    public void testLogBetaNanPositive() {
        testLogBeta(Double.NaN, Double.NaN, 2.0);
    }

// org.apache.commons.math3.special.BetaTest::testLogBetaPositiveNan
    public void testLogBetaPositiveNan() {
        testLogBeta(Double.NaN, 1.0, Double.NaN);
    }

// org.apache.commons.math3.special.BetaTest::testLogBetaNegativePositive
    public void testLogBetaNegativePositive() {
        testLogBeta(Double.NaN, -1.0, 2.0);
    }

// org.apache.commons.math3.special.BetaTest::testLogBetaPositiveNegative
    public void testLogBetaPositiveNegative() {
        testLogBeta(Double.NaN, 1.0, -2.0);
    }

// org.apache.commons.math3.special.BetaTest::testLogBetaZeroPositive
    public void testLogBetaZeroPositive() {
        testLogBeta(Double.NaN, 0.0, 2.0);
    }

// org.apache.commons.math3.special.BetaTest::testLogBetaPositiveZero
    public void testLogBetaPositiveZero() {
        testLogBeta(Double.NaN, 1.0, 0.0);
    }

// org.apache.commons.math3.special.BetaTest::testLogBetaPositivePositive
    public void testLogBetaPositivePositive() {
        testLogBeta(-0.693147180559945, 1.0, 2.0);
    }

// org.apache.commons.math3.special.ErfTest::testErf0
    public void testErf0() {
        double actual = Erf.erf(0.0);
        double expected = 0.0;
        Assert.assertEquals(expected, actual, 1.0e-15);
        Assert.assertEquals(1 - expected, Erf.erfc(0.0), 1.0e-15);
    }

// org.apache.commons.math3.special.ErfTest::testErf1960
    public void testErf1960() {
        double x = 1.960 / FastMath.sqrt(2.0);
        double actual = Erf.erf(x);
        double expected = 0.95;
        Assert.assertEquals(expected, actual, 1.0e-5);
        Assert.assertEquals(1 - actual, Erf.erfc(x), 1.0e-15);

        actual = Erf.erf(-x);
        expected = -expected;
        Assert.assertEquals(expected, actual, 1.0e-5);
        Assert.assertEquals(1 - actual, Erf.erfc(-x), 1.0e-15);
    }

// org.apache.commons.math3.special.ErfTest::testErf2576
    public void testErf2576() {
        double x = 2.576 / FastMath.sqrt(2.0);
        double actual = Erf.erf(x);
        double expected = 0.99;
        Assert.assertEquals(expected, actual, 1.0e-5);
        Assert.assertEquals(1 - actual, Erf.erfc(x), 1e-15);

        actual = Erf.erf(-x);
        expected = -expected;
        Assert.assertEquals(expected, actual, 1.0e-5);
        Assert.assertEquals(1 - actual, Erf.erfc(-x), 1.0e-15);
    }

// org.apache.commons.math3.special.ErfTest::testErf2807
    public void testErf2807() {
        double x = 2.807 / FastMath.sqrt(2.0);
        double actual = Erf.erf(x);
        double expected = 0.995;
        Assert.assertEquals(expected, actual, 1.0e-5);
        Assert.assertEquals(1 - actual, Erf.erfc(x), 1.0e-15);

        actual = Erf.erf(-x);
        expected = -expected;
        Assert.assertEquals(expected, actual, 1.0e-5);
        Assert.assertEquals(1 - actual, Erf.erfc(-x), 1.0e-15);
    }

// org.apache.commons.math3.special.ErfTest::testErf3291
    public void testErf3291() {
        double x = 3.291 / FastMath.sqrt(2.0);
        double actual = Erf.erf(x);
        double expected = 0.999;
        Assert.assertEquals(expected, actual, 1.0e-5);
        Assert.assertEquals(1 - expected, Erf.erfc(x), 1.0e-5);

        actual = Erf.erf(-x);
        expected = -expected;
        Assert.assertEquals(expected, actual, 1.0e-5);
        Assert.assertEquals(1 - expected, Erf.erfc(-x), 1.0e-5);
    }

// org.apache.commons.math3.special.ErfTest::testLargeValues
    public void testLargeValues() throws Exception {
        for (int i = 1; i < 200; i*=10) {
            double result = Erf.erf(i);
            Assert.assertFalse(Double.isNaN(result));
            Assert.assertTrue(result > 0 && result <= 1);
            result = Erf.erf(-i);
            Assert.assertFalse(Double.isNaN(result));
            Assert.assertTrue(result >= -1 && result < 0);
            result = Erf.erfc(i);
            Assert.assertFalse(Double.isNaN(result));
            Assert.assertTrue(result >= 0 && result < 1);
            result = Erf.erfc(-i);
            Assert.assertFalse(Double.isNaN(result));
            Assert.assertTrue(result >= 1 && result <= 2);    
        }
        Assert.assertEquals(-1, Erf.erf(Double.NEGATIVE_INFINITY), 0);
        Assert.assertEquals(1, Erf.erf(Double.POSITIVE_INFINITY), 0);
        Assert.assertEquals(2, Erf.erfc(Double.NEGATIVE_INFINITY), 0);
        Assert.assertEquals(0, Erf.erfc(Double.POSITIVE_INFINITY), 0);
    }

// org.apache.commons.math3.special.ErfTest::testErfGnu
    public void testErfGnu() throws Exception {
        final double tol = 1E-15;
        final double[] gnuValues = new double[] {-1, -1, -1, -1, -1, 
        -1, -1, -1, -0.99999999999999997848, 
        -0.99999999999999264217, -0.99999999999846254017, -0.99999999980338395581, -0.99999998458274209971, 
        -0.9999992569016276586, -0.99997790950300141459, -0.99959304798255504108, -0.99532226501895273415, 
        -0.96610514647531072711, -0.84270079294971486948, -0.52049987781304653809,  0, 
         0.52049987781304653809, 0.84270079294971486948, 0.96610514647531072711, 0.99532226501895273415, 
         0.99959304798255504108, 0.99997790950300141459, 0.9999992569016276586, 0.99999998458274209971, 
         0.99999999980338395581, 0.99999999999846254017, 0.99999999999999264217, 0.99999999999999997848, 
         1,  1,  1,  1, 
         1,  1,  1,  1};
        double x = -10d;
        for (int i = 0; i < 41; i++) {
            Assert.assertEquals(gnuValues[i], Erf.erf(x), tol);
            x += 0.5d;
        }
    }

// org.apache.commons.math3.special.ErfTest::testErfcGnu
    public void testErfcGnu() throws Exception {
        final double tol = 1E-15;
        final double[] gnuValues = new double[] { 2,  2,  2,  2,  2, 
        2,  2,  2, 1.9999999999999999785, 
        1.9999999999999926422, 1.9999999999984625402, 1.9999999998033839558, 1.9999999845827420998, 
        1.9999992569016276586, 1.9999779095030014146, 1.9995930479825550411, 1.9953222650189527342, 
        1.9661051464753107271, 1.8427007929497148695, 1.5204998778130465381,  1, 
        0.47950012218695346194, 0.15729920705028513051, 0.033894853524689272893, 0.0046777349810472658333, 
        0.00040695201744495893941, 2.2090496998585441366E-05, 7.4309837234141274516E-07, 1.5417257900280018858E-08, 
        1.966160441542887477E-10, 1.5374597944280348501E-12, 7.3578479179743980661E-15, 2.1519736712498913103E-17, 
        3.8421483271206474691E-20, 4.1838256077794144006E-23, 2.7766493860305691016E-26, 1.1224297172982927079E-29, 
        2.7623240713337714448E-33, 4.1370317465138102353E-37, 3.7692144856548799402E-41, 2.0884875837625447567E-45};
        double x = -10d;
        for (int i = 0; i < 41; i++) {
            Assert.assertEquals(gnuValues[i], Erf.erfc(x), tol);
            x += 0.5d;
        }
    }

// org.apache.commons.math3.special.ErfTest::testErfcMaple
    public void testErfcMaple() throws Exception {
        double[][] ref = new double[][]
                        {{0.1, 4.60172162722971e-01},
                         {1.2, 1.15069670221708e-01},
                         {2.3, 1.07241100216758e-02},
                         {3.4, 3.36929265676881e-04},
                         {4.5, 3.39767312473006e-06},
                         {5.6, 1.07175902583109e-08}, 
                         {6.7, 1.04209769879652e-11},
                         {7.8, 3.09535877195870e-15},
                         {8.9, 2.79233437493966e-19},
                         {10.0, 7.61985302416053e-24},
                         {11.1, 6.27219439321703e-29},
                         {12.2, 1.55411978638959e-34}, 
                         {13.3, 1.15734162836904e-40},
                         {14.4, 2.58717592540226e-47},
                         {15.5, 1.73446079179387e-54},
                         {16.6, 3.48454651995041e-62}
        };
        for (int i = 0; i < 15; i++) {
            final double result = 0.5*Erf.erfc(ref[i][0]/Math.sqrt(2));
            Assert.assertEquals(ref[i][1], result, 1E-15);
            TestUtils.assertRelativelyEquals(ref[i][1], result, 1E-13);
        }
    }

// org.apache.commons.math3.special.ErfTest::testTwoArgumentErf
    public void testTwoArgumentErf() throws Exception {
        double[] xi = new double[]{-2.0, -1.0, -0.9, -0.1, 0.0, 0.1, 0.9, 1.0, 2.0};
        for(double x1 : xi) {
            for(double x2 : xi) {
                double a = Erf.erf(x1, x2);
                double b = Erf.erf(x2) - Erf.erf(x1);
                double c = Erf.erfc(x1) - Erf.erfc(x2);
                Assert.assertEquals(a, b, 1E-15);
                Assert.assertEquals(a, c, 1E-15);
            }
        }
    }

// org.apache.commons.math3.special.GammaTest::testRegularizedGammaNanPositive
    public void testRegularizedGammaNanPositive() {
        testRegularizedGamma(Double.NaN, Double.NaN, 1.0);
    }

// org.apache.commons.math3.special.GammaTest::testRegularizedGammaPositiveNan
    public void testRegularizedGammaPositiveNan() {
        testRegularizedGamma(Double.NaN, 1.0, Double.NaN);
    }

// org.apache.commons.math3.special.GammaTest::testRegularizedGammaNegativePositive
    public void testRegularizedGammaNegativePositive() {
        testRegularizedGamma(Double.NaN, -1.5, 1.0);
    }

// org.apache.commons.math3.special.GammaTest::testRegularizedGammaPositiveNegative
    public void testRegularizedGammaPositiveNegative() {
        testRegularizedGamma(Double.NaN, 1.0, -1.0);
    }

// org.apache.commons.math3.special.GammaTest::testRegularizedGammaZeroPositive
    public void testRegularizedGammaZeroPositive() {
        testRegularizedGamma(Double.NaN, 0.0, 1.0);
    }

// org.apache.commons.math3.special.GammaTest::testRegularizedGammaPositiveZero
    public void testRegularizedGammaPositiveZero() {
        testRegularizedGamma(0.0, 1.0, 0.0);
    }

// org.apache.commons.math3.special.GammaTest::testRegularizedGammaPositivePositive
    public void testRegularizedGammaPositivePositive() {
        testRegularizedGamma(0.632120558828558, 1.0, 1.0);
    }

// org.apache.commons.math3.special.GammaTest::testLogGammaNan
    public void testLogGammaNan() {
        testLogGamma(Double.NaN, Double.NaN);
    }

// org.apache.commons.math3.special.GammaTest::testLogGammaNegative
    public void testLogGammaNegative() {
        testLogGamma(Double.NaN, -1.0);
    }

// org.apache.commons.math3.special.GammaTest::testLogGammaZero
    public void testLogGammaZero() {
        testLogGamma(Double.NaN, 0.0);
    }

// org.apache.commons.math3.special.GammaTest::testLogGammaPositive
    public void testLogGammaPositive() {
        testLogGamma(0.6931471805599457, 3.0);
    }

// org.apache.commons.math3.special.GammaTest::testDigammaLargeArgs
    public void testDigammaLargeArgs() {
        double eps = 1e-8;
        Assert.assertEquals(4.6001618527380874002, Gamma.digamma(100), eps);
        Assert.assertEquals(3.9019896734278921970, Gamma.digamma(50), eps);
        Assert.assertEquals(2.9705239922421490509, Gamma.digamma(20), eps);
        Assert.assertEquals(2.9958363947076465821, Gamma.digamma(20.5), eps);
        Assert.assertEquals(2.2622143570941481605, Gamma.digamma(10.1), eps);
        Assert.assertEquals(2.1168588189004379233, Gamma.digamma(8.8), eps);
        Assert.assertEquals(1.8727843350984671394, Gamma.digamma(7), eps);
        Assert.assertEquals(0.42278433509846713939, Gamma.digamma(2), eps);
        Assert.assertEquals(-100.56088545786867450, Gamma.digamma(0.01), eps);
        Assert.assertEquals(-4.0390398965921882955, Gamma.digamma(-0.8), eps);
        Assert.assertEquals(4.2003210041401844726, Gamma.digamma(-6.3), eps);
    }

// org.apache.commons.math3.special.GammaTest::testDigammaSmallArgs
    public void testDigammaSmallArgs() {
        
        
        double[] expected = {-10.423754940411076795, -100.56088545786867450, -1000.5755719318103005,
                -10000.577051183514335, -100000.57719921568107, -1.0000005772140199687e6, -1.0000000577215500408e7,
                -1.0000000057721564845e8, -1.0000000005772156633e9, -1.0000000000577215665e10, -1.0000000000057721566e11,
                -1.0000000000005772157e12, -1.0000000000000577216e13, -1.0000000000000057722e14, -1.0000000000000005772e15, -1e+16,
                -1e+17, -1e+18, -1e+19, -1e+20, -1e+21, -1e+22, -1e+23, -1e+24, -1e+25, -1e+26,
                -1e+27, -1e+28, -1e+29, -1e+30};
        for (double n = 1; n < 30; n++) {
            checkRelativeError(String.format("Test %.0f: ", n), expected[(int) (n - 1)], Gamma.digamma(FastMath.pow(10.0, -n)), 1e-8);
        }
    }

// org.apache.commons.math3.special.GammaTest::testTrigamma
    public void testTrigamma() {
        double eps = 1e-8;
        
        
        
        double[] data = {
                1e-4, 1.0000000164469368793e8,
                1e-3, 1.0000016425331958690e6,
                1e-2, 10001.621213528313220,
                1e-1, 101.43329915079275882,
                1, 1.6449340668482264365,
                2, 0.64493406684822643647,
                3, 0.39493406684822643647,
                4, 0.28382295573711532536,
                5, 0.22132295573711532536,
                10, 0.10516633568168574612,
                20, 0.051270822935203119832,
                50, 0.020201333226697125806,
                100, 0.010050166663333571395
        };
        for (int i = data.length - 2; i >= 0; i -= 2) {
            Assert.assertEquals(String.format("trigamma %.0f", data[i]), data[i + 1], Gamma.trigamma(data[i]), eps);
        }
    }

// org.apache.commons.math3.stat.correlation.PearsonsCorrelationTest::testLongly
    public void testLongly() throws Exception {
        RealMatrix matrix = createRealMatrix(longleyData, 16, 7);
        PearsonsCorrelation corrInstance = new PearsonsCorrelation(matrix);
        RealMatrix correlationMatrix = corrInstance.getCorrelationMatrix();
        double[] rData = new double[] {
                1.000000000000000, 0.9708985250610560, 0.9835516111796693, 0.5024980838759942,
                0.4573073999764817, 0.960390571594376, 0.9713294591921188,
                0.970898525061056, 1.0000000000000000, 0.9915891780247822, 0.6206333925590966,
                0.4647441876006747, 0.979163432977498, 0.9911491900672053,
                0.983551611179669, 0.9915891780247822, 1.0000000000000000, 0.6042609398895580,
                0.4464367918926265, 0.991090069458478, 0.9952734837647849,
                0.502498083875994, 0.6206333925590966, 0.6042609398895580, 1.0000000000000000,
                -0.1774206295018783, 0.686551516365312, 0.6682566045621746,
                0.457307399976482, 0.4647441876006747, 0.4464367918926265, -0.1774206295018783,
                1.0000000000000000, 0.364416267189032, 0.4172451498349454,
                0.960390571594376, 0.9791634329774981, 0.9910900694584777, 0.6865515163653120,
                0.3644162671890320, 1.000000000000000, 0.9939528462329257,
                0.971329459192119, 0.9911491900672053, 0.9952734837647849, 0.6682566045621746,
                0.4172451498349454, 0.993952846232926, 1.0000000000000000
        };
        TestUtils.assertEquals("correlation matrix", createRealMatrix(rData, 7, 7), correlationMatrix, 10E-15);

        double[] rPvalues = new double[] {
                4.38904690369668e-10,
                8.36353208910623e-12, 7.8159700933611e-14,
                0.0472894097790304, 0.01030636128354301, 0.01316878049026582,
                0.0749178049642416, 0.06971758330341182, 0.0830166169296545, 0.510948586323452,
                3.693245043123738e-09, 4.327782576751815e-11, 1.167954621905665e-13, 0.00331028281967516, 0.1652293725106684,
                3.95834476307755e-10, 1.114663916723657e-13, 1.332267629550188e-15, 0.00466039138541463, 0.1078477071581498, 7.771561172376096e-15
        };
        RealMatrix rPMatrix = createLowerTriangularRealMatrix(rPvalues, 7);
        fillUpper(rPMatrix, 0d);
        TestUtils.assertEquals("correlation p values", rPMatrix, corrInstance.getCorrelationPValues(), 10E-15);
    }

// org.apache.commons.math3.stat.correlation.PearsonsCorrelationTest::testSwissFertility
    public void testSwissFertility() throws Exception {
         RealMatrix matrix = createRealMatrix(swissData, 47, 5);
         PearsonsCorrelation corrInstance = new PearsonsCorrelation(matrix);
         RealMatrix correlationMatrix = corrInstance.getCorrelationMatrix();
         double[] rData = new double[] {
               1.0000000000000000, 0.3530791836199747, -0.6458827064572875, -0.6637888570350691,  0.4636847006517939,
                 0.3530791836199747, 1.0000000000000000,-0.6865422086171366, -0.6395225189483201, 0.4010950530487398,
                -0.6458827064572875, -0.6865422086171366, 1.0000000000000000, 0.6984152962884830, -0.5727418060641666,
                -0.6637888570350691, -0.6395225189483201, 0.6984152962884830, 1.0000000000000000, -0.1538589170909148,
                 0.4636847006517939, 0.4010950530487398, -0.5727418060641666, -0.1538589170909148, 1.0000000000000000
         };
         TestUtils.assertEquals("correlation matrix", createRealMatrix(rData, 5, 5), correlationMatrix, 10E-15);

         double[] rPvalues = new double[] {
                 0.01491720061472623,
                 9.45043734069043e-07, 9.95151527133974e-08,
                 3.658616965962355e-07, 1.304590105694471e-06, 4.811397236181847e-08,
                 0.001028523190118147, 0.005204433539191644, 2.588307925380906e-05, 0.301807756132683
         };
         RealMatrix rPMatrix = createLowerTriangularRealMatrix(rPvalues, 5);
         fillUpper(rPMatrix, 0d);
         TestUtils.assertEquals("correlation p values", rPMatrix, corrInstance.getCorrelationPValues(), 10E-15);
    }

// org.apache.commons.math3.stat.correlation.PearsonsCorrelationTest::testPValueNearZero
    public void testPValueNearZero() throws Exception {
        
        int dimension = 120;
        double[][] data = new double[dimension][2];
        for (int i = 0; i < dimension; i++) {
            data[i][0] = i;
            data[i][1] = i + 1/((double)i + 1);
        }
        PearsonsCorrelation corrInstance = new PearsonsCorrelation(data);
        Assert.assertTrue(corrInstance.getCorrelationPValues().getEntry(0, 1) > 0);
    }

// org.apache.commons.math3.stat.correlation.PearsonsCorrelationTest::testConstant
    public void testConstant() {
        double[] noVariance = new double[] {1, 1, 1, 1};
        double[] values = new double[] {1, 2, 3, 4};
        Assert.assertTrue(Double.isNaN(new PearsonsCorrelation().correlation(noVariance, values)));
    }

// org.apache.commons.math3.stat.correlation.PearsonsCorrelationTest::testInsufficientData
    public void testInsufficientData() {
        double[] one = new double[] {1};
        double[] two = new double[] {2};
        try {
            new PearsonsCorrelation().correlation(one, two);
            Assert.fail("Expecting IllegalArgumentException");
        } catch (IllegalArgumentException ex) {
            
        }
        RealMatrix matrix = new BlockRealMatrix(new double[][] {{0},{1}});
        try {
            new PearsonsCorrelation(matrix);
            Assert.fail("Expecting IllegalArgumentException");
        } catch (IllegalArgumentException ex) {
            
        }
    }

// org.apache.commons.math3.stat.correlation.PearsonsCorrelationTest::testStdErrorConsistency
    public void testStdErrorConsistency() throws Exception {
        TDistribution tDistribution = new TDistribution(45);
        RealMatrix matrix = createRealMatrix(swissData, 47, 5);
        PearsonsCorrelation corrInstance = new PearsonsCorrelation(matrix);
        RealMatrix rValues = corrInstance.getCorrelationMatrix();
        RealMatrix pValues = corrInstance.getCorrelationPValues();
        RealMatrix stdErrors = corrInstance.getCorrelationStandardErrors();
        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < i; j++) {
                double t = FastMath.abs(rValues.getEntry(i, j)) / stdErrors.getEntry(i, j);
                double p = 2 * (1 - tDistribution.cumulativeProbability(t));
                Assert.assertEquals(p, pValues.getEntry(i, j), 10E-15);
            }
        }
    }

// org.apache.commons.math3.stat.correlation.PearsonsCorrelationTest::testCovarianceConsistency
    public void testCovarianceConsistency() throws Exception {
        RealMatrix matrix = createRealMatrix(longleyData, 16, 7);
        PearsonsCorrelation corrInstance = new PearsonsCorrelation(matrix);
        Covariance covInstance = new Covariance(matrix);
        PearsonsCorrelation corrFromCovInstance = new PearsonsCorrelation(covInstance);
        TestUtils.assertEquals("correlation values", corrInstance.getCorrelationMatrix(),
                corrFromCovInstance.getCorrelationMatrix(), 10E-15);
        TestUtils.assertEquals("p values", corrInstance.getCorrelationPValues(),
                corrFromCovInstance.getCorrelationPValues(), 10E-15);
        TestUtils.assertEquals("standard errors", corrInstance.getCorrelationStandardErrors(),
                corrFromCovInstance.getCorrelationStandardErrors(), 10E-15);

        PearsonsCorrelation corrFromCovInstance2 =
            new PearsonsCorrelation(covInstance.getCovarianceMatrix(), 16);
        TestUtils.assertEquals("correlation values", corrInstance.getCorrelationMatrix(),
                corrFromCovInstance2.getCorrelationMatrix(), 10E-15);
        TestUtils.assertEquals("p values", corrInstance.getCorrelationPValues(),
                corrFromCovInstance2.getCorrelationPValues(), 10E-15);
        TestUtils.assertEquals("standard errors", corrInstance.getCorrelationStandardErrors(),
                corrFromCovInstance2.getCorrelationStandardErrors(), 10E-15);
    }

// org.apache.commons.math3.stat.correlation.PearsonsCorrelationTest::testConsistency
    public void testConsistency() {
        RealMatrix matrix = createRealMatrix(longleyData, 16, 7);
        PearsonsCorrelation corrInstance = new PearsonsCorrelation(matrix);
        double[][] data = matrix.getData();
        double[] x = matrix.getColumn(0);
        double[] y = matrix.getColumn(1);
        Assert.assertEquals(new PearsonsCorrelation().correlation(x, y),
                corrInstance.getCorrelationMatrix().getEntry(0, 1), Double.MIN_VALUE);
        TestUtils.assertEquals("Correlation matrix", corrInstance.getCorrelationMatrix(),
                new PearsonsCorrelation().computeCorrelationMatrix(data), Double.MIN_VALUE);
    }

// org.apache.commons.math3.stat.correlation.SpearmansRankCorrelationTest::testLongly
    public void testLongly() throws Exception {
        RealMatrix matrix = createRealMatrix(longleyData, 16, 7);
        SpearmansCorrelation corrInstance = new SpearmansCorrelation(matrix);
        RealMatrix correlationMatrix = corrInstance.getCorrelationMatrix();
        double[] rData = new double[] {
                1, 0.982352941176471, 0.985294117647059, 0.564705882352941, 0.2264705882352941, 0.976470588235294,
                0.976470588235294, 0.982352941176471, 1, 0.997058823529412, 0.664705882352941, 0.2205882352941176,
                0.997058823529412, 0.997058823529412, 0.985294117647059, 0.997058823529412, 1, 0.638235294117647,
                0.2235294117647059, 0.9941176470588236, 0.9941176470588236, 0.564705882352941, 0.664705882352941,
                0.638235294117647, 1, -0.3411764705882353, 0.685294117647059, 0.685294117647059, 0.2264705882352941,
                0.2205882352941176, 0.2235294117647059, -0.3411764705882353, 1, 0.2264705882352941, 0.2264705882352941,
                0.976470588235294, 0.997058823529412, 0.9941176470588236, 0.685294117647059, 0.2264705882352941, 1, 1,
                0.976470588235294, 0.997058823529412, 0.9941176470588236, 0.685294117647059, 0.2264705882352941, 1, 1
        };
        TestUtils.assertEquals("Spearman's correlation matrix", createRealMatrix(rData, 7, 7), correlationMatrix, 10E-15);
    }

// org.apache.commons.math3.stat.correlation.SpearmansRankCorrelationTest::testSwiss
    public void testSwiss() throws Exception {
        RealMatrix matrix = createRealMatrix(swissData, 47, 5);
        SpearmansCorrelation corrInstance = new SpearmansCorrelation(matrix);
        RealMatrix correlationMatrix = corrInstance.getCorrelationMatrix();
        double[] rData = new double[] {
                1, 0.2426642769364176, -0.660902996352354, -0.443257690360988, 0.4136455623012432,
                0.2426642769364176, 1, -0.598859938748963, -0.650463814145816, 0.2886878090882852,
               -0.660902996352354, -0.598859938748963, 1, 0.674603831406147, -0.4750575257171745,
               -0.443257690360988, -0.650463814145816, 0.674603831406147, 1, -0.1444163088302244,
                0.4136455623012432, 0.2886878090882852, -0.4750575257171745, -0.1444163088302244, 1
        };
        TestUtils.assertEquals("Spearman's correlation matrix", createRealMatrix(rData, 5, 5), correlationMatrix, 10E-15);
    }

// org.apache.commons.math3.stat.correlation.SpearmansRankCorrelationTest::testConstant
    public void testConstant() {
        double[] noVariance = new double[] {1, 1, 1, 1};
        double[] values = new double[] {1, 2, 3, 4};
        Assert.assertTrue(Double.isNaN(new SpearmansCorrelation().correlation(noVariance, values)));
    }

// org.apache.commons.math3.stat.correlation.SpearmansRankCorrelationTest::testInsufficientData
    public void testInsufficientData() {
        double[] one = new double[] {1};
        double[] two = new double[] {2};
        try {
            new SpearmansCorrelation().correlation(one, two);
            Assert.fail("Expecting IllegalArgumentException");
        } catch (IllegalArgumentException ex) {
            
        }
        RealMatrix matrix = new BlockRealMatrix(new double[][] {{0},{1}});
        try {
            new SpearmansCorrelation(matrix);
            Assert.fail("Expecting IllegalArgumentException");
        } catch (IllegalArgumentException ex) {
            
        }
    }

// org.apache.commons.math3.stat.correlation.SpearmansRankCorrelationTest::testConsistency
    public void testConsistency() {
        RealMatrix matrix = createRealMatrix(longleyData, 16, 7);
        SpearmansCorrelation corrInstance = new SpearmansCorrelation(matrix);
        double[][] data = matrix.getData();
        double[] x = matrix.getColumn(0);
        double[] y = matrix.getColumn(1);
        Assert.assertEquals(new SpearmansCorrelation().correlation(x, y),
                corrInstance.getCorrelationMatrix().getEntry(0, 1), Double.MIN_VALUE);
        TestUtils.assertEquals("Correlation matrix", corrInstance.getCorrelationMatrix(),
                new SpearmansCorrelation().computeCorrelationMatrix(data), Double.MIN_VALUE);
    }

// org.apache.commons.math3.stat.correlation.SpearmansRankCorrelationTest::testStdErrorConsistency
    public void testStdErrorConsistency() throws Exception {}

// org.apache.commons.math3.stat.correlation.SpearmansRankCorrelationTest::testCovarianceConsistency
    public void testCovarianceConsistency() throws Exception {}

// org.apache.commons.math3.stat.inference.ChiSquareTestTest::testChiSquare
    public void testChiSquare() throws Exception {

        
        
        
        

        long[] observed = {10, 9, 11};
        double[] expected = {10, 10, 10};
        Assert.assertEquals("chi-square statistic", 0.2,  testStatistic.chiSquare(expected, observed), 10E-12);
        Assert.assertEquals("chi-square p-value", 0.904837418036, testStatistic.chiSquareTest(expected, observed), 1E-10);

        long[] observed1 = { 500, 623, 72, 70, 31 };
        double[] expected1 = { 485, 541, 82, 61, 37 };
        Assert.assertEquals( "chi-square test statistic", 9.023307936427388, testStatistic.chiSquare(expected1, observed1), 1E-10);
        Assert.assertEquals("chi-square p-value", 0.06051952647453607, testStatistic.chiSquareTest(expected1, observed1), 1E-9);
        Assert.assertTrue("chi-square test reject", testStatistic.chiSquareTest(expected1, observed1, 0.08));
        Assert.assertTrue("chi-square test accept", !testStatistic.chiSquareTest(expected1, observed1, 0.05));

        try {
            testStatistic.chiSquareTest(expected1, observed1, 95);
            Assert.fail("alpha out of range, OutOfRangeException expected");
        } catch (OutOfRangeException ex) {
            
        }

        long[] tooShortObs = { 0 };
        double[] tooShortEx = { 1 };
        try {
            testStatistic.chiSquare(tooShortEx, tooShortObs);
            Assert.fail("arguments too short, DimensionMismatchException expected");
        } catch (DimensionMismatchException ex) {
            
        }

        
        long[] unMatchedObs = { 0, 1, 2, 3 };
        double[] unMatchedEx = { 1, 1, 2 };
        try {
            testStatistic.chiSquare(unMatchedEx, unMatchedObs);
            Assert.fail("arrays have different lengths, DimensionMismatchException expected");
        } catch (DimensionMismatchException ex) {
            
        }

        
        expected[0] = 0;
        try {
            testStatistic.chiSquareTest(expected, observed, .01);
            Assert.fail("bad expected count, NotStrictlyPositiveException expected");
        } catch (NotStrictlyPositiveException ex) {
            
        }

        
        expected[0] = 1;
        observed[0] = -1;
        try {
            testStatistic.chiSquareTest(expected, observed, .01);
            Assert.fail("bad expected count, NotPositiveException expected");
        } catch (NotPositiveException ex) {
            
        }

    }

// org.apache.commons.math3.stat.inference.ChiSquareTestTest::testChiSquareIndependence
    public void testChiSquareIndependence() throws Exception {

        

        long[][] counts = { {40, 22, 43}, {91, 21, 28}, {60, 10, 22}};
        Assert.assertEquals( "chi-square test statistic", 22.709027688, testStatistic.chiSquare(counts), 1E-9);
        Assert.assertEquals("chi-square p-value", 0.000144751460134, testStatistic.chiSquareTest(counts), 1E-9);
        Assert.assertTrue("chi-square test reject", testStatistic.chiSquareTest(counts, 0.0002));
        Assert.assertTrue("chi-square test accept", !testStatistic.chiSquareTest(counts, 0.0001));

        long[][] counts2 = {{10, 15}, {30, 40}, {60, 90} };
        Assert.assertEquals( "chi-square test statistic", 0.168965517241, testStatistic.chiSquare(counts2), 1E-9);
        Assert.assertEquals("chi-square p-value",0.918987499852, testStatistic.chiSquareTest(counts2), 1E-9);
        Assert.assertTrue("chi-square test accept", !testStatistic.chiSquareTest(counts2, 0.1));

        
        long[][] counts3 = { {40, 22, 43}, {91, 21, 28}, {60, 10}};
        try {
            testStatistic.chiSquare(counts3);
            Assert.fail("Expecting DimensionMismatchException");
        } catch (DimensionMismatchException ex) {
            
        }

        
        long[][] counts4 = {{40, 22, 43}};
        try {
            testStatistic.chiSquare(counts4);
            Assert.fail("Expecting DimensionMismatchException");
        } catch (DimensionMismatchException ex) {
            
        }
        long[][] counts5 = {{40}, {40}, {30}, {10}};
        try {
            testStatistic.chiSquare(counts5);
            Assert.fail("Expecting DimensionMismatchException");
        } catch (DimensionMismatchException ex) {
            
        }

        
        long[][] counts6 = {{10, -2}, {30, 40}, {60, 90} };
        try {
            testStatistic.chiSquare(counts6);
            Assert.fail("Expecting NotPositiveException");
        } catch (NotPositiveException ex) {
            
        }

        
        try {
            testStatistic.chiSquareTest(counts, 0);
            Assert.fail("Expecting OutOfRangeException");
        } catch (OutOfRangeException ex) {
            
        }
    }

// org.apache.commons.math3.stat.inference.ChiSquareTestTest::testChiSquareLargeTestStatistic
    public void testChiSquareLargeTestStatistic() throws Exception {
        double[] exp = new double[] {
            3389119.5, 649136.6, 285745.4, 25357364.76, 11291189.78, 543628.0,
            232921.0, 437665.75
        };

        long[] obs = new long[] {
            2372383, 584222, 257170, 17750155, 7903832, 489265, 209628, 393899
        };
        org.apache.commons.math3.stat.inference.ChiSquareTest csti =
            new org.apache.commons.math3.stat.inference.ChiSquareTest();
        double cst = csti.chiSquareTest(exp, obs);
        Assert.assertEquals("chi-square p-value", 0.0, cst, 1E-3);
        Assert.assertEquals( "chi-square test statistic",
                114875.90421929007, testStatistic.chiSquare(exp, obs), 1E-9);
    }

// org.apache.commons.math3.stat.inference.ChiSquareTestTest::testChiSquareZeroCount
    public void testChiSquareZeroCount() throws Exception {
        
        long[][] counts = { {40, 0, 4}, {91, 1, 2}, {60, 2, 0}};
        Assert.assertEquals( "chi-square test statistic", 9.67444662263,
                testStatistic.chiSquare(counts), 1E-9);
        Assert.assertEquals("chi-square p-value", 0.0462835770603,
                testStatistic.chiSquareTest(counts), 1E-9);
    }

// org.apache.commons.math3.stat.inference.ChiSquareTestTest::testChiSquareDataSetsComparisonEqualCounts
    public void testChiSquareDataSetsComparisonEqualCounts()
        throws Exception {
        long[] observed1 = {10, 12, 12, 10};
        long[] observed2 = {5, 15, 14, 10};
        Assert.assertEquals("chi-square p value", 0.541096,
                testStatistic.chiSquareTestDataSetsComparison(
                observed1, observed2), 1E-6);
        Assert.assertEquals("chi-square test statistic", 2.153846,
                testStatistic.chiSquareDataSetsComparison(
                observed1, observed2), 1E-6);
        Assert.assertFalse("chi-square test result",
                testStatistic.chiSquareTestDataSetsComparison(
                observed1, observed2, 0.4));
    }

// org.apache.commons.math3.stat.inference.ChiSquareTestTest::testChiSquareDataSetsComparisonUnEqualCounts
    public void testChiSquareDataSetsComparisonUnEqualCounts()
        throws Exception {
        long[] observed1 = {10, 12, 12, 10, 15};
        long[] observed2 = {15, 10, 10, 15, 5};
        Assert.assertEquals("chi-square p value", 0.124115,
                testStatistic.chiSquareTestDataSetsComparison(
                observed1, observed2), 1E-6);
        Assert.assertEquals("chi-square test statistic", 7.232189,
                testStatistic.chiSquareDataSetsComparison(
                observed1, observed2), 1E-6);
        Assert.assertTrue("chi-square test result",
                testStatistic.chiSquareTestDataSetsComparison(
                observed1, observed2, 0.13));
        Assert.assertFalse("chi-square test result",
                testStatistic.chiSquareTestDataSetsComparison(
                observed1, observed2, 0.12));
    }

// org.apache.commons.math3.stat.inference.ChiSquareTestTest::testChiSquareDataSetsComparisonBadCounts
    public void testChiSquareDataSetsComparisonBadCounts()
        throws Exception {
        long[] observed1 = {10, -1, 12, 10, 15};
        long[] observed2 = {15, 10, 10, 15, 5};
        try {
            testStatistic.chiSquareTestDataSetsComparison(
                    observed1, observed2);
            Assert.fail("Expecting NotPositiveException - negative count");
        } catch (NotPositiveException ex) {
            
        }
        long[] observed3 = {10, 0, 12, 10, 15};
        long[] observed4 = {15, 0, 10, 15, 5};
        try {
            testStatistic.chiSquareTestDataSetsComparison(
                    observed3, observed4);
            Assert.fail("Expecting ZeroException - double 0's");
        } catch (ZeroException ex) {
            
        }
        long[] observed5 = {10, 10, 12, 10, 15};
        long[] observed6 = {0, 0, 0, 0, 0};
        try {
            testStatistic.chiSquareTestDataSetsComparison(
                    observed5, observed6);
            Assert.fail("Expecting ZeroException - vanishing counts");
        } catch (ZeroException ex) {
            
        }
    }

// org.apache.commons.math3.stat.inference.MannWhitneyUTestTest::testMannWhitneyUSimple
    public void testMannWhitneyUSimple() throws Exception {
        
        final double x[] = {19, 22, 16, 29, 24};
        final double y[] = {20, 11, 17, 12};
        
        Assert.assertEquals(17, testStatistic.mannWhitneyU(x, y), 1e-10);
        Assert.assertEquals(0.08641, testStatistic.mannWhitneyUTest(x, y), 1e-5);
    }

// org.apache.commons.math3.stat.inference.MannWhitneyUTestTest::testMannWhitneyUInputValidation
    public void testMannWhitneyUInputValidation() throws Exception {
        
        try {
            testStatistic.mannWhitneyUTest(new double[] { }, new double[] { 1.0 });
            Assert.fail("x does not contain samples (exact), NoDataException expected");
        } catch (NoDataException ex) {
            
        }

        try {
            testStatistic.mannWhitneyUTest(new double[] { 1.0 }, new double[] { });
            Assert.fail("y does not contain samples (exact), NoDataException expected");
        } catch (NoDataException ex) {
            
        }

        
        try {
            testStatistic.mannWhitneyUTest(null, null);
            Assert.fail("x and y is null (exact), NullArgumentException expected");
        } catch (NullArgumentException ex) {
            
        }
        
        try {
            testStatistic.mannWhitneyUTest(null, null);
            Assert.fail("x and y is null (asymptotic), NullArgumentException expected");
        } catch (NullArgumentException ex) {
            
        }
        
        
        try {
            testStatistic.mannWhitneyUTest(null, new double[] { 1.0 });
            Assert.fail("x is null (exact), NullArgumentException expected");
        } catch (NullArgumentException ex) {
            
        }
        
        try {
            testStatistic.mannWhitneyUTest(new double[] { 1.0 }, null);
            Assert.fail("y is null (exact), NullArgumentException expected");
        } catch (NullArgumentException ex) {
            
        }
    }

// org.apache.commons.math3.stat.inference.OneWayAnovaTest::testAnovaFValue
    public void testAnovaFValue() throws Exception {
        
        List<double[]> threeClasses = new ArrayList<double[]>();
        threeClasses.add(classA);
        threeClasses.add(classB);
        threeClasses.add(classC);

        Assert.assertEquals("ANOVA F-value",  24.67361709460624,
                 testStatistic.anovaFValue(threeClasses), 1E-12);

        List<double[]> twoClasses = new ArrayList<double[]>();
        twoClasses.add(classA);
        twoClasses.add(classB);

        Assert.assertEquals("ANOVA F-value",  0.0150579150579,
                 testStatistic.anovaFValue(twoClasses), 1E-12);

        List<double[]> emptyContents = new ArrayList<double[]>();
        emptyContents.add(emptyArray);
        emptyContents.add(classC);
        try {
            testStatistic.anovaFValue(emptyContents);
            Assert.fail("empty array for key classX, MathIllegalArgumentException expected");
        } catch (MathIllegalArgumentException ex) {
            
        }

        List<double[]> tooFew = new ArrayList<double[]>();
        tooFew.add(classA);
        try {
            testStatistic.anovaFValue(tooFew);
            Assert.fail("less than two classes, MathIllegalArgumentException expected");
        } catch (MathIllegalArgumentException ex) {
            
        }
    }

// org.apache.commons.math3.stat.inference.OneWayAnovaTest::testAnovaPValue
    public void testAnovaPValue() throws Exception {
        
        List<double[]> threeClasses = new ArrayList<double[]>();
        threeClasses.add(classA);
        threeClasses.add(classB);
        threeClasses.add(classC);

        Assert.assertEquals("ANOVA P-value", 6.959446E-06,
                 testStatistic.anovaPValue(threeClasses), 1E-12);

        List<double[]> twoClasses = new ArrayList<double[]>();
        twoClasses.add(classA);
        twoClasses.add(classB);

        Assert.assertEquals("ANOVA P-value",  0.904212960464,
                 testStatistic.anovaPValue(twoClasses), 1E-12);

    }

// org.apache.commons.math3.stat.inference.OneWayAnovaTest::testAnovaTest
    public void testAnovaTest() throws Exception {
        
        List<double[]> threeClasses = new ArrayList<double[]>();
        threeClasses.add(classA);
        threeClasses.add(classB);
        threeClasses.add(classC);

        Assert.assertTrue("ANOVA Test P<0.01", testStatistic.anovaTest(threeClasses, 0.01));

        List<double[]> twoClasses = new ArrayList<double[]>();
        twoClasses.add(classA);
        twoClasses.add(classB);

        Assert.assertFalse("ANOVA Test P>0.01", testStatistic.anovaTest(twoClasses, 0.01));
    }

// org.apache.commons.math3.stat.inference.TTestTest::testOneSampleT
    public void testOneSampleT() throws Exception {
        double[] observed =
            {93.0, 103.0, 95.0, 101.0, 91.0, 105.0, 96.0, 94.0, 101.0,  88.0, 98.0, 94.0, 101.0, 92.0, 95.0 };
        double mu = 100.0;
        SummaryStatistics sampleStats = null;
        sampleStats = new SummaryStatistics();
        for (int i = 0; i < observed.length; i++) {
            sampleStats.addValue(observed[i]);
        }

        
        Assert.assertEquals("t statistic",  -2.81976445346,
                testStatistic.t(mu, observed), 10E-10);
        Assert.assertEquals("t statistic",  -2.81976445346,
                testStatistic.t(mu, sampleStats), 10E-10);
        Assert.assertEquals("p value", 0.0136390585873,
                testStatistic.tTest(mu, observed), 10E-10);
        Assert.assertEquals("p value", 0.0136390585873,
                testStatistic.tTest(mu, sampleStats), 10E-10);

        try {
            testStatistic.t(mu, (double[]) null);
            Assert.fail("arguments too short, NullArgumentException expected");
        } catch (NullArgumentException ex) {
            
        }

        try {
            testStatistic.t(mu, (SummaryStatistics) null);
            Assert.fail("arguments too short, NullArgumentException expected");
        } catch (NullArgumentException ex) {
            
        }

        try {
            testStatistic.t(mu, emptyObs);
            Assert.fail("arguments too short, NumberIsTooSmallException expected");
        } catch (NumberIsTooSmallException ex) {
            
        }

        try {
            testStatistic.t(mu, emptyStats);
            Assert.fail("arguments too short, NumberIsTooSmallException expected");
        } catch (NumberIsTooSmallException ex) {
            
        }

        try {
            testStatistic.t(mu, tooShortObs);
            Assert.fail("insufficient data to compute t statistic, NumberIsTooSmallException expected");
        } catch (NumberIsTooSmallException ex) {
            
        }
        try {
            testStatistic.tTest(mu, tooShortObs);
            Assert.fail("insufficient data to perform t test, NumberIsTooSmallException expected");
        } catch (NumberIsTooSmallException ex) {
           
        }

        try {
            testStatistic.t(mu, tooShortStats);
            Assert.fail("insufficient data to compute t statistic, NumberIsTooSmallException expected");
        } catch (NumberIsTooSmallException ex) {
            
        }
        try {
            testStatistic.tTest(mu, tooShortStats);
            Assert.fail("insufficient data to perform t test, NumberIsTooSmallException expected");
        } catch (NumberIsTooSmallException ex) {
            
        }
    }

// org.apache.commons.math3.stat.inference.TTestTest::testOneSampleTTest
    public void testOneSampleTTest() throws Exception {
        double[] oneSidedP =
            {2d, 0d, 6d, 6d, 3d, 3d, 2d, 3d, -6d, 6d, 6d, 6d, 3d, 0d, 1d, 1d, 0d, 2d, 3d, 3d };
        SummaryStatistics oneSidedPStats = new SummaryStatistics();
        for (int i = 0; i < oneSidedP.length; i++) {
            oneSidedPStats.addValue(oneSidedP[i]);
        }
        
        Assert.assertEquals("one sample t stat", 3.86485535541,
                testStatistic.t(0d, oneSidedP), 10E-10);
        Assert.assertEquals("one sample t stat", 3.86485535541,
                testStatistic.t(0d, oneSidedPStats),1E-10);
        Assert.assertEquals("one sample p value", 0.000521637019637,
                testStatistic.tTest(0d, oneSidedP) / 2d, 10E-10);
        Assert.assertEquals("one sample p value", 0.000521637019637,
                testStatistic.tTest(0d, oneSidedPStats) / 2d, 10E-5);
        Assert.assertTrue("one sample t-test reject", testStatistic.tTest(0d, oneSidedP, 0.01));
        Assert.assertTrue("one sample t-test reject", testStatistic.tTest(0d, oneSidedPStats, 0.01));
        Assert.assertTrue("one sample t-test accept", !testStatistic.tTest(0d, oneSidedP, 0.0001));
        Assert.assertTrue("one sample t-test accept", !testStatistic.tTest(0d, oneSidedPStats, 0.0001));

        try {
            testStatistic.tTest(0d, oneSidedP, 95);
            Assert.fail("alpha out of range, OutOfRangeException expected");
        } catch (OutOfRangeException ex) {
            
        }

        try {
            testStatistic.tTest(0d, oneSidedPStats, 95);
            Assert.fail("alpha out of range, OutOfRangeException expected");
        } catch (OutOfRangeException ex) {
            
        }

    }

// org.apache.commons.math3.stat.inference.TTestTest::testTwoSampleTHeterscedastic
    public void testTwoSampleTHeterscedastic() throws Exception {
        double[] sample1 = { 7d, -4d, 18d, 17d, -3d, -5d, 1d, 10d, 11d, -2d };
        double[] sample2 = { -1d, 12d, -1d, -3d, 3d, -5d, 5d, 2d, -11d, -1d, -3d };
        SummaryStatistics sampleStats1 = new SummaryStatistics();
        for (int i = 0; i < sample1.length; i++) {
            sampleStats1.addValue(sample1[i]);
        }
        SummaryStatistics sampleStats2 = new SummaryStatistics();
        for (int i = 0; i < sample2.length; i++) {
            sampleStats2.addValue(sample2[i]);
        }

        
        Assert.assertEquals("two sample heteroscedastic t stat", 1.60371728768,
                testStatistic.t(sample1, sample2), 1E-10);
        Assert.assertEquals("two sample heteroscedastic t stat", 1.60371728768,
                testStatistic.t(sampleStats1, sampleStats2), 1E-10);
        Assert.assertEquals("two sample heteroscedastic p value", 0.128839369622,
                testStatistic.tTest(sample1, sample2), 1E-10);
        Assert.assertEquals("two sample heteroscedastic p value", 0.128839369622,
                testStatistic.tTest(sampleStats1, sampleStats2), 1E-10);
        Assert.assertTrue("two sample heteroscedastic t-test reject",
                testStatistic.tTest(sample1, sample2, 0.2));
        Assert.assertTrue("two sample heteroscedastic t-test reject",
                testStatistic.tTest(sampleStats1, sampleStats2, 0.2));
        Assert.assertTrue("two sample heteroscedastic t-test accept",
                !testStatistic.tTest(sample1, sample2, 0.1));
        Assert.assertTrue("two sample heteroscedastic t-test accept",
                !testStatistic.tTest(sampleStats1, sampleStats2, 0.1));

        try {
            testStatistic.tTest(sample1, sample2, .95);
            Assert.fail("alpha out of range, OutOfRangeException expected");
        } catch (OutOfRangeException ex) {
            
        }

        try {
            testStatistic.tTest(sampleStats1, sampleStats2, .95);
            Assert.fail("alpha out of range, OutOfRangeException expected");
        } catch (OutOfRangeException ex) {
            
        }

        try {
            testStatistic.tTest(sample1, tooShortObs, .01);
            Assert.fail("insufficient data, NumberIsTooSmallException expected");
        } catch (NumberIsTooSmallException ex) {
            
        }

        try {
            testStatistic.tTest(sampleStats1, tooShortStats, .01);
            Assert.fail("insufficient data, NumberIsTooSmallException expected");
        } catch (NumberIsTooSmallException ex) {
            
        }

        try {
            testStatistic.tTest(sample1, tooShortObs);
            Assert.fail("insufficient data, NumberIsTooSmallException expected");
        } catch (NumberIsTooSmallException ex) {
           
        }

        try {
            testStatistic.tTest(sampleStats1, tooShortStats);
            Assert.fail("insufficient data, NumberIsTooSmallException expected");
        } catch (NumberIsTooSmallException ex) {
            
        }

        try {
            testStatistic.t(sample1, tooShortObs);
            Assert.fail("insufficient data, NumberIsTooSmallException expected");
        } catch (NumberIsTooSmallException ex) {
            
        }

        try {
            testStatistic.t(sampleStats1, tooShortStats);
            Assert.fail("insufficient data, NumberIsTooSmallException expected");
        } catch (NumberIsTooSmallException ex) {
           
        }
    }

// org.apache.commons.math3.stat.inference.TTestTest::testTwoSampleTHomoscedastic
    public void testTwoSampleTHomoscedastic() throws Exception {
        double[] sample1 ={2, 4, 6, 8, 10, 97};
        double[] sample2 = {4, 6, 8, 10, 16};
        SummaryStatistics sampleStats1 = new SummaryStatistics();
        for (int i = 0; i < sample1.length; i++) {
            sampleStats1.addValue(sample1[i]);
        }
        SummaryStatistics sampleStats2 = new SummaryStatistics();
        for (int i = 0; i < sample2.length; i++) {
            sampleStats2.addValue(sample2[i]);
        }

        
        Assert.assertEquals("two sample homoscedastic t stat", 0.73096310086,
              testStatistic.homoscedasticT(sample1, sample2), 10E-11);
        Assert.assertEquals("two sample homoscedastic p value", 0.4833963785,
                testStatistic.homoscedasticTTest(sampleStats1, sampleStats2), 1E-10);
        Assert.assertTrue("two sample homoscedastic t-test reject",
                testStatistic.homoscedasticTTest(sample1, sample2, 0.49));
        Assert.assertTrue("two sample homoscedastic t-test accept",
                !testStatistic.homoscedasticTTest(sample1, sample2, 0.48));
    }

// org.apache.commons.math3.stat.inference.TTestTest::testSmallSamples
    public void testSmallSamples() throws Exception {
        double[] sample1 = {1d, 3d};
        double[] sample2 = {4d, 5d};

        
        Assert.assertEquals(-2.2360679775, testStatistic.t(sample1, sample2),
                1E-10);
        Assert.assertEquals(0.198727388935, testStatistic.tTest(sample1, sample2),
                1E-10);
    }

// org.apache.commons.math3.stat.inference.TTestTest::testPaired
    public void testPaired() throws Exception {
        double[] sample1 = {1d, 3d, 5d, 7d};
        double[] sample2 = {0d, 6d, 11d, 2d};
        double[] sample3 = {5d, 7d, 8d, 10d};

        
        Assert.assertEquals(-0.3133, testStatistic.pairedT(sample1, sample2), 1E-4);
        Assert.assertEquals(0.774544295819, testStatistic.pairedTTest(sample1, sample2), 1E-10);
        Assert.assertEquals(0.001208, testStatistic.pairedTTest(sample1, sample3), 1E-6);
        Assert.assertFalse(testStatistic.pairedTTest(sample1, sample3, .001));
        Assert.assertTrue(testStatistic.pairedTTest(sample1, sample3, .002));
    }

// org.apache.commons.math3.stat.inference.TestUtilsTest::testChiSquare
    public void testChiSquare() throws Exception {

        
        
        
        

        long[] observed = {10, 9, 11};
        double[] expected = {10, 10, 10};
        Assert.assertEquals("chi-square statistic", 0.2,  TestUtils.chiSquare(expected, observed), 10E-12);
        Assert.assertEquals("chi-square p-value", 0.904837418036, TestUtils.chiSquareTest(expected, observed), 1E-10);

        long[] observed1 = { 500, 623, 72, 70, 31 };
        double[] expected1 = { 485, 541, 82, 61, 37 };
        Assert.assertEquals( "chi-square test statistic", 9.023307936427388, TestUtils.chiSquare(expected1, observed1), 1E-10);
        Assert.assertEquals("chi-square p-value", 0.06051952647453607, TestUtils.chiSquareTest(expected1, observed1), 1E-9);
        Assert.assertTrue("chi-square test reject", TestUtils.chiSquareTest(expected1, observed1, 0.07));
        Assert.assertTrue("chi-square test accept", !TestUtils.chiSquareTest(expected1, observed1, 0.05));

        try {
            TestUtils.chiSquareTest(expected1, observed1, 95);
            Assert.fail("alpha out of range, OutOfRangeException expected");
        } catch (OutOfRangeException ex) {
            
        }

        long[] tooShortObs = { 0 };
        double[] tooShortEx = { 1 };
        try {
            TestUtils.chiSquare(tooShortEx, tooShortObs);
            Assert.fail("arguments too short, DimensionMismatchException expected");
        } catch (DimensionMismatchException ex) {
            
        }

        
        long[] unMatchedObs = { 0, 1, 2, 3 };
        double[] unMatchedEx = { 1, 1, 2 };
        try {
            TestUtils.chiSquare(unMatchedEx, unMatchedObs);
            Assert.fail("arrays have different lengths, DimensionMismatchException expected");
        } catch (DimensionMismatchException ex) {
            
        }

        
        expected[0] = 0;
        try {
            TestUtils.chiSquareTest(expected, observed, .01);
            Assert.fail("bad expected count, NotStrictlyPositiveException expected");
        } catch (NotStrictlyPositiveException ex) {
            
        }

        
        expected[0] = 1;
        observed[0] = -1;
        try {
            TestUtils.chiSquareTest(expected, observed, .01);
            Assert.fail("bad expected count, NotPositiveException expected");
        } catch (NotPositiveException ex) {
            
        }

    }

// org.apache.commons.math3.stat.inference.TestUtilsTest::testChiSquareIndependence
    public void testChiSquareIndependence() throws Exception {

        

        long[][] counts = { {40, 22, 43}, {91, 21, 28}, {60, 10, 22}};
        Assert.assertEquals( "chi-square test statistic", 22.709027688, TestUtils.chiSquare(counts), 1E-9);
        Assert.assertEquals("chi-square p-value", 0.000144751460134, TestUtils.chiSquareTest(counts), 1E-9);
        Assert.assertTrue("chi-square test reject", TestUtils.chiSquareTest(counts, 0.0002));
        Assert.assertTrue("chi-square test accept", !TestUtils.chiSquareTest(counts, 0.0001));

        long[][] counts2 = {{10, 15}, {30, 40}, {60, 90} };
        Assert.assertEquals( "chi-square test statistic", 0.168965517241, TestUtils.chiSquare(counts2), 1E-9);
        Assert.assertEquals("chi-square p-value",0.918987499852, TestUtils.chiSquareTest(counts2), 1E-9);
        Assert.assertTrue("chi-square test accept", !TestUtils.chiSquareTest(counts2, 0.1));

        
        long[][] counts3 = { {40, 22, 43}, {91, 21, 28}, {60, 10}};
        try {
            TestUtils.chiSquare(counts3);
            Assert.fail("Expecting DimensionMismatchException");
        } catch (DimensionMismatchException ex) {
            
        }

        
        long[][] counts4 = {{40, 22, 43}};
        try {
            TestUtils.chiSquare(counts4);
            Assert.fail("Expecting DimensionMismatchException");
        } catch (DimensionMismatchException ex) {
            
        }
        long[][] counts5 = {{40}, {40}, {30}, {10}};
        try {
            TestUtils.chiSquare(counts5);
            Assert.fail("Expecting DimensionMismatchException");
        } catch (DimensionMismatchException ex) {
            
        }

        
        long[][] counts6 = {{10, -2}, {30, 40}, {60, 90} };
        try {
            TestUtils.chiSquare(counts6);
            Assert.fail("Expecting NotPositiveException");
        } catch (NotPositiveException ex) {
            
        }

        
        try {
            TestUtils.chiSquareTest(counts, 0);
            Assert.fail("Expecting OutOfRangeException");
        } catch (OutOfRangeException ex) {
            
        }
    }

// org.apache.commons.math3.stat.inference.TestUtilsTest::testChiSquareLargeTestStatistic
    public void testChiSquareLargeTestStatistic() throws Exception {
        double[] exp = new double[] {
                3389119.5, 649136.6, 285745.4, 25357364.76, 11291189.78, 543628.0,
                232921.0, 437665.75
        };

        long[] obs = new long[] {
                2372383, 584222, 257170, 17750155, 7903832, 489265, 209628, 393899
        };
        org.apache.commons.math3.stat.inference.ChiSquareTest csti =
            new org.apache.commons.math3.stat.inference.ChiSquareTest();
        double cst = csti.chiSquareTest(exp, obs);
        Assert.assertEquals("chi-square p-value", 0.0, cst, 1E-3);
        Assert.assertEquals( "chi-square test statistic",
                114875.90421929007, TestUtils.chiSquare(exp, obs), 1E-9);
    }

// org.apache.commons.math3.stat.inference.TestUtilsTest::testChiSquareZeroCount
    public void testChiSquareZeroCount() throws Exception {
        
        long[][] counts = { {40, 0, 4}, {91, 1, 2}, {60, 2, 0}};
        Assert.assertEquals( "chi-square test statistic", 9.67444662263,
                TestUtils.chiSquare(counts), 1E-9);
        Assert.assertEquals("chi-square p-value", 0.0462835770603,
                TestUtils.chiSquareTest(counts), 1E-9);
    }

// org.apache.commons.math3.stat.inference.TestUtilsTest::testOneSampleT
    public void testOneSampleT() throws Exception {
        double[] observed =
            {93.0, 103.0, 95.0, 101.0, 91.0, 105.0, 96.0, 94.0, 101.0,  88.0, 98.0, 94.0, 101.0, 92.0, 95.0 };
        double mu = 100.0;
        SummaryStatistics sampleStats = null;
        sampleStats = new SummaryStatistics();
        for (int i = 0; i < observed.length; i++) {
            sampleStats.addValue(observed[i]);
        }

        
        Assert.assertEquals("t statistic",  -2.81976445346,
                TestUtils.t(mu, observed), 10E-10);
        Assert.assertEquals("t statistic",  -2.81976445346,
                TestUtils.t(mu, sampleStats), 10E-10);
        Assert.assertEquals("p value", 0.0136390585873,
                TestUtils.tTest(mu, observed), 10E-10);
        Assert.assertEquals("p value", 0.0136390585873,
                TestUtils.tTest(mu, sampleStats), 10E-10);

        try {
            TestUtils.t(mu, (double[]) null);
            Assert.fail("arguments too short, NullArgumentException expected");
        } catch (NullArgumentException ex) {
            
        }

        try {
            TestUtils.t(mu, (SummaryStatistics) null);
            Assert.fail("arguments too short, NullArgumentException expected");
        } catch (NullArgumentException ex) {
            
        }

        try {
            TestUtils.t(mu, emptyObs);
            Assert.fail("arguments too short, NumberIsTooSmallException expected");
        } catch (NumberIsTooSmallException ex) {
            
        }

        try {
            TestUtils.t(mu, emptyStats);
            Assert.fail("arguments too short, NumberIsTooSmallException expected");
        } catch (NumberIsTooSmallException ex) {
            
        }

        try {
            TestUtils.t(mu, tooShortObs);
            Assert.fail("insufficient data to compute t statistic, NumberIsTooSmallException expected");
        } catch (NumberIsTooSmallException ex) {
            
        }
        try {
            TestUtils.tTest(mu, tooShortObs);
            Assert.fail("insufficient data to perform t test, NumberIsTooSmallException expected");
        } catch (NumberIsTooSmallException ex) {
            
        }

        try {
            TestUtils.t(mu, (SummaryStatistics) null);
            Assert.fail("insufficient data to compute t statistic, NullArgumentException expected");
        } catch (NullArgumentException ex) {
            
        }
        try {
            TestUtils.tTest(mu, (SummaryStatistics) null);
            Assert.fail("insufficient data to perform t test, NullArgumentException expected");
        } catch (NullArgumentException ex) {
            
        }
    }

// org.apache.commons.math3.stat.inference.TestUtilsTest::testOneSampleTTest
    public void testOneSampleTTest() throws Exception {
        double[] oneSidedP =
            {2d, 0d, 6d, 6d, 3d, 3d, 2d, 3d, -6d, 6d, 6d, 6d, 3d, 0d, 1d, 1d, 0d, 2d, 3d, 3d };
        SummaryStatistics oneSidedPStats = new SummaryStatistics();
        for (int i = 0; i < oneSidedP.length; i++) {
            oneSidedPStats.addValue(oneSidedP[i]);
        }
        
        Assert.assertEquals("one sample t stat", 3.86485535541,
                TestUtils.t(0d, oneSidedP), 10E-10);
        Assert.assertEquals("one sample t stat", 3.86485535541,
                TestUtils.t(0d, oneSidedPStats),1E-10);
        Assert.assertEquals("one sample p value", 0.000521637019637,
                TestUtils.tTest(0d, oneSidedP) / 2d, 10E-10);
        Assert.assertEquals("one sample p value", 0.000521637019637,
                TestUtils.tTest(0d, oneSidedPStats) / 2d, 10E-5);
        Assert.assertTrue("one sample t-test reject", TestUtils.tTest(0d, oneSidedP, 0.01));
        Assert.assertTrue("one sample t-test reject", TestUtils.tTest(0d, oneSidedPStats, 0.01));
        Assert.assertTrue("one sample t-test accept", !TestUtils.tTest(0d, oneSidedP, 0.0001));
        Assert.assertTrue("one sample t-test accept", !TestUtils.tTest(0d, oneSidedPStats, 0.0001));

        try {
            TestUtils.tTest(0d, oneSidedP, 95);
            Assert.fail("alpha out of range, OutOfRangeException expected");
        } catch (OutOfRangeException ex) {
            
        }

        try {
            TestUtils.tTest(0d, oneSidedPStats, 95);
            Assert.fail("alpha out of range, OutOfRangeException expected");
        } catch (OutOfRangeException ex) {
            
        }

    }

// org.apache.commons.math3.stat.inference.TestUtilsTest::testTwoSampleTHeterscedastic
    public void testTwoSampleTHeterscedastic() throws Exception {
        double[] sample1 = { 7d, -4d, 18d, 17d, -3d, -5d, 1d, 10d, 11d, -2d };
        double[] sample2 = { -1d, 12d, -1d, -3d, 3d, -5d, 5d, 2d, -11d, -1d, -3d };
        SummaryStatistics sampleStats1 = new SummaryStatistics();
        for (int i = 0; i < sample1.length; i++) {
            sampleStats1.addValue(sample1[i]);
        }
        SummaryStatistics sampleStats2 = new SummaryStatistics();
        for (int i = 0; i < sample2.length; i++) {
            sampleStats2.addValue(sample2[i]);
        }

        
        Assert.assertEquals("two sample heteroscedastic t stat", 1.60371728768,
                TestUtils.t(sample1, sample2), 1E-10);
        Assert.assertEquals("two sample heteroscedastic t stat", 1.60371728768,
                TestUtils.t(sampleStats1, sampleStats2), 1E-10);
        Assert.assertEquals("two sample heteroscedastic p value", 0.128839369622,
                TestUtils.tTest(sample1, sample2), 1E-10);
        Assert.assertEquals("two sample heteroscedastic p value", 0.128839369622,
                TestUtils.tTest(sampleStats1, sampleStats2), 1E-10);
        Assert.assertTrue("two sample heteroscedastic t-test reject",
                TestUtils.tTest(sample1, sample2, 0.2));
        Assert.assertTrue("two sample heteroscedastic t-test reject",
                TestUtils.tTest(sampleStats1, sampleStats2, 0.2));
        Assert.assertTrue("two sample heteroscedastic t-test accept",
                !TestUtils.tTest(sample1, sample2, 0.1));
        Assert.assertTrue("two sample heteroscedastic t-test accept",
                !TestUtils.tTest(sampleStats1, sampleStats2, 0.1));

        try {
            TestUtils.tTest(sample1, sample2, .95);
            Assert.fail("alpha out of range, OutOfRangeException expected");
        } catch (OutOfRangeException ex) {
            
        }

        try {
            TestUtils.tTest(sampleStats1, sampleStats2, .95);
            Assert.fail("alpha out of range, OutOfRangeException expected");
        } catch (OutOfRangeException ex) {
            
        }

        try {
            TestUtils.tTest(sample1, tooShortObs, .01);
            Assert.fail("insufficient data, NumberIsTooSmallException expected");
        } catch (NumberIsTooSmallException ex) {
            
        }

        try {
            TestUtils.tTest(sampleStats1, (SummaryStatistics) null, .01);
            Assert.fail("insufficient data, NullArgumentException expected");
        } catch (NullArgumentException ex) {
            
        }

        try {
            TestUtils.tTest(sample1, tooShortObs);
            Assert.fail("insufficient data, NumberIsTooSmallException expected");
        } catch (NumberIsTooSmallException ex) {
            
        }

        try {
            TestUtils.tTest(sampleStats1, (SummaryStatistics) null);
            Assert.fail("insufficient data, NullArgumentException expected");
        } catch (NullArgumentException ex) {
            
        }

        try {
            TestUtils.t(sample1, tooShortObs);
            Assert.fail("insufficient data, NumberIsTooSmallException expected");
        } catch (NumberIsTooSmallException ex) {
            
        }

        try {
            TestUtils.t(sampleStats1, (SummaryStatistics) null);
            Assert.fail("insufficient data, NullArgumentException expected");
        } catch (NullArgumentException ex) {
            
        }
    }

// org.apache.commons.math3.stat.inference.TestUtilsTest::testTwoSampleTHomoscedastic
    public void testTwoSampleTHomoscedastic() throws Exception {
        double[] sample1 ={2, 4, 6, 8, 10, 97};
        double[] sample2 = {4, 6, 8, 10, 16};
        SummaryStatistics sampleStats1 = new SummaryStatistics();
        for (int i = 0; i < sample1.length; i++) {
            sampleStats1.addValue(sample1[i]);
        }
        SummaryStatistics sampleStats2 = new SummaryStatistics();
        for (int i = 0; i < sample2.length; i++) {
            sampleStats2.addValue(sample2[i]);
        }

        
        Assert.assertEquals("two sample homoscedastic t stat", 0.73096310086,
                TestUtils.homoscedasticT(sample1, sample2), 10E-11);
        Assert.assertEquals("two sample homoscedastic p value", 0.4833963785,
                TestUtils.homoscedasticTTest(sampleStats1, sampleStats2), 1E-10);
        Assert.assertTrue("two sample homoscedastic t-test reject",
                TestUtils.homoscedasticTTest(sample1, sample2, 0.49));
        Assert.assertTrue("two sample homoscedastic t-test accept",
                !TestUtils.homoscedasticTTest(sample1, sample2, 0.48));
    }

// org.apache.commons.math3.stat.inference.TestUtilsTest::testSmallSamples
    public void testSmallSamples() throws Exception {
        double[] sample1 = {1d, 3d};
        double[] sample2 = {4d, 5d};

        
        Assert.assertEquals(-2.2360679775, TestUtils.t(sample1, sample2),
                1E-10);
        Assert.assertEquals(0.198727388935, TestUtils.tTest(sample1, sample2),
                1E-10);
    }

// org.apache.commons.math3.stat.inference.TestUtilsTest::testPaired
    public void testPaired() throws Exception {
        double[] sample1 = {1d, 3d, 5d, 7d};
        double[] sample2 = {0d, 6d, 11d, 2d};
        double[] sample3 = {5d, 7d, 8d, 10d};

        
        Assert.assertEquals(-0.3133, TestUtils.pairedT(sample1, sample2), 1E-4);
        Assert.assertEquals(0.774544295819, TestUtils.pairedTTest(sample1, sample2), 1E-10);
        Assert.assertEquals(0.001208, TestUtils.pairedTTest(sample1, sample3), 1E-6);
        Assert.assertFalse(TestUtils.pairedTTest(sample1, sample3, .001));
        Assert.assertTrue(TestUtils.pairedTTest(sample1, sample3, .002));
    }

// org.apache.commons.math3.stat.inference.TestUtilsTest::testOneWayAnovaUtils
    public void testOneWayAnovaUtils() throws Exception {
        classes.add(classA);
        classes.add(classB);
        classes.add(classC);
        Assert.assertEquals(oneWayAnova.anovaFValue(classes),
                TestUtils.oneWayAnovaFValue(classes), 10E-12);
        Assert.assertEquals(oneWayAnova.anovaPValue(classes),
                TestUtils.oneWayAnovaPValue(classes), 10E-12);
        Assert.assertEquals(oneWayAnova.anovaTest(classes, 0.01),
                TestUtils.oneWayAnovaTest(classes, 0.01));
    }

// org.apache.commons.math3.stat.inference.WilcoxonSignedRankTestTest::testWilcoxonSignedRankSimple
    public void testWilcoxonSignedRankSimple() throws Exception {
        
        final double x[] = {1.83, 0.50, 1.62, 2.48, 1.68, 1.88, 1.55, 3.06, 1.30};
        final double y[] = {0.878, 0.647, 0.598, 2.05, 1.06, 1.29, 1.06, 3.14, 1.29};
        
        
        Assert.assertEquals(40, testStatistic.wilcoxonSignedRank(x, y), 1e-10);
        Assert.assertEquals(0.03906, testStatistic.wilcoxonSignedRankTest(x, y, true), 1e-5);        
        
        
        Assert.assertEquals(40, testStatistic.wilcoxonSignedRank(x, y), 1e-10);
        Assert.assertEquals(0.0329693812, testStatistic.wilcoxonSignedRankTest(x, y, false), 1e-10);
    }

// org.apache.commons.math3.stat.inference.WilcoxonSignedRankTestTest::testWilcoxonSignedRankInputValidation
    public void testWilcoxonSignedRankInputValidation() throws Exception {
        
        final double[] x1 = new double[30];
        final double[] x2 = new double[31];
        final double[] y1 = new double[30];
        final double[] y2 = new double[31];
        for (int i = 0; i < 30; ++i) {
            x1[i] = x2[i] = y1[i] = y2[i] = i;            
        }
        
        
        
        
        try {
            testStatistic.wilcoxonSignedRankTest(x2, y2, true);
            Assert.fail("More than 30 samples and exact chosen, NumberIsTooLargeException expected");
        } catch (NumberIsTooLargeException ex) {
            
        }
        
        
        try {
            testStatistic.wilcoxonSignedRankTest(new double[] { }, new double[] { 1.0 }, true);
            Assert.fail("x does not contain samples (exact), NoDataException expected");
        } catch (NoDataException ex) {
            
        }

        try {
            testStatistic.wilcoxonSignedRankTest(new double[] { }, new double[] { 1.0 }, false);
            Assert.fail("x does not contain samples (asymptotic), NoDataException expected");
        } catch (NoDataException ex) {
            
        }

        try {
            testStatistic.wilcoxonSignedRankTest(new double[] { 1.0 }, new double[] { }, true);
            Assert.fail("y does not contain samples (exact), NoDataException expected");
        } catch (NoDataException ex) {
            
        }

        try {
            testStatistic.wilcoxonSignedRankTest(new double[] { 1.0 }, new double[] { }, false);
            Assert.fail("y does not contain samples (asymptotic), NoDataException expected");
        } catch (NoDataException ex) {
            
        }

        
        try {
            testStatistic.wilcoxonSignedRankTest(new double[] { 1.0, 2.0 }, new double[] { 3.0 }, true);
            Assert.fail("x and y not same size (exact), DimensionMismatchException expected");
        } catch (DimensionMismatchException ex) {
            
        }

        try {
            testStatistic.wilcoxonSignedRankTest(new double[] { 1.0, 2.0 }, new double[] { 3.0 }, false);
            Assert.fail("x and y not same size (asymptotic), DimensionMismatchException expected");
        } catch (DimensionMismatchException ex) {
            
        }
        
        
        try {
            testStatistic.wilcoxonSignedRankTest(null, null, true);
            Assert.fail("x and y is null (exact), NullArgumentException expected");
        } catch (NullArgumentException ex) {
            
        }
        
        try {
            testStatistic.wilcoxonSignedRankTest(null, null, false);
            Assert.fail("x and y is null (asymptotic), NullArgumentException expected");
        } catch (NullArgumentException ex) {
            
        }
        
        
        try {
            testStatistic.wilcoxonSignedRankTest(null, new double[] { 1.0 }, true);
            Assert.fail("x is null (exact), NullArgumentException expected");
        } catch (NullArgumentException ex) {
            
        }
        
        try {
            testStatistic.wilcoxonSignedRankTest(null, new double[] { 1.0 }, false);
            Assert.fail("x is null (asymptotic), NullArgumentException expected");
        } catch (NullArgumentException ex) {
            
        }
        
        try {
            testStatistic.wilcoxonSignedRankTest(new double[] { 1.0 }, null, true);
            Assert.fail("y is null (exact), NullArgumentException expected");
        } catch (NullArgumentException ex) {
            
        }
        
        try {
            testStatistic.wilcoxonSignedRankTest(new double[] { 1.0 }, null, false);
            Assert.fail("y is null (asymptotic), NullArgumentException expected");
        } catch (NullArgumentException ex) {
            
        }
    }

// org.apache.commons.math3.stat.regression.SimpleRegressionTest::testRegressIfaceMethod
    public void testRegressIfaceMethod(){
        final SimpleRegression regression = new SimpleRegression(true);
        final UpdatingMultipleLinearRegression iface = regression;
        final SimpleRegression regressionNoint = new SimpleRegression( false );
        final SimpleRegression regressionIntOnly= new SimpleRegression( false );
        for (int i = 0; i < data.length; i++) {
            iface.addObservation( new double[]{data[i][1]}, data[i][0]);
            regressionNoint.addData(data[i][1], data[i][0]);
            regressionIntOnly.addData(1.0, data[i][0]);
        }

        
        final RegressionResults fullReg = iface.regress( );
        Assert.assertNotNull(fullReg);
        Assert.assertEquals("intercept", regression.getIntercept(), fullReg.getParameterEstimate(0), 1.0e-16);
        Assert.assertEquals("intercept std err",regression.getInterceptStdErr(), fullReg.getStdErrorOfEstimate(0),1.0E-16);
        Assert.assertEquals("slope", regression.getSlope(), fullReg.getParameterEstimate(1), 1.0e-16);
        Assert.assertEquals("slope std err",regression.getSlopeStdErr(), fullReg.getStdErrorOfEstimate(1),1.0E-16);
        Assert.assertEquals("number of observations",regression.getN(), fullReg.getN());
        Assert.assertEquals("r-square",regression.getRSquare(), fullReg.getRSquared(), 1.0E-16);
        Assert.assertEquals("SSR", regression.getRegressionSumSquares(), fullReg.getRegressionSumSquares() ,1.0E-16);
        Assert.assertEquals("MSE", regression.getMeanSquareError(), fullReg.getMeanSquareError() ,1.0E-16);
        Assert.assertEquals("SSE", regression.getSumSquaredErrors(), fullReg.getErrorSumSquares() ,1.0E-16);

        final RegressionResults noInt   = iface.regress( new int[]{1} );
        Assert.assertNotNull(noInt);
        Assert.assertEquals("slope", regressionNoint.getSlope(), noInt.getParameterEstimate(0), 1.0e-12);
        Assert.assertEquals("slope std err",regressionNoint.getSlopeStdErr(), noInt.getStdErrorOfEstimate(0),1.0E-16);
        Assert.assertEquals("number of observations",regressionNoint.getN(), noInt.getN());
        Assert.assertEquals("r-square",regressionNoint.getRSquare(), noInt.getRSquared(), 1.0E-16);
        Assert.assertEquals("SSR", regressionNoint.getRegressionSumSquares(), noInt.getRegressionSumSquares() ,1.0E-8);
        Assert.assertEquals("MSE", regressionNoint.getMeanSquareError(), noInt.getMeanSquareError() ,1.0E-16);
        Assert.assertEquals("SSE", regressionNoint.getSumSquaredErrors(), noInt.getErrorSumSquares() ,1.0E-16);

        final RegressionResults onlyInt = iface.regress( new int[]{0} );
        Assert.assertNotNull(onlyInt);
        Assert.assertEquals("slope", regressionIntOnly.getSlope(), onlyInt.getParameterEstimate(0), 1.0e-12);
        Assert.assertEquals("slope std err",regressionIntOnly.getSlopeStdErr(), onlyInt.getStdErrorOfEstimate(0),1.0E-12);
        Assert.assertEquals("number of observations",regressionIntOnly.getN(), onlyInt.getN());
        Assert.assertEquals("r-square",regressionIntOnly.getRSquare(), onlyInt.getRSquared(), 1.0E-14);
        Assert.assertEquals("SSE", regressionIntOnly.getSumSquaredErrors(), onlyInt.getErrorSumSquares() ,1.0E-8);
        Assert.assertEquals("SSR", regressionIntOnly.getRegressionSumSquares(), onlyInt.getRegressionSumSquares() ,1.0E-8);
        Assert.assertEquals("MSE", regressionIntOnly.getMeanSquareError(), onlyInt.getMeanSquareError() ,1.0E-8);

    }

// org.apache.commons.math3.stat.regression.SimpleRegressionTest::testNoInterceot_noint2
    public void testNoInterceot_noint2(){
         SimpleRegression regression = new SimpleRegression(false);
         regression.addData(noint2[0][1], noint2[0][0]);
         regression.addData(noint2[1][1], noint2[1][0]);
         regression.addData(noint2[2][1], noint2[2][0]);
         Assert.assertEquals("intercept", 0, regression.getIntercept(), 0);
         Assert.assertEquals("slope", 0.727272727272727,
                 regression.getSlope(), 10E-12);
         Assert.assertEquals("slope std err", 0.420827318078432E-01,
                regression.getSlopeStdErr(),10E-12);
        Assert.assertEquals("number of observations", 3, regression.getN());
        Assert.assertEquals("r-square", 0.993348115299335,
            regression.getRSquare(), 10E-12);
        Assert.assertEquals("SSR", 40.7272727272727,
            regression.getRegressionSumSquares(), 10E-9);
        Assert.assertEquals("MSE", 0.136363636363636,
            regression.getMeanSquareError(), 10E-10);
        Assert.assertEquals("SSE", 0.272727272727273,
            regression.getSumSquaredErrors(),10E-9);
    }

// org.apache.commons.math3.stat.regression.SimpleRegressionTest::testNoIntercept_noint1
    public void testNoIntercept_noint1(){
        SimpleRegression regression = new SimpleRegression(false);
        for (int i = 0; i < noint1.length; i++) {
            regression.addData(noint1[i][1], noint1[i][0]);
        }
        Assert.assertEquals("intercept", 0, regression.getIntercept(), 0);
        Assert.assertEquals("slope", 2.07438016528926, regression.getSlope(), 10E-12);
        Assert.assertEquals("slope std err", 0.165289256198347E-01,
                regression.getSlopeStdErr(),10E-12);
        Assert.assertEquals("number of observations", 11, regression.getN());
        Assert.assertEquals("r-square", 0.999365492298663,
            regression.getRSquare(), 10E-12);
        Assert.assertEquals("SSR", 200457.727272727,
            regression.getRegressionSumSquares(), 10E-9);
        Assert.assertEquals("MSE", 12.7272727272727,
            regression.getMeanSquareError(), 10E-10);
        Assert.assertEquals("SSE", 127.272727272727,
            regression.getSumSquaredErrors(),10E-9);

    }

// org.apache.commons.math3.stat.regression.SimpleRegressionTest::testNorris
    public void testNorris() {
        SimpleRegression regression = new SimpleRegression();
        for (int i = 0; i < data.length; i++) {
            regression.addData(data[i][1], data[i][0]);
        }
        
        
        Assert.assertEquals("slope", 1.00211681802045, regression.getSlope(), 10E-12);
        Assert.assertEquals("slope std err", 0.429796848199937E-03,
                regression.getSlopeStdErr(),10E-12);
        Assert.assertEquals("number of observations", 36, regression.getN());
        Assert.assertEquals( "intercept", -0.262323073774029,
            regression.getIntercept(),10E-12);
        Assert.assertEquals("std err intercept", 0.232818234301152,
            regression.getInterceptStdErr(),10E-12);
        Assert.assertEquals("r-square", 0.999993745883712,
            regression.getRSquare(), 10E-12);
        Assert.assertEquals("SSR", 4255954.13232369,
            regression.getRegressionSumSquares(), 10E-9);
        Assert.assertEquals("MSE", 0.782864662630069,
            regression.getMeanSquareError(), 10E-10);
        Assert.assertEquals("SSE", 26.6173985294224,
            regression.getSumSquaredErrors(),10E-9);
        

        Assert.assertEquals( "predict(0)",  -0.262323073774029,
            regression.predict(0), 10E-12);
        Assert.assertEquals("predict(1)", 1.00211681802045 - 0.262323073774029,
            regression.predict(1), 10E-12);
    }

// org.apache.commons.math3.stat.regression.SimpleRegressionTest::testCorr
    public void testCorr() {
        SimpleRegression regression = new SimpleRegression();
        regression.addData(corrData);
        Assert.assertEquals("number of observations", 17, regression.getN());
        Assert.assertEquals("r-square", .896123, regression.getRSquare(), 10E-6);
        Assert.assertEquals("r", -0.94663767742, regression.getR(), 1E-10);
    }

// org.apache.commons.math3.stat.regression.SimpleRegressionTest::testNaNs
    public void testNaNs() {
        SimpleRegression regression = new SimpleRegression();
        Assert.assertTrue("intercept not NaN", Double.isNaN(regression.getIntercept()));
        Assert.assertTrue("slope not NaN", Double.isNaN(regression.getSlope()));
        Assert.assertTrue("slope std err not NaN", Double.isNaN(regression.getSlopeStdErr()));
        Assert.assertTrue("intercept std err not NaN", Double.isNaN(regression.getInterceptStdErr()));
        Assert.assertTrue("MSE not NaN", Double.isNaN(regression.getMeanSquareError()));
        Assert.assertTrue("e not NaN", Double.isNaN(regression.getR()));
        Assert.assertTrue("r-square not NaN", Double.isNaN(regression.getRSquare()));
        Assert.assertTrue( "RSS not NaN", Double.isNaN(regression.getRegressionSumSquares()));
        Assert.assertTrue("SSE not NaN",Double.isNaN(regression.getSumSquaredErrors()));
        Assert.assertTrue("SSTO not NaN", Double.isNaN(regression.getTotalSumSquares()));
        Assert.assertTrue("predict not NaN", Double.isNaN(regression.predict(0)));

        regression.addData(1, 2);
        regression.addData(1, 3);

        
        Assert.assertTrue("intercept not NaN", Double.isNaN(regression.getIntercept()));
        Assert.assertTrue("slope not NaN", Double.isNaN(regression.getSlope()));
        Assert.assertTrue("slope std err not NaN", Double.isNaN(regression.getSlopeStdErr()));
        Assert.assertTrue("intercept std err not NaN", Double.isNaN(regression.getInterceptStdErr()));
        Assert.assertTrue("MSE not NaN", Double.isNaN(regression.getMeanSquareError()));
        Assert.assertTrue("e not NaN", Double.isNaN(regression.getR()));
        Assert.assertTrue("r-square not NaN", Double.isNaN(regression.getRSquare()));
        Assert.assertTrue("RSS not NaN", Double.isNaN(regression.getRegressionSumSquares()));
        Assert.assertTrue("SSE not NaN", Double.isNaN(regression.getSumSquaredErrors()));
        Assert.assertTrue("predict not NaN", Double.isNaN(regression.predict(0)));

        
        Assert.assertTrue("SSTO NaN", !Double.isNaN(regression.getTotalSumSquares()));

        regression = new SimpleRegression();

        regression.addData(1, 2);
        regression.addData(3, 3);

        
        Assert.assertTrue("interceptNaN", !Double.isNaN(regression.getIntercept()));
        Assert.assertTrue("slope NaN", !Double.isNaN(regression.getSlope()));
        Assert.assertTrue("slope std err not NaN", Double.isNaN(regression.getSlopeStdErr()));
        Assert.assertTrue("intercept std err not NaN", Double.isNaN(regression.getInterceptStdErr()));
        Assert.assertTrue("MSE not NaN", Double.isNaN(regression.getMeanSquareError()));
        Assert.assertTrue("r NaN", !Double.isNaN(regression.getR()));
        Assert.assertTrue("r-square NaN", !Double.isNaN(regression.getRSquare()));
        Assert.assertTrue("RSS NaN", !Double.isNaN(regression.getRegressionSumSquares()));
        Assert.assertTrue("SSE NaN", !Double.isNaN(regression.getSumSquaredErrors()));
        Assert.assertTrue("SSTO NaN", !Double.isNaN(regression.getTotalSumSquares()));
        Assert.assertTrue("predict NaN", !Double.isNaN(regression.predict(0)));

        regression.addData(1, 4);

        
        Assert.assertTrue("MSE NaN", !Double.isNaN(regression.getMeanSquareError()));
        Assert.assertTrue("slope std err NaN", !Double.isNaN(regression.getSlopeStdErr()));
        Assert.assertTrue("intercept std err NaN", !Double.isNaN(regression.getInterceptStdErr()));
    }

// org.apache.commons.math3.stat.regression.SimpleRegressionTest::testClear
    public void testClear() {
        SimpleRegression regression = new SimpleRegression();
        regression.addData(corrData);
        Assert.assertEquals("number of observations", 17, regression.getN());
        regression.clear();
        Assert.assertEquals("number of observations", 0, regression.getN());
        regression.addData(corrData);
        Assert.assertEquals("r-square", .896123, regression.getRSquare(), 10E-6);
        regression.addData(data);
        Assert.assertEquals("number of observations", 53, regression.getN());
    }

// org.apache.commons.math3.stat.regression.SimpleRegressionTest::testInference
    public void testInference() throws Exception {
        
        
        SimpleRegression regression = new SimpleRegression();
        regression.addData(infData);
        Assert.assertEquals("slope std err", 0.011448491,
                regression.getSlopeStdErr(), 1E-10);
        Assert.assertEquals("std err intercept", 0.286036932,
                regression.getInterceptStdErr(),1E-8);
        Assert.assertEquals("significance", 4.596e-07,
                regression.getSignificance(),1E-8);
        Assert.assertEquals("slope conf interval half-width", 0.0270713794287,
                regression.getSlopeConfidenceInterval(),1E-8);
        
        regression = new SimpleRegression();
        regression.addData(infData2);
        Assert.assertEquals("slope std err", 1.07260253,
                regression.getSlopeStdErr(), 1E-8);
        Assert.assertEquals("std err intercept",4.17718672,
                regression.getInterceptStdErr(),1E-8);
        Assert.assertEquals("significance", 0.261829133982,
                regression.getSignificance(),1E-11);
        Assert.assertEquals("slope conf interval half-width", 2.97802204827,
                regression.getSlopeConfidenceInterval(),1E-8);
        

        
        Assert.assertTrue("tighter means wider",
                regression.getSlopeConfidenceInterval() < regression.getSlopeConfidenceInterval(0.01));

        try {
            regression.getSlopeConfidenceInterval(1);
            Assert.fail("expecting MathIllegalArgumentException for alpha = 1");
        } catch (MathIllegalArgumentException ex) {
            
        }

    }

// org.apache.commons.math3.stat.regression.SimpleRegressionTest::testPerfect
    public void testPerfect() throws Exception {
        SimpleRegression regression = new SimpleRegression();
        int n = 100;
        for (int i = 0; i < n; i++) {
            regression.addData(((double) i) / (n - 1), i);
        }
        Assert.assertEquals(0.0, regression.getSignificance(), 1.0e-5);
        Assert.assertTrue(regression.getSlope() > 0.0);
        Assert.assertTrue(regression.getSumSquaredErrors() >= 0.0);
    }

// org.apache.commons.math3.stat.regression.SimpleRegressionTest::testPerfectNegative
    public void testPerfectNegative() throws Exception {
        SimpleRegression regression = new SimpleRegression();
        int n = 100;
        for (int i = 0; i < n; i++) {
            regression.addData(- ((double) i) / (n - 1), i);
        }

        Assert.assertEquals(0.0, regression.getSignificance(), 1.0e-5);
        Assert.assertTrue(regression.getSlope() < 0.0);
    }

// org.apache.commons.math3.stat.regression.SimpleRegressionTest::testRandom
    public void testRandom() throws Exception {
        SimpleRegression regression = new SimpleRegression();
        Random random = new Random(1);
        int n = 100;
        for (int i = 0; i < n; i++) {
            regression.addData(((double) i) / (n - 1), random.nextDouble());
        }

        Assert.assertTrue( 0.0 < regression.getSignificance()
                    && regression.getSignificance() < 1.0);
    }

// org.apache.commons.math3.stat.regression.SimpleRegressionTest::testSSENonNegative
    public void testSSENonNegative() {
        double[] y = { 8915.102, 8919.302, 8923.502 };
        double[] x = { 1.107178495E2, 1.107264895E2, 1.107351295E2 };
        SimpleRegression reg = new SimpleRegression();
        for (int i = 0; i < x.length; i++) {
            reg.addData(x[i], y[i]);
        }
        Assert.assertTrue(reg.getSumSquaredErrors() >= 0.0);
    }

// org.apache.commons.math3.stat.regression.SimpleRegressionTest::testRemoveXY
    public void testRemoveXY() throws Exception {
        
        SimpleRegression regression = new SimpleRegression();
        regression.addData(infData);
        regression.removeData(removeX, removeY);
        regression.addData(removeX, removeY);
        
        Assert.assertEquals("slope std err", 0.011448491,
                regression.getSlopeStdErr(), 1E-10);
        Assert.assertEquals("std err intercept", 0.286036932,
                regression.getInterceptStdErr(),1E-8);
        Assert.assertEquals("significance", 4.596e-07,
                regression.getSignificance(),1E-8);
        Assert.assertEquals("slope conf interval half-width", 0.0270713794287,
                regression.getSlopeConfidenceInterval(),1E-8);
     }

// org.apache.commons.math3.stat.regression.SimpleRegressionTest::testRemoveSingle
    public void testRemoveSingle() throws Exception {
        
        SimpleRegression regression = new SimpleRegression();
        regression.addData(infData);
        regression.removeData(removeSingle);
        regression.addData(removeSingle);
        
        Assert.assertEquals("slope std err", 0.011448491,
                regression.getSlopeStdErr(), 1E-10);
        Assert.assertEquals("std err intercept", 0.286036932,
                regression.getInterceptStdErr(),1E-8);
        Assert.assertEquals("significance", 4.596e-07,
                regression.getSignificance(),1E-8);
        Assert.assertEquals("slope conf interval half-width", 0.0270713794287,
                regression.getSlopeConfidenceInterval(),1E-8);
     }

// org.apache.commons.math3.stat.regression.SimpleRegressionTest::testRemoveMultiple
    public void testRemoveMultiple() throws Exception {
        
        SimpleRegression regression = new SimpleRegression();
        regression.addData(infData);
        regression.removeData(removeMultiple);
        regression.addData(removeMultiple);
        
        Assert.assertEquals("slope std err", 0.011448491,
                regression.getSlopeStdErr(), 1E-10);
        Assert.assertEquals("std err intercept", 0.286036932,
                regression.getInterceptStdErr(),1E-8);
        Assert.assertEquals("significance", 4.596e-07,
                regression.getSignificance(),1E-8);
        Assert.assertEquals("slope conf interval half-width", 0.0270713794287,
                regression.getSlopeConfidenceInterval(),1E-8);
     }

// org.apache.commons.math3.stat.regression.SimpleRegressionTest::testRemoveObsFromEmpty
    public void testRemoveObsFromEmpty() {
        SimpleRegression regression = new SimpleRegression();
        regression.removeData(removeX, removeY);
        Assert.assertEquals(regression.getN(), 0);
    }

// org.apache.commons.math3.stat.regression.SimpleRegressionTest::testRemoveObsFromSingle
    public void testRemoveObsFromSingle() {
        SimpleRegression regression = new SimpleRegression();
        regression.addData(removeX, removeY);
        regression.removeData(removeX, removeY);
        Assert.assertEquals(regression.getN(), 0);
    }

// org.apache.commons.math3.stat.regression.SimpleRegressionTest::testRemoveMultipleToEmpty
    public void testRemoveMultipleToEmpty() {
        SimpleRegression regression = new SimpleRegression();
        regression.addData(removeMultiple);
        regression.removeData(removeMultiple);
        Assert.assertEquals(regression.getN(), 0);
    }

// org.apache.commons.math3.stat.regression.SimpleRegressionTest::testRemoveMultiplePastEmpty
    public void testRemoveMultiplePastEmpty() {
        SimpleRegression regression = new SimpleRegression();
        regression.addData(removeX, removeY);
        regression.removeData(removeMultiple);
        Assert.assertEquals(regression.getN(), 0);
    }

// org.apache.commons.math3.util.ContinuedFractionTest::testGoldenRatio
    public void testGoldenRatio() throws Exception {
        ContinuedFraction cf = new ContinuedFraction() {

            @Override
            public double getA(int n, double x) {
                return 1.0;
            }

            @Override
            public double getB(int n, double x) {
                return 1.0;
            }
        };

        double gr = cf.evaluate(0.0, 10e-9);
        Assert.assertEquals(1.61803399, gr, 10e-9);
    }

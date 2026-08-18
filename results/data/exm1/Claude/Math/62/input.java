// buggy code
    public UnivariateRealPointValuePair optimize(final FUNC f,
                                                 final GoalType goal,
                                                 final double min, final double max)
        throws FunctionEvaluationException {
        return optimize(f, goal, min, max, 0);
    }

    public UnivariateRealPointValuePair optimize(final FUNC f, final GoalType goal,
                                                 final double min, final double max,
                                                 final double startValue)
        throws FunctionEvaluationException {
        optima = new UnivariateRealPointValuePair[starts];
        totalEvaluations = 0;

        // Multi-start loop.
        for (int i = 0; i < starts; ++i) {
            try {
                final double bound1 = (i == 0) ? min : min + generator.nextDouble() * (max - min);
                final double bound2 = (i == 0) ? max : min + generator.nextDouble() * (max - min);
                optima[i] = optimizer.optimize(f, goal, FastMath.min(bound1, bound2), FastMath.max(bound1, bound2));
            } catch (FunctionEvaluationException fee) {
                optima[i] = null;
            } catch (ConvergenceException ce) {
                optima[i] = null;
            }

            final int usedEvaluations = optimizer.getEvaluations();
            optimizer.setMaxEvaluations(optimizer.getMaxEvaluations() - usedEvaluations);
            totalEvaluations += usedEvaluations;
        }

        sortPairs(goal);

        if (optima[0] == null) {
            throw new ConvergenceException(LocalizedFormats.NO_CONVERGENCE_WITH_ANY_START_POINT,
                                           starts);
        }

        // Return the point with the best objective function value.
        return optima[0];
    }

// relevant test
// org.apache.commons.math.optimization.univariate.MultiStartUnivariateRealOptimizerTest::testSinMin
    public void testSinMin() throws MathException {
        UnivariateRealFunction f = new SinFunction();
        UnivariateRealOptimizer underlying = new BrentOptimizer(1e-10, 1e-14);
        underlying.setMaxEvaluations(300);
        JDKRandomGenerator g = new JDKRandomGenerator();
        g.setSeed(44428400075l);
        MultiStartUnivariateRealOptimizer optimizer =
            new MultiStartUnivariateRealOptimizer(underlying, 10, g);
        optimizer.optimize(f, GoalType.MINIMIZE, -100.0, 100.0);
        UnivariateRealPointValuePair[] optima = optimizer.getOptima();
        for (int i = 1; i < optima.length; ++i) {
            double d = (optima[i].getPoint() - optima[i-1].getPoint()) / (2 * FastMath.PI);
            assertTrue (FastMath.abs(d - FastMath.rint(d)) < 1.0e-8);
            assertEquals(-1.0, f.value(optima[i].getPoint()), 1.0e-10);
            assertEquals(f.value(optima[i].getPoint()), optima[i].getValue(), 1.0e-10);
        }
        assertTrue(optimizer.getEvaluations() > 200);
        assertTrue(optimizer.getEvaluations() < 300);
    }

// org.apache.commons.math.optimization.univariate.MultiStartUnivariateRealOptimizerTest::testQuinticMin
    public void testQuinticMin() throws MathException {
        
        
        UnivariateRealFunction f = new QuinticFunction();
        UnivariateRealOptimizer underlying = new BrentOptimizer(1e-9, 1e-14);
        underlying.setMaxEvaluations(300);
        JDKRandomGenerator g = new JDKRandomGenerator();
        g.setSeed(4312000053L);
        MultiStartUnivariateRealOptimizer optimizer =
            new MultiStartUnivariateRealOptimizer(underlying, 5, g);

        UnivariateRealPointValuePair optimum
            = optimizer.optimize(f, GoalType.MINIMIZE, -0.3, -0.2);
        assertEquals(-0.2719561293, optimum.getPoint(), 1e-9);
        assertEquals(-0.0443342695, optimum.getValue(), 1e-9);

        UnivariateRealPointValuePair[] optima = optimizer.getOptima();
        for (int i = 0; i < optima.length; ++i) {
            assertEquals(f.value(optima[i].getPoint()), optima[i].getValue(), 1e-9);
        }
        assertTrue(optimizer.getEvaluations() >= 50);
        assertTrue(optimizer.getEvaluations() <= 100);
    }

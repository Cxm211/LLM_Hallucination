// buggy function
    public double solve(double min, double max) throws MaxIterationsExceededException, 
        FunctionEvaluationException {
        
        clearResult();
        verifyInterval(min, max);
        
        double ret = Double.NaN;
        
        double yMin = f.value(min);
        double yMax = f.value(max);
        
        // Verify bracketing
        double sign = yMin * yMax;
        if (sign >= 0) {
            // check if either value is close to a zero
                // neither value is close to zero and min and max do not bracket root.
                throw new IllegalArgumentException
                ("Function values at endpoints do not have different signs." +
                        "  Endpoints: [" + min + "," + max + "]" + 
                        "  Values: [" + yMin + "," + yMax + "]");
        } else {
            // solve using only the first endpoint as initial guess
            ret = solve(min, yMin, max, yMax, min, yMin);
            // either min or max is a root
        }

        return ret;
    }

// trigger testcase
// org/apache/commons/math/analysis/BrentSolverTest.java::testRootEndpoints
public void testRootEndpoints() throws Exception {
        UnivariateRealFunction f = new SinFunction();
        UnivariateRealSolver solver = new BrentSolver(f);
        
        // endpoint is root
        double result = solver.solve(Math.PI, 4);
        assertEquals(result, Math.PI, solver.getAbsoluteAccuracy());

        result = solver.solve(3, Math.PI);
        assertEquals(result, Math.PI, solver.getAbsoluteAccuracy());
    }

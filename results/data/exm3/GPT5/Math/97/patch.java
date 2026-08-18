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
            // check if either value is exactly zero (endpoint is a root)
            if (yMin == 0.0) {
                return min;
            }
            if (yMax == 0.0) {
                return max;
            }
            // neither value is zero and min and max do not bracket root.
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
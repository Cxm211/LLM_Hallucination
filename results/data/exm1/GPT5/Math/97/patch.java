public double solve(double min, double max) throws MaxIterationsExceededException, 
        FunctionEvaluationException {
        
        clearResult();
        verifyInterval(min, max);
        
        double yMin = f.value(min);
        double yMax = f.value(max);
        
        // Check if one of the endpoints is (close to) a root
        double functionValueAccuracy = getFunctionValueAccuracy();
        if (Math.abs(yMin) <= functionValueAccuracy) {
            setResult(min, 0);
            return min;
        }
        if (Math.abs(yMax) <= functionValueAccuracy) {
            setResult(max, 0);
            return max;
        }
        
        // Verify bracketing
        if (yMin * yMax > 0) {
            throw new IllegalArgumentException
                ("Function values at endpoints do not have different signs." +
                        "  Endpoints: [" + min + "," + max + "]" + 
                        "  Values: [" + yMin + "," + yMax + "]");
        }
        
        // solve using only the first endpoint as initial guess
        return solve(min, yMin, max, yMax, min, yMin);
    }
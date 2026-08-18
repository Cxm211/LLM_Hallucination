public double getSumSquaredErrors() {
        if (sumXX == 0.0) {
            return Double.NaN;
        }
        double sse = sumYY - (sumXY * sumXY) / sumXX;
        return sse < 0.0 ? 0.0 : sse;
    }
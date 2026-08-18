public double getSumSquaredErrors() {
        double sse = sumYY - (sumXY * sumXY) / sumXX;
        return Math.max(0.0, sse);
    }
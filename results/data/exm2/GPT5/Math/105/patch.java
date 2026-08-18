public double getSumSquaredErrors() {
        double sse = sumYY - sumXY * sumXY / sumXX;
        return (sse < 0.0) ? 0.0 : sse;
    }
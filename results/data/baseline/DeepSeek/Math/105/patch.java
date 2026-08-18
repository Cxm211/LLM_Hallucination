    public double getSumSquaredErrors() {
        if (sumXX == 0.0) {
            return Double.NaN;
        }
        return sumYY - sumXY * sumXY / sumXX;
    }
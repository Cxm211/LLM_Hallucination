    public double getSumSquaredErrors() {
        if (sumXX == 0.0) {
            return sumYY;
        }
        return Math.max(0.0, sumYY - sumXY * sumXY / sumXX);
    }
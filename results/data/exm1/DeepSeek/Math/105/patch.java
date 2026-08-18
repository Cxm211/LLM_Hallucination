public double getSumSquaredErrors() {
    double sum = sumYY - sumXY * sumXY / sumXX;
    if (sum < 0) {
        return 0;
    }
    return sum;
}
public double getSumSquaredErrors() {
    double ssto = sumYY - sumY * sumY / n;
    double ssr = sumXY * sumXY / sumXX;
    double sse = ssto - ssr;
    return sse;
}
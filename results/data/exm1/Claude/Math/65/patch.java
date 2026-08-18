public double getRMS() {
    double criterion = 0;
    double sumWeights = 0;
    for (int i = 0; i < rows; ++i) {
        final double residual = residuals[i];
        criterion += residual * residual * residualsWeights[i];
        sumWeights += residualsWeights[i];
    }
    return Math.sqrt(criterion / sumWeights);
}
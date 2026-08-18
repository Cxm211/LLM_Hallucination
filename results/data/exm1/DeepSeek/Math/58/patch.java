    public double[] fit() {
        final WeightedObservedPoint[] obs = getObservations();
        if (obs.length == 0) {
            return new double[] { 0.0, 0.0, 1.0 };
        }
        final double[] guess;
        try {
            guess = new ParameterGuesser(obs).guess();
        } catch (Exception e) {
            double sumX = 0, sumY = 0;
            double minX = Double.MAX_VALUE, maxX = -Double.MAX_VALUE;
            double maxY = -Double.MAX_VALUE;
            for (WeightedObservedPoint p : obs) {
                sumX += p.getX();
                sumY += p.getY();
                if (p.getX() < minX) minX = p.getX();
                if (p.getX() > maxX) maxX = p.getX();
                if (p.getY() > maxY) maxY = p.getY();
            }
            double meanX = sumX / obs.length;
            double sigma = (maxX - minX) / 2.0;
            if (sigma <= 0) sigma = 1.0;
            guess = new double[] { maxY, meanX, sigma };
        }
        return fit(new Gaussian.Parametric(), guess);
    }
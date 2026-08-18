public double getLInfNorm() {
        double max = 0;
        for (double a : data) {
            double abs = Math.abs(a);
            if (abs > max) {
                max = abs;
            }
        }
        return max;
    }
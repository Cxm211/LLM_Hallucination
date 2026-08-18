    public double getLInfNorm() {
        double max = 0;
        Iterator iter = entries.iterator();
        while (iter.hasNext()) {
            iter.advance();
            double value = Math.abs(iter.value());
            if (value > max) {
                max = value;
            }
        }
        return max;
    }
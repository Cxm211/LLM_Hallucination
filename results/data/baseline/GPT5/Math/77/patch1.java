public double getLInfNorm() {
        double max = 0;
        Iterator iter = entries.iterator();
        while (iter.hasNext()) {
            iter.advance();
            double abs = Math.abs(iter.value());
            if (abs > max) {
                max = abs;
            }
        }
        return max;
    }
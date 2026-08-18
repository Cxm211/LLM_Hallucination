public double getMaximumExplodePercent() {
        double result = 0.0;
        if (this.dataset == null) {
            return result;
        }
        Iterator iterator = this.dataset.getKeys().iterator();
        while (iterator.hasNext()) {
            Comparable key = (Comparable) iterator.next();
            Number explode = (this.explodePercentages != null) ? (Number) this.explodePercentages.get(key) : null;
            if (explode != null) {
                result = Math.max(result, explode.doubleValue());
            }
        }
        return result;
    }
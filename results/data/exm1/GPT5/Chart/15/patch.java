public double getMaximumExplodePercent() {
        double result = 0.0;
        if (this.dataset != null) {
            Iterator iterator = this.dataset.getKeys().iterator();
            while (iterator.hasNext()) {
                Comparable key = (Comparable) iterator.next();
                Number explode = (Number) this.explodePercentages.get(key);
                if (explode != null) {
                    result = Math.max(result, explode.doubleValue());
                }
            }
        } else {
            Iterator it = this.explodePercentages.values().iterator();
            while (it.hasNext()) {
                Number explode = (Number) it.next();
                if (explode != null) {
                    result = Math.max(result, explode.doubleValue());
                }
            }
        }
        return result;
    }
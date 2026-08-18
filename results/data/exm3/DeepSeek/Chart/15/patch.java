    public double getMaximumExplodePercent() {
        double result = 0.0;
        if (this.dataset != null) {
            Iterator iterator = this.dataset.getKeys().iterator();
            while (iterator.hasNext()) {
                Comparable key = (Comparable) iterator.next();
                if (this.explodePercentages != null) {
                    Number explode = (Number) this.explodePercentages.get(key);
                    if (explode != null) {
                        result = Math.max(result, explode.doubleValue());   
                    }
                }
            }
        }
        return result;
    }
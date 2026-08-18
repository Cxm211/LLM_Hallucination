public double getMaximumExplodePercent() {
    double result = 0.0;
    Iterator iterator = this.explodePercentages.keySet().iterator();
    while (iterator.hasNext()) {
        Comparable key = (Comparable) iterator.next();
        Number explode = (Number) this.explodePercentages.get(key);
        if (explode != null) {
            result = Math.max(result, explode.doubleValue());
        }
    }
    return result;
}
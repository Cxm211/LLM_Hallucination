public double getPct(Object v) {
    if (v instanceof Integer) {
        return getCumPct(Long.valueOf(((Integer) v).longValue()));
    }
    if (v instanceof Long) {
        return getCumPct((Long) v);
    }
    if (v instanceof Comparable) {
        return getCumPct((Comparable<?>) v);
    }
    // For non-comparable objects, return 0
    // This handles cases like strings that don't match any frequency entry
    try {
        return getCumPct((Comparable<?>) v);
    } catch (ClassCastException e) {
        return 0.0;
    }
}
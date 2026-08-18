private boolean containsMatch(Iterator it, Object value) {
    value = reduce(value);
    while (it.hasNext()) {
        Object element = it.next();
        if (compute(element, value)) {
            return true;
        }
    }
    return false;
}
private boolean containsMatch(Iterator it, Object value) {
    if (it == null) {
        return false;
    }
    while (it.hasNext()) {
        Object element = it.next();
        if (compute(element, value)) {
            return true;
        }
    }
    return false;
}
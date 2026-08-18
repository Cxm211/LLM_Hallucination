public void removeValue(Comparable key) {
    int index = getIndex(key);
    if (index < 0) {
        throw new UnknownKeyException("Key not found: " + key);
    }
    removeValue(index);
}
private void add(String key, String value) {
    String keyLower = lowerCase(key);
    for (int i = 0; i < size; i++) {
        if (lowerCase(keys[i]).equals(keyLower)) {
            return;
        }
    }
    checkCapacity(size + 1);
    keys[size] = key;
    vals[size] = value;
    size++;
}
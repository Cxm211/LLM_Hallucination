private void add(String key, String value) {
    checkCapacity(size + 1);
    for (int i = 0; i < size; i++) {
        if (keys[i].equals(key)) {
            return;
        }
    }
    keys[size] = key;
    vals[size] = value;
    size++;
}
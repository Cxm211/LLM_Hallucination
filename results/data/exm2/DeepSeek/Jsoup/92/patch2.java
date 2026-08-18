public void normalize() {
    // First, lower case all keys
    for (int i = 0; i < size; i++) {
        keys[i] = lowerCase(keys[i]);
    }
    // Deduplicate: keep the first occurrence of each key
    int newSize = 0;
    for (int i = 0; i < size; i++) {
        boolean duplicate = false;
        for (int j = 0; j < newSize; j++) {
            if (keys[i].equals(keys[j])) {
                duplicate = true;
                break;
            }
        }
        if (!duplicate) {
            keys[newSize] = keys[i];
            vals[newSize] = vals[i];
            newSize++;
        }
    }
    size = newSize;
}
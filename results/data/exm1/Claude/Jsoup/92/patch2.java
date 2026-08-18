public void normalize() {
    for (int i = 0; i < size; i++) {
        keys[i] = lowerCase(keys[i]);
    }
}
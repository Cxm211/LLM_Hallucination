public void normalize() {
    for (int i = 0; i < size; i++) {
        keys[i] = lowerCase(keys[i]);
    }
    
    for (int i = 0; i < size; i++) {
        for (int j = i + 1; j < size; ) {
            if (keys[i].equals(keys[j])) {
                remove(j);
            } else {
                j++;
            }
        }
    }
}
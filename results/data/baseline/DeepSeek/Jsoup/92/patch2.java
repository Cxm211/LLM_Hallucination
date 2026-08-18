    public void normalize() {
        // first lower case all keys
        for (int i = 0; i < size; i++) {
            keys[i] = lowerCase(keys[i]);
        }
        // deduplicate, keep first occurrence
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < i; j++) {
                if (keys[j].equals(keys[i])) {
                    System.arraycopy(keys, i+1, keys, i, size - i - 1);
                    System.arraycopy(vals, i+1, vals, i, size - i - 1);
                    size--;
                    i--;
                    break;
                }
            }
        }
    }
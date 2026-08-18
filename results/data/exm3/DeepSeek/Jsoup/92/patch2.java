    public void normalize() {
        for (int i = 0; i < size; i++) {
            keys[i] = lowerCase(keys[i]);
        }
        // deduplicate after lowercasing
        int newSize = 0;
        for (int i = 0; i < size; i++) {
            boolean found = false;
            for (int j = 0; j < newSize; j++) {
                if (keys[i].equals(keys[j])) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                keys[newSize] = keys[i];
                vals[newSize] = vals[i];
                newSize++;
            }
        }
        size = newSize;
    }
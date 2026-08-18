public int[] getCounts(int index) {
        if (index < 0 || index >= totalSize) {
            throw new OutOfRangeException(index, 0, totalSize);
        }

        final int[] indices = new int[dimension];
        int remainder = index;
        for (int i = 0; i < dimension; i++) {
            final int offset = uniCounterOffset[i];
            final int idx = remainder / offset;
            indices[i] = idx;
            remainder -= idx * offset;
        }

        return indices;
    }
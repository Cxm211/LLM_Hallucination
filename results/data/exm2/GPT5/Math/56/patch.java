public int[] getCounts(int index) {
        if (index < 0 ||
            index >= totalSize) {
            throw new OutOfRangeException(index, 0, totalSize - 1);
        }

        final int[] indices = new int[dimension];

        int count = index;
        for (int i = 0; i < last; i++) {
            final int offset = uniCounterOffset[i];
            final int idx = count / offset;
            indices[i] = idx;
            count -= idx * offset;
        }

        indices[last] = count;

        return indices;
    }
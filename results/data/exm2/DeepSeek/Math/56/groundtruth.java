// ===== FIXED org.apache.commons.math.util.MultidimensionalCounter :: getCounts(int) [lines 216-240] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/Math/Math-56-fixed/src/main/java/org/apache/commons/math/util/MultidimensionalCounter.java =====
    public int[] getCounts(int index) {
        if (index < 0 ||
            index >= totalSize) {
            throw new OutOfRangeException(index, 0, totalSize);
        }

        final int[] indices = new int[dimension];

        int count = 0;
        for (int i = 0; i < last; i++) {
            int idx = 0;
            final int offset = uniCounterOffset[i];
            while (count <= index) {
                count += offset;
                ++idx;
            }
            --idx;
            count -= offset;
            indices[i] = idx;
        }

        indices[last] = index - count;

        return indices;
    }

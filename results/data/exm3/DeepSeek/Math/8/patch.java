    public T[] sample(int sampleSize) throws NotStrictlyPositiveException {
        if (sampleSize <= 0) {
            throw new NotStrictlyPositiveException(LocalizedFormats.NUMBER_OF_SAMPLES,
                    sampleSize);
        }

        // Determine the common class of all singletons to avoid ArrayStoreException
        Class<?> commonClass = null;
        for (T value : singletons) {
            if (commonClass == null) {
                commonClass = value.getClass();
            } else if (commonClass != value.getClass()) {
                Class<?> candidate = commonClass;
                while (candidate != null && !candidate.isAssignableFrom(value.getClass())) {
                    candidate = candidate.getSuperclass();
                }
                if (candidate == null) {
                    // No common superclass found (should not happen if all objects derive from Object)
                    commonClass = Object.class;
                } else {
                    commonClass = candidate;
                }
            }
        }
        if (commonClass == null) {
            // No singletons? fallback to Object
            commonClass = Object.class;
        }

        final T[] out = (T[]) java.lang.reflect.Array.newInstance(commonClass, sampleSize);

        for (int i = 0; i < sampleSize; i++) {
            out[i] = sample();
        }

        return out;
    }
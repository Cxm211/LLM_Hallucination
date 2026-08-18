    public T[] sample(int sampleSize) throws NotStrictlyPositiveException {
        if (sampleSize <= 0) {
            throw new NotStrictlyPositiveException(LocalizedFormats.NUMBER_OF_SAMPLES,
                    sampleSize);
        }

        // Determine the common superclass of all singletons
        Class<?> commonClass = null;
        for (T s : singletons) {
            if (s != null) {
                commonClass = s.getClass();
                break;
            }
        }
        if (commonClass == null) {
            // All singletons are null, use Object
            commonClass = Object.class;
        } else {
            for (T s : singletons) {
                if (s != null) {
                    Class<?> c = s.getClass();
                    while (!commonClass.isAssignableFrom(c)) {
                        commonClass = commonClass.getSuperclass();
                    }
                }
            }
        }

        final T[] out = (T[]) java.lang.reflect.Array.newInstance(commonClass, sampleSize);

        for (int i = 0; i < sampleSize; i++) {
            out[i] = sample();
        }

        return out;
    }
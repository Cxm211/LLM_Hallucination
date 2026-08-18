public T[] sample(int sampleSize) throws NotStrictlyPositiveException {
        if (sampleSize <= 0) {
            throw new NotStrictlyPositiveException(LocalizedFormats.NUMBER_OF_SAMPLES,
                    sampleSize);
        }

        Class<?> componentType = null;
        for (T v : singletons) {
            if (v == null) {
                componentType = Object.class;
                break;
            }
            Class<?> vc = v.getClass();
            if (componentType == null) {
                componentType = vc;
            } else if (!componentType.isAssignableFrom(vc)) {
                // Find common superclass
                Class<?> c = componentType;
                while (c != null && !c.isAssignableFrom(vc)) {
                    c = c.getSuperclass();
                }
                componentType = (c == null) ? Object.class : c;
                if (componentType == Object.class) {
                    break;
                }
            }
        }
        if (componentType == null) {
            componentType = Object.class;
        }

        final T[] out = (T[]) java.lang.reflect.Array.newInstance(componentType, sampleSize);

        for (int i = 0; i < sampleSize; i++) {
            out[i] = sample();
        }

        return out;

    }
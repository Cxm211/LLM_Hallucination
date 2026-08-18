public T[] sample(int sampleSize) throws NotStrictlyPositiveException {
    if (sampleSize <= 0) {
        throw new NotStrictlyPositiveException(LocalizedFormats.NUMBER_OF_SAMPLES,
                sampleSize);
    }

    final T[]out = (T[]) java.lang.reflect.Array.newInstance(sample().getClass(), sampleSize);

    out[0] = sample();
    for (int i = 1; i < sampleSize; i++) {
        out[i] = sample();
    }

    return out;

}
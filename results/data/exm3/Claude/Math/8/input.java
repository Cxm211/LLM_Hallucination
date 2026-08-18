// buggy function
    public T[] sample(int sampleSize) throws NotStrictlyPositiveException {
        if (sampleSize <= 0) {
            throw new NotStrictlyPositiveException(LocalizedFormats.NUMBER_OF_SAMPLES,
                    sampleSize);
        }

        final T[]out = (T[]) java.lang.reflect.Array.newInstance(singletons.get(0).getClass(), sampleSize);

        for (int i = 0; i < sampleSize; i++) {
            out[i] = sample();
        }

        return out;

    }

// trigger testcase
// org/apache/commons/math3/distribution/DiscreteRealDistributionTest.java::testIssue942
@Test
    public void testIssue942() {
        List<Pair<Object,Double>> list = new ArrayList<Pair<Object, Double>>();
        list.add(new Pair<Object, Double>(new Object() {}, new Double(0)));
        list.add(new Pair<Object, Double>(new Object() {}, new Double(1)));
        Assert.assertEquals(1, new DiscreteDistribution<Object>(list).sample(1).length);
    }

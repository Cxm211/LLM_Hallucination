// org/apache/commons/math3/distribution/DiscreteRealDistributionTest.java
@Test
    public void testSampleWithDifferentClasses() {
        List<Pair<Object,Double>> list = new ArrayList<Pair<Object, Double>>();
        list.add(new Pair<Object, Double>("string1", 0.5));
        list.add(new Pair<Object, Double>(Integer.valueOf(42), 0.5));
        DiscreteDistribution<Object> dist = new DiscreteDistribution<Object>(list);
        Object[] samples = dist.sample(10);
        Assert.assertEquals(10, samples.length);
        for (Object sample : samples) {
            Assert.assertTrue(sample instanceof String || sample instanceof Integer);
        }
    }

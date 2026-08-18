// org/apache/commons/math3/distribution/DiscreteRealDistributionTest.java
@Test
    public void testSampleWithCommonSuperclass() {
        List<Pair<Number,Double>> list = new ArrayList<Pair<Number, Double>>();
        list.add(new Pair<Number, Double>(Integer.valueOf(1), 0.5));
        list.add(new Pair<Number, Double>(Double.valueOf(2.0), 0.5));
        DiscreteDistribution<Number> dist = new DiscreteDistribution<Number>(list);
        Number[] samples = dist.sample(10);
        Assert.assertEquals(10, samples.length);
        for (Number sample : samples) {
            Assert.assertTrue(sample instanceof Integer || sample instanceof Double);
        }
    }

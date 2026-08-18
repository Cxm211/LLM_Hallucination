// org/apache/commons/math3/distribution/DiscreteRealDistributionTest.java::testIssue942
@Test
    public void testIssue942_nullSingleton() {
        List<Pair<Object,Double>> list = new ArrayList<Pair<Object, Double>>();
        list.add(new Pair<Object, Double>(null, 1.0));
        Assert.assertEquals(1, new DiscreteDistribution<Object>(list).sample(1).length);
    }
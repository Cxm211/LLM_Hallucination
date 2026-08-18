// org/apache/commons/math3/distribution/DiscreteRealDistributionTest.java
@Test
public void testIssue942SingleElement() {
    List<Pair<Object,Double>> list = new ArrayList<Pair<Object, Double>>();
    list.add(new Pair<Object, Double>(new Object() {}, new Double(1.0)));
    Object[] result = new DiscreteDistribution<Object>(list).sample(5);
    Assert.assertEquals(5, result.length);
    Assert.assertNotNull(result[0]);
}
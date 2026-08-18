// org/apache/commons/math3/distribution/DiscreteRealDistributionTest.java
@Test
public void testIssue942MultipleElements() {
    List<Pair<Object,Double>> list = new ArrayList<Pair<Object, Double>>();
    list.add(new Pair<Object, Double>(new Object() {}, new Double(0.5)));
    list.add(new Pair<Object, Double>(new Object() {}, new Double(0.5)));
    Assert.assertEquals(3, new DiscreteDistribution<Object>(list).sample(3).length);
}
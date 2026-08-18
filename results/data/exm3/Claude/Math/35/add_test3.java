// org/apache/commons/math3/genetics/ElitisticListPopulationTest.java
@Test(expected = IllegalArgumentException.class)
public void testConstructorJustBelowZero() {
    final double rate = -0.0001;
    new ElitisticListPopulation(100, rate);
}
// org/apache/commons/math3/genetics/ElitisticListPopulationTest.java
public void testConstructorNaN() {
    final double rate = Double.NaN;
    new ElitisticListPopulation(100, rate);
}

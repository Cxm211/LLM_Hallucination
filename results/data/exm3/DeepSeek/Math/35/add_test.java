// org/apache/commons/math3/genetics/ElitisticListPopulationTest.java
public void testChromosomeListConstructorNaN() {
    final List<Chromosome> chromosomes = Collections.emptyList();
    final double rate = Double.NaN;
    new ElitisticListPopulation(chromosomes, 100, rate);
}

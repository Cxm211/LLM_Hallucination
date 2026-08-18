// org/apache/commons/math3/genetics/ElitisticListPopulationTest.java
@Test
public void testValidElitismRateNonListConstructorBoundary() {
    ElitisticListPopulation pop1 = new ElitisticListPopulation(100, 0.0);
    assertEquals(0.0, pop1.getElitismRate(), 0.0);
    ElitisticListPopulation pop2 = new ElitisticListPopulation(100, 1.0);
    assertEquals(1.0, pop2.getElitismRate(), 0.0);
}
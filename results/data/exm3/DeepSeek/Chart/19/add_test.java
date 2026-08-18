// org/jfree/chart/plot/junit/CategoryPlotTests.java
public void testGetDomainAxisIndexWithParent() {
    CategoryAxis domainAxis = new CategoryAxis("X");
    NumberAxis rangeAxis = new NumberAxis("Y");
    CategoryPlot subplot = new CategoryPlot(null, null, rangeAxis, null);
    CombinedDomainCategoryPlot parentPlot = new CombinedDomainCategoryPlot(domainAxis);
    parentPlot.add(subplot);
    assertEquals(0, subplot.getDomainAxisIndex(domainAxis));
    assertEquals(-1, subplot.getDomainAxisIndex(new CategoryAxis("Other")));
}

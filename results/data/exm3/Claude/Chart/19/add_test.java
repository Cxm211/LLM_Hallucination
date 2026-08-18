// org/jfree/chart/plot/junit/CategoryPlotTests.java
public void testGetDomainAxisIndexWithParent() {
    CategoryAxis domainAxis1 = new CategoryAxis("X1");
    CategoryAxis domainAxis2 = new CategoryAxis("X2");
    NumberAxis rangeAxis1 = new NumberAxis("Y1");
    CategoryPlot parentPlot = new CategoryPlot(null, domainAxis1, rangeAxis1, null);
    parentPlot.setDomainAxis(1, domainAxis2);
    CategoryPlot childPlot = new CategoryPlot(null, domainAxis1, rangeAxis1, null);
    childPlot.setParent(parentPlot);
    assertEquals(0, childPlot.getDomainAxisIndex(domainAxis1));
    assertEquals(-1, childPlot.getDomainAxisIndex(domainAxis2));
}
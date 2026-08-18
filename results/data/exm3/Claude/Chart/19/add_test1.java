// org/jfree/chart/plot/junit/CategoryPlotTests.java
public void testGetRangeAxisIndexWithParent() {
    CategoryAxis domainAxis1 = new CategoryAxis("X1");
    NumberAxis rangeAxis1 = new NumberAxis("Y1");
    NumberAxis rangeAxis2 = new NumberAxis("Y2");
    CategoryPlot parentPlot = new CategoryPlot(null, domainAxis1, rangeAxis1, null);
    parentPlot.setRangeAxis(1, rangeAxis2);
    CategoryPlot childPlot = new CategoryPlot(null, domainAxis1, rangeAxis1, null);
    childPlot.setParent(parentPlot);
    assertEquals(0, childPlot.getRangeAxisIndex(rangeAxis1));
    assertEquals(1, childPlot.getRangeAxisIndex(rangeAxis2));
}
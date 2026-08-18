// org/jfree/chart/plot/junit/CategoryPlotTests.java
public void testGetRangeAxisIndexWithNonCategoryParent() {
    CategoryAxis domainAxis1 = new CategoryAxis("X1");
    NumberAxis rangeAxis1 = new NumberAxis("Y1");
    NumberAxis rangeAxis2 = new NumberAxis("Y2");
    CategoryPlot plot = new CategoryPlot(null, domainAxis1, rangeAxis1, null);
    plot.setParent(new XYPlot());
    assertEquals(0, plot.getRangeAxisIndex(rangeAxis1));
    assertEquals(-1, plot.getRangeAxisIndex(rangeAxis2));
}
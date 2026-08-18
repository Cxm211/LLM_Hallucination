// org/jfree/chart/renderer/category/junit/AbstractCategoryItemRendererTests.java
public void testGetLegendItemsWithDescendingOrder() {
    AbstractCategoryItemRenderer r = new LineAndShapeRenderer();
    DefaultCategoryDataset dataset = new DefaultCategoryDataset();
    CategoryPlot plot = new CategoryPlot();
    plot.setRowRenderingOrder(SortOrder.DESCENDING);
    plot.setDataset(dataset);
    plot.setRenderer(r);
    
    dataset.addValue(1.0, "S1", "C1");
    dataset.addValue(2.0, "S2", "C1");
    dataset.addValue(3.0, "S3", "C1");
    
    LegendItemCollection lic = r.getLegendItems();
    assertEquals(3, lic.getItemCount());
    assertEquals("S3", lic.get(0).getLabel());
    assertEquals("S2", lic.get(1).getLabel());
    assertEquals("S1", lic.get(2).getLabel());
}
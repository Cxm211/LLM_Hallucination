// org/jfree/chart/renderer/category/junit/AbstractCategoryItemRendererTests.java::test2947660
public void testLegendItemsDescendingOrder() {
        AbstractCategoryItemRenderer r = new LineAndShapeRenderer();
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        dataset.addValue(1.0, "S1", "C1");
        dataset.addValue(2.0, "S2", "C1");
        CategoryPlot plot = new CategoryPlot();
        plot.setDataset(dataset);
        plot.setRenderer(r);
        plot.setRowRenderingOrder(SortOrder.DESCENDING);
        LegendItemCollection lic = r.getLegendItems();
        assertEquals(2, lic.getItemCount());
        assertEquals("S2", lic.get(0).getLabel());
        assertEquals("S1", lic.get(1).getLabel());
    }
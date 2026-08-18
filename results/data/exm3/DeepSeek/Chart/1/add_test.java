// org/jfree/chart/renderer/category/junit/AbstractCategoryItemRendererTests.java
public void testGetLegendItemsWithNullDataset() {
        AbstractCategoryItemRenderer r = new LineAndShapeRenderer();
        CategoryPlot plot = new CategoryPlot();
        plot.setRenderer(r);
        LegendItemCollection lic = r.getLegendItems();
        assertNotNull(lic);
        assertEquals(0, lic.getItemCount());
    }

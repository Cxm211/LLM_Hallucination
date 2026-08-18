// org/jfree/chart/renderer/category/junit/AbstractCategoryItemRendererTests.java
public void testGetLegendItemsDescendingWithInvisibleAndNull() {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        dataset.addValue(1.0, "S0", "C1");
        dataset.addValue(2.0, "S1", "C1");
        dataset.addValue(3.0, "S2", "C1");
        LineAndShapeRenderer r = new LineAndShapeRenderer() {
            @Override
            public LegendItem getLegendItem(int datasetIndex, int series) {
                if (series == 1) {
                    return null;
                }
                return super.getLegendItem(datasetIndex, series);
            }
        };
        r.setSeriesVisibleInLegend(0, Boolean.FALSE);
        CategoryPlot plot = new CategoryPlot();
        plot.setDataset(dataset);
        plot.setRenderer(r);
        plot.setRowRenderingOrder(SortOrder.DESCENDING);
        LegendItemCollection lic = r.getLegendItems();
        assertEquals(1, lic.getItemCount());
        assertEquals("S2", lic.get(0).getLabel());
    }

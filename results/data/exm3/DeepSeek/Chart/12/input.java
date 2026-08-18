// buggy function
    public MultiplePiePlot(CategoryDataset dataset) {
        super();
        this.dataset = dataset;
        PiePlot piePlot = new PiePlot(null);
        this.pieChart = new JFreeChart(piePlot);
        this.pieChart.removeLegend();
        this.dataExtractOrder = TableOrder.BY_COLUMN;
        this.pieChart.setBackgroundPaint(null);
        TextTitle seriesTitle = new TextTitle("Series Title",
                new Font("SansSerif", Font.BOLD, 12));
        seriesTitle.setPosition(RectangleEdge.BOTTOM);
        this.pieChart.setTitle(seriesTitle);
        this.aggregatedItemsKey = "Other";
        this.aggregatedItemsPaint = Color.lightGray;
        this.sectionPaints = new HashMap();
    }

// trigger testcase
// org/jfree/chart/plot/junit/MultiplePiePlotTests.java::testConstructor
public void testConstructor() {
    	MultiplePiePlot plot = new MultiplePiePlot();
    	assertNull(plot.getDataset());

    	// the following checks that the plot registers itself as a listener
    	// with the dataset passed to the constructor - see patch 1943021
    	DefaultCategoryDataset dataset = new DefaultCategoryDataset();
    	plot = new MultiplePiePlot(dataset);
    	assertTrue(dataset.hasListener(plot));
    }

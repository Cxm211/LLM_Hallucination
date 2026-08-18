// buggy code
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

// relevant test
// org.jfree.chart.axis.junit.LogAxisTests::testCloning
    public void testCloning() {
        LogAxis a1 = new LogAxis("Test");
        LogAxis a2 = null;
        try {
            a2 = (LogAxis) a1.clone();
        }
        catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        assertTrue(a1 != a2);
        assertTrue(a1.getClass() == a2.getClass());
        assertTrue(a1.equals(a2));
    }

// org.jfree.chart.axis.junit.LogAxisTests::testEquals
    public void testEquals() {}

// org.jfree.chart.axis.junit.LogAxisTests::testHashCode
    public void testHashCode() {
        LogAxis a1 = new LogAxis("Test");
        LogAxis a2 = new LogAxis("Test");
        assertTrue(a1.equals(a2));
        int h1 = a1.hashCode();
        int h2 = a2.hashCode();
        assertEquals(h1, h2);
    }

// org.jfree.chart.axis.junit.LogAxisTests::testTranslateJava2DToValue
    public void testTranslateJava2DToValue() {
        LogAxis axis = new LogAxis();
        axis.setRange(50.0, 100.0); 
        Rectangle2D dataArea = new Rectangle2D.Double(10.0, 50.0, 400.0, 300.0);
        double y1 = axis.java2DToValue(75.0, dataArea, RectangleEdge.LEFT);  
        assertEquals(94.3874312681693, y1, EPSILON); 
        double y2 = axis.java2DToValue(75.0, dataArea, RectangleEdge.RIGHT);   
        assertEquals(94.3874312681693, y2, EPSILON); 
        double x1 = axis.java2DToValue(75.0, dataArea, RectangleEdge.TOP);   
        assertEquals(55.961246381405, x1, EPSILON); 
        double x2 = axis.java2DToValue(75.0, dataArea, RectangleEdge.BOTTOM);   
        assertEquals(55.961246381405, x2, EPSILON); 
        axis.setInverted(true);
        double y3 = axis.java2DToValue(75.0, dataArea, RectangleEdge.LEFT);  
        assertEquals(52.9731547179647, y3, EPSILON); 
        double y4 = axis.java2DToValue(75.0, dataArea, RectangleEdge.RIGHT);   
        assertEquals(52.9731547179647, y4, EPSILON); 
        double x3 = axis.java2DToValue(75.0, dataArea, RectangleEdge.TOP);   
        assertEquals(89.3475453695651, x3, EPSILON); 
        double x4 = axis.java2DToValue(75.0, dataArea, RectangleEdge.BOTTOM);   
        assertEquals(89.3475453695651, x4, EPSILON); 
    }

// org.jfree.chart.axis.junit.LogAxisTests::testSerialization
    public void testSerialization() {

        LogAxis a1 = new LogAxis("Test Axis");
        LogAxis a2 = null;

        try {
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            ObjectOutput out = new ObjectOutputStream(buffer);
            out.writeObject(a1);
            out.close();

            ObjectInput in = new ObjectInputStream(
                    new ByteArrayInputStream(buffer.toByteArray()));
            a2 = (LogAxis) in.readObject();
            in.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        assertEquals(a1, a2);

    }

// org.jfree.chart.axis.junit.LogAxisTests::testAutoRange1
    public void testAutoRange1() {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        dataset.setValue(100.0, "Row 1", "Column 1");
        dataset.setValue(200.0, "Row 1", "Column 2");
        JFreeChart chart = ChartFactory.createBarChart(
            "Test", 
            "Categories",
            "Value",
            dataset,
            PlotOrientation.VERTICAL,
            false, 
            false,
            false
        );
        CategoryPlot plot = (CategoryPlot) chart.getPlot();
        LogAxis axis = new LogAxis("Log(Y)");
        plot.setRangeAxis(axis);
        assertEquals(0.0, axis.getLowerBound(), EPSILON);    
        assertEquals(2.6066426411261268E7, axis.getUpperBound(), EPSILON); 
    }

// org.jfree.chart.axis.junit.LogAxisTests::testAutoRange3
    public void testAutoRange3() {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        dataset.setValue(100.0, "Row 1", "Column 1");
        dataset.setValue(200.0, "Row 1", "Column 2");
        JFreeChart chart = ChartFactory.createLineChart("Test", "Categories",
                "Value", dataset, PlotOrientation.VERTICAL, false, false,
                false);
        CategoryPlot plot = (CategoryPlot) chart.getPlot();
        LogAxis axis = new LogAxis("Log(Y)");
        plot.setRangeAxis(axis);
        assertEquals(96.59363289248458, axis.getLowerBound(), EPSILON);    
        assertEquals(207.0529847682752, axis.getUpperBound(), EPSILON);    
        
        
        DefaultCategoryDataset dataset2 = new DefaultCategoryDataset();
        dataset2.setValue(900.0, "Row 1", "Column 1");
        dataset2.setValue(1000.0, "Row 1", "Column 2");
        plot.setDataset(dataset2);
        assertEquals(895.2712433374774, axis.getLowerBound(), EPSILON);    
        assertEquals(1005.2819262292991, axis.getUpperBound(), EPSILON);    
    }

// org.jfree.chart.axis.junit.LogAxisTests::testXYAutoRange1
    public void testXYAutoRange1() {
        XYSeries series = new XYSeries("Series 1");
        series.add(1.0, 1.0);
        series.add(2.0, 2.0);
        series.add(3.0, 3.0);
        XYSeriesCollection dataset = new XYSeriesCollection();
        dataset.addSeries(series);
        JFreeChart chart = ChartFactory.createScatterPlot(
            "Test", 
            "X",
            "Y",
            dataset,
            PlotOrientation.VERTICAL,
            false, 
            false,
            false
        );
        XYPlot plot = (XYPlot) chart.getPlot();
        LogAxis axis = new LogAxis("Log(Y)");
        plot.setRangeAxis(axis);
        assertEquals(0.9465508226401592, axis.getLowerBound(), EPSILON);    
        assertEquals(3.1694019256486126, axis.getUpperBound(), EPSILON);    
    }

// org.jfree.chart.axis.junit.LogAxisTests::testXYAutoRange2
    public void testXYAutoRange2() {
        XYSeries series = new XYSeries("Series 1");
        series.add(1.0, 1.0);
        series.add(2.0, 2.0);
        series.add(3.0, 3.0);
        XYSeriesCollection dataset = new XYSeriesCollection();
        dataset.addSeries(series);
        JFreeChart chart = ChartFactory.createScatterPlot(
            "Test", 
            "X",
            "Y",
            dataset,
            PlotOrientation.VERTICAL,
            false, 
            false,
            false
        );
        XYPlot plot = (XYPlot) chart.getPlot();
        LogAxis axis = new LogAxis("Log(Y)");
        plot.setRangeAxis(axis);
        assertEquals(0.9465508226401592, axis.getLowerBound(), EPSILON);    
        assertEquals(3.1694019256486126, axis.getUpperBound(), EPSILON);    
    }

// org.jfree.chart.axis.junit.LogAxisTests::testSetLowerBound
    public void testSetLowerBound() {
        LogAxis axis = new LogAxis("X");
        axis.setRange(0.0, 10.0);
        axis.setLowerBound(5.0);
        assertEquals(5.0, axis.getLowerBound(), EPSILON);
        axis.setLowerBound(10.0);
        assertEquals(10.0, axis.getLowerBound(), EPSILON);
        assertEquals(11.0, axis.getUpperBound(), EPSILON);
    }

// org.jfree.chart.axis.junit.LogAxisTests::testTickMarksVisibleDefault
    public void testTickMarksVisibleDefault() {
        LogAxis axis = new LogAxis("Log Axis");
        assertTrue(axis.isTickMarksVisible());
    }

// org.jfree.chart.axis.junit.NumberAxisTests::testCloning
    public void testCloning() {
        NumberAxis a1 = new NumberAxis("Test");
        NumberAxis a2 = null;
        try {
            a2 = (NumberAxis) a1.clone();
        }
        catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        assertTrue(a1 != a2);
        assertTrue(a1.getClass() == a2.getClass());
        assertTrue(a1.equals(a2));
    }

// org.jfree.chart.axis.junit.NumberAxisTests::testEquals
    public void testEquals() {
        
        NumberAxis a1 = new NumberAxis("Test");
        NumberAxis a2 = new NumberAxis("Test");
        assertTrue(a1.equals(a2));
        
        
        a1.setAutoRangeIncludesZero(false);
        assertFalse(a1.equals(a2));
        a2.setAutoRangeIncludesZero(false);
        assertTrue(a1.equals(a2));

        
        a1.setAutoRangeStickyZero(false);
        assertFalse(a1.equals(a2));
        a2.setAutoRangeStickyZero(false);
        assertTrue(a1.equals(a2));

        
        a1.setTickUnit(new NumberTickUnit(25.0));
        assertFalse(a1.equals(a2));
        a2.setTickUnit(new NumberTickUnit(25.0));
        assertTrue(a1.equals(a2));

        
        a1.setNumberFormatOverride(new DecimalFormat("0.00"));
        assertFalse(a1.equals(a2));
        a2.setNumberFormatOverride(new DecimalFormat("0.00"));
        assertTrue(a1.equals(a2));
        
        a1.setRangeType(RangeType.POSITIVE);
        assertFalse(a1.equals(a2));
        a2.setRangeType(RangeType.POSITIVE);
        assertTrue(a1.equals(a2));
        
    }

// org.jfree.chart.axis.junit.NumberAxisTests::testHashCode
    public void testHashCode() {
        NumberAxis a1 = new NumberAxis("Test");
        NumberAxis a2 = new NumberAxis("Test");
        assertTrue(a1.equals(a2));
        int h1 = a1.hashCode();
        int h2 = a2.hashCode();
        assertEquals(h1, h2);
    }

// org.jfree.chart.axis.junit.NumberAxisTests::testTranslateJava2DToValue
    public void testTranslateJava2DToValue() {
        NumberAxis axis = new NumberAxis();
        axis.setRange(50.0, 100.0); 
        Rectangle2D dataArea = new Rectangle2D.Double(10.0, 50.0, 400.0, 300.0);
        double y1 = axis.java2DToValue(75.0, dataArea, RectangleEdge.LEFT);  
        assertEquals(y1, 95.8333333, EPSILON); 
        double y2 = axis.java2DToValue(75.0, dataArea, RectangleEdge.RIGHT);   
        assertEquals(y2, 95.8333333, EPSILON); 
        double x1 = axis.java2DToValue(75.0, dataArea, RectangleEdge.TOP);   
        assertEquals(x1, 58.125, EPSILON); 
        double x2 = axis.java2DToValue(75.0, dataArea, RectangleEdge.BOTTOM);   
        assertEquals(x2, 58.125, EPSILON); 
        axis.setInverted(true);
        double y3 = axis.java2DToValue(75.0, dataArea, RectangleEdge.LEFT);  
        assertEquals(y3, 54.1666667, EPSILON); 
        double y4 = axis.java2DToValue(75.0, dataArea, RectangleEdge.RIGHT);   
        assertEquals(y4, 54.1666667, EPSILON); 
        double x3 = axis.java2DToValue(75.0, dataArea, RectangleEdge.TOP);   
        assertEquals(x3, 91.875, EPSILON); 
        double x4 = axis.java2DToValue(75.0, dataArea, RectangleEdge.BOTTOM);   
        assertEquals(x4, 91.875, EPSILON); 
    }

// org.jfree.chart.axis.junit.NumberAxisTests::testSerialization
    public void testSerialization() {

        NumberAxis a1 = new NumberAxis("Test Axis");
        NumberAxis a2 = null;

        try {
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            ObjectOutput out = new ObjectOutputStream(buffer);
            out.writeObject(a1);
            out.close();

            ObjectInput in = new ObjectInputStream(
                    new ByteArrayInputStream(buffer.toByteArray()));
            a2 = (NumberAxis) in.readObject();
            in.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        assertEquals(a1, a2);

    }

// org.jfree.chart.axis.junit.NumberAxisTests::testAutoRange1
    public void testAutoRange1() {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        dataset.setValue(100.0, "Row 1", "Column 1");
        dataset.setValue(200.0, "Row 1", "Column 2");
        JFreeChart chart = ChartFactory.createBarChart(
            "Test", 
            "Categories",
            "Value",
            dataset,
            PlotOrientation.VERTICAL,
            false, 
            false,
            false
        );
        CategoryPlot plot = (CategoryPlot) chart.getPlot();
        NumberAxis axis = (NumberAxis) plot.getRangeAxis();
        assertEquals(axis.getLowerBound(), 0.0, EPSILON);    
        assertEquals(axis.getUpperBound(), 210.0, EPSILON);    
    }

// org.jfree.chart.axis.junit.NumberAxisTests::testAutoRange2
    public void testAutoRange2() {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        dataset.setValue(100.0, "Row 1", "Column 1");
        dataset.setValue(200.0, "Row 1", "Column 2");
        JFreeChart chart = ChartFactory.createLineChart("Test", "Categories",
                "Value", dataset, PlotOrientation.VERTICAL, false, false,
                false);
        CategoryPlot plot = (CategoryPlot) chart.getPlot();
        NumberAxis axis = (NumberAxis) plot.getRangeAxis();
        axis.setAutoRangeIncludesZero(false);
        assertEquals(axis.getLowerBound(), 95.0, EPSILON);    
        assertEquals(axis.getUpperBound(), 205.0, EPSILON);    
    }

// org.jfree.chart.axis.junit.NumberAxisTests::testAutoRange3
    public void testAutoRange3() {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        dataset.setValue(100.0, "Row 1", "Column 1");
        dataset.setValue(200.0, "Row 1", "Column 2");
        JFreeChart chart = ChartFactory.createLineChart("Test", "Categories",
                "Value", dataset, PlotOrientation.VERTICAL, false, false,
                false);
        CategoryPlot plot = (CategoryPlot) chart.getPlot();
        NumberAxis axis = (NumberAxis) plot.getRangeAxis();
        axis.setAutoRangeIncludesZero(false);
        assertEquals(axis.getLowerBound(), 95.0, EPSILON);    
        assertEquals(axis.getUpperBound(), 205.0, EPSILON);    
        
        
        DefaultCategoryDataset dataset2 = new DefaultCategoryDataset();
        dataset2.setValue(900.0, "Row 1", "Column 1");
        dataset2.setValue(1000.0, "Row 1", "Column 2");
        plot.setDataset(dataset2);
        assertEquals(axis.getLowerBound(), 895.0, EPSILON);    
        assertEquals(axis.getUpperBound(), 1005.0, EPSILON);    
    }

// org.jfree.chart.axis.junit.NumberAxisTests::testAutoRange4
    public void testAutoRange4() {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        dataset.setValue(100.0, "Row 1", "Column 1");
        dataset.setValue(200.0, "Row 1", "Column 2");
        JFreeChart chart = ChartFactory.createBarChart("Test", "Categories",
                "Value", dataset, PlotOrientation.VERTICAL, false, false,
                false);
        CategoryPlot plot = (CategoryPlot) chart.getPlot();
        NumberAxis axis = (NumberAxis) plot.getRangeAxis();
        axis.setAutoRangeIncludesZero(false);
        BarRenderer br = (BarRenderer) plot.getRenderer();
        br.setIncludeBaseInRange(false);
        assertEquals(95.0, axis.getLowerBound(), EPSILON);    
        assertEquals(205.0, axis.getUpperBound(), EPSILON);    
        
        br.setIncludeBaseInRange(true);
        assertEquals(0.0, axis.getLowerBound(), EPSILON);    
        assertEquals(210.0, axis.getUpperBound(), EPSILON);    
        
        axis.setAutoRangeIncludesZero(true);
        assertEquals(0.0, axis.getLowerBound(), EPSILON);    
        assertEquals(210.0, axis.getUpperBound(), EPSILON);    
        
        br.setIncludeBaseInRange(true);
        assertEquals(0.0, axis.getLowerBound(), EPSILON);    
        assertEquals(210.0, axis.getUpperBound(), EPSILON);    

        
        DefaultCategoryDataset dataset2 = new DefaultCategoryDataset();
        dataset2.setValue(900.0, "Row 1", "Column 1");
        dataset2.setValue(1000.0, "Row 1", "Column 2");
        plot.setDataset(dataset2);
        assertEquals(0.0, axis.getLowerBound(), EPSILON);    
        assertEquals(1050.0, axis.getUpperBound(), EPSILON);
        
        br.setIncludeBaseInRange(false);
        assertEquals(0.0, axis.getLowerBound(), EPSILON);    
        assertEquals(1050.0, axis.getUpperBound(), EPSILON);
        
        axis.setAutoRangeIncludesZero(false);
        assertEquals(895.0, axis.getLowerBound(), EPSILON);    
        assertEquals(1005.0, axis.getUpperBound(), EPSILON);        
    }

// org.jfree.chart.axis.junit.NumberAxisTests::testXYAutoRange1
    public void testXYAutoRange1() {
        XYSeries series = new XYSeries("Series 1");
        series.add(1.0, 1.0);
        series.add(2.0, 2.0);
        series.add(3.0, 3.0);
        XYSeriesCollection dataset = new XYSeriesCollection();
        dataset.addSeries(series);
        JFreeChart chart = ChartFactory.createScatterPlot(
            "Test", 
            "X",
            "Y",
            dataset,
            PlotOrientation.VERTICAL,
            false, 
            false,
            false
        );
        XYPlot plot = (XYPlot) chart.getPlot();
        NumberAxis axis = (NumberAxis) plot.getDomainAxis();
        axis.setAutoRangeIncludesZero(false);
        assertEquals(0.9, axis.getLowerBound(), EPSILON);    
        assertEquals(3.1, axis.getUpperBound(), EPSILON);    
    }

// org.jfree.chart.axis.junit.NumberAxisTests::testXYAutoRange2
    public void testXYAutoRange2() {
        XYSeries series = new XYSeries("Series 1");
        series.add(1.0, 1.0);
        series.add(2.0, 2.0);
        series.add(3.0, 3.0);
        XYSeriesCollection dataset = new XYSeriesCollection();
        dataset.addSeries(series);
        JFreeChart chart = ChartFactory.createScatterPlot(
            "Test", 
            "X",
            "Y",
            dataset,
            PlotOrientation.VERTICAL,
            false, 
            false,
            false
        );
        XYPlot plot = (XYPlot) chart.getPlot();
        NumberAxis axis = (NumberAxis) plot.getRangeAxis();
        axis.setAutoRangeIncludesZero(false);
        assertEquals(0.9, axis.getLowerBound(), EPSILON);    
        assertEquals(3.1, axis.getUpperBound(), EPSILON);    
    }

// org.jfree.chart.axis.junit.NumberAxisTests::testSetLowerBound
    public void testSetLowerBound() {
        NumberAxis axis = new NumberAxis("X");
        axis.setRange(0.0, 10.0);
        axis.setLowerBound(5.0);
        assertEquals(5.0, axis.getLowerBound(), EPSILON);
        axis.setLowerBound(10.0);
        assertEquals(10.0, axis.getLowerBound(), EPSILON);
        assertEquals(11.0, axis.getUpperBound(), EPSILON);
        
        
        
        
    }

// org.jfree.chart.axis.junit.ValueAxisTests::testCloning
    public void testCloning() {
        ValueAxis a1 = new NumberAxis("Test");
        ValueAxis a2 = null;
        try {
            a2 = (NumberAxis) a1.clone();
        }
        catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        assertTrue(a1 != a2);
        assertTrue(a1.getClass() == a2.getClass());
        assertTrue(a1.equals(a2));
    }

// org.jfree.chart.axis.junit.ValueAxisTests::testEquals
    public void testEquals() {
        
        NumberAxis a1 = new NumberAxis("Test");
        NumberAxis a2 = new NumberAxis("Test");
        assertTrue(a1.equals(a2));
        
        
        a1.setAxisLineVisible(false);
        assertFalse(a1.equals(a2));
        a2.setAxisLineVisible(false);
        assertTrue(a1.equals(a2));
    
        
        a1.setPositiveArrowVisible(true);
        assertFalse(a1.equals(a2));
        a2.setPositiveArrowVisible(true);
        assertTrue(a1.equals(a2));
    
        
        a1.setNegativeArrowVisible(true);
        assertFalse(a1.equals(a2));
        a2.setNegativeArrowVisible(true);
        assertTrue(a1.equals(a2));
    
        
    
        
    
        
    
        
    
        
        a1.setAxisLinePaint(Color.blue);
        assertFalse(a1.equals(a2));
        a2.setAxisLinePaint(Color.blue);
        assertTrue(a1.equals(a2));
        
        
        Stroke stroke = new BasicStroke(2.0f);
        a1.setAxisLineStroke(stroke);
        assertFalse(a1.equals(a2));
        a2.setAxisLineStroke(stroke);
        assertTrue(a1.equals(a2));
    
        
        a1.setInverted(true);
        assertFalse(a1.equals(a2));
        a2.setInverted(true);
        assertTrue(a1.equals(a2));
        
        
        a1.setRange(new Range(50.0, 75.0));
        assertFalse(a1.equals(a2));
        a2.setRange(new Range(50.0, 75.0));
        assertTrue(a1.equals(a2));

        
        a1.setAutoRange(true);
        assertFalse(a1.equals(a2));
        a2.setAutoRange(true);
        assertTrue(a1.equals(a2));

        
        a1.setAutoRangeMinimumSize(3.33);
        assertFalse(a1.equals(a2));
        a2.setAutoRangeMinimumSize(3.33);
        assertTrue(a1.equals(a2));

        a1.setDefaultAutoRange(new Range(1.2, 3.4));
        assertFalse(a1.equals(a2));
        a2.setDefaultAutoRange(new Range(1.2, 3.4));
        assertTrue(a1.equals(a2));
        
        
        a1.setUpperMargin(0.09);
        assertFalse(a1.equals(a2));
        a2.setUpperMargin(0.09);
        assertTrue(a1.equals(a2));

        
        a1.setLowerMargin(0.09);
        assertFalse(a1.equals(a2));
        a2.setLowerMargin(0.09);
        assertTrue(a1.equals(a2));

        
        a1.setFixedAutoRange(50.0);
        assertFalse(a1.equals(a2));
        a2.setFixedAutoRange(50.0);
        assertTrue(a1.equals(a2));

        
        a1.setAutoTickUnitSelection(false);
        assertFalse(a1.equals(a2));
        a2.setAutoTickUnitSelection(false);
        assertTrue(a1.equals(a2));

        
        a1.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
        assertFalse(a1.equals(a2));
        a2.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
        assertTrue(a1.equals(a2));

        
        a1.setVerticalTickLabels(true);
        assertFalse(a1.equals(a2));
        a2.setVerticalTickLabels(true);
        assertTrue(a1.equals(a2));

        
        
        
    
    }

// org.jfree.chart.axis.junit.ValueAxisTests::testAxisMargins
    public void testAxisMargins() {
        XYSeries series = new XYSeries("S1");
        series.add(100.0, 1.1);
        series.add(200.0, 2.2);
        XYSeriesCollection dataset = new XYSeriesCollection(series);
        dataset.setIntervalWidth(0.0);
        JFreeChart chart = ChartFactory.createScatterPlot(
            "Title", "X", "Y", dataset, PlotOrientation.VERTICAL, 
            false, false, false
        );
        ValueAxis domainAxis = ((XYPlot) chart.getPlot()).getDomainAxis();
        Range r = domainAxis.getRange();
        assertEquals(110.0, r.getLength(), EPSILON);
        domainAxis.setLowerMargin(0.10);
        domainAxis.setUpperMargin(0.10);
        r = domainAxis.getRange();
        assertEquals(120.0, r.getLength(), EPSILON);
    }

// org.jfree.chart.junit.AreaChartTests::testSetSeriesToolTipGenerator
    public void testSetSeriesToolTipGenerator() {
        CategoryPlot plot = (CategoryPlot) this.chart.getPlot();
        CategoryItemRenderer renderer = plot.getRenderer();
        StandardCategoryToolTipGenerator tt
                = new StandardCategoryToolTipGenerator();
        renderer.setSeriesToolTipGenerator(0, tt);
        CategoryToolTipGenerator tt2 = renderer.getToolTipGenerator(0, 0);
        assertTrue(tt2 == tt);
    }

// org.jfree.chart.junit.AreaChartTests::testSetSeriesURLGenerator
    public void testSetSeriesURLGenerator() {
        CategoryPlot plot = (CategoryPlot) this.chart.getPlot();
        CategoryItemRenderer renderer = plot.getRenderer();
        StandardCategoryURLGenerator url1
                = new StandardCategoryURLGenerator();
        renderer.setSeriesURLGenerator(0, url1);
        CategoryURLGenerator url2 = renderer.getURLGenerator(0, 0);
        assertTrue(url2 == url1);
    }

// org.jfree.chart.junit.AreaChartTests::testDrawWithNullInfo
    public void testDrawWithNullInfo() {
        boolean success = false;
        try {
            BufferedImage image = new BufferedImage(200 , 100, 
                    BufferedImage.TYPE_INT_RGB);
            Graphics2D g2 = image.createGraphics();
            this.chart.draw(g2, new Rectangle2D.Double(0, 0, 200, 100), null, 
                    null);
            g2.dispose();
            success = true;
        }
        catch (Exception e) {
            success = false;
        }
        assertTrue(success);
    }

// org.jfree.chart.junit.AreaChartTests::testReplaceDataset
    public void testReplaceDataset() {
        Number[][] data = new Integer[][]
            {{new Integer(-30), new Integer(-20)},
             {new Integer(-10), new Integer(10)},
             {new Integer(20), new Integer(30)}};

        CategoryDataset newData = DatasetUtilities.createCategoryDataset(
                "S", "C", data);
        LocalListener l = new LocalListener();
        this.chart.addChangeListener(l);
        CategoryPlot plot = (CategoryPlot) this.chart.getPlot();
        plot.setDataset(newData);
        assertEquals(true, l.flag);
        ValueAxis axis = plot.getRangeAxis();
        Range range = axis.getRange();
        assertTrue("Expecting the lower bound of the range to be around -30: "
                   + range.getLowerBound(), range.getLowerBound() <= -30);
        assertTrue("Expecting the upper bound of the range to be around 30: "
                   + range.getUpperBound(), range.getUpperBound() >= 30);

    }

// org.jfree.chart.junit.BarChart3DTests::testDrawWithNullInfo
    public void testDrawWithNullInfo() {
        boolean success = false;
        try {
            BufferedImage image = new BufferedImage(200 , 100, 
                    BufferedImage.TYPE_INT_RGB);
            Graphics2D g2 = image.createGraphics();
            this.chart.draw(g2, new Rectangle2D.Double(0, 0, 200, 100), null, 
                    null);
            g2.dispose();
            success = true;
        }
        catch (Exception e) {
            success = false;
        }
        assertTrue(success);
    }

// org.jfree.chart.junit.BarChart3DTests::testReplaceDataset
    public void testReplaceDataset() {

        
        Number[][] data = new Integer[][]
            {{new Integer(-30), new Integer(-20)},
             {new Integer(-10), new Integer(10)},
             {new Integer(20), new Integer(30)}};

        CategoryDataset newData = DatasetUtilities.createCategoryDataset("S", 
                "C", data);

        LocalListener l = new LocalListener();
        this.chart.addChangeListener(l);
        CategoryPlot plot = (CategoryPlot) this.chart.getPlot();
        plot.setDataset(newData);
        assertEquals(true, l.flag);
        ValueAxis axis = plot.getRangeAxis();
        Range range = axis.getRange();
        assertTrue("Expecting the lower bound of the range to be around -30: "
                + range.getLowerBound(), range.getLowerBound() <= -30);
        assertTrue("Expecting the upper bound of the range to be around 30: "
                + range.getUpperBound(), range.getUpperBound() >= 30);

    }

// org.jfree.chart.junit.BarChart3DTests::testSetSeriesToolTipGenerator
    public void testSetSeriesToolTipGenerator() {
        CategoryPlot plot = (CategoryPlot) this.chart.getPlot();
        CategoryItemRenderer renderer = plot.getRenderer();
        StandardCategoryToolTipGenerator tt
                = new StandardCategoryToolTipGenerator();
        renderer.setSeriesToolTipGenerator(0, tt);
        CategoryToolTipGenerator tt2 = renderer.getToolTipGenerator(0, 0);
        assertTrue(tt2 == tt);
    }

// org.jfree.chart.junit.BarChart3DTests::testSetSeriesURLGenerator
    public void testSetSeriesURLGenerator() {
        CategoryPlot plot = (CategoryPlot) this.chart.getPlot();
        CategoryItemRenderer renderer = plot.getRenderer();
        StandardCategoryURLGenerator url1
                = new StandardCategoryURLGenerator();
        renderer.setSeriesURLGenerator(0, url1);
        CategoryURLGenerator url2 = renderer.getURLGenerator(0, 0);
        assertTrue(url2 == url1);
    }

// org.jfree.chart.junit.BarChartTests::testDrawWithNullInfo
    public void testDrawWithNullInfo() {

        boolean success = false;

        try {
            BufferedImage image = new BufferedImage(200 , 100, 
                    BufferedImage.TYPE_INT_RGB);
            Graphics2D g2 = image.createGraphics();
            this.chart.draw(g2, new Rectangle2D.Double(0, 0, 200, 100), null, 
                    null);
            g2.dispose();
            success = true;
        }
        catch (Exception e) {
            success = false;
        }

        assertTrue(success);

    }

// org.jfree.chart.junit.BarChartTests::testReplaceDataset
    public void testReplaceDataset() {

        
        Number[][] data = new Integer[][]
            {{new Integer(-30), new Integer(-20)},
             {new Integer(-10), new Integer(10)},
             {new Integer(20), new Integer(30)}};

        CategoryDataset newData = DatasetUtilities.createCategoryDataset("S", 
                "C", data);

        LocalListener l = new LocalListener();
        this.chart.addChangeListener(l);
        CategoryPlot plot = (CategoryPlot) this.chart.getPlot();
        plot.setDataset(newData);
        assertEquals(true, l.flag);
        ValueAxis axis = plot.getRangeAxis();
        Range range = axis.getRange();
        assertTrue("Expecting the lower bound of the range to be around -30: "
                   + range.getLowerBound(), range.getLowerBound() <= -30);
        assertTrue("Expecting the upper bound of the range to be around 30: "
                   + range.getUpperBound(), range.getUpperBound() >= 30);

    }

// org.jfree.chart.junit.BarChartTests::testSetSeriesToolTipGenerator
    public void testSetSeriesToolTipGenerator() {
        CategoryPlot plot = (CategoryPlot) this.chart.getPlot();
        CategoryItemRenderer renderer = plot.getRenderer();
        StandardCategoryToolTipGenerator tt
                = new StandardCategoryToolTipGenerator();
        renderer.setSeriesToolTipGenerator(0, tt);
        CategoryToolTipGenerator tt2 = renderer.getToolTipGenerator(0, 0);
        assertTrue(tt2 == tt);
    }

// org.jfree.chart.junit.BarChartTests::testSetSeriesURLGenerator
    public void testSetSeriesURLGenerator() {
        CategoryPlot plot = (CategoryPlot) this.chart.getPlot();
        CategoryItemRenderer renderer = plot.getRenderer();
        StandardCategoryURLGenerator url1
                = new StandardCategoryURLGenerator();
        renderer.setSeriesURLGenerator(0, url1);
        CategoryURLGenerator url2 = renderer.getURLGenerator(0, 0);
        assertTrue(url2 == url1);
    }

// org.jfree.chart.junit.GanttChartTests::testDrawWithNullInfo
    public void testDrawWithNullInfo() {
        boolean success = false;
        try {
            BufferedImage image = new BufferedImage(200 , 100, 
                    BufferedImage.TYPE_INT_RGB);
            Graphics2D g2 = image.createGraphics();
            this.chart.draw(g2, new Rectangle2D.Double(0, 0, 200, 100), null, 
                    null);
            g2.dispose();
            success = true;
        }
        catch (Exception e) {
            success = false;
        }
        assertTrue(success);
    }

// org.jfree.chart.junit.GanttChartTests::testDrawWithNullInfo2
    public void testDrawWithNullInfo2() {
        boolean success = false;
        try {
            JFreeChart chart = createGanttChart();
            CategoryPlot plot = (CategoryPlot) chart.getPlot();
            plot.setDataset(createDataset());
             chart.createBufferedImage(300, 200, null);
            success = true;
        }
        catch (NullPointerException e) {
            success = false;
        }
        assertTrue(success);
    }

// org.jfree.chart.junit.GanttChartTests::testReplaceDataset
    public void testReplaceDataset() {
        LocalListener l = new LocalListener();
        this.chart.addChangeListener(l);
        CategoryPlot plot = (CategoryPlot) this.chart.getPlot();
        plot.setDataset(null);
        assertEquals(true, l.flag);
    }

// org.jfree.chart.junit.GanttChartTests::testSetSeriesToolTipGenerator
    public void testSetSeriesToolTipGenerator() {
        CategoryPlot plot = (CategoryPlot) this.chart.getPlot();
        CategoryItemRenderer renderer = plot.getRenderer();
        StandardCategoryToolTipGenerator tt
                = new StandardCategoryToolTipGenerator();
        renderer.setSeriesToolTipGenerator(0, tt);
        CategoryToolTipGenerator tt2 = renderer.getToolTipGenerator(0, 0);
        assertTrue(tt2 == tt);
    }

// org.jfree.chart.junit.GanttChartTests::testSetSeriesURLGenerator
    public void testSetSeriesURLGenerator() {
        CategoryPlot plot = (CategoryPlot) this.chart.getPlot();
        CategoryItemRenderer renderer = plot.getRenderer();
        StandardCategoryURLGenerator url1
                = new StandardCategoryURLGenerator();
        renderer.setSeriesURLGenerator(0, url1);
        CategoryURLGenerator url2 = renderer.getURLGenerator(0, 0);
        assertTrue(url2 == url1);
    }

// org.jfree.chart.junit.JFreeChartTests::testEquals
    public void testEquals() {
        JFreeChart chart1 = new JFreeChart("Title", 
                new Font("SansSerif", Font.PLAIN, 12), new PiePlot(), true);
        JFreeChart chart2 = new JFreeChart("Title", 
                new Font("SansSerif", Font.PLAIN, 12), new PiePlot(), true);
        assertTrue(chart1.equals(chart2));
        assertTrue(chart2.equals(chart1));
        
        
        chart1.setRenderingHints(new RenderingHints(
                RenderingHints.KEY_TEXT_ANTIALIASING, 
                RenderingHints.VALUE_TEXT_ANTIALIAS_ON));
        assertFalse(chart1.equals(chart2));
        chart2.setRenderingHints(new RenderingHints(
                RenderingHints.KEY_TEXT_ANTIALIASING, 
                RenderingHints.VALUE_TEXT_ANTIALIAS_ON));
        assertTrue(chart1.equals(chart2));
        
        
        chart1.setBorderVisible(true);
        assertFalse(chart1.equals(chart2));
        chart2.setBorderVisible(true);
        assertTrue(chart1.equals(chart2));
        
        
        BasicStroke s = new BasicStroke(2.0f);
        chart1.setBorderStroke(s);
        assertFalse(chart1.equals(chart2));
        chart2.setBorderStroke(s);
        assertTrue(chart1.equals(chart2));
        
        
        chart1.setBorderPaint(Color.red);
        assertFalse(chart1.equals(chart2));
        chart2.setBorderPaint(Color.red);
        assertTrue(chart1.equals(chart2));
        
        
        chart1.setPadding(new RectangleInsets(1, 2, 3, 4));
        assertFalse(chart1.equals(chart2));
        chart2.setPadding(new RectangleInsets(1, 2, 3, 4));
        assertTrue(chart1.equals(chart2));
        
        
        chart1.setTitle("XYZ");
        assertFalse(chart1.equals(chart2));
        chart2.setTitle("XYZ");
        assertTrue(chart1.equals(chart2));
        
        
        chart1.addSubtitle(new TextTitle("Subtitle"));
        assertFalse(chart1.equals(chart2));
        chart2.addSubtitle(new TextTitle("Subtitle"));
        assertTrue(chart1.equals(chart2));
        
        
        chart1 = new JFreeChart("Title", 
                new Font("SansSerif", Font.PLAIN, 12), new RingPlot(), false);
        chart2 = new JFreeChart("Title", 
                new Font("SansSerif", Font.PLAIN, 12), new PiePlot(), false);
        assertFalse(chart1.equals(chart2));
        chart2 = new JFreeChart("Title", 
                new Font("SansSerif", Font.PLAIN, 12), new RingPlot(), false);
        assertTrue(chart1.equals(chart2));
        
        
        chart1.setBackgroundPaint(new GradientPaint(1.0f, 2.0f, Color.red, 
                3.0f, 4.0f, Color.blue));
        assertFalse(chart1.equals(chart2));
        chart2.setBackgroundPaint(new GradientPaint(1.0f, 2.0f, Color.red, 
                3.0f, 4.0f, Color.blue));
        assertTrue(chart1.equals(chart2));
        
        
        chart1.setBackgroundImage(JFreeChart.INFO.getLogo());
        assertFalse(chart1.equals(chart2));
        chart2.setBackgroundImage(JFreeChart.INFO.getLogo());
        assertTrue(chart1.equals(chart2));
        
        
        chart1.setBackgroundImageAlignment(Align.BOTTOM_LEFT);
        assertFalse(chart1.equals(chart2));
        chart2.setBackgroundImageAlignment(Align.BOTTOM_LEFT);
        assertTrue(chart1.equals(chart2));
        
        
        chart1.setBackgroundImageAlpha(0.1f);
        assertFalse(chart1.equals(chart2));
        chart2.setBackgroundImageAlpha(0.1f);
        assertTrue(chart1.equals(chart2));
    }

// org.jfree.chart.junit.JFreeChartTests::testEquals2
    public void testEquals2() {
        JFreeChart chart1 = new JFreeChart("Title", 
                new Font("SansSerif", Font.PLAIN, 12), new PiePlot(), true);
        JFreeChart chart2 = new JFreeChart("Title", 
                new Font("SansSerif", Font.PLAIN, 12), new PiePlot(), false);
        assertFalse(chart1.equals(chart2));
        assertFalse(chart2.equals(chart1));
    }

// org.jfree.chart.junit.JFreeChartTests::testSubtitleCount
    public void testSubtitleCount() {
        int count = this.pieChart.getSubtitleCount();
        assertEquals(1, count);
    }

// org.jfree.chart.junit.JFreeChartTests::testGetSubtitle
    public void testGetSubtitle() {
        DefaultPieDataset dataset = new DefaultPieDataset();
        JFreeChart chart = ChartFactory.createPieChart("title", dataset, true, 
                false, false);
        Title t = chart.getSubtitle(0);
        assertTrue(t instanceof LegendTitle);
        
        boolean pass = false;
        try {
            t = chart.getSubtitle(-1);
        }
        catch (IllegalArgumentException e) {
            pass = true;
        }
        assertTrue(pass);
        
        pass = false;
        try {
            t = chart.getSubtitle(1);
        }
        catch (IllegalArgumentException e) {
            pass = true;
        }
        assertTrue(pass);

        pass = false;
        try {
            t = chart.getSubtitle(2);
        }
        catch (IllegalArgumentException e) {
            pass = true;
        }
        assertTrue(pass);
    }

// org.jfree.chart.junit.JFreeChartTests::testSerialization1
    public void testSerialization1() {

        DefaultPieDataset data = new DefaultPieDataset();
        data.setValue("Type 1", 54.5);
        data.setValue("Type 2", 23.9);
        data.setValue("Type 3", 45.8);

        JFreeChart c1 = ChartFactory.createPieChart("Test", data, true, true, 
                true);
        JFreeChart c2 = null;

        try {
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            ObjectOutput out = new ObjectOutputStream(buffer);
            out.writeObject(c1);
            out.close();

            ObjectInput in = new ObjectInputStream(new ByteArrayInputStream(
                    buffer.toByteArray()));
            c2 = (JFreeChart) in.readObject();
            in.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        assertEquals(c1, c2);
        LegendTitle lt2 = c2.getLegend();
        assertTrue(lt2.getSources()[0] == c2.getPlot());
    }

// org.jfree.chart.junit.JFreeChartTests::testSerialization2
    public void testSerialization2() {

        DefaultPieDataset data = new DefaultPieDataset();
        data.setValue("Type 1", 54.5);
        data.setValue("Type 2", 23.9);
        data.setValue("Type 3", 45.8);

        JFreeChart c1 = ChartFactory.createPieChart3D("Test", data, true, true,
                true);
        JFreeChart c2 = null;

        try {
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            ObjectOutput out = new ObjectOutputStream(buffer);
            out.writeObject(c1);
            out.close();

            ObjectInput in = new ObjectInputStream(new ByteArrayInputStream(
                    buffer.toByteArray()));
            c2 = (JFreeChart) in.readObject();
            in.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        assertEquals(c1, c2);

    }

// org.jfree.chart.junit.JFreeChartTests::testSerialization3
    public void testSerialization3() {

        
        String series1 = "First";
        String series2 = "Second";
        String series3 = "Third";

        
        String category1 = "Category 1";
        String category2 = "Category 2";
        String category3 = "Category 3";
        String category4 = "Category 4";
        String category5 = "Category 5";
        String category6 = "Category 6";
        String category7 = "Category 7";
        String category8 = "Category 8";

        
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        dataset.addValue(1.0, series1, category1);
        dataset.addValue(4.0, series1, category2);
        dataset.addValue(3.0, series1, category3);
        dataset.addValue(5.0, series1, category4);
        dataset.addValue(5.0, series1, category5);
        dataset.addValue(7.0, series1, category6);
        dataset.addValue(7.0, series1, category7);
        dataset.addValue(8.0, series1, category8);

        dataset.addValue(5.0, series2, category1);
        dataset.addValue(7.0, series2, category2);
        dataset.addValue(6.0, series2, category3);
        dataset.addValue(8.0, series2, category4);
        dataset.addValue(4.0, series2, category5);
        dataset.addValue(4.0, series2, category6);
        dataset.addValue(2.0, series2, category7);
        dataset.addValue(1.0, series2, category8);

        dataset.addValue(4.0, series3, category1);
        dataset.addValue(3.0, series3, category2);
        dataset.addValue(2.0, series3, category3);
        dataset.addValue(3.0, series3, category4);
        dataset.addValue(6.0, series3, category5);
        dataset.addValue(3.0, series3, category6);
        dataset.addValue(4.0, series3, category7);
        dataset.addValue(3.0, series3, category8);

        
        JFreeChart c1 = ChartFactory.createBarChart(
            "Vertical Bar Chart",      
            "Category",                
            "Value",                   
            dataset,                   
            PlotOrientation.VERTICAL,  
            true,                      
            true,
            false
        );

        JFreeChart c2 = null;

        try {
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            ObjectOutput out = new ObjectOutputStream(buffer);
            out.writeObject(c1);
            out.close();

            ObjectInput in = new ObjectInputStream(new ByteArrayInputStream(
                    buffer.toByteArray()));
            c2 = (JFreeChart) in.readObject();
            in.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        assertEquals(c1, c2);

    }

// org.jfree.chart.junit.JFreeChartTests::testSerialization4
    public void testSerialization4() {

        RegularTimePeriod t = new Day();
        TimeSeries series = new TimeSeries("Series 1");
        series.add(t, 36.4);
        t = t.next();
        series.add(t, 63.5);
        TimeSeriesCollection dataset = new TimeSeriesCollection();
        dataset.addSeries(series);

        JFreeChart c1 = ChartFactory.createTimeSeriesChart("Test", "Date", 
                "Value", dataset, true, true, true);
        JFreeChart c2 = null;

        try {
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            ObjectOutput out = new ObjectOutputStream(buffer);
            out.writeObject(c1);
            out.close();

            ObjectInput in = new ObjectInputStream(new ByteArrayInputStream(
                    buffer.toByteArray()));
            c2 = (JFreeChart) in.readObject();
            in.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        assertEquals(c1, c2);

    }

// org.jfree.chart.junit.JFreeChartTests::testAddSubtitle
    public void testAddSubtitle() {
        DefaultPieDataset dataset = new DefaultPieDataset();
        JFreeChart chart = ChartFactory.createPieChart("title", dataset, true, 
                false, false);
        
        TextTitle t0 = new TextTitle("T0");
        chart.addSubtitle(0, t0);
        assertEquals(t0, chart.getSubtitle(0));
        
        TextTitle t1 = new TextTitle("T1");
        chart.addSubtitle(t1);
        assertEquals(t1, chart.getSubtitle(2));  

        boolean pass = false;
        try {
            chart.addSubtitle(null);
        }
        catch (IllegalArgumentException e) {
            pass = true;
        }
        assertTrue(pass);
        
        pass = false;
        try {
            chart.addSubtitle(-1, t0);
        }
        catch (IllegalArgumentException e) {
            pass = true;
        }
        assertTrue(pass);
            
        pass = false;
        try {
            chart.addSubtitle(4, t0);
        }
        catch (IllegalArgumentException e) {
            pass = true;
        }
        assertTrue(pass);
    }

// org.jfree.chart.junit.JFreeChartTests::testGetSubtitles
    public void testGetSubtitles() {
        DefaultPieDataset dataset = new DefaultPieDataset();
        JFreeChart chart = ChartFactory.createPieChart("title", dataset, true, 
                false, false);
        List subtitles = chart.getSubtitles();
        
        assertEquals(1, chart.getSubtitleCount());
        
        
        subtitles.add(new TextTitle("T"));
        assertEquals(1, chart.getSubtitleCount());
    }

// org.jfree.chart.junit.JFreeChartTests::testLegendEvents
    public void testLegendEvents() {
        DefaultPieDataset dataset = new DefaultPieDataset();
        JFreeChart chart = ChartFactory.createPieChart("title", dataset, true, 
                false, false);
        chart.addChangeListener(this);
        this.lastChartChangeEvent = null;
        LegendTitle legend = chart.getLegend();
        legend.setPosition(RectangleEdge.TOP);
        assertNotNull(this.lastChartChangeEvent);
    }

// org.jfree.chart.junit.JFreeChartTests::testTitleChangeEvent
    public void testTitleChangeEvent() {
        DefaultPieDataset dataset = new DefaultPieDataset();
        JFreeChart chart = ChartFactory.createPieChart("title", dataset, true, 
                false, false);
        chart.addChangeListener(this);
        this.lastChartChangeEvent = null;
        TextTitle t = chart.getTitle();
        t.setFont(new Font("Dialog", Font.BOLD, 9));
        assertNotNull(this.lastChartChangeEvent);
        this.lastChartChangeEvent = null;
        
        
        
        
        
        
        
        TextTitle t2 = new TextTitle("T2");
        chart.setTitle(t2);
        assertNotNull(this.lastChartChangeEvent);
        this.lastChartChangeEvent = null;
        
        t2.setFont(new Font("Dialog", Font.BOLD, 9));
        assertNotNull(this.lastChartChangeEvent);
        this.lastChartChangeEvent = null;
        
        t.setFont(new Font("Dialog", Font.BOLD, 9));
        assertNull(this.lastChartChangeEvent);
        this.lastChartChangeEvent = null;
    }

// org.jfree.chart.junit.LineChart3DTests::testDrawWithNullInfo
    public void testDrawWithNullInfo() {

        boolean success = false;
        try {
            BufferedImage image = new BufferedImage(200 , 100, 
                    BufferedImage.TYPE_INT_RGB);
            Graphics2D g2 = image.createGraphics();
            this.chart.draw(g2, new Rectangle2D.Double(0, 0, 200, 100), null, 
                    null);
            g2.dispose();
            success = true;
        }
        catch (Exception e) {
            success = false;
        }
        assertTrue(success);

    }

// org.jfree.chart.junit.LineChart3DTests::testReplaceDataset
    public void testReplaceDataset() {

        
        Number[][] data = new Integer[][]
            {{new Integer(-30), new Integer(-20)},
             {new Integer(-10), new Integer(10)},
             {new Integer(20), new Integer(30)}};

        CategoryDataset newData = DatasetUtilities.createCategoryDataset("S", 
                "C", data);

        LocalListener l = new LocalListener();
        this.chart.addChangeListener(l);
        CategoryPlot plot = (CategoryPlot) this.chart.getPlot();
        plot.setDataset(newData);
        assertEquals(true, l.flag);
        ValueAxis axis = plot.getRangeAxis();
        Range range = axis.getRange();
        assertTrue("Expecting the lower bound of the range to be around -30: "
                   + range.getLowerBound(), range.getLowerBound() <= -30);
        assertTrue("Expecting the upper bound of the range to be around 30: "
                   + range.getUpperBound(), range.getUpperBound() >= 30);

    }

// org.jfree.chart.junit.LineChart3DTests::testSetSeriesToolTipGenerator
    public void testSetSeriesToolTipGenerator() {
        CategoryPlot plot = (CategoryPlot) this.chart.getPlot();
        CategoryItemRenderer renderer = plot.getRenderer();
        StandardCategoryToolTipGenerator tt
                = new StandardCategoryToolTipGenerator();
        renderer.setSeriesToolTipGenerator(0, tt);
        CategoryToolTipGenerator tt2 = renderer.getToolTipGenerator(0, 0);
        assertTrue(tt2 == tt);
    }

// org.jfree.chart.junit.LineChart3DTests::testSetSeriesURLGenerator
    public void testSetSeriesURLGenerator() {
        CategoryPlot plot = (CategoryPlot) this.chart.getPlot();
        CategoryItemRenderer renderer = plot.getRenderer();
        StandardCategoryURLGenerator url1
                = new StandardCategoryURLGenerator();
        renderer.setSeriesURLGenerator(0, url1);
        CategoryURLGenerator url2 = renderer.getURLGenerator(0, 0);
        assertTrue(url2 == url1);
    }

// org.jfree.chart.junit.LineChartTests::testDrawWithNullInfo
    public void testDrawWithNullInfo() {

        boolean success = false;
        try {
            BufferedImage image = new BufferedImage(200 , 100, 
                    BufferedImage.TYPE_INT_RGB);
            Graphics2D g2 = image.createGraphics();
            this.chart.draw(g2, new Rectangle2D.Double(0, 0, 200, 100), null, 
                    null);
            g2.dispose();
            success = true;
        }
        catch (Exception e) {
            success = false;
        }
        assertTrue(success);

    }

// org.jfree.chart.junit.LineChartTests::testReplaceDataset
    public void testReplaceDataset() {

        
        Number[][] data = new Integer[][]
            {{new Integer(-30), new Integer(-20)},
             {new Integer(-10), new Integer(10)},
             {new Integer(20), new Integer(30)}};

        CategoryDataset newData = DatasetUtilities.createCategoryDataset("S", 
                "C", data);

        LocalListener l = new LocalListener();
        this.chart.addChangeListener(l);
        CategoryPlot plot = (CategoryPlot) this.chart.getPlot();
        plot.setDataset(newData);
        assertEquals(true, l.flag);
        ValueAxis axis = plot.getRangeAxis();
        Range range = axis.getRange();
        assertTrue("Expecting the lower bound of the range to be around -30: "
                   + range.getLowerBound(), range.getLowerBound() <= -30);
        assertTrue("Expecting the upper bound of the range to be around 30: "
                   + range.getUpperBound(), range.getUpperBound() >= 30);

    }

// org.jfree.chart.junit.LineChartTests::testSetSeriesToolTipGenerator
    public void testSetSeriesToolTipGenerator() {
        CategoryPlot plot = (CategoryPlot) this.chart.getPlot();
        CategoryItemRenderer renderer = plot.getRenderer();
        StandardCategoryToolTipGenerator tt
                = new StandardCategoryToolTipGenerator();
        renderer.setSeriesToolTipGenerator(0, tt);
        CategoryToolTipGenerator tt2 = renderer.getToolTipGenerator(0, 0);
        assertTrue(tt2 == tt);
    }

// org.jfree.chart.junit.LineChartTests::testSetSeriesURLGenerator
    public void testSetSeriesURLGenerator() {
        CategoryPlot plot = (CategoryPlot) this.chart.getPlot();
        CategoryItemRenderer renderer = plot.getRenderer();
        StandardCategoryURLGenerator url1
                = new StandardCategoryURLGenerator();
        renderer.setSeriesURLGenerator(0, url1);
        CategoryURLGenerator url2 = renderer.getURLGenerator(0, 0);
        assertTrue(url2 == url1);
    }

// org.jfree.chart.junit.PieChart3DTests::testReplaceDatasetOnPieChart
    public void testReplaceDatasetOnPieChart() {
        LocalListener l = new LocalListener();
        this.pieChart.addChangeListener(l);
        PiePlot plot = (PiePlot) this.pieChart.getPlot();
        plot.setDataset(null);
        assertEquals(true, l.flag);
        assertNull(plot.getDataset());
    }

// org.jfree.chart.junit.PieChart3DTests::testNullValueInDataset
    public void testNullValueInDataset() {
        DefaultPieDataset dataset = new DefaultPieDataset();
        dataset.setValue("Section 1", 10.0);
        dataset.setValue("Section 2", 11.0);
        dataset.setValue("Section 3", null);
        JFreeChart chart = createPieChart3D(dataset);
        boolean success = false;
        try {
            BufferedImage image = new BufferedImage(200 , 100, 
                    BufferedImage.TYPE_INT_RGB);
            Graphics2D g2 = image.createGraphics();
            chart.draw(g2, new Rectangle2D.Double(0, 0, 200, 100), null, null);
            g2.dispose();
            success = true;
        }
        catch (Throwable t) {
            success = false;
        }
        assertTrue(success);
    }

// org.jfree.chart.junit.PieChartTests::testReplaceDatasetOnPieChart
    public void testReplaceDatasetOnPieChart() {
        LocalListener l = new LocalListener();
        this.pieChart.addChangeListener(l);
        PiePlot plot = (PiePlot) this.pieChart.getPlot();
        plot.setDataset(null);
        assertEquals(true, l.flag);
        assertNull(plot.getDataset());
    }

// org.jfree.chart.junit.ScatterPlotTests::testDrawWithNullInfo
    public void testDrawWithNullInfo() {

        boolean success = false;

        try {
            BufferedImage image = new BufferedImage(200 , 100, 
                    BufferedImage.TYPE_INT_RGB);
            Graphics2D g2 = image.createGraphics();
            this.chart.draw(g2, new Rectangle2D.Double(0, 0, 200, 100), null, 
                    null);
            g2.dispose();
            success = true;
        }
        catch (Exception e) {
          success = false;
          e.printStackTrace();
        }

        assertTrue(success);

    }

// org.jfree.chart.junit.ScatterPlotTests::testReplaceDataset
    public void testReplaceDataset() {

        
        XYSeries series1 = new XYSeries("Series 1");
        series1.add(10.0, 10.0);
        series1.add(20.0, 20.0);
        series1.add(30.0, 30.0);
        XYDataset dataset = new XYSeriesCollection(series1);

        LocalListener l = new LocalListener();
        this.chart.addChangeListener(l);
        XYPlot plot = (XYPlot) this.chart.getPlot();
        plot.setDataset(dataset);
        assertEquals(true, l.flag);
        ValueAxis axis = plot.getRangeAxis();
        Range range = axis.getRange();
        assertTrue("Expecting the lower bound of the range to be around 10: "
                   + range.getLowerBound(), range.getLowerBound() <= 10);
        assertTrue("Expecting the upper bound of the range to be around 30: "
                   + range.getUpperBound(), range.getUpperBound() >= 30);

    }

// org.jfree.chart.junit.ScatterPlotTests::testSetSeriesToolTipGenerator
    public void testSetSeriesToolTipGenerator() {
        XYPlot plot = (XYPlot) this.chart.getPlot();
        XYItemRenderer renderer = plot.getRenderer();
        StandardXYToolTipGenerator tt = new StandardXYToolTipGenerator();
        renderer.setSeriesToolTipGenerator(0, tt);
        XYToolTipGenerator tt2 = renderer.getToolTipGenerator(0, 0);
        assertTrue(tt2 == tt);
    }

// org.jfree.chart.junit.StackedAreaChartTests::testDrawWithNullInfo
    public void testDrawWithNullInfo() {

        boolean success = false;
        try {
            BufferedImage image = new BufferedImage(200 , 100, 
                    BufferedImage.TYPE_INT_RGB);
            Graphics2D g2 = image.createGraphics();
            this.chart.draw(g2, new Rectangle2D.Double(0, 0, 200, 100), null, 
                    null);
            g2.dispose();
            success = true;
        }
        catch (Exception e) {
          success = false;
        }
        assertTrue(success);

    }

// org.jfree.chart.junit.StackedAreaChartTests::testReplaceDataset
    public void testReplaceDataset() {

        
        Number[][] data = new Integer[][]
            {{new Integer(-30), new Integer(-20)},
             {new Integer(-10), new Integer(10)},
             {new Integer(20), new Integer(30)}};

        CategoryDataset newData = DatasetUtilities.createCategoryDataset("S", 
                "C", data);

        LocalListener l = new LocalListener();
        this.chart.addChangeListener(l);
        CategoryPlot plot = (CategoryPlot) this.chart.getPlot();
        plot.setDataset(newData);
        assertEquals(true, l.flag);
        ValueAxis axis = plot.getRangeAxis();
        Range range = axis.getRange();
        assertTrue("Expecting the lower bound of the range to be around -30: "
                    + range.getLowerBound(), range.getLowerBound() <= -30);
        assertTrue("Expecting the upper bound of the range to be around 30: "
                   + range.getUpperBound(), range.getUpperBound() >= 30);

    }

// org.jfree.chart.junit.StackedAreaChartTests::testSetSeriesToolTipGenerator
    public void testSetSeriesToolTipGenerator() {
        CategoryPlot plot = (CategoryPlot) this.chart.getPlot();
        CategoryItemRenderer renderer = plot.getRenderer();
        StandardCategoryToolTipGenerator tt
            = new StandardCategoryToolTipGenerator();
        renderer.setSeriesToolTipGenerator(0, tt);
        CategoryToolTipGenerator tt2 = renderer.getToolTipGenerator(0, 0);
        assertTrue(tt2 == tt);
    }

// org.jfree.chart.junit.StackedAreaChartTests::testSetSeriesURLGenerator
    public void testSetSeriesURLGenerator() {
        CategoryPlot plot = (CategoryPlot) this.chart.getPlot();
        CategoryItemRenderer renderer = plot.getRenderer();
        StandardCategoryURLGenerator url1
                = new StandardCategoryURLGenerator();
        renderer.setSeriesURLGenerator(0, url1);
        CategoryURLGenerator url2 = renderer.getURLGenerator(0, 0);
        assertTrue(url2 == url1);
    }

// org.jfree.chart.junit.StackedBarChart3DTests::testDrawWithNullInfo
    public void testDrawWithNullInfo() {

        boolean success = false;
        try {
            BufferedImage image = new BufferedImage(200 , 100, 
                    BufferedImage.TYPE_INT_RGB);
            Graphics2D g2 = image.createGraphics();
            this.chart.draw(g2, new Rectangle2D.Double(0, 0, 200, 100), null,
                    null);
            g2.dispose();
            success = true;
        }
        catch (Exception e) {
          success = false;
        }
        assertTrue(success);

    }

// org.jfree.chart.junit.StackedBarChart3DTests::testReplaceDataset
    public void testReplaceDataset() {

        
        Number[][] data = new Integer[][]
            {{new Integer(-30), new Integer(-20)},
             {new Integer(-10), new Integer(10)},
             {new Integer(20), new Integer(30)}};

        CategoryDataset newData = DatasetUtilities.createCategoryDataset("S", 
                "C", data);

        LocalListener l = new LocalListener();
        this.chart.addChangeListener(l);
        CategoryPlot plot = (CategoryPlot) this.chart.getPlot();
        plot.setDataset(newData);
        assertEquals(true, l.flag);
        ValueAxis axis = plot.getRangeAxis();
        Range range = axis.getRange();
        assertTrue("Expecting the lower bound of the range to be around -30: "
                    + range.getLowerBound(), range.getLowerBound() <= -30);
        assertTrue("Expecting the upper bound of the range to be around 30: "
                   + range.getUpperBound(), range.getUpperBound() >= 30);

    }

// org.jfree.chart.junit.StackedBarChart3DTests::testSetSeriesToolTipGenerator
    public void testSetSeriesToolTipGenerator() {
        CategoryPlot plot = (CategoryPlot) this.chart.getPlot();
        CategoryItemRenderer renderer = plot.getRenderer();
        StandardCategoryToolTipGenerator tt
                = new StandardCategoryToolTipGenerator();
        renderer.setSeriesToolTipGenerator(0, tt);
        CategoryToolTipGenerator tt2 = renderer.getToolTipGenerator(0, 0);
        assertTrue(tt2 == tt);
    }

// org.jfree.chart.junit.StackedBarChart3DTests::testSetSeriesURLGenerator
    public void testSetSeriesURLGenerator() {
        CategoryPlot plot = (CategoryPlot) this.chart.getPlot();
        CategoryItemRenderer renderer = plot.getRenderer();
        StandardCategoryURLGenerator url1
                = new StandardCategoryURLGenerator();
        renderer.setSeriesURLGenerator(0, url1);
        CategoryURLGenerator url2 = renderer.getURLGenerator(0, 0);
        assertTrue(url2 == url1);
    }

// org.jfree.chart.junit.StackedBarChartTests::testDrawWithNullInfo
    public void testDrawWithNullInfo() {

        boolean success = false;

        try {
            BufferedImage image = new BufferedImage(200 , 100, 
                    BufferedImage.TYPE_INT_RGB);
            Graphics2D g2 = image.createGraphics();
            this.chart.draw(g2, new Rectangle2D.Double(0, 0, 200, 100), null, 
                    null);
            g2.dispose();
            success = true;
        }
        catch (Exception e) {
          success = false;
        }

        assertTrue(success);

    }

// org.jfree.chart.junit.StackedBarChartTests::testReplaceDataset
    public void testReplaceDataset() {

        
        Number[][] data = new Integer[][]
            {{new Integer(-30), new Integer(-20)},
             {new Integer(-10), new Integer(10)},
             {new Integer(20), new Integer(30)}};

        CategoryDataset newData = DatasetUtilities.createCategoryDataset("S", 
                "C", data);

        LocalListener l = new LocalListener();
        this.chart.addChangeListener(l);
        CategoryPlot plot = (CategoryPlot) this.chart.getPlot();
        plot.setDataset(newData);
        assertEquals(true, l.flag);
        ValueAxis axis = plot.getRangeAxis();
        Range range = axis.getRange();
        assertTrue("Expecting the lower bound of the range to be around -30: "
                    + range.getLowerBound(), range.getLowerBound() <= -30);
        assertTrue("Expecting the upper bound of the range to be around 30: "
                   + range.getUpperBound(), range.getUpperBound() >= 30);

    }

// org.jfree.chart.junit.StackedBarChartTests::testSetSeriesToolTipGenerator
    public void testSetSeriesToolTipGenerator() {
        CategoryPlot plot = (CategoryPlot) this.chart.getPlot();
        CategoryItemRenderer renderer = plot.getRenderer();
        StandardCategoryToolTipGenerator tt
                = new StandardCategoryToolTipGenerator();
        renderer.setSeriesToolTipGenerator(0, tt);
        CategoryToolTipGenerator tt2 = renderer.getToolTipGenerator(0, 0);
        assertTrue(tt2 == tt);
    }

// org.jfree.chart.junit.StackedBarChartTests::testSetSeriesURLGenerator
    public void testSetSeriesURLGenerator() {
        CategoryPlot plot = (CategoryPlot) this.chart.getPlot();
        CategoryItemRenderer renderer = plot.getRenderer();
        StandardCategoryURLGenerator url1
                = new StandardCategoryURLGenerator();
        renderer.setSeriesURLGenerator(0, url1);
        CategoryURLGenerator url2 = renderer.getURLGenerator(0, 0);
        assertTrue(url2 == url1);
    }

// org.jfree.chart.junit.TimeSeriesChartTests::testDrawWithNullInfo
    public void testDrawWithNullInfo() {

        boolean success = false;
        try {
            BufferedImage image = new BufferedImage(200 , 100, 
                    BufferedImage.TYPE_INT_RGB);
            Graphics2D g2 = image.createGraphics();
            this.chart.draw(g2, new Rectangle2D.Double(0, 0, 200, 100), null, 
                    null);
            g2.dispose();
            success = true;
        }
        catch (Exception e) {
          success = false;
          e.printStackTrace();
        }
        assertTrue(success);

    }

// org.jfree.chart.junit.TimeSeriesChartTests::testReplaceDataset
    public void testReplaceDataset() {

        
        XYSeries series1 = new XYSeries("Series 1");
        series1.add(10.0, 10.0);
        series1.add(20.0, 20.0);
        series1.add(30.0, 30.0);
        XYDataset dataset = new XYSeriesCollection(series1);

        LocalListener l = new LocalListener();
        this.chart.addChangeListener(l);
        XYPlot plot = (XYPlot) this.chart.getPlot();
        plot.setDataset(dataset);
        assertEquals(true, l.flag);
        ValueAxis axis = plot.getRangeAxis();
        Range range = axis.getRange();
        assertTrue("Expecting the lower bound of the range to be around 10: "
                   + range.getLowerBound(), range.getLowerBound() <= 10);
        assertTrue("Expecting the upper bound of the range to be around 30: "
                   + range.getUpperBound(), range.getUpperBound() >= 30);

    }

// org.jfree.chart.junit.TimeSeriesChartTests::testSetSeriesToolTipGenerator
    public void testSetSeriesToolTipGenerator() {
        XYPlot plot = (XYPlot) this.chart.getPlot();
        XYItemRenderer renderer = plot.getRenderer();
        StandardXYToolTipGenerator tt = new StandardXYToolTipGenerator();
        renderer.setSeriesToolTipGenerator(0, tt);
        XYToolTipGenerator tt2 = renderer.getToolTipGenerator(0, 0);
        assertTrue(tt2 == tt);
    }

// org.jfree.chart.junit.WaterfallChartTests::testDrawWithNullInfo
    public void testDrawWithNullInfo() {

        boolean success = false;

        try {
            BufferedImage image = new BufferedImage(200 , 100, 
                    BufferedImage.TYPE_INT_RGB);
            Graphics2D g2 = image.createGraphics();
            this.chart.draw(g2, new Rectangle2D.Double(0, 0, 200, 100), null, 
                    null);
            g2.dispose();
            success = true;
        }
        catch (Exception e) {
            success = false;
        }

        assertTrue(success);

    }

// org.jfree.chart.junit.WaterfallChartTests::testSetSeriesToolTipGenerator
    public void testSetSeriesToolTipGenerator() {
        CategoryPlot plot = (CategoryPlot) this.chart.getPlot();
        CategoryItemRenderer renderer = plot.getRenderer();
        StandardCategoryToolTipGenerator tt
                = new StandardCategoryToolTipGenerator();
        renderer.setSeriesToolTipGenerator(0, tt);
        CategoryToolTipGenerator tt2 = renderer.getToolTipGenerator(0, 0);
        assertTrue(tt2 == tt);
    }

// org.jfree.chart.junit.WaterfallChartTests::testSetSeriesURLGenerator
    public void testSetSeriesURLGenerator() {
        CategoryPlot plot = (CategoryPlot) this.chart.getPlot();
        CategoryItemRenderer renderer = plot.getRenderer();
        StandardCategoryURLGenerator url1
                = new StandardCategoryURLGenerator();
        renderer.setSeriesURLGenerator(0, url1);
        CategoryURLGenerator url2 = renderer.getURLGenerator(0, 0);
        assertTrue(url2 == url1);
    }

// org.jfree.chart.junit.XYAreaChartTests::testDrawWithNullInfo
    public void testDrawWithNullInfo() {

        boolean success = false;
        try {
            BufferedImage image = new BufferedImage(200 , 100, 
                    BufferedImage.TYPE_INT_RGB);
            Graphics2D g2 = image.createGraphics();
            this.chart.draw(g2, new Rectangle2D.Double(0, 0, 200, 100), null, 
                    null);
            g2.dispose();
            success = true;
        }
        catch (Exception e) {
          success = false;
          e.printStackTrace();
        }
        assertTrue(success);

    }

// org.jfree.chart.junit.XYAreaChartTests::testReplaceDataset
    public void testReplaceDataset() {

        
        XYSeries series1 = new XYSeries("Series 1");
        series1.add(10.0, 10.0);
        series1.add(20.0, 20.0);
        series1.add(30.0, 30.0);
        XYDataset dataset = new XYSeriesCollection(series1);

        LocalListener l = new LocalListener();
        this.chart.addChangeListener(l);
        XYPlot plot = (XYPlot) this.chart.getPlot();
        plot.setDataset(dataset);
        assertEquals(true, l.flag);
        ValueAxis axis = plot.getRangeAxis();
        Range range = axis.getRange();
        assertTrue("Expecting the lower bound of the range to be around 10: "
                   + range.getLowerBound(), range.getLowerBound() <= 10);
        assertTrue("Expecting the upper bound of the range to be around 30: "
                   + range.getUpperBound(), range.getUpperBound() >= 30);

    }

// org.jfree.chart.junit.XYAreaChartTests::testSetSeriesToolTipGenerator
    public void testSetSeriesToolTipGenerator() {
        XYPlot plot = (XYPlot) this.chart.getPlot();
        XYItemRenderer renderer = plot.getRenderer();
        StandardXYToolTipGenerator tt = new StandardXYToolTipGenerator();
        renderer.setSeriesToolTipGenerator(0, tt);
        XYToolTipGenerator tt2 = renderer.getToolTipGenerator(0, 0);
        assertTrue(tt2 == tt);
    }

// org.jfree.chart.junit.XYBarChartTests::testDrawWithNullInfo
    public void testDrawWithNullInfo() {

        boolean success = false;
        try {
            BufferedImage image = new BufferedImage(200 , 100, 
                    BufferedImage.TYPE_INT_RGB);
            Graphics2D g2 = image.createGraphics();
            this.chart.draw(g2, new Rectangle2D.Double(0, 0, 200, 100), null, 
                    null);
            g2.dispose();
            success = true;
        }
        catch (Exception e) {
          success = false;
          e.printStackTrace();
        }
        assertTrue(success);

    }

// org.jfree.chart.junit.XYBarChartTests::testReplaceDataset
    public void testReplaceDataset() {

        
        XYSeries series1 = new XYSeries("Series 1");
        series1.add(10.0, 10.0);
        series1.add(20.0, 20.0);
        series1.add(30.0, 30.0);
        XYDataset dataset = new XYSeriesCollection(series1);

        LocalListener l = new LocalListener();
        this.chart.addChangeListener(l);
        
        XYPlot plot = (XYPlot) this.chart.getPlot();
        plot.setDataset(dataset);
        assertEquals(true, l.flag);
        ValueAxis axis = plot.getRangeAxis();
        Range range = axis.getRange();
        assertTrue("Expecting the lower bound of the range to be around 10: "
                   + range.getLowerBound(), range.getLowerBound() <= 10);
        assertTrue("Expecting the upper bound of the range to be around 30: "
                   + range.getUpperBound(), range.getUpperBound() >= 30);

    }

// org.jfree.chart.junit.XYBarChartTests::testSetSeriesToolTipGenerator
    public void testSetSeriesToolTipGenerator() {
        XYPlot plot = (XYPlot) this.chart.getPlot();
        XYItemRenderer renderer = plot.getRenderer();
        StandardXYToolTipGenerator tt = new StandardXYToolTipGenerator();
        renderer.setSeriesToolTipGenerator(0, tt);
        XYToolTipGenerator tt2 = renderer.getToolTipGenerator(0, 0);
        assertTrue(tt2 == tt);
    }

// org.jfree.chart.junit.XYLineChartTests::testDrawWithNullInfo
    public void testDrawWithNullInfo() {

        boolean success = false;
        try {
            BufferedImage image = new BufferedImage(200 , 100, 
                    BufferedImage.TYPE_INT_RGB);
            Graphics2D g2 = image.createGraphics();
            this.chart.draw(g2, new Rectangle2D.Double(0, 0, 200, 100), null, 
                    null);
            g2.dispose();
            success = true;
        }
        catch (Exception e) {
          success = false;
          e.printStackTrace();
        }
        assertTrue(success);

    }

// org.jfree.chart.junit.XYLineChartTests::testReplaceDataset
    public void testReplaceDataset() {

        
        XYSeries series1 = new XYSeries("Series 1");
        series1.add(10.0, 10.0);
        series1.add(20.0, 20.0);
        series1.add(30.0, 30.0);
        XYDataset dataset = new XYSeriesCollection(series1);

        LocalListener l = new LocalListener();
        this.chart.addChangeListener(l);
        XYPlot plot = (XYPlot) this.chart.getPlot();
        plot.setDataset(dataset);
        assertEquals(true, l.flag);
        ValueAxis axis = plot.getRangeAxis();
        Range range = axis.getRange();
        assertTrue("Expecting the lower bound of the range to be around 10: "
                   + range.getLowerBound(), range.getLowerBound() <= 10);
        assertTrue("Expecting the upper bound of the range to be around 30: "
                   + range.getUpperBound(), range.getUpperBound() >= 30);

    }

// org.jfree.chart.junit.XYLineChartTests::testSetSeriesToolTipGenerator
    public void testSetSeriesToolTipGenerator() {
        XYPlot plot = (XYPlot) this.chart.getPlot();
        XYItemRenderer renderer = plot.getRenderer();
        StandardXYToolTipGenerator tt = new StandardXYToolTipGenerator();
        renderer.setSeriesToolTipGenerator(0, tt);
        XYToolTipGenerator tt2 = renderer.getToolTipGenerator(0, 0);
        assertTrue(tt2 == tt);
    }

// org.jfree.chart.junit.XYStepAreaChartTests::testDrawWithNullInfo
    public void testDrawWithNullInfo() {

        boolean success = false;
        try {
            BufferedImage image = new BufferedImage(200 , 100, 
                    BufferedImage.TYPE_INT_RGB);
            Graphics2D g2 = image.createGraphics();
            this.chart.draw(g2, new Rectangle2D.Double(0, 0, 200, 100), null, 
                    null);
            g2.dispose();
            success = true;
        }
        catch (Exception e) {
          success = false;
          e.printStackTrace();
        }
        assertTrue(success);

    }

// org.jfree.chart.junit.XYStepAreaChartTests::testReplaceDataset
    public void testReplaceDataset() {

        
        XYSeries series1 = new XYSeries("Series 1");
        series1.add(10.0, 10.0);
        series1.add(20.0, 20.0);
        series1.add(30.0, 30.0);
        XYDataset dataset = new XYSeriesCollection(series1);

        LocalListener l = new LocalListener();
        this.chart.addChangeListener(l);
        XYPlot plot = (XYPlot) this.chart.getPlot();
        plot.setDataset(dataset);
        assertEquals(true, l.flag);
        ValueAxis axis = plot.getRangeAxis();
        Range range = axis.getRange();
        assertTrue("Expecting the lower bound of the range to be around 10: "
                   + range.getLowerBound(), range.getLowerBound() <= 10);
        assertTrue("Expecting the upper bound of the range to be around 30: "
                   + range.getUpperBound(), range.getUpperBound() >= 30);

    }

// org.jfree.chart.junit.XYStepAreaChartTests::testSetSeriesToolTipGenerator
    public void testSetSeriesToolTipGenerator() {
        XYPlot plot = (XYPlot) this.chart.getPlot();
        XYItemRenderer renderer = plot.getRenderer();
        StandardXYToolTipGenerator tt = new StandardXYToolTipGenerator();
        renderer.setSeriesToolTipGenerator(0, tt);
        XYToolTipGenerator tt2 = renderer.getToolTipGenerator(0, 0);
        assertTrue(tt2 == tt);
    }

// org.jfree.chart.junit.XYStepChartTests::testDrawWithNullInfo
    public void testDrawWithNullInfo() {

        boolean success = false;
        try {
            BufferedImage image = new BufferedImage(200 , 100, 
                    BufferedImage.TYPE_INT_RGB);
            Graphics2D g2 = image.createGraphics();
            this.chart.draw(g2, new Rectangle2D.Double(0, 0, 200, 100), null, 
                    null);
            g2.dispose();
            success = true;
        }
        catch (Exception e) {
          success = false;
          e.printStackTrace();
        }
        assertTrue(success);

    }

// org.jfree.chart.junit.XYStepChartTests::testReplaceDataset
    public void testReplaceDataset() {

        
        XYSeries series1 = new XYSeries("Series 1");
        series1.add(10.0, 10.0);
        series1.add(20.0, 20.0);
        series1.add(30.0, 30.0);
        XYDataset dataset = new XYSeriesCollection(series1);

        LocalListener l = new LocalListener();
        this.chart.addChangeListener(l);
        XYPlot plot = (XYPlot) this.chart.getPlot();
        plot.setDataset(dataset);
        assertEquals(true, l.flag);
        ValueAxis axis = plot.getRangeAxis();
        Range range = axis.getRange();
        assertTrue("Expecting the lower bound of the range to be around 10: "
                   + range.getLowerBound(), range.getLowerBound() <= 10);
        assertTrue("Expecting the upper bound of the range to be around 30: "
                   + range.getUpperBound(), range.getUpperBound() >= 30);

    }

// org.jfree.chart.junit.XYStepChartTests::testSetSeriesToolTipGenerator
    public void testSetSeriesToolTipGenerator() {
        XYPlot plot = (XYPlot) this.chart.getPlot();
        XYItemRenderer renderer = plot.getRenderer();
        StandardXYToolTipGenerator tt = new StandardXYToolTipGenerator();
        renderer.setSeriesToolTipGenerator(0, tt);
        XYToolTipGenerator tt2 = renderer.getToolTipGenerator(0, 0);
        assertTrue(tt2 == tt);
    }

// org.jfree.chart.plot.junit.CategoryPlotTests::testConstructor
    public void testConstructor() {
        CategoryPlot plot = new CategoryPlot();
        assertEquals(new RectangleInsets(4.0, 4.0, 4.0, 4.0), 
        		plot.getAxisOffset());
    }

// org.jfree.chart.plot.junit.CategoryPlotTests::testAxisRange
    public void testAxisRange() {
        DefaultCategoryDataset datasetA = new DefaultCategoryDataset();
        DefaultCategoryDataset datasetB = new DefaultCategoryDataset();
        datasetB.addValue(50.0, "R1", "C1");
        datasetB.addValue(80.0, "R1", "C1");
        CategoryPlot plot = new CategoryPlot(datasetA, new CategoryAxis(null), 
                new NumberAxis(null), new LineAndShapeRenderer());
        plot.setDataset(1, datasetB);
        plot.setRenderer(1, new LineAndShapeRenderer());
        Range r = plot.getRangeAxis().getRange();
        assertEquals(84.0, r.getUpperBound(), 0.00001);
    }

// org.jfree.chart.plot.junit.CategoryPlotTests::testEquals
    public void testEquals() {
        
        CategoryPlot plot1 = new CategoryPlot();
        CategoryPlot plot2 = new CategoryPlot();
        assertTrue(plot1.equals(plot2));    
        assertTrue(plot2.equals(plot1));
        
        
        plot1.setOrientation(PlotOrientation.HORIZONTAL);
        assertFalse(plot1.equals(plot2));
        plot2.setOrientation(PlotOrientation.HORIZONTAL);
        assertTrue(plot1.equals(plot2));
        
        
        plot1.setAxisOffset(new RectangleInsets(0.05, 0.05, 0.05, 0.05));
        assertFalse(plot1.equals(plot2));
        plot2.setAxisOffset(new RectangleInsets(0.05, 0.05, 0.05, 0.05));
        assertTrue(plot1.equals(plot2));

        
        plot1.setDomainAxis(new CategoryAxis("Category Axis"));
        assertFalse(plot1.equals(plot2));
        plot2.setDomainAxis(new CategoryAxis("Category Axis"));
        assertTrue(plot1.equals(plot2));

        
        plot1.setDomainAxis(11, new CategoryAxis("Secondary Axis"));
        assertFalse(plot1.equals(plot2));
        plot2.setDomainAxis(11, new CategoryAxis("Secondary Axis"));
        assertTrue(plot1.equals(plot2));

        
        plot1.setDomainAxisLocation(AxisLocation.TOP_OR_RIGHT);
        assertFalse(plot1.equals(plot2));
        plot2.setDomainAxisLocation(AxisLocation.TOP_OR_RIGHT);
        assertTrue(plot1.equals(plot2));

        
        plot1.setDomainAxisLocation(11, AxisLocation.TOP_OR_RIGHT);
        assertFalse(plot1.equals(plot2));
        plot2.setDomainAxisLocation(11, AxisLocation.TOP_OR_RIGHT);
        assertTrue(plot1.equals(plot2));

        
        plot1.setDrawSharedDomainAxis(!plot1.getDrawSharedDomainAxis());
        assertFalse(plot1.equals(plot2));
        plot2.setDrawSharedDomainAxis(!plot2.getDrawSharedDomainAxis());
        assertTrue(plot1.equals(plot2));
        
        
        plot1.setRangeAxis(new NumberAxis("Range Axis"));
        assertFalse(plot1.equals(plot2));
        plot2.setRangeAxis(new NumberAxis("Range Axis"));
        assertTrue(plot1.equals(plot2));

        
        plot1.setRangeAxis(11, new NumberAxis("Secondary Range Axis"));
        assertFalse(plot1.equals(plot2));
        plot2.setRangeAxis(11, new NumberAxis("Secondary Range Axis"));
        assertTrue(plot1.equals(plot2));

        
        plot1.setRangeAxisLocation(AxisLocation.TOP_OR_RIGHT);
        assertFalse(plot1.equals(plot2));
        plot2.setRangeAxisLocation(AxisLocation.TOP_OR_RIGHT);
        assertTrue(plot1.equals(plot2));

        
        plot1.setRangeAxisLocation(11, AxisLocation.TOP_OR_RIGHT);
        assertFalse(plot1.equals(plot2));
        plot2.setRangeAxisLocation(11, AxisLocation.TOP_OR_RIGHT);
        assertTrue(plot1.equals(plot2));
        
        
        plot1.mapDatasetToDomainAxis(11, 11);
        assertFalse(plot1.equals(plot2));
        plot2.mapDatasetToDomainAxis(11, 11);
        assertTrue(plot1.equals(plot2));

        
        plot1.mapDatasetToRangeAxis(11, 11);
        assertFalse(plot1.equals(plot2));
        plot2.mapDatasetToRangeAxis(11, 11);
        assertTrue(plot1.equals(plot2));
        
        
        plot1.setRenderer(new AreaRenderer());
        assertFalse(plot1.equals(plot2));
        plot2.setRenderer(new AreaRenderer());
        assertTrue(plot1.equals(plot2));
        
        
        plot1.setRenderer(11, new AreaRenderer());
        assertFalse(plot1.equals(plot2));
        plot2.setRenderer(11, new AreaRenderer());
        assertTrue(plot1.equals(plot2));
        
        
        plot1.setDatasetRenderingOrder(DatasetRenderingOrder.FORWARD);
        assertFalse(plot1.equals(plot2));
        plot2.setDatasetRenderingOrder(DatasetRenderingOrder.FORWARD);
        assertTrue(plot1.equals(plot2));

        
        plot1.setColumnRenderingOrder(SortOrder.DESCENDING);
        assertFalse(plot1.equals(plot2));
        plot2.setColumnRenderingOrder(SortOrder.DESCENDING);
        assertTrue(plot1.equals(plot2));
        
        
        plot1.setRowRenderingOrder(SortOrder.DESCENDING);
        assertFalse(plot1.equals(plot2));
        plot2.setRowRenderingOrder(SortOrder.DESCENDING);
        assertTrue(plot1.equals(plot2));
        
        
        plot1.setDomainGridlinesVisible(true);
        assertFalse(plot1.equals(plot2));
        plot2.setDomainGridlinesVisible(true);
        assertTrue(plot1.equals(plot2));

        
        plot1.setDomainGridlinePosition(CategoryAnchor.END);
        assertFalse(plot1.equals(plot2));
        plot2.setDomainGridlinePosition(CategoryAnchor.END);
        assertTrue(plot1.equals(plot2));

        
        Stroke stroke = new BasicStroke(2.0f);
        plot1.setDomainGridlineStroke(stroke);
        assertFalse(plot1.equals(plot2));
        plot2.setDomainGridlineStroke(stroke);
        assertTrue(plot1.equals(plot2));
        
        
        plot1.setDomainGridlinePaint(new GradientPaint(1.0f, 2.0f, Color.blue, 
                3.0f, 4.0f, Color.yellow));
        assertFalse(plot1.equals(plot2));
        plot2.setDomainGridlinePaint(new GradientPaint(1.0f, 2.0f, Color.blue, 
                3.0f, 4.0f, Color.yellow));
        assertTrue(plot1.equals(plot2));
        
        
        plot1.setRangeGridlinesVisible(false);
        assertFalse(plot1.equals(plot2));
        plot2.setRangeGridlinesVisible(false);
        assertTrue(plot1.equals(plot2));

        
        plot1.setRangeGridlineStroke(stroke);
        assertFalse(plot1.equals(plot2));
        plot2.setRangeGridlineStroke(stroke);
        assertTrue(plot1.equals(plot2));
        
        
        plot1.setRangeGridlinePaint(new GradientPaint(1.0f, 2.0f, Color.green, 
                3.0f, 4.0f, Color.yellow));
        assertFalse(plot1.equals(plot2));
        plot2.setRangeGridlinePaint(new GradientPaint(1.0f, 2.0f, Color.green, 
                3.0f, 4.0f, Color.yellow));
        assertTrue(plot1.equals(plot2));
        
        
        plot1.setAnchorValue(100.0);
        assertFalse(plot1.equals(plot2));
        plot2.setAnchorValue(100.0);
        assertTrue(plot1.equals(plot2));
        
        
        plot1.setRangeCrosshairVisible(true);
        assertFalse(plot1.equals(plot2));
        plot2.setRangeCrosshairVisible(true);
        assertTrue(plot1.equals(plot2));
        
        
        plot1.setRangeCrosshairValue(100.0);
        assertFalse(plot1.equals(plot2));
        plot2.setRangeCrosshairValue(100.0);
        assertTrue(plot1.equals(plot2));
        
        
        plot1.setRangeCrosshairStroke(stroke);
        assertFalse(plot1.equals(plot2));
        plot2.setRangeCrosshairStroke(stroke);
        assertTrue(plot1.equals(plot2));
        
        
        plot1.setRangeCrosshairPaint(new GradientPaint(1.0f, 2.0f, Color.white, 
                3.0f, 4.0f, Color.yellow));
        assertFalse(plot1.equals(plot2));
        plot2.setRangeCrosshairPaint(new GradientPaint(1.0f, 2.0f, Color.white, 
                3.0f, 4.0f, Color.yellow));
        assertTrue(plot1.equals(plot2));
        
        
        plot1.setRangeCrosshairLockedOnData(false);
        assertFalse(plot1.equals(plot2));
        plot2.setRangeCrosshairLockedOnData(false);
        assertTrue(plot1.equals(plot2));
        
        
        plot1.addRangeMarker(new ValueMarker(4.0), Layer.FOREGROUND);
        assertFalse(plot1.equals(plot2));
        plot2.addRangeMarker(new ValueMarker(4.0), Layer.FOREGROUND);
        assertTrue(plot1.equals(plot2));
        
        plot1.addRangeMarker(new ValueMarker(5.0), Layer.BACKGROUND);
        assertFalse(plot1.equals(plot2));
        plot2.addRangeMarker(new ValueMarker(5.0), Layer.BACKGROUND);
        assertTrue(plot1.equals(plot2));
        
        
        plot1.addRangeMarker(1, new ValueMarker(4.0), Layer.FOREGROUND);
        assertFalse(plot1.equals(plot2));
        plot2.addRangeMarker(1, new ValueMarker(4.0), Layer.FOREGROUND);
        assertTrue(plot1.equals(plot2));
        
        
        plot1.addRangeMarker(1, new ValueMarker(5.0), Layer.BACKGROUND);
        assertFalse(plot1.equals(plot2));
        plot2.addRangeMarker(1, new ValueMarker(5.0), Layer.BACKGROUND);
        assertTrue(plot1.equals(plot2));

        
        plot1.addAnnotation(
            new CategoryTextAnnotation("Text", "Category", 43.0)
        );
        assertFalse(plot1.equals(plot2));
        plot2.addAnnotation(new CategoryTextAnnotation("Text", "Category", 
                43.0));
        assertTrue(plot1.equals(plot2));
        
        
        plot1.setWeight(3);
        assertFalse(plot1.equals(plot2));
        plot2.setWeight(3);
        assertTrue(plot1.equals(plot2));
        
        
        plot1.setFixedDomainAxisSpace(new AxisSpace());
        assertFalse(plot1.equals(plot2));
        plot2.setFixedDomainAxisSpace(new AxisSpace());
        assertTrue(plot1.equals(plot2));
        
        
        plot1.setFixedRangeAxisSpace(new AxisSpace());
        assertFalse(plot1.equals(plot2));
        plot2.setFixedRangeAxisSpace(new AxisSpace());
        assertTrue(plot1.equals(plot2));

    }

// org.jfree.chart.plot.junit.CategoryPlotTests::testCloning
    public void testCloning() {
        CategoryPlot p1 = new CategoryPlot();
        p1.setRangeCrosshairPaint(new GradientPaint(1.0f, 2.0f, Color.white, 
                3.0f, 4.0f, Color.yellow));
        CategoryPlot p2 = null;
        try {
            p2 = (CategoryPlot) p1.clone();
        }
        catch (CloneNotSupportedException e) {
            e.printStackTrace();
            System.err.println("Failed to clone.");
        }
        assertTrue(p1 != p2);
        assertTrue(p1.getClass() == p2.getClass());
        assertTrue(p1.equals(p2));
    }

// org.jfree.chart.plot.junit.CategoryPlotTests::testSerialization
    public void testSerialization() {

        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        CategoryAxis domainAxis = new CategoryAxis("Domain");
        NumberAxis rangeAxis = new NumberAxis("Range");
        BarRenderer renderer = new BarRenderer();
        CategoryPlot p1 = new CategoryPlot(dataset, domainAxis, rangeAxis, 
                renderer);
        p1.setOrientation(PlotOrientation.HORIZONTAL);
        CategoryPlot p2 = null;
        try {
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            ObjectOutput out = new ObjectOutputStream(buffer);
            out.writeObject(p1);
            out.close();

            ObjectInput in = new ObjectInputStream(
                    new ByteArrayInputStream(buffer.toByteArray()));
            p2 = (CategoryPlot) in.readObject();
            in.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        assertTrue(p1.equals(p2));
    }

// org.jfree.chart.plot.junit.CategoryPlotTests::testSerialization2
    public void testSerialization2() {

        DefaultCategoryDataset data = new DefaultCategoryDataset();
        CategoryAxis domainAxis = new CategoryAxis("Domain");
        NumberAxis rangeAxis = new NumberAxis("Range");
        BarRenderer renderer = new BarRenderer();
        CategoryPlot p1 = new CategoryPlot(data, domainAxis, rangeAxis, 
                renderer);
        p1.setOrientation(PlotOrientation.VERTICAL);
        CategoryPlot p2 = null;

        try {
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            ObjectOutput out = new ObjectOutputStream(buffer);
            out.writeObject(p1);
            out.close();

            ObjectInput in = new ObjectInputStream(
                    new ByteArrayInputStream(buffer.toByteArray()));
            p2 = (CategoryPlot) in.readObject();
            in.close();
        }
        catch (Exception e) {
            fail(e.toString());
        }
        assertEquals(p1, p2);

    }

// org.jfree.chart.plot.junit.CategoryPlotTests::testSerialization3
    public void testSerialization3() {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        JFreeChart chart = ChartFactory.createBarChart(
            "Test Chart",
            "Category Axis",
            "Value Axis",
            dataset,
            PlotOrientation.VERTICAL,
            true,
            true,
            false
        );
        JFreeChart chart2 = null;
        
        
        try {
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            ObjectOutput out = new ObjectOutputStream(buffer);
            out.writeObject(chart);
            out.close();

            ObjectInput in = new ObjectInputStream(
                    new ByteArrayInputStream(buffer.toByteArray()));
            chart2 = (JFreeChart) in.readObject();
            in.close();
        }
        catch (Exception e) {
            fail(e.toString());
        }

        
        boolean passed = true;
        try {
            chart2.createBufferedImage(300, 200);
        }
        catch (Exception e) {
            passed = false;  
            e.printStackTrace();            
        }
        assertTrue(passed);
    }

// org.jfree.chart.plot.junit.CategoryPlotTests::testSerialization4
    public void testSerialization4() {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        JFreeChart chart = ChartFactory.createBarChart(
            "Test Chart",
            "Category Axis",
            "Value Axis",
            dataset,
            PlotOrientation.VERTICAL,
            true,
            true,
            false
        );
        CategoryPlot plot = (CategoryPlot) chart.getPlot();
        plot.addRangeMarker(new ValueMarker(1.1), Layer.FOREGROUND);
        plot.addRangeMarker(new IntervalMarker(2.2, 3.3), Layer.BACKGROUND);
        JFreeChart chart2 = null;
        
        
        try {
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            ObjectOutput out = new ObjectOutputStream(buffer);
            out.writeObject(chart);
            out.close();

            ObjectInput in = new ObjectInputStream(
                    new ByteArrayInputStream(buffer.toByteArray()));
            chart2 = (JFreeChart) in.readObject();
            in.close();
        }
        catch (Exception e) {
            fail(e.toString());
        }
        assertEquals(chart, chart2);
        
        
        boolean passed = true;
        try {
            chart2.createBufferedImage(300, 200);
        }
        catch (Exception e) {
            passed = false;  
            e.printStackTrace();            
        }
        assertTrue(passed);
    }

// org.jfree.chart.plot.junit.CategoryPlotTests::testSerialization5
    public void testSerialization5() {
        DefaultCategoryDataset dataset1 = new DefaultCategoryDataset();
        CategoryAxis domainAxis1 = new CategoryAxis("Domain 1");
        NumberAxis rangeAxis1 = new NumberAxis("Range 1");
        BarRenderer renderer1 = new BarRenderer();
        CategoryPlot p1 = new CategoryPlot(dataset1, domainAxis1, rangeAxis1, 
                renderer1);
        CategoryAxis domainAxis2 = new CategoryAxis("Domain 2");
        NumberAxis rangeAxis2 = new NumberAxis("Range 2");
        BarRenderer renderer2 = new BarRenderer();
        DefaultCategoryDataset dataset2 = new DefaultCategoryDataset();
        p1.setDataset(1, dataset2);
        p1.setDomainAxis(1, domainAxis2);
        p1.setRangeAxis(1, rangeAxis2);
        p1.setRenderer(1, renderer2);
        CategoryPlot p2 = null;
        try {
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            ObjectOutput out = new ObjectOutputStream(buffer);
            out.writeObject(p1);
            out.close();
            ObjectInput in = new ObjectInputStream(
                    new ByteArrayInputStream(buffer.toByteArray()));
            p2 = (CategoryPlot) in.readObject();
            in.close();
        }
        catch (Exception e) {
            fail(e.toString());
        }
        assertEquals(p1, p2);
        
        
        
        CategoryAxis domainAxisA = p2.getDomainAxis(0);
        NumberAxis rangeAxisA = (NumberAxis) p2.getRangeAxis(0);
        DefaultCategoryDataset datasetA 
                = (DefaultCategoryDataset) p2.getDataset(0);
        BarRenderer rendererA = (BarRenderer) p2.getRenderer(0);
        CategoryAxis domainAxisB = p2.getDomainAxis(1);
        NumberAxis rangeAxisB = (NumberAxis) p2.getRangeAxis(1);
        DefaultCategoryDataset datasetB 
                = (DefaultCategoryDataset) p2.getDataset(1);
        BarRenderer rendererB  = (BarRenderer) p2.getRenderer(1);
        assertTrue(datasetA.hasListener(p2));
        assertTrue(domainAxisA.hasListener(p2));
        assertTrue(rangeAxisA.hasListener(p2));
        assertTrue(rendererA.hasListener(p2));
        assertTrue(datasetB.hasListener(p2));
        assertTrue(domainAxisB.hasListener(p2));
        assertTrue(rangeAxisB.hasListener(p2));
        assertTrue(rendererB.hasListener(p2));
    }

// org.jfree.chart.plot.junit.CategoryPlotTests::testSetRenderer
    public void testSetRenderer() {
        CategoryPlot plot = new CategoryPlot();
        CategoryItemRenderer renderer = new LineAndShapeRenderer();
        plot.setRenderer(renderer);
        
        
        MyPlotChangeListener listener = new MyPlotChangeListener();
        plot.addChangeListener(listener);
        renderer.setSeriesPaint(0, Color.black);
        assertTrue(listener.getEvent() != null);
    }

// org.jfree.chart.plot.junit.CategoryPlotTests::test1169972
    public void test1169972() {
        CategoryPlot plot = new CategoryPlot(null, null, null, null);
        plot.setDomainAxis(new CategoryAxis("C"));
        plot.setRangeAxis(new NumberAxis("Y"));
        plot.setRenderer(new BarRenderer());
        plot.setDataset(new DefaultCategoryDataset());
        assertTrue(plot != null);
    }

// org.jfree.chart.plot.junit.CategoryPlotTests::testAddDomainMarker
    public void testAddDomainMarker() {
        CategoryPlot plot = new CategoryPlot();
        CategoryMarker m = new CategoryMarker("C1");
        plot.addDomainMarker(m);
        List listeners = Arrays.asList(m.getListeners(
                MarkerChangeListener.class));
        assertTrue(listeners.contains(plot));
        plot.clearDomainMarkers();
        listeners = Arrays.asList(m.getListeners(MarkerChangeListener.class));
        assertFalse(listeners.contains(plot));
    }

// org.jfree.chart.plot.junit.CategoryPlotTests::testAddRangeMarker
    public void testAddRangeMarker() {
        CategoryPlot plot = new CategoryPlot();
        Marker m = new ValueMarker(1.0);
        plot.addRangeMarker(m);
        List listeners = Arrays.asList(m.getListeners(
                MarkerChangeListener.class));
        assertTrue(listeners.contains(plot));
        plot.clearRangeMarkers();
        listeners = Arrays.asList(m.getListeners(MarkerChangeListener.class));
        assertFalse(listeners.contains(plot));
    }

// org.jfree.chart.plot.junit.CategoryPlotTests::test1654215
    public void test1654215() {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        JFreeChart chart = ChartFactory.createLineChart("Title", "X", "Y",
                dataset, PlotOrientation.VERTICAL, true, false, false);
        CategoryPlot plot = (CategoryPlot) chart.getPlot();
        plot.setRenderer(1, new LineAndShapeRenderer());
        boolean success = false;
        try {
            BufferedImage image = new BufferedImage(200 , 100, 
                    BufferedImage.TYPE_INT_RGB);
            Graphics2D g2 = image.createGraphics();
            chart.draw(g2, new Rectangle2D.Double(0, 0, 200, 100), null, null);
            g2.dispose();
            success = true;
        }
        catch (Exception e) {
            e.printStackTrace();
            success = false;
        }
        assertTrue(success);
    }

// org.jfree.chart.plot.junit.CategoryPlotTests::testGetDomainAxisIndex
    public void testGetDomainAxisIndex() {
        CategoryAxis domainAxis1 = new CategoryAxis("X1");
        CategoryAxis domainAxis2 = new CategoryAxis("X2");
        NumberAxis rangeAxis1 = new NumberAxis("Y1");
        CategoryPlot plot = new CategoryPlot(null, domainAxis1, rangeAxis1, 
                null);
        assertEquals(0, plot.getDomainAxisIndex(domainAxis1));
        assertEquals(-1, plot.getDomainAxisIndex(domainAxis2));
        plot.setDomainAxis(1, domainAxis2);
        assertEquals(1, plot.getDomainAxisIndex(domainAxis2));
        assertEquals(-1, plot.getDomainAxisIndex(new CategoryAxis("X2")));
        boolean pass = false;
        try {
            plot.getDomainAxisIndex(null);
        }
        catch (IllegalArgumentException e) {
            pass = true;
        }
        assertTrue(pass);
    }

// org.jfree.chart.plot.junit.CategoryPlotTests::testGetRangeAxisIndex
    public void testGetRangeAxisIndex() {
        CategoryAxis domainAxis1 = new CategoryAxis("X1");
        NumberAxis rangeAxis1 = new NumberAxis("Y1");
        NumberAxis rangeAxis2 = new NumberAxis("Y2");
        CategoryPlot plot = new CategoryPlot(null, domainAxis1, rangeAxis1, 
                null);
        assertEquals(0, plot.getRangeAxisIndex(rangeAxis1));
        assertEquals(-1, plot.getRangeAxisIndex(rangeAxis2));
        plot.setRangeAxis(1, rangeAxis2);
        assertEquals(1, plot.getRangeAxisIndex(rangeAxis2));
        assertEquals(-1, plot.getRangeAxisIndex(new NumberAxis("Y2")));
        boolean pass = false;
        try {
            plot.getRangeAxisIndex(null);
        }
        catch (IllegalArgumentException e) {
            pass = true;
        }
        assertTrue(pass);
    }

// org.jfree.chart.plot.junit.CategoryPlotTests::testRemoveDomainMarker
    public void testRemoveDomainMarker() {
    	CategoryPlot plot = new CategoryPlot();
    	assertFalse(plot.removeDomainMarker(new CategoryMarker("Category 1")));
    }

// org.jfree.chart.plot.junit.CategoryPlotTests::testRemoveRangeMarker
    public void testRemoveRangeMarker() {
    	CategoryPlot plot = new CategoryPlot();
    	assertFalse(plot.removeRangeMarker(new ValueMarker(0.5)));
    }

// org.jfree.chart.plot.junit.MultiplePiePlotTests::testConstructor
    public void testConstructor() {
    	MultiplePiePlot plot = new MultiplePiePlot();
    	assertNull(plot.getDataset());

    	
    	
    	DefaultCategoryDataset dataset = new DefaultCategoryDataset();
    	plot = new MultiplePiePlot(dataset);
    	assertTrue(dataset.hasListener(plot));
    }

// org.jfree.chart.plot.junit.MultiplePiePlotTests::testEquals
    public void testEquals() {
        MultiplePiePlot p1 = new MultiplePiePlot();
        MultiplePiePlot p2 = new MultiplePiePlot();
        assertTrue(p1.equals(p2));
        assertTrue(p2.equals(p1));

        p1.setDataExtractOrder(TableOrder.BY_ROW);
        assertFalse(p1.equals(p2));
        p2.setDataExtractOrder(TableOrder.BY_ROW);
        assertTrue(p1.equals(p2));

        p1.setLimit(1.23);
        assertFalse(p1.equals(p2));
        p2.setLimit(1.23);
        assertTrue(p1.equals(p2));

        p1.setAggregatedItemsKey("Aggregated Items");
        assertFalse(p1.equals(p2));
        p2.setAggregatedItemsKey("Aggregated Items");
        assertTrue(p1.equals(p2));

        p1.setAggregatedItemsPaint(new GradientPaint(1.0f, 2.0f, Color.red,
                3.0f, 4.0f, Color.yellow));
        assertFalse(p1.equals(p2));
        p2.setAggregatedItemsPaint(new GradientPaint(1.0f, 2.0f, Color.red,
                3.0f, 4.0f, Color.yellow));
        assertTrue(p1.equals(p2));

        p1.setPieChart(ChartFactory.createPieChart("Title", null, true, true,
                true));
        assertFalse(p1.equals(p2));
        p2.setPieChart(ChartFactory.createPieChart("Title", null, true, true,
                true));
        assertTrue(p1.equals(p2));
    }

// org.jfree.chart.plot.junit.MultiplePiePlotTests::testCloning
    public void testCloning() {
        MultiplePiePlot p1 = new MultiplePiePlot();
        MultiplePiePlot p2 = null;
        try {
            p2 = (MultiplePiePlot) p1.clone();
        }
        catch (CloneNotSupportedException e) {
            e.printStackTrace();
            System.err.println("Failed to clone.");
        }
        assertTrue(p1 != p2);
        assertTrue(p1.getClass() == p2.getClass());
        assertTrue(p1.equals(p2));
    }

// org.jfree.chart.plot.junit.MultiplePiePlotTests::testSerialization
    public void testSerialization() {
        MultiplePiePlot p1 = new MultiplePiePlot(null);
        p1.setAggregatedItemsPaint(new GradientPaint(1.0f, 2.0f, Color.yellow,
                3.0f, 4.0f, Color.red));
        MultiplePiePlot p2 = null;
        try {
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            ObjectOutput out = new ObjectOutputStream(buffer);
            out.writeObject(p1);
            out.close();

            ObjectInput in = new ObjectInputStream(
                new ByteArrayInputStream(buffer.toByteArray())
            );
            p2 = (MultiplePiePlot) in.readObject();
            in.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        assertEquals(p1, p2);
    }

// org.jfree.chart.plot.junit.PiePlot3DTests::testEquals
    public void testEquals() {
        PiePlot3D p1 = new PiePlot3D();
        PiePlot3D p2 = new PiePlot3D();
        assertTrue(p1.equals(p2));
        assertTrue(p2.equals(p1));
        
        p1.setDepthFactor(1.23);
        assertFalse(p1.equals(p2));
        p2.setDepthFactor(1.23);
        assertTrue(p1.equals(p2));
        
        p1.setDarkerSides(true);
        assertFalse(p1.equals(p2));
        p2.setDarkerSides(true);
        assertTrue(p1.equals(p2));
    }

// org.jfree.chart.plot.junit.PiePlot3DTests::testSerialization
    public void testSerialization() {

        PiePlot3D p1 = new PiePlot3D(null);
        PiePlot3D p2 = null;

        try {
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            ObjectOutput out = new ObjectOutputStream(buffer);
            out.writeObject(p1);
            out.close();

            ObjectInput in = new ObjectInputStream(
                    new ByteArrayInputStream(buffer.toByteArray()));
            p2 = (PiePlot3D) in.readObject();
            in.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        assertEquals(p1, p2);

    }

// org.jfree.chart.plot.junit.PiePlot3DTests::testDrawWithNullDataset
    public void testDrawWithNullDataset() {
        JFreeChart chart = ChartFactory.createPieChart3D("Test", null, true, 
                false, false);
        boolean success = false;
        try {
            BufferedImage image = new BufferedImage(200 , 100, 
                    BufferedImage.TYPE_INT_RGB);
            Graphics2D g2 = image.createGraphics();
            chart.draw(g2, new Rectangle2D.Double(0, 0, 200, 100), null, null);
            g2.dispose();
            success = true;
        }
        catch (Exception e) {
            success = false;
        }
        assertTrue(success);
    }

// org.jfree.chart.plot.junit.PiePlotTests::testEquals
    public void testEquals() {
        
        PiePlot plot1 = new PiePlot();
        PiePlot plot2 = new PiePlot();
        assertTrue(plot1.equals(plot2));
        assertTrue(plot2.equals(plot1));
                
        
        plot1.setPieIndex(99);
        assertFalse(plot1.equals(plot2));
        plot2.setPieIndex(99);
        assertTrue(plot1.equals(plot2));
        
        
        plot1.setInteriorGap(0.15);
        assertFalse(plot1.equals(plot2));
        plot2.setInteriorGap(0.15);
        assertTrue(plot1.equals(plot2));

        
        plot1.setCircular(!plot1.isCircular());
        assertFalse(plot1.equals(plot2));
        plot2.setCircular(false);
        assertTrue(plot1.equals(plot2));
        
        
        plot1.setStartAngle(Math.PI);
        assertFalse(plot1.equals(plot2));
        plot2.setStartAngle(Math.PI);
        assertTrue(plot1.equals(plot2));
        
        
        plot1.setDirection(Rotation.ANTICLOCKWISE);
        assertFalse(plot1.equals(plot2));
        plot2.setDirection(Rotation.ANTICLOCKWISE);
        assertTrue(plot1.equals(plot2));
        
        
        plot1.setIgnoreZeroValues(true);
        plot2.setIgnoreZeroValues(false);
        assertFalse(plot1.equals(plot2));
        plot2.setIgnoreZeroValues(true);
        assertTrue(plot1.equals(plot2));
        
        
        plot1.setIgnoreNullValues(true);
        plot2.setIgnoreNullValues(false);
        assertFalse(plot1.equals(plot2));
        plot2.setIgnoreNullValues(true);
        assertTrue(plot1.equals(plot2));
        
        
        plot1.setSectionPaint("A", new GradientPaint(1.0f, 2.0f, Color.blue, 
                3.0f, 4.0f, Color.white));
        assertFalse(plot1.equals(plot2));
        plot2.setSectionPaint("A", new GradientPaint(1.0f, 2.0f, Color.blue, 
                3.0f, 4.0f, Color.white));
        assertTrue(plot1.equals(plot2));
        
        
        plot1.setBaseSectionPaint(new GradientPaint(1.0f, 2.0f, Color.black, 
                3.0f, 4.0f, Color.white));
        assertFalse(plot1.equals(plot2));
        plot2.setBaseSectionPaint(new GradientPaint(1.0f, 2.0f, Color.black, 
                3.0f, 4.0f, Color.white));
        assertTrue(plot1.equals(plot2));
        
        
        plot1.setSectionOutlinesVisible(false);
        assertFalse(plot1.equals(plot2));
        plot2.setSectionOutlinesVisible(false);
        assertTrue(plot1.equals(plot2)); 
                
        
        plot1.setSectionOutlinePaint("A", new GradientPaint(1.0f, 2.0f, 
                Color.green, 3.0f, 4.0f, Color.white));
        assertFalse(plot1.equals(plot2));
        plot2.setSectionOutlinePaint("A", new GradientPaint(1.0f, 2.0f, 
                Color.green, 3.0f, 4.0f, Color.white));
        assertTrue(plot1.equals(plot2));
        
        
        plot1.setBaseSectionOutlinePaint(new GradientPaint(1.0f, 2.0f, 
                Color.gray, 3.0f, 4.0f, Color.white));
        assertFalse(plot1.equals(plot2));
        plot2.setBaseSectionOutlinePaint(new GradientPaint(1.0f, 2.0f, 
                Color.gray, 3.0f, 4.0f, Color.white));
        assertTrue(plot1.equals(plot2));
        
        
        plot1.setSectionOutlineStroke("A", new BasicStroke(1.0f));
        assertFalse(plot1.equals(plot2));
        plot2.setSectionOutlineStroke("A", new BasicStroke(1.0f));
        assertTrue(plot1.equals(plot2));
        
        
        plot1.setBaseSectionOutlineStroke(new BasicStroke(1.0f));
        assertFalse(plot1.equals(plot2));
        plot2.setBaseSectionOutlineStroke(new BasicStroke(1.0f));
        assertTrue(plot1.equals(plot2));
        
        
        plot1.setShadowPaint(new GradientPaint(1.0f, 2.0f, Color.orange, 
                3.0f, 4.0f, Color.white));
        assertFalse(plot1.equals(plot2));
        plot2.setShadowPaint(new GradientPaint(1.0f, 2.0f, Color.orange, 
                3.0f, 4.0f, Color.white));
        assertTrue(plot1.equals(plot2));

        
        plot1.setShadowXOffset(4.4);
        assertFalse(plot1.equals(plot2));
        plot2.setShadowXOffset(4.4);
        assertTrue(plot1.equals(plot2));

        
        plot1.setShadowYOffset(4.4);
        assertFalse(plot1.equals(plot2));
        plot2.setShadowYOffset(4.4);
        assertTrue(plot1.equals(plot2));

        
        plot1.setLabelFont(new Font("Serif", Font.PLAIN, 18));
        assertFalse(plot1.equals(plot2));
        plot2.setLabelFont(new Font("Serif", Font.PLAIN, 18));
        assertTrue(plot1.equals(plot2));
       
        
        plot1.setLabelPaint(new GradientPaint(1.0f, 2.0f, Color.darkGray, 
                3.0f, 4.0f, Color.white));
        assertFalse(plot1.equals(plot2));
        plot2.setLabelPaint(new GradientPaint(1.0f, 2.0f, Color.darkGray, 
                3.0f, 4.0f, Color.white));
        assertTrue(plot1.equals(plot2));
       
        
        plot1.setLabelBackgroundPaint(new GradientPaint(1.0f, 2.0f, Color.red, 
                3.0f, 4.0f, Color.white));
        assertFalse(plot1.equals(plot2));
        plot2.setLabelBackgroundPaint(new GradientPaint(1.0f, 2.0f, Color.red, 
                3.0f, 4.0f, Color.white));
        assertTrue(plot1.equals(plot2));
        
        
        plot1.setLabelOutlinePaint(new GradientPaint(1.0f, 2.0f, Color.blue, 
                3.0f, 4.0f, Color.white));
        assertFalse(plot1.equals(plot2));
        plot2.setLabelOutlinePaint(new GradientPaint(1.0f, 2.0f, Color.blue, 
                3.0f, 4.0f, Color.white));
        assertTrue(plot1.equals(plot2));
        
        
        Stroke s = new BasicStroke(1.1f);
        plot1.setLabelOutlineStroke(s);
        assertFalse(plot1.equals(plot2));
        plot2.setLabelOutlineStroke(s);
        assertTrue(plot1.equals(plot2));
        
        
        plot1.setLabelShadowPaint(new GradientPaint(1.0f, 2.0f, Color.yellow, 
                3.0f, 4.0f, Color.white));
        assertFalse(plot1.equals(plot2));
        plot2.setLabelShadowPaint(new GradientPaint(1.0f, 2.0f, Color.yellow, 
                3.0f, 4.0f, Color.white));
        assertTrue(plot1.equals(plot2));
        
        
        plot1.setExplodePercent("A", 0.33);
        assertFalse(plot1.equals(plot2));
        plot2.setExplodePercent("A", 0.33);
        assertTrue(plot1.equals(plot2));
        
        
        plot1.setLabelGenerator(new StandardPieSectionLabelGenerator(
                "{2}{1}{0}"));
        assertFalse(plot1.equals(plot2));
        plot2.setLabelGenerator(new StandardPieSectionLabelGenerator(
                "{2}{1}{0}"));
        assertTrue(plot1.equals(plot2));
       
        
        Font f = new Font("SansSerif", Font.PLAIN, 20);
        plot1.setLabelFont(f);
        assertFalse(plot1.equals(plot2));
        plot2.setLabelFont(f);
        assertTrue(plot1.equals(plot2));
        
        
        plot1.setLabelPaint(new GradientPaint(1.0f, 2.0f, Color.magenta, 
                3.0f, 4.0f, Color.white));
        assertFalse(plot1.equals(plot2));
        plot2.setLabelPaint(new GradientPaint(1.0f, 2.0f, Color.magenta, 
                3.0f, 4.0f, Color.white));
        assertTrue(plot1.equals(plot2));
        
        
        plot1.setMaximumLabelWidth(0.33);
        assertFalse(plot1.equals(plot2));
        plot2.setMaximumLabelWidth(0.33);
        assertTrue(plot1.equals(plot2));
        
        
        plot1.setLabelGap(0.11);
        assertFalse(plot1.equals(plot2));
        plot2.setLabelGap(0.11);
        assertTrue(plot1.equals(plot2));
        
        
        plot1.setLabelLinksVisible(false);
        assertFalse(plot1.equals(plot2));
        plot2.setLabelLinksVisible(false);
        assertTrue(plot1.equals(plot2));
        
        
        plot1.setLabelLinkMargin(0.11);
        assertFalse(plot1.equals(plot2));
        plot2.setLabelLinkMargin(0.11);
        assertTrue(plot1.equals(plot2));

        
        plot1.setLabelLinkPaint(new GradientPaint(1.0f, 2.0f, Color.magenta, 
                3.0f, 4.0f, Color.white));
        assertFalse(plot1.equals(plot2));
        plot2.setLabelLinkPaint(new GradientPaint(1.0f, 2.0f, Color.magenta, 
                3.0f, 4.0f, Color.white));
        assertTrue(plot1.equals(plot2));
       
        
        plot1.setLabelLinkStroke(new BasicStroke(1.0f));
        assertFalse(plot1.equals(plot2));
        plot2.setLabelLinkStroke(new BasicStroke(1.0f));
        assertTrue(plot1.equals(plot2));
       
        
        plot1.setToolTipGenerator(
            new StandardPieToolTipGenerator("{2}{1}{0}")
        );
        assertFalse(plot1.equals(plot2));
        plot2.setToolTipGenerator(
            new StandardPieToolTipGenerator("{2}{1}{0}")
        );
        assertTrue(plot1.equals(plot2));
        
        
        plot1.setURLGenerator(new StandardPieURLGenerator("xx"));
        assertFalse(plot1.equals(plot2));
        plot2.setURLGenerator(new StandardPieURLGenerator("xx"));
        assertTrue(plot1.equals(plot2));
        
        
        plot1.setMinimumArcAngleToDraw(1.0);
        assertFalse(plot1.equals(plot2));
        plot2.setMinimumArcAngleToDraw(1.0);  
        assertTrue(plot1.equals(plot2));
        
        
        plot1.setLegendItemShape(new Rectangle2D.Double(1.0, 2.0, 3.0, 4.0));
        assertFalse(plot1.equals(plot2));
        plot2.setLegendItemShape(new Rectangle2D.Double(1.0, 2.0, 3.0, 4.0));
        assertTrue(plot1.equals(plot2));
        
        
        plot1.setLegendLabelGenerator(new StandardPieSectionLabelGenerator(
                "{0} --> {1}"));
        assertFalse(plot1.equals(plot2));
        plot2.setLegendLabelGenerator(new StandardPieSectionLabelGenerator(
                "{0} --> {1}"));
        assertTrue(plot1.equals(plot2));
        
        
        plot1.setLegendLabelToolTipGenerator(
                new StandardPieSectionLabelGenerator("{0} is {1}"));
        assertFalse(plot1.equals(plot2));
        plot2.setLegendLabelToolTipGenerator(
                new StandardPieSectionLabelGenerator("{0} is {1}"));
        assertTrue(plot1.equals(plot2));
        
        
        plot1.setLegendLabelURLGenerator(new StandardPieURLGenerator(
                "index.html"));
        assertFalse(plot1.equals(plot2));
        plot2.setLegendLabelURLGenerator(new StandardPieURLGenerator(
                "index.html"));
        assertTrue(plot1.equals(plot2));
        
    }

// org.jfree.chart.plot.junit.PiePlotTests::testCloning
    public void testCloning() {
        PiePlot p1 = new PiePlot();
        PiePlot p2 = null;
        try {
            p2 = (PiePlot) p1.clone();
        }
        catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        assertTrue(p1 != p2);
        assertTrue(p1.getClass() == p2.getClass());
        assertTrue(p1.equals(p2));
    }

// org.jfree.chart.plot.junit.PiePlotTests::testCloning_URLGenerator
    public void testCloning_URLGenerator() {
        CustomPieURLGenerator generator = new CustomPieURLGenerator();
        PiePlot p1 = new PiePlot();
        p1.setURLGenerator(generator);
        PiePlot p2 = null;
        try {
            p2 = (PiePlot) p1.clone();
        }
        catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        assertTrue(p1 != p2);
        assertTrue(p1.getClass() == p2.getClass());
        assertTrue(p1.equals(p2));
        
        
        assertTrue(p1.getURLGenerator() != p2.getURLGenerator());
    }

// org.jfree.chart.plot.junit.PiePlotTests::testCloning_LegendItemShape
    public void testCloning_LegendItemShape() {
        Rectangle shape = new Rectangle(-4, -4, 8, 8);
        PiePlot p1 = new PiePlot();
        p1.setLegendItemShape(shape);
        PiePlot p2 = null;
        try {
            p2 = (PiePlot) p1.clone();
        }
        catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        assertTrue(p1 != p2);
        assertTrue(p1.getClass() == p2.getClass());
        assertTrue(p1.equals(p2));
        
        
        shape.setRect(1.0, 2.0, 3.0, 4.0);
        assertFalse(p1.equals(p2));
    }

// org.jfree.chart.plot.junit.PiePlotTests::testCloning_LegendLabelGenerator
    public void testCloning_LegendLabelGenerator() {
        StandardPieSectionLabelGenerator generator 
                = new StandardPieSectionLabelGenerator();
        PiePlot p1 = new PiePlot();
        p1.setLegendLabelGenerator(generator);
        PiePlot p2 = null;
        try {
            p2 = (PiePlot) p1.clone();
        }
        catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        assertTrue(p1 != p2);
        assertTrue(p1.getClass() == p2.getClass());
        assertTrue(p1.equals(p2));
        
        
        generator.getNumberFormat().setMinimumFractionDigits(2);
        assertFalse(p1.equals(p2));
    }

// org.jfree.chart.plot.junit.PiePlotTests::testCloning_LegendLabelToolTipGenerator
    public void testCloning_LegendLabelToolTipGenerator() {
        StandardPieSectionLabelGenerator generator 
                = new StandardPieSectionLabelGenerator();
        PiePlot p1 = new PiePlot();
        p1.setLegendLabelToolTipGenerator(generator);
        PiePlot p2 = null;
        try {
            p2 = (PiePlot) p1.clone();
        }
        catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        assertTrue(p1 != p2);
        assertTrue(p1.getClass() == p2.getClass());
        assertTrue(p1.equals(p2));
        
        
        generator.getNumberFormat().setMinimumFractionDigits(2);
        assertFalse(p1.equals(p2));
    }

// org.jfree.chart.plot.junit.PiePlotTests::testCloning_LegendLabelURLGenerator
    public void testCloning_LegendLabelURLGenerator() {
        CustomPieURLGenerator generator = new CustomPieURLGenerator();
        PiePlot p1 = new PiePlot();
        p1.setLegendLabelURLGenerator(generator);
        PiePlot p2 = null;
        try {
            p2 = (PiePlot) p1.clone();
        }
        catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        assertTrue(p1 != p2);
        assertTrue(p1.getClass() == p2.getClass());
        assertTrue(p1.equals(p2));
        
        
        assertTrue(p1.getLegendLabelURLGenerator() 
                != p2.getLegendLabelURLGenerator());
    }

// org.jfree.chart.plot.junit.PiePlotTests::testSerialization
    public void testSerialization() {
        PiePlot p1 = new PiePlot(null);
        PiePlot p2 = null;
        try {
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            ObjectOutput out = new ObjectOutputStream(buffer);
            out.writeObject(p1);
            out.close();

            ObjectInput in = new ObjectInputStream(new ByteArrayInputStream(
                    buffer.toByteArray()));
            p2 = (PiePlot) in.readObject();
            in.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        assertEquals(p1, p2);
    }

// org.jfree.chart.plot.junit.PiePlotTests::testGetLegendItems
    public void testGetLegendItems() {
        DefaultPieDataset dataset = new DefaultPieDataset();
        dataset.setValue("Item 1", 1.0);
        dataset.setValue("Item 2", 2.0);
        dataset.setValue("Item 3", 0.0);
        dataset.setValue("Item 4", null);
       
        PiePlot plot = new PiePlot(dataset);
        plot.setIgnoreNullValues(false);
        plot.setIgnoreZeroValues(false);
        LegendItemCollection items = plot.getLegendItems();
        assertEquals(4, items.getItemCount());
        
        
        plot.setIgnoreNullValues(true);
        items = plot.getLegendItems();
        assertEquals(3, items.getItemCount());
        
        
        plot.setIgnoreZeroValues(true);
        items = plot.getLegendItems();
        assertEquals(2, items.getItemCount());
        
        
        dataset.setValue("Item 5", -1.0);
        items = plot.getLegendItems();
        assertEquals(2, items.getItemCount());        
    }

// org.jfree.chart.plot.junit.PiePlotTests::testGetBaseSectionPaint
    public void testGetBaseSectionPaint() {
        PiePlot plot = new PiePlot();
        assertNotNull(plot.getBaseSectionPaint());
        
        boolean pass = false;
        try {
            plot.setBaseSectionPaint(null);
        }
        catch (IllegalArgumentException e) {
            pass = true;
        }
        assertTrue(pass);
    }

// org.jfree.chart.plot.junit.PiePlotTests::testDrawWithNullLegendLabels
    public void testDrawWithNullLegendLabels() {
        DefaultPieDataset dataset = new DefaultPieDataset();
        dataset.setValue("L1", 12.0);
        dataset.setValue("L2", 11.0);
        JFreeChart chart = ChartFactory.createPieChart("Test", dataset, true, 
                false, false);
        PiePlot plot = (PiePlot) chart.getPlot();
        plot.setLegendLabelGenerator(new NullLegendLabelGenerator());
        boolean success = false;
        try {
            BufferedImage image = new BufferedImage(200 , 100, 
                    BufferedImage.TYPE_INT_RGB);
            Graphics2D g2 = image.createGraphics();
            chart.draw(g2, new Rectangle2D.Double(0, 0, 200, 100), null, null);
            g2.dispose();
            success = true;
        }
        catch (Exception e) {
          success = false;
        }
        assertTrue(success);
    }

// org.jfree.chart.plot.junit.XYPlotTests::testEquals
    public void testEquals() {
        
        XYPlot plot1 = new XYPlot();
        XYPlot plot2 = new XYPlot();
        assertTrue(plot1.equals(plot2));    
        
        
        plot1.setOrientation(PlotOrientation.HORIZONTAL);
        assertFalse(plot1.equals(plot2));
        plot2.setOrientation(PlotOrientation.HORIZONTAL);
        assertTrue(plot1.equals(plot2));
        
        
        plot1.setAxisOffset(new RectangleInsets(0.05, 0.05, 0.05, 0.05));
        assertFalse(plot1.equals(plot2));
        plot2.setAxisOffset(new RectangleInsets(0.05, 0.05, 0.05, 0.05));
        assertTrue(plot1.equals(plot2));

        
        plot1.setDomainAxis(new NumberAxis("Domain Axis"));
        assertFalse(plot1.equals(plot2));
        plot2.setDomainAxis(new NumberAxis("Domain Axis"));
        assertTrue(plot1.equals(plot2));

        
        plot1.setDomainAxisLocation(AxisLocation.TOP_OR_RIGHT);
        assertFalse(plot1.equals(plot2));
        plot2.setDomainAxisLocation(AxisLocation.TOP_OR_RIGHT);
        assertTrue(plot1.equals(plot2));

        
        plot1.setDomainAxis(11, new NumberAxis("Secondary Domain Axis"));
        assertFalse(plot1.equals(plot2));
        plot2.setDomainAxis(11, new NumberAxis("Secondary Domain Axis"));
        assertTrue(plot1.equals(plot2));

        
        plot1.setDomainAxisLocation(11, AxisLocation.TOP_OR_RIGHT);
        assertFalse(plot1.equals(plot2));
        plot2.setDomainAxisLocation(11, AxisLocation.TOP_OR_RIGHT);
        assertTrue(plot1.equals(plot2));

        
        plot1.setRangeAxis(new NumberAxis("Range Axis"));
        assertFalse(plot1.equals(plot2));
        plot2.setRangeAxis(new NumberAxis("Range Axis"));
        assertTrue(plot1.equals(plot2));

        
        plot1.setRangeAxisLocation(AxisLocation.TOP_OR_RIGHT);
        assertFalse(plot1.equals(plot2));
        plot2.setRangeAxisLocation(AxisLocation.TOP_OR_RIGHT);
        assertTrue(plot1.equals(plot2));

        
        plot1.setRangeAxis(11, new NumberAxis("Secondary Range Axis"));
        assertFalse(plot1.equals(plot2));
        plot2.setRangeAxis(11, new NumberAxis("Secondary Range Axis"));
        assertTrue(plot1.equals(plot2));

        
        plot1.setRangeAxisLocation(11, AxisLocation.TOP_OR_RIGHT);
        assertFalse(plot1.equals(plot2));
        plot2.setRangeAxisLocation(11, AxisLocation.TOP_OR_RIGHT);
        assertTrue(plot1.equals(plot2));
        
        
        plot1.mapDatasetToDomainAxis(11, 11);
        assertFalse(plot1.equals(plot2));
        plot2.mapDatasetToDomainAxis(11, 11);
        assertTrue(plot1.equals(plot2));

        
        plot1.mapDatasetToRangeAxis(11, 11);
        assertFalse(plot1.equals(plot2));
        plot2.mapDatasetToRangeAxis(11, 11);
        assertTrue(plot1.equals(plot2));
        
        
        plot1.setRenderer(new DefaultXYItemRenderer());
        assertFalse(plot1.equals(plot2));
        plot2.setRenderer(new DefaultXYItemRenderer());
        assertTrue(plot1.equals(plot2));
        
        
        plot1.setRenderer(11, new DefaultXYItemRenderer());
        assertFalse(plot1.equals(plot2));
        plot2.setRenderer(11, new DefaultXYItemRenderer());
        assertTrue(plot1.equals(plot2));
        
        
        plot1.setDomainGridlinesVisible(false);
        assertFalse(plot1.equals(plot2));
        plot2.setDomainGridlinesVisible(false);
        assertTrue(plot1.equals(plot2));

        
        Stroke stroke = new BasicStroke(2.0f);
        plot1.setDomainGridlineStroke(stroke);
        assertFalse(plot1.equals(plot2));
        plot2.setDomainGridlineStroke(stroke);
        assertTrue(plot1.equals(plot2));
        
        
        plot1.setDomainGridlinePaint(new GradientPaint(1.0f, 2.0f, Color.blue, 
                3.0f, 4.0f, Color.red));
        assertFalse(plot1.equals(plot2));
        plot2.setDomainGridlinePaint(new GradientPaint(1.0f, 2.0f, Color.blue, 
                3.0f, 4.0f, Color.red));
        assertTrue(plot1.equals(plot2));
        
        
        plot1.setRangeGridlinesVisible(false);
        assertFalse(plot1.equals(plot2));
        plot2.setRangeGridlinesVisible(false);
        assertTrue(plot1.equals(plot2));

        
        plot1.setRangeGridlineStroke(stroke);
        assertFalse(plot1.equals(plot2));
        plot2.setRangeGridlineStroke(stroke);
        assertTrue(plot1.equals(plot2));
        
        
        plot1.setRangeGridlinePaint(new GradientPaint(1.0f, 2.0f, Color.green, 
                3.0f, 4.0f, Color.red));
        assertFalse(plot1.equals(plot2));
        plot2.setRangeGridlinePaint(new GradientPaint(1.0f, 2.0f, Color.green, 
                3.0f, 4.0f, Color.red));
        assertTrue(plot1.equals(plot2));
                
        
        plot1.setRangeZeroBaselineVisible(true);
        assertFalse(plot1.equals(plot2));
        plot2.setRangeZeroBaselineVisible(true);
        assertTrue(plot1.equals(plot2));

        
        plot1.setRangeZeroBaselineStroke(stroke);
        assertFalse(plot1.equals(plot2));
        plot2.setRangeZeroBaselineStroke(stroke);
        assertTrue(plot1.equals(plot2));
        
        
        plot1.setRangeZeroBaselinePaint(new GradientPaint(1.0f, 2.0f, Color.white, 
                3.0f, 4.0f, Color.red));
        assertFalse(plot1.equals(plot2));
        plot2.setRangeZeroBaselinePaint(new GradientPaint(1.0f, 2.0f, Color.white, 
                3.0f, 4.0f, Color.red));
        assertTrue(plot1.equals(plot2));

        
        plot1.setRangeCrosshairVisible(true);
        assertFalse(plot1.equals(plot2));
        plot2.setRangeCrosshairVisible(true);
        assertTrue(plot1.equals(plot2));
        
        
        plot1.setRangeCrosshairValue(100.0);
        assertFalse(plot1.equals(plot2));
        plot2.setRangeCrosshairValue(100.0);
        assertTrue(plot1.equals(plot2));
        
        
        plot1.setRangeCrosshairStroke(stroke);
        assertFalse(plot1.equals(plot2));
        plot2.setRangeCrosshairStroke(stroke);
        assertTrue(plot1.equals(plot2));
        
        
        plot1.setRangeCrosshairPaint(new GradientPaint(1.0f, 2.0f, Color.pink, 
                3.0f, 4.0f, Color.red));
        assertFalse(plot1.equals(plot2));
        plot2.setRangeCrosshairPaint(new GradientPaint(1.0f, 2.0f, Color.pink, 
                3.0f, 4.0f, Color.red));
        assertTrue(plot1.equals(plot2));
        
        
        plot1.setRangeCrosshairLockedOnData(false);
        assertFalse(plot1.equals(plot2));
        plot2.setRangeCrosshairLockedOnData(false);
        assertTrue(plot1.equals(plot2));
        
        
        plot1.addRangeMarker(new ValueMarker(4.0));
        assertFalse(plot1.equals(plot2));
        plot2.addRangeMarker(new ValueMarker(4.0));
        assertTrue(plot1.equals(plot2));
        
        
        plot1.addRangeMarker(1, new ValueMarker(4.0), Layer.FOREGROUND);
        assertFalse(plot1.equals(plot2));
        plot2.addRangeMarker(1, new ValueMarker(4.0), Layer.FOREGROUND);
        assertTrue(plot1.equals(plot2));
        
        plot1.addRangeMarker(1, new ValueMarker(99.0), Layer.BACKGROUND);
        assertFalse(plot1.equals(plot2));
        plot2.addRangeMarker(1, new ValueMarker(99.0), Layer.BACKGROUND);
        assertTrue(plot1.equals(plot2));
                
        
        plot1.setWeight(3);
        assertFalse(plot1.equals(plot2));
        plot2.setWeight(3);
        assertTrue(plot1.equals(plot2));
        
        
        plot1.setQuadrantOrigin(new Point2D.Double(12.3, 45.6));
        assertFalse(plot1.equals(plot2));
        plot2.setQuadrantOrigin(new Point2D.Double(12.3, 45.6));
        assertTrue(plot1.equals(plot2));
        
        
        plot1.setQuadrantPaint(0, new GradientPaint(1.0f, 2.0f, Color.red,
                3.0f, 4.0f, Color.blue));
        assertFalse(plot1.equals(plot2));
        plot2.setQuadrantPaint(0, new GradientPaint(1.0f, 2.0f, Color.red,
                3.0f, 4.0f, Color.blue));
        assertTrue(plot1.equals(plot2));
        plot1.setQuadrantPaint(1, new GradientPaint(2.0f, 3.0f, Color.red,
                4.0f, 5.0f, Color.blue));
        assertFalse(plot1.equals(plot2));
        plot2.setQuadrantPaint(1, new GradientPaint(2.0f, 3.0f, Color.red,
                4.0f, 5.0f, Color.blue));
        assertTrue(plot1.equals(plot2));
        plot1.setQuadrantPaint(2, new GradientPaint(3.0f, 4.0f, Color.red,
                5.0f, 6.0f, Color.blue));
        assertFalse(plot1.equals(plot2));
        plot2.setQuadrantPaint(2, new GradientPaint(3.0f, 4.0f, Color.red,
                5.0f, 6.0f, Color.blue));
        assertTrue(plot1.equals(plot2));
        plot1.setQuadrantPaint(3, new GradientPaint(4.0f, 5.0f, Color.red,
                6.0f, 7.0f, Color.blue));
        assertFalse(plot1.equals(plot2));
        plot2.setQuadrantPaint(3, new GradientPaint(4.0f, 5.0f, Color.red,
                6.0f, 7.0f, Color.blue));
        assertTrue(plot1.equals(plot2));  
        
        plot1.setDomainTickBandPaint(Color.red);
        assertFalse(plot1.equals(plot2));
        plot2.setDomainTickBandPaint(Color.red);
        assertTrue(plot1.equals(plot2));
        
        plot1.setRangeTickBandPaint(Color.blue);
        assertFalse(plot1.equals(plot2));
        plot2.setRangeTickBandPaint(Color.blue);
        assertTrue(plot1.equals(plot2));
        
    }

// org.jfree.chart.plot.junit.XYPlotTests::testCloning
    public void testCloning() {
        XYPlot p1 = new XYPlot();
        XYPlot p2 = null;
        try {
            p2 = (XYPlot) p1.clone();
        }
        catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        assertTrue(p1 != p2);
        assertTrue(p1.getClass() == p2.getClass());
        assertTrue(p1.equals(p2));
    }

// org.jfree.chart.plot.junit.XYPlotTests::testCloning2
    public void testCloning2() {
        XYPlot p1 = new XYPlot(null, new NumberAxis("Domain Axis"), 
                new NumberAxis("Range Axis"), new StandardXYItemRenderer());
        p1.setRangeAxis(1, new NumberAxis("Range Axis 2"));
        p1.setRenderer(1, new XYBarRenderer());
        XYPlot p2 = null;
        try {
            p2 = (XYPlot) p1.clone();
        }
        catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        assertTrue(p1 != p2);
        assertTrue(p1.getClass() == p2.getClass());
        assertTrue(p1.equals(p2));
    }

// org.jfree.chart.plot.junit.XYPlotTests::testCloning_QuadrantOrigin
    public void testCloning_QuadrantOrigin() {
        XYPlot p1 = new XYPlot();
        Point2D p = new Point2D.Double(1.2, 3.4);
        p1.setQuadrantOrigin(p);
        XYPlot p2 = null;
        try {
            p2 = (XYPlot) p1.clone();
        }
        catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        assertTrue(p1 != p2);
        assertTrue(p1.getClass() == p2.getClass());
        assertTrue(p1.equals(p2));
        assertTrue(p2.getQuadrantOrigin() != p);
    }

// org.jfree.chart.plot.junit.XYPlotTests::testCloning_QuadrantPaint
    public void testCloning_QuadrantPaint() {
        XYPlot p1 = new XYPlot();
        p1.setQuadrantPaint(3, new GradientPaint(1.0f, 2.0f, Color.red, 
                3.0f, 4.0f, Color.blue));
        XYPlot p2 = null;
        try {
            p2 = (XYPlot) p1.clone();
        }
        catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        assertTrue(p1 != p2);
        assertTrue(p1.getClass() == p2.getClass());
        assertTrue(p1.equals(p2));
        
        
        p1.setQuadrantPaint(1, Color.red);
        assertFalse(p1.equals(p2));
        p2.setQuadrantPaint(1, Color.red);
        assertTrue(p1.equals(p2));
    }

// org.jfree.chart.plot.junit.XYPlotTests::testCloneIndependence
    public void testCloneIndependence() {
        XYPlot p1 = new XYPlot(null, new NumberAxis("Domain Axis"), 
                new NumberAxis("Range Axis"), new StandardXYItemRenderer());
        p1.setDomainAxis(1, new NumberAxis("Domain Axis 2"));
        p1.setDomainAxisLocation(1, AxisLocation.BOTTOM_OR_LEFT);
        p1.setRangeAxis(1, new NumberAxis("Range Axis 2"));
        p1.setRangeAxisLocation(1, AxisLocation.TOP_OR_RIGHT);
        p1.setRenderer(1, new XYBarRenderer());
        XYPlot p2 = null;        
        try {
            p2 = (XYPlot) p1.clone();
        }
        catch (CloneNotSupportedException e) {
            e.printStackTrace();
            System.err.println("Failed to clone.");
        }
        assertTrue(p1.equals(p2));
        
        p1.getDomainAxis().setLabel("Label");
        assertFalse(p1.equals(p2));
        p2.getDomainAxis().setLabel("Label");
        assertTrue(p1.equals(p2));
        
        p1.getDomainAxis(1).setLabel("S1");
        assertFalse(p1.equals(p2));
        p2.getDomainAxis(1).setLabel("S1");
        assertTrue(p1.equals(p2));
        
        p1.setDomainAxisLocation(1, AxisLocation.TOP_OR_RIGHT);
        assertFalse(p1.equals(p2));
        p2.setDomainAxisLocation(1, AxisLocation.TOP_OR_RIGHT);
        assertTrue(p1.equals(p2));
        
        p1.mapDatasetToDomainAxis(2, 1);
        assertFalse(p1.equals(p2));
        p2.mapDatasetToDomainAxis(2, 1);
        assertTrue(p1.equals(p2));

        p1.getRangeAxis().setLabel("Label");
        assertFalse(p1.equals(p2));
        p2.getRangeAxis().setLabel("Label");
        assertTrue(p1.equals(p2));
        
        p1.getRangeAxis(1).setLabel("S1");
        assertFalse(p1.equals(p2));
        p2.getRangeAxis(1).setLabel("S1");
        assertTrue(p1.equals(p2));
        
        p1.setRangeAxisLocation(1, AxisLocation.TOP_OR_LEFT);
        assertFalse(p1.equals(p2));
        p2.setRangeAxisLocation(1, AxisLocation.TOP_OR_LEFT);
        assertTrue(p1.equals(p2));
        
        p1.mapDatasetToRangeAxis(2, 1);
        assertFalse(p1.equals(p2));
        p2.mapDatasetToRangeAxis(2, 1);
        assertTrue(p1.equals(p2));

        p1.getRenderer().setBaseOutlinePaint(Color.cyan);
        assertFalse(p1.equals(p2));
        p2.getRenderer().setBaseOutlinePaint(Color.cyan);
        assertTrue(p1.equals(p2));
        
        p1.getRenderer(1).setBaseOutlinePaint(Color.red);
        assertFalse(p1.equals(p2));
        p2.getRenderer(1).setBaseOutlinePaint(Color.red);
        assertTrue(p1.equals(p2));
        
    }

// org.jfree.chart.plot.junit.XYPlotTests::testSetNullRenderer
    public void testSetNullRenderer() {
        boolean failed = false;
        try {
            XYPlot plot = new XYPlot(null, new NumberAxis("X"), 
                    new NumberAxis("Y"), null);
            plot.setRenderer(null);
        }
        catch (Exception e) {
            failed = true;
        }
        assertTrue(!failed);
    }

// org.jfree.chart.plot.junit.XYPlotTests::testSerialization1
    public void testSerialization1() {

        XYDataset data = new XYSeriesCollection();
        NumberAxis domainAxis = new NumberAxis("Domain");
        NumberAxis rangeAxis = new NumberAxis("Range");
        StandardXYItemRenderer renderer = new StandardXYItemRenderer();
        XYPlot p1 = new XYPlot(data, domainAxis, rangeAxis, renderer);
        XYPlot p2 = null;

        try {
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            ObjectOutput out = new ObjectOutputStream(buffer);
            out.writeObject(p1);
            out.close();

            ObjectInput in = new ObjectInputStream(
                new ByteArrayInputStream(buffer.toByteArray())
            );
            p2 = (XYPlot) in.readObject();
            in.close();
        }
        catch (Exception e) {
            fail(e.toString());
        }
        assertEquals(p1, p2);

    }

// org.jfree.chart.plot.junit.XYPlotTests::testSerialization2
    public void testSerialization2() {

        IntervalXYDataset data1 = createDataset1();
        XYItemRenderer renderer1 = new XYBarRenderer(0.20);
        renderer1.setBaseToolTipGenerator(
                StandardXYToolTipGenerator.getTimeSeriesInstance());
        XYPlot p1 = new XYPlot(data1, new DateAxis("Date"), null, renderer1);
        XYPlot p2 = null;

        try {
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            ObjectOutput out = new ObjectOutputStream(buffer);
            out.writeObject(p1);
            out.close();

            ObjectInput in = new ObjectInputStream(
                    new ByteArrayInputStream(buffer.toByteArray()));
            p2 = (XYPlot) in.readObject();
            in.close();
        }
        catch (Exception e) {
            fail(e.toString());
        }
        assertEquals(p1, p2);

    }

// org.jfree.chart.plot.junit.XYPlotTests::testSerialization3
    public void testSerialization3() {
        
        XYSeriesCollection dataset = new XYSeriesCollection();
        JFreeChart chart = ChartFactory.createXYLineChart(
            "Test Chart",
            "Domain Axis",
            "Range Axis",
            dataset,
            PlotOrientation.VERTICAL,
            true,
            true,
            false
        );
        JFreeChart chart2 = null;
        
        
        try {
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            ObjectOutput out = new ObjectOutputStream(buffer);
            out.writeObject(chart);
            out.close();

            ObjectInput in = new ObjectInputStream(
                new ByteArrayInputStream(buffer.toByteArray())
            );
            chart2 = (JFreeChart) in.readObject();
            in.close();
        }
        catch (Exception e) {
            fail(e.toString());
        }

        assertEquals(chart, chart2);
        boolean passed = true;
        try {
            chart2.createBufferedImage(300, 200);
        }
        catch (Exception e) {
            passed = false;  
            e.printStackTrace();            
        }
        assertTrue(passed);
    }

// org.jfree.chart.plot.junit.XYPlotTests::testSerialization4
    public void testSerialization4() {
        
        XYSeriesCollection dataset = new XYSeriesCollection();
        JFreeChart chart = ChartFactory.createXYLineChart(
            "Test Chart",
            "Domain Axis",
            "Range Axis",
            dataset,
            PlotOrientation.VERTICAL,
            true,
            true,
            false
        );
        XYPlot plot = (XYPlot) chart.getPlot();
        plot.addDomainMarker(new ValueMarker(1.0), Layer.FOREGROUND);
        plot.addDomainMarker(new IntervalMarker(2.0, 3.0), Layer.BACKGROUND);
        plot.addRangeMarker(new ValueMarker(4.0), Layer.FOREGROUND);
        plot.addRangeMarker(new IntervalMarker(5.0, 6.0), Layer.BACKGROUND);
        JFreeChart chart2 = null;
        
        
        try {
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            ObjectOutput out = new ObjectOutputStream(buffer);
            out.writeObject(chart);
            out.close();

            ObjectInput in = new ObjectInputStream(
                new ByteArrayInputStream(buffer.toByteArray())
            );
            chart2 = (JFreeChart) in.readObject();
            in.close();
        }
        catch (Exception e) {
            fail(e.toString());
        }

        assertEquals(chart, chart2);
        boolean passed = true;
        try {
            chart2.createBufferedImage(300, 200);
        }
        catch (Exception e) {
            passed = false;  
            e.printStackTrace();            
        }
        assertTrue(passed);
    }

// org.jfree.chart.plot.junit.XYPlotTests::testSerialization5
    public void testSerialization5() {
        XYSeriesCollection dataset1 = new XYSeriesCollection();
        NumberAxis domainAxis1 = new NumberAxis("Domain 1");
        NumberAxis rangeAxis1 = new NumberAxis("Range 1");
        StandardXYItemRenderer renderer1 = new StandardXYItemRenderer();
        XYPlot p1 = new XYPlot(dataset1, domainAxis1, rangeAxis1, renderer1);
        NumberAxis domainAxis2 = new NumberAxis("Domain 2");
        NumberAxis rangeAxis2 = new NumberAxis("Range 2");
        StandardXYItemRenderer renderer2 = new StandardXYItemRenderer();
        XYSeriesCollection dataset2 = new XYSeriesCollection();
        p1.setDataset(1, dataset2);
        p1.setDomainAxis(1, domainAxis2);
        p1.setRangeAxis(1, rangeAxis2);
        p1.setRenderer(1, renderer2);
        XYPlot p2 = null;
        try {
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            ObjectOutput out = new ObjectOutputStream(buffer);
            out.writeObject(p1);
            out.close();
            ObjectInput in = new ObjectInputStream(
                new ByteArrayInputStream(buffer.toByteArray())
            );
            p2 = (XYPlot) in.readObject();
            in.close();
        }
        catch (Exception e) {
            fail(e.toString());
        }
        assertEquals(p1, p2);
        
        
        
        NumberAxis domainAxisA = (NumberAxis) p2.getDomainAxis(0);
        NumberAxis rangeAxisA = (NumberAxis) p2.getRangeAxis(0);
        XYSeriesCollection datasetA = (XYSeriesCollection) p2.getDataset(0);
        StandardXYItemRenderer rendererA 
            = (StandardXYItemRenderer) p2.getRenderer(0);
        NumberAxis domainAxisB = (NumberAxis) p2.getDomainAxis(1);
        NumberAxis rangeAxisB = (NumberAxis) p2.getRangeAxis(1);
        XYSeriesCollection datasetB = (XYSeriesCollection) p2.getDataset(1);
        StandardXYItemRenderer rendererB 
            = (StandardXYItemRenderer) p2.getRenderer(1);
        assertTrue(datasetA.hasListener(p2));
        assertTrue(domainAxisA.hasListener(p2));
        assertTrue(rangeAxisA.hasListener(p2));
        assertTrue(rendererA.hasListener(p2));
        assertTrue(datasetB.hasListener(p2));
        assertTrue(domainAxisB.hasListener(p2));
        assertTrue(rangeAxisB.hasListener(p2));
        assertTrue(rendererB.hasListener(p2));
    }

// org.jfree.chart.plot.junit.XYPlotTests::testGetRendererForDataset
    public void testGetRendererForDataset() {
        XYDataset d0 = new XYSeriesCollection();
        XYDataset d1 = new XYSeriesCollection();
        XYDataset d2 = new XYSeriesCollection();
        XYDataset d3 = new XYSeriesCollection();  
        XYItemRenderer r0 = new XYLineAndShapeRenderer();
        XYItemRenderer r2 = new XYLineAndShapeRenderer();
        XYPlot plot = new XYPlot();
        plot.setDataset(0, d0);
        plot.setDataset(1, d1);
        plot.setDataset(2, d2);
        plot.setRenderer(0, r0);
        
        plot.setRenderer(2, r2);
        assertEquals(r0, plot.getRendererForDataset(d0));
        assertEquals(r0, plot.getRendererForDataset(d1));
        assertEquals(r2, plot.getRendererForDataset(d2));
        assertEquals(null, plot.getRendererForDataset(d3));
        assertEquals(null, plot.getRendererForDataset(null));
    }

// org.jfree.chart.plot.junit.XYPlotTests::testGetLegendItems
    public void testGetLegendItems() {
        
        
        XYDataset d0 = createDataset1();
        XYDataset d1 = createDataset2();
        XYItemRenderer r0 = new XYLineAndShapeRenderer();
        XYPlot plot = new XYPlot();
        plot.setDataset(0, d0);
        plot.setDataset(1, d1);
        plot.setRenderer(0, r0);
        LegendItemCollection items = plot.getLegendItems();
        assertEquals(2, items.getItemCount());
    }

// org.jfree.chart.plot.junit.XYPlotTests::testSetRenderer
    public void testSetRenderer() {
        XYPlot plot = new XYPlot();
        XYItemRenderer renderer = new XYLineAndShapeRenderer();
        plot.setRenderer(renderer);
        
        
        MyPlotChangeListener listener = new MyPlotChangeListener();
        plot.addChangeListener(listener);
        renderer.setSeriesPaint(0, Color.black);
        assertTrue(listener.getEvent() != null);
    }

// org.jfree.chart.plot.junit.XYPlotTests::testRemoveAnnotation
    public void testRemoveAnnotation() {
        XYPlot plot = new XYPlot();
        XYTextAnnotation a1 = new XYTextAnnotation("X", 1.0, 2.0);
        XYTextAnnotation a2 = new XYTextAnnotation("X", 3.0, 4.0);
        XYTextAnnotation a3 = new XYTextAnnotation("X", 1.0, 2.0);
        plot.addAnnotation(a1);
        plot.addAnnotation(a2);
        plot.addAnnotation(a3);
        plot.removeAnnotation(a2);
        XYTextAnnotation x = (XYTextAnnotation) plot.getAnnotations().get(0);
        assertEquals(x, a1);
        
        
        
        assertTrue(a1.equals(a3));
        plot.removeAnnotation(a3);  
        x = (XYTextAnnotation) plot.getAnnotations().get(0);
        assertEquals(x, a3); 
    }

// org.jfree.chart.plot.junit.XYPlotTests::testAddDomainMarker
    public void testAddDomainMarker() {
        XYPlot plot = new XYPlot();
        Marker m = new ValueMarker(1.0);
        plot.addDomainMarker(m);
        List listeners = Arrays.asList(m.getListeners(
                MarkerChangeListener.class));
        assertTrue(listeners.contains(plot));
        plot.clearDomainMarkers();
        listeners = Arrays.asList(m.getListeners(MarkerChangeListener.class));
        assertFalse(listeners.contains(plot));
    }

// org.jfree.chart.plot.junit.XYPlotTests::testAddRangeMarker
    public void testAddRangeMarker() {
        XYPlot plot = new XYPlot();
        Marker m = new ValueMarker(1.0);
        plot.addRangeMarker(m);
        List listeners = Arrays.asList(m.getListeners(
                MarkerChangeListener.class));
        assertTrue(listeners.contains(plot));
        plot.clearRangeMarkers();
        listeners = Arrays.asList(m.getListeners(MarkerChangeListener.class));
        assertFalse(listeners.contains(plot));
    }

// org.jfree.chart.plot.junit.XYPlotTests::test1654215
    public void test1654215() {
        DefaultXYDataset dataset = new DefaultXYDataset();
        JFreeChart chart = ChartFactory.createXYLineChart("Title", "X", "Y",
                dataset, PlotOrientation.VERTICAL, true, false, false);
        XYPlot plot = (XYPlot) chart.getPlot();
        plot.setRenderer(1, new XYLineAndShapeRenderer());
        boolean success = false;
        try {
            BufferedImage image = new BufferedImage(200 , 100, 
                    BufferedImage.TYPE_INT_RGB);
            Graphics2D g2 = image.createGraphics();
            chart.draw(g2, new Rectangle2D.Double(0, 0, 200, 100), null, null);
            g2.dispose();
            success = true;
        }
        catch (Exception e) {
            e.printStackTrace();
            success = false;
        }
        assertTrue(success);
    }

// org.jfree.chart.plot.junit.XYPlotTests::testDrawRangeGridlines
    public void testDrawRangeGridlines() {
        DefaultXYDataset dataset = new DefaultXYDataset();
        JFreeChart chart = ChartFactory.createXYLineChart("Title", "X", "Y",
                dataset, PlotOrientation.VERTICAL, true, false, false);
        XYPlot plot = (XYPlot) chart.getPlot();
        plot.setRenderer(null);
        boolean success = false;
        try {
            BufferedImage image = new BufferedImage(200 , 100, 
                    BufferedImage.TYPE_INT_RGB);
            Graphics2D g2 = image.createGraphics();
            chart.draw(g2, new Rectangle2D.Double(0, 0, 200, 100), null, null);
            g2.dispose();
            success = true;
        }
        catch (Exception e) {
            e.printStackTrace();
            success = false;
        }
        assertTrue(success);
    }

// org.jfree.chart.plot.junit.XYPlotTests::testDrawSeriesWithZeroItems
    public void testDrawSeriesWithZeroItems() {
        DefaultXYDataset dataset = new DefaultXYDataset();
        dataset.addSeries("Series 1", new double[][] {{1.0, 2.0}, {3.0, 4.0}});
        dataset.addSeries("Series 2", new double[][] {{}, {}});
        JFreeChart chart = ChartFactory.createXYLineChart("Title", "X", "Y",
                dataset, PlotOrientation.VERTICAL, true, false, false);
        boolean success = false;
        try {
            BufferedImage image = new BufferedImage(200 , 100, 
                    BufferedImage.TYPE_INT_RGB);
            Graphics2D g2 = image.createGraphics();
            chart.draw(g2, new Rectangle2D.Double(0, 0, 200, 100), null, null);
            g2.dispose();
            success = true;
        }
        catch (Exception e) {
            e.printStackTrace();
            success = false;
        }
        assertTrue(success);
    }

// org.jfree.chart.plot.junit.XYPlotTests::testRemoveDomainMarker
    public void testRemoveDomainMarker() {
    	XYPlot plot = new XYPlot();
    	assertFalse(plot.removeDomainMarker(new ValueMarker(0.5)));
    }

// org.jfree.chart.plot.junit.XYPlotTests::testRemoveRangeMarker
    public void testRemoveRangeMarker() {
    	XYPlot plot = new XYPlot();
    	assertFalse(plot.removeRangeMarker(new ValueMarker(0.5)));
    }

// org.jfree.chart.renderer.xy.junit.StackedXYAreaRenderer2Tests::testDrawWithEmptyDataset
    public void testDrawWithEmptyDataset() {
        boolean success = false;
        JFreeChart chart = ChartFactory.createStackedXYAreaChart("title", "x",
                "y", new DefaultTableXYDataset(), PlotOrientation.VERTICAL,
                true, false, false);
        XYPlot plot = (XYPlot) chart.getPlot();
        plot.setRenderer(new StackedXYAreaRenderer2());
        try {
            BufferedImage image = new BufferedImage(200 , 100, 
                    BufferedImage.TYPE_INT_RGB);
            Graphics2D g2 = image.createGraphics();
            chart.draw(g2, new Rectangle2D.Double(0, 0, 200, 100), null, null);
            g2.dispose();
            success = true;
        }
        catch (Exception e) {
            success = false;
        }
        assertTrue(success);
    }

// org.jfree.chart.renderer.xy.junit.StackedXYAreaRenderer2Tests::testEquals
    public void testEquals() {
        StackedXYAreaRenderer2 r1 = new StackedXYAreaRenderer2();
        StackedXYAreaRenderer2 r2 = new StackedXYAreaRenderer2();
        assertEquals(r1, r2);
        assertEquals(r2, r1);
        
        r1.setRoundXCoordinates(!r1.getRoundXCoordinates());
        assertFalse(r1.equals(r2));
        r2.setRoundXCoordinates(r1.getRoundXCoordinates());
        assertTrue(r1.equals(r2));
    }

// org.jfree.chart.renderer.xy.junit.StackedXYAreaRenderer2Tests::testHashcode
    public void testHashcode() {
        StackedXYAreaRenderer2 r1 = new StackedXYAreaRenderer2();
        StackedXYAreaRenderer2 r2 = new StackedXYAreaRenderer2();
        assertTrue(r1.equals(r2));
        int h1 = r1.hashCode();
        int h2 = r2.hashCode();
        assertEquals(h1, h2);
    }

// org.jfree.chart.renderer.xy.junit.StackedXYAreaRenderer2Tests::testCloning
    public void testCloning() {
        StackedXYAreaRenderer2 r1 = new StackedXYAreaRenderer2();
        StackedXYAreaRenderer2 r2 = null;
        try {
            r2 = (StackedXYAreaRenderer2) r1.clone();
        }
        catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        assertTrue(r1 != r2);
        assertTrue(r1.getClass() == r2.getClass());
        assertTrue(r1.equals(r2));
    }

// org.jfree.chart.renderer.xy.junit.StackedXYAreaRenderer2Tests::testSerialization
    public void testSerialization() {
        StackedXYAreaRenderer2 r1 = new StackedXYAreaRenderer2();
        StackedXYAreaRenderer2 r2 = null;
        try {
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            ObjectOutput out = new ObjectOutputStream(buffer);
            out.writeObject(r1);
            out.close();

            ObjectInput in = new ObjectInputStream(
                    new ByteArrayInputStream(buffer.toByteArray()));
            r2 = (StackedXYAreaRenderer2) in.readObject();
            in.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        assertEquals(r1, r2);
    }

// org.jfree.chart.renderer.xy.junit.StackedXYAreaRenderer2Tests::testFindRangeBounds
    public void testFindRangeBounds() {
        TableXYDataset dataset 
                = RendererXYPackageTests.createTestTableXYDataset();
        JFreeChart chart = ChartFactory.createStackedXYAreaChart(
                "Test Chart", "X", "Y", dataset, PlotOrientation.VERTICAL, 
                false, false, false);
        XYPlot plot = (XYPlot) chart.getPlot();
        StackedXYAreaRenderer2 renderer = new StackedXYAreaRenderer2();
        plot.setRenderer(renderer);
        NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
        Range bounds = rangeAxis.getRange();
        assertTrue(bounds.contains(6.0));
        assertTrue(bounds.contains(8.0));
        
        
        assertNull(renderer.findRangeBounds(null));
        
        
        assertNull(renderer.findRangeBounds(new DefaultTableXYDataset()));
    }

// org.jfree.chart.renderer.xy.junit.StackedXYAreaRendererTests::testEquals
    public void testEquals() {
        StackedXYAreaRenderer r1 = new StackedXYAreaRenderer();
        StackedXYAreaRenderer r2 = new StackedXYAreaRenderer();
        assertEquals(r1, r2);
        assertEquals(r2, r1);
        
        r1.setShapePaint(new GradientPaint(1.0f, 2.0f, Color.yellow, 
                3.0f, 4.0f, Color.green));
        assertFalse(r1.equals(r2));
        r2.setShapePaint(new GradientPaint(1.0f, 2.0f, Color.yellow, 
                3.0f, 4.0f, Color.green));
        assertTrue(r1.equals(r2));
        
        Stroke s = new BasicStroke(1.23f);
        r1.setShapeStroke(s);
        assertFalse(r1.equals(r2));
        r2.setShapeStroke(s);
        assertTrue(r1.equals(r2)); 
    }

// org.jfree.chart.renderer.xy.junit.StackedXYAreaRendererTests::testHashcode
    public void testHashcode() {
        StackedXYAreaRenderer r1 = new StackedXYAreaRenderer();
        StackedXYAreaRenderer r2 = new StackedXYAreaRenderer();
        assertTrue(r1.equals(r2));
        int h1 = r1.hashCode();
        int h2 = r2.hashCode();
        assertEquals(h1, h2);
    }

// org.jfree.chart.renderer.xy.junit.StackedXYAreaRendererTests::testCloning
    public void testCloning() {
        StackedXYAreaRenderer r1 = new StackedXYAreaRenderer();
        StackedXYAreaRenderer r2 = null;
        try {
            r2 = (StackedXYAreaRenderer) r1.clone();
        }
        catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        assertTrue(r1 != r2);
        assertTrue(r1.getClass() == r2.getClass());
        assertTrue(r1.equals(r2));
    }

// org.jfree.chart.renderer.xy.junit.StackedXYAreaRendererTests::testSerialization
    public void testSerialization() {
        StackedXYAreaRenderer r1 = new StackedXYAreaRenderer();
        r1.setShapePaint(Color.red);
        r1.setShapeStroke(new BasicStroke(1.23f));
        StackedXYAreaRenderer r2 = null;
        try {
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            ObjectOutput out = new ObjectOutputStream(buffer);
            out.writeObject(r1);
            out.close();

            ObjectInput in = new ObjectInputStream(
                    new ByteArrayInputStream(buffer.toByteArray()));
            r2 = (StackedXYAreaRenderer) in.readObject();
            in.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        assertEquals(r1, r2);
    }

// org.jfree.chart.renderer.xy.junit.StackedXYAreaRendererTests::testFindRangeBounds
    public void testFindRangeBounds() {
        TableXYDataset dataset 
                = RendererXYPackageTests.createTestTableXYDataset();
        JFreeChart chart = ChartFactory.createStackedXYAreaChart(
                "Test Chart", "X", "Y", dataset, PlotOrientation.VERTICAL, 
                false, false, false);
        XYPlot plot = (XYPlot) chart.getPlot();
        NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
        Range bounds = rangeAxis.getRange();
        assertTrue(bounds.contains(6.0));
        assertTrue(bounds.contains(8.0));
    }

// org.jfree.chart.renderer.xy.junit.StackedXYAreaRendererTests::testDrawWithNullInfo
    public void testDrawWithNullInfo() {
        boolean success = false;
        try {
            DefaultTableXYDataset dataset = new DefaultTableXYDataset();
        
            XYSeries s1 = new XYSeries("Series 1", true, false);
            s1.add(5.0, 5.0);
            s1.add(10.0, 15.5);
            s1.add(15.0, 9.5);
            s1.add(20.0, 7.5);
            dataset.addSeries(s1);
        
            XYSeries s2 = new XYSeries("Series 2", true, false);
            s2.add(5.0, 5.0);
            s2.add(10.0, 15.5);
            s2.add(15.0, 9.5);
            s2.add(20.0, 3.5);
            dataset.addSeries(s2);
            XYPlot plot = new XYPlot(dataset, 
                    new NumberAxis("X"), new NumberAxis("Y"), 
                    new StackedXYAreaRenderer());
            JFreeChart chart = new JFreeChart(plot);
             chart.createBufferedImage(300, 200, 
                    null);
            success = true;
        }
        catch (NullPointerException e) {
            e.printStackTrace();
            success = false;
        }
        assertTrue(success);
    }

// org.jfree.chart.renderer.xy.junit.StackedXYAreaRendererTests::testBug1593156
    public void testBug1593156() {
        boolean success = false;
        try {
            DefaultTableXYDataset dataset = new DefaultTableXYDataset();
        
            XYSeries s1 = new XYSeries("Series 1", true, false);
            s1.add(5.0, 5.0);
            s1.add(10.0, 15.5);
            s1.add(15.0, 9.5);
            s1.add(20.0, 7.5);
            dataset.addSeries(s1);
        
            XYSeries s2 = new XYSeries("Series 2", true, false);
            s2.add(5.0, 5.0);
            s2.add(10.0, 15.5);
            s2.add(15.0, 9.5);
            s2.add(20.0, 3.5);
            dataset.addSeries(s2);
            StackedXYAreaRenderer renderer = new StackedXYAreaRenderer(
                    XYAreaRenderer.LINES);
            XYPlot plot = new XYPlot(dataset, 
                    new NumberAxis("X"), new NumberAxis("Y"), 
                    renderer);
            JFreeChart chart = new JFreeChart(plot);
             chart.createBufferedImage(300, 200, 
                    null);
            success = true;
        }
        catch (NullPointerException e) {
            e.printStackTrace();
            success = false;
        }
        assertTrue(success);
    }

// org.jfree.chart.renderer.xy.junit.StackedXYBarRendererTests::testEquals
    public void testEquals() {
        StackedXYBarRenderer r1 = new StackedXYBarRenderer();
        StackedXYBarRenderer r2 = new StackedXYBarRenderer();
        assertTrue(r1.equals(r2));
        assertTrue(r2.equals(r1));
        
        r1.setRenderAsPercentages(true);
        assertFalse(r1.equals(r2));
        r2.setRenderAsPercentages(true);
        assertTrue(r1.equals(r2));
    }

// org.jfree.chart.renderer.xy.junit.StackedXYBarRendererTests::testHashcode
    public void testHashcode() {
        StackedXYBarRenderer r1 = new StackedXYBarRenderer();
        StackedXYBarRenderer r2 = new StackedXYBarRenderer();
        assertTrue(r1.equals(r2));
        int h1 = r1.hashCode();
        int h2 = r2.hashCode();
        assertEquals(h1, h2);
        
        r1.setRenderAsPercentages(true);
        h1 = r1.hashCode();
        h2 = r2.hashCode();
        assertFalse(h1 == h2);
    }

// org.jfree.chart.renderer.xy.junit.StackedXYBarRendererTests::testCloning
    public void testCloning() {
        StackedXYBarRenderer r1 = new StackedXYBarRenderer();
        StackedXYBarRenderer r2 = null;
        try {
            r2 = (StackedXYBarRenderer) r1.clone();
        }
        catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        assertTrue(r1 != r2);
        assertTrue(r1.getClass() == r2.getClass());
        assertTrue(r1.equals(r2));
    }

// org.jfree.chart.renderer.xy.junit.StackedXYBarRendererTests::testSerialization
    public void testSerialization() {
        StackedXYBarRenderer r1 = new StackedXYBarRenderer();
        r1.setSeriesPaint(0, new GradientPaint(1.0f, 2.0f, Color.red, 3.0f, 
                4.0f, Color.yellow));
        StackedXYBarRenderer r2 = null;
        try {
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            ObjectOutput out = new ObjectOutputStream(buffer);
            out.writeObject(r1);
            out.close();

            ObjectInput in = new ObjectInputStream(
                    new ByteArrayInputStream(buffer.toByteArray()));
            r2 = (StackedXYBarRenderer) in.readObject();
            in.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        assertEquals(r1, r2);
    }

// org.jfree.chart.renderer.xy.junit.StackedXYBarRendererTests::testFindDomainBounds
    public void testFindDomainBounds() {
        TableXYDataset dataset 
                = RendererXYPackageTests.createTestTableXYDataset();
        JFreeChart chart = ChartFactory.createStackedXYAreaChart(
                "Test Chart", "X", "Y", dataset, 
                PlotOrientation.VERTICAL, false, false, false);
        XYPlot plot = (XYPlot) chart.getPlot();
        plot.setRenderer(new StackedXYBarRenderer());
        NumberAxis domainAxis = (NumberAxis) plot.getDomainAxis();
        domainAxis.setAutoRangeIncludesZero(false);
        Range bounds = domainAxis.getRange();
        assertFalse(bounds.contains(0.3));
        assertTrue(bounds.contains(0.5));
        assertTrue(bounds.contains(2.5));
        assertFalse(bounds.contains(2.8));
    }

// org.jfree.chart.renderer.xy.junit.StackedXYBarRendererTests::testFindRangeBounds
    public void testFindRangeBounds() {
        TableXYDataset dataset 
                = RendererXYPackageTests.createTestTableXYDataset();
        JFreeChart chart = ChartFactory.createStackedXYAreaChart(
                "Test Chart", "X", "Y", dataset, 
                PlotOrientation.VERTICAL, false, false, false);
        XYPlot plot = (XYPlot) chart.getPlot();
        plot.setRenderer(new StackedXYBarRenderer());
        NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
        Range bounds = rangeAxis.getRange();
        assertTrue(bounds.contains(6.0));
        assertTrue(bounds.contains(8.0));
    }

// org.jfree.chart.renderer.xy.junit.StandardXYItemRendererTests::testEquals
    public void testEquals() {
        StandardXYItemRenderer r1 = new StandardXYItemRenderer();
        StandardXYItemRenderer r2 = new StandardXYItemRenderer();
        assertEquals(r1, r2);
        
        r1.setBaseShapesVisible(true);
        assertFalse(r1.equals(r2));
        r2.setBaseShapesVisible(true);
        assertTrue(r1.equals(r2));
        
        r1.setPlotLines(false);
        assertFalse(r1.equals(r2));
        r2.setPlotLines(false);
        assertTrue(r1.equals(r2));

        r1.setPlotImages(true);
        assertFalse(r1.equals(r2));
        r2.setPlotImages(true);
        assertTrue(r1.equals(r2));

        r1.setPlotDiscontinuous(true);
        assertFalse(r1.equals(r2));
        r2.setPlotDiscontinuous(true);
        assertTrue(r1.equals(r2));
        
        r1.setGapThresholdType(UnitType.ABSOLUTE);
        assertFalse(r1.equals(r2));
        r2.setGapThresholdType(UnitType.ABSOLUTE);
        assertTrue(r1.equals(r2));
        
        r1.setGapThreshold(1.23);
        assertFalse(r1.equals(r2));
        r2.setGapThreshold(1.23);
        assertTrue(r1.equals(r2));
        
        r1.setLegendLine(new Line2D.Double(1.0, 2.0, 3.0, 4.0));
        assertFalse(r1.equals(r2));
        r2.setLegendLine(new Line2D.Double(1.0, 2.0, 3.0, 4.0));
        assertTrue(r1.equals(r2));
        
        r1.setSeriesShapesFilled(1, Boolean.TRUE);
        assertFalse(r1.equals(r2));
        r2.setSeriesShapesFilled(1, Boolean.TRUE);
        assertTrue(r1.equals(r2));
        
        r1.setBaseShapesFilled(false);
        assertFalse(r1.equals(r2));
        r2.setBaseShapesFilled(false);
        assertTrue(r1.equals(r2));
        
        r1.setDrawSeriesLineAsPath(true);
        assertFalse(r1.equals(r2));
        r2.setDrawSeriesLineAsPath(true);
        assertTrue(r1.equals(r2));
    }

// org.jfree.chart.renderer.xy.junit.StandardXYItemRendererTests::testHashcode
    public void testHashcode() {
        StandardXYItemRenderer r1 = new StandardXYItemRenderer();
        StandardXYItemRenderer r2 = new StandardXYItemRenderer();
        assertTrue(r1.equals(r2));
        int h1 = r1.hashCode();
        int h2 = r2.hashCode();
        assertEquals(h1, h2);
    }

// org.jfree.chart.renderer.xy.junit.StandardXYItemRendererTests::testCloning
    public void testCloning() {
        StandardXYItemRenderer r1 = new StandardXYItemRenderer();
        Rectangle2D rect1 = new Rectangle2D.Double(1.0, 2.0, 3.0, 4.0);
        r1.setLegendLine(rect1);
        StandardXYItemRenderer r2 = null;
        try {
            r2 = (StandardXYItemRenderer) r1.clone();
        }
        catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        assertTrue(r1 != r2);
        assertTrue(r1.getClass() == r2.getClass());
        assertTrue(r1.equals(r2));
        
        
        rect1.setRect(4.0, 3.0, 2.0, 1.0);
        assertFalse(r1.equals(r2));
        r2.setLegendLine(new Rectangle2D.Double(4.0, 3.0, 2.0, 1.0));
        assertTrue(r1.equals(r2));
        
        r1.setSeriesShapesFilled(1, Boolean.TRUE);
        assertFalse(r1.equals(r2));
        r2.setSeriesShapesFilled(1, Boolean.TRUE);
        assertTrue(r1.equals(r2));
        
    }

// org.jfree.chart.renderer.xy.junit.StandardXYItemRendererTests::testSerialization
    public void testSerialization() {
        StandardXYItemRenderer r1 = new StandardXYItemRenderer();
        StandardXYItemRenderer r2 = null;
        try {
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            ObjectOutput out = new ObjectOutputStream(buffer);
            out.writeObject(r1);
            out.close();

            ObjectInput in = new ObjectInputStream(
                    new ByteArrayInputStream(buffer.toByteArray()));
            r2 = (StandardXYItemRenderer) in.readObject();
            in.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        assertEquals(r1, r2);
    }

// org.jfree.chart.renderer.xy.junit.StandardXYItemRendererTests::testGetLegendItemSeriesIndex
    public void testGetLegendItemSeriesIndex() {
        XYSeriesCollection d1 = new XYSeriesCollection();
        XYSeries s1 = new XYSeries("S1");
        s1.add(1.0, 1.1);
        XYSeries s2 = new XYSeries("S2");
        s2.add(1.0, 1.1);
        d1.addSeries(s1);
        d1.addSeries(s2);
        
        XYSeriesCollection d2 = new XYSeriesCollection();
        XYSeries s3 = new XYSeries("S3");
        s3.add(1.0, 1.1);
        XYSeries s4 = new XYSeries("S4");
        s4.add(1.0, 1.1);
        XYSeries s5 = new XYSeries("S5");
        s5.add(1.0, 1.1);
        d2.addSeries(s3);
        d2.addSeries(s4);
        d2.addSeries(s5);

        StandardXYItemRenderer r = new StandardXYItemRenderer();
        XYPlot plot = new XYPlot(d1, new NumberAxis("x"),
                new NumberAxis("y"), r);
        plot.setDataset(1, d2);
         new JFreeChart(plot);
        LegendItem li = r.getLegendItem(1, 2);
        assertEquals("S5", li.getLabel());
        assertEquals(1, li.getDatasetIndex());
        assertEquals(2, li.getSeriesIndex());
    }

// org.jfree.chart.renderer.xy.junit.StandardXYItemRendererTests::testNoDisplayedItem
    public void testNoDisplayedItem() {
        XYSeriesCollection dataset = new XYSeriesCollection();
        XYSeries s1 = new XYSeries("S1");
        s1.add(10.0, 10.0);
        dataset.addSeries(s1);
        JFreeChart chart = ChartFactory.createXYLineChart("Title", "X", "Y", 
                dataset, PlotOrientation.VERTICAL, false, true, false);
        XYPlot plot = (XYPlot) chart.getPlot();
        plot.setRenderer(new StandardXYItemRenderer());
        NumberAxis xAxis = (NumberAxis) plot.getDomainAxis();
        xAxis.setRange(0.0, 5.0);
        NumberAxis yAxis = (NumberAxis) plot.getRangeAxis();
        yAxis.setRange(0.0, 5.0);
        BufferedImage image = new BufferedImage(200 , 100, 
                BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = image.createGraphics();
        ChartRenderingInfo info = new ChartRenderingInfo();
        chart.draw(g2, new Rectangle2D.Double(0, 0, 200, 100), null, info);
        g2.dispose();
        EntityCollection ec = info.getEntityCollection();
        assertFalse(TestUtilities.containsInstanceOf(ec.getEntities(), 
                XYItemEntity.class));
    }

// org.jfree.chart.renderer.xy.junit.XYBarRendererTests::testEquals
    public void testEquals() {
        
        
        XYBarRenderer r1 = new XYBarRenderer();
        XYBarRenderer r2 = new XYBarRenderer();
        assertTrue(r1.equals(r2));
        assertTrue(r2.equals(r1));
        
        
        r1.setBase(1.0);
        assertFalse(r1.equals(r2));
        r2.setBase(1.0);
        assertTrue(r1.equals(r2));
        
        
        r1.setUseYInterval(!r1.getUseYInterval());
        assertFalse(r1.equals(r2));
        r2.setUseYInterval(!r2.getUseYInterval());
        assertTrue(r1.equals(r2));
        
        
        r1.setMargin(0.10);
        assertFalse(r1.equals(r2));
        r2.setMargin(0.10);
        assertTrue(r1.equals(r2));
        
        
        r1.setDrawBarOutline(!r1.isDrawBarOutline());
        assertFalse(r1.equals(r2));
        r2.setDrawBarOutline(!r2.isDrawBarOutline());
        assertTrue(r1.equals(r2));
        
        
        r1.setGradientPaintTransformer(new StandardGradientPaintTransformer(
                GradientPaintTransformType.CENTER_HORIZONTAL));
        assertFalse(r1.equals(r2));
        r2.setGradientPaintTransformer(new StandardGradientPaintTransformer(
                GradientPaintTransformType.CENTER_HORIZONTAL));
        assertTrue(r1.equals(r2));
        
        
        r1.setLegendBar(new Rectangle2D.Double(1.0, 2.0, 3.0, 4.0));
        assertFalse(r1.equals(r2));
        r2.setLegendBar(new Rectangle2D.Double(1.0, 2.0, 3.0, 4.0));
        assertTrue(r1.equals(r2));
        
        
        r1.setPositiveItemLabelPositionFallback(new ItemLabelPosition());
        assertFalse(r1.equals(r2));
        r2.setPositiveItemLabelPositionFallback(new ItemLabelPosition());
        assertTrue(r1.equals(r2));

        
        r1.setNegativeItemLabelPositionFallback(new ItemLabelPosition());
        assertFalse(r1.equals(r2));
        r2.setNegativeItemLabelPositionFallback(new ItemLabelPosition());
        assertTrue(r1.equals(r2));
    }

// org.jfree.chart.renderer.xy.junit.XYBarRendererTests::testHashcode
    public void testHashcode() {
        XYBarRenderer r1 = new XYBarRenderer();
        XYBarRenderer r2 = new XYBarRenderer();
        assertTrue(r1.equals(r2));
        int h1 = r1.hashCode();
        int h2 = r2.hashCode();
        assertEquals(h1, h2);
    }

// org.jfree.chart.renderer.xy.junit.XYBarRendererTests::testCloning
    public void testCloning() {
        XYBarRenderer r1 = new XYBarRenderer();
        Rectangle2D rect = new Rectangle2D.Double(1.0, 2.0, 3.0, 4.0);
        r1.setLegendBar(rect);
        XYBarRenderer r2 = null;
        try {
            r2 = (XYBarRenderer) r1.clone();
        }
        catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        assertTrue(r1 != r2);
        assertTrue(r1.getClass() == r2.getClass());
        assertTrue(r1.equals(r2));
        
        
        rect.setRect(4.0, 3.0, 2.0, 1.0);
        assertFalse(r1.equals(r2));
        r2.setLegendBar(new Rectangle2D.Double(4.0, 3.0, 2.0, 1.0));
        assertTrue(r1.equals(r2));
    }

// org.jfree.chart.renderer.xy.junit.XYBarRendererTests::testSerialization
    public void testSerialization() {

        XYBarRenderer r1 = new XYBarRenderer();
        XYBarRenderer r2 = null;

        try {
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            ObjectOutput out = new ObjectOutputStream(buffer);
            out.writeObject(r1);
            out.close();

            ObjectInput in = new ObjectInputStream(
                new ByteArrayInputStream(buffer.toByteArray())
            );
            r2 = (XYBarRenderer) in.readObject();
            in.close();
        }
        catch (Exception e) {
            System.out.println(e.toString());
        }
        assertEquals(r1, r2);

    }

// org.jfree.chart.renderer.xy.junit.XYBarRendererTests::testSerialization2
    public void testSerialization2() {

        XYBarRenderer r1 = new XYBarRenderer();
        r1.setPositiveItemLabelPositionFallback(new ItemLabelPosition());
        XYBarRenderer r2 = null;

        try {
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            ObjectOutput out = new ObjectOutputStream(buffer);
            out.writeObject(r1);
            out.close();

            ObjectInput in = new ObjectInputStream(
                    new ByteArrayInputStream(buffer.toByteArray()));
            r2 = (XYBarRenderer) in.readObject();
            in.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        assertEquals(r1, r2);

    }

// org.jfree.chart.renderer.xy.junit.XYBarRendererTests::testFindDomainBounds
    public void testFindDomainBounds() {
        XYSeriesCollection dataset 
                = RendererXYPackageTests.createTestXYSeriesCollection();
        JFreeChart chart = ChartFactory.createXYBarChart("Test Chart", "X", 
                false, "Y", dataset, PlotOrientation.VERTICAL, false, false, 
                false);
        XYPlot plot = (XYPlot) chart.getPlot();
        NumberAxis domainAxis = (NumberAxis) plot.getDomainAxis();
        domainAxis.setAutoRangeIncludesZero(false);
        Range bounds = domainAxis.getRange();
        assertFalse(bounds.contains(0.3));
        assertTrue(bounds.contains(0.5));
        assertTrue(bounds.contains(2.5));
        assertFalse(bounds.contains(2.8));
    }

// org.jfree.chart.renderer.xy.junit.XYBarRendererTests::testGetLegendItemSeriesIndex
    public void testGetLegendItemSeriesIndex() {
        XYSeriesCollection d1 = new XYSeriesCollection();
        XYSeries s1 = new XYSeries("S1");
        s1.add(1.0, 1.1);
        XYSeries s2 = new XYSeries("S2");
        s2.add(1.0, 1.1);
        d1.addSeries(s1);
        d1.addSeries(s2);
        
        XYSeriesCollection d2 = new XYSeriesCollection();
        XYSeries s3 = new XYSeries("S3");
        s3.add(1.0, 1.1);
        XYSeries s4 = new XYSeries("S4");
        s4.add(1.0, 1.1);
        XYSeries s5 = new XYSeries("S5");
        s5.add(1.0, 1.1);
        d2.addSeries(s3);
        d2.addSeries(s4);
        d2.addSeries(s5);

        XYBarRenderer r = new XYBarRenderer();
        XYPlot plot = new XYPlot(new XYBarDataset(d1, 1.0), new NumberAxis("x"),
                new NumberAxis("y"), r);
        plot.setDataset(1, new XYBarDataset(d2, 2.0));
         new JFreeChart(plot);
        LegendItem li = r.getLegendItem(1, 2);
        assertEquals("S5", li.getLabel());
        assertEquals(1, li.getDatasetIndex());
        assertEquals(2, li.getSeriesIndex());
    }

// org.jfree.chart.renderer.xy.junit.XYLineAndShapeRendererTests::testEquals
    public void testEquals() {
        
        XYLineAndShapeRenderer r1 = new XYLineAndShapeRenderer();
        XYLineAndShapeRenderer r2 = new XYLineAndShapeRenderer();
        assertEquals(r1, r2);
        assertEquals(r2, r1);
        
        r1.setSeriesLinesVisible(3, true);
        assertFalse(r1.equals(r2));
        r2.setSeriesLinesVisible(3, true);
        assertTrue(r1.equals(r2));
        
        r1.setBaseLinesVisible(false);
        assertFalse(r1.equals(r2));
        r2.setBaseLinesVisible(false);
        assertTrue(r1.equals(r2));
        
        r1.setLegendLine(new Line2D.Double(1.0, 2.0, 3.0, 4.0));
        assertFalse(r1.equals(r2));
        r2.setLegendLine(new Line2D.Double(1.0, 2.0, 3.0, 4.0));
        assertTrue(r1.equals(r2));
        
        r1.setSeriesShapesVisible(3, true);
        assertFalse(r1.equals(r2));
        r2.setSeriesShapesVisible(3, true);
        assertTrue(r1.equals(r2));
        
        r1.setBaseShapesVisible(false);
        assertFalse(r1.equals(r2));
        r2.setBaseShapesVisible(false);
        assertTrue(r1.equals(r2));
        
        r1.setSeriesShapesFilled(3, true);
        assertFalse(r1.equals(r2));
        r2.setSeriesShapesFilled(3, true);
        assertTrue(r1.equals(r2));
        
        r1.setBaseShapesFilled(false);
        assertFalse(r1.equals(r2));
        r2.setBaseShapesFilled(false);
        assertTrue(r1.equals(r2));
    
        r1.setDrawOutlines(!r1.getDrawOutlines());
        assertFalse(r1.equals(r2));
        r2.setDrawOutlines(r1.getDrawOutlines());
        assertTrue(r1.equals(r2));
    
        r1.setUseOutlinePaint(true);
        assertFalse(r1.equals(r2));
        r2.setUseOutlinePaint(true);
        assertTrue(r1.equals(r2));
        
        r1.setUseFillPaint(true);
        assertFalse(r1.equals(r2));
        r2.setUseFillPaint(true);
        assertTrue(r1.equals(r2));
        
        r1.setDrawSeriesLineAsPath(true);
        assertFalse(r1.equals(r2));
        r2.setDrawSeriesLineAsPath(true);
        assertTrue(r1.equals(r2));
    }

// org.jfree.chart.renderer.xy.junit.XYLineAndShapeRendererTests::testEquals2
    public void testEquals2() {
        XYLineAndShapeRenderer r1 = new XYLineAndShapeRenderer();
        XYLineAndShapeRenderer r2 = new XYLineAndShapeRenderer();
        assertEquals(r1, r2);
        assertEquals(r2, r1);
    
        r1.setBaseURLGenerator(new TimeSeriesURLGenerator());
        assertFalse(r1.equals(r2));
        r2.setBaseURLGenerator(new TimeSeriesURLGenerator());
        assertTrue(r1.equals(r2));
    }

// org.jfree.chart.renderer.xy.junit.XYLineAndShapeRendererTests::testHashcode
    public void testHashcode() {
        XYLineAndShapeRenderer r1 = new XYLineAndShapeRenderer();
        XYLineAndShapeRenderer r2 = new XYLineAndShapeRenderer();
        assertTrue(r1.equals(r2));
        int h1 = r1.hashCode();
        int h2 = r2.hashCode();
        assertEquals(h1, h2);
    }

// org.jfree.chart.renderer.xy.junit.XYLineAndShapeRendererTests::testCloning
    public void testCloning() {
        Rectangle2D legendShape = new Rectangle2D.Double(1.0, 2.0, 3.0, 4.0);
        XYLineAndShapeRenderer r1 = new XYLineAndShapeRenderer();
        r1.setLegendLine(legendShape);
        XYLineAndShapeRenderer r2 = null;
        try {
            r2 = (XYLineAndShapeRenderer) r1.clone();
        }
        catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        assertTrue(r1 != r2);
        assertTrue(r1.getClass() == r2.getClass());
        assertTrue(r1.equals(r2));
        
        r1.setSeriesLinesVisible(0, false);
        assertFalse(r1.equals(r2));
        r2.setSeriesLinesVisible(0, false);
        assertTrue(r1.equals(r2));
        
        legendShape.setRect(4.0, 3.0, 2.0, 1.0);
        assertFalse(r1.equals(r2));
        r2.setLegendLine(new Rectangle2D.Double(4.0, 3.0, 2.0, 1.0));
        assertTrue(r1.equals(r2));
        
        r1.setSeriesShapesVisible(1, true);
        assertFalse(r1.equals(r2));
        r2.setSeriesShapesVisible(1, true);
        assertTrue(r1.equals(r2));
        
        r1.setSeriesShapesFilled(1, true);
        assertFalse(r1.equals(r2));
        r2.setSeriesShapesFilled(1, true);
        assertTrue(r1.equals(r2));
    }

// org.jfree.chart.renderer.xy.junit.XYLineAndShapeRendererTests::testSerialization
    public void testSerialization() {

        XYLineAndShapeRenderer r1 = new XYLineAndShapeRenderer();
        XYLineAndShapeRenderer r2 = null;
        try {
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            ObjectOutput out = new ObjectOutputStream(buffer);
            out.writeObject(r1);
            out.close();

            ObjectInput in = new ObjectInputStream(
                    new ByteArrayInputStream(buffer.toByteArray()));
            r2 = (XYLineAndShapeRenderer) in.readObject();
            in.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        assertEquals(r1, r2);

    }

// org.jfree.chart.renderer.xy.junit.XYLineAndShapeRendererTests::testFindDomainBounds
    public void testFindDomainBounds() {
        XYSeriesCollection dataset 
                = RendererXYPackageTests.createTestXYSeriesCollection();
        JFreeChart chart = ChartFactory.createXYLineChart(
                "Test Chart", "X", "Y", dataset, PlotOrientation.VERTICAL, 
                false, false, false);
        XYPlot plot = (XYPlot) chart.getPlot();
        NumberAxis domainAxis = (NumberAxis) plot.getDomainAxis();
        domainAxis.setAutoRangeIncludesZero(false);
        Range bounds = domainAxis.getRange();
        assertFalse(bounds.contains(0.9));
        assertTrue(bounds.contains(1.0));
        assertTrue(bounds.contains(2.0));
        assertFalse(bounds.contains(2.10));
    }

// org.jfree.chart.renderer.xy.junit.XYLineAndShapeRendererTests::testFindRangeBounds
    public void testFindRangeBounds() {
        TableXYDataset dataset 
                = RendererXYPackageTests.createTestTableXYDataset();
        JFreeChart chart = ChartFactory.createXYLineChart(
                "Test Chart", "X", "Y", dataset, PlotOrientation.VERTICAL, 
                false, false, false);
        XYPlot plot = (XYPlot) chart.getPlot();
        NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
        rangeAxis.setAutoRangeIncludesZero(false);
        Range bounds = rangeAxis.getRange();
        assertFalse(bounds.contains(1.0));
        assertTrue(bounds.contains(2.0));
        assertTrue(bounds.contains(5.0));
        assertFalse(bounds.contains(6.0));
    }

// org.jfree.chart.renderer.xy.junit.XYLineAndShapeRendererTests::testGetLegendItemSeriesIndex
    public void testGetLegendItemSeriesIndex() {
        XYSeriesCollection d1 = new XYSeriesCollection();
        XYSeries s1 = new XYSeries("S1");
        s1.add(1.0, 1.1);
        XYSeries s2 = new XYSeries("S2");
        s2.add(1.0, 1.1);
        d1.addSeries(s1);
        d1.addSeries(s2);
        
        XYSeriesCollection d2 = new XYSeriesCollection();
        XYSeries s3 = new XYSeries("S3");
        s3.add(1.0, 1.1);
        XYSeries s4 = new XYSeries("S4");
        s4.add(1.0, 1.1);
        XYSeries s5 = new XYSeries("S5");
        s5.add(1.0, 1.1);
        d2.addSeries(s3);
        d2.addSeries(s4);
        d2.addSeries(s5);

        XYLineAndShapeRenderer r = new XYLineAndShapeRenderer();
        XYPlot plot = new XYPlot(d1, new NumberAxis("x"),
                new NumberAxis("y"), r);
        plot.setDataset(1, d2);
         new JFreeChart(plot);
        LegendItem li = r.getLegendItem(1, 2);
        assertEquals("S5", li.getLabel());
        assertEquals(1, li.getDatasetIndex());
        assertEquals(2, li.getSeriesIndex());
    }

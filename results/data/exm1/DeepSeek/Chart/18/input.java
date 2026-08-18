// buggy code
    public void removeValue(int index) {
        this.keys.remove(index);
        this.values.remove(index);
        if (index < this.keys.size()) {
        rebuildIndex();
        }
    }

    public void removeValue(Comparable key) {
        int index = getIndex(key);
        if (index < 0) {
			return;
        }
        removeValue(index);
    }

    public void removeColumn(Comparable columnKey) {
        Iterator iterator = this.rows.iterator();
        while (iterator.hasNext()) {
            DefaultKeyedValues rowData = (DefaultKeyedValues) iterator.next();
                rowData.removeValue(columnKey);
        }
        this.columnKeys.remove(columnKey);
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
    public void testEquals() {
        
        LogAxis a1 = new LogAxis("Test");
        LogAxis a2 = new LogAxis("Test");
        assertTrue(a1.equals(a2));

        a1.setBase(2.0);
        assertFalse(a1.equals(a2));
        a2.setBase(2.0);
        assertTrue(a1.equals(a2));
        
        a1.setSmallestValue(0.1);
        assertFalse(a1.equals(a2));
        a2.setSmallestValue(0.1);
        assertTrue(a1.equals(a2));
        
        a1.setMinorTickCount(9);
        assertFalse(a1.equals(a2));
        a2.setMinorTickCount(9);
        assertTrue(a1.equals(a2));
    }

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

// org.jfree.chart.entity.junit.CategoryItemEntityTests::testEquals
    public void testEquals() {
        DefaultCategoryDataset d = new DefaultCategoryDataset();
        d.addValue(1.0, "R1", "C1");
        d.addValue(2.0, "R1", "C2");
        d.addValue(3.0, "R2", "C1");
        d.addValue(4.0, "R2", "C2");
        CategoryItemEntity e1 = new CategoryItemEntity(new Rectangle2D.Double(
                1.0, 2.0, 3.0, 4.0), "ToolTip", "URL", d, "R2", "C2"); 
        CategoryItemEntity e2 = new CategoryItemEntity(new Rectangle2D.Double(
                1.0, 2.0, 3.0, 4.0), "ToolTip", "URL", d, "R2", "C2"); 
        assertTrue(e1.equals(e2));  
        
        e1.setArea(new Rectangle2D.Double(4.0, 3.0, 2.0, 1.0));
        assertFalse(e1.equals(e2));
        e2.setArea(new Rectangle2D.Double(4.0, 3.0, 2.0, 1.0));
        assertTrue(e1.equals(e2));  

        e1.setToolTipText("New ToolTip");
        assertFalse(e1.equals(e2));
        e2.setToolTipText("New ToolTip");
        assertTrue(e1.equals(e2));  

        e1.setURLText("New URL");
        assertFalse(e1.equals(e2));
        e2.setURLText("New URL");
        assertTrue(e1.equals(e2));  
        
        e1.setColumnKey("C1");
        assertFalse(e1.equals(e2));
        e2.setColumnKey("C1");
        assertTrue(e1.equals(e2)); 
        
        e1.setRowKey("R1");
        assertFalse(e1.equals(e2));
        e2.setRowKey("R1");
        assertTrue(e1.equals(e2)); 
        
    }

// org.jfree.chart.entity.junit.CategoryItemEntityTests::testCloning
    public void testCloning() {
        DefaultCategoryDataset d = new DefaultCategoryDataset();
        d.addValue(1.0, "R1", "C1");
        d.addValue(2.0, "R1", "C2");
        d.addValue(3.0, "R2", "C1");
        d.addValue(4.0, "R2", "C2");
        CategoryItemEntity e1 = new CategoryItemEntity(new Rectangle2D.Double(
                1.0, 2.0, 3.0, 4.0), "ToolTip", "URL", d, "C2", "C2"); 
        CategoryItemEntity e2 = null;
        
        try {
            e2 = (CategoryItemEntity) e1.clone();
        }
        catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        assertTrue(e1 != e2);
        assertTrue(e1.getClass() == e2.getClass());
        assertTrue(e1.equals(e2));
    }

// org.jfree.chart.entity.junit.CategoryItemEntityTests::testSerialization
    public void testSerialization() {
        DefaultCategoryDataset d = new DefaultCategoryDataset();
        d.addValue(1.0, "R1", "C1");
        d.addValue(2.0, "R1", "C2");
        d.addValue(3.0, "R2", "C1");
        d.addValue(4.0, "R2", "C2");
        CategoryItemEntity e1 = new CategoryItemEntity(new Rectangle2D.Double(
                1.0, 2.0, 3.0, 4.0), "ToolTip", "URL", d, "R2", "C2"); 
        CategoryItemEntity e2 = null;
        try {
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            ObjectOutput out = new ObjectOutputStream(buffer);
            out.writeObject(e1);
            out.close();

            ObjectInput in = new ObjectInputStream(
                    new ByteArrayInputStream(buffer.toByteArray()));
            e2 = (CategoryItemEntity) in.readObject();
            in.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        assertEquals(e1, e2);
    }

// org.jfree.chart.entity.junit.LegendItemEntityTests::testEquals
    public void testEquals() {
        LegendItemEntity e1 = new LegendItemEntity(new Rectangle2D.Double(1.0, 
                2.0, 3.0, 4.0)); 
        LegendItemEntity e2 = new LegendItemEntity(new Rectangle2D.Double(1.0,
                2.0, 3.0, 4.0)); 
        assertTrue(e1.equals(e2));  
        
        e1.setArea(new Rectangle2D.Double(4.0, 3.0, 2.0, 1.0));
        assertFalse(e1.equals(e2));
        e2.setArea(new Rectangle2D.Double(4.0, 3.0, 2.0, 1.0));
        assertTrue(e1.equals(e2));  

        e1.setToolTipText("New ToolTip");
        assertFalse(e1.equals(e2));
        e2.setToolTipText("New ToolTip");
        assertTrue(e1.equals(e2));  

        e1.setURLText("New URL");
        assertFalse(e1.equals(e2));
        e2.setURLText("New URL");
        assertTrue(e1.equals(e2));
        
        e1.setDataset(new DefaultCategoryDataset());
        assertFalse(e1.equals(e2));
        e2.setDataset(new DefaultCategoryDataset());
        assertTrue(e1.equals(e2));
        
        e1.setSeriesKey("A");
        assertFalse(e1.equals(e2));
        e2.setSeriesKey("A");
        assertTrue(e1.equals(e2));
    }

// org.jfree.chart.entity.junit.LegendItemEntityTests::testCloning
    public void testCloning() {
        LegendItemEntity e1 = new LegendItemEntity(new Rectangle2D.Double(1.0, 
                2.0, 3.0, 4.0)); 
        LegendItemEntity e2 = null;
        
        try {
            e2 = (LegendItemEntity) e1.clone();
        }
        catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        assertTrue(e1 != e2);
        assertTrue(e1.getClass() == e2.getClass());
        assertTrue(e1.equals(e2));
    }

// org.jfree.chart.entity.junit.LegendItemEntityTests::testSerialization
    public void testSerialization() {
        LegendItemEntity e1 = new LegendItemEntity(new Rectangle2D.Double(1.0, 
                2.0, 3.0, 4.0)); 
        LegendItemEntity e2 = null;
        try {
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            ObjectOutput out = new ObjectOutputStream(buffer);
            out.writeObject(e1);
            out.close();

            ObjectInput in = new ObjectInputStream(
                    new ByteArrayInputStream(buffer.toByteArray()));
            e2 = (LegendItemEntity) in.readObject();
            in.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        assertEquals(e1, e2);
    }

// org.jfree.chart.entity.junit.PieSectionEntityTests::testEquals
    public void testEquals() {
        PieSectionEntity e1 = new PieSectionEntity(
            new Rectangle2D.Double(1.0, 2.0, 3.0, 4.0), 
            new DefaultPieDataset(), 1, 2, "Key", "ToolTip", "URL"
        ); 
        PieSectionEntity e2 = new PieSectionEntity(
            new Rectangle2D.Double(1.0, 2.0, 3.0, 4.0), 
            new DefaultPieDataset(), 1, 2, "Key", "ToolTip", "URL"
        ); 
        assertTrue(e1.equals(e2));  
        
        e1.setArea(new Rectangle2D.Double(4.0, 3.0, 2.0, 1.0));
        assertFalse(e1.equals(e2));
        e2.setArea(new Rectangle2D.Double(4.0, 3.0, 2.0, 1.0));
        assertTrue(e1.equals(e2));  

        e1.setToolTipText("New ToolTip");
        assertFalse(e1.equals(e2));
        e2.setToolTipText("New ToolTip");
        assertTrue(e1.equals(e2));  

        e1.setURLText("New URL");
        assertFalse(e1.equals(e2));
        e2.setURLText("New URL");
        assertTrue(e1.equals(e2));  
    }

// org.jfree.chart.entity.junit.PieSectionEntityTests::testCloning
    public void testCloning() {
        PieSectionEntity e1 = new PieSectionEntity(
            new Rectangle2D.Double(1.0, 2.0, 3.0, 4.0), 
            new DefaultPieDataset(), 1, 2, "Key", "ToolTip", "URL"
        ); 
        PieSectionEntity e2 = null;
        try {
            e2 = (PieSectionEntity) e1.clone();
        }
        catch (CloneNotSupportedException e) {
            System.err.println("Failed to clone.");
        }
        assertTrue(e1 != e2);
        assertTrue(e1.getClass() == e2.getClass());
        assertTrue(e1.equals(e2));
    }

// org.jfree.chart.entity.junit.PieSectionEntityTests::testSerialization
    public void testSerialization() {
        PieSectionEntity e1 = new PieSectionEntity(
            new Rectangle2D.Double(1.0, 2.0, 3.0, 4.0), 
            new DefaultPieDataset(), 1, 2, "Key", "ToolTip", "URL"
        ); 
        PieSectionEntity e2 = null;
        try {
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            ObjectOutput out = new ObjectOutputStream(buffer);
            out.writeObject(e1);
            out.close();

            ObjectInput in = new ObjectInputStream(
                new ByteArrayInputStream(buffer.toByteArray())
            );
            e2 = (PieSectionEntity) in.readObject();
            in.close();
        }
        catch (Exception e) {
            System.out.println(e.toString());
        }
        assertEquals(e1, e2);
    }

// org.jfree.chart.entity.junit.StandardEntityCollectionTests::testEquals
    public void testEquals() {
        StandardEntityCollection c1 = new StandardEntityCollection();
        StandardEntityCollection c2 = new StandardEntityCollection();
        assertTrue(c1.equals(c2));
        
        PieSectionEntity e1 = new PieSectionEntity(new Rectangle2D.Double(1.0, 
                2.0, 3.0, 4.0), new DefaultPieDataset(), 0, 1, "Key", 
                "ToolTip", "URL");
        c1.add(e1);
        assertFalse(c1.equals(c2));
        PieSectionEntity e2 = new PieSectionEntity(new Rectangle2D.Double(1.0, 
                2.0, 3.0, 4.0), new DefaultPieDataset(), 0, 1, "Key", 
                "ToolTip", "URL");
        c2.add(e2);
        assertTrue(c1.equals(c2));        
    }

// org.jfree.chart.entity.junit.StandardEntityCollectionTests::testCloning
    public void testCloning() {
        PieSectionEntity e1 = new PieSectionEntity(new Rectangle2D.Double(1.0, 
                2.0, 3.0, 4.0), new DefaultPieDataset(), 0, 1, "Key", 
                "ToolTip", "URL");
        StandardEntityCollection c1 = new StandardEntityCollection();
        c1.add(e1);
        StandardEntityCollection c2 = null;
        try {
            c2 = (StandardEntityCollection) c1.clone();
        }
        catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        assertTrue(c1 != c2);
        assertTrue(c1.getClass() == c2.getClass());
        assertTrue(c1.equals(c2));
        
        
        c1.clear();
        assertFalse(c1.equals(c2));
        c2.clear();
        assertTrue(c1.equals(c2));
    }

// org.jfree.chart.entity.junit.StandardEntityCollectionTests::testSerialization
    public void testSerialization() {
        PieSectionEntity e1 = new PieSectionEntity(new Rectangle2D.Double(1.0, 
                2.0, 3.0, 4.0), new DefaultPieDataset(), 0, 1, "Key", 
                "ToolTip", "URL");
        StandardEntityCollection c1 = new StandardEntityCollection();
        c1.add(e1);
        StandardEntityCollection c2 = null;
        try {
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            ObjectOutput out = new ObjectOutputStream(buffer);
            out.writeObject(c1);
            out.close();

            ObjectInput in = new ObjectInputStream(new ByteArrayInputStream(
                    buffer.toByteArray()));
            c2 = (StandardEntityCollection) in.readObject();
            in.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        assertEquals(c1, c2);
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

// org.jfree.chart.labels.junit.StandardCategoryItemLabelGeneratorTests::testGenerateLabel
    public void testGenerateLabel() {
        StandardCategoryItemLabelGenerator g 
            = new StandardCategoryItemLabelGenerator("{2}", 
                    new DecimalFormat("0.000"));
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        dataset.addValue(1.0, "R0", "C0");
        dataset.addValue(2.0, "R0", "C1");
        dataset.addValue(3.0, "R1", "C0");
        dataset.addValue(null, "R1", "C1");
        String s = g.generateLabel(dataset, 0, 0);
        assertEquals("1.000", s);
        
        
        s = g.generateLabel(dataset, 1, 1);
        assertEquals("-", s);
    }

// org.jfree.chart.labels.junit.StandardCategoryItemLabelGeneratorTests::testEquals
    public void testEquals() {
        
        StandardCategoryItemLabelGenerator g1 
            = new StandardCategoryItemLabelGenerator();
        StandardCategoryItemLabelGenerator g2 
            = new StandardCategoryItemLabelGenerator();
        assertTrue(g1.equals(g2));
        assertTrue(g2.equals(g1));
        
        g1 = new StandardCategoryItemLabelGenerator("{0}", 
                new DecimalFormat("0.000"));
        assertFalse(g1.equals(g2));
        g2 = new StandardCategoryItemLabelGenerator("{0}", 
                new DecimalFormat("0.000"));
        assertTrue(g1.equals(g2));

        g1 = new StandardCategoryItemLabelGenerator("{1}", 
                new DecimalFormat("0.000"));
        assertFalse(g1.equals(g2));
        g2 = new StandardCategoryItemLabelGenerator("{1}", 
                new DecimalFormat("0.000"));
        assertTrue(g1.equals(g2));

        g1 = new StandardCategoryItemLabelGenerator("{2}", 
                new SimpleDateFormat("d-MMM"));
        assertFalse(g1.equals(g2));
        g2 = new StandardCategoryItemLabelGenerator("{2}", 
                new SimpleDateFormat("d-MMM"));
        assertTrue(g1.equals(g2));
        
    }

// org.jfree.chart.labels.junit.StandardCategoryItemLabelGeneratorTests::testCloning
    public void testCloning() {
        StandardCategoryItemLabelGenerator g1 
            = new StandardCategoryItemLabelGenerator();
        StandardCategoryItemLabelGenerator g2 = null;
        try {
            g2 = (StandardCategoryItemLabelGenerator) g1.clone();
        }
        catch (CloneNotSupportedException e) {
            System.err.println("Failed to clone.");
        }
        assertTrue(g1 != g2);
        assertTrue(g1.getClass() == g2.getClass());
        assertTrue(g1.equals(g2));
    }

// org.jfree.chart.labels.junit.StandardCategoryItemLabelGeneratorTests::testSerialization
    public void testSerialization() {

        StandardCategoryItemLabelGenerator g1
            = new StandardCategoryItemLabelGenerator("{2}", 
                    DateFormat.getInstance());
        StandardCategoryItemLabelGenerator g2 = null;

        try {
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            ObjectOutput out = new ObjectOutputStream(buffer);
            out.writeObject(g1);
            out.close();

            ObjectInput in = new ObjectInputStream(
                    new ByteArrayInputStream(buffer.toByteArray()));
            g2 = (StandardCategoryItemLabelGenerator) in.readObject();
            in.close();
        }
        catch (Exception e) {
            System.out.println(e.toString());
        }
        assertEquals(g1, g2);

    }

// org.jfree.chart.labels.junit.StandardCategoryItemLabelGeneratorTests::testEquals1481087
    public void testEquals1481087() {
        StandardCategoryItemLabelGenerator g1 
            = new StandardCategoryItemLabelGenerator("{0}", 
                    new DecimalFormat("0.00"));
        StandardCategoryToolTipGenerator g2 
            = new StandardCategoryToolTipGenerator("{0}", 
                    new DecimalFormat("0.00"));
        assertFalse(g1.equals(g2));
    }

// org.jfree.chart.labels.junit.StandardCategorySeriesLabelGeneratorTests::testGenerateLabel
    public void testGenerateLabel() {
        StandardCategorySeriesLabelGenerator g 
            = new StandardCategorySeriesLabelGenerator("{0}");
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        dataset.addValue(1.0, "R0", "C0");
        dataset.addValue(2.0, "R0", "C1");
        dataset.addValue(3.0, "R1", "C0");
        dataset.addValue(null, "R1", "C1");
        String s = g.generateLabel(dataset, 0);
        assertEquals("R0", s);
    }

// org.jfree.chart.labels.junit.StandardCategorySeriesLabelGeneratorTests::testEquals
    public void testEquals() {
        StandardCategorySeriesLabelGenerator g1 
            = new StandardCategorySeriesLabelGenerator();
        StandardCategorySeriesLabelGenerator g2 
            = new StandardCategorySeriesLabelGenerator();
        assertTrue(g1.equals(g2));
        assertTrue(g2.equals(g1));
        
        g1 = new StandardCategorySeriesLabelGenerator("{1}");
        assertFalse(g1.equals(g2));
        g2 = new StandardCategorySeriesLabelGenerator("{1}");
        assertTrue(g1.equals(g2));        
    }

// org.jfree.chart.labels.junit.StandardCategorySeriesLabelGeneratorTests::testCloning
    public void testCloning() {
        StandardCategorySeriesLabelGenerator g1 
            = new StandardCategorySeriesLabelGenerator("{1}");
        StandardCategorySeriesLabelGenerator g2 = null;
        try {
            g2 = (StandardCategorySeriesLabelGenerator) g1.clone();
        }
        catch (CloneNotSupportedException e) {
            System.err.println("Failed to clone.");
        }
        assertTrue(g1 != g2);
        assertTrue(g1.getClass() == g2.getClass());
        assertTrue(g1.equals(g2));
    }

// org.jfree.chart.labels.junit.StandardCategorySeriesLabelGeneratorTests::testSerialization
    public void testSerialization() {

        StandardCategorySeriesLabelGenerator g1
            = new StandardCategorySeriesLabelGenerator("{2}");
        StandardCategorySeriesLabelGenerator g2 = null;

        try {
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            ObjectOutput out = new ObjectOutputStream(buffer);
            out.writeObject(g1);
            out.close();

            ObjectInput in = new ObjectInputStream(
                    new ByteArrayInputStream(buffer.toByteArray()));
            g2 = (StandardCategorySeriesLabelGenerator) in.readObject();
            in.close();
        }
        catch (Exception e) {
            System.out.println(e.toString());
        }
        assertEquals(g1, g2);

    }

// org.jfree.chart.plot.junit.CategoryPlotTests::testConstructor
    public void testConstructor() {}

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

// org.jfree.chart.plot.junit.CombinedDomainCategoryPlotTests::testRemoveSubplot
    public void testRemoveSubplot() {
        CombinedDomainCategoryPlot plot = new CombinedDomainCategoryPlot();
        CategoryPlot plot1 = new CategoryPlot();
        CategoryPlot plot2 = new CategoryPlot();
        plot.add(plot1);
        plot.add(plot2);
        
        plot.remove(plot2);
        List plots = plot.getSubplots();
        assertTrue(plots.get(0) == plot1);
        assertEquals(1, plots.size());
    }

// org.jfree.chart.plot.junit.CombinedDomainCategoryPlotTests::testEquals
    public void testEquals() {
        CombinedDomainCategoryPlot plot1 = createPlot();
        CombinedDomainCategoryPlot plot2 = createPlot();
        assertTrue(plot1.equals(plot2));    
    }

// org.jfree.chart.plot.junit.CombinedDomainCategoryPlotTests::testCloning
    public void testCloning() {
        CombinedDomainCategoryPlot plot1 = createPlot();        
        CombinedDomainCategoryPlot plot2 = null;
        try {
            plot2 = (CombinedDomainCategoryPlot) plot1.clone();
        }
        catch (CloneNotSupportedException e) {
            System.err.println("Failed to clone.");
        }
        assertTrue(plot1 != plot2);
        assertTrue(plot1.getClass() == plot2.getClass());
        assertTrue(plot1.equals(plot2));
    }

// org.jfree.chart.plot.junit.CombinedDomainCategoryPlotTests::testSerialization
    public void testSerialization() {
        CombinedDomainCategoryPlot plot1 = createPlot();
        CombinedDomainCategoryPlot plot2 = null;
        try {
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            ObjectOutput out = new ObjectOutputStream(buffer);
            out.writeObject(plot1);
            out.close();
            ObjectInput in = new ObjectInputStream(
                new ByteArrayInputStream(buffer.toByteArray())
            );
            plot2 = (CombinedDomainCategoryPlot) in.readObject();
            in.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        assertEquals(plot1, plot2);
    }

// org.jfree.chart.plot.junit.CombinedRangeCategoryPlotTests::testEquals
    public void testEquals() {
        CombinedRangeCategoryPlot plot1 = createPlot();
        CombinedRangeCategoryPlot plot2 = createPlot();
        assertTrue(plot1.equals(plot2));    
    }

// org.jfree.chart.plot.junit.CombinedRangeCategoryPlotTests::testCloning
    public void testCloning() {
        CombinedRangeCategoryPlot plot1 = createPlot();        
        CombinedRangeCategoryPlot plot2 = null;
        try {
            plot2 = (CombinedRangeCategoryPlot) plot1.clone();
        }
        catch (CloneNotSupportedException e) {
            System.err.println("Failed to clone.");
        }
        assertTrue(plot1 != plot2);
        assertTrue(plot1.getClass() == plot2.getClass());
        assertTrue(plot1.equals(plot2));
    }

// org.jfree.chart.plot.junit.CombinedRangeCategoryPlotTests::testSerialization
    public void testSerialization() {
        CombinedRangeCategoryPlot plot1 = createPlot();
        CombinedRangeCategoryPlot plot2 = null;
        try {
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            ObjectOutput out = new ObjectOutputStream(buffer);
            out.writeObject(plot1);
            out.close();
            ObjectInput in = new ObjectInputStream(
                new ByteArrayInputStream(buffer.toByteArray())
            );
            plot2 = (CombinedRangeCategoryPlot) in.readObject();
            in.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        assertEquals(plot1, plot2);

    }

// org.jfree.chart.plot.junit.CombinedRangeCategoryPlotTests::testRemoveSubplot
    public void testRemoveSubplot() {
        CombinedRangeCategoryPlot plot = new CombinedRangeCategoryPlot();
        CategoryPlot plot1 = new CategoryPlot();
        CategoryPlot plot2 = new CategoryPlot();
        CategoryPlot plot3 = new CategoryPlot();
        plot.add(plot1);
        plot.add(plot2);
        plot.add(plot3);
        plot.remove(plot2);
        List plots = plot.getSubplots();
        assertEquals(2, plots.size());
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

// org.jfree.chart.plot.junit.SpiderWebPlotTests::testEquals
    public void testEquals() {
        SpiderWebPlot p1 = new SpiderWebPlot(new DefaultCategoryDataset());
        SpiderWebPlot p2 = new SpiderWebPlot(new DefaultCategoryDataset());
        assertTrue(p1.equals(p2));
        assertTrue(p2.equals(p1));
        
        
        p1.setDataExtractOrder(TableOrder.BY_COLUMN);
        assertFalse(p1.equals(p2));
        p2.setDataExtractOrder(TableOrder.BY_COLUMN);
        assertTrue(p1.equals(p2));
        
        
        p1.setHeadPercent(0.321);
        assertFalse(p1.equals(p2));
        p2.setHeadPercent(0.321);
        assertTrue(p1.equals(p2));
        
        
        p1.setInteriorGap(0.123);
        assertFalse(p1.equals(p2));
        p2.setInteriorGap(0.123);
        assertTrue(p1.equals(p2));
        
        
        p1.setStartAngle(0.456);
        assertFalse(p1.equals(p2));
        p2.setStartAngle(0.456);
        assertTrue(p1.equals(p2));
        
        
        p1.setDirection(Rotation.ANTICLOCKWISE);
        assertFalse(p1.equals(p2));
        p2.setDirection(Rotation.ANTICLOCKWISE);
        assertTrue(p1.equals(p2));
        
        
        p1.setMaxValue(123.4);
        assertFalse(p1.equals(p2));
        p2.setMaxValue(123.4);
        assertTrue(p1.equals(p2));
        
        
        p1.setLegendItemShape(new Rectangle(1, 2, 3, 4));
        assertFalse(p1.equals(p2));
        p2.setLegendItemShape(new Rectangle(1, 2, 3, 4));
        assertTrue(p1.equals(p2));

        
        p1.setSeriesPaint(new GradientPaint(1.0f, 2.0f, Color.red, 
                3.0f, 4.0f, Color.white));
        assertFalse(p1.equals(p2));
        p2.setSeriesPaint(new GradientPaint(1.0f, 2.0f, Color.red, 
                3.0f, 4.0f, Color.white));
        assertTrue(p1.equals(p2));

        
        p1.setSeriesPaint(1, new GradientPaint(1.0f, 2.0f, Color.yellow, 
                3.0f, 4.0f, Color.white));
        assertFalse(p1.equals(p2));
        p2.setSeriesPaint(1, new GradientPaint(1.0f, 2.0f, Color.yellow, 
                3.0f, 4.0f, Color.white));
        assertTrue(p1.equals(p2));
        
        
        p1.setBaseSeriesPaint(new GradientPaint(1.0f, 2.0f, Color.red, 
                3.0f, 4.0f, Color.black));
        assertFalse(p1.equals(p2));
        p2.setBaseSeriesPaint(new GradientPaint(1.0f, 2.0f, Color.red, 
                3.0f, 4.0f, Color.black));
        assertTrue(p1.equals(p2));
        
        
        p1.setSeriesOutlinePaint(new GradientPaint(1.0f, 2.0f, Color.blue, 
                3.0f, 4.0f, Color.black));
        assertFalse(p1.equals(p2));
        p2.setSeriesOutlinePaint(new GradientPaint(1.0f, 2.0f, Color.blue, 
                3.0f, 4.0f, Color.black));
        assertTrue(p1.equals(p2));

        
        p1.setSeriesOutlinePaint(1, new GradientPaint(1.0f, 2.0f, Color.blue, 
                3.0f, 4.0f, Color.green));
        assertFalse(p1.equals(p2));
        p2.setSeriesOutlinePaint(1, new GradientPaint(1.0f, 2.0f, Color.blue, 
                3.0f, 4.0f, Color.green));
        assertTrue(p1.equals(p2));

        
        p1.setBaseSeriesOutlinePaint(new GradientPaint(1.0f, 2.0f, Color.cyan, 
                3.0f, 4.0f, Color.green));
        assertFalse(p1.equals(p2));
        p2.setBaseSeriesOutlinePaint(new GradientPaint(1.0f, 2.0f, Color.cyan, 
                3.0f, 4.0f, Color.green));
        assertTrue(p1.equals(p2));
        
        
        BasicStroke s = new BasicStroke(1.23f);
        p1.setSeriesOutlineStroke(s);
        assertFalse(p1.equals(p2));
        p2.setSeriesOutlineStroke(s);
        assertTrue(p1.equals(p2));
        
        
        p1.setSeriesOutlineStroke(1, s);
        assertFalse(p1.equals(p2));
        p2.setSeriesOutlineStroke(1, s);
        assertTrue(p1.equals(p2));
        
        
        p1.setBaseSeriesOutlineStroke(s);
        assertFalse(p1.equals(p2));
        p2.setBaseSeriesOutlineStroke(s);
        assertTrue(p1.equals(p2));

        
        p1.setWebFilled(false);
        assertFalse(p1.equals(p2));
        p2.setWebFilled(false);
        assertTrue(p1.equals(p2));
        
        
        p1.setAxisLabelGap(0.11);
        assertFalse(p1.equals(p2));
        p2.setAxisLabelGap(0.11);
        assertTrue(p1.equals(p2));

        
        p1.setLabelFont(new Font("Serif", Font.PLAIN, 9));
        assertFalse(p1.equals(p2));
        p2.setLabelFont(new Font("Serif", Font.PLAIN, 9));
        assertTrue(p1.equals(p2));
        
        
        p1.setLabelPaint(new GradientPaint(1.0f, 2.0f, Color.red, 
                3.0f, 4.0f, Color.blue));
        assertFalse(p1.equals(p2));
        p2.setLabelPaint(new GradientPaint(1.0f, 2.0f, Color.red, 
                3.0f, 4.0f, Color.blue));
        assertTrue(p1.equals(p2));

        
        p1.setLabelGenerator(new StandardCategoryItemLabelGenerator("XYZ: {0}",
                new DecimalFormat("0.000")));
        assertFalse(p1.equals(p2));
        p2.setLabelGenerator(new StandardCategoryItemLabelGenerator("XYZ: {0}",
                new DecimalFormat("0.000")));
        assertTrue(p1.equals(p2));
        
        
        p1.setToolTipGenerator(new StandardCategoryToolTipGenerator());
        assertFalse(p1.equals(p2));
        p2.setToolTipGenerator(new StandardCategoryToolTipGenerator());
        assertTrue(p1.equals(p2));

        
        p1.setURLGenerator(new StandardCategoryURLGenerator());
        assertFalse(p1.equals(p2));
        p2.setURLGenerator(new StandardCategoryURLGenerator());
        assertTrue(p1.equals(p2));
        
        
        p1.setAxisLinePaint(Color.red);
        assertFalse(p1.equals(p2));
        p2.setAxisLinePaint(Color.red);
        assertTrue(p1.equals(p2));
        
        
        p1.setAxisLineStroke(new BasicStroke(1.1f));
        assertFalse(p1.equals(p2));
        p2.setAxisLineStroke(new BasicStroke(1.1f));
        assertTrue(p1.equals(p2));
    }

// org.jfree.chart.plot.junit.SpiderWebPlotTests::testCloning
    public void testCloning() {
        SpiderWebPlot p1 = new SpiderWebPlot(new DefaultCategoryDataset());
        Rectangle2D legendShape = new Rectangle2D.Double(1.0, 2.0, 3.0, 4.0);
        p1.setLegendItemShape(legendShape);
        SpiderWebPlot p2 = null;
        try {
            p2 = (SpiderWebPlot) p1.clone();
        }
        catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        assertTrue(p1 != p2);
        assertTrue(p1.getClass() == p2.getClass());
        assertTrue(p1.equals(p2));
        
        
        legendShape.setRect(4.0, 3.0, 2.0, 1.0);
        assertFalse(p1.equals(p2));
        p2.setLegendItemShape(legendShape);
        assertTrue(p1.equals(p2));
        
        
        p1.setSeriesPaint(1, Color.black);
        assertFalse(p1.equals(p2));
        p2.setSeriesPaint(1, Color.black);
        assertTrue(p1.equals(p2));
        
        
        p1.setSeriesOutlinePaint(0, Color.red);
        assertFalse(p1.equals(p2));
        p2.setSeriesOutlinePaint(0, Color.red);
        assertTrue(p1.equals(p2));
        
        
        p1.setSeriesOutlineStroke(0, new BasicStroke(1.1f));
        assertFalse(p1.equals(p2));
        p2.setSeriesOutlineStroke(0, new BasicStroke(1.1f));
        assertTrue(p1.equals(p2));
        
    }

// org.jfree.chart.plot.junit.SpiderWebPlotTests::testSerialization
    public void testSerialization() {

        SpiderWebPlot p1 = new SpiderWebPlot(new DefaultCategoryDataset());
        SpiderWebPlot p2 = null;

        try {
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            ObjectOutput out = new ObjectOutputStream(buffer);
            out.writeObject(p1);
            out.close();

            ObjectInput in = new ObjectInputStream(
                    new ByteArrayInputStream(buffer.toByteArray()));
            p2 = (SpiderWebPlot) in.readObject();
            in.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        assertEquals(p1, p2);

    }

// org.jfree.chart.plot.junit.SpiderWebPlotTests::testDrawWithNullInfo
    public void testDrawWithNullInfo() {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        dataset.addValue(35.0, "S1", "C1");
        dataset.addValue(45.0, "S1", "C2");
        dataset.addValue(55.0, "S1", "C3");
        dataset.addValue(15.0, "S1", "C4");
        dataset.addValue(25.0, "S1", "C5");
        SpiderWebPlot plot = new SpiderWebPlot(dataset);
        JFreeChart chart = new JFreeChart(plot);
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

// org.jfree.chart.renderer.category.junit.AreaRendererTests::testEquals
    public void testEquals() {
        AreaRenderer r1 = new AreaRenderer();
        AreaRenderer r2 = new AreaRenderer();
        assertEquals(r1, r2);
        
        r1.setEndType(AreaRendererEndType.LEVEL);
        assertFalse(r1.equals(r2));
        r2.setEndType(AreaRendererEndType.LEVEL);
        assertTrue(r1.equals(r2));
    }

// org.jfree.chart.renderer.category.junit.AreaRendererTests::testHashcode
    public void testHashcode() {
        AreaRenderer r1 = new AreaRenderer();
        AreaRenderer r2 = new AreaRenderer();
        assertTrue(r1.equals(r2));
        int h1 = r1.hashCode();
        int h2 = r2.hashCode();
        assertEquals(h1, h2);
    }

// org.jfree.chart.renderer.category.junit.AreaRendererTests::testCloning
    public void testCloning() {
        AreaRenderer r1 = new AreaRenderer();
        AreaRenderer r2 = null;
        try {
            r2 = (AreaRenderer) r1.clone();
        }
        catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        assertTrue(r1 != r2);
        assertTrue(r1.getClass() == r2.getClass());
        assertTrue(r1.equals(r2));
    }

// org.jfree.chart.renderer.category.junit.AreaRendererTests::testSerialization
    public void testSerialization() {
        AreaRenderer r1 = new AreaRenderer();
        AreaRenderer r2 = null;
        try {
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            ObjectOutput out = new ObjectOutputStream(buffer);
            out.writeObject(r1);
            out.close();

            ObjectInput in = new ObjectInputStream(
                    new ByteArrayInputStream(buffer.toByteArray()));
            r2 = (AreaRenderer) in.readObject();
            in.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        assertEquals(r1, r2);
    }

// org.jfree.chart.renderer.category.junit.AreaRendererTests::testGetLegendItemSeriesIndex
    public void testGetLegendItemSeriesIndex() {
        DefaultCategoryDataset dataset0 = new DefaultCategoryDataset();
        dataset0.addValue(21.0, "R1", "C1");
        dataset0.addValue(22.0, "R2", "C1");        
        DefaultCategoryDataset dataset1 = new DefaultCategoryDataset();
        dataset1.addValue(23.0, "R3", "C1");
        dataset1.addValue(24.0, "R4", "C1");        
        dataset1.addValue(25.0, "R5", "C1");        
        AreaRenderer r = new AreaRenderer();
        CategoryPlot plot = new CategoryPlot(dataset0, new CategoryAxis("x"),
                new NumberAxis("y"), r);
        plot.setDataset(1, dataset1);
         new JFreeChart(plot);
        LegendItem li = r.getLegendItem(1, 2);
        assertEquals("R5", li.getLabel());
        assertEquals(1, li.getDatasetIndex());
        assertEquals(2, li.getSeriesIndex());
    }

// org.jfree.chart.renderer.category.junit.BarRendererTests::testEquals
    public void testEquals() {
        BarRenderer r1 = new BarRenderer();
        BarRenderer r2 = new BarRenderer();
        assertTrue(r1.equals(r2));
        assertTrue(r2.equals(r1));
        
        
        r1.setBase(0.123);
        assertFalse(r1.equals(r2));
        r2.setBase(0.123);
        assertTrue(r1.equals(r2));
        
        
        r1.setItemMargin(0.22);
        assertFalse(r1.equals(r2));
        r2.setItemMargin(0.22);
        assertTrue(r1.equals(r2));
        
        
        r1.setDrawBarOutline(!r1.isDrawBarOutline());
        assertFalse(r1.equals(r2));
        r2.setDrawBarOutline(!r2.isDrawBarOutline());
        assertTrue(r1.equals(r2));
        
        
        r1.setMaximumBarWidth(0.11);
        assertFalse(r1.equals(r2));
        r2.setMaximumBarWidth(0.11);
        assertTrue(r1.equals(r2));
        
        
        r1.setMinimumBarLength(0.04);
        assertFalse(r1.equals(r2));
        r2.setMinimumBarLength(0.04);
        assertTrue(r1.equals(r2));
        
        
        r1.setGradientPaintTransformer(new StandardGradientPaintTransformer(
                GradientPaintTransformType.CENTER_VERTICAL));
        assertFalse(r1.equals(r2));
        r2.setGradientPaintTransformer(new StandardGradientPaintTransformer(
                GradientPaintTransformType.CENTER_VERTICAL));
        assertTrue(r1.equals(r2));
        
        
        r1.setPositiveItemLabelPositionFallback(new ItemLabelPosition(
                ItemLabelAnchor.INSIDE1, TextAnchor.CENTER));
        assertFalse(r1.equals(r2));
        r2.setPositiveItemLabelPositionFallback(new ItemLabelPosition(
                ItemLabelAnchor.INSIDE1, TextAnchor.CENTER));
        assertTrue(r1.equals(r2));

        
        r1.setNegativeItemLabelPositionFallback(new ItemLabelPosition(
                ItemLabelAnchor.INSIDE1, TextAnchor.CENTER));
        assertFalse(r1.equals(r2));
        r2.setNegativeItemLabelPositionFallback(new ItemLabelPosition(
                ItemLabelAnchor.INSIDE1, TextAnchor.CENTER));
        assertTrue(r1.equals(r2));

    }

// org.jfree.chart.renderer.category.junit.BarRendererTests::testHashcode
    public void testHashcode() {
        BarRenderer r1 = new BarRenderer();
        BarRenderer r2 = new BarRenderer();
        assertTrue(r1.equals(r2));
        int h1 = r1.hashCode();
        int h2 = r2.hashCode();
        assertEquals(h1, h2);
    }

// org.jfree.chart.renderer.category.junit.BarRendererTests::testCloning
    public void testCloning() {
        BarRenderer r1 = new BarRenderer();
        r1.setBaseItemLabelGenerator(new StandardCategoryItemLabelGenerator());
        BarRenderer r2 = null;
        try {
            r2 = (BarRenderer) r1.clone();
        }
        catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        assertTrue(r1 != r2);
        assertTrue(r1.getClass() == r2.getClass());
        assertTrue(r1.equals(r2));
    }

// org.jfree.chart.renderer.category.junit.BarRendererTests::testSerialization
    public void testSerialization() {

        BarRenderer r1 = new BarRenderer();
        BarRenderer r2 = null;

        try {
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            ObjectOutput out = new ObjectOutputStream(buffer);
            out.writeObject(r1);
            out.close();

            ObjectInput in = new ObjectInputStream(new ByteArrayInputStream(
                    buffer.toByteArray()));
            r2 = (BarRenderer) in.readObject();
            in.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        assertEquals(r1, r2);

    }

// org.jfree.chart.renderer.category.junit.BarRendererTests::testEventNotification
    public void testEventNotification() {
        
        RendererChangeDetector detector = new RendererChangeDetector();
        BarRenderer r1 = new BarRenderer();
        r1.addChangeListener(detector);
        
        detector.setNotified(false);
        r1.setBasePaint(Color.red);
        assertTrue(detector.getNotified());

    }

// org.jfree.chart.renderer.category.junit.BarRendererTests::testGetLegendItem
    public void testGetLegendItem() {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        dataset.addValue(21.0, "R1", "C1");
        BarRenderer r = new BarRenderer();
        CategoryPlot plot = new CategoryPlot(dataset, new CategoryAxis("x"),
                new NumberAxis("y"), r);
         new JFreeChart(plot);
        LegendItem li = r.getLegendItem(0, 0);
        assertNotNull(li);
        r.setSeriesVisibleInLegend(0, Boolean.FALSE);
        li = r.getLegendItem(0, 0);
        assertNull(li);
    }

// org.jfree.chart.renderer.category.junit.BarRendererTests::testGetLegendItemSeriesIndex
    public void testGetLegendItemSeriesIndex() {
        DefaultCategoryDataset dataset0 = new DefaultCategoryDataset();
        dataset0.addValue(21.0, "R1", "C1");
        dataset0.addValue(22.0, "R2", "C1");        
        DefaultCategoryDataset dataset1 = new DefaultCategoryDataset();
        dataset1.addValue(23.0, "R3", "C1");
        dataset1.addValue(24.0, "R4", "C1");        
        dataset1.addValue(25.0, "R5", "C1");        
        BarRenderer r = new BarRenderer();
        CategoryPlot plot = new CategoryPlot(dataset0, new CategoryAxis("x"),
                new NumberAxis("y"), r);
        plot.setDataset(1, dataset1);
         new JFreeChart(plot);
        LegendItem li = r.getLegendItem(1, 2);
        assertEquals("R5", li.getLabel());
        assertEquals(1, li.getDatasetIndex());
        assertEquals(2, li.getSeriesIndex());
    }

// org.jfree.chart.renderer.category.junit.BoxAndWhiskerRendererTests::testEquals
    public void testEquals() {
        BoxAndWhiskerRenderer r1 = new BoxAndWhiskerRenderer();
        BoxAndWhiskerRenderer r2 = new BoxAndWhiskerRenderer();
        assertEquals(r1, r2);
        
        r1.setArtifactPaint(new GradientPaint(1.0f, 2.0f, Color.yellow, 
                3.0f, 4.0f, Color.blue));
        assertFalse(r1.equals(r2));
        r2.setArtifactPaint(new GradientPaint(1.0f, 2.0f, Color.yellow, 
                3.0f, 4.0f, Color.blue));
        assertEquals(r1, r2);
        
        r1.setFillBox(!r1.getFillBox());
        assertFalse(r1.equals(r2));
        r2.setFillBox(!r2.getFillBox());
        assertEquals(r1, r2);
        
        r1.setItemMargin(0.11);
        assertFalse(r1.equals(r2));
        r2.setItemMargin(0.11);
        assertEquals(r1, r2);
        
    }

// org.jfree.chart.renderer.category.junit.BoxAndWhiskerRendererTests::testHashcode
    public void testHashcode() {
        BoxAndWhiskerRenderer r1 = new BoxAndWhiskerRenderer();
        BoxAndWhiskerRenderer r2 = new BoxAndWhiskerRenderer();
        assertTrue(r1.equals(r2));
        int h1 = r1.hashCode();
        int h2 = r2.hashCode();
        assertEquals(h1, h2);
    }

// org.jfree.chart.renderer.category.junit.BoxAndWhiskerRendererTests::testCloning
    public void testCloning() {
        BoxAndWhiskerRenderer r1 = new BoxAndWhiskerRenderer();
        BoxAndWhiskerRenderer r2 = null;
        try {
            r2 = (BoxAndWhiskerRenderer) r1.clone();
        }
        catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        assertTrue(r1 != r2);
        assertTrue(r1.getClass() == r2.getClass());
        assertTrue(r1.equals(r2));
    }

// org.jfree.chart.renderer.category.junit.BoxAndWhiskerRendererTests::testSerialization
    public void testSerialization() {

        BoxAndWhiskerRenderer r1 = new BoxAndWhiskerRenderer();
        BoxAndWhiskerRenderer r2 = null;

        try {
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            ObjectOutput out = new ObjectOutputStream(buffer);
            out.writeObject(r1);
            out.close();

            ObjectInput in = new ObjectInputStream(
                    new ByteArrayInputStream(buffer.toByteArray()));
            r2 = (BoxAndWhiskerRenderer) in.readObject();
            in.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        assertEquals(r1, r2);

    }

// org.jfree.chart.renderer.category.junit.BoxAndWhiskerRendererTests::testDrawWithNullInfo
    public void testDrawWithNullInfo() {
        boolean success = false;
        try {
            DefaultBoxAndWhiskerCategoryDataset dataset 
                = new DefaultBoxAndWhiskerCategoryDataset();
            dataset.add(new BoxAndWhiskerItem(new Double(1.0), new Double(2.0),
                    new Double(0.0), new Double(4.0), new Double(0.5), 
                    new Double(4.5), new Double(-0.5), new Double(5.5), 
                    null), "S1", "C1");
            CategoryPlot plot = new CategoryPlot(dataset, 
                    new CategoryAxis("Category"), new NumberAxis("Value"), 
                    new BoxAndWhiskerRenderer());
            JFreeChart chart = new JFreeChart(plot);
             chart.createBufferedImage(300, 200, 
                    null);
            success = true;
        }
        catch (NullPointerException e) {
            success = false;
        }
        assertTrue(success);
    }

// org.jfree.chart.renderer.category.junit.BoxAndWhiskerRendererTests::testBug1572478Vertical
    public void testBug1572478Vertical() {
        DefaultBoxAndWhiskerCategoryDataset dataset 
                = new DefaultBoxAndWhiskerCategoryDataset() {
                
            public Number getQ1Value(int row, int column) {
                return null;
            }

            public Number getQ1Value(Comparable rowKey, Comparable columnKey) {
                return null;
            }
        };
        List values = new ArrayList();
        values.add(new Double(1.0));
        values.add(new Double(10.0));
        values.add(new Double(100.0));
        dataset.add(values, "row", "column");
        CategoryPlot plot = new CategoryPlot(dataset, new CategoryAxis("x"),
                new NumberAxis("y"), new BoxAndWhiskerRenderer());
        JFreeChart chart = new JFreeChart(plot);
        boolean success = false;

        try {
            BufferedImage image = new BufferedImage(200 , 100, 
                    BufferedImage.TYPE_INT_RGB);
            Graphics2D g2 = image.createGraphics();
            chart.draw(g2, new Rectangle2D.Double(0, 0, 200, 100), null, 
                    new ChartRenderingInfo());
            g2.dispose();
            success = true;
        }
        catch (Exception e) {
            success = false;
        }

        assertTrue(success);

    }

// org.jfree.chart.renderer.category.junit.BoxAndWhiskerRendererTests::testBug1572478Horizontal
    public void testBug1572478Horizontal() {
        DefaultBoxAndWhiskerCategoryDataset dataset 
                = new DefaultBoxAndWhiskerCategoryDataset() {
                
            public Number getQ1Value(int row, int column) {
                return null;
            }

            public Number getQ1Value(Comparable rowKey, Comparable columnKey) {
                return null;
            }
        };
        List values = new ArrayList();
        values.add(new Double(1.0));
        values.add(new Double(10.0));
        values.add(new Double(100.0));
        dataset.add(values, "row", "column");
        CategoryPlot plot = new CategoryPlot(dataset, new CategoryAxis("x"),
                new NumberAxis("y"), new BoxAndWhiskerRenderer());
        plot.setOrientation(PlotOrientation.HORIZONTAL);
        JFreeChart chart = new JFreeChart(plot);
        boolean success = false;

        try {
            BufferedImage image = new BufferedImage(200 , 100, 
                    BufferedImage.TYPE_INT_RGB);
            Graphics2D g2 = image.createGraphics();
            chart.draw(g2, new Rectangle2D.Double(0, 0, 200, 100), null, 
                    new ChartRenderingInfo());
            g2.dispose();
            success = true;
        }
        catch (Exception e) {
            success = false;
        }

        assertTrue(success);

    }

// org.jfree.chart.renderer.category.junit.BoxAndWhiskerRendererTests::testGetLegendItem
    public void testGetLegendItem() {
        DefaultBoxAndWhiskerCategoryDataset dataset 
                = new DefaultBoxAndWhiskerCategoryDataset();
        List values = new ArrayList();
        values.add(new Double(1.10));
        values.add(new Double(1.45));
        values.add(new Double(1.33));
        values.add(new Double(1.23));
        dataset.add(values, "R1", "C1");
        BoxAndWhiskerRenderer r = new BoxAndWhiskerRenderer();
        CategoryPlot plot = new CategoryPlot(dataset, new CategoryAxis("x"),
                new NumberAxis("y"), r);
         new JFreeChart(plot);
        LegendItem li = r.getLegendItem(0, 0);
        assertNotNull(li);
        r.setSeriesVisibleInLegend(0, Boolean.FALSE);
        li = r.getLegendItem(0, 0);
        assertNull(li);
    }

// org.jfree.chart.renderer.category.junit.BoxAndWhiskerRendererTests::testGetLegendItemSeriesIndex
    public void testGetLegendItemSeriesIndex() {
        DefaultCategoryDataset dataset0 = new DefaultCategoryDataset();
        dataset0.addValue(21.0, "R1", "C1");
        dataset0.addValue(22.0, "R2", "C1");        
        DefaultCategoryDataset dataset1 = new DefaultCategoryDataset();
        dataset1.addValue(23.0, "R3", "C1");
        dataset1.addValue(24.0, "R4", "C1");        
        dataset1.addValue(25.0, "R5", "C1");        
        BoxAndWhiskerRenderer r = new BoxAndWhiskerRenderer();
        CategoryPlot plot = new CategoryPlot(dataset0, new CategoryAxis("x"),
                new NumberAxis("y"), r);
        plot.setDataset(1, dataset1);
         new JFreeChart(plot);
        LegendItem li = r.getLegendItem(1, 2);
        assertEquals("R5", li.getLabel());
        assertEquals(1, li.getDatasetIndex());
        assertEquals(2, li.getSeriesIndex());
    }

// org.jfree.chart.renderer.category.junit.BoxAndWhiskerRendererTests::testDrawWithNullMean
    public void testDrawWithNullMean() {
        boolean success = false;
        try {
            DefaultBoxAndWhiskerCategoryDataset dataset 
                    = new DefaultBoxAndWhiskerCategoryDataset();
            dataset.add(new BoxAndWhiskerItem(null, new Double(2.0),
                    new Double(0.0), new Double(4.0), new Double(0.5), 
                    new Double(4.5), new Double(-0.5), new Double(5.5), 
                    null), "S1", "C1");
            CategoryPlot plot = new CategoryPlot(dataset, 
                    new CategoryAxis("Category"), new NumberAxis("Value"), 
                    new BoxAndWhiskerRenderer());
            ChartRenderingInfo info = new ChartRenderingInfo();
            JFreeChart chart = new JFreeChart(plot);
             chart.createBufferedImage(300, 200, 
                    info);
            success = true;
        }
        catch (Exception e) {
            success = false;
        }
        assertTrue(success);
    }

// org.jfree.chart.renderer.category.junit.BoxAndWhiskerRendererTests::testDrawWithNullMedian
    public void testDrawWithNullMedian() {
        boolean success = false;
        try {
            DefaultBoxAndWhiskerCategoryDataset dataset 
                    = new DefaultBoxAndWhiskerCategoryDataset();
            dataset.add(new BoxAndWhiskerItem(new Double(1.0), null,
                    new Double(0.0), new Double(4.0), new Double(0.5), 
                    new Double(4.5), new Double(-0.5), new Double(5.5), 
                    null), "S1", "C1");
            CategoryPlot plot = new CategoryPlot(dataset, 
                    new CategoryAxis("Category"), new NumberAxis("Value"), 
                    new BoxAndWhiskerRenderer());
            ChartRenderingInfo info = new ChartRenderingInfo();
            JFreeChart chart = new JFreeChart(plot);
             chart.createBufferedImage(300, 200, 
                    info);
            success = true;
        }
        catch (Exception e) {
            success = false;
        }
        assertTrue(success);
    }

// org.jfree.chart.renderer.category.junit.BoxAndWhiskerRendererTests::testDrawWithNullQ1
    public void testDrawWithNullQ1() {
        boolean success = false;
        try {
            DefaultBoxAndWhiskerCategoryDataset dataset 
                    = new DefaultBoxAndWhiskerCategoryDataset();
            dataset.add(new BoxAndWhiskerItem(new Double(1.0), new Double(2.0),
                    null, new Double(4.0), new Double(0.5), 
                    new Double(4.5), new Double(-0.5), new Double(5.5), 
                    null), "S1", "C1");
            CategoryPlot plot = new CategoryPlot(dataset, 
                    new CategoryAxis("Category"), new NumberAxis("Value"), 
                    new BoxAndWhiskerRenderer());
            ChartRenderingInfo info = new ChartRenderingInfo();
            JFreeChart chart = new JFreeChart(plot);
             chart.createBufferedImage(300, 200, 
                    info);
            success = true;
        }
        catch (Exception e) {
            success = false;
        }
        assertTrue(success);
    }

// org.jfree.chart.renderer.category.junit.BoxAndWhiskerRendererTests::testDrawWithNullQ3
    public void testDrawWithNullQ3() {
        boolean success = false;
        try {
            DefaultBoxAndWhiskerCategoryDataset dataset 
                    = new DefaultBoxAndWhiskerCategoryDataset();
            dataset.add(new BoxAndWhiskerItem(new Double(1.0), new Double(2.0),
                    new Double(3.0), null, new Double(0.5), 
                    new Double(4.5), new Double(-0.5), new Double(5.5), 
                    null), "S1", "C1");
            CategoryPlot plot = new CategoryPlot(dataset, 
                    new CategoryAxis("Category"), new NumberAxis("Value"), 
                    new BoxAndWhiskerRenderer());
            ChartRenderingInfo info = new ChartRenderingInfo();
            JFreeChart chart = new JFreeChart(plot);
             chart.createBufferedImage(300, 200, 
                    info);
            success = true;
        }
        catch (Exception e) {
            success = false;
        }
        assertTrue(success);
    }

// org.jfree.chart.renderer.category.junit.BoxAndWhiskerRendererTests::testDrawWithNullMinRegular
    public void testDrawWithNullMinRegular() {
        boolean success = false;
        try {
            DefaultBoxAndWhiskerCategoryDataset dataset 
                    = new DefaultBoxAndWhiskerCategoryDataset();
            dataset.add(new BoxAndWhiskerItem(new Double(1.0), new Double(2.0),
                    new Double(3.0), new Double(4.0), null, 
                    new Double(4.5), new Double(-0.5), new Double(5.5), 
                    null), "S1", "C1");
            CategoryPlot plot = new CategoryPlot(dataset, 
                    new CategoryAxis("Category"), new NumberAxis("Value"), 
                    new BoxAndWhiskerRenderer());
            ChartRenderingInfo info = new ChartRenderingInfo();
            JFreeChart chart = new JFreeChart(plot);
             chart.createBufferedImage(300, 200, 
                    info);
            success = true;
        }
        catch (Exception e) {
            success = false;
        }
        assertTrue(success);
    }

// org.jfree.chart.renderer.category.junit.BoxAndWhiskerRendererTests::testDrawWithNullMaxRegular
    public void testDrawWithNullMaxRegular() {
        boolean success = false;
        try {
            DefaultBoxAndWhiskerCategoryDataset dataset 
                    = new DefaultBoxAndWhiskerCategoryDataset();
            dataset.add(new BoxAndWhiskerItem(new Double(1.0), new Double(2.0),
                    new Double(3.0), new Double(4.0), new Double(0.5), 
                    null, new Double(-0.5), new Double(5.5), 
                    null), "S1", "C1");
            CategoryPlot plot = new CategoryPlot(dataset, 
                    new CategoryAxis("Category"), new NumberAxis("Value"), 
                    new BoxAndWhiskerRenderer());
            ChartRenderingInfo info = new ChartRenderingInfo();
            JFreeChart chart = new JFreeChart(plot);
             chart.createBufferedImage(300, 200, 
                    info);
            success = true;
        }
        catch (Exception e) {
            success = false;
        }
        assertTrue(success);
    }

// org.jfree.chart.renderer.category.junit.BoxAndWhiskerRendererTests::testDrawWithNullMinOutlier
    public void testDrawWithNullMinOutlier() {
        boolean success = false;
        try {
            DefaultBoxAndWhiskerCategoryDataset dataset 
                    = new DefaultBoxAndWhiskerCategoryDataset();
            dataset.add(new BoxAndWhiskerItem(new Double(1.0), new Double(2.0),
                    new Double(3.0), new Double(4.0), new Double(0.5), 
                    new Double(4.5), null, new Double(5.5), 
                    null), "S1", "C1");
            CategoryPlot plot = new CategoryPlot(dataset, 
                    new CategoryAxis("Category"), new NumberAxis("Value"), 
                    new BoxAndWhiskerRenderer());
            ChartRenderingInfo info = new ChartRenderingInfo();
            JFreeChart chart = new JFreeChart(plot);
             chart.createBufferedImage(300, 200, 
                    info);
            success = true;
        }
        catch (Exception e) {
            success = false;
        }
        assertTrue(success);
    }

// org.jfree.chart.renderer.category.junit.BoxAndWhiskerRendererTests::testDrawWithNullMaxOutlier
    public void testDrawWithNullMaxOutlier() {
        boolean success = false;
        try {
            DefaultBoxAndWhiskerCategoryDataset dataset 
                    = new DefaultBoxAndWhiskerCategoryDataset();
            dataset.add(new BoxAndWhiskerItem(new Double(1.0), new Double(2.0),
                    new Double(3.0), new Double(4.0), new Double(0.5), 
                    new Double(4.5), new Double(-0.5), null, 
                    new java.util.ArrayList()), "S1", "C1");
            CategoryPlot plot = new CategoryPlot(dataset, 
                    new CategoryAxis("Category"), new NumberAxis("Value"), 
                    new BoxAndWhiskerRenderer());
            ChartRenderingInfo info = new ChartRenderingInfo();
            JFreeChart chart = new JFreeChart(plot);
             chart.createBufferedImage(300, 200, 
                    info);
            success = true;
        }
        catch (Exception e) {
            success = false;
        }
        assertTrue(success);
    }

// org.jfree.chart.renderer.category.junit.CategoryStepRendererTests::testEquals
    public void testEquals() {
        CategoryStepRenderer r1 = new CategoryStepRenderer(false);
        CategoryStepRenderer r2 = new CategoryStepRenderer(false);
        assertEquals(r1, r2);
        
        r1 = new CategoryStepRenderer(true);
        assertFalse(r1.equals(r2));
        r2 = new CategoryStepRenderer(true);
        assertTrue(r1.equals(r2));
    }

// org.jfree.chart.renderer.category.junit.CategoryStepRendererTests::testCloning
    public void testCloning() {
        CategoryStepRenderer r1 = new CategoryStepRenderer(false);
        CategoryStepRenderer r2 = null;
        try {
            r2 = (CategoryStepRenderer) r1.clone();
        }
        catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        assertTrue(r1 != r2);
        assertTrue(r1.getClass() == r2.getClass());
        assertTrue(r1.equals(r2));
    }

// org.jfree.chart.renderer.category.junit.CategoryStepRendererTests::testSerialization
    public void testSerialization() {

        CategoryStepRenderer r1 = new CategoryStepRenderer();
        CategoryStepRenderer r2 = null;

        try {
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            ObjectOutput out = new ObjectOutputStream(buffer);
            out.writeObject(r1);
            out.close();

            ObjectInput in = new ObjectInputStream(
                    new ByteArrayInputStream(buffer.toByteArray()));
            r2 = (CategoryStepRenderer) in.readObject();
            in.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        assertEquals(r1, r2);

    }

// org.jfree.chart.renderer.category.junit.CategoryStepRendererTests::testGetLegendItemSeriesIndex
    public void testGetLegendItemSeriesIndex() {
        DefaultCategoryDataset dataset0 = new DefaultCategoryDataset();
        dataset0.addValue(21.0, "R1", "C1");
        dataset0.addValue(22.0, "R2", "C1");        
        DefaultCategoryDataset dataset1 = new DefaultCategoryDataset();
        dataset1.addValue(23.0, "R3", "C1");
        dataset1.addValue(24.0, "R4", "C1");        
        dataset1.addValue(25.0, "R5", "C1");        
        CategoryStepRenderer r = new CategoryStepRenderer();
        CategoryPlot plot = new CategoryPlot(dataset0, new CategoryAxis("x"),
                new NumberAxis("y"), r);
        plot.setDataset(1, dataset1);
         new JFreeChart(plot);
        LegendItem li = r.getLegendItem(1, 2);
        assertEquals("R5", li.getLabel());
        assertEquals(1, li.getDatasetIndex());
        assertEquals(2, li.getSeriesIndex());
    }

// org.jfree.chart.renderer.category.junit.GroupedStackedBarRendererTests::testEquals
    public void testEquals() {
        GroupedStackedBarRenderer r1 = new GroupedStackedBarRenderer();
        GroupedStackedBarRenderer r2 = new GroupedStackedBarRenderer();
        assertTrue(r1.equals(r2));
        assertTrue(r2.equals(r1));
        
        
        KeyToGroupMap m1 = new KeyToGroupMap("G1");
        m1.mapKeyToGroup("S1", "G2");
        r1.setSeriesToGroupMap(m1);
        assertFalse(r1.equals(r2));
        KeyToGroupMap m2 = new KeyToGroupMap("G1");
        m2.mapKeyToGroup("S1", "G2");
        r2.setSeriesToGroupMap(m2);
        assertTrue(r1.equals(r2));
    }

// org.jfree.chart.renderer.category.junit.GroupedStackedBarRendererTests::testCloning
    public void testCloning() {
        GroupedStackedBarRenderer r1 = new GroupedStackedBarRenderer();
        GroupedStackedBarRenderer r2 = null;
        try {
            r2 = (GroupedStackedBarRenderer) r1.clone();
        }
        catch (CloneNotSupportedException e) {
            System.err.println("Failed to clone.");
        }
        assertTrue(r1 != r2);
        assertTrue(r1.getClass() == r2.getClass());
        assertTrue(r1.equals(r2));
    }

// org.jfree.chart.renderer.category.junit.GroupedStackedBarRendererTests::testSerialization
    public void testSerialization() {

        GroupedStackedBarRenderer r1 = new GroupedStackedBarRenderer();
        GroupedStackedBarRenderer r2 = null;
        try {
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            ObjectOutput out = new ObjectOutputStream(buffer);
            out.writeObject(r1);
            out.close();

            ObjectInput in = new ObjectInputStream(
                new ByteArrayInputStream(buffer.toByteArray())
            );
            r2 = (GroupedStackedBarRenderer) in.readObject();
            in.close();
        }
        catch (Exception e) {
            System.out.println(e.toString());
        }
        assertEquals(r1, r2);

    }

// org.jfree.chart.renderer.category.junit.GroupedStackedBarRendererTests::testDrawWithNullInfo
    public void testDrawWithNullInfo() {
        boolean success = false;
        try {
            DefaultCategoryDataset dataset = new DefaultCategoryDataset();
            dataset.addValue(1.0, "S1", "C1");
            dataset.addValue(2.0, "S1", "C2");
            dataset.addValue(3.0, "S2", "C1");
            dataset.addValue(4.0, "S2", "C2");
            GroupedStackedBarRenderer renderer 
                = new GroupedStackedBarRenderer();
            CategoryPlot plot = new CategoryPlot(dataset, 
                    new CategoryAxis("Category"), new NumberAxis("Value"), 
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

// org.jfree.chart.renderer.category.junit.LayeredBarRendererTests::testEquals
    public void testEquals() {
        LayeredBarRenderer r1 = new LayeredBarRenderer();
        LayeredBarRenderer r2 = new LayeredBarRenderer();
        assertEquals(r1, r2);
    }

// org.jfree.chart.renderer.category.junit.LayeredBarRendererTests::testHashcode
    public void testHashcode() {
        LayeredBarRenderer r1 = new LayeredBarRenderer();
        LayeredBarRenderer r2 = new LayeredBarRenderer();
        assertTrue(r1.equals(r2));
        int h1 = r1.hashCode();
        int h2 = r2.hashCode();
        assertEquals(h1, h2);
    }

// org.jfree.chart.renderer.category.junit.LayeredBarRendererTests::testCloning
    public void testCloning() {
        LayeredBarRenderer r1 = new LayeredBarRenderer();
        LayeredBarRenderer r2 = null;
        try {
            r2 = (LayeredBarRenderer) r1.clone();
        }
        catch (CloneNotSupportedException e) {
            System.err.println("Failed to clone.");
        }
        assertTrue(r1 != r2);
        assertTrue(r1.getClass() == r2.getClass());
        assertTrue(r1.equals(r2));
    }

// org.jfree.chart.renderer.category.junit.LayeredBarRendererTests::testSerialization
    public void testSerialization() {

        LayeredBarRenderer r1 = new LayeredBarRenderer();
        LayeredBarRenderer r2 = null;

        try {
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            ObjectOutput out = new ObjectOutputStream(buffer);
            out.writeObject(r1);
            out.close();

            ObjectInput in = new ObjectInputStream(
                new ByteArrayInputStream(buffer.toByteArray())
            );
            r2 = (LayeredBarRenderer) in.readObject();
            in.close();
        }
        catch (Exception e) {
            System.out.println(e.toString());
        }
        assertEquals(r1, r2);

    }

// org.jfree.chart.renderer.category.junit.LayeredBarRendererTests::testDrawWithNullInfo
    public void testDrawWithNullInfo() {
        boolean success = false;
        try {
            DefaultCategoryDataset dataset = new DefaultCategoryDataset();
            dataset.addValue(1.0, "S1", "C1");
            CategoryPlot plot = new CategoryPlot(dataset, 
                    new CategoryAxis("Category"), new NumberAxis("Value"), 
                    new LayeredBarRenderer());
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

// org.jfree.chart.renderer.category.junit.LevelRendererTests::testEquals
    public void testEquals() {
        LevelRenderer r1 = new LevelRenderer();
        LevelRenderer r2 = new LevelRenderer();
        assertTrue(r1.equals(r2));
        assertTrue(r2.equals(r1));
        
        r1.setItemMargin(0.123);
        assertFalse(r1.equals(r2));
        r2.setItemMargin(0.123);
        assertTrue(r1.equals(r2));

        r1.setMaximumItemWidth(0.234);
        assertFalse(r1.equals(r2));
        r2.setMaximumItemWidth(0.234);
        assertTrue(r1.equals(r2));
    
    }

// org.jfree.chart.renderer.category.junit.LevelRendererTests::testHashcode
    public void testHashcode() {
        LevelRenderer r1 = new LevelRenderer();
        LevelRenderer r2 = new LevelRenderer();
        assertTrue(r1.equals(r2));
        int h1 = r1.hashCode();
        int h2 = r2.hashCode();
        assertEquals(h1, h2);
    }

// org.jfree.chart.renderer.category.junit.LevelRendererTests::testCloning
    public void testCloning() {
        LevelRenderer r1 = new LevelRenderer();
        r1.setItemMargin(0.123);
        r1.setMaximumItemWidth(0.234);
        LevelRenderer r2 = null;
        try {
            r2 = (LevelRenderer) r1.clone();
        }
        catch (CloneNotSupportedException e) {
            System.err.println("Failed to clone.");
        }
        assertTrue(r1 != r2);
        assertTrue(r1.getClass() == r2.getClass());
        assertTrue(r1.equals(r2));
        
        assertTrue(checkIndependence(r1, r2));
        
    }

// org.jfree.chart.renderer.category.junit.LevelRendererTests::testSerialization
    public void testSerialization() {

        LevelRenderer r1 = new LevelRenderer();
        LevelRenderer r2 = null;

        try {
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            ObjectOutput out = new ObjectOutputStream(buffer);
            out.writeObject(r1);
            out.close();

            ObjectInput in = new ObjectInputStream(
                new ByteArrayInputStream(buffer.toByteArray())
            );
            r2 = (LevelRenderer) in.readObject();
            in.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        assertEquals(r1, r2);

    }

// org.jfree.chart.renderer.category.junit.LevelRendererTests::testDrawWithNullInfo
    public void testDrawWithNullInfo() {
        boolean success = false;
        try {
            DefaultCategoryDataset dataset = new DefaultCategoryDataset();
            dataset.addValue(1.0, "S1", "C1");
            CategoryPlot plot = new CategoryPlot(dataset, 
                    new CategoryAxis("Category"), new NumberAxis("Value"), 
                    new LevelRenderer());
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

// org.jfree.chart.renderer.category.junit.LevelRendererTests::testGetLegendItemSeriesIndex
    public void testGetLegendItemSeriesIndex() {
        DefaultCategoryDataset dataset0 = new DefaultCategoryDataset();
        dataset0.addValue(21.0, "R1", "C1");
        dataset0.addValue(22.0, "R2", "C1");        
        DefaultCategoryDataset dataset1 = new DefaultCategoryDataset();
        dataset1.addValue(23.0, "R3", "C1");
        dataset1.addValue(24.0, "R4", "C1");        
        dataset1.addValue(25.0, "R5", "C1");        
        LevelRenderer r = new LevelRenderer();
        CategoryPlot plot = new CategoryPlot(dataset0, new CategoryAxis("x"),
                new NumberAxis("y"), r);
        plot.setDataset(1, dataset1);
         new JFreeChart(plot);
        LegendItem li = r.getLegendItem(1, 2);
        assertEquals("R5", li.getLabel());
        assertEquals(1, li.getDatasetIndex());
        assertEquals(2, li.getSeriesIndex());
    }

// org.jfree.chart.renderer.category.junit.LineAndShapeRendererTests::testEquals
    public void testEquals() {
        
        LineAndShapeRenderer r1 = new LineAndShapeRenderer();
        LineAndShapeRenderer r2 = new LineAndShapeRenderer();
        assertEquals(r1, r2);
        
        r1.setBaseLinesVisible(!r1.getBaseLinesVisible());
        assertFalse(r1.equals(r2));
        r2.setBaseLinesVisible(r1.getBaseLinesVisible());
        assertTrue(r1.equals(r2));
        
        r1.setSeriesLinesVisible(1, true);
        assertFalse(r1.equals(r2));
        r2.setSeriesLinesVisible(1, true);
        assertTrue(r1.equals(r2));
        
        r1.setBaseShapesVisible(!r1.getBaseShapesVisible());
        assertFalse(r1.equals(r2));
        r2.setBaseShapesVisible(r1.getBaseShapesVisible());
        assertTrue(r1.equals(r2));
        
        r1.setSeriesShapesVisible(1, true);
        assertFalse(r1.equals(r2));
        r2.setSeriesShapesVisible(1, true);
        assertTrue(r1.equals(r2));
        
        r1.setSeriesShapesFilled(1, true);
        assertFalse(r1.equals(r2));
        r2.setSeriesShapesFilled(1, true);
        assertTrue(r1.equals(r2));
        
        r1.setBaseShapesFilled(false);
        assertFalse(r1.equals(r2));
        r2.setBaseShapesFilled(false);
        assertTrue(r1.equals(r2));
        
        r1.setUseOutlinePaint(true);
        assertFalse(r1.equals(r2));
        r2.setUseOutlinePaint(true);
        assertTrue(r1.equals(r2));
        
        r1.setUseSeriesOffset(true);
        assertFalse(r1.equals(r2));
        r2.setUseSeriesOffset(true);
        assertTrue(r1.equals(r2));
        
        r1.setItemMargin(0.14);
        assertFalse(r1.equals(r2));
        r2.setItemMargin(0.14);
        assertTrue(r1.equals(r2));
        
    }

// org.jfree.chart.renderer.category.junit.LineAndShapeRendererTests::testHashcode
    public void testHashcode() {
        LineAndShapeRenderer r1 = new LineAndShapeRenderer();
        LineAndShapeRenderer r2 = new LineAndShapeRenderer();
        assertTrue(r1.equals(r2));
        int h1 = r1.hashCode();
        int h2 = r2.hashCode();
        assertEquals(h1, h2);
    }

// org.jfree.chart.renderer.category.junit.LineAndShapeRendererTests::testCloning
    public void testCloning() {
        LineAndShapeRenderer r1 = new LineAndShapeRenderer();
        LineAndShapeRenderer r2 = null;
        try {
            r2 = (LineAndShapeRenderer) r1.clone();
        }
        catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        assertTrue(r1 != r2);
        assertTrue(r1.getClass() == r2.getClass());
        assertTrue(r1.equals(r2));
        
        assertTrue(checkIndependence(r1, r2));
        
    }

// org.jfree.chart.renderer.category.junit.LineAndShapeRendererTests::testSerialization
    public void testSerialization() {

        LineAndShapeRenderer r1 = new LineAndShapeRenderer();
        LineAndShapeRenderer r2 = null;

        try {
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            ObjectOutput out = new ObjectOutputStream(buffer);
            out.writeObject(r1);
            out.close();

            ObjectInput in = new ObjectInputStream(
                    new ByteArrayInputStream(buffer.toByteArray()));
            r2 = (LineAndShapeRenderer) in.readObject();
            in.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        assertEquals(r1, r2);

    }

// org.jfree.chart.renderer.category.junit.LineAndShapeRendererTests::testGetLegendItemSeriesIndex
    public void testGetLegendItemSeriesIndex() {
        DefaultCategoryDataset dataset0 = new DefaultCategoryDataset();
        dataset0.addValue(21.0, "R1", "C1");
        dataset0.addValue(22.0, "R2", "C1");        
        DefaultCategoryDataset dataset1 = new DefaultCategoryDataset();
        dataset1.addValue(23.0, "R3", "C1");
        dataset1.addValue(24.0, "R4", "C1");        
        dataset1.addValue(25.0, "R5", "C1");        
        LineAndShapeRenderer r = new LineAndShapeRenderer();
        CategoryPlot plot = new CategoryPlot(dataset0, new CategoryAxis("x"),
                new NumberAxis("y"), r);
        plot.setDataset(1, dataset1);
         new JFreeChart(plot);
        LegendItem li = r.getLegendItem(1, 2);
        assertEquals("R5", li.getLabel());
        assertEquals(1, li.getDatasetIndex());
        assertEquals(2, li.getSeriesIndex());
    }

// org.jfree.chart.renderer.category.junit.MinMaxCategoryRendererTests::testEquals
    public void testEquals() {
        MinMaxCategoryRenderer r1 = new MinMaxCategoryRenderer();
        MinMaxCategoryRenderer r2 = new MinMaxCategoryRenderer();
        assertEquals(r1, r2);
        
        r1.setDrawLines(true);
        assertFalse(r1.equals(r2));
        r2.setDrawLines(true);
        assertTrue(r1.equals(r2));
        
        r1.setGroupPaint(new GradientPaint(1.0f, 2.0f, Color.red, 3.0f, 4.0f, 
                Color.yellow));
        assertFalse(r1.equals(r2));
        r2.setGroupPaint(new GradientPaint(1.0f, 2.0f, Color.red, 3.0f, 4.0f, 
                Color.yellow));
        assertTrue(r1.equals(r2));
        
        r1.setGroupStroke(new BasicStroke(1.2f));
        assertFalse(r1.equals(r2));
        r2.setGroupStroke(new BasicStroke(1.2f));
        assertTrue(r1.equals(r2));
    }

// org.jfree.chart.renderer.category.junit.MinMaxCategoryRendererTests::testHashcode
    public void testHashcode() {
        MinMaxCategoryRenderer r1 = new MinMaxCategoryRenderer();
        MinMaxCategoryRenderer r2 = new MinMaxCategoryRenderer();
        assertTrue(r1.equals(r2));
        int h1 = r1.hashCode();
        int h2 = r2.hashCode();
        assertEquals(h1, h2);
    }

// org.jfree.chart.renderer.category.junit.MinMaxCategoryRendererTests::testCloning
    public void testCloning() {
        MinMaxCategoryRenderer r1 = new MinMaxCategoryRenderer();
        MinMaxCategoryRenderer r2 = null;
        try {
            r2 = (MinMaxCategoryRenderer) r1.clone();
        }
        catch (CloneNotSupportedException e) {
            System.err.println("Failed to clone.");
        }
        assertTrue(r1 != r2);
        assertTrue(r1.getClass() == r2.getClass());
        assertTrue(r1.equals(r2));
    }

// org.jfree.chart.renderer.category.junit.MinMaxCategoryRendererTests::testSerialization
    public void testSerialization() {

        MinMaxCategoryRenderer r1 = new MinMaxCategoryRenderer();
        MinMaxCategoryRenderer r2 = null;

        try {
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            ObjectOutput out = new ObjectOutputStream(buffer);
            out.writeObject(r1);
            out.close();

            ObjectInput in = new ObjectInputStream(
                    new ByteArrayInputStream(buffer.toByteArray()));
            r2 = (MinMaxCategoryRenderer) in.readObject();
            in.close();
        }
        catch (Exception e) {
            System.out.println(e.toString());
        }
        assertEquals(r1, r2);

    }

// org.jfree.chart.renderer.category.junit.MinMaxCategoryRendererTests::testDrawWithNullInfo
    public void testDrawWithNullInfo() {
        boolean success = false;
        try {
            DefaultCategoryDataset dataset = new DefaultCategoryDataset();
            dataset.addValue(1.0, "S1", "C1");
            CategoryPlot plot = new CategoryPlot(dataset, 
                    new CategoryAxis("Category"), new NumberAxis("Value"), 
                    new MinMaxCategoryRenderer());
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

// org.jfree.chart.renderer.category.junit.StackedBarRenderer3DTests::testEquals
    public void testEquals() {
        StackedBarRenderer3D r1 = new StackedBarRenderer3D();
        StackedBarRenderer3D r2 = new StackedBarRenderer3D();
        assertEquals(r1, r2);
    }

// org.jfree.chart.renderer.category.junit.StackedBarRenderer3DTests::testHashcode
    public void testHashcode() {
        StackedBarRenderer3D r1 = new StackedBarRenderer3D();
        StackedBarRenderer3D r2 = new StackedBarRenderer3D();
        assertTrue(r1.equals(r2));
        int h1 = r1.hashCode();
        int h2 = r2.hashCode();
        assertEquals(h1, h2);
    }

// org.jfree.chart.renderer.category.junit.StackedBarRenderer3DTests::testCloning
    public void testCloning() {
        StackedBarRenderer3D r1 = new StackedBarRenderer3D();
        StackedBarRenderer3D r2 = null;
        try {
            r2 = (StackedBarRenderer3D) r1.clone();
        }
        catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        assertTrue(r1 != r2);
        assertTrue(r1.getClass() == r2.getClass());
        assertTrue(r1.equals(r2));
    }

// org.jfree.chart.renderer.category.junit.StackedBarRenderer3DTests::testSerialization
    public void testSerialization() {

        StackedBarRenderer3D r1 = new StackedBarRenderer3D();
        StackedBarRenderer3D r2 = null;

        try {
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            ObjectOutput out = new ObjectOutputStream(buffer);
            out.writeObject(r1);
            out.close();

            ObjectInput in = new ObjectInputStream(
                    new ByteArrayInputStream(buffer.toByteArray()));
            r2 = (StackedBarRenderer3D) in.readObject();
            in.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        assertEquals(r1, r2);

    }

// org.jfree.chart.renderer.category.junit.StackedBarRenderer3DTests::testCreateStackedValueList1
    public void testCreateStackedValueList1() {
        DefaultCategoryDataset d = new DefaultCategoryDataset();
        d.addValue(1.0, "s0", "c0");
        MyRenderer r = new MyRenderer();
        List l = r.createStackedValueList(d, "c0", 0.0, false);
        assertEquals(2, l.size());
        assertEquals(new Double(0.0), ((Object[]) l.get(0))[1]);
        assertEquals(new Double(1.0), ((Object[]) l.get(1))[1]);
    }

// org.jfree.chart.renderer.category.junit.StackedBarRenderer3DTests::testCreateStackedValueList2
    public void testCreateStackedValueList2() {
        DefaultCategoryDataset d = new DefaultCategoryDataset();
        d.addValue(-1.0, "s0", "c0");
        MyRenderer r = new MyRenderer();
        List l = r.createStackedValueList(d, "c0", 0.0, false);
        assertEquals(2, l.size());
        assertEquals(new Double(-1.0), ((Object[]) l.get(0))[1]);
        assertEquals(new Double(0.0), ((Object[]) l.get(1))[1]);
    }

// org.jfree.chart.renderer.category.junit.StackedBarRenderer3DTests::testCreateStackedValueList3
    public void testCreateStackedValueList3() {
        DefaultCategoryDataset d = new DefaultCategoryDataset();
        d.addValue(0.0, "s0", "c0");
        MyRenderer r = new MyRenderer();
        List l = r.createStackedValueList(d, "c0", 0.0, false);
        assertEquals(2, l.size());
        assertEquals(new Double(0.0), ((Object[]) l.get(0))[1]);
        assertEquals(new Double(0.0), ((Object[]) l.get(1))[1]);
    }

// org.jfree.chart.renderer.category.junit.StackedBarRenderer3DTests::testCreateStackedValueList4
    public void testCreateStackedValueList4() {
        DefaultCategoryDataset d = new DefaultCategoryDataset();
        d.addValue(null, "s0", "c0");
        MyRenderer r = new MyRenderer();
        List l = r.createStackedValueList(d, "c0", 0.0, false);
        assertEquals(0, l.size());
    }

// org.jfree.chart.renderer.category.junit.StackedBarRenderer3DTests::testCreateStackedValueList1a
    public void testCreateStackedValueList1a() {
        DefaultCategoryDataset d = new DefaultCategoryDataset();
        d.addValue(1.0, "s0", "c0");
        d.addValue(1.1, "s1", "c0");
        MyRenderer r = new MyRenderer();
        List l = r.createStackedValueList(d, "c0", 0.0, false);
        assertEquals(3, l.size());
        assertEquals(new Double(0.0), ((Object[]) l.get(0))[1]);
        assertEquals(new Double(1.0), ((Object[]) l.get(1))[1]);
        assertEquals(new Double(2.1), ((Object[]) l.get(2))[1]);
    }

// org.jfree.chart.renderer.category.junit.StackedBarRenderer3DTests::testCreateStackedValueList1b
    public void testCreateStackedValueList1b() {
        DefaultCategoryDataset d = new DefaultCategoryDataset();
        d.addValue(1.0, "s0", "c0");
        d.addValue(-1.1, "s1", "c0");
        MyRenderer r = new MyRenderer();
        List l = r.createStackedValueList(d, "c0", 0.0, false);
        assertEquals(3, l.size());
        assertEquals(new Double(-1.1), ((Object[]) l.get(0))[1]);
        assertEquals(new Double(0.0), ((Object[]) l.get(1))[1]);
        assertEquals(new Double(1.0), ((Object[]) l.get(2))[1]);
    }

// org.jfree.chart.renderer.category.junit.StackedBarRenderer3DTests::testCreateStackedValueList1c
    public void testCreateStackedValueList1c() {
        DefaultCategoryDataset d = new DefaultCategoryDataset();
        d.addValue(1.0, "s0", "c0");
        d.addValue(0.0, "s1", "c0");
        MyRenderer r = new MyRenderer();
        List l = r.createStackedValueList(d, "c0", 0.0, false);
        assertEquals(3, l.size());
        assertEquals(new Double(0.0), ((Object[]) l.get(0))[1]);
        assertEquals(new Double(1.0), ((Object[]) l.get(1))[1]);
        assertEquals(new Double(1.0), ((Object[]) l.get(2))[1]);
    }

// org.jfree.chart.renderer.category.junit.StackedBarRenderer3DTests::testCreateStackedValueList1d
    public void testCreateStackedValueList1d() {
        DefaultCategoryDataset d = new DefaultCategoryDataset();
        d.addValue(1.0, "s0", "c0");
        d.addValue(null, "s1", "c0");
        MyRenderer r = new MyRenderer();
        List l = r.createStackedValueList(d, "c0", 0.0, false);
        assertEquals(2, l.size());
        assertEquals(new Double(0.0), ((Object[]) l.get(0))[1]);
        assertEquals(new Double(1.0), ((Object[]) l.get(1))[1]);
    }

// org.jfree.chart.renderer.category.junit.StackedBarRenderer3DTests::testCreateStackedValueList2a
    public void testCreateStackedValueList2a() {
        DefaultCategoryDataset d = new DefaultCategoryDataset();
        d.addValue(-1.0, "s0", "c0");
        d.addValue(1.1, "s1", "c0");
        MyRenderer r = new MyRenderer();
        List l = r.createStackedValueList(d, "c0", 0.0, false);
        assertEquals(3, l.size());
        assertEquals(new Double(-1.0), ((Object[]) l.get(0))[1]);
        assertEquals(new Double(0.0), ((Object[]) l.get(1))[1]);
        assertEquals(new Double(1.1), ((Object[]) l.get(2))[1]);
    }

// org.jfree.chart.renderer.category.junit.StackedBarRenderer3DTests::testCreateStackedValueList2b
    public void testCreateStackedValueList2b() {
        DefaultCategoryDataset d = new DefaultCategoryDataset();
        d.addValue(-1.0, "s0", "c0");
        d.addValue(-1.1, "s1", "c0");
        MyRenderer r = new MyRenderer();
        List l = r.createStackedValueList(d, "c0", 0.0, false);
        assertEquals(3, l.size());
        assertEquals(new Double(-2.1), ((Object[]) l.get(0))[1]);
        assertEquals(new Double(-1.0), ((Object[]) l.get(1))[1]);
        assertEquals(new Double(0.0), ((Object[]) l.get(2))[1]);
    }

// org.jfree.chart.renderer.category.junit.StackedBarRenderer3DTests::testCreateStackedValueList2c
    public void testCreateStackedValueList2c() {
        DefaultCategoryDataset d = new DefaultCategoryDataset();
        d.addValue(-1.0, "s0", "c0");
        d.addValue(0.0, "s1", "c0");
        MyRenderer r = new MyRenderer();
        List l = r.createStackedValueList(d, "c0", 0.0, false);
        assertEquals(3, l.size());
        assertEquals(new Double(-1.0), ((Object[]) l.get(0))[1]);
        assertEquals(new Double(0.0), ((Object[]) l.get(1))[1]);
        assertEquals(new Double(0.0), ((Object[]) l.get(2))[1]);
    }

// org.jfree.chart.renderer.category.junit.StackedBarRenderer3DTests::testCreateStackedValueList2d
    public void testCreateStackedValueList2d() {
        DefaultCategoryDataset d = new DefaultCategoryDataset();
        d.addValue(-1.0, "s0", "c0");
        d.addValue(null, "s1", "c0");
        MyRenderer r = new MyRenderer();
        List l = r.createStackedValueList(d, "c0", 0.0, false);
        assertEquals(2, l.size());
        assertEquals(new Double(-1.0), ((Object[]) l.get(0))[1]);
        assertEquals(new Double(0.0), ((Object[]) l.get(1))[1]);
    }

// org.jfree.chart.renderer.category.junit.StackedBarRenderer3DTests::testCreateStackedValueList3a
    public void testCreateStackedValueList3a() {
        DefaultCategoryDataset d = new DefaultCategoryDataset();
        d.addValue(0.0, "s0", "c0");
        d.addValue(1.1, "s1", "c0");
        MyRenderer r = new MyRenderer();
        List l = r.createStackedValueList(d, "c0", 0.0, false);
        assertEquals(3, l.size());
        assertEquals(new Double(0.0), ((Object[]) l.get(0))[1]);
        assertEquals(new Double(0.0), ((Object[]) l.get(1))[1]);
        assertEquals(new Double(1.1), ((Object[]) l.get(2))[1]);
    }

// org.jfree.chart.renderer.category.junit.StackedBarRenderer3DTests::testCreateStackedValueList3b
    public void testCreateStackedValueList3b() {
        DefaultCategoryDataset d = new DefaultCategoryDataset();
        d.addValue(0.0, "s0", "c0");
        d.addValue(-1.1, "s1", "c0");
        MyRenderer r = new MyRenderer();
        List l = r.createStackedValueList(d, "c0", 0.0, false);
        assertEquals(3, l.size());
        assertEquals(new Double(-1.1), ((Object[]) l.get(0))[1]);
        assertEquals(new Double(0.0), ((Object[]) l.get(1))[1]);
        assertEquals(new Double(0.0), ((Object[]) l.get(2))[1]);
    }

// org.jfree.chart.renderer.category.junit.StackedBarRenderer3DTests::testCreateStackedValueList3c
    public void testCreateStackedValueList3c() {
        DefaultCategoryDataset d = new DefaultCategoryDataset();
        d.addValue(0.0, "s0", "c0");
        d.addValue(0.0, "s1", "c0");
        MyRenderer r = new MyRenderer();
        List l = r.createStackedValueList(d, "c0", 0.0, false);
        assertEquals(3, l.size());
        assertEquals(new Double(0.0), ((Object[]) l.get(0))[1]);
        assertEquals(new Double(0.0), ((Object[]) l.get(1))[1]);
        assertEquals(new Double(0.0), ((Object[]) l.get(2))[1]);
    }

// org.jfree.chart.renderer.category.junit.StackedBarRenderer3DTests::testCreateStackedValueList3d
    public void testCreateStackedValueList3d() {
        DefaultCategoryDataset d = new DefaultCategoryDataset();
        d.addValue(0.0, "s0", "c0");
        d.addValue(null, "s1", "c0");
        MyRenderer r = new MyRenderer();
        List l = r.createStackedValueList(d, "c0", 0.0, false);
        assertEquals(2, l.size());
        assertEquals(new Double(0.0), ((Object[]) l.get(0))[1]);
        assertEquals(new Double(0.0), ((Object[]) l.get(1))[1]);
    }

// org.jfree.chart.renderer.category.junit.StackedBarRenderer3DTests::testCreateStackedValueList5
    public void testCreateStackedValueList5() {
        DefaultCategoryDataset d = new DefaultCategoryDataset();
        d.addValue(1.0, "s0", "c0");
        d.addValue(null, "s1", "c0");
        d.addValue(2.0, "s2", "c0");
        MyRenderer r = new MyRenderer();
        List l = r.createStackedValueList(d, "c0", 0.0, false);
        assertEquals(3, l.size());
        assertEquals(new Double(0.0), ((Object[]) l.get(0))[1]);
        assertEquals(new Double(1.0), ((Object[]) l.get(1))[1]);
        assertEquals(new Double(3.0), ((Object[]) l.get(2))[1]);
    }

// org.jfree.chart.urls.junit.StandardPieURLGeneratorTests::testEquals
    public void testEquals() {
        StandardPieURLGenerator g1 = new StandardPieURLGenerator();
        StandardPieURLGenerator g2 = new StandardPieURLGenerator();
        assertTrue(g1.equals(g2));
        
        g1 = new StandardPieURLGenerator("prefix", "category", "index");
        assertFalse(g1.equals(g2));
        g2 = new StandardPieURLGenerator("prefix", "category", "index");
        assertTrue(g1.equals(g2));
        
        g1 = new StandardPieURLGenerator("prefix2", "category", "index");
        assertFalse(g1.equals(g2));
        g2 = new StandardPieURLGenerator("prefix2", "category", "index");
        assertTrue(g1.equals(g2));
        
        g1 = new StandardPieURLGenerator("prefix2", "category2", "index");
        assertFalse(g1.equals(g2));
        g2 = new StandardPieURLGenerator("prefix2", "category2", "index");
        assertTrue(g1.equals(g2));

        g1 = new StandardPieURLGenerator("prefix2", "category2", "index2");
        assertFalse(g1.equals(g2));
        g2 = new StandardPieURLGenerator("prefix2", "category2", "index2");
        assertTrue(g1.equals(g2));

        g1 = new StandardPieURLGenerator("prefix2", "category2", null);
        assertFalse(g1.equals(g2));
        g2 = new StandardPieURLGenerator("prefix2", "category2", null);
        assertTrue(g1.equals(g2));
    }

// org.jfree.chart.urls.junit.StandardPieURLGeneratorTests::testSerialization
    public void testSerialization() {

        StandardPieURLGenerator g1 = new StandardPieURLGenerator(
                "index.html?", "cat");
        StandardPieURLGenerator g2 = null;

        try {
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            ObjectOutput out = new ObjectOutputStream(buffer);
            out.writeObject(g1);
            out.close();

            ObjectInput in = new ObjectInputStream(
                    new ByteArrayInputStream(buffer.toByteArray()));
            g2 = (StandardPieURLGenerator) in.readObject();
            in.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        assertEquals(g1, g2);

    }

// org.jfree.chart.urls.junit.StandardPieURLGeneratorTests::testURL
    public void testURL() {
        DefaultPieDataset dataset = new DefaultPieDataset();
        dataset.setValue("Alpha '1'", new Double(5.0));
        dataset.setValue("Beta", new Double(5.5));
        StandardPieURLGenerator g1 = new StandardPieURLGenerator(
                "chart.jsp", "category");
        String url = g1.generateURL(dataset, "Beta", 0);
        assertEquals("chart.jsp?category=Beta&amp;pieIndex=0", url);
        url = g1.generateURL(dataset, "Alpha '1'", 0);
        assertEquals("chart.jsp?category=Alpha+%271%27&amp;pieIndex=0", url);
    }

// org.jfree.data.category.junit.CategoryToPieDatasetTests::testConstructor
    public void testConstructor() {
        
        CategoryToPieDataset p1 = new CategoryToPieDataset(null, 
                TableOrder.BY_COLUMN, 0);
        assertNull(p1.getUnderlyingDataset());
        assertEquals(p1.getItemCount(), 0);
        assertTrue(p1.getKeys().isEmpty());
        assertNull(p1.getValue("R1"));
    }

// org.jfree.data.category.junit.CategoryToPieDatasetTests::testGetValue
    public void testGetValue() {
        DefaultCategoryDataset underlying = new DefaultCategoryDataset();
        underlying.addValue(1.1, "R1", "C1");
        underlying.addValue(2.2, "R1", "C2");
        CategoryToPieDataset d1 = new CategoryToPieDataset(underlying, 
                TableOrder.BY_ROW, 0);
        assertEquals(d1.getValue("C1"), new Double(1.1));
        assertEquals(d1.getValue("C2"), new Double(2.2));

        
        try {
             d1.getValue(-1);
            fail("Expected IndexOutOfBoundsException.");
        }
        catch (IndexOutOfBoundsException e) {
            
        }
        
        
        try {
             d1.getValue(d1.getItemCount());
            fail("Expected IndexOutOfBoundsException.");
        }
        catch (IndexOutOfBoundsException e) {
            
        }

        
        CategoryToPieDataset p1 = new CategoryToPieDataset(null, 
                TableOrder.BY_COLUMN, 0);
        try {
             p1.getValue(0);
            fail("Expected IndexOutOfBoundsException.");
        }
        catch (IndexOutOfBoundsException e) {
            
        }
    }

// org.jfree.data.category.junit.CategoryToPieDatasetTests::testGetKey
    public void testGetKey() {
        DefaultCategoryDataset underlying = new DefaultCategoryDataset();
        underlying.addValue(1.1, "R1", "C1");
        underlying.addValue(2.2, "R1", "C2");
        CategoryToPieDataset d1 = new CategoryToPieDataset(underlying, 
                TableOrder.BY_ROW, 0);
        assertEquals(d1.getKey(0), "C1");
        assertEquals(d1.getKey(1), "C2");

        
        try {
             d1.getKey(-1);
            fail("Expected IndexOutOfBoundsException.");
        }
        catch (IndexOutOfBoundsException e) {
            
        }
        
        
        try {
             d1.getKey(d1.getItemCount());
            fail("Expected IndexOutOfBoundsException.");
        }
        catch (IndexOutOfBoundsException e) {
            
        }

        
        CategoryToPieDataset p1 = new CategoryToPieDataset(null, 
                TableOrder.BY_COLUMN, 0);
        try {
             p1.getKey(0);
            fail("Expected IndexOutOfBoundsException.");
        }
        catch (IndexOutOfBoundsException e) {
            
        }
    }

// org.jfree.data.category.junit.CategoryToPieDatasetTests::testGetIndex
    public void testGetIndex() {
        DefaultCategoryDataset underlying = new DefaultCategoryDataset();
        underlying.addValue(1.1, "R1", "C1");
        underlying.addValue(2.2, "R1", "C2");
        CategoryToPieDataset d1 = new CategoryToPieDataset(underlying, 
                TableOrder.BY_ROW, 0);
        assertEquals(0, d1.getIndex("C1"));
        assertEquals(1, d1.getIndex("C2"));
        assertEquals(-1, d1.getIndex("XX"));
        
        
        boolean pass = false;
        try {
            d1.getIndex(null);
        }
        catch (IllegalArgumentException e) {
            pass = true;
        }
        assertTrue(pass);
    }

// org.jfree.data.category.junit.CategoryToPieDatasetTests::testEquals
    public void testEquals() {
        DefaultCategoryDataset underlying = new DefaultCategoryDataset();
        underlying.addValue(1.1, "R1", "C1");
        underlying.addValue(2.2, "R1", "C2");
        CategoryToPieDataset d1 = new CategoryToPieDataset(underlying, 
                TableOrder.BY_COLUMN, 1);
        DefaultPieDataset d2 = new DefaultPieDataset();
        d2.setValue("R1", 2.2);
        assertTrue(d1.equals(d2));
    }

// org.jfree.data.category.junit.CategoryToPieDatasetTests::testSerialization
    public void testSerialization() {
        DefaultCategoryDataset underlying = new DefaultCategoryDataset();
        underlying.addValue(1.1, "R1", "C1");
        underlying.addValue(2.2, "R1", "C2");
        CategoryToPieDataset d1 = new CategoryToPieDataset(underlying, 
                TableOrder.BY_COLUMN, 1);
        CategoryToPieDataset d2 = null;

        try {
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            ObjectOutput out = new ObjectOutputStream(buffer);
            out.writeObject(d1);
            out.close();

            ObjectInput in = new ObjectInputStream(
                new ByteArrayInputStream(buffer.toByteArray())
            );
            d2 = (CategoryToPieDataset) in.readObject();
            in.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        assertEquals(d1, d2);
        
        
        
        assertEquals(d1.getUnderlyingDataset(), d2.getUnderlyingDataset());
        assertEquals(d1.getExtractType(), d2.getExtractType());
        assertEquals(d1.getExtractIndex(), d2.getExtractIndex());
    }

// org.jfree.data.category.junit.DefaultCategoryDatasetTests::testGetValue
    public void testGetValue() {
        DefaultCategoryDataset d = new DefaultCategoryDataset();
        d.addValue(1.0, "R1", "C1");
        assertEquals(new Double(1.0), d.getValue("R1", "C1"));
        boolean pass = false;
        try {
            d.getValue("XX", "C1");
        }
        catch (UnknownKeyException e) {
            pass = true;   
        }
        assertTrue(pass);
        
        pass = false;
        try {
            d.getValue("R1", "XX");
        }
        catch (UnknownKeyException e) {
            pass = true;   
        }
        assertTrue(pass);
    }

// org.jfree.data.category.junit.DefaultCategoryDatasetTests::testGetValue2
    public void testGetValue2() {
        DefaultCategoryDataset d = new DefaultCategoryDataset();
        boolean pass = false;
        try {
             d.getValue(0, 0);
        }
        catch (IndexOutOfBoundsException e) {
            pass = true;
        }
        assertTrue(pass);
    }

// org.jfree.data.category.junit.DefaultCategoryDatasetTests::testIncrementValue
    public void testIncrementValue() {
        DefaultCategoryDataset d = new DefaultCategoryDataset();
        d.addValue(1.0, "R1", "C1");
        d.incrementValue(2.0, "R1", "C1");
        assertEquals(new Double(3.0), d.getValue("R1", "C1"));
        
        
        d.addValue(null, "R2", "C1");
        d.incrementValue(2.0, "R2", "C1");
        assertEquals(new Double(2.0), d.getValue("R2", "C1"));
        
        
        boolean pass = false;
        try {
            d.incrementValue(1.0, "XX", "C1");
        }
        catch (UnknownKeyException e) {
            pass = true;   
        }
        assertTrue(pass);
        
        
        pass = false;
        try {
            d.incrementValue(1.0, "R1", "XX");
        }
        catch (UnknownKeyException e) {
            pass = true;   
        }
        assertTrue(pass); 
    }

// org.jfree.data.category.junit.DefaultCategoryDatasetTests::testGetRowCount
    public void testGetRowCount() {
        DefaultCategoryDataset d = new DefaultCategoryDataset();
        assertTrue(d.getRowCount() == 0);
        
        d.addValue(1.0, "R1", "C1");
        assertTrue(d.getRowCount() == 1);
        
        d.addValue(1.0, "R2", "C1");
        assertTrue(d.getRowCount() == 2);
        
        d.addValue(2.0, "R2", "C1");
        assertTrue(d.getRowCount() == 2);
        
        
        d.setValue(null, "R2", "C1");
        assertTrue(d.getRowCount() == 2);
    }

// org.jfree.data.category.junit.DefaultCategoryDatasetTests::testGetColumnCount
    public void testGetColumnCount() {
        DefaultCategoryDataset d = new DefaultCategoryDataset();
        assertTrue(d.getColumnCount() == 0);
        
        d.addValue(1.0, "R1", "C1");
        assertTrue(d.getColumnCount() == 1);
        
        d.addValue(1.0, "R1", "C2");
        assertTrue(d.getColumnCount() == 2);
        
        d.addValue(2.0, "R1", "C2");
        assertTrue(d.getColumnCount() == 2);
        
        
        d.setValue(null, "R1", "C2");
        assertTrue(d.getColumnCount() == 2);
    }

// org.jfree.data.category.junit.DefaultCategoryDatasetTests::testEquals
    public void testEquals() {
        DefaultCategoryDataset d1 = new DefaultCategoryDataset();
        d1.setValue(23.4, "R1", "C1");
        DefaultCategoryDataset d2 = new DefaultCategoryDataset();
        d2.setValue(23.4, "R1", "C1");
        assertTrue(d1.equals(d2));
        assertTrue(d2.equals(d1));

        d1.setValue(36.5, "R1", "C2");
        assertFalse(d1.equals(d2));
        d2.setValue(36.5, "R1", "C2");
        assertTrue(d1.equals(d2));

        d1.setValue(null, "R1", "C1");
        assertFalse(d1.equals(d2));
        d2.setValue(null, "R1", "C1");
        assertTrue(d1.equals(d2));
    }

// org.jfree.data.category.junit.DefaultCategoryDatasetTests::testSerialization
    public void testSerialization() {

        DefaultCategoryDataset d1 = new DefaultCategoryDataset();
        d1.setValue(23.4, "R1", "C1");
        DefaultCategoryDataset d2 = null;

        try {
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            ObjectOutput out = new ObjectOutputStream(buffer);
            out.writeObject(d1);
            out.close();

            ObjectInput in = new ObjectInputStream(
                    new ByteArrayInputStream(buffer.toByteArray()));
            d2 = (DefaultCategoryDataset) in.readObject();
            in.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        assertEquals(d1, d2);

    }

// org.jfree.data.category.junit.DefaultCategoryDatasetTests::testAddValue
    public void testAddValue() {
        DefaultCategoryDataset d1 = new DefaultCategoryDataset();
        d1.addValue(null, "R1", "C1");
        assertNull(d1.getValue("R1", "C1"));
        d1.addValue(new Double(1.0), "R2", "C1");
        assertEquals(new Double(1.0), d1.getValue("R2", "C1"));
        
        boolean pass = false;
        try {
            d1.addValue(new Double(1.1), null, "C2");
        }
        catch (IllegalArgumentException e) {
            pass = true;
        }
        assertTrue(pass);
    }

// org.jfree.data.category.junit.DefaultCategoryDatasetTests::testRemoveValue
    public void testRemoveValue() {
        DefaultCategoryDataset d = new DefaultCategoryDataset();
        d.removeValue("R1", "C1");
        d.addValue(new Double(1.0), "R1", "C1");
        d.removeValue("R1", "C1");
        assertEquals(0, d.getRowCount());
        assertEquals(0, d.getColumnCount());
        
        d.addValue(new Double(1.0), "R1", "C1");
        d.addValue(new Double(2.0), "R2", "C1");
        d.removeValue("R1", "C1");
        assertEquals(new Double(2.0), d.getValue(0, 0));
        
        boolean pass = false;
        try {
            d.removeValue(null, "C1");
        }
        catch (IllegalArgumentException e) {
            pass = true;
        }
        assertTrue(pass);

        pass = false;
        try {
            d.removeValue("R1", null);
        }
        catch (IllegalArgumentException e) {
            pass = true;
        }
        assertTrue(pass);
    }

// org.jfree.data.category.junit.DefaultCategoryDatasetTests::testCloning
    public void testCloning() {
        DefaultCategoryDataset d1 = new DefaultCategoryDataset();
        DefaultCategoryDataset d2 = null;
        try {
            d2 = (DefaultCategoryDataset) d1.clone();
        }
        catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        assertTrue(d1 != d2);
        assertTrue(d1.getClass() == d2.getClass());
        assertTrue(d1.equals(d2));
        
        
        d1.addValue(1.0, "R1", "C1");
        d1.addValue(2.0, "R1", "C2");
        try {
            d2 = (DefaultCategoryDataset) d1.clone();
        }
        catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        assertTrue(d1 != d2);
        assertTrue(d1.getClass() == d2.getClass());
        assertTrue(d1.equals(d2));
        
        
        d1.addValue(3.0, "R1", "C1");
        assertFalse(d1.equals(d2));
        d2.addValue(3.0, "R1", "C1");
        assertTrue(d1.equals(d2));
    }

// org.jfree.data.category.junit.DefaultCategoryDatasetTests::testBug1835955
    public void testBug1835955() {
    	DefaultCategoryDataset d = new DefaultCategoryDataset();
    	d.addValue(1.0, "R1", "C1");
    	d.addValue(2.0, "R2", "C2");
    	d.removeColumn("C2");
    	d.addValue(3.0, "R2", "C2");
    	assertEquals(3.0, d.getValue("R2", "C2").doubleValue(), EPSILON);
    }

// org.jfree.data.general.junit.DatasetUtilitiesTests::testJava
    public void testJava() {
        assertTrue(Double.isNaN(Math.min(1.0, Double.NaN)));   
        assertTrue(Double.isNaN(Math.max(1.0, Double.NaN)));     
    }

// org.jfree.data.general.junit.DatasetUtilitiesTests::testCalculatePieDatasetTotal
    public void testCalculatePieDatasetTotal() {
        DefaultPieDataset d = new DefaultPieDataset();
        assertEquals(0.0, DatasetUtilities.calculatePieDatasetTotal(d), 
                EPSILON);
        d.setValue("A", 1.0);
        assertEquals(1.0, DatasetUtilities.calculatePieDatasetTotal(d), 
                EPSILON);
        d.setValue("B", 3.0);
        assertEquals(4.0, DatasetUtilities.calculatePieDatasetTotal(d), 
                EPSILON); 
    }

// org.jfree.data.general.junit.DatasetUtilitiesTests::testFindDomainBounds
    public void testFindDomainBounds() {
        XYDataset dataset = createXYDataset1();
        Range r = DatasetUtilities.findDomainBounds(dataset);
        assertEquals(1.0, r.getLowerBound(), EPSILON);
        assertEquals(3.0, r.getUpperBound(), EPSILON);
    }

// org.jfree.data.general.junit.DatasetUtilitiesTests::testIterateDomainBounds
    public void testIterateDomainBounds() {
        XYDataset dataset = createXYDataset1();
        Range r = DatasetUtilities.iterateDomainBounds(dataset);
        assertEquals(1.0, r.getLowerBound(), EPSILON);
        assertEquals(3.0, r.getUpperBound(), EPSILON);           
    }

// org.jfree.data.general.junit.DatasetUtilitiesTests::testFindRangeBounds1
    public void testFindRangeBounds1() {
        CategoryDataset dataset = createCategoryDataset1();
        Range r = DatasetUtilities.findRangeBounds(dataset);
        assertEquals(1.0, r.getLowerBound(), EPSILON);
        assertEquals(6.0, r.getUpperBound(), EPSILON);
    }

// org.jfree.data.general.junit.DatasetUtilitiesTests::testFindRangeBounds2
    public void testFindRangeBounds2() {
        XYDataset dataset = createXYDataset1();
        Range r = DatasetUtilities.findRangeBounds(dataset);
        assertEquals(100.0, r.getLowerBound(), EPSILON);
        assertEquals(105.0, r.getUpperBound(), EPSILON);
    }

// org.jfree.data.general.junit.DatasetUtilitiesTests::testIterateCategoryRangeBounds
    public void testIterateCategoryRangeBounds() {
        CategoryDataset dataset = createCategoryDataset1();
        Range r = DatasetUtilities.iterateCategoryRangeBounds(dataset, false);
        assertEquals(1.0, r.getLowerBound(), EPSILON);
        assertEquals(6.0, r.getUpperBound(), EPSILON);           
    }

// org.jfree.data.general.junit.DatasetUtilitiesTests::testIterateXYRangeBounds
    public void testIterateXYRangeBounds() {
        XYDataset dataset = createXYDataset1();
        Range r = DatasetUtilities.iterateXYRangeBounds(dataset);
        assertEquals(100.0, r.getLowerBound(), EPSILON);
        assertEquals(105.0, r.getUpperBound(), EPSILON);           
    }

// org.jfree.data.general.junit.DatasetUtilitiesTests::testIterateXYRangeBounds2
    public void testIterateXYRangeBounds2() {
        XYSeries s1 = new XYSeries("S1");
        s1.add(1.0, 1.1);
        s1.add(2.0, null);
        s1.add(3.0, 3.3);
        XYSeriesCollection dataset = new XYSeriesCollection(s1);
        Range r = DatasetUtilities.iterateXYRangeBounds(dataset);
        assertEquals(1.1, r.getLowerBound(), EPSILON);
        assertEquals(3.3, r.getUpperBound(), EPSILON);
    }

// org.jfree.data.general.junit.DatasetUtilitiesTests::testFindMinimumDomainValue
    public void testFindMinimumDomainValue() {
        XYDataset dataset = createXYDataset1();
        Number minimum = DatasetUtilities.findMinimumDomainValue(dataset);
        assertEquals(new Double(1.0), minimum);
    }

// org.jfree.data.general.junit.DatasetUtilitiesTests::testFindMaximumDomainValue
    public void testFindMaximumDomainValue() {
        XYDataset dataset = createXYDataset1();
        Number maximum = DatasetUtilities.findMaximumDomainValue(dataset);
        assertEquals(new Double(3.0), maximum);
    }

// org.jfree.data.general.junit.DatasetUtilitiesTests::testFindMinimumRangeValue
    public void testFindMinimumRangeValue() {
        CategoryDataset d1 = createCategoryDataset1();
        Number min1 = DatasetUtilities.findMinimumRangeValue(d1);
        assertEquals(new Double(1.0), min1);
        
        XYDataset d2 = createXYDataset1();
        Number min2 = DatasetUtilities.findMinimumRangeValue(d2);
        assertEquals(new Double(100.0), min2);        
    }

// org.jfree.data.general.junit.DatasetUtilitiesTests::testFindMaximumRangeValue
    public void testFindMaximumRangeValue() {
        CategoryDataset d1 = createCategoryDataset1();
        Number max1 = DatasetUtilities.findMaximumRangeValue(d1);
        assertEquals(new Double(6.0), max1);

        XYDataset dataset = createXYDataset1();
        Number maximum = DatasetUtilities.findMaximumRangeValue(dataset);
        assertEquals(new Double(105.0), maximum);
    }

// org.jfree.data.general.junit.DatasetUtilitiesTests::testMinMaxRange
    public void testMinMaxRange() {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        dataset.addValue(100.0, "Series 1", "Type 1");
        dataset.addValue(101.1, "Series 1", "Type 2");
        Number min = DatasetUtilities.findMinimumRangeValue(dataset);
        assertTrue(min.doubleValue() < 100.1);
        Number max = DatasetUtilities.findMaximumRangeValue(dataset);
        assertTrue(max.doubleValue() > 101.0);
    }

// org.jfree.data.general.junit.DatasetUtilitiesTests::test803660
    public void test803660() {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        dataset.addValue(100.0, "Series 1", "Type 1");
        dataset.addValue(101.1, "Series 1", "Type 2");
        Number n = DatasetUtilities.findMaximumRangeValue(dataset);
        assertTrue(n.doubleValue() > 101.0);
    }

// org.jfree.data.general.junit.DatasetUtilitiesTests::testCumulativeRange1
    public void testCumulativeRange1() {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        dataset.addValue(10.0, "Series 1", "Start");
        dataset.addValue(15.0, "Series 1", "Delta 1");
        dataset.addValue(-7.0, "Series 1", "Delta 2");
        Range range = DatasetUtilities.findCumulativeRangeBounds(dataset);
        assertEquals(0.0, range.getLowerBound(), 0.00000001);
        assertEquals(25.0, range.getUpperBound(), 0.00000001);
    }

// org.jfree.data.general.junit.DatasetUtilitiesTests::testCumulativeRange2
    public void testCumulativeRange2() {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        dataset.addValue(-21.4, "Series 1", "Start Value");
        dataset.addValue(11.57, "Series 1", "Delta 1");
        dataset.addValue(3.51, "Series 1", "Delta 2");
        dataset.addValue(-12.36, "Series 1", "Delta 3");
        dataset.addValue(3.39, "Series 1", "Delta 4");
        dataset.addValue(38.68, "Series 1", "Delta 5");
        dataset.addValue(-43.31, "Series 1", "Delta 6");
        dataset.addValue(-29.59, "Series 1", "Delta 7");
        dataset.addValue(35.30, "Series 1", "Delta 8");
        dataset.addValue(5.0, "Series 1", "Delta 9");
        Range range = DatasetUtilities.findCumulativeRangeBounds(dataset);
        assertEquals(-49.51, range.getLowerBound(), 0.00000001);
        assertEquals(23.39, range.getUpperBound(), 0.00000001);
    }

// org.jfree.data.general.junit.DatasetUtilitiesTests::testCreateCategoryDataset1
    public void testCreateCategoryDataset1() {
        String[] rowKeys = {"R1", "R2", "R3"};
        String[] columnKeys = {"C1", "C2"};
        double[][] data = new double[3][];
        data[0] = new double[] {1.1, 1.2};
        data[1] = new double[] {2.1, 2.2};
        data[2] = new double[] {3.1, 3.2};
        CategoryDataset dataset = DatasetUtilities.createCategoryDataset(
                rowKeys, columnKeys, data);
        assertTrue(dataset.getRowCount() == 3);
        assertTrue(dataset.getColumnCount() == 2);
    }

// org.jfree.data.general.junit.DatasetUtilitiesTests::testCreateCategoryDataset2
    public void testCreateCategoryDataset2() {
        boolean pass = false;
        String[] rowKeys = {"R1", "R2", "R3"};
        String[] columnKeys = {"C1", "C2"};
        double[][] data = new double[2][];
        data[0] = new double[] {1.1, 1.2, 1.3};
        data[1] = new double[] {2.1, 2.2, 2.3};
        CategoryDataset dataset = null;
        try {
            dataset = DatasetUtilities.createCategoryDataset(rowKeys, 
                    columnKeys, data);
        }
        catch (IllegalArgumentException e) {
            pass = true;  
        }
        assertTrue(pass);
        assertTrue(dataset == null);
    }

// org.jfree.data.general.junit.DatasetUtilitiesTests::testMaximumStackedRangeValue
    public void testMaximumStackedRangeValue() {
        double v1 = 24.3;
        double v2 = 14.2;
        double v3 = 33.2;
        double v4 = 32.4;
        double v5 = 26.3;
        double v6 = 22.6;
        Number answer = new Double(Math.max(v1 + v2 + v3, v4 + v5 + v6));
        DefaultCategoryDataset d = new DefaultCategoryDataset();
        d.addValue(v1, "Row 0", "Column 0");
        d.addValue(v2, "Row 1", "Column 0");
        d.addValue(v3, "Row 2", "Column 0");
        d.addValue(v4, "Row 0", "Column 1");
        d.addValue(v5, "Row 1", "Column 1");
        d.addValue(v6, "Row 2", "Column 1");
        Number max = DatasetUtilities.findMaximumStackedRangeValue(d);
        assertTrue(max.equals(answer));
    }

// org.jfree.data.general.junit.DatasetUtilitiesTests::testFindStackedRangeBoundsForCategoryDataset1
    public void testFindStackedRangeBoundsForCategoryDataset1() {
        CategoryDataset d1 = createCategoryDataset1();
        Range r = DatasetUtilities.findStackedRangeBounds(d1);
        assertEquals(0.0, r.getLowerBound(), EPSILON);
        assertEquals(15.0, r.getUpperBound(), EPSILON);
        
        d1 = createCategoryDataset2();
        r = DatasetUtilities.findStackedRangeBounds(d1);
        assertEquals(-2.0, r.getLowerBound(), EPSILON);
        assertEquals(2.0, r.getUpperBound(), EPSILON);     
    }

// org.jfree.data.general.junit.DatasetUtilitiesTests::testFindStackedRangeBoundsForCategoryDataset2
    public void testFindStackedRangeBoundsForCategoryDataset2() {
        CategoryDataset d1 = new DefaultCategoryDataset();
        Range r = DatasetUtilities.findStackedRangeBounds(d1);
        assertTrue(r == null);     
    }

// org.jfree.data.general.junit.DatasetUtilitiesTests::testFindStackedRangeBoundsForTableXYDataset1
    public void testFindStackedRangeBoundsForTableXYDataset1() {
        TableXYDataset d2 = createTableXYDataset1();
        Range r = DatasetUtilities.findStackedRangeBounds(d2);
        assertEquals(-2.0, r.getLowerBound(), EPSILON);
        assertEquals(2.0, r.getUpperBound(), EPSILON);        
    }

// org.jfree.data.general.junit.DatasetUtilitiesTests::testFindStackedRangeBoundsForTableXYDataset2
    public void testFindStackedRangeBoundsForTableXYDataset2() {
        DefaultTableXYDataset d = new DefaultTableXYDataset();
        Range r = DatasetUtilities.findStackedRangeBounds(d);
        assertEquals(r, new Range(0.0, 0.0));
    }

// org.jfree.data.general.junit.DatasetUtilitiesTests::testStackedRangeWithMap
    public void testStackedRangeWithMap() {
        CategoryDataset d = createCategoryDataset1();
        KeyToGroupMap map = new KeyToGroupMap("G0");
        map.mapKeyToGroup("R2", "G1");
        Range r = DatasetUtilities.findStackedRangeBounds(d, map);
        assertEquals(0.0, r.getLowerBound(), EPSILON);
        assertEquals(9.0, r.getUpperBound(), EPSILON);        
    }

// org.jfree.data.general.junit.DatasetUtilitiesTests::testIsEmptyOrNullXYDataset
    public void testIsEmptyOrNullXYDataset() {
        XYSeriesCollection dataset = null;
        assertTrue(DatasetUtilities.isEmptyOrNull(dataset));
        dataset = new XYSeriesCollection();
        assertTrue(DatasetUtilities.isEmptyOrNull(dataset));
        XYSeries s1 = new XYSeries("S1");
        dataset.addSeries(s1);
        assertTrue(DatasetUtilities.isEmptyOrNull(dataset));
        s1.add(1.0, 2.0);
        assertFalse(DatasetUtilities.isEmptyOrNull(dataset));
        s1.clear();
        assertTrue(DatasetUtilities.isEmptyOrNull(dataset));
    }

// org.jfree.data.general.junit.DatasetUtilitiesTests::testLimitPieDataset
    public void testLimitPieDataset() {
        
        
        DefaultPieDataset d1 = new DefaultPieDataset();
        PieDataset d2 = DatasetUtilities.createConsolidatedPieDataset(d1, 
                "Other", 0.05);
        assertEquals(0, d2.getItemCount());
        
        
        d1.setValue("Item 1", 1.0);
        d1.setValue("Item 2", 49.50);
        d1.setValue("Item 3", 49.50);
        d2 = DatasetUtilities.createConsolidatedPieDataset(d1, "Other", 0.05);
        assertEquals(3, d2.getItemCount());
        assertEquals("Item 1", d2.getKey(0));
        assertEquals("Item 2", d2.getKey(1));
        assertEquals("Item 3", d2.getKey(2));

        
        d1.setValue("Item 4", 1.0);
        d2 = DatasetUtilities.createConsolidatedPieDataset(d1, "Other", 0.05, 
                2);
        
        
        assertEquals(3, d2.getItemCount());
        assertEquals("Item 2", d2.getKey(0));
        assertEquals("Item 3", d2.getKey(1));
        assertEquals("Other", d2.getKey(2));
        assertEquals(new Double(2.0), d2.getValue("Other"));
        
    }

// org.jfree.data.general.junit.DefaultKeyedValues2DDatasetTests::testCloning
    public void testCloning() {
        DefaultKeyedValues2DDataset d1 = new DefaultKeyedValues2DDataset();
        d1.setValue(new Integer(1), "V1", "C1");
        d1.setValue(null, "V2", "C1");
        d1.setValue(new Integer(3), "V3", "C2");
        DefaultKeyedValues2DDataset d2 = null;
        try {
            d2 = (DefaultKeyedValues2DDataset) d1.clone();
        }
        catch (CloneNotSupportedException e) {
            System.err.println("Failed to clone.");
        }
        assertTrue(d1 != d2);
        assertTrue(d1.getClass() == d2.getClass());
        assertTrue(d1.equals(d2));
    }

// org.jfree.data.general.junit.DefaultKeyedValues2DDatasetTests::testSerialization
    public void testSerialization() {

        DefaultKeyedValues2DDataset d1 = new DefaultKeyedValues2DDataset();
        d1.addValue(new Double(234.2), "Row1", "Col1");
        d1.addValue(null, "Row1", "Col2");
        d1.addValue(new Double(345.9), "Row2", "Col1");
        d1.addValue(new Double(452.7), "Row2", "Col2");

        DefaultKeyedValues2DDataset d2 = null;

        try {
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            ObjectOutput out = new ObjectOutputStream(buffer);
            out.writeObject(d1);
            out.close();

            ObjectInput in = new ObjectInputStream(
                new ByteArrayInputStream(buffer.toByteArray())
            );
            d2 = (DefaultKeyedValues2DDataset) in.readObject();
            in.close();
        }
        catch (Exception e) {
            System.out.println(e.toString());
        }
        assertEquals(d1, d2);

    }

// org.jfree.data.general.junit.DefaultKeyedValuesDatasetTests::testCloning
    public void testCloning() {
        DefaultKeyedValuesDataset d1 = new DefaultKeyedValuesDataset();
        d1.setValue("V1", new Integer(1));
        d1.setValue("V2", null);
        d1.setValue("V3", new Integer(3));
        DefaultKeyedValuesDataset d2 = null;
        try {
            d2 = (DefaultKeyedValuesDataset) d1.clone();
        }
        catch (CloneNotSupportedException e) {
            System.err.println("Failed to clone.");
        }
        assertTrue(d1 != d2);
        assertTrue(d1.getClass() == d2.getClass());
        assertTrue(d1.equals(d2));
    }

// org.jfree.data.general.junit.DefaultKeyedValuesDatasetTests::testSerialization
    public void testSerialization() {

        DefaultKeyedValuesDataset d1 = new DefaultKeyedValuesDataset();
        d1.setValue("C1", new Double(234.2));
        d1.setValue("C2", null);
        d1.setValue("C3", new Double(345.9));
        d1.setValue("C4", new Double(452.7));

        KeyedValuesDataset d2 = null;

        try {
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            ObjectOutput out = new ObjectOutputStream(buffer);
            out.writeObject(d1);
            out.close();

            ObjectInput in = new ObjectInputStream(
                new ByteArrayInputStream(buffer.toByteArray())
            );
            d2 = (KeyedValuesDataset) in.readObject();
            in.close();
        }
        catch (Exception e) {
            System.out.println(e.toString());
        }
        assertEquals(d1, d2);

    }

// org.jfree.data.general.junit.DefaultPieDatasetTests::testClear
    public void testClear() {
        DefaultPieDataset d = new DefaultPieDataset();
        d.addChangeListener(this);
        
        d.clear();
        assertNull(this.lastEvent);
        d.setValue("A", 1.0);
        assertEquals(1, d.getItemCount());
        this.lastEvent = null;
        d.clear();
        assertNotNull(this.lastEvent);
        assertEquals(0, d.getItemCount());
    }

// org.jfree.data.general.junit.DefaultPieDatasetTests::testGetKey
    public void testGetKey() {
        DefaultPieDataset d = new DefaultPieDataset();
        d.setValue("A", 1.0);
        d.setValue("B", 2.0);
        assertEquals("A", d.getKey(0));
        assertEquals("B", d.getKey(1));
        
        boolean pass = false;
        try {
            d.getKey(-1);
        }
        catch (IndexOutOfBoundsException e) {
            pass = true;
        }
        assertTrue(pass);
        
        pass = false;
        try {
            d.getKey(2);
        }
        catch (IndexOutOfBoundsException e) {
            pass = true;
        }
        assertTrue(pass);
    }

// org.jfree.data.general.junit.DefaultPieDatasetTests::testGetIndex
    public void testGetIndex() {
        DefaultPieDataset d = new DefaultPieDataset();
        d.setValue("A", 1.0);
        d.setValue("B", 2.0);
        assertEquals(0, d.getIndex("A"));
        assertEquals(1, d.getIndex("B"));
        assertEquals(-1, d.getIndex("XX"));
        
        boolean pass = false;
        try {
            d.getIndex(null);
        }
        catch (IllegalArgumentException e) {
            pass = true;
        }
        assertTrue(pass);
    }

// org.jfree.data.general.junit.DefaultPieDatasetTests::testCloning
    public void testCloning() {
        DefaultPieDataset d1 = new DefaultPieDataset();
        d1.setValue("V1", new Integer(1));
        d1.setValue("V2", null);
        d1.setValue("V3", new Integer(3));
        DefaultPieDataset d2 = null;
        try {
            d2 = (DefaultPieDataset) d1.clone();
        }
        catch (CloneNotSupportedException e) {
            System.err.println("Failed to clone.");
        }
        assertTrue(d1 != d2);
        assertTrue(d1.getClass() == d2.getClass());
        assertTrue(d1.equals(d2));
    }

// org.jfree.data.general.junit.DefaultPieDatasetTests::testSerialization
    public void testSerialization() {

        DefaultPieDataset d1 = new DefaultPieDataset();
        d1.setValue("C1", new Double(234.2));
        d1.setValue("C2", null);
        d1.setValue("C3", new Double(345.9));
        d1.setValue("C4", new Double(452.7));

        DefaultPieDataset d2 = null;

        try {
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            ObjectOutput out = new ObjectOutputStream(buffer);
            out.writeObject(d1);
            out.close();

            ObjectInput in = new ObjectInputStream(
                new ByteArrayInputStream(buffer.toByteArray())
            );
            d2 = (DefaultPieDataset) in.readObject();
            in.close();
        }
        catch (Exception e) {
            System.out.println(e.toString());
        }
        assertEquals(d1, d2);

    }

// org.jfree.data.junit.DataUtilitiesTests::testCreateNumberArray2D
    public void testCreateNumberArray2D() {
        double[][] d = new double[2][];
        d[0] = new double[] {1.1, 2.2, 3.3, 4.4};
        d[1] = new double[] {1.1, 2.2, 3.3, 4.4, 5.5};
        Number[][] n = DataUtilities.createNumberArray2D(d);
        assertEquals(2, n.length);
        assertEquals(4, n[0].length);
        assertEquals(5, n[1].length);
    }

// org.jfree.data.junit.DataUtilitiesTests::testCalculateColumnTotal
    public void testCalculateColumnTotal() {
        DefaultKeyedValues2D table = new DefaultKeyedValues2D();
        table.addValue(new Double(1.0), "R0", "C0");
        table.addValue(new Double(2.0), "R0", "C1");
        table.addValue(new Double(3.0), "R1", "C0");
        table.addValue(new Double(4.0), "R1", "C1");
        assertEquals(4.0, DataUtilities.calculateColumnTotal(table, 0), EPSILON);
        assertEquals(6.0, DataUtilities.calculateColumnTotal(table, 1), EPSILON);
        table.setValue(null, "R1", "C1");
        assertEquals(2.0, DataUtilities.calculateColumnTotal(table, 1), EPSILON);
    }

// org.jfree.data.junit.DataUtilitiesTests::testCalculateRowTotal
    public void testCalculateRowTotal() {
        DefaultKeyedValues2D table = new DefaultKeyedValues2D();
        table.addValue(new Double(1.0), "R0", "C0");
        table.addValue(new Double(2.0), "R0", "C1");
        table.addValue(new Double(3.0), "R1", "C0");
        table.addValue(new Double(4.0), "R1", "C1");
        assertEquals(3.0, DataUtilities.calculateRowTotal(table, 0), EPSILON);
        assertEquals(7.0, DataUtilities.calculateRowTotal(table, 1), EPSILON);
        table.setValue(null, "R1", "C1");
        assertEquals(3.0, DataUtilities.calculateRowTotal(table, 1), EPSILON);
    }

// org.jfree.data.junit.DefaultKeyedValues2DTests::testGetValue
    public void testGetValue() {
        DefaultKeyedValues2D d = new DefaultKeyedValues2D();
        d.addValue(new Double(1.0), "R1", "C1");
        assertEquals(new Double(1.0), d.getValue("R1", "C1"));
        boolean pass = false;
        try {
            d.getValue("XX", "C1");
        }
        catch (UnknownKeyException e) {
            pass = true;   
        }
        assertTrue(pass);
        
        pass = false;
        try {
            d.getValue("R1", "XX");
        }
        catch (UnknownKeyException e) {
            pass = true;   
        }
        assertTrue(pass);
    }

// org.jfree.data.junit.DefaultKeyedValues2DTests::testCloning
    public void testCloning() {
        DefaultKeyedValues2D v1 = new DefaultKeyedValues2D();
        v1.setValue(new Integer(1), "V1", "C1");
        v1.setValue(null, "V2", "C1");
        v1.setValue(new Integer(3), "V3", "C2");
        DefaultKeyedValues2D v2 = null;
        try {
            v2 = (DefaultKeyedValues2D) v1.clone();
        }
        catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        assertTrue(v1 != v2);
        assertTrue(v1.getClass() == v2.getClass());
        assertTrue(v1.equals(v2));
        
        
        v2.setValue(new Integer(2), "V2", "C1");
        assertFalse(v1.equals(v2));
    }

// org.jfree.data.junit.DefaultKeyedValues2DTests::testSerialization
    public void testSerialization() {

        DefaultKeyedValues2D kv2D1 = new DefaultKeyedValues2D();
        kv2D1.addValue(new Double(234.2), "Row1", "Col1");
        kv2D1.addValue(null, "Row1", "Col2");
        kv2D1.addValue(new Double(345.9), "Row2", "Col1");
        kv2D1.addValue(new Double(452.7), "Row2", "Col2");

        DefaultKeyedValues2D kv2D2 = null;

        try {
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            ObjectOutput out = new ObjectOutputStream(buffer);
            out.writeObject(kv2D1);
            out.close();

            ObjectInput in = new ObjectInputStream(
                    new ByteArrayInputStream(buffer.toByteArray()));
            kv2D2 = (DefaultKeyedValues2D) in.readObject();
            in.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        assertEquals(kv2D1, kv2D2);

    }

// org.jfree.data.junit.DefaultKeyedValues2DTests::testEquals
    public void testEquals() {
        DefaultKeyedValues2D d1 = new DefaultKeyedValues2D();
        DefaultKeyedValues2D d2 = new DefaultKeyedValues2D();
        assertTrue(d1.equals(d2));
        assertTrue(d2.equals(d1));
        
        d1.addValue(new Double(1.0), new Double(2.0), "S1");
        assertFalse(d1.equals(d2));
        d2.addValue(new Double(1.0), new Double(2.0), "S1");
        assertTrue(d1.equals(d2));
    }

// org.jfree.data.junit.DefaultKeyedValues2DTests::testSparsePopulation
    public void testSparsePopulation() {
        DefaultKeyedValues2D d = new DefaultKeyedValues2D();
        d.addValue(new Integer(11), "R1", "C1");
        d.addValue(new Integer(22), "R2", "C2");
        
        assertEquals(new Integer(11), d.getValue("R1", "C1"));
        assertNull(d.getValue("R1", "C2"));
        assertEquals(new Integer(22), d.getValue("R2", "C2"));
        assertNull(d.getValue("R2", "C1"));
    }

// org.jfree.data.junit.DefaultKeyedValues2DTests::testRowCount
    public void testRowCount() {
        DefaultKeyedValues2D d = new DefaultKeyedValues2D();
        assertEquals(0, d.getRowCount());
        d.addValue(new Double(1.0), "R1", "C1");
        assertEquals(1, d.getRowCount());
        d.addValue(new Double(2.0), "R2", "C1");
        assertEquals(2, d.getRowCount());
    }

// org.jfree.data.junit.DefaultKeyedValues2DTests::testColumnCount
    public void testColumnCount() {
        DefaultKeyedValues2D d = new DefaultKeyedValues2D();
        assertEquals(0, d.getColumnCount());
        d.addValue(new Double(1.0), "R1", "C1");
        assertEquals(1, d.getColumnCount());
        d.addValue(new Double(2.0), "R1", "C2");
        assertEquals(2, d.getColumnCount());
    }

// org.jfree.data.junit.DefaultKeyedValues2DTests::testGetValue2
    public void testGetValue2() {
        DefaultKeyedValues2D d = new DefaultKeyedValues2D();
        boolean pass = false;
        try {
            d.getValue(0, 0);
        }
        catch (IndexOutOfBoundsException e) {
            pass = true;
        }
        assertTrue(pass);
        d.addValue(new Double(1.0), "R1", "C1");
        assertEquals(1.0, d.getValue(0, 0).doubleValue(), EPSILON);
        d.addValue(new Double(2.0), "R2", "C2");
        assertEquals(2.0, d.getValue(1, 1).doubleValue(), EPSILON);
        assertNull(d.getValue(1, 0));
        assertNull(d.getValue(0, 1));
        
        pass = false;
        try {
            d.getValue(2, 0);
        }
        catch (IndexOutOfBoundsException e) {
            pass = true;
        }
        assertTrue(pass);
    }

// org.jfree.data.junit.DefaultKeyedValues2DTests::testGetRowKey
    public void testGetRowKey() {
        DefaultKeyedValues2D d = new DefaultKeyedValues2D();
        boolean pass = false;
        try {
            d.getRowKey(0);
        }
        catch (IndexOutOfBoundsException e) {
            pass = true;
        }
        assertTrue(pass);
        d.addValue(new Double(1.0), "R1", "C1");
        d.addValue(new Double(1.0), "R2", "C1");
        assertEquals("R1", d.getRowKey(0));
        assertEquals("R2", d.getRowKey(1));
        
        
        d = new DefaultKeyedValues2D(true);
        d.addValue(new Double(1.0), "R1", "C1");
        assertEquals("R1", d.getRowKey(0));
        d.addValue(new Double(0.0), "R0", "C1");
        assertEquals("R0", d.getRowKey(0));
        assertEquals("R1", d.getRowKey(1));
    }

// org.jfree.data.junit.DefaultKeyedValues2DTests::testGetColumnKey
    public void testGetColumnKey() {
        DefaultKeyedValues2D d = new DefaultKeyedValues2D();
        boolean pass = false;
        try {
            d.getColumnKey(0);
        }
        catch (IndexOutOfBoundsException e) {
            pass = true;
        }
        assertTrue(pass);
        d.addValue(new Double(1.0), "R1", "C1");
        d.addValue(new Double(1.0), "R1", "C2");
        assertEquals("C1", d.getColumnKey(0));
        assertEquals("C2", d.getColumnKey(1));
    }

// org.jfree.data.junit.DefaultKeyedValues2DTests::testRemoveValue
    public void testRemoveValue() {
        DefaultKeyedValues2D d = new DefaultKeyedValues2D();
        d.removeValue("R1", "C1");
        d.addValue(new Double(1.0), "R1", "C1");
        d.removeValue("R1", "C1");
        assertEquals(0, d.getRowCount());
        assertEquals(0, d.getColumnCount());
        
        d.addValue(new Double(1.0), "R1", "C1");
        d.addValue(new Double(2.0), "R2", "C1");
        d.removeValue("R1", "C1");
        assertEquals(new Double(2.0), d.getValue(0, 0));
    }

// org.jfree.data.junit.DefaultKeyedValues2DTests::testRemoveValueBug1690654
    public void testRemoveValueBug1690654() {
        DefaultKeyedValues2D d = new DefaultKeyedValues2D();
        d.addValue(new Double(1.0), "R1", "C1");
        d.addValue(new Double(2.0), "R2", "C2");
        assertEquals(2, d.getColumnCount());
        assertEquals(2, d.getRowCount());
        d.removeValue("R2", "C2");
        assertEquals(1, d.getColumnCount());
        assertEquals(1, d.getRowCount());
        assertEquals(new Double(1.0), d.getValue(0, 0));
    }

// org.jfree.data.junit.DefaultKeyedValues2DTests::testRemoveRow
    public void testRemoveRow() {
        DefaultKeyedValues2D d = new DefaultKeyedValues2D();
        boolean pass = false;
        try {
            d.removeRow(0);
        }
        catch (IndexOutOfBoundsException e) {
            pass = true;
        }
        assertTrue(pass);        
    }

// org.jfree.data.junit.DefaultKeyedValues2DTests::testRemoveColumnByKey
    public void testRemoveColumnByKey() {
    	DefaultKeyedValues2D d = new DefaultKeyedValues2D();
    	d.addValue(new Double(1.0), "R1", "C1");
    	d.addValue(new Double(2.0), "R2", "C2");
    	d.removeColumn("C2");
    	d.addValue(new Double(3.0), "R2", "C2");
    	assertEquals(3.0, d.getValue("R2", "C2").doubleValue(), EPSILON);
    	
    	
    	boolean pass = false;
    	try {
    		d.removeColumn("XXX");
    	}
    	catch (UnknownKeyException e) {
    		pass = true;
    	}
    	assertTrue(pass);
    }

// org.jfree.data.junit.DefaultKeyedValuesTests::testConstructor
    public void testConstructor() {
        DefaultKeyedValues d = new DefaultKeyedValues();
        assertEquals(0, d.getItemCount());
    }

// org.jfree.data.junit.DefaultKeyedValuesTests::testGetItemCount
    public void testGetItemCount() {
        DefaultKeyedValues d = new DefaultKeyedValues();
        assertEquals(0, d.getItemCount());
        d.addValue("A", 1.0);
        assertEquals(1, d.getItemCount());
        d.addValue("B", 2.0);
        assertEquals(2, d.getItemCount());
        d.clear();
        assertEquals(0, d.getItemCount());        
    }

// org.jfree.data.junit.DefaultKeyedValuesTests::testGetKeys
    public void testGetKeys() {
        DefaultKeyedValues d = new DefaultKeyedValues();
        List keys = d.getKeys();
        assertTrue(keys.isEmpty());
        d.addValue("A", 1.0);
        keys = d.getKeys();
        assertEquals(1, keys.size());
        assertTrue(keys.contains("A"));
        d.addValue("B", 2.0);
        keys = d.getKeys();
        assertEquals(2, keys.size());
        assertTrue(keys.contains("A"));
        assertTrue(keys.contains("B"));
        d.clear();
        keys = d.getKeys();
        assertEquals(0, keys.size());        
    }

// org.jfree.data.junit.DefaultKeyedValuesTests::testClear
    public void testClear() {
        DefaultKeyedValues v1 = new DefaultKeyedValues();
        v1.addValue("A", 1.0);
        v1.addValue("B", 2.0);
        assertEquals(2, v1.getItemCount());
        v1.clear();
        assertEquals(0, v1.getItemCount());
    }

// org.jfree.data.junit.DefaultKeyedValuesTests::testGetValue
    public void testGetValue() {
        DefaultKeyedValues v1 = new DefaultKeyedValues();
        try {
             v1.getValue(-1);
            assertTrue(false);
        }
        catch (IndexOutOfBoundsException e) {
            
        }
        try {
             v1.getValue(0);
            assertTrue(false);
        }
        catch (IndexOutOfBoundsException e) {
            
        }
        DefaultKeyedValues v2 = new DefaultKeyedValues();
        v2.addValue("K1", new Integer(1));
        v2.addValue("K2", new Integer(2));
        v2.addValue("K3", new Integer(3));
        assertEquals(new Integer(3), v2.getValue(2));
        
        boolean pass = false;
        try {
             v2.getValue("KK");
        }
        catch (UnknownKeyException e) {
            pass = true;
        }
        assertTrue(pass);
    }

// org.jfree.data.junit.DefaultKeyedValuesTests::testGetKey
    public void testGetKey() {
        DefaultKeyedValues v1 = new DefaultKeyedValues();
        try {
             v1.getKey(-1);
            assertTrue(false);
        }
        catch (IndexOutOfBoundsException e) {
            
        }
        try {
             v1.getKey(0);
            assertTrue(false);
        }
        catch (IndexOutOfBoundsException e) {
            
        }
        DefaultKeyedValues v2 = new DefaultKeyedValues();
        v2.addValue("K1", new Integer(1));
        v2.addValue("K2", new Integer(2));
        v2.addValue("K3", new Integer(3));
        assertEquals("K2", v2.getKey(1));
    }

// org.jfree.data.junit.DefaultKeyedValuesTests::testGetIndex
    public void testGetIndex() {
        DefaultKeyedValues v1 = new DefaultKeyedValues();
        assertEquals(-1, v1.getIndex("K1"));

        DefaultKeyedValues v2 = new DefaultKeyedValues();
        v2.addValue("K1", new Integer(1));
        v2.addValue("K2", new Integer(2));
        v2.addValue("K3", new Integer(3));
        assertEquals(2, v2.getIndex("K3"));
        
        
        boolean pass = false;
        try {
            v2.getIndex(null);
        }
        catch (IllegalArgumentException e) {
            pass = true;
        }
        assertTrue(pass);
    }

// org.jfree.data.junit.DefaultKeyedValuesTests::testGetIndex2
    public void testGetIndex2() {
    	DefaultKeyedValues v = new DefaultKeyedValues();
    	assertEquals(-1, v.getIndex("K1"));
    	v.addValue("K1", 1.0);
    	assertEquals(0, v.getIndex("K1"));
    	v.removeValue("K1");
    	assertEquals(-1, v.getIndex("K1"));
    }

// org.jfree.data.junit.DefaultKeyedValuesTests::testAddValue
    public void testAddValue() {
        DefaultKeyedValues v1 = new DefaultKeyedValues();
        v1.addValue("A", 1.0);
        assertEquals(new Double(1.0), v1.getValue("A"));
        v1.addValue("B", 2.0);
        assertEquals(new Double(2.0), v1.getValue("B"));
        v1.addValue("B", 3.0);
        assertEquals(new Double(3.0), v1.getValue("B"));
        assertEquals(2, v1.getItemCount());
        v1.addValue("A", null);
        assertNull(v1.getValue("A"));
        assertEquals(2, v1.getItemCount());
        
        boolean pass = false;
        try {
            v1.addValue(null, 99.9);
        }
        catch (IllegalArgumentException e) {
            pass = true;
        }
        assertTrue(pass);
    }

// org.jfree.data.junit.DefaultKeyedValuesTests::testInsertValue
    public void testInsertValue() {
        DefaultKeyedValues v1 = new DefaultKeyedValues();
        v1.insertValue(0, "A", 1.0);
        assertEquals(new Double(1.0), v1.getValue(0));
        v1.insertValue(0, "B", 2.0);
        assertEquals(new Double(2.0), v1.getValue(0));
        assertEquals(new Double(1.0), v1.getValue(1));
        
        
        v1.insertValue(2, "C", 3.0);
        assertEquals(new Double(2.0), v1.getValue(0));
        assertEquals(new Double(1.0), v1.getValue(1));
        assertEquals(new Double(3.0), v1.getValue(2));
        
        
        v1.insertValue(2, "B", 4.0);
        assertEquals(new Double(1.0), v1.getValue(0));
        assertEquals(new Double(3.0), v1.getValue(1));
        assertEquals(new Double(4.0), v1.getValue(2));
    }

// org.jfree.data.junit.DefaultKeyedValuesTests::testCloning
    public void testCloning() {
        DefaultKeyedValues v1 = new DefaultKeyedValues();
        v1.addValue("V1", new Integer(1));
        v1.addValue("V2", null);
        v1.addValue("V3", new Integer(3));
        DefaultKeyedValues v2 = null;
        try {
            v2 = (DefaultKeyedValues) v1.clone();
        }
        catch (CloneNotSupportedException e) {
            System.err.println("Failed to clone.");
        }
        assertTrue(v1 != v2);
        assertTrue(v1.getClass() == v2.getClass());
        assertTrue(v1.equals(v2));
        
        
        v2.setValue("V1", new Integer(44));
        assertFalse(v1.equals(v2));
    }

// org.jfree.data.junit.DefaultKeyedValuesTests::testInsertAndRetrieve
    public void testInsertAndRetrieve() {

        DefaultKeyedValues data = new DefaultKeyedValues();
        data.addValue("A", new Double(1.0));
        data.addValue("B", new Double(2.0));
        data.addValue("C", new Double(3.0));
        data.addValue("D", null);

        
        assertEquals(data.getKey(0), "A");
        assertEquals(data.getKey(1), "B");
        assertEquals(data.getKey(2), "C");
        assertEquals(data.getKey(3), "D");

        
        assertEquals(data.getValue("A"), new Double(1.0));
        assertEquals(data.getValue("B"), new Double(2.0));
        assertEquals(data.getValue("C"), new Double(3.0));
        assertEquals(data.getValue("D"), null);

        
        assertEquals(data.getValue(0), new Double(1.0));
        assertEquals(data.getValue(1), new Double(2.0));
        assertEquals(data.getValue(2), new Double(3.0));
        assertEquals(data.getValue(3), null);

    }

// org.jfree.data.junit.DefaultKeyedValuesTests::testRemoveValue
    public void testRemoveValue() {
        DefaultKeyedValues data = new DefaultKeyedValues();
        data.addValue("A", new Double(1.0));
        data.addValue("B", null);
        data.addValue("C", new Double(3.0));
        data.addValue("D", new Double(2.0));
        assertEquals(1, data.getIndex("B"));
        data.removeValue("B");
        assertEquals(-1, data.getIndex("B"));
        
        boolean pass = false;
        try {
            data.removeValue("XXX");
        }
        catch (UnknownKeyException e) {
            pass = true;   
        }
        assertTrue(pass);
    }

// org.jfree.data.junit.DefaultKeyedValuesTests::testSortByKeyAscending
    public void testSortByKeyAscending() {

        DefaultKeyedValues data = new DefaultKeyedValues();
        data.addValue("C", new Double(1.0));
        data.addValue("B", null);
        data.addValue("D", new Double(3.0));
        data.addValue("A", new Double(2.0));

        data.sortByKeys(SortOrder.ASCENDING);

        
        assertEquals(data.getKey(0), "A");
        assertEquals(data.getKey(1), "B");
        assertEquals(data.getKey(2), "C");
        assertEquals(data.getKey(3), "D");

        
        assertEquals(data.getValue("A"), new Double(2.0));
        assertEquals(data.getValue("B"), null);
        assertEquals(data.getValue("C"), new Double(1.0));
        assertEquals(data.getValue("D"), new Double(3.0));

        
        assertEquals(data.getValue(0), new Double(2.0));
        assertEquals(data.getValue(1), null);
        assertEquals(data.getValue(2), new Double(1.0));
        assertEquals(data.getValue(3), new Double(3.0));

    }

// org.jfree.data.junit.DefaultKeyedValuesTests::testSortByKeyDescending
    public void testSortByKeyDescending() {

        DefaultKeyedValues data = new DefaultKeyedValues();
        data.addValue("C", new Double(1.0));
        data.addValue("B", null);
        data.addValue("D", new Double(3.0));
        data.addValue("A", new Double(2.0));

        data.sortByKeys(SortOrder.DESCENDING);

        
        assertEquals(data.getKey(0), "D");
        assertEquals(data.getKey(1), "C");
        assertEquals(data.getKey(2), "B");
        assertEquals(data.getKey(3), "A");

        
        assertEquals(data.getValue("A"), new Double(2.0));
        assertEquals(data.getValue("B"), null);
        assertEquals(data.getValue("C"), new Double(1.0));
        assertEquals(data.getValue("D"), new Double(3.0));

        
        assertEquals(data.getValue(0), new Double(3.0));
        assertEquals(data.getValue(1), new Double(1.0));
        assertEquals(data.getValue(2), null);
        assertEquals(data.getValue(3), new Double(2.0));

    }

// buggy code
    public XYDataItem addOrUpdate(Number x, Number y) {
        if (x == null) {
            throw new IllegalArgumentException("Null 'x' argument.");
        }

        // if we get to here, we know that duplicate X values are not permitted
        XYDataItem overwritten = null;
        int index = indexOf(x);
        if (index >= 0 && !this.allowDuplicateXValues) {
            XYDataItem existing = (XYDataItem) this.data.get(index);
            try {
                overwritten = (XYDataItem) existing.clone();
            }
            catch (CloneNotSupportedException e) {
                throw new SeriesException("Couldn't clone XYDataItem!");
            }
            existing.setY(y);
        }
        else {
            // if the series is sorted, the negative index is a result from
            // Collections.binarySearch() and tells us where to insert the
            // new item...otherwise it will be just -1 and we should just
            // append the value to the list...
            if (this.autoSort) {
                this.data.add(-index - 1, new XYDataItem(x, y));
            }
            else {
                this.data.add(new XYDataItem(x, y));
            }
            // check if this addition will exceed the maximum item count...
            if (getItemCount() > this.maximumItemCount) {
                this.data.remove(0);
            }
        }
        fireSeriesChanged();
        return overwritten;
    }

// relevant test
// org.jfree.chart.annotations.junit.XYBoxAnnotationTests::testEquals
    public void testEquals() {

        XYBoxAnnotation a1 = new XYBoxAnnotation(1.0, 2.0, 3.0, 4.0,
                new BasicStroke(1.2f), Color.red, Color.blue);
        XYBoxAnnotation a2 = new XYBoxAnnotation(1.0, 2.0, 3.0, 4.0,
                new BasicStroke(1.2f), Color.red, Color.blue);
        assertTrue(a1.equals(a2));
        assertTrue(a2.equals(a1));

        
        a1 = new XYBoxAnnotation(2.0, 2.0, 3.0, 4.0, new BasicStroke(1.2f),
                Color.red, Color.blue);
        assertFalse(a1.equals(a2));
        a2 = new XYBoxAnnotation(2.0, 2.0, 3.0, 4.0, new BasicStroke(1.2f),
                Color.red, Color.blue);
        assertTrue(a1.equals(a2));

        
        a1 = new XYBoxAnnotation(1.0, 2.0, 3.0, 4.0, new BasicStroke(2.3f),
                Color.red, Color.blue);
        assertFalse(a1.equals(a2));
        a2 = new XYBoxAnnotation(1.0, 2.0, 3.0, 4.0, new BasicStroke(2.3f),
                Color.red, Color.blue);
        assertTrue(a1.equals(a2));

        GradientPaint gp1a = new GradientPaint(1.0f, 2.0f, Color.blue,
                3.0f, 4.0f, Color.red);
        GradientPaint gp1b = new GradientPaint(1.0f, 2.0f, Color.blue,
                3.0f, 4.0f, Color.red);
        GradientPaint gp2a = new GradientPaint(5.0f, 6.0f, Color.pink,
                7.0f, 8.0f, Color.white);
        GradientPaint gp2b = new GradientPaint(5.0f, 6.0f, Color.pink,
                7.0f, 8.0f, Color.white);

        
        a1 = new XYBoxAnnotation(1.0, 2.0, 3.0, 4.0, new BasicStroke(2.3f),
                gp1a, Color.blue);
        assertFalse(a1.equals(a2));
        a2 = new XYBoxAnnotation(1.0, 2.0, 3.0, 4.0, new BasicStroke(2.3f),
                gp1b, Color.blue);
        assertTrue(a1.equals(a2));

        
        a1 = new XYBoxAnnotation(1.0, 2.0, 3.0, 4.0, new BasicStroke(2.3f),
                gp1a, gp2a);
        assertFalse(a1.equals(a2));
        a2 = new XYBoxAnnotation(1.0, 2.0, 3.0, 4.0, new BasicStroke(2.3f),
                gp1b, gp2b);
        assertTrue(a1.equals(a2));
    }

// org.jfree.chart.annotations.junit.XYBoxAnnotationTests::testHashCode
    public void testHashCode() {
        XYBoxAnnotation a1 = new XYBoxAnnotation(1.0, 2.0, 3.0, 4.0,
                new BasicStroke(1.2f), Color.red, Color.blue);
        XYBoxAnnotation a2 = new XYBoxAnnotation(1.0, 2.0, 3.0, 4.0,
                new BasicStroke(1.2f), Color.red, Color.blue);
        assertTrue(a1.equals(a2));
        int h1 = a1.hashCode();
        int h2 = a2.hashCode();
        assertEquals(h1, h2);
    }

// org.jfree.chart.annotations.junit.XYBoxAnnotationTests::testCloning
    public void testCloning() {
        XYBoxAnnotation a1 = new XYBoxAnnotation(1.0, 2.0, 3.0, 4.0,
                new BasicStroke(1.2f), Color.red, Color.blue);
        XYBoxAnnotation a2 = null;
        try {
            a2 = (XYBoxAnnotation) a1.clone();
        }
        catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        assertTrue(a1 != a2);
        assertTrue(a1.getClass() == a2.getClass());
        assertTrue(a1.equals(a2));
    }

// org.jfree.chart.annotations.junit.XYBoxAnnotationTests::testPublicCloneable
    public void testPublicCloneable() {
        XYBoxAnnotation a1 = new XYBoxAnnotation(1.0, 2.0, 3.0, 4.0,
                new BasicStroke(1.2f), Color.red, Color.blue);
        assertTrue(a1 instanceof PublicCloneable);
    }

// org.jfree.chart.annotations.junit.XYBoxAnnotationTests::testSerialization
    public void testSerialization() {

        XYBoxAnnotation a1 = new XYBoxAnnotation(1.0, 2.0, 3.0, 4.0,
                new BasicStroke(1.2f), Color.red, Color.blue);
        XYBoxAnnotation a2 = null;

        try {
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            ObjectOutput out = new ObjectOutputStream(buffer);
            out.writeObject(a1);
            out.close();

            ObjectInput in = new ObjectInputStream(new ByteArrayInputStream(
                    buffer.toByteArray()));
            a2 = (XYBoxAnnotation) in.readObject();
            in.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        assertEquals(a1, a2);
    }

// org.jfree.chart.annotations.junit.XYBoxAnnotationTests::testDrawWithNullInfo
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
                    new XYLineAndShapeRenderer());
            plot.addAnnotation(new XYBoxAnnotation(10.0, 12.0, 3.0, 4.0,
                    new BasicStroke(1.2f), Color.red, Color.blue));
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

        a1.setMinorTickCount(8);
        assertFalse(a1.equals(a2));
        a2.setMinorTickCount(8);
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

// org.jfree.chart.labels.junit.BubbleXYItemLabelGeneratorTests::testEquals
    public void testEquals() {

        
        String f1 = "{1}";
        String f2 = "{2}";
        NumberFormat xnf1 = new DecimalFormat("0.00");
        NumberFormat xnf2 = new DecimalFormat("0.000");
        NumberFormat ynf1 = new DecimalFormat("0.00");
        NumberFormat ynf2 = new DecimalFormat("0.000");
        NumberFormat znf1 = new DecimalFormat("0.00");
        NumberFormat znf2 = new DecimalFormat("0.000");

        BubbleXYItemLabelGenerator g1 = null;
        BubbleXYItemLabelGenerator g2 = null;

        g1 = new BubbleXYItemLabelGenerator(f1, xnf1, ynf1, znf1);
        g2 = new BubbleXYItemLabelGenerator(f1, xnf1, ynf1, znf1);
        assertTrue(g1.equals(g2));
        assertTrue(g2.equals(g1));

        g1 = new BubbleXYItemLabelGenerator(f2, xnf1, ynf1, znf1);
        assertFalse(g1.equals(g2));
        g2 = new BubbleXYItemLabelGenerator(f2, xnf1, ynf1, znf1);
        assertTrue(g1.equals(g2));

        g1 = new BubbleXYItemLabelGenerator(f2, xnf2, ynf1, znf1);
        assertFalse(g1.equals(g2));
        g2 = new BubbleXYItemLabelGenerator(f2, xnf2, ynf1, znf1);
        assertTrue(g1.equals(g2));

        g1 = new BubbleXYItemLabelGenerator(f2, xnf2, ynf2, znf1);
        assertFalse(g1.equals(g2));
        g2 = new BubbleXYItemLabelGenerator(f2, xnf2, ynf2, znf1);
        assertTrue(g1.equals(g2));

        g1 = new BubbleXYItemLabelGenerator(f2, xnf2, ynf2, znf2);
        assertFalse(g1.equals(g2));
        g2 = new BubbleXYItemLabelGenerator(f2, xnf2, ynf2, znf2);
        assertTrue(g1.equals(g2));

        DateFormat xdf1 = new SimpleDateFormat("d-MMM");
        DateFormat xdf2 = new SimpleDateFormat("d-MMM-yyyy");
        DateFormat ydf1 = new SimpleDateFormat("d-MMM");
        DateFormat ydf2 = new SimpleDateFormat("d-MMM-yyyy");
        DateFormat zdf1 = new SimpleDateFormat("d-MMM");
        DateFormat zdf2 = new SimpleDateFormat("d-MMM-yyyy");

        g1 = new BubbleXYItemLabelGenerator(f1, xdf1, ydf1, zdf1);
        g2 = new BubbleXYItemLabelGenerator(f1, xdf1, ydf1, zdf1);
        assertTrue(g1.equals(g2));
        assertTrue(g2.equals(g1));

        g1 = new BubbleXYItemLabelGenerator(f1, xdf2, ydf1, zdf1);
        assertFalse(g1.equals(g2));
        g2 = new BubbleXYItemLabelGenerator(f1, xdf2, ydf1, zdf1);
        assertTrue(g1.equals(g2));

        g1 = new BubbleXYItemLabelGenerator(f1, xdf2, ydf2, zdf1);
        assertFalse(g1.equals(g2));
        g2 = new BubbleXYItemLabelGenerator(f1, xdf2, ydf2, zdf1);
        assertTrue(g1.equals(g2));

        g1 = new BubbleXYItemLabelGenerator(f1, xdf2, ydf2, zdf2);
        assertFalse(g1.equals(g2));
        g2 = new BubbleXYItemLabelGenerator(f1, xdf2, ydf2, zdf2);
        assertTrue(g1.equals(g2));
    }

// org.jfree.chart.labels.junit.BubbleXYItemLabelGeneratorTests::testHashCode
    public void testHashCode() {
        BubbleXYItemLabelGenerator g1
                = new BubbleXYItemLabelGenerator();
        BubbleXYItemLabelGenerator g2
                = new BubbleXYItemLabelGenerator();
        assertTrue(g1.equals(g2));
        assertTrue(g1.hashCode() == g2.hashCode());
    }

// org.jfree.chart.labels.junit.BubbleXYItemLabelGeneratorTests::testCloning
    public void testCloning() {
        BubbleXYItemLabelGenerator g1 = new BubbleXYItemLabelGenerator();
        BubbleXYItemLabelGenerator g2 = null;
        try {
            g2 = (BubbleXYItemLabelGenerator) g1.clone();
        }
        catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        assertTrue(g1 != g2);
        assertTrue(g1.getClass() == g2.getClass());
        assertTrue(g1.equals(g2));
    }

// org.jfree.chart.labels.junit.BubbleXYItemLabelGeneratorTests::testPublicCloneable
    public void testPublicCloneable() {
        BubbleXYItemLabelGenerator g1 = new BubbleXYItemLabelGenerator();
        assertTrue(g1 instanceof PublicCloneable);
    }

// org.jfree.chart.labels.junit.BubbleXYItemLabelGeneratorTests::testSerialization
    public void testSerialization() {

        BubbleXYItemLabelGenerator g1 = new BubbleXYItemLabelGenerator();
        BubbleXYItemLabelGenerator g2 = null;

        try {
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            ObjectOutput out = new ObjectOutputStream(buffer);
            out.writeObject(g1);
            out.close();

            ObjectInput in = new ObjectInputStream(
                    new ByteArrayInputStream(buffer.toByteArray()));
            g2 = (BubbleXYItemLabelGenerator) in.readObject();
            in.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        assertEquals(g1, g2);

    }

// org.jfree.chart.labels.junit.BubbleXYItemLabelGeneratorTests::testGenerateLabel
    public void testGenerateLabel() {
        
        
        XYSeries s1 = new XYSeries("S1");
        s1.add(1.0, 2.0);
        s1.add(2.2, 3.3);
        XYSeriesCollection dataset = new XYSeriesCollection(s1);
        BubbleXYItemLabelGenerator g = new BubbleXYItemLabelGenerator();
        assertEquals("{3}", g.generateLabel(dataset, 0, 0));
        assertEquals("{3}", g.generateLabel(dataset, 0, 1));
    }

// org.jfree.chart.labels.junit.StandardXYSeriesLabelGeneratorTests::testGenerateLabel
    public void testGenerateLabel() {
        StandardXYSeriesLabelGenerator g
                = new StandardXYSeriesLabelGenerator("Series {0}");
        XYSeriesCollection dataset = new XYSeriesCollection();
        dataset.addSeries(new XYSeries("1"));
        dataset.addSeries(new XYSeries("2"));
        assertEquals("Series 1", g.generateLabel(dataset, 0));
        assertEquals("Series 2", g.generateLabel(dataset, 1));
    }

// org.jfree.chart.labels.junit.StandardXYSeriesLabelGeneratorTests::testEquals
    public void testEquals() {
        StandardXYSeriesLabelGenerator g1
                = new StandardXYSeriesLabelGenerator("Series {0}");
        StandardXYSeriesLabelGenerator g2
                = new StandardXYSeriesLabelGenerator("Series {0}");
        assertTrue(g1.equals(g2));
        assertTrue(g2.equals(g1));

        g1 = new StandardXYSeriesLabelGenerator("{1}");
        assertFalse(g1.equals(g2));
        g2 = new StandardXYSeriesLabelGenerator("{1}");
        assertTrue(g1.equals(g2));
    }

// org.jfree.chart.labels.junit.StandardXYSeriesLabelGeneratorTests::testHashCode
    public void testHashCode() {
        StandardXYSeriesLabelGenerator g1
                = new StandardXYSeriesLabelGenerator();
        StandardXYSeriesLabelGenerator g2
                = new StandardXYSeriesLabelGenerator();
        assertTrue(g1.equals(g2));
        assertTrue(g1.hashCode() == g2.hashCode());
    }

// org.jfree.chart.labels.junit.StandardXYSeriesLabelGeneratorTests::testCloning
    public void testCloning() {
        StandardXYSeriesLabelGenerator g1
                = new StandardXYSeriesLabelGenerator("Series {0}");
        StandardXYSeriesLabelGenerator g2 = null;
        try {
            g2 = (StandardXYSeriesLabelGenerator) g1.clone();
        }
        catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        assertTrue(g1 != g2);
        assertTrue(g1.getClass() == g2.getClass());
        assertTrue(g1.equals(g2));
    }

// org.jfree.chart.labels.junit.StandardXYSeriesLabelGeneratorTests::testPublicCloneable
    public void testPublicCloneable() {
        StandardXYSeriesLabelGenerator g1
                = new StandardXYSeriesLabelGenerator("Series {0}");
        assertTrue(g1 instanceof PublicCloneable);
    }

// org.jfree.chart.labels.junit.StandardXYSeriesLabelGeneratorTests::testSerialization
    public void testSerialization() {
        StandardXYSeriesLabelGenerator g1
                = new StandardXYSeriesLabelGenerator("Series {0}");
        StandardXYSeriesLabelGenerator g2 = null;

        try {
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            ObjectOutput out = new ObjectOutputStream(buffer);
            out.writeObject(g1);
            out.close();

            ObjectInput in = new ObjectInputStream(
                    new ByteArrayInputStream(buffer.toByteArray()));
            g2 = (StandardXYSeriesLabelGenerator) in.readObject();
            in.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        assertEquals(g1, g2);
    }

// org.jfree.chart.plot.junit.CombinedDomainXYPlotTests::testConstructor1
    public void testConstructor1() {
        CombinedDomainXYPlot plot = new CombinedDomainXYPlot(null);
        assertEquals(null, plot.getDomainAxis());
    }

// org.jfree.chart.plot.junit.CombinedDomainXYPlotTests::testRemoveSubplot
    public void testRemoveSubplot() {
        CombinedDomainXYPlot plot = new CombinedDomainXYPlot();
        XYPlot plot1 = new XYPlot();
        XYPlot plot2 = new XYPlot();
        plot.add(plot1);
        plot.add(plot2);
        
        plot.remove(plot2);
        List plots = plot.getSubplots();
        assertTrue(plots.get(0) == plot1);
    }

// org.jfree.chart.plot.junit.CombinedDomainXYPlotTests::testEquals
    public void testEquals() {
        CombinedDomainXYPlot plot1 = createPlot();
        CombinedDomainXYPlot plot2 = createPlot();
        assertTrue(plot1.equals(plot2));
        assertTrue(plot2.equals(plot1));
    }

// org.jfree.chart.plot.junit.CombinedDomainXYPlotTests::testCloning
    public void testCloning() {
        CombinedDomainXYPlot plot1 = createPlot();
        CombinedDomainXYPlot plot2 = null;
        try {
            plot2 = (CombinedDomainXYPlot) plot1.clone();
        }
        catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        assertTrue(plot1 != plot2);
        assertTrue(plot1.getClass() == plot2.getClass());
        assertTrue(plot1.equals(plot2));
    }

// org.jfree.chart.plot.junit.CombinedDomainXYPlotTests::testSerialization
    public void testSerialization() {
        CombinedDomainXYPlot plot1 = createPlot();
        CombinedDomainXYPlot plot2 = null;
        try {
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            ObjectOutput out = new ObjectOutputStream(buffer);
            out.writeObject(plot1);
            out.close();

            ObjectInput in = new ObjectInputStream(new ByteArrayInputStream(
                    buffer.toByteArray()));
            plot2 = (CombinedDomainXYPlot) in.readObject();
            in.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        assertEquals(plot1, plot2);
    }

// org.jfree.chart.plot.junit.CombinedDomainXYPlotTests::testNotification
    public void testNotification() {
        CombinedDomainXYPlot plot = createPlot();
        JFreeChart chart = new JFreeChart(plot);
        chart.addChangeListener(this);
        XYPlot subplot1 = (XYPlot) plot.getSubplots().get(0);
        NumberAxis yAxis = (NumberAxis) subplot1.getRangeAxis();
        yAxis.setAutoRangeIncludesZero(!yAxis.getAutoRangeIncludesZero());
        assertEquals(1, this.events.size());

        
        BufferedImage image = new BufferedImage(200, 100,
                BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = image.createGraphics();
        this.events.clear();
        chart.draw(g2, new Rectangle2D.Double(0.0, 0.0, 200.0, 100.0));
        assertTrue(this.events.isEmpty());
    }

// org.jfree.chart.plot.junit.CombinedRangeXYPlotTests::testEquals
    public void testEquals() {
        CombinedRangeXYPlot plot1 = createPlot();
        CombinedRangeXYPlot plot2 = createPlot();
        assertTrue(plot1.equals(plot2));
        assertTrue(plot2.equals(plot1));
    }

// org.jfree.chart.plot.junit.CombinedRangeXYPlotTests::testRemoveSubplot
    public void testRemoveSubplot() {
        CombinedRangeXYPlot plot = new CombinedRangeXYPlot();
        XYPlot plot1 = new XYPlot();
        XYPlot plot2 = new XYPlot();
        plot.add(plot1);
        plot.add(plot2);
        
        plot.remove(plot2);
        List plots = plot.getSubplots();
        assertTrue(plots.get(0) == plot1);
    }

// org.jfree.chart.plot.junit.CombinedRangeXYPlotTests::testCloning
    public void testCloning() {
        CombinedRangeXYPlot plot1 = createPlot();
        CombinedRangeXYPlot plot2 = null;
        try {
            plot2 = (CombinedRangeXYPlot) plot1.clone();
        }
        catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        assertTrue(plot1 != plot2);
        assertTrue(plot1.getClass() == plot2.getClass());
        assertTrue(plot1.equals(plot2));
    }

// org.jfree.chart.plot.junit.CombinedRangeXYPlotTests::testSerialization
    public void testSerialization() {
        CombinedRangeXYPlot plot1 = createPlot();
        CombinedRangeXYPlot plot2 = null;
        try {
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            ObjectOutput out = new ObjectOutputStream(buffer);
            out.writeObject(plot1);
            out.close();

            ObjectInput in = new ObjectInputStream(new ByteArrayInputStream(
                    buffer.toByteArray()));
            plot2 = (CombinedRangeXYPlot) in.readObject();
            in.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        assertEquals(plot1, plot2);
    }

// org.jfree.chart.plot.junit.CombinedRangeXYPlotTests::testNotification
    public void testNotification() {
        CombinedRangeXYPlot plot = createPlot();
        JFreeChart chart = new JFreeChart(plot);
        chart.addChangeListener(this);
        XYPlot subplot1 = (XYPlot) plot.getSubplots().get(0);
        NumberAxis xAxis = (NumberAxis) subplot1.getDomainAxis();
        xAxis.setAutoRangeIncludesZero(!xAxis.getAutoRangeIncludesZero());
        assertEquals(1, this.events.size());

        
        BufferedImage image = new BufferedImage(200, 100,
                BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = image.createGraphics();
        this.events.clear();
        chart.draw(g2, new Rectangle2D.Double(0.0, 0.0, 200.0, 100.0));
        assertTrue(this.events.isEmpty());
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

        plot1.setDomainMinorGridlinesVisible(true);
        assertFalse(plot1.equals(plot2));
        plot2.setDomainMinorGridlinesVisible(true);
        assertTrue(plot1.equals(plot2));

        plot1.setDomainMinorGridlinePaint(Color.red);
        assertFalse(plot1.equals(plot2));
        plot2.setDomainMinorGridlinePaint(Color.red);
        assertTrue(plot1.equals(plot2));

        plot1.setDomainGridlineStroke(new BasicStroke(1.1f));
        assertFalse(plot1.equals(plot2));
        plot2.setDomainGridlineStroke(new BasicStroke(1.1f));
        assertTrue(plot1.equals(plot2));

        plot1.setRangeMinorGridlinesVisible(true);
        assertFalse(plot1.equals(plot2));
        plot2.setRangeMinorGridlinesVisible(true);
        assertTrue(plot1.equals(plot2));

        plot1.setRangeMinorGridlinePaint(Color.blue);
        assertFalse(plot1.equals(plot2));
        plot2.setRangeMinorGridlinePaint(Color.blue);
        assertTrue(plot1.equals(plot2));

        plot1.setRangeMinorGridlineStroke(new BasicStroke(1.23f));
        assertFalse(plot1.equals(plot2));
        plot2.setRangeMinorGridlineStroke(new BasicStroke(1.23f));
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

// org.jfree.chart.renderer.xy.junit.AbstractXYItemRendererTests::testFindDomainBounds
    public void testFindDomainBounds() {
        AbstractXYItemRenderer renderer = new StandardXYItemRenderer();

        
        XYDataset dataset = createDataset1();
        Range r = renderer.findDomainBounds(dataset);
        assertEquals(1.0, r.getLowerBound(), EPSILON);
        assertEquals(3.0, r.getUpperBound(), EPSILON);

        
        assertTrue(renderer.findDomainBounds(null) == null);
    }

// org.jfree.chart.renderer.xy.junit.AbstractXYItemRendererTests::testFindRangeBounds
    public void testFindRangeBounds() {
        AbstractXYItemRenderer renderer = new StandardXYItemRenderer();
        
        assertTrue(renderer.findRangeBounds(null) == null);
    }

// org.jfree.chart.renderer.xy.junit.AbstractXYItemRendererTests::testCloning_LegendItemLabelGenerator
    public void testCloning_LegendItemLabelGenerator() {
        StandardXYSeriesLabelGenerator generator
                = new StandardXYSeriesLabelGenerator("Series {0}");
        XYBarRenderer r1 = new XYBarRenderer();
        r1.setLegendItemLabelGenerator(generator);
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

        
        assertTrue(r1.getLegendItemLabelGenerator()
                != r2.getLegendItemLabelGenerator());
    }

// org.jfree.chart.renderer.xy.junit.AbstractXYItemRendererTests::testCloning_LegendItemToolTipGenerator
    public void testCloning_LegendItemToolTipGenerator() {
        StandardXYSeriesLabelGenerator generator
                = new StandardXYSeriesLabelGenerator("Series {0}");
        XYBarRenderer r1 = new XYBarRenderer();
        r1.setLegendItemToolTipGenerator(generator);
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

        
        assertTrue(r1.getLegendItemToolTipGenerator()
                != r2.getLegendItemToolTipGenerator());
    }

// org.jfree.chart.renderer.xy.junit.AbstractXYItemRendererTests::testCloning_LegendItemURLGenerator
    public void testCloning_LegendItemURLGenerator() {
        StandardXYSeriesLabelGenerator generator
                = new StandardXYSeriesLabelGenerator("Series {0}");
        XYBarRenderer r1 = new XYBarRenderer();
        r1.setLegendItemURLGenerator(generator);
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

        
        assertTrue(r1.getLegendItemURLGenerator()
                != r2.getLegendItemURLGenerator());
    }

// org.jfree.chart.renderer.xy.junit.AbstractXYItemRendererTests::testEquals
    public void testEquals() {
        XYBarRenderer r1 = new XYBarRenderer();
        XYBarRenderer r2 = new XYBarRenderer();
        assertTrue(r1.equals(r2));

        
        r1.addAnnotation(new XYTextAnnotation("ABC", 1.0, 2.0),
                Layer.BACKGROUND);
        assertFalse(r1.equals(r2));
        r2.addAnnotation(new XYTextAnnotation("ABC", 1.0, 2.0),
                Layer.BACKGROUND);
        assertTrue(r1.equals(r2));

        
        r1.addAnnotation(new XYTextAnnotation("DEF", 3.0, 4.0),
                Layer.FOREGROUND);
        assertFalse(r1.equals(r2));
        r2.addAnnotation(new XYTextAnnotation("DEF", 3.0, 4.0),
                Layer.FOREGROUND);
        assertTrue(r1.equals(r2));

        
        r1.setDefaultEntityRadius(99);
        assertFalse(r1.equals(r2));
        r2.setDefaultEntityRadius(99);
        assertTrue(r1.equals(r2));

        
        r1.setLegendItemLabelGenerator(new StandardXYSeriesLabelGenerator(
                "X:{0}"));
        assertFalse(r1.equals(r2));
        r2.setLegendItemLabelGenerator(new StandardXYSeriesLabelGenerator(
                "X:{0}"));
        assertTrue(r1.equals(r2));

        
        r1.setLegendItemToolTipGenerator(new StandardXYSeriesLabelGenerator(
                "X:{0}"));
        assertFalse(r1.equals(r2));
        r2.setLegendItemToolTipGenerator(new StandardXYSeriesLabelGenerator(
                "X:{0}"));
        assertTrue(r1.equals(r2));

        
        r1.setLegendItemURLGenerator(new StandardXYSeriesLabelGenerator(
                "X:{0}"));
        assertFalse(r1.equals(r2));
        r2.setLegendItemURLGenerator(new StandardXYSeriesLabelGenerator(
                "X:{0}"));
        assertTrue(r1.equals(r2));
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

// org.jfree.chart.renderer.xy.junit.StackedXYAreaRenderer2Tests::testPublicCloneable
    public void testPublicCloneable() {
        StackedXYAreaRenderer2 r1 = new StackedXYAreaRenderer2();
        assertTrue(r1 instanceof PublicCloneable);
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

// org.jfree.chart.renderer.xy.junit.StackedXYAreaRendererTests::testPublicCloneable
    public void testPublicCloneable() {
        StackedXYAreaRenderer r1 = new StackedXYAreaRenderer();
        assertTrue(r1 instanceof PublicCloneable);
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

// org.jfree.chart.renderer.xy.junit.StackedXYBarRendererTests::testPublicCloneable
    public void testPublicCloneable() {
        StackedXYBarRenderer r1 = new StackedXYBarRenderer();
        assertTrue(r1 instanceof PublicCloneable);
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

// org.jfree.chart.renderer.xy.junit.StandardXYItemRendererTests::testPublicCloneable
    public void testPublicCloneable() {
        StandardXYItemRenderer r1 = new StandardXYItemRenderer();
        assertTrue(r1 instanceof PublicCloneable);
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

// org.jfree.chart.renderer.xy.junit.XYAreaRenderer2Tests::testEquals
    public void testEquals() {
        XYAreaRenderer2 r1 = new XYAreaRenderer2();
        XYAreaRenderer2 r2 = new XYAreaRenderer2();
        assertEquals(r1, r2);

        r1.setOutline(!r1.isOutline());
        assertFalse(r1.equals(r2));
        r2.setOutline(r1.isOutline());
        assertTrue(r1.equals(r2));

        r1.setLegendArea(new Rectangle(1, 2, 3, 4));
        assertFalse(r1.equals(r2));
        r2.setLegendArea(new Rectangle(1, 2, 3, 4));
        assertTrue(r1.equals(r2));
    }

// org.jfree.chart.renderer.xy.junit.XYAreaRenderer2Tests::testHashcode
    public void testHashcode() {
        XYAreaRenderer2 r1 = new XYAreaRenderer2();
        XYAreaRenderer2 r2 = new XYAreaRenderer2();
        assertTrue(r1.equals(r2));
        int h1 = r1.hashCode();
        int h2 = r2.hashCode();
        assertEquals(h1, h2);
    }

// org.jfree.chart.renderer.xy.junit.XYAreaRenderer2Tests::testCloning
    public void testCloning() {
        XYAreaRenderer2 r1 = new XYAreaRenderer2();
        Rectangle rect = new Rectangle(1, 2, 3, 4);
        r1.setLegendArea(rect);
        XYAreaRenderer2 r2 = null;
        try {
            r2 = (XYAreaRenderer2) r1.clone();
        }
        catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        assertTrue(r1 != r2);
        assertTrue(r1.getClass() == r2.getClass());
        assertTrue(r1.equals(r2));

        
        rect.setBounds(99, 99, 99, 99);
        assertFalse(r1.equals(r2));
    }

// org.jfree.chart.renderer.xy.junit.XYAreaRenderer2Tests::testPublicCloneable
    public void testPublicCloneable() {
        XYAreaRenderer2 r1 = new XYAreaRenderer2();
        assertTrue(r1 instanceof PublicCloneable);
    }

// org.jfree.chart.renderer.xy.junit.XYAreaRenderer2Tests::testSerialization
    public void testSerialization() {

        XYAreaRenderer2 r1 = new XYAreaRenderer2();
        XYAreaRenderer2 r2 = null;

        try {
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            ObjectOutput out = new ObjectOutputStream(buffer);
            out.writeObject(r1);
            out.close();

            ObjectInput in = new ObjectInputStream(
                    new ByteArrayInputStream(buffer.toByteArray()));
            r2 = (XYAreaRenderer2) in.readObject();
            in.close();
        }
        catch (Exception e) {
            fail(e.toString());
        }
        assertEquals(r1, r2);

    }

// org.jfree.chart.renderer.xy.junit.XYAreaRenderer2Tests::testDrawWithNullInfo
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
                    new XYAreaRenderer2());
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

// org.jfree.chart.renderer.xy.junit.XYAreaRenderer2Tests::testGetLegendItemSeriesIndex
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

        XYAreaRenderer2 r = new XYAreaRenderer2();
        XYPlot plot = new XYPlot(d1, new NumberAxis("x"),
                new NumberAxis("y"), r);
        plot.setDataset(1, d2);
         new JFreeChart(plot);
        LegendItem li = r.getLegendItem(1, 2);
        assertEquals("S5", li.getLabel());
        assertEquals(1, li.getDatasetIndex());
        assertEquals(2, li.getSeriesIndex());
    }

// org.jfree.chart.renderer.xy.junit.XYAreaRendererTests::testEquals
    public void testEquals() {
        XYAreaRenderer r1 = new XYAreaRenderer();
        XYAreaRenderer r2 = new XYAreaRenderer();
        assertEquals(r1, r2);

        r1 = new XYAreaRenderer(XYAreaRenderer.AREA_AND_SHAPES);
        assertFalse(r1.equals(r2));
        r2 = new XYAreaRenderer(XYAreaRenderer.AREA_AND_SHAPES);
        assertTrue(r1.equals(r2));

        r1 = new XYAreaRenderer(XYAreaRenderer.AREA);
        assertFalse(r1.equals(r2));
        r2 = new XYAreaRenderer(XYAreaRenderer.AREA);
        assertTrue(r1.equals(r2));

        r1 = new XYAreaRenderer(XYAreaRenderer.LINES);
        assertFalse(r1.equals(r2));
        r2 = new XYAreaRenderer(XYAreaRenderer.LINES);
        assertTrue(r1.equals(r2));

        r1 = new XYAreaRenderer(XYAreaRenderer.SHAPES);
        assertFalse(r1.equals(r2));
        r2 = new XYAreaRenderer(XYAreaRenderer.SHAPES);
        assertTrue(r1.equals(r2));

        r1 = new XYAreaRenderer(XYAreaRenderer.SHAPES_AND_LINES);
        assertFalse(r1.equals(r2));
        r2 = new XYAreaRenderer(XYAreaRenderer.SHAPES_AND_LINES);
        assertTrue(r1.equals(r2));

        r1.setOutline(true);
        assertFalse(r1.equals(r2));
        r2.setOutline(true);
        assertTrue(r1.equals(r2));

        r1.setLegendArea(new Rectangle2D.Double(1.0, 2.0, 3.0, 4.0));
        assertFalse(r1.equals(r2));
        r2.setLegendArea(new Rectangle2D.Double(1.0, 2.0, 3.0, 4.0));
        assertTrue(r1.equals(r2));
    }

// org.jfree.chart.renderer.xy.junit.XYAreaRendererTests::testHashcode
    public void testHashcode() {
        XYAreaRenderer r1 = new XYAreaRenderer();
        XYAreaRenderer r2 = new XYAreaRenderer();
        assertTrue(r1.equals(r2));
        int h1 = r1.hashCode();
        int h2 = r2.hashCode();
        assertEquals(h1, h2);
    }

// org.jfree.chart.renderer.xy.junit.XYAreaRendererTests::testCloning
    public void testCloning() {
        XYAreaRenderer r1 = new XYAreaRenderer();
        Rectangle2D rect1 = new Rectangle2D.Double(1.0, 2.0, 3.0, 4.0);
        r1.setLegendArea(rect1);
        XYAreaRenderer r2 = null;
        try {
            r2 = (XYAreaRenderer) r1.clone();
        }
        catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        assertTrue(r1 != r2);
        assertTrue(r1.getClass() == r2.getClass());
        assertTrue(r1.equals(r2));

        
        rect1.setRect(4.0, 3.0, 2.0, 1.0);
        assertFalse(r1.equals(r2));
        r2.setLegendArea(new Rectangle2D.Double(4.0, 3.0, 2.0, 1.0));
        assertTrue(r1.equals(r2));
    }

// org.jfree.chart.renderer.xy.junit.XYAreaRendererTests::testPublicCloneable
    public void testPublicCloneable() {
        XYAreaRenderer r1 = new XYAreaRenderer();
        assertTrue(r1 instanceof PublicCloneable);
    }

// org.jfree.chart.renderer.xy.junit.XYAreaRendererTests::testSerialization
    public void testSerialization() {

        XYAreaRenderer r1 = new XYAreaRenderer();
        XYAreaRenderer r2 = null;

        try {
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            ObjectOutput out = new ObjectOutputStream(buffer);
            out.writeObject(r1);
            out.close();

            ObjectInput in = new ObjectInputStream(
                    new ByteArrayInputStream(buffer.toByteArray()));
            r2 = (XYAreaRenderer) in.readObject();
            in.close();
        }
        catch (Exception e) {
            System.out.println(e.toString());
        }
        assertEquals(r1, r2);

    }

// org.jfree.chart.renderer.xy.junit.XYAreaRendererTests::testDrawWithNullInfo
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
                    new XYAreaRenderer());
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

// org.jfree.chart.renderer.xy.junit.XYAreaRendererTests::testGetLegendItemSeriesIndex
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

        XYAreaRenderer r = new XYAreaRenderer();
        XYPlot plot = new XYPlot(d1, new NumberAxis("x"),
                new NumberAxis("y"), r);
        plot.setDataset(1, d2);
         new JFreeChart(plot);
        LegendItem li = r.getLegendItem(1, 2);
        assertEquals("S5", li.getLabel());
        assertEquals(1, li.getDatasetIndex());
        assertEquals(2, li.getSeriesIndex());
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

        
        r1.setBarPainter(new GradientXYBarPainter(0.11, 0.22, 0.33));
        assertFalse(r1.equals(r2));
        r2.setBarPainter(new GradientXYBarPainter(0.11, 0.22, 0.33));
        assertTrue(r1.equals(r2));

        
        r1.setShadowVisible(false);
        assertFalse(r1.equals(r2));
        r2.setShadowVisible(false);
        assertTrue(r1.equals(r2));

        
        r1.setShadowXOffset(3.3);
        assertFalse(r1.equals(r2));
        r2.setShadowXOffset(3.3);
        assertTrue(r1.equals(r2));

        
        r1.setShadowYOffset(3.3);
        assertFalse(r1.equals(r2));
        r2.setShadowYOffset(3.3);
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

// org.jfree.chart.renderer.xy.junit.XYBarRendererTests::testPublicCloneable
    public void testPublicCloneable() {
        XYBarRenderer r1 = new XYBarRenderer();
        assertTrue(r1 instanceof PublicCloneable);
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
                    new ByteArrayInputStream(buffer.toByteArray()));
            r2 = (XYBarRenderer) in.readObject();
            in.close();
        }
        catch (Exception e) {
            e.printStackTrace();
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

// org.jfree.chart.renderer.xy.junit.XYBarRendererTests::testFindRangeBounds
    public void testFindRangeBounds() {
        DefaultIntervalXYDataset dataset = new DefaultIntervalXYDataset();
        double[] x = {1.0, 2.0, 3.0, 4.0};
        double[] startx = {0.9, 1.8, 2.7, 3.6};
        double[] endx = {1.1, 2.2, 3.3, 4.4};
        double[] y = {1.0, 2.0, 3.0, 4.0};
        double[] starty = {0.9, 1.8, 2.7, 3.6};
        double[] endy = {1.1, 2.2, 3.3, 4.4};
        double[][] data = new double[][] {x, startx, endx, y, starty, endy};
        dataset.addSeries("Series 1", data);
        XYBarRenderer renderer = new XYBarRenderer();
        renderer.setUseYInterval(true);
        Range r = renderer.findRangeBounds(dataset);
        assertEquals(0.9, r.getLowerBound(), EPSILON);
        assertEquals(4.4, r.getUpperBound(), EPSILON);

        renderer.setUseYInterval(false);
        r = renderer.findRangeBounds(dataset);
        assertEquals(1.0, r.getLowerBound(), EPSILON);
        assertEquals(4.0, r.getUpperBound(), EPSILON);
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

// org.jfree.chart.renderer.xy.junit.XYDifferenceRendererTests::testEquals
    public void testEquals() {
        XYDifferenceRenderer r1 = new XYDifferenceRenderer(
                Color.red, Color.blue, false);
        XYDifferenceRenderer r2 = new XYDifferenceRenderer(
                Color.red, Color.blue, false);
        assertEquals(r1, r2);

        
        r1.setPositivePaint(new GradientPaint(1.0f, 2.0f, Color.red,
                3.0f, 4.0f, Color.blue));
        assertFalse(r1.equals(r2));
        r2.setPositivePaint(new GradientPaint(1.0f, 2.0f, Color.red,
                3.0f, 4.0f, Color.blue));
        assertTrue(r1.equals(r2));

        
        r1.setNegativePaint(new GradientPaint(1.0f, 2.0f, Color.yellow,
                3.0f, 4.0f, Color.blue));
        assertFalse(r1.equals(r2));
        r2.setNegativePaint(new GradientPaint(1.0f, 2.0f, Color.yellow,
                3.0f, 4.0f, Color.blue));
        assertTrue(r1.equals(r2));

        
        r1 = new XYDifferenceRenderer(Color.green, Color.yellow, true);
        assertFalse(r1.equals(r2));
        r2 = new XYDifferenceRenderer(Color.green, Color.yellow, true);
        assertTrue(r1.equals(r2));

        
        r1.setLegendLine(new Line2D.Double(1.0, 2.0, 3.0, 4.0));
        assertFalse(r1.equals(r2));
        r2.setLegendLine(new Line2D.Double(1.0, 2.0, 3.0, 4.0));
        assertTrue(r1.equals(r2));

        
        r1.setRoundXCoordinates(true);
        assertFalse(r1.equals(r2));
        r2.setRoundXCoordinates(true);
        assertTrue(r1.equals(r2));

        assertFalse(r1.equals(null));
    }

// org.jfree.chart.renderer.xy.junit.XYDifferenceRendererTests::testHashcode
    public void testHashcode() {
        XYDifferenceRenderer r1
            = new XYDifferenceRenderer(Color.red, Color.blue, false);
        XYDifferenceRenderer r2
            = new XYDifferenceRenderer(Color.red, Color.blue, false);
        assertTrue(r1.equals(r2));
        int h1 = r1.hashCode();
        int h2 = r2.hashCode();
        assertEquals(h1, h2);
    }

// org.jfree.chart.renderer.xy.junit.XYDifferenceRendererTests::testCloning
    public void testCloning() {
        XYDifferenceRenderer r1 = new XYDifferenceRenderer(Color.red,
                Color.blue, false);
        XYDifferenceRenderer r2 = null;
        try {
            r2 = (XYDifferenceRenderer) r1.clone();
        }
        catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        assertTrue(r1 != r2);
        assertTrue(r1.getClass() == r2.getClass());
        assertTrue(r1.equals(r2));

        
        Shape s = r1.getLegendLine();
        if (s instanceof Line2D) {
            Line2D l = (Line2D) s;
            l.setLine(1.0, 2.0, 3.0, 4.0);
            assertFalse(r1.equals(r2));
        }
    }

// org.jfree.chart.renderer.xy.junit.XYDifferenceRendererTests::testPublicCloneable
    public void testPublicCloneable() {
        XYDifferenceRenderer r1 = new XYDifferenceRenderer();
        assertTrue(r1 instanceof PublicCloneable);
    }

// org.jfree.chart.renderer.xy.junit.XYDifferenceRendererTests::testSerialization
    public void testSerialization() {

        XYDifferenceRenderer r1 = new XYDifferenceRenderer(Color.red,
                Color.blue, false);
        XYDifferenceRenderer r2 = null;

        try {
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            ObjectOutput out = new ObjectOutputStream(buffer);
            out.writeObject(r1);
            out.close();

            ObjectInput in = new ObjectInputStream(
                    new ByteArrayInputStream(buffer.toByteArray()));
            r2 = (XYDifferenceRenderer) in.readObject();
            in.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        assertEquals(r1, r2);

    }

// org.jfree.chart.renderer.xy.junit.XYDifferenceRendererTests::testGetLegendItemSeriesIndex
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

        XYDifferenceRenderer r = new XYDifferenceRenderer();
        XYPlot plot = new XYPlot(d1, new NumberAxis("x"),
                new NumberAxis("y"), r);
        plot.setDataset(1, d2);
         new JFreeChart(plot);
        LegendItem li = r.getLegendItem(1, 2);
        assertEquals("S5", li.getLabel());
        assertEquals(1, li.getDatasetIndex());
        assertEquals(2, li.getSeriesIndex());
    }

// org.jfree.chart.renderer.xy.junit.XYDotRendererTests::testEquals
    public void testEquals() {
        XYDotRenderer r1 = new XYDotRenderer();
        XYDotRenderer r2 = new XYDotRenderer();
        assertEquals(r1, r2);

        r1.setDotWidth(11);
        assertFalse(r1.equals(r2));
        r2.setDotWidth(11);
        assertTrue(r1.equals(r2));

        r1.setDotHeight(12);
        assertFalse(r1.equals(r2));
        r2.setDotHeight(12);
        assertTrue(r1.equals(r2));

        r1.setLegendShape(new Rectangle2D.Double(1.0, 2.0, 3.0, 4.0));
        assertFalse(r1.equals(r2));
        r2.setLegendShape(new Rectangle2D.Double(1.0, 2.0, 3.0, 4.0));
        assertTrue(r1.equals(r2));
    }

// org.jfree.chart.renderer.xy.junit.XYDotRendererTests::testHashcode
    public void testHashcode() {
        XYDotRenderer r1 = new XYDotRenderer();
        XYDotRenderer r2 = new XYDotRenderer();
        assertTrue(r1.equals(r2));
        int h1 = r1.hashCode();
        int h2 = r2.hashCode();
        assertEquals(h1, h2);

        r1.setDotHeight(12);
        r2.setDotHeight(12);
        assertTrue(r1.equals(r2));
        h1 = r1.hashCode();
        h2 = r2.hashCode();
        assertEquals(h1, h2);
    }

// org.jfree.chart.renderer.xy.junit.XYDotRendererTests::testCloning
    public void testCloning() {
        XYDotRenderer r1 = new XYDotRenderer();
        XYDotRenderer r2 = null;
        try {
            r2 = (XYDotRenderer) r1.clone();
        }
        catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        assertTrue(r1 != r2);
        assertTrue(r1.getClass() == r2.getClass());
        assertTrue(r1.equals(r2));
    }

// org.jfree.chart.renderer.xy.junit.XYDotRendererTests::testPublicCloneable
    public void testPublicCloneable() {
        XYDotRenderer r1 = new XYDotRenderer();
        assertTrue(r1 instanceof PublicCloneable);
    }

// org.jfree.chart.renderer.xy.junit.XYDotRendererTests::testSerialization
    public void testSerialization() {

        XYDotRenderer r1 = new XYDotRenderer();
        XYDotRenderer r2 = null;

        try {
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            ObjectOutput out = new ObjectOutputStream(buffer);
            out.writeObject(r1);
            out.close();

            ObjectInput in = new ObjectInputStream(
                    new ByteArrayInputStream(buffer.toByteArray()));
            r2 = (XYDotRenderer) in.readObject();
            in.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        assertEquals(r1, r2);

    }

// org.jfree.chart.renderer.xy.junit.XYDotRendererTests::testGetLegendItemSeriesIndex
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

        XYDotRenderer r = new XYDotRenderer();
        XYPlot plot = new XYPlot(d1, new NumberAxis("x"),
                new NumberAxis("y"), r);
        plot.setDataset(1, d2);
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

// org.jfree.chart.renderer.xy.junit.XYLineAndShapeRendererTests::testPublicCloneable
    public void testPublicCloneable() {
        XYLineAndShapeRenderer r1 = new XYLineAndShapeRenderer();
        assertTrue(r1 instanceof PublicCloneable);
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

// org.jfree.chart.renderer.xy.junit.XYStepAreaRendererTests::testEquals
    public void testEquals() {
        XYStepAreaRenderer r1 = new XYStepAreaRenderer();
        XYStepAreaRenderer r2 = new XYStepAreaRenderer();
        assertEquals(r1, r2);

        r1.setOutline(true);
        assertFalse(r1.equals(r2));
        r2.setOutline(true);
        assertTrue(r1.equals(r2));

        r1.setShapesVisible(true);
        assertFalse(r1.equals(r2));
        r2.setShapesVisible(true);
        assertTrue(r1.equals(r2));

        r1.setShapesFilled(true);
        assertFalse(r1.equals(r2));
        r2.setShapesFilled(true);
        assertTrue(r1.equals(r2));

        r1.setPlotArea(false);
        assertFalse(r1.equals(r2));
        r2.setPlotArea(false);
        assertTrue(r1.equals(r2));

        r1.setRangeBase(-1.0);
        assertFalse(r1.equals(r2));
        r2.setRangeBase(-1.0);
        assertTrue(r1.equals(r2));
    }

// org.jfree.chart.renderer.xy.junit.XYStepAreaRendererTests::testHashcode
    public void testHashcode() {
        XYStepAreaRenderer r1 = new XYStepAreaRenderer();
        XYStepAreaRenderer r2 = new XYStepAreaRenderer();
        assertTrue(r1.equals(r2));
        int h1 = r1.hashCode();
        int h2 = r2.hashCode();
        assertEquals(h1, h2);
    }

// org.jfree.chart.renderer.xy.junit.XYStepAreaRendererTests::testCloning
    public void testCloning() {
        XYStepAreaRenderer r1 = new XYStepAreaRenderer();
        XYStepAreaRenderer r2 = null;
        try {
            r2 = (XYStepAreaRenderer) r1.clone();
        }
        catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        assertTrue(r1 != r2);
        assertTrue(r1.getClass() == r2.getClass());
        assertTrue(r1.equals(r2));
    }

// org.jfree.chart.renderer.xy.junit.XYStepAreaRendererTests::testPublicCloneable
    public void testPublicCloneable() {
        XYStepAreaRenderer r1 = new XYStepAreaRenderer();
        assertTrue(r1 instanceof PublicCloneable);
    }

// org.jfree.chart.renderer.xy.junit.XYStepAreaRendererTests::testSerialization
    public void testSerialization() {

        XYStepAreaRenderer r1 = new XYStepAreaRenderer();
        XYStepAreaRenderer r2 = null;

        try {
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            ObjectOutput out = new ObjectOutputStream(buffer);
            out.writeObject(r1);
            out.close();

            ObjectInput in = new ObjectInputStream(
                    new ByteArrayInputStream(buffer.toByteArray()));
            r2 = (XYStepAreaRenderer) in.readObject();
            in.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        assertEquals(r1, r2);
    }

// org.jfree.chart.renderer.xy.junit.XYStepAreaRendererTests::testDrawWithNullInfo
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
                    new XYStepAreaRenderer());
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

// org.jfree.chart.renderer.xy.junit.XYStepRendererTests::testEquals
    public void testEquals() {
        XYStepRenderer r1 = new XYStepRenderer();
        XYStepRenderer r2 = new XYStepRenderer();
        assertEquals(r1, r2);

        r1.setStepPoint(0.44);
        assertFalse(r1.equals(r2));
        r2.setStepPoint(0.44);
        assertTrue(r1.equals(r2));

        
        r1.setBaseCreateEntities(false);
        assertFalse(r1.equals(r2));
        r2.setBaseCreateEntities(false);
        assertTrue(r1.equals(r2));
    }

// org.jfree.chart.renderer.xy.junit.XYStepRendererTests::testHashcode
    public void testHashcode() {
        XYStepRenderer r1 = new XYStepRenderer();
        r1.setStepPoint(0.123);
        XYStepRenderer r2 = new XYStepRenderer();
        r2.setStepPoint(0.123);
        assertTrue(r1.equals(r2));
        int h1 = r1.hashCode();
        int h2 = r2.hashCode();
        assertEquals(h1, h2);
    }

// org.jfree.chart.renderer.xy.junit.XYStepRendererTests::testCloning
    public void testCloning() {
        XYStepRenderer r1 = new XYStepRenderer();
        XYStepRenderer r2 = null;
        try {
            r2 = (XYStepRenderer) r1.clone();
        }
        catch (CloneNotSupportedException e) {
            System.err.println("Failed to clone.");
        }
        assertTrue(r1 != r2);
        assertTrue(r1.getClass() == r2.getClass());
        assertTrue(r1.equals(r2));
    }

// org.jfree.chart.renderer.xy.junit.XYStepRendererTests::testPublicCloneable
    public void testPublicCloneable() {
        XYStepRenderer r1 = new XYStepRenderer();
        assertTrue(r1 instanceof PublicCloneable);
    }

// org.jfree.chart.renderer.xy.junit.XYStepRendererTests::testSerialization
    public void testSerialization() {
        XYStepRenderer r1 = new XYStepRenderer();
        r1.setStepPoint(0.123);
        XYStepRenderer r2 = null;
        try {
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            ObjectOutput out = new ObjectOutputStream(buffer);
            out.writeObject(r1);
            out.close();

            ObjectInput in = new ObjectInputStream(
                    new ByteArrayInputStream(buffer.toByteArray()));
            r2 = (XYStepRenderer) in.readObject();
            in.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        assertEquals(r1, r2);
    }

// org.jfree.chart.renderer.xy.junit.XYStepRendererTests::testDrawWithNullInfo
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
                    new XYStepRenderer());
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

// org.jfree.chart.renderer.xy.junit.XYStepRendererTests::testDrawWithNullValue
    public void testDrawWithNullValue() {
        boolean success = false;
        try {
            DefaultTableXYDataset dataset = new DefaultTableXYDataset();

            XYSeries s1 = new XYSeries("Series 1", true, false);
            s1.add(5.0, 5.0);
            s1.add(10.0, null);
            s1.add(15.0, 9.5);
            s1.add(20.0, 7.5);
            dataset.addSeries(s1);

            XYSeries s2 = new XYSeries("Series 2", true, false);
            s2.add(5.0, 5.0);
            s2.add(10.0, 15.5);
            s2.add(15.0, null);
            s2.add(20.0, null);
            dataset.addSeries(s2);
            XYPlot plot = new XYPlot(dataset,
                    new NumberAxis("X"), new NumberAxis("Y"),
                    new XYStepRenderer());
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

// org.jfree.data.general.junit.DatasetUtilitiesTests::testFindDomainBounds2
    public void testFindDomainBounds2() {
        DefaultIntervalXYDataset dataset = new DefaultIntervalXYDataset();
        double[] x1 = new double[] {1.0, 2.0, 3.0};
        double[] x1Start = new double[] {0.9, 1.9, 2.9};
        double[] x1End = new double[] {1.1, 2.1, 3.1};
        double[] y1 = new double[] {4.0, 5.0, 6.0};
        double[] y1Start = new double[] {1.09, 2.09, 3.09};
        double[] y1End = new double[] {1.11, 2.11, 3.11};
        double[][] data1 = new double[][] {x1, x1Start, x1End, y1, y1Start,
                y1End};
        dataset.addSeries("S1", data1);
        Range r = DatasetUtilities.findDomainBounds(dataset);
        assertEquals(0.9, r.getLowerBound(), EPSILON);
        assertEquals(3.1, r.getUpperBound(), EPSILON);
    }

// org.jfree.data.general.junit.DatasetUtilitiesTests::testFindDomainBounds3
    public void testFindDomainBounds3() {
        DefaultIntervalXYDataset dataset = new DefaultIntervalXYDataset();
        double[] x1 = new double[] {1.0, 2.0, 3.0};
        double[] x1Start = new double[] {0.9, 1.9, 2.9};
        double[] x1End = new double[] {1.1, 2.1, 3.1};
        double[] y1 = new double[] {4.0, 5.0, 6.0};
        double[] y1Start = new double[] {1.09, 2.09, 3.09};
        double[] y1End = new double[] {1.11, 2.11, 3.11};
        double[][] data1 = new double[][] {x1, x1Start, x1End, y1, y1Start,
                y1End};
        dataset.addSeries("S1", data1);
        Range r = DatasetUtilities.findDomainBounds(dataset, false);
        assertEquals(1.0, r.getLowerBound(), EPSILON);
        assertEquals(3.0, r.getUpperBound(), EPSILON);
    }

// org.jfree.data.general.junit.DatasetUtilitiesTests::testFindDomainBounds_NaN
    public void testFindDomainBounds_NaN() {
        DefaultIntervalXYDataset dataset = new DefaultIntervalXYDataset();
        double[] x1 = new double[] {1.0, 2.0, Double.NaN};
        double[] x1Start = new double[] {0.9, 1.9, Double.NaN};
        double[] x1End = new double[] {1.1, 2.1, Double.NaN};
        double[] y1 = new double[] {4.0, 5.0, 6.0};
        double[] y1Start = new double[] {1.09, 2.09, 3.09};
        double[] y1End = new double[] {1.11, 2.11, 3.11};
        double[][] data1 = new double[][] {x1, x1Start, x1End, y1, y1Start,
                y1End};
        dataset.addSeries("S1", data1);
        Range r = DatasetUtilities.findDomainBounds(dataset);
        assertEquals(0.9, r.getLowerBound(), EPSILON);
        assertEquals(2.1, r.getUpperBound(), EPSILON);

        r = DatasetUtilities.findDomainBounds(dataset, false);
        assertEquals(1.0, r.getLowerBound(), EPSILON);
        assertEquals(2.0, r.getUpperBound(), EPSILON);
    }

// org.jfree.data.general.junit.DatasetUtilitiesTests::testIterateDomainBounds
    public void testIterateDomainBounds() {
        XYDataset dataset = createXYDataset1();
        Range r = DatasetUtilities.iterateDomainBounds(dataset);
        assertEquals(1.0, r.getLowerBound(), EPSILON);
        assertEquals(3.0, r.getUpperBound(), EPSILON);
    }

// org.jfree.data.general.junit.DatasetUtilitiesTests::testIterateDomainBounds_NaN
    public void testIterateDomainBounds_NaN() {
        DefaultXYDataset dataset = new DefaultXYDataset();
        double[] x = new double[] {1.0, 2.0, Double.NaN, 3.0};
        double[] y = new double[] {9.0, 8.0, 7.0, 6.0};
        dataset.addSeries("S1", new double[][] {x, y});
        Range r = DatasetUtilities.iterateDomainBounds(dataset);
        assertEquals(1.0, r.getLowerBound(), EPSILON);
        assertEquals(3.0, r.getUpperBound(), EPSILON);
    }

// org.jfree.data.general.junit.DatasetUtilitiesTests::testIterateDomainBounds_NaN2
    public void testIterateDomainBounds_NaN2() {
        DefaultIntervalXYDataset dataset = new DefaultIntervalXYDataset();
        double[] x1 = new double[] {Double.NaN, 2.0, 3.0};
        double[] x1Start = new double[] {0.9, Double.NaN, 2.9};
        double[] x1End = new double[] {1.1, Double.NaN, 3.1};
        double[] y1 = new double[] {4.0, 5.0, 6.0};
        double[] y1Start = new double[] {1.09, 2.09, 3.09};
        double[] y1End = new double[] {1.11, 2.11, 3.11};
        double[][] data1 = new double[][] {x1, x1Start, x1End, y1, y1Start,
                y1End};
        dataset.addSeries("S1", data1);
        Range r = DatasetUtilities.iterateDomainBounds(dataset, false);
        assertEquals(2.0, r.getLowerBound(), EPSILON);
        assertEquals(3.0, r.getUpperBound(), EPSILON);
        r = DatasetUtilities.iterateDomainBounds(dataset, true);
        assertEquals(0.9, r.getLowerBound(), EPSILON);
        assertEquals(3.1, r.getUpperBound(), EPSILON);
    }

// org.jfree.data.general.junit.DatasetUtilitiesTests::testFindRangeBounds_CategoryDataset
    public void testFindRangeBounds_CategoryDataset() {
        CategoryDataset dataset = createCategoryDataset1();
        Range r = DatasetUtilities.findRangeBounds(dataset);
        assertEquals(1.0, r.getLowerBound(), EPSILON);
        assertEquals(6.0, r.getUpperBound(), EPSILON);
    }

// org.jfree.data.general.junit.DatasetUtilitiesTests::testFindRangeBounds
    public void testFindRangeBounds() {
        XYDataset dataset = createXYDataset1();
        Range r = DatasetUtilities.findRangeBounds(dataset);
        assertEquals(100.0, r.getLowerBound(), EPSILON);
        assertEquals(105.0, r.getUpperBound(), EPSILON);
    }

// org.jfree.data.general.junit.DatasetUtilitiesTests::testFindRangeBounds2
    public void testFindRangeBounds2() {
        YIntervalSeriesCollection dataset = new YIntervalSeriesCollection();
        Range r = DatasetUtilities.findRangeBounds(dataset);
        assertNull(r);
        YIntervalSeries s1 = new YIntervalSeries("S1");
        dataset.addSeries(s1);
        r = DatasetUtilities.findRangeBounds(dataset);
        assertNull(r);

        
        s1.add(1.0, 2.0, 1.5, 2.5);
        r = DatasetUtilities.findRangeBounds(dataset);
        assertEquals(1.5, r.getLowerBound(), EPSILON);
        assertEquals(2.5, r.getUpperBound(), EPSILON);

        r = DatasetUtilities.findRangeBounds(dataset, false);
        assertEquals(2.0, r.getLowerBound(), EPSILON);
        assertEquals(2.0, r.getUpperBound(), EPSILON);

        
        s1.add(2.0, 2.0, 1.4, 2.1);
        r = DatasetUtilities.findRangeBounds(dataset);
        assertEquals(1.4, r.getLowerBound(), EPSILON);
        assertEquals(2.5, r.getUpperBound(), EPSILON);

        
        YIntervalSeries s2 = new YIntervalSeries("S2");
        dataset.addSeries(s2);
        r = DatasetUtilities.findRangeBounds(dataset);
        assertEquals(1.4, r.getLowerBound(), EPSILON);
        assertEquals(2.5, r.getUpperBound(), EPSILON);

        
        s2.add(1.0, 2.0, 1.9, 2.6);
        r = DatasetUtilities.findRangeBounds(dataset);
        assertEquals(1.4, r.getLowerBound(), EPSILON);
        assertEquals(2.6, r.getUpperBound(), EPSILON);

        
        r = DatasetUtilities.findRangeBounds(dataset, false);
        assertEquals(2.0, r.getLowerBound(), EPSILON);
        assertEquals(2.0, r.getUpperBound(), EPSILON);
    }

// org.jfree.data.general.junit.DatasetUtilitiesTests::testIterateRangeBounds_CategoryDataset
    public void testIterateRangeBounds_CategoryDataset() {
        CategoryDataset dataset = createCategoryDataset1();
        Range r = DatasetUtilities.iterateRangeBounds(dataset, false);
        assertEquals(1.0, r.getLowerBound(), EPSILON);
        assertEquals(6.0, r.getUpperBound(), EPSILON);
    }

// org.jfree.data.general.junit.DatasetUtilitiesTests::testIterateRangeBounds2_CategoryDataset
    public void testIterateRangeBounds2_CategoryDataset() {
        
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        Range r = DatasetUtilities.iterateRangeBounds(dataset, false);
        assertNull(r);

        
        dataset.addValue(1.23, "R1", "C1");
        r = DatasetUtilities.iterateRangeBounds(dataset, false);
        assertEquals(1.23, r.getLowerBound(), EPSILON);
        assertEquals(1.23, r.getUpperBound(), EPSILON);

        
        dataset.addValue(null, "R2", "C1");
        r = DatasetUtilities.iterateRangeBounds(dataset, false);
        assertEquals(1.23, r.getLowerBound(), EPSILON);
        assertEquals(1.23, r.getUpperBound(), EPSILON);

        
        dataset.addValue(Double.NaN, "R2", "C1");
        r = DatasetUtilities.iterateRangeBounds(dataset, false);
        assertEquals(1.23, r.getLowerBound(), EPSILON);
        assertEquals(1.23, r.getUpperBound(), EPSILON);
    }

// org.jfree.data.general.junit.DatasetUtilitiesTests::testIterateRangeBounds3_CategoryDataset
    public void testIterateRangeBounds3_CategoryDataset() {
        Number[][] starts = new Double[2][3];
        Number[][] ends = new Double[2][3];
        starts[0][0] = new Double(1.0);
        starts[0][1] = new Double(2.0);
        starts[0][2] = new Double(3.0);
        starts[1][0] = new Double(11.0);
        starts[1][1] = new Double(12.0);
        starts[1][2] = new Double(13.0);
        ends[0][0] = new Double(4.0);
        ends[0][1] = new Double(5.0);
        ends[0][2] = new Double(6.0);
        ends[1][0] = new Double(16.0);
        ends[1][1] = new Double(15.0);
        ends[1][2] = new Double(14.0);

        DefaultIntervalCategoryDataset d = new DefaultIntervalCategoryDataset(
                starts, ends);
        Range r = DatasetUtilities.iterateRangeBounds(d, false);
        assertEquals(4.0, r.getLowerBound(), EPSILON);
        assertEquals(16.0, r.getUpperBound(), EPSILON);
        r = DatasetUtilities.iterateRangeBounds(d, true);
        assertEquals(1.0, r.getLowerBound(), EPSILON);
        assertEquals(16.0, r.getUpperBound(), EPSILON);
    }

// org.jfree.data.general.junit.DatasetUtilitiesTests::testIterateRangeBounds
    public void testIterateRangeBounds() {
        XYDataset dataset = createXYDataset1();
        Range r = DatasetUtilities.iterateRangeBounds(dataset);
        assertEquals(100.0, r.getLowerBound(), EPSILON);
        assertEquals(105.0, r.getUpperBound(), EPSILON);
    }

// org.jfree.data.general.junit.DatasetUtilitiesTests::testIterateRangeBounds2
    public void testIterateRangeBounds2() {
        XYSeries s1 = new XYSeries("S1");
        s1.add(1.0, 1.1);
        s1.add(2.0, null);
        s1.add(3.0, 3.3);
        XYSeriesCollection dataset = new XYSeriesCollection(s1);
        Range r = DatasetUtilities.iterateRangeBounds(dataset);
        assertEquals(1.1, r.getLowerBound(), EPSILON);
        assertEquals(3.3, r.getUpperBound(), EPSILON);
    }

// org.jfree.data.general.junit.DatasetUtilitiesTests::testIterateRangeBounds3
    public void testIterateRangeBounds3() {
        
        XYSeriesCollection dataset = new XYSeriesCollection();
        Range r = DatasetUtilities.iterateRangeBounds(dataset);
        assertNull(r);
        XYSeries s1 = new XYSeries("S1");
        dataset.addSeries(s1);
        r = DatasetUtilities.iterateRangeBounds(dataset);
        assertNull(r);

        
        s1.add(1.0, 1.23);
        r = DatasetUtilities.iterateRangeBounds(dataset);
        assertEquals(1.23, r.getLowerBound(), EPSILON);
        assertEquals(1.23, r.getUpperBound(), EPSILON);

        
        s1.add(2.0, null);
        r = DatasetUtilities.iterateRangeBounds(dataset);
        assertEquals(1.23, r.getLowerBound(), EPSILON);
        assertEquals(1.23, r.getUpperBound(), EPSILON);

        
        s1.add(3.0, Double.NaN);
        r = DatasetUtilities.iterateRangeBounds(dataset);
        assertEquals(1.23, r.getLowerBound(), EPSILON);
        assertEquals(1.23, r.getUpperBound(), EPSILON);
    }

// org.jfree.data.general.junit.DatasetUtilitiesTests::testIterateRangeBounds4
    public void testIterateRangeBounds4() {
        YIntervalSeriesCollection dataset = new YIntervalSeriesCollection();
        Range r = DatasetUtilities.iterateRangeBounds(dataset);
        assertNull(r);
        YIntervalSeries s1 = new YIntervalSeries("S1");
        dataset.addSeries(s1);
        r = DatasetUtilities.iterateRangeBounds(dataset);
        assertNull(r);

        
        s1.add(1.0, 2.0, 1.5, 2.5);
        r = DatasetUtilities.iterateRangeBounds(dataset);
        assertEquals(1.5, r.getLowerBound(), EPSILON);
        assertEquals(2.5, r.getUpperBound(), EPSILON);

        
        s1.add(2.0, 2.0, 1.4, 2.1);
        r = DatasetUtilities.iterateRangeBounds(dataset);
        assertEquals(1.4, r.getLowerBound(), EPSILON);
        assertEquals(2.5, r.getUpperBound(), EPSILON);

        
        YIntervalSeries s2 = new YIntervalSeries("S2");
        dataset.addSeries(s2);
        r = DatasetUtilities.iterateRangeBounds(dataset);
        assertEquals(1.4, r.getLowerBound(), EPSILON);
        assertEquals(2.5, r.getUpperBound(), EPSILON);

        
        s2.add(1.0, 2.0, 1.9, 2.6);
        r = DatasetUtilities.iterateRangeBounds(dataset);
        assertEquals(1.4, r.getLowerBound(), EPSILON);
        assertEquals(2.6, r.getUpperBound(), EPSILON);
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

// org.jfree.data.general.junit.DatasetUtilitiesTests::testCumulativeRange3
    public void testCumulativeRange3() {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        dataset.addValue(15.76, "Product 1", "Labour");
        dataset.addValue(8.66, "Product 1", "Administration");
        dataset.addValue(4.71, "Product 1", "Marketing");
        dataset.addValue(3.51, "Product 1", "Distribution");
        dataset.addValue(32.64, "Product 1", "Total Expense");
        Range range = DatasetUtilities.findCumulativeRangeBounds(dataset);
        assertEquals(0.0, range.getLowerBound(), EPSILON);
        assertEquals(65.28, range.getUpperBound(), EPSILON);
    }

// org.jfree.data.general.junit.DatasetUtilitiesTests::testCumulativeRange_NaN
    public void testCumulativeRange_NaN() {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        dataset.addValue(10.0, "Series 1", "Start");
        dataset.addValue(15.0, "Series 1", "Delta 1");
        dataset.addValue(Double.NaN, "Series 1", "Delta 2");
        Range range = DatasetUtilities.findCumulativeRangeBounds(dataset);
        assertEquals(0.0, range.getLowerBound(), EPSILON);
        assertEquals(25.0, range.getUpperBound(), EPSILON);
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

// org.jfree.data.general.junit.DatasetUtilitiesTests::testFindStackedRangeBounds_CategoryDataset1
    public void testFindStackedRangeBounds_CategoryDataset1() {
        CategoryDataset d1 = createCategoryDataset1();
        Range r = DatasetUtilities.findStackedRangeBounds(d1);
        assertEquals(0.0, r.getLowerBound(), EPSILON);
        assertEquals(15.0, r.getUpperBound(), EPSILON);

        d1 = createCategoryDataset2();
        r = DatasetUtilities.findStackedRangeBounds(d1);
        assertEquals(-2.0, r.getLowerBound(), EPSILON);
        assertEquals(2.0, r.getUpperBound(), EPSILON);
    }

// org.jfree.data.general.junit.DatasetUtilitiesTests::testFindStackedRangeBounds_CategoryDataset2
    public void testFindStackedRangeBounds_CategoryDataset2() {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        Range r = DatasetUtilities.findStackedRangeBounds(dataset);
        assertTrue(r == null);

        dataset.addValue(5.0, "R1", "C1");
        r = DatasetUtilities.findStackedRangeBounds(dataset, 3.0);
        assertEquals(3.0, r.getLowerBound(), EPSILON);
        assertEquals(8.0, r.getUpperBound(), EPSILON);

        dataset.addValue(-1.0, "R2", "C1");
        r = DatasetUtilities.findStackedRangeBounds(dataset, 3.0);
        assertEquals(2.0, r.getLowerBound(), EPSILON);
        assertEquals(8.0, r.getUpperBound(), EPSILON);

        dataset.addValue(null, "R3", "C1");
        r = DatasetUtilities.findStackedRangeBounds(dataset, 3.0);
        assertEquals(2.0, r.getLowerBound(), EPSILON);
        assertEquals(8.0, r.getUpperBound(), EPSILON);

        dataset.addValue(Double.NaN, "R4", "C1");
        r = DatasetUtilities.findStackedRangeBounds(dataset, 3.0);
        assertEquals(2.0, r.getLowerBound(), EPSILON);
        assertEquals(8.0, r.getUpperBound(), EPSILON);
    }

// org.jfree.data.general.junit.DatasetUtilitiesTests::testFindStackedRangeBounds_CategoryDataset3
    public void testFindStackedRangeBounds_CategoryDataset3() {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        KeyToGroupMap map = new KeyToGroupMap("Group A");
        Range r = DatasetUtilities.findStackedRangeBounds(dataset, map);
        assertTrue(r == null);

        dataset.addValue(1.0, "R1", "C1");
        dataset.addValue(2.0, "R2", "C1");
        dataset.addValue(3.0, "R3", "C1");
        dataset.addValue(4.0, "R4", "C1");

        map.mapKeyToGroup("R1", "Group A");
        map.mapKeyToGroup("R2", "Group A");
        map.mapKeyToGroup("R3", "Group B");
        map.mapKeyToGroup("R4", "Group B");

        r = DatasetUtilities.findStackedRangeBounds(dataset, map);
        assertEquals(0.0, r.getLowerBound(), EPSILON);
        assertEquals(7.0, r.getUpperBound(), EPSILON);

        dataset.addValue(null, "R5", "C1");
        r = DatasetUtilities.findStackedRangeBounds(dataset, map);
        assertEquals(0.0, r.getLowerBound(), EPSILON);
        assertEquals(7.0, r.getUpperBound(), EPSILON);

        dataset.addValue(Double.NaN, "R6", "C1");
        r = DatasetUtilities.findStackedRangeBounds(dataset, map);
        assertEquals(0.0, r.getLowerBound(), EPSILON);
        assertEquals(7.0, r.getUpperBound(), EPSILON);
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

// org.jfree.data.general.junit.DatasetUtilitiesTests::testSampleFunction2D
    public void testSampleFunction2D() {
        Function2D f = new LineFunction2D(0, 1);
        XYDataset dataset = DatasetUtilities.sampleFunction2D(f, 0.0, 1.0, 2,
                "S1");
        assertEquals(1, dataset.getSeriesCount());
        assertEquals("S1", dataset.getSeriesKey(0));
        assertEquals(2, dataset.getItemCount(0));
        assertEquals(0.0, dataset.getXValue(0, 0), EPSILON);
        assertEquals(0.0, dataset.getYValue(0, 0), EPSILON);
        assertEquals(1.0, dataset.getXValue(0, 1), EPSILON);
        assertEquals(1.0, dataset.getYValue(0, 1), EPSILON);
    }

// org.jfree.data.general.junit.DatasetUtilitiesTests::testFindMinimumStackedRangeValue
    public void testFindMinimumStackedRangeValue() {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        
        Number min = DatasetUtilities.findMinimumStackedRangeValue(dataset);
        assertNull(min);

        dataset.addValue(1.0, "R1", "C1");
        min = DatasetUtilities.findMinimumStackedRangeValue(dataset);
        assertEquals(0.0, min.doubleValue(), EPSILON);

        dataset.addValue(2.0, "R2", "C1");
        min = DatasetUtilities.findMinimumStackedRangeValue(dataset);
        assertEquals(0.0, min.doubleValue(), EPSILON);

        dataset.addValue(-3.0, "R3", "C1");
        min = DatasetUtilities.findMinimumStackedRangeValue(dataset);
        assertEquals(-3.0, min.doubleValue(), EPSILON);

        dataset.addValue(Double.NaN, "R4", "C1");
        min = DatasetUtilities.findMinimumStackedRangeValue(dataset);
        assertEquals(-3.0, min.doubleValue(), EPSILON);
    }

// org.jfree.data.general.junit.DatasetUtilitiesTests::testFindMinimumStackedRangeValue2
    public void testFindMinimumStackedRangeValue2() {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        dataset.addValue(-1.0, "R1", "C1");
        Number min = DatasetUtilities.findMinimumStackedRangeValue(dataset);
        assertEquals(-1.0, min.doubleValue(), EPSILON);

        dataset.addValue(-2.0, "R2", "C1");
        min = DatasetUtilities.findMinimumStackedRangeValue(dataset);
        assertEquals(-3.0, min.doubleValue(), EPSILON);
    }

// org.jfree.data.general.junit.DatasetUtilitiesTests::testFindMaximumStackedRangeValue
    public void testFindMaximumStackedRangeValue() {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        
        Number max = DatasetUtilities.findMaximumStackedRangeValue(dataset);
        assertNull(max);

        dataset.addValue(1.0, "R1", "C1");
        max = DatasetUtilities.findMaximumStackedRangeValue(dataset);
        assertEquals(1.0, max.doubleValue(), EPSILON);

        dataset.addValue(2.0, "R2", "C1");
        max = DatasetUtilities.findMaximumStackedRangeValue(dataset);
        assertEquals(3.0, max.doubleValue(), EPSILON);

        dataset.addValue(-3.0, "R3", "C1");
        max = DatasetUtilities.findMaximumStackedRangeValue(dataset);
        assertEquals(3.0, max.doubleValue(), EPSILON);

        dataset.addValue(Double.NaN, "R4", "C1");
        max = DatasetUtilities.findMaximumStackedRangeValue(dataset);
        assertEquals(3.0, max.doubleValue(), EPSILON);
    }

// org.jfree.data.general.junit.DatasetUtilitiesTests::testFindMaximumStackedRangeValue2
    public void testFindMaximumStackedRangeValue2() {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        dataset.addValue(-1.0, "R1", "C1");
        Number max = DatasetUtilities.findMaximumStackedRangeValue(dataset);
        assertEquals(0.0, max.doubleValue(), EPSILON);

        dataset.addValue(-2.0, "R2", "C1");
        max = DatasetUtilities.findMaximumStackedRangeValue(dataset);
        assertEquals(0.0, max.doubleValue(), EPSILON);
    }

// org.jfree.data.statistics.junit.RegressionTests::testOLSRegression1a
    public void testOLSRegression1a() {

        double[][] data = createSampleData1();
        double[] result1 = Regression.getOLSRegression(data);
        assertEquals(.25680930, result1[0], 0.0000001);
        assertEquals(0.72792106, result1[1], 0.0000001);

    }

// org.jfree.data.statistics.junit.RegressionTests::testOLSRegression1b
    public void testOLSRegression1b() {

        double[][] data = createSampleData1();

        XYSeries series = new XYSeries("Test");
        for (int i = 0; i < 11; i++) {
            series.add(data[i][0], data[i][1]);
        }
        XYDataset ds = new XYSeriesCollection(series);
        double[] result2 = Regression.getOLSRegression(ds, 0);

        assertEquals(.25680930, result2[0], 0.0000001);
        assertEquals(0.72792106, result2[1], 0.0000001);

    }

// org.jfree.data.statistics.junit.RegressionTests::testPowerRegression1a
    public void testPowerRegression1a() {

        double[][] data = createSampleData1();
        double[] result = Regression.getPowerRegression(data);
        assertEquals(0.91045813, result[0], 0.0000001);
        assertEquals(0.88918346, result[1], 0.0000001);

    }

// org.jfree.data.statistics.junit.RegressionTests::testPowerRegression1b
    public void testPowerRegression1b() {

        double[][] data = createSampleData1();

        XYSeries series = new XYSeries("Test");
        for (int i = 0; i < 11; i++) {
            series.add(data[i][0], data[i][1]);
        }
        XYDataset ds = new XYSeriesCollection(series);
        double[] result = Regression.getPowerRegression(ds, 0);

        assertEquals(0.91045813, result[0], 0.0000001);
        assertEquals(0.88918346, result[1], 0.0000001);

    }

// org.jfree.data.statistics.junit.RegressionTests::testOLSRegression2a
    public void testOLSRegression2a() {

        double[][] data = createSampleData2();
        double[] result = Regression.getOLSRegression(data);
        assertEquals(53.9729697, result[0], 0.0000001);
        assertEquals(-4.1823030, result[1], 0.0000001);

    }

// org.jfree.data.statistics.junit.RegressionTests::testOLSRegression2b
    public void testOLSRegression2b() {

        double[][] data = createSampleData2();

        XYSeries series = new XYSeries("Test");
        for (int i = 0; i < 10; i++) {
            series.add(data[i][0], data[i][1]);
        }
        XYDataset ds = new XYSeriesCollection(series);
        double[] result = Regression.getOLSRegression(ds, 0);

        assertEquals(53.9729697, result[0], 0.0000001);
        assertEquals(-4.1823030, result[1], 0.0000001);

    }

// org.jfree.data.statistics.junit.RegressionTests::testPowerRegression2a
    public void testPowerRegression2a() {

        double[][] data = createSampleData2();
        double[] result = Regression.getPowerRegression(data);
        assertEquals(106.1241681, result[0], 0.0000001);
        assertEquals(-0.8466615, result[1], 0.0000001);

    }

// org.jfree.data.statistics.junit.RegressionTests::testPowerRegression2b
    public void testPowerRegression2b() {

        double[][] data = createSampleData2();

        XYSeries series = new XYSeries("Test");
        for (int i = 0; i < 10; i++) {
            series.add(data[i][0], data[i][1]);
        }
        XYDataset ds = new XYSeriesCollection(series);
        double[] result = Regression.getPowerRegression(ds, 0);

        assertEquals(106.1241681, result[0], 0.0000001);
        assertEquals(-0.8466615, result[1], 0.0000001);

    }

// org.jfree.data.xy.junit.DefaultTableXYDatasetTests::testEquals
    public void testEquals() {

        DefaultTableXYDataset d1 = new DefaultTableXYDataset();
        XYSeries s1 = new XYSeries("Series 1", true, false);
        s1.add(1.0, 1.1);
        s1.add(2.0, 2.2);
        d1.addSeries(s1);

        DefaultTableXYDataset d2 = new DefaultTableXYDataset();
        XYSeries s2 = new XYSeries("Series 1", true, false);
        s2.add(1.0, 1.1);
        s2.add(2.0, 2.2);
        d2.addSeries(s2);

        assertTrue(d1.equals(d2));
        assertTrue(d2.equals(d1));

        s1.add(3.0, 3.3);
        assertFalse(d1.equals(d2));

        s2.add(3.0, 3.3);
        assertTrue(d1.equals(d2));

    }

// org.jfree.data.xy.junit.DefaultTableXYDatasetTests::testCloning
    public void testCloning() {
        DefaultTableXYDataset d1 = new DefaultTableXYDataset();
        XYSeries s1 = new XYSeries("Series 1", true, false);
        s1.add(1.0, 1.1);
        s1.add(2.0, 2.2);
        d1.addSeries(s1);

        DefaultTableXYDataset d2 = null;
        try {
            d2 = (DefaultTableXYDataset) d1.clone();
        }
        catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        assertTrue(d1 != d2);
        assertTrue(d1.getClass() == d2.getClass());
        assertTrue(d1.equals(d2));

        s1.add(3.0, 3.3);
        assertFalse(d1.equals(d2));
    }

// org.jfree.data.xy.junit.DefaultTableXYDatasetTests::testPublicCloneable
    public void testPublicCloneable() {
        DefaultTableXYDataset d1 = new DefaultTableXYDataset();
        assertTrue(d1 instanceof PublicCloneable);
    }

// org.jfree.data.xy.junit.DefaultTableXYDatasetTests::testSerialization
    public void testSerialization() {

        DefaultTableXYDataset d1 = new DefaultTableXYDataset();
        XYSeries s1 = new XYSeries("Series 1", true, false);
        s1.add(1.0, 1.1);
        s1.add(2.0, 2.2);
        d1.addSeries(s1);

        DefaultTableXYDataset d2 = null;

        try {
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            ObjectOutput out = new ObjectOutputStream(buffer);
            out.writeObject(d1);
            out.close();

            ObjectInput in = new ObjectInputStream(
                    new ByteArrayInputStream(buffer.toByteArray()));
            d2 = (DefaultTableXYDataset) in.readObject();
            in.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        assertEquals(d1, d2);

    }

// org.jfree.data.xy.junit.DefaultTableXYDatasetTests::testAddSeries
    public void testAddSeries() {
        DefaultTableXYDataset d1 = new DefaultTableXYDataset();
        d1.setAutoWidth(true);
        XYSeries s1 = new XYSeries("Series 1", true, false);
        s1.add(3.0, 1.1);
        s1.add(7.0, 2.2);
        d1.addSeries(s1);
        assertEquals(3.0, d1.getXValue(0, 0), EPSILON);
        assertEquals(7.0, d1.getXValue(0, 1), EPSILON);
        assertEquals(1.0, d1.getStartXValue(0, 0), EPSILON);
        assertEquals(5.0, d1.getStartXValue(0, 1), EPSILON);
        assertEquals(5.0, d1.getEndXValue(0, 0), EPSILON);
        assertEquals(9.0, d1.getEndXValue(0, 1), EPSILON);

        
        XYSeries s2 = new XYSeries("Series 2", true, false);
        s2.add(7.5, 1.1);
        s2.add(9.0, 2.2);
        d1.addSeries(s2);

        assertEquals(3.0, d1.getXValue(1, 0), EPSILON);
        assertEquals(7.0, d1.getXValue(1, 1), EPSILON);
        assertEquals(7.5, d1.getXValue(1, 2), EPSILON);
        assertEquals(9.0, d1.getXValue(1, 3), EPSILON);

        assertEquals(7.25, d1.getStartXValue(1, 2), EPSILON);
        assertEquals(8.75, d1.getStartXValue(1, 3), EPSILON);
        assertEquals(7.75, d1.getEndXValue(1, 2), EPSILON);
        assertEquals(9.25, d1.getEndXValue(1, 3), EPSILON);

        
        assertEquals(2.75, d1.getStartXValue(0, 0), EPSILON);
        assertEquals(6.75, d1.getStartXValue(0, 1), EPSILON);
        assertEquals(3.25, d1.getEndXValue(0, 0), EPSILON);
        assertEquals(7.25, d1.getEndXValue(0, 1), EPSILON);
    }

// org.jfree.data.xy.junit.DefaultTableXYDatasetTests::testGetSeries
    public void testGetSeries() {
        DefaultTableXYDataset d1 = new DefaultTableXYDataset();
        XYSeries s1 = new XYSeries("Series 1", true, false);
        d1.addSeries(s1);
        assertEquals("Series 1", d1.getSeries(0).getKey());

        boolean pass = false;
        try {
            d1.getSeries(-1);
        }
        catch (IllegalArgumentException e) {
            pass = true;
        }
        assertTrue(pass);

        pass = false;
        try {
            d1.getSeries(1);
        }
        catch (IllegalArgumentException e) {
            pass = true;
        }
        assertTrue(pass);
    }

// org.jfree.data.xy.junit.IntervalXYDelegateTests::testEquals
    public void testEquals() {
       XYSeries s1 = new XYSeries("Series");
       s1.add(1.2, 3.4);
       XYSeriesCollection c1 = new XYSeriesCollection();
       c1.addSeries(s1);
       IntervalXYDelegate d1 = new IntervalXYDelegate(c1);

       XYSeries s2 = new XYSeries("Series");
       XYSeriesCollection c2 = new XYSeriesCollection();
       s2.add(1.2, 3.4);
       c2.addSeries(s2);
       IntervalXYDelegate d2 = new IntervalXYDelegate(c2);

       assertTrue(d1.equals(d2));
       assertTrue(d2.equals(d1));

       d1.setAutoWidth(false);
       assertFalse(d1.equals(d2));
       d2.setAutoWidth(false);
       assertTrue(d1.equals(d2));

       d1.setIntervalPositionFactor(0.123);
       assertFalse(d1.equals(d2));
       d2.setIntervalPositionFactor(0.123);
       assertTrue(d1.equals(d2));

       d1.setFixedIntervalWidth(1.23);
       assertFalse(d1.equals(d2));
       d2.setFixedIntervalWidth(1.23);
       assertTrue(d1.equals(d2));
    }

// org.jfree.data.xy.junit.IntervalXYDelegateTests::testCloning
    public void testCloning() {
        XYSeries s1 = new XYSeries("Series");
        s1.add(1.2, 3.4);
        XYSeriesCollection c1 = new XYSeriesCollection();
        c1.addSeries(s1);
        IntervalXYDelegate d1 = new IntervalXYDelegate(c1);

        IntervalXYDelegate d2 = null;
        try {
            d2 = (IntervalXYDelegate) d1.clone();
        }
        catch (CloneNotSupportedException e) {
            System.err.println("Failed to clone.");
        }
        assertTrue(d1 != d2);
        assertTrue(d1.getClass() == d2.getClass());
        assertTrue(d1.equals(d2));
    }

// org.jfree.data.xy.junit.IntervalXYDelegateTests::testSerialization
    public void testSerialization() {
        XYSeries s1 = new XYSeries("Series");
        s1.add(1.2, 3.4);
        XYSeriesCollection c1 = new XYSeriesCollection();
        c1.addSeries(s1);
        IntervalXYDelegate d1 = new IntervalXYDelegate(c1);
        IntervalXYDelegate d2 = null;
        try {
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            ObjectOutput out = new ObjectOutputStream(buffer);
            out.writeObject(d1);
            out.close();

            ObjectInput in = new ObjectInputStream(
                new ByteArrayInputStream(buffer.toByteArray())
            );
            d2 = (IntervalXYDelegate) in.readObject();
            in.close();
        }
        catch (Exception e) {
            System.out.println(e.toString());
        }
        assertEquals(d1, d2);

    }

// org.jfree.data.xy.junit.TableXYDatasetTests::testEquals
    public void testEquals() {

        DefaultTableXYDataset d1 = new DefaultTableXYDataset();
        DefaultTableXYDataset d2 = new DefaultTableXYDataset();
        assertTrue(d1.equals(d2));
        assertTrue(d2.equals(d1));

        d1.addSeries(createSeries1());
        assertFalse(d1.equals(d2));

        d2.addSeries(createSeries1());
        assertTrue(d1.equals(d2));

    }

// org.jfree.data.xy.junit.TableXYDatasetTests::testCloning
    public void testCloning() {
        DefaultTableXYDataset d1 = new DefaultTableXYDataset();
        d1.addSeries(createSeries1());
        DefaultTableXYDataset d2 = null;
        try {
            d2 = (DefaultTableXYDataset) d1.clone();
        }
        catch (CloneNotSupportedException e) {
            System.err.println("Failed to clone.");
        }
        assertTrue(d1 != d2);
        assertTrue(d1.getClass() == d2.getClass());
        assertTrue(d1.equals(d2));
    }

// org.jfree.data.xy.junit.TableXYDatasetTests::testPublicCloneable
    public void testPublicCloneable() {
        DefaultTableXYDataset d1 = new DefaultTableXYDataset();
        assertTrue(d1 instanceof PublicCloneable);
    }

// org.jfree.data.xy.junit.TableXYDatasetTests::testSerialization
    public void testSerialization() {

        DefaultTableXYDataset d1 = new DefaultTableXYDataset();
        d1.addSeries(createSeries2());
        DefaultTableXYDataset d2 = null;

        try {
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            ObjectOutput out = new ObjectOutputStream(buffer);
            out.writeObject(d1);
            out.close();

            ObjectInput in = new ObjectInputStream(
                new ByteArrayInputStream(buffer.toByteArray())
            );
            d2 = (DefaultTableXYDataset) in.readObject();
            in.close();
        }
        catch (Exception e) {
            System.out.println(e.toString());
        }
        assertEquals(d1, d2);

    }

// org.jfree.data.xy.junit.TableXYDatasetTests::testTableXYDataset
    public void testTableXYDataset() {

        XYSeries series1 = createSeries1();
        XYSeries series2 = createSeries2();

        DefaultTableXYDataset dataset = new DefaultTableXYDataset();
        dataset.addSeries(series1);
        dataset.addSeries(series2);

        
        assertEquals(6, dataset.getItemCount());
        assertEquals(6, dataset.getX(0, 5).intValue());
        assertEquals(null, dataset.getY(0, 5));
        assertEquals(6, dataset.getX(1, 5).intValue());
        assertEquals(2, dataset.getY(1, 5).intValue());

        
        
        series2.add(7, 2);
        assertEquals(7, dataset.getItemCount());
        assertEquals(null, dataset.getY(0, 6));
        assertEquals(2, dataset.getY(1, 6).intValue());

        
        dataset.removeSeries(series1);
        
        assertEquals(7, dataset.getItemCount());

        
        dataset.removeSeries(series2);
        series1 = createSeries1();
        dataset.addSeries(series1);

        
        assertEquals(4, dataset.getItemCount());

    }

// org.jfree.data.xy.junit.TableXYDatasetTests::test788597
    public void test788597() {
        DefaultTableXYDataset dataset = new DefaultTableXYDataset();
        dataset.addSeries(createSeries1());
        assertEquals(4, dataset.getItemCount());
        dataset.removeAllSeries();
        assertEquals(0, dataset.getItemCount());
    }

// org.jfree.data.xy.junit.TableXYDatasetTests::testRemoveAllValuesForX
    public void testRemoveAllValuesForX() {
        DefaultTableXYDataset dataset = new DefaultTableXYDataset();
        dataset.addSeries(createSeries1());
        dataset.addSeries(createSeries2());
        dataset.removeAllValuesForX(new Double(2.0));
        assertEquals(5, dataset.getItemCount());
        assertEquals(new Double(1.0), dataset.getX(0, 0));
        assertEquals(new Double(3.0), dataset.getX(0, 1));
        assertEquals(new Double(4.0), dataset.getX(0, 2));
        assertEquals(new Double(5.0), dataset.getX(0, 3));
        assertEquals(new Double(6.0), dataset.getX(0, 4));
    }

// org.jfree.data.xy.junit.TableXYDatasetTests::testPrune
    public void testPrune() {
        DefaultTableXYDataset dataset = new DefaultTableXYDataset();
        dataset.addSeries(createSeries1());
        dataset.addSeries(createSeries2());
        dataset.removeSeries(1);
        dataset.prune();
        assertEquals(4, dataset.getItemCount());
    }

// org.jfree.data.xy.junit.TableXYDatasetTests::testAutoPrune
    public void testAutoPrune() {

        
        DefaultTableXYDataset dataset = new DefaultTableXYDataset(true);
        dataset.addSeries(createSeriesA());
        assertEquals(2, dataset.getItemCount());  
        dataset.addSeries(createSeriesB());
        assertEquals(2, dataset.getItemCount());  
        dataset.removeSeries(1);
        assertEquals(1, dataset.getItemCount());  

        
        DefaultTableXYDataset dataset2 = new DefaultTableXYDataset(true);
        dataset2.addSeries(createSeriesA());
        assertEquals(2, dataset2.getItemCount());  
        dataset2.addSeries(createSeriesB());
        assertEquals(2, dataset2.getItemCount());  
        dataset2.removeSeries(1);
        assertEquals(1, dataset2.getItemCount());  

    }

// org.jfree.data.xy.junit.XYSeriesCollectionTests::testEquals
    public void testEquals() {
        XYSeries s1 = new XYSeries("Series");
        s1.add(1.0, 1.1);
        XYSeriesCollection c1 = new XYSeriesCollection();
        c1.addSeries(s1);
        XYSeries s2 = new XYSeries("Series");
        s2.add(1.0, 1.1);
        XYSeriesCollection c2 = new XYSeriesCollection();
        c2.addSeries(s2);
        assertTrue(c1.equals(c2));
        assertTrue(c2.equals(c1));

        c1.addSeries(new XYSeries("Empty Series"));
        assertFalse(c1.equals(c2));

        c2.addSeries(new XYSeries("Empty Series"));
        assertTrue(c1.equals(c2));
    }

// org.jfree.data.xy.junit.XYSeriesCollectionTests::testCloning
    public void testCloning() {
        XYSeries s1 = new XYSeries("Series");
        s1.add(1.0, 1.1);
        XYSeriesCollection c1 = new XYSeriesCollection();
        c1.addSeries(s1);
        XYSeriesCollection c2 = null;
        try {
            c2 = (XYSeriesCollection) c1.clone();
        }
        catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        assertTrue(c1 != c2);
        assertTrue(c1.getClass() == c2.getClass());
        assertTrue(c1.equals(c2));

        
        s1.setDescription("XYZ");
        assertFalse(c1.equals(c2));
    }

// org.jfree.data.xy.junit.XYSeriesCollectionTests::testPublicCloneable
    public void testPublicCloneable() {
        XYSeriesCollection c1 = new XYSeriesCollection();
        assertTrue(c1 instanceof PublicCloneable);
    }

// org.jfree.data.xy.junit.XYSeriesCollectionTests::testSerialization
    public void testSerialization() {
        XYSeries s1 = new XYSeries("Series");
        s1.add(1.0, 1.1);
        XYSeriesCollection c1 = new XYSeriesCollection();
        c1.addSeries(s1);
        XYSeriesCollection c2 = null;

        try {
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            ObjectOutput out = new ObjectOutputStream(buffer);
            out.writeObject(c1);
            out.close();

            ObjectInput in = new ObjectInputStream(
                    new ByteArrayInputStream(buffer.toByteArray()));
            c2 = (XYSeriesCollection) in.readObject();
            in.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        assertEquals(c1, c2);
    }

// org.jfree.data.xy.junit.XYSeriesCollectionTests::test1170825
    public void test1170825() {
        XYSeries s1 = new XYSeries("Series1");
        XYSeriesCollection dataset = new XYSeriesCollection();
        dataset.addSeries(s1);
        try {
             dataset.getSeries(1);
        }
        catch (IllegalArgumentException e) {
            
        }
        catch (IndexOutOfBoundsException e) {
            assertTrue(false);  
        }
    }

// org.jfree.data.xy.junit.XYSeriesCollectionTests::testGetSeries
    public void testGetSeries() {
        XYSeriesCollection c = new XYSeriesCollection();
        XYSeries s1 = new XYSeries("s1");
        c.addSeries(s1);
        assertEquals("s1", c.getSeries(0).getKey());

        boolean pass = false;
        try {
            c.getSeries(-1);
        }
        catch (IllegalArgumentException e) {
            pass = true;
        }
        assertTrue(pass);

        pass = false;
        try {
            c.getSeries(1);
        }
        catch (IllegalArgumentException e) {
            pass = true;
        }
        assertTrue(pass);
    }

// org.jfree.data.xy.junit.XYSeriesCollectionTests::testGetSeriesByKey
    public void testGetSeriesByKey() {
        XYSeriesCollection c = new XYSeriesCollection();
        XYSeries s1 = new XYSeries("s1");
        c.addSeries(s1);
        assertEquals("s1", c.getSeries("s1").getKey());

        boolean pass = false;
        try {
            c.getSeries("s2");
        }
        catch (UnknownKeyException e) {
            pass = true;
        }
        assertTrue(pass);

        pass = false;
        try {
            c.getSeries(null);
        }
        catch (IllegalArgumentException e) {
            pass = true;
        }
        assertTrue(pass);
    }

// org.jfree.data.xy.junit.XYSeriesCollectionTests::testRemoveSeries
    public void testRemoveSeries() {
        XYSeriesCollection c = new XYSeriesCollection();
        XYSeries s1 = new XYSeries("s1");
        c.addSeries(s1);
        c.removeSeries(0);
        assertEquals(0, c.getSeriesCount());
        c.addSeries(s1);

        boolean pass = false;
        try {
            c.removeSeries(-1);
        }
        catch (IllegalArgumentException e) {
            pass = true;
        }
        assertTrue(pass);

        pass = false;
        try {
            c.removeSeries(1);
        }
        catch (IllegalArgumentException e) {
            pass = true;
        }
        assertTrue(pass);
    }

// org.jfree.data.xy.junit.XYSeriesCollectionTests::testIndexOf
    public void testIndexOf() {
        XYSeries s1 = new XYSeries("S1");
        XYSeries s2 = new XYSeries("S2");
        XYSeriesCollection dataset = new XYSeriesCollection();
        assertEquals(-1, dataset.indexOf(s1));
        assertEquals(-1, dataset.indexOf(s2));

        dataset.addSeries(s1);
        assertEquals(0, dataset.indexOf(s1));
        assertEquals(-1, dataset.indexOf(s2));

        dataset.addSeries(s2);
        assertEquals(0, dataset.indexOf(s1));
        assertEquals(1, dataset.indexOf(s2));

        dataset.removeSeries(s1);
        assertEquals(-1, dataset.indexOf(s1));
        assertEquals(0, dataset.indexOf(s2));

        XYSeries s2b = new XYSeries("S2");
        assertEquals(0, dataset.indexOf(s2b));
    }

// org.jfree.data.xy.junit.XYSeriesTests::testEquals
    public void testEquals() {

        XYSeries s1 = new XYSeries("Series");
        s1.add(1.0, 1.1);
        XYSeries s2 = new XYSeries("Series");
        s2.add(1.0, 1.1);
        assertTrue(s1.equals(s2));
        assertTrue(s2.equals(s1));

        s1.setKey("Series X");
        assertFalse(s1.equals(s2));

        s2.setKey("Series X");
        assertTrue(s1.equals(s2));

    }

// org.jfree.data.xy.junit.XYSeriesTests::testHashCode
    public void testHashCode() {
        XYSeries s1 = new XYSeries("Test");
        XYSeries s2 = new XYSeries("Test");
        assertEquals(s1, s2);
        assertEquals(s1.hashCode(), s2.hashCode());

        s1.add(1.0, 500.0);
        s2.add(1.0, 500.0);
        assertEquals(s1, s2);
        assertEquals(s1.hashCode(), s2.hashCode());

        s1.add(2.0, null);
        s2.add(2.0, null);
        assertEquals(s1, s2);
        assertEquals(s1.hashCode(), s2.hashCode());

        s1.add(5.0, 111.0);
        s2.add(5.0, 111.0);
        assertEquals(s1, s2);
        assertEquals(s1.hashCode(), s2.hashCode());

        s1.add(9.0, 1.0);
        s2.add(9.0, 1.0);
        assertEquals(s1, s2);
        assertEquals(s1.hashCode(), s2.hashCode());
    }

// org.jfree.data.xy.junit.XYSeriesTests::testCloning
    public void testCloning() {
        XYSeries s1 = new XYSeries("Series");
        s1.add(1.0, 1.1);
        XYSeries s2 = null;
        try {
            s2 = (XYSeries) s1.clone();
        }
        catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        assertTrue(s1 != s2);
        assertTrue(s1.getClass() == s2.getClass());
        assertTrue(s1.equals(s2));
    }

// org.jfree.data.xy.junit.XYSeriesTests::testCloning2
    public void testCloning2() {
        XYSeries s1 = new XYSeries("S1");
        s1.add(1.0, 100.0);
        s1.add(2.0, null);
        s1.add(3.0, 200.0);
        XYSeries s2 = null;
        try {
            s2 = (XYSeries) s1.clone();
        }
        catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        assertTrue(s1.equals(s2));

        
        s2.add(4.0, 300.0);
        assertFalse(s1.equals(s2));
        s1.add(4.0, 300.0);
        assertTrue(s1.equals(s2));
    }

// org.jfree.data.xy.junit.XYSeriesTests::testCloning3
    public void testCloning3() {
        XYSeries s1 = new XYSeries("S1");
        XYSeries s2 = null;
        try {
            s2 = (XYSeries) s1.clone();
        }
        catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        assertTrue(s1.equals(s2));

        
        s2.add(4.0, 300.0);
        assertFalse(s1.equals(s2));
        s1.add(4.0, 300.0);
        assertTrue(s1.equals(s2));
    }

// org.jfree.data.xy.junit.XYSeriesTests::testSerialization
    public void testSerialization() {

        XYSeries s1 = new XYSeries("Series");
        s1.add(1.0, 1.1);
        XYSeries s2 = null;

        try {
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            ObjectOutput out = new ObjectOutputStream(buffer);
            out.writeObject(s1);
            out.close();

            ObjectInput in = new ObjectInputStream(
                    new ByteArrayInputStream(buffer.toByteArray()));
            s2 = (XYSeries) in.readObject();
            in.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        assertEquals(s1, s2);

    }

// org.jfree.data.xy.junit.XYSeriesTests::testIndexOf
    public void testIndexOf() {
        XYSeries s1 = new XYSeries("Series 1");
        s1.add(1.0, 1.0);
        s1.add(2.0, 2.0);
        s1.add(3.0, 3.0);
        assertEquals(0, s1.indexOf(new Double(1.0)));
    }

// org.jfree.data.xy.junit.XYSeriesTests::testIndexOf2
    public void testIndexOf2() {
        XYSeries s1 = new XYSeries("Series 1", false, true);
        s1.add(1.0, 1.0);
        s1.add(3.0, 3.0);
        s1.add(2.0, 2.0);
        assertEquals(0, s1.indexOf(new Double(1.0)));
        assertEquals(1, s1.indexOf(new Double(3.0)));
        assertEquals(2, s1.indexOf(new Double(2.0)));
    }

// org.jfree.data.xy.junit.XYSeriesTests::testRemove
    public void testRemove() {

        XYSeries s1 = new XYSeries("Series 1");
        s1.add(1.0, 1.0);
        s1.add(2.0, 2.0);
        s1.add(3.0, 3.0);

        assertEquals(3, s1.getItemCount());
        s1.remove(new Double(2.0));
        assertEquals(new Double(3.0), s1.getX(1));

        s1.remove(0);
        assertEquals(new Double(3.0), s1.getX(0));

    }

// org.jfree.data.xy.junit.XYSeriesTests::testAdditionOfDuplicateXValues
    public void testAdditionOfDuplicateXValues() {
        XYSeries s1 = new XYSeries("Series 1");
        s1.add(1.0, 1.0);
        s1.add(2.0, 2.0);
        s1.add(2.0, 3.0);
        s1.add(2.0, 4.0);
        s1.add(3.0, 5.0);
        assertEquals(1.0, s1.getY(0).doubleValue(), EPSILON);
        assertEquals(2.0, s1.getY(1).doubleValue(), EPSILON);
        assertEquals(3.0, s1.getY(2).doubleValue(), EPSILON);
        assertEquals(4.0, s1.getY(3).doubleValue(), EPSILON);
        assertEquals(5.0, s1.getY(4).doubleValue(), EPSILON);
    }

// org.jfree.data.xy.junit.XYSeriesTests::testUpdate
    public void testUpdate() {
        XYSeries series = new XYSeries("S1");
        series.add(new Integer(1), new Integer(2));
        assertEquals(new Integer(2), series.getY(0));
        series.update(new Integer(1), new Integer(3));
        assertEquals(new Integer(3), series.getY(0));
        try {
            series.update(new Integer(2), new Integer(99));
            assertTrue(false);
        }
        catch (SeriesException e) {
            
        }
    }

// org.jfree.data.xy.junit.XYSeriesTests::testUpdate2
    public void testUpdate2() {
       XYSeries series = new XYSeries("Series", false, true);
       series.add(5.0, 55.0);
       series.add(4.0, 44.0);
       series.add(6.0, 66.0);
       series.update(new Double(4.0), new Double(99.0));
       assertEquals(new Double(99.0), series.getY(1));
    }

// org.jfree.data.xy.junit.XYSeriesTests::testAddOrUpdate
    public void testAddOrUpdate() {
        XYSeries series = new XYSeries("S1", true, false);
        XYDataItem old = series.addOrUpdate(new Long(1), new Long(2));
        assertTrue(old == null);
        assertEquals(1, series.getItemCount());
        assertEquals(new Long(2), series.getY(0));

        old = series.addOrUpdate(new Long(2), new Long(3));
        assertTrue(old == null);
        assertEquals(2, series.getItemCount());
        assertEquals(new Long(3), series.getY(1));

        old = series.addOrUpdate(new Long(1), new Long(99));
        assertEquals(new XYDataItem(new Long(1), new Long(2)), old);
        assertEquals(2, series.getItemCount());
        assertEquals(new Long(99), series.getY(0));
        assertEquals(new Long(3), series.getY(1));
    }

// org.jfree.data.xy.junit.XYSeriesTests::testAddOrUpdate2
    public void testAddOrUpdate2() {
        XYSeries series = new XYSeries("Series", false, false);
        series.add(5.0, 5.5);
        series.add(6.0, 6.6);
        series.add(3.0, 3.3);
        series.add(4.0, 4.4);
        series.add(2.0, 2.2);
        series.add(1.0, 1.1);
        series.addOrUpdate(new Double(3.0), new Double(33.3));
        series.addOrUpdate(new Double(2.0), new Double(22.2));
        assertEquals(33.3, series.getY(2).doubleValue(), EPSILON);
        assertEquals(22.2, series.getY(4).doubleValue(), EPSILON);
    }

// org.jfree.data.xy.junit.XYSeriesTests::testAddOrUpdate3
    public void testAddOrUpdate3() {
        XYSeries series = new XYSeries("Series", false, true);
        series.addOrUpdate(1.0, 1.0);
        series.addOrUpdate(1.0, 2.0);
        series.addOrUpdate(1.0, 3.0);
        assertEquals(new Double(1.0), series.getY(0));
        assertEquals(new Double(2.0), series.getY(1));
        assertEquals(new Double(3.0), series.getY(2));
        assertEquals(3, series.getItemCount());
    }

// org.jfree.data.xy.junit.XYSeriesTests::testAdd
    public void testAdd() {
        XYSeries series = new XYSeries("Series", false, true);
        series.add(5.0, 5.50);
        series.add(5.1, 5.51);
        series.add(6.0, 6.6);
        series.add(3.0, 3.3);
        series.add(4.0, 4.4);
        series.add(2.0, 2.2);
        series.add(1.0, 1.1);
        assertEquals(5.5, series.getY(0).doubleValue(), EPSILON);
        assertEquals(5.51, series.getY(1).doubleValue(), EPSILON);
        assertEquals(6.6, series.getY(2).doubleValue(), EPSILON);
        assertEquals(3.3, series.getY(3).doubleValue(), EPSILON);
        assertEquals(4.4, series.getY(4).doubleValue(), EPSILON);
        assertEquals(2.2, series.getY(5).doubleValue(), EPSILON);
        assertEquals(1.1, series.getY(6).doubleValue(), EPSILON);
    }

// org.jfree.data.xy.junit.XYSeriesTests::testSetMaximumItemCount
    public void testSetMaximumItemCount() {
        XYSeries s1 = new XYSeries("S1");
        assertEquals(Integer.MAX_VALUE, s1.getMaximumItemCount());
        s1.setMaximumItemCount(2);
        assertEquals(2, s1.getMaximumItemCount());
        s1.add(1.0, 1.1);
        s1.add(2.0, 2.2);
        s1.add(3.0, 3.3);
        assertEquals(2.0, s1.getX(0).doubleValue(), EPSILON);
        assertEquals(3.0, s1.getX(1).doubleValue(), EPSILON);
    }

// org.jfree.data.xy.junit.XYSeriesTests::testSetMaximumItemCount2
    public void testSetMaximumItemCount2() {
        XYSeries s1 = new XYSeries("S1");
        s1.add(1.0, 1.1);
        s1.add(2.0, 2.2);
        s1.add(3.0, 3.3);
        s1.setMaximumItemCount(2);
        assertEquals(2.0, s1.getX(0).doubleValue(), EPSILON);
        assertEquals(3.0, s1.getX(1).doubleValue(), EPSILON);
    }

// org.jfree.data.xy.junit.XYSeriesTests::testToArray
    public void testToArray() {
        XYSeries s = new XYSeries("S1");
        double[][] array = s.toArray();
        assertEquals(2, array.length);
        assertEquals(0, array[0].length);
        assertEquals(0, array[1].length);

        s.add(1.0, 2.0);
        array = s.toArray();
        assertEquals(1, array[0].length);
        assertEquals(1, array[1].length);
        assertEquals(2, array.length);
        assertEquals(1.0, array[0][0], EPSILON);
        assertEquals(2.0, array[1][0], EPSILON);

        s.add(2.0, null);
        array = s.toArray();
        assertEquals(2, array.length);
        assertEquals(2, array[0].length);
        assertEquals(2, array[1].length);
        assertEquals(2.0, array[0][1], EPSILON);
        assertTrue(Double.isNaN(array[1][1]));
    }

// org.jfree.data.xy.junit.XYSeriesTests::testToArrayExample
    public void testToArrayExample() {
        XYSeries s = new XYSeries("S");
        s.add(1.0, 11.0);
        s.add(2.0, 22.0);
        s.add(3.5, 35.0);
        s.add(5.0, null);
        DefaultXYDataset dataset = new DefaultXYDataset();
        dataset.addSeries("S", s.toArray());
        assertEquals(1, dataset.getSeriesCount());
        assertEquals(4, dataset.getItemCount(0));
        assertEquals("S", dataset.getSeriesKey(0));
        assertEquals(1.0, dataset.getXValue(0, 0), EPSILON);
        assertEquals(2.0, dataset.getXValue(0, 1), EPSILON);
        assertEquals(3.5, dataset.getXValue(0, 2), EPSILON);
        assertEquals(5.0, dataset.getXValue(0, 3), EPSILON);
        assertEquals(11.0, dataset.getYValue(0, 0), EPSILON);
        assertEquals(22.0, dataset.getYValue(0, 1), EPSILON);
        assertEquals(35.0, dataset.getYValue(0, 2), EPSILON);
        assertTrue(Double.isNaN(dataset.getYValue(0, 3)));
    }

// org.jfree.data.xy.junit.XYSeriesTests::testBug1955483
    public void testBug1955483() {
        XYSeries series = new XYSeries("Series", true, true);
        series.addOrUpdate(1.0, 1.0);
        series.addOrUpdate(1.0, 2.0);
        assertEquals(new Double(1.0), series.getY(0));
        assertEquals(new Double(2.0), series.getY(1));
        assertEquals(2, series.getItemCount());
    }

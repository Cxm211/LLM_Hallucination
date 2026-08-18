// buggy code
    public static Range iterateDomainBounds(XYDataset dataset,
                                            boolean includeInterval) {
        if (dataset == null) {
            throw new IllegalArgumentException("Null 'dataset' argument.");
        }
        double minimum = Double.POSITIVE_INFINITY;
        double maximum = Double.NEGATIVE_INFINITY;
        int seriesCount = dataset.getSeriesCount();
        double lvalue;
        double uvalue;
        if (includeInterval && dataset instanceof IntervalXYDataset) {
            IntervalXYDataset intervalXYData = (IntervalXYDataset) dataset;
            for (int series = 0; series < seriesCount; series++) {
                int itemCount = dataset.getItemCount(series);
                for (int item = 0; item < itemCount; item++) {
                    lvalue = intervalXYData.getStartXValue(series, item);
                    uvalue = intervalXYData.getEndXValue(series, item);
                    if (!Double.isNaN(lvalue)) {
                        minimum = Math.min(minimum, lvalue);
                    }
                    if (!Double.isNaN(uvalue)) {
                        maximum = Math.max(maximum, uvalue);
                    }
                }
            }
        }
        else {
            for (int series = 0; series < seriesCount; series++) {
                int itemCount = dataset.getItemCount(series);
                for (int item = 0; item < itemCount; item++) {
                    lvalue = dataset.getXValue(series, item);
                    uvalue = lvalue;
                    if (!Double.isNaN(lvalue)) {
                        minimum = Math.min(minimum, lvalue);
                        maximum = Math.max(maximum, uvalue);
                    }
                }
            }
        }
        if (minimum > maximum) {
            return null;
        }
        else {
            return new Range(minimum, maximum);
        }
    }

    public static Range iterateRangeBounds(XYDataset dataset,
            boolean includeInterval) {
        double minimum = Double.POSITIVE_INFINITY;
        double maximum = Double.NEGATIVE_INFINITY;
        int seriesCount = dataset.getSeriesCount();

        // handle three cases by dataset type
        if (includeInterval && dataset instanceof IntervalXYDataset) {
            // handle special case of IntervalXYDataset
            IntervalXYDataset ixyd = (IntervalXYDataset) dataset;
            for (int series = 0; series < seriesCount; series++) {
                int itemCount = dataset.getItemCount(series);
                for (int item = 0; item < itemCount; item++) {
                    double lvalue = ixyd.getStartYValue(series, item);
                    double uvalue = ixyd.getEndYValue(series, item);
                    if (!Double.isNaN(lvalue)) {
                        minimum = Math.min(minimum, lvalue);
                    }
                    if (!Double.isNaN(uvalue)) {
                        maximum = Math.max(maximum, uvalue);
                    }
                }
            }
        }
        else if (includeInterval && dataset instanceof OHLCDataset) {
            // handle special case of OHLCDataset
            OHLCDataset ohlc = (OHLCDataset) dataset;
            for (int series = 0; series < seriesCount; series++) {
                int itemCount = dataset.getItemCount(series);
                for (int item = 0; item < itemCount; item++) {
                    double lvalue = ohlc.getLowValue(series, item);
                    double uvalue = ohlc.getHighValue(series, item);
                    if (!Double.isNaN(lvalue)) {
                        minimum = Math.min(minimum, lvalue);
                    }
                    if (!Double.isNaN(uvalue)) {
                        maximum = Math.max(maximum, uvalue);
                    }
                }
            }
        }
        else {
            // standard case - plain XYDataset
            for (int series = 0; series < seriesCount; series++) {
                int itemCount = dataset.getItemCount(series);
                for (int item = 0; item < itemCount; item++) {
                    double value = dataset.getYValue(series, item);
                    if (!Double.isNaN(value)) {
                        minimum = Math.min(minimum, value);
                        maximum = Math.max(maximum, value);
                    }
                }
            }
        }
        if (minimum == Double.POSITIVE_INFINITY) {
            return null;
        }
        else {
            return new Range(minimum, maximum);
        }
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

// org.jfree.chart.annotations.junit.XYTitleAnnotationTests::testEquals
    public void testEquals() {
        TextTitle t = new TextTitle("Title");
        XYTitleAnnotation a1 = new XYTitleAnnotation(1.0, 2.0, t);
        XYTitleAnnotation a2 = new XYTitleAnnotation(1.0, 2.0, t);
        assertTrue(a1.equals(a2));

        a1 = new XYTitleAnnotation(1.1, 2.0, t);
        assertFalse(a1.equals(a2));
        a2 = new XYTitleAnnotation(1.1, 2.0, t);
        assertTrue(a1.equals(a2));

        a1 = new XYTitleAnnotation(1.1, 2.2, t);
        assertFalse(a1.equals(a2));
        a2 = new XYTitleAnnotation(1.1, 2.2, t);
        assertTrue(a1.equals(a2));

        TextTitle t2 = new TextTitle("Title 2");
        a1 = new XYTitleAnnotation(1.1, 2.2, t2);
        assertFalse(a1.equals(a2));
        a2 = new XYTitleAnnotation(1.1, 2.2, t2);
        assertTrue(a1.equals(a2));
    }

// org.jfree.chart.annotations.junit.XYTitleAnnotationTests::testHashCode
    public void testHashCode() {
        TextTitle t = new TextTitle("Title");
        XYTitleAnnotation a1 = new XYTitleAnnotation(1.0, 2.0, t);
        XYTitleAnnotation a2 = new XYTitleAnnotation(1.0, 2.0, t);
        assertTrue(a1.equals(a2));
        int h1 = a1.hashCode();
        int h2 = a2.hashCode();
        assertEquals(h1, h2);
    }

// org.jfree.chart.annotations.junit.XYTitleAnnotationTests::testCloning
    public void testCloning() {
        TextTitle t = new TextTitle("Title");
        XYTitleAnnotation a1 = new XYTitleAnnotation(1.0, 2.0, t);
        XYTitleAnnotation a2 = null;
        try {
            a2 = (XYTitleAnnotation) a1.clone();
        }
        catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        assertTrue(a1 != a2);
        assertTrue(a1.getClass() == a2.getClass());
        assertTrue(a1.equals(a2));
    }

// org.jfree.chart.annotations.junit.XYTitleAnnotationTests::testSerialization
    public void testSerialization() {
        TextTitle t = new TextTitle("Title");
        XYTitleAnnotation a1 = new XYTitleAnnotation(1.0, 2.0, t);
        XYTitleAnnotation a2 = null;
        try {
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            ObjectOutput out = new ObjectOutputStream(buffer);
            out.writeObject(a1);
            out.close();

            ObjectInput in = new ObjectInputStream(new ByteArrayInputStream(
                    buffer.toByteArray()));
            a2 = (XYTitleAnnotation) in.readObject();
            in.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        assertEquals(a1, a2);
    }

// org.jfree.chart.annotations.junit.XYTitleAnnotationTests::testDrawWithNullInfo
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
            plot.addAnnotation(new XYTitleAnnotation(5.0, 6.0,
                    new TextTitle("Hello World!")));
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
        JFreeChart chart = ChartFactory.createBarChart("Test", "Categories",
                "Value", dataset, false);
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
                "Value", dataset, false);
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
        JFreeChart chart = ChartFactory.createScatterPlot("Test", "X", "Y",
                dataset, false);
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
        JFreeChart chart = ChartFactory.createScatterPlot("Test", "X", "Y",
                dataset, false);
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
        JFreeChart chart = ChartFactory.createBarChart("Test", "Categories",
                "Value", dataset, false);
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
                "Value", dataset, false);
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
                "Value", dataset, false);
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
                "Value", dataset, false);
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
        JFreeChart chart = ChartFactory.createScatterPlot("Test", "X", "Y",
                dataset, false);
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
        JFreeChart chart = ChartFactory.createScatterPlot("Test", "X", "Y",
                dataset, false);
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

// org.jfree.chart.axis.junit.SubCategoryAxisTests::testEquals
    public void testEquals() {

        SubCategoryAxis a1 = new SubCategoryAxis("Test");
        SubCategoryAxis a2 = new SubCategoryAxis("Test");
        assertTrue(a1.equals(a2));
        assertTrue(a2.equals(a1));

        
        a1.addSubCategory("Sub 1");
        assertFalse(a1.equals(a2));
        a2.addSubCategory("Sub 1");
        assertTrue(a1.equals(a2));

        
        a1.setSubLabelFont(new Font("Serif", Font.BOLD, 15));
        assertFalse(a1.equals(a2));
        a2.setSubLabelFont(new Font("Serif", Font.BOLD, 15));
        assertTrue(a1.equals(a2));

        
        a1.setSubLabelPaint(Color.red);
        assertFalse(a1.equals(a2));
        a2.setSubLabelPaint(Color.red);
        assertTrue(a1.equals(a2));

    }

// org.jfree.chart.axis.junit.SubCategoryAxisTests::testHashCode
    public void testHashCode() {
        SubCategoryAxis a1 = new SubCategoryAxis("Test");
        SubCategoryAxis a2 = new SubCategoryAxis("Test");
        assertTrue(a1.equals(a2));
        int h1 = a1.hashCode();
        int h2 = a2.hashCode();
        assertEquals(h1, h2);
    }

// org.jfree.chart.axis.junit.SubCategoryAxisTests::testCloning
    public void testCloning() {
        SubCategoryAxis a1 = new SubCategoryAxis("Test");
        a1.addSubCategory("SubCategoryA");
        SubCategoryAxis a2 = null;
        try {
            a2 = (SubCategoryAxis) a1.clone();
        }
        catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        assertTrue(a1 != a2);
        assertTrue(a1.getClass() == a2.getClass());
        assertTrue(a1.equals(a2));
    }

// org.jfree.chart.axis.junit.SubCategoryAxisTests::testSerialization
    public void testSerialization() {
        SubCategoryAxis a1 = new SubCategoryAxis("Test Axis");
        a1.addSubCategory("SubCategoryA");
        SubCategoryAxis a2 = null;
        try {
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            ObjectOutput out = new ObjectOutputStream(buffer);
            out.writeObject(a1);
            out.close();

            ObjectInput in = new ObjectInputStream(
                    new ByteArrayInputStream(buffer.toByteArray()));
            a2 = (SubCategoryAxis) in.readObject();
            in.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        assertEquals(a1, a2);
    }

// org.jfree.chart.axis.junit.SubCategoryAxisTests::test2275695
    public void test2275695() {
        JFreeChart chart = ChartFactory.createStackedBarChart("Test",
                "Category", "Value", null, true);
        CategoryPlot plot = (CategoryPlot) chart.getPlot();
        plot.setDomainAxis(new SubCategoryAxis("SubCategoryAxis"));
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
            "Title", "X", "Y", dataset, false);
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
        CategoryToolTipGenerator tt2 = renderer.getToolTipGenerator(0, 0,
                false);
        assertTrue(tt2 == tt);
    }

// org.jfree.chart.junit.AreaChartTests::testSetSeriesURLGenerator
    public void testSetSeriesURLGenerator() {
        CategoryPlot plot = (CategoryPlot) this.chart.getPlot();
        CategoryItemRenderer renderer = plot.getRenderer();
        StandardCategoryURLGenerator url1
                = new StandardCategoryURLGenerator();
        renderer.setSeriesURLGenerator(0, url1);
        CategoryURLGenerator url2 = renderer.getURLGenerator(0, 0, false);
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
        CategoryToolTipGenerator tt2 = renderer.getToolTipGenerator(0, 0,
                false);
        assertTrue(tt2 == tt);
    }

// org.jfree.chart.junit.BarChart3DTests::testSetSeriesURLGenerator
    public void testSetSeriesURLGenerator() {
        CategoryPlot plot = (CategoryPlot) this.chart.getPlot();
        CategoryItemRenderer renderer = plot.getRenderer();
        StandardCategoryURLGenerator url1
                = new StandardCategoryURLGenerator();
        renderer.setSeriesURLGenerator(0, url1);
        CategoryURLGenerator url2 = renderer.getURLGenerator(0, 0, false);
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
        CategoryToolTipGenerator tt2 = renderer.getToolTipGenerator(0, 0,
                false);
        assertTrue(tt2 == tt);
    }

// org.jfree.chart.junit.BarChartTests::testSetSeriesURLGenerator
    public void testSetSeriesURLGenerator() {
        CategoryPlot plot = (CategoryPlot) this.chart.getPlot();
        CategoryItemRenderer renderer = plot.getRenderer();
        StandardCategoryURLGenerator url1
                = new StandardCategoryURLGenerator();
        renderer.setSeriesURLGenerator(0, url1);
        CategoryURLGenerator url2 = renderer.getURLGenerator(0, 0, false);
        assertTrue(url2 == url1);
    }

// org.jfree.chart.junit.ChartPanelTests::testConstructor1
    public void testConstructor1() {
        ChartPanel panel = new ChartPanel(null);
        assertEquals(null, panel.getChart());
    }

// org.jfree.chart.junit.ChartPanelTests::testSetChart
    public void testSetChart() {
        JFreeChart chart = new JFreeChart(new XYPlot());
        ChartPanel panel = new ChartPanel(chart);
        panel.setChart(null);
        assertEquals(null, panel.getChart());
    }

// org.jfree.chart.junit.ChartPanelTests::testGetListeners
    public void testGetListeners() {
        ChartPanel p = new ChartPanel(null);
        p.addChartMouseListener(this);
        EventListener[] listeners = p.getListeners(ChartMouseListener.class);
        assertEquals(1, listeners.length);
        assertEquals(this, listeners[0]);
        
        listeners = p.getListeners(CaretListener.class);
        assertEquals(0, listeners.length);
        p.removeChartMouseListener(this);
        listeners = p.getListeners(ChartMouseListener.class);
        assertEquals(0, listeners.length);

        
        boolean pass = false;
        try {
            listeners = p.getListeners((Class) null);
        }
        catch (NullPointerException e) {
            pass = true;
        }
        assertTrue(pass);

        
        pass = false;
        try {
            listeners = p.getListeners(Integer.class);
        }
        catch (ClassCastException e) {
            pass = true;
        }
        assertTrue(pass);
    }

// org.jfree.chart.junit.ChartPanelTests::test2502355_zoom
    public void test2502355_zoom() {
        DefaultXYDataset dataset = new DefaultXYDataset();
        JFreeChart chart = ChartFactory.createXYLineChart("TestChart", "X",
                "Y", dataset, false);
        ChartPanel panel = new ChartPanel(chart);
        chart.addChangeListener(this);
        this.chartChangeEvents.clear();
        panel.zoom(new Rectangle2D.Double(1.0, 2.0, 3.0, 4.0));
        assertEquals(1, this.chartChangeEvents.size());
    }

// org.jfree.chart.junit.ChartPanelTests::test2502355_zoomInBoth
    public void test2502355_zoomInBoth() {
        DefaultXYDataset dataset = new DefaultXYDataset();
        JFreeChart chart = ChartFactory.createXYLineChart("TestChart", "X",
                "Y", dataset, false);
        ChartPanel panel = new ChartPanel(chart);
        chart.addChangeListener(this);
        this.chartChangeEvents.clear();
        panel.zoomInBoth(1.0, 2.0);
        assertEquals(1, this.chartChangeEvents.size());
    }

// org.jfree.chart.junit.ChartPanelTests::test2502355_zoomOutBoth
    public void test2502355_zoomOutBoth() {
        DefaultXYDataset dataset = new DefaultXYDataset();
        JFreeChart chart = ChartFactory.createXYLineChart("TestChart", "X",
                "Y", dataset, false);
        ChartPanel panel = new ChartPanel(chart);
        chart.addChangeListener(this);
        this.chartChangeEvents.clear();
        panel.zoomOutBoth(1.0, 2.0);
        assertEquals(1, this.chartChangeEvents.size());
    }

// org.jfree.chart.junit.ChartPanelTests::test2502355_restoreAutoBounds
    public void test2502355_restoreAutoBounds() {
        DefaultXYDataset dataset = new DefaultXYDataset();
        JFreeChart chart = ChartFactory.createXYLineChart("TestChart", "X",
                "Y", dataset, false);
        ChartPanel panel = new ChartPanel(chart);
        chart.addChangeListener(this);
        this.chartChangeEvents.clear();
        panel.restoreAutoBounds();
        assertEquals(1, this.chartChangeEvents.size());
    }

// org.jfree.chart.junit.ChartPanelTests::test2502355_zoomInDomain
    public void test2502355_zoomInDomain() {
        DefaultXYDataset dataset = new DefaultXYDataset();
        JFreeChart chart = ChartFactory.createXYLineChart("TestChart", "X",
                "Y", dataset, false);
        XYPlot plot = (XYPlot) chart.getPlot();
        plot.setDomainAxis(1, new NumberAxis("X2"));
        ChartPanel panel = new ChartPanel(chart);
        chart.addChangeListener(this);
        this.chartChangeEvents.clear();
        panel.zoomInDomain(1.0, 2.0);
        assertEquals(1, this.chartChangeEvents.size());
    }

// org.jfree.chart.junit.ChartPanelTests::test2502355_zoomInRange
    public void test2502355_zoomInRange() {
        DefaultXYDataset dataset = new DefaultXYDataset();
        JFreeChart chart = ChartFactory.createXYLineChart("TestChart", "X",
                "Y", dataset, false);
        XYPlot plot = (XYPlot) chart.getPlot();
        plot.setRangeAxis(1, new NumberAxis("X2"));
        ChartPanel panel = new ChartPanel(chart);
        chart.addChangeListener(this);
        this.chartChangeEvents.clear();
        panel.zoomInRange(1.0, 2.0);
        assertEquals(1, this.chartChangeEvents.size());
    }

// org.jfree.chart.junit.ChartPanelTests::test2502355_zoomOutDomain
    public void test2502355_zoomOutDomain() {
        DefaultXYDataset dataset = new DefaultXYDataset();
        JFreeChart chart = ChartFactory.createXYLineChart("TestChart", "X",
                "Y", dataset, false);
        XYPlot plot = (XYPlot) chart.getPlot();
        plot.setDomainAxis(1, new NumberAxis("X2"));
        ChartPanel panel = new ChartPanel(chart);
        chart.addChangeListener(this);
        this.chartChangeEvents.clear();
        panel.zoomOutDomain(1.0, 2.0);
        assertEquals(1, this.chartChangeEvents.size());
    }

// org.jfree.chart.junit.ChartPanelTests::test2502355_zoomOutRange
    public void test2502355_zoomOutRange() {
        DefaultXYDataset dataset = new DefaultXYDataset();
        JFreeChart chart = ChartFactory.createXYLineChart("TestChart", "X",
                "Y", dataset, false);
        XYPlot plot = (XYPlot) chart.getPlot();
        plot.setRangeAxis(1, new NumberAxis("X2"));
        ChartPanel panel = new ChartPanel(chart);
        chart.addChangeListener(this);
        this.chartChangeEvents.clear();
        panel.zoomOutRange(1.0, 2.0);
        assertEquals(1, this.chartChangeEvents.size());
    }

// org.jfree.chart.junit.ChartPanelTests::test2502355_restoreAutoDomainBounds
    public void test2502355_restoreAutoDomainBounds() {
        DefaultXYDataset dataset = new DefaultXYDataset();
        JFreeChart chart = ChartFactory.createXYLineChart("TestChart", "X",
                "Y", dataset, false);
        XYPlot plot = (XYPlot) chart.getPlot();
        plot.setDomainAxis(1, new NumberAxis("X2"));
        ChartPanel panel = new ChartPanel(chart);
        chart.addChangeListener(this);
        this.chartChangeEvents.clear();
        panel.restoreAutoDomainBounds();
        assertEquals(1, this.chartChangeEvents.size());
    }

// org.jfree.chart.junit.ChartPanelTests::test2502355_restoreAutoRangeBounds
    public void test2502355_restoreAutoRangeBounds() {
        DefaultXYDataset dataset = new DefaultXYDataset();
        JFreeChart chart = ChartFactory.createXYLineChart("TestChart", "X",
                "Y", dataset, false);
        XYPlot plot = (XYPlot) chart.getPlot();
        plot.setRangeAxis(1, new NumberAxis("X2"));
        ChartPanel panel = new ChartPanel(chart);
        chart.addChangeListener(this);
        this.chartChangeEvents.clear();
        panel.restoreAutoRangeBounds();
        assertEquals(1, this.chartChangeEvents.size());
    }

// org.jfree.chart.junit.ChartPanelTests::testSetMouseWheelEnabled
    public void testSetMouseWheelEnabled() {
        DefaultXYDataset dataset = new DefaultXYDataset();
        JFreeChart chart = ChartFactory.createXYLineChart("TestChart", "X",
                "Y", dataset, false);
        ChartPanel panel = new ChartPanel(chart);
        panel.setMouseWheelEnabled(true);
        assertTrue(panel.isMouseWheelEnabled());
        panel.setMouseWheelEnabled(true);
        assertTrue(panel.isMouseWheelEnabled());
        panel.setMouseWheelEnabled(false);
        assertFalse(panel.isMouseWheelEnabled());
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
            e.printStackTrace();
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
        CategoryToolTipGenerator tt2 = renderer.getToolTipGenerator(0, 0,
                false);
        assertTrue(tt2 == tt);
    }

// org.jfree.chart.junit.GanttChartTests::testSetSeriesURLGenerator
    public void testSetSeriesURLGenerator() {
        CategoryPlot plot = (CategoryPlot) this.chart.getPlot();
        CategoryItemRenderer renderer = plot.getRenderer();
        StandardCategoryURLGenerator url1
                = new StandardCategoryURLGenerator();
        renderer.setSeriesURLGenerator(0, url1);
        CategoryURLGenerator url2 = renderer.getURLGenerator(0, 0, false);
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
        JFreeChart chart = ChartFactory.createPieChart("title", dataset, true);
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

        JFreeChart c1 = ChartFactory.createPieChart("Test", data, true);
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

        JFreeChart c1 = ChartFactory.createPieChart3D("Test", data, true);
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

        
        JFreeChart c1 = ChartFactory.createBarChart("Vertical Bar Chart", 
                "Category", "Value", dataset, true);

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
                "Value", dataset, true);
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
        JFreeChart chart = ChartFactory.createPieChart("title", dataset, true);

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
        JFreeChart chart = ChartFactory.createPieChart("title", dataset, true);
        List subtitles = chart.getSubtitles();

        assertEquals(1, chart.getSubtitleCount());

        
        subtitles.add(new TextTitle("T"));
        assertEquals(1, chart.getSubtitleCount());
    }

// org.jfree.chart.junit.JFreeChartTests::testLegendEvents
    public void testLegendEvents() {
        DefaultPieDataset dataset = new DefaultPieDataset();
        JFreeChart chart = ChartFactory.createPieChart("title", dataset, true);
        chart.addChangeListener(this);
        this.lastChartChangeEvent = null;
        LegendTitle legend = chart.getLegend();
        legend.setPosition(RectangleEdge.TOP);
        assertNotNull(this.lastChartChangeEvent);
    }

// org.jfree.chart.junit.JFreeChartTests::testTitleChangeEvent
    public void testTitleChangeEvent() {
        DefaultPieDataset dataset = new DefaultPieDataset();
        JFreeChart chart = ChartFactory.createPieChart("title", dataset, true);
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
        CategoryToolTipGenerator tt2 = renderer.getToolTipGenerator(0, 0,
                false);
        assertTrue(tt2 == tt);
    }

// org.jfree.chart.junit.LineChart3DTests::testSetSeriesURLGenerator
    public void testSetSeriesURLGenerator() {
        CategoryPlot plot = (CategoryPlot) this.chart.getPlot();
        CategoryItemRenderer renderer = plot.getRenderer();
        StandardCategoryURLGenerator url1
                = new StandardCategoryURLGenerator();
        renderer.setSeriesURLGenerator(0, url1);
        CategoryURLGenerator url2 = renderer.getURLGenerator(0, 0, false);
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
        CategoryToolTipGenerator tt2 = renderer.getToolTipGenerator(0, 0,
                false);
        assertTrue(tt2 == tt);
    }

// org.jfree.chart.junit.LineChartTests::testSetSeriesURLGenerator
    public void testSetSeriesURLGenerator() {
        CategoryPlot plot = (CategoryPlot) this.chart.getPlot();
        CategoryItemRenderer renderer = plot.getRenderer();
        StandardCategoryURLGenerator url1
                = new StandardCategoryURLGenerator();
        renderer.setSeriesURLGenerator(0, url1);
        CategoryURLGenerator url2 = renderer.getURLGenerator(0, 0, false);
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
        XYToolTipGenerator tt2 = renderer.getToolTipGenerator(0, 0, false);
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
        CategoryToolTipGenerator tt2 = renderer.getToolTipGenerator(0, 0,
                false);
        assertTrue(tt2 == tt);
    }

// org.jfree.chart.junit.StackedAreaChartTests::testSetSeriesURLGenerator
    public void testSetSeriesURLGenerator() {
        CategoryPlot plot = (CategoryPlot) this.chart.getPlot();
        CategoryItemRenderer renderer = plot.getRenderer();
        StandardCategoryURLGenerator url1
                = new StandardCategoryURLGenerator();
        renderer.setSeriesURLGenerator(0, url1);
        CategoryURLGenerator url2 = renderer.getURLGenerator(0, 0, false);
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
        CategoryToolTipGenerator tt2 = renderer.getToolTipGenerator(0, 0,
                false);
        assertTrue(tt2 == tt);
    }

// org.jfree.chart.junit.StackedBarChart3DTests::testSetSeriesURLGenerator
    public void testSetSeriesURLGenerator() {
        CategoryPlot plot = (CategoryPlot) this.chart.getPlot();
        CategoryItemRenderer renderer = plot.getRenderer();
        StandardCategoryURLGenerator url1
                = new StandardCategoryURLGenerator();
        renderer.setSeriesURLGenerator(0, url1);
        CategoryURLGenerator url2 = renderer.getURLGenerator(0, 0, false);
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
        CategoryToolTipGenerator tt2 = renderer.getToolTipGenerator(0, 0,
                false);
        assertTrue(tt2 == tt);
    }

// org.jfree.chart.junit.StackedBarChartTests::testSetSeriesURLGenerator
    public void testSetSeriesURLGenerator() {
        CategoryPlot plot = (CategoryPlot) this.chart.getPlot();
        CategoryItemRenderer renderer = plot.getRenderer();
        StandardCategoryURLGenerator url1
                = new StandardCategoryURLGenerator();
        renderer.setSeriesURLGenerator(0, url1);
        CategoryURLGenerator url2 = renderer.getURLGenerator(0, 0, false);
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
        XYToolTipGenerator tt2 = renderer.getToolTipGenerator(0, 0, false);
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
        CategoryToolTipGenerator tt2 = renderer.getToolTipGenerator(0, 0,
                false);
        assertTrue(tt2 == tt);
    }

// org.jfree.chart.junit.WaterfallChartTests::testSetSeriesURLGenerator
    public void testSetSeriesURLGenerator() {
        CategoryPlot plot = (CategoryPlot) this.chart.getPlot();
        CategoryItemRenderer renderer = plot.getRenderer();
        StandardCategoryURLGenerator url1
                = new StandardCategoryURLGenerator();
        renderer.setSeriesURLGenerator(0, url1);
        CategoryURLGenerator url2 = renderer.getURLGenerator(0, 0, false);
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
        XYToolTipGenerator tt2 = renderer.getToolTipGenerator(0, 0,
                false);
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
        XYToolTipGenerator tt2 = renderer.getToolTipGenerator(0, 0, false);
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
        XYToolTipGenerator tt2 = renderer.getToolTipGenerator(0, 0, false);
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
        XYToolTipGenerator tt2 = renderer.getToolTipGenerator(0, 0, false);
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
        XYToolTipGenerator tt2 = renderer.getToolTipGenerator(0, 0, false);
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

        
        plot1.addDomainMarker(new CategoryMarker("C1"), Layer.FOREGROUND);
        assertFalse(plot1.equals(plot2));
        plot2.addDomainMarker(new CategoryMarker("C1"), Layer.FOREGROUND);
        assertTrue(plot1.equals(plot2));

        
        plot1.addDomainMarker(new CategoryMarker("C2"), Layer.BACKGROUND);
        assertFalse(plot1.equals(plot2));
        plot2.addDomainMarker(new CategoryMarker("C2"), Layer.BACKGROUND);
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

        
        plot1.addAnnotation(new CategoryTextAnnotation("Text", "Category",
                43.0));
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

        
        plot1.setFixedLegendItems(new LegendItemCollection());
        assertFalse(plot1.equals(plot2));
        plot2.setFixedLegendItems(new LegendItemCollection());
        assertTrue(plot1.equals(plot2));

        
        plot1.setCrosshairDatasetIndex(99);
        assertFalse(plot1.equals(plot2));
        plot2.setCrosshairDatasetIndex(99);
        assertTrue(plot1.equals(plot2));

        
        plot1.setDomainCrosshairColumnKey("A");
        assertFalse(plot1.equals(plot2));
        plot2.setDomainCrosshairColumnKey("A");
        assertTrue(plot1.equals(plot2));

        
        plot1.setDomainCrosshairRowKey("B");
        assertFalse(plot1.equals(plot2));
        plot2.setDomainCrosshairRowKey("B");
        assertTrue(plot1.equals(plot2));

        
        plot1.setDomainCrosshairVisible(true);
        assertFalse(plot1.equals(plot2));
        plot2.setDomainCrosshairVisible(true);
        assertTrue(plot1.equals(plot2));

        
        plot1.setDomainCrosshairPaint(new GradientPaint(1.0f, 2.0f, Color.red,
                3.0f, 4.0f, Color.blue));
        assertFalse(plot1.equals(plot2));
        plot2.setDomainCrosshairPaint(new GradientPaint(1.0f, 2.0f, Color.red,
                3.0f, 4.0f, Color.blue));
        assertTrue(plot1.equals(plot2));

        
        plot1.setDomainCrosshairStroke(new BasicStroke(1.23f));
        assertFalse(plot1.equals(plot2));
        plot2.setDomainCrosshairStroke(new BasicStroke(1.23f));
        assertTrue(plot1.equals(plot2));

        plot1.setRangeMinorGridlinesVisible(true);
        assertFalse(plot1.equals(plot2));
        plot2.setRangeMinorGridlinesVisible(true);
        assertTrue(plot1.equals(plot2));

        plot1.setRangeMinorGridlinePaint(new GradientPaint(1.0f, 2.0f,
                Color.red, 3.0f, 4.0f, Color.blue));
        assertFalse(plot1.equals(plot2));
        plot2.setRangeMinorGridlinePaint(new GradientPaint(1.0f, 2.0f, Color.red,
                3.0f, 4.0f, Color.blue));
        assertTrue(plot1.equals(plot2));

        plot1.setRangeMinorGridlineStroke(new BasicStroke(1.23f));
        assertFalse(plot1.equals(plot2));
        plot2.setRangeMinorGridlineStroke(new BasicStroke(1.23f));
        assertTrue(plot1.equals(plot2));

        plot1.setRangeZeroBaselineVisible(!plot1.isRangeZeroBaselineVisible());
        assertFalse(plot1.equals(plot2));
        plot2.setRangeZeroBaselineVisible(!plot2.isRangeZeroBaselineVisible());
        assertTrue(plot1.equals(plot2));

        plot1.setRangeZeroBaselinePaint(new GradientPaint(1.0f, 2.0f,
                Color.red, 3.0f, 4.0f, Color.blue));
        assertFalse(plot1.equals(plot2));
        plot2.setRangeZeroBaselinePaint(new GradientPaint(1.0f, 2.0f, Color.red,
                3.0f, 4.0f, Color.blue));
        assertTrue(plot1.equals(plot2));

        plot1.setRangeZeroBaselineStroke(new BasicStroke(1.23f));
        assertFalse(plot1.equals(plot2));
        plot2.setRangeZeroBaselineStroke(new BasicStroke(1.23f));
        assertTrue(plot1.equals(plot2));

        
        plot1.setShadowGenerator(new DefaultShadowGenerator(5, Color.gray,
                0.6f, 4, -Math.PI / 4));
        assertFalse(plot1.equals(plot2));
        plot2.setShadowGenerator(new DefaultShadowGenerator(5, Color.gray,
                0.6f, 4, -Math.PI / 4));
        assertTrue(plot1.equals(plot2));

        plot1.setShadowGenerator(null);
        assertFalse(plot1.equals(plot2));
        plot2.setShadowGenerator(null);
        assertTrue(plot1.equals(plot2));
    }

// org.jfree.chart.plot.junit.CategoryPlotTests::testCloning
    public void testCloning() {
        CategoryPlot p1 = new CategoryPlot();
        p1.setRangeCrosshairPaint(new GradientPaint(1.0f, 2.0f, Color.white,
                3.0f, 4.0f, Color.yellow));
        p1.setRangeMinorGridlinePaint(new GradientPaint(2.0f, 3.0f, Color.white,
                4.0f, 5.0f, Color.red));
        p1.setRangeZeroBaselinePaint(new GradientPaint(3.0f, 4.0f, Color.red,
                5.0f, 6.0f, Color.white));
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

        
        p1.addAnnotation(new CategoryLineAnnotation("C1", 1.0, "C2", 2.0,
                Color.red, new BasicStroke(1.0f)));
        assertFalse(p1.equals(p2));
        p2.addAnnotation(new CategoryLineAnnotation("C1", 1.0, "C2", 2.0,
                Color.red, new BasicStroke(1.0f)));
        assertTrue(p1.equals(p2));

        p1.addDomainMarker(new CategoryMarker("C1"), Layer.FOREGROUND);
        assertFalse(p1.equals(p2));
        p2.addDomainMarker(new CategoryMarker("C1"), Layer.FOREGROUND);
        assertTrue(p1.equals(p2));

        p1.addDomainMarker(new CategoryMarker("C2"), Layer.BACKGROUND);
        assertFalse(p1.equals(p2));
        p2.addDomainMarker(new CategoryMarker("C2"), Layer.BACKGROUND);
        assertTrue(p1.equals(p2));

        p1.addRangeMarker(new ValueMarker(1.0), Layer.FOREGROUND);
        assertFalse(p1.equals(p2));
        p2.addRangeMarker(new ValueMarker(1.0), Layer.FOREGROUND);
        assertTrue(p1.equals(p2));

        p1.addRangeMarker(new ValueMarker(2.0), Layer.BACKGROUND);
        assertFalse(p1.equals(p2));
        p2.addRangeMarker(new ValueMarker(2.0), Layer.BACKGROUND);
        assertTrue(p1.equals(p2));
    }

// org.jfree.chart.plot.junit.CategoryPlotTests::testCloning2
    public void testCloning2() {
        AxisSpace da1 = new AxisSpace();
        AxisSpace ra1 = new AxisSpace();
        CategoryPlot p1 = new CategoryPlot();
        p1.setFixedDomainAxisSpace(da1);
        p1.setFixedRangeAxisSpace(ra1);
        CategoryPlot p2 = null;
        try {
            p2 = (CategoryPlot) p1.clone();
        }
        catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        assertTrue(p1 != p2);
        assertTrue(p1.getClass() == p2.getClass());
        assertTrue(p1.equals(p2));

        da1.setBottom(99.0);
        assertFalse(p1.equals(p2));
        p2.getFixedDomainAxisSpace().setBottom(99.0);
        assertTrue(p1.equals(p2));

        ra1.setBottom(11.0);
        assertFalse(p1.equals(p2));
        p2.getFixedRangeAxisSpace().setBottom(11.0);
        assertTrue(p1.equals(p2));
    }

// org.jfree.chart.plot.junit.CategoryPlotTests::testCloning3
    public void testCloning3() {
        LegendItemCollection c1 = new LegendItemCollection();
        CategoryPlot p1 = new CategoryPlot();
        p1.setFixedLegendItems(c1);
        CategoryPlot p2 = null;
        try {
            p2 = (CategoryPlot) p1.clone();
        }
        catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        assertTrue(p1 != p2);
        assertTrue(p1.getClass() == p2.getClass());
        assertTrue(p1.equals(p2));

        c1.add(new LegendItem("X", "XX", "tt", "url", true,
                new Rectangle2D.Double(1.0, 2.0, 3.0, 4.0), true, Color.red,
                true, Color.yellow, new BasicStroke(1.0f), true,
                new Line2D.Double(1.0, 2.0, 3.0, 4.0), new BasicStroke(1.0f),
                Color.green));
        assertFalse(p1.equals(p2));
        p2.getFixedLegendItems().add(new LegendItem("X", "XX", "tt", "url",
                true, new Rectangle2D.Double(1.0, 2.0, 3.0, 4.0), true,
                Color.red, true, Color.yellow, new BasicStroke(1.0f), true,
                new Line2D.Double(1.0, 2.0, 3.0, 4.0), new BasicStroke(1.0f),
                Color.green));
        assertTrue(p1.equals(p2));
    }

// org.jfree.chart.plot.junit.CategoryPlotTests::testBug2817504
    public void testBug2817504() {
        CategoryPlot p1 = new CategoryPlot();
        LineAndShapeRenderer r1 = new LineAndShapeRenderer();
        p1.setRenderer(r1);
        CategoryPlot p2 = null;
        try {
            p2 = (CategoryPlot) p1.clone();
        }
        catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        assertTrue(p1 != p2);
        assertTrue(p1.getClass() == p2.getClass());
        assertTrue(p1.equals(p2));

        
        LineAndShapeRenderer r2 = (LineAndShapeRenderer) p2.getRenderer();
        assertTrue(r2.getPlot() == p2);
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
        JFreeChart chart = ChartFactory.createBarChart("Test Chart", 
                "Category Axis", "Value Axis", dataset, true);
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
        JFreeChart chart = ChartFactory.createBarChart("Test Chart",
                "Category Axis", "Value Axis", dataset, true);
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
                dataset, true);
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

// org.jfree.chart.plot.junit.CategoryPlotTests::testGetDomainAxisForDataset
    public void testGetDomainAxisForDataset() {
        CategoryDataset dataset = new DefaultCategoryDataset();
        CategoryAxis xAxis = new CategoryAxis("X");
        NumberAxis yAxis = new NumberAxis("Y");
        CategoryItemRenderer renderer = new BarRenderer();
        CategoryPlot plot = new CategoryPlot(dataset, xAxis, yAxis, renderer);
        assertEquals(xAxis, plot.getDomainAxisForDataset(0));

        
        boolean pass = false;
        try {
            plot.getDomainAxisForDataset(-1);
        }
        catch (IllegalArgumentException e) {
            pass = true;
        }
        assertTrue(pass);

        
        
        CategoryAxis xAxis2 = new CategoryAxis("X2");
        plot.setDomainAxis(1, xAxis2);
        assertEquals(xAxis, plot.getDomainAxisForDataset(0));

        plot.mapDatasetToDomainAxis(0, 1);
        assertEquals(xAxis2, plot.getDomainAxisForDataset(0));

        List axisIndices = Arrays.asList(new Integer[] {new Integer(0),
                new Integer(1)});
        plot.mapDatasetToDomainAxes(0, axisIndices);
        assertEquals(xAxis, plot.getDomainAxisForDataset(0));

        axisIndices = Arrays.asList(new Integer[] {new Integer(1),
                new Integer(2)});
        plot.mapDatasetToDomainAxes(0, axisIndices);
        assertEquals(xAxis2, plot.getDomainAxisForDataset(0));
    }

// org.jfree.chart.plot.junit.CategoryPlotTests::testGetRangeAxisForDataset
    public void testGetRangeAxisForDataset() {
        CategoryDataset dataset = new DefaultCategoryDataset();
        CategoryAxis xAxis = new CategoryAxis("X");
        NumberAxis yAxis = new NumberAxis("Y");
        CategoryItemRenderer renderer = new DefaultCategoryItemRenderer();
        CategoryPlot plot = new CategoryPlot(dataset, xAxis, yAxis, renderer);
        assertEquals(yAxis, plot.getRangeAxisForDataset(0));

        
        boolean pass = false;
        try {
            plot.getRangeAxisForDataset(-1);
        }
        catch (IllegalArgumentException e) {
            pass = true;
        }
        assertTrue(pass);

        
        
        NumberAxis yAxis2 = new NumberAxis("Y2");
        plot.setRangeAxis(1, yAxis2);
        assertEquals(yAxis, plot.getRangeAxisForDataset(0));

        plot.mapDatasetToRangeAxis(0, 1);
        assertEquals(yAxis2, plot.getRangeAxisForDataset(0));

        List axisIndices = Arrays.asList(new Integer[] {new Integer(0),
                new Integer(1)});
        plot.mapDatasetToRangeAxes(0, axisIndices);
        assertEquals(yAxis, plot.getRangeAxisForDataset(0));

        axisIndices = Arrays.asList(new Integer[] {new Integer(1),
                new Integer(2)});
        plot.mapDatasetToRangeAxes(0, axisIndices);
        assertEquals(yAxis2, plot.getRangeAxisForDataset(0));
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
            ObjectInput in = new ObjectInputStream(new ByteArrayInputStream(
                    buffer.toByteArray()));
            plot2 = (CombinedDomainCategoryPlot) in.readObject();
            in.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        assertEquals(plot1, plot2);
    }

// org.jfree.chart.plot.junit.CombinedDomainCategoryPlotTests::testNotification
    public void testNotification() {
        CombinedDomainCategoryPlot plot = createPlot();
        JFreeChart chart = new JFreeChart(plot);
        chart.addChangeListener(this);
        CategoryPlot subplot1 = (CategoryPlot) plot.getSubplots().get(0);
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
            ObjectInput in = new ObjectInputStream(new ByteArrayInputStream(
                    buffer.toByteArray()));
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

// org.jfree.chart.plot.junit.CombinedRangeCategoryPlotTests::testNotification
    public void testNotification() {
        CombinedRangeCategoryPlot plot = createPlot();
        JFreeChart chart = new JFreeChart(plot);
        chart.addChangeListener(this);
        CategoryPlot subplot1 = (CategoryPlot) plot.getSubplots().get(0);
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
        JFreeChart chart = ChartFactory.createPieChart3D("Test", null, true);
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

        plot1.setLabelLinkStyle(PieLabelLinkStyle.QUAD_CURVE);
        assertFalse(plot1.equals(plot2));
        plot2.setLabelLinkStyle(PieLabelLinkStyle.QUAD_CURVE);
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

        
        plot1.setAutoPopulateSectionPaint(false);
        assertFalse(plot1.equals(plot2));
        plot2.setAutoPopulateSectionPaint(false);
        assertTrue(plot1.equals(plot2));

        
        plot1.setAutoPopulateSectionOutlinePaint(true);
        assertFalse(plot1.equals(plot2));
        plot2.setAutoPopulateSectionOutlinePaint(true);
        assertTrue(plot1.equals(plot2));

        
        plot1.setAutoPopulateSectionOutlineStroke(true);
        assertFalse(plot1.equals(plot2));
        plot2.setAutoPopulateSectionOutlineStroke(true);
        assertTrue(plot1.equals(plot2));

        
        plot1.setShadowGenerator(new DefaultShadowGenerator(5, Color.gray,
                0.6f, 4, -Math.PI / 4));
        assertFalse(plot1.equals(plot2));
        plot2.setShadowGenerator(new DefaultShadowGenerator(5, Color.gray,
                0.6f, 4, -Math.PI / 4));
        assertTrue(plot1.equals(plot2));

        plot1.setShadowGenerator(null);
        assertFalse(plot1.equals(plot2));
        plot2.setShadowGenerator(null);
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
        JFreeChart chart = ChartFactory.createPieChart("Test", dataset, true);
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

// org.jfree.chart.plot.junit.PolarPlotTests::testEquals
    public void testEquals() {
        PolarPlot plot1 = new PolarPlot();
        PolarPlot plot2 = new PolarPlot();
        assertTrue(plot1.equals(plot2));
        assertTrue(plot2.equals(plot1));

        plot1.setAngleGridlinePaint(new GradientPaint(1.0f, 2.0f, Color.red,
                3.0f, 4.0f, Color.blue));
        assertFalse(plot1.equals(plot2));
        plot2.setAngleGridlinePaint(new GradientPaint(1.0f, 2.0f, Color.red,
                3.0f, 4.0f, Color.blue));
        assertTrue(plot1.equals(plot2));

        Stroke s = new BasicStroke(1.23f);
        plot1.setAngleGridlineStroke(s);
        assertFalse(plot1.equals(plot2));
        plot2.setAngleGridlineStroke(s);
        assertTrue(plot1.equals(plot2));

        plot1.setAngleTickUnit(new NumberTickUnit(11.0));
        assertFalse(plot1.equals(plot2));
        plot2.setAngleTickUnit(new NumberTickUnit(11.0));
        assertTrue(plot1.equals(plot2));

        plot1.setAngleGridlinesVisible(false);
        assertFalse(plot1.equals(plot2));
        plot2.setAngleGridlinesVisible(false);
        assertTrue(plot1.equals(plot2));

        plot1.setAngleLabelFont(new Font("Serif", Font.PLAIN, 9));
        assertFalse(plot1.equals(plot2));
        plot2.setAngleLabelFont(new Font("Serif", Font.PLAIN, 9));
        assertTrue(plot1.equals(plot2));

        plot1.setAngleLabelPaint(new GradientPaint(9.0f, 8.0f, Color.blue,
                7.0f, 6.0f, Color.red));
        assertFalse(plot1.equals(plot2));
        plot2.setAngleLabelPaint(new GradientPaint(9.0f, 8.0f, Color.blue,
                7.0f, 6.0f, Color.red));
        assertTrue(plot1.equals(plot2));

        plot1.setAngleLabelsVisible(false);
        assertFalse(plot1.equals(plot2));
        plot2.setAngleLabelsVisible(false);
        assertTrue(plot1.equals(plot2));

        plot1.setAxis(new NumberAxis("Test"));
        assertFalse(plot1.equals(plot2));
        plot2.setAxis(new NumberAxis("Test"));
        assertTrue(plot1.equals(plot2));

        plot1.setRadiusGridlinePaint(new GradientPaint(1.0f, 2.0f, Color.white,
                3.0f, 4.0f, Color.black));
        assertFalse(plot1.equals(plot2));
        plot2.setRadiusGridlinePaint(new GradientPaint(1.0f, 2.0f, Color.white,
                3.0f, 4.0f, Color.black));
        assertTrue(plot1.equals(plot2));

        plot1.setRadiusGridlineStroke(s);
        assertFalse(plot1.equals(plot2));
        plot2.setRadiusGridlineStroke(s);
        assertTrue(plot1.equals(plot2));

        plot1.setRadiusGridlinesVisible(false);
        assertFalse(plot1.equals(plot2));
        plot2.setRadiusGridlinesVisible(false);
        assertTrue(plot1.equals(plot2));

        plot1.addCornerTextItem("XYZ");
        assertFalse(plot1.equals(plot2));
        plot2.addCornerTextItem("XYZ");
        assertTrue(plot1.equals(plot2));
    }

// org.jfree.chart.plot.junit.PolarPlotTests::testCloning
    public void testCloning() {
        PolarPlot p1 = new PolarPlot();
        PolarPlot p2 = null;
        try {
            p2 = (PolarPlot) p1.clone();
        }
        catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        assertTrue(p1 != p2);
        assertTrue(p1.getClass() == p2.getClass());
        assertTrue(p1.equals(p2));

        
        p1.addCornerTextItem("XYZ");
        assertFalse(p1.equals(p2));
        p2.addCornerTextItem("XYZ");
        assertTrue(p1.equals(p2));

        p1 = new PolarPlot(new DefaultXYDataset(), new NumberAxis("A1"),
                new DefaultPolarItemRenderer());
        p2 = null;
        try {
            p2 = (PolarPlot) p1.clone();
        }
        catch (CloneNotSupportedException e) {
            e.printStackTrace();
            System.err.println("Failed to clone.");
        }
        assertTrue(p1 != p2);
        assertTrue(p1.getClass() == p2.getClass());
        assertTrue(p1.equals(p2));

        
        p1.getAxis().setLabel("ABC");
        assertFalse(p1.equals(p2));
        p2.getAxis().setLabel("ABC");
        assertTrue(p1.equals(p2));

    }

// org.jfree.chart.plot.junit.PolarPlotTests::testSerialization
    public void testSerialization() {

        PolarPlot p1 = new PolarPlot();
        p1.setAngleGridlinePaint(new GradientPaint(1.0f, 2.0f, Color.red, 3.0f,
                4.0f, Color.blue));
        p1.setAngleLabelPaint(new GradientPaint(1.0f, 2.0f, Color.red, 3.0f,
                4.0f, Color.blue));
        p1.setRadiusGridlinePaint(new GradientPaint(1.0f, 2.0f, Color.red, 3.0f,
                4.0f, Color.blue));
        PolarPlot p2 = null;

        try {
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            ObjectOutput out = new ObjectOutputStream(buffer);
            out.writeObject(p1);
            out.close();

            ObjectInput in = new ObjectInputStream(
                    new ByteArrayInputStream(buffer.toByteArray()));
            p2 = (PolarPlot) in.readObject();
            in.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        assertEquals(p1, p2);

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

        
        plot1.setFixedLegendItems(new LegendItemCollection());
        assertFalse(plot1.equals(plot2));
        plot2.setFixedLegendItems(new LegendItemCollection());
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

        List axisIndices = Arrays.asList(new Integer[] {new Integer(0),
            new Integer(1)});
        plot1.mapDatasetToDomainAxes(0, axisIndices);
        assertFalse(plot1.equals(plot2));
        plot2.mapDatasetToDomainAxes(0, axisIndices);
        assertTrue(plot1.equals(plot2));

        plot1.mapDatasetToRangeAxes(0, axisIndices);
        assertFalse(plot1.equals(plot2));
        plot2.mapDatasetToRangeAxes(0, axisIndices);
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
        List axisIndices = Arrays.asList(new Integer[] {new Integer(0),
                new Integer(1)});
        p1.mapDatasetToDomainAxes(0, axisIndices);
        p1.mapDatasetToRangeAxes(0, axisIndices);
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

// org.jfree.chart.plot.junit.XYPlotTests::testCloning3
    public void testCloning3() {
        XYPlot p1 = new XYPlot(null, new NumberAxis("Domain Axis"),
                new NumberAxis("Range Axis"), new StandardXYItemRenderer());
        LegendItemCollection c1 = new LegendItemCollection();
        p1.setFixedLegendItems(c1);
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

        
        c1.add(new LegendItem("X"));
        assertFalse(p1.equals(p2));
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

// org.jfree.chart.plot.junit.XYPlotTests::testBug2817504
    public void testBug2817504() {
        XYPlot p1 = new XYPlot();
        XYLineAndShapeRenderer r1 = new XYLineAndShapeRenderer();
        p1.setRenderer(r1);
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

        
        XYLineAndShapeRenderer r2 = (XYLineAndShapeRenderer) p2.getRenderer();
        assertTrue(r2.getPlot() == p2);
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

            ObjectInput in = new ObjectInputStream(new ByteArrayInputStream(
                    buffer.toByteArray()));
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
        JFreeChart chart = ChartFactory.createXYLineChart("Test Chart", 
                "Domain Axis", "Range Axis", dataset, true);
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
        JFreeChart chart = ChartFactory.createXYLineChart("Test Chart", 
                "Domain Axis", "Range Axis", dataset, true);
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
                dataset, true);
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
                dataset, true);
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
                dataset, true);
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

// org.jfree.chart.plot.junit.XYPlotTests::testGetDomainAxisForDataset
    public void testGetDomainAxisForDataset() {
        XYDataset dataset = new XYSeriesCollection();
        NumberAxis xAxis = new NumberAxis("X");
        NumberAxis yAxis = new NumberAxis("Y");
        XYItemRenderer renderer = new DefaultXYItemRenderer();
        XYPlot plot = new XYPlot(dataset, xAxis, yAxis, renderer);
        assertEquals(xAxis, plot.getDomainAxisForDataset(0));

        
        boolean pass = false;
        try {
            plot.getDomainAxisForDataset(-1);
        }
        catch (IllegalArgumentException e) {
            pass = true;
        }
        assertTrue(pass);

        
        pass = false;
        try {
            plot.getDomainAxisForDataset(1);
        }
        catch (IllegalArgumentException e) {
            pass = true;
        }
        assertTrue(pass);

        
        
        NumberAxis xAxis2 = new NumberAxis("X2");
        plot.setDomainAxis(1, xAxis2);
        assertEquals(xAxis, plot.getDomainAxisForDataset(0));

        plot.mapDatasetToDomainAxis(0, 1);
        assertEquals(xAxis2, plot.getDomainAxisForDataset(0));

        List axisIndices = Arrays.asList(new Integer[] {new Integer(0),
                new Integer(1)});
        plot.mapDatasetToDomainAxes(0, axisIndices);
        assertEquals(xAxis, plot.getDomainAxisForDataset(0));

        axisIndices = Arrays.asList(new Integer[] {new Integer(1),
                new Integer(2)});
        plot.mapDatasetToDomainAxes(0, axisIndices);
        assertEquals(xAxis2, plot.getDomainAxisForDataset(0));
    }

// org.jfree.chart.plot.junit.XYPlotTests::testGetRangeAxisForDataset
    public void testGetRangeAxisForDataset() {
        XYDataset dataset = new XYSeriesCollection();
        NumberAxis xAxis = new NumberAxis("X");
        NumberAxis yAxis = new NumberAxis("Y");
        XYItemRenderer renderer = new DefaultXYItemRenderer();
        XYPlot plot = new XYPlot(dataset, xAxis, yAxis, renderer);
        assertEquals(yAxis, plot.getRangeAxisForDataset(0));

        
        boolean pass = false;
        try {
            plot.getRangeAxisForDataset(-1);
        }
        catch (IllegalArgumentException e) {
            pass = true;
        }
        assertTrue(pass);

        
        pass = false;
        try {
            plot.getRangeAxisForDataset(1);
        }
        catch (IllegalArgumentException e) {
            pass = true;
        }
        assertTrue(pass);

        
        
        NumberAxis yAxis2 = new NumberAxis("Y2");
        plot.setRangeAxis(1, yAxis2);
        assertEquals(yAxis, plot.getRangeAxisForDataset(0));

        plot.mapDatasetToRangeAxis(0, 1);
        assertEquals(yAxis2, plot.getRangeAxisForDataset(0));

        List axisIndices = Arrays.asList(new Integer[] {new Integer(0),
                new Integer(1)});
        plot.mapDatasetToRangeAxes(0, axisIndices);
        assertEquals(yAxis, plot.getRangeAxisForDataset(0));

        axisIndices = Arrays.asList(new Integer[] {new Integer(1),
                new Integer(2)});
        plot.mapDatasetToRangeAxes(0, axisIndices);
        assertEquals(yAxis2, plot.getRangeAxisForDataset(0));
    }

// org.jfree.chart.renderer.category.junit.AbstractCategoryItemRendererTests::testEquals
    public void testEquals() {
        BarRenderer r1 = new BarRenderer();
        BarRenderer r2 = new BarRenderer();
        assertEquals(r1, r2);

        

        
        r1.setSeriesToolTipGenerator(1, new StandardCategoryToolTipGenerator());
        assertFalse(r1.equals(r2));
        r2.setSeriesToolTipGenerator(1, new StandardCategoryToolTipGenerator());
        assertTrue(r1.equals(r2));

        
        r1.setBaseToolTipGenerator(new StandardCategoryToolTipGenerator("{2}",
                NumberFormat.getInstance()));
        assertFalse(r1.equals(r2));
        r2.setBaseToolTipGenerator(new StandardCategoryToolTipGenerator("{2}",
                NumberFormat.getInstance()));
        assertTrue(r1.equals(r2));

        
        r1.setSeriesItemLabelGenerator(1,
                new StandardCategoryItemLabelGenerator());
        assertFalse(r1.equals(r2));
        r2.setSeriesItemLabelGenerator(1,
                new StandardCategoryItemLabelGenerator());
        assertTrue(r1.equals(r2));

        
        r1.setBaseItemLabelGenerator(new StandardCategoryItemLabelGenerator(
                "{2}", NumberFormat.getInstance()));
        assertFalse(r1.equals(r2));
        r2.setBaseItemLabelGenerator(new StandardCategoryItemLabelGenerator(
                "{2}", NumberFormat.getInstance()));
        assertTrue(r1.equals(r2));

        
        r1.setSeriesURLGenerator(1, new StandardCategoryURLGenerator());
        assertFalse(r1.equals(r2));
        r2.setSeriesURLGenerator(1, new StandardCategoryURLGenerator());
        assertTrue(r1.equals(r2));

        
        r1.setBaseURLGenerator(new StandardCategoryURLGenerator(
                "abc.html"));
        assertFalse(r1.equals(r2));
        r2.setBaseURLGenerator(new StandardCategoryURLGenerator(
                "abc.html"));
        assertTrue(r1.equals(r2));

        
        r1.setLegendItemLabelGenerator(new StandardCategorySeriesLabelGenerator(
                "XYZ"));
        assertFalse(r1.equals(r2));
        r2.setLegendItemLabelGenerator(new StandardCategorySeriesLabelGenerator(
                "XYZ"));
        assertTrue(r1.equals(r2));

        
        r1.setLegendItemToolTipGenerator(
                new StandardCategorySeriesLabelGenerator("ToolTip"));
        assertFalse(r1.equals(r2));
        r2.setLegendItemToolTipGenerator(
                new StandardCategorySeriesLabelGenerator("ToolTip"));
        assertTrue(r1.equals(r2));

        
        r1.setLegendItemURLGenerator(
                new StandardCategorySeriesLabelGenerator("URL"));
        assertFalse(r1.equals(r2));
        r2.setLegendItemURLGenerator(
                new StandardCategorySeriesLabelGenerator("URL"));
        assertTrue(r1.equals(r2));

        
        r1.addAnnotation(new CategoryTextAnnotation("ABC", "A", 2.0),
                Layer.BACKGROUND);
        assertFalse(r1.equals(r2));
        r2.addAnnotation(new CategoryTextAnnotation("ABC", "A", 2.0),
                Layer.BACKGROUND);
        assertTrue(r1.equals(r2));

        
        r1.addAnnotation(new CategoryTextAnnotation("DEF", "B", 4.0),
                Layer.FOREGROUND);
        assertFalse(r1.equals(r2));
        r2.addAnnotation(new CategoryTextAnnotation("DEF", "B", 4.0),
                Layer.FOREGROUND);
        assertTrue(r1.equals(r2));
    }

// org.jfree.chart.renderer.category.junit.AbstractCategoryItemRendererTests::testCloning1
    public void testCloning1() {
        AbstractCategoryItemRenderer r1 = new BarRenderer();
        r1.setBaseItemLabelGenerator(new StandardCategoryItemLabelGenerator());
        AbstractCategoryItemRenderer r2 = null;
        try {
            r2 = (BarRenderer) r1.clone();
        }
        catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        assertTrue(r1 != r2);
        assertTrue(r1.getClass() == r2.getClass());
        assertTrue(r1.equals(r2));

        r1 = new BarRenderer();
        r1.setSeriesItemLabelGenerator(0,
                new StandardCategoryItemLabelGenerator());
        r2 = null;
        try {
            r2 = (BarRenderer) r1.clone();
        }
        catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        assertTrue(r1 != r2);
        assertTrue(r1.getClass() == r2.getClass());
        assertTrue(r1.equals(r2));

        r1 = new BarRenderer();
        r1.setBaseItemLabelGenerator(new StandardCategoryItemLabelGenerator());
        r2 = null;
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

// org.jfree.chart.renderer.category.junit.AbstractCategoryItemRendererTests::testCloning2
    public void testCloning2() {
        BarRenderer r1 = new BarRenderer();
        r1.setBaseItemLabelGenerator(new IntervalCategoryItemLabelGenerator());
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

        r1 = new BarRenderer();
        r1.setSeriesItemLabelGenerator(0,
                new IntervalCategoryItemLabelGenerator());
        r2 = null;
        try {
            r2 = (BarRenderer) r1.clone();
        }
        catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        assertTrue(r1 != r2);
        assertTrue(r1.getClass() == r2.getClass());
        assertTrue(r1.equals(r2));

        r1 = new BarRenderer();
        r1.setBaseItemLabelGenerator(new IntervalCategoryItemLabelGenerator());
        r2 = null;
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

// org.jfree.chart.renderer.category.junit.AbstractCategoryItemRendererTests::testCloning_LegendItemLabelGenerator
    public void testCloning_LegendItemLabelGenerator() {
        StandardCategorySeriesLabelGenerator generator
                = new StandardCategorySeriesLabelGenerator("Series {0}");
        BarRenderer r1 = new BarRenderer();
        r1.setLegendItemLabelGenerator(generator);
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

        
        assertTrue(r1.getLegendItemLabelGenerator()
                != r2.getLegendItemLabelGenerator());
    }

// org.jfree.chart.renderer.category.junit.AbstractCategoryItemRendererTests::testCloning_LegendItemToolTipGenerator
    public void testCloning_LegendItemToolTipGenerator() {
        StandardCategorySeriesLabelGenerator generator
                = new StandardCategorySeriesLabelGenerator("Series {0}");
        BarRenderer r1 = new BarRenderer();
        r1.setLegendItemToolTipGenerator(generator);
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

        
        assertTrue(r1.getLegendItemToolTipGenerator()
                != r2.getLegendItemToolTipGenerator());
    }

// org.jfree.chart.renderer.category.junit.AbstractCategoryItemRendererTests::testCloning_LegendItemURLGenerator
    public void testCloning_LegendItemURLGenerator() {
        StandardCategorySeriesLabelGenerator generator
                = new StandardCategorySeriesLabelGenerator("Series {0}");
        BarRenderer r1 = new BarRenderer();
        r1.setLegendItemURLGenerator(generator);
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

        
        assertTrue(r1.getLegendItemURLGenerator()
                != r2.getLegendItemURLGenerator());
    }

// org.jfree.chart.renderer.category.junit.AbstractCategoryItemRendererTests::testGetSeriesItemLabelGenerator
    public void testGetSeriesItemLabelGenerator() {
        CategoryItemRenderer r = new BarRenderer();
        assertNull(r.getSeriesItemLabelGenerator(2));
        r.setSeriesItemLabelGenerator(2,
                new StandardCategoryItemLabelGenerator());
        assertNotNull(r.getSeriesItemLabelGenerator(2));
        r.setSeriesItemLabelGenerator(2, null);
        assertNull(r.getSeriesItemLabelGenerator(2));
        r.setBaseItemLabelGenerator(new StandardCategoryItemLabelGenerator());
        assertNull(r.getSeriesItemLabelGenerator(2));
    }

// org.jfree.chart.renderer.category.junit.AbstractCategoryItemRendererTests::testGetSeriesURLGenerator
    public void testGetSeriesURLGenerator() {
        CategoryItemRenderer r = new BarRenderer();
        assertNull(r.getSeriesURLGenerator(2));
        r.setSeriesURLGenerator(2, new StandardCategoryURLGenerator());
        assertNotNull(r.getSeriesURLGenerator(2));
        r.setSeriesURLGenerator(2, null);
        assertNull(r.getSeriesURLGenerator(2));
        r.setBaseURLGenerator(new StandardCategoryURLGenerator());
        assertNull(r.getSeriesURLGenerator(2));
    }

// org.jfree.chart.renderer.category.junit.AbstractCategoryItemRendererTests::testFindRangeBounds
    public void testFindRangeBounds() {
        AbstractCategoryItemRenderer r = new LineAndShapeRenderer();
        assertNull(r.findRangeBounds(null));

        
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        assertNull(r.findRangeBounds(dataset));

        dataset.addValue(1.0, "R1", "C1");
        assertEquals(new Range(1.0, 1.0), r.findRangeBounds(dataset));

        dataset.addValue(-2.0, "R1", "C2");
        assertEquals(new Range(-2.0, 1.0), r.findRangeBounds(dataset));

        dataset.addValue(null, "R1", "C3");
        assertEquals(new Range(-2.0, 1.0), r.findRangeBounds(dataset));
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

// org.jfree.chart.renderer.category.junit.AreaRendererTests::testPublicCloneable
    public void testPublicCloneable() {
        AreaRenderer r1 = new AreaRenderer();
        assertTrue(r1 instanceof PublicCloneable);
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

        
        r1.setBarPainter(new GradientBarPainter(0.1, 0.2, 0.3));
        assertFalse(r1.equals(r2));
        r2.setBarPainter(new GradientBarPainter(0.1, 0.2, 0.3));
        assertTrue(r1.equals(r2));

        
        r1.setShadowVisible(false);
        assertFalse(r1.equals(r2));
        r2.setShadowVisible(false);
        assertTrue(r1.equals(r2));

        r1.setShadowPaint(Color.red);
        assertFalse(r1.equals(r2));
        r2.setShadowPaint(Color.red);
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
        r1.setBarPainter(new GradientBarPainter(0.11, 0.22, 0.33));
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

// org.jfree.chart.renderer.category.junit.BarRendererTests::testPublicCloneable
    public void testPublicCloneable() {
        BarRenderer r1 = new BarRenderer();
        assertTrue(r1 instanceof PublicCloneable);
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

// org.jfree.chart.renderer.category.junit.BarRendererTests::testFindRangeBounds
    public void testFindRangeBounds() {
        BarRenderer r = new BarRenderer();
        assertNull(r.findRangeBounds(null));

        
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        assertNull(r.findRangeBounds(dataset));

        dataset.addValue(1.0, "R1", "C1");
        assertEquals(new Range(0.0, 1.0), r.findRangeBounds(dataset));
        r.setIncludeBaseInRange(false);
        assertEquals(new Range(1.0, 1.0), r.findRangeBounds(dataset));
        r.setIncludeBaseInRange(true);

        dataset.addValue(-2.0, "R1", "C2");
        assertEquals(new Range(-2.0, 1.0), r.findRangeBounds(dataset));

        dataset.addValue(null, "R1", "C3");
        assertEquals(new Range(-2.0, 1.0), r.findRangeBounds(dataset));
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

        r1.setMaximumBarWidth(0.99);
        assertFalse(r1.equals(r2));
        r2.setMaximumBarWidth(0.99);
        assertTrue(r1.equals(r2));

        
        r1.setMeanVisible(true);
        assertFalse(r1.equals(r2));
        r2.setMeanVisible(true);
        assertTrue(r1.equals(r2));

        r1.setMedianVisible(false);
        assertFalse(r1.equals(r2));
        r2.setMedianVisible(false);
        assertTrue(r1.equals(r2));
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

// org.jfree.chart.renderer.category.junit.BoxAndWhiskerRendererTests::testPublicCloneable
    public void testPublicCloneable() {
        BoxAndWhiskerRenderer r1 = new BoxAndWhiskerRenderer();
        assertTrue(r1 instanceof PublicCloneable);
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
            BufferedImageRenderingSource birs 
                    = new BufferedImageRenderingSource(image);
            ChartRenderingInfo info = new ChartRenderingInfo();
            info.setRenderingSource(birs);
            Graphics2D g2 = image.createGraphics();
            chart.draw(g2, new Rectangle2D.Double(0, 0, 200, 100), null,
                    info);
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
            BufferedImageRenderingSource birs
                    = new BufferedImageRenderingSource(image);
            ChartRenderingInfo info = new ChartRenderingInfo();
            info.setRenderingSource(birs);
            Graphics2D g2 = image.createGraphics();
            chart.draw(g2, new Rectangle2D.Double(0, 0, 200, 100), null,
                    info);
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
            e.printStackTrace();
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

// org.jfree.chart.renderer.category.junit.CategoryStepRendererTests::testPublicCloneable
    public void testPublicCloneable() {
        CategoryStepRenderer r1 = new CategoryStepRenderer(false);
        assertTrue(r1 instanceof PublicCloneable);
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
            e.printStackTrace();
        }
        assertTrue(r1 != r2);
        assertTrue(r1.getClass() == r2.getClass());
        assertTrue(r1.equals(r2));
    }

// org.jfree.chart.renderer.category.junit.GroupedStackedBarRendererTests::testPublicCloneable
    public void testPublicCloneable() {
        GroupedStackedBarRenderer r1 = new GroupedStackedBarRenderer();
        assertTrue(r1 instanceof PublicCloneable);
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

            ObjectInput in = new ObjectInputStream(new ByteArrayInputStream(
                    buffer.toByteArray()));
            r2 = (GroupedStackedBarRenderer) in.readObject();
            in.close();
        }
        catch (Exception e) {
            e.printStackTrace();
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

// org.jfree.chart.renderer.category.junit.GroupedStackedBarRendererTests::testFindRangeBounds
    public void testFindRangeBounds() {
        GroupedStackedBarRenderer r = new GroupedStackedBarRenderer();
        assertNull(r.findRangeBounds(null));

        
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        assertNull(r.findRangeBounds(dataset));

        dataset.addValue(1.0, "R1", "C1");
        assertEquals(new Range(0.0, 1.0), r.findRangeBounds(dataset));

        dataset.addValue(-2.0, "R1", "C2");
        assertEquals(new Range(-2.0, 1.0), r.findRangeBounds(dataset));

        dataset.addValue(null, "R1", "C3");
        assertEquals(new Range(-2.0, 1.0), r.findRangeBounds(dataset));

        KeyToGroupMap m = new KeyToGroupMap("G1");
        m.mapKeyToGroup("R1", "G1");
        m.mapKeyToGroup("R2", "G1");
        m.mapKeyToGroup("R3", "G2");
        r.setSeriesToGroupMap(m);

        dataset.addValue(0.5, "R3", "C1");
        assertEquals(new Range(-2.0, 1.0), r.findRangeBounds(dataset));

        dataset.addValue(5.0, "R3", "C2");
        assertEquals(new Range(-2.0, 5.0), r.findRangeBounds(dataset));
    }

// org.jfree.chart.renderer.category.junit.IntervalBarRendererTests::testEquals
    public void testEquals() {
        IntervalBarRenderer r1 = new IntervalBarRenderer();
        IntervalBarRenderer r2 = new IntervalBarRenderer();
        assertEquals(r1, r2);

        
        BarRenderer br = new BarRenderer();
        assertFalse(r1.equals(br));
    }

// org.jfree.chart.renderer.category.junit.IntervalBarRendererTests::testHashcode
    public void testHashcode() {
        IntervalBarRenderer r1 = new IntervalBarRenderer();
        IntervalBarRenderer r2 = new IntervalBarRenderer();
        assertTrue(r1.equals(r2));
        int h1 = r1.hashCode();
        int h2 = r2.hashCode();
        assertEquals(h1, h2);
    }

// org.jfree.chart.renderer.category.junit.IntervalBarRendererTests::testCloning
    public void testCloning() {
        IntervalBarRenderer r1 = new IntervalBarRenderer();
        IntervalBarRenderer r2 = null;
        try {
            r2 = (IntervalBarRenderer) r1.clone();
        }
        catch (CloneNotSupportedException e) {
            System.err.println("Failed to clone.");
        }
        assertTrue(r1 != r2);
        assertTrue(r1.getClass() == r2.getClass());
        assertTrue(r1.equals(r2));
    }

// org.jfree.chart.renderer.category.junit.IntervalBarRendererTests::testPublicCloneable
    public void testPublicCloneable() {
        IntervalBarRenderer r1 = new IntervalBarRenderer();
        assertTrue(r1 instanceof PublicCloneable);
    }

// org.jfree.chart.renderer.category.junit.IntervalBarRendererTests::testSerialization
    public void testSerialization() {
        IntervalBarRenderer r1 = new IntervalBarRenderer();
        IntervalBarRenderer r2 = null;
        try {
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            ObjectOutput out = new ObjectOutputStream(buffer);
            out.writeObject(r1);
            out.close();
            ObjectInput in = new ObjectInputStream(new ByteArrayInputStream(
                    buffer.toByteArray()));
            r2 = (IntervalBarRenderer) in.readObject();
            in.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        assertEquals(r1, r2);
    }

// org.jfree.chart.renderer.category.junit.IntervalBarRendererTests::testDrawWithNullInfo
    public void testDrawWithNullInfo() {
        boolean success = false;
        try {
            double[][] starts = new double[][] {{0.1, 0.2, 0.3},
                    {0.3, 0.4, 0.5}};
            double[][] ends = new double[][] {{0.5, 0.6, 0.7}, {0.7, 0.8, 0.9}};
            DefaultIntervalCategoryDataset dataset
                = new DefaultIntervalCategoryDataset(starts, ends);
            IntervalBarRenderer renderer = new IntervalBarRenderer();
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

// org.jfree.chart.renderer.category.junit.LayeredBarRendererTests::testPublicCloneable
    public void testPublicCloneable() {
        LayeredBarRenderer r1 = new LayeredBarRenderer();
        assertTrue(r1 instanceof PublicCloneable);
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

// org.jfree.chart.renderer.category.junit.LevelRendererTests::testPublicCloneable
    public void testPublicCloneable() {
        LevelRenderer r1 = new LevelRenderer();
        assertTrue(r1 instanceof PublicCloneable);
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

// org.jfree.chart.renderer.category.junit.LineAndShapeRendererTests::testPublicCloneable
    public void testPublicCloneable() {
        LineAndShapeRenderer r1 = new LineAndShapeRenderer();
        assertTrue(r1 instanceof PublicCloneable);
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

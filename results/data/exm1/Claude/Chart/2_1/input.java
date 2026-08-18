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

// org.jfree.chart.renderer.category.junit.MinMaxCategoryRendererTests::testPublicCloneable
    public void testPublicCloneable() {
        MinMaxCategoryRenderer r1 = new MinMaxCategoryRenderer();
        assertTrue(r1 instanceof PublicCloneable);
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

// org.jfree.chart.renderer.category.junit.StackedBarRenderer3DTests::testFindRangeBounds
    public void testFindRangeBounds() {
        StackedBarRenderer3D r = new StackedBarRenderer3D();
        assertNull(r.findRangeBounds(null));

        
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        assertNull(r.findRangeBounds(dataset));

        dataset.addValue(1.0, "R1", "C1");
        assertEquals(new Range(0.0, 1.0), r.findRangeBounds(dataset));

        dataset.addValue(-2.0, "R1", "C2");
        assertEquals(new Range(-2.0, 1.0), r.findRangeBounds(dataset));

        dataset.addValue(null, "R1", "C3");
        assertEquals(new Range(-2.0, 1.0), r.findRangeBounds(dataset));

        dataset.addValue(2.0, "R2", "C1");
        assertEquals(new Range(-2.0, 3.0), r.findRangeBounds(dataset));

        dataset.addValue(null, "R2", "C2");
        assertEquals(new Range(-2.0, 3.0), r.findRangeBounds(dataset));
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

// org.jfree.chart.renderer.category.junit.StackedBarRenderer3DTests::testPublicCloneable
    public void testPublicCloneable() {
        StackedBarRenderer3D r1 = new StackedBarRenderer3D();
        assertTrue(r1 instanceof PublicCloneable);
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
        List l = r.createStackedValueList(d, "c0", new int[] { 0 }, 0.0, false);
        assertEquals(2, l.size());
        assertEquals(new Double(0.0), ((Object[]) l.get(0))[1]);
        assertEquals(new Double(1.0), ((Object[]) l.get(1))[1]);
    }

// org.jfree.chart.renderer.category.junit.StackedBarRenderer3DTests::testCreateStackedValueList2
    public void testCreateStackedValueList2() {
        DefaultCategoryDataset d = new DefaultCategoryDataset();
        d.addValue(-1.0, "s0", "c0");
        MyRenderer r = new MyRenderer();
        List l = r.createStackedValueList(d, "c0", new int[] { 0 }, 0.0, false);
        assertEquals(2, l.size());
        assertEquals(new Double(-1.0), ((Object[]) l.get(0))[1]);
        assertEquals(new Double(0.0), ((Object[]) l.get(1))[1]);
    }

// org.jfree.chart.renderer.category.junit.StackedBarRenderer3DTests::testCreateStackedValueList3
    public void testCreateStackedValueList3() {
        DefaultCategoryDataset d = new DefaultCategoryDataset();
        d.addValue(0.0, "s0", "c0");
        MyRenderer r = new MyRenderer();
        List l = r.createStackedValueList(d, "c0", new int[] { 0 }, 0.0, false);
        assertEquals(2, l.size());
        assertEquals(new Double(0.0), ((Object[]) l.get(0))[1]);
        assertEquals(new Double(0.0), ((Object[]) l.get(1))[1]);
    }

// org.jfree.chart.renderer.category.junit.StackedBarRenderer3DTests::testCreateStackedValueList4
    public void testCreateStackedValueList4() {
        DefaultCategoryDataset d = new DefaultCategoryDataset();
        d.addValue(null, "s0", "c0");
        MyRenderer r = new MyRenderer();
        List l = r.createStackedValueList(d, "c0", new int[] { 0 }, 0.0, false);
        assertEquals(0, l.size());
    }

// org.jfree.chart.renderer.category.junit.StackedBarRenderer3DTests::testCreateStackedValueList1a
    public void testCreateStackedValueList1a() {
        DefaultCategoryDataset d = new DefaultCategoryDataset();
        d.addValue(1.0, "s0", "c0");
        d.addValue(1.1, "s1", "c0");
        MyRenderer r = new MyRenderer();
        List l = r.createStackedValueList(d, "c0", new int[] { 0, 1 }, 0.0,
                false);
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
        List l = r.createStackedValueList(d, "c0", new int[] { 0, 1 }, 0.0,
                false);
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
        List l = r.createStackedValueList(d, "c0", new int[] { 0, 1 }, 0.0,
                false);
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
        List l = r.createStackedValueList(d, "c0", new int[] { 0, 1 }, 0.0,
                false);
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
        List l = r.createStackedValueList(d, "c0", new int[] { 0, 1 }, 0.0,
                false);
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
        List l = r.createStackedValueList(d, "c0", new int[] { 0, 1 }, 0.0,
                false);
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
        List l = r.createStackedValueList(d, "c0", new int[] { 0, 1 }, 0.0,
                false);
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
        List l = r.createStackedValueList(d, "c0", new int[] { 0, 1 }, 0.0,
                false);
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
        List l = r.createStackedValueList(d, "c0", new int[] { 0, 1 }, 0.0,
                false);
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
        List l = r.createStackedValueList(d, "c0", new int[] { 0, 1 }, 0.0,
                false);
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
        List l = r.createStackedValueList(d, "c0", new int[] { 0, 1 }, 0.0,
                false);
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
        List l = r.createStackedValueList(d, "c0", new int[] { 0, 1 }, 0.0,
                false);
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
        List l = r.createStackedValueList(d, "c0", new int[] { 0, 1, 2 }, 0.0,
                false);
        assertEquals(3, l.size());
        assertEquals(new Double(0.0), ((Object[]) l.get(0))[1]);
        assertEquals(new Double(1.0), ((Object[]) l.get(1))[1]);
        assertEquals(new Double(3.0), ((Object[]) l.get(2))[1]);
    }

// org.jfree.chart.renderer.category.junit.StackedBarRendererTests::testEquals
    public void testEquals() {
        StackedBarRenderer r1 = new StackedBarRenderer();
        StackedBarRenderer r2 = new StackedBarRenderer();
        assertTrue(r1.equals(r2));
        assertTrue(r2.equals(r1));

        r1.setRenderAsPercentages(true);
        assertFalse(r1.equals(r2));
        r2.setRenderAsPercentages(true);
        assertTrue(r1.equals(r2));
    }

// org.jfree.chart.renderer.category.junit.StackedBarRendererTests::testHashCode
    public void testHashCode() {
        StackedBarRenderer r1 = new StackedBarRenderer();
        StackedBarRenderer r2 = new StackedBarRenderer();
        assertTrue(r1.equals(r2));
        int h1 = r1.hashCode();
        int h2 = r2.hashCode();
        assertEquals(h1, h2);
    }

// org.jfree.chart.renderer.category.junit.StackedBarRendererTests::testCloning
    public void testCloning() {
        StackedBarRenderer r1 = new StackedBarRenderer();
        StackedBarRenderer r2 = null;
        try {
            r2 = (StackedBarRenderer) r1.clone();
        }
        catch (CloneNotSupportedException e) {
            System.err.println("Failed to clone.");
        }
        assertTrue(r1 != r2);
        assertTrue(r1.getClass() == r2.getClass());
        assertTrue(r1.equals(r2));
    }

// org.jfree.chart.renderer.category.junit.StackedBarRendererTests::testPublicCloneable
    public void testPublicCloneable() {
        StackedBarRenderer r1 = new StackedBarRenderer();
        assertTrue(r1 instanceof PublicCloneable);
    }

// org.jfree.chart.renderer.category.junit.StackedBarRendererTests::testSerialization
    public void testSerialization() {

        StackedBarRenderer r1 = new StackedBarRenderer();
        StackedBarRenderer r2 = null;

        try {
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            ObjectOutput out = new ObjectOutputStream(buffer);
            out.writeObject(r1);
            out.close();

            ObjectInput in = new ObjectInputStream(
                    new ByteArrayInputStream(buffer.toByteArray()));
            r2 = (StackedBarRenderer) in.readObject();
            in.close();
        }
        catch (Exception e) {
            System.out.println(e.toString());
        }
        assertEquals(r1, r2);

    }

// org.jfree.chart.renderer.category.junit.StackedBarRendererTests::testFindRangeBounds
    public void testFindRangeBounds() {
        StackedBarRenderer r = new StackedBarRenderer();
        assertNull(r.findRangeBounds(null));

        
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        assertNull(r.findRangeBounds(dataset));

        dataset.addValue(1.0, "R1", "C1");
        assertEquals(new Range(0.0, 1.0), r.findRangeBounds(dataset));

        dataset.addValue(-2.0, "R1", "C2");
        assertEquals(new Range(-2.0, 1.0), r.findRangeBounds(dataset));

        dataset.addValue(null, "R1", "C3");
        assertEquals(new Range(-2.0, 1.0), r.findRangeBounds(dataset));

        dataset.addValue(2.0, "R2", "C1");
        assertEquals(new Range(-2.0, 3.0), r.findRangeBounds(dataset));

        dataset.addValue(null, "R2", "C2");
        assertEquals(new Range(-2.0, 3.0), r.findRangeBounds(dataset));
    }

// org.jfree.chart.renderer.category.junit.StatisticalBarRendererTests::testEquals
    public void testEquals() {
        StatisticalBarRenderer r1 = new StatisticalBarRenderer();
        StatisticalBarRenderer r2 = new StatisticalBarRenderer();
        assertEquals(r1, r2);

        r1.setErrorIndicatorPaint(Color.red);
        assertFalse(r1.equals(r2));
        r2.setErrorIndicatorPaint(Color.red);
        assertTrue(r2.equals(r1));

        r1.setErrorIndicatorStroke(new BasicStroke(1.5f));
        assertFalse(r1.equals(r2));
        r2.setErrorIndicatorStroke(new BasicStroke(1.5f));
        assertTrue(r2.equals(r1));
    }

// org.jfree.chart.renderer.category.junit.StatisticalBarRendererTests::testHashcode
    public void testHashcode() {
        StatisticalBarRenderer r1 = new StatisticalBarRenderer();
        StatisticalBarRenderer r2 = new StatisticalBarRenderer();
        assertTrue(r1.equals(r2));
        int h1 = r1.hashCode();
        int h2 = r2.hashCode();
        assertEquals(h1, h2);
    }

// org.jfree.chart.renderer.category.junit.StatisticalBarRendererTests::testCloning
    public void testCloning() {
        StatisticalBarRenderer r1 = new StatisticalBarRenderer();
        StatisticalBarRenderer r2 = null;
        try {
            r2 = (StatisticalBarRenderer) r1.clone();
        }
        catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        assertTrue(r1 != r2);
        assertTrue(r1.getClass() == r2.getClass());
        assertTrue(r1.equals(r2));
    }

// org.jfree.chart.renderer.category.junit.StatisticalBarRendererTests::testPublicCloneable
    public void testPublicCloneable() {
        StatisticalBarRenderer r1 = new StatisticalBarRenderer();
        assertTrue(r1 instanceof PublicCloneable);
    }

// org.jfree.chart.renderer.category.junit.StatisticalBarRendererTests::testSerialization
    public void testSerialization() {

        StatisticalBarRenderer r1 = new StatisticalBarRenderer();
        StatisticalBarRenderer r2 = null;

        try {
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            ObjectOutput out = new ObjectOutputStream(buffer);
            out.writeObject(r1);
            out.close();

            ObjectInput in = new ObjectInputStream(new ByteArrayInputStream(
                    buffer.toByteArray()));
            r2 = (StatisticalBarRenderer) in.readObject();
            in.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        assertEquals(r1, r2);

    }

// org.jfree.chart.renderer.category.junit.StatisticalBarRendererTests::testDrawWithNullInfo
    public void testDrawWithNullInfo() {
        boolean success = false;
        try {
            DefaultStatisticalCategoryDataset dataset
                    = new DefaultStatisticalCategoryDataset();
            dataset.add(1.0, 2.0, "S1", "C1");
            dataset.add(3.0, 4.0, "S1", "C2");
            CategoryPlot plot = new CategoryPlot(dataset,
                    new CategoryAxis("Category"), new NumberAxis("Value"),
                    new StatisticalBarRenderer());
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

// org.jfree.chart.renderer.category.junit.StatisticalBarRendererTests::testDrawWithNullMeanVertical
    public void testDrawWithNullMeanVertical() {
        boolean success = false;
        try {
            DefaultStatisticalCategoryDataset dataset
                    = new DefaultStatisticalCategoryDataset();
            dataset.add(1.0, 2.0, "S1", "C1");
            dataset.add(null, new Double(4.0), "S1", "C2");
            CategoryPlot plot = new CategoryPlot(dataset,
                    new CategoryAxis("Category"), new NumberAxis("Value"),
                    new StatisticalBarRenderer());
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

// org.jfree.chart.renderer.category.junit.StatisticalBarRendererTests::testDrawWithNullMeanHorizontal
    public void testDrawWithNullMeanHorizontal() {
        boolean success = false;
        try {
            DefaultStatisticalCategoryDataset dataset
                    = new DefaultStatisticalCategoryDataset();
            dataset.add(1.0, 2.0, "S1", "C1");
            dataset.add(null, new Double(4.0), "S1", "C2");
            CategoryPlot plot = new CategoryPlot(dataset,
                    new CategoryAxis("Category"), new NumberAxis("Value"),
                    new StatisticalBarRenderer());
            plot.setOrientation(PlotOrientation.HORIZONTAL);
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

// org.jfree.chart.renderer.category.junit.StatisticalBarRendererTests::testDrawWithNullDeviationVertical
    public void testDrawWithNullDeviationVertical() {
        boolean success = false;
        try {
            DefaultStatisticalCategoryDataset dataset
                    = new DefaultStatisticalCategoryDataset();
            dataset.add(1.0, 2.0, "S1", "C1");
            dataset.add(new Double(4.0), null, "S1", "C2");
            CategoryPlot plot = new CategoryPlot(dataset,
                    new CategoryAxis("Category"), new NumberAxis("Value"),
                    new StatisticalBarRenderer());
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

// org.jfree.chart.renderer.category.junit.StatisticalBarRendererTests::testDrawWithNullDeviationHorizontal
    public void testDrawWithNullDeviationHorizontal() {
        boolean success = false;
        try {
            DefaultStatisticalCategoryDataset dataset
                    = new DefaultStatisticalCategoryDataset();
            dataset.add(1.0, 2.0, "S1", "C1");
            dataset.add(new Double(4.0), null, "S1", "C2");
            CategoryPlot plot = new CategoryPlot(dataset,
                    new CategoryAxis("Category"), new NumberAxis("Value"),
                    new StatisticalBarRenderer());
            plot.setOrientation(PlotOrientation.HORIZONTAL);
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

// org.jfree.chart.renderer.category.junit.StatisticalLineAndShapeRendererTests::testEquals
    public void testEquals() {
        StatisticalLineAndShapeRenderer r1
            = new StatisticalLineAndShapeRenderer();
        StatisticalLineAndShapeRenderer r2
            = new StatisticalLineAndShapeRenderer();
        assertTrue(r1.equals(r2));
        assertTrue(r2.equals(r1));

        r1.setErrorIndicatorPaint(Color.red);
        assertFalse(r1.equals(r2));
        r2.setErrorIndicatorPaint(Color.red);
        assertTrue(r2.equals(r1));
    }

// org.jfree.chart.renderer.category.junit.StatisticalLineAndShapeRendererTests::testHashcode
    public void testHashcode() {
        StatisticalLineAndShapeRenderer r1
            = new StatisticalLineAndShapeRenderer();
        StatisticalLineAndShapeRenderer r2
            = new StatisticalLineAndShapeRenderer();
        assertTrue(r1.equals(r2));
        int h1 = r1.hashCode();
        int h2 = r2.hashCode();
        assertEquals(h1, h2);
    }

// org.jfree.chart.renderer.category.junit.StatisticalLineAndShapeRendererTests::testCloning
    public void testCloning() {
        StatisticalLineAndShapeRenderer r1
                = new StatisticalLineAndShapeRenderer();
        StatisticalLineAndShapeRenderer r2 = null;
        try {
            r2 = (StatisticalLineAndShapeRenderer) r1.clone();
        }
        catch (CloneNotSupportedException e) {
            System.err.println("Failed to clone.");
        }
        assertTrue(r1 != r2);
        assertTrue(r1.getClass() == r2.getClass());
        assertTrue(r1.equals(r2));
    }

// org.jfree.chart.renderer.category.junit.StatisticalLineAndShapeRendererTests::testPublicCloneable
    public void testPublicCloneable() {
        StatisticalLineAndShapeRenderer r1
                = new StatisticalLineAndShapeRenderer();
        assertTrue(r1 instanceof PublicCloneable);
    }

// org.jfree.chart.renderer.category.junit.StatisticalLineAndShapeRendererTests::testSerialization
    public void testSerialization() {

        StatisticalLineAndShapeRenderer r1
            = new StatisticalLineAndShapeRenderer();
        StatisticalLineAndShapeRenderer r2 = null;

        try {
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            ObjectOutput out = new ObjectOutputStream(buffer);
            out.writeObject(r1);
            out.close();

            ObjectInput in = new ObjectInputStream(
                    new ByteArrayInputStream(buffer.toByteArray()));
            r2 = (StatisticalLineAndShapeRenderer) in.readObject();
            in.close();
        }
        catch (Exception e) {
            System.out.println(e.toString());
        }
        assertEquals(r1, r2);

    }

// org.jfree.chart.renderer.category.junit.StatisticalLineAndShapeRendererTests::testDrawWithNullInfo
    public void testDrawWithNullInfo() {
        boolean success = false;
        try {
            DefaultStatisticalCategoryDataset dataset
                = new DefaultStatisticalCategoryDataset();
            dataset.add(1.0, 2.0, "S1", "C1");
            dataset.add(3.0, 4.0, "S1", "C2");
            CategoryPlot plot = new CategoryPlot(dataset,
                    new CategoryAxis("Category"), new NumberAxis("Value"),
                    new StatisticalLineAndShapeRenderer());
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

// org.jfree.chart.renderer.category.junit.StatisticalLineAndShapeRendererTests::test1562759
    public void test1562759() {
        StatisticalLineAndShapeRenderer r
            = new StatisticalLineAndShapeRenderer(true, false);
        assertTrue(r.getBaseLinesVisible());
        assertFalse(r.getBaseShapesVisible());

        r = new StatisticalLineAndShapeRenderer(false, true);
        assertFalse(r.getBaseLinesVisible());
        assertTrue(r.getBaseShapesVisible());
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

// org.jfree.chart.renderer.xy.junit.CandlestickRendererTests::testConstructor
    public void testConstructor() {
        CandlestickRenderer r1 = new CandlestickRenderer();

        
        assertEquals(Color.green, r1.getUpPaint());
        assertEquals(Color.red, r1.getDownPaint());
        assertFalse(r1.getUseOutlinePaint());
        assertTrue(r1.getDrawVolume());
        assertEquals(Color.gray, r1.getVolumePaint());
        assertEquals(-1.0, r1.getCandleWidth(), EPSILON);
    }

// org.jfree.chart.renderer.xy.junit.CandlestickRendererTests::testEquals
    public void testEquals() {
        CandlestickRenderer r1 = new CandlestickRenderer();
        CandlestickRenderer r2 = new CandlestickRenderer();
        assertEquals(r1, r2);

        
        r1.setUpPaint(new GradientPaint(1.0f, 2.0f, Color.red, 3.0f, 4.0f,
                Color.white));
        assertFalse(r1.equals(r2));
        r2.setUpPaint(new GradientPaint(1.0f, 2.0f, Color.red, 3.0f, 4.0f,
                Color.white));
        assertTrue(r1.equals(r2));

        
        r1.setDownPaint(new GradientPaint(5.0f, 6.0f, Color.green, 7.0f, 8.0f,
                Color.yellow));
        assertFalse(r1.equals(r2));
        r2.setDownPaint(new GradientPaint(5.0f, 6.0f, Color.green, 7.0f, 8.0f,
                Color.yellow));
        assertTrue(r1.equals(r2));

        
        r1.setDrawVolume(false);
        assertFalse(r1.equals(r2));
        r2.setDrawVolume(false);
        assertTrue(r1.equals(r2));

        
        r1.setCandleWidth(3.3);
        assertFalse(r1.equals(r2));
        r2.setCandleWidth(3.3);
        assertTrue(r1.equals(r2));

        
        r1.setMaxCandleWidthInMilliseconds(123);
        assertFalse(r1.equals(r2));
        r2.setMaxCandleWidthInMilliseconds(123);
        assertTrue(r1.equals(r2));

        
        r1.setAutoWidthMethod(CandlestickRenderer.WIDTHMETHOD_SMALLEST);
        assertFalse(r1.equals(r2));
        r2.setAutoWidthMethod(CandlestickRenderer.WIDTHMETHOD_SMALLEST);
        assertTrue(r1.equals(r2));

        
        r1.setAutoWidthFactor(0.22);
        assertFalse(r1.equals(r2));
        r2.setAutoWidthFactor(0.22);
        assertTrue(r1.equals(r2));

        
        r1.setAutoWidthGap(1.1);
        assertFalse(r1.equals(r2));
        r2.setAutoWidthGap(1.1);
        assertTrue(r1.equals(r2));

        r1.setUseOutlinePaint(true);
        assertFalse(r1.equals(r2));
        r2.setUseOutlinePaint(true);
        assertTrue(r1.equals(r2));

        r1.setVolumePaint(Color.blue);
        assertFalse(r1.equals(r2));
        r2.setVolumePaint(Color.blue);
        assertTrue(r1.equals(r2));
    }

// org.jfree.chart.renderer.xy.junit.CandlestickRendererTests::testHashcode
    public void testHashcode() {
        CandlestickRenderer r1 = new CandlestickRenderer();
        CandlestickRenderer r2 = new CandlestickRenderer();
        assertTrue(r1.equals(r2));
        int h1 = r1.hashCode();
        int h2 = r2.hashCode();
        assertEquals(h1, h2);
    }

// org.jfree.chart.renderer.xy.junit.CandlestickRendererTests::testCloning
    public void testCloning() {
        CandlestickRenderer r1 = new CandlestickRenderer();
        CandlestickRenderer r2 = null;
        try {
            r2 = (CandlestickRenderer) r1.clone();
        }
        catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        assertTrue(r1 != r2);
        assertTrue(r1.getClass() == r2.getClass());
        assertTrue(r1.equals(r2));
    }

// org.jfree.chart.renderer.xy.junit.CandlestickRendererTests::testPublicCloneable
    public void testPublicCloneable() {
        CandlestickRenderer r1 = new CandlestickRenderer();
        assertTrue(r1 instanceof PublicCloneable);
    }

// org.jfree.chart.renderer.xy.junit.CandlestickRendererTests::testSerialization
    public void testSerialization() {

        CandlestickRenderer r1 = new CandlestickRenderer();
        CandlestickRenderer r2 = null;

        try {
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            ObjectOutput out = new ObjectOutputStream(buffer);
            out.writeObject(r1);
            out.close();

            ObjectInput in = new ObjectInputStream(
                    new ByteArrayInputStream(buffer.toByteArray()));
            r2 = (CandlestickRenderer) in.readObject();
            in.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        assertEquals(r1, r2);

    }

// org.jfree.chart.renderer.xy.junit.CandlestickRendererTests::testFindRangeBounds
    public void testFindRangeBounds() {
        CandlestickRenderer renderer = new CandlestickRenderer();

        OHLCDataItem item1 = new OHLCDataItem(new Date(1L), 2.0, 4.0, 1.0, 3.0,
                100);
        OHLCDataset dataset = new DefaultOHLCDataset("S1",
                new OHLCDataItem[] {item1});
        Range range = renderer.findRangeBounds(dataset);
        assertEquals(new Range(1.0, 4.0), range);

        OHLCDataItem item2 = new OHLCDataItem(new Date(1L), -1.0, 3.0, -1.0,
                3.0, 100);
        dataset = new DefaultOHLCDataset("S1", new OHLCDataItem[] {item1,
                item2});
        range = renderer.findRangeBounds(dataset);
        assertEquals(new Range(-1.0, 4.0), range);

        
        dataset = new DefaultOHLCDataset("S1", new OHLCDataItem[] {});
        range = renderer.findRangeBounds(dataset);
        assertNull(range);

        
        range = renderer.findRangeBounds(null);
        assertNull(range);
    }

// org.jfree.chart.renderer.xy.junit.ClusteredXYBarRendererTests::testEquals
    public void testEquals() {
        ClusteredXYBarRenderer r1 = new ClusteredXYBarRenderer();
        ClusteredXYBarRenderer r2 = new ClusteredXYBarRenderer();
        assertEquals(r1, r2);
        assertEquals(r2, r1);

        r1 = new ClusteredXYBarRenderer(1.2, false);
        assertFalse(r1.equals(r2));
        r2 = new ClusteredXYBarRenderer(1.2, false);
        assertTrue(r1.equals(r2));

        r1 = new ClusteredXYBarRenderer(1.2, true);
        assertFalse(r1.equals(r2));
        r2 = new ClusteredXYBarRenderer(1.2, true);
        assertTrue(r1.equals(r2));
    }

// org.jfree.chart.renderer.xy.junit.ClusteredXYBarRendererTests::testHashcode
    public void testHashcode() {
        ClusteredXYBarRenderer r1 = new ClusteredXYBarRenderer();
        ClusteredXYBarRenderer r2 = new ClusteredXYBarRenderer();
        assertTrue(r1.equals(r2));
        int h1 = r1.hashCode();
        int h2 = r2.hashCode();
        assertEquals(h1, h2);
    }

// org.jfree.chart.renderer.xy.junit.ClusteredXYBarRendererTests::testCloning
    public void testCloning() {
        ClusteredXYBarRenderer r1 = new ClusteredXYBarRenderer();
        ClusteredXYBarRenderer r2 = null;
        try {
            r2 = (ClusteredXYBarRenderer) r1.clone();
        }
        catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        assertTrue(r1 != r2);
        assertTrue(r1.getClass() == r2.getClass());
        assertTrue(r1.equals(r2));
    }

// org.jfree.chart.renderer.xy.junit.ClusteredXYBarRendererTests::testPublicCloneable
    public void testPublicCloneable() {
        ClusteredXYBarRenderer r1 = new ClusteredXYBarRenderer();
        assertTrue(r1 instanceof PublicCloneable);
    }

// org.jfree.chart.renderer.xy.junit.ClusteredXYBarRendererTests::testSerialization
    public void testSerialization() {

        ClusteredXYBarRenderer r1 = new ClusteredXYBarRenderer();
        ClusteredXYBarRenderer r2 = null;

        try {
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            ObjectOutput out = new ObjectOutputStream(buffer);
            out.writeObject(r1);
            out.close();

            ObjectInput in = new ObjectInputStream(
                    new ByteArrayInputStream(buffer.toByteArray()));
            r2 = (ClusteredXYBarRenderer) in.readObject();
            in.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        assertEquals(r1, r2);

    }

// org.jfree.chart.renderer.xy.junit.ClusteredXYBarRendererTests::testFindDomainBounds
    public void testFindDomainBounds() {
        AbstractXYItemRenderer renderer = new ClusteredXYBarRenderer();
        XYDataset dataset = createSampleDataset1();
        Range r = renderer.findDomainBounds(dataset);
        assertEquals(0.9, r.getLowerBound(), EPSILON);
        assertEquals(13.1, r.getUpperBound(), EPSILON);

        renderer = new ClusteredXYBarRenderer(0.0, true);
        r = renderer.findDomainBounds(dataset);
        assertEquals(0.8, r.getLowerBound(), EPSILON);
        assertEquals(13.0, r.getUpperBound(), EPSILON);

        
        assertTrue(renderer.findDomainBounds(null) == null);
    }

// org.jfree.chart.renderer.xy.junit.HighLowRendererTests::testEquals
    public void testEquals() {
        HighLowRenderer r1 = new HighLowRenderer();
        HighLowRenderer r2 = new HighLowRenderer();
        assertEquals(r1, r2);

        
        r1.setDrawOpenTicks(false);
        assertFalse(r1.equals(r2));
        r2.setDrawOpenTicks(false);
        assertTrue(r1.equals(r2));

        
        r1.setDrawCloseTicks(false);
        assertFalse(r1.equals(r2));
        r2.setDrawCloseTicks(false);
        assertTrue(r1.equals(r2));

        
        r1.setOpenTickPaint(Color.red);
        assertFalse(r1.equals(r2));
        r2.setOpenTickPaint(Color.red);
        assertTrue(r1.equals(r2));

        
        r1.setCloseTickPaint(Color.blue);
        assertFalse(r1.equals(r2));
        r2.setCloseTickPaint(Color.blue);
        assertTrue(r1.equals(r2));

        
        r1.setTickLength(99.9);
        assertFalse(r1.equals(r2));
        r2.setTickLength(99.9);
        assertTrue(r1.equals(r2));
    }

// org.jfree.chart.renderer.xy.junit.HighLowRendererTests::testHashcode
    public void testHashcode() {
        HighLowRenderer r1 = new HighLowRenderer();
        HighLowRenderer r2 = new HighLowRenderer();
        assertTrue(r1.equals(r2));
        int h1 = r1.hashCode();
        int h2 = r2.hashCode();
        assertEquals(h1, h2);
    }

// org.jfree.chart.renderer.xy.junit.HighLowRendererTests::testCloning
    public void testCloning() {
        HighLowRenderer r1 = new HighLowRenderer();
        r1.setCloseTickPaint(Color.green);
        HighLowRenderer r2 = null;
        try {
            r2 = (HighLowRenderer) r1.clone();
        }
        catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        assertTrue(r1 != r2);
        assertTrue(r1.getClass() == r2.getClass());
        assertTrue(r1.equals(r2));
    }

// org.jfree.chart.renderer.xy.junit.HighLowRendererTests::testPublicCloneable
    public void testPublicCloneable() {
        HighLowRenderer r1 = new HighLowRenderer();
        assertTrue(r1 instanceof PublicCloneable);
    }

// org.jfree.chart.renderer.xy.junit.HighLowRendererTests::testSerialization
    public void testSerialization() {

        HighLowRenderer r1 = new HighLowRenderer();
        r1.setCloseTickPaint(Color.green);
        HighLowRenderer r2 = null;

        try {
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            ObjectOutput out = new ObjectOutputStream(buffer);
            out.writeObject(r1);
            out.close();

            ObjectInput in = new ObjectInputStream(
                    new ByteArrayInputStream(buffer.toByteArray()));
            r2 = (HighLowRenderer) in.readObject();
            in.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        assertEquals(r1, r2);

    }

// org.jfree.chart.renderer.xy.junit.HighLowRendererTests::testFindRangeBounds
    public void testFindRangeBounds() {
        HighLowRenderer renderer = new HighLowRenderer();

        OHLCDataItem item1 = new OHLCDataItem(new Date(1L), 2.0, 4.0, 1.0, 3.0,
                100);
        OHLCDataset dataset = new DefaultOHLCDataset("S1",
                new OHLCDataItem[] {item1});
        Range range = renderer.findRangeBounds(dataset);
        assertEquals(new Range(1.0, 4.0), range);

        OHLCDataItem item2 = new OHLCDataItem(new Date(1L), -1.0, 3.0, -1.0,
                3.0, 100);
        dataset = new DefaultOHLCDataset("S1", new OHLCDataItem[] {item1,
                item2});
        range = renderer.findRangeBounds(dataset);
        assertEquals(new Range(-1.0, 4.0), range);

        
        dataset = new DefaultOHLCDataset("S1", new OHLCDataItem[] {});
        range = renderer.findRangeBounds(dataset);
        assertNull(range);

        
        range = renderer.findRangeBounds(null);
        assertNull(range);
    }

// org.jfree.chart.renderer.xy.junit.StackedXYAreaRenderer2Tests::testDrawWithEmptyDataset
    public void testDrawWithEmptyDataset() {
        boolean success = false;
        JFreeChart chart = ChartFactory.createStackedXYAreaChart("title", "x",
                "y", new DefaultTableXYDataset(), true);
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
                "Test Chart", "X", "Y", dataset, false);
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
                "Test Chart", "X", "Y", dataset, false);
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
        JFreeChart chart = ChartFactory.createStackedXYAreaChart("Test Chart",
                "X", "Y", dataset, false);
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
        JFreeChart chart = ChartFactory.createStackedXYAreaChart("Test Chart",
                "X", "Y", dataset, false);
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
                dataset, false);
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
                false, "Y", dataset, false);
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

// org.jfree.chart.renderer.xy.junit.XYBlockRendererTests::testEquals
    public void testEquals() {

        
        XYBlockRenderer r1 = new XYBlockRenderer();
        XYBlockRenderer r2 = new XYBlockRenderer();
        assertTrue(r1.equals(r2));
        assertTrue(r2.equals(r1));

        
        r1.setBlockHeight(2.0);
        assertFalse(r1.equals(r2));
        r2.setBlockHeight(2.0);
        assertTrue(r1.equals(r2));

        
        r1.setBlockWidth(2.0);
        assertFalse(r1.equals(r2));
        r2.setBlockWidth(2.0);
        assertTrue(r1.equals(r2));

        
        r1.setPaintScale(new GrayPaintScale(0.0, 1.0));
        assertFalse(r1.equals(r2));
        r2.setPaintScale(new GrayPaintScale(0.0, 1.0));
        assertTrue(r1.equals(r2));

    }

// org.jfree.chart.renderer.xy.junit.XYBlockRendererTests::testHashcode
    public void testHashcode() {
        XYBlockRenderer r1 = new XYBlockRenderer();
        XYBlockRenderer r2 = new XYBlockRenderer();
        assertTrue(r1.equals(r2));
        int h1 = r1.hashCode();
        int h2 = r2.hashCode();
        assertEquals(h1, h2);
    }

// org.jfree.chart.renderer.xy.junit.XYBlockRendererTests::testCloning
    public void testCloning() {
        XYBlockRenderer r1 = new XYBlockRenderer();
        LookupPaintScale scale1 = new LookupPaintScale();
        r1.setPaintScale(scale1);
        XYBlockRenderer r2 = null;
        try {
            r2 = (XYBlockRenderer) r1.clone();
        }
        catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        assertTrue(r1 != r2);
        assertTrue(r1.getClass() == r2.getClass());
        assertTrue(r1.equals(r2));

        
        scale1.add(0.5, Color.red);
        assertFalse(r1.equals(r2));
        LookupPaintScale scale2 = (LookupPaintScale) r2.getPaintScale();
        scale2.add(0.5, Color.red);
        assertTrue(r1.equals(r2));
    }

// org.jfree.chart.renderer.xy.junit.XYBlockRendererTests::testPublicCloneable
    public void testPublicCloneable() {
        XYBlockRenderer r1 = new XYBlockRenderer();
        assertTrue(r1 instanceof PublicCloneable);
    }

// org.jfree.chart.renderer.xy.junit.XYBlockRendererTests::testSerialization
    public void testSerialization() {
        XYBlockRenderer r1 = new XYBlockRenderer();
        XYBlockRenderer r2 = null;
        try {
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            ObjectOutput out = new ObjectOutputStream(buffer);
            out.writeObject(r1);
            out.close();

            ObjectInput in = new ObjectInputStream(
                    new ByteArrayInputStream(buffer.toByteArray()));
            r2 = (XYBlockRenderer) in.readObject();
            in.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        assertEquals(r1, r2);
    }

// org.jfree.chart.renderer.xy.junit.XYBlockRendererTests::testBug1766646A
    public void testBug1766646A() {
        XYBlockRenderer r = new XYBlockRenderer();
        Range range = r.findDomainBounds(null);
        assertTrue(range == null);
        DefaultXYZDataset emptyDataset = new DefaultXYZDataset();
        range = r.findDomainBounds(emptyDataset);
        assertTrue(range == null);
    }

// org.jfree.chart.renderer.xy.junit.XYBlockRendererTests::testBug1766646B
    public void testBug1766646B() {
        XYBlockRenderer r = new XYBlockRenderer();
        Range range = r.findRangeBounds(null);
        assertTrue(range == null);
        DefaultXYZDataset emptyDataset = new DefaultXYZDataset();
        range = r.findRangeBounds(emptyDataset);
        assertTrue(range == null);
    }

// org.jfree.chart.renderer.xy.junit.XYBubbleRendererTests::testEquals
    public void testEquals() {
        XYBubbleRenderer r1 = new XYBubbleRenderer();
        XYBubbleRenderer r2 = new XYBubbleRenderer();
        assertEquals(r1, r2);

        r1 = new XYBubbleRenderer(XYBubbleRenderer.SCALE_ON_RANGE_AXIS);
        assertFalse(r1.equals(r2));
        r2 = new XYBubbleRenderer(XYBubbleRenderer.SCALE_ON_RANGE_AXIS);
        assertTrue(r1.equals(r2));
    }

// org.jfree.chart.renderer.xy.junit.XYBubbleRendererTests::testHashcode
    public void testHashcode() {
        XYBubbleRenderer r1 = new XYBubbleRenderer();
        XYBubbleRenderer r2 = new XYBubbleRenderer();
        assertTrue(r1.equals(r2));
        int h1 = r1.hashCode();
        int h2 = r2.hashCode();
        assertEquals(h1, h2);
    }

// org.jfree.chart.renderer.xy.junit.XYBubbleRendererTests::testCloning
    public void testCloning() {
        XYBubbleRenderer r1 = new XYBubbleRenderer();
        XYBubbleRenderer r2 = null;
        try {
            r2 = (XYBubbleRenderer) r1.clone();
        }
        catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        assertTrue(r1 != r2);
        assertTrue(r1.getClass() == r2.getClass());
        assertTrue(r1.equals(r2));
    }

// org.jfree.chart.renderer.xy.junit.XYBubbleRendererTests::testPublicCloneable
    public void testPublicCloneable() {
        XYBubbleRenderer r1 = new XYBubbleRenderer();
        assertTrue(r1 instanceof PublicCloneable);
    }

// org.jfree.chart.renderer.xy.junit.XYBubbleRendererTests::testSerialization
    public void testSerialization() {
        XYBubbleRenderer r1 = new XYBubbleRenderer();
        XYBubbleRenderer r2 = null;

        try {
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            ObjectOutput out = new ObjectOutputStream(buffer);
            out.writeObject(r1);
            out.close();

            ObjectInput in = new ObjectInputStream(
                    new ByteArrayInputStream(buffer.toByteArray()));
            r2 = (XYBubbleRenderer) in.readObject();
            in.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        assertEquals(r1, r2);
    }

// org.jfree.chart.renderer.xy.junit.XYBubbleRendererTests::testGetLegendItemSeriesIndex
    public void testGetLegendItemSeriesIndex() {
        DefaultXYZDataset d1 = new DefaultXYZDataset();
        double[] x = {2.1, 2.3, 2.3, 2.2, 2.2, 1.8, 1.8, 1.9, 2.3, 3.8};
        double[] y = {14.1, 11.1, 10.0, 8.8, 8.7, 8.4, 5.4, 4.1, 4.1, 25};
        double[] z = {2.4, 2.7, 2.7, 2.2, 2.2, 2.2, 2.1, 2.2, 1.6, 4};
        double[][] s1 = new double[][] {x, y, z};
        d1.addSeries("S1", s1);
        x = new double[] {2.1};
        y = new double[] {14.1};
        z = new double[] {2.4};
        double[][] s2 = new double[][] {x, y, z};
        d1.addSeries("S2", s2);

        DefaultXYZDataset d2 = new DefaultXYZDataset();
        x = new double[] {2.1};
        y = new double[] {14.1};
        z = new double[] {2.4};
        double[][] s3 = new double[][] {x, y, z};
        d2.addSeries("S3", s3);
        x = new double[] {2.1};
        y = new double[] {14.1};
        z = new double[] {2.4};
        double[][] s4 = new double[][] {x, y, z};
        d2.addSeries("S4", s4);
        x = new double[] {2.1};
        y = new double[] {14.1};
        z = new double[] {2.4};
        double[][] s5 = new double[][] {x, y, z};
        d2.addSeries("S5", s5);

        XYBubbleRenderer r = new XYBubbleRenderer();
        XYPlot plot = new XYPlot(d1, new NumberAxis("x"),
                new NumberAxis("y"), r);
        plot.setDataset(1, d2);
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
                "Test Chart", "X", "Y", dataset, false);
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
        JFreeChart chart = ChartFactory.createXYLineChart("Test Chart", 
                "X", "Y", dataset, false);
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

// org.jfree.chart.renderer.xy.junit.YIntervalRendererTests::testEquals
    public void testEquals() {
        YIntervalRenderer r1 = new YIntervalRenderer();
        YIntervalRenderer r2 = new YIntervalRenderer();
        assertEquals(r1, r2);

        
        r1.setSeriesItemLabelGenerator(0, new StandardXYItemLabelGenerator());
        assertFalse(r1.equals(r2));
        r2.setSeriesItemLabelGenerator(0, new StandardXYItemLabelGenerator());
        assertTrue(r1.equals(r2));

        r1.setBaseItemLabelGenerator(new StandardXYItemLabelGenerator());
        assertFalse(r1.equals(r2));
        r2.setBaseItemLabelGenerator(new StandardXYItemLabelGenerator());
        assertTrue(r1.equals(r2));

        r1.setSeriesToolTipGenerator(0, new StandardXYToolTipGenerator());
        assertFalse(r1.equals(r2));
        r2.setSeriesToolTipGenerator(0, new StandardXYToolTipGenerator());
        assertTrue(r1.equals(r2));

        r1.setBaseToolTipGenerator(new StandardXYToolTipGenerator());
        assertFalse(r1.equals(r2));
        r2.setBaseToolTipGenerator(new StandardXYToolTipGenerator());
        assertTrue(r1.equals(r2));

        r1.setSeriesURLGenerator(0, new StandardXYURLGenerator());
        assertFalse(r1.equals(r2));
        r2.setSeriesURLGenerator(0, new StandardXYURLGenerator());
        assertTrue(r1.equals(r2));

        r1.setBaseURLGenerator(new StandardXYURLGenerator());
        assertFalse(r1.equals(r2));
        r2.setBaseURLGenerator(new StandardXYURLGenerator());
        assertTrue(r1.equals(r2));

        r1.addAnnotation(new XYTextAnnotation("X", 1.0, 2.0), Layer.FOREGROUND);
        assertFalse(r1.equals(r2));
        r2.addAnnotation(new XYTextAnnotation("X", 1.0, 2.0), Layer.FOREGROUND);
        assertTrue(r1.equals(r2));

        r1.addAnnotation(new XYTextAnnotation("X", 1.0, 2.0), Layer.BACKGROUND);
        assertFalse(r1.equals(r2));
        r2.addAnnotation(new XYTextAnnotation("X", 1.0, 2.0), Layer.BACKGROUND);
        assertTrue(r1.equals(r2));

        r1.setDefaultEntityRadius(99);
        assertFalse(r1.equals(r2));
        r2.setDefaultEntityRadius(99);
        assertTrue(r1.equals(r2));

        r1.setLegendItemLabelGenerator(new StandardXYSeriesLabelGenerator(
                "{0} {1}"));
        assertFalse(r1.equals(r2));
        r2.setLegendItemLabelGenerator(new StandardXYSeriesLabelGenerator(
                "{0} {1}"));
        assertTrue(r1.equals(r2));

        r1.setLegendItemToolTipGenerator(new StandardXYSeriesLabelGenerator());
        assertFalse(r1.equals(r2));
        r2.setLegendItemToolTipGenerator(new StandardXYSeriesLabelGenerator());
        assertTrue(r1.equals(r2));

        r1.setLegendItemURLGenerator(new StandardXYSeriesLabelGenerator());
        assertFalse(r1.equals(r2));
        r2.setLegendItemURLGenerator(new StandardXYSeriesLabelGenerator());
        assertTrue(r1.equals(r2));

        r1.setAdditionalItemLabelGenerator(new IntervalXYItemLabelGenerator());
        assertFalse(r1.equals(r2));
        r2.setAdditionalItemLabelGenerator(new IntervalXYItemLabelGenerator());
        assertTrue(r1.equals(r2));

    }

// org.jfree.chart.renderer.xy.junit.YIntervalRendererTests::testHashcode
    public void testHashcode() {
        YIntervalRenderer r1 = new YIntervalRenderer();
        YIntervalRenderer r2 = new YIntervalRenderer();
        assertTrue(r1.equals(r2));
        int h1 = r1.hashCode();
        int h2 = r2.hashCode();
        assertEquals(h1, h2);
    }

// org.jfree.chart.renderer.xy.junit.YIntervalRendererTests::testCloning
    public void testCloning() {
        YIntervalRenderer r1 = new YIntervalRenderer();
        YIntervalRenderer r2 = null;
        try {
            r2 = (YIntervalRenderer) r1.clone();
        }
        catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        assertTrue(r1 != r2);
        assertTrue(r1.getClass() == r2.getClass());
        assertTrue(r1.equals(r2));

        
        r1.setSeriesItemLabelGenerator(0, new StandardXYItemLabelGenerator());
        assertFalse(r1.equals(r2));
        r2.setSeriesItemLabelGenerator(0, new StandardXYItemLabelGenerator());
        assertTrue(r1.equals(r2));

        r1.setSeriesToolTipGenerator(0, new StandardXYToolTipGenerator());
        assertFalse(r1.equals(r2));
        r2.setSeriesToolTipGenerator(0, new StandardXYToolTipGenerator());
        assertTrue(r1.equals(r2));

        r1.addAnnotation(new XYTextAnnotation("ABC", 1.0, 2.0),
                Layer.FOREGROUND);
        assertFalse(r1.equals(r2));
        r2.addAnnotation(new XYTextAnnotation("ABC", 1.0, 2.0),
                Layer.FOREGROUND);
        assertTrue(r1.equals(r2));

        r1.addAnnotation(new XYTextAnnotation("ABC", 1.0, 2.0),
                Layer.BACKGROUND);
        assertFalse(r1.equals(r2));
        r2.addAnnotation(new XYTextAnnotation("ABC", 1.0, 2.0),
                Layer.BACKGROUND);
        assertTrue(r1.equals(r2));

    }

// org.jfree.chart.renderer.xy.junit.YIntervalRendererTests::testPublicCloneable
    public void testPublicCloneable() {
        YIntervalRenderer r1 = new YIntervalRenderer();
        assertTrue(r1 instanceof PublicCloneable);
    }

// org.jfree.chart.renderer.xy.junit.YIntervalRendererTests::testSerialization
    public void testSerialization() {

        YIntervalRenderer r1 = new YIntervalRenderer();
        YIntervalRenderer r2 = null;

        try {
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            ObjectOutput out = new ObjectOutputStream(buffer);
            out.writeObject(r1);
            out.close();

            ObjectInput in = new ObjectInputStream(
                    new ByteArrayInputStream(buffer.toByteArray()));
            r2 = (YIntervalRenderer) in.readObject();
            in.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        assertEquals(r1, r2);

    }

// org.jfree.chart.renderer.xy.junit.YIntervalRendererTests::testGetLegendItemSeriesIndex
    public void testGetLegendItemSeriesIndex() {
        YIntervalSeriesCollection d1 = new YIntervalSeriesCollection();
        YIntervalSeries s1 = new YIntervalSeries("S1");
        s1.add(1.0, 1.1, 1.2, 1.3);
        YIntervalSeries s2 = new YIntervalSeries("S2");
        s2.add(1.0, 1.1, 1.2, 1.3);
        d1.addSeries(s1);
        d1.addSeries(s2);

        YIntervalSeriesCollection d2 = new YIntervalSeriesCollection();
        YIntervalSeries s3 = new YIntervalSeries("S3");
        s3.add(1.0, 1.1, 1.2, 1.3);
        YIntervalSeries s4 = new YIntervalSeries("S4");
        s4.add(1.0, 1.1, 1.2, 1.3);
        YIntervalSeries s5 = new YIntervalSeries("S5");
        s5.add(1.0, 1.1, 1.2, 1.3);
        d2.addSeries(s3);
        d2.addSeries(s4);
        d2.addSeries(s5);

        YIntervalRenderer r = new YIntervalRenderer();
        XYPlot plot = new XYPlot(d1, new NumberAxis("x"),
                new NumberAxis("y"), r);
        plot.setDataset(1, d2);
         new JFreeChart(plot);
        LegendItem li = r.getLegendItem(1, 2);
        assertEquals("S5", li.getLabel());
        assertEquals(1, li.getDatasetIndex());
        assertEquals(2, li.getSeriesIndex());
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

// org.jfree.data.general.junit.DatasetUtilitiesTests::testIterateToFindRangeBounds1_XYDataset
    public void testIterateToFindRangeBounds1_XYDataset() {
        
        boolean pass = false;
        try {
            DatasetUtilities.iterateToFindRangeBounds(null, new ArrayList(),
                    new Range(0.0, 1.0), true);
        }
        catch (IllegalArgumentException e) {
            pass = true;
        }
        assertTrue(pass);

        
        pass = false;
        try {
            DatasetUtilities.iterateToFindRangeBounds(new XYSeriesCollection(),
                    null, new Range(0.0, 1.0), true);
        }
        catch (IllegalArgumentException e) {
            pass = true;
        }
        assertTrue(pass);

        
        pass = false;
        try {
            DatasetUtilities.iterateToFindRangeBounds(new XYSeriesCollection(),
                    new ArrayList(), null, true);
        }
        catch (IllegalArgumentException e) {
            pass = true;
        }
        assertTrue(pass);
    }

// org.jfree.data.general.junit.DatasetUtilitiesTests::testIterateToFindRangeBounds2_XYDataset
    public void testIterateToFindRangeBounds2_XYDataset() {
        List visibleSeriesKeys = new ArrayList();
        Range xRange = new Range(0.0, 10.0);

        
        XYSeriesCollection dataset = new XYSeriesCollection();
        Range r = DatasetUtilities.iterateToFindRangeBounds(dataset,
                visibleSeriesKeys, xRange, false);
        assertNull(r);

        
        XYSeries s1 = new XYSeries("A");
        dataset.addSeries(s1);
        visibleSeriesKeys.add("A");
        r = DatasetUtilities.iterateToFindRangeBounds(dataset,
                visibleSeriesKeys, xRange, false);
        assertNull(r);

        
        s1.add(1.0, null);
        r = DatasetUtilities.iterateToFindRangeBounds(dataset,
                visibleSeriesKeys, xRange, false);
        assertNull(r);

        
        s1.add(2.0, Double.NaN);
        r = DatasetUtilities.iterateToFindRangeBounds(dataset,
                visibleSeriesKeys, xRange, false);
        assertNull(r);

        
        s1.add(3.0, 5.0);
        r = DatasetUtilities.iterateToFindRangeBounds(dataset,
                visibleSeriesKeys, xRange, false);
        assertEquals(new Range(5.0, 5.0), r);

        
        s1.add(4.0, 6.0);
        r = DatasetUtilities.iterateToFindRangeBounds(dataset,
                visibleSeriesKeys, xRange, false);
        assertEquals(new Range(5.0, 6.0), r);

        
        XYSeries s2 = new XYSeries("B");
        dataset.addSeries(s2);
        r = DatasetUtilities.iterateToFindRangeBounds(dataset,
                visibleSeriesKeys, xRange, false);
        assertEquals(new Range(5.0, 6.0), r);
        visibleSeriesKeys.add("B");
        r = DatasetUtilities.iterateToFindRangeBounds(dataset,
                visibleSeriesKeys, xRange, false);
        assertEquals(new Range(5.0, 6.0), r);

        
        s2.add(5.0, 15.0);
        r = DatasetUtilities.iterateToFindRangeBounds(dataset,
                visibleSeriesKeys, xRange, false);
        assertEquals(new Range(5.0, 15.0), r);

        
        s2.add(15.0, 150.0);
        r = DatasetUtilities.iterateToFindRangeBounds(dataset,
                visibleSeriesKeys, xRange, false);
        assertEquals(new Range(5.0, 15.0), r);

        r = DatasetUtilities.iterateToFindRangeBounds(dataset,
                visibleSeriesKeys, new Range(0.0, 20.0), false);
        assertEquals(new Range(5.0, 150.0), r);
    }

// org.jfree.data.general.junit.DatasetUtilitiesTests::testIterateToFindRangeBounds_BoxAndWhiskerXYDataset
    public void testIterateToFindRangeBounds_BoxAndWhiskerXYDataset() {
        DefaultBoxAndWhiskerXYDataset dataset
                = new DefaultBoxAndWhiskerXYDataset("Series 1");
        List visibleSeriesKeys = new ArrayList();
        visibleSeriesKeys.add("Series 1");
        Range xRange = new Range(Double.NEGATIVE_INFINITY,
                Double.POSITIVE_INFINITY);
        assertNull(DatasetUtilities.iterateToFindRangeBounds(dataset,
                visibleSeriesKeys, xRange, false));

        dataset.add(new Date(50L), new BoxAndWhiskerItem(5.0, 4.9, 2.0, 8.0,
                1.0, 9.0, 0.0, 10.0, new ArrayList()));
        assertEquals(new Range(5.0, 5.0),
                DatasetUtilities.iterateToFindRangeBounds(dataset,
                visibleSeriesKeys, xRange, false));
        assertEquals(new Range(1.0, 9.0),
                DatasetUtilities.iterateToFindRangeBounds(dataset,
                visibleSeriesKeys, xRange, true));
    }

// org.jfree.data.general.junit.DatasetUtilitiesTests::testIterateToFindRangeBounds_StatisticalCategoryDataset
    public void testIterateToFindRangeBounds_StatisticalCategoryDataset() {
        DefaultStatisticalCategoryDataset dataset
                = new DefaultStatisticalCategoryDataset();
        List visibleSeriesKeys = new ArrayList();
        assertNull(DatasetUtilities.iterateToFindRangeBounds(dataset,
                visibleSeriesKeys, false));
        dataset.add(1.0, 0.5, "R1", "C1");
        visibleSeriesKeys.add("R1");
        assertEquals(new Range(1.0, 1.0),
                DatasetUtilities.iterateToFindRangeBounds(dataset,
                visibleSeriesKeys, false));
        assertEquals(new Range(0.5, 1.5),
                DatasetUtilities.iterateToFindRangeBounds(dataset,
                visibleSeriesKeys, true));
    }

// org.jfree.data.general.junit.DatasetUtilitiesTests::testIterateToFindRangeBounds_MultiValueCategoryDataset
    public void testIterateToFindRangeBounds_MultiValueCategoryDataset() {
        DefaultMultiValueCategoryDataset dataset
                = new DefaultMultiValueCategoryDataset();
        List visibleSeriesKeys = new ArrayList();
        assertNull(DatasetUtilities.iterateToFindRangeBounds(dataset,
                visibleSeriesKeys, true));
        List values = Arrays.asList(new Double[] {new Double(1.0)});
        dataset.add(values, "R1", "C1");
        visibleSeriesKeys.add("R1");
        assertEquals(new Range(1.0, 1.0),
                DatasetUtilities.iterateToFindRangeBounds(dataset,
                visibleSeriesKeys, true));

        values = Arrays.asList(new Double[] {new Double(2.0), new Double(3.0)});
        dataset.add(values, "R1", "C2");
        assertEquals(new Range(1.0, 3.0),
                DatasetUtilities.iterateToFindRangeBounds(dataset,
                visibleSeriesKeys, true));

        values = Arrays.asList(new Double[] {new Double(-1.0),
                new Double(-2.0)});
        dataset.add(values, "R2", "C1");
        assertEquals(new Range(1.0, 3.0),
                DatasetUtilities.iterateToFindRangeBounds(dataset,
                visibleSeriesKeys, true));
        visibleSeriesKeys.add("R2");
        assertEquals(new Range(-2.0, 3.0),
                DatasetUtilities.iterateToFindRangeBounds(dataset,
                visibleSeriesKeys, true));
    }

// org.jfree.data.general.junit.DatasetUtilitiesTests::testIterateRangeBounds_IntervalCategoryDataset
    public void testIterateRangeBounds_IntervalCategoryDataset() {}

// org.jfree.data.general.junit.DatasetUtilitiesTests::testBug2849731
    public void testBug2849731() {}

// org.jfree.data.general.junit.DatasetUtilitiesTests::testBug2849731_2
    public void testBug2849731_2() {
        XYIntervalSeriesCollection d = new XYIntervalSeriesCollection();
        XYIntervalSeries s = new XYIntervalSeries("S1");
        s.add(1.0, Double.NaN, Double.NaN, Double.NaN, 1.5, Double.NaN);
        d.addSeries(s);
        Range r = DatasetUtilities.iterateDomainBounds(d);
        assertEquals(1.0, r.getLowerBound(), EPSILON);
        assertEquals(1.0, r.getUpperBound(), EPSILON);

        s.add(1.0, 1.5, Double.NaN, Double.NaN, 1.5, Double.NaN);
        r = DatasetUtilities.iterateDomainBounds(d);
        assertEquals(1.0, r.getLowerBound(), EPSILON);
        assertEquals(1.5, r.getUpperBound(), EPSILON);

        s.add(1.0, Double.NaN, 0.5, Double.NaN, 1.5, Double.NaN);
        r = DatasetUtilities.iterateDomainBounds(d);
        assertEquals(0.5, r.getLowerBound(), EPSILON);
        assertEquals(1.5, r.getUpperBound(), EPSILON);
    }

// org.jfree.data.general.junit.DatasetUtilitiesTests::testBug2849731_3
    public void testBug2849731_3() {
        XYIntervalSeriesCollection d = new XYIntervalSeriesCollection();
        XYIntervalSeries s = new XYIntervalSeries("S1");
        s.add(1.0, Double.NaN, Double.NaN, 1.5, Double.NaN, Double.NaN);
        d.addSeries(s);
        Range r = DatasetUtilities.iterateRangeBounds(d);
        assertEquals(1.5, r.getLowerBound(), EPSILON);
        assertEquals(1.5, r.getUpperBound(), EPSILON);

        s.add(1.0, 1.5, Double.NaN, Double.NaN, Double.NaN, 2.5);
        r = DatasetUtilities.iterateRangeBounds(d);
        assertEquals(1.5, r.getLowerBound(), EPSILON);
        assertEquals(2.5, r.getUpperBound(), EPSILON);

        s.add(1.0, Double.NaN, 0.5, Double.NaN, 3.5, Double.NaN);
        r = DatasetUtilities.iterateRangeBounds(d);
        assertEquals(1.5, r.getLowerBound(), EPSILON);
        assertEquals(3.5, r.getUpperBound(), EPSILON);
    }

// org.jfree.data.xy.junit.DefaultOHLCDatasetTests::testDataRange
    public void testDataRange() {
        OHLCDataItem[] data = new OHLCDataItem[3];
        data[0] = new OHLCDataItem(new Date(11L), 2.0, 4.0, 1.0, 3.0, 100.0);
        data[1] = new OHLCDataItem(new Date(22L), 4.0, 9.0, 2.0, 5.0, 120.0);
        data[2] = new OHLCDataItem(new Date(33L), 3.0, 7.0, 3.0, 6.0, 140.0);
        DefaultOHLCDataset d = new DefaultOHLCDataset("S1", data);
        Range r = DatasetUtilities.findRangeBounds(d, true);
        assertEquals(1.0, r.getLowerBound(), EPSILON);
        assertEquals(9.0, r.getUpperBound(), EPSILON);
    }

// org.jfree.data.xy.junit.DefaultOHLCDatasetTests::testEquals
    public void testEquals() {
        DefaultOHLCDataset d1 = new DefaultOHLCDataset("Series 1",
                new OHLCDataItem[0]);
        DefaultOHLCDataset d2 = new DefaultOHLCDataset("Series 1",
                new OHLCDataItem[0]);
        assertTrue(d1.equals(d2));
        assertTrue(d2.equals(d1));

        d1 = new DefaultOHLCDataset("Series 2", new OHLCDataItem[0]);
        assertFalse(d1.equals(d2));
        d2 = new DefaultOHLCDataset("Series 2", new OHLCDataItem[0]);
        assertTrue(d1.equals(d2));

        d1 = new DefaultOHLCDataset("Series 2", new OHLCDataItem[] {
                new OHLCDataItem(new Date(123L), 1.2, 3.4, 5.6, 7.8, 99.9)});
        assertFalse(d1.equals(d2));
        d2 = new DefaultOHLCDataset("Series 2", new OHLCDataItem[] {
                new OHLCDataItem(new Date(123L), 1.2, 3.4, 5.6, 7.8, 99.9)});
        assertTrue(d1.equals(d2));

    }

// org.jfree.data.xy.junit.DefaultOHLCDatasetTests::testCloning
    public void testCloning() {
        DefaultOHLCDataset d1 = new DefaultOHLCDataset("Series 1",
                new OHLCDataItem[0]);
        DefaultOHLCDataset d2 = null;
        try {
            d2 = (DefaultOHLCDataset) d1.clone();
        }
        catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        assertTrue(d1 != d2);
        assertTrue(d1.getClass() == d2.getClass());
        assertTrue(d1.equals(d2));
    }

// org.jfree.data.xy.junit.DefaultOHLCDatasetTests::testCloning2
    public void testCloning2() {
        OHLCDataItem item1 = new OHLCDataItem(new Date(1L), 1.0, 2.0, 3.0, 4.0,
                5.0);
        OHLCDataItem item2 = new OHLCDataItem(new Date(2L), 6.0, 7.0, 8.0, 9.0,
                10.0);
        
        OHLCDataItem[] items = new OHLCDataItem[] {item2, item1};
        DefaultOHLCDataset d1 = new DefaultOHLCDataset("Series 1", items);
        DefaultOHLCDataset d2 = null;
        try {
            d2 = (DefaultOHLCDataset) d1.clone();
        }
        catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        assertTrue(d1 != d2);
        assertTrue(d1.getClass() == d2.getClass());
        assertTrue(d1.equals(d2));

        d1.sortDataByDate();
        assertFalse(d1.equals(d2));
    }

// org.jfree.data.xy.junit.DefaultOHLCDatasetTests::testPublicCloneable
    public void testPublicCloneable() {
        DefaultOHLCDataset d1 = new DefaultOHLCDataset("Series 1",
                new OHLCDataItem[0]);
        assertTrue(d1 instanceof PublicCloneable);
    }

// org.jfree.data.xy.junit.DefaultOHLCDatasetTests::testSerialization
    public void testSerialization() {
        DefaultOHLCDataset d1 = new DefaultOHLCDataset("Series 1",
                new OHLCDataItem[0]);
        DefaultOHLCDataset d2 = null;

        try {
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            ObjectOutput out = new ObjectOutputStream(buffer);
            out.writeObject(d1);
            out.close();

            ObjectInput in = new ObjectInputStream(
                new ByteArrayInputStream(buffer.toByteArray())
            );
            d2 = (DefaultOHLCDataset) in.readObject();
            in.close();
        }
        catch (Exception e) {
            System.out.println(e.toString());
        }
        assertEquals(d1, d2);
    }

// org.jfree.data.xy.junit.XYSeriesCollectionTests::testConstructor
    public void testConstructor() {
        XYSeriesCollection xysc = new XYSeriesCollection();
        assertEquals(0, xysc.getSeriesCount());
        assertEquals(1.0, xysc.getIntervalWidth(), EPSILON);
        assertEquals(0.5, xysc.getIntervalPositionFactor(), EPSILON);
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

        c1.setIntervalWidth(5.0);
        assertFalse(c1.equals(c2));
        c2.setIntervalWidth(5.0);
        assertTrue(c1.equals(c2));

        c1.setIntervalPositionFactor(0.75);
        assertFalse(c1.equals(c2));
        c2.setIntervalPositionFactor(0.75);
        assertTrue(c1.equals(c2));

        c1.setAutoWidth(true);
        assertFalse(c1.equals(c2));
        c2.setAutoWidth(true);
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

// org.jfree.data.xy.junit.XYSeriesCollectionTests::testGetDomainBounds
    public void testGetDomainBounds() {
        XYSeriesCollection dataset = new XYSeriesCollection();
        Range r = dataset.getDomainBounds(false);
        assertNull(r);
        r = dataset.getDomainBounds(true);
        assertNull(r);

        XYSeries series = new XYSeries("S1");
        dataset.addSeries(series);
        r = dataset.getDomainBounds(false);
        assertNull(r);
        r = dataset.getDomainBounds(true);
        assertNull(r);

        series.add(1.0, 1.1);
        r = dataset.getDomainBounds(false);
        assertEquals(new Range(1.0, 1.0), r);
        r = dataset.getDomainBounds(true);
        assertEquals(new Range(0.5, 1.5), r);

        series.add(-1.0, -1.1);
        r = dataset.getDomainBounds(false);
        assertEquals(new Range(-1.0, 1.0), r);
        r = dataset.getDomainBounds(true);
        assertEquals(new Range(-1.5, 1.5), r);
}

// org.jfree.data.xy.junit.XYSeriesCollectionTests::testGetRangeBounds
    public void testGetRangeBounds() {
        XYSeriesCollection dataset = new XYSeriesCollection();
        Range r = dataset.getRangeBounds(false);
        assertNull(r);
        r = dataset.getRangeBounds(true);
        assertNull(r);

        XYSeries series = new XYSeries("S1");
        dataset.addSeries(series);
        r = dataset.getRangeBounds(false);
        assertNull(r);
        r = dataset.getRangeBounds(true);
        assertNull(r);

        series.add(1.0, 1.1);
        r = dataset.getRangeBounds(false);
        assertEquals(new Range(1.1, 1.1), r);
        r = dataset.getRangeBounds(true);
        assertEquals(new Range(1.1, 1.1), r);

        series.add(-1.0, -1.1);
        r = dataset.getRangeBounds(false);
        assertEquals(new Range(-1.1, 1.1), r);
        r = dataset.getRangeBounds(true);
        assertEquals(new Range(-1.1, 1.1), r);
    }

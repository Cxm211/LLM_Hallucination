// buggy code
    public static boolean equal(GeneralPath p1, GeneralPath p2) {
        if (p1 == null) {
            return (p2 == null);
        }
        if (p2 == null) {
            return false;
        }
        if (p1.getWindingRule() != p2.getWindingRule()) {
            return false;
        }
        PathIterator iterator1 = p1.getPathIterator(null);
        PathIterator iterator2 = p1.getPathIterator(null);
        double[] d1 = new double[6];
        double[] d2 = new double[6];
        boolean done = iterator1.isDone() && iterator2.isDone();
        while (!done) {
            if (iterator1.isDone() != iterator2.isDone()) {
                return false;
            }
            int seg1 = iterator1.currentSegment(d1);
            int seg2 = iterator2.currentSegment(d2);
            if (seg1 != seg2) {
                return false;
            }
            if (!Arrays.equals(d1, d2)) {
                return false;
            }
            iterator1.next();
            iterator2.next();
            done = iterator1.isDone() && iterator2.isDone();
        }
        return true;
    }

// relevant test
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

// org.jfree.chart.renderer.category.junit.ScatterRendererTests::testEquals
    public void testEquals() {
        
        ScatterRenderer r1 = new ScatterRenderer();
        ScatterRenderer r2 = new ScatterRenderer();
        assertEquals(r1, r2);
        
        r1.setSeriesShapesFilled(1, true);
        assertFalse(r1.equals(r2));
        r2.setSeriesShapesFilled(1, true);
        assertTrue(r1.equals(r2));
        
        r1.setBaseShapesFilled(false);
        assertFalse(r1.equals(r2));
        r2.setBaseShapesFilled(false);
        assertTrue(r1.equals(r2));
        
        r1.setUseFillPaint(true);
        assertFalse(r1.equals(r2));
        r2.setUseFillPaint(true);
        assertTrue(r1.equals(r2));
        
        r1.setDrawOutlines(true);
        assertFalse(r1.equals(r2));
        r2.setDrawOutlines(true);
        assertTrue(r1.equals(r2));
        
        r1.setUseOutlinePaint(true);
        assertFalse(r1.equals(r2));
        r2.setUseOutlinePaint(true);
        assertTrue(r1.equals(r2));
        
        r1.setUseSeriesOffset(false);
        assertFalse(r1.equals(r2));
        r2.setUseSeriesOffset(false);
        assertTrue(r1.equals(r2));
        
    }

// org.jfree.chart.renderer.category.junit.ScatterRendererTests::testHashcode
    public void testHashcode() {
        ScatterRenderer r1 = new ScatterRenderer();
        ScatterRenderer r2 = new ScatterRenderer();
        assertTrue(r1.equals(r2));
        int h1 = r1.hashCode();
        int h2 = r2.hashCode();
        assertEquals(h1, h2);
    }

// org.jfree.chart.renderer.category.junit.ScatterRendererTests::testCloning
    public void testCloning() {
        ScatterRenderer r1 = new ScatterRenderer();
        ScatterRenderer r2 = null;
        try {
            r2 = (ScatterRenderer) r1.clone();
        }
        catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        assertTrue(r1 != r2);
        assertTrue(r1.getClass() == r2.getClass());
        assertTrue(r1.equals(r2));
        
        assertTrue(checkIndependence(r1, r2));
        
    }

// org.jfree.chart.renderer.category.junit.ScatterRendererTests::testSerialization
    public void testSerialization() {

        ScatterRenderer r1 = new ScatterRenderer();
        ScatterRenderer r2 = null;

        try {
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            ObjectOutput out = new ObjectOutputStream(buffer);
            out.writeObject(r1);
            out.close();

            ObjectInput in = new ObjectInputStream(
                    new ByteArrayInputStream(buffer.toByteArray()));
            r2 = (ScatterRenderer) in.readObject();
            in.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        assertEquals(r1, r2);

    }

// org.jfree.chart.renderer.category.junit.StackedAreaRendererTests::testEquals
    public void testEquals() {
        StackedAreaRenderer r1 = new StackedAreaRenderer();
        StackedAreaRenderer r2 = new StackedAreaRenderer();
        assertEquals(r1, r2);
        
        r1.setRenderAsPercentages(true);
        assertFalse(r1.equals(r2));
        r2.setRenderAsPercentages(true);
        assertTrue(r1.equals(r2));
    }

// org.jfree.chart.renderer.category.junit.StackedAreaRendererTests::testHashcode
    public void testHashcode() {
        StackedAreaRenderer r1 = new StackedAreaRenderer();
        StackedAreaRenderer r2 = new StackedAreaRenderer();
        assertTrue(r1.equals(r2));
        int h1 = r1.hashCode();
        int h2 = r2.hashCode();
        assertEquals(h1, h2);
    }

// org.jfree.chart.renderer.category.junit.StackedAreaRendererTests::testCloning
    public void testCloning() {
        StackedAreaRenderer r1 = new StackedAreaRenderer();
        StackedAreaRenderer r2 = null;
        try {
            r2 = (StackedAreaRenderer) r1.clone();
        }
        catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        assertTrue(r1 != r2);
        assertTrue(r1.getClass() == r2.getClass());
        assertTrue(r1.equals(r2));
        
    }

// org.jfree.chart.renderer.category.junit.StackedAreaRendererTests::testSerialization
    public void testSerialization() {

        StackedAreaRenderer r1 = new StackedAreaRenderer();
        StackedAreaRenderer r2 = null;

        try {
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            ObjectOutput out = new ObjectOutputStream(buffer);
            out.writeObject(r1);
            out.close();

            ObjectInput in = new ObjectInputStream(
                    new ByteArrayInputStream(buffer.toByteArray()));
            r2 = (StackedAreaRenderer) in.readObject();
            in.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        assertEquals(r1, r2);

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
                new ByteArrayInputStream(buffer.toByteArray())
            );
            r2 = (StackedBarRenderer) in.readObject();
            in.close();
        }
        catch (Exception e) {
            System.out.println(e.toString());
        }
        assertEquals(r1, r2);

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

// org.jfree.chart.renderer.category.junit.WaterfallBarRendererTests::testEquals
    public void testEquals() {
        WaterfallBarRenderer r1 = new WaterfallBarRenderer();
        WaterfallBarRenderer r2 = new WaterfallBarRenderer();
        assertEquals(r1, r2);
                
        
        r1.setFirstBarPaint(Color.cyan);
        assertFalse(r1.equals(r2));
        r2.setFirstBarPaint(Color.cyan);
        assertTrue(r1.equals(r2));
        
        
        r1.setLastBarPaint(Color.cyan);
        assertFalse(r1.equals(r2));
        r2.setLastBarPaint(Color.cyan);
        assertTrue(r1.equals(r2));

        
        r1.setPositiveBarPaint(Color.cyan);
        assertFalse(r1.equals(r2));
        r2.setPositiveBarPaint(Color.cyan);
        assertTrue(r1.equals(r2));

        
        r1.setNegativeBarPaint(Color.cyan);
        assertFalse(r1.equals(r2));
        r2.setNegativeBarPaint(Color.cyan);
        assertTrue(r1.equals(r2));

    }

// org.jfree.chart.renderer.category.junit.WaterfallBarRendererTests::testHashcode
    public void testHashcode() {
        WaterfallBarRenderer r1 = new WaterfallBarRenderer();
        WaterfallBarRenderer r2 = new WaterfallBarRenderer();
        assertTrue(r1.equals(r2));
        int h1 = r1.hashCode();
        int h2 = r2.hashCode();
        assertEquals(h1, h2);
    }

// org.jfree.chart.renderer.category.junit.WaterfallBarRendererTests::testCloning
    public void testCloning() {
        WaterfallBarRenderer r1 = new WaterfallBarRenderer();
        WaterfallBarRenderer r2 = null;
        try {
            r2 = (WaterfallBarRenderer) r1.clone();
        }
        catch (CloneNotSupportedException e) {
            System.err.println("Failed to clone.");
        }
        assertTrue(r1 != r2);
        assertTrue(r1.getClass() == r2.getClass());
        assertTrue(r1.equals(r2));
        
        
        r1.setFirstBarPaint(Color.yellow);
        assertFalse(r1.equals(r2));
        r2.setFirstBarPaint(Color.yellow);
        assertTrue(r1.equals(r2));
        
    }

// org.jfree.chart.renderer.category.junit.WaterfallBarRendererTests::testSerialization
    public void testSerialization() {

        WaterfallBarRenderer r1 = new WaterfallBarRenderer();
        WaterfallBarRenderer r2 = null;

        try {
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            ObjectOutput out = new ObjectOutputStream(buffer);
            out.writeObject(r1);
            out.close();

            ObjectInput in = new ObjectInputStream(
                new ByteArrayInputStream(buffer.toByteArray())
            );
            r2 = (WaterfallBarRenderer) in.readObject();
            in.close();
        }
        catch (Exception e) {
            System.out.println(e.toString());
        }
        assertEquals(r1, r2);

    }

// org.jfree.chart.renderer.junit.AbstractRendererTests::testEquals
    public void testEquals() {
        
        
        BarRenderer r1 = new BarRenderer();
        BarRenderer r2 = new BarRenderer();
        assertTrue(r1.equals(r2));
        assertTrue(r2.equals(r1));
        
        
        r1.setSeriesVisible(2, Boolean.TRUE);
        assertFalse(r1.equals(r2));
        r2.setSeriesVisible(2, Boolean.TRUE);
        assertTrue(r1.equals(r2));
        
        
        r1.setBaseSeriesVisible(false);
        assertFalse(r1.equals(r2));
        r2.setBaseSeriesVisible(false);
        assertTrue(r1.equals(r2));
        
        
        r1.setSeriesVisibleInLegend(1, Boolean.TRUE);
        assertFalse(r1.equals(r2));
        r2.setSeriesVisibleInLegend(1, Boolean.TRUE);
        assertTrue(r1.equals(r2));
        
        
        r1.setBaseSeriesVisibleInLegend(false);
        assertFalse(r1.equals(r2));
        r2.setBaseSeriesVisibleInLegend(false);
        assertTrue(r1.equals(r2));
        
        
        r1.setSeriesPaint(0, new GradientPaint(1.0f, 2.0f, Color.red, 
                3.0f, 4.0f, Color.white));
        assertFalse(r1.equals(r2));
        r2.setSeriesPaint(0, new GradientPaint(1.0f, 2.0f, Color.red, 
                3.0f, 4.0f, Color.white));
        assertTrue(r1.equals(r2));
        
        
        r1.setBasePaint(new GradientPaint(1.0f, 2.0f, Color.blue, 
                3.0f, 4.0f, Color.red));
        assertFalse(r1.equals(r2));
        r2.setBasePaint(new GradientPaint(1.0f, 2.0f, Color.blue, 
                3.0f, 4.0f, Color.red));
        assertTrue(r1.equals(r2));
        
        
        r1.setSeriesFillPaint(0, new GradientPaint(1.0f, 2.0f, Color.blue, 
                3.0f, 4.0f, Color.red));
        assertFalse(r1.equals(r2));
        r2.setSeriesFillPaint(0, new GradientPaint(1.0f, 2.0f, Color.blue, 
                3.0f, 4.0f, Color.red));
        assertTrue(r1.equals(r2));
        
        
        r1.setBaseFillPaint(new GradientPaint(1.0f, 2.0f, Color.blue, 
                3.0f, 4.0f, Color.red));
        assertFalse(r1.equals(r2));
        r2.setBaseFillPaint(new GradientPaint(1.0f, 2.0f, Color.blue, 
                3.0f, 4.0f, Color.red));
        assertTrue(r1.equals(r2));
        
        
        r1.setSeriesOutlinePaint(0, new GradientPaint(1.0f, 2.0f, Color.blue, 
                3.0f, 4.0f, Color.red));
        assertFalse(r1.equals(r2));
        r2.setSeriesOutlinePaint(0, new GradientPaint(1.0f, 2.0f, Color.blue, 
                3.0f, 4.0f, Color.red));
        assertTrue(r1.equals(r2));
        
        
        r1.setBaseOutlinePaint(new GradientPaint(1.0f, 2.0f, Color.blue, 
                3.0f, 4.0f, Color.red));
        assertFalse(r1.equals(r2));
        r2.setBaseOutlinePaint(new GradientPaint(1.0f, 2.0f, Color.blue, 
                3.0f, 4.0f, Color.red));
        assertTrue(r1.equals(r2));
        
        
        Stroke s = new BasicStroke(3.21f);
        r1.setSeriesStroke(1, s);
        assertFalse(r1.equals(r2));
        r2.setSeriesStroke(1, s);
        assertTrue(r1.equals(r2));

        
        r1.setBaseStroke(s);
        assertFalse(r1.equals(r2));
        r2.setBaseStroke(s);
        assertTrue(r1.equals(r2));
        
        
        r1.setSeriesOutlineStroke(0, s);
        assertFalse(r1.equals(r2));
        r2.setSeriesOutlineStroke(0, s);
        assertTrue(r1.equals(r2));

        
        r1.setBaseOutlineStroke(s);
        assertFalse(r1.equals(r2));
        r2.setBaseOutlineStroke(s);
        assertTrue(r1.equals(r2));
        
        
        r1.setSeriesShape(1, new Rectangle(1, 2, 3, 4));
        assertFalse(r1.equals(r2));
        r2.setSeriesShape(1, new Rectangle(1, 2, 3, 4));
        assertTrue(r1.equals(r2));

        
        r1.setBaseShape(new Rectangle(1, 2, 3, 4));
        assertFalse(r1.equals(r2));
        r2.setBaseShape(new Rectangle(1, 2, 3, 4));
        assertTrue(r1.equals(r2));
        
        
        r1.setSeriesItemLabelsVisible(1, Boolean.TRUE);
        assertFalse(r1.equals(r2));
        r2.setSeriesItemLabelsVisible(1, Boolean.TRUE);
        assertTrue(r1.equals(r2));
        
        
        r1.setBaseItemLabelsVisible(true);
        assertFalse(r1.equals(r2));
        r2.setBaseItemLabelsVisible(true);
        assertTrue(r1.equals(r2));
        
        
        r1.setSeriesItemLabelFont(1, new Font("Serif", Font.BOLD, 9));
        assertFalse(r1.equals(r2));
        r2.setSeriesItemLabelFont(1, new Font("Serif", Font.BOLD, 9));
        assertTrue(r1.equals(r2));
        
        
        r1.setBaseItemLabelFont(new Font("Serif", Font.PLAIN, 10));
        assertFalse(r1.equals(r2));
        r2.setBaseItemLabelFont(new Font("Serif", Font.PLAIN, 10));
        assertTrue(r1.equals(r2));
        
        
        r1.setSeriesItemLabelPaint(0, new GradientPaint(1.0f, 2.0f, Color.red, 
                3.0f, 4.0f, Color.gray));
        assertFalse(r1.equals(r2));
        r2.setSeriesItemLabelPaint(0, new GradientPaint(1.0f, 2.0f, Color.red, 
                3.0f, 4.0f, Color.gray));
        assertTrue(r1.equals(r2));

        
        r1.setBaseItemLabelPaint(new GradientPaint(1.0f, 2.0f, Color.red, 
                3.0f, 4.0f, Color.gray));
        assertFalse(r1.equals(r2));
        r2.setBaseItemLabelPaint(new GradientPaint(1.0f, 2.0f, Color.red, 
                3.0f, 4.0f, Color.gray));
        assertTrue(r1.equals(r2));
        
        
        r1.setSeriesPositiveItemLabelPosition(0, new ItemLabelPosition());
        assertFalse(r1.equals(r2));
        r2.setSeriesPositiveItemLabelPosition(0, new ItemLabelPosition());
        assertTrue(r1.equals(r2));

        
        r1.setBasePositiveItemLabelPosition(new ItemLabelPosition(
                ItemLabelAnchor.INSIDE10, TextAnchor.BASELINE_RIGHT));
        assertFalse(r1.equals(r2));
        r2.setBasePositiveItemLabelPosition(new ItemLabelPosition(
                ItemLabelAnchor.INSIDE10, TextAnchor.BASELINE_RIGHT));
        assertTrue(r1.equals(r2));
        
        
        r1.setSeriesNegativeItemLabelPosition(1, new ItemLabelPosition(
                ItemLabelAnchor.INSIDE10, TextAnchor.BASELINE_RIGHT));
        assertFalse(r1.equals(r2));
        r2.setSeriesNegativeItemLabelPosition(1, new ItemLabelPosition(
                ItemLabelAnchor.INSIDE10, TextAnchor.BASELINE_RIGHT));
        assertTrue(r1.equals(r2));

        
        r1.setBaseNegativeItemLabelPosition(new ItemLabelPosition(
                ItemLabelAnchor.INSIDE10, TextAnchor.BASELINE_RIGHT));
        assertFalse(r1.equals(r2));
        r2.setBaseNegativeItemLabelPosition(new ItemLabelPosition(
                ItemLabelAnchor.INSIDE10, TextAnchor.BASELINE_RIGHT));
        assertTrue(r1.equals(r2));

        
        r1.setItemLabelAnchorOffset(3.0);
        assertFalse(r1.equals(r2));
        r2.setItemLabelAnchorOffset(3.0);
        assertTrue(r1.equals(r2));

        
        r1.setSeriesCreateEntities(0, Boolean.TRUE);
        assertFalse(r1.equals(r2));
        r2.setSeriesCreateEntities(0, Boolean.TRUE);
        assertTrue(r1.equals(r2));
        
        
        r1.setBaseCreateEntities(false);
        assertFalse(r1.equals(r2));
        r2.setBaseCreateEntities(false);
        assertTrue(r1.equals(r2));

    }

// org.jfree.chart.renderer.junit.AbstractRendererTests::testCloning
    public void testCloning() {
        LineAndShapeRenderer r1 = new LineAndShapeRenderer();
        Rectangle2D baseShape = new Rectangle2D.Double(11.0, 12.0, 13.0, 14.0);
        r1.setBaseShape(baseShape);
        
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
        
        r1.setSeriesVisible(0, Boolean.FALSE);
        assertFalse(r1.equals(r2));
        r2.setSeriesVisible(0, Boolean.FALSE);
        assertTrue(r1.equals(r2));
        
        r1.setSeriesVisibleInLegend(0, Boolean.FALSE);
        assertFalse(r1.equals(r2));
        r2.setSeriesVisibleInLegend(0, Boolean.FALSE);
        assertTrue(r1.equals(r2));
        
        r1.setSeriesPaint(0, Color.black);
        assertFalse(r1.equals(r2));
        r2.setSeriesPaint(0, Color.black);
        assertTrue(r1.equals(r2));
        
        r1.setSeriesFillPaint(0, Color.yellow);
        assertFalse(r1.equals(r2));
        r2.setSeriesFillPaint(0, Color.yellow);
        assertTrue(r1.equals(r2));

        r1.setSeriesOutlinePaint(0, Color.yellow);
        assertFalse(r1.equals(r2));
        r2.setSeriesOutlinePaint(0, Color.yellow);
        assertTrue(r1.equals(r2));
        
        r1.setSeriesStroke(0, new BasicStroke(2.2f));
        assertFalse(r1.equals(r2));
        r2.setSeriesStroke(0, new BasicStroke(2.2f));
        assertTrue(r1.equals(r2));
    
        r1.setSeriesOutlineStroke(0, new BasicStroke(2.2f));
        assertFalse(r1.equals(r2));
        r2.setSeriesOutlineStroke(0, new BasicStroke(2.2f));
        assertTrue(r1.equals(r2));
        
        baseShape.setRect(4.0, 3.0, 2.0, 1.0);
        assertFalse(r1.equals(r2));
        r2.setBaseShape(new Rectangle2D.Double(4.0, 3.0, 2.0, 1.0));
        assertTrue(r1.equals(r2));
        
        r1.setSeriesShape(0, new Rectangle2D.Double(1.0, 2.0, 3.0, 4.0));
        assertFalse(r1.equals(r2));
        r2.setSeriesShape(0, new Rectangle2D.Double(1.0, 2.0, 3.0, 4.0));
        assertTrue(r1.equals(r2));
        
        r1.setSeriesItemLabelsVisible(0, Boolean.TRUE);
        assertFalse(r1.equals(r2));
        r2.setSeriesItemLabelsVisible(0, Boolean.TRUE);
        assertTrue(r1.equals(r2));
        
        r1.setSeriesItemLabelPaint(0, Color.red);
        assertFalse(r1.equals(r2));
        r2.setSeriesItemLabelPaint(0, Color.red);
        assertTrue(r1.equals(r2));
        
        r1.setSeriesPositiveItemLabelPosition(0, new ItemLabelPosition());
        assertFalse(r1.equals(r2));
        r2.setSeriesPositiveItemLabelPosition(0, new ItemLabelPosition());
        assertTrue(r1.equals(r2));

        r1.setSeriesNegativeItemLabelPosition(0, new ItemLabelPosition());
        assertFalse(r1.equals(r2));
        r2.setSeriesNegativeItemLabelPosition(0, new ItemLabelPosition());
        assertTrue(r1.equals(r2));
        
        r1.setSeriesCreateEntities(0, Boolean.FALSE);
        assertFalse(r1.equals(r2));
        r2.setSeriesCreateEntities(0, Boolean.FALSE);
        assertTrue(r1.equals(r2));
    }

// org.jfree.chart.renderer.junit.AbstractRendererTests::testCloning2
    public void testCloning2() {
        LineAndShapeRenderer r1 = new LineAndShapeRenderer();
        r1.setBasePaint(Color.blue);
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
        
        MyRendererChangeListener listener = new MyRendererChangeListener();
        r2.addChangeListener(listener);
        r2.setBasePaint(Color.red);
        assertTrue(listener.lastEvent.getRenderer() == r2);
        assertFalse(r1.hasListener(listener));
    }

// org.jfree.chart.renderer.junit.AbstractRendererTests::testEventNotification
    public void testEventNotification() {
        
        RendererChangeDetector detector = new RendererChangeDetector();
        BarRenderer r1 = new BarRenderer();  
                                             
        r1.addChangeListener(detector);
        
        
        detector.setNotified(false);
        r1.setSeriesPaint(0, Color.red);
        assertTrue(detector.getNotified());

        detector.setNotified(false);
        r1.setBasePaint(Color.red);
        assertTrue(detector.getNotified());

        
        detector.setNotified(false);
        r1.setSeriesOutlinePaint(0, Color.red);
        assertTrue(detector.getNotified());

        detector.setNotified(false);
        r1.setBaseOutlinePaint(Color.red);
        assertTrue(detector.getNotified());
        
        
        detector.setNotified(false);
        r1.setSeriesStroke(0, new BasicStroke(1.0f));
        assertTrue(detector.getNotified());

        detector.setNotified(false);
        r1.setBaseStroke(new BasicStroke(1.0f));
        assertTrue(detector.getNotified());

        
        detector.setNotified(false);
        r1.setSeriesOutlineStroke(0, new BasicStroke(1.0f));
        assertTrue(detector.getNotified());

        detector.setNotified(false);
        r1.setBaseOutlineStroke(new BasicStroke(1.0f));
        assertTrue(detector.getNotified());

        
        detector.setNotified(false);
        r1.setSeriesShape(0, new Rectangle2D.Float());
        assertTrue(detector.getNotified());

        detector.setNotified(false);
        r1.setBaseShape(new Rectangle2D.Float());
        assertTrue(detector.getNotified());

        
        detector.setNotified(false);
        r1.setSeriesItemLabelsVisible(0, Boolean.TRUE);
        assertTrue(detector.getNotified());

        detector.setNotified(false);
        r1.setBaseItemLabelsVisible(true);
        assertTrue(detector.getNotified());
        
        
        detector.setNotified(false);
        r1.setSeriesItemLabelFont(0, new Font("Serif", Font.PLAIN, 12));
        assertTrue(detector.getNotified());

        detector.setNotified(false);
        r1.setBaseItemLabelFont(new Font("Serif", Font.PLAIN, 12));
        assertTrue(detector.getNotified());
        
        
        detector.setNotified(false);
        r1.setSeriesItemLabelPaint(0, Color.blue);
        assertTrue(detector.getNotified());

        detector.setNotified(false);
        r1.setBaseItemLabelPaint(Color.blue);
        assertTrue(detector.getNotified());
        
        
        detector.setNotified(false);
        r1.setSeriesPositiveItemLabelPosition(0, new ItemLabelPosition(
                ItemLabelAnchor.CENTER, TextAnchor.CENTER));
        assertTrue(detector.getNotified());

        detector.setNotified(false);
        r1.setBasePositiveItemLabelPosition(new ItemLabelPosition(
                ItemLabelAnchor.CENTER, TextAnchor.CENTER));
        assertTrue(detector.getNotified());

        
        detector.setNotified(false);
        r1.setSeriesNegativeItemLabelPosition(0, new ItemLabelPosition(
                ItemLabelAnchor.CENTER, TextAnchor.CENTER));
        assertTrue(detector.getNotified());

        detector.setNotified(false);
        r1.setBaseNegativeItemLabelPosition(new ItemLabelPosition(
                ItemLabelAnchor.CENTER, TextAnchor.CENTER));
        assertTrue(detector.getNotified());

    }

// org.jfree.chart.renderer.junit.AbstractRendererTests::testSerialization
    public void testSerialization() {

        BarRenderer r1 = new BarRenderer();  
        BarRenderer r2 = null;

        try {
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            ObjectOutput out = new ObjectOutputStream(buffer);
            out.writeObject(r1);
            out.close();

            ObjectInput in = new ObjectInputStream(
                    new ByteArrayInputStream(buffer.toByteArray()));
            r2 = (BarRenderer) in.readObject();
            in.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        assertEquals(r1, r2);
        try {
            r2.notifyListeners(new RendererChangeEvent(r2));
        }
        catch (NullPointerException e) {
            assertTrue(false);  
        }

    }

// org.jfree.chart.renderer.junit.AbstractRendererTests::testAutoPopulateFlagDefaults
    public void testAutoPopulateFlagDefaults() {
        BarRenderer r = new BarRenderer();
        assertEquals(true, r.getAutoPopulateSeriesPaint());
        assertEquals(false, r.getAutoPopulateSeriesFillPaint());
        assertEquals(false, r.getAutoPopulateSeriesOutlinePaint());
        assertEquals(false, r.getAutoPopulateSeriesStroke());
        assertEquals(false, r.getAutoPopulateSeriesOutlineStroke());
        assertEquals(true, r.getAutoPopulateSeriesShape());
    }

// org.jfree.chart.renderer.junit.AbstractRendererTests::testPaintLookup
    public void testPaintLookup() {
        BarRenderer r = new BarRenderer();
        assertEquals(Color.blue, r.getBasePaint());
        
        
        r.setAutoPopulateSeriesPaint(false);
        assertEquals(Color.blue, r.lookupSeriesPaint(0));
        assertNull(r.getSeriesPaint(0));
        
        
        r.setAutoPopulateSeriesPaint(true);
         new CategoryPlot(null, new CategoryAxis(
                "Category"), new NumberAxis("Value"), r);
        assertEquals(DefaultDrawingSupplier.DEFAULT_PAINT_SEQUENCE[0], 
                r.lookupSeriesPaint(0));
        assertNotNull(r.getSeriesPaint(0));
    }

// org.jfree.chart.renderer.junit.AbstractRendererTests::testFillPaintLookup
    public void testFillPaintLookup() {
        BarRenderer r = new BarRenderer();
        assertEquals(Color.white, r.getBaseFillPaint());
        
        
        r.setAutoPopulateSeriesFillPaint(false);
        assertEquals(Color.white, r.lookupSeriesFillPaint(0));
        assertNull(r.getSeriesFillPaint(0));
        
        
        r.setAutoPopulateSeriesFillPaint(true);
         new CategoryPlot(null, new CategoryAxis(
                "Category"), new NumberAxis("Value"), r);
        assertEquals(DefaultDrawingSupplier.DEFAULT_FILL_PAINT_SEQUENCE[0], 
                r.lookupSeriesFillPaint(0));
        assertNotNull(r.getSeriesFillPaint(0));
    }

// org.jfree.chart.renderer.junit.AbstractRendererTests::testOutlinePaintLookup
    public void testOutlinePaintLookup() {
        BarRenderer r = new BarRenderer();
        assertEquals(Color.gray, r.getBaseOutlinePaint());
        
        
        r.setAutoPopulateSeriesOutlinePaint(false);
        assertEquals(Color.gray, r.lookupSeriesOutlinePaint(0));
        assertNull(r.getSeriesOutlinePaint(0));
        
        
        r.setAutoPopulateSeriesOutlinePaint(true);
         new CategoryPlot(null, new CategoryAxis(
                "Category"), new NumberAxis("Value"), r);
        assertEquals(DefaultDrawingSupplier.DEFAULT_OUTLINE_PAINT_SEQUENCE[0], 
                r.lookupSeriesOutlinePaint(0));
        assertNotNull(r.getSeriesOutlinePaint(0));
    }

// org.jfree.chart.renderer.junit.AbstractRendererTests::testHashCode
    public void testHashCode() {}

// org.jfree.chart.renderer.junit.DefaultPolarItemRendererTests::testEquals
    public void testEquals() {
        DefaultPolarItemRenderer r1 = new DefaultPolarItemRenderer();
        DefaultPolarItemRenderer r2 = new DefaultPolarItemRenderer();
        assertEquals(r1, r2);
        
        r1.setSeriesFilled(1, true);
        assertFalse(r1.equals(r2));
        r2.setSeriesFilled(1, true);
        assertTrue(r1.equals(r2));
        
    }

// org.jfree.chart.renderer.junit.DefaultPolarItemRendererTests::testHashcode
    public void testHashcode() {
        DefaultPolarItemRenderer r1 = new DefaultPolarItemRenderer();
        DefaultPolarItemRenderer r2 = new DefaultPolarItemRenderer();
        assertTrue(r1.equals(r2));
        int h1 = r1.hashCode();
        int h2 = r2.hashCode();
        assertEquals(h1, h2);
    }

// org.jfree.chart.renderer.junit.DefaultPolarItemRendererTests::testCloning
    public void testCloning() {
        DefaultPolarItemRenderer r1 = new DefaultPolarItemRenderer();
        DefaultPolarItemRenderer r2 = null;
        try {
            r2 = (DefaultPolarItemRenderer) r1.clone();
        }
        catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        assertTrue(r1 != r2);
        assertTrue(r1.getClass() == r2.getClass());
        assertTrue(r1.equals(r2));
        
        r1.setSeriesFilled(1, true);
        assertFalse(r1.equals(r2));
        r2.setSeriesFilled(1, true);
        assertTrue(r1.equals(r2));
    }

// org.jfree.chart.renderer.junit.DefaultPolarItemRendererTests::testSerialization
    public void testSerialization() {
        DefaultPolarItemRenderer r1 = new DefaultPolarItemRenderer();
        DefaultPolarItemRenderer r2 = null;
        try {
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            ObjectOutput out = new ObjectOutputStream(buffer);
            out.writeObject(r1);
            out.close();

            ObjectInput in = new ObjectInputStream(
                    new ByteArrayInputStream(buffer.toByteArray()));
            r2 = (DefaultPolarItemRenderer) in.readObject();
            in.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        assertEquals(r1, r2);
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

// org.jfree.chart.renderer.xy.junit.DeviationRendererTests::testEquals
    public void testEquals() {
        
        
        DeviationRenderer r1 = new DeviationRenderer();
        DeviationRenderer r2 = new DeviationRenderer();
        assertTrue(r1.equals(r2));
        assertTrue(r2.equals(r1));
        
        r1.setAlpha(0.1f);
        assertFalse(r1.equals(r2));
        r2.setAlpha(0.1f);
        assertTrue(r1.equals(r2));

    }

// org.jfree.chart.renderer.xy.junit.DeviationRendererTests::testHashcode
    public void testHashcode() {
        DeviationRenderer r1 = new DeviationRenderer();
        DeviationRenderer r2 = new DeviationRenderer();
        assertTrue(r1.equals(r2));
        int h1 = r1.hashCode();
        int h2 = r2.hashCode();
        assertEquals(h1, h2);
    }

// org.jfree.chart.renderer.xy.junit.DeviationRendererTests::testCloning
    public void testCloning() {
        DeviationRenderer r1 = new DeviationRenderer();
        DeviationRenderer r2 = null;
        try {
            r2 = (DeviationRenderer) r1.clone();
        }
        catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        assertTrue(r1 != r2);
        assertTrue(r1.getClass() == r2.getClass());
        assertTrue(r1.equals(r2));
        
        
        
    }

// org.jfree.chart.renderer.xy.junit.DeviationRendererTests::testSerialization
    public void testSerialization() {

        DeviationRenderer r1 = new DeviationRenderer();
        DeviationRenderer r2 = null;

        try {
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            ObjectOutput out = new ObjectOutputStream(buffer);
            out.writeObject(r1);
            out.close();

            ObjectInput in = new ObjectInputStream(
                    new ByteArrayInputStream(buffer.toByteArray()));
            r2 = (DeviationRenderer) in.readObject();
            in.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        assertEquals(r1, r2);

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
                new ByteArrayInputStream(buffer.toByteArray())
            );
            r2 = (HighLowRenderer) in.readObject();
            in.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        assertEquals(r1, r2);

    }

// org.jfree.chart.renderer.xy.junit.HighLowRendererTests::testFindRangeBounds
    public void testFindRangeBounds() {}

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

// org.jfree.chart.renderer.xy.junit.VectorRendererTests::testEquals
    public void testEquals() {        
        
        VectorRenderer r1 = new VectorRenderer();
        VectorRenderer r2 = new VectorRenderer();
        assertTrue(r1.equals(r2));
        assertTrue(r2.equals(r1));
        
        
        r1.setSeriesFillPaint(0, Color.green);
        assertFalse(r1.equals(r2));
        r2.setSeriesFillPaint(0, Color.green);
        assertTrue(r1.equals(r2));   
    }

// org.jfree.chart.renderer.xy.junit.VectorRendererTests::testHashcode
    public void testHashcode() {
        VectorRenderer r1 = new VectorRenderer();
        VectorRenderer r2 = new VectorRenderer();
        assertTrue(r1.equals(r2));
        int h1 = r1.hashCode();
        int h2 = r2.hashCode();
        assertEquals(h1, h2);
    }

// org.jfree.chart.renderer.xy.junit.VectorRendererTests::testCloning
    public void testCloning() {
        VectorRenderer r1 = new VectorRenderer();
        VectorRenderer r2 = null;
        try {
            r2 = (VectorRenderer) r1.clone();
        }
        catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        assertTrue(r1 != r2);
        assertTrue(r1.getClass() == r2.getClass());
        assertTrue(r1.equals(r2));
    }

// org.jfree.chart.renderer.xy.junit.VectorRendererTests::testSerialization
    public void testSerialization() {
        VectorRenderer r1 = new VectorRenderer();
        VectorRenderer r2 = null;
        try {
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            ObjectOutput out = new ObjectOutputStream(buffer);
            out.writeObject(r1);
            out.close();

            ObjectInput in = new ObjectInputStream(
                    new ByteArrayInputStream(buffer.toByteArray()));
            r2 = (VectorRenderer) in.readObject();
            in.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        assertEquals(r1, r2);
    }

// org.jfree.chart.renderer.xy.junit.WindItemRendererTests::testEquals
    public void testEquals() {
        WindItemRenderer r1 = new WindItemRenderer();
        WindItemRenderer r2 = new WindItemRenderer();
        assertEquals(r1, r2);
    }

// org.jfree.chart.renderer.xy.junit.WindItemRendererTests::testHashcode
    public void testHashcode() {
        WindItemRenderer r1 = new WindItemRenderer();
        WindItemRenderer r2 = new WindItemRenderer();
        assertTrue(r1.equals(r2));
        int h1 = r1.hashCode();
        int h2 = r2.hashCode();
        assertEquals(h1, h2);
    }

// org.jfree.chart.renderer.xy.junit.WindItemRendererTests::testCloning
    public void testCloning() {
        WindItemRenderer r1 = new WindItemRenderer();
        WindItemRenderer r2 = null;
        try {
            r2 = (WindItemRenderer) r1.clone();
        }
        catch (CloneNotSupportedException e) {
            System.err.println("Failed to clone.");
        }
        assertTrue(r1 != r2);
        assertTrue(r1.getClass() == r2.getClass());
        assertTrue(r1.equals(r2));
    }

// org.jfree.chart.renderer.xy.junit.WindItemRendererTests::testSerialization
    public void testSerialization() {

        WindItemRenderer r1 = new WindItemRenderer();
        WindItemRenderer r2 = null;

        try {
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            ObjectOutput out = new ObjectOutputStream(buffer);
            out.writeObject(r1);
            out.close();

            ObjectInput in = new ObjectInputStream(
                new ByteArrayInputStream(buffer.toByteArray())
            );
            r2 = (WindItemRenderer) in.readObject();
            in.close();
        }
        catch (Exception e) {
            System.out.println(e.toString());
        }
        assertEquals(r1, r2);

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
                new ByteArrayInputStream(buffer.toByteArray())
            );
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
                new ByteArrayInputStream(buffer.toByteArray())
            );
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

// org.jfree.chart.renderer.xy.junit.XYBoxAndWhiskerRendererTests::testEquals
    public void testEquals() {
        
        XYBoxAndWhiskerRenderer r1 = new XYBoxAndWhiskerRenderer();
        XYBoxAndWhiskerRenderer r2 = new XYBoxAndWhiskerRenderer();
        assertEquals(r1, r2);
        
        r1.setArtifactPaint(new GradientPaint(1.0f, 2.0f, Color.green, 
                3.0f, 4.0f, Color.red));
        assertFalse(r1.equals(r2));
        r2.setArtifactPaint(new GradientPaint(1.0f, 2.0f, Color.green, 
                3.0f, 4.0f, Color.red));
        assertEquals(r1, r2);
        
        r1.setBoxWidth(0.55);
        assertFalse(r1.equals(r2));
        r2.setBoxWidth(0.55);
        assertEquals(r1, r2);
        
        r1.setFillBox(!r1.getFillBox());
        assertFalse(r1.equals(r2));
        r2.setFillBox(!r2.getFillBox());
        assertEquals(r1, r2);
        
    }

// org.jfree.chart.renderer.xy.junit.XYBoxAndWhiskerRendererTests::testHashcode
    public void testHashcode() {
        XYBoxAndWhiskerRenderer r1 = new XYBoxAndWhiskerRenderer();
        XYBoxAndWhiskerRenderer r2 = new XYBoxAndWhiskerRenderer();
        assertTrue(r1.equals(r2));
        int h1 = r1.hashCode();
        int h2 = r2.hashCode();
        assertEquals(h1, h2);
    }

// org.jfree.chart.renderer.xy.junit.XYBoxAndWhiskerRendererTests::testCloning
    public void testCloning() {
        XYBoxAndWhiskerRenderer r1 = new XYBoxAndWhiskerRenderer();
        XYBoxAndWhiskerRenderer r2 = null;
        try {
            r2 = (XYBoxAndWhiskerRenderer) r1.clone();
        }
        catch (CloneNotSupportedException e) {
            System.err.println("Failed to clone.");
        }
        assertTrue(r1 != r2);
        assertTrue(r1.getClass() == r2.getClass());
        assertTrue(r1.equals(r2));
    }

// org.jfree.chart.renderer.xy.junit.XYBoxAndWhiskerRendererTests::testSerialization
    public void testSerialization() {

        XYBoxAndWhiskerRenderer r1 = new XYBoxAndWhiskerRenderer();
        XYBoxAndWhiskerRenderer r2 = null;

        try {
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            ObjectOutput out = new ObjectOutputStream(buffer);
            out.writeObject(r1);
            out.close();

            ObjectInput in = new ObjectInputStream(
                new ByteArrayInputStream(buffer.toByteArray())
            );
            r2 = (XYBoxAndWhiskerRenderer) in.readObject();
            in.close();
        }
        catch (Exception e) {
            System.out.println(e.toString());
        }
        assertEquals(r1, r2);

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
        double[][] s1 = new double[][] { x, y, z };
        d1.addSeries("S1", s1);
        x = new double[] {2.1};
        y = new double[] {14.1};
        z = new double[] {2.4};
        double[][] s2 = new double[][] { x, y, z };
        d1.addSeries("S2", s2);
        
        DefaultXYZDataset d2 = new DefaultXYZDataset(); 
        x = new double[] {2.1};
        y = new double[] {14.1};
        z = new double[] {2.4};
        double[][] s3 = new double[][] { x, y, z };
        d2.addSeries("S3", s3);
        x = new double[] {2.1};
        y = new double[] {14.1};
        z = new double[] {2.4};
        double[][] s4 = new double[][] { x, y, z };
        d2.addSeries("S4", s4);
        x = new double[] {2.1};
        y = new double[] {14.1};
        z = new double[] {2.4};
        double[][] s5 = new double[][] { x, y, z };
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

// org.jfree.chart.renderer.xy.junit.XYErrorRendererTests::testEquals
    public void testEquals() {
        XYErrorRenderer r1 = new XYErrorRenderer();
        XYErrorRenderer r2 = new XYErrorRenderer();
        assertEquals(r1, r2);
        
        
        r1.setDrawXError(false);
        assertFalse(r1.equals(r2));
        r2.setDrawXError(false);
        assertTrue(r1.equals(r2));
        
        
        r1.setDrawYError(false);
        assertFalse(r1.equals(r2));
        r2.setDrawYError(false);
        assertTrue(r1.equals(r2));
        
        
        r1.setCapLength(9.0);
        assertFalse(r1.equals(r2));
        r2.setCapLength(9.0);
        assertTrue(r1.equals(r2));
        
        
        r1.setErrorPaint(new GradientPaint(1.0f, 2.0f, Color.red, 3.0f, 4.0f, 
                Color.green));
        assertFalse(r1.equals(r2));
        r2.setErrorPaint(new GradientPaint(1.0f, 2.0f, Color.red, 3.0f, 4.0f, 
                Color.green));
        assertTrue(r1.equals(r2));
    }

// org.jfree.chart.renderer.xy.junit.XYErrorRendererTests::testHashcode
    public void testHashcode() {
        XYErrorRenderer r1 = new XYErrorRenderer();
        XYErrorRenderer r2 = new XYErrorRenderer();
        assertTrue(r1.equals(r2));
        int h1 = r1.hashCode();
        int h2 = r2.hashCode();
        assertEquals(h1, h2);
    }

// org.jfree.chart.renderer.xy.junit.XYErrorRendererTests::testCloning
    public void testCloning() {
        XYErrorRenderer r1 = new XYErrorRenderer();
        r1.setErrorPaint(new GradientPaint(1.0f, 2.0f, Color.red, 3.0f, 4.0f, 
                Color.white));
        XYErrorRenderer r2 = null;
        try {
            r2 = (XYErrorRenderer) r1.clone();
        }
        catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        assertTrue(r1 != r2);
        assertTrue(r1.getClass() == r2.getClass());
        assertTrue(r1.equals(r2));
    }

// org.jfree.chart.renderer.xy.junit.XYErrorRendererTests::testSerialization
    public void testSerialization() {

        XYErrorRenderer r1 = new XYErrorRenderer();
        r1.setErrorPaint(new GradientPaint(1.0f, 2.0f, Color.red, 3.0f, 4.0f, 
                Color.white));
        XYErrorRenderer r2 = null;

        try {
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            ObjectOutput out = new ObjectOutputStream(buffer);
            out.writeObject(r1);
            out.close();

            ObjectInput in = new ObjectInputStream(
                    new ByteArrayInputStream(buffer.toByteArray()));
            r2 = (XYErrorRenderer) in.readObject();
            in.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        assertEquals(r1, r2);

    }

// org.jfree.chart.renderer.xy.junit.XYLine3DRendererTests::testEquals
    public void testEquals() {
        XYLine3DRenderer r1 = new XYLine3DRenderer();
        XYLine3DRenderer r2 = new XYLine3DRenderer();
        assertEquals(r1, r2);
        
        r1.setXOffset(11.1);
        assertFalse(r1.equals(r2));
        r2.setXOffset(11.1);
        assertTrue(r1.equals(r2));

        r1.setYOffset(11.1);
        assertFalse(r1.equals(r2));
        r2.setYOffset(11.1);
        assertTrue(r1.equals(r2));
        
        r1.setWallPaint(new GradientPaint(1.0f, 2.0f, Color.red, 3.0f, 
                4.0f, Color.blue));
        assertFalse(r1.equals(r2));
        r2.setWallPaint(new GradientPaint(1.0f, 2.0f, Color.red, 3.0f, 
                4.0f, Color.blue));
        assertTrue(r1.equals(r2));   
    }

// org.jfree.chart.renderer.xy.junit.XYLine3DRendererTests::testHashcode
    public void testHashcode() {
        XYLine3DRenderer r1 = new XYLine3DRenderer();
        XYLine3DRenderer r2 = new XYLine3DRenderer();
        assertTrue(r1.equals(r2));
        int h1 = r1.hashCode();
        int h2 = r2.hashCode();
        assertEquals(h1, h2);
    }

// org.jfree.chart.renderer.xy.junit.XYLine3DRendererTests::testCloning
    public void testCloning() {
        XYLine3DRenderer r1 = new XYLine3DRenderer();
        r1.setWallPaint(new GradientPaint(1.0f, 2.0f, Color.red, 3.0f, 4.0f,
                Color.blue));
        XYLine3DRenderer r2 = null;
        try {
            r2 = (XYLine3DRenderer) r1.clone();
        }
        catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        assertTrue(r1 != r2);
        assertTrue(r1.getClass() == r2.getClass());
        assertTrue(r1.equals(r2));
    }

// org.jfree.chart.renderer.xy.junit.XYLine3DRendererTests::testSerialization
    public void testSerialization() {

        XYLine3DRenderer r1 = new XYLine3DRenderer();
        r1.setWallPaint(new GradientPaint(1.0f, 2.0f, Color.red, 3.0f, 4.0f,
                Color.blue));
        XYLine3DRenderer r2 = null;

        try {
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            ObjectOutput out = new ObjectOutputStream(buffer);
            out.writeObject(r1);
            out.close();

            ObjectInput in = new ObjectInputStream(
                    new ByteArrayInputStream(buffer.toByteArray()));
            r2 = (XYLine3DRenderer) in.readObject();
            in.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        assertEquals(r1, r2);

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

// org.jfree.chart.renderer.xy.junit.XYSplineAndShapeRendererTests::testEquals
    public void testEquals() {
        
        XYSplineAndShapeRenderer r1 = new XYSplineAndShapeRenderer();
        XYSplineAndShapeRenderer r2 = new XYSplineAndShapeRenderer();
        assertEquals(r1, r2);
        assertEquals(r2, r1);
    
        r1.setPrecision(9);
        assertFalse(r1.equals(r2));
        r2.setPrecision(9);
        assertTrue(r1.equals(r2));
    }

// org.jfree.chart.renderer.xy.junit.XYSplineAndShapeRendererTests::testHashcode
    public void testHashcode() {
        XYSplineAndShapeRenderer r1 = new XYSplineAndShapeRenderer();
        XYSplineAndShapeRenderer r2 = new XYSplineAndShapeRenderer();
        assertTrue(r1.equals(r2));
        int h1 = r1.hashCode();
        int h2 = r2.hashCode();
        assertEquals(h1, h2);
    }

// org.jfree.chart.renderer.xy.junit.XYSplineAndShapeRendererTests::testCloning
    public void testCloning() {
        Rectangle2D legendShape = new Rectangle2D.Double(1.0, 2.0, 3.0, 4.0);
        XYSplineAndShapeRenderer r1 = new XYSplineAndShapeRenderer();
        r1.setLegendLine(legendShape);
        XYSplineAndShapeRenderer r2 = null;
        try {
            r2 = (XYSplineAndShapeRenderer) r1.clone();
        }
        catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        assertTrue(r1 != r2);
        assertTrue(r1.getClass() == r2.getClass());
        assertTrue(r1.equals(r2));
    }

// org.jfree.chart.renderer.xy.junit.XYSplineAndShapeRendererTests::testSerialization
    public void testSerialization() {

        XYSplineAndShapeRenderer r1 = new XYSplineAndShapeRenderer();
        XYSplineAndShapeRenderer r2 = null;
        try {
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            ObjectOutput out = new ObjectOutputStream(buffer);
            out.writeObject(r1);
            out.close();

            ObjectInput in = new ObjectInputStream(
                    new ByteArrayInputStream(buffer.toByteArray()));
            r2 = (XYSplineAndShapeRenderer) in.readObject();
            in.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        assertEquals(r1, r2);

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

// org.jfree.chart.title.junit.CompositeTitleTests::testEquals
    public void testEquals() {
        CompositeTitle t1 = new CompositeTitle(new BlockContainer());
        CompositeTitle t2 = new CompositeTitle(new BlockContainer());
        assertEquals(t1, t2);
        assertEquals(t2, t1);
        
        
        t1.setMargin(new RectangleInsets(1.0, 2.0, 3.0, 4.0));
        assertFalse(t1.equals(t2));
        t2.setMargin(new RectangleInsets(1.0, 2.0, 3.0, 4.0));
        assertTrue(t1.equals(t2));
        
        
        t1.setFrame(new BlockBorder(Color.red));
        assertFalse(t1.equals(t2));
        t2.setFrame(new BlockBorder(Color.red));
        assertTrue(t1.equals(t2));
       
        
        t1.setPadding(new RectangleInsets(1.0, 2.0, 3.0, 4.0));
        assertFalse(t1.equals(t2));
        t2.setPadding(new RectangleInsets(1.0, 2.0, 3.0, 4.0));
        assertTrue(t1.equals(t2));
        
        
        t1.getContainer().add(new TextTitle("T1"));
        assertFalse(t1.equals(t2));
        t2.getContainer().add(new TextTitle("T1"));
        assertTrue(t1.equals(t2));
        
    }

// org.jfree.chart.title.junit.CompositeTitleTests::testHashcode
    public void testHashcode() {
        CompositeTitle t1 = new CompositeTitle(new BlockContainer());
        t1.getContainer().add(new TextTitle("T1"));
        CompositeTitle t2 = new CompositeTitle(new BlockContainer());
        t2.getContainer().add(new TextTitle("T1"));
        assertTrue(t1.equals(t2));
        int h1 = t1.hashCode();
        int h2 = t2.hashCode();
        assertEquals(h1, h2);
    }

// org.jfree.chart.title.junit.CompositeTitleTests::testCloning
    public void testCloning() {
        CompositeTitle t1 = new CompositeTitle(new BlockContainer());
        t1.getContainer().add(new TextTitle("T1"));
        CompositeTitle t2 = null;
        try {
            t2 = (CompositeTitle) t1.clone();
        }
        catch (CloneNotSupportedException e) {
            fail(e.toString());
        }
        assertTrue(t1 != t2);
        assertTrue(t1.getClass() == t2.getClass());
        assertTrue(t1.equals(t2));
    }

// org.jfree.chart.title.junit.CompositeTitleTests::testSerialization
    public void testSerialization() {
        CompositeTitle t1 = new CompositeTitle(new BlockContainer());
        t1.getContainer().add(new TextTitle("T1"));
        CompositeTitle t2 = null;
        try {
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            ObjectOutput out = new ObjectOutputStream(buffer);
            out.writeObject(t1);
            out.close();

            ObjectInput in = new ObjectInputStream(
                new ByteArrayInputStream(buffer.toByteArray())
            );
            t2 = (CompositeTitle) in.readObject();
            in.close();
        }
        catch (Exception e) {
            System.out.println(e.toString());
        }
        assertEquals(t1, t2);
    }

// org.jfree.chart.title.junit.DateTitleTests::testEquals
    public void testEquals() {
        DateTitle t1 = new DateTitle();
        DateTitle t2 = new DateTitle();
        assertEquals(t1, t2);
        
        t1.setText("Test 1");
        assertFalse(t1.equals(t2));
        t2.setText("Test 1");
        assertTrue(t1.equals(t2));
        
        Font f = new Font("SansSerif", Font.PLAIN, 15);
        t1.setFont(f);
        assertFalse(t1.equals(t2));
        t2.setFont(f);
        assertTrue(t1.equals(t2));
        
        t1.setPaint(Color.blue);
        assertFalse(t1.equals(t2));
        t2.setPaint(Color.blue);
        assertTrue(t1.equals(t2));
        
        t1.setBackgroundPaint(Color.blue);
        assertFalse(t1.equals(t2));
        t2.setBackgroundPaint(Color.blue);
        assertTrue(t1.equals(t2));
        
    }

// org.jfree.chart.title.junit.DateTitleTests::testHashcode
    public void testHashcode() {
        DateTitle t1 = new DateTitle();
        DateTitle t2 = new DateTitle();
        assertTrue(t1.equals(t2));
        int h1 = t1.hashCode();
        int h2 = t2.hashCode();
        assertEquals(h1, h2);
    }

// org.jfree.chart.title.junit.DateTitleTests::testCloning
    public void testCloning() {
        DateTitle t1 = new DateTitle();
        DateTitle t2 = null;
        try {
            t2 = (DateTitle) t1.clone();
        }
        catch (CloneNotSupportedException e) {
            System.err.println("DateTitleTests.testCloning: failed to clone.");
        }
        assertTrue(t1 != t2);
        assertTrue(t1.getClass() == t2.getClass());
        assertTrue(t1.equals(t2));
    }

// org.jfree.chart.title.junit.DateTitleTests::testSerialization
    public void testSerialization() {

        DateTitle t1 = new DateTitle();
        DateTitle t2 = null;

        try {
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            ObjectOutput out = new ObjectOutputStream(buffer);
            out.writeObject(t1);
            out.close();

            ObjectInput in = new ObjectInputStream(
                new ByteArrayInputStream(buffer.toByteArray())
            );
            t2 = (DateTitle) in.readObject();
            in.close();
        }
        catch (Exception e) {
            System.out.println(e.toString());
        }
        assertEquals(t1, t2);

    }

// org.jfree.chart.title.junit.ImageTitleTests::testEquals
    public void testEquals() {
        ImageTitle t1 = new ImageTitle(JFreeChart.INFO.getLogo());
        ImageTitle t2 = new ImageTitle(JFreeChart.INFO.getLogo());
        assertEquals(t1, t2);        
    }

// org.jfree.chart.title.junit.ImageTitleTests::testHashcode
    public void testHashcode() {
        ImageTitle t1 = new ImageTitle(JFreeChart.INFO.getLogo());
        ImageTitle t2 = new ImageTitle(JFreeChart.INFO.getLogo());
        assertTrue(t1.equals(t2));
        int h1 = t1.hashCode();
        int h2 = t2.hashCode();
        assertEquals(h1, h2);
    }

// org.jfree.chart.title.junit.ImageTitleTests::testCloning
    public void testCloning() {
        ImageTitle t1 = new ImageTitle(JFreeChart.INFO.getLogo());
        ImageTitle t2 = null;
        try {
            t2 = (ImageTitle) t1.clone();
        }
        catch (CloneNotSupportedException e) {
            System.err.println("ImageTitleTests.testCloning: failed to clone.");
        }
        assertTrue(t1 != t2);
        assertTrue(t1.getClass() == t2.getClass());
        assertTrue(t1.equals(t2));
    }

// org.jfree.chart.title.junit.ImageTitleTests::testSerialization
    public void testSerialization() {

        

    }

// org.jfree.chart.title.junit.ImageTitleTests::testWidthAndHeight
    public void testWidthAndHeight() {
        ImageTitle t1 = new ImageTitle(JFreeChart.INFO.getLogo());
        assertEquals(100, t1.getWidth(), EPSILON);
        assertEquals(100, t1.getHeight(), EPSILON);
    }

// org.jfree.chart.title.junit.LegendGraphicTests::testEquals
    public void testEquals() {
        LegendGraphic g1 = new LegendGraphic(new Rectangle2D.Double(1.0, 2.0, 
                3.0, 4.0), Color.black);
        LegendGraphic g2 = new LegendGraphic(new Rectangle2D.Double(1.0, 2.0, 
                3.0, 4.0), Color.black);
        assertEquals(g1, g2);
        assertEquals(g2, g1);
        
        
        g1.setShapeVisible(!g1.isShapeVisible());
        assertFalse(g1.equals(g2));
        g2.setShapeVisible(!g2.isShapeVisible());
        assertTrue(g1.equals(g2));
        
        
        g1.setShape(new Rectangle2D.Double(4.0, 3.0, 2.0, 1.0));
        assertFalse(g1.equals(g2));
        g2.setShape(new Rectangle2D.Double(4.0, 3.0, 2.0, 1.0));
        assertTrue(g1.equals(g2));

        
        g1.setShapeFilled(!g1.isShapeFilled());
        assertFalse(g1.equals(g2));
        g2.setShapeFilled(!g2.isShapeFilled());
        assertTrue(g1.equals(g2));
        
        
        g1.setFillPaint(Color.green);
        assertFalse(g1.equals(g2));
        g2.setFillPaint(Color.green);
        assertTrue(g1.equals(g2));
        
        
        g1.setShapeOutlineVisible(!g1.isShapeOutlineVisible());
        assertFalse(g1.equals(g2));
        g2.setShapeOutlineVisible(!g2.isShapeOutlineVisible());
        assertTrue(g1.equals(g2));

        
        g1.setOutlinePaint(Color.green);
        assertFalse(g1.equals(g2));
        g2.setOutlinePaint(Color.green);
        assertTrue(g1.equals(g2));

        
        g1.setOutlineStroke(new BasicStroke(1.23f));
        assertFalse(g1.equals(g2));
        g2.setOutlineStroke(new BasicStroke(1.23f));
        assertTrue(g1.equals(g2));
        
        
        g1.setShapeAnchor(RectangleAnchor.BOTTOM_RIGHT);
        assertFalse(g1.equals(g2));
        g2.setShapeAnchor(RectangleAnchor.BOTTOM_RIGHT);
        assertTrue(g1.equals(g2));
        
        
        g1.setShapeLocation(RectangleAnchor.BOTTOM_RIGHT);
        assertFalse(g1.equals(g2));
        g2.setShapeLocation(RectangleAnchor.BOTTOM_RIGHT);
        assertTrue(g1.equals(g2));
        
        
        g1.setLineVisible(!g1.isLineVisible());
        assertFalse(g1.equals(g2));
        g2.setLineVisible(!g2.isLineVisible());
        assertTrue(g1.equals(g2));
        
        
        g1.setLine(new Line2D.Double(1.0, 2.0, 3.0, 4.0));
        assertFalse(g1.equals(g2));
        g2.setLine(new Line2D.Double(1.0, 2.0, 3.0, 4.0));
        assertTrue(g1.equals(g2));
        
        
        g1.setLinePaint(Color.green);
        assertFalse(g1.equals(g2));
        g2.setLinePaint(Color.green);
        assertTrue(g1.equals(g2));
        
        
        g1.setLineStroke(new BasicStroke(1.23f));
        assertFalse(g1.equals(g2));
        g2.setLineStroke(new BasicStroke(1.23f));
        assertTrue(g1.equals(g2));
        
        
        g1.setFillPaintTransformer(new StandardGradientPaintTransformer(
                GradientPaintTransformType.CENTER_HORIZONTAL));
        assertFalse(g1.equals(g2));
        g2.setFillPaintTransformer(new StandardGradientPaintTransformer(
                GradientPaintTransformType.CENTER_HORIZONTAL));
        assertTrue(g1.equals(g2));

    }

// org.jfree.chart.title.junit.LegendGraphicTests::testHashcode
    public void testHashcode() {
        LegendGraphic g1 = new LegendGraphic(new Rectangle2D.Double(1.0, 2.0, 
                3.0, 4.0), Color.black);
        LegendGraphic g2 = new LegendGraphic(new Rectangle2D.Double(1.0, 2.0, 
                3.0, 4.0), Color.black);
        assertTrue(g1.equals(g2));
        int h1 = g1.hashCode();
        int h2 = g2.hashCode();
        assertEquals(h1, h2);
    }

// org.jfree.chart.title.junit.LegendGraphicTests::testCloning
    public void testCloning() {
        Rectangle r = new Rectangle(1, 2, 3, 4);
        LegendGraphic g1 = new LegendGraphic(r, Color.black);
        LegendGraphic g2 = null;
        try {
            g2 = (LegendGraphic) g1.clone();
        }
        catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        assertTrue(g1 != g2);
        assertTrue(g1.getClass() == g2.getClass());
        assertTrue(g1.equals(g2));
        
        
        r.setBounds(4, 3, 2, 1);
        assertFalse(g1.equals(g2));
    }

// org.jfree.chart.title.junit.LegendGraphicTests::testCloning2
    public void testCloning2() {
        Rectangle r = new Rectangle(1, 2, 3, 4);
        LegendGraphic g1 = new LegendGraphic(r, Color.black);
        Line2D l = new Line2D.Double(1.0, 2.0, 3.0, 4.0);
        g1.setLine(l);
        LegendGraphic g2 = null;
        try {
            g2 = (LegendGraphic) g1.clone();
        }
        catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        assertTrue(g1 != g2);
        assertTrue(g1.getClass() == g2.getClass());
        assertTrue(g1.equals(g2));
        
        
        l.setLine(4.0, 3.0, 2.0, 1.0);
        assertFalse(g1.equals(g2));
       
    }

// org.jfree.chart.title.junit.LegendGraphicTests::testSerialization
    public void testSerialization() {

        Stroke s = new BasicStroke(1.23f);
        LegendGraphic g1 = new LegendGraphic(new Rectangle2D.Double(1.0, 2.0, 3.0, 4.0), Color.black);
        g1.setOutlineStroke(s);
        LegendGraphic g2 = null;

        try {
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            ObjectOutput out = new ObjectOutputStream(buffer);
            out.writeObject(g1);
            out.close();

            ObjectInput in = new ObjectInputStream(
                new ByteArrayInputStream(buffer.toByteArray())
            );
            g2 = (LegendGraphic) in.readObject();
            in.close();
        }
        catch (Exception e) {
            System.out.println(e.toString());
        }
        assertTrue(g1.equals(g2));

    }

// org.jfree.chart.title.junit.LegendTitleTests::testEquals
    public void testEquals() {
        XYPlot plot1 = new XYPlot();
        LegendTitle t1 = new LegendTitle(plot1);
        LegendTitle t2 = new LegendTitle(plot1);
        assertEquals(t1, t2);
        
        t1.setBackgroundPaint(
            new GradientPaint(1.0f, 2.0f, Color.red, 3.0f, 4.0f, Color.yellow)
        );
        assertFalse(t1.equals(t2));
        t2.setBackgroundPaint(
            new GradientPaint(1.0f, 2.0f, Color.red, 3.0f, 4.0f, Color.yellow)
        );
        assertTrue(t1.equals(t2));
        
        t1.setLegendItemGraphicEdge(RectangleEdge.BOTTOM);
        assertFalse(t1.equals(t2));
        t2.setLegendItemGraphicEdge(RectangleEdge.BOTTOM);
        assertTrue(t1.equals(t2));
        
        t1.setLegendItemGraphicAnchor(RectangleAnchor.BOTTOM_LEFT);
        assertFalse(t1.equals(t2));
        t2.setLegendItemGraphicAnchor(RectangleAnchor.BOTTOM_LEFT);
        assertTrue(t1.equals(t2));
        
        t1.setLegendItemGraphicLocation(RectangleAnchor.TOP_LEFT);
        assertFalse(t1.equals(t2));
        t2.setLegendItemGraphicLocation(RectangleAnchor.TOP_LEFT);
        assertTrue(t1.equals(t2));
        
        t1.setItemFont(new Font("Dialog", Font.PLAIN, 19));
        assertFalse(t1.equals(t2));
        t2.setItemFont(new Font("Dialog", Font.PLAIN, 19));
        assertTrue(t1.equals(t2));
    }

// org.jfree.chart.title.junit.LegendTitleTests::testHashcode
    public void testHashcode() {
        XYPlot plot1 = new XYPlot();
        LegendTitle t1 = new LegendTitle(plot1);
        LegendTitle t2 = new LegendTitle(plot1);
        assertTrue(t1.equals(t2));
        int h1 = t1.hashCode();
        int h2 = t2.hashCode();
        assertEquals(h1, h2);
    }

// org.jfree.chart.title.junit.LegendTitleTests::testCloning
    public void testCloning() {
        XYPlot plot = new XYPlot();
        Rectangle2D bounds1 = new Rectangle2D.Double(10.0, 20.0, 30.0, 40.0);
        LegendTitle t1 = new LegendTitle(plot);
        t1.setBackgroundPaint(new GradientPaint(1.0f, 2.0f, Color.red, 3.0f, 
                4.0f, Color.yellow));
        t1.setBounds(bounds1);
        LegendTitle t2 = null;
        try {
            t2 = (LegendTitle) t1.clone();
        }
        catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        assertTrue(t1 != t2);
        assertTrue(t1.getClass() == t2.getClass());
        assertTrue(t1.equals(t2));
        
        
        bounds1.setFrame(40.0, 30.0, 20.0, 10.0);
        assertFalse(t1.equals(t2));
        t2.setBounds(new Rectangle2D.Double(40.0, 30.0, 20.0, 10.0));
        assertTrue(t1.equals(t2));
    }

// org.jfree.chart.title.junit.LegendTitleTests::testSerialization
    public void testSerialization() {

        XYPlot plot = new XYPlot();
        LegendTitle t1 = new LegendTitle(plot);
        LegendTitle t2 = null;

        try {
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            ObjectOutput out = new ObjectOutputStream(buffer);
            out.writeObject(t1);
            out.close();

            ObjectInput in = new ObjectInputStream(
                new ByteArrayInputStream(buffer.toByteArray())
            );
            t2 = (LegendTitle) in.readObject();
            in.close();
        }
        catch (Exception e) {
            fail(e.toString());
        }
        assertTrue(t1.equals(t2));
        assertTrue(t2.getSources()[0].equals(plot));
    }

// org.jfree.chart.title.junit.PaintScaleLegendTests::testEquals
    public void testEquals() {
        
        
        PaintScaleLegend l1 = new PaintScaleLegend(new GrayPaintScale(), 
                new NumberAxis("X"));
        PaintScaleLegend l2 = new PaintScaleLegend(new GrayPaintScale(), 
                new NumberAxis("X"));
        assertTrue(l1.equals(l2));
        assertTrue(l2.equals(l1));
        
        
        l1.setScale(new LookupPaintScale());
        assertFalse(l1.equals(l2));
        l2.setScale(new LookupPaintScale());
        assertTrue(l1.equals(l2));
        
        
        l1.setAxis(new NumberAxis("Axis 2"));
        assertFalse(l1.equals(l2));
        l2.setAxis(new NumberAxis("Axis 2"));
        assertTrue(l1.equals(l2));
        
        
        l1.setAxisLocation(AxisLocation.BOTTOM_OR_RIGHT);
        assertFalse(l1.equals(l2));
        l2.setAxisLocation(AxisLocation.BOTTOM_OR_RIGHT);
        assertTrue(l1.equals(l2));
        
        
        l1.setAxisOffset(99.0);
        assertFalse(l1.equals(l2));
        l2.setAxisOffset(99.0);
        assertTrue(l1.equals(l2));
        
        
        l1.setStripWidth(99.0);
        assertFalse(l1.equals(l2));
        l2.setStripWidth(99.0);
        assertTrue(l1.equals(l2));
        
        
        l1.setStripOutlineVisible(!l1.isStripOutlineVisible());
        assertFalse(l1.equals(l2));
        l2.setStripOutlineVisible(l1.isStripOutlineVisible());
        assertTrue(l1.equals(l2));
        
        
        l1.setStripOutlinePaint(new GradientPaint(1.0f, 2.0f, Color.red,
                3.0f, 4.0f, Color.blue));
        assertFalse(l1.equals(l2));
        l2.setStripOutlinePaint(new GradientPaint(1.0f, 2.0f, Color.red,
                3.0f, 4.0f, Color.blue));
        assertTrue(l1.equals(l2));
        
        
        l1.setStripOutlineStroke(new BasicStroke(1.1f));
        assertFalse(l1.equals(l2));
        l2.setStripOutlineStroke(new BasicStroke(1.1f));
        assertTrue(l1.equals(l2));
        
        
        l1.setBackgroundPaint(new GradientPaint(1.0f, 2.0f, Color.red,
                3.0f, 4.0f, Color.blue));
        assertFalse(l1.equals(l2));
        l2.setBackgroundPaint(new GradientPaint(1.0f, 2.0f, Color.red,
                3.0f, 4.0f, Color.blue));
        assertTrue(l1.equals(l2));
            
    }

// org.jfree.chart.title.junit.PaintScaleLegendTests::testHashcode
    public void testHashcode() {
        PaintScaleLegend l1 = new PaintScaleLegend(new GrayPaintScale(), 
                new NumberAxis("X"));
        PaintScaleLegend l2 = new PaintScaleLegend(new GrayPaintScale(), 
                new NumberAxis("X"));
        assertTrue(l1.equals(l2));
        int h1 = l1.hashCode();
        int h2 = l2.hashCode();
        assertEquals(h1, h2);
    }

// org.jfree.chart.title.junit.PaintScaleLegendTests::testCloning
    public void testCloning() {
        PaintScaleLegend l1 = new PaintScaleLegend(new GrayPaintScale(), 
                new NumberAxis("X"));
        PaintScaleLegend l2 = null;
        try {
            l2 = (PaintScaleLegend) l1.clone();
        }
        catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        assertTrue(l1 != l2);
        assertTrue(l1.getClass() == l2.getClass());
        assertTrue(l1.equals(l2));
    }

// org.jfree.chart.title.junit.PaintScaleLegendTests::testSerialization
    public void testSerialization() {
        PaintScaleLegend l1 = new PaintScaleLegend(new GrayPaintScale(), 
                new NumberAxis("X"));
        PaintScaleLegend l2 = null;
        try {
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            ObjectOutput out = new ObjectOutputStream(buffer);
            out.writeObject(l1);
            out.close();

            ObjectInput in = new ObjectInputStream(
                    new ByteArrayInputStream(buffer.toByteArray()));
            l2 = (PaintScaleLegend) in.readObject();
            in.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        assertEquals(l1, l2);
    }

// org.jfree.chart.title.junit.TextTitleTests::testEquals
    public void testEquals() {
        TextTitle t1 = new TextTitle();
        TextTitle t2 = new TextTitle();
        assertEquals(t1, t2);
        
        t1.setText("Test 1");
        assertFalse(t1.equals(t2));
        t2.setText("Test 1");
        assertTrue(t1.equals(t2));
        
        Font f = new Font("SansSerif", Font.PLAIN, 15);
        t1.setFont(f);
        assertFalse(t1.equals(t2));
        t2.setFont(f);
        assertTrue(t1.equals(t2));
        
        t1.setTextAlignment(HorizontalAlignment.RIGHT);
        assertFalse(t1.equals(t2));
        t2.setTextAlignment(HorizontalAlignment.RIGHT);
        assertTrue(t1.equals(t2));
        
        
        t1.setPaint(new GradientPaint(1.0f, 2.0f, Color.red, 
                3.0f, 4.0f, Color.blue));
        assertFalse(t1.equals(t2));
        t2.setPaint(new GradientPaint(1.0f, 2.0f, Color.red, 
                3.0f, 4.0f, Color.blue));
        assertTrue(t1.equals(t2));
        
        
        t1.setBackgroundPaint(new GradientPaint(4.0f, 3.0f, Color.red, 
                2.0f, 1.0f, Color.blue));
        assertFalse(t1.equals(t2));
        t2.setBackgroundPaint(new GradientPaint(4.0f, 3.0f, Color.red, 
                2.0f, 1.0f, Color.blue));
        assertTrue(t1.equals(t2));
        
    }

// org.jfree.chart.title.junit.TextTitleTests::testHashcode
    public void testHashcode() {
        TextTitle t1 = new TextTitle();
        TextTitle t2 = new TextTitle();
        assertTrue(t1.equals(t2));
        int h1 = t1.hashCode();
        int h2 = t2.hashCode();
        assertEquals(h1, h2);
    }

// org.jfree.chart.title.junit.TextTitleTests::testCloning
    public void testCloning() {
        TextTitle t1 = new TextTitle();
        TextTitle t2 = null;
        try {
            t2 = (TextTitle) t1.clone();
        }
        catch (CloneNotSupportedException e) {
            System.err.println("TextTitleTests.testCloning: failed to clone.");
        }
        assertTrue(t1 != t2);
        assertTrue(t1.getClass() == t2.getClass());
        assertTrue(t1.equals(t2));
    }

// org.jfree.chart.title.junit.TextTitleTests::testSerialization
    public void testSerialization() {

        TextTitle t1 = new TextTitle("Test");
        TextTitle t2 = null;

        try {
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            ObjectOutput out = new ObjectOutputStream(buffer);
            out.writeObject(t1);
            out.close();

            ObjectInput in = new ObjectInputStream(
                new ByteArrayInputStream(buffer.toByteArray())
            );
            t2 = (TextTitle) in.readObject();
            in.close();
        }
        catch (Exception e) {
            System.out.println(e.toString());
        }
        assertEquals(t1, t2);

    }

// org.jfree.chart.util.junit.ShapeUtilitiesTests::testEqualLine2Ds
    public void testEqualLine2Ds() {

        assertTrue(ShapeUtilities.equal((Line2D) null, (Line2D) null));
        Line2D l1 = new Line2D.Float(1.0f, 2.0f, 3.0f, 4.0f);
        Line2D l2 = new Line2D.Float(1.0f, 2.0f, 3.0f, 4.0f);
        assertTrue(ShapeUtilities.equal(l1, l2));

        l1 = new Line2D.Float(4.0f, 3.0f, 2.0f, 1.0f);
        assertFalse(ShapeUtilities.equal(l1, l2));
        l2 = new Line2D.Float(4.0f, 3.0f, 2.0f, 1.0f);
        assertTrue(ShapeUtilities.equal(l1, l2));

        l1 = new Line2D.Double(4.0f, 3.0f, 2.0f, 1.0f);
        assertTrue(ShapeUtilities.equal(l1, l2));

    }

// org.jfree.chart.util.junit.ShapeUtilitiesTests::testEqualShapes
    public void testEqualShapes() {

        
        Shape s1 = null;
        Shape s2 = null;
        assertTrue(ShapeUtilities.equal(s1, s2));

        
        s1 = new Line2D.Double(1.0, 2.0, 3.0, 4.0);
        assertFalse(ShapeUtilities.equal(s1, s2));
        s2 = new Line2D.Double(1.0, 2.0, 3.0, 4.0);
        assertTrue(ShapeUtilities.equal(s1, s2));
        assertFalse(s1.equals(s2));

        
        s1 = new Rectangle2D.Double(1.0, 2.0, 3.0, 4.0);
        assertFalse(ShapeUtilities.equal(s1, s2));
        s2 = new Rectangle2D.Double(1.0, 2.0, 3.0, 4.0);
        assertTrue(ShapeUtilities.equal(s1, s2));
        assertTrue(s1.equals(s2));  

        
        s1 = new Ellipse2D.Double(1.0, 2.0, 3.0, 4.0);
        assertFalse(ShapeUtilities.equal(s1, s2));
        s2 = new Ellipse2D.Double(1.0, 2.0, 3.0, 4.0);
        assertTrue(ShapeUtilities.equal(s1, s2));

        
        s1 = new Arc2D.Double(1.0, 2.0, 3.0, 4.0, 5.0, 6.0, Arc2D.PIE);
        assertFalse(ShapeUtilities.equal(s1, s2));
        s2 = new Arc2D.Double(1.0, 2.0, 3.0, 4.0, 5.0, 6.0, Arc2D.PIE);
        assertTrue(ShapeUtilities.equal(s1, s2));

        
        Polygon p1 = new Polygon(new int[] {0, 1, 0}, new int[] {1, 0, 1}, 3);
        Polygon p2 = new Polygon(new int[] {1, 1, 0}, new int[] {1, 0, 1}, 3);
        s1 = p1;
        s2 = p2;
        assertFalse(ShapeUtilities.equal(s1, s2));
        p2 = new Polygon(new int[] {0, 1, 0}, new int[] {1, 0, 1}, 3);
        s2 = p2;
        assertTrue(ShapeUtilities.equal(s1, s2));

        
        GeneralPath g1 = new GeneralPath();
        g1.moveTo(1.0f, 2.0f);
        g1.lineTo(3.0f, 4.0f);
        g1.curveTo(5.0f, 6.0f, 7.0f, 8.0f, 9.0f, 10.0f);
        g1.quadTo(1.0f, 2.0f, 3.0f, 4.0f);
        g1.closePath();
        s1 = g1;
        assertFalse(ShapeUtilities.equal(s1, s2));
        GeneralPath g2 = new GeneralPath();
        g2.moveTo(1.0f, 2.0f);
        g2.lineTo(3.0f, 4.0f);
        g2.curveTo(5.0f, 6.0f, 7.0f, 8.0f, 9.0f, 10.0f);
        g2.quadTo(1.0f, 2.0f, 3.0f, 4.0f);
        g2.closePath();
        s2 = g2;
        assertTrue(ShapeUtilities.equal(s1, s2));
        assertFalse(s1.equals(s2));

    }

// org.jfree.chart.util.junit.ShapeUtilitiesTests::testIntersects
    public void testIntersects() {
        final Rectangle2D r1 = new Rectangle2D.Float(0, 0, 100, 100);
        final Rectangle2D r2 = new Rectangle2D.Float(0, 0, 100, 100);
        assertTrue(ShapeUtilities.intersects(r1, r2));

        r1.setRect(100, 0, 100, 0);
        assertTrue(ShapeUtilities.intersects(r1, r2));
        assertTrue(ShapeUtilities.intersects(r2, r1));

        r1.setRect(0, 0, 0, 0);
        assertTrue(ShapeUtilities.intersects(r1, r2));
        assertTrue(ShapeUtilities.intersects(r2, r1));

        r1.setRect(50, 50, 10, 0);
        assertTrue(ShapeUtilities.intersects(r1, r2));
        assertTrue(ShapeUtilities.intersects(r2, r1));
    }

// org.jfree.chart.util.junit.ShapeUtilitiesTests::testEqualGeneralPaths
    public void testEqualGeneralPaths() {
        GeneralPath g1 = new GeneralPath();
        g1.moveTo(1.0f, 2.0f);
        g1.lineTo(3.0f, 4.0f);
        g1.curveTo(5.0f, 6.0f, 7.0f, 8.0f, 9.0f, 10.0f);
        g1.quadTo(1.0f, 2.0f, 3.0f, 4.0f);
        g1.closePath();
        GeneralPath g2 = new GeneralPath();
        g2.moveTo(1.0f, 2.0f);
        g2.lineTo(3.0f, 4.0f);
        g2.curveTo(5.0f, 6.0f, 7.0f, 8.0f, 9.0f, 10.0f);
        g2.quadTo(1.0f, 2.0f, 3.0f, 4.0f);
        g2.closePath();
        assertTrue(ShapeUtilities.equal(g1, g2));

        g2 = new GeneralPath();
        g2.moveTo(11.0f, 22.0f);
        g2.lineTo(3.0f, 4.0f);
        g2.curveTo(5.0f, 6.0f, 7.0f, 8.0f, 9.0f, 10.0f);
        g2.quadTo(1.0f, 2.0f, 3.0f, 4.0f);
        g2.closePath();
        assertFalse(ShapeUtilities.equal(g1, g2));

        g2 = new GeneralPath();
        g2.moveTo(1.0f, 2.0f);
        g2.lineTo(33.0f, 44.0f);
        g2.curveTo(5.0f, 6.0f, 7.0f, 8.0f, 9.0f, 10.0f);
        g2.quadTo(1.0f, 2.0f, 3.0f, 4.0f);
        g2.closePath();
        assertFalse(ShapeUtilities.equal(g1, g2));

        g2 = new GeneralPath();
        g2.moveTo(1.0f, 2.0f);
        g2.lineTo(3.0f, 4.0f);
        g2.curveTo(55.0f, 66.0f, 77.0f, 88.0f, 99.0f, 100.0f);
        g2.quadTo(1.0f, 2.0f, 3.0f, 4.0f);
        g2.closePath();
        assertFalse(ShapeUtilities.equal(g1, g2));

        g2 = new GeneralPath();
        g2.moveTo(1.0f, 2.0f);
        g2.lineTo(3.0f, 4.0f);
        g2.curveTo(5.0f, 6.0f, 7.0f, 8.0f, 9.0f, 10.0f);
        g2.quadTo(11.0f, 22.0f, 33.0f, 44.0f);
        g2.closePath();
        assertFalse(ShapeUtilities.equal(g1, g2));

        g2 = new GeneralPath();
        g2.moveTo(1.0f, 2.0f);
        g2.lineTo(3.0f, 4.0f);
        g2.curveTo(5.0f, 6.0f, 7.0f, 8.0f, 9.0f, 10.0f);
        g2.quadTo(1.0f, 2.0f, 3.0f, 4.0f);
        g2.lineTo(3.0f, 4.0f);
        g2.closePath();
        assertFalse(ShapeUtilities.equal(g1, g2));
    }

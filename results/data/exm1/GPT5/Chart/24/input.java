// buggy code
    public Paint getPaint(double value) {
        double v = Math.max(value, this.lowerBound);
        v = Math.min(v, this.upperBound);
        int g = (int) ((value - this.lowerBound) / (this.upperBound 
                - this.lowerBound) * 255.0);
        return new Color(g, g, g);
    }

// relevant test
// org.jfree.chart.renderer.junit.GrayPaintScaleTests::testConstructor
    public void testConstructor() {
        GrayPaintScale gps = new GrayPaintScale();
        assertEquals(0.0, gps.getLowerBound(), EPSILON);
        assertEquals(1.0, gps.getUpperBound(), EPSILON);
    }

// org.jfree.chart.renderer.junit.GrayPaintScaleTests::testGetPaint
    public void testGetPaint() {
        GrayPaintScale gps = new GrayPaintScale();
        Color c = (Color) gps.getPaint(0.0);
        assertTrue(c.equals(Color.black));
        c = (Color) gps.getPaint(1.0);
        assertTrue(c.equals(Color.white));
        
        
        
        c = (Color) gps.getPaint(-0.5);
        assertTrue(c.equals(Color.black));
        c = (Color) gps.getPaint(1.5);
        assertTrue(c.equals(Color.white));
    }

// org.jfree.chart.renderer.junit.GrayPaintScaleTests::testEquals
    public void testEquals() {
        GrayPaintScale g1 = new GrayPaintScale();
        GrayPaintScale g2 = new GrayPaintScale();
        assertTrue(g1.equals(g2));
        assertTrue(g2.equals(g1));
        
        g1 = new GrayPaintScale(0.0, 1.0);
        g2 = new GrayPaintScale(0.0, 1.0);
        assertTrue(g1.equals(g2));
        g1 = new GrayPaintScale(0.1, 1.0);
        assertFalse(g1.equals(g2));
        g2 = new GrayPaintScale(0.1, 1.0);
        assertTrue(g1.equals(g2));
        
        g1 = new GrayPaintScale(0.1, 0.9);
        assertFalse(g1.equals(g2));
        g2 = new GrayPaintScale(0.1, 0.9);
        assertTrue(g1.equals(g2));
    }

// org.jfree.chart.renderer.junit.GrayPaintScaleTests::testCloning
    public void testCloning() {
        GrayPaintScale g1 = new GrayPaintScale();
        GrayPaintScale g2 = null;
        try {
            g2 = (GrayPaintScale) g1.clone();
        }
        catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        assertTrue(g1 != g2);
        assertTrue(g1.getClass() == g2.getClass());
        assertTrue(g1.equals(g2));
    }

// org.jfree.chart.renderer.junit.GrayPaintScaleTests::testSerialization
    public void testSerialization() {
        GrayPaintScale g1 = new GrayPaintScale();
        GrayPaintScale g2 = null;
        try {
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            ObjectOutput out = new ObjectOutputStream(buffer);
            out.writeObject(g1);
            out.close();

            ObjectInput in = new ObjectInputStream(
                    new ByteArrayInputStream(buffer.toByteArray()));
            g2 = (GrayPaintScale) in.readObject();
            in.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        assertEquals(g1, g2);       
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

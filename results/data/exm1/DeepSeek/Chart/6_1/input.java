// buggy code
    public boolean equals(Object obj) {

        if (obj == this) {
            return true;
        }
        if (!(obj instanceof ShapeList)) {
            return false;
        }
        return super.equals(obj);

    }

// relevant test
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

// org.jfree.chart.renderer.xy.junit.DeviationRendererTests::testPublicCloneable
    public void testPublicCloneable() {
        DeviationRenderer r1 = new DeviationRenderer();
        assertTrue(r1 instanceof PublicCloneable);
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

// org.jfree.chart.renderer.xy.junit.VectorRendererTests::testPublicCloneable
    public void testPublicCloneable() {
        VectorRenderer r1 = new VectorRenderer();
        assertTrue(r1 instanceof PublicCloneable);
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

// org.jfree.chart.renderer.xy.junit.WindItemRendererTests::testPublicCloneable
    public void testPublicCloneable() {
        WindItemRenderer r1 = new WindItemRenderer();
        assertTrue(r1 instanceof PublicCloneable);
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
                    new ByteArrayInputStream(buffer.toByteArray()));
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

        r1.setBoxPaint(Color.yellow);
        assertFalse(r1.equals(r2));
        r2.setBoxPaint(Color.yellow);
        assertEquals(r1, r2);

        
        r1.setBoxPaint(null);
        assertFalse(r1.equals(r2));
        r2.setBoxPaint(null);
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

// org.jfree.chart.renderer.xy.junit.XYBoxAndWhiskerRendererTests::testPublicCloneable
    public void testPublicCloneable() {
        XYBoxAndWhiskerRenderer r1 = new XYBoxAndWhiskerRenderer();
        assertTrue(r1 instanceof PublicCloneable);
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

// org.jfree.chart.renderer.xy.junit.XYErrorRendererTests::testPublicCloneable
    public void testPublicCloneable() {
        XYErrorRenderer r1 = new XYErrorRenderer();
        assertTrue(r1 instanceof PublicCloneable);
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

// org.jfree.chart.renderer.xy.junit.XYLine3DRendererTests::testPublicCloneable
    public void testPublicCloneable() {
        XYLine3DRenderer r1 = new XYLine3DRenderer();
        assertTrue(r1 instanceof PublicCloneable);
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

// org.jfree.chart.renderer.xy.junit.XYSplineRendererTests::testEquals
    public void testEquals() {

        XYSplineRenderer r1 = new XYSplineRenderer();
        XYSplineRenderer r2 = new XYSplineRenderer();
        assertEquals(r1, r2);
        assertEquals(r2, r1);

        r1.setPrecision(9);
        assertFalse(r1.equals(r2));
        r2.setPrecision(9);
        assertTrue(r1.equals(r2));
    }

// org.jfree.chart.renderer.xy.junit.XYSplineRendererTests::testHashcode
    public void testHashcode() {
        XYSplineRenderer r1 = new XYSplineRenderer();
        XYSplineRenderer r2 = new XYSplineRenderer();
        assertTrue(r1.equals(r2));
        int h1 = r1.hashCode();
        int h2 = r2.hashCode();
        assertEquals(h1, h2);
    }

// org.jfree.chart.renderer.xy.junit.XYSplineRendererTests::testCloning
    public void testCloning() {
        Rectangle2D legendShape = new Rectangle2D.Double(1.0, 2.0, 3.0, 4.0);
        XYSplineRenderer r1 = new XYSplineRenderer();
        r1.setLegendLine(legendShape);
        XYSplineRenderer r2 = null;
        try {
            r2 = (XYSplineRenderer) r1.clone();
        }
        catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        assertTrue(r1 != r2);
        assertTrue(r1.getClass() == r2.getClass());
        assertTrue(r1.equals(r2));
    }

// org.jfree.chart.renderer.xy.junit.XYSplineRendererTests::testPublicCloneable
    public void testPublicCloneable() {
        XYSplineRenderer r1 = new XYSplineRenderer();
        assertTrue(r1 instanceof PublicCloneable);
    }

// org.jfree.chart.renderer.xy.junit.XYSplineRendererTests::testSerialization
    public void testSerialization() {

        XYSplineRenderer r1 = new XYSplineRenderer();
        XYSplineRenderer r2 = null;
        try {
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            ObjectOutput out = new ObjectOutputStream(buffer);
            out.writeObject(r1);
            out.close();

            ObjectInput in = new ObjectInputStream(
                    new ByteArrayInputStream(buffer.toByteArray()));
            r2 = (XYSplineRenderer) in.readObject();
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

// org.jfree.chart.util.junit.ShapeListTests::testEquals
    public void testEquals() {
        ShapeList l1 = new ShapeList();
        l1.setShape(0, new Rectangle(1, 2, 3, 4));
        l1.setShape(1, new Line2D.Double(1.0, 2.0, 3.0, 4.0));
        l1.setShape(2, null);

        ShapeList l2 = new ShapeList();
        l2.setShape(0, new Rectangle(1, 2, 3, 4));
        l2.setShape(1, new Line2D.Double(1.0, 2.0, 3.0, 4.0));
        l2.setShape(2, null);

        assertTrue(l1.equals(l2));
        assertTrue(l2.equals(l2));
    }

// org.jfree.chart.util.junit.ShapeListTests::testCloning
    public void testCloning() {

        ShapeList l1 = new ShapeList();
        l1.setShape(0, new Rectangle(1, 2, 3, 4));
        l1.setShape(1, new Line2D.Double(1.0, 2.0, 3.0, 4.0));
        l1.setShape(2, null);

        ShapeList l2 = null;
        try {
            l2 = (ShapeList) l1.clone();
        }
        catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        assertTrue(l1 != l2);
        assertTrue(l1.getClass() == l2.getClass());
        assertTrue(l1.equals(l2));

        l2.setShape(0, new Rectangle(5, 6, 7, 8));
        assertFalse(l1.equals(l2));

    }

// org.jfree.chart.util.junit.ShapeListTests::testSerialization
    public void testSerialization() {

        ShapeList l1 = new ShapeList();
        l1.setShape(0, new Rectangle(1, 2, 3, 4));
        l1.setShape(1, new Line2D.Double(1.0, 2.0, 3.0, 4.0));
        l1.setShape(2, null);

        ShapeList l2 = null;

        try {
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            ObjectOutput out = new ObjectOutputStream(buffer);
            out.writeObject(l1);
            out.close();

            ObjectInput in = new ObjectInputStream(new ByteArrayInputStream(
            		buffer.toByteArray()));
            l2 = (ShapeList) in.readObject();
            in.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        assertEquals(l1, l2);

    }

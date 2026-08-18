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

// org.jfree.chart.block.junit.AbstractBlockTests::testEquals
    public void testEquals() {
        EmptyBlock b1 = new EmptyBlock(1.0, 2.0);
        EmptyBlock b2 = new EmptyBlock(1.0, 2.0);
        assertTrue(b1.equals(b2));
        assertTrue(b2.equals(b2));
        
        b1.setID("Test");
        assertFalse(b1.equals(b2));
        b2.setID("Test");
        assertTrue(b1.equals(b2));
        
        b1.setMargin(new RectangleInsets(1.0, 2.0, 3.0, 4.0));
        assertFalse(b1.equals(b2));
        b2.setMargin(new RectangleInsets(1.0, 2.0, 3.0, 4.0));
        assertTrue(b1.equals(b2));
        
        b1.setFrame(new BlockBorder(Color.red));
        assertFalse(b1.equals(b2));
        b2.setFrame(new BlockBorder(Color.red));
        assertTrue(b1.equals(b2));
        
        b1.setPadding(new RectangleInsets(2.0, 4.0, 6.0, 8.0));
        assertFalse(b1.equals(b2));
        b2.setPadding(new RectangleInsets(2.0, 4.0, 6.0, 8.0));
        assertTrue(b1.equals(b2));
        
        b1.setWidth(1.23);
        assertFalse(b1.equals(b2));
        b2.setWidth(1.23);
        assertTrue(b1.equals(b2));
        
        b1.setHeight(4.56);
        assertFalse(b1.equals(b2));
        b2.setHeight(4.56);
        assertTrue(b1.equals(b2));
        
        b1.setBounds(new Rectangle2D.Double(1.0, 2.0, 3.0, 4.0));
        assertFalse(b1.equals(b2));
        b2.setBounds(new Rectangle2D.Double(1.0, 2.0, 3.0, 4.0));
        assertTrue(b1.equals(b2));
        
        b1 = new EmptyBlock(1.1, 2.0);
        assertFalse(b1.equals(b2));
        b2 = new EmptyBlock(1.1, 2.0);
        assertTrue(b1.equals(b2));

        b1 = new EmptyBlock(1.1, 2.2);
        assertFalse(b1.equals(b2));
        b2 = new EmptyBlock(1.1, 2.2);
        assertTrue(b1.equals(b2));    
    }

// org.jfree.chart.block.junit.AbstractBlockTests::testCloning
    public void testCloning() {
        EmptyBlock b1 = new EmptyBlock(1.0, 2.0);
        Rectangle2D bounds1 = new Rectangle2D.Double(1.0, 2.0, 3.0, 4.0);
        b1.setBounds(bounds1);
        EmptyBlock b2 = null;
        
        try {
            b2 = (EmptyBlock) b1.clone();
        }
        catch (CloneNotSupportedException e) {
            fail(e.toString());
        }
        assertTrue(b1 != b2);
        assertTrue(b1.getClass() == b2.getClass());
        assertTrue(b1.equals(b2));
        
        bounds1.setFrame(2.0, 4.0, 6.0, 8.0);
        assertFalse(b1.equals(b2));
        b2.setBounds(new Rectangle2D.Double(2.0, 4.0, 6.0, 8.0));
        assertTrue(b1.equals(b2));
    }

// org.jfree.chart.block.junit.AbstractBlockTests::testSerialization
    public void testSerialization() {
        EmptyBlock b1 = new EmptyBlock(1.0, 2.0);
        EmptyBlock b2 = null;
        try {
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            ObjectOutput out = new ObjectOutputStream(buffer);
            out.writeObject(b1);
            out.close();

            ObjectInput in = new ObjectInputStream(
                    new ByteArrayInputStream(buffer.toByteArray()));
            b2 = (EmptyBlock) in.readObject();
            in.close();
        }
        catch (Exception e) {
            fail(e.toString());
        }
        assertEquals(b1, b2);
    }

// org.jfree.chart.block.junit.BlockContainerTests::testEquals
    public void testEquals() {
        BlockContainer c1 = new BlockContainer(new FlowArrangement());
        BlockContainer c2 = new BlockContainer(new FlowArrangement());
        assertTrue(c1.equals(c2));
        assertTrue(c2.equals(c2));
        
        c1.setArrangement(new ColumnArrangement());
        assertFalse(c1.equals(c2));
        c2.setArrangement(new ColumnArrangement());
        assertTrue(c1.equals(c2));
        
        c1.add(new EmptyBlock(1.2, 3.4));
        assertFalse(c1.equals(c2));
        c2.add(new EmptyBlock(1.2, 3.4));
        assertTrue(c1.equals(c2));
    }

// org.jfree.chart.block.junit.BlockContainerTests::testCloning
    public void testCloning() {
        BlockContainer c1 = new BlockContainer(new FlowArrangement());
        c1.add(new EmptyBlock(1.2, 3.4));
        
        BlockContainer c2 = null;
        
        try {
            c2 = (BlockContainer) c1.clone();
        }
        catch (CloneNotSupportedException e) {
            System.err.println("Failed to clone.");
        }
        assertTrue(c1 != c2);
        assertTrue(c1.getClass() == c2.getClass());
        assertTrue(c1.equals(c2));
    }

// org.jfree.chart.block.junit.BlockContainerTests::testSerialization
    public void testSerialization() {
        BlockContainer c1 = new BlockContainer();
        c1.add(new EmptyBlock(1.2, 3.4));
        BlockContainer c2 = null;
        try {
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            ObjectOutput out = new ObjectOutputStream(buffer);
            out.writeObject(c1);
            out.close();

            ObjectInput in = new ObjectInputStream(
                new ByteArrayInputStream(buffer.toByteArray())
            );
            c2 = (BlockContainer) in.readObject();
            in.close();
        }
        catch (Exception e) {
            fail(e.toString());
        }
        assertEquals(c1, c2);
    }

// org.jfree.chart.block.junit.ColorBlockTests::testEquals
    public void testEquals() {
        ColorBlock b1 = new ColorBlock(Color.red, 1.0, 2.0);
        ColorBlock b2 = new ColorBlock(Color.red, 1.0, 2.0);
        assertTrue(b1.equals(b2));
        assertTrue(b2.equals(b2));
        
        b1 = new ColorBlock(Color.blue, 1.0, 2.0);
        assertFalse(b1.equals(b2));
        b2 = new ColorBlock(Color.blue, 1.0, 2.0);
        assertTrue(b1.equals(b2));
        
        b1 = new ColorBlock(Color.blue, 1.1, 2.0);
        assertFalse(b1.equals(b2));
        b2 = new ColorBlock(Color.blue, 1.1, 2.0);
        assertTrue(b1.equals(b2));
        
        b1 = new ColorBlock(Color.blue, 1.1, 2.2);
        assertFalse(b1.equals(b2));
        b2 = new ColorBlock(Color.blue, 1.1, 2.2);
        assertTrue(b1.equals(b2));
    }

// org.jfree.chart.block.junit.ColorBlockTests::testCloning
    public void testCloning() {
        GradientPaint gp = new GradientPaint(1.0f, 2.0f, Color.red, 3.0f, 4.0f,
                Color.blue);
        Rectangle2D bounds1 = new Rectangle2D.Double(10.0, 20.0, 30.0, 40.0);
        ColorBlock b1 = new ColorBlock(gp, 1.0, 2.0);
        b1.setBounds(bounds1);
        ColorBlock b2 = null;
        
        try {
            b2 = (ColorBlock) b1.clone();
        }
        catch (CloneNotSupportedException e) {
            fail(e.toString());
        }
        assertTrue(b1 != b2);
        assertTrue(b1.getClass() == b2.getClass());
        assertTrue(b1.equals(b2));
        
        
        bounds1.setRect(1.0, 2.0, 3.0, 4.0);
        assertFalse(b1.equals(b2));
        b2.setBounds(new Rectangle2D.Double(1.0, 2.0, 3.0, 4.0));
        assertTrue(b1.equals(b2));
    }

// org.jfree.chart.block.junit.ColorBlockTests::testSerialization
    public void testSerialization() {
        GradientPaint gp = new GradientPaint(1.0f, 2.0f, Color.red, 3.0f, 4.0f,
                Color.blue);
        ColorBlock b1 = new ColorBlock(gp, 1.0, 2.0);
        ColorBlock b2 = null;
        try {
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            ObjectOutput out = new ObjectOutputStream(buffer);
            out.writeObject(b1);
            out.close();

            ObjectInput in = new ObjectInputStream(
                    new ByteArrayInputStream(buffer.toByteArray()));
            b2 = (ColorBlock) in.readObject();
            in.close();
        }
        catch (Exception e) {
            fail(e.toString());
        }
        assertEquals(b1, b2);
    }

// org.jfree.chart.block.junit.EmptyBlockTests::testEquals
    public void testEquals() {
        EmptyBlock b1 = new EmptyBlock(1.0, 2.0);
        EmptyBlock b2 = new EmptyBlock(1.0, 2.0);
        assertTrue(b1.equals(b2));
        assertTrue(b2.equals(b2));
        
        b1 = new EmptyBlock(1.1, 2.0);
        assertFalse(b1.equals(b2));
        b2 = new EmptyBlock(1.1, 2.0);
        assertTrue(b1.equals(b2));

        b1 = new EmptyBlock(1.1, 2.2);
        assertFalse(b1.equals(b2));
        b2 = new EmptyBlock(1.1, 2.2);
        assertTrue(b1.equals(b2));    
    }

// org.jfree.chart.block.junit.EmptyBlockTests::testCloning
    public void testCloning() {
        EmptyBlock b1 = new EmptyBlock(1.0, 2.0);
        EmptyBlock b2 = null;
        
        try {
            b2 = (EmptyBlock) b1.clone();
        }
        catch (CloneNotSupportedException e) {
            fail(e.toString());
        }
        assertTrue(b1 != b2);
        assertTrue(b1.getClass() == b2.getClass());
        assertTrue(b1.equals(b2));
    }

// org.jfree.chart.block.junit.EmptyBlockTests::testSerialization
    public void testSerialization() {
        EmptyBlock b1 = new EmptyBlock(1.0, 2.0);
        EmptyBlock b2 = null;
        try {
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            ObjectOutput out = new ObjectOutputStream(buffer);
            out.writeObject(b1);
            out.close();

            ObjectInput in = new ObjectInputStream(
                    new ByteArrayInputStream(buffer.toByteArray()));
            b2 = (EmptyBlock) in.readObject();
            in.close();
        }
        catch (Exception e) {
            fail(e.toString());
        }
        assertEquals(b1, b2);
    }

// org.jfree.chart.block.junit.LabelBlockTests::testEquals
    public void testEquals() {
        LabelBlock b1 = new LabelBlock("ABC", new Font("Dialog", 
                Font.PLAIN, 12), Color.red);
        LabelBlock b2 = new LabelBlock("ABC", new Font("Dialog", 
                Font.PLAIN, 12), Color.red);
        assertTrue(b1.equals(b2));
        assertTrue(b2.equals(b2));
        
        b1 = new LabelBlock("XYZ", new Font("Dialog", Font.PLAIN, 12), 
                Color.red);
        assertFalse(b1.equals(b2));
        b2 = new LabelBlock("XYZ", new Font("Dialog", Font.PLAIN, 12), 
                Color.red);
        assertTrue(b1.equals(b2));

        b1 = new LabelBlock("XYZ", new Font("Dialog", Font.BOLD, 12), 
                Color.red);
        assertFalse(b1.equals(b2));
        b2 = new LabelBlock("XYZ", new Font("Dialog", Font.BOLD, 12), 
                Color.red);
        assertTrue(b1.equals(b2));    

        b1 = new LabelBlock("XYZ", new Font("Dialog", Font.BOLD, 12), 
                Color.blue);
        assertFalse(b1.equals(b2));
        b2 = new LabelBlock("XYZ", new Font("Dialog", Font.BOLD, 12), 
                Color.blue);
        assertTrue(b1.equals(b2));
        
        b1.setToolTipText("Tooltip");
        assertFalse(b1.equals(b2));
        b2.setToolTipText("Tooltip");
        assertTrue(b1.equals(b2));
        
        b1.setURLText("URL");
        assertFalse(b1.equals(b2));
        b2.setURLText("URL");
        assertTrue(b1.equals(b2));
    }

// org.jfree.chart.block.junit.LabelBlockTests::testCloning
    public void testCloning() {
        LabelBlock b1 = new LabelBlock("ABC", new Font("Dialog", 
                Font.PLAIN, 12), Color.red);
        LabelBlock b2 = null;
        
        try {
            b2 = (LabelBlock) b1.clone();
        }
        catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        assertTrue(b1 != b2);
        assertTrue(b1.getClass() == b2.getClass());
        assertTrue(b1.equals(b2));
    }

// org.jfree.chart.block.junit.LabelBlockTests::testSerialization
    public void testSerialization() {
        GradientPaint gp = new GradientPaint(1.0f, 2.0f, Color.red, 3.0f, 4.0f, 
                Color.blue);
        LabelBlock b1 = new LabelBlock("ABC", new Font("Dialog", 
                Font.PLAIN, 12), gp);
        LabelBlock b2 = null;
        try {
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            ObjectOutput out = new ObjectOutputStream(buffer);
            out.writeObject(b1);
            out.close();

            ObjectInput in = new ObjectInputStream(
                    new ByteArrayInputStream(buffer.toByteArray()));
            b2 = (LabelBlock) in.readObject();
            in.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        assertEquals(b1, b2);
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

// org.jfree.chart.junit.LegendItemCollectionTests::testEquals
    public void testEquals() {
        
        LegendItemCollection c1 = new LegendItemCollection();
        LegendItemCollection c2 = new LegendItemCollection();
        assertTrue(c1.equals(c2));
        assertTrue(c2.equals(c1));

        LegendItem item1 = new LegendItem("Label", "Description", 
                "ToolTip", "URL", true,  
                new Rectangle2D.Double(1.0, 2.0, 3.0, 4.0), true, Color.red, 
                true, Color.blue, new BasicStroke(1.2f), true, 
                new Line2D.Double(1.0, 2.0, 3.0, 4.0), 
                new BasicStroke(2.1f), Color.green);
        LegendItem item2 = new LegendItem("Label", "Description", 
                "ToolTip", "URL", true, 
                new Rectangle2D.Double(1.0, 2.0, 3.0, 4.0), 
                true, Color.red, true, Color.blue, new BasicStroke(1.2f), true, 
                new Line2D.Double(1.0, 2.0, 3.0, 4.0), new BasicStroke(2.1f), 
                Color.green);
        c1.add(item1);
        c2.add(item2);
        assertTrue(c1.equals(c2));
        
    }

// org.jfree.chart.junit.LegendItemCollectionTests::testSerialization
    public void testSerialization() {
        LegendItemCollection c1 = new LegendItemCollection();
        c1.add(new LegendItem("Item", "Description", "ToolTip", "URL", 
                new Rectangle2D.Double(1.0, 2.0, 3.0, 4.0), Color.red)); 
        LegendItemCollection c2 = null;
        try {
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            ObjectOutput out = new ObjectOutputStream(buffer);
            out.writeObject(c1);
            out.close();

            ObjectInput in = new ObjectInputStream(new ByteArrayInputStream(
                    buffer.toByteArray()));
            c2 = (LegendItemCollection) in.readObject();
            in.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        assertEquals(c1, c2);
    }

// org.jfree.chart.junit.LegendItemCollectionTests::testCloning
    public void testCloning() {

        LegendItemCollection c1 = new LegendItemCollection();
        c1.add(new LegendItem("Item", "Description", "ToolTip", "URL", 
                new Rectangle2D.Double(1.0, 2.0, 3.0, 4.0), Color.red)); 
        LegendItemCollection c2 = null;
        try {
            c2 = (LegendItemCollection) c1.clone();
        }
        catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        assertTrue(c1 != c2);
        assertTrue(c1.getClass() == c2.getClass());
        assertTrue(c1.equals(c2));
        
    }

// org.jfree.chart.junit.LegendItemTests::testEquals
    public void testEquals() {
        
        LegendItem item1 = new LegendItem("Label", "Description", 
                "ToolTip", "URL", true, 
                new Rectangle2D.Double(1.0, 2.0, 3.0, 4.0), true, Color.red, 
                true, Color.blue, new BasicStroke(1.2f), true, 
                new Line2D.Double(1.0, 2.0, 3.0, 4.0), 
                new BasicStroke(2.1f), Color.green);  
        LegendItem item2 = new LegendItem("Label", "Description", 
                "ToolTip", "URL", true, 
                new Rectangle2D.Double(1.0, 2.0, 3.0, 4.0), 
                true, Color.red, true, Color.blue, new BasicStroke(1.2f), true, 
                new Line2D.Double(1.0, 2.0, 3.0, 4.0), new BasicStroke(2.1f), 
                Color.green);  
        assertTrue(item1.equals(item2));  
        assertTrue(item2.equals(item1));  
        
        item1 = new LegendItem("Label2", "Description", "ToolTip", "URL",
                true, new Rectangle2D.Double(1.0, 2.0, 3.0, 4.0), true, 
                Color.red, true, Color.blue, new BasicStroke(1.2f), true, 
                new Line2D.Double(1.0, 2.0, 3.0, 4.0), new BasicStroke(2.1f), 
                Color.green); 
        assertFalse(item1.equals(item2));
        item2 = new LegendItem("Label2", "Description", "ToolTip", "URL", 
                true, new Rectangle2D.Double(1.0, 2.0, 3.0, 4.0), 
                true, Color.red, true, Color.blue, new BasicStroke(1.2f), true, 
                new Line2D.Double(1.0, 2.0, 3.0, 4.0), new BasicStroke(2.1f), 
                Color.green); 
        assertTrue(item1.equals(item2));
        
        item1 = new LegendItem("Label2", "Description2", "ToolTip", 
                "URL", true, new Rectangle2D.Double(1.0, 2.0, 3.0, 4.0), 
                true, Color.red, true, Color.blue, new BasicStroke(1.2f), true, 
                new Line2D.Double(1.0, 2.0, 3.0, 4.0), new BasicStroke(2.1f), 
                Color.green); 
        assertFalse(item1.equals(item2));
        item2 = new LegendItem("Label2", "Description2", "ToolTip", 
                "URL", true, new Rectangle2D.Double(1.0, 2.0, 3.0, 4.0), 
                true, Color.red, true, Color.blue, new BasicStroke(1.2f), true, 
                new Line2D.Double(1.0, 2.0, 3.0, 4.0), new BasicStroke(2.1f), 
                Color.green); 
        assertTrue(item1.equals(item2));
        
        item1 = new LegendItem("Label2", "Description2", "ToolTip", 
                "URL", false, new Rectangle2D.Double(1.0, 2.0, 3.0, 4.0), 
                true, Color.red, true, Color.blue, new BasicStroke(1.2f), true, 
                new Line2D.Double(1.0, 2.0, 3.0, 4.0), new BasicStroke(2.1f), 
                Color.green); 
        assertFalse(item1.equals(item2));
        item2 = new LegendItem("Label2", "Description2", "ToolTip", 
                "URL", false, new Rectangle2D.Double(1.0, 2.0, 3.0, 4.0), 
                true, Color.red, true, Color.blue, new BasicStroke(1.2f), true, 
                new Line2D.Double(1.0, 2.0, 3.0, 4.0), new BasicStroke(2.1f), 
                Color.green); 
        assertTrue(item1.equals(item2));
        
        item1 = new LegendItem("Label2", "Description2", "ToolTip", 
                "URL", false, new Rectangle2D.Double(4.0, 3.0, 2.0, 1.0), 
                true, Color.red, true, Color.blue, new BasicStroke(1.2f), true, 
                new Line2D.Double(1.0, 2.0, 3.0, 4.0), new BasicStroke(2.1f), 
                Color.green); 
        assertFalse(item1.equals(item2));
        item2 = new LegendItem("Label2", "Description2", "ToolTip", 
                "URL", false, new Rectangle2D.Double(4.0, 3.0, 2.0, 1.0), 
                true, Color.red, true, Color.blue, new BasicStroke(1.2f), true, 
                new Line2D.Double(1.0, 2.0, 3.0, 4.0), new BasicStroke(2.1f), 
                Color.green); 
        assertTrue(item1.equals(item2));
        
        item1 = new LegendItem("Label2", "Description2", "ToolTip", 
                "URL", false, new Rectangle2D.Double(4.0, 3.0, 2.0, 1.0), 
                false, Color.red, true, Color.blue, new BasicStroke(1.2f), true, 
                new Line2D.Double(1.0, 2.0, 3.0, 4.0), new BasicStroke(2.1f), 
                Color.green); 
        assertFalse(item1.equals(item2));
        item2 = new LegendItem("Label2", "Description2", "ToolTip", 
                "URL", false, new Rectangle2D.Double(4.0, 3.0, 2.0, 1.0), 
                false, Color.red, true, Color.blue, new BasicStroke(1.2f), true, 
                new Line2D.Double(1.0, 2.0, 3.0, 4.0), new BasicStroke(2.1f), 
                Color.green); 
        assertTrue(item1.equals(item2));
        
        item1 = new LegendItem("Label2", "Description2", "ToolTip", "URL",
                false, new Rectangle2D.Double(4.0, 3.0, 2.0, 1.0), false, 
                Color.black, true, Color.blue, new BasicStroke(1.2f), true, 
                new Line2D.Double(1.0, 2.0, 3.0, 4.0), new BasicStroke(2.1f), 
                Color.green); 
        assertFalse(item1.equals(item2));
        item2 = new LegendItem("Label2", "Description2", "ToolTip", "URL", 
                false, new Rectangle2D.Double(4.0, 3.0, 2.0, 1.0), false, 
                Color.black, true, Color.blue, new BasicStroke(1.2f), true, 
                new Line2D.Double(1.0, 2.0, 3.0, 4.0), new BasicStroke(2.1f), 
                Color.green); 
        assertTrue(item1.equals(item2));
        
        item1 = new LegendItem("Label2", "Description2", "ToolTip", 
                "URL", false, new Rectangle2D.Double(4.0, 3.0, 2.0, 1.0), 
                false, Color.black, false, Color.blue, new BasicStroke(1.2f), 
                true, new Line2D.Double(1.0, 2.0, 3.0, 4.0), 
                new BasicStroke(2.1f), Color.green); 
        assertFalse(item1.equals(item2));
        item2 = new LegendItem("Label2", "Description2", "ToolTip", "URL",
                false, new Rectangle2D.Double(4.0, 3.0, 2.0, 1.0), false, 
                Color.black, false, Color.blue, new BasicStroke(1.2f), true, 
                new Line2D.Double(1.0, 2.0, 3.0, 4.0), new BasicStroke(2.1f), 
                Color.green); 
        assertTrue(item1.equals(item2));
        
        item1 = new LegendItem("Label2", "Description2", "ToolTip", "URL", 
                false, new Rectangle2D.Double(4.0, 3.0, 2.0, 1.0), false, 
                Color.black, false, Color.yellow, new BasicStroke(1.2f), true, 
                new Line2D.Double(1.0, 2.0, 3.0, 4.0), new BasicStroke(2.1f), 
                Color.green); 
        assertFalse(item1.equals(item2));
        item2 = new LegendItem("Label2", "Description2", "ToolTip", "URL", 
                false, new Rectangle2D.Double(4.0, 3.0, 2.0, 1.0), false, 
                Color.black, false, Color.yellow, new BasicStroke(1.2f), true, 
                new Line2D.Double(1.0, 2.0, 3.0, 4.0), new BasicStroke(2.1f), 
                Color.green); 
        assertTrue(item1.equals(item2));
        
        item1 = new LegendItem("Label2", "Description2", "ToolTip", "URL",
                false, new Rectangle2D.Double(4.0, 3.0, 2.0, 1.0), false, 
                Color.black, false, Color.yellow, new BasicStroke(2.1f), true, 
                new Line2D.Double(1.0, 2.0, 3.0, 4.0), new BasicStroke(2.1f), 
                Color.green); 
        assertFalse(item1.equals(item2));
        item2 = new LegendItem("Label2", "Description2", "ToolTip", "URL",
                false, new Rectangle2D.Double(4.0, 3.0, 2.0, 1.0), false, 
                Color.black, false, Color.yellow, new BasicStroke(2.1f), true, 
                new Line2D.Double(1.0, 2.0, 3.0, 4.0), new BasicStroke(2.1f), 
                Color.green); 
        assertTrue(item1.equals(item2));
        
        item1 = new LegendItem("Label2", "Description2", "ToolTip", 
                "URL", false, new Rectangle2D.Double(4.0, 3.0, 2.0, 1.0), 
                false, Color.black, false, Color.yellow, new BasicStroke(2.1f), 
                false, new Line2D.Double(1.0, 2.0, 3.0, 4.0), 
                new BasicStroke(2.1f), Color.green); 
        assertFalse(item1.equals(item2));
        item2 = new LegendItem("Label2", "Description2", "ToolTip", 
                "URL", false, new Rectangle2D.Double(4.0, 3.0, 2.0, 1.0), 
                false, Color.black, false, Color.yellow, new BasicStroke(2.1f),
                false, new Line2D.Double(1.0, 2.0, 3.0, 4.0), 
                new BasicStroke(2.1f),  Color.green); 
        assertTrue(item1.equals(item2));
        
        item1 = new LegendItem("Label2", "Description2", "ToolTip", 
                "URL", false, new Rectangle2D.Double(4.0, 3.0, 2.0, 1.0), 
                false, Color.black, false, Color.yellow, new BasicStroke(2.1f),
                false, new Line2D.Double(4.0, 3.0, 2.0, 1.0), 
                new BasicStroke(2.1f), Color.green); 
        assertFalse(item1.equals(item2));
        item2 = new LegendItem("Label2", "Description2", "ToolTip", 
                "URL", false, new Rectangle2D.Double(4.0, 3.0, 2.0, 1.0), 
                false, Color.black, false, Color.yellow, new BasicStroke(2.1f),
                false, new Line2D.Double(4.0, 3.0, 2.0, 1.0), 
                new BasicStroke(2.1f), Color.green); 
        assertTrue(item1.equals(item2));
        
        item1 = new LegendItem("Label2", "Description2", "ToolTip", 
                "URL", false, new Rectangle2D.Double(4.0, 3.0, 2.0, 1.0), 
                false, Color.black, false, Color.yellow, new BasicStroke(2.1f), 
                false, new Line2D.Double(4.0, 3.0, 2.0, 1.0), 
                new BasicStroke(3.3f), Color.green); 
        assertFalse(item1.equals(item2));
        item2 = new LegendItem("Label2", "Description2", "ToolTip", 
                "URL", false, new Rectangle2D.Double(4.0, 3.0, 2.0, 1.0), 
                false, Color.black, false, Color.yellow, new BasicStroke(2.1f), 
                false, new Line2D.Double(4.0, 3.0, 2.0, 1.0), 
                new BasicStroke(3.3f), Color.green); 
        assertTrue(item1.equals(item2));
        
        item1 = new LegendItem("Label2", "Description2", "ToolTip", "URL",
                false, new Rectangle2D.Double(4.0, 3.0, 2.0, 1.0), false, 
                Color.black, false, Color.yellow, new BasicStroke(2.1f), false, 
            new Line2D.Double(4.0, 3.0, 2.0, 1.0), new BasicStroke(3.3f), 
            Color.white
        ); 
        assertFalse(item1.equals(item2));
        item2 = new LegendItem("Label2", "Description2", "ToolTip", 
                "URL", false, new Rectangle2D.Double(4.0, 3.0, 2.0, 1.0), 
                false, Color.black, false, Color.yellow, new BasicStroke(2.1f), 
                false, new Line2D.Double(4.0, 3.0, 2.0, 1.0), 
                new BasicStroke(3.3f), 
                Color.white); 
        assertTrue(item1.equals(item2));
        
        
        item1.setFillPaintTransformer(new StandardGradientPaintTransformer(
                GradientPaintTransformType.CENTER_VERTICAL));
        assertFalse(item1.equals(item2));
        item2.setFillPaintTransformer(new StandardGradientPaintTransformer(
                GradientPaintTransformType.CENTER_VERTICAL));
        assertTrue(item1.equals(item2));
    }

// org.jfree.chart.junit.LegendItemTests::testSerialization
    public void testSerialization() {
        LegendItem item1 = new LegendItem("Item", "Description", 
                "ToolTip", "URL", 
                new Rectangle2D.Double(1.0, 2.0, 3.0, 4.0), Color.red); 
        LegendItem item2 = null;
        try {
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            ObjectOutput out = new ObjectOutputStream(buffer);
            out.writeObject(item1);
            out.close();

            ObjectInput in = new ObjectInputStream(
                    new ByteArrayInputStream(buffer.toByteArray()));
            item2 = (LegendItem) in.readObject();
            in.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        assertEquals(item1, item2);
    }

// org.jfree.chart.junit.LegendItemTests::testSerialization2
    public void testSerialization2() {
        AttributedString as = new AttributedString("Test String");
        as.addAttribute(TextAttribute.FONT, new Font("Dialog", Font.PLAIN, 12));
        LegendItem item1 = new LegendItem(as, "Description", "ToolTip", "URL", 
                new Rectangle2D.Double(1.0, 2.0, 3.0, 4.0), Color.red); 
        LegendItem item2 = null;
        try {
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            ObjectOutput out = new ObjectOutputStream(buffer);
            out.writeObject(item1);
            out.close();

            ObjectInput in = new ObjectInputStream(
                    new ByteArrayInputStream(buffer.toByteArray()));
            item2 = (LegendItem) in.readObject();
            in.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        assertEquals(item1, item2);
    }

// org.jfree.chart.junit.LegendItemTests::testCloning
    public void testCloning() {
        LegendItem item = new LegendItem("Item", "Description", 
                "ToolTip", "URL", 
                new Rectangle2D.Double(1.0, 2.0, 3.0, 4.0), Color.red); 
        assertFalse(item instanceof Cloneable);
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

// org.jfree.chart.junit.MeterChartTests::testDrawWithNullInfo
    public void testDrawWithNullInfo() {
        boolean success = false;
        MeterPlot plot = new MeterPlot(new DefaultValueDataset(60.0));
        plot.addInterval(new MeterInterval("Normal", new Range(0.0, 80.0)));
        JFreeChart chart = new JFreeChart(plot);
        try {
            BufferedImage image = new BufferedImage(200, 100, 
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

// org.jfree.chart.plot.dial.junit.DialPlotTests::testEquals
    public void testEquals() {
        DialPlot p1 = new DialPlot();
        DialPlot p2 = new DialPlot();
        assertTrue(p1.equals(p2));
        
        
        p1.setBackground(new DialBackground(Color.green));
        assertFalse(p1.equals(p2));
        p2.setBackground(new DialBackground(Color.green));
        assertTrue(p1.equals(p2));
        
        p1.setBackground(null);
        assertFalse(p1.equals(p2));
        p2.setBackground(null);
        assertTrue(p1.equals(p2));
        
        
        DialCap cap1 = new DialCap();
        cap1.setFillPaint(Color.red);
        p1.setCap(cap1);
        assertFalse(p1.equals(p2));
        DialCap cap2 = new DialCap();
        cap2.setFillPaint(Color.red);
        p2.setCap(cap2);
        assertTrue(p1.equals(p2));
        
        p1.setCap(null);
        assertFalse(p1.equals(p2));
        p2.setCap(null);
        assertTrue(p1.equals(p2));
        
        
        StandardDialFrame f1 = new StandardDialFrame();
        f1.setBackgroundPaint(new GradientPaint(1.0f, 2.0f, Color.red, 3.0f, 
                4.0f, Color.white));
        p1.setDialFrame(f1);
        assertFalse(p1.equals(p2));
        StandardDialFrame f2 = new StandardDialFrame();
        f2.setBackgroundPaint(new GradientPaint(1.0f, 2.0f, Color.red, 3.0f, 
                4.0f, Color.white));
        p2.setDialFrame(f2);
        assertTrue(p1.equals(p2));
        
        
        p1.setView(0.2, 0.0, 0.8, 1.0);
        assertFalse(p1.equals(p2));
        p2.setView(0.2, 0.0, 0.8, 1.0);
        assertTrue(p1.equals(p2));
        
        
        p1.addLayer(new StandardDialScale());
        assertFalse(p1.equals(p2));
        p2.addLayer(new StandardDialScale());
        assertTrue(p1.equals(p2));
    }

// org.jfree.chart.plot.dial.junit.DialPlotTests::testHashCode
    public void testHashCode() {
        DialPlot p1 = new DialPlot();
        DialPlot p2 = new DialPlot();
        assertTrue(p1.equals(p2));
        int h1 = p1.hashCode();
        int h2 = p2.hashCode();
        assertEquals(h1, h2);
    }

// org.jfree.chart.plot.dial.junit.DialPlotTests::testCloning
    public void testCloning() {
        DialPlot p1 = new DialPlot();
        DialPlot p2 = null;
        try {
            p2 = (DialPlot) p1.clone();
        }
        catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        assertTrue(p1 != p2);
        assertTrue(p1.getClass() == p2.getClass());
        assertTrue(p1.equals(p2));
    }

// org.jfree.chart.plot.dial.junit.DialPlotTests::testSerialization
    public void testSerialization() {
        DialPlot p1 = new DialPlot();
        DialPlot p2 = null;

        try {
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            ObjectOutput out = new ObjectOutputStream(buffer);
            out.writeObject(p1);
            out.close();

            ObjectInput in = new ObjectInputStream(
                    new ByteArrayInputStream(buffer.toByteArray()));
            p2 = (DialPlot) in.readObject();
            in.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        assertEquals(p1, p2);
    }

// org.jfree.chart.plot.dial.junit.DialPlotTests::testBackgroundListener
    public void testBackgroundListener() {
        DialPlot p = new DialPlot();
        DialBackground b1 = new DialBackground(Color.red);
        p.setBackground(b1);
        p.addChangeListener(this);
        this.lastEvent = null;
        b1.setPaint(Color.blue);
        assertNotNull(this.lastEvent);
        
        DialBackground b2 = new DialBackground(Color.green);
        p.setBackground(b2);
        this.lastEvent = null;
        b1.setPaint(Color.red);
        assertNull(this.lastEvent);
        b2.setPaint(Color.red);
        assertNotNull(this.lastEvent);
    }

// org.jfree.chart.plot.dial.junit.DialPlotTests::testCapListener
    public void testCapListener() {
        DialPlot p = new DialPlot();
        DialCap c1 = new DialCap();
        p.setCap(c1);
        p.addChangeListener(this);
        this.lastEvent = null;
        c1.setFillPaint(Color.red);
        assertNotNull(this.lastEvent);
        
        DialCap c2 = new DialCap();
        p.setCap(c2);
        this.lastEvent = null;
        c1.setFillPaint(Color.blue);
        assertNull(this.lastEvent);
        c2.setFillPaint(Color.green);
        assertNotNull(this.lastEvent);
    }

// org.jfree.chart.plot.dial.junit.DialPlotTests::testFrameListener
    public void testFrameListener() {
        DialPlot p = new DialPlot();
        ArcDialFrame f1 = new ArcDialFrame();
        p.setDialFrame(f1);
        p.addChangeListener(this);
        this.lastEvent = null;
        f1.setBackgroundPaint(Color.gray);
        assertNotNull(this.lastEvent);
        
        ArcDialFrame f2 = new ArcDialFrame();
        p.setDialFrame(f2);
        this.lastEvent = null;
        f1.setBackgroundPaint(Color.blue);
        assertNull(this.lastEvent);
        f2.setBackgroundPaint(Color.green);
        assertNotNull(this.lastEvent);
    }

// org.jfree.chart.plot.dial.junit.DialPlotTests::testScaleListener
    public void testScaleListener() {
        DialPlot p = new DialPlot();
        StandardDialScale s1 = new StandardDialScale();
        p.addScale(0, s1);
        p.addChangeListener(this);
        this.lastEvent = null;
        s1.setStartAngle(22.0);
        assertNotNull(this.lastEvent);
        
        StandardDialScale s2 = new StandardDialScale();
        p.addScale(0, s2);
        this.lastEvent = null;
        s1.setStartAngle(33.0);
        assertNull(this.lastEvent);
        s2.setStartAngle(33.0);
        assertNotNull(this.lastEvent);
    }

// org.jfree.chart.plot.dial.junit.DialPlotTests::testLayerListener
    public void testLayerListener() {
        DialPlot p = new DialPlot();
        DialBackground b1 = new DialBackground(Color.red);
        p.addLayer(b1);
        p.addChangeListener(this);
        this.lastEvent = null;
        b1.setPaint(Color.blue);
        assertNotNull(this.lastEvent);
        
        DialBackground b2 = new DialBackground(Color.green);
        p.addLayer(b2);
        this.lastEvent = null;
        b1.setPaint(Color.red);
        assertNotNull(this.lastEvent);
        b2.setPaint(Color.green);
        assertNotNull(this.lastEvent);
        
        p.removeLayer(b2);
        this.lastEvent = null;
        b2.setPaint(Color.red);
        assertNull(this.lastEvent);   
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
            System.err.println("Failed to clone.");
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

            ObjectInput in = new ObjectInputStream(
                new ByteArrayInputStream(buffer.toByteArray())
            );
            plot2 = (CombinedDomainXYPlot) in.readObject();
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
            System.err.println("Failed to clone.");
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

            ObjectInput in = new ObjectInputStream(
                new ByteArrayInputStream(buffer.toByteArray())
            );
            plot2 = (CombinedRangeXYPlot) in.readObject();
            in.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        assertEquals(plot1, plot2);

    }

// org.jfree.chart.plot.junit.CompassPlotTests::testEquals
    public void testEquals() {
        CompassPlot plot1 = new CompassPlot();
        CompassPlot plot2 = new CompassPlot();
        assertTrue(plot1.equals(plot2));    
        
        
        plot1.setLabelType(CompassPlot.VALUE_LABELS);
        assertFalse(plot1.equals(plot2));
        plot2.setLabelType(CompassPlot.VALUE_LABELS);
        assertTrue(plot1.equals(plot2));
        
        
        plot1.setLabelFont(new Font("Serif", Font.PLAIN, 10));
        assertFalse(plot1.equals(plot2));
        plot2.setLabelFont(new Font("Serif", Font.PLAIN, 10));
        assertTrue(plot1.equals(plot2));
        
        
        plot1.setDrawBorder(true);
        assertFalse(plot1.equals(plot2));
        plot2.setDrawBorder(true);
        assertTrue(plot1.equals(plot2));
        
        
        plot1.setRosePaint(new GradientPaint(1.0f, 2.0f, Color.blue, 
                3.0f, 4.0f, Color.yellow));
        assertFalse(plot1.equals(plot2));
        plot2.setRosePaint(new GradientPaint(1.0f, 2.0f, Color.blue, 
                3.0f, 4.0f, Color.yellow));
        assertTrue(plot1.equals(plot2));
        
        
        plot1.setRoseCenterPaint(new GradientPaint(1.0f, 2.0f, Color.red, 
                3.0f, 4.0f, Color.yellow));
        assertFalse(plot1.equals(plot2));
        plot2.setRoseCenterPaint(new GradientPaint(1.0f, 2.0f, Color.red, 
                3.0f, 4.0f, Color.yellow));
        assertTrue(plot1.equals(plot2));
        
        
        plot1.setRoseHighlightPaint(new GradientPaint(1.0f, 2.0f, Color.green, 
                3.0f, 4.0f, Color.yellow));
        assertFalse(plot1.equals(plot2));
        plot2.setRoseHighlightPaint(new GradientPaint(1.0f, 2.0f, Color.green, 
                3.0f, 4.0f, Color.yellow));
        assertTrue(plot1.equals(plot2));
    }

// org.jfree.chart.plot.junit.CompassPlotTests::testSerialization
    public void testSerialization() {

        CompassPlot p1 = new CompassPlot(null);
        p1.setRosePaint(new GradientPaint(1.0f, 2.0f, Color.red, 3.0f, 4.0f, 
                Color.blue));
        p1.setRoseCenterPaint(new GradientPaint(4.0f, 3.0f, Color.red, 2.0f,
                1.0f, Color.green));
        p1.setRoseHighlightPaint(new GradientPaint(4.0f, 3.0f, Color.red, 2.0f,
                1.0f, Color.green));
        CompassPlot p2 = null;

        try {
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            ObjectOutput out = new ObjectOutputStream(buffer);
            out.writeObject(p1);
            out.close();

            ObjectInput in = new ObjectInputStream(
                    new ByteArrayInputStream(buffer.toByteArray()));
            p2 = (CompassPlot) in.readObject();
            in.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        assertEquals(p1, p2);

    }

// org.jfree.chart.plot.junit.CompassPlotTests::testCloning
    public void testCloning() {
        CompassPlot p1 = new CompassPlot(new DefaultValueDataset(15.0));
        CompassPlot p2 = null;
        try {
            p2 = (CompassPlot) p1.clone();
        }
        catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        assertTrue(p1 != p2);
        assertTrue(p1.getClass() == p2.getClass());
        assertTrue(p1.equals(p2));
    }

// org.jfree.chart.plot.junit.DefaultDrawingSupplierTests::testEquals
    public void testEquals() {
        DefaultDrawingSupplier r1 = new DefaultDrawingSupplier();
        DefaultDrawingSupplier r2 = new DefaultDrawingSupplier();
        assertTrue(r1.equals(r2));
        assertTrue(r2.equals(r1));
        
        
        Paint[] ps1A = new Paint[] {Color.red, Color.blue};
        Paint[] ps2A = new Paint[] {Color.green, Color.yellow, Color.white};
        Paint[] ops1A = new Paint[] {Color.lightGray, Color.blue};
        Paint[] ops2A = new Paint[] {Color.black, Color.yellow, Color.cyan};
        Stroke[] ss1A = new Stroke[] {new BasicStroke(1.1f)};
        Stroke[] ss2A 
            = new Stroke[] {new BasicStroke(2.2f), new BasicStroke(3.3f)};
        Stroke[] oss1A = new Stroke[] {new BasicStroke(4.4f)};
        Stroke[] oss2A 
            = new Stroke[] {new BasicStroke(5.5f), new BasicStroke(6.6f)};
        Shape[] shapes1A = new Shape[] {
            new Rectangle2D.Double(1.0, 1.0, 1.0, 1.0)
        };
        Shape[] shapes2A = new Shape[] {
            new Rectangle2D.Double(2.0, 2.0, 2.0, 2.0),
            new Rectangle2D.Double(2.0, 2.0, 2.0, 2.0)
        };
        Paint[] ps1B = new Paint[] {Color.red, Color.blue};
        Paint[] ps2B = new Paint[] {Color.green, Color.yellow, Color.white};
        Paint[] ops1B = new Paint[] {Color.lightGray, Color.blue};
        Paint[] ops2B = new Paint[] {Color.black, Color.yellow, Color.cyan};
        Stroke[] ss1B = new Stroke[] {new BasicStroke(1.1f)};
        Stroke[] ss2B 
            = new Stroke[] {new BasicStroke(2.2f), new BasicStroke(3.3f)};
        Stroke[] oss1B = new Stroke[] {new BasicStroke(4.4f)};
        Stroke[] oss2B 
            = new Stroke[] {new BasicStroke(5.5f), new BasicStroke(6.6f)};
        Shape[] shapes1B = new Shape[] {
            new Rectangle2D.Double(1.0, 1.0, 1.0, 1.0)
        };
        Shape[] shapes2B = new Shape[] {
            new Rectangle2D.Double(2.0, 2.0, 2.0, 2.0),
            new Rectangle2D.Double(2.0, 2.0, 2.0, 2.0)
        };
        
        r1 = new DefaultDrawingSupplier(ps1A, ops1A, ss1A, oss1A, shapes1A);
        r2 = new DefaultDrawingSupplier(ps1B, ops1B, ss1B, oss1B, shapes1B);
        assertTrue(r1.equals(r2));
        
        
        r1 = new DefaultDrawingSupplier(ps2A, ops1A, ss1A, oss1A, shapes1A);
        assertFalse(r1.equals(r2));
        r2 = new DefaultDrawingSupplier(ps2B, ops1B, ss1B, oss1B, shapes1B);
        assertTrue(r1.equals(r2));
        
        r1 = new DefaultDrawingSupplier(ps2A, ops2A, ss1A, oss1A, shapes1A);
        assertFalse(r1.equals(r2));
        r2 = new DefaultDrawingSupplier(ps2B, ops2B, ss1B, oss1B, shapes1B);
        assertTrue(r1.equals(r2));
        
        r1 = new DefaultDrawingSupplier(ps2A, ops2A, ss2A, oss1A, shapes1A);
        assertFalse(r1.equals(r2));
        r2 = new DefaultDrawingSupplier(ps2B, ops2B, ss2B, oss1B, shapes1B);
        assertTrue(r1.equals(r2));
        
        r1 = new DefaultDrawingSupplier(ps2A, ops2A, ss2A, oss2A, shapes1A);
        assertFalse(r1.equals(r2));
        r2 = new DefaultDrawingSupplier(ps2B, ops2B, ss2B, oss2B, shapes1B);
        assertTrue(r1.equals(r2));
        
        r1 = new DefaultDrawingSupplier(ps2A, ops2A, ss2A, oss2A, shapes2A);
        assertFalse(r1.equals(r2));
        r2 = new DefaultDrawingSupplier(ps2B, ops2B, ss2B, oss2B, shapes2B);
        assertTrue(r1.equals(r2));
        
        
        r1.getNextPaint();
        assertFalse(r1.equals(r2));
        r2.getNextPaint();
        assertTrue(r1.equals(r2));
        
        
        r1.getNextOutlinePaint();
        assertFalse(r1.equals(r2));
        r2.getNextOutlinePaint();
        assertTrue(r1.equals(r2));
        
        
        r1.getNextStroke();
        assertFalse(r1.equals(r2));
        r2.getNextStroke();
        assertTrue(r1.equals(r2));
        
        
        r1.getNextOutlineStroke();
        assertFalse(r1.equals(r2));
        r2.getNextOutlineStroke();
        assertTrue(r1.equals(r2));
        
        
        r1.getNextShape();
        assertFalse(r1.equals(r2));
        r2.getNextShape();
        assertTrue(r1.equals(r2));
    }

// org.jfree.chart.plot.junit.DefaultDrawingSupplierTests::testCloning
    public void testCloning() {
        DefaultDrawingSupplier r1 = new DefaultDrawingSupplier();
        DefaultDrawingSupplier r2 = null;
        try {
            r2 = (DefaultDrawingSupplier) r1.clone();
        }
        catch (CloneNotSupportedException e) {
            e.printStackTrace();
            System.err.println("Failed to clone.");
        }
        assertTrue(r1 != r2);
        assertTrue(r1.getClass() == r2.getClass());
        assertTrue(r1.equals(r2));
    }

// org.jfree.chart.plot.junit.DefaultDrawingSupplierTests::testSerialization
    public void testSerialization() {

        DefaultDrawingSupplier r1 = new DefaultDrawingSupplier();
        DefaultDrawingSupplier r2 = null;

        try {
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            ObjectOutput out = new ObjectOutputStream(buffer);
            out.writeObject(r1);
            out.close();

            ObjectInput in = new ObjectInputStream(
                new ByteArrayInputStream(buffer.toByteArray())
            );
            r2 = (DefaultDrawingSupplier) in.readObject();
            in.close();
        }
        catch (Exception e) {
            System.out.println(e.toString());
        }
        assertEquals(r1, r2);

    }

// org.jfree.chart.plot.junit.FastScatterPlotTests::testEquals
    public void testEquals() {
        
        FastScatterPlot plot1 = new FastScatterPlot();
        FastScatterPlot plot2 = new FastScatterPlot();
        assertTrue(plot1.equals(plot2));    
        assertTrue(plot2.equals(plot1));
        
        plot1.setPaint(new GradientPaint(1.0f, 2.0f, Color.red, 
                3.0f, 4.0f, Color.yellow));
        assertFalse(plot1.equals(plot2));
        plot2.setPaint(new GradientPaint(1.0f, 2.0f, Color.red, 
                3.0f, 4.0f, Color.yellow));
        assertTrue(plot1.equals(plot2));
        
        plot1.setDomainGridlinesVisible(false);
        assertFalse(plot1.equals(plot2));
        plot2.setDomainGridlinesVisible(false);
        assertTrue(plot1.equals(plot2));
        
        plot1.setDomainGridlinePaint(new GradientPaint(1.0f, 2.0f, Color.blue, 
                3.0f, 4.0f, Color.yellow));
        assertFalse(plot1.equals(plot2));
        plot2.setDomainGridlinePaint(new GradientPaint(1.0f, 2.0f, Color.blue, 
                3.0f, 4.0f, Color.yellow));
        assertTrue(plot1.equals(plot2));
        
        Stroke s = new BasicStroke(1.5f);
        plot1.setDomainGridlineStroke(s);
        assertFalse(plot1.equals(plot2));
        plot2.setDomainGridlineStroke(s);
        assertTrue(plot1.equals(plot2));
        
        plot1.setRangeGridlinesVisible(false);
        assertFalse(plot1.equals(plot2));
        plot2.setRangeGridlinesVisible(false);
        assertTrue(plot1.equals(plot2));
        
        plot1.setRangeGridlinePaint(new GradientPaint(1.0f, 2.0f, Color.green, 
                3.0f, 4.0f, Color.yellow));
        assertFalse(plot1.equals(plot2));
        plot2.setRangeGridlinePaint(new GradientPaint(1.0f, 2.0f, Color.green, 
                3.0f, 4.0f, Color.yellow));
        assertTrue(plot1.equals(plot2));
        
        Stroke s2 = new BasicStroke(1.5f);
        plot1.setRangeGridlineStroke(s2);
        assertFalse(plot1.equals(plot2));
        plot2.setRangeGridlineStroke(s2);
        assertTrue(plot1.equals(plot2));
        
    }

// org.jfree.chart.plot.junit.FastScatterPlotTests::testCloning
    public void testCloning() {
        FastScatterPlot p1 = new FastScatterPlot();
        FastScatterPlot p2 = null;
        try {
            p2 = (FastScatterPlot) p1.clone();
        }
        catch (CloneNotSupportedException e) {
            e.printStackTrace();
            System.err.println("Failed to clone.");
        }
        assertTrue(p1 != p2);
        assertTrue(p1.getClass() == p2.getClass());
        assertTrue(p1.equals(p2));
    }

// org.jfree.chart.plot.junit.FastScatterPlotTests::testSerialization
    public void testSerialization() {

        float[][] data = createData();

        ValueAxis domainAxis = new NumberAxis("X");
        ValueAxis rangeAxis = new NumberAxis("Y");
        FastScatterPlot p1 = new FastScatterPlot(data, domainAxis, rangeAxis);
        FastScatterPlot p2 = null;

        try {
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            ObjectOutput out = new ObjectOutputStream(buffer);
            out.writeObject(p1);
            out.close();

            ObjectInput in = new ObjectInputStream(
                new ByteArrayInputStream(buffer.toByteArray())
            );
            p2 = (FastScatterPlot) in.readObject();
            in.close();
        }
        catch (Exception e) {
            System.out.println(e.toString());
        }
        assertEquals(p1, p2);

    }

// org.jfree.chart.plot.junit.FastScatterPlotTests::testDrawWithNullInfo
    public void testDrawWithNullInfo() {
        boolean success = false;
        try {
            float[][] data = createData();

            ValueAxis domainAxis = new NumberAxis("X");
            ValueAxis rangeAxis = new NumberAxis("Y");
            FastScatterPlot plot = new FastScatterPlot(data, domainAxis, 
                    rangeAxis);
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

// org.jfree.chart.plot.junit.MeterPlotTests::testEquals
    public void testEquals() {
        MeterPlot plot1 = new MeterPlot();
        MeterPlot plot2 = new MeterPlot();
        assertTrue(plot1.equals(plot2));    
        
        
        plot1.setUnits("mph");
        assertFalse(plot1.equals(plot2));
        plot2.setUnits("mph");
        assertTrue(plot1.equals(plot2));
        
        
        plot1.setRange(new Range(50.0, 70.0));
        assertFalse(plot1.equals(plot2));
        plot2.setRange(new Range(50.0, 70.0));
        assertTrue(plot1.equals(plot2));
        
        
        plot1.addInterval(new MeterInterval("Normal", new Range(55.0, 60.0)));
        assertFalse(plot1.equals(plot2));
        plot2.addInterval(new MeterInterval("Normal", new Range(55.0, 60.0)));
        assertTrue(plot1.equals(plot2));
        
        
        plot1.setDialOutlinePaint(new GradientPaint(1.0f, 2.0f, Color.red, 
                3.0f, 4.0f, Color.blue));
        assertFalse(plot1.equals(plot2));
        plot2.setDialOutlinePaint(new GradientPaint(1.0f, 2.0f, Color.red, 
                3.0f, 4.0f, Color.blue));
        assertTrue(plot1.equals(plot2));
        
        
        plot1.setDialShape(DialShape.CHORD);
        assertFalse(plot1.equals(plot2));
        plot2.setDialShape(DialShape.CHORD);
        assertTrue(plot1.equals(plot2));
        
        
        plot1.setDialBackgroundPaint(new GradientPaint(9.0f, 8.0f, Color.red, 
                7.0f, 6.0f, Color.blue));
        assertFalse(plot1.equals(plot2));
        plot2.setDialBackgroundPaint(new GradientPaint(9.0f, 8.0f, Color.red, 
                7.0f, 6.0f, Color.blue));
        assertTrue(plot1.equals(plot2));
         
        
        plot1.setDialOutlinePaint(new GradientPaint(1.0f, 2.0f, Color.green,
                3.0f, 4.0f, Color.red));
        assertFalse(plot1.equals(plot2));
        plot2.setDialOutlinePaint(new GradientPaint(1.0f, 2.0f, Color.green,
                3.0f, 4.0f, Color.red));
        assertTrue(plot1.equals(plot2));
        
        
        plot1.setNeedlePaint(new GradientPaint(9.0f, 8.0f, Color.red, 
                7.0f, 6.0f, Color.blue));
        assertFalse(plot1.equals(plot2));
        plot2.setNeedlePaint(new GradientPaint(9.0f, 8.0f, Color.red, 
                7.0f, 6.0f, Color.blue));
        assertTrue(plot1.equals(plot2));
        
        
        plot1.setValueFont(new Font("Serif", Font.PLAIN, 6));
        assertFalse(plot1.equals(plot2));
        plot2.setValueFont(new Font("Serif", Font.PLAIN, 6));
        assertTrue(plot1.equals(plot2));
        
        
        plot1.setValuePaint(new GradientPaint(1.0f, 2.0f, Color.black, 
                3.0f, 4.0f, Color.white));
        assertFalse(plot1.equals(plot2));
        plot2.setValuePaint(new GradientPaint(1.0f, 2.0f, Color.black, 
                3.0f, 4.0f, Color.white));
        assertTrue(plot1.equals(plot2));
        
        
        plot1.setTickLabelsVisible(false);
        assertFalse(plot1.equals(plot2));
        plot2.setTickLabelsVisible(false);
        assertTrue(plot1.equals(plot2));
        
        
        plot1.setTickLabelFont(new Font("Serif", Font.PLAIN, 6));
        assertFalse(plot1.equals(plot2));
        plot2.setTickLabelFont(new Font("Serif", Font.PLAIN, 6));
        assertTrue(plot1.equals(plot2));
        
        
        plot1.setTickLabelPaint(Color.red);
        assertFalse(plot1.equals(plot2));
        plot2.setTickLabelPaint(Color.red);
        assertTrue(plot1.equals(plot2));        
        
        
        plot1.setTickLabelFormat(new DecimalFormat("0"));
        assertFalse(plot1.equals(plot2));
        plot2.setTickLabelFormat(new DecimalFormat("0"));
        assertTrue(plot1.equals(plot2));
        
        
        plot1.setTickPaint(Color.green);
        assertFalse(plot1.equals(plot2));
        plot2.setTickPaint(Color.green);
        assertTrue(plot1.equals(plot2));
        
        
        plot1.setTickSize(1.23);
        assertFalse(plot1.equals(plot2));
        plot2.setTickSize(1.23);
        assertTrue(plot1.equals(plot2));        
        
        
        plot1.setDrawBorder(!plot1.getDrawBorder());
        assertFalse(plot1.equals(plot2));
        plot2.setDrawBorder(plot1.getDrawBorder());
        assertTrue(plot1.equals(plot2));
        
        
        plot1.setMeterAngle(22);
        assertFalse(plot1.equals(plot2));
        plot2.setMeterAngle(22);
        assertTrue(plot1.equals(plot2));
        
    }

// org.jfree.chart.plot.junit.MeterPlotTests::testCloning
    public void testCloning() {
        MeterPlot p1 = new MeterPlot();
        MeterPlot p2 = null;
        try {
            p2 = (MeterPlot) p1.clone();
        }
        catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        assertTrue(p1 != p2);
        assertTrue(p1.getClass() == p2.getClass());
        assertTrue(p1.equals(p2));
        
        
        assertTrue(p1.getDataset() == p2.getDataset());
        
        
        
        p1.getTickLabelFormat().setMinimumIntegerDigits(99);
        assertFalse(p1.equals(p2));
        p2.getTickLabelFormat().setMinimumIntegerDigits(99);
        assertTrue(p1.equals(p2));
        
        p1.addInterval(new MeterInterval("Test", new Range(1.234, 5.678)));
        assertFalse(p1.equals(p2));
        p2.addInterval(new MeterInterval("Test", new Range(1.234, 5.678)));
        assertTrue(p1.equals(p2));
        
    }

// org.jfree.chart.plot.junit.MeterPlotTests::testSerialization1
    public void testSerialization1() {
        MeterPlot p1 = new MeterPlot(null);
        p1.setDialBackgroundPaint(new GradientPaint(1.0f, 2.0f, Color.red,
                3.0f, 4.0f, Color.blue));
        p1.setDialOutlinePaint(new GradientPaint(4.0f, 3.0f, Color.red,
                2.0f, 1.0f, Color.blue));
        p1.setNeedlePaint(new GradientPaint(1.0f, 2.0f, Color.red,
                3.0f, 4.0f, Color.blue));
        p1.setTickLabelPaint(new GradientPaint(1.0f, 2.0f, Color.red,
                3.0f, 4.0f, Color.blue));
        p1.setTickPaint(new GradientPaint(1.0f, 2.0f, Color.red,
                3.0f, 4.0f, Color.blue));
        MeterPlot p2 = null;
        try {
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            ObjectOutput out = new ObjectOutputStream(buffer);
            out.writeObject(p1);
            out.close();

            ObjectInput in = new ObjectInputStream(
                     new ByteArrayInputStream(buffer.toByteArray()));
            p2 = (MeterPlot) in.readObject();
            in.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        assertEquals(p1, p2);
    }

// org.jfree.chart.plot.junit.MeterPlotTests::testSerialization2
    public void testSerialization2() {
        MeterPlot p1 = new MeterPlot(new DefaultValueDataset(1.23));
        MeterPlot p2 = null;
        try {
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            ObjectOutput out = new ObjectOutputStream(buffer);
            out.writeObject(p1);
            out.close();

            ObjectInput in = new ObjectInputStream(
                     new ByteArrayInputStream(buffer.toByteArray()));
            p2 = (MeterPlot) in.readObject();
            in.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        assertEquals(p1, p2);

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

// org.jfree.chart.plot.junit.PlotTests::testEquals
    public void testEquals() {
        PiePlot plot1 = new PiePlot();
        PiePlot plot2 = new PiePlot();
        assertTrue(plot1.equals(plot2));    
        assertTrue(plot2.equals(plot1));

        
        plot1.setNoDataMessage("No data XYZ");
        assertFalse(plot1.equals(plot2));
        plot2.setNoDataMessage("No data XYZ");
        assertTrue(plot1.equals(plot2));
        
        
        plot1.setNoDataMessageFont(new Font("SansSerif", Font.PLAIN, 13));
        assertFalse(plot1.equals(plot2));
        plot2.setNoDataMessageFont(new Font("SansSerif", Font.PLAIN, 13));
        assertTrue(plot1.equals(plot2));
        
        
        plot1.setNoDataMessagePaint(new GradientPaint(1.0f, 2.0f, Color.red, 
                3.0f, 4.0f, Color.blue));
        assertFalse(plot1.equals(plot2));
        plot2.setNoDataMessagePaint(new GradientPaint(1.0f, 2.0f, Color.red, 
                3.0f, 4.0f, Color.blue));
        assertTrue(plot1.equals(plot2));
        
        
        plot1.setInsets(new RectangleInsets(1.0, 2.0, 3.0, 4.0));
        assertFalse(plot1.equals(plot2));
        plot2.setInsets(new RectangleInsets(1.0, 2.0, 3.0, 4.0));
        assertTrue(plot1.equals(plot2));
        
        
        plot1.setOutlineVisible(false);
        assertFalse(plot1.equals(plot2));
        plot2.setOutlineVisible(false);
        assertTrue(plot1.equals(plot2));
        
        
        BasicStroke s = new BasicStroke(1.23f);
        plot1.setOutlineStroke(s);
        assertFalse(plot1.equals(plot2));
        plot2.setOutlineStroke(s);
        assertTrue(plot1.equals(plot2));
        
        
        plot1.setOutlinePaint(new GradientPaint(1.0f, 2.0f, Color.yellow, 
                3.0f, 4.0f, Color.green));
        assertFalse(plot1.equals(plot2));
        plot2.setOutlinePaint(new GradientPaint(1.0f, 2.0f, Color.yellow, 
                3.0f, 4.0f, Color.green));
        assertTrue(plot1.equals(plot2));
        
        
        plot1.setBackgroundPaint(new GradientPaint(1.0f, 2.0f, Color.cyan, 
                3.0f, 4.0f, Color.green));
        assertFalse(plot1.equals(plot2));
        plot2.setBackgroundPaint(new GradientPaint(1.0f, 2.0f, Color.cyan, 
                3.0f, 4.0f, Color.green));
        assertTrue(plot1.equals(plot2));
        
        
        plot1.setBackgroundImage(JFreeChart.INFO.getLogo());
        assertFalse(plot1.equals(plot2));
        plot2.setBackgroundImage(JFreeChart.INFO.getLogo());
        assertTrue(plot1.equals(plot2));
        
        
        plot1.setBackgroundImageAlignment(Align.BOTTOM_RIGHT);
        assertFalse(plot1.equals(plot2));
        plot2.setBackgroundImageAlignment(Align.BOTTOM_RIGHT);
        assertTrue(plot1.equals(plot2));
        
        
        plot1.setBackgroundImageAlpha(0.77f);
        assertFalse(plot1.equals(plot2));
        plot2.setBackgroundImageAlpha(0.77f);
        assertTrue(plot1.equals(plot2));
        
        
        plot1.setForegroundAlpha(0.99f);
        assertFalse(plot1.equals(plot2));
        plot2.setForegroundAlpha(0.99f);
        assertTrue(plot1.equals(plot2));
        
        
        plot1.setBackgroundAlpha(0.99f);
        assertFalse(plot1.equals(plot2));
        plot2.setBackgroundAlpha(0.99f);
        assertTrue(plot1.equals(plot2));
        
        
        plot1.setDrawingSupplier(new DefaultDrawingSupplier(
                new Paint[] {Color.blue}, new Paint[] {Color.red}, 
                new Stroke[] {new BasicStroke(1.1f)}, 
                new Stroke[] {new BasicStroke(9.9f)}, 
                new Shape[] {new Rectangle(1, 2, 3, 4)}));
        assertFalse(plot1.equals(plot2));
        plot2.setDrawingSupplier(new DefaultDrawingSupplier(
                new Paint[] {Color.blue}, new Paint[] {Color.red}, 
                new Stroke[] {new BasicStroke(1.1f)}, 
                new Stroke[] {new BasicStroke(9.9f)}, 
                new Shape[] {new Rectangle(1, 2, 3, 4)}));
        assertTrue(plot1.equals(plot2));
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

// org.jfree.chart.plot.junit.RingPlotTests::testEquals
    public void testEquals() {
        
        RingPlot plot1 = new RingPlot(null);
        RingPlot plot2 = new RingPlot(null);
        assertTrue(plot1.equals(plot2));
        assertTrue(plot2.equals(plot1));
                
        
        plot1.setSeparatorsVisible(false);
        assertFalse(plot1.equals(plot2));
        plot2.setSeparatorsVisible(false);
        assertTrue(plot1.equals(plot2));
        
        
        Stroke s = new BasicStroke(1.1f);
        plot1.setSeparatorStroke(s);
        assertFalse(plot1.equals(plot2));
        plot2.setSeparatorStroke(s);
        assertTrue(plot1.equals(plot2));

        
        plot1.setSeparatorPaint(new GradientPaint(1.0f, 2.0f, Color.red, 
                2.0f, 1.0f, Color.blue));
        assertFalse(plot1.equals(plot2));
        plot2.setSeparatorPaint(new GradientPaint(1.0f, 2.0f, Color.red, 
                2.0f, 1.0f, Color.blue));
        assertTrue(plot1.equals(plot2));
        
        
        plot1.setInnerSeparatorExtension(0.01);
        assertFalse(plot1.equals(plot2));
        plot2.setInnerSeparatorExtension(0.01);
        assertTrue(plot1.equals(plot2));
        
        
        plot1.setOuterSeparatorExtension(0.02);
        assertFalse(plot1.equals(plot2));
        plot2.setOuterSeparatorExtension(0.02);
        assertTrue(plot1.equals(plot2));

        
        plot1.setSectionDepth(0.12);
        assertFalse(plot1.equals(plot2));
        plot2.setSectionDepth(0.12);
        assertTrue(plot1.equals(plot2));
        
    }

// org.jfree.chart.plot.junit.RingPlotTests::testCloning
    public void testCloning() {
        RingPlot p1 = new RingPlot(null);
        GradientPaint gp = new GradientPaint(1.0f, 2.0f, Color.yellow,
                3.0f, 4.0f, Color.red);
        p1.setSeparatorPaint(gp);
        RingPlot p2 = null;
        try {
            p2 = (RingPlot) p1.clone();
        }
        catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        assertTrue(p1 != p2);
        assertTrue(p1.getClass() == p2.getClass());
        assertTrue(p1.equals(p2));
    }

// org.jfree.chart.plot.junit.RingPlotTests::testSerialization
    public void testSerialization() {

        RingPlot p1 = new RingPlot(null);
        GradientPaint gp = new GradientPaint(1.0f, 2.0f, Color.yellow,
                3.0f, 4.0f, Color.red);
        p1.setSeparatorPaint(gp);
        RingPlot p2 = null;
        try {
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            ObjectOutput out = new ObjectOutputStream(buffer);
            out.writeObject(p1);
            out.close();

            ObjectInput in = new ObjectInputStream(
                    new ByteArrayInputStream(buffer.toByteArray()));
            p2 = (RingPlot) in.readObject();
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

// org.jfree.chart.plot.junit.ThermometerPlotTests::testEquals
    public void testEquals() {
    	ThermometerPlot p1 = new ThermometerPlot();
    	ThermometerPlot p2 = new ThermometerPlot();
    	assertTrue(p1.equals(p2));
    	assertTrue(p2.equals(p1));
        
        
        p1.setPadding(new RectangleInsets(1.0, 2.0, 3.0, 4.0));
        assertFalse(p1.equals(p2));
        p2.setPadding(new RectangleInsets(1.0, 2.0, 3.0, 4.0));
        assertTrue(p2.equals(p1));

        
        BasicStroke s = new BasicStroke(1.23f);
        p1.setThermometerStroke(s);
        assertFalse(p1.equals(p2));
        p2.setThermometerStroke(s);
        assertTrue(p2.equals(p1));

        
        p1.setThermometerPaint(new GradientPaint(1.0f, 2.0f, Color.blue, 
                3.0f, 4.0f, Color.red));
        assertFalse(p1.equals(p2));
        p2.setThermometerPaint(new GradientPaint(1.0f, 2.0f, Color.blue, 
                3.0f, 4.0f, Color.red));
        assertTrue(p2.equals(p1));
        
        
        p1.setUnits(ThermometerPlot.UNITS_KELVIN);
        assertFalse(p1.equals(p2));
        p2.setUnits(ThermometerPlot.UNITS_KELVIN);
        assertTrue(p2.equals(p1));
        
        
        p1.setValueLocation(ThermometerPlot.LEFT);
        assertFalse(p1.equals(p2));
        p2.setValueLocation(ThermometerPlot.LEFT);
        assertTrue(p2.equals(p1));
        
        
        p1.setAxisLocation(ThermometerPlot.RIGHT);
        assertFalse(p1.equals(p2));
        p2.setAxisLocation(ThermometerPlot.RIGHT);
        assertTrue(p2.equals(p1));
        
        
        p1.setValueFont(new Font("Serif", Font.PLAIN, 9));
        assertFalse(p1.equals(p2));
        p2.setValueFont(new Font("Serif", Font.PLAIN, 9));
        assertTrue(p2.equals(p1));
        
        
        p1.setValuePaint(new GradientPaint(4.0f, 5.0f, Color.red, 
                6.0f, 7.0f, Color.white));
        assertFalse(p1.equals(p2));
        p2.setValuePaint(new GradientPaint(4.0f, 5.0f, Color.red, 
                6.0f, 7.0f, Color.white));
        assertTrue(p2.equals(p1));
        
        
        p1.setValueFormat(new DecimalFormat("0.0000"));
        assertFalse(p1.equals(p2));
        p2.setValueFormat(new DecimalFormat("0.0000"));
        assertTrue(p2.equals(p1));
        
        
        p1.setMercuryPaint(new GradientPaint(9.0f, 8.0f, Color.red, 
                7.0f, 6.0f, Color.blue));
        assertFalse(p1.equals(p2));
        p2.setMercuryPaint(new GradientPaint(9.0f, 8.0f, Color.red, 
                7.0f, 6.0f, Color.blue));
        assertTrue(p2.equals(p1));
        
        p1.setSubrange(1, 1.0, 2.0);
        assertFalse(p1.equals(p2));
        p2.setSubrange(1, 1.0, 2.0);
        assertTrue(p2.equals(p1));
        
        p1.setSubrangePaint(1, new GradientPaint(1.0f, 2.0f, Color.red, 
                3.0f, 4.0f, Color.yellow));
        assertFalse(p1.equals(p2));
        p2.setSubrangePaint(1, new GradientPaint(1.0f, 2.0f, Color.red, 
                3.0f, 4.0f, Color.yellow));
        assertTrue(p2.equals(p1));
        
        p1.setBulbRadius(9);
        assertFalse(p1.equals(p2));
        p2.setBulbRadius(9);
        assertTrue(p2.equals(p1));
        
        p1.setColumnRadius(8);
        assertFalse(p1.equals(p2));
        p2.setColumnRadius(8);
        assertTrue(p2.equals(p1));
        
        p1.setGap(7);
        assertFalse(p1.equals(p2));
        p2.setGap(7);
        assertTrue(p2.equals(p1));
    }

// org.jfree.chart.plot.junit.ThermometerPlotTests::testCloning
    public void testCloning() {
        ThermometerPlot p1 = new ThermometerPlot();
        ThermometerPlot p2 = null;
        try {
            p2 = (ThermometerPlot) p1.clone();
        }
        catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        assertTrue(p1 != p2);
        assertTrue(p1.getClass() == p2.getClass());
        assertTrue(p1.equals(p2));
    }

// org.jfree.chart.plot.junit.ThermometerPlotTests::testSerialization
    public void testSerialization() {

        ThermometerPlot p1 = new ThermometerPlot();
        ThermometerPlot p2 = null;

        try {
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            ObjectOutput out = new ObjectOutputStream(buffer);
            out.writeObject(p1);
            out.close();

            ObjectInput in = new ObjectInputStream(
                    new ByteArrayInputStream(buffer.toByteArray()));
            p2 = (ThermometerPlot) in.readObject();
            in.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        assertTrue(p1.equals(p2));

    }

// org.jfree.chart.plot.junit.ThermometerPlotTests::testSerialization2
    public void testSerialization2() {
        ThermometerPlot p1 = new ThermometerPlot();
        p1.setSubrangePaint(1, new GradientPaint(1.0f, 2.0f, Color.red, 3.0f, 
                4.0f, Color.blue));
        ThermometerPlot p2 = null;

        try {
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            ObjectOutput out = new ObjectOutputStream(buffer);
            out.writeObject(p1);
            out.close();

            ObjectInput in = new ObjectInputStream(
                    new ByteArrayInputStream(buffer.toByteArray()));
            p2 = (ThermometerPlot) in.readObject();
            in.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        assertTrue(p1.equals(p2));
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

// org.jfree.chart.renderer.category.junit.BarRenderer3DTests::testEquals
    public void testEquals() {
        BarRenderer3D r1 = new BarRenderer3D(1.0, 2.0);
        BarRenderer3D r2 = new BarRenderer3D(1.0, 2.0);
        assertEquals(r1, r2);
        
        r1 = new BarRenderer3D(1.1, 2.0);
        assertFalse(r1.equals(r2));
        r2 = new BarRenderer3D(1.1, 2.0);
        assertTrue(r1.equals(r2));
        
        r1 = new BarRenderer3D(1.1, 2.2);
        assertFalse(r1.equals(r2));
        r2 = new BarRenderer3D(1.1, 2.2);
        assertTrue(r1.equals(r2));

        r1.setWallPaint(new GradientPaint(1.0f, 2.0f, Color.red, 4.0f, 3.0f, 
                Color.blue));
        assertFalse(r1.equals(r2));
        r2.setWallPaint(new GradientPaint(1.0f, 2.0f, Color.red, 4.0f, 3.0f, 
                Color.blue));
        assertTrue(r1.equals(r2));
    }

// org.jfree.chart.renderer.category.junit.BarRenderer3DTests::testHashcode
    public void testHashcode() {
        BarRenderer3D r1 = new BarRenderer3D();
        BarRenderer3D r2 = new BarRenderer3D();
        assertTrue(r1.equals(r2));
        int h1 = r1.hashCode();
        int h2 = r2.hashCode();
        assertEquals(h1, h2);
    }

// org.jfree.chart.renderer.category.junit.BarRenderer3DTests::testCloning
    public void testCloning() {
        BarRenderer3D r1 = new BarRenderer3D();
        BarRenderer3D r2 = null;
        try {
            r2 = (BarRenderer3D) r1.clone();
        }
        catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        assertTrue(r1 != r2);
        assertTrue(r1.getClass() == r2.getClass());
        assertTrue(r1.equals(r2));
    }

// org.jfree.chart.renderer.category.junit.BarRenderer3DTests::testSerialization
    public void testSerialization() {
        BarRenderer3D r1 = new BarRenderer3D();
        r1.setWallPaint(new GradientPaint(1.0f, 2.0f, Color.red, 4.0f, 3.0f, 
                Color.blue));
        BarRenderer3D r2 = null;
        try {
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            ObjectOutput out = new ObjectOutputStream(buffer);
            out.writeObject(r1);
            out.close();

            ObjectInput in = new ObjectInputStream(
                    new ByteArrayInputStream(buffer.toByteArray()));
            r2 = (BarRenderer3D) in.readObject();
            in.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        assertEquals(r1, r2);
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

// org.jfree.chart.renderer.category.junit.DefaultCategoryItemRendererTests::testEquals
    public void testEquals() {
        DefaultCategoryItemRenderer r1 = new DefaultCategoryItemRenderer();
        DefaultCategoryItemRenderer r2 = new DefaultCategoryItemRenderer();
        assertEquals(r1, r2);
    }

// org.jfree.chart.renderer.category.junit.DefaultCategoryItemRendererTests::testHashcode
    public void testHashcode() {
        DefaultCategoryItemRenderer r1 = new DefaultCategoryItemRenderer();
        DefaultCategoryItemRenderer r2 = new DefaultCategoryItemRenderer();
        assertTrue(r1.equals(r2));
        int h1 = r1.hashCode();
        int h2 = r2.hashCode();
        assertEquals(h1, h2);
    }

// org.jfree.chart.renderer.category.junit.DefaultCategoryItemRendererTests::testCloning
    public void testCloning() {
        DefaultCategoryItemRenderer r1 = new DefaultCategoryItemRenderer();
        DefaultCategoryItemRenderer r2 = null;
        try {
            r2 = (DefaultCategoryItemRenderer) r1.clone();
        }
        catch (CloneNotSupportedException e) {
            System.err.println("Failed to clone.");
        }
        assertTrue(r1 != r2);
        assertTrue(r1.getClass() == r2.getClass());
        assertTrue(r1.equals(r2));
    }

// org.jfree.chart.renderer.category.junit.DefaultCategoryItemRendererTests::testSerialization
    public void testSerialization() {

        DefaultCategoryItemRenderer r1 = new DefaultCategoryItemRenderer();
        DefaultCategoryItemRenderer r2 = null;

        try {
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            ObjectOutput out = new ObjectOutputStream(buffer);
            out.writeObject(r1);
            out.close();

            ObjectInput in = new ObjectInputStream(
                new ByteArrayInputStream(buffer.toByteArray())
            );
            r2 = (DefaultCategoryItemRenderer) in.readObject();
            in.close();
        }
        catch (Exception e) {
            System.out.println(e.toString());
        }
        assertEquals(r1, r2);

    }

// org.jfree.chart.renderer.category.junit.GanttRendererTests::testEquals
    public void testEquals() {
        GanttRenderer r1 = new GanttRenderer();
        GanttRenderer r2 = new GanttRenderer();
        assertEquals(r1, r2);
        
        r1.setCompletePaint(Color.yellow);
        assertFalse(r1.equals(r2));
        r2.setCompletePaint(Color.yellow);
        assertTrue(r1.equals(r2));
        
        r1.setIncompletePaint(Color.green);
        assertFalse(r1.equals(r2));
        r2.setIncompletePaint(Color.green);
        assertTrue(r1.equals(r2));

        r1.setStartPercent(0.11);
        assertFalse(r1.equals(r2));
        r2.setStartPercent(0.11);
        assertTrue(r1.equals(r2));

        r1.setEndPercent(0.88);
        assertFalse(r1.equals(r2));
        r2.setEndPercent(0.88);
        assertTrue(r1.equals(r2));
    }

// org.jfree.chart.renderer.category.junit.GanttRendererTests::testHashcode
    public void testHashcode() {
        GanttRenderer r1 = new GanttRenderer();
        GanttRenderer r2 = new GanttRenderer();
        assertTrue(r1.equals(r2));
        int h1 = r1.hashCode();
        int h2 = r2.hashCode();
        assertEquals(h1, h2);
    }

// org.jfree.chart.renderer.category.junit.GanttRendererTests::testCloning
    public void testCloning() {
        GanttRenderer r1 = new GanttRenderer();
        GanttRenderer r2 = null;
        try {
            r2 = (GanttRenderer) r1.clone();
        }
        catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        assertTrue(r1 != r2);
        assertTrue(r1.getClass() == r2.getClass());
        assertTrue(r1.equals(r2));
    }

// org.jfree.chart.renderer.category.junit.GanttRendererTests::testSerialization
    public void testSerialization() {

        GanttRenderer r1 = new GanttRenderer();
        r1.setCompletePaint(new GradientPaint(1.0f, 2.0f, Color.red, 3.0f, 
                4.0f, Color.blue));
        r1.setIncompletePaint(new GradientPaint(4.0f, 3.0f, Color.red, 2.0f, 
                1.0f, Color.blue));
        GanttRenderer r2 = null;

        try {
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            ObjectOutput out = new ObjectOutputStream(buffer);
            out.writeObject(r1);
            out.close();

            ObjectInput in = new ObjectInputStream(
                    new ByteArrayInputStream(buffer.toByteArray()));
            r2 = (GanttRenderer) in.readObject();
            in.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        assertEquals(r1, r2);

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

// org.jfree.chart.renderer.category.junit.IntervalBarRendererTests::testEquals
    public void testEquals() {
        IntervalBarRenderer r1 = new IntervalBarRenderer();
        IntervalBarRenderer r2 = new IntervalBarRenderer();
        assertEquals(r1, r2);
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

// org.jfree.chart.renderer.category.junit.IntervalBarRendererTests::testSerialization
    public void testSerialization() {

        IntervalBarRenderer r1 = new IntervalBarRenderer();
        IntervalBarRenderer r2 = null;

        try {
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            ObjectOutput out = new ObjectOutputStream(buffer);
            out.writeObject(r1);
            out.close();

            ObjectInput in = new ObjectInputStream(
                new ByteArrayInputStream(buffer.toByteArray())
            );
            r2 = (IntervalBarRenderer) in.readObject();
            in.close();
        }
        catch (Exception e) {
            System.out.println(e.toString());
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

// org.jfree.chart.renderer.category.junit.LineRenderer3DTests::testEquals
    public void testEquals() {
        LineRenderer3D r1 = new LineRenderer3D();
        LineRenderer3D r2 = new LineRenderer3D();
        assertEquals(r1, r2);
        
        r1.setXOffset(99.9);
        assertFalse(r1.equals(r2));
        r2.setXOffset(99.9);
        assertTrue(r1.equals(r2));
        
        r1.setYOffset(111.1);
        assertFalse(r1.equals(r2));
        r2.setYOffset(111.1);
        assertTrue(r1.equals(r2));
        
        r1.setWallPaint(new GradientPaint(1.0f, 2.0f, Color.red, 3.0f, 4.0f, 
                Color.blue));
        assertFalse(r1.equals(r2));
        r2.setWallPaint(new GradientPaint(1.0f, 2.0f, Color.red, 3.0f, 4.0f, 
                Color.blue));
        assertTrue(r1.equals(r2));
    }

// org.jfree.chart.renderer.category.junit.LineRenderer3DTests::testHashcode
    public void testHashcode() {
        LineRenderer3D r1 = new LineRenderer3D();
        LineRenderer3D r2 = new LineRenderer3D();
        assertTrue(r1.equals(r2));
        int h1 = r1.hashCode();
        int h2 = r2.hashCode();
        assertEquals(h1, h2);
    }

// org.jfree.chart.renderer.category.junit.LineRenderer3DTests::testCloning
    public void testCloning() {
        LineRenderer3D r1 = new LineRenderer3D();
        LineRenderer3D r2 = null;
        try {
            r2 = (LineRenderer3D) r1.clone();
        }
        catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        assertTrue(r1 != r2);
        assertTrue(r1.getClass() == r2.getClass());
        assertTrue(r1.equals(r2));
        
        assertTrue(checkIndependence(r1, r2));
        
    }

// org.jfree.chart.renderer.category.junit.LineRenderer3DTests::testSerialization
    public void testSerialization() {
        LineRenderer3D r1 = new LineRenderer3D();
        LineRenderer3D r2 = null;
        try {
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            ObjectOutput out = new ObjectOutputStream(buffer);
            out.writeObject(r1);
            out.close();

            ObjectInput in = new ObjectInputStream(new ByteArrayInputStream(
                    buffer.toByteArray()));
            r2 = (LineRenderer3D) in.readObject();
            in.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        assertEquals(r1, r2);
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

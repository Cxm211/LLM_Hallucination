// buggy code
    public Range getDataRange(ValueAxis axis) {

        Range result = null;
        List mappedDatasets = new ArrayList();
        List includedAnnotations = new ArrayList();
        boolean isDomainAxis = true;

        // is it a domain axis?
        int domainIndex = getDomainAxisIndex(axis);
        if (domainIndex >= 0) {
            isDomainAxis = true;
            mappedDatasets.addAll(getDatasetsMappedToDomainAxis(
                    new Integer(domainIndex)));
            if (domainIndex == 0) {
                // grab the plot's annotations
                Iterator iterator = this.annotations.iterator();
                while (iterator.hasNext()) {
                    XYAnnotation annotation = (XYAnnotation) iterator.next();
                    if (annotation instanceof XYAnnotationBoundsInfo) {
                        includedAnnotations.add(annotation);
                    }
                }
            }
        }

        // or is it a range axis?
        int rangeIndex = getRangeAxisIndex(axis);
        if (rangeIndex >= 0) {
            isDomainAxis = false;
            mappedDatasets.addAll(getDatasetsMappedToRangeAxis(
                    new Integer(rangeIndex)));
            if (rangeIndex == 0) {
                Iterator iterator = this.annotations.iterator();
                while (iterator.hasNext()) {
                    XYAnnotation annotation = (XYAnnotation) iterator.next();
                    if (annotation instanceof XYAnnotationBoundsInfo) {
                        includedAnnotations.add(annotation);
                    }
                }
            }
        }

        // iterate through the datasets that map to the axis and get the union
        // of the ranges.
        Iterator iterator = mappedDatasets.iterator();
        while (iterator.hasNext()) {
            XYDataset d = (XYDataset) iterator.next();
            if (d != null) {
                XYItemRenderer r = getRendererForDataset(d);
                if (isDomainAxis) {
                    if (r != null) {
                        result = Range.combine(result, r.findDomainBounds(d));
                    }
                    else {
                        result = Range.combine(result,
                                DatasetUtilities.findDomainBounds(d));
                    }
                }
                else {
                    if (r != null) {
                        result = Range.combine(result, r.findRangeBounds(d));
                    }
                    else {
                        result = Range.combine(result,
                                DatasetUtilities.findRangeBounds(d));
                    }
                }
                
                    Collection c = r.getAnnotations();
                    Iterator i = c.iterator();
                    while (i.hasNext()) {
                        XYAnnotation a = (XYAnnotation) i.next();
                        if (a instanceof XYAnnotationBoundsInfo) {
                            includedAnnotations.add(a);
                        }
                    }
            }
        }

        Iterator it = includedAnnotations.iterator();
        while (it.hasNext()) {
            XYAnnotationBoundsInfo xyabi = (XYAnnotationBoundsInfo) it.next();
            if (xyabi.getIncludeInDataBounds()) {
                if (isDomainAxis) {
                    result = Range.combine(result, xyabi.getXRange());
                }
                else {
                    result = Range.combine(result, xyabi.getYRange());
                }
            }
        }

        return result;

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

// org.jfree.chart.annotations.junit.XYDrawableAnnotationTests::testEquals
    public void testEquals() {
        XYDrawableAnnotation a1 = new XYDrawableAnnotation(10.0, 20.0, 100.0,
                200.0, new TestDrawable());
        XYDrawableAnnotation a2 = new XYDrawableAnnotation(10.0, 20.0, 100.0,
                200.0, new TestDrawable());
        assertTrue(a1.equals(a2));

        a1 = new XYDrawableAnnotation(11.0, 20.0, 100.0, 200.0,
                new TestDrawable());
        assertFalse(a1.equals(a2));
        a2 = new XYDrawableAnnotation(11.0, 20.0, 100.0, 200.0,
                new TestDrawable());
        assertTrue(a1.equals(a2));

        a1 = new XYDrawableAnnotation(11.0, 22.0, 100.0, 200.0,
                new TestDrawable());
        assertFalse(a1.equals(a2));
        a2 = new XYDrawableAnnotation(11.0, 22.0, 100.0, 200.0,
                new TestDrawable());
        assertTrue(a1.equals(a2));

        a1 = new XYDrawableAnnotation(11.0, 22.0, 101.0, 200.0,
                new TestDrawable());
        assertFalse(a1.equals(a2));
        a2 = new XYDrawableAnnotation(11.0, 22.0, 101.0, 200.0,
                new TestDrawable());
        assertTrue(a1.equals(a2));

        a1 = new XYDrawableAnnotation(11.0, 22.0, 101.0, 202.0,
                new TestDrawable());
        assertFalse(a1.equals(a2));
        a2 = new XYDrawableAnnotation(11.0, 22.0, 101.0, 202.0,
                new TestDrawable());
        assertTrue(a1.equals(a2));

        a1 = new XYDrawableAnnotation(11.0, 22.0, 101.0, 202.0, 2.0,
                new TestDrawable());
        assertFalse(a1.equals(a2));
        a2 = new XYDrawableAnnotation(11.0, 22.0, 101.0, 202.0, 2.0,
                new TestDrawable());
        assertTrue(a1.equals(a2));
    }

// org.jfree.chart.annotations.junit.XYDrawableAnnotationTests::testHashCode
    public void testHashCode() {
        XYDrawableAnnotation a1 = new XYDrawableAnnotation(10.0, 20.0, 100.0,
                200.0, new TestDrawable());
        XYDrawableAnnotation a2 = new XYDrawableAnnotation(10.0, 20.0, 100.0,
                200.0, new TestDrawable());
        assertTrue(a1.equals(a2));
        int h1 = a1.hashCode();
        int h2 = a2.hashCode();
        assertEquals(h1, h2);
    }

// org.jfree.chart.annotations.junit.XYDrawableAnnotationTests::testCloning
    public void testCloning() {
        XYDrawableAnnotation a1 = new XYDrawableAnnotation(10.0, 20.0, 100.0,
                200.0, new TestDrawable());
        XYDrawableAnnotation a2 = null;
        try {
            a2 = (XYDrawableAnnotation) a1.clone();
        }
        catch (CloneNotSupportedException e) {
            System.err.println("Failed to clone.");
        }
        assertTrue(a1 != a2);
        assertTrue(a1.getClass() == a2.getClass());
        assertTrue(a1.equals(a2));
    }

// org.jfree.chart.annotations.junit.XYDrawableAnnotationTests::testPublicCloneable
    public void testPublicCloneable() {
        XYDrawableAnnotation a1 = new XYDrawableAnnotation(10.0, 20.0, 100.0,
                200.0, new TestDrawable());
        assertTrue(a1 instanceof PublicCloneable);
    }

// org.jfree.chart.annotations.junit.XYDrawableAnnotationTests::testSerialization
    public void testSerialization() {

        XYDrawableAnnotation a1 = new XYDrawableAnnotation(10.0, 20.0, 100.0,
                200.0, new TestDrawable());
        XYDrawableAnnotation a2 = null;

        try {
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            ObjectOutput out = new ObjectOutputStream(buffer);
            out.writeObject(a1);
            out.close();

            ObjectInput in = new ObjectInputStream(new ByteArrayInputStream(
                    buffer.toByteArray()));
            a2 = (XYDrawableAnnotation) in.readObject();
            in.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        assertEquals(a1, a2);

    }

// org.jfree.chart.annotations.junit.XYLineAnnotationTests::testEquals
    public void testEquals() {
        Stroke stroke = new BasicStroke(2.0f);
        XYLineAnnotation a1 = new XYLineAnnotation(10.0, 20.0, 100.0, 200.0,
                stroke, Color.blue);
        XYLineAnnotation a2 = new XYLineAnnotation(10.0, 20.0, 100.0, 200.0,
                stroke, Color.blue);
        assertTrue(a1.equals(a2));
        assertTrue(a2.equals(a1));

        a1 = new XYLineAnnotation(11.0, 20.0, 100.0, 200.0, stroke, Color.blue);
        assertFalse(a1.equals(a2));
        a2 = new XYLineAnnotation(11.0, 20.0, 100.0, 200.0, stroke, Color.blue);
        assertTrue(a1.equals(a2));

        a1 = new XYLineAnnotation(11.0, 21.0, 100.0, 200.0, stroke, Color.blue);
        assertFalse(a1.equals(a2));
        a2 = new XYLineAnnotation(11.0, 21.0, 100.0, 200.0, stroke, Color.blue);
        assertTrue(a1.equals(a2));

        a1 = new XYLineAnnotation(11.0, 21.0, 101.0, 200.0, stroke, Color.blue);
        assertFalse(a1.equals(a2));
        a2 = new XYLineAnnotation(11.0, 21.0, 101.0, 200.0, stroke, Color.blue);
        assertTrue(a1.equals(a2));

        a1 = new XYLineAnnotation(11.0, 21.0, 101.0, 201.0, stroke, Color.blue);
        assertFalse(a1.equals(a2));
        a2 = new XYLineAnnotation(11.0, 21.0, 101.0, 201.0, stroke, Color.blue);
        assertTrue(a1.equals(a2));

        Stroke stroke2 = new BasicStroke(0.99f);
        a1 = new XYLineAnnotation(11.0, 21.0, 101.0, 200.0, stroke2, Color.blue);
        assertFalse(a1.equals(a2));
        a2 = new XYLineAnnotation(11.0, 21.0, 101.0, 200.0, stroke2, Color.blue);
        assertTrue(a1.equals(a2));

        GradientPaint g1 = new GradientPaint(1.0f, 2.0f, Color.red,
                3.0f, 4.0f, Color.white);
        GradientPaint g2 = new GradientPaint(1.0f, 2.0f, Color.red,
                3.0f, 4.0f, Color.white);
        a1 = new XYLineAnnotation(11.0, 21.0, 101.0, 200.0, stroke2, g1);
        assertFalse(a1.equals(a2));
        a2 = new XYLineAnnotation(11.0, 21.0, 101.0, 200.0, stroke2, g2);
        assertTrue(a1.equals(a2));
    }

// org.jfree.chart.annotations.junit.XYLineAnnotationTests::testHashCode
    public void testHashCode() {
        Stroke stroke = new BasicStroke(2.0f);
        XYLineAnnotation a1 = new XYLineAnnotation(10.0, 20.0, 100.0, 200.0,
                stroke, Color.blue);
        XYLineAnnotation a2 = new XYLineAnnotation(10.0, 20.0, 100.0, 200.0,
                stroke, Color.blue);
        assertTrue(a1.equals(a2));
        int h1 = a1.hashCode();
        int h2 = a2.hashCode();
        assertEquals(h1, h2);
    }

// org.jfree.chart.annotations.junit.XYLineAnnotationTests::testCloning
    public void testCloning() {
        Stroke stroke = new BasicStroke(2.0f);
        XYLineAnnotation a1 = new XYLineAnnotation(10.0, 20.0, 100.0, 200.0,
                stroke, Color.blue);
        XYLineAnnotation a2 = null;
        try {
            a2 = (XYLineAnnotation) a1.clone();
        }
        catch (CloneNotSupportedException e) {
            System.err.println("Failed to clone.");
        }
        assertTrue(a1 != a2);
        assertTrue(a1.getClass() == a2.getClass());
        assertTrue(a1.equals(a2));
    }

// org.jfree.chart.annotations.junit.XYLineAnnotationTests::testPublicCloneable
    public void testPublicCloneable() {
        Stroke stroke = new BasicStroke(2.0f);
        XYLineAnnotation a1 = new XYLineAnnotation(10.0, 20.0, 100.0, 200.0,
                stroke, Color.blue);
        assertTrue(a1 instanceof PublicCloneable);
    }

// org.jfree.chart.annotations.junit.XYLineAnnotationTests::testSerialization
    public void testSerialization() {

        Stroke stroke = new BasicStroke(2.0f);
        XYLineAnnotation a1 = new XYLineAnnotation(10.0, 20.0, 100.0, 200.0,
                stroke, Color.blue);
        XYLineAnnotation a2 = null;

        try {
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            ObjectOutput out = new ObjectOutputStream(buffer);
            out.writeObject(a1);
            out.close();

            ObjectInput in = new ObjectInputStream(new ByteArrayInputStream(
                    buffer.toByteArray()));
            a2 = (XYLineAnnotation) in.readObject();
            in.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        assertEquals(a1, a2);

    }

// org.jfree.chart.annotations.junit.XYPointerAnnotationTests::testEquals
    public void testEquals() {

        XYPointerAnnotation a1 = new XYPointerAnnotation("Label", 10.0, 20.0,
                Math.PI);
        XYPointerAnnotation a2 = new XYPointerAnnotation("Label", 10.0, 20.0,
                Math.PI);
        assertTrue(a1.equals(a2));

        a1 = new XYPointerAnnotation("Label2", 10.0, 20.0, Math.PI);
        assertFalse(a1.equals(a2));
        a2 = new XYPointerAnnotation("Label2", 10.0, 20.0, Math.PI);
        assertTrue(a1.equals(a2));

        a1.setX(11.0);
        assertFalse(a1.equals(a2));
        a2.setX(11.0);
        assertTrue(a1.equals(a2));

        a1.setY(22.0);
        assertFalse(a1.equals(a2));
        a2.setY(22.0);
        assertTrue(a1.equals(a2));

        
        a1.setAngle(Math.PI / 4.0);
        assertFalse(a1.equals(a2));
        a2.setAngle(Math.PI / 4.0);
        assertTrue(a1.equals(a2));

        
        a1.setTipRadius(20.0);
        assertFalse(a1.equals(a2));
        a2.setTipRadius(20.0);
        assertTrue(a1.equals(a2));

        
        a1.setBaseRadius(5.0);
        assertFalse(a1.equals(a2));
        a2.setBaseRadius(5.0);
        assertTrue(a1.equals(a2));

        
        a1.setArrowLength(33.0);
        assertFalse(a1.equals(a2));
        a2.setArrowLength(33.0);
        assertTrue(a1.equals(a2));

        
        a1.setArrowWidth(9.0);
        assertFalse(a1.equals(a2));
        a2.setArrowWidth(9.0);
        assertTrue(a1.equals(a2));

        
        Stroke stroke = new BasicStroke(1.5f);
        a1.setArrowStroke(stroke);
        assertFalse(a1.equals(a2));
        a2.setArrowStroke(stroke);
        assertTrue(a1.equals(a2));

        
        a1.setArrowPaint(Color.blue);
        assertFalse(a1.equals(a2));
        a2.setArrowPaint(Color.blue);
        assertTrue(a1.equals(a2));

        
        a1.setLabelOffset(10.0);
        assertFalse(a1.equals(a2));
        a2.setLabelOffset(10.0);
        assertTrue(a1.equals(a2));

    }

// org.jfree.chart.annotations.junit.XYPointerAnnotationTests::testHashCode
    public void testHashCode() {
        XYPointerAnnotation a1 = new XYPointerAnnotation("Label", 10.0, 20.0,
                Math.PI);
        XYPointerAnnotation a2 = new XYPointerAnnotation("Label", 10.0, 20.0,
                Math.PI);
        assertTrue(a1.equals(a2));
        int h1 = a1.hashCode();
        int h2 = a2.hashCode();
        assertEquals(h1, h2);
    }

// org.jfree.chart.annotations.junit.XYPointerAnnotationTests::testCloning
    public void testCloning() {
        XYPointerAnnotation a1 = new XYPointerAnnotation("Label", 10.0, 20.0,
                Math.PI);
        XYPointerAnnotation a2 = null;
        try {
            a2 = (XYPointerAnnotation) a1.clone();
        }
        catch (CloneNotSupportedException e) {
            System.err.println("Failed to clone.");
        }
        assertTrue(a1 != a2);
        assertTrue(a1.getClass() == a2.getClass());
        assertTrue(a1.equals(a2));
    }

// org.jfree.chart.annotations.junit.XYPointerAnnotationTests::testPublicCloneable
    public void testPublicCloneable() {
        XYPointerAnnotation a1 = new XYPointerAnnotation("Label", 10.0, 20.0,
                Math.PI);
        assertTrue(a1 instanceof PublicCloneable);
    }

// org.jfree.chart.annotations.junit.XYPointerAnnotationTests::testSerialization
    public void testSerialization() {

        XYPointerAnnotation a1 = new XYPointerAnnotation("Label", 10.0, 20.0,
                Math.PI);
        XYPointerAnnotation a2 = null;

        try {
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            ObjectOutput out = new ObjectOutputStream(buffer);
            out.writeObject(a1);
            out.close();

            ObjectInput in = new ObjectInputStream(new ByteArrayInputStream(
                    buffer.toByteArray()));
            a2 = (XYPointerAnnotation) in.readObject();
            in.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        assertEquals(a1, a2);

    }

// org.jfree.chart.annotations.junit.XYPolygonAnnotationTests::testEquals
    public void testEquals() {
        Stroke stroke1 = new BasicStroke(2.0f);
        Stroke stroke2 = new BasicStroke(2.5f);
        XYPolygonAnnotation a1 = new XYPolygonAnnotation(new double[] {1.0,
                2.0, 3.0, 4.0, 5.0, 6.0}, stroke1, Color.red, Color.blue);
        XYPolygonAnnotation a2 = new XYPolygonAnnotation(new double[] {1.0,
                2.0, 3.0, 4.0, 5.0, 6.0}, stroke1, Color.red, Color.blue);
        assertTrue(a1.equals(a2));
        assertTrue(a2.equals(a1));

        a1 = new XYPolygonAnnotation(new double[] {99.0, 2.0, 3.0, 4.0, 5.0,
                6.0}, stroke1, Color.red, Color.blue);
        assertFalse(a1.equals(a2));
        a2 = new XYPolygonAnnotation(new double[] {99.0, 2.0, 3.0, 4.0, 5.0,
                6.0}, stroke1, Color.red, Color.blue);
        assertTrue(a1.equals(a2));

        a1 = new XYPolygonAnnotation(new double[] {99.0, 2.0, 3.0, 4.0, 5.0,
                6.0}, stroke2, Color.red, Color.blue);
        assertFalse(a1.equals(a2));
        a2 = new XYPolygonAnnotation(new double[] {99.0, 2.0, 3.0, 4.0, 5.0,
                6.0}, stroke2, Color.red, Color.blue);
        assertTrue(a1.equals(a2));

        GradientPaint gp1 = new GradientPaint(1.0f, 2.0f, Color.yellow, 3.0f,
                4.0f, Color.white);
        GradientPaint gp2 = new GradientPaint(1.0f, 2.0f, Color.yellow, 3.0f,
                4.0f, Color.white);
        a1 = new XYPolygonAnnotation(new double[] {99.0, 2.0, 3.0, 4.0, 5.0,
                6.0}, stroke2, gp1, Color.blue);
        assertFalse(a1.equals(a2));
        a2 = new XYPolygonAnnotation(new double[] {99.0, 2.0, 3.0, 4.0, 5.0,
                6.0}, stroke2, gp2, Color.blue);
        assertTrue(a1.equals(a2));

        GradientPaint gp3 = new GradientPaint(1.0f, 2.0f, Color.green, 3.0f,
                4.0f, Color.white);
        GradientPaint gp4 = new GradientPaint(1.0f, 2.0f, Color.green, 3.0f,
                4.0f, Color.white);
        a1 = new XYPolygonAnnotation(new double[] {99.0, 2.0, 3.0, 4.0, 5.0,
                6.0}, stroke2, gp1, gp3);
        assertFalse(a1.equals(a2));
        a2 = new XYPolygonAnnotation(new double[] {99.0, 2.0, 3.0, 4.0, 5.0,
                6.0}, stroke2, gp2, gp4);
        assertTrue(a1.equals(a2));
    }

// org.jfree.chart.annotations.junit.XYPolygonAnnotationTests::testHashCode
    public void testHashCode() {
        Stroke stroke = new BasicStroke(2.0f);
        XYPolygonAnnotation a1 = new XYPolygonAnnotation(new double[] {1.0,
                2.0, 3.0, 4.0, 5.0, 6.0}, stroke, Color.red, Color.blue);
        XYPolygonAnnotation a2 = new XYPolygonAnnotation(new double[] {1.0,
                2.0, 3.0, 4.0, 5.0, 6.0}, stroke, Color.red, Color.blue);
        assertTrue(a1.equals(a2));
        int h1 = a1.hashCode();
        int h2 = a2.hashCode();
        assertEquals(h1, h2);
    }

// org.jfree.chart.annotations.junit.XYPolygonAnnotationTests::testCloning
    public void testCloning() {
        Stroke stroke1 = new BasicStroke(2.0f);
        XYPolygonAnnotation a1 = new XYPolygonAnnotation(new double[] {1.0,
                2.0, 3.0, 4.0, 5.0, 6.0}, stroke1, Color.red, Color.blue);
        XYPolygonAnnotation a2 = null;
        try {
            a2 = (XYPolygonAnnotation) a1.clone();
        }
        catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        assertTrue(a1 != a2);
        assertTrue(a1.getClass() == a2.getClass());
        assertTrue(a1.equals(a2));
    }

// org.jfree.chart.annotations.junit.XYPolygonAnnotationTests::testPublicCloneable
    public void testPublicCloneable() {
        Stroke stroke1 = new BasicStroke(2.0f);
        XYPolygonAnnotation a1 = new XYPolygonAnnotation(new double[] {1.0,
                2.0, 3.0, 4.0, 5.0, 6.0}, stroke1, Color.red, Color.blue);
        assertTrue(a1 instanceof PublicCloneable);
    }

// org.jfree.chart.annotations.junit.XYPolygonAnnotationTests::testSerialization
    public void testSerialization() {

        Stroke stroke1 = new BasicStroke(2.0f);
        XYPolygonAnnotation a1 = new XYPolygonAnnotation(new double[] {1.0,
                2.0, 3.0, 4.0, 5.0, 6.0}, stroke1, Color.red, Color.blue);
        XYPolygonAnnotation a2 = null;

        try {
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            ObjectOutput out = new ObjectOutputStream(buffer);
            out.writeObject(a1);
            out.close();

            ObjectInput in = new ObjectInputStream(
                new ByteArrayInputStream(buffer.toByteArray())
            );
            a2 = (XYPolygonAnnotation) in.readObject();
            in.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        assertEquals(a1, a2);

    }

// org.jfree.chart.annotations.junit.XYShapeAnnotationTests::testEquals
    public void testEquals() {

        XYShapeAnnotation a1 = new XYShapeAnnotation(
                new Rectangle2D.Double(1.0, 2.0, 3.0, 4.0),
                new BasicStroke(1.2f), Color.red, Color.blue);
        XYShapeAnnotation a2 = new XYShapeAnnotation(
                new Rectangle2D.Double(1.0, 2.0, 3.0, 4.0),
                new BasicStroke(1.2f), Color.red, Color.blue);
        assertTrue(a1.equals(a2));
        assertTrue(a2.equals(a1));

        
        a1 = new XYShapeAnnotation(
                new Rectangle2D.Double(4.0, 3.0, 2.0, 1.0),
                new BasicStroke(1.2f), Color.red, Color.blue);
        assertFalse(a1.equals(a2));
        a2 = new XYShapeAnnotation(
                new Rectangle2D.Double(4.0, 3.0, 2.0, 1.0),
                new BasicStroke(1.2f), Color.red, Color.blue);
        assertTrue(a1.equals(a2));

        
        a1 = new XYShapeAnnotation(
                new Rectangle2D.Double(4.0, 3.0, 2.0, 1.0),
                new BasicStroke(2.3f), Color.red, Color.blue);
        assertFalse(a1.equals(a2));
        a2 = new XYShapeAnnotation(
                new Rectangle2D.Double(4.0, 3.0, 2.0, 1.0),
                new BasicStroke(2.3f), Color.red, Color.blue);
        assertTrue(a1.equals(a2));

        GradientPaint gp1a = new GradientPaint(1.0f, 2.0f, Color.blue,
                3.0f, 4.0f, Color.red);
        GradientPaint gp1b = new GradientPaint(1.0f, 2.0f, Color.blue,
                3.0f, 4.0f, Color.red);
        GradientPaint gp2a = new GradientPaint(5.0f, 6.0f, Color.pink,
                7.0f, 8.0f, Color.white);
        GradientPaint gp2b = new GradientPaint(5.0f, 6.0f, Color.pink,
                7.0f, 8.0f, Color.white);

        
        a1 = new XYShapeAnnotation(
                new Rectangle2D.Double(4.0, 3.0, 2.0, 1.0),
                new BasicStroke(2.3f), gp1a, Color.blue);
        assertFalse(a1.equals(a2));
        a2 = new XYShapeAnnotation(
                new Rectangle2D.Double(4.0, 3.0, 2.0, 1.0),
                new BasicStroke(2.3f), gp1b, Color.blue);
        assertTrue(a1.equals(a2));

        
        a1 = new XYShapeAnnotation(
                new Rectangle2D.Double(4.0, 3.0, 2.0, 1.0),
                new BasicStroke(2.3f), gp1a, gp2a);
        assertFalse(a1.equals(a2));
        a2 = new XYShapeAnnotation(
                new Rectangle2D.Double(4.0, 3.0, 2.0, 1.0),
                new BasicStroke(2.3f), gp1b, gp2b);
        assertTrue(a1.equals(a2));
    }

// org.jfree.chart.annotations.junit.XYShapeAnnotationTests::testHashCode
    public void testHashCode() {
        XYShapeAnnotation a1 = new XYShapeAnnotation(
                new Rectangle2D.Double(1.0, 2.0, 3.0, 4.0),
                new BasicStroke(1.2f), Color.red, Color.blue);
        XYShapeAnnotation a2 = new XYShapeAnnotation(
                new Rectangle2D.Double(1.0, 2.0, 3.0, 4.0),
                new BasicStroke(1.2f), Color.red, Color.blue);
        assertTrue(a1.equals(a2));
        int h1 = a1.hashCode();
        int h2 = a2.hashCode();
        assertEquals(h1, h2);
    }

// org.jfree.chart.annotations.junit.XYShapeAnnotationTests::testCloning
    public void testCloning() {
        XYShapeAnnotation a1 = new XYShapeAnnotation(
                new Rectangle2D.Double(1.0, 2.0, 3.0, 4.0),
                new BasicStroke(1.2f), Color.red, Color.blue);
        XYShapeAnnotation a2 = null;
        try {
            a2 = (XYShapeAnnotation) a1.clone();
        }
        catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        assertTrue(a1 != a2);
        assertTrue(a1.getClass() == a2.getClass());
        assertTrue(a1.equals(a2));
    }

// org.jfree.chart.annotations.junit.XYShapeAnnotationTests::testPublicCloneable
    public void testPublicCloneable() {
        XYShapeAnnotation a1 = new XYShapeAnnotation(
                new Rectangle2D.Double(1.0, 2.0, 3.0, 4.0),
                new BasicStroke(1.2f), Color.red, Color.blue);
        assertTrue(a1 instanceof PublicCloneable);
    }

// org.jfree.chart.annotations.junit.XYShapeAnnotationTests::testSerialization
    public void testSerialization() {
        XYShapeAnnotation a1 = new XYShapeAnnotation(
                new Rectangle2D.Double(1.0, 2.0, 3.0, 4.0),
                new BasicStroke(1.2f), Color.red, Color.blue);
        XYShapeAnnotation a2 = null;

        try {
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            ObjectOutput out = new ObjectOutputStream(buffer);
            out.writeObject(a1);
            out.close();

            ObjectInput in = new ObjectInputStream(
                    new ByteArrayInputStream(buffer.toByteArray()));
            a2 = (XYShapeAnnotation) in.readObject();
            in.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        assertEquals(a1, a2);
    }

// org.jfree.chart.annotations.junit.XYTextAnnotationTests::testEquals
    public void testEquals() {
        XYTextAnnotation a1 = new XYTextAnnotation("Text", 10.0, 20.0);
        XYTextAnnotation a2 = new XYTextAnnotation("Text", 10.0, 20.0);
        assertTrue(a1.equals(a2));

        
        a1 = new XYTextAnnotation("ABC", 10.0, 20.0);
        assertFalse(a1.equals(a2));
        a2 = new XYTextAnnotation("ABC", 10.0, 20.0);
        assertTrue(a1.equals(a2));

        
        a1 = new XYTextAnnotation("ABC", 11.0, 20.0);
        assertFalse(a1.equals(a2));
        a2 = new XYTextAnnotation("ABC", 11.0, 20.0);
        assertTrue(a1.equals(a2));

        
        a1 = new XYTextAnnotation("ABC", 11.0, 22.0);
        assertFalse(a1.equals(a2));
        a2 = new XYTextAnnotation("ABC", 11.0, 22.0);
        assertTrue(a1.equals(a2));

        
        a1.setFont(new Font("Serif", Font.PLAIN, 23));
        assertFalse(a1.equals(a2));
        a2.setFont(new Font("Serif", Font.PLAIN, 23));
        assertTrue(a1.equals(a2));

        
        GradientPaint gp1 = new GradientPaint(1.0f, 2.0f, Color.red, 3.0f,
                4.0f, Color.yellow);
        GradientPaint gp2 = new GradientPaint(1.0f, 2.0f, Color.red, 3.0f,
                4.0f, Color.yellow);
        a1.setPaint(gp1);
        assertFalse(a1.equals(a2));
        a2.setPaint(gp2);
        assertTrue(a1.equals(a2));

        
        a1.setRotationAnchor(TextAnchor.BASELINE_RIGHT);
        assertFalse(a1.equals(a2));
        a2.setRotationAnchor(TextAnchor.BASELINE_RIGHT);
        assertTrue(a1.equals(a2));

        
        a1.setRotationAngle(12.3);
        assertFalse(a1.equals(a2));
        a2.setRotationAngle(12.3);
        assertTrue(a1.equals(a2));

        
        a1.setTextAnchor(TextAnchor.BASELINE_RIGHT);
        assertFalse(a1.equals(a2));
        a2.setTextAnchor(TextAnchor.BASELINE_RIGHT);
        assertTrue(a1.equals(a2));

        a1.setBackgroundPaint(gp1);
        assertFalse(a1.equals(a2));
        a2.setBackgroundPaint(gp1);
        assertTrue(a1.equals(a2));

        a1.setOutlinePaint(gp1);
        assertFalse(a1.equals(a2));
        a2.setOutlinePaint(gp1);
        assertTrue(a1.equals(a2));

        a1.setOutlineStroke(new BasicStroke(1.2f));
        assertFalse(a1.equals(a2));
        a2.setOutlineStroke(new BasicStroke(1.2f));
        assertTrue(a1.equals(a2));

        a1.setOutlineVisible(!a1.isOutlineVisible());
        assertFalse(a1.equals(a2));
        a2.setOutlineVisible(a1.isOutlineVisible());
        assertTrue(a1.equals(a2));

    }

// org.jfree.chart.annotations.junit.XYTextAnnotationTests::testHashCode
    public void testHashCode() {
        XYTextAnnotation a1 = new XYTextAnnotation("Text", 10.0, 20.0);
        XYTextAnnotation a2 = new XYTextAnnotation("Text", 10.0, 20.0);
        assertTrue(a1.equals(a2));
        int h1 = a1.hashCode();
        int h2 = a2.hashCode();
        assertEquals(h1, h2);
    }

// org.jfree.chart.annotations.junit.XYTextAnnotationTests::testCloning
    public void testCloning() {
        XYTextAnnotation a1 = new XYTextAnnotation("Text", 10.0, 20.0);
        XYTextAnnotation a2 = null;
        try {
            a2 = (XYTextAnnotation) a1.clone();
        }
        catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        assertTrue(a1 != a2);
        assertTrue(a1.getClass() == a2.getClass());
        assertTrue(a1.equals(a2));
    }

// org.jfree.chart.annotations.junit.XYTextAnnotationTests::testPublicCloneable
    public void testPublicCloneable() {
        XYTextAnnotation a1 = new XYTextAnnotation("Text", 10.0, 20.0);
        assertTrue(a1 instanceof PublicCloneable);
    }

// org.jfree.chart.annotations.junit.XYTextAnnotationTests::testSerialization
    public void testSerialization() {
        XYTextAnnotation a1 = new XYTextAnnotation("Text", 10.0, 20.0);
        a1.setOutlinePaint(new GradientPaint(1.0f, 2.0f, Color.red, 3.0f, 4.0f,
                Color.blue));
        XYTextAnnotation a2 = null;

        try {
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            ObjectOutput out = new ObjectOutputStream(buffer);
            out.writeObject(a1);
            out.close();

            ObjectInput in = new ObjectInputStream(new ByteArrayInputStream(
                    buffer.toByteArray()));
            a2 = (XYTextAnnotation) in.readObject();
            in.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        assertEquals(a1, a2);
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
                "Category", "Value", null, PlotOrientation.VERTICAL,
                true, false, false);
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
                "Y", dataset, PlotOrientation.VERTICAL, false, false, false);
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
                "Y", dataset, PlotOrientation.VERTICAL, false, false, false);
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
                "Y", dataset, PlotOrientation.VERTICAL, false, false, false);
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
                "Y", dataset, PlotOrientation.VERTICAL, false, false, false);
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
                "Y", dataset, PlotOrientation.VERTICAL, false, false, false);
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
                "Y", dataset, PlotOrientation.VERTICAL, false, false, false);
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
                "Y", dataset, PlotOrientation.VERTICAL, false, false, false);
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
                "Y", dataset, PlotOrientation.VERTICAL, false, false, false);
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
                "Y", dataset, PlotOrientation.VERTICAL, false, false, false);
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
                "Y", dataset, PlotOrientation.VERTICAL, false, false, false);
        XYPlot plot = (XYPlot) chart.getPlot();
        plot.setRangeAxis(1, new NumberAxis("X2"));
        ChartPanel panel = new ChartPanel(chart);
        chart.addChangeListener(this);
        this.chartChangeEvents.clear();
        panel.restoreAutoRangeBounds();
        assertEquals(1, this.chartChangeEvents.size());
    }

// org.jfree.chart.junit.ChartPanelTests::testSetMouseWheelEnabled
    public void testSetMouseWheelEnabled() {}

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

// org.jfree.chart.junit.StandardChartThemeTests::testEquals
    public void testEquals() {

        StandardChartTheme t1 = new StandardChartTheme("Name");
        StandardChartTheme t2 = new StandardChartTheme("Name");
        assertTrue(t1.equals(t2));

        
        t1 = new StandardChartTheme("t1");
        assertFalse(t1.equals(t2));
        t2 = new StandardChartTheme("t1");
        assertTrue(t1.equals(t2));

        
        t1.setExtraLargeFont(new Font("Dialog", Font.PLAIN, 21));
        assertFalse(t1.equals(t2));
        t2.setExtraLargeFont(new Font("Dialog", Font.PLAIN, 21));
        assertTrue(t1.equals(t2));

        
        t1.setLargeFont(new Font("Dialog", Font.PLAIN, 19));
        assertFalse(t1.equals(t2));
        t2.setLargeFont(new Font("Dialog", Font.PLAIN, 19));
        assertTrue(t1.equals(t2));

        
        t1.setRegularFont(new Font("Dialog", Font.PLAIN, 17));
        assertFalse(t1.equals(t2));
        t2.setRegularFont(new Font("Dialog", Font.PLAIN, 17));
        assertTrue(t1.equals(t2));

        
        t1.setTitlePaint(new GradientPaint(0f, 1f, Color.red, 2f, 3f, Color.blue));
        assertFalse(t1.equals(t2));
        t2.setTitlePaint(new GradientPaint(0f, 1f, Color.red, 2f, 3f, Color.blue));
        assertTrue(t1.equals(t2));

        
        t1.setSubtitlePaint(new GradientPaint(1f, 2f, Color.red, 3f, 4f, Color.blue));
        assertFalse(t1.equals(t2));
        t2.setSubtitlePaint(new GradientPaint(1f, 2f, Color.red, 3f, 4f, Color.blue));
        assertTrue(t1.equals(t2));

        
        t1.setChartBackgroundPaint(new GradientPaint(2f, 3f, Color.blue, 4f, 5f, Color.red));
        assertFalse(t1.equals(t2));
        t2.setChartBackgroundPaint(new GradientPaint(2f, 3f, Color.blue, 4f, 5f, Color.red));
        assertTrue(t1.equals(t2));

        
        t1.setLegendBackgroundPaint(new GradientPaint(3f, 4f, Color.gray, 1f, 2f, Color.red));
        assertFalse(t1.equals(t2));
        t2.setLegendBackgroundPaint(new GradientPaint(3f, 4f, Color.gray, 1f, 2f, Color.red));
        assertTrue(t1.equals(t2));

        
        t1.setLegendItemPaint(new GradientPaint(9f, 8f, Color.red, 7f, 6f, Color.blue));
        assertFalse(t1.equals(t2));
        t2.setLegendItemPaint(new GradientPaint(9f, 8f, Color.red, 7f, 6f, Color.blue));
        assertTrue(t1.equals(t2));

        
        t1.setDrawingSupplier(new DefaultDrawingSupplier(
                new Paint[] {Color.red},
                new Paint[] {Color.blue},
                new Stroke[] {new BasicStroke(1.0f)},
                new Stroke[] {new BasicStroke(1.0f)},
                new Shape[] {new Rectangle2D.Double(1.0, 2.0, 3.0, 4.0)}));
        assertFalse(t1.equals(t2));
        t2.setDrawingSupplier(new DefaultDrawingSupplier(
                new Paint[] {Color.red},
                new Paint[] {Color.blue},
                new Stroke[] {new BasicStroke(1.0f)},
                new Stroke[] {new BasicStroke(1.0f)},
                new Shape[] {new Rectangle2D.Double(1.0, 2.0, 3.0, 4.0)}));
        assertTrue(t1.equals(t2));

        
        t1.setPlotBackgroundPaint(new GradientPaint(4f, 3f, Color.red, 6f, 7f, Color.blue));
        assertFalse(t1.equals(t2));
        t2.setPlotBackgroundPaint(new GradientPaint(4f, 3f, Color.red, 6f, 7f, Color.blue));
        assertTrue(t1.equals(t2));

        
        t1.setPlotOutlinePaint(new GradientPaint(5f, 2f, Color.blue, 6f, 7f, Color.red));
        assertFalse(t1.equals(t2));
        t2.setPlotOutlinePaint(new GradientPaint(5f, 2f, Color.blue, 6f, 7f, Color.red));
        assertTrue(t1.equals(t2));

        
        t1.setLabelLinkStyle(PieLabelLinkStyle.STANDARD);
        assertFalse(t1.equals(t2));
        t2.setLabelLinkStyle(PieLabelLinkStyle.STANDARD);
        assertTrue(t1.equals(t2));

        
        t1.setLabelLinkPaint(new GradientPaint(4f, 3f, Color.red, 2f, 9f, Color.blue));
        assertFalse(t1.equals(t2));
        t2.setLabelLinkPaint(new GradientPaint(4f, 3f, Color.red, 2f, 9f, Color.blue));
        assertTrue(t1.equals(t2));

        
        t1.setDomainGridlinePaint(Color.blue);
        assertFalse(t1.equals(t2));
        t2.setDomainGridlinePaint(Color.blue);
        assertTrue(t1.equals(t2));

        
        t1.setRangeGridlinePaint(Color.red);
        assertFalse(t1.equals(t2));
        t2.setRangeGridlinePaint(Color.red);
        assertTrue(t1.equals(t2));

        
        t1.setAxisOffset(new RectangleInsets(1, 2, 3, 4));
        assertFalse(t1.equals(t2));
        t2.setAxisOffset(new RectangleInsets(1, 2, 3, 4));
        assertTrue(t1.equals(t2));

        
        t1.setAxisLabelPaint(new GradientPaint(8f, 4f, Color.gray, 2f, 9f, Color.blue));
        assertFalse(t1.equals(t2));
        t2.setAxisLabelPaint(new GradientPaint(8f, 4f, Color.gray, 2f, 9f, Color.blue));
        assertTrue(t1.equals(t2));

        
        t1.setTickLabelPaint(new GradientPaint(3f, 4f, Color.red, 5f, 6f, Color.yellow));
        assertFalse(t1.equals(t2));
        t2.setTickLabelPaint(new GradientPaint(3f, 4f, Color.red, 5f, 6f, Color.yellow));
        assertTrue(t1.equals(t2));

        
        t1.setItemLabelPaint(new GradientPaint(2f, 5f, Color.gray, 1f, 2f, Color.blue));
        assertFalse(t1.equals(t2));
        t2.setItemLabelPaint(new GradientPaint(2f, 5f, Color.gray, 1f, 2f, Color.blue));
        assertTrue(t1.equals(t2));

        
        t1.setShadowVisible(!t1.isShadowVisible());
        assertFalse(t1.equals(t2));
        t2.setShadowVisible(t1.isShadowVisible());
        assertTrue(t1.equals(t2));

        
        t1.setShadowPaint(new GradientPaint(7f, 1f, Color.blue, 4f, 6f, Color.red));
        assertFalse(t1.equals(t2));
        t2.setShadowPaint(new GradientPaint(7f, 1f, Color.blue, 4f, 6f, Color.red));
        assertTrue(t1.equals(t2));

        
        t1.setBarPainter(new StandardBarPainter());
        assertFalse(t1.equals(t2));
        t2.setBarPainter(new StandardBarPainter());
        assertTrue(t1.equals(t2));

        
        t1.setXYBarPainter(new StandardXYBarPainter());
        assertFalse(t1.equals(t2));
        t2.setXYBarPainter(new StandardXYBarPainter());
        assertTrue(t1.equals(t2));

        
        t1.setThermometerPaint(new GradientPaint(9f, 7f, Color.red, 5f, 1f, Color.blue));
        assertFalse(t1.equals(t2));
        t2.setThermometerPaint(new GradientPaint(9f, 7f, Color.red, 5f, 1f, Color.blue));
        assertTrue(t1.equals(t2));

        
        t1.setWallPaint(new GradientPaint(4f, 5f, Color.red, 1f, 0f, Color.gray));
        assertFalse(t1.equals(t2));
        t2.setWallPaint(new GradientPaint(4f, 5f, Color.red, 1f, 0f, Color.gray));
        assertTrue(t1.equals(t2));

        
        t1.setErrorIndicatorPaint(new GradientPaint(0f, 1f, Color.white, 2f, 3f, Color.blue));
        assertFalse(t1.equals(t2));
        t2.setErrorIndicatorPaint(new GradientPaint(0f, 1f, Color.white, 2f, 3f, Color.blue));
        assertTrue(t1.equals(t2));

        
        t1.setGridBandPaint(new GradientPaint(1f, 2f, Color.white, 4f, 8f, Color.red));
        assertFalse(t1.equals(t2));
        t2.setGridBandPaint(new GradientPaint(1f, 2f, Color.white, 4f, 8f, Color.red));
        assertTrue(t1.equals(t2));

        
        t1.setGridBandAlternatePaint(new GradientPaint(1f, 4f, Color.green, 1f, 2f, Color.red));
        assertFalse(t1.equals(t2));
        t2.setGridBandAlternatePaint(new GradientPaint(1f, 4f, Color.green, 1f, 2f, Color.red));
        assertTrue(t1.equals(t2));

    }

// org.jfree.chart.junit.StandardChartThemeTests::testSerialization
    public void testSerialization() {
        StandardChartTheme t1 = new StandardChartTheme("Name");
        StandardChartTheme t2 = null;
        try {
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            ObjectOutput out = new ObjectOutputStream(buffer);
            out.writeObject(t1);
            out.close();

            ObjectInput in = new ObjectInputStream(
                    new ByteArrayInputStream(buffer.toByteArray()));
            t2 = (StandardChartTheme) in.readObject();
            in.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        assertTrue(t1.equals(t2));
    }

// org.jfree.chart.junit.StandardChartThemeTests::testCloning
    public void testCloning() {
        StandardChartTheme t1 = new StandardChartTheme("Name");
        StandardChartTheme t2 = null;
        try {
            t2 = (StandardChartTheme) t1.clone();
        }
        catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        assertTrue(t1 != t2);
        assertTrue(t1.getClass() == t2.getClass());
        assertTrue(t1.equals(t2));
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

// org.jfree.chart.plot.junit.MarkerTests::testGetSetPaint
    public void testGetSetPaint() {
        
        
        ValueMarker m = new ValueMarker(1.1);
        m.addChangeListener(this);
        this.lastEvent = null;
        assertEquals(Color.gray, m.getPaint());
        m.setPaint(Color.blue);
        assertEquals(Color.blue, m.getPaint());
        assertEquals(m, this.lastEvent.getMarker());

        
        try {
            m.setPaint(null);
            fail("Expected an IllegalArgumentException for null.");
        }
        catch (IllegalArgumentException e) {
            assertTrue(true);
        }
    }

// org.jfree.chart.plot.junit.MarkerTests::testGetSetStroke
    public void testGetSetStroke() {
        
        
        ValueMarker m = new ValueMarker(1.1);
        m.addChangeListener(this);
        this.lastEvent = null;
        assertEquals(new BasicStroke(0.5f), m.getStroke());
        m.setStroke(new BasicStroke(1.1f));
        assertEquals(new BasicStroke(1.1f), m.getStroke());
        assertEquals(m, this.lastEvent.getMarker());

        
        try {
            m.setStroke(null);
            fail("Expected an IllegalArgumentException for null.");
        }
        catch (IllegalArgumentException e) {
            assertTrue(true);
        }
    }

// org.jfree.chart.plot.junit.MarkerTests::testGetSetOutlinePaint
    public void testGetSetOutlinePaint() {
        
        
        ValueMarker m = new ValueMarker(1.1);
        m.addChangeListener(this);
        this.lastEvent = null;
        assertEquals(Color.gray, m.getOutlinePaint());
        m.setOutlinePaint(Color.yellow);
        assertEquals(Color.yellow, m.getOutlinePaint());
        assertEquals(m, this.lastEvent.getMarker());

        
        m.setOutlinePaint(null);
        assertEquals(null, m.getOutlinePaint());
    }

// org.jfree.chart.plot.junit.MarkerTests::testGetSetOutlineStroke
    public void testGetSetOutlineStroke() {
        
        
        ValueMarker m = new ValueMarker(1.1);
        m.addChangeListener(this);
        this.lastEvent = null;
        assertEquals(new BasicStroke(0.5f), m.getOutlineStroke());
        m.setOutlineStroke(new BasicStroke(1.1f));
        assertEquals(new BasicStroke(1.1f), m.getOutlineStroke());
        assertEquals(m, this.lastEvent.getMarker());

        
        m.setOutlineStroke(null);
        assertEquals(null, m.getOutlineStroke());
    }

// org.jfree.chart.plot.junit.MarkerTests::testGetSetAlpha
    public void testGetSetAlpha() {
        
        
        ValueMarker m = new ValueMarker(1.1);
        m.addChangeListener(this);
        this.lastEvent = null;
        assertEquals(0.8f, m.getAlpha(), EPSILON);
        m.setAlpha(0.5f);
        assertEquals(0.5f, m.getAlpha(), EPSILON);
        assertEquals(m, this.lastEvent.getMarker());
    }

// org.jfree.chart.plot.junit.MarkerTests::testGetSetLabel
    public void testGetSetLabel() {
        
        
        ValueMarker m = new ValueMarker(1.1);
        m.addChangeListener(this);
        this.lastEvent = null;
        assertEquals(null, m.getLabel());
        m.setLabel("XYZ");
        assertEquals("XYZ", m.getLabel());
        assertEquals(m, this.lastEvent.getMarker());

        
        m.setLabel(null);
        assertEquals(null, m.getLabel());
    }

// org.jfree.chart.plot.junit.MarkerTests::testGetSetLabelFont
    public void testGetSetLabelFont() {
        
        
        ValueMarker m = new ValueMarker(1.1);
        m.addChangeListener(this);
        this.lastEvent = null;
        assertEquals(new Font("Tahoma", Font.PLAIN, 9), m.getLabelFont());
        m.setLabelFont(new Font("SansSerif", Font.BOLD, 10));
        assertEquals(new Font("SansSerif", Font.BOLD, 10), m.getLabelFont());
        assertEquals(m, this.lastEvent.getMarker());

        
        try {
            m.setLabelFont(null);
            fail("Expected an IllegalArgumentException for null.");
        }
        catch (IllegalArgumentException e) {
            assertTrue(true);
        }
    }

// org.jfree.chart.plot.junit.MarkerTests::testGetSetLabelPaint
    public void testGetSetLabelPaint() {
        
        
        ValueMarker m = new ValueMarker(1.1);
        m.addChangeListener(this);
        this.lastEvent = null;
        assertEquals(Color.black, m.getLabelPaint());
        m.setLabelPaint(Color.red);
        assertEquals(Color.red, m.getLabelPaint());
        assertEquals(m, this.lastEvent.getMarker());

        
        try {
            m.setLabelPaint(null);
            fail("Expected an IllegalArgumentException for null.");
        }
        catch (IllegalArgumentException e) {
            assertTrue(true);
        }
    }

// org.jfree.chart.plot.junit.MarkerTests::testGetSetLabelAnchor
    public void testGetSetLabelAnchor() {
        
        
        ValueMarker m = new ValueMarker(1.1);
        m.addChangeListener(this);
        this.lastEvent = null;
        assertEquals(RectangleAnchor.TOP_LEFT, m.getLabelAnchor());
        m.setLabelAnchor(RectangleAnchor.TOP);
        assertEquals(RectangleAnchor.TOP, m.getLabelAnchor());
        assertEquals(m, this.lastEvent.getMarker());

        
        try {
            m.setLabelAnchor(null);
            fail("Expected an IllegalArgumentException for null.");
        }
        catch (IllegalArgumentException e) {
            assertTrue(true);
        }
    }

// org.jfree.chart.plot.junit.MarkerTests::testGetSetLabelOffset
    public void testGetSetLabelOffset() {
        
        
        ValueMarker m = new ValueMarker(1.1);
        m.addChangeListener(this);
        this.lastEvent = null;
        assertEquals(new RectangleInsets(3, 3, 3, 3), m.getLabelOffset());
        m.setLabelOffset(new RectangleInsets(1, 2, 3, 4));
        assertEquals(new RectangleInsets(1, 2, 3, 4), m.getLabelOffset());
        assertEquals(m, this.lastEvent.getMarker());

        
        try {
            m.setLabelOffset(null);
            fail("Expected an IllegalArgumentException for null.");
        }
        catch (IllegalArgumentException e) {
            assertTrue(true);
        }
    }

// org.jfree.chart.plot.junit.MarkerTests::testGetSetLabelOffsetType
    public void testGetSetLabelOffsetType() {
        
        
        ValueMarker m = new ValueMarker(1.1);
        m.addChangeListener(this);
        this.lastEvent = null;
        assertEquals(LengthAdjustmentType.CONTRACT, m.getLabelOffsetType());
        m.setLabelOffsetType(LengthAdjustmentType.EXPAND);
        assertEquals(LengthAdjustmentType.EXPAND, m.getLabelOffsetType());
        assertEquals(m, this.lastEvent.getMarker());

        
        try {
            m.setLabelOffsetType(null);
            fail("Expected an IllegalArgumentException for null.");
        }
        catch (IllegalArgumentException e) {
            assertTrue(true);
        }
    }

// org.jfree.chart.plot.junit.MarkerTests::testGetSetLabelTextAnchor
    public void testGetSetLabelTextAnchor() {
        
        
        ValueMarker m = new ValueMarker(1.1);
        m.addChangeListener(this);
        this.lastEvent = null;
        assertEquals(TextAnchor.CENTER, m.getLabelTextAnchor());
        m.setLabelTextAnchor(TextAnchor.BASELINE_LEFT);
        assertEquals(TextAnchor.BASELINE_LEFT, m.getLabelTextAnchor());
        assertEquals(m, this.lastEvent.getMarker());

        
        try {
            m.setLabelTextAnchor(null);
            fail("Expected an IllegalArgumentException for null.");
        }
        catch (IllegalArgumentException e) {
            assertTrue(true);
        }
    }

// org.jfree.chart.plot.junit.MarkerTests::testListenersWithCategoryPlot
    public void testListenersWithCategoryPlot() {
        CategoryPlot plot = new CategoryPlot();
        CategoryMarker marker1 = new CategoryMarker("X");
        ValueMarker marker2 = new ValueMarker(1.0);
        plot.addDomainMarker(marker1);
        plot.addRangeMarker(marker2);
        EventListener[] listeners1 = marker1.getListeners(
                MarkerChangeListener.class);
        assertTrue(Arrays.asList(listeners1).contains(plot));
        EventListener[] listeners2 = marker1.getListeners(
                MarkerChangeListener.class);
        assertTrue(Arrays.asList(listeners2).contains(plot));
        plot.clearDomainMarkers();
        plot.clearRangeMarkers();
        listeners1 = marker1.getListeners(MarkerChangeListener.class);
        assertFalse(Arrays.asList(listeners1).contains(plot));
        listeners2 = marker1.getListeners(MarkerChangeListener.class);
        assertFalse(Arrays.asList(listeners2).contains(plot));
    }

// org.jfree.chart.plot.junit.MarkerTests::testListenersWithXYPlot
    public void testListenersWithXYPlot() {
        XYPlot plot = new XYPlot();
        ValueMarker marker1 = new ValueMarker(1.0);
        ValueMarker marker2 = new ValueMarker(2.0);
        plot.addDomainMarker(marker1);
        plot.addRangeMarker(marker2);
        EventListener[] listeners1 = marker1.getListeners(
                MarkerChangeListener.class);
        assertTrue(Arrays.asList(listeners1).contains(plot));
        EventListener[] listeners2 = marker1.getListeners(
                MarkerChangeListener.class);
        assertTrue(Arrays.asList(listeners2).contains(plot));
        plot.clearDomainMarkers();
        plot.clearRangeMarkers();
        listeners1 = marker1.getListeners(MarkerChangeListener.class);
        assertFalse(Arrays.asList(listeners1).contains(plot));
        listeners2 = marker1.getListeners(MarkerChangeListener.class);
        assertFalse(Arrays.asList(listeners2).contains(plot));
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

        p1.setLegendItemShape(new Rectangle2D.Double(1.0, 2.0, 3.0, 4.0));
        assertFalse(p1.equals(p2));
        p2.setLegendItemShape(new Rectangle2D.Double(1.0, 2.0, 3.0, 4.0));
        assertTrue(p1.equals(p2));
    }

// org.jfree.chart.plot.junit.MultiplePiePlotTests::testCloning
    public void testCloning() {
        MultiplePiePlot p1 = new MultiplePiePlot();
        Rectangle2D rect = new Rectangle2D.Double(1.0, 2.0, 3.0, 4.0);
        p1.setLegendItemShape(rect);
        MultiplePiePlot p2 = null;
        try {
            p2 = (MultiplePiePlot) p1.clone();
        }
        catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        assertTrue(p1 != p2);
        assertTrue(p1.getClass() == p2.getClass());
        assertTrue(p1.equals(p2));

        
        rect.setRect(2.0, 3.0, 4.0, 5.0);
        assertFalse(p1.equals(p2));
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

            ObjectInput in = new ObjectInputStream(new ByteArrayInputStream(
                    buffer.toByteArray()));
            p2 = (MultiplePiePlot) in.readObject();
            in.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        assertEquals(p1, p2);
    }

// org.jfree.chart.plot.junit.MultiplePiePlotTests::testGetLegendItems
    public void testGetLegendItems() {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        dataset.addValue(35.0, "S1", "C1");
        dataset.addValue(45.0, "S1", "C2");
        dataset.addValue(55.0, "S2", "C1");
        dataset.addValue(15.0, "S2", "C2");
        MultiplePiePlot plot = new MultiplePiePlot(dataset);
        JFreeChart chart = new JFreeChart(plot);
        LegendItemCollection legendItems = plot.getLegendItems();
        assertEquals(2, legendItems.getItemCount());
        LegendItem item1 = legendItems.get(0);
        assertEquals("S1", item1.getLabel());
        assertEquals("S1", item1.getSeriesKey());
        assertEquals(0, item1.getSeriesIndex());
        assertEquals(dataset, item1.getDataset());
        assertEquals(0, item1.getDatasetIndex());

        LegendItem item2 = legendItems.get(1);
        assertEquals("S2", item2.getLabel());
        assertEquals("S2", item2.getSeriesKey());
        assertEquals(1, item2.getSeriesIndex());
        assertEquals(dataset, item2.getDataset());
        assertEquals(0, item2.getDatasetIndex());
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

// buggy code
    protected AxisState drawLabel(String label, Graphics2D g2, 
            Rectangle2D plotArea, Rectangle2D dataArea, RectangleEdge edge, 
            AxisState state, PlotRenderingInfo plotState) {

        // it is unlikely that 'state' will be null, but check anyway...
        if (state == null) {
            throw new IllegalArgumentException("Null 'state' argument.");
        }
        
        if ((label == null) || (label.equals(""))) {
            return state;
        }

        Font font = getLabelFont();
        RectangleInsets insets = getLabelInsets();
        g2.setFont(font);
        g2.setPaint(getLabelPaint());
        FontMetrics fm = g2.getFontMetrics();
        Rectangle2D labelBounds = TextUtilities.getTextBounds(label, g2, fm);
        Shape hotspot = null;
        
        if (edge == RectangleEdge.TOP) {
            AffineTransform t = AffineTransform.getRotateInstance(
                    getLabelAngle(), labelBounds.getCenterX(), 
                    labelBounds.getCenterY());
            Shape rotatedLabelBounds = t.createTransformedShape(labelBounds);
            labelBounds = rotatedLabelBounds.getBounds2D();
            float w = (float) labelBounds.getWidth();
            float h = (float) labelBounds.getHeight();
            float labelx = (float) dataArea.getCenterX();
            float labely = (float) (state.getCursor() - insets.getBottom() 
                    - h / 2.0);
            TextUtilities.drawRotatedString(label, g2, labelx, labely, 
                    TextAnchor.CENTER, getLabelAngle(), TextAnchor.CENTER);
            hotspot = new Rectangle2D.Float(labelx - w / 2.0f, 
                    labely - h / 2.0f, w, h);
            state.cursorUp(insets.getTop() + labelBounds.getHeight() 
                    + insets.getBottom());
        }
        else if (edge == RectangleEdge.BOTTOM) {
            AffineTransform t = AffineTransform.getRotateInstance(
                    getLabelAngle(), labelBounds.getCenterX(), 
                    labelBounds.getCenterY());
            Shape rotatedLabelBounds = t.createTransformedShape(labelBounds);
            labelBounds = rotatedLabelBounds.getBounds2D();
            float w = (float) labelBounds.getWidth();
            float h = (float) labelBounds.getHeight();
            float labelx = (float) dataArea.getCenterX();
            float labely = (float) (state.getCursor() + insets.getTop() 
                    + h / 2.0);
            TextUtilities.drawRotatedString(label, g2, labelx, labely, 
                    TextAnchor.CENTER, getLabelAngle(), TextAnchor.CENTER);
            hotspot = new Rectangle2D.Float(labelx - w / 2.0f, 
                    labely - h / 2.0f, w, h);
            state.cursorDown(insets.getTop() + labelBounds.getHeight() 
                    + insets.getBottom());
        }
        else if (edge == RectangleEdge.LEFT) {
            AffineTransform t = AffineTransform.getRotateInstance(
                    getLabelAngle() - Math.PI / 2.0, labelBounds.getCenterX(), 
                    labelBounds.getCenterY());
            Shape rotatedLabelBounds = t.createTransformedShape(labelBounds);
            labelBounds = rotatedLabelBounds.getBounds2D();
            float w = (float) labelBounds.getWidth();
            float h = (float) labelBounds.getHeight();
            float labelx = (float) (state.getCursor() - insets.getRight() 
                    - w / 2.0);
            float labely = (float) dataArea.getCenterY();
            TextUtilities.drawRotatedString(label, g2, labelx, labely, 
                    TextAnchor.CENTER, getLabelAngle() - Math.PI / 2.0, 
                    TextAnchor.CENTER);
            hotspot = new Rectangle2D.Float(labelx - w / 2.0f, 
                    labely - h / 2.0f, w, h);
            state.cursorLeft(insets.getLeft() + labelBounds.getWidth() 
                    + insets.getRight());
        }
        else if (edge == RectangleEdge.RIGHT) {

            AffineTransform t = AffineTransform.getRotateInstance(
                    getLabelAngle() + Math.PI / 2.0, 
                    labelBounds.getCenterX(), labelBounds.getCenterY());
            Shape rotatedLabelBounds = t.createTransformedShape(labelBounds);
            labelBounds = rotatedLabelBounds.getBounds2D();
            float w = (float) labelBounds.getWidth();
            float h = (float) labelBounds.getHeight();
            float labelx = (float) (state.getCursor() 
                            + insets.getLeft() + w / 2.0);
            float labely = (float) (dataArea.getY() + dataArea.getHeight() 
                    / 2.0);
            TextUtilities.drawRotatedString(label, g2, labelx, labely, 
                    TextAnchor.CENTER, getLabelAngle() + Math.PI / 2.0, 
                    TextAnchor.CENTER);
            hotspot = new Rectangle2D.Float(labelx - w / 2.0f, 
                    labely - h / 2.0f, w, h);
            state.cursorRight(insets.getLeft() + labelBounds.getWidth() 
                    + insets.getRight());

        }
        if (plotState != null && hotspot != null) {
            ChartRenderingInfo owner = plotState.getOwner();
                EntityCollection entities = owner.getEntityCollection();
                if (entities != null) {
                    entities.add(new AxisLabelEntity(this, hotspot, 
                            this.labelToolTip, this.labelURL));
                }
        }
        return state;

    }

// relevant test
// org.jfree.chart.annotations.junit.CategoryLineAnnotationTests::testEquals
    public void testEquals() {
        
        BasicStroke s1 = new BasicStroke(1.0f);
        BasicStroke s2 = new BasicStroke(2.0f);
        CategoryLineAnnotation a1 = new CategoryLineAnnotation(
            "Category 1", 1.0, "Category 2", 2.0, Color.red, s1);
        CategoryLineAnnotation a2 = new CategoryLineAnnotation(
            "Category 1", 1.0, "Category 2", 2.0, Color.red, s1);
        assertTrue(a1.equals(a2));
        assertTrue(a2.equals(a1));
        
        
        a1.setCategory1("Category A");
        assertFalse(a1.equals(a2));
        a2.setCategory1("Category A");
        assertTrue(a1.equals(a2));

        
        a1.setValue1(0.15);
        assertFalse(a1.equals(a2));
        a2.setValue1(0.15);
        assertTrue(a1.equals(a2));
      
        
        a1.setCategory2("Category B");
        assertFalse(a1.equals(a2));
        a2.setCategory2("Category B");
        assertTrue(a1.equals(a2));

        
        a1.setValue2(0.25);
        assertFalse(a1.equals(a2));
        a2.setValue2(0.25);
        assertTrue(a1.equals(a2));
        
        
        a1.setPaint(Color.yellow);
        assertFalse(a1.equals(a2));
        a2.setPaint(Color.yellow);
        assertTrue(a1.equals(a2));
        
        
        a1.setStroke(s2);
        assertFalse(a1.equals(a2));
        a2.setStroke(s2);
        assertTrue(a1.equals(a2));
    }

// org.jfree.chart.annotations.junit.CategoryLineAnnotationTests::testHashcode
    public void testHashcode() {
        CategoryLineAnnotation a1 = new CategoryLineAnnotation(
            "Category 1", 1.0, "Category 2", 2.0, Color.red, 
            new BasicStroke(1.0f));
        CategoryLineAnnotation a2 = new CategoryLineAnnotation(
            "Category 1", 1.0, "Category 2", 2.0, Color.red, 
            new BasicStroke(1.0f));
        assertTrue(a1.equals(a2));
        int h1 = a1.hashCode();
        int h2 = a2.hashCode();
        assertEquals(h1, h2);
    }

// org.jfree.chart.annotations.junit.CategoryLineAnnotationTests::testCloning
    public void testCloning() {
        CategoryLineAnnotation a1 = new CategoryLineAnnotation(
            "Category 1", 1.0, "Category 2", 2.0, Color.red, 
            new BasicStroke(1.0f));
        CategoryLineAnnotation a2 = null;
        try {
            a2 = (CategoryLineAnnotation) a1.clone();
        }
        catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        assertTrue(a1 != a2);
        assertTrue(a1.getClass() == a2.getClass());
        assertTrue(a1.equals(a2));
    }

// org.jfree.chart.annotations.junit.CategoryLineAnnotationTests::testSerialization
    public void testSerialization() {

        CategoryLineAnnotation a1 = new CategoryLineAnnotation(
            "Category 1", 1.0, "Category 2", 2.0, Color.red, 
            new BasicStroke(1.0f));
        CategoryLineAnnotation a2 = null;

        try {
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            ObjectOutput out = new ObjectOutputStream(buffer);
            out.writeObject(a1);
            out.close();

            ObjectInput in = new ObjectInputStream(
                new ByteArrayInputStream(buffer.toByteArray())
            );
            a2 = (CategoryLineAnnotation) in.readObject();
            in.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        assertEquals(a1, a2);

    }

// org.jfree.chart.annotations.junit.CategoryPointerAnnotationTests::testEquals
    public void testEquals() {
        
        CategoryPointerAnnotation a1 = new CategoryPointerAnnotation("Label", 
                "Key 1", 20.0, Math.PI);
        CategoryPointerAnnotation a2 = new CategoryPointerAnnotation("Label", 
                "Key 1", 20.0, Math.PI);
        assertTrue(a1.equals(a2));
        
        a1 = new CategoryPointerAnnotation("Label2", "Key 1", 20.0, Math.PI);
        assertFalse(a1.equals(a2));
        a2 = new CategoryPointerAnnotation("Label2", "Key 1", 20.0, Math.PI);
        assertTrue(a1.equals(a2));
        
        a1.setCategory("Key 2");
        assertFalse(a1.equals(a2));
        a2.setCategory("Key 2");
        assertTrue(a1.equals(a2));
        
        a1.setValue(22.0);
        assertFalse(a1.equals(a2));
        a2.setValue(22.0);
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

// org.jfree.chart.annotations.junit.CategoryPointerAnnotationTests::testHashCode
    public void testHashCode() {
        CategoryPointerAnnotation a1 = new CategoryPointerAnnotation("Label", 
                "A", 20.0, Math.PI);
        CategoryPointerAnnotation a2 = new CategoryPointerAnnotation("Label", 
                "A", 20.0, Math.PI);
        assertTrue(a1.equals(a2));
        int h1 = a1.hashCode();
        int h2 = a2.hashCode();
        assertEquals(h1, h2);
    }

// org.jfree.chart.annotations.junit.CategoryPointerAnnotationTests::testCloning
    public void testCloning() {
        CategoryPointerAnnotation a1 = new CategoryPointerAnnotation("Label", 
                "A", 20.0, Math.PI);
        CategoryPointerAnnotation a2 = null;
        try {
            a2 = (CategoryPointerAnnotation) a1.clone();
        }
        catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        assertTrue(a1 != a2);
        assertTrue(a1.getClass() == a2.getClass());
        assertTrue(a1.equals(a2));
    }

// org.jfree.chart.annotations.junit.CategoryPointerAnnotationTests::testSerialization
    public void testSerialization() {

        CategoryPointerAnnotation a1 = new CategoryPointerAnnotation("Label", 
                "A", 20.0, Math.PI);
        CategoryPointerAnnotation a2 = null;

        try {
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            ObjectOutput out = new ObjectOutputStream(buffer);
            out.writeObject(a1);
            out.close();

            ObjectInput in = new ObjectInputStream(new ByteArrayInputStream(
                    buffer.toByteArray()));
            a2 = (CategoryPointerAnnotation) in.readObject();
            in.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        assertEquals(a1, a2);

    }

// org.jfree.chart.annotations.junit.CategoryTextAnnotationTests::testEquals
    public void testEquals() {
        
        CategoryTextAnnotation a1 = new CategoryTextAnnotation(
            "Test", "Category", 1.0
        );
        CategoryTextAnnotation a2 = new CategoryTextAnnotation(
            "Test", "Category", 1.0
        );
        assertTrue(a1.equals(a2));
        
        
        a1.setCategory("Category 2");
        assertFalse(a1.equals(a2));
        a2.setCategory("Category 2");
        assertTrue(a1.equals(a2));

        
        a1.setCategoryAnchor(CategoryAnchor.START);
        assertFalse(a1.equals(a2));
        a2.setCategoryAnchor(CategoryAnchor.START);
        assertTrue(a1.equals(a2));

        
        a1.setValue(0.15);
        assertFalse(a1.equals(a2));
        a2.setValue(0.15);
        assertTrue(a1.equals(a2));
      
    }

// org.jfree.chart.annotations.junit.CategoryTextAnnotationTests::testHashcode
    public void testHashcode() {
        CategoryTextAnnotation a1 = new CategoryTextAnnotation(
            "Test", "Category", 1.0
        );
        CategoryTextAnnotation a2 = new CategoryTextAnnotation(
            "Test", "Category", 1.0
        );
        assertTrue(a1.equals(a2));
        int h1 = a1.hashCode();
        int h2 = a2.hashCode();
        assertEquals(h1, h2);
    }

// org.jfree.chart.annotations.junit.CategoryTextAnnotationTests::testCloning
    public void testCloning() {
        CategoryTextAnnotation a1 = new CategoryTextAnnotation(
            "Test", "Category", 1.0
        );
        CategoryTextAnnotation a2 = null;
        try {
            a2 = (CategoryTextAnnotation) a1.clone();
        }
        catch (CloneNotSupportedException e) {
            System.err.println("Failed to clone.");
        }
        assertTrue(a1 != a2);
        assertTrue(a1.getClass() == a2.getClass());
        assertTrue(a1.equals(a2));
    }

// org.jfree.chart.annotations.junit.CategoryTextAnnotationTests::testSerialization
    public void testSerialization() {

        CategoryTextAnnotation a1 = new CategoryTextAnnotation(
            "Test", "Category", 1.0
        );
        CategoryTextAnnotation a2 = null;

        try {
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            ObjectOutput out = new ObjectOutputStream(buffer);
            out.writeObject(a1);
            out.close();

            ObjectInput in = new ObjectInputStream(
                new ByteArrayInputStream(buffer.toByteArray())
            );
            a2 = (CategoryTextAnnotation) in.readObject();
            in.close();
        }
        catch (Exception e) {
            System.out.println(e.toString());
        }
        assertEquals(a1, a2);

    }

// org.jfree.chart.annotations.junit.XYBoxAnnotationTests::testEquals
    public void testEquals() {
        
        XYBoxAnnotation a1 = new XYBoxAnnotation(
            1.0, 2.0, 3.0, 4.0, new BasicStroke(1.2f), Color.red, Color.blue
        );
        XYBoxAnnotation a2 = new XYBoxAnnotation(
            1.0, 2.0, 3.0, 4.0, new BasicStroke(1.2f), Color.red, Color.blue
        );
        assertTrue(a1.equals(a2));
        assertTrue(a2.equals(a1));
      
        
        a1 = new XYBoxAnnotation(
            2.0, 2.0, 3.0, 4.0, new BasicStroke(1.2f), Color.red, Color.blue
        );
        assertFalse(a1.equals(a2));
        a2 = new XYBoxAnnotation(
            2.0, 2.0, 3.0, 4.0, new BasicStroke(1.2f), Color.red, Color.blue
        );
        assertTrue(a1.equals(a2));
        
        
        a1 = new XYBoxAnnotation(
            1.0, 2.0, 3.0, 4.0, new BasicStroke(2.3f), Color.red, Color.blue
        );
        assertFalse(a1.equals(a2));
        a2 = new XYBoxAnnotation(
            1.0, 2.0, 3.0, 4.0, new BasicStroke(2.3f), Color.red, Color.blue
        );
        assertTrue(a1.equals(a2));
        
        GradientPaint gp1a = new GradientPaint(1.0f, 2.0f, Color.blue, 
                3.0f, 4.0f, Color.red);
        GradientPaint gp1b = new GradientPaint(1.0f, 2.0f, Color.blue, 
                3.0f, 4.0f, Color.red);
        GradientPaint gp2a = new GradientPaint(5.0f, 6.0f, Color.pink, 
                7.0f, 8.0f, Color.white);
        GradientPaint gp2b = new GradientPaint(5.0f, 6.0f, Color.pink, 
                7.0f, 8.0f, Color.white);
        
        
        a1 = new XYBoxAnnotation(
            1.0, 2.0, 3.0, 4.0, new BasicStroke(2.3f), gp1a, Color.blue
        );
        assertFalse(a1.equals(a2));
        a2 = new XYBoxAnnotation(
            1.0, 2.0, 3.0, 4.0, new BasicStroke(2.3f), gp1b, Color.blue
        );
        assertTrue(a1.equals(a2));
        
        
        a1 = new XYBoxAnnotation(
            1.0, 2.0, 3.0, 4.0, new BasicStroke(2.3f), gp1a, gp2a
        );
        assertFalse(a1.equals(a2));
        a2 = new XYBoxAnnotation(
            1.0, 2.0, 3.0, 4.0, new BasicStroke(2.3f), gp1b, gp2b
        );
        assertTrue(a1.equals(a2));
    }

// org.jfree.chart.annotations.junit.XYBoxAnnotationTests::testHashCode
    public void testHashCode() {
        XYBoxAnnotation a1 = new XYBoxAnnotation(
            1.0, 2.0, 3.0, 4.0, new BasicStroke(1.2f), Color.red, Color.blue
        );
        XYBoxAnnotation a2 = new XYBoxAnnotation(
            1.0, 2.0, 3.0, 4.0, new BasicStroke(1.2f), Color.red, Color.blue
        );
        assertTrue(a1.equals(a2));
        int h1 = a1.hashCode();
        int h2 = a2.hashCode();
        assertEquals(h1, h2);
    }

// org.jfree.chart.annotations.junit.XYBoxAnnotationTests::testCloning
    public void testCloning() {

        XYBoxAnnotation a1 = new XYBoxAnnotation(
            1.0, 2.0, 3.0, 4.0, new BasicStroke(1.2f), Color.red, Color.blue
        );
        XYBoxAnnotation a2 = null;
        try {
            a2 = (XYBoxAnnotation) a1.clone();
        }
        catch (CloneNotSupportedException e) {
            System.err.println("Failed to clone.");
        }
        assertTrue(a1 != a2);
        assertTrue(a1.getClass() == a2.getClass());
        assertTrue(a1.equals(a2));
    }

// org.jfree.chart.annotations.junit.XYBoxAnnotationTests::testSerialization
    public void testSerialization() {

        XYBoxAnnotation a1 = new XYBoxAnnotation(
            1.0, 2.0, 3.0, 4.0, 
            new BasicStroke(1.2f), Color.red, Color.blue
        );
        XYBoxAnnotation a2 = null;

        try {
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            ObjectOutput out = new ObjectOutputStream(buffer);
            out.writeObject(a1);
            out.close();

            ObjectInput in = new ObjectInputStream(
                new ByteArrayInputStream(buffer.toByteArray())
            );
            a2 = (XYBoxAnnotation) in.readObject();
            in.close();
        }
        catch (Exception e) {
            System.out.println(e.toString());
        }
        assertEquals(a1, a2);

    }

// org.jfree.chart.annotations.junit.XYDrawableAnnotationTests::testEquals
    public void testEquals() {    
        XYDrawableAnnotation a1 = new XYDrawableAnnotation(10.0, 20.0, 100.0, 
                200.0, new TestDrawable());
        XYDrawableAnnotation a2 = new XYDrawableAnnotation(10.0, 20.0, 100.0, 
                200.0, new TestDrawable());
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
        XYLineAnnotation a1 = new XYLineAnnotation(
            10.0, 20.0, 100.0, 200.0, stroke, Color.blue
        );
        XYLineAnnotation a2 = new XYLineAnnotation(
            10.0, 20.0, 100.0, 200.0, stroke, Color.blue
        );
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
        XYLineAnnotation a1 = new XYLineAnnotation(
            10.0, 20.0, 100.0, 200.0, stroke, Color.blue
        );
        XYLineAnnotation a2 = new XYLineAnnotation(
            10.0, 20.0, 100.0, 200.0, stroke, Color.blue
        );
        assertTrue(a1.equals(a2));
        int h1 = a1.hashCode();
        int h2 = a2.hashCode();
        assertEquals(h1, h2);
    }

// org.jfree.chart.annotations.junit.XYLineAnnotationTests::testCloning
    public void testCloning() {
        Stroke stroke = new BasicStroke(2.0f);
        XYLineAnnotation a1 = new XYLineAnnotation(
            10.0, 20.0, 100.0, 200.0, stroke, Color.blue
        );
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

// org.jfree.chart.annotations.junit.XYLineAnnotationTests::testSerialization
    public void testSerialization() {

        Stroke stroke = new BasicStroke(2.0f);
        XYLineAnnotation a1 = new XYLineAnnotation(
            10.0, 20.0, 100.0, 200.0, stroke, Color.blue
        );
        XYLineAnnotation a2 = null;

        try {
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            ObjectOutput out = new ObjectOutputStream(buffer);
            out.writeObject(a1);
            out.close();

            ObjectInput in = new ObjectInputStream(
                new ByteArrayInputStream(buffer.toByteArray())
            );
            a2 = (XYLineAnnotation) in.readObject();
            in.close();
        }
        catch (Exception e) {
            System.out.println(e.toString());
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
            System.out.println(e.toString());
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
            System.err.println("Failed to clone.");
        }
        assertTrue(a1 != a2);
        assertTrue(a1.getClass() == a2.getClass());
        assertTrue(a1.equals(a2));
    }

// org.jfree.chart.annotations.junit.XYTextAnnotationTests::testSerialization
    public void testSerialization() {

        XYTextAnnotation a1 = new XYTextAnnotation("Text", 10.0, 20.0);
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
            System.out.println(e.toString());
        }
        assertEquals(a1, a2);

    }

// org.jfree.chart.axis.junit.AxisTests::testCloning
    public void testCloning() {
        CategoryAxis a1 = new CategoryAxis("Test");
        a1.setAxisLinePaint(Color.red);
        CategoryAxis a2 = null;
        try {
            a2 = (CategoryAxis) a1.clone();
        }
        catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        assertTrue(a1 != a2);
        assertTrue(a1.getClass() == a2.getClass());
        assertTrue(a1.equals(a2));
    }

// org.jfree.chart.axis.junit.AxisTests::testEquals
    public void testEquals() {
        
        Axis a1 = new CategoryAxis("Test");
        Axis a2 = new CategoryAxis("Test");
        assertTrue(a1.equals(a2));
        
        
        a1.setVisible(false);
        assertFalse(a1.equals(a2));
        a2.setVisible(false);
        assertTrue(a1.equals(a2));
                
        
        a1.setLabel("New Label");
        assertFalse(a1.equals(a2));
        a2.setLabel("New Label");
        assertTrue(a1.equals(a2));

        
        a1.setLabelFont(new Font("Dialog", Font.PLAIN, 8));
        assertFalse(a1.equals(a2));
        a2.setLabelFont(new Font("Dialog", Font.PLAIN, 8));
        assertTrue(a1.equals(a2));

        
        a1.setLabelPaint(new GradientPaint(1.0f, 2.0f, Color.white, 
                3.0f, 4.0f, Color.black));
        assertFalse(a1.equals(a2));
        a2.setLabelPaint(new GradientPaint(1.0f, 2.0f, Color.white, 
                3.0f, 4.0f, Color.black));
        assertTrue(a1.equals(a2));
       
        
        a1.setLabelInsets(new RectangleInsets(10.0, 10.0, 10.0, 10.0));
        assertFalse(a1.equals(a2));
        a2.setLabelInsets(new RectangleInsets(10.0, 10.0, 10.0, 10.0));
        assertTrue(a1.equals(a2));

        
        a1.setLabelAngle(1.23);
        assertFalse(a1.equals(a2));
        a2.setLabelAngle(1.23);
        assertTrue(a1.equals(a2));

        a1.setLabelToolTip("123");
        assertFalse(a1.equals(a2));
        a2.setLabelToolTip("123");
        assertTrue(a1.equals(a2));

        a1.setLabelURL("ABC");
        assertFalse(a1.equals(a2));
        a2.setLabelURL("ABC");
        assertTrue(a1.equals(a2));

        
        a1.setAxisLineVisible(false);
        assertFalse(a1.equals(a2));
        a2.setAxisLineVisible(false);
        assertTrue(a1.equals(a2));
        
        
        BasicStroke s = new BasicStroke(1.1f);
        a1.setAxisLineStroke(s);
        assertFalse(a1.equals(a2));
        a2.setAxisLineStroke(s);
        assertTrue(a1.equals(a2));
        
        
        a1.setAxisLinePaint(new GradientPaint(1.0f, 2.0f, Color.red, 
                3.0f, 4.0f, Color.black));
        assertFalse(a1.equals(a2));
        a2.setAxisLinePaint(new GradientPaint(1.0f, 2.0f, Color.red, 
                3.0f, 4.0f, Color.black));
        assertTrue(a1.equals(a2));
        
        
        a1.setTickLabelsVisible(false);
        assertFalse(a1.equals(a2));
        a2.setTickLabelsVisible(false);
        assertTrue(a1.equals(a2));
                
        
        a1.setTickLabelFont(new Font("Dialog", Font.PLAIN, 12));
        assertFalse(a1.equals(a2));
        a2.setTickLabelFont(new Font("Dialog", Font.PLAIN, 12));
        assertTrue(a1.equals(a2));

        
        a1.setTickLabelPaint(new GradientPaint(1.0f, 2.0f, Color.yellow, 
                3.0f, 4.0f, Color.black));
        assertFalse(a1.equals(a2));
        a2.setTickLabelPaint(new GradientPaint(1.0f, 2.0f, Color.yellow, 
                3.0f, 4.0f, Color.black));
        assertTrue(a1.equals(a2));

        
        a1.setTickLabelInsets(new RectangleInsets(10.0, 10.0, 10.0, 10.0));
        assertFalse(a1.equals(a2));
        a2.setTickLabelInsets(new RectangleInsets(10.0, 10.0, 10.0, 10.0));
        assertTrue(a1.equals(a2));

        
        a1.setTickMarksVisible(true);
        assertFalse(a1.equals(a2));
        a2.setTickMarksVisible(true);
        assertTrue(a1.equals(a2));
                
        
        a1.setTickMarkInsideLength(1.23f);
        assertFalse(a1.equals(a2));
        a2.setTickMarkInsideLength(1.23f);
        assertTrue(a1.equals(a2));

        
        a1.setTickMarkOutsideLength(1.23f);
        assertFalse(a1.equals(a2));
        a2.setTickMarkOutsideLength(1.23f);
        assertTrue(a1.equals(a2));

        
        a1.setTickMarkStroke(new BasicStroke(2.0f));
        assertFalse(a1.equals(a2));
        a2.setTickMarkStroke(new BasicStroke(2.0f));
        assertTrue(a1.equals(a2));

        
        a1.setTickMarkPaint(new GradientPaint(1.0f, 2.0f, Color.cyan, 
                3.0f, 4.0f, Color.black));
        assertFalse(a1.equals(a2));
        a2.setTickMarkPaint(new GradientPaint(1.0f, 2.0f, Color.cyan, 
                3.0f, 4.0f, Color.black));
        assertTrue(a1.equals(a2));

        
        a1.setFixedDimension(3.21f);
        assertFalse(a1.equals(a2));
        a2.setFixedDimension(3.21f);
        assertTrue(a1.equals(a2));

    }

// org.jfree.chart.axis.junit.AxisTests::testHashCode
    public void testHashCode() {
        Axis a1 = new CategoryAxis("Test");
        Axis a2 = new CategoryAxis("Test");
        assertTrue(a1.equals(a2));
        int h1 = a1.hashCode();
        int h2 = a2.hashCode();
        assertEquals(h1, h2);
    }

// org.jfree.chart.axis.junit.CategoryAxis3DTests::testCloning
    public void testCloning() {
        CategoryAxis3D a1 = new CategoryAxis3D("Test");
        CategoryAxis3D a2 = null;
        try {
            a2 = (CategoryAxis3D) a1.clone();
        }
        catch (CloneNotSupportedException e) {
            System.err.println("Failed to clone.");
        }
        assertTrue(a1 != a2);
        assertTrue(a1.getClass() == a2.getClass());
        assertTrue(a1.equals(a2));
    }

// org.jfree.chart.axis.junit.CategoryAxis3DTests::testSerialization
    public void testSerialization() {

        CategoryAxis3D a1 = new CategoryAxis3D("Test Axis");
        CategoryAxis3D a2 = null;

        try {
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            ObjectOutput out = new ObjectOutputStream(buffer);
            out.writeObject(a1);
            out.close();

            ObjectInput in = new ObjectInputStream(
                new ByteArrayInputStream(buffer.toByteArray())
            );
            a2 = (CategoryAxis3D) in.readObject();
            in.close();
        }
        catch (Exception e) {
            System.out.println(e.toString());
        }
        assertEquals(a1, a2);

    }

// org.jfree.chart.axis.junit.CategoryAxisTests::testEquals
    public void testEquals() {
        
        CategoryAxis a1 = new CategoryAxis("Test");
        CategoryAxis a2 = new CategoryAxis("Test");
        assertTrue(a1.equals(a2));
        
        
        a1.setLowerMargin(0.15);
        assertFalse(a1.equals(a2));
        a2.setLowerMargin(0.15);
        assertTrue(a1.equals(a2));

        
        a1.setUpperMargin(0.15);
        assertFalse(a1.equals(a2));
        a2.setUpperMargin(0.15);
        assertTrue(a1.equals(a2));
      
        
        a1.setCategoryMargin(0.15);
        assertFalse(a1.equals(a2));
        a2.setCategoryMargin(0.15);
        assertTrue(a1.equals(a2));
        
        
        a1.setMaximumCategoryLabelWidthRatio(0.98f);
        assertFalse(a1.equals(a2));
        a2.setMaximumCategoryLabelWidthRatio(0.98f);
        assertTrue(a1.equals(a2));
        
        
        a1.setCategoryLabelPositionOffset(11);
        assertFalse(a1.equals(a2));
        a2.setCategoryLabelPositionOffset(11);
        assertTrue(a1.equals(a2));
        
        
        a1.setCategoryLabelPositions(CategoryLabelPositions.DOWN_45);
        assertFalse(a1.equals(a2));
        a2.setCategoryLabelPositions(CategoryLabelPositions.DOWN_45);
        assertTrue(a1.equals(a2));
        
        
        a1.addCategoryLabelToolTip("Test", "Check");
        assertFalse(a1.equals(a2));
        a2.addCategoryLabelToolTip("Test", "Check");
        assertTrue(a1.equals(a2));
        
        
        a1.setTickLabelFont("C1", new Font("Dialog", Font.PLAIN, 21));
        assertFalse(a1.equals(a2));
        a2.setTickLabelFont("C1", new Font("Dialog", Font.PLAIN, 21));
        assertTrue(a1.equals(a2));
        
        
        a1.setTickLabelPaint("C1", Color.red);
        assertFalse(a1.equals(a2));
        a2.setTickLabelPaint("C1", Color.red);
        assertTrue(a1.equals(a2));

        
        a1.setTickLabelPaint("C1", new GradientPaint(1.0f, 2.0f, Color.red, 
                3.0f, 4.0f, Color.yellow));
        assertFalse(a1.equals(a2));
        a2.setTickLabelPaint("C1", new GradientPaint(1.0f, 2.0f, Color.red, 
                3.0f, 4.0f, Color.yellow));
        assertTrue(a1.equals(a2));
    
    }

// org.jfree.chart.axis.junit.CategoryAxisTests::testHashCode
    public void testHashCode() {
        CategoryAxis a1 = new CategoryAxis("Test");
        CategoryAxis a2 = new CategoryAxis("Test");
        assertTrue(a1.equals(a2));
        int h1 = a1.hashCode();
        int h2 = a2.hashCode();
        assertEquals(h1, h2);
    }

// org.jfree.chart.axis.junit.CategoryAxisTests::testCloning
    public void testCloning() {
        CategoryAxis a1 = new CategoryAxis("Test");
        CategoryAxis a2 = null;
        try {
            a2 = (CategoryAxis) a1.clone();
        }
        catch (CloneNotSupportedException e) {
            System.err.println("Failed to clone.");
        }
        assertTrue(a1 != a2);
        assertTrue(a1.getClass() == a2.getClass());
        assertTrue(a1.equals(a2));
    }

// org.jfree.chart.axis.junit.CategoryAxisTests::testCloning2
    public void testCloning2() {
        CategoryAxis a1 = new CategoryAxis("Test");
        a1.setTickLabelFont("C1", new Font("Dialog", Font.PLAIN, 15));
        a1.setTickLabelPaint("C1", new GradientPaint(1.0f, 2.0f, Color.red, 
                3.0f, 4.0f, Color.white));
        CategoryAxis a2 = null;
        try {
            a2 = (CategoryAxis) a1.clone();
        }
        catch (CloneNotSupportedException e) {
            System.err.println("Failed to clone.");
        }
        assertTrue(a1 != a2);
        assertTrue(a1.getClass() == a2.getClass());
        assertTrue(a1.equals(a2));
        
        
        a1.setTickLabelFont("C1", null);
        assertFalse(a1.equals(a2));
        a2.setTickLabelFont("C1", null);
        assertTrue(a1.equals(a2));
        
        
        a1.setTickLabelPaint("C1", Color.yellow);
        assertFalse(a1.equals(a2));
        a2.setTickLabelPaint("C1", Color.yellow);
        assertTrue(a1.equals(a2));

        
        a1.addCategoryLabelToolTip("C1", "XYZ");
        assertFalse(a1.equals(a2));
        a2.addCategoryLabelToolTip("C1", "XYZ");
        assertTrue(a1.equals(a2));
    }

// org.jfree.chart.axis.junit.CategoryAxisTests::testSerialization
    public void testSerialization() {
        CategoryAxis a1 = new CategoryAxis("Test Axis");
        a1.setTickLabelPaint("C1", new GradientPaint(1.0f, 2.0f, Color.red, 
                3.0f, 4.0f, Color.white));
        CategoryAxis a2 = null;

        try {
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            ObjectOutput out = new ObjectOutputStream(buffer);
            out.writeObject(a1);
            out.close();

            ObjectInput in = new ObjectInputStream(
                new ByteArrayInputStream(buffer.toByteArray())
            );
            a2 = (CategoryAxis) in.readObject();
            in.close();
        }
        catch (Exception e) {
            System.out.println(e.toString());
        }
        assertEquals(a1, a2);
    }

// org.jfree.chart.axis.junit.CyclicNumberAxisTests::testCloning
    public void testCloning() {
        CyclicNumberAxis a1 = new CyclicNumberAxis(10, 0, "Test");
        CyclicNumberAxis a2 = null;
        try {
            a2 = (CyclicNumberAxis) a1.clone();
        }
        catch (CloneNotSupportedException e) {
            System.err.println("Failed to clone.");
        }
        assertTrue(a1 != a2);
        assertTrue(a1.getClass() == a2.getClass());
        assertTrue(a1.equals(a2));
    }

// org.jfree.chart.axis.junit.CyclicNumberAxisTests::testEquals
    public void testEquals() {
       
        CyclicNumberAxis a1 = new CyclicNumberAxis(10, 0, "Test");
        CyclicNumberAxis a2 = new CyclicNumberAxis(10, 0, "Test");
        assertTrue(a1.equals(a2));
        
        
        a1.setPeriod(5);
        assertFalse(a1.equals(a2));
        a2.setPeriod(5);
        assertTrue(a1.equals(a2));

        
        a1.setOffset(2.0);
        assertFalse(a1.equals(a2));
        a2.setOffset(2.0);
        assertTrue(a1.equals(a2));

        
        a1.setAdvanceLinePaint(new GradientPaint(1.0f, 2.0f, Color.red, 
                3.0f, 4.0f, Color.black));
        assertFalse(a1.equals(a2));
        a2.setAdvanceLinePaint(new GradientPaint(1.0f, 2.0f, Color.red, 
                3.0f, 4.0f, Color.black));
        assertTrue(a1.equals(a2));
        
        
        Stroke stroke = new BasicStroke(0.2f);
        a1.setAdvanceLineStroke(stroke);
        assertFalse(a1.equals(a2));
        a2.setAdvanceLineStroke(stroke);
        assertTrue(a1.equals(a2));

        
        a1.setAdvanceLineVisible(!a1.isAdvanceLineVisible());
        assertFalse(a1.equals(a2));
        a2.setAdvanceLineVisible(a1.isAdvanceLineVisible());
        assertTrue(a1.equals(a2));

        
        a1.setBoundMappedToLastCycle(!a1.isBoundMappedToLastCycle());
        assertFalse(a1.equals(a2));
        a2.setBoundMappedToLastCycle(a1.isBoundMappedToLastCycle());
        assertTrue(a1.equals(a2));
        
    }

// org.jfree.chart.axis.junit.CyclicNumberAxisTests::testHashCode
    public void testHashCode() {
        CyclicNumberAxis a1 = new CyclicNumberAxis(10, 0, "Test");
        CyclicNumberAxis a2 = new CyclicNumberAxis(10, 0, "Test");
        assertTrue(a1.equals(a2));
        int h1 = a1.hashCode();
        int h2 = a2.hashCode();
        assertEquals(h1, h2);
    }

// org.jfree.chart.axis.junit.CyclicNumberAxisTests::testSerialization
    public void testSerialization() {

        CyclicNumberAxis a1 = new CyclicNumberAxis(10, 0, "Test Axis");
        CyclicNumberAxis a2 = null;

        try {
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            ObjectOutput out = new ObjectOutputStream(buffer);
            out.writeObject(a1);
            out.close();

            ObjectInput in = new ObjectInputStream(
                new ByteArrayInputStream(buffer.toByteArray())
            );
            a2 = (CyclicNumberAxis) in.readObject();
            in.close();
        }
        catch (Exception e) {
            System.out.println(e.toString());
        }
        assertEquals(a1, a2);

    }

// org.jfree.chart.axis.junit.DateAxisTests::testEquals
    public void testEquals() {
        
        DateAxis a1 = new DateAxis("Test");
        DateAxis a2 = new DateAxis("Test");
        assertTrue(a1.equals(a2));
        assertFalse(a1.equals(null));
        assertFalse(a1.equals("Some non-DateAxis object"));
        
        
        a1.setTickUnit(new DateTickUnit(DateTickUnit.DAY, 7));
        assertFalse(a1.equals(a2));
        a2.setTickUnit(new DateTickUnit(DateTickUnit.DAY, 7));
        assertTrue(a1.equals(a2));

        
        a1.setDateFormatOverride(new SimpleDateFormat("yyyy"));
        assertFalse(a1.equals(a2));
        a2.setDateFormatOverride(new SimpleDateFormat("yyyy"));
        assertTrue(a1.equals(a2));

        
        a1.setTickMarkPosition(DateTickMarkPosition.END);
        assertFalse(a1.equals(a2));
        a2.setTickMarkPosition(DateTickMarkPosition.END);
        assertTrue(a1.equals(a2));
        
        
        a1.setTimeline(SegmentedTimeline.newMondayThroughFridayTimeline());
        assertFalse(a1.equals(a2));
        a2.setTimeline(SegmentedTimeline.newMondayThroughFridayTimeline());
        assertTrue(a1.equals(a2));
        
    }

// org.jfree.chart.axis.junit.DateAxisTests::test1472942
    public void test1472942() {
        DateAxis a1 = new DateAxis("Test");
        DateAxis a2 = new DateAxis("Test");
        assertTrue(a1.equals(a2));

        
        a1.setRange(new Date(1L), new Date(2L));
        assertFalse(a1.equals(a2));
        a2.setRange(new Date(1L), new Date(2L));
        assertTrue(a1.equals(a2));
    }

// org.jfree.chart.axis.junit.DateAxisTests::testHashCode
    public void testHashCode() {
        DateAxis a1 = new DateAxis("Test");
        DateAxis a2 = new DateAxis("Test");
        assertTrue(a1.equals(a2));
        int h1 = a1.hashCode();
        int h2 = a2.hashCode();
        assertEquals(h1, h2);
    }

// org.jfree.chart.axis.junit.DateAxisTests::testCloning
    public void testCloning() {
        DateAxis a1 = new DateAxis("Test");
        DateAxis a2 = null;
        try {
            a2 = (DateAxis) a1.clone();
        }
        catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        assertTrue(a1 != a2);
        assertTrue(a1.getClass() == a2.getClass());
        assertTrue(a1.equals(a2));
    }

// org.jfree.chart.axis.junit.DateAxisTests::testSetRange
    public void testSetRange() {

        DateAxis axis = new DateAxis("Test Axis");
        Calendar calendar = Calendar.getInstance();
        calendar.set(1999, Calendar.JANUARY, 3);
        Date d1 = calendar.getTime();
        calendar.set(1999, Calendar.JANUARY, 31);
        Date d2 = calendar.getTime();
        axis.setRange(d1, d2);

        DateRange range = (DateRange) axis.getRange();
        assertEquals(d1, range.getLowerDate());
        assertEquals(d2, range.getUpperDate());

    }

// org.jfree.chart.axis.junit.DateAxisTests::testSetMaximumDate
    public void testSetMaximumDate() {
        DateAxis axis = new DateAxis("Test Axis");
        Date date = new Date();
        axis.setMaximumDate(date);
        assertEquals(date, axis.getMaximumDate());

        
        
        Date d1 = new Date();
        Date d2 = new Date(d1.getTime() + 1);
        Date d0 = new Date(d1.getTime() - 1);
        axis.setMaximumDate(d2);
        axis.setMinimumDate(d1);
        axis.setMaximumDate(d1);
        assertEquals(d0, axis.getMinimumDate());
    }

// org.jfree.chart.axis.junit.DateAxisTests::testSetMinimumDate
    public void testSetMinimumDate() {
        DateAxis axis = new DateAxis("Test Axis");
        Date d1 = new Date();
        Date d2 = new Date(d1.getTime() + 1);
        axis.setMaximumDate(d2);
        axis.setMinimumDate(d1);
        assertEquals(d1, axis.getMinimumDate());
        
        
        
        Date d3 = new Date(d2.getTime() + 1);
        axis.setMinimumDate(d2);
        assertEquals(d3, axis.getMaximumDate());
    }

// org.jfree.chart.axis.junit.DateAxisTests::testJava2DToValue
    public void testJava2DToValue() {
        DateAxis axis = new DateAxis();
        axis.setRange(50.0, 100.0); 
        Rectangle2D dataArea = new Rectangle2D.Double(10.0, 50.0, 400.0, 300.0);
        double y1 = axis.java2DToValue(75.0, dataArea, RectangleEdge.LEFT);  
        assertTrue(same(y1, 95.8333333, 1.0)); 
        double y2 = axis.java2DToValue(75.0, dataArea, RectangleEdge.RIGHT);   
        assertTrue(same(y2, 95.8333333, 1.0)); 
        double x1 = axis.java2DToValue(75.0, dataArea, RectangleEdge.TOP);   
        assertTrue(same(x1, 58.125, 1.0)); 
        double x2 = axis.java2DToValue(75.0, dataArea, RectangleEdge.BOTTOM);   
        assertTrue(same(x2, 58.125, 1.0)); 
        axis.setInverted(true);
        double y3 = axis.java2DToValue(75.0, dataArea, RectangleEdge.LEFT);  
        assertTrue(same(y3, 54.1666667, 1.0)); 
        double y4 = axis.java2DToValue(75.0, dataArea, RectangleEdge.RIGHT);   
        assertTrue(same(y4, 54.1666667, 1.0)); 
        double x3 = axis.java2DToValue(75.0, dataArea, RectangleEdge.TOP);   
        assertTrue(same(x3, 91.875, 1.0)); 
        double x4 = axis.java2DToValue(75.0, dataArea, RectangleEdge.BOTTOM);   
        assertTrue(same(x4, 91.875, 1.0));   
    }

// org.jfree.chart.axis.junit.DateAxisTests::testSerialization
    public void testSerialization() {

        DateAxis a1 = new DateAxis("Test Axis");
        DateAxis a2 = null;

        try {
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            ObjectOutput out = new ObjectOutputStream(buffer);
            out.writeObject(a1);
            out.close();

            ObjectInput in = new ObjectInputStream(
                    new ByteArrayInputStream(buffer.toByteArray()));
            a2 = (DateAxis) in.readObject();
            in.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        boolean b = a1.equals(a2);
        assertTrue(b);

    }

// org.jfree.chart.axis.junit.DateAxisTests::testPreviousStandardDateYearA
    public void testPreviousStandardDateYearA() {
        MyDateAxis axis = new MyDateAxis("Year");
        Year y2006 = new Year(2006);
        Year y2007 = new Year(2007);
        
        
        Date d0 = new Date(y2006.getFirstMillisecond());
        Date d1 = new Date(y2006.getFirstMillisecond() + 500L);
        Date d2 = new Date(y2006.getMiddleMillisecond());
        Date d3 = new Date(y2006.getMiddleMillisecond() + 500L);
        Date d4 = new Date(y2006.getLastMillisecond());
        
        Date end = new Date(y2007.getLastMillisecond());
        
        DateTickUnit unit = new DateTickUnit(DateTickUnit.YEAR, 1);
        axis.setTickUnit(unit);

        
        axis.setTickMarkPosition(DateTickMarkPosition.START);

        axis.setRange(d0, end);
        Date psd = axis.previousStandardDate(d0, unit);
        Date nsd = unit.addToDate(psd);
        assertTrue(psd.getTime() < d0.getTime());
        assertTrue(nsd.getTime() >= d0.getTime());

        axis.setRange(d1, end);
        psd = axis.previousStandardDate(d1, unit);
        nsd = unit.addToDate(psd);
        assertTrue(psd.getTime() < d1.getTime());
        assertTrue(nsd.getTime() >= d1.getTime());

        
        axis.setTickMarkPosition(DateTickMarkPosition.MIDDLE);
        
        axis.setRange(d1, end);
        psd = axis.previousStandardDate(d1, unit);
        nsd = unit.addToDate(psd);
        assertTrue(psd.getTime() < d1.getTime());
        assertTrue(nsd.getTime() >= d1.getTime());
        
        axis.setRange(d2, end);
        psd = axis.previousStandardDate(d2, unit);
        nsd = unit.addToDate(psd);
        assertTrue(psd.getTime() < d2.getTime());
        assertTrue(nsd.getTime() >= d2.getTime());

        axis.setRange(d3, end);
        psd = axis.previousStandardDate(d3, unit);
        nsd = unit.addToDate(psd);
        assertTrue(psd.getTime() < d3.getTime());
        assertTrue(nsd.getTime() >= d3.getTime());

        
        axis.setTickMarkPosition(DateTickMarkPosition.END);
        
        axis.setRange(d3, end);
        psd = axis.previousStandardDate(d3, unit);
        nsd = unit.addToDate(psd);
        assertTrue(psd.getTime() < d3.getTime());
        assertTrue(nsd.getTime() >= d3.getTime());
        
        axis.setRange(d4, end);
        psd = axis.previousStandardDate(d4, unit);
        nsd = unit.addToDate(psd);
        assertTrue(psd.getTime() < d4.getTime());
        assertTrue(nsd.getTime() >= d4.getTime());
    }

// org.jfree.chart.axis.junit.DateAxisTests::testPreviousStandardDateYearB
    public void testPreviousStandardDateYearB() {
        MyDateAxis axis = new MyDateAxis("Year");
        Year y2006 = new Year(2006);
        Year y2007 = new Year(2007);
        
        
        Date d0 = new Date(y2006.getFirstMillisecond());
        Date d1 = new Date(y2006.getFirstMillisecond() + 500L);
        Date d2 = new Date(y2006.getMiddleMillisecond());
        Date d3 = new Date(y2006.getMiddleMillisecond() + 500L);
        Date d4 = new Date(y2006.getLastMillisecond());
        
        Date end = new Date(y2007.getLastMillisecond());
        
        DateTickUnit unit = new DateTickUnit(DateTickUnit.YEAR, 10);
        axis.setTickUnit(unit);

        
        axis.setTickMarkPosition(DateTickMarkPosition.START);

        axis.setRange(d0, end);
        Date psd = axis.previousStandardDate(d0, unit);
        Date nsd = unit.addToDate(psd);
        assertTrue(psd.getTime() < d0.getTime());
        assertTrue(nsd.getTime() >= d0.getTime());

        axis.setRange(d1, end);
        psd = axis.previousStandardDate(d1, unit);
        nsd = unit.addToDate(psd);
        assertTrue(psd.getTime() < d1.getTime());
        assertTrue(nsd.getTime() >= d1.getTime());

        
        axis.setTickMarkPosition(DateTickMarkPosition.MIDDLE);
        
        axis.setRange(d1, end);
        psd = axis.previousStandardDate(d1, unit);
        nsd = unit.addToDate(psd);
        assertTrue(psd.getTime() < d1.getTime());
        assertTrue(nsd.getTime() >= d1.getTime());
        
        axis.setRange(d2, end);
        psd = axis.previousStandardDate(d2, unit);
        nsd = unit.addToDate(psd);
        assertTrue(psd.getTime() < d2.getTime());
        assertTrue(nsd.getTime() >= d2.getTime());

        axis.setRange(d3, end);
        psd = axis.previousStandardDate(d3, unit);
        nsd = unit.addToDate(psd);
        assertTrue(psd.getTime() < d3.getTime());
        assertTrue(nsd.getTime() >= d3.getTime());

        
        axis.setTickMarkPosition(DateTickMarkPosition.END);
        
        axis.setRange(d3, end);
        psd = axis.previousStandardDate(d3, unit);
        nsd = unit.addToDate(psd);
        assertTrue(psd.getTime() < d3.getTime());
        assertTrue(nsd.getTime() >= d3.getTime());
        
        axis.setRange(d4, end);
        psd = axis.previousStandardDate(d4, unit);
        nsd = unit.addToDate(psd);
        assertTrue(psd.getTime() < d4.getTime());
        assertTrue(nsd.getTime() >= d4.getTime());
    }

// org.jfree.chart.axis.junit.DateAxisTests::testPreviousStandardDateMonthA
    public void testPreviousStandardDateMonthA() {
        MyDateAxis axis = new MyDateAxis("Month");
        Month nov2006 = new Month(11, 2006);
        Month dec2006 = new Month(12, 2006);
        
        
        Date d0 = new Date(nov2006.getFirstMillisecond());
        Date d1 = new Date(nov2006.getFirstMillisecond() + 500L);
        Date d2 = new Date(nov2006.getMiddleMillisecond());
        Date d3 = new Date(nov2006.getMiddleMillisecond() + 500L);
        Date d4 = new Date(nov2006.getLastMillisecond());
        
        Date end = new Date(dec2006.getLastMillisecond());
        
        DateTickUnit unit = new DateTickUnit(DateTickUnit.MONTH, 1);
        axis.setTickUnit(unit);

        
        axis.setTickMarkPosition(DateTickMarkPosition.START);

        axis.setRange(d0, end);
        Date psd = axis.previousStandardDate(d0, unit);
        Date nsd = unit.addToDate(psd);
        assertTrue(psd.getTime() < d0.getTime());
        assertTrue(nsd.getTime() >= d0.getTime());

        axis.setRange(d1, end);
        psd = axis.previousStandardDate(d1, unit);
        nsd = unit.addToDate(psd);
        assertTrue(psd.getTime() < d1.getTime());
        assertTrue(nsd.getTime() >= d1.getTime());

        
        axis.setTickMarkPosition(DateTickMarkPosition.MIDDLE);
        
        axis.setRange(d1, end);
        psd = axis.previousStandardDate(d1, unit);
        nsd = unit.addToDate(psd);
        assertTrue(psd.getTime() < d1.getTime());
        assertTrue(nsd.getTime() >= d1.getTime());
        
        axis.setRange(d2, end);
        psd = axis.previousStandardDate(d2, unit);
        nsd = unit.addToDate(psd);
        assertTrue(psd.getTime() < d2.getTime());
        assertTrue(nsd.getTime() >= d2.getTime());

        axis.setRange(d3, end);
        psd = axis.previousStandardDate(d3, unit);
        nsd = unit.addToDate(psd);
        assertTrue(psd.getTime() < d3.getTime());
        assertTrue(nsd.getTime() >= d3.getTime());

        
        axis.setTickMarkPosition(DateTickMarkPosition.END);
        
        axis.setRange(d3, end);
        psd = axis.previousStandardDate(d3, unit);
        nsd = unit.addToDate(psd);
        assertTrue(psd.getTime() < d3.getTime());
        assertTrue(nsd.getTime() >= d3.getTime());
        
        axis.setRange(d4, end);
        psd = axis.previousStandardDate(d4, unit);
        nsd = unit.addToDate(psd);
        assertTrue(psd.getTime() < d4.getTime());
        assertTrue(nsd.getTime() >= d4.getTime());
    }

// org.jfree.chart.axis.junit.DateAxisTests::testPreviousStandardDateMonthB
    public void testPreviousStandardDateMonthB() {
        MyDateAxis axis = new MyDateAxis("Month");
        Month nov2006 = new Month(11, 2006);
        Month dec2006 = new Month(12, 2006);
        
        
        Date d0 = new Date(nov2006.getFirstMillisecond());
        Date d1 = new Date(nov2006.getFirstMillisecond() + 500L);
        Date d2 = new Date(nov2006.getMiddleMillisecond());
        Date d3 = new Date(nov2006.getMiddleMillisecond() + 500L);
        Date d4 = new Date(nov2006.getLastMillisecond());
        
        Date end = new Date(dec2006.getLastMillisecond());
        
        DateTickUnit unit = new DateTickUnit(DateTickUnit.MONTH, 3);
        axis.setTickUnit(unit);

        
        axis.setTickMarkPosition(DateTickMarkPosition.START);

        axis.setRange(d0, end);
        Date psd = axis.previousStandardDate(d0, unit);
        Date nsd = unit.addToDate(psd);
        assertTrue(psd.getTime() < d0.getTime());
        assertTrue(nsd.getTime() >= d0.getTime());

        axis.setRange(d1, end);
        psd = axis.previousStandardDate(d1, unit);
        nsd = unit.addToDate(psd);
        assertTrue(psd.getTime() < d1.getTime());
        assertTrue(nsd.getTime() >= d1.getTime());

        
        axis.setTickMarkPosition(DateTickMarkPosition.MIDDLE);
        
        axis.setRange(d1, end);
        psd = axis.previousStandardDate(d1, unit);
        nsd = unit.addToDate(psd);
        assertTrue(psd.getTime() < d1.getTime());
        assertTrue(nsd.getTime() >= d1.getTime());
        
        axis.setRange(d2, end);
        psd = axis.previousStandardDate(d2, unit);
        nsd = unit.addToDate(psd);
        assertTrue(psd.getTime() < d2.getTime());
        assertTrue(nsd.getTime() >= d2.getTime());

        axis.setRange(d3, end);
        psd = axis.previousStandardDate(d3, unit);
        nsd = unit.addToDate(psd);
        assertTrue(psd.getTime() < d3.getTime());
        assertTrue(nsd.getTime() >= d3.getTime());

        
        axis.setTickMarkPosition(DateTickMarkPosition.END);
        
        axis.setRange(d3, end);
        psd = axis.previousStandardDate(d3, unit);
        nsd = unit.addToDate(psd);
        assertTrue(psd.getTime() < d3.getTime());
        assertTrue(nsd.getTime() >= d3.getTime());
        
        axis.setRange(d4, end);
        psd = axis.previousStandardDate(d4, unit);
        nsd = unit.addToDate(psd);
        assertTrue(psd.getTime() < d4.getTime());
        assertTrue(nsd.getTime() >= d4.getTime());
    }

// org.jfree.chart.axis.junit.DateAxisTests::testPreviousStandardDateDayA
    public void testPreviousStandardDateDayA() {
        MyDateAxis axis = new MyDateAxis("Day");
        Day apr12007 = new Day(1, 4, 2007);
        Day apr22007 = new Day(2, 4, 2007);
        
        
        Date d0 = new Date(apr12007.getFirstMillisecond());
        Date d1 = new Date(apr12007.getFirstMillisecond() + 500L);
        Date d2 = new Date(apr12007.getMiddleMillisecond());
        Date d3 = new Date(apr12007.getMiddleMillisecond() + 500L);
        Date d4 = new Date(apr12007.getLastMillisecond());
     
        Date end = new Date(apr22007.getLastMillisecond());
        
        DateTickUnit unit = new DateTickUnit(DateTickUnit.DAY, 1);
        axis.setTickUnit(unit);

        
        axis.setTickMarkPosition(DateTickMarkPosition.START);

        axis.setRange(d0, end);
        Date psd = axis.previousStandardDate(d0, unit);
        Date nsd = unit.addToDate(psd);
        assertTrue(psd.getTime() < d0.getTime());
        assertTrue(nsd.getTime() >= d0.getTime());

        axis.setRange(d1, end);
        psd = axis.previousStandardDate(d1, unit);
        nsd = unit.addToDate(psd);
        assertTrue(psd.getTime() < d1.getTime());
        assertTrue(nsd.getTime() >= d1.getTime());

        
        axis.setTickMarkPosition(DateTickMarkPosition.MIDDLE);
        
        axis.setRange(d1, end);
        psd = axis.previousStandardDate(d1, unit);
        nsd = unit.addToDate(psd);
        assertTrue(psd.getTime() < d1.getTime());
        assertTrue(nsd.getTime() >= d1.getTime());
        
        axis.setRange(d2, end);
        psd = axis.previousStandardDate(d2, unit);
        nsd = unit.addToDate(psd);
        assertTrue(psd.getTime() < d2.getTime());
        assertTrue(nsd.getTime() >= d2.getTime());

        axis.setRange(d3, end);
        psd = axis.previousStandardDate(d3, unit);
        nsd = unit.addToDate(psd);
        assertTrue(psd.getTime() < d3.getTime());
        assertTrue(nsd.getTime() >= d3.getTime());

        
        axis.setTickMarkPosition(DateTickMarkPosition.END);
        
        axis.setRange(d3, end);
        psd = axis.previousStandardDate(d3, unit);
        nsd = unit.addToDate(psd);
        assertTrue(psd.getTime() < d3.getTime());
        assertTrue(nsd.getTime() >= d3.getTime());
        
        axis.setRange(d4, end);
        psd = axis.previousStandardDate(d4, unit);
        nsd = unit.addToDate(psd);
        assertTrue(psd.getTime() < d4.getTime());
        assertTrue(nsd.getTime() >= d4.getTime());
    }

// org.jfree.chart.axis.junit.DateAxisTests::testPreviousStandardDateDayB
    public void testPreviousStandardDateDayB() {
        MyDateAxis axis = new MyDateAxis("Day");
        Day apr12007 = new Day(1, 4, 2007);
        Day apr22007 = new Day(2, 4, 2007);
        
        
        Date d0 = new Date(apr12007.getFirstMillisecond());
        Date d1 = new Date(apr12007.getFirstMillisecond() + 500L);
        Date d2 = new Date(apr12007.getMiddleMillisecond());
        Date d3 = new Date(apr12007.getMiddleMillisecond() + 500L);
        Date d4 = new Date(apr12007.getLastMillisecond());
     
        Date end = new Date(apr22007.getLastMillisecond());
        
        DateTickUnit unit = new DateTickUnit(DateTickUnit.DAY, 7);
        axis.setTickUnit(unit);

        
        axis.setTickMarkPosition(DateTickMarkPosition.START);

        axis.setRange(d0, end);
        Date psd = axis.previousStandardDate(d0, unit);
        Date nsd = unit.addToDate(psd);
        assertTrue(psd.getTime() < d0.getTime());
        assertTrue(nsd.getTime() >= d0.getTime());

        axis.setRange(d1, end);
        psd = axis.previousStandardDate(d1, unit);
        nsd = unit.addToDate(psd);
        assertTrue(psd.getTime() < d1.getTime());
        assertTrue(nsd.getTime() >= d1.getTime());

        
        axis.setTickMarkPosition(DateTickMarkPosition.MIDDLE);
        
        axis.setRange(d1, end);
        psd = axis.previousStandardDate(d1, unit);
        nsd = unit.addToDate(psd);
        assertTrue(psd.getTime() < d1.getTime());
        assertTrue(nsd.getTime() >= d1.getTime());
        
        axis.setRange(d2, end);
        psd = axis.previousStandardDate(d2, unit);
        nsd = unit.addToDate(psd);
        assertTrue(psd.getTime() < d2.getTime());
        assertTrue(nsd.getTime() >= d2.getTime());

        axis.setRange(d3, end);
        psd = axis.previousStandardDate(d3, unit);
        nsd = unit.addToDate(psd);
        assertTrue(psd.getTime() < d3.getTime());
        assertTrue(nsd.getTime() >= d3.getTime());

        
        axis.setTickMarkPosition(DateTickMarkPosition.END);
        
        axis.setRange(d3, end);
        psd = axis.previousStandardDate(d3, unit);
        nsd = unit.addToDate(psd);
        assertTrue(psd.getTime() < d3.getTime());
        assertTrue(nsd.getTime() >= d3.getTime());
        
        axis.setRange(d4, end);
        psd = axis.previousStandardDate(d4, unit);
        nsd = unit.addToDate(psd);
        assertTrue(psd.getTime() < d4.getTime());
        assertTrue(nsd.getTime() >= d4.getTime());
    }

// org.jfree.chart.axis.junit.DateAxisTests::testPreviousStandardDateHourA
    public void testPreviousStandardDateHourA() {
        MyDateAxis axis = new MyDateAxis("Hour");
        Hour h0 = new Hour(12, 1, 4, 2007);
        Hour h1 = new Hour(13, 1, 4, 2007);
        
        
        Date d0 = new Date(h0.getFirstMillisecond());
        Date d1 = new Date(h0.getFirstMillisecond() + 500L);
        Date d2 = new Date(h0.getMiddleMillisecond());
        Date d3 = new Date(h0.getMiddleMillisecond() + 500L);
        Date d4 = new Date(h0.getLastMillisecond());
     
        Date end = new Date(h1.getLastMillisecond());
        
        DateTickUnit unit = new DateTickUnit(DateTickUnit.HOUR, 1);
        axis.setTickUnit(unit);

        
        axis.setTickMarkPosition(DateTickMarkPosition.START);

        axis.setRange(d0, end);
        Date psd = axis.previousStandardDate(d0, unit);
        Date nsd = unit.addToDate(psd);
        assertTrue(psd.getTime() < d0.getTime());
        assertTrue(nsd.getTime() >= d0.getTime());

        axis.setRange(d1, end);
        psd = axis.previousStandardDate(d1, unit);
        nsd = unit.addToDate(psd);
        assertTrue(psd.getTime() < d1.getTime());
        assertTrue(nsd.getTime() >= d1.getTime());

        
        axis.setTickMarkPosition(DateTickMarkPosition.MIDDLE);
        
        axis.setRange(d1, end);
        psd = axis.previousStandardDate(d1, unit);
        nsd = unit.addToDate(psd);
        assertTrue(psd.getTime() < d1.getTime());
        assertTrue(nsd.getTime() >= d1.getTime());
        
        axis.setRange(d2, end);
        psd = axis.previousStandardDate(d2, unit);
        nsd = unit.addToDate(psd);
        assertTrue(psd.getTime() < d2.getTime());
        assertTrue(nsd.getTime() >= d2.getTime());

        axis.setRange(d3, end);
        psd = axis.previousStandardDate(d3, unit);
        nsd = unit.addToDate(psd);
        assertTrue(psd.getTime() < d3.getTime());
        assertTrue(nsd.getTime() >= d3.getTime());

        
        axis.setTickMarkPosition(DateTickMarkPosition.END);
        
        axis.setRange(d3, end);
        psd = axis.previousStandardDate(d3, unit);
        nsd = unit.addToDate(psd);
        assertTrue(psd.getTime() < d3.getTime());
        assertTrue(nsd.getTime() >= d3.getTime());
        
        axis.setRange(d4, end);
        psd = axis.previousStandardDate(d4, unit);
        nsd = unit.addToDate(psd);
        assertTrue(psd.getTime() < d4.getTime());
        assertTrue(nsd.getTime() >= d4.getTime());
    }

// org.jfree.chart.axis.junit.DateAxisTests::testPreviousStandardDateHourB
    public void testPreviousStandardDateHourB() {
        MyDateAxis axis = new MyDateAxis("Hour");
        Hour h0 = new Hour(12, 1, 4, 2007);
        Hour h1 = new Hour(13, 1, 4, 2007);
        
        
        Date d0 = new Date(h0.getFirstMillisecond());
        Date d1 = new Date(h0.getFirstMillisecond() + 500L);
        Date d2 = new Date(h0.getMiddleMillisecond());
        Date d3 = new Date(h0.getMiddleMillisecond() + 500L);
        Date d4 = new Date(h0.getLastMillisecond());
     
        Date end = new Date(h1.getLastMillisecond());
        
        DateTickUnit unit = new DateTickUnit(DateTickUnit.HOUR, 6);
        axis.setTickUnit(unit);

        
        axis.setTickMarkPosition(DateTickMarkPosition.START);

        axis.setRange(d0, end);
        Date psd = axis.previousStandardDate(d0, unit);
        Date nsd = unit.addToDate(psd);
        assertTrue(psd.getTime() < d0.getTime());
        assertTrue(nsd.getTime() >= d0.getTime());

        axis.setRange(d1, end);
        psd = axis.previousStandardDate(d1, unit);
        nsd = unit.addToDate(psd);
        assertTrue(psd.getTime() < d1.getTime());
        assertTrue(nsd.getTime() >= d1.getTime());

        
        axis.setTickMarkPosition(DateTickMarkPosition.MIDDLE);
        
        axis.setRange(d1, end);
        psd = axis.previousStandardDate(d1, unit);
        nsd = unit.addToDate(psd);
        assertTrue(psd.getTime() < d1.getTime());
        assertTrue(nsd.getTime() >= d1.getTime());
        
        axis.setRange(d2, end);
        psd = axis.previousStandardDate(d2, unit);
        nsd = unit.addToDate(psd);
        assertTrue(psd.getTime() < d2.getTime());
        assertTrue(nsd.getTime() >= d2.getTime());

        axis.setRange(d3, end);
        psd = axis.previousStandardDate(d3, unit);
        nsd = unit.addToDate(psd);
        assertTrue(psd.getTime() < d3.getTime());
        assertTrue(nsd.getTime() >= d3.getTime());

        
        axis.setTickMarkPosition(DateTickMarkPosition.END);
        
        axis.setRange(d3, end);
        psd = axis.previousStandardDate(d3, unit);
        nsd = unit.addToDate(psd);
        assertTrue(psd.getTime() < d3.getTime());
        assertTrue(nsd.getTime() >= d3.getTime());
        
        axis.setRange(d4, end);
        psd = axis.previousStandardDate(d4, unit);
        nsd = unit.addToDate(psd);
        assertTrue(psd.getTime() < d4.getTime());
        assertTrue(nsd.getTime() >= d4.getTime());
    }

// org.jfree.chart.axis.junit.DateAxisTests::testPreviousStandardDateSecondA
    public void testPreviousStandardDateSecondA() {
        MyDateAxis axis = new MyDateAxis("Second");
        Second s0 = new Second(58, 31, 12, 1, 4, 2007);
        Second s1 = new Second(59, 31, 12, 1, 4, 2007);
        
        
        Date d0 = new Date(s0.getFirstMillisecond());
        Date d1 = new Date(s0.getFirstMillisecond() + 50L);
        Date d2 = new Date(s0.getMiddleMillisecond());
        Date d3 = new Date(s0.getMiddleMillisecond() + 50L);
        Date d4 = new Date(s0.getLastMillisecond());
     
        Date end = new Date(s1.getLastMillisecond());
        
        DateTickUnit unit = new DateTickUnit(DateTickUnit.SECOND, 1);
        axis.setTickUnit(unit);

        
        axis.setTickMarkPosition(DateTickMarkPosition.START);

        axis.setRange(d0, end);
        Date psd = axis.previousStandardDate(d0, unit);
        Date nsd = unit.addToDate(psd);
        assertTrue(psd.getTime() < d0.getTime());
        assertTrue(nsd.getTime() >= d0.getTime());

        axis.setRange(d1, end);
        psd = axis.previousStandardDate(d1, unit);
        nsd = unit.addToDate(psd);
        assertTrue(psd.getTime() < d1.getTime());
        assertTrue(nsd.getTime() >= d1.getTime());

        
        axis.setTickMarkPosition(DateTickMarkPosition.MIDDLE);
        
        axis.setRange(d1, end);
        psd = axis.previousStandardDate(d1, unit);
        nsd = unit.addToDate(psd);
        assertTrue(psd.getTime() < d1.getTime());
        assertTrue(nsd.getTime() >= d1.getTime());
        
        axis.setRange(d2, end);
        psd = axis.previousStandardDate(d2, unit);
        nsd = unit.addToDate(psd);
        assertTrue(psd.getTime() < d2.getTime());
        assertTrue(nsd.getTime() >= d2.getTime());

        axis.setRange(d3, end);
        psd = axis.previousStandardDate(d3, unit);
        nsd = unit.addToDate(psd);
        assertTrue(psd.getTime() < d3.getTime());
        assertTrue(nsd.getTime() >= d3.getTime());

        
        axis.setTickMarkPosition(DateTickMarkPosition.END);
        
        axis.setRange(d3, end);
        psd = axis.previousStandardDate(d3, unit);
        nsd = unit.addToDate(psd);
        assertTrue(psd.getTime() < d3.getTime());
        assertTrue(nsd.getTime() >= d3.getTime());
        
        axis.setRange(d4, end);
        psd = axis.previousStandardDate(d4, unit);
        nsd = unit.addToDate(psd);
        assertTrue(psd.getTime() < d4.getTime());
        assertTrue(nsd.getTime() >= d4.getTime());
    }

// org.jfree.chart.axis.junit.DateAxisTests::testPreviousStandardDateSecondB
    public void testPreviousStandardDateSecondB() {
        MyDateAxis axis = new MyDateAxis("Second");
        Second s0 = new Second(58, 31, 12, 1, 4, 2007);
        Second s1 = new Second(59, 31, 12, 1, 4, 2007);
        
        
        Date d0 = new Date(s0.getFirstMillisecond());
        Date d1 = new Date(s0.getFirstMillisecond() + 50L);
        Date d2 = new Date(s0.getMiddleMillisecond());
        Date d3 = new Date(s0.getMiddleMillisecond() + 50L);
        Date d4 = new Date(s0.getLastMillisecond());
     
        Date end = new Date(s1.getLastMillisecond());
        
        DateTickUnit unit = new DateTickUnit(DateTickUnit.SECOND, 5);
        axis.setTickUnit(unit);

        
        axis.setTickMarkPosition(DateTickMarkPosition.START);

        axis.setRange(d0, end);
        Date psd = axis.previousStandardDate(d0, unit);
        Date nsd = unit.addToDate(psd);
        assertTrue(psd.getTime() < d0.getTime());
        assertTrue(nsd.getTime() >= d0.getTime());

        axis.setRange(d1, end);
        psd = axis.previousStandardDate(d1, unit);
        nsd = unit.addToDate(psd);
        assertTrue(psd.getTime() < d1.getTime());
        assertTrue(nsd.getTime() >= d1.getTime());

        
        axis.setTickMarkPosition(DateTickMarkPosition.MIDDLE);
        
        axis.setRange(d1, end);
        psd = axis.previousStandardDate(d1, unit);
        nsd = unit.addToDate(psd);
        assertTrue(psd.getTime() < d1.getTime());
        assertTrue(nsd.getTime() >= d1.getTime());
        
        axis.setRange(d2, end);
        psd = axis.previousStandardDate(d2, unit);
        nsd = unit.addToDate(psd);
        assertTrue(psd.getTime() < d2.getTime());
        assertTrue(nsd.getTime() >= d2.getTime());

        axis.setRange(d3, end);
        psd = axis.previousStandardDate(d3, unit);
        nsd = unit.addToDate(psd);
        assertTrue(psd.getTime() < d3.getTime());
        assertTrue(nsd.getTime() >= d3.getTime());

        
        axis.setTickMarkPosition(DateTickMarkPosition.END);
        
        axis.setRange(d3, end);
        psd = axis.previousStandardDate(d3, unit);
        nsd = unit.addToDate(psd);
        assertTrue(psd.getTime() < d3.getTime());
        assertTrue(nsd.getTime() >= d3.getTime());
        
        axis.setRange(d4, end);
        psd = axis.previousStandardDate(d4, unit);
        nsd = unit.addToDate(psd);
        assertTrue(psd.getTime() < d4.getTime());
        assertTrue(nsd.getTime() >= d4.getTime());
    }

// org.jfree.chart.axis.junit.DateAxisTests::testPreviousStandardDateMillisecondA
    public void testPreviousStandardDateMillisecondA() {
        MyDateAxis axis = new MyDateAxis("Millisecond");
        Millisecond m0 = new Millisecond(458, 58, 31, 12, 1, 4, 2007);
        Millisecond m1 = new Millisecond(459, 58, 31, 12, 1, 4, 2007);

        Date d0 = new Date(m0.getFirstMillisecond());
        Date end = new Date(m1.getLastMillisecond());
        
        DateTickUnit unit = new DateTickUnit(DateTickUnit.MILLISECOND, 1);
        axis.setTickUnit(unit);

        
        axis.setTickMarkPosition(DateTickMarkPosition.START);

        axis.setRange(d0, end);
        Date psd = axis.previousStandardDate(d0, unit);
        Date nsd = unit.addToDate(psd);
        assertTrue(psd.getTime() < d0.getTime());
        assertTrue(nsd.getTime() >= d0.getTime());

        
        axis.setTickMarkPosition(DateTickMarkPosition.MIDDLE);
        
        axis.setRange(d0, end);
        psd = axis.previousStandardDate(d0, unit);
        nsd = unit.addToDate(psd);
        assertTrue(psd.getTime() < d0.getTime());
        assertTrue(nsd.getTime() >= d0.getTime());
        
        
        axis.setTickMarkPosition(DateTickMarkPosition.END);
        
        axis.setRange(d0, end);
        psd = axis.previousStandardDate(d0, unit);
        nsd = unit.addToDate(psd);
        assertTrue(psd.getTime() < d0.getTime());
        assertTrue(nsd.getTime() >= d0.getTime());
    }

// org.jfree.chart.axis.junit.DateAxisTests::testPreviousStandardDateMillisecondB
    public void testPreviousStandardDateMillisecondB() {
        MyDateAxis axis = new MyDateAxis("Millisecond");
        Millisecond m0 = new Millisecond(458, 58, 31, 12, 1, 4, 2007);
        Millisecond m1 = new Millisecond(459, 58, 31, 12, 1, 4, 2007);

        Date d0 = new Date(m0.getFirstMillisecond());
        Date end = new Date(m1.getLastMillisecond());
        
        DateTickUnit unit = new DateTickUnit(DateTickUnit.MILLISECOND, 10);
        axis.setTickUnit(unit);

        
        axis.setTickMarkPosition(DateTickMarkPosition.START);

        axis.setRange(d0, end);
        Date psd = axis.previousStandardDate(d0, unit);
        Date nsd = unit.addToDate(psd);
        assertTrue(psd.getTime() < d0.getTime());
        assertTrue(nsd.getTime() >= d0.getTime());

        
        axis.setTickMarkPosition(DateTickMarkPosition.MIDDLE);
        
        axis.setRange(d0, end);
        psd = axis.previousStandardDate(d0, unit);
        nsd = unit.addToDate(psd);
        assertTrue(psd.getTime() < d0.getTime());
        assertTrue(nsd.getTime() >= d0.getTime());
        
        
        axis.setTickMarkPosition(DateTickMarkPosition.END);
        
        axis.setRange(d0, end);
        psd = axis.previousStandardDate(d0, unit);
        nsd = unit.addToDate(psd);
        assertTrue(psd.getTime() < d0.getTime());
        assertTrue(nsd.getTime() >= d0.getTime());
    }

// org.jfree.chart.axis.junit.ExtendedCategoryAxisTests::testEquals
    public void testEquals() {
        
        ExtendedCategoryAxis a1 = new ExtendedCategoryAxis("Test");
        ExtendedCategoryAxis a2 = new ExtendedCategoryAxis("Test");
        assertTrue(a1.equals(a2));
        
        a1.addSubLabel("C1", "C1-sublabel");
        assertFalse(a1.equals(a2));
        a2.addSubLabel("C1", "C1-sublabel");
        assertTrue(a1.equals(a2));
        
        a1.setSubLabelFont(new Font("Dialog", Font.BOLD, 8));
        assertFalse(a1.equals(a2));
        a2.setSubLabelFont(new Font("Dialog", Font.BOLD, 8));
        assertTrue(a1.equals(a2));
        
        a1.setSubLabelPaint(Color.red);
        assertFalse(a1.equals(a2));
        a2.setSubLabelPaint(Color.red);
        assertTrue(a1.equals(a2));
    }

// org.jfree.chart.axis.junit.ExtendedCategoryAxisTests::testHashCode
    public void testHashCode() {
        ExtendedCategoryAxis a1 = new ExtendedCategoryAxis("Test");
        ExtendedCategoryAxis a2 = new ExtendedCategoryAxis("Test");
        assertTrue(a1.equals(a2));
        int h1 = a1.hashCode();
        int h2 = a2.hashCode();
        assertEquals(h1, h2);
    }

// org.jfree.chart.axis.junit.ExtendedCategoryAxisTests::testCloning
    public void testCloning() {
        ExtendedCategoryAxis a1 = new ExtendedCategoryAxis("Test");
        ExtendedCategoryAxis a2 = null;
        try {
            a2 = (ExtendedCategoryAxis) a1.clone();
        }
        catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        assertTrue(a1 != a2);
        assertTrue(a1.getClass() == a2.getClass());
        assertTrue(a1.equals(a2));
        
        
        a1.addSubLabel("C1", "ABC");
        assertFalse(a1.equals(a2));
        a2.addSubLabel("C1", "ABC");
        assertTrue(a1.equals(a2));
        
    }

// org.jfree.chart.axis.junit.ExtendedCategoryAxisTests::testCloning2
    public void testCloning2() {
        ExtendedCategoryAxis a1 = new ExtendedCategoryAxis("Test");
        a1.setTickLabelFont("C1", new Font("Dialog", Font.PLAIN, 15));
        a1.setTickLabelPaint("C1", new GradientPaint(1.0f, 2.0f, Color.red, 
                3.0f, 4.0f, Color.white));
        ExtendedCategoryAxis a2 = null;
        try {
            a2 = (ExtendedCategoryAxis) a1.clone();
        }
        catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        assertTrue(a1 != a2);
        assertTrue(a1.getClass() == a2.getClass());
        assertTrue(a1.equals(a2));
        
        
        a1.setTickLabelFont("C1", null);
        assertFalse(a1.equals(a2));
        a2.setTickLabelFont("C1", null);
        assertTrue(a1.equals(a2));
        
        
        a1.setTickLabelPaint("C1", Color.yellow);
        assertFalse(a1.equals(a2));
        a2.setTickLabelPaint("C1", Color.yellow);
        assertTrue(a1.equals(a2));

        
        a1.addCategoryLabelToolTip("C1", "XYZ");
        assertFalse(a1.equals(a2));
        a2.addCategoryLabelToolTip("C1", "XYZ");
        assertTrue(a1.equals(a2));
    }

// org.jfree.chart.axis.junit.ExtendedCategoryAxisTests::testSerialization
    public void testSerialization() {
        ExtendedCategoryAxis a1 = new ExtendedCategoryAxis("Test");
        a1.setSubLabelPaint(new GradientPaint(1.0f, 2.0f, Color.red, 3.0f, 
                4.0f, Color.blue));
        ExtendedCategoryAxis a2 = null;

        try {
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            ObjectOutput out = new ObjectOutputStream(buffer);
            out.writeObject(a1);
            out.close();

            ObjectInput in = new ObjectInputStream(
                    new ByteArrayInputStream(buffer.toByteArray()));
            a2 = (ExtendedCategoryAxis) in.readObject();
            in.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        assertEquals(a1, a2);
    }

// org.jfree.chart.axis.junit.LogarithmicAxisTests::testSerialization
    public void testSerialization() {

        LogarithmicAxis a1 = new LogarithmicAxis("Test Axis");
        LogarithmicAxis a2 = null;

        try {
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            ObjectOutput out = new ObjectOutputStream(buffer);
            out.writeObject(a1);
            out.close();

            ObjectInput in = new ObjectInputStream(new ByteArrayInputStream(
                    buffer.toByteArray()));
            a2 = (LogarithmicAxis) in.readObject();
            in.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        assertEquals(a1, a2);

    }

// org.jfree.chart.axis.junit.LogarithmicAxisTests::testAdjustedLog10
     public void testAdjustedLog10() {
         checkLogPowRoundTrip(20);
         checkLogPowRoundTrip(10);
         checkLogPowRoundTrip(5);
         checkLogPowRoundTrip(2);
         checkLogPowRoundTrip(1);
         checkLogPowRoundTrip(0.5);
         checkLogPowRoundTrip(0.2);
         checkLogPowRoundTrip(0.0001);
     }

// org.jfree.chart.axis.junit.LogarithmicAxisTests::testSwitchedLog10
      public void testSwitchedLog10() {
          assertFalse("Axis should not allow negative values",
                  this.axis.getAllowNegativesFlag());
                
          assertEquals(Math.log(0.5) / LogarithmicAxis.LOG10_VALUE,
                  this.axis.switchedLog10(0.5), EPSILON);

          checkSwitchedLogPowRoundTrip(20);
          checkSwitchedLogPowRoundTrip(10);
          checkSwitchedLogPowRoundTrip(5);
          checkSwitchedLogPowRoundTrip(2);
          checkSwitchedLogPowRoundTrip(1);
          checkSwitchedLogPowRoundTrip(0.5);
          checkSwitchedLogPowRoundTrip(0.2);
          checkSwitchedLogPowRoundTrip(0.0001);
      }

// org.jfree.chart.axis.junit.LogarithmicAxisTests::testJava2DToValue
      public void testJava2DToValue() {
          Rectangle2D plotArea = new Rectangle2D.Double(22, 33, 500, 500);
          RectangleEdge edge = RectangleEdge.BOTTOM;

          
          this.axis.setRange(10, 20);
          checkPointsToValue(edge, plotArea);

          
          this.axis.setRange(0.5, 10);
          checkPointsToValue(edge, plotArea);

          
          this.axis.setRange(0.2, 20);
          checkPointsToValue(edge, plotArea);

          
          this.axis.setRange(0.2, 0.7);
          checkPointsToValue(edge, plotArea);
      }

// org.jfree.chart.axis.junit.LogarithmicAxisTests::testValueToJava2D
      public void testValueToJava2D() {
          Rectangle2D plotArea = new Rectangle2D.Double(22, 33, 500, 500);
          RectangleEdge edge = RectangleEdge.BOTTOM;

          
          this.axis.setRange(10, 20);
          checkPointsToJava2D(edge, plotArea);

          
          this.axis.setRange(0.5, 10);
          checkPointsToJava2D(edge, plotArea);

          
          this.axis.setRange(0.2, 20);
          checkPointsToJava2D(edge, plotArea);

          
          this.axis.setRange(0.2, 0.7);
          checkPointsToJava2D(edge, plotArea);
      }

// org.jfree.chart.axis.junit.MarkerAxisBandTests::testEquals
    public void testEquals() {        
        Font font1 = new Font("SansSerif", Font.PLAIN, 12);
        Font font2 = new Font("SansSerif", Font.PLAIN, 14);
        
        MarkerAxisBand a1 = new MarkerAxisBand(null, 1.0, 1.0, 1.0, 1.0, font1);
        MarkerAxisBand a2 = new MarkerAxisBand(null, 1.0, 1.0, 1.0, 1.0, font1);
        assertEquals(a1, a2);
        
        a1 = new MarkerAxisBand(null, 2.0, 1.0, 1.0, 1.0, font1);
        assertFalse(a1.equals(a2));
        a2 = new MarkerAxisBand(null, 2.0, 1.0, 1.0, 1.0, font1);
        assertTrue(a1.equals(a2));
        
        a1 = new MarkerAxisBand(null, 2.0, 3.0, 1.0, 1.0, font1);
        assertFalse(a1.equals(a2));
        a2 = new MarkerAxisBand(null, 2.0, 3.0, 1.0, 1.0, font1);
        assertTrue(a1.equals(a2));
        
        a1 = new MarkerAxisBand(null, 2.0, 3.0, 4.0, 1.0, font1);
        assertFalse(a1.equals(a2));
        a2 = new MarkerAxisBand(null, 2.0, 3.0, 4.0, 1.0, font1);
        assertTrue(a1.equals(a2));

        a1 = new MarkerAxisBand(null, 2.0, 3.0, 4.0, 5.0, font1);
        assertFalse(a1.equals(a2));
        a2 = new MarkerAxisBand(null, 2.0, 3.0, 4.0, 5.0, font1);
        assertTrue(a1.equals(a2));

        a1 = new MarkerAxisBand(null, 2.0, 3.0, 4.0, 5.0, font2);
        assertFalse(a1.equals(a2));
        a2 = new MarkerAxisBand(null, 2.0, 3.0, 4.0, 5.0, font2);
        assertTrue(a1.equals(a2));
    }

// org.jfree.chart.axis.junit.MarkerAxisBandTests::testHashCode
    public void testHashCode() {
        Font font1 = new Font("SansSerif", Font.PLAIN, 12);
        
        MarkerAxisBand a1 = new MarkerAxisBand(null, 1.0, 1.0, 1.0, 1.0, font1);
        MarkerAxisBand a2 = new MarkerAxisBand(null, 1.0, 1.0, 1.0, 1.0, font1);
         assertTrue(a1.equals(a2));
        int h1 = a1.hashCode();
        int h2 = a2.hashCode();
        assertEquals(h1, h2);
    }

// org.jfree.chart.axis.junit.MarkerAxisBandTests::testSerialization
    public void testSerialization() {

        MarkerAxisBand a1 = new MarkerAxisBand(null, 1.0, 1.0, 1.0, 1.0, null);
        MarkerAxisBand a2 = null;

        try {
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            ObjectOutput out = new ObjectOutputStream(buffer);
            out.writeObject(a1);
            out.close();

            ObjectInput in = new ObjectInputStream(
                new ByteArrayInputStream(buffer.toByteArray())
            );
            a2 = (MarkerAxisBand) in.readObject();
            in.close();
        }
        catch (Exception e) {
            System.out.println(e.toString());
        }
        assertEquals(a1, a2);

    }

// org.jfree.chart.axis.junit.NumberAxis3DTests::testSerialization
    public void testSerialization() {

        NumberAxis3D a1 = new NumberAxis3D("Test Axis");
        NumberAxis3D a2 = null;

        try {
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            ObjectOutput out = new ObjectOutputStream(buffer);
            out.writeObject(a1);
            out.close();

            ObjectInput in = new ObjectInputStream(
                new ByteArrayInputStream(buffer.toByteArray())
            );
            a2 = (NumberAxis3D) in.readObject();
            in.close();
        }
        catch (Exception e) {
            System.out.println(e.toString());
        }
        assertEquals(a1, a2);

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

// org.jfree.chart.axis.junit.PeriodAxisTests::testEquals
    public void testEquals() {
        
        PeriodAxis a1 = new PeriodAxis("Test");
        PeriodAxis a2 = new PeriodAxis("Test");
        assertTrue(a1.equals(a2));
        assertTrue(a2.equals(a1));
        
        a1.setFirst(new Year(2000));
        assertFalse(a1.equals(a2));
        a2.setFirst(new Year(2000));
        assertTrue(a1.equals(a2));
        
        a1.setLast(new Year(2004));
        assertFalse(a1.equals(a2));
        a2.setLast(new Year(2004));
        assertTrue(a1.equals(a2));

        a1.setTimeZone(TimeZone.getTimeZone("Pacific/Auckland"));
        assertFalse(a1.equals(a2));
        a2.setTimeZone(TimeZone.getTimeZone("Pacific/Auckland"));
        assertTrue(a1.equals(a2));
        
        a1.setAutoRangeTimePeriodClass(Quarter.class);
        assertFalse(a1.equals(a2));
        a2.setAutoRangeTimePeriodClass(Quarter.class);
        assertTrue(a1.equals(a2));
        
        PeriodAxisLabelInfo info[] = new PeriodAxisLabelInfo[1];
        info[0] = new PeriodAxisLabelInfo(
            Month.class, new SimpleDateFormat("MMM")
        );
        
        a1.setLabelInfo(info);
        assertFalse(a1.equals(a2));
        a2.setLabelInfo(info);
        assertTrue(a1.equals(a2));
        
        a1.setMajorTickTimePeriodClass(Minute.class);
        assertFalse(a1.equals(a2));
        a2.setMajorTickTimePeriodClass(Minute.class);
        assertTrue(a1.equals(a2));
        
        a1.setMinorTickMarksVisible(!a1.isMinorTickMarksVisible());
        assertFalse(a1.equals(a2));
        a2.setMinorTickMarksVisible(a1.isMinorTickMarksVisible());
        assertTrue(a1.equals(a2));
        
        a1.setMinorTickTimePeriodClass(Minute.class);
        assertFalse(a1.equals(a2));
        a2.setMinorTickTimePeriodClass(Minute.class);
        assertTrue(a1.equals(a2));

        Stroke s = new BasicStroke(1.23f);
        a1.setMinorTickMarkStroke(s);
        assertFalse(a1.equals(a2));
        a2.setMinorTickMarkStroke(s);
        assertTrue(a1.equals(a2));
    
        a1.setMinorTickMarkPaint(Color.blue);
        assertFalse(a1.equals(a2));
        a2.setMinorTickMarkPaint(Color.blue);
        assertTrue(a1.equals(a2));
    
    }

// org.jfree.chart.axis.junit.PeriodAxisTests::testHashCode
    public void testHashCode() {
        PeriodAxis a1 = new PeriodAxis("Test");
        PeriodAxis a2 = new PeriodAxis("Test");
        assertTrue(a1.equals(a2));
        int h1 = a1.hashCode();
        int h2 = a2.hashCode();
        assertEquals(h1, h2);
    }

// org.jfree.chart.axis.junit.PeriodAxisTests::testCloning
    public void testCloning() {
        PeriodAxis a1 = new PeriodAxis("Test");
        PeriodAxis a2 = null;
        try {
            a2 = (PeriodAxis) a1.clone();
        }
        catch (CloneNotSupportedException e) {
            System.err.println("Failed to clone.");
        }
        assertTrue(a1 != a2);
        assertTrue(a1.getClass() == a2.getClass());
        assertTrue(a1.equals(a2));
        
        
        a1.setLabel("New Label");
        assertFalse(a1.equals(a2));
        a2.setLabel("New Label");
        assertTrue(a1.equals(a2));
        
        a1.setFirst(new Year(1920));
        assertFalse(a1.equals(a2));
        a2.setFirst(new Year(1920));
        assertTrue(a1.equals(a2));

        a1.setLast(new Year(2020));
        assertFalse(a1.equals(a2));
        a2.setLast(new Year(2020));
        assertTrue(a1.equals(a2));

        PeriodAxisLabelInfo[] info = new PeriodAxisLabelInfo[2];
        info[0] = new PeriodAxisLabelInfo(Day.class, new SimpleDateFormat("d"));
        info[1] = new PeriodAxisLabelInfo(
            Year.class, new SimpleDateFormat("yyyy")
        );
        a1.setLabelInfo(info);
        assertFalse(a1.equals(a2));
        a2.setLabelInfo(info);
        assertTrue(a1.equals(a2));

        a1.setAutoRangeTimePeriodClass(Second.class);
        assertFalse(a1.equals(a2));
        a2.setAutoRangeTimePeriodClass(Second.class);
        assertTrue(a1.equals(a2));

        a1.setTimeZone(new SimpleTimeZone(123, "Bogus"));
        assertFalse(a1.equals(a2));
        a2.setTimeZone(new SimpleTimeZone(123, "Bogus"));
        assertTrue(a1.equals(a2));

    }

// org.jfree.chart.axis.junit.PeriodAxisTests::testSerialization
    public void testSerialization() {
        PeriodAxis a1 = new PeriodAxis("Test Axis");
        PeriodAxis a2 = null;
        try {
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            ObjectOutput out = new ObjectOutputStream(buffer);
            out.writeObject(a1);
            out.close();

            ObjectInput in = new ObjectInputStream(
                new ByteArrayInputStream(buffer.toByteArray())
            );
            a2 = (PeriodAxis) in.readObject();
            in.close();
        }
        catch (Exception e) {
            System.out.println(e.toString());
        }
        boolean b = a1.equals(a2);
        assertTrue(b);
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
        SubCategoryAxis a2 = null;
        try {
            a2 = (SubCategoryAxis) a1.clone();
        }
        catch (CloneNotSupportedException e) {
            System.err.println("Failed to clone.");
        }
        assertTrue(a1 != a2);
        assertTrue(a1.getClass() == a2.getClass());
        assertTrue(a1.equals(a2));
    }

// org.jfree.chart.axis.junit.SubCategoryAxisTests::testSerialization
    public void testSerialization() {

        SubCategoryAxis a1 = new SubCategoryAxis("Test Axis");
        SubCategoryAxis a2 = null;

        try {
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            ObjectOutput out = new ObjectOutputStream(buffer);
            out.writeObject(a1);
            out.close();

            ObjectInput in = new ObjectInputStream(
                new ByteArrayInputStream(buffer.toByteArray())
            );
            a2 = (SubCategoryAxis) in.readObject();
            in.close();
        }
        catch (Exception e) {
            System.out.println(e.toString());
        }
        assertEquals(a1, a2);

    }

// org.jfree.chart.axis.junit.SymbolAxisTests::testSerialization
    public void testSerialization() {

        String[] tickLabels = new String[] {"One", "Two", "Three"};
        SymbolAxis a1 = new SymbolAxis("Test Axis", tickLabels);
        SymbolAxis a2 = null;

        try {
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            ObjectOutput out = new ObjectOutputStream(buffer);
            out.writeObject(a1);
            out.close();

            ObjectInput in = new ObjectInputStream(new ByteArrayInputStream(
                    buffer.toByteArray()));
            a2 = (SymbolAxis) in.readObject();
            in.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        assertEquals(a1, a2);

    }

// org.jfree.chart.axis.junit.SymbolAxisTests::testCloning
    public void testCloning() {
        SymbolAxis a1 = new SymbolAxis("Axis", new String[] {"A", "B"});
        SymbolAxis a2 = null;
        try {
            a2 = (SymbolAxis) a1.clone();
        }
        catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        assertTrue(a1 != a2);
        assertTrue(a1.getClass() == a2.getClass());
        assertTrue(a1.equals(a2));
    }

// org.jfree.chart.axis.junit.SymbolAxisTests::testEquals
    public void testEquals() {
        SymbolAxis a1 = new SymbolAxis("Axis", new String[] {"A", "B"});
        SymbolAxis a2 = new SymbolAxis("Axis", new String[] {"A", "B"});
        assertTrue(a1.equals(a2));
        assertTrue(a2.equals(a1));
        
        a1 = new SymbolAxis("Axis 2", new String[] {"A", "B"});
        assertFalse(a1.equals(a2));
        a2 = new SymbolAxis("Axis 2", new String[] {"A", "B"});
        assertTrue(a1.equals(a2));    

        a1 = new SymbolAxis("Axis 2", new String[] {"C", "B"});
        assertFalse(a1.equals(a2));
        a2 = new SymbolAxis("Axis 2", new String[] {"C", "B"});
        assertTrue(a1.equals(a2));    
        
        a1.setGridBandPaint(Color.black);
        assertFalse(a1.equals(a2));
        a2.setGridBandPaint(Color.black);
        assertTrue(a1.equals(a2));
        
        a1.setGridBandsVisible(false);
        assertFalse(a1.equals(a2));
        a2.setGridBandsVisible(false);
        assertTrue(a1.equals(a2));
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
        assertEquals(new Font("SansSerif", Font.PLAIN, 9), m.getLabelFont());
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

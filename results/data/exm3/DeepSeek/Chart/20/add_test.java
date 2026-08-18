// org/jfree/chart/plot/junit/ValueMarkerTests.java
public void testOutlinePaintAndStroke1() {
        Stroke stroke = new BasicStroke(3.0f);
        Stroke outlineStroke = new BasicStroke(4.0f);
        ValueMarker m = new ValueMarker(2.0, Color.green, stroke, Color.cyan, outlineStroke, 0.0f);
        assertEquals(2.0, m.getValue(), EPSILON);
        assertEquals(Color.green, m.getPaint());
        assertEquals(stroke, m.getStroke());
        assertEquals(Color.cyan, m.getOutlinePaint());
        assertEquals(outlineStroke, m.getOutlineStroke());
        assertEquals(0.0f, m.getAlpha(), EPSILON);
    }

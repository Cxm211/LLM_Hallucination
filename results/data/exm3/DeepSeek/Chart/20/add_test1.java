// org/jfree/chart/plot/junit/ValueMarkerTests.java
public void testOutlinePaintAndStroke2() {
        Stroke stroke = new BasicStroke(0.5f);
        Stroke outlineStroke = new BasicStroke(5.0f);
        ValueMarker m = new ValueMarker(-5.0, Color.magenta, stroke, Color.orange, outlineStroke, 1.0f);
        assertEquals(-5.0, m.getValue(), EPSILON);
        assertEquals(Color.magenta, m.getPaint());
        assertEquals(stroke, m.getStroke());
        assertEquals(Color.orange, m.getOutlinePaint());
        assertEquals(outlineStroke, m.getOutlineStroke());
        assertEquals(1.0f, m.getAlpha(), EPSILON);
    }

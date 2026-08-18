// org/jfree/chart/plot/junit/ValueMarkerTests.java
public void testNullOutline() {
        Stroke stroke = new BasicStroke(1.0f);
        ValueMarker m = new ValueMarker(2.0, Color.red, stroke, null, null, 0.3f);
        assertEquals(Color.red, m.getPaint());
        assertEquals(stroke, m.getStroke());
        assertNull(m.getOutlinePaint());
        assertNull(m.getOutlineStroke());
    }
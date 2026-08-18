// org/jfree/chart/plot/junit/ValueMarkerTests.java
public void testValueMarkerWithNullOutlinePaint() {
    Stroke stroke = new BasicStroke(1.0f);
    Stroke outlineStroke = new BasicStroke(2.0f);
    ValueMarker m = new ValueMarker(2.5, Color.green, stroke, null, 
            outlineStroke, 0.8f);
    assertEquals(2.5, m.getValue(), EPSILON);
    assertEquals(Color.green, m.getPaint());
    assertEquals(stroke, m.getStroke());
    assertNull(m.getOutlinePaint());
    assertEquals(outlineStroke, m.getOutlineStroke());
    assertEquals(0.8f, m.getAlpha(), EPSILON);
}
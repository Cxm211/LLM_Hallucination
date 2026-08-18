// org/jfree/chart/plot/junit/ValueMarkerTests.java
public void testValueMarkerWithDifferentStrokes() {
    Stroke stroke1 = new BasicStroke(3.0f);
    Stroke stroke2 = new BasicStroke(4.0f);
    ValueMarker m = new ValueMarker(5.0, Color.yellow, stroke1, Color.black, 
            stroke2, 1.0f);
    assertEquals(5.0, m.getValue(), EPSILON);
    assertEquals(Color.yellow, m.getPaint());
    assertEquals(stroke1, m.getStroke());
    assertEquals(Color.black, m.getOutlinePaint());
    assertEquals(stroke2, m.getOutlineStroke());
    assertEquals(1.0f, m.getAlpha(), EPSILON);
}
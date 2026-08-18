// org/jfree/chart/renderer/junit/GrayPaintScaleTests.java
public void testGetPaintZeroRange() {
    GrayPaintScale gps = new GrayPaintScale(5.0, 5.0);
    Color c = (Color) gps.getPaint(5.0);
    assertTrue(c.equals(Color.black));
    c = (Color) gps.getPaint(0.0);
    assertTrue(c.equals(Color.black));
    c = (Color) gps.getPaint(10.0);
    assertTrue(c.equals(Color.black));
}

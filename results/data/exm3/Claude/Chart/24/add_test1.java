// org/jfree/chart/renderer/junit/GrayPaintScaleTests.java
public void testGetPaintQuarter() {
    GrayPaintScale gps = new GrayPaintScale();
    Color c = (Color) gps.getPaint(0.25);
    assertEquals(63, c.getRed());
    assertEquals(63, c.getGreen());
    assertEquals(63, c.getBlue());
}
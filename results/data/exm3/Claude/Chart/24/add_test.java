// org/jfree/chart/renderer/junit/GrayPaintScaleTests.java
public void testGetPaintMidpoint() {
    GrayPaintScale gps = new GrayPaintScale();
    Color c = (Color) gps.getPaint(0.5);
    assertEquals(127, c.getRed());
    assertEquals(127, c.getGreen());
    assertEquals(127, c.getBlue());
}
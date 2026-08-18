// org/jfree/chart/renderer/junit/GrayPaintScaleTests.java
public void testGetPaintNonDefaultBounds() {
    GrayPaintScale gps = new GrayPaintScale(10.0, 20.0);
    Color c = (Color) gps.getPaint(10.0);
    assertTrue(c.equals(Color.black));
    c = (Color) gps.getPaint(20.0);
    assertTrue(c.equals(Color.white));
    c = (Color) gps.getPaint(5.0);
    assertTrue(c.equals(Color.black));
    c = (Color) gps.getPaint(25.0);
    assertTrue(c.equals(Color.white));
    c = (Color) gps.getPaint(15.0);
    int expectedGray = 127;
    Color expected = new Color(expectedGray, expectedGray, expectedGray);
    assertTrue(c.equals(expected));
}

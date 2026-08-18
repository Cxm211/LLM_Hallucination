// org/jfree/chart/renderer/junit/GrayPaintScaleTests.java
public void testGetPaintWithCustomBounds() {
        GrayPaintScale gps = new GrayPaintScale(10.0, 20.0);
        Color c = (Color) gps.getPaint(5.0);
        assertTrue(c.equals(Color.black));
        c = (Color) gps.getPaint(25.0);
        assertTrue(c.equals(Color.white));
    }
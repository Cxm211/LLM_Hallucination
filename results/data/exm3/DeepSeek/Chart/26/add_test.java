// org/jfree/chart/plot/junit/CategoryPlotTests.java
public void testDrawLabelWithNullOwner() {
    NumberAxis axis = new NumberAxis("Test Axis");
    axis.setLabel("Label");
    BufferedImage image = new BufferedImage(200, 100, BufferedImage.TYPE_INT_RGB);
    Graphics2D g2 = image.createGraphics();
    Rectangle2D plotArea = new Rectangle2D.Double(0, 0, 200, 100);
    Rectangle2D dataArea = new Rectangle2D.Double(50, 50, 100, 50);
    AxisState state = new AxisState();
    state.setCursor(100);
    PlotRenderingInfo plotState = new PlotRenderingInfo(null);
    RectangleEdge[] edges = {
        RectangleEdge.TOP,
        RectangleEdge.BOTTOM,
        RectangleEdge.LEFT,
        RectangleEdge.RIGHT
    };
    for (RectangleEdge edge : edges) {
        try {
            axis.drawLabel(axis.getLabel(), g2, plotArea, dataArea, edge, state, plotState);
        } catch (NullPointerException e) {
            fail("NullPointerException for edge " + edge + ": " + e.getMessage());
        }
    }
    g2.dispose();
}

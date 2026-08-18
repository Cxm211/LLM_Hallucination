// org/jfree/chart/renderer/category/junit/MinMaxCategoryRendererTests.java
public void testDrawItemEntityShapeHorizontal() {
    MinMaxCategoryRenderer renderer = new MinMaxCategoryRenderer();
    DefaultCategoryDataset dataset = new DefaultCategoryDataset();
    dataset.addValue(5.0, "Row1", "Col1");
    CategoryPlot plot = new CategoryPlot(dataset, new CategoryAxis(), new NumberAxis(), renderer);
    plot.setOrientation(PlotOrientation.HORIZONTAL);
    Rectangle2D dataArea = new Rectangle2D.Double(0, 0, 400, 300);
    BufferedImage image = new BufferedImage(400, 300, BufferedImage.TYPE_INT_ARGB);
    Graphics2D g2 = image.createGraphics();
    StandardEntityCollection entities = new StandardEntityCollection();
    CategoryItemRendererState state = new CategoryItemRendererState();
    state.setEntityCollection(entities);
    CategoryAxis domainAxis = new CategoryAxis() {
        @Override
        public double getCategoryMiddle(int category, int categoryCount, Rectangle2D area, RectangleEdge edge) {
            return 100.0;
        }
    };
    ValueAxis rangeAxis = new NumberAxis() {
        @Override
        public double valueToJava2D(double value, Rectangle2D area, RectangleEdge edge) {
            return 200.0;
        }
    };
    renderer.drawItem(g2, state, dataArea, plot, domainAxis, rangeAxis, dataset, 0, 0, 0);
    assertEquals(1, entities.getEntityCount());
    CategoryItemEntity entity = (CategoryItemEntity) entities.getEntity(0);
    Rectangle2D bounds = entity.getArea().getBounds2D();
    assertEquals(200.0, bounds.getCenterX(), 0.001);
    assertEquals(100.0, bounds.getCenterY(), 0.001);
}

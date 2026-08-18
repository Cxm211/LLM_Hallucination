// org/jfree/chart/block/junit/BorderArrangementTests.java
public void testSizingWithHeightRange() {
    final double EPSILON = 1e-10;
    RectangleConstraint constraint = new RectangleConstraint(
        10.0, new Range(10.0, 10.0), LengthConstraintType.FIXED,
        0.0, new Range(5.0, 20.0), LengthConstraintType.RANGE
    );
    BlockContainer container = new BlockContainer(new BorderArrangement());
    BufferedImage image = new BufferedImage(200, 100, BufferedImage.TYPE_INT_RGB);
    Graphics2D g2 = image.createGraphics();

    // center only, natural height within range
    container.add(new EmptyBlock(5.0, 6.0));
    Size2D size = container.arrange(g2, constraint);
    assertEquals(10.0, size.width, EPSILON);
    assertEquals(6.0, size.height, EPSILON);

    // center only, natural height below lower bound -> clamp to lower bound
    container.clear();
    container.add(new EmptyBlock(5.0, 3.0));
    size = container.arrange(g2, constraint);
    assertEquals(10.0, size.width, EPSILON);
    assertEquals(5.0, size.height, EPSILON);

    // center only, natural height above upper bound -> clamp to upper bound
    container.clear();
    container.add(new EmptyBlock(5.0, 25.0));
    size = container.arrange(g2, constraint);
    assertEquals(10.0, size.width, EPSILON);
    assertEquals(20.0, size.height, EPSILON);

    // top and center, total natural height within range
    container.clear();
    container.add(new EmptyBlock(10.0, 4.0), RectangleEdge.TOP);
    container.add(new EmptyBlock(5.0, 7.0));
    size = container.arrange(g2, constraint);
    assertEquals(10.0, size.width, EPSILON);
    assertEquals(11.0, size.height, EPSILON);

    // top and center, total natural height below lower bound -> clamp
    container.clear();
    container.add(new EmptyBlock(10.0, 2.0), RectangleEdge.TOP);
    container.add(new EmptyBlock(5.0, 2.0));
    size = container.arrange(g2, constraint);
    assertEquals(10.0, size.width, EPSILON);
    assertEquals(5.0, size.height, EPSILON);

    // top, bottom, center, total natural height within range
    container.clear();
    container.add(new EmptyBlock(10.0, 3.0), RectangleEdge.TOP);
    container.add(new EmptyBlock(10.0, 4.0), RectangleEdge.BOTTOM);
    container.add(new EmptyBlock(5.0, 10.0));
    size = container.arrange(g2, constraint);
    assertEquals(10.0, size.width, EPSILON);
    assertEquals(17.0, size.height, EPSILON);

    // left and right, max height within range
    container.clear();
    container.add(new EmptyBlock(4.0, 8.0), RectangleEdge.LEFT);
    container.add(new EmptyBlock(3.0, 9.0), RectangleEdge.RIGHT);
    size = container.arrange(g2, constraint);
    assertEquals(10.0, size.width, EPSILON);
    assertEquals(9.0, size.height, EPSILON);

    g2.dispose();
}

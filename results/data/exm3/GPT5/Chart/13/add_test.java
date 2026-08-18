// org/jfree/chart/block/junit/BorderArrangementTests.java
public void testArrangeFFClampsNegativeRanges() {
        RectangleConstraint constraint = new RectangleConstraint(
            10.0, new Range(10.0, 10.0), LengthConstraintType.FIXED,
            5.0, new Range(5.0, 5.0), LengthConstraintType.FIXED
        );

        BlockContainer container = new BlockContainer(new BorderArrangement());
        BufferedImage image = new BufferedImage(200, 100, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = image.createGraphics();

        container.add(new EmptyBlock(8.0, 7.0), RectangleEdge.TOP);
        container.add(new EmptyBlock(6.0, 6.0), RectangleEdge.BOTTOM);

        Size2D size = container.arrange(g2, constraint);
        assertEquals(10.0, size.width, EPSILON);
        assertEquals(5.0, size.height, EPSILON);
    }
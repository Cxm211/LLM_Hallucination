// org/jfree/chart/block/junit/BorderArrangementTests.java
public void testSizingWithWidthConstraintAdditional1() {
        RectangleConstraint constraint = new RectangleConstraint(
            15.0, new Range(15.0, 15.0), LengthConstraintType.FIXED,
            0.0, new Range(0.0, 0.0), LengthConstraintType.NONE
        );
                
        BlockContainer container = new BlockContainer(new BorderArrangement());
        BufferedImage image = new BufferedImage(
            200, 100, BufferedImage.TYPE_INT_RGB
        );
        Graphics2D g2 = image.createGraphics();
        
        // TBLRC
        // 11001 - top, bottom and center
        container.clear();
        container.add(new EmptyBlock(10.0, 5.0));
        container.add(new EmptyBlock(15.0, 7.0), RectangleEdge.TOP);
        container.add(new EmptyBlock(15.0, 9.0), RectangleEdge.BOTTOM);
        Size2D size = container.arrange(g2, constraint);
        assertEquals(15.0, size.width, EPSILON);
        assertEquals(21.0, size.height, EPSILON);
        
        // TBLRC
        // 10000 - top only with specific height
        container.clear();
        container.add(new EmptyBlock(15.0, 30.0), RectangleEdge.TOP);
        size = container.arrange(g2, constraint);
        assertEquals(15.0, size.width, EPSILON);
        assertEquals(30.0, size.height, EPSILON);
        
        // TBLRC
        // 01000 - bottom only with specific height
        container.clear();
        container.add(new EmptyBlock(15.0, 25.0), RectangleEdge.BOTTOM);
        size = container.arrange(g2, constraint);
        assertEquals(15.0, size.width, EPSILON);
        assertEquals(25.0, size.height, EPSILON);
    }
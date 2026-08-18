// org/jfree/chart/util/junit/ShapeListTests.java
public void testEqualsDifferentSizes() {
    ShapeList l1 = new ShapeList();
    l1.setShape(0, new Rectangle(1, 2, 3, 4));
    l1.setShape(1, new Line2D.Double(1.0, 2.0, 3.0, 4.0));

    ShapeList l2 = new ShapeList();
    l2.setShape(0, new Rectangle(1, 2, 3, 4));

    assertFalse(l1.equals(l2));
    assertFalse(l2.equals(l1));
}
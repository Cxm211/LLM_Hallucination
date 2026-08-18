// org/jfree/chart/util/junit/ShapeListTests.java
public void testEqualsDifferentShapes() {
    ShapeList l1 = new ShapeList();
    l1.setShape(0, new Rectangle(1, 2, 3, 4));

    ShapeList l2 = new ShapeList();
    l2.setShape(0, new Rectangle(5, 6, 7, 8));

    assertFalse(l1.equals(l2));
    assertFalse(l2.equals(l1));
}
// org/jfree/chart/util/junit/ShapeListTests.java
public void testEqualsSingleShape() {
    ShapeList l1 = new ShapeList();
    l1.setShape(0, new Rectangle(1, 2, 3, 4));
    ShapeList l2 = new ShapeList();
    l2.setShape(0, new Rectangle(1, 2, 3, 4));
    assertTrue(l1.equals(l2));
    assertTrue(l2.equals(l1));
}

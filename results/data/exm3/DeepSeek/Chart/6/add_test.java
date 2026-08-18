// org/jfree/chart/util/junit/ShapeListTests.java
public void testEqualsEmpty() {
    ShapeList l1 = new ShapeList();
    ShapeList l2 = new ShapeList();
    assertTrue(l1.equals(l2));
    assertTrue(l2.equals(l1));
}

// org/jfree/chart/util/junit/ShapeListTests.java
public void testEqualsNullVsNonNull() {
    ShapeList l1 = new ShapeList();
    l1.setShape(0, null);

    ShapeList l2 = new ShapeList();
    l2.setShape(0, new Rectangle(1, 2, 3, 4));

    assertFalse(l1.equals(l2));
    assertFalse(l2.equals(l1));
}
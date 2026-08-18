// org/apache/commons/collections/functors/TestEqualPredicate.java
@Test
public void objectFactoryWithNullValue() throws Exception {
    Predicate<EqualsTestObject> predicate = equalPredicate(null);
    assertTrue(predicate.evaluate(null));
    assertFalse(predicate.evaluate(TRUE_OBJECT));
}

@Test
public void objectFactoryWithNonNullValue() throws Exception {
    EqualsTestObject obj = new EqualsTestObject(42);
    Predicate<EqualsTestObject> predicate = equalPredicate(obj);
    assertTrue(predicate.evaluate(obj));
    assertTrue(predicate.evaluate(new EqualsTestObject(42)));
    assertFalse(predicate.evaluate(new EqualsTestObject(43)));
    assertFalse(predicate.evaluate(null));
}
// org/apache/commons/collections/functors/TestEqualPredicate.java::constructorUsesEqualsForTest
@Test
public void constructorUsesEqualsForTest() throws Exception {
    Predicate<EqualsTestObject> predicate = new EqualPredicate<EqualsTestObject>(FALSE_OBJECT);
    assertFalse(predicate, FALSE_OBJECT);
    assertTrue(new EqualPredicate<EqualsTestObject>(TRUE_OBJECT), TRUE_OBJECT);
}

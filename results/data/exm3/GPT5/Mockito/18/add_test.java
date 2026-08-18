// org/mockito/internal/stubbing/defaultanswers/ReturnsEmptyValuesTest.java::should_return_modifiable_iterable
@Test
public void should_return_modifiable_iterable() throws Exception {
    Iterable iterable = (Iterable) values.returnValueFor(Iterable.class);
    assertNotNull(iterable);
    Collection c = (Collection) iterable;
    assertTrue(c.isEmpty());
    c.add("x");
    assertFalse(c.isEmpty());
}
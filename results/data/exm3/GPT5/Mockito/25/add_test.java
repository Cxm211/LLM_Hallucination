// org/mockito/internal/stubbing/defaultanswers/ReturnsGenericDeepStubsTest.java::deep_stub_implements_all_bounds_of_typevar
@Test
public void deep_stub_implements_all_bounds_of_typevar() throws Exception {
    GenericsNest<?> mock = mock(GenericsNest.class, RETURNS_DEEP_STUBS);

    Object k = mock.returningK();
    assertThat(k).isInstanceOf(Cloneable.class);
    assertThat(k).isInstanceOf(Comparable.class);
}
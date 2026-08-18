// org/mockito/internal/stubbing/defaultanswers/ReturnsGenericDeepStubsTest.java
@Test
public void can_reuse_deep_stub_in_nested_chain() throws Exception {
    GenericsNest<?> mock = mock(GenericsNest.class, RETURNS_DEEP_STUBS);

    Set<Number> firstValue = mock.entrySet().iterator().next().getValue();
    Set<Number> secondValue = mock.entrySet().iterator().next().getValue();
    
    assertThat(firstValue).isSameAs(secondValue);
}
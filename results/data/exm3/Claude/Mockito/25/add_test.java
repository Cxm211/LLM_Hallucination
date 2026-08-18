// org/mockito/internal/stubbing/defaultanswers/ReturnsGenericDeepStubsTest.java
@Test
public void can_reuse_same_deep_stub_mock_after_multiple_invocations() throws Exception {
    GenericsNest<?> mock = mock(GenericsNest.class, RETURNS_DEEP_STUBS);

    Cloneable first = mock.returningK();
    Cloneable second = mock.returningK();
    
    assertThat(first).isSameAs(second);
}
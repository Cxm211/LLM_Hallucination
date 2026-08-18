// org/mockitousage/matchers/CapturingArgumentsTest.java
@Test
public void should_capture_varargs_with_empty_varargs() throws Exception {
    // given
    ArgumentCaptor<String> argumentCaptor = ArgumentCaptor.forClass(String.class);

    // when
    mock.mixedVarargs(42);

    // then
    verify(mock).mixedVarargs(any(), argumentCaptor.capture());
    Assertions.assertThat(argumentCaptor.getAllValues()).isEmpty();
}
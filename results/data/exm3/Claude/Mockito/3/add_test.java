// org/mockitousage/matchers/CapturingArgumentsTest.java
@Test
public void should_capture_varargs_with_single_element() throws Exception {
    // given
    ArgumentCaptor<String> argumentCaptor = ArgumentCaptor.forClass(String.class);

    // when
    mock.mixedVarargs(42, "single");

    // then
    verify(mock).mixedVarargs(any(), argumentCaptor.capture());
    Assertions.assertThat(argumentCaptor.getAllValues()).containsExactly("single");
}
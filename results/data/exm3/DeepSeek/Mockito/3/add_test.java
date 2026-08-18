// org/mockitousage/matchers/CapturingArgumentsTest.java
@Test
    public void should_capture_nothing_when_vararg_is_null_or_empty() throws Exception {
        // given
        ArgumentCaptor<String> argumentCaptor = ArgumentCaptor.forClass(String.class);
        
        // when
        mock.mixedVarargs(42, (String[]) null);
        mock.mixedVarargs(42);
        
        // then
        verify(mock, times(2)).mixedVarargs(any(), argumentCaptor.capture());
        Assertions.assertThat(argumentCaptor.getAllValues()).isEmpty();
    }

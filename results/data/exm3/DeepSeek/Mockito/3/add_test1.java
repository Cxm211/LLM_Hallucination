// org/mockitousage/matchers/CapturingArgumentsTest.java
@Test
    public void should_capture_vararg_elements_according_to_matcher_count() throws Exception {
        // given
        ArgumentCaptor<String> captor1 = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> captor2 = ArgumentCaptor.forClass(String.class);
        
        // when
        mock.mixedVarargs(1, "a");
        mock.mixedVarargs(2, "x", "y", "z");
        
        // then
        verify(mock).mixedVarargs(eq(1), captor1.capture(), captor2.capture());
        verify(mock).mixedVarargs(eq(2), captor1.capture(), captor2.capture());
        
        Assertions.assertThat(captor1.getAllValues()).containsExactly("a", "x");
        Assertions.assertThat(captor2.getAllValues()).containsExactly("y");
    }

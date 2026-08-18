// org/mockitousage/matchers/CapturingArgumentsTest.java
@Test
    public void should_capture_pure_varargs_with_multiple_matchers() throws Exception {
        // given
        ArgumentCaptor<Byte> captor1 = ArgumentCaptor.forClass(byte.class);
        ArgumentCaptor<Byte> captor2 = ArgumentCaptor.forClass(byte.class);
        ArgumentCaptor<Byte> captor3 = ArgumentCaptor.forClass(byte.class);
        
        // when
        mock.varargsbyte((byte)1, (byte)2, (byte)3);
        
        // then
        verify(mock).varargsbyte(captor1.capture(), captor2.capture(), captor3.capture());
        Assertions.assertThat(captor1.getAllValues()).containsExactly((byte)1);
        Assertions.assertThat(captor2.getAllValues()).containsExactly((byte)2);
        Assertions.assertThat(captor3.getAllValues()).containsExactly((byte)3);
    }

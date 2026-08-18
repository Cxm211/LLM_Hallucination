// buggy function
    public void captureArgumentsFrom(Invocation invocation) {
        if (invocation.getMethod().isVarArgs()) {
            int indexOfVararg = invocation.getRawArguments().length - 1;
            for (int position = 0; position < indexOfVararg; position++) {
                Matcher m = matchers.get(position);
                if (m instanceof CapturesArguments) {
                    ((CapturesArguments) m).captureFrom(invocation.getArgumentAt(position, Object.class));
                }
            }
            for (int position = indexOfVararg; position < matchers.size(); position++) {
                Matcher m = matchers.get(position);
                if (m instanceof CapturesArguments) {
                    ((CapturesArguments) m).captureFrom(invocation.getRawArguments()[position - indexOfVararg]);
                }
            }
        } else {
            for (int position = 0; position < matchers.size(); position++) {
                Matcher m = matchers.get(position);
                if (m instanceof CapturesArguments) {
                    ((CapturesArguments) m).captureFrom(invocation.getArgumentAt(position, Object.class));
                }
            }
        }
    }

// trigger testcase
// org/mockito/internal/invocation/InvocationMatcherTest.java::should_capture_varargs_as_vararg
@Test
    public void should_capture_varargs_as_vararg() throws Exception {
        //given
        mock.mixedVarargs(1, "a", "b");
        Invocation invocation = getLastInvocation();
        CapturingMatcher m = new CapturingMatcher();
        InvocationMatcher invocationMatcher = new InvocationMatcher(invocation, (List) asList(new Equals(1), new LocalizedMatcher(m)));

        //when
        invocationMatcher.captureArgumentsFrom(invocation);

        //then
        Assertions.assertThat(m.getAllValues()).containsExactly("a", "b");
    }

// org/mockitousage/bugs/varargs/VarargsAndAnyObjectPicksUpExtraInvocationsTest.java::shouldVerifyCorrectlyNumberOfInvocationsWithVarargs
@Test
    public void shouldVerifyCorrectlyNumberOfInvocationsWithVarargs() {
        //when
        table.newRow("qux", "foo", "bar", "baz");
        table.newRow("abc", "def");
        
        //then
        verify(table).newRow(anyString(), eq("foo"), anyString(), anyString());
        verify(table).newRow(anyString(), anyString());
    }

// org/mockitousage/bugs/varargs/VarargsNotPlayingWithAnyObjectTest.java::shouldMatchAnyVararg
@Test
    public void shouldMatchAnyVararg() {
        mock.run("a", "b");

        verify(mock).run(anyString(), anyString());
        verify(mock).run((String) anyObject(), (String) anyObject());

        verify(mock).run((String[]) anyVararg());
        
        verify(mock, never()).run();
        verify(mock, never()).run(anyString(), eq("f"));
    }

// org/mockitousage/matchers/CapturingArgumentsTest.java::captures_correctly_when_captor_used_multiple_times
@Test
    public void captures_correctly_when_captor_used_multiple_times() throws Exception {
        // given
        ArgumentCaptor<String> argumentCaptor = ArgumentCaptor.forClass(String.class);

        // when
        mock.mixedVarargs(42, "a", "b", "c");

        // then
        // this is only for backwards compatibility. It does not make sense in real to do so.
        verify(mock).mixedVarargs(any(), argumentCaptor.capture(), argumentCaptor.capture(), argumentCaptor.capture());
        Assertions.assertThat(argumentCaptor.getAllValues()).containsExactly("a", "b", "c");
    }

// org/mockitousage/matchers/CapturingArgumentsTest.java::captures_correctly_when_captor_used_on_pure_vararg_method
@Test
    public void captures_correctly_when_captor_used_on_pure_vararg_method() throws Exception {
        // given
        ArgumentCaptor<String> argumentCaptor = ArgumentCaptor.forClass(String.class);

        // when
        mock.varargs(42, "capturedValue");

        // then
        verify(mock).varargs(eq(42), argumentCaptor.capture());
        Assertions.assertThat(argumentCaptor.getValue()).contains("capturedValue");
    }

// org/mockitousage/matchers/CapturingArgumentsTest.java::should_capture_all_vararg
@Test
    public void should_capture_all_vararg() throws Exception {
        // given
        ArgumentCaptor<String> argumentCaptor = ArgumentCaptor.forClass(String.class);

        // when
        mock.mixedVarargs(42, "a", "b", "c");
        mock.mixedVarargs(42, "again ?!");

        // then
        verify(mock, times(2)).mixedVarargs(any(), argumentCaptor.capture());

        Assertions.assertThat(argumentCaptor.getAllValues()).containsExactly("a", "b", "c", "again ?!");
    }

// org/mockitousage/matchers/CapturingArgumentsTest.java::should_capture_byte_vararg_by_creating_captor_with_primitive
@Test
    public void should_capture_byte_vararg_by_creating_captor_with_primitive() throws Exception {
        // given
        ArgumentCaptor<Byte> argumentCaptor = ArgumentCaptor.forClass(byte.class);

        // when
        mock.varargsbyte((byte) 1, (byte) 2);

        // then
        verify(mock).varargsbyte(argumentCaptor.capture());
        assertEquals((byte) 2, (byte) argumentCaptor.getValue());
        Assertions.assertThat(argumentCaptor.getAllValues()).containsExactly((byte) 1, (byte) 2);
    }

// org/mockitousage/matchers/CapturingArgumentsTest.java::should_capture_byte_vararg_by_creating_captor_with_primitive_wrapper
@Test
    public void should_capture_byte_vararg_by_creating_captor_with_primitive_wrapper() throws Exception {
        // given
        ArgumentCaptor<Byte> argumentCaptor = ArgumentCaptor.forClass(Byte.class);

        // when
        mock.varargsbyte((byte) 1, (byte) 2);

        // then
        verify(mock).varargsbyte(argumentCaptor.capture());
        assertEquals((byte) 2, (byte) argumentCaptor.getValue());
        Assertions.assertThat(argumentCaptor.getAllValues()).containsExactly((byte) 1, (byte) 2);
    }

// org/mockitousage/matchers/CapturingArgumentsTest.java::should_capture_vararg
@Test
    public void should_capture_vararg() throws Exception {
        // given
        ArgumentCaptor<String> argumentCaptor = ArgumentCaptor.forClass(String.class);

        // when
        mock.mixedVarargs(42, "a", "b", "c");

        // then
        verify(mock).mixedVarargs(any(), argumentCaptor.capture());
        Assertions.assertThat(argumentCaptor.getAllValues()).containsExactly("a", "b", "c");
    }

// buggy code
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

// relevant test
// org.mockitousage.basicapi.MocksSerializationForAnnotationTest::should_be_serialize_and_have_extra_interfaces
    public void should_be_serialize_and_have_extra_interfaces() throws Exception {
        
        Assertions.assertThat((Object) serializeAndBack((List) imethodsWithExtraInterfacesMock))
                .isInstanceOf(List.class)
                .isInstanceOf(IMethods.class);
    }

// org.mockitousage.basicapi.MocksSerializationForAnnotationTest::should_fail_when_serializable_used_with_type_that_dont_implements_Serializable_and_dont_declare_a_no_arg_constructor
    public void should_fail_when_serializable_used_with_type_that_dont_implements_Serializable_and_dont_declare_a_no_arg_constructor() throws Exception {
        try {
            FailTestClass testClass = new FailTestClass();
            MockitoAnnotations.initMocks(testClass);
            serializeAndBack(testClass.notSerializableAndNoDefaultConstructor);
            fail("should have thrown an exception to say the object is not serializable");
        } catch (MockitoException e) {
            Assertions.assertThat(e.getMessage())
                    .contains(NotSerializableAndNoDefaultConstructor.class.getSimpleName())
                    .contains("serializable()")
                    .contains("implement Serializable")
                    .contains("no-arg constructor");
        }
    }

// org.mockitousage.basicapi.MocksSerializationForAnnotationTest::should_be_able_to_serialize_type_that_implements_Serializable_but_but_dont_declare_a_no_arg_constructor
    public void should_be_able_to_serialize_type_that_implements_Serializable_but_but_dont_declare_a_no_arg_constructor() throws Exception {
        TestClassThatHoldValidField testClass = new TestClassThatHoldValidField();
        MockitoAnnotations.initMocks(testClass);

        serializeAndBack(testClass.serializableAndNoDefaultConstructor);
    }

// org.mockitousage.basicapi.MocksSerializationTest::should_allow_throws_exception_to_be_serializable
    public void should_allow_throws_exception_to_be_serializable() throws Exception {
        
        Bar mock = mock(Bar.class, new ThrowsException(new RuntimeException()));
        
        serializeAndBack(mock);
    }

// org.mockitousage.basicapi.MocksSerializationTest::should_allow_method_delegation
    public void should_allow_method_delegation() throws Exception {
        
        Bar barMock = mock(Bar.class, withSettings().serializable());
        Foo fooMock = mock(Foo.class);
        when(barMock.doSomething()).thenAnswer(new ThrowsException(new RuntimeException()));

        
        serializeAndBack(barMock);
    }

// org.mockitousage.basicapi.MocksSerializationTest::should_allow_mock_to_be_serializable
    public void should_allow_mock_to_be_serializable() throws Exception {
        
        IMethods mock = mock(IMethods.class, withSettings().serializable());

        
        serializeAndBack(mock);
    }

// org.mockitousage.basicapi.MocksSerializationTest::should_allow_mock_and_boolean_value_to_serializable
    public void should_allow_mock_and_boolean_value_to_serializable() throws Exception {
        
        IMethods mock = mock(IMethods.class, withSettings().serializable());
        when(mock.booleanReturningMethod()).thenReturn(true);

        
        ByteArrayOutputStream serialized = serializeMock(mock);

        
        IMethods readObject = deserializeMock(serialized, IMethods.class);
        assertTrue(readObject.booleanReturningMethod());
    }

// org.mockitousage.basicapi.MocksSerializationTest::should_allow_mock_and_string_value_to_be_serializable
    public void should_allow_mock_and_string_value_to_be_serializable() throws Exception {
        
        IMethods mock = mock(IMethods.class, withSettings().serializable());
        String value = "value";
        when(mock.stringReturningMethod()).thenReturn(value);

        
        ByteArrayOutputStream serialized = serializeMock(mock);

        
        IMethods readObject = deserializeMock(serialized, IMethods.class);
        assertEquals(value, readObject.stringReturningMethod());
    }

// org.mockitousage.basicapi.MocksSerializationTest::should_all_mock_and_serializable_value_to_be_serialized
    public void should_all_mock_and_serializable_value_to_be_serialized() throws Exception {
        
        IMethods mock = mock(IMethods.class, withSettings().serializable());
        List<?> value = Collections.emptyList();
        when(mock.objectReturningMethodNoArgs()).thenReturn(value);

        
        ByteArrayOutputStream serialized = serializeMock(mock);

        
        IMethods readObject = deserializeMock(serialized, IMethods.class);
        assertEquals(value, readObject.objectReturningMethodNoArgs());
    }

// org.mockitousage.basicapi.MocksSerializationTest::should_serialize_method_call_with_parameters_that_are_serializable
    public void should_serialize_method_call_with_parameters_that_are_serializable() throws Exception {
        IMethods mock = mock(IMethods.class, withSettings().serializable());
        List<?> value = Collections.emptyList();
        when(mock.objectArgMethod(value)).thenReturn(value);

        
        ByteArrayOutputStream serialized = serializeMock(mock);

        
        IMethods readObject = deserializeMock(serialized, IMethods.class);
        assertEquals(value, readObject.objectArgMethod(value));
    }

// org.mockitousage.basicapi.MocksSerializationTest::should_serialize_method_calls_using_any_string_matcher
    public void should_serialize_method_calls_using_any_string_matcher() throws Exception {
        IMethods mock = mock(IMethods.class, withSettings().serializable());
        List<?> value = Collections.emptyList();
        when(mock.objectArgMethod(anyString())).thenReturn(value);

        
        ByteArrayOutputStream serialized = serializeMock(mock);

        
        IMethods readObject = deserializeMock(serialized, IMethods.class);
        assertEquals(value, readObject.objectArgMethod(""));
    }

// org.mockitousage.basicapi.MocksSerializationTest::should_verify_called_n_times_for_serialized_mock
    public void should_verify_called_n_times_for_serialized_mock() throws Exception {
        IMethods mock = mock(IMethods.class, withSettings().serializable());
        List<?> value = Collections.emptyList();
        when(mock.objectArgMethod(anyString())).thenReturn(value);
        mock.objectArgMethod("");

        
        ByteArrayOutputStream serialized = serializeMock(mock);

        
        IMethods readObject = deserializeMock(serialized, IMethods.class);
        verify(readObject, times(1)).objectArgMethod("");
    }

// org.mockitousage.basicapi.MocksSerializationTest::should_verify_even_if_some_methods_called_after_serialization
    public void should_verify_even_if_some_methods_called_after_serialization() throws Exception {
        
        IMethods mock = mock(IMethods.class, withSettings().serializable());

        
        mock.simpleMethod(1);
        ByteArrayOutputStream serialized = serializeMock(mock);
        IMethods readObject = deserializeMock(serialized, IMethods.class);
        readObject.simpleMethod(1);

        
        verify(readObject, times(2)).simpleMethod(1);

        
        
    }

// org.mockitousage.basicapi.MocksSerializationTest::should_serialization_work
    public void should_serialization_work() throws Exception {
        
        Foo foo = new Foo();
        
        foo = serializeAndBack(foo);
        
        assertSame(foo, foo.bar.foo);
    }

// org.mockitousage.basicapi.MocksSerializationTest::should_stub_even_if_some_methods_called_after_serialization
    public void should_stub_even_if_some_methods_called_after_serialization() throws Exception {
        
        IMethods mock = mock(IMethods.class, withSettings().serializable());

        
        when(mock.simpleMethod(1)).thenReturn("foo");
        ByteArrayOutputStream serialized = serializeMock(mock);
        IMethods readObject = deserializeMock(serialized, IMethods.class);
        when(readObject.simpleMethod(2)).thenReturn("bar");

        
        assertEquals("foo", readObject.simpleMethod(1));
        assertEquals("bar", readObject.simpleMethod(2));
    }

// org.mockitousage.basicapi.MocksSerializationTest::should_verify_call_order_for_serialized_mock
    public void should_verify_call_order_for_serialized_mock() throws Exception {
        IMethods mock = mock(IMethods.class, withSettings().serializable());
        IMethods mock2 = mock(IMethods.class, withSettings().serializable());
        mock.arrayReturningMethod();
        mock2.arrayReturningMethod();

        
        ByteArrayOutputStream serialized = serializeMock(mock);
        ByteArrayOutputStream serialized2 = serializeMock(mock2);

        
        IMethods readObject = deserializeMock(serialized, IMethods.class);
        IMethods readObject2 = deserializeMock(serialized2, IMethods.class);
        InOrder inOrder = inOrder(readObject, readObject2);
        inOrder.verify(readObject).arrayReturningMethod();
        inOrder.verify(readObject2).arrayReturningMethod();
    }

// org.mockitousage.basicapi.MocksSerializationTest::should_remember_interactions_for_serialized_mock
    public void should_remember_interactions_for_serialized_mock() throws Exception {
        IMethods mock = mock(IMethods.class, withSettings().serializable());
        List<?> value = Collections.emptyList();
        when(mock.objectArgMethod(anyString())).thenReturn(value);
        mock.objectArgMethod("happened");

        
        ByteArrayOutputStream serialized = serializeMock(mock);

        
        IMethods readObject = deserializeMock(serialized, IMethods.class);
        verify(readObject, never()).objectArgMethod("never happened");
    }

// org.mockitousage.basicapi.MocksSerializationTest::should_serialize_with_stubbing_callback
    public void should_serialize_with_stubbing_callback() throws Exception {

        
        IMethods mock = mock(IMethods.class, withSettings().serializable());
        CustomAnswersMustImplementSerializableForSerializationToWork answer =
                new CustomAnswersMustImplementSerializableForSerializationToWork();
        answer.string = "return value";
        when(mock.objectArgMethod(anyString())).thenAnswer(answer);

        
        ByteArrayOutputStream serialized = serializeMock(mock);

        
        IMethods readObject = deserializeMock(serialized, IMethods.class);
        assertEquals(answer.string, readObject.objectArgMethod(""));
    }

// org.mockitousage.basicapi.MocksSerializationTest::should_serialize_with_real_object_spy
    public void should_serialize_with_real_object_spy() throws Exception {
        
        List<Object> list = new ArrayList<Object>();
        List<Object> spy = mock(ArrayList.class, withSettings()
                .spiedInstance(list)
                .defaultAnswer(CALLS_REAL_METHODS)
                .serializable());
        when(spy.size()).thenReturn(100);

        
        ByteArrayOutputStream serialized = serializeMock(spy);

        
        List<?> readObject = deserializeMock(serialized, List.class);
        assertEquals(100, readObject.size());
    }

// org.mockitousage.basicapi.MocksSerializationTest::should_serialize_object_mock
    public void should_serialize_object_mock() {}

// org.mockitousage.basicapi.MocksSerializationTest::should_serialize_real_partial_mock
    public void should_serialize_real_partial_mock() {}

// org.mockitousage.basicapi.MocksSerializationTest::should_serialize_already_serializable_class
    public void should_serialize_already_serializable_class() throws Exception {
        
        AlreadySerializable mock = mock(AlreadySerializable.class, withSettings().serializable());
        when(mock.toString()).thenReturn("foo");

        
        mock = serializeAndBack(mock);

        
        assertEquals("foo", mock.toString());
    }

// org.mockitousage.basicapi.MocksSerializationTest::should_be_serialize_and_have_extra_interfaces
    public void should_be_serialize_and_have_extra_interfaces() throws Exception {
        
        IMethods mock = mock(IMethods.class, withSettings().serializable().extraInterfaces(List.class));
        IMethods mockTwo = mock(IMethods.class, withSettings().extraInterfaces(List.class).serializable());

        
        Assertions.assertThat((Object) serializeAndBack((List) mock))
                .isInstanceOf(List.class)
                .isInstanceOf(IMethods.class);
        Assertions.assertThat((Object) serializeAndBack((List) mockTwo))
                .isInstanceOf(List.class)
                .isInstanceOf(IMethods.class);
    }

// org.mockitousage.basicapi.MocksSerializationTest::should_fail_when_serializable_used_with_type_that_dont_implements_Serializable_and_dont_declare_a_no_arg_constructor
    public void should_fail_when_serializable_used_with_type_that_dont_implements_Serializable_and_dont_declare_a_no_arg_constructor() throws Exception {
        try {
            serializeAndBack(mock(NotSerializableAndNoDefaultConstructor.class, withSettings().serializable()));
            fail("should have thrown an exception to say the object is not serializable");
        } catch (MockitoException e) {
            Assertions.assertThat(e.getMessage())
                    .contains(NotSerializableAndNoDefaultConstructor.class.getSimpleName())
                    .contains("serializable()")
                    .contains("implement Serializable")
                    .contains("no-arg constructor");
        }
    }

// org.mockitousage.basicapi.MocksSerializationTest::should_be_able_to_serialize_type_that_implements_Serializable_but_but_dont_declare_a_no_arg_constructor
    public void should_be_able_to_serialize_type_that_implements_Serializable_but_but_dont_declare_a_no_arg_constructor() throws Exception {
        serializeAndBack(mock(SerializableAndNoDefaultConstructor.class));
    }

// org.mockitousage.basicapi.MocksSerializationTest::private_constructor_currently_not_supported_at_the_moment_at_deserialization_time
    public void private_constructor_currently_not_supported_at_the_moment_at_deserialization_time() throws Exception {
        
        AClassWithPrivateNoArgConstructor mockWithPrivateConstructor = Mockito.mock(
                AClassWithPrivateNoArgConstructor.class,
                Mockito.withSettings().serializable()
        );

        try {
            
            SimpleSerializationUtil.serializeAndBack(mockWithPrivateConstructor);
            fail("should have thrown an ObjectStreamException or a subclass of it");
        } catch (ObjectStreamException e) {
            
            Assertions.assertThat(e.toString()).contains("no valid constructor");
        }
    }

// org.mockitousage.basicapi.MocksSerializationTest::BUG_ISSUE_399_try_some_mocks_with_current_answers
    public void BUG_ISSUE_399_try_some_mocks_with_current_answers() throws Exception {
        IMethods iMethods = mock(IMethods.class, withSettings().serializable().defaultAnswer(RETURNS_DEEP_STUBS));

        when(iMethods.iMethodsReturningMethod().linkedListReturningMethod().contains(anyString())).thenReturn(false);

        serializeAndBack(iMethods);
    }

// org.mockitousage.basicapi.ReplacingObjectMethodsTest::shouldProvideMockyImplementationOfToString
    public void shouldProvideMockyImplementationOfToString() {
        DummyClass dummyClass = Mockito.mock(DummyClass.class);
        assertEquals("Mock for DummyClass, hashCode: " + dummyClass.hashCode(), dummyClass.toString());
        DummyInterface dummyInterface = Mockito.mock(DummyInterface.class);
        assertEquals("Mock for DummyInterface, hashCode: " + dummyInterface.hashCode(), dummyInterface.toString());
    }

// org.mockitousage.basicapi.ReplacingObjectMethodsTest::shouldReplaceObjectMethods
    public void shouldReplaceObjectMethods() {
        Object mock = Mockito.mock(ObjectMethodsOverridden.class);
        Object otherMock = Mockito.mock(ObjectMethodsOverridden.class);
        
        assertThat(mock, equalTo(mock));
        assertThat(mock, not(equalTo(otherMock)));
        
        assertThat(mock.hashCode(), not(equalTo(otherMock.hashCode())));
        
        assertContains("Mock for ObjectMethodsOverridden", mock.toString());
    }

// org.mockitousage.basicapi.ReplacingObjectMethodsTest::shouldReplaceObjectMethodsWhenOverridden
    public void shouldReplaceObjectMethodsWhenOverridden() {
        Object mock = Mockito.mock(ObjectMethodsOverriddenSubclass.class);
        Object otherMock = Mockito.mock(ObjectMethodsOverriddenSubclass.class);
        
        assertThat(mock, equalTo(mock));
        assertThat(mock, not(equalTo(otherMock)));
        
        assertThat(mock.hashCode(), not(equalTo(otherMock.hashCode())));
        
        assertContains("Mock for ObjectMethodsOverriddenSubclass", mock.toString());
    }

// org.mockitousage.basicapi.ResetTest::shouldResetOngoingStubbingSoThatMoreMeaningfulExceptionsAreRaised
    public void shouldResetOngoingStubbingSoThatMoreMeaningfulExceptionsAreRaised() {
        mock(IMethods.class);
        mock.booleanReturningMethod();
        reset(mock);
        try {
            when(null).thenReturn("anything");
            fail();
        } catch (MissingMethodInvocationException e) {
        }
    }

// org.mockitousage.basicapi.ResetTest::resettingNonMockIsSafe
    public void resettingNonMockIsSafe() {
        reset("");
    }

// org.mockitousage.basicapi.ResetTest::resettingNullIsSafe
    public void resettingNullIsSafe() {
        reset(new Object[] {null});
    }

// org.mockitousage.basicapi.ResetTest::shouldRemoveAllStubbing
    public void shouldRemoveAllStubbing() throws Exception {
        when(mock.objectReturningMethod(isA(Integer.class))).thenReturn(100);
        when(mock.objectReturningMethod(200)).thenReturn(200);
        reset(mock);
        assertNull(mock.objectReturningMethod(200));
        assertEquals("default behavior should return null", null, mock.objectReturningMethod("blah"));
    }

// org.mockitousage.basicapi.ResetTest::shouldRemoveAllInteractions
    public void shouldRemoveAllInteractions() throws Exception {
        mock.simpleMethod(1);
        reset(mock);
        verifyZeroInteractions(mock);
    }

// org.mockitousage.basicapi.ResetTest::shouldRemoveStubbingToString
    public void shouldRemoveStubbingToString() throws Exception {
        IMethods mockTwo = mock(IMethods.class);
        when(mockTwo.toString()).thenReturn("test");
        reset(mockTwo);
        assertContains("Mock for IMethods", mockTwo.toString());
    }

// org.mockitousage.basicapi.ResetTest::shouldStubbingNotBeTreatedAsInteraction
    public void shouldStubbingNotBeTreatedAsInteraction() {
        when(mock.simpleMethod("one")).thenThrow(new RuntimeException());
        doThrow(new RuntimeException()).when(mock).simpleMethod("two");
        reset(mock);
        verifyZeroInteractions(mock);
    }

// org.mockitousage.basicapi.ResetTest::shouldNotAffectMockName
    public void shouldNotAffectMockName() {
        IMethods mock = mock(IMethods.class, "mockie");
        IMethods mockTwo = mock(IMethods.class);
        reset(mock);
        assertContains("Mock for IMethods", "" + mockTwo);
        assertEquals("mockie", "" + mock);
    }

// org.mockitousage.basicapi.ResetTest::shouldResetMultipleMocks
    public void shouldResetMultipleMocks() {
        mock.simpleMethod();
        mockTwo.simpleMethod();
        reset(mock, mockTwo);
        verifyNoMoreInteractions(mock, mockTwo);
    }

// org.mockitousage.basicapi.ResetTest::shouldValidateStateWhenResetting
    public void shouldValidateStateWhenResetting() {
        
        verify(mock);
        
        try {
            reset(mockTwo);
            fail();
        } catch (UnfinishedVerificationException e) {}
    }

// org.mockitousage.basicapi.ResetTest::shouldMaintainPreviousDefaultAnswer
    public void shouldMaintainPreviousDefaultAnswer() {
        
        mock = mock(IMethods.class, RETURNS_MOCKS);
        
        reset(mock);
        
        assertNotNull(mock.iMethodsReturningMethod());
    }

// org.mockitousage.basicapi.UsingVarargsTest::shouldStubStringVarargs
    public void shouldStubStringVarargs() {
        when(mock.withStringVarargsReturningString(1)).thenReturn("1");
        when(mock.withStringVarargsReturningString(2, "1", "2", "3")).thenReturn("2");
        
        RuntimeException expected = new RuntimeException();
        stubVoid(mock).toThrow(expected).on().withStringVarargs(3, "1", "2", "3", "4");

        assertEquals("1", mock.withStringVarargsReturningString(1));
        assertEquals(null, mock.withStringVarargsReturningString(2));
        
        assertEquals("2", mock.withStringVarargsReturningString(2, "1", "2", "3"));
        assertEquals(null, mock.withStringVarargsReturningString(2, "1", "2"));
        assertEquals(null, mock.withStringVarargsReturningString(2, "1", "2", "3", "4"));
        assertEquals(null, mock.withStringVarargsReturningString(2, "1", "2", "9999"));
        
        mock.withStringVarargs(3, "1", "2", "3", "9999");
        mock.withStringVarargs(9999, "1", "2", "3", "4");
        
        try {
            mock.withStringVarargs(3, "1", "2", "3", "4");
            fail();
        } catch (Exception e) {
            assertEquals(expected, e);
        }
    }

// org.mockitousage.basicapi.UsingVarargsTest::shouldStubBooleanVarargs
    public void shouldStubBooleanVarargs() {
        when(mock.withBooleanVarargs(1)).thenReturn(true);
        when(mock.withBooleanVarargs(1, true, false)).thenReturn(true);
        
        assertEquals(true, mock.withBooleanVarargs(1));
        assertEquals(false, mock.withBooleanVarargs(9999));
        
        assertEquals(true, mock.withBooleanVarargs(1, true, false));
        assertEquals(false, mock.withBooleanVarargs(1, true, false, true));
        assertEquals(false, mock.withBooleanVarargs(2, true, false));
        assertEquals(false, mock.withBooleanVarargs(1, true));
        assertEquals(false, mock.withBooleanVarargs(1, false, false));
    }

// org.mockitousage.basicapi.UsingVarargsTest::shouldVerifyStringVarargs
    public void shouldVerifyStringVarargs() {
        mock.withStringVarargs(1);
        mock.withStringVarargs(2, "1", "2", "3");
        mock.withStringVarargs(3, "1", "2", "3", "4");

        verify(mock).withStringVarargs(1);
        verify(mock).withStringVarargs(2, "1", "2", "3");
        try {
            verify(mock).withStringVarargs(2, "1", "2", "79", "4");
            fail();
        } catch (ArgumentsAreDifferent e) {}
    }

// org.mockitousage.basicapi.UsingVarargsTest::shouldVerifyObjectVarargs
    public void shouldVerifyObjectVarargs() {
        mock.withObjectVarargs(1);
        mock.withObjectVarargs(2, "1", new ArrayList<Object>(), new Integer(1));
        mock.withObjectVarargs(3, new Integer(1));

        verify(mock).withObjectVarargs(1);
        verify(mock).withObjectVarargs(2, "1", new ArrayList<Object>(), new Integer(1));
        try {
            verifyNoMoreInteractions(mock);
            fail();
        } catch (NoInteractionsWanted e) {}
    }

// org.mockitousage.basicapi.UsingVarargsTest::shouldVerifyBooleanVarargs
    public void shouldVerifyBooleanVarargs() {
        mock.withBooleanVarargs(1);
        mock.withBooleanVarargs(2, true, false, true);
        mock.withBooleanVarargs(3, true, true, true);

        verify(mock).withBooleanVarargs(1);
        verify(mock).withBooleanVarargs(2, true, false, true);
        try {
            verify(mock).withBooleanVarargs(3, true, true, true, true);
            fail();
        } catch (ArgumentsAreDifferent e) {}
    }

// org.mockitousage.basicapi.UsingVarargsTest::shouldVerifyWithAnyObject
    public void shouldVerifyWithAnyObject() {
        Foo foo = Mockito.mock(Foo.class);
        foo.varArgs("");        
        Mockito.verify(foo).varArgs((String[]) Mockito.anyObject());
        Mockito.verify(foo).varArgs((String) Mockito.anyObject());
    }

// org.mockitousage.basicapi.UsingVarargsTest::shouldVerifyWithNullVarArgArray
    public void shouldVerifyWithNullVarArgArray() {
        Foo foo = Mockito.mock(Foo.class);
        foo.varArgs((String[]) null);    
        Mockito.verify(foo).varArgs((String[]) Mockito.anyObject());
        Mockito.verify(foo).varArgs((String[]) null);
    }

// org.mockitousage.basicapi.UsingVarargsTest::shouldStubCorrectlyWhenMixedVarargsUsed
    public void shouldStubCorrectlyWhenMixedVarargsUsed() {
        MixedVarargs mixedVarargs = mock(MixedVarargs.class);
        when(mixedVarargs.doSomething("hello", (String[])null)).thenReturn("hello");
        when(mixedVarargs.doSomething("goodbye", (String[])null)).thenReturn("goodbye");

        String result = mixedVarargs.doSomething("hello",(String[]) null);
        assertEquals("hello", result);
        
        verify(mixedVarargs).doSomething("hello", (String[])null);
    }

// org.mockitousage.basicapi.UsingVarargsTest::shouldStubCorrectlyWhenDoubleStringAndMixedVarargsUsed
    public void shouldStubCorrectlyWhenDoubleStringAndMixedVarargsUsed() {
        MixedVarargs mixedVarargs = mock(MixedVarargs.class);
        when(mixedVarargs.doSomething("one", "two", (String[])null)).thenReturn("hello");
        when(mixedVarargs.doSomething("1", "2", (String[])null)).thenReturn("goodbye");

        String result = mixedVarargs.doSomething("one", "two", (String[])null);
        assertEquals("hello", result);
    }

// org.mockitousage.basicapi.UsingVarargsTest::shouldMatchEasilyEmptyVararg
    public void shouldMatchEasilyEmptyVararg() throws Exception {
        
        when(mock.foo(anyVararg())).thenReturn(-1);

        
        assertEquals(-1, mock.foo());
    }

// org.mockitousage.bugs.AIOOBExceptionWithAtLeastTest::testCompleteProgress
    public void testCompleteProgress() throws Exception {
        IProgressMonitor progressMonitor = mock(IProgressMonitor.class);

        progressMonitor.beginTask("foo", 12);
        progressMonitor.worked(10);
        progressMonitor.done();

        verify(progressMonitor).beginTask(anyString(), anyInt());
        verify(progressMonitor, atLeastOnce()).worked(anyInt());
    }

// org.mockitousage.bugs.ActualInvocationHasNullArgumentNPEBugTest::shouldAllowPassingNullArgument
    public void shouldAllowPassingNullArgument() {
        
        Fun mockFun = mock(Fun.class);
        when(mockFun.doFun((String) anyObject())).thenReturn("value");

        
        mockFun.doFun(null);

        
        try {
            verify(mockFun).doFun("hello");
            fail();
        } catch(AssertionError r) {
            
        }
    }

// org.mockitousage.bugs.BridgeMethodsHitAgainTest::basicCheck
  public void basicCheck() {
    Mockito.when((someSubInterface).factory()).thenReturn(extendedFactory);
    SomeInterface si = someSubInterface;
    assertTrue(si.factory() != null);
  }

// org.mockitousage.bugs.BridgeMethodsHitAgainTest::checkWithExtraCast
  public void checkWithExtraCast() {
    Mockito.when(((SomeInterface) someSubInterface).factory()).thenReturn(extendedFactory);
    SomeInterface si = someSubInterface;
    assertTrue(si.factory() != null);
  }

// org.mockitousage.bugs.CaptorAnnotationAutoboxingTest::shouldAutoboxSafely
    public void shouldAutoboxSafely() {
        
        fun.doFun(1.0);
        
        
        verify(fun).doFun(captor.capture());
        assertEquals((Double) 1.0, captor.getValue());
    }

// org.mockitousage.bugs.CaptorAnnotationAutoboxingTest::shouldAutoboxAllPrimitives
    public void shouldAutoboxAllPrimitives() {
        verify(fun, never()).moreFun(intCaptor.capture());
    }

// org.mockitousage.bugs.ClassCastExOnVerifyZeroInteractionsTest::should_not_throw_ClassCastException_when_mock_verification_fails
    public void should_not_throw_ClassCastException_when_mock_verification_fails() {
        TestMock test = mock(TestMock.class, new Answer() {
            public Object answer(InvocationOnMock invocation) throws Throwable {
                return false;
            }
        });
        test.m1();
        verifyZeroInteractions(test);
    }

// org.mockitousage.bugs.ClassCastExOnVerifyZeroInteractionsTest::should_report_bogus_default_answer
    public void should_report_bogus_default_answer() throws Exception {
        TestMock test = mock(TestMock.class, new Answer() {
            public Object answer(InvocationOnMock invocation) throws Throwable {
                return false;
            }
        });

        test.toString();
    }

// org.mockitousage.bugs.ConcurrentModificationExceptionOnMultiThreadedVerificationTest::shouldSuccessfullyVerifyConcurrentInvocationsWithTimeout
    public void shouldSuccessfullyVerifyConcurrentInvocationsWithTimeout() throws Exception {
        int potentialOverhead = 1000; 
        int expectedMaxTestLength = TIMES * INTERVAL_MILLIS + potentialOverhead;

        reset(target);
        startInvocations();
        
        verify(target, timeout(expectedMaxTestLength).times(TIMES * nThreads)).targetMethod("arg");
        verifyNoMoreInteractions(target);
    }

// org.mockitousage.bugs.CovariantOverrideTest::returnFoo1
    public void returnFoo1() {
        ReturnsObject mock = mock(ReturnsObject.class);
        when(mock.callMe()).thenReturn("foo");
        assertEquals("foo", mock.callMe()); 
    }

// org.mockitousage.bugs.CovariantOverrideTest::returnFoo2
    public void returnFoo2() {
        ReturnsString mock = mock(ReturnsString.class);
        when(mock.callMe()).thenReturn("foo");
        assertEquals("foo", mock.callMe()); 
    }

// org.mockitousage.bugs.CovariantOverrideTest::returnFoo3
    public void returnFoo3() {
        ReturnsObject mock = mock(ReturnsString.class);
        when(mock.callMe()).thenReturn("foo");
        assertEquals("foo", mock.callMe()); 
    }

// org.mockitousage.bugs.CovariantOverrideTest::returnFoo4
    public void returnFoo4() {
        ReturnsString mock = mock(ReturnsString.class);
        mock.callMe(); 
        ReturnsObject mock2 = mock; 
        verify(mock2).callMe(); 
    }

// org.mockitousage.bugs.DeepStubsWronglyReportsSerializationProblemsTest::should_not_raise_a_mockito_exception_about_serialization_when_accessing_deep_stub
    public void should_not_raise_a_mockito_exception_about_serialization_when_accessing_deep_stub() {
        NotSerializableShouldBeMocked the_deep_stub = mock(ToBeDeepStubbed.class, RETURNS_DEEP_STUBS).getSomething();
        assertThat(the_deep_stub).isNotNull();
    }

// org.mockitousage.bugs.IOOBExceptionShouldNotBeThrownWhenNotCodingFluentlyTest::second_stubbing_throws_IndexOutOfBoundsException
    public void second_stubbing_throws_IndexOutOfBoundsException() throws Exception {
        Map<String, String> map = mock(Map.class);

        OngoingStubbing<String> mapOngoingStubbing = when(map.get(anyString()));

        mapOngoingStubbing.thenReturn("first stubbing");

        try {
            mapOngoingStubbing.thenReturn("second stubbing");
            fail();
        } catch (MockitoException e) {
            assertThat(e.getMessage())
                    .contains("Incorrect use of API detected here")
                    .contains(this.getClass().getSimpleName());
        }
    }

// org.mockitousage.bugs.InheritedGenericsPolimorphicCallTest::shouldStubbingWork
    public void shouldStubbingWork() {
        Mockito.when(iterable.iterator()).thenReturn(myIterator);
        Assert.assertNotNull(((Iterable) iterable).iterator());
        Assert.assertNotNull(iterable.iterator());
    }

// org.mockitousage.bugs.InheritedGenericsPolimorphicCallTest::shouldVerificationWorks
    public void shouldVerificationWorks() {
        iterable.iterator();
        
        verify(iterable).iterator();
        verify((Iterable) iterable).iterator();
    }

// org.mockitousage.bugs.InheritedGenericsPolimorphicCallTest::shouldWorkExactlyAsJavaProxyWould
    public void shouldWorkExactlyAsJavaProxyWould() {
        
        final List<Method> methods = new LinkedList<Method>();
        InvocationHandler handler = new InvocationHandler() {
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            methods.add(method);
            return null;
        }};
            
        iterable = (MyIterable) Proxy.newProxyInstance(
                this.getClass().getClassLoader(),
                new Class[] { MyIterable.class },
                handler);

        
        iterable.iterator();
        ((Iterable) iterable).iterator();
        
        
        assertEquals(2, methods.size());
        assertEquals(methods.get(0), methods.get(1));
    }

// org.mockitousage.bugs.ListenersLostOnResetMockTest::listener
    public void listener() throws Exception {
        InvocationListener invocationListener = mock(InvocationListener.class);

        List mockedList = mock(List.class, withSettings().invocationListeners(invocationListener));
        reset(mockedList);

        mockedList.clear();

        verify(invocationListener).reportInvocation(any(MethodInvocationReport.class));
    }

// org.mockitousage.bugs.MultipleInOrdersTest::inOrderTest
    public void inOrderTest(){
        List list= mock(List.class);
        
        list.add("a");
        list.add("x");
        list.add("b");
        list.add("y");
        
        InOrder inOrder = inOrder(list);
        InOrder inAnotherOrder = inOrder(list);
        assertNotSame(inOrder, inAnotherOrder);
        
        inOrder.verify(list).add("a");
        inOrder.verify(list).add("b");
        
        inAnotherOrder.verify(list).add("x");
        inAnotherOrder.verify(list).add("y");
    }

// org.mockitousage.bugs.NPEOnAnyClassMatcherAutounboxTest::shouldNotThrowNPE
    public void shouldNotThrowNPE() {
        Foo f = mock(Foo.class);
        f.bar(1);
        verify(f).bar(any(Long.class));
    }

// org.mockitousage.bugs.NPEWhenMockingThrowablesTest::shouldNotThrowNPE
    public void shouldNotThrowNPE() {
        when(mock.simpleMethod()).thenThrow(mock2);
        try {
            mock.simpleMethod();
            fail();
        } catch(DummyException e) {}
    }

// org.mockitousage.bugs.NPEWithCertainMatchersTest::shouldNotThrowNPEWhenIntegerPassed
    public void shouldNotThrowNPEWhenIntegerPassed() {
        mock.intArgumentMethod(100);

        verify(mock).intArgumentMethod(isA(Integer.class));
    }

// org.mockitousage.bugs.NPEWithCertainMatchersTest::shouldNotThrowNPEWhenIntPassed
    public void shouldNotThrowNPEWhenIntPassed() {
        mock.intArgumentMethod(100);
        
        verify(mock).intArgumentMethod(isA(Integer.class));
    }

// org.mockitousage.bugs.NPEWithCertainMatchersTest::shouldNotThrowNPEWhenIntegerPassedToEq
    public void shouldNotThrowNPEWhenIntegerPassedToEq() {
        mock.intArgumentMethod(100);
        
        verify(mock).intArgumentMethod(eq(new Integer(100)));
    }

// org.mockitousage.bugs.NPEWithCertainMatchersTest::shouldNotThrowNPEWhenIntegerPassedToSame
    public void shouldNotThrowNPEWhenIntegerPassedToSame() {
        mock.intArgumentMethod(100);

        verify(mock, never()).intArgumentMethod(same(new Integer(100)));
    }

// org.mockitousage.bugs.NPEWithCertainMatchersTest::shouldNotThrowNPEWhenNullPassedToEq
    public void shouldNotThrowNPEWhenNullPassedToEq() {
        mock.objectArgMethod("not null");

        verify(mock).objectArgMethod(eq(null));
    }

// org.mockitousage.bugs.NPEWithCertainMatchersTest::shouldNotThrowNPEWhenNullPassedToSame
    public void shouldNotThrowNPEWhenNullPassedToSame() {
        mock.objectArgMethod("not null");

        verify(mock).objectArgMethod(same(null));
    }

// org.mockitousage.bugs.ShouldAllowInlineMockCreationTest::shouldAllowInlineMockCreation
    public void shouldAllowInlineMockCreation() {
        when(list.get(0)).thenReturn(mock(Set.class));
        assertTrue(list.get(0) instanceof Set);
    }

// org.mockitousage.bugs.ShouldMocksCompareToBeConsistentWithEqualsTest::should_compare_to_be_consistent_with_equals
    public void should_compare_to_be_consistent_with_equals() {
        
        Date today    = mock(Date.class);
        Date tomorrow = mock(Date.class);

        
        Set<Date> set = new TreeSet<Date>();
        set.add(today);
        set.add(tomorrow);

        
        assertEquals(2, set.size());
    }

// org.mockitousage.bugs.ShouldMocksCompareToBeConsistentWithEqualsTest::should_compare_to_be_consistent_with_equals_when_comparing_the_same_reference
    public void should_compare_to_be_consistent_with_equals_when_comparing_the_same_reference() {
        
        Date today    = mock(Date.class);

        
        Set<Date> set = new TreeSet<Date>();
        set.add(today);
        set.add(today);

        
        assertEquals(1, set.size());
    }

// org.mockitousage.bugs.ShouldMocksCompareToBeConsistentWithEqualsTest::should_allow_stubbing_and_verifying_compare_to
    public void should_allow_stubbing_and_verifying_compare_to() {
        
        Date mock    = mock(Date.class);
        when(mock.compareTo(any(Date.class))).thenReturn(10);

        
        mock.compareTo(new Date());

        
        assertEquals(10, mock.compareTo(new Date()));
        verify(mock, atLeastOnce()).compareTo(any(Date.class));
    }

// org.mockitousage.bugs.ShouldMocksCompareToBeConsistentWithEqualsTest::should_reset_not_remove_default_stubbing
    public void should_reset_not_remove_default_stubbing() {
        
        Date mock    = mock(Date.class);
        reset(mock);

        
        assertEquals(1, mock.compareTo(new Date()));
    }

// org.mockitousage.bugs.ShouldNotDeadlockAnswerExecutionTest::failIfMockIsSharedBetweenThreads
    public void failIfMockIsSharedBetweenThreads() throws Exception {
        Service service = Mockito.mock(Service.class);
        ExecutorService threads = Executors.newCachedThreadPool();
        AtomicInteger counter = new AtomicInteger(2);

        

        Mockito.when(service.verySlowMethod()).thenAnswer(new LockingAnswer(counter));

        

        threads.execute(new ServiceRunner(service));
        threads.execute(new ServiceRunner(service));

        

        threads.shutdown();

        if (!threads.awaitTermination(1000, TimeUnit.MILLISECONDS)) {
            
            Assert.fail();
        }
    }

// org.mockitousage.bugs.ShouldNotDeadlockAnswerExecutionTest::successIfEveryThreadHasItsOwnMock
    public void successIfEveryThreadHasItsOwnMock() {}

// org.mockitousage.bugs.ShouldOnlyModeAllowCapturingArgumentsTest::shouldAllowCapturingArguments
    public void shouldAllowCapturingArguments() {
        
        mock.simpleMethod("o");
        ArgumentCaptor<String> arg = ArgumentCaptor.forClass(String.class);
        
        
        verify(mock, only()).simpleMethod(arg.capture());

        
        assertEquals("o", arg.getValue());
    }

// org.mockitousage.bugs.SpyShouldHaveNiceNameTest::shouldPrintNiceName
    public void shouldPrintNiceName() {
        
        veryCoolSpy.add(1);

        try {
            verify(veryCoolSpy).add(2);
            fail();
        } catch(AssertionError e) {
            Assertions.assertThat(e.getMessage()).contains("veryCoolSpy");
        }
    }

// org.mockitousage.bugs.StubbingMocksThatAreConfiguredToReturnMocksTest::shouldAllowStubbingMocksConfiguredWithRETURNS_MOCKS
    public void shouldAllowStubbingMocksConfiguredWithRETURNS_MOCKS() {
        IMethods mock = mock(IMethods.class, RETURNS_MOCKS);
        when(mock.objectReturningMethodNoArgs()).thenReturn(null);
    }

// org.mockitousage.bugs.StubbingMocksThatAreConfiguredToReturnMocksTest::shouldAllowStubbingMocksConfiguredWithRETURNS_MOCKSWithDoApi
    public void shouldAllowStubbingMocksConfiguredWithRETURNS_MOCKSWithDoApi() {
        IMethods mock = mock(IMethods.class, RETURNS_MOCKS);
        doReturn(null).when(mock).objectReturningMethodNoArgs();
    }

// org.mockitousage.bugs.VarargsErrorWhenCallingRealMethodTest::shouldNotThrowAnyException
    public void shouldNotThrowAnyException() throws Exception {
        Foo foo = mock(Foo.class);

        when(foo.blah(anyString(), anyString())).thenCallRealMethod();

        assertEquals(1, foo.blah("foo", "bar"));
    }

// org.mockitousage.bugs.VerifyingWithAnExtraCallToADifferentMockTest::shouldAllowVerifyingWhenOtherMockCallIsInTheSameLine
    public void shouldAllowVerifyingWhenOtherMockCallIsInTheSameLine() {
        
        when(mock.otherMethod()).thenReturn("foo");
        
        
        mockTwo.simpleMethod("foo");
        
        
        verify(mockTwo).simpleMethod(mock.otherMethod());
        try {
            verify(mockTwo, never()).simpleMethod(mock.otherMethod());
            fail();
        } catch (NeverWantedButInvoked e) {}
    }

// org.mockitousage.bugs.deepstubs.DeepStubFailingWhenGenericNestedAsRawTypeTest::discoverDeepMockingOfGenerics
  public void discoverDeepMockingOfGenerics() {
    MyClass1 myMock1 = mock(MyClass1.class, RETURNS_DEEP_STUBS);
    when(myMock1.getNested().getNested().returnSomething()).thenReturn("Hello World.");
  }

// org.mockitousage.bugs.varargs.VarargsAndAnyObjectPicksUpExtraInvocationsTest::shouldVerifyCorrectlyWithAnyVarargs
    public void shouldVerifyCorrectlyWithAnyVarargs() {
        
        table.newRow("qux", "foo", "bar", "baz");
        table.newRow("abc", "def");
        
        
        verify(table, times(2)).newRow(anyString(), (String[]) anyVararg());
    }

// org.mockitousage.bugs.varargs.VarargsAndAnyObjectPicksUpExtraInvocationsTest::shouldVerifyCorrectlyNumberOfInvocationsUsingAnyVarargAndEqualArgument
    public void shouldVerifyCorrectlyNumberOfInvocationsUsingAnyVarargAndEqualArgument() {
        
        table.newRow("x", "foo", "bar", "baz");
        table.newRow("x", "def");

        
        verify(table, times(2)).newRow(eq("x"), (String[]) anyVararg());
    }

// org.mockitousage.bugs.varargs.VarargsAndAnyObjectPicksUpExtraInvocationsTest::shouldVerifyCorrectlyNumberOfInvocationsWithVarargs
    public void shouldVerifyCorrectlyNumberOfInvocationsWithVarargs() {
        
        table.newRow("qux", "foo", "bar", "baz");
        table.newRow("abc", "def");
        
        
        verify(table).newRow(anyString(), eq("foo"), anyString(), anyString());
        verify(table).newRow(anyString(), anyString());
    }

// org.mockitousage.bugs.varargs.VarargsNotPlayingWithAnyObjectTest::shouldMatchAnyVararg
    public void shouldMatchAnyVararg() {
        mock.run("a", "b");

        verify(mock).run(anyString(), anyString());
        verify(mock).run((String) anyObject(), (String) anyObject());

        verify(mock).run((String[]) anyVararg());
        
        verify(mock, never()).run();
        verify(mock, never()).run(anyString(), eq("f"));
    }

// org.mockitousage.bugs.varargs.VarargsNotPlayingWithAnyObjectTest::shouldNotAllowUsingAnyObjectForVarArgs
    public void shouldNotAllowUsingAnyObjectForVarArgs() {
        mock.run("a", "b");

        try {
            verify(mock).run((String[]) anyObject());
            fail();
        } catch (AssertionError e) {}
    }

// org.mockitousage.bugs.varargs.VarargsNotPlayingWithAnyObjectTest::shouldStubUsingAnyVarargs
    public void shouldStubUsingAnyVarargs() {
        when(mock.run((String[]) anyVararg())).thenReturn("foo");
        
        assertEquals("foo", mock.run("a", "b"));
    }

// org.mockitousage.configuration.CustomizedAnnotationForSmartMockTest::shouldUseCustomAnnotation
    public void shouldUseCustomAnnotation() {
        assertEquals("SmartMock should return empty String by default", "", smartMock.simpleMethod(1));
        verify(smartMock).simpleMethod(1);
    }

// org.mockitousage.constructor.CreatingMocksWithConstructorTest::can_create_mock_with_constructor
    public void can_create_mock_with_constructor() {
        Message mock = mock(Message.class, withSettings().useConstructor().defaultAnswer(CALLS_REAL_METHODS));
        
        assertEquals("hey!", mock.getMessage());
    }

// org.mockitousage.constructor.CreatingMocksWithConstructorTest::can_mock_abstract_classes
    public void can_mock_abstract_classes() {
        AbstractMessage mock = mock(AbstractMessage.class, withSettings().useConstructor().defaultAnswer(CALLS_REAL_METHODS));
        assertEquals("hey!", mock.getMessage());
    }

// org.mockitousage.constructor.CreatingMocksWithConstructorTest::can_spy_abstract_classes
    public void can_spy_abstract_classes() {
        AbstractMessage mock = spy(AbstractMessage.class);
        assertEquals("hey!", mock.getMessage());
    }

// org.mockitousage.constructor.CreatingMocksWithConstructorTest::can_mock_inner_classes
    public void can_mock_inner_classes() {
        InnerClass mock = mock(InnerClass.class, withSettings().useConstructor().outerInstance(this).defaultAnswer(CALLS_REAL_METHODS));
        assertEquals("hey!", mock.getMessage());
    }

// org.mockitousage.constructor.CreatingMocksWithConstructorTest::exception_message_when_constructor_not_found
    public void exception_message_when_constructor_not_found() {
        try {
            
            spy(HasConstructor.class);
            
            fail();
        } catch (MockitoException e) {
            assertEquals("Unable to create mock instance of type 'HasConstructor'", e.getMessage());
            assertContains("0-arg constructor", e.getCause().getMessage());
        }
    }

// org.mockitousage.constructor.CreatingMocksWithConstructorTest::mocking_inner_classes_with_wrong_outer_instance
    public void mocking_inner_classes_with_wrong_outer_instance() {
        try {
            
            mock(InnerClass.class, withSettings().useConstructor().outerInstance("foo").defaultAnswer(CALLS_REAL_METHODS));
            
            fail();
        } catch (MockitoException e) {
            assertEquals("Unable to create mock instance of type 'InnerClass'", e.getMessage());
            assertContains("Please ensure that the outer instance has correct type and that the target class has 0-arg constructor.", e.getCause().getMessage());
        }
    }

// org.mockitousage.constructor.CreatingMocksWithConstructorTest::mocking_interfaces_with_constructor
    public void mocking_interfaces_with_constructor() {
        
        
        mock(IMethods.class, withSettings().useConstructor());
        spy(IMethods.class);
    }

// org.mockitousage.constructor.CreatingMocksWithConstructorTest::prevents_across_jvm_serialization_with_constructor
    public void prevents_across_jvm_serialization_with_constructor() {
        try {
            
            mock(AbstractMessage.class, withSettings().useConstructor().serializable(SerializableMode.ACROSS_CLASSLOADERS));
            
            fail();
        } catch (MockitoException e) {
            assertEquals("Mocks instantiated with constructor cannot be combined with " + SerializableMode.ACROSS_CLASSLOADERS + " serialization mode.", e.getMessage());
        }
    }

// org.mockitousage.constructor.CreatingMocksWithConstructorTest::abstract_method_returns_default
    public void abstract_method_returns_default() {
        AbstractThing thing = spy(AbstractThing.class);
        assertEquals("abstract null", thing.fullName());
    }

// org.mockitousage.constructor.CreatingMocksWithConstructorTest::abstract_method_stubbed
    public void abstract_method_stubbed() {
        AbstractThing thing = spy(AbstractThing.class);
        when(thing.name()).thenReturn("me");
        assertEquals("abstract me", thing.fullName());
    }

// org.mockitousage.constructor.CreatingMocksWithConstructorTest::calls_real_interface_method
    public void calls_real_interface_method() {
        List list = mock(List.class, withSettings().defaultAnswer(CALLS_REAL_METHODS));
        assertNull(list.get(1));
    }

// org.mockitousage.customization.BDDMockitoTest::shouldStub
    public void shouldStub() throws Exception {
        given(mock.simpleMethod("foo")).willReturn("bar");

        assertEquals("bar", mock.simpleMethod("foo"));
        assertEquals(null, mock.simpleMethod("whatever"));
    }

// org.mockitousage.customization.BDDMockitoTest::shouldStubWithThrowable
    public void shouldStubWithThrowable() throws Exception {
        given(mock.simpleMethod("foo")).willThrow(new RuntimeException());

        try {
            assertEquals("foo", mock.simpleMethod("foo"));
            fail();
        } catch(RuntimeException e) {}
    }

// org.mockitousage.customization.BDDMockitoTest::shouldStubWithThrowableClass
    public void shouldStubWithThrowableClass() throws Exception {
        given(mock.simpleMethod("foo")).willThrow(RuntimeException.class);

        try {
            assertEquals("foo", mock.simpleMethod("foo"));
            fail();
        } catch(RuntimeException e) {}
    }

// org.mockitousage.customization.BDDMockitoTest::shouldStubWithAnswer
    public void shouldStubWithAnswer() throws Exception {
        given(mock.simpleMethod(anyString())).willAnswer(new Answer<String>() {
            public String answer(InvocationOnMock invocation) throws Throwable {
                return (String) invocation.getArguments()[0];
            }});

        assertEquals("foo", mock.simpleMethod("foo"));
    }

// org.mockitousage.customization.BDDMockitoTest::shouldStubWithWillAnswerAlias
    public void shouldStubWithWillAnswerAlias() throws Exception {
        given(mock.simpleMethod(anyString())).will(new Answer<String>() {
            public String answer(InvocationOnMock invocation) throws Throwable {
                return (String) invocation.getArguments()[0];
            }});

        assertEquals("foo", mock.simpleMethod("foo"));
    }

// org.mockitousage.customization.BDDMockitoTest::shouldStubConsecutively
    public void shouldStubConsecutively() throws Exception {
       given(mock.simpleMethod(anyString()))
           .willReturn("foo")
           .willReturn("bar");

       assertEquals("foo", mock.simpleMethod("whatever"));
       assertEquals("bar", mock.simpleMethod("whatever"));
    }

// org.mockitousage.customization.BDDMockitoTest::shouldStubConsecutivelyWithCallRealMethod
    public void shouldStubConsecutivelyWithCallRealMethod() throws Exception {
        MethodsImpl mock = mock(MethodsImpl.class);
        willReturn("foo").willCallRealMethod()
                .given(mock).simpleMethod();

       assertEquals("foo", mock.simpleMethod());
       assertEquals(null, mock.simpleMethod());
    }

// org.mockitousage.customization.BDDMockitoTest::shouldStubVoid
    public void shouldStubVoid() throws Exception {
        willThrow(new RuntimeException()).given(mock).voidMethod();

        try {
            mock.voidMethod();
            fail();
        } catch(RuntimeException e) {}
    }

// org.mockitousage.customization.BDDMockitoTest::shouldStubVoidWithExceptionClass
    public void shouldStubVoidWithExceptionClass() throws Exception {
        willThrow(RuntimeException.class).given(mock).voidMethod();

        try {
            mock.voidMethod();
            fail();
        } catch(RuntimeException e) {}
    }

// org.mockitousage.customization.BDDMockitoTest::shouldStubVoidConsecutively
    public void shouldStubVoidConsecutively() throws Exception {
        willDoNothing()
        .willThrow(new RuntimeException())
        .given(mock).voidMethod();

        mock.voidMethod();
        try {
            mock.voidMethod();
            fail();
        } catch(RuntimeException e) {}
    }

// org.mockitousage.customization.BDDMockitoTest::shouldStubVoidConsecutivelyWithExceptionClass
    public void shouldStubVoidConsecutivelyWithExceptionClass() throws Exception {
        willDoNothing()
        .willThrow(IllegalArgumentException.class)
        .given(mock).voidMethod();

        mock.voidMethod();
        try {
            mock.voidMethod();
            fail();
        } catch(IllegalArgumentException e) {}
    }

// org.mockitousage.customization.BDDMockitoTest::shouldStubUsingDoReturnStyle
    public void shouldStubUsingDoReturnStyle() throws Exception {
        willReturn("foo").given(mock).simpleMethod("bar");

        assertEquals(null, mock.simpleMethod("boooo"));
        assertEquals("foo", mock.simpleMethod("bar"));
    }

// org.mockitousage.customization.BDDMockitoTest::shouldStubUsingDoAnswerStyle
    public void shouldStubUsingDoAnswerStyle() throws Exception {
        willAnswer(new Answer<String>() {
            public String answer(InvocationOnMock invocation) throws Throwable {
                return (String) invocation.getArguments()[0];
            }})
        .given(mock).simpleMethod(anyString());

        assertEquals("foo", mock.simpleMethod("foo"));
    }

// org.mockitousage.customization.BDDMockitoTest::shouldStubByDelegatingToRealMethod
    public void shouldStubByDelegatingToRealMethod() throws Exception {
        
        Dog dog = mock(Dog.class);
        
        willCallRealMethod().given(dog).bark();
        
        assertEquals("woof", dog.bark());
    }

// org.mockitousage.customization.BDDMockitoTest::shouldStubByDelegatingToRealMethodUsingTypicalStubbingSyntax
    public void shouldStubByDelegatingToRealMethodUsingTypicalStubbingSyntax() throws Exception {
        
        Dog dog = mock(Dog.class);
        
        given(dog.bark()).willCallRealMethod();
        
        assertEquals("woof", dog.bark());
    }

// org.mockitousage.customization.BDDMockitoTest::shouldAllStubbedMockReferenceAccess
    public void shouldAllStubbedMockReferenceAccess() throws Exception {
        Set expectedMock = mock(Set.class);

        Set returnedMock = given(expectedMock.isEmpty()).willReturn(false).getMock();

        assertEquals(expectedMock, returnedMock);
    }

// org.mockitousage.customization.BDDMockitoTest::shouldValidateMockWhenVerifying
    public void shouldValidateMockWhenVerifying() {

        then("notMock").should();
    }

// org.mockitousage.customization.BDDMockitoTest::shouldValidateMockWhenVerifyingWithExpectedNumberOfInvocations
    public void shouldValidateMockWhenVerifyingWithExpectedNumberOfInvocations() {

        then("notMock").should(times(19));
    }

// org.mockitousage.customization.BDDMockitoTest::shouldValidateMockWhenVerifyingNoMoreInteractions
    public void shouldValidateMockWhenVerifyingNoMoreInteractions() {

        then("notMock").should();
    }

// org.mockitousage.customization.BDDMockitoTest::shouldFailForExpectedBehaviorThatDidNotHappen
    public void shouldFailForExpectedBehaviorThatDidNotHappen() {

        then(mock).should().booleanObjectReturningMethod();
    }

// org.mockitousage.customization.BDDMockitoTest::shouldPassForExpectedBehaviorThatHappened
    public void shouldPassForExpectedBehaviorThatHappened() {

        mock.booleanObjectReturningMethod();

        then(mock).should().booleanObjectReturningMethod();
    }

// org.mockitousage.customization.BDDMockitoTest::shouldPassFluentBddScenario
    public void shouldPassFluentBddScenario() {

        Bike bike = new Bike();
        Person person = mock(Person.class);

        person.ride(bike);
        person.ride(bike);

        then(person).should(times(2)).ride(bike);
    }

// org.mockitousage.debugging.InvocationListenerCallbackTest::should_call_single_listener_when_mock_return_normally
    public void should_call_single_listener_when_mock_return_normally() throws Exception {
        
        Foo foo = mock(Foo.class, withSettings().invocationListeners(listener1));
        willReturn("basil").given(foo).giveMeSomeString("herb");

        
        foo.giveMeSomeString("herb");

        
        assertThatHasBeenNotified(listener1, "basil", getClass().getSimpleName());
    }

// org.mockitousage.debugging.InvocationListenerCallbackTest::should_call_all_listener_when_mock_return_normally
    public void should_call_all_listener_when_mock_return_normally() throws Exception {
        
        Foo foo = mock(Foo.class, withSettings().invocationListeners(listener1, listener2));
        given(foo.giveMeSomeString("herb")).willReturn("rosemary");

        
        foo.giveMeSomeString("herb");

        
        assertThatHasBeenNotified(listener1, "rosemary", getClass().getSimpleName());
        assertThatHasBeenNotified(listener2, "rosemary", getClass().getSimpleName());
    }

// org.mockitousage.debugging.InvocationListenerCallbackTest::should_call_all_listener_when_mock_throws_exception
    public void should_call_all_listener_when_mock_throws_exception() throws Exception {
        
        InvocationListener listener1 = mock(InvocationListener.class, "listener1");
        InvocationListener listener2 = mock(InvocationListener.class, "listener2");
        Foo foo = mock(Foo.class, withSettings().invocationListeners(listener1, listener2));
        doThrow(new OvenNotWorking()).when(foo).doSomething("cook");

        
        try {
            foo.doSomething("cook");
            fail("Exception expected.");
        } catch (OvenNotWorking actualException) {
            
            InOrder orderedVerify = inOrder(listener1, listener2);
            orderedVerify.verify(listener1).reportInvocation(any(MethodInvocationReport.class));
            orderedVerify.verify(listener2).reportInvocation(any(MethodInvocationReport.class));
        }
    }

// org.mockitousage.debugging.PrintingInvocationsDetectsUnusedStubTest::shouldDetectUnusedStubbingWhenPrinting
    public void shouldDetectUnusedStubbingWhenPrinting() throws Exception {
        
        given(mock.giveMeSomeString("different arg")).willReturn("foo");
        mock.giveMeSomeString("arg");

        
        String log = NewMockito.debug().printInvocations(mock, mockTwo);

        
        assertContainsIgnoringCase("unused", log);
    }

// org.mockitousage.debugging.PrintingInvocationsWhenEverythingOkTest::shouldPrintInvocationsWhenStubbingNotUsed
    public void shouldPrintInvocationsWhenStubbingNotUsed() throws Exception {
        
        performStubbing();
        
        businessLogicWithAsking("arg");
        
        verify(mockTwo).doSomething("foo");
    }

// org.mockitousage.debugging.PrintingInvocationsWhenStubNotUsedTest::shouldPrintInvocationsWhenStubbingNotUsed
    public void shouldPrintInvocationsWhenStubbingNotUsed() throws Exception {
        
        performStubbing();
        
        businessLogicWithAsking("arg");
        
        verify(mockTwo).doSomething("foo");
    }

// org.mockitousage.debugging.VerboseLoggingOfInvocationsOnMockTest::shouldNotPrintInvocationOnMockWithoutSetting
    public void shouldNotPrintInvocationOnMockWithoutSetting() {
        
        Foo foo = mock(Foo.class, withSettings().verboseLogging());

        
        foo.giveMeSomeString("Klipsch");
        unrelatedMock.unrelatedMethod("Apple");

        
        Assertions.assertThat(printed())
                .doesNotContain(mockName(unrelatedMock))
                .doesNotContain("unrelatedMethod")
                .doesNotContain("Apple");
    }

// org.mockitousage.debugging.VerboseLoggingOfInvocationsOnMockTest::shouldPrintUnstubbedInvocationOnMockToStdOut
    public void shouldPrintUnstubbedInvocationOnMockToStdOut() {
        
        Foo foo = mock(Foo.class, withSettings().verboseLogging());

        
        foo.doSomething("Klipsch");

        
        Assertions.assertThat(printed())
                .contains(getClass().getName())
                .contains(mockName(foo))
                .contains("doSomething")
                .contains("Klipsch");
    }

// org.mockitousage.debugging.VerboseLoggingOfInvocationsOnMockTest::shouldPrintStubbedInvocationOnMockToStdOut
    public void shouldPrintStubbedInvocationOnMockToStdOut() {
        
        Foo foo = mock(Foo.class, withSettings().verboseLogging());
        given(foo.giveMeSomeString("Klipsch")).willReturn("earbuds");

        
        foo.giveMeSomeString("Klipsch");

        
        Assertions.assertThat(printed())
                .contains(getClass().getName())
                .contains(mockName(foo))
                .contains("giveMeSomeString")
                .contains("Klipsch")
                .contains("earbuds");
    }

// org.mockitousage.debugging.VerboseLoggingOfInvocationsOnMockTest::shouldPrintThrowingInvocationOnMockToStdOut
    public void shouldPrintThrowingInvocationOnMockToStdOut() {
        
        Foo foo = mock(Foo.class, withSettings().verboseLogging());
        doThrow(new ThirdPartyException()).when(foo).doSomething("Klipsch");

        try {
            
            foo.doSomething("Klipsch");
            fail("Exception excepted.");
        } catch (ThirdPartyException e) {
            
            Assertions.assertThat(printed())
                    .contains(getClass().getName())
                    .contains(mockName(foo))
                    .contains("doSomething")
                    .contains("Klipsch")
                    .contains(ThirdPartyException.class.getName());
        }
    }

// org.mockitousage.debugging.VerboseLoggingOfInvocationsOnMockTest::shouldPrintRealInvocationOnSpyToStdOut
    public void shouldPrintRealInvocationOnSpyToStdOut() {
        
        FooImpl fooSpy = mock(FooImpl.class,
                withSettings().spiedInstance(new FooImpl()).verboseLogging());
        doCallRealMethod().when(fooSpy).doSomething("Klipsch");
        
        
        fooSpy.doSomething("Klipsch");
        
        
        Assertions.assertThat(printed())
                .contains(getClass().getName())
                .contains(mockName(fooSpy))
                .contains("doSomething")
                .contains("Klipsch");
    }

// org.mockitousage.debugging.VerboseLoggingOfInvocationsOnMockTest::usage
    public void usage() {
        
        Foo foo = mock(Foo.class, withSettings().verboseLogging());
        given(foo.giveMeSomeString("Apple")).willReturn(
                "earbuds");

        
        foo.giveMeSomeString("Shure");
        foo.giveMeSomeString("Apple");
        foo.doSomething("Klipsch");
    }

// org.mockitousage.examples.use.ExampleTest::managerCountsArticlesAndSavesThemInTheDatabase
    public void managerCountsArticlesAndSavesThemInTheDatabase() {
        when(mockCalculator.countArticles("Guardian")).thenReturn(12);
        when(mockCalculator.countArticlesInPolish(anyString())).thenReturn(5);

        articleManager.updateArticleCounters("Guardian");
        
        verify(mockDatabase).updateNumberOfArticles("Guardian", 12);
        verify(mockDatabase).updateNumberOfPolishArticles("Guardian", 5);
        verify(mockDatabase).updateNumberOfEnglishArticles("Guardian", 7);
    }

// org.mockitousage.examples.use.ExampleTest::managerCountsArticlesUsingCalculator
    public void managerCountsArticlesUsingCalculator() {
        articleManager.updateArticleCounters("Guardian");

        verify(mockCalculator).countArticles("Guardian");
        verify(mockCalculator).countArticlesInPolish("Guardian");
    }

// org.mockitousage.examples.use.ExampleTest::managerSavesArticlesInTheDatabase
    public void managerSavesArticlesInTheDatabase() {
        articleManager.updateArticleCounters("Guardian");

        verify(mockDatabase).updateNumberOfArticles("Guardian", 0);
        verify(mockDatabase).updateNumberOfPolishArticles("Guardian", 0);
        verify(mockDatabase).updateNumberOfEnglishArticles("Guardian", 0);
    }

// org.mockitousage.examples.use.ExampleTest::managerUpdatesNumberOfRelatedArticles
    public void managerUpdatesNumberOfRelatedArticles() {
        Article articleOne = new Article();
        Article articleTwo = new Article();
        Article articleThree = new Article();
        
        when(mockCalculator.countNumberOfRelatedArticles(articleOne)).thenReturn(1);
        when(mockCalculator.countNumberOfRelatedArticles(articleTwo)).thenReturn(12);
        when(mockCalculator.countNumberOfRelatedArticles(articleThree)).thenReturn(0);
        
        when(mockDatabase.getArticlesFor("Guardian")).thenReturn(Arrays.asList(articleOne, articleTwo, articleThree)); 
        
        articleManager.updateRelatedArticlesCounters("Guardian");

        verify(mockDatabase).save(articleOne);
        verify(mockDatabase).save(articleTwo);
        verify(mockDatabase).save(articleThree);
    }

// org.mockitousage.examples.use.ExampleTest::shouldPersistRecalculatedArticle
    public void shouldPersistRecalculatedArticle() {
        Article articleOne = new Article();
        Article articleTwo = new Article();
        
        when(mockCalculator.countNumberOfRelatedArticles(articleOne)).thenReturn(1);
        when(mockCalculator.countNumberOfRelatedArticles(articleTwo)).thenReturn(12);
        
        when(mockDatabase.getArticlesFor("Guardian")).thenReturn(Arrays.asList(articleOne, articleTwo)); 
        
        articleManager.updateRelatedArticlesCounters("Guardian");

        InOrder inOrder = inOrder(mockDatabase, mockCalculator);
        
        inOrder.verify(mockCalculator).countNumberOfRelatedArticles((Article) anyObject());
        inOrder.verify(mockDatabase, atLeastOnce()).save((Article) anyObject());
    }

// org.mockitousage.junitrunner.JUnit44RunnerTest::shouldInitMocksUsingRunner
    public void shouldInitMocksUsingRunner() {
        list.add("test");
        verify(list).add("test");
    }

// org.mockitousage.junitrunner.JUnit44RunnerTest::shouldInjectMocksUsingRunner
    public void shouldInjectMocksUsingRunner() {
        assertSame(list, listDependent.getList());
    }

// org.mockitousage.junitrunner.JUnit44RunnerTest::shouldFilterTestMethodsCorrectly
    public void shouldFilterTestMethodsCorrectly() throws Exception{
        MockitoJUnit44Runner runner = new MockitoJUnit44Runner(this.getClass());

        runner.filter(methodNameContains("shouldInitMocksUsingRunner"));

        assertEquals(1, runner.testCount());
    }

// org.mockitousage.junitrunner.JUnit45RunnerTest::shouldInitMocksUsingRunner
    public void shouldInitMocksUsingRunner() {
        list.add("test");
        verify(list).add("test");
    }

// org.mockitousage.junitrunner.JUnit45RunnerTest::shouldInjectMocksUsingRunner
    public void shouldInjectMocksUsingRunner() {
        assertNotNull(list);
        assertSame(list, listDependent.getList());
    }

// org.mockitousage.junitrunner.JUnit45RunnerTest::shouldFilterTestMethodsCorrectly
    public void shouldFilterTestMethodsCorrectly() throws Exception{
        MockitoJUnitRunner runner = new MockitoJUnitRunner(this.getClass());

        runner.filter(methodNameContains("shouldInitMocksUsingRunner"));

        assertEquals(1, runner.testCount());
    }

// org.mockitousage.matchers.AnyXMatchersAcceptNullsTest::shouldAcceptNullsInAnyMatcher
    public void shouldAcceptNullsInAnyMatcher() {
        when(mock.oneArg(any())).thenReturn("matched");

        assertEquals(null, mock.forObject(null));
    }

// org.mockitousage.matchers.AnyXMatchersAcceptNullsTest::shouldAcceptNullsInAnyObjectMatcher
    public void shouldAcceptNullsInAnyObjectMatcher() {
        when(mock.oneArg(anyObject())).thenReturn("matched");

        assertEquals(null, mock.forObject(null));
    }

// org.mockitousage.matchers.AnyXMatchersAcceptNullsTest::shouldNotAcceptNullInAnyXMatchers
    public void shouldNotAcceptNullInAnyXMatchers() {
        when(mock.oneArg(anyString())).thenReturn("0");
        when(mock.forList(anyList())).thenReturn("1");
        when(mock.forMap(anyMap())).thenReturn("2");
        when(mock.forCollection(anyCollection())).thenReturn("3");
        when(mock.forSet(anySet())).thenReturn("4");
        
        assertEquals(null, mock.oneArg((Object) null));
        assertEquals(null, mock.oneArg((String) null));
        assertEquals(null, mock.forList(null));
        assertEquals(null, mock.forMap(null));
        assertEquals(null, mock.forCollection(null));
        assertEquals(null, mock.forSet(null));
    }

// org.mockitousage.matchers.AnyXMatchersAcceptNullsTest::shouldNotAcceptNullInAllAnyPrimitiveWrapperMatchers
    public void shouldNotAcceptNullInAllAnyPrimitiveWrapperMatchers() {
        when(mock.forInteger(anyInt())).thenReturn("0");
        when(mock.forCharacter(anyChar())).thenReturn("1");
        when(mock.forShort(anyShort())).thenReturn("2");
        when(mock.forByte(anyByte())).thenReturn("3");
        when(mock.forBoolean(anyBoolean())).thenReturn("4");
        when(mock.forLong(anyLong())).thenReturn("5");
        when(mock.forFloat(anyFloat())).thenReturn("6");
        when(mock.forDouble(anyDouble())).thenReturn("7");
        
        assertEquals(null, mock.forInteger(null));
        assertEquals(null, mock.forCharacter(null));
        assertEquals(null, mock.forShort(null));
        assertEquals(null, mock.forByte(null));
        assertEquals(null, mock.forBoolean(null));
        assertEquals(null, mock.forLong(null));
        assertEquals(null, mock.forFloat(null));
        assertEquals(null, mock.forDouble(null));
    }

// org.mockitousage.matchers.CapturingArgumentsTest::should_allow_assertions_on_captured_argument
    public void should_allow_assertions_on_captured_argument() {
        
        ArgumentCaptor<Person> argument = new ArgumentCaptor<Person>();

        
        bulkEmailService.email(12);

        
        verify(emailService).sendEmailTo(argument.capture());
        assertEquals(12, argument.getValue().getAge());
    }

// org.mockitousage.matchers.CapturingArgumentsTest::should_allow_assertions_on_all_captured_arguments
    public void should_allow_assertions_on_all_captured_arguments() {
        
        ArgumentCaptor<Person> argument = ArgumentCaptor.forClass(Person.class);

        
        bulkEmailService.email(11, 12);

        
        verify(emailService, times(2)).sendEmailTo(argument.capture());
        assertEquals(11, argument.getAllValues().get(0).getAge());
        assertEquals(12, argument.getAllValues().get(1).getAge());
    }

// org.mockitousage.matchers.CapturingArgumentsTest::should_allow_assertions_on_last_argument
    public void should_allow_assertions_on_last_argument() {
        
        ArgumentCaptor<Person> argument = ArgumentCaptor.forClass(Person.class);

        
        bulkEmailService.email(11, 12, 13);

        
        verify(emailService, times(3)).sendEmailTo(argument.capture());
        assertEquals(13, argument.getValue().getAge());
    }

// org.mockitousage.matchers.CapturingArgumentsTest::should_print_captor_matcher
    public void should_print_captor_matcher() {
        
        ArgumentCaptor<Person> person = ArgumentCaptor.forClass(Person.class);
        
        try {
            
            verify(emailService).sendEmailTo(person.capture());
            fail();
        } catch(WantedButNotInvoked e) {
            
            assertContains("<Capturing argument>", e.getMessage());
        }
    }

// org.mockitousage.matchers.CapturingArgumentsTest::should_allow_assertions_on_captured_null
    public void should_allow_assertions_on_captured_null() {
        
        ArgumentCaptor<Person> argument = ArgumentCaptor.forClass(Person.class);

        
        emailService.sendEmailTo(null);

        
        verify(emailService).sendEmailTo(argument.capture());
        assertEquals(null, argument.getValue());
    }

// org.mockitousage.matchers.CapturingArgumentsTest::should_allow_construction_of_captor_for_parameterized_type_in_a_convenient_way
    public void should_allow_construction_of_captor_for_parameterized_type_in_a_convenient_way()  {
        
        ArgumentCaptor<List<Person>> argument = ArgumentCaptor.forClass(List.class);
        assertNotNull(argument);
    }

// org.mockitousage.matchers.CapturingArgumentsTest::should_allow_construction_of_captor_for_a_more_specific_type
    public void should_allow_construction_of_captor_for_a_more_specific_type()  {
        
        ArgumentCaptor<List> argument = ArgumentCaptor.forClass(ArrayList.class);
        assertNotNull(argument);
    }

// org.mockitousage.matchers.CapturingArgumentsTest::should_allow_capturing_for_stubbing
    public void should_allow_capturing_for_stubbing() {
        
        ArgumentCaptor<Person> argument = ArgumentCaptor.forClass(Person.class);
        when(emailService.sendEmailTo(argument.capture())).thenReturn(false);
        
        
        emailService.sendEmailTo(new Person(10));
        
        
        assertEquals(10, argument.getValue().getAge());
    }

// org.mockitousage.matchers.CapturingArgumentsTest::should_capture_when_stubbing_only_when_entire_invocation_matches
    public void should_capture_when_stubbing_only_when_entire_invocation_matches() {
        
        ArgumentCaptor<String> argument = ArgumentCaptor.forClass(String.class);
        when(mock.simpleMethod(argument.capture(), eq(2))).thenReturn("blah");
        
        
        mock.simpleMethod("foo", 200);
        mock.simpleMethod("bar", 2);
        
        
        Assertions.assertThat(argument.getAllValues()).containsOnly("bar");
    }

// org.mockitousage.matchers.CapturingArgumentsTest::should_say_something_smart_when_misused
    public void should_say_something_smart_when_misused() {
        ArgumentCaptor<Person> argument = ArgumentCaptor.forClass(Person.class);
        try {
            argument.getValue();
            fail();
        } catch (MockitoException e) {
            Assert.assertTrue(true);
        }
    }

// org.mockitousage.matchers.CapturingArgumentsTest::should_capture_when_full_arg_list_matches
    public void should_capture_when_full_arg_list_matches() throws Exception {
        
        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);

        
        mock.simpleMethod("foo", 1);
        mock.simpleMethod("bar", 2);

        
        verify(mock).simpleMethod(captor.capture(), eq(1));
        assertEquals(1, captor.getAllValues().size());
        assertEquals("foo", captor.getValue());
    }

// org.mockitousage.matchers.CapturingArgumentsTest::should_capture_int_by_creating_captor_with_primitive_wrapper
    public void should_capture_int_by_creating_captor_with_primitive_wrapper() {
        
        ArgumentCaptor<Integer> argument = ArgumentCaptor.forClass(Integer.class);

        
        mock.intArgumentMethod(10);
        
        
        verify(mock).intArgumentMethod(argument.capture());
        assertEquals(10, (int) argument.getValue());
    }

// org.mockitousage.matchers.CapturingArgumentsTest::should_capture_int_by_creating_captor_with_primitive
    public void should_capture_int_by_creating_captor_with_primitive() throws Exception {
        
        ArgumentCaptor<Integer> argument = ArgumentCaptor.forClass(int.class);
        
        
        mock.intArgumentMethod(10);
        
        
        verify(mock).intArgumentMethod(argument.capture());
        assertEquals(10, (int) argument.getValue());
    }

// org.mockitousage.matchers.CapturingArgumentsTest::should_capture_byte_vararg_by_creating_captor_with_primitive
    public void should_capture_byte_vararg_by_creating_captor_with_primitive() throws Exception {
        
        ArgumentCaptor<Byte> argumentCaptor = ArgumentCaptor.forClass(byte.class);

        
        mock.varargsbyte((byte) 1, (byte) 2);

        
        verify(mock).varargsbyte(argumentCaptor.capture());
        assertEquals((byte) 2, (byte) argumentCaptor.getValue());
        Assertions.assertThat(argumentCaptor.getAllValues()).containsExactly((byte) 1, (byte) 2);
    }

// org.mockitousage.matchers.CapturingArgumentsTest::should_capture_byte_vararg_by_creating_captor_with_primitive_wrapper
    public void should_capture_byte_vararg_by_creating_captor_with_primitive_wrapper() throws Exception {
        
        ArgumentCaptor<Byte> argumentCaptor = ArgumentCaptor.forClass(Byte.class);

        
        mock.varargsbyte((byte) 1, (byte) 2);

        
        verify(mock).varargsbyte(argumentCaptor.capture());
        assertEquals((byte) 2, (byte) argumentCaptor.getValue());
        Assertions.assertThat(argumentCaptor.getAllValues()).containsExactly((byte) 1, (byte) 2);
    }

// org.mockitousage.matchers.CapturingArgumentsTest::should_capture_vararg
    public void should_capture_vararg() throws Exception {
        
        ArgumentCaptor<String> argumentCaptor = ArgumentCaptor.forClass(String.class);

        
        mock.mixedVarargs(42, "a", "b", "c");

        
        verify(mock).mixedVarargs(any(), argumentCaptor.capture());
        Assertions.assertThat(argumentCaptor.getAllValues()).containsExactly("a", "b", "c");
    }

// org.mockitousage.matchers.CapturingArgumentsTest::should_capture_all_vararg
    public void should_capture_all_vararg() throws Exception {
        
        ArgumentCaptor<String> argumentCaptor = ArgumentCaptor.forClass(String.class);

        
        mock.mixedVarargs(42, "a", "b", "c");
        mock.mixedVarargs(42, "again ?!");

        
        verify(mock, times(2)).mixedVarargs(any(), argumentCaptor.capture());

        Assertions.assertThat(argumentCaptor.getAllValues()).containsExactly("a", "b", "c", "again ?!");
    }

// org.mockitousage.matchers.CapturingArgumentsTest::should_capture_one_arg_even_when_using_vararg_captor_on_nonvararg_method
    public void should_capture_one_arg_even_when_using_vararg_captor_on_nonvararg_method() throws Exception {
        
        ArgumentCaptor<String> argumentCaptor = ArgumentCaptor.forClass(String.class);

        
        mock.simpleMethod("a", 2);

        
        verify(mock).simpleMethod(argumentCaptor.capture(), eq(2));
        Assertions.assertThat(argumentCaptor.getAllValues()).containsExactly("a");
    }

// org.mockitousage.matchers.CapturingArgumentsTest::captures_correctly_when_captor_used_multiple_times
    public void captures_correctly_when_captor_used_multiple_times() throws Exception {
        
        ArgumentCaptor<String> argumentCaptor = ArgumentCaptor.forClass(String.class);

        
        mock.mixedVarargs(42, "a", "b", "c");

        
        
        verify(mock).mixedVarargs(any(), argumentCaptor.capture(), argumentCaptor.capture(), argumentCaptor.capture());
        Assertions.assertThat(argumentCaptor.getAllValues()).containsExactly("a", "b", "c");
    }

// org.mockitousage.matchers.CapturingArgumentsTest::captures_correctly_when_captor_used_on_pure_vararg_method
    public void captures_correctly_when_captor_used_on_pure_vararg_method() throws Exception {
        
        ArgumentCaptor<String> argumentCaptor = ArgumentCaptor.forClass(String.class);

        
        mock.varargs(42, "capturedValue");

        
        verify(mock).varargs(eq(42), argumentCaptor.capture());
        Assertions.assertThat(argumentCaptor.getValue()).contains("capturedValue");
    }

// org.mockitousage.matchers.CustomMatcherDoesYieldCCETest::shouldNotThrowCCE
    public void shouldNotThrowCCE() {
        mock.simpleMethod(new Object());

        try {
            
            
            verify(mock).simpleMethod(argThat(isStringWithTextFoo()));
            fail();
        } catch (ArgumentsAreDifferent e) {}
    }

// org.mockitousage.matchers.CustomMatchersTest::shouldUseCustomBooleanMatcher
    public void shouldUseCustomBooleanMatcher() {
        when(mock.oneArg(booleanThat(new IsAnyBoolean()))).thenReturn("foo");
        
        assertEquals("foo", mock.oneArg(true));
        assertEquals("foo", mock.oneArg(false));
        
        assertEquals(null, mock.oneArg("x"));
    }

// org.mockitousage.matchers.CustomMatchersTest::shouldUseCustomCharMatcher
    public void shouldUseCustomCharMatcher() {
        when(mock.oneArg(charThat(new IsSorZ()))).thenReturn("foo");
      
        assertEquals("foo", mock.oneArg('s'));
        assertEquals("foo", mock.oneArg('z'));
        assertEquals(null, mock.oneArg('x'));
    }

// org.mockitousage.matchers.CustomMatchersTest::shouldUseCustomPrimitiveNumberMatchers
    public void shouldUseCustomPrimitiveNumberMatchers() {
        when(mock.oneArg(byteThat(new IsZeroOrOne<Byte>()))).thenReturn("byte");
        when(mock.oneArg(shortThat(new IsZeroOrOne<Short>()))).thenReturn("short");
        when(mock.oneArg(intThat(new IsZeroOrOne<Integer>()))).thenReturn("int");
        when(mock.oneArg(longThat(new IsZeroOrOne<Long>()))).thenReturn("long");
        when(mock.oneArg(floatThat(new IsZeroOrOne<Float>()))).thenReturn("float");
        when(mock.oneArg(doubleThat(new IsZeroOrOne<Double>()))).thenReturn("double");
        
        assertEquals("byte", mock.oneArg((byte) 0));
        assertEquals("short", mock.oneArg((short) 1));
        assertEquals("int", mock.oneArg(0));
        assertEquals("long", mock.oneArg(1L));
        assertEquals("float", mock.oneArg(0F));
        assertEquals("double", mock.oneArg(1.0));
        
        assertEquals(null, mock.oneArg(2));
        assertEquals(null, mock.oneArg("foo"));
    }

// org.mockitousage.matchers.CustomMatchersTest::shouldUseCustomObjectMatcher
    public void shouldUseCustomObjectMatcher() {
        when(mock.oneArg(argThat(new ContainsFoo()))).thenReturn("foo");
        
        assertEquals("foo", mock.oneArg("foo"));
        assertEquals(null, mock.oneArg("bar"));
    }

// org.mockitousage.matchers.CustomMatchersTest::shouldCustomMatcherPrintDescriptionBasedOnName
    public void shouldCustomMatcherPrintDescriptionBasedOnName() {
        mock.simpleMethod("foo");

        try {
            verify(mock).simpleMethod(containsTest());
            fail();
        } catch (AssertionError e) {
            assertContains("<String that contains xxx>", e.getMessage());
        }
    }

// org.mockitousage.matchers.CustomMatchersTest::shouldAnonymousCustomMatcherPrintDefaultDescription
    public void shouldAnonymousCustomMatcherPrintDefaultDescription() {
        mock.simpleMethod("foo");

        try {
            verify(mock).simpleMethod((String) argThat(new ArgumentMatcher<Object>() {
                @Override public boolean matches(Object argument) {
                    return false;
                }}));
            fail();
        } catch (AssertionError e) {
            assertContains("<custom argument matcher>", e.getMessage());
            assertContains("foo", e.getMessage());
        }
    }

// org.mockitousage.matchers.GenericMatchersTest::shouldCompile
    public void shouldCompile() {
        when(sorter.convertDate(new Date())).thenReturn("one");
        when(sorter.convertDate((Date) anyObject())).thenReturn("two");

        
        when(sorter.sort(anyList())).thenReturn(null);
    }

// org.mockitousage.matchers.HamcrestMatchersTest::shouldAcceptHamcrestMatcher
    public void shouldAcceptHamcrestMatcher() {
        when(mock.simpleMethod(argThat(new ContainsX()))).thenReturn("X");
        assertNull(mock.simpleMethod("blah"));
        assertEquals("X", mock.simpleMethod("blah X blah"));
    }

// org.mockitousage.matchers.HamcrestMatchersTest::shouldVerifyUsingHamcrestMatcher
    public void shouldVerifyUsingHamcrestMatcher() {
        mock.simpleMethod("blah");
        
        try {
            verify(mock).simpleMethod(argThat(new ContainsX()));
            fail();
        } catch (ArgumentsAreDifferent e) {
            assertContains("contains 'X'", e.getMessage());
        }
    }

// org.mockitousage.matchers.InvalidUseOfMatchersTest::should_detect_wrong_number_of_matchers_when_stubbing
    public void should_detect_wrong_number_of_matchers_when_stubbing() {
        when(mock.threeArgumentMethod(1, "2", "3")).thenReturn(null);
        try {
            when(mock.threeArgumentMethod(1, eq("2"), "3")).thenReturn(null);
            fail();
        } catch (InvalidUseOfMatchersException e) {
            assertThat(e.getMessage())
                      .contains("3 matchers expected")
                      .contains("1 recorded");
        }
    }

// org.mockitousage.matchers.InvalidUseOfMatchersTest::should_detect_stupid_use_of_matchers_when_verifying
    public void should_detect_stupid_use_of_matchers_when_verifying() {
        mock.oneArg(true);
        eq("that's the stupid way");
        eq("of using matchers");
        try {
            Mockito.verify(mock).oneArg(true);
            fail();
        } catch (InvalidUseOfMatchersException e) {
            assertThat(e.getMessage())
                      .contains("Misplaced argument matcher detected here");
            e.printStackTrace();
        }
    }

// org.mockitousage.matchers.InvalidUseOfMatchersTest::should_not_scream_on_correct_usage
    public void should_not_scream_on_correct_usage() throws Exception {
        mock.simpleMethod(AdditionalMatchers.not(eq("asd")));
        mock.simpleMethod(AdditionalMatchers.or(eq("jkl"), eq("asd")));
    }

// org.mockitousage.matchers.InvalidUseOfMatchersTest::should_scream_when_no_matchers_inside_not
    public void should_scream_when_no_matchers_inside_not() {
        try {
            mock.simpleMethod(AdditionalMatchers.not("jkl"));
            fail();
        } catch (InvalidUseOfMatchersException e) {
            assertThat(e.getMessage())
                    .contains("No matchers found for")
                    .containsIgnoringCase("Not(?)");
        }
    }

// org.mockitousage.matchers.InvalidUseOfMatchersTest::should_scream_when_not_enough_matchers_inside_or_AddtionalMatcher
    public void should_scream_when_not_enough_matchers_inside_or_AddtionalMatcher() {
        try {
            mock.simpleMethod(AdditionalMatchers.or(eq("jkl"), "asd"));
            fail();
        } catch (InvalidUseOfMatchersException e) {
            assertThat(e.getMessage())
                    .containsIgnoringCase("inside additional matcher Or(?)")
                    .contains("2 sub matchers expected")
                    .contains("1 recorded");
        }
    }

// org.mockitousage.matchers.InvalidUseOfMatchersTest::should_scream_when_Matchers_count_dont_match_parameter_count
    public void should_scream_when_Matchers_count_dont_match_parameter_count() {
        try {
            mock.threeArgumentMethod(1, "asd", eq("asd"));
            fail();
        } catch (InvalidUseOfMatchersException e) {
            assertThat(e.getMessage())
                      .contains("3 matchers expected")
                      .contains("1 recorded");
        }
    }

// org.mockitousage.matchers.MatchersTest::andOverloaded
    public void andOverloaded() {
        when(mock.oneArg(and(eq(false), eq(false)))).thenReturn("0");
        when(mock.oneArg(and(eq((byte) 1), eq((byte) 1)))).thenReturn("1");
        when(mock.oneArg(and(eq('a'), eq('a')))).thenReturn("2");
        when(mock.oneArg(and(eq((double) 1), eq((double) 1)))).thenReturn("3");
        when(mock.oneArg(and(eq((float) 1), eq((float) 1)))).thenReturn("4");
        when(mock.oneArg(and(eq((int) 1), eq((int) 1)))).thenReturn("5");
        when(mock.oneArg(and(eq((long) 1), eq((long) 1)))).thenReturn("6");
        when(mock.oneArg(and(eq((short) 1), eq((short) 1)))).thenReturn("7");
        when(mock.oneArg(and(Matchers.contains("a"), Matchers.contains("d")))).thenReturn("8");
        when(mock.oneArg(and(isA(Class.class), eq(Object.class)))).thenReturn("9");

        assertEquals("0", mock.oneArg(false));
        assertEquals(null, mock.oneArg(true));

        assertEquals("1", mock.oneArg((byte) 1));
        assertEquals("2", mock.oneArg('a'));
        assertEquals("3", mock.oneArg((double) 1));
        assertEquals("4", mock.oneArg((float) 1));
        assertEquals("5", mock.oneArg((int) 1));
        assertEquals("6", mock.oneArg((long) 1));
        assertEquals("7", mock.oneArg((short) 1));

        assertEquals("8", mock.oneArg("abcde"));
        assertEquals(null, mock.oneArg("aaaaa"));

        assertEquals("9", mock.oneArg(Object.class));
    }

// org.mockitousage.matchers.MatchersTest::orOverloaded
    public void orOverloaded() {
        when(mock.oneArg(or(eq(false), eq(true)))).thenReturn("0");
        when(mock.oneArg(or(eq((byte) 1), eq((byte) 2)))).thenReturn("1");
        when(mock.oneArg(or(eq((char) 1), eq((char) 2)))).thenReturn("2");
        when(mock.oneArg(or(eq((double) 1), eq((double) 2)))).thenReturn("3");
        when(mock.oneArg(or(eq((float) 1), eq((float) 2)))).thenReturn("4");
        when(mock.oneArg(or(eq((int) 1), eq((int) 2)))).thenReturn("5");
        when(mock.oneArg(or(eq((long) 1), eq((long) 2)))).thenReturn("6");
        when(mock.oneArg(or(eq((short) 1), eq((short) 2)))).thenReturn("7");
        when(mock.oneArg(or(eq("asd"), eq("jkl")))).thenReturn("8");
        when(mock.oneArg(or(eq(this.getClass()), eq(Object.class)))).thenReturn("9");

        assertEquals("0", mock.oneArg(true));
        assertEquals("0", mock.oneArg(false));

        assertEquals("1", mock.oneArg((byte) 2));
        assertEquals("2", mock.oneArg((char) 1));
        assertEquals("3", mock.oneArg((double) 2));
        assertEquals("4", mock.oneArg((float) 1));
        assertEquals("5", mock.oneArg((int) 2));
        assertEquals("6", mock.oneArg((long) 1));
        assertEquals("7", mock.oneArg((short) 1));

        assertEquals("8", mock.oneArg("jkl"));
        assertEquals("8", mock.oneArg("asd"));
        assertEquals(null, mock.oneArg("asdjkl"));

        assertEquals("9", mock.oneArg(Object.class));
        assertEquals(null, mock.oneArg(String.class));
    }

// org.mockitousage.matchers.MatchersTest::notOverloaded
    public void notOverloaded() {
        when(mock.oneArg(not(eq(false)))).thenReturn("0");
        when(mock.oneArg(not(eq((byte) 1)))).thenReturn("1");
        when(mock.oneArg(not(eq('a')))).thenReturn("2");
        when(mock.oneArg(not(eq((double) 1)))).thenReturn("3");
        when(mock.oneArg(not(eq((float) 1)))).thenReturn("4");
        when(mock.oneArg(not(eq((int) 1)))).thenReturn("5");
        when(mock.oneArg(not(eq((long) 1)))).thenReturn("6");
        when(mock.oneArg(not(eq((short) 1)))).thenReturn("7");
        when(mock.oneArg(not(Matchers.contains("a")))).thenReturn("8");
        when(mock.oneArg(not(isA(Class.class)))).thenReturn("9");

        assertEquals("0", mock.oneArg(true));
        assertEquals(null, mock.oneArg(false));

        assertEquals("1", mock.oneArg((byte) 2));
        assertEquals("2", mock.oneArg('b'));
        assertEquals("3", mock.oneArg((double) 2));
        assertEquals("4", mock.oneArg((float) 2));
        assertEquals("5", mock.oneArg((int) 2));
        assertEquals("6", mock.oneArg((long) 2));
        assertEquals("7", mock.oneArg((short) 2));
        assertEquals("8", mock.oneArg("bcde"));

        assertEquals("9", mock.oneArg(new Object()));
        assertEquals(null, mock.oneArg(Class.class));
    }

// org.mockitousage.matchers.MatchersTest::lessOrEqualOverloaded
    public void lessOrEqualOverloaded() {
        when(mock.oneArg(leq((byte) 1))).thenReturn("1");
        when(mock.oneArg(leq((double) 1))).thenReturn("3");
        when(mock.oneArg(leq((float) 1))).thenReturn("4");
        when(mock.oneArg(leq((int) 1))).thenReturn("5");
        when(mock.oneArg(leq((long) 1))).thenReturn("6");
        when(mock.oneArg(leq((short) 1))).thenReturn("7");
        when(mock.oneArg(leq(new BigDecimal("1")))).thenReturn("8");

        assertEquals("1", mock.oneArg((byte) 1));
        assertEquals(null, mock.oneArg((byte) 2));

        assertEquals("3", mock.oneArg((double) 1));
        assertEquals("7", mock.oneArg((short) 0));
        assertEquals("4", mock.oneArg((float) -5));
        assertEquals("5", mock.oneArg((int) -2));
        assertEquals("6", mock.oneArg((long) -3));

        assertEquals("8", mock.oneArg(new BigDecimal("0.5")));
        assertEquals(null, mock.oneArg(new BigDecimal("1.1")));
    }

// org.mockitousage.matchers.MatchersTest::lessThanOverloaded
    public void lessThanOverloaded() {
        when(mock.oneArg(lt((byte) 1))).thenReturn("1");
        when(mock.oneArg(lt((double) 1))).thenReturn("3");
        when(mock.oneArg(lt((float) 1))).thenReturn("4");
        when(mock.oneArg(lt((int) 1))).thenReturn("5");
        when(mock.oneArg(lt((long) 1))).thenReturn("6");
        when(mock.oneArg(lt((short) 1))).thenReturn("7");
        when(mock.oneArg(lt(new BigDecimal("1")))).thenReturn("8");

        assertEquals("1", mock.oneArg((byte) 0));
        assertEquals(null, mock.oneArg((byte) 1));

        assertEquals("3", mock.oneArg((double) 0));
        assertEquals("7", mock.oneArg((short) 0));
        assertEquals("4", mock.oneArg((float) -4));
        assertEquals("5", mock.oneArg((int) -34));
        assertEquals("6", mock.oneArg((long) -6));

        assertEquals("8", mock.oneArg(new BigDecimal("0.5")));
        assertEquals(null, mock.oneArg(new BigDecimal("23")));
    }

// org.mockitousage.matchers.MatchersTest::greaterOrEqualMatcherOverloaded
    public void greaterOrEqualMatcherOverloaded() {
        when(mock.oneArg(geq((byte) 1))).thenReturn("1");
        when(mock.oneArg(geq((double) 1))).thenReturn("3");
        when(mock.oneArg(geq((float) 1))).thenReturn("4");
        when(mock.oneArg(geq((int) 1))).thenReturn("5");
        when(mock.oneArg(geq((long) 1))).thenReturn("6");
        when(mock.oneArg(geq((short) 1))).thenReturn("7");
        when(mock.oneArg(geq(new BigDecimal("1")))).thenReturn("8");

        assertEquals("1", mock.oneArg((byte) 2));
        assertEquals(null, mock.oneArg((byte) 0));

        assertEquals("3", mock.oneArg((double) 1));
        assertEquals("7", mock.oneArg((short) 2));
        assertEquals("4", mock.oneArg((float) 3));
        assertEquals("5", mock.oneArg((int) 4));
        assertEquals("6", mock.oneArg((long) 5));

        assertEquals("8", mock.oneArg(new BigDecimal("1.00")));
        assertEquals(null, mock.oneArg(new BigDecimal("0.9")));
    }

// org.mockitousage.matchers.MatchersTest::greaterThanMatcherOverloaded
    public void greaterThanMatcherOverloaded() {
        when(mock.oneArg(gt((byte) 1))).thenReturn("1");
        when(mock.oneArg(gt((double) 1))).thenReturn("3");
        when(mock.oneArg(gt((float) 1))).thenReturn("4");
        when(mock.oneArg(gt((int) 1))).thenReturn("5");
        when(mock.oneArg(gt((long) 1))).thenReturn("6");
        when(mock.oneArg(gt((short) 1))).thenReturn("7");
        when(mock.oneArg(gt(new BigDecimal("1")))).thenReturn("8");

        assertEquals("1", mock.oneArg((byte) 2));
        assertEquals(null, mock.oneArg((byte) 1));

        assertEquals("3", mock.oneArg((double) 2));
        assertEquals("7", mock.oneArg((short) 2));
        assertEquals("4", mock.oneArg((float) 3));
        assertEquals("5", mock.oneArg((int) 2));
        assertEquals("6", mock.oneArg((long) 5));

        assertEquals("8", mock.oneArg(new BigDecimal("1.5")));
        assertEquals(null, mock.oneArg(new BigDecimal("0.9")));
    }

// org.mockitousage.matchers.MatchersTest::compareToMatcher
    public void compareToMatcher() {
        when(mock.oneArg(cmpEq(new BigDecimal("1.5")))).thenReturn("0");

        assertEquals("0", mock.oneArg(new BigDecimal("1.50")));
        assertEquals(null, mock.oneArg(new BigDecimal("1.51")));
    }

// org.mockitousage.matchers.MatchersTest::anyStringMatcher
    public void anyStringMatcher() {
        when(mock.oneArg(anyString())).thenReturn("matched");
        
        assertEquals("matched", mock.oneArg(""));
        assertEquals("matched", mock.oneArg("any string"));
        assertEquals(null, mock.oneArg((String) null));
    }

// org.mockitousage.matchers.MatchersTest::anyMatcher
    public void anyMatcher() {
        when(mock.forObject(any())).thenReturn("matched");

        assertEquals("matched", mock.forObject(123));
        assertEquals("matched", mock.forObject("any string"));
        assertEquals("matched", mock.forObject("any string"));
        assertEquals("matched", mock.forObject((Object) null));
    }

// org.mockitousage.matchers.MatchersTest::anyXMatcher
    public void anyXMatcher() {
        when(mock.oneArg(anyBoolean())).thenReturn("0");
        when(mock.oneArg(anyByte())).thenReturn("1");
        when(mock.oneArg(anyChar())).thenReturn("2");
        when(mock.oneArg(anyDouble())).thenReturn("3");
        when(mock.oneArg(anyFloat())).thenReturn("4");
        when(mock.oneArg(anyInt())).thenReturn("5");
        when(mock.oneArg(anyLong())).thenReturn("6");
        when(mock.oneArg(anyShort())).thenReturn("7");
        when(mock.oneArg((String) anyObject())).thenReturn("8");
        when(mock.oneArg(anyObject())).thenReturn("9");
        
        assertEquals("0", mock.oneArg(true));
        assertEquals("0", mock.oneArg(false));

        assertEquals("1", mock.oneArg((byte) 1));
        assertEquals("2", mock.oneArg((char) 1));
        assertEquals("3", mock.oneArg((double) 1));
        assertEquals("4", mock.oneArg((float) 889));
        assertEquals("5", mock.oneArg((int) 1));
        assertEquals("6", mock.oneArg((long) 1));
        assertEquals("7", mock.oneArg((short) 1));
        assertEquals("8", mock.oneArg("Test"));

        assertEquals("9", mock.oneArg(new Object()));
        assertEquals("9", mock.oneArg(new HashMap()));
    }

// org.mockitousage.matchers.MatchersTest::shouldArrayEqualsDealWithNullArray
    public void shouldArrayEqualsDealWithNullArray() throws Exception {
        Object[] nullArray = null;
        when(mock.oneArray(aryEq(nullArray))).thenReturn("null");

        assertEquals("null", mock.oneArray(nullArray));

        mock = mock(IMethods.class);

        try {
            verify(mock).oneArray(aryEq(nullArray));
            fail();
        } catch (WantedButNotInvoked e) {
            assertContains("oneArray(null)", e.getMessage());
        }
    }

// org.mockitousage.matchers.MatchersTest::shouldUseSmartEqualsForArrays
    public void shouldUseSmartEqualsForArrays() throws Exception {
        
        mock.arrayMethod(new String[] {"one"});
        verify(mock).arrayMethod(eq(new String[] {"one"}));
        verify(mock).arrayMethod(new String[] {"one"});
    }

// org.mockitousage.matchers.MatchersTest::shouldUseSmartEqualsForPrimitiveArrays
    public void shouldUseSmartEqualsForPrimitiveArrays() throws Exception {
        
        mock.objectArgMethod(new int[] {1, 2});
        verify(mock).objectArgMethod(eq(new int[] {1, 2}));
        verify(mock).objectArgMethod(new int[] {1, 2});
    }

// org.mockitousage.matchers.MatchersTest::arrayEqualsShouldThrowArgumentsAreDifferentExceptionForNonMatchingArguments
    public void arrayEqualsShouldThrowArgumentsAreDifferentExceptionForNonMatchingArguments() {        
        List list = Mockito.mock(List.class);
        
        list.add("test"); 
        list.contains(new Object[] {"1"});
        
        Mockito.verify(list).contains(new Object[] {"1", "2", "3"});    
    }

// org.mockitousage.matchers.MatchersTest::arrayEqualsMatcher
    public void arrayEqualsMatcher() {
        when(mock.oneArray(aryEq(new boolean[] { true, false, false }))).thenReturn("0");
        when(mock.oneArray(aryEq(new byte[] { 1 }))).thenReturn("1");
        when(mock.oneArray(aryEq(new char[] { 1 }))).thenReturn("2");
        when(mock.oneArray(aryEq(new double[] { 1 }))).thenReturn("3");
        when(mock.oneArray(aryEq(new float[] { 1 }))).thenReturn("4");
        when(mock.oneArray(aryEq(new int[] { 1 }))).thenReturn("5");
        when(mock.oneArray(aryEq(new long[] { 1 }))).thenReturn("6");
        when(mock.oneArray(aryEq(new short[] { 1 }))).thenReturn("7");
        when(mock.oneArray(aryEq(new String[] { "Test" }))).thenReturn("8");
        when(mock.oneArray(aryEq(new Object[] { "Test", new Integer(4) }))).thenReturn("9");

        assertEquals("0", mock.oneArray(new boolean[] { true, false, false }));
        assertEquals("1", mock.oneArray(new byte[] { 1 }));
        assertEquals("2", mock.oneArray(new char[] { 1 }));
        assertEquals("3", mock.oneArray(new double[] { 1 }));
        assertEquals("4", mock.oneArray(new float[] { 1 }));
        assertEquals("5", mock.oneArray(new int[] { 1 }));
        assertEquals("6", mock.oneArray(new long[] { 1 }));
        assertEquals("7", mock.oneArray(new short[] { 1 }));
        assertEquals("8", mock.oneArray(new String[] { "Test" }));
        assertEquals("9", mock.oneArray(new Object[] { "Test", new Integer(4) }));

        assertEquals(null, mock.oneArray(new Object[] { "Test", new Integer(999) }));
        assertEquals(null, mock.oneArray(new Object[] { "Test", new Integer(4), "x" }));

        assertEquals(null, mock.oneArray(new boolean[] { true, false }));
        assertEquals(null, mock.oneArray(new boolean[] { true, true, false }));
    }

// org.mockitousage.matchers.MatchersTest::greaterOrEqualMatcher
    public void greaterOrEqualMatcher() {
        when(mock.oneArg(geq(7))).thenReturn(">= 7");
        when(mock.oneArg(lt(7))).thenReturn("< 7");

        assertEquals(">= 7", mock.oneArg(7));
        assertEquals(">= 7", mock.oneArg(8));
        assertEquals(">= 7", mock.oneArg(9));

        assertEquals("< 7", mock.oneArg(6));
        assertEquals("< 7", mock.oneArg(6));
    }

// org.mockitousage.matchers.MatchersTest::greaterThanMatcher
    public void greaterThanMatcher() {
        when(mock.oneArg(gt(7))).thenReturn("> 7");
        when(mock.oneArg(leq(7))).thenReturn("<= 7");

        assertEquals("> 7", mock.oneArg(8));
        assertEquals("> 7", mock.oneArg(9));
        assertEquals("> 7", mock.oneArg(10));

        assertEquals("<= 7", mock.oneArg(7));
        assertEquals("<= 7", mock.oneArg(6));
    }

// org.mockitousage.matchers.MatchersTest::lessOrEqualMatcher
    public void lessOrEqualMatcher() {
        when(mock.oneArg(leq(7))).thenReturn("<= 7");
        when(mock.oneArg(gt(7))).thenReturn("> 7");

        assertEquals("<= 7", mock.oneArg(7));
        assertEquals("<= 7", mock.oneArg(6));
        assertEquals("<= 7", mock.oneArg(5));

        assertEquals("> 7", mock.oneArg(8));
        assertEquals("> 7", mock.oneArg(9));
    }

// org.mockitousage.matchers.MatchersTest::lessThanMatcher
    public void lessThanMatcher() {
        when(mock.oneArg(lt(7))).thenReturn("< 7");
        when(mock.oneArg(geq(7))).thenReturn(">= 7");

        assertEquals("< 7", mock.oneArg(5));
        assertEquals("< 7", mock.oneArg(6));
        assertEquals("< 7", mock.oneArg(4));

        assertEquals(">= 7", mock.oneArg(7));
        assertEquals(">= 7", mock.oneArg(8));
    }

// org.mockitousage.matchers.MatchersTest::orMatcher
    public void orMatcher() {
        when(mock.oneArg(anyInt())).thenReturn("other");
        when(mock.oneArg(or(eq(7), eq(9)))).thenReturn("7 or 9");

        assertEquals("other", mock.oneArg(10));
        assertEquals("7 or 9", mock.oneArg(7));
        assertEquals("7 or 9", mock.oneArg(9));
    }

// org.mockitousage.matchers.MatchersTest::nullMatcher
    public void nullMatcher() {
        when(mock.threeArgumentMethod(eq(1), isNull(), eq(""))).thenReturn("1");
        when(mock.threeArgumentMethod(eq(1), not(isNull()), eq(""))).thenReturn("2");

        assertEquals("1", mock.threeArgumentMethod(1, null, ""));
        assertEquals("2", mock.threeArgumentMethod(1, new Object(), ""));
    }

// org.mockitousage.matchers.MatchersTest::notNullMatcher
    public void notNullMatcher() {
        when(mock.threeArgumentMethod(eq(1), notNull(), eq(""))).thenReturn("1");
        when(mock.threeArgumentMethod(eq(1), not(isNotNull()), eq(""))).thenReturn("2");

        assertEquals("1", mock.threeArgumentMethod(1, new Object(), ""));
        assertEquals("2", mock.threeArgumentMethod(1, null, ""));
    }

// org.mockitousage.matchers.MatchersTest::findMatcher
    public void findMatcher() {
        when(mock.oneArg(find("([a-z]+)\\d"))).thenReturn("1");

        assertEquals("1", mock.oneArg("ab12"));
        assertEquals(null, mock.oneArg("12345"));
        assertEquals(null, mock.oneArg((Object) null));
    }

// org.mockitousage.matchers.MatchersTest::matchesMatcher
    public void matchesMatcher() {
        when(mock.oneArg(matches("[a-z]+\\d\\d"))).thenReturn("1");
        when(mock.oneArg(matches("\\d\\d\\d"))).thenReturn("2");

        assertEquals("1", mock.oneArg("a12"));
        assertEquals("2", mock.oneArg("131"));
        assertEquals(null, mock.oneArg("blah"));
    }

// org.mockitousage.matchers.MatchersTest::containsMatcher
    public void containsMatcher() {
        when(mock.oneArg(Matchers.contains("ell"))).thenReturn("1");
        when(mock.oneArg(Matchers.contains("ld"))).thenReturn("2");

        assertEquals("1", mock.oneArg("hello"));
        assertEquals("2", mock.oneArg("world"));
        assertEquals(null, mock.oneArg("xlx"));
    }

// org.mockitousage.matchers.MatchersTest::startsWithMatcher
    public void startsWithMatcher() {
        when(mock.oneArg(startsWith("ab"))).thenReturn("1");
        when(mock.oneArg(startsWith("bc"))).thenReturn("2");

        assertEquals("1", mock.oneArg("ab quake"));
        assertEquals("2", mock.oneArg("bc quake"));
        assertEquals(null, mock.oneArg("ba quake"));
    }

// org.mockitousage.matchers.MatchersTest::endsWithMatcher
    public void endsWithMatcher() {
        when(mock.oneArg(Matchers.endsWith("ab"))).thenReturn("1");
        when(mock.oneArg(Matchers.endsWith("bc"))).thenReturn("2");

        assertEquals("1", mock.oneArg("xab"));
        assertEquals("2", mock.oneArg("xbc"));
        assertEquals(null, mock.oneArg("ac"));
    }

// org.mockitousage.matchers.MatchersTest::deltaMatcher
    public void deltaMatcher() {
        when(mock.oneArg(eq(1.0D, 0.1D))).thenReturn("1");
        when(mock.oneArg(eq(2.0D, 0.1D))).thenReturn("2");
        when(mock.oneArg(eq(1.0F, 0.1F))).thenReturn("3");
        when(mock.oneArg(eq(2.0F, 0.1F))).thenReturn("4");
        when(mock.oneArg(eq(2.0F, 0.1F))).thenReturn("4");

        assertEquals("1", mock.oneArg(1.0));
        assertEquals("1", mock.oneArg(0.91));
        assertEquals("1", mock.oneArg(1.09));
        assertEquals("2", mock.oneArg(2.0));

        assertEquals("3", mock.oneArg(1.0F));
        assertEquals("3", mock.oneArg(0.91F));
        assertEquals("3", mock.oneArg(1.09F));
        assertEquals("4", mock.oneArg(2.1F));

        assertEquals(null, mock.oneArg(2.2F));
    }

// org.mockitousage.matchers.MatchersTest::deltaMatcherPrintsItself
    public void deltaMatcherPrintsItself() {
        try {
            verify(mock).oneArg(eq(1.0D, 0.1D));
            fail();
        } catch (WantedButNotInvoked e) {
            assertContains("eq(1.0, 0.1)", e.getMessage());
        }
    }

// org.mockitousage.matchers.MatchersTest::sameMatcher
    public void sameMatcher() {
        Object one = new String("1243");
        Object two = new String("1243");
        Object three = new String("1243");

        assertNotSame(one, two);
        assertEquals(one, two);
        assertEquals(two, three);

        when(mock.oneArg(same(one))).thenReturn("1");
        when(mock.oneArg(same(two))).thenReturn("2");

        assertEquals("1", mock.oneArg(one));
        assertEquals("2", mock.oneArg(two));
        assertEquals(null, mock.oneArg(three));
    }

// org.mockitousage.matchers.MatchersTest::eqMatcherAndNulls
    public void eqMatcherAndNulls() {
        mock.simpleMethod((Object) null);

        verify(mock).simpleMethod((Object) eq(null));
    }

// org.mockitousage.matchers.MatchersTest::sameMatcherAndNulls
    public void sameMatcherAndNulls() {
        mock.simpleMethod((Object) null);

        verify(mock).simpleMethod(same(null));
    }

// org.mockitousage.matchers.MoreMatchersTest::shouldHelpOutWithUnnecessaryCasting
    public void shouldHelpOutWithUnnecessaryCasting() {
        when(mock.objectArgMethod(any(String.class))).thenReturn("string");

        assertEquals("string", mock.objectArgMethod("foo"));
    }

// org.mockitousage.matchers.MoreMatchersTest::shouldAnyBeActualAliasToAnyObject
    public void shouldAnyBeActualAliasToAnyObject() {
        mock.simpleMethod((Object) null);

        verify(mock).simpleMethod(anyObject());
        verify(mock).simpleMethod(any(Object.class));
    }

// org.mockitousage.matchers.MoreMatchersTest::shouldHelpOutWithUnnecessaryCastingOfLists
    public void shouldHelpOutWithUnnecessaryCastingOfLists() {
        
        
        when(mock.listArgMethod(anyListOf(String.class))).thenReturn("list");

        assertEquals("list", mock.listArgMethod(new LinkedList<String>()));
        assertEquals("list", mock.listArgMethod(Collections.<String>emptyList()));
    }

// org.mockitousage.matchers.MoreMatchersTest::shouldHelpOutWithUnnecessaryCastingOfSets
    public void shouldHelpOutWithUnnecessaryCastingOfSets() {
        
        
        when(mock.setArgMethod(anySetOf(String.class))).thenReturn("set");

        assertEquals("set", mock.setArgMethod(new HashSet<String>()));
        assertEquals("set", mock.setArgMethod(Collections.<String>emptySet()));
    }

// org.mockitousage.matchers.MoreMatchersTest::shouldHelpOutWithUnnecessaryCastingOfMaps
    public void shouldHelpOutWithUnnecessaryCastingOfMaps() {
        
        
        when(mock.forMap(anyMapOf(String.class, String.class))).thenReturn("map");

        assertEquals("map", mock.forMap(new HashMap<String, String>()));
        assertEquals("map", mock.forMap(Collections.<String, String>emptyMap()));
    }

// org.mockitousage.matchers.MoreMatchersTest::shouldHelpOutWithUnnecessaryCastingOfCollections
    public void shouldHelpOutWithUnnecessaryCastingOfCollections() {
        
        
        when(mock.collectionArgMethod(anyCollectionOf(String.class))).thenReturn("col");

        assertEquals("col", mock.collectionArgMethod(new ArrayList<String>()));
        assertEquals("col", mock.collectionArgMethod(Collections.<String>emptyList()));
    }

// org.mockitousage.matchers.MoreMatchersTest::shouldHelpOutWithUnnecessaryCastingOfNullityChecks
    public void shouldHelpOutWithUnnecessaryCastingOfNullityChecks() {
        when(mock.objectArgMethod(isNull(LinkedList.class))).thenReturn("string");
        when(mock.objectArgMethod(notNull(LinkedList.class))).thenReturn("string");
        when(mock.objectArgMethod(isNotNull(LinkedList.class))).thenReturn("string");

        assertEquals("string", mock.objectArgMethod(null));
        assertEquals("string", mock.objectArgMethod("foo"));
        assertEquals("string", mock.objectArgMethod("foo"));
    }

// org.mockitousage.matchers.NewMatchersTest::shouldAllowAnyList
    public void shouldAllowAnyList() {
        when(mock.forList(anyList())).thenReturn("matched");
        
        assertEquals("matched", mock.forList(Arrays.asList("x", "y")));
        assertEquals(null, mock.forList(null));

        verify(mock, times(1)).forList(anyList());
    }

// org.mockitousage.matchers.NewMatchersTest::shouldAllowAnyCollection
    public void shouldAllowAnyCollection() {
        when(mock.forCollection(anyCollection())).thenReturn("matched");
        
        assertEquals("matched", mock.forCollection(Arrays.asList("x", "y")));
        assertEquals(null, mock.forCollection(null));

        verify(mock, times(1)).forCollection(anyCollection());
    }

// org.mockitousage.matchers.NewMatchersTest::shouldAllowAnyMap
    public void shouldAllowAnyMap() {
        when(mock.forMap(anyMap())).thenReturn("matched");
        
        assertEquals("matched", mock.forMap(new HashMap<String, String>()));
        assertEquals(null, mock.forMap(null));

        verify(mock, times(1)).forMap(anyMap());
    }

// org.mockitousage.matchers.NewMatchersTest::shouldAllowAnySet
    public void shouldAllowAnySet() {
        when(mock.forSet(anySet())).thenReturn("matched");
        
        assertEquals("matched", mock.forSet(new HashSet<String>()));
        assertEquals(null, mock.forSet(null));

        verify(mock, times(1)).forSet(anySet());
    }

// org.mockitousage.matchers.ReflectionMatchersTest::shouldMatchWhenFieldValuesEqual
    public void shouldMatchWhenFieldValuesEqual() throws Exception {
        Child wanted = new Child(1, "foo", 2, "bar");
        verify(mock).run(refEq(wanted));
    }

// org.mockitousage.matchers.ReflectionMatchersTest::shouldNotMatchWhenFieldValuesDiffer
    public void shouldNotMatchWhenFieldValuesDiffer() throws Exception {
        Child wanted = new Child(1, "foo", 2, "bar XXX");
        verify(mock).run(refEq(wanted));
    }

// org.mockitousage.matchers.ReflectionMatchersTest::shouldNotMatchAgain
    public void shouldNotMatchAgain() throws Exception {
        Child wanted = new Child(1, "foo", 999, "bar");
        verify(mock).run(refEq(wanted));
    }

// org.mockitousage.matchers.ReflectionMatchersTest::shouldNotMatchYetAgain
    public void shouldNotMatchYetAgain() throws Exception {
        Child wanted = new Child(1, "XXXXX", 2, "bar");
        verify(mock).run(refEq(wanted));
    }

// org.mockitousage.matchers.ReflectionMatchersTest::shouldNotMatch
    public void shouldNotMatch() throws Exception {
        Child wanted = new Child(234234, "foo", 2, "bar");
        verify(mock).run(refEq(wanted));
    }

// org.mockitousage.matchers.ReflectionMatchersTest::shouldMatchWhenFieldValuesEqualWithOneFieldExcluded
    public void shouldMatchWhenFieldValuesEqualWithOneFieldExcluded() throws Exception {
        Child wanted = new Child(1, "foo", 2, "excluded");
        verify(mock).run(refEq(wanted, "childFieldTwo"));
    }

// org.mockitousage.matchers.ReflectionMatchersTest::shouldMatchWhenFieldValuesEqualWithTwoFieldsExcluded
    public void shouldMatchWhenFieldValuesEqualWithTwoFieldsExcluded() throws Exception {
        Child wanted = new Child(234234, "foo", 2, "excluded");
        verify(mock).run(refEq(wanted, "childFieldTwo", "parentField"));
        verify(mock).run(refEq(wanted, "parentField", "childFieldTwo"));
    }

// org.mockitousage.matchers.ReflectionMatchersTest::shouldNotMatchWithFieldsExclusion
    public void shouldNotMatchWithFieldsExclusion() throws Exception {
        Child wanted = new Child(234234, "foo", 2, "excluded");
        verify(mock).run(refEq(wanted, "childFieldTwo"));
    }

// org.mockitousage.matchers.VerificationAndStubbingUsingMatchersTest::shouldStubUsingMatchers
    public void shouldStubUsingMatchers() {
        when(one.simpleMethod(2)).thenReturn("2");
        when(two.simpleMethod(anyString())).thenReturn("any");
        when(three.simpleMethod(startsWith("test"))).thenThrow(new RuntimeException());

        assertEquals(null, one.simpleMethod(1));
        assertEquals("2", one.simpleMethod(2));
        
        assertEquals("any", two.simpleMethod("two"));
        assertEquals("any", two.simpleMethod("two again"));
        
        assertEquals(null, three.simpleMethod("three"));
        assertEquals(null, three.simpleMethod("three again"));
       
        try {
            three.simpleMethod("test three again");
            fail();
        } catch (RuntimeException e) {}
    }

// org.mockitousage.matchers.VerificationAndStubbingUsingMatchersTest::shouldVerifyUsingMatchers
    public void shouldVerifyUsingMatchers() {
        stubVoid(one).toThrow(new RuntimeException()).on().oneArg(true);
        when(three.varargsObject(5, "first arg", "second arg")).thenReturn("stubbed");

        try {
            one.oneArg(true);
            fail();
        } catch (RuntimeException e) {}

        one.simpleMethod(100);
        two.simpleMethod("test Mockito");
        three.varargsObject(10, "first arg", "second arg");
        
        assertEquals("stubbed", three.varargsObject(5, "first arg", "second arg"));

        verify(one).oneArg(eq(true));
        verify(one).simpleMethod(anyInt());
        verify(two).simpleMethod(startsWith("test"));
        verify(three).varargsObject(5, "first arg", "second arg");
        verify(three).varargsObject(eq(10), eq("first arg"), startsWith("second"));
        
        verifyNoMoreInteractions(one, two, three);
        
        try {
            verify(three).varargsObject(eq(10), eq("first arg"), startsWith("third"));
            fail();
        } catch (WantedButNotInvoked e) {}
    }

// org.mockitousage.misuse.CleaningUpPotentialStubbingTest::shouldResetOngoingStubbingOnVerify
    public void shouldResetOngoingStubbingOnVerify() {
        
        mock.booleanReturningMethod();
        verify(mock).booleanReturningMethod();
        
        
        assertOngoingStubbingIsReset();
    }

// org.mockitousage.misuse.CleaningUpPotentialStubbingTest::shouldResetOngoingStubbingOnInOrder
    public void shouldResetOngoingStubbingOnInOrder() {
        mock.booleanReturningMethod();
        InOrder inOrder = inOrder(mock);
        inOrder.verify(mock).booleanReturningMethod();
        assertOngoingStubbingIsReset();
    }

// org.mockitousage.misuse.CleaningUpPotentialStubbingTest::shouldResetOngoingStubbingOnDoReturn
    public void shouldResetOngoingStubbingOnDoReturn() {
        mock.booleanReturningMethod();
        doReturn(false).when(mock).booleanReturningMethod();
        assertOngoingStubbingIsReset();
    }

// org.mockitousage.misuse.DescriptiveMessagesOnMisuseTest::tryDescriptiveMessagesOnMisuse
    public void tryDescriptiveMessagesOnMisuse() {
        Foo foo = mock(Foo.class);
        

        

        

    }

// org.mockitousage.misuse.DescriptiveMessagesOnMisuseTest::shouldScreamWhenWholeMethodPassedToVerify
    public void shouldScreamWhenWholeMethodPassedToVerify() {
        verify(mock.booleanReturningMethod());
    }

// org.mockitousage.misuse.DescriptiveMessagesOnMisuseTest::shouldScreamWhenWholeMethodPassedToVerifyNoMoreInteractions
    public void shouldScreamWhenWholeMethodPassedToVerifyNoMoreInteractions() {
        verifyNoMoreInteractions(mock.byteReturningMethod());
    }

// org.mockitousage.misuse.DescriptiveMessagesOnMisuseTest::shouldScreamWhenInOrderCreatedWithDodgyMock
    public void shouldScreamWhenInOrderCreatedWithDodgyMock() {
        inOrder("not a mock");
    }

// org.mockitousage.misuse.DescriptiveMessagesOnMisuseTest::shouldScreamWhenInOrderCreatedWithNulls
    public void shouldScreamWhenInOrderCreatedWithNulls() {
        inOrder(mock, null);
    }

// org.mockitousage.misuse.DescriptiveMessagesOnMisuseTest::shouldScreamNullPassedToVerify
    public void shouldScreamNullPassedToVerify() {
        verify(null);
    }

// org.mockitousage.misuse.DescriptiveMessagesOnMisuseTest::shouldScreamWhenNotMockPassedToVerifyNoMoreInteractions
    public void shouldScreamWhenNotMockPassedToVerifyNoMoreInteractions() {
        verifyNoMoreInteractions(null, "blah");
    }

// org.mockitousage.misuse.DescriptiveMessagesOnMisuseTest::shouldScreamWhenNullPassedToVerifyNoMoreInteractions
    public void shouldScreamWhenNullPassedToVerifyNoMoreInteractions() {
        verifyNoMoreInteractions((Object[])null);
    }

// org.mockitousage.misuse.ExplicitFrameworkValidationTest::shouldValidateExplicitly
    public void shouldValidateExplicitly() {
        verify(mock);
        try {
            Mockito.validateMockitoUsage();
            fail();
        } catch (UnfinishedVerificationException e) {}
    }

// org.mockitousage.misuse.ExplicitFrameworkValidationTest::shouldDetectUnfinishedStubbing
    public void shouldDetectUnfinishedStubbing() {
        when(mock.simpleMethod());
        try {
            Mockito.validateMockitoUsage();
            fail();
        } catch (UnfinishedStubbingException e) {}
    }

// org.mockitousage.misuse.ExplicitFrameworkValidationTest::shouldDetectMisplacedArgumentMatcher
    public void shouldDetectMisplacedArgumentMatcher() {
        anyObject();
        try {
            Mockito.validateMockitoUsage();
            fail();
        } catch (InvalidUseOfMatchersException e) {}
    }

// org.mockitousage.misuse.InvalidUsageTest::shouldRequireArgumentsWhenVerifyingNoMoreInteractions
    public void shouldRequireArgumentsWhenVerifyingNoMoreInteractions() {
        verifyNoMoreInteractions();
    }

// org.mockitousage.misuse.InvalidUsageTest::shouldRequireArgumentsWhenVerifyingZeroInteractions
    public void shouldRequireArgumentsWhenVerifyingZeroInteractions() {
        verifyZeroInteractions();
    }

// org.mockitousage.misuse.InvalidUsageTest::shouldNotCreateInOrderObjectWithoutMocks
    public void shouldNotCreateInOrderObjectWithoutMocks() {
        inOrder();
    }

// org.mockitousage.misuse.InvalidUsageTest::shouldNotAllowVerifyingInOrderUnfamilarMocks
    public void shouldNotAllowVerifyingInOrderUnfamilarMocks() {
        InOrder inOrder = inOrder(mock);
        inOrder.verify(mockTwo).simpleMethod();
    }

// org.mockitousage.misuse.InvalidUsageTest::shouldReportMissingMethodInvocationWhenStubbing
    public void shouldReportMissingMethodInvocationWhenStubbing() {
        when(mock.simpleMethod()).thenReturn("this stubbing is required to make sure Stubbable is pulled");
        when("".toString()).thenReturn("x");
    }

// org.mockitousage.misuse.InvalidUsageTest::shouldNotAllowSettingInvalidCheckedException
    public void shouldNotAllowSettingInvalidCheckedException() throws Exception {
        when(mock.simpleMethod()).thenThrow(new Exception());
    }

// org.mockitousage.misuse.InvalidUsageTest::shouldNotAllowSettingNullThrowable
    public void shouldNotAllowSettingNullThrowable() throws Exception {
        when(mock.simpleMethod()).thenThrow(new Throwable[] {null});
    }

// org.mockitousage.misuse.InvalidUsageTest::shouldNotAllowSettingNullThrowableVararg
    public void shouldNotAllowSettingNullThrowableVararg() throws Exception {
        when(mock.simpleMethod()).thenThrow((Throwable) null);
    }

// org.mockitousage.misuse.InvalidUsageTest::shouldNotAllowSettingNullConsecutiveThrowable
    public void shouldNotAllowSettingNullConsecutiveThrowable() throws Exception {
        when(mock.simpleMethod()).thenThrow(new RuntimeException(), null);
    }

// org.mockitousage.misuse.InvalidUsageTest::shouldNotAllowMockingFinalClasses
    public void shouldNotAllowMockingFinalClasses() throws Exception {
        mock(FinalClass.class); 
    }

// org.mockitousage.misuse.InvalidUsageTest::shouldNotAllowMockingPrimitves
    public void shouldNotAllowMockingPrimitves() throws Exception {
        mock(Integer.TYPE); 
    }

// org.mockitousage.misuse.InvalidUsageTest::shouldNotMockObjectMethodsOnInterface
    public void shouldNotMockObjectMethodsOnInterface() throws Exception {
        ObjectLikeInterface inter = mock(ObjectLikeInterface.class);
        
        inter.equals(null);
        inter.toString();
        inter.hashCode();
        
        verifyZeroInteractions(inter);
    }

// org.mockitousage.misuse.RestrictedObjectMethodsTest::shouldScreamWhenVerifyToString
    public void shouldScreamWhenVerifyToString() {
        try {
            verify(mock).toString();
            fail();
        } catch (MockitoException e) {
            assertContains("cannot verify", e.getMessage());
        }
    }

// org.mockitousage.misuse.RestrictedObjectMethodsTest::shouldBeSilentWhenVerifyHashCode
    public void shouldBeSilentWhenVerifyHashCode() {
        
        
        
        verify(mock).hashCode();
    }

// org.mockitousage.misuse.RestrictedObjectMethodsTest::shouldBeSilentWhenVerifyEquals
    public void shouldBeSilentWhenVerifyEquals() {
        
        
        
        verify(mock).equals(null);
    }

// org.mockitousage.misuse.RestrictedObjectMethodsTest::shouldBeSilentWhenVerifyEqualsInOrder
    public void shouldBeSilentWhenVerifyEqualsInOrder() {
        
        
        
        InOrder inOrder = inOrder(mock);
        inOrder.verify(mock).equals(null);
    }

// org.mockitousage.misuse.SpyStubbingMisuseTest::nestedWhenTest
    public void nestedWhenTest() {
        Strategy mfoo = mock(Strategy.class);
        Sampler mpoo = mock(Sampler.class);
        Producer out = spy(new Producer(mfoo));

        try {
            when(out.produce()).thenReturn(mpoo);
            fail();
        } catch (WrongTypeOfReturnValue e) {
            assertThat(e.getMessage()).contains("spy").contains("syntax").contains("doReturn|Throw");
        }
    }

// org.mockitousage.performance.StubOnlyAvoidMemoryConsumptionTest::using_stub_only_wont_thrown_an_OutOfMemoryError
    public void using_stub_only_wont_thrown_an_OutOfMemoryError() {
        Object obj = mock(Object.class, withSettings().stubOnly());
        when(obj.toString()).thenReturn("asdf");

        for (int i = 0; i < 1000000; i++) {
            obj.toString();
        }
    }

// org.mockitousage.performance.StubOnlyAvoidMemoryConsumptionTest::without_stub_only_mocks_will_store_invocations_leading_to_an_OutOfMemoryError
    public void without_stub_only_mocks_will_store_invocations_leading_to_an_OutOfMemoryError() {
        Object obj = mock(Object.class, withSettings());
        when(obj.toString()).thenReturn("asdf");

        for (int i = 0; i < 1000000; i++) {
            obj.toString();
        }
    }

// org.mockitousage.puzzlers.BridgeMethodPuzzleTest::shouldHaveBridgeMethod
    public void shouldHaveBridgeMethod() throws Exception {
        Super s = new Sub();
        
        assertEquals("Dummy says: Hello", s.say("Hello"));
        
        assertThat(Sub.class, hasBridgeMethod("say"));
        assertThat(s, hasBridgeMethod("say"));
    }

// org.mockitousage.puzzlers.BridgeMethodPuzzleTest::shouldVerifyCorrectlyWhenBridgeMethodCalled
    public void shouldVerifyCorrectlyWhenBridgeMethodCalled() throws Exception {
        
        
        Sub s = mock(Sub.class);
        setMockWithDownCast(s);
        say("Hello");
        
        verify(s).say("Hello");
    }

// org.mockitousage.puzzlers.OverloadingPuzzleTest::shouldUseArgumentTypeWhenOverloadingPuzzleDetected
    public void shouldUseArgumentTypeWhenOverloadingPuzzleDetected() throws Exception {
        Sub sub = mock(Sub.class);
        setMockWithDowncast(sub);
        say("Hello");
        try {
            verify(sub).say("Hello");
            fail();
        } catch (WantedButNotInvoked e) {}
    }

// org.mockitousage.serialization.AcrossClassLoaderSerializationTest::check_that_mock_can_be_serialized_in_a_classloader_and_deserialized_in_another
    public void check_that_mock_can_be_serialized_in_a_classloader_and_deserialized_in_another() throws Exception {
        byte[] bytes = create_mock_and_serialize_it_in_class_loader_A();

        Object the_deserialized_mock = read_stream_and_deserialize_it_in_class_loader_B(bytes);
    }

// org.mockitousage.serialization.DeepStubsSerializableTest::should_serialize_and_deserialize_mock_created_with_deep_stubs
    public void should_serialize_and_deserialize_mock_created_with_deep_stubs() throws Exception {
        
        SampleClass sampleClass = mock(SampleClass.class, withSettings().defaultAnswer(RETURNS_DEEP_STUBS).serializable());
        when(sampleClass.getSample().isFalse()).thenReturn(true);
        when(sampleClass.getSample().number()).thenReturn(999);

        
        SampleClass deserializedSample = serializeAndBack(sampleClass);

        
        assertThat(deserializedSample.getSample().isFalse()).isEqualTo(true);
        assertThat(deserializedSample.getSample().number()).isEqualTo(999);
    }

// org.mockitousage.serialization.DeepStubsSerializableTest::should_serialize_and_deserialize_parameterized_class_mocked_with_deep_stubs
    public void should_serialize_and_deserialize_parameterized_class_mocked_with_deep_stubs() throws Exception {
        
        ListContainer deep_stubbed = mock(ListContainer.class, withSettings().defaultAnswer(RETURNS_DEEP_STUBS).serializable());
        when(deep_stubbed.iterator().next().add("yes")).thenReturn(true);

        
        ListContainer deserialized_deep_stub = serializeAndBack(deep_stubbed);
        
        
        assertThat(deserialized_deep_stub.iterator().next().add("not stubbed but mock already previously resolved")).isEqualTo(false);
        assertThat(deserialized_deep_stub.iterator().next().add("yes")).isEqualTo(true);
    }

// org.mockitousage.serialization.DeepStubsSerializableTest::should_discard_generics_metadata_when_serialized_then_disabling_deep_stubs_with_generics
    public void should_discard_generics_metadata_when_serialized_then_disabling_deep_stubs_with_generics() throws Exception {
        
        ListContainer deep_stubbed = mock(ListContainer.class, withSettings().defaultAnswer(RETURNS_DEEP_STUBS).serializable());
        when(deep_stubbed.iterator().hasNext()).thenReturn(true);

        ListContainer deserialized_deep_stub = serializeAndBack(deep_stubbed);

        
        when(deserialized_deep_stub.iterator().next().get(42)).thenReturn("no");

        
    }

// org.mockitousage.serialization.ParallelSerializationTest::single_mock_being_serialized_in_different_classloaders_by_multiple_threads
    public void single_mock_being_serialized_in_different_classloaders_by_multiple_threads() throws ExecutionException, InterruptedException {
        
        int iterations = 2;
        int threadingFactor = 200;
        final ExecutorService executorService = Executors.newFixedThreadPool(threadingFactor);
        final IMethods iMethods_that_store_invocations = mock(IMethods.class, withSettings().serializable());

        
        for (int i = 0; i <= iterations; i++) {
            List<Future> futures = new ArrayList<Future>(threadingFactor);
            final CyclicBarrier barrier_that_will_wait_until_threads_are_ready = new CyclicBarrier(threadingFactor);

            
            
            
            for (int j = 0; j < threadingFactor; j++) {
                
                futures.add(executorService.submit(new Callable<Object>() {
                    public Object call() throws Exception {
                        barrier_that_will_wait_until_threads_are_ready.await();

                        randomCallOn(iMethods_that_store_invocations);

                        return SimpleSerializationUtil.serializeMock(iMethods_that_store_invocations).toByteArray();
                    }
                }));

                
                executorService.submit(new Callable<Object>() {
                    public Object call() throws Exception {
                        barrier_that_will_wait_until_threads_are_ready.await();
                        return iMethods_that_store_invocations.longObjectReturningMethod();
                    }
                });
            }

            
            for (Future future : futures) {
                future.get();
            }
        }
    }

// org.mockitousage.spies.PartialMockingWithSpiesTest::shouldCallRealMethdsEvenDelegatedToOtherSelfMethod
    public void shouldCallRealMethdsEvenDelegatedToOtherSelfMethod() {
        
        String name = spy.getName();

        
        assertEquals("Default name", name);
    }

// org.mockitousage.spies.PartialMockingWithSpiesTest::shouldAllowStubbingOfMethodsThatDelegateToOtherMethods
    public void shouldAllowStubbingOfMethodsThatDelegateToOtherMethods() {
        
        when(spy.getName()).thenReturn("foo");
        
        
        assertEquals("foo", spy.getName());
    }

// org.mockitousage.spies.PartialMockingWithSpiesTest::shouldAllowStubbingWithThrowablesMethodsThatDelegateToOtherMethods
    public void shouldAllowStubbingWithThrowablesMethodsThatDelegateToOtherMethods() {
        
        doThrow(new RuntimeException("appetite for destruction"))
            .when(spy).getNameButDelegateToMethodThatThrows();
        
        
        try {
            spy.getNameButDelegateToMethodThatThrows();
            fail();
        } catch(Exception e) {
            assertEquals("appetite for destruction", e.getMessage());
        }
    }

// org.mockitousage.spies.PartialMockingWithSpiesTest::shouldStackTraceGetFilteredOnUserExceptions
    public void shouldStackTraceGetFilteredOnUserExceptions() {
        try {
            
            spy.getNameButDelegateToMethodThatThrows();
            fail();
        } catch (Throwable t) {
            
            assertThat(t, ExtraMatchers.hasMethodsInStackTrace(
                    "throwSomeException",
                    "getNameButDelegateToMethodThatThrows",
                    "shouldStackTraceGetFilteredOnUserExceptions"
                    ));
        }
    }

// org.mockitousage.spies.PartialMockingWithSpiesTest::shouldVerify
    public void shouldVerify() {
        
        spy.getName();

        
        verify(spy).guessName();
    }

// org.mockitousage.spies.PartialMockingWithSpiesTest::shouldStub
    public void shouldStub() {
        
        when(spy.guessName()).thenReturn(new Name("John"));
        
        String name = spy.getName();
        
        assertEquals("John", name);
    }

// org.mockitousage.spies.PartialMockingWithSpiesTest::shouldDealWithPrivateFieldsOfSubclasses
    public void shouldDealWithPrivateFieldsOfSubclasses() {
        assertEquals("100$", spy.howMuchDidYouInherit());
    }

// org.mockitousage.spies.SpyingOnInterfacesTest::shouldFailFastWhenCallingRealMethodOnInterface
    public void shouldFailFastWhenCallingRealMethodOnInterface() throws Exception {
        List list = mock(List.class);
        try {
            
            when(list.get(0)).thenCallRealMethod();
            
            fail();
        } catch (MockitoException e) {}
    }

// org.mockitousage.spies.SpyingOnInterfacesTest::shouldFailInRuntimeWhenCallingRealMethodOnInterface
    public void shouldFailInRuntimeWhenCallingRealMethodOnInterface() throws Exception {
        
        List list = mock(List.class);
        when(list.get(0)).thenAnswer(
            new Answer() {
                public Object answer(InvocationOnMock invocation) throws Throwable {
                    return invocation.callRealMethod();
                }
            }
        );
        try {
            
            list.get(0);            
            
            fail();
        } catch (MockitoException e) {}
    }

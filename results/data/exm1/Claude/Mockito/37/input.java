// buggy code
    public void validate(Answer<?> answer, Invocation invocation) {
        if (answer instanceof ThrowsException) {
            validateException((ThrowsException) answer, invocation);
        }
        
        if (answer instanceof Returns) {
            validateReturnValue((Returns) answer, invocation);
        }
        
        if (answer instanceof DoesNothing) {
            validateDoNothing((DoesNothing) answer, invocation);
        }
        
    }

// relevant test
// org.concurrentmockito.ThreadsShareGenerouslyStubbedMockTest::shouldAllowVerifyingInThreads
    public void shouldAllowVerifyingInThreads() throws Exception {
        for(int i = 0; i < 50; i++) {
            performTest();
        }
    }

// org.mockito.MockitoTest::shouldRemoveStubbableFromProgressAfterStubbing
    public void shouldRemoveStubbableFromProgressAfterStubbing() {
        List mock = Mockito.mock(List.class);
        Mockito.when(mock.add("test")).thenReturn(true);
        
        assertNull(new ThreadSafeMockingProgress().pullOngoingStubbing());
    }

// org.mockito.MockitoTest::shouldValidateMockWhenVerifying
    public void shouldValidateMockWhenVerifying() {
        Mockito.verify("notMock");
    }

// org.mockito.MockitoTest::shouldValidateMockWhenVerifyingWithExpectedNumberOfInvocations
    public void shouldValidateMockWhenVerifyingWithExpectedNumberOfInvocations() {
        Mockito.verify("notMock", times(19));
    }

// org.mockito.MockitoTest::shouldValidateMockWhenVerifyingNoMoreInteractions
    public void shouldValidateMockWhenVerifyingNoMoreInteractions() {
        Mockito.verifyNoMoreInteractions("notMock");
    }

// org.mockito.MockitoTest::shouldValidateMockWhenVerifyingZeroInteractions
    public void shouldValidateMockWhenVerifyingZeroInteractions() {
        Mockito.verifyZeroInteractions("notMock");
    }

// org.mockito.MockitoTest::shouldValidateMockWhenStubbingVoid
    public void shouldValidateMockWhenStubbingVoid() {
        Mockito.stubVoid("notMock");
    }

// org.mockito.MockitoTest::shouldValidateMockWhenCreatingInOrderObject
    public void shouldValidateMockWhenCreatingInOrderObject() {
        Mockito.inOrder("notMock");
    }

// org.mockito.MockitoTest::shouldStartingMockSettingsContainDefaultBehavior
    public void shouldStartingMockSettingsContainDefaultBehavior() {
        
        MockSettingsImpl settings = (MockSettingsImpl) Mockito.withSettings();
        
        
        assertEquals(Mockito.RETURNS_DEFAULTS, settings.getDefaultAnswer());
    }

// org.mockito.internal.InvalidStateDetectionTest::shouldDetectUnfinishedStubbing
    public void shouldDetectUnfinishedStubbing() {
        when(mock.simpleMethod());
        detectsAndCleansUp(new OnMethodCallOnMock(), UnfinishedStubbingException.class);

        when(mock.simpleMethod());
        detectsAndCleansUp(new OnStub(), UnfinishedStubbingException.class);
        
        when(mock.simpleMethod());
        detectsAndCleansUp(new OnStubVoid(), UnfinishedStubbingException.class);
        
        when(mock.simpleMethod());
        detectsAndCleansUp(new OnVerify(), UnfinishedStubbingException.class);
        
        when(mock.simpleMethod());
        detectsAndCleansUp(new OnVerifyInOrder(), UnfinishedStubbingException.class);
        
        when(mock.simpleMethod());
        detectsAndCleansUp(new OnVerifyZeroInteractions(), UnfinishedStubbingException.class);
        
        when(mock.simpleMethod());
        detectsAndCleansUp(new OnVerifyNoMoreInteractions(), UnfinishedStubbingException.class);

        when(mock.simpleMethod());
        detectsAndCleansUp(new OnDoAnswer(), UnfinishedStubbingException.class);
        
        when(mock.simpleMethod());
        detectsAndCleansUp(new OnMockCreation(), UnfinishedStubbingException.class);
        
        when(mock.simpleMethod());
        detectsAndCleansUp(new OnSpyCreation(), UnfinishedStubbingException.class);
    }

// org.mockito.internal.InvalidStateDetectionTest::shouldDetectUnfinishedStubbingVoid
    public void shouldDetectUnfinishedStubbingVoid() {
        stubVoid(mock);
        detectsAndCleansUp(new OnMethodCallOnMock(), UnfinishedStubbingException.class);
        
        stubVoid(mock);
        detectsAndCleansUp(new OnStub(), UnfinishedStubbingException.class);
        
        stubVoid(mock);
        detectsAndCleansUp(new OnStubVoid(), UnfinishedStubbingException.class);
        
        stubVoid(mock);
        detectsAndCleansUp(new OnVerify(), UnfinishedStubbingException.class);
        
        stubVoid(mock);
        detectsAndCleansUp(new OnVerifyInOrder(), UnfinishedStubbingException.class);
        
        stubVoid(mock);
        detectsAndCleansUp(new OnVerifyZeroInteractions(), UnfinishedStubbingException.class);
        
        stubVoid(mock);
        detectsAndCleansUp(new OnVerifyNoMoreInteractions(), UnfinishedStubbingException.class);
        
        stubVoid(mock);
        detectsAndCleansUp(new OnDoAnswer(), UnfinishedStubbingException.class);
    }

// org.mockito.internal.InvalidStateDetectionTest::shouldDetectUnfinishedDoAnswerStubbing
    public void shouldDetectUnfinishedDoAnswerStubbing() {
        doAnswer(null);
        detectsAndCleansUp(new OnMethodCallOnMock(), UnfinishedStubbingException.class);
        
        doAnswer(null);
        detectsAndCleansUp(new OnStub(), UnfinishedStubbingException.class);
        
        doAnswer(null);
        detectsAndCleansUp(new OnStubVoid(), UnfinishedStubbingException.class);
        
        doAnswer(null);
        detectsAndCleansUp(new OnVerify(), UnfinishedStubbingException.class);
        
        doAnswer(null);
        detectsAndCleansUp(new OnVerifyInOrder(), UnfinishedStubbingException.class);
        
        doAnswer(null);
        detectsAndCleansUp(new OnVerifyZeroInteractions(), UnfinishedStubbingException.class);
        
        doAnswer(null);
        detectsAndCleansUp(new OnVerifyNoMoreInteractions(), UnfinishedStubbingException.class);
        
        doAnswer(null);
        detectsAndCleansUp(new OnDoAnswer(), UnfinishedStubbingException.class);
    }

// org.mockito.internal.InvalidStateDetectionTest::shouldDetectUnfinishedVerification
    public void shouldDetectUnfinishedVerification() {
        verify(mock);
        detectsAndCleansUp(new OnStub(), UnfinishedVerificationException.class);
        
        verify(mock);
        detectsAndCleansUp(new OnStubVoid(), UnfinishedVerificationException.class);
        
        verify(mock);
        detectsAndCleansUp(new OnVerify(), UnfinishedVerificationException.class);
        
        verify(mock);
        detectsAndCleansUp(new OnVerifyInOrder(), UnfinishedVerificationException.class);
        
        verify(mock);
        detectsAndCleansUp(new OnVerifyZeroInteractions(), UnfinishedVerificationException.class);
        
        verify(mock);
        detectsAndCleansUp(new OnVerifyNoMoreInteractions(), UnfinishedVerificationException.class);
        
        verify(mock);
        detectsAndCleansUp(new OnDoAnswer(), UnfinishedVerificationException.class);
    }

// org.mockito.internal.InvalidStateDetectionTest::shouldDetectMisplacedArgumentMatcher
    public void shouldDetectMisplacedArgumentMatcher() {
        anyObject();
        detectsAndCleansUp(new OnStubVoid(), InvalidUseOfMatchersException.class);
        
        anyObject();
        detectsAndCleansUp(new OnVerify(), InvalidUseOfMatchersException.class);
        
        anyObject();
        detectsAndCleansUp(new OnVerifyInOrder(), InvalidUseOfMatchersException.class);
        
        anyObject();
        detectsAndCleansUp(new OnVerifyZeroInteractions(), InvalidUseOfMatchersException.class);
        
        anyObject();
        detectsAndCleansUp(new OnVerifyNoMoreInteractions(), InvalidUseOfMatchersException.class);
        
        anyObject();
        detectsAndCleansUp(new OnDoAnswer(), InvalidUseOfMatchersException.class);
    }

// org.mockito.internal.InvalidStateDetectionTest::shouldCorrectStateAfterDetectingUnfinishedStubbing
    public void shouldCorrectStateAfterDetectingUnfinishedStubbing() {
        stubVoid(mock).toThrow(new RuntimeException());
        
        try {
            stubVoid(mock).toThrow(new RuntimeException()).on().oneArg(true);
            fail();
        } catch (UnfinishedStubbingException e) {}
        
        stubVoid(mock).toThrow(new RuntimeException()).on().oneArg(true);
        try {
            mock.oneArg(true);
            fail();
        } catch (RuntimeException e) {}
    }

// org.mockito.internal.InvalidStateDetectionTest::shouldCorrectStateAfterDetectingUnfinishedVerification
    public void shouldCorrectStateAfterDetectingUnfinishedVerification() {
        mock.simpleMethod();
        verify(mock);
        
        try {
            verify(mock).simpleMethod();
            fail();
        } catch (UnfinishedVerificationException e) {}
        
        verify(mock).simpleMethod();
    }

// org.mockito.internal.creation.cglib.CGLIBHackerTest::shouldSetMockitoNamingPolicy
    public void shouldSetMockitoNamingPolicy() throws Exception {
        
        MockitoMethodProxy methodProxy = new MethodProxyBuilder().build();
        
        
        new CGLIBHacker().setMockitoNamingPolicy(methodProxy);
        
        
        Object realMethodProxy = Whitebox.invokeMethod(methodProxy, "getMethodProxy", new Object[0]);
        Object createInfo = Whitebox.getInternalState(realMethodProxy, "createInfo");
        NamingPolicy namingPolicy = (NamingPolicy) Whitebox.getInternalState(createInfo, "namingPolicy");
        assertEquals(MockitoNamingPolicy.INSTANCE, namingPolicy);
    }

// org.mockito.internal.creation.cglib.CGLIBHackerTest::shouldSetMockitoNamingPolicyEvenIfMethodProxyIsProxied
    public void shouldSetMockitoNamingPolicyEvenIfMethodProxyIsProxied() throws Exception {
        
        MockitoMethodProxy proxiedMethodProxy = spy(new MethodProxyBuilder().build());
        
        
        new CGLIBHacker().setMockitoNamingPolicy(proxiedMethodProxy);
        
        
        Object realMethodProxy = Whitebox.invokeMethod(proxiedMethodProxy, "getMethodProxy", new Object[0]);
        Object createInfo = Whitebox.getInternalState(realMethodProxy, "createInfo");
        NamingPolicy namingPolicy = (NamingPolicy) Whitebox.getInternalState(createInfo, "namingPolicy");
        assertEquals(MockitoNamingPolicy.INSTANCE, namingPolicy);
    }

// org.mockito.internal.runners.CollectingDebugDataTest::shouldNotCollectWhenNoJUnitRunner
    public void shouldNotCollectWhenNoJUnitRunner() throws Throwable {
        
        when(mock.simpleMethod()).thenReturn("foo");
        
        
        mock.differentMethod();
        
        MockingProgress progress = new ThreadSafeMockingProgress();
        
        assertFalse(progress.getDebuggingInfo().hasData());
    }

// org.mockito.internal.stubbing.MockitoStubberTest::shouldFinishStubbingWhenWrongThrowableIsSet
    public void shouldFinishStubbingWhenWrongThrowableIsSet() throws Exception {
        state.stubbingStarted();
        try {
            mockitoStubber.addAnswer(new ThrowsException(new Exception()));
            fail();
        } catch (MockitoException e) {
            state.validateState();
        }
    }

// org.mockito.internal.stubbing.MockitoStubberTest::shouldFinishStubbingOnAddingReturnValue
    public void shouldFinishStubbingOnAddingReturnValue() throws Exception {
        state.stubbingStarted();
        mockitoStubber.addAnswer(new Returns("test"));
        state.validateState();
    }

// org.mockito.internal.stubbing.MockitoStubberTest::shouldGetResultsForMethods
    public void shouldGetResultsForMethods() throws Throwable {
        mockitoStubber.setInvocationForPotentialStubbing(new InvocationMatcher(simpleMethod));
        mockitoStubber.addAnswer(new Returns("simpleMethod"));
        
        Invocation differentMethod = new InvocationBuilder().differentMethod().toInvocation();
        mockitoStubber.setInvocationForPotentialStubbing(new InvocationMatcher(differentMethod));
        mockitoStubber.addAnswer(new ThrowsException(new MyException()));
        
        assertEquals("simpleMethod", mockitoStubber.answerTo(simpleMethod));
        
        try {
            mockitoStubber.answerTo(differentMethod);
            fail();
        } catch (MyException e) {}
    }

// org.mockito.internal.stubbing.MockitoStubberTest::shouldAddThrowableForVoidMethod
    public void shouldAddThrowableForVoidMethod() throws Throwable {
        mockitoStubber.addAnswerForVoidMethod(new ThrowsException(new MyException()));
        mockitoStubber.setMethodForStubbing(new InvocationMatcher(simpleMethod));
        
        try {
            mockitoStubber.answerTo(simpleMethod);
            fail();
        } catch (MyException e) {}
    }

// org.mockito.internal.stubbing.MockitoStubberTest::shouldValidateThrowableForVoidMethod
    public void shouldValidateThrowableForVoidMethod() throws Throwable {
        mockitoStubber.addAnswerForVoidMethod(new ThrowsException(new Exception()));
        
        try {
            mockitoStubber.setMethodForStubbing(new InvocationMatcher(simpleMethod));
            fail();
        } catch (MockitoException e) {}
    }

// org.mockito.internal.stubbing.MockitoStubberTest::shouldValidateThrowable
    public void shouldValidateThrowable() throws Throwable {
        try {
            mockitoStubber.addAnswer(new ThrowsException(null));
            fail();
        } catch (MockitoException e) {}
    }

// org.mockito.internal.stubbing.answers.AnswersValidatorTest::shouldValidateNullThrowable
    public void shouldValidateNullThrowable() throws Throwable {
        try {
            validator.validate(new ThrowsException(null), null);
            fail();
        } catch (MockitoException e) {}
    }

// org.mockito.internal.stubbing.answers.AnswersValidatorTest::shouldPassProperCheckedException
    public void shouldPassProperCheckedException() throws Throwable {
        validator.validate(new ThrowsException(new CharacterCodingException()), invocation);
    }

// org.mockito.internal.stubbing.answers.AnswersValidatorTest::shouldFailInvalidCheckedException
    public void shouldFailInvalidCheckedException() throws Throwable {
        validator.validate(new ThrowsException(new IOException()), invocation);
    }

// org.mockito.internal.stubbing.answers.AnswersValidatorTest::shouldPassRuntimeExceptions
    public void shouldPassRuntimeExceptions() throws Throwable {
        validator.validate(new ThrowsException(new Error()), invocation);
        validator.validate(new ThrowsException(new RuntimeException()), invocation);
    }

// org.mockito.internal.stubbing.answers.AnswersValidatorTest::shouldFailWhenReturnValueIsSetForVoidMethod
    public void shouldFailWhenReturnValueIsSetForVoidMethod() throws Throwable {
        validator.validate(new Returns("one"), new InvocationBuilder().method("voidMethod").toInvocation());
    }

// org.mockito.internal.stubbing.answers.AnswersValidatorTest::shouldFailWhenNonVoidMethodDoesNothing
    public void shouldFailWhenNonVoidMethodDoesNothing() throws Throwable {
        validator.validate(new DoesNothing(), new InvocationBuilder().simpleMethod().toInvocation());
    }

// org.mockito.internal.stubbing.answers.AnswersValidatorTest::shouldAllowVoidReturnForVoidMethod
    public void shouldAllowVoidReturnForVoidMethod() throws Throwable {
        validator.validate(new DoesNothing(), new InvocationBuilder().method("voidMethod").toInvocation());
    }

// org.mockito.internal.stubbing.answers.AnswersValidatorTest::shouldAllowCorrectTypeOfReturnValue
    public void shouldAllowCorrectTypeOfReturnValue() throws Throwable {
        validator.validate(new Returns("one"), new InvocationBuilder().simpleMethod().toInvocation());
        validator.validate(new Returns(false), new InvocationBuilder().method("booleanReturningMethod").toInvocation());
        validator.validate(new Returns(new Boolean(true)), new InvocationBuilder().method("booleanObjectReturningMethod").toInvocation());
        validator.validate(new Returns(1), new InvocationBuilder().method("integerReturningMethod").toInvocation());
        validator.validate(new Returns(1L), new InvocationBuilder().method("longReturningMethod").toInvocation());
        validator.validate(new Returns(1L), new InvocationBuilder().method("longObjectReturningMethod").toInvocation());
        validator.validate(new Returns(null), new InvocationBuilder().method("objectReturningMethodNoArgs").toInvocation());
        validator.validate(new Returns(1), new InvocationBuilder().method("objectReturningMethodNoArgs").toInvocation());
    }

// org.mockito.internal.stubbing.answers.AnswersValidatorTest::shouldFailOnReturnTypeMismatch
    public void shouldFailOnReturnTypeMismatch() throws Throwable {
        validator.validate(new Returns("String"), new InvocationBuilder().method("booleanReturningMethod").toInvocation());
    }

// org.mockito.internal.stubbing.answers.AnswersValidatorTest::shouldFailOnWrongPrimitive
    public void shouldFailOnWrongPrimitive() throws Throwable {
        validator.validate(new Returns(1), new InvocationBuilder().method("doubleReturningMethod").toInvocation());
    }

// org.mockito.internal.stubbing.answers.AnswersValidatorTest::shouldFailOnNullWithPrimitive
    public void shouldFailOnNullWithPrimitive() throws Throwable {
        validator.validate(new Returns(null), new InvocationBuilder().method("booleanReturningMethod").toInvocation());
    }

// org.mockito.internal.stubbing.answers.AnswersValidatorTest::shouldFailWhenCallingRealMethodOnIterface
    public void shouldFailWhenCallingRealMethodOnIterface() throws Throwable {
        
        Invocation inovcationOnIterface = new InvocationBuilder().method("simpleMethod").toInvocation();
        try {
            
            validator.validate(new CallsRealMethods(), inovcationOnIterface);
            
            fail();
        } catch (MockitoException e) {}
    }

// org.mockito.internal.stubbing.answers.AnswersValidatorTest::shouldBeOKWhenCallingRealMethodOnConcreteClass
    public void shouldBeOKWhenCallingRealMethodOnConcreteClass() throws Throwable {
        
        ArrayList mock = mock(ArrayList.class);
        mock.clear();
        Invocation invocationOnClass = getLastInvocation();
        
        validator.validate(new CallsRealMethods(), invocationOnClass);
        
    }

// org.mockito.internal.util.reflection.LenientCopyToolTest::shouldShallowCopyBasicFinalField
    public void shouldShallowCopyBasicFinalField() throws Exception {
        
        assertEquals(100, from.finalField);
        assertNotEquals(100, to.finalField);

        
        tool.copyToMock(from, to);

        
        assertEquals(100, to.finalField);
    }

// org.mockito.internal.util.reflection.LenientCopyToolTest::shouldShallowCopyTransientPrivateFields
    public void shouldShallowCopyTransientPrivateFields() throws Exception {
        
        from.privateTransientField = 1000;
        assertNotEquals(1000, to.privateTransientField);

        
        tool.copyToMock(from, to);

        
        assertEquals(1000, to.privateTransientField);
    }

// org.mockito.internal.util.reflection.LenientCopyToolTest::shouldShallowCopyLinkedListIntoMock
    public void shouldShallowCopyLinkedListIntoMock() throws Exception {
        
        LinkedList fromList = new LinkedList();
        LinkedList toList = mock(LinkedList.class);

        
        tool.copyToMock(fromList, toList);

        
    }

// org.mockito.internal.util.reflection.LenientCopyToolTest::shouldShallowCopyFieldValuesIntoMock
    public void shouldShallowCopyFieldValuesIntoMock() throws Exception {
        
        from.defaultField = "foo";
        from.instancePublicField = new SomeOtherObject();
        from.privateField = 1;
        from.privateTransientField = 2;
        from.protectedField = 3;
        
        assertNotEquals(from.defaultField, to.defaultField);
        assertNotEquals(from.instancePublicField, to.instancePublicField);
        assertNotEquals(from.privateField, to.privateField);
        assertNotEquals(from.privateTransientField, to.privateTransientField);
        assertNotEquals(from.protectedField, to.protectedField);

        
        tool.copyToMock(from, to);

        
        assertEquals(from.defaultField, to.defaultField);
        assertEquals(from.instancePublicField, to.instancePublicField);
        assertEquals(from.privateField, to.privateField);
        assertEquals(from.privateTransientField, to.privateTransientField);
        assertEquals(from.protectedField, to.protectedField);
    }

// org.mockito.internal.util.reflection.LenientCopyToolTest::shouldCopyValuesOfInheritedFields
    public void shouldCopyValuesOfInheritedFields() throws Exception {
        
        ((InheritMe) from).privateInherited = "foo";
        ((InheritMe) from).protectedInherited = "bar";
    
        assertNotEquals(((InheritMe) from).privateInherited, ((InheritMe) to).privateInherited);
        assertNotEquals(((InheritMe) from).privateInherited, ((InheritMe) to).privateInherited);
        
        
        tool.copyToMock(from, to);
        
        
        assertEquals(((InheritMe) from).privateInherited, ((InheritMe) to).privateInherited);
        assertEquals(((InheritMe) from).privateInherited, ((InheritMe) to).privateInherited);
    }

// org.mockito.internal.util.reflection.LenientCopyToolTest::shouldEnableAndThenDisableAccessibility
    public void shouldEnableAndThenDisableAccessibility() throws Exception {
        
        Field privateField = SomeObject.class.getDeclaredField("privateField");
        assertFalse(privateField.isAccessible());
        
        
        tool.copyToMock(from, to);
        
        
        privateField = SomeObject.class.getDeclaredField("privateField");
        assertFalse(privateField.isAccessible());
    }

// org.mockito.internal.util.reflection.LenientCopyToolTest::shouldContinueEvenIfThereAreProblemsCopyingSingleFieldValue
    public void shouldContinueEvenIfThereAreProblemsCopyingSingleFieldValue() throws Exception {
        
        tool.fieldCopier = mock(FieldCopier.class);
        
        doNothing().
        doThrow(new IllegalAccessException()).
        doNothing().
        when(tool.fieldCopier).
        copyValue(anyObject(), anyObject(), any(Field.class));
        
        
        tool.copyToMock(from, to);
        
        
        verify(tool.fieldCopier, atLeast(3)).copyValue(any(), any(), any(Field.class));
    }

// org.mockito.runners.ConsoleSpammingMockitoJUnitRunnerTest::shouldLogUnusedStubbingWarningWhenTestFails
    public void shouldLogUnusedStubbingWarningWhenTestFails() throws Exception {
        runner = new ConsoleSpammingMockitoJUnitRunner(this.getClass(), loggerStub, new RunnerImplStub() {
            @Override
            public void run(RunNotifier notifier) {
                
                
                unusedStubbingThatQualifiesForWarning();
                
                notifier.fireTestFailure(null);
                
                String loggedInfo = loggerStub.getLoggedInfo();
                assertContains(".unusedStubbingThatQualifiesForWarning(", loggedInfo);
            }
        });
        
        runner.run(notifier);
    }

// org.mockito.runners.ConsoleSpammingMockitoJUnitRunnerTest::shouldLogUnstubbedMethodWarningWhenTestFails
    public void shouldLogUnstubbedMethodWarningWhenTestFails() throws Exception {
        runner = new ConsoleSpammingMockitoJUnitRunner(this.getClass(), loggerStub, new RunnerImplStub() {
            @Override
            public void run(RunNotifier notifier) {
                callUnstubbedMethodThatQualifiesForWarning();
                notifier.fireTestFailure(null);

                String loggedInfo = loggerStub.getLoggedInfo();
                assertContains("method was not stubbed", loggedInfo);
                assertContains("mock.simpleMethod(456);", loggedInfo);
                assertContains(".callUnstubbedMethodThatQualifiesForWarning(", loggedInfo);
            }
        });
        
        runner.run(notifier);
    }

// org.mockito.runners.ConsoleSpammingMockitoJUnitRunnerTest::shouldLogStubCalledWithDifferentArgumentsWhenTestFails
    public void shouldLogStubCalledWithDifferentArgumentsWhenTestFails() throws Exception {
        runner = new ConsoleSpammingMockitoJUnitRunner(this.getClass(), loggerStub, new RunnerImplStub() {
            @Override
            public void run(RunNotifier notifier) {
                someStubbing();
                callStubbedMethodWithDifferentArgs();
                notifier.fireTestFailure(null);
                
                String loggedInfo = loggerStub.getLoggedInfo();
                assertContains("with different arguments", loggedInfo);
                assertContains(".someStubbing(", loggedInfo);
                assertContains(".callStubbedMethodWithDifferentArgs(", loggedInfo);
            }
        });
        
        runner.run(notifier);
    }

// org.mockito.runners.ConsoleSpammingMockitoJUnitRunnerTest::shouldNotLogAnythingWhenStubCalledCorrectly
    public void shouldNotLogAnythingWhenStubCalledCorrectly() throws Exception {
        runner = new ConsoleSpammingMockitoJUnitRunner(this.getClass(), loggerStub, new RunnerImplStub() {
            @Override
            public void run(RunNotifier notifier) {
                when(mock.simpleMethod(1)).thenReturn("foo");
                mock.simpleMethod(1);

                notifier.fireTestFailure(null);
                
                assertEquals("", loggerStub.getLoggedInfo());
            }
        });
        
        runner.run(notifier);
    }

// org.mockito.runners.ConsoleSpammingMockitoJUnitRunnerTest::shouldNotLogWhenTestPasses
    public void shouldNotLogWhenTestPasses() throws Exception {
        runner = new ConsoleSpammingMockitoJUnitRunner(this.getClass(), loggerStub, new RunnerImplStub() {
            @Override
            public void run(RunNotifier notifier) {
                when(mock.simpleMethod()).thenReturn("foo");
                
                notifier.fireTestFinished(null);
                
                assertEquals("", loggerStub.getLoggedInfo());
            }
        });
        
        runner.run(notifier);
    }

// org.mockito.runners.ConsoleSpammingMockitoJUnitRunnerTest::shouldDelegateToGetDescription
    public void shouldDelegateToGetDescription() throws Exception {
        
        final Description expectedDescription = Description.createSuiteDescription(this.getClass());
        runner = new ConsoleSpammingMockitoJUnitRunner(this.getClass(), loggerStub, new RunnerImplStub() {
            public Description getDescription() {
                return expectedDescription;
            }
        });
        
        
        Description description = runner.getDescription();
        
        
        assertEquals(expectedDescription, description);
    }

// org.mockitousage.basicapi.MocksSerializationTest::shouldAllowMockToBeSerializable
    public void shouldAllowMockToBeSerializable() throws Exception {
        
        IMethods mock = mock(IMethods.class, withSettings().extraInterfaces(Serializable.class).serializable());

        
        serializeAndBack(mock);
    }

// org.mockitousage.basicapi.MocksSerializationTest::shouldAllowMockAndBooleanValueToSerializable
    public void shouldAllowMockAndBooleanValueToSerializable() throws Exception {
        
        IMethods mock = mock(IMethods.class, withSettings().extraInterfaces(Serializable.class).serializable());
        when(mock.booleanReturningMethod()).thenReturn(true);

        
        ByteArrayOutputStream serialized = serializeMock(mock);

        
        IMethods readObject = deserializeMock(serialized, IMethods.class);
        assertTrue(readObject.booleanReturningMethod());
    }

// org.mockitousage.basicapi.MocksSerializationTest::shouldAllowMockAndStringValueToBeSerializable
    public void shouldAllowMockAndStringValueToBeSerializable() throws Exception {
        
        IMethods mock = mock(IMethods.class, withSettings().extraInterfaces(Serializable.class).serializable());
        String value = "value";
        when(mock.stringReturningMethod()).thenReturn(value);

        
        ByteArrayOutputStream serialized = serializeMock(mock);

        
        IMethods readObject = deserializeMock(serialized, IMethods.class);
        assertEquals(value, readObject.stringReturningMethod());
    }

// org.mockitousage.basicapi.MocksSerializationTest::shouldAllMockAndSerializableValueToBeSerialized
    public void shouldAllMockAndSerializableValueToBeSerialized() throws Exception {
        
        IMethods mock = mock(IMethods.class, withSettings().extraInterfaces(Serializable.class).serializable());
        List<?> value = Collections.emptyList();
        when(mock.objectReturningMethodNoArgs()).thenReturn(value);

        
        ByteArrayOutputStream serialized = serializeMock(mock);

        
        IMethods readObject = deserializeMock(serialized, IMethods.class);
        assertEquals(value, readObject.objectReturningMethodNoArgs());
    }

// org.mockitousage.basicapi.MocksSerializationTest::shouldSerializeMethodCallWithParametersThatAreSerializable
    public void shouldSerializeMethodCallWithParametersThatAreSerializable() throws Exception {
        IMethods mock = mock(IMethods.class, withSettings().extraInterfaces(Serializable.class).serializable());
        List<?> value = Collections.emptyList();
        when(mock.objectArgMethod(value)).thenReturn(value);

        
        ByteArrayOutputStream serialized = serializeMock(mock);

        
        IMethods readObject = deserializeMock(serialized, IMethods.class);
        assertEquals(value, readObject.objectArgMethod(value));
    }

// org.mockitousage.basicapi.MocksSerializationTest::shouldSerializeMethodCallsUsingAnyStringMatcher
    public void shouldSerializeMethodCallsUsingAnyStringMatcher() throws Exception {
        IMethods mock = mock(IMethods.class, withSettings().extraInterfaces(Serializable.class).serializable());
        List<?> value = Collections.emptyList();
        when(mock.objectArgMethod(anyString())).thenReturn(value);

        
        ByteArrayOutputStream serialized = serializeMock(mock);

        
        IMethods readObject = deserializeMock(serialized, IMethods.class);
        assertEquals(value, readObject.objectArgMethod(""));
    }

// org.mockitousage.basicapi.MocksSerializationTest::shouldVerifyCalledNTimesForSerializedMock
    public void shouldVerifyCalledNTimesForSerializedMock() throws Exception {
        IMethods mock = mock(IMethods.class, withSettings().extraInterfaces(Serializable.class).serializable());
        List<?> value = Collections.emptyList();
        when(mock.objectArgMethod(anyString())).thenReturn(value);
        mock.objectArgMethod("");

        
        ByteArrayOutputStream serialized = serializeMock(mock);

        
        IMethods readObject = deserializeMock(serialized, IMethods.class);
        verify(readObject, times(1)).objectArgMethod("");
    }

// org.mockitousage.basicapi.MocksSerializationTest::shouldVerifyEvenIfSomeMethodsCalledAfterSerialization
    public void shouldVerifyEvenIfSomeMethodsCalledAfterSerialization() throws Exception {
        
        IMethods mock = mock(IMethods.class, withSettings().extraInterfaces(Serializable.class).serializable());

        
        mock.simpleMethod(1);
        ByteArrayOutputStream serialized = serializeMock(mock);
        IMethods readObject = deserializeMock(serialized, IMethods.class);
        readObject.simpleMethod(1);

        
        verify(readObject, times(2)).simpleMethod(1);

        
        
    }

// org.mockitousage.basicapi.MocksSerializationTest::shouldSerializationWork
    public void shouldSerializationWork() throws Exception {
        
        Foo foo = new Foo();
        
        foo = serializeAndBack(foo);
        
        assertSame(foo, foo.bar.foo);
    }

// org.mockitousage.basicapi.MocksSerializationTest::shouldStubEvenIfSomeMethodsCalledAfterSerialization
    public void shouldStubEvenIfSomeMethodsCalledAfterSerialization() throws Exception {
        
        IMethods mock = mock(IMethods.class, withSettings().extraInterfaces(Serializable.class).serializable());

        
        when(mock.simpleMethod(1)).thenReturn("foo");
        ByteArrayOutputStream serialized = serializeMock(mock);
        IMethods readObject = deserializeMock(serialized, IMethods.class);
        when(readObject.simpleMethod(2)).thenReturn("bar");

        
        assertEquals("foo", readObject.simpleMethod(1));
        assertEquals("bar", readObject.simpleMethod(2));
    }

// org.mockitousage.basicapi.MocksSerializationTest::shouldVerifyCallOrderForSerializedMock
    public void shouldVerifyCallOrderForSerializedMock() throws Exception {
        IMethods mock = mock(IMethods.class, withSettings().extraInterfaces(Serializable.class).serializable());
        IMethods mock2 = mock(IMethods.class, withSettings().extraInterfaces(Serializable.class).serializable());
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

// org.mockitousage.basicapi.MocksSerializationTest::shouldRememberInteractionsForSerializedMock
    public void shouldRememberInteractionsForSerializedMock() throws Exception {
        IMethods mock = mock(IMethods.class, withSettings().extraInterfaces(Serializable.class).serializable());
        List<?> value = Collections.emptyList();
        when(mock.objectArgMethod(anyString())).thenReturn(value);
        mock.objectArgMethod("happened");

        
        ByteArrayOutputStream serialized = serializeMock(mock);

        
        IMethods readObject = deserializeMock(serialized, IMethods.class);
        verify(readObject, never()).objectArgMethod("never happened");
    }

// org.mockitousage.basicapi.MocksSerializationTest::shouldSerializeWithStubbingCallback
    public void shouldSerializeWithStubbingCallback() throws Exception {

        
        IMethods mock = mock(IMethods.class, withSettings().extraInterfaces(Serializable.class).serializable());
        final String string = "return value";
        when(mock.objectArgMethod(anyString())).thenAnswer(new Answer<Object>() {
            public Object answer(InvocationOnMock invocation) {
                invocation.getArguments();
                invocation.getMock();
                return string;
            }
        });

        
        ByteArrayOutputStream serialized = serializeMock(mock);

        
        IMethods readObject = deserializeMock(serialized, IMethods.class);
        assertEquals(string, readObject.objectArgMethod(""));
    }

// org.mockitousage.basicapi.MocksSerializationTest::shouldSerializeWithRealObjectSpy
    public void shouldSerializeWithRealObjectSpy() throws Exception {
        
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

// org.mockitousage.basicapi.MocksSerializationTest::shouldSerializeObjectMock
    public void shouldSerializeObjectMock() {}

// org.mockitousage.basicapi.MocksSerializationTest::shouldSerializeRealPartialMock
    public void shouldSerializeRealPartialMock() {}

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
        when(mixedVarargs.doSomething("hello", null)).thenReturn("hello");
        when(mixedVarargs.doSomething("goodbye", null)).thenReturn("goodbye");

        String result = mixedVarargs.doSomething("hello", null);
        assertEquals("hello", result);
        
        verify(mixedVarargs).doSomething("hello", null);
    }

// org.mockitousage.basicapi.UsingVarargsTest::shouldStubCorrectlyWhenDoubleStringAndMixedVarargsUsed
    public void shouldStubCorrectlyWhenDoubleStringAndMixedVarargsUsed() {
        MixedVarargs mixedVarargs = mock(MixedVarargs.class);
        when(mixedVarargs.doSomething("one", "two", null)).thenReturn("hello");
        when(mixedVarargs.doSomething("1", "2", null)).thenReturn("goodbye");

        String result = mixedVarargs.doSomething("one", "two", null);
        assertEquals("hello", result);
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

// org.mockitousage.bugs.NPEWhenMockingThrowablesTest::shouldNotThrowNPE
    public void shouldNotThrowNPE() {
        when(mock.simpleMethod()).thenThrow(mock2);
        try {
            mock.simpleMethod();
            fail();
        } catch(DummyException e) {}
    }

// org.mockitousage.bugs.VarargsErrorWhenCallingRealMethodTest::shouldNotThrowAnyException
    public void shouldNotThrowAnyException() throws Exception {
        Foo foo = mock(Foo.class);

        when(foo.blah(anyString(), anyString())).thenCallRealMethod();

        assertEquals(1, foo.blah("foo", "bar"));
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

// org.mockitousage.customization.BDDMockitoTest::shouldStubWithAnswer
    public void shouldStubWithAnswer() throws Exception {
        given(mock.simpleMethod(anyString())).willAnswer(new Answer<String>() {
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

// org.mockitousage.customization.BDDMockitoTest::shouldStubVoid
    public void shouldStubVoid() throws Exception {
        willThrow(new RuntimeException()).given(mock).voidMethod();
        
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

// org.mockitousage.matchers.AnyXMatchersAcceptNullsTest::shouldAnyXMatchersAcceptNull
    public void shouldAnyXMatchersAcceptNull() {
        when(mock.oneArg(anyObject())).thenReturn("0");
        when(mock.oneArg(anyString())).thenReturn("1");
        when(mock.forList(anyList())).thenReturn("2");
        when(mock.forMap(anyMap())).thenReturn("3");
        when(mock.forCollection(anyCollection())).thenReturn("4");
        when(mock.forSet(anySet())).thenReturn("5");
        
        assertEquals("0", mock.oneArg((Object) null));
        assertEquals("1", mock.oneArg((String) null));
        assertEquals("2", mock.forList(null));
        assertEquals("3", mock.forMap(null));
        assertEquals("4", mock.forCollection(null));
        assertEquals("5", mock.forSet(null));
    }

// org.mockitousage.matchers.AnyXMatchersAcceptNullsTest::shouldAnyPrimiteWraperMatchersAcceptNull
    public void shouldAnyPrimiteWraperMatchersAcceptNull() {
        when(mock.forInteger(anyInt())).thenReturn("0");
        when(mock.forCharacter(anyChar())).thenReturn("1");
        when(mock.forShort(anyShort())).thenReturn("2");
        when(mock.forByte(anyByte())).thenReturn("3");
        when(mock.forBoolean(anyBoolean())).thenReturn("4");
        when(mock.forLong(anyLong())).thenReturn("5");
        when(mock.forFloat(anyFloat())).thenReturn("6");
        when(mock.forDouble(anyDouble())).thenReturn("7");
        
        assertEquals("0", mock.forInteger(null));
        assertEquals("1", mock.forCharacter(null));
        assertEquals("2", mock.forShort(null));
        assertEquals("3", mock.forByte(null));
        assertEquals("4", mock.forBoolean(null));
        assertEquals("5", mock.forLong(null));
        assertEquals("6", mock.forFloat(null));
        assertEquals("7", mock.forDouble(null));
    }

// org.mockitousage.matchers.CapturingArgumentsTest::shouldAllowAssertionsOnCapturedArgument
    public void shouldAllowAssertionsOnCapturedArgument() {
        
        emailer.email(12);
        
        
        ArgumentCaptor<Person> argument = new ArgumentCaptor<Person>();
        verify(emailService).sendEmailTo(argument.capture());
        
        assertEquals(12, argument.getValue().getAge());
    }

// org.mockitousage.matchers.CapturingArgumentsTest::shouldAllowAssertionsOnAllCapturedArguments
    public void shouldAllowAssertionsOnAllCapturedArguments() {
        
        emailer.email(11, 12);
        
        
        ArgumentCaptor<Person> argument = ArgumentCaptor.forClass(Person.class);
        verify(emailService, atLeastOnce()).sendEmailTo(argument.capture());
        List<Person> allValues = argument.getAllValues();
        
        assertEquals(11, allValues.get(0).getAge());
        assertEquals(12, allValues.get(1).getAge());
    }

// org.mockitousage.matchers.CapturingArgumentsTest::shouldAllowAssertionsOnLastArgument
    public void shouldAllowAssertionsOnLastArgument() {
        
        emailer.email(11, 12, 13);
        
        
        ArgumentCaptor<Person> argument = ArgumentCaptor.forClass(Person.class);
        verify(emailService, atLeastOnce()).sendEmailTo(argument.capture());
        
        assertEquals(13, argument.getValue().getAge());
    }

// org.mockitousage.matchers.CapturingArgumentsTest::shouldPrintCaptorMatcher
    public void shouldPrintCaptorMatcher() {
        
        ArgumentCaptor<Person> person = ArgumentCaptor.forClass(Person.class);
        
        try {
            
            verify(emailService).sendEmailTo(person.capture());
            fail();
        } catch(WantedButNotInvoked e) {
            
            assertContains("<Capturing argument>", e.getMessage());
        }
    }

// org.mockitousage.matchers.CapturingArgumentsTest::shouldAllowAssertionsOnCapturedNull
    public void shouldAllowAssertionsOnCapturedNull() {
        
        emailService.sendEmailTo(null);
        
        
        ArgumentCaptor<Person> argument = ArgumentCaptor.forClass(Person.class);
        verify(emailService).sendEmailTo(argument.capture());
        assertEquals(null, argument.getValue());
    }

// org.mockitousage.matchers.CapturingArgumentsTest::shouldAllowCapturingForStubbing
    public void shouldAllowCapturingForStubbing() {
        
        ArgumentCaptor<Person> argument = ArgumentCaptor.forClass(Person.class);
        when(emailService.sendEmailTo(argument.capture())).thenReturn(false);
        
        
        emailService.sendEmailTo(new Person(10));
        
        
        assertEquals(10, argument.getValue().getAge());
    }

// org.mockitousage.matchers.CapturingArgumentsTest::shouldCaptureWhenStubbingOnlyWhenEntireInvocationMatches
    public void shouldCaptureWhenStubbingOnlyWhenEntireInvocationMatches() {
        
        ArgumentCaptor<String> argument = ArgumentCaptor.forClass(String.class);
        when(mock.simpleMethod(argument.capture(), eq(2))).thenReturn("blah");
        
        
        mock.simpleMethod("foo", 200);
        mock.simpleMethod("bar", 2);
        
        
        Assertions.assertThat(argument.getAllValues()).containsOnly("bar");
    }

// org.mockitousage.matchers.CapturingArgumentsTest::shouldSaySomethingSmartWhenMisused
    public void shouldSaySomethingSmartWhenMisused() {
        ArgumentCaptor<Person> argument = ArgumentCaptor.forClass(Person.class);
        try {
            argument.getValue();
            fail();
        } catch (MockitoException e) {}
    }

// org.mockitousage.matchers.CapturingArgumentsTest::shouldCaptureWhenFullArgListMatches
    public void shouldCaptureWhenFullArgListMatches() throws Exception {
        
        mock.simpleMethod("foo", 1);
        mock.simpleMethod("bar", 2);
        
        
        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(mock).simpleMethod(captor.capture(), eq(1));
        
        
        assertEquals(1, captor.getAllValues().size());
        assertEquals("foo", captor.getValue());
    }

// org.mockitousage.matchers.CapturingArgumentsTest::shouldCaptureIntByCreatingCaptorWithPrimitiveWrapper
    public void shouldCaptureIntByCreatingCaptorWithPrimitiveWrapper() {
        
        IMethods mock = mock(IMethods.class);
        ArgumentCaptor<Integer> argument = ArgumentCaptor.forClass(Integer.class);

        
        mock.intArgumentMethod(10);
        
        
        verify(mock).intArgumentMethod(argument.capture());
        assertEquals(10, (int) argument.getValue());
    }

// org.mockitousage.matchers.CapturingArgumentsTest::shouldCaptureIntByCreatingCaptorWithPrimitive
    public void shouldCaptureIntByCreatingCaptorWithPrimitive() throws Exception {
        
        IMethods mock = mock(IMethods.class);
        ArgumentCaptor<Integer> argument = ArgumentCaptor.forClass(int.class);
        
        
        mock.intArgumentMethod(10);
        
        
        verify(mock).intArgumentMethod(argument.capture());
        assertEquals(10, (int) argument.getValue());
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

// org.mockitousage.matchers.InvalidUseOfMatchersTest::shouldDetectWrongNumberOfMatchersWhenStubbing
    public void shouldDetectWrongNumberOfMatchersWhenStubbing() {
        Mockito.when(mock.threeArgumentMethod(1, "2", "3")).thenReturn(null);
        try {
            Mockito.when(mock.threeArgumentMethod(1, eq("2"), "3")).thenReturn(null);
            fail();
        } catch (InvalidUseOfMatchersException e) {}
    }

// org.mockitousage.matchers.InvalidUseOfMatchersTest::shouldDetectStupidUseOfMatchersWhenVerifying
    public void shouldDetectStupidUseOfMatchersWhenVerifying() {
        mock.oneArg(true);
        eq("that's the stupid way");
        eq("of using matchers");
        try {
            Mockito.verify(mock).oneArg(true);
            fail();
        } catch (InvalidUseOfMatchersException e) {}
    }

// org.mockitousage.matchers.InvalidUseOfMatchersTest::shouldScreamWhenMatchersAreInvalid
    public void shouldScreamWhenMatchersAreInvalid() {
        mock.simpleMethod(AdditionalMatchers.not(eq("asd")));
        try {
            mock.simpleMethod(AdditionalMatchers.not("jkl"));
            fail();
        } catch (InvalidUseOfMatchersException e) {
            assertContains("No matchers found for Not(?).", e.getMessage());
        }

        try {
            mock.simpleMethod(AdditionalMatchers.or(eq("jkl"), "asd"));
            fail();
        } catch (InvalidUseOfMatchersException e) {
            assertContains("2 matchers expected, 1 recorded.", e.getMessage());
        }

        try {
            mock.threeArgumentMethod(1, "asd", eq("asd"));
            fail();
        } catch (InvalidUseOfMatchersException e) {
            assertContains("3 matchers expected, 1 recorded.", e.getMessage());
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
        when(mock.oneArg(anyString())).thenReturn("1");
        
        assertEquals("1", mock.oneArg(""));
        assertEquals("1", mock.oneArg("any string"));
        assertEquals(null, mock.oneArg((Object) null));
    }

// org.mockitousage.matchers.MatchersTest::anyMatcher
    public void anyMatcher() {
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

// org.mockitousage.matchers.MoreMatchersTest::shouldHelpOutWithUnnecessaryCastingOfCollections
    public void shouldHelpOutWithUnnecessaryCastingOfCollections() {
        
        
        when(mock.collectionArgMethod(anyCollectionOf(String.class))).thenReturn("col");
        
        assertEquals("col", mock.collectionArgMethod(new ArrayList<String>()));
        assertEquals("col", mock.collectionArgMethod(Collections.<String>emptyList()));
    }

// org.mockitousage.matchers.NewMatchersTest::shouldAllowAnyList
    public void shouldAllowAnyList() {
        when(mock.forList(anyList())).thenReturn("x");
        
        assertEquals("x", mock.forList(null));
        assertEquals("x", mock.forList(Arrays.asList("x", "y")));
        
        verify(mock, times(2)).forList(anyList());
    }

// org.mockitousage.matchers.NewMatchersTest::shouldAllowAnyCollection
    public void shouldAllowAnyCollection() {
        when(mock.forCollection(anyCollection())).thenReturn("x");
        
        assertEquals("x", mock.forCollection(null));
        assertEquals("x", mock.forCollection(Arrays.asList("x", "y")));
        
        verify(mock, times(2)).forCollection(anyCollection());
    }

// org.mockitousage.matchers.NewMatchersTest::shouldAllowAnyMap
    public void shouldAllowAnyMap() {
        when(mock.forMap(anyMap())).thenReturn("x");
        
        assertEquals("x", mock.forMap(null));
        assertEquals("x", mock.forMap(new HashMap<String, String>()));
        
        verify(mock, times(2)).forMap(anyMap());
    }

// org.mockitousage.matchers.NewMatchersTest::shouldAllowAnySet
    public void shouldAllowAnySet() {
        when(mock.forSet(anySet())).thenReturn("x");
        
        assertEquals("x", mock.forSet(null));
        assertEquals("x", mock.forSet(new HashSet<String>()));
        
        verify(mock, times(2)).forSet(anySet());
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

// org.mockitousage.misuse.CleaningUpPotentialStubbingTest::shouldResetOngoingStubbingOnMock
    public void shouldResetOngoingStubbingOnMock() {
        mock.booleanReturningMethod();
        mock(IMethods.class);
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

// org.mockitousage.misuse.CleaningUpPotentialStubbingTest::shouldResetOngoingStubbingOnVerifyNoMoreInteractions
    public void shouldResetOngoingStubbingOnVerifyNoMoreInteractions() {
        mock.booleanReturningMethod();
        IMethods mock2 = mock(IMethods.class);
        verifyNoMoreInteractions(mock2);
        assertOngoingStubbingIsReset();
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
        when(mock.simpleMethod()).thenThrow(null);
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

// org.mockitousage.spies.SpyingOnInterfacesTest::shouldFailFastWhenCallingRealMethodOnInterface2
    public void shouldFailFastWhenCallingRealMethodOnInterface2() throws Exception {
        
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

// org.mockitousage.spies.SpyingOnRealObjectsTest::shouldVerify
    public void shouldVerify() {
        spy.add("one");
        spy.add("two");
        
        assertEquals("one", spy.get(0));
        assertEquals("two", spy.get(1));
        
        verify(spy).add("one");
        verify(spy).add("two");
    }

// org.mockitousage.spies.SpyingOnRealObjectsTest::shouldStub
    public void shouldStub() {
        spy.add("one");
        when(spy.get(0))
            .thenReturn("1")
            .thenReturn("1 again");
               
        assertEquals("1", spy.get(0));
        assertEquals("1 again", spy.get(0));
        assertEquals("one", spy.iterator().next());
        
        assertEquals(1, spy.size());
    }

// org.mockitousage.spies.SpyingOnRealObjectsTest::shouldAllowOverridingStubs
    public void shouldAllowOverridingStubs() {
        when(spy.contains(anyObject())).thenReturn(true);
        when(spy.contains("foo")).thenReturn(false);
        
        assertTrue(spy.contains("bar"));
        assertFalse(spy.contains("foo"));
    }

// org.mockitousage.spies.SpyingOnRealObjectsTest::shouldStubVoid
    public void shouldStubVoid() {
        stubVoid(spy)
            .toReturn()
            .toThrow(new RuntimeException())
            .on().clear();

        spy.add("one");
        spy.clear();
        try {
            spy.clear();
            fail();
        } catch (RuntimeException e) {}
            
        assertEquals(1, spy.size());
    }

// org.mockitousage.spies.SpyingOnRealObjectsTest::shouldStubWithDoReturnAndVerify
    public void shouldStubWithDoReturnAndVerify() {
        doReturn("foo")
        .doReturn("bar")
        .when(spy).get(0);
        
        assertEquals("foo", spy.get(0));
        assertEquals("bar", spy.get(0));
        
        verify(spy, times(2)).get(0);
        verifyNoMoreInteractions(spy);
    }

// org.mockitousage.spies.SpyingOnRealObjectsTest::shouldVerifyInOrder
    public void shouldVerifyInOrder() {
        spy.add("one");
        spy.add("two");
        
        InOrder inOrder = inOrder(spy);
        inOrder.verify(spy).add("one");
        inOrder.verify(spy).add("two");
        
        verifyNoMoreInteractions(spy);
    }

// org.mockitousage.spies.SpyingOnRealObjectsTest::shouldVerifyInOrderAndFail
    public void shouldVerifyInOrderAndFail() {
        spy.add("one");
        spy.add("two");
        
        InOrder inOrder = inOrder(spy);
        inOrder.verify(spy).add("two");
        try {
            inOrder.verify(spy).add("one");
            fail();
        } catch (VerificationInOrderFailure f) {}
    }

// org.mockitousage.spies.SpyingOnRealObjectsTest::shouldVerifyNumberOfTimes
    public void shouldVerifyNumberOfTimes() {
        spy.add("one");
        spy.add("one");
        
        verify(spy, times(2)).add("one");
        verifyNoMoreInteractions(spy);
    }

// org.mockitousage.spies.SpyingOnRealObjectsTest::shouldVerifyNumberOfTimesAndFail
    public void shouldVerifyNumberOfTimesAndFail() {
        spy.add("one");
        spy.add("one");
        
        try {
            verify(spy, times(3)).add("one");
            fail();
        } catch (TooLittleActualInvocations e) {}
    }

// org.mockitousage.spies.SpyingOnRealObjectsTest::shouldVerifyNoMoreInteractionsAndFail
    public void shouldVerifyNoMoreInteractionsAndFail() {
        spy.add("one");
        spy.add("two");
        
        verify(spy).add("one");
        try {
            verifyNoMoreInteractions(spy);
            fail();
        } catch (NoInteractionsWanted e) {}
    }

// org.mockitousage.spies.SpyingOnRealObjectsTest::shouldToString
    public void shouldToString() {
        spy.add("foo");
        assertEquals("[foo]" , spy.toString());
    }

// org.mockitousage.spies.SpyingOnRealObjectsTest::shouldDealWithAnonymousClasses
    public void shouldDealWithAnonymousClasses() {
        try {
            spy(new Foo() {});
            fail();
        } catch (MockitoException e) {
            assertContains("cannot mock", e.getMessage());
        }
    }

// org.mockitousage.spies.SpyingOnRealObjectsTest::shouldSayNiceMessageWhenSpyingOnPrivateClass
    public void shouldSayNiceMessageWhenSpyingOnPrivateClass() throws Exception {
        List real = Arrays.asList(new String[] {"first", "second"});
        try {
            spy(real);
            fail();
        } catch (MockitoException e) {
            assertContains("Most likely it is a private class that is not visible by Mockito", e.getMessage());
        }
    }

// org.mockitousage.stacktrace.StackTraceFilteringTest::shouldFilterStackTraceOnVerify
    public void shouldFilterStackTraceOnVerify() {
        try {
            verify(mock).simpleMethod();
            fail();
        } catch (WantedButNotInvoked e) {
            assertThat(e, hasFirstMethodInStackTrace("shouldFilterStackTraceOnVerify"));
        }
    }

// org.mockitousage.stacktrace.StackTraceFilteringTest::shouldFilterStackTraceOnVerifyNoMoreInteractions
    public void shouldFilterStackTraceOnVerifyNoMoreInteractions() {
        mock.oneArg(true);
        try {
            verifyNoMoreInteractions(mock);
            fail();
        } catch (NoInteractionsWanted e) {
            assertThat(e, hasFirstMethodInStackTrace("shouldFilterStackTraceOnVerifyNoMoreInteractions"));
        }
    }

// org.mockitousage.stacktrace.StackTraceFilteringTest::shouldFilterStackTraceOnVerifyZeroInteractions
    public void shouldFilterStackTraceOnVerifyZeroInteractions() {
        mock.oneArg(true);
        try {
            verifyZeroInteractions(mock);
            fail();
        } catch (NoInteractionsWanted e) {
            assertThat(e, hasFirstMethodInStackTrace("shouldFilterStackTraceOnVerifyZeroInteractions"));
        }
    }

// org.mockitousage.stacktrace.StackTraceFilteringTest::shouldFilterStacktraceOnMockitoException
    public void shouldFilterStacktraceOnMockitoException() {
        verify(mock);
        try {
            verify(mock).oneArg(true); 
            fail();
        } catch (MockitoException expected) {
            assertThat(expected, hasFirstMethodInStackTrace("shouldFilterStacktraceOnMockitoException"));
        }
    }

// org.mockitousage.stacktrace.StackTraceFilteringTest::shouldFilterStacktraceWhenVerifyingInOrder
    public void shouldFilterStacktraceWhenVerifyingInOrder() {
        InOrder inOrder = inOrder(mock);
        mock.oneArg(true);
        mock.oneArg(false);
        
        inOrder.verify(mock).oneArg(false);
        try {
            inOrder.verify(mock).oneArg(true);
            fail();
        } catch (VerificationInOrderFailure e) {
            assertThat(e, hasFirstMethodInStackTrace("shouldFilterStacktraceWhenVerifyingInOrder"));
        }
    }

// org.mockitousage.stacktrace.StackTraceFilteringTest::shouldFilterStacktraceWhenInOrderThrowsMockitoException
    public void shouldFilterStacktraceWhenInOrderThrowsMockitoException() {
        try {
            inOrder();
            fail();
        } catch (MockitoException expected) {
            assertThat(expected, hasFirstMethodInStackTrace("shouldFilterStacktraceWhenInOrderThrowsMockitoException"));
        }
    }

// org.mockitousage.stacktrace.StackTraceFilteringTest::shouldFilterStacktraceWhenInOrderVerifies
    public void shouldFilterStacktraceWhenInOrderVerifies() {
        try {
            InOrder inOrder = inOrder(mock);
            inOrder.verify(null);
            fail();
        } catch (MockitoException expected) {
            assertThat(expected, hasFirstMethodInStackTrace("shouldFilterStacktraceWhenInOrderVerifies"));
        }
    }

// org.mockitousage.stacktrace.StackTraceFilteringTest::shouldFilterStackTraceWhenThrowingExceptionFromMockHandler
    public void shouldFilterStackTraceWhenThrowingExceptionFromMockHandler() {
        try {
            when(mock.oneArg(true)).thenThrow(new Exception());
            fail();
        } catch (MockitoException expected) {
            assertThat(expected, hasFirstMethodInStackTrace("shouldFilterStackTraceWhenThrowingExceptionFromMockHandler"));
        }
    }

// org.mockitousage.stacktrace.StackTraceFilteringTest::shouldShowProperExceptionStackTrace
    public void shouldShowProperExceptionStackTrace() throws Exception {
        when(mock.simpleMethod()).thenThrow(new RuntimeException());

        try {
            mock.simpleMethod();
            fail();
        } catch (RuntimeException e) {
            assertThat(e, hasFirstMethodInStackTrace("shouldShowProperExceptionStackTrace"));
        }
    }

// org.mockitousage.stubbing.BasicStubbingTest::shouldEvaluateLatestStubbingFirst
    public void shouldEvaluateLatestStubbingFirst() throws Exception {
        when(mock.objectReturningMethod(isA(Integer.class))).thenReturn(100);
        when(mock.objectReturningMethod(200)).thenReturn(200);
        
        assertEquals(200, mock.objectReturningMethod(200));
        assertEquals(100, mock.objectReturningMethod(666));
        assertEquals("default behavior should return null", null, mock.objectReturningMethod("blah"));
    }

// org.mockitousage.stubbing.BasicStubbingTest::shouldStubbingBeTreatedAsInteraction
    public void shouldStubbingBeTreatedAsInteraction() throws Exception {
        when(mock.booleanReturningMethod()).thenReturn(true);
        
        mock.booleanReturningMethod();
        
        try {
            verifyNoMoreInteractions(mock);
            fail();
        } catch (NoInteractionsWanted e) {}
    }

// org.mockitousage.stubbing.BasicStubbingTest::shouldAllowStubbingToString
    public void shouldAllowStubbingToString() throws Exception {
        IMethods mockTwo = mock(IMethods.class);
        when(mockTwo.toString()).thenReturn("test");
        
        assertContains("Mock for IMethods", mock.toString());
        assertEquals("test", mockTwo.toString());
    }

// org.mockitousage.stubbing.BasicStubbingTest::shouldStubbingNotBeTreatedAsInteraction
    public void shouldStubbingNotBeTreatedAsInteraction() {
        when(mock.simpleMethod("one")).thenThrow(new RuntimeException());
        doThrow(new RuntimeException()).when(mock).simpleMethod("two");
        
        verifyZeroInteractions(mock);
    }

// org.mockitousage.stubbing.BasicStubbingTest::unfinishedStubbingCleansUpTheState
    public void unfinishedStubbingCleansUpTheState() {
        try {
            when("").thenReturn("");
            fail(); 
        } catch (MissingMethodInvocationException e) {}

        
        verifyZeroInteractions(mock);
    }

// org.mockitousage.stubbing.BasicStubbingTest::shouldToStringMockName
    public void shouldToStringMockName() {
        IMethods mock = mock(IMethods.class, "mockie");
        IMethods mockTwo = mock(IMethods.class);
        
        assertContains("Mock for IMethods", "" + mockTwo);
        assertEquals("mockie", "" + mock);
    }

// org.mockitousage.stubbing.BasicStubbingTest::shouldAllowMockingWhenToStringIsFinal
    public void shouldAllowMockingWhenToStringIsFinal() throws Exception {
        mock(Foo.class);
    }

// org.mockitousage.stubbing.CallingRealMethodTest::shouldAllowCallingInternalMethod
    public void shouldAllowCallingInternalMethod() {
        when(mock.getValue()).thenReturn("foo");
        when(mock.callInternalMethod()).thenCallRealMethod();
        
        assertEquals("foo", mock.callInternalMethod());
    }

// org.mockitousage.stubbing.CallingRealMethodTest::shouldReturnRealValue
    public void shouldReturnRealValue() {
        when(mock.getValue()).thenCallRealMethod();

        Assert.assertEquals("HARD_CODED_RETURN_VALUE", mock.getValue());
    }

// org.mockitousage.stubbing.CallingRealMethodTest::shouldExecuteRealMethod
    public void shouldExecuteRealMethod() {
        doCallRealMethod().when(mock).setValue(anyString());

        mock.setValue("REAL_VALUE");

        Assert.assertEquals("REAL_VALUE", mock.value);
    }

// org.mockitousage.stubbing.CallingRealMethodTest::shouldCallRealMethodByDefault
    public void shouldCallRealMethodByDefault() {
        TestedObject mock = mock(TestedObject.class, CALLS_REAL_METHODS);

        Assert.assertEquals("HARD_CODED_RETURN_VALUE", mock.getValue());
    }

// org.mockitousage.stubbing.CallingRealMethodTest::shouldNotCallRealMethodWhenStubbedLater
    public void shouldNotCallRealMethodWhenStubbedLater() {
        TestedObject mock = mock(TestedObject.class);

        when(mock.getValue()).thenCallRealMethod();
        when(mock.getValue()).thenReturn("FAKE_VALUE");

        Assert.assertEquals("FAKE_VALUE", mock.getValue());
    }

// org.mockitousage.stubbing.DeprecatedStubbingTest::shouldEvaluateLatestStubbingFirst
    public void shouldEvaluateLatestStubbingFirst() throws Exception {
        stub(mock.objectReturningMethod(isA(Integer.class))).toReturn(100);
        stub(mock.objectReturningMethod(200)).toReturn(200);
        
        assertEquals(200, mock.objectReturningMethod(200));
        assertEquals(100, mock.objectReturningMethod(666));
        assertEquals("default behavior should return null", null, mock.objectReturningMethod("blah"));
    }

// org.mockitousage.stubbing.DeprecatedStubbingTest::shouldStubbingBeTreatedAsInteraction
    public void shouldStubbingBeTreatedAsInteraction() throws Exception {
        stub(mock.booleanReturningMethod()).toReturn(true);
        
        mock.booleanReturningMethod();
        
        try {
            verifyNoMoreInteractions(mock);
            fail();
        } catch (NoInteractionsWanted e) {}
    }

// org.mockitousage.stubbing.DeprecatedStubbingTest::shouldAllowStubbingToString
    public void shouldAllowStubbingToString() throws Exception {
        IMethods mockTwo = mock(IMethods.class);
        stub(mockTwo.toString()).toReturn("test");
        
        assertContains("Mock for IMethods", mock.toString());
        assertEquals("test", mockTwo.toString());
    }

// org.mockitousage.stubbing.DeprecatedStubbingTest::shouldStubbingNotBeTreatedAsInteraction
    public void shouldStubbingNotBeTreatedAsInteraction() {
        stub(mock.simpleMethod("one")).toThrow(new RuntimeException());
        stubVoid(mock).toThrow(new RuntimeException()).on().simpleMethod("two");
        
        verifyZeroInteractions(mock);
    }

// org.mockitousage.stubbing.DeprecatedStubbingTest::shouldAllowConsecutiveStubbing
    public void shouldAllowConsecutiveStubbing() throws Exception {
        
        stub(mock.simpleMethod())
            .toReturn("100")
            .toReturn("200");
        
        
        assertEquals("100", mock.simpleMethod());
        assertEquals("200", mock.simpleMethod());
    }

// org.mockitousage.stubbing.StubbingConsecutiveAnswersTest::shouldReturnConsecutiveValues
    public void shouldReturnConsecutiveValues() throws Exception {
        when(mock.simpleMethod())
            .thenReturn("one")
            .thenReturn("two")
            .thenReturn("three");
        
        assertEquals("one", mock.simpleMethod());
        assertEquals("two", mock.simpleMethod());
        assertEquals("three", mock.simpleMethod());
        assertEquals("three", mock.simpleMethod());
        assertEquals("three", mock.simpleMethod());
    }

// org.mockitousage.stubbing.StubbingConsecutiveAnswersTest::shouldReturnConsecutiveValuesForTwoNulls
    public void shouldReturnConsecutiveValuesForTwoNulls() throws Exception {
        when(mock.simpleMethod()).thenReturn(null, null);
        
        assertNull(mock.simpleMethod());        
        assertNull(mock.simpleMethod());        
    }

// org.mockitousage.stubbing.StubbingConsecutiveAnswersTest::shouldReturnConsecutiveValuesSetByShortenThenReturnMethod
    public void shouldReturnConsecutiveValuesSetByShortenThenReturnMethod() throws Exception {        
        when(mock.simpleMethod())
            .thenReturn("one", "two", "three");
        
        assertEquals("one", mock.simpleMethod());
        assertEquals("two", mock.simpleMethod());
        assertEquals("three", mock.simpleMethod());
        assertEquals("three", mock.simpleMethod());
        assertEquals("three", mock.simpleMethod());
    }

// org.mockitousage.stubbing.StubbingConsecutiveAnswersTest::shouldReturnConsecutiveValueAndThrowExceptionssSetByShortenReturnMethods
    public void shouldReturnConsecutiveValueAndThrowExceptionssSetByShortenReturnMethods()
            throws Exception {
        when(mock.simpleMethod())
            .thenReturn("zero")
            .thenReturn("one", "two")
            .thenThrow(new NullPointerException(), new RuntimeException())
            .thenReturn("three")
            .thenThrow(new IllegalArgumentException());

        assertEquals("zero", mock.simpleMethod());
        assertEquals("one", mock.simpleMethod());
        assertEquals("two", mock.simpleMethod());
        try {
            mock.simpleMethod();
            fail();
        } catch (NullPointerException e) {}
        try {
            mock.simpleMethod();
            fail();
        } catch (RuntimeException e) {}
        assertEquals("three", mock.simpleMethod());
        try {
            mock.simpleMethod();
            fail();
        } catch (IllegalArgumentException e) {}
    }

// org.mockitousage.stubbing.StubbingConsecutiveAnswersTest::shouldThrowConsecutively
    public void shouldThrowConsecutively() throws Exception {
        when(mock.simpleMethod())
            .thenThrow(new RuntimeException())
            .thenThrow(new IllegalArgumentException())
            .thenThrow(new NullPointerException());

        try {
            mock.simpleMethod();
            fail();
        } catch (RuntimeException e) {}
        
        try {
            mock.simpleMethod();
            fail();
        } catch (IllegalArgumentException e) {}
        
        try {
            mock.simpleMethod();
            fail();
        } catch (NullPointerException e) {}
        
        try {
            mock.simpleMethod();
            fail();
        } catch (NullPointerException e) {}
    }

// org.mockitousage.stubbing.StubbingConsecutiveAnswersTest::shouldThrowConsecutivelySetByShortenThenThrowMethod
    public void shouldThrowConsecutivelySetByShortenThenThrowMethod() throws Exception {
        when(mock.simpleMethod())
            .thenThrow(new RuntimeException(), new IllegalArgumentException(), new NullPointerException());

        try {
            mock.simpleMethod();
            fail();
        } catch (RuntimeException e) {}
        
        try {
            mock.simpleMethod();
            fail();
        } catch (IllegalArgumentException e) {}
        
        try {
            mock.simpleMethod();
            fail();
        } catch (NullPointerException e) {}
        
        try {
            mock.simpleMethod();
            fail();
        } catch (NullPointerException e) {}
    }

// org.mockitousage.stubbing.StubbingConsecutiveAnswersTest::shouldMixConsecutiveReturnsWithExcepions
    public void shouldMixConsecutiveReturnsWithExcepions() throws Exception {
        when(mock.simpleMethod())
            .thenThrow(new IllegalArgumentException())
            .thenReturn("one")
            .thenThrow(new NullPointerException())
            .thenReturn(null);
        
        try {
            mock.simpleMethod();
            fail();
        } catch (IllegalArgumentException e) {}
        
        assertEquals("one", mock.simpleMethod());
        
        try {
            mock.simpleMethod();
            fail();
        } catch (NullPointerException e) {}
        
        assertEquals(null, mock.simpleMethod());
        assertEquals(null, mock.simpleMethod());
    }

// org.mockitousage.stubbing.StubbingConsecutiveAnswersTest::shouldValidateConsecutiveException
    public void shouldValidateConsecutiveException() throws Exception {
        when(mock.simpleMethod())
            .thenReturn("one")
            .thenThrow(new Exception());
    }

// org.mockitousage.stubbing.StubbingConsecutiveAnswersTest::shouldStubVoidMethodAndContinueThrowing
    public void shouldStubVoidMethodAndContinueThrowing() throws Exception {
        stubVoid(mock)
            .toThrow(new IllegalArgumentException())
            .toReturn()
            .toThrow(new NullPointerException())
            .on().voidMethod();
        
        try {
            mock.voidMethod();
            fail();
        } catch (IllegalArgumentException e) {}
        
        mock.voidMethod();
        
        try {
            mock.voidMethod();
            fail();
        } catch (NullPointerException e) {}
        
        try {
            mock.voidMethod();
            fail();
        } catch (NullPointerException e) {}        
    }

// org.mockitousage.stubbing.StubbingConsecutiveAnswersTest::shouldStubVoidMethod
    public void shouldStubVoidMethod() throws Exception {
        stubVoid(mock)
            .toReturn()
            .toThrow(new NullPointerException())
            .toReturn()
            .on().voidMethod();
        
        mock.voidMethod();
        
        try {
            mock.voidMethod();
            fail();
        } catch (NullPointerException e) {}
        
        mock.voidMethod();
        mock.voidMethod();
    }

// org.mockitousage.stubbing.StubbingConsecutiveAnswersTest::shouldValidateConsecutiveExceptionForVoidMethod
    public void shouldValidateConsecutiveExceptionForVoidMethod() throws Exception {
        stubVoid(mock)
            .toReturn()
            .toThrow(new Exception())
            .on().voidMethod();
    }

// org.mockitousage.stubbing.StubbingUsingDoReturnTest::shouldStub
    public void shouldStub() throws Exception {
        doReturn("foo").when(mock).simpleMethod();
        doReturn("bar").when(mock).simpleMethod();
        
        assertEquals("bar", mock.simpleMethod());
    }

// org.mockitousage.stubbing.StubbingUsingDoReturnTest::shouldStubWithArgs
    public void shouldStubWithArgs() throws Exception {
        doReturn("foo").when(mock).simpleMethod("foo");
        doReturn("bar").when(mock).simpleMethod(eq("one"), anyInt());
        
        assertEquals("foo", mock.simpleMethod("foo"));
        assertEquals("bar", mock.simpleMethod("one", 234));
        assertEquals(null, mock.simpleMethod("xxx", 234));
    }

// org.mockitousage.stubbing.StubbingUsingDoReturnTest::shouldStubWithThrowable
    public void shouldStubWithThrowable() throws Exception {
        doThrow(new FooRuntimeException()).when(mock).voidMethod();
        try {
            mock.voidMethod();
            fail();
        } catch (FooRuntimeException e) {}
    }

// org.mockitousage.stubbing.StubbingUsingDoReturnTest::shouldAllowSettingValidCheckedException
    public void shouldAllowSettingValidCheckedException() throws Exception {
        doThrow(new IOException()).when(mock).throwsIOException(0);
        
        try {
            mock.throwsIOException(0);
            fail();
        } catch (IOException e) {}
    }

// org.mockitousage.stubbing.StubbingUsingDoReturnTest::shouldDetectInvalidCheckedException
    public void shouldDetectInvalidCheckedException() throws Exception {
        try {
            doThrow(new FooCheckedException()).when(mock).throwsIOException(0);
            fail();
        } catch (Exception e) {
            assertContains("Checked exception is invalid", e.getMessage());
        }
    }

// org.mockitousage.stubbing.StubbingUsingDoReturnTest::shouldScreamWhenReturnSetForVoid
    public void shouldScreamWhenReturnSetForVoid() throws Exception {
        try {
            doReturn("foo").when(mock).voidMethod();
            fail();
        } catch (MockitoException e) {
            assertContains("Cannot stub a void method with a return value", e.getMessage());
        }
    }

// org.mockitousage.stubbing.StubbingUsingDoReturnTest::shouldScreamWhenNotAMockPassed
    public void shouldScreamWhenNotAMockPassed() throws Exception {
        try {
            doReturn("foo").when("foo").toString();
            fail();
        } catch (Exception e) {
            assertContains("Argument passed to when() is not a mock", e.getMessage());
        }
    }

// org.mockitousage.stubbing.StubbingUsingDoReturnTest::shouldScreamWhenNullPassed
    public void shouldScreamWhenNullPassed() throws Exception {
        try {
            doReturn("foo").when((Object) null).toString();
            fail();
        } catch (Exception e) {
            assertContains("Argument passed to when() is null", e.getMessage());
        }
    }

// org.mockitousage.stubbing.StubbingUsingDoReturnTest::shouldAllowChainedStubbing
    public void shouldAllowChainedStubbing() {
        doReturn("foo").
        doThrow(new RuntimeException()).
        doReturn("bar")
        .when(mock).simpleMethod();
        
        assertEquals("foo", mock.simpleMethod());
        try {
            mock.simpleMethod();
            fail();
        } catch (RuntimeException e) {}
        
        assertEquals("bar", mock.simpleMethod());
        assertEquals("bar", mock.simpleMethod());
    }

// org.mockitousage.stubbing.StubbingUsingDoReturnTest::shouldAllowChainedStubbingOnVoidMethods
    public void shouldAllowChainedStubbingOnVoidMethods() {
        doNothing().
        doNothing().
        doThrow(new RuntimeException())
        .when(mock).voidMethod();
        
        mock.voidMethod();
        mock.voidMethod();
        try {
            mock.voidMethod();
            fail();
        } catch (RuntimeException e) {}
        try {
            mock.voidMethod();
            fail();
        } catch (RuntimeException e) {}
    }

// org.mockitousage.stubbing.StubbingUsingDoReturnTest::shouldStubWithGenericAnswer
    public void shouldStubWithGenericAnswer() {
        doAnswer(new Answer<Object>() {
            public Object answer(InvocationOnMock invocation) throws Throwable {
                return "foo";
            }
        })
        .when(mock).simpleMethod();
        
        assertEquals("foo", mock.simpleMethod());
    }

// org.mockitousage.stubbing.StubbingUsingDoReturnTest::shouldNotAllowDoNothingOnNonVoids
    public void shouldNotAllowDoNothingOnNonVoids() {
        try {
            doNothing().when(mock).simpleMethod();
            fail();
        } catch (MockitoException e) {
            assertContains("Only void methods can doNothing()", e.getMessage());
        }
    }

// org.mockitousage.stubbing.StubbingUsingDoReturnTest::shouldStubbingBeTreatedAsInteraction
    public void shouldStubbingBeTreatedAsInteraction() throws Exception {
        doReturn("foo").when(mock).simpleMethod();
        mock.simpleMethod();
        try {
            verifyNoMoreInteractions(mock);
            fail();
        } catch (NoInteractionsWanted e) {}
    }

// org.mockitousage.stubbing.StubbingUsingDoReturnTest::shouldVerifyStubbedCall
    public void shouldVerifyStubbedCall() throws Exception {
        doReturn("foo").when(mock).simpleMethod();
        mock.simpleMethod();
        mock.simpleMethod();
        
        verify(mock, times(2)).simpleMethod();
        verifyNoMoreInteractions(mock);
    }

// org.mockitousage.stubbing.StubbingUsingDoReturnTest::shouldAllowStubbingToString
    public void shouldAllowStubbingToString() throws Exception {
        doReturn("test").when(mock).toString();
        assertEquals("test", mock.toString());
    }

// org.mockitousage.stubbing.StubbingUsingDoReturnTest::shouldDetectInvalidReturnType
    public void shouldDetectInvalidReturnType() throws Exception {
        try {
            doReturn("foo").when(mock).booleanObjectReturningMethod();
            fail();
        } catch (Exception e) {
            assertContains("String cannot be returned by booleanObjectReturningMethod()" +
                    "\n" +
                    "booleanObjectReturningMethod() should return Boolean",
                    e.getMessage());
        }
    }

// org.mockitousage.stubbing.StubbingUsingDoReturnTest::shouldDetectWhenNullAssignedToBoolean
    public void shouldDetectWhenNullAssignedToBoolean() throws Exception {
        try {
            doReturn(null).when(mock).intReturningMethod();
            fail();
        } catch (Exception e) {
            assertContains("null cannot be returned by intReturningMethod", e.getMessage());
        }
    }

// org.mockitousage.stubbing.StubbingUsingDoReturnTest::shouldAllowStubbingWhenTypesMatchSignature
    public void shouldAllowStubbingWhenTypesMatchSignature() throws Exception {
        doReturn("foo").when(mock).objectReturningMethodNoArgs();
        doReturn("foo").when(mock).simpleMethod();
        doReturn(1).when(mock).intReturningMethod();
        doReturn(new Integer(2)).when(mock).intReturningMethod();
    }

// org.mockitousage.stubbing.StubbingWithCustomAnswerTest::shouldAnswer
    public void shouldAnswer() throws Exception {
        when(mock.simpleMethod(anyString())).thenAnswer(new Answer<String>() {
            public String answer(InvocationOnMock invocation) throws Throwable {
                String arg = (String) invocation.getArguments()[0];

                return invocation.getMethod().getName() + "-" + arg;
            }
        });

        assertEquals("simpleMethod-test", mock.simpleMethod("test"));
    }

// org.mockitousage.stubbing.StubbingWithCustomAnswerTest::shouldAnswerConsecutively
    public void shouldAnswerConsecutively() throws Exception {
        when(mock.simpleMethod())
                .thenAnswer(new Answer<String>() {
                    public String answer(InvocationOnMock invocation) throws Throwable {
                        return invocation.getMethod().getName();
                    }
                })
                .thenReturn("Hello")
                .thenAnswer(new Answer<String>() {
                    public String answer(InvocationOnMock invocation) throws Throwable {
                        return invocation.getMethod().getName() + "-1";
                    }
                });

        assertEquals("simpleMethod", mock.simpleMethod());
        assertEquals("Hello", mock.simpleMethod());
        assertEquals("simpleMethod-1", mock.simpleMethod());
        assertEquals("simpleMethod-1", mock.simpleMethod());
    }

// org.mockitousage.stubbing.StubbingWithCustomAnswerTest::shoudAnswerVoidMethod
    public void shoudAnswerVoidMethod() throws Exception {
        RecordCall recordCall = new RecordCall();

        stubVoid(mock).toAnswer(recordCall).on().voidMethod();

        mock.voidMethod();
        assertTrue(recordCall.isCalled());
    }

// org.mockitousage.stubbing.StubbingWithCustomAnswerTest::shouldAnswerVoidMethodConsecutively
    public void shouldAnswerVoidMethodConsecutively() throws Exception {
        RecordCall call1 = new RecordCall();
        RecordCall call2 = new RecordCall();

        stubVoid(mock).toAnswer(call1)
                .toThrow(new UnsupportedOperationException())
                .toAnswer(call2)
                .on().voidMethod();

        mock.voidMethod();
        assertTrue(call1.isCalled());
        assertFalse(call2.isCalled());

        try {
            mock.voidMethod();
            fail();
        } catch (UnsupportedOperationException e) {
        }

        mock.voidMethod();
        assertTrue(call2.isCalled());

    }

// org.mockitousage.stubbing.StubbingWithThrowablesTest::shouldStubWithThrowable
    public void shouldStubWithThrowable() throws Exception {
        IllegalArgumentException expected = new IllegalArgumentException("thrown by mock");
        when(mock.add("throw")).thenThrow(expected);
        
        try {
            mock.add("throw");
            fail();
        } catch (IllegalArgumentException e) {
            assertEquals(expected, e);
        }
    }

// org.mockitousage.stubbing.StubbingWithThrowablesTest::shouldSetThrowableToVoidMethod
    public void shouldSetThrowableToVoidMethod() throws Exception {
        IllegalArgumentException expected = new IllegalArgumentException("thrown by mock");
        
        stubVoid(mock).toThrow(expected).on().clear();
        try {
            mock.clear();
            fail();
        } catch (Exception e) {
            assertEquals(expected, e);
        }
    }

// org.mockitousage.stubbing.StubbingWithThrowablesTest::shouldLastStubbingVoidBeImportant
    public void shouldLastStubbingVoidBeImportant() throws Exception {
        stubVoid(mock).toThrow(new ExceptionOne()).on().clear();
        stubVoid(mock).toThrow(new ExceptionTwo()).on().clear();
        
        try {
            mock.clear();
            fail();
        } catch (ExceptionTwo e) {}
    }

// org.mockitousage.stubbing.StubbingWithThrowablesTest::shouldFailStubbingThrowableOnTheSameInvocationDueToAcceptableLimitation
    public void shouldFailStubbingThrowableOnTheSameInvocationDueToAcceptableLimitation() throws Exception {
        when(mock.get(1)).thenThrow(new ExceptionOne());
        
        try {
            when(mock.get(1)).thenThrow(new ExceptionTwo());
            fail();
        } catch (ExceptionOne e) {}
    }

// org.mockitousage.stubbing.StubbingWithThrowablesTest::shouldAllowSettingCheckedException
    public void shouldAllowSettingCheckedException() throws Exception {
        Reader reader = mock(Reader.class);
        IOException ioException = new IOException();
        
        when(reader.read()).thenThrow(ioException);
        
        try {
            reader.read();
            fail();
        } catch (Exception e) {
            assertEquals(ioException, e);
        }
    }

// org.mockitousage.stubbing.StubbingWithThrowablesTest::shouldAllowSettingError
    public void shouldAllowSettingError() throws Exception {
        Error error = new Error();
        
        when(mock.add("quake")).thenThrow(error);
        
        try {
            mock.add("quake");
            fail();
        } catch (Error e) {
            assertEquals(error, e);
        }
    }

// org.mockitousage.stubbing.StubbingWithThrowablesTest::shouldNotAllowSettingInvalidCheckedException
    public void shouldNotAllowSettingInvalidCheckedException() throws Exception {
        when(mock.add("monkey island")).thenThrow(new Exception());
    }

// org.mockitousage.stubbing.StubbingWithThrowablesTest::shouldNotAllowSettingNullThrowable
    public void shouldNotAllowSettingNullThrowable() throws Exception {
        when(mock.add("monkey island")).thenThrow(null);
    }

// org.mockitousage.stubbing.StubbingWithThrowablesTest::shouldMixThrowablesAndReturnsOnDifferentMocks
    public void shouldMixThrowablesAndReturnsOnDifferentMocks() throws Exception {
        when(mock.add("ExceptionOne")).thenThrow(new ExceptionOne());
        when(mock.getLast()).thenReturn("last");
        stubVoid(mock).toThrow(new ExceptionTwo()).on().clear();
        
        stubVoid(mockTwo).toThrow(new ExceptionThree()).on().clear();
        when(mockTwo.containsValue("ExceptionFour")).thenThrow(new ExceptionFour());
        when(mockTwo.get("Are you there?")).thenReturn("Yes!");

        assertNull(mockTwo.get("foo"));
        assertTrue(mockTwo.keySet().isEmpty());
        assertEquals("Yes!", mockTwo.get("Are you there?"));
        try {
            mockTwo.clear();
            fail();
        } catch (ExceptionThree e) {}
        try {
            mockTwo.containsValue("ExceptionFour");
            fail();
        } catch (ExceptionFour e) {}
        
        assertNull(mock.getFirst());
        assertEquals("last", mock.getLast());
        try {
            mock.add("ExceptionOne");
            fail();
        } catch (ExceptionOne e) {}
        try {
            mock.clear();
            fail();
        } catch (ExceptionTwo e) {}
    }

// org.mockitousage.stubbing.StubbingWithThrowablesTest::shouldStubbingWithThrowableBeVerifiable
    public void shouldStubbingWithThrowableBeVerifiable() {
        when(mock.size()).thenThrow(new RuntimeException());
        stubVoid(mock).toThrow(new RuntimeException()).on().clone();
        
        try {
            mock.size();
            fail();
        } catch (RuntimeException e) {}
        
        try {
            mock.clone();
            fail();
        } catch (RuntimeException e) {}
        
        verify(mock).size();
        verify(mock).clone();
        verifyNoMoreInteractions(mock);
    }

// org.mockitousage.stubbing.StubbingWithThrowablesTest::shouldStubbingWithThrowableFailVerification
    public void shouldStubbingWithThrowableFailVerification() {
        when(mock.size()).thenThrow(new RuntimeException());
        stubVoid(mock).toThrow(new RuntimeException()).on().clone();
        
        verifyZeroInteractions(mock);
        
        mock.add("test");
        
        try {
            verify(mock).size();
            fail();
        } catch (WantedButNotInvoked e) {}
        
        try {
            verify(mock).clone();
            fail();
        } catch (WantedButNotInvoked e) {}
        
        try {
            verifyNoMoreInteractions(mock);
            fail();
        } catch (NoInteractionsWanted e) {}
    }

// org.mockitousage.verification.BasicVerificationTest::shouldVerify
    public void shouldVerify() throws Exception {
        mock.clear();
        verify(mock).clear();

        mock.add("test");
        verify(mock).add("test");

        verifyNoMoreInteractions(mock);
    }

// org.mockitousage.verification.BasicVerificationTest::shouldFailVerification
    public void shouldFailVerification() throws Exception {
        verify(mock).clear();
    }

// org.mockitousage.verification.BasicVerificationTest::shouldFailVerificationOnMethodArgument
    public void shouldFailVerificationOnMethodArgument() throws Exception {
        mock.clear();
        mock.add("foo");

        verify(mock).clear();
        try {
            verify(mock).add("bar");
            fail();
        } catch (AssertionError expected) {}
    }

// org.mockitousage.verification.BasicVerificationTest::shouldFailOnWrongMethod
    public void shouldFailOnWrongMethod() throws Exception {
        mock.clear();
        mock.clear();
        
        mockTwo.add("add");

        verify(mock, atLeastOnce()).clear();
        verify(mockTwo, atLeastOnce()).add("add");
        try {
            verify(mockTwo, atLeastOnce()).add("foo");
            fail();
        } catch (WantedButNotInvoked e) {}
    }

// org.mockitousage.verification.BasicVerificationTest::shouldDetectRedundantInvocation
    public void shouldDetectRedundantInvocation() throws Exception {
        mock.clear();
        mock.add("foo");
        mock.add("bar");

        verify(mock).clear();
        verify(mock).add("foo");

        try {
            verifyNoMoreInteractions(mock);
            fail();
        } catch (NoInteractionsWanted e) {}
    }

// org.mockitousage.verification.BasicVerificationTest::shouldDetectWhenInvokedMoreThanOnce
    public void shouldDetectWhenInvokedMoreThanOnce() throws Exception {
        mock.add("foo");
        mock.clear();
        mock.clear();
        
        verify(mock).add("foo");

        try {
            verify(mock).clear();
            fail();
        } catch (TooManyActualInvocations e) {}
    }

// org.mockitousage.verification.BasicVerificationTest::shouldVerifyStubbedMethods
    public void shouldVerifyStubbedMethods() throws Exception {
        when(mock.add("test")).thenReturn(Boolean.FALSE);
        
        mock.add("test");
        
        verify(mock).add("test");
    }

// org.mockitousage.verification.BasicVerificationTest::shouldDetectWhenOverloadedMethodCalled
    public void shouldDetectWhenOverloadedMethodCalled() throws Exception {
        IMethods mockThree = mock(IMethods.class);
        
        mockThree.varargs((Object[]) new Object[] {});
        try {
            verify(mockThree).varargs((String[]) new String[] {});
            fail();
        } catch(WantedButNotInvoked e) {}
    }

// org.mockitousage.verification.ExactNumberOfTimesVerificationTest::shouldDetectTooLittleActualInvocations
    public void shouldDetectTooLittleActualInvocations() throws Exception {
        mock.clear();
        mock.clear();

        verify(mock, times(2)).clear();
        try {
            verify(mock, times(100)).clear();
            fail();
        } catch (TooLittleActualInvocations e) {
            assertContains("Wanted 100 times", e.getMessage());
            assertContains("was 2", e.getMessage());
        }
    }

// org.mockitousage.verification.ExactNumberOfTimesVerificationTest::shouldDetectTooManyActualInvocations
    public void shouldDetectTooManyActualInvocations() throws Exception {
        mock.clear();
        mock.clear();

        verify(mock, times(2)).clear();
        try {
            verify(mock, times(1)).clear();
            fail();
        } catch (TooManyActualInvocations e) {
            assertContains("Wanted 1 time", e.getMessage());
            assertContains("was 2 times", e.getMessage());
        }
    }

// org.mockitousage.verification.ExactNumberOfTimesVerificationTest::shouldDetectActualInvocationsCountIsMoreThanZero
    public void shouldDetectActualInvocationsCountIsMoreThanZero() throws Exception {
        verify(mock, times(0)).clear();
        try {
            verify(mock, times(15)).clear();
            fail();
        } catch (WantedButNotInvoked e) {}
    }

// org.mockitousage.verification.ExactNumberOfTimesVerificationTest::shouldDetectActuallyCalledOnce
    public void shouldDetectActuallyCalledOnce() throws Exception {
        mock.clear();

        try {
            verify(mock, times(0)).clear();
            fail();
        } catch (NeverWantedButInvoked e) {
            assertContains("Never wanted here", e.getMessage());
        }
    }

// org.mockitousage.verification.ExactNumberOfTimesVerificationTest::shouldPassWhenMethodsActuallyNotCalled
    public void shouldPassWhenMethodsActuallyNotCalled() throws Exception {
        verify(mock, times(0)).clear();
        verify(mock, times(0)).add("yes, I wasn't called");
    }

// org.mockitousage.verification.ExactNumberOfTimesVerificationTest::shouldNotCountInStubbedInvocations
    public void shouldNotCountInStubbedInvocations() throws Exception {
        when(mock.add("test")).thenReturn(false);
        when(mock.add("test")).thenReturn(true);

        mock.add("test");
        mock.add("test");

        verify(mock, times(2)).add("test");
    }

// org.mockitousage.verification.ExactNumberOfTimesVerificationTest::shouldAllowVerifyingInteractionNeverHappened
    public void shouldAllowVerifyingInteractionNeverHappened() throws Exception {
        mock.add("one");

        verify(mock, never()).add("two");
        verify(mock, never()).clear();
        
        try {
            verify(mock, never()).add("one");
            fail();
        } catch (NeverWantedButInvoked e) {}
    }

// org.mockitousage.verification.ExactNumberOfTimesVerificationTest::shouldAllowVerifyingInteractionNeverHappenedInOrder
    public void shouldAllowVerifyingInteractionNeverHappenedInOrder() throws Exception {
        mock.add("one");
        mock.add("two");

        InOrder inOrder = inOrder(mock);
        
        inOrder.verify(mock, never()).add("xxx");
        inOrder.verify(mock).add("one");
        inOrder.verify(mock, never()).add("one");
        
        try {
            inOrder.verify(mock, never()).add("two");
            fail();
        } catch (VerificationInOrderFailure e) {}
    }

// org.mockitousage.verification.NoMoreInteractionsVerificationTest::shouldStubbingNotRegisterRedundantInteractions
    public void shouldStubbingNotRegisterRedundantInteractions() throws Exception {
        when(mock.add("one")).thenReturn(true);
        when(mock.add("two")).thenReturn(true);

        mock.add("one");
        
        verify(mock).add("one");
        verifyNoMoreInteractions(mock);
    }

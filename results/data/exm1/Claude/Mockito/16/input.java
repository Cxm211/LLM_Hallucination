// buggy code
    public <T> T mock(Class<T> classToMock, MockSettings mockSettings, boolean shouldResetOngoingStubbing) { return mock(classToMock, mockSettings); }

    public <T> T mock(Class<T> classToMock, MockSettings mockSettings) {
        mockingProgress.validateState();
            mockingProgress.resetOngoingStubbing();
        return mockUtil.createMock(classToMock, (MockSettingsImpl) mockSettings);
    }

    public static <T> T mock(Class<T> classToMock, MockSettings mockSettings) {
        return MOCKITO_CORE.mock(classToMock, mockSettings);
    }

    public static <T> T spy(T object) {
        return MOCKITO_CORE.mock((Class<T>) object.getClass(), withSettings()
                .spiedInstance(object)
                .defaultAnswer(CALLS_REAL_METHODS)); 
    }

// relevant test
// org.concurrentmockito.ThreadVerifiesContinuoslyInteractingMockTest::shouldAllowVerifyingInThreads
    public void shouldAllowVerifyingInThreads() throws Exception {
        for(int i = 0; i < 100; i++) {
            performTest();
        }
    }

// org.concurrentmockito.ThreadsShareAMockTest::shouldAllowVerifyingInThreads
    public void shouldAllowVerifyingInThreads() throws Exception {
        for(int i = 0; i < 100; i++) {
            performTest();
        }
    }

// org.concurrentmockito.ThreadsShareGenerouslyStubbedMockTest::shouldAllowVerifyingInThreads
    public void shouldAllowVerifyingInThreads() throws Exception {
        for(int i = 0; i < 50; i++) {
            performTest();
        }
    }

// org.concurrentmockito.VerificationInOrderFromMultipleThreadsTest::shouldVerifyInOrderWhenMultipleThreadsInteractWithMock
    public void shouldVerifyInOrderWhenMultipleThreadsInteractWithMock() throws Exception {
        final Foo testInf = mock(Foo.class);
        
        Thread threadOne = new Thread(new Runnable(){
            public void run() {
                testInf.methodOne();
            }
        });
        threadOne.start();
        threadOne.join();
        
        Thread threadTwo = new Thread(new Runnable(){
            public void run() {
                testInf.methodTwo();
            }
        });
        threadTwo.start();
        threadTwo.join();
        
        InOrder inOrder = inOrder(testInf);
        inOrder.verify(testInf).methodOne();
        inOrder.verify(testInf).methodTwo();
    }

// org.mockito.ArgumentCaptorTest::tellHandyReturnValuesToReturnValueFor
    public void tellHandyReturnValuesToReturnValueFor() throws Exception {
        
        final Object expected = new Object(); 
        ArgumentCaptor<Object> argumentCaptor = ArgumentCaptor.forClass(Object.class);
        argumentCaptor.handyReturnValues = new HandyReturnValues() {
            @Override
            public <T> T returnFor(Class<T> clazz) {
                return (T) expected;
            }
        };
        
        
        Object returned = argumentCaptor.capture();
        
        
        assertEquals(expected, returned);
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

// org.mockito.exceptions.ReporterTest::shouldLetPassingNullLastActualStackTrace
    public void shouldLetPassingNullLastActualStackTrace() throws Exception {
        new Reporter().tooLittleActualInvocations(new Discrepancy(1, 2), new InvocationBuilder().toInvocation(), null);
    }

// org.mockito.internal.AllInvocationsFinderTest::shouldGetAllInvocationsInOrder
    public void shouldGetAllInvocationsInOrder() throws Exception {
        mockOne.simpleMethod(100);
        mockTwo.simpleMethod(200);
        mockOne.simpleMethod(300);
        
        List<Invocation> invocations = finder.getAllInvocations(asList(mockOne, mockTwo));
        
        assertEquals(3, invocations.size());
        assertArgumentEquals(100, invocations.get(0));
        assertArgumentEquals(200, invocations.get(1));
        assertArgumentEquals(300, invocations.get(2));
    }

// org.mockito.internal.AllInvocationsFinderTest::shouldNotCountDuplicatedInteractions
    public void shouldNotCountDuplicatedInteractions() throws Exception {
        mockOne.simpleMethod(100);

        List<Invocation> invocations = finder.getAllInvocations(asList(mockOne, mockOne, mockOne));

        assertEquals(1, invocations.size());
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

// org.mockito.internal.MockHandlerTest::shouldRemoveVerificationModeEvenWhenInvalidMatchers
    public void shouldRemoveVerificationModeEvenWhenInvalidMatchers() throws Throwable {
        
        Invocation invocation = new InvocationBuilder().toInvocation();
        MockHandler handler = new MockHandler();
        handler.mockingProgress.verificationStarted(VerificationModeFactory.atLeastOnce());
        handler.matchersBinder = new MatchersBinder() {
            public InvocationMatcher bindMatchers(ArgumentMatcherStorage argumentMatcherStorage, Invocation invocation) {
                throw new InvalidUseOfMatchersException();
            }
        };
        
        try {
            
            handler.handle(invocation);
            
            
            fail();
        } catch (InvalidUseOfMatchersException e) {}
        
        assertNull(handler.mockingProgress.pullVerificationMode());
    }

// org.mockito.internal.MockitoCoreTest::shouldResetOngoingStubbingWhenAsked
    public void shouldResetOngoingStubbingWhenAsked() throws Exception {
        
        core.mock(Object.class, new MockSettingsImpl(), true);

        
        assertNull(mockingProgress.pullOngoingStubbing());
    }

// org.mockito.internal.MockitoCoreTest::shouldNOTResetOngoingStubbingWhenAsked
    public void shouldNOTResetOngoingStubbingWhenAsked() throws Exception {
        
        core.mock(Object.class, new MockSettingsImpl(), false);

        
        assertNull(mockingProgress.pullOngoingStubbing());
    }

// org.mockito.internal.configuration.ReadingConfigurationFromClasspathTest::shouldReadConfigurationClassFromClassPath
    public void shouldReadConfigurationClassFromClassPath() {
        ConfigurationAccess.getConfig().overrideDefaultAnswer(new Answer<Object>() {
            public Object answer(InvocationOnMock invocation) {
                return "foo";
            }});

        IMethods mock = mock(IMethods.class); 
        assertEquals("foo", mock.simpleMethod());
    }

// org.mockito.internal.creation.MethodInterceptorFilterTest::shouldBeSerializable
    public void shouldBeSerializable() throws Exception {
        new ObjectOutputStream(new ByteArrayOutputStream()).writeObject(new MethodInterceptorFilter(null, null));
    }

// org.mockito.internal.creation.MethodInterceptorFilterTest::shouldProvideOwnImplementationOfHashCode
    public void shouldProvideOwnImplementationOfHashCode() throws Throwable {
        
        Object ret = filter.intercept(new MethodsImpl(), MethodsImpl.class.getMethod("hashCode"), new Object[0], null);

        
        assertTrue((Integer) ret != 0);
        Mockito.verify(handler, never()).handle(any(Invocation.class));
    }

// org.mockito.internal.creation.MethodInterceptorFilterTest::shouldProvideOwnImplementationOfEquals
    public void shouldProvideOwnImplementationOfEquals() throws Throwable {
        
        MethodsImpl proxy = new MethodsImpl();
        Object ret = filter.intercept(proxy, MethodsImpl.class.getMethod("equals", Object.class), new Object[] {proxy}, null);

        
        assertTrue((Boolean) ret);
        Mockito.verify(handler, never()).handle(any(Invocation.class));
    }

// org.mockito.internal.creation.MethodInterceptorFilterTest::shouldCreateSerializableMethodProxyIfIsSerializableMock
    public void shouldCreateSerializableMethodProxyIfIsSerializableMock() throws Exception {
        MethodInterceptorFilter filter = new MethodInterceptorFilter(handler, (MockSettingsImpl) withSettings().serializable());
        MethodProxy methodProxy = MethodProxy.create(String.class, String.class, "", "toString", "toString");
        
        
        MockitoMethodProxy mockitoMethodProxy = filter.createMockitoMethodProxy(methodProxy);
        
        
        assertThat(mockitoMethodProxy, instanceOf(SerializableMockitoMethodProxy.class));
    }

// org.mockito.internal.creation.MethodInterceptorFilterTest::shouldCreateNONSerializableMethodProxyIfIsNotSerializableMock
    public void shouldCreateNONSerializableMethodProxyIfIsNotSerializableMock() throws Exception {
        MethodInterceptorFilter filter = new MethodInterceptorFilter(handler, (MockSettingsImpl) withSettings());
        MethodProxy methodProxy = MethodProxy.create(String.class, String.class, "", "toString", "toString");
        
        
        MockitoMethodProxy mockitoMethodProxy = filter.createMockitoMethodProxy(methodProxy);
        
        
        assertThat(mockitoMethodProxy, instanceOf(DelegatingMockitoMethodProxy.class));
    }

// org.mockito.internal.creation.MethodInterceptorFilterTest::shouldCreateSerializableMethodIfIsSerializableMock
    public void shouldCreateSerializableMethodIfIsSerializableMock() throws Exception {
        MethodInterceptorFilter filter = new MethodInterceptorFilter(handler, (MockSettingsImpl) withSettings().serializable());
        Method method = new InvocationBuilder().toInvocation().getMethod();
        
        
        MockitoMethod mockitoMethod = filter.createMockitoMethod(method);
        
        
        assertThat(mockitoMethod, instanceOf(SerializableMethod.class));
    }

// org.mockito.internal.creation.MethodInterceptorFilterTest::shouldCreateJustDelegatingMethodIfIsNotSerializableMock
    public void shouldCreateJustDelegatingMethodIfIsNotSerializableMock() throws Exception {
        MethodInterceptorFilter filter = new MethodInterceptorFilter(handler, (MockSettingsImpl) withSettings());
        Method method = new InvocationBuilder().toInvocation().getMethod();
        
        
        MockitoMethod mockitoMethod = filter.createMockitoMethod(method);
        
        
        assertThat(mockitoMethod, instanceOf(DelegatingMethod.class));
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

// org.mockito.internal.debugging.WarningsPrinterImplTest::shouldPrintUnusedStub
    public void shouldPrintUnusedStub() {
        
        Invocation unusedStub = new InvocationBuilder().simpleMethod().toInvocation();
        WarningsPrinterImpl p = new WarningsPrinterImpl(asList(unusedStub), Arrays.<InvocationMatcher> asList());

        
        p.print(logger);

        
        assertContains("never used", logger.getLoggedInfo());
    }

// org.mockito.internal.debugging.WarningsPrinterImplTest::shouldPrintUnstubbedInvocation
    public void shouldPrintUnstubbedInvocation() {
        
        InvocationMatcher unstubbedInvocation = new InvocationBuilder().differentMethod().toInvocationMatcher();
        WarningsPrinterImpl p = new WarningsPrinterImpl(Arrays.<Invocation> asList(), Arrays.<InvocationMatcher> asList(unstubbedInvocation), true);

        
        p.print(logger);

        
        assertContains("was not stubbed", logger.getLoggedInfo());
        assertContains("differentMethod()", logger.getLoggedInfo());
    }

// org.mockito.internal.debugging.WarningsPrinterImplTest::shouldPrintStubWasUsedWithDifferentArgs
    public void shouldPrintStubWasUsedWithDifferentArgs() {
        
        Invocation stub = new InvocationBuilder().arg("foo").mock(mock).toInvocation();
        InvocationMatcher wrongArg = new InvocationBuilder().arg("bar").mock(mock).toInvocationMatcher();

        WarningsPrinterImpl p = new WarningsPrinterImpl(Arrays.<Invocation> asList(stub), Arrays.<InvocationMatcher> asList(wrongArg));

        
        p.print(logger);

        
        assertContains("different arguments", logger.getLoggedInfo());
    }

// org.mockito.internal.debugging.WarningsPrinterImplTest::shouldNotPrintRedundantInformation
    public void shouldNotPrintRedundantInformation() {
        
        Invocation stub = new InvocationBuilder().arg("foo").mock(mock).toInvocation();
        InvocationMatcher wrongArg = new InvocationBuilder().arg("bar").mock(mock).toInvocationMatcher();

        WarningsPrinterImpl p = new WarningsPrinterImpl(Arrays.<Invocation> asList(stub), Arrays.<InvocationMatcher> asList(wrongArg));

        
        p.print(logger);

        
        assertNotContains("stub was not used", logger.getLoggedInfo());
        assertNotContains("was not stubbed", logger.getLoggedInfo());
    }

// org.mockito.internal.invocation.ArgumentsComparatorTest::shouldKnowWhenArgumentsMatch
    public void shouldKnowWhenArgumentsMatch() {
        
        Invocation invocation = new InvocationBuilder().args("1", 100).toInvocation();
        InvocationMatcher invocationMatcher = new InvocationBuilder().args("1", 100).toInvocationMatcher();

        
        boolean match = comparator.argumentsMatch(invocationMatcher, invocation);

        
        assertTrue(match);
    }

// org.mockito.internal.invocation.ArgumentsComparatorTest::shouldKnowWhenArgsDifferent
    public void shouldKnowWhenArgsDifferent() {
        
        Invocation invocation = new InvocationBuilder().args("1", 100).toInvocation();
        InvocationMatcher invocationMatcher = new InvocationBuilder().args("100", 100).toInvocationMatcher();

        
        boolean match = comparator.argumentsMatch(invocationMatcher, invocation);

        
        assertFalse(match);
    }

// org.mockito.internal.invocation.ArgumentsComparatorTest::shouldKnowWhenActualArgsSizeIsDifferent
    public void shouldKnowWhenActualArgsSizeIsDifferent() {
        
        Invocation invocation = new InvocationBuilder().args("100", 100).toInvocation();
        InvocationMatcher invocationMatcher = new InvocationBuilder().args("100").toInvocationMatcher();

        
        boolean match = comparator.argumentsMatch(invocationMatcher, invocation);

        
        assertFalse(match);
    }

// org.mockito.internal.invocation.ArgumentsComparatorTest::shouldKnowWhenMatchersSizeIsDifferent
    public void shouldKnowWhenMatchersSizeIsDifferent() {
        
        Invocation invocation = new InvocationBuilder().args("100").toInvocation();
        InvocationMatcher invocationMatcher = new InvocationBuilder().args("100", 100).toInvocationMatcher();

        
        boolean match = comparator.argumentsMatch(invocationMatcher, invocation);

        
        assertFalse(match);
    }

// org.mockito.internal.invocation.ArgumentsComparatorTest::shouldKnowWhenVarargsMatch
    public void shouldKnowWhenVarargsMatch() {
        
        mock.varargs("1", "2", "3");
        Invocation invocation = getLastInvocation();
        InvocationMatcher invocationMatcher = new InvocationMatcher(invocation, (List) asList(new Equals("1"), Any.ANY, new InstanceOf(String.class)));

        
        boolean match = comparator.argumentsMatch(invocationMatcher, invocation);

        
        assertTrue(match);
    }

// org.mockito.internal.invocation.ArgumentsComparatorTest::shouldKnowWhenVarargsDifferent
    public void shouldKnowWhenVarargsDifferent() {
        
        mock.varargs("1", "2");
        Invocation invocation = getLastInvocation();
        InvocationMatcher invocationMatcher = new InvocationMatcher(invocation, (List) asList(new Equals("100"), Any.ANY));

        
        boolean match = comparator.argumentsMatch(invocationMatcher, invocation);

        
        assertFalse(match);
    }

// org.mockito.internal.invocation.ArgumentsComparatorTest::shouldNotAllowAnyObjectMatchEntireVararg
    public void shouldNotAllowAnyObjectMatchEntireVararg() {
        
        mock.varargs("1", "2");
        Invocation invocation = getLastInvocation();
        InvocationMatcher invocationMatcher = new InvocationMatcher(invocation, (List) asList(Any.ANY));

        
        boolean match = comparator.argumentsMatch(invocationMatcher, invocation);

        
        assertFalse(match);
    }

// org.mockito.internal.invocation.ArgumentsComparatorTest::shouldAllowAnyVarargMatchEntireVararg
    public void shouldAllowAnyVarargMatchEntireVararg() {
        
        mock.varargs("1", "2");
        Invocation invocation = getLastInvocation();
        InvocationMatcher invocationMatcher = new InvocationMatcher(invocation, (List) asList(AnyVararg.ANY_VARARG));

        
        boolean match = comparator.argumentsMatch(invocationMatcher, invocation);

        
        assertTrue(match);
    }

// org.mockito.internal.invocation.ArgumentsComparatorTest::shouldNotAllowAnyObjectWithMixedVarargs
    public void shouldNotAllowAnyObjectWithMixedVarargs() {
        
        mock.mixedVarargs(1, "1", "2");
        Invocation invocation = getLastInvocation();
        InvocationMatcher invocationMatcher = new InvocationMatcher(invocation, (List) asList(new Equals(1)));

        
        boolean match = comparator.argumentsMatch(invocationMatcher, invocation);

        
        assertFalse(match);
    }

// org.mockito.internal.invocation.ArgumentsComparatorTest::shouldAllowAnyObjectWithMixedVarargs
    public void shouldAllowAnyObjectWithMixedVarargs() {
        
        mock.mixedVarargs(1, "1", "2");
        Invocation invocation = getLastInvocation();
        InvocationMatcher invocationMatcher = new InvocationMatcher(invocation, (List) asList(new Equals(1), AnyVararg.ANY_VARARG));

        
        boolean match = comparator.argumentsMatch(invocationMatcher, invocation);

        
        assertTrue(match);
    }

// org.mockito.internal.invocation.ArgumentsComparatorTest::shouldNotMatchWhenSomeOtherArgumentDoesNotMatch
    public void shouldNotMatchWhenSomeOtherArgumentDoesNotMatch() {
        
        mock.mixedVarargs(1, "1", "2");
        Invocation invocation = getLastInvocation();
        InvocationMatcher invocationMatcher = new InvocationMatcher(invocation, (List) asList(new Equals(100), AnyVararg.ANY_VARARG));

        
        boolean match = comparator.argumentsMatch(invocationMatcher, invocation);

        
        assertFalse(match);
    }

// org.mockito.internal.invocation.ArgumentsComparatorTest::shouldAnyObjectVarargDealWithDifferentSizeOfArgs
    public void shouldAnyObjectVarargDealWithDifferentSizeOfArgs() {
        
        mock.mixedVarargs(1, "1", "2");
        Invocation invocation = getLastInvocation();
        InvocationMatcher invocationMatcher = new InvocationMatcher(invocation, (List) asList(new Equals(1)));

        
        boolean match = comparator.argumentsMatch(invocationMatcher, invocation);

        
        assertFalse(match);
    }

// org.mockito.internal.invocation.ArgumentsComparatorTest::shouldMatchAnyVarargEvenIfOneOfTheArgsIsNull
    public void shouldMatchAnyVarargEvenIfOneOfTheArgsIsNull() {
        
        mock.mixedVarargs(null, null, "2");
        Invocation invocation = getLastInvocation();
        InvocationMatcher invocationMatcher = new InvocationMatcher(invocation, (List) asList(new Equals(null), AnyVararg.ANY_VARARG));

        
        boolean match = comparator.argumentsMatch(invocationMatcher, invocation);

        
        assertTrue(match);
    }

// org.mockito.internal.invocation.ArgumentsComparatorTest::shouldMatchAnyVarargEvenIfMatcherIsDecorated
    public void shouldMatchAnyVarargEvenIfMatcherIsDecorated() {
        
        mock.varargs("1", "2");
        Invocation invocation = getLastInvocation();
        InvocationMatcher invocationMatcher = new InvocationMatcher(invocation, (List) asList(new LocalizedMatcher(AnyVararg.ANY_VARARG)));

        
        boolean match = comparator.argumentsMatch(invocationMatcher, invocation);

        
        assertTrue(match);
    }

// org.mockito.internal.invocation.InvocationMarkerTest::shouldMarkInvocationAsVerified
    public void shouldMarkInvocationAsVerified() {
        
        InvocationMarker marker = new InvocationMarker();
        Invocation i = new InvocationBuilder().toInvocation();
        InvocationMatcher im = new InvocationBuilder().toInvocationMatcher();
        assertFalse(i.isVerified());
        
        
        marker.markVerified(Arrays.asList(i), im);
        
        
        assertTrue(i.isVerified());
    }

// org.mockito.internal.invocation.InvocationMarkerTest::shouldCaptureArguments
    public void shouldCaptureArguments() {
        
        InvocationMarker marker = new InvocationMarker();
        Invocation i = new InvocationBuilder().toInvocation();
        final ObjectBox box = new ObjectBox();
        CapturesArgumensFromInvocation c = new CapturesArgumensFromInvocation() {
            public void captureArgumentsFrom(Invocation i) {
                box.put(i);
            }};
        
        
        marker.markVerified(Arrays.asList(i), c);
        
        
        assertEquals(i, box.getObject());
    }

// org.mockito.internal.invocation.InvocationMarkerTest::shouldMarkInvocationsAsVerifiedInOrder
    public void shouldMarkInvocationsAsVerifiedInOrder() {
        
        InvocationMarker marker = new InvocationMarker();
        Invocation i = new InvocationBuilder().toInvocation();
        InvocationMatcher im = new InvocationBuilder().toInvocationMatcher();
        assertFalse(i.isVerifiedInOrder());
        assertFalse(i.isVerified());
        
        
        marker.markVerifiedInOrder(Arrays.asList(i), im);
        
        
        assertTrue(i.isVerifiedInOrder());
        assertTrue(i.isVerified());
    }

// org.mockito.internal.invocation.InvocationMatcherTest::shouldBeACitizenOfHashes
    public void shouldBeACitizenOfHashes() throws Exception {
        Invocation invocation = new InvocationBuilder().toInvocation();
        Invocation invocationTwo = new InvocationBuilder().args("blah").toInvocation();
        
        Map map = new HashMap();
        map.put(new InvocationMatcher(invocation), "one");
        map.put(new InvocationMatcher(invocationTwo), "two");
        
        assertEquals(2, map.size());
    }

// org.mockito.internal.invocation.InvocationMatcherTest::shouldNotEqualIfNumberOfArgumentsDiffer
    public void shouldNotEqualIfNumberOfArgumentsDiffer() throws Exception {
        PrintingFriendlyInvocation withOneArg = new InvocationMatcher(new InvocationBuilder().args("test").toInvocation());
        PrintingFriendlyInvocation withTwoArgs = new InvocationMatcher(new InvocationBuilder().args("test", 100).toInvocation());

        assertFalse(withOneArg.equals(null));
        assertFalse(withOneArg.equals(withTwoArgs));
    }

// org.mockito.internal.invocation.InvocationMatcherTest::shouldToStringWithMatchers
    public void shouldToStringWithMatchers() throws Exception {
        Matcher m = NotNull.NOT_NULL;
        InvocationMatcher notNull = new InvocationMatcher(new InvocationBuilder().toInvocation(), asList(m));
        Matcher mTwo = new Equals('x');
        InvocationMatcher equals = new InvocationMatcher(new InvocationBuilder().toInvocation(), asList(mTwo));

        assertContains("simpleMethod(notNull())", notNull.toString());
        assertContains("simpleMethod('x')", equals.toString());
    }

// org.mockito.internal.invocation.InvocationMatcherTest::shouldKnowIfIsSimilarTo
    public void shouldKnowIfIsSimilarTo() throws Exception {
        Invocation same = new InvocationBuilder().mock(mock).simpleMethod().toInvocation();
        assertTrue(simpleMethod.hasSimilarMethod(same));
        
        Invocation different = new InvocationBuilder().mock(mock).differentMethod().toInvocation();
        assertFalse(simpleMethod.hasSimilarMethod(different));
    }

// org.mockito.internal.invocation.InvocationMatcherTest::shouldNotBeSimilarToVerifiedInvocation
    public void shouldNotBeSimilarToVerifiedInvocation() throws Exception {
        Invocation verified = new InvocationBuilder().simpleMethod().verified().toInvocation();
        assertFalse(simpleMethod.hasSimilarMethod(verified));
    }

// org.mockito.internal.invocation.InvocationMatcherTest::shouldNotBeSimilarIfMocksAreDifferent
    public void shouldNotBeSimilarIfMocksAreDifferent() throws Exception {
        Invocation onDifferentMock = new InvocationBuilder().simpleMethod().mock("different mock").toInvocation();
        assertFalse(simpleMethod.hasSimilarMethod(onDifferentMock));
    }

// org.mockito.internal.invocation.InvocationMatcherTest::shouldNotBeSimilarIfIsOverloadedButUsedWithTheSameArg
    public void shouldNotBeSimilarIfIsOverloadedButUsedWithTheSameArg() throws Exception {
        Method method = IMethods.class.getMethod("simpleMethod", String.class);
        Method overloadedMethod = IMethods.class.getMethod("simpleMethod", Object.class);
        
        String sameArg = "test";
        
        InvocationMatcher invocation = new InvocationBuilder().method(method).arg(sameArg).toInvocationMatcher();
        Invocation overloadedInvocation = new InvocationBuilder().method(overloadedMethod).arg(sameArg).toInvocation();
        
        assertFalse(invocation.hasSimilarMethod(overloadedInvocation));
    }

// org.mockito.internal.invocation.InvocationMatcherTest::shouldBeSimilarIfIsOverloadedButUsedWithDifferentArg
    public void shouldBeSimilarIfIsOverloadedButUsedWithDifferentArg() throws Exception {
        Method method = IMethods.class.getMethod("simpleMethod", String.class);
        Method overloadedMethod = IMethods.class.getMethod("simpleMethod", Object.class);
        
        InvocationMatcher invocation = new InvocationBuilder().mock(mock).method(method).arg("foo").toInvocationMatcher();
        Invocation overloadedInvocation = new InvocationBuilder().mock(mock).method(overloadedMethod).arg("bar").toInvocation();
        
        assertTrue(invocation.hasSimilarMethod(overloadedInvocation));
    }

// org.mockito.internal.invocation.InvocationMatcherTest::shouldCaptureArgumentsFromInvocation
    public void shouldCaptureArgumentsFromInvocation() throws Exception {
        
        Invocation invocation = new InvocationBuilder().args("1", 100).toInvocation();
        CapturingMatcher capturingMatcher = new CapturingMatcher();
        InvocationMatcher invocationMatcher = new InvocationMatcher(invocation, (List) asList(new Equals("1"), capturingMatcher));
        
        
        invocationMatcher.captureArgumentsFrom(invocation);
        
        
        assertEquals(1, capturingMatcher.getAllValues().size());
        assertEquals(100, capturingMatcher.getLastValue());
    }

// org.mockito.internal.invocation.InvocationMatcherTest::shouldMatchVarargsUsingAnyVarargs
    public void shouldMatchVarargsUsingAnyVarargs() throws Exception {
        
        mock.varargs("1", "2");
        Invocation invocation = getLastInvocation();
        InvocationMatcher invocationMatcher = new InvocationMatcher(invocation, (List) asList(AnyVararg.ANY_VARARG));

        
        boolean match = invocationMatcher.matches(invocation);

        
        assertTrue(match);
    }

// org.mockito.internal.invocation.InvocationTest::shouldKnowIfIsEqualTo
    public void shouldKnowIfIsEqualTo() {
        Invocation equal =                  new InvocationBuilder().args(" ").mock("mock").toInvocation();
        Invocation nonEqual =               new InvocationBuilder().args("X").mock("mock").toInvocation();
        Invocation withNewStringInstance =  new InvocationBuilder().args(new String(" ")).mock("mock").toInvocation();

        assertFalse(invocation.equals(null));
        assertFalse(invocation.equals(""));
        assertTrue(invocation.equals(equal));
        assertFalse(invocation.equals(nonEqual));
        assertTrue(invocation.equals(withNewStringInstance));
    }

// org.mockito.internal.invocation.InvocationTest::shouldEqualToNotConsiderSequenceNumber
    public void shouldEqualToNotConsiderSequenceNumber() {
        Invocation equal = new InvocationBuilder().args(" ").mock("mock").seq(2).toInvocation();
        
        assertTrue(invocation.equals(equal));
        assertTrue(invocation.getSequenceNumber() != equal.getSequenceNumber());
    }

// org.mockito.internal.invocation.InvocationTest::shouldNotBeACitizenOfHashes
    public void shouldNotBeACitizenOfHashes() {
        Map map = new HashMap();
        try {
            map.put(invocation, "one");
            fail();
        } catch (RuntimeException e) {
            assertEquals("hashCode() is not implemented", e.getMessage());
        }
    }

// org.mockito.internal.invocation.InvocationTest::shouldPrintMethodName
    public void shouldPrintMethodName() {
        invocation = new InvocationBuilder().toInvocation();
        assertEquals("iMethods.simpleMethod();", invocation.toString());
    }

// org.mockito.internal.invocation.InvocationTest::shouldPrintMethodArgs
    public void shouldPrintMethodArgs() {
        invocation = new InvocationBuilder().args("foo").toInvocation();
        assertThat(invocation.toString(), endsWith("simpleMethod(\"foo\");"));
    }

// org.mockito.internal.invocation.InvocationTest::shouldPrintMethodIntegerArgAndString
    public void shouldPrintMethodIntegerArgAndString() {
        invocation = new InvocationBuilder().args("foo", 1).toInvocation();
        assertThat(invocation.toString(), endsWith("simpleMethod(\"foo\", 1);"));
    }

// org.mockito.internal.invocation.InvocationTest::shouldPrintNull
    public void shouldPrintNull() {
        invocation = new InvocationBuilder().args((String) null).toInvocation();
        assertThat(invocation.toString(), endsWith("simpleMethod(null);"));
    }

// org.mockito.internal.invocation.InvocationTest::shouldPrintArray
    public void shouldPrintArray() {
        invocation = new InvocationBuilder().method("oneArray").args(new int[] { 1, 2, 3 }).toInvocation();
        assertThat(invocation.toString(), endsWith("oneArray([1, 2, 3]);"));
    }

// org.mockito.internal.invocation.InvocationTest::shouldPrintNullIfArrayIsNull
    public void shouldPrintNullIfArrayIsNull() throws Exception {
        Method m = IMethods.class.getMethod("oneArray", Object[].class);
        invocation = new InvocationBuilder().method(m).args((Object) null).toInvocation();
        assertThat(invocation.toString(), endsWith("oneArray(null);"));
    }

// org.mockito.internal.invocation.InvocationTest::shouldPrintArgumentsInMultilinesWhenGetsTooBig
    public void shouldPrintArgumentsInMultilinesWhenGetsTooBig() {
        invocation = new InvocationBuilder().args("veeeeery long string that makes it ugly in one line", 1).toInvocation();
        assertThat(invocation.toString(), endsWith( 
                "simpleMethod(" +
                "\n" +
                "    \"veeeeery long string that makes it ugly in one line\"," +
                "\n" +
                "    1" +
                "\n" +
                ");"));
    }

// org.mockito.internal.invocation.InvocationTest::shouldMarkVerifiedWhenMarkingVerifiedInOrder
    public void shouldMarkVerifiedWhenMarkingVerifiedInOrder() throws Exception {
        assertFalse(invocation.isVerified());
        assertFalse(invocation.isVerifiedInOrder());
        
        invocation.markVerifiedInOrder();
        
        assertTrue(invocation.isVerified());
        assertTrue(invocation.isVerifiedInOrder());
    }

// org.mockito.internal.invocation.InvocationTest::shouldTransformArgumentsToMatchers
    public void shouldTransformArgumentsToMatchers() throws Exception {
        Invocation i = new InvocationBuilder().args("foo", new String[] {"bar"}).toInvocation();
        List matchers = i.argumentsToMatchers();

        assertEquals(2, matchers.size());
        assertEquals(Equals.class, matchers.get(0).getClass());
        assertEquals(ArrayEquals.class, matchers.get(1).getClass());
    }

// org.mockito.internal.invocation.InvocationTest::shouldKnowIfIsToString
    public void shouldKnowIfIsToString() throws Exception {
        Invocation toString = new InvocationBuilder().method("toString").toInvocation();
        assertTrue(Invocation.isToString(toString));
        
        Invocation notToString = new InvocationBuilder().method("toString").arg("foo").toInvocation();
        assertFalse(Invocation.isToString(notToString));
    }

// org.mockito.internal.invocation.InvocationTest::shouldKnowValidThrowables
    public void shouldKnowValidThrowables() throws Exception {
        Invocation invocation = new InvocationBuilder().method("canThrowException").toInvocation();
        assertFalse(invocation.isValidException(new Exception()));
        assertTrue(invocation.isValidException(new CharacterCodingException()));
    }

// org.mockito.internal.invocation.InvocationTest::shouldBeAbleToCallRealMethod
    public void shouldBeAbleToCallRealMethod() throws Throwable {
        
        Invocation invocation = invocationOf(Foo.class, "bark", new RealMethod() {
            public Object invoke(Object target, Object[] arguments) throws Throwable {
                return new Foo().bark();
            }});
        
        assertEquals("woof", invocation.callRealMethod());
    }

// org.mockito.internal.invocation.InvocationTest::shouldScreamWhenCallingRealMethodOnInterface
    public void shouldScreamWhenCallingRealMethodOnInterface() throws Throwable {
        
        Invocation invocationOnInterface = new InvocationBuilder().toInvocation();

        try {
            
            invocationOnInterface.callRealMethod();
            
            fail();
        } catch(MockitoException e) {}
    }

// org.mockito.internal.invocation.InvocationsFinderTest::shouldFindActualInvocations
    public void shouldFindActualInvocations() throws Exception {
        List<Invocation> actual = finder.findInvocations(invocations, new InvocationMatcher(simpleMethodInvocation));
        assertThat(actual, hasExactlyInOrder(simpleMethodInvocation, simpleMethodInvocationTwo));
        
        actual = finder.findInvocations(invocations, new InvocationMatcher(differentMethodInvocation));
        assertThat(actual, hasExactlyInOrder(differentMethodInvocation));
    }

// org.mockito.internal.invocation.InvocationsFinderTest::shouldFindFirstUnverifiedInvocation
    public void shouldFindFirstUnverifiedInvocation() throws Exception {
        assertSame(simpleMethodInvocation, finder.findFirstUnverified(invocations));
        
        simpleMethodInvocationTwo.markVerified();
        simpleMethodInvocation.markVerified();
        
        assertSame(differentMethodInvocation, finder.findFirstUnverified(invocations));
        
        differentMethodInvocation.markVerified();
        assertNull(finder.findFirstUnverified(invocations));
    }

// org.mockito.internal.invocation.InvocationsFinderTest::shouldFindFirstUnverifiedInvocationOnMock
    public void shouldFindFirstUnverifiedInvocationOnMock() throws Exception {
        assertSame(simpleMethodInvocation, finder.findFirstUnverified(invocations, simpleMethodInvocation.getMock()));
        assertNull(finder.findFirstUnverified(invocations, "different mock"));
    }

// org.mockito.internal.invocation.InvocationsFinderTest::shouldFindFirstSimilarInvocationByName
    public void shouldFindFirstSimilarInvocationByName() throws Exception {
        Invocation overloadedSimpleMethod = new InvocationBuilder().mock(mock).simpleMethod().arg("test").toInvocation();
        
        Invocation found = finder.findSimilarInvocation(invocations, new InvocationMatcher(overloadedSimpleMethod));
        assertSame(found, simpleMethodInvocation);
    }

// org.mockito.internal.invocation.InvocationsFinderTest::shouldFindInvocationWithTheSameMethod
    public void shouldFindInvocationWithTheSameMethod() throws Exception {
        Invocation overloadedDifferentMethod = new InvocationBuilder().differentMethod().arg("test").toInvocation();
        
        invocations.add(overloadedDifferentMethod);
        
        Invocation found = finder.findSimilarInvocation(invocations, new InvocationMatcher(overloadedDifferentMethod));
        assertSame(found, overloadedDifferentMethod);
    }

// org.mockito.internal.invocation.InvocationsFinderTest::shouldGetLastStackTrace
    public void shouldGetLastStackTrace() throws Exception {
        Location last = finder.getLastLocation(invocations);
        assertSame(differentMethodInvocation.getLocation(), last);
        
        assertNull(finder.getLastLocation(Collections.<Invocation>emptyList()));
    }

// org.mockito.internal.invocation.InvocationsFinderTest::shouldFindAllMatchingUnverifiedChunks
    public void shouldFindAllMatchingUnverifiedChunks() throws Exception {
        List<Invocation> allMatching = finder.findAllMatchingUnverifiedChunks(invocations, new InvocationMatcher(simpleMethodInvocation));
        assertThat(allMatching, hasExactlyInOrder(simpleMethodInvocation, simpleMethodInvocationTwo));
        
        simpleMethodInvocation.markVerifiedInOrder();
        allMatching = finder.findAllMatchingUnverifiedChunks(invocations, new InvocationMatcher(simpleMethodInvocation));
        assertThat(allMatching, hasExactlyInOrder(simpleMethodInvocationTwo));
        
        simpleMethodInvocationTwo.markVerifiedInOrder();
        allMatching = finder.findAllMatchingUnverifiedChunks(invocations, new InvocationMatcher(simpleMethodInvocation));
        assertTrue(allMatching.isEmpty());
    }

// org.mockito.internal.invocation.InvocationsFinderTest::shouldFindMatchingChunk
    public void shouldFindMatchingChunk() throws Exception {
        List<Invocation> chunk = finder.findMatchingChunk(invocations, new InvocationMatcher(simpleMethodInvocation), 2);
        assertThat(chunk, hasExactlyInOrder(simpleMethodInvocation, simpleMethodInvocationTwo));
    }

// org.mockito.internal.invocation.InvocationsFinderTest::shouldReturnAllChunksWhenModeIsAtLeastOnce
    public void shouldReturnAllChunksWhenModeIsAtLeastOnce() throws Exception {
        Invocation simpleMethodInvocationThree = new InvocationBuilder().mock(mock).toInvocation();
        invocations.add(simpleMethodInvocationThree);
        
        List<Invocation> chunk = finder.findMatchingChunk(invocations, new InvocationMatcher(simpleMethodInvocation), 1);
        assertThat(chunk, hasExactlyInOrder(simpleMethodInvocation, simpleMethodInvocationTwo, simpleMethodInvocationThree));
    }

// org.mockito.internal.invocation.InvocationsFinderTest::shouldReturnAllChunksWhenWantedCountDoesntMatch
    public void shouldReturnAllChunksWhenWantedCountDoesntMatch() throws Exception {
        Invocation simpleMethodInvocationThree = new InvocationBuilder().mock(mock).toInvocation();
        invocations.add(simpleMethodInvocationThree);
        
        List<Invocation> chunk = finder.findMatchingChunk(invocations, new InvocationMatcher(simpleMethodInvocation), 1);
        assertThat(chunk, hasExactlyInOrder(simpleMethodInvocation, simpleMethodInvocationTwo, simpleMethodInvocationThree));
    }

// org.mockito.internal.invocation.InvocationsFinderTest::shouldFindPreviousInOrder
    public void shouldFindPreviousInOrder() throws Exception {
        Invocation previous = finder.findPreviousVerifiedInOrder(invocations);
        assertNull(previous);
        
        simpleMethodInvocation.markVerifiedInOrder();
        simpleMethodInvocationTwo.markVerifiedInOrder();
        
        previous = finder.findPreviousVerifiedInOrder(invocations);
        assertSame(simpleMethodInvocationTwo, previous);
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
            invocationContainerImpl.addAnswer(new ThrowsException(new Exception()));
            fail();
        } catch (MockitoException e) {
            state.validateState();
        }
    }

// org.mockito.internal.stubbing.MockitoStubberTest::shouldFinishStubbingOnAddingReturnValue
    public void shouldFinishStubbingOnAddingReturnValue() throws Exception {
        state.stubbingStarted();
        invocationContainerImpl.addAnswer(new Returns("test"));
        state.validateState();
    }

// org.mockito.internal.stubbing.MockitoStubberTest::shouldGetResultsForMethods
    public void shouldGetResultsForMethods() throws Throwable {
        invocationContainerImpl.setInvocationForPotentialStubbing(new InvocationMatcher(simpleMethod));
        invocationContainerImpl.addAnswer(new Returns("simpleMethod"));
        
        Invocation differentMethod = new InvocationBuilder().differentMethod().toInvocation();
        invocationContainerImpl.setInvocationForPotentialStubbing(new InvocationMatcher(differentMethod));
        invocationContainerImpl.addAnswer(new ThrowsException(new MyException()));
        
        assertEquals("simpleMethod", invocationContainerImpl.answerTo(simpleMethod));
        
        try {
            invocationContainerImpl.answerTo(differentMethod);
            fail();
        } catch (MyException e) {}
    }

// org.mockito.internal.stubbing.MockitoStubberTest::shouldAddThrowableForVoidMethod
    public void shouldAddThrowableForVoidMethod() throws Throwable {
        invocationContainerImpl.addAnswerForVoidMethod(new ThrowsException(new MyException()));
        invocationContainerImpl.setMethodForStubbing(new InvocationMatcher(simpleMethod));
        
        try {
            invocationContainerImpl.answerTo(simpleMethod);
            fail();
        } catch (MyException e) {}
    }

// org.mockito.internal.stubbing.MockitoStubberTest::shouldValidateThrowableForVoidMethod
    public void shouldValidateThrowableForVoidMethod() throws Throwable {
        invocationContainerImpl.addAnswerForVoidMethod(new ThrowsException(new Exception()));
        
        try {
            invocationContainerImpl.setMethodForStubbing(new InvocationMatcher(simpleMethod));
            fail();
        } catch (MockitoException e) {}
    }

// org.mockito.internal.stubbing.MockitoStubberTest::shouldValidateThrowable
    public void shouldValidateThrowable() throws Throwable {
        try {
            invocationContainerImpl.addAnswer(new ThrowsException(null));
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

// org.mockito.internal.stubbing.defaultanswers.ReturnsMocksTest::shouldReturnMockValueForInterface
    public void shouldReturnMockValueForInterface() throws Exception {
        Object interfaceMock = values.returnValueFor(FooInterface.class);
        assertTrue(new MockUtil().isMock(interfaceMock));
    }

// org.mockito.internal.stubbing.defaultanswers.ReturnsMocksTest::shouldReturnNullForFinalClass
    public void shouldReturnNullForFinalClass() throws Exception {
        assertNull(values.returnValueFor(Baz.class));
    }

// org.mockito.internal.stubbing.defaultanswers.ReturnsMocksTest::shouldReturnTheUsualDefaultValuesForPrimitives
    public void shouldReturnTheUsualDefaultValuesForPrimitives()
            throws Throwable {
        ReturnsMocks answer = new ReturnsMocks();
        assertEquals(false, answer.answer(invocationOf(HasPrimitiveMethods.class, "booleanMethod")));
        assertEquals((char) 0, answer.answer(invocationOf(HasPrimitiveMethods.class, "charMethod")));
        assertEquals(0, answer.answer(invocationOf(HasPrimitiveMethods.class, "intMethod")));
        assertEquals(0, answer.answer(invocationOf(HasPrimitiveMethods.class, "longMethod")));
        assertEquals(0, answer.answer(invocationOf(HasPrimitiveMethods.class, "floatMethod")));
        assertEquals(0, answer.answer(invocationOf(HasPrimitiveMethods.class, "doubleMethod")));
    }

// org.mockito.internal.stubbing.defaultanswers.ReturnsMocksTest::shouldReturnEmptyArray
    public void shouldReturnEmptyArray() throws Throwable {
        String[] ret = (String[]) values.answer(invocationOf(StringMethods.class, "stringArrayMethod"));
        
        assertTrue(ret.getClass().isArray());
        assertTrue(ret.length == 0);
    }

// org.mockito.internal.stubbing.defaultanswers.ReturnsMocksTest::shouldReturnEmptyString
    public void shouldReturnEmptyString() throws Throwable {
        assertEquals("", values.answer(invocationOf(StringMethods.class, "stringMethod")));
    }

// org.mockito.internal.util.MockUtilTest::shouldValidate
    public void shouldValidate() {
        
        assertFalse(creationValidator.extraInterfacesValidated);
        assertFalse(creationValidator.typeValidated);

        
        mockUtil.createMock(IMethods.class, new MockSettingsImpl());
        
        
        assertTrue(creationValidator.extraInterfacesValidated);
        assertTrue(creationValidator.typeValidated);
    }

// org.mockito.internal.util.MockUtilTest::shouldGetHandler
    public void shouldGetHandler() {
        List mock = Mockito.mock(List.class);
        assertNotNull(mockUtil.getMockHandler(mock));
    }

// org.mockito.internal.util.MockUtilTest::shouldScreamWhenEnhancedButNotAMockPassed
    public void shouldScreamWhenEnhancedButNotAMockPassed() {
        Object o = Enhancer.create(ArrayList.class, NoOp.INSTANCE);
        try {
            mockUtil.getMockHandler(o);
            fail();
        } catch (NotAMockException e) {}
    }

// org.mockito.internal.util.MockUtilTest::shouldScreamWhenNotAMockPassed
    public void shouldScreamWhenNotAMockPassed() {
        mockUtil.getMockHandler("");
    }

// org.mockito.internal.util.MockUtilTest::shouldScreamWhenNullPassed
    public void shouldScreamWhenNullPassed() {
        mockUtil.getMockHandler(null);
    }

// org.mockito.internal.util.MockUtilTest::shouldValidateMock
    public void shouldValidateMock() {
        assertFalse(mockUtil.isMock("i mock a mock"));
        assertTrue(mockUtil.isMock(Mockito.mock(List.class)));
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

// org.mockito.internal.util.reflection.LenientCopyToolTest::shouldBeAbleToCopyFromRealObjectToRealObject
    public void shouldBeAbleToCopyFromRealObjectToRealObject() throws Exception {
        
        
        from.defaultField = "defaultField";
        from.instancePublicField = new SomeOtherObject();
        from.privateField = 1;
        from.privateTransientField = 2;
        from.protectedField = "protectedField";
        from.protectedInherited = "protectedInherited";
        to = new SomeObject(0);
        
        
        tool.copyToRealObject(from, to);
        
        
        assertEquals(from.defaultField, to.defaultField);
        assertEquals(from.instancePublicField, to.instancePublicField);
        assertEquals(from.privateField, to.privateField);
        assertEquals(from.privateTransientField, to.privateTransientField);
        assertEquals(from.protectedField, to.protectedField);
        assertEquals(from.protectedInherited, to.protectedInherited);
        
    }

// org.mockito.internal.verification.RegisteredInvocationsTest::shouldNotReturnToStringMethod
    public void shouldNotReturnToStringMethod() throws Exception {
        Invocation toString = new InvocationBuilder().method("toString").toInvocation();
        Invocation simpleMethod = new InvocationBuilder().simpleMethod().toInvocation();
        
        invocations.add(toString);
        invocations.add(simpleMethod);
        
        assertTrue(invocations.getAll().contains(simpleMethod));
        assertFalse(invocations.getAll().contains(toString));
    }

// org.mockito.internal.verification.SmartPrinterTest::shouldPrintBothInMultilinesWhenFirstIsMulti
    public void shouldPrintBothInMultilinesWhenFirstIsMulti() {
        
        SmartPrinter printer = new SmartPrinter(multi, shortie);
        
        
        assertContains("\n", printer.getWanted().toString());
        assertContains("\n", printer.getActual().toString());
    }

// org.mockito.internal.verification.SmartPrinterTest::shouldPrintBothInMultilinesWhenSecondIsMulti
    public void shouldPrintBothInMultilinesWhenSecondIsMulti() {
        
        SmartPrinter printer = new SmartPrinter(shortie, multi);
        
        
        assertContains("\n", printer.getWanted().toString());
        assertContains("\n", printer.getActual().toString());
    }

// org.mockito.internal.verification.SmartPrinterTest::shouldPrintBothInMultilinesWhenBothAreMulti
    public void shouldPrintBothInMultilinesWhenBothAreMulti() {
        
        SmartPrinter printer = new SmartPrinter(multi, multi);
        
        
        assertContains("\n", printer.getWanted().toString());
        assertContains("\n", printer.getActual().toString());
    }

// org.mockito.internal.verification.SmartPrinterTest::shouldPrintBothInSingleLineWhenBothAreShort
    public void shouldPrintBothInSingleLineWhenBothAreShort() {
        
        SmartPrinter printer = new SmartPrinter(shortie, shortie);
        
        
        assertNotContains("\n", printer.getWanted().toString());
        assertNotContains("\n", printer.getActual().toString());
    }

// org.mockito.internal.verification.checkers.AtLeastXNumberOfInvocationsCheckerTest::shouldMarkActualInvocationsAsVerified
    public void shouldMarkActualInvocationsAsVerified() {
        
        AtLeastXNumberOfInvocationsChecker c = new AtLeastXNumberOfInvocationsChecker();
        c.invocationMarker = Mockito.mock(InvocationMarker.class);
        Invocation invocation = new InvocationBuilder().simpleMethod().toInvocation();
        Invocation invocationTwo = new InvocationBuilder().differentMethod().toInvocation();

        
        c.check(asList(invocation, invocationTwo), new InvocationMatcher(invocation), 1);

        
        Mockito.verify(c.invocationMarker).markVerified(eq(asList(invocation)), any(CapturesArgumensFromInvocation.class));
    }

// org.mockito.internal.verification.checkers.MissingInvocationCheckerTest::shouldAskFinderForActualInvocations
    public void shouldAskFinderForActualInvocations() {
        finderStub.actualToReturn.add(new InvocationBuilder().toInvocation());
        checker.check(invocations, wanted);
        
        assertSame(invocations, finderStub.invocations);
    }

// org.mockito.internal.verification.checkers.MissingInvocationCheckerTest::shouldPassBecauseActualInvocationFound
    public void shouldPassBecauseActualInvocationFound() {
        finderStub.actualToReturn.add(new InvocationBuilder().toInvocation());
        checker.check(invocations, wanted);
    }

// org.mockito.internal.verification.checkers.MissingInvocationCheckerTest::shouldAskAnalyzerForSimilarInvocation
    public void shouldAskAnalyzerForSimilarInvocation() {
        checker.check(invocations, wanted);
        
        assertSame(invocations, finderStub.invocations);
    }

// org.mockito.internal.verification.checkers.MissingInvocationCheckerTest::shouldReportWantedButNotInvoked
    public void shouldReportWantedButNotInvoked() {
        
        assertTrue(finderStub.actualToReturn.isEmpty());
        finderStub.similarToReturn = null;
        
        
        checker.check(invocations, wanted);
        
        
        assertEquals(wanted, reporterStub.wanted);
        assertNull(reporterStub.actualLocation);
    }

// org.mockito.internal.verification.checkers.MissingInvocationCheckerTest::shouldReportWantedInvocationDiffersFromActual
    public void shouldReportWantedInvocationDiffersFromActual() {
        assertTrue(finderStub.actualToReturn.isEmpty());
        Invocation actualInvocation = new InvocationBuilder().toInvocation();
        finderStub.similarToReturn = actualInvocation;
        
        checker.check(invocations, wanted);
        
        assertNotNull(reporterStub.wanted);
        assertNotNull(reporterStub.actual);
        
        assertSame(actualInvocation.getLocation(), reporterStub.actualLocation);
    }

// org.mockito.internal.verification.checkers.MissingInvocationInOrderCheckerTest::shouldPassWhenMatchingInteractionFound
    public void shouldPassWhenMatchingInteractionFound() throws Exception {
        Invocation actual = new InvocationBuilder().toInvocation();
        finderStub.allMatchingUnverifiedChunksToReturn.add(actual);
        
        checker.check(invocations, wanted, new VerificationModeBuilder().inOrder());
    }

// org.mockito.internal.verification.checkers.MissingInvocationInOrderCheckerTest::shouldReportWantedButNotInvoked
    public void shouldReportWantedButNotInvoked() throws Exception {
        assertTrue(finderStub.allMatchingUnverifiedChunksToReturn.isEmpty());
        checker.check(invocations, wanted, new VerificationModeBuilder().inOrder());
        
        assertEquals(wanted, reporterStub.wanted);
    }

// org.mockito.internal.verification.checkers.MissingInvocationInOrderCheckerTest::shouldReportWantedDiffersFromActual
    public void shouldReportWantedDiffersFromActual() throws Exception {
        Invocation previous = new InvocationBuilder().toInvocation();
        finderStub.previousInOrderToReturn = previous;
        
        checker.check(invocations, wanted, new VerificationModeBuilder().inOrder());
        
        assertEquals(wanted, reporterStub.wanted);
        assertEquals(previous, reporterStub.previous);
    }

// org.mockito.internal.verification.checkers.NumberOfInvocationsCheckerTest::shouldReportTooLittleActual
    public void shouldReportTooLittleActual() throws Exception {
        finderStub.actualToReturn.add(new InvocationBuilder().toInvocation());
        
        checker.check(invocations, wanted, 100);
        
        assertEquals(1, reporterStub.actualCount);
        assertEquals(100, reporterStub.wantedCount);
        assertEquals(wanted, reporterStub.wanted);
    }

// org.mockito.internal.verification.checkers.NumberOfInvocationsCheckerTest::shouldReportWithLastInvocationStackTrace
    public void shouldReportWithLastInvocationStackTrace() throws Exception {
        Invocation first = new InvocationBuilder().toInvocation();
        Invocation second = new InvocationBuilder().toInvocation();
        
        finderStub.actualToReturn.addAll(asList(first, second));
        
        checker.check(invocations, wanted, 100);
        
        assertSame(second.getLocation(), reporterStub.location);
    }

// org.mockito.internal.verification.checkers.NumberOfInvocationsCheckerTest::shouldNotReportWithLastInvocationStackTraceIfNoInvocationsFound
    public void shouldNotReportWithLastInvocationStackTraceIfNoInvocationsFound() throws Exception {
        assertTrue(finderStub.actualToReturn.isEmpty());
        
        checker.check(invocations, wanted, 100);
        
        assertNull(reporterStub.location);
    }

// org.mockito.internal.verification.checkers.NumberOfInvocationsCheckerTest::shouldReportWithFirstUndesiredInvocationStackTrace
    public void shouldReportWithFirstUndesiredInvocationStackTrace() throws Exception {
        Invocation first = new InvocationBuilder().toInvocation();
        Invocation second = new InvocationBuilder().toInvocation();
        Invocation third = new InvocationBuilder().toInvocation();
        
        finderStub.actualToReturn.addAll(asList(first, second, third));
        
        checker.check(invocations, wanted, 2);
        
        assertSame(third.getLocation(), reporterStub.location);
    }

// org.mockito.internal.verification.checkers.NumberOfInvocationsCheckerTest::shouldReportTooManyActual
    public void shouldReportTooManyActual() throws Exception {
        finderStub.actualToReturn.add(new InvocationBuilder().toInvocation());
        finderStub.actualToReturn.add(new InvocationBuilder().toInvocation());
        
        checker.check(invocations, wanted, 1);
        
        assertEquals(2, reporterStub.actualCount);
        assertEquals(1, reporterStub.wantedCount);
        assertEquals(wanted, reporterStub.wanted);
    }

// org.mockito.internal.verification.checkers.NumberOfInvocationsCheckerTest::shouldReportNeverWantedButInvoked
    public void shouldReportNeverWantedButInvoked() throws Exception {
        Invocation invocation = new InvocationBuilder().toInvocation();
        finderStub.actualToReturn.add(invocation);
        
        checker.check(invocations, wanted, 0);
        
        assertEquals(wanted, reporterStub.wanted);
        assertEquals(invocation.getLocation(), reporterStub.location);
    }

// org.mockito.internal.verification.checkers.NumberOfInvocationsCheckerTest::shouldMarkInvocationsAsVerified
    public void shouldMarkInvocationsAsVerified() throws Exception {
        Invocation invocation = new InvocationBuilder().toInvocation();
        finderStub.actualToReturn.add(invocation);
        assertFalse(invocation.isVerified());
        
        checker.check(invocations, wanted, 1);
        
        assertTrue(invocation.isVerified());
    }

// org.mockito.internal.verification.checkers.NumberOfInvocationsInOrderCheckerTest::shouldPassIfWantedIsZeroAndMatchingChunkIsEmpty
    public void shouldPassIfWantedIsZeroAndMatchingChunkIsEmpty() throws Exception {
        assertTrue(finderStub.validMatchingChunkToReturn.isEmpty());
        checker.check(invocations, wanted, 0);
    }

// org.mockito.internal.verification.checkers.NumberOfInvocationsInOrderCheckerTest::shouldPassIfChunkMatches
    public void shouldPassIfChunkMatches() throws Exception {
        finderStub.validMatchingChunkToReturn.add(wanted.getInvocation());
        
        checker.check(invocations, wanted, 1);
    }

// org.mockito.internal.verification.checkers.NumberOfInvocationsInOrderCheckerTest::shouldReportTooLittleInvocations
    public void shouldReportTooLittleInvocations() throws Exception {
        Invocation first = new InvocationBuilder().toInvocation();
        Invocation second = new InvocationBuilder().toInvocation();
        finderStub.validMatchingChunkToReturn.addAll(asList(first, second)); 
        
        try {
            checker.check(invocations, wanted, 4);
            fail();
        } catch (VerificationInOrderFailure e) {
            assertContains("Wanted 4 times", e.getMessage());
            assertContains("But was 2 times", e.getMessage());
        }
    }

// org.mockito.internal.verification.checkers.NumberOfInvocationsInOrderCheckerTest::shouldReportTooManyInvocations
    public void shouldReportTooManyInvocations() throws Exception {
        Invocation first = new InvocationBuilder().toInvocation();
        Invocation second = new InvocationBuilder().toInvocation();
        finderStub.validMatchingChunkToReturn.addAll(asList(first, second)); 
        
        try {
            checker.check(invocations, wanted, 1);
            fail();
        } catch (VerificationInOrderFailure e) {
            assertContains("Wanted 1 time", e.getMessage());
            assertContains("But was 2 times", e.getMessage());
        }
    }

// org.mockito.internal.verification.checkers.NumberOfInvocationsInOrderCheckerTest::shouldMarkAsVerifiedInOrder
    public void shouldMarkAsVerifiedInOrder() throws Exception {
        Invocation invocation = new InvocationBuilder().toInvocation();
        assertFalse(invocation.isVerifiedInOrder());
        finderStub.validMatchingChunkToReturn.addAll(asList(invocation)); 
        
        checker.check(invocations, wanted, 1);
        
        assertTrue(invocation.isVerifiedInOrder());
    }

// org.mockito.runners.ConsoleSpammingMockitoJUnitRunnerTest::shouldLogUnusedStubbingWarningWhenTestFails
    public void shouldLogUnusedStubbingWarningWhenTestFails() throws Exception {
        runner = new ConsoleSpammingMockitoJUnitRunner(loggerStub, new RunnerImplStub() {
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
        runner = new ConsoleSpammingMockitoJUnitRunner(loggerStub, new RunnerImplStub() {
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
        runner = new ConsoleSpammingMockitoJUnitRunner(loggerStub, new RunnerImplStub() {
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
        runner = new ConsoleSpammingMockitoJUnitRunner(loggerStub, new RunnerImplStub() {
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
        runner = new ConsoleSpammingMockitoJUnitRunner(loggerStub, new RunnerImplStub() {
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
        runner = new ConsoleSpammingMockitoJUnitRunner(loggerStub, new RunnerImplStub() {
            public Description getDescription() {
                return expectedDescription;
            }
        });
        
        
        Description description = runner.getDescription();
        
        
        assertEquals(expectedDescription, description);
    }

// org.mockitousage.PlaygroundTest::should
    public void should() throws Exception {
        Boo boo = mock(Boo.class);
        boo.withLong(100);
        
        verify(boo).withLong(new Long(100));
    }

// org.mockitousage.PlaygroundTest::spyInAction
    public void spyInAction() {
        mock = spy(new Foo());

        
        when(mock.getStuff()).thenReturn("aha!");
        
        mock.doSomeThing();
    }

// org.mockitousage.PlaygroundTest::partialMockInAction
    public void partialMockInAction() {

        

    }

// org.mockitousage.PlaygroundWithDemoOfUnclonedParametersProblemTest::shouldIncludeInitialLog
    public void shouldIncludeInitialLog() {
        
        int importType = 0;
        Date currentDate = new GregorianCalendar(2009, 10, 12).getTime();

        ImportLogBean initialLog = new ImportLogBean(currentDate, importType);
        initialLog.setStatus(1);

        given(importLogDao.anyImportRunningOrRunnedToday(importType, currentDate)).willReturn(false);
        willAnswer(byCheckingLogEquals(initialLog)).given(importLogDao).include(any(ImportLogBean.class));

        
        importManager.startImportProcess(importType, currentDate);

        
        verify(importLogDao).include(any(ImportLogBean.class));
    }

// org.mockitousage.PlaygroundWithDemoOfUnclonedParametersProblemTest::shouldAlterFinalLog
    public void shouldAlterFinalLog() {
        
        int importType = 0;
        Date currentDate = new GregorianCalendar(2009, 10, 12).getTime();

        ImportLogBean finalLog = new ImportLogBean(currentDate, importType);
        finalLog.setStatus(9);

        given(importLogDao.anyImportRunningOrRunnedToday(importType, currentDate)).willReturn(false);
        willAnswer(byCheckingLogEquals(finalLog)).given(importLogDao).alter(any(ImportLogBean.class));

        
        importManager.startImportProcess(importType, currentDate);

        
        verify(importLogDao).alter(any(ImportLogBean.class));
    }

// org.mockitousage.basicapi.AnnotationsTest::shouldInitMocks
    public void shouldInitMocks() throws Exception {
        list.clear();
        map.clear();
        listTwo.clear();
        
        verify(list).clear();
        verify(map).clear();
        verify(listTwo).clear();
    }

// org.mockitousage.basicapi.AnnotationsTest::shouldScreamWhenInitializingMocksForNullClass
    public void shouldScreamWhenInitializingMocksForNullClass() throws Exception {
        try {
            MockitoAnnotations.initMocks(null);
            fail();
        } catch (MockitoException e) {
            assertEquals("testClass cannot be null. For info how to use @Mock annotations see examples in javadoc for MockitoAnnotations class",
                    e.getMessage());
        }
    }

// org.mockitousage.basicapi.AnnotationsTest::shouldLookForAnnotatedMocksInSuperClasses
    public void shouldLookForAnnotatedMocksInSuperClasses() throws Exception {
        Sub sub = new Sub();
        MockitoAnnotations.initMocks(sub);
        
        assertNotNull(sub.getMock());
        assertNotNull(sub.getBaseMock());
        assertNotNull(sub.getSuperBaseMock());
    }

// org.mockitousage.basicapi.MockingMultipleInterfacesTest::shouldAllowMultipleInterfaces
    public void shouldAllowMultipleInterfaces() {
        
        Foo mock = mock(Foo.class, withSettings().extraInterfaces(IFoo.class, IBar.class));
        
        
        assertThat(mock, is(IFoo.class));
        assertThat(mock, is(IBar.class));
    }

// org.mockitousage.basicapi.MockingMultipleInterfacesTest::shouldScreamWhenNullPassedInsteadOfAnInterface
    public void shouldScreamWhenNullPassedInsteadOfAnInterface() {
        try {
            
            mock(Foo.class, withSettings().extraInterfaces(IFoo.class, null));
            fail();
        } catch (MockitoException e) {
            
            assertContains("extraInterfaces() does not accept null parameters", e.getMessage());
        }
    }

// org.mockitousage.basicapi.MockingMultipleInterfacesTest::shouldScreamWhenNoArgsPassed
    public void shouldScreamWhenNoArgsPassed() {
        try {
            
            mock(Foo.class, withSettings().extraInterfaces());
            fail();
        } catch (MockitoException e) {
            
            assertContains("extraInterfaces() requires at least one interface", e.getMessage());
        }
    }

// org.mockitousage.basicapi.MockingMultipleInterfacesTest::shouldScreamWhenNullPassedInsteadOfAnArray
    public void shouldScreamWhenNullPassedInsteadOfAnArray() {
        try {
            
            mock(Foo.class, withSettings().extraInterfaces((Class[]) null));
            fail();
        } catch (MockitoException e) {
            
            assertContains("extraInterfaces() requires at least one interface", e.getMessage());
        }
    }

// org.mockitousage.basicapi.MockingMultipleInterfacesTest::shouldScreamWhenNonInterfacePassed
    public void shouldScreamWhenNonInterfacePassed() {
        try {
            
            mock(Foo.class, withSettings().extraInterfaces(Foo.class));
            fail();
        } catch (MockitoException e) {
            
            assertContains("Foo which is not an interface", e.getMessage());
        }
    }

// org.mockitousage.basicapi.MockingMultipleInterfacesTest::shouldScreamWhenTheSameInterfacesPassed
    public void shouldScreamWhenTheSameInterfacesPassed() {
        try {
            
            mock(IMethods.class, withSettings().extraInterfaces(IMethods.class));
            fail();
        } catch (MockitoException e) {
            
            assertContains("You mocked following type: IMethods", e.getMessage());
        }
    }

// org.mockitousage.basicapi.MocksCreationTest::shouldCreateMockWhenConstructorIsPrivate
    public void shouldCreateMockWhenConstructorIsPrivate() {
        assertNotNull(Mockito.mock(HasPrivateConstructor.class));
    }

// org.mockitousage.basicapi.MocksCreationTest::shouldCombineMockNameAndSmartNulls
    public void shouldCombineMockNameAndSmartNulls() {
        
        IMethods mock = mock(IMethods.class, withSettings()
            .defaultAnswer(RETURNS_SMART_NULLS)
            .name("great mockie"));    
        
        
        IMethods smartNull = mock.iMethodsReturningMethod();
        String name = mock.toString();
        
        
        assertContains("great mockie", name);
        
        try {
            smartNull.simpleMethod();
            fail();
        } catch(SmartNullPointerException e) {}
    }

// org.mockitousage.basicapi.MocksCreationTest::shouldCombineMockNameAndExtraInterfaces
    public void shouldCombineMockNameAndExtraInterfaces() {
        
        IMethods mock = mock(IMethods.class, withSettings()
                .extraInterfaces(List.class)
                .name("great mockie"));
        
        
        String name = mock.toString();
        
        
        assertContains("great mockie", name);
        
        assertThat(mock, is(List.class));
    }

// org.mockitousage.basicapi.MocksCreationTest::shouldSpecifyMockNameViaSettings
    public void shouldSpecifyMockNameViaSettings() {
        
        IMethods mock = mock(IMethods.class, withSettings().name("great mockie"));

        
        String name = mock.toString();
        
        
        assertContains("great mockie", name);
    }

// org.mockitousage.basicapi.MocksCreationTest::shouldScreamWhenSpyCreatedWithWrongType
    public void shouldScreamWhenSpyCreatedWithWrongType() {
        
        List list = new LinkedList();
        try {
            
            mock(List.class, withSettings().spiedInstance(list));
            fail();
            
        } catch (MockitoException e) {}
    }

// org.mockitousage.basicapi.MocksCreationTest::shouldAllowCreatingSpiesWithCorrectType
    public void shouldAllowCreatingSpiesWithCorrectType() {
        List list = new LinkedList();
        mock(LinkedList.class, withSettings().spiedInstance(list));
    }

// org.mockitousage.basicapi.MocksSerializationTest::shouldAllowMockToBeSerializable
    public void shouldAllowMockToBeSerializable() throws Exception {
        
        IMethods mock = mock(IMethods.class, withSettings().serializable());

        
        serializeAndBack(mock);
    }

// org.mockitousage.basicapi.MocksSerializationTest::shouldAllowMockAndBooleanValueToSerializable
    public void shouldAllowMockAndBooleanValueToSerializable() throws Exception {
        
        IMethods mock = mock(IMethods.class, withSettings().serializable());
        when(mock.booleanReturningMethod()).thenReturn(true);

        
        ByteArrayOutputStream serialized = serializeMock(mock);

        
        IMethods readObject = deserializeMock(serialized, IMethods.class);
        assertTrue(readObject.booleanReturningMethod());
    }

// org.mockitousage.basicapi.MocksSerializationTest::shouldAllowMockAndStringValueToBeSerializable
    public void shouldAllowMockAndStringValueToBeSerializable() throws Exception {
        
        IMethods mock = mock(IMethods.class, withSettings().serializable());
        String value = "value";
        when(mock.stringReturningMethod()).thenReturn(value);

        
        ByteArrayOutputStream serialized = serializeMock(mock);

        
        IMethods readObject = deserializeMock(serialized, IMethods.class);
        assertEquals(value, readObject.stringReturningMethod());
    }

// org.mockitousage.basicapi.MocksSerializationTest::shouldAllMockAndSerializableValueToBeSerialized
    public void shouldAllMockAndSerializableValueToBeSerialized() throws Exception {
        
        IMethods mock = mock(IMethods.class, withSettings().serializable());
        List<?> value = Collections.emptyList();
        when(mock.objectReturningMethodNoArgs()).thenReturn(value);

        
        ByteArrayOutputStream serialized = serializeMock(mock);

        
        IMethods readObject = deserializeMock(serialized, IMethods.class);
        assertEquals(value, readObject.objectReturningMethodNoArgs());
    }

// org.mockitousage.basicapi.MocksSerializationTest::shouldSerializeMethodCallWithParametersThatAreSerializable
    public void shouldSerializeMethodCallWithParametersThatAreSerializable() throws Exception {
        IMethods mock = mock(IMethods.class, withSettings().serializable());
        List<?> value = Collections.emptyList();
        when(mock.objectArgMethod(value)).thenReturn(value);

        
        ByteArrayOutputStream serialized = serializeMock(mock);

        
        IMethods readObject = deserializeMock(serialized, IMethods.class);
        assertEquals(value, readObject.objectArgMethod(value));
    }

// org.mockitousage.basicapi.MocksSerializationTest::shouldSerializeMethodCallsUsingAnyStringMatcher
    public void shouldSerializeMethodCallsUsingAnyStringMatcher() throws Exception {
        IMethods mock = mock(IMethods.class, withSettings().serializable());
        List<?> value = Collections.emptyList();
        when(mock.objectArgMethod(anyString())).thenReturn(value);

        
        ByteArrayOutputStream serialized = serializeMock(mock);

        
        IMethods readObject = deserializeMock(serialized, IMethods.class);
        assertEquals(value, readObject.objectArgMethod(""));
    }

// org.mockitousage.basicapi.MocksSerializationTest::shouldVerifyCalledNTimesForSerializedMock
    public void shouldVerifyCalledNTimesForSerializedMock() throws Exception {
        IMethods mock = mock(IMethods.class, withSettings().serializable());
        List<?> value = Collections.emptyList();
        when(mock.objectArgMethod(anyString())).thenReturn(value);
        mock.objectArgMethod("");

        
        ByteArrayOutputStream serialized = serializeMock(mock);

        
        IMethods readObject = deserializeMock(serialized, IMethods.class);
        verify(readObject, times(1)).objectArgMethod("");
    }

// org.mockitousage.basicapi.MocksSerializationTest::shouldVerifyEvenIfSomeMethodsCalledAfterSerialization
    public void shouldVerifyEvenIfSomeMethodsCalledAfterSerialization() throws Exception {
        
        IMethods mock = mock(IMethods.class, withSettings().serializable());

        
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
        
        IMethods mock = mock(IMethods.class, withSettings().serializable());

        
        when(mock.simpleMethod(1)).thenReturn("foo");
        ByteArrayOutputStream serialized = serializeMock(mock);
        IMethods readObject = deserializeMock(serialized, IMethods.class);
        when(readObject.simpleMethod(2)).thenReturn("bar");

        
        assertEquals("foo", readObject.simpleMethod(1));
        assertEquals("bar", readObject.simpleMethod(2));
    }

// org.mockitousage.basicapi.MocksSerializationTest::shouldVerifyCallOrderForSerializedMock
    public void shouldVerifyCallOrderForSerializedMock() throws Exception {
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

// org.mockitousage.basicapi.MocksSerializationTest::shouldRememberInteractionsForSerializedMock
    public void shouldRememberInteractionsForSerializedMock() throws Exception {
        IMethods mock = mock(IMethods.class, withSettings().serializable());
        List<?> value = Collections.emptyList();
        when(mock.objectArgMethod(anyString())).thenReturn(value);
        mock.objectArgMethod("happened");

        
        ByteArrayOutputStream serialized = serializeMock(mock);

        
        IMethods readObject = deserializeMock(serialized, IMethods.class);
        verify(readObject, never()).objectArgMethod("never happened");
    }

// org.mockitousage.basicapi.MocksSerializationTest::shouldSerializeWithStubbingCallback
    public void shouldSerializeWithStubbingCallback() throws Exception {

        
        IMethods mock = mock(IMethods.class, withSettings().serializable());
        CustomAnswersMustImplementSerializableForSerializationToWork answer = 
            new CustomAnswersMustImplementSerializableForSerializationToWork();
        answer.string = "return value";
        when(mock.objectArgMethod(anyString())).thenAnswer(answer);

        
        ByteArrayOutputStream serialized = serializeMock(mock);

        
        IMethods readObject = deserializeMock(serialized, IMethods.class);
        assertEquals(answer.string, readObject.objectArgMethod(""));
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

// org.mockitousage.basicapi.MocksSerializationTest::shouldSerializeAlreadySerializableClass
    public void shouldSerializeAlreadySerializableClass() throws Exception {
        
        AlreadySerializable mock = mock(AlreadySerializable.class, withSettings().serializable());
        when(mock.toString()).thenReturn("foo");

        
        mock = serializeAndBack(mock);

        
        assertEquals("foo", mock.toString());
    }

// org.mockitousage.basicapi.MocksSerializationTest::shouldBeSerializeAndHaveExtraInterfaces
    public void shouldBeSerializeAndHaveExtraInterfaces() throws Exception {
        
        IMethods mock = mock(IMethods.class, withSettings().serializable().extraInterfaces(List.class));
        IMethods mockTwo = mock(IMethods.class, withSettings().extraInterfaces(List.class).serializable());

        
        serializeAndBack((List) mock);
        serializeAndBack((List) mockTwo);
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

// org.mockitousage.debugging.PrintingInvocationsDetectsUnusedStubTest::shouldDetectUnusedStubbingWhenPrinting
    public void shouldDetectUnusedStubbingWhenPrinting() throws Exception {
        
        given(mock.giveMeSomeString("different arg")).willReturn("foo");
        mock.giveMeSomeString("arg");

        
        String log = Mockito.debug().printInvocations(mock, mockTwo);

        
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

// org.mockitousage.junitrunner.JUnit45RunnerTest::shouldInitMocksUsingRunner
    public void shouldInitMocksUsingRunner() {
        list.add("test");
        verify(list).add("test");
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

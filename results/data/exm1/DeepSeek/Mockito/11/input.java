// buggy code
    public boolean equals(Object o) {
            return method.equals(o);
    }

    public int hashCode() {
        return 1;
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

// org.mockito.MockingDetailsTest::testGetInvocations
	public void testGetInvocations() {
		List<String> methodsInvoked = new ArrayList<String>() {{
			add("add");
			add("remove");
			add("clear");
		}};
		
		List<String> mockedList = (List<String>) mock(List.class);
		
		mockedList.add("one");
		mockedList.remove(0);
		mockedList.clear();
		
		MockingDetails mockingDetails = new MockitoCore().mockingDetails(mockedList);
		Collection<Invocation> invocations = mockingDetails.getInvocations();
		
		assertNotNull(invocations);
		assertEquals(invocations.size(),3);
		for (Invocation method : invocations) {
			assertTrue(methodsInvoked.contains(method.getMethod().getName()));
			if (method.getMethod().getName().equals("add")) {
				assertEquals(method.getArguments().length,1);
				assertEquals(method.getArguments()[0],"one");
			}
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

// org.mockito.internal.AllInvocationsFinderTest::shouldGetAllInvocationsInOrder
    public void shouldGetAllInvocationsInOrder() throws Exception {
        mockOne.simpleMethod(100);
        mockTwo.simpleMethod(200);
        mockOne.simpleMethod(300);
        
        List<Invocation> invocations = finder.find(asList(mockOne, mockTwo));
        
        assertEquals(3, invocations.size());
        assertArgumentEquals(100, invocations.get(0));
        assertArgumentEquals(200, invocations.get(1));
        assertArgumentEquals(300, invocations.get(2));
    }

// org.mockito.internal.AllInvocationsFinderTest::shouldNotCountDuplicatedInteractions
    public void shouldNotCountDuplicatedInteractions() throws Exception {
        mockOne.simpleMethod(100);

        List<Invocation> invocations = finder.find(asList(mockOne, mockOne, mockOne));

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

// org.mockito.internal.configuration.ReadingConfigurationFromClasspathTest::shouldReadConfigurationClassFromClassPath
    public void shouldReadConfigurationClassFromClassPath() {
        ConfigurationAccess.getConfig().overrideDefaultAnswer(new Answer<Object>() {
            public Object answer(InvocationOnMock invocation) {
                return "foo";
            }});

        IMethods mock = mock(IMethods.class);
        assertEquals("foo", mock.simpleMethod());
    }

// org.mockito.internal.configuration.ReadingConfigurationFromClasspathTest::readerToLinesEmptyString
    public void readerToLinesEmptyString() throws IOException {
        assertEquals(Collections.emptyList(), ClassPathLoader.readerToLines(new StringReader("")));
    }

// org.mockito.internal.configuration.ReadingConfigurationFromClasspathTest::readerToLinesNoLineBreaks
    public void readerToLinesNoLineBreaks() throws IOException {
        assertEquals(Arrays.asList("a"), ClassPathLoader.readerToLines(new StringReader("a")));
    }

// org.mockito.internal.configuration.ReadingConfigurationFromClasspathTest::readerToLinesWithLineBreaks
    public void readerToLinesWithLineBreaks() throws IOException {
        assertEquals(Arrays.asList("a", "b", "c"),
                ClassPathLoader.readerToLines(new StringReader("a\nb\nc")));
    }

// org.mockito.internal.configuration.ReadingConfigurationFromClasspathTest::readerToLinesWithEmptyLines
    public void readerToLinesWithEmptyLines() throws IOException {
        assertEquals(Arrays.asList("a", "", "c"),
                ClassPathLoader.readerToLines(new StringReader("a\n\nc")));
    }

// org.mockito.internal.configuration.ReadingConfigurationFromClasspathTest::stripCommentsAndWhitespaceEmptyInput
    public void stripCommentsAndWhitespaceEmptyInput() throws IOException {
        assertEquals("", ClassPathLoader.stripCommentAndWhitespace(""));
    }

// org.mockito.internal.configuration.ReadingConfigurationFromClasspathTest::stripCommentsAndWhitespaceWhitespaceInput
    public void stripCommentsAndWhitespaceWhitespaceInput() throws IOException {
        assertEquals("", ClassPathLoader.stripCommentAndWhitespace(" "));
    }

// org.mockito.internal.configuration.ReadingConfigurationFromClasspathTest::stripCommentsAndWhitespaceCommentInInput
    public void stripCommentsAndWhitespaceCommentInInput() throws IOException {
        assertEquals("a", ClassPathLoader.stripCommentAndWhitespace("a#b"));
    }

// org.mockito.internal.configuration.ReadingConfigurationFromClasspathTest::stripCommentsAndWhitespaceMultipleHashes
    public void stripCommentsAndWhitespaceMultipleHashes() throws IOException {
        assertEquals("a", ClassPathLoader.stripCommentAndWhitespace("a#b#c"));
    }

// org.mockito.internal.configuration.ReadingConfigurationFromClasspathTest::stripCommentsAndWhitespaceWithWhitespaceAndComments
    public void stripCommentsAndWhitespaceWithWhitespaceAndComments() throws IOException {
        assertEquals("a", ClassPathLoader.stripCommentAndWhitespace(" a #b"));
    }

// org.mockito.internal.configuration.injection.ConstructorInjectionTest::should_do_the_trick_of_instantiating
    public void should_do_the_trick_of_instantiating() throws Exception {
        given(resolver.resolveTypeInstances(Matchers.<Class<?>[]>anyVararg())).willReturn(new Object[] { observer });

        boolean result = underTest.process(field("whatever"), this, newSetOf(observer));

        assertTrue(result);
        assertNotNull(whatever);
    }

// org.mockito.internal.creation.DelegatingMethodTest::equals_should_return_false_when_not_equal
    public void equals_should_return_false_when_not_equal() throws Exception {
        DelegatingMethod notEqual = new DelegatingMethod(otherMethod);
        assertFalse(delegatingMethod.equals(notEqual));
    }

// org.mockito.internal.creation.DelegatingMethodTest::equals_should_return_true_when_equal
    public void equals_should_return_true_when_equal() throws Exception {
        DelegatingMethod equal = new DelegatingMethod(someMethod);
        assertTrue(delegatingMethod.equals(equal));
    }

// org.mockito.internal.creation.DelegatingMethodTest::equals_should_return_true_when_self
    public void equals_should_return_true_when_self() throws Exception {
        assertTrue(delegatingMethod.equals(delegatingMethod));
    }

// org.mockito.internal.creation.DelegatingMethodTest::equals_should_return_false_when_not_equal_to_method
    public void equals_should_return_false_when_not_equal_to_method() throws Exception {
        assertFalse(delegatingMethod.equals(otherMethod));
    }

// org.mockito.internal.creation.DelegatingMethodTest::equals_should_return_true_when_equal_to_method
    public void equals_should_return_true_when_equal_to_method() throws Exception {
        assertTrue(delegatingMethod.equals(someMethod));
    }

// org.mockito.internal.creation.MethodInterceptorFilterTest::shouldBeSerializable
    public void shouldBeSerializable() throws Exception {
        new ObjectOutputStream(new ByteArrayOutputStream()).writeObject(new MethodInterceptorFilter(null, null));
    }

// org.mockito.internal.creation.MethodInterceptorFilterTest::shouldProvideOwnImplementationOfHashCode
    public void shouldProvideOwnImplementationOfHashCode() throws Throwable {
        
        Object ret = filter.intercept(new MethodsImpl(), MethodsImpl.class.getMethod("hashCode"), new Object[0], null);

        
        assertTrue((Integer) ret != 0);
        Mockito.verify(handler, never()).handle(any(InvocationImpl.class));
    }

// org.mockito.internal.creation.MethodInterceptorFilterTest::shouldProvideOwnImplementationOfEquals
    public void shouldProvideOwnImplementationOfEquals() throws Throwable {
        
        MethodsImpl proxy = new MethodsImpl();
        Object ret = filter.intercept(proxy, MethodsImpl.class.getMethod("equals", Object.class), new Object[] {proxy}, null);

        
        assertTrue((Boolean) ret);
        Mockito.verify(handler, never()).handle(any(InvocationImpl.class));
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

// org.mockito.internal.debugging.LoggingListenerTest::shouldLogUnusedStub
    public void shouldLogUnusedStub() {
        
        LoggingListener listener = new LoggingListener(false, logger);

        
        listener.foundUnusedStub(new InvocationBuilder().toInvocation());

        
        verify(logger).log(notNull());
    }

// org.mockito.internal.debugging.LoggingListenerTest::shouldLogUnstubbed
    public void shouldLogUnstubbed() {
        
        LoggingListener listener = new LoggingListener(true, logger);

        
        listener.foundUnstubbed(new InvocationBuilder().toInvocationMatcher());

        
        verify(logger).log(notNull());
    }

// org.mockito.internal.debugging.LoggingListenerTest::shouldNotLogUnstubbed
    public void shouldNotLogUnstubbed() {
        
        LoggingListener listener = new LoggingListener(false, logger);

        
        listener.foundUnstubbed(new InvocationBuilder().toInvocationMatcher());

        
        verify(logger, never()).log(notNull());
    }

// org.mockito.internal.debugging.LoggingListenerTest::shouldLogDifferentArgs
    public void shouldLogDifferentArgs() {
        
        LoggingListener listener = new LoggingListener(true, logger);

        
        listener.foundStubCalledWithDifferentArgs(new InvocationBuilder().toInvocation(), new InvocationBuilder().toInvocationMatcher());

        
        verify(logger).log(notNull());
    }

// org.mockito.internal.debugging.WarningsFinderTest::shouldPrintUnusedStub
    public void shouldPrintUnusedStub() {
        
        Invocation unusedStub = new InvocationBuilder().simpleMethod().toInvocation();

        
        WarningsFinder finder = new WarningsFinder(asList(unusedStub), Arrays.<InvocationMatcher>asList());
        finder.find(listener);

        
        verify(listener, only()).foundUnusedStub(unusedStub);
    }

// org.mockito.internal.debugging.WarningsFinderTest::shouldPrintUnstubbedInvocation
    public void shouldPrintUnstubbedInvocation() {
        
        InvocationMatcher unstubbedInvocation = new InvocationBuilder().differentMethod().toInvocationMatcher();

        
        WarningsFinder finder = new WarningsFinder(Arrays.<Invocation>asList(), Arrays.<InvocationMatcher>asList(unstubbedInvocation));
        finder.find(listener);

        
        verify(listener, only()).foundUnstubbed(unstubbedInvocation);
    }

// org.mockito.internal.debugging.WarningsFinderTest::shouldPrintStubWasUsedWithDifferentArgs
    public void shouldPrintStubWasUsedWithDifferentArgs() {
        
        Invocation stub = new InvocationBuilder().arg("foo").mock(mock).toInvocation();
        InvocationMatcher wrongArg = new InvocationBuilder().arg("bar").mock(mock).toInvocationMatcher();

        
        WarningsFinder finder = new WarningsFinder(Arrays.<Invocation> asList(stub), Arrays.<InvocationMatcher> asList(wrongArg));
        finder.find(listener);

        
        verify(listener, only()).foundStubCalledWithDifferentArgs(stub, wrongArg);
    }

// org.mockito.internal.debugging.WarningsPrinterImplTest::shouldUseFinderCorrectly
    public void shouldUseFinderCorrectly() {
        
        WarningsPrinterImpl printer = new WarningsPrinterImpl(false, finder);

        
        printer.print(logger);

        
        ArgumentCaptor<LoggingListener> arg = ArgumentCaptor.forClass(LoggingListener.class);
        verify(finder).find(arg.capture());
        assertEquals(logger, arg.getValue().getLogger());
        assertEquals(false, arg.getValue().isWarnAboutUnstubbed());
    }

// org.mockito.internal.debugging.WarningsPrinterImplTest::shouldPassCorrectWarningFlag
    public void shouldPassCorrectWarningFlag() {
        
        WarningsPrinterImpl printer = new WarningsPrinterImpl(true, finder);

        
        printer.print(logger);

        
        ArgumentCaptor<LoggingListener> arg = ArgumentCaptor.forClass(LoggingListener.class);
        verify(finder).find(arg.capture());
        assertEquals(true, arg.getValue().isWarnAboutUnstubbed());
    }

// org.mockito.internal.debugging.WarningsPrinterImplTest::shouldPrintToString
    public void shouldPrintToString() {
        
        WarningsPrinterImpl printer = spy(new WarningsPrinterImpl(true, finder));

        
        String out = printer.print();

        
        verify(printer).print((MockitoLogger) notNull());
        assertNotNull(out);
    }

// org.mockito.internal.handler.InvocationNotifierHandlerTest::should_notify_all_listeners_when_calling_delegate_handler
    public void should_notify_all_listeners_when_calling_delegate_handler() throws Throwable {
        
        given(mockHandler.handle(invocation)).willReturn("returned value");

        
        notifier.handle(invocation);

        
        verify(listener1).reportInvocation(new NotifiedMethodInvocationReport(invocation, "returned value"));
        verify(listener2).reportInvocation(new NotifiedMethodInvocationReport(invocation, "returned value"));
    }

// org.mockito.internal.handler.InvocationNotifierHandlerTest::should_notify_all_listeners_when_called_delegate_handler_returns_ex
    public void should_notify_all_listeners_when_called_delegate_handler_returns_ex() throws Throwable {
        
        Exception computedException = new Exception("computed");
        given(mockHandler.handle(invocation)).willReturn(computedException);

        
        notifier.handle(invocation);

        
        verify(listener1).reportInvocation(new NotifiedMethodInvocationReport(invocation, (Object) computedException));
        verify(listener2).reportInvocation(new NotifiedMethodInvocationReport(invocation, (Object) computedException));
    }

// org.mockito.internal.handler.InvocationNotifierHandlerTest::should_notify_all_listeners_when_called_delegate_handler_throws_exception_and_rethrow_it
    public void should_notify_all_listeners_when_called_delegate_handler_throws_exception_and_rethrow_it() throws Throwable {
        
        ParseException parseException = new ParseException("", 0);
        given(mockHandler.handle(invocation)).willThrow(parseException);

        
        try {
            notifier.handle(invocation);
            fail();
        } finally {
            
            verify(listener1).reportInvocation(new NotifiedMethodInvocationReport(invocation, parseException));
            verify(listener2).reportInvocation(new NotifiedMethodInvocationReport(invocation, parseException));
        }
    }

// org.mockito.internal.handler.InvocationNotifierHandlerTest::should_report_listener_exception
    public void should_report_listener_exception() throws Throwable {
        willThrow(new NullPointerException()).given(customListener).reportInvocation(any(MethodInvocationReport.class));

        try {
            notifier.handle(invocation);
            fail();
        } catch (MockitoException me) {
            assertThat(me.getMessage())
                    .contains("invocation listener")
                    .contains("CustomListener")
                    .contains("threw an exception")
                    .contains("NullPointerException");
        }
    }

// org.mockito.internal.handler.InvocationNotifierHandlerTest::should_delegate_all_MockHandlerInterface_to_the_parameterized_MockHandler
    public void should_delegate_all_MockHandlerInterface_to_the_parameterized_MockHandler() throws Exception {
        notifier.getInvocationContainer();
        notifier.getMockSettings();
        notifier.voidMethodStubbable(mock(IMethods.class));
        notifier.setAnswersForStubbing(new ArrayList<Answer>());

        verify(mockHandler).getInvocationContainer();
        verify(mockHandler).getMockSettings();
        verify(mockHandler).voidMethodStubbable(any());
        verify(mockHandler).setAnswersForStubbing(anyList());
    }

// org.mockito.internal.handler.MockHandlerFactoryTest::handle_result_must_not_be_null_for_primitives
    public void handle_result_must_not_be_null_for_primitives() throws Throwable {
        
        MockCreationSettings settings = (MockCreationSettings) new MockSettingsImpl().defaultAnswer(new Returns(null));
        InternalMockHandler handler = new MockHandlerFactory().create(settings);

        mock.intReturningMethod();
        Invocation invocation = super.getLastInvocation();

        
        Object result = handler.handle(invocation);

        
        assertNotNull(result);
        assertEquals(0, result);
    }

// org.mockito.internal.handler.MockHandlerFactoryTest::valid_handle_result_is_permitted
    public void valid_handle_result_is_permitted() throws Throwable {
        
        MockCreationSettings settings = (MockCreationSettings) new MockSettingsImpl().defaultAnswer(new Returns(123));
        InternalMockHandler handler = new MockHandlerFactory().create(settings);

        mock.intReturningMethod();
        Invocation invocation = super.getLastInvocation();

        
        Object result = handler.handle(invocation);

        
        assertEquals(123, result);
    }

// org.mockito.internal.handler.MockHandlerImplTest::shouldRemoveVerificationModeEvenWhenInvalidMatchers
	public void shouldRemoveVerificationModeEvenWhenInvalidMatchers() throws Throwable {
		
		Invocation invocation = new InvocationBuilder().toInvocation();
		@SuppressWarnings("rawtypes")
        MockHandlerImpl<?> handler = new MockHandlerImpl(new MockSettingsImpl());
		handler.mockingProgress.verificationStarted(VerificationModeFactory.atLeastOnce());
		handler.matchersBinder = new MatchersBinder() {
			public InvocationMatcher bindMatchers(ArgumentMatcherStorage argumentMatcherStorage, Invocation invocation) {
				throw new InvalidUseOfMatchersException();
			}
		};

		try {
			
			handler.handle(invocation);

			
			fail();
		} catch (InvalidUseOfMatchersException e) {
		}

		assertNull(handler.mockingProgress.pullVerificationMode());
	}

// org.mockito.internal.handler.MockHandlerImplTest::shouldThrowMockitoExceptionWhenInvocationHandlerThrowsAnything
	public void shouldThrowMockitoExceptionWhenInvocationHandlerThrowsAnything() throws Throwable {
		
		InvocationListener throwingListener = mock(InvocationListener.class);
		doThrow(new Throwable()).when(throwingListener).reportInvocation(any(MethodInvocationReport.class));
		MockHandlerImpl<?> handler = createCorrectlyStubbedHandler(throwingListener);

		
		handler.handle(invocation);
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

// org.mockito.internal.invocation.InvocationMatcherTest::should_be_a_citizen_of_hashes
    public void should_be_a_citizen_of_hashes() throws Exception {
        Invocation invocation = new InvocationBuilder().toInvocation();
        Invocation invocationTwo = new InvocationBuilder().args("blah").toInvocation();

        Map map = new HashMap();
        map.put(new InvocationMatcher(invocation), "one");
        map.put(new InvocationMatcher(invocationTwo), "two");

        assertEquals(2, map.size());
    }

// org.mockito.internal.invocation.InvocationMatcherTest::should_not_equal_if_number_of_arguments_differ
    public void should_not_equal_if_number_of_arguments_differ() throws Exception {
        InvocationMatcher withOneArg = new InvocationMatcher(new InvocationBuilder().args("test").toInvocation());
        InvocationMatcher withTwoArgs = new InvocationMatcher(new InvocationBuilder().args("test", 100).toInvocation());

        assertFalse(withOneArg.equals(null));
        assertFalse(withOneArg.equals(withTwoArgs));
    }

// org.mockito.internal.invocation.InvocationMatcherTest::should_to_string_with_matchers
    public void should_to_string_with_matchers() throws Exception {
        Matcher m = NotNull.NOT_NULL;
        InvocationMatcher notNull = new InvocationMatcher(new InvocationBuilder().toInvocation(), asList(m));
        Matcher mTwo = new Equals('x');
        InvocationMatcher equals = new InvocationMatcher(new InvocationBuilder().toInvocation(), asList(mTwo));

        assertContains("simpleMethod(notNull())", notNull.toString());
        assertContains("simpleMethod('x')", equals.toString());
    }

// org.mockito.internal.invocation.InvocationMatcherTest::should_know_if_is_similar_to
    public void should_know_if_is_similar_to() throws Exception {
        Invocation same = new InvocationBuilder().mock(mock).simpleMethod().toInvocation();
        assertTrue(simpleMethod.hasSimilarMethod(same));

        Invocation different = new InvocationBuilder().mock(mock).differentMethod().toInvocation();
        assertFalse(simpleMethod.hasSimilarMethod(different));
    }

// org.mockito.internal.invocation.InvocationMatcherTest::should_not_be_similar_to_verified_invocation
    public void should_not_be_similar_to_verified_invocation() throws Exception {
        Invocation verified = new InvocationBuilder().simpleMethod().verified().toInvocation();
        assertFalse(simpleMethod.hasSimilarMethod(verified));
    }

// org.mockito.internal.invocation.InvocationMatcherTest::should_not_be_similar_if_mocks_are_different
    public void should_not_be_similar_if_mocks_are_different() throws Exception {
        Invocation onDifferentMock = new InvocationBuilder().simpleMethod().mock("different mock").toInvocation();
        assertFalse(simpleMethod.hasSimilarMethod(onDifferentMock));
    }

// org.mockito.internal.invocation.InvocationMatcherTest::should_not_be_similar_if_is_overloaded_but_used_with_the_same_arg
    public void should_not_be_similar_if_is_overloaded_but_used_with_the_same_arg() throws Exception {
        Method method = IMethods.class.getMethod("simpleMethod", String.class);
        Method overloadedMethod = IMethods.class.getMethod("simpleMethod", Object.class);

        String sameArg = "test";

        InvocationMatcher invocation = new InvocationBuilder().method(method).arg(sameArg).toInvocationMatcher();
        Invocation overloadedInvocation = new InvocationBuilder().method(overloadedMethod).arg(sameArg).toInvocation();

        assertFalse(invocation.hasSimilarMethod(overloadedInvocation));
    }

// org.mockito.internal.invocation.InvocationMatcherTest::should_be_similar_if_is_overloaded_but_used_with_different_arg
    public void should_be_similar_if_is_overloaded_but_used_with_different_arg() throws Exception {
        Method method = IMethods.class.getMethod("simpleMethod", String.class);
        Method overloadedMethod = IMethods.class.getMethod("simpleMethod", Object.class);

        InvocationMatcher invocation = new InvocationBuilder().mock(mock).method(method).arg("foo").toInvocationMatcher();
        Invocation overloadedInvocation = new InvocationBuilder().mock(mock).method(overloadedMethod).arg("bar").toInvocation();

        assertTrue(invocation.hasSimilarMethod(overloadedInvocation));
    }

// org.mockito.internal.invocation.InvocationMatcherTest::should_capture_arguments_from_invocation
    public void should_capture_arguments_from_invocation() throws Exception {
        
        Invocation invocation = new InvocationBuilder().args("1", 100).toInvocation();
        CapturingMatcher capturingMatcher = new CapturingMatcher();
        InvocationMatcher invocationMatcher = new InvocationMatcher(invocation, (List) asList(new Equals("1"), capturingMatcher));

        
        invocationMatcher.captureArgumentsFrom(invocation);

        
        assertEquals(1, capturingMatcher.getAllValues().size());
        assertEquals(100, capturingMatcher.getLastValue());
    }

// org.mockito.internal.invocation.InvocationMatcherTest::should_match_varargs_using_any_varargs
    public void should_match_varargs_using_any_varargs() throws Exception {
        
        mock.varargs("1", "2");
        Invocation invocation = getLastInvocation();
        InvocationMatcher invocationMatcher = new InvocationMatcher(invocation, (List) asList(AnyVararg.ANY_VARARG));

        
        boolean match = invocationMatcher.matches(invocation);

        
        assertTrue(match);
    }

// org.mockito.internal.invocation.InvocationMatcherTest::should_capture_varargs_as_vararg
    public void should_capture_varargs_as_vararg() throws Exception {
        
        mock.mixedVarargs(1, "a", "b");
        Invocation invocation = getLastInvocation();
        CapturingMatcher m = new CapturingMatcher();
        InvocationMatcher invocationMatcher = new InvocationMatcher(invocation, (List) asList(new Equals(1), new LocalizedMatcher(m)));

        
        invocationMatcher.captureArgumentsFrom(invocation);

        
        Assertions.assertThat(m.getAllValues()).containsExactly("a", "b");
    }

// org.mockito.internal.invocation.InvocationMatcherTest::should_capture_arguments_when_args_count_does_NOT_match
    public void should_capture_arguments_when_args_count_does_NOT_match() throws Exception {
        
        mock.varargs();
        Invocation invocation = getLastInvocation();

        
        InvocationMatcher invocationMatcher = new InvocationMatcher(invocation, (List) asList(new LocalizedMatcher(AnyVararg.ANY_VARARG)));

        
        invocationMatcher.captureArgumentsFrom(invocation);
    }

// org.mockito.internal.invocation.InvocationMatcherTest::should_create_from_invocations
    public void should_create_from_invocations() throws Exception {
        
        Invocation i = new InvocationBuilder().toInvocation();
        
        List<InvocationMatcher> out = InvocationMatcher.createFrom(asList(i));
        
        assertEquals(1, out.size());
        assertEquals(i, out.get(0).getInvocation());
    }

// org.mockito.internal.progress.MockingProgressImplTest::shouldStartVerificationAndPullVerificationMode
    public void shouldStartVerificationAndPullVerificationMode() throws Exception {
        assertNull(mockingProgress.pullVerificationMode());
        
        VerificationMode mode = VerificationModeFactory.times(19);
        
        mockingProgress.verificationStarted(mode);
        
        assertSame(mode, mockingProgress.pullVerificationMode());
        
        assertNull(mockingProgress.pullVerificationMode());
    }

// org.mockito.internal.progress.MockingProgressImplTest::shouldCheckIfVerificationWasFinished
    public void shouldCheckIfVerificationWasFinished() throws Exception {
        mockingProgress.verificationStarted(VerificationModeFactory.atLeastOnce());
        try {
            mockingProgress.verificationStarted(VerificationModeFactory.atLeastOnce());
            fail();
        } catch (MockitoException e) {}
    }

// org.mockito.internal.progress.MockingProgressImplTest::shouldNotifyListenerWhenMockingStarted
    public void shouldNotifyListenerWhenMockingStarted() throws Exception {
        
        MockingStartedListener listener = mock(MockingStartedListener.class);
        mockingProgress.setListener(listener);

        
        mockingProgress.mockingStarted("foo", List.class);

        
        verify(listener).mockingStarted(eq("foo"), eq(List.class));
    }

// org.mockito.internal.progress.MockingProgressImplTest::shouldNotifyListenerSafely
    public void shouldNotifyListenerSafely() throws Exception {
        
        mockingProgress.setListener(null);

        
        mockingProgress.mockingStarted(null, null);
    }

// org.mockito.internal.stubbing.InvocationContainerImplTest::should_be_thread_safe
    public void should_be_thread_safe() throws Throwable {
        doShouldBeThreadSafe(container);
    }

// org.mockito.internal.stubbing.InvocationContainerImplTest::should_be_thread_safe_stub_only
    public void should_be_thread_safe_stub_only() throws Throwable {
        doShouldBeThreadSafe(containerStubOnly);
    }

// org.mockito.internal.stubbing.InvocationContainerImplTest::should_return_invoked_mock
    public void should_return_invoked_mock() throws Exception {
        container.setInvocationForPotentialStubbing(new InvocationMatcher(invocation));

        assertEquals(invocation.getMock(), container.invokedMock());
    }

// org.mockito.internal.stubbing.InvocationContainerImplTest::should_return_invoked_mock_stub_only
    public void should_return_invoked_mock_stub_only() throws Exception {
        containerStubOnly.setInvocationForPotentialStubbing(new InvocationMatcher(invocation));

        assertEquals(invocation.getMock(), containerStubOnly.invokedMock());
    }

// org.mockito.internal.stubbing.InvocationContainerImplTest::should_tell_if_has_invocation_for_potential_stubbing
    public void should_tell_if_has_invocation_for_potential_stubbing() throws Exception {
        container.setInvocationForPotentialStubbing(new InvocationBuilder().toInvocationMatcher());
        assertTrue(container.hasInvocationForPotentialStubbing());

        container.addAnswer(new ReturnsEmptyValues());
        assertFalse(container.hasInvocationForPotentialStubbing());
    }

// org.mockito.internal.stubbing.InvocationContainerImplTest::should_tell_if_has_invocation_for_potential_stubbing_stub_only
    public void should_tell_if_has_invocation_for_potential_stubbing_stub_only() throws Exception {
        containerStubOnly.setInvocationForPotentialStubbing(new InvocationBuilder().toInvocationMatcher());
        assertTrue(containerStubOnly.hasInvocationForPotentialStubbing());

        containerStubOnly.addAnswer(new ReturnsEmptyValues());
        assertFalse(containerStubOnly.hasInvocationForPotentialStubbing());
    }

// org.mockito.internal.stubbing.answers.AnswersValidatorTest::should_validate_null_throwable
    public void should_validate_null_throwable() throws Throwable {
        try {
            validator.validate(new ThrowsException(null), new InvocationBuilder().toInvocation());
            fail();
        } catch (MockitoException e) {}
    }

// org.mockito.internal.stubbing.answers.AnswersValidatorTest::should_pass_proper_checked_exception
    public void should_pass_proper_checked_exception() throws Throwable {
        validator.validate(new ThrowsException(new CharacterCodingException()), invocation);
    }

// org.mockito.internal.stubbing.answers.AnswersValidatorTest::should_fail_invalid_checked_exception
    public void should_fail_invalid_checked_exception() throws Throwable {
        validator.validate(new ThrowsException(new IOException()), invocation);
    }

// org.mockito.internal.stubbing.answers.AnswersValidatorTest::should_pass_RuntimeExceptions
    public void should_pass_RuntimeExceptions() throws Throwable {
        validator.validate(new ThrowsException(new Error()), invocation);
        validator.validate(new ThrowsException(new RuntimeException()), invocation);
    }

// org.mockito.internal.stubbing.answers.AnswersValidatorTest::should_fail_when_return_Value_is_set_for_void_method
    public void should_fail_when_return_Value_is_set_for_void_method() throws Throwable {
        validator.validate(new Returns("one"), new InvocationBuilder().method("voidMethod").toInvocation());
    }

// org.mockito.internal.stubbing.answers.AnswersValidatorTest::should_fail_when_non_void_method_does_nothing
    public void should_fail_when_non_void_method_does_nothing() throws Throwable {
        validator.validate(new DoesNothing(), new InvocationBuilder().simpleMethod().toInvocation());
    }

// org.mockito.internal.stubbing.answers.AnswersValidatorTest::should_allow_void_return_for_void_method
    public void should_allow_void_return_for_void_method() throws Throwable {
        validator.validate(new DoesNothing(), new InvocationBuilder().method("voidMethod").toInvocation());
    }

// org.mockito.internal.stubbing.answers.AnswersValidatorTest::should_allow_correct_type_of_return_value
    public void should_allow_correct_type_of_return_value() throws Throwable {
        validator.validate(new Returns("one"), new InvocationBuilder().simpleMethod().toInvocation());
        validator.validate(new Returns(false), new InvocationBuilder().method("booleanReturningMethod").toInvocation());
        validator.validate(new Returns(Boolean.TRUE), new InvocationBuilder().method("booleanObjectReturningMethod").toInvocation());
        validator.validate(new Returns(1), new InvocationBuilder().method("integerReturningMethod").toInvocation());
        validator.validate(new Returns(1L), new InvocationBuilder().method("longReturningMethod").toInvocation());
        validator.validate(new Returns(1L), new InvocationBuilder().method("longObjectReturningMethod").toInvocation());
        validator.validate(new Returns(null), new InvocationBuilder().method("objectReturningMethodNoArgs").toInvocation());
        validator.validate(new Returns(1), new InvocationBuilder().method("objectReturningMethodNoArgs").toInvocation());
    }

// org.mockito.internal.stubbing.answers.AnswersValidatorTest::should_fail_on_return_type_mismatch
    public void should_fail_on_return_type_mismatch() throws Throwable {
        validator.validate(new Returns("String"), new InvocationBuilder().method("booleanReturningMethod").toInvocation());
    }

// org.mockito.internal.stubbing.answers.AnswersValidatorTest::should_fail_on_wrong_primitive
    public void should_fail_on_wrong_primitive() throws Throwable {
        validator.validate(new Returns(1), new InvocationBuilder().method("doubleReturningMethod").toInvocation());
    }

// org.mockito.internal.stubbing.answers.AnswersValidatorTest::should_fail_on_null_with_primitive
    public void should_fail_on_null_with_primitive() throws Throwable {
        validator.validate(new Returns(null), new InvocationBuilder().method("booleanReturningMethod").toInvocation());
    }

// org.mockito.internal.stubbing.answers.AnswersValidatorTest::should_fail_when_calling_real_method_on_interface
    public void should_fail_when_calling_real_method_on_interface() throws Throwable {
        
        Invocation invocationOnInterface = new InvocationBuilder().method("simpleMethod").toInvocation();
        try {
            
            validator.validate(new CallsRealMethods(), invocationOnInterface);
            
            fail();
        } catch (MockitoException e) {}
    }

// org.mockito.internal.stubbing.answers.AnswersValidatorTest::should_be_OK_when_calling_real_method_on_concrete_class
    public void should_be_OK_when_calling_real_method_on_concrete_class() throws Throwable {
        
        ArrayList mock = mock(ArrayList.class);
        mock.clear();
        Invocation invocationOnClass = new MockitoCore().getLastInvocation();
        
        validator.validate(new CallsRealMethods(), invocationOnClass);
        
    }

// org.mockito.internal.stubbing.answers.AnswersValidatorTest::should_allow_possible_argument_types
    public void should_allow_possible_argument_types() throws Exception {
        validator.validate(
                new ReturnsArgumentAt(0),
                new InvocationBuilder().method("intArgumentReturningInt").argTypes(int.class).arg(1000).toInvocation()
        );
        validator.validate(
                new ReturnsArgumentAt(0),
                new InvocationBuilder().method("toString").argTypes(String.class).arg("whatever").toInvocation()
        );
        validator.validate(
                new ReturnsArgumentAt(2),
                new InvocationBuilder().method("varargsObject")
                                       .argTypes(int.class, Object[].class)
                                       .args(1000, "Object", "Object")
                                       .toInvocation()
        );
        validator.validate(
                new ReturnsArgumentAt(1),
                new InvocationBuilder().method("threeArgumentMethod")
                                       .argTypes(int.class, Object.class, String.class)
                                       .args(1000, "Object", "String")
                                       .toInvocation()
        );
    }

// org.mockito.internal.stubbing.answers.AnswersValidatorTest::should_fail_if_index_is_not_in_range_for_one_arg_invocation
    public void should_fail_if_index_is_not_in_range_for_one_arg_invocation() throws Throwable {
        try {
            validator.validate(new ReturnsArgumentAt(30), new InvocationBuilder().method("oneArg").arg("A").toInvocation());
            fail();
        } catch (MockitoException e) {
            assertThat(e.getMessage())
                    .containsIgnoringCase("invalid argument index")
                    .containsIgnoringCase("iMethods.oneArg")
                    .containsIgnoringCase("[0] String")
                    .containsIgnoringCase("position")
                    .contains("30");
        }
    }

// org.mockito.internal.stubbing.answers.AnswersValidatorTest::should_fail_if_index_is_not_in_range_for_example_with_no_arg_invocation
    public void should_fail_if_index_is_not_in_range_for_example_with_no_arg_invocation() throws Throwable {
        try {
            validator.validate(
                    new ReturnsArgumentAt(ReturnsArgumentAt.LAST_ARGUMENT),
                    new InvocationBuilder().simpleMethod().toInvocation()
            );
            fail();
        } catch (MockitoException e) {
            assertThat(e.getMessage())
                    .containsIgnoringCase("invalid argument index")
                    .containsIgnoringCase("iMethods.simpleMethod")
                    .containsIgnoringCase("no arguments")
                    .containsIgnoringCase("last parameter wanted");
        }
    }

// org.mockito.internal.stubbing.answers.AnswersValidatorTest::should_fail_if_argument_type_of_signature_is_incompatible_with_return_type
    public void should_fail_if_argument_type_of_signature_is_incompatible_with_return_type() throws Throwable {
        try {
            validator.validate(
                    new ReturnsArgumentAt(2),
                    new InvocationBuilder().method("varargsReturningString")
                                           .argTypes(Object[].class)
                                           .args("anyString", new Object(), "anyString")
                                           .toInvocation()
            );
            fail();
        } catch (WrongTypeOfReturnValue e) {
            assertThat(e.getMessage())
                    .containsIgnoringCase("argument of type")
                    .containsIgnoringCase("Object")
                    .containsIgnoringCase("varargsReturningString")
                    .containsIgnoringCase("should return")
                    .containsIgnoringCase("String")
                    .containsIgnoringCase("possible argument indexes");
        }
    }

// org.mockito.internal.stubbing.defaultanswers.ReturnsEmptyValuesTest::should_return_empty_collections_or_null_for_non_collections
    @Test public void should_return_empty_collections_or_null_for_non_collections() {
        assertTrue(((Collection) values.returnValueFor(Collection.class)).isEmpty());

        assertTrue(((Set) values.returnValueFor(Set.class)).isEmpty());
        assertTrue(((SortedSet) values.returnValueFor(SortedSet.class)).isEmpty());
        assertTrue(((HashSet) values.returnValueFor(HashSet.class)).isEmpty());
        assertTrue(((TreeSet) values.returnValueFor(TreeSet.class)).isEmpty());
        assertTrue(((LinkedHashSet) values.returnValueFor(LinkedHashSet.class)).isEmpty());

        assertTrue(((List) values.returnValueFor(List.class)).isEmpty());
        assertTrue(((ArrayList) values.returnValueFor(ArrayList.class)).isEmpty());
        assertTrue(((LinkedList) values.returnValueFor(LinkedList.class)).isEmpty());

        assertTrue(((Map) values.returnValueFor(Map.class)).isEmpty());
        assertTrue(((SortedMap) values.returnValueFor(SortedMap.class)).isEmpty());
        assertTrue(((HashMap) values.returnValueFor(HashMap.class)).isEmpty());
        assertTrue(((TreeMap) values.returnValueFor(TreeMap.class)).isEmpty());
        assertTrue(((LinkedHashMap) values.returnValueFor(LinkedHashMap.class)).isEmpty());

        assertNull(values.returnValueFor(String.class));
    }

// org.mockito.internal.stubbing.defaultanswers.ReturnsEmptyValuesTest::should_return_primitive
    @Test public void should_return_primitive() {
        assertEquals(false, values.returnValueFor(Boolean.TYPE));
        assertEquals((char) 0, values.returnValueFor(Character.TYPE));
        assertEquals((byte) 0, values.returnValueFor(Byte.TYPE));
        assertEquals((short) 0, values.returnValueFor(Short.TYPE));
        assertEquals(0, values.returnValueFor(Integer.TYPE));
        assertEquals(0L, values.returnValueFor(Long.TYPE));
        assertEquals(0F, values.returnValueFor(Float.TYPE));
        assertEquals(0D, values.returnValueFor(Double.TYPE));
    }

// org.mockito.internal.stubbing.defaultanswers.ReturnsEmptyValuesTest::should_return_non_zero_for_compareTo_method
    @Test public void should_return_non_zero_for_compareTo_method() {
        
        Date d = mock(Date.class);
        d.compareTo(new Date());
        Invocation compareTo = this.getLastInvocation();

        
        Object result = values.answer(compareTo);
        
        
        assertTrue(result != (Object) 0);
    }

// org.mockito.internal.stubbing.defaultanswers.ReturnsEmptyValuesTest::should_return_zero_if_mock_is_compared_to_itself
    @Test public void should_return_zero_if_mock_is_compared_to_itself() {
        
        Date d = mock(Date.class);
        d.compareTo(d);
        Invocation compareTo = this.getLastInvocation();

        
        Object result = values.answer(compareTo);

        
        assertEquals(0, result);
    }

// org.mockito.internal.stubbing.defaultanswers.ReturnsGenericDeepStubsTest::generic_deep_mock_frenzy__look_at_these_chained_calls
    public void generic_deep_mock_frenzy__look_at_these_chained_calls() throws Exception {
        GenericsNest<?> mock = mock(GenericsNest.class, RETURNS_DEEP_STUBS);

        Set<? extends Map.Entry<? extends Cloneable, Set<Number>>> entries = mock.entrySet();
        Iterator<? extends Map.Entry<? extends Cloneable,Set<Number>>> entriesIterator = mock.entrySet().iterator();
        Map.Entry<? extends Cloneable, Set<Number>> nextEntry = mock.entrySet().iterator().next();

        Cloneable cloneableKey = mock.entrySet().iterator().next().getKey();
        Comparable<?> comparableKey = mock.entrySet().iterator().next().getKey();

        Set<Number> value = mock.entrySet().iterator().next().getValue();
        Iterator<Number> numbersIterator = mock.entrySet().iterator().next().getValue().iterator();
        Number number = mock.entrySet().iterator().next().getValue().iterator().next();
    }

// org.mockito.internal.stubbing.defaultanswers.ReturnsGenericDeepStubsTest::can_create_mock_from_multiple_type_variable_bounds_when_return_type_of_parameterized_method_is_a_parameterizedtype_that_is_referencing_a_typevar_on_class
    public void can_create_mock_from_multiple_type_variable_bounds_when_return_type_of_parameterized_method_is_a_parameterizedtype_that_is_referencing_a_typevar_on_class() throws Exception {
        GenericsNest<?> mock = mock(GenericsNest.class, RETURNS_DEEP_STUBS);

        Cloneable cloneable_bound_that_is_declared_on_typevar_K_in_the_class_which_is_referenced_by_typevar_O_declared_on_the_method =
                mock.paramTypeWithTypeParams().get(0);
        Comparable<?> comparable_bound_that_is_declared_on_typevar_K_in_the_class_which_is_referenced_by_typevar_O_declared_on_the_method =
                mock.paramTypeWithTypeParams().get(0);
    }

// org.mockito.internal.stubbing.defaultanswers.ReturnsGenericDeepStubsTest::can_create_mock_from_multiple_type_variable_bounds_when_method_return_type_is_referencing_a_typevar_on_class
    public void can_create_mock_from_multiple_type_variable_bounds_when_method_return_type_is_referencing_a_typevar_on_class() throws Exception {
        GenericsNest<?> mock = mock(GenericsNest.class, RETURNS_DEEP_STUBS);

        Cloneable cloneable_bound_of_typevar_K = mock.returningK();
        Comparable<?> comparable_bound_of_typevar_K = mock.returningK();
    }

// org.mockito.internal.stubbing.defaultanswers.ReturnsGenericDeepStubsTest::can_create_mock_from_multiple_type_variable_bounds_when_return_type_of_parameterized_method_is_a_typevar_that_is_referencing_a_typevar_on_class
    public void can_create_mock_from_multiple_type_variable_bounds_when_return_type_of_parameterized_method_is_a_typevar_that_is_referencing_a_typevar_on_class() throws Exception {
        GenericsNest<?> mock = mock(GenericsNest.class, RETURNS_DEEP_STUBS);

        Cloneable cloneable_bound_of_typevar_K_referenced_by_typevar_O = (Cloneable) mock.typeVarWithTypeParams();
        Comparable<?> comparable_bound_of_typevar_K_referenced_by_typevar_O = (Comparable) mock.typeVarWithTypeParams();
    }

// org.mockito.internal.stubbing.defaultanswers.ReturnsGenericDeepStubsTest::can_create_mock_from_return_types_declared_with_a_bounded_wildcard
    public void can_create_mock_from_return_types_declared_with_a_bounded_wildcard() throws Exception {
        GenericsNest<?> mock = mock(GenericsNest.class, RETURNS_DEEP_STUBS);

        List<? super Integer> objects = mock.returningWildcard();
        Number type_that_is_the_upper_bound_of_the_wildcard = (Number) mock.returningWildcard().get(45);
        type_that_is_the_upper_bound_of_the_wildcard.floatValue();
    }

// org.mockito.internal.stubbing.defaultanswers.ReturnsGenericDeepStubsTest::can_still_work_with_raw_type_in_the_return_type
    public void can_still_work_with_raw_type_in_the_return_type() throws Exception {
        GenericsNest<?> mock = mock(GenericsNest.class, RETURNS_DEEP_STUBS);

        Number the_raw_type_that_should_be_returned = mock.returnsNormalType();
        the_raw_type_that_should_be_returned.floatValue();
    }

// org.mockito.internal.stubbing.defaultanswers.ReturnsGenericDeepStubsTest::will_return_default_value_on_non_mockable_nested_generic
    public void will_return_default_value_on_non_mockable_nested_generic() throws Exception {
        GenericsNest<?> genericsNest = mock(GenericsNest.class, RETURNS_DEEP_STUBS);
        ListOfInteger listOfInteger = mock(ListOfInteger.class, RETURNS_DEEP_STUBS);

        assertThat(genericsNest.returningNonMockableNestedGeneric().keySet().iterator().next()).isNull();
        assertThat(listOfInteger.get(25)).isEqualTo(0);
    }

// org.mockito.internal.stubbing.defaultanswers.ReturnsGenericDeepStubsTest::as_expected_fail_with_a_CCE_on_callsite_when_erasure_takes_place_for_example___StringBuilder_is_subject_to_erasure
    public void as_expected_fail_with_a_CCE_on_callsite_when_erasure_takes_place_for_example___StringBuilder_is_subject_to_erasure() throws Exception {
        GenericsNest<?> mock = mock(GenericsNest.class, RETURNS_DEEP_STUBS);

        
        StringBuilder stringBuilder_assignment_that_should_throw_a_CCE =
                mock.twoTypeParams(new StringBuilder()).append(2).append(3);
    }

// org.mockito.internal.stubbing.defaultanswers.ReturnsMocksTest::should_return_mock_value_for_interface
    public void should_return_mock_value_for_interface() throws Exception {
        Object interfaceMock = values.returnValueFor(FooInterface.class);
        assertTrue(new MockUtil().isMock(interfaceMock));
    }

// org.mockito.internal.stubbing.defaultanswers.ReturnsMocksTest::should_return_mock_value_for_class
    public void should_return_mock_value_for_class() throws Exception {
        Object classMock = values.returnValueFor(BarClass.class);
        assertTrue(new MockUtil().isMock(classMock));
    }

// org.mockito.internal.stubbing.defaultanswers.ReturnsMocksTest::should_return_null_for_final_class
    public void should_return_null_for_final_class() throws Exception {
        assertNull(values.returnValueFor(Baz.class));
    }

// org.mockito.internal.stubbing.defaultanswers.ReturnsMocksTest::should_return_the_usual_default_values_for_primitives
    public void should_return_the_usual_default_values_for_primitives() throws Throwable {
        ReturnsMocks answer = new ReturnsMocks();
        assertEquals(false, answer.answer(invocationOf(HasPrimitiveMethods.class, "booleanMethod")));
        assertEquals((char) 0, answer.answer(invocationOf(HasPrimitiveMethods.class, "charMethod")));
        assertEquals((byte) 0, answer.answer(invocationOf(HasPrimitiveMethods.class, "byteMethod")));
        assertEquals((short) 0, answer.answer(invocationOf(HasPrimitiveMethods.class, "shortMethod")));
        assertEquals(0, answer.answer(invocationOf(HasPrimitiveMethods.class, "intMethod")));
        assertEquals(0L, answer.answer(invocationOf(HasPrimitiveMethods.class, "longMethod")));
        assertEquals(0f, answer.answer(invocationOf(HasPrimitiveMethods.class, "floatMethod")));
        assertEquals(0d, answer.answer(invocationOf(HasPrimitiveMethods.class, "doubleMethod")));
    }

// org.mockito.internal.stubbing.defaultanswers.ReturnsMocksTest::should_return_empty_array
    public void should_return_empty_array() throws Throwable {
        String[] ret = (String[]) values.answer(invocationOf(StringMethods.class, "stringArrayMethod"));
        
        assertTrue(ret.getClass().isArray());
        assertTrue(ret.length == 0);
    }

// org.mockito.internal.stubbing.defaultanswers.ReturnsMocksTest::should_return_empty_string
    public void should_return_empty_string() throws Throwable {
        assertEquals("", values.answer(invocationOf(StringMethods.class, "stringMethod")));
    }

// org.mockito.internal.stubbing.defaultanswers.ReturnsSmartNullsTest::should_return_the_usual_default_values_for_primitives
    public void should_return_the_usual_default_values_for_primitives() throws Throwable {
        Answer<Object> answer = new ReturnsSmartNulls();
        assertEquals(false  ,   answer.answer(invocationOf(HasPrimitiveMethods.class, "booleanMethod")));
        assertEquals((char) 0,  answer.answer(invocationOf(HasPrimitiveMethods.class, "charMethod")));
        assertEquals((byte) 0,  answer.answer(invocationOf(HasPrimitiveMethods.class, "byteMethod")));
        assertEquals((short) 0, answer.answer(invocationOf(HasPrimitiveMethods.class, "shortMethod")));
        assertEquals(0,         answer.answer(invocationOf(HasPrimitiveMethods.class, "intMethod")));
        assertEquals(0L,        answer.answer(invocationOf(HasPrimitiveMethods.class, "longMethod")));
        assertEquals(0f,        answer.answer(invocationOf(HasPrimitiveMethods.class, "floatMethod")));
        assertEquals(0d,        answer.answer(invocationOf(HasPrimitiveMethods.class, "doubleMethod")));
    }

// org.mockito.internal.stubbing.defaultanswers.ReturnsSmartNullsTest::should_return_an_object_that_fails_on_any_method_invocation_for_non_primitives
    public void should_return_an_object_that_fails_on_any_method_invocation_for_non_primitives() throws Throwable {
        Answer<Object> answer = new ReturnsSmartNulls();

        Foo smartNull = (Foo) answer.answer(invocationOf(Foo.class, "get"));

        try {
            smartNull.get();
            fail();
        } catch (SmartNullPointerException expected) {}
    }

// org.mockito.internal.stubbing.defaultanswers.ReturnsSmartNullsTest::should_return_an_object_that_allows_object_methods
    public void should_return_an_object_that_allows_object_methods() throws Throwable {
        Answer<Object> answer = new ReturnsSmartNulls();

        Foo smartNull = (Foo) answer.answer(invocationOf(Foo.class, "get"));

        assertContains("SmartNull returned by", smartNull + "");
        assertContains("foo.get()", smartNull + "");
    }

// org.mockito.internal.stubbing.defaultanswers.ReturnsSmartNullsTest::should_print_the_parameters_when_calling_a_method_with_args
    public void should_print_the_parameters_when_calling_a_method_with_args() throws Throwable {
    	Answer<Object> answer = new ReturnsSmartNulls();

    	Foo smartNull = (Foo) answer.answer(invocationOf(Foo.class, "withArgs", "oompa", "lumpa"));

        assertContains("foo.withArgs", smartNull + "");
        assertContains("oompa", smartNull + "");
        assertContains("lumpa", smartNull + "");
    }

// org.mockito.internal.stubbing.defaultanswers.ReturnsSmartNullsTest::should_print_the_parameters_on_SmartNullPointerException_message
	public void should_print_the_parameters_on_SmartNullPointerException_message() throws Throwable {
    	Answer<Object> answer = new ReturnsSmartNulls();

        Foo smartNull = (Foo) answer.answer(invocationOf(Foo.class, "withArgs", "oompa", "lumpa"));

        try {
            smartNull.get();
            fail();
        } catch (SmartNullPointerException e) {
        	assertContains("oompa", e.getMessage());
        	assertContains("lumpa", e.getMessage());
        }
	}

// org.mockito.internal.util.ObjectMethodsGuruTest::shouldKnowToStringMethod
    public void shouldKnowToStringMethod() throws Exception {
        assertFalse(guru.isToString(Object.class.getMethod("equals", Object.class)));
        assertFalse(guru.isToString(IMethods.class.getMethod("toString", String.class)));
        assertTrue(guru.isToString(IMethods.class.getMethod("toString")));
    }

// org.mockito.internal.util.ObjectMethodsGuruTest::shouldKnowEqualsMethod
    public void shouldKnowEqualsMethod() throws Exception {
        assertFalse(guru.isEqualsMethod(IMethods.class.getMethod("equals", String.class)));
        assertFalse(guru.isEqualsMethod(IMethods.class.getMethod("equals")));
        assertFalse(guru.isEqualsMethod(Object.class.getMethod("toString")));
        assertTrue(guru.isEqualsMethod(Object.class.getMethod("equals", Object.class)));
    }

// org.mockito.internal.util.ObjectMethodsGuruTest::shouldKnowHashCodeMethod
    public void shouldKnowHashCodeMethod() throws Exception {
        assertFalse(guru.isHashCodeMethod(IMethods.class.getMethod("toString")));
        assertFalse(guru.isHashCodeMethod(IMethods.class.getMethod("hashCode", String.class)));
        assertTrue(guru.isHashCodeMethod(Object.class.getDeclaredMethod("hashCode")));
    }

// org.mockito.internal.util.ObjectMethodsGuruTest::shouldKnowCompareToMethod
    public void shouldKnowCompareToMethod() throws Exception {
        assertFalse(guru.isCompareToMethod(Date.class.getMethod("toString")));
        assertFalse(guru.isCompareToMethod(HasCompare.class.getMethod("foo", HasCompare.class)));
        assertFalse(guru.isCompareToMethod(HasCompare.class.getMethod("compareTo", HasCompare.class, String.class)));
        assertFalse(guru.isCompareToMethod(HasCompare.class.getMethod("compareTo", String.class)));
        assertFalse(guru.isCompareToMethod(HasCompareToButDoesNotImplementComparable.class.getDeclaredMethod("compareTo", HasCompareToButDoesNotImplementComparable.class)));

        assertTrue(guru.isCompareToMethod(HasCompare.class.getMethod("compareTo", HasCompare.class)));
    }

// org.mockito.internal.util.reflection.FieldInitializerTest::should_keep_same_instance_if_field_initialized
    public void should_keep_same_instance_if_field_initialized() throws Exception {
        final StaticClass backupInstance = alreadyInstantiated;
        FieldInitializer fieldInitializer = new FieldInitializer(this, field("alreadyInstantiated"));
        FieldInitializationReport report = fieldInitializer.initialize();

        assertSame(backupInstance, report.fieldInstance());
        assertFalse(report.fieldWasInitialized());
        assertFalse(report.fieldWasInitializedUsingContructorArgs());
    }

// org.mockito.internal.util.reflection.FieldInitializerTest::should_instantiate_field_when_type_has_no_constructor
    public void should_instantiate_field_when_type_has_no_constructor() throws Exception {
        FieldInitializer fieldInitializer = new FieldInitializer(this, field("noConstructor"));
        FieldInitializationReport report = fieldInitializer.initialize();

        assertNotNull(report.fieldInstance());
        assertTrue(report.fieldWasInitialized());
        assertFalse(report.fieldWasInitializedUsingContructorArgs());
    }

// org.mockito.internal.util.reflection.FieldInitializerTest::should_instantiate_field_with_default_constructor
    public void should_instantiate_field_with_default_constructor() throws Exception {
        FieldInitializer fieldInitializer = new FieldInitializer(this, field("defaultConstructor"));
        FieldInitializationReport report = fieldInitializer.initialize();

        assertNotNull(report.fieldInstance());
        assertTrue(report.fieldWasInitialized());
        assertFalse(report.fieldWasInitializedUsingContructorArgs());
    }

// org.mockito.internal.util.reflection.FieldInitializerTest::should_instantiate_field_with_private_default_constructor
    public void should_instantiate_field_with_private_default_constructor() throws Exception {
        FieldInitializer fieldInitializer = new FieldInitializer(this, field("privateDefaultConstructor"));
        FieldInitializationReport report = fieldInitializer.initialize();

        assertNotNull(report.fieldInstance());
        assertTrue(report.fieldWasInitialized());
        assertFalse(report.fieldWasInitializedUsingContructorArgs());
    }

// org.mockito.internal.util.reflection.FieldInitializerTest::should_fail_to_instantiate_field_if_no_default_constructor
    public void should_fail_to_instantiate_field_if_no_default_constructor() throws Exception {
        FieldInitializer fieldInitializer = new FieldInitializer(this, field("noDefaultConstructor"));
        fieldInitializer.initialize();
    }

// org.mockito.internal.util.reflection.FieldInitializerTest::should_fail_to_instantiate_field_if_default_constructor_throws_exception
    public void should_fail_to_instantiate_field_if_default_constructor_throws_exception() throws Exception {
        FieldInitializer fieldInitializer = new FieldInitializer(this, field("throwingExDefaultConstructor"));
        try {
            fieldInitializer.initialize();
            fail();
        } catch (MockitoException e) {
            InvocationTargetException ite = (InvocationTargetException) e.getCause();
            assertTrue(ite.getTargetException() instanceof NullPointerException);
            assertEquals("business logic failed", ite.getTargetException().getMessage());
        }
    }

// org.mockito.internal.util.reflection.FieldInitializerTest::should_fail_for_abstract_field
    public void should_fail_for_abstract_field() throws Exception {
        new FieldInitializer(this, field("abstractType"));
    }

// org.mockito.internal.util.reflection.FieldInitializerTest::should_not_fail_if_abstract_field_is_instantiated
    public void should_not_fail_if_abstract_field_is_instantiated() throws Exception {
        new FieldInitializer(this, field("instantiatedAbstractType"));
    }

// org.mockito.internal.util.reflection.FieldInitializerTest::should_fail_for_interface_field
    public void should_fail_for_interface_field() throws Exception {
        new FieldInitializer(this, field("interfaceType"));
    }

// org.mockito.internal.util.reflection.FieldInitializerTest::should_not_fail_if_interface_field_is_instantiated
    public void should_not_fail_if_interface_field_is_instantiated() throws Exception {
        new FieldInitializer(this, field("instantiatedInterfaceType"));
    }

// org.mockito.internal.util.reflection.FieldInitializerTest::should_fail_for_local_type_field
    public void should_fail_for_local_type_field() throws Exception {
        
        class LocalType { }

        class TheTestWithLocalType {
            @InjectMocks LocalType field;
        }

        TheTestWithLocalType testWithLocalType = new TheTestWithLocalType();

        
        new FieldInitializer(testWithLocalType, testWithLocalType.getClass().getDeclaredField("field"));
    }

// org.mockito.internal.util.reflection.FieldInitializerTest::should_not_fail_if_local_type_field_is_instantiated
    public void should_not_fail_if_local_type_field_is_instantiated() throws Exception {
        
        class LocalType { }

        class TheTestWithLocalType {
            @InjectMocks LocalType field = new LocalType();
        }

        TheTestWithLocalType testWithLocalType = new TheTestWithLocalType();

        
        new FieldInitializer(testWithLocalType, testWithLocalType.getClass().getDeclaredField("field"));
    }

// org.mockito.internal.util.reflection.FieldInitializerTest::should_fail_for_inner_class_field
    public void should_fail_for_inner_class_field() throws Exception {
        new FieldInitializer(this, field("innerClassType"));
    }

// org.mockito.internal.util.reflection.FieldInitializerTest::should_not_fail_if_inner_class_field_is_instantiated
    public void should_not_fail_if_inner_class_field_is_instantiated() throws Exception {
        new FieldInitializer(this, field("instantiatedInnerClassType"));
    }

// org.mockito.internal.util.reflection.FieldInitializerTest::can_instantiate_class_with_parameterized_constructor
    public void can_instantiate_class_with_parameterized_constructor() throws Exception {
        ConstructorArgumentResolver resolver = given(mock(ConstructorArgumentResolver.class).resolveTypeInstances(any(Class[].class)))
                        .willReturn(new Object[]{null}).getMock();

        new FieldInitializer(this, field("noDefaultConstructor"), resolver).initialize();

        assertNotNull(noDefaultConstructor);
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

// org.mockito.internal.util.reflection.ParameterizedConstructorInstantiatorTest::should_be_created_with_an_argument_resolver
    public void should_be_created_with_an_argument_resolver() throws Exception {
        new ParameterizedConstructorInstantiator(this, field("whateverForNow"), resolver);
    }

// org.mockito.internal.util.reflection.ParameterizedConstructorInstantiatorTest::should_fail_if_no_parameterized_constructor_found___excluding_inner_and_others_kind_of_types
    public void should_fail_if_no_parameterized_constructor_found___excluding_inner_and_others_kind_of_types() throws Exception {
        try {
            new ParameterizedConstructorInstantiator(this, field("withNoArgConstructor"), resolver).instantiate();
            fail();
        } catch (MockitoException me) {
            assertThat(me.getMessage()).contains("no parameterized constructor").contains("withNoArgConstructor").contains("NoArgConstructor");
        }
    }

// org.mockito.internal.util.reflection.ParameterizedConstructorInstantiatorTest::should_instantiate_type_if_resolver_provide_matching_types
    public void should_instantiate_type_if_resolver_provide_matching_types() throws Exception {
        Observer observer = mock(Observer.class);
        Map map = mock(Map.class);
        given(resolver.resolveTypeInstances(Matchers.<Class<?>[]>anyVararg())).willReturn(new Object[]{ observer, map });

        new ParameterizedConstructorInstantiator(this, field("withMultipleConstructor"), resolver).instantiate();

        assertNotNull(withMultipleConstructor);
        assertNotNull(withMultipleConstructor.observer);
        assertNotNull(withMultipleConstructor.map);
    }

// org.mockito.internal.util.reflection.ParameterizedConstructorInstantiatorTest::should_fail_if_an_argument_instance_type_do_not_match_wanted_type
    public void should_fail_if_an_argument_instance_type_do_not_match_wanted_type() throws Exception {
        Observer observer = mock(Observer.class);
        Set wrongArg = mock(Set.class);
        given(resolver.resolveTypeInstances(Matchers.<Class<?>[]>anyVararg())).willReturn(new Object[]{ observer, wrongArg });

        try {
            new ParameterizedConstructorInstantiator(this, field("withMultipleConstructor"), resolver).instantiate();
            fail();
        } catch (MockitoException e) {
            assertThat(e.getMessage()).contains("argResolver").contains("incorrect types");
        }
    }

// org.mockito.internal.util.reflection.ParameterizedConstructorInstantiatorTest::should_report_failure_if_constructor_throws_exception
    public void should_report_failure_if_constructor_throws_exception() throws Exception {
        given(resolver.resolveTypeInstances(Matchers.<Class<?>[]>anyVararg())).willReturn(new Object[]{ null });

        try {
            new ParameterizedConstructorInstantiator(this, field("withThrowingConstructor"), resolver).instantiate();
            fail();
        } catch (MockitoException e) {
            assertThat(e.getMessage()).contains("constructor").contains("raised an exception");
        }
    }

// org.mockito.internal.util.reflection.ParameterizedConstructorInstantiatorTest::should_instantiate_type_with_vararg_constructor
    public void should_instantiate_type_with_vararg_constructor() throws Exception {
        Observer[] vararg = new Observer[] {  };
        given(resolver.resolveTypeInstances(Matchers.<Class<?>[]>anyVararg())).willReturn(new Object[]{ "", vararg});

        new ParameterizedConstructorInstantiator(this, field("withVarargConstructor"), resolver).instantiate();

        assertNotNull(withVarargConstructor);
    }

// org.mockito.internal.verification.DefaultRegisteredInvocationsTest::should_not_return_to_string_method
    public void should_not_return_to_string_method() throws Exception {
        Invocation toString = new InvocationBuilder().method("toString").toInvocation();
        Invocation simpleMethod = new InvocationBuilder().simpleMethod().toInvocation();
        
        invocations.add(toString);
        invocations.add(simpleMethod);
        
        assertTrue(invocations.getAll().contains(simpleMethod));
        assertFalse(invocations.getAll().contains(toString));
    }

// org.mockito.internal.verification.NoMoreInteractionsTest::shouldVerifyInOrder
    public void shouldVerifyInOrder() {
        
        NoMoreInteractions n = new NoMoreInteractions();
        Invocation i = new InvocationBuilder().toInvocation();
        assertFalse(context.isVerified(i));
        
        try {
            
            n.verifyInOrder(new VerificationDataInOrderImpl(context, asList(i), null));
            
            fail();
        } catch(VerificationInOrderFailure e) {}
    }

// org.mockito.internal.verification.NoMoreInteractionsTest::shouldVerifyInOrderAndPass
    public void shouldVerifyInOrderAndPass() {
        
        NoMoreInteractions n = new NoMoreInteractions();
        Invocation i = new InvocationBuilder().toInvocation();
        context.markVerified(i);
        assertTrue(context.isVerified(i));
        
        
        n.verifyInOrder(new VerificationDataInOrderImpl(context, asList(i), null));
        
    }

// org.mockito.internal.verification.NoMoreInteractionsTest::shouldVerifyInOrderMultipleInvoctions
    public void shouldVerifyInOrderMultipleInvoctions() {
        
        NoMoreInteractions n = new NoMoreInteractions();
        Invocation i = new InvocationBuilder().seq(1).toInvocation();
        Invocation i2 = new InvocationBuilder().seq(2).toInvocation();

        
        context.markVerified(i2);

        
        n.verifyInOrder(new VerificationDataInOrderImpl(context, asList(i, i2), null));
    }

// org.mockito.internal.verification.NoMoreInteractionsTest::shouldVerifyInOrderMultipleInvoctionsAndThrow
    public void shouldVerifyInOrderMultipleInvoctionsAndThrow() {
        
        NoMoreInteractions n = new NoMoreInteractions();
        Invocation i = new InvocationBuilder().seq(1).toInvocation();
        Invocation i2 = new InvocationBuilder().seq(2).toInvocation();

        try {
            
            n.verifyInOrder(new VerificationDataInOrderImpl(context, asList(i, i2), null));
            fail();
        } catch (VerificationInOrderFailure e) {}
    }

// org.mockito.internal.verification.NoMoreInteractionsTest::noMoreInteractionsExceptionMessageShouldDescribeMock
    public void noMoreInteractionsExceptionMessageShouldDescribeMock() {
        
        NoMoreInteractions n = new NoMoreInteractions();
        String mock = "a mock";
        InvocationMatcher i = new InvocationBuilder().mock(mock).toInvocationMatcher();

        InvocationContainerImpl invocations =
            new InvocationContainerImpl(new ThreadSafeMockingProgress(), new MockSettingsImpl());
        invocations.setInvocationForPotentialStubbing(i);

        try {
            
            n.verify(new VerificationDataImpl(invocations, null));
            
            fail();
        } catch (NoInteractionsWanted e) {
            Assertions.assertThat(e.toString()).contains(mock.toString());
        }
    }

// org.mockito.internal.verification.NoMoreInteractionsTest::noMoreInteractionsInOrderExceptionMessageShouldDescribeMock
    public void noMoreInteractionsInOrderExceptionMessageShouldDescribeMock() {
        
        NoMoreInteractions n = new NoMoreInteractions();
        String mock = "a mock";
        Invocation i = new InvocationBuilder().mock(mock).toInvocation();

        try {
            
            n.verifyInOrder(new VerificationDataInOrderImpl(context, asList(i), null));
            
            fail();
        } catch (VerificationInOrderFailure e) {
            Assertions.assertThat(e.toString()).contains(mock.toString());
        }
    }

// org.mockito.internal.verification.SmartPrinterTest::shouldPrintBothInMultilinesWhenFirstIsMulti
    public void shouldPrintBothInMultilinesWhenFirstIsMulti() {
        
        SmartPrinter printer = new SmartPrinter(multi, shortie.getInvocation());
        
        
        assertContains("\n", printer.getWanted().toString());
        assertContains("\n", printer.getActual().toString());
    }

// org.mockito.internal.verification.SmartPrinterTest::shouldPrintBothInMultilinesWhenSecondIsMulti
    public void shouldPrintBothInMultilinesWhenSecondIsMulti() {
        
        SmartPrinter printer = new SmartPrinter(shortie, multi.getInvocation());
        
        
        assertContains("\n", printer.getWanted().toString());
        assertContains("\n", printer.getActual().toString());
    }

// org.mockito.internal.verification.SmartPrinterTest::shouldPrintBothInMultilinesWhenBothAreMulti
    public void shouldPrintBothInMultilinesWhenBothAreMulti() {
        
        SmartPrinter printer = new SmartPrinter(multi, multi.getInvocation());
        
        
        assertContains("\n", printer.getWanted().toString());
        assertContains("\n", printer.getActual().toString());
    }

// org.mockito.internal.verification.SmartPrinterTest::shouldPrintBothInSingleLineWhenBothAreShort
    public void shouldPrintBothInSingleLineWhenBothAreShort() {
        
        SmartPrinter printer = new SmartPrinter(shortie, shortie.getInvocation());
        
        
        assertNotContains("\n", printer.getWanted().toString());
        assertNotContains("\n", printer.getActual().toString());
    }

// org.mockito.internal.verification.VerificationDataImplTest::shouldToStringBeNotVerifiable
    public void shouldToStringBeNotVerifiable() throws Exception {
        InvocationMatcher toString = new InvocationBuilder().method("toString").toInvocationMatcher();
        try {
            new VerificationDataImpl(null, toString);
            fail();
        } catch (MockitoException e) {}
    }

// org.mockito.internal.verification.VerificationOverTimeImplTest::should_return_on_success
    public void should_return_on_success() {
        impl.verify(null);
        verify(delegate).verify(null);
    }

// org.mockito.internal.verification.VerificationOverTimeImplTest::should_throw_mockito_assertion_error
    public void should_throw_mockito_assertion_error() {
        MockitoAssertionError toBeThrown = new MockitoAssertionError("message");
        exception.expect(is(toBeThrown));

        doThrow(toBeThrown).when(delegate).verify(null);
        impl.verify(null);
    }

// org.mockito.internal.verification.VerificationOverTimeImplTest::should_deal_with_junit_assertion_error
    public void should_deal_with_junit_assertion_error() {
        ArgumentsAreDifferent toBeThrown = new ArgumentsAreDifferent("message", "wanted", "actual");
        exception.expect(is(toBeThrown));
        exception.expectMessage("message");

        doThrow(toBeThrown).when(delegate).verify(null);
        impl.verify(null);
    }

// org.mockito.internal.verification.VerificationOverTimeImplTest::should_not_wrap_other_exceptions
    public void should_not_wrap_other_exceptions() {
        RuntimeException toBeThrown = new RuntimeException();
        exception.expect(is(toBeThrown));

        doThrow(toBeThrown).when(delegate).verify(null);
        impl.verify(null);
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

// org.mockito.verification.TimeoutTest::should_pass_when_verification_passes
    public void should_pass_when_verification_passes() {
        Timeout t = new Timeout(1, 3, mode);
        
        doNothing().when(mode).verify(data);
        
        t.verify(data);
    }

// org.mockito.verification.TimeoutTest::should_fail_because_verification_fails
    public void should_fail_because_verification_fails() {
        Timeout t = new Timeout(1, 2, mode);
        
        doThrow(error).
        doThrow(error).
        doThrow(error).
        when(mode).verify(data);
        
        try {
            t.verify(data);
            fail();
        } catch (MockitoAssertionError e) {}
    }

// org.mockito.verification.TimeoutTest::should_pass_even_if_first_verification_fails
    public void should_pass_even_if_first_verification_fails() {}

// org.mockito.verification.TimeoutTest::should_try_to_verify_correct_number_of_times
    public void should_try_to_verify_correct_number_of_times() {}

// org.mockito.verification.TimeoutTest::should_create_correctly_configured_timeout
    public void should_create_correctly_configured_timeout() {
        Timeout t = new Timeout(25, 50, mode);
        
        assertTimeoutCorrectlyConfigured(t.atLeastOnce(), Timeout.class, 50, 25, AtLeast.class);
        assertTimeoutCorrectlyConfigured(t.atLeast(5), Timeout.class, 50, 25, AtLeast.class);
        assertTimeoutCorrectlyConfigured(t.times(5), Timeout.class, 50, 25, Times.class);
        assertTimeoutCorrectlyConfigured(t.only(), Timeout.class, 50, 25, Only.class);
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

// org.mockitousage.annotation.AnnotationsTest::shouldInitMocks
    public void shouldInitMocks() throws Exception {
        list.clear();
        map.clear();
        listTwo.clear();

        verify(list).clear();
        verify(map).clear();
        verify(listTwo).clear();
    }

// org.mockitousage.annotation.AnnotationsTest::shouldScreamWhenInitializingMocksForNullClass
    public void shouldScreamWhenInitializingMocksForNullClass() throws Exception {
        try {
            MockitoAnnotations.initMocks(null);
            fail();
        } catch (MockitoException e) {
            assertEquals("testClass cannot be null. For info how to use @Mock annotations see examples in javadoc for MockitoAnnotations class",
                    e.getMessage());
        }
    }

// org.mockitousage.annotation.AnnotationsTest::shouldLookForAnnotatedMocksInSuperClasses
    public void shouldLookForAnnotatedMocksInSuperClasses() throws Exception {
        Sub sub = new Sub();
        MockitoAnnotations.initMocks(sub);

        assertNotNull(sub.getMock());
        assertNotNull(sub.getBaseMock());
        assertNotNull(sub.getSuperBaseMock());
    }

// org.mockitousage.annotation.AnnotationsTest::shouldInitMocksWithGivenSettings
    public void shouldInitMocksWithGivenSettings() throws Exception {
        assertEquals("i have a name", namedAndReturningMocks.toString());
        assertNotNull(namedAndReturningMocks.iMethodsReturningMethod());
       
        assertEquals("returningDefaults", returningDefaults.toString());
        assertEquals(0, returningDefaults.intReturningMethod()); 
        
        assertTrue(hasExtraInterfaces instanceof List);
        
        assertEquals(0, noExtraConfig.intReturningMethod());        
    }

// org.mockitousage.annotation.CaptorAnnotationBasicTest::shouldUseCaptorInOrdinaryWay
    public void shouldUseCaptorInOrdinaryWay() {
        
        createPerson("Wes", "Williams");
        
        
        ArgumentCaptor<Person> captor = ArgumentCaptor.forClass(Person.class);
        verify(peopleRepository).save(captor.capture());
        assertEquals("Wes", captor.getValue().getName());
        assertEquals("Williams", captor.getValue().getSurname());
    }

// org.mockitousage.annotation.CaptorAnnotationBasicTest::shouldUseAnnotatedCaptor
    public void shouldUseAnnotatedCaptor() {
        
        createPerson("Wes", "Williams");
        
        
        verify(peopleRepository).save(captor.capture());
        assertEquals("Wes", captor.getValue().getName());
        assertEquals("Williams", captor.getValue().getSurname());
    }

// org.mockitousage.annotation.CaptorAnnotationBasicTest::shouldUseGenericlessAnnotatedCaptor
    public void shouldUseGenericlessAnnotatedCaptor() {
        
        createPerson("Wes", "Williams");
        
        
        verify(peopleRepository).save((Person) genericLessCaptor.capture());
        assertEquals("Wes", ((Person) genericLessCaptor.getValue()).getName());
        assertEquals("Williams", ((Person) genericLessCaptor.getValue()).getSurname());
    }

// org.mockitousage.annotation.CaptorAnnotationBasicTest::shouldCaptureGenericList
    public void shouldCaptureGenericList() {
        
        List<String> list = new LinkedList<String>();
        mock.listArgMethod(list);
                
        
        verify(mock).listArgMethod(genericListCaptor.capture());
        
        
        assertSame(list, genericListCaptor.getValue());
    }

// org.mockitousage.annotation.CaptorAnnotationTest::testNormalUsage
    public void testNormalUsage() {

        MockitoAnnotations.initMocks(this);

        
        assertNotNull(finalCaptor);
        assertNotNull(genericsCaptor);
        assertNotNull(nonGenericCaptorIsAllowed);
        assertNull(notAMock);

        
        String argForFinalCaptor = "Hello";
        ArrayList<List<String>> argForGenericsCaptor = new ArrayList<List<String>>();

        mockInterface.testMe(argForFinalCaptor, argForGenericsCaptor);

        Mockito.verify(mockInterface).testMe(finalCaptor.capture(), genericsCaptor.capture());

        assertEquals(argForFinalCaptor, finalCaptor.getValue());
        assertEquals(argForGenericsCaptor, genericsCaptor.getValue());

    }

// org.mockitousage.annotation.CaptorAnnotationTest::shouldScreamWhenWrongTypeForCaptor
    public void shouldScreamWhenWrongTypeForCaptor() {
        try {
            MockitoAnnotations.initMocks(new WrongType());
            fail();
        } catch (MockitoException e) {}
    }

// org.mockitousage.annotation.CaptorAnnotationTest::shouldScreamWhenMoreThanOneMockitoAnnotaton
    public void shouldScreamWhenMoreThanOneMockitoAnnotaton() {
        try {
            MockitoAnnotations.initMocks(new ToManyAnnotations());
            fail();
        } catch (MockitoException e) {
            assertContains("missingGenericsField", e.getMessage());
            assertContains("multiple Mockito annotations", e.getMessage());            
        }
    }

// org.mockitousage.annotation.CaptorAnnotationTest::shouldScreamWhenInitializingCaptorsForNullClass
    public void shouldScreamWhenInitializingCaptorsForNullClass() throws Exception {
        try {
            MockitoAnnotations.initMocks(null);
            fail();
        } catch (MockitoException e) {
        }
    }

// org.mockitousage.annotation.CaptorAnnotationTest::shouldLookForAnnotatedCaptorsInSuperClasses
    public void shouldLookForAnnotatedCaptorsInSuperClasses() throws Exception {
        Sub sub = new Sub();
        MockitoAnnotations.initMocks(sub);

        assertNotNull(sub.getCaptor());
        assertNotNull(sub.getBaseCaptor());
        assertNotNull(sub.getSuperBaseCaptor());
    }

// org.mockitousage.annotation.MockInjectionUsingConstructorTest::shouldNotFailWhenNotInitialized
    public void shouldNotFailWhenNotInitialized() {
        assertNotNull(articleManager);
    }

// org.mockitousage.annotation.MockInjectionUsingConstructorTest::innerMockShouldRaiseAnExceptionThatChangesOuterMockBehavior
    public void innerMockShouldRaiseAnExceptionThatChangesOuterMockBehavior() {
        when(calculator.countArticles("new")).thenThrow(new IllegalArgumentException());

        articleManager.updateArticleCounters("new");
    }

// org.mockitousage.annotation.MockInjectionUsingConstructorTest::mockJustWorks
    public void mockJustWorks() {
        articleManager.updateArticleCounters("new");
    }

// org.mockitousage.annotation.MockInjectionUsingConstructorTest::constructor_is_called_for_each_test_in_test_class
    public void constructor_is_called_for_each_test_in_test_class() throws Exception {
        
        JUnitCore jUnitCore = new JUnitCore();
        jUnitCore.addListener(new TextListener(System.out));

        
        jUnitCore.run(junit_test_with_3_tests_methods.class);

        
        assertThat(junit_test_with_3_tests_methods.constructor_instantiation).isEqualTo(3);
    }

// org.mockitousage.annotation.MockInjectionUsingConstructorTest::objects_created_with_constructor_initialization_can_be_spied
    public void objects_created_with_constructor_initialization_can_be_spied() throws Exception {
        assertFalse(mockUtil.isMock(articleManager));
        assertTrue(mockUtil.isMock(spiedArticleManager));
    }

// org.mockitousage.annotation.MockInjectionUsingConstructorTest::should_report_failure_only_when_object_initialization_throws_exception
    public void should_report_failure_only_when_object_initialization_throws_exception() throws Exception {

        try {
            MockitoAnnotations.initMocks(new ATest());
            fail();
        } catch (MockitoException e) {
            assertThat(e.getMessage()).contains("failingConstructor").contains("constructor").contains("threw an exception");
            assertThat(e.getCause()).isInstanceOf(IllegalStateException.class);
        }
    }

// org.mockitousage.annotation.MockInjectionUsingConstructorTest::test_1
        @Test public void test_1() { }

// org.mockitousage.annotation.MockInjectionUsingConstructorTest::test_2
        @Test public void test_2() { }

// org.mockitousage.annotation.MockInjectionUsingConstructorTest::test_3
        @Test public void test_3() { }

// org.mockitousage.annotation.MockInjectionUsingSetterOrPropertyTest::shouldKeepSameInstanceIfFieldInitialized
    public void shouldKeepSameInstanceIfFieldInitialized() {
        assertSame(baseUnderTestingInstance, initializedBase);
    }

// org.mockitousage.annotation.MockInjectionUsingSetterOrPropertyTest::shouldInitializeAnnotatedFieldIfNull
    public void shouldInitializeAnnotatedFieldIfNull() {
        assertNotNull(notInitializedBase);
    }

// org.mockitousage.annotation.MockInjectionUsingSetterOrPropertyTest::shouldIInjectMocksInSpy
    public void shouldIInjectMocksInSpy() {
        assertNotNull(initializedSpy.getAList());
        assertTrue(mockUtil.isMock(initializedSpy));
    }

// org.mockitousage.annotation.MockInjectionUsingSetterOrPropertyTest::shouldInitializeSpyIfNullAndInjectMocks
    public void shouldInitializeSpyIfNullAndInjectMocks() {
        assertNotNull(notInitializedSpy);
        assertNotNull(notInitializedSpy.getAList());
        assertTrue(mockUtil.isMock(notInitializedSpy));
    }

// org.mockitousage.annotation.MockInjectionUsingSetterOrPropertyTest::shouldInjectMocksIfAnnotated
	public void shouldInjectMocksIfAnnotated() {
		MockitoAnnotations.initMocks(this);
		assertSame(list, superUnderTest.getAList());
	}

// org.mockitousage.annotation.MockInjectionUsingSetterOrPropertyTest::shouldNotInjectIfNotAnnotated
	public void shouldNotInjectIfNotAnnotated() {
		MockitoAnnotations.initMocks(this);
		assertNull(superUnderTestWithoutInjection.getAList());
	}

// org.mockitousage.annotation.MockInjectionUsingSetterOrPropertyTest::shouldInjectMocksForClassHierarchyIfAnnotated
	public void shouldInjectMocksForClassHierarchyIfAnnotated() {
		MockitoAnnotations.initMocks(this);
		assertSame(list, baseUnderTest.getAList());
		assertSame(map, baseUnderTest.getAMap());
	}

// org.mockitousage.annotation.MockInjectionUsingSetterOrPropertyTest::shouldInjectMocksByName
	public void shouldInjectMocksByName() {
		MockitoAnnotations.initMocks(this);
		assertSame(histogram1, subUnderTest.getHistogram1());
		assertSame(histogram2, subUnderTest.getHistogram2());
	}

// org.mockitousage.annotation.MockInjectionUsingSetterOrPropertyTest::shouldInjectSpies
	public void shouldInjectSpies() {
		MockitoAnnotations.initMocks(this);
		assertSame(searchTree, otherBaseUnderTest.getSearchTree());
	}

// org.mockitousage.annotation.MockInjectionUsingSetterOrPropertyTest::shouldInstantiateInjectMockFieldIfPossible
    public void shouldInstantiateInjectMockFieldIfPossible() throws Exception {
        assertNotNull(notInitializedBase);
    }

// org.mockitousage.annotation.MockInjectionUsingSetterOrPropertyTest::shouldKeepInstanceOnInjectMockFieldIfPresent
    public void shouldKeepInstanceOnInjectMockFieldIfPresent() throws Exception {
        assertSame(baseUnderTestingInstance, initializedBase);
    }

// org.mockitousage.annotation.MockInjectionUsingSetterOrPropertyTest::shouldReportNicely
    public void shouldReportNicely() throws Exception {
        Object failing = new Object() {
            @InjectMocks ThrowingConstructor failingConstructor;
        };
        try {
            MockitoAnnotations.initMocks(failing);
            fail();
        } catch (MockitoException e) {
            Assertions.assertThat(e.getMessage()).contains("failingConstructor").contains("constructor").contains("threw an exception");
            Assertions.assertThat(e.getCause()).isInstanceOf(RuntimeException.class);
        }
    }

// org.mockitousage.annotation.SpyAnnotationTest::shouldInitSpies
    public void shouldInitSpies() throws Exception {
        doReturn("foo").when(spiedList).get(10);

        assertEquals("foo", spiedList.get(10));
        assertTrue(spiedList.isEmpty());
    }

// org.mockitousage.annotation.SpyAnnotationTest::shouldInitSpyIfNestedStaticClass
    public void shouldInitSpyIfNestedStaticClass() throws Exception {
		assertNotNull(staticTypeWithNoArgConstructor);
		assertNotNull(staticTypeWithoutDefinedConstructor);
    }

// org.mockitousage.annotation.SpyAnnotationTest::shouldFailIfTypeIsAnInterface
    public void shouldFailIfTypeIsAnInterface() throws Exception {
		class FailingSpy {
			@Spy private List spyTypeIsInterface;
		}

        try {
            MockitoAnnotations.initMocks(new FailingSpy());
            fail();
        } catch (Exception e) {
            Assertions.assertThat(e.getMessage()).contains("an interface");
        }
    }

// org.mockitousage.annotation.SpyAnnotationTest::shouldReportWhenNoArgConstructor
    public void shouldReportWhenNoArgConstructor() throws Exception {
		class FailingSpy {
	        @Spy
            NoValidConstructor noValidConstructor;
		}

        try {
            MockitoAnnotations.initMocks(new FailingSpy());
            fail();
        } catch (Exception e) {
            Assertions.assertThat(e.getMessage()).contains("default constructor");
        }
    }

// org.mockitousage.annotation.SpyAnnotationTest::shouldReportWhenConstructorThrows
    public void shouldReportWhenConstructorThrows() throws Exception {
		class FailingSpy {
	        @Spy
            ThrowingConstructor throwingConstructor;
		}

        try {
            MockitoAnnotations.initMocks(new FailingSpy());
            fail();
        } catch (Exception e) {
            Assertions.assertThat(e.getMessage()).contains("raised an exception");
        }
    }

// org.mockitousage.annotation.SpyAnnotationTest::shouldFailIfTypeIsAbstract
    public void shouldFailIfTypeIsAbstract() throws Exception {
		class FailingSpy {
			@Spy private AbstractList spyTypeIsAbstract;
		}

        try {
            MockitoAnnotations.initMocks(new FailingSpy());
            fail();
        } catch (Exception e) {
            Assertions.assertThat(e.getMessage()).contains("abstract class");
        }
    }

// org.mockitousage.annotation.SpyAnnotationTest::shouldFailIfTypeIsInnerClass
    public void shouldFailIfTypeIsInnerClass() throws Exception {
		class FailingSpy {
			@Spy private TheInnerClass spyTypeIsInner;
            class TheInnerClass { }
		}

        try {
            MockitoAnnotations.initMocks(new FailingSpy());
            fail();
        } catch (MockitoException e) {
            Assertions.assertThat(e.getMessage()).contains("inner class");
        }
    }

// org.mockitousage.annotation.SpyAnnotationTest::shouldResetSpies
    public void shouldResetSpies() throws Exception {
        spiedList.get(10); 
    }

// org.mockitousage.basicapi.MockAccessTest::shouldAllowStubbedMockReferenceAccess
    public void shouldAllowStubbedMockReferenceAccess() throws Exception {
        Set expectedMock = mock(Set.class);

        Set returnedMock = when(expectedMock.isEmpty()).thenReturn(false).getMock();

        assertEquals(expectedMock, returnedMock);
    }

// org.mockitousage.basicapi.MockAccessTest::stubbedMockShouldWorkAsUsual
    public void stubbedMockShouldWorkAsUsual() throws Exception {
        Set returnedMock = when(mock(Set.class).isEmpty()).thenReturn(false, true).getMock();

        assertEquals(false, returnedMock.isEmpty());
        assertEquals(true, returnedMock.isEmpty());
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

// org.mockitousage.basicapi.MocksCreationTest::shouldAllowInlineMockCreation
    public void shouldAllowInlineMockCreation() throws Exception {
        when(mock(Set.class).isEmpty()).thenReturn(false);
    }

// org.mockitousage.basicapi.MocksSerializationForAnnotationTest::should_allow_throws_exception_to_be_serializable
    public void should_allow_throws_exception_to_be_serializable() throws Exception {
        
        when(barMock.doSomething()).thenAnswer(new ThrowsException(new RuntimeException()));

        
        serializeAndBack(barMock);
    }

// org.mockitousage.basicapi.MocksSerializationForAnnotationTest::should_allow_mock_to_be_serializable
    public void should_allow_mock_to_be_serializable() throws Exception {
        
        serializeAndBack(imethodsMock);
    }

// org.mockitousage.basicapi.MocksSerializationForAnnotationTest::should_allow_mock_and_boolean_value_to_serializable
    public void should_allow_mock_and_boolean_value_to_serializable() throws Exception {
        
        when(imethodsMock.booleanReturningMethod()).thenReturn(true);

        
        ByteArrayOutputStream serialized = serializeMock(imethodsMock);

        
        IMethods readObject = deserializeMock(serialized, IMethods.class);
        assertTrue(readObject.booleanReturningMethod());
    }

// org.mockitousage.basicapi.MocksSerializationForAnnotationTest::should_allow_mock_and_string_value_to_be_serializable
    public void should_allow_mock_and_string_value_to_be_serializable() throws Exception {
        
        String value = "value";
        when(imethodsMock.stringReturningMethod()).thenReturn(value);

        
        ByteArrayOutputStream serialized = serializeMock(imethodsMock);

        
        IMethods readObject = deserializeMock(serialized, IMethods.class);
        assertEquals(value, readObject.stringReturningMethod());
    }

// org.mockitousage.basicapi.MocksSerializationForAnnotationTest::should_all_mock_and_serializable_value_to_be_serialized
    public void should_all_mock_and_serializable_value_to_be_serialized() throws Exception {
        
        List<?> value = Collections.emptyList();
        when(imethodsMock.objectReturningMethodNoArgs()).thenReturn(value);

        
        ByteArrayOutputStream serialized = serializeMock(imethodsMock);

        
        IMethods readObject = deserializeMock(serialized, IMethods.class);
        assertEquals(value, readObject.objectReturningMethodNoArgs());
    }

// org.mockitousage.basicapi.MocksSerializationForAnnotationTest::should_serialize_method_call_with_parameters_that_are_serializable
    public void should_serialize_method_call_with_parameters_that_are_serializable() throws Exception {
        List<?> value = Collections.emptyList();
        when(imethodsMock.objectArgMethod(value)).thenReturn(value);

        
        ByteArrayOutputStream serialized = serializeMock(imethodsMock);

        
        IMethods readObject = deserializeMock(serialized, IMethods.class);
        assertEquals(value, readObject.objectArgMethod(value));
    }

// org.mockitousage.basicapi.MocksSerializationForAnnotationTest::should_serialize_method_calls_using_any_string_matcher
    public void should_serialize_method_calls_using_any_string_matcher() throws Exception {
        List<?> value = Collections.emptyList();
        when(imethodsMock.objectArgMethod(anyString())).thenReturn(value);

        
        ByteArrayOutputStream serialized = serializeMock(imethodsMock);

        
        IMethods readObject = deserializeMock(serialized, IMethods.class);
        assertEquals(value, readObject.objectArgMethod(""));
    }

// org.mockitousage.basicapi.MocksSerializationForAnnotationTest::should_verify_called_n_times_for_serialized_mock
    public void should_verify_called_n_times_for_serialized_mock() throws Exception {
        List<?> value = Collections.emptyList();
        when(imethodsMock.objectArgMethod(anyString())).thenReturn(value);
        imethodsMock.objectArgMethod("");

        
        ByteArrayOutputStream serialized = serializeMock(imethodsMock);

        
        IMethods readObject = deserializeMock(serialized, IMethods.class);
        verify(readObject, times(1)).objectArgMethod("");
    }

// org.mockitousage.basicapi.MocksSerializationForAnnotationTest::should_verify_even_if_some_methods_called_after_serialization
    public void should_verify_even_if_some_methods_called_after_serialization() throws Exception {

        
    	imethodsMock.simpleMethod(1);
        ByteArrayOutputStream serialized = serializeMock(imethodsMock);
        IMethods readObject = deserializeMock(serialized, IMethods.class);
        readObject.simpleMethod(1);

        
        verify(readObject, times(2)).simpleMethod(1);

        
        
    }

// org.mockitousage.basicapi.MocksSerializationForAnnotationTest::should_serialization_work
    public void should_serialization_work() {}

// org.mockitousage.basicapi.MocksSerializationForAnnotationTest::should_stub_even_if_some_methods_called_after_serialization
    public void should_stub_even_if_some_methods_called_after_serialization() throws Exception {
        
        
        when(imethodsMock.simpleMethod(1)).thenReturn("foo");
        ByteArrayOutputStream serialized = serializeMock(imethodsMock);
        IMethods readObject = deserializeMock(serialized, IMethods.class);
        when(readObject.simpleMethod(2)).thenReturn("bar");

        
        assertEquals("foo", readObject.simpleMethod(1));
        assertEquals("bar", readObject.simpleMethod(2));
    }

// org.mockitousage.basicapi.MocksSerializationForAnnotationTest::should_verify_call_order_for_serialized_mock
    public void should_verify_call_order_for_serialized_mock() throws Exception {
        imethodsMock.arrayReturningMethod();
        imethodsMock2.arrayReturningMethod();

        
        ByteArrayOutputStream serialized = serializeMock(imethodsMock);
        ByteArrayOutputStream serialized2 = serializeMock(imethodsMock2);

        
        IMethods readObject = deserializeMock(serialized, IMethods.class);
        IMethods readObject2 = deserializeMock(serialized2, IMethods.class);
        InOrder inOrder = inOrder(readObject, readObject2);
        inOrder.verify(readObject).arrayReturningMethod();
        inOrder.verify(readObject2).arrayReturningMethod();
    }

// org.mockitousage.basicapi.MocksSerializationForAnnotationTest::should_remember_interactions_for_serialized_mock
    public void should_remember_interactions_for_serialized_mock() throws Exception {
        List<?> value = Collections.emptyList();
        when(imethodsMock.objectArgMethod(anyString())).thenReturn(value);
        imethodsMock.objectArgMethod("happened");

        
        ByteArrayOutputStream serialized = serializeMock(imethodsMock);

        
        IMethods readObject = deserializeMock(serialized, IMethods.class);
        verify(readObject, never()).objectArgMethod("never happened");
    }

// org.mockitousage.basicapi.MocksSerializationForAnnotationTest::should_serialize_with_stubbing_callback
    public void should_serialize_with_stubbing_callback() throws Exception {

        
        CustomAnswersMustImplementSerializableForSerializationToWork answer = 
            new CustomAnswersMustImplementSerializableForSerializationToWork();
        answer.string = "return value";
        when(imethodsMock.objectArgMethod(anyString())).thenAnswer(answer);

        
        ByteArrayOutputStream serialized = serializeMock(imethodsMock);

        
        IMethods readObject = deserializeMock(serialized, IMethods.class);
        assertEquals(answer.string, readObject.objectArgMethod(""));
    }

// org.mockitousage.basicapi.MocksSerializationForAnnotationTest::should_serialize_with_real_object_spy
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

// org.mockitousage.basicapi.MocksSerializationForAnnotationTest::should_serialize_object_mock
    public void should_serialize_object_mock() {}

// org.mockitousage.basicapi.MocksSerializationForAnnotationTest::should_serialize_real_partial_mock
    public void should_serialize_real_partial_mock() {}

// org.mockitousage.basicapi.MocksSerializationForAnnotationTest::should_serialize_already_serializable_class
    public void should_serialize_already_serializable_class() throws Exception {
        
        when(alreadySerializableMock.toString()).thenReturn("foo");

        
        alreadySerializableMock = serializeAndBack(alreadySerializableMock);

        
        assertEquals("foo", alreadySerializableMock.toString());
    }

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

// buggy code
    public Object answer(InvocationOnMock invocation) throws Throwable {
        GenericMetadataSupport returnTypeGenericMetadata =
                actualParameterizedType(invocation.getMock()).resolveGenericReturnType(invocation.getMethod());

        Class<?> rawType = returnTypeGenericMetadata.rawType();
        if (!new MockCreationValidator().isTypeMockable(rawType)) {
            return delegate.returnValueFor(rawType);
        }

        return getMock(invocation);
    }

    private Object getMock(InvocationOnMock invocation) throws Throwable {
    	InternalMockHandler<Object> handler = new MockUtil().getMockHandler(invocation.getMock());
    	InvocationContainerImpl container = (InvocationContainerImpl) handler.getInvocationContainer();

        // matches invocation for verification
        for (StubbedInvocationMatcher stubbedInvocationMatcher : container.getStubbedInvocations()) {
    		if(container.getInvocationForStubbing().matches(stubbedInvocationMatcher.getInvocation())) {
    			return stubbedInvocationMatcher.answer(invocation);
    		}
		}

        // deep stub
        return recordDeepStubMock(invocation, container);
    }

    private Object recordDeepStubMock(InvocationOnMock invocation, InvocationContainerImpl container) {
        Class<?> clz = invocation.getMethod().getReturnType();
        final Object mock = org.mockito.Mockito.mock(clz, this);

        container.addAnswer(new Answer<Object>() {
            public Object answer(InvocationOnMock invocation) throws Throwable {
                return mock;
            }
        }, false);

        return mock;
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
        new Reporter().tooLittleActualInvocations(new org.mockito.internal.reporting.Discrepancy(1, 2), new InvocationBuilder().toInvocation(), null);
    }

// org.mockito.exceptions.ReporterTest::shouldThrowCorrectExceptionForNullInvocationListener
    public void shouldThrowCorrectExceptionForNullInvocationListener() throws Exception {
    	new Reporter().invocationListenerDoesNotAcceptNullParameters();
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

// org.mockito.internal.InOrderImplTest::shouldMarkVerifiedInOrder
    public void shouldMarkVerifiedInOrder() throws Exception {
        
        InOrderImpl impl = new InOrderImpl((List) asList(mock));
        Invocation i = new InvocationBuilder().toInvocation();
        assertFalse(impl.isVerified(i));
        
        
        impl.markVerified(i);
        
        
        assertTrue(impl.isVerified(i));
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

// org.mockito.internal.configuration.MockInjectionTest::should_not_allow_null_on_field
    public void should_not_allow_null_on_field() {
        MockInjection.onField((Field) null, this);
    }

// org.mockito.internal.configuration.MockInjectionTest::should_not_allow_null_on_fields
    public void should_not_allow_null_on_fields() {
        MockInjection.onFields((Set<Field>) null, this);
    }

// org.mockito.internal.configuration.MockInjectionTest::should_not_allow_null_on_instance_owning_the_field
    public void should_not_allow_null_on_instance_owning_the_field() throws Exception {
        MockInjection.onField(field("withConstructor"), null);
    }

// org.mockito.internal.configuration.MockInjectionTest::should_not_allow_null_on_mocks
    public void should_not_allow_null_on_mocks() throws Exception {
        MockInjection.onField(field("withConstructor"), this).withMocks(null);
    }

// org.mockito.internal.configuration.MockInjectionTest::can_try_constructor_injection
    public void can_try_constructor_injection() throws Exception {
        MockInjection.onField(field("withConstructor"), this).withMocks(oneSetMock()).tryConstructorInjection().apply();

        assertThat(withConstructor.initializedWithConstructor).isEqualTo(true);
    }

// org.mockito.internal.configuration.MockInjectionTest::should_not_fail_if_constructor_injection_is_not_possible
    public void should_not_fail_if_constructor_injection_is_not_possible() throws Exception {
        MockInjection.onField(field("withoutConstructor"), this).withMocks(otherKindOfMocks()).tryConstructorInjection().apply();

        assertThat(withoutConstructor).isNull();
    }

// org.mockito.internal.configuration.MockInjectionTest::can_try_property_or_setter_injection
    public void can_try_property_or_setter_injection() throws Exception {
        MockInjection.onField(field("withoutConstructor"), this).withMocks(oneSetMock()).tryPropertyOrFieldInjection().apply();

        assertThat(withoutConstructor.theSet).isNotNull();
    }

// org.mockito.internal.configuration.MockInjectionTest::should_not_fail_if_property_or_field_injection_is_not_possible
    public void should_not_fail_if_property_or_field_injection_is_not_possible() throws Exception {
        MockInjection.onField(field("withoutConstructor"), this).withMocks(otherKindOfMocks()).tryPropertyOrFieldInjection().apply();

        assertThat(withoutConstructor.theSet).isNull();
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

// org.mockito.internal.creation.MockSettingsImplTest::shouldNotAllowSettingNullInterface
    public void shouldNotAllowSettingNullInterface() {
        mockSettingsImpl.extraInterfaces(List.class, null);
    }

// org.mockito.internal.creation.MockSettingsImplTest::shouldNotAllowNonInterfaces
    public void shouldNotAllowNonInterfaces() {
        mockSettingsImpl.extraInterfaces(List.class, LinkedList.class);
    }

// org.mockito.internal.creation.MockSettingsImplTest::shouldNotAllowUsingTheSameInterfaceAsExtra
    public void shouldNotAllowUsingTheSameInterfaceAsExtra() {
        mockSettingsImpl.extraInterfaces(List.class, LinkedList.class);
    }

// org.mockito.internal.creation.MockSettingsImplTest::shouldNotAllowEmptyExtraInterfaces
    public void shouldNotAllowEmptyExtraInterfaces() {
        mockSettingsImpl.extraInterfaces();
    }

// org.mockito.internal.creation.MockSettingsImplTest::shouldNotAllowNullArrayOfExtraInterfaces
    public void shouldNotAllowNullArrayOfExtraInterfaces() {
        mockSettingsImpl.extraInterfaces((Class[]) null);
    }

// org.mockito.internal.creation.MockSettingsImplTest::shouldAllowMultipleInterfaces
    public void shouldAllowMultipleInterfaces() {
        
        mockSettingsImpl.extraInterfaces(List.class, Set.class);
        
        
        assertEquals(2, mockSettingsImpl.getExtraInterfaces().size());
        assertTrue(mockSettingsImpl.getExtraInterfaces().contains(List.class));
        assertTrue(mockSettingsImpl.getExtraInterfaces().contains(Set.class));
    }

// org.mockito.internal.creation.MockSettingsImplTest::shouldSetMockToBeSerializable
    public void shouldSetMockToBeSerializable() throws Exception {
        
        mockSettingsImpl.serializable();

        
        assertTrue(mockSettingsImpl.isSerializable());
    }

// org.mockito.internal.creation.MockSettingsImplTest::shouldKnowIfIsSerializable
    public void shouldKnowIfIsSerializable() throws Exception {
        
        assertFalse(mockSettingsImpl.isSerializable());

        
        mockSettingsImpl.serializable();

        
        assertTrue(mockSettingsImpl.isSerializable());
    }

// org.mockito.internal.creation.MockSettingsImplTest::shouldAddVerboseLoggingListener
    public void shouldAddVerboseLoggingListener() {
        
        assertFalse(mockSettingsImpl.hasInvocationListeners());

        
        mockSettingsImpl.verboseLogging();

        
        assertContainsType(mockSettingsImpl.getInvocationListeners(), VerboseMockInvocationLogger.class);
    }

// org.mockito.internal.creation.MockSettingsImplTest::shouldAddVerboseLoggingListenerOnlyOnce
    public void shouldAddVerboseLoggingListenerOnlyOnce() {
    	
    	assertFalse(mockSettingsImpl.hasInvocationListeners());
    	
    	
    	mockSettingsImpl.verboseLogging().verboseLogging();
    	
    	
    	Assertions.assertThat(mockSettingsImpl.getInvocationListeners()).hasSize(1);
    }

// org.mockito.internal.creation.MockSettingsImplTest::shouldNotAllowNullListener
    public void shouldNotAllowNullListener() {
    	mockSettingsImpl.invocationListeners(null);
    }

// org.mockito.internal.creation.MockSettingsImplTest::shouldAddInvocationListener
    public void shouldAddInvocationListener() {
    	
    	assertFalse(mockSettingsImpl.hasInvocationListeners());
    	
    	
    	mockSettingsImpl.invocationListeners(invocationListener);
    	
    	
        Assertions.assertThat(mockSettingsImpl.getInvocationListeners()).contains(invocationListener);
    }

// org.mockito.internal.creation.MockSettingsImplTest::canAddDuplicateInvocationListeners_ItsNotOurBusinessThere
    public void canAddDuplicateInvocationListeners_ItsNotOurBusinessThere() {
    	
    	assertFalse(mockSettingsImpl.hasInvocationListeners());
    	
    	
    	mockSettingsImpl.invocationListeners(invocationListener, invocationListener).invocationListeners(invocationListener);
    	
    	
    	Assertions.assertThat(mockSettingsImpl.getInvocationListeners()).containsSequence(invocationListener, invocationListener, invocationListener);
    }

// org.mockito.internal.creation.MockSettingsImplTest::shouldReportErrorWhenAddingNoInvocationListeners
    public void shouldReportErrorWhenAddingNoInvocationListeners() throws Exception {
        try {
            mockSettingsImpl.invocationListeners();
            fail();
        } catch (Exception e) {
            Assertions.assertThat(e.getMessage()).contains("at least one listener");
        }
    }

// org.mockito.internal.creation.MockSettingsImplTest::shouldReportErrorWhenAddingANullInvocationListener
    public void shouldReportErrorWhenAddingANullInvocationListener() throws Exception {
        try {
            mockSettingsImpl.invocationListeners(invocationListener, null);
            fail();
        } catch (Exception e) {
            Assertions.assertThat(e.getMessage()).contains("does not accept null");
        }
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

// org.mockito.internal.debugging.VerboseMockInvocationLoggerTest::should_print_to_system_out
    public void should_print_to_system_out() {
        assertThat(new VerboseMockInvocationLogger().printStream).isSameAs(System.out);
    }

// org.mockito.internal.debugging.VerboseMockInvocationLoggerTest::should_print_invocation_with_return_value
    public void should_print_invocation_with_return_value() {
        
        listener.reportInvocation(new NotifiedMethodInvocationReport(invocation, "return value"));

        
        assertThat(printed())
                .contains(invocation.toString())
                .contains(invocation.getLocation().toString())
                .contains("return value");
    }

// org.mockito.internal.debugging.VerboseMockInvocationLoggerTest::should_print_invocation_with_exception
    public void should_print_invocation_with_exception() {
        
        listener.reportInvocation(new NotifiedMethodInvocationReport(invocation, new ThirdPartyException()));

        
        assertThat(printed())
                .contains(invocation.toString())
                .contains(invocation.getLocation().toString())
                .contains(ThirdPartyException.class.getName());
    }

// org.mockito.internal.debugging.VerboseMockInvocationLoggerTest::should_print_if_method_has_not_been_stubbed
    public void should_print_if_method_has_not_been_stubbed() throws Exception {
        listener.reportInvocation(new NotifiedMethodInvocationReport(invocation, "whatever"));

        assertThat(printed()).doesNotContain("stubbed");
    }

// org.mockito.internal.debugging.VerboseMockInvocationLoggerTest::should_print_stubbed_info_if_availbable
    public void should_print_stubbed_info_if_availbable() throws Exception {
        invocation.markStubbed(new StubInfoImpl(stubbedInvocation));

        listener.reportInvocation(new NotifiedMethodInvocationReport(invocation, "whatever"));

        assertThat(printed())
                .contains("stubbed")
                .contains(stubbedInvocation.getLocation().toString());
    }

// org.mockito.internal.debugging.VerboseMockInvocationLoggerTest::should_log_count_of_interactions
    public void should_log_count_of_interactions() {
        
        listener.reportInvocation(new NotifiedMethodInvocationReport(invocation, new ThirdPartyException()));
        assertThat(printed()).contains("#1");

        listener.reportInvocation(new NotifiedMethodInvocationReport(invocation, new ThirdPartyException()));
        assertThat(printed()).contains("#2");

        listener.reportInvocation(new NotifiedMethodInvocationReport(invocation, new ThirdPartyException()));
        assertThat(printed()).contains("#3");
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

// org.mockito.internal.exceptions.util.ScenarioPrinterTest::shouldPrintInvocations
    public void shouldPrintInvocations() {
        
        Invocation verified = new InvocationBuilder().simpleMethod().verified().toInvocation();
        Invocation unverified = new InvocationBuilder().differentMethod().toInvocation();
        
        
        String out = sp.print((List) asList(verified, unverified));
        
        
        assertContains("1. -> at", out);
        assertContains("2. [?]-> at", out);
    }

// org.mockito.internal.exceptions.util.ScenarioPrinterTest::shouldNotPrintInvocationsWhenSingleUnwanted
    public void shouldNotPrintInvocationsWhenSingleUnwanted() {
        
        Invocation unverified = new InvocationBuilder().differentMethod().toInvocation();
        
        
        String out = sp.print((List) asList(unverified));
        
        
        assertContains("Actually, above is the only interaction with this mock.", out);
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

// org.mockito.internal.invocation.InvocationImplTest::shouldKnowIfIsEqualTo
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

// org.mockito.internal.invocation.InvocationImplTest::shouldEqualToNotConsiderSequenceNumber
    public void shouldEqualToNotConsiderSequenceNumber() {
        Invocation equal = new InvocationBuilder().args(" ").mock("mock").seq(2).toInvocation();
        
        assertTrue(invocation.equals(equal));
        assertTrue(invocation.getSequenceNumber() != equal.getSequenceNumber());
    }

// org.mockito.internal.invocation.InvocationImplTest::shouldBeACitizenOfHashes
    public void shouldBeACitizenOfHashes() {
        Map map = new HashMap();
        map.put(invocation, "one");
        assertEquals("one", map.get(invocation));
    }

// org.mockito.internal.invocation.InvocationImplTest::shouldPrintMethodName
    public void shouldPrintMethodName() {
        invocation = new InvocationBuilder().toInvocation();
        assertEquals("iMethods.simpleMethod();", invocation.toString());
    }

// org.mockito.internal.invocation.InvocationImplTest::shouldPrintMethodArgs
    public void shouldPrintMethodArgs() {
        invocation = new InvocationBuilder().args("foo").toInvocation();
        assertThat(invocation.toString(), endsWith("simpleMethod(\"foo\");"));
    }

// org.mockito.internal.invocation.InvocationImplTest::shouldPrintMethodIntegerArgAndString
    public void shouldPrintMethodIntegerArgAndString() {
        invocation = new InvocationBuilder().args("foo", 1).toInvocation();
        assertThat(invocation.toString(), endsWith("simpleMethod(\"foo\", 1);"));
    }

// org.mockito.internal.invocation.InvocationImplTest::shouldPrintNull
    public void shouldPrintNull() {
        invocation = new InvocationBuilder().args((String) null).toInvocation();
        assertThat(invocation.toString(), endsWith("simpleMethod(null);"));
    }

// org.mockito.internal.invocation.InvocationImplTest::shouldPrintArray
    public void shouldPrintArray() {
        invocation = new InvocationBuilder().method("oneArray").args(new int[] { 1, 2, 3 }).toInvocation();
        assertThat(invocation.toString(), endsWith("oneArray([1, 2, 3]);"));
    }

// org.mockito.internal.invocation.InvocationImplTest::shouldPrintNullIfArrayIsNull
    public void shouldPrintNullIfArrayIsNull() throws Exception {
        Method m = IMethods.class.getMethod("oneArray", Object[].class);
        invocation = new InvocationBuilder().method(m).args((Object) null).toInvocation();
        assertThat(invocation.toString(), endsWith("oneArray(null);"));
    }

// org.mockito.internal.invocation.InvocationImplTest::shouldPrintArgumentsInMultilinesWhenGetsTooBig
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

// org.mockito.internal.invocation.InvocationImplTest::shouldTransformArgumentsToMatchers
    public void shouldTransformArgumentsToMatchers() throws Exception {
        Invocation i = new InvocationBuilder().args("foo", new String[]{"bar"}).toInvocation();
        List matchers = ArgumentsProcessor.argumentsToMatchers(i.getArguments());

        assertEquals(2, matchers.size());
        assertEquals(Equals.class, matchers.get(0).getClass());
        assertEquals(ArrayEquals.class, matchers.get(1).getClass());
    }

// org.mockito.internal.invocation.InvocationImplTest::shouldBeAbleToCallRealMethod
    public void shouldBeAbleToCallRealMethod() throws Throwable {
        
        Invocation invocation = invocationOf(Foo.class, "bark", new RealMethod() {
            public Object invoke(Object target, Object[] arguments) throws Throwable {
                return new Foo().bark();
            }});
        
        assertEquals("woof", invocation.callRealMethod());
    }

// org.mockito.internal.invocation.InvocationImplTest::shouldScreamWhenCallingRealMethodOnInterface
    public void shouldScreamWhenCallingRealMethodOnInterface() throws Throwable {
        
        Invocation invocationOnInterface = new InvocationBuilder().toInvocation();

        try {
            
            invocationOnInterface.callRealMethod();
            
            fail();
        } catch(MockitoException e) {}
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
        
        InOrderContextImpl context = new InOrderContextImpl();
        InvocationMarker marker = new InvocationMarker();
        Invocation i = new InvocationBuilder().toInvocation();
        InvocationMatcher im = new InvocationBuilder().toInvocationMatcher();
        assertFalse(context.isVerified(i));
        assertFalse(i.isVerified());
        
        
        marker.markVerifiedInOrder(Arrays.asList(i), im, context);
        
        
        assertTrue(context.isVerified(i));
        assertTrue(i.isVerified());
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
        VarargCapturingMatcher varargCapturingMatcher = new VarargCapturingMatcher();
        InvocationMatcher invocationMatcher = new InvocationMatcher(invocation, (List) asList(new Equals(1), new LocalizedMatcher(varargCapturingMatcher)));

        
        invocationMatcher.captureArgumentsFrom(invocation);

        
        Assertions.assertThat(varargCapturingMatcher.getLastVarargs()).containsExactly("a", "b");
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

// org.mockito.internal.invocation.InvocationsFinderTest::shouldFindFirstUnverifiedInOrder
    public void shouldFindFirstUnverifiedInOrder() throws Exception {
        
        InOrderContextImpl context = new InOrderContextImpl();
        assertSame(simpleMethodInvocation, finder.findFirstUnverifiedInOrder(context, invocations));        
        
        
        context.markVerified(simpleMethodInvocationTwo);
        context.markVerified(simpleMethodInvocation);
        
        
        assertSame(differentMethodInvocation, finder.findFirstUnverifiedInOrder(context, invocations));
        
        
        context.markVerified(differentMethodInvocation);
        
        
        assertNull(finder.findFirstUnverifiedInOrder(context, invocations));
    }

// org.mockito.internal.invocation.InvocationsFinderTest::shouldFindFirstUnverifiedInOrderAndRespectSequenceNumber
    public void shouldFindFirstUnverifiedInOrderAndRespectSequenceNumber() throws Exception {
        
        InOrderContextImpl context = new InOrderContextImpl();
        assertSame(simpleMethodInvocation, finder.findFirstUnverifiedInOrder(context, invocations));        
        
        
        
        context.markVerified(simpleMethodInvocationTwo);
        context.markVerified(differentMethodInvocation);
        
        
        assertSame(null, finder.findFirstUnverifiedInOrder(context, invocations));        
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
        List<Invocation> allMatching = finder.findAllMatchingUnverifiedChunks(invocations, new InvocationMatcher(simpleMethodInvocation), context);
        assertThat(allMatching, hasExactlyInOrder(simpleMethodInvocation, simpleMethodInvocationTwo));
        
        context.markVerified(simpleMethodInvocation);
        allMatching = finder.findAllMatchingUnverifiedChunks(invocations, new InvocationMatcher(simpleMethodInvocation), context);
        assertThat(allMatching, hasExactlyInOrder(simpleMethodInvocationTwo));
        
        context.markVerified(simpleMethodInvocationTwo);
        allMatching = finder.findAllMatchingUnverifiedChunks(invocations, new InvocationMatcher(simpleMethodInvocation), context);
        assertTrue(allMatching.isEmpty());
    }

// org.mockito.internal.invocation.InvocationsFinderTest::shouldFindMatchingChunk
    public void shouldFindMatchingChunk() throws Exception {
        List<Invocation> chunk = finder.findMatchingChunk(invocations, new InvocationMatcher(simpleMethodInvocation), 2, context);
        assertThat(chunk, hasExactlyInOrder(simpleMethodInvocation, simpleMethodInvocationTwo));
    }

// org.mockito.internal.invocation.InvocationsFinderTest::shouldReturnAllChunksWhenModeIsAtLeastOnce
    public void shouldReturnAllChunksWhenModeIsAtLeastOnce() throws Exception {
        Invocation simpleMethodInvocationThree = new InvocationBuilder().mock(mock).toInvocation();
        invocations.add(simpleMethodInvocationThree);
        
        List<Invocation> chunk = finder.findMatchingChunk(invocations, new InvocationMatcher(simpleMethodInvocation), 1, context);
        assertThat(chunk, hasExactlyInOrder(simpleMethodInvocation, simpleMethodInvocationTwo, simpleMethodInvocationThree));
    }

// org.mockito.internal.invocation.InvocationsFinderTest::shouldReturnAllChunksWhenWantedCountDoesntMatch
    public void shouldReturnAllChunksWhenWantedCountDoesntMatch() throws Exception {
        Invocation simpleMethodInvocationThree = new InvocationBuilder().mock(mock).toInvocation();
        invocations.add(simpleMethodInvocationThree);
        
        List<Invocation> chunk = finder.findMatchingChunk(invocations, new InvocationMatcher(simpleMethodInvocation), 1, context);
        assertThat(chunk, hasExactlyInOrder(simpleMethodInvocation, simpleMethodInvocationTwo, simpleMethodInvocationThree));
    }

// org.mockito.internal.invocation.InvocationsFinderTest::shouldFindPreviousInOrder
    public void shouldFindPreviousInOrder() throws Exception {
        Invocation previous = finder.findPreviousVerifiedInOrder(invocations, context);
        assertNull(previous);
        
        context.markVerified(simpleMethodInvocation);
        context.markVerified(simpleMethodInvocationTwo);
        
        previous = finder.findPreviousVerifiedInOrder(invocations, context);
        assertSame(simpleMethodInvocationTwo, previous);
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

// org.mockito.internal.progress.ThreadSafeMockingProgressTest::shouldShareState
    public void shouldShareState() throws Exception {
        
        ThreadSafeMockingProgress p = new ThreadSafeMockingProgress();
        p.verificationStarted(new DummyVerificationMode());

        
        p = new ThreadSafeMockingProgress();
        assertNotNull(p.pullVerificationMode());
    }

// org.mockito.internal.progress.ThreadSafeMockingProgressTest::shouldKnowWhenVerificationHasStarted
    public void shouldKnowWhenVerificationHasStarted() throws Exception {
        
        verify(mock(List.class));
        ThreadSafeMockingProgress p = new ThreadSafeMockingProgress();

        
        assertNotNull(p.pullVerificationMode());
    }

// org.mockito.internal.stubbing.InvocationContainerImplStubbingTest::should_finish_stubbing_when_wrong_throwable_is_set
    public void should_finish_stubbing_when_wrong_throwable_is_set() throws Exception {
        state.stubbingStarted();
        try {
            invocationContainerImpl.addAnswer(new ThrowsException(new Exception()));
            fail();
        } catch (MockitoException e) {
            state.validateState();
        }
    }

// org.mockito.internal.stubbing.InvocationContainerImplStubbingTest::should_finish_stubbing_on_adding_return_value
    public void should_finish_stubbing_on_adding_return_value() throws Exception {
        state.stubbingStarted();
        invocationContainerImpl.addAnswer(new Returns("test"));
        state.validateState();
    }

// org.mockito.internal.stubbing.InvocationContainerImplStubbingTest::should_get_results_for_methods
    public void should_get_results_for_methods() throws Throwable {
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

// org.mockito.internal.stubbing.InvocationContainerImplStubbingTest::should_get_results_for_methods_stub_only
    public void should_get_results_for_methods_stub_only() throws Throwable {
        invocationContainerImplStubOnly.setInvocationForPotentialStubbing(new InvocationMatcher(simpleMethod));
        invocationContainerImplStubOnly.addAnswer(new Returns("simpleMethod"));

        Invocation differentMethod = new InvocationBuilder().differentMethod().toInvocation();
        invocationContainerImplStubOnly.setInvocationForPotentialStubbing(new InvocationMatcher(differentMethod));
        invocationContainerImplStubOnly.addAnswer(new ThrowsException(new MyException()));

        assertEquals("simpleMethod", invocationContainerImplStubOnly.answerTo(simpleMethod));

        try {
            invocationContainerImplStubOnly.answerTo(differentMethod);
            fail();
        } catch (MyException e) {}
    }

// org.mockito.internal.stubbing.InvocationContainerImplStubbingTest::should_add_throwable_for_void_method
    public void should_add_throwable_for_void_method() throws Throwable {
        invocationContainerImpl.addAnswerForVoidMethod(new ThrowsException(new MyException()));
        invocationContainerImpl.setMethodForStubbing(new InvocationMatcher(simpleMethod));

        try {
            invocationContainerImpl.answerTo(simpleMethod);
            fail();
        } catch (MyException e) {}
    }

// org.mockito.internal.stubbing.InvocationContainerImplStubbingTest::should_validate_throwable_for_void_method
    public void should_validate_throwable_for_void_method() throws Throwable {
        invocationContainerImpl.addAnswerForVoidMethod(new ThrowsException(new Exception()));

        try {
            invocationContainerImpl.setMethodForStubbing(new InvocationMatcher(simpleMethod));
            fail();
        } catch (MockitoException e) {}
    }

// org.mockito.internal.stubbing.InvocationContainerImplStubbingTest::should_validate_throwable
    public void should_validate_throwable() throws Throwable {
        try {
            invocationContainerImpl.addAnswer(new ThrowsException(null));
            fail();
        } catch (MockitoException e) {}
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

// org.mockito.internal.stubbing.answers.MethodInfoTest::shouldKnowValidThrowables
    public void shouldKnowValidThrowables() throws Exception {
        
        Invocation invocation = new InvocationBuilder().method("canThrowException").toInvocation();
        MethodInfo info = new MethodInfo(invocation);

        
        assertFalse(info.isValidException(new Exception()));
        assertTrue(info.isValidException(new CharacterCodingException()));
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

// org.mockito.internal.util.MockUtilTest::shouldValidateSpy
    public void shouldValidateSpy() {
        assertFalse(mockUtil.isSpy("i mock a mock"));
        assertFalse(mockUtil.isSpy(Mockito.mock(List.class)));
        assertTrue(mockUtil.isSpy(Mockito.spy(new ArrayList())));
    }

// org.mockito.internal.util.MockUtilTest::should_redefine_MockName_if_default
    public void should_redefine_MockName_if_default() {
        List mock = Mockito.mock(List.class);
        mockUtil.maybeRedefineMockName(mock, "newName");

        Assertions.assertThat(mockUtil.getMockName(mock).toString()).isEqualTo("newName");
    }

// org.mockito.internal.util.MockUtilTest::should_not_redefine_MockName_if_default
    public void should_not_redefine_MockName_if_default() {
        List mock = Mockito.mock(List.class, "original");
        mockUtil.maybeRedefineMockName(mock, "newName");

        Assertions.assertThat(mockUtil.getMockName(mock).toString()).isEqualTo("original");
    }

// org.mockito.internal.util.collections.HashCodeAndEqualsSafeSetTest::can_add_mock_that_have_failing_hashCode_method
    public void can_add_mock_that_have_failing_hashCode_method() throws Exception {
        new HashCodeAndEqualsSafeSet().add(mock(UnmockableHashCodeAndEquals.class));
    }

// org.mockito.internal.util.collections.HashCodeAndEqualsSafeSetTest::mock_with_failing_hashCode_method_can_be_added
    public void mock_with_failing_hashCode_method_can_be_added() throws Exception {
        new HashCodeAndEqualsSafeSet().add(mock(UnmockableHashCodeAndEquals.class));
    }

// org.mockito.internal.util.collections.HashCodeAndEqualsSafeSetTest::mock_with_failing_equals_method_can_be_used
    public void mock_with_failing_equals_method_can_be_used() throws Exception {
        HashCodeAndEqualsSafeSet mocks = new HashCodeAndEqualsSafeSet();
        UnmockableHashCodeAndEquals mock = mock(UnmockableHashCodeAndEquals.class);
        mocks.add(mock);

        assertThat(mocks.contains(mock)).isTrue();
        assertThat(mocks.contains(mock(UnmockableHashCodeAndEquals.class))).isFalse();
    }

// org.mockito.internal.util.collections.HashCodeAndEqualsSafeSetTest::can_remove
    public void can_remove() throws Exception {
        HashCodeAndEqualsSafeSet mocks = new HashCodeAndEqualsSafeSet();
        UnmockableHashCodeAndEquals mock = mock(UnmockableHashCodeAndEquals.class);
        mocks.add(mock);
        mocks.remove(mock);

        assertThat(mocks.isEmpty()).isTrue();
    }

// org.mockito.internal.util.collections.HashCodeAndEqualsSafeSetTest::can_add_a_collection
    public void can_add_a_collection() throws Exception {
        HashCodeAndEqualsSafeSet mocks = HashCodeAndEqualsSafeSet.of(
                mock(UnmockableHashCodeAndEquals.class),
                mock(Observer.class));

        HashCodeAndEqualsSafeSet workingSet = new HashCodeAndEqualsSafeSet();

        workingSet.addAll(mocks);

        assertThat(workingSet.containsAll(mocks)).isTrue();
    }

// org.mockito.internal.util.collections.HashCodeAndEqualsSafeSetTest::can_retain_a_collection
    public void can_retain_a_collection() throws Exception {
        HashCodeAndEqualsSafeSet mocks = HashCodeAndEqualsSafeSet.of(
                mock(UnmockableHashCodeAndEquals.class),
                mock(Observer.class));

        HashCodeAndEqualsSafeSet workingSet = new HashCodeAndEqualsSafeSet();

        workingSet.addAll(mocks);
        workingSet.add(mock(List.class));

        assertThat(workingSet.retainAll(mocks)).isTrue();
        assertThat(workingSet.containsAll(mocks)).isTrue();
    }

// org.mockito.internal.util.collections.HashCodeAndEqualsSafeSetTest::can_remove_a_collection
    public void can_remove_a_collection() throws Exception {
        HashCodeAndEqualsSafeSet mocks = HashCodeAndEqualsSafeSet.of(
                mock(UnmockableHashCodeAndEquals.class),
                mock(Observer.class));

        HashCodeAndEqualsSafeSet workingSet = new HashCodeAndEqualsSafeSet();

        workingSet.addAll(mocks);
        workingSet.add(mock(List.class));

        assertThat(workingSet.removeAll(mocks)).isTrue();
        assertThat(workingSet.containsAll(mocks)).isFalse();
    }

// org.mockito.internal.util.collections.HashCodeAndEqualsSafeSetTest::can_iterate
    public void can_iterate() throws Exception {
        HashCodeAndEqualsSafeSet mocks = HashCodeAndEqualsSafeSet.of(
                mock(UnmockableHashCodeAndEquals.class),
                mock(Observer.class));

        LinkedList<Object> accumulator = new LinkedList<Object>();
        for (Object mock : mocks) {
            accumulator.add(mock);
        }
        assertThat(accumulator).isNotEmpty();
    }

// org.mockito.internal.util.collections.HashCodeAndEqualsSafeSetTest::toArray_just_work
    public void toArray_just_work() throws Exception {
        UnmockableHashCodeAndEquals mock1 = mock(UnmockableHashCodeAndEquals.class);
        HashCodeAndEqualsSafeSet mocks = HashCodeAndEqualsSafeSet.of(mock1);

        assertThat(mocks.toArray()[0]).isSameAs(mock1);

        assertThat(mocks.toArray(new UnmockableHashCodeAndEquals[0])[0]).isSameAs(mock1);
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

// org.mockito.internal.verification.OnlyTest::shouldMarkAsVerified
    public void shouldMarkAsVerified() {
        
        Invocation invocation = new InvocationBuilder().toInvocation();
        assertFalse(invocation.isVerified());
        
        
        only.verify(new VerificationDataStub(new InvocationMatcher(invocation), invocation));
        
        
        assertTrue(invocation.isVerified());
    }

// org.mockito.internal.verification.OnlyTest::shouldNotMarkAsVerifiedWhenAssertionFailed
    public void shouldNotMarkAsVerifiedWhenAssertionFailed() {
        
        Invocation invocation = new InvocationBuilder().toInvocation();
        assertFalse(invocation.isVerified());
        
        
        try {
            only.verify(new VerificationDataStub(new InvocationBuilder().toInvocationMatcher(), invocation));
            fail();
        } catch (MockitoAssertionError e) {}
        
        
        assertFalse(invocation.isVerified());
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
        
        checker.check(invocations, wanted, new VerificationModeBuilder().inOrder(), context);
    }

// org.mockito.internal.verification.checkers.MissingInvocationInOrderCheckerTest::shouldReportWantedButNotInvoked
    public void shouldReportWantedButNotInvoked() throws Exception {
        assertTrue(finderStub.allMatchingUnverifiedChunksToReturn.isEmpty());
        checker.check(invocations, wanted, new VerificationModeBuilder().inOrder(), context);
        
        assertEquals(wanted, reporterStub.wanted);
    }

// org.mockito.internal.verification.checkers.MissingInvocationInOrderCheckerTest::shouldReportArgumentsAreDifferent
    public void shouldReportArgumentsAreDifferent() throws Exception {
        assertTrue(finderStub.findInvocations(invocations, wanted).isEmpty());
        finderStub.similarToReturn = new InvocationBuilder().toInvocation();
        checker.check(invocations, wanted, new VerificationModeBuilder().inOrder(), context);
        SmartPrinter printer = new SmartPrinter(wanted, finderStub.similarToReturn, 0);
        assertEquals(printer.getWanted(), reporterStub.wantedString);
        assertEquals(printer.getActual(), reporterStub.actual);
        assertEquals(finderStub.similarToReturn.getLocation(), reporterStub.actualLocation);
     }

// org.mockito.internal.verification.checkers.MissingInvocationInOrderCheckerTest::shouldReportWantedDiffersFromActual
    public void shouldReportWantedDiffersFromActual() throws Exception {
        Invocation previous = new InvocationBuilder().toInvocation();
        finderStub.previousInOrderToReturn = previous;
        
        checker.check(invocations, wanted, new VerificationModeBuilder().inOrder(), context);
        
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
        checker.check(invocations, wanted, 0, context);
    }

// org.mockito.internal.verification.checkers.NumberOfInvocationsInOrderCheckerTest::shouldPassIfChunkMatches
    public void shouldPassIfChunkMatches() throws Exception {
        finderStub.validMatchingChunkToReturn.add(wanted.getInvocation());
        
        checker.check(invocations, wanted, 1, context);
    }

// org.mockito.internal.verification.checkers.NumberOfInvocationsInOrderCheckerTest::shouldReportTooLittleInvocations
    public void shouldReportTooLittleInvocations() throws Exception {
        Invocation first = new InvocationBuilder().toInvocation();
        Invocation second = new InvocationBuilder().toInvocation();
        finderStub.validMatchingChunkToReturn.addAll(asList(first, second)); 
        
        try {
            checker.check(invocations, wanted, 4, context);
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
            checker.check(invocations, wanted, 1, context);
            fail();
        } catch (VerificationInOrderFailure e) {
            assertContains("Wanted 1 time", e.getMessage());
            assertContains("But was 2 times", e.getMessage());
        }
    }

// org.mockito.internal.verification.checkers.NumberOfInvocationsInOrderCheckerTest::shouldMarkAsVerifiedInOrder
    public void shouldMarkAsVerifiedInOrder() throws Exception {
        Invocation invocation = new InvocationBuilder().toInvocation();
        assertFalse(context.isVerified(invocation));
        finderStub.validMatchingChunkToReturn.addAll(asList(invocation)); 
        
        checker.check(invocations, wanted, 1, context);
        
        assertTrue(context.isVerified(invocation));
    }

// org.mockito.verification.TimeoutTest::shouldPassWhenVerificationPasses
    public void shouldPassWhenVerificationPasses() {
        Timeout t = new Timeout(1, 3, mode);
        
        doNothing().when(mode).verify(data);
        
        t.verify(data);
    }

// org.mockito.verification.TimeoutTest::shouldFailBecauseVerificationFails
    public void shouldFailBecauseVerificationFails() {
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

// org.mockito.verification.TimeoutTest::shouldPassEvenIfFirstVerificationFails
    public void shouldPassEvenIfFirstVerificationFails() {
        Timeout t = new Timeout(1, 2, mode);
        
        doThrow(error).
        doThrow(error).
        doNothing().    
        when(mode).verify(data);
        
        t.verify(data);
    }

// org.mockito.verification.TimeoutTest::shouldTryToVerifyCorrectNumberOfTimes
    public void shouldTryToVerifyCorrectNumberOfTimes() {
        Timeout t = new Timeout(1, 4, mode);
        
        doThrow(error).when(mode).verify(data);
        
        try {
            t.verify(data);
            fail();
        } catch (MockitoAssertionError e) {};
        
        verify(mode, times(5)).verify(data);
    }

// org.mockito.verification.TimeoutTest::shouldCreateCorrectType
    public void shouldCreateCorrectType() {
        Timeout t = new Timeout(25, 50, mode);
        
        assertCorrectMode(t.atLeastOnce(), Timeout.class, 50, 25, AtLeast.class);
        assertCorrectMode(t.atLeast(5), Timeout.class, 50, 25, AtLeast.class);
        assertCorrectMode(t.times(5), Timeout.class, 50, 25, Times.class);
        assertCorrectMode(t.never(), Timeout.class, 50, 25, Times.class);
        assertCorrectMode(t.only(), Timeout.class, 50, 25, Only.class);
    }

// org.mockitousage.PlaygroundTest::spyInAction
    public void spyInAction() {

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

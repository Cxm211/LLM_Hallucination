// buggy code
    public <T> T createMock(MockCreationSettings<T> settings, MockHandler handler) {
        if (settings.getSerializableMode() == SerializableMode.ACROSS_CLASSLOADERS) {
            throw new MockitoException("Serialization across classloaders not yet supported with ByteBuddyMockMaker");
        }
        Class<? extends T> mockedProxyType = cachingMockBytecodeGenerator.get(
                settings.getTypeToMock(),
                settings.getExtraInterfaces()
        );
        T mockInstance = null;
        try {
            mockInstance = classInstantiator.instantiate(mockedProxyType);
            MockMethodInterceptor.MockAccess mockAccess = (MockMethodInterceptor.MockAccess) mockInstance;
            mockAccess.setMockitoInterceptor(new MockMethodInterceptor(asInternalMockHandler(handler), settings));

            return ensureMockIsAssignableToMockedType(settings, mockInstance);
        } catch (ClassCastException cce) {
            throw new MockitoException(join(
                    "ClassCastException occurred while creating the mockito mock :",
                    "  class to mock : " + describeClass(mockedProxyType),
                    "  created class : " + describeClass(settings.getTypeToMock()),
                    "  proxy instance class : " + describeClass(mockInstance),
                    "  instance creation by : " + classInstantiator.getClass().getSimpleName(),
                    "",
                    "You might experience classloading issues, please ask the mockito mailing-list.",
                    ""
            ),cce);
        } catch (org.mockito.internal.creation.instance.InstantiationException e) {
            throw new MockitoException("Unable to create mock instance of type '" + mockedProxyType.getSuperclass().getSimpleName() + "'", e);
        }
    }

// relevant test
// org.concurrentmockito.ThreadVerifiesContinuoslyInteractingMockTest::shouldAllowVerifyingInThreads
    public void shouldAllowVerifyingInThreads() throws Exception {
        for(int i = 0; i < 100; i++) {
            performTest();
        }
    }

// org.concurrentmockito.ThreadsRunAllTestsHalfManualTest::shouldRunInMultipleThreads
    public void shouldRunInMultipleThreads() {}

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

// org.mockito.ArgumentCaptorTest::tell_handy_return_values_to_return_value_for
    public void tell_handy_return_values_to_return_value_for() throws Exception {
        
        final Object expected = new Object(); 
        ArgumentCaptor<Object> argumentCaptor = ArgumentCaptor.forClass(Object.class);
        argumentCaptor.handyReturnValues = will_return(expected);
        
        
        Object returned = argumentCaptor.capture();

        
        assertEquals(expected, returned);
    }

// org.mockito.MockingDetailsTest::should_provide_invocations
	public void should_provide_invocations() {
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

// org.mockito.MockingDetailsTest::should_handle_null_input
    public void should_handle_null_input() {
        
        
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

// org.mockito.exceptions.base.MockitoAssertionErrorTest::shouldKeepUnfilteredStackTrace
    public void shouldKeepUnfilteredStackTrace() {
        try {
            throwIt();
            fail();
        } catch (MockitoAssertionError e) {
            assertEquals("throwIt", e.getUnfilteredStackTrace()[0].getMethodName());
        }
    }

// org.mockito.exceptions.base.MockitoExceptionTest::shouldKeepUnfilteredStackTrace
    public void shouldKeepUnfilteredStackTrace() {
        try {
            throwIt();
            fail();
        } catch (MockitoException e) {
            assertEquals("throwIt", e.getUnfilteredStackTrace()[0].getMethodName());
        }
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

// org.mockito.internal.configuration.ClassPathLoaderTest::shouldReadConfigurationClassFromClassPath
    public void shouldReadConfigurationClassFromClassPath() {
        ConfigurationAccess.getConfig().overrideDefaultAnswer(new Answer<Object>() {
            public Object answer(InvocationOnMock invocation) {
                return "foo";
            }});

        IMethods mock = mock(IMethods.class);
        assertEquals("foo", mock.simpleMethod());
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

// org.mockito.internal.configuration.injection.ConstructorInjectionTest::should_do_the_trick_of_instantiating
    public void should_do_the_trick_of_instantiating() throws Exception {
        given(resolver.resolveTypeInstances(Matchers.<Class<?>[]>anyVararg())).willReturn(new Object[] { observer });

        boolean result = underTest.process(field("whatever"), this, newSetOf(observer));

        assertTrue(result);
        assertNotNull(whatever);
    }

// org.mockito.internal.configuration.plugins.PluginFileReaderTest::no_class_in_resource
    public void no_class_in_resource() throws IOException {
        
        assertNull(reader.readPluginClass(impl("")));
        assertNull(reader.readPluginClass(impl("  ")));
        assertNull(reader.readPluginClass(impl(" \n ")));

        
        assertNull(reader.readPluginClass(impl("#foo")));
        assertNull(reader.readPluginClass(impl("  # foo  ")));
        assertNull(reader.readPluginClass(impl("  # # # java.langString # ")));
        assertNull(reader.readPluginClass(impl("  \n # foo \n # foo \n ")));
    }

// org.mockito.internal.configuration.plugins.PluginFileReaderTest::reads_class_name
    public void reads_class_name() throws IOException {
        assertEquals("java.lang.String", reader.readPluginClass(impl("java.lang.String")));
        assertEquals("x", reader.readPluginClass(impl("x")));
        assertEquals("x y z", reader.readPluginClass(impl(" x y z ")));
        assertEquals("foo.Foo", reader.readPluginClass(impl(" #my class\n  foo.Foo \n #other class ")));
        assertEquals("foo.Foo", reader.readPluginClass(impl("foo.Foo  # cool class")));
    }

// org.mockito.internal.configuration.plugins.PluginFinderTest::empty_resources
    @Test public void empty_resources() {
        assertNull(finder.findPluginClass((Iterable) asList()));
    }

// org.mockito.internal.configuration.plugins.PluginFinderTest::no_valid_impl
    @Test public void no_valid_impl() throws Exception {
        File f = tmp.newFile();

        
        IOUtil.writeText("  \n  ", f);

        
        assertNull(finder.findPluginClass((Iterable) asList(f.toURI().toURL())));
    }

// org.mockito.internal.configuration.plugins.PluginFinderTest::single_implementation
    @Test public void single_implementation() throws Exception {
        File f = tmp.newFile();
        when(switcher.isEnabled("foo.Foo")).thenReturn(true);

        
        IOUtil.writeText("  foo.Foo  ", f);

        
        assertEquals("foo.Foo", finder.findPluginClass((Iterable) asList(f.toURI().toURL())));
    }

// org.mockito.internal.configuration.plugins.PluginFinderTest::single_implementation_disabled
    @Test public void single_implementation_disabled() throws Exception {
        File f = tmp.newFile();
        when(switcher.isEnabled("foo.Foo")).thenReturn(false);

        
        IOUtil.writeText("  foo.Foo  ", f);

        
        assertEquals(null, finder.findPluginClass((Iterable) asList(f.toURI().toURL())));
    }

// org.mockito.internal.configuration.plugins.PluginFinderTest::multiple_implementations_only_one_enabled
    @Test public void multiple_implementations_only_one_enabled() throws Exception {
        File f1 = tmp.newFile(); File f2 = tmp.newFile();

        when(switcher.isEnabled("Bar")).thenReturn(true);

        
        IOUtil.writeText("Foo", f1); IOUtil.writeText("Bar", f2);

        
        assertEquals("Bar", finder.findPluginClass((Iterable) asList(f1.toURI().toURL(), f2.toURI().toURL())));
    }

// org.mockito.internal.configuration.plugins.PluginFinderTest::multiple_implementations_only_one_useful
    @Test public void multiple_implementations_only_one_useful() throws Exception {
        File f1 = tmp.newFile(); File f2 = tmp.newFile();

        when(switcher.isEnabled(anyString())).thenReturn(true);

        
        IOUtil.writeText("   ", f1); IOUtil.writeText("X", f2);

        
        assertEquals("X", finder.findPluginClass((Iterable) asList(f1.toURI().toURL(), f2.toURI().toURL())));
    }

// org.mockito.internal.configuration.plugins.PluginFinderTest::multiple_empty_implementations
    @Test public void multiple_empty_implementations() throws Exception {
        File f1 = tmp.newFile(); File f2 = tmp.newFile();

        when(switcher.isEnabled(anyString())).thenReturn(true);

        
        IOUtil.writeText("   ", f1); IOUtil.writeText("\n", f2);

        
        assertEquals(null, finder.findPluginClass((Iterable) asList(f1.toURI().toURL(), f2.toURI().toURL())));
    }

// org.mockito.internal.configuration.plugins.PluginFinderTest::problems_loading_impl
    @Test public void problems_loading_impl() throws Exception {
        when(switcher.isEnabled(anyString())).thenThrow(new RuntimeException("Boo!"));

        try {
            
            finder.findPluginClass((Iterable) asList(new File("xxx").toURI().toURL()));
            
            fail();
        } catch(Exception e) {
            assertContains("xxx", e.getMessage());
            e.getCause().getMessage().equals("Boo!");
        }
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
    	mockSettingsImpl.invocationListeners((InvocationListener[])null);
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

// org.mockito.internal.creation.bytebuddy.ByteBuddyMockMakerTest::report_issue_when_trying_to_load_objenesis
    public void report_issue_when_trying_to_load_objenesis() throws Exception {
        
        ClassLoader classpath_without_objenesis = ClassLoaders.excludingClassLoader()
                .withCodeSourceUrlOf(Mockito.class, ByteBuddy.class)
                .without("org.objenesis")
                .build();
        boolean initialize_class = true;

        Class<?> mock_maker_class_loaded_fine_until = Class.forName(
                "org.mockito.internal.creation.bytebuddy.ByteBuddyMockMaker",
                initialize_class,
                classpath_without_objenesis
        );

        
        try {
            mock_maker_class_loaded_fine_until.newInstance();
            fail();
        } catch (Throwable e) {
            
            assertThat(e).isInstanceOf(IllegalStateException.class);
            assertThat(e.getMessage()).containsIgnoringCase("objenesis").contains("missing");
        }
    }

// org.mockito.internal.creation.bytebuddy.ByteBuddyMockMakerTest::instantiate_fine_when_objenesis_on_the_classpath
    public void instantiate_fine_when_objenesis_on_the_classpath() throws Exception {
        
        ClassLoader classpath_without_objenesis = ClassLoaders.excludingClassLoader()
                .withCodeSourceUrlOf(Mockito.class, ByteBuddy.class, ObjenesisStd.class)
                .build();
        boolean initialize_class = true;

        Class<?> mock_maker_class_loaded_fine_until = Class.forName(
                "org.mockito.internal.creation.bytebuddy.ByteBuddyMockMaker",
                initialize_class,
                classpath_without_objenesis
        );

        
        mock_maker_class_loaded_fine_until.newInstance();

        
    }

// org.mockito.internal.creation.cglib.ClassImposterizerTest::shouldCreateMockFromInterface
    public void shouldCreateMockFromInterface() throws Exception {
        SomeInterface proxy = imposterizer.imposterise(new MethodInterceptorStub(), SomeInterface.class);
        
        Class superClass = proxy.getClass().getSuperclass();
        assertEquals(Object.class, superClass);
    }

// org.mockito.internal.creation.cglib.ClassImposterizerTest::shouldCreateMockFromClass
    public void shouldCreateMockFromClass() throws Exception {
        ClassWithoutConstructor proxy = imposterizer.imposterise(new MethodInterceptorStub(), ClassWithoutConstructor.class);
        
        Class superClass = proxy.getClass().getSuperclass();
        assertEquals(ClassWithoutConstructor.class, superClass);
    }

// org.mockito.internal.creation.cglib.ClassImposterizerTest::shouldCreateMockFromClassEvenWhenConstructorIsDodgy
    public void shouldCreateMockFromClassEvenWhenConstructorIsDodgy() throws Exception {
        try {
            new ClassWithDodgyConstructor();
            fail();
        } catch (Exception e) {}
        
        ClassWithDodgyConstructor mock = imposterizer.imposterise(new MethodInterceptorStub(), ClassWithDodgyConstructor.class);
        assertNotNull(mock);
    }

// org.mockito.internal.creation.cglib.ClassImposterizerTest::shouldMocksHaveDifferentInterceptors
    public void shouldMocksHaveDifferentInterceptors() throws Exception {
        SomeClass mockOne = imposterizer.imposterise(new MethodInterceptorStub(), SomeClass.class);
        SomeClass mockTwo = imposterizer.imposterise(new MethodInterceptorStub(), SomeClass.class);
        
        Factory cglibFactoryOne = (Factory) mockOne;
        Factory cglibFactoryTwo = (Factory) mockTwo;
        
        assertNotSame(cglibFactoryOne.getCallback(0), cglibFactoryTwo.getCallback(0));
    }

// org.mockito.internal.creation.cglib.ClassImposterizerTest::shouldUseAnicilliaryTypes
    public void shouldUseAnicilliaryTypes() {
        SomeClass mock = imposterizer.imposterise(new MethodInterceptorStub(), SomeClass.class, SomeInterface.class);
        
        assertThat(mock, is(instanceOf(SomeInterface.class)));
    }

// org.mockito.internal.creation.cglib.ClassImposterizerTest::shouldCreateClassByConstructor
    public void shouldCreateClassByConstructor() {
        imposterizer = new ClassImposterizer(new ConstructorInstantiator(null));
        OtherClass mock = imposterizer.imposterise(new MethodInterceptorStub(), OtherClass.class);
        assertNotNull(mock);
    }

// org.mockito.internal.creation.cglib.MethodInterceptorFilterTest::shouldBeSerializable
    public void shouldBeSerializable() throws Exception {
        new ObjectOutputStream(new ByteArrayOutputStream()).writeObject(new MethodInterceptorFilter(null, null));
    }

// org.mockito.internal.creation.cglib.MethodInterceptorFilterTest::shouldProvideOwnImplementationOfHashCode
    public void shouldProvideOwnImplementationOfHashCode() throws Throwable {
        
        Object ret = filter.intercept(new MethodsImpl(), MethodsImpl.class.getMethod("hashCode"), new Object[0], null);

        
        assertTrue((Integer) ret != 0);
        Mockito.verify(handler, never()).handle(any(InvocationImpl.class));
    }

// org.mockito.internal.creation.cglib.MethodInterceptorFilterTest::shouldProvideOwnImplementationOfEquals
    public void shouldProvideOwnImplementationOfEquals() throws Throwable {
        
        MethodsImpl proxy = new MethodsImpl();
        Object ret = filter.intercept(proxy, MethodsImpl.class.getMethod("equals", Object.class), new Object[] {proxy}, null);

        
        assertTrue((Boolean) ret);
        Mockito.verify(handler, never()).handle(any(InvocationImpl.class));
    }

// org.mockito.internal.creation.cglib.MethodInterceptorFilterTest::shouldCreateSerializableMethodProxyIfIsSerializableMock
    public void shouldCreateSerializableMethodProxyIfIsSerializableMock() throws Exception {
        MethodInterceptorFilter filter = new MethodInterceptorFilter(handler, (MockSettingsImpl) withSettings().serializable());
        MethodProxy methodProxy = MethodProxy.create(String.class, String.class, "", "toString", "toString");
        
        
        MockitoMethodProxy mockitoMethodProxy = filter.createMockitoMethodProxy(methodProxy);
        
        
        assertThat(mockitoMethodProxy, instanceOf(SerializableMockitoMethodProxy.class));
    }

// org.mockito.internal.creation.cglib.MethodInterceptorFilterTest::shouldCreateNONSerializableMethodProxyIfIsNotSerializableMock
    public void shouldCreateNONSerializableMethodProxyIfIsNotSerializableMock() throws Exception {
        MethodInterceptorFilter filter = new MethodInterceptorFilter(handler, (MockSettingsImpl) withSettings());
        MethodProxy methodProxy = MethodProxy.create(String.class, String.class, "", "toString", "toString");
        
        
        MockitoMethodProxy mockitoMethodProxy = filter.createMockitoMethodProxy(methodProxy);
        
        
        assertThat(mockitoMethodProxy, instanceOf(DelegatingMockitoMethodProxy.class));
    }

// org.mockito.internal.creation.cglib.MethodInterceptorFilterTest::shouldCreateSerializableMethodIfIsSerializableMock
    public void shouldCreateSerializableMethodIfIsSerializableMock() throws Exception {
        MethodInterceptorFilter filter = new MethodInterceptorFilter(handler, (MockSettingsImpl) withSettings().serializable());
        Method method = new InvocationBuilder().toInvocation().getMethod();
        
        
        MockitoMethod mockitoMethod = filter.createMockitoMethod(method);
        
        
        assertThat(mockitoMethod, instanceOf(SerializableMethod.class));
    }

// org.mockito.internal.creation.cglib.MethodInterceptorFilterTest::shouldCreateJustDelegatingMethodIfIsNotSerializableMock
    public void shouldCreateJustDelegatingMethodIfIsNotSerializableMock() throws Exception {
        MethodInterceptorFilter filter = new MethodInterceptorFilter(handler, (MockSettingsImpl) withSettings());
        Method method = new InvocationBuilder().toInvocation().getMethod();
        
        
        MockitoMethod mockitoMethod = filter.createMockitoMethod(method);
        
        
        assertThat(mockitoMethod, instanceOf(DelegatingMethod.class));
    }

// org.mockito.internal.creation.cglib.SerializableMockitoMethodProxyTest::shouldCreateCorrectCreationInfo
    public void shouldCreateCorrectCreationInfo() throws Exception {
        
        MethodProxy proxy = MethodProxy.create(String.class, Integer.class, "", "", "");
        SerializableMockitoMethodProxy serializableMockitoMethodProxy = new SerializableMockitoMethodProxy(proxy);
        serializableMockitoMethodProxy.methodProxy = null;

        
        Object methodProxy = Whitebox.invokeMethod(serializableMockitoMethodProxy, "getMethodProxy",  new Object[0]);

        
        Object info = Whitebox.getInternalState(methodProxy, "createInfo");
        assertEquals(String.class, Whitebox.getInternalState(info, "c1"));
        assertEquals(Integer.class, Whitebox.getInternalState(info, "c2"));
    }

// org.mockito.internal.creation.cglib.SerializableMockitoMethodProxyTest::shouldCreateCorrectSignatures
    public void shouldCreateCorrectSignatures() throws Exception {
        
        MethodProxy proxy = MethodProxy.create(String.class, Integer.class, "a", "b", "c");
        SerializableMockitoMethodProxy serializableMockitoMethodProxy = new SerializableMockitoMethodProxy(proxy);
        serializableMockitoMethodProxy.methodProxy = null;

        
        MethodProxy methodProxy = (MethodProxy) Whitebox.invokeMethod(serializableMockitoMethodProxy, "getMethodProxy",  new Object[0]);

        
        assertEquals("a", methodProxy.getSignature().getDescriptor());
        assertEquals("b", methodProxy.getSignature().getName());
        assertEquals("c", methodProxy.getSuperName());
    }

// org.mockito.internal.creation.instance.ConstructorInstantiatorTest::creates_instances
    @Test public void creates_instances() {
        assertEquals(new ConstructorInstantiator(null).newInstance(SomeClass.class).getClass(), SomeClass.class);
    }

// org.mockito.internal.creation.instance.ConstructorInstantiatorTest::creates_instances_of_inner_classes
    @Test public void creates_instances_of_inner_classes() {
        assertEquals(new ConstructorInstantiator(this).newInstance(SomeInnerClass.class).getClass(), SomeInnerClass.class);
        assertEquals(new ConstructorInstantiator(new ChildOfThis()).newInstance(SomeInnerClass.class).getClass(), SomeInnerClass.class);
    }

// org.mockito.internal.creation.instance.ConstructorInstantiatorTest::explains_when_constructor_cannot_be_found
    @Test public void explains_when_constructor_cannot_be_found() {
        try {
            new ConstructorInstantiator(null).newInstance(SomeClass2.class);
            fail();
        } catch (InstantiationException e) {
            assertContains("Unable to create instance of 'SomeClass2'.\n" +
                    "Please ensure it has 0-arg constructor which invokes cleanly.", e.getMessage());
        }
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

// org.mockito.internal.exceptions.stacktrace.ConditionalStackTraceFilterTest::shouldNotFilterWhenConfigurationSaysNo
    public void shouldNotFilterWhenConfigurationSaysNo() {
        ConfigurationAccess.getConfig().overrideCleansStackTrace(false);
        
        Throwable t = new TraceBuilder().classes(
                "org.test.MockitoSampleTest",
                "org.mockito.Mockito" 
        ).toThrowable();
        
        filter.filter(t);
        
        assertThat(t, hasOnlyThoseClassesInStackTrace("org.mockito.Mockito", "org.test.MockitoSampleTest"));
    }

// org.mockito.internal.exceptions.stacktrace.ConditionalStackTraceFilterTest::shouldFilterWhenConfigurationSaysYes
    public void shouldFilterWhenConfigurationSaysYes() {
        ConfigurationAccess.getConfig().overrideCleansStackTrace(true);
        
        Throwable t = new TraceBuilder().classes(
                "org.test.MockitoSampleTest",
                "org.mockito.Mockito" 
        ).toThrowable();
        
        filter.filter(t);
        
        assertThat(t, hasOnlyThoseClassesInStackTrace("org.test.MockitoSampleTest"));
    }

// org.mockito.internal.exceptions.stacktrace.StackTraceFilterTest::shouldFilterOutCglibGarbage
    public void shouldFilterOutCglibGarbage() {
        StackTraceElement[] t = new TraceBuilder().classes(
            "MockitoExampleTest",
            "List$$EnhancerByMockitoWithCGLIB$$2c406024"
        ).toTraceArray();
        
        StackTraceElement[] filtered = filter.filter(t, false);
        
        assertThat(filtered, hasOnlyThoseClasses("MockitoExampleTest"));
    }

// org.mockito.internal.exceptions.stacktrace.StackTraceFilterTest::shouldFilterOutMockitoPackage
    public void shouldFilterOutMockitoPackage() {
        StackTraceElement[] t = new TraceBuilder().classes(
            "org.test.MockitoSampleTest",
            "org.mockito.Mockito"
        ).toTraceArray();
            
        StackTraceElement[] filtered = filter.filter(t, false);
        
        assertThat(filtered, hasOnlyThoseClasses("org.test.MockitoSampleTest"));
    }

// org.mockito.internal.exceptions.stacktrace.StackTraceFilterTest::shouldFilterOutTracesMiddleBadTraces
    public void shouldFilterOutTracesMiddleBadTraces() {
        StackTraceElement[] t = new TraceBuilder().classes(
                "org.test.MockitoSampleTest",
                "org.test.TestSupport",
                "org.mockito.Mockito", 
                "org.test.TestSupport",
                "org.mockito.Mockito"
        ).toTraceArray();
        
        StackTraceElement[] filtered = filter.filter(t, false);
        
        assertThat(filtered, hasOnlyThoseClasses("org.test.TestSupport", "org.test.MockitoSampleTest"));
    }

// org.mockito.internal.exceptions.stacktrace.StackTraceFilterTest::shouldKeepRunners
    public void shouldKeepRunners() {
        StackTraceElement[] t = new TraceBuilder().classes(
                "org.mockito.runners.Runner",
                "junit.stuff",
                "org.test.MockitoSampleTest",
                "org.mockito.Mockito"
        ).toTraceArray();
        
        StackTraceElement[] filtered = filter.filter(t, false);
        
        assertThat(filtered, hasOnlyThoseClasses("org.test.MockitoSampleTest", "junit.stuff", "org.mockito.runners.Runner"));
    }

// org.mockito.internal.exceptions.stacktrace.StackTraceFilterTest::shouldKeepInternalRunners
    public void shouldKeepInternalRunners() {
        StackTraceElement[] t = new TraceBuilder().classes(
                "org.mockito.internal.runners.Runner",
                "org.test.MockitoSampleTest"
        ).toTraceArray();
        
        StackTraceElement[] filtered = filter.filter(t, false);
        
        assertThat(filtered, hasOnlyThoseClasses("org.test.MockitoSampleTest", "org.mockito.internal.runners.Runner"));
    }

// org.mockito.internal.exceptions.stacktrace.StackTraceFilterTest::shouldStartFilteringAndKeepTop
    public void shouldStartFilteringAndKeepTop() {
        
        StackTraceElement[] t = new TraceBuilder().classes(
                "org.test.Good",
                "org.mockito.internal.Bad",
                "org.test.MockitoSampleTest"
        ).toTraceArray();
        
        
        StackTraceElement[] filtered = filter.filter(t, true);
        
        
        assertThat(filtered, hasOnlyThoseClasses("org.test.MockitoSampleTest", "org.test.Good"));
    }

// org.mockito.internal.exceptions.stacktrace.StackTraceFilterTest::shouldKeepGoodTraceFromTheTopBecauseRealImplementationsOfSpiesSometimesThrowExceptions
    public void shouldKeepGoodTraceFromTheTopBecauseRealImplementationsOfSpiesSometimesThrowExceptions() {
        StackTraceElement[] t = new TraceBuilder().classes(
                "org.good.Trace",
                "org.yet.another.good.Trace",
                "org.mockito.internal.to.be.Filtered",
                "org.test.MockitoSampleTest"
        ).toTraceArray();
        
        StackTraceElement[] filtered = filter.filter(t, true);
        
        assertThat(filtered, hasOnlyThoseClasses(
                "org.test.MockitoSampleTest",
                "org.yet.another.good.Trace",
                "org.good.Trace"
                ));
    }

// org.mockito.internal.exceptions.stacktrace.StackTraceFilterTest::shouldReturnEmptyArrayWhenInputIsEmpty
    public void shouldReturnEmptyArrayWhenInputIsEmpty() throws Exception {
        
        StackTraceElement[] filtered = filter.filter(new StackTraceElement[0], false);
        
        assertEquals(0, filtered.length);
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

// org.mockito.internal.invocation.InvocationImplTest::shouldReturnCastedArgumentAt
    public void shouldReturnCastedArgumentAt(){
        
        int argument = 42;
        Invocation invocationOnInterface = new InvocationBuilder().method("twoArgumentMethod").
            argTypes(int.class, int.class).args(1, argument).toInvocation();

        
        int secondArgument = invocationOnInterface.getArgumentAt(1, int.class);

        
        assertTrue(secondArgument == argument);
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

// org.mockito.internal.invocation.SerializableMethodTest::shouldBeSerializable
    public void shouldBeSerializable() throws Exception {
        ByteArrayOutputStream serialized = new ByteArrayOutputStream();
        new ObjectOutputStream(serialized).writeObject(method);
    }

// org.mockito.internal.invocation.SerializableMethodTest::shouldBeAbleToRetrieveMethodExceptionTypes
    public void shouldBeAbleToRetrieveMethodExceptionTypes() throws Exception {
        assertArrayEquals(toStringMethod.getExceptionTypes(), method.getExceptionTypes());
    }

// org.mockito.internal.invocation.SerializableMethodTest::shouldBeAbleToRetrieveMethodName
    public void shouldBeAbleToRetrieveMethodName() throws Exception {
        assertEquals(toStringMethod.getName(), method.getName());
    }

// org.mockito.internal.invocation.SerializableMethodTest::shouldBeAbleToCheckIsArgVargs
    public void shouldBeAbleToCheckIsArgVargs() throws Exception {
        assertEquals(toStringMethod.isVarArgs(), method.isVarArgs());
    }

// org.mockito.internal.invocation.SerializableMethodTest::shouldBeAbleToGetParameterTypes
    public void shouldBeAbleToGetParameterTypes() throws Exception {
        assertArrayEquals(toStringMethod.getParameterTypes(), method.getParameterTypes());
    }

// org.mockito.internal.invocation.SerializableMethodTest::shouldBeAbleToGetReturnType
    public void shouldBeAbleToGetReturnType() throws Exception {
        assertEquals(toStringMethod.getReturnType(), method.getReturnType());
    }

// org.mockito.internal.invocation.SerializableMethodTest::shouldBeEqualForTwoInstances
    public void shouldBeEqualForTwoInstances() throws Exception {
        assertTrue(new SerializableMethod(toStringMethod).equals(method));
    }

// org.mockito.internal.invocation.SerializableMethodTest::shouldNotBeEqualForSameMethodFromTwoDifferentClasses
    public void shouldNotBeEqualForSameMethodFromTwoDifferentClasses() throws Exception {
        Method testBaseToStringMethod = String.class.getMethod("toString", args);
        assertFalse(new SerializableMethod(testBaseToStringMethod).equals(method));
    }

// org.mockito.internal.junit.JUnitRuleTest::shouldInjectIntoTestCase
    public void shouldInjectIntoTestCase() throws Throwable {
        jUnitRule.apply(new DummyStatement(), injectTestCase).evaluate();
        assertNotNull("@Mock mock object created", injectTestCase.getInjected());
        assertNotNull("@InjectMocks object created", injectTestCase.getInjectInto());
        assertNotNull("Mock injected into the object", injectTestCase.getInjectInto().getInjected());
    }

// org.mockito.internal.junit.JUnitRuleTest::shouldRethrowException
    public void shouldRethrowException() throws Throwable {
        try {
            jUnitRule.apply(new ExceptionStatement(), injectTestCase).evaluate();
            fail("Should throw exception");
        } catch (RuntimeException e) {
            assertEquals("Correct message", "Statement exception", e.getMessage());
        }
    }

// org.mockito.internal.junit.JUnitRuleTest::shouldDetectUnfinishedStubbing
    public void shouldDetectUnfinishedStubbing() throws Throwable {
        try {
            jUnitRule.apply(new UnfinishedStubbingStatement(), injectTestCase).evaluate();
            fail("Should detect invalid Mockito usage");
        } catch (UnfinishedStubbingException e) {
        }
    }

// org.mockito.internal.junit.JUnitRuleTest::dummy
        public void dummy() throws Exception {
        }

// org.mockito.internal.matchers.CapturingMatcherTest::should_capture_arguments
    public void should_capture_arguments() throws Exception {
        
        CapturingMatcher m = new CapturingMatcher();
        
        
        m.captureFrom("foo");
        m.captureFrom("bar");
        
        
        Assertions.assertThat(m.getAllValues()).containsSequence("foo", "bar");
    }

// org.mockito.internal.matchers.CapturingMatcherTest::should_know_last_captured_value
    public void should_know_last_captured_value() throws Exception {
        
        CapturingMatcher m = new CapturingMatcher();
        
        
        m.captureFrom("foo");
        m.captureFrom("bar");
        
        
        assertEquals("bar", m.getLastValue());
    }

// org.mockito.internal.matchers.CapturingMatcherTest::should_scream_when_nothing_yet_captured
    public void should_scream_when_nothing_yet_captured() throws Exception {
        
        CapturingMatcher m = new CapturingMatcher();

        try {
            
            m.getLastValue();
            
            fail();
        } catch (MockitoException e) {}
    }

// org.mockito.internal.matchers.ComparableMatchersTest::testLessThan
    public void testLessThan() {
        test(new LessThan<String>("b"), true, false, false, "lt");
    }

// org.mockito.internal.matchers.ComparableMatchersTest::testGreaterThan
    public void testGreaterThan() {
        test(new GreaterThan<String>("b"), false, true, false, "gt");
    }

// org.mockito.internal.matchers.ComparableMatchersTest::testLessOrEqual
    public void testLessOrEqual() {
        test(new LessOrEqual<String>("b"), true, false, true, "leq");
    }

// org.mockito.internal.matchers.ComparableMatchersTest::testGreaterOrEqual
    public void testGreaterOrEqual() {
        test(new GreaterOrEqual<String>("b"), false, true, true, "geq");
    }

// org.mockito.internal.matchers.ComparableMatchersTest::testCompareEqual
    public void testCompareEqual() {
        test(new CompareEqual<String>("b"), false, false, true, "cmpEq");

        
        CompareEqual<BigDecimal> cmpEq = new CompareEqual<BigDecimal>(new BigDecimal("5.00"));
        assertTrue(cmpEq.matches(new BigDecimal("5")));
    }

// org.mockito.internal.matchers.EqualityTest::shouldKnowIfObjectsAreEqual
    public void shouldKnowIfObjectsAreEqual() throws Exception {
        int[] arr = new int[] {1, 2};
        assertTrue(areEqual(arr, arr));
        assertTrue(areEqual(new int[] {1, 2}, new int[] {1, 2}));
        assertTrue(areEqual(new Double[] {1.0}, new Double[] {1.0}));
        assertTrue(areEqual(new String[0], new String[0]));
        assertTrue(areEqual(new Object[10], new Object[10]));
        assertTrue(areEqual(new int[] {1}, new Integer[] {1}));
        assertTrue(areEqual(new Object[] {"1"}, new String[] {"1"}));
	Object badequals=new BadEquals();
	assertTrue(areEqual(badequals,badequals));

        assertFalse(areEqual(new Object[9], new Object[10]));
        assertFalse(areEqual(new int[] {1, 2}, new int[] {1}));
        assertFalse(areEqual(new int[] {1}, new double[] {1.0}));
    }

// org.mockito.internal.matchers.EqualsTest::shouldBeEqual
    public void shouldBeEqual() {
        assertEquals(new Equals(null), new Equals(null));
        assertEquals(new Equals(new Integer(2)), new Equals(new Integer(2)));
        assertFalse(new Equals(null).equals(null));
        assertFalse(new Equals(null).equals("Test"));
        assertEquals(1, new Equals(null).hashCode());
    }

// org.mockito.internal.matchers.EqualsTest::shouldArraysBeEqual
    public void shouldArraysBeEqual() {
        assertTrue(new Equals(new int[] {1, 2}).matches(new int[] {1, 2}));
        assertFalse(new Equals(new Object[] {"1"}).matches(new Object[] {"1.0"}));
    }

// org.mockito.internal.matchers.EqualsTest::shouldDescribeWithExtraTypeInfo
    public void shouldDescribeWithExtraTypeInfo() throws Exception {
        String descStr = describe(new Equals(100).withExtraTypeInfo());
        
        assertEquals("(Integer) 100", descStr);
    }

// org.mockito.internal.matchers.EqualsTest::shouldDescribeWithExtraTypeInfoOfLong
    public void shouldDescribeWithExtraTypeInfoOfLong() throws Exception {
        String descStr = describe(new Equals(100L).withExtraTypeInfo());
        
        assertEquals("(Long) 100", descStr);
    }

// org.mockito.internal.matchers.EqualsTest::shouldAppendQuotingForString
    public void shouldAppendQuotingForString() {
        String descStr = describe(new Equals("str"));
        
        assertEquals("\"str\"", descStr);
    }

// org.mockito.internal.matchers.EqualsTest::shouldAppendQuotingForChar
    public void shouldAppendQuotingForChar() {
        String descStr = describe(new Equals('s'));
        
        assertEquals("'s'", descStr);
    }

// org.mockito.internal.matchers.EqualsTest::shouldDescribeUsingToString
    public void shouldDescribeUsingToString() {
        String descStr = describe(new Equals(100));
        
        assertEquals("100", descStr);
    }

// org.mockito.internal.matchers.EqualsTest::shouldDescribeNull
    public void shouldDescribeNull() {
        String descStr = describe(new Equals(null));
        
        assertEquals("null", descStr);
    }

// org.mockito.internal.matchers.EqualsTest::shouldMatchTypes
    public void shouldMatchTypes() throws Exception {
        
        ContainsExtraTypeInformation equals = new Equals(10);
        
        
        assertTrue(equals.typeMatches(10));
        assertFalse(equals.typeMatches(10L));
    }

// org.mockito.internal.matchers.EqualsTest::shouldMatchTypesSafelyWhenActualIsNull
    public void shouldMatchTypesSafelyWhenActualIsNull() throws Exception {
        
        ContainsExtraTypeInformation equals = new Equals(null);
        
        
        assertFalse(equals.typeMatches(10));
    }

// org.mockito.internal.matchers.EqualsTest::shouldMatchTypesSafelyWhenGivenIsNull
    public void shouldMatchTypesSafelyWhenGivenIsNull() throws Exception {
        
        ContainsExtraTypeInformation equals = new Equals(10);
        
        
        assertFalse(equals.typeMatches(null));
    }

// org.mockito.internal.matchers.LocalizedMatcherTest::shouldMatchTypesWhenActualMatcherHasCorrectType
    public void shouldMatchTypesWhenActualMatcherHasCorrectType() throws Exception {
        
        ContainsExtraTypeInformation equals10 = new Equals(10);
        LocalizedMatcher m = new LocalizedMatcher((Matcher) equals10);
        
        
        assertTrue(m.typeMatches(10));
        assertFalse(m.typeMatches(10L));
    }

// org.mockito.internal.matchers.LocalizedMatcherTest::shouldNotMatchTypesWhenActualMatcherDoesNotHaveCorrectType
    public void shouldNotMatchTypesWhenActualMatcherDoesNotHaveCorrectType() throws Exception {
        
        LocalizedMatcher m = new LocalizedMatcher(Any.ANY);
        
        
        assertFalse(m.typeMatches(10));
    }

// org.mockito.internal.matchers.LocalizedMatcherTest::shouldDescribeWithTypeInfoWhenActualMatcherHasCorrectType
    public void shouldDescribeWithTypeInfoWhenActualMatcherHasCorrectType() throws Exception {
        
        ContainsExtraTypeInformation equals10 = new Equals(10);
        LocalizedMatcher m = new LocalizedMatcher((Matcher) equals10);
        
        
        assertEquals("(Integer) 10", describe(m.withExtraTypeInfo()));
    }

// org.mockito.internal.matchers.LocalizedMatcherTest::shouldNotDescribeWithTypeInfoWhenActualMatcherDoesNotHaveCorrectType
    public void shouldNotDescribeWithTypeInfoWhenActualMatcherDoesNotHaveCorrectType() throws Exception {
        
        LocalizedMatcher m = new LocalizedMatcher(Any.ANY);
        
        
        assertSame(m, m.withExtraTypeInfo());
    }

// org.mockito.internal.matchers.LocalizedMatcherTest::shouldDelegateToCapturingMatcher
    public void shouldDelegateToCapturingMatcher() throws Exception {
        
        CapturingMatcher capturingMatcher = new CapturingMatcher();
        LocalizedMatcher m = new LocalizedMatcher(capturingMatcher);
        
        
        m.captureFrom("boo");
        
        
        assertEquals("boo", capturingMatcher.getLastValue());
    }

// org.mockito.internal.matchers.MatchersPrinterTest::shouldGetArgumentsLine
    public void shouldGetArgumentsLine() {
        String line = printer.getArgumentsLine((List) Arrays.asList(new Equals(1), new Equals(2)), new PrintSettings());
        assertEquals("(1, 2);", line);
    }

// org.mockito.internal.matchers.MatchersPrinterTest::shouldGetArgumentsBlock
    public void shouldGetArgumentsBlock() {
        String line = printer.getArgumentsBlock((List) Arrays.asList(new Equals(1), new Equals(2)), new PrintSettings());
        assertEquals("(\n    1,\n    2\n);", line);
    }

// org.mockito.internal.matchers.MatchersPrinterTest::shouldDescribeTypeInfoOnlyMarkedMatchers
    public void shouldDescribeTypeInfoOnlyMarkedMatchers() {
        
        String line = printer.getArgumentsLine((List) Arrays.asList(new Equals(1L), new Equals(2)), PrintSettings.verboseMatchers(1));
        
        assertEquals("(1, (Integer) 2);", line);
    }

// org.mockito.internal.matchers.MatchersPrinterTest::shouldGetVerboseArgumentsInBlock
    public void shouldGetVerboseArgumentsInBlock() {
        
        String line = printer.getArgumentsBlock((List) Arrays.asList(new Equals(1L), new Equals(2)), PrintSettings.verboseMatchers(0, 1));
        
        assertEquals("(\n    (Long) 1,\n    (Integer) 2\n);", line);
    }

// org.mockito.internal.matchers.MatchersPrinterTest::shouldGetVerboseArgumentsEvenIfSomeMatchersAreNotVerbose
    public void shouldGetVerboseArgumentsEvenIfSomeMatchersAreNotVerbose() {
        
        String line = printer.getArgumentsLine((List) Arrays.asList(new Equals(1L), NotNull.NOT_NULL), PrintSettings.verboseMatchers(0));
        
        assertEquals("((Long) 1, notNull());", line);
    }

// org.mockito.internal.matchers.MatchersToStringTest::sameToStringWithString
    public void sameToStringWithString() {
        assertEquals("same(\"X\")", describe(new Same("X")));

    }

// org.mockito.internal.matchers.MatchersToStringTest::nullToString
    public void nullToString() {
        assertEquals("isNull()", describe(Null.NULL));
    }

// org.mockito.internal.matchers.MatchersToStringTest::notNullToString
    public void notNullToString() {
        assertEquals("notNull()", describe(NotNull.NOT_NULL));
    }

// org.mockito.internal.matchers.MatchersToStringTest::anyToString
    public void anyToString() {
        assertEquals("<any>", describe(Any.ANY));
    }

// org.mockito.internal.matchers.MatchersToStringTest::sameToStringWithChar
    public void sameToStringWithChar() {
        assertEquals("same('x')", describe(new Same('x')));
    }

// org.mockito.internal.matchers.MatchersToStringTest::sameToStringWithObject
    public void sameToStringWithObject() {
        Object o = new Object() {
            @Override
            public String toString() {
                return "X";
            }
        };
        assertEquals("same(X)", describe(new Same(o)));
    }

// org.mockito.internal.matchers.MatchersToStringTest::equalsToStringWithString
    public void equalsToStringWithString() {
        assertEquals("\"X\"", describe(new Equals("X")));

    }

// org.mockito.internal.matchers.MatchersToStringTest::equalsToStringWithChar
    public void equalsToStringWithChar() {
        assertEquals("'x'", describe(new Equals('x')));
    }

// org.mockito.internal.matchers.MatchersToStringTest::equalsToStringWithObject
    public void equalsToStringWithObject() {
        Object o = new Object() {
            @Override
            public String toString() {
                return "X";
            }
        };
        assertEquals("X", describe(new Equals(o)));
    }

// org.mockito.internal.matchers.MatchersToStringTest::orToString
    public void orToString() {
        List<Matcher> matchers = new ArrayList<Matcher>();
        matchers.add(new Equals(1));
        matchers.add(new Equals(2));
        assertEquals("or(1, 2)", describe(new Or(matchers)));
    }

// org.mockito.internal.matchers.MatchersToStringTest::notToString
    public void notToString() {
        assertEquals("not(1)", describe(new Not(new Equals(1))));
    }

// org.mockito.internal.matchers.MatchersToStringTest::andToString
    public void andToString() {
        List<Matcher> matchers = new ArrayList<Matcher>();
        matchers.add(new Equals(1));
        matchers.add(new Equals(2));
        assertEquals("and(1, 2)", describe(new And(matchers)));
    }

// org.mockito.internal.matchers.MatchersToStringTest::startsWithToString
    public void startsWithToString() {
        assertEquals("startsWith(\"AB\")", describe(new StartsWith("AB")));
    }

// org.mockito.internal.matchers.MatchersToStringTest::endsWithToString
    public void endsWithToString() {
        assertEquals("endsWith(\"AB\")", describe(new EndsWith("AB")));
    }

// org.mockito.internal.matchers.MatchersToStringTest::containsToString
    public void containsToString() {
        assertEquals("contains(\"AB\")", describe(new Contains("AB")));
    }

// org.mockito.internal.matchers.MatchersToStringTest::findToString
    public void findToString() {
        assertEquals("find(\"\\\\s+\")", describe(new Find("\\s+")));
    }

// org.mockito.internal.matchers.MatchersToStringTest::matchesToString
    public void matchesToString() {
        assertEquals("matches(\"\\\\s+\")", describe(new Matches("\\s+")));
    }

// org.mockito.internal.matchers.VarargCapturingMatcherTest::should_capture_simple_arguments
    public void should_capture_simple_arguments() throws Exception {
        
        VarargCapturingMatcher m = new VarargCapturingMatcher();

        
        m.captureFrom("foo");
        m.captureFrom("bar");

        
        assertThat(m.getAllVarargs()).containsSequence(asList("foo"), asList("bar"));
    }

// org.mockito.internal.matchers.VarargCapturingMatcherTest::should_know_last_captured_vararg
    public void should_know_last_captured_vararg() throws Exception {
        
        VarargCapturingMatcher m = new VarargCapturingMatcher();

        
        m.captureFrom(new Object[] { "foo", "bar" });
        m.captureFrom(new Object[] { "fool", "bard" });

        
        assertThat(m.getLastVarargs()).containsSequence("fool", "bard");
    }

// org.mockito.internal.matchers.VarargCapturingMatcherTest::can_capture_primitive_varargs
    public void can_capture_primitive_varargs() throws Exception {
        
        VarargCapturingMatcher m = new VarargCapturingMatcher();

        
        m.captureFrom(new int[] { 1, 2, 3 });

        
        assertThat(m.getLastVarargs()).containsSequence(1, 2, 3);
    }

// org.mockito.internal.matchers.VarargCapturingMatcherTest::should_scream_when_nothing_yet_captured
    public void should_scream_when_nothing_yet_captured() throws Exception {
        
        VarargCapturingMatcher m = new VarargCapturingMatcher();

        try {
            
            m.getLastVarargs();
            
            fail();
        } catch (MockitoException e) {}
    }

// org.mockito.internal.matchers.apachecommons.EqualsBuilderTest::testname
    public void testname() throws Exception {
        
    }

// org.mockito.internal.matchers.apachecommons.EqualsBuilderTest::testReflectionEquals
    @Test public void testReflectionEquals() {
        TestObject o1 = new TestObject(4);
        TestObject o2 = new TestObject(5);
        assertTrue(EqualsBuilder.reflectionEquals(o1, o1));
        assertTrue(!EqualsBuilder.reflectionEquals(o1, o2));
        o2.setA(4);
        assertTrue(EqualsBuilder.reflectionEquals(o1, o2));

        assertTrue(!EqualsBuilder.reflectionEquals(o1, this));

        assertTrue(!EqualsBuilder.reflectionEquals(o1, null));
        assertTrue(!EqualsBuilder.reflectionEquals(null, o2));
        assertTrue(EqualsBuilder.reflectionEquals((Object) null, (Object) null));
    }

// org.mockito.internal.matchers.apachecommons.EqualsBuilderTest::testReflectionHierarchyEquals
    @Test public void testReflectionHierarchyEquals() {
        testReflectionHierarchyEquals(false);
        testReflectionHierarchyEquals(true);
        
        assertTrue(EqualsBuilder.reflectionEquals(new TestTTLeafObject(1, 2, 3, 4), new TestTTLeafObject(1, 2, 3, 4), true));
        assertTrue(EqualsBuilder.reflectionEquals(new TestTTLeafObject(1, 2, 3, 4), new TestTTLeafObject(1, 2, 3, 4), false));
        assertTrue(!EqualsBuilder.reflectionEquals(new TestTTLeafObject(1, 0, 0, 4), new TestTTLeafObject(1, 2, 3, 4), true));
        assertTrue(!EqualsBuilder.reflectionEquals(new TestTTLeafObject(1, 2, 3, 4), new TestTTLeafObject(1, 2, 3, 0), true));
        assertTrue(!EqualsBuilder.reflectionEquals(new TestTTLeafObject(0, 2, 3, 4), new TestTTLeafObject(1, 2, 3, 4), true));
    }

// org.mockito.internal.matchers.apachecommons.EqualsBuilderTest::testSuper
    @Test public void testSuper() {
        TestObject o1 = new TestObject(4);
        TestObject o2 = new TestObject(5);
        assertEquals(true, new EqualsBuilder().appendSuper(true).append(o1, o1).isEquals());
        assertEquals(false, new EqualsBuilder().appendSuper(false).append(o1, o1).isEquals());
        assertEquals(false, new EqualsBuilder().appendSuper(true).append(o1, o2).isEquals());
        assertEquals(false, new EqualsBuilder().appendSuper(false).append(o1, o2).isEquals());
    }

// org.mockito.internal.matchers.apachecommons.EqualsBuilderTest::testObject
    @Test public void testObject() {
        TestObject o1 = new TestObject(4);
        TestObject o2 = new TestObject(5);
        assertTrue(new EqualsBuilder().append(o1, o1).isEquals());
        assertTrue(!new EqualsBuilder().append(o1, o2).isEquals());
        o2.setA(4);
        assertTrue(new EqualsBuilder().append(o1, o2).isEquals());

        assertTrue(!new EqualsBuilder().append(o1, this).isEquals());
        
        assertTrue(!new EqualsBuilder().append(o1, null).isEquals());
        assertTrue(!new EqualsBuilder().append(null, o2).isEquals());
        assertTrue(new EqualsBuilder().append((Object) null, (Object) null).isEquals());
    }

// org.mockito.internal.matchers.apachecommons.EqualsBuilderTest::testLong
    @Test public void testLong() {
        long o1 = 1L;
        long o2 = 2L;
        assertTrue(new EqualsBuilder().append(o1, o1).isEquals());
        assertTrue(!new EqualsBuilder().append(o1, o2).isEquals());
    }

// org.mockito.internal.matchers.apachecommons.EqualsBuilderTest::testInt
    @Test public void testInt() {
        int o1 = 1;
        int o2 = 2;
        assertTrue(new EqualsBuilder().append(o1, o1).isEquals());
        assertTrue(!new EqualsBuilder().append(o1, o2).isEquals());
    }

// org.mockito.internal.matchers.apachecommons.EqualsBuilderTest::testShort
    @Test public void testShort() {
        short o1 = 1;
        short o2 = 2;
        assertTrue(new EqualsBuilder().append(o1, o1).isEquals());
        assertTrue(!new EqualsBuilder().append(o1, o2).isEquals());
    }

// org.mockito.internal.matchers.apachecommons.EqualsBuilderTest::testChar
    @Test public void testChar() {
        char o1 = 1;
        char o2 = 2;
        assertTrue(new EqualsBuilder().append(o1, o1).isEquals());
        assertTrue(!new EqualsBuilder().append(o1, o2).isEquals());
    }

// org.mockito.internal.matchers.apachecommons.EqualsBuilderTest::testByte
    @Test public void testByte() {
        byte o1 = 1;
        byte o2 = 2;
        assertTrue(new EqualsBuilder().append(o1, o1).isEquals());
        assertTrue(!new EqualsBuilder().append(o1, o2).isEquals());
    }

// org.mockito.internal.matchers.apachecommons.EqualsBuilderTest::testDouble
    @Test public void testDouble() {
        double o1 = 1;
        double o2 = 2;
        assertTrue(new EqualsBuilder().append(o1, o1).isEquals());
        assertTrue(!new EqualsBuilder().append(o1, o2).isEquals());
        assertTrue(!new EqualsBuilder().append(o1, Double.NaN).isEquals());
        assertTrue(new EqualsBuilder().append(Double.NaN, Double.NaN).isEquals());
        assertTrue(new EqualsBuilder().append(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY).isEquals());
    }

// org.mockito.internal.matchers.apachecommons.EqualsBuilderTest::testFloat
    @Test public void testFloat() {
        float o1 = 1;
        float o2 = 2;
        assertTrue(new EqualsBuilder().append(o1, o1).isEquals());
        assertTrue(!new EqualsBuilder().append(o1, o2).isEquals());
        assertTrue(!new EqualsBuilder().append(o1, Float.NaN).isEquals());
        assertTrue(new EqualsBuilder().append(Float.NaN, Float.NaN).isEquals());
        assertTrue(new EqualsBuilder().append(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY).isEquals());
    }

// org.mockito.internal.matchers.apachecommons.EqualsBuilderTest::testBigDecimal
    @Test public void testBigDecimal() {
        BigDecimal o1 = new BigDecimal("2.0");
        BigDecimal o2 = new BigDecimal("2.00");
        assertTrue(new EqualsBuilder().append(o1, o1).isEquals());
        assertTrue(new EqualsBuilder().append(o1, o2).isEquals());
    }

// org.mockito.internal.matchers.apachecommons.EqualsBuilderTest::testAccessors
    @Test public void testAccessors() {
        EqualsBuilder equalsBuilder = new EqualsBuilder();
        assertTrue(equalsBuilder.isEquals());
        equalsBuilder.setEquals(true);
        assertTrue(equalsBuilder.isEquals());
        equalsBuilder.setEquals(false);
        assertFalse(equalsBuilder.isEquals());
    }

// org.mockito.internal.matchers.apachecommons.EqualsBuilderTest::testBoolean
    @Test public void testBoolean() {
        boolean o1 = true;
        boolean o2 = false;
        assertTrue(new EqualsBuilder().append(o1, o1).isEquals());
        assertTrue(!new EqualsBuilder().append(o1, o2).isEquals());
    }

// org.mockito.internal.matchers.apachecommons.EqualsBuilderTest::testObjectArray
    @Test public void testObjectArray() {
        TestObject[] obj1 = new TestObject[3];
        obj1[0] = new TestObject(4);
        obj1[1] = new TestObject(5);
        obj1[2] = null;
        TestObject[] obj2 = new TestObject[3];
        obj2[0] = new TestObject(4);
        obj2[1] = new TestObject(5);
        obj2[2] = null;
        
        assertTrue(new EqualsBuilder().append(obj1, obj1).isEquals());
        assertTrue(new EqualsBuilder().append(obj2, obj2).isEquals());
        assertTrue(new EqualsBuilder().append(obj1, obj2).isEquals());
        obj1[1].setA(6);
        assertTrue(!new EqualsBuilder().append(obj1, obj2).isEquals());
        obj1[1].setA(5);
        assertTrue(new EqualsBuilder().append(obj1, obj2).isEquals());
        obj1[2] = obj1[1];
        assertTrue(!new EqualsBuilder().append(obj1, obj2).isEquals());
        obj1[2] = null;
        assertTrue(new EqualsBuilder().append(obj1, obj2).isEquals());
                       
        obj2 = null;
        assertTrue(!new EqualsBuilder().append(obj1, obj2).isEquals());
        obj1 = null;
        assertTrue(new EqualsBuilder().append(obj1, obj2).isEquals());
    }

// org.mockito.internal.matchers.apachecommons.EqualsBuilderTest::testLongArray
    @Test public void testLongArray() {
        long[] obj1 = new long[2];
        obj1[0] = 5L;
        obj1[1] = 6L;
        long[] obj2 = new long[2];
        obj2[0] = 5L;
        obj2[1] = 6L;
        assertTrue(new EqualsBuilder().append(obj1, obj1).isEquals());
        assertTrue(new EqualsBuilder().append(obj1, obj2).isEquals());
        obj1[1] = 7;
        assertTrue(!new EqualsBuilder().append(obj1, obj2).isEquals());

        obj2 = null;
        assertTrue(!new EqualsBuilder().append(obj1, obj2).isEquals());
        obj1 = null;
        assertTrue(new EqualsBuilder().append(obj1, obj2).isEquals());
    }

// org.mockito.internal.matchers.apachecommons.EqualsBuilderTest::testIntArray
    @Test public void testIntArray() {
        int[] obj1 = new int[2];
        obj1[0] = 5;
        obj1[1] = 6;
        int[] obj2 = new int[2];
        obj2[0] = 5;
        obj2[1] = 6;
        assertTrue(new EqualsBuilder().append(obj1, obj1).isEquals());
        assertTrue(new EqualsBuilder().append(obj1, obj2).isEquals());
        obj1[1] = 7;
        assertTrue(!new EqualsBuilder().append(obj1, obj2).isEquals());

        obj2 = null;
        assertTrue(!new EqualsBuilder().append(obj1, obj2).isEquals());
        obj1 = null;
        assertTrue(new EqualsBuilder().append(obj1, obj2).isEquals());
    }

// org.mockito.internal.matchers.apachecommons.EqualsBuilderTest::testShortArray
    @Test public void testShortArray() {
        short[] obj1 = new short[2];
        obj1[0] = 5;
        obj1[1] = 6;
        short[] obj2 = new short[2];
        obj2[0] = 5;
        obj2[1] = 6;
        assertTrue(new EqualsBuilder().append(obj1, obj1).isEquals());
        assertTrue(new EqualsBuilder().append(obj1, obj2).isEquals());
        obj1[1] = 7;
        assertTrue(!new EqualsBuilder().append(obj1, obj2).isEquals());

        obj2 = null;
        assertTrue(!new EqualsBuilder().append(obj1, obj2).isEquals());
        obj1 = null;
        assertTrue(new EqualsBuilder().append(obj1, obj2).isEquals());
    }

// org.mockito.internal.matchers.apachecommons.EqualsBuilderTest::testCharArray
    @Test public void testCharArray() {
        char[] obj1 = new char[2];
        obj1[0] = 5;
        obj1[1] = 6;
        char[] obj2 = new char[2];
        obj2[0] = 5;
        obj2[1] = 6;
        assertTrue(new EqualsBuilder().append(obj1, obj1).isEquals());
        assertTrue(new EqualsBuilder().append(obj1, obj2).isEquals());
        obj1[1] = 7;
        assertTrue(!new EqualsBuilder().append(obj1, obj2).isEquals());

        obj2 = null;
        assertTrue(!new EqualsBuilder().append(obj1, obj2).isEquals());
        obj1 = null;
        assertTrue(new EqualsBuilder().append(obj1, obj2).isEquals());
    }

// org.mockito.internal.matchers.apachecommons.EqualsBuilderTest::testByteArray
    @Test public void testByteArray() {
        byte[] obj1 = new byte[2];
        obj1[0] = 5;
        obj1[1] = 6;
        byte[] obj2 = new byte[2];
        obj2[0] = 5;
        obj2[1] = 6;
        assertTrue(new EqualsBuilder().append(obj1, obj1).isEquals());
        assertTrue(new EqualsBuilder().append(obj1, obj2).isEquals());
        obj1[1] = 7;
        assertTrue(!new EqualsBuilder().append(obj1, obj2).isEquals());

        obj2 = null;
        assertTrue(!new EqualsBuilder().append(obj1, obj2).isEquals());
        obj1 = null;
        assertTrue(new EqualsBuilder().append(obj1, obj2).isEquals());
    }

// org.mockito.internal.matchers.apachecommons.EqualsBuilderTest::testDoubleArray
    @Test public void testDoubleArray() {
        double[] obj1 = new double[2];
        obj1[0] = 5;
        obj1[1] = 6;
        double[] obj2 = new double[2];
        obj2[0] = 5;
        obj2[1] = 6;
        assertTrue(new EqualsBuilder().append(obj1, obj1).isEquals());
        assertTrue(new EqualsBuilder().append(obj1, obj2).isEquals());
        obj1[1] = 7;
        assertTrue(!new EqualsBuilder().append(obj1, obj2).isEquals());

        obj2 = null;
        assertTrue(!new EqualsBuilder().append(obj1, obj2).isEquals());
        obj1 = null;
        assertTrue(new EqualsBuilder().append(obj1, obj2).isEquals());
    }

// org.mockito.internal.matchers.apachecommons.EqualsBuilderTest::testFloatArray
    @Test public void testFloatArray() {
        float[] obj1 = new float[2];
        obj1[0] = 5;
        obj1[1] = 6;
        float[] obj2 = new float[2];
        obj2[0] = 5;
        obj2[1] = 6;
        assertTrue(new EqualsBuilder().append(obj1, obj1).isEquals());
        assertTrue(new EqualsBuilder().append(obj1, obj2).isEquals());
        obj1[1] = 7;
        assertTrue(!new EqualsBuilder().append(obj1, obj2).isEquals());

        obj2 = null;
        assertTrue(!new EqualsBuilder().append(obj1, obj2).isEquals());
        obj1 = null;
        assertTrue(new EqualsBuilder().append(obj1, obj2).isEquals());
    }

// org.mockito.internal.matchers.apachecommons.EqualsBuilderTest::testBooleanArray
    @Test public void testBooleanArray() {
        boolean[] obj1 = new boolean[2];
        obj1[0] = true;
        obj1[1] = false;
        boolean[] obj2 = new boolean[2];
        obj2[0] = true;
        obj2[1] = false;
        assertTrue(new EqualsBuilder().append(obj1, obj1).isEquals());
        assertTrue(new EqualsBuilder().append(obj1, obj2).isEquals());
        obj1[1] = true;
        assertTrue(!new EqualsBuilder().append(obj1, obj2).isEquals());

        obj2 = null;
        assertTrue(!new EqualsBuilder().append(obj1, obj2).isEquals());
        obj1 = null;
        assertTrue(new EqualsBuilder().append(obj1, obj2).isEquals());
    }

// org.mockito.internal.matchers.apachecommons.EqualsBuilderTest::testMultiLongArray
    @Test public void testMultiLongArray() {
        long[][] array1 = new long[2][2];
        long[][] array2 = new long[2][2];
        for (int i = 0; i < array1.length; ++i) {
            for (int j = 0; j < array1[0].length; j++) {
                array1[i][j] = (i + 1) * (j + 1);
                array2[i][j] = (i + 1) * (j + 1);
            }
        }
        assertTrue(new EqualsBuilder().append(array1, array1).isEquals());
        assertTrue(new EqualsBuilder().append(array1, array2).isEquals());
        array1[1][1] = 0;
        assertTrue(!new EqualsBuilder().append(array1, array2).isEquals());
    }

// org.mockito.internal.matchers.apachecommons.EqualsBuilderTest::testMultiIntArray
    @Test public void testMultiIntArray() {
        int[][] array1 = new int[2][2];
        int[][] array2 = new int[2][2];
        for (int i = 0; i < array1.length; ++i) {
            for (int j = 0; j < array1[0].length; j++) {
                array1[i][j] = (i + 1) * (j + 1);
                array2[i][j] = (i + 1) * (j + 1);
            }
        }
        assertTrue(new EqualsBuilder().append(array1, array1).isEquals());
        assertTrue(new EqualsBuilder().append(array1, array2).isEquals());
        array1[1][1] = 0;
        assertTrue(!new EqualsBuilder().append(array1, array2).isEquals());
    }

// org.mockito.internal.matchers.apachecommons.EqualsBuilderTest::testMultiShortArray
    @Test public void testMultiShortArray() {
        short[][] array1 = new short[2][2];
        short[][] array2 = new short[2][2];
        for (short i = 0; i < array1.length; ++i) {
            for (short j = 0; j < array1[0].length; j++) {
                array1[i][j] = i;
                array2[i][j] = i;
            }
        }
        assertTrue(new EqualsBuilder().append(array1, array1).isEquals());
        assertTrue(new EqualsBuilder().append(array1, array2).isEquals());
        array1[1][1] = 0;
        assertTrue(!new EqualsBuilder().append(array1, array2).isEquals());
    }

// org.mockito.internal.matchers.apachecommons.EqualsBuilderTest::testMultiCharArray
    @Test public void testMultiCharArray() {
        char[][] array1 = new char[2][2];
        char[][] array2 = new char[2][2];
        for (char i = 0; i < array1.length; ++i) {
            for (char j = 0; j < array1[0].length; j++) {
                array1[i][j] = i;
                array2[i][j] = i;
            }
        }
        assertTrue(new EqualsBuilder().append(array1, array1).isEquals());
        assertTrue(new EqualsBuilder().append(array1, array2).isEquals());
        array1[1][1] = 0;
        assertTrue(!new EqualsBuilder().append(array1, array2).isEquals());
    }

// org.mockito.internal.matchers.apachecommons.EqualsBuilderTest::testMultiByteArray
    @Test public void testMultiByteArray() {
        byte[][] array1 = new byte[2][2];
        byte[][] array2 = new byte[2][2];
        for (byte i = 0; i < array1.length; ++i) {
            for (byte j = 0; j < array1[0].length; j++) {
                array1[i][j] = i;
                array2[i][j] = i;
            }
        }
        assertTrue(new EqualsBuilder().append(array1, array1).isEquals());
        assertTrue(new EqualsBuilder().append(array1, array2).isEquals());
        array1[1][1] = 0;
        assertTrue(!new EqualsBuilder().append(array1, array2).isEquals());
    }

// org.mockito.internal.matchers.apachecommons.EqualsBuilderTest::testMultiFloatArray
    @Test public void testMultiFloatArray() {
        float[][] array1 = new float[2][2];
        float[][] array2 = new float[2][2];
        for (int i = 0; i < array1.length; ++i) {
            for (int j = 0; j < array1[0].length; j++) {
                array1[i][j] = (i + 1) * (j + 1);
                array2[i][j] = (i + 1) * (j + 1);
            }
        }
        assertTrue(new EqualsBuilder().append(array1, array1).isEquals());
        assertTrue(new EqualsBuilder().append(array1, array2).isEquals());
        array1[1][1] = 0;
        assertTrue(!new EqualsBuilder().append(array1, array2).isEquals());
    }

// org.mockito.internal.matchers.apachecommons.EqualsBuilderTest::testMultiDoubleArray
    @Test public void testMultiDoubleArray() {
        double[][] array1 = new double[2][2];
        double[][] array2 = new double[2][2];
        for (int i = 0; i < array1.length; ++i) {
            for (int j = 0; j < array1[0].length; j++) {
                array1[i][j] = (i + 1) * (j + 1);
                array2[i][j] = (i + 1) * (j + 1);
            }
        }
        assertTrue(new EqualsBuilder().append(array1, array1).isEquals());
        assertTrue(new EqualsBuilder().append(array1, array2).isEquals());
        array1[1][1] = 0;
        assertTrue(!new EqualsBuilder().append(array1, array2).isEquals());
    }

// org.mockito.internal.matchers.apachecommons.EqualsBuilderTest::testMultiBooleanArray
    @Test public void testMultiBooleanArray() {
        boolean[][] array1 = new boolean[2][2];
        boolean[][] array2 = new boolean[2][2];
        for (int i = 0; i < array1.length; ++i) {
            for (int j = 0; j < array1[0].length; j++) {
                array1[i][j] = (i == 1) || (j == 1);
                array2[i][j] = (i == 1) || (j == 1);
            }
        }
        assertTrue(new EqualsBuilder().append(array1, array1).isEquals());
        assertTrue(new EqualsBuilder().append(array1, array2).isEquals());
        array1[1][1] = false;
        assertTrue(!new EqualsBuilder().append(array1, array2).isEquals());
        
        
        boolean[] array3 = new boolean[]{true, true};
        assertFalse(new EqualsBuilder().append(array1, array3).isEquals());
        assertFalse(new EqualsBuilder().append(array3, array1).isEquals());
        assertFalse(new EqualsBuilder().append(array2, array3).isEquals());
        assertFalse(new EqualsBuilder().append(array3, array2).isEquals());
    }

// org.mockito.internal.matchers.apachecommons.EqualsBuilderTest::testRaggedArray
    @Test public void testRaggedArray() {
        long[][] array1 = new long[2][];
        long[][] array2 = new long[2][];
        for (int i = 0; i < array1.length; ++i) {
            array1[i] = new long[2];
            array2[i] = new long[2];
            for (int j = 0; j < array1[i].length; ++j) {
                array1[i][j] = (i + 1) * (j + 1);
                array2[i][j] = (i + 1) * (j + 1);
            }
        }
        assertTrue(new EqualsBuilder().append(array1, array1).isEquals());
        assertTrue(new EqualsBuilder().append(array1, array2).isEquals());
        array1[1][1] = 0;
        assertTrue(!new EqualsBuilder().append(array1, array2).isEquals());
    }

// org.mockito.internal.matchers.apachecommons.EqualsBuilderTest::testMixedArray
    @Test public void testMixedArray() {
        Object[] array1 = new Object[2];
        Object[] array2 = new Object[2];
        for (int i = 0; i < array1.length; ++i) {
            array1[i] = new long[2];
            array2[i] = new long[2];
            for (int j = 0; j < 2; ++j) {
                ((long[]) array1[i])[j] = (i + 1) * (j + 1);
                ((long[]) array2[i])[j] = (i + 1) * (j + 1);
            }
        }
        assertTrue(new EqualsBuilder().append(array1, array1).isEquals());
        assertTrue(new EqualsBuilder().append(array1, array2).isEquals());
        ((long[]) array1[1])[1] = 0;
        assertTrue(!new EqualsBuilder().append(array1, array2).isEquals());
    }

// org.mockito.internal.matchers.apachecommons.EqualsBuilderTest::testObjectArrayHiddenByObject
    @Test public void testObjectArrayHiddenByObject() {
        TestObject[] array1 = new TestObject[2];
        array1[0] = new TestObject(4);
        array1[1] = new TestObject(5);
        TestObject[] array2 = new TestObject[2];
        array2[0] = new TestObject(4);
        array2[1] = new TestObject(5);
        Object obj1 = array1;
        Object obj2 = array2;
        assertTrue(new EqualsBuilder().append(obj1, obj1).isEquals());
        assertTrue(new EqualsBuilder().append(obj1, array1).isEquals());
        assertTrue(new EqualsBuilder().append(obj1, obj2).isEquals());
        assertTrue(new EqualsBuilder().append(obj1, array2).isEquals());
        array1[1].setA(6);
        assertTrue(!new EqualsBuilder().append(obj1, obj2).isEquals());
    }

// org.mockito.internal.matchers.apachecommons.EqualsBuilderTest::testLongArrayHiddenByObject
    @Test public void testLongArrayHiddenByObject() {
        long[] array1 = new long[2];
        array1[0] = 5L;
        array1[1] = 6L;
        long[] array2 = new long[2];
        array2[0] = 5L;
        array2[1] = 6L;
        Object obj1 = array1;
        Object obj2 = array2;
        assertTrue(new EqualsBuilder().append(obj1, obj1).isEquals());
        assertTrue(new EqualsBuilder().append(obj1, array1).isEquals());
        assertTrue(new EqualsBuilder().append(obj1, obj2).isEquals());
        assertTrue(new EqualsBuilder().append(obj1, array2).isEquals());
        array1[1] = 7;
        assertTrue(!new EqualsBuilder().append(obj1, obj2).isEquals());
    }

// org.mockito.internal.matchers.apachecommons.EqualsBuilderTest::testIntArrayHiddenByObject
    @Test public void testIntArrayHiddenByObject() {
        int[] array1 = new int[2];
        array1[0] = 5;
        array1[1] = 6;
        int[] array2 = new int[2];
        array2[0] = 5;
        array2[1] = 6;
        Object obj1 = array1;
        Object obj2 = array2;
        assertTrue(new EqualsBuilder().append(obj1, obj1).isEquals());
        assertTrue(new EqualsBuilder().append(obj1, array1).isEquals());
        assertTrue(new EqualsBuilder().append(obj1, obj2).isEquals());
        assertTrue(new EqualsBuilder().append(obj1, array2).isEquals());
        array1[1] = 7;
        assertTrue(!new EqualsBuilder().append(obj1, obj2).isEquals());
    }

// org.mockito.internal.matchers.apachecommons.EqualsBuilderTest::testShortArrayHiddenByObject
    @Test public void testShortArrayHiddenByObject() {
        short[] array1 = new short[2];
        array1[0] = 5;
        array1[1] = 6;
        short[] array2 = new short[2];
        array2[0] = 5;
        array2[1] = 6;
        Object obj1 = array1;
        Object obj2 = array2;
        assertTrue(new EqualsBuilder().append(obj1, obj1).isEquals());
        assertTrue(new EqualsBuilder().append(obj1, array1).isEquals());
        assertTrue(new EqualsBuilder().append(obj1, obj2).isEquals());
        assertTrue(new EqualsBuilder().append(obj1, array2).isEquals());
        array1[1] = 7;
        assertTrue(!new EqualsBuilder().append(obj1, obj2).isEquals());
    }

// org.mockito.internal.matchers.apachecommons.EqualsBuilderTest::testCharArrayHiddenByObject
    @Test public void testCharArrayHiddenByObject() {
        char[] array1 = new char[2];
        array1[0] = 5;
        array1[1] = 6;
        char[] array2 = new char[2];
        array2[0] = 5;
        array2[1] = 6;
        Object obj1 = array1;
        Object obj2 = array2;
        assertTrue(new EqualsBuilder().append(obj1, obj1).isEquals());
        assertTrue(new EqualsBuilder().append(obj1, array1).isEquals());
        assertTrue(new EqualsBuilder().append(obj1, obj2).isEquals());
        assertTrue(new EqualsBuilder().append(obj1, array2).isEquals());
        array1[1] = 7;
        assertTrue(!new EqualsBuilder().append(obj1, obj2).isEquals());
    }

// org.mockito.internal.matchers.apachecommons.EqualsBuilderTest::testByteArrayHiddenByObject
    @Test public void testByteArrayHiddenByObject() {
        byte[] array1 = new byte[2];
        array1[0] = 5;
        array1[1] = 6;
        byte[] array2 = new byte[2];
        array2[0] = 5;
        array2[1] = 6;
        Object obj1 = array1;
        Object obj2 = array2;
        assertTrue(new EqualsBuilder().append(obj1, obj1).isEquals());
        assertTrue(new EqualsBuilder().append(obj1, array1).isEquals());
        assertTrue(new EqualsBuilder().append(obj1, obj2).isEquals());
        assertTrue(new EqualsBuilder().append(obj1, array2).isEquals());
        array1[1] = 7;
        assertTrue(!new EqualsBuilder().append(obj1, obj2).isEquals());
    }

// org.mockito.internal.matchers.apachecommons.EqualsBuilderTest::testDoubleArrayHiddenByObject
    @Test public void testDoubleArrayHiddenByObject() {
        double[] array1 = new double[2];
        array1[0] = 5;
        array1[1] = 6;
        double[] array2 = new double[2];
        array2[0] = 5;
        array2[1] = 6;
        Object obj1 = array1;
        Object obj2 = array2;
        assertTrue(new EqualsBuilder().append(obj1, obj1).isEquals());
        assertTrue(new EqualsBuilder().append(obj1, array1).isEquals());
        assertTrue(new EqualsBuilder().append(obj1, obj2).isEquals());
        assertTrue(new EqualsBuilder().append(obj1, array2).isEquals());
        array1[1] = 7;
        assertTrue(!new EqualsBuilder().append(obj1, obj2).isEquals());
    }

// org.mockito.internal.matchers.apachecommons.EqualsBuilderTest::testFloatArrayHiddenByObject
    @Test public void testFloatArrayHiddenByObject() {
        float[] array1 = new float[2];
        array1[0] = 5;
        array1[1] = 6;
        float[] array2 = new float[2];
        array2[0] = 5;
        array2[1] = 6;
        Object obj1 = array1;
        Object obj2 = array2;
        assertTrue(new EqualsBuilder().append(obj1, obj1).isEquals());
        assertTrue(new EqualsBuilder().append(obj1, array1).isEquals());
        assertTrue(new EqualsBuilder().append(obj1, obj2).isEquals());
        assertTrue(new EqualsBuilder().append(obj1, array2).isEquals());
        array1[1] = 7;
        assertTrue(!new EqualsBuilder().append(obj1, obj2).isEquals());
    }

// org.mockito.internal.matchers.apachecommons.EqualsBuilderTest::testBooleanArrayHiddenByObject
    @Test public void testBooleanArrayHiddenByObject() {
        boolean[] array1 = new boolean[2];
        array1[0] = true;
        array1[1] = false;
        boolean[] array2 = new boolean[2];
        array2[0] = true;
        array2[1] = false;
        Object obj1 = array1;
        Object obj2 = array2;
        assertTrue(new EqualsBuilder().append(obj1, obj1).isEquals());
        assertTrue(new EqualsBuilder().append(obj1, array1).isEquals());
        assertTrue(new EqualsBuilder().append(obj1, obj2).isEquals());
        assertTrue(new EqualsBuilder().append(obj1, array2).isEquals());
        array1[1] = true;
        assertTrue(!new EqualsBuilder().append(obj1, obj2).isEquals());
    }

// org.mockito.internal.matchers.apachecommons.EqualsBuilderTest::testUnrelatedClasses
    @Test public void testUnrelatedClasses() {
        Object[] x = new Object[]{new TestACanEqualB(1)};
        Object[] y = new Object[]{new TestBCanEqualA(1)};

        
        assertTrue(Arrays.equals(x, x));
        assertTrue(Arrays.equals(y, y));
        assertTrue(Arrays.equals(x, y));
        assertTrue(Arrays.equals(y, x));
        
        assertTrue(x[0].equals(x[0]));
        assertTrue(y[0].equals(y[0]));
        assertTrue(x[0].equals(y[0]));
        assertTrue(y[0].equals(x[0]));
        assertTrue(new EqualsBuilder().append(x, x).isEquals());
        assertTrue(new EqualsBuilder().append(y, y).isEquals());
        assertTrue(new EqualsBuilder().append(x, y).isEquals());
        assertTrue(new EqualsBuilder().append(y, x).isEquals());
    }

// org.mockito.internal.matchers.apachecommons.EqualsBuilderTest::testNpeForNullElement
    @Test public void testNpeForNullElement() {
        Object[] x1 = new Object[] { new Integer(1), null, new Integer(3) };
        Object[] x2 = new Object[] { new Integer(1), new Integer(2), new Integer(3) };

        
        
        new EqualsBuilder().append(x1, x2);
    }

// org.mockito.internal.matchers.apachecommons.EqualsBuilderTest::testReflectionEqualsExcludeFields
    @Test public void testReflectionEqualsExcludeFields() throws Exception {
        TestObjectWithMultipleFields x1 = new TestObjectWithMultipleFields(1, 2, 3);
        TestObjectWithMultipleFields x2 = new TestObjectWithMultipleFields(1, 3, 4);

        
        assertTrue(!EqualsBuilder.reflectionEquals(x1, x2));

        
        assertTrue(!EqualsBuilder.reflectionEquals(x1, x2, (String[]) null));
        assertTrue(!EqualsBuilder.reflectionEquals(x1, x2, new String[] {}));
        assertTrue(!EqualsBuilder.reflectionEquals(x1, x2, new String[] {"xxx"}));

        
        assertTrue(!EqualsBuilder.reflectionEquals(x1, x2, new String[] {"two"}));
        assertTrue(!EqualsBuilder.reflectionEquals(x1, x2, new String[] {"three"}));

        
        assertTrue(EqualsBuilder.reflectionEquals(x1, x2, new String[] {"two", "three"}));

        
        assertTrue(EqualsBuilder.reflectionEquals(x1, x2, new String[] {"one", "two", "three"}));
        assertTrue(EqualsBuilder.reflectionEquals(x1, x2, new String[] {"one", "two", "three", "xxx"}));
    }

// org.mockito.internal.progress.AtLeastTest::shouldNotAllowNegativeNumberOfMinimumInvocations
    public void shouldNotAllowNegativeNumberOfMinimumInvocations() throws Exception {
        try {
            VerificationModeFactory.atLeast(-50);
            fail();
        } catch (MockitoException e) {
            assertEquals("Negative value is not allowed here", e.getMessage());
        }
    }

// org.mockito.internal.progress.AtLeastTest::shouldAllowZeroInvocations
    public void shouldAllowZeroInvocations() throws Exception {
        VerificationModeFactory.atLeast(0);
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

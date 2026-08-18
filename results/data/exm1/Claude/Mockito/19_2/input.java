// buggy code
    public OngoingInjecter filterCandidate(final Collection<Object> mocks, final Field field, final Object fieldInstance) {
        if(mocks.size() == 1) {
            final Object matchingMock = mocks.iterator().next();

            return new OngoingInjecter() {
                public Object thenInject() {
                    try {
                        if (!new BeanPropertySetter(fieldInstance, field).set(matchingMock)) {
                            new FieldSetter(fieldInstance, field).set(matchingMock);
                        }
                    } catch (RuntimeException e) {
                        new Reporter().cannotInjectDependency(field, matchingMock, e);
                    }
                    return matchingMock;
                }
            };
        }

        return new OngoingInjecter() {
            public Object thenInject() {
                return null;
            }
        };

    }

    OngoingInjecter filterCandidate(
            Collection<Object> mocks,
            Field fieldToBeInjected,
            Object fieldInstance
    );

	public OngoingInjecter filterCandidate(Collection<Object> mocks,
			Field field, Object fieldInstance) {
		List<Object> mockNameMatches = new ArrayList<Object>();
		if (mocks.size() > 1) {
			for (Object mock : mocks) {
				if (field.getName().equals(mockUtil.getMockName(mock).toString())) {
					mockNameMatches.add(mock);
				}
			}
			return next.filterCandidate(mockNameMatches, field,
					fieldInstance);
			/*
			 * In this case we have to check whether we have conflicting naming
			 * fields. E.g. 2 fields of the same type, but we have to make sure
			 * we match on the correct name.
			 * 
			 * Therefore we have to go through all other fields and make sure
			 * whenever we find a field that does match its name with the mock
			 * name, we should take that field instead.
			 */
		}
		return next.filterCandidate(mocks, field, fieldInstance);
	}

    public OngoingInjecter filterCandidate(Collection<Object> mocks, Field field, Object fieldInstance) {
        List<Object> mockTypeMatches = new ArrayList<Object>();
        for (Object mock : mocks) {
            if (field.getType().isAssignableFrom(mock.getClass())) {
                mockTypeMatches.add(mock);
            }
        }

        return next.filterCandidate(mockTypeMatches, field, fieldInstance);
    }

    private boolean injectMockCandidatesOnFields(Set<Object> mocks, Object instance, boolean injectionOccurred, List<Field> orderedInstanceFields) {
        for (Iterator<Field> it = orderedInstanceFields.iterator(); it.hasNext(); ) {
            Field field = it.next();
            Object injected = mockCandidateFilter.filterCandidate(mocks, field, instance).thenInject();
            if (injected != null) {
                injectionOccurred |= true;
                mocks.remove(injected);
                it.remove();
            }
        }
        return injectionOccurred;
    }

// relevant test
// org.mockitousage.bugs.ParentTestMockInjectionTest::noNullPointerException
        public void noNullPointerException() {
            sut.businessMethod();
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

// org.mockitousage.bugs.ShouldNotTryToInjectInFinalOrStaticFieldsTest::dont_fail_with_CONSTANTS
    public void dont_fail_with_CONSTANTS() throws Exception {
    }

// org.mockitousage.bugs.ShouldNotTryToInjectInFinalOrStaticFieldsTest::dont_inject_in_final
    public void dont_inject_in_final() {
        assertNotSame(unrelatedSet, exampleService.aSet);
    }

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

// org.mockitousage.bugs.TimeoutWithAtMostOrNeverShouldBeDisabledTest::shouldDisableTimeoutForAtMost
    public void shouldDisableTimeoutForAtMost() {
        try {
            verify(mock, timeout(30000).atMost(1)).simpleMethod();
            fail();
        } catch (FriendlyReminderException e) {}
    }

// org.mockitousage.bugs.TimeoutWithAtMostOrNeverShouldBeDisabledTest::shouldDisableTimeoutForNever
    public void shouldDisableTimeoutForNever() {
        try {
            verify(mock, timeout(30000).never()).simpleMethod();
            fail();
        } catch (FriendlyReminderException e) {}
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

// org.mockitousage.internal.debugging.LocationImplTest::shouldLocationNotContainGetStackTraceMethod
    public void shouldLocationNotContainGetStackTraceMethod() {
        assertContains("shouldLocationNotContainGetStackTraceMethod", new LocationImpl().toString());
    }

// org.mockitousage.internal.debugging.LocationImplTest::shouldBeSafeInCaseForSomeReasonFilteredStackTraceIsEmpty
    public void shouldBeSafeInCaseForSomeReasonFilteredStackTraceIsEmpty() {
        
        StackTraceFilter filterReturningEmptyArray = new StackTraceFilter() {
            @Override
            public StackTraceElement[] filter(StackTraceElement[] target, boolean keepTop) {
                return new StackTraceElement[0];
            }
        };

        
        String loc = new LocationImpl(filterReturningEmptyArray).toString();

        
        assertEquals("-> at <<unknown line>>", loc);
    }

// org.mockitousage.internal.invocation.realmethod.CleanTraceRealMethodTest::shouldRemoveMockitoInternalsFromStackTraceWhenRealMethodThrows
    public void shouldRemoveMockitoInternalsFromStackTraceWhenRealMethodThrows() throws Throwable {
        
        CleanTraceRealMethod realMethod = new CleanTraceRealMethod(new RealMethod() {
            public Object invoke(Object target, Object[] arguments) throws Throwable {
                return new Foo().throwSomething();
            }});
        
        
        try {
            realMethod.invoke(null, null);
            fail();
        
        } catch (Exception e) {
            assertThat(e, hasMethodInStackTraceAt(0, "throwSomething"));
            assertThat(e, hasMethodInStackTraceAt(1, "invoke"));
            assertThat(e, hasMethodInStackTraceAt(2, "shouldRemoveMockitoInternalsFromStackTraceWhenRealMethodThrows"));
        }
    }

// org.mockitousage.junitrule.InvalidTargetMockitoJUnitRuleTest::shouldInjectWithInvalidReference
    public void shouldInjectWithInvalidReference() throws Exception {
        assertNotNull("Mock created", injected);
        assertNotNull("Test object created", injectInto);
    }

// org.mockitousage.junitrule.MockitoJUnitRuleTest::testInjectMocks
    public void testInjectMocks() throws Exception {
        assertNotNull("Mock created", injected);
        assertNotNull("Object created", injectInto);
        assertEquals("A injected into B", injected, injectInto.getInjected());
    }

// org.mockitousage.junitrule.RuleTestWithFactoryMethodTest::testInjectMocks
    public void testInjectMocks() throws Exception {
        assertNotNull("Mock created", injected);
        assertNotNull("Object created", injectInto);
        assertEquals("A injected into B", injected, injectInto.getInjected());

    }

// org.mockitousage.junitrule.RuleTestWithParameterConstructorTest::testInjectMocks
    public void testInjectMocks() throws Exception {
        assertNotNull("Mock created", injected);
        assertNotNull("Object created", injectInto);
        assertEquals("A injected into B", injected, injectInto.getInjected());

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
        
        emailer.email(12);
        
        
        ArgumentCaptor<Person> argument = new ArgumentCaptor<Person>();
        verify(emailService).sendEmailTo(argument.capture());
        
        assertEquals(12, argument.getValue().getAge());
    }

// org.mockitousage.matchers.CapturingArgumentsTest::should_allow_assertions_on_all_captured_arguments
    public void should_allow_assertions_on_all_captured_arguments() {
        
        emailer.email(11, 12);
        
        
        ArgumentCaptor<Person> argument = ArgumentCaptor.forClass(Person.class);
        verify(emailService, atLeastOnce()).sendEmailTo(argument.capture());
        List<Person> allValues = argument.getAllValues();
        
        assertEquals(11, allValues.get(0).getAge());
        assertEquals(12, allValues.get(1).getAge());
    }

// org.mockitousage.matchers.CapturingArgumentsTest::should_allow_assertions_on_last_argument
    public void should_allow_assertions_on_last_argument() {
        
        emailer.email(11, 12, 13);
        
        
        ArgumentCaptor<Person> argument = ArgumentCaptor.forClass(Person.class);
        verify(emailService, atLeastOnce()).sendEmailTo(argument.capture());
        
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
        
        emailService.sendEmailTo(null);
        
        
        ArgumentCaptor<Person> argument = ArgumentCaptor.forClass(Person.class);
        verify(emailService).sendEmailTo(argument.capture());
        assertEquals(null, argument.getValue());
    }

// org.mockitousage.matchers.CapturingArgumentsTest::should_allow_construction_of_captor_for_parameterized_type_in_a_convenient_way
    public void should_allow_construction_of_captor_for_parameterized_type_in_a_convenient_way()  {
        
        ArgumentCaptor<List<Person>> argument = ArgumentCaptor.forClass(List.class);
    }

// org.mockitousage.matchers.CapturingArgumentsTest::should_allow_construction_of_captor_for_a_more_specific_type
    public void should_allow_construction_of_captor_for_a_more_specific_type()  {
        
        ArgumentCaptor<List> argument = ArgumentCaptor.forClass(ArrayList.class);
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
        } catch (MockitoException e) {}
    }

// org.mockitousage.matchers.CapturingArgumentsTest::should_capture_when_full_arg_list_matches
    public void should_capture_when_full_arg_list_matches() throws Exception {
        
        mock.simpleMethod("foo", 1);
        mock.simpleMethod("bar", 2);
        
        
        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(mock).simpleMethod(captor.capture(), eq(1));
        
        
        assertEquals(1, captor.getAllValues().size());
        assertEquals("foo", captor.getValue());
    }

// org.mockitousage.matchers.CapturingArgumentsTest::should_capture_int_by_creating_captor_with_primitive_wrapper
    public void should_capture_int_by_creating_captor_with_primitive_wrapper() {
        
        IMethods mock = mock(IMethods.class);
        ArgumentCaptor<Integer> argument = ArgumentCaptor.forClass(Integer.class);

        
        mock.intArgumentMethod(10);
        
        
        verify(mock).intArgumentMethod(argument.capture());
        assertEquals(10, (int) argument.getValue());
    }

// org.mockitousage.matchers.CapturingArgumentsTest::should_capture_int_by_creating_captor_with_primitive
    public void should_capture_int_by_creating_captor_with_primitive() throws Exception {
        
        IMethods mock = mock(IMethods.class);
        ArgumentCaptor<Integer> argument = ArgumentCaptor.forClass(int.class);
        
        
        mock.intArgumentMethod(10);
        
        
        verify(mock).intArgumentMethod(argument.capture());
        assertEquals(10, (int) argument.getValue());
    }

// org.mockitousage.matchers.CapturingArgumentsTest::should_capture_byte_vararg_by_creating_captor_with_primitive
    public void should_capture_byte_vararg_by_creating_captor_with_primitive() throws Exception {
        
        IMethods mock = mock(IMethods.class);
        ArgumentCaptor<Byte> argumentCaptor = ArgumentCaptor.forClass(byte.class);

        
        mock.varargsbyte((byte) 1, (byte) 2);

        
        verify(mock).varargsbyte(argumentCaptor.capture());
        assertEquals((byte) 2, (byte) argumentCaptor.getValue());
        Assertions.assertThat(argumentCaptor.getAllValues()).containsExactly((byte) 1, (byte) 2);
    }

// org.mockitousage.matchers.CapturingArgumentsTest::should_capture_byte_vararg_by_creating_captor_with_primitive_wrapper
    public void should_capture_byte_vararg_by_creating_captor_with_primitive_wrapper() throws Exception {
        
        IMethods mock = mock(IMethods.class);
        ArgumentCaptor<Byte> argumentCaptor = ArgumentCaptor.forClass(Byte.class);

        
        mock.varargsbyte((byte) 1, (byte) 2);

        
        verify(mock).varargsbyte(argumentCaptor.capture());
        assertEquals((byte) 2, (byte) argumentCaptor.getValue());
        Assertions.assertThat(argumentCaptor.getAllValues()).containsExactly((byte) 1, (byte) 2);
    }

// org.mockitousage.matchers.CapturingArgumentsTest::should_capture_vararg
    public void should_capture_vararg() throws Exception {
        
        IMethods mock = mock(IMethods.class);
        ArgumentCaptor<String> argumentCaptor = ArgumentCaptor.forClass(String.class);

        
        mock.mixedVarargs(42, "a", "b", "c");

        
        verify(mock).mixedVarargs(any(), argumentCaptor.capture());
        Assertions.assertThat(argumentCaptor.getAllValues()).containsExactly("a", "b", "c");
    }

// org.mockitousage.matchers.CapturingArgumentsTest::should_capture_all_vararg
    public void should_capture_all_vararg() throws Exception {
        
        IMethods mock = mock(IMethods.class);
        ArgumentCaptor<String> argumentCaptor = ArgumentCaptor.forClass(String.class);

        
        mock.mixedVarargs(42, "a", "b", "c");
        mock.mixedVarargs(42, "again ?!");

        
        verify(mock, times(2)).mixedVarargs(any(), argumentCaptor.capture());

        List<String> allVarargsValues = argumentCaptor.getAllValues();
        Assertions.assertThat(allVarargsValues).containsExactly("a", "b", "c", "again ?!");
    }

// org.mockitousage.matchers.CapturingArgumentsTest::should_capture_one_arg_even_when_using_vararg_captor_on_nonvararg_method
    public void should_capture_one_arg_even_when_using_vararg_captor_on_nonvararg_method() throws Exception {
        
        IMethods mock = mock(IMethods.class);
        ArgumentCaptor<String> argumentCaptor = ArgumentCaptor.forClass(String.class);

        
        mock.simpleMethod("a", 2);

        
        verify(mock).simpleMethod(argumentCaptor.capture(), eq(2));
        Assertions.assertThat(argumentCaptor.getAllValues()).containsExactly("a");
    }

// org.mockitousage.matchers.CapturingArgumentsTest::captures_correclty_when_captor_used_multiple_times
    public void captures_correclty_when_captor_used_multiple_times() throws Exception {
        
        IMethods mock = mock(IMethods.class);
        ArgumentCaptor<String> argumentCaptor = ArgumentCaptor.forClass(String.class);

        
        mock.mixedVarargs(42, "a", "b", "c");

        
        
        verify(mock).mixedVarargs(any(), argumentCaptor.capture(), argumentCaptor.capture(), argumentCaptor.capture());
        Assertions.assertThat(argumentCaptor.getAllValues()).containsExactly("a", "b", "c");
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

// org.mockitousage.misuse.DetectingFinalMethodsTest::shouldFailWithUnfinishedVerification
    public void shouldFailWithUnfinishedVerification() {
        withFinal = mock(WithFinal.class);
        verify(withFinal).foo();
        try {
            verify(withFinal).foo();
            fail();
        } catch (UnfinishedVerificationException e) {}
    }

// org.mockitousage.misuse.DetectingFinalMethodsTest::shouldFailWithUnfinishedStubbing
    public void shouldFailWithUnfinishedStubbing() {
        withFinal = mock(WithFinal.class);
        try {
            when(withFinal.foo()).thenReturn(null);
            fail();
        } catch (MissingMethodInvocationException e) {}
    }

// org.mockitousage.misuse.DetectingMisusedMatchersTest::should_fail_fast_when_argument_matchers_are_abused
    public void should_fail_fast_when_argument_matchers_are_abused() {
        misplaced_anyObject_argument_matcher();
        try {
            mock(IMethods.class);
            fail();
        } catch (InvalidUseOfMatchersException e) {
            assertContains("Misplaced argument matcher", e.getMessage());
        }
    }

// org.mockitousage.misuse.DetectingMisusedMatchersTest::should_report_argument_locations_when_argument_matchers_misused
    public void should_report_argument_locations_when_argument_matchers_misused() {
        try {
            Observer observer = mock(Observer.class);
            
            misplaced_anyInt_argument_matcher();
            misplaced_anyObject_argument_matcher();
            misplaced_anyBoolean_argument_matcher();
            
            observer.update(null, null);
            
            validateMockitoUsage();
            fail();
        } catch (InvalidUseOfMatchersException e) {
            assertContains("DetectingMisusedMatchersTest.misplaced_anyInt_argument_matcher", e.getMessage());
            assertContains("DetectingMisusedMatchersTest.misplaced_anyObject_argument_matcher", e.getMessage());
            assertContains("DetectingMisusedMatchersTest.misplaced_anyBoolean_argument_matcher", e.getMessage());
        }
    }

// org.mockitousage.misuse.DetectingMisusedMatchersTest::shouldSayUnfinishedVerificationButNotInvalidUseOfMatchers
    public void shouldSayUnfinishedVerificationButNotInvalidUseOfMatchers() {
        verify(withFinal).finalMethod(anyObject());
        try {
            verify(withFinal);
            fail();
        } catch (UnfinishedVerificationException e) {}
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

// org.mockitousage.packageprotected.MockingPackageProtectedTest::shouldMockPackageProtectedClasses
    public void shouldMockPackageProtectedClasses() {
        mock(PackageProtected.class);
        mock(Foo.class);
        mock(Bar.class);
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

// org.mockitousage.spies.SpyingOnRealObjectsTest::shouldVerify
    public void shouldVerify() {
        spy.add("one");
        spy.add("two");

        assertEquals("one", spy.get(0));
        assertEquals("two", spy.get(1));

        verify(spy).add("one");
        verify(spy).add("two");
    }

// org.mockitousage.spies.SpyingOnRealObjectsTest::shouldBeAbleToMockObjectBecauseWhyNot
    public void shouldBeAbleToMockObjectBecauseWhyNot() {
        spy(new Object());
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

// org.mockitousage.spies.SpyingOnRealObjectsTest::shouldAllowSpyingAnonymousClasses
    public void shouldAllowSpyingAnonymousClasses() {
        
        Foo spy = spy(new Foo() {
            public String print() {
                return "foo";
            }
        });

        
        assertEquals("foo", spy.print());
    }

// org.mockitousage.spies.SpyingOnRealObjectsTest::shouldSayNiceMessageWhenSpyingOnPrivateClass
    public void shouldSayNiceMessageWhenSpyingOnPrivateClass() throws Exception {
        List real = Arrays.asList("first", "second");
        try {
            spy(real);
            fail();
        } catch (MockitoException e) {
            assertContains("Most likely it is a private class that is not visible by Mockito", e.getMessage());
        }
    }

// org.mockitousage.spies.StubbingSpiesDoesNotYieldNPETest::shouldNotThrowNPE
    public void shouldNotThrowNPE() throws Exception {
        Foo foo = new Foo();
        Foo spy = spy(foo);
        
        spy.len(anyString());
        spy.size(anyMap());
        spy.size(anyList());
        spy.size(anyCollection());
        spy.size(anySet());
    }

// org.mockitousage.stacktrace.ClickableStackTracesTest::shouldShowActualAndExpectedWhenArgumentsAreDifferent
    public void shouldShowActualAndExpectedWhenArgumentsAreDifferent() {
        callMethodOnMock("foo");
        try {
            verifyTheMock(1, "not foo");
            fail();
        } catch (ArgumentsAreDifferent e) {
            assertContains("callMethodOnMock(", e.getMessage());
            assertContains("verifyTheMock(", e.getMessage());
        }
    }

// org.mockitousage.stacktrace.ClickableStackTracesWhenFrameworkMisusedTest::shouldPointOutMisplacedMatcher
    public void shouldPointOutMisplacedMatcher() {
        misplacedArgumentMatcherHere();
        try {
            verify(mock).simpleMethod();
            fail();
        } catch (InvalidUseOfMatchersException e) {
            assertContains("-> at ", e.getMessage());
            assertContains("misplacedArgumentMatcherHere(", e.getMessage());
        }
    }

// org.mockitousage.stacktrace.ClickableStackTracesWhenFrameworkMisusedTest::shouldPointOutUnfinishedStubbing
    public void shouldPointOutUnfinishedStubbing() {
        unfinishedStubbingHere();
        
        try {
            verify(mock).simpleMethod();
            fail();
        } catch (UnfinishedStubbingException e) {
            assertContains("-> at ", e.getMessage());
            assertContains("unfinishedStubbingHere(", e.getMessage());
        }
    }

// org.mockitousage.stacktrace.ClickableStackTracesWhenFrameworkMisusedTest::shouldShowWhereIsUnfinishedVerification
    public void shouldShowWhereIsUnfinishedVerification() throws Exception {
        unfinishedVerificationHere();
        try {
            mock(IMethods.class);
            fail();
        } catch (UnfinishedVerificationException e) {
            assertContains("unfinishedVerificationHere(", e.getMessage());
        }
    }

// org.mockitousage.stacktrace.PointingStackTraceToActualInvocationChunkInOrderTest::shouldPointStackTraceToPreviousInvocation
    public void shouldPointStackTraceToPreviousInvocation() {
        inOrder.verify(mock, times(2)).simpleMethod(anyInt());
        inOrder.verify(mockTwo, times(2)).simpleMethod(anyInt());
        
        try {
            inOrder.verify(mock).simpleMethod(999);
            fail();
        } catch (VerificationInOrderFailure e) {
            assertContains("secondChunk(", e.getMessage());
        }
    }

// org.mockitousage.stacktrace.PointingStackTraceToActualInvocationChunkInOrderTest::shouldPointToThirdInteractionBecauseAtLeastOnceUsed
    public void shouldPointToThirdInteractionBecauseAtLeastOnceUsed() {
        inOrder.verify(mock, atLeastOnce()).simpleMethod(anyInt());
        
        try {
            inOrder.verify(mockTwo).simpleMethod(999);
            fail();
        } catch (VerificationInOrderFailure e) {
            assertContains("thirdChunk(", e.getMessage());
        }
    }

// org.mockitousage.stacktrace.PointingStackTraceToActualInvocationChunkInOrderTest::shouldPointToThirdChunkWhenTooLittleActualInvocations
    public void shouldPointToThirdChunkWhenTooLittleActualInvocations() {
        inOrder.verify(mock, times(2)).simpleMethod(anyInt());
        inOrder.verify(mockTwo, times(2)).simpleMethod(anyInt());
        inOrder.verify(mock, atLeastOnce()).simpleMethod(anyInt());
        
        try {
            inOrder.verify(mockTwo, times(3)).simpleMethod(999);
            fail();
        } catch (VerificationInOrderFailure e) {
            assertContains("thirdChunk(", e.getMessage());
        }
    }

// org.mockitousage.stacktrace.PointingStackTraceToActualInvocationChunkInOrderTest::shouldPointToFourthChunkBecauseTooManyActualInvocations
    public void shouldPointToFourthChunkBecauseTooManyActualInvocations() {
        inOrder.verify(mock, atLeastOnce()).simpleMethod(anyInt());
        
        try {
            inOrder.verify(mockTwo, times(0)).simpleMethod(anyInt());
            fail();
        } catch (VerificationInOrderFailure e) {
            assertContains("fourthChunk(", e.getMessage());
        }
    }

// org.mockitousage.stacktrace.PointingStackTraceToActualInvocationInOrderTest::shouldPointStackTraceToPreviousVerified
    public void shouldPointStackTraceToPreviousVerified() {
        inOrder.verify(mock, atLeastOnce()).simpleMethod(anyInt());
        inOrder.verify(mockTwo).simpleMethod(anyInt());
        
        try {
            inOrder.verify(mock).simpleMethod(999);
            fail();
        } catch (VerificationInOrderFailure e) {
            assertContains("fourth(", e.getMessage());
        }
    }

// org.mockitousage.stacktrace.PointingStackTraceToActualInvocationInOrderTest::shouldPointToThirdMethod
    public void shouldPointToThirdMethod() {
        inOrder.verify(mock, atLeastOnce()).simpleMethod(anyInt());
        
        try {
            inOrder.verify(mockTwo).simpleMethod(999);
            fail();
        } catch (VerificationInOrderFailure e) {
            assertContains("third(", e.getMessage());
        }
    }

// org.mockitousage.stacktrace.PointingStackTraceToActualInvocationInOrderTest::shouldPointToSecondMethod
    public void shouldPointToSecondMethod() {
        inOrder.verify(mock).simpleMethod(anyInt());
        inOrder.verify(mockTwo).simpleMethod(anyInt());
        
        try {
            inOrder.verify(mockTwo, times(3)).simpleMethod(999);
            fail();
        } catch (VerificationInOrderFailure e) {
            assertContains("second(", e.getMessage());
        }
    }

// org.mockitousage.stacktrace.PointingStackTraceToActualInvocationInOrderTest::shouldPointToFirstMethodBecauseOfTooManyActualInvocations
    public void shouldPointToFirstMethodBecauseOfTooManyActualInvocations() {
        try {
            inOrder.verify(mock, times(0)).simpleMethod(anyInt());
            fail();
        } catch (VerificationInOrderFailure e) {
            assertContains("first(", e.getMessage());
        }
    }

// org.mockitousage.stacktrace.PointingStackTraceToActualInvocationInOrderTest::shouldPointToSecondMethodBecauseOfTooManyActualInvocations
    public void shouldPointToSecondMethodBecauseOfTooManyActualInvocations() {
        inOrder.verify(mock).simpleMethod(anyInt());
        
        try {
            inOrder.verify(mockTwo, times(0)).simpleMethod(anyInt());
            fail();
        } catch (VerificationInOrderFailure e) {
            assertContains("second(", e.getMessage());
        }
    }

// org.mockitousage.stacktrace.PointingStackTraceToActualInvocationInOrderTest::shouldPointToFourthMethodBecauseOfTooLittleActualInvocations
    public void shouldPointToFourthMethodBecauseOfTooLittleActualInvocations() {
        inOrder.verify(mock).simpleMethod(anyInt());
        inOrder.verify(mockTwo).simpleMethod(anyInt());
        inOrder.verify(mock).simpleMethod(anyInt());
        
        try {
            inOrder.verify(mockTwo, times(3)).simpleMethod(anyInt());
            fail();
        } catch (VerificationInOrderFailure e) {
            assertContains("fourth(", e.getMessage());
        }
    }

// org.mockitousage.stacktrace.PointingStackTraceToActualInvocationTest::shouldPointToTooManyInvocationsChunkOnError
    public void shouldPointToTooManyInvocationsChunkOnError() {
        try {
            verify(mock, times(0)).simpleMethod(1);
            fail();
        } catch (NeverWantedButInvoked e) {
            assertContains("first(", e.getMessage());
        }
    }

// org.mockitousage.stacktrace.PointingStackTraceToActualInvocationTest::shouldNotPointStackTracesToRunnersCode
    public void shouldNotPointStackTracesToRunnersCode() {
        try {
            verify(mock, times(0)).simpleMethod(1);
            fail();
        } catch (NeverWantedButInvoked e) {
            assertNotContains(".runners.", e.getMessage());
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

// org.mockitousage.stubbing.BasicStubbingTest::should_evaluate_latest_stubbing_first
    public void should_evaluate_latest_stubbing_first() throws Exception {
        when(mock.objectReturningMethod(isA(Integer.class))).thenReturn(100);
        when(mock.objectReturningMethod(200)).thenReturn(200);
        
        assertEquals(200, mock.objectReturningMethod(200));
        assertEquals(100, mock.objectReturningMethod(666));
        assertEquals("default behavior should return null", null, mock.objectReturningMethod("blah"));
    }

// org.mockitousage.stubbing.BasicStubbingTest::should_stubbing_be_treated_as_interaction
    public void should_stubbing_be_treated_as_interaction() throws Exception {
        when(mock.booleanReturningMethod()).thenReturn(true);
        
        mock.booleanReturningMethod();
        
        try {
            verifyNoMoreInteractions(mock);
            fail();
        } catch (NoInteractionsWanted e) {}
    }

// org.mockitousage.stubbing.BasicStubbingTest::should_allow_stubbing_to_string
    public void should_allow_stubbing_to_string() throws Exception {
        IMethods mockTwo = mock(IMethods.class);
        when(mockTwo.toString()).thenReturn("test");
        
        assertContains("Mock for IMethods", mock.toString());
        assertEquals("test", mockTwo.toString());
    }

// org.mockitousage.stubbing.BasicStubbingTest::should_stubbing_not_be_treated_as_interaction
    public void should_stubbing_not_be_treated_as_interaction() {
        when(mock.simpleMethod("one")).thenThrow(new RuntimeException());
        doThrow(new RuntimeException()).when(mock).simpleMethod("two");
        
        verifyZeroInteractions(mock);
    }

// org.mockitousage.stubbing.BasicStubbingTest::unfinished_stubbing_cleans_up_the_state
    public void unfinished_stubbing_cleans_up_the_state() {
        reset(mock);
        try {
            when("").thenReturn("");
            fail(); 
        } catch (MissingMethodInvocationException e) {}

        
        verifyZeroInteractions(mock);
    }

// org.mockitousage.stubbing.BasicStubbingTest::should_to_string_mock_name
    public void should_to_string_mock_name() {
        IMethods mock = mock(IMethods.class, "mockie");
        IMethods mockTwo = mock(IMethods.class);
        
        assertContains("Mock for IMethods", "" + mockTwo);
        assertEquals("mockie", "" + mock);
    }

// org.mockitousage.stubbing.BasicStubbingTest::should_allow_mocking_when_to_string_is_final
    public void should_allow_mocking_when_to_string_is_final() throws Exception {
        mock(Foo.class);
    }

// org.mockitousage.stubbing.BasicStubbingTest::test_stub_only_not_verifiable
    public void test_stub_only_not_verifiable() throws Exception {
        IMethods localMock = mock(IMethods.class, withSettings().stubOnly());

        when(localMock.objectReturningMethod(isA(Integer.class))).thenReturn(100);
        when(localMock.objectReturningMethod(200)).thenReturn(200);

        assertEquals(200, localMock.objectReturningMethod(200));
        assertEquals(100, localMock.objectReturningMethod(666));
        assertEquals("default behavior should return null", null, localMock.objectReturningMethod("blah"));

        try {
            verify(localMock, atLeastOnce()).objectReturningMethod(eq(200));
            fail();
        } catch (CannotVerifyStubOnlyMock e) {}
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

// org.mockitousage.stubbing.CloningParameterTest::shouldVerifyEvenIfArgumentsWereMutated
    public void shouldVerifyEvenIfArgumentsWereMutated() throws Exception {

        
        EmailSender emailSender = mock(EmailSender.class, new ClonesArguments());

        
        businessLogic(emailSender);

        
        verify(emailSender).sendEmail(1, new Person("Wes"));
    }

// org.mockitousage.stubbing.CloningParameterTest::shouldReturnDefaultValueWithCloningAnswer
    public void shouldReturnDefaultValueWithCloningAnswer() throws Exception {

        
        EmailSender emailSender = mock(EmailSender.class, new ClonesArguments());
        when(emailSender.getAllEmails(new Person("Wes"))).thenAnswer(new ClonesArguments());

        
        List<?> emails = emailSender.getAllEmails(new Person("Wes"));

        
        assertNotNull(emails);
    }

// org.mockitousage.stubbing.DeepStubbingTest::myTest
    public void myTest() throws Exception {
        SocketFactory sf = mock(SocketFactory.class, RETURNS_DEEP_STUBS);
        when(sf.createSocket(anyString(), eq(80))).thenReturn(null);
        sf.createSocket("what", 80);
    }

// org.mockitousage.stubbing.DeepStubbingTest::simpleCase
    public void simpleCase() throws Exception {
        OutputStream out = new ByteArrayOutputStream();
        Socket socket = mock(Socket.class);
        when(socket.getOutputStream()).thenReturn(out);

        assertSame(out, socket.getOutputStream());
    }

// org.mockitousage.stubbing.DeepStubbingTest::oneLevelDeep
    public void oneLevelDeep() throws Exception {
        OutputStream out = new ByteArrayOutputStream();

        SocketFactory socketFactory = mock(SocketFactory.class, RETURNS_DEEP_STUBS);
        when(socketFactory.createSocket().getOutputStream()).thenReturn(out);

        assertSame(out, socketFactory.createSocket().getOutputStream());
    }

// org.mockitousage.stubbing.DeepStubbingTest::interactions
    public void interactions() throws Exception {
        OutputStream out1 = new ByteArrayOutputStream();
        OutputStream out2 = new ByteArrayOutputStream();

        SocketFactory sf1 = mock(SocketFactory.class, RETURNS_DEEP_STUBS);
        when(sf1.createSocket().getOutputStream()).thenReturn(out1);

        SocketFactory sf2 = mock(SocketFactory.class, RETURNS_DEEP_STUBS);
        when(sf2.createSocket().getOutputStream()).thenReturn(out2);

        assertSame(out1, sf1.createSocket().getOutputStream());
        assertSame(out2, sf2.createSocket().getOutputStream());
    }

// org.mockitousage.stubbing.DeepStubbingTest::withArguments
    public void withArguments() throws Exception {
        OutputStream out1 = new ByteArrayOutputStream();
        OutputStream out2 = new ByteArrayOutputStream();
        OutputStream out3 = new ByteArrayOutputStream();

        SocketFactory sf = mock(SocketFactory.class, RETURNS_DEEP_STUBS);
        when(sf.createSocket().getOutputStream()).thenReturn(out1);
        when(sf.createSocket("google.com", 80).getOutputStream()).thenReturn(out2);
        when(sf.createSocket("stackoverflow.com", 80).getOutputStream()).thenReturn(out3);

        assertSame(out1, sf.createSocket().getOutputStream());
        assertSame(out2, sf.createSocket("google.com", 80).getOutputStream());
        assertSame(out3, sf.createSocket("stackoverflow.com", 80).getOutputStream());
    }

// org.mockitousage.stubbing.DeepStubbingTest::withAnyPatternArguments
    public void withAnyPatternArguments() throws Exception {
        OutputStream out = new ByteArrayOutputStream();

        
        SocketFactory sf = mock(SocketFactory.class, RETURNS_DEEP_STUBS);
        when(sf.createSocket(anyString(), anyInt()).getOutputStream()).thenReturn(out);

        assertSame(out, sf.createSocket("google.com", 80).getOutputStream());
        assertSame(out, sf.createSocket("stackoverflow.com", 8080).getOutputStream());
    }

// org.mockitousage.stubbing.DeepStubbingTest::withComplexPatternArguments
    public void withComplexPatternArguments() throws Exception {
        OutputStream out1 = new ByteArrayOutputStream();
        OutputStream out2 = new ByteArrayOutputStream();

        SocketFactory sf = mock(SocketFactory.class, RETURNS_DEEP_STUBS);
        when(sf.createSocket(anyString(), eq(80)).getOutputStream()).thenReturn(out1);
        when(sf.createSocket(anyString(), eq(8080)).getOutputStream()).thenReturn(out2);

        assertSame(out2, sf.createSocket("stackoverflow.com", 8080).getOutputStream());
        assertSame(out1, sf.createSocket("google.com", 80).getOutputStream());
        assertSame(out2, sf.createSocket("google.com", 8080).getOutputStream());
        assertSame(out1, sf.createSocket("stackoverflow.com", 80).getOutputStream());
    }

// org.mockitousage.stubbing.DeepStubbingTest::withSimplePrimitive
    public void withSimplePrimitive() throws Exception {
        int a = 32;

        SocketFactory sf = mock(SocketFactory.class, RETURNS_DEEP_STUBS);
        when(sf.createSocket().getPort()).thenReturn(a);

        assertEquals(a, sf.createSocket().getPort());
    }

// org.mockitousage.stubbing.DeepStubbingTest::withPatternPrimitive
    public void withPatternPrimitive() throws Exception {
        int a = 12, b = 23, c = 34;

        SocketFactory sf = mock(SocketFactory.class, RETURNS_DEEP_STUBS);
        when(sf.createSocket(eq("stackoverflow.com"), eq(80)).getPort()).thenReturn(a);
        when(sf.createSocket(eq("google.com"), anyInt()).getPort()).thenReturn(b);
        when(sf.createSocket(eq("stackoverflow.com"), eq(8080)).getPort()).thenReturn(c);

        assertEquals(b, sf.createSocket("google.com", 80).getPort());
        assertEquals(c, sf.createSocket("stackoverflow.com", 8080).getPort());
        assertEquals(a, sf.createSocket("stackoverflow.com", 80).getPort());
    }

// org.mockitousage.stubbing.DeepStubbingTest::shouldStubbingBasicallyWorkFine
    public void shouldStubbingBasicallyWorkFine() throws Exception {
        
        given(person.getAddress().getStreet().getName()).willReturn("Norymberska");
        
        
        String street = person.getAddress().getStreet().getName();
        
        
        assertEquals("Norymberska", street);
    }

// org.mockitousage.stubbing.DeepStubbingTest::shouldVerificationBasicallyWorkFine
    public void shouldVerificationBasicallyWorkFine() throws Exception {
        
        person.getAddress().getStreet().getName();
        
        
        verify(person.getAddress().getStreet()).getName();
    }

// org.mockitousage.stubbing.DeepStubbingTest::verification_work_with_argument_Matchers_in_nested_calls
    public void verification_work_with_argument_Matchers_in_nested_calls() throws Exception {
        
        person.getAddress("111 Mock Lane").getStreet();
        person.getAddress("111 Mock Lane").getStreet(Locale.ITALIAN).getName();

        
        verify(person.getAddress(anyString())).getStreet();
        verify(person.getAddress(anyString()).getStreet(Locale.CHINESE), never()).getName();
        verify(person.getAddress(anyString()).getStreet(eq(Locale.ITALIAN))).getName();
    }

// org.mockitousage.stubbing.DeepStubbingTest::deep_stub_return_same_mock_instance_if_invocation_matchers_matches
    public void deep_stub_return_same_mock_instance_if_invocation_matchers_matches() throws Exception {
        when(person.getAddress(anyString()).getStreet().getName()).thenReturn("deep");

        person.getAddress("the docks").getStreet().getName();

        assertSame(person.getAddress("the docks").getStreet(), person.getAddress(anyString()).getStreet());
        assertSame(person.getAddress(anyString()).getStreet(), person.getAddress(anyString()).getStreet());
        assertSame(person.getAddress("the docks").getStreet(), person.getAddress("the docks").getStreet());
        assertSame(person.getAddress(anyString()).getStreet(), person.getAddress("the docks").getStreet());
        assertSame(person.getAddress("111 Mock Lane").getStreet(), person.getAddress("the docks").getStreet());
    }

// org.mockitousage.stubbing.DeepStubbingTest::times_never_atLeast_atMost_verificationModes_should_work
    public void times_never_atLeast_atMost_verificationModes_should_work() throws Exception {
        when(person.getAddress(anyString()).getStreet().getName()).thenReturn("deep");

        person.getAddress("the docks").getStreet().getName();
        person.getAddress("the docks").getStreet().getName();
        person.getAddress("the docks").getStreet().getName();
        person.getAddress("the docks").getStreet(Locale.ITALIAN).getName();

        verify(person.getAddress("the docks").getStreet(), times(3)).getName();
        verify(person.getAddress("the docks").getStreet(Locale.CHINESE), never()).getName();
        verify(person.getAddress("the docks").getStreet(Locale.ITALIAN), atMost(1)).getName();
    }

// org.mockitousage.stubbing.DeepStubbingTest::inOrder_only_work_on_the_very_last_mock_but_it_works
    public void inOrder_only_work_on_the_very_last_mock_but_it_works() throws Exception {
        when(person.getAddress(anyString()).getStreet().getName()).thenReturn("deep");
        when(person.getAddress(anyString()).getStreet(Locale.ITALIAN).getName()).thenReturn("deep");
        when(person.getAddress(anyString()).getStreet(Locale.CHINESE).getName()).thenReturn("deep");

        person.getAddress("the docks").getStreet().getName();
        person.getAddress("the docks").getStreet().getLongName();
        person.getAddress("the docks").getStreet(Locale.ITALIAN).getName();
        person.getAddress("the docks").getStreet(Locale.CHINESE).getName();

        InOrder inOrder = inOrder(
                person.getAddress("the docks").getStreet(),
                person.getAddress("the docks").getStreet(Locale.CHINESE),
                person.getAddress("the docks").getStreet(Locale.ITALIAN)
        );
        inOrder.verify(person.getAddress("the docks").getStreet(), times(1)).getName();
        inOrder.verify(person.getAddress("the docks").getStreet()).getLongName();
        inOrder.verify(person.getAddress("the docks").getStreet(Locale.ITALIAN), atLeast(1)).getName();
        inOrder.verify(person.getAddress("the docks").getStreet(Locale.CHINESE)).getName();
    }

// org.mockitousage.stubbing.DeepStubbingTest::verificationMode_only_work_on_the_last_returned_mock
    public void verificationMode_only_work_on_the_last_returned_mock() throws Exception {
        
        when(person.getAddress("the docks").getStreet().getName()).thenReturn("deep");

        
        person.getAddress("the docks").getStreet().getName();
        
        
        verify(person.getAddress("the docks").getStreet()).getName();

        try {
            verify(person.getAddress("the docks"), times(1)).getStreet();
            fail();
        } catch (TooManyActualInvocations e) {
            Assertions.assertThat(e.getMessage())
                    .contains("Wanted 1 time")
                    .contains("But was 3 times");
        }
    }

// org.mockitousage.stubbing.DeepStubbingTest::shouldFailGracefullyWhenClassIsFinal
    public void shouldFailGracefullyWhenClassIsFinal() throws Exception {
        
        FinalClass value = new FinalClass();
        given(person.getFinalClass()).willReturn(value);
        
        
        assertEquals(value, person.getFinalClass());
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

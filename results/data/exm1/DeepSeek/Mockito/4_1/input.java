// buggy code
    public void noMoreInteractionsWanted(Invocation undesired, List<VerificationAwareInvocation> invocations) {
        ScenarioPrinter scenarioPrinter = new ScenarioPrinter();
        String scenario = scenarioPrinter.print(invocations);

        throw new NoInteractionsWanted(join(
                "No interactions wanted here:",
                new LocationImpl(),
                "But found this interaction on mock '" + undesired.getMock() + "':",
                undesired.getLocation(),
                scenario
        ));
    }

    public void noMoreInteractionsWantedInOrder(Invocation undesired) {
        throw new VerificationInOrderFailure(join(
                "No interactions wanted here:",
                new LocationImpl(),
                "But found this interaction on mock '" + undesired.getMock() + "':",
                undesired.getLocation()
        ));
    }

    private String exceptionCauseMessageIfAvailable(Exception details) {
        return details.getCause().getMessage();
    }

// relevant test
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

// org.mockito.internal.progress.ThreadSafeMockingProgressTest::shouldKnowWhenVerificationHasStarted
    public void shouldKnowWhenVerificationHasStarted() throws Exception {
        
        verify(mock(List.class));
        ThreadSafeMockingProgress p = new ThreadSafeMockingProgress();

        
        assertNotNull(p.pullVerificationMode());
    }

// org.mockito.internal.progress.TimesTest::shouldNotAllowNegativeNumberOfInvocations
    public void shouldNotAllowNegativeNumberOfInvocations() throws Exception {
        try {
            VerificationModeFactory.times(-50);
            fail();
        } catch (MockitoException e) {
            assertEquals("Negative value is not allowed here", e.getMessage());
        }
    }

// org.mockito.internal.reporting.PluralizerTest::shouldGetPluralizedNumber
    public void shouldGetPluralizedNumber() {
        new Pluralizer();
        assertEquals("0 times", Pluralizer.pluralize(0));
        assertEquals("1 time", Pluralizer.pluralize(1));
        assertEquals("2 times", Pluralizer.pluralize(2));
        assertEquals("20 times", Pluralizer.pluralize(20));
    }

// org.mockito.internal.runners.RunnerFactoryTest::shouldCreateRunnerForJUnit44
    public void shouldCreateRunnerForJUnit44() throws Exception {
        
        RunnerProvider provider = new RunnerProvider() {
            public boolean isJUnit45OrHigherAvailable() {
                return false;
            }
        };
        RunnerFactory factory = new RunnerFactory(provider);
        
        
        RunnerImpl runner = factory.create(RunnerFactoryTest.class);
        
        
        assertThat(runner, is(JUnit44RunnerImpl.class));
    }

// org.mockito.internal.runners.RunnerFactoryTest::shouldCreateRunnerForJUnit45
    public void shouldCreateRunnerForJUnit45()  throws Exception{
        
        RunnerProvider provider = new RunnerProvider() {
            public boolean isJUnit45OrHigherAvailable() {
                return true;
            }
        };
        RunnerFactory factory = new RunnerFactory(provider);
        
        
        RunnerImpl runner = factory.create(RunnerFactoryTest.class);
        
        
        assertThat(runner, is(JUnit45AndHigherRunnerImpl.class));
    }

// org.mockito.internal.runners.RunnerFactoryTest::shouldSaySomethingMeaningfulWhenNoTestMethods
    public void shouldSaySomethingMeaningfulWhenNoTestMethods()  throws Exception{
        
        RunnerFactory factory = new RunnerFactory(new RunnerProvider());

        
        try {
            factory.create(NoTestMethods.class);
            fail();
        }
        
        catch (MockitoException e) {
            assertContains("No tests", e.getMessage());
        }
    }

// org.mockito.internal.runners.RunnerFactoryTest::shouldForwardInvocationTargetException
    public void shouldForwardInvocationTargetException()  throws Exception{
        
        RunnerFactory factory = new RunnerFactory(new RunnerProvider()
        {
            @Override
            public RunnerImpl newInstance(String runnerClassName, Class<?> constructorParam) throws Exception {
                throw new InvocationTargetException(new RuntimeException());
            }
        });

        
        try {
            factory.create(this.getClass());
            fail();
        }
        
        catch (InvocationTargetException e) {}
    }

// org.mockito.internal.runners.util.RunnerProviderTest::shouldKnowAboutJUnit45
    public void shouldKnowAboutJUnit45() throws Exception {
        
        RunnerProvider provider = new RunnerProvider();
        
        assertTrue(provider.isJUnit45OrHigherAvailable());
        
    }

// org.mockito.internal.runners.util.RunnerProviderTest::shouldCreateRunnerInstance
    public void shouldCreateRunnerInstance() throws Throwable {
        
        RunnerProvider provider = new RunnerProvider();
        
        RunnerImpl runner = provider.newInstance("org.mockito.internal.runners.JUnit45AndHigherRunnerImpl", this.getClass());
        
        assertNotNull(runner);
    }

// org.mockito.internal.runners.util.TestMethodsFinderTest::someTest
        @Test public void someTest() {}

// org.mockito.internal.runners.util.TestMethodsFinderTest::shouldKnowWhenClassHasTests
    public void shouldKnowWhenClassHasTests() {
        assertTrue(new TestMethodsFinder().hasTestMethods(HasTests.class));
        assertFalse(new TestMethodsFinder().hasTestMethods(DoesNotHaveTests.class));
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

// org.mockito.internal.stubbing.answers.ReturnsArgumentAtTest::should_be_able_to_return_the_first_parameter
	public void should_be_able_to_return_the_first_parameter() throws Throwable {
		assertThat(new ReturnsArgumentAt(0).answer(invocationWith("A", "B"))).isEqualTo("A");
	}

// org.mockito.internal.stubbing.answers.ReturnsArgumentAtTest::should_be_able_to_return_the_second_parameter
	public void should_be_able_to_return_the_second_parameter()
			throws Throwable {
		assertThat(new ReturnsArgumentAt(1).answer(invocationWith("A", "B", "C"))).isEqualTo("B");
	}

// org.mockito.internal.stubbing.answers.ReturnsArgumentAtTest::should_be_able_to_return_the_last_parameter
	public void should_be_able_to_return_the_last_parameter() throws Throwable {
		assertThat(new ReturnsArgumentAt(-1).answer(invocationWith("A"))).isEqualTo("A");
		assertThat(new ReturnsArgumentAt(-1).answer(invocationWith("A", "B"))).isEqualTo("B");
	}

// org.mockito.internal.stubbing.answers.ReturnsArgumentAtTest::should_be_able_to_return_the_specified_parameter
	public void should_be_able_to_return_the_specified_parameter() throws Throwable {
		assertThat(new ReturnsArgumentAt(0).answer(invocationWith("A", "B", "C"))).isEqualTo("A");
		assertThat(new ReturnsArgumentAt(1).answer(invocationWith("A", "B", "C"))).isEqualTo("B");
		assertThat(new ReturnsArgumentAt(2).answer(invocationWith("A", "B", "C"))).isEqualTo("C");
	}

// org.mockito.internal.stubbing.answers.ReturnsArgumentAtTest::should_raise_an_exception_if_index_is_not_in_allowed_range_at_creation_time
	public void should_raise_an_exception_if_index_is_not_in_allowed_range_at_creation_time() throws Throwable {
        try {
            new ReturnsArgumentAt(-30);
            fail();
        } catch (Exception e) {
            assertThat(e.getMessage())
                    .containsIgnoringCase("argument index")
                    .containsIgnoringCase("positive number")
                    .contains("1")
                    .containsIgnoringCase("last argument");
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

// org.mockito.internal.stubbing.defaultanswers.ReturnsMoreEmptyValuesTest::shouldReturnEmptyArray
    public void shouldReturnEmptyArray() {
        String[] ret = (String[]) rv.returnValueFor((new String[0]).getClass());
        assertTrue(ret.getClass().isArray());
        assertTrue(ret.length == 0);
    }

// org.mockito.internal.stubbing.defaultanswers.ReturnsMoreEmptyValuesTest::shouldReturnEmptyString
    public void shouldReturnEmptyString() {
        assertEquals("", rv.returnValueFor(String.class));
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

// org.mockito.internal.util.DecamelizerTest::shouldProduceDecentDescription
    public void shouldProduceDecentDescription() throws Exception {
        assertEquals("<Sentence with strong language>", decamelizeMatcher("SentenceWithStrongLanguage"));
        assertEquals("<W e i r d o 1>", decamelizeMatcher("WEIRDO1"));
        assertEquals("<_>", decamelizeMatcher("_"));
        assertEquals("<Has exactly 3 elements>", decamelizeMatcher("HasExactly3Elements"));
        assertEquals("<custom argument matcher>", decamelizeMatcher(""));
    }

// org.mockito.internal.util.MockCreationValidatorTest::should_not_allow_extra_interface_that_is_the_same_as_the_mocked_type
    public void should_not_allow_extra_interface_that_is_the_same_as_the_mocked_type() throws Exception {
        try {
            
            validator.validateExtraInterfaces(IMethods.class, (Collection) asList(IMethods.class));
            fail();
        } catch (MockitoException e) {
            
            assertThat(e.getMessage()).contains("You mocked following type: IMethods");
        }
    }

// org.mockito.internal.util.MockCreationValidatorTest::should_not_allow_inconsistent_types
    public void should_not_allow_inconsistent_types() throws Exception {
        
        validator.validateMockedType(List.class, new ArrayList());
        
    }

// org.mockito.internal.util.MockCreationValidatorTest::should_allow_only_consistent_types
    public void should_allow_only_consistent_types() throws Exception {
        
        validator.validateMockedType(ArrayList.class, new ArrayList());
        
    }

// org.mockito.internal.util.MockCreationValidatorTest::should_validation_be_safe_when_nulls_passed
    public void should_validation_be_safe_when_nulls_passed() throws Exception {
        
        validator.validateMockedType(null, new ArrayList());
        
        validator.validateMockedType(ArrayList.class, null);
        
    }

// org.mockito.internal.util.MockCreationValidatorTest::should_not_allow_serializable_with_Object_that_dont_implement_Serializable
    public void should_not_allow_serializable_with_Object_that_dont_implement_Serializable() {
        class NonSerializableInnerClassThatHaveAHiddenOneArgConstructor {}
        boolean serializable = true;
        validator.validateSerializable(NonSerializableInnerClassThatHaveAHiddenOneArgConstructor.class, serializable);
    }

// org.mockito.internal.util.MockCreationValidatorTest::should_allow_serializable_with_interfaces_or_Serializable_objects
    public void should_allow_serializable_with_interfaces_or_Serializable_objects() {
        boolean serializable = true;
        validator.validateSerializable(Observer.class, serializable);
        validator.validateSerializable(Integer.class, serializable);
    }

// org.mockito.internal.util.MockNameImplTest::shouldProvideTheNameForClass
    public void shouldProvideTheNameForClass() throws Exception {
        
        String name = new MockNameImpl(null, SomeClass.class).toString();
        
        assertEquals("someClass", name);
    }

// org.mockito.internal.util.MockNameImplTest::shouldProvideTheNameForAnonymousClass
    public void shouldProvideTheNameForAnonymousClass() throws Exception {
        
        SomeInterface anonymousInstance = new SomeInterface() {};
        
        String name = new MockNameImpl(null, anonymousInstance.getClass()).toString();
        
        assertEquals("someInterface", name);
    }

// org.mockito.internal.util.MockNameImplTest::shouldProvideTheGivenName
    public void shouldProvideTheGivenName() throws Exception {
        
        String name = new MockNameImpl("The Hulk", SomeClass.class).toString();
        
        assertEquals("The Hulk", name);
    }

// org.mockito.internal.util.MockUtilTest::should_get_handler
    public void should_get_handler() {
        List mock = Mockito.mock(List.class);
        assertNotNull(mockUtil.getMockHandler(mock));
    }

// org.mockito.internal.util.MockUtilTest::should_scream_when_enhanced_but_not_a_mock_passed
    public void should_scream_when_enhanced_but_not_a_mock_passed() {
        Object o = Enhancer.create(ArrayList.class, NoOp.INSTANCE);
        try {
            mockUtil.getMockHandler(o);
            fail();
        } catch (NotAMockException e) {}
    }

// org.mockito.internal.util.MockUtilTest::should_scream_when_not_a_mock_passed
    public void should_scream_when_not_a_mock_passed() {
        mockUtil.getMockHandler("");
    }

// org.mockito.internal.util.MockUtilTest::should_scream_when_null_passed
    public void should_scream_when_null_passed() {
        mockUtil.getMockHandler(null);
    }

// org.mockito.internal.util.MockUtilTest::should_get_mock_settings
    public void should_get_mock_settings() {
        List mock = Mockito.mock(List.class);
        assertNotNull(mockUtil.getMockSettings(mock));
    }

// org.mockito.internal.util.MockUtilTest::should_validate_mock
    public void should_validate_mock() {
        assertFalse(mockUtil.isMock("i mock a mock"));
        assertTrue(mockUtil.isMock(Mockito.mock(List.class)));
    }

// org.mockito.internal.util.MockUtilTest::should_validate_spy
    public void should_validate_spy() {
        assertFalse(mockUtil.isSpy("i mock a mock"));
        assertFalse(mockUtil.isSpy(Mockito.mock(List.class)));
        assertFalse(mockUtil.isSpy((Class) null));

        assertTrue(mockUtil.isSpy(Mockito.spy(new ArrayList())));
        assertTrue(mockUtil.isSpy(Mockito.spy(ArrayList.class)));
        assertTrue(mockUtil.isSpy(Mockito.mock(ArrayList.class, withSettings().defaultAnswer(Mockito.CALLS_REAL_METHODS))));
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

// org.mockito.internal.util.MockUtilTest::should_konw_if_type_is_mockable
    public void should_konw_if_type_is_mockable() throws Exception {
        assertFalse(mockUtil.isTypeMockable(FinalClass.class));
        assertFalse(mockUtil.isTypeMockable(int.class));

        assertTrue(mockUtil.isTypeMockable(SomeClass.class));
        assertTrue(mockUtil.isTypeMockable(SomeInterface.class));
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

// org.mockito.internal.util.SimpleMockitoLoggerTest::shouldLog
    public void shouldLog() throws Exception {
        
        SimpleMockitoLogger logger = new SimpleMockitoLogger();
        
        logger.log("foo");
        
        assertEquals("foo", logger.getLoggedInfo());
    }

// org.mockito.internal.util.TimerTest::should_return_true_if_task_is_in_acceptable_time_bounds
    public void should_return_true_if_task_is_in_acceptable_time_bounds() {
        
        long duration = 10000L;
        Timer timer = new Timer(duration);

        
        timer.start();
        boolean stillCounting = timer.isCounting();

        
        assertThat(stillCounting, is(true));
    }

// org.mockito.internal.util.TimerTest::should_return_false_if_task_is_outside_the_acceptable_time_bounds
    public void should_return_false_if_task_is_outside_the_acceptable_time_bounds() {
        
        Timer timer = new Timer(-1);
        timer.start();

        
        boolean stillCounting = timer.isCounting();

        
        assertThat(timer.isCounting(), is(false));
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

// org.mockito.internal.util.collections.ListUtilTest::shouldFilterList
    public void shouldFilterList() throws Exception {
        List list = asList("one", "x", "two", "x", "three");
        List filtered = ListUtil.filter(list, new Filter() {
            public boolean isOut(Object object) {
                return object == "x";
            }
        });
        
        assertThat(filtered, hasExactlyInOrder("one", "two", "three"));
    }

// org.mockito.internal.util.collections.ListUtilTest::shouldReturnEmptyIfEmptyListGiven
    public void shouldReturnEmptyIfEmptyListGiven() throws Exception {
        List list = new LinkedList();
        List filtered = ListUtil.filter(list, null);
        assertTrue(filtered.isEmpty());
    }

// org.mockito.internal.util.junit.JUnitFailureHackerTest::shouldReplaceException
    public void shouldReplaceException() throws Exception {
        
        RuntimeException actualExc = new RuntimeException("foo");
        Failure failure = new Failure(Description.EMPTY, actualExc);
        
        
        hacker.appendWarnings(failure, "unused stubbing");
                
        
        assertEquals(ExceptionIncludingMockitoWarnings.class, failure.getException().getClass());
        assertEquals(actualExc, failure.getException().getCause());
        Assertions.assertThat(actualExc.getStackTrace()).isEqualTo(failure.getException().getStackTrace());
    }

// org.mockito.internal.util.junit.JUnitFailureHackerTest::shouldAppendWarning
    public void shouldAppendWarning() throws Exception {
        Failure failure = new Failure(Description.EMPTY, new RuntimeException("foo"));
        
        
        hacker.appendWarnings(failure, "unused stubbing blah");
        
        
        assertContains("unused stubbing blah", failure.getException().getMessage());        
    }

// org.mockito.internal.util.junit.JUnitFailureHackerTest::shouldNotAppendWhenNoWarnings
    public void shouldNotAppendWhenNoWarnings() throws Exception {
        RuntimeException ex = new RuntimeException("foo");
        Failure failure = new Failure(Description.EMPTY, ex);
        
        
        hacker.appendWarnings(failure, "");
        
        
        assertEquals(ex, failure.getException());        
    }

// org.mockito.internal.util.junit.JUnitFailureHackerTest::shouldNotAppendWhenNullWarnings
    public void shouldNotAppendWhenNullWarnings() throws Exception {
        RuntimeException ex = new RuntimeException("foo");
        Failure failure = new Failure(Description.EMPTY, ex);
        
        
        hacker.appendWarnings(failure, null);
        
        
        assertEquals(ex, failure.getException());        
    }

// org.mockito.internal.util.junit.JUnitFailureHackerTest::shouldPrintTheWarningSoICanSeeIt
    public void shouldPrintTheWarningSoICanSeeIt() throws Exception {
        Failure failure = new Failure(Description.EMPTY, new RuntimeException("foo"));
        
        
        hacker.appendWarnings(failure, "unused stubbing blah");
        
        
        System.out.println(failure.getException());        
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

// org.mockito.internal.util.reflection.FieldReaderTest::shouldKnowWhenNull
    public void shouldKnowWhenNull() throws Exception {
        
        FieldReader reader = new FieldReader(new Foo(), Foo.class.getDeclaredField("isNull"));
        
        assertTrue(reader.isNull());
    }

// org.mockito.internal.util.reflection.FieldReaderTest::shouldKnowWhenNotNull
    public void shouldKnowWhenNotNull() throws Exception {
        
        FieldReader reader = new FieldReader(new Foo(), Foo.class.getDeclaredField("notNull"));
        
        assertFalse(reader.isNull());
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

// org.mockito.internal.util.reflection.WhiteboxTest::shouldSetInternalStateOnHierarchy
    public void shouldSetInternalStateOnHierarchy() {
        
        DummyClassForTests dummy = new DummyClassForTests();
        
        Whitebox.setInternalState(dummy, "somePrivateField", "cool!");
        
        Object internalState = org.powermock.reflect.Whitebox.getInternalState(dummy, "somePrivateField");
        assertEquals("cool!", internalState);
    }

// org.mockito.internal.util.reflection.WhiteboxTest::shouldGetInternalStateFromHierarchy
    public void shouldGetInternalStateFromHierarchy() {
        
        DummyClassForTests dummy = new DummyClassForTests();
        org.powermock.reflect.Whitebox.setInternalState(dummy, "somePrivateField", "boo!");
        
        Object internalState = Whitebox.getInternalState(dummy, "somePrivateField");
        
        assertEquals("boo!", internalState);
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
    public void noMoreInteractionsExceptionMessageShouldDescribeMock() {}

// org.mockito.internal.verification.NoMoreInteractionsTest::noMoreInteractionsInOrderExceptionMessageShouldDescribeMock
    public void noMoreInteractionsInOrderExceptionMessageShouldDescribeMock() {}

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

// org.mockito.internal.verification.argumentmatching.ArgumentMatchingToolTest::shouldNotFindAnySuspiciousMatchersWhenNumberOfArgumentsDoesntMatch
    public void shouldNotFindAnySuspiciousMatchersWhenNumberOfArgumentsDoesntMatch() {
        
        List<Matcher> matchers = (List) Arrays.asList(new Equals(1));

        
        Integer[] suspicious = tool.getSuspiciouslyNotMatchingArgsIndexes(matchers, new Object[] {10, 20});
        
        
        assertEquals(0, suspicious.length);
    }

// org.mockito.internal.verification.argumentmatching.ArgumentMatchingToolTest::shouldNotFindAnySuspiciousMatchersWhenArgumentsMatch
    public void shouldNotFindAnySuspiciousMatchersWhenArgumentsMatch() {
        
        List<Matcher> matchers = (List) Arrays.asList(new Equals(10), new Equals(20));
        
        
        Integer[] suspicious = tool.getSuspiciouslyNotMatchingArgsIndexes(matchers, new Object[] {10, 20});
        
        
        assertEquals(0, suspicious.length);
    }

// org.mockito.internal.verification.argumentmatching.ArgumentMatchingToolTest::shouldFindSuspiciousMatchers
    public void shouldFindSuspiciousMatchers() {
        
        Equals matcherInt20 = new Equals(20);
        Long longPretendingAnInt = new Long(20);
        
        
        List<Matcher> matchers = (List) Arrays.asList(new Equals(10), matcherInt20);
        Integer[] suspicious = tool.getSuspiciouslyNotMatchingArgsIndexes(matchers, new Object[] {10, longPretendingAnInt});
        
        
        assertEquals(1, suspicious.length);
        assertEquals(new Integer(1), suspicious[0]);
    }

// org.mockito.internal.verification.argumentmatching.ArgumentMatchingToolTest::shouldNotFindSuspiciousMatchersWhenTypesAreTheSame
    public void shouldNotFindSuspiciousMatchersWhenTypesAreTheSame() {
        
        Equals matcherWithBadDescription = new Equals(20) {
            public void describeTo(Description desc) {
                
                desc.appendText("10");
            }
        };
        Integer argument = 10;
        
        
        Integer[] suspicious = tool.getSuspiciouslyNotMatchingArgsIndexes((List) Arrays.asList(matcherWithBadDescription), new Object[] {argument});
        
        
        assertEquals(0, suspicious.length);
    }

// org.mockito.internal.verification.argumentmatching.ArgumentMatchingToolTest::shouldWorkFineWhenGivenArgIsNull
    public void shouldWorkFineWhenGivenArgIsNull() {
        
        Integer[] suspicious = tool.getSuspiciouslyNotMatchingArgsIndexes((List) Arrays.asList(new Equals(20)), new Object[] {null});
        
        
        assertEquals(0, suspicious.length);
    }

// org.mockito.internal.verification.argumentmatching.ArgumentMatchingToolTest::shouldUseMatchersSafely
    public void shouldUseMatchersSafely() {
        
        List<Matcher> matchers = (List) Arrays.asList(new BaseMatcher() {
            public boolean matches(Object item) {
                throw new ClassCastException("nasty matcher");
            }

            public void describeTo(Description description) {
            }});
        
        
        Integer[] suspicious = tool.getSuspiciouslyNotMatchingArgsIndexes(matchers, new Object[] {10});
        
        
        assertEquals(0, suspicious.length);
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

// org.mockito.runners.RunnersValidateFrameworkUsageTest::dummy
        @Test public void dummy() throws Exception {}

// org.mockito.runners.RunnersValidateFrameworkUsageTest::shouldValidateWithDefaultRunner
    public void shouldValidateWithDefaultRunner() throws Exception {
        
        runner = new MockitoJUnitRunner(DummyTest.class);

        
        runner.run(notifier);
        
        
        assertThat(notifier.addedListeners, contains(clazz(FrameworkUsageValidator.class)));
    }

// org.mockito.runners.RunnersValidateFrameworkUsageTest::shouldValidateWithD44Runner
    public void shouldValidateWithD44Runner() throws Exception {
        
        runner = new MockitoJUnit44Runner(DummyTest.class);

        
        runner.run(notifier);
        
        
        assertThat(notifier.addedListeners, contains(clazz(FrameworkUsageValidator.class)));
    }

// org.mockito.runners.RunnersValidateFrameworkUsageTest::shouldValidateWithVerboseRunner
    public void shouldValidateWithVerboseRunner() throws Exception {
        
        runner = new ConsoleSpammingMockitoJUnitRunner(DummyTest.class);
        
        
        runner.run(notifier);
        
        
        assertEquals(2, notifier.addedListeners.size());
        assertThat(notifier.addedListeners, contains(clazz(FrameworkUsageValidator.class)));
    }

// org.mockito.verification.TimeoutTest::should_pass_when_verification_passes
    public void should_pass_when_verification_passes() {
        Timeout t = new Timeout(1, 3, mode, timer);

        when(timer.isCounting()).thenReturn(true);
        doNothing().when(mode).verify(data);

        t.verify(data);

        InOrder inOrder = inOrder(timer);
        inOrder.verify(timer).start();
        inOrder.verify(timer).isCounting();
    }

// org.mockito.verification.TimeoutTest::should_fail_because_verification_fails
    public void should_fail_because_verification_fails() {
        Timeout t = new Timeout(1, 2, mode, timer);

        when(timer.isCounting()).thenReturn(true, true, true, false);
        doThrow(error).
        doThrow(error).
        doThrow(error).
        when(mode).verify(data);
        
        try {
            t.verify(data);
            fail();
        } catch (MockitoAssertionError e) {}

        verify(timer, times(4)).isCounting();
    }

// org.mockito.verification.TimeoutTest::should_pass_even_if_first_verification_fails
    public void should_pass_even_if_first_verification_fails() {}

// org.mockito.verification.TimeoutTest::should_try_to_verify_correct_number_of_times
    public void should_try_to_verify_correct_number_of_times() {}

// org.mockito.verification.TimeoutTest::should_create_correctly_configured_timeout
    public void should_create_correctly_configured_timeout() {
        Timeout t = new Timeout(25, 50, mode, timer);
        
        assertTimeoutCorrectlyConfigured(t.atLeastOnce(), Timeout.class, 50, 25, AtLeast.class);
        assertTimeoutCorrectlyConfigured(t.atLeast(5), Timeout.class, 50, 25, AtLeast.class);
        assertTimeoutCorrectlyConfigured(t.times(5), Timeout.class, 50, 25, Times.class);
        assertTimeoutCorrectlyConfigured(t.only(), Timeout.class, 50, 25, Only.class);
    }

// org.mockitointegration.NoJUnitDependenciesTest::pure_mockito_should_not_depend_JUnit
    public void pure_mockito_should_not_depend_JUnit() throws Exception {
        ClassLoader classLoader_without_JUnit = ClassLoaders.excludingClassLoader()
                .withCodeSourceUrlOf(
                        Mockito.class,
                        Matcher.class,
                        Enhancer.class,
                        Objenesis.class
                )
                .without("junit", "org.junit")
                .build();

        Set<String> pureMockitoAPIClasses = ClassLoaders.in(classLoader_without_JUnit).omit("runners", "junit", "JUnit").listOwnedClasses();

        for (String pureMockitoAPIClass : pureMockitoAPIClasses) {
            checkDependency(classLoader_without_JUnit, pureMockitoAPIClass);
        }
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

// org.mockitousage.annotation.CaptorAnnotationUnhappyPathTest::shouldFailIfCaptorHasWrongType
    public void shouldFailIfCaptorHasWrongType() throws Exception {
        try {
            
            MockitoAnnotations.initMocks(this);
            fail();
        } catch (MockitoException e) {
            
            assertContains("notACaptorField", e.getMessage());
            assertContains("wrong type", e.getMessage());
        }
    }

// org.mockitousage.annotation.DeprecatedAnnotationEngineApiTest::shouldInjectMocksIfThereIsNoUserDefinedEngine
    public void shouldInjectMocksIfThereIsNoUserDefinedEngine() throws Exception {
        
        AnnotationEngine defaultEngine = new DefaultMockitoConfiguration().getAnnotationEngine();
        ConfigurationAccess.getConfig().overrideAnnotationEngine(defaultEngine);
        SimpleTestCase test = new SimpleTestCase();
        
        
        MockitoAnnotations.initMocks(test);
        
        
        assertNotNull(test.mock);
        assertNotNull(test.tested.dependency);
        assertSame(test.mock, test.tested.dependency);
    }

// org.mockitousage.annotation.DeprecatedAnnotationEngineApiTest::shouldRespectUsersEngine
    public void shouldRespectUsersEngine() throws Exception {
        
        AnnotationEngine customizedEngine = new DefaultAnnotationEngine() {  };
        ConfigurationAccess.getConfig().overrideAnnotationEngine(customizedEngine);
        SimpleTestCase test = new SimpleTestCase();
        
        
        MockitoAnnotations.initMocks(test);
        
        
        assertNotNull(test.mock);
        assertNull(test.tested.dependency);
    }

// org.mockitousage.annotation.DeprecatedMockAnnotationTest::shouldCreateMockForDeprecatedMockAnnotation
    public void shouldCreateMockForDeprecatedMockAnnotation() throws Exception {
        assertNotNull(deprecatedMock);
    }

// org.mockitousage.annotation.DeprecatedMockAnnotationTest::shouldInjectDeprecatedMockAnnotation
    public void shouldInjectDeprecatedMockAnnotation() throws Exception {
        assertNotNull(anInjectedObject.aFieldAwaitingInjection);
    }

// org.mockitousage.annotation.InjectionOfInlinedMockDeclarationTest::mock_declared_fields_shall_be_injected_too
    public void mock_declared_fields_shall_be_injected_too() throws Exception {
        assertNotNull(receiver.oldAntenna);
        assertNotNull(receiver.satelliteAntenna);
        assertNotNull(receiver.dvbtAntenna);
        assertNotNull(receiver.tuner);
    }

// org.mockitousage.annotation.InjectionOfInlinedMockDeclarationTest::unnamed_mocks_should_be_resolved_withe_their_field_names
    public void unnamed_mocks_should_be_resolved_withe_their_field_names() throws Exception {
        assertSame(oldAntenna, receiver.oldAntenna);
        assertSame(satelliteAntenna, receiver.satelliteAntenna);
    }

// org.mockitousage.annotation.InjectionOfInlinedMockDeclarationTest::named_mocks_should_be_resolved_with_their_name
    public void named_mocks_should_be_resolved_with_their_name() throws Exception {
        assertSame(antenna, receiver.dvbtAntenna);
    }

// org.mockitousage.annotation.InjectionOfInlinedMockDeclarationTest::inject_mocks_even_in_declared_spy
    public void inject_mocks_even_in_declared_spy() throws Exception {
        assertNotNull(spiedReceiver.oldAntenna);
        assertNotNull(spiedReceiver.tuner);
    }

// org.mockitousage.annotation.MockInjectionUsingConstructorIssue421Test::mockJustWorks
    public void mockJustWorks() {
	    issue421.checkIfMockIsInjected();
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

// org.mockitousage.annotation.SpyAnnotationInitializedInBaseClassTest::shouldInitSpiesInBaseClass
    public void shouldInitSpiesInBaseClass() throws Exception {
        
        SubClass subClass = new SubClass();
        
        MockitoAnnotations.initMocks(subClass);
        
        assertTrue(isMock(subClass.list));
    }

// org.mockitousage.annotation.SpyAnnotationInitializedInBaseClassTest::shouldInitSpiesInHierarchy
        public void shouldInitSpiesInHierarchy() throws Exception {
            assertTrue(isMock(spyInSubclass));
            assertTrue(isMock(spyInBaseclass));            
        }

// org.mockitousage.annotation.SpyAnnotationTest::should_init_spy_by_instance
    public void should_init_spy_by_instance() throws Exception {
        doReturn("foo").when(spiedList).get(10);
        assertEquals("foo", spiedList.get(10));
        assertTrue(spiedList.isEmpty());
    }

// org.mockitousage.annotation.SpyAnnotationTest::should_init_spy_and_automatically_create_instance
    public void should_init_spy_and_automatically_create_instance() throws Exception {
        when(staticTypeWithNoArgConstructor.toString()).thenReturn("x");
        when(staticTypeWithoutDefinedConstructor.toString()).thenReturn("y");
        assertEquals("x", staticTypeWithNoArgConstructor.toString());
        assertEquals("y", staticTypeWithoutDefinedConstructor.toString());
    }

// org.mockitousage.annotation.SpyAnnotationTest::should_prevent_spying_on_interfaces
    public void should_prevent_spying_on_interfaces() throws Exception {
		class WithSpy {
			@Spy List<String> list;
		}

		WithSpy withSpy = new WithSpy();
        try {
            MockitoAnnotations.initMocks(withSpy);
            fail();
        } catch (MockitoException e) {
            Assertions.assertThat(e.getMessage()).contains("is an interface and it cannot be spied on");
        }
    }

// org.mockitousage.annotation.SpyAnnotationTest::should_allow_spying_on_interfaces_when_instance_is_concrete
    public void should_allow_spying_on_interfaces_when_instance_is_concrete() throws Exception {
        class WithSpy {
            @Spy List<String> list = new LinkedList<String>();
        }

        WithSpy withSpy = new WithSpy();
        
        MockitoAnnotations.initMocks(withSpy);

        
        verify(withSpy.list, never()).clear();
    }

// org.mockitousage.annotation.SpyAnnotationTest::should_report_when_no_arg_less_constructor
    public void should_report_when_no_arg_less_constructor() throws Exception {
		class FailingSpy {
	        @Spy
            NoValidConstructor noValidConstructor;
		}

        try {
            MockitoAnnotations.initMocks(new FailingSpy());
            fail();
        } catch (MockitoException e) {
            Assertions.assertThat(e.getMessage()).contains("0-arg constructor");
        }
    }

// org.mockitousage.annotation.SpyAnnotationTest::should_report_when_constructor_is_explosive
    public void should_report_when_constructor_is_explosive() throws Exception {
		class FailingSpy {
	        @Spy
            ThrowingConstructor throwingConstructor;
		}

        try {
            MockitoAnnotations.initMocks(new FailingSpy());
            fail();
        } catch (MockitoException e) {
            Assertions.assertThat(e.getMessage()).contains("Unable to create mock instance");
        }
    }

// org.mockitousage.annotation.SpyAnnotationTest::should_spy_abstract_class
    public void should_spy_abstract_class() throws Exception {
		class SpyAbstractClass {
			@Spy AbstractList<String> list;
			
			List<String> asSingletonList(String s) {
				when(list.size()).thenReturn(1);
				when(list.get(0)).thenReturn(s);
				return list;
			}
		}
		SpyAbstractClass withSpy = new SpyAbstractClass();
        MockitoAnnotations.initMocks(withSpy);
        assertEquals(Arrays.asList("a"), withSpy.asSingletonList("a"));
    }

// org.mockitousage.annotation.SpyAnnotationTest::should_spy_inner_class
    public void should_spy_inner_class() throws Exception {
    	 
     class WithMockAndSpy {
    		@Spy private InnerStrength strength;
    		@Mock private List<String> list;

            abstract class InnerStrength {
            	private final String name;

            	InnerStrength() {
            		
            		assertNotNull(list);
            		
            		this.name = "inner";
            	}
            	
            	abstract String strength();
            	
            	String fullStrength() {
            		return name + " " + strength();
            	}
            }
    	}
		WithMockAndSpy outer = new WithMockAndSpy();
        MockitoAnnotations.initMocks(outer);
        when(outer.strength.strength()).thenReturn("strength");
        assertEquals("inner strength", outer.strength.fullStrength());
    }

// org.mockitousage.annotation.SpyAnnotationTest::should_reset_spy
    public void should_reset_spy() throws Exception {
        spiedList.get(10); 
    }

// org.mockitousage.annotation.SpyAnnotationTest::should_report_when_encosing_instance_is_needed
	public void should_report_when_encosing_instance_is_needed() throws Exception {
		class Outer {
			class Inner {}
		}
		class WithSpy {
			@Spy private Outer.Inner inner;
		}
		try {
            MockitoAnnotations.initMocks(new WithSpy());
            fail();
        } catch (MockitoException e) {
            assertContains("@Spy annotation can only initialize inner classes", e.getMessage());
        }
	}

// org.mockitousage.annotation.SpyInjectionTest::shouldDoStuff
    public void shouldDoStuff() throws Exception {
        isMock(hasSpy.spy);
    }

// org.mockitousage.annotation.WrongSetOfAnnotationsTest::should_not_allow_Mock_and_Spy
    public void should_not_allow_Mock_and_Spy() throws Exception {
        MockitoAnnotations.initMocks(new Object() {
            @Mock @Spy List mock;
        });
    }

// org.mockitousage.annotation.WrongSetOfAnnotationsTest::should_not_allow_Spy_and_or_InjectMocks_on_interfaces
    public void should_not_allow_Spy_and_or_InjectMocks_on_interfaces() throws Exception {
        try {
            MockitoAnnotations.initMocks(new Object() { @InjectMocks @Spy List mock; });
            fail();
        } catch (MockitoException me) {
            Assertions.assertThat(me.getMessage()).contains("'List' is an interface");
        }
        try {
            MockitoAnnotations.initMocks(new Object() { @Spy List mock; });
            fail();
        } catch (MockitoException me) {
            Assertions.assertThat(me.getMessage()).contains("'List' is an interface");
        }
    }

// org.mockitousage.annotation.WrongSetOfAnnotationsTest::should_not_allow_Mock_and_InjectMocks
    public void should_not_allow_Mock_and_InjectMocks() throws Exception {
        MockitoAnnotations.initMocks(new Object() {
            @InjectMocks @Mock List mock;
        });
    }

// org.mockitousage.annotation.WrongSetOfAnnotationsTest::should_not_allow_Captor_and_Mock
    public void should_not_allow_Captor_and_Mock() throws Exception {
        MockitoAnnotations.initMocks(new Object() {
            @Mock @Captor ArgumentCaptor captor;
        });
    }

// org.mockitousage.annotation.WrongSetOfAnnotationsTest::should_not_allow_Captor_and_Spy
    public void should_not_allow_Captor_and_Spy() throws Exception {
        MockitoAnnotations.initMocks(new Object() {
            @Spy @Captor ArgumentCaptor captor;
        });
    }

// org.mockitousage.annotation.WrongSetOfAnnotationsTest::should_not_allow_Captor_and_InjectMocks
    public void should_not_allow_Captor_and_InjectMocks() throws Exception {
        MockitoAnnotations.initMocks(new Object() {
            @InjectMocks @Captor ArgumentCaptor captor;
        });
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

// org.mockitousage.basicapi.MockingDetailsTest::should_know_spy
    public void should_know_spy(){
        assertTrue(mockingDetails(annotatedSpy).isMock());
        assertTrue(mockingDetails(spy( new TestClass())).isMock());
        assertTrue(mockingDetails(spy(TestClass.class)).isMock());
        assertTrue(mockingDetails(mock(TestClass.class, withSettings().defaultAnswer(Mockito.CALLS_REAL_METHODS))).isMock());

        assertTrue(mockingDetails(annotatedSpy).isSpy());
        assertTrue(mockingDetails(spy( new TestClass())).isSpy());
        assertTrue(mockingDetails(spy(TestClass.class)).isSpy());
        assertTrue(mockingDetails(mock(TestClass.class, withSettings().defaultAnswer(Mockito.CALLS_REAL_METHODS))).isSpy());
    }

// org.mockitousage.basicapi.MockingDetailsTest::should_know_mock
    public void should_know_mock(){
        assertTrue(mockingDetails(annotatedMock).isMock());
        assertTrue(mockingDetails(mock(TestClass.class)).isMock());

        assertFalse(mockingDetails(annotatedMock).isSpy());
        assertFalse(mockingDetails(mock(TestClass.class)).isSpy());
    }

// org.mockitousage.basicapi.MockingDetailsTest::should_handle_non_mocks
    public void should_handle_non_mocks() {
        assertFalse(mockingDetails("non mock").isSpy());
        assertFalse(mockingDetails("non mock").isMock());

        assertFalse(mockingDetails(null).isSpy());
        assertFalse(mockingDetails(null).isMock());
    }

// org.mockitousage.basicapi.MockingMultipleInterfacesTest::should_allow_multiple_interfaces
    public void should_allow_multiple_interfaces() {
        
        Foo mock = mock(Foo.class, withSettings().extraInterfaces(IFoo.class, IBar.class));
        
        
        assertThat(mock).isInstanceOf(IFoo.class);
        assertThat(mock).isInstanceOf(IBar.class);
    }

// org.mockitousage.basicapi.MockingMultipleInterfacesTest::should_scream_when_null_passed_instead_of_an_interface
    public void should_scream_when_null_passed_instead_of_an_interface() {
        try {
            
            mock(Foo.class, withSettings().extraInterfaces(IFoo.class, null));
            fail();
        } catch (MockitoException e) {
            
            assertThat(e.getMessage()).contains("extraInterfaces() does not accept null parameters");
        }
    }

// org.mockitousage.basicapi.MockingMultipleInterfacesTest::should_scream_when_no_args_passed
    public void should_scream_when_no_args_passed() {
        try {
            
            mock(Foo.class, withSettings().extraInterfaces());
            fail();
        } catch (MockitoException e) {
            
            assertThat(e.getMessage()).contains("extraInterfaces() requires at least one interface");
        }
    }

// org.mockitousage.basicapi.MockingMultipleInterfacesTest::should_scream_when_null_passed_instead_of_an_array
    public void should_scream_when_null_passed_instead_of_an_array() {
        try {
            
            mock(Foo.class, withSettings().extraInterfaces((Class[]) null));
            fail();
        } catch (MockitoException e) {
            
            assertThat(e.getMessage()).contains("extraInterfaces() requires at least one interface");
        }
    }

// org.mockitousage.basicapi.MockingMultipleInterfacesTest::should_scream_when_non_interface_passed
    public void should_scream_when_non_interface_passed() {
        try {
            
            mock(Foo.class, withSettings().extraInterfaces(Foo.class));
            fail();
        } catch (MockitoException e) {
            
            assertThat(e.getMessage()).contains("Foo which is not an interface");
        }
    }

// org.mockitousage.basicapi.MockingMultipleInterfacesTest::should_scream_when_the_same_interfaces_passed
    public void should_scream_when_the_same_interfaces_passed() {
        try {
            
            mock(IMethods.class, withSettings().extraInterfaces(IMethods.class));
            fail();
        } catch (MockitoException e) {
            
            assertThat(e.getMessage()).contains("You mocked following type: IMethods");
        }
    }

// org.mockitousage.basicapi.MockingMultipleInterfacesTest::should_mock_class_with_interfaces_of_different_class_loader_AND_different_classpaths
    public void should_mock_class_with_interfaces_of_different_class_loader_AND_different_classpaths() throws ClassNotFoundException {
        
        Class<?> interface1 = inMemoryClassLoader()
                .withClassDefinition("test.Interface1", makeMarkerInterface("test.Interface1"))
                .build()
                .loadClass("test.Interface1");
        Class<?> interface2 = inMemoryClassLoader()
                .withClassDefinition("test.Interface2", makeMarkerInterface("test.Interface2"))
                .build()
                .loadClass("test.Interface2");

        Object mocked = mock(interface1, withSettings().extraInterfaces(interface2));
        assertThat(interface2.isInstance(mocked)).describedAs("mock should be assignable from interface2 type").isTrue();
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

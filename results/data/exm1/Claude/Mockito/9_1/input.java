// buggy code
    public Object answer(InvocationOnMock invocation) throws Throwable {
        return invocation.callRealMethod();
    }

// relevant test
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

// org.mockito.junit.MockitoJUnitRuleTest::testInjectMocks
    public void testInjectMocks() throws Exception {
        assertNotNull("Mock created", injected);
        assertNotNull("Object created", injectInto);
        assertEquals("A injected into B", injected, injectInto.getInjected());

    }

// org.mockito.junit.MockitoJUnitRuleTest::testThrowExceptionWhenNullTarget
    public void testThrowExceptionWhenNullTarget() throws Exception {
        try {
            new MockitoJUnitRule(null);
            fail();
        } catch (NullPointerException e) {
            assertEquals("valid message", "Mockito JUnit rule target should not be null", e.getMessage());
        }
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

// org.mockitousage.annotation.SpyInjectionTest::shouldDoStuff
    public void shouldDoStuff() throws Exception {
        isMock(hasSpy.spy);
    }

// org.mockitousage.annotation.WrongSetOfAnnotationsTest::shouldNotAllowMockAndSpy
    public void shouldNotAllowMockAndSpy() throws Exception {
        MockitoAnnotations.initMocks(new Object() {
            @Mock @Spy List mock;
        });
    }

// org.mockitousage.annotation.WrongSetOfAnnotationsTest::shouldNotAllowSpyAndInjectMock
    public void shouldNotAllowSpyAndInjectMock() throws Exception {
        MockitoAnnotations.initMocks(new Object() {
            @InjectMocks @Spy List mock;
        });
    }

// org.mockitousage.annotation.WrongSetOfAnnotationsTest::shouldNotAllowMockAndInjectMock
    public void shouldNotAllowMockAndInjectMock() throws Exception {
        MockitoAnnotations.initMocks(new Object() {
            @InjectMocks @Mock List mock;
        });
    }

// org.mockitousage.annotation.WrongSetOfAnnotationsTest::shouldNotAllowCaptorAndMock
    public void shouldNotAllowCaptorAndMock() throws Exception {
        MockitoAnnotations.initMocks(new Object() {
            @Mock @Captor ArgumentCaptor captor;
        });
    }

// org.mockitousage.annotation.WrongSetOfAnnotationsTest::shouldNotAllowCaptorAndSpy
    public void shouldNotAllowCaptorAndSpy() throws Exception {
        MockitoAnnotations.initMocks(new Object() {
            @Spy @Captor ArgumentCaptor captor;
        });
    }

// org.mockitousage.annotation.WrongSetOfAnnotationsTest::shouldNotAllowCaptorAndInjectMock
    public void shouldNotAllowCaptorAndInjectMock() throws Exception {
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

// org.mockitousage.basicapi.MockingDetailsTest::shouldReturnTrue_FromIsMock_ForAnnotatedMock
    public void shouldReturnTrue_FromIsMock_ForAnnotatedMock(){
        assertTrue(mockingDetails(mock1).isMock());
    }

// org.mockitousage.basicapi.MockingDetailsTest::shouldReturnTrue_FromIsMock_ForDirectMock
    public void shouldReturnTrue_FromIsMock_ForDirectMock(){
        assertTrue(mockingDetails(mock2).isMock());
    }

// org.mockitousage.basicapi.MockingDetailsTest::shouldReturnTrue_FromIsMock_ForAnnotatedSpy
    public void shouldReturnTrue_FromIsMock_ForAnnotatedSpy(){
        assertTrue(mockingDetails(spy1).isMock());
    }

// org.mockitousage.basicapi.MockingDetailsTest::shouldReturnTrue_FromIsMock_ForDirectSpy
    public void shouldReturnTrue_FromIsMock_ForDirectSpy(){

        assertTrue(mockingDetails(spy2).isMock());
    }

// org.mockitousage.basicapi.MockingDetailsTest::shouldReturnFalse_FromIsMock_ForNonMock
    public void shouldReturnFalse_FromIsMock_ForNonMock(){
        assertFalse(mockingDetails(nonMock).isMock());
    }

// org.mockitousage.basicapi.MockingDetailsTest::shouldReturnFalse_FromIsSpy_ForAnnotatedMock
    public void shouldReturnFalse_FromIsSpy_ForAnnotatedMock(){
        assertFalse(mockingDetails(mock1).isSpy());
    }

// org.mockitousage.basicapi.MockingDetailsTest::shouldReturnFalse_FromIsSpy_ForDirectMock
    public void shouldReturnFalse_FromIsSpy_ForDirectMock(){
        assertFalse(mockingDetails(mock2).isSpy());
    }

// org.mockitousage.basicapi.MockingDetailsTest::shouldReturnTrue_FromIsSpy_ForAnnotatedSpy
    public void shouldReturnTrue_FromIsSpy_ForAnnotatedSpy(){
        assertTrue(mockingDetails(spy1).isSpy());
    }

// org.mockitousage.basicapi.MockingDetailsTest::shouldReturnTrue_FromIsSpy_ForDirectSpy
    public void shouldReturnTrue_FromIsSpy_ForDirectSpy(){
        assertTrue(mockingDetails(spy2).isSpy());
    }

// org.mockitousage.basicapi.MockingDetailsTest::shouldReturnFalse_FromIsSpy_ForNonMock
    public void shouldReturnFalse_FromIsSpy_ForNonMock(){
        assertFalse(mockingDetails(nonMock).isSpy());
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

// org.mockitousage.bugs.ChildWithSameParentFieldInjectionTest::parent_field_is_not_null
    public void parent_field_is_not_null() {
        assertNotNull(((AbstractSystem) system).someService);
    }

// org.mockitousage.bugs.ChildWithSameParentFieldInjectionTest::child_field_is_not_null
    public void child_field_is_not_null() {
        assertNotNull(system.someService);
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

// org.mockitousage.bugs.FinalHashCodeAndEqualsRaiseNPEInInitMocksTest::dont_raise_NullPointerException
    public void dont_raise_NullPointerException() throws Exception {
        MockitoAnnotations.initMocks(this);
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

// org.mockitousage.bugs.InjectMocksShouldTryPropertySettersFirstBeforeFieldAccessTest::shouldInjectUsingPropertySetterIfAvailable
    public void shouldInjectUsingPropertySetterIfAvailable() {
        assertTrue(awaitingInjection.propertySetterUsed);
    }

// org.mockitousage.bugs.InjectMocksShouldTryPropertySettersFirstBeforeFieldAccessTest::shouldInjectFieldIfNoSetter
    public void shouldInjectFieldIfNoSetter() {
        assertEquals(fieldAccess, awaitingInjection.fieldAccess);
    }

// org.mockitousage.bugs.InjectionByTypeShouldFirstLookForExactTypeThenAncestorTest::just_for_information_fields_are_read_in_declaration_order_see_Service
    public void just_for_information_fields_are_read_in_declaration_order_see_Service() {
        Field[] declaredFields = Service.class.getDeclaredFields();

        assertEquals("mockShouldNotGoInHere", declaredFields[0].getName());
        assertEquals("mockShouldGoInHere", declaredFields[1].getName());
    }

// org.mockitousage.bugs.InjectionByTypeShouldFirstLookForExactTypeThenAncestorTest::mock_should_be_injected_once_and_in_the_best_matching_type
    public void mock_should_be_injected_once_and_in_the_best_matching_type() {
        assertSame(REFERENCE, illegalInjectionExample.mockShouldNotGoInHere);
        assertSame(mockedBean, illegalInjectionExample.mockShouldGoInHere);
    }

// org.mockitousage.bugs.InjectionByTypeShouldFirstLookForExactTypeThenAncestorTest::should_match_be_consistent_regardless_of_order
    public void should_match_be_consistent_regardless_of_order() {
        assertSame(REFERENCE, reversedOrderService.mockShouldNotGoInHere);
        assertSame(mockedBean, reversedOrderService.mockShouldGoInHere);
    }

// org.mockitousage.bugs.InjectionByTypeShouldFirstLookForExactTypeThenAncestorTest::should_inject_the_mock_only_once_and_in_the_correct_type
    public void should_inject_the_mock_only_once_and_in_the_correct_type() {
        assertNull(withNullObjectField.keepMeNull);
        assertSame(mockedBean, withNullObjectField.injectMePlease);
    }

// org.mockitousage.bugs.Issue353InjectionMightNotHappenInCertainConfigurationTest::when_identical_types_and_the_correct_mock_name_is_greater_than_the_non_matching_name_then_injection_occurs_only_on_the_named_one
    public void when_identical_types_and_the_correct_mock_name_is_greater_than_the_non_matching_name_then_injection_occurs_only_on_the_named_one() {
        assertThat("stringString_that_matches_field".compareTo("mockStringInteger_was_not_injected")).isGreaterThanOrEqualTo(1);

        assertSame(stringString_that_matches_field, fooService.stringString_that_matches_field);
        assertSame(mockStringInteger_was_not_injected, fooService.stringInteger_field);
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

// org.mockitousage.bugs.ParentClassNotPublicTest::hints_that_parent_not_public_during_stubbing
    public void hints_that_parent_not_public_during_stubbing() throws Exception {
        ClassForMocking clazzMock = mock(ClassForMocking.class);
        try {
            when(clazzMock.isValid()).thenReturn(true);
            fail();
        } catch (MissingMethodInvocationException e) {
            Assertions.assertThat(e.getMessage())
                    .contains(MockitoLimitations.NON_PUBLIC_PARENT);
        }
    }

// org.mockitousage.bugs.ParentClassNotPublicTest::hints_that_parent_not_public_during_stubbing_start
    public void hints_that_parent_not_public_during_stubbing_start() throws Exception {
        ClassForMocking clazzMock = mock(ClassForMocking.class);
        mock(List.class).clear();
        try {
            
            when(clazzMock.isValid()).thenReturn(true);
            fail();
        } catch (CannotStubVoidMethodWithReturnValue e) {
            Assertions.assertThat(e.getMessage())
                    .contains(MockitoLimitations.NON_PUBLIC_PARENT);
        }
    }

// org.mockitousage.bugs.ParentClassNotPublicTest::hints_that_parent_not_public_during_verify
    public void hints_that_parent_not_public_during_verify() throws Exception {
        ClassForMocking clazzMock = mock(ClassForMocking.class);
        verify(clazzMock).isValid();
        try {
            
            verify(clazzMock);
            fail();
        } catch (UnfinishedVerificationException e) {
            Assertions.assertThat(e.getMessage())
                    .contains(MockitoLimitations.NON_PUBLIC_PARENT);
        }
    }

// org.mockitousage.bugs.ParentClassNotPublicTest::hints_that_parent_not_public_when_misplaced_matchers_detected
    public void hints_that_parent_not_public_when_misplaced_matchers_detected() throws Exception {
        ClassForMocking clazzMock = mock(ClassForMocking.class);
        try {
            
            when(clazzMock.arg(anyObject())).thenReturn(0);
            fail();
        } catch (InvalidUseOfMatchersException e) {
            Assertions.assertThat(e.getMessage())
                    .contains(MockitoLimitations.NON_PUBLIC_PARENT);
        }
    }

// org.mockitousage.bugs.ParentTestMockInjectionTest::injectMocksShouldInjectMocksFromTestSuperClasses
    public void injectMocksShouldInjectMocksFromTestSuperClasses() {
        ImplicitTest it = new ImplicitTest();
        MockitoAnnotations.initMocks(it);

        assertNotNull(it.daoFromParent);
        assertNotNull(it.daoFromSub);
        assertNotNull(it.sut.daoFromParent);
        assertNotNull(it.sut.daoFromSub);
    }

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
            assertContains("Please ensure it has parameter-less constructor", e.getCause().getMessage());
        }
    }

// org.mockitousage.constructor.CreatingMocksWithConstructorTest::mocking_inner_classes_with_wrong_outer_instance
    public void mocking_inner_classes_with_wrong_outer_instance() {
        try {
            
            mock(InnerClass.class, withSettings().useConstructor().outerInstance("foo").defaultAnswer(CALLS_REAL_METHODS));
            
            fail();
        } catch (MockitoException e) {
            assertEquals("Unable to create mock instance of type 'InnerClass'", e.getMessage());
            assertContains("Please ensure that the outer instance has correct type and that the target class has parameter-less constructor", e.getCause().getMessage());
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

// org.mockitousage.constructor.CreatingMocksWithConstructorTest::abstractMethodReturnsDefault
    public void abstractMethodReturnsDefault() {
    	AbstractThing thing = spy(AbstractThing.class);
    	assertEquals("abstract null", thing.fullName());
    }

// org.mockitousage.constructor.CreatingMocksWithConstructorTest::abstractMethodStubbed
    public void abstractMethodStubbed() {
    	AbstractThing thing = spy(AbstractThing.class);
    	when(thing.name()).thenReturn("me");
    	assertEquals("abstract me", thing.fullName());
    }

// org.mockitousage.constructor.CreatingMocksWithConstructorTest::testCallsRealInterfaceMethod
    public void testCallsRealInterfaceMethod() {
    	List<String> list = mock(List.class, withSettings().defaultAnswer(CALLS_REAL_METHODS));
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

// org.mockitousage.junitrunner.VerboseMockitoRunnerTest::test
        public void test() {
            IMethods mock = mock(IMethods.class);
            mock.simpleMethod(1);
            mock.otherMethod();
            
            verify(mock).simpleMethod(1);
            throw new RuntimeException("boo");
        }

// org.mockitousage.junitrunner.VerboseMockitoRunnerTest::testIgnored
        public void testIgnored() {}

// org.mockitousage.junitrunner.VerboseMockitoRunnerTest::shouldContainWarnings
    public void shouldContainWarnings() throws Exception {
        
        Result result = new JUnitCore().run(new ContainsWarnings());
        
        assertEquals(1, result.getFailures().size());
        Throwable exception = result.getFailures().get(0).getException();
        assertTrue(exception instanceof ExceptionIncludingMockitoWarnings);        
    }

// org.mockitousage.junitrunner.VerboseMockitoRunnerTest::shouldNotContainWarnings
    public void shouldNotContainWarnings() throws Exception {
        Result result = new JUnitCore().run(NoWarnings.class);
        assertEquals(1, result.getFailures().size());
        assertEquals("boo", result.getFailures().get(0).getException().getMessage());
    }

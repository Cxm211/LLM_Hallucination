// buggy code
    public OngoingInjecter filterCandidate(final Collection<Object> mocks, final Field field, final Object fieldInstance) {
        if(mocks.size() == 1) {
            final Object matchingMock = mocks.iterator().next();

            return new OngoingInjecter() {
                public boolean thenInject() {
                    try {
                            new FieldSetter(fieldInstance, field).set(matchingMock);
                    } catch (Exception e) {
                        throw new MockitoException("Problems injecting dependency in " + field.getName(), e);
                    }
                    return true;
                }
            };
        }

        return new OngoingInjecter() {
            public boolean thenInject() {
                return false;
            }
        };

    }

// relevant test
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

// org.mockitousage.annotation.MockInjectionTest::shouldKeepSameInstanceIfFieldInitialized
    public void shouldKeepSameInstanceIfFieldInitialized() {
        assertSame(baseUnderTestingInstance, initializedBase);
    }

// org.mockitousage.annotation.MockInjectionTest::shouldInitializeAnnotatedFieldIfNull
    public void shouldInitializeAnnotatedFieldIfNull() {
        assertNotNull(notInitializedBase);
    }

// org.mockitousage.annotation.MockInjectionTest::shouldIInjectMocksInSpy
    public void shouldIInjectMocksInSpy() {
        assertNotNull(initializedSpy.getAList());
    }

// org.mockitousage.annotation.MockInjectionTest::shouldInitializeSpyIfNullAndInjectMocks
    public void shouldInitializeSpyIfNullAndInjectMocks() {
        assertNotNull(notInitializedSpy);
        assertNotNull(notInitializedSpy.getAList());
    }

// org.mockitousage.annotation.MockInjectionTest::shouldInjectMocksIfAnnotated
	public void shouldInjectMocksIfAnnotated() {
		MockitoAnnotations.initMocks(this);
		assertSame(list, superUnderTest.getAList());
	}

// org.mockitousage.annotation.MockInjectionTest::shouldNotInjectIfNotAnnotated
	public void shouldNotInjectIfNotAnnotated() {
		MockitoAnnotations.initMocks(this);
		assertNull(superUnderTestWithoutInjection.getAList());
	}

// org.mockitousage.annotation.MockInjectionTest::shouldInjectMocksForClassHierarchyIfAnnotated
	public void shouldInjectMocksForClassHierarchyIfAnnotated() {
		MockitoAnnotations.initMocks(this);
		assertSame(list, baseUnderTest.getAList());
		assertSame(map, baseUnderTest.getAMap());
	}

// org.mockitousage.annotation.MockInjectionTest::shouldInjectMocksByName
	public void shouldInjectMocksByName() {
		MockitoAnnotations.initMocks(this);
		assertSame(histogram1, subUnderTest.getHistogram1());
		assertSame(histogram2, subUnderTest.getHistogram2());
	}

// org.mockitousage.annotation.MockInjectionTest::shouldInjectSpies
	public void shouldInjectSpies() {
		MockitoAnnotations.initMocks(this);
		assertSame(searchTree, otherBaseUnderTest.getSearchTree());
	}

// org.mockitousage.annotation.MockInjectionTest::shouldInstantiateInjectMockFieldIfPossible
    public void shouldInstantiateInjectMockFieldIfPossible() throws Exception {
        assertNotNull(notInitializedBase);
    }

// org.mockitousage.annotation.MockInjectionTest::shouldKeepInstanceOnInjectMockFieldIfPresent
    public void shouldKeepInstanceOnInjectMockFieldIfPresent() throws Exception {
        assertSame(baseUnderTestingInstance, initializedBase);
    }

// org.mockitousage.annotation.MockInjectionTest::shouldReportNicely
    public void shouldReportNicely() throws Exception {
        Object failing = new Object() {
            @InjectMocks
            ThrowingConstructor c;
        };
        try {
            MockitoAnnotations.initMocks(failing);
            fail();
        } catch (MockitoException e) {
            assertContains("correct usage of @InjectMocks", e.getMessage());
        }
    }

// org.mockitousage.annotation.SpyInjectionTest::shouldDoStuff
    public void shouldDoStuff() throws Exception {
        isMock(hasSpy.spy);
    }

// org.mockitousage.bugs.InjectMocksShouldTryPropertySettersFirstBeforeFieldAccessTest::shouldInjectUsingPropertySetterIfAvailable
    public void shouldInjectUsingPropertySetterIfAvailable() {
        assertTrue(awaitingInjection.propertySetterUsed);
    }

// org.mockitousage.bugs.InjectMocksShouldTryPropertySettersFirstBeforeFieldAccessTest::shouldInjectFieldIfNoSetter
    public void shouldInjectFieldIfNoSetter() {
        assertEquals(fieldAccess, awaitingInjection.fieldAccess);
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

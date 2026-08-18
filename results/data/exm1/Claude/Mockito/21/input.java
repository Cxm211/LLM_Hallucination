// buggy code
    public <T> T newInstance(Class<T> cls) {
        if (outerClassInstance == null) {
            return noArgConstructor(cls);
        }
        return withOuterClass(cls);
    }

    private <T> T withOuterClass(Class<T> cls) {
        try {
            //this is kind of overengineered because we don't need to support more params
            //however, I know we will be needing it :)
            Constructor<T> c = cls.getDeclaredConstructor(outerClassInstance.getClass());
            return c.newInstance(outerClassInstance);
        } catch (Exception e) {
            throw paramsException(cls, e);
        }
    }

    private static <T> InstantationException paramsException(Class<T> cls, Exception e) {
        return new InstantationException("Unable to create mock instance of '"
                + cls.getSimpleName() + "'.\nPlease ensure that the outer instance has correct type and that the target class has parameter-less constructor.", e);
    }

// relevant test
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
        } catch (InstantationException e) {
            assertEquals("Unable to create mock instance of 'SomeClass2'.\n" +
                    "Please ensure it has parameter-less constructor.", e.getMessage());
        }
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

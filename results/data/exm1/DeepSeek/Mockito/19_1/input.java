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

        
        assertThat(timer.isCounting(), is(true));
    }

// org.mockito.internal.util.TimerTest::should_return_false_when_time_run_out
    public void should_return_false_when_time_run_out() throws Exception {
        
        Timer timer = new Timer(0);
        timer.start();

        
        oneMillisecondPasses();

        
        assertThat(timer.isCounting(), is(false));
    }

// org.mockito.internal.util.TimerTest::should_throw_friendly_reminder_exception_when_duration_is_negative
    public void should_throw_friendly_reminder_exception_when_duration_is_negative() {
        try {
            new Timer(-1);
            Assert.fail("It is forbidden to create timer with negative value of timer's duration.");
        } catch (FriendlyReminderException e) {
            Assert.assertTrue(true);
        }
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
    public void noMoreInteractionsExceptionMessageShouldDescribeMock() {
        
        NoMoreInteractions n = new NoMoreInteractions();
        IMethods mock = mock(IMethods.class, "a mock");
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
        IMethods mock = mock(IMethods.class, "a mock");
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
        Timeout t = new Timeout(1, mode, timer);

        when(timer.isCounting()).thenReturn(true);
        doNothing().when(mode).verify(data);

        t.verify(data);

        InOrder inOrder = inOrder(timer);
        inOrder.verify(timer).start();
        inOrder.verify(timer).isCounting();
    }

// org.mockito.verification.TimeoutTest::should_fail_because_verification_fails
    public void should_fail_because_verification_fails() {
        Timeout t = new Timeout(1, mode, timer);

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

// org.mockitointegration.NoJUnitDependenciesTest::pure_mockito_should_not_depend_JUnit___ByteBuddy
    public void pure_mockito_should_not_depend_JUnit___ByteBuddy() throws Exception {
        Assume.assumeTrue("ByteBuddyMockMaker".equals(Plugins.getMockMaker().getClass().getSimpleName()));

        ClassLoader classLoader_without_JUnit = ClassLoaders.excludingClassLoader()
                .withCodeSourceUrlOf(
                        Mockito.class,
                        Matcher.class,
                        ByteBuddy.class,
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

// org.mockitousage.annotation.MockInjectionUsingSetterOrPropertyTest::shouldInsertFieldWithCorrectNameWhenMultipleTypesAvailable
	public void shouldInsertFieldWithCorrectNameWhenMultipleTypesAvailable() {
		MockitoAnnotations.initMocks(this);
		assertNull(otherSuperUnderTesting.candidate1);
		assertNotNull(otherSuperUnderTesting.candidate2);
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

// org.mockitousage.basicapi.ObjectsSerializationTest::shouldSerializationWork
    public void shouldSerializationWork() throws Exception {
        
        Foo foo = new Foo();
        
        foo = serializeAndBack(foo);
        
        assertSame(foo, foo.bar.foo);
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

// org.mockitousage.bugs.FinalHashCodeAndEqualsRaiseNPEInInitMocksTest::dont_raise_NullPointerException
    public void dont_raise_NullPointerException() throws Exception {
        MockitoAnnotations.initMocks(this);
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

// org.mockitousage.bugs.MockitoRunnerBreaksWhenNoTestMethodsTest::ensure_the_test_runner_breaks
    public void ensure_the_test_runner_breaks() throws Exception {
        JUnitCore runner = new JUnitCore();
        runner.addListener(new TextListener(System.out));

        Result result = runner.run(TestClassWithoutTestMethod.class);

        assertEquals(1, result.getFailureCount());
        assertFalse(result.wasSuccessful());
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

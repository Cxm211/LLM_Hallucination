// buggy code
    public void smartNullPointerException(Location location) {
        throw new SmartNullPointerException(join(
                "You have a NullPointerException here:",
                new Location(),
                "Because this method was *not* stubbed correctly:",
                location,
                ""
                ));
    }

        public Object intercept(Object obj, Method method, Object[] args, MethodProxy proxy) throws Throwable {
            if (new ObjectMethodsGuru().isToString(method)) {
                return "SmartNull returned by unstubbed " + formatMethodCall()  + " method on mock";
            }

            new Reporter().smartNullPointerException(location);
            return null;
        }

// relevant test
// org.mockito.internal.util.MockNameTest::shouldProvideTheNameForClass
    public void shouldProvideTheNameForClass() throws Exception {
        
        String name = new MockName(null, SomeClass.class).toString();
        
        assertEquals("someClass", name);
    }

// org.mockito.internal.util.MockNameTest::shouldProvideTheNameForAnonymousClass
    public void shouldProvideTheNameForAnonymousClass() throws Exception {
        
        SomeInterface anonymousInstance = new SomeInterface() {};
        
        String name = new MockName(null, anonymousInstance.getClass()).toString();
        
        assertEquals("someInterface", name);
    }

// org.mockito.internal.util.MockNameTest::shouldProvideTheGivenName
    public void shouldProvideTheGivenName() throws Exception {
        
        String name = new MockName("The Hulk", SomeClass.class).toString();
        
        assertEquals("The Hulk", name);
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

// org.mockito.internal.verification.VerificationDataImplTest::shouldToStringBeNotVerifiable
    public void shouldToStringBeNotVerifiable() throws Exception {
        InvocationMatcher toString = new InvocationBuilder().method("toString").toInvocationMatcher();
        try {
            new VerificationDataImpl(null, toString);
            fail();
        } catch (MockitoException e) {}
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
        assertCorrectMode(t.atMost(10), Timeout.class, 50, 25, AtMost.class);
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

// org.mockitousage.basicapi.MocksSerializationTest::shouldAllowThrowsExceptionToBeSerializable
    public void shouldAllowThrowsExceptionToBeSerializable() throws Exception {
        
        Bar mock = mock(Bar.class, new ThrowsException(new RuntimeException()));
        
        serializeAndBack(mock);
    }

// org.mockitousage.basicapi.MocksSerializationTest::shouldAllowMethodDelegation
    public void shouldAllowMethodDelegation() throws Exception {
        
        Bar barMock = mock(Bar.class, withSettings().serializable());
        Foo fooMock = mock(Foo.class);
        when(barMock.doSomething()).thenAnswer(new ThrowsException(new RuntimeException()));

        
        serializeAndBack(barMock);
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

// org.mockitousage.bugs.ShouldAllowInlineMockCreationTest::shouldAllowInlineMockCreation
    public void shouldAllowInlineMockCreation() {
        when(list.get(0)).thenReturn(mock(Set.class));
        assertTrue(list.get(0) instanceof Set);
    }

// org.mockitousage.bugs.ShouldMocksCompareToBeConsistentWithEqualsTest::shouldCompareToBeConsistentWithEquals
    public void shouldCompareToBeConsistentWithEquals() {
        
        Date today    = mock(Date.class);
        Date tomorrow = mock(Date.class);

        
        Set<Date> set = new TreeSet<Date>();
        set.add(today);
        set.add(tomorrow);

        
        assertEquals(2, set.size());
    }

// org.mockitousage.bugs.ShouldMocksCompareToBeConsistentWithEqualsTest::shouldAllowStubbingAndVerifyingCompareTo
    public void shouldAllowStubbingAndVerifyingCompareTo() {}

// org.mockitousage.bugs.ShouldMocksCompareToBeConsistentWithEqualsTest::shouldResetNotRemoveDefaultStubbing
    public void shouldResetNotRemoveDefaultStubbing() {
        
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

// org.mockitousage.internal.debugging.LocationTest::shouldLocationNotContainGetStackTraceMethod
    public void shouldLocationNotContainGetStackTraceMethod() {
        assertContains("shouldLocationNotContainGetStackTraceMethod", new Location().toString());
    }

// org.mockitousage.internal.debugging.LocationTest::shouldBeSafeInCaseForSomeReasonFilteredStackTraceIsEmpty
    public void shouldBeSafeInCaseForSomeReasonFilteredStackTraceIsEmpty() {
        
        StackTraceFilter filterReturningEmptyArray = new StackTraceFilter() {
            @Override
            public StackTraceElement[] filter(StackTraceElement[] target, boolean keepTop) {
                return new StackTraceElement[0];
            }
        };

        
        String loc = new Location(filterReturningEmptyArray).toString();

        
        assertEquals("-> at <<unknown line>>", loc);
    }

// org.mockitousage.internal.invocation.realmethod.FilteredCGLIBProxyRealMethodTest::shouldRemoveMockitoInternalsFromStackTraceWhenRealMethodThrows
    public void shouldRemoveMockitoInternalsFromStackTraceWhenRealMethodThrows() throws Throwable {
        
        FilteredCGLIBProxyRealMethod realMethod = new FilteredCGLIBProxyRealMethod(new RealMethod() {
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

// org.mockitousage.matchers.MatchersMixedWithRawArgumentsTest::shouldAllowMixingRawArgumentsWithMatchers
    public void shouldAllowMixingRawArgumentsWithMatchers() {
        mock.varargs("1", "2", "3");
        verify(mock).varargs("1", anyString(), "3");
        
        verify(mock).varargs(anyBoolean(), false);
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

// org.mockitousage.matchers.MatchersTest::anyMatcher
    public void anyMatcher() {
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

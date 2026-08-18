// buggy code
    public Object callRealMethod() throws Throwable {
        return realMethod.invoke(mock, rawArguments);
    }

// relevant test
// org.mockitousage.stubbing.ReturningDefaultValuesTest::shouldReturnTheSameValuesForWrapperClasses
    public void shouldReturnTheSameValuesForWrapperClasses() throws Exception {
        assertEquals(new Byte((byte) 0), mock.byteObjectReturningMethod());
        assertEquals(new Short((short) 0), mock.shortObjectReturningMethod());
        assertEquals(new Integer(0), mock.integerReturningMethod());
        assertEquals(new Long(0L), mock.longObjectReturningMethod());
        assertEquals(new Float(0.0F), mock.floatObjectReturningMethod(), 0.0F);
        assertEquals(new Double(0.0D), mock.doubleObjectReturningMethod(), 0.0D);
        assertEquals(new Character((char) 0), mock.charObjectReturningMethod());
        assertEquals(new Boolean(false), mock.booleanObjectReturningMethod());
    }

// org.mockitousage.stubbing.ReturningDefaultValuesTest::shouldReturnEmptyCollections
    public void shouldReturnEmptyCollections() {
        CollectionsServer mock = Mockito.mock(CollectionsServer.class);
        
        assertTrue(mock.list().isEmpty());
        assertTrue(mock.linkedList().isEmpty());
        assertTrue(mock.map().isEmpty());
        assertTrue(mock.hashSet().isEmpty());
    }

// org.mockitousage.stubbing.ReturningDefaultValuesTest::shouldReturnMutableEmptyCollection
    public void shouldReturnMutableEmptyCollection() {
        CollectionsServer mock = Mockito.mock(CollectionsServer.class);
        
        List list = mock.list();
        list.add("test");
       
        assertTrue(mock.list().isEmpty());
    }

// org.mockitousage.stubbing.ReturningMockValuesTest::should
    public void should() throws Exception {
        
    }

// org.mockitousage.stubbing.SmartNullsStubbingTest::shouldSmartNPEPointToUnstubbedCall
    public void shouldSmartNPEPointToUnstubbedCall() throws Exception {
        IMethods methods = unstubbedMethodInvokedHere(mock); 
        try {
            methods.simpleMethod();
            fail();
        } catch (SmartNullPointerException e) {
            assertContains("unstubbedMethodInvokedHere(", e.getMessage());
        }
    }

// org.mockitousage.stubbing.SmartNullsStubbingTest::shouldThrowSmartNPEWhenMethodReturnsClass
    public void shouldThrowSmartNPEWhenMethodReturnsClass() throws Exception {
        Foo mock = mock(Foo.class, RETURNS_SMART_NULLS);
        Foo foo = mock.getSomeClass(); 
        try {
            foo.boo();
            fail();
        } catch (SmartNullPointerException e) {}
    }

// org.mockitousage.stubbing.SmartNullsStubbingTest::shouldThrowSmartNPEWhenMethodReturnsInterface
    public void shouldThrowSmartNPEWhenMethodReturnsInterface() throws Exception {
        Foo mock = mock(Foo.class, RETURNS_SMART_NULLS);
        Bar bar = mock.getSomeInterface(); 
        try {
            bar.boo();
            fail();
        } catch (SmartNullPointerException e) {}
    }

// org.mockitousage.stubbing.SmartNullsStubbingTest::shouldReturnOrdinaryEmptyValuesForOrdinaryTypes
    public void shouldReturnOrdinaryEmptyValuesForOrdinaryTypes() throws Exception {
        IMethods mock = mock(IMethods.class, RETURNS_SMART_NULLS);
        
        assertEquals("", mock.stringReturningMethod());
        assertEquals(0, mock.intReturningMethod());
        assertEquals(true, mock.listReturningMethod().isEmpty());
        assertEquals(0, mock.arrayReturningMethod().length);
    }

// org.mockitousage.stubbing.SmartNullsStubbingTest::shouldNotThrowSmartNullPointerOnToString
    public void shouldNotThrowSmartNullPointerOnToString() {
        Object smartNull = mock.objectReturningMethod();
        try {
            verify(mock).simpleMethod(smartNull);
            fail();
        } catch (WantedButNotInvoked e) {}
    }

// org.mockitousage.stubbing.SmartNullsStubbingTest::shouldNotThrowSmartNullPointerOnObjectMethods
    public void shouldNotThrowSmartNullPointerOnObjectMethods() {
        Object smartNull = mock.objectReturningMethod();
        smartNull.toString();
    }

// org.mockitousage.stubbing.StubbingConsecutiveAnswersTest::shouldReturnConsecutiveValues
    public void shouldReturnConsecutiveValues() throws Exception {
        when(mock.simpleMethod())
            .thenReturn("one")
            .thenReturn("two")
            .thenReturn("three");
        
        assertEquals("one", mock.simpleMethod());
        assertEquals("two", mock.simpleMethod());
        assertEquals("three", mock.simpleMethod());
        assertEquals("three", mock.simpleMethod());
        assertEquals("three", mock.simpleMethod());
    }

// org.mockitousage.stubbing.StubbingConsecutiveAnswersTest::shouldReturnConsecutiveValuesForTwoNulls
    public void shouldReturnConsecutiveValuesForTwoNulls() throws Exception {
        when(mock.simpleMethod()).thenReturn(null, null);
        
        assertNull(mock.simpleMethod());        
        assertNull(mock.simpleMethod());        
    }

// org.mockitousage.stubbing.StubbingConsecutiveAnswersTest::shouldReturnConsecutiveValuesSetByShortenThenReturnMethod
    public void shouldReturnConsecutiveValuesSetByShortenThenReturnMethod() throws Exception {        
        when(mock.simpleMethod())
            .thenReturn("one", "two", "three");
        
        assertEquals("one", mock.simpleMethod());
        assertEquals("two", mock.simpleMethod());
        assertEquals("three", mock.simpleMethod());
        assertEquals("three", mock.simpleMethod());
        assertEquals("three", mock.simpleMethod());
    }

// org.mockitousage.stubbing.StubbingConsecutiveAnswersTest::shouldReturnConsecutiveValueAndThrowExceptionssSetByShortenReturnMethods
    public void shouldReturnConsecutiveValueAndThrowExceptionssSetByShortenReturnMethods()
            throws Exception {
        when(mock.simpleMethod())
            .thenReturn("zero")
            .thenReturn("one", "two")
            .thenThrow(new NullPointerException(), new RuntimeException())
            .thenReturn("three")
            .thenThrow(new IllegalArgumentException());

        assertEquals("zero", mock.simpleMethod());
        assertEquals("one", mock.simpleMethod());
        assertEquals("two", mock.simpleMethod());
        try {
            mock.simpleMethod();
            fail();
        } catch (NullPointerException e) {}
        try {
            mock.simpleMethod();
            fail();
        } catch (RuntimeException e) {}
        assertEquals("three", mock.simpleMethod());
        try {
            mock.simpleMethod();
            fail();
        } catch (IllegalArgumentException e) {}
    }

// org.mockitousage.stubbing.StubbingConsecutiveAnswersTest::shouldThrowConsecutively
    public void shouldThrowConsecutively() throws Exception {
        when(mock.simpleMethod())
            .thenThrow(new RuntimeException())
            .thenThrow(new IllegalArgumentException())
            .thenThrow(new NullPointerException());

        try {
            mock.simpleMethod();
            fail();
        } catch (RuntimeException e) {}
        
        try {
            mock.simpleMethod();
            fail();
        } catch (IllegalArgumentException e) {}
        
        try {
            mock.simpleMethod();
            fail();
        } catch (NullPointerException e) {}
        
        try {
            mock.simpleMethod();
            fail();
        } catch (NullPointerException e) {}
    }

// org.mockitousage.stubbing.StubbingConsecutiveAnswersTest::shouldThrowConsecutivelySetByShortenThenThrowMethod
    public void shouldThrowConsecutivelySetByShortenThenThrowMethod() throws Exception {
        when(mock.simpleMethod())
            .thenThrow(new RuntimeException(), new IllegalArgumentException(), new NullPointerException());

        try {
            mock.simpleMethod();
            fail();
        } catch (RuntimeException e) {}
        
        try {
            mock.simpleMethod();
            fail();
        } catch (IllegalArgumentException e) {}
        
        try {
            mock.simpleMethod();
            fail();
        } catch (NullPointerException e) {}
        
        try {
            mock.simpleMethod();
            fail();
        } catch (NullPointerException e) {}
    }

// org.mockitousage.stubbing.StubbingConsecutiveAnswersTest::shouldMixConsecutiveReturnsWithExcepions
    public void shouldMixConsecutiveReturnsWithExcepions() throws Exception {
        when(mock.simpleMethod())
            .thenThrow(new IllegalArgumentException())
            .thenReturn("one")
            .thenThrow(new NullPointerException())
            .thenReturn(null);
        
        try {
            mock.simpleMethod();
            fail();
        } catch (IllegalArgumentException e) {}
        
        assertEquals("one", mock.simpleMethod());
        
        try {
            mock.simpleMethod();
            fail();
        } catch (NullPointerException e) {}
        
        assertEquals(null, mock.simpleMethod());
        assertEquals(null, mock.simpleMethod());
    }

// org.mockitousage.stubbing.StubbingConsecutiveAnswersTest::shouldValidateConsecutiveException
    public void shouldValidateConsecutiveException() throws Exception {
        when(mock.simpleMethod())
            .thenReturn("one")
            .thenThrow(new Exception());
    }

// org.mockitousage.stubbing.StubbingConsecutiveAnswersTest::shouldStubVoidMethodAndContinueThrowing
    public void shouldStubVoidMethodAndContinueThrowing() throws Exception {
        stubVoid(mock)
            .toThrow(new IllegalArgumentException())
            .toReturn()
            .toThrow(new NullPointerException())
            .on().voidMethod();
        
        try {
            mock.voidMethod();
            fail();
        } catch (IllegalArgumentException e) {}
        
        mock.voidMethod();
        
        try {
            mock.voidMethod();
            fail();
        } catch (NullPointerException e) {}
        
        try {
            mock.voidMethod();
            fail();
        } catch (NullPointerException e) {}        
    }

// org.mockitousage.stubbing.StubbingConsecutiveAnswersTest::shouldStubVoidMethod
    public void shouldStubVoidMethod() throws Exception {
        stubVoid(mock)
            .toReturn()
            .toThrow(new NullPointerException())
            .toReturn()
            .on().voidMethod();
        
        mock.voidMethod();
        
        try {
            mock.voidMethod();
            fail();
        } catch (NullPointerException e) {}
        
        mock.voidMethod();
        mock.voidMethod();
    }

// org.mockitousage.stubbing.StubbingConsecutiveAnswersTest::shouldValidateConsecutiveExceptionForVoidMethod
    public void shouldValidateConsecutiveExceptionForVoidMethod() throws Exception {
        stubVoid(mock)
            .toReturn()
            .toThrow(new Exception())
            .on().voidMethod();
    }

// org.mockitousage.stubbing.StubbingUsingDoReturnTest::shouldStub
    public void shouldStub() throws Exception {
        doReturn("foo").when(mock).simpleMethod();
        doReturn("bar").when(mock).simpleMethod();
        
        assertEquals("bar", mock.simpleMethod());
    }

// org.mockitousage.stubbing.StubbingUsingDoReturnTest::shouldStubWithArgs
    public void shouldStubWithArgs() throws Exception {
        doReturn("foo").when(mock).simpleMethod("foo");
        doReturn("bar").when(mock).simpleMethod(eq("one"), anyInt());
        
        assertEquals("foo", mock.simpleMethod("foo"));
        assertEquals("bar", mock.simpleMethod("one", 234));
        assertEquals(null, mock.simpleMethod("xxx", 234));
    }

// org.mockitousage.stubbing.StubbingUsingDoReturnTest::shouldStubWithThrowable
    public void shouldStubWithThrowable() throws Exception {
        doThrow(new FooRuntimeException()).when(mock).voidMethod();
        try {
            mock.voidMethod();
            fail();
        } catch (FooRuntimeException e) {}
    }

// org.mockitousage.stubbing.StubbingUsingDoReturnTest::shouldAllowSettingValidCheckedException
    public void shouldAllowSettingValidCheckedException() throws Exception {
        doThrow(new IOException()).when(mock).throwsIOException(0);
        
        try {
            mock.throwsIOException(0);
            fail();
        } catch (IOException e) {}
    }

// org.mockitousage.stubbing.StubbingUsingDoReturnTest::shouldDetectInvalidCheckedException
    public void shouldDetectInvalidCheckedException() throws Exception {
        try {
            doThrow(new FooCheckedException()).when(mock).throwsIOException(0);
            fail();
        } catch (Exception e) {
            assertContains("Checked exception is invalid", e.getMessage());
        }
    }

// org.mockitousage.stubbing.StubbingUsingDoReturnTest::shouldScreamWhenReturnSetForVoid
    public void shouldScreamWhenReturnSetForVoid() throws Exception {
        try {
            doReturn("foo").when(mock).voidMethod();
            fail();
        } catch (MockitoException e) {
            assertContains("Cannot stub a void method with a return value", e.getMessage());
        }
    }

// org.mockitousage.stubbing.StubbingUsingDoReturnTest::shouldScreamWhenNotAMockPassed
    public void shouldScreamWhenNotAMockPassed() throws Exception {
        try {
            doReturn("foo").when("foo").toString();
            fail();
        } catch (Exception e) {
            assertContains("Argument passed to when() is not a mock", e.getMessage());
        }
    }

// org.mockitousage.stubbing.StubbingUsingDoReturnTest::shouldScreamWhenNullPassed
    public void shouldScreamWhenNullPassed() throws Exception {
        try {
            doReturn("foo").when((Object) null).toString();
            fail();
        } catch (Exception e) {
            assertContains("Argument passed to when() is null", e.getMessage());
        }
    }

// org.mockitousage.stubbing.StubbingUsingDoReturnTest::shouldAllowChainedStubbing
    public void shouldAllowChainedStubbing() {
        doReturn("foo").
        doThrow(new RuntimeException()).
        doReturn("bar")
        .when(mock).simpleMethod();
        
        assertEquals("foo", mock.simpleMethod());
        try {
            mock.simpleMethod();
            fail();
        } catch (RuntimeException e) {}
        
        assertEquals("bar", mock.simpleMethod());
        assertEquals("bar", mock.simpleMethod());
    }

// org.mockitousage.stubbing.StubbingUsingDoReturnTest::shouldAllowChainedStubbingOnVoidMethods
    public void shouldAllowChainedStubbingOnVoidMethods() {
        doNothing().
        doNothing().
        doThrow(new RuntimeException())
        .when(mock).voidMethod();
        
        mock.voidMethod();
        mock.voidMethod();
        try {
            mock.voidMethod();
            fail();
        } catch (RuntimeException e) {}
        try {
            mock.voidMethod();
            fail();
        } catch (RuntimeException e) {}
    }

// org.mockitousage.stubbing.StubbingUsingDoReturnTest::shouldStubWithGenericAnswer
    public void shouldStubWithGenericAnswer() {
        doAnswer(new Answer<Object>() {
            public Object answer(InvocationOnMock invocation) throws Throwable {
                return "foo";
            }
        })
        .when(mock).simpleMethod();
        
        assertEquals("foo", mock.simpleMethod());
    }

// org.mockitousage.stubbing.StubbingUsingDoReturnTest::shouldNotAllowDoNothingOnNonVoids
    public void shouldNotAllowDoNothingOnNonVoids() {
        try {
            doNothing().when(mock).simpleMethod();
            fail();
        } catch (MockitoException e) {
            assertContains("Only void methods can doNothing()", e.getMessage());
        }
    }

// org.mockitousage.stubbing.StubbingUsingDoReturnTest::shouldStubbingBeTreatedAsInteraction
    public void shouldStubbingBeTreatedAsInteraction() throws Exception {
        doReturn("foo").when(mock).simpleMethod();
        mock.simpleMethod();
        try {
            verifyNoMoreInteractions(mock);
            fail();
        } catch (NoInteractionsWanted e) {}
    }

// org.mockitousage.stubbing.StubbingUsingDoReturnTest::shouldVerifyStubbedCall
    public void shouldVerifyStubbedCall() throws Exception {
        doReturn("foo").when(mock).simpleMethod();
        mock.simpleMethod();
        mock.simpleMethod();
        
        verify(mock, times(2)).simpleMethod();
        verifyNoMoreInteractions(mock);
    }

// org.mockitousage.stubbing.StubbingUsingDoReturnTest::shouldAllowStubbingToString
    public void shouldAllowStubbingToString() throws Exception {
        doReturn("test").when(mock).toString();
        assertEquals("test", mock.toString());
    }

// org.mockitousage.stubbing.StubbingUsingDoReturnTest::shouldDetectInvalidReturnType
    public void shouldDetectInvalidReturnType() throws Exception {
        try {
            doReturn("foo").when(mock).booleanObjectReturningMethod();
            fail();
        } catch (Exception e) {
            assertContains("String cannot be returned by booleanObjectReturningMethod()" +
                    "\n" +
                    "booleanObjectReturningMethod() should return Boolean",
                    e.getMessage());
        }
    }

// org.mockitousage.stubbing.StubbingUsingDoReturnTest::shouldDetectWhenNullAssignedToBoolean
    public void shouldDetectWhenNullAssignedToBoolean() throws Exception {
        try {
            doReturn(null).when(mock).intReturningMethod();
            fail();
        } catch (Exception e) {
            assertContains("null cannot be returned by intReturningMethod", e.getMessage());
        }
    }

// org.mockitousage.stubbing.StubbingUsingDoReturnTest::shouldAllowStubbingWhenTypesMatchSignature
    public void shouldAllowStubbingWhenTypesMatchSignature() throws Exception {
        doReturn("foo").when(mock).objectReturningMethodNoArgs();
        doReturn("foo").when(mock).simpleMethod();
        doReturn(1).when(mock).intReturningMethod();
        doReturn(new Integer(2)).when(mock).intReturningMethod();
    }

// org.mockitousage.stubbing.StubbingWithCustomAnswerTest::shouldAnswer
    public void shouldAnswer() throws Exception {
        when(mock.simpleMethod(anyString())).thenAnswer(new Answer<String>() {
            public String answer(InvocationOnMock invocation) throws Throwable {
                String arg = (String) invocation.getArguments()[0];

                return invocation.getMethod().getName() + "-" + arg;
            }
        });

        assertEquals("simpleMethod-test", mock.simpleMethod("test"));
    }

// org.mockitousage.stubbing.StubbingWithCustomAnswerTest::shouldAnswerConsecutively
    public void shouldAnswerConsecutively() throws Exception {
        when(mock.simpleMethod())
                .thenAnswer(new Answer<String>() {
                    public String answer(InvocationOnMock invocation) throws Throwable {
                        return invocation.getMethod().getName();
                    }
                })
                .thenReturn("Hello")
                .thenAnswer(new Answer<String>() {
                    public String answer(InvocationOnMock invocation) throws Throwable {
                        return invocation.getMethod().getName() + "-1";
                    }
                });

        assertEquals("simpleMethod", mock.simpleMethod());
        assertEquals("Hello", mock.simpleMethod());
        assertEquals("simpleMethod-1", mock.simpleMethod());
        assertEquals("simpleMethod-1", mock.simpleMethod());
    }

// org.mockitousage.stubbing.StubbingWithCustomAnswerTest::shoudAnswerVoidMethod
    public void shoudAnswerVoidMethod() throws Exception {
        RecordCall recordCall = new RecordCall();

        stubVoid(mock).toAnswer(recordCall).on().voidMethod();

        mock.voidMethod();
        assertTrue(recordCall.isCalled());
    }

// org.mockitousage.stubbing.StubbingWithCustomAnswerTest::shouldAnswerVoidMethodConsecutively
    public void shouldAnswerVoidMethodConsecutively() throws Exception {
        RecordCall call1 = new RecordCall();
        RecordCall call2 = new RecordCall();

        stubVoid(mock).toAnswer(call1)
                .toThrow(new UnsupportedOperationException())
                .toAnswer(call2)
                .on().voidMethod();

        mock.voidMethod();
        assertTrue(call1.isCalled());
        assertFalse(call2.isCalled());

        try {
            mock.voidMethod();
            fail();
        } catch (UnsupportedOperationException e) {
        }

        mock.voidMethod();
        assertTrue(call2.isCalled());

    }

// org.mockitousage.stubbing.StubbingWithThrowablesTest::shouldStubWithThrowable
    public void shouldStubWithThrowable() throws Exception {
        IllegalArgumentException expected = new IllegalArgumentException("thrown by mock");
        when(mock.add("throw")).thenThrow(expected);
        
        try {
            mock.add("throw");
            fail();
        } catch (IllegalArgumentException e) {
            assertEquals(expected, e);
        }
    }

// org.mockitousage.stubbing.StubbingWithThrowablesTest::shouldSetThrowableToVoidMethod
    public void shouldSetThrowableToVoidMethod() throws Exception {
        IllegalArgumentException expected = new IllegalArgumentException("thrown by mock");
        
        stubVoid(mock).toThrow(expected).on().clear();
        try {
            mock.clear();
            fail();
        } catch (Exception e) {
            assertEquals(expected, e);
        }
    }

// org.mockitousage.stubbing.StubbingWithThrowablesTest::shouldLastStubbingVoidBeImportant
    public void shouldLastStubbingVoidBeImportant() throws Exception {
        stubVoid(mock).toThrow(new ExceptionOne()).on().clear();
        stubVoid(mock).toThrow(new ExceptionTwo()).on().clear();
        
        try {
            mock.clear();
            fail();
        } catch (ExceptionTwo e) {}
    }

// org.mockitousage.stubbing.StubbingWithThrowablesTest::shouldFailStubbingThrowableOnTheSameInvocationDueToAcceptableLimitation
    public void shouldFailStubbingThrowableOnTheSameInvocationDueToAcceptableLimitation() throws Exception {
        when(mock.get(1)).thenThrow(new ExceptionOne());
        
        try {
            when(mock.get(1)).thenThrow(new ExceptionTwo());
            fail();
        } catch (ExceptionOne e) {}
    }

// org.mockitousage.stubbing.StubbingWithThrowablesTest::shouldAllowSettingCheckedException
    public void shouldAllowSettingCheckedException() throws Exception {
        Reader reader = mock(Reader.class);
        IOException ioException = new IOException();
        
        when(reader.read()).thenThrow(ioException);
        
        try {
            reader.read();
            fail();
        } catch (Exception e) {
            assertEquals(ioException, e);
        }
    }

// org.mockitousage.stubbing.StubbingWithThrowablesTest::shouldAllowSettingError
    public void shouldAllowSettingError() throws Exception {
        Error error = new Error();
        
        when(mock.add("quake")).thenThrow(error);
        
        try {
            mock.add("quake");
            fail();
        } catch (Error e) {
            assertEquals(error, e);
        }
    }

// org.mockitousage.stubbing.StubbingWithThrowablesTest::shouldNotAllowSettingInvalidCheckedException
    public void shouldNotAllowSettingInvalidCheckedException() throws Exception {
        when(mock.add("monkey island")).thenThrow(new Exception());
    }

// org.mockitousage.stubbing.StubbingWithThrowablesTest::shouldNotAllowSettingNullThrowable
    public void shouldNotAllowSettingNullThrowable() throws Exception {
        when(mock.add("monkey island")).thenThrow(null);
    }

// org.mockitousage.stubbing.StubbingWithThrowablesTest::shouldMixThrowablesAndReturnsOnDifferentMocks
    public void shouldMixThrowablesAndReturnsOnDifferentMocks() throws Exception {
        when(mock.add("ExceptionOne")).thenThrow(new ExceptionOne());
        when(mock.getLast()).thenReturn("last");
        stubVoid(mock).toThrow(new ExceptionTwo()).on().clear();
        
        stubVoid(mockTwo).toThrow(new ExceptionThree()).on().clear();
        when(mockTwo.containsValue("ExceptionFour")).thenThrow(new ExceptionFour());
        when(mockTwo.get("Are you there?")).thenReturn("Yes!");

        assertNull(mockTwo.get("foo"));
        assertTrue(mockTwo.keySet().isEmpty());
        assertEquals("Yes!", mockTwo.get("Are you there?"));
        try {
            mockTwo.clear();
            fail();
        } catch (ExceptionThree e) {}
        try {
            mockTwo.containsValue("ExceptionFour");
            fail();
        } catch (ExceptionFour e) {}
        
        assertNull(mock.getFirst());
        assertEquals("last", mock.getLast());
        try {
            mock.add("ExceptionOne");
            fail();
        } catch (ExceptionOne e) {}
        try {
            mock.clear();
            fail();
        } catch (ExceptionTwo e) {}
    }

// org.mockitousage.stubbing.StubbingWithThrowablesTest::shouldStubbingWithThrowableBeVerifiable
    public void shouldStubbingWithThrowableBeVerifiable() {
        when(mock.size()).thenThrow(new RuntimeException());
        stubVoid(mock).toThrow(new RuntimeException()).on().clone();
        
        try {
            mock.size();
            fail();
        } catch (RuntimeException e) {}
        
        try {
            mock.clone();
            fail();
        } catch (RuntimeException e) {}
        
        verify(mock).size();
        verify(mock).clone();
        verifyNoMoreInteractions(mock);
    }

// org.mockitousage.stubbing.StubbingWithThrowablesTest::shouldStubbingWithThrowableFailVerification
    public void shouldStubbingWithThrowableFailVerification() {
        when(mock.size()).thenThrow(new RuntimeException());
        stubVoid(mock).toThrow(new RuntimeException()).on().clone();
        
        verifyZeroInteractions(mock);
        
        mock.add("test");
        
        try {
            verify(mock).size();
            fail();
        } catch (WantedButNotInvoked e) {}
        
        try {
            verify(mock).clone();
            fail();
        } catch (WantedButNotInvoked e) {}
        
        try {
            verifyNoMoreInteractions(mock);
            fail();
        } catch (NoInteractionsWanted e) {}
    }

// org.mockitousage.verification.AtLeastXVerificationTest::shouldVerifyAtLeastXTimes
    public void shouldVerifyAtLeastXTimes() throws Exception {
        
        mock.clear();
        mock.clear();
        mock.clear();

        
        verify(mock, atLeast(2)).clear();
    }

// org.mockitousage.verification.AtLeastXVerificationTest::shouldFailVerifiationAtLeastXTimes
    public void shouldFailVerifiationAtLeastXTimes() throws Exception {
        mock.add("one");
        verify(mock, atLeast(1)).add(anyString());

        try {
            verify(mock, atLeast(2)).add(anyString());
            fail();
        } catch (MockitoAssertionError e) {}
    }

// org.mockitousage.verification.AtLeastXVerificationTest::shouldAllowAtLeastZeroForTheSakeOfVerifyNoMoreInteractionsSometimes
    public void shouldAllowAtLeastZeroForTheSakeOfVerifyNoMoreInteractionsSometimes() throws Exception {
        
        mock.add("one");
        mock.clear();

        
        verify(mock, atLeast(0)).add("one");
        verify(mock, atLeast(0)).clear();

        verifyNoMoreInteractions(mock);        
    }

// org.mockitousage.verification.AtMostXVerificationTest::shouldVerifyAtMostXTimes
    public void shouldVerifyAtMostXTimes() throws Exception {
        mock.clear();
        mock.clear();
        
        verify(mock, atMost(2)).clear();
        verify(mock, atMost(3)).clear();
        
        try {
            verify(mock, atMost(1)).clear();
            fail();
        } catch (MockitoAssertionError e) {}
    }

// org.mockitousage.verification.AtMostXVerificationTest::shouldWorkWithArgumentMatchers
    public void shouldWorkWithArgumentMatchers() throws Exception {
        mock.add("one");
        verify(mock, atMost(5)).add(anyString());
        
        try {
            verify(mock, atMost(0)).add(anyString());
            fail();
        } catch (MockitoAssertionError e) {}
    }

// org.mockitousage.verification.AtMostXVerificationTest::shouldNotAllowNegativeNumber
    public void shouldNotAllowNegativeNumber() throws Exception {
        try {
            verify(mock, atMost(-1)).clear();
            fail();
        } catch (MockitoException e) {
            assertEquals("Negative value is not allowed here", e.getMessage());
        }
    }

// org.mockitousage.verification.AtMostXVerificationTest::shouldPrintDecentMessage
    public void shouldPrintDecentMessage() throws Exception {
        mock.clear();
        mock.clear();
        
        try {
            verify(mock, atMost(1)).clear();
            fail();
        } catch (MockitoAssertionError e) {
            assertEquals("\nWanted at most 1 time but was 2", e.getMessage());
        }
    }

// org.mockitousage.verification.AtMostXVerificationTest::shouldNotAllowInOrderMode
    public void shouldNotAllowInOrderMode() throws Exception {
        mock.clear();
        InOrder inOrder = inOrder(mock);
        
        try {
            inOrder.verify(mock, atMost(1)).clear();
            fail();
        } catch (MockitoException e) {
            assertEquals("AtMost is not implemented to work with InOrder", e.getMessage());
        }
    }

// org.mockitousage.verification.AtMostXVerificationTest::shouldMarkInteractionsAsVerified
    public void shouldMarkInteractionsAsVerified() throws Exception {
        mock.clear();
        mock.clear();
        
        verify(mock, atMost(3)).clear();
        verifyNoMoreInteractions(mock);
    }

// org.mockitousage.verification.AtMostXVerificationTest::shouldDetectUnverifiedInMarkInteractionsAsVerified
    public void shouldDetectUnverifiedInMarkInteractionsAsVerified() throws Exception {
        mock.clear();
        mock.clear();
        undesiredInteraction();
        
        verify(mock, atMost(3)).clear();
        try {
            verifyNoMoreInteractions(mock);
            fail();
        } catch(NoInteractionsWanted e) {
            assertContains("undesiredInteraction(", e.getMessage());
        }
    }

// org.mockitousage.verification.BasicVerificationInOrderTest::shouldVerifyInOrder
    public void shouldVerifyInOrder() {
        inOrder.verify(mockOne).simpleMethod(1);
        inOrder.verify(mockTwo, times(2)).simpleMethod(2);
        inOrder.verify(mockThree).simpleMethod(3);
        inOrder.verify(mockTwo).simpleMethod(2);
        inOrder.verify(mockOne).simpleMethod(4);
        verifyNoMoreInteractions(mockOne, mockTwo, mockThree);
    }

// org.mockitousage.verification.BasicVerificationInOrderTest::shouldVerifyInOrderUsingAtLeastOnce
    public void shouldVerifyInOrderUsingAtLeastOnce() {
        inOrder.verify(mockOne, atLeastOnce()).simpleMethod(1);
        inOrder.verify(mockTwo, times(2)).simpleMethod(2);
        inOrder.verify(mockThree).simpleMethod(3);
        inOrder.verify(mockTwo).simpleMethod(2);
        inOrder.verify(mockOne, atLeastOnce()).simpleMethod(4);
        verifyNoMoreInteractions(mockOne, mockTwo, mockThree);
    }

// org.mockitousage.verification.BasicVerificationInOrderTest::shouldVerifyInOrderWhenExpectingSomeInvocationsToBeCalledZeroTimes
    public void shouldVerifyInOrderWhenExpectingSomeInvocationsToBeCalledZeroTimes() {
        inOrder.verify(mockOne, times(0)).oneArg(false);
        inOrder.verify(mockOne).simpleMethod(1);
        inOrder.verify(mockTwo, times(2)).simpleMethod(2);
        inOrder.verify(mockTwo, times(0)).simpleMethod(22);
        inOrder.verify(mockThree).simpleMethod(3);
        inOrder.verify(mockTwo).simpleMethod(2);
        inOrder.verify(mockOne).simpleMethod(4);
        inOrder.verify(mockThree, times(0)).oneArg(false);
        verifyNoMoreInteractions(mockOne, mockTwo, mockThree);
    }

// org.mockitousage.verification.BasicVerificationInOrderTest::shouldFailWhenFirstMockCalledTwice
    public void shouldFailWhenFirstMockCalledTwice() {
        inOrder.verify(mockOne).simpleMethod(1);
        try {
            inOrder.verify(mockOne).simpleMethod(1);
            fail();
        } catch (VerificationInOrderFailure e) {}
    }

// org.mockitousage.verification.BasicVerificationInOrderTest::shouldFailWhenLastMockCalledTwice
    public void shouldFailWhenLastMockCalledTwice() {
        inOrder.verify(mockOne).simpleMethod(1);
        inOrder.verify(mockTwo, times(2)).simpleMethod(2);
        inOrder.verify(mockThree).simpleMethod(3);
        inOrder.verify(mockTwo).simpleMethod(2);
        inOrder.verify(mockOne).simpleMethod(4);
        try {
            inOrder.verify(mockOne).simpleMethod(4);
            fail();
        } catch (VerificationInOrderFailure e) {}
    }

// org.mockitousage.verification.BasicVerificationInOrderTest::shouldFailOnFirstMethodBecauseOneInvocationWanted
    public void shouldFailOnFirstMethodBecauseOneInvocationWanted() {
        inOrder.verify(mockOne, times(0)).simpleMethod(1);
    }

// org.mockitousage.verification.BasicVerificationInOrderTest::shouldFailOnFirstMethodBecauseOneInvocationWantedAgain
    public void shouldFailOnFirstMethodBecauseOneInvocationWantedAgain() {
        inOrder.verify(mockOne, times(2)).simpleMethod(1);
    }

// org.mockitousage.verification.BasicVerificationInOrderTest::shouldFailOnSecondMethodBecauseFourInvocationsWanted
    public void shouldFailOnSecondMethodBecauseFourInvocationsWanted() {
        inOrder.verify(mockOne, times(1)).simpleMethod(1);
        try {
            inOrder.verify(mockTwo, times(4)).simpleMethod(2);
            fail();
        } catch (VerificationInOrderFailure e) {}
    }

// org.mockitousage.verification.BasicVerificationInOrderTest::shouldFailOnSecondMethodBecauseTwoInvocationsWantedAgain
    public void shouldFailOnSecondMethodBecauseTwoInvocationsWantedAgain() {
        inOrder.verify(mockOne, times(1)).simpleMethod(1);
        try {
            inOrder.verify(mockTwo, times(0)).simpleMethod(2);
            fail();
        } catch (VerificationInOrderFailure e) {}
    }

// org.mockitousage.verification.BasicVerificationInOrderTest::shouldFailOnLastMethodBecauseOneInvocationWanted
    public void shouldFailOnLastMethodBecauseOneInvocationWanted() {
        inOrder.verify(mockOne, atLeastOnce()).simpleMethod(1);
        inOrder.verify(mockTwo, times(2)).simpleMethod(2);
        inOrder.verify(mockThree, atLeastOnce()).simpleMethod(3);
        inOrder.verify(mockTwo, atLeastOnce()).simpleMethod(2);
        try {
            inOrder.verify(mockOne, times(0)).simpleMethod(4);
            fail();
        } catch (VerificationInOrderFailure e) {}
    }

// org.mockitousage.verification.BasicVerificationInOrderTest::shouldFailOnLastMethodBecauseOneInvocationWantedAgain
    public void shouldFailOnLastMethodBecauseOneInvocationWantedAgain() {
        inOrder.verify(mockOne, atLeastOnce()).simpleMethod(1);
        inOrder.verify(mockTwo, times(2)).simpleMethod(2);
        inOrder.verify(mockThree, atLeastOnce()).simpleMethod(3);
        inOrder.verify(mockTwo, atLeastOnce()).simpleMethod(2);
        try {
            inOrder.verify(mockOne, times(2)).simpleMethod(4);
            fail();
        } catch (VerificationInOrderFailure e) {}
    }

// org.mockitousage.verification.BasicVerificationInOrderTest::shouldFailOnFirstMethodBecauseDifferentArgsWanted
    public void shouldFailOnFirstMethodBecauseDifferentArgsWanted() {
        inOrder.verify(mockOne).simpleMethod(100);
    }

// org.mockitousage.verification.BasicVerificationInOrderTest::shouldFailOnFirstMethodBecauseDifferentMethodWanted
    public void shouldFailOnFirstMethodBecauseDifferentMethodWanted() {
        inOrder.verify(mockOne).oneArg(true);
    }

// org.mockitousage.verification.BasicVerificationInOrderTest::shouldFailOnSecondMethodBecauseDifferentArgsWanted
    public void shouldFailOnSecondMethodBecauseDifferentArgsWanted() {
        inOrder.verify(mockOne).simpleMethod(1);
        try {
            inOrder.verify(mockTwo, times(2)).simpleMethod(-999);
            fail();
        } catch (VerificationInOrderFailure e) {}
    }

// org.mockitousage.verification.BasicVerificationInOrderTest::shouldFailOnSecondMethodBecauseDifferentMethodWanted
    public void shouldFailOnSecondMethodBecauseDifferentMethodWanted() {
        inOrder.verify(mockOne, times(1)).simpleMethod(1);
        try {
            inOrder.verify(mockTwo, times(2)).oneArg(true);
            fail();
        } catch (VerificationInOrderFailure e) {}
    }

// org.mockitousage.verification.BasicVerificationInOrderTest::shouldFailOnLastMethodBecauseDifferentArgsWanted
    public void shouldFailOnLastMethodBecauseDifferentArgsWanted() {
        inOrder.verify(mockOne).simpleMethod(1);
        inOrder.verify(mockTwo, times(2)).simpleMethod(2);
        inOrder.verify(mockThree).simpleMethod(3);
        inOrder.verify(mockTwo).simpleMethod(2);
        try {
            inOrder.verify(mockOne).simpleMethod(-666);
            fail();
        } catch (VerificationInOrderFailure e) {}
    }

// org.mockitousage.verification.BasicVerificationInOrderTest::shouldFailOnLastMethodBecauseDifferentMethodWanted
    public void shouldFailOnLastMethodBecauseDifferentMethodWanted() {
        inOrder.verify(mockOne).simpleMethod(1);
        inOrder.verify(mockTwo, times(2)).simpleMethod(2);
        inOrder.verify(mockThree).simpleMethod(3);
        inOrder.verify(mockTwo).simpleMethod(2);
        try {
            inOrder.verify(mockOne).oneArg(false);
            fail();
        } catch (VerificationInOrderFailure e) {}
    }

// org.mockitousage.verification.BasicVerificationInOrderTest::shouldFailWhenLastMethodVerifiedFirst
    public void shouldFailWhenLastMethodVerifiedFirst() {
        inOrder.verify(mockOne).simpleMethod(4);
        try {
            inOrder.verify(mockOne).simpleMethod(1);
            fail();
        } catch (VerificationInOrderFailure e) {}
    }

// org.mockitousage.verification.BasicVerificationInOrderTest::shouldFailWhenMiddleMethodVerifiedFirst
    public void shouldFailWhenMiddleMethodVerifiedFirst() {
        inOrder.verify(mockTwo, times(2)).simpleMethod(2);
        try {
            inOrder.verify(mockOne).simpleMethod(1);
            fail();
        } catch (VerificationInOrderFailure e) {}
    }

// org.mockitousage.verification.BasicVerificationInOrderTest::shouldFailWhenMiddleMethodVerifiedFirstInAtLeastOnceMode
    public void shouldFailWhenMiddleMethodVerifiedFirstInAtLeastOnceMode() {
        inOrder.verify(mockTwo, atLeastOnce()).simpleMethod(2);
        try {
            inOrder.verify(mockOne).simpleMethod(1);
            fail();
        } catch (VerificationInOrderFailure e) {}
    }

// org.mockitousage.verification.BasicVerificationInOrderTest::shouldFailOnVerifyNoMoreInteractions
    public void shouldFailOnVerifyNoMoreInteractions() {
        inOrder.verify(mockOne).simpleMethod(1);
        inOrder.verify(mockTwo, times(2)).simpleMethod(2);
        inOrder.verify(mockThree).simpleMethod(3);
        inOrder.verify(mockTwo).simpleMethod(2);
        
        try {
            verifyNoMoreInteractions(mockOne, mockTwo, mockThree);
            fail();
        } catch (NoInteractionsWanted e) {}
    }

// org.mockitousage.verification.BasicVerificationInOrderTest::shouldFailOnVerifyZeroInteractions
    public void shouldFailOnVerifyZeroInteractions() {
        verifyZeroInteractions(mockOne);
    }

// org.mockitousage.verification.BasicVerificationInOrderTest::shouldScreamWhenNullPassed
    public void shouldScreamWhenNullPassed() {
        inOrder(null);
    }

// org.mockitousage.verification.BasicVerificationTest::shouldVerify
    public void shouldVerify() throws Exception {
        mock.clear();
        verify(mock).clear();

        mock.add("test");
        verify(mock).add("test");

        verifyNoMoreInteractions(mock);
    }

// org.mockitousage.verification.BasicVerificationTest::shouldFailVerification
    public void shouldFailVerification() throws Exception {
        verify(mock).clear();
    }

// org.mockitousage.verification.BasicVerificationTest::shouldFailVerificationOnMethodArgument
    public void shouldFailVerificationOnMethodArgument() throws Exception {
        mock.clear();
        mock.add("foo");

        verify(mock).clear();
        try {
            verify(mock).add("bar");
            fail();
        } catch (AssertionError expected) {}
    }

// org.mockitousage.verification.BasicVerificationTest::shouldFailOnWrongMethod
    public void shouldFailOnWrongMethod() throws Exception {
        mock.clear();
        mock.clear();
        
        mockTwo.add("add");

        verify(mock, atLeastOnce()).clear();
        verify(mockTwo, atLeastOnce()).add("add");
        try {
            verify(mockTwo, atLeastOnce()).add("foo");
            fail();
        } catch (WantedButNotInvoked e) {}
    }

// org.mockitousage.verification.BasicVerificationTest::shouldDetectRedundantInvocation
    public void shouldDetectRedundantInvocation() throws Exception {
        mock.clear();
        mock.add("foo");
        mock.add("bar");

        verify(mock).clear();
        verify(mock).add("foo");

        try {
            verifyNoMoreInteractions(mock);
            fail();
        } catch (NoInteractionsWanted e) {}
    }

// org.mockitousage.verification.BasicVerificationTest::shouldDetectWhenInvokedMoreThanOnce
    public void shouldDetectWhenInvokedMoreThanOnce() throws Exception {
        mock.add("foo");
        mock.clear();
        mock.clear();
        
        verify(mock).add("foo");

        try {
            verify(mock).clear();
            fail();
        } catch (TooManyActualInvocations e) {}
    }

// org.mockitousage.verification.BasicVerificationTest::shouldVerifyStubbedMethods
    public void shouldVerifyStubbedMethods() throws Exception {
        when(mock.add("test")).thenReturn(Boolean.FALSE);
        
        mock.add("test");
        
        verify(mock).add("test");
    }

// org.mockitousage.verification.BasicVerificationTest::shouldDetectWhenOverloadedMethodCalled
    public void shouldDetectWhenOverloadedMethodCalled() throws Exception {
        IMethods mockThree = mock(IMethods.class);
        
        mockThree.varargs((Object[]) new Object[] {});
        try {
            verify(mockThree).varargs((String[]) new String[] {});
            fail();
        } catch(WantedButNotInvoked e) {}
    }

// org.mockitousage.verification.DescriptiveMessagesOnVerificationInOrderErrorsTest::shouldPrintVerificationInOrderErrorAndShowBothWantedAndPrevious
    public void shouldPrintVerificationInOrderErrorAndShowBothWantedAndPrevious() {
        inOrder.verify(one).simpleMethod(1);
        inOrder.verify(two, atLeastOnce()).simpleMethod(2);
        
        try {
            inOrder.verify(one, atLeastOnce()).simpleMethod(11);
            fail();
        } catch (VerificationInOrderFailure e) {
            String expected = 
                    "\n" +
                    "Verification in order failure" +
                    "\n" +
                    "Wanted but not invoked:" +
                    "\n" +
                    "iMethods.simpleMethod(11);" +
                    "\n" +
                    "-> at "; 
            
            assertContains(expected, e.getMessage());
            
            String expectedCause = 
                "\n" +
                "Wanted anywhere AFTER following interaction:" +
                "\n" +
                "iMethods.simpleMethod(2);" +
                "\n" +
                "-> at ";
            
            assertContains(expectedCause, e.getMessage());
        }
    }

// org.mockitousage.verification.DescriptiveMessagesOnVerificationInOrderErrorsTest::shouldPrintVerificationInOrderErrorAndShowWantedOnly
    public void shouldPrintVerificationInOrderErrorAndShowWantedOnly() {
        try {
            inOrder.verify(one).differentMethod();
            fail();
        } catch (WantedButNotInvoked e) {
            String expected = 
                    "\n" +
                    "Wanted but not invoked:" +
                    "\n" +
                    "iMethods.differentMethod();" +
                    "\n" +
                    "-> at"; 
            
            assertContains(expected, e.getMessage());
        }
    }

// org.mockitousage.verification.DescriptiveMessagesOnVerificationInOrderErrorsTest::shouldPrintVerificationInOrderErrorAndShowWantedAndActual
    public void shouldPrintVerificationInOrderErrorAndShowWantedAndActual() {
        try {
            inOrder.verify(one).simpleMethod(999);
            fail();
        } catch (ArgumentsAreDifferent e) {
            String expected = 
                    "\n" +
                    "Arguments are different!" +
                    "\n" +
                    "IMethods.simpleMethod(999);"; 
            
            assertEquals(expected, e.getMessage());
            
            assertEquals(null, e.getCause());
        }
    }

// org.mockitousage.verification.DescriptiveMessagesOnVerificationInOrderErrorsTest::shouldPrintMethodThatWasNotInvoked
    public void shouldPrintMethodThatWasNotInvoked() {
        inOrder.verify(one).simpleMethod(1);
        inOrder.verify(one).simpleMethod(11);
        inOrder.verify(two, times(2)).simpleMethod(2);
        inOrder.verify(three).simpleMethod(3);
        try {
            inOrder.verify(three).simpleMethod(999);
            fail();
        } catch (VerificationInOrderFailure e) {
            String actualMessage = e.getMessage();
            String expectedMessage = 
                    "\n" +
                    "Verification in order failure" +
                    "\n" +
                    "Wanted but not invoked:" +
                    "\n" +
                    "iMethods.simpleMethod(999);"; 
            assertContains(expectedMessage, actualMessage);     
        }
    }

// org.mockitousage.verification.DescriptiveMessagesOnVerificationInOrderErrorsTest::shouldPrintTooManyInvocations
    public void shouldPrintTooManyInvocations() {
        inOrder.verify(one).simpleMethod(1);
        inOrder.verify(one).simpleMethod(11);
        try {
            inOrder.verify(two, times(1)).simpleMethod(2);
            fail();
        } catch (VerificationInOrderFailure e) {
            String actualMessage = e.getMessage();
            String expectedMessage = 
                    "\n" +
                    "Verification in order failure:" +
                    "\n" +
                    "iMethods.simpleMethod(2);" +
                    "\n" +
                    "Wanted 1 time:" +
                    "\n" +
                    "-> at"; 
            assertContains(expectedMessage, actualMessage);      

            String expectedCause =
                "\n" +
                "But was 2 times. Undesired invocation:" +
                "\n" +
                "-> at";
            assertContains(expectedCause, e.getMessage());
        }
    }

// org.mockitousage.verification.DescriptiveMessagesOnVerificationInOrderErrorsTest::shouldPrintTooLittleInvocations
    public void shouldPrintTooLittleInvocations() {
        two.simpleMethod(2);
        
        inOrder.verify(one, atLeastOnce()).simpleMethod(anyInt());
        inOrder.verify(two, times(2)).simpleMethod(2);
        inOrder.verify(three, atLeastOnce()).simpleMethod(3);
        
        try {
            inOrder.verify(two, times(2)).simpleMethod(2);
            fail();
        } catch (VerificationInOrderFailure e) {
            String actualMessage = e.getMessage();
            String expectedMessage = 
                    "\n" +
                    "Verification in order failure:" +
                    "\n" +
                    "iMethods.simpleMethod(2);" +
                    "\n" +
                    "Wanted 2 times:" +
                    "\n" +
                    "-> at";
            assertContains(expectedMessage, actualMessage);
            
            String expectedCause = 
                "\n" +
                "But was 1 time:" +
                "\n" +
                "-> at";
            
            assertContains(expectedCause, e.getMessage());
        }
    }

// org.mockitousage.verification.DescriptiveMessagesWhenTimesXVerificationFailsTest::shouldVerifyActualNumberOfInvocationsSmallerThanWanted
    public void shouldVerifyActualNumberOfInvocationsSmallerThanWanted() throws Exception {
        mock.clear();
        mock.clear();
        mock.clear();

        Mockito.verify(mock, times(3)).clear();
        try {
            Mockito.verify(mock, times(100)).clear();
            fail();
        } catch (TooLittleActualInvocations e) {
            assertContains("mock.clear();", e.getMessage());
            assertContains("Wanted 100 times", e.getMessage());
            assertContains("was 3", e.getMessage());
        }
    }

// org.mockitousage.verification.DescriptiveMessagesWhenTimesXVerificationFailsTest::shouldVerifyActualNumberOfInvocationsLargerThanWanted
    public void shouldVerifyActualNumberOfInvocationsLargerThanWanted() throws Exception {
        mock.clear();
        mock.clear();
        mock.clear();
        mock.clear();

        Mockito.verify(mock, times(4)).clear();
        try {
            Mockito.verify(mock, times(1)).clear();
            fail();
        } catch (TooManyActualInvocations e) {
            assertContains("mock.clear();", e.getMessage());
            assertContains("Wanted 1 time", e.getMessage());
            assertContains("was 4", e.getMessage());
        }
    }

// org.mockitousage.verification.DescriptiveMessagesWhenVerificationFailsTest::shouldPrintMethodName
    public void shouldPrintMethodName() {
        try {
            verify(mock).simpleMethod();
            fail();
        } catch (WantedButNotInvoked e) {
            String actualMessage = e.getMessage();
            String expectedMessage =
                    "\n" +
                    "Wanted but not invoked:" +
                    "\n" +
                    "iMethods.simpleMethod();" +
                    "\n" +
                    "-> at";
            assertContains(expectedMessage, actualMessage);
        }
    }

// org.mockitousage.verification.DescriptiveMessagesWhenVerificationFailsTest::shouldPrintMethodNameAndArguments
    public void shouldPrintMethodNameAndArguments() {
        try {
            verify(mock).threeArgumentMethod(12, new Foo(), "xx");
            fail();
        } catch (WantedButNotInvoked e) {
            assertContains("iMethods.threeArgumentMethod(12, foo, \"xx\")", e.getMessage());
        }
    }

// org.mockitousage.verification.DescriptiveMessagesWhenVerificationFailsTest::shouldPrintActualAndWantedInLine
    public void shouldPrintActualAndWantedInLine() {
        mock.varargs(1, 2);

        try {
            verify(mock).varargs(1, 1000);
            fail();
        } catch (ArgumentsAreDifferent e) {
            String wanted =
                    "\n" +
                    "Argument(s) are different! Wanted:" +
                    "\n" +
                    "iMethods.varargs(1, 1000);";

            assertContains(wanted, e.getMessage());
            
            String actual = 
                    "\n" +
                    "Actual invocation has different arguments:" +
                    "\n" +
                    "iMethods.varargs(1, 2);";

            assertContains(actual, e.getMessage());
        }
    }

// org.mockitousage.verification.DescriptiveMessagesWhenVerificationFailsTest::shouldPrintActualAndWantedInMultipleLines
    public void shouldPrintActualAndWantedInMultipleLines() {
        mock.varargs("this is very long string", "this is another very long string");

        try {
            verify(mock).varargs("x", "y", "z");
            fail();
        } catch (ArgumentsAreDifferent e) {
            String wanted =
                    "\n" +
                    "Argument(s) are different! Wanted:" +
                    "\n" +
                    "iMethods.varargs(" +
                    "\n" +
                    "    \"x\"," +
                    "\n" +
                    "    \"y\"," +
                    "\n" +
                    "    \"z\"" +
                    "\n" +
                    ");";

            assertContains(wanted, e.getMessage());

            String actual =
                    "\n" +
                    "Actual invocation has different arguments:" +
                    "\n" +
                    "iMethods.varargs(" +
                    "\n" +
                    "    \"this is very long string\"," +
                    "\n" +
                    "    \"this is another very long string\"" +
                    "\n" +
                    ");";

            assertContains(actual, e.getMessage());
        }
    }

// org.mockitousage.verification.DescriptiveMessagesWhenVerificationFailsTest::shouldPrintActualAndWantedWhenActualMethodNameAndWantedMethodNameAreTheSame
    public void shouldPrintActualAndWantedWhenActualMethodNameAndWantedMethodNameAreTheSame() {
        mock.simpleMethod();

        try {
            verify(mock).simpleMethod(10);
            fail();
        } catch (ArgumentsAreDifferent e) {
            assertContains("simpleMethod(10)", e.getMessage());
            assertContains("simpleMethod()", e.getMessage());
        }
    }

// org.mockitousage.verification.DescriptiveMessagesWhenVerificationFailsTest::shouldPrintActualAndUnverifiedWantedWhenTheDifferenceIsAboutArguments
    public void shouldPrintActualAndUnverifiedWantedWhenTheDifferenceIsAboutArguments() {
        mock.twoArgumentMethod(1, 1);
        mock.twoArgumentMethod(2, 2);

        verify(mock).twoArgumentMethod(1, 1);
        try {
            verify(mock).twoArgumentMethod(2, 1000);
            fail();
        } catch (ArgumentsAreDifferent e) {
            assertContains("(2, 1000)", e.getMessage());
            assertContains("(2, 2)", e.getMessage());
        }
    }

// org.mockitousage.verification.DescriptiveMessagesWhenVerificationFailsTest::shouldPrintFirstUnexpectedInvocation
    public void shouldPrintFirstUnexpectedInvocation() {
        mock.oneArg(true);
        mock.oneArg(false);
        mock.threeArgumentMethod(1, "2", "3");

        verify(mock).oneArg(true);
        try {
            verifyNoMoreInteractions(mock);
            fail();
        } catch (NoInteractionsWanted e) {
            String expectedMessage =
                    "\n" +
                    "No interactions wanted here:" +
                    "\n" +
                    "-> at";
            assertContains(expectedMessage, e.getMessage());

            String expectedCause =
                    "\n" +
                    "But found this interaction:" +
                    "\n" +
                    "-> at";
            assertContains(expectedCause, e.getMessage());
        }
    }

// org.mockitousage.verification.DescriptiveMessagesWhenVerificationFailsTest::shouldPrintFirstUnexpectedInvocationWhenVerifyingZeroInteractions
    public void shouldPrintFirstUnexpectedInvocationWhenVerifyingZeroInteractions() {
        mock.twoArgumentMethod(1, 2);
        mock.threeArgumentMethod(1, "2", "3");

        try {
            verifyZeroInteractions(mock);
            fail();
        } catch (NoInteractionsWanted e) {
            String expected =
                    "\n" +
                    "No interactions wanted here:" +
                    "\n" +
                    "-> at";

            assertContains(expected, e.getMessage());

            String expectedCause =
                "\n" +
                "But found this interaction:" +
                "\n" +
                "-> at";

            assertContains(expectedCause, e.getMessage());
        }
    }

// org.mockitousage.verification.DescriptiveMessagesWhenVerificationFailsTest::shouldPrintMethodNameWhenVerifyingAtLeastOnce
    public void shouldPrintMethodNameWhenVerifyingAtLeastOnce() throws Exception {
        try {
            verify(mock, atLeastOnce()).twoArgumentMethod(1, 2);
            fail();
        } catch (WantedButNotInvoked e) {
            assertContains("twoArgumentMethod(1, 2)", e.getMessage());
        }
    }

// org.mockitousage.verification.DescriptiveMessagesWhenVerificationFailsTest::shouldPrintMethodWhenMatcherUsed
    public void shouldPrintMethodWhenMatcherUsed() throws Exception {
        try {
            verify(mock, atLeastOnce()).twoArgumentMethod(anyInt(), eq(100));
            fail();
        } catch (WantedButNotInvoked e) {
            String actualMessage = e.getMessage();
            String expectedMessage =
                "\n" +
                "Wanted but not invoked:" +
                "\n" +
                "iMethods.twoArgumentMethod(<any>, 100);";
            assertContains(expectedMessage, actualMessage);
        }
    }

// org.mockitousage.verification.DescriptiveMessagesWhenVerificationFailsTest::shouldPrintMethodWhenMissingInvocationWithArrayMatcher
    public void shouldPrintMethodWhenMissingInvocationWithArrayMatcher() {
        mock.oneArray(new boolean[] { true, false, false });

        try {
            verify(mock).oneArray(aryEq(new boolean[] { false, false, false }));
            fail();
        } catch (ArgumentsAreDifferent e) {
            assertContains("[false, false, false]", e.getMessage());
            assertContains("[true, false, false]", e.getMessage());
        }
    }

// org.mockitousage.verification.DescriptiveMessagesWhenVerificationFailsTest::shouldPrintMethodWhenMissingInvocationWithVarargMatcher
    public void shouldPrintMethodWhenMissingInvocationWithVarargMatcher() {
        mock.varargsString(10, "xxx", "yyy", "zzz");

        try {
            verify(mock).varargsString(10, "111", "222", "333");
            fail();
        } catch (ArgumentsAreDifferent e) {
            assertContains("111", e.getMessage());
            assertContains("\"xxx\"", e.getMessage());
        }
    }

// org.mockitousage.verification.DescriptiveMessagesWhenVerificationFailsTest::shouldPrintMethodWhenMissingInvocationWithMatcher
    public void shouldPrintMethodWhenMissingInvocationWithMatcher() {
        mock.simpleMethod("foo");

        try {
            verify(mock).simpleMethod(matches("burrito from Exmouth"));
            fail();
        } catch (ArgumentsAreDifferent e) {
            assertContains("matches(\"burrito from Exmouth\")", e.getMessage());
            assertContains("\"foo\"", e.getMessage());
        }
    }

// org.mockitousage.verification.DescriptiveMessagesWhenVerificationFailsTest::shouldPrintNullArguments
    public void shouldPrintNullArguments() throws Exception {
        mock.simpleMethod(null, (Integer) null);
        try {
            verify(mock).simpleMethod("test");
            fail();
        } catch (ArgumentsAreDifferent e) {
            assertContains("simpleMethod(null, null);", e.getMessage());
        }
    }

// org.mockitousage.verification.DescriptiveMessagesWhenVerificationFailsTest::shouldSayNeverWantedButInvoked
    public void shouldSayNeverWantedButInvoked() throws Exception {
        mock.simpleMethod(1);
    
        verify(mock, never()).simpleMethod(2);
        try {
            verify(mock, never()).simpleMethod(1);
            fail();
        } catch (NeverWantedButInvoked e) {
            assertContains("Never wanted here:", e.getMessage());
            assertContains("But invoked here:", e.getMessage());
        }
    }

// org.mockitousage.verification.DescriptiveMessagesWhenVerificationFailsTest::shouldShowRightActualMethod
    public void shouldShowRightActualMethod() throws Exception {
        mock.simpleMethod(9191);
        mock.simpleMethod("foo");
    
        try {
            verify(mock).simpleMethod("bar");
            fail();
        } catch (ArgumentsAreDifferent e) {
            assertContains("bar", e.getMessage());
            assertContains("foo", e.getMessage());
        }
    }

// org.mockitousage.verification.DescriptiveMessagesWhenVerificationFailsTest::shouldPrintFieldNameWhenAnnotationsUsed
    public void shouldPrintFieldNameWhenAnnotationsUsed() throws Exception {
        iHavefunkyName.simpleMethod(10);
    
        try {
            verify(iHavefunkyName).simpleMethod(20);
            fail();
        } catch (ArgumentsAreDifferent e) {
            assertContains("iHavefunkyName.simpleMethod(20)", e.getMessage());
            assertContains("iHavefunkyName.simpleMethod(10)", e.getMessage());
        }
    }

// org.mockitousage.verification.DescriptiveMessagesWhenVerificationFailsTest::shouldPrintInteractionsOnMockWhenOrdinaryVerificationFail
    public void shouldPrintInteractionsOnMockWhenOrdinaryVerificationFail() throws Exception {
        mock.otherMethod();
        mock.booleanReturningMethod();
        
        try {
            verify(mock).simpleMethod();
            fail();
        } catch (WantedButNotInvoked e) {

        }
    }

// org.mockitousage.verification.DescriptiveMessagesWhenVerificationFailsTest::shouldNeverBreakMethodStringWhenNoArgsInMethod
    public void shouldNeverBreakMethodStringWhenNoArgsInMethod() throws Exception {
        try {
            verify(veeeeeeeeeeeeeeeeeeeeeeeerylongNameMock).simpleMethod();
            fail();
        } catch(WantedButNotInvoked e) {
            assertContains("veeeeeeeeeeeeeeeeeeeeeeeerylongNameMock.simpleMethod()", e.getMessage());
        }
    }

// org.mockitousage.verification.ExactNumberOfTimesVerificationTest::shouldDetectTooLittleActualInvocations
    public void shouldDetectTooLittleActualInvocations() throws Exception {
        mock.clear();
        mock.clear();

        verify(mock, times(2)).clear();
        try {
            verify(mock, times(100)).clear();
            fail();
        } catch (TooLittleActualInvocations e) {
            assertContains("Wanted 100 times", e.getMessage());
            assertContains("was 2", e.getMessage());
        }
    }

// org.mockitousage.verification.ExactNumberOfTimesVerificationTest::shouldDetectTooManyActualInvocations
    public void shouldDetectTooManyActualInvocations() throws Exception {
        mock.clear();
        mock.clear();

        verify(mock, times(2)).clear();
        try {
            verify(mock, times(1)).clear();
            fail();
        } catch (TooManyActualInvocations e) {
            assertContains("Wanted 1 time", e.getMessage());
            assertContains("was 2 times", e.getMessage());
        }
    }

// org.mockitousage.verification.ExactNumberOfTimesVerificationTest::shouldDetectActualInvocationsCountIsMoreThanZero
    public void shouldDetectActualInvocationsCountIsMoreThanZero() throws Exception {
        verify(mock, times(0)).clear();
        try {
            verify(mock, times(15)).clear();
            fail();
        } catch (WantedButNotInvoked e) {}
    }

// org.mockitousage.verification.ExactNumberOfTimesVerificationTest::shouldDetectActuallyCalledOnce
    public void shouldDetectActuallyCalledOnce() throws Exception {
        mock.clear();

        try {
            verify(mock, times(0)).clear();
            fail();
        } catch (NeverWantedButInvoked e) {
            assertContains("Never wanted here", e.getMessage());
        }
    }

// org.mockitousage.verification.ExactNumberOfTimesVerificationTest::shouldPassWhenMethodsActuallyNotCalled
    public void shouldPassWhenMethodsActuallyNotCalled() throws Exception {
        verify(mock, times(0)).clear();
        verify(mock, times(0)).add("yes, I wasn't called");
    }

// org.mockitousage.verification.ExactNumberOfTimesVerificationTest::shouldNotCountInStubbedInvocations
    public void shouldNotCountInStubbedInvocations() throws Exception {
        when(mock.add("test")).thenReturn(false);
        when(mock.add("test")).thenReturn(true);

        mock.add("test");
        mock.add("test");

        verify(mock, times(2)).add("test");
    }

// org.mockitousage.verification.ExactNumberOfTimesVerificationTest::shouldAllowVerifyingInteractionNeverHappened
    public void shouldAllowVerifyingInteractionNeverHappened() throws Exception {
        mock.add("one");

        verify(mock, never()).add("two");
        verify(mock, never()).clear();
        
        try {
            verify(mock, never()).add("one");
            fail();
        } catch (NeverWantedButInvoked e) {}
    }

// org.mockitousage.verification.ExactNumberOfTimesVerificationTest::shouldAllowVerifyingInteractionNeverHappenedInOrder
    public void shouldAllowVerifyingInteractionNeverHappenedInOrder() throws Exception {
        mock.add("one");
        mock.add("two");

        InOrder inOrder = inOrder(mock);
        
        inOrder.verify(mock, never()).add("xxx");
        inOrder.verify(mock).add("one");
        inOrder.verify(mock, never()).add("one");
        
        try {
            inOrder.verify(mock, never()).add("two");
            fail();
        } catch (VerificationInOrderFailure e) {}
    }

// org.mockitousage.verification.NoMoreInteractionsVerificationTest::shouldStubbingNotRegisterRedundantInteractions
    public void shouldStubbingNotRegisterRedundantInteractions() throws Exception {
        when(mock.add("one")).thenReturn(true);
        when(mock.add("two")).thenReturn(true);

        mock.add("one");
        
        verify(mock).add("one");
        verifyNoMoreInteractions(mock);
    }

// org.mockitousage.verification.NoMoreInteractionsVerificationTest::shouldVerifyWhenWantedNumberOfInvocationsUsed
    public void shouldVerifyWhenWantedNumberOfInvocationsUsed() throws Exception {
        mock.add("one");
        mock.add("one");
        mock.add("one");
        
        verify(mock, times(3)).add("one");
        
        verifyNoMoreInteractions(mock);
    }

// org.mockitousage.verification.NoMoreInteractionsVerificationTest::shouldVerifyNoInteractionsAsManyTimesAsYouWant
    public void shouldVerifyNoInteractionsAsManyTimesAsYouWant() throws Exception {
        verifyNoMoreInteractions(mock);
        verifyNoMoreInteractions(mock);
        
        verifyZeroInteractions(mock);
        verifyZeroInteractions(mock);
    }

// org.mockitousage.verification.NoMoreInteractionsVerificationTest::shouldFailZeroInteractionsVerification
    public void shouldFailZeroInteractionsVerification() throws Exception {
        mock.clear();
        
        try {
            verifyZeroInteractions(mock);
            fail();
        } catch (NoInteractionsWanted e) {}
    }

// org.mockitousage.verification.NoMoreInteractionsVerificationTest::shouldFailNoMoreInteractionsVerification
    public void shouldFailNoMoreInteractionsVerification() throws Exception {
        mock.clear();
        
        try {
            verifyNoMoreInteractions(mock);
            fail();
        } catch (NoInteractionsWanted e) {}
    }

// org.mockitousage.verification.NoMoreInteractionsVerificationTest::shouldVerifyOneMockButFailOnOther
    public void shouldVerifyOneMockButFailOnOther() throws Exception {
        List list = mock(List.class);
        Map map = mock(Map.class);

        list.add("one");
        list.add("one");
        
        map.put("one", 1);
        
        verify(list, times(2)).add("one");
        
        verifyNoMoreInteractions(list);
        try {
            verifyZeroInteractions(map);
            fail();
        } catch (NoInteractionsWanted e) {}
    }

// org.mockitousage.verification.NoMoreInteractionsVerificationTest::verifyNoMoreInteractionsShouldScreamWhenNullPassed
    public void verifyNoMoreInteractionsShouldScreamWhenNullPassed() throws Exception {
        verifyNoMoreInteractions(null);
    }

// org.mockitousage.verification.OnlyVerificationTest::shouldVerifyMethodWasInvokedExclusively
	public void shouldVerifyMethodWasInvokedExclusively() {
		mock.clear();
		verify(mock, only()).clear();
	}

// org.mockitousage.verification.OnlyVerificationTest::shouldVerifyMethodWasInvokedExclusivelyWithMatchersUsage
	public void shouldVerifyMethodWasInvokedExclusivelyWithMatchersUsage() {
		mock.get(0);
		verify(mock, only()).get(anyInt());
	}

// org.mockitousage.verification.OnlyVerificationTest::shouldFailIfMethodWasNotInvoked
	public void shouldFailIfMethodWasNotInvoked() {
		mock.clear();
		try {
			verify(mock, only()).get(0);
			fail();
		} catch (WantedButNotInvoked e) {}
	}

// org.mockitousage.verification.OnlyVerificationTest::shouldFailIfMethodWasInvokedMoreThanOnce
	public void shouldFailIfMethodWasInvokedMoreThanOnce() {
		mock.clear();
		mock.clear();
		try {
			verify(mock, only()).clear();
			fail();
		} catch (NoInteractionsWanted e) {}
	}

// org.mockitousage.verification.OnlyVerificationTest::shouldFailIfMethodWasInvokedButWithDifferentArguments
	public void shouldFailIfMethodWasInvokedButWithDifferentArguments() {
		mock.get(0);
		mock.get(2);
		try {
			verify(mock, only()).get(1);
			fail();
		} catch (WantedButNotInvoked e) {}
	}

// org.mockitousage.verification.OnlyVerificationTest::shouldVerifyMethodWasInvokedExclusivelyWhenTwoMocksInUse
	public void shouldVerifyMethodWasInvokedExclusivelyWhenTwoMocksInUse() {
		mock.clear();
		mock2.get(0);
		verify(mock, only()).clear();
		verify(mock2, only()).get(0);
	}

// org.mockitousage.verification.OrdinaryVerificationPrintsAllInteractionsTest::shouldShowAllInteractionsOnMockWhenOrdinaryVerificationFail
    public void shouldShowAllInteractionsOnMockWhenOrdinaryVerificationFail() throws Exception {
        firstInteraction();
        secondInteraction();
        
        try {
            verify(mock).simpleMethod();
            fail();
        } catch (WantedButNotInvoked e) {
            assertContains("However, there were other interactions with this mock", e.getMessage());
            assertContains("firstInteraction(", e.getMessage());
            assertContains("secondInteraction(", e.getMessage());
        }
    }

// org.mockitousage.verification.OrdinaryVerificationPrintsAllInteractionsTest::shouldNotShowAllInteractionsOnDifferentMock
    public void shouldNotShowAllInteractionsOnDifferentMock() throws Exception {
        differentMockInteraction();
        firstInteraction();
        
        try {
            verify(mock).simpleMethod();
            fail();
        } catch (WantedButNotInvoked e) {
            assertContains("firstInteraction(", e.getMessage());
            assertNotContains("differentMockInteraction(", e.getMessage());
        }
    }

// org.mockitousage.verification.OrdinaryVerificationPrintsAllInteractionsTest::shouldNotShowAllInteractionsHeaderWhenNoOtherInteractions
    public void shouldNotShowAllInteractionsHeaderWhenNoOtherInteractions() throws Exception {
        try {
            verify(mock).simpleMethod();
            fail();
        } catch (WantedButNotInvoked e) {
            assertContains("there were zero interactions with this mock.", e.getMessage());
        }
    }

// org.mockitousage.verification.PrintingVerboseTypesWithArgumentsTest::shouldNotReportArgumentTypesWhenToStringIsTheSame
    public void shouldNotReportArgumentTypesWhenToStringIsTheSame() throws Exception {
        
        Boo boo = mock(Boo.class);
        boo.withLong(100);
        
        try {
            
            verify(boo).withLong(eq(100));
            fail();
        } catch (ArgumentsAreDifferent e) {
            
            assertContains("withLong((Integer) 100);", e.getMessage());
            assertContains("withLong((Long) 100);", e.getMessage());
        }
    }

// org.mockitousage.verification.PrintingVerboseTypesWithArgumentsTest::shouldShowTheTypeOfOnlyTheArgumentThatDoesntMatch
    public void shouldShowTheTypeOfOnlyTheArgumentThatDoesntMatch() throws Exception {
        
        Boo boo = mock(Boo.class);
        boo.withLongAndInt(100, 200);
        
        try {
            
            verify(boo).withLongAndInt(eq(100), eq(200));
            fail();
        } catch (ArgumentsAreDifferent e) {
            
            assertContains("withLongAndInt((Integer) 100, 200)", e.getMessage());
            assertContains("withLongAndInt((Long) 100, 200)", e.getMessage());
        }
    }

// org.mockitousage.verification.PrintingVerboseTypesWithArgumentsTest::shouldShowTheTypeOfTheMismatchingArgumentWhenOutputDescriptionsForInvocationsAreDifferent
    public void shouldShowTheTypeOfTheMismatchingArgumentWhenOutputDescriptionsForInvocationsAreDifferent() throws Exception {
        
        Boo boo = mock(Boo.class);
        boo.withLongAndInt(100, 200);
        
        try {
            
            verify(boo).withLongAndInt(eq(100), anyInt());
            fail();
        } catch (ArgumentsAreDifferent e) {
            
            assertContains("withLongAndInt((Long) 100, 200)", e.getMessage());
            assertContains("withLongAndInt((Integer) 100, <any>)", e.getMessage());
        }
    }

// org.mockitousage.verification.PrintingVerboseTypesWithArgumentsTest::shouldNotShowTypesWhenArgumentValueIsDifferent
    public void shouldNotShowTypesWhenArgumentValueIsDifferent() throws Exception {
        
        Boo boo = mock(Boo.class);
        boo.withLongAndInt(100, 200);
        
        try {
            
            verify(boo).withLongAndInt(eq(100L), eq(230));
            fail();
        } catch (ArgumentsAreDifferent e) {
            
            assertContains("withLongAndInt(100, 200)", e.getMessage());
            assertContains("withLongAndInt(100, 230)", e.getMessage());
        }
    }

// org.mockitousage.verification.PrintingVerboseTypesWithArgumentsTest::shouldNotShowTypesWhenTypesAreTheSameEvenIfToStringGivesTheSameResult
    public void shouldNotShowTypesWhenTypesAreTheSameEvenIfToStringGivesTheSameResult() throws Exception {
        
        IMethods mock = mock(IMethods.class);
        mock.simpleMethod(new Foo(10));
        
        try {
            
            verify(mock).simpleMethod(new Foo(20));
            fail();
        } catch (ArgumentsAreDifferent e) {
            
            assertContains("simpleMethod(foo)", e.getMessage());
        }
    }

// org.mockitousage.verification.RelaxedVerificationInOrderTest::shouldVerifyInOrderAllInvocations
    public void shouldVerifyInOrderAllInvocations() {
        inOrder.verify(mockOne).simpleMethod(1);
        inOrder.verify(mockTwo, times(2)).simpleMethod(2);
        inOrder.verify(mockThree).simpleMethod(3);
        inOrder.verify(mockTwo).simpleMethod(2);
        inOrder.verify(mockOne).simpleMethod(4);
        verifyNoMoreInteractions(mockOne, mockTwo, mockThree);
    }

// org.mockitousage.verification.RelaxedVerificationInOrderTest::shouldVerifyInOrderAndBeRelaxed
    public void shouldVerifyInOrderAndBeRelaxed() {
        inOrder.verify(mockTwo, times(2)).simpleMethod(2);
        inOrder.verify(mockThree).simpleMethod(3);
        
        verifyNoMoreInteractions(mockThree);
    }

// org.mockitousage.verification.RelaxedVerificationInOrderTest::shouldAllowFirstChunkBeforeLastInvocation
    public void shouldAllowFirstChunkBeforeLastInvocation() {
        inOrder.verify(mockTwo, times(2)).simpleMethod(2);
        inOrder.verify(mockOne).simpleMethod(4);
        
        try {
            verifyNoMoreInteractions(mockTwo);
            fail();
        } catch (NoInteractionsWanted e) {}
    }

// org.mockitousage.verification.RelaxedVerificationInOrderTest::shouldAllowAllChunksBeforeLastInvocation
    public void shouldAllowAllChunksBeforeLastInvocation() {
        inOrder.verify(mockTwo, times(3)).simpleMethod(2);
        inOrder.verify(mockOne).simpleMethod(4);
        
        verifyNoMoreInteractions(mockTwo);
    }

// org.mockitousage.verification.RelaxedVerificationInOrderTest::shouldVerifyDetectFirstChunkOfInvocationThatExistInManyChunks
    public void shouldVerifyDetectFirstChunkOfInvocationThatExistInManyChunks() {
        inOrder.verify(mockTwo, times(2)).simpleMethod(2);
        inOrder.verify(mockThree).simpleMethod(3);
        try {
            verifyNoMoreInteractions(mockTwo);
            fail();
        } catch(NoInteractionsWanted e) {}
    }

// org.mockitousage.verification.RelaxedVerificationInOrderTest::shouldVerifyDetectAllChunksOfInvocationThatExistInManyChunks
    public void shouldVerifyDetectAllChunksOfInvocationThatExistInManyChunks() {
        inOrder.verify(mockTwo, times(3)).simpleMethod(2);
        inOrder.verify(mockOne).simpleMethod(4);
        verifyNoMoreInteractions(mockTwo);
    }

// org.mockitousage.verification.RelaxedVerificationInOrderTest::shouldVerifyInteractionsFromAllChunksWhenAtLeastOnceMode
    public void shouldVerifyInteractionsFromAllChunksWhenAtLeastOnceMode() {
        inOrder.verify(mockTwo, atLeastOnce()).simpleMethod(2);
        verifyNoMoreInteractions(mockTwo);
        try {
            inOrder.verify(mockThree).simpleMethod(3);
            fail();
        } catch (VerificationInOrderFailure e) {}
    }

// org.mockitousage.verification.RelaxedVerificationInOrderTest::shouldVerifyInteractionsFromFirstChunk
    public void shouldVerifyInteractionsFromFirstChunk() {
        inOrder.verify(mockTwo, times(2)).simpleMethod(2);
        try {
            verifyNoMoreInteractions(mockTwo);
            fail();
        } catch (NoInteractionsWanted e) {}
    }

// org.mockitousage.verification.RelaxedVerificationInOrderTest::shouldFailVerificationOfNonFirstChunk
    public void shouldFailVerificationOfNonFirstChunk() {
        inOrder.verify(mockTwo, times(1)).simpleMethod(2);
    }

// org.mockitousage.verification.RelaxedVerificationInOrderTest::shouldPassOnCombinationOfTimesAndAtLeastOnce
    public void shouldPassOnCombinationOfTimesAndAtLeastOnce() {
        mockTwo.simpleMethod(2);
        
        inOrder.verify(mockTwo, times(2)).simpleMethod(2);
        inOrder.verify(mockTwo, atLeastOnce()).simpleMethod(2);
        verifyNoMoreInteractions(mockTwo);
    }

// org.mockitousage.verification.RelaxedVerificationInOrderTest::shouldPassOnEdgyCombinationOfTimesAndAtLeastOnce
    public void shouldPassOnEdgyCombinationOfTimesAndAtLeastOnce() {
        mockTwo.simpleMethod(2);
        mockThree.simpleMethod(3);
        
        inOrder.verify(mockThree).simpleMethod(3);
        inOrder.verify(mockTwo, atLeastOnce()).simpleMethod(2);
        inOrder.verify(mockThree).simpleMethod(3);
        
        verifyNoMoreInteractions(mockThree);
    }

// org.mockitousage.verification.RelaxedVerificationInOrderTest::shouldVerifyInOrderMockTwoAndThree
    public void shouldVerifyInOrderMockTwoAndThree() {
        inOrder.verify(mockTwo, times(2)).simpleMethod(2);
        inOrder.verify(mockThree).simpleMethod(3);
        inOrder.verify(mockTwo).simpleMethod(2);
        verifyNoMoreInteractions(mockTwo, mockThree);
    }

// org.mockitousage.verification.RelaxedVerificationInOrderTest::shouldVerifyInOrderMockOneAndThree
    public void shouldVerifyInOrderMockOneAndThree() {
        inOrder.verify(mockOne).simpleMethod(1);
        inOrder.verify(mockThree).simpleMethod(3);
        inOrder.verify(mockOne).simpleMethod(4);
        verifyNoMoreInteractions(mockOne, mockThree);
    }

// org.mockitousage.verification.RelaxedVerificationInOrderTest::shouldVerifyInOrderOnlyTwoInvocations
    public void shouldVerifyInOrderOnlyTwoInvocations() {
        inOrder.verify(mockTwo, times(2)).simpleMethod(2);
        inOrder.verify(mockOne).simpleMethod(4);
    }

// org.mockitousage.verification.RelaxedVerificationInOrderTest::shouldVerifyInOrderOnlyMockTwo
    public void shouldVerifyInOrderOnlyMockTwo() {
        inOrder.verify(mockTwo, times(2)).simpleMethod(2);
        inOrder.verify(mockTwo).simpleMethod(2);
        verifyNoMoreInteractions(mockTwo);
    }

// org.mockitousage.verification.RelaxedVerificationInOrderTest::shouldVerifyMockTwoCalledTwice
    public void shouldVerifyMockTwoCalledTwice() {
        inOrder.verify(mockTwo, times(2)).simpleMethod(2);
    }

// org.mockitousage.verification.RelaxedVerificationInOrderTest::shouldVerifyMockTwoCalledAtLeastOnce
    public void shouldVerifyMockTwoCalledAtLeastOnce() {
        inOrder.verify(mockTwo, atLeastOnce()).simpleMethod(2);
    }

// org.mockitousage.verification.RelaxedVerificationInOrderTest::shouldFailOnWrongMethodCalledOnMockTwo
    public void shouldFailOnWrongMethodCalledOnMockTwo() {
        inOrder.verify(mockTwo, atLeastOnce()).differentMethod();
    }

// org.mockitousage.verification.RelaxedVerificationInOrderTest::shouldAllowTimesZeroButOnlyInOrder
    public void shouldAllowTimesZeroButOnlyInOrder() {
        inOrder.verify(mockTwo, atLeastOnce()).simpleMethod(2);
        inOrder.verify(mockOne, times(0)).simpleMethod(1);
        
        try {
            verify(mockOne, times(0)).simpleMethod(1);
            fail();
        } catch (NeverWantedButInvoked e) {}
    }

// org.mockitousage.verification.RelaxedVerificationInOrderTest::shouldFailTimesZeroInOrder
    public void shouldFailTimesZeroInOrder() {
        inOrder.verify(mockTwo, times(2)).simpleMethod(2);
        try {
            inOrder.verify(mockThree, times(0)).simpleMethod(3);
            fail();
        } catch (VerificationInOrderFailure e) {}
    }

// org.mockitousage.verification.RelaxedVerificationInOrderTest::shouldFailWhenMockTwoWantedZeroTimes
    public void shouldFailWhenMockTwoWantedZeroTimes() {
        inOrder.verify(mockTwo, times(0)).simpleMethod(2);
    }

// org.mockitousage.verification.RelaxedVerificationInOrderTest::shouldVerifyLastInvocation
    public void shouldVerifyLastInvocation() {
        inOrder.verify(mockOne).simpleMethod(4);
    }

// org.mockitousage.verification.RelaxedVerificationInOrderTest::shouldVerifySecondAndLastInvocation
    public void shouldVerifySecondAndLastInvocation() {
        inOrder.verify(mockTwo, atLeastOnce()).simpleMethod(2);
        inOrder.verify(mockOne).simpleMethod(4);
    }

// org.mockitousage.verification.RelaxedVerificationInOrderTest::shouldVerifySecondAndLastInvocationWhenAtLeastOnceUsed
    public void shouldVerifySecondAndLastInvocationWhenAtLeastOnceUsed() {
        inOrder.verify(mockTwo, atLeastOnce()).simpleMethod(2);
        inOrder.verify(mockOne).simpleMethod(4);
    }

// org.mockitousage.verification.RelaxedVerificationInOrderTest::shouldFailOnLastTwoInvocationsInWrongOrder
    public void shouldFailOnLastTwoInvocationsInWrongOrder() {
        inOrder.verify(mockOne).simpleMethod(4);
        try {
            inOrder.verify(mockTwo, atLeastOnce()).simpleMethod(2);
            fail();
        } catch (VerificationInOrderFailure e) {}
    }

// org.mockitousage.verification.RelaxedVerificationInOrderTest::shouldFailOnLastAndFirstInWrongOrder
    public void shouldFailOnLastAndFirstInWrongOrder() {
        inOrder.verify(mockOne).simpleMethod(4);
        try {
            inOrder.verify(mockOne).simpleMethod(1);
            fail();
        } catch (VerificationInOrderFailure e) {}
    }

// org.mockitousage.verification.RelaxedVerificationInOrderTest::shouldFailOnWrongMethodAfterLastInvocation
    public void shouldFailOnWrongMethodAfterLastInvocation() {
        inOrder.verify(mockOne).simpleMethod(4);
        try {
            inOrder.verify(mockOne).simpleMethod(999);
            fail();
        } catch (VerificationInOrderFailure e) {}
    }

// org.mockitousage.verification.SelectedMocksInOrderVerificationTest::shouldVerifyAllInvocationsInOrder
    public void shouldVerifyAllInvocationsInOrder() {
        InOrder inOrder = inOrder(mockOne, mockTwo, mockThree);
        inOrder.verify(mockOne).simpleMethod(1);
        inOrder.verify(mockTwo, times(2)).simpleMethod(2);
        inOrder.verify(mockThree).simpleMethod(3);
        inOrder.verify(mockTwo).simpleMethod(2);
        inOrder.verify(mockOne).simpleMethod(4);
        verifyNoMoreInteractions(mockOne, mockTwo, mockThree);
    }

// org.mockitousage.verification.SelectedMocksInOrderVerificationTest::shouldVerifyInOrderMockTwoAndThree
    public void shouldVerifyInOrderMockTwoAndThree() {
        InOrder inOrder = inOrder(mockTwo, mockThree);
        
        inOrder.verify(mockTwo, times(2)).simpleMethod(2);
        inOrder.verify(mockThree).simpleMethod(3);
        inOrder.verify(mockTwo).simpleMethod(2);
        verifyNoMoreInteractions(mockTwo, mockThree);
    }

// org.mockitousage.verification.SelectedMocksInOrderVerificationTest::shouldVerifyInOrderMockOneAndThree
    public void shouldVerifyInOrderMockOneAndThree() {
        InOrder inOrder = inOrder(mockOne, mockThree);
        
        inOrder.verify(mockOne).simpleMethod(1);
        inOrder.verify(mockThree).simpleMethod(3);
        inOrder.verify(mockOne).simpleMethod(4);
        verifyNoMoreInteractions(mockOne, mockThree);
    }

// org.mockitousage.verification.SelectedMocksInOrderVerificationTest::shouldVerifyMockOneInOrder
    public void shouldVerifyMockOneInOrder() {
        InOrder inOrder = inOrder(mockOne);
        
        inOrder.verify(mockOne).simpleMethod(1);
        inOrder.verify(mockOne).simpleMethod(4);
        
        verifyNoMoreInteractions(mockOne);
    }

// org.mockitousage.verification.SelectedMocksInOrderVerificationTest::shouldFailVerificationForMockOne
    public void shouldFailVerificationForMockOne() {
        InOrder inOrder = inOrder(mockOne);
        
        inOrder.verify(mockOne).simpleMethod(1);
        try {
            inOrder.verify(mockOne).differentMethod();
            fail();
        } catch (VerificationInOrderFailure e) {}
    }

// org.mockitousage.verification.SelectedMocksInOrderVerificationTest::shouldFailVerificationForMockOneBecauseOfWrongOrder
    public void shouldFailVerificationForMockOneBecauseOfWrongOrder() {
        InOrder inOrder = inOrder(mockOne);
        
        inOrder.verify(mockOne).simpleMethod(4);
        try {
            inOrder.verify(mockOne).simpleMethod(1);
            fail();
        } catch (VerificationInOrderFailure e) {}
    }

// org.mockitousage.verification.SelectedMocksInOrderVerificationTest::shouldVerifyMockTwoWhenThreeTimesUsed
    public void shouldVerifyMockTwoWhenThreeTimesUsed() {
        InOrder inOrder = inOrder(mockTwo);
        
        inOrder.verify(mockTwo, times(3)).simpleMethod(2);
        
        verifyNoMoreInteractions(mockTwo);
    }

// org.mockitousage.verification.SelectedMocksInOrderVerificationTest::shouldVerifyMockTwo
    public void shouldVerifyMockTwo() {
        InOrder inOrder = inOrder(mockTwo);
        
        inOrder.verify(mockTwo, atLeastOnce()).simpleMethod(2);
        
        verifyNoMoreInteractions(mockTwo);
    }

// org.mockitousage.verification.SelectedMocksInOrderVerificationTest::shouldFailVerificationForMockTwo
    public void shouldFailVerificationForMockTwo() {
        InOrder inOrder = inOrder(mockTwo);

        try {
            inOrder.verify(mockTwo).simpleMethod(2);
            fail();
        } catch (VerificationInOrderFailure e) {}
    }

// org.mockitousage.verification.SelectedMocksInOrderVerificationTest::shouldThrowNoMoreInvocationsForMockTwo
    public void shouldThrowNoMoreInvocationsForMockTwo() {
        InOrder inOrder = inOrder(mockTwo);

        try {
            inOrder.verify(mockTwo, times(2)).simpleMethod(2);
            fail();
        } catch (VerificationInOrderFailure e) {}
    }

// org.mockitousage.verification.SelectedMocksInOrderVerificationTest::shouldThrowTooLittleInvocationsForMockTwo
    public void shouldThrowTooLittleInvocationsForMockTwo() {
        InOrder inOrder = inOrder(mockTwo);

        try {
            inOrder.verify(mockTwo, times(4)).simpleMethod(2);
            fail();
        } catch (VerificationInOrderFailure e) {}
    }

// org.mockitousage.verification.SelectedMocksInOrderVerificationTest::shouldThrowTooManyInvocationsForMockTwo
    public void shouldThrowTooManyInvocationsForMockTwo() {
        InOrder inOrder = inOrder(mockTwo);

        try {
            inOrder.verify(mockTwo, times(2)).simpleMethod(2);
            fail();
        } catch (VerificationInOrderFailure e) {}
    }

// org.mockitousage.verification.SelectedMocksInOrderVerificationTest::shouldAllowThreeTimesOnMockTwo
    public void shouldAllowThreeTimesOnMockTwo() {
        InOrder inOrder = inOrder(mockTwo);

        inOrder.verify(mockTwo, times(3)).simpleMethod(2);
        verifyNoMoreInteractions(mockTwo);
    }

// org.mockitousage.verification.SelectedMocksInOrderVerificationTest::shouldVerifyMockTwoCompletely
    public void shouldVerifyMockTwoCompletely() {
        InOrder inOrder = inOrder(mockTwo, mockThree);

        inOrder.verify(mockTwo, times(2)).simpleMethod(2);
        inOrder.verify(mockThree).simpleMethod(3);
        inOrder.verify(mockTwo).simpleMethod(2);
        verifyNoMoreInteractions(mockTwo, mockThree);
    }

// org.mockitousage.verification.SelectedMocksInOrderVerificationTest::shouldAllowTwoTimesOnMockTwo
    public void shouldAllowTwoTimesOnMockTwo() {
        InOrder inOrder = inOrder(mockTwo, mockThree);

        inOrder.verify(mockTwo, times(2)).simpleMethod(2);
        try {
            verifyNoMoreInteractions(mockTwo);
            fail();
        } catch (NoInteractionsWanted e) {}
    }

// org.mockitousage.verification.VerificationInOrderMixedWithOrdiraryVerificationTest::shouldMixVerificationInOrderAndOrdinaryVerification
    public void shouldMixVerificationInOrderAndOrdinaryVerification() {
        inOrder.verify(mockOne, atLeastOnce()).simpleMethod(1);
        inOrder.verify(mockThree).simpleMethod(3);
        inOrder.verify(mockThree).simpleMethod(4);
        verify(mockTwo).simpleMethod(2);
        
        verifyNoMoreInteractions(mockOne, mockTwo, mockThree);
    }

// org.mockitousage.verification.VerificationInOrderMixedWithOrdiraryVerificationTest::shouldAllowOrdinarilyVerifyingMockPassedToInOrderObject
    public void shouldAllowOrdinarilyVerifyingMockPassedToInOrderObject() {
        inOrder.verify(mockOne, atLeastOnce()).simpleMethod(1);

        verify(mockThree).simpleMethod(3);
        verify(mockThree).simpleMethod(4);
        verify(mockTwo).simpleMethod(2);
        
        verifyNoMoreInteractions(mockOne, mockTwo, mockThree);
    }

// org.mockitousage.verification.VerificationInOrderMixedWithOrdiraryVerificationTest::shouldAllowRedundantVerifications
    public void shouldAllowRedundantVerifications() {
        verify(mockOne, atLeastOnce()).simpleMethod(1);
        verify(mockTwo).simpleMethod(2);
        verify(mockThree).simpleMethod(3);
        verify(mockThree).simpleMethod(4);
        
        inOrder.verify(mockOne, atLeastOnce()).simpleMethod(1);
        inOrder.verify(mockThree).simpleMethod(3);
        inOrder.verify(mockThree).simpleMethod(4);
        
        verifyNoMoreInteractions(mockOne, mockTwo, mockThree);
    }

// org.mockitousage.verification.VerificationInOrderMixedWithOrdiraryVerificationTest::shouldFailOnNoMoreInteractions
    public void shouldFailOnNoMoreInteractions() {
        inOrder.verify(mockOne, atLeastOnce()).simpleMethod(1);
        inOrder.verify(mockThree).simpleMethod(3);
        inOrder.verify(mockThree).simpleMethod(4);
        
        try {
            verifyNoMoreInteractions(mockOne, mockTwo, mockThree);
            fail();
        } catch (NoInteractionsWanted e) {}
    }

// org.mockitousage.verification.VerificationInOrderMixedWithOrdiraryVerificationTest::shouldFailOnNoMoreInteractionsOnMockVerifiedInOrder
    public void shouldFailOnNoMoreInteractionsOnMockVerifiedInOrder() {
        inOrder.verify(mockOne, atLeastOnce()).simpleMethod(1);
        inOrder.verify(mockThree).simpleMethod(3);
        verify(mockTwo).simpleMethod(2);
        
        try {
            verifyNoMoreInteractions(mockOne, mockTwo, mockThree);
            fail();
        } catch (NoInteractionsWanted e) {}
    }

// org.mockitousage.verification.VerificationInOrderMixedWithOrdiraryVerificationTest::shouldAllowOneMethodVerifiedInOrder
    public void shouldAllowOneMethodVerifiedInOrder() {
        verify(mockTwo).simpleMethod(2);
        verify(mockOne, atLeastOnce()).simpleMethod(1);

        inOrder.verify(mockOne, atLeastOnce()).simpleMethod(1);
    }

// org.mockitousage.verification.VerificationInOrderMixedWithOrdiraryVerificationTest::shouldFailOnLastInvocationTooEarly
    public void shouldFailOnLastInvocationTooEarly() {
        inOrder.verify(mockThree).simpleMethod(4);
        
        verify(mockThree).simpleMethod(4);
        verify(mockTwo).simpleMethod(2);
        
        try {
            inOrder.verify(mockOne, atLeastOnce()).simpleMethod(1);
            fail();
        } catch (VerificationInOrderFailure e) {}
    }

// org.mockitousage.verification.VerificationInOrderMixedWithOrdiraryVerificationTest::shouldScreamWhenUnfamiliarMockPassedToInOrderObject
    public void shouldScreamWhenUnfamiliarMockPassedToInOrderObject() {
        inOrder.verify(mockTwo, atLeastOnce()).simpleMethod(1);
    }

// org.mockitousage.verification.VerificationInOrderMixedWithOrdiraryVerificationTest::shouldUseEqualsToVerifyMethodArguments
    public void shouldUseEqualsToVerifyMethodArguments() {
        mockOne = mock(IMethods.class);
        
        String textOne = "test";
        String textTwo = new String(textOne);
        
        assertEquals(textOne, textTwo);
        assertNotSame(textOne, textTwo);
        
        mockOne.simpleMethod(textOne);
        mockOne.simpleMethod(textTwo);
        
        verify(mockOne, times(2)).simpleMethod(textOne);
        
        inOrder = inOrder(mockOne);
        inOrder.verify(mockOne, times(2)).simpleMethod(textOne);
    }

// org.mockitousage.verification.VerificationInOrderMixedWithOrdiraryVerificationTest::shouldUseEqualsToVerifyMethodVarargs
    public void shouldUseEqualsToVerifyMethodVarargs() {
        mockOne = mock(IMethods.class);
        
        String textOne = "test";
        String textTwo = new String(textOne);
        
        assertEquals(textOne, textTwo);
        assertNotSame(textOne, textTwo);
        
        mockOne.varargsObject(1, textOne, textOne);
        mockOne.varargsObject(1, textTwo, textTwo);
        
        verify(mockOne, times(2)).varargsObject(1, textOne, textOne);
        
        inOrder = inOrder(mockOne);
        inOrder.verify(mockOne, times(2)).varargsObject(1, textOne, textOne);
    }

// org.mockitousage.verification.VerificationInOrderTest::shouldVerifySingleMockInOrderAndNotInOrder
    public void shouldVerifySingleMockInOrderAndNotInOrder() {
        mockOne = mock(IMethods.class);
        inOrder = inOrder(mockOne);
        
        mockOne.simpleMethod(1);
        mockOne.simpleMethod(2);
        
        verify(mockOne).simpleMethod(2);
        verify(mockOne).simpleMethod(1);
        
        inOrder.verify(mockOne).simpleMethod(2);
        try {
            inOrder.verify(mockOne).simpleMethod(1);
            fail();
        } catch (VerificationInOrderFailure e) {}
    }

// org.mockitousage.verification.VerificationInOrderTest::shouldMessagesPointToProperMethod
    public void shouldMessagesPointToProperMethod() {
        mockTwo.differentMethod();
        mockOne.simpleMethod();
        
        try {
            inOrder.verify(mockOne, atLeastOnce()).differentMethod();
            fail();
        } catch (WantedButNotInvoked e) {
            assertContains("differentMethod()", e.getMessage());
        }
    }

// org.mockitousage.verification.VerificationInOrderTest::shouldVerifyInOrderWhenTwoChunksAreEqual
    public void shouldVerifyInOrderWhenTwoChunksAreEqual() {
        mockOne.simpleMethod();
        mockOne.simpleMethod();
        mockTwo.differentMethod();
        mockOne.simpleMethod();
        mockOne.simpleMethod();
        
        inOrder.verify(mockOne, times(2)).simpleMethod();
        inOrder.verify(mockTwo).differentMethod();
        inOrder.verify(mockOne, times(2)).simpleMethod();
        try {
            inOrder.verify(mockOne, atLeastOnce()).simpleMethod();
            fail();
        } catch (VerificationInOrderFailure e) {}
    }

// org.mockitousage.verification.VerificationInOrderTest::shouldVerifyInOrderUsingMatcher
    public void shouldVerifyInOrderUsingMatcher() {
        mockOne.simpleMethod(1);
        mockOne.simpleMethod(2);
        mockTwo.differentMethod();
        mockOne.simpleMethod(3);
        mockOne.simpleMethod(4);
        
        verify(mockOne, times(4)).simpleMethod(anyInt());
        
        inOrder.verify(mockOne, times(2)).simpleMethod(anyInt());
        inOrder.verify(mockTwo).differentMethod();
        inOrder.verify(mockOne, times(2)).simpleMethod(anyInt());
        try {
            inOrder.verify(mockOne, times(3)).simpleMethod(anyInt());
            fail();
        } catch (VerificationInOrderFailure e) {}
    }

// org.mockitousage.verification.VerificationOnMultipleMocksUsingMatchersTest::shouldVerifyUsingMatchers
    public void shouldVerifyUsingMatchers() throws Exception {
        List list = Mockito.mock(List.class);
        HashMap map = Mockito.mock(HashMap.class);
        
        list.add("test");
        list.add(1, "test two");
        
        map.put("test", 100);
        map.put("test two", 200);
        
        verify(list).add(anyObject());
        verify(list).add(anyInt(), eq("test two"));
        
        verify(map, times(2)).put(anyObject(), anyObject());
        verify(map).put(eq("test two"), eq(200));
        
        verifyNoMoreInteractions(list, map);
    }

// org.mockitousage.verification.VerificationOnMultipleMocksUsingMatchersTest::shouldVerifyMultipleMocks
    public void shouldVerifyMultipleMocks() throws Exception {
        List list = mock(List.class);
        Map map = mock(Map.class);
        Set set = mock(Set.class);

        list.add("one");
        list.add("one");
        list.add("two");
        
        map.put("one", 1);
        map.put("one", 1);
        
        verify(list, times(2)).add("one");
        verify(list, times(1)).add("two");
        verify(list, times(0)).add("three");
        
        verify(map, times(2)).put(anyObject(), anyInt());
        
        verifyNoMoreInteractions(list, map);
        verifyZeroInteractions(set);
    }

// org.mockitousage.verification.VerificationUsingMatchersTest::shouldVerifyExactNumberOfInvocationsUsingMatcher
    public void shouldVerifyExactNumberOfInvocationsUsingMatcher() {
        mock.simpleMethod(1);
        mock.simpleMethod(2);
        mock.simpleMethod(3);
        
        verify(mock, times(3)).simpleMethod(anyInt());
    }

// org.mockitousage.verification.VerificationUsingMatchersTest::shouldVerifyUsingSameMatcher
    public void shouldVerifyUsingSameMatcher() {
        Object one = new String("1243");
        Object two = new String("1243");
        Object three = new String("1243");

        assertNotSame(one, two);
        assertEquals(one, two);
        assertEquals(two, three);

        mock.oneArg(one);
        mock.oneArg(two);
        
        verify(mock).oneArg(same(one));
        verify(mock, times(2)).oneArg(two);
        
        try {
            verify(mock).oneArg(same(three));
            fail();
        } catch (WantedButNotInvoked e) {}
    }

// org.mockitousage.verification.VerificationUsingMatchersTest::shouldVerifyUsingMixedMatchers
    public void shouldVerifyUsingMixedMatchers() {
        mock.threeArgumentMethod(11, "", "01234");

        try {
            verify(mock).threeArgumentMethod(and(geq(7), leq(10)), isA(String.class), Matchers.contains("123"));
            fail();
        } catch (ArgumentsAreDifferent e) {}

        mock.threeArgumentMethod(8, new Object(), "01234");
        
        try {
            verify(mock).threeArgumentMethod(and(geq(7), leq(10)), isA(String.class), Matchers.contains("123"));
            fail();
        } catch (ArgumentsAreDifferent e) {}
        
        mock.threeArgumentMethod(8, "", "no match");

        try {
            verify(mock).threeArgumentMethod(and(geq(7), leq(10)), isA(String.class), Matchers.contains("123"));
            fail();
        } catch (ArgumentsAreDifferent e) {}
        
        mock.threeArgumentMethod(8, "", "123");
        
        verify(mock).threeArgumentMethod(and(geq(7), leq(10)), isA(String.class), Matchers.contains("123"));
    }

// org.mockitousage.verification.VerificationWithTimeoutTest::shouldVerify
    public void shouldVerify() throws Exception {
        mock.clear();
        
        
        
    }

// buggy code
    public Object answer(InvocationOnMock invocation) {
        if (methodsGuru.isToString(invocation.getMethod())) {
            Object mock = invocation.getMock();
            MockName name = mockUtil.getMockName(mock);
            if (name.isDefault()) {
                return "Mock for " + mockUtil.getMockSettings(mock).getTypeToMock().getSimpleName() + ", hashCode: " + mock.hashCode();
            } else {
                return name.toString();
            }
        } else if (methodsGuru.isCompareToMethod(invocation.getMethod())) {
            //see issue 184.
            //mocks by default should return 0 if references are the same, otherwise some other value because they are not the same. Hence we return 1 (anything but 0 is good).
            //Only for compareTo() method by the Comparable interface
            return 1;
        }
        
        Class<?> returnType = invocation.getMethod().getReturnType();
        return returnValueFor(returnType);
    }

// relevant test
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

// org.mockitousage.verification.DescriptiveMessagesWhenVerificationFailsTest::should_print_method_name
    public void should_print_method_name() {
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

// org.mockitousage.verification.DescriptiveMessagesWhenVerificationFailsTest::should_print_method_name_and_arguments
    public void should_print_method_name_and_arguments() {
        try {
            verify(mock).threeArgumentMethod(12, new Foo(), "xx");
            fail();
        } catch (WantedButNotInvoked e) {
            assertContains("iMethods.threeArgumentMethod(12, foo, \"xx\")", e.getMessage());
        }
    }

// org.mockitousage.verification.DescriptiveMessagesWhenVerificationFailsTest::should_print_actual_and_wanted_in_line
    public void should_print_actual_and_wanted_in_line() {
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

// org.mockitousage.verification.DescriptiveMessagesWhenVerificationFailsTest::should_print_actual_and_wanted_in_multiple_lines
    public void should_print_actual_and_wanted_in_multiple_lines() {
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

// org.mockitousage.verification.DescriptiveMessagesWhenVerificationFailsTest::should_print_actual_and_wanted_when_actual_method_name_and_wanted_method_name_are_the_same
    public void should_print_actual_and_wanted_when_actual_method_name_and_wanted_method_name_are_the_same() {
        mock.simpleMethod();

        try {
            verify(mock).simpleMethod(10);
            fail();
        } catch (ArgumentsAreDifferent e) {
            assertContains("simpleMethod(10)", e.getMessage());
            assertContains("simpleMethod()", e.getMessage());
        }
    }

// org.mockitousage.verification.DescriptiveMessagesWhenVerificationFailsTest::should_print_actual_and_unverified_wanted_when_the_difference_is_about_arguments
    public void should_print_actual_and_unverified_wanted_when_the_difference_is_about_arguments() {
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

// org.mockitousage.verification.DescriptiveMessagesWhenVerificationFailsTest::should_print_first_unexpected_invocation
    public void should_print_first_unexpected_invocation() {
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

// org.mockitousage.verification.DescriptiveMessagesWhenVerificationFailsTest::should_print_first_unexpected_invocation_when_verifying_zero_interactions
    public void should_print_first_unexpected_invocation_when_verifying_zero_interactions() {
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

// org.mockitousage.verification.DescriptiveMessagesWhenVerificationFailsTest::should_print_method_name_when_verifying_at_least_once
    public void should_print_method_name_when_verifying_at_least_once() throws Exception {
        try {
            verify(mock, atLeastOnce()).twoArgumentMethod(1, 2);
            fail();
        } catch (WantedButNotInvoked e) {
            assertContains("twoArgumentMethod(1, 2)", e.getMessage());
        }
    }

// org.mockitousage.verification.DescriptiveMessagesWhenVerificationFailsTest::should_print_method_when_matcher_used
    public void should_print_method_when_matcher_used() throws Exception {
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

// org.mockitousage.verification.DescriptiveMessagesWhenVerificationFailsTest::should_print_method_when_missing_invocation_with_array_matcher
    public void should_print_method_when_missing_invocation_with_array_matcher() {
        mock.oneArray(new boolean[] { true, false, false });

        try {
            verify(mock).oneArray(aryEq(new boolean[] { false, false, false }));
            fail();
        } catch (ArgumentsAreDifferent e) {
            assertContains("[false, false, false]", e.getMessage());
            assertContains("[true, false, false]", e.getMessage());
        }
    }

// org.mockitousage.verification.DescriptiveMessagesWhenVerificationFailsTest::should_print_method_when_missing_invocation_with_vararg_matcher
    public void should_print_method_when_missing_invocation_with_vararg_matcher() {
        mock.varargsString(10, "xxx", "yyy", "zzz");

        try {
            verify(mock).varargsString(10, "111", "222", "333");
            fail();
        } catch (ArgumentsAreDifferent e) {
            assertContains("111", e.getMessage());
            assertContains("\"xxx\"", e.getMessage());
        }
    }

// org.mockitousage.verification.DescriptiveMessagesWhenVerificationFailsTest::should_print_method_when_missing_invocation_with_matcher
    public void should_print_method_when_missing_invocation_with_matcher() {
        mock.simpleMethod("foo");

        try {
            verify(mock).simpleMethod(matches("burrito from Exmouth"));
            fail();
        } catch (ArgumentsAreDifferent e) {
            assertContains("matches(\"burrito from Exmouth\")", e.getMessage());
            assertContains("\"foo\"", e.getMessage());
        }
    }

// org.mockitousage.verification.DescriptiveMessagesWhenVerificationFailsTest::should_print_null_arguments
    public void should_print_null_arguments() throws Exception {
        mock.simpleMethod(null, (Integer) null);
        try {
            verify(mock).simpleMethod("test");
            fail();
        } catch (ArgumentsAreDifferent e) {
            assertContains("simpleMethod(null, null);", e.getMessage());
        }
    }

// org.mockitousage.verification.DescriptiveMessagesWhenVerificationFailsTest::should_say_never_wanted_but_invoked
    public void should_say_never_wanted_but_invoked() throws Exception {
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

// org.mockitousage.verification.DescriptiveMessagesWhenVerificationFailsTest::should_show_right_actual_method
    public void should_show_right_actual_method() throws Exception {
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

// org.mockitousage.verification.DescriptiveMessagesWhenVerificationFailsTest::should_print_field_name_when_annotations_used
    public void should_print_field_name_when_annotations_used() throws Exception {
        iHavefunkyName.simpleMethod(10);
    
        try {
            verify(iHavefunkyName).simpleMethod(20);
            fail();
        } catch (ArgumentsAreDifferent e) {
            assertContains("iHavefunkyName.simpleMethod(20)", e.getMessage());
            assertContains("iHavefunkyName.simpleMethod(10)", e.getMessage());
        }
    }

// org.mockitousage.verification.DescriptiveMessagesWhenVerificationFailsTest::should_print_interactions_on_mock_when_ordinary_verification_fail
    public void should_print_interactions_on_mock_when_ordinary_verification_fail() throws Exception {
        mock.otherMethod();
        mock.booleanReturningMethod();
        
        try {
            verify(mock).simpleMethod();
            fail();
        } catch (WantedButNotInvoked e) {

        }
    }

// org.mockitousage.verification.DescriptiveMessagesWhenVerificationFailsTest::should_never_break_method_string_when_no_args_in_method
    public void should_never_break_method_string_when_no_args_in_method() throws Exception {
        try {
            verify(veeeeeeeeeeeeeeeeeeeeeeeerylongNameMock).simpleMethod();
            fail();
        } catch(WantedButNotInvoked e) {
            assertContains("veeeeeeeeeeeeeeeeeeeeeeeerylongNameMock.simpleMethod()", e.getMessage());
        }
    }

// org.mockitousage.verification.DescriptiveMessagesWhenVerificationFailsTest::should_print_method_name_and_arguments_of_other_interactions_with_different_methods
    public void should_print_method_name_and_arguments_of_other_interactions_with_different_methods() throws Exception {
        try {
            mock.arrayMethod(new String[] {"a", "b", "c"});
            mock.forByte((byte) 25);

            verify(mock).threeArgumentMethod(12, new Foo(), "xx");
            fail();
        } catch (WantedButNotInvoked e) {
            System.out.println(e);
            assertContains("iMethods.threeArgumentMethod(12, foo, \"xx\")", e.getMessage());
            assertContains("iMethods.arrayMethod([\"a\", \"b\", \"c\"])", e.getMessage());
            assertContains("iMethods.forByte(25)", e.getMessage());
        }
    }

// org.mockitousage.verification.DescriptiveMessagesWhenVerificationFailsTest::should_print_method_name_and_arguments_of_other_interactions_of_same_method
    public void should_print_method_name_and_arguments_of_other_interactions_of_same_method() throws Exception {
        try {
            mock.forByte((byte) 25);
            mock.forByte((byte) 12);

            verify(mock).forByte((byte) 42);
            fail();
        } catch (WantedButNotInvoked e) {
            System.out.println(e);
            assertContains("iMethods.forByte(42)", e.getMessage());
            assertContains("iMethods.forByte(25)", e.getMessage());
            assertContains("iMethods.forByte(12)", e.getMessage());
        }
    }

// org.mockitousage.verification.DescriptiveMessagesWhenVerificationFailsTest::test1
    public void test1() {
        AnInterface m = Mockito.mock(AnInterface.class);

        for (int i = 1; i <= 2; i++) {
            m.foo(i);
        }

        verify(m).foo(1);
        verify(m).foo(2);
        verify(m).foo(3); 
        verify(m).foo(4);
    }

// org.mockitousage.verification.DescriptiveMessagesWhenVerificationFailsTest::test2
    public void test2() {
        AnInterface m = Mockito.mock(AnInterface.class);

        for (int i = 1; i <= 4; i++) {
            m.foo(i);
        }

        verify(m).foo(1);
        verify(m).foo(2);
        verify(m).foo(5); 
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

// org.mockitousage.verification.FindingRedundantInvocationsInOrderTest::shouldWorkFineIfNoInvocatins
    public void shouldWorkFineIfNoInvocatins() throws Exception {
        
        InOrder inOrder = inOrder(mock);
        
        
        inOrder.verifyNoMoreInteractions();        
    }

// org.mockitousage.verification.FindingRedundantInvocationsInOrderTest::shouldSayNoInteractionsWanted
    public void shouldSayNoInteractionsWanted() throws Exception {
        
        mock.simpleMethod();
        
        
        InOrder inOrder = inOrder(mock);
        try {
            inOrder.verifyNoMoreInteractions();
            fail();
        } catch(VerificationInOrderFailure e) {
            assertContains("No interactions wanted", e.getMessage());
        }
    }

// org.mockitousage.verification.FindingRedundantInvocationsInOrderTest::shouldVerifyNoMoreInteractionsInOrder
    public void shouldVerifyNoMoreInteractionsInOrder() throws Exception {
        
        mock.simpleMethod();
        mock.simpleMethod(10);
        mock.otherMethod();
        
        
        InOrder inOrder = inOrder(mock);
        inOrder.verify(mock).simpleMethod(10);
        inOrder.verify(mock).otherMethod();
        inOrder.verifyNoMoreInteractions();        
    }

// org.mockitousage.verification.FindingRedundantInvocationsInOrderTest::shouldVerifyNoMoreInteractionsInOrderWithMultipleMocks
    public void shouldVerifyNoMoreInteractionsInOrderWithMultipleMocks() throws Exception {
        
        mock.simpleMethod();
        mock2.simpleMethod();
        mock.otherMethod();
        
        
        InOrder inOrder = inOrder(mock, mock2);
        inOrder.verify(mock2).simpleMethod();
        inOrder.verify(mock).otherMethod();
        inOrder.verifyNoMoreInteractions();        
    }

// org.mockitousage.verification.FindingRedundantInvocationsInOrderTest::shouldFailToVerifyNoMoreInteractionsInOrder
    public void shouldFailToVerifyNoMoreInteractionsInOrder() throws Exception {
        
        mock.simpleMethod();
        mock.simpleMethod(10);
        mock.otherMethod();
        
        
        InOrder inOrder = inOrder(mock);
        inOrder.verify(mock).simpleMethod(10);
        try {
            inOrder.verifyNoMoreInteractions();
            fail();
        } catch(VerificationInOrderFailure e) {}
    }

// org.mockitousage.verification.FindingRedundantInvocationsInOrderTest::shouldFailToVerifyNoMoreInteractionsInOrderWithMultipleMocks
    public void shouldFailToVerifyNoMoreInteractionsInOrderWithMultipleMocks() throws Exception {
        
        mock.simpleMethod();
        mock2.simpleMethod();
        mock.otherMethod();
        
        
        InOrder inOrder = inOrder(mock, mock2);
        inOrder.verify(mock2).simpleMethod();
        try {
            inOrder.verifyNoMoreInteractions();
            fail();
        } catch(VerificationInOrderFailure e) {}
    }

// org.mockitousage.verification.FindingRedundantInvocationsInOrderTest::shouldValidateState
    public void shouldValidateState() throws Exception {
        
        InOrder inOrder = inOrder(mock);
        verify(mock); 
        
        
        try {
            inOrder.verifyNoMoreInteractions();
            fail();
        } catch(UnfinishedVerificationException e) {}
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

// org.mockitousage.verification.NoMoreInteractionsVerificationTest::shouldPrintAllInvocationsWhenVerifyingNoMoreInvocations
    public void shouldPrintAllInvocationsWhenVerifyingNoMoreInvocations() throws Exception {
        mock.add(1);
        mock.add(2);
        mock.clear();
        
        verify(mock).add(2);
        try {
            verifyNoMoreInteractions(mock);
            fail();
        } catch (NoInteractionsWanted e) {
            assertContains("list of all invocations", e.getMessage());
        }
    }

// org.mockitousage.verification.NoMoreInteractionsVerificationTest::shouldNotContainAllInvocationsWhenSingleUnwantedFound
    public void shouldNotContainAllInvocationsWhenSingleUnwantedFound() throws Exception {
        mock.add(1);
        
        try {
            verifyNoMoreInteractions(mock);
            fail();
        } catch (NoInteractionsWanted e) {
            assertNotContains("list of all invocations", e.getMessage());
        }
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
			verify(mock, only()).get(999);
			fail();
		} catch (WantedButNotInvoked e) {}
	}

// org.mockitousage.verification.OnlyVerificationTest::shouldFailIfExtraMethodWithDifferentArgsFound
	public void shouldFailIfExtraMethodWithDifferentArgsFound() {
	    mock.get(0);
	    mock.get(2);
	    try {
	        verify(mock, only()).get(2);
	        fail();
	    } catch (NoInteractionsWanted e) {}
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

// org.mockitousage.verification.VerificationAfterDelayTest::shouldVerifyNormallyWithSpecificTimes
    public void shouldVerifyNormallyWithSpecificTimes() {}

// org.mockitousage.verification.VerificationAfterDelayTest::shouldVerifyNormallyWithAtLeast
    public void shouldVerifyNormallyWithAtLeast() {}

// org.mockitousage.verification.VerificationAfterDelayTest::shouldFailVerificationWithWrongTimes
    public void shouldFailVerificationWithWrongTimes() throws Exception {
        
        Thread t = waitAndExerciseMock(20);

        
        t.start();

        
        verify(mock, times(0)).clear();
        
        expected.expect(MockitoAssertionError.class);
        verify(mock, after(50).times(2)).clear();
    }

// org.mockitousage.verification.VerificationAfterDelayTest::shouldWaitTheFullTimeIfTheTestCouldPass
    public void shouldWaitTheFullTimeIfTheTestCouldPass() throws Exception {
        
        Thread t = waitAndExerciseMock(50);

        
        t.start();

        
        long startTime = System.currentTimeMillis();
        
        try {
            verify(mock, after(100).atLeast(2)).clear();
            fail();
        } catch (MockitoAssertionError e) {}
        
        assertTrue(System.currentTimeMillis() - startTime >= 100);
    }

// org.mockitousage.verification.VerificationAfterDelayTest::shouldStopEarlyIfTestIsDefinitelyFailed
    public void shouldStopEarlyIfTestIsDefinitelyFailed() throws Exception {
        
        Thread t = waitAndExerciseMock(50);
        
        
        t.start();
        
        
        expected.expect(MockitoAssertionError.class);
        verify(mock, after(10000).never()).clear();
    }

// org.mockitousage.verification.VerificationExcludingStubsTest::shouldAllowToExcludeStubsForVerification
    public void shouldAllowToExcludeStubsForVerification() throws Exception {
        
        when(mock.simpleMethod()).thenReturn("foo");

        
        String stubbed = mock.simpleMethod(); 
        mock.objectArgMethod(stubbed);

        
        verify(mock).objectArgMethod("foo");

        
        try { verifyNoMoreInteractions(mock); fail(); } catch (NoInteractionsWanted e) {};
        
        
        ignoreStubs(mock);
        verifyNoMoreInteractions(mock);
    }

// org.mockitousage.verification.VerificationExcludingStubsTest::shouldExcludeFromVerificationInOrder
    public void shouldExcludeFromVerificationInOrder() throws Exception {
        
        when(mock.simpleMethod()).thenReturn("foo");

        
        mock.objectArgMethod("1");
        mock.objectArgMethod("2");
        mock.simpleMethod(); 

        
        InOrder inOrder = inOrder(ignoreStubs(mock));
        inOrder.verify(mock).objectArgMethod("1");
        inOrder.verify(mock).objectArgMethod("2");
        inOrder.verifyNoMoreInteractions();
        verifyNoMoreInteractions(mock);
    }

// org.mockitousage.verification.VerificationExcludingStubsTest::shouldIgnoringStubsDetectNulls
    public void shouldIgnoringStubsDetectNulls() throws Exception {
        ignoreStubs(mock, null);
    }

// org.mockitousage.verification.VerificationExcludingStubsTest::shouldIgnoringStubsDetectNonMocks
    public void shouldIgnoringStubsDetectNonMocks() throws Exception {
        ignoreStubs(mock, new Object());
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

// org.mockitousage.verification.VerificationInOrderWithCallsTest::shouldFailWhenMethodNotCalled
    public void shouldFailWhenMethodNotCalled(){
        
        mockOne.oneArg( 1 );
        InOrder verifier = inOrder( mockOne );
        verifier.verify( mockOne, calls(1)).oneArg( 1 );

        exceptionRule.expect( VerificationInOrderFailure.class );
        exceptionRule.expectMessage( "Verification in order failure" );
        exceptionRule.expectMessage( "Wanted but not invoked" );
        exceptionRule.expectMessage( "mockOne.oneArg(2)" );

        
        verifier.verify( mockOne, calls(1)).oneArg( 2 );

        
    }

// org.mockitousage.verification.VerificationInOrderWithCallsTest::shouldFailWhenMethodCalledTooFewTimes
    public void shouldFailWhenMethodCalledTooFewTimes(){
        
        mockOne.oneArg( 1 );
        mockOne.oneArg( 2 );

        InOrder verifier = inOrder( mockOne );
        verifier.verify( mockOne, calls(1)).oneArg( 1 );

        exceptionRule.expect( VerificationInOrderFailure.class );
        exceptionRule.expectMessage( "Verification in order failure" );
        exceptionRule.expectMessage( "mockOne.oneArg(2)" );
        exceptionRule.expectMessage( "Wanted 2 times" );
        exceptionRule.expectMessage( "But was 1 time" );

        
        verifier.verify( mockOne, calls(2)).oneArg( 2 );

        
    }

// org.mockitousage.verification.VerificationInOrderWithCallsTest::shouldFailWhenSingleMethodCallsAreOutOfSequence
    public void shouldFailWhenSingleMethodCallsAreOutOfSequence(){
        
        mockOne.oneArg( 1 );
        mockOne.oneArg( 2 );

        InOrder verifier = inOrder( mockOne );
        verifier.verify( mockOne, calls(1)).oneArg( 2 );

        exceptionRule.expect( VerificationInOrderFailure.class );
        exceptionRule.expectMessage( "Verification in order failure" );
        exceptionRule.expectMessage( "Wanted but not invoked" );
        exceptionRule.expectMessage( "mockOne.oneArg(1)" );

        
        verifier.verify( mockOne, calls(1)).oneArg( 1 );

        
    }

// org.mockitousage.verification.VerificationInOrderWithCallsTest::shouldFailWhenDifferentMethodCallsAreOutOfSequence
    public void shouldFailWhenDifferentMethodCallsAreOutOfSequence(){
        
        mockOne.oneArg( 1 );
        mockOne.voidMethod();

        InOrder verifier = inOrder( mockOne );
        verifier.verify( mockOne, calls(1)).voidMethod();

        exceptionRule.expect( VerificationInOrderFailure.class );
        exceptionRule.expectMessage( "Verification in order failure" );
        exceptionRule.expectMessage( "Wanted but not invoked" );
        exceptionRule.expectMessage( "mockOne.oneArg(1)" );

        
        verifier.verify( mockOne, calls(1)).oneArg( 1 );

        
    }

// org.mockitousage.verification.VerificationInOrderWithCallsTest::shouldFailWhenMethodCallsOnDifferentMocksAreOutOfSequence
    public void shouldFailWhenMethodCallsOnDifferentMocksAreOutOfSequence(){
        
        mockOne.voidMethod();
        mockTwo.voidMethod();

        InOrder verifier = inOrder( mockOne, mockTwo );
        verifier.verify( mockTwo, calls(1)).voidMethod();

        exceptionRule.expect( VerificationInOrderFailure.class );
        exceptionRule.expectMessage( "Verification in order failure" );
        exceptionRule.expectMessage( "Wanted but not invoked" );
        exceptionRule.expectMessage( "mockOne.voidMethod()" );

        
        verifier.verify( mockOne, calls(1)).voidMethod();

        
    }

// org.mockitousage.verification.VerificationInOrderWithCallsTest::shouldAllowSequentialCallsToCallsForSingleMethod
    public void shouldAllowSequentialCallsToCallsForSingleMethod(){
        
        mockOne.oneArg( 1 );
        mockOne.oneArg( 2 );
        mockOne.oneArg( 2 );
        mockOne.oneArg( 1 );

        InOrder verifier = inOrder( mockOne );

        
        verifier.verify( mockOne, calls(1)).oneArg( 1 );
        verifier.verify( mockOne, calls(2)).oneArg( 2 );
        verifier.verify( mockOne, calls(1)).oneArg( 1 );
        verifyNoMoreInteractions(mockOne);
        verifier.verifyNoMoreInteractions();

        
    }

// org.mockitousage.verification.VerificationInOrderWithCallsTest::shouldAllowSequentialCallsToCallsForDifferentMethods
    public void shouldAllowSequentialCallsToCallsForDifferentMethods(){
        
        mockOne.oneArg( 1 );
        mockOne.voidMethod();
        mockOne.voidMethod();
        mockOne.oneArg( 1 );

        InOrder verifier = inOrder( mockOne );

        
        verifier.verify( mockOne, calls(1)).oneArg( 1 );
        verifier.verify( mockOne, calls(2)).voidMethod();
        verifier.verify( mockOne, calls(1)).oneArg(1);
        verifyNoMoreInteractions(mockOne);
        verifier.verifyNoMoreInteractions();

        
    }

// org.mockitousage.verification.VerificationInOrderWithCallsTest::shouldAllowSequentialCallsToCallsForMethodsOnDifferentMocks
    public void shouldAllowSequentialCallsToCallsForMethodsOnDifferentMocks(){
        
        mockOne.voidMethod();
        mockTwo.voidMethod();
        mockTwo.voidMethod();
        mockOne.voidMethod();

        InOrder verifier = inOrder( mockOne, mockTwo );

        
        verifier.verify( mockOne, calls(1)).voidMethod();
        verifier.verify( mockTwo, calls(2)).voidMethod();
        verifier.verify( mockOne, calls(1)).voidMethod();
        verifyNoMoreInteractions(mockOne);
        verifyNoMoreInteractions(mockTwo);
        verifier.verifyNoMoreInteractions();

        
    }

// org.mockitousage.verification.VerificationInOrderWithCallsTest::shouldAllowFewerCallsForSingleMethod
    public void shouldAllowFewerCallsForSingleMethod(){
        
        mockOne.oneArg( 1 );
        mockOne.oneArg( 2 );
        mockOne.oneArg( 2 );
        mockOne.oneArg( 1 );
        mockOne.oneArg( 2 );

        InOrder verifier = inOrder( mockOne );

        
        verifier.verify( mockOne, calls(1)).oneArg( 1 );
        verifier.verify( mockOne, calls(1)).oneArg( 2 );
        verifier.verify( mockOne, calls(1)).oneArg( 1 );
        verifier.verify( mockOne, calls(1)).oneArg( 2 );

        
    }

// org.mockitousage.verification.VerificationInOrderWithCallsTest::shouldNotVerifySkippedCallsWhenFewerCallsForSingleMethod
    public void shouldNotVerifySkippedCallsWhenFewerCallsForSingleMethod(){
        
        mockOne.oneArg( 1 );
        mockOne.oneArg( 2 );
        mockOne.oneArg( 2 );
        mockOne.oneArg( 1 );

        InOrder verifier = inOrder( mockOne );
        verifier.verify( mockOne, calls(1)).oneArg( 1 );
        verifier.verify( mockOne, calls(1)).oneArg( 2 );
        verifier.verify( mockOne, calls(1)).oneArg( 1 );
        
        exceptionRule.expect( NoInteractionsWanted.class );

        
        verifyNoMoreInteractions( mockOne );

        
    }

// org.mockitousage.verification.VerificationInOrderWithCallsTest::shouldNotVerifySkippedCallsInInOrderWhenFewerCallsForSingleMethod
    public void shouldNotVerifySkippedCallsInInOrderWhenFewerCallsForSingleMethod(){
        
        mockOne.oneArg( 1 );
        mockOne.oneArg( 2 );
        mockOne.oneArg( 2 );

        InOrder verifier = inOrder( mockOne );
        verifier.verify( mockOne, calls(1)).oneArg( 1 );
        verifier.verify( mockOne, calls(1)).oneArg( 2 );

        exceptionRule.expect( VerificationInOrderFailure.class );
        exceptionRule.expectMessage( "No interactions wanted here" );

        
        verifier.verifyNoMoreInteractions();

        
    }

// org.mockitousage.verification.VerificationInOrderWithCallsTest::shouldAllowFewerCallsForDifferentMethods
    public void shouldAllowFewerCallsForDifferentMethods(){
        
        mockOne.oneArg( 1 );
        mockOne.voidMethod();
        mockOne.voidMethod();
        mockOne.oneArg( 1 );
        mockOne.voidMethod();

        InOrder verifier = inOrder( mockOne );

        
        verifier.verify( mockOne, calls(1)).oneArg( 1 );
        verifier.verify( mockOne, calls(1)).voidMethod();
        verifier.verify( mockOne, calls(1)).oneArg( 1 );
        verifier.verify( mockOne, calls(1)).voidMethod();

        
    }

// org.mockitousage.verification.VerificationInOrderWithCallsTest::shouldNotVerifySkippedCallsWhenFewerCallsForDifferentMethods
    public void shouldNotVerifySkippedCallsWhenFewerCallsForDifferentMethods(){
        
        mockOne.oneArg( 1 );
        mockOne.voidMethod();
        mockOne.voidMethod();
        mockOne.oneArg( 1 );

        InOrder verifier = inOrder( mockOne );
        verifier.verify( mockOne, calls(1)).oneArg( 1 );
        verifier.verify( mockOne, calls(1)).voidMethod();
        verifier.verify( mockOne, calls(1)).oneArg( 1 );

        exceptionRule.expect( NoInteractionsWanted.class );

        
        verifyNoMoreInteractions( mockOne );

        
    }

// org.mockitousage.verification.VerificationInOrderWithCallsTest::shouldNotVerifySkippedCallsInInOrderWhenFewerCallsForDifferentMethods
    public void shouldNotVerifySkippedCallsInInOrderWhenFewerCallsForDifferentMethods(){
        
        mockOne.oneArg( 1 );
        mockOne.voidMethod();
        mockOne.voidMethod();

        InOrder verifier = inOrder( mockOne );
        verifier.verify( mockOne, calls(1)).oneArg( 1 );
        verifier.verify( mockOne, calls(1)).voidMethod();

        exceptionRule.expect( VerificationInOrderFailure.class );
        exceptionRule.expectMessage( "No interactions wanted here" );

        
        verifier.verifyNoMoreInteractions();

        
    }

// org.mockitousage.verification.VerificationInOrderWithCallsTest::shouldAllowFewerCallsForMethodsOnDifferentMocks
    public void shouldAllowFewerCallsForMethodsOnDifferentMocks(){
        
        mockOne.voidMethod();
        mockTwo.voidMethod();
        mockTwo.voidMethod();
        mockOne.voidMethod();
        mockTwo.voidMethod();

        InOrder verifier = inOrder( mockOne, mockTwo );

        
        verifier.verify( mockOne, calls(1)).voidMethod();
        verifier.verify( mockTwo, calls(1)).voidMethod();
        verifier.verify( mockOne, calls(1)).voidMethod();
        verifier.verify( mockTwo, calls(1)).voidMethod();

        
    }

// org.mockitousage.verification.VerificationInOrderWithCallsTest::shouldNotVerifySkippedCallsWhenFewerCallsForMethodsOnDifferentMocks
    public void shouldNotVerifySkippedCallsWhenFewerCallsForMethodsOnDifferentMocks(){
        
        mockOne.voidMethod();
        mockTwo.voidMethod();
        mockTwo.voidMethod();
        mockOne.voidMethod();

        InOrder verifier = inOrder( mockOne, mockTwo );
        verifier.verify( mockOne, calls(1)).voidMethod();
        verifier.verify( mockTwo, calls(1)).voidMethod();
        verifier.verify( mockOne, calls(1)).voidMethod();

        exceptionRule.expect(NoInteractionsWanted.class);

        
        verifyNoMoreInteractions( mockTwo );

        
    }

// org.mockitousage.verification.VerificationInOrderWithCallsTest::shouldNotVerifySkippedCallsInInOrderWhenFewerCallsForMethodsOnDifferentMocks
    public void shouldNotVerifySkippedCallsInInOrderWhenFewerCallsForMethodsOnDifferentMocks(){
        
        mockOne.voidMethod();
        mockTwo.voidMethod();
        mockTwo.voidMethod();

        InOrder verifier = inOrder( mockOne, mockTwo );
        verifier.verify( mockOne, calls(1)).voidMethod();
        verifier.verify( mockTwo, calls(1)).voidMethod();

        exceptionRule.expect( VerificationInOrderFailure.class );
        exceptionRule.expectMessage( "No interactions wanted here" );

        
        verifier.verifyNoMoreInteractions();

        
    }

// org.mockitousage.verification.VerificationInOrderWithCallsTest::shouldVerifyWithCallsAfterUseOfTimes
    public void shouldVerifyWithCallsAfterUseOfTimes(){
        
        mockOne.oneArg( 1 );
        mockOne.oneArg( 2 );
        mockOne.oneArg( 2 );
        mockOne.oneArg( 1 );

        InOrder verifier = inOrder( mockOne );

        
        verifier.verify( mockOne, times(1)).oneArg( 1 );
        verifier.verify( mockOne, calls(2)).oneArg( 2 );
        verifier.verify( mockOne, calls(1)).oneArg( 1 );

        
    }

// org.mockitousage.verification.VerificationInOrderWithCallsTest::shouldVerifyWithCallsAfterUseOfAtLeast
    public void shouldVerifyWithCallsAfterUseOfAtLeast(){
        
        mockOne.oneArg( 1 );
        mockOne.oneArg( 2 );
        mockOne.oneArg( 2 );

        InOrder verifier = inOrder( mockOne );

        
        verifier.verify( mockOne, atLeast(1)).oneArg( 1 );
        verifier.verify( mockOne, calls(2)).oneArg( 2 );

        
    }

// org.mockitousage.verification.VerificationInOrderWithCallsTest::shouldVerifyWithTimesAfterUseOfCalls
    public void shouldVerifyWithTimesAfterUseOfCalls(){
        
        mockOne.oneArg( 1 );
        mockOne.oneArg( 2 );
        mockOne.oneArg( 2 );
        mockOne.oneArg( 1 );

        InOrder verifier = inOrder( mockOne );

        
        verifier.verify( mockOne, calls(1)).oneArg( 1 );
        verifier.verify( mockOne, times(2)).oneArg( 2 );
        verifier.verify( mockOne, times(1)).oneArg( 1 );

        
    }

// org.mockitousage.verification.VerificationInOrderWithCallsTest::shouldVerifyWithAtLeastAfterUseOfCalls
    public void shouldVerifyWithAtLeastAfterUseOfCalls(){
        
        mockOne.oneArg( 1 );
        mockOne.oneArg( 2 );
        mockOne.oneArg( 2 );
        mockOne.oneArg( 1 );

        InOrder verifier = inOrder( mockOne );

        
        verifier.verify( mockOne, calls(1)).oneArg( 1 );
        verifier.verify( mockOne, atLeast(1)).oneArg( 2 );
        verifier.verify( mockOne, atLeast(1)).oneArg( 1 );

        
    }

// org.mockitousage.verification.VerificationInOrderWithCallsTest::shouldVerifyWithTimesAfterCallsInSameChunk
    public void shouldVerifyWithTimesAfterCallsInSameChunk(){
        
        mockOne.oneArg( 1 );
        mockOne.oneArg( 1 );
        mockOne.oneArg( 1 );

        InOrder verifier = inOrder( mockOne );

        
        verifier.verify( mockOne, calls(1)).oneArg( 1 );
        verifier.verify( mockOne, times(2)).oneArg( 1 );
        verifier.verifyNoMoreInteractions();

        
    }

// org.mockitousage.verification.VerificationInOrderWithCallsTest::shouldFailToCreateCallsWithZeroArgument
    public void shouldFailToCreateCallsWithZeroArgument(){
        
        InOrder verifier = inOrder( mockOne );
        exceptionRule.expect( MockitoException.class );
        exceptionRule.expectMessage( "Negative and zero values are not allowed here" );

        
        verifier.verify( mockOne, calls(0)).voidMethod();

        
    }

// org.mockitousage.verification.VerificationInOrderWithCallsTest::shouldFailToCreateCallsWithNegativeArgument
    public void shouldFailToCreateCallsWithNegativeArgument(){
        
        InOrder verifier = inOrder( mockOne );
        exceptionRule.expect( MockitoException.class );
        exceptionRule.expectMessage( "Negative and zero values are not allowed here" );

        
        verifier.verify( mockOne, calls(-1)).voidMethod();

        
    }

// org.mockitousage.verification.VerificationInOrderWithCallsTest::shouldFailToCreateCallsForNonInOrderVerification
    public void shouldFailToCreateCallsForNonInOrderVerification(){
        
        mockOne.voidMethod();
        exceptionRule.expect( MockitoException.class );
        exceptionRule.expectMessage( "calls is only intended to work with InOrder" );

        
        verify( mockOne, calls(1)).voidMethod();

        
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

// org.mockitousage.verification.VerificationWithTimeoutTest::shouldVerifyWithTimeout
    public void shouldVerifyWithTimeout() {}

// org.mockitousage.verification.VerificationWithTimeoutTest::shouldFailVerificationWithTimeout
    public void shouldFailVerificationWithTimeout() {}

// org.mockitousage.verification.VerificationWithTimeoutTest::shouldAllowMixingOtherModesWithTimeout
    public void shouldAllowMixingOtherModesWithTimeout() {}

// org.mockitousage.verification.VerificationWithTimeoutTest::shouldAllowMixingOtherModesWithTimeoutAndFail
    public void shouldAllowMixingOtherModesWithTimeoutAndFail() {}

// org.mockitousage.verification.VerificationWithTimeoutTest::shouldAllowMixingOnlyWithTimeout
    public void shouldAllowMixingOnlyWithTimeout() {}

// org.mockitousage.verification.VerificationWithTimeoutTest::shouldAllowMixingOnlyWithTimeoutAndFail
    public void shouldAllowMixingOnlyWithTimeoutAndFail() {}

// org.mockitousage.verification.VerificationWithTimeoutTest::shouldAllowTimeoutVerificationInOrder
    public void shouldAllowTimeoutVerificationInOrder() throws Exception {
        
        Thread t1 = waitAndExerciseMock(20);
        
        
        t1.start();
        mock.add("foo");
        
        
        InOrder inOrder = inOrder(mock);
        inOrder.verify(mock).add(anyString());
        inOrder.verify(mock, never()).clear();
        inOrder.verify(mock, timeout(40)).clear();
    }

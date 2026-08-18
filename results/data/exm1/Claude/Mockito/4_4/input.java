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
        verifyNoMoreInteractions((Object[])null);
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
            
            verify(boo).withLongAndInt(eq(100), any(Integer.class));
            fail();
        } catch (ArgumentsAreDifferent e) {
            
            assertContains("withLongAndInt((Long) 100, 200)", e.getMessage());
            assertContains("withLongAndInt((Integer) 100, <any>", e.getMessage());
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

// org.mockitousage.verification.VerificationWithTimeoutTest::canIgnoreInvocationsWithJunit
    public void canIgnoreInvocationsWithJunit() {
        
        Thread t1 = new Thread() {
            @Override
            public void run() {
                mock.add("0");
                mock.add("1");
                VerificationWithTimeoutTest.this.sleep(100);
                mock.add("2");
            }
        };

        
        t1.start();

        
        verify(mock, timeout(200)).add("1");
        verify(mock, timeout(200)).add("2");
    }

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

// org.mockitoutil.CustomAssertionsTest::shouldKnowWhenStringContainsIgnoringCase
    public void shouldKnowWhenStringContainsIgnoringCase() throws Exception {
        assertContainsIgnoringCase("foo", "foo");
        assertContainsIgnoringCase("fOo", "foo");
        assertContainsIgnoringCase("FoO", "foo");
        assertContainsIgnoringCase("foo", "a foo :)");
        assertContainsIgnoringCase("fOo", "a foo :)");
        assertContainsIgnoringCase("FoO", "a foo :)");
        assertContainsIgnoringCase("", "a foo :)");
        assertContainsIgnoringCase("", "");
    }

// org.mockitoutil.CustomAssertionsTest::shouldKnowWhenStringDoesNotContainIgnoringCase
    public void shouldKnowWhenStringDoesNotContainIgnoringCase() throws Exception {
        assertContainsIgnoringCase("fooo", "foo");
    }

// org.mockitoutil.CustomAssertionsTest::shouldKnowWhenStringDoesNotContainIgnoringCase2
    public void shouldKnowWhenStringDoesNotContainIgnoringCase2() throws Exception {
        assertContainsIgnoringCase("fOo", "f oo");
    }

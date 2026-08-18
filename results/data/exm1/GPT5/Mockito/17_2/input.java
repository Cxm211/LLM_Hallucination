// buggy code
    public MockSettings serializable() {
        return this.extraInterfaces(java.io.Serializable.class);
    }

    public boolean isSerializable() {
        return extraInterfaces != null && java.util.Arrays.asList(extraInterfaces).contains(java.io.Serializable.class);
    }

    public <T> T createMock(Class<T> classToMock, MockSettingsImpl settings) {
        creationValidator.validateType(classToMock);
        creationValidator.validateExtraInterfaces(classToMock, settings.getExtraInterfaces());
        creationValidator.validateMockedType(classToMock, settings.getSpiedInstance());

        settings.initiateMockName(classToMock);

        MockHandler<T> mockHandler = new MockHandler<T>(settings);
        MethodInterceptorFilter filter = new MethodInterceptorFilter(mockHandler, settings);
        Class<?>[] interfaces = settings.getExtraInterfaces();

        Class<?>[] ancillaryTypes;
            ancillaryTypes = interfaces == null ? new Class<?>[0] : interfaces;

        Object spiedInstance = settings.getSpiedInstance();
        
        T mock = ClassImposterizer.INSTANCE.imposterise(filter, classToMock, ancillaryTypes);
        
        if (spiedInstance != null) {
            new LenientCopyTool().copyToMock(spiedInstance, mock);
        }
        
        return mock;
    }

// relevant test
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

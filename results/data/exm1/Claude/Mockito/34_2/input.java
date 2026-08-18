// buggy code
    public void captureArgumentsFrom(Invocation i) {
        int k = 0;
        for (Matcher m : matchers) {
            if (m instanceof CapturesArguments) {
                ((CapturesArguments) m).captureFrom(i.getArguments()[k]);
            }
            k++;
        }
    }

// relevant test
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

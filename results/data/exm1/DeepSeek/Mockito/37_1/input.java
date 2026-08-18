// buggy code
    public void validate(Answer<?> answer, Invocation invocation) {
        if (answer instanceof ThrowsException) {
            validateException((ThrowsException) answer, invocation);
        }
        
        if (answer instanceof Returns) {
            validateReturnValue((Returns) answer, invocation);
        }
        
        if (answer instanceof DoesNothing) {
            validateDoNothing((DoesNothing) answer, invocation);
        }
        
    }

// relevant test
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

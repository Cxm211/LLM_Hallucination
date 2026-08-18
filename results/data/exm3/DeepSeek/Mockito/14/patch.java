    public Object handle(Invocation invocation) throws Throwable {
        if (invocationContainerImpl.hasAnswersForStubbing()) {
            // stubbing voids with stubVoid() or doAnswer() style
            InvocationMatcher invocationMatcher = matchersBinder.bindMatchers(mockingProgress
                            .getArgumentMatcherStorage(), invocation);
            invocationContainerImpl.setMethodForStubbing(invocationMatcher);
            return null;
        }

        // Check if there is a verification started and if it's for this mock
        Object verificationMock = mockingProgress.getVerificationMock();
        VerificationMode verificationMode = null;
        if (verificationMock != null && verificationMock == invocation.getMock()) {
            verificationMode = mockingProgress.pullVerificationMode();
            // Also clear the verification mock since we are consuming it
            mockingProgress.clearVerificationMock();
        }

        InvocationMatcher invocationMatcher = matchersBinder.bindMatchers(mockingProgress.getArgumentMatcherStorage(),
                        invocation);

        mockingProgress.validateState();

        //if verificationMode is not null then someone is doing verify()        
        if (verificationMode != null) {
            //We need to check if verification was started on the correct mock 
            // - see VerifyingWithAnExtraCallToADifferentMockTest
            // Already checked above.
                VerificationDataImpl data = new VerificationDataImpl(invocationContainerImpl.getInvocations(), invocationMatcher);            
                verificationMode.verify(data);
                return null;
        }
        
        invocationContainerImpl.setInvocationForPotentialStubbing(invocationMatcher);
        OngoingStubbingImpl<T> ongoingStubbing = new OngoingStubbingImpl<T>(invocationContainerImpl);
        mockingProgress.reportOngoingStubbing(ongoingStubbing);

        StubbedInvocationMatcher stubbedInvocation = invocationContainerImpl.findAnswerFor(invocation);

        if (stubbedInvocation != null) {
            stubbedInvocation.captureArgumentsFrom(invocation);
            return stubbedInvocation.answer(invocation);
        } else {
            Object ret = mockSettings.getDefaultAnswer().answer(invocation);

            // redo setting invocation for potential stubbing in case of partial
            // mocks / spies.
            // Without it, the real method inside 'when' might have delegated
            // to other self method and overwrite the intended stubbed method
            // with a different one.
            invocationContainerImpl.resetInvocationForPotentialStubbing(invocationMatcher);
            return ret;
        }
    }
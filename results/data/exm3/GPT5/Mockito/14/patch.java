public Object handle(Invocation invocation) throws Throwable {
        if (invocationContainerImpl.hasAnswersForStubbing()) {
            // stubbing voids with stubVoid() or doAnswer() style
            InvocationMatcher invocationMatcher = matchersBinder.bindMatchers(mockingProgress
                            .getArgumentMatcherStorage(), invocation);
            invocationContainerImpl.setMethodForStubbing(invocationMatcher);
            return null;
        }
        VerificationMode verificationMode = mockingProgress.pullVerificationMode();

        InvocationMatcher invocationMatcher = matchersBinder.bindMatchers(mockingProgress.getArgumentMatcherStorage(),
                        invocation);

        mockingProgress.validateState();

        //if verificationMode is not null then someone is doing verify()        
        if (verificationMode != null) {
            // Check if verification was started on the correct mock. If not, push the verification mode back
            // and proceed with normal invocation handling (other mock call in the same line as verify).
            try {
                java.lang.reflect.Field f = verificationMode.getClass().getDeclaredField("targetMock");
                f.setAccessible(true);
                Object targetMock = f.get(verificationMode);
                // If targetMock is present and different than current invocation mock, don't verify now.
                Object currentMock = invocation.getMock();
                if (targetMock != null && targetMock != currentMock) {
                    // put the verification mode back so that the next correct mock call will consume it
                    mockingProgress.verificationStarted(verificationMode);
                } else {
                    VerificationDataImpl data = new VerificationDataImpl(invocationContainerImpl.getInvocations(), invocationMatcher);
                    verificationMode.verify(data);
                    return null;
                }
            } catch (NoSuchFieldException ignore) {
                // Fallback to original behavior if the wrapper field is not present
                VerificationDataImpl data = new VerificationDataImpl(invocationContainerImpl.getInvocations(), invocationMatcher);
                verificationMode.verify(data);
                return null;
            }
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
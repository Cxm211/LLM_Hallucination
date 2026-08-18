    public void captureArgumentsFrom(Invocation invocation) {
        if (invocation.getMethod().isVarArgs()) {
            int nonVarargCount = invocation.getRawArguments().length - 1;
            Object varargArray = invocation.getRawArguments()[nonVarargCount];
            int varargLength = 0;
            if (varargArray != null) {
                varargLength = java.lang.reflect.Array.getLength(varargArray);
            }
            int totalMatchers = matchers.size();
            
            // Capture non‑vararg arguments
            for (int i = 0; i < nonVarargCount && i < totalMatchers; i++) {
                Matcher m = matchers.get(i);
                if (m instanceof CapturesArguments) {
                    ((CapturesArguments) m).captureFrom(invocation.getArgumentAt(i, Object.class));
                }
            }
            
            // Determine if there is a single matcher for the vararg parameter or multiple
            if (totalMatchers == nonVarargCount + 1) {
                // Single matcher for the vararg parameter: capture each vararg element with that matcher
                Matcher varargMatcher = matchers.get(nonVarargCount);
                if (varargMatcher instanceof CapturesArguments) {
                    for (int i = 0; i < varargLength; i++) {
                        ((CapturesArguments) varargMatcher).captureFrom(java.lang.reflect.Array.get(varargArray, i));
                    }
                }
            } else if (totalMatchers > nonVarargCount) {
                // Multiple matchers for vararg elements: each matcher corresponds to one vararg element
                int varargMatchersCount = totalMatchers - nonVarargCount;
                for (int i = 0; i < varargMatchersCount; i++) {
                    if (i < varargLength) {
                        Matcher m = matchers.get(nonVarargCount + i);
                        if (m instanceof CapturesArguments) {
                            ((CapturesArguments) m).captureFrom(java.lang.reflect.Array.get(varargArray, i));
                        }
                    }
                }
            }
        } else {
            // Non‑varargs method
            for (int position = 0; position < matchers.size(); position++) {
                Matcher m = matchers.get(position);
                if (m instanceof CapturesArguments) {
                    ((CapturesArguments) m).captureFrom(invocation.getArgumentAt(position, Object.class));
                }
            }
        }
    }
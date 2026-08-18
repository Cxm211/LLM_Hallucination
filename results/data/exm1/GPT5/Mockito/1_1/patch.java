public void captureArgumentsFrom(Invocation invocation) {
        if (invocation.getMethod().isVarArgs()) {
            int indexOfVararg = invocation.getRawArguments().length - 1;
            for (int position = 0; position < matchers.size(); position++) {
                Matcher m = matchers.get(position);
                if (m instanceof CapturesArguments && invocation.getRawArguments().length > position) {
                    if (isVariableArgument(invocation, position) && isVarargMatcher(m)) {
                        Object array = invocation.getRawArguments()[position];
                        for (int i = 0; i < java.lang.reflect.Array.getLength(array); i++) {
                            ((CapturesArguments) m).captureFrom(java.lang.reflect.Array.get(array, i));
                        }
                        return;
                    } else {
                        ((CapturesArguments) m).captureFrom(invocation.getRawArguments()[position]);
                    }
                }
            }
        } else {
            for (int position = 0; position < matchers.size(); position++) {
                Matcher m = matchers.get(position);
                if (m instanceof CapturesArguments && invocation.getRawArguments().length > position) {
                    ((CapturesArguments) m).captureFrom(invocation.getRawArguments()[position]);
                }
            }
        }
    }
public void captureArgumentsFrom(Invocation invocation) {
    if (invocation.getMethod().isVarArgs()) {
        int indexOfVararg = invocation.getRawArguments().length - 1;
        for (int position = 0; position < indexOfVararg; position++) {
            Matcher m = matchers.get(position);
            if (m instanceof CapturesArguments) {
                ((CapturesArguments) m).captureFrom(invocation.getArgumentAt(position, Object.class));
            }
        }
        int varargMatchersCount = matchers.size() - indexOfVararg;
        Object varargArray = invocation.getRawArguments()[indexOfVararg];
        if (varargMatchersCount == 1) {
            Matcher m = matchers.get(indexOfVararg);
            if (m instanceof CapturesArguments) {
                ((CapturesArguments) m).captureFrom(varargArray);
            }
        } else {
            for (int i = 0; i < varargMatchersCount; i++) {
                Matcher m = matchers.get(indexOfVararg + i);
                if (m instanceof CapturesArguments) {
                    ((CapturesArguments) m).captureFrom(java.lang.reflect.Array.get(varargArray, i));
                }
            }
        }
    } else {
        for (int position = 0; position < matchers.size(); position++) {
            Matcher m = matchers.get(position);
            if (m instanceof CapturesArguments) {
                ((CapturesArguments) m).captureFrom(invocation.getArgumentAt(position, Object.class));
            }
        }
    }
}
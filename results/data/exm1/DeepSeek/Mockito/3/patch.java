public void captureArgumentsFrom(Invocation invocation) {
    if (invocation.getMethod().isVarArgs()) {
        int indexOfVararg = invocation.getRawArguments().length - 1;
        for (int position = 0; position < indexOfVararg; position++) {
            Matcher m = matchers.get(position);
            if (m instanceof CapturesArguments) {
                ((CapturesArguments) m).captureFrom(invocation.getArgumentAt(position, Object.class));
            }
        }
        Object[] varargs = (Object[]) invocation.getRawArguments()[indexOfVararg];
        for (int i = 0; i < varargs.length; i++) {
            int matcherPosition = Math.min(indexOfVararg + i, matchers.size() - 1);
            Matcher m = matchers.get(matcherPosition);
            if (m instanceof CapturesArguments) {
                ((CapturesArguments) m).captureFrom(varargs[i]);
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
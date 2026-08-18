public void captureArgumentsFrom(Invocation invocation) {
    for (int position = 0; position < matchers.size(); position++) {
        Matcher m = matchers.get(position);
        if (m instanceof CapturesArguments && position < invocation.getRawArguments().length) {
            Object argument = invocation.getRawArguments()[position];
            if (invocation.getMethod().isVarArgs() && position == invocation.getRawArguments().length - 1 && argument != null && argument.getClass().isArray()) {
                for (int i = 0; i < java.lang.reflect.Array.getLength(argument); i++) {
                    ((CapturesArguments) m).captureFrom(java.lang.reflect.Array.get(argument, i));
                }
                return;
            } else {
                ((CapturesArguments) m).captureFrom(argument);
            }
        }
    }
}
public void captureArgumentsFrom(Invocation invocation) {
    if (invocation.getMethod().isVarArgs()) {
        int indexOfVararg = invocation.getRawArguments().length - 1;
        for (int position = 0; position < matchers.size(); position++) {
            Matcher m = matchers.get(position);
            if (m instanceof CapturesArguments) {
                if (position == indexOfVararg) {
                    Object array = invocation.getRawArguments()[position];
                    if (array != null && array.getClass().isArray()) {
                        for (int i = 0; i < java.lang.reflect.Array.getLength(array); i++) {
                            ((CapturesArguments) m).captureFrom(java.lang.reflect.Array.get(array, i));
                        }
                    }
                } else {
                    ((CapturesArguments) m).captureFrom(invocation.getArgumentAt(position, Object.class));
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
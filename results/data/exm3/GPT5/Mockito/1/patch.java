public void captureArgumentsFrom(Invocation invocation) {
        if (invocation.getMethod().isVarArgs()) {
            Object[] rawArgs = invocation.getRawArguments();
            int lastIndex = rawArgs.length - 1;

            for (int position = 0; position < matchers.size(); position++) {
                if (position >= rawArgs.length) {
                    break;
                }
                Matcher m = matchers.get(position);
                if (m instanceof CapturesArguments) {
                    if (position == lastIndex && rawArgs[position] != null && rawArgs[position].getClass().isArray()) {
                        Object array = rawArgs[position];
                        int length = java.lang.reflect.Array.getLength(array);
                        for (int i = 0; i < length; i++) {
                            ((CapturesArguments) m).captureFrom(java.lang.reflect.Array.get(array, i));
                        }
                    } else {
                        ((CapturesArguments) m).captureFrom(rawArgs[position]);
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
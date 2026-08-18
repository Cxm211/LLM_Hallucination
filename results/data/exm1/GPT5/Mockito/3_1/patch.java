public void captureArgumentsFrom(Invocation invocation) {
        if (invocation.getMethod().isVarArgs()) {
            int indexOfVararg = invocation.getRawArguments().length - 1;
            for (int position = 0; position < indexOfVararg; position++) {
                Matcher m = matchers.get(position);
                if (m instanceof CapturesArguments) {
                    ((CapturesArguments) m).captureFrom(invocation.getArgumentAt(position, Object.class));
                }
            }
            // handle varargs
            Object varargsArray = invocation.getRawArguments()[indexOfVararg];
            int varargsLength = varargsArray == null ? 0 : java.lang.reflect.Array.getLength(varargsArray);
            int varargMatchersCount = matchers.size() - indexOfVararg;
            if (varargMatchersCount <= 0) {
                return;
            }
            if (varargMatchersCount == 1) {
                Matcher m = matchers.get(indexOfVararg);
                if (m instanceof CapturesArguments) {
                    for (int i = 0; i < varargsLength; i++) {
                        Object value = java.lang.reflect.Array.get(varargsArray, i);
                        ((CapturesArguments) m).captureFrom(value);
                    }
                }
            } else {
                for (int i = 0; i < varargMatchersCount; i++) {
                    Matcher m = matchers.get(indexOfVararg + i);
                    if (m instanceof CapturesArguments) {
                        Object value = i < varargsLength ? java.lang.reflect.Array.get(varargsArray, i) : null;
                        ((CapturesArguments) m).captureFrom(value);
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
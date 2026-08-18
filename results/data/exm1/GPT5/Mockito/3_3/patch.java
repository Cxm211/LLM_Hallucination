public void captureArgumentsFrom(Invocation invocation) {
        if (invocation.getMethod().isVarArgs()) {
            int indexOfVararg = invocation.getRawArguments().length - 1;
            for (int position = 0; position < indexOfVararg; position++) {
                Matcher m = matchers.get(position);
                if (m instanceof CapturesArguments) {
                    ((CapturesArguments) m).captureFrom(invocation.getArgumentAt(position, Object.class));
                }
            }
            Object varargsArray = invocation.getRawArguments()[indexOfVararg];
            int varargLength = varargsArray == null ? 0 : java.lang.reflect.Array.getLength(varargsArray);
            for (int position = indexOfVararg; position < matchers.size(); position++) {
                Matcher m = matchers.get(position);
                if (m instanceof CapturesArguments) {
                    int varargIndex = position - indexOfVararg;
                    Object value = varargIndex < varargLength ? java.lang.reflect.Array.get(varargsArray, varargIndex) : null;
                    ((CapturesArguments) m).captureFrom(value);
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
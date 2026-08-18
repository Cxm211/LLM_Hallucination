public void captureArgumentsFrom(Invocation invocation) {
        if (invocation.getMethod().isVarArgs()) {
            int indexOfVararg = invocation.getRawArguments().length - 1;
            // capture non-vararg (fixed) arguments
            for (int position = 0; position < indexOfVararg; position++) {
                Matcher m = matchers.get(position);
                if (m instanceof CapturesArguments) {
                    ((CapturesArguments) m).captureFrom(invocation.getArgumentAt(position, Object.class));
                }
            }
            // capture vararg arguments
            Object varargsArray = invocation.getRawArguments()[indexOfVararg];
            int varargLength = varargsArray != null && varargsArray.getClass().isArray() ? java.lang.reflect.Array.getLength(varargsArray) : 0;

            if (matchers.size() == indexOfVararg + 1) {
                // single matcher for the whole vararg: capture each element
                Matcher m = matchers.get(indexOfVararg);
                if (m instanceof CapturesArguments) {
                    for (int i = 0; i < varargLength; i++) {
                        Object element = java.lang.reflect.Array.get(varargsArray, i);
                        ((CapturesArguments) m).captureFrom(element);
                    }
                }
            } else {
                // multiple matchers for individual vararg elements
                for (int position = indexOfVararg; position < matchers.size(); position++) {
                    int varargPos = position - indexOfVararg;
                    if (varargPos >= varargLength) {
                        // no corresponding vararg element
                        break;
                    }
                    Matcher m = matchers.get(position);
                    if (m instanceof CapturesArguments) {
                        Object element = java.lang.reflect.Array.get(varargsArray, varargPos);
                        ((CapturesArguments) m).captureFrom(element);
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
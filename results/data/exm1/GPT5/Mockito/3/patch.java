public void captureArgumentsFrom(Invocation invocation) {
        if (invocation.getMethod().isVarArgs()) {
            int indexOfVararg = invocation.getRawArguments().length - 1;
            for (int position = 0; position < indexOfVararg; position++) {
                Matcher m = matchers.get(position);
                if (m instanceof CapturesArguments) {
                    ((CapturesArguments) m).captureFrom(invocation.getArgumentAt(position, Object.class));
                }
            }
            // handle varargs part
            Object varargArray = invocation.getRawArguments()[indexOfVararg];
            int remainingMatchers = matchers.size() - indexOfVararg;
            if (varargArray != null && varargArray.getClass().isArray()) {
                int varargLength = java.lang.reflect.Array.getLength(varargArray);
                if (remainingMatchers == 1) {
                    // single matcher corresponds to the entire vararg; capture each element
                    Matcher m = matchers.get(indexOfVararg);
                    if (m instanceof CapturesArguments) {
                        for (int i = 0; i < varargLength; i++) {
                            ((CapturesArguments) m).captureFrom(java.lang.reflect.Array.get(varargArray, i));
                        }
                    }
                } else {
                    // multiple matchers correspond to individual vararg elements
                    for (int position = indexOfVararg; position < matchers.size(); position++) {
                        Matcher m = matchers.get(position);
                        int idx = position - indexOfVararg;
                        if (m instanceof CapturesArguments && idx < varargLength) {
                            ((CapturesArguments) m).captureFrom(java.lang.reflect.Array.get(varargArray, idx));
                        }
                    }
                }
            } else {
                // No vararg array (null or not an array). If there is a single matcher for vararg, nothing to capture.
                if (remainingMatchers > 1) {
                    for (int position = indexOfVararg; position < matchers.size(); position++) {
                        Matcher m = matchers.get(position);
                        if (m instanceof CapturesArguments) {
                            ((CapturesArguments) m).captureFrom(null);
                        }
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
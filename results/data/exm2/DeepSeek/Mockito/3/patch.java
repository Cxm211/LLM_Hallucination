    public void captureArgumentsFrom(Invocation invocation) {
        if (invocation.getMethod().isVarArgs()) {
            int indexOfVararg = invocation.getRawArguments().length - 1;
            for (int position = 0; position < indexOfVararg; position++) {
                Matcher m = matchers.get(position);
                if (m instanceof CapturesArguments) {
                    ((CapturesArguments) m).captureFrom(invocation.getArgumentAt(position, Object.class));
                }
            }
            Object varargArray = invocation.getRawArguments()[indexOfVararg];
            int varargLength = Array.getLength(varargArray);
            for (int i = 0; i < varargLength; i++) {
                int matcherIndex;
                if (matchers.size() - indexOfVararg == 1) {
                    matcherIndex = indexOfVararg;
                } else {
                    matcherIndex = indexOfVararg + i;
                }
                if (matcherIndex < matchers.size()) {
                    Matcher m = matchers.get(matcherIndex);
                    if (m instanceof CapturesArguments) {
                        Object element = Array.get(varargArray, i);
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
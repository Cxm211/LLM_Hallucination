public void captureArgumentsFrom(Invocation invocation) {
        if (invocation.getMethod().isVarArgs()) {
            Object[] varargs = invocation.getRawArguments();
            int varCount = varargs == null ? 0 : varargs.length;
            int msize = matchers.size();
            int nonVarargCount = Math.max(0, Math.min(msize - 1, msize - varCount));
            for (int position = 0; position < nonVarargCount; position++) {
                Matcher m = matchers.get(position);
                if (m instanceof CapturesArguments) {
                    ((CapturesArguments) m).captureFrom(invocation.getArgumentAt(position, Object.class));
                }
            }
            int remainingMatchers = msize - nonVarargCount;
            if (remainingMatchers == 1) {
                Matcher m = matchers.get(nonVarargCount);
                if (m instanceof CapturesArguments) {
                    for (int i = 0; i < varCount; i++) {
                        ((CapturesArguments) m).captureFrom(varargs[i]);
                    }
                }
            } else if (remainingMatchers > 1) {
                int limit = Math.min(varCount, remainingMatchers);
                for (int i = 0; i < limit; i++) {
                    Matcher m = matchers.get(nonVarargCount + i);
                    if (m instanceof CapturesArguments) {
                        ((CapturesArguments) m).captureFrom(varargs[i]);
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
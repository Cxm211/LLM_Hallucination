public void captureArgumentsFrom(Invocation i) {
        int k = 0;
        Object[] args = i.getArguments();
        int lastArgIndex = args.length - 1;
        for (Matcher m : matchers) {
            int idx = Math.min(k, lastArgIndex);
            if (m instanceof CapturesArguments) {
                ((CapturesArguments) m).captureFrom(args[idx]);
            }
            if (k < lastArgIndex) {
                k++;
            }
        }
    }
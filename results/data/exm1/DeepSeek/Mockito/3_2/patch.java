public void captureArgumentsFrom(Invocation invocation) {
    for (int position = 0; position < matchers.size(); position++) {
        Matcher m = matchers.get(position);
        if (m instanceof CapturesArguments) {
            ((CapturesArguments) m).captureFrom(invocation.getArgumentAt(position, Object.class));
        }
    }
}
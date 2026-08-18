public void validate(Answer<?> answer, Invocation invocation) {
    if (answer instanceof ThrowsException) {
        validateException((ThrowsException) answer, invocation);
    } else if (answer instanceof Returns) {
        validateReturnValue((Returns) answer, invocation);
    } else if (answer instanceof DoesNothing) {
        validateDoNothing((DoesNothing) answer, invocation);
    }
}
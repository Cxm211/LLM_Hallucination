public Object callRealMethod() throws Throwable {
    if (mock != null && mock.getClass().isInterface()) {
        throw new MockitoException("Cannot call real method on java interface. Interface does not have any implementation!\nInterface: " + mock.getClass().getSimpleName());
    }
    if (realMethod.getDeclaringClass().isInterface()) {
        throw new MockitoException("Cannot call real method on java interface. Interface does not have any implementation!\nInterface: " + realMethod.getDeclaringClass().getSimpleName());
    }
    return realMethod.invoke(mock, rawArguments);
}
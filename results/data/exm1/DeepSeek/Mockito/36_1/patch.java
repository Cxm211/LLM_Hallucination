public Object callRealMethod() throws Throwable {
    if (this.realMethod.getMethod().isAbstract()) {
        throw new MockitoException("Cannot call real method on abstract method.");
    }
    return this.realMethod.invoke(this.mock, this.rawArguments);
}
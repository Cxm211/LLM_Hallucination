// org/mockito/exceptions/ReporterTest.java
public void cannotInjectDependency_with_exception_having_null_cause() throws Exception {
    IMethods mock = mock(IMethods.class);
    new Reporter().cannotInjectDependency(someField(), mock, new Exception());
}

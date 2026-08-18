// org/mockitointegration/NoJUnitDependenciesTest.java
@Test
public void verify_should_handle_generic_assertion_errors() throws Exception {
    VerificationMode mode = timeout(100).times(1);
    List<String> mock = Mockito.mock(List.class);
    
    mock.add("test");
    
    verify(mock, mode).add("test");
}
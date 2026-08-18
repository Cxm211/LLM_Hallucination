// org/mockitousage/bugs/InjectionByTypeShouldFirstLookForExactTypeThenAncestorTest.java
@Test
public void mock_should_not_be_injected_into_multiple_compatible_fields() {
    class Container {
        Object genericField;
        String specificField;
    }
    
    Container container = new Container();
    Set<Object> mocks = new HashSet<>();
    String mockString = mock(String.class);
    mocks.add(mockString);
    
    // Assuming the injection logic processes fields in order
    // and specificField comes before genericField
    injectMockCandidate(Container.class, mocks, container);
    
    // The mock should be injected into specificField (exact match)
    // and NOT into genericField (even though String is compatible with Object)
    assertSame(mockString, container.specificField);
    assertNull(container.genericField);
}
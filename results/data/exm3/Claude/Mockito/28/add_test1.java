// org/mockitousage/bugs/InjectionByTypeShouldFirstLookForExactTypeThenAncestorTest.java
@Test
public void multiple_mocks_should_each_be_injected_once() {
    class MultiFieldContainer {
        String stringField;
        Integer integerField;
        Object objectField;
    }
    
    MultiFieldContainer container = new MultiFieldContainer();
    Set<Object> mocks = new HashSet<>();
    String mockString = mock(String.class);
    Integer mockInteger = mock(Integer.class);
    Object mockObject = mock(Object.class);
    mocks.add(mockString);
    mocks.add(mockInteger);
    mocks.add(mockObject);
    
    injectMockCandidate(MultiFieldContainer.class, mocks, container);
    
    // Each mock should be injected exactly once into its exact type match
    assertSame(mockString, container.stringField);
    assertSame(mockInteger, container.integerField);
    assertSame(mockObject, container.objectField);
}
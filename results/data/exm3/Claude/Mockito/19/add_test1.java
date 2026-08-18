// org/mockitousage/annotation/MockInjectionUsingSetterOrPropertyTest.java
@Test
public void shouldInjectWhenSingleMockMatchesFieldType() {
    class TestCase {
        @Mock private Candidate mockCandidate;
        @InjectMocks private UnderTest underTest;
        
        class UnderTest {
            private Candidate anyFieldName;
        }
    }
    TestCase tc = new TestCase();
    MockitoAnnotations.initMocks(tc);
    assertNotNull(tc.underTest.anyFieldName);
    assertSame(tc.mockCandidate, tc.underTest.anyFieldName);
}
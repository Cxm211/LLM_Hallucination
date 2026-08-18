// org/mockitousage/annotation/MockInjectionUsingSetterOrPropertyTest.java
@Test
public void shouldNotInjectWhenNoNameMatchAndMultipleCandidatesOfSameType() {
    class TestCase {
        @Mock private Candidate mockA;
        @Mock private Candidate mockB;
        @InjectMocks private UnderTest underTest;
        
        class UnderTest {
            private Candidate field1;
        }
    }
    TestCase tc = new TestCase();
    MockitoAnnotations.initMocks(tc);
    assertNull(tc.underTest.field1);
}
// org/mockitousage/annotation/MockInjectionUsingSetterOrPropertyTest.java
@Test
public void shouldInjectByNameWhenMultipleMocksAndOneMatchesName() {
    class TestCase {
        @Mock private Candidate specificName;
        @Mock private Candidate otherMock;
        @InjectMocks private UnderTest underTest;
        
        class UnderTest {
            private Candidate specificName;
        }
    }
    TestCase tc = new TestCase();
    MockitoAnnotations.initMocks(tc);
    assertNotNull(tc.underTest.specificName);
    assertSame(tc.specificName, tc.underTest.specificName);
}
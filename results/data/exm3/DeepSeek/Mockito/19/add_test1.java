// org/mockitousage/annotation/MockInjectionUsingSetterOrPropertyTest.java
@Test
	public void shouldNotThrowNPEWhenMockNameIsNull() {
		class Dependency {}
		class Target {
			private Dependency field;
		}
		class TestClass {
			@InjectMocks private Target target = new Target();
			@Mock private Dependency mock1;
			@Mock(name = "field") private Dependency mock2;
		}
		TestClass test = new TestClass();
		MockitoAnnotations.initMocks(test);
		assertSame(test.mock2, test.target.field);
	}

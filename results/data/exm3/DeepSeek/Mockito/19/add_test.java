// org/mockitousage/annotation/MockInjectionUsingSetterOrPropertyTest.java
@Test
	public void shouldNotInjectSingleMockWhenNameDoesNotMatch() {
		class Dependency {}
		class Target {
			private Dependency field;
		}
		class TestClass {
			@InjectMocks private Target target = new Target();
			@Mock(name = "otherName") private Dependency mock;
		}
		TestClass test = new TestClass();
		MockitoAnnotations.initMocks(test);
		assertNull(test.target.field);
	}

// org/mockitousage/matchers/AnyXMatchersAcceptNullsTest.java::shouldNotAcceptNullInAnyClassMatcher
@Test
public void shouldNotAcceptNullInAnyClassMatcher() {
    when(mock.oneArg(any(String.class))).thenReturn("ok");

    assertEquals("ok", mock.oneArg("x"));
    assertEquals(null, mock.oneArg((String) null));
}
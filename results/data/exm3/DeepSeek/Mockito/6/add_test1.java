// org/mockitousage/matchers/MatchersTest.java
@Test
    public void anyMatcherShouldNotAcceptNull() {
        when(mock.oneArg(any())).thenReturn("any");
        when(mock.oneArg(any(Object.class))).thenReturn("anyObject");
        
        assertEquals("any", mock.oneArg("something"));
        assertEquals("anyObject", mock.oneArg(new Integer(5)));
        
        assertEquals(null, mock.oneArg((Object) null));
        assertEquals(null, mock.oneArg((String) null));
    }

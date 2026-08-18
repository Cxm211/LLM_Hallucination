// org/mockitousage/matchers/AnyXMatchersAcceptNullsTest.java
@Test
    public void shouldNotAcceptWrongTypeInAllAnyPrimitiveWrapperMatchers() {
        when(mock.oneArg(anyInt())).thenReturn("int");
        when(mock.oneArg(anyChar())).thenReturn("char");
        when(mock.oneArg(anyShort())).thenReturn("short");
        when(mock.oneArg(anyByte())).thenReturn("byte");
        when(mock.oneArg(anyBoolean())).thenReturn("boolean");
        when(mock.oneArg(anyLong())).thenReturn("long");
        when(mock.oneArg(anyFloat())).thenReturn("float");
        when(mock.oneArg(anyDouble())).thenReturn("double");
        
        assertEquals("int", mock.oneArg(123));
        assertEquals("char", mock.oneArg('a'));
        assertEquals("short", mock.oneArg((short) 1));
        assertEquals("byte", mock.oneArg((byte) 1));
        assertEquals("boolean", mock.oneArg(true));
        assertEquals("long", mock.oneArg(123L));
        assertEquals("float", mock.oneArg(1.0f));
        assertEquals("double", mock.oneArg(1.0));
        
        assertEquals(null, mock.oneArg("string"));
        assertEquals(null, mock.oneArg(new Object()));
    }

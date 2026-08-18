// org/mockitousage/matchers/AnyXMatchersAcceptNullsTest.java
@Test
public void shouldNotAcceptNullInAnyPrimitiveWrapperMatchersWithVerify() {
    mock.forInteger(null);
    mock.forCharacter(null);
    mock.forShort(null);
    mock.forByte(null);
    mock.forBoolean(null);
    mock.forLong(null);
    mock.forFloat(null);
    mock.forDouble(null);
    
    try {
        verify(mock).forInteger(anyInt());
        fail("Should have thrown exception");
    } catch (AssertionError e) {
    }
    
    try {
        verify(mock).forCharacter(anyChar());
        fail("Should have thrown exception");
    } catch (AssertionError e) {
    }
    
    try {
        verify(mock).forShort(anyShort());
        fail("Should have thrown exception");
    } catch (AssertionError e) {
    }
    
    try {
        verify(mock).forByte(anyByte());
        fail("Should have thrown exception");
    } catch (AssertionError e) {
    }
    
    try {
        verify(mock).forBoolean(anyBoolean());
        fail("Should have thrown exception");
    } catch (AssertionError e) {
    }
    
    try {
        verify(mock).forLong(anyLong());
        fail("Should have thrown exception");
    } catch (AssertionError e) {
    }
    
    try {
        verify(mock).forFloat(anyFloat());
        fail("Should have thrown exception");
    } catch (AssertionError e) {
    }
    
    try {
        verify(mock).forDouble(anyDouble());
        fail("Should have thrown exception");
    } catch (AssertionError e) {
    }
}
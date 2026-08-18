// org/mockitousage/bugs/NPEWithCertainMatchersTest.java
@Test
public void shouldNotThrowNPEWhenBytePassedToSame() {
    mock.byteArgumentMethod((byte)5);
    
    verify(mock, never()).byteArgumentMethod(same(Byte.valueOf((byte)5)));
}
// org/mockitousage/bugs/NPEWithCertainMatchersTest.java
public void shouldNotThrowNPEWhenNullPassedToSameWithNoInteraction() {
        verify(mock).objectArgMethod(same(null));
    }

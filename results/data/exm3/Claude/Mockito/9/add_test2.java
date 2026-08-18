// org/mockitousage/constructor/CreatingMocksWithConstructorTest.java
@Test
public void abstractMethodWithConcreteMethod() {
    MixedAbstractClass obj = spy(MixedAbstractClass.class);
    assertNull(obj.abstractMethod());
    assertEquals("concrete", obj.concreteMethod());
}

static abstract class MixedAbstractClass {
    public abstract String abstractMethod();
    public String concreteMethod() {
        return "concrete";
    }
}
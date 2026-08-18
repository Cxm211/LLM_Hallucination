// org/mockitousage/constructor/CreatingMocksWithConstructorTest.java
@Test
public void abstractMethodWithPrimitiveReturnType() {
    AbstractPrimitiveThing thing = spy(AbstractPrimitiveThing.class);
    assertEquals(0, thing.getCount());
}

static abstract class AbstractPrimitiveThing {
    public abstract int getCount();
}
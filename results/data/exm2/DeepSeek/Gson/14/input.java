  public static WildcardType subtypeOf(Type bound) {
    Type[] upperBounds;
      upperBounds = new Type[] { bound };
    return new WildcardTypeImpl(upperBounds, EMPTY_TYPE_ARRAY);
  }

  public static WildcardType supertypeOf(Type bound) {
    Type[] lowerBounds;
      lowerBounds = new Type[] { bound };
    return new WildcardTypeImpl(new Type[] { Object.class }, lowerBounds);
  }

// trigger testcase
public void testDoubleSubtype() {
    assertEquals($Gson$Types.subtypeOf(Number.class),
            $Gson$Types.subtypeOf($Gson$Types.subtypeOf(Number.class)));
  }

public void testDoubleSupertype() {
    assertEquals($Gson$Types.supertypeOf(Number.class),
            $Gson$Types.supertypeOf($Gson$Types.supertypeOf(Number.class)));
  }

public void testIssue440WeakReference() throws Exception {
    TypeAdapter<WeakReference> adapter = new Gson().getAdapter(WeakReference.class);
    assertNotNull(adapter);
  }

public void testIssue603PrintStream() {
    TypeAdapter<PrintStream> adapter = new Gson().getAdapter(PrintStream.class);
    assertNotNull(adapter);
  }

public void testRecursiveResolveSimple() {
    TypeAdapter<Foo1> adapter = new Gson().getAdapter(Foo1.class);
    assertNotNull(adapter);
  }

public void testSubSupertype() {
    assertEquals($Gson$Types.subtypeOf(Object.class),
            $Gson$Types.subtypeOf($Gson$Types.supertypeOf(Number.class)));
  }

public void testSuperSubtype() {
    assertEquals($Gson$Types.subtypeOf(Object.class),
            $Gson$Types.supertypeOf($Gson$Types.subtypeOf(Number.class)));
  }

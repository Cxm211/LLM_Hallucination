// com/google/gson/functional/TypeVariableTest.java
public void testWildcardType() throws Exception {
    Gson gson = new Gson();
    class WithWildcard {
      List<? extends Number> list;
    }
    WithWildcard obj = new WithWildcard();
    obj.list = new ArrayList<Number>();
    obj.list.add(1);
    obj.list.add(2.5);
    String json = gson.toJson(obj);
    WithWildcard obj2 = gson.fromJson(json, WithWildcard.class);
    assertEquals(obj.list, obj2.list);
  }

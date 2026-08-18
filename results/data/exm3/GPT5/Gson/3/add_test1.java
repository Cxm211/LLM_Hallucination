// com/google/gson/functional/MapTest.java::testConcurrentNavigableMap
public void testConcurrentNavigableMapImplementation() throws Exception {
    Type typeOfMap = new TypeToken<ConcurrentNavigableMap<Integer, String>>() {}.getType();
    ConcurrentNavigableMap<Integer, String> map = gson.fromJson("{\"1\":\"a\"}", typeOfMap);
    assertTrue(map instanceof ConcurrentNavigableMap);
  }
// com/google/gson/functional/MapTest.java::testConcurrentMap
public void testConcurrentMapImplementation() throws Exception {
    Type typeOfMap = new TypeToken<ConcurrentMap<Integer, String>>() {}.getType();
    ConcurrentMap<Integer, String> map = gson.fromJson("{\"1\":\"a\"}", typeOfMap);
    assertTrue(map instanceof ConcurrentMap);
  }
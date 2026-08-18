// org/apache/commons/collections/list/TestSetUniqueList.java::testIntCollectionAddAll
public void testIntCollectionAddAll_IndexBounds() {
      List list = new SetUniqueList(new ArrayList(), new HashSet());
      list.add(new Integer(1));
      List toAdd = Arrays.asList(new Integer[] { new Integer(2) });

      try {
        list.addAll(-1, toAdd);
        fail("Expected IndexOutOfBoundsException for negative index");
      } catch (IndexOutOfBoundsException expected) {
        // expected
      }

      try {
        list.addAll(2, toAdd); // size is 1, so index 2 is out of bounds
        fail("Expected IndexOutOfBoundsException for index > size");
      } catch (IndexOutOfBoundsException expected) {
        // expected
      }
    }
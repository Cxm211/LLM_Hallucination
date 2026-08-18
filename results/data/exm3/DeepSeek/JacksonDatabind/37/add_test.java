// com/fasterxml/jackson/databind/objectid/Objecid1083Test.java
public void testObjectIdCyclicReference() throws Exception {
          final ObjectMapper mapper = new ObjectMapper();
          @JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
          class Node {
              public String id;
              public Node next;
          }
          Node node = new Node();
          node.id = "1";
          node.next = node;
          String json = mapper.writeValueAsString(node);
          Node result = mapper.readValue(json, Node.class);
          assertEquals("1", result.id);
          assertSame(result, result.next);
      }

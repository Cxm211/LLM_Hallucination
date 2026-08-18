private void processPrototypeParent(Node n, CompilerInput input) {
      switch (n.getType()) {
        
        case Token.GETPROP:
        case Token.GETELEM:
          Node dest = n.getFirstChild().getNext();
          if (dest.getType() == Token.STRING) {
            markPrototypePropertyCandidate(dest, input);
          }
          break;

        
        case Token.ASSIGN:
        case Token.CALL:
          Node map;
          if (n.getType() == Token.ASSIGN) {
            map = n.getFirstChild().getNext();
          } else {
            map = n.getLastChild();
          }
          if (map.getType() == Token.OBJECTLIT) {
            
            
            prototypeObjLits.add(map);

            for (Node key = map.getFirstChild();
                 key != null; key = key.getNext().getNext()) {
              if (key.getType() != Token.NUMBER) {
               
                markPrototypePropertyCandidate(key, input);
              }
              
              if (key.getNext() == null) {
                break;
              }
            }
          }
          break;
      }
    }
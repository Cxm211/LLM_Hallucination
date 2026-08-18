// buggy function
    private void writeObject(ObjectOutputStream out) throws IOException {
        out.defaultWriteObject();
        out.writeInt(size());
        for (Iterator it = iterator(); it.hasNext();) {
            out.writeObject(it.next());
        }
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        int size = in.readInt();
        buffer = new Object[size + 1];
        for (int i = 0; i < size; i++) {
            buffer[i] = in.readObject();
        }
        head = 0;
        tail = size;
    }

// trigger testcase
// org/apache/commons/collections/buffer/TestUnboundedFifoBuffer.java::testCollections220
public void testCollections220() throws Exception {
         UnboundedFifoBuffer buffer = new UnboundedFifoBuffer();
         
         buffer = (UnboundedFifoBuffer) serializeDeserialize(buffer);

         // test size() gets incremented
         buffer.add("Foo");
         assertEquals(1, buffer.size());
    }

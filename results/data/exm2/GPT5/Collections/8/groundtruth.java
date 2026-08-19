// ===== FIXED org.apache.commons.collections.buffer.UnboundedFifoBuffer :: readObject(ObjectInputStream) [lines 131-141] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/Collections/Collections-8-fixed/src/java/org/apache/commons/collections/buffer/UnboundedFifoBuffer.java =====
    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        int size = in.readInt();
        int length = in.readInt();
        buffer = new Object[length];
        for (int i = 0; i < size; i++) {
            buffer[i] = in.readObject();
        }
        head = 0;
        tail = size;
    }

// ===== FIXED org.apache.commons.collections.buffer.UnboundedFifoBuffer :: writeObject(ObjectOutputStream) [lines 115-122] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/Collections/Collections-8-fixed/src/java/org/apache/commons/collections/buffer/UnboundedFifoBuffer.java =====
    private void writeObject(ObjectOutputStream out) throws IOException {
        out.defaultWriteObject();
        out.writeInt(size());
        out.writeInt(buffer.length);
        for (Iterator it = iterator(); it.hasNext();) {
            out.writeObject(it.next());
        }
    }

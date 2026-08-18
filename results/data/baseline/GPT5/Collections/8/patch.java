private void writeObject(ObjectOutputStream out) throws IOException {
        Object[] oldBuffer = buffer;
        int oldHead = head;
        int oldTail = tail;
        try {
            buffer = null;
            head = 0;
            tail = 0;
            out.defaultWriteObject();
        } finally {
            buffer = oldBuffer;
            head = oldHead;
            tail = oldTail;
        }
        out.writeInt(size());
        for (Iterator it = iterator(); it.hasNext();) {
            out.writeObject(it.next());
        }
    }
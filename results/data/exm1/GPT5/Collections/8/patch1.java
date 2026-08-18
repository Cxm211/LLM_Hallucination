private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        int size = in.readInt();
        int capacity = Math.max(2, size + 1);
        buffer = new Object[capacity];
        for (int i = 0; i < size; i++) {
            buffer[i] = in.readObject();
        }
        head = 0;
        tail = size;
    }
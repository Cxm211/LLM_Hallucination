    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        int size = in.readInt();
        // Ensure buffer length is at least 2 to distinguish between empty and full when size==0
        int bufferLength = Math.max(size + 1, 2);
        buffer = new Object[bufferLength];
        for (int i = 0; i < size; i++) {
            buffer[i] = in.readObject();
        }
        head = 0;
        tail = size;
    }
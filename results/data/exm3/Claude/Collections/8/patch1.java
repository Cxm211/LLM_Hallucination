private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
    in.defaultReadObject();
    int size = in.readInt();
    int initialCapacity = (size == 0) ? 32 : size + 1;
    buffer = new Object[initialCapacity];
    for (int i = 0; i < size; i++) {
        buffer[i] = in.readObject();
    }
    head = 0;
    tail = size;
}
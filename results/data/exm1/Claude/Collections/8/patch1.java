private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
    in.defaultReadObject();
    int size = in.readInt();
    int capacity = (size < 1) ? 1 : size;
    buffer = new Object[capacity + 1];
    for (int i = 0; i < size; i++) {
        buffer[i] = in.readObject();
    }
    head = 0;
    tail = size;
}
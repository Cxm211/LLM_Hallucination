// buggy code
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

// relevant test
// org.apache.commons.collections.buffer.TestBoundedBuffer::testMaxSize
    public void testMaxSize() {
        final Buffer bounded = BoundedBuffer.decorate(new UnboundedFifoBuffer(), 2, 500);
        BoundedCollection bc = (BoundedCollection) bounded;
        assertEquals(2, bc.maxSize());
        assertEquals(false, bc.isFull());
        bounded.add("A");
        assertEquals(false, bc.isFull());
        bounded.add("B");
        assertEquals(true, bc.isFull());
        bounded.remove();
        assertEquals(false, bc.isFull());
        try {
            BoundedBuffer.decorate(new UnboundedFifoBuffer(), 0);
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            BoundedBuffer.decorate(new UnboundedFifoBuffer(), -1);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.apache.commons.collections.buffer.TestBoundedBuffer::testAddToFullBufferNoTimeout
    public void testAddToFullBufferNoTimeout() {
        final Buffer bounded = BoundedBuffer.decorate(new UnboundedFifoBuffer(), 1);
        bounded.add( "Hello" );
        try {
            bounded.add("World");
            fail();
        } catch (BufferOverflowException e) {
        }
    }

// org.apache.commons.collections.buffer.TestBoundedBuffer::testAddAllToFullBufferNoTimeout
    public void testAddAllToFullBufferNoTimeout() {
        final Buffer bounded = BoundedBuffer.decorate(new UnboundedFifoBuffer(), 1);
        bounded.add( "Hello" );
        try {
            bounded.addAll(Collections.singleton("World"));
            fail();
        } catch (BufferOverflowException e) {
        }
    }

// org.apache.commons.collections.buffer.TestBoundedBuffer::testAddAllToEmptyBufferExceedMaxSizeNoTimeout
    public void testAddAllToEmptyBufferExceedMaxSizeNoTimeout() {
        final Buffer bounded = BoundedBuffer.decorate(new UnboundedFifoBuffer(), 1);
        try {
            bounded.addAll(Collections.nCopies(2, "test"));
            fail();
        } catch (BufferOverflowException e) {
        }
    }

// org.apache.commons.collections.buffer.TestBoundedBuffer::testAddToFullBufferRemoveViaIterator
    public void testAddToFullBufferRemoveViaIterator() {
        final Buffer bounded = BoundedBuffer.decorate(new UnboundedFifoBuffer(), 1, 500);
        bounded.add( "Hello" );
        new DelayedIteratorRemove( bounded, 200 ).start();
        bounded.add( "World" );
        assertEquals( 1, bounded.size() );
        assertEquals( "World", bounded.get() );

    }

// org.apache.commons.collections.buffer.TestBoundedBuffer::testAddAllToFullBufferRemoveViaIterator
    public void testAddAllToFullBufferRemoveViaIterator() {
        final Buffer bounded = BoundedBuffer.decorate(new UnboundedFifoBuffer(), 2, 500);
        bounded.add( "Hello" );
        bounded.add( "World" );
        new DelayedIteratorRemove( bounded, 200, 2 ).start();
        bounded.addAll( Arrays.asList( new String[] { "Foo", "Bar" } ) );
        assertEquals( 2, bounded.size() );
        assertEquals( "Foo", bounded.remove() );
        assertEquals( "Bar", bounded.remove() );
    }

// org.apache.commons.collections.buffer.TestBoundedBuffer::testAddToFullBufferWithTimeout
    public void testAddToFullBufferWithTimeout() {
        final Buffer bounded = BoundedBuffer.decorate(new UnboundedFifoBuffer(), 1, 500);
        bounded.add( "Hello" );
        new DelayedRemove( bounded, 200 ).start();
        bounded.add( "World" );
        assertEquals( 1, bounded.size() );
        assertEquals( "World", bounded.get() );
        try {
            bounded.add( "!" );
            fail();
        }
        catch( BufferOverflowException e ) {
        }
    }

// org.apache.commons.collections.buffer.TestBoundedBuffer::testAddAllToFullBufferWithTimeout
    public void testAddAllToFullBufferWithTimeout() {
        final Buffer bounded = BoundedBuffer.decorate(new UnboundedFifoBuffer(), 2, 500);
        bounded.add( "Hello" );
        bounded.add( "World" );
        new DelayedRemove( bounded, 200, 2 ).start();

        bounded.addAll( Arrays.asList( new String[] { "Foo", "Bar" } ) );
        assertEquals( 2, bounded.size() );
        assertEquals( "Foo", bounded.get() );
        try {
            bounded.add( "!" );
            fail();
        }
        catch( BufferOverflowException e ) {
        }
    }

// org.apache.commons.collections.buffer.TestUnboundedFifoBuffer::testUnboundedFifoBufferRemove
    public void testUnboundedFifoBufferRemove() {
        resetFull();
        int size = confirmed.size();
        for (int i = 0; i < size; i++) {
            Object o1 = ((UnboundedFifoBuffer)collection).remove();
            Object o2 = ((ArrayList)confirmed).remove(0);
            assertEquals("Removed objects should be equal", o1, o2);
            verify();
        }
    }

// org.apache.commons.collections.buffer.TestUnboundedFifoBuffer::testConstructorException1
    public void testConstructorException1() {
        try {
            new UnboundedFifoBuffer(0);
        } catch (IllegalArgumentException ex) {
            return;
        }
        fail();
    }

// org.apache.commons.collections.buffer.TestUnboundedFifoBuffer::testConstructorException2
    public void testConstructorException2() {
        try {
            new UnboundedFifoBuffer(-20);
        } catch (IllegalArgumentException ex) {
            return;
        }
        fail();
    }

// org.apache.commons.collections.buffer.TestUnboundedFifoBuffer::testInternalStateAdd
    public void testInternalStateAdd() {
        UnboundedFifoBuffer test = new UnboundedFifoBuffer(2);
        assertEquals(3, test.buffer.length);
        assertEquals(0, test.head);
        assertEquals(0, test.tail);
        test.add("A");
        assertEquals(3, test.buffer.length);
        assertEquals(0, test.head);
        assertEquals(1, test.tail);
        test.add("B");
        assertEquals(3, test.buffer.length);
        assertEquals(0, test.head);
        assertEquals(2, test.tail);
        test.add("C");  
        assertEquals(5, test.buffer.length);
        assertEquals(0, test.head);
        assertEquals(3, test.tail);
        test.add("D");
        assertEquals(5, test.buffer.length);
        assertEquals(0, test.head);
        assertEquals(4, test.tail);
    }

// org.apache.commons.collections.buffer.TestUnboundedFifoBuffer::testInternalStateAddWithWrap
    public void testInternalStateAddWithWrap() {
        UnboundedFifoBuffer test = new UnboundedFifoBuffer(3);
        assertEquals(4, test.buffer.length);
        assertEquals(0, test.head);
        assertEquals(0, test.tail);
        test.add("A");
        assertEquals(4, test.buffer.length);
        assertEquals(0, test.head);
        assertEquals(1, test.tail);
        test.add("B");
        assertEquals(4, test.buffer.length);
        assertEquals(0, test.head);
        assertEquals(2, test.tail);
        test.add("C");
        assertEquals(4, test.buffer.length);
        assertEquals(0, test.head);
        assertEquals(3, test.tail);
        test.remove("A");
        assertEquals(4, test.buffer.length);
        assertEquals(1, test.head);
        assertEquals(3, test.tail);
        test.remove("B");
        assertEquals(4, test.buffer.length);
        assertEquals(2, test.head);
        assertEquals(3, test.tail);
        test.add("D");
        assertEquals(4, test.buffer.length);
        assertEquals(2, test.head);
        assertEquals(0, test.tail);
        test.add("E");
        assertEquals(4, test.buffer.length);
        assertEquals(2, test.head);
        assertEquals(1, test.tail);
    }

// org.apache.commons.collections.buffer.TestUnboundedFifoBuffer::testInternalStateRemove1
    public void testInternalStateRemove1() {
        UnboundedFifoBuffer test = new UnboundedFifoBuffer(4);
        test.add("A");
        test.add("B");
        test.add("C");
        assertEquals(5, test.buffer.length);
        assertEquals(0, test.head);
        assertEquals(3, test.tail);
        
        test.remove("A");
        assertEquals(5, test.buffer.length);
        assertEquals(1, test.head);
        assertEquals(3, test.tail);
        
        test.add("D");
        assertEquals(5, test.buffer.length);
        assertEquals(1, test.head);
        assertEquals(4, test.tail);
    }

// org.apache.commons.collections.buffer.TestUnboundedFifoBuffer::testInternalStateRemove2
    public void testInternalStateRemove2() {
        UnboundedFifoBuffer test = new UnboundedFifoBuffer(4);
        test.add("A");
        test.add("B");
        test.add("C");
        assertEquals(5, test.buffer.length);
        assertEquals(0, test.head);
        assertEquals(3, test.tail);
        
        test.remove("B");
        assertEquals(5, test.buffer.length);
        assertEquals(0, test.head);
        assertEquals(2, test.tail);
        
        test.add("D");
        assertEquals(5, test.buffer.length);
        assertEquals(0, test.head);
        assertEquals(3, test.tail);
    }

// org.apache.commons.collections.buffer.TestUnboundedFifoBuffer::testInternalStateIteratorRemove1
    public void testInternalStateIteratorRemove1() {
        UnboundedFifoBuffer test = new UnboundedFifoBuffer(4);
        test.add("A");
        test.add("B");
        test.add("C");
        assertEquals(5, test.buffer.length);
        assertEquals(0, test.head);
        assertEquals(3, test.tail);
        
        Iterator it = test.iterator();
        it.next();
        it.remove();
        assertEquals(5, test.buffer.length);
        assertEquals(1, test.head);
        assertEquals(3, test.tail);
        
        test.add("D");
        assertEquals(5, test.buffer.length);
        assertEquals(1, test.head);
        assertEquals(4, test.tail);
    }

// org.apache.commons.collections.buffer.TestUnboundedFifoBuffer::testInternalStateIteratorRemove2
    public void testInternalStateIteratorRemove2() {
        UnboundedFifoBuffer test = new UnboundedFifoBuffer(4);
        test.add("A");
        test.add("B");
        test.add("C");
        
        Iterator it = test.iterator();
        it.next();
        it.next();
        it.remove();
        assertEquals(5, test.buffer.length);
        assertEquals(0, test.head);
        assertEquals(2, test.tail);
        
        test.add("D");
        assertEquals(5, test.buffer.length);
        assertEquals(0, test.head);
        assertEquals(3, test.tail);
    }

// org.apache.commons.collections.buffer.TestUnboundedFifoBuffer::testInternalStateIteratorRemoveWithTailAtEnd1
    public void testInternalStateIteratorRemoveWithTailAtEnd1() {
        UnboundedFifoBuffer test = new UnboundedFifoBuffer(3);
        test.add("A");
        test.add("B");
        test.add("C");
        test.remove("A");
        test.add("D");
        assertEquals(4, test.buffer.length);
        assertEquals(1, test.head);
        assertEquals(0, test.tail);
        
        Iterator it = test.iterator();
        assertEquals("B", it.next());
        it.remove();
        assertEquals(4, test.buffer.length);
        assertEquals(2, test.head);
        assertEquals(0, test.tail);
    }

// org.apache.commons.collections.buffer.TestUnboundedFifoBuffer::testInternalStateIteratorRemoveWithTailAtEnd2
    public void testInternalStateIteratorRemoveWithTailAtEnd2() {
        UnboundedFifoBuffer test = new UnboundedFifoBuffer(3);
        test.add("A");
        test.add("B");
        test.add("C");
        test.remove("A");
        test.add("D");
        assertEquals(4, test.buffer.length);
        assertEquals(1, test.head);
        assertEquals(0, test.tail);
        
        Iterator it = test.iterator();
        assertEquals("B", it.next());
        assertEquals("C", it.next());
        it.remove();
        assertEquals(4, test.buffer.length);
        assertEquals(1, test.head);
        assertEquals(3, test.tail);
    }

// org.apache.commons.collections.buffer.TestUnboundedFifoBuffer::testInternalStateIteratorRemoveWithTailAtEnd3
    public void testInternalStateIteratorRemoveWithTailAtEnd3() {
        UnboundedFifoBuffer test = new UnboundedFifoBuffer(3);
        test.add("A");
        test.add("B");
        test.add("C");
        test.remove("A");
        test.add("D");
        assertEquals(4, test.buffer.length);
        assertEquals(1, test.head);
        assertEquals(0, test.tail);
        
        Iterator it = test.iterator();
        assertEquals("B", it.next());
        assertEquals("C", it.next());
        assertEquals("D", it.next());
        it.remove();
        assertEquals(4, test.buffer.length);
        assertEquals(1, test.head);
        assertEquals(3, test.tail);
    }

// org.apache.commons.collections.buffer.TestUnboundedFifoBuffer::testInternalStateIteratorRemoveWithWrap1
    public void testInternalStateIteratorRemoveWithWrap1() {
        UnboundedFifoBuffer test = new UnboundedFifoBuffer(3);
        test.add("A");
        test.add("B");
        test.add("C");
        test.remove("A");
        test.remove("B");
        test.add("D");
        test.add("E");
        assertEquals(4, test.buffer.length);
        assertEquals(2, test.head);
        assertEquals(1, test.tail);
        
        Iterator it = test.iterator();
        assertEquals("C", it.next());
        it.remove();
        assertEquals(4, test.buffer.length);
        assertEquals(3, test.head);
        assertEquals(1, test.tail);
    }

// org.apache.commons.collections.buffer.TestUnboundedFifoBuffer::testInternalStateIteratorRemoveWithWrap2
    public void testInternalStateIteratorRemoveWithWrap2() {
        UnboundedFifoBuffer test = new UnboundedFifoBuffer(3);
        test.add("A");
        test.add("B");
        test.add("C");
        test.remove("A");
        test.remove("B");
        test.add("D");
        test.add("E");
        assertEquals(4, test.buffer.length);
        assertEquals(2, test.head);
        assertEquals(1, test.tail);
        
        Iterator it = test.iterator();
        assertEquals("C", it.next());
        assertEquals("D", it.next());
        it.remove();
        assertEquals(4, test.buffer.length);
        assertEquals(2, test.head);
        assertEquals(0, test.tail);
    }

// org.apache.commons.collections.buffer.TestUnboundedFifoBuffer::testInternalStateIteratorRemoveWithWrap3
    public void testInternalStateIteratorRemoveWithWrap3() {
        UnboundedFifoBuffer test = new UnboundedFifoBuffer(3);
        test.add("A");
        test.add("B");
        test.add("C");
        test.remove("A");
        test.remove("B");
        test.add("D");
        test.add("E");
        assertEquals(4, test.buffer.length);
        assertEquals(2, test.head);
        assertEquals(1, test.tail);
        
        Iterator it = test.iterator();
        assertEquals("C", it.next());
        assertEquals("D", it.next());
        assertEquals("E", it.next());
        it.remove();
        assertEquals(4, test.buffer.length);
        assertEquals(2, test.head);
        assertEquals(0, test.tail);
    }

// org.apache.commons.collections.buffer.TestUnboundedFifoBuffer::testCollections220
    public void testCollections220() throws Exception {
         UnboundedFifoBuffer buffer = new UnboundedFifoBuffer();
         
         buffer = (UnboundedFifoBuffer) serializeDeserialize(buffer);

         
         buffer.add("Foo");
         assertEquals(1, buffer.size());
    }

// org.apache.commons.collections.buffer.TestUnmodifiableBuffer::testBufferRemove
    public void testBufferRemove() {
        resetEmpty();
        Buffer buffer = (Buffer) collection;
        try {
            buffer.remove();
            fail();
        } catch (UnsupportedOperationException ex) {}
    }

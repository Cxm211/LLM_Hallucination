// com/fasterxml/jackson/core/util/TestTextBuffer.java
public void testExpandNoUnitGrowthAtThreshold()
      {
          TextBuffer tb = new TextBuffer(new BufferRecycler());
          char[] buf = tb.getCurrentSegment();

          // Expand enough times to cross any reasonable threshold
          // and ensure we never get just +1 growth at boundary
          for (int i = 0; i < 200000; ++i) {
              char[] old = buf;
              buf = tb.expandCurrentSegment();
              if (buf.length == old.length + 1) {
                  fail("Unexpected unit-size growth at boundary: " + old.length + " -> " + buf.length);
              }
              if (buf.length > 5_000_000) {
                  break;
              }
          }
      }
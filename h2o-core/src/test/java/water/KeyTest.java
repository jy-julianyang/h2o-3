package water;

import org.junit.Test;
import org.junit.runner.RunWith;
import water.runner.CloudSize;
import water.runner.H2ORunner;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.Assert.*;

@RunWith(H2ORunner.class)
@CloudSize(1)
public class KeyTest {
  
  @Test public void makeRandomKeyHasRightPrefixAndPostfix() {
    final String prefix = "foo";
    final String postfix = "bar";
    
    final Key<Keyed> key = Key.makeRandom(prefix, postfix);

    assertTrue(key.toString().startsWith(prefix));
    assertTrue(key.toString().endsWith(postfix));
  }

  @Test public void makeRandomKeyHasRightPrefix() {
    final String prefix = "FOO";
    
    final Key<Keyed> key = Key.makeRandom(prefix);

    assertTrue(key.toString().startsWith(prefix));
  }
  
  @Test public void makeRandomKeyHasNoWhitechars() {
    final Key<Keyed> key = Key.makeRandom("f o o ", "b a r");
    
    assertFalse(key.toString().contains("\\W"));
  }

  @Test public void makeRandomKeyCreatesUniqueKeys() {
    final int count = 1000;

    final Set<Key<Keyed>> keys = IntStream.range(0, count)
                                          .mapToObj(i -> Key.makeRandom("foo", "bar"))
                                          .collect(Collectors.toSet());

    assertEquals(count, keys.size());
  }
}

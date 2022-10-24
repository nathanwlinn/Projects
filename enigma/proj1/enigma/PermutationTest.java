package enigma;

import org.junit.Test;
import org.junit.Rule;
import org.junit.rules.Timeout;


import static org.junit.Assert.*;

import static enigma.TestUtils.*;

/** The suite of all JUnit tests for the Permutation class.
 *  @author nathan
 */
public class PermutationTest {

    /** Testing time limit. */
    @Rule
    public Timeout globalTimeout = Timeout.seconds(5);

    /* ***** TESTING UTILITIES ***** */

    private Permutation perm;
    private String alpha = UPPER_STRING;

    /** Check that perm has an alphabet whose size is that of
     *  FROMALPHA and TOALPHA and that maps each character of
     *  FROMALPHA to the corresponding character of FROMALPHA, and
     *  vice-versa. TESTID is used in error messages. */
    private void checkPerm(String testId,
                           String fromAlpha, String toAlpha) {
        int N = fromAlpha.length();
        assertEquals(testId + " (wrong length)", N, perm.size());
        for (int i = 0; i < N; i += 1) {
            char c = fromAlpha.charAt(i), e = toAlpha.charAt(i);
            assertEquals(msg(testId, "wrong translation of '%c'", c),
                         e, perm.permute(c));
            assertEquals(msg(testId, "wrong inverse of '%c'", e),
                         c, perm.invert(e));
            int ci = alpha.indexOf(c), ei = alpha.indexOf(e);
            assertEquals(msg(testId, "wrong translation of %d", ci),
                         ei, perm.permute(ci));
            assertEquals(msg(testId, "wrong inverse of %d", ei),
                         ci, perm.invert(ei));
        }
    }

    /* ***** TESTS ***** */

    @Test
    public void checkIdTransform() {
        perm = new Permutation("", UPPER);
        checkPerm("identity", UPPER_STRING, UPPER_STRING);
    }
    @Test
    public void sizetest() {
        Permutation one = new Permutation("(ABCD)", new Alphabet("ABCD"));
        Permutation two = new Permutation("(ABCD)", new Alphabet("ABCDQWERTY"));
        Permutation three = new Permutation("(ABCDZ)", new Alphabet());
        assertEquals(one.size(), 4);
        assertEquals(two.size(), 10);
        assertEquals(three.size(), 26);
    }
    @Test
    public void intpermute() {
        Permutation one = new Permutation("(ABCD)", new Alphabet("ABCD"));
        Permutation two = new Permutation("(ABCD) (E)", new Alphabet("ABCDE"));
        Permutation a = new Permutation("", new Alphabet("XYZ"));
        assertEquals(0, a.permute(0));
        assertEquals(1, a.permute(1));
        assertEquals(2, one.permute(1));
        assertEquals(3, one.permute(2));
        assertEquals(1, one.permute(-4));
        assertEquals(1, one.permute(16));
        assertEquals(1, two.permute(5));
        assertEquals(4, two.permute(4));
    }
    @Test
    public void charpermute() {
        Permutation one = new Permutation("(ABCD) (E)", new Alphabet("ABCDE"));
        assertEquals('B', one.permute('A'));
        assertEquals('D', one.permute('C'));
        assertEquals('E', one.permute('E'));
        assertEquals('C', one.permute('B'));
        assertEquals('A', one.permute('D'));
    }
    @Test
    public void intinvert() {
        Permutation one = new Permutation("", new Alphabet("ABC"));
        Permutation three = new Permutation("(ABCDZ)", new Alphabet());
        assertEquals(0, one.invert(0));
        assertEquals(1, one.invert(1));
        assertEquals(2, one.invert(17));
        assertEquals(0, one.invert(-3));
    }
    @Test
    public void charinvert() {
        Permutation one = new Permutation("(ABCDEF)", new Alphabet());
        assertEquals('D', one.invert('E'));
        assertEquals('E', one.invert('F'));
        assertEquals('C', one.invert('D'));
        assertEquals('B', one.invert('C'));
        assertEquals('F', one.invert('A'));
        Permutation two = new Permutation("(Z)", new Alphabet("Z"));
        assertEquals('Z', two.invert('Z'));
        Permutation three = new Permutation("(ABCZ)", new Alphabet());
        assertEquals('X', three.invert('X'));
        Permutation four =
                new Permutation("(ABC) (DE) (F)", new Alphabet("ABCDEF"));
        assertEquals('B', four.invert('C'));
        assertEquals('D', four.invert('E'));
        assertEquals('F', four.invert('F'));
    }
    @Test
    public void derangementt() {
        Permutation one = new Permutation("(X)", new Alphabet("X"));
        assertEquals(one.derangement(), false);
    }
}

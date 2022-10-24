package enigma;

import jdk.jshell.spi.ExecutionControl;
import net.sf.saxon.functions.EncodeForUri;

import java.util.HashMap;

import static enigma.EnigmaException.*;

/** Represents a permutation of a range of integers starting at 0 corresponding
 *  to the characters of an alphabet.
 *  @author nathan
 */
class Permutation {
    /** creates a hashmap for cycles. */
    private HashMap<Character, Character> cyclestring;

    /** Set this Permutation to that specified by CYCLES, a string in the
     *  form "(cccc) (cc) ..." where the c's are characters in ALPHABET, which
     *  is interpreted as a permutation in cycle notation.  Characters in the
     *  alphabet that are not included in any cycle map to themselves.
     *  Whitespace is ignored. */
    Permutation(String cycles, Alphabet alphabet) {
        _alphabet = alphabet;
        cyclestring = new HashMap<Character, Character>();

        cycles = cycles.replaceAll(" ", "");
        String cyc = cycles.replaceAll("[()]", "");
        StringBuilder checker = new StringBuilder();
        int start = 0;
        int end = 0;

        for (int x = 0; x < cyc.length(); x += 1) {
            char c = cyc.charAt(x);
            if (!_alphabet.contains(c)) {
                throw new EnigmaException("Letter not in alphabet");
            }
        }

        if (!cycles.equals("")) {
            for (int x = 1; x < cycles.length() - 1; x += 1) {
                if (cycles.charAt(x) == ')') {
                    if (cycles.charAt(x + 1) != '(') {
                        throw new EnigmaException("it's backwards");
                    }
                }
            }
            for (int i = 0; i < cycles.length(); i += 1) {
                if (cycles.charAt(i) == '(') {
                    start += 1;
                } else if (cycles.charAt(i) == ')') {
                    end += 1;
                    addCycle(checker.toString());
                    checker = new StringBuilder();
                } else {
                    checker.append(cycles.charAt(i));
                }
            }
            if (start != end) {
                throw new EnigmaException("wrong cycle");
            }
        }
    }

    /** Add the cycle c0->c1->...->cm->c0 to the permutation, where CYCLE is
     *  c0c1...cm. */
    private void addCycle(String cycle) {
        String cycl = cycle.replaceAll("[()]", "");
        for (int x = 0; x < cycle.length(); x += 1) {
            if (!_alphabet.contains(cycl.charAt(x))) {
                throw new EnigmaException("not in alphabet");
            } else if (cyclestring.containsKey(cycle.charAt(x))) {
                throw new EnigmaException("duplicate");
            }
            if (x == cycle.length() - 1) {
                cyclestring.put(cycle.charAt(x), cycle.charAt(0));
            } else {
                cyclestring.put(cycle.charAt(x), cycle.charAt(x + 1));
            }
        }
        for (int x = 0; x < cycle.length() - 1; x += 1) {
            if (cycl.charAt(x) == cycl.charAt(x + 1)) {
                throw new EnigmaException("duplicate");
            }
        }
    }

    /** Return the value of P modulo the size of this permutation. */
    final int wrap(int p) {
        int r = p % size();
        if (r < 0) {
            r += size();
        }
        return r;
    }

    /** Returns the size of the alphabet I permute. */
    int size() {
        return _alphabet.size();
    }

    /** Return the result of applying this permutation to P modulo the
     *  alphabet size. */
    int permute(int p) {
        p = wrap(p);
        return _alphabet.toInt(permute(_alphabet.toChar(p)));
    }

    /** Return the result of applying the inverse of this permutation
     *  to  C modulo the alphabet size. */
    int invert(int c) {
        c = wrap(c);
        return _alphabet.toInt(invert(_alphabet.toChar(c)));
    }

    /** Return the result of applying this permutation to the index of P
     *  in ALPHABET, and converting the result to a character of ALPHABET. */
    char permute(char p) {
        if (!alphabet().contains(p)) {
            throw new EnigmaException("letter missing");
        }
        return cyclestring.getOrDefault(p, p);
    }

    /** Return the result of applying the inverse of this permutation to C. */
    char invert(char c) {
        if (!_alphabet.contains(c)) {
            throw new EnigmaException("Inverted value not in alphabet");
        } else if (_alphabet.contains(c) && cyclestring.containsValue(c)) {
            for (char e : cyclestring.keySet()) {
                if (cyclestring.get(e) == c) {
                    return e;
                }
            }
        }
        return c;
    }

    /** Return the alphabet used to initialize this Permutation. */
    Alphabet alphabet() {
        return _alphabet;
    }

    /** Return true iff this permutation is a derangement (i.e., a
     *  permutation for which no value maps to itself). */
    boolean derangement() {
        boolean x = true;
        if (cyclestring.isEmpty()) {
            x = false;
        }
        if (!cyclestring.isEmpty()) {
            for (int y = 0; y < _alphabet.size(); y += 1) {
                if (permute(y) == y) {
                    x = false;
                }
            }
        }
        return x;
    }

    /** Alphabet of this permutation. */
    private Alphabet _alphabet;

}

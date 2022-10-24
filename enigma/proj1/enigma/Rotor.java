package enigma;

import static enigma.EnigmaException.*;

/** Superclass that represents a rotor in the enigma machine.
 *  @author nathan
 */
class Rotor {
    /** creates setting integer. */
    private int newset = 0;
    /** creates the ringset. */
    private int ringset = 0;

    /** A rotor named NAME whose permutation is given by PERM. */
    Rotor(String name, Permutation perm) {
        _name = name;
        _permutation = perm;
        newset = 0;
        ringset = 0;
    }

    /** Return my name. */
    String name() {
        return _name;
    }

    /** Return my alphabet. */
    Alphabet alphabet() {
        return _permutation.alphabet();
    }

    /** Return my permutation. */
    Permutation permutation() {
        return _permutation;
    }

    /** Return the size of my alphabet. */
    int size() {
        return _permutation.size();
    }

    /** Return true iff I have a ratchet and can move. */
    boolean rotates() {
        return false;
    }

    /** Return true iff I reflect. */
    boolean reflecting() {
        return false;
    }

    /** Return my current setting. */
    int setting() {
        return newset;
    }

    /** Set setting() to POSN.  */
    void set(int posn) {
        newset = _permutation.wrap(posn);
    }

    /** Set setting() to character CPOSN. */
    void set(char cposn) {
        newset = alphabet().toInt(cposn);
    }

    /** Return the conversion of P (an integer in the range 0..size()-1)
     *  according to my permutation. */
    int convertForward(int p) {
        return permutation().wrap(permutation().permute
                (permutation().wrap(p + setting()
                        - ringSetting())) - setting() + ringSetting());
    }

    /** Return the conversion of E (an integer in the range 0..size()-1)
     *  according to the inverse of my permutation. */
    int convertBackward(int e) {
        return permutation().wrap(permutation().invert
                (permutation().wrap(e + setting()
                    - ringSetting())) - setting() + ringSetting());
    }

    /** Returns true iff I am positioned to allow the rotor to my left
     *  to advance. */
    boolean atNotch() {
        return false;
    }

    /** Advance me one position, if possible. By default, does nothing. */
    void advance() {
    }

    @Override
    public String toString() {
        return "Rotor " + _name;
    }

    /** My name. */
    private final String _name;

    /** The permutation implemented by this rotor in its 0 position. */
    private Permutation _permutation;

    /** setting the ring setting POSN. */
    void setRing(int posn) {
        ringset = permutation().wrap(posn);
    }

    /** setting for ring for CPOSN. */
    void setRing(char cposn) {
        ringset = alphabet().toInt(cposn);
    }

    /** Return my current setting. */
    int ringSetting() {
        return ringset;
    }

}

package enigma;

import static enigma.EnigmaException.*;

/** Class that represents a rotating rotor in the enigma machine.
 *  @author nathan
 */
class MovingRotor extends Rotor {
    /** creates private string for notches. */
    private String notches2;

    /** A rotor named NAME whose permutation in its default setting is
     *  PERM, and whose notches are at the positions indicated in NOTCHES.
     *  The Rotor is initally in its 0 setting (first character of its
     *  alphabet).
     */
    MovingRotor(String name, Permutation perm, String notches) {
        super(name, perm);
        notches2 = notches;
    }

    @Override
    void advance() {
        set(permutation().wrap(setting()) + 1);
    }

    @Override
    boolean atNotch() {
        for (int x = 0; x < notches2.length(); x += 1) {
            if (permutation().wrap(setting())
                    == alphabet().toInt(notches2.charAt(x))) {
                return true;
            }
        }
        return false;
    }
    @Override
    boolean rotates() {
        return true;
    }

}

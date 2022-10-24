package enigma;

import jdk.jshell.spi.ExecutionControl;

import java.util.Collection;
import java.util.HashMap;


import static enigma.EnigmaException.*;

/** Class that represents a complete enigma machine.
 *  @author nathan
 */
class Machine {

    /** num of rotors. */
    private int numofrotors;
    /** num of pawls. */
    private int numofpawls;
    /** rotor arrays of current things. */
    private Rotor[] rotorcurrent;
    /** num of rotors (all). */
    private Rotor[] _allRotors;
    /** creates plugboard. */
    private Permutation _plugboard;

    /** A new Enigma machine with alphabet ALPHA, 1 < NUMROTORS rotor slots,
     *  and 0 <= PAWLS < NUMROTORS pawls.  ALLROTORS contains all the
     *  available rotors. */
    Machine(Alphabet alpha, int numRotors, int pawls,
            Collection<Rotor> allRotors) {
        _alphabet = alpha;
        numofrotors = numRotors;
        numofpawls = pawls;
        rotorcurrent = new Rotor[numofrotors];
        _allRotors = allRotors.toArray(new Rotor[0]);
        assert numofpawls >= 0;
        assert numofrotors > 1;
        if (numofrotors < 1) {
            throw new EnigmaException("1 < NUMROTORS rotor slots");
        }
        if (numofpawls <= 0) {
            throw new EnigmaException("pawls must be >= 0");
        }
        if (numofpawls >= numofrotors) {
            throw new EnigmaException("pawls < rotors");
        }
    }

    /** Return the number of rotor slots I have. */
    int numRotors() {
        return numofrotors;
    }

    /** Return the number pawls (and thus rotating rotors) I have. */
    int numPawls() {
        return numofpawls;
    }

    /** Return number of rotorcurrent. */
    Rotor[] rotorCurr() {
        return rotorcurrent;
    }

    /** Set my rotor slots to the rotors named ROTORS from my set of
     *  available rotors (ROTORS[0] names the reflector).
     *  Initially, all rotors are set at their 0 setting. */
    void insertRotors(String[] rotors) {
        if (numofrotors != rotors.length) {
            throw new EnigmaException("num rotors diff slots");
        }
        if (rotors == null) {
            throw new EnigmaException("empty string");
        }
        for (int i = 0; i < rotors.length; i += 1) {
            for (Rotor e : _allRotors) {
                if ((rotors[i].toLowerCase()).equals
                        ((e.name().toLowerCase()))) {
                    rotorcurrent[i] = e;
                }
            }
        }
        if (!rotorcurrent[0].reflecting()) {
            throw new EnigmaException("1st rotor isnt reflecting");
        }
        for (int y = 0; y < numRotors(); y += 1) {
            if (rotors[y] == null) {
                throw new EnigmaException("cannot be empty");
            }
        }
    }

    /** Set my rotors according to SETTING, which must be a string of
     *  numRotors()-1 characters in my alphabet. The first letter refers
     *  to the leftmost rotor setting (not counting the reflector).  */
    void setRotors(String setting) {
        if (setting.length() != (numRotors() - 1)) {
            throw new EnigmaException("settings is not numRotors() - 1");
        }
        if (setting.length() == (numRotors() - 1)) {
            for (int x = 0; x < setting.length(); x += 1) {
                if (!(_alphabet.contains(setting.charAt(x)))) {
                    throw new EnigmaException("char setting not in alphabet");
                }
                if  ((_alphabet.contains(setting.charAt(x)))) {
                    rotorcurrent[x + 1].set(setting.charAt(x));
                }
            }
        }
    }

    /** Set the plugboard to PLUGBOARD. */
    void setPlugboard(Permutation plugboard) {
        _plugboard = plugboard;
    }

    /** Returns the result of converting the input character C (as an
     *  index in the range 0..alphabet size - 1), after first advancing
     *  the machine. */
    int convert(int c) {
        int out = _plugboard.permute(c);
        int index = 0;
        int filler = numofrotors - 1;
        HashMap<Integer, Boolean> mover = new HashMap<Integer, Boolean>();
        for (int x = numRotors() - 1; x
                > (numofrotors - numPawls() - 1); x -= 1) {
            if (x == rotorcurrent.length - 1) {
                mover.put(index, true);
            } else if (rotorcurrent[x].rotates()) {
                if (rotorcurrent[x + 1].atNotch()) {
                    mover.put(index, true);
                    if (index >= 0) {
                        mover.put(index - 1, true);
                    }
                }
            }
            index = index + 1;
        }
        for (int x = 0; x < mover.size(); x += 1) {
            if (mover.get(x)) {
                rotorcurrent[filler].advance();
            }
            filler = filler - 1;
        }
        for (int i = numofrotors - 1; i >= 0; i -= 1) {
            out = rotorcurrent[i].convertForward(out);
        }
        for (int i = 1; i < rotorcurrent.length; i += 1) {
            out = rotorcurrent[i].convertBackward(out);
        }
        return _plugboard.permute(out);
    }

    /** Returns the encoding/decoding of MSG, updating the state of
     *  the rotors accordingly. */
    String convert(String msg) {
        String result = "";
        for (int i = 0; i < msg.length(); i += 1) {
            char newChar;
            int newInt;
            if (msg.charAt(i) == ' ') {
                continue;
            } else if (!_alphabet.contains(msg.charAt(i))) {
                throw new EnigmaException("not in alpha");
            }
            result = result
                    + _alphabet.toChar(convert(_alphabet.toInt(msg.charAt(i))));
        }
        return result;
    }
    /** * @param setting for the setrings. */
    void setRings(String setting) {
        assert numofrotors != 0;
        if ((setting.length() - (numofrotors - 1)) != 0) {
            throw new EnigmaException("wrong num rings");
        }
        for (int x = 1; x < rotorcurrent.length; x += 1) {
            rotorcurrent[x].setRing(setting.charAt(x - 1));
        }
    }

    /** Common alphabet of my rotors. */
    private final Alphabet _alphabet;

}

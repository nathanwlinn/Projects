package enigma;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;

import java.util.ArrayList;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.List;
import java.util.Arrays;


import static enigma.EnigmaException.*;

/** Enigma simulator.
 *  @author nathan
 */
public final class Main {

    /** Process a sequence of encryptions and decryptions, as
     *  specified by ARGS, where 1 <= ARGS.length <= 3.
     *  ARGS[0] is the name of a configuration file.
     *  ARGS[1] is optional; when present, it names an input file
     *  containing messages.  Otherwise, input comes from the standard
     *  input.  ARGS[2] is optional; when present, it names an output
     *  file for processed messages.  Otherwise, output goes to the
     *  standard output. Exits normally if there are no errors in the input;
     *  otherwise with code 1. */
    public static void main(String... args) {
        try {
            new Main(args).process();
            return;
        } catch (EnigmaException excp) {
            System.err.printf("Error: %s%n", excp.getMessage());
        }
        System.exit(1);
    }

    /** Check ARGS and open the necessary files (see comment on main). */
    Main(String[] args) {
        if (args.length < 1 || args.length > 3) {
            throw error("Only 1, 2, or 3 command-line arguments allowed");
        }
        _config = getInput(args[0]);
        if (args.length > 1) {
            _input = getInput(args[1]);
        } else {
            _input = new Scanner(System.in);
        }

        if (args.length > 2) {
            _output = getOutput(args[2]);
        } else {
            _output = System.out;
        }
    }

    /** Return a Scanner reading from the file named NAME. */
    private Scanner getInput(String name) {
        try {
            return new Scanner(new File(name));
        } catch (IOException excp) {
            throw error("could not open %s", name);
        }
    }

    /** Return a PrintStream writing to the file named NAME. */
    private PrintStream getOutput(String name) {
        try {
            return new PrintStream(new File(name));
        } catch (IOException excp) {
            throw error("could not open %s", name);
        }
    }

    /** Configure an Enigma machine from the contents of configuration
     *  file _config and apply it to the messages in _input, sending the
     *  results to _output. */
    private void process() {
        int e = 0;
        if (!_input.hasNextLine()) {
            throw new EnigmaException("no input");
        }
        Machine x = readConfig();
        _config.close();
        String printed = "";
        if (_input.hasNextLine()) {
            while (_input.hasNextLine()) {
                String step = _input.nextLine();
                if (step.isEmpty()) {
                    printMessageLine(step);
                } else if (step.trim().charAt(0) == '*') {
                    setUp(x, step);
                    e += 1;
                } else if (e == 0) {
                    throw new EnigmaException("first line has no reflector");
                } else {
                    printMessageLine(x.convert(step));
                }
            }
            _config.close();
            _input.close();
        }
    }


    /** Return an Enigma machine configured from the contents of configuration
     *  file _config. */
    private Machine readConfig() {
        try {
            if (_config.hasNextLine()) {
                _alphabet = new Alphabet(_config.nextLine().trim());
            } else {
                throw new EnigmaException("bad config");
            }
            ArrayList<Rotor> container = new ArrayList<Rotor>();
            int nrotors = _config.nextInt();
            int npawls = _config.nextInt();
            _config.nextLine();
            while (_config.hasNext()) {
                container.add(readRotor());
            }
            return new Machine(_alphabet, nrotors, npawls, container);
        } catch (NoSuchElementException excp) {
            throw error("configuration file truncated");
        }
    }

    /** Return a rotor, reading its description from _config. */
    private Rotor readRotor() {
        Rotor holder = null;
        try {
            if (_config.hasNextLine() && _config.hasNext()) {
                String id = _config.next();
                String position = _config.next();
                String line = _config.nextLine();
                String notch = "";
                while (_config.hasNext("[(].*[)]")) {
                    line += _config.next();
                }
                if (position.length() > 1) {
                    for (int x = 1; x < position.length(); x += 1) {
                        notch += position.charAt(x);
                    }
                }
                char type = position.charAt(0);
                if (type == 'N') {
                    holder = new FixedRotor(id,
                            new Permutation(line, _alphabet));
                } else if (type == 'M') {
                    holder = new MovingRotor(id,
                            new Permutation(line, _alphabet), notch);
                } else if (type == 'R') {
                    holder = new Reflector(id,
                            new Permutation(line, _alphabet));
                } else {
                    throw new EnigmaException("unknown type");
                }
            }
        } catch (NoSuchElementException excp) {
            throw error("bad rotor description");
        }
        return holder;
    }

    /** Set M according to the specification given on SETTINGS,
     *  which must have the format specified in the assignment. */
    private void setUp(Machine M, String settings) {
        try {
            String[] rotoring = new String[M.numRotors()];
            String[] rotorsetting = settings.trim().split(" ");
            if (rotorsetting.length - 1 < M.numRotors()) {
                throw new EnigmaException("too little args");
            }
            Scanner scan = new Scanner(settings);
            String filler = "";
            for (int x = 0; x < M.numRotors(); x += 1) {
                String pos = scan.next();
                List<String> detect = Arrays.asList(rotoring);
                if (detect.contains(pos)) {
                    throw new EnigmaException("already in list");
                }
            }
            for (int q = 1; q < rotorsetting.length; q += 1) {
                if (M.numRotors() >= q) {
                    rotoring[q - 1] = rotorsetting[q];
                } else if ((M.numRotors() + 1) < q) {
                    filler = filler + rotorsetting[q];
                }
            }
            M.insertRotors(rotoring);
            String rings = "";
            try {
                if (!rotorsetting[M.numRotors() + 2].contains("(")) {
                    rings = rotorsetting[M.numRotors() + 2];
                    if (rings.length() != 0 && rings.length() > 0) {
                        M.setRings(rings);
                    }
                }
            } catch (ArrayIndexOutOfBoundsException outb) {
                rings = "";
            }
            if (!M.rotorCurr()[0].reflecting()) {
                throw new EnigmaException("missing reflect");
            }
            try {
                M.setRotors(rotorsetting[M.numRotors() + 1]);
            } catch (ArrayIndexOutOfBoundsException outb) {
                throw error("out of bounds");
            }
            try {
                M.setPlugboard(new Permutation(filler, _alphabet));
            } catch (ArrayIndexOutOfBoundsException outb) {
                M.setPlugboard(new Permutation("", _alphabet));
            }
            if ((M.numRotors() - 1)
                    != rotorsetting[M.numRotors() + 1].length()) {
                throw new EnigmaException("numrotors diff string length");
            }
        } catch (NoSuchElementException excp) {
            throw error("bad setting");
        }
    }

    /** Print MSG in groups of five (except that the last group may
     *  have fewer letters). */
    private void printMessageLine(String msg) {
        int limit = 5;
        for (int x = 0; x < msg.length(); x += 1) {
            if (limit == 0) {
                _output.print(" ");
                limit = 5;
            }
            if (msg.charAt(x) != ' ') {
                _output.print(msg.charAt(x));
                limit -= 1;
            }
        }
        _output.println();
    }

    /** Alphabet used in this machine. */
    private Alphabet _alphabet;

    /** Source of input messages. */
    private Scanner _input;

    /** Source of machine configuration. */
    private Scanner _config;

    /** File for encoded/decoded messages. */
    private PrintStream _output;
}

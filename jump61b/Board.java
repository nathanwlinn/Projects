
package jump61;


import java.util.function.Consumer;
import java.util.ArrayList;
import java.util.ArrayDeque;
import java.util.Formatter;
import static jump61.Side.*;
import static jump61.Square.square;

/** Represents the state of a Jump61 game.  Squares are indexed either by
 *  row and column (between 1 and size()), or by square number, numbering
 *  squares by rows, with squares in row 1 numbered from 0 to size()-1, in
 *  row 2 numbered from size() to 2*size() - 1, etc. (i.e., row-major order).
 *
 *  A Board may be given a notifier---a Consumer<Board> whose
 *  .accept method is called whenever the Board's contents are changed.
 *
 *  @author nathan
 */
class Board {

    /** Playboard. */
    private Square[][] playboard;
    /** Movecounter. */
    private int movecounter;
    /** History. */
    private ArrayList<Board> phistory;

    /** An uninitialized Board.  Only for use by subtypes. */
    protected Board() {
        _notifier = NOP;
    }

    /** An N x N board in initial configuration. */
    Board(int N) {
        this();
        try {
            assert N > 1;
        } catch (AssertionError daddas) {
            throw new GameException("N too small");
        }
        movecounter = 0;
        playboard = new Square[N][N];
        phistory = new ArrayList<Board>();
        for (int x = 0; x < N; x += 1) {
            for (int y = 0; y < N; y += 1) {
                playboard[x][y] = square(WHITE, 1);
            }
        }
    }

    /** A board whose initial contents are copied from BOARD0, but whose
     *  undo history is clear, and whose notifier does nothing. */
    Board(Board board0) {
        this(board0.size());
        playboard = new Square[board0.size()][board0.size()];
        for (int x = 0; x < board0.size(); x += 1) {
            for (int y = 0; y < board0.size(); y += 1) {
                playboard[x][y] = board0.get(x + 1, y + 1);
            }
        }
        _readonlyBoard = new ConstantBoard(this);
        phistory = new ArrayList<Board>();
    }

    /** Returns a readonly version of this board. */
    Board readonlyBoard() {
        return _readonlyBoard;
    }

    /** (Re)initialize me to a cleared board with N squares on a side. Clears
     *  the undo history and sets the number of moves to 0. */
    void clear(int N) {
        movecounter = 0;
        playboard = new Square[N][N];
        for (int x = 0; x < playboard.length; x += 1) {
            for (int y = 0; y < playboard.length; y += 1) {
                playboard[x][y] = square(WHITE, 1);
            }
        }
        announce();
    }

    /** Copy the contents of BOARD into me. */
    void copy(Board board) {
        clear(board.size());
        for (int x = 0; x < board.size(); x += 1) {
            for (int y = 0; y < board.size(); y += 1) {
                playboard[x][y] = board.get(x + 1, y + 1);
            }
        }
        movecounter = board.movecounter;
    }

    /** Copy the contents of BOARD into me, without modifying my undo
     *  history. Assumes BOARD and I have the same size. */
    private void internalCopy(Board board) {
        assert size() == board.size();
        int e = 0;
        for (int x = 0; x < board.size(); x += 1) {
            for (int y = 0; y < board.size(); y += 1) {
                playboard[x][y] = board.get(x + 1, y + 1);
            }
        }
        movecounter = board.movecounter;
    }

    /** Return the number of rows and of columns of THIS. */
    int size() {
        return this.playboard.length;
    }

    /** Returns the contents of the square at row R, column C
     *  1 <= R, C <= size (). */
    Square get(int r, int c) {
        return get(sqNum(r, c));
    }

    /** Returns the contents of square #N, numbering squares by rows, with
     *  squares in row 1 number 0 - size()-1, in row 2 numbered
     *  size() - 2*size() - 1, etc. */
    Square get(int n) {
        return playboard[row(n) - 1][col(n) - 1];
    }

    /** Returns the total number of spots on the board. */
    int numPieces() {
        int e = 0;
        int spots = 0;
        for (int x = 0; x < size(); x += 1) {
            for (int y = 0; y < size(); y += 1) {
                spots = get(x + 1, y + 1).getSpots();
                e += spots;
            }
        }
        return e;
    }

    /** Returns the Side of the player who would be next to move.  If the
     *  game is won, this will return the loser (assuming legal position). */
    Side whoseMove() {
        return ((numPieces() + size()) & 1) == 0 ? RED : BLUE;
    }

    /** Return true iff row R and column C denotes a valid square. */
    final boolean exists(int r, int c) {
        return 1 <= r && r <= size() && 1 <= c && c <= size();
    }

    /** Return true iff S is a valid square number. */
    final boolean exists(int s) {
        int N = size();
        return 0 <= s && s < N * N;
    }

    /** Return the row number for square #N. */
    final int row(int n) {
        return n / size() + 1;
    }

    /** Return the column number for square #N. */
    final int col(int n) {
        return n % size() + 1;
    }

    /** Return the square number of row R, column C. */
    final int sqNum(int r, int c) {
        return (c - 1) + (r - 1) * size();
    }

    /** Return a string denoting move (ROW, COL)N. */
    String moveString(int row, int col) {
        return String.format("%d %d", row, col);
    }

    /** Return a string denoting move N. */
    String moveString(int n) {
        return String.format("%d %d", row(n), col(n));
    }

    /** Returns true iff it would currently be legal for PLAYER to add a spot
        to square at row R, column C. */
    boolean isLegal(Side player, int r, int c) {
        return isLegal(player, sqNum(r, c));
    }

    /** Returns true iff it would currently be legal for PLAYER to add a spot
     *  to square #N. */
    boolean isLegal(Side player, int n) {
        if (!exists(n)) {
            return false;
        }
        if (this.get(n).getSide() == player) {
            return true;
        }
        if (this.get(n).getSide() == WHITE) {
            return true;
        }
        return false;
    }

    /** Returns true iff PLAYER is allowed to move at this point. */
    boolean isLegal(Side player) {
        if (getWinner() == null) {
            return player == whoseMove();
        }
        return false;
    }

    /** Returns the winner of the current position, if the game is over,
     *  and otherwise null. */
    final Side getWinner() {
        int e = 0;
        if (numOfSide(BLUE) == (playboard.length * playboard.length)) {
            e = 1;
        }
        if (numOfSide(RED) == (playboard.length * playboard.length)) {
            e = 2;
        }
        if (numOfSide(WHITE) >= 1) {
            e = 3;
        }
        if ((numOfSide(RED) != (playboard.length * playboard.length))
                && (numOfSide(BLUE) != (playboard.length * playboard.length))) {
            e = 3;
        }
        if (e == 1) {
            return BLUE;
        } else if (e == 2) {
            return RED;
        } else if (e == 3) {
            return null;
        }
        return null;
    }

    /** Return the number of squares of given SIDE. */
    int numOfSide(Side side) {
        int blu = 0;
        int red = 0;
        int white = 0;
        int counter = 0;
        for (int x = 0; x < size(); x += 1) {
            for (int y = 0; y < size(); y += 1) {
                if (playboard[x][y].getSide() == BLUE) {
                    blu += 1;
                }
                if (playboard[x][y].getSide() == RED) {
                    red += 1;
                }
                if (playboard[x][y].getSide() == WHITE) {
                    white += 1;
                }
            }
        }
        if (side == BLUE) {
            counter = blu;
        }
        if (side == RED) {
            counter = red;
        }
        if (side == WHITE) {
            counter = white;
        }
        return counter;
    }

    /** Add a spot from PLAYER at row R, column C.  Assumes
     *  isLegal(PLAYER, R, C). */
    void addSpot(Side player, int r, int c) {
        markUndo();
        int spots = playboard[r - 1][c - 1].getSpots();
        movecounter += 1;
        simpleAdd(player, r, c, 1);
        jump(sqNum(r, c));
    }

    /** Add a spot from PLAYER at square #N.  Assumes isLegal(PLAYER, N). */
    void addSpot(Side player, int n) {
        markUndo();
        set(row(n), col(n), get(n).getSpots() + 1, player);
        jump(n);
    }

    /** Set the square at row R, column C to NUM spots (0 <= NUM), and give
     *  it color PLAYER if NUM > 0 (otherwise, white). */
    void set(int r, int c, int num, Side player) {
        internalSet(r, c, num, player);
        announce();
    }

    /** Set the square at row R, column C to NUM spots (0 <= NUM), and give
     *  it color PLAYER if NUM > 0 (otherwise, white).  Does not announce
     *  changes. */
    private void internalSet(int r, int c, int num, Side player) {
        internalSet(sqNum(r, c), num, player);
    }

    /** Set the square #N to NUM spots (0 <= NUM), and give it color PLAYER
     *  if NUM > 0 (otherwise, white). Does not announce changes. */
    private void internalSet(int n, int num, Side player) {
        if (num > 0) {
            playboard[row(n) - 1][col(n) - 1] = Square.square(player, num);
        } else {
            playboard[row(n) - 1][col(n) - 1] = Square.square(WHITE, 1);
        }
    }


    /** Undo the effects of one move (that is, one addSpot command).  One
     *  can only undo back to the last point at which the undo history
     *  was cleared, or the construction of this Board. */
    void undo() {
        if (phistory.size() != 0 && phistory.size() > 0) {
            copy(phistory.remove(phistory.size() - 1));
        }
    }

    /** Record the beginning of a move in the undo history. */
    private void markUndo() {
        phistory.add(new Board(this));
    }

    /** Add DELTASPOTS spots of side PLAYER to row R, column C,
     *  updating counts of numbers of squares of each color. */
    private void simpleAdd(Side player, int r, int c, int deltaSpots) {
        internalSet(r, c, deltaSpots + get(r, c).getSpots(), player);
    }

    /** Add DELTASPOTS spots of color PLAYER to square #N,
     *  updating counts of numbers of squares of each color. */
    private void simpleAdd(Side player, int n, int deltaSpots) {
        internalSet(n, deltaSpots + get(n).getSpots(), player);
    }

    /** Used in jump to keep track of squares needing processing.  Allocated
     *  here to cut down on allocations. */
    private final ArrayDeque<Integer> _workQueue = new ArrayDeque<>();

    /** Do all jumping on this board, assuming that initially, S is the only
     *  square that might be over-full. */
    private void jump(int S) {
        if (get(S).getSpots() > neighbors(S)) {
            if (exists(row(S), col(S))) {
                if (exists(row(S), col(S) - 1)) {
                    simpleAdd(get(S).getSide(), row(S), col(S) - 1, 1);
                    if (getWinner() == null && neighbors(sqNum(row(S),
                            col(S) - 1))
                            < get(sqNum(row(S), col(S) - 1)).getSpots()) {
                        _workQueue.add(sqNum(row(S), col(S) - 1));
                    }
                }
                if (exists(row(S), col(S) + 1)) {
                    simpleAdd(get(S).getSide(), row(S), col(S) + 1, 1);
                    if (neighbors(sqNum(row(S), col(S) + 1))
                            < get(sqNum(row(S), col(S) + 1)).getSpots()) {
                        _workQueue.add(sqNum(row(S), col(S) + 1));
                    }
                }
                if (exists(row(S) + 1, col(S))) {
                    simpleAdd(get(S).getSide(), row(S) + 1, col(S), 1);
                    if (neighbors(sqNum(row(S) + 1, col(S)))
                            < get(sqNum(row(S) + 1, col(S))).getSpots()) {
                        _workQueue.add(sqNum(row(S) + 1, col(S)));
                    }
                }
                if (exists(row(S) - 1, col(S))) {
                    simpleAdd(get(S).getSide(), row(S) - 1, col(S), 1);
                    if (neighbors(sqNum(row(S) - 1, col(S)))
                            < get(sqNum(row(S) - 1, col(S))).getSpots()) {
                        _workQueue.add(sqNum(row(S) - 1, col(S)));
                    }
                }
            }
            set(row(S), col(S), get(S).getSpots() - neighbors(S),
                    get(S).getSide());
        }
        if (_workQueue.size() > 0) {
            for (int x = 0; x < _workQueue.size(); x += 1) {
                jump(_workQueue.pollFirst());
            }
        }
    }

    /** Returns my dumped representation. */
    @Override
    public String toString() {
        Formatter out = new Formatter();
        out.format("===\n");
        for (int x = 0; x < size(); x += 1) {
            out.format("    ");
            for (int y = 0; y < size(); y += 1) {
                if (playboard[x][y].getSide() == BLUE) {
                    out.format(String.valueOf(playboard[x][y].getSpots())
                            + "b ");
                } else if (playboard[x][y].getSide() == RED) {
                    out.format(String.valueOf(playboard[x][y].getSpots())
                            + "r ");
                } else if (playboard[x][y].getSide() == WHITE) {
                    out.format(String.valueOf(playboard[x][y].getSpots())
                            + "- ");
                }
            }
            out.format("%n");
        }
        out.format("===");
        return out.toString();
    }

    /** Returns an external rendition of me, suitable for human-readable
     *  textual display, with row and column numbers.  This is distinct
     *  from the dumped representation (returned by toString). */
    public String toDisplayString() {
        String[] lines = toString().trim().split("\\R");
        Formatter out = new Formatter();
        for (int i = 1; i + 1 < lines.length; i += 1) {
            out.format("%2d %s%n", i, lines[i].trim());
        }
        out.format("  ");
        for (int i = 1; i <= size(); i += 1) {
            out.format("%3d", i);
        }
        return out.toString();
    }

    /** Returns the number of neighbors of the square at row R, column C. */
    int neighbors(int r, int c) {
        int size = size();
        int n;
        n = 0;
        if (r > 1) {
            n += 1;
        }
        if (c > 1) {
            n += 1;
        }
        if (r < size) {
            n += 1;
        }
        if (c < size) {
            n += 1;
        }
        return n;
    }

    /** Returns the number of neighbors of square #N. */
    int neighbors(int n) {
        return neighbors(row(n), col(n));
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Board)) {
            return false;
        } else {
            Board B = (Board) obj;
            return this == obj;
        }
    }

    @Override
    public int hashCode() {
        return numPieces();
    }

    /** Set my notifier to NOTIFY. */
    public void setNotifier(Consumer<Board> notify) {
        _notifier = notify;
        announce();
    }

    /** Take any action that has been set for a change in my state. */
    private void announce() {
        _notifier.accept(this);
    }

    /** A notifier that does nothing. */
    private static final Consumer<Board> NOP = (s) -> { };

    /** A read-only version of this Board. */
    private ConstantBoard _readonlyBoard;

    /** Use _notifier.accept(B) to announce changes to this board. */
    private Consumer<Board> _notifier;


}

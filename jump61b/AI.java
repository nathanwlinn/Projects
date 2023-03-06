
package jump61;

import java.util.Random;

import static jump61.Side.*;

/** An automated Player.
 *  @author P. N. Hilfinger
 */
class AI extends Player {

    /** A new player of GAME initially COLOR that chooses moves automatically.
     *  SEED provides a random-number seed used for choosing moves.
     */
    AI(Game game, Side color, long seed) {
        super(game, color);
        _random = new Random(seed);
    }

    @Override
    String getMove() {
        Board board = getGame().getBoard();

        assert getSide() == board.whoseMove();
        int choice = searchForMove();
        getGame().reportMove(board.row(choice), board.col(choice));
        return String.format("%d %d", board.row(choice), board.col(choice));
    }

    /** Return a move after searching the game tree to DEPTH>0 moves
     *  from the current position. Assumes the game is not over. */
    private int searchForMove() {
        Board work = new Board(getBoard());
        int value;
        assert getSide() == work.whoseMove();
        _foundMove = -1;
        if (getSide() == RED) {
            value = minMax(work, 4, true, 1,
                    -(Integer.MAX_VALUE), Integer.MAX_VALUE);
        } else {
            value = minMax(work, 4, true, -1,
                    -(Integer.MAX_VALUE), Integer.MAX_VALUE);
        }
        if (_foundMove != -1 && work.getWinner() == null) {
            return _foundMove;
        } else {
            for (int x = 0; x < (work.size() * work.size()); x += 1) {
                if (work.isLegal(getSide(), x)) {
                    _foundMove = x;
                }
            }
        }
        return _foundMove;
    }


    /** Find a move from position BOARD and return its value, recording
     *  the move found in _foundMove iff SAVEMOVE. The move
     *  should have maximal value or have value > BETA if SENSE==1,
     *  and minimal value or value < ALPHA if SENSE==-1. Searches up to
     *  DEPTH levels.  Searching at level 0 simply returns a static estimate
     *  of the board value and does not set _foundMove. If the game is over
     *  on BOARD, does not set _foundMove. */
    private int minMax(Board board, int depth, boolean saveMove,
                       int sense, int alpha, int beta) {
        Side filler = null; int holder = 0; int score = 0;
        boolean comparer = false;
        int area = board.size() * board.size();
        if (board.getWinner() == RED && sense == -1) {
            return staticEval(board, Integer.MAX_VALUE);
        } else if (board.getWinner() == BLUE && sense == 1) {
            return staticEval(board, Integer.MIN_VALUE);
        }
        if (depth == 0) {
            return staticEval(board, Integer.MAX_VALUE);
        }
        if (sense == -1) {
            filler = BLUE; holder = beta;
            score = Integer.MAX_VALUE; comparer = false;
        } else {
            filler = RED; holder = alpha;
            score = Integer.MIN_VALUE; comparer = true;
        }
        if (depth != 0 && depth > 0) {
            for (int x = 0; x < area; x += 1) {
                if (board.isLegal(filler, x)) {
                    board.addSpot(filler, x);
                    int e = minMax(board, depth - 1,
                            false, (sense * -1), alpha, beta);
                    board.undo();
                    if (!comparer) {
                        if (e < score) {
                            score = e;
                            if (beta > score) {
                                beta = score;
                            }
                            if (saveMove) {
                                _foundMove = x;
                            }
                            if (beta <= alpha) {
                                break;
                            }
                        }
                    }
                    if (comparer) {
                        if (e > score) {
                            score = e;
                            if (alpha < score) {
                                alpha = score;
                            }
                            if (saveMove) {
                                _foundMove = x;
                            }
                            if (beta <= alpha) {
                                break;
                            }
                        }
                    }
                }
            }
        }
        return score;
    }

    /** Return a heuristic estimate of the value of board position B.
     *  Use WINNINGVALUE to indicate a win for Red and -WINNINGVALUE to
     *  indicate a win for Blue. */
    private int staticEval(Board b, int winningValue) {
        int filler1 = 0;
        int filler2 = 0;
        Side sider = WHITE;
        if (b.getWinner() == RED) {
            return Integer.MAX_VALUE;
        } else if (b.getWinner() == BLUE) {
            return (Integer.MAX_VALUE) * (-1);
        }
        return b.numOfSide(RED) - b.numOfSide(BLUE);
    }

    /** A random-number generator used for move selection. */
    private Random _random;

    /** Used to convey moves discovered by minMax. */
    private int _foundMove;
}

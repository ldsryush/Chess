package chess;

import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;
import java.util.Vector;

/**
 * Represents a single chess piece
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessPiece {
    private ChessGame.TeamColor color;
    private PieceType type;

    public ChessPiece(ChessGame.TeamColor pieceColor, PieceType type) {
        this.color = pieceColor;
        this.type = type;
    }

    /**
     * The various different chess piece options
     */
    public enum PieceType {
        KING,
        QUEEN,
        BISHOP,
        KNIGHT,
        ROOK,
        PAWN
    }

    /**
     * @return Which team this chess piece belongs to
     */
    public ChessGame.TeamColor getTeamColor() {
        return this.color;
    }

    /**
     * @return which type of chess piece this piece is
     */
    public PieceType getPieceType() {
        return this.type;
    }

    /**
     * Calculates all the positions a chess piece can move to
     * Does not take into account moves that are illegal due to leaving the king in
     * danger
     *
     * @return Collection of valid moves
     */
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {
//        Initialize a collection of the moves
        Collection<ChessMove> valid_moves = new HashSet<>(0);

//        Determine the type of piece to know how to move
        ChessPiece piece = board.getPiece(myPosition);

//        Store the row and column
        int row = myPosition.getRow();
        int col = myPosition.getColumn();

//        PAWN
        if (piece.getPieceType() == PieceType.PAWN) {
            int dir = 1;
            if (piece.getTeamColor() == ChessGame.TeamColor.BLACK) {
                dir = -1;
            }

//            Check if the pawn can move forwards
            ChessPosition newPosition = new ChessPosition(row+dir, col);
            if (board.getPiece(newPosition) == null) {
                valid_moves.add(new ChessMove(myPosition, newPosition, null));
            }

//            Check if the pawn can KO a piece diagonal to it on one direction
            newPosition = new ChessPosition(row+dir, col+1);
            if (board.getPiece(newPosition).getTeamColor() != piece.getTeamColor()) {
                valid_moves.add(new ChessMove(myPosition, newPosition, null));
            }

//            Check if the pawn can KO a piece diagonal to it on the other direction
            newPosition = new ChessPosition(row+dir, col-1);
            if (board.getPiece(newPosition).getTeamColor() != piece.getTeamColor()) {
                valid_moves.add(new ChessMove(myPosition, newPosition, null));
            }
        }

//        BISHOP
        else if (piece.getPieceType() == PieceType.BISHOP) {
            ChessPosition newPosition;

            int[] dirs = {-1, 1};
            for (var up : dirs){
                for (var side : dirs) {
                    for (int i=1; i < 8; i++) {
                        newPosition = new ChessPosition(row + up*i, col + side*i);
                        if (row + up*i > 8 || row + up*i < 1 || col + side*i > 8 || col + side*i < 1) {
                            break;
                        }
                        if (board.getPiece(newPosition) == null) {
                            valid_moves.add(new ChessMove(myPosition, newPosition, null));
                        } else if (board.getPiece(newPosition).getTeamColor() != piece.getTeamColor()) {
                            valid_moves.add(new ChessMove(myPosition, newPosition, null));
                            break;
                        } else {
                            break;
                        }
                    }

                }
            }

        }

//        KING
        else if (piece.getPieceType() == PieceType.KING) {
            int endRow;
            int endCol;
            ChessPosition newPosition;

            int[] dirs = {-1, 0, 1};
            for (var up : dirs) {
                for (var side : dirs) {
                    endRow = row + up;
                    endCol = col + side;
                    newPosition = new ChessPosition(endRow, endCol);

                    if (endRow > 8 || endRow < 1 || endCol > 8 || endCol < 1) {
                        break;
                    }

                    if (board.getPiece(newPosition) == null || board.getPiece(newPosition).getTeamColor() != piece.getTeamColor()) {
                        valid_moves.add(new ChessMove(myPosition, newPosition, null));
                    }
                }
            }
        }

        return valid_moves;
    }

    @Override
    public String toString() {
        return "ChessPiece{" +
                "color=" + color +
                ", type=" + type +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ChessPiece that = (ChessPiece) o;
        return color == that.color && type == that.type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(color, type);
    }
}
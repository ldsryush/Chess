package chess;

import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;

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

        int endRow;
        int endCol;

        ChessPosition newPosition;

//        PAWN
        if (piece.getPieceType() == PieceType.PAWN) {
            PieceType[] types = {PieceType.QUEEN, PieceType.KNIGHT, PieceType.ROOK, PieceType.BISHOP};

//            Get the color to find direction of movement
            int dir = 1;
            int dist = 1;
            boolean prom = false;
            if (piece.getTeamColor() == ChessGame.TeamColor.BLACK) {
                dir = -1;
                if (myPosition.getRow() == 7) {
                    dist = 2;
                } else if (myPosition.getRow() == 2) {
                    prom = true;
                }
            } else {
                if (myPosition.getRow() == 2) {
                    dist = 2;
                } else if (myPosition.getRow() == 7) {
                    prom = true;
                }
            }

//            Check if the pawn can move forwards (must be empty)
            newPosition = new ChessPosition(row+dir, col);
            if (board.getPiece(newPosition) == null) {
                if (prom) {
                    for (var type : types) {
                        valid_moves.add(new ChessMove(myPosition, newPosition, type));
                    }
                } else {
                    valid_moves.add(new ChessMove(myPosition, newPosition, null));
                    if (dist == 2) {
                        newPosition = new ChessPosition(row + dist * dir, col);
                        if (board.getPiece(newPosition) == null) {
                            valid_moves.add(new ChessMove(myPosition, newPosition, null));
                        }
                    }
                }
            }

//            Check if the pawn can KO a piece diagonal to it on one direction
            int targetCol = col + 1;
            if (targetCol <= 8) {
                newPosition = new ChessPosition(row+dir, targetCol);
                ChessPiece target = board.getPiece(newPosition);
                if (target != null && target.getTeamColor() != piece.getTeamColor()) {
                    if (prom) {
                        for (var type : types) {
                            valid_moves.add(new ChessMove(myPosition, newPosition, type));
                        }
                    } else {
                        valid_moves.add(new ChessMove(myPosition, newPosition, null));
                    }
                }
            }
            targetCol = col - 1;
            if (targetCol >= 1) {
                newPosition = new ChessPosition(row + dir, targetCol);
                ChessPiece target = board.getPiece(newPosition);
                if (target != null && target.getTeamColor() != piece.getTeamColor()) {
                    if (prom) {
                        for (var type : types) {
                            valid_moves.add(new ChessMove(myPosition, newPosition, type));
                        }
                    } else {
                        valid_moves.add(new ChessMove(myPosition, newPosition, null));
                    }
                }
            }


//            Check if the pawn can KO a piece diagonal to it on the other direction
            newPosition = new ChessPosition(row+dir, col-1);
            if (board.getPiece(newPosition) != null) {
                if (board.getPiece(newPosition).getTeamColor() != piece.getTeamColor()) {
                    if (prom) {
                        for (var type : types) {
                            valid_moves.add(new ChessMove(myPosition, newPosition, type));
                        }
                    } else {
                        valid_moves.add(new ChessMove(myPosition, newPosition, null));
                    }
                }
            }
        }


//        BISHOP
        if (piece.getPieceType() == PieceType.BISHOP || piece.getPieceType() == PieceType.QUEEN) {
            int[] dirs = {-1, 1};
            for (var up : dirs){
                for (var side : dirs) {
                    for (int i=1; i < 8; i++) {
                        newPosition = new ChessPosition(row + up*i, col + side*i);

//                        In bounds?
                        if (row + up*i > 8 || row + up*i < 1 || col + side*i > 8 || col + side*i < 1) {
                            break;
                        }

//                        Empty?
                        if (board.getPiece(newPosition) == null) {
                            valid_moves.add(new ChessMove(myPosition, newPosition, null));
                        }

//                        Can steal?
                        else if (board.getPiece(newPosition).getTeamColor() != piece.getTeamColor()) {
                            valid_moves.add(new ChessMove(myPosition, newPosition, null));
                            break;
                        }

//                        Can't go there?
                        else {
                            break;
                        }
                    }

                }
            }

        }

//        KING
        if (piece.getPieceType() == PieceType.KING) {
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

//        ROOK
        if (piece.getPieceType() == PieceType.ROOK || piece.getPieceType() == PieceType.QUEEN) {
            int[] dirs = {-1, 1};
            for (var dir : dirs) {
//                Check vertical direction
                for (int i=1; i < 8; i++) {
                    endRow = row + i * dir;
                    if (endRow > 8 || endRow < 1) {
                        break;
                    }
                    newPosition = new ChessPosition(endRow, col);
                    if (board.getPiece(newPosition) == null) {
                        valid_moves.add(new ChessMove(myPosition, newPosition, null));
                    } else if (board.getPiece(newPosition).getTeamColor() != piece.getTeamColor()) {
                        valid_moves.add(new ChessMove(myPosition, newPosition, null));
                        break;
                    } else {
                        break;
                    }
                }

//                Check horizontal direction
                for (int i=1; i < 8; i++) {
                    endCol = col + i * dir;
                    if (endCol > 8 || endCol < 1) {
                        break;
                    }

                    newPosition = new ChessPosition(row, endCol);

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


//        KNIGHT
        if (piece.getPieceType() == PieceType.KNIGHT) {
            int[] dirs = {-1, 1};

            for (var dir_r : dirs) {
                for (var dir_c : dirs) {
                    endRow = row + 2*dir_r;
                    endCol = col + dir_c;

                    if (!(endRow > 8 || endRow < 1 || endCol > 8 || endCol < 1)) {
                        newPosition = new ChessPosition(endRow, endCol);

                        if (board.getPiece(newPosition) == null || board.getPiece(newPosition).getTeamColor() != piece.getTeamColor()) {
                            valid_moves.add(new ChessMove(myPosition, newPosition, null));
                        }
                    }



                    endRow = row + dir_c;
                    endCol = col + 2*dir_r;

                    if (!(endRow > 8 || endRow < 1 || endCol > 8 || endCol < 1)) {
                        newPosition = new ChessPosition(endRow, endCol);

                        if (board.getPiece(newPosition) == null || board.getPiece(newPosition).getTeamColor() != piece.getTeamColor()) {
                            valid_moves.add(new ChessMove(myPosition, newPosition, null));
                        }
                    }

                }
            }

        }


//        QUEEN
//        else if (piece.getPieceType() == PieceType.QUEEN) {
//
//        }

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
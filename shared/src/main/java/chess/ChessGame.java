package chess;

import java.util.*;

/**
 * For a class that can manage a chess game, making moves on a board
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessGame {
    private TeamColor turn;
    private ChessBoard board;

    public ChessGame() {
        this.turn=TeamColor.WHITE;
        this.board=new ChessBoard();
        this.board.resetBoard();
    }
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ChessGame other)) return false;
        return this.turn == other.turn && this.board.equals(other.board);
    }

    @Override
    public int hashCode() {
        return Objects.hash(turn, board);
    }

    /**
     * @return Which team's turn it is
     */
    public TeamColor getTeamTurn() {
        return this.turn;
    }

    /**
     * Set's which teams turn it is
     *
     * @param team the team whose turn it is
     */
    public void setTeamTurn(TeamColor team) {
        this.turn=team;
    }

    /**
     * Enum identifying the 2 possible teams in a chess game
     */
    public enum TeamColor {
        WHITE,
        BLACK
    }

    /**
     * Gets a valid moves for a piece at the given location
     *
     * @param startPosition the piece to get valid moves for
     * @return Set of valid moves for requested piece, or null if no piece at
     * startPosition
     */
    public Collection<ChessMove> validMoves(ChessPosition startPosition) {
        Collection<ChessMove> allMoves = new HashSet<>(0);
        if (this.board.getPiece(startPosition) == null) {
            return allMoves;
        } else {
            allMoves = this.board.getPiece(startPosition).pieceMoves(this.board, startPosition);
            Collection<ChessMove> onlyValid = new HashSet<>(0);
            for (var move : allMoves) {
                ChessPiece piece = this.board.getPiece(move.getStartPosition());
                ChessPiece targetPiece = this.board.getPiece(move.getEndPosition());

                this.board.addPiece(move.getEndPosition(), piece);
                this.board.removePiece(move.getStartPosition());

                if (!this.isInCheck(piece.getTeamColor())) {
                    onlyValid.add(move);
                }

                this.board.addPiece(move.getStartPosition(), piece);
                this.board.addPiece(move.getEndPosition(), targetPiece);
            }
            return onlyValid;
        }
    }

    /**
     * Makes a move in a chess game
     *
     * @param move chess move to preform
     * @throws InvalidMoveException if move is invalid
     */
    public void makeMove(ChessMove move) throws InvalidMoveException {
        TeamColor turn = getTeamTurn();
        ChessPiece piece = this.board.getPiece(move.getStartPosition());
        if (piece == null) {
            throw new InvalidMoveException("No piece at the start position");
        }
        TeamColor color = piece.getTeamColor();
        Collection<ChessMove> possibleMoves = piece.pieceMoves(this.board, move.getStartPosition());
        if (color != turn) {
            throw new InvalidMoveException("Not this team's turn to move");
        } if (this.board.getPiece(move.getEndPosition()) != null) {
            if ((this.board.getPiece(move.getEndPosition()).getTeamColor() == color)) {
                throw new InvalidMoveException("Can't move to a position with a piece of the same team");
            }
        } if (!possibleMoves.contains(move)){
            throw new InvalidMoveException("Not a possible move for that piece");
        } else {
            ChessPiece newPiece = piece;
            if (move.getPromotionPiece() != null) {
                newPiece = new ChessPiece(color, move.getPromotionPiece());
            }
            ChessPiece takenPiece = this.board.getPiece(move.getEndPosition());
            this.board.addPiece(move.getEndPosition(), newPiece);
            this.board.removePiece(move.getStartPosition());

            if (this.isInCheck(color)) {
                this.board.addPiece(move.getEndPosition(), takenPiece);
                this.board.addPiece(move.getStartPosition(), piece);
                throw new InvalidMoveException("This move will leave the king in check");
            }

            if (this.getTeamTurn() == TeamColor.WHITE) {
                this.setTeamTurn(TeamColor.BLACK);
            } else {
                this.setTeamTurn(TeamColor.WHITE);
            }
        }
    }

    /**
     * Determines if the given team is in check
     *
     * @param teamColor which team to check for check
     * @return True if the specified team is in check
     */
    public boolean isInCheck(TeamColor teamColor) {
        ChessPiece piece;
        ChessPosition kingPosition = null;
        ChessPosition currPosition;
        for (int row = 1; row <= 8; row++) {
            for (int col = 1; col <= 8; col++) {
                currPosition = new ChessPosition(row, col);
                if (this.board.getPiece(currPosition) != null) {
                    piece = this.board.getPiece(currPosition);
                    if (piece.getTeamColor() == teamColor && piece.getPieceType() == ChessPiece.PieceType.KING) {
                        // Found the king! Saving its position...
                        kingPosition = currPosition;
                        break;
                    }
                }
            }
        }
        for (int row = 1; row <= 8; row++) {
            for (int col = 1; col <= 8; col++) {
                currPosition = new ChessPosition(row, col);
                if (this.board.getPiece(currPosition) != null) {

                    piece = this.board.getPiece(currPosition);
                    if (piece.getTeamColor() != teamColor) {
                        if (piece.pieceMoves(this.board, currPosition).contains(new ChessMove(currPosition, kingPosition, null))) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    /**
     * Determines if the given team is in checkmate
     *
     * @param teamColor which team to check for checkmate
     * @return True if the specified team is in checkmate
     */
    public boolean isInCheckmate(TeamColor teamColor) {
        if (!isInCheck(teamColor)) {
            return false;
        }

        for (int row = 1; row <= 8; row++) {
            for (int col = 1; col <= 8; col++) {
                ChessPosition position = new ChessPosition(row, col);

                if (this.board.getPiece(position) != null) {
                    if (this.board.getPiece(position).getTeamColor() == teamColor) {
                        if (!this.validMoves(position).isEmpty()) {
                            return false;
                        }
                    }
                }
            }
        }

        return true;
    }

    /**
     * Determines if the given team is in stalemate, which here is defined as having
     * no valid moves
     *
     * @param teamColor which team to check for stalemate
     * @return True if the specified team is in stalemate, otherwise false
     */
    public boolean isInStalemate(TeamColor teamColor) {
        ChessPosition myPosition;
        if (this.getTeamTurn() == teamColor) {
            for (int row = 1; row <= 8; row++) {
                for (int col = 1; col <= 8; col++) {
                    myPosition = new ChessPosition(row, col);
                    if (this.board.getPiece(myPosition) != null) {
                        if (this.board.getPiece(myPosition).getTeamColor() == teamColor) {
                            if (!this.validMoves(myPosition).isEmpty()) {
                                return false;
                            }
                        }
                    }
                }
            }
            return true;
        } else {
            return false;
        }
    }

    /**
     * Sets this game's chessboard with a given board
     *
     * @param board the new board to use
     */
    public void setBoard(ChessBoard board) {
        this.board = board;
    }

    /**
     * Gets the current chessboard
     *
     * @return the chessboard
     */
    public ChessBoard getBoard() {
        return this.board;
    }
}
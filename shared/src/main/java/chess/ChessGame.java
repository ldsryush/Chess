package chess;

import java.util.Collection;

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
        if (this.board.getPiece(startPosition)==null) {
            return null;
        } else {
            return this.board.getPiece(startPosition).pieceMoves(this.board,startPosition);
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
        ChessPiece piece=this.board.getPiece(move.getStartPosition());
        TeamColor color=piece.getTeamColor();
        Collection<ChessMove> possibleMoves=piece.pieceMoves(this.board,move.getStartPosition());
        if (color!=turn) {
            throw new InvalidMoveException("Not your turn");
        } else if (this.board.getPiece(move.getEndPosition())!=null) {
            if ((this.board.getPiece(move.getEndPosition()).getTeamColor()==color)) {
                throw new InvalidMoveException("Can't move to position with existing team piece");
            }
        } else if (!possibleMoves.contains(move)) {
            throw new InvalidMoveException("Not possible move");
        } else if (this.isInCheck(color)) {
            throw new InvalidMoveException("This move will leave the King in check");
        } else {
            this.board.addPiece(move.getEndPosition(), piece);
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
        ChessPiece king;
        ChessPosition kingPosition = null;
        ChessPosition currPosition = null;
        for (int row = 1; row <= 8; row++) {
            for (int col = 1; col <= 8; col++) {
                currPosition = new ChessPosition(row, col);
                if (this.board.getPiece(currPosition) != null) {
                    piece = this.board.getPiece(currPosition);
                    if (piece.getTeamColor() == teamColor && piece.getPieceType() == ChessPiece.PieceType.KING) {
                        king = piece;
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
        return isInCheck(teamColor);
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
                    if (!this.validMoves(myPosition).isEmpty()) {
                        return false;
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
        this.board=board;
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
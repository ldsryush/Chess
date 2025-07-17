package chess;

import java.util.*;

public class ChessGame {
    private TeamColor turn;
    private ChessBoard board;
    private ChessMove lastMove;
    private final Set<ChessPosition> hasMoved = new HashSet<>();

    public ChessGame() {
        this.turn = TeamColor.WHITE;
        this.board = new ChessBoard();
        this.board.resetBoard();
        this.lastMove = null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ChessGame other)) {
            return false;
        }
        return this.turn == other.turn && this.board.equals(other.board);
    }

    @Override
    public int hashCode() {
        return Objects.hash(turn, board);
    }

    public TeamColor getTeamTurn() {
        return this.turn;
    }

    public void setTeamTurn(TeamColor team) {
        this.turn = team;
    }

    public enum TeamColor {
        WHITE, BLACK
    }

    public ChessMove getLastMove() {
        return lastMove;
    }

    public Collection<ChessMove> validMoves(ChessPosition startPosition) {
        Collection<ChessMove> allMoves = new HashSet<>();
        ChessPiece currentPiece = board.getPiece(startPosition);
        if (currentPiece == null) {
            return allMoves;
        }

        // 1) Base moves
        allMoves = currentPiece.pieceMoves(board, startPosition);

        // 2) En passant
        addEnPassantMoves(currentPiece, startPosition, allMoves);

        // 3) Filter out moves that leave king in check
        Collection<ChessMove> onlyValid = filterLegalMoves(allMoves, currentPiece);

        // 4) Castling
        addCastlingMoves(currentPiece, startPosition, onlyValid);

        return onlyValid;
    }

    private void addEnPassantMoves(ChessPiece currentPiece,
                                   ChessPosition startPosition,
                                   Collection<ChessMove> allMoves) {
        if (currentPiece.getPieceType() != ChessPiece.PieceType.PAWN
                || lastMove == null) {
            return;
        }

        ChessPiece lastMoved = board.getPiece(lastMove.getEndPosition());
        if (lastMoved == null
                || lastMoved.getPieceType() != ChessPiece.PieceType.PAWN
                || lastMoved.getTeamColor() == currentPiece.getTeamColor()) {
            return;
        }

        int lastStart = lastMove.getStartPosition().getRow();
        int lastEnd   = lastMove.getEndPosition().getRow();
        int lastCol   = lastMove.getEndPosition().getColumn();
        int currRow   = startPosition.getRow();
        int currCol   = startPosition.getColumn();

        if (Math.abs(lastEnd - lastStart) == 2
                && lastEnd == currRow
                && Math.abs(lastCol - currCol) == 1) {

            int dir = (currentPiece.getTeamColor() == TeamColor.WHITE) ? 1 : -1;
            ChessPosition cap = new ChessPosition(currRow + dir, lastCol);
            if (board.getPiece(cap) == null) {
                allMoves.add(new ChessMove(startPosition, cap, null));
            }
        }
    }

    private Collection<ChessMove> filterLegalMoves(Collection<ChessMove> allMoves,
                                                   ChessPiece currentPiece) {
        Collection<ChessMove> onlyValid = new HashSet<>();
        for (ChessMove move : allMoves) {
            ChessPiece orig    = board.getPiece(move.getStartPosition());
            ChessPiece target  = board.getPiece(move.getEndPosition());
            ChessPiece simPiece = (move.getPromotionPiece() != null)
                    ? new ChessPiece(orig.getTeamColor(), move.getPromotionPiece())
                    : orig;

            board.addPiece(move.getEndPosition(), simPiece);
            board.removePiece(move.getStartPosition());

            if (orig.getPieceType() == ChessPiece.PieceType.PAWN
                    && move.getStartPosition().getColumn()
                    != move.getEndPosition().getColumn()
                    && target == null) {

                int dir = (orig.getTeamColor() == TeamColor.WHITE) ? -1 : 1;
                ChessPosition vic = new ChessPosition(
                        move.getEndPosition().getRow() + dir,
                        move.getEndPosition().getColumn()
                );
                board.removePiece(vic);
            }

            if (!isInCheck(simPiece.getTeamColor())) {
                onlyValid.add(move);
            }

            board.addPiece(move.getStartPosition(), orig);
            board.addPiece(move.getEndPosition(), target);

            if (orig.getPieceType() == ChessPiece.PieceType.PAWN
                    && move.getStartPosition().getColumn()
                    != move.getEndPosition().getColumn()
                    && target == null) {

                int dir = (orig.getTeamColor() == TeamColor.WHITE) ? -1 : 1;
                ChessPosition vic = new ChessPosition(
                        move.getEndPosition().getRow() + dir,
                        move.getEndPosition().getColumn()
                );
                board.addPiece(
                        vic,
                        new ChessPiece(
                                this.turn == TeamColor.WHITE ? TeamColor.BLACK : TeamColor.WHITE,
                                ChessPiece.PieceType.PAWN
                        )
                );
            }
        }
        return onlyValid;
    }

    private void addCastlingMoves(ChessPiece currentPiece,
                                  ChessPosition startPosition,
                                  Collection<ChessMove> onlyValid) {
        if (currentPiece.getPieceType() != ChessPiece.PieceType.KING
                || hasMoved.contains(startPosition)
                || isInCheck(currentPiece.getTeamColor())) {
            return;
        }

        int row  = startPosition.getRow();
        TeamColor team = currentPiece.getTeamColor();

        tryCastle(startPosition, row, new int[]{6, 7}, 8, team, onlyValid);
        tryCastle(startPosition, row, new int[]{4, 3}, 1, team, onlyValid);
    }

    private void tryCastle(ChessPosition start, int row,
                           int[] pathCols, int rookCol,
                           TeamColor team,
                           Collection<ChessMove> out) {
        ChessPosition rookPos = new ChessPosition(row, rookCol);
        ChessPiece rook = board.getPiece(rookPos);
        if (hasMoved.contains(rookPos)
                || rook == null
                || rook.getPieceType() != ChessPiece.PieceType.ROOK) {
            return;
        }

        for (int c : pathCols) {
            if (board.getPiece(new ChessPosition(row, c)) != null) {
                return;
            }
        }

        ChessPosition[] path = new ChessPosition[pathCols.length + 1];
        path[0] = start;
        for (int i = 0; i < pathCols.length; i++) {
            path[i + 1] = new ChessPosition(row, pathCols[i]);
        }

        if (isPathSafe(team, path)) {
            out.add(new ChessMove(start, path[path.length - 1], null));
        }
    }

    private boolean isPathSafe(TeamColor team, ChessPosition[] positions) {
        ChessPiece saved = board.getPiece(positions[0]);
        board.removePiece(positions[0]);

        boolean safe = true;
        for (ChessPosition pos : positions) {
            ChessPiece temp = board.getPiece(pos);
            board.removePiece(pos);
            board.addPiece(pos, new ChessPiece(team, ChessPiece.PieceType.KING));
            if (isInCheck(team)) {
                safe = false;
            }
            board.removePiece(pos);
            if (temp != null) {
                board.addPiece(pos, temp);
            }
        }

        board.addPiece(positions[0], saved);
        return safe;
    }

    public void makeMove(ChessMove move) throws InvalidMoveException {
        TeamColor turn = getTeamTurn();
        ChessPiece piece = board.getPiece(move.getStartPosition());
        if (piece == null) {
            throw new InvalidMoveException("No piece at the start position");
        }
        if (piece.getTeamColor() != turn) {
            throw new InvalidMoveException("Not this team's turn to move");
        }
        ChessPiece dest = board.getPiece(move.getEndPosition());
        if (dest != null && dest.getTeamColor() == turn) {
            throw new InvalidMoveException(
                    "Can't move to a position with a piece of the same team"
            );
        }

        Collection<ChessMove> possibleMoves = validMoves(move.getStartPosition());
        if (!possibleMoves.contains(move)) {
            throw new InvalidMoveException("Not a possible move for that piece");
        }

        ChessPiece newPiece = (move.getPromotionPiece() != null)
                ? new ChessPiece(turn, move.getPromotionPiece())
                : piece;

        hasMoved.add(move.getStartPosition());

        if (piece.getPieceType() == ChessPiece.PieceType.PAWN
                && move.getStartPosition().getColumn()
                != move.getEndPosition().getColumn()
                && board.getPiece(move.getEndPosition()) == null) {
            int dir = (turn == TeamColor.WHITE) ? -1 : 1;
            ChessPosition vic = new ChessPosition(
                    move.getEndPosition().getRow() + dir,
                    move.getEndPosition().getColumn()
            );
            board.removePiece(vic);
        }

        handleCastlingRook(move, piece);

        board.addPiece(move.getEndPosition(), newPiece);
        board.removePiece(move.getStartPosition());
        this.lastMove = move;

        setTeamTurn(turn == TeamColor.WHITE ? TeamColor.BLACK : TeamColor.WHITE);
    }

    private void handleCastlingRook(ChessMove move, ChessPiece piece) {
        if (piece.getPieceType() == ChessPiece.PieceType.KING
                && Math.abs(
                move.getEndPosition().getColumn()
                        - move.getStartPosition().getColumn()
        ) == 2) {
            int row = move.getStartPosition().getRow();
            if (move.getEndPosition().getColumn() == 7) {
                moveRook(row, 8, 6);
            } else {
                moveRook(row, 1, 4);
            }
        }
    }

    private void moveRook(int row, int fromCol, int toCol) {
        ChessPosition from = new ChessPosition(row, fromCol);
        ChessPiece rook = board.getPiece(from);
        hasMoved.add(from);
        board.removePiece(from);
        board.addPiece(new ChessPosition(row, toCol), rook);
    }

    public boolean isInCheck(TeamColor teamColor) {
        ChessPiece piece;
        ChessPosition kingPosition = null;

        // Find the king's position
        for (int row = 1; row <= 8; row++) {
            for (int col = 1; col <= 8; col++) {
                ChessPosition currPosition = new ChessPosition(row, col);
                piece = this.board.getPiece(currPosition);
                if (piece != null &&
                        piece.getTeamColor() == teamColor &&
                        piece.getPieceType() == ChessPiece.PieceType.KING) {
                    kingPosition = currPosition;
                    break;
                }
            }
            if (kingPosition != null) {
                break;
            }
        }

        for (int row = 1; row <= 8; row++) {
            for (int col = 1; col <= 8; col++) {
                ChessPosition currPosition = new ChessPosition(row, col);
                piece = this.board.getPiece(currPosition);

                if (piece == null || piece.getTeamColor() == teamColor) {
                    continue;
                }

                for (ChessMove move : piece.pieceMoves(this.board, currPosition)) {
                    if (move.getEndPosition().equals(kingPosition)) {
                        return true;
                    }
                }
            }
        }

        return false;
    }


    private boolean hasAnyLegalMove(TeamColor teamColor) {
        for (int row = 1; row <= 8; row++) {
            for (int col = 1; col <= 8; col++) {
                ChessPosition pos = new ChessPosition(row, col);
                ChessPiece p = board.getPiece(pos);
                if (p != null && p.getTeamColor() == teamColor) {
                    if (!validMoves(pos).isEmpty()) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public boolean isInCheckmate(TeamColor teamColor) {
        if (!isInCheck(teamColor)) {
            return false;
        }
        return !hasAnyLegalMove(teamColor);
    }

    public boolean isInStalemate(TeamColor teamColor) {
        if (isInCheck(teamColor)) {
            return false;
        }
        return !hasAnyLegalMove(teamColor);
    }

    public void setBoard(ChessBoard board) {
        this.board = board;
    }

    public ChessBoard getBoard() {
        return this.board;
    }
}

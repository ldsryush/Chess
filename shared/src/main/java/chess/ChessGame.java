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
        WHITE,
        BLACK
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

        allMoves = currentPiece.pieceMoves(board, startPosition);
        addEnPassantMoves(currentPiece, startPosition, allMoves);

        Collection<ChessMove> onlyValid = filterLegalMoves(allMoves, currentPiece);
        addCastlingMoves(currentPiece, startPosition, onlyValid);

        return onlyValid;
    }

    private void addEnPassantMoves(ChessPiece currentPiece,
                                   ChessPosition startPosition,
                                   Collection<ChessMove> allMoves) {
        if (currentPiece.getPieceType() != ChessPiece.PieceType.PAWN ||
                lastMove == null) {
            return;
        }

        ChessPiece lastMoved = board.getPiece(lastMove.getEndPosition());
        if (lastMoved == null ||
                lastMoved.getPieceType() != ChessPiece.PieceType.PAWN ||
                lastMoved.getTeamColor() == currentPiece.getTeamColor()) {
            return;
        }

        int lastStartRow = lastMove.getStartPosition().getRow();
        int lastEndRow   = lastMove.getEndPosition().getRow();
        int lastCol      = lastMove.getEndPosition().getColumn();
        int currRow      = startPosition.getRow();
        int currCol      = startPosition.getColumn();

        if (Math.abs(lastEndRow - lastStartRow) == 2 &&
                lastEndRow == currRow &&
                Math.abs(lastCol - currCol) == 1) {

            int direction = (currentPiece.getTeamColor() == TeamColor.WHITE) ? 1 : -1;
            ChessPosition capturePos =
                    new ChessPosition(currRow + direction, lastCol);

            if (board.getPiece(capturePos) == null) {
                allMoves.add(new ChessMove(startPosition, capturePos, null));
            }
        }
    }

    private Collection<ChessMove> filterLegalMoves(Collection<ChessMove> allMoves,
                                                   ChessPiece currentPiece) {
        Collection<ChessMove> onlyValid = new HashSet<>();
        for (ChessMove move : allMoves) {
            ChessPiece originalPiece = board.getPiece(move.getStartPosition());
            ChessPiece targetPiece   = board.getPiece(move.getEndPosition());

            ChessPiece simPiece =
                    (move.getPromotionPiece() != null)
                            ? new ChessPiece(originalPiece.getTeamColor(),
                            move.getPromotionPiece())
                            : originalPiece;

            board.addPiece(move.getEndPosition(), simPiece);
            board.removePiece(move.getStartPosition());

            if (originalPiece.getPieceType() == ChessPiece.PieceType.PAWN &&
                    move.getStartPosition().getColumn()
                            != move.getEndPosition().getColumn() &&
                    targetPiece == null) {

                int dir = (originalPiece.getTeamColor() == TeamColor.WHITE) ? -1 : 1;
                ChessPosition capturedPawn = new ChessPosition(
                        move.getEndPosition().getRow() + dir,
                        move.getEndPosition().getColumn()
                );
                board.removePiece(capturedPawn);
            }

            if (!isInCheck(simPiece.getTeamColor())) {
                onlyValid.add(move);
            }

            board.addPiece(move.getStartPosition(), originalPiece);
            board.addPiece(move.getEndPosition(), targetPiece);

            if (originalPiece.getPieceType() == ChessPiece.PieceType.PAWN &&
                    move.getStartPosition().getColumn()
                            != move.getEndPosition().getColumn() &&
                    targetPiece == null) {

                int dir = (originalPiece.getTeamColor() == TeamColor.WHITE) ? -1 : 1;
                ChessPosition capturedPawn = new ChessPosition(
                        move.getEndPosition().getRow() + dir,
                        move.getEndPosition().getColumn()
                );
                board.addPiece(capturedPawn,
                        new ChessPiece(
                                this.turn == TeamColor.WHITE
                                        ? TeamColor.BLACK
                                        : TeamColor.WHITE,
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
        if (currentPiece.getPieceType() != ChessPiece.PieceType.KING ||
                hasMoved.contains(startPosition) ||
                isInCheck(currentPiece.getTeamColor())) {
            return;
        }

        int row  = startPosition.getRow();
        TeamColor team = currentPiece.getTeamColor();

        // Kingside
        ChessPosition rookPosK = new ChessPosition(row, 8);
        if (!hasMoved.contains(rookPosK) &&
                board.getPiece(rookPosK) != null &&
                board.getPiece(rookPosK).getPieceType()
                        == ChessPiece.PieceType.ROOK &&
                board.getPiece(new ChessPosition(row, 6)) == null &&
                board.getPiece(new ChessPosition(row, 7)) == null) {

            ChessPosition[] pathK =
                    { startPosition, new ChessPosition(row, 6),
                            new ChessPosition(row, 7) };

            if (isPathSafe(team, pathK)) {
                onlyValid.add(
                        new ChessMove(startPosition,
                                new ChessPosition(row, 7),
                                null)
                );
            }
        }

        // Queenside
        ChessPosition rookPosQ = new ChessPosition(row, 1);
        if (!hasMoved.contains(rookPosQ) &&
                board.getPiece(rookPosQ) != null &&
                board.getPiece(rookPosQ).getPieceType()
                        == ChessPiece.PieceType.ROOK &&
                board.getPiece(new ChessPosition(row, 2)) == null &&
                board.getPiece(new ChessPosition(row, 3)) == null &&
                board.getPiece(new ChessPosition(row, 4)) == null) {

            ChessPosition[] pathQ =
                    { startPosition, new ChessPosition(row, 4),
                            new ChessPosition(row, 3) };

            if (isPathSafe(team, pathQ)) {
                onlyValid.add(
                        new ChessMove(startPosition,
                                new ChessPosition(row, 3),
                                null)
                );
            }
        }
    }

    private boolean isPathSafe(TeamColor team, ChessPosition[] positions) {
        ChessPiece savedKing = board.getPiece(positions[0]);
        board.removePiece(positions[0]);

        boolean safe = true;
        for (ChessPosition pos : positions) {
            ChessPiece saved = board.getPiece(pos);
            board.removePiece(pos);
            board.addPiece(pos,
                    new ChessPiece(team, ChessPiece.PieceType.KING));
            if (isInCheck(team)) {
                safe = false;
            }
            board.removePiece(pos);
            if (saved != null) {
                board.addPiece(pos, saved);
            }
        }

        board.addPiece(positions[0], savedKing);
        return safe;
    }

    public void makeMove(ChessMove move) throws InvalidMoveException {
        TeamColor turn = getTeamTurn();
        ChessPiece piece = this.board.getPiece(move.getStartPosition());

        if (piece == null) {
            throw new InvalidMoveException("No piece at the start position");
        }

        if (piece.getTeamColor() != turn) {
            throw new InvalidMoveException(
                    "Not this team's turn to move"
            );
        }

        if (this.board.getPiece(move.getEndPosition()) != null &&
                this.board.getPiece(move.getEndPosition())
                        .getTeamColor() == turn) {
            throw new InvalidMoveException(
                    "Can't move to a position with a piece of the same team"
            );
        }

        Collection<ChessMove> possibleMoves =
                validMoves(move.getStartPosition());
        if (!possibleMoves.contains(move)) {
            throw new InvalidMoveException(
                    "Not a possible move for that piece"
            );
        }

        ChessPiece newPiece =
                (move.getPromotionPiece() != null)
                        ? new ChessPiece(turn, move.getPromotionPiece())
                        : piece;

        hasMoved.add(move.getStartPosition());

        if (piece.getPieceType() == ChessPiece.PieceType.PAWN &&
                move.getStartPosition().getColumn()
                        != move.getEndPosition().getColumn() &&
                this.board.getPiece(move.getEndPosition()) == null) {

            int dir = (turn == TeamColor.WHITE) ? -1 : 1;
            ChessPosition capturedPawn =
                    new ChessPosition(
                            move.getEndPosition().getRow() + dir,
                            move.getEndPosition().getColumn()
                    );
            this.board.removePiece(capturedPawn);
        }

        if (piece.getPieceType() == ChessPiece.PieceType.KING &&
                Math.abs(move.getEndPosition().getColumn()
                        - move.getStartPosition().getColumn()) == 2) {

            int row = move.getStartPosition().getRow();
            if (move.getEndPosition().getColumn() == 7) {
                ChessPiece rook =
                        this.board.getPiece(new ChessPosition(row, 8));
                hasMoved.add(new ChessPosition(row, 8));
                this.board.removePiece(new ChessPosition(row, 8));
                this.board.addPiece(new ChessPosition(row, 6), rook);

            } else if (move.getEndPosition().getColumn() == 3) {
                ChessPiece rook =
                        this.board.getPiece(new ChessPosition(row, 1));
                hasMoved.add(new ChessPosition(row, 1));
                this.board.removePiece(new ChessPosition(row, 1));
                this.board.addPiece(new ChessPosition(row, 4), rook);
            }
        }

        this.board.addPiece(move.getEndPosition(), newPiece);
        this.board.removePiece(move.getStartPosition());
        this.lastMove = move;

        if (this.turn == TeamColor.WHITE) {
            this.turn = TeamColor.BLACK;
        } else {
            this.turn = TeamColor.WHITE;
        }
    }

    public boolean isInCheck(TeamColor teamColor) {
        ChessPosition kingPosition = null;
        for (int row = 1; row <= 8; row++) {
            for (int col = 1; col <= 8; col++) {
                ChessPosition pos = new ChessPosition(row, col);
                ChessPiece piece = board.getPiece(pos);
                if (piece != null &&
                        piece.getTeamColor() == teamColor &&
                        piece.getPieceType()
                                == ChessPiece.PieceType.KING) {

                    kingPosition = pos;
                    break;
                }
            }
        }

        for (int row = 1; row <= 8; row++) {
            for (int col = 1; col <= 8; col++) {
                ChessPosition pos = new ChessPosition(row, col);
                ChessPiece piece = board.getPiece(pos);
                if (piece != null &&
                        piece.getTeamColor() != teamColor) {

                    for (ChessMove move :
                            piece.pieceMoves(board, pos)) {

                        if (move.getEndPosition()
                                .equals(kingPosition)) {
                            return true;
                        }
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

        for (int row = 1; row <= 8; row++) {
            for (int col = 1; col <= 8; col++) {
                ChessPosition pos = new ChessPosition(row, col);
                ChessPiece piece = board.getPiece(pos);
                if (piece != null &&
                        piece.getTeamColor() == teamColor) {

                    if (!validMoves(pos).isEmpty()) {
                        return false;
                    }
                }
            }
        }

        return true;
    }

    public boolean isInStalemate(TeamColor teamColor) {
        if (isInCheck(teamColor)) {
            return false;
        }

        for (int row = 1; row <= 8; row++) {
            for (int col = 1; col <= 8; col++) {
                ChessPosition pos = new ChessPosition(row, col);
                ChessPiece piece = board.getPiece(pos);
                if (piece != null &&
                        piece.getTeamColor() == teamColor) {

                    if (!validMoves(pos).isEmpty()) {
                        return false;
                    }
                }
            }
        }

        return true;
    }

    public void setBoard(ChessBoard board) {
        this.board = board;
    }

    public ChessBoard getBoard() {
        return this.board;
    }
}

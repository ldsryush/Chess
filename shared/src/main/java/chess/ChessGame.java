package chess;

import java.util.*;

public class ChessGame {
    private TeamColor turn;
    private ChessBoard board;
    private ChessMove lastMove;
    private Set<ChessPosition> hasMoved = new HashSet<>();

    public ChessGame() {
        this.turn = TeamColor.WHITE;
        this.board = new ChessBoard();
        this.board.resetBoard();
        this.lastMove = null;
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
        Collection<ChessMove> allMoves = new HashSet<>(0);
        if (this.board.getPiece(startPosition) == null) {
            return allMoves;
        }
        ChessPiece currentPiece = board.getPiece(startPosition);
        allMoves = this.board.getPiece(startPosition)
                .pieceMoves(this.board, startPosition);

        if (currentPiece.getPieceType() == ChessPiece.PieceType.PAWN
                && lastMove != null) {
            // en passant logic...
            ChessPiece lastMoved = board.getPiece(lastMove.getEndPosition());
            if (lastMoved != null
                    && lastMoved.getPieceType() == ChessPiece.PieceType.PAWN
                    && lastMoved.getTeamColor() != currentPiece.getTeamColor()) {

                int lastStartRow = lastMove.getStartPosition().getRow();
                int lastEndRow   = lastMove.getEndPosition().getRow();
                int lastCol      = lastMove.getEndPosition().getColumn();
                int currRow      = startPosition.getRow();
                int currCol      = startPosition.getColumn();

                if (Math.abs(lastEndRow - lastStartRow) == 2
                        && lastEndRow == currRow
                        && Math.abs(lastCol - currCol) == 1) {

                    int direction = (currentPiece.getTeamColor() == TeamColor.WHITE)
                            ? 1
                            : -1;
                    ChessPosition capturePos =
                            new ChessPosition(currRow + direction, lastCol);

                    if (board.getPiece(capturePos) == null) {
                        allMoves.add(new ChessMove(
                                startPosition, capturePos, null));
                    }
                }
            }
        }

        Collection<ChessMove> onlyValid = new HashSet<>(0);
        for (ChessMove move : allMoves) {
            ChessPiece originalPiece = board.getPiece(move.getStartPosition());
            ChessPiece targetPiece   = board.getPiece(move.getEndPosition());

            ChessPiece simPiece =
                    (move.getPromotionPiece() != null)
                            ? new ChessPiece(
                            originalPiece.getTeamColor(),
                            move.getPromotionPiece()
                    )
                            : originalPiece;

            board.addPiece(move.getEndPosition(), simPiece);
            board.removePiece(move.getStartPosition());

            if (originalPiece.getPieceType() == ChessPiece.PieceType.PAWN
                    && move.getStartPosition().getColumn()
                    != move.getEndPosition().getColumn()
                    && targetPiece == null) {

                int dir = (originalPiece.getTeamColor() == TeamColor.WHITE)
                        ? -1
                        : 1;
                ChessPosition capturedPawn =
                        new ChessPosition(
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

            if (originalPiece.getPieceType() == ChessPiece.PieceType.PAWN
                    && move.getStartPosition().getColumn()
                    != move.getEndPosition().getColumn()
                    && targetPiece == null) {

                int dir = (originalPiece.getTeamColor() == TeamColor.WHITE)
                        ? -1
                        : 1;
                ChessPosition capturedPawn =
                        new ChessPosition(
                                move.getEndPosition().getRow() + dir,
                                move.getEndPosition().getColumn()
                        );
                board.addPiece(
                        capturedPawn,
                        new ChessPiece(
                                this.turn == TeamColor.WHITE
                                        ? TeamColor.BLACK
                                        : TeamColor.WHITE,
                                ChessPiece.PieceType.PAWN
                        )
                );
            }
        }

        if (currentPiece.getPieceType() == ChessPiece.PieceType.KING
                && !hasMoved.contains(startPosition)
                && !isInCheck(currentPiece.getTeamColor())) {

            int row  = startPosition.getRow();
            TeamColor team = currentPiece.getTeamColor();

            // kingside castling...
            ChessPosition rookPosK = new ChessPosition(row, 8);
            if (!hasMoved.contains(rookPosK)
                    && board.getPiece(rookPosK) != null
                    && board.getPiece(rookPosK).getPieceType()
                    == ChessPiece.PieceType.ROOK
                    && board.getPiece(new ChessPosition(row, 6)) == null
                    && board.getPiece(new ChessPosition(row, 7)) == null) {

                ChessPosition[] pathK = {
                        startPosition,
                        new ChessPosition(row, 6),
                        new ChessPosition(row, 7)
                };
                boolean safe = true;
                ChessPiece savedKing = board.getPiece(startPosition);
                board.removePiece(startPosition);

                for (ChessPosition pos : pathK) {
                    ChessPiece saved = board.getPiece(pos);
                    board.removePiece(pos);
                    board.addPiece(
                            pos,
                            new ChessPiece(
                                    team, ChessPiece.PieceType.KING
                            )
                    );
                    if (isInCheck(team)) {
                        safe = false;
                    }
                    board.removePiece(pos);
                    if (saved != null) {
                        board.addPiece(pos, saved);
                    }
                }

                board.addPiece(startPosition, savedKing);
                if (safe) {
                    onlyValid.add(new ChessMove(
                            startPosition,
                            new ChessPosition(row, 7),
                            null
                    ));
                }
            }

            // queenside castling...
            ChessPosition rookPosQ = new ChessPosition(row, 1);
            if (!hasMoved.contains(rookPosQ)
                    && board.getPiece(rookPosQ) != null
                    && board.getPiece(rookPosQ).getPieceType()
                    == ChessPiece.PieceType.ROOK
                    && board.getPiece(new ChessPosition(row, 2)) == null
                    && board.getPiece(new ChessPosition(row, 3)) == null
                    && board.getPiece(new ChessPosition(row, 4)) == null) {

                ChessPosition[] pathQ = {
                        startPosition,
                        new ChessPosition(row, 4),
                        new ChessPosition(row, 3)
                };
                boolean safe = true;
                ChessPiece savedKing = board.getPiece(startPosition);
                board.removePiece(startPosition);

                for (ChessPosition pos : pathQ) {
                    ChessPiece saved = board.getPiece(pos);
                    board.removePiece(pos);
                    board.addPiece(
                            pos,
                            new ChessPiece(
                                    team, ChessPiece.PieceType.KING
                            )
                    );
                    if (isInCheck(team)) {
                        safe = false;
                    }
                    board.removePiece(pos);
                    if (saved != null) {
                        board.addPiece(pos, saved);
                    }
                }

                board.addPiece(startPosition, savedKing);
                if (safe) {
                    onlyValid.add(new ChessMove(
                            startPosition,
                            new ChessPosition(row, 3),
                            null
                    ));
                }
            }
        }

        return onlyValid;
    }

    public void makeMove(ChessMove move) throws InvalidMoveException {
        TeamColor turn = getTeamTurn();
        ChessPiece piece = this.board.getPiece(move.getStartPosition());
        if (piece == null) {
            throw new InvalidMoveException(
                    "No piece at the start position"
            );
        }
        TeamColor color = piece.getTeamColor();
        Collection<ChessMove> possibleMoves =
                validMoves(move.getStartPosition());
        if (color != turn) {
            throw new InvalidMoveException(
                    "Not this team's turn to move"
            );
        }
        if (this.board.getPiece(move.getEndPosition()) != null) {
            if (this.board
                    .getPiece(move.getEndPosition())
                    .getTeamColor()
                    == color) {
                throw new InvalidMoveException(
                        "Can't move to a position with a piece of the same team"
                );
            }
        }
        if (!possibleMoves.contains(move)) {
            throw new InvalidMoveException(
                    "Not a possible move for that piece"
            );
        } else {
            ChessPiece newPiece = piece;
            if (move.getPromotionPiece() != null) {
                newPiece =
                        new ChessPiece(color, move.getPromotionPiece());
            }

            hasMoved.add(move.getStartPosition());

            if (piece.getPieceType() == ChessPiece.PieceType.PAWN
                    && move.getStartPosition().getColumn()
                    != move.getEndPosition().getColumn()
                    && this.board.getPiece(move.getEndPosition())
                    == null) {

                int dir =
                        (piece.getTeamColor() == TeamColor.WHITE) ? -1 : 1;
                ChessPosition capturedPawn =
                        new ChessPosition(
                                move.getEndPosition().getRow() + dir,
                                move.getEndPosition().getColumn()
                        );
                this.board.removePiece(capturedPawn);
            }

            if (piece.getPieceType()
                    == ChessPiece.PieceType.KING
                    && Math.abs(
                    move.getEndPosition().getColumn()
                            - move.getStartPosition().getColumn()
            )
                    == 2) {

                int row = move.getStartPosition().getRow();
                if (move.getEndPosition().getColumn() == 7) {
                    ChessPiece rook =
                            this.board.getPiece(
                                    new ChessPosition(row, 8)
                            );
                    hasMoved
                            .add(new ChessPosition(row, 8));
                    this.board
                            .removePiece(new ChessPosition(row, 8));
                    this.board.addPiece(
                            new ChessPosition(row, 6),
                            rook
                    );
                } else if (
                        move.getEndPosition().getColumn() == 3
                ) {
                    ChessPiece rook =
                            this.board.getPiece(
                                    new ChessPosition(row, 1)
                            );
                    hasMoved
                            .add(new ChessPosition(row, 1));
                    this.board
                            .removePiece(new ChessPosition(row, 1));
                    this.board.addPiece(
                            new ChessPosition(row, 4),
                            rook
                    );
                }
            }

            ChessPiece takenPiece =
                    this.board.getPiece(move.getEndPosition());
            this.board.addPiece(move.getEndPosition(), newPiece);
            this.board.removePiece(move.getStartPosition());
            this.lastMove = move;

            if (this.getTeamTurn() == TeamColor.WHITE) {
                this.setTeamTurn(TeamColor.BLACK);
            } else {
                this.setTeamTurn(TeamColor.WHITE);
            }
        }
    }

    public boolean isInCheck(TeamColor teamColor) {
        ChessPiece piece;
        ChessPosition kingPosition = null;
        ChessPosition currPosition;
        for (int row = 1; row <= 8; row++) {
            for (int col = 1; col <= 8; col++) {
                currPosition = new ChessPosition(row, col);
                if (this.board.getPiece(currPosition) != null) {
                    piece = this.board.getPiece(currPosition);
                    if (
                            piece.getTeamColor() == teamColor
                                    && piece.getPieceType()
                                    == ChessPiece.PieceType.KING
                    ) {
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
                    if (
                            piece.getTeamColor() != teamColor
                    ) {
                        for (
                                ChessMove move :
                                piece.pieceMoves(this.board, currPosition)
                        ) {
                            if (
                                    move.getEndPosition()
                                            .equals(kingPosition)
                            ) {
                                return true;
                            }
                        }
                    }
                }
            }
        }
        return false;
    }

    // Updated to remove duplication and flatten nesting:
    private boolean hasAnyLegalMove(TeamColor teamColor) {
        for (int row = 1; row <= 8; row++) {
            for (int col = 1; col <= 8; col++) {
                ChessPosition pos = new ChessPosition(row, col);
                ChessPiece piece = board.getPiece(pos);
                if (piece != null && piece.getTeamColor() == teamColor) {
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

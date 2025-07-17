package chess;

import java.util.*;

/**
 * Represents a chess piece and its movement logic.
 */
public class ChessPiece {
    private final ChessGame.TeamColor color;
    private final PieceType type;

    public ChessPiece(ChessGame.TeamColor color, PieceType type) {
        this.color = color;
        this.type = type;
    }

    public enum PieceType { KING, QUEEN, BISHOP, KNIGHT, ROOK, PAWN }

    public ChessGame.TeamColor getTeamColor() {
        return color;
    }

    public PieceType getPieceType() {
        return type;
    }

    private boolean isValidPosition(ChessPosition pos) {
        int row = pos.getRow();
        int col = pos.getColumn();
        return row >= 1 && row <= 8 && col >= 1 && col <= 8;
    }

    private void tryAddMove(Collection<ChessMove> moves, ChessBoard board, ChessPosition from, ChessPosition to, boolean canPromote) {
        if (!isValidPosition(to)) {
            return;
        }

        ChessPiece target = board.getPiece(to);
        if (target == null || target.getTeamColor() != color) {
            if (canPromote && type == PieceType.PAWN) {
                for (PieceType promotion : new PieceType[]{PieceType.QUEEN, PieceType.KNIGHT, PieceType.ROOK, PieceType.BISHOP}) {
                    moves.add(new ChessMove(from, to, promotion));
                }
            } else {
                moves.add(new ChessMove(from, to, null));
            }
        }
    }

    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition pos) {
        Collection<ChessMove> moves = new HashSet<>();

        switch (type) {
            case PAWN -> generatePawnMoves(moves, board, pos);
            case BISHOP -> generateDirectionalMoves(moves, board, pos, new int[][]{{1, 1}, {1, -1}, {-1, 1}, {-1, -1}});
            case ROOK -> generateDirectionalMoves(moves, board, pos, new int[][]{{1, 0}, {-1, 0}, {0, 1}, {0, -1}});
            case QUEEN -> generateDirectionalMoves(moves, board, pos, new int[][]{
                    {1, 1}, {1, -1}, {-1, 1}, {-1, -1}, {1, 0}, {-1, 0}, {0, 1}, {0, -1}
            });
            case KNIGHT -> generateJumpMoves(moves, board, pos, new int[][]{
                    {2, 1}, {1, 2}, {-1, 2}, {-2, 1}, {-2, -1}, {-1, -2}, {1, -2}, {2, -1}
            });
            case KING -> generateJumpMoves(moves, board, pos, new int[][]{
                    {1, 1}, {1, 0}, {1, -1}, {0, 1}, {0, -1}, {-1, 1}, {-1, 0}, {-1, -1}
            });
        }

        return moves;
    }

    private void generatePawnMoves(Collection<ChessMove> moves, ChessBoard board, ChessPosition pos) {
        int row = pos.getRow();
        int col = pos.getColumn();
        int direction = (color == ChessGame.TeamColor.WHITE) ? 1 : -1;
        int startRow = (color == ChessGame.TeamColor.WHITE) ? 2 : 7;
        int promoteRow = (color == ChessGame.TeamColor.WHITE) ? 8 : 1;

        // Forward move
        ChessPosition oneStep = new ChessPosition(row + direction, col);
        if (isValidPosition(oneStep) && board.getPiece(oneStep) == null) {
            tryAddMove(moves, board, pos, oneStep, oneStep.getRow() == promoteRow);

            // Two-step move from starting position
            if (row == startRow) {
                ChessPosition twoStep = new ChessPosition(row + 2 * direction, col);
                if (board.getPiece(twoStep) == null) {
                    tryAddMove(moves, board, pos, twoStep, false);
                }
            }
        }

        // Diagonal captures
        for (int dc : new int[]{-1, 1}) {
            ChessPosition diag = new ChessPosition(row + direction, col + dc);
            if (isValidPosition(diag)) {
                ChessPiece target = board.getPiece(diag);
                if (target != null && target.getTeamColor() != color) {
                    tryAddMove(moves, board, pos, diag, diag.getRow() == promoteRow);
                }
            }
        }
    }

    private void generateDirectionalMoves(Collection<ChessMove> moves, ChessBoard board, ChessPosition pos, int[][] directions) {
        for (int[] d : directions) {
            for (int i = 1; i < 8; i++) {
                ChessPosition next = new ChessPosition(pos.getRow() + d[0] * i, pos.getColumn() + d[1] * i);
                if (!isValidPosition(next)) {
                    break;
                }

                ChessPiece target = board.getPiece(next);
                if (target == null) {
                    moves.add(new ChessMove(pos, next, null));
                } else {
                    if (target.getTeamColor() != color) {
                        moves.add(new ChessMove(pos, next, null));
                    }
                    break;
                }
            }
        }
    }

    private void generateJumpMoves(Collection<ChessMove> moves, ChessBoard board, ChessPosition pos, int[][] deltas) {
        for (int[] d : deltas) {
            ChessPosition next = new ChessPosition(pos.getRow() + d[0], pos.getColumn() + d[1]);
            tryAddMove(moves, board, pos, next, false);
        }
    }

    @Override
    public boolean equals(Object o) {
        return this == o || (o instanceof ChessPiece cp && color == cp.color && type == cp.type);
    }

    @Override
    public int hashCode() {
        return Objects.hash(color, type);
    }
}
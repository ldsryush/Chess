package chess;

import java.util.Objects;

public class ChessMove {
    private final ChessPosition start, end;
    private final ChessPiece.PieceType promotionPiece;

    public ChessMove(ChessPosition start, ChessPosition end, ChessPiece.PieceType promotionPiece) {
        this.start = start;
        this.end = end;
        this.promotionPiece = promotionPiece;
    }

    public ChessPosition getStartPosition() { return start; }

    public ChessPosition getEndPosition() { return end; }

    public ChessPiece.PieceType getPromotionPiece() { return promotionPiece; }

    @Override
    public boolean equals(Object o) {
        return this == o || (o instanceof ChessMove m &&
                Objects.equals(start, m.start) &&
                Objects.equals(end, m.end) &&
                promotionPiece == m.promotionPiece);
    }

    @Override
    public int hashCode() {
        return Objects.hash(start, end, promotionPiece);
    }
}

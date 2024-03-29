package wisematches.server.web.servlet.mvc.playground.scribble.game.form;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import wisematches.playground.scribble.Direction;
import wisematches.playground.scribble.Position;
import wisematches.playground.scribble.Tile;
import wisematches.playground.scribble.Word;

import java.util.Arrays;

/**
 * @author Sergey Klimenko (smklimenko@gmail.com)
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ScribbleWordForm {
    private String direction;
    private PositionForm position;
    private ScribbleTileForm[] tiles;

    public ScribbleWordForm() {
    }

    public Word createWord() {
        final Tile[] t = new Tile[tiles.length];
        for (int i = 0, tileEditorsLength = tiles.length; i < tileEditorsLength; i++) {
            t[i] = tiles[i].createTile();
        }
        return new Word(position.createPosition(), Direction.valueOf(direction.toUpperCase()), t);
    }


    public String getDirection() {
        return direction;
    }

    public void setDirection(String direction) {
        this.direction = direction;
    }

    public ScribbleTileForm[] getTiles() {
        return tiles;
    }

    public void setTiles(ScribbleTileForm[] tiles) {
        this.tiles = tiles;
    }

    public PositionForm getPosition() {
        return position;
    }

    public void setPosition(PositionForm position) {
        this.position = position;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("ScribbleWordForm");
        sb.append("{direction='").append(direction).append('\'');
        sb.append(", position=").append(position);
        sb.append(", tiles=").append(tiles == null ? "null" : Arrays.asList(tiles).toString());
        sb.append('}');
        return sb.toString();
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class PositionForm {
        private int row;
        private int column;

        public PositionForm() {
        }

        public int getRow() {
            return row;
        }

        public void setRow(int row) {
            this.row = row;
        }

        public int getColumn() {
            return column;
        }

        public void setColumn(int column) {
            this.column = column;
        }

        public Position createPosition() {
            return new Position(row, column);
        }

        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder();
            sb.append("PositionEditor");
            sb.append("{row=").append(row);
            sb.append(", column=").append(column);
            sb.append('}');
            return sb.toString();
        }
    }
}

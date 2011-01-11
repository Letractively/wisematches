package wisematches.server.games.scribble.board;

import wisematches.server.core.board.GamePlayerHand;
import wisematches.server.games.scribble.core.Tile;

import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Transient;
import java.util.Arrays;

/**
 * @author Sergey Klimenko (smklimenko@gmail.com)
 */
@Entity
@Table(name = "scribble_player")
public final class ScribblePlayerHand extends GamePlayerHand<ScribbleBoard> {
    @Transient
    private Tile[] tiles = EMPTY_TILES;

    private static final Tile[] EMPTY_TILES = new Tile[0];

    ScribblePlayerHand() {
    }

    /**
     * Creates new player hand with specified player id.
     *
     * @param playerId the player id.
     */
    public ScribblePlayerHand(long playerId) {
        this(playerId, EMPTY_TILES);
    }

    public ScribblePlayerHand(long playerId, Tile... tiles) {
        super(playerId);
        this.tiles = tiles;
    }

    public ScribblePlayerHand(long playerId, int points, Tile... tiles) {
        super(playerId, points);
        this.tiles = tiles;
    }

    public Tile[] getTiles() {
        return tiles;
    }

    public void addTiles(Tile[] tiles) {
        Tile[] res = new Tile[tiles.length + this.tiles.length];
        System.arraycopy(this.tiles, 0, res, 0, this.tiles.length);
        System.arraycopy(tiles, 0, res, this.tiles.length, tiles.length);

        this.tiles = res;
    }

    public void removeTiles(Tile[] tiles) {
        Tile[] res = new Tile[this.tiles.length - tiles.length];

        int index = 0;
        for (Tile pt : this.tiles) {
            boolean remove = false;
            for (int i = 0; i < tiles.length && !remove; i++) {
                remove = pt.equals(tiles[i]);
            }

            if (!remove) {
                res[index++] = pt;
            }
        }
        this.tiles = res;
    }

    public boolean containsTile(Tile tile) {
        for (Tile tile1 : tiles) {
            if (tile1.equals(tile)) {
                return true;
            }
        }
        return false;
    }

    public void setTiles(Tile[] tiles) {
        if (tiles == null) {
            throw new IllegalArgumentException("Letters can't be null");
        }
        this.tiles = tiles;
    }

    @Override
    public String toString() {
        return "ScribblePlayerHand{" +
                super.toString() +
                ", tiles=" + (tiles == null ? null : Arrays.asList(tiles)) +
                '}';
    }
}
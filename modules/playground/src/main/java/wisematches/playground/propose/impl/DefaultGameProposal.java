package wisematches.playground.propose.impl;

import wisematches.personality.Personality;
import wisematches.personality.player.Player;
import wisematches.playground.GameSettings;
import wisematches.playground.propose.GameProposal;
import wisematches.playground.propose.GameRestriction;
import wisematches.playground.propose.ViolatedRestrictionException;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

/**
 * @author Sergey Klimenko (smklimenko@gmail.com)
 */
public final class DefaultGameProposal<S extends GameSettings> implements GameProposal<S>, Serializable {
    private final long id;
    private final S gameSettings;
    private final int playersCount;
    private final GameRestriction restriction;
    private final Collection<Personality> players;

    public DefaultGameProposal(long id, S gameSettings, int playersCount, Collection<Player> players) {
        this(id, gameSettings, playersCount, null, players);
    }

    /**
     * Opens new waiting game in this manager.
     *
     * @param id           id of this proposal. Can't be zero.
     * @param gameSettings the original game settings. Can't be null.
     * @param playersCount the max players count. Can't be less than 2.
     * @param restriction  the restriction for game proposal.
     * @param players      current players count. List must contains at least one player (initiator). Size can't be more
     *                     or equals than {@code playersCount}
     * @throws IllegalArgumentException if {@code settings} or {@code players} are null or if {@code playersCount}
     *                                  is less than two or if {@code players} collection size more than
     *                                  {@code playersCount} or contains null.
     */
    protected DefaultGameProposal(long id, S gameSettings, int playersCount, GameRestriction restriction, Collection<Player> players) {
        if (id == 0) {
            throw new IllegalArgumentException("error.proposal.null.id");
        }
        if (gameSettings == null) {
            throw new IllegalArgumentException("error.proposal.null.settings");
        }
        if (players == null) {
            throw new IllegalArgumentException("error.proposal.null.players");
        }
        if (playersCount < 2) {
            throw new IllegalArgumentException("error.proposal.illegal.count");
        }
        if (players.size() < 1) {
            throw new IllegalArgumentException("error.proposal.notenough.players");
        }
        if (players.size() > playersCount) {
            throw new IllegalArgumentException("error.proposal.many.players");
        }

        this.restriction = restriction;

        this.id = id;
        this.gameSettings = gameSettings;
        this.playersCount = playersCount;

        this.players = new ArrayList<Personality>(playersCount);
        for (Player player : players) {
            if (player == null) {
                throw new IllegalArgumentException("error.proposal.null.player");
            }
            if (this.players.contains(player)) {
                throw new IllegalArgumentException("error.proposal.twice.player");
            }
            this.players.add(Personality.untie(player));
        }
    }

    @Override
    public long getId() {
        return id;
    }

    @Override
    public S getGameSettings() {
        return gameSettings;
    }

    @Override
    public int getPlayersCount() {
        return playersCount;
    }

    @Override
    public Collection<Personality> getPlayers() {
        return Collections.unmodifiableCollection(players);
    }

    @Override
    public GameRestriction getRestriction() {
        return restriction;
    }

    @Override
    public void isSuitablePlayer(Player player) throws ViolatedRestrictionException {
        if (player == null) {
            throw new ViolatedRestrictionException("player.null");
        }
        if (players.contains(player)) {
            throw new ViolatedRestrictionException("player.exist");
        }
        if (restriction != null) {
            restriction.validatePlayer(player);
        }
    }

    @Override
    public boolean containsPlayer(Personality personality) {
        return players.contains(personality);
    }

    @Override
    public boolean isChallenge() {
        return false;
    }

    @Override
    public boolean isReady() {
        return players.size() == playersCount;
    }

    /**
     * Attaches this player to this proposal.
     *
     * @param player the player to be attached
     * @throws IllegalArgumentException     if player is null
     * @throws IllegalStateException        if proposal already full and new player can't be added.
     * @throws ViolatedRestrictionException if player can't be attached because one or more restrictions are broken.
     */
    void attachPlayer(Player player) throws ViolatedRestrictionException {
        if (player == null) {
            throw new ViolatedRestrictionException("player.null");
        }
        if (this.players.contains(player)) {
            throw new ViolatedRestrictionException("player.exist");
        }
        if (isReady()) {
            throw new ViolatedRestrictionException("ready");
        }
        validateRestrictions(player);
        players.add(Personality.untie(player));
    }

    /**
     * Attaches this player to this proposal.
     *
     * @param player the player to be attached
     * @throws IllegalArgumentException     if player is null
     * @throws ViolatedRestrictionException if player can't be detached because one or more restrictions are broken.
     */
    void detachPlayer(Player player) throws ViolatedRestrictionException {
        if (player == null) {
            throw new ViolatedRestrictionException("player.null");
        }
        validateRestrictions(player);
        players.remove(player);
    }

    void validateRestrictions(Player player) throws ViolatedRestrictionException {
        if (restriction != null) {
            restriction.validatePlayer(player);
        }
    }
}

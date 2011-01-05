/*
 * Copyright (c) 2009, WiseMatches (by Sergey Klimenko).
 */

package wisematches.server.core.tournament.subscription;

import wisematches.server.core.tournament.TournamentException;

/**
 * TODO: write description here
 *
 * @author <a href="mailto:smklimenko@gmail.com">Sergey Klimenko</a>
 */
public class TournamentSubscriptionException extends TournamentException {
    public TournamentSubscriptionException(String message) {
        super(message);
    }

    public TournamentSubscriptionException(String message, Throwable cause) {
        super(message, cause);
    }
}

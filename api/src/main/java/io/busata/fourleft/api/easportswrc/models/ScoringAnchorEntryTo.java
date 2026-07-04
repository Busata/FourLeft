package io.busata.fourleft.api.easportswrc.models;

import java.math.BigDecimal;

/**
 * One entry of a {@link ScoringAnchorsTo} definition. Either an anchor ({@code points} set, pinning the
 * position to an absolute value) or a decrease ({@code decrease} set, a per-position drop like {@code 1.83}).
 * Exactly one of the two is non-null.
 */
public record ScoringAnchorEntryTo(
        int position,
        Integer points,
        BigDecimal decrease)
{
}

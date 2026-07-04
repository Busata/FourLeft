package io.busata.fourleft.backendeasportswrc.domain.models.scoring;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.math.BigDecimal;

/**
 * One entry in a {@link ScoringAnchors} definition, living at a finishing {@code position}. It is either
 * an <b>anchor</b> ({@code points} set) that pins the position to an absolute value, or a <b>decrease</b>
 * ({@code decrease} set) that subtracts that many points per position until the next entry. Exactly one of
 * {@code points} / {@code decrease} is non-null.
 */
public record ScoringAnchorEntry(int position, Integer points, BigDecimal decrease) {

    /** Derived; {@code @JsonIgnore} so it is not persisted as a phantom "anchor" field in the jsonb. */
    @JsonIgnore
    public boolean isAnchor() {
        return points != null;
    }
}

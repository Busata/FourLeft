package io.busata.fourleft.common;

/**
 * Whether a MIXED channel locks each driver to their home class: the class of their first run in the
 * championship. Runs in another class never score custom points and are left out of that class's racenet
 * standings section; the mode only decides how such a run shows on results.
 */
public enum ClassLockMode {
    /** Drivers may switch class between events; every run counts in its own class. */
    OFF,
    /** A run outside the home class is shown with a warning. */
    WARN,
    /** A run outside the home class is hidden and the board re-ranked. */
    EXCLUDE
}

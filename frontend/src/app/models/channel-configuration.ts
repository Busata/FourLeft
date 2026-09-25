export type ScoringStrategy = 'LOOKUP_TABLE' | 'POINT_ANCHOR' | 'RACENET_DEFAULT';

// One entry of a POINT_ANCHOR definition: either an anchor (points set) or a decrease (decrease set).
export interface ScoringAnchorEntry {
  position: number;
  points?: number | null;
  decrease?: number | null;
}

export interface ScoringAnchors {
  floor: number;
  entries: ScoringAnchorEntry[];
}

export type RestrictionType = 'VEHICLE_ALLOWLIST';
export type RestrictionDisplayMode = 'WARN' | 'EXCLUDE';
export type RestrictionScoringMode = 'EXCLUDE' | 'PENALTY';

// Targets exactly one of championshipId (all its events) or eventId (that event only, wins).
export interface EventRestriction {
  type: RestrictionType;
  championshipId: string | null;
  eventId: string | null;
  displayMode: RestrictionDisplayMode;
  scoringMode: RestrictionScoringMode;
  penaltyPoints?: number | null;
  allowedVehicles: string[];
}

export interface RestrictionTargetEvent {
  id: string;
  location: string;
  vehicleClass: string;
  absoluteCloseDate: string;
}

export interface RestrictionTargetChampionship {
  id: string;
  name: string;
  absoluteOpenDate: string;
  absoluteCloseDate: string;
  events: RestrictionTargetEvent[];
}

export interface RestrictionTargets {
  championships: RestrictionTargetChampionship[];
}

// SINGLE posts the primary club only; MIXED merges clubs running the same championship in different car
// classes. TIERED is reserved for divisions and not offered yet.
export type ChannelClubMode = 'SINGLE' | 'MIXED' | 'TIERED';

// A club tracked by the channel; the first one is the primary club. The label names its class ("WRC2").
export interface ChannelClub {
  clubId: string;
  label: string | null;
}

// What one club is currently running, as compared for mixed mode.
export interface ClubChampionship {
  clubId: string;
  label: string | null;
  championshipId: string | null;
  championshipName: string | null;
  location: string | null;
  vehicleClass: string | null;
  eventCloseDate: string | null;
}

// Whether the clubs run the same championship (only the car class may differ).
export interface ChannelClubCompatibility {
  compatible: boolean;
  problems: string[];
  clubs: ClubChampionship[];
}

export interface ChannelConfiguration {
  guildId: string;
  channelId: string;
  configured: boolean;
  clubId: string | null;
  autopostingEnabled: boolean | null;
  requiresTracking: boolean | null;
  enabled: boolean | null;
  customScoringEnabled: boolean | null;
  timeTrialTopEnabled: boolean | null;
  timeTrialTopTrackedOnly: boolean | null;
  scoringStrategy: ScoringStrategy | null;
  scoringTable: Record<string, number> | null;
  scoringAnchors: ScoringAnchors | null;
  eventRestrictions: EventRestriction[] | null;
  mode: ChannelClubMode | null;
  clubs: ChannelClub[];
  // What posting actually uses: MIXED falls back to SINGLE while the clubs don't match.
  effectiveMode: ChannelClubMode | null;
  // Null for a single-club channel.
  compatibility: ChannelClubCompatibility | null;
}

export type ScoringStrategy = 'LOOKUP_TABLE' | 'POINT_ANCHOR';

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

export interface ChannelConfiguration {
  guildId: string;
  channelId: string;
  configured: boolean;
  clubId: string | null;
  autopostingEnabled: boolean | null;
  requiresTracking: boolean | null;
  enabled: boolean | null;
  customScoringEnabled: boolean | null;
  scoringStrategy: ScoringStrategy | null;
  scoringTable: Record<string, number> | null;
  scoringAnchors: ScoringAnchors | null;
  eventRestrictions: EventRestriction[] | null;
}

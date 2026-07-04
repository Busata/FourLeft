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
}

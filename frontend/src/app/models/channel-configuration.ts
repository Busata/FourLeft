export type ScoringStrategy = 'LOOKUP_TABLE';

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
}

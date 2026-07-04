export interface ChannelConfiguration {
  guildId: string;
  channelId: string;
  configured: boolean;
  clubId: string | null;
  autopostingEnabled: boolean | null;
  requiresTracking: boolean | null;
  enabled: boolean | null;
}

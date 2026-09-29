// Mirrors ProfileDisputeAdminTo / DiscordUserInfoTo in the api module.
export interface DiscordUserInfo {
  id: string;
  username: string | null; // null when the live lookup failed
  globalName: string | null;
  avatarUrl: string | null;
}

export interface ProfileDisputeAdmin {
  playerId: string; // racenet ssid
  racenet: string;
  displayName: string;
  holder: DiscordUserInfo | null;
  disputer: DiscordUserInfo;
  disputedAt: string;
}

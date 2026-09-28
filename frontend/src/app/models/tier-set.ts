// Mirrors TierSetTo / TierTo / TierPlayerTo in the api module.
export interface TierPlayer {
  playerId: string; // racenet ssid
  displayName: string;
}

export interface Tier {
  id: string;
  label: string;
  players: TierPlayer[];
}

export interface TierSet {
  id: string;
  name: string;
  tiers: Tier[]; // top tier first
}

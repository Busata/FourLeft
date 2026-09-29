export type ControllerType = 'WHEEL' | 'CONTROLLER' | 'KEYBOARD' | 'OTHER' | 'UNKNOWN';

export type Platform =
  | 'PC'
  | 'PLAYSTATION'
  | 'XBOX'
  | 'EPIC'
  | 'EA_APP'
  | 'STEAM'
  | 'STEAM_DECK'
  | 'OTHER'
  | 'UNKNOWN';

export type PeripheralType = 'MONITOR' | 'TRIPLES' | 'WIDESCREEN' | 'VR' | 'OTHER' | 'UNKNOWN';

export type ProfileClaimState = 'CLAIMED' | 'VERIFIED' | 'DISPUTED';

export interface Profile {
  id: string;
  displayName: string;
  controller: ControllerType;
  platform: Platform;
  peripheral: PeripheralType;
  racenet: string;
  trackDiscord: boolean;
  claimState: ProfileClaimState;
}

// A racenet account (ssid) someone disputes, as shown to the disputer.
export interface ProfileDispute {
  playerId: string;
  racenet: string;
  disputedAt: string;
}

// What a profile link opens: null profile = the user still has to pick their racenet account.
export interface ProfilePage {
  profile: Profile | null;
  disputing: ProfileDispute | null;
}

export type ProfileClaimOutcome =
  | 'LINKED'
  | 'PICK'
  | 'CONFIRM_DISPUTE'
  | 'DISPUTED'
  | 'ALREADY_DISPUTED'
  | 'DISPUTE_LIMIT'
  | 'VERIFIED_BY_OTHER';

export interface ProfileClaimResult {
  requestId: string | null;
  outcome: ProfileClaimOutcome;
  playerId: string | null; // racenet ssid the outcome is about
  racenet: string | null;
}

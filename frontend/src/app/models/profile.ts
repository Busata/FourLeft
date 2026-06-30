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

export interface Profile {
  id: string;
  displayName: string;
  controller: ControllerType;
  platform: Platform;
  peripheral: PeripheralType;
  racenet: string;
  trackDiscord: boolean;
}

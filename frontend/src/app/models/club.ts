import { WireDuration } from '../common/time-format';

/** Club identity for the selector dropdown — mirrors the backend ClubReferenceTo. */
export interface ClubReference {
  clubId: string;
  clubName: string;
}

/** One member's result on an event leaderboard. Times are serialized java.time.Duration values. */
export interface ClubResultEntry {
  wrcPlayerId: string;
  playerName: string;
  nationalityID: number | null;
  platform: number | null;
  rank: number | null;
  vehicle: string;
  /** The final stage's own time. */
  time: WireDuration | null;
  /** Accumulated time across the event's stages — the event result that decides standings. */
  timeAccumulated: WireDuration | null;
  timePenalty: WireDuration | null;
  differenceToFirst: WireDuration | null;
  differenceAccumulated: WireDuration | null;
}

export interface EventSettings {
  location: string;
  weatherSeason: string;
  vehicleClass: string;
  duration: string;
}

export interface StageSettings {
  route: string;
  timeOfDay: string;
  serviceArea: string;
  weatherAndSurface: string;
}

export interface ClubEventResult {
  eventSettings: EventSettings;
  stages: StageSettings[];
  id: string;
  status: string;
  absoluteOpenDate: string;
  absoluteCloseDate: string;
  leaderboardEntries: ClubResultEntry[];
}

export interface ChampionshipSettings {
  name: string;
}

export interface ClubChampionshipResult {
  id: string;
  /** Championship title (named `string` by the backend record/generated model). */
  string: string;
  absoluteOpenDate: string;
  absoluteCloseDate: string;
  championshipSettings: ChampionshipSettings | null;
  events: ClubEventResult[];
}

/** The cached club export: every championship → event → member result. */
export interface ClubOverview {
  id: string;
  clubName: string;
  clubDescription: string;
  activeMemberCount: number;
  championships: ClubChampionshipResult[];
}

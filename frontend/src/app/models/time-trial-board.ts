/** Drill-down catalog of time-trial boards that have fetched entries — mirrors TimeTrialBoardEndpoint. */
export interface TtCatalog {
  rallies: TtRally[];
}

export interface TtRally {
  locationId: number;
  location: string;
  stages: TtStage[];
}

export interface TtStage {
  routeId: number;
  route: string;
  surfaces: TtSurface[];
}

export interface TtSurface {
  /** 0 = dry, 1 = wet. */
  surfaceCondition: number;
  classes: TtClass[];
}

/** A leaf: a specific car class on a stage+surface, with the combination id to fetch its entries. */
export interface TtClass {
  vehicleClassId: number;
  vehicleClass: string;
  combinationId: string;
}

/** One leaderboard row. Times are raw Racenet strings ("hh:mm:ss.fffffff"). */
export interface TtEntry {
  rank: number | null;
  displayName: string;
  nationalityId: number | null;
  vehicle: string;
  platform: number | null;
  time: string;
  differenceToFirst: string;
  timePenalty: string;
  splits: string[] | null;
}

export interface TtEntryPage {
  entries: TtEntry[];
  page: number;
  size: number;
  total: number;
  totalPages: number;
}

/** One of a player's stored times, with the board it's on — a row of the profile page. */
export interface TtPlayerEntry {
  combinationId: string;
  locationId: number;
  location: string;
  routeId: number;
  route: string;
  surfaceCondition: number;
  vehicleClassId: number;
  vehicleClass: string;
  rank: number | null;
  time: string;
  differenceToFirst: string;
  platform: number | null;
  /** Per-sector cumulative split times ("hh:mm:ss.fffffff"); null/empty when not stored. */
  splits: string[] | null;
}

/** Reverse lookup: every board a player appears on. */
export interface TtPlayerProfile {
  name: string;
  entries: TtPlayerEntry[];
}

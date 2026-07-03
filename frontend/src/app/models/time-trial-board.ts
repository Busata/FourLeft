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

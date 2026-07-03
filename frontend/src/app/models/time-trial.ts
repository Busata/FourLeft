/** One queryable Racenet time-trial leaderboard — mirrors the backend TimeTrialEndpoint.CombinationView. */
export interface TimeTrialCombination {
  /** "{locationId}-{routeId}-{surfaceCondition}-{vehicleClassId}", e.g. "6-99-0-19". */
  id: string;
  locationId: number;
  location: string;
  /** The stage. */
  routeId: number;
  route: string;
  /** 0 = dry, 1 = wet. */
  surfaceCondition: number;
  vehicleClassId: number;
  vehicleClass: string;
  /** From the latest probe: whether the board exists on Racenet; null = never probed. */
  valid: boolean | null;
  /** From the latest probe: entry count on the board; null = never probed / board absent. */
  totalEntries: number | null;
  /** From the latest observation: entries changed since the previous fetch; null for probes (not fetched). */
  changedEntries: number | null;
  /** When the latest probe ran (ISO); null = never probed. */
  probedAt: string | null;
}

/** A page of combinations — mirrors the backend TimeTrialEndpoint.PageView. */
export interface TimeTrialPage {
  items: TimeTrialCombination[];
  total: number;
  page: number;
  size: number;
  totalPages: number;
}

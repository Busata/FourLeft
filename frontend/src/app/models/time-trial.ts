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
  /** Whether this combination has a board on Racenet; null = not yet determined. */
  valid: boolean | null;
}

/** A page of combinations — mirrors the backend TimeTrialEndpoint.PageView. */
export interface TimeTrialPage {
  items: TimeTrialCombination[];
  total: number;
  page: number;
  size: number;
  totalPages: number;
}

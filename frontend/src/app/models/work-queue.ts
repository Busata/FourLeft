export type JobStatus = 'PENDING' | 'RUNNING' | 'DONE' | 'FAILED';
export type JobType = 'CLUB' | 'TT_PROBE' | 'TT_FETCH';

/** What a completed job did — mirrors the backend JobOutcome enum. */
export type JobOutcome =
  | 'CLUB_CREATED'
  | 'CHAMPIONSHIP_STARTED'
  | 'EVENT_ENDED'
  | 'LEADERBOARDS_UPDATED'
  | 'HISTORY_UPDATED'
  | 'DETAILS_REFRESHED'
  | 'NO_CHANGE'
  | 'SYNC_DISABLED'
  | 'TT_PROBED'
  | 'TT_FETCHED';

export interface WorkQueueSummary {
  queueEnabled: boolean;
  jobCountsByStatus: Record<JobStatus, number>;
  jobCountsByType: Record<JobType, number>;
}

export interface WorkJobView {
  id: number;
  type: JobType;
  ref: string;
  status: JobStatus;
  createdAt: string;
  /** When the (last) run began; null while still PENDING. */
  startedAt: string | null;
  /** When it reached a terminal state; null until DONE/FAILED. */
  finishedAt: string | null;
  /** Run time in ms (finishedAt − startedAt); null until finished. */
  durationMs: number | null;
  /** Time spent waiting in the queue in ms (startedAt − createdAt); null until started. */
  waitMs: number | null;
  /** Worker starts; > 1 means it was recovered after a crashed worker. */
  attempts: number;
  recovered: boolean;
  /** What the run did; null until terminal (and for a hard-failed job). */
  outcome: JobOutcome | null;
  /** Whether the run altered stored data; null until done. */
  changed: boolean | null;
  leaderboardsUpdated: number | null;
  standingsUpdated: number | null;
  entriesImported: number | null;
  targetId: number | null;
  lastError: string | null;
}

/** A recurring target and its derived next-due time — "when is this club next scheduled". */
export interface WorkTargetView {
  id: number;
  type: JobType;
  ref: string;
  nextRunAt: string;
  enabled: boolean;
}

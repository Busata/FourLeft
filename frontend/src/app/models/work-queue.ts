export type JobStatus = 'PENDING' | 'RUNNING' | 'DONE' | 'FAILED';
export type JobType = 'CLUB';

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
  lockedAt: string | null;
  createdAt: string;
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

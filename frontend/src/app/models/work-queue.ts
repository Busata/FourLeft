export type JobStatus = 'PENDING' | 'RUNNING' | 'DONE' | 'FAILED';
export type JobType = 'CLUB' | 'TT' | 'CONFIG_CLEANUP';

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
  attempts: number;
  runAfter: string | null;
  lockedAt: string | null;
  createdAt: string;
  targetId: number | null;
  lastError: string | null;
}

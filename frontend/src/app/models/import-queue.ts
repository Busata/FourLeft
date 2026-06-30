export type ImportJobStatus = 'PENDING' | 'RUNNING' | 'DONE' | 'FAILED';

export interface ImportQueueSummary {
  queueEnabled: boolean;
  jobCountsByStatus: Record<ImportJobStatus, number>;
}

export interface ImportJobView {
  id: number;
  ref: string;
  status: ImportJobStatus;
  attempts: number;
  runAfter: string | null;
  lockedAt: string | null;
  createdAt: string;
  targetId: number | null;
  lastError: string | null;
}

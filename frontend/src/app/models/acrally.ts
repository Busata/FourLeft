// Hand-rolled mirrors of the backend ACRally `*To` records. The generated
// `common/generated/server-models.d.ts` is gitignored and only produced by the Java
// build, so it isn't present when the frontend image builds — the app hand-rolls its
// types (see models/club.ts, models/profile.ts) for exactly this reason. Keep these in
// sync with `io.busata.fourleft.api.acrally.models.*`. Dates arrive as ISO strings.

export interface AuthUserTo {
  id: string;
  email: string;
  displayName: string;
  status: string;
  admin: boolean;
}

// Admin-only view of a user, from GET /acrally-api/admin/users.
export interface AdminUserTo {
  id: string;
  email: string;
  displayName: string;
  status: string;
  admin: boolean;
  createdAt: string;
}

// A catalogued stage, from GET /acrally-api/admin/stage-names. displayName is null until assigned.
export interface StageNameTo {
  id: string;
  rawName: string;
  displayName: string | null;
  createdAt: string;
  updatedAt: string | null;
}

// The outcome of POST /acrally-api/admin/stage-names/collect.
export interface StageNameCollectResultTo {
  added: number;
  stages: StageNameTo[];
}

export interface LoginRequestTo {
  email: string;
  password: string;
}

export interface RegisterRequestTo {
  email: string;
  password: string;
  displayName: string;
}

export interface LinkedIdentityTo {
  provider: string;
  providerUserId: string;
  linkedAt: string;
}

export interface SteamProfileTo {
  personaName: string | null;
  avatarUrl: string | null;
  profileUrl: string | null;
  accountCreated: string | null;
  visibilityState: number | null;
  vacBanned: boolean;
  gameBanCount: number;
  communityBanned: boolean;
}

export interface ApiKeyTo {
  id: string;
  label: string | null;
  createdAt: string;
  lastUsedAt: string | null;
  revoked: boolean;
}

export interface PairLookupResultTo {
  userCode: string;
  label: string | null;
  expiresAt: string;
}

// Not a backend record: the static release manifest (acrally-agent/latest.json)
// written by acrally-agent/release.ps1 and served at /acrally-agent/latest.json.
export interface AgentReleaseTo {
  version: string;
  url: string;
  min_supported: string;
  notes: string;
}

export interface MySessionTo {
  id: string;
  driver: string | null;
  car: string | null;
  stage: string | null;
  track: string | null;
  status: string;
  startedAtMs: number | null;
  currentMs: number | null;
  lastHeartbeatAt: string | null;
  createdAt: string;
}

export interface MyResultTo {
  id: string;
  stage: string | null;
  car: string | null;
  driver: string | null;
  rawMs: number;
  penaltyMs: number;
  totalMs: number;
  recordedAt: string;
}

export interface ClubTo {
  id: string;
  name: string;
  description: string | null;
  socialLink: string | null;
  createdByDisplayName: string | null;
  memberCount: number;
  member: boolean;
  owner: boolean;
  createdAt: string;
}

export interface CreateClubRequestTo {
  name: string;
  description: string;
  socialLink: string;
}

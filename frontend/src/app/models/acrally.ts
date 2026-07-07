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

// A rally location, from GET /acrally-api/admin/locations. stageCount > 0 blocks deletion.
export interface LocationTo {
  id: string;
  name: string;
  nation: string | null;
  stageCount: number;
  createdAt: string;
  updatedAt: string | null;
}

export interface LocationRequestTo {
  name: string;
  nation: string;
}

// A stage, from GET /acrally-api/admin/stages. variantCount > 0 blocks deletion.
export interface StageTo {
  id: string;
  name: string;
  locationId: string | null;
  locationName: string | null;
  variantCount: number;
  createdAt: string;
  updatedAt: string | null;
}

export interface StageRequestTo {
  name: string;
  locationId: string | null;
}

// A collected variant (the raw result key), from GET /acrally-api/admin/variants.
export interface VariantTo {
  id: string;
  rawName: string;
  displayName: string | null;
  stageId: string | null;
  stageName: string | null;
  locationName: string | null;
  createdAt: string;
  updatedAt: string | null;
}

export interface UpdateVariantRequestTo {
  displayName: string;
  stageId: string | null;
}

// The outcome of POST /acrally-api/admin/variants/collect.
export interface VariantCollectResultTo {
  added: number;
  variants: VariantTo[];
}

// A rally car, from GET /acrally-api/admin/cars.
export interface CarTo {
  id: string;
  name: string;
  year: number | null;
  groupName: string | null;
  className: string | null;
  createdAt: string;
  updatedAt: string | null;
}

export interface CarRequestTo {
  name: string;
  year: number | null;
  groupName: string | null;
  className: string | null;
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

// A championship summary, from GET /acrally-api/clubs/{clubId}/championships.
export interface ChampionshipTo {
  id: string;
  clubId: string;
  name: string;
  startsAt: string; // ISO date-time (yyyy-MM-ddTHH:mm:ss)
  status: string; // DRAFT | PUBLISHED
  eventCount: number;
  owner: boolean;
  createdAt: string;
}

// A championship with its ordered events, from GET /acrally-api/championships/{id}.
export interface ChampionshipDetailTo {
  id: string;
  clubId: string;
  clubName: string | null;
  name: string;
  startsAt: string; // ISO date-time
  status: string; // DRAFT | PUBLISHED
  owner: boolean;
  events: ChampionshipEventTo[];
}

export interface ChampionshipEventTo {
  id: string;
  label: string; // derived from the distinct locations of the event's stages
  position: number;
  gapDays: number;
  durationDays: number;
  opensAt: string; // ISO date-time, derived
  closesAt: string; // ISO date-time, derived
  variants: EventVariantTo[];
  cars: CarTo[];
}

export interface EventVariantTo {
  variantId: string;
  position: number;
  label: string;
  stageName: string | null;
  locationName: string | null;
}

export interface CreateChampionshipRequestTo {
  name: string;
  startsAt: string; // ISO date-time
}

export interface UpdateChampionshipRequestTo {
  name: string;
  startsAt: string; // ISO date-time
  status: string;
}

// Add an event with its stages and cars in one shot. Events aren't named — they're labelled from
// their stages' locations.
export interface CreateEventRequestTo {
  gapDays: number;
  durationDays: number;
  variantIds: string[];
  carIds: string[];
}

export interface UpsertEventRequestTo {
  gapDays: number;
  durationDays: number;
}

export interface SetEventVariantsRequestTo {
  variantIds: string[];
}

export interface SetEventCarsRequestTo {
  carIds: string[];
}

export interface ReorderEventsRequestTo {
  eventIds: string[];
}

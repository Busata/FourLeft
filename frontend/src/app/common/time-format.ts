// Shared time parsing / formatting for the time-trial and club views. Two families of value show up:
//   - Racenet leaderboard strings — "hh:mm:ss.fffffff" (time-trial boards + profile).
//   - java.time.Duration values from the club export — encoded as a JSON number (fractional seconds)
//     or an ISO-8601 "PT…S" string depending on the backend's Jackson config; parseDuration handles
//     both so the club view is robust either way.

/** One row of a side-by-side comparison (a sector split, a stage, or a finish total). */
export interface CompareRow {
  label: string;
  /** Display strings for each side (already formatted for the club view; raw for the TT view). */
  a: string | null;
  b: string | null;
  /** true when that side is strictly faster on this row. */
  aWins: boolean;
  bWins: boolean;
}

/** "hh:mm:ss.fffffff" → seconds as a float (full fractional precision), or null if unparseable. */
export function parseSeconds(raw: string | null): number | null {
  if (!raw) {
    return null;
  }
  const parts = raw.split(':');
  if (parts.length !== 3) {
    return null;
  }
  const [hh, mm, ss] = parts;
  const seconds = Number(hh) * 3600 + Number(mm) * 60 + Number(ss);
  return Number.isFinite(seconds) ? seconds : null;
}

/** "00:10:23.1400000" → "10:23.140" (drops a zero hours component, trims to milliseconds). */
export function formatTime(raw: string | null): string {
  if (!raw) {
    return '—';
  }
  const parts = raw.split(':');
  if (parts.length !== 3) {
    return raw;
  }
  const [hh, mm, ss] = parts;
  const secs = trimFraction(ss);
  return parseInt(hh, 10) > 0 ? `${parseInt(hh, 10)}:${mm}:${secs}` : `${mm}:${secs}`;
}

/** Gap to the leader as "+2.147"; blank for the leader (rank 1) / a zero gap. Raw "hh:mm:ss(.fff)". */
export function formatDiff(raw: string | null, rank: number | null): string {
  if (rank === 1 || !raw || raw === '00:00:00') {
    return '';
  }
  const parts = raw.split(':');
  if (parts.length !== 3) {
    return raw;
  }
  const [hh, mm, ss] = parts;
  const h = parseInt(hh, 10);
  const m = parseInt(mm, 10);
  const secs = trimFraction(ss);
  if (h > 0) {
    return `+${h}:${mm}:${secs}`;
  }
  return m > 0 ? `+${m}:${secs}` : `+${secs}`;
}

/** Podium tier for a rank: 'gold' | 'silver' | 'bronze' for 1/2/3, else ''. */
export function podium(rank: number | null): string {
  if (rank === 1) return 'gold';
  if (rank === 2) return 'silver';
  if (rank === 3) return 'bronze';
  return '';
}

export function surfaceLabel(surfaceCondition: number): string {
  return surfaceCondition === 1 ? 'Wet' : 'Dry';
}

/** Compare two raw Racenet time strings into a row, tagging the faster (strictly lower) side. */
export function compareRow(label: string, a: string | null, b: string | null): CompareRow {
  const av = parseSeconds(a);
  const bv = parseSeconds(b);
  const comparable = av != null && bv != null;
  return {
    label,
    a,
    b,
    aWins: comparable && av < bv,
    bWins: comparable && bv < av,
  };
}

/** "23.1400000" → "23.140"; "00" → "00.000". */
function trimFraction(ss: string): string {
  const [sec, frac = ''] = ss.split('.');
  return `${sec}.${(frac + '000').slice(0, 3)}`;
}

// --- Club durations (java.time.Duration over the wire) -----------------------

/**
 * A serialized java.time.Duration. The backend emits ISO-8601 strings ("PT1M23.456S", "PT0S") —
 * verified by ClubResultSerializationTest against the app's real ObjectMapper — but the type and
 * parser also accept a numeric-seconds / clock encoding so a Jackson config change can't break us.
 */
export type WireDuration = number | string;

/**
 * Parse a serialized Duration to seconds, tolerating every shape Jackson might emit: an ISO-8601
 * duration ("PT1M23.456S" — what this backend produces), a JSON number of fractional seconds, a bare
 * numeric string, or a clock string. Returns null when absent/unparseable.
 */
export function parseDuration(value: WireDuration | null | undefined): number | null {
  if (value == null) {
    return null;
  }
  if (typeof value === 'number') {
    return Number.isFinite(value) ? value : null;
  }
  const s = value.trim();
  if (!s) {
    return null;
  }
  if (/^[-+]?P/i.test(s)) {
    return parseIso8601Duration(s);
  }
  if (/^[-+]?\d+(\.\d+)?$/.test(s)) {
    return Number(s);
  }
  if (s.includes(':')) {
    return parseSeconds(s);
  }
  return null;
}

/** ISO-8601 duration ("PT1H2M3.4S", "PT0S", "-PT5M") → seconds, or null. Only D/T components. */
function parseIso8601Duration(s: string): number | null {
  const m = /^([-+]?)P(?:(\d+)D)?(?:T(?:(\d+)H)?(?:(\d+)M)?(?:(\d+(?:\.\d+)?)S)?)?$/i.exec(s);
  if (!m) {
    return null;
  }
  const sign = m[1] === '-' ? -1 : 1;
  const days = Number(m[2] ?? 0);
  const hours = Number(m[3] ?? 0);
  const minutes = Number(m[4] ?? 0);
  const secs = Number(m[5] ?? 0);
  return sign * (days * 86400 + hours * 3600 + minutes * 60 + secs);
}

/** Seconds → "m:ss.mmm" (or "h:mm:ss.mmm" past an hour); "—" when null. */
export function formatDurationClock(totalSeconds: number | null): string {
  if (totalSeconds == null) {
    return '—';
  }
  const neg = totalSeconds < 0;
  let t = Math.abs(totalSeconds);
  const h = Math.floor(t / 3600);
  t -= h * 3600;
  const m = Math.floor(t / 60);
  t -= m * 60;
  const sec = Math.floor(t);
  const ms = Math.min(999, Math.round((t - sec) * 1000));
  const secStr = `${String(sec).padStart(2, '0')}.${String(ms).padStart(3, '0')}`;
  const body = h > 0 ? `${h}:${String(m).padStart(2, '0')}:${secStr}` : `${m}:${secStr}`;
  return (neg ? '-' : '') + body;
}

/** A positive gap in seconds as "+m:ss.mmm"; blank for a null / non-positive gap. */
export function formatGap(seconds: number | null): string {
  if (seconds == null || seconds <= 0) {
    return '';
  }
  return `+${formatDurationClock(seconds)}`;
}

/** "Top X%" standing bands, ascending: a rank's percentile rounds up to the first band it's within. */
const PERCENTILE_BANDS = [1, 5, 10, 20, 30, 40, 50, 60, 70, 80, 90, 100];

/**
 * A rank's bucketed "top X%" standing within a field of {@code total} entries — rounded up to the
 * nearest band (1, 5, 10, then 10% steps), e.g. P1 of 1000 → "Top 1%". Blank when the rank or the
 * field size is unknown.
 */
export function percentileBand(rank: number | null, total: number | null | undefined): string {
  if (rank == null || total == null || total < 1) {
    return '';
  }
  const pct = (rank / total) * 100;
  const band = PERCENTILE_BANDS.find((b) => pct <= b) ?? 100;
  return `Top ${band}%`;
}

/** Compare two already-parsed second values into a display row (values pre-formatted as clocks). */
export function compareDurationRow(label: string, a: number | null, b: number | null): CompareRow {
  const comparable = a != null && b != null;
  return {
    label,
    a: formatDurationClock(a),
    b: formatDurationClock(b),
    aWins: comparable && a < b,
    bWins: comparable && b < a,
  };
}

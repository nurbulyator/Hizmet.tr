// Turkey uses UTC+3 all year (no daylight saving time).
export const ISTANBUL_OFFSET_MINUTES = 180;
export const SLOT_STEP_MINUTES = 30;

/** "2026-10-21" + 600 (10:00 Istanbul time) -> Date in UTC */
export function istanbulTime(date: string, minuteOfDay: number): Date {
  const midnightUtc = Date.parse(`${date}T00:00:00Z`);
  const ms = midnightUtc + (minuteOfDay - ISTANBUL_OFFSET_MINUTES) * 60_000;
  return new Date(ms);
}

/** 1 = Monday ... 7 = Sunday */
export function weekdayOf(date: string): number {
  const day = new Date(`${date}T00:00:00Z`).getUTCDay();
  return day === 0 ? 7 : day;
}

export function formatMinute(minuteOfDay: number): string {
  const h = Math.floor(minuteOfDay / 60);
  const m = minuteOfDay % 60;
  return `${String(h).padStart(2, '0')}:${String(m).padStart(2, '0')}`;
}

export interface Slot {
  time: string;
  startsAt: string;
  available: boolean;
}

export interface BusyInterval {
  startsAt: Date;
  endsAt: Date;
}

/**
 * Splits the working day into slots of SLOT_STEP_MINUTES.
 * A slot is available when the whole service fits into working hours,
 * it is in the future and it does not overlap an existing booking.
 */
export function buildSlots(
  date: string,
  startMinute: number,
  endMinute: number,
  durationMin: number,
  busy: BusyInterval[],
  now: Date,
): Slot[] {
  const slots: Slot[] = [];
  for (
    let minute = startMinute;
    minute + durationMin <= endMinute;
    minute += SLOT_STEP_MINUTES
  ) {
    const start = istanbulTime(date, minute);
    const end = istanbulTime(date, minute + durationMin);
    const overlaps = busy.some((b) => start < b.endsAt && b.startsAt < end);
    slots.push({
      time: formatMinute(minute),
      startsAt: start.toISOString(),
      available: start > now && !overlaps,
    });
  }
  return slots;
}

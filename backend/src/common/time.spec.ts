import { buildSlots, istanbulTime, weekdayOf } from './time';

describe('time helpers', () => {
  it('converts Istanbul time to UTC', () => {
    expect(istanbulTime('2026-10-21', 600).toISOString()).toBe(
      '2026-10-21T07:00:00.000Z',
    );
  });

  it('returns weekday with Monday = 1 and Sunday = 7', () => {
    expect(weekdayOf('2026-10-19')).toBe(1);
    expect(weekdayOf('2026-10-25')).toBe(7);
  });
});

describe('buildSlots', () => {
  const past = new Date('2026-01-01T00:00:00Z');

  it('only offers slots where the whole service fits', () => {
    // 10:00-12:00, 90 minute service -> 10:00 and 10:30 only
    const slots = buildSlots('2026-10-21', 600, 720, 90, [], past);
    expect(slots.map((s) => s.time)).toEqual(['10:00', '10:30']);
  });

  it('marks slots overlapping a booking as unavailable', () => {
    const busy = [
      {
        startsAt: istanbulTime('2026-10-21', 660),
        endsAt: istanbulTime('2026-10-21', 720),
      },
    ];
    const slots = buildSlots('2026-10-21', 600, 780, 60, busy, past);
    const available = slots.filter((s) => s.available).map((s) => s.time);
    expect(available).toEqual(['10:00', '12:00']);
  });

  it('marks slots in the past as unavailable', () => {
    const now = istanbulTime('2026-10-21', 615);
    const slots = buildSlots('2026-10-21', 600, 690, 30, [], now);
    expect(slots.map((s) => s.available)).toEqual([false, true, true]);
  });
});

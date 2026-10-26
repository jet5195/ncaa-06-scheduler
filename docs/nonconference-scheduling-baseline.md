# Nonconference scheduling baseline — 2026-10-26

This test-first pass covers filling FBS schedules to 12 games, without specifying
random opponents, venues, or rivalry policies. Conference games are protected.
Rivalry selection, aggressive rivalry replacement, and home/away requirements
remain a separate testing task.

The 11 existing season cases (2000–2010) now also check for self-games, repeated
opponents, weekly conflicts, out-of-season weeks, conference schedule changes,
and same-conference matchups incorrectly added as nonconference games. All pass.

Ten controlled cases cover FBS pairing, FCS fallback, same-conference exclusion,
already-full FBS teams, conference metadata preservation during normal filling,
conference preservation during overfull-season trimming, exhausted opponent
options, and independent-school self eligibility. Six pass; four fail:

- With no FCS teams, only an already-played FCS team, or no shared open week,
  the fallback selects from an empty list and throws `IllegalArgumentException`.
  The proposed requirement is best-effort completion: leave unmatched slots
  open and return without damaging existing games.
- `isEligibleNonConfMatchup(independent, independent)` returns true because
  independent schools are not considered to share a conference. This is an
  eligibility defect; the test does not claim that a self-game was saved.

Combined baseline: **21 cases, 17 passing, four failing**, zero errors or skips.
No production code changed. These new failures remain enabled, separate from
the previously deferred conference minimum-window limitations.

## Fixes — 2026-10-26

The FCS fallback now stops filling the current school when its eligible list is
empty, logs a warning, and continues with other schools. Games already scheduled
are retained. Nonconference eligibility also explicitly excludes the same school
(using school equality by TGID), including independent schools.

The original 21 nonconference cases and four existing ScheduleService cases now
pass. Two additional regressions cover continuing after an unfillable school and
retaining partial progress when the remaining opponent options run out. Rivalry
logic and conference-game protection rules were not changed.

Run with JDK 21:

```powershell
./mvnw.cmd test "-Dtest=NonConferenceSchedulingRequirementsTest,NonConferenceSchedulingIntegrationTest"
```

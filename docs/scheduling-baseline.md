# Scheduling requirements and confidence baseline

This document separates intended behavior from the current implementation and
from what tests actually demonstrate. Baseline started October 20, 2026 on main
at ba585ff. The TEAM CSV branch remains separate.

## Current requirements

- The automatic season filler targets 12 regular-season games per FBS team.
  This is the current product policy, confirmed by the owner, not a permanent
  restriction for historical/custom formats. Treatment of FCS opponents and
  postseason games must be tested separately; do not assume every team in a
  dynasty should have a 12-game schedule.
- No regular-season rematches. Postseason rematches are a separate question.
- Teams cannot play themselves or play twice in the same week.
- Games must reference known teams and fit the game's supported schedule format.
- Conference schedules should satisfy the configured conference game count and
  protected cross-division opponents for supported formats.
- Generated eight-game conference schedules must have exactly four home and four
  away games per team every season. Nine-game schedules must have four or five
  home games, alternating each team's five-home and four-home seasons annually.
- Every matchup must swap venue on its next meeting, including after a gap of
  several seasons. This applies to generated conference schedules; nonconference,
  neutral-site and user-preserved matchups are outside this requirement's scope.
- For fourteen teams with nine conference games and no protected cross-division
  rivals, every cross-division pair must meet three times in any seven seasons
  and six times, split three home/three away, in any fourteen seasons.
- Top rivalries have late-season preferences today. Exact annual dates are not
  required: the named yearly-series code in ScheduleService is commented out.
- Configurations that cannot be satisfied should produce an explanation rather
  than silently return a partial schedule as successful.

## Policies to confirm before treating failures as bugs

- What is the allowed opponent rotation cycle and desired opponent frequency?
- Which divisional matchups are mandatory? Which protected rivals and rivalry
  weeks are requirements versus preferences?
- Should auto-generation preserve existing games, or be allowed to move/replace
  them? A future "locked" game would explicitly preserve its teams, week and venue.
- How should byes, conference start weeks, FCS games, and postseason scheduling
  interact with regular-season totals?

Do not assume modern divisionless formats are the default. Future rule profiles
should support historical and custom seasons, including different season lengths,
conference sizes, divisions, protected opponents, and rotation rules. Year alone
must not force a real-world alignment or scheduling policy.

## Implementation limits discovered

- Twelve-game targets are hard-coded throughout ScheduleService. Count queries
  currently count all games for a team, so postseason separation needs coverage.
- The regular-season placement window is currently zero-based weeks 0 through 14;
  ordinary game numbering is recalculated below week 16, with bowl identities
  preserved at week 16 and later. These are implementation conventions to verify
  against exported game data, not universal football rules.
- Conferences with 1 through 11 teams use round robin, regardless of a smaller
  configured game count. Existing generated fixtures exercise sizes 8 through 11.
- The 12/14-team generators assume two divisions and selected 8/9-game formats.
- The 13/15/16-team generators are empty stubs despite factory entries.
- The original generated test matrix excluded 12 teams / 9 games / protected
  cross-division rivals with an incorrect "impossible" comment. The missing format
  is now implemented and included in the matrix; see the focused fix below.
- Random selection has no single reproducible seed. A passing run does not prove
  absence of intermittent failures.

## Evidence before test repairs

| Suite | Passed | Failed | Scope |
| --- | ---: | ---: | --- |
| ScheduleServiceTest | 1 | 3 | Three broken test fixtures, repaired in this change |
| ConferenceSchedulerTest | 557 | 48 | Includes 121 print-only cases in original results |
| Application startup | 1 | 0 | Context creation only |
| ConferenceSchedulingIntegrationTest | 34 | 11 | All-scenarios fixture expects 9 ACC games, gets 8 |
| NonConferenceSchedulingIntegrationTest | 11 | 0 | Checks totals only, not full schedule validity |

Original scheduler failures: 37 home-count checks and 11 consecutive-year venue
checks. These are policy-dependent findings pending diagnosis, not proof that
all other scheduling behavior is correct. SchoolServiceTest and ExcelReaderTest
are empty classes and provide no coverage.

## First test improvements

- Repair FBS conference setup, distinct school IDs, and conference-game flags in
  service fixtures. Exercise non-FBS exclusion using out-of-range counts.
- Fail scheduler tests when generation throws instead of printing exceptions.
- Replace print-only success with assertions for nonempty output, no self-games,
  unique matchups, one game per team/week, and the current placement window.
- Keep balance/rotation failures visible; do not change production scheduling or
  relax expectations just to make the build green.

## Next evidence needed

After the first test repairs, the full local suite ran 666 cases: 607 passed,
59 failed, zero errors and zero skips. All four service tests passed, and all
121 replacements for the print-only cases passed their new structural assertions.
The remaining failures were still 48 scheduler balance/rotation cases and 11
all-scenarios integration cases. The nonconference suite again passed 11 cases.
This is one run with uncontrolled randomness, not proof of full correctness.

1. Classify failing balance/rotation cases by format and real calendar year;
   generated cases currently use years 0 through 10, while integration cases use
   2000 through 2010. Check whether year-relative rotation causes differences.
2. Apply independent schedule validation to complete fixture-generated seasons,
   including team totals, conference membership, division requirements, rivalry
   preservation, rematches, and export identity constraints.
3. Add import/export round-trip fixtures and rejected-edit tests that demonstrate
   existing valid data remains intact after a failed operation.
4. Introduce reproducible randomness, then run a documented set of seeds.
5. Resolve policy-dependent failures with the owner before expanding rule options.

## Focused fix: twelve teams, nine games, protected cross-division rivals

The old routing selected the eight-game generator whenever protected rivals were
present. The new route generates five divisional games, the protected rival, and
three other cross-division opponents. Cross-division selections use complete
matchings so every school receives four cross-division games, two home and two away.

The rotation retains opponents for two-year home/away pairs and cycles through
six combinations over twelve seasons. Every non-protected opponent is included.
This initial rotation prioritizes annual home/away balance and venue reversal:
non-protected opponents are not played equally often (six or eight meetings over
twelve years). Equal-frequency rotation is a separate policy/design decision,
not a guarantee of this fix.

The focused deterministic regression initially failed all four starting-year
cases with 48 total games instead of 54. It now checks a twelve-season window for
starting years 0, 1980, 2005, and 2020, including reversed division list order,
game totals, all divisional opponents, protected rivals, four/five home games,
no duplicate matchups or weekly conflicts, and consecutive-year venue reversal.

After this fix the full suite ran 725 cases: 666 passed, 59 failed, no errors or
skips. The new format passed all 55 added cases in the existing matrix plus all
four focused multi-season cases. The original 48 balance/rotation failures remain.
The eleven all-scenarios integration cases now get past the ACC and fail at the
Big East (expected nine, found zero). The fixture's Big East is conference ID 3,
but its divisions reference ID 1; the 14-team generator encounters an empty
division list and catches its exception internally. This previously masked
fixture/validation problem is the next investigation, not a regression in the
new 12-team path.

### Big East fixture repair

Both local copies of Test_All_Scenarios_Alignment.xlsx now have conference ID 3
in Divisions!B12:B13 (previously 1). Cell values across all three worksheets were
compared before and after export to verify only these two values changed.
The test fixture was previously ignored and absent from Git; a narrow .gitignore
exception now allows it to be committed for reproducible CI runs.

The conference integration suite then ran 46 cases: 35 passed, 11 failed.
The new Big East relationship regression passes, and all eleven all-scenarios
year cases get past Big East's nine-game checks. Those cases now stop at the
Sun Belt (Appalachian State: expected eight games, found zero). That separate
fixture/generation issue remains to be investigated. The full suite was not
rerun for this data-only repair.

### Sun Belt fixture repair

The next failure had the same cause: Sun Belt is conference ID 13, but its
Winners/Losers divisions referenced ID 12. Both local all-scenarios workbooks now
use ID 13 in Divisions!B14:B15. Other cell values were checked for preservation
across all three worksheets after export. The conference relationship regression
now covers both Big East and Sun Belt, including two seven-team divisions with
consistent conference and school links. No production scheduler logic changed
for this fixture repair.

Verification: all 47 conference integration cases passed, with zero failures,
errors or skips. This includes all eleven all-scenarios years (2000 through 2010)
and both conference relationship regression cases. The full suite was not rerun;
the previously identified 48 balance/rotation failures remain outside this repair.

### Annual home/away requirements and first balancing fix

The owner confirmed exact annual 4/4 conference balance for eight games, a 5/4
split alternating annually for nine games, and venue reversal on every next
meeting (including gaps). Nonconference, neutral-site and user-preserved games
are outside this change. Those rules now appear under Current requirements.

The first fix targets twelve-team, eight-game schedules. Protected rivals now
have complementary divisional home totals, with the second division ordered by
pairing before its divisional games are generated. The no-rival format replaces
the old three-team blocks with three complete cross-division matchings, giving
the extra home game to schools with only two divisional home games.

The no-rival rotation changes opponents from the previous algorithm. It uses
two-year venue pairs over a twelve-year cycle and covers every opponent. Equal
opponent frequency is not guaranteed: one offset recurs annually, the other even
offsets occur six times each and the odd offsets four times each over the cycle.
There are no protected-rival inputs in this path; equal-frequency scheduling
remains an open policy/design question, as with the nine-game protected format.

A deterministic regression exercises 24 seasons with no protected rivals and
with protected rivals in both normal and reversed opposite-division import order.
It checks eight unique opponents, four home games, protected rival preservation,
and no team double-booking. The initial test reproduced an unbalanced home total.
This step does not implement history-based venue tracking across imported seasons
or resolve the remaining fourteen-team scheduling failures.

Verification: the full suite ran 730 cases, with 699 passes, 31 failures, no
errors and no skips. All three new eight-game regressions passed; all 47
conference integration cases stayed green. Existing failures fell from 48 to 31:
20 annual home-count failures and 11 consecutive-year venue failures, all in the
fourteen-team nine-game formats. The protected twelve-team nine-game regression
was then strengthened to retain the last host across skipped seasons and require
annual four/five-home alternation; all four starting-year cases passed separately.
The remaining rotation tests still need gap-aware coverage for the other formats.

### Fourteen-team nine-game balance and venue fix

Both original nine-game branches assigned cross-division venues from changing
opponent indices. The protected-rival helper also chose the same hosting side
each year because every seven-team division supplies exactly three home games
per school. The focused regression reproduced home totals of three or six and
unchanged hosts on repeated matchups; all eight starting-format/year cases failed.

The initial replacement used three complete cross-division matchings. Each school gets
six divisional opponents and three cross-division opponents. Two matchings host
in the first division and one hosts in the second in even years; odd years reverse
those venues. This gives every school alternating five-home/four-home seasons.
Each matchup's base venue is tied to its stable offset, and opponents are retained
for two-year venue pairs, so the next occurrence also reverses host after gaps.

With protected rivals, offset zero is annual and the other six opponents rotate
equally over six seasons. The initial unprotected rotation had unequal opponent
frequencies (twelve or eight meetings over twenty-four seasons). That limitation
is now resolved by the equal-frequency fix below. Generated opponent selections
differ from the original nine-game algorithm.

Fourteen-team generation now propagates exceptions instead of printing them and
returning a partial result as success. The eight-game opponent algorithms remain
unchanged. Historical imported hosting and arbitrary alignment changes are not
tracked by these calendar-based cycles.

The new regression checks both rival settings from starting years 0, 1980, 2005,
and 2020 over twenty-eight seasons, crossing cycle boundaries. It covers reversed
opposite-division import order, nine distinct opponents, all divisional games,
protected rivals, weekly conflicts, schedule windows, annual home/away balance,
next-meeting venue reversal including gaps, and coverage of all conference teams
within seven seasons. All eight cases pass after the fix.

Full-suite verification after this change: 738 cases passed, zero failures,
errors or skips. This includes all 660 original parameterized scheduler cases,
47 conference integration cases, eight new fourteen-team cases, and the existing
service, startup, nonconference and twelve-team regressions. This is a passing
baseline for the covered configurations, not coverage of all unsupported formats
or of next-meeting venue rotation in every older eight-game path.

### Equal-frequency fourteen-team nine-game rotation without protected rivals

Each team must play every cross-division opponent exactly three times in any
seven consecutive seasons, and six times (three home, three away) in any fourteen.
Four new rolling-window cases reproduced four meetings where three were expected
against the previous implementation, so the old tests' coverage-only assertions
were not enough to establish frequency fairness.

The new algorithm walks the seven complete matchings sequentially, consuming
three per season, and alternates the host side on every matching. Seven seasons
consume twenty-one matching slots, giving every pair three meetings. Because
three slots per season and seven slots per opponent cycle are both odd, annual
five/four home totals and each pair's next-meeting venue both alternate. Fourteen
seasons complete the full opponent/venue cycle. There are no implicit annual
cross-division opponents in this unprotected format; each returns every two or
three seasons. The protected-rival and eight-game paths are unchanged by this fix.

The twelve focused cases pass: the existing eight balance/rotation cases plus
four new frequency cases starting at years 0, 1980, 2005 and 2020. Each new case
generates twenty-eight seasons and checks every rolling seven- and fourteen-year
window, covering all cycle phases and boundaries.

Full-suite verification: 742 cases passed, zero failures, errors or skips.
The new frequency assertions passed alongside all existing annual balance,
next-meeting venue, protected-rival and conference integration checks.

## Running checks

Use JDK 21 and `./mvnw test` (Windows: `./mvnw.cmd test`). Avoid `verify` for
ordinary tests because the POM binds native installer packaging to that phase.
The GitHub Actions workflow runs the full suite and uploads Surefire reports even
when tests fail. Earlier red baselines above record the progression to a passing
suite; a passing suite establishes only the behavior covered by its assertions.

The initial local run needed an ignored temporary Maven settings file pointing at
Maven Central because the machine's corporate mirror did not resolve. Personal
Maven settings are not changed or committed.

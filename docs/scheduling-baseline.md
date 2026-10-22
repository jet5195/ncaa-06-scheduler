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

### Test-first conference rotation regressions (October 21, 2026)

ConferenceRotationRequirementsTest adds 48 enabled JUnit cases for twelve teams /
nine games / no protected rivals and fourteen teams / eight games / either rival
setting. Each path is exercised from 1980, 2000, 2005 and 2020 with normal and
reversed opposite-division ordering, for twenty-eight seasons. Conference games
start at week 3 and stay within the current week-14 placement limit.

Annual requirements and next-meeting venues are tested separately so a venue
failure cannot hide a game-count, opponent or balance failure. Checks include
correct totals, all divisional opponents, protected rivals, unique matchups,
known conference opponents, no self-games or weekly conflicts, home/away balance,
annual five/four alternation for nine games, and venue reversal after gaps.
The venue test retains the last meeting across skipped seasons, rejects partial
generation and checks every conference opponent is visited over the test horizon.
No new equal-frequency policy was assumed for these three formats.

Only the new class was run for this step. Result: 48 cases, 32 passes, 16 failures,
zero errors or skips. All 24 annual-requirement cases passed. Venue results:

| Format | Passed | Failed |
| --- | ---: | ---: |
| 12 teams, 9 games, no protected rivals | 0 | 8 |
| 14 teams, 8 games, no protected rivals | 4 | 4 |
| 14 teams, 8 games, protected rivals | 4 | 4 |

For the twelve-team format, a natural-order run repeats G hosting E in 1980 and
1982. For the fourteen-team formats, runs spanning 2005 repeat the same host in
2004 and 2006; the 2005/2020 starting runs pass, including cycle boundaries.
Those observations narrow the diagnosis without declaring a complete root cause.

Production code is unchanged in this test-first step. The new tests are deliberately
not disabled or excluded: the expanded suite is expected to be red until these
conference rotation defects are fixed. The previously passing 742-case baseline
was not rerun here and does not include these 48 new cases.

### Diagnosis: twelve teams, nine games, no protected rivals

An independent trace built a fresh twelve-school conference without the JUnit
fixture helper, last-meeting map or mocked week selection, then called the real
factory/generator. It confirmed the test's failure:

| Year | First-division rotation | E's rotated index | E-G host |
| --- | ---: | ---: | --- |
| 1980 | 0 | 4 | G |
| 1981 | 1 | 5 | No game |
| 1982 | 2 | 0 | G |
| 1983 | 3 | 1 | E |
| 1984 | 4 | 2 | No game |
| 1985 | 5 | 3 | E |
| 1986 | 0 | 4 | G |

All seasons contained the expected 54 conference games. G hosts consecutive
actual E-G meetings in 1980/1982; E does the same in 1983/1985.

TwelveTeamConferenceScheduler.schedule9Games rotates the first division by
year % 6 and selects four opponents using that rotated row's pattern. The shared
scheduleXDivGamesByIndex helper chooses venues from the parity of the rotated
row index and the opposite division's fixed opponent index. G remains at opposite
index 0. E's indices 4 and 0 in 1980 and 1982 are both even, so the helper makes
E the away team in both years. The explicit specifyHome=true argument prevents
the yearly-series helper from applying another venue decision.

For a fixed pair, this index parity effectively flips its potential host every
calendar year, including years the pair does not play. A two-year gap returns
to the same parity and same host. Annual balance can therefore pass while
next-meeting venue alternation fails. Merely switching to year parity would not
solve that gap case either.

The test's matchup key sorts the two team IDs; its immutable stored value records
the last actual host/year. Map.put returns that previous value before replacing
it. Thus neither reversed home/away order nor clearing the season repository
causes the observed failure. The independent trace establishes a production
rotation defect under the agreed next-meeting requirement. No scheduling logic
was changed during this diagnosis.

### Fix: twelve teams, nine games, no protected rivals (2026-10-22)

This path retains the existing six-year opponent patterns, but chooses the host
by counting earlier actual meetings of each pair within that cycle. Each pair
meets four times per cycle, so venue alternation also holds across its boundary
without retaining previous generated seasons. Other conference formats continue
using their existing scheduling paths.

TwelveTeamNineGameRotationTest adds eight preservation cases across four starting
years and both opposing-division orders. Every rolling six-season window must
contain four meetings per cross-division pair, with two at each stadium; every
rolling four-season window must visit both stadiums. These eight cases passed
before the fix as well: the old defect was consecutive meetings sharing a host,
not a demonstrated failure of four-year stadium coverage.

Full-suite verification: 798 tests, 790 passing, eight failing, no errors or
skips. All twelve-team requirements and the eight new preservation cases pass;
the original 742 cases also pass. The remaining eight failures are the already
identified fourteen-team, eight-game next-meeting venue cases spanning 2005,
with and without protected rivals. They remain enabled for the next fix.

### Diagnosis: fourteen teams, eight games (2026-10-22)

Both rival settings derive their opponent rotation from `Math.abs(year - 2005)`.
This folds the timeline around 2005: 2002..2008 use offsets 3,2,1,0,1,2,3.
An independent fourteen-school fixture, using the real Spring generator without
the JUnit fixture/history helper or mocked weeks, confirmed these A matchups:

| Year | Offset | No protected rivals | Protected rivals (A-H) |
| --- | ---: | --- | --- |
| 2003 | 2 | A at J; K at A | H at A; A at K |
| 2004 | 1 | A at I; J at A | A at H; J at A |
| 2005 | 0 | A at H; I at A | H at A; A at I |
| 2006 | 1 | A at I; J at A | A at H; J at A |
| 2007 | 2 | A at J; K at A | H at A; A at K |

All traced seasons contain 56 games. A-J is absent in 2005, but A hosts both
its 2004 and 2006 meetings in both formats. Without rivals, the first rotating
opponent is always away and the second always home, so repeating the rotation
position repeats those venues. With rivals, the protected game correctly uses
calendar-year parity; the rotating game's venue fills the fourth home slot.
The same rotation position and year parity in 2004/2006 therefore repeat that
venue too. The protected rivalry itself is not the failing matchup.

This explains why the 1980/2000 starting venue cases fail, while the 2005/2020
cases pass: only the former retain meetings from both sides of the reversal.
Annual balance remains correct; the failure is next-meeting alternation across
the historical boundary, rather than a general failure of post-2005 rotation.

A narrow candidate fix is a signed season offset, with `Math.floorMod` at each
cycle calculation (seven seasons without rivals, twelve with rivals). This would
keep the existing 2005-and-later schedule while extending the cycle backward
instead of reflecting it. Merely removing `Math.abs` would leave negative Java
remainders and invalid opponent indexes. No fourteen-team production code or
permanent tests were changed during this diagnosis; the candidate still needs
implementation and regression verification if selected.

### Fix: fourteen teams, eight games (2026-10-22)

Both eight-game paths now use the signed `year - 2005` offset and
`Math.floorMod` for their respective seven- and twelve-season cycles. This
extends the existing rotation backward without the 2005 reversal. The nine-game
paths are unchanged.

An independent real-generator comparison confirmed identical complete matchup
and host lists before and after the fix for all 56 sampled seasons (2005-2032,
both protected-rival settings). Weeks were excluded because week selection is
random. The existing requirement tests exercise historical seasons, the 2005
boundary, cycle boundaries, reversed opposing-division order, annual balance,
protected/divisional opponents, and next-meeting venue alternation.

Full-suite verification after this fix: **798 tests passed, zero failures,
errors, or skips**. This includes all 48 rotation requirement cases, including
the eight previously failing fourteen-team cases, and the twelve-team
preservation tests. No tests were disabled or weakened.

### Test-first opponent frequency audit (2026-10-22)

ConferenceOpponentFrequencyTest adds 32 cases covering all eight combinations
of 12/14 teams, 8/9 games, and protected rivals on/off. Each uses starting years
1980 and 2000, normal/reversed opposite-division order, and rolling windows
covering every phase of the existing rotation. Expected rotating frequency is
derived from available cross-division slots divided by eligible opponents;
protected pairs are checked separately for annual meetings. Home/away totals
are also checked over windows allowing equal venue counts.

The comparison windows include full existing patterns and enough repetitions
for equal counts to be integral. In particular, the 12-team nine-game protected
format needs a 60-season comparison because its existing 12-season pattern
assigns 36 rotating games among five opponents. This is a diagnostic window,
not a requirement allowing a team to wait 60 years to meet an opponent.

Focused run: **32 tests, 24 passing, eight failing**, no errors or skips.
All four cases fail for each of these formats:

* 12 teams, eight games, no protected rivals: in a 12-season cycle, relative
  cross-division offsets 0..5 occur 12,4,6,4,6,4 times, instead of six each.
  Offset zero is always selected despite no configured protected rivalry.
* 12 teams, nine games, protected rivals: offset zero correctly plays annually,
  but rotating offsets 1/2 play six times each per 12 seasons, while offsets
  3/4/5 play eight times each. Over 60 seasons these become 30 vs 40 meetings,
  instead of 36 each. This imbalance persists through repeated cycles.

The other six formats pass their frequency and venue-total assertions. These
failures are consistent with an independent count of the offset patterns in
the production code. No production changes were made in this test-first audit.
The new tests remain enabled; the full suite was not rerun after adding them.
The preceding 798-test green baseline does not include these 32 cases.

### Equal-frequency fix: twelve teams, eight games, no rivals (2026-10-22)

The cross-division generator now alternates two groups of three complete
matchings. Each group plays a two-season home-and-away series before switching
to the other group. Every cross-division pairing therefore meets twice in
every rolling four-season window, once at each venue. There is no fixed annual
cross-division opponent.

Venue assignments give schools with three divisional home games one home
cross-division game, and schools with two divisional home games two. Both
divisions finish with four home and four away conference games. Each pairing's
base venue is fixed across cycle repeats, with odd seasons reversing it.

The frequency tests retain the original twelve-season regression window and
add the stronger four-season requirement. Annual and next-meeting requirement
tests now also include this format across four starting years and both opposing
division orders. The separate twelve-team nine-game protected-rival frequency
issue is outside this change and remains exposed by enabled tests.

Full-suite verification: **850 tests, 846 passing, four failing**, zero errors
or skips. All 64 annual/next-meeting requirement cases pass. The only failures
are the four existing frequency cases for twelve teams, nine games, protected
rivals. No tests were disabled or weakened, and no other regressions appeared.

### Diagnosis: twelve teams, nine games, protected rivals (2026-10-22)

schedule9GamesWithXDivRivals fixes the protected matchup at offset zero, then
selects one offset from 1/2 and two from 3/4/5. Each selection persists for a
two-season venue pair. The six rotating selections are 1/4/5, 2/4/5, 1/3/5,
2/3/5, 1/3/4, 2/3/4. Thus offsets 1/2 each occupy three season pairs (six
meetings), while offsets 3/4/5 each occupy four pairs (eight meetings).

This split is coupled to the venue rule: offsets 0/1/2 host on one division's
side in even years, while 3/4/5 host on the other side; odd years reverse it.
The protected home matching plus one rotating home matching and two rotating
away matchings ensures two home/two away cross-division games. Combined with
the five divisional games this satisfies annual four/five home alternation and
next-meeting venue reversal, but permanently biases opponent selection.

The four frequency failures cover two starting years and two division orders,
not four distinct defects. The protected matchup is correctly annual and is
excluded from the equal-frequency comparison. Sixty years is only the test's
common comparison window for the current twelve-year period: five repetitions
produce 30 or 40 rotating meetings, where equal frequency would require 36.
The imbalance is not an artifact of rounding a twelve-year window.

A replacement must rotate three games among five opponents fairly while
retaining the annual protected game and existing venue requirements. Ten seasons
contain thirty rotating slots, allowing six meetings per opponent, three at each
venue. That is a useful target, not an implemented or verified replacement.
Opponent selection and venue assignment need to be redesigned together: retaining
the current fixed two-opponent home pool and three-opponent away pool cannot
yield equal long-run frequency. No production code or tests changed in this
diagnosis.

### Equal-frequency fix: twelve teams, nine games, protected rivals (2026-10-22)

The protected matching remains annual. The three other cross-division matchings
now advance continuously through offsets 1..5, with the hosting division
alternating by selection slot. Each rotating pair returns every five slots,
so every next meeting swaps venue even after skipped seasons. Ten seasons
complete thirty slots: six meetings per rotating opponent, three at each venue.

In even seasons, the rotating games give the first division two home games and
the protected game is away; odd seasons give one rotating home game and the
protected game at home. Both divisions therefore receive two cross-division
home games annually. Combined with the five divisional games, each school
alternates between five and four home conference games.

This replaces consecutive two-year opponent blocks with a ten-year slot cycle;
opponent selections and venues change for this format. No guarantee that a
rotating opponent plays in consecutive seasons is required. The protected
rivalry, annual balance, and next-actual-meeting venue requirements remain.

Four new ten-year frequency cases failed against the old generator, alongside
the four original sixty-year cases (40 focused cases, eight failures). Both
windows are retained. The annual/next-meeting requirement matrix also now
includes this format across four starting years and both opposite-division
orders, exercising 28 consecutive seasons per case.

Full-suite verification: **870 tests passed, zero failures, errors, or skips**.
All forty frequency cases and eighty annual/next-meeting requirement cases pass,
alongside the original integration and scheduling regressions. No tests were
disabled or weakened.

## Running checks

Use JDK 21 and `./mvnw test` (Windows: `./mvnw.cmd test`). Avoid `verify` for
ordinary tests because the POM binds native installer packaging to that phase.
The GitHub Actions workflow runs the full suite and uploads Surefire reports even
when tests fail. Earlier red baselines above record the progression to a passing
suite; a passing suite establishes only the behavior covered by its assertions.

The initial local run needed an ignored temporary Maven settings file pointing at
Maven Central because the machine's corporate mirror did not resolve. Personal
Maven settings are not changed or committed.

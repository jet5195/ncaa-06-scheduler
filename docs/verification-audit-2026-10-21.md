# Scheduling safety and test-coverage audit

Date: October 21, 2026. Reviewed checkpoint: `e8b41f8`, branch
`feature/codex-test-fixes`. This is a verification pass, not a production fix.

The existing suite concentrates on successful synthetic conference generation.
Its passing count does not establish safe editing, safe imports, faithful exports,
postseason separation, or all supported-format boundaries.

## Evidence and scope

Isolated executable probes used the real ScheduleService and in-memory game
repository. School lookup and import parsing were mocked only where needed to
control an input or simulate a parse failure. Rotation probes used the real Spring
application context, conference factory and generated fixtures, from 1980 through
2007. These probes did not write dynasty files or change production behavior.

Twenty-one diagnostic checks ran: seven passed and fourteen failed. Several
failures are multiple manifestations of one issue, not fourteen distinct bugs.
The normal Maven suite was also rerun on this checkpoint: all 742 cases passed,
with zero failures, errors or skips. The diagnostic failures are newly exposed
coverage gaps; they are not failures of the existing JUnit run.
The diagnostic harnesses and logs are local under `target/verification/`, outside
the normal JUnit suite. Their failures must become permanent regression tests
when each issue is addressed; the normal suite does not yet guard these cases.

## Confirmed findings, in recommended order

### 1. Rejected edits delete the original game (high priority)

Source: `service/ScheduleService.java:702` (`saveGame`).

Reproduction: schedule A-B in week 0 and A-C in week 1. Edit A-B to week 1.
The edit removes A-B, mutates it, then calls addGame. Because A is already busy,
addGame refuses the replacement. The repository ends with one game, and the old
object has already been changed to week 1. There is no rollback or explicit
failure result from addGame.

Required regression: a rejected edit preserves all original fields, games and
game identifiers. Validate a proposed replacement before committing any mutation.

### 2. Failed imports clear the existing schedule (high priority)

Source: `service/ScheduleService.java:147` (`setScheduleFile`).

Reproduction: start with a valid game and make populateSeasonScheduleFromExcel
throw IOException. removeAllGames has already executed, the IOException is
printed and swallowed, and the repository is empty.

Required regression: parse/validate into temporary state first; only replace live
state after success. The API must surface failures and preserve existing data.
Alignment/school imports also need equivalent failed-operation coverage.

### 3. A valid team schedule swap loses games (high priority)

Source: `service/ScheduleService.java:732` (`swapSchedule`).

Reproduction: schedule A-C and B-D in the same week, then swap A and B's
schedules. The desired result is B-C and A-D. The method attempts to add B-C
while B-D is still present, rejects the transient conflict, and leaves only A-D.

Required regression: apply the entire swap atomically, check the final schedule,
preserve game metadata, and test a direct A-B matchup as well as overlapping weeks.

### 4. New games export duplicate schedule identifiers (high priority)

Sources: `model/GameBuilder.java:14`, `service/ScheduleService.java:79`, `:568`.

Reproduction: add A-B and C-D in the same week using the builder defaults, then
download the CSV. Both rows export SEWN=0 and SGNM=0. GameBuilder defaults SGNM
to zero; addGame/downloadSchedule do not assign or recalculate identifiers.

The model itself states gameNumber must be unique per week, and API game lookup
also relies on this pair. A generated schedule with valid opponents can therefore
still have invalid/ambiguous identities. No claim was made about emulator behavior.

Required regressions: unique exported week/game-number keys after generation,
addition, edit, deletion and swapping; preserve bowl identifiers. Add an export
round-trip test using known fixture data, not only domain-object totals.

### 5. Regular-season totals include postseason games

Sources: `service/ScheduleService.java:1103` and `:1116`.

Confirmed both cases: eleven regular games plus a week-18 bowl is treated as a
complete twelve-game season; twelve regular games plus a bowl is flagged as too
many games. These queries feed auto-filling and removeExtraGames, so the
misclassification reaches modifying operations. Exactly which game a repair
would remove was not tested in this audit.

Required regressions: twelve means twelve regular-season games; postseason
records neither fill regular-season gaps nor trigger regular-season removal.

### 6. Next-meeting venue alternation still fails in three older paths

The existing generic venue test compares consecutive seasons only. The new
gap-aware probes retained the last host for every pair through 28 seasons.

| Format | Reproduction |
| --- | --- |
| 12 teams, 9 games, no protected rivals | Pair 5:7 is hosted by team 7 in both 1980 and 1982 |
| 14 teams, 8 games, no protected rivals | Pair 1:10 is hosted by team 1 in both 2004 and 2006 |
| 14 teams, 8 games, protected rivals | Pair 1:10 is hosted by team 1 in both 2004 and 2006 |

Required regression: retain the last venue across missing seasons, with real
calendar years and cycle boundaries. The 14-team eight-game year-offset formula
uses abs(year - 2005), so the behavior around 2005 deserves particular attention.
This is a diagnostic lead, not a complete root-cause claim.

### 7. Overtime data is exported in the wrong column

Sources: `model/Game.java:201`, `ExcelReader.java:356`.

A game with GameResult.ot=2 exports GFOT=0 and GSTA=2. Import reads overtime from
GFOT (column 9), while gameToList writes it to column 0 and emits zero in column
9. Thus this field cannot survive an export/import round trip. The separate
meaning and preservation of GSTA also need an actual database fixture check.

Required regression: nonzero overtime counts survive export/import in GFOT.
Other stored schedule fields should get the same preservation checks.

### 8. Unsupported formats silently return empty schedules

Sources: `scheduler/conference/ConferenceSchedulerFactory.java:21` and the
13-/15-/16-team scheduler stubs.

All three factory-selected generators returned normally with zero games.
Unsupported formats should be rejected explicitly rather than appear successful.
This was known from source review, but is now confirmed through the factory.

### 9. Invalid weeks are accepted

Source: `service/ScheduleService.java:88` (`canScheduleGame`).

A game in week -1 passes validation. Existing checks cover self-games, occupied
weeks and duplicate matchups, but not the lower calendar bound. Unknown teams,
upper bounds, regular/postseason distinctions and invalid editing inputs need
their own tests; only the negative-week case was executed here.

## Checks that passed

- A valid game can be added and a self-game is rejected (control check).
- Editing opponents updates the conference flag through the current setters.
  This suspected problem was disproved by the diagnostic.
- Gap-aware next-meeting venue probes passed for both 12-team eight-game paths,
  the protected 12-team nine-game path, and both repaired 14-team nine-game paths.
- Existing targeted tests already establish equal frequency for the unprotected
  14-team nine-game format; that recent repair was not implicated by these findings.

## Other coverage gaps, not established as new bugs here

- SchoolServiceTest and ExcelReaderTest remain empty. No controller failure-path
  or complete CSV/XLSX round-trip tests exist.
- Nonconference integration checks season totals, not structural schedule validity,
  game IDs, field preservation or failed-operation behavior.
- Invalid/missing divisions, nonreciprocal protected rivals, configured game counts
  incompatible with team counts, and existing schedules need rejection tests.
- Input order, sparse data and null metadata deserve broader fixture coverage.
- Randomness lacks a common seed. Reproducible seed sets and shuffled school orders
  would give stronger coverage than repeating the same unseeded green suite.
- Uploaded files use their supplied names in the working directory. This is why
  test runs can rewrite root-level workbooks. Temporary-file handling and cleanup
  deserve a focused review before broadening file import.
- Conference.equals compares IDs while hashCode uses names: this violates the
  equality/hash contract for equal IDs with different names. This was observed
  in source, not exercised by the runtime probes or tied to a scheduling failure.

## Recommended next work

1. Promote the rejected-edit/import/swap probes into regression tests and fix
   data-preserving mutation behavior first.
2. Add an independent exported-schedule validator and round-trip field tests;
   resolve game numbering and overtime preservation.
3. Separate regular-season and postseason counting, then test the modifying
   operations that consume those counts.
4. Extend gap-aware venue regression coverage to all eight supported 12/14-team
   configurations and repair the three remaining older paths.
5. Add explicit unsupported-format and malformed-input failures, followed by
   reproducible randomized and input-order tests.

Feature development and broad refactoring can wait until these safety boundaries
have permanent tests. Passing the current generation suite remains useful, but
does not cover the failure modes demonstrated above.

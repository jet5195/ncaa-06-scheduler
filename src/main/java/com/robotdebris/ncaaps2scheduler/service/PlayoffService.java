package com.robotdebris.ncaaps2scheduler.service;

import com.robotdebris.ncaaps2scheduler.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class PlayoffService {

    @Autowired
    ScheduleService scheduleService;
    @Autowired
    BowlService bowlService;
    @Autowired
    List<Bowl> bowlList;
    List<PlayoffSchool> playoffSchools;

    int placeholderTgid = 120;

    void schedulePlayoff() {
        // check if bowl week 1 has games played
        // if not
        // move bowl games to week 1
        //
    }

    public List<PlayoffSchool> getPlayoffTeams() {
        return this.playoffSchools;
    }

    public void setPlayoffTeams(List<Integer> tgids) {
        this.playoffSchools = new ArrayList<>();
        int seed = 1;
        for (Integer tgid : tgids) {
            School school = scheduleService.searchSchoolByTgid(tgid);
            PlayoffSchool playoffSchool = new PlayoffSchoolBuilder().setSchool(school).setSeed(seed).createPlayoffSchool();
            this.playoffSchools.add(playoffSchool);
            seed++;
        }
        updateBowlCsv();
    }

    public void updateBowlCsv() {
        List<PlayoffGame> playoffGames = new ArrayList<>();
        School placeholder = scheduleService.searchSchoolByTgid(placeholderTgid);

        // null check
        if (this.playoffSchools == null || this.playoffSchools.isEmpty()) {
            // error handling
        }
        Bowl championship = bowlService.findChampionship();
        int lastWeek = championship.getWeek();

        // schedule 1st round, don't care about which bowls?
        int currWeek = lastWeek - 3;
        int round = 1;
        List<Game> weeklySchedule = this.scheduleService.getScheduleByWeek(currWeek);

        // steal some games from the next week to populate games needed
        while (weeklySchedule.size() < 4) {
            List<Game> nextWeek = this.scheduleService.getScheduleByWeek(currWeek + 1);
            while (!nextWeek.isEmpty() && weeklySchedule.size() < 4) {
                weeklySchedule.add(nextWeek.get(0));
                nextWeek.remove(0);
            }
        }
        for (int i = 0; i < 4; i++) {
            Game game = weeklySchedule.get(i);
            PlayoffGame pg = new PlayoffGame(game, round);
            pg.setWeek(currWeek);
            playoffGames.add(pg);
            if (i == 0) {
                pg.setHomeTeam(playoffSchools.get(7));
                pg.setAwayTeam(playoffSchools.get(8));
            } else if (i == 1) {
                pg.setHomeTeam(playoffSchools.get(4));
                pg.setAwayTeam(playoffSchools.get(11));
            } else if (i == 2) {
                pg.setHomeTeam(playoffSchools.get(5));
                pg.setAwayTeam(playoffSchools.get(10));
            } else if (i == 3) {
                pg.setHomeTeam(playoffSchools.get(6));
                pg.setAwayTeam(playoffSchools.get(9));
            }
            this.scheduleService.saveGame(new AddGameRequest(pg.getAwayTeam().getTgid(), pg.getHomeTeam().getTgid(),
                    pg.getWeek(), pg.getTime(), pg.getDay(), pg.getGameResult()), game.getWeek(), game.getGameNumber());
        }
        round++;
        currWeek++;

        // find new year's 6
        // search based on logo ID!
        Bowl orangeBowl = bowlService.findBowl(24);
        Bowl sugarBowl = bowlService.findBowl(31);
        Bowl fiestaBowl = bowlService.findBowl(8);
        Bowl roseBowl = bowlService.findBowl(28);
        Bowl peachBowl = bowlService.findBowl(26);
        Bowl cottonBowl = bowlService.findBowl(7);
        // if needed 7th game
        Bowl citrusBowl = bowlService.findBowl(6);

        List<Bowl> ny6List = new ArrayList<>();
        ny6List.add(orangeBowl);
        ny6List.add(sugarBowl);
        ny6List.add(fiestaBowl);
        ny6List.add(roseBowl);
        ny6List.add(peachBowl);
        ny6List.add(cottonBowl);
        ny6List.add(citrusBowl);

        // now add all these bowls to a playoffGameList and remove the game that is also
        // the nationalchampionship, so it is not included twice
        // set round & week for quarterfinals, semifinals

        int i = 0;
        // do this so we save it in order
        List<PlayoffGame> quarterfinals = new ArrayList<>();
        List<PlayoffGame> semifinals = new ArrayList<>();
        for (Bowl bowl : ny6List) {
            if (bowl != championship) {
                // go to the next round after scheduling game 4 (after quarterfinals) and 6
                // (after semifinals)
                if (i == 4 || i == 6) {
                    round++;
                    currWeek++;
                }
                Game game = this.scheduleService.getGame(bowl.getWeek(), bowl.getGameNumber());
                // changeBowlWeek(bowl, game, currWeek);
                PlayoffGame pg = new PlayoffGame(game, round);
                pg.setWeek(currWeek);
                playoffGames.add(pg);
                // schedule the 1 seed game, then 4 seed, then 3, then 2 (to match the order of
                // the bracket)
                if (i < 4) {
                    pg.setHomeTeam(playoffSchools.get(i));
                    pg.setAwayTeam(placeholder);
                    quarterfinals.add(pg);
//                } else if (i == 1) {
//                    pg.setHomeTeam(playoffSchools.get(3));
//                    pg.setAwayTeam(placeholder);
//                    quarterfinals.add(pg);
//                } else if (i == 2) {
//                    pg.setHomeTeam(playoffSchools.get(2));
//                    pg.setAwayTeam(placeholder);
//                    quarterfinals.add(pg);
//                } else if (i == 3) {
//                    pg.setHomeTeam(playoffSchools.get(1));
//                    pg.setAwayTeam(placeholder);
//                    quarterfinals.add(pg);
                } else {
                    pg.setHomeTeam(placeholder);
                    pg.setAwayTeam(placeholder);
                    semifinals.add(pg);
                }
                this.scheduleService
                        .saveGame(
                                new AddGameRequest(pg.getAwayTeam().getTgid(), pg.getHomeTeam().getTgid(), pg.getWeek(),
                                        pg.getTime(), pg.getDay(), pg.getGameResult()),
                                game.getWeek(), game.getGameNumber());
                i++;
            }
        }

        // manipulate the order of the games a little (we need quarterfinals to be 1
        // seed, then 4, then 3, then 2
        this.scheduleService.saveGame(new AddGameRequest(quarterfinals.get(2).getAwayTeam().getTgid(),
                        quarterfinals.get(2).getHomeTeam().getTgid(), quarterfinals.get(2).getWeek(),
                        quarterfinals.get(2).getTime(), quarterfinals.get(2).getDay(), quarterfinals.get(2).getGameResult()),
                quarterfinals.get(2).getWeek(), quarterfinals.get(2).getGameNumber());
        this.scheduleService.saveGame(new AddGameRequest(quarterfinals.get(1).getAwayTeam().getTgid(),
                        quarterfinals.get(1).getHomeTeam().getTgid(), quarterfinals.get(1).getWeek(),
                        quarterfinals.get(1).getTime(), quarterfinals.get(1).getDay(), quarterfinals.get(1).getGameResult()),
                quarterfinals.get(1).getWeek(), quarterfinals.get(1).getGameNumber());
        // now add the games in order
//        this.scheduleService.saveGame(new AddGameRequest(quarterfinals.get(0).getAwayTeam().getTgid(), quarterfinals.get(0).getHomeTeam().getTgid(), quarterfinals.get(0).getWeek(), quarterfinals.get(0).getTime(), quarterfinals.get(0).getDay(), quarterfinals.get(0).getGameResult()),
//                quarterfinals.get(0).getWeek(), quarterfinals.get(0).getGameNumber());
//        this.scheduleService.saveGame(new AddGameRequest(quarterfinals.get(0).getAwayTeam().getTgid(), quarterfinals.get(0).getHomeTeam().getTgid(), quarterfinals.get(0).getWeek(), quarterfinals.get(0).getTime(), quarterfinals.get(0).getDay(), quarterfinals.get(0).getGameResult()),
//                quarterfinals.get(0).getWeek(), quarterfinals.get(0).getGameNumber());

        // add finals game to playoff list
        Game game = this.scheduleService.getGame(championship.getWeek(), championship.getGameNumber());
        playoffGames.add(new PlayoffGame(game, 4));

        // iterate through the quarterfinal games and so on. Start at 4 since 0-3 are
        // 1st round
        // iterate through the playoff schools starting at 0, since they're sorted by
        // seed
//        for (int j = 4, k = 0; i < playoffGames.size(); j++, k++) {
//            PlayoffGame pg = playoffGames.get(j);
//            if (j<7){
//                pg.setHomeTeam(playoffSchools.get(k));
//                pg.setAwayTeam(placeholder);
//            } else {
//                pg.setHomeTeam(placeholder);
//                pg.setAwayTeam(placeholder);
//            }
//            this.scheduleService.saveGame(new AddGameRequest(pg.getAwayTeam().getTgid(), pg.getHomeTeam().getTgid(), pg.getWeek(), pg.getTime(), pg.getDay(), pg.getGameResult()),
//                    pg.getWeek(), pg.getGameNumber());
//        }
        bowlService.recalculateGameNumbers();
    }

//    public void changeBowlWeek(Bowl bowl, Game game, int currentWeek){
//        bowl.setWeek(currentWeek);
//        game.setWeek(currentWeek);
//        // now we have to calculate the bidx & sgnm
//        bowlService.recalculateGameNumbers();
//    }

    public void setupQuarterfinals() {
        List<PlayoffGame> playoffGames = this.getPlayoffGamesAndPlayoffSchools();

        // see who won the 1st round games
        List<School> winners = new ArrayList<>();
        winners.add(playoffGames.get(0).getWinner());
        winners.add(playoffGames.get(1).getWinner());
        winners.add(playoffGames.get(2).getWinner());
        winners.add(playoffGames.get(3).getWinner());

        // set quarterfinals
        List<Game> quarterfinals = new ArrayList<>();
        quarterfinals.add(playoffGames.get(4));
        quarterfinals.add(playoffGames.get(5));
        quarterfinals.add(playoffGames.get(6));
        quarterfinals.add(playoffGames.get(7));

        for (int i = 0; i < quarterfinals.size(); i++) {
            Game game = quarterfinals.get(i);
            game.setAwayTeam(winners.get(i));

            this.scheduleService
                    .saveGame(
                            new AddGameRequest(game.getAwayTeam().getTgid(), game.getHomeTeam().getTgid(),
                                    game.getWeek(), game.getTime(), game.getDay(), game.getGameResult()),
                            game.getWeek(), game.getGameNumber());
        }
    }

    public List<PlayoffGame> getPlayoffGamesAndPlayoffSchools() {
        // is this needed?
        this.playoffSchools = new ArrayList<>();
        List<PlayoffGame> playoffGames = new ArrayList<>();
        Bowl championship = bowlService.findChampionship();

        // find firstRound
        int currWeek = championship.getWeek() - 3;
        List<Game> firstRound = this.scheduleService.getScheduleByWeek(currWeek);
        // find the games
        Game game1 = firstRound.get(0);
        Game game2 = firstRound.get(1);
        Game game3 = firstRound.get(2);
        Game game4 = firstRound.get(3);

        // throw teams into playoffSchools
        this.playoffSchools.add(new PlayoffSchoolBuilder().setSchool(game1.getHomeTeam()).setSeed(8).createPlayoffSchool());
        this.playoffSchools.add(new PlayoffSchoolBuilder().setSchool(game1.getAwayTeam()).setSeed(9).createPlayoffSchool());

        this.playoffSchools.add(new PlayoffSchoolBuilder().setSchool(game2.getHomeTeam()).setSeed(5).createPlayoffSchool());
        this.playoffSchools.add(new PlayoffSchoolBuilder().setSchool(game2.getAwayTeam()).setSeed(12).createPlayoffSchool());

        this.playoffSchools.add(new PlayoffSchoolBuilder().setSchool(game3.getHomeTeam()).setSeed(6).createPlayoffSchool());
        this.playoffSchools.add(new PlayoffSchoolBuilder().setSchool(game3.getAwayTeam()).setSeed(11).createPlayoffSchool());

        this.playoffSchools.add(new PlayoffSchoolBuilder().setSchool(game4.getHomeTeam()).setSeed(7).createPlayoffSchool());
        this.playoffSchools.add(new PlayoffSchoolBuilder().setSchool(game4.getAwayTeam()).setSeed(10).createPlayoffSchool());

        // find playoffSchools
        // start at quarterfinals because that's where teams with bye's start
        currWeek = championship.getWeek() - 2;
        List<Game> quarterfinals = this.scheduleService.getScheduleByWeek(currWeek);

        // find the games
        Game qfGame1 = quarterfinals.get(quarterfinals.size() - 4);
        Game qfGame2 = quarterfinals.get(quarterfinals.size() - 3);
        Game qfGame3 = quarterfinals.get(quarterfinals.size() - 2);
        Game qfGame4 = quarterfinals.get(quarterfinals.size() - 1);

        // throw teams into playoffSchools
        this.playoffSchools.add(new PlayoffSchoolBuilder().setSchool(qfGame1.getHomeTeam()).setSeed(1).createPlayoffSchool());
        this.playoffSchools.add(new PlayoffSchoolBuilder().setSchool(qfGame2.getHomeTeam()).setSeed(4).createPlayoffSchool());
        this.playoffSchools.add(new PlayoffSchoolBuilder().setSchool(qfGame3.getHomeTeam()).setSeed(3).createPlayoffSchool());
        this.playoffSchools.add(new PlayoffSchoolBuilder().setSchool(qfGame4.getHomeTeam()).setSeed(2).createPlayoffSchool());

        // at this point all playoffSchools have been added
        // add games to playoffGames list
        // round 1
        playoffGames.add(new PlayoffGame(game1, 1));
        playoffGames.add(new PlayoffGame(game2, 1));
        playoffGames.add(new PlayoffGame(game3, 1));
        playoffGames.add(new PlayoffGame(game4, 1));

        // quarterfinals
        playoffGames.add(new PlayoffGame(qfGame1, 2));
        playoffGames.add(new PlayoffGame(qfGame2, 2));
        playoffGames.add(new PlayoffGame(qfGame3, 2));
        playoffGames.add(new PlayoffGame(qfGame4, 2));

        // semifinals (this might be harder, right now edited games are saved to the
        // BOTTOM
        currWeek = championship.getWeek() - 1;
        List<Game> semifinals = this.scheduleService.getScheduleByWeek(currWeek);
        Game sfGame1 = semifinals.get(semifinals.size() - 2);
        Game sfGame2 = semifinals.get(semifinals.size() - 1);
        playoffGames.add(new PlayoffGame(sfGame1, 3));
        playoffGames.add(new PlayoffGame(sfGame2, 3));

        // Championship
        List<Game> championshipWeek = this.scheduleService.getScheduleByWeek(championship.getWeek());
        Game championshipGame = championshipWeek.get(championshipWeek.size() - 1);
        playoffGames.add(new PlayoffGame(championshipGame, 4));

        return playoffGames;
    }

    public void scheduleNextRound() {
        Bowl championship = bowlService.findChampionship();
        List<PlayoffGame> playoffGames = this.getPlayoffGamesAndPlayoffSchools();
        // check 1st round for results
        List<PlayoffGame> r1Games = new ArrayList<>();
        r1Games.add(playoffGames.get(0));
        r1Games.add(playoffGames.get(1));
        r1Games.add(playoffGames.get(2));
        r1Games.add(playoffGames.get(3));

        List<PlayoffGame> qfGames = new ArrayList<>();
        qfGames.add(playoffGames.get(4));
        qfGames.add(playoffGames.get(5));
        qfGames.add(playoffGames.get(6));
        qfGames.add(playoffGames.get(7));

        List<PlayoffGame> sfGames = new ArrayList<>();
        sfGames.add(playoffGames.get(8));
        sfGames.add(playoffGames.get(9));

        Game champGame = playoffGames.get(10);

        if (r1Games.get(0).getGameResult().getHomeScore() == 0 && r1Games.get(0).getGameResult().getAwayScore() == 0) {
            // then we can't do anything so fail here
        } else if (qfGames.get(0).getGameResult().getHomeScore() == 0
                && qfGames.get(0).getGameResult().getAwayScore() == 0) {
            // schedule qf games
            for (int i = 0; i < qfGames.size(); i++) {
                Game game = qfGames.get(i);
                Game r1Game = r1Games.get(i);

                game.setAwayTeam(r1Game.getWinner());
                this.scheduleService.saveGame(
                        new AddGameRequest(game.getAwayTeam().getTgid(), game.getHomeTeam().getTgid(), game.getWeek(),
                                game.getTime(), game.getDay(), game.getGameResult()),
                        game.getWeek(), game.getGameNumber());
            }
        } else if (sfGames.get(0).getGameResult().getHomeScore() == 0
                && sfGames.get(0).getGameResult().getAwayScore() == 0) {
            // schedule sf games
            // schedule game 1
            PlayoffGame game = sfGames.get(0);
            PlayoffSchool s1 = findPlayoffSchool(qfGames.get(0).getWinner());
            PlayoffSchool s2 = findPlayoffSchool(qfGames.get(1).getWinner());
            if (s1.getSeed() < s2.getSeed()) {
                game.setHomeTeam(s1);
                game.setAwayTeam(s2);
            } else {
                game.setHomeTeam(s2);
                game.setAwayTeam(s1);
            }
            this.scheduleService
                    .saveGame(
                            new AddGameRequest(game.getAwayTeam().getTgid(), game.getHomeTeam().getTgid(),
                                    game.getWeek(), game.getTime(), game.getDay(), game.getGameResult()),
                            game.getWeek(), game.getGameNumber());

            // schedule game 2
            PlayoffGame game2 = sfGames.get(1);
            PlayoffSchool s3 = findPlayoffSchool(qfGames.get(2).getWinner());
            PlayoffSchool s4 = findPlayoffSchool(qfGames.get(3).getWinner());
            if (s3.getSeed() < s4.getSeed()) {
                game2.setHomeTeam(s3);
                game2.setAwayTeam(s4);
            } else {
                game2.setHomeTeam(s4);
                game2.setAwayTeam(s3);
            }
            this.scheduleService.saveGame(
                    new AddGameRequest(game2.getAwayTeam().getTgid(), game2.getHomeTeam().getTgid(), game2.getWeek(),
                            game2.getTime(), game2.getDay(), game2.getGameResult()),
                    game2.getWeek(), game2.getGameNumber());

        } else if (champGame.getGameResult().getHomeScore() == 0 && champGame.getGameResult().getAwayScore() == 0) {
            // schedule champ games
            // schedule game 1
            PlayoffSchool s1 = findPlayoffSchool(sfGames.get(0).getWinner());
            PlayoffSchool s2 = findPlayoffSchool(sfGames.get(1).getWinner());
            if (s1.getSeed() < s2.getSeed()) {
                champGame.setHomeTeam(s1);
                champGame.setAwayTeam(s2);
            } else {
                champGame.setHomeTeam(s2);
                champGame.setAwayTeam(s1);
            }
            this.scheduleService.saveGame(
                    new AddGameRequest(champGame.getAwayTeam().getTgid(), champGame.getHomeTeam().getTgid(),
                            champGame.getWeek(), champGame.getTime(), champGame.getDay(), champGame.getGameResult()),
                    champGame.getWeek(), champGame.getGameNumber());
        } else {
            // you're finished, what are you trying to do?
        }
    }

    public PlayoffSchool findPlayoffSchool(School school) {
        for (PlayoffSchool ps : this.playoffSchools) {
            if (ps.getTgid() == school.getTgid()) {
                return ps;
            }
        }
        return null;
    }

    public void scheduleNextRoundBak() {
        Bowl championship = bowlService.findChampionship();
        // find first week
        // int currWeek = championship.getWeek()-3;
//        List <Game> firstRound = this.scheduleService.getScheduleByWeek(currWeek);
//        Game game1 = firstRound.get(0);
        /// maybe do a for loop
        for (int i = 2; i >= 0; i--) {
            int currWeek = championship.getWeek() - i;
            List<Game> round = this.scheduleService.getScheduleByWeek(currWeek);
            Game game1 = round.get(0);

            // Check if game has occurred
            if (game1.getGameResult().getHomeScore() == 0 && game1.getGameResult().getAwayScore() == 0) {
                // game has not occurred, so schedule the whole week then break out of the loop

                // schedule quarterfinals
                if (i == 2) {
                    // get last week's schedule
                    List<Game> firstRound = this.scheduleService.getScheduleByWeek(currWeek - 1);
                    for (int j = 0; j < 4; j++) {
                        School opponent = firstRound.get(j).getWinner();
                        Game game = round.get(round.size() - 3 + j);
                        game.setAwayTeam(opponent);
                        this.scheduleService.saveGame(
                                new AddGameRequest(game.getAwayTeam().getTgid(), game.getHomeTeam().getTgid(),
                                        game.getWeek(), game.getTime(), game.getDay(), game.getGameResult()),
                                game.getWeek(), game.getGameNumber());
                    }
                    // schedule semifinals
                }
                if (i == 1) {
                    // get last week's schedule
                    List<Game> quarterfinals = this.scheduleService.getScheduleByWeek(currWeek - 1);
                    for (int j = 0; j < 4; j++) {
                        School opponent = quarterfinals.get(j).getWinner();
                        Game game = round.get(round.size() - 3 + j);
                        game.setAwayTeam(opponent);
                        this.scheduleService.saveGame(
                                new AddGameRequest(game.getAwayTeam().getTgid(), game.getHomeTeam().getTgid(),
                                        game.getWeek(), game.getTime(), game.getDay(), game.getGameResult()),
                                game.getWeek(), game.getGameNumber());
                    }
                    // schedule championship
                }
                if (i == 0) {
                    // get last week's schedule
                    List<Game> semifinals = this.scheduleService.getScheduleByWeek(currWeek - 1);
                    for (int j = 0; j < 2; j++) {
                        School opponent = semifinals.get(j).getWinner();
                        Game game = round.get(round.size() - 3 + j);
                        game.setAwayTeam(opponent);
                        this.scheduleService.saveGame(
                                new AddGameRequest(game.getAwayTeam().getTgid(), game.getHomeTeam().getTgid(),
                                        game.getWeek(), game.getTime(), game.getDay(), game.getGameResult()),
                                game.getWeek(), game.getGameNumber());
                    }

                    // I don't love this, but it'll break us out of the loop
                    i = 100;
                }

            }
        }
    }
}
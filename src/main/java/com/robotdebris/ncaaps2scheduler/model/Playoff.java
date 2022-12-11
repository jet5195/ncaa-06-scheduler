package com.robotdebris.ncaaps2scheduler.model;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Component
public class Playoff{
    private List<PlayoffGame> playoffGames;

}

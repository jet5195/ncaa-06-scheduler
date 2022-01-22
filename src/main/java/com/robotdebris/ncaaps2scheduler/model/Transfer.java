package com.robotdebris.ncaaps2scheduler.model;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class Transfer implements Comparable {
	private int playerId;//pgid
    private School team;//ptid, is this former team or future team???
    private int transferYear;//tryr, can be 0 or 1 it seems

    public Transfer() {

    }

    public Transfer(int playerId, School team, int transferYear) {
        this.playerId = playerId;
    	this.team = team;
        this.transferYear = transferYear;
    }

    public int getPlayerId() {
        return playerId;
    }

    public void setPlayerId(int playerId) {
        this.playerId = playerId;
    }

    public School getTeam() {
        return team;
    }

    public void setTeam(School team) {
        this.team = team;
    }

    public int getTransferYear() {
        return transferYear;
    }

    public void setTransferYear(int transferYear) {
        this.transferYear = transferYear;
    }

    /**
     * Compares 2 transfers
     * @param transfer2 the transfer to compare
     * @return a negative number if this transfer's playerId is before lower than transfer2
     */
    public int compareTo(@NotNull Object transfer2) {
        int comparePlayerId = ((Transfer) transfer2).getPlayerId();
        return this.getPlayerId() - comparePlayerId;
    }
}

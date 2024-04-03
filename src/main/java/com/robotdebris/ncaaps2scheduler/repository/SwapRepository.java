package com.robotdebris.ncaaps2scheduler.repository;

import com.robotdebris.ncaaps2scheduler.model.Swap;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SwapRepository {
    List<Swap> findAll();

    void saveAll(List<Swap> swaps);

    void saveSwap(Swap swap);

}

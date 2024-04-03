package com.robotdebris.ncaaps2scheduler.repository;

import com.robotdebris.ncaaps2scheduler.model.Swap;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Repository
public class ExcelSwapRepository implements SwapRepository {

    List<Swap> swapList = new ArrayList<>();

    @Override
    public List<Swap> findAll() {
        return swapList;
    }

    @Override
    public void saveAll(List<Swap> swaps) {
        this.swapList = swaps;
    }

    @Override
    public void saveSwap(Swap swap) {
        this.swapList.add(swap);
    }
}

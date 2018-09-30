package com.grepstar.starcraft

class Player {

    int maxMmr
    List<LadderRank> ranks = []

    int getMaxMmr() {
        if (maxMmr == 0) {
            maxMmr = ranks.collect { rank ->
                rank.mmr
            }.max()
        }
        maxMmr
    }

}

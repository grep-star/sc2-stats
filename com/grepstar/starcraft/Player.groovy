package com.grepstar.starcraft

class Player implements Comparable<Player> {

    int maxMmr
    int id
    Region region
    List<LadderRank> ranks

    Player(LadderRank rank) {
        setId(rank.playerId)
        setRegion(rank.region)
        setRanks([rank])
    }

    int getMaxMmr() {
        if (maxMmr == 0) {
            maxMmr = ranks.collect { rank ->
                rank.mmr
            }.max()
        }
        maxMmr
    }

    @Override
    int compareTo(Player o) {
        o.getMaxMmr() <=> getMaxMmr()
    }

}

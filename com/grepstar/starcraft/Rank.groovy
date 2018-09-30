package com.grepstar.starcraft

class Rank {

    League league
    int tier

    Rank(League league, int tier) {
        setLeague(league)
        setTier(tier)
    }

    @Override
    String toString() {
        (league == League.GRANDMASTER) ? league.toString() : "${league} ${tier}"
    }

}

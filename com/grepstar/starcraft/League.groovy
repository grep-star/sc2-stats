package com.grepstar.starcraft

enum League {

    BRONZE ('Bronze', 0),
    SILVER ('Silver', 1),
    GOLD ('Gold', 2),
    PLATINUM ('Platinum', 3),
    DIAMOND ('Diamond', 4),
    MASTER ('Master', 5),
    GRANDMASTER ('Grandmaster', 6)

    String leagueName
    int leagueId

    League(String leagueName, int leagueId) {
        setLeagueName(leagueName)
        setLeagueId(leagueId)
    }

    @Override
    String toString() {
        leagueName
    }

}

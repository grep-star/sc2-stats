package com.grepstar.starcraft

enum Race {

    TERRAN ('Terran'),
    PROTOSS ('Protoss'),
    ZERG ('Zerg'),
    RANDOM ('Random')

    private String simpleName

    Race(String simpleName) {
        this.simpleName = simpleName
    }

    @Override
    String toString() {
        simpleName
    }

}

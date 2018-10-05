package com.grepstar.starcraft

enum Region {

    KR ('KR'),
    EU ('EU'),
    US ('US'),
    CN ('CN') {
        @Override
        String getApiBase() {
            "https://api.battlenet.com.cn"
        }
    }

    String code

    Region(String code) {
        setCode(code)
    }

    String getApiBase() {
        "https://${code.toLowerCase()}.api.battle.net"
    }

}
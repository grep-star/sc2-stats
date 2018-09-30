package com.grepstar.jackson

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.module.SimpleModule
import com.grepstar.starcraft.LadderRank

class StarcraftObjectMapper extends ObjectMapper {

    public static final StarcraftObjectMapper INSTANCE = new StarcraftObjectMapper()

    StarcraftObjectMapper() {
        final SimpleModule module = new SimpleModule('StarCraft (De)serializers')
        module.addDeserializer(LadderRank, new LadderRankDeserializer())
        registerModule(module)
    }

}

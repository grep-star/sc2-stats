package com.grepstar.jackson

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import com.grepstar.starcraft.LadderRank
import com.grepstar.starcraft.Race

class LadderRankDeserializer extends StdDeserializer<LadderRank> {

    LadderRankDeserializer() {
        super(LadderRank)
    }

    @Override
    LadderRank deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JsonProcessingException {
        final JsonNode ladderRankNode = jsonParser.readValueAsTree() as JsonNode
        final LadderRank ladderRank = new LadderRank()
        ladderRank.setId(ladderRankNode.get('id').asText())
        ladderRank.setMmr(ladderRankNode.get('rating').asInt())
        ladderRank.setGamesWon(ladderRankNode.get('wins').asInt())
        ladderRank.setGamesLost(ladderRankNode.get('losses').asInt())
        final JsonNode memberNode = ladderRankNode.get('member')[0]
        ladderRank.setRace(Race.values().find { race ->
            race.toString() == memberNode.get('played_race_count')[0].get('race')[0].asText()
        })
        final JsonNode clanLinkNode = memberNode.get('clan_link')
        if (clanLinkNode != null) {
            ladderRank.setClan(clanLinkNode.get('clan_tag').asText())
        }
        final JsonNode legacyNode = memberNode.get('legacy_link')
        if (legacyNode.has('name')) {
            ladderRank.setName(legacyNode.get('name').asText())
        }
        ladderRank.setPlayerId(legacyNode.get('id').asInt())

        ladderRank
    }

}

package com.grepstar.starcraft

import com.fasterxml.jackson.databind.ObjectMapper
import com.grepstar.jackson.StarcraftObjectMapper
import io.restassured.RestAssured
import io.restassured.config.ObjectMapperConfig
import io.restassured.config.RestAssuredConfig
import io.restassured.mapper.factory.Jackson2ObjectMapperFactory
import io.restassured.path.json.JsonPath
import io.restassured.response.Response

import java.lang.reflect.Type
import java.time.LocalDateTime

class Processor {

    Client client

    Processor(String apiKey, String oauthToken) {
        client = new Client(apiKey, oauthToken)
    }

    void run() {
        RestAssured.config = RestAssuredConfig.config().objectMapperConfig(
                ObjectMapperConfig.objectMapperConfig().jackson2ObjectMapperFactory(
                        new Jackson2ObjectMapperFactory() {
                            @Override
                            ObjectMapper create(Type type, String s) {
                                StarcraftObjectMapper.INSTANCE
                            }
                        }
                )
        )

        final List<LadderRank> ranks = Region.values().collectMany { region ->
            final String apiBase = "https://${region.name().toLowerCase()}.api.battle.net"
            final String gameApiBase = "${apiBase}/data/sc2"
            final String seasonId = client.requestUsingOauthToken("${gameApiBase}/season/current").jsonPath().getString('id')

            final List<LadderRank> rankList = League.values().collectMany { league ->
                client.requestUsingOauthToken("${gameApiBase}/league/${seasonId}/201/0/${league.leagueId}").jsonPath().get('tier').collect { Map tier ->
                    final Rank rank = new Rank(league, tier.get('id') + 1 as int)

                    tier.get('division').collect { Map division ->
                        final int divisionId = division.get('ladder_id') as int
                        final LocalDateTime time = LocalDateTime.now()
                        final Response ladderRankResponse = client.requestUsingOauthToken("${gameApiBase}/ladder/${divisionId}")
                        if (ladderRankResponse.statusCode() == 500) {
                            println("Error: could not read information for ${region} ${rank} division ${divisionId}.")
                            null
                        } else {
                            final List<LadderRank> ladderRanks = client.requestUsingOauthToken("${gameApiBase}/ladder/${divisionId}").jsonPath().getObject('team', LadderRank[])
                            ladderRanks.each { ladderRank ->
                                ladderRank.setReadTime(time)
                                ladderRank.setRank(rank)
                            }
                            final List<LadderRank> playersMissingNames = ladderRanks.findAll { ladderRank ->
                                ladderRank.name == null
                            }
                            if (!playersMissingNames.isEmpty()) {
                                final JsonPath legacyLadder = client.requestUsingApiKey("${apiBase}/sc2/ladder/${divisionId}").jsonPath()
                                playersMissingNames.each { player ->
                                    player.setName(legacyLadder.getString("ladderMembers.find { it.id == ${player.playerId} }.displayName"))
                                }
                            }
                            println("Parsed information for ${region} ${rank} division ${divisionId}.")
                            ladderRanks
                        }
                    }.flatten()
                }.flatten()
            } as List<LadderRank>
            rankList.removeAll([null])
            rankList.each { rank ->
                rank.setRegion(region)
            }
        }

        println ranks
    }

}

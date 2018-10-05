package com.grepstar.starcraft

import com.fasterxml.jackson.databind.ObjectMapper
import com.grepstar.jackson.StarcraftObjectMapper
import io.restassured.RestAssured
import io.restassured.config.ObjectMapperConfig
import io.restassured.config.RestAssuredConfig
import io.restassured.mapper.factory.Jackson2ObjectMapperFactory
import io.restassured.path.json.JsonPath
import io.restassured.response.Response
import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVPrinter

import java.lang.reflect.Type
import java.nio.file.Files
import java.nio.file.Paths
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

        final List<String> unreadableDivisions = []
        final List<LadderRank> ranks = Region.values().collectMany { region ->
            final String apiBase = region.getApiBase()
            final String gameApiBase = "${apiBase}/data/sc2"
            final String seasonId = client.requestUsingOauthToken("${gameApiBase}/season/current").jsonPath().getString('id')

            final List<LadderRank> rankList = League.values().collectMany { league ->
                client.requestUsingOauthToken("${gameApiBase}/league/${seasonId}/201/0/${league.leagueId}").jsonPath().get('tier').collect { Map tier ->
                    final Rank rank = new Rank(league, tier.get('id') + 1 as int)

                    tier.get('division').collect { Map division ->
                        final int divisionId = division.get('ladder_id') as int
                        final String divisionString = "${region} ${rank} division ${divisionId}"
                        final LocalDateTime time = LocalDateTime.now()
                        final Response ladderRankResponse = client.requestUsingOauthToken("${gameApiBase}/ladder/${divisionId}")
                        if (ladderRankResponse.statusCode() == 500) {
                            println("Error: could not read information for ${divisionString}.")
                            unreadableDivisions << divisionString
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
                                println("Division ${divisionString} has players with missing names. Issuing a call to legacy API as a backup...")
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

        final List<Player> players = []
        ranks.each { ladderRank ->
            final Player player = players.find { player ->
                player.region == ladderRank.region && player.id == ladderRank.playerId
            }
            if (player == null) {
                players << new Player(ladderRank)
            } else {
                player.ranks << ladderRank
            }
        }

        players.each { player ->
            Race.values().each { race ->
                final List<LadderRank> raceRanks = player.ranks.findAll { rank ->
                    rank.race == race
                }
                if (raceRanks.size() > 1) {
                    println("Player ${player.id} on server ${raceRanks[0].region} has multiple ladder ranks as ${race}. Only the latest-read entry will be kept.")
                    player.ranks.removeIf { rank ->
                        raceRanks.any { other ->
                            other.readTime.isAfter(rank.readTime)
                        }
                    }
                }
            }
        }

        println("Could not read ladder divisions: ${unreadableDivisions}.")

        Collections.sort(players)

        final BufferedWriter writer = Files.newBufferedWriter(Paths.get('ladder_data.csv'))
        final List<Race> allRaces = Race.values()
        //noinspection GroovyAssignabilityCheck
        final CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.DEFAULT.withHeader(['Player ID', 'Region', 'Clan', 'Name', 'Max MMR'] + allRaces.collectMany { race ->
            ["${race} MMR", "${race} League", "${race} Wins", "${race} Losses"]
        } as String[]))
        players.each { player ->
            final List<String> row = []
            final LadderRank arbitraryRank = player.ranks[0]
            row.addAll([String.valueOf(player.id), player.region.name(), arbitraryRank.clan, arbitraryRank.name, String.valueOf(player.maxMmr)])
            allRaces.each { race ->
                final LadderRank rank = player.ranks.find { rank ->
                    rank.race == race
                }
                if (rank == null) {
                    row.addAll(['', '', '', ''])
                } else {
                    row.addAll([String.valueOf(rank.mmr), rank.rank.toString(), String.valueOf(rank.gamesWon), String.valueOf(rank.gamesLost)])
                }
            }
            csvPrinter.printRecord(row)
        }
        csvPrinter.flush()
    }

}

import com.grepstar.starcraft.Processor
import groovy.cli.picocli.CliBuilder
import groovy.cli.picocli.OptionAccessor

@Grapes([
        @Grab('io.rest-assured:rest-assured:3.1.1'),
        @GrabExclude('org.codehaus.groovy:groovy-xml'),
        @GrabExclude('org.codehaus.groovy:groovy-json'),
        @Grab('com.fasterxml.jackson.core:jackson-core:2.8.2'),
        @Grab('com.fasterxml.jackson.core:jackson-databind:2.8.2'),
        @Grab('org.apache.commons:commons-csv:1.6')
])

final CliBuilder cli = new CliBuilder()
cli.a(type: String, required: true, 'API key to use for authentication')
cli.t(type: String, required: true, 'OAuth token to use for authentication')
final OptionAccessor options = cli.parse(args)
new Processor(options.a as String, options.t as String).run()

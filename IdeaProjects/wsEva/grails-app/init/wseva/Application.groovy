package wseva

import grails.boot.GrailsApp
import grails.boot.config.GrailsAutoConfiguration
import grails.plugin.externalconfig.ExternalConfig

class Application extends GrailsAutoConfiguration implements ExternalConfig {
    static void main(String[] args) {
        GrailsApp.run(Application, args)
    }
}
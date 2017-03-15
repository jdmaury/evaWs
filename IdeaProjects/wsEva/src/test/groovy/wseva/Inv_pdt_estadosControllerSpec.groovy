package wseva

import grails.test.mixin.*
import spock.lang.*

@TestFor(Inv_pdt_estadosController)
@Mock(Inv_pdt_estados)
class Inv_pdt_estadosControllerSpec extends Specification {

    def populateValidParams(params) {
        assert params != null

        // TODO: Populate valid properties like...
        //params["name"] = 'someValidName'
        assert false, "TODO: Provide a populateValidParams() implementation for this generated test suite"
    }

    void "Test the index action returns the correct model"() {

        when:"The index action is executed"
            controller.index()

        then:"The model is correct"
            !model.inv_pdt_estadosList
            model.inv_pdt_estadosCount == 0
    }

    void "Test the create action returns the correct model"() {
        when:"The create action is executed"
            controller.create()

        then:"The model is correctly created"
            model.inv_pdt_estados!= null
    }

    void "Test the save action correctly persists an instance"() {

        when:"The save action is executed with an invalid instance"
            request.contentType = FORM_CONTENT_TYPE
            request.method = 'POST'
            def inv_pdt_estados = new Inv_pdt_estados()
            inv_pdt_estados.validate()
            controller.save(inv_pdt_estados)

        then:"The create view is rendered again with the correct model"
            model.inv_pdt_estados!= null
            view == 'create'

        when:"The save action is executed with a valid instance"
            response.reset()
            populateValidParams(params)
            inv_pdt_estados = new Inv_pdt_estados(params)

            controller.save(inv_pdt_estados)

        then:"A redirect is issued to the show action"
            response.redirectedUrl == '/inv_pdt_estados/show/1'
            controller.flash.message != null
            Inv_pdt_estados.count() == 1
    }

    void "Test that the show action returns the correct model"() {
        when:"The show action is executed with a null domain"
            controller.show(null)

        then:"A 404 error is returned"
            response.status == 404

        when:"A domain instance is passed to the show action"
            populateValidParams(params)
            def inv_pdt_estados = new Inv_pdt_estados(params)
            controller.show(inv_pdt_estados)

        then:"A model is populated containing the domain instance"
            model.inv_pdt_estados == inv_pdt_estados
    }

    void "Test that the edit action returns the correct model"() {
        when:"The edit action is executed with a null domain"
            controller.edit(null)

        then:"A 404 error is returned"
            response.status == 404

        when:"A domain instance is passed to the edit action"
            populateValidParams(params)
            def inv_pdt_estados = new Inv_pdt_estados(params)
            controller.edit(inv_pdt_estados)

        then:"A model is populated containing the domain instance"
            model.inv_pdt_estados == inv_pdt_estados
    }

    void "Test the update action performs an update on a valid domain instance"() {
        when:"Update is called for a domain instance that doesn't exist"
            request.contentType = FORM_CONTENT_TYPE
            request.method = 'PUT'
            controller.update(null)

        then:"A 404 error is returned"
            response.redirectedUrl == '/inv_pdt_estados/index'
            flash.message != null

        when:"An invalid domain instance is passed to the update action"
            response.reset()
            def inv_pdt_estados = new Inv_pdt_estados()
            inv_pdt_estados.validate()
            controller.update(inv_pdt_estados)

        then:"The edit view is rendered again with the invalid instance"
            view == 'edit'
            model.inv_pdt_estados == inv_pdt_estados

        when:"A valid domain instance is passed to the update action"
            response.reset()
            populateValidParams(params)
            inv_pdt_estados = new Inv_pdt_estados(params).save(flush: true)
            controller.update(inv_pdt_estados)

        then:"A redirect is issued to the show action"
            inv_pdt_estados != null
            response.redirectedUrl == "/inv_pdt_estados/show/$inv_pdt_estados.id"
            flash.message != null
    }

    void "Test that the delete action deletes an instance if it exists"() {
        when:"The delete action is called for a null instance"
            request.contentType = FORM_CONTENT_TYPE
            request.method = 'DELETE'
            controller.delete(null)

        then:"A 404 is returned"
            response.redirectedUrl == '/inv_pdt_estados/index'
            flash.message != null

        when:"A domain instance is created"
            response.reset()
            populateValidParams(params)
            def inv_pdt_estados = new Inv_pdt_estados(params).save(flush: true)

        then:"It exists"
            Inv_pdt_estados.count() == 1

        when:"The domain instance is passed to the delete action"
            controller.delete(inv_pdt_estados)

        then:"The instance is deleted"
            Inv_pdt_estados.count() == 0
            response.redirectedUrl == '/inv_pdt_estados/index'
            flash.message != null
    }
}

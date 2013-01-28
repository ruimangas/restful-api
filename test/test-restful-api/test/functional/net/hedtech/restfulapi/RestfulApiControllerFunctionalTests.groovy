/* ****************************************************************************
Copyright 2013 Ellucian Company L.P. and its affiliates.
******************************************************************************/ 

package net.hedtech.restfulapi

import com.grailsrocks.functionaltest.*

import grails.converters.JSON


class RestfulApiControllerFunctionalTests extends BrowserTestCase {

    static final String localBase = "http://127.0.0.1:8080/test-restful-api"


    void testList_json() {

        get( "$localBase/api/things" ) {
            headers['Content-Type']  = 'application/json'
            headers['Accept']        = 'application/json'
//            headers['Authorization'] = TestUtils.authHeader('user','password')
        }
        assertStatus 200
        assertEquals 'application/json', page?.webResponse?.contentType

        def stringContent = page?.webResponse?.contentAsString
        def json = JSON.parse stringContent
        assert "AA" == json.data[0].code
        assert "An AA thing" == json.data[0].description

        // assert localization of the message
        assert "List of thing resources" == json.message

        // Assert that an '_href' property is present but not a 'numParts'
        // property, which proves the plugin-registered marshaller is 
        // being used versus the built-in grails marshaller or the 
        // resource-specific marshaller registered by this test app.
        //
        assert json.data[0]._href?.contains('things')  
        assertNull json.data[0].numParts
    }


    void testList_jsonv0() {

        get( "$localBase/api/things" ) {
            headers['Content-Type']  = 'application/json'
            headers['Accept']        = 'application/vnd.hedtech.v0+json'
//            headers['Authorization'] = TestUtils.authHeader('user','password')
        }
        assertStatus 200
        assertEquals 'application/json', page?.webResponse?.contentType

        def stringContent = page?.webResponse?.contentAsString
        def json = JSON.parse stringContent
        assert "AA" == json.data[0].code
        assert "An AA thing" == json.data[0].description

        // Assert the 'numParts' property is present proving the 
        // resource-specific marshaller registered for the 'jsonv0'
        // configuration was used.
        //
        assert 2 == json.data[0].numParts
    }


    void testValidationError() {

        // TODO: Replace with real validation testing in create/update
        get( "$localBase/api/things/?forceValidationError=y" ) {
            headers['Content-Type']  = 'application/json'
            headers['Accept']        = 'application/json'
//            headers['Authorization'] = TestUtils.authHeader('user','password')
        }
        assertStatus 400
        assertEquals 'application/json', page?.webResponse?.contentType

        def stringContent = page?.webResponse?.contentAsString
        def json = JSON.parse stringContent
        assertFalse json.success
        assert "validation" == json.errors.type
        assert json.errors.resource.class == 'net.hedtech.restfulapi.Thing'
        assertNotNull json.errors.errorMessage  
    }


    void testShow_json() {

        get( "$localBase/api/things/1" ) {
            headers['Content-Type']  = 'application/json'
            headers['Accept']        = 'application/json'
//            headers['Authorization'] = TestUtils.authHeader('user','password')
        }
        assertStatus 200
        assertEquals 'application/json', page?.webResponse?.contentType

        def stringContent = page?.webResponse?.contentAsString

        def json = JSON.parse stringContent
        assert "AA" == json.data.code
        assert "An AA thing" == json.data.description
        assert json.data._href?.contains('things')  
        assertNull json.data.numParts

        // test localization of the message
        assert "Details for the thing resource" == json.message
    }


    void testShow_jsonv0() {

        get( "$localBase/api/things/1" ) {
            headers['Content-Type']  = 'application/json'
            headers['Accept']        = 'application/vnd.hedtech.v0+json'
//            headers['Authorization'] = TestUtils.authHeader('user','password')
        }
        assertStatus 200
        assertEquals 'application/json', page?.webResponse?.contentType

        def stringContent = page?.webResponse?.contentAsString
        def json = JSON.parse stringContent
        assert "AA" == json.data.code
        assert "An AA thing" == json.data.description
        assert 2 == json.data.numParts
    }

    void testSave_json() {
        post( "$localBase/api/things") {
            headers['Content-Type'] = 'application/json'
            headers['Accept']       = 'application/json'
            body {
                """
                { 
                    code:'AC',
                    description:'An AC thingy',
                }
                """
            }
        }
        assertStatus 201
        assertEquals 'application/json', page?.webResponse?.contentType

        def stringContent = page?.webResponse?.contentAsString
        def json = JSON.parse stringContent
        assertNotNull json.data.id 
        assert "AC" == json.data.code
        assert "An AC thingy" == json.data.description
        assert 0 == json.data.parts.size()
    }

    void testSave_json_response_jsonv0() {
        post( "$localBase/api/things") {
            headers['Content-Type'] = 'application/json'
            headers['Accept']       = 'application/vnd.hedtech.v0+json'
            body {
                """
                { 
                    code:'AD',
                    description:'An AD thingy',
                }
                """
            }
        }
        assertStatus 201
        assertEquals 'application/json', page?.webResponse?.contentType

        def stringContent = page?.webResponse?.contentAsString
        def json = JSON.parse stringContent
        assertNotNull json.data.id 
        assert "AD" == json.data.code
        assert "An AD thingy" == json.data.description
        assert 0 == json.data.numParts
    }

    void testSaveExisting() {
        post( "$localBase/api/things") {
            headers['Content-Type'] = 'application/json'
            headers['Accept']       = 'application/json'
            body {
                """
                { 
                    code:'AA',
                    description:'An AA thingy',
                }
                """
            }
        }
        assertStatus 400
        assertHeader "X-Status-Reason", 'Validation failed'

        def stringContent = page?.webResponse?.contentAsString
        def json = JSON.parse stringContent
        assertFalse json.success
        assert "validation" == json.errors.type
        assert json.errors.resource.class == 'net.hedtech.restfulapi.Thing'
        assertNotNull json.errors.errorMessage  
    }

    void testGenericErrorOnSave() {
        post( "$localBase/api/things?forceGenericError=y") {
            headers['Content-Type'] = 'application/json'
            headers['Accept']       = 'application/json'
            body {
                """
                { 
                    code:'AA',
                    description:'An AA thingy',
                }
                """
            }
        }
        assertStatus 500
        def stringContent = page?.webResponse?.contentAsString
        def json = JSON.parse stringContent
        assertFalse json.success
        assert "general" == json.errors.type
        assert json.errors.resource.class == 'net.hedtech.restfulapi.Thing'
        assertNotNull json.errors.errorMessage 

    }
}

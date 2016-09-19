package gemsMetricGenerator

import static org.mockserver.integration.ClientAndServer.startClientAndServer
import static org.mockserver.model.HttpRequest.request
import static org.mockserver.model.HttpResponse.response

import grails.test.mixin.TestFor
import spock.lang.Specification
import org.mockserver.integration.ClientAndServer
import org.mockserver.model.Header
import org.mockserver.model.Parameter
import org.mockserver.verify.VerificationTimes

/**
 * See the API for {@link grails.test.mixin.services.ServiceUnitTestMixin} for usage instructions
 */
@TestFor(WorkedHoursMetricGeneratorService)
class WorkedHoursMetricGeneratorServiceSpec extends Specification {
    protected static ClientAndServer mockServer

    def setupSpec() {
        mockServer = startClientAndServer(8081)
    }

    def cleanupSpec() {
        mockServer.stop()
    }

    def setup() {
    }

    def cleanup() {
        mockServer.reset()
    }

    private String getTracesResponse() {
        """[
    {
    "id": "57d5f5e48acec62fb22f8a73",
    "project": {
      "id": "57cc59368acec62bf2f7d7ed"
    },
    "taskTraces": [
      {
        "name": null,
        "status": "ESTADO!",
        "taskTraceId": "57d5fe9c8acec641a6c8d926",
        "traceDetails": [
          {
            "date": "2016-09-11T06:00:00Z",
            "hours": 5,
            "member": {
              "id": "57c3c4858acec662dab6dcf4"
            }
          }
        ]
      },
      {
        "name": null,
        "status": "ESTADO!",
        "taskTraceId": "57d5f5e28acec63dfc6b1317",
        "traceDetails": [
          {
            "date": "2016-08-30T06:00:00Z",
            "hours": 3,
            "member": {
              "id": "57c3c4858acec662dab6dcf4"
            }
          },
          {
            "date": "2016-08-29T06:00:00Z",
            "hours": 2,
            "member": {
              "id": "57c3c4858acec662dab6dcf4"
            }
          }
        ]
      }
    ]
    },
    {
    "id": "57d5f5e48acec62fb22f8a74",
    "project": {
      "id": "57cc59368acec62bf2f7d7ee"
    },
    "taskTraces": [
      {
        "name": "Revisión de código",
        "status": "ESTADO!",
        "taskTraceId": "57d5fe9c8acec641a6c8d927",
        "traceDetails": [
          {
            "date": "2016-09-11T06:00:00Z",
            "hours": 5,
            "member": {
              "id": "57c3c4858acec662dab6dcf4"
            }
          }
        ]
      },
      {
        "name": "Inyector plan Redmine",
        "status": "ESTADO!",
        "taskTraceId": "57d5f5e28acec63dfc6b1318",
        "traceDetails": [
          {
            "date": "2016-08-30T06:00:00Z",
            "hours": 3,
            "member": {
              "id": "57c3c4858acec662dab6dcf4"
            }
          },
          {
            "date": "2016-08-29T06:00:00Z",
            "hours": 2,
            "member": {
              "id": "57c3c4838acec662dab6dcf2"
            }
          }
        ]
      }
    ]
    }
    ]"""
    }

    private String getPlanResponse(){
        """[
    {
    "id": "57cf835f8acec65eba3b579f",
    "project": {
      "id": "57cc59368acec62bf2f7d7ed"
    },
    "tasks": [
      {
        "contributors": [],
        "dueDate": "2016-09-13T06:00:00Z",
        "name": "Revisión de código",
        "responsible": {
          "id": "57c3c4838acec662dab6dcf2"
        },
        "startDate": "2016-08-28T06:00:00Z",
        "status": "In Progress",
        "taskId": "57d0c86c8acec6725ee5accf"
      },
      {
        "contributors": [],
        "dueDate": "2016-08-30T06:00:00Z",
        "name": "Dashboard",
        "responsible": null,
        "startDate": "2016-08-25T06:00:00Z",
        "status": "New",
        "taskId": "57d0c86c8acec6725ee5acd0"
      },
      {
        "contributors": [],
        "dueDate": "2016-08-24T06:00:00Z",
        "name": "Inyector plan Redmine",
        "responsible": {
          "id": "57c3c4858acec662dab6dcf4"
        },
        "startDate": "2016-08-22T06:00:00Z",
        "status": "New",
        "taskId": "57d0c86c8acec6725ee5acd1"
      },
      {
        "contributors": [],
        "dueDate": "2016-08-19T06:00:00Z",
        "name": "API Blackboard",
        "responsible": {
          "id": "57c3c4858acec662dab6dcf4"
        },
        "startDate": "2016-08-08T06:00:00Z",
        "status": "New",
        "taskId": "57d0c86c8acec6725ee5acd2"
      }
    ]
    }
    ]"""
    }

    private String getProjectResponse(String id, String name) {
        """{
    "id": "${id}",
    "name": "${name}"
    }"""
    }

    private String getMemberResponse(String id, String name, String email) {
        """{
    "id": "${id}",
    "name": "${name}",
    "email": "${email}"
    }"""
    }

    private String postProjectMetricResponse() {
        '''{
    "id": "57df7b7a8acec6573c085e99",
    "name":"Horas trabajadas en otros proyectos",
    "year":2016,
    "month":9,
    "project": {
        "id":"57cc59368acec62bf2f7d7ed",
        "name":"Mi proyecto"
    },
    "projectsSummary":[
        {
            "project":{
                "id":"57cc59368acec62bf2f7d7ee",
                "name":"Proyecto 2"
            },
            "metricData":{
                "hours":2
            }
        }
    ],
    "membersSummary":[
        {
            "member": {
                "id":"57c3c4838acec662dab6dcf2",
                "name":"Miembro 1",
                "email":"miembro1@gems.cl"
            },
            "metricData":{
                "hours":2
            }
        }
    ],
    "details":[
        {
            "project":{
                "id":"57cc59368acec62bf2f7d7ee"
            },
            "member":{
                "id":"57c3c4838acec662dab6dcf2"
            },
            "date":"2016-08-29T06:00:00Z",
            "metricData":{
                "hours":2
            }
        }
    ]
    }
    '''
    }

    private String

    void "test generate project metric"() {
        setup:
        def projectId = '57cc59368acec62bf2f7d7ed'

        mockServer.when(
                request('/plans')
                        .withMethod('GET')
                        //.withQueryStringParameters(new Parameter('projectId', projectId))
        ).respond(response(getPlanResponse())
                .withStatusCode(200)
                .withHeaders(new Header('Content-Type', 'application/json; charset=utf-8'))
        )

        mockServer.when(
                request("/traces")
                        .withMethod('GET')
        ).respond(response(getTracesResponse())
                .withStatusCode(200)
                .withHeaders(new Header('Content-Type', 'application/json; charset=utf-8'))
        )

        mockServer.when(
                request("/projects/${projectId}")
                        .withMethod('GET')
        ).respond(response(getProjectResponse(projectId, 'Proyecto 1'))
                .withStatusCode(200)
                .withHeaders(new Header('Content-Type', 'application/json; charset=utf-8'))
        )
        def otherProjectId = '57cc59368acec62bf2f7d7ee'
        mockServer.when(
                request("/projects/${otherProjectId}")
                        .withMethod('GET')
        ).respond(response(getProjectResponse(otherProjectId, 'Proyecto 2'))
                .withStatusCode(200)
                .withHeaders(new Header('Content-Type', 'application/json; charset=utf-8'))
        )

        def memberId = '57c3c4838acec662dab6dcf2'
        mockServer.when(
                request("/members/${memberId}")
                        .withMethod('GET')
        ).respond(response(getMemberResponse(memberId, 'Miembro 1', 'miembro1@gems.cl'))
                .withStatusCode(200)
                .withHeaders(new Header('Content-Type', 'application/json; charset=utf-8'))
        )
        memberId = '57c3c4858acec662dab6dcf4'
        mockServer.when(
                request("/members/${memberId}")
                        .withMethod('GET')
        ).respond(response(getMemberResponse(memberId, 'Miembro 2', 'miembro2@gems.cl'))
                .withStatusCode(200)
                .withHeaders(new Header('Content-Type', 'application/json; charset=utf-8'))
        )

        mockServer.when(
            request('/projectMetrics')
            .withMethod('POST')
        ).respond(response(postProjectMetricResponse())
                .withStatusCode(200)
                .withHeaders(new Header('Content-Type', 'application/json; charset=utf-8'))
        )

        when:
        def metricResult = service.generateProjectMetric(projectId)


        then:
        metricResult.id != null
    }
}

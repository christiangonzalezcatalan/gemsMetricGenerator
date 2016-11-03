package gemsMetricGenerator

import grails.transaction.Transactional
import org.grails.web.json.JSONObject
import org.springframework.http.HttpStatus
import grails.util.Holders
import grails.plugins.rest.client.RestBuilder
import static java.util.Calendar.YEAR
import static java.util.Calendar.MONTH

@Transactional
class WorkedHoursMetricGeneratorService {
    RestBuilder restClient = new RestBuilder()
    String gemsbbUrl = Holders.grailsApplication.config.getProperty('metricGenerator.gemsbbUrl')

    private def getPlansFromBlackboard(String projectId) {
        def resp = restClient.get("${gemsbbUrl}/plans?projectId=${projectId}")

        if(resp.getStatusCode() != HttpStatus.OK) {
            throw new Exception("Error al obtener el registro del plan del Blackboard. HttpStatusCode: ${resp.getStatusCode()}")
        }

        resp.json
    }

    private def getTracesFromBlackboard() {
        def resp = restClient.get("${gemsbbUrl}/traces")

        if(resp.getStatusCode() != HttpStatus.OK) {
            throw new Exception("Error al obtener el registro de traza del Blackboard. HttpStatusCode: ${resp.getStatusCode()}")
        }

        resp.json
    }

    private def getMetricFromBloackboard(metricName, projectId, month, year) {
        def params = "?name=${metricName}&projectId=${projectId}&month=${month}&year=${year}"
        def resp = restClient.get("${gemsbbUrl}/projectMetrics${params}")

        if(resp.getStatusCode() != HttpStatus.OK) {
            throw new Exception("Error al obtener el registro de la métrica de proyecto del Blackboard. HttpStatusCode: ${resp.getStatusCode()}")
        }

        resp.json[0]
    }

    private def getProjectFromBlackboard(projectId) {
        def resp = restClient.get("${gemsbbUrl}/projects/${projectId}")

        if(resp.getStatusCode() != HttpStatus.OK) {
            throw new Exception("Error al obtener el proyecto del Blackboard. HttpStatusCode: ${resp.getStatusCode()}")
        }

        resp.json
    }

    private def getProjectsByOrganizationFromBlackboard(organizationId) {
        def resp = restClient.get("${gemsbbUrl}/projects?toolName=Redmine&processName=NotAssignedWorkMetric&organizationId=${organizationId}")

        if(resp.getStatusCode() != HttpStatus.OK) {
            throw new Exception("Error al obtener el proyecto del Blackboard. HttpStatusCode: ${resp.getStatusCode()}")
        }

        resp.json
    }

    private def getProject(projectsMap, projectId) {
        if(!projectsMap.containsKey(projectId)) {
            def project = getProjectFromBlackboard(projectId)
            projectsMap[projectId] = [
                id: project.id,
                name: project.name
            ]
        }
        projectsMap[projectId]
    }

    private def getMember(membersMap, memberId) {
        if(!membersMap.containsKey(memberId)) {
            def resp = restClient.get("${gemsbbUrl}/members/${memberId}")

            if(resp.getStatusCode() != HttpStatus.OK) {
                throw new Exception("Error al obtener el usuario ${memberId} del Blackboard. HttpStatusCode: ${resp.getStatusCode()}")
            }

            def member = resp.json
            membersMap[memberId] = [
                id: member.id,
                name: member.name,
                email: member.email
            ]
        }
        membersMap[memberId]
    }

    def findPlanedTaskWithConflict(traceDetail, plan) {
        plan.tasks.findAll() {
            Date traceDate = Date.parse('yyyy-MM-dd', traceDetail.date)
            Date startDate = Date.parse('yyyy-MM-dd', it.startDate)
            Date dueDate = Date.parse('yyyy-MM-dd', it.dueDate)

            it.responsible?.id == traceDetail.member.id &&
            traceDate >= startDate &&
            traceDate <= dueDate
        }
    }

    private def saveBlackboardProjectMetric(metric) {
        def response
        if(metric.id == null) {
            response = restClient.post("${gemsbbUrl}/projectMetrics") {
                contentType "application/json"
                json {
                    project = metric.project
                    name = metric.name
                    year = metric.year
                    month = metric.month
                    projectsSummary = metric.projectsSummary
                    membersSummary = metric.membersSummary
                    details = metric.details
                }
            }
        }
        else {
            response = restClient.put("${gemsbbUrl}/projectMetrics/${metric.id}") {
                contentType "application/json"
                json {
                    id = metric.id
                    project = metric.project
                    name = metric.name
                    year = metric.year
                    month = metric.month
                    projectsSummary = metric.projectsSummary
                    membersSummary = metric.membersSummary
                    details = metric.details
                }
            }
        }
        if (response.getStatusCode() != HttpStatus.OK &&
            response.getStatusCode() != HttpStatus.CREATED) {
            throw new Exception("Error al guardar el registro de la métrica. HttpStatusCode: ${response.getStatusCode()}")
        }

        response.json
    }

    def generateMetrics(project, Integer month, Integer year) {
        println "Cálculo de métrica para proyecto ${project.id} (${month+1}/${year})."

        def metricName = 'Horas trabajadas en otros proyectos'
        def plans = getPlansFromBlackboard(project.id)
        def traces = getTracesFromBlackboard()
        def metric = getMetricFromBloackboard(metricName, project.id, month, year) ?: new LinkedHashMap()
        def projectSummary = new LinkedHashMap()
        def memberSummary = new LinkedHashMap()
        def details = new LinkedHashMap()
        def members = new LinkedHashMap()
        def projects = new LinkedHashMap()

        def currentPlan = plans.find { it.project.id == project.id  }
        if(currentPlan == null) {
            throw new Exception('El plan del proyecto no se encuentra.')
        }

        traces.each {
            if(it.project.id != project.id) {
                def projectTrace = it
                projectSummary[projectTrace.project.id] = [
                    metricData: [hours: 0],
                    project: getProject(projects, projectTrace.project.id)
                ]
                projectTrace.taskTraces.each {
                    def taskTrace = it

                    taskTrace.traceDetails.each {
                        Date traceDate = Date.parse('yyyy-MM-dd', it.date)
                        if(traceDate[YEAR] != year || traceDate[MONTH] != month) {
                            return
                        }

                        if(findPlanedTaskWithConflict(it, currentPlan).size() == 0) {
                            return
                        }

                        if(!memberSummary.containsKey(it.member.id)) {
                            memberSummary[it.member.id] = [
                                metricData: [hours: 0],
                                member: getMember(members, it.member.id)
                            ]
                        }
                        def detailKey = [
                            project: projectTrace.project.id,
                            member: it.member.id,
                            date: Date.parse('yyyy-MM-dd', it.date)
                        ]

                        if(!details.containsKey(
                        detailKey)) {
                            details[detailKey] = [
                                metricData: [hours: 0]
                            ]
                        }

                        projectSummary[projectTrace.project.id].metricData.hours += it.hours
                        memberSummary[it.member.id].metricData.hours += it.hours
                        details[detailKey].metricData.hours += it.hours
                    }
                }
            }
        }

        metric << [
            name: metricName,
            year: year,
            month: month,
            project: [
                id: project.id,
                name: project.name
            ],
            projectsSummary: projectSummary.collect() {
                key, value ->
                value
            },
            membersSummary: memberSummary.collect() {
                key, value ->
                value
            },
            details: details.collect() {
                key, value ->
                [
                    project: [id:key.project],
                    member: [id:key.member],
                    date: key.date,
                    metricData: value.metricData
                ]
            }
        ]

        saveBlackboardProjectMetric(metric)
        println "Métrica de proyecto ${project.id} (${month+1}/${year}) cargada."
    }

    def generateProjectMetric(String projectId, Integer month, Integer year) {
        def project = getProjectFromBlackboard(projectId)
        generateMetrics(project, month, year)
    }

    def generateOrganizationMetric(String projectId, Integer month, Integer year) {
        def project = getProjectFromBlackboard(projectId)
        def projects = getProjectsByOrganizationFromBlackboard(project.organization.id)

        projects.each() {
            generateMetrics(it, month, year)
        }
    }
}

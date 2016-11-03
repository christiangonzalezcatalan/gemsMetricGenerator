package gemsMetricGenerator

import com.budjb.rabbitmq.consumer.MessageContext
import static java.util.Calendar.YEAR
import static java.util.Calendar.MONTH

class WorkedHoursConsumer {
    def workedHoursMetricGeneratorService

    static rabbitConfig = [
        "queue": "testWorkedHoursMetricGenerator"
    ]

    /**
     * Handle an incoming RabbitMQ message.
     *
     * @param body    The converted body of the incoming message.
     * @param context Properties of the incoming message.
     * @return
     */
    def handleMessage(String projectId, MessageContext messageContext) {
        println "Mensaje recibido para proyecto ${projectId}. ${new Date()} [${messageContext.envelope.routingKey}]"
        Date currentDate = new Date()

        if(messageContext.envelope.routingKey == 'Plan.update')
        {
            workedHoursMetricGeneratorService.generateProjectMetric(projectId, currentDate[MONTH], currentDate[YEAR])
        }
        else if(messageContext.envelope.routingKey == 'Trace.update') {
            workedHoursMetricGeneratorService.generateOrganizationMetric(projectId, currentDate[MONTH], currentDate[YEAR])
        }
    }

    def handleMessage(MessageContext messageContext) {
        // TODO: Handle messages
        println "Mensaje recibido sin body"
    }
}

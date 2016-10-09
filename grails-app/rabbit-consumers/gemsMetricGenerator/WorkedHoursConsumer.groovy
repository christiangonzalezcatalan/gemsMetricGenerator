package gemsMetricGenerator

import com.budjb.rabbitmq.consumer.MessageContext
import static java.util.Calendar.YEAR
import static java.util.Calendar.MONTH

class WorkedHoursConsumer {
    def workedHoursMetricGeneratorService

    static rabbitConfig = [
        "queue": "myQueue"
    ]

    /**
     * Handle an incoming RabbitMQ message.
     *
     * @param body    The converted body of the incoming message.
     * @param context Properties of the incoming message.
     * @return
     */
    def handleMessage(String projectId, MessageContext messageContext) {
        // TODO: Handle messages
        println "Mensaje recibido para proyecto ${projectId}."
        Date currentDate = new Date()
        workedHoursMetricGeneratorService.generateProjectMetric(projectId, currentDate[MONTH], currentDate[YEAR])
        println "Métrica para proyecto ${projectId} cargada."
    }

    def handleMessage(MessageContext messageContext) {
        // TODO: Handle messages
        println "Mensaje recibido sin body"
    }
}

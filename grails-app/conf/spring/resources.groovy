// Place your Spring DSL code here
beans = {
    workedHoursMetricGeneratorService(gemsMetricGenerator.WorkedHoursMetricGeneratorService) {}
    workedHoursConsumer(gemsMetricGenerator.WorkedHoursConsumer) {
        workedHoursMetricGeneratorService = ref("workedHoursMetricGeneratorService")
    }
}

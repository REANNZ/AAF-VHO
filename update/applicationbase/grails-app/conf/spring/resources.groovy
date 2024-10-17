import aaf.base.SMSDeliveryService

// Place your Spring DSL code here
beans = {
    smsDeliveryService(SMSDeliveryService) {
        autowire = 'byName'
    }
}

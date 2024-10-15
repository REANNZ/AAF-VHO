testDataConfig {
  sampleData {
   'aaf.base.admin.EmailTemplate' {
      def i = 1
      name = {-> "name${i++}" }
    }
  }
}

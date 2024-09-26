testDataConfig {
  sampleData {
   'aaf.base.identity.Subject' {
    def i = 1
    principal = {-> "http://idp.test.com/idp!http://sp.test.com!abcdefg${i++}"}
   }
   'aaf.vhr.ManagedSubject' {
      def i = 1
      login = {-> "loginname${i++}" }
      hash = {-> '$2a$04$42Oi6EJ3T9JceCfNGv4e3u.LhYM6XgVOPWVbXwsC6CnZ46oKS7QZm'} // password == "test"
      email = {-> "testuser${i++}@testdomain.com" }
      sharedToken = {-> "sharedtoken${i++}"}
    }
    'aaf.vhr.Group' {
      def i = 1
      name = {-> "groupname${i++}" }
      description = {-> "group $i description" }
    }
  }
}

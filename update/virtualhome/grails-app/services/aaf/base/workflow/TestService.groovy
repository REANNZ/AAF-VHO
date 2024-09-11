package aaf.base.workflow

/* This is only here to assist in unit tests right now once plugins can be installed per environment we'll remove this */
class TestService {
  def testmethod(def env) {
    println env
    return true
  }
  
  def testmethod2(def env) {
    println env
    return true
  }
  
  def testmethod3(def env) {
    println env
    return true
  }
}

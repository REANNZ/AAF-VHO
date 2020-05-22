hibernate {
  cache.use_second_level_cache = true
  cache.use_query_cache = false
  // cache.region.factory_class = 'net.sf.ehcache.hibernate.EhCacheRegionFactory' // Hibernate 3
  cache.region.factory_class = 'org.hibernate.cache.ehcache.EhCacheRegionFactory' // Hibernate 4
}

environments {
  test {
    dataSource {
      pooled = true
      driverClassName = "org.h2.Driver"
      username = "sa"
      password = ""

      dbCreate = "create-drop"
      url = "jdbc:h2:mem:testDb;MVCC=TRUE;LOCK_TIMEOUT=10000"
    }
  }
}

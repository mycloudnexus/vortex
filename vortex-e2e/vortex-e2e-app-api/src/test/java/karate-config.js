function fn() {
  var env = karate.env; // get system property 'karate.env'
  karate.log('karate.env system property was:', env);
  if (!env) {
    env = 'dev';
  }
  var config = {
    env: env,
    apiBaseUrl: 'http://localhost:8000'
  }
  if (env === 'dev') {
    // customize
    // e.g. config.foo = 'bar';
    // resellerAdminToken: the bearer token of a reseller user , format: 'Bearer xxx'
    config.resellerAdminToken = 'Bearer xxx'
    config.resellerOrgId = ''
    config.customerAOrgId = ''
    config.customerSSOOrgId = ''
  } else if (env === 'e2e') {
    // customize
  }
  return config;
}
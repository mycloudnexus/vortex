const pkg = require(`${__dirname}/package.json`);
const spawnTime = Date.now();
module.exports = {
  name: pkg.name,
  version: pkg.version,
  now: Date.now(),
  uptime: Date.now() - spawnTime,
}


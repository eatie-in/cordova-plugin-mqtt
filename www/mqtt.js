const exec = require("cordova/exec");

const PLUGIN_NAME = "MQTTPlugin";

const functionName = {
  echo: "echo",
};

function echo(cb) {
  exec(cb, null, PLUGIN_NAME, functionName.echo, []);
}

module.exports = {
  echo,
};

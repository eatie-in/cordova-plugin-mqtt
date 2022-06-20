const exec = require("cordova/exec");

const PLUGIN_NAME = "MQTTPlugin";

const functionName = {
  echo: "echo",
  subscribe: "subscribe",
  connect: "connect",
  disconnect: "disconnect",
};

function echo(cb) {
  exec(cb, null, PLUGIN_NAME, functionName.echo, []);
}

async function connect(args) {
  return new Promise((resolve, reject) => {
    exec(resolve, reject, PLUGIN_NAME, functionName.connect, [args]);
  });
}
async function disconnect() {
  return new Promise((resolve, reject) => {
    exec(resolve, reject, PLUGIN_NAME, functionName.disconnect, []);
  });
}
async function subscribe(args) {
  return new Promise((resolve, reject) => {
    exec(resolve, reject, PLUGIN_NAME, functionName.subscribe, [args]);
  });
}
module.exports = {
  echo,
  connect,
  subscribe,
  disconnect,
};

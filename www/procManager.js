function procManager() {
  this.exec = function(cmd, callback) {
    return cordova.exec(callback, function(err) {
      callback({exitStatus: 100, output: err});
    }, "procManager", "exec", [cmd]);

  };
}

window.procManager = new procManager();

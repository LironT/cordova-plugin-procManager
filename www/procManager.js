function procManager() {
  this.exec = function(cmd, callback) {
    return cordova.exec(callback, failCB, 'ShellExec', 'exec', [cmd]);
  };

  this.getProcessList = function(successCB, failCB) {
    return cordova.exec(successCB, failCB, 'ShellExec', 'getProcessList', []);
  };

  this.killProcessByName = function(packageName, successCB, failCB) {
    return cordova.exec(successCB, failCB, 'ShellExec', 'killProcessByName', [packageName]);
  };

  this.killAllNonSystemProcess = function(successCB, failCB) {
    return cordova.exec(successCB, failCB, 'ShellExec', 'killAllNonSystemProcess', []);
  };

  function failCB(callback, err){
    callback({exitStatus: 100, output: err});
  }
}

window.procManager = new procManager();

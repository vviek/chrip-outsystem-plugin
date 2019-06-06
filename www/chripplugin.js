cordova.define("chrip-outsystem-plugin.chripPlugin", function(require, exports, module) {
  function ChripPlugin() {}
  
  // The function that passes work along to native shells
  // Set Configration
  ChripPlugin.prototype.setConfigration = function(successCallback, errorCallback) {
    cordova.exec(successCallback, errorCallback, 'ChripPlugin', 'setConfigration');
  }
  
  // Check Permissions
  ChripPlugin.prototype.checkPermission= function(successCallback, errorCallback) {
    cordova.exec(successCallback, errorCallback, 'ChripPlugin', 'checkPermission');
  }
  
  //Send Data
  ChripPlugin.prototype.sendData= function(dataToSend,successCallback, errorCallback) {
    cordova.exec(successCallback, errorCallback, 'ChripPlugin', 'sendData',[dataToSend]);
  }
  
  
  
  // Installation constructor that binds ToastyPlugin to window
  ChripPlugin.install = function() {
    if (!window.plugins) {
      window.plugins = {};
    }
    window.plugins.chripPlugin = new ChripPlugin();
    return window.plugins.chripPlugin;
  };
  cordova.addConstructor(ChripPlugin.install);
  });
  
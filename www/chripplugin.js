cordova.define("chrip-outsystem-plugin.chripPlugin", function(require, exports, module) {
  function ChripPlugin() {}
  
  // The function that passes work along to native shells
  //Send Data
  ChripPlugin.prototype.sendData= function(dataToSend,successCallback, errorCallback) {
    cordova.exec(successCallback, errorCallback, 'ChripPlugin', 'sendData',[dataToSend]);
  }
  
  //Register as Listners
    ChripPlugin.prototype.registerAsReceiver= function(successCallback, errorCallback) {
      cordova.exec(successCallback, errorCallback, 'ChripPlugin', 'registerAsReceiver');
    }

  // Stop Chirp
  ChripPlugin.prototype.stop= function(successCallback, errorCallback) {
    cordova.exec(successCallback, errorCallback, 'ChripPlugin', 'stop');
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
  
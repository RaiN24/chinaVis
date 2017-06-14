(function(){
	var observerManager = {};
	var listeners = [];

	observerManager.post = function(message, data, sender) {
		for (var i = 0; i < listeners.length; i++) {
			if (listeners[i].OMListen) {
				if (sender != listeners[i].name){
					listeners[i].OMListen(message, data);
				}
			}
		}
	}
	observerManager.addListener = function(listener) {
		listeners.push(listener);
	}
	observerManager.getListeners = function() {
		return listeners;
	}
	observerManager.setListeners = function(_listeners) {
		listeners = _listeners;
	}
	observerManager.changeListener = function(listener,i){
		listeners[i] = listener;
	}
	window["observerManager"] = observerManager;
})()
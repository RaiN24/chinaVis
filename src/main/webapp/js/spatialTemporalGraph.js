var spatialTemporalGraph = {
	name: "spatialTemporalGraph",
	_spatialTemporalGraphDivID: undefined,
	_spatialViewDivID: "spatialDiv",
	_dayScaleTimelineDivID: "dayTimelineDiv",
	_totalScaleTimelineDivID: "totalTimelineDiv",
	_dayTimelineSvgG: undefined,
	_totalTimelineSvgG: undefined,
	_map: undefined,
	_heatmap: undefined,
	_canvas: undefined,
	_dataByDate: undefined,
	_totalData: undefined,
	_multiMessageNumByTime: undefined,
	selectedStartTime: undefined,
	selectedEndTime: undefined,
	TIMEINTERVAL: 0.001,
	_ifCreatedLayer: false,
	_tip: d3.tip().attr("class", "d3-tip"),

	initialize: function(divID) {
		var self = this;
		var tmpDiv;
		self._addListener();
		self._spatialTemporalGraphDivID = divID;
		self._requestTotalData();
		tmpDiv = self._createDiv(self._spatialTemporalGraphDivID,
						self._spatialViewDivID,
						self._dayScaleTimelineDivID,
						self._totalScaleTimelineDivID);
		self._dayTimelineSvgG = tmpDiv.daySvgG;
		self._totalTimelineSvgG = tmpDiv.totalSvgG;
		self._totalTimelineSvgG.call(self._tip);
		self._map = self._createMap(self._spatialViewDivID);

		
	},
	_addListener: function() {
		var self = this;
		observerManager.addListener(self);
	},
	_createDiv: function(_spatialTemporalGraphDivID, _spatialViewDivID, _dayScaleTimelineDivID, _totalScaleTimelineDivID) {
		var self = this;
		var tmpTimelineDiv;
		var tmpDayTimelineSvgG;
		var tmpTotalTimelineSvgG;
		d3.select("#" + _spatialTemporalGraphDivID)
			.append("div")
			.attr("id", _spatialViewDivID);
		tmpTimelineDiv = d3.select("#" + _spatialTemporalGraphDivID)
			.append("div")
			.attr("id", "twoTimelineDiv");
		tmpDayTimelineSvgG = tmpTimelineDiv.append("div")
			.attr("id", _dayScaleTimelineDivID);
		tmpDayTimelineSvgG.append("div")
			.attr("id", _dayScaleTimelineDivID + "-label")
			.append("text")
			.attr("x", 60)
			.attr("y", 15)
			.html("sqrt(Num) by minutes");
		var dayG = tmpDayTimelineSvgG.append("div")
			.attr("id", _dayScaleTimelineDivID + "-svg")
			.append("svg")
			.attr("height", "100%")
			.attr("width", "100%")
			.append("g")
			.attr("transform", "translate(0, 0)");
		tmpTotalTimelineSvgG = tmpTimelineDiv.append("div")
			.attr("id", _totalScaleTimelineDivID);
		tmpTotalTimelineSvgG.append("div")
			.attr("id", _totalScaleTimelineDivID + "-label")
			.append("text")
			.attr("x", 60)
			.attr("y", 15)
			.html("sqrt(Num) by days");
		var totalG = tmpTotalTimelineSvgG.append("div")
			.attr("id", _totalScaleTimelineDivID + "-svg")
			.append("svg")
			.attr("height", "100%")
			.attr("width", "100%")
			.append("g")
			.attr("transform", "translate(0, 0)");
		return {daySvgG: dayG, totalSvgG: totalG};
	},
	_createMap: function(_spatialViewDivID) {
		var self = this;
		var map = new AMap.Map(_spatialViewDivID, {
        	mapStyle: 'amap://styles/39741624a4f8730dd1c218cb2732ae29'
     	});
     	map.setCity("北京市");
     	d3.select("#" + _spatialViewDivID)
     		.selectAll(".amap-copyright")
     		.style("visibility", "hidden");
     	d3.select("#" + _spatialViewDivID)
     		.selectAll(".amap-logo")
     		.style("display", "none");
     	return map;
	},
	_createTraceLayer: function(_map) {
		var self = this;
		console.log("create trace layer");
		_map.plugin(['AMap.CustomLayer'], function() {
			self._canvas = document.createElement('canvas');
			self._canvas.width = _map.getSize().width;
			self._canvas.height = _map.getSize().height;
			var cus = new AMap.CustomLayer(self._canvas, {
				zooms: [0, 12],
				zIndex: 100
			});
			cus.render = onRender;
			cus.setMap(_map);
		});
		console.log(self._canvas);

		function onRender() {
			self._renderTraceLayer();
		}
	},
	_renderTraceLayer: function() {
		console.log("begin to render canvas");
		var self = this;
		var context = self._canvas.getContext('2d');
		var circleBSArray = [];
		context.onmousedown = canvasClick;
		context.clearRect(0, 0, self._canvas.width, self._canvas.height);
		for(var id in self._dataByDate) {
			var messageArray = self._dataByDate[id].action;
			var usedMessageArray = [];
			if(messageArray.length > 1) {
				var startPoint = toPixel(self._map, messageArray[0].lng, messageArray[0].lat);
				var circleBSObject = {};
				circleBSObject.id = id;
				circleBSObject.lng = messageArray[0].lng;
				circleBSObject.lat = messageArray[0].lat;
				circleBSArray.push(circleBSObject);
				usedMessageArray.push(messageArray[0]);
				if(id === dataCenter.globalVariables['selectedBaseStation']) {
					context.strokeStyle = '#D0D0D0';
					context.lineWidth = 2;
				}
				else {
					context.strokeStyle = '#8E8E8E';
					context.lineWidth = 1;
				}
				context.beginPath();
				context.moveTo(startPoint.x, startPoint.y);
				for(var i = 1; i < messageArray.length; i++) {
					if(messageArray[i].conntime 
						=== usedMessageArray[usedMessageArray.length - 1].conntime) {
						continue;
					}
					if(self._calculateTimeInterval(usedMessageArray[usedMessageArray.length - 1].conntime
						, messageArray[i].conntime) < self.TIMEINTERVAL) {
						if(i < messageArray.length - 1) {
							continue;
						}
					}
					
					var point = toPixel(self._map, messageArray[i].lng, messageArray[i].lat);
					
					if(!self.selectedStartTime) {
						context.globalAlpha = 0.1;
					}
					else if(self._calculateTimeInterval(messageArray[i].conntime, self.selectedStartTime) >= 0
						|| self._calculateTimeInterval(usedMessageArray[usedMessageArray.length - 1].conntime, self.selectedEndTime) <= 0) {
						context.globalAlpha = 0;
					}
					else {
						context.globalAlpha = 0;
					}
					context.lineTo(point.x, point.y);
					context.stroke();
					context.beginPath();
					context.moveTo(point.x, point.y);
					usedMessageArray.push(messageArray[i]);
					var circleBSObject = {};
					circleBSObject.id = id;
					circleBSObject.lng = messageArray[i].lng;
					circleBSObject.lat = messageArray[i].lat;
					circleBSArray.push(circleBSObject);
				}
				context.stroke();
			}
			//draw joint-circle between straight lines
			/*for(var i = 0; i < usedMessageArray.length; i++) {
				var point = toPixel(self._map, usedMessageArray[i].lng, usedMessageArray[i].lat);
				var radius;
				context.beginPath();
				context.fillStyle = '#E0E0E0';
				context.lineWidth = 1;
				if(id === dataCenter.globalVariables['selectedBaseStation']) {
					context.strokeStyle = 'black';
					radius = 4;
				}
				else {
					context.strokeStyle = 'gray';
					radius = 2;
				}
				if(usedMessageArray[i].conntime >= self.selectedStartTime &&
					usedMessageArray[i].conntime <= self.selectedEndTime) {
					context.globalAlpha = 0.8;
				}
				else if(!self.selectedStartTime) {
					context.globalAlpha = 0.5;
				}
				else {
					context.globalAlpha = 0.2;
				}
				context.arc(point.x, point.y, radius, 0, Math.PI * 2);
				context.fill();
				context.stroke();
			}*/
			//draw message points
			var messagePoints = getMessagePointsToDraw(usedMessageArray, self._dataByDate[id].send);
			for(var i = 0; i < messagePoints.length; i++) {
				var radius;
				context.beginPath();
				//should be consistence with colors in other view
				context.fillStyle = '#E0E0E0';
				context.lineWidth = 1;
				if(id === dataCenter.globalVariables['selectedBaseStation']) {
					context.strokeStyle = 'lightgray';
					radius = 3;
				}
				else {
					context.strokeStyle = 'gray';
					radius = 2;
				}
				if(self._calculateTimeInterval(messagePoints[i].time, self.selectedStartTime) <= 0
					&& self._calculateTimeInterval(messagePoints[i].time, self.selectedEndTime) >= 0) {
					context.globalAlpha = 1;
				}
				else if(!self.selectedStartTime) {
					context.globalAlpha = 0.6;
				}
				else {
					context.globalAlpha = 0;
				}
				context.arc(messagePoints[i].x, messagePoints[i].y, radius, 0, Math.PI * 2);
				context.fill();
				context.stroke();
			}
			
			function getMessagePointsToDraw(usedMessageArray, messagePointsArray) {
				var pointsArray = [];
				for(var i = 0; i < messagePointsArray.length; i++) {
					var tmpMessage = messagePointsArray[i];
					var time = tmpMessage.time;
					for(var j = usedMessageArray.length - 1; j >= 0; j--) {
						//凡不在此区间内都放弃
						if(self._calculateTimeInterval(usedMessageArray[j].conntime, time) < 0) {
							continue;
						}
						if(j === usedMessageArray.length - 1) {
							break;
						}
						var startPixel = toPixel(self._map, usedMessageArray[j].lng, usedMessageArray[j].lat);
						var endPixel = toPixel(self._map, usedMessageArray[j + 1].lng, usedMessageArray[j + 1].lat);
						var tmpK1 = self._calculateTimeInterval(usedMessageArray[j].conntime, time);
						var tmpK2 = self._calculateTimeInterval(usedMessageArray[j].conntime, usedMessageArray[j + 1].conntime);
						if(tmpK2 === 0) {
							console.log("divide by 0");
						}
						var point = {};
						point.time = time;
						point.type = tmpMessage.type;
						point.x = startPixel.x + (endPixel.x - startPixel.x) * tmpK1 / tmpK2;
						point.y = startPixel.y + (endPixel.y - startPixel.y) * tmpK1 / tmpK2;
						pointsArray.push(point);
						break;
					}
				}
				return pointsArray;
			}
		}
		console.log("render canvas completed");
		function canvasClick(e) {
			var clickX = e.pageX - self._canvas.offsetLeft;
			var clickY = e.pageY - self._canvas.offsetTop;
			for(var i = 0; i < circleBSArray.length; i++) {
				var point = toPixel(self._map, circleBSArray[i].lng, circleBSArray[i].lat);
				var distance = Math.sqrt(Math.pow((point.x - clickX), 2)
					+ Math.pow((point.y - clickY), 2));
				var radius;
				if(circleBSArray[i].id === dataCenter.globalVariables['selectedBaseStation']) {
					radius = 4;
				}
				else {
					radius = 2;
				}
				if(radius >= distance) {
					if(circleBSArray[i].id != dataCenter.globalVariables['selectedBaseStation']) {
						dataCenter.setGlobalVariable('selectedBaseStation'
													, circleBSArray[i].id
													, self.name);
						console.log("change BS to " + circleBSArray[i].id);
						self._renderTraceLayer();
					}
				}
			}
		}
		function toPixel(_map, lng, lat) {
			var pixel = _map.lnglatTocontainer([lng, lat]);
			return {x: pixel.getX(), y: pixel.getY()};
		}
	},
	_calculateTimeInterval: function(time1, time2) {
		var self = this;
		if(time2 === undefined) {
			return false;
		}
		var tmp1 = time1.split(":");
		var tmp2 = time2.split(":");
		var minutes1 = parseInt(tmp1[0], 10) * 60 + parseInt(tmp1[1], 10);
		var minutes2 = parseInt(tmp2[0], 10) * 60 + parseInt(tmp2[1], 10);
		return minutes2 - minutes1;
	},
	/*heatmap*/
	/*_createHeatmapLayer: function(_map) {
		var self = this;
		_map.plugin(["AMap.Heatmap"], function() {
			self._heatmap = new AMap.Heatmap(_map, {
				radius: 25,
				opacity: [0, 0.8]
			});
		});
	},
	_renderHeatmapLayer: function(heatmap, type, ) {
		var self = this;
		heatmap.setDataSet({
			data: coordinatesArray
		});
	},
	_closeHeatmapLayer: function() {
		var self = this;
		self._heatmap.hide();
	},
	_openHeatmapLayer: function() {
		var self = this;
		self._heatmap.show();
	},*/
	_renderTimelineByDate: function(_dayScaleTimelineDivID, _dayTimelineSvgG, _dataByDate) {
		var self = this;
		var INTERVAL = 20;
		var DURATION = 800;
		var svg = d3.select("#" + _dayScaleTimelineDivID)
			.select("svg");
		var totalWidth = $("#" + _dayScaleTimelineDivID + " svg").width();
		var totalHeight = $("#" + _dayScaleTimelineDivID + " svg").height();
		var multiMessageNumByTime = [];
		multiMessageNumByTime.length = (60 * 24) / INTERVAL;
		for(var i = 0; i < multiMessageNumByTime.length; i++) {
			multiMessageNumByTime[i] = {};
			multiMessageNumByTime[i].totalNum = 0;
			multiMessageNumByTime[i].typeNum = [];
			multiMessageNumByTime[i].idNum = [];
		}
		for(var id in _dataByDate) {
			var messages = _dataByDate[id].send;
			for(var i = 0; i < messages.length; i++) {
				var minutes = self._calculateTimeInterval('00:00:00', messages[i].time);
				var index = parseInt(minutes / INTERVAL);
				if(multiMessageNumByTime[index].typeNum[messages[i].type]) {
					multiMessageNumByTime[index].typeNum[messages[i].type] ++;
				}
				else {
					multiMessageNumByTime[index].typeNum[messages[i].type] = 1;
				}
				if(multiMessageNumByTime[index].idNum[id]) {
					multiMessageNumByTime[index].idNum[id] ++;
				}
				else {
					multiMessageNumByTime[index].idNum[id] = 1;
				}
				multiMessageNumByTime[index].totalNum ++;
			}
		}
		renderTimeline();
		return multiMessageNumByTime;

		function renderTimeline() {
			var maxNum = 0;
			var barWidth = (totalWidth / multiMessageNumByTime.length);
			for(var i = 0; i < multiMessageNumByTime.length; i++) {
				if(multiMessageNumByTime[i].totalNum > maxNum) {
					maxNum = multiMessageNumByTime[i].totalNum;
				}
			}
			//axis
			var yScale = d3.scale.linear()
				.domain([0, maxNum * 1.1])
				.range([totalHeight, 0]);
			var xScale = d3.scale.linear()
				.domain([0, multiMessageNumByTime.length])
				.range([0, totalWidth]);
			var xAxis = d3.svg.axis()
				.scale(xScale)
				.orient("bottom")
				.ticks(0);
			var yAxis = d3.svg.axis()
				.scale(yScale)
				.orient("left")
				.ticks(0);
			var xAxisGroup = svg.append("g")
			   .attr("class","x-axis")
			   .attr("transform","translate(" + 0 + "," + totalHeight + ")")
			   .call(xAxis);
			var yAxisGroup = svg.append("g")
				.attr("class","y-axis")
				.call(yAxis);
			var bars = _dayTimelineSvgG.selectAll(".TimelineBarByDate")
				.data(multiMessageNumByTime, function(d, i) {
					return i;
				});
			bars.enter()
				.append("rect")
				.attr("class", "TimelineBarByDate")
				.attr("width", barWidth)
				.attr("height", function(d, i) {
					return totalHeight - yScale(multiMessageNumByTime[i].totalNum);
				})
				.attr("x", function(d, i) {
					return 1 + xScale(i);
				})
				.attr("y", function(d, i) {
					return yScale(multiMessageNumByTime[i].totalNum);
				})
				.on("mouseover", function(d, i) {
					var startminutes = INTERVAL * i;
					var endminutes = INTERVAL * (i + 1);
					var time1;
					var time2;
					if((startminutes / 60) < 10) {
						time1 = '0' + parseInt(startminutes / 60);
					}
					else {
						time1 = parseInt(startminutes / 60);
					}
					if((startminutes % 60) < 10) {
						time1 += ':0' + (startminutes % 60);
					}
					else {
						time1 += ':' + (startminutes % 60);
					}
					if((endminutes / 60) < 10) {
						time2 = '0' + parseInt(endminutes / 60);
					}
					else {
						time2 = parseInt(endminutes / 60);
					}
					if((endminutes % 60) < 10) {
						time2 += ':0' + (endminutes % 60);
					}
					else {
						time2 += ':' + (endminutes % 60);
					}
					time1 += ':00';
					time2 += ':00';
					self._mouseoverBarInDay(multiMessageNumByTime[i], time1, time2);
				})
				.on("mouseout", function(d, i) {
					self._mouoseoutBarInDay();
				})
				.on("click", function(d, i) {
 
				});
			bars.transition()
				.duration(DURATION)
				.attr("height", function(d, i) {
					return totalHeight - yScale(multiMessageNumByTime[i].totalNum);
				})
				.attr("y", function(d, i) {
					return yScale(multiMessageNumByTime[i].totalNum);
				});
			bars.exit()
				.remove();
		}
	},
	_renderTotalTimeline: function(_totalScaleTimelineDivID, _totalTimelineSvgG, _totalData) {
		var self = this;
		var svg = d3.select("#" + _totalScaleTimelineDivID)
			.select("svg");
		var totalHeight = $("#" + _totalScaleTimelineDivID + " svg").height();
		var totalWidth = $("#" + _totalScaleTimelineDivID + " svg").width();
		var maxNum = 0;
		var barWidth = (totalWidth / _totalData.length);
		var dateArray = [];
		dateArray.length = 0;
		for(var i in _totalData) {
			dateArray[dateArray.length] = i;
			if(_totalData[i].total > maxNum) {
				maxNum = _totalData[i].total;
			}
		}
		//axis
		var yScale = d3.scale.linear()
			.domain([0, Math.sqrt(maxNum * 1.1)])
			.range([totalHeight, 0]);
		var xScale = d3.scale.linear()
			.domain([0, _totalData.length + 1])
			.range([0, totalWidth]);
		var xAxis = d3.svg.axis()
			.scale(xScale)
			.orient("bottom")
			.ticks(0);
		var yAxis = d3.svg.axis()
			.scale(yScale)
			.orient("left")
			.ticks(0);
		var xAxisGroup = svg.append("g")
		   .attr("class","x-axis")
		   .attr("transform","translate(" + 0 + "," + totalHeight + ")")
		   .call(xAxis);
		var yAxisGroup = svg.append("g")
			.attr("class","y-axis")
			.call(yAxis);
		var bars = _totalTimelineSvgG.selectAll(".totalTimelineBar")
			.data(_totalData);
		bars.enter()
			.append("rect")
			.attr("class", "totalTimelineBar")
			.attr("width", barWidth)
			.attr("height", function(d, i) {
				return totalHeight - yScale(Math.sqrt(_totalData[dateArray[i]].total));
			})
			.attr("x", function(d, i) {
				return 1 + xScale(i);
			})
			.attr("y", function(d, i) {
				return yScale(Math.sqrt(_totalData[dateArray[i]].total));
			})
			.on("mouseover", function(d, i) {
				self._mouseoverBarInTotal(dateArray[i], _totalData[dateArray[i]].total)
			})
			.on("mouseout", function(d, i) {
				self._mouseoutBarInTotal();
			})
			.on("click", function(d, i) {
				self._clickBarInTotal(dateArray[i]);
			});
	},
	_mouseoverBarInTotal: function(date, total) {
		var self = this;
		self._tip.html(function() {
			return "<b>Date: </b><font color=\"#FF6347\">" 
				+ date
				+ "</font><br><b>total messages: </b><font color=\"#FF6347\">"
				+ total + "</font>";
		});
		self._tip.show();
	},
	_mouseoutBarInTotal: function() {
		var self = this;
		self._tip.hide();
	},
	_clickBarInTotal: function(date) {
		var self = this;
		self._requestDataByDate(date);
	},
	_mouseoverBarInDay: function(data, time1, time2) {
		var self = this;
		self._tip.html(function() {
			return  "<b>time: </b><font color=\"#FF6347\">" 
				+ time1 + " ~ " + time2
				+ "</font><br><b>total messages: </b><font color=\"#FF6347\">"
				+ data.totalNum + "</font>";
		});
		self._tip.show();
		self.selectedStartTime = time1;
		self.selectedEndTime = time2;
		self._renderTraceLayer();
	},
	_mouoseoutBarInDay: function() {
		var self = this;
		self._tip.hide();
		self.selectedStartTime = undefined;
		self.selectedEndTime = undefined;
		self._renderTraceLayer();
	},
	_requestDataByDate: function(date) {
		var self = this;
		console.log(date);
		$.getJSON("/getActionByDate?date=" + date, function(data){
			console.log(data);
			self._dataByDate = data;
			self._multiMessageNumByTime = self._renderTimelineByDate(self._dayScaleTimelineDivID, 
										self._dayTimelineSvgG, 
										self._dataByDate);
			if(self._ifCreatedLayer === false) {
				self._ifCreatedLayer = true;
				self._createTraceLayer(self._map);
			}
			else {
				self._renderTraceLayer();
			}
		});
	},
	_requestTotalData: function() {
		var self = this;
		$.getJSON("/getAllTypeMessage",function(data){
			console.log(data);
			self._totalData = data;
			var count = 0;
			for(var date in self._totalData) {
				count ++;
			}
			self._totalData.length = count;
			self._renderTotalTimeline(self._totalScaleTimelineDivID, 
									self._totalTimelineSvgG, 
									self._totalData);
		});
	},
	OMListen: function(message, data) {
		var self = this;
		if(message === "") {
			//handle message
		}
	}
}
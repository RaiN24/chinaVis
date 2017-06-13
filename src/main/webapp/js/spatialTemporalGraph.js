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
	selectedStartTime: undefined,
	selectedEndTime: undefined,
	TIMEINTERVAL: 5,
	_tip: d3.tip().attr("class", "d3-tip"),

	initialize: function(divID) {
		var self = this;
		var tmpDiv;
		self._addListener();
		self._spatialTemporalGraphDivID = divID;
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
			.attr("id", _dayScaleTimelineDivID)
			.append("svg")
			.attr("height", "100%")
			.attr("width", "100%")
			.append("g")
			.attr("transform", "translate(0, 0)");
		tmpTotalTimelineSvgG = tmpTimelineDiv.append("div")
			.attr("id", _totalScaleTimelineDivID)
			.append("svg")
			.attr("height", "100%")
			.attr("width", "100%")
			.append("g")
			.attr("transform", "translate(0, 0)");
		return {daySvgG: tmpDayTimelineSvgG, totalSvgG: tmpTotalTimelineSvgG};
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
		_map.plugin(['AMap.CustomLayer'], function() {
			self._canvas = document.createElement('canvas');
			self._canvas.width = _map.getSize().width;
			self._canvas.height = _map.getSize().height;
			var cus = new AMap.CustomLayer(self._canvas, {
				zooms: [3, 8],
				zIndex: 12
			});
			cus.render = self._renderTraceLayer;
			cus.setMap(_map);
		});
	},
	_renderTraceLayer: function() {
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
					if((messageArray[i].conntime >= self.selectedStartTime)
						|| (usedMessageArray[usedMessageArray.length - 1].conntime <= self.selectedEndTime)) {
						context.globalAlpha = 0.2;
					}
					else if(!self.selectedStartTime) {
						context.globalAlpha = 0.5;
					}
					else {
						context.globalAlpha = 0.8;
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
			for(var i = 0; i < usedMessageArray.length; i++) {
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
			}
			//draw message points
			var messagePoints = getMessagePointsToDraw(usedMessageArray, self._dataByDate[id].send);
			for(var i = 0; i < messagePoints.length; i++) {
				var radius;
				context.beginPath();
				//should be consistence with colors in other view
				context.fillStyle = '#E0E0E0';
				context.lineWidth = 1;
				if(id === dataCenter.globalVariables['selectedBaseStation']) {
					context.strokeStyle = 'black';
					radius = 3;
				}
				else {
					context.strokeStyle = 'gray';
					radius = 2;
				}
				if(messagePoints[i].recitime >= self.selectedStartTime &&
					messagePoints[i].recitime <= self.selectedEndTime) {
					context.globalAlpha = 0.8;
				}
				else if(!self.selectedStartTime) {
					context.globalAlpha = 0.5;
				}
				else {
					context.globalAlpha = 0.2;
				}
				context.arc(messagePoints[i].x, messagePoints[i].y, radius, 0, Math.PI * 2);
				context.fill();
				context.stroke();
			}

			function getMessagePointsToDraw(usedMessageArray, messagePointsArray) {
				var pointsArray = [];
				for(var i = 0; i < messagePointsArray.length; i++) {
					var tmpMessage = messagePointsArray[i];
					var recitime = tmpMessage.recitime;
					for(var j = usedMessageArray.length - 1; j >= 0; j--) {
						//凡不在此区间内都放弃
						if(usedMessageArray[j].conntime > recitime) {
							continue;
						}
						if(j === usedMessageArray.length - 1) {
							break;
						}
						var startPixel = toPixel(self._map, usedMessageArray[j].lng, usedMessageArray[j].lat);
						var endPixel = toPixel(self._map, usedMessageArray[j + 1].lng, usedMessageArray[j + 1].lat);
						var tmpK1 = self._calculateTimeInterval(usedMessageArray[j].conntime, recitime);
						var tmpK2 = self._calculateTimeInterval(usedMessageArray[j].conntime, usedMessageArray[j + 1].conntime);
						if(tmpK2 === 0) {
							console.log("divide by 0");
						}
						var point = {};
						point.recitime = recitime;
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
		var totalWidth = svg.attr("width");
		var totalHeight = svg.attr("height");
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
				var minutes = self._calculateTimeInterval('00:00:00', messages[i].recitime);
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
		renderTotalTimeline();
		return multiMessageNumByTime;

		function renderTotalTimeline() {
			var maxNum = 0;
			var barWidth = (totalWidth / multiMessageNumByTime.length) - 1;
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
			yAxisGroup.append("text")
				.attr("class","label")
				.attr("x", 0)
				.attr("y", -5)
				.attr("transform","rotate(-90)")
				.style("text-anchor","end")
				.text("Num");
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

				})
				.on("mouseout", function(d, i) {

				})
				.on("click", function(d, i) {

				});
			bars.transition()
				.duration(DURATION)
				.attr("height", function(d, i) {
					return totalHeight - yScale(multiMessageNumByTime[i].totalNum);
				});
			bars.exit()
				.remove();
		}
	},
	_renderTotalTimeline: function(_totalScaleTimelineDivID, _totalTimelineSvgG, _totalData) {
		var self = this;
		var svg = d3.select("#" + _totalScaleTimelineDivID)
			.select("svg");
		var totalHeight = svg.attr("height");
		var totalWidth = svg.attr("width");
		var maxNum = 0;
		var barWidth = (totalWidth / _totalData.length) - 1;
		for(var i = 0; i < _totalData.length; i++) {
			if(_totalData[i].total > maxNum) {
				maxNum = _totalData[i].total;
			}
		}
		//axis
		var yScale = d3.scale.linear()
			.domain([0, maxNum * 1.1])
			.range([totalHeight, 0]);
		var xScale = d3.scale.linear()
			.domain([0, _totalData.length])
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
		yAxisGroup.append("text")
			.attr("class","label")
			.attr("x", 0)
			.attr("y", -5)
			.attr("transform","rotate(-90)")
			.style("text-anchor","end")
			.text("Num");
		var bars = _dayTimelineSvgG.selectAll(".totalTimelineBar")
			.data(_totalData);
		bars.enter()
			.append("rect")
			.attr("class", "totalTimelineBar")
			.attr("width", barWidth)
			.attr("height", function(d, i) {
				return totalHeight - yScale(_totalData[i].total);
			})
			.attr("x", function(d, i) {
				return 1 + xScale(i);
			})
			.attr("y", function(d, i) {
				return yScale(_totalData[i].total);
			})
			.on("mouseover", function(d, i) {

			})
			.on("mouseout", function(d, i) {

			})
			.on("click", function(d, i) {

			});
	},
	_mouseoverBarInTotal: function(d) {
		var self = this;
		self._tip.html(function() {
			return "<b>Date: </b><font color=\"#FF6347\">" 
				+ d.date 
				+ "</font><br><b>total messages: </b><font color=\"#FF6347\">"
				+ d.total + "</font>";
		});
		self._tip.show();
	},
	_mouseoutBarInTotal: function(d) {
		var self = this;
		self._tip.hide();
	},
	_requestDataByDate: function() {
		
	},
	_requestTotalData: function() {
		$.getJSON("/getMessagesByPhone?phone=10690000567890",function(data){
			d=data;
		});
	},
	OMListen: function(message, data) {
		var self = this;
		if(message === "") {
			//handle message
		}
	}
}
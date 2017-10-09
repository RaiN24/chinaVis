//todo 高亮当前选中时间
var baseStationOverview = {
	name:"baseStationOverview",
	$bsView:undefined,
	_tip: d3.tip().attr("class", "d3-tip").attr('id', 'celltip'),
	_height:undefined,
	_width:undefined,
	_svgHeight:undefined,
	_svgWidth:undefined,
	_timematrix:undefined,
	_defaultStation:1,
    
	initialize: function() {
		var self = this;
		self._addListener();

		self.$bsView=$("#basestation-view");
		var $rbDiv=$("#right-bottom-bottom-div");

		self._width=$rbDiv.width();
		self._height=$rbDiv.height();
		self._svgWidth=self._width*0.8;
		// self._svgHeight=self._height;

	    self._getData(self._defaultStation);
	    self._createLegend();
	},

	outputData:function(selectedBaseStation){

	},

	_addListener: function() {
		var self = this;
		observerManager.addListener(self);
	},
    
    //day-24hour matrix
    _createTimeMatrix:function(data){
    	var self=this;
	    $("#basestation-view svg:first-child").remove();

		var sortDate=function(a,b){
			var [year1,day1,hour1]=a.split("-"),[year2,day2,hour2]=b.split("-");
			if(day1===day2){
				return hour1-hour2;
			}
			else return day1-day2;
		};
    	var days=Object.keys(data).sort(sortDate),
    	    yCount=days.length,
			rectMargin = {top: 20, text: 20, left: 50,right:10,bottom:10},
			mainWidth=self._svgWidth-rectMargin.left-rectMargin.right;

    	var unitW=unitH=mainWidth/24;
    	var mainHeight=unitH*yCount;
    	self._svgHeight=mainHeight+rectMargin.top+rectMargin.bottom;

		var svg=d3.select(self.$bsView[0])
		          .append("svg")
		          .attr({
		          	width:self._svgWidth,
		          	height:self._svgHeight,
		          })
		          .style({
		          	top:5,
		          	left:10,
		          });

		//blank rects
		var rectGroup=svg.append("g").attr("class","rects").attr("transform", "translate(" + rectMargin.left + "," + rectMargin.top + ")");
		for(var i=0;i<24*yCount;i++){
			rectGroup.append("rect")
			    .attr({
		            class: 'cell',
		            width: unitW,
		            height: unitH,
		            x: function(d) { return (i%24)* unitW; },
		            y: function(d) { return   Math.floor(i/24)* unitH; },
		            stroke:"#222",
		            "stroke-width":"1px",
		            fill:"#444",
		        })
		}


	    //add axis
	    var yaxisGroup=svg.append("g").attr("class","yaxis").attr("transform", "translate(" + rectMargin.text + "," +rectMargin.top + ")");
	    var xaxisGroup=svg.append("g").attr("class","xaxis").attr("transform", "translate(" + rectMargin.left + "," +0 + ")");
		//y day axix //todo 添加是周几的信息 且在周一处加分割线
		var i=0;
		for(var day of days){
			yaxisGroup.append("text")
					  .attr({
				           x: 0,
				           y: i*unitH+unitH/1.5,
				           fill:"#ccc",
				           "font-size":10,
				       })
				       .text(day.split("-")[1]+"-"+day.split("-")[2]); //只显示月-日 不显示年

			i+=1;
		};

		//x hour axis
		for(var j=0;j<=24;j+=4){
			xaxisGroup.append("text")
					 .attr({
			            x: j* unitW,
			            y: unitH,
			            fill:"#ccc",
			            "font-size":10,
			        })
			        .text(j);

			xaxisGroup.append("line")
			         .attr({
			         	x1:j* unitW,
			         	x2:j* unitW,
			         	y1:rectMargin.top-5,
			         	y2:rectMargin.top+mainHeight,
			         	stroke:"black",
			         	"stroke-width":"2px",
			         })
		}  

        //drawCircles
	    var circleGroup=svg.append("g")
	                       .attr("class","circles")
	                       .attr("transform", "translate(" +rectMargin.left + "," + rectMargin.top + ")");
	    var countMax=0,circleData=[];
		for(var dayth=0;dayth<days.length;dayth+=1){
			var day=days[dayth];
			for(var hour in data[day]){
				data[day][hour].day=day;
				data[day][hour].hour=hour;
				data[day][hour].dayth=dayth; //todo 之后写一个计算日期差的，就不需要了
				circleData.push(data[day][hour]);
				if(countMax<data[day][hour].total){
					countMax=data[day][hour].total;
				}
			}
		}

	    var scaler=d3.scale.pow().exponent(.25).domain([0,countMax]).range([0,unitW/2])
		for(var data of circleData){
			self._drawOnePi(data,scaler(data.total),data.hour* unitW+unitW/2,data.dayth* unitH+unitH/2,circleGroup);
		}

	     /* Initialize tooltip*/
        self._tip.html(function(d) {return d.regions+"\<br\>"+"total:"+d.total; });
		//add tip
	    circleGroup.call(self._tip);
    },

	//初始化celltip
	_updateCellTip:function(data){
		var self=this;
        $("#celltip").remove("svg");
        
        var unitW=unitH=self._svgWidth/(3*6);
        // self._tip.html(function(d) { console.log(d);return d.regions+"\<br\>"+d.total; });
	    var cellTipSvg=d3.select("#celltip").append("svg").attr("width",unitW*6).attr("height",unitH*2).attr("transform","translate("+(-unitW)+","+0+")");
	    cellTipSvg.append("g").attr("class","rects");
	    cellTipSvg.append("g").attr("class","circles");
	    cellTipSvg.append("g").attr("class","axis");

		var minuteData=[];
		var countMax=0;
		for(var key in data){
			if(!isNaN(key)){
				// console.log(key)
				data[key].minute=key;
				minuteData.push(data[key]);
				if(countMax<data[key].count){
					countMax=data[key].count;
				}
			}
		}
		// console.log(minuteData)

		//一行矩形格子
		var rectGroup=d3.select("#celltip .rects");
	    for(var i=0;i<6;i++){
	    	rectGroup.append("rect")
			    .attr({
		            class: 'cell',
		            width: unitW,
		            height: unitH,
		            x: function(d) { return (i%24)* unitW; },
		            y: function(d) { return   Math.floor(i/24)* unitH; },
		            stroke:"#222",
		            "stroke-width":"1px",
		            fill:"#444",
		        })
	    }
	    
	    var circleGroup=d3.select("#celltip .circles")
	    var scaler=d3.scale.pow().exponent(.25).domain([0,countMax]).range([0,unitW/2])
	    for(var data of minuteData){
			self._drawOnePi(data,scaler(data.count),data.minute*unitW+unitW/2,unitH/2,circleGroup);
		}
	},

	_createLegend:function(){
		//todo 显示基站id,region的颜色图例

	},

	_getData:function(selectedBaseStation){
		var self=this;
		//遍历该基站对应的所有电话号码，获取数据
		
		var url="/getJizhanBitMap?jizhan="+selectedBaseStation;

		$.getJSON(url,function(data){
		    console.log(data);
	        self._createTimeMatrix(data);
		});	 
		// d3.json("testdata/view1.json", function(error, data){
		// 	self._createTimeMatrix(data);
		// })
	},

	_eventHandler:function(){
		var self=this;
	},

	_drawOnePi:function(oridata,r,dx,dy,group){
		var self=this;
		// console.log(oridata)
		var regions=oridata.regions,		   
	       dataset=Array.apply(null, Array(regions.length)).map(function(item, i) {return 1;}),//每个地区占的比例相同;
	        //转化数据为适合生成饼图的对象数组
	    	pie=d3.layout.pie(dataset),
	    	arc=d3.svg.arc()
	        .outerRadius(r)
	        .innerRadius(0);
	    
	   var tipdata={
	   	  "regions":regions.map(function(item,i){
	   	  	return dataCenter.globalVariables.regionsMap[item];
	   	  }),
	   	  "total": oridata.total?oridata.total:oridata.count,
	   }
	    //准备分组,把每个分组移到图表中心
	    var arcs=group.append("g").datum(tipdata)
	        .attr("transform","translate("+(dx)+","+(dy)+")")//translate(a,b)a表示横坐标起点，b表示纵坐标起点
	        .on('mouseover', self._tip.show) //考虑是添到arc上还是整个圆上
	        .on('mouseout', self._tip.hide)
	        .on('click',function(){
	        	self._tip.show;
	        	self._updateCellTip(oridata);
	        })
	        .selectAll("path")
	        .data(pie(dataset))
	        .enter()
	        .append("path")//每个g元素都追加一个path元素用绑定到这个g的数据d生成路径信息
	        .attr("fill",function(d,i){//填充颜色
	            return dataCenter.globalVariables.colorPannel[regions[i]];//todo 和rigion的颜色对应
	        })
	        .attr("d",arc)//将角度转为弧度（d3使用弧度绘制）
	},

	OMListen: function(message, data) {
		var self = this;
		if(message === "set:selectedBaseStation") {
			//handle message
			var station=dataCenter.globalVariables.selectedBaseStation;
			console.log(station)
			if(station!==null){
				console.log(station)
				self._getData(station);
			}
			else{            
		    	self._getData(self._defaultStation);
			}
		}
	}
}


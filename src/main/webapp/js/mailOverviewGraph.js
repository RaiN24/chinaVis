//sanky：三个轴 短信类型-发送时间-地区
//todo 轴可交换 类型-地区-时间
//todo hover的效果改 每次三维度有关联的高亮，不要只高亮一段
//url: /getTypeTimeAreaByDate2?date=2017-3-25   "/getTypeAreaTimeByDate2?date="

//桑基图来描述区域类型时间的分布关系
var mailOverviewGraph={
    name:"mailOverviewGraph",
    $mailView:undefined,
    selectedType:undefined,
    _tip: d3.tip().attr("class", "d3-tip").attr('id', 'nodetip'),
    _height:undefined,
    _width:undefined,
    _svgHeight:undefined,
    _svgWidth:undefined,
    _defaultDate:"2017-2-25",


    initialize: function(){
        var self=this;
        self.$mailView=$('#mail-category-view');
        var $rtDiv=$("#right-bottom-top-div");
        self._width=$rtDiv.width();
        self._height=$rtDiv.height();
        self._svgWidth=self._width*0.9;
        self._svgHeight=self._height;
        self._addListener();
        self._getData("TAT",self._defaultDate);
        self._createLegend()

    },

    _addListener: function() {
        var self = this;
        observerManager.addListener(self);
    },

    _getData:function(form,selectedDate){
        var self=this;
        var nodeUrl="staticdata/node.json";//该文件为静态文件

        //todo 
        var linkUrl=((form==="TTA")?"/getTypeTimeAreaByDate2?date=":"/getTypeAreaTimeByDate2?date=")+selectedDate;
        
        d3.json(nodeUrl,function(error, onodes){
            $.getJSON(linkUrl,function(olinks){
                
                var data={};
                data.nodes=onodes.nodes;
                data.links=olinks.links;
                restructData(data);
                console.log(data);
                self._createSankeyGraph(data);
            }); 
        })  

        // d3.json("testdata/node.json", function(error, onodes) {
        //     d3.json("testdata/typeareatime.json", function(error, olinks){
        //         // console.log(onodes,olinks.links)
        //         var data={};
        //         data.nodes=onodes.nodes;
        //         data.links=olinks.links;
        //         restructData(data);
        //         self._createSankeyGraph(data); 
        //     })
        // });  

        function restructData(data){
            var links=data.links;
            var nodes=data.nodes;
            var added=[];

            for(var each of links){//添加另一维度数据

                var targetIndex=nodes.findIndex(function(node){return node.name===each.target;});
                targetIndex=(targetIndex===-1)?(form==="TAT"?21:15):targetIndex;
                for(var key in each.value){
                    var addedIdex=added.findIndex(function(link){
                        return link.source===targetIndex&&link.target===key;
                    });

                    if(addedIdex===-1){//add a new link
                       added.push({
                          "source":targetIndex,
                          "target":key,
                          "value":each.value[key]
                       })
                    }
                    else{
                        added[addedIdex].value+=each.value[key];
                    }

                }
            }

            links=links.concat(added);

            for(var each of links){
                var targetIndex=nodes.findIndex(function(node){
                    return node.name===each.target;
                });

                each.target=targetIndex===-1?(form==="TAT"?15:21):targetIndex//改为序号 【！！】todo:暂时的方案 实际上不应该有为空的情况
                var total=0;
                if(!isNaN(each.value)){
                    total=each.value;
                }
                else{
                    for(var key in each.value){
                        total+=each.value[key];
                    }
                }
                each.value=total; //改为总和
            }
            data.links=links;
        }
    },
    
    _createSankeyGraph:function(data){
        var self=this,width=self._svgWidth,height=self._svgHeight;
        var margin = {top: 20, right: 30, bottom: 30, left: 30},
            skwidth=width- margin.left - margin.right,
            skheight=height- margin.top - margin.bottom;

        $("#mail-category-view svg:first-child").remove();

        var svg=d3.select(self.$mailView[0]).append("svg")
                        .attr("width", width)
                        .attr("height", height)
                        .style({
                            top:10,
                            left:10
                        })
                        ;

        var sankeyVis=svg.append("g")
                        .attr('class',"sankey")
                        .attr("transform", "translate(" + margin.top + "," + margin.left + ")");

        var sankey = d3.sankey()
                .nodeWidth(15) 
                .nodePadding(10) 
                .size([skwidth, skheight]) 
                .nodes(data.nodes)  
                .links(data.links)
                .layout(5); //桑基布局用来优化流布局的时间

        // // 路径数据生成器
        var path = sankey.link();

        // 绑定连接数据
        var links = sankeyVis.append("g").selectAll("path")
                    .data(data.links)//todo 绑定连接数据能否包含进更细致的
                    .enter()

        // 绑定节点数据
        var nodes = sankeyVis.append("g").selectAll(".nodes")
                        .data(data.nodes)
                        .enter()
                        .append("g").attr("class","node")
                        ;

        
        nodes.call(self._tip);
        //绘制连接
        // var linkcolor = d3.scale.category20();
        var link=links.append("path")
            .attr({
                fill: "none",   //填充色
                stroke: function(d,i){ return "gray"; },  
                "stroke-opacity": 0.5,  //描边透明度
                d: path,  //路径数据
                id: function(d,i){ return d.source.name +'_'+d.target.name },  //ID
                class:"link",
            })
            .style("stroke-width",function(d){
                return Math.max(1,d.dy);
            })
            .sort(function(a,b){ //？
                return b.dy - a.dy;
            })
            ;


        // // 绘制连接文本 ？？
        // links.append('text')
        //     .append('textPath')
        //     .attr('xlink:href', function (d,i) { return '#link' + i; })
        //     .attr('startOffset','50%')
        //     .text(function (d) { return d.value; });

        // //绘制节点时给节点添加拖动行为，并绑定拖动事件监听器。
        
            
        // d3.select("mail-category-view .node rect").
        
        nodes.append("text")
            .attr("text-anchor","middle")
            .attr({
                x: function (d) { return d.x+sankey.nodeWidth()*2; },
                y: function (d) { return d.y+d.dy / 2; },
                fontSize:8,
                "fill":"white",
                "stroke":"none",

            })
            .filter(function(d) {
                return d.dy >0 ;
            })
            .text(function(d) { 
                if(d.name.length==1) {
                    return Number(d.name)*4+"-"+(Number(d.name)*4+4);
                }
            });
        
        // 绘制矩形节点   
        nodes.append("rect")
            .attr({
                    x: function (d) { return d.x; },
                    y: function (d) { return d.y; },
                    height: function (d) {
                        if(d.value===0) return 0;
                        else{
                            return Math.max(2,Math.abs(d.dy)); 
                        }
                    },
                    width: sankey.nodeWidth(), 
                    fill:  function(d,i){
                        if(d.name.slice(0,4) === "type") return d.color=dataCenter.globalVariables.colorPannel[dataCenter.globalVariables.typesMap[d.name]];
                        else return d.color=dataCenter.globalVariables.colorPannel[d.name];
                    },
            })
            .on("mouseover",function(d){
                self._tip.html(function(){
                    if(d.name.slice(0,2)==="11" || d.name.slice(0,2)==="13"){
                        return dataCenter.globalVariables.regionsMap[d.name];
                    }
                    else if(d.name.length===1){
                        return Number(d.name)*4+"-"+(Number(d.name)*4+4);
                    }
                    else{
                        return dataCenter.globalVariables.typesMap[d.name];
                    }
                })
                self._tip.show();
            })
            .on("mouseout",function(){
                self._tip.hide();
            })
            .on("click",function(d){
                if(isNaN(Number(d.name))){
                    var typeIndex=data.nodes.findIndex(function(node){return node.name===d.name;}).toString();
                    dataCenter.setGlobalVariable('selectedType',typeIndex,self.name)
                }
                //高亮所有相关link
                link.attr({
                        stroke: function(path,i){ 
                            if(path.source.name === d.name || path.target.name === d.name) return d.color;
                            else return "gray"; 
                        }
                    })
            })
            .call(d3.behavior.drag()
                            .origin(function(d) { return d; }) //防止拖动时出现跳动
                            .on("drag", dragmove)
            );  

        // 拖动事件响应函数
        //在拖动过程中重新计算矩形的坐标位置,并重新启动桑基布局，之后给路径元素绑定新的路径数据
        function dragmove(d) {
             d3.select(this).attr({
                "x":d.x,//仅竖直方向拖动
                "y": (d.y = Math.max(0, d3.event.y))
             });

             sankey.relayout();
             link.attr('d',path);
        }

        var labels=["Message type","Distribution area","Time quantum"];
        var categorys=svg.append('g').attr("class","ctgry");

        categorys.selectAll("text").data(labels)
                 .enter()
                 .append("text")
                 .attr({
                    x:function(d,i){return width/3*i+15*i+40;},
                    y:15,
                    fill:"white",
                    fontSize:12,
                 })
                 .text(function(d){return d;});

        //todo 拖动轴 交换
        $("#right-bottom-bottom-div").click(function(e){
            if(e.target.nodeName !== 'rect') {
                console.log(e.target.nodeName)
                link.attr({stroke: "gray"})
            } 
        });
    },

    _createLegend:function(){
        
    },


    OMListen: function(message, data) {
        var self = this;

        if(message ==="set:selectedDate") {
            //handle message
            var date=dataCenter.globalVariables.selectedDate;
                console.log(date,date!==null)
                

            if(date!==null){
                self._getData('TAT',date);
            }
            else{
                self._getData('TAT',self._defaultDate);
            }
        }
    }
  
}
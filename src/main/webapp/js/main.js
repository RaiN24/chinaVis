$(document).ready(function() {
	//整个页面的初始化部分

    //获得当前窗口的大小并设置各个view的尺寸
    var wWidth = window.innerWidth;
    var wHeight = window.innerHeight;

    $("#left-div").height(wHeight - $("#title-div").height() - 8);
    $("#right-div").height(wHeight - $("#title-div").height() - 4);

    viewInitialize();
});

function viewInitialize() {
	spatialTemporalGraph.initialize("spatial-temporal-view");
	mailOverviewGraph.initialize();
	baseStationOverview.initialize();

    dataCenter.setGlobalVariable('selectedBaseStation',4,"spatialTemporalGraph")
    dataCenter.setGlobalVariable('selectedDate',"2017-3-25","spatialTemporalGraph")
}


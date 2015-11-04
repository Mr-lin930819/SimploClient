import QtQuick 2.4
import "QChart.js" as Charts
import "QChartGallery.js" as ChartsData

Item {
    id:self
    width: 720
    height: 1280

    Column{
        anchors.fill: parent
        QChart {
          id: chart_bar_top;
          width: parent.width;
          height: parent.height/2;
          chartAnimated: true;
          chartAnimationEasing: Easing.OutBounce;
          chartAnimationDuration: 2000;
          chartData: ChartsData.ChartBarTopData;
          chartType: Charts.ChartType.BAR;
        }
        QChart {
          id: chart_bar_bottom;
          width: parent.width;
          height: parent.height/2;
          chartAnimated: true;
          chartAnimationEasing: Easing.OutBounce;
          chartAnimationDuration: 2000;
          chartData: ChartsData.ChartBarBottomData;
          chartType: Charts.ChartType.BAR;
        }
    }
    Behavior on  opacity {
        NumberAnimation {
            id: bouncebehavior
            duration: 1000
            easing.type: Easing.InQuint
        }
    }

}


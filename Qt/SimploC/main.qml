import QtQuick 2.4
import QtQuick.Controls 1.3
import QtQuick.Window 2.2
import QtQuick.Dialogs 1.2
import QtQuick.Controls.Styles 1.4
import "QChart.js" as Charts
import "QChartGallery.js" as ChartsData
import DataTrans 1.0

ApplicationWindow {
    id:applicationWindow
    title: qsTr("Hello World")
    width: Qt.platform === "android"? Screen.width : 640
    height: Qt.platform === "android"? Screen.height : 480
    visible: true

    menuBar: MenuBar {
        style: MenuBarStyle{
            background:Rectangle{
                color: "#00ff00"
            }
        }
        Menu {
            title: qsTr("&File")


            MenuItem {
                text: qsTr("&Open")
                onTriggered: messageDialog.show(qsTr("Open action triggered"));
            }
            MenuItem {
                text: qsTr("E&xit")
                onTriggered: Qt.quit();
            }
        }
    }

    DataTransType{
        id:dataTrans
    }

    MainForm{
        id:mainForm
        anchors.fill: parent
        button1.onClicked: {

            state = "ResultPage"
        }
    }

}

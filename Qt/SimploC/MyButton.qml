import QtQuick 2.4
import QtQuick.Controls 1.4

Button{
    property string label: ""
    property int textSize: 20
    text: ""
    Text{
        anchors.fill: parent
        text: label
        font.pixelSize: textSize
        anchors.centerIn: parent
        horizontalAlignment: Text.AlignHCenter
        verticalAlignment: Text.AlignVCenter

    }

}


import QtQuick 2.4
import QtQuick.Controls 1.4
Rectangle {
    property alias loginButton: myButton1
    Text {
        id: userInput
        x: parent.width/10
        y: parent.height/8
        width: parent.width/5
        height: parent.height/20
        text: qsTr("用户名")
        font.family: "Arial"
        font.pixelSize: parent.width/20
    }

    TextField {
        id: textField1
        x: userInput.x+userInput.width/3*4
        y: userInput.y
        width: userInput.width*2.5
        height: userInput.height
        placeholderText: qsTr("Text Field")
    }

    Text {
        id: passwdInput
        x: userInput.x
        y: userInput.y+userInput.height*2.5
        width: parent.width/5
        height: parent.height/20
        text: qsTr("密码")
        font.pixelSize: parent.width/20
        font.family: "Arial"
    }

    TextField {
        id: textField2
        x: textField1.x
        y: passwdInput.y
        width: userInput.width*2.5
        height: userInput.height
        placeholderText: qsTr("Text Field")
    }

    Text {
        id: checkInput
        x: userInput.x
        y: passwdInput.y+passwdInput.height*2.5
        width: parent.width/5
        height: parent.height/20
        text: qsTr("验证码")
        font.pixelSize: parent.width/20
        font.family: "Arial"
    }

    TextField {
        id: textField3
        x: textField1.x
        y: checkInput.y
        width: userInput.width*2.5
        height: userInput.height
        placeholderText: qsTr("Text Field")
    }

    Image {
        id: image1
        x: 447
        width: textField3.width/2
        height: 82
        anchors.topMargin: userInput.height*1.5
        anchors.top: textField3.bottom
        source: "qrc:/qtquickplugin/images/template_image.png"
    }

    MyButton {
        id: myButton1
        x: userInput.x
        y: parent.height/15*12
        width: userInput.width/3*4+textField1.width
        height: userInput.height*1.5
        textSize: parent.width/15
        label: qsTr("Login")
    }
}


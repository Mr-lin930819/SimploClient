import QtQuick 2.4
import QtQuick.Controls 1.3
import QtQuick.Layouts 1.1

Item {
    id: item1
    width: 720
    height: 1280

    property alias button1: loginPage1.loginButton

    GradeShowForm {
        id: gradeShowForm1
        anchors.fill: parent
        opacity: 0
    }

    LoginPage {
        id: loginPage1
        anchors.fill: parent
    }


    states: [
        State {
            name: "ResultPage"

            PropertyChanges {
                target: item1
                width: 720
                height: 1280
            }

            PropertyChanges {
                target: gradeShowForm1
                opacity: 1
            }

            PropertyChanges {
                target: loginPage1
                visible: false
            }
        }
    ]
}
